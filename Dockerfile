FROM java:8-jdk-alpine
WORKDIR /opt/docker
ADD --chown=root:root target/docker/stage/opt /opt
USER root
ENTRYPOINT ["bin/helloworld"]
CMD []
