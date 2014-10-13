package de.ipk.ag_ba.gui.util;

public class MatchInfo {
	
	String height = "", frWeight = "", dryWeight = "";
	
	final String desc;
	
	private Double cv;
	
	private String leafWidth = "";
	
	private String leafCount = "";
	
	public MatchInfo(String desc) {
		this.desc = desc;
	}
	
	public String getDesc() {
		String res = desc;
		if (res == null)
			return res;
		if (res.startsWith("corr."))
			res = res.substring("corr.".length());
		if (res.endsWith(".mean"))
			res = res.substring(0, res.length() - ".mean".length());
		else
			if (res.endsWith(".angles"))
				res = res.substring(0, res.length() - ".angles".length());
		return res;
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
	
	public String getLeafCount() {
		return leafCount;
	}
	
	public String getLeafWidth() {
		return leafWidth;
	}
	
	public void setDryWeight(String dryWeight) {
		this.dryWeight = dryWeight;
	}
	
	public void setFreshWeight(String frWeight) {
		this.frWeight = frWeight;
	}
	
	public void setComparisonValue(Double cv) {
		this.cv = cv;
	}
	
	public void setHeight(String height) {
		this.height = height;
	}
	
	public Double getComparisonValue() {
		return cv;
	}
	
	public void setLeafCount(String leafCount) {
		this.leafCount = leafCount;
	}
	
	public void setLeafWidth(String leafWidth) {
		this.leafWidth = leafWidth;
	}
}
