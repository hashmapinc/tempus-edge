.. _developer-messageTypes:

#############
Message Types
#############

In general, a `messageType` will be similar to a `messageProtocol`. It will also be a `byte` value and will be the **second byte** in the ioFog message content field. The type will serve to further specify how to decode the incoming message content or further specify what action a container should take when receiving a message.

The message types for each message protocol are defined below:

Configuration Message Types
===========================
Protocol Byte  | Type Byte | Protobuf Enum | Description
------------- | ------------- | ------------- | -------------
`001`  | `000` | `UPDATE_ALERT` | informs the receiver that new configs are available. The receiver is expected to update their own configs.
`001`  | `001` | `TRACK_CONFIG_SUBMISSION` | informs the receiver that a new track config is contained in this message and that the receiver should parse and persist the new track config. The `track-manager` is the intended consumer of this message type. **NOTE:** this config will be accepted as the new config by the track manager. This means changes will not be 'merged' with current configs. Use this message type when you want to submit a completely new config. Use other message types to update individual pieces of a track's configuration.
`001`  | `002` | `TRACK_METADATA_SUBMISSION` | this message contains a TrackMetadata protobuf byte array and commands the receiver to update a track's metadata.
`001`  | `003` | `MQTT_CONFIG_SUBMISSION` | this message contains an MqttConfig protobuf byte array and commands the receiver to update a track's MQTT configuration.
`001`  | `004` | `OPC_CONFIG_SUBMISSION` | this message contains an OpcConfig protobuf byte array and commands the receiver to update a track's OPC configuration.
`001`  | `005` | `OPC_SUBSCRIPTIONS_SUBMISSION` | this message contains an OpcConfig.subs value (OpcConfig.Subscriptions protobuf message) and commands the receiver to u pdate a track's OPC configuration subscriptions value.


Data Message Types
==================
Protocol Byte  | Type Byte | Protobuf Enum | Description
------------- | ------------- | ------------- | -------------
`002`  | `000` | `JSON` | used to define a message that is a protobuf byte array that will decode into a JSON string that will need to be parsed by the receiver.
`002`  | `001` | `MQTT` | used to define a message that is a protobuf byte array that will decode into an MQTT message. This message contains everything necessary for the `mqtt-client` to send this message in MQTT format.
`002`  | `002` | `OPC` | used to define a message that is a protobuf byte array that will decode into an OPC message. This message contains everything necessary for the `opc-client` to send this message in OPC format.

NOTE: new message types will be defined here as new types become necessary. Please send a pull request if you'd like to suggest other types!