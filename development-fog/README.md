# Development Fog

## Docker Quickstart
For development purposes, it is often easier to run iofog in a docker container locally.

To run this docker container, clone this repository, cd into this directory (development-fog), and execute the following commands 
```bash
mvn package
sh startFog.sh
```

## Development Notes
This container will run a full docker instance inside itself. This may have security implications and is therefore not suitable for anything other than local development on a dev machine.

To make this work, 3 special steps were necessary:

1. The Dockerfile must include `VOLUME /var/lib/docker` in order for the internal docker daemon to be able to create internal containers.
2. The container must be ran with the `--privileged` argument to allow the docker daemon to start.
3. The docker service must be started manually. This is acheived by the [entrypoint.sh](entrypoint/entrypoint.sh) script that is executed at container launch.

# Help
Please reach out to [Randy Pitcher](https://github.com/randypitcherii) if you have any questions.