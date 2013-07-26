package org.graffiti.editor;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.OpenFileDialogService;

public class RecentEntry extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public RecentEntry(String data, boolean visible, Icon icon) {
		super();
		if (!data.equalsIgnoreCase("")) {
			setAction(getOpenAction(new File(data)));
			setText(data.substring(data.lastIndexOf(File.separator) + 1));
			setToolTipText(data);
			setVisible(visible);
		} else
			setVisible(false);
		setIcon(icon);
	}
	
	public RecentEntry(File data, boolean visible, Icon icon) {
		super();
		setAction(getOpenAction(data));
		setText(data.getName());
		setToolTipText(data.getAbsolutePath());
		setVisible(visible);
		setIcon(icon);
	}
	
	public RecentEntry(RecentEntry from) {
		super();
		setNewData(from);
	}
	
	public void setNewData(RecentEntry from) {
		setAction(from.getAction());
		setText(from.getText());
		setToolTipText(from.getToolTipText());
		setVisible(from.isVisible());
		setIcon(from.getIcon());
	}
	
	private Action getOpenAction(final File file) {
		return new Action() {
			public void actionPerformed(ActionEvent e) {
				try {
					MainFrame.getInstance().loadGraphInBackground(file, null, false);
					OpenFileDialogService.setActiveDirectoryFrom(file.getParentFile());
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				}
			}
			
			public void setEnabled(boolean b) {
			}
			
			public void removePropertyChangeListener(PropertyChangeListener listener) {
			}
			
			public void putValue(String key, Object value) {
			}
			
			public boolean isEnabled() {
				return true;
			}
			
			public Object getValue(String key) {
				return null;
			}
			
			public void addPropertyChangeListener(PropertyChangeListener listener) {
			}
		};
	}
	
}
