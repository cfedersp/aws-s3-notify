FROM amazoncorretto:17-alpine-jdk

COPY target/*.jar .
EXPOSE 8080

CMD java -jar aws-s3-notify-1.6.jar
