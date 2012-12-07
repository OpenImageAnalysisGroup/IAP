/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 14.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JFileChooser;
import javax.xml.rpc.ServiceException;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.OrganismEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.TabKegg;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class CreateOrgSpecificSuperGraphsAlgorithm extends AbstractAlgorithm {
	private boolean checkOrthologs = true;
	private boolean checkEnzymes = false;
	private boolean checkGlycans = false;
	private boolean checkCompounds = false;
	private boolean convertKOsToGenes = true;
	private OrganismEntry[] organismSelection;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(checkOrthologs, "Check Orthologs", "If selected, the organism specific ortholog information is processed"),
							new BooleanParameter(convertKOsToGenes, "Convert KOs to Genes", "(requires 'Check Orthologs')"),
							new BooleanParameter(checkEnzymes, "Check Enzymes", "If selected, the organism specific enzymes are enumerated"),
							new BooleanParameter(checkGlycans, "Check Glycans", "If selected, the organism specific glycans are enumerated"),
							new BooleanParameter(checkCompounds, "Check Compounds", "If selected, the organism specific compounds are enumerated") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		checkOrthologs = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		convertKOsToGenes = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		checkEnzymes = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		checkGlycans = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		checkCompounds = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		organismSelection = null;
		try {
			KeggHelper kegg = new KeggHelper();
			Collection<OrganismEntry> organisms = kegg.getOrganisms();
			organismSelection = TabKegg.getKEGGorganismFromUser(organisms);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public String getName() {
		return null; // start from kegg tab 2
		// return "Create ALL Organism-Specific SG from current SG";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("The active graph needs to be a KEGG reference super-pathway!");
		super.check();
	}
	
	public void execute() {
		if (organismSelection == null || organismSelection.length <= 0) {
			MainFrame.showMessageDialog("No organism has been selected. Processing aborted.", "Information");
		}
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select Target Folder");
		fc.setApproveButtonText("Set Target-Folder");
		int returnVal = fc.showOpenDialog(MainFrame.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String targetFolder = file.getPath();
			AllSuperGraphsCreator workTask = new AllSuperGraphsCreator(graph, targetFolder, organismSelection,
								checkOrthologs, checkEnzymes, checkGlycans, checkCompounds, convertKOsToGenes);
			BackgroundTaskHelper.issueSimpleTask("Organism Specific Super Graphs", "Please wait...", workTask, null, workTask);
		}
	}
}
