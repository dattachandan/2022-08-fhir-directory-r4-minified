FROM openjdk:8-jdk-alpine
COPY target/fhir-directory-r4.jar .
ADD conf conf
COPY target/docker/conf/application.properties .
COPY target/docker/conf/logback.xml conf
COPY conf/UserPermissions.xml conf

ENTRYPOINT ["java", "-Djava.rmi.server.hostname=0.0.0.0", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=9000", "-Dcom.sun.management.jmxremote.rmi.port=9000", "-Dcom.sun.management.jmxremote.local.only=false", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-jar", "fhir-directory-r4.jar", "--logging.config=conf/logback.xml"]