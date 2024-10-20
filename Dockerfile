FROM openjdk:17-jdk

WORKDIR /app

COPY build/libs/DigitalChiefProject-0.1.jar /app/app-backend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app-backend.jar"]