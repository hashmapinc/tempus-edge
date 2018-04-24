package mqtt

import (
	"com/hashmapinc/tempus/edge/proto"
	"errors"
	"fmt"

	"github.com/golang/protobuf/jsonpb"
)

/*
publish sends msg to the current broker

@param msg - pointer to the MqttMessage to send to the broker
*/
func publish(msg *proto.MqttMessage) error {
	// add some thread safety
	clientLock.Lock()
	defer clientLock.Unlock()

	logger.Printf("publishing mqtt message to broker: %v\n", msg)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
		return token.Error()
	}

	qos := byte(proto.MqttMessage_QoS_value[msg.GetQos().String()])
	if token := client.Publish(msg.GetTopic(), qos, false, msg.GetPayload()); token.Wait() && token.Error() != nil {
		return token.Error()
	}
	return nil
}

/*
PublishMQTTMessage pushes msg to the outbox channel for processing

@param msg - pointer to the MqttMessage to send to the broker
*/
func PublishMQTTMessage(msg *proto.MqttMessage) (err error) {
	Outbox <- msg
	return
}

/*
PublishJSONDataMessage converts msg into an MqttMessage and pushes it to the outbox channel

@param msg - pointer to the JsonDataMessage to parse and send to the broker
*/
func PublishJSONDataMessage(msg *proto.JsonDataMessage) error {
	// try to deserialize the json into an MqttMessage proto struct
	jsonString := msg.GetJson()
	mqttMsg := &proto.MqttMessage{}
	err := jsonpb.UnmarshalString(jsonString, mqttMsg)

	// if deserialization worked, push the msg to the outbox
	if err == nil {
		// push mqttMsg to outbox
		Outbox <- mqttMsg
	} else {
		// deserialization failed. Just push the json as a normal json string
		PublishJSONString([]byte(jsonString))
	}

	return nil
}

/*
PublishJSONString converts msg into an MqttMessage and pushes it to the outbox channel

@param json - byte slice to parse and send to the broker
*/
func PublishJSONString(json []byte) {
	mqttMsg := &proto.MqttMessage{
		Qos:     2,
		Topic:   "v1/devices/me/telemetry",
		Payload: json,
	}
	Outbox <- mqttMsg
}

/*
PublishOPCMessage converts msg into an MqttMessage and pushes it to the outbox channel

@param msg - pointer to the OpcMessage to parse and send to the broker
*/
func PublishOPCMessage(msg *proto.OpcMessage) error {
	// get payload
	var payload []byte
	switch msg.Value.(type) {
	case *proto.OpcMessage_ValueBoolean:
		payload = []byte(fmt.Sprintf("%t", msg.GetValueBoolean()))
	case *proto.OpcMessage_ValueDouble:
		payload = []byte(fmt.Sprintf("%f", msg.GetValueDouble()))
	case *proto.OpcMessage_ValueFloat:
		payload = []byte(fmt.Sprintf("%f", msg.GetValueFloat()))
	case *proto.OpcMessage_ValueInt32:
		payload = []byte(fmt.Sprintf("%d", msg.GetValueInt32()))
	case *proto.OpcMessage_ValueInt64:
		payload = []byte(fmt.Sprintf("%d", msg.GetValueInt64()))
	case *proto.OpcMessage_ValueString:
		payload = []byte(fmt.Sprintf("%s", msg.GetValueString()))
	case nil:
		// The field is not set. do nothing
	default:
		logger.Println("Could not process opc message value:", msg)
		return errors.New("could not process opc message value")
	}

	// create mqttMsg
	mqttMsg := &proto.MqttMessage{
		Qos:     proto.MqttMessage_TWO, // 2 is default for all incoming opc messages
		Topic:   msg.GetDeviceName(),
		Payload: payload,
	}

	// push mqttMsg to outbox
	Outbox <- mqttMsg
	return nil
}
