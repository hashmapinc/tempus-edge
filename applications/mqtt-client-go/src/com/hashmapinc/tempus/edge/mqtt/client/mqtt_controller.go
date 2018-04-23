package client

import (
	"com/hashmapinc/tempus/edge/iofog"
	pb "com/hashmapinc/tempus/edge/proto"

	paho "github.com/eclipse/paho.mqtt.golang"
	"github.com/golang/protobuf/proto"
)

// OnMqttMessage handles new incoming mqtt messages
func OnMqttMessage(mqttClient paho.Client, msg paho.Message) {
	logger.Printf("received mqtt msg: %v \n", msg)

	// get qos
	var qos pb.MqttMessage_QoS
	switch msg.Qos() {
	case 0:
		qos = pb.MqttMessage_ZERO
	case 1:
		qos = pb.MqttMessage_ONE
	case 2:
		qos = pb.MqttMessage_TWO
	}

	// create tempus edge MQTT msg
	mqttMsg := &pb.MqttMessage{
		Topic:   msg.Topic(),
		Qos:     qos,
		Payload: msg.Payload(),
	}

	// get proto byte array
	iofogPayload, err := proto.Marshal(mqttMsg)
	if err != nil {
		logger.Println("recieved error when marshalling mqtt message: ", err.Error())
		return
	}

	// append tempus edge message protocol and type
	header := []byte{byte(pb.MessageProtocols_DATA), byte(pb.DataMessageTypes_MQTT)}
	iofogPayload = append(header, iofogPayload...)

	// send payload
	iofog.Outbox <- iofogPayload
}
