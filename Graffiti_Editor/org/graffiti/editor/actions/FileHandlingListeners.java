package org.graffiti.editor.actions;

import java.io.File;

import org.graffiti.graph.Graph;

public interface FileHandlingListeners {
	
	public void fileSavedAs(File oldFile, File newFile);
	
	public void fileSaved(File file, String ext, Graph graph);
	
	public void fileOpened(File f);
	
	public void fileNew();
	
}
