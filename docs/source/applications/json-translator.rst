.. _applications-json-translator:

###############
JSON Translator
###############

This edge application provides interoperability between Tempus Edge applications that use a protobuf message serialization and default IoFog applications that serialize to JSON.

This scala application accepts incoming ioFog messages. If the incoming message is a JSON, a protobuf wrapper is created around the JSON and the message is then written to the ioFog message stream. If the incoming message is a tempus edge protobuf message, it is deserialized and converted to a JSON string that is written to the ioFog message stream.

Because Tempus Edge messages also contain 2 descriptive bytes at the beginning of incoming ioFog messageContent byte arrays, the Translator will treat all protobuf'd JSON strings as messages using the `DATA` protocol and of type `JSON`.

Expected IoFog Config
=====================
There is no expected config for this edge app.

Building
========
Build the project and docker image using:

.. code-block:: bash

  mvn package

Usage
=====
Deploy this image to dockerhub and publish to ioFog. From there, the image can be used in your tracks. Put it in between default IoFog services (like the stream viewer or the JSON REST API) and Tempus Edge applications (like the track-manager).

One use case would be to connect track-manager outputs to the json-translator and feed translations to the JSON REST API element so configuration events could be monitored by an external web GUI or dashboard application.

Help
====
If you need any help, please reach out to `Randy Pitcher <https://github.com/randypitcherii>`_.