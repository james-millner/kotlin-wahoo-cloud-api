# Use a Java 17 base image
FROM amazoncorretto:21-alpine-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file to the container
COPY build/libs/kotlin-wahoo-cloud-api-1.0-SNAPSHOT-all.jar /app/app.jar

# Expose the port on which the application will run
EXPOSE 8080

# Set the entry point command for the container
CMD ["java", "-jar", "app.jar"]