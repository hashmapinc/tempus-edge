package iofog_test

import (
	"testing"
)

func TestTestability(t *testing.T) {
	if 1 != 1 {
		t.Fatalf("iofog connection is not testable")
	}
}
