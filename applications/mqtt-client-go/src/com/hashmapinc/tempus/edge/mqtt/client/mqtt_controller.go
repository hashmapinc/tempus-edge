package client

import (
	"com/hashmapinc/tempus/edge/iofog"
	pb "com/hashmapinc/tempus/edge/proto"

	"github.com/golang/protobuf/proto"
)

/*
InitMqttController starts a goroutine for handling new mqtt messages.

@param inbox - channel containing MqttMessage pointers that need processing
*/
func InitMqttController(inbox chan *pb.MqttMessage) {
	// listen for new messages
	logger.Println("Listening for mqtt messages...")
	go func() {
		for {
			msg := <-inbox
			logger.Println("dispatching new mqtt message to processing...")
			onMqttMessage(msg)
		}
	}()
}

/*
onMqttMessage handles new incoming mqtt messages

@param msg - pointer to the MqttMessage to process
*/
func onMqttMessage(msg *pb.MqttMessage) {
	logger.Println("processing new mqtt message...")

	// get proto byte array
	iofogPayload, err := proto.Marshal(msg)
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
