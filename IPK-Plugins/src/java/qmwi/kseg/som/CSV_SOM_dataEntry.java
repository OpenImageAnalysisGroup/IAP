/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.util.ArrayList;

// Hilfsklasse zum vereinfachten Einlesen von Excel-Dateien im CSV-Format
// Eingabezeile wird mit Hilfe des Trennzeichens ";" in einzelne Strings zerlegt
public class CSV_SOM_dataEntry implements SOMdataEntry {
	private String[] dataValues;
	private Object userData;
	private boolean isNormalizedDataset = false;
	private ArrayList<Double> optDifferencesToCentroids;
	
	public CSV_SOM_dataEntry(int size) {
		dataValues = new String[size];
	}
	
	@Override
	public String toString() {
		String res = "[";
		for (int i = 0; i < dataValues.length; i++)
			if (i < dataValues.length - 1)
				res += dataValues[i] + ";";
			else
				res += dataValues[i];
		res += "]";
		return res;
	}
	
	/**
	 * @param columnCount
	 * @param n
	 */
	public CSV_SOM_dataEntry(int columnCount, Object userData) {
		this(columnCount);
		this.userData = userData;
	}
	
	public String[] getColumnData() {
		return dataValues;
	}
	
	public Object getUserData() {
		return userData;
	}
	
	/*
	 * (non-Javadoc)
	 * @see qmwi.kseg.som.SOMdataEntry#setUserData(java.lang.Object)
	 */
	public void setUserData(Object data) {
		this.userData = data;
	}
	
	public SOMdataEntry addValues(String inputLine, boolean normalized) {
		this.isNormalizedDataset = normalized;
		addValues(inputLine);
		return this;
	}
	
	public SOMdataEntry addValues(String inputLine) {
		if (!inputLine.endsWith(";"))
			inputLine = inputLine + ";";
		int i = 0;
		while (inputLine.length() > 1) {
			int idx = inputLine.indexOf(";");
			dataValues[i] = inputLine.substring(0, idx);
			inputLine = inputLine.substring(idx + 1);
			i++;
		}
		return this;
	}
	
	public String getColumnData(int i) {
		if (getColumnData()[i] != null)
			return getColumnData()[i];
		else
			return new Double(0).toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see qmwi.kseg.som.SOMdataEntry#isAlreadyNormalized()
	 */
	public boolean isAlreadyNormalized() {
		return isNormalizedDataset;
	}
	
	public ArrayList<Double> getDifferencesToCentroids() {
		return optDifferencesToCentroids;
	}
	
	public void setDifferences(ArrayList<Double> differences) {
		optDifferencesToCentroids = differences;
	}
	
	public double getMinDiff() {
		double min = Double.MAX_VALUE;
		for (double d : optDifferencesToCentroids) {
			if (d < min)
				min = d;
		}
		return min;
	}
} // class DataEnty
