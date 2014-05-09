/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.AttributeHelper;
import org.HelperClass;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

public class GraphElementHelper implements HelperClass {
	
	GraphElement ge;
	
	public GraphElementHelper(GraphElement ge) {
		this.ge = ge;
	}
	
	public String getLabel() {
		return AttributeHelper.getLabel(ge, null);
	}
	
	/**
	 * @return The Experiment (will never be null)
	 */
	public ExperimentInterface getDataMappings() {
		try {
			CollectionAttribute ca = (CollectionAttribute) ge.getAttribute(Experiment2GraphHelper.mapFolder);
			XMLAttribute xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
			return xa.getMappedData();
		} catch (AttributeNotFoundException e) {
			// no mapping data
		}
		return new Experiment();
	}
	
	public double getAverage() {
		double sum = 0;
		ExperimentInterface mdl = getDataMappings();
		for (SubstanceInterface md : mdl)
			sum += md.getAverage();
		return sum / mdl.size();
	}
	
	public void setLabel(String label) {
		AttributeHelper.setLabel(ge, label);
	}
	
	public void setCluster(String clusterId) {
		NodeTools.setClusterID(ge, clusterId);
	}
	
	public String getCluster() {
		return NodeTools.getClusterID(ge, null);
	}
	
	public ArrayList<SampleInterface> getMappedSampleData() {
		ArrayList<SampleInterface> result = new ArrayList<SampleInterface>();
		for (SubstanceInterface md : getDataMappings()) {
			for (ConditionInterface co : md) {
				result.addAll(co);
			}
		}
		return result;
	}
	
	public Set<Integer> getMappedTimePointsCoveredByAllLines() {
		List<HashSet<Integer>> timePoints = new ArrayList<HashSet<Integer>>();
		for (ConditionInterface sd : getMappedSeriesData()) {
			HashSet<Integer> tp = new HashSet<Integer>();
			sd.getTimes(tp);
			timePoints.add(tp);
		}
		HashSet<Integer> timePointsCoveredInAllSeries = new HashSet<Integer>();
		Set<Integer> allPossibleTimePoints = getMappedUniqueTimePoints();
		for (Integer checkTime : allPossibleTimePoints) {
			boolean inAll = true;
			for (HashSet<Integer> checkSeries : timePoints) {
				if (!checkSeries.contains(checkTime)) {
					inAll = false;
					break;
				}
			}
			if (inAll)
				timePointsCoveredInAllSeries.add(checkTime);
		}
		return timePointsCoveredInAllSeries;
	}
	
	public Set<Integer> getMappedUniqueTimePoints() {
		HashSet<Integer> result = new HashSet<Integer>();
		for (SubstanceInterface md : getDataMappings()) {
			for (ConditionInterface co : md) {
				co.getTimes(result);
			}
		}
		return result;
	}
	
	public ArrayList<ConditionInterface> getMappedSeriesData() {
		ArrayList<ConditionInterface> result = new ArrayList<ConditionInterface>();
		for (SubstanceInterface md : getDataMappings()) {
			result.addAll(md);
		}
		return result;
	}
}
