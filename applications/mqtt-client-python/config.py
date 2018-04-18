import threading
from tempus.edge.proto import TrackConfig_pb2 as TC

config_lock = threading.Lock()
CONFIG_PATH="./config.pb"
tc = TC.TrackConfig()

def updateTrackConfig():
  try:
    with config_lock:
      tc.Clear()
      tc.ParseFromString(open(CONFIG_PATH, "rb").read())
      print("succesfully updated track config:")
      print(str(tc))
  except Exception as err:
    print("could not update track config: " + str(err))

def getBroker():
  broker = {}
  try:
    with config_lock:
      broker['host'] = tc.mqtt_config.broker.host
      broker['port'] = tc.mqtt_config.broker.port
  except Exception as err:
    print("could get broker: " + str(err))
  return broker


def getUser():
  user = {}
  try:
    with config_lock:
      user['username'] = tc.mqtt_config.user.username
      user['password'] = tc.mqtt_config.user.password
  except Exception as err:
    print("could not get user: " + str(err))
  return user


def getCert():
  return None


def getSubscriptions():
  subs = []
  try:
    with config_lock:
      for sub in tc.mqtt_config.subscriptions:
        subs.append({'topic': sub.topic, 'qos': sub.qos})
  except Exception as err:
    print("could not get subscriptions: " + str(err))
  return subs
