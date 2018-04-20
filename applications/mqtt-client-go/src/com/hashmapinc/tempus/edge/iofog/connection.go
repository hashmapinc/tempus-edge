/*
Package iofog contains reusable logic for establishing an iofog connection for tempus edge applications.
*/
package iofog

import (
	"log"
	"os"

	sdk "github.com/iofog/container-sdk-go"
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
var client = &sdk.IoFogClient{}

// DataChannel holds all incoming iofog messages
var DataChannel <-chan *sdk.IoMessage

// ReceiptChannel holds all incoming iofog messages
var ReceiptChannel <-chan *sdk.PostMessageResponse

// StartConnection connects to the ioFog agent
func StartConnection() (err error) {
	client, err = sdk.NewDefaultIoFogClient()
	if err != nil {
		logger.Println("received erorr when starting iofog connection:", err.Error())
	} else {
		DataChannel, ReceiptChannel = client.EstablishMessageWsConnection(100, 100)
	}
	return
}

// SendWSMessage sens a ws message to iofog with payload as the IoMessage content
func SendWSMessage(payload []byte) (err error) {
	// create msg
	msg := &sdk.IoMessage{ContentData: payload}

	// send msg
	err = client.SendMessageViaSocket(msg)
	return
}
