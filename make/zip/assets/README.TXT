*************************************************
***  Generation of iap.jar from source code   ***
*************************************************

make - project:

Start 'createfilelist.bat' from within Eclipse (by double-clicking) or 
'createfilelist.cmd' from the terminal (Linux/Mac).

IAP - project:

Then right-click 'createReleaseQuick.xml' and choose 'Run As' > 'Ant Build'.
The binary code file 'iap.jar' will be created and placed in the sub-folder 'release'.

For execution, place the iap.jar file and the startup-scripts in a single folder.


*************************************************
***  Installing and starting IAP under Linux  ***
*************************************************

type in shell:
$: unzip iap.zip
$: chmod +x startIAP.sh
$: startIAP.sh

If you need more memory edit the start parameters within "startIAP.sh".

***************************************************
***  Installing and starting IAP under Windows  ***
***************************************************

unzip iap.zip by double-clicking
start startIAP.cmd by double-clicking

If you need more memory edit the start parameters within "startIAP.cmd".


**********************************************************
***  Starting IAP under Windows within Remote Desktop  ***
**********************************************************

If you start IAP from within the Windows Remote Desktop, in some situations
it may happen, that the GUI of IAP is not loading and the application window
is unresponsive, without drawing the GUI content. The exact reason for that
is currently unknown, but it seems to be related to a situation, when
another user is logged in at also uses Java in his session. 

To prevent this GUI freeze in the remote desktop session, add the following 
parameter to the Java command line (e.g. before the -Xmx parameter): 

 -Dsun.java2d.noddraw=true
 