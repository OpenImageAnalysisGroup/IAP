@echo off
echo **********************************************************
echo *       IAP grid execution with auto-updating client     *
echo **********************************************************
cd /d %~dp0
echo Current path:
cd
:download
echo Current client will be downloaded from http://ba-13.ipk-gatersleben.de/iap.jar
java -cp iap.jar iap.Download http://ba-13.ipk-gatersleben.de/iap.jar iap2.jar
IF %ERRORLEVEL% NEQ 0 GOTO downloaderror 
echo Move downloaded jar into place ...
move /Y iap2.jar iap.jar
IF %ERRORLEVEL% NEQ 0 GOTO downloaderror 
echo JAR Release Info:
dir iap.jar
echo One moment, start grid job execution ...
:start
java -Xmx24g -jar iap.jar close
echo Grid client closed
IF %ERRORLEVEL% NEQ 0 GOTO morework 
echo Return value indicates no more work needs to be done. Waiting 1 minute for restart.
PING 1.1.1.1 -n 1 -w 60000 >NUL
GOTO download
:morework
echo Return value indicates more work needs to be done. Start processing next job in 10 seconds ...
PING 1.1.1.1 -n 1 -w 10000 >NUL
rem GOTO start
GOTO download
:downloaderror
echo Return value indicates that the current release could not be downloaded. Waiting 10 minutes for next try.
PING 1.1.1.1 -n 10 -w 60000 >NUL
GOTO download
