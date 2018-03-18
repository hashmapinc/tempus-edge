<img src="./Docs/images/TempusEdgeforWeb.png" alt="Tempus Edge"></img>
[![License](http://img.shields.io/:license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Waffle.io - Columns and their card count](https://badge.waffle.io/hashmapinc/tempus-edge.svg?columns=all)](https://waffle.io/hashmapinc/tempus-edge)

# Tempus Edge
This repository holds all code for Tempus Edge from [Hashmap](https://www.hashmapinc.com/).

## Repository Standards
- Application folders should be all lower case with '-' to separate words; no underscores, no spaces, no camelCase.
- Application folders should contain Dockerfiles defining the applicaiton image.
  - Docker images should be named using the pattern `hashmapinc/tempus-edge-<APPLICATION-NAME>:<VERSION>`
- Application folders should contain a README.md describing the application and usage. This includes instructions for building and running both the Docker image and the source code.
- Build files / directories should not be committed to the repository. This means no JAR files, no \*.pyc files, and no mvn \*\*/target/ build directories.
- All commits should result in buildable code. The code may be buggy, but please do not commit unbuildable code if you can help it.

NOTE: Please submit pull requests to udpate these rules if you think something else makes more sense!

## Configuration Standards
Configuration for each application will be managed by an instance of the `track-manager` service. Configurations will be made available through a shared `volume` in each application's docker container. 

### Configuration Volume Mapping
The volume in each **application container** is accessible at:
```
/mnt/config/config.pb
```

The **host machine** where the ioFog agent is running will store shared configs at:
```
/iofog/config/<YOUR_TRACK_NAME>/config.pb
```

This allows multiple tracks to run on the same `fog` host.

The volume mapping from the **host machine** to each **application container** is performed in ioAuthoring on each element in the volume mappings section. The JSON that defines this volume mapping is:
```json
{"volumemappings": [
  {"hostdestination": "/iofog/config/<YOUR_TRACK_NAME>", 
   "containerdestination": "/mnt/config", 
   "accessmode": "rw"}
]}
```

### Configuration Files
Configurations are stored as `protobuf` files. The `track-manager` will store incoming configurations in the shared volume using the following convention:
```
/mnt/config/<YOUR_TRACK_NAME>/config.pb
```

When new configurations are available, the `track-manager` will send a config protocol message to the track and each application will be responsible for updating their configs with the new `config.pb` contents.

## IoFog Messaging Standards
Standard message formatting should be used by all edge applications. The purpose of standard message formatting is:

- reduce message parsing cost
- promote reusability between edge applications
- maintain flexibility for future edge application requirements
- improve the ease of application debugging

### Message Protocol
Each IoFog message should contain a `messageProtocol` byte that describes the kind of message being sent. This value is a `byte` or `uint8` depending on the language. It is the **first byte** in the messageContent field of the IoFog message. The significance of each value is detailed below:

Protocol Byte  | Protocol Name | Description
------------- | ------------- | -------------
`000`  | Undefined protocol | This can indicate a message that didn't originate from a Hashmap application. This is used to allow support for JSON-based messaging used in default ioFog elements.
`001`  | Config protocol | describes messages relating to configurations. This is generally either commanding a receiving element to accept new configs or alerting elements that new configs are available.
`002`  | Data protocol | used for standard data passing from element to element.

NOTE: new message protocols will be defined here as new protocols become necessary. Please send a pull request if you'd like to suggest other protocols!

### Message Type
In general, a `messageType` will be similar to a `messageProtocol`. It will also be a `byte` value and will be the **second byte** in the ioFog message content field. The type will serve to further specify how to decode the incoming message content or further specify what action a container should take when receiving a message.

The message types for each message protocol are defined below:

Protocol Byte  | Type Byte | Protobuf Enum | Description
------------- | ------------- | ------------- | -------------
`000`  | `-` | not defined | -
`001`  | `000` | `UPDATE_ALERT` | informs the receiver that new configs are available. The receiver is expected to update their own configs.
`001`  | `001` | `TRACK_CONFIG_SUBMISSION` | informs the receiver that a new track config is contained in this message and that the receiver should parse and persist the new track config. The `track-manager` is the intended consumer of this message type. **NOTE:** this config will be accepted as the new config by the track manager. This means changes will not be 'merged' with current configs. Use this message type when you want to submit a completely new config. Use other message types to update individual pieces of a track's configuration.
`001`  | `002` | `TRACK_METADATA_SUBMISSION` | this message contains a TrackMetadata protobuf byte array and commands the receiver to update a track's metadata.
`001`  | `003` | `MQTT_CONFIG_SUBMISSION` | this message contains an MqttConfig protobuf byte array and commands the receiver to update a track's MQTT configuration.
`001`  | `004` | `OPC_CONFIG_SUBMISSION` | this message contains an OpcConfig protobuf byte array and commands the receiver to update a track's OPC configuration.
`002`  | `000` | `JSON` | used to define a message that is a protobuf byte array that will decode into a JSON string that will need to be parsed by the receiver.
`002`  | `001` | `MQTT` | used to define a message that is a protobuf byte array that will decode into an MQTT message. This message contains everything necessary for the `mqtt-client` to send this message in MQTT format.
`002`  | `002` | `OPC` | used to define a message that is a protobuf byte array that will decode into an OPC message. This message contains everything necessary for the `opc-client` to send this message in OPC format.

NOTE: new message types will be defined here as new types become necessary. Please send a pull request if you'd like to suggest other types!