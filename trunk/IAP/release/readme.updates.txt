See end of file for upgrade procedure.

V1.0.3 (August 24, 2013)
--------------------------------------------------------------------------------
* Fix (change 11):
  Corrected the problem, that two of the same analysis-submit buttons where 
  shown, in case two mongodb databases have been configured. Now only one
  button is shown.
* Fix (change 10):
  There was a performance degradation/error (new in V1.0.2) when moving the 
  mouse over the image display. When using the 'Show Image' context 
  menu command in the image grid view and the Analysis test-command.
* Fix (change 9):
  When using the 'Load > DB-DB-Imp.-Exp. Dataset' command, the meta data 
  file was not allowed to be open at the same time in Excel. IAP now handles
  this situation without an error. In case of other loading errors a error 
  message is shown. 
* Fix (change 8):
  When saving experiments or performing an analysis on local disc, the special
  characters '[', ':' and ']' caused problems. These characters, if included 
  in the experiment name, are handled now correctly.  
* New (change 7):
  Added hints on next workflow steps, after clicking the 'Load > DB-Imp.-Exp. 
  Dataset' command.
* New (change 6):
  Update output counter, when saving analysis output to local disk. Status 
  is shown during analysis.   
  
V1.0.2b (August 12, 2013)
--------------------------------------------------------------------------------
* Fix (change 5):
  Experiment header information about used storage space when saving to disk 
  (VFS) has been corrected
* New (change 4):
  Reduced memory usage during loading of data sets from MongoDB databases and
  from VFS
* New (change 3):
  Reduced memory usage during data analysis


V1.0.1 (July 26, 2013)
--------------------------------------------------------------------------------
* Fix (change 2):
  A problem has been fixed, which prevented the proper analysis of data sets, 
  loaded from a temporary VFS location (e.g. using the data set-loading command, 
  to access a local file system folder).
* New (change 1):
  Analysis blocks, used for Barley and Arabidopsis analysis have been moved to 
  their proper package locations (similar to the Maize analysis blocks).


Upgrade procedure
--------------------------------------------------------------------------------
1. Unzip the downloaded iap.zip file and use the startup-scripts to start IAP.
   Modify the memory settings (-Xmx7g) within the windows or linux/mac start-
   script to fit your system configuration (e.g. system memory minus one or 
   two gigabytes).

2. Confirm the proper update, by clicking "About". The first text box should
   include the text "(V1.0.3)" below the program name.
   
  
New installation
--------------------------------------------------------------------------------
Perform step 1 of the upgrade procedure documentation. 


In case of problems/errors:
--------------------------------------------------------------------------------
Within IAP click "Settings > Show Config-File". Close IAP. Move one directory
up and delete or rename the IAP settings folder ("IAP" or ".iap").