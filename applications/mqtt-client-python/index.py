import threading

from iofog_container_sdk.client import IoFogClient, IoFogException
from iofog_container_sdk.iomessage import IoMessage
from iofog_container_sdk.listener import IoFogMessageWsListener
from google.protobuf import json_format
from paho.mqtt import client
from paho.mqtt.client import MQTT_ERR_SUCCESS

from tempus.edge.proto import MessageProtocols_pb2 as protocols
from tempus.edge.proto import MessageTypes_pb2 as messageTypes
from tempus.edge.proto import JsonDataMessage_pb2 as te_jsonMsg
from tempus.edge.proto import OpcMessage_pb2 as te_opcMsg
from tempus.edge.proto import MqttMessage_pb2 as te_mqttMsg

import config

clientLock = threading.Lock()
fogClient = IoFogClient()
mqttClient = None
config = None
update()

def update():
    config.updateTrackConfig()
    update_mqtt_client()

def update_mqtt_client():
    global mqttClient
    broker = config.getBroker()
    user = config.getUser()
    cert = config.getCert()
    if not mqttClient:
        mqttClient = MqttClient(broker, user, cert)
        mqttClient.connect()
    elif mqttClient.broker != broker or mqttClient.ca_cert != cert:
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
            mqttClient = MqttClient(broker, user, cert)
        mqttClient.connect()
    mqttClient.update_subscriptions(config.getSubscriptions())

# converts an opcMsg into an mqtt sendable message.
def parseOpcMessage(opcMsg):
    try:
        mqttMsg = {}
        mqttMsg['topic'] = opcMsg.device_name
        mqttMsg['qos'] = 2
        oneof = opcMsg.WhichOneof('value')
        if oneof:
            vals = {
                'value_boolean': opcMsg.value_boolean,
                'value_double': opcMsg.value_double,
                'value_float': opcMsg.value_float,
                'value_int32': opcMsg.value_int32,
                'value_int64': opcMsg.value_int64,
                'value_string': opcMsg.value_string,
            }
            mqttMsg['payload'] = vals[oneof]
        return opcMsg
    except:
        return {}

# parses a json string into an outgoing mqtt message
def parseJson(jsonString):
    # try parsing into mqtt
    try:
        parsed_mqtt = te_mqttMsg.MqttMessage()
        json_format.Parse(jsonString, parsed_mqtt, ignore_unknown_fields=True)
        return parseMqttMessage(parsed_mqtt)
    except:
        pass
    
    # try parsing into opc
    try:
        parsed_opc = te_opcMsg.OpcMessage()
        json_format.Parse(jsonString, parsed_opc, ignore_unknown_fields=True)
        return parseOpcMessage(parsed_opc)
    except:
        pass
    return {}

# parses a json string into an outgoing mqtt message
def parseMqttMessage(mqttMessage):
    # try parsing into mqtt
    try:
        return {
            'qos': mqttMessage.qos,
            'topic': mqttMessage.topic,
            'payload': mqttMessage.payload
        }
    except:
        return {}


# parses an iofog json data message into an mqtt message
def parseJsonDataMessage(jsonMessage):
    return parseJson(jsonMessage.json)

class MessageListener(IoFogMessageWsListener):
    def on_message(self, msg):
        # create empty mqtt message
        mqttMsg = {}

        try:
            # process iofog message
            ptcl = msg.contentdata[0]

            # handle true json
            if ptcl == '{':
                mqttMsg = parseJson(msg.contentdata.decode('utf-8'))

            # handle config messages
            elif ptcl == protocols.CONFIG:
                update() # update configs
            
            # handle data messages
            elif ptcl == protocols.DATA:
                typ = msg.contentdata[1] # check what type of data message this is
                # handle opc message
                if typ == messageTypes.OPC:
                    opcMsg = te_opcMsg.OpcMessage().ParseFromString(msg.contentdata[2:])
                    mqttMsg = parseOpcMessage(opcMsg)
                # handle mqtt message
                elif typ == messageTypes.MQTT:
                    mqttMessage = te_mqttMsg.MqttMessage().ParseFromString(msg.contentdata[2:])
                    mqttMsg = parseMqttMessage(mqttMessage)
                # handle json message
                elif typ == messageTypes.JSON:
                    jsonMessage = te_jsonMsg.JsonDataMessage().ParseFromString(msg.contentdata[2:])
                    mqttMsg = parseJsonDataMessage(jsonMessage)
                # handle all other date message types
                else:
                    print("could not handle iofog message: " + str(msg))

            # handle all other ptcls
            else:
                print("could not handle iofog message: " + str(msg))

        except:
            print("could not handle iofog message: " + str(msg))
        
        # send msg if it was created
        if mqttMsg:
            mqttClient.publish(mqttMsg)


class MqttClient:
    connected = False

    def __init__(self, broker, user, ca_cert):
        self.broker = broker
        self.ca_cert = ca_cert
        self.subscriptions = []
        self.mqttClient = client.Client(client_id="tempus-edge-mqtt-client")
        if user:
            self.mqttClient.username_pw_set(user['username'], user.get('password'))
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
            self.mqttClient.connect(self.broker['host'], port=self.broker['port'])
            self.connectAttempt = 0
            self.mqttClient.loop_forever()
        except Exception as e:
            print("Error while connecting to broker: " + str(e) + ". Reconnecting...")
            print(e.message)
            with self.reconnect_lock:
                if self.reconnect_needed:
                    sleep_time = 1 << self.connectAttempt * self.connectTimeout
                    if self.connectAttempt < self.connectTimeout:
                        self.connectAttempt += 1
                    self.reconnect_timer = threading.Timer(sleep_time, self._connect_to_broker)
                    self.reconnect_timer.start()

    @staticmethod
    def on_disconnect(client, userdata, rc):
        MqttClient.connected = False
        print("MQTT Client disconnected.")

        # if this disconnect unintentionall, begin reconnecting
        # rc = MQTT_ERR_SUCCESS means disconnect() was called
        if rc is not MQTT_ERR_SUCCESS:
            print("Reconnecting...")
            # attempt to reconnnect indefinitely
            reconnStatus = client.reconnect()
            while reconnStatus is not MQTT_ERR_SUCCESS:
                print("Reconnection failed.")
                print("Reconnecting...")
                reconnStatus = client.reconnect()

            # to get here, reconnection is successful
            MqttClient.connected = True

    @staticmethod
    def on_connect(client, userdata, flags, rc):
        if rc:
            print("Connection to broker refused. Error code is " + str(rc))
        else:
            MqttClient.connected = True

    @staticmethod
    def on_message(client, userdata, msg):
        io_mesage = IoMessage()
        io_mesage.contentdata = bytearray(msg.payload)
        fogClient.post_message_via_socket(io_mesage)

    def publish(self, message):
        self.mqttClient.publish(message['topic'], message['payload'], message['qos'])

    def update_subscriptions(self, subscriptions):
        if not MqttClient.connected:
            with self.subscribe_lock:
                if self.resubscribe_needed:
                    self.subscribe_timer = threading.Timer(2, self.update_subscriptions, args=[subscriptions])
                    self.subscribe_timer.start()
            return
        for s in self.subscriptions:
            self.mqttClient.unsubscribe(str(s['topic']))
        self.subscriptions = subscriptions
        for s in self.subscriptions:
            self.mqttClient.subscribe(s['topic'], s['qos'])

fogClient.establish_message_ws_connection(MessageListener())
