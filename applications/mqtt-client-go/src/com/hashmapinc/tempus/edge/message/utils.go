package message

import (
	"com/hashmapinc/tempus/edge/proto"
	"errors"
)

// DecodeHeader returns string values for the message protocol and message type
func DecodeHeader(mProtocol, mType byte) (ptclName string, typeName string, err error) {
	// get protocol name
	ptclName = proto.MessageProtocols_name[int32(mProtocol)]
	if ptclName == "" {
		err = errors.New("could not decode message protocol")
		return
	}

	// get typeName
	switch ptclName {
	case proto.MessageProtocols_CONFIG.String():
		typeName = proto.ConfigMessageTypes_name[int32(mType)]
	case proto.MessageProtocols_DATA.String():
		typeName = proto.DataMessageTypes_name[int32(mType)]
	}
	if typeName == "" {
		err = errors.New("could not decode message type")
		return
	}

	// return decoded values
	return
}
