/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.ErrorMsg;
import org.FolderPanel;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

public class KGMLerrorWindow extends JDialog {
	private Collection<Gml2PathwayWarningInformation> warnings;
	private Collection<Gml2PathwayErrorInformation> errors;
	private FolderPanel panelWarning;
	private FolderPanel panelError;
	
	public KGMLerrorWindow(Collection<Gml2PathwayWarningInformation> warnings, Collection<Gml2PathwayErrorInformation> errors) {
		super(MainFrame.getInstance());
		this.warnings = warnings;
		this.errors = errors;
	}
	
	@Override
	protected void dialogInit() {
		super.dialogInit();
		
		setResizable(false);
		
		setTitle("KGML Conversion Errors / Warnings");
		setLayout(TableLayout.getLayout(
							TableLayoutConstants.PREFERRED,
							new double[] {
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED
						}));
		
		panelWarning = createFolderPaneWarnings(true);
		panelError = createFolderPaneErrors(true);
		updatePanels(panelWarning, panelError, warnings, errors);
		add(panelWarning, "0,0");
		add(panelError, "0,1");
		add(getButtonCmdPane(), "0,2");
		pack();
		setModal(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updatePanels(panelWarning, panelError, warnings, errors);
			}
		});
	}
	
	private Component getButtonCmdPane() {
		JButton updateB = new JButton("Refresh");
		updateB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				warnings = new ArrayList<Gml2PathwayWarningInformation>();
				errors = new ArrayList<Gml2PathwayErrorInformation>();
				HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
				try {
					Graph graph = MainFrame.getInstance().getActiveSession().getGraph();
					Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
					updatePanels(panelWarning, panelError, warnings, errors); // process warnings and errors
					pack();
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		
		JButton okB = new JButton("Close");
		okB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				warnings = null;
				errors = null;
				setVisible(false);
				dispose();
			}
		});
		return TableLayout.get3Split(okB, null, updateB,
							TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
	}
	
	public static void updatePanels(
						FolderPanel panelWarning, FolderPanel panelError,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors) {
		panelWarning.clearGuiComponentList();
		if (warnings == null || warnings.size() <= 0) {
			panelWarning.addGuiComponentRow(new JLabel(""), new JLabel("No Warnings"), false);
			panelWarning.setTitle("No Warnings");
		} else {
			panelWarning.addGuiComponentRow(new JLabel("<html>Description<hr>"), new JLabel("<html>Related Graphelement<hr>"), false);
			panelWarning.setTitle("Warnings (" + warnings.size() + ")");
			for (Gml2PathwayWarningInformation wi : warnings) {
				panelWarning.addGuiComponentRow(
									FolderPanel.getBorderedComponent(
														new JLabel(wi.getWarning().toString()),
														2, 0, 0, 5
														),
									new GraphElementSelectionLabel(wi.getCausingGraphElement()),
									false);
			}
		}
		panelWarning.layoutRows();
		
		panelError.clearGuiComponentList();
		if (errors == null || errors.size() <= 0) {
			panelError.addGuiComponentRow(new JLabel(""), new JLabel("No Errors"), false);
			panelError.setTitle("No Errors");
		} else {
			panelError.addGuiComponentRow(new JLabel("<html>Description<hr>"), new JLabel("<html>Related Graphelement(s)<hr>"), false);
			panelError.setTitle("Errors (" + errors.size() + ")");
			for (Gml2PathwayErrorInformation er : errors) {
				panelError.addGuiComponentRow(
									FolderPanel.getBorderedComponent(
														new JLabel(er.getError().toString()),
														2, 0, 0, 5
														),
									new GraphElementSelectionLabel(er.getCausingGraphElements()),
									false);
			}
		}
		panelError.layoutRows();
	}
	
	public static FolderPanel createFolderPaneWarnings(boolean usedInDialog) {
		FolderPanel result = new FolderPanel("Warnings", true, true, false, null);
		if (usedInDialog)
			result.addCollapseListenerDialogSizeUpdate();
		result.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 5, 5);
		result.setMaximumRowCount(5);
		result.layoutRows();
		return result;
	}
	
	public static FolderPanel createFolderPaneErrors(boolean usedInDialog) {
		FolderPanel result = new FolderPanel("Errors", true, true, false, null);
		if (usedInDialog)
			result.addCollapseListenerDialogSizeUpdate();
		result.addCollapseListenerDialogSizeUpdate();
		result.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 5, 5);
		result.setMaximumRowCount(5);
		result.layoutRows();
		return result;
	}
	
	private static final long serialVersionUID = 1L;
	
}
