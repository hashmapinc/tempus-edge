import paho.mqtt.client as paho

def on_connect(self, client, userdata, rc):
    print "Connected"
    if rc:
        print "Connection to broker refused. Error code is {}.".format(rc)

def on_message(clnt, userdata, msg):
    print(msg.topic+" "+str(msg.payload))

mqttc = paho.Client()
mqttc.on_message = on_message
mqttc.on_connect = on_connect
#mqttc.tls_set("mosquitto.org.crt") # http://test.mosquitto.org/ssl/mosquitto.org.crt
#mqttc.username_pw_set('ioFogToken', ' ')
mqttc.connect('192.168.1.183', 1883)
#mqttc.publish('v1/devices/me/telemetry', '{\"lon\":\"54.5224\",\"lat\":\"-49.6229\"}', 1)
#mqttc.subscribe("bbc/#")
mqttc.loop_forever()