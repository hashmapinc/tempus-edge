import paho.mqtt.client as paho
from index import MqttClient
from util import *

testConfig =  {
    "subscriptions": [{
        "topic": "v1/devices/me/telemetry",
        "qos": 2
    }],
    "publishers": [{
        "topic": "v1/devices/me/telemetry",
        "qos": 2
    }],
    "broker": {
        "host": "192.168.1.8",
        "port": 1883
    },
    "user": {
        "username": "gxrIQp93GPsf2Qo0cDT6",
        "password": " "
    }
}

testClient = MqttClient(
    testConfig[BROKER], 
    testConfig.get(USER), 
    testConfig.get(CA_CERT)
)


#==============================================================================
# define callbacks
#==============================================================================
def on_connect(self, client, userdata, rc):
    print "Connected"
    if rc:
        print "Connection to broker refused. Error code is {}.".format(rc)

def on_message(clnt, userdata, msg):
    print(msg.topic+" "+str(msg.payload))

#==============================================================================


#==============================================================================
# test connection
#==============================================================================
"""
mqttc = paho.Client()
mqttc.on_message = on_message
mqttc.on_connect = on_connect
#mqttc.tls_set("mosquitto.org.crt") # http://test.mosquitto.org/ssl/mosquitto.org.crt
mqttc.username_pw_set('gxrIQp93GPsf2Qo0cDT6', ' ')
#mqttc.connect('192.168.1.8', 1883)
mqttc.connect('172.18.0.8', 1883)
mqttc.publish('v1/devices/me/telemetry', '{\"lon\":\"54.5224\",\"lat\":\"-49.6229\"}', 1)
#mqttc.subscribe("bbc/#")
mqttc.loop_forever()
"""
#==============================================================================