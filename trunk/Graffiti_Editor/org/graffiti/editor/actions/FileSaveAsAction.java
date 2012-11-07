// ==============================================================================
//
// FileSaveAsAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: FileSaveAsAction.java,v 1.2 2012-11-07 14:42:07 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.graffiti.core.GenericFileFilter;
import org.graffiti.core.StringBundle;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Graph;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.plugin.actions.GraffitiAction;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.view.SuppressSaveActionsView;
import org.graffiti.session.EditorSession;
import org.graffiti.session.SessionManager;

/**
 * The action for saving a graph to a named file.
 * 
 * @version $Revision: 1.2 $
 */
public class FileSaveAsAction
		extends GraffitiAction {
	// ~ Instance fields ========================================================
	private static final long serialVersionUID = 1L;
	
	/** DOCUMENT ME! */
	private final IOManager ioManager;
	
	/** DOCUMENT ME! */
	private final SessionManager sessionManager;
	
	/** DOCUMENT ME! */
	private final StringBundle sBundle;
	
	JTextField jTextFieldFileName;
	boolean isTextFieldFileNameSearchDone;
	
	// ~ Constructors ===========================================================
	
	// private JFileChooser fc;
	public FileSaveAsAction(MainFrame mainFrame, IOManager ioManager,
			SessionManager sessionManager, StringBundle sBundle) {
		super("file.saveAs", mainFrame, "filemenu_saveas");
		this.ioManager = ioManager;
		this.sessionManager = sessionManager;
		this.sBundle = sBundle;
		
		// fc = new JFileChooser();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isEnabled() {
		EditorSession session = (EditorSession) mainFrame.getActiveSession();
		if (session != null && session.getActiveView() instanceof SuppressSaveActionsView)
			return false;
		
		return ioManager.hasOutputSerializer() &&
				sessionManager.isSessionActive();
	}
	
	/**
	 * @see org.graffiti.plugin.actions.GraffitiAction#getHelpContext()
	 */
	@Override
	public HelpContext getHelpContext() {
		return null;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = ioManager.createSaveFileChooser();
		
		OpenFileDialogService.setActiveDirectoryFor(fc);
		
		this.jTextFieldFileName = null;
		this.isTextFieldFileNameSearchDone = false;
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
				
				JFileChooser fileChooser = (JFileChooser) propertyChangeEvent.getSource();
				if (!FileSaveAsAction.this.isTextFieldFileNameSearchDone &&
						propertyChangeEvent.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY) &&
						fileChooser.getSelectedFile() != null) {
					String fileName = fileChooser.getSelectedFile().getName();
					// try to set file filter to current file extension
					for (FileFilter filterFilter : fileChooser.getChoosableFileFilters())
						if (fileName.endsWith(((GenericFileFilter) filterFilter).getExtension())) {
							fileChooser.setFileFilter(filterFilter);
							break;
						}
					// try to find file name text field in file save as dialog
					FileSaveAsAction.this.jTextFieldFileName = getTextFieldFileName(fileChooser.getComponents(), fileName);
					FileSaveAsAction.this.isTextFieldFileNameSearchDone = true;
					// FileSaveAsAction.this.jTextFieldFileName = null;
				}
				if (FileSaveAsAction.this.jTextFieldFileName != null &&
						(propertyChangeEvent.getPropertyName().equals(JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) ||
						propertyChangeEvent.getPropertyName().equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) &&
						fileChooser.getFileFilter() != null) {
					String fileName = FileSaveAsAction.this.jTextFieldFileName.getText();
					if (fileName != null && fileName.length() > 0) {
						String path = fileChooser.getCurrentDirectory().getAbsolutePath();
						// try to remove old file extension from file name
						String oldFileExtension = ((GenericFileFilter) propertyChangeEvent.getOldValue()).getExtension();
						if (fileName.endsWith(oldFileExtension))
							fileName = fileName.substring(0, fileName.lastIndexOf(oldFileExtension));
						else
							// else try to remove file extensions known from input serializers
							for (String fileExtension : MainFrame.getInstance().getIoManager().getGraphFileExtensions())
								if (fileName.endsWith(fileExtension)) {
									fileName = fileName.substring(0, fileName.lastIndexOf(fileExtension));
									break;
								}
						// add new file extension to file name if necessary
						String fileExtension = ((GenericFileFilter) fileChooser.getFileFilter()).getExtension();
						if (!fileName.endsWith(fileExtension))
							fileChooser.setSelectedFile(new File(path + File.separator + fileName + fileExtension));
					}
				}
				
			}
		};
		fc.addPropertyChangeListener(propertyChangeListener);
		
		try {
			String n = getGraph().getName(true);
			String on = n;
			if (n.endsWith("*"))
				n = n.substring(0, n.length() - 1);
			n = n.replaceAll("%20", " ");
			// n = StringManipulationTools.stringReplace(n, "[not saved]", "new file");
			if (n.startsWith("[not saved") && n.endsWith("]")) {
				n = StringManipulationTools.stringReplace(n, "[not saved", "new file");
				n = StringManipulationTools.stringReplace(n, "]", "");
			}
			n = n.trim();
			if (n != null && n.length() > 0 && on.equals(n) && new File(n).canWrite())
				fc.setSelectedFile(new File(n));
			else
				if (n.startsWith("new file"))
					fc.setSelectedFile(new File(n));
				else
					fc.setSelectedFile(new File(getGraph().getName(false).trim()));
		} catch (Exception err) {
			// empty
		}
		
		boolean needFile = true;
		while (needFile) {
			int returnVal = fc.showDialog(mainFrame, sBundle.getString("menu.file.saveAs"));
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				File oldfile = null;
				try {
					oldfile = new File(mainFrame.getActiveEditorSession().getFileNameFull()).getParentFile();
				} catch (Exception e1) {
				}
				
				File file = fc.getSelectedFile();
				String ext = ((GenericFileFilter) fc.getFileFilter()).getExtension();
				
				String path = fc.getCurrentDirectory().getAbsolutePath();
				String fileName = file.getName();
				// fall back if file name text field could not be found
				if (FileSaveAsAction.this.jTextFieldFileName == null) {
					// try to remove old file extension ...
					// ... use file extensions from file save as dialog
					for (FileFilter filterFilter : fc.getChoosableFileFilters()) {
						String fileExtension = ((GenericFileFilter) filterFilter).getExtension();
						if (!ext.equals(fileExtension) && fileName.endsWith(fileExtension)) {
							fileName = fileName.substring(0, fileName.lastIndexOf(fileExtension));
							break;
						}
					}
					// ... use file extensions from input serializers
					for (String fileExtension : MainFrame.getInstance().getIoManager().getGraphFileExtensions())
						if (!ext.equals(fileExtension) && fileName.endsWith(fileExtension)) {
							fileName = fileName.substring(0, fileName.lastIndexOf(fileExtension));
							break;
						}
				}
				// add file extension to file name if necessary
				// if file name contains '.' as in 'abc.def' but doesn't contain
				// a known file extension '.def' is treated as unknown file extension
				if (!fileName.endsWith(ext))
					file = new File(path + File.separator + fileName + ext);
				
				needFile = safeFile(file, ext, getGraph());
				
				FileHandlingManager.getInstance().throwFileSavedAs(oldfile, file.getParentFile());
				
				if (!needFile) {
					EditorSession session = (EditorSession) mainFrame.getActiveSession();
					
					if (!file.getName().endsWith(ext))
						file = new File(file.getAbsolutePath() + ext);
					session.setFileName(file.getAbsolutePath());
					
					if (session != null && session.getUndoManager() != null)
						session.getUndoManager().discardAllEdits();
					
					mainFrame.fireSessionDataChanged(session);
					OpenFileDialogService.setActiveDirectoryFrom(fc.getCurrentDirectory());
				}
			} else {
				// leave loop
				needFile = false;
			}
		}
		fc.removePropertyChangeListener(propertyChangeListener);
	}
	
	public static boolean safeFile(File file, String ext, Graph graph) {
		String fileName = file.getName();
		boolean needFile = true;
		// System.err.println(fileName);
		
		if (fileName.indexOf(".") == -1) {
			fileName = file.getName() + ext;
			file = new File(file.getAbsolutePath() + ext);
		} else {
			ext = FileSaveAction.getFileExt(fileName);
		}
		
		// System.err.println(fileName);
		if (file.exists()) {
			if (JOptionPane.showConfirmDialog(MainFrame.getInstance(),
					"<html>Do you want to overwrite the existing file <i>" +
							fileName + "</i>?</html>", "Overwrite File?",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				needFile = false;
			}
		} else {
			needFile = false;
		}
		
		if (!needFile) {
			try {
				IOManager ioManager = MainFrame.getInstance().getIoManager();
				OutputSerializer os = ioManager.createOutputSerializer(ext);
				if (os == null)
					MainFrame.getInstance().showMessageDialog("Output serializer unknown for file extension '" + ext + "'.");
				else {
					MainFrame.showMessage("Save graph to file " + file.getAbsolutePath() + "...", MessageType.PERMANENT_INFO);
					os.write(new FileOutputStream(file), graph);
					graph.setModified(false);
					graph.setName(file.getAbsolutePath());
					long fs = file.length();
					MainFrame.showMessage("Graph saved to file " + file.getAbsolutePath() + " (" + (fs / 1024) + "KB)", MessageType.INFO);
					MainFrame.getInstance().addNewRecentFileMenuItem(file);
				}
			} catch (Exception ioe) {
				ErrorMsg.addErrorMessage(ioe);
				MainFrame.getInstance().warnUserAboutFileSaveProblem(ioe);
			}
		}
		return needFile;
	}
	
	JTextField getTextFieldFileName(Component[] components, String fileName) {
		
		JTextField jTextField = null;
		for (Component component : components)
			if (component instanceof JPanel) {
				jTextField = getTextFieldFileName(((JPanel) component).getComponents(), fileName);
				if (jTextField != null)
					break;
			}
			else
				if (component instanceof JTextField &&
						fileName.equals(((JTextField) component).getText())) {
					jTextField = (JTextField) component;
					break;
				}
		return jTextField;
		
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
