.. _applications:

#################
Edge Applications
#################

Tempus Edge currently supports the following edge applications:

+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`JSON Translator <applications-json-translator>`           | service for converting back and forth between JSON and Tempus Edge messages. |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`Golang MQTT Client <applications-mqtt-client-go>`         | MQTT client implemented in golang.                                           |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`Python MQTT Client <applications-mqtt-client-python>`     | Python implementation of an MQTT client.                                     |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`OPC Client <applications-opc-client>`                     | OPC client for monitoring and publishing to OPC UA servers.                  |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`OPC Tag Filter <applications-opc-tag-filter>`             | service for searching OPC UA servers for monitorable tags.                   |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`Timeseries Generator <applications-timeseries-generator>` | generates changing timeseries data.                                          |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+
| :ref:`Track Manager <applications-track-manager>`               | core track management service.                                               |
+-----------------------------------------------------------------+------------------------------------------------------------------------------+

.. toctree::
  :maxdepth: 1
  :caption: Applications:

  json-translator
  mqtt-client-go
  mqtt-client-python
  opc-client
  opc-tag-filter
  timeseries-generator
  track-manager

