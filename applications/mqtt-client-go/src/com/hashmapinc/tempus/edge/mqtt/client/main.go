package main

import (
	"com/hashmapinc/tempus/edge/iofog"
	"com/hashmapinc/tempus/edge/mqtt"
	"fmt"
)

func main() {
	// Check that this runs
	fmt.Println("This is where the edge application lives")

	// Check the iofog package
	fmt.Println("Iofog has connection module: ", iofog.IsConnection)
	fmt.Println("Iofog has controller module: ", iofog.IsController)

	// Check the mqtt package
	fmt.Println("MQTT has connection module: ", mqtt.IsConnection)
	fmt.Println("MQTT has controller module: ", mqtt.IsController)
	fmt.Println("MQTT has security module: ", mqtt.IsSecurity)
}
