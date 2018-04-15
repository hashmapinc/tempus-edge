package client

import paho "github.com/eclipse/paho.mqtt.golang"

// OnMqttMessage handles new incoming mqtt messages
func OnMqttMessage(mqttClient paho.Client, msg paho.Message) {
	// TODO: do this for real
	logger.Printf("received mqtt msg: %v \n", msg)
}
