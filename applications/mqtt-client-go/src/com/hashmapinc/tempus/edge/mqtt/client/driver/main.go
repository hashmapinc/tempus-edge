/*
This package is the entrypoint for the tempus-edge-mqtt-client edge application.
*/
package main

import (
	"com/hashmapinc/tempus/edge/iofog"
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
	logger.Println("Starting edge application...")
	retriesLeft := 10
	retryDelay := 10 * time.Second
	for err := iofog.Connect(); err != nil; {
		retriesLeft--
		if retriesLeft < 1 {
			logger.Panicln("Could not connect to ioFog agent. Terminating edge application...")
		}
		logger.Println("Could not connect to ioFog Agent. Received error: ", err.Error())
		logger.Printf("Will retry ioFog connection in %d seconds. %d retries remaining...\n", retryDelay, retriesLeft)
		time.Sleep(retryDelay * time.Second)
		err = iofog.Connect()
	}

	// connect msg handler to ws data channel
	iofog.Listen(client.OnIofogMessage)
}
