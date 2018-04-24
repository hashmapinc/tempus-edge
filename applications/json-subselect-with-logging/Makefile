# default make command
all: local_compile

# build for local machine running
local_compile:
	go build -o subselect .

# build for an alpine-only environment
alpine_compile:
	CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o subselect .

# build the docker image
docker: alpine_compile
	docker build -t hashmapinc/iofog-subselect-with-logging .

# deploy the docker image
deploy: docker
	docker push hashmapinc/iofog-subselect-with-logging

# remove the executable
clean:
	rm subselect