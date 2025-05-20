FROM amazoncorretto

COPY target/*.jar .
EXPOSE 8080

CMD java -jar aws-s3-notify-0.0.1-SNAPSHOT.jar