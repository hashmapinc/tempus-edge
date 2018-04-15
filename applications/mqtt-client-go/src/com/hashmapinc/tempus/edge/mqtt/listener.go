package mqtt

import paho "github.com/eclipse/paho.mqtt.golang"

// Listener is a function type for handling mqtt data events
type Listener func(client paho.Client, msg paho.Message) error
