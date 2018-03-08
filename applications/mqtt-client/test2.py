import json
from util import *
from iofog_container_sdk.client import IoFogClient
from iofog_container_sdk.exception import IoFogException
from iofog_container_sdk.iomessage import IoMessage
from iofog_container_sdk.listener import *

client = IoFogClient()

class ControlListener(IoFogControlWsListener):
    def on_control_signal(self):
        print("control!")


class MessageListener(IoFogMessageWsListener):
    def on_receipt(self, message_id, timestamp):
        print("receipt!")
        print(message_id)
    
    def on_message(self, io_msg):
        print("message!")
        print(io_msg)
        print(io_msg.to_json())


client.establish_message_ws_connection(MessageListener())
client.establish_control_ws_connection(ControlListener())


topic='v1/devices/me/telemetry'
qos=2

msg = IoMessage()
msg.infotype = MQTT_INFO_TYPE
msg.infoformat = MQTT_INFO_FORMAT
msg.contentdata = bytearray(json.dumps({"values":{"Attn":"-0.5921182196003841","crpm":"3.2362590082892218","stor":"3.2362590082892218","density":"-0.5921182196003841","porosity":"3.2362590082892218","temperature":"-0.5921182196003841","torque":"-0.41241862882195507","pressure":"3.2362590082892218","aprs":"3.2362590082892218","rpm":"-0.5921182196003841","gamma":"-0.41241862882195507","resistivity":"-0.41241862882195507"},"ts":"1518031993177"}))
msg.contextdata = bytearray(json.dumps({
    TOPIC: topic,
    QOS: qos
}))

client.post_message_via_socket(msg)