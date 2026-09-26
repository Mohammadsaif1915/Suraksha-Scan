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
# Disable Tomcat shutdown port to prevent Render health checks from shutting it down
RUN sed -i 's/port="8005"/port="-1"/g' /usr/local/tomcat/conf/server.xml

EXPOSE 8080
# Dynamically assign Render's $PORT to Tomcat's HTTP connector at runtime
CMD ["/bin/sh", "-c", "sed -i 's/port=\"8080\"/port=\"'${PORT:-8080}'\"/g' /usr/local/tomcat/conf/server.xml && exec catalina.sh run"]
