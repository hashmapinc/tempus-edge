#!/bin/bash

# remember project home dir
PROJECT_HOME=$(shell pwd)

# install lib/proto
cd lib/proto

# point golang vars to proper locations
export GOPATH="$HOME/go"
export PATH=$PATH:$GOPATH/bin
export GOPATH="$GOPATH:$PROJECT_HOME/lib/go"
export GOPATH="$GOPATH:$PROJECT_HOME/applications/mqtt-client-go"
export GOPATH="$GOPATH:$PROJECT_HOME/protobuf-definitions/go-protobuf-output"