package ij;

import ij.util.Tools;
import ij.text.TextWindow;
import ij.plugin.MacroInstaller;
import ij.plugin.frame.Recorder;
import ij.io.OpenDialog;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.Menu;

/** Runs ImageJ menu commands in a separate thread. */
public class Executer implements Runnable {
	
	private static String previousCommand;
	private static Vector<CommandListener> listeners = new Vector<CommandListener>();
	
	private String command;
	private Thread thread;
	
	/**
	 * Create an Executer to run the specified menu command
	 * in this thread using the active image.
	 */
	public Executer(String cmd) {
		command = cmd;
	}
	
	/**
	 * Create an Executer that runs the specified menu command
	 * in a separate thread using the active image image.
	 */
	public Executer(String cmd, ImagePlus ignored) {
		if (cmd.startsWith("Repeat")) {
			command = previousCommand;
			IJ.setKeyUp(KeyEvent.VK_SHIFT);
		} else {
			command = cmd;
			if (!(cmd.equals("Undo") || cmd.equals("Close")))
				previousCommand = cmd;
		}
		IJ.resetEscape();
		thread = new Thread(this, cmd);
		thread.setPriority(Math.max(thread.getPriority() - 2, Thread.MIN_PRIORITY));
		thread.start();
	}
	
	public void run() {
		if (command == null)
			return;
		if (listeners.size() > 0)
			synchronized (listeners) {
				for (int i = 0; i < listeners.size(); i++) {
					CommandListener listener = (CommandListener) listeners.elementAt(i);
					command = listener.commandExecuting(command);
					if (command == null)
						return;
				}
			}
		try {
			if (Recorder.record) {
				Recorder.setCommand(command);
				runCommand(command);
				Recorder.saveCommand();
			} else
				runCommand(command);
			int len = command.length();
			if (command.charAt(len - 1) != ']' && !(len < 4 && (command.equals("In") || command.equals("Out"))))
				IJ.setKeyUp(IJ.ALL_KEYS); // set keys up except for "<", ">", "+" and "-" shortcuts
		} catch (Throwable e) {
			IJ.showStatus("");
			IJ.showProgress(1, 1);
			ImagePlus imp = WindowManager.getCurrentImage();
			if (imp != null)
				imp.unlock();
			String msg = e.getMessage();
			if (e instanceof OutOfMemoryError)
				IJ.outOfMemory(command);
			else
				if (e instanceof RuntimeException && msg != null && msg.equals(Macro.MACRO_CANCELED))
					; // do nothing
				else {
					CharArrayWriter caw = new CharArrayWriter();
					PrintWriter pw = new PrintWriter(caw);
					e.printStackTrace(pw);
					String s = caw.toString();
					if (IJ.isMacintosh()) {
						if (s.indexOf("ThreadDeath") > 0)
							return;
						s = Tools.fixNewLines(s);
					}
					int w = 350, h = 250;
					if (s.indexOf("UnsupportedClassVersionError") != -1) {
						if (s.indexOf("version 49.0") != -1) {
							s = e + "\n \nThis plugin requires Java 1.5 or later.";
							w = 700;
							h = 150;
						}
						if (s.indexOf("version 50.0") != -1) {
							s = e + "\n \nThis plugin requires Java 1.6 or later.";
							w = 700;
							h = 150;
						}
						if (s.indexOf("version 51.0") != -1) {
							s = e + "\n \nThis plugin requires Java 1.7 or later.";
							w = 700;
							h = 150;
						}
					}
					if (IJ.getInstance() != null)
						new TextWindow("Exception", s, w, h);
					else
						IJ.log(s);
				}
		}
	}
	
	void runCommand(String cmd) {
		Hashtable<?, ?> table = Menus.getCommands();
		String className = (String) table.get(cmd);
		if (className != null) {
			String arg = "";
			if (className.endsWith("\")")) {
				// extract string argument (e.g. className("arg"))
				int argStart = className.lastIndexOf("(\"");
				if (argStart > 0) {
					arg = className.substring(argStart + 2, className.length() - 2);
					className = className.substring(0, argStart);
				}
			}
			if (IJ.shiftKeyDown() && className.startsWith("ij.plugin.Macro_Runner") && !Menus.getShortcuts().contains("*" + cmd))
				IJ.open(IJ.getDirectory("plugins") + arg);
			else
				IJ.runPlugIn(cmd, className, arg);
		} else {
			// Is this command in Plugins>Macros?
			if (MacroInstaller.runMacroCommand(cmd))
				return;
			// Is this command a LUT name?
			String path = IJ.getDirectory("luts") + cmd + ".lut";
			File f = new File(path);
			if (f.exists()) {
				String dir = OpenDialog.getLastDirectory();
				IJ.open(path);
				OpenDialog.setLastDirectory(dir);
			} else
				if (!openRecent(cmd))
					IJ.error("Unrecognized command: " + cmd);
		}
	}
	
	/**
	 * Opens a file from the File/Open Recent menu
	 * and returns 'true' if successful.
	 */
	boolean openRecent(String cmd) {
		Menu menu = Menus.openRecentMenu;
		if (menu == null)
			return false;
		for (int i = 0; i < menu.getItemCount(); i++) {
			if (menu.getItem(i).getLabel().equals(cmd)) {
				IJ.open(cmd);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the last command executed. Returns null
	 * if no command has been executed.
	 */
	public static String getCommand() {
		return previousCommand;
	}
	
	/** Adds the specified command listener. */
	public static void addCommandListener(CommandListener listener) {
		listeners.addElement(listener);
	}
	
	/** Removes the specified command listener. */
	public static void removeCommandListener(CommandListener listener) {
		listeners.removeElement(listener);
	}
	
}
