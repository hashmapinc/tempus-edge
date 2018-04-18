FROM iofog/python

RUN pip install paho-mqtt && pip install jsonschema && pip install tempus.edge.proto

WORKDIR /src
COPY *.py ./

CMD ["python", "-u", "index.py"]
