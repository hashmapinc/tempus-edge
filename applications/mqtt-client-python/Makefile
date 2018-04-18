all:build

build:
	docker build --no-cache -t hashmapinc/iofog:mqtt-client .
run: build
	docker run -it hashmapinc/iofog:mqtt-client
shell: build
	docker run -it hashmapinc/iofog:mqtt-client /bin/ash
deploy: build
	docker push hashmapinc/iofog:mqtt-client
