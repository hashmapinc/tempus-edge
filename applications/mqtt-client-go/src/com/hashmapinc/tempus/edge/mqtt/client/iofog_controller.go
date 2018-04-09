package client

import (
	"com/hashmapinc/tempus/edge/message"

	sdk "github.com/iotracks/container-sdk-go"
)

// OnIofogMessage processes incoming iofog messages
func OnIofogMessage(msg *sdk.IoMessage) error {
	payload := msg.ContentData
	var ptclB = payload[0]
	var typB = payload[1]
	ptcl, typ, err := message.DecodeHeader(ptclB, typB)
	logger.Printf("Processing msg with protocol '%s' and type '%s'\n", ptcl, typ)
	return err
}
