/*
Package iofog contains reusable logic for establishing an iofog connection for tempus edge applications.
*/
package iofog

import (
	"log"
	"os"
	"runtime"

	sdk "github.com/iotracks/container-sdk-go"
)

// configure logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// Client holds the connection to the local ioFog agent
var Client = &sdk.IoFogClient{}

// Connect connects to the ioFog agent
func Connect() (err error) {
	Client, err = sdk.NewDefaultIoFogClient()
	return
}

// Listen connects the listener to incoming ioFog data messages
func Listen(lstnr Listener) {
	// get ws connection IoMessage data channel with buffer size = numCPU cores
	dataChannel, _ := Client.EstablishMessageWsConnection(runtime.NumCPU(), 0)

	// listen forever
	for {
		msg := <-dataChannel // get next message from the data channel
		logger.Println("Received iofog message!")
		go lstnr(msg) // launch go routine to handle the new message
	}
}
