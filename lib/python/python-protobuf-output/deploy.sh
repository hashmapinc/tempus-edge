#!/bin/bash

rm -rf ./dist ./build
python3 setup.py bdist_wheel --universal
twine upload dist/*