# Make parameters
APP_NAME=mqtt-client
VERSION=0.1.0
EXECUTABLE=$(APP_NAME)$(VERSION)
TARGET_PACKAGE=com/hashmapinc/tempus/edge/mqtt/client/driver
MAIN_FILE=src/$(TARGET_PACKAGE)/main.go
TESTABLE_PACKAGES :=com/hashmapinc/tempus/edge/mqtt/client \
									com/hashmapinc/tempus/edge/mqtt \
									com/hashmapinc/tempus/edge/iofog

# default make command
all: local_compile

# build for local machine running
local_compile:
	go build -o $(EXECUTABLE) $(TARGET_PACKAGE)

test: local_compile
	go test -v $(TESTABLE_PACKAGES)

# build for an alpine-only environment
alpine_compile:
	CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o $(EXECUTABLE) $(TARGET_PACKAGE)

# build the docker image
docker: alpine_compile
	docker build -t hashmapinc/tempus-edge-$(APP_NAME):$(VERSION) --build-arg EXECUTABLE=$(EXECUTABLE) .

# deploy the docker image
deploy: docker
	docker push hashmapinc/tempus-edge-$(APP_NAME):$(VERSION)

# run the application locally
run: local_compile
	go run $(MAIN_FILE)

# run the docker container
runDocker:
	docker run -it hashmapinc/tempus-edge-$(APP_NAME):$(VERSION)

# run the docker container and go straight to a shell
runShell:
	docker run -it hashmapinc/tempus-edge-$(APP_NAME):$(VERSION) ash

# remove the executable
clean:
	rm $(EXECUTABLE)