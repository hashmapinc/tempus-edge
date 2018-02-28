# Track Manager
This edge application is core to Tempus Edge. It is the "brain" that allows for seamless control of individual tracks within a fog from both ioFog and Tempus.

This scala application takes initial configs from ioFog and stores them in a sqlite DB. This DB is accessible to all track elements through a shared volume (mounted at /iofog/config/< track-name > on the host).

As updated configurations come from ioFog and Tempus (through the mqtt client element), Track Manager updates the sqlite DB and sends newConfig messages to the iofog message queue. Other elements in the track are then responsible for handling new configs in response to this newConfig message. Most often, this will involve querying the sqlite DB through their volume mount (located in each container at /mnt/config).

## Building
Build the project using:
```bash
mvn package
``` 

Build the docker image using:
```bash
sh buildDockerImage.sh
```

## Usage
Deploy this image to dockerhub and publish to ioFog. From there, the image can be used in your tracks.

Any configurations you need to make available to your track elements should be provided through ioAuthoring to this element.

## Help
If you need any help, please reach out to [Randy Pitcher](https://github.com/randypitcherii).