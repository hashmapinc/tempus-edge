package client

import (
	"com/hashmapinc/tempus/edge/message"
	"com/hashmapinc/tempus/edge/mqtt"
	pb "com/hashmapinc/tempus/edge/proto"
	"errors"

	"github.com/golang/protobuf/proto"
	sdk "github.com/iofog/container-sdk-go"
)

/*
InitIofogController starts a goroutine for handling new iofog messages.

@param inbox - channel containing IoMessage pointers that need processed
*/
func InitIofogController(inbox <-chan *sdk.IoMessage) {
	// listen for new messages
	go func() {
		logger.Println("Listening for iofog messages...")
		for {
			msg := <-inbox
			logger.Println("heard new iofog message:", msg.ID)
			err := onIofogMessage(msg)
			if err != nil {
				logger.Println("error handling message:", err.Error())
			}
		}
	}()
}

/*
onIofogMessage processes incoming iofog messages

@param msg - IoMessage pointer containing message to process
@returns error - any error that occurs durring processing
*/
func onIofogMessage(msg *sdk.IoMessage) error {
	if msg == nil || msg.ContentData == nil || len(msg.ContentData) < 2 {
		logger.Println("could not handle new iofog message:", msg)
		return errors.New("received non-tempus-edge message")
	}
	payload := msg.ContentData

	// check for raw json first
	if payload[0] == '{' && payload[len(payload)-1] == '}' {
		logger.Println("Received iofog data containing raw json. Publishing now...")
		mqtt.PublishJSONString(payload)
		return nil
	}

	// handle a tempus edge message
	var ptclB = payload[0]
	var typB = payload[1]
	ptcl, typ, err := message.DecodeHeader(ptclB, typB)
	if err != nil {
		logger.Println("Could not handle new iofog message:", msg)
		return err
	}
	logger.Printf("Processing msg with protocol '%s' and type '%s'\n", ptcl, typ)

	// handle message
	switch ptcl {
	case pb.MessageProtocols_CONFIG.String(): // handle new configs
		logger.Println("Received iofog config msg. Updating configs...")
		return UpdateTrackConfig()
	case pb.MessageProtocols_DATA.String(): // handle new data message
		switch typ {
		// handle a JSON tempus edge message
		case pb.DataMessageTypes_JSON.String():
			logger.Println("Received iofog data msg of type JSON! Attempting to send...")
			jsonMsg := &pb.JsonDataMessage{}
			err = proto.Unmarshal(payload[2:], jsonMsg)
			if err != nil {
				return err
			}
			err = mqtt.PublishJSONDataMessage(jsonMsg)
			if err != nil {
				return err
			}
		// handle an MQTT tempus edge message
		case pb.DataMessageTypes_MQTT.String():
			logger.Println("Received iofog data msg of type MQTT! Sending message...")
			mqttMsg := &pb.MqttMessage{}
			err = proto.Unmarshal(payload[2:], mqttMsg)
			if err != nil {
				return err
			}
			err = mqtt.PublishMQTTMessage(mqttMsg)
			if err != nil {
				return err
			}
		// handle an OPC tempus edge message
		case pb.DataMessageTypes_OPC.String():
			logger.Println("Received iofog data msg of type OPC! Attempting to send...")
			opcMessage := &pb.OpcMessage{}
			err = proto.Unmarshal(payload[2:], opcMessage)
			if err != nil {
				return err
			}
			err = mqtt.PublishOPCMessage(opcMessage)
			if err != nil {
				return err
			}
		// handle an unexpected tempus edge message type
		default:
			logger.Println("Cannot handle msg with protocol:", ptcl, "and type:", typ)
			return errors.New("cannot handle msg type")
		}
	default: // cannot handle this ptcl!
		logger.Println("Cannot handle msg with protocol:", ptcl)
		return errors.New("cannot handle msg protocol")
	}
	return nil
}
