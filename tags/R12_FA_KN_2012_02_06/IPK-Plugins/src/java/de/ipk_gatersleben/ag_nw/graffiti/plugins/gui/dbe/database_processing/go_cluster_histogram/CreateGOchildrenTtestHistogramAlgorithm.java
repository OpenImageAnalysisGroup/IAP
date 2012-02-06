/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.FolderPanel;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class CreateGOchildrenTtestHistogramAlgorithm extends AbstractAlgorithm implements
					AlgorithmWithComponentDescription {
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Add t-Test Significance Histogram";
	}
	
	@Override
	public String getCategory() {
		return "Hierarchy";
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/go_cmd_desc_scaled.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "This command enumerates all leaf nodes<br>"
							+ "with mapping data connected to a given GO-Term-<br>" + "Hierarchy-Node.<br>"
							+ "A histogram of the number of significant <br>" + "differences of a line in comparison to the <br>"
							+ "reference set is created.<br>" + "A data mapping representing this data is <br>"
							+ "created and shown as a bar-chart.<br>" + "A t- or U-Test with the desired settings <br>"
							+ "needs to be performed before issuing this <br>" + "command!";
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
		TreeMap<String, Integer> significance2frequency = new TreeMap<String, Integer>();
		for (String ci : knownSeriesAndTimeIDs)
			significance2frequency.put(ci, new Integer(0));
		
		for (Node n : childNodes) {
			NodeHelper nnh = new NodeHelper(n);
			if (nnh.hasDataMapping() && nnh.getOutDegree() == 0) {
				String testGoTerm = (String) nnh.getAttributeValue("go", "term", null, "");
				if (testGoTerm == null) {
					for (ConditionInterface sd : nnh.getMappedSeriesData()) {
						for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
							if (!mcdp.ttestIsReference && mcdp.ttestIsSignificantDifferent) {
								String seriesAndTime = mcdp.serie + "§" + mcdp.timeUnitAndTime;
								int cnt = significance2frequency.get(seriesAndTime);
								significance2frequency.put(seriesAndTime, new Integer(cnt + 1));
							}
						}
					}
				}
			}
		}
		// create dataset
		nh.removeDataMapping();
		for (String seriesNameAndTime : significance2frequency.keySet()) {
			String seriesName = seriesNameAndTime.substring(0, seriesNameAndTime.indexOf("§"));
			String timeAndUnit = seriesNameAndTime.substring(seriesNameAndTime.indexOf("§") + 1);
			int timePoint = MyComparableDataPoint.getTimePointFromTimeAndUnit(timeAndUnit);
			String timeUnit = MyComparableDataPoint.getTimeUnitFromTimeAndUnit(timeAndUnit);
			int plantID = nh.memGetPlantID(seriesName, "", "", "", "");
			Integer value = significance2frequency.get(seriesNameAndTime);
			if (value == null)
				nh.memSample(new Double(0), -1, plantID, "frequency", timeUnit, timePoint);
			else
				nh.memSample(new Double(value), -1, plantID, "frequency", timeUnit, timePoint);
		}
		nh
							.memAddDataMapping(
												"mapping",
												"significance frequency",
												null,
												"calculated analysis",
												"system",
												"Frequency of significant differences for series in comparison to reference set in child nodes of a GO-Term-Hierarchy-Node",
												"");
		nh.setChartType(GraffitiCharts.BAR);
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
