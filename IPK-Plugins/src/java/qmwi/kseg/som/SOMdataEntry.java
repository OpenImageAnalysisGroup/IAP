/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.util.ArrayList;

// Hilfsklasse zum vereinfachten Einlesen von Excel-Dateien im CSV-Format
// Eingabezeile wird mit Hilfe des Trennzeichens ";" in einzelne Strings zerlegt
public interface SOMdataEntry {
	public String[] getColumnData();
	
	public SOMdataEntry addValues(String inputLine, boolean normalized);
	
	public SOMdataEntry addValues(String inputLine);
	
	public Object getUserData();
	
	public void setUserData(Object data);
	
	public String getColumnData(int i);
	
	/**
	 * @return True, if the supplied data is already normalized in the range of -1..1. If this value is False,
	 *         The SOM train and recall method will normalize the data column wise! This means, e.g. all values for time point
	 *         1 will be collected and then normalized, thus from day to day a different normalization is performed.
	 *         This is useful while analyzing independent data points. For time series data this is clearly not a good idea,
	 *         in this case the data should be supplied normalized and this flag should return true.
	 */
	public boolean isAlreadyNormalized();
	
	public ArrayList<Double> getDifferencesToCentroids();
	
	public void setDifferences(ArrayList<Double> differences);
	
	public double getMinDiff();
} // class DataEnty
