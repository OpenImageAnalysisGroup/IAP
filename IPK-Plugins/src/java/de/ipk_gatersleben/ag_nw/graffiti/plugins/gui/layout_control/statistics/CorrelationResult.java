/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 07.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ErrorMsg;
import org.StringManipulationTools;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class CorrelationResult {
	private static DecimalFormat outputBase = ErrorMsg.getDecimalFormat("########0.000");
	
	private final List<FloatAndDesc> r = new ArrayList<FloatAndDesc>();
	private final List<Double> corrprobs = new ArrayList<Double>();
	private float maxR = Float.MIN_VALUE;
	private boolean isAnyOneSignificant = false;
	private double maxCorrProb = Double.NEGATIVE_INFINITY;
	private final String dataset1, dataset2;
	private int dataset2offsetOfMaxR = 0;
	private int indexOfMaxOrMin = Integer.MAX_VALUE;
	private String maxRcalculationHistory = "";
	
	public CorrelationResult(String dataset1, String dataset2) {
		this.dataset1 = dataset1;
		this.dataset2 = dataset2;
	}
	
	public void addR(float r,
			double significantProbability, int dataset2offset, String calculationHistory, String seriesName,
			double corrprobability) {
		this.r.add(new FloatAndDesc(r, seriesName));
		this.corrprobs.add(new Double(corrprobability));
		if (Math.abs(corrprobability) >= significantProbability) {
			isAnyOneSignificant = true;
		}
		if (r >= 0 && Math.abs(corrprobability) >= significantProbability) {
		}
		if (Math.abs(corrprobability) > maxCorrProb)
			maxCorrProb = Math.abs(corrprobability);
		if (Math.abs(r) > maxR) {
			// System.out.println(this.toString()+"new max r="+r+" oldmax="+maxR);
			this.dataset2offsetOfMaxR = dataset2offset;
			this.maxRcalculationHistory = calculationHistory;
			this.indexOfMaxOrMin = corrprobs.size() - 1;
			maxR = r;
		}
	}
	
	public boolean isAnyOneSignificant(double minimumR) {
		boolean result;
		
		result = isAnyOneSignificant
				&& ((Math.abs(maxR) >= Math.abs(minimumR)));
		return result;
	}
	
	public String getRlist() {
		String res1;
		// double alpha = 1d - significantProbability;
		if (isAnyOneSignificant)
			res1 = "Significant corr. (" +
					dataset1 + " - " + dataset2 + "): ";
		else
			res1 = "Not significant corr. (" +
					dataset1 + " - " + dataset2 + "): ";
		
		for (Iterator<FloatAndDesc> it = this.r.iterator(); it.hasNext();) {
			FloatAndDesc fad;
			fad = it.next();
			if (it.hasNext())
				res1 += "r=" + outputBase.format(fad.f) + " (" + fad.desc + ")" + ", ";
			else
				res1 += "r=" + outputBase.format(fad.f) + " (" + fad.desc + ")";
		}
		String calcHist = StringManipulationTools.stringReplace(getCalculationHistoryForMaxR(), "<html>", "");
		if (!isAnyOneSignificant)
			calcHist = StringManipulationTools.stringReplace(getCalculationHistoryForMaxR(), "significant", "(insignificant)");
		res1 += "<hr>" + calcHist;
		return "<html>" + res1;
	}
	
	public float getMaxR() {
		return maxR;
	}
	
	public int getDataset2offsetOfMaxOrMinR() {
		return dataset2offsetOfMaxR;
	}
	
	public String getCalculationHistoryForMaxR() {
		return maxRcalculationHistory;
	}
	
	public double getMaxTrueCorrProb() {
		return maxCorrProb;
	}
	
	public void setCalculationHistoryForMaxR(String history) {
		maxRcalculationHistory = history;
	}
	
	public String getMaxOrMinR2() {
		float r = getMaxR();
		return outputBase.format(r);
	}
	
	public String getMaxOrMinProb() {
		double r;
		if (indexOfMaxOrMin > corrprobs.size() && corrprobs.size() == 1)
			r = corrprobs.get(0);
		else
			r = corrprobs.get(indexOfMaxOrMin);
		return outputBase.format(r);
	}
}
