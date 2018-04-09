package iofog_test

import (
	"com/hashmapinc/tempus/edge/iofog"
	"fmt"
	"testing"
)

// Checks the type of the iofog client
func TestConnection(t *testing.T) {
	fmt.Printf("ioFog client = %T\n", iofog.Client)
}
