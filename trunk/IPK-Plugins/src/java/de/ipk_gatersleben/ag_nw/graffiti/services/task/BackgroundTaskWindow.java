/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.services.task;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import org.BackgroundTaskStatusProvider;
import org.FolderPanel;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;

public class BackgroundTaskWindow extends JDialog implements BackgroundTaskGUIprovider, ActionListener {
	private static final long serialVersionUID = -4862794883295382225L;
	private final BackgroundTaskPanelEntry panel;
	private final boolean isModal;
	
	public BackgroundTaskWindow(boolean modal) {
		super(GravistoService.getInstance().getMainFrame(),
				"", false);
		// setSize(320, 180);
		// setLocationByPlatform(true);
		double border = 10;
		double[][] size =
		{
				{ border, TableLayoutConstants.PREFERRED, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, border }
		}; // Rows
		
		setLayout(new TableLayout(size));
		
		panel = new BackgroundTaskPanelEntry(true);
		
		add(panel, "1,1");
		
		panel.addCloseActionListener(this);
		
		this.isModal = modal;
		
		if (isModal) {
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			GravistoService.getInstance().getMainFrame().setEnabled(false);
		}
		
		validate();
	}
	
	@Override
	public String getTitle() {
		String s = panel.taskMessage;
		if (s != null && s.indexOf("<br>") > 0)
			s = s.substring(0, s.indexOf("<br>")).trim();
		if (s != null && s.indexOf("<small>") > 0)
			s = s.substring(0, s.indexOf("<small>")).trim();
		return StringManipulationTools.removeHTMLtags(s);
	}
	
	@Override
	public boolean isResizable() {
		return false;
	}
	
	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return panel.getStatusProvider();
	}
	
	@Override
	public boolean isProgressViewVisible() {
		return panel.isProgressViewVisible();
	}
	
	@Override
	public void setStatusProvider(BackgroundTaskStatusProvider statusProvider,
			String title, String taskMessage) {
		panel.setStatusProvider(statusProvider, title, taskMessage);
		panel.disableTitleView();
		validate();
		pack();
		setLocationRelativeTo(MainFrame.getInstance());
		setVisible(true);
		updateSize();
	}
	
	@Override
	public void setTaskFinished(boolean autoClose, long duration) {
		panel.setTaskFinished(autoClose, duration);
		if (isModal) {
			GravistoService.getInstance().getMainFrame().setEnabled(true);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		dispose();
	}
	
	public void updateSize() {
		FolderPanel.performDialogResize(panel);
	}
}