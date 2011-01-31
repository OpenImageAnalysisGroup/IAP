/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ErrorMsg;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ExperimentData {
	private Set<String> knownSubstanceNames = new LinkedHashSet<String>();
	private Set<String> knownPlantOrLineNames = new LinkedHashSet<String>();
	
	/**
	 * Initialize list of known Substance Names; use the data from the given column.
	 */
	public void searchIndividualSubstances(TableData myData, int col) {
		for (int row = 2; row <= myData.getMaximumRow(col); row++) {
			Object data = myData.getCellData(col, row, null);
			if (data != null && data instanceof String)
				knownSubstanceNames.add((String) data);
		}
	}
	
	/**
	 * Search possible line names out of the data column headers.
	 * (00per_2, 02per_3, 00emb_1, 02emb_2, ... ==> "per", "emb")
	 */
	public void searchIndividualPlantOrLineNames(TableData myData, int data_start_col, int row) {
		for (int col = data_start_col; col <= myData.getMaximumCol(); col++) {
			DataColumnHeader dch = new DataColumnHeader(myData.getUnicodeStringCellData(col, row), col);
			if (dch.isValid())
				knownPlantOrLineNames.add(dch.getPlant());
		}
	}
	
	public Iterator<String> getPlantOrLineIterator(TableData myData, int row, int check_datarow) {
		return knownPlantOrLineNames.iterator();
	}
	
	public Iterator<TimeAndPlantName> getSampleTimeIterator(TableData myData, String plantOrLine, int data_start_col, int row, int check_datarow) {
		SortedSet<TimeAndPlantName> result = new TreeSet<TimeAndPlantName>(new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				TimeAndPlantName o1, o2;
				o1 = (TimeAndPlantName) arg0;
				o2 = (TimeAndPlantName) arg1;
				return o1.getTime() - o2.getTime();
			}
		});
		for (int col = data_start_col; col <= myData.getMaximumCol(); col++) {
			DataColumnHeader dch = new DataColumnHeader(myData.getUnicodeStringCellData(col, row), col);
			Object mesVal = myData.getCellData(col, check_datarow, null);
			if (dch.isValid() && dch.getPlant().equals(plantOrLine) && mesVal != null) {
				TimeAndPlantName o = new TimeAndPlantName(dch.getPlant(), dch.getTime());
				result.add(o);
			}
		}
		return result.iterator();
	}
	
	public ArrayList<DataColumnHeader> getReplicateColumns(
						TableData myData,
						TimeAndPlantName timeAndPlantName,
						int data_start_col,
						int row,
						int check_datarow) {
		
		ArrayList<DataColumnHeader> result = new ArrayList<DataColumnHeader>();
		for (int col = data_start_col; col <= myData.getMaximumCol(); col++) {
			DataColumnHeader dch = new DataColumnHeader(myData.getUnicodeStringCellData(col, row), col);
			if (dch.isValid()
								&& dch.getPlant().equals(timeAndPlantName.getPlant())
								&& dch.getTime() == timeAndPlantName.getTime()
								&& myData.getCellData(col, check_datarow, null) != null) {
				result.add(dch);
			}
		}
		return result;
	}
	
	public ArrayList<ReplicateDouble> getMeasurementValues(TableData myData, ArrayList<DataColumnHeader> replicates, int row) {
		ArrayList<ReplicateDouble> result = new ArrayList<ReplicateDouble>();
		for (DataColumnHeader dch : replicates) {
			Object mes_val = myData.getCellData(dch.getColumn(), row, null);
			if (mes_val != null && mes_val instanceof Double) {
				ReplicateDouble rd = new ReplicateDouble(mes_val, new Integer(dch.getReplicateNumber()).toString(), null);
				result.add(rd);
			}
			if (mes_val != null && mes_val instanceof String) {
				String mes_val_s = myData.getUnicodeStringCellData(dch.getColumn(), row);
				if (mes_val_s != null && mes_val_s.length() > 0
									&& (mes_val_s.equalsIgnoreCase("-") || mes_val_s.equalsIgnoreCase("n/a") || mes_val_s.equalsIgnoreCase("na"))) {
					ReplicateDouble rd = new ReplicateDouble(Double.NaN, new Integer(dch.getReplicateNumber()).toString(), null);
					result.add(rd);
				} else {
					try {
						int colPos = mes_val_s.indexOf(":");
						String annotation = null;
						if (colPos > 0) {
							annotation = mes_val_s.substring(colPos + 1);
							mes_val_s = mes_val_s.substring(0, colPos);
						}
						ReplicateDouble rd = new ReplicateDouble(Double.parseDouble(mes_val_s), new Integer(dch.getReplicateNumber()).toString(), annotation);
						result.add(rd);
					} catch (NumberFormatException nfe) {
						ErrorMsg.addErrorMessage("Number Format Exception in row " + row + ", column " + dch.getColumn() + ", value: " + mes_val_s);
					}
				}
			}
		}
		return result;
	}
	
	public static double getMinimum(ArrayList<ReplicateDouble> measurements) {
		int n = measurements.size();
		if (n == 0)
			return Double.NaN;
		double min = Double.POSITIVE_INFINITY;
		for (ReplicateDouble curVal : measurements)
			if (curVal.doubleValue() < min)
				min = curVal.doubleValue();
		
		if (min < Double.POSITIVE_INFINITY)
			return min;
		else
			return Double.NaN;
	}
	
	public static double getMaximum(ArrayList<ReplicateDouble> measurements) {
		int n = measurements.size();
		if (n == 0)
			return Double.NaN;
		double max = Double.NEGATIVE_INFINITY;
		for (ReplicateDouble curVal : measurements)
			if (curVal.doubleValue() > max)
				max = curVal.doubleValue();
		
		if (max > Double.NEGATIVE_INFINITY)
			return max;
		else
			return Double.NaN;
	}
	
	// http://coe.sdsu.edu/eet/Articles/standarddev/index.htm
	public static double getStddev(ArrayList<ReplicateDouble> measurements) {
		int n = measurements.size();
		if (n <= 1)
			return Double.NaN;
		double avg = getAverage(measurements);
		
		n = 0;
		
		double t = 0d;
		for (ReplicateDouble curVal : measurements) {
			double x = curVal.doubleValue();
			
			if (Double.isNaN(x))
				continue;
			
			n++;
			t += (x - avg) * (x - avg);
		}
		if (n <= 1)
			return Double.NaN;
		return Math.sqrt(t / (n - 1));
	}
	
	public static double getAverage(ArrayList<ReplicateDouble> measurements) {
		int n = measurements.size();
		if (n == 0)
			return Double.NaN;
		double sum = 0d;
		
		n = 0;
		
		for (ReplicateDouble curVal : measurements) {
			double val = curVal.doubleValue();
			
			if (Double.isNaN(val))
				continue;
			
			n++;
			sum += val;
		}
		if (n == 0)
			return Double.NaN;
		else
			return sum / n;
	}
}
