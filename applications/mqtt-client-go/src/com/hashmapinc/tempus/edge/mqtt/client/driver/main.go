/*
This package is the entrypoint for the tempus-edge-mqtt-client edge application.
*/
package main

import (
	"com/hashmapinc/tempus/edge/iofog"
	"com/hashmapinc/tempus/edge/mqtt"
	"com/hashmapinc/tempus/edge/mqtt/client"
	"log"
	"os"
	"time"
)

// configure logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

func main() {
	logger.Println("Starting edge application...")

	// connect to local iofog agent
	retriesLeft := 10
	retryDelay := 10
	logger.Println("starting iofog connection")
	for err := iofog.StartConnection(); err != nil; {
		retriesLeft--
		if retriesLeft < 1 {
			logger.Panicln("Could not connect to ioFog agent. Terminating edge application...")
		}
		logger.Println("Could not connect to ioFog Agent. Received error: ", err.Error())
		logger.Printf("Will retry ioFog connection in %d seconds. %d retries remaining...\n", retryDelay, retriesLeft)
		time.Sleep(time.Duration(retryDelay) * time.Second)
		err = iofog.StartConnection()
	}
	// connect msg handler to ws data channel
	logger.Println("Successfully connected to iofog!")
	iofog.ConnectListener(client.OnIofogMessage, iofog.Client)

	// connect mqtt msg handler
	mqtt.MsgHandler = client.OnMqttMessage

	// start mqtt client
	logger.Println("starting mqtt connection")
	mqtt.Listen()

	// perform initial updates
	client.UpdateTrackConfig()
	mqtt.UpdateClient(client.LocalTrackConfig)

	// loop forever
	logger.Println("listening for tasks...")
	for {
	}
}
