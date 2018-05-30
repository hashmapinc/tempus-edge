#!/bin/bash

# configure go

# configure docker
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USER" --password-stdin
