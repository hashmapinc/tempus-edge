.. _applications:

#################
Edge Applications
#################

Tempus Edge currently supports the following edge applications:

:ref:`JSON Translator <applications-json-translator>`
  service for converting back and forth between JSON and Tempus Edge messages.

:ref:`Golang MQTT Client <applications-mqtt-client-go>`
  MQTT client implemented in golang.

:ref:`OPC Client <applications-opc-client>`
  OPC client for monitoring and publishing to OPC UA servers.

:ref:`OPC Tag Filter <applications-opc-tag-filter>`
  service for searching OPC UA servers for monitorable tags.

:ref:`Track Manager <applications-track-manager>`
  core track management service.


Additionaly, the following microservices exists but are currently undocumented:

- Timeseries Generator - generates changing timeseries data.
- JSON Filter - filters incoming JSON.
- Python MQTT Client - Python implementation of an MQTT client.

.. toctree::
  :maxdepth: 1
  :caption: Applications:

  json-translator
  mqtt-client-go
  opc-client
  opc-tag-filter
  track-manager

