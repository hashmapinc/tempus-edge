/*
Package mqtt implements reusable logic for mqtt connections for tempus edge applications.

Security and mqtt client connectivity is implemented here, but application logic is left
to the package of specific tempus edge applications.
*/
package mqtt

import (
	"com/hashmapinc/tempus/edge/proto"
	"log"
	"os"
	"sync"

	paho "github.com/eclipse/paho.mqtt.golang"
)

// create logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// client holds pointer to current active paho Client
var client = paho.NewClient(paho.NewClientOptions())
var clientLock = &sync.Mutex{}

// ConfigInbox holds updated mqtt configs from the shared volume
var ConfigInbox = make(chan *proto.MqttConfig, 10)

// Inbox holds incoming mqtt messages from the broker
var Inbox chan *proto.MqttMessage

// Outbox holds outgoing mqtt messages to the broker
var Outbox chan *proto.MqttMessage

/*
Init does the following:
- creates Inbox and Outbox channels
- starts a go routine for handling new mqtt configs
- starts a go routine for draining the Outbox

@param inboxSize 	- int size of the Inbox channel
@param outboxSize - int size of the Outbox channel
*/
func Init(inboxSize, outboxSize int) {
	// create channels
	Inbox = make(chan *proto.MqttMessage, inboxSize)
	Outbox = make(chan *proto.MqttMessage, outboxSize)

	// launch new config listener loop
	go func() {
		for {
			mqttConfig := <-ConfigInbox // block until new config is available
			// launch handler for new config
			go func() {
				newClient, err := createClient(mqttConfig)
				if err != nil {
					logger.Println("error creating client:", err.Error())
				} else {
					logger.Println("successfully created new client!")

					// update client, give old client 1000ms to shut down
					clientLock.Lock()
					client.Disconnect(1000.0)
					client = newClient
					clientLock.Unlock()
				}
			}()
		}
	}()

	// launch outbox listener loop
	go func() {
		for {
			outgoingMsg := <-Outbox // block until new msg is available
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
