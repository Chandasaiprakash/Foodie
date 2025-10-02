@echo off
echo Starting Infrastructure Services...

:: Start MySQL
net start MySQL80

:: Start MongoDB
net start MongoDB

:: Start ZooKeeper
start cmd /k "cd C:\kafka && bin\windows\zookeeper-server-start.bat config\zookeeper.properties"

:: Start Kafka
start cmd /k "cd C:\kafka && bin\windows\kafka-server-start.bat config\server.properties"

echo All infra services are starting...
pause
