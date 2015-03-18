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
import java.util.TreeSet;

import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class CreateDistanceMatrixAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
			return "Create Distance Matrix from Data Points";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		DistanceMatrix dm = new DistanceMatrix();
		try {
			Collection<Node> workNodes = getSelectedOrAllNodes();
			int i = 0;
			for (Node n1 : workNodes) {
				NodeHelper nh1 = new NodeHelper(n1);
				int j = 0;
				for (Node n2 : workNodes) {
					NodeHelper nh2 = new NodeHelper(n2);
					ArrayList<String> calculationHistory = new ArrayList<String>();
					String lbl1 = nh1.getLabel();
					String lbl2 = nh2.getLabel();
					int distance = calculateDistance(nh1.getDatasetTable(), nh2.getDatasetTable(), calculationHistory, lbl1, lbl2);
					dm.setDistanceInformation(lbl1, lbl2, i, j, distance, calculationHistory);
					j++;
				}
				i++;
			}
			dm.saveDMF();
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"<h2>This algorithm is currently included only for testing purposes:</h2>" +
							"This algorithm calculates a distance matrix from the selected or all graph nodes<br>" +
							"with mapping data. Currently the data mappings may not contain time series data<br>" +
							"or more than one replicate value. This situation will be supported with future<br>" +
							"updates. The differences between the datapoints are specially weighted:<br>" +
							"A detailed description and a GUI for changing the difference-calcualtion-<br>" +
							"settings will be provided with future updates.";
	}
	
	private int calculateDistance(DataSetTable datasetTable1, DataSetTable datasetTable2, ArrayList<String> history, String lbl1, String lbl2) {
		TreeSet<String> knownSubstanceNames = new TreeSet<String>();
		int result = 0;
		for (DataSetRow dsr1 : datasetTable1.getRows()) {
			String v = dsr1.rowLabel;
			if (!knownSubstanceNames.contains(v))
				knownSubstanceNames.add(v);
		}
		for (DataSetRow dsr2 : datasetTable2.getRows()) {
			String v = dsr2.rowLabel;
			if (!knownSubstanceNames.contains(v))
				knownSubstanceNames.add(v);
		}
		for (String substance : knownSubstanceNames) {
			ArrayList<Double> values1 = new ArrayList<Double>();
			for (DataSetRow dsr1 : datasetTable1.getRows()) {
				String v = dsr1.rowLabel;
				if (v.equals(substance))
					values1.add(dsr1.value);
			}
			ArrayList<Double> values2 = new ArrayList<Double>();
			for (DataSetRow dsr2 : datasetTable2.getRows()) {
				String v = dsr2.rowLabel;
				if (v.equals(substance))
					values2.add(dsr2.value);
			}
			if (values1.size() > 1 || values2.size() > 1) {
				history.add("More than one value per line for line " + substance);
				ErrorMsg.addErrorMessage("More than one value per line for line " + substance);
				return Integer.MAX_VALUE;
			} else
				if (values1.size() == 0 && values2.size() == 0) {
					history.add("Internal Error: no value per line for line " + substance);
					ErrorMsg.addErrorMessage("Internal Error: no value per line for line " + substance);
					return Integer.MAX_VALUE;
				} else
					if (values1.size() == 0) {
						if (lbl1.equalsIgnoreCase("Brenda/Brenda") || lbl2.equalsIgnoreCase("Brenda/Brenda"))
							;//
						else
							result++;
					} else
						if (values2.size() == 0) {
							if (lbl1.equalsIgnoreCase("Brenda/Brenda") || lbl2.equalsIgnoreCase("Brenda/Brenda"))
								;//
							else
								result++;
						} else
							if (values1.size() == 1 && values2.size() == 1) {
								double v1 = values1.iterator().next();
								double v2 = values2.iterator().next();
								boolean isNull1 = Math.abs(v1) < 0.00000000000000001d;
								boolean isNull2 = Math.abs(v2) < 0.00000000000000001d;
								if (isNull1 && isNull2) {
									result = result + 0;
									history.add(lbl1 + " / " + lbl2 + ": " + substance + " not found in both lines # +0");
								} else {
									if (isNull1 && v2 > 0) {
										result = result + 1;
										history.add(lbl1 + " / " + lbl2 + ": " + substance + " not found in line 1, greater than 0 in line 2 # +1");
									} else
										if (isNull1 && v2 < 0) {
											result = result + 1;
											history.add(lbl1 + " / " + lbl2 + ": " + substance + " not found in line 1, smaller than 0 in line 2 # +1");
										} else
											if (isNull2 && v1 < 0) {
												result = result + 1;
												history.add(lbl1 + " / " + lbl2 + ": " + substance + " not found in line 2, smaller than 0 in line 1 # +1");
											} else
												if (isNull2 && v1 > 0) {
													result = result + 1;
													history.add(lbl1 + " / " + lbl2 + ": " + substance + " not found in line 2, greater than 0 in line 1 # +1");
												} else {
													if (v2 > v1) {
														result = result + 1;
														history.add(lbl1 + " / " + lbl2 + ": " + substance + " value of line 1 smaller than line 2 # +1");
													} else
														if (v1 > v2) {
															result = result + 1;
															history.add(lbl1 + " / " + lbl2 + ": " + substance + " value of line 1 greater than line 2 # +1");
														}
												}
								}
							}
			
		}
		return result;
	}
}
