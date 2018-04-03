# MQTT Client
This edge application is designed to act as a 2 way client between ioFog messages and MQTT messages. It is responsible for:
- accepting incoming MQTT messages and converting them to outgoing ioFog messages
- accepting incoming ioFog messages and converting them to outgoing MQTT messages

At this time, MQTT client is the core communication service between Tempus Edge and Tempus Cloud. Because of the way configuration is currently done, each Tempus Edge track will only support 1 MQTT client, which will always need to be Tempus Cloud.

In the future, this edge application may be split into the following 2 applications:
- a dedicated Tempus Cloud client
- a standard MQTT client

This will also result in separate TrackConfig entries for MqttConfig and TempusConfig based on the underlying MqttConfig.proto message definition.

## Expected IoFog Config
No expected ioFog config. All config should come through the track-manager.

## Expected Tempus Edge Config
See the [MQTT config protobuff definition](../../protobuf-definitions/src/main/protobuf/MqttConfig.proto)

## Building
Build the project and docker image using:
```bash
make build
```

## Usage
Build and deploy this image to dockerhub using:
```bash
make all
```

Publish the image to ioFog. From there, the image can be used in your tracks.

Any configurations you need should be provided through the track-manager.

## Help
If you need any help, please reach out to [Randy Pitcher](https://github.com/randypitcherii).