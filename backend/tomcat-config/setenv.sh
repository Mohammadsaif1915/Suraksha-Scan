#!/bin/sh
# setenv.sh — sourced automatically by catalina.sh before Tomcat starts.
# Passes Render's $PORT (or falls back to 8080) into Tomcat as a Java system property
# so that server.xml can reference it as ${PORT}.
export JAVA_OPTS="${JAVA_OPTS} -DPORT=${PORT:-8080}"
