package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.frame.*;
import ij.text.TextWindow;
import ij.macro.Interpreter;
import java.awt.Frame;
import java.io.File;
import java.applet.Applet;
	
/**	Runs miscellaneous File and Window menu commands. */
public class Commands implements PlugIn {
	
	public void run(String cmd) {
		if (cmd.equals("new"))
			new NewImage();
		else if (cmd.equals("open")) {
			if (Prefs.useJFileChooser && !IJ.macroRunning())
				new Opener().openMultiple();
			else
				new Opener().open();
		} else if (cmd.equals("close"))
			close();
		else if (cmd.equals("close-all"))
			closeAll();
		else if (cmd.equals("save"))
			save();
		else if (cmd.equals("ij")) {
			ImageJ ij = IJ.getInstance();
			if (ij!=null) ij.toFront();
		} else if (cmd.equals("tab"))
			WindowManager.putBehind();
		else if (cmd.equals("quit")) {
			ImageJ ij = IJ.getInstance();
			if (ij!=null) ij.quit();
		} else if (cmd.equals("revert"))
			revert();
		else if (cmd.equals("undo"))
			undo();
		else if (cmd.equals("startup"))
			openStartupMacros();
    }
    
    void revert() {
    	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			imp.revert();
		else
			IJ.noImage();
	}

    void save() {
    	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null) {
			boolean unlockedImage = imp.getStackSize()==1&&!imp.isLocked();
			if (unlockedImage) imp.lock();
			new FileSaver(imp).save();
			if (unlockedImage) imp.unlock();
		} else
			IJ.noImage();
	}
	
    void undo() {
    	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp!=null)
			Undo.undo();
		else
			IJ.noImage();
	}

	void close() {
    	ImagePlus imp = WindowManager.getCurrentImage();
		Frame frame = WindowManager.getFrontWindow();
		if (frame==null || (Interpreter.isBatchMode() && frame instanceof ImageWindow))
			closeImage(imp);
		else if (frame instanceof PlugInFrame)
			((PlugInFrame)frame).close();
		else if (frame instanceof TextWindow)
			((TextWindow)frame).close();
		else
			closeImage(imp);
	}

	void closeAll() {
    	int[] list = WindowManager.getIDList();
    	if (list!=null) {
    		int imagesWithChanges = 0;
			for (int i=0; i<list.length; i++) {
				ImagePlus imp = WindowManager.getImage(list[i]);
				if (imp!=null && imp.changes) imagesWithChanges++;
			}
			if (imagesWithChanges>0 && !IJ.macroRunning()) {
				GenericDialog gd = new GenericDialog("Close All");
				String msg = null;
				String pronoun = null;
				if (imagesWithChanges==1) {
					msg = "There is one image";
					pronoun = "it";
				} else {
					msg = "There are "+imagesWithChanges+" images";
					pronoun = "they";
				}
				gd.addMessage(msg+" with unsaved changes. If you\nclick \"OK\" "+pronoun
					+" will be closed without being saved.");
				gd.showDialog();
				if (gd.wasCanceled()) return;
			}
			for (int i=0; i<list.length; i++) {
				ImagePlus imp = WindowManager.getImage(list[i]);
				if (imp!=null) {
					imp.changes = false;
					imp.close();
				}
			}
    	}
    	//Frame[] windows = WindowManager.getNonImageWindows();
    	//for (int i=0; i<windows.length; i++) {
    	//	if ((windows[i] instanceof PlugInFrame) && !(windows[i] instanceof Editor))
    	//		((PlugInFrame)windows[i]).close();
    	//}
	}

	void closeImage(ImagePlus imp) {
		if (imp==null) {
			IJ.noImage();
			return;
		}
		imp.close();
		if (Recorder.record && !IJ.isMacro()) {
			if (Recorder.scriptMode())
				Recorder.recordCall("imp.close();");
			else
				Recorder.record("close");
			Recorder.setCommand(null); // don't record run("Close")
		}
	}
	
	// Plugins>Macros>Open Startup Macros command
	void openStartupMacros() {
		Applet applet = IJ.getApplet();
		if (applet!=null) {
			IJ.run("URL...", "url="+IJ.URL+"/applet/StartupMacros.txt");
		} else {
			String path = IJ.getDirectory("macros")+"/StartupMacros.txt";
			File f = new File(path);
			if (!f.exists())
				IJ.error("\"StartupMacros.txt\" not found in ImageJ/macros/");
			else
				IJ.open(path);
		}
	}
	
}



