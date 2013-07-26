// ==============================================================================
//
// Dash.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Dash.java,v 1.1 2011-01-31 09:04:47 klukas Exp $

package org.graffiti.graphics;

/**
 * Class that encapsulates the information needed to specify stroke properties.
 * 
 * @author schoeffl
 * @version $Revision: 1.1 $
 */
public class Dash {
	// ~ Instance fields ========================================================
	
	/**
	 * @see java.awt.BasicStroke
	 */
	private float[] dashArray;
	
	/**
	 * @see java.awt.BasicStroke
	 */
	private float dashPhase;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new Dash. Initializes the dashArray with <code>null</code> and the dashPhase with 0.0.
	 */
	public Dash() {
		this.dashArray = null;
		this.dashPhase = 0f;
	}
	
	/**
	 * Constructs a new Dash. Sets the dashArray and the dashPhase to the given
	 * values.
	 * 
	 * @param da
	 *           the array to set the dashArray to.
	 * @param dp
	 *           the value to set the dashPhase to.
	 */
	public Dash(float[] da, float dp) {
		this.dashArray = da;
		this.dashPhase = dp;
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Sets the dashArray to the given array.
	 * 
	 * @param da
	 *           the array to set the dashArray to.
	 */
	public void setDashArray(float[] da) {
		this.dashArray = da;
	}
	
	/**
	 * Returns the dashArray.
	 * 
	 * @return the dashArray.
	 */
	public float[] getDashArray() {
		return dashArray;
	}
	
	/**
	 * Sets the dashPhase to the given value.
	 * 
	 * @param dp
	 *           the new value for the dashPhase.
	 */
	public void setDashPhase(float dp) {
		this.dashPhase = dp;
	}
	
	/**
	 * Returns the dashPhase.
	 * 
	 * @return the dashPhase.
	 */
	public float getDashPhase() {
		return dashPhase;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
