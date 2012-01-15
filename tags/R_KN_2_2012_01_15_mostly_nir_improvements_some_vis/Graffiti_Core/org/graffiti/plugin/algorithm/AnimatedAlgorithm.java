// ==============================================================================
//
// AnimatedAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AnimatedAlgorithm.java,v 1.1 2011-01-31 09:04:43 klukas Exp $

package org.graffiti.plugin.algorithm;

import org.graffiti.graph.Graph;

/**
 * An <code>AnimatedAlgorithm</code> provides the possibility to execute the
 * underlying algorithm in steps, so that the intermediate results can be
 * seen. It still provides a way to execute the whole algorithm as one big
 * step, as if it was a 'normal' <code>Algorithm</code>.
 * 
 * @version $Revision: 1.1 $
 * @see Algorithm
 */
public interface AnimatedAlgorithm
					extends Algorithm {
	// ~ Methods ================================================================
	
	/**
	 * Returns <code>true</code> if the algorithm has another step (that means
	 * that <code>nextStep()</code> will not throw an Exception).
	 * 
	 * @return <code>true</code> if the algorithm has another step, <code>false</code> otherwise.
	 */
	public boolean isFinished();
	
	/**
	 * Starts the animation of the algorithm. Further execution steps are made
	 * through the <code>nextStep()</code> method.
	 * 
	 * @param g
	 *           the <code>Graph</code> to execute the algorithm on.
	 */
	public void animate(Graph g);
	
	/**
	 * Executes the whole algorithm as one big step.
	 * 
	 * @param g
	 *           the <code>Graph</code> to execute the algorithm on.
	 */
	public void execute(Graph g);
	
	/**
	 * Executes the next step of the algorithm.
	 * 
	 * @exception NoNextStepException
	 *               if the algorithm is already finished.
	 */
	public void nextStep()
						throws NoNextStepException;
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
