/*
Package mqtt implements reusable logic for mqtt connections for tempus edge applications.

Security and mqtt client connectivity is implemented here, but application logic is left
to the package of specific tempus edge applications.
*/
package mqtt

import (
	"com/hashmapinc/tempus/edge/proto"
	"errors"
	"fmt"
	"log"
	"os"

	paho "github.com/eclipse/paho.mqtt.golang"
	"github.com/golang/protobuf/jsonpb"
)

// create logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// client holds pointer to current active paho Client
var client = paho.NewClient(paho.NewClientOptions())

// MsgHandler points to the function that will handle
var MsgHandler paho.MessageHandler = func(msgClient paho.Client, msg paho.Message) {
	logger.Println("Received mqtt message:", msg.MessageID(), "on topic:", msg.Topic())
}

// concurrency vars
var newConfigChannel = make(chan *proto.MqttConfig, 10)
var newClientChannel = make(chan paho.Client, 10)
var msgOutboxChannel = make(chan *proto.MqttMessage, 100)

// Listen launches goroutines necessary for handling config changes
func Listen() {
	// launch new client listener loop
	go func() {
		client = <-newClientChannel
	}()

	// launch new config listener loop
	go func() {
		for {
			mqttConfig := <-newConfigChannel // block until new config is available
			// launch handler for new config
			go func() {
				newClient, err := createClient(mqttConfig)
				if err != nil {
					logger.Println("error creating client:", err.Error())
				} else {
					logger.Println("successfully created new client!")
					newClientChannel <- newClient
				}
			}()
		}
	}()

	// launch outbox listener loop
	go func() {
		for {
			outgoingMsg := <-msgOutboxChannel // block until new msg is available
			//launch msg sender
			go func() {
				err := publish(outgoingMsg)
				if err != nil {
					logger.Println("error publishing message:", err.Error())
				}
			}()
		}
	}()
}

// publish sends msg to the current broker
func publish(msg *proto.MqttMessage) error {
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

// PublishMQTTMessage pushes msg to the outbox channel
func PublishMQTTMessage(msg *proto.MqttMessage) (err error) {
	msgOutboxChannel <- msg
	return
}

// PublishJSONDataMessage converts msg into an MqttMessage and pushes it to the outbox channel
func PublishJSONDataMessage(msg *proto.JsonDataMessage) (err error) {
	jsonString := msg.GetJson()
	mqttMsg := &proto.MqttMessage{}
	err = jsonpb.UnmarshalString(jsonString, mqttMsg)
	if err == nil {
		// push mqttMsg to outbox
		msgOutboxChannel <- mqttMsg
	}
	return
}

// PublishJSONString converts msg into an MqttMessage and pushes it to the outbox channel
func PublishJSONString(json []byte) {
	mqttMsg := &proto.MqttMessage{
		Qos:     2,
		Topic:   "v1/devices/me/telemetry",
		Payload: json,
	}
	msgOutboxChannel <- mqttMsg
}

// PublishOPCMessage converts msg into an MqttMessage and Publishes it
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
	msgOutboxChannel <- mqttMsg
	return nil
}

// createClient creates a paho client from the given mqttConfig
func parseOptsFromConf(mqttConfig *proto.MqttConfig) (opts *paho.ClientOptions, err error) {
	if mqttConfig == nil {
		err = errors.New("cannot parse nil mqttConfig into client options struct")
		return
	}
	logger.Printf("Parsing mqttConf:\n%v\n into client options struct\n", mqttConfig)

	opts = paho.NewClientOptions().
		SetUsername(mqttConfig.GetUser().GetUsername()).
		SetPassword(mqttConfig.GetUser().GetPassword()).
		AddBroker(fmt.Sprintf("tcp://%s:%d", mqttConfig.GetBroker().GetHost(), mqttConfig.GetBroker().GetPort())).
		SetClientID("tempus::edge::mqtt")

	logger.Println("parsed opts:", opts)
	return
}

// createClient creates a paho client from the given mqttConfig
func createClient(mqttConfig *proto.MqttConfig) (newClient paho.Client, err error) {
	// get paho client options from mqtt config
	opts, err := parseOptsFromConf(mqttConfig)
	if err != nil {
		return
	}

	// attach msg handler if it exists to opts
	if MsgHandler != nil {
		opts = opts.SetDefaultPublishHandler(MsgHandler)
	}

	newClient = paho.NewClient(opts)

	return
}

// UpdateClient uses trackConfig to update the mqtt client
func UpdateClient(trackConfig *proto.TrackConfig) (err error) {
	logger.Println("Attempting to update mqtt client...")

	if trackConfig == nil {
		err = errors.New("cannot update mqtt client from nil track config")
		logger.Println("Nil track config given to mqtt client updater. Ignoring and moving on...")
		return
	}

	newMqttConf := trackConfig.GetMqttConfig()

	if newMqttConf == nil || newMqttConf.Broker == nil {
		err = errors.New("invalid mqtt conf from track config")
		logger.Println("Nil track config given to mqtt client updater. Ignoring and moving on...")
		return
	}

	// pass new configs to new config channel
	newConfigChannel <- newMqttConf
	return
}
