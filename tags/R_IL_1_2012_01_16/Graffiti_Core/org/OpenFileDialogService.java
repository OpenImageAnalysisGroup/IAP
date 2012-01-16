/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package org;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK-Gatersleben
 */
public class OpenFileDialogService implements HelperClass {
	
	private static JFileChooser openDialog = null;
	
	public static File getFile(final String[] valid_extensions, final String description) {
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		openDialog.setMultiSelectionEnabled(false);
		openDialog.resetChoosableFileFilters();
		openDialog.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return (f.isDirectory()) ||
									((f.canRead() && extensionOK(f.getName(), valid_extensions)));
			}
			
			private boolean extensionOK(String fileName, String[] valid_extensions) {
				for (String ext : valid_extensions) {
					if (fileName.toUpperCase().endsWith(ext.toUpperCase()))
						return true;
				}
				return false;
			}
			
			@Override
			public String getDescription() {
				return description;
			}
		});
		int option = openDialog.showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			return openDialog.getSelectedFile();
		} else
			return null;
	}
	
	public static File getFile(final String[] valid_extensions, final String description, String selectButtonText) {
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		openDialog.setMultiSelectionEnabled(false);
		openDialog.resetChoosableFileFilters();
		openDialog.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return (f.isDirectory()) ||
									((f.canRead() && extensionOK(f.getName(), valid_extensions)));
			}
			
			private boolean extensionOK(String fileName, String[] valid_extensions) {
				for (String ext : valid_extensions) {
					if (fileName.toUpperCase().endsWith(ext.toUpperCase()))
						return true;
				}
				return false;
			}
			
			@Override
			public String getDescription() {
				return description;
			}
		});
		int option = openDialog.showDialog(null, selectButtonText);
		if (option == JFileChooser.APPROVE_OPTION) {
			return openDialog.getSelectedFile();
		} else
			return null;
	}
	
	public static ArrayList<File> getFiles(final String[] valid_extensions, final String description) {
		ArrayList<File> result = new ArrayList<File>();
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		openDialog.resetChoosableFileFilters();
		openDialog.setMultiSelectionEnabled(false);
		openDialog.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return (f.isDirectory()) ||
									((f.canRead() && extensionOK(f.getName(), valid_extensions)));
			}
			
			private boolean extensionOK(String fileName, String[] valid_extensions) {
				for (String ext : valid_extensions) {
					if (fileName.toUpperCase().endsWith(ext.toUpperCase()))
						return true;
				}
				return false;
			}
			
			@Override
			public String getDescription() {
				return description;
			}
		});
		openDialog.setMultiSelectionEnabled(true);
		int option = openDialog.showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			for (File f : openDialog.getSelectedFiles()) {
				result.add(f);
			}
			return result;
		} else
			return null;
	}
	
	public static File getSaveFile(final String[] valid_extensions, final String description) {
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		openDialog.resetChoosableFileFilters();
		openDialog.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return (f.isDirectory()) ||
									((f.canRead() && extensionOK(f.getName(), valid_extensions)));
			}
			
			private boolean extensionOK(String fileName, String[] valid_extensions) {
				for (String ext : valid_extensions) {
					if (fileName.toUpperCase().endsWith(ext.toUpperCase()))
						return true;
				}
				return false;
			}
			
			@Override
			public String getDescription() {
				return description;
			}
		});
		
		int option = openDialog.showSaveDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			return openDialog.getSelectedFile();
		} else
			return null;
	}
	
	public static File getActiveDirectory() {
		if (openDialog == null)
			return null;
		else
			return openDialog.getCurrentDirectory();
	}
	
	public static void setActiveDirectoryFrom(File currentDirectory) {
		if (openDialog == null)
			openDialog = new JFileChooser();
		openDialog.setCurrentDirectory(currentDirectory);
	}
	
	public static void setActiveDirectoryFor(JFileChooser fc) {
		if (openDialog != null)
			fc.setCurrentDirectory(openDialog.getCurrentDirectory());
	}
	
	public static File getDirectoryFromUser(String okButtonText) {
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		openDialog.setMultiSelectionEnabled(false);
		openDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		// openDialog.resetChoosableFileFilters();
		// openDialog.setFileFilter(new FileFilter() {
		// public boolean accept(File f) {
		// return f.isDirectory();
		// }
		//
		// public String getDescription() {
		// return "Directory";
		// }
		// });
		int option = openDialog.showDialog(null, okButtonText);
		if (option == JFileChooser.APPROVE_OPTION) {
			return openDialog.getSelectedFile();
		} else
			return null;
	}
}
