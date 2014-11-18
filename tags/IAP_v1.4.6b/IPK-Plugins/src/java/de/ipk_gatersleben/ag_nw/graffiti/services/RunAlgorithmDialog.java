/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.09.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.services;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings.PreferencesDialog;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.GravistoMainHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class RunAlgorithmDialog extends JDialog
		implements HandlesAlgorithmData {
	
	private static final long serialVersionUID = 1L;
	
	private Algorithm algorithm;
	
	private String title;
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public RunAlgorithmDialog(String title, Graph graph, Selection selection,
			boolean returnAlgorithmInsteadOfRun,
			boolean executeMoveToTopAfterwards) {
		algorithm = null;
		
		if (title == null)
			this.title = "Apply Layout";
		else
			this.title = title;
		double border = 2;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		getContentPane().setLayout(new TableLayout(size));
		
		final PreferencesDialog pd = new PreferencesDialog();
		GravistoMainHelper.getPluginManager()
				.addPluginManagerListener(pd);
		JPanel newPanel = new JPanel();
		if (returnAlgorithmInsteadOfRun)
			pd.initializeGUIforGivenContainer(newPanel, this, false, false, true, false,
					false, false, true, graph, selection, this, executeMoveToTopAfterwards);
		else
			pd.initializeGUIforGivenContainer(newPanel, this, false, false, true, false,
					true, false, true, graph, selection, null, executeMoveToTopAfterwards);
		
		getContentPane().add(newPanel, "1,1");
		getContentPane().validate();
		setSize(600, 450);
		setLocationRelativeTo(MainFrame.getInstance());
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			@SuppressWarnings("deprecation")
			public void windowClosing(WindowEvent e) {
				if (pd.optionsForPlugin != null) {
					pd.optionsForPlugin.setAbortWanted(true);
				}
				hide();
				dispose();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.HandlesAlgorithmData#setAlgorithm(org.graffiti.plugin.algorithm.Algorithm)
	 */
	@Override
	public synchronized void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.HandlesAlgorithmData#getAlgorithm()
	 */
	@Override
	public Algorithm getAlgorithm() {
		return algorithm;
	}
}
