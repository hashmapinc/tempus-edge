# Edge Applications

## Repository Standards
- Application folders will be all lower case with '-' to separate words; no underscores, no spaces, no camelCase. This is to conform with Docker image naming conventions.
- Application folders shall contain Dockerfiles defining the applicaiton image.
- Applications shall contain a README.md describing the application and usage. This includes instructions for building and running both the Docker image and the source code.
- Build files / directories shall not be committed to the repository. This means no JAR files, no \*.pyc files, and no mvn \*\*/target/ build directories.
- All commits shall result in buildable code. The code may be buggy, but do not commit unbuildable code.


NOTE: Please submit pull requests to udpate these rules if you think something else makes more sense!

## Configuration Standards
Configuration for each application will be managed by an instance of the `track-manager` service. Configurations will be made available through a shared `volume` in each application's docker container. 

### Configuration Volume Mapping
The volume in each **application container** is accessible at:
```
/mnt/config
```

The **host machine** where the ioFog agent is running will store shared configs at:
```
/iofog/config/<YOUR_TRACK_NAME>
```

This allows multiple tracks to run on the same `fog` instance.

The volume mapping from the **host machine** to each **application container** is performed in ioAuthoring on each element in the volume mappings section. The JSON that defines this volume mapping is:
```json
{"volumemappings": [
  {"hostdestination": "/iofog/config/YOUR_TRACK_NAME", 
   "containerdestination": "/mnt/config", 
   "accessmode": "rw"}
]}
```

### Configuration Files
Configurations are stored as `protobuf` files. The `track-manager` will store incoming configurations in the shared volume using the following convention:
```
/mnt/config/<APPLICATION-NAME>/config.pb
```

When new configurations are available, the `track-manager` will send a config message to the track and each application will be responsible for updating their configs with the new `config.pb` contents.

## IoFog Messaging Standards
Standard message formatting should be used by all edge applications. The purpose of standard message formatting is:

- reduce message parsing cost
- promote reusability between edge applications
- maintain flexibility for future edge application requirements
- improve the ease of application debugging

### Message Types
Each IoFog message should contain a `messageType` field that describes the type of the message. This value is a `byte` or `uint8` depending on the language. The significance of each value is detailed below:

* `000` = undefined message - should not be used. This can indicate a message that didn't originate from a Hashmap application.
* `001` = general data message - used for standard data passing from element to element
* `002` = config alert message - alerts applications to check for updated configuration.
* `003` = update config message - used to submit new configs to the `track-manager`

NOTE: new message types will be defined here as new types become necessary. Please send a pull request if you'd like to suggest other types!

### Message Subtypes
Usage of `messageSubtype` is still not fully defined. In general, a message subtype will be similar to a `messageType`.

NOTE: new message subtypes will be defined here as new subtypes become necessary. Please send a pull request if you'd like to suggest other subtypes!