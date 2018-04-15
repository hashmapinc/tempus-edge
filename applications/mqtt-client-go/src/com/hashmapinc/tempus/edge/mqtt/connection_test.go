package mqtt

import (
	pb "com/hashmapinc/tempus/edge/proto"
	"testing"

	"github.com/golang/protobuf/proto"
)

var mqttConfigTable = []struct {
	username string
	password string
	host     string
	port     int32
}{
	{"asdf", "fdas", "my.mqtt.host", 1883},
}

func TestUpdateClient(t *testing.T) {
	for _, conf := range mqttConfigTable {
		mqttUser := &pb.MqttConfig_User{Username: conf.username, Password: conf.password}
		mqttBroker := &pb.MqttConfig_Broker{Host: conf.host, Port: conf.port}
		mqttConfig := &pb.MqttConfig{User: mqttUser, Broker: mqttBroker}
		tc := &pb.TrackConfig{MqttConfig: mqttConfig}
		t.Logf("updating client with tc: %v\n", tc)
		UpdateClient(tc)

		found := <-newConfigChannel
		expected := mqttConfig

		if !proto.Equal(found, expected) {
			t.Errorf("found:\n%v\nexpected:\n%v\n", found, expected)
		}
	}
}
