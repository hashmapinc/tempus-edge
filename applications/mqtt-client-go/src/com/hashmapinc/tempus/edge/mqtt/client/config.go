/*
Package client contains application-specific logic to implement the
tempus-edge-mqtt-client edge application. This logic is executed by
the driver subpackage of this package.
*/
package client

import "com/hashmapinc/tempus/edge/proto"

// LocalTrackConfig - local config for this edge application
var LocalTrackConfig = new(proto.TrackConfig)

// UpdateTrackConfig attempts to update LocalTrackConfig with the latest track config
func UpdateTrackConfig() {
	LocalTrackConfig = new(proto.TrackConfig)
}
