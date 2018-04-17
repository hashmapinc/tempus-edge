FROM iofog/python

RUN pip install paho-mqtt
RUN pip install jsonschema

WORKDIR /src
COPY *.py ./

CMD ["python", "-u", "index.py"]
