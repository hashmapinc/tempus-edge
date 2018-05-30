#!/bin/bash

# remember project home dir
PROJECT_HOME=$(shell pwd)

# go to the applications and package them
cd applications
make

# go back to the project home
cd $(PROJECT_HOME)
