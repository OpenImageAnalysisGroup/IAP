@echo off
echo **********************************************************
echo *       IAP grid execution with auto-updating client     *
echo **********************************************************
cd /d %~dp0
echo >Current path:
cd
:download
echo >Current client will be downloaded from http://ba-13.ipk-gatersleben.de/iap.jar
echo java -Xmx1g -cp iap.jar iap.download http://ba-13.ipk-gatersleben.de/iap.jar
IF %ERRORLEVEL% NEQ 0 GOTO downloaderror 
echo >JAR Release Info:
dir iap.jar
echo >One moment, start grid job execution ...
:start
java -Xmx12g -jar iap.jar close
echo >Grid client closed
IF %ERRORLEVEL% NEQ 0 GOTO morework 
echo >Return value indicates no more work needs to be done. Waiting 5 minutes for restart.
PING 1.1.1.1 -n 5 -w 60000 2>NUL
GOTO download
:morework
echo >Return value indicates more work needs to be done. Start processing next job. One moment ...
PING 1.1.1.1 -n 1 -w 10000 2>NUL
GOTO start
:downloaderror
echo >Return value indicates that the current release could not be downloaded. Waiting 10 minutes for next try.
PING 1.1.1.1 -n 10 -w 60000 2>NUL
GOTO download
