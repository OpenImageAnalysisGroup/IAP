V1.0.3 (August 24, 2013)
--------------------------------------------------------------------------------
* Corrected the problem, that two of the same analysis-submit buttons where 
  shown, in case two mongodb databases have been configured. Now only one
  button is shown.
* There was a performance degradation when using the 'Show Image' context menu 
  command in the image grid view or when using the Analysis test-command.
* When using the Load, DB-ImportExport command, the metadata file can't be
  opened at the same time in Excel or another program. IAP now shows a warning/
  error message in this situation. Before, data loading was not possible,
  with no error message.
* Added hints on next workflow steps, after clicking the 'Load > DB-Imp.-Exp. 
  Dataset' command.
  
  
V1.0.2b (August 12, 2013)
--------------------------------------------------------------------------------
* Experiment header information about used storage space when saving to disk 
  (VFS) has been corrected
* Reduced memory usage during loading of data sets from MongoDB databases and
  from VFS
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