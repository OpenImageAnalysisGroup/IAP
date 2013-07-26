package org.graffiti.plugins.editcomponents.defaults;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.LineBorder;

import net.iharder.dnd.FileDrop;

import org.OpenFileDialogService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.parameter.MultiFileSelectionParameter;

public class MultiFileSelectionEditComponent extends AbstractValueEditComponent {
	
	private JButton selectFiles = null;
	// private JLabel indicator = null;
	private ArrayList<IOurl> urls;
	protected String oldText = "";
	
	public MultiFileSelectionEditComponent(Displayable disp) {
		super(disp);
	}
	
	@Override
	public JComponent getComponent() {
		if (selectFiles == null) {
			selectFiles = new JButton("Choose");
			new FileDrop(selectFiles, new LineBorder(new Color(0, 0, 200, 100), 5), false, new FileDrop.Listener() {
				@Override
				public void filesDropped(File[] files) {
					String[] extensions = ((MultiFileSelectionParameter) getDisplayable()).getExtensions();
					ArrayList<File> goodFiles = new ArrayList<File>();
					for (File f : files)
						for (String ext : extensions)
							if (f.getName().toLowerCase().endsWith(ext.toLowerCase()))
								goodFiles.add(f);
					
					urls = new ArrayList<IOurl>();
					if (!((MultiFileSelectionParameter) getDisplayable()).selectMultipleFile() && goodFiles.size() > 1) {
						urls.add(FileSystemHandler.getURL(goodFiles.iterator().next()));
						MainFrame.showMessage("Many files dropped, but just one allowed. Using only first file...", MessageType.INFO);
					} else
						for (File f : goodFiles)
							urls.add(FileSystemHandler.getURL(f));
					
					setValue();
					setEditFieldValue();
					
				}
			});
			selectFiles.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					String[] extensions = ((MultiFileSelectionParameter) getDisplayable()).getExtensions();
					String extensionDescription = ((MultiFileSelectionParameter) getDisplayable()).getExtensionDescription();
					ArrayList<File> files = new ArrayList<File>();
					if (((MultiFileSelectionParameter) getDisplayable()).selectMultipleFile()) {
						ArrayList<File> selected = OpenFileDialogService.getFiles(extensions, extensionDescription);
						if (selected != null)
							files.addAll(selected);
					} else {
						File f = OpenFileDialogService.getFile(extensions, extensionDescription);
						if (f != null)
							files.add(f);
					}
					urls = new ArrayList<IOurl>();
					for (File f : files)
						urls.add(FileSystemHandler.getURL(f));
					
					setValue();
					setEditFieldValue();
				}
			});
		}
		
		return selectFiles;
	}
	
	@Override
	public void setEditFieldValue() {
		if (showEmpty)
			selectFiles.setText("none selected");
		else {
			int size = ((MultiFileSelectionParameter) displayable).getFileList().size();
			selectFiles.setText(size + " file" + (size == 1 ? "" : "s") + " selected");
		}
	}
	
	@Override
	public void setValue() {
		displayable.setValue(MultiFileSelectionParameter.convertToString(urls));
	}
	
}