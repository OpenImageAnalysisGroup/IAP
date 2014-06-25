package org.graffiti.plugin.parameter;

import java.util.ArrayList;

import org.graffiti.plugin.io.resources.IOurl;

public class MultiFileSelectionParameter extends StringParameter {
	
	private final String[] extensions;
	private final String extensionDescription;
	private final boolean multipleFile;
	
	public MultiFileSelectionParameter(ArrayList<IOurl> value, String name, String description, String[] extensions, String extensionDescription,
						boolean multipleFiles) {
		super(convertToString(value), name, description);
		this.extensions = extensions;
		this.extensionDescription = extensionDescription;
		this.multipleFile = multipleFiles;
	}
	
	public String[] getExtensions() {
		return extensions;
	}
	
	public String getExtensionDescription() {
		return extensionDescription;
	}
	
	public boolean selectMultipleFile() {
		return multipleFile;
	}
	
	public ArrayList<IOurl> getFileList() {
		ArrayList<IOurl> urls = new ArrayList<IOurl>();
		for (String u : ((String) getValue()).split(";"))
			if (u.length() > 0)
				urls.add(new IOurl(u));
		return urls;
	}
	
	public static String convertToString(ArrayList<IOurl> urls) {
		String s = "";
		if (urls == null || urls.size() == 0)
			return s;
		for (IOurl u : urls)
			s += u.toString() + ";";
		if (s.length() > 0)
			s = s.substring(0, s.length() - ";".length());
		
		return s;
	}
	
}
