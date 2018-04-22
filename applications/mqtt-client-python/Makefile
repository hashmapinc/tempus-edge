all:build

build:
	docker build --no-cache -t hashmapinc/tempus-edge-mqtt-client-python .
run: build
	docker run -it hashmapinc/tempus-edge-mqtt-client-python
shell: build
	docker run -it hashmapinc/tempus-edge-mqtt-client-python /bin/ash
deploy: build
	docker push hashmapinc/tempus-edge-mqtt-client-python
