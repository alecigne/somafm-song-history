FROM eclipse-temurin:17-jre-alpine
COPY target/somafm-song-history-with-dependencies.jar ./
COPY src/main/resources/application.conf ./
ENTRYPOINT ["java", "-Dfile.encoding=utf-8", "-Dconfig.file=/application.conf", "-jar", "/somafm-song-history-with-dependencies.jar"]
