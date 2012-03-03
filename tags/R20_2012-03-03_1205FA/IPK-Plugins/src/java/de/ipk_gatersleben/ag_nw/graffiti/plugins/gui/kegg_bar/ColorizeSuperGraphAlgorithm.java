/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 14.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.rpc.ServiceException;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.OrganismEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.TabKegg;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class ColorizeSuperGraphAlgorithm extends AbstractAlgorithm {
	
	OrganismEntry organismSelection;
	private boolean checkOrthologs = true;
	private boolean checkEnzymes = false;
	private boolean checkGlycans = false;
	private boolean checkCompounds = false;
	
	public String getName() {
		return null; // start from kegg tab 2
		// return "Create Organism-Specific KEGG Super-Graph";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"This algorithm enumerates a list of pathways, which are the source of the active<br>" +
							"(super)pathway. It then uses the KEGG SOAP API to enumerate the specified elements<br>" +
							"for a given organism and marks elements that are returned for the selected organism.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new BooleanParameter(checkOrthologs, "Check Orthologs (KO IDs)", "If selected, the organism specific KO based entries are processed"),
							new BooleanParameter(checkEnzymes, "Check Enzymes", "If selected, the organism specific Enzyme ID based entries are processed"),
							new BooleanParameter(checkGlycans, "Check Glycans", "If selected, the organism specific Glycan ID based entries are processed"),
							new BooleanParameter(checkCompounds, "Check Compounds", "If selected, the organism specific Compound ID based entries are processed") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		checkOrthologs = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		checkEnzymes = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		checkGlycans = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
		checkCompounds = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("The active graph needs to be a KEGG reference pathway!");
		
		super.check();
	}
	
	public void execute() {
		final Collection<OrganismEntry> organisms = new ArrayList<OrganismEntry>();
		try {
			KeggHelper h = new KeggHelper();
			organisms.addAll(h.getOrganisms());
		} catch (IOException er) {
			ErrorMsg.addErrorMessage(er);
		} catch (ServiceException er) {
			ErrorMsg.addErrorMessage(er);
		}
		if (organisms == null)
			return;
		OrganismEntry[] organismSelections = TabKegg.getKEGGorganismFromUser(organisms);
		if (organismSelections == null)
			return;
		if (organismSelections.length < 1) {
			MainFrame.showMessageDialog(
								"No organism has been selected. Operation aborted.",
								"Information");
			return;
		}
		if (organismSelections.length > 1) {
			MainFrame.showMessageDialog(
								"More than one organism has been selected, processing the first: " + organismSelections[0].toString(),
								"Information");
		}
		organismSelection = organismSelections[0];
		
		final ArrayList<String> coveredMaps = new ArrayList<String>();
		for (Node n : graph.getNodes()) {
			NodeHelper nh = new NodeHelper(n);
			String keggID = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", "", "");
			if (checkOrthologs)
				if (keggID.startsWith("ko:"))
					AttributeHelper.setAttribute(n, "kegg", "present", "not found");
			if (checkEnzymes)
				if (keggID.startsWith("ec:"))
					AttributeHelper.setAttribute(n, "kegg", "present", "not found");
			if (checkGlycans)
				if (keggID.startsWith("glycan:"))
					AttributeHelper.setAttribute(n, "kegg", "present", "not found");
			if (checkCompounds)
				if (keggID.startsWith("cpd:"))
					AttributeHelper.setAttribute(n, "kegg", "present", "not found");
			String cluster = nh.getClusterID(null);
			if (cluster != null) {
				if (cluster.indexOf("map") >= 0) {
					cluster = cluster.substring(cluster.indexOf("map"));
					cluster = StringManipulationTools.stringReplace(cluster, ".xml", "");
					String sn = organismSelection.getShortName();
					cluster = StringManipulationTools.stringReplace(cluster, "map", sn);
					if (!coveredMaps.contains(cluster))
						coveredMaps.add(cluster);
				} else
					if (cluster.indexOf("ko") >= 0) {
						cluster = cluster.substring(cluster.indexOf("ko"));
						cluster = StringManipulationTools.stringReplace(cluster, ".xml", "");
						String sn = organismSelection.getShortName();
						cluster = StringManipulationTools.stringReplace(cluster, "ko", sn);
						if (!coveredMaps.contains(cluster))
							coveredMaps.add(cluster);
					}
			}
			String lbl = nh.getLabel();
			if (lbl != null && lbl.contains("TITLE:")) {
				if (keggID != null) {
					if (keggID.indexOf("map") >= 0) {
						keggID = keggID.substring(keggID.indexOf("map"));
						String sn = organismSelection.getShortName();
						keggID = StringManipulationTools.stringReplace(keggID, "map", sn);
						if (!coveredMaps.contains(keggID))
							coveredMaps.add(keggID);
					} else
						if (keggID.indexOf("ko") >= 0) {
							keggID = keggID.substring(keggID.indexOf("ko"));
							String sn = organismSelection.getShortName();
							keggID = StringManipulationTools.stringReplace(keggID, "ko", sn);
							if (!coveredMaps.contains(keggID))
								coveredMaps.add(keggID);
						}
				}
			}
		}
		final Graph ggg = graph;
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Please wait...", "");
		BackgroundTaskHelper.issueSimpleTask("Enumerate organism specific elements", "Please wait...",
							new Runnable() {
								public void run() {
									status.setCurrentStatusText1("Process covered maps (" + coveredMaps.size() + ")...");
									double workLoad = coveredMaps.size();
									double progress = 0;
									String covering = "";
									if (checkOrthologs)
										covering += ", orthologs";
									if (checkEnzymes)
										covering += ", enzymes";
									if (checkGlycans)
										covering += ", glycans";
									if (checkCompounds)
										covering += ", compounds";
									if (covering.startsWith(", "))
										covering = covering.substring(", ".length());
									for (String map : coveredMaps) {
										status.setCurrentStatusText1("Process covered maps (" + (int) (progress + 1) + "/" + coveredMaps.size() + ")...");
										status.setCurrentStatusText2("Request and process " + covering + " of map " + map);
										KeggService.colorizeEnzymesGlycansCompounds(ggg, map, KeggService.getDefaultEnzymeColor(), false,
															checkOrthologs, checkEnzymes, checkGlycans, checkCompounds, checkOrthologs);
										progress = progress + 1;
										status.setCurrentStatusValueFine(100d * progress / workLoad);
										if (status.wantsToStop()) {
											break;
										}
									}
									if (status.wantsToStop()) {
										status.setCurrentStatusValueFine(100d);
										status.setCurrentStatusText1("Processing incomplete!");
										status.setCurrentStatusText2("Operation aborted.");
									} else {
										status.setCurrentStatusText1("Processing complete!");
										status.setCurrentStatusText2("");
									}
								}
							}, null, status);
	}
}
