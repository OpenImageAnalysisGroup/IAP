package ij.plugin;


/** This plugin displays the contents of a text file in a window. */
public class TextFileReader implements PlugIn {
	
	public void run(String arg) {
		new ij.text.TextWindow(arg,400,450);
	}

}
