/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.FeatureSet;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.DatabaseBasedLabelReplacementService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggFTPinfo;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class InterpreteLabelNamesAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Interpret Database-Identifiers...";
	}
	
	@Override
	public String getCategory() {
		return null;// "menu.edit";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null || graph.getNumberOfNodes() <= 0)
			throw new PreconditionException("No active graph, or graph contains no nodes!");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		exchangeLabel(getSelectedOrAllNodes());
	}
	
	void exchangeLabel(final Collection<Node> nodes) {
		
		boolean compoundNameToID = false;
		boolean compoundIDtoName = false;
		boolean ecNumberToName = false;
		// boolean ecNumberToSynonyme=false;
		boolean ecNameOrSynonymeToECnumber = false;
		boolean reactionNumberToName = false;
		boolean reactionNameToNo = false;
		boolean processKeggId2EcAnnotaion = false;
		boolean koId2koName = false;
		
		boolean briteKO2geneName = true;
		boolean briteKO2ecName = false;
		
		JPanel guiPanel = new JPanel();
		
		// **********************************
		// * HOW TO USE TEXT COL 1-3 * R1
		// * DESC | BUTTON | DESC * R2
		// * DESC | BUTTON | DESC * R3
		// * DESC | BUTTON | DESC * R4
		// * DESC | BUTTON | DESC * R5
		// * CHECKBOX COL 1-3 * R6
		// * Help * R7
		// **********************************
		double border = 5;
		double[][] size =
		{ { border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border }, // Columns
				{ border,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												border }
		}; // Rows
		
		guiPanel.setLayout(new TableLayout(size));
		
		JButton changeButton1 = getChangeButton(KeggFTPinfo.keggFTPavailable, false);
		JButton changeButton2 = getChangeButton(false, false);
		JButton changeButton3 = getChangeButton(false, true);
		JButton changeButton4 = getChangeButton(KeggFTPinfo.keggFTPavailable, true);
		JButton changeButton5 = getChangeButton(KeggFTPinfo.keggFTPavailable, true);
		JButton changeButton6 = getChangeButton(true, true);
		JButton changeButton7 = getChangeButton(false, true);
		
		if (KeggFTPinfo.keggFTPavailable) {
			guiPanel.add(new JLabel("Compound IDs  "), "1,1");
			guiPanel.add(new JLabel("  Compound Names (<-> Comp. File-DB)"), "3,1");
			guiPanel.add(changeButton1, "2,1");
		}
		
		guiPanel.add(new JLabel("EC Numbers  "), "1,2 r");
		guiPanel.add(new JLabel("  Substance Names (<-> Enz. File-DB)"), "3,2");
		guiPanel.add(changeButton2, "2,2");
		
		guiPanel.add(new JLabel("Reaction No.~ "), "1,3 r");
		guiPanel.add(new JLabel("  Enzyme IDs (-> SOAP*)"), "3,3");
		guiPanel.add(changeButton3, "2,3");
		
		if (KeggFTPinfo.keggFTPavailable) {
			guiPanel.add(new JLabel("KO/Gene ID# "), "1,4 r");
			guiPanel.add(new JLabel("  Enzyme IDs (-> KO File-DB)"), "3,4");
			guiPanel.add(changeButton4, "2,4");
			
			guiPanel.add(new JLabel("KO IDs  "), "1,5 r");
			guiPanel.add(new JLabel("  KO Name (-> KO File-DB)"), "3,5");
			guiPanel.add(changeButton5, "2,5");
		} else {
			guiPanel.add(new JLabel("IDs of Orthologs (KO)  "), "1,4 r");
			guiPanel.add(new JLabel("  Gene Name (-> KEGG BRITE DB)"), "3,4");
			guiPanel.add(changeButton6, "2,4");
			
			guiPanel.add(new JLabel("IDs of Othologs (KO)  "), "1,5 r");
			guiPanel.add(new JLabel("  Enzyme IDs (-> KEGG BRITE DB)"), "3,5");
			guiPanel.add(changeButton7, "2,5");
		}
		
		JCheckBox increaseSizeCheckBox = new JCheckBox("<html>" +
							"Increase node size if label does not fit<br>" +
							"(Nodes with Ellipse-Shape are not processed)");
		guiPanel.add(increaseSizeCheckBox, "1,6,3,6");
		
		JCheckBox useShortNameCheckBox = new JCheckBox("Use shortest synonym for labeling");
		useShortNameCheckBox.setSelected(false);
		guiPanel.add(
							TableLayout.getSplitVertical(
												useShortNameCheckBox,
												new JLabel("<html><small>" +
																	"<br>* SOAP based renaming-operation may take a longer time" +
																	"<br>~ Requires KEGG Reaction node attribute" +
																	(KeggFTPinfo.keggFTPavailable ?
																			"<br># Requires KEGG ID attribute matching a KO/KO-Gene entry and KO-EC annotation" : "")),
												TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED), "1,7,3,7");
		
		String helpText;
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			helpText = "<html><font color=\"gray\"><small><small>Open help:&nbsp;&nbsp;&nbsp;";
		else
			helpText = "";
		
		guiPanel.add(TableLayout.get3Split(
							new JLabel(""),
							new JLabel(helpText),
							FolderPanel.getHelpButton(
												JLabelJavaHelpLink.getHelpActionListener("nodesmenu_interpret"),
												guiPanel.getBackground()),
							TableLayoutConstants.FILL,
							TableLayoutConstants.PREFERRED,
							TableLayoutConstants.PREFERRED),
							"1,8,3,8");
		
		guiPanel.revalidate();
		
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(),
							guiPanel,
							"Interpret and replace Node-Labels",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
			compoundIDtoName = changeButton1.getText().contains(">");
			compoundNameToID = changeButton1.getText().contains("<");
			ecNumberToName = changeButton2.getText().contains(">");
			ecNameOrSynonymeToECnumber = changeButton2.getText().contains("<");
			reactionNumberToName = changeButton3.getText().contains(">");
			reactionNameToNo = changeButton3.getText().contains("<");
			processKeggId2EcAnnotaion = changeButton4.getText().contains(">");
			koId2koName = changeButton5.getText().contains(">");
			briteKO2geneName = changeButton6.getText().contains(">");
			briteKO2ecName = changeButton7.getText().contains(">");
			
			DatabaseBasedLabelReplacementService mwt = new DatabaseBasedLabelReplacementService(
								nodes,
								compoundNameToID,
								compoundIDtoName,
								ecNumberToName,
								ecNameOrSynonymeToECnumber,
								reactionNumberToName,
								reactionNameToNo,
								processKeggId2EcAnnotaion,
								koId2koName,
								briteKO2geneName,
								briteKO2ecName,
								increaseSizeCheckBox.isSelected(),
								useShortNameCheckBox.isSelected(),
								false, false, null);
			BackgroundTaskHelper bth = new BackgroundTaskHelper(mwt, mwt,
								"Label Mapping", "Label Mapping", true, false);
			bth.startWork(this);
		}
	}
	
	private JButton getChangeButton(boolean enabled, final boolean disableBackState) {
		final String dir1 = "       <<<       ";
		final String dir2 = "       >>>       ";
		final String dir3 = "unchanged";
		JButton res = new JButton(dir2);
		if (!enabled)
			res.setText(dir3);
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				if (src.getText().equals(dir2)) {
					if (!disableBackState)
						src.setText(dir1);
					else
						src.setText(dir3);
				} else
					if (src.getText().equals(dir1))
						src.setText(dir3);
					else
						src.setText(dir2);
			}
		});
		return res;
	}
}
