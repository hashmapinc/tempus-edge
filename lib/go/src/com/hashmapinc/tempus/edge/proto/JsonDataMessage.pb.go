// Code generated by protoc-gen-go. DO NOT EDIT.
// source: JsonDataMessage.proto

/*
Package proto is a generated protocol buffer package.

It is generated from these files:
	JsonDataMessage.proto
	MessageProtocols.proto
	MessageTypes.proto
	MqttConfig.proto
	MqttMessage.proto
	OpcConfig.proto
	OpcMessage.proto
	TrackConfig.proto
	TrackMetadata.proto

It has these top-level messages:
	JsonDataMessage
	MqttConfig
	MqttMessage
	OpcConfig
	OpcMessage
	TrackConfig
	TrackMetadata
*/
package proto

import proto1 "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto1.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto1.ProtoPackageIsVersion2 // please upgrade the proto package

// =============================================================================
// Define protobuf wrapper for json strings
// =============================================================================
type JsonDataMessage struct {
	Json string `protobuf:"bytes,1,opt,name=json" json:"json,omitempty"`
}

func (m *JsonDataMessage) Reset()                    { *m = JsonDataMessage{} }
func (m *JsonDataMessage) String() string            { return proto1.CompactTextString(m) }
func (*JsonDataMessage) ProtoMessage()               {}
func (*JsonDataMessage) Descriptor() ([]byte, []int) { return fileDescriptor0, []int{0} }

func (m *JsonDataMessage) GetJson() string {
	if m != nil {
		return m.Json
	}
	return ""
}

func init() {
	proto1.RegisterType((*JsonDataMessage)(nil), "com.hashmapinc.tempus.edge.proto.JsonDataMessage")
}

func init() { proto1.RegisterFile("JsonDataMessage.proto", fileDescriptor0) }

var fileDescriptor0 = []byte{
	// 120 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0xe2, 0x12, 0xf5, 0x2a, 0xce, 0xcf,
	0x73, 0x49, 0x2c, 0x49, 0xf4, 0x4d, 0x2d, 0x2e, 0x4e, 0x4c, 0x4f, 0xd5, 0x2b, 0x28, 0xca, 0x2f,
	0xc9, 0x17, 0x52, 0x48, 0xce, 0xcf, 0xd5, 0xcb, 0x48, 0x2c, 0xce, 0xc8, 0x4d, 0x2c, 0xc8, 0xcc,
	0x4b, 0xd6, 0x2b, 0x49, 0xcd, 0x2d, 0x28, 0x2d, 0xd6, 0x4b, 0x4d, 0x81, 0xa9, 0x50, 0x52, 0xe5,
	0xe2, 0x47, 0xd3, 0x2a, 0x24, 0xc4, 0xc5, 0x92, 0x55, 0x9c, 0x9f, 0x27, 0xc1, 0xa8, 0xc0, 0xa8,
	0xc1, 0x19, 0x04, 0x66, 0x3b, 0x29, 0x45, 0x81, 0x8c, 0xd2, 0x47, 0x18, 0xa5, 0x0f, 0x31, 0x4a,
	0x1f, 0x64, 0x94, 0x3e, 0xd8, 0xa8, 0x24, 0x36, 0x30, 0x65, 0x0c, 0x08, 0x00, 0x00, 0xff, 0xff,
	0xbb, 0x0c, 0xa3, 0x87, 0x8c, 0x00, 0x00, 0x00,
}
