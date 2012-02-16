/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression;

public class KeggExpressionDatapoint {
	
	private String geneId;
	Double controlSig, controlBgk;
	Double targetSig, targetBgk;
	private Double targetValue;
	private Double controlValue;
	private Double optY;
	private Double optX;
	String optRawTextFileInformationSignalQualityPMA;
	
	public KeggExpressionDatapoint(String geneId,
						Double cSig, Double cBgk,
						Double tSig, Double tBgk,
						Double c, Double t,
						Double optX, Double optY,
						String optRawTextFileInformationSignalQualityPMA) {
		this.setGeneId(geneId);
		this.controlSig = cSig;
		this.controlBgk = cBgk;
		this.targetSig = tSig;
		this.targetBgk = tBgk;
		this.setC(c);
		this.setT(t);
		this.setOptX(optX);
		this.setOptY(optY);
		this.optRawTextFileInformationSignalQualityPMA = optRawTextFileInformationSignalQualityPMA;
	}
	
	public String getOptQualityTag(String preString, String returnValueIfValueNotAvailable) {
		if (optRawTextFileInformationSignalQualityPMA != null)
			return preString + optRawTextFileInformationSignalQualityPMA;
		else
			return returnValueIfValueNotAvailable;
	}
	
	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}
	
	public String getGeneId() {
		return geneId;
	}
	
	public void setC(Double c) {
		this.controlValue = c;
	}
	
	public Double getC() {
		return getControlValue();
	}
	
	public Double getControlValue() {
		return controlValue;
	}
	
	public void setT(Double t) {
		this.targetValue = t;
	}
	
	public Double getT() {
		return getTargetValue();
	}
	
	public Double getTargetValue() {
		return targetValue;
	}
	
	public void setOptX(Double optX) {
		this.optX = optX;
	}
	
	public Double getOptX() {
		return optX;
	}
	
	public void setOptY(Double optY) {
		this.optY = optY;
	}
	
	public Double getOptY() {
		return optY;
	}
	
	public Double getLog2TargetDivControlValue() {
		double r = getTargetValue() / getControlValue();
		double value = Math.log(r) / Math.log(2);
		return value;
	}
}
