# Make parameters
APP_NAME=mqtt-client-go
VERSION=0.1.02
EXECUTABLE=$(APP_NAME)$(VERSION)
TARGET_PACKAGE=com/hashmapinc/tempus/edge/mqtt/client/driver
MAIN_FILE=src/$(TARGET_PACKAGE)/main.go
TESTABLE_PACKAGES :=com/hashmapinc/tempus/edge/mqtt/client
TEST_HEADER="================================================================================="
TEST_FOOTER="=================================================================================\n\n"


# default make command
all: local_compile

# build for local machine running
local_compile:
	go build -o $(EXECUTABLE) $(TARGET_PACKAGE)

test: local_compile
	for tp in $(TESTABLE_PACKAGES); do \
		echo $(TEST_HEADER); \
		echo ------- testing $$tp -------; \
		echo $(TEST_HEADER); \
		go test $$tp; \
		echo $(TEST_FOOTER); \
	done;

# build for an alpine-only environment
alpine_compile:
	CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o $(EXECUTABLE) $(TARGET_PACKAGE)

# build the docker image
docker: alpine_compile
	docker build -t hashmapinc/tempus-edge-$(APP_NAME) --build-arg EXECUTABLE=$(EXECUTABLE) .
	docker tag hashmapinc/tempus-edge-$(APP_NAME) hashmapinc/tempus-edge-$(APP_NAME):$(VERSION)

# deploy the docker image
deploy: test docker
	docker push hashmapinc/tempus-edge-$(APP_NAME)

# run the application locally
run: local_compile
	go run $(MAIN_FILE)

# run the docker container
runDocker: docker
	docker run -it hashmapinc/tempus-edge-$(APP_NAME):$(VERSION)

# run the docker container and go straight to a shell
runShell:
	docker run -it hashmapinc/tempus-edge-$(APP_NAME):$(VERSION) ash

# remove the executable
clean:
	rm $(EXECUTABLE)