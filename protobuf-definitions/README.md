# Tempus Edge - Protobuf Definitions
This directory holds the protobuf `proto3` definitions for structuring standard Tempus Edge data.

The protobufs are defined in src/proto to conform with guidelines from the [ScalaPB library and Maven plugin](https://scalapb.github.io/).

Please feel free to submit pull requests if you'd like to extend these definitions or add new defs.

If you add new message protocols or message types, make sure to update the [README in the edge apps directory](../applications/README.md).

## Building
At this point, only Scala, Python, and Golang protobuf compiling is automated in this directory. Other languages will have to be compiled manually using `protoc`.

To build all, simply run `make` to execute each build script.

### Scala
Build using:
```bash
sh ./buildSCALA.sh
```

### Golang
Build using:
```bash
sh ./buildGOLANG.sh
```

To make the compiled go files available to your edge applications, ensure that you add the go build output directory to your `$GOPATH`.

### PYTHON
Build using:
```bash
sh ./buildPYTHON.sh
```

Please feel free to submit Pull Requests to automate protobuf compiling into other languages!