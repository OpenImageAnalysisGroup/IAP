## IAP - The Integrated Analysis Platform for high-throughput plant image analysis

### Docker-Integration

For testing purposes automated builds, based on the current GitHub conntent are automatically performed. The basis for this is the available docker/github integration. The provided 'Dockerfile' shows how to download and compile the current source code, and how to create a new 'fat' iap-jar file. An example IAP commandline image is available here: [https://hub.docker.com/r/klukas/iap/](https://hub.docker.com/r/klukas/iap/). Use the command 'docker pull klukas/iap' to download the container. Enter 'docker run --rm --name IAPconsole -it klukas/iap' to start IAP in commandline mode. At the moment only limited functionality is available using this execution mode and due to the container isolation. Call options for functional use are in work and will be available in the future. Also, for now these commands work best under the Linux environment, but the Docker beta developments will very soon make it possible to execute docker container apps on Windows and Mac in the same way.

### Setup of Development Environment

The following procedure is suggested for setting up the development environment. First make sure that you have already installed the latest stable versions of Java and Eclipse. Then you can load the IAP source code from GitHub. 

#### Download/Info-Links

**Java Software Development Kit (SDK)**

[Install newest version of JDK.](http://www.oracle.com/technetwork/java/javase/overview/index.html)

**Eclipse Development Environment**

[Install newest version of Eclipse.](https://eclipse.org/)

**IAP Source Code on GitHub**

[Source Code Link](http://cklukas.github.io/IAP/)

#### GitHub Setup in Eclipse

To check out the project in Eclipse a GitHub interface is needed. If you have not yet installed an interface follow the next steps, otherwise go to the next section.

**[the following information ist at the moment outdated, as it describes the SVN plugin installation]**

#### Project import into Eclipse

After finishing the installation make sure that you create a separate and clean Eclipse workspace. Then follow the next steps to import the project and configure the workspace:
First change the Text file encoding to UTF-8 (select Window > Preferences > General > Workspace, choose other and select UTF-8, then click OK)
Next, switch to the GIT Repository Exploring view and add the Git repository URL.
After connecting to the Git repository, all projects should be pulled and initialized correctly within Eclipse.
Now switch to the Preferences menu again and go to Java > Code Style > Formatter and import the VANTED_SAVE_ACTION_FORMAT, you can find it under your workspace > make > save_action_format.xml.
Then (also in the Preferences menu) go to Java > Editor > Save Actions and enable all options.
After that, switch to the Java Perspective and navigate into the make folder.
Depending on your operating system execute the createfilelist, if you use windows just double click on the .bat file, for Unix or mac execute the .cmd file in the shell.
Now, you have to configure the Run configuration (click on the arrow beside the run icon), choose a name for the configuration, choose the IAP folder by clicking on the Browse button and then search for the main class.
Finally, switch to the Arguments tab and free up some additional main memory by adding -Xmx20g (in this example 20 GB, size of the allocated memory depends on your system, you should left some reserves for your operating system) to the VM arguments, then click Run. Congrats, the IAP should start!
Normally, there should be no errors in the Problems view, warnings can be ignored. If you have further questions, please write a mail.

*Happy coding!*

*Dr. Christian Klukas*

### Short introduction into IAP extension development

IAP can be customized by the end-user with new data storage locations, accessible via FTP, SFTP, direct file system access (e.g. a local folder or mounted network drive), HTTP and SMB file share protocol. Pipeline blocks may be arranged in templates, blocks can be removed, added and the block settings, which modify the behavior of a analysis block, may easily be changed. Resulting custom templates can be exported, applied to other experiments and shared to other users. The end-user documentation (link) contains detailed infos on how to achieve these tasks. To add new segmentation, analysis functions or custom user commands, e.g. for export of data in a certain file format or for custom post-processing, new source code can be developed and easily integrated into IAP using the supplied extension API. The extension API is defined in the interface IAPplugin. This file defines all possible extension points for a user-defined IAP-Plugin.

### Development of custom Analysis-Pipelines

If you plan to develop and use custom analysis pipelines, including new Analysis blocks, it may be a good idea not to customize the analysis templates from the "Setting>[Template]" editor. But to define the content of the template inside a custom Plugin, which will define the pipeline content and the custom analysis pipeline blocks. IAP already contains a plugin, for the definition of the Maize, Barley and Arabidopsis pipelines. The class PluginIAPanalyisTemplates forms a IAP plugin, which extends the system with 3 new analysis pipelines. Each pipeline definition is defined as an object implementing the interface AnalysisPipelineTemplate. A custom pipeline needs to implement the methods defined in the interface. For example the name of the pipeline and the list of blocks needs to be defined, as shown in the supplied maize analysis pipeline class: MaizePipeline. To let IAP load the new plugin, a XML plugin description file (example) needs to be created as described in the section "Adding new plugins to IAP" (below).

### Development of new Analysis-Pipelines-Blocks

IAP contains a number of pre-configured analysis blocks, which are used for the definition of the provided analysis pipelines. In order to utilize new and advanced image segmentation techniques of to extract new traits, it is possible to quite easily extend the system with your custom analysis code. Such new Analysis block needs to implement the basis interface ImageAnalysisBlock.

#### Processing a image from a certain camera types

As not every analysis block needs to customize all aspects of such block, and may not need to process all input images from all possible camera types, it is a good idea to base a new block on the provided abstract class AbstractBlock. If you base your custom block on this task, it is only required to implement the method "Image processMask(Image mask) { ... }". This method is called by IAP once the previous piepline blocks have been executed, and the given mask-Image may be interpreted or modified by your code and then returned as a result. The camera type of the given image may be checked using the method "getCameraType()". To process the given image, you may access some build-in processing functions, by calling the method "io()", which returnes the so-called ImageOperation-object. Using this object you may easily invoke a list of standard-commands such as "rotate", "resize", "sharpen", "removeSmallElements", and many more. Each method in this class returns a new ImageOperation object, also containing the processed image result. This way, you can easily connect multiple processing calls, and finally call "getImage()", which can be returned in the analysis block. The Image class as well as the ImageOperation class contain the method "getAsImgePlus()", which returns the image as a ImageJ image object. As ImageJ is embedded into the application you may this way easily use methods or approaches, applicable to ImageJ within IAP.

#### Processing images from different camera types at the same time

If you would like to process or overlay images from different camera types (from a single snapshot), you should not base your image analysis block on abstract class AbstractBlock, but instead on the abstract class AbstractSnapshotAnalysisBlock. This base class helps you developing image analysis blocks, which may process images from all available camera types in a single step. For that you may implement and override the method "prepare()". Call "input().images()" or "input().masks()" to access main images or processed images within this method. You may also individually process the images of a certain camera type, by implementing the methods "processVISmask", processFLUOmask", ....

#### End-User Customization (Settings)

If you base your custom analysis block on any of the provided abstract classes, and not on the raw interface, you have access to automatically created settings. You may call the methods "int getInt(String setting, int defaultValue)", "int getDouble(String setting, double defaultValue)", "int getBoolean(String setting, boolean defaultValue)", to very easily make your code dynamic, according to user-provided changes to these settings. You need to specifiy default values for a particular settings, and the settings title. E.g. you could provide the user with the possibility to ommit a certain processing step by adding the following to your code: "if (getBoolean("blur image", true)) { ... }". By default the code will be executed, but if the user changes the newly appearing setting "blur image", the code will not be executed. You are free to name your settings as you will, but you should not use special non-ascii characters or other special formatting characters. After the first execution of a certain pipeline, the pipeline will be extended and include settings (accessible to the user as described in the end-user documentation), which may be easily changed to the particular situation. All settings are categorized according the the name of the analysis block. If you include your analysis block several times in a pipeline, all blocks will share the same settings. IAP also automatically creates separate settings for side- and top-images. In addition, the user may modify the behavior so that different settings are used for early or late time points or for different camera configurations. In all of these cases initially your defined default values are used.

#### Creating the plugin for the definition of the new analysis blocks

You need to extend your plugin definition class (implementing IAPplugin or better extending the abstract class AbstractIAPplugin) and override the method "ImageAnalysisBlock[] getImageAnalysisBlocks(). Also, the new plugin needs to be added to the list of known plugins as described in the section "dding new plugins to IAP" (below).

### Development of new Command Buttons

You may add custom or new command buttons at several places inside IAP, e.g. at the Start-screen, as a command button, visible once an experiment has been opened, or within the Tools-section of a particular experiment. A detailed description on how to implement and add such new command buttons will be added to this website in the future. For now it may be best to check the existing source code and if unclear points exist, to ask the developers of IAP for help.

### Adding new plugins to IAP

Once a new Plugin has been defined, IAP will only load it, if is known to the system. IAP uses a list of text files (plugin1.txt, plugin2.txt, plugins3.txt, plugins4.txt and pluginsIAP.txt). These text files need initially be created, before the first run of the application. And these text files need to be updated, once a new plugin has been defined. The scripts "createfilelist.bat" (Windows) and "createfilelist.cmd" (Linux/Mac), will create or update these text files. Each line is these text file contains the link (relative path and filename) to a Plugin description file in XML form. These plugin files contain information about the defined plugin. It is probably best to copy and adapt a existing plugin description file and modify its content (most importantly the class name of the plugin). The content of the xml file is self-evident. For example the analysis pipeline plugin description file looks like this: pluginIAPanalysisTemplates.xml.

### Development of Add-ons

As IAP uses VANTED as one of its core libraries, custom plugins may be packaged into a single Jar file, which can easily be distributed and loaded by the end-users. Such Add-ons may customize the behavior of the included VANTED functionality and also of the new image analysis processing commands. The list of Add-ons (tab "Add-ons" on this page) gives an impression on the possibilities. Please refer to the Eclipse Project "IAP-Addon-Example" and use this as an template for your own developments. Check the content of the file IAPexamplAddOn.xml and modify the meta data (description of the Add-on) as needed. The createAdd-on.xml file is an Ant-Script (right-click it within Eclipse), which is used to pacakge the project content into a single JAR file, which can be loaded as an IAP Add-on. The Add-on is essentially a packages Plug-in, developed as described before on this page. The Plugin-description-XML file needs to be placed in the root of the source code folder. And the final JAR file name needs to be the same as the XML description file (ignoring the file extension). If you have developed your Block/Pipeline or Plugin and have problems in packaging it in a working Add-on, dont hesitate to contact the authors of the sytem
