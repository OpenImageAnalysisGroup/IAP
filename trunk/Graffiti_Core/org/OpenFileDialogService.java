package org;

import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.collections.ListUtils;

/**
 * @author Christian Klukas
 *         (c) 2014 IPK-Gatersleben
 */
public class OpenFileDialogService implements HelperClass {
	
	private static final boolean USE_FX_BY_DEFAULT = true;
	private static JFileChooser openDialog = null;
	
	public static File getFile(final String[] valid_extensions, final String description) {
		if (!SystemOptions.getInstance().getBoolean("IAP", "FX//Use FX File Choosers", USE_FX_BY_DEFAULT))
			return OpenFileDialogServiceSwing.getFile(valid_extensions, description);
		
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		JavaFX.init();
		FileChooser fc = new FileChooser();
		if (openDialog.getCurrentDirectory() != null)
			fc.setInitialDirectory(openDialog.getCurrentDirectory());
		fc.setTitle(description);
		
		for (String ext : valid_extensions) {
			fc.getExtensionFilters().add(
					new FileChooser.ExtensionFilter(StringManipulationTools.stringReplace(ext, "*.", "").toUpperCase(), ext));
		}
		if (Platform.isFxApplicationThread()) {
			File f = fc.showOpenDialog(null);
			if (f != null)
				openDialog.setCurrentDirectory(f);
			return f;
		} else {
			ObjectRef res = new ObjectRef();
			Semaphore s = new Semaphore(1);
			s.acquireUninterruptibly();
			Platform.runLater(() -> {
				try {
					File f = fc.showOpenDialog(null);
					res.setObject(f);
				} finally {
					s.release();
				}
			});
			s.acquireUninterruptibly();
			File f = (File) res.getObject();
			if (f != null) {
				if (!f.isDirectory())
					f = f.getParentFile();
				openDialog.setCurrentDirectory(f);
			}
			return f;
		}
	}
	
	public static File getFile(final String[] valid_extensions, final String description, String selectButtonText) {
		if (!SystemOptions.getInstance().getBoolean("IAP", "FX//Use FX File Choosers", USE_FX_BY_DEFAULT))
			return OpenFileDialogServiceSwing.getFile(valid_extensions, description, selectButtonText);
		
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		JavaFX.init();
		FileChooser fc = new FileChooser();
		if (openDialog.getCurrentDirectory() != null)
			fc.setInitialDirectory(openDialog.getCurrentDirectory());
		fc.setTitle(description + " - " + selectButtonText);
		
		for (String ext : valid_extensions) {
			fc.getExtensionFilters().add(
					new FileChooser.ExtensionFilter(description + " - " + StringManipulationTools.stringReplace(ext, "*.", "").toUpperCase(), ext));
		}
		if (Platform.isFxApplicationThread()) {
			File f = fc.showOpenDialog(null);
			if (f != null)
				openDialog.setCurrentDirectory(f);
			return f;
		} else {
			ObjectRef res = new ObjectRef();
			Semaphore s = new Semaphore(1);
			s.acquireUninterruptibly();
			Platform.runLater(() -> {
				try {
					File f = fc.showOpenDialog(null);
					res.setObject(f);
				} finally {
					s.release();
				}
			});
			s.acquireUninterruptibly();
			File f = (File) res.getObject();
			if (f != null) {
				if (!f.isDirectory())
					f = f.getParentFile();
				openDialog.setCurrentDirectory(f);
			}
			return f;
		}
	}
	
	public static List<File> getFiles(final String[] valid_extensions, final String description) {
		if (!SystemOptions.getInstance().getBoolean("IAP", "FX//Use FX File Choosers", USE_FX_BY_DEFAULT))
			return OpenFileDialogServiceSwing.getFiles(valid_extensions, description);
		
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		JavaFX.init();
		FileChooser fc = new FileChooser();
		if (openDialog.getCurrentDirectory() != null)
			fc.setInitialDirectory(openDialog.getCurrentDirectory());
		fc.setTitle(description);
		
		fc.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Images", ListUtils.union(
						StringManipulationTools.getStringListFromStream(StringManipulationTools.getStringListFromArray(valid_extensions).stream()
								.map(s -> "*." + s.toLowerCase()))
						, (StringManipulationTools.getStringListFromStream(StringManipulationTools.getStringListFromArray(valid_extensions).stream()
								.map(s -> "*." + s.toUpperCase()))))));
		
		for (String ext : valid_extensions) {
			fc.getExtensionFilters().add(
					new FileChooser.ExtensionFilter(StringManipulationTools.stringReplace(ext, "*.", "").toUpperCase() + " (*." + ext + ")", "*."
							+ ext.toUpperCase(), "*."
							+ ext.toLowerCase()));
		}
		if (Platform.isFxApplicationThread()) {
			List<File> f = fc.showOpenMultipleDialog(null);
			if (f != null && !f.isEmpty()) {
				File ff = f.iterator().next();
				if (!ff.isDirectory())
					ff = ff.getParentFile();
				openDialog.setCurrentDirectory(ff);
			}
			return f;
		} else {
			ObjectRef res = new ObjectRef();
			Semaphore s = new Semaphore(1);
			s.acquireUninterruptibly();
			Platform.runLater(() -> {
				try {
					List<File> f = fc.showOpenMultipleDialog(null);
					res.setObject(f);
				} finally {
					s.release();
				}
			});
			s.acquireUninterruptibly();
			List<File> f = (List<File>) res.getObject();
			if (f != null && !f.isEmpty()) {
				File ff = f.iterator().next();
				if (!ff.isDirectory())
					ff = ff.getParentFile();
				openDialog.setCurrentDirectory(ff);
			}
			return f;
		}
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
		return getDirectoryFromUser(okButtonText, null);
	}
	
	public static File getDirectoryFromUser(String okButtonText, String startDir) {
		if (!SystemOptions.getInstance().getBoolean("IAP", "FX//Use FX File Choosers", USE_FX_BY_DEFAULT))
			return OpenFileDialogServiceSwing.getDirectoryFromUser(okButtonText, startDir);
		if (openDialog == null) {
			openDialog = new JFileChooser();
		}
		try {
			if (startDir != null && !startDir.trim().isEmpty()) {
				File f = new File(startDir);
				if (f.exists())
					openDialog.setCurrentDirectory(f);
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + e.getMessage());
		}
		JavaFX.init();
		DirectoryChooser dc = new DirectoryChooser();
		if (openDialog.getCurrentDirectory() != null && openDialog.getCurrentDirectory().exists())
			dc.setInitialDirectory(openDialog.getCurrentDirectory());
		dc.setTitle(okButtonText);
		if (Platform.isFxApplicationThread()) {
			File f = dc.showDialog(null);
			if (f != null)
				openDialog.setCurrentDirectory(f);
			return f;
		} else {
			ObjectRef res = new ObjectRef();
			Semaphore s = new Semaphore(1);
			s.acquireUninterruptibly();
			Platform.runLater(() -> {
				try {
					File f = dc.showDialog(null);
					res.setObject(f);
				} finally {
					s.release();
				}
			});
			s.acquireUninterruptibly();
			File f = (File) res.getObject();
			if (f != null)
				openDialog.setCurrentDirectory(f);
			return f;
		}
	}
}
