.. _developer-messages:

#########################
Tempus Edge Messaging
#########################

Standard message formatting should be used by all edge applications. The purpose of standard message formatting is:

- reduce message parsing cost
- promote reusability between edge applications
- maintain flexibility for future edge application requirements
- improve the ease of application debugging


Message Anatomy
===============
A Tempus Edge message is a single byte array that lives in the ``contentdata`` field of an IoFog message. 

In general, Tempus Edge messages consist of 3 parts:

:``protocol``: first byte of the array defining the :ref:`Tempus Edge message protocol <developer-messageProtocols>` to use.
:``type``: optional second byte of the array defining the :ref:`Tempus Edge message type <developer-messageTypes>` for the given ``protocol``.
:``payload``: optional remaining byte array containing a serialized protobuf message of the type defined by ``type``.


.. toctree::
    :maxdepth: 1
    :caption: Contents:

    messageProtocols
    messageTypes