/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.algorithm.Algorithm;

import qmwi.kseg.som.DataSet;
import qmwi.kseg.som.SOMdataEntry;
import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

/**
 * @author Christian Klukas
 */
public class SOMplugin extends IPK_PluginAdapter {
	
	private static DataSet mydataset = null;
	private static String[] columns;
	private static boolean usedAverageValuesForStep1;
	
	public SOMplugin() {
		this.algorithms = new Algorithm[] { new SOManalysis() };
	}
	
	/**
	 * @return
	 */
	public static DataSet getDataSet(boolean init) {
		if (init || mydataset == null)
			mydataset = new DataSet();
		return mydataset;
	}
	
	static String[] initDataSetWithSelection(DataSet mydataset, Collection<GraphElement> selection, boolean returnNaN,
						boolean useSampleAverageValues) {
		HashSet<String> timePoints = new HashSet<String>();
		for (GraphElement n : selection) {
			for (MyComparableDataPoint mcdp : NodeTools.getDataTimePoints(n, useSampleAverageValues)) {
				String desc = mcdp.getSOMcolumnDesc(useSampleAverageValues);
				timePoints.add(desc);
			}
		}
		String[] groups = new String[timePoints.size()];
		int i = 0;
		String heading = "";
		for (String s : timePoints) {
			groups[i++] = s;
		}
		Arrays.sort(groups);
		for (String group : groups)
			heading += group + ";";
		if (heading.length() > 0)
			heading = heading.substring(0, heading.length() - 1);
		
		mydataset.setGroupDescription(heading);
		mydataset.clearEntries();
		for (GraphElement n : selection) {
			SOMdataEntry sde = IAPnodeTools.getSOMdataSet(n, groups.length, groups, returnNaN, useSampleAverageValues);
			if (sde != null)
				mydataset.addEntry(sde);
		}
		return groups;
	}
	
	public static String[] getColumns() {
		return columns;
	}
	
	public static void setColumns(String[] cols) {
		columns = cols;
	}
	
	public static boolean getLastUseAverageSetting() {
		return usedAverageValuesForStep1;
	}
	
	public static void setLastUseAverageSetting(boolean useAverageValues) {
		usedAverageValuesForStep1 = useAverageValues;
	}
}
