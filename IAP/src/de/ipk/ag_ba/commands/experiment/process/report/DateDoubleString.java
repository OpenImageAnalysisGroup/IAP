package de.ipk.ag_ba.commands.experiment.process.report;

import java.util.ArrayList;
import java.util.Date;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

public class DateDoubleString {
	
	private final String text;
	private final Double value;
	private final Date date;
	private String link;
	private ArrayList<BinaryMeasurement> bms;
	
	public DateDoubleString(Double v) {
		this.text = null;
		this.value = v;
		this.date = null;
	}
	
	public DateDoubleString(String t) {
		this.text = t;
		this.value = null;
		this.date = null;
	}
	
	public DateDoubleString(String t, ArrayList<BinaryMeasurement> bms) {
		this.text = t;
		this.value = null;
		this.date = null;
		this.bms = bms;
	}
	
	public DateDoubleString(Date d) {
		this.text = null;
		this.value = null;
		this.date = d;
	}
	
	public DateDoubleString(Integer i) {
		this(i != null ? i.doubleValue() : null);
	}
	
	public DateDoubleString() {
		this.text = null;
		this.value = null;
		this.date = null;
	}
	
	public String getString() {
		return text;
	}
	
	public String getLink() {
		return link;
	}
	
	public Double getDouble() {
		return value;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void checkForLink() {
		if (text != null && text.indexOf("|") < 0) {
			link = text;
		}
	}
	
	public ArrayList<BinaryMeasurement> getBinaryData() {
		return bms;
	}
}
