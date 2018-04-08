/*
This package is the entrypoint for the tempus-edge-mqtt-client edge application.
*/
package main

import (
	"com/hashmapinc/tempus/edge/mqtt/client"
	"log"
	"os"
)

// configure logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

func main() {
	// update track
	logger.Println("Get to testing!!")
	client.LogTest()
}
