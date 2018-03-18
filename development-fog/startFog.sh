#!/bin/bash

# run privileged container so that internal docker daemon will work
docker run -it --privileged hashmapinc/tempus-edge-development-fog:0.1.0