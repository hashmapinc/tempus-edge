# Define dirs
CURR_DIR=$(shell pwd)
JSON_TRANSLATOR=$(CURR_DIR)/json-translator
MQTT_CLIENT_GO=$(CURR_DIR)/mqtt-client-go
MQTT_CLIENT_PYTHON=$(CURR_DIR)/mqtt-client-python
OPC_CLIENT=$(CURR_DIR)/opc-client
OPC_TAG_FILTER=$(CURR_DIR)/opc-tag-filter
TIMESERIES_GENERATOR=$(CURR_DIR)/timeseries-generator
TRACK_MANAGER=$(CURR_DIR)/track-manager

# define docker repo to deploy to
DOCKER_REPO=hashmapinc

all: build 

build:build-json-translator build-mqtt-client-go build-mqtt-client-python build-opc-client build-opc-tag-filter build-timeseries-generator build-track-manager

build-json-translator: 
	echo "\n\n\n\n\n"
	cd $(JSON_TRANSLATOR) && \
	mvn clean package -q

build-mqtt-client-go: 
	echo "\n\n\n\n\n"
	cd $(MQTT_CLIENT_GO) && \
	make clean docker

build-mqtt-client-python: 
	echo "\n\n\n\n\n"
	cd $(MQTT_CLIENT_PYTHON) && \
	make build

build-opc-client: 
	echo "\n\n\n\n\n"
	cd $(OPC_CLIENT) && \
	mvn clean package -q

build-opc-tag-filter: 
	echo "\n\n\n\n\n"
	cd $(OPC_TAG_FILTER) && \
	mvn clean package -q

build-timeseries-generator: 
	echo "\n\n\n\n\n"
	cd $(TIMESERIES_GENERATOR) && \
	echo "timeseries-generator building is trash and is not automated. See github issues if you want to fix this."

build-track-manager: 
	echo "\n\n\n\n\n"
	cd $(TRACK_MANAGER) && \
	mvn clean package -q


deploy:deploy-json-translator deploy-mqtt-client-go deploy-mqtt-client-python deploy-opc-client deploy-opc-tag-filter deploy-timeseries-generator deploy-track-manager

deploy-json-translator: 
	echo "\n\n\n\n\n"
	docker push $(DOCKER_REPO)/tempus-edge-json-translator

deploy-mqtt-client-go: 
	echo "\n\n\n\n\n"
	cd $(MQTT_CLIENT_GO) && \
	make deploy

deploy-mqtt-client-python: 
	echo "\n\n\n\n\n"
	cd $(MQTT_CLIENT_PYTHON) && \
	make deploy

deploy-opc-client: 
	echo "\n\n\n\n\n"
	docker push $(DOCKER_REPO)/tempus-edge-opc-client

deploy-opc-tag-filter: 
	echo "\n\n\n\n\n"
	docker push $(DOCKER_REPO)/tempus-edge-opc-tag-filter

deploy-timeseries-generator: 
	echo "\n\n\n\n\n"
	echo "timeseries-generator deployment is trash and is not automated. See github issues if you want to fix this."

deploy-track-manager: 
	echo "\n\n\n\n\n"
	docker push $(DOCKER_REPO)/tempus-edge-track-manager
