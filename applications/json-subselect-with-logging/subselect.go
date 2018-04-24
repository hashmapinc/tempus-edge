package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
	"sync"

	sdk "github.com/iofog/container-sdk-go"
)

var (
	logger              = log.New(os.Stderr, "", log.LstdFlags)
	containerConfig     map[string]interface{}
	configMutex         = sync.RWMutex{}
	client, clientError = sdk.NewDefaultIoFogClient()
)

type config struct {
	Selections []selection `json:"selections"`
}

type selection struct {
	InputType    string   `json:"inputtype"`
	InputFormat  string   `json:"inputformat"`
	OutputType   string   `json:"outputtype"`
	OutputFormat string   `json:"outputformat"`
	Outputs      []output `json:"outputs"`
}

type output struct {
	SubSelection    string `json:"subselection"`
	OutputJSONArray bool   `json:"outputjsonarray"`
	FieldName       string `json:"fieldname"`
}

func main() {
	logger.Println("Starting appliction...")
	if clientError != nil {
		logger.Println(clientError.Error())
		return
	}

	logger.Println("Updating configs...")
	updateConfig()

	go func() {
		confChannel := client.EstablishControlWsConnection(0)
		for {
			select {
			case <-confChannel:
				logger.Println("Received new config!")
				updateConfig()
			}
		}
	}()

	messageChannel, receiptChannel := client.EstablishMessageWsConnection(0, 0)
	for {
		select {
		case msg := <-messageChannel:
			logger.Println("Received new data message:", msg.ContentData)
			go func() {
				selected, err := buildMessage(msg)
				if err != nil {
					logger.Println(err.Error())
				} else {
					client.SendMessageViaSocket(selected)
				}
			}()
		case rcpt := <-receiptChannel:
			logger.Println("Received new receipt:", rcpt.ID, rcpt.Timestamp)
		}
	}

}

func updateConfig() {
	attemptLimit := 5
	var config map[string]interface{}
	var err error

	for config, err = client.GetConfig(); err != nil && attemptLimit > 0; attemptLimit-- {
		logger.Println(err.Error())
	}

	if attemptLimit == 0 {
		logger.Println("Update config failed")
		return
	}

	configMutex.Lock()
	containerConfig = config
	configMutex.Unlock()
}

func buildMessage(msg *sdk.IoMessage) (*sdk.IoMessage, error) {
	logger.Println("Building subselected message...")
	var newMsg *sdk.IoMessage
	config := new(config)
	configMutex.RLock()
	configBytes, err := json.Marshal(containerConfig)
	configMutex.RUnlock()

	if err != nil {
		return nil, err
	} else if err = json.Unmarshal(configBytes, config); err != nil {
		return nil, err
	}

	for _, selection := range config.Selections {
		if msg.InfoType == selection.InputType && msg.InfoFormat == selection.InputFormat {
			newContentData, err := transformContentData(msg.ContentData, selection.Outputs)
			if err != nil {
				return nil, err
			}
			newMsg = &sdk.IoMessage{
				InfoType:    selection.OutputType,
				InfoFormat:  selection.OutputFormat,
				ContentData: newContentData,
			}
		}
	}

	if newMsg == nil {
		return nil, errors.New("No matched selections for input message found")
	}
	return newMsg, nil
}

func transformContentData(contentData []byte, outputs []output) (result []byte, e error) {
	oldJsonContentData := make(map[string]interface{})
	newJsonContentData := make(map[string]interface{})
	var curValue interface{}
	var present bool
	err := json.Unmarshal(contentData, &oldJsonContentData)
	if err != nil {
		return nil, err
	}
	defer func() {
		if r := recover(); r != nil {
			e = errors.New(fmt.Sprintf("Panic while subselecting occurred: %v", r))
		}
	}()
	for _, output := range outputs {
		path := strings.Split(output.SubSelection, ".")
		curValue = oldJsonContentData
		for _, p := range path {
			if curValueAsMap, ok := curValue.(map[string]interface{}); ok {
				if curValue, present = curValueAsMap[p]; !present {
					curValue = nil
					break
				}
			} else if curValueAsArray, ok := curValue.([]interface{}); ok {
				indx, err := strconv.ParseInt(p, 10, 0)
				if err != nil {
					return nil, err
				}
				curValue = curValueAsArray[indx]
			}
		}
		if output.OutputJSONArray {
			newJsonContentData[output.FieldName] = []interface{}{curValue}
		} else {
			newJsonContentData[output.FieldName] = curValue
		}
	}
	return json.Marshal(newJsonContentData)
}
