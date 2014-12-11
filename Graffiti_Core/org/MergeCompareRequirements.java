package org;

/**
 * @author klukas
 */
public class MergeCompareRequirements {
	
	private boolean compareSamples = true;
	private boolean compareValues = true;
	
	public void setCompareSamples(boolean compareSamples) {
		this.compareSamples = compareSamples;
	}
	
	public void setCompareValues(boolean compareValues) {
		this.compareValues = compareValues;
	}
	
	public boolean needsCompareConditions() {
		return true;
	}
	
	public boolean needsCompareSamples() {
		return compareSamples;
	}
	
}
