/*
Package iofog contains reusable logic for establishing an iofog connection for tempus edge applications.
*/
package iofog

import (
	"log"
	"os"
)

// configure logger
var logger = log.New(os.Stderr, "", log.LstdFlags|log.LUTC|log.Lshortfile)
