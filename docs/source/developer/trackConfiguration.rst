.. _developer-trackConfiguration:

###################
Track Configuration
###################

Configuration for each application will be managed by an instance of the ``track-manager`` service. 

Configurations will be made available through a shared ``volume`` in each application's docker container. 

``config.pb`` Files
-------------------
Configurations are stored as ``protobuf`` files. The ``track-manager`` will store incoming configurations in the shared volume using the following convention:

.. code-block:: bash

  /mnt/config/config.pb

When new configurations are available, the ``track-manager`` will send a config protocol message to the track and each application will be responsible for updating their configs with the new ``config.pb`` contents.


Application Volume Mapping
----------------------------
The ``config.pb`` file in each **application container** is accessible through a shared volume at:

.. code-block:: bash

  /mnt/config/config.pb

The **host machine** where the ioFog agent is running will store shared configs at:

.. code-block:: bash

  /iofog/config/<YOUR_TRACK_NAME>/config.pb


This allows multiple tracks to run on the same ``fog`` host.

The volume mapping from the **host machine** to each **application container** is performed in ioAuthoring on each element in the volume mappings section. 

The JSON that defines this volume mapping is:

.. code-block:: json

  {
    "volumemappings": [
      {"hostdestination": "/iofog/config/<YOUR_TRACK_NAME>", 
      "containerdestination": "/mnt/config", 
      "accessmode": "rw"}
    ]
  }


