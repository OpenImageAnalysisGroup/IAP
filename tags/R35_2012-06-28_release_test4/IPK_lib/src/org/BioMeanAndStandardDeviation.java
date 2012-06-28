/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 02.11.2004 by Christian Klukas
 */
package org;

import org.jfree.data.MeanAndStandardDeviation;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class BioMeanAndStandardDeviation extends MeanAndStandardDeviation {

	public boolean isReference;
	public boolean isSignificantDifferent;

	/**
	 * Creates a new data point. Be aware, that if isReference is True and
	 * isSignificantDifferent is True, this means, that NO t test calculation was
	 * done for this sample.
	 * 
	 * @param mean
	 *           The mean value
	 * @param standardDeviation
	 *           The StdDev
	 * @param isReference
	 *           t test info, if this value is a reference
	 * @param isSignificantDifferent
	 *           t test info, true for H1, false for H0 probable
	 */
	public BioMeanAndStandardDeviation(Number mean, Number standardDeviation,
						boolean isReference, boolean isSignificantDifferent) {
		super(mean, standardDeviation);
		this.isReference = isReference;
		this.isSignificantDifferent = isSignificantDifferent;
	}

}
