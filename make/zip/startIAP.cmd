@echo off
cd /d %~dp0
echo About to start IAP with 7g memory utilization...
java -Xmx7g -cp iap.jar de.ipk.ag_ba.gui.webstart.IAPmain 
echo.
echo IAP execution has finished.
pause