/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.random;

import java.util.Collection;
import java.util.HashMap;

import org.AttributeHelper;
import org.Vector2d;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;

/**
 * This algorithm performs a random placement of the nodes on the visible
 * area.
 * 
 * @author Dirk Koschützki, Christian Klukas
 */
public class RandomLayouterAlgorithm
					extends AbstractAlgorithm {
	
	private boolean use3d = false;
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return "Random";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	/**
	 * Checks, if a graph was given.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm
	 *            invocation
	 */
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
			throw errors;
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		// EditorSession session =
		// GravistoService.getInstance().getMainFrame()
		// .getActiveEditorSession();
		// View theView = session.getActiveView();
		//
		// Dimension viewDimension = theView.getViewComponent().getSize();
		// double viewHeight = viewDimension.getHeight();
		// double viewWidth = viewDimension.getWidth();
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		Collection<Node> work = getSelectedOrAllNodes();
		Vector2d min = NodeTools.getMinimumXY(work, 1, 0, 0, false, false);
		Vector2d max = NodeTools.getMaximumXY(work, 1, 0, 0, false, false);
		
		for (Node n : work) {
			if (AttributeHelper.isHiddenGraphElement(n))
				continue;
			double newX = Math.random() * (max.x - min.x) + min.x;
			double newY = Math.random() * (max.y - min.y) + min.y;
			if (use3d) {
				double newZ = Math.random() * (max.x - min.x) + min.x;
				AttributeHelper.setPositionZ(n, newZ);
			}
			nodes2newPositions.put(n, new Vector2d(newX, newY));
		}
		if (work.size() > 1) {
			Vector2d newMin = new Vector2d(Double.MAX_VALUE, Double.MAX_VALUE);
			Vector2d newMax = new Vector2d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			for (Vector2d np : nodes2newPositions.values()) {
				if (np.x < newMin.x)
					newMin.x = np.x;
				if (np.y < newMin.y)
					newMin.y = np.y;
				if (np.x > newMax.x)
					newMax.x = np.x;
				if (np.y > newMax.y)
					newMax.y = np.y;
			}
			if (newMax.x - newMin.x < 1)
				newMax.x += 1;
			if (newMax.y - newMin.y < 1)
				newMax.y += 1;
			double xScale = (max.x - min.x) / (newMax.x - newMin.x);
			double yScale = (max.y - min.y) / (newMax.y - newMin.y);
			// correct positions to retain scale and position of prior min and max values
			for (Vector2d np : nodes2newPositions.values()) {
				np.x = (np.x - newMin.x) * xScale + min.x;
				np.y = (np.y - newMin.y) * yScale + min.y;
			}
		}
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, "Random Layout");
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
	
	public void enable3d() {
		this.use3d = true;
	}
}
