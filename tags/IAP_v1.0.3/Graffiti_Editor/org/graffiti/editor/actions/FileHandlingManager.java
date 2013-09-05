package org.graffiti.editor.actions;

import java.io.File;
import java.util.LinkedHashSet;

import org.graffiti.graph.Graph;

public class FileHandlingManager {
	
	private static FileHandlingManager instance;
	private LinkedHashSet<FileHandlingListeners> listeners = new LinkedHashSet<FileHandlingListeners>();
	
	private FileHandlingManager() {
	}
	
	public static synchronized FileHandlingManager getInstance() {
		if (instance == null)
			instance = new FileHandlingManager();
		return instance;
	}
	
	public synchronized void registerFileHandlingListener(FileHandlingListeners l) {
		if (l == null)
			throw new IllegalArgumentException("The argument " + "may not be null");
		else
			listeners.add(l);
	}
	
	public synchronized boolean removeFileHandlingListener(FileHandlingListeners l) {
		return listeners.remove(l);
	}
	
	public synchronized void throwFileSavedAs(File oldFile, File newFile) {
		for (FileHandlingListeners fhl : listeners)
			fhl.fileSavedAs(oldFile, newFile);
		
	}
	
	public synchronized void throwFileSaved(File file, String ext, Graph graph) {
		for (FileHandlingListeners fhl : listeners)
			fhl.fileSaved(file, ext, graph);
		
	}
	
	public synchronized void throwFileOpened(File f) {
		for (FileHandlingListeners fhl : listeners)
			fhl.fileOpened(f);
	}
	
	public synchronized void throwFileNew() {
		for (FileHandlingListeners fhl : listeners)
			fhl.fileNew();
	}
	
}
