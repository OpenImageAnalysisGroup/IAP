/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (19.12.2001 12:16:24)
 * 
 * @author:
 */
public class DiagrammData {
	public int row;
	public int column;
	public double value;
	public String seriesLabel;
	
	/**
	 * DiagrammData constructor comment.
	 */
	public DiagrammData() {
		super();
	}
	
	/**
	 * DiagrammData constructor comment.
	 */
	public DiagrammData(int r, int c, double v, String s) {
		super();
		row = r;
		column = c;
		value = v;
		seriesLabel = s;
	}
}
