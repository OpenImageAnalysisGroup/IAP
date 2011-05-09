package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.Opener;
import ij.text.TextWindow;
import java.awt.Frame;

/** This plugin implements the Plugins/Utilities/Unlock, Image/Rename
	and Plugins/Utilities/Search commands. */
public class SimpleCommands implements PlugIn {
	static String searchArg;
    private static String[] choices = {"Locked Image", "Clipboard", "Undo Buffer"};
    private static int choiceIndex = 0;

	public void run(String arg) {
		if (arg.equals("search"))
			search();
		else if (arg.equals("import")) 
			Opener.openResultsTable("");
		else if (arg.equals("rename"))
			rename();
		else if (arg.equals("reset"))
			reset();
		else if (arg.equals("about"))
			aboutPluginsHelp();
		else if (arg.equals("install"))
			installation();
	}

	void reset() {
		GenericDialog gd = new GenericDialog("");
		gd.addChoice("Reset:", choices, choices[choiceIndex]);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		choiceIndex = gd.getNextChoiceIndex();
		switch (choiceIndex) {
			case 0: unlock(); break;
			case 1: resetClipboard(); break;
			case 2: resetUndo(); break;
		}
	}
	
	void unlock() {
		ImagePlus imp = IJ.getImage();
		boolean wasUnlocked = imp.lockSilently();
		if (wasUnlocked)
			IJ.showStatus("\""+imp.getTitle()+"\" is not locked");
		else {
			IJ.showStatus("\""+imp.getTitle()+"\" is now unlocked");
			IJ.beep();
		}
		imp.unlock();
	}

	void resetClipboard() {
		ImagePlus.resetClipboard();
		IJ.showStatus("Clipboard reset");
	}
	
	void resetUndo() {
		Undo.setup(Undo.NOTHING, null);
		IJ.showStatus("Undo reset");
	}
	
	void rename() {
		ImagePlus imp = IJ.getImage();
		GenericDialog gd = new GenericDialog("Rename");
		gd.addStringField("Title:", imp.getTitle(), 30);
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		else
			imp.setTitle(gd.getNextString());
	}
		
	void search() {
		searchArg = IJ.runMacroFile("ij.jar:Search", searchArg);
	}
		
	void installation() {
		String url = IJ.URL+"/docs/install/";
		if (IJ.isMacintosh())
			url += "osx.html";
		else if (IJ.isWindows())
			url += "windows.html";
		else if (IJ.isLinux())
			url += "linux.html";
		IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
	}
	
	void aboutPluginsHelp() {
		IJ.showMessage("\"About Plugins\" Submenu", 
			"Plugins packaged as JAR files can add entries\n"+
			"to this submenu. There is an example at\n \n"+
			IJ.URL+"/plugins/jar-demo.html");
	}
	
}
