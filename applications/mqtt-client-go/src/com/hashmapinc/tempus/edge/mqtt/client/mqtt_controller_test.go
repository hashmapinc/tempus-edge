package client_test

import (
	"testing"
)

func TestTestability(t *testing.T) {
	if 1 != 1 {
		t.Fatalf("mqtt client is not testable")
	}
}
