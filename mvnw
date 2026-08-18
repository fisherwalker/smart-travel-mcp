#!/bin/sh
# Maven Wrapper script
# Downloads and runs Maven

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVA_EXEC="$JAVA_HOME/bin/java"
else
    JAVA_EXEC="java"
fi

# Setup maven wrapper
MAVEN_PROJECTBASEDIR=$(cd "$(dirname "$0")" && pwd)
MAVEN_OPTS="${MAVEN_OPTS} -Dfile.encoding=UTF-8"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Run the wrapper
exec "$JAVA_EXEC" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
