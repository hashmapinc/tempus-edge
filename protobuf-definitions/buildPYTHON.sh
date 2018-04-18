#!/bin/bash

# Set dir variables
CURR_DIR=`pwd`
SRC_DIR=$CURR_DIR/src/main/protobuf
DEST_DIR=$CURR_DIR/python-protobuf-output/tempus/edge/proto

# recreate output directory
rm -rf    $DEST_DIR
mkdir -p  $DEST_DIR

# compile to python source code
protoc \
  --proto_path=$SRC_DIR \
  --python_out=$DEST_DIR \
  $SRC_DIR/*.proto

# add __init__.py to destination
touch $DEST_DIR/__init__.py

echo 
echo "Make sure to modify imports so they are python3 compatible!"
echo

# tell the dear user to add $DEST_DIR to their $GOPATH
echo "If you haven't already, please do not forget to add $CURR_DIR/python-protobuf-output to your \$PYTHONPATH to make proto go files importable."
