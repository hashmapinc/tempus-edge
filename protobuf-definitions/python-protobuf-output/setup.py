from setuptools import setup, find_packages
setup(
    name="tempus.edge.proto",
    version="0.0.11dev",
    packages=find_packages(),
    license="Apache License 2.0",
    author="Randy Pitcher",
    author_email="randy.pitcher@hashmapinc.com",
    description='Tempus Edge protobuf definitions compiled into a python package.',
    long_description=open('../README.md').read(),
    keywords="hashmap hashmapinc tempus edge iot protobuf",
    url="https://github.com/hashmapinc/tempus-edge",
    install_requires=['protobuf'],
)
