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
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;

public class BackgroundTaskWindow extends JDialog implements BackgroundTaskGUIprovider, ActionListener {
	private static final long serialVersionUID = -4862794883295382225L;
	private BackgroundTaskPanelEntry panel;
	private boolean isModal;
	
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
		return panel.taskMessage;
	}
	
	@Override
	public boolean isResizable() {
		return false;
	}
	
	public BackgroundTaskStatusProvider getStatusProvider() {
		return panel.getStatusProvider();
	}
	
	public boolean isProgressViewVisible() {
		return panel.isProgressViewVisible();
	}
	
	public void setStatusProvider(BackgroundTaskStatusProvider statusProvider,
						String title, String taskMessage) {
		panel.setStatusProvider(statusProvider, title, taskMessage);
		panel.disableTitleView();
		validate();
		pack();
		setLocationRelativeTo(MainFrame.getInstance());
		setVisible(true);
	}
	
	public void setTaskFinished(boolean autoClose, long duration) {
		panel.setTaskFinished(autoClose, duration);
		if (isModal) {
			GravistoService.getInstance().getMainFrame().setEnabled(true);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		dispose();
	}
}