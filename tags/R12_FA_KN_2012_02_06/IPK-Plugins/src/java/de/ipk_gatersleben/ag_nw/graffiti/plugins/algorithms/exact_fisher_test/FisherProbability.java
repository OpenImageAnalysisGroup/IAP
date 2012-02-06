package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.exact_fisher_test;

import java.math.BigDecimal;

public class FisherProbability {
	private BigDecimal oneSided;
	private BigDecimal twoSided;
	private boolean twoSidedCalculated;
	
	public FisherProbability(BigDecimal oneSided, BigDecimal twoSided, boolean twoSidedCalculated) {
		this.setOneSided(oneSided);
		if (twoSidedCalculated)
			this.setTwoSided(twoSided);
		this.twoSidedCalculated = twoSidedCalculated;
	}
	
	private void setOneSided(BigDecimal oneSided) {
		this.oneSided = oneSided;
	}
	
	private void setTwoSided(BigDecimal twoSided) {
		this.twoSided = twoSided;
	}
	
	public BigDecimal getOneSided() {
		return oneSided;
	}
	
	public BigDecimal getTwoSided() {
		return twoSided;
	}
	
	public double getOneSidedD() {
		return oneSided.doubleValue();
	}
	
	public double getTwoSidedD() {
		return twoSided.doubleValue();
	}
	
	public String getResultString() {
		if (twoSidedCalculated)
			return "P_1=" + getOneSidedD() + ", P_2=" + getTwoSidedD();
		else
			return "P_1=" + getOneSidedD() + ", P_2=n/a (not calculated)";
	}
}
