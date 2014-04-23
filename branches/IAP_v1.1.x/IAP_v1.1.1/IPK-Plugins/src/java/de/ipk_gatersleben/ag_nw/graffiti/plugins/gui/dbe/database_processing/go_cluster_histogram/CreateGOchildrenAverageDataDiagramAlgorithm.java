/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class CreateGOchildrenAverageDataDiagramAlgorithm extends AbstractAlgorithm {
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Add Average-Values Diagram";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "This command enumerates all <br>" + "leaf nodes with mapping data of a<br>"
							+ "given GO-Term-Hierarchy-Node.";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		try {
			graph.getListenerManager().transactionStarted(this);
			Collection<Node> workingSet = getSelectedOrAllNodes();
			TreeSet<String> knownSeriesIDs = new TreeSet<String>();
			HashSet<Node> processedNodes = new HashSet<Node>();
			for (Node n : workingSet) {
				knownSeriesIDs.addAll(getChildNodeSeriesNamesAndTimes(processedNodes, new NodeHelper(n), knownSeriesIDs));
			}
			for (Node n : workingSet) {
				NodeHelper nh = new NodeHelper(n);
				if (nh.getOutDegree() > 0) {
					processNode(nh, knownSeriesIDs);
				}
			}
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private void processNode(NodeHelper nh, TreeSet<String> knownSeriesAndTimeIDs) {
		Collection<Node> childNodes = nh.getAllOutChildNodes();
		TreeMap<String, ArrayList<Double>> seriesAndTime2mappedValues = new TreeMap<String, ArrayList<Double>>();
		for (String ci : knownSeriesAndTimeIDs)
			seriesAndTime2mappedValues.put(ci, new ArrayList<Double>());
		
		HashMap<String, Integer> seriesAndTime2replicateid = new HashMap<String, Integer>();
		int currentReplicateID = 1;
		for (Node n : childNodes) {
			NodeHelper nnh = new NodeHelper(n);
			if (nnh.hasDataMapping() && nnh.getOutDegree() == 0) {
				String testGoTerm = (String) nnh.getAttributeValue("go", "term", null, "");
				if (testGoTerm == null) {
					for (ConditionInterface sd : nnh.getMappedSeriesData()) {
						for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
							String seriesAndTime = mcdp.serie + "§" + mcdp.timeUnitAndTime;
							seriesAndTime2mappedValues.get(seriesAndTime).add(mcdp.mean);
							if (!seriesAndTime2replicateid.containsKey(seriesAndTime)) {
								seriesAndTime2replicateid.put(seriesAndTime, new Integer(currentReplicateID++));
							}
						}
					}
				}
			}
		}
		// create dataset
		nh.removeDataMapping();
		for (String seriesNameAndTime : seriesAndTime2mappedValues.keySet()) {
			String seriesName = seriesNameAndTime.substring(0, seriesNameAndTime.indexOf("§"));
			String timeAndUnit = seriesNameAndTime.substring(seriesNameAndTime.indexOf("§") + 1);
			int timePoint = MyComparableDataPoint.getTimePointFromTimeAndUnit(timeAndUnit);
			String timeUnit = MyComparableDataPoint.getTimeUnitFromTimeAndUnit(timeAndUnit);
			int plantID = nh.memGetPlantID(seriesName, "", "", "", "");
			ArrayList<Double> values = seriesAndTime2mappedValues.get(seriesNameAndTime);
			if (values == null || values.size() <= 0)
				nh.memSample(new Double(Double.NaN), -1, plantID, "frequency", timeUnit, timePoint);
			else {
				boolean added = false;
				for (Double val : values) {
					if (val != null && !Double.isNaN(val)) {
						Integer replicateId = seriesAndTime2replicateid.get(seriesNameAndTime);
						if (replicateId == null)
							replicateId = new Integer(-1);
						nh.memSample(val, replicateId, plantID, "average of sample means", timeUnit, timePoint);
						added = true;
					}
				}
				if (!added)
					nh.memSample(new Double(Double.NaN), -1, plantID, "average of sample means", timeUnit, timePoint);
			}
		}
		nh.memAddDataMapping("mapping", "average of sample means", null, "calculated analysis", "system",
							"Average of sample means", "");
		nh.setChartType(GraffitiCharts.BAR_FLAT);
	}
	
	private TreeSet<String> getChildNodeSeriesNamesAndTimes(HashSet<Node> processedNodes, NodeHelper nh,
						Collection<String> knownSeriesIDs) {
		TreeSet<String> result = new TreeSet<String>();
		if (!processedNodes.contains(nh.getGraphNode())) {
			processedNodes.add(nh.getGraphNode());
			Collection<Node> childNodes = nh.getAllOutChildNodes();
			for (Node n : childNodes) {
				NodeHelper nnh = new NodeHelper(n);
				if (nnh.hasDataMapping() && nnh.getOutDegree() == 0) {
					for (ConditionInterface sd : nnh.getMappedSeriesData()) {
						for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
							result.add(mcdp.serie + "§" + mcdp.timeUnitAndTime);
						}
					}
				}
			}
		}
		return result;
	}
}
