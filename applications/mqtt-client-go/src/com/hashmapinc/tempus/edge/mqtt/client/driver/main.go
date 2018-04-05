/*
This package is the entrypoint for the tempus-edge-mqtt-client edge application.
*/
package main

import (
	"com/hashmapinc/tempus/edge/iofog"
	"com/hashmapinc/tempus/edge/mqtt"
	"com/hashmapinc/tempus/edge/proto"
	"fmt"
)

func main() {
	// Check that this runs
	fmt.Println("This is where the edge application lives")

	// Check the iofog package
	fmt.Println("Iofog has connection module: ", iofog.IsConnection)

	// Check the mqtt package
	fmt.Println("MQTT has connection module: ", mqtt.IsConnection)
	fmt.Println("MQTT has security module: ", mqtt.IsSecurity)

	// Check that the protos are ok
	var myTrackConfig = new(proto.TrackConfig)
	fmt.Println("Protos are importable: ", myTrackConfig.String)
}
