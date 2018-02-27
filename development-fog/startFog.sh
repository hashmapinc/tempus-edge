#!/bin/bash

docker build -t development_fog .

# run privileged container so that internal docker daemon will work
docker run -it \
  --privileged \
  development_fog