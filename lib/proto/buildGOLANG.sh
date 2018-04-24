#!/bin/bash

# Set dir variables
CURR_DIR=`pwd`
LIB_DIR=$CURR_DIR/..
SRC_DIR=$CURR_DIR/src/main/protobuf
DEST_DIR=$LIB_DIR/go

# compile to go source code
echo "Compiling protobuf defs into golang src code..."
protoc \
  --proto_path=$SRC_DIR \
  --go_out=$DEST_DIR/src \
  $SRC_DIR/*.proto
