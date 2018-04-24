APP_NAME=opc-client

all: build

build:
	mvn clean compile -q

deploy:
	mvn clean package -q
	docker push hashmapinc/tempus-edge-$(APP_NAME)