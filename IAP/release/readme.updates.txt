-- See end of file for help on upgrade procedure --

V1.1.0 (October 16, 2013)
--------------------------------------------------------------------------------
- Outlook -
* More information on improvements and fixes of V1.1 will be added here, later.
* Future versions will be even more streamlined according to received user-
  feedback (initial feedback has been already incorporated). 
  Further performance improvements are planned.

- Bug Fixes -
* Fix (change 21):
  Bug fixes related to the separation of analysis settings, based on the
  image unit configuration name.

- New Features -
* New (change 20)
  New commands to detect and view sets of experiments with common camera 
  configurations (based on imported image unit configuration names).
  The two button commands are available from the MongoDB > Database Tools
  section. The second command allows copying analysis settings from a previous
  experiment, to experiments which used the same image unit configurations. 
  This way it is very quickly possible to analyze all experiments with a common
  experiment setup, based on a selected experiment analysis pipeline.
* New (change 19)
  Maize default pipeline uses various blocks, which are "auto-tuning" its 
  parameters. The standard pipeline requires thus much less initial 
  parameterization.
* New (change 18)
  Block selection dialog improved (allows selecting analysis blocks using graphical
  GUI).
* New (change 17)
  Add-on manager button command (from IAP > About > Add-on Manager) more easily
  accessible.

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