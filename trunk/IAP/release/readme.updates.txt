V1.0.1 (July 26, 2013):

* A problem has been fixed, which prevented the proper analysis of datasets, 
  loaded from a temporary VFS location (e.g. using the dataset-loading command, 
  to access a local file system folder).
* Analysis blocks, used for Barley and Arabidopsis analysis have been moved to 
  their proper package locations (similar to the Maize analysis blocks).

Upgrade procedure:

1. Within IAP click "Settings > Show Config-File". Close IAP. Then delete 
   previously saved or automatically generated pipeline definitions
   (file names ending with .pipeline.ini).

2. Unzip the downloaded iap.zip file and use the startup-scripts to start IAP.
   Modify the memory settings (-Xmx7g) within the windows or linux/mac start-
   script to fit your system configuration (e.g. system memory minus one or 
   two gigabytes).

3. Confirm the proper update, by clicking "About", the first text box should
   include the text "(V1.0.1)" below the program name.
   
  
New installation:

Perform step 2 and 3 of the upgrade procedure documentation. 

In case of problems/errors:

Within IAP click "Settings > Show Config-File". Close IAP. Move one directory
up and delete or rename the IAP settings folder ("IAP" or ".iap").