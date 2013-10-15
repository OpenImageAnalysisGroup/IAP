package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;

import org.StringManipulationTools;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableProcessingSubstance;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 */
public class ActionCreateImageConfigurationList extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentHeaderInterface> experiments;
	
	private final ArrayList<String> errors = new ArrayList<String>();
	private StringBuilder res;
	
	public ActionCreateImageConfigurationList(String tooltip) {
		super(tooltip);
	}
	
	public ActionCreateImageConfigurationList(MongoDB m, ArrayList<ExperimentHeaderInterface> experiments) {
		this("<html>" +
				"Create a table which lists experiment names and the used image unit configurations.<br>" +
				"(substance info field values)");
		this.m = m;
		this.experiments = experiments;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		status.setCurrentStatusText1("");
		status.setCurrentStatusText2("");
		errors.clear();
		res = new StringBuilder();
		res.append("<html><head>" +
				"<title>Experiment overview (list of imported image unit configurations)</title><body>" +
				"<h1>Experiment overview (list of imported image unit configurations)</h1>" +
				"<table border='1'>");
		res.append("<tr>" +
				"<th>Experiments</th>" +
				// "<th>Database ID</th>" +
				// "<th>Source</th>" +
				"<th>VIS</th>" +
				"<th>FLUO</th>" +
				"<th>NIR</th>" +
				"<th>IR</th>" +
				"<th>Other</th></tr>");
		try {
			final TreeMap<String, ArrayList<String>> config2names = new TreeMap<String, ArrayList<String>>();
			for (ExperimentHeaderInterface eh : ExperimentHeaderService.filterNewest(experiments)) {
				if (eh.getImportusergroup() != null && !eh.getImportusergroup().equalsIgnoreCase("ANALYSIS RESULTS")
						&& !eh.getImportusergroup().equalsIgnoreCase("TEMP")) {
					try {
						status.setCurrentStatusText1("Process experiment info");
						status.setCurrentStatusText2(eh.getExperimentName());
						final TreeSet<String> vis = new TreeSet<String>();
						final TreeSet<String> flu = new TreeSet<String>();
						final TreeSet<String> nir = new TreeSet<String>();
						final TreeSet<String> ir = new TreeSet<String>();
						final TreeSet<String> other = new TreeSet<String>();
						ThreadSafeOptions invalid = new ThreadSafeOptions();
						RunnableProcessingSubstance visitSubstanceObject = new RunnableProcessingSubstance() {
							@Override
							public void visit(SubstanceInterface substance) {
								if (substance.getInfo() == null || substance.getInfo().trim().isEmpty())
									return;
								ImageConfiguration conf = ImageConfiguration.get(substance.getName());
								switch (conf) {
									case FluoSide:
									case FluoTop:
										flu.add(substance.getInfo());
										break;
									case IrSide:
									case IrTop:
										ir.add(substance.getInfo());
										break;
									case NirSide:
									case NirTop:
										nir.add(substance.getInfo());
										break;
									case RgbSide:
									case RgbTop:
										vis.add(substance.getInfo());
										break;
									case Unknown:
										other.add(substance.getInfo());
										break;
								}
							}
						};
						m.visitExperiment(eh, status, visitSubstanceObject, false, null, null, null, invalid);
						StringBuilder config = new StringBuilder();
						config.append("<td>" + StringManipulationTools.getStringList(vis, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(flu, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(nir, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(ir, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(other, ", ") + "</td>");
						if (vis.size() + flu.size() + nir.size() + ir.size() + other.size() > 0) {
							String s = config.toString();
							if (!config2names.containsKey(s))
								config2names.put(s, new ArrayList<String>());
							config2names.get(s).add(eh.getExperimentName() +
									(
									eh.getSettings() != null && !eh.getSettings().isEmpty() ? "&nbsp;(settings)" : ""));
						}
					} catch (Exception err) {
						errors.add("Could not process " + eh.getExperimentName() + ": " + err.getMessage());
						err.printStackTrace();
					}
				}
			}
			for (String conf : config2names.keySet()) {
				ArrayList<String> experimentNames = config2names.get(conf);
				Collections.sort(experimentNames);
				res.append("<tr><td>" + StringManipulationTools.getStringList(experimentNames, "<br>") + "</td>" + conf + "</tr>");
			}
		} finally {
			res.append("</table></body></html>");
			if (!errors.isEmpty()) {
				String r = res.toString();
				res = new StringBuilder();
				r = r.replaceFirst("table border", "h2>Processing Errors (" + errors.size() + ")</h2>" + StringManipulationTools.getStringList(errors, "<br>")
						+ "<br><br><table border");
				res.append(r);
			}
		}
		status.setCurrentStatusText1("Processing finished");
		status.setCurrentStatusText2("");
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(res.toString());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Create Image Unit Configuration List";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-System-Search-64.png";
	}
	
}
