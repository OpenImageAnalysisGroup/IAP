-- See end of file for help on upgrade procedure --

V2.0.2 (June 25, 2015)
--------------------------------------------------------------------------------
* The calculation of the convex hull length was incorrect. This problem has been
  resolved. Previous calculations of the convex hull length and of the convex
  hull compactness are either incorrect (in about 10% of the cases) or missing
  in the result table row (in about 90% of the cases).  Other values, such as
  convex hull area are not affected by this bug.
* For testing purposes a image resize block was added.
* Datasets can be created from a list of files using the 'Load or Create Dataset'
  > 'Create Dataset from Files' commands.

V2.0.0 (March 17, 2015)
--------------------------------------------------------------------------------
* Culmination of a larger number of improvements, both on usability as on
  feature completeness.
* Uses a different settings folder path (postfix '2.0') in order to separate 
  settings and stored pipelines from previous versions (1.x), as analysis 
  block names and parameter names changed from version 1.1.x to 2.x.
* The program documentation (documentation.pdf) may still reflect in part old
  settings names and analysis block names. In case of difficulty, it is
  recommended to download and use IAP version 1.1.x, as described in the 
  documentation.pdf from November 2013 (see bottom of title page).
  Once you are proficient in working with the system, you may update and use
  version 2.x, which adds new features and is more streamlined. A revision
  of the documentation will be created over time.
* After a long period of working at IPK, developing VANTED, KGML-ED and IAP,
  time has come to finish the current chapter. The overall functionality of 
  these systems is mostly preserved within IAP.

  Sincerly, Dr. Christian Klukas

V1.1.3 (May 5, 2014)
--------------------------------------------------------------------------------
- New Features -
* Block selection dialog lists blocks in alphabetical order

- Bug Fixes -
* Improved compatibility with Add-ons
* Example Add-on (root analysis) updated

V1.1.2 (April 24, 2014)
--------------------------------------------------------------------------------
- New Features -
* The main experiment data file (XML) is stored in compressed form
  (according VFS setting is enabled by default). Loading of compressed
  experiment files is supported. The example result files are now compressed,
  to speed-up the loading when using slow network connections.
* Improved pipeline block editor window. A context menu ('...' button), can
  be used to disable a block, to insert new blocks, or to remove a block
* System Status, Block Execution Statistics command has been added

- Bug Fixes -
* Loading of analysis blocks, defined in Add-ons, did not work in some cases
* Fixed layout problem of initially not visible buttons at the end of
  an command list (if window size was too narrow)
* Seed-Date meta data processing has been corrected

V1.1.1 (November 22, 2013)
--------------------------------------------------------------------------------
- New Features -
* Tested again with example data set
* Support for example Add-on (downloadable)
* Add-on contains working detached leaves analysis functions
* Updated documentation files
* Improved result data tree display
* Leaf-tip-count block newly introduced

- Bug Fixes -
* Various updates, e.g. related several processing blocks
* Morphological operations block correctly uses round mask of variable size
* Calculation of relative growth rates works again

V1.1.0 (November 22, 2013)
--------------------------------------------------------------------------------
- New Features -
- Bug Fixes -
* Various new features and bug fixes (see V1.1.1 for more details)

V1.0.3 (August 24, 2013)
--------------------------------------------------------------------------------
- New Features -
* New (change 16)
  Added directory-chooser button to input fields, which ask for a directory.
  (Useful for local directory input. It is also shown for remote locations,
  case by case decision will be added, later, as it helps in most cases and 
  makes no problem in the remaining cases.)
* New (change 15):
  Experiments, saved in VFS can be put in a virtual Trash. Trash operation and
  undo operation is implemented. Emptying (deleting data) within VFS is not yet
  implemented.
* New (change 14):
  The Experiment-header properties can be updated and saved for experiments
  loaded from VFS.
* New (change 13):
  The 'Save Annotations Change' command has been implemented for VFS storage
  locations.
* New (change 12):
  Added hints on next workflow steps, after clicking the 'Load > DB-Imp.-Exp. 
  Dataset' command.
* New (change 11):
  Update output counter, when saving analysis output to local disk. Status 
  is shown during analysis.   

- Bug Fixes -
* Fix (change 11):
  Test re-analysis by right-clicking in the result image button list and 
  choosing '[Analysis Name] (Reference+Old Reference)' now works also for
  cases where the input data set has no reference images.
* Fix (change 10):
  When clicking View/Export Data, Create CSV File, the output file is located
  and shown in the default file manager.
* Fix (change 9):
  Corrected the problem, that two of the same analysis-submit buttons where 
  shown, in case two mongodb databases have been configured. Now only one
  button is shown.
* Fix (change 8):
  There was a performance degradation/error (new in V1.0.2) when moving the 
  mouse over the image display. When using the 'Show Image' context 
  menu command in the image grid view and the Analysis test-command.
* Fix (change 7):
  When using the 'Load > DB-DB-Imp.-Exp. Dataset' command, the meta data 
  file was not allowed to be open at the same time in Excel. IAP now handles
  this situation without an error. In case of other loading errors a error 
  message is shown. 
* Fix (change 6):
  When saving experiments or performing an analysis on local disc, the special
  characters '[', ':' and ']' caused problems. These characters, if included 
  in the experiment name, are handled now correctly.  
  
V1.0.2b (August 12, 2013)
--------------------------------------------------------------------------------
- New Features -
* New (change 5):
  Reduced memory usage during loading of data sets from MongoDB databases and
  from VFS
* New (change 4):
  Reduced memory usage during data analysis

- Bug Fixes -
* Fix (change 3):
  Experiment header information about used storage space when saving to disk 
  (VFS) has been corrected


V1.0.1 (July 26, 2013)
--------------------------------------------------------------------------------
- New Features -
* New (change 2):
  Analysis blocks, used for Barley and Arabidopsis analysis have been moved to 
  their proper package locations (similar to the Maize analysis blocks).

- Bug Fixes -
* Fix (change 1):
  A problem has been fixed, which prevented the proper analysis of data sets, 
  loaded from a temporary VFS location (e.g. using the data set-loading command, 
  to access a local file system folder).


Upgrade procedure
--------------------------------------------------------------------------------
1. Unzip the downloaded iap.zip file and use the startup-scripts to start IAP.
   Modify the memory settings (-Xmx7g) within the windows or linux/mac start-
   script to fit your system configuration (e.g. system memory minus one or 
   two gigabytes). For Mac/Linux, use the ".sh" script files for starting.
   For Windows, use the ".cmd" script files for starting the program.

2. Confirm the proper update, by clicking "About". The first text box should
   include the text "(V1.0.3)" or a higher number, depending of the
   intended download version, below the program name.
   
  
New installation
--------------------------------------------------------------------------------
Perform step 1 of the upgrade procedure documentation. 


In case of problems/errors:
--------------------------------------------------------------------------------
Within IAP click "Settings > Show Config-File". Close IAP. Move one directory
up and delete or rename the IAP settings folder (starting with "IAP" or ".iap").