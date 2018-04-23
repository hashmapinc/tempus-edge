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

	// init the client's controllers
	client.InitIofogController(iofog.Inbox)
	client.InitMqttController(mqtt.Inbox)

	// connect to local iofog agent
	retriesLeft := 10
	retryDelay := 10
	logger.Println("starting iofog connection")
	for err := iofog.Init(20, 20); err != nil; {
		retriesLeft--
		if retriesLeft < 1 {
			logger.Panicln("Could not connect to ioFog agent. Terminating edge application...")
		}
		logger.Println("Could not connect to ioFog Agent. Received error: ", err.Error())
		logger.Printf("Will retry ioFog connection in %d seconds. %d retries remaining...\n", retryDelay, retriesLeft)
		time.Sleep(time.Duration(retryDelay) * time.Second)
	}
	logger.Println("Successfully connected to iofog!")

	// start mqtt client
	logger.Println("starting mqtt connection")
	mqtt.Init(20, 20)

	// perform initial updates
	client.UpdateTrackConfig() // this updates the track config and this edge application

	// loop forever
	logger.Println("listening for tasks...")
	for {
	}
}
