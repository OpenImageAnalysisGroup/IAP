/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 7, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.AttributeHelper;
import org.graffiti.graph.GraphElement;

import qmwi.kseg.som.CSV_SOM_dataEntry;
import qmwi.kseg.som.SOMdataEntry;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

/**
 * @author klukas
 */
public class IAPnodeTools extends NodeTools {
	public static SOMdataEntry getSOMdataSet(GraphElement n, int columnCount, String[] columnDesc, boolean returnNaN,
						boolean useSampleAverageValues) {
		List<MyComparableDataPoint> mappedData = new ArrayList<MyComparableDataPoint>();
		StringBuffer res = new StringBuffer();
		int missingDataPoints = 0;
		double lastDataPoint = Double.NaN;
		if (getMappedDataListFromNode(n) != null) {
			for (SubstanceInterface mappingData : getMappedDataListFromNode(n)) {
				List<MyComparableDataPoint> list;
				if (useSampleAverageValues)
					list = getSortedAverageDataSetValues(mappingData);
				else
					list = getSortedDataSetValues(mappingData);
				mappedData.addAll(list);
			}
			
			ArrayList<Double> dataPoints = new ArrayList<Double>();
			
			for (int column = 0; column < columnDesc.length; column++) {
				String findDataPointWithColumnDesc = columnDesc[column];
				MyComparableDataPoint mcdpResult = null;
				for (MyComparableDataPoint mcdp : mappedData) {
					String desc = mcdp.getSOMcolumnDesc(useSampleAverageValues);
					if (desc.equals(findDataPointWithColumnDesc)) {
						mcdpResult = mcdp;
						break;
					}
				}
				if (mcdpResult == null) {
					if (returnNaN)
						dataPoints.add(new Double(Double.NaN)); // missing value
					else {
						if (Double.isNaN(lastDataPoint))
							missingDataPoints++;
						else
							dataPoints.add(new Double(lastDataPoint));
					}
				} else {
					while (missingDataPoints > 0) {
						dataPoints.add(new Double(mcdpResult.mean));
						missingDataPoints--;
					}
					dataPoints.add(new Double(mcdpResult.mean));
					lastDataPoint = mcdpResult.mean;
				}
			}
			
			// search min max during
			double min = Double.MAX_VALUE;
			double max = Double.NEGATIVE_INFINITY;
			for (Double val : dataPoints) {
				if (val.isNaN())
					continue;
				if (val.doubleValue() > max)
					max = val.doubleValue();
				if (val.doubleValue() < min)
					min = val.doubleValue();
			}
			if (min > 0)
				min = 0;
			if (max < 0)
				max = 0;
			for (Iterator<Double> it = dataPoints.iterator(); it.hasNext();) {
				Double dp = it.next();
				double normValue;
				if (!AttributeHelper.getLabel(n, "").startsWith("Centroid"))
					normValue = (((dp.doubleValue() - min) / (max - min)) * 2) - 1;
				else
					normValue = dp.doubleValue();
				res.append(normValue);
				if (it.hasNext())
					res.append(";");
			}
		}
		// System.out.println("SOM input: "+res.toString());
		if (res.toString().length() > 0)
			return new CSV_SOM_dataEntry(columnCount, n).addValues(res.toString(), true);
		else
			return null;
	}
}
