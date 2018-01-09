import json
import threading
import jsonschema
from iofog_container_sdk.client import IoFogClient, IoFogException
from iofog_container_sdk.iomessage import IoMessage
from iofog_container_sdk.listener import *
from paho.mqtt import client
from util import *

clientLock = threading.Lock()


def update_config():
    attempt_limit = 5
    global config

    while attempt_limit > 0:
        try:
            config = fogClient.get_config()
            break
        except IoFogException, ex:
            attempt_limit -= 1
            print str(ex)

    if attempt_limit == 0:
        print 'Config update failed :('
        return

    try:
        jsonschema.validate(config, config_schema)
    except jsonschema.ValidationError as e:
        print 'Error while validating config: {}'.format(e)
        print 'Nothing is updated'
        return

    print 'Got new valid config: {}'.format(config)
    update_mqtt_client(config)


def update_mqtt_client(config):
    global mqttClient
    if not mqttClient:
        mqttClient = MqttClient(config[BROKER], config.get(USER), config.get(CA_CERT))
        mqttClient.connect()
    elif mqttClient.broker != config[BROKER] or mqttClient.ca_cert != config.get(CA_CERT):
        with mqttClient.reconnect_lock:
            mqttClient.reconnect_needed = False
            if mqttClient.reconnect_timer:
                mqttClient.reconnect_timer.cancel()
        with mqttClient.subscribe_lock:
            mqttClient.resubscribe_needed = False
            if mqttClient.subscribe_timer:
                mqttClient.subscribe_timer.cancel()
        mqttClient.disconnect()
        while mqttClient.connected:
            pass
        with clientLock:
            mqttClient = MqttClient(config[BROKER], config.get(USER), config.get(CA_CERT))
        mqttClient.connect()
    mqttClient.update_subscriptions(config.get(SUBSCRIPTIONS, []))


class ControlListener(IoFogControlWsListener):
    def on_control_signal(self):
        update_config()


class MessageListener(IoFogMessageWsListener):
    def on_message(self, io_msg):
        if io_msg.infotype == MQTT_INFO_TYPE and io_msg.infoformat == MQTT_INFO_FORMAT:
            mqtt_message = json.loads(str(io_msg.contextdata))
            callPublish(mqtt_message, io_msg)
        else:
            mqtt_messages = config.get(PUBLISHERS, [])
            for msg in mqtt_messages:
                callPublish(msg, io_msg)

def callPublish(message, io_msg):
    message[PAYLOAD] = io_msg.contentdata
    with clientLock:
        if mqttClient:
            mqttClient.publish(message)


class MqttClient:
    connected = False

    def __init__(self, broker, user, ca_cert):
        self.broker = broker
        self.ca_cert = ca_cert
        self.subscriptions = []
        self.mqttClient = client.Client(fogClient.id, transport=broker.get(TRANSPORT, TCP))
        if user:
            self.mqttClient.username_pw_set(user[USERNAME], user.get(PASSWORD))
        if ca_cert:
            write_certificate(ca_cert)
            self.mqttClient.tls_set(CERT_FILE_LOCATION)
        self.mqttClient.on_connect = MqttClient.on_connect
        self.mqttClient.on_disconnect = MqttClient.on_disconnect
        self.mqttClient.on_message = MqttClient.on_message
        self.reconnect_needed = True
        self.resubscribe_needed = True
        self.reconnect_timer = None
        self.reconnect_lock = threading.Lock()
        self.subscribe_timer = None
        self.subscribe_lock = threading.Lock()
        self.connectAttempt = 0
        self.connectTimeout = 5

    def disconnect(self):
        self.mqttClient.disconnect()

    def connect(self):
        worker = threading.Thread(target=self._connect_to_broker)
        worker.start()

    def _connect_to_broker(self):
        try:
            self.mqttClient.connect(self.broker[HOST],
                                    port=self.broker.get(PORT, DEFAULT_PORT),
                                    bind_address=self.broker.get(BIND_ADDRESS, DEFAULT_ADDRESS))
            self.connectAttempt = 0
            self.mqttClient.loop_forever()
        except Exception, e:
            print "Error while connecting to broker: {}. Reconnecting...".format(e)
            print e.message
            with self.reconnect_lock:
                if self.reconnect_needed:
                    sleep_time = 1 << self.connectAttempt * self.connectTimeout
                    if self.connectAttempt < self.connectTimeout:
                        self.connectAttempt += 1
                    self.reconnect_timer = threading.Timer(sleep_time, self._connect_to_broker)
                    self.reconnect_timer.start()

    @staticmethod
    def on_disconnect(self, client, userdata, rc):
        MqttClient.connected = False

    @staticmethod
    def on_connect(self, client, userdata, rc):
        if rc:
            print "Connection to broker refused. Error code is {}.".format(rc)
        else:
            MqttClient.connected = True

    @staticmethod
    def on_message(self, client, userdata, msg):
        io_mesage = IoMessage()
        io_mesage.infotype = MQTT_INFO_TYPE
        io_mesage.infoformat = MQTT_INFO_FORMAT
        io_mesage.contextdata = bytearray(json.dumps({
            TOPIC: msg.topic,
            QOS: msg.qos
        }))
        io_mesage.contentdata = bytearray(msg.payload)
        fogClient.post_message_via_socket(io_mesage)

    def publish(self, message):
        self.mqttClient.publish(message[TOPIC], message[PAYLOAD], message[QOS])

    def update_subscriptions(self, subscriptions):
        if not MqttClient.connected:
            with self.subscribe_lock:
                if self.resubscribe_needed:
                    self.subscribe_timer = threading.Timer(2, self.update_subscriptions, args=[subscriptions])
                    self.subscribe_timer.start()
            return
        for s in self.subscriptions:
            self.mqttClient.unsubscribe(str(s[TOPIC]))
        self.subscriptions = subscriptions
        for s in self.subscriptions:
            self.mqttClient.subscribe(s[TOPIC], s[QOS])


fogClient = IoFogClient()
mqttClient = None
config = None
update_config()
fogClient.establish_message_ws_connection(MessageListener())
fogClient.establish_control_ws_connection(ControlListener())
