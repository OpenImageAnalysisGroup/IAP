// ==============================================================================
//
// ConstraintCheckerListener.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: ConstraintCheckerListener.java,v 1.1 2011-01-31 09:04:31 klukas Exp $

/*
 * $Id: ConstraintCheckerListener.java,v 1.1 2011-01-31 09:04:31 klukas Exp $
 */
package org.graffiti.session;

/**
 * Defines a listener to the <code>GraphConstraintChecker</code>. The method <code>checkFailed</code> is called every time the
 * <code>GraphConstraintChecker</code> finds an unsatisfied constraint.
 * 
 * @see GraphConstraintChecker
 */
public interface ConstraintCheckerListener {
	// ~ Methods ================================================================
	
	/**
	 * Handles the message received by the constraint checker indicating an
	 * unsatisfied constraint.
	 * 
	 * @param msg
	 *           the message telling about the unsatisfied constraint.
	 */
	public void checkFailed(String msg);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
