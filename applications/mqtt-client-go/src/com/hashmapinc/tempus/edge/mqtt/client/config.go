/*
Package client contains application-specific logic to implement the
tempus-edge-mqtt-client edge application. This logic is executed by
the driver subpackage of this package.
*/
package client

import (
	pb "com/hashmapinc/tempus/edge/proto"
	"io/ioutil"
	"log"
	"os"
	"sync"

	"github.com/golang/protobuf/proto"
)

// configure logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)

// LocalTrackConfig - local config for this edge application
var LocalTrackConfig = &pb.TrackConfig{}

// TrackConfigWriteLock ensures the local track config is threadsafe
var TrackConfigWriteLock = &sync.Mutex{}

// pathToTrackConfig holds the expected location of production configs
const pathToTrackConfig = "/mnt/config/config.pb"

// UpdateTrackConfig attempts to update LocalTrackConfig with the latest track config
func UpdateTrackConfig() error {
	err := loadTrackConfig(pathToTrackConfig, LocalTrackConfig)
	return err
}

/* loadTrackConfig loads the config.pb file at path and stores it in tc.
@param path - string containing the full path to the config.pb file to load
@param tc - pb.TrackConfig pointer to load track config into

@return err - error, if any, from loading.
*/
func loadTrackConfig(path string, tc *pb.TrackConfig) (err error) {
	// lock and defer unlock
	TrackConfigWriteLock.Lock()
	defer TrackConfigWriteLock.Unlock()

	// read file
	in, err := ioutil.ReadFile(path)
	if err != nil {
		logger.Println("File at path", path, "could not be read")
		return
	}

	// unmarshal (deserialize) track config
	if err = proto.Unmarshal(in, tc); err != nil {
		logger.Println("Failed to parse track config:", err.Error())
	}
	return
}
