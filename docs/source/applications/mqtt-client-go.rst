.. _applications-mqtt-client-go:

#################
Golang MQTT Client
#################

This edge application is designed to act as a 2 way client between ioFog messages and MQTT messages. It is responsible for:
- accepting incoming MQTT messages and converting them to outgoing ioFog messages
- accepting incoming ioFog messages and converting them to outgoing MQTT messages

At this time, MQTT client is the core communication service between Tempus Edge and Tempus Cloud. Because of the way configuration is currently done, each Tempus Edge track will only support 1 MQTT client, which will always need to be Tempus Cloud.

In the future, this edge application may be split into the following 2 applications:
- a dedicated Tempus Cloud client
- a standard MQTT client

This will also result in separate TrackConfig entries for MqttConfig and TempusConfig based on the underlying MqttConfig.proto message definition.

Building
========
Make sure your `$GOPATH` contains the `src` directory when you're ready to build.

Build the project and docker image using:

.. code-block:: bash

  make docker


Usage
=====
Build and deploy this image to dockerhub using:

.. code-block:: bash

  make deploy


Publish the image to ioFog. From there, the image can be used in your tracks.

Any configurations you need should be provided through the track-manager.

Supported Messages
==================
The client supports the following incoming tempus edge messages:

========  ==============  ========
Protocol  Type            MQTT-CLIENT Behavior 
========  ==============  ========
`CONFIG`  `UPDATE_ALERT`  updates current configs
`DATA`    `JSON`          attempts to convert the json string value into a `MQTT` message type then sends to the current broker.
`DATA`    `MQTT`          uses the `qos`, `topic`, and `payload` fields of the message to send an mqtt message to the current broker.
`DATA`    `OPC`           attempts to convert this message into an `MQTT` message type. `qos` is set to 2, `topic` is set to the value of 
                          the `deviceName` field, and `payload` is the byte array resulting from an `fmt.Sprintf` of the `value` oneof field 
                          where the formatting is based on the type of the oneof value. No set value will send an empty byte array as the `payload`
========  ==============  ========
The client converts all messages from the current broker into `DATA MQTT` tempus edge messages and sends them to the iofog message bus. `payload`s from the broker are not altered and are sent as the byte arrays that they are. 

Help
====
If you need any help, please reach out to `Randy Pitcher <https://github.com/randypitcherii>`_.
