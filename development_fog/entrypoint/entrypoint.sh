#!/bin/bash

# start docker daemon
service docker start

# start iofog
service iofog start

# execute the CMD
exec "$@"