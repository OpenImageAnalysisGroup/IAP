V1.0.3 (September 5, 2013)
==========================
- Fix for situations where the pipeline name contained non alphanumeric characters and image analysis
  was performed on VFS locations.
- Handle data set loading situations where the meta data file is concurrently open in Excel.
- Fix performance degration (introduced in V1.0.2), where moving the mouse over opened images was slower
  than expected or caused errors in some situations.
- Open file storage location when using the CSV export command.
- Improved output progress information during analysis.
- Experiment header information can be directly be updated for experiments, stored in VFS locations.
- Some more minor changes, detailed in the readme.updates.txt file, contained in the IAP.zip download.

V1.0.2 (August 13, 2013)
========================
- Experiment header information about used storage space when saving to disk 
  (VFS) has been corrected
- Reduced memory usage during loading of data sets and during data analysis

V1.0.1 (July 26, 2013)
======================

- A problem has been fixed, which prevented the proper analysis of datasets, 
  loaded from a temporary VFS location (e.g. using the dataset-loading command, 
  to access a local file system folder).
- Analysis blocks, used for Barley and Arabidopsis analysis have been moved to 
  their proper package locations (similar to the Maize analysis blocks).

Upgrade procedure
-----------------

1. Unzip the downloaded iap.zip file and use the startup-scripts to start IAP.
   Modify the memory settings (-Xmx7g) within the windows or linux/mac start-
   script to fit your system configuration (e.g. system memory minus one or 
   two gigabytes).

2. Confirm the proper update, by clicking "About". The first text box should
   include the text "(V1.0.2)" below the program name.
   
  
New installation
----------------

Perform step 1 of the upgrade procedure documentation. Consult PDF documentation
for further details.

In case of problems/errors
--------------------------

Within IAP click "Settings > Show Config-File". Close IAP. Move one directory
up and delete or rename the IAP settings folder ("IAP" or ".iap").