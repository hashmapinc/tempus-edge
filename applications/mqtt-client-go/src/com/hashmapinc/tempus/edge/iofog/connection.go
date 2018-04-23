/*
Package iofog contains reusable logic for establishing an iofog connection for tempus edge applications.

Usage:
- Init this package before using. This should go in the driver / main method of your edge application.
- edge applications should monitor the Inbox channel for incoming iofog messages.
- to publish a message to ifog, just push the content data to the Outbox channel.
*/
package iofog

import (
	"log"
	"os"

	sdk "github.com/iofog/container-sdk-go"
)

var (
	logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)
	// client holds the connection to the local ioFog agent
	client = &sdk.IoFogClient{}
	// Inbox holds all incoming iofog messages
	Inbox <-chan *sdk.IoMessage
	// Outbox holds all outgoing iofog messages
	Outbox chan []byte
	// receiptChannel holds all iofog message receipts
	receiptChannel <-chan *sdk.PostMessageResponse
	// confChannel holds all iofog configuration signals
	confChannel <-chan byte
)

/*
Init does the following:
- creates the iofog client and message bus channels
- handles draining of the receiptChannel in a go routine
- handles draining of the Outbox in a go routine

@param inboxSize - size of the inbox channel buffer
@param outboxSize - size of the outbox channel buffer
*/
func Init(inboxSize, outboxSize int) error {
	// create client
	var err error
	client, err = sdk.NewDefaultIoFogClient()
	if err != nil {
		logger.Println("received error when starting iofog connection:", err.Error())
		return err
	}

	// create channels
	confChannel = client.EstablishControlWsConnection(5)
	Inbox, receiptChannel = client.EstablishMessageWsConnection(inboxSize, inboxSize)
	Outbox = make(chan []byte, outboxSize)

	// start confChannel drainer
	go func() {
		for {
			_ = <-confChannel
		}
	}()

	// start receiptChannel drainer
	go func() {
		for {
			_ = <-receiptChannel
		}
	}()

	// start Outbox drainer
	go func() {
		for {
			payload := <-Outbox
			err = publish(payload)
			if err != nil {
				logger.Printf("Error when publishing payload %v -> %s\n", payload, err.Error())
			} else {
				logger.Println("Successfully published message!")
			}
		}
	}()
	return nil
}

/*
publish sends an IoMessage to teh iofog message bus with ContentData = payload

@param payload - bytes to use for the outgoing IoMessage's ContentData
*/
func publish(payload []byte) (err error) {
	// create msg
	msg := &sdk.IoMessage{ContentData: payload}

	// send msg
	err = client.SendMessageViaSocket(msg)
	return
}
