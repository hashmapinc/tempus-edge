#!/bin/bash

# configure docker
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin

# install protoc
set -e
if [ ! -d "$HOME/protobuf/lib" ]; then # check to see if protobuf folder is empty
  wget https://github.com/google/protobuf/releases/download/v3.5.1/protobuf-all-3.5.1.tar.gz
  tar -xzvf protobuf-all-3.5.1.tar.gz
  cd protobuf-all-3.5.1 && ./configure --prefix=$HOME/protobuf && make && make install
else
  echo "Using cached directory."

