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
)

// create logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// client holds pointer to current active paho client
var client = paho.NewClient(paho.NewClientOptions())

// MsgHandler points to function that handles new mqtt messages
var MsgHandler paho.MessageHandler = func(msgClient paho.Client, msg paho.Message) {
	logger.Println("Received mqtt message:", msg.MessageID, "on topic:", msg.Topic())
}

// concurrency vars
var newConfigChannel = make(chan *proto.MqttConfig, 0)
var newClientChannel = make(chan paho.Client, 0)
var msgOutboxChannel = make(chan *paho.Message, 100)

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
