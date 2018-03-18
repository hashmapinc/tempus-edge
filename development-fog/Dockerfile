#==============================================================================
# Dockerfile for creating an iofog development fog instance
#
# Linux base + java install + iofog install + docker install + iofog starting
#
# You will still need to provision the fog yourselfs
#==============================================================================
# start from ubuntu base
FROM ubuntu

# install java, docker, iofog
RUN apt-get update \
    && apt-get install -y \
      curl \
      sudo \
      default-jdk \
    && curl -fsSL https://get.docker.com/ | sh \
    && curl -s https://packagecloud.io/install/repositories/iotracks/iofog/script.deb.sh | sudo bash \
    && apt-get install -y \
      iofog \
      vim \
      net-tools \
    && apt-get clean

# need this to allow internal docker daemon to create containers
VOLUME /var/lib/docker

# do some general iofog config and start the service
COPY entrypoint/ /entrypoint
ENTRYPOINT ["sh", "/entrypoint/entrypoint.sh"]

# start the shell
CMD ["/bin/bash"] 
#==============================================================================