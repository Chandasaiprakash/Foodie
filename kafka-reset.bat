@echo off
echo ==========================================
echo  Resetting ZooKeeper and Kafka
echo ==========================================

:: --- STOP KAFKA & ZOOKEEPER (force kill) ---
echo Stopping Kafka and ZooKeeper...
taskkill /F /IM java.exe >nul 2>&1

:: --- CLEAN ZOOKEEPER DATA ---
echo Cleaning ZooKeeper data directory...
rmdir /s /q C:\zookeeper

:: --- CLEAN KAFKA LOGS ---
echo Cleaning Kafka logs directory...
rmdir /s /q C:\kafka-logs

:: --- START ZOOKEEPER ---
echo Starting ZooKeeper...
start cmd /k "cd C:\kafka && .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties"

:: Give ZooKeeper some time to boot
timeout /t 5 /nobreak >nul

:: --- START KAFKA BROKER ---
echo Starting Kafka Broker...
start cmd /k "cd C:\kafka && .\bin\windows\kafka-server-start.bat .\config\server.properties"

echo ==========================================
echo Kafka & ZooKeeper restarted successfully!
echo ==========================================
pause
