#!/bin/bash

# Set dir variables
CURR_DIR=`pwd`
SRC_DIR=$CURR_DIR/src/main/protobuf
DEST_DIR=$CURR_DIR/go-protobuf-output

# recreate output directory
rm -rf    $DEST_DIR
mkdir -p  $DEST_DIR/src

# compile to go source code
protoc \
  --proto_path=$SRC_DIR \
  --go_out=$DEST_DIR/src \
  $SRC_DIR/*.proto

# tell the dear user to add $DEST_DIR to their $GOPATH
echo "Please do not forget to add $DEST_DIR to your \$GOPATH to make proto go files importable!"
