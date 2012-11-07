/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ProjectEntity;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TabDBE;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class ExtractMappingDataAlgorithm extends AbstractAlgorithm {
	
	public String getName() {
		return "Extract Mapped Data";
	}
	
	@Override
	public String getDescription() {
		return "<html>Gathers all experimental data from the selected<br>" +
				"graph elements and put this data in the \"Experiments\" tab.";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	private boolean onlyOne = false;
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(onlyOne, "Extract single experiment",
							"<html>If enabled, all experiments will be merged together") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int idx = 0;
		onlyOne = ((BooleanParameter) params[idx++]).getBoolean();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		// graph.getListenerManager().transactionStarted(this);
		final Collection<GraphElement> workNodes = getSelectedOrAllGraphElements();
		final BackgroundTaskStatusProviderSupportingExternalCall status =
							new BackgroundTaskStatusProviderSupportingExternalCallImpl("Initialize...", "");
		if (workNodes.size() > 0)
			BackgroundTaskHelper.issueSimpleTask(getName(), "Initialize...", new Runnable() {
				public void run() {
					status.setCurrentStatusValue(-1);
					status.setCurrentStatusText1("Extracting mapped data...");
					status.setCurrentStatusText2("");
					Graph g = workNodes.iterator().next().getGraph();
					g.getListenerManager().transactionStarted(ExtractMappingDataAlgorithm.this);
					try {
						status.setCurrentStatusText2("Getting mapped data from elements");
						status.setCurrentStatusValue(0);
						
						for (ExperimentInterface e : getExperiments(workNodes, onlyOne, status)) {
							String expname = e.getName();
							if (expname == null) {
								ErrorMsg.addErrorMessage("Error occured: could not determine experiment name!");
								expname = "unknown";
							}
							TabDBE.addOrUpdateExperimentPane(new ProjectEntity(expname, e));
						}
						
						status.setCurrentStatusValue(-1);
					} finally {
						g.getListenerManager().transactionFinished(ExtractMappingDataAlgorithm.this, false, status);
						status.setCurrentStatusValue(100);
					}
					if (status.wantsToStop())
						status.setCurrentStatusText1("Processing aborted");
					else
						status.setCurrentStatusText1("Finished");
					status.setCurrentStatusText2("");
				}
				
			}, null, status);
		
	}
	
	public static Collection<ExperimentInterface> getExperiments(Collection<GraphElement> workNodes, boolean onlyOne,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		HashMap<String, ExperimentInterface> allData = new HashMap<String, ExperimentInterface>();
		for (GraphElement ge : workNodes) {
			if (status != null && status.wantsToStop())
				break;
			GraphElementHelper geh = new GraphElementHelper(ge);
			for (ExperimentInterface e : geh.getDataMappings().split()) {
				if (allData.containsKey(e.getName()))
					allData.get(e.getName()).addAll(e);
				else
					allData.put(e.getName(), e);
			}
		}
		if (status != null && status.wantsToStop())
			return new ArrayList<ExperimentInterface>();
		
		if (status != null)
			status.setCurrentStatusText2("Loading experiments into Experiment-Tab");
		if (onlyOne) {
			ExperimentInterface all = new Experiment();
			for (ExperimentInterface e : allData.values())
				all.addAndMerge(e);
			ArrayList<ExperimentInterface> lst = new ArrayList<ExperimentInterface>();
			lst.add(all.clone());
			return lst;
		} else
			return allData.values();
		
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
}
