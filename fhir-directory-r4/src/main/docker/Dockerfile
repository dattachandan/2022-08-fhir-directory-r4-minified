FROM cd-harbor-dev.cd.hlk/oracle/servercore-v1809-serverjre8:202

ADD fhir-directory-r4/target/docker-ready/start.bat C:/healthlink/app/start.bat
ADD fhir-directory-r4/target/@project.build.finalName@.jar C:/healthlink/app/@project.artifactId@.jar
RUN mkdir c:\healthlink\app\logs
RUN mkdir c:\healthlink\app\conf
ENTRYPOINT [ "c:/healthlink/app/start.bat" ]
