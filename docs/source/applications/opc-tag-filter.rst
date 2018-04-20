.. _applications-opc-tag-filter:

#################
OPC Tag Filter
#################
This edge application uses the track's ``config.pb`` file to connect to an OPC UA server and search for subscriptions.

Subscriptions are defined by any OPC node that matches the ``whitelist`` and does not match the ``blacklist`` regex arrays in the ``OpcConfig`` of the ``TrackConfig``.

The matching subscriptions are then assigned a ``deviceName`` based on a ``deviceMap`` structure in the ``OpcConfig``.

On startup and when new ``CONFIG UPDATE`` messages arrive, this edge application will use the latest ``config.pb`` to find new subscriptions and will send them to the Track Manager.


Building
========
Build the edge application and docker image using:

.. code-block:: bash

  mvn package


Help
====
If you need any help, please reach out to `Randy Pitcher <https://github.com/randypitcherii>`_.
