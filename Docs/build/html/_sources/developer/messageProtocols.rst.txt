.. _developer-messageProtocols:

#################
Message Protocols
#################

Each IoFog message should contain a `messageProtocol` byte that describes the kind of message being sent. This value is a `byte` or `uint8` depending on the language. It is the **first byte** in the messageContent field of the IoFog message. The significance of each value is detailed below:

Protocol Byte  | Protocol Name | Description
------------- | ------------- | -------------
`000`  | Undefined protocol | This can indicate a message that didn't originate from a Hashmap application. This is used to allow support for JSON-based messaging used in default ioFog elements.
`001`  | Config protocol | describes messages relating to configurations. This is generally either commanding a receiving element to accept new configs or alerting elements that new configs are available.
`002`  | Data protocol | used for standard data passing from element to element.

NOTE: new message protocols will be defined here as new protocols become necessary. Please send a pull request if you'd like to suggest other protocols!
