# ── Build stage ──────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first for better layer caching
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM tomcat:9.0.86-jre11

# Remove default Tomcat sample apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR as ROOT.war (serves at context path /)
COPY --from=build /app/target/surakshascan-backend-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# ── Tomcat configuration ──────────────────────────────────────────────────────
# Replace server.xml with our own:
#   - Shutdown port is -1  (disabled completely)
#   - HTTP Connector port uses ${PORT} Java system property (set by setenv.sh)
COPY tomcat-config/server.xml /usr/local/tomcat/conf/server.xml

# setenv.sh is sourced by catalina.sh automatically before startup.
# It exports JAVA_OPTS with -DPORT=${PORT:-8080} so ${PORT} resolves in server.xml.
COPY tomcat-config/setenv.sh /usr/local/tomcat/bin/setenv.sh
RUN chmod +x /usr/local/tomcat/bin/setenv.sh

# Render dynamically assigns a port — EXPOSE is informational only
EXPOSE 8080

# Start Tomcat in the foreground (exec form ensures PID 1 = Java)
CMD ["catalina.sh", "run"]
