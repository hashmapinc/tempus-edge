#!/bin/bash

# Set dir variables
CURR_DIR=`pwd`
SRC_DIR=$CURR_DIR/src/main/protobuf
DEST_DIR=$CURR_DIR/go-protobuf-output

# recreate output directory
echo "Setting up go output directory..."
rm -rf    $DEST_DIR
mkdir -p  $DEST_DIR/src

# compile to go source code
echo "Compiling protobuf defs into golang src code..."
protoc \
  --proto_path=$SRC_DIR \
  --go_out=$DEST_DIR/src \
  $SRC_DIR/*.proto

# tell the dear user to add $DEST_DIR to their $GOPATH
echo "If you haven't already, please do not forget to add $DEST_DIR to your \$GOPATH to make proto go files importable."
