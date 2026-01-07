@echo off
chcp 65001 >nul
cd /d "%~dp0src"
echo Derleniyor...
javac -encoding UTF-8 -cp ".;postgresql-42.7.4.jar" *.java
if %errorlevel% neq 0 (
    echo Derleme hatasi!
    pause
    exit /b
)
echo Calistiriliyor...
java -Dfile.encoding=UTF-8 -cp ".;postgresql-42.7.4.jar" Main
pause
