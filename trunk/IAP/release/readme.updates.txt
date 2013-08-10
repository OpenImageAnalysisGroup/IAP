V1.0.2 (August 10, 2013)
--------------------------------------------------------------------------------
* Experiment header information about used storage space when saving to disk 
  (VFS) has been corrected
* Reduced memory usage during loading of data sets from MongoDB databases
* Reduced memory usage during data analysis


V1.0.1 (July 26, 2013)
--------------------------------------------------------------------------------
* A problem has been fixed, which prevented the proper analysis of data sets, 
  loaded from a temporary VFS location (e.g. using the data set-loading command, 
  to access a local file system folder).
* Analysis blocks, used for Barley and Arabidopsis analysis have been moved to 
  their proper package locations (similar to the Maize analysis blocks).


Upgrade procedure
--------------------------------------------------------------------------------
1. Unzip the downloaded iap.zip file and use the startup-scripts to start IAP.
   Modify the memory settings (-Xmx7g) within the windows or linux/mac start-
   script to fit your system configuration (e.g. system memory minus one or 
   two gigabytes).

2. Confirm the proper update, by clicking "About". The first text box should
   include the text "(V1.0.2)" below the program name.
   
  
New installation
--------------------------------------------------------------------------------
Perform step 1 of the upgrade procedure documentation. 


In case of problems/errors:
--------------------------------------------------------------------------------
Within IAP click "Settings > Show Config-File". Close IAP. Move one directory
up and delete or rename the IAP settings folder ("IAP" or ".iap").