/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.05.2004
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.HelperClass;
import org.OpenFileDialogService;
import org.graffiti.editor.GravistoService;

public class FileHelper implements HelperClass {
	public static String getFileName(final String defaultExt,
						final String description, String defaultFileName) {
		JFileChooser fc = new JFileChooser();
		
		OpenFileDialogService.setActiveDirectoryFor(fc);
		if (defaultFileName != null && defaultFileName.length() > 0)
			fc.setSelectedFile(new File(defaultFileName));
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toUpperCase().endsWith(defaultExt.toUpperCase());
			}
			
			@Override
			public String getDescription() {
				return defaultExt + " files";
			}
		});
		
		String fileName = "";
		File file = null;
		boolean needFile = true;
		
		while (needFile) {
			int returnVal = fc.showDialog(GravistoService.getInstance()
								.getMainFrame(), "Create " + description);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				fileName = file.getName();
				// System.err.println(fileName);
				String ext = defaultExt;
				
				if (fileName.indexOf(".") == -1) {
					fileName = file.getName() + "." + ext;
					file = new File(file.getAbsolutePath() + "." + ext);
				}
				
				// System.err.println(fileName);
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(GravistoService.getInstance()
										.getMainFrame(),
										"<html>Do you want to overwrite the existing file <i>"
															+ fileName + "</i>?</html>", "Overwrite File?",
										JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						needFile = false;
					} else
						file = null;
				} else {
					needFile = false;
				}
			} else {
				// leave loop
				needFile = false;
			}
		}
		
		if (file != null) {
			OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
			return file.getAbsolutePath();
		} else {
			return null;
		}
	}
	
	public static String getFileName(final String defaultExt,
						final String description) {
		return getFileName(defaultExt, description, null);
	}
	
	/**
	 * Recursively deletes a directory
	 * 
	 * @param path
	 *           the path of the directory to be deleted
	 */
	public static void deleteDirRecursively(File f) {
		if (!f.exists())
			return;
		if (!f.isDirectory())
			f.delete();
		else {
			for (File file : f.listFiles()) {
				if (file.isDirectory())
					deleteDirRecursively(file);
				else
					file.delete();
			}
		}
	}
}
