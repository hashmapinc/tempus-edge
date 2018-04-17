package client

import (
	"io/ioutil"
	"testing"

	pb "com/hashmapinc/tempus/edge/proto"

	"github.com/golang/protobuf/proto"
)

var configPath = "./testdata/trackConfig.pb"

func TestLoadTrackConfig(t *testing.T) {
	// read file
	in, err := ioutil.ReadFile(configPath)
	if err != nil {
		t.Errorf("File at path '%s' could not be read\n", configPath)
	}

	// unmarshal (deserialize) track config
	expected := &pb.TrackConfig{}
	if err := proto.Unmarshal(in, expected); err != nil {
		t.Errorf("Failed to parse track config: %s\n", err.Error())
	}

	// use loadTrackConfig method to get a loaded track config
	got := &pb.TrackConfig{}
	err = loadTrackConfig(configPath, got)

	// compare
	t.Logf("Expected track config: %v\n", expected)
	t.Logf("Got track config: %v\n", got)
	if nil == got || nil == expected || !proto.Equal(got, expected) {
		t.Errorf("Expected track config was not gotten!\n")
	}
}
