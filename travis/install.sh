#!/bin/bash

# remember project home dir
PROJECT_HOME=$(shell pwd)

# go to the applications and deploy them
cd applications
make deploy

# go back to the project home
cd $(PROJECT_HOME)
