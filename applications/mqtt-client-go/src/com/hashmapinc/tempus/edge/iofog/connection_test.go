package iofog

import (
	"bytes"
	"com/hashmapinc/tempus/edge/proto"
	"testing"

	sdk "github.com/iofog/container-sdk-go"
)

//=============================================================================
// define test client
//=============================================================================
type testClient struct {
	dataChannel  <-chan *sdk.IoMessage
	recptChannel <-chan *sdk.PostMessageResponse
	lastMessage  *sdk.IoMessage
}

func (tc *testClient) EstablishMessageWsConnection(dataBufSize, recptBufSize int) (<-chan *sdk.IoMessage, <-chan *sdk.PostMessageResponse) {
	return tc.dataChannel, tc.recptChannel
}

func (tc *testClient) SendMessageViaSocket(msg *sdk.IoMessage) error {
	tc.lastMessage = msg
	return nil
}

var messageTable = []struct {
	payload []byte
}{
	{[]byte{byte(proto.MessageProtocols_value[proto.MessageProtocols_CONFIG.String()]),
		byte(proto.ConfigMessageTypes_value[proto.ConfigMessageTypes_UPDATE_ALERT.String()])}},
	{[]byte{byte(proto.MessageProtocols_value[proto.MessageProtocols_DATA.String()]),
		byte(proto.ConfigMessageTypes_value[proto.DataMessageTypes_MQTT.String()])}},
}

// Checks the type of the iofog client
func TestSendWSMessage(t *testing.T) {
	// create client
	clientStub := &testClient{}

	for _, message := range messageTable {
		msg := &sdk.IoMessage{ContentData: message.payload}
		SendWSMessage(message.payload, clientStub)

		t.Logf("sent msg: %v", msg)
		t.Logf("got msg: %v", clientStub.lastMessage)

		if clientStub.lastMessage == nil || !bytes.Equal(msg.ContentData, clientStub.lastMessage.ContentData) {
			t.Errorf("clientStub.lastMessage is null (shouldn't be) OR listener failed to update the client's last message")
		}
	}
}
