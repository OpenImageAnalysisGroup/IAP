/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;

import qmwi.kseg.som.DataSet;
import qmwi.kseg.som.SOMdataEntry;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SOMclusterAnalysisDoCluster extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Step 2: Compare mapping-data to centroids and set cluster ID";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.extension.Extension#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null || graph.getNodes().size() <= 0)
			throw new PreconditionException("No graph available or graph empty!");
		if (SOMplugin.getColumns() == null)
			throw new PreconditionException("SOM has not yet been trained!");
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "With this command, the trained SOM weight matrix is used for the<br>"
							+ "assignment of cluster IDs. For each node or edge the data mapping is analysed<br>"
							+ "and compared to the SOM centroids. The most similar centroid and the assignment<br>"
							+ "of the cluster ID is determined and used to set the node or edge cluster ID.<br>"
							+ "The result is not immediately visible (besides by looking at the graph elements<br>"
							+ "attributes). You may make the node cluster distribution visible with the menu<br>"
							+ "command Elements/Visualize Clusters";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
		// new BooleanParameter(false, "Create Data Nodes",
		// "If enabled, new nodes are created, containing all of the normalised data from the corresponding clusters.")
		};
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		super.setParameters(params);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		SOMclusterService mcs = new SOMclusterService(getSelectedOrAllGraphElements());
		
		BackgroundTaskHelper bth = new BackgroundTaskHelper(mcs, mcs, "Clustering with trained SOM",
							"Clustering with trained SOM", false, false);
		bth.startWork(this);
	}
}

class SOMclusterService implements BackgroundTaskStatusProvider, BackgroundTaskStatusProviderSupportingExternalCall,
					Runnable {
	
	double statusDouble = -1d;
	boolean pleaseStop = false;
	String status1, status2;
	
	Collection<GraphElement> selection;
	
	SOMclusterService(Collection<GraphElement> selection) {
		this.selection = selection;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusValue()
	 */
	public int getCurrentStatusValue() {
		return (int) statusDouble;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusValueFine()
	 */
	public double getCurrentStatusValueFine() {
		return statusDouble;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusMessage1()
	 */
	public String getCurrentStatusMessage1() {
		return status1;
	}
	
	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#
	 * getCurrentStatusMessage2()
	 */
	public String getCurrentStatusMessage2() {
		return status2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.BackgroundTaskStatusProvider#pleaseStop
	 * ()
	 */
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (checkStop())
			return;
		status1 = "Initialize...";
		status2 = "Use trained dataset...";
		DataSet mydataset = SOMplugin.getDataSet(false);
		boolean returnNaN = mydataset.getTrainedWithReturnNaN();
		String cols[] = SOMplugin.getColumns();
		String[] groups = SOMplugin.initDataSetWithSelection(mydataset, selection, returnNaN, SOMplugin
							.getLastUseAverageSetting());
		if (cols.length != groups.length) {
			HashSet<String> g1 = new HashSet<String>();
			for (String s : cols)
				g1.add(s);
			HashSet<String> g2 = new HashSet<String>();
			for (String s : groups)
				g2.add(s);
			ErrorMsg.addErrorMessage("Invalid Dataset. SOM training data differs from work data.");
			status1 = "Error";
			status2 = "SOM training data differs from work data.";
			System.out.println("New, not in training-set:");
			for (String s : groups) {
				if (!g1.contains(s))
					System.out.println(s);
			}
			System.out.println("Missing, included in training-set:");
			for (String s : cols) {
				if (!g2.contains(s))
					System.out.println(s);
			}
			return;
		}
		statusDouble = -1;
		Vector<SOMdataEntry>[] result = mydataset.trainOrUseSOM(false, -1, cols, 1, this, 0);
		status1 = "Assign cluster info...";
		String sz = "[";
		for (int i = 0; i < result.length; i++) {
			sz += result[i].size();
			if (i < result.length - 1)
				sz += ";";
		}
		sz += "]";
		status2 = "Result-Groups " + sz;
		for (int i = 0; i < result.length; i++) {
			for (SOMdataEntry sde : result[i]) {
				GraphElement n = (GraphElement) sde.getUserData();
				AttributeHelper.deleteAttribute(n, "som", "diff*");
				ArrayList<Double> differences = sde.getDifferencesToCentroids();
				double minDiff = sde.getMinDiff();
				AttributeHelper.setAttribute(n, "som", "diff_minimum_", minDiff);
				int idx = 0;
				double sumDiff = 0;
				for (Double d : differences) {
					idx++;
					AttributeHelper.setAttribute(n, "som", "diff_centroid_" + idx, d);
					sumDiff += d;
				}
				AttributeHelper.setAttribute(n, "som", "diff_sum_", sumDiff);
				int tc = mydataset.getSOMmap().getTargetClusterForNode(i);
				NodeTools.setClusterID(n, new Integer(tc).toString());
			}
		}
		status1 = "Cluster IDs have been assigned";
		statusDouble = 100;
		if (checkStop())
			return;
	}
	
	private boolean checkStop() {
		if (pleaseStop) {
			status1 = "SOM-Analysis not complete: aborted";
			status2 = "";
			statusDouble = 100;
		}
		return pleaseStop;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pluginWaitsForUser()
	 */
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider
	 * #pleaseContinueRun()
	 */
	public void pleaseContinueRun() {
		// empty
	}
	
	public void setCurrentStatusValue(int value) {
		statusDouble = value;
	}
	
	public void setCurrentStatusValueFine(double value) {
		statusDouble = value;
	}
	
	public boolean wantsToStop() {
		return checkStop();
	}
	
	public void setCurrentStatusText1(String status) {
		status1 = status;
	}
	
	public void setCurrentStatusText2(String status) {
		status2 = status;
	}
	
	/*
	 * (non-Javadoc)
	 * @seeorg.BackgroundTaskStatusProviderSupportingExternalCall#
	 * setCurrentStatusValueFineAdd(double)
	 */
	public void setCurrentStatusValueFineAdd(double smallProgressStep) {
		statusDouble += smallProgressStep;
	}
}
