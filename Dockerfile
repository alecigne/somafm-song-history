FROM openjdk:17-alpine
COPY target/somafm-song-history-0.2.0-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
