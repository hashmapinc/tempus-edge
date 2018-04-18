#!/bin/bash

# Set dir variables
CURR_DIR=`pwd`
SRC_DIR=$CURR_DIR/src/main/protobuf
DEST_DIR=$CURR_DIR/python-protobuf-output/tempus/edge/proto
PACKAGE_DIR=$CURR_DIR/python-protobuf-output

# recreate output directory
echo "recreating output directory..."
rm -rf    $DEST_DIR
mkdir -p  $DEST_DIR

# compile to python source code
echo "compiling proto defs into python src code..."
protoc \
  --proto_path=$SRC_DIR \
  --python_out=$DEST_DIR \
  $SRC_DIR/*.proto

# add __init__.py to destination
echo "adding __init__.py for python packaging..."
touch $DEST_DIR/__init__.py

echo "Modifying compiled files to be python3 compatible (relative importing fix)..."
sed -i '.old' 's/^import \([^ ]*\)_pb2 as \([^ ]*\)$/from . import \1_pb2 as \2/' $DEST_DIR/*_pb2.py
rm $DEST_DIR/*.old

echo "\n============================================================"
echo "installing python package with python3 for mac..."
echo "============================================================"
cd $PACKAGE_DIR
python3 setup.py develop
cd $CURR_DIR
echo "============================================================"

echo "\n\nif any of this didn't go well, please contact randy.pitcher@hashmapinc.com. It is his fault."
