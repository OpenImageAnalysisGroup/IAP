/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.util.Collection;

import org.BackgroundTaskStatusProvider;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import qmwi.kseg.som.DataSet;
import qmwi.kseg.som.Map;
import qmwi.kseg.som.Tools;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

public class SOMservice implements BackgroundTaskStatusProvider, BackgroundTaskStatusProviderSupportingExternalCall,
					Runnable {
	
	double statusDouble = -1;
	boolean pleaseStop = false;
	String status1, status2;
	
	int numberOfNeuronsParm = 6;
	int widthOfSOMparm = 0; // 0=quadratic
	double maxNeighbourHoodParm = 100;
	int decreaseNeighbourhoodAfterXiterationsParam = 0; // 0=off
	int typeOfNeighbourhoodFunctionParam = 1;
	int numberLearnIterationsParam = 1000;
	double betaParam = 0.1;
	double gammaParam = 2;
	Collection<GraphElement> selection;
	boolean returnNaN = false;
	private final boolean useSampleAverageValues;
	private final boolean showCentroidNodes;
	private final Graph optSrcGraph;
	
	SOMservice(int numberOfNeuronsParm, int widthOfSOMparm, double maxNeighbourHoodParm,
						int decreaseNeighbourhoodAfterXiterationsParam, int typeOfNeighbourhoodFunctionParam,
						int numberLearnIterationsParam, double betaParam, double gammaParam, Collection<GraphElement> selection,
						boolean returnNaN, boolean useSampleAverageValues, boolean showCentroidNodes, Graph optSrcGraph) {
		
		this.numberOfNeuronsParm = numberOfNeuronsParm;
		this.widthOfSOMparm = widthOfSOMparm;
		this.maxNeighbourHoodParm = maxNeighbourHoodParm;
		this.decreaseNeighbourhoodAfterXiterationsParam = decreaseNeighbourhoodAfterXiterationsParam;
		this.typeOfNeighbourhoodFunctionParam = typeOfNeighbourhoodFunctionParam;
		this.numberLearnIterationsParam = numberLearnIterationsParam;
		this.betaParam = betaParam;
		this.gammaParam = gammaParam;
		this.selection = selection;
		this.returnNaN = returnNaN;
		this.useSampleAverageValues = useSampleAverageValues;
		this.showCentroidNodes = showCentroidNodes;
		this.optSrcGraph = optSrcGraph;
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
	
	public void setCurrentStatusValue(int value) {
		this.statusDouble = value;
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
		status2 = "Prepare Train-Dataset...";
		
		DataSet mydataset = SOMplugin.getDataSet(true);
		String[] groups = SOMplugin.initDataSetWithSelection(mydataset, selection, returnNaN, useSampleAverageValues);
		
		if (groups.length <= 0) {
			MainFrame.showMessageDialog(
								"<html>" + "Can not start SOM processing.<br>" + "No experimental data available.", "Missing Data");
			status1 = "Prepare Train-Dataset...";
			status2 = "No data available!";
			return;
		}
		
		int w = widthOfSOMparm;
		if (w <= 0) {
			w = Tools.getBreite(numberOfNeuronsParm);
		}
		if (checkStop())
			return;
		mydataset.initSOM(numberOfNeuronsParm, w, maxNeighbourHoodParm, decreaseNeighbourhoodAfterXiterationsParam,
							mydataset.inputNeuronsNeededFor(null, groups), returnNaN);
		mydataset.setBetaAndGamma(betaParam, gammaParam);
		if (checkStop())
			return;
		status1 = "Train SOM using " + mydataset.inputNeuronsNeededFor(null, groups) + " Input-Neurons";
		status2 = "";
		SOMplugin.setColumns(groups);
		mydataset.trainOrUseSOM(true, typeOfNeighbourhoodFunctionParam, groups, numberLearnIterationsParam, this,
							mydataset.getDataSetSize());
		status2 = "Training finished.";
		statusDouble = 100;
		if (checkStop())
			return;
		Map som = mydataset.getSOMmap();
		String cols[] = SOMplugin.getColumns();
		SOMguiHelper.showSOMcentroidsAndClusterAssignmentSettings(som, cols, optSrcGraph);
		if (showCentroidNodes) {
			Graph g = SOMguiHelper.createCentroidNodesGraph(som, cols);
			g.setModified(false);
			g.setName("SOM Centroids");
			if (optSrcGraph != null) {
				Vector2d max = NodeTools.getMaximumXY(optSrcGraph.getNodes(), 1, 0, 0, true);
				for (Node n : g.getNodes()) {
					NodeHelper nh = new NodeHelper(n);
					nh.setPosition(nh.getX(), nh.getY() + max.y + 50);
					optSrcGraph.addNodeCopy(n);
				}
				GraphHelper.issueCompleteRedrawForGraph(optSrcGraph);
			} else {
				MainFrame.getInstance().showGraph(g, null);
			}
		}
	}
	
	/**
	 * @return
	 */
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
