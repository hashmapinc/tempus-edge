package mqtt

import (
	"com/hashmapinc/tempus/edge/proto"
	"fmt"
	"testing"
	"time"
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
		mqttUser := &proto.MqttConfig_User{Username: conf.username, Password: conf.password}
		mqttBroker := &proto.MqttConfig_Broker{Host: conf.host, Port: conf.port}
		mqttConfig := &proto.MqttConfig{User: mqttUser, Broker: mqttBroker}
		tc := &proto.TrackConfig{MqttConfig: mqttConfig}
		UpdateClient(tc)

		// wait a second to allow goroutine processing
		time.Sleep(1 * time.Second)

		opts := client.OptionsReader()

		username := opts.Username()
		password := opts.Password()
		server := opts.Servers()[0]
		host := server.Host
		port := server.Port()

		got := []string{username, password, host, port}
		expected := []string{conf.username, conf.password, conf.host, fmt.Sprintf("%d", conf.port)}

		for i := 0; i < len(expected); i++ {
			if got[i] != expected[i] {
				t.Errorf("expected %s ; got %s\n", got[i], expected[i])
			}
		}

		if opts.Username() != mqttUser.GetUsername() {
			t.Errorf("Expected %s ; got %s", opts.Username(), "F")
		}
	}
}
