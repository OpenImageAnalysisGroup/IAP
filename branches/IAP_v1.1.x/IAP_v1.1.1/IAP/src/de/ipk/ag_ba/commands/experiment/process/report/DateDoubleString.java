package de.ipk.ag_ba.commands.experiment.process.report;

import java.util.Date;

public class DateDoubleString {
	
	private final String text;
	private final Double value;
	private final Date date;
	
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
	
	public Double getDouble() {
		return value;
	}
	
	public Date getDate() {
		return date;
	}
	
}
