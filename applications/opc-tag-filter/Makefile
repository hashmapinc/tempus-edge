APP_NAME=opc-tag-filter

all: build

build:
	mvn clean compile -q

deploy:
	mvn clean package -q
	docker push hashmapinc/tempus-edge-$(APP_NAME)