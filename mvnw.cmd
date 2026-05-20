@echo off
set "MAVEN_WRAPPER_JAR=%cd%\.mvn\wrapper\maven-wrapper.jar"
if not exist "%MAVEN_WRAPPER_JAR%" (
    echo Maven Wrapper JAR not found: %MAVEN_WRAPPER_JAR%
    exit /b 1
)
set "MVNW_USER_HOME=%cd%\.mvn\wrapper\dists"
java -jar "%MAVEN_WRAPPER_JAR%" %*
