@echo off
set JAVA_OPTS=-Xms128m -Xmx512m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=128m
java %JAVA_OPTS% -jar bili-admin/target/bili-admin-1.0.jar
pause