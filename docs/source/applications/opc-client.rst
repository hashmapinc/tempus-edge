.. _applications-opc-client:

#################
OPC Client
#################
This edge application uses the track's ``config.pb`` file to connect to an OPC UA server. 

It performs 2 main functions:

- subscribes to OPC nodes and writes value changes to the IoFog message bus.
- writes OPC values when a new ``DATA OPC`` message arrives.

On startup and when new ``CONFIG UPDATE`` messages arrive, this edge application will use the latest ``config.pb`` to refresh its OPC UA subscriptions. 

The latest values for each subscribed OPC node will be written to the IoFog message bus each time subscriptions are updated, regardless of whether the values are actually new or not.

Building
========
Build the edge application and docker image using:

.. code-block:: bash

  mvn package


Help
====
If you need any help, please reach out to `Randy Pitcher <https://github.com/randypitcherii>`_.