/*
Package client contains application-specific logic to implement the
tempus-edge-mqtt-client edge application. This logic is executed by
the driver subpackage of this package.
*/
package client

import (
	pb "com/hashmapinc/tempus/edge/proto"
	"errors"
	"log"
	"os"
)

// configure logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// LocalTrackConfig - local config for this edge application
var LocalTrackConfig = &pb.TrackConfig{}

// pathToTrackConfig holds the expected location of production configs
const pathToTrackConfig = "/mnt/config/config.pb"

// UpdateTrackConfig attempts to update LocalTrackConfig with the latest track config
func UpdateTrackConfig() error {
	newConfig, err := loadTrackConfig(pathToTrackConfig)
	if err == nil {
		*LocalTrackConfig = newConfig
	}
	return err
}

/* loadTrackConfig loads the config.pb file at path and stores it in tc.
@param path - string containing the full path to the config.pb file to load

@return tc - pb.TrackConfig loaded from path
@return err - error, if any, from loading.
*/
func loadTrackConfig(path string) (tc pb.TrackConfig, err error) {
	// TODO: implement this for real
	tc.TrackMetadata = &pb.TrackMetadata{TrackId: 1, TrackName: "new_track"}
	err = errors.New("Error in loadTrackConfig")
	return
}
