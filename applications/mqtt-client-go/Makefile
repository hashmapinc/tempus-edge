all:build push

build:
	docker build -t hashmapinc/tempus-edge-mqtt-client:0.1.0 .
run:
	docker run -v /Users/randypitcherii/projects/tempus-edge/applications/mqtt-client-go:/mnt/go -w /mnt/go -it hashmapinc/tempus-edge-mqtt-client:0.1.0
push:
	docker push hashmapinc/tempus-edge-mqtt-client:0.1.0
