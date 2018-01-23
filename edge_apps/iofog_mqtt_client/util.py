TOPIC = 'topic'
QOS = 'qos'
PAYLOAD = 'payload'
SUBSCRIPTIONS = 'subscriptions'
PUBLISHERS = 'publishers'
MQTT_INFO_TYPE = 'mqtt/message'
MQTT_INFO_FORMAT = 'application/json'
BROKER = 'broker'
TLS = 'tls'
CA_CERT = 'ca_cert'
USER = 'user'
USERNAME = 'username'
PASSWORD = 'password'
HOST = 'host'
BIND_ADDRESS = 'bind_address'
PORT = 'port'
TRANSPORT = 'transport'
TCP = 'tcp'

DEFAULT_PORT = 1883
DEFAULT_ADDRESS = ''
CERT_FILE_LOCATION = './ca_cert.pem'

config_schema = {
    "type": "object",
    "properties": {
        SUBSCRIPTIONS: {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    TOPIC: {
                        "type": "string",
                        "minLength": 1
                    },
                    QOS: {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 2
                    }
                },
                "required": [TOPIC, QOS]
            }
        },
        PUBLISHERS: {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    TOPIC: {
                        "type": "string",
                        "minLength": 1
                    },
                    QOS: {
                        "type": "integer",
                        "minimum": 0,
                        "maximum": 2
                    }
                },
                "required": [TOPIC, QOS]
            }
        },
        BROKER: {
            "type": "object",
            "properties": {
                TRANSPORT: {
                    "type": "string",
                    "enum": ["tcp", "websockets"]
                },
                HOST: {
                    "type": "string",
                    "minLength": 1
                },
                PORT: {
                    "type": "integer",
                    "minimum": 0,
                    "exclusiveMinimum": True
                },
                BIND_ADDRESS: {
                    "type": "string",
                    "minLength": 1
                }
            },
            "required": [HOST]
        },
        USER: {
            "type": "object",
            "properties": {
                USERNAME: {
                    "type": "string",
                    "minLength": 1
                },
                PASSWORD: {
                    "type": "string",
                    "minLength": 1
                }
            },
            "required": [USERNAME]
        },
        CA_CERT: {
            "type": "string",
            "minLength": 1
        }
    },
    "required": [BROKER]
}


def read_certificate():
    with open(CERT_FILE_LOCATION) as f:
        return f.read()


def write_certificate(cert):
    with open(CERT_FILE_LOCATION, 'w') as f:
        f.write('-----BEGIN CERTIFICATE-----\n')
        while cert:
            f.write(cert[:64])
            f.write('\n')
            cert = cert[64:]
        f.write('-----END CERTIFICATE-----\n')
