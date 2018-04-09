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
var client = &sdk.IoFogClient{}

// StartConnection connects to the ioFog agent
func StartConnection() (err error) {
	client, err = sdk.NewDefaultIoFogClient()
	return
}

// ConnectListener connects the listener to incoming ioFog data messages
func ConnectListener(lstnr Listener, iofogClient *sdk.IoFogClient) {
	// check iofogClient is real. If not, use default
	if nil == iofogClient {
		iofogClient = client
	}
	// get ws connection IoMessage data channel with buffer size = numCPU cores
	dataChannel, _ := iofogClient.EstablishMessageWsConnection(runtime.NumCPU(), 0)

	// listen forever
	for {
		msg := <-dataChannel // get next message from the data channel
		logger.Println("Received iofog message!")
		go lstnr(msg) // launch go routine to handle the new message
	}
}
