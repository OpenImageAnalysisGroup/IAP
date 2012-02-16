package de.ipk.ag_ba.gui_model;

import java.util.Date;

public abstract class GuiModelDialog {
	private String title;
	private String description;
	private String okButtonText;
	private String[] fieldLabels;
	private String[] fieldStrings;
	private Date[] fieldDates;
	private Double[] fieldDoubles;
	private Integer[] fieldIntegers;
	
	public GuiModelDialog() {
		// empty
	}
	
	public GuiModelDialog(String title, String description, String okButtonText,
			String[] fieldLabels, String[] fieldStrings, Date[] fieldDates, Double[] fieldDoubles, Integer[] fieldIntegers) {
		this.setTitle(title);
		this.setDescription(description);
		this.setOkButtonText(okButtonText);
		this.setFieldLabels(fieldLabels);
		this.setFieldStrings(fieldStrings);
		this.setFieldDates(fieldDates);
		this.setFieldDoubles(fieldDoubles);
		this.setFieldIntegers(fieldIntegers);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setOkButtonText(String okButtonText) {
		this.okButtonText = okButtonText;
	}
	
	public String getOkButtonText() {
		return okButtonText;
	}
	
	public void setFieldLabels(String[] fieldLabels) {
		this.fieldLabels = fieldLabels;
	}
	
	public String[] getFieldLabels() {
		return fieldLabels;
	}
	
	public void setFieldStrings(String[] fieldStrings) {
		this.fieldStrings = fieldStrings;
	}
	
	public String[] getFieldStrings() {
		return fieldStrings;
	}
	
	public void setFieldDates(Date[] fieldDates) {
		this.fieldDates = fieldDates;
	}
	
	public Date[] getFieldDates() {
		return fieldDates;
	}
	
	public void setFieldDoubles(Double[] fieldDoubles) {
		this.fieldDoubles = fieldDoubles;
	}
	
	public Double[] getFieldDoubles() {
		return fieldDoubles;
	}
	
	public void setFieldIntegers(Integer[] fieldIntegers) {
		this.fieldIntegers = fieldIntegers;
	}
	
	public Integer[] getFieldIntegers() {
		return fieldIntegers;
	}
	
	public abstract void performOKaction();
	
	public abstract void updateGUI();
}
