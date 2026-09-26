# Build stage
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first for better caching
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM tomcat:9.0.86-jre11
# Remove default apps
RUN rm -rf /usr/local/tomcat/webapps/*
# Copy the built WAR file as ROOT.war so it serves at /
COPY --from=build /app/target/surakshascan-backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
