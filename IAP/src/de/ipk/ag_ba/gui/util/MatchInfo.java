package de.ipk.ag_ba.gui.util;

public class MatchInfo {
	
	String height = "", frWeight = "", dryWeight = "";
	
	final String desc;
	
	public MatchInfo(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public String getHeight() {
		return height;
	}
	
	public String getFreshWeight() {
		return frWeight;
	}
	
	public String getDryWeight() {
		return dryWeight;
	}
	
	public void setDryWeight(String dryWeight) {
		this.dryWeight = dryWeight;
	}
	
	public void setFreshWeight(String frWeight) {
		this.frWeight = frWeight;
	}
	
	public void setHeight(String height) {
		this.height = height;
	}
}
