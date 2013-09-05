/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

/**
 * Represensts a Data Column Header in the Gene Expression Excel table.
 * 
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DataColumnHeader {
	public static final int ERROR_NUMBER = -2;
	private boolean valid = false;
	private int replicateNumber = ERROR_NUMBER;
	private String plant = null;
	private int time = ERROR_NUMBER;
	private int column = -1;
	
	/**
	 * 00emb_1 ==> time "0", location/plant "emb", replicate number "1"
	 * 
	 * @param headerText
	 */
	public DataColumnHeader(String headerText, int column) {
		boolean negative = false;
		if (headerText != null && headerText.length() >= 4) {
			if (headerText.startsWith("-")) {
				negative = true;
				headerText = headerText.substring(1);
			}
			char[] c = headerText.toCharArray();
			
			boolean startsWithNumber = Character.isDigit(c[0]);
			if (!startsWithNumber)
				return;
			
			int lastNumber = 0;
			while (Character.isDigit(c[lastNumber + 1]))
				lastNumber++;
			
			time = Integer.parseInt(headerText.substring(0, lastNumber + 1));
			if (negative)
				time = -time;
			
			int lastDiv = headerText.lastIndexOf("_");
			if (lastDiv < 0)
				return;
			plant = headerText.substring(lastNumber + 1, lastDiv);
			if (plant.startsWith("_"))
				plant = plant.substring("_".length());
			
			String endReplicateNumberText = headerText.substring(lastDiv + 1);
			try {
				replicateNumber = Integer.parseInt(endReplicateNumberText);
			} catch (NumberFormatException nfe) {
				replicateNumber = ERROR_NUMBER;
			}
			valid = startsWithNumber && plant != null && replicateNumber != ERROR_NUMBER && time != ERROR_NUMBER;
			if (valid)
				this.column = column;
		}
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public String getPlant() {
		if (valid)
			return plant;
		else
			return null;
	}
	
	public int getTime() {
		if (valid)
			return time;
		else
			return ERROR_NUMBER;
	}
	
	public int getReplicateNumber() {
		if (valid)
			return replicateNumber;
		else
			return ERROR_NUMBER;
	}
	
	public int getColumn() {
		if (valid)
			return column;
		else
			return ERROR_NUMBER;
	}
}
