@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-23
set MYSQL_CMD="C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe"
set MVN_CMD="%~dp0apache-maven-3.9.6\bin\mvn.cmd"

echo Starting database setup...
%MYSQL_CMD% -u root -e "CREATE DATABASE IF NOT EXISTS surakshascan;"
%MYSQL_CMD% -u root surakshascan < database\schema.sql
%MYSQL_CMD% -u root surakshascan < database\scam_patterns_seed.sql
%MYSQL_CMD% -u root surakshascan < database\scam_patterns_seed_extended.sql
echo Database setup complete.

echo Building backend...
cd backend
call %MVN_CMD% clean package
cd ..
echo Backend build complete.

echo Deploying files to Tomcat...
copy backend\target\surakshascan-backend-1.0-SNAPSHOT.war apache-tomcat-9.0.86\webapps\surakshascan-backend.war
xcopy frontend\* apache-tomcat-9.0.86\webapps\ROOT\ /E /H /C /I /Y
echo Deployment complete.

echo Starting Tomcat...
cd apache-tomcat-9.0.86\bin
call startup.bat
echo Setup and startup complete!
