/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.ErrorMsg;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.CircleLayouterAlgorithm;

/**
 * @author Christian Klukas
 *         4.7.2007
 */
public class PatternVistorLayouter implements PatternVisitor {
	
	private Algorithm layout;
	
	public PatternVistorLayouter(Algorithm layoutAlgorithm) {
		this.layout = layoutAlgorithm;
	}
	
	public boolean visitPattern(int numberOfNodesInMatch,
						Node[] matchInPattern, Node[] matchInTarget, String patternName) {
		if (matchInTarget != null && matchInTarget.length > 0) {
			if (MarkingPatternVisitor.checkForDuplicateMatch(matchInTarget)) {
				return false;
			}
			layout.reset();
			Node firstNode = matchInTarget[0];
			Selection sel = new Selection("temp");
			for (Node n : matchInTarget)
				sel.add(n);
			
			layout.attach(firstNode.getGraph(), sel);
			try {
				layout.check();
				
				if (layout instanceof CircleLayouterAlgorithm) {
					((CircleLayouterAlgorithm) layout).setRadius(
										((CircleLayouterAlgorithm) layout).getPatternNodeDistance() * matchInTarget.length / 2 / Math.PI);
				}
				
				layout.execute();
			} catch (PreconditionException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		return false;
	}
	
}
