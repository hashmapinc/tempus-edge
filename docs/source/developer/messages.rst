#########################
Track Messaging Standards
#########################

Standard message formatting should be used by all edge applications. The purpose of standard message formatting is:

- reduce message parsing cost
- promote reusability between edge applications
- maintain flexibility for future edge application requirements
- improve the ease of application debugging

Message Anatomy
===============
A tempus edge message is a single byte array that lives in the `contentdata` feild of an IoFog message. 

In general, tempus edge messages consist of 3 parts:

:`protocol`: first byte of the array defining the tempus edge message protocol to use.
:`type`: optional second byte of the tempus edge message defining the payload type for the given `protocol`.
:`payload`: optional remaining byte array containing a serialized protobuf message of the type defined by `type`.

Protocols and Types
===================
.. toctree::
    :maxdepth: 1

    messageProtocols
    messageTypes