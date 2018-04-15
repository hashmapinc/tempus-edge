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

/*
DataClient interface is an ioFog client that only connects listeners to a data websocket channel
and sends IoMessages via socket. It is a subset of the full sdk.IoFogClient.

This is useful for both unit testing and for easily modifying iofog clients in the future.
*/
type DataClient interface {
	EstablishMessageWsConnection(dataBufSize, recptBufSize int) (<-chan *sdk.IoMessage, <-chan *sdk.PostMessageResponse)
	SendMessageViaSocket(msg *sdk.IoMessage) error
}

var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// Client holds the connection to the local ioFog agent
var Client = &sdk.IoFogClient{}

// StartConnection connects to the ioFog agent
func StartConnection() (err error) {
	Client, err = sdk.NewDefaultIoFogClient()
	return
}

// ConnectListener connects the listener to incoming ioFog data messages
func ConnectListener(lstnr Listener, client DataClient) {
	// get ws connection IoMessage data channel with buffer size = numCPU cores
	dataChannel, _ := client.EstablishMessageWsConnection(runtime.NumCPU(), 0)

	// listen forever
	go func() {
		for {
			msg := <-dataChannel // get next message from the data channel
			logger.Println("Received iofog message!")
			lstnr(msg) // launch go routine to handle the new message
		}
	}()
}

// SendWSMessage sens a ws message to iofog with payload as the IoMessage content
func SendWSMessage(payload []byte, client DataClient) (err error) {
	// create msg
	msg := &sdk.IoMessage{ContentData: payload}

	// send msg
	err = client.SendMessageViaSocket(msg)
	return
}
