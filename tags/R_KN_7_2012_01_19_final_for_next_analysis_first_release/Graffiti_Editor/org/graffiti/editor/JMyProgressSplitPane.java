/*
 * Created on 12.08.2004 by Christian Klukas
 */
package org.graffiti.editor;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.graffiti.plugin.gui.PluginPanel;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class JMyProgressSplitPane extends JSplitPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel progressPanel;
	
	/**
	 * @param vertical_split
	 * @param pluginPanel
	 * @param progressPanel
	 */
	public JMyProgressSplitPane(int vertical_split, PluginPanel pluginPanel, JPanel progressPanel) {
		super(vertical_split, pluginPanel, progressPanel);
		this.progressPanel = progressPanel;
	}
	
	@Override
	public int getDividerLocation() {
		return super.getDividerLocation();
		/*
		 * if (progressPanel==null) return super.getDividerLocation();
		 * int height=0;
		 * Component[] gc=progressPanel.getComponents();
		 * for (int i=0; i<gc.length; i++) {
		 * height+=gc[i].getPreferredSize().height;
		 * }
		 * return getHeight()-height;
		 */
	}
}
