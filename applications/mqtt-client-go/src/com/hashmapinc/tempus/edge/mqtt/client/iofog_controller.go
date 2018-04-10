package client

import (
	"com/hashmapinc/tempus/edge/message"
	"errors"

	sdk "github.com/iotracks/container-sdk-go"
)

// OnIofogMessage processes incoming iofog messages
func OnIofogMessage(msg *sdk.IoMessage) error {
	if msg == nil || msg.ContentData == nil || len(msg.ContentData) < 2 {
		return errors.New("received non-tempus-edge message")
	}
	payload := msg.ContentData
	var ptclB = payload[0]
	var typB = payload[1]
	ptcl, typ, err := message.DecodeHeader(ptclB, typB)
	logger.Printf("Processing msg with protocol '%s' and type '%s'\n", ptcl, typ)
	return err
}
