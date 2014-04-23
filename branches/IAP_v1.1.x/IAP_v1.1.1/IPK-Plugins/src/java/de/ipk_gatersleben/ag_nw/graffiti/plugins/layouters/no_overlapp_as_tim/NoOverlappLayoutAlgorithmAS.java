/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.no_overlapp_as_tim;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.session.EditorSession;

import placement.QPRectanglePlacement;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Epands the layout to remove any overlapping of nodes.
 * 
 * @author Christian Klukas
 */
public class NoOverlappLayoutAlgorithmAS extends AbstractAlgorithm {
	
	private double spaceX = 10, spaceY = 10;
	private boolean doNotAskForParameters = false;
	private boolean considerGraphViewComponents = true;
	
	public NoOverlappLayoutAlgorithmAS() {
		super();
		doNotAskForParameters = false;
	}
	
	public NoOverlappLayoutAlgorithmAS(int spaceX, int spaceY) {
		doNotAskForParameters = true;
		considerGraphViewComponents = true;
		this.spaceX = spaceX;
		this.spaceY = spaceY;
	}
	
	@Override
	public void reset() {
		super.reset();
		spaceX = 10d;
		spaceY = 10d;
		considerGraphViewComponents = true;
	}
	
	public String getName() {
		return "Remove Node Overlaps";
	}
	
	/**
	 * Checks, if a graph was given and that the radius is positive.
	 * 
	 * @throws PreconditionException
	 *            if no graph was given during algorithm
	 *            invocation or the radius is negative
	 */
	@Override
	public void check() throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
		
		if (graph.getNumberOfNodes() <= 0) {
			throw new PreconditionException("The graph is empty. Cannot run layouter.");
		}
		
	}
	
	/**
	 * Performs the layout.
	 */
	public void execute() {
		View view = null;
		if (considerGraphViewComponents) {
			try {
				EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
				view = session.getActiveView();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		Collection<Node> workNodesC = getSelectedOrAllNodes();
		workNodesC = GraphHelper.getVisibleNodes(workNodesC);
		Node[] workNodes = workNodesC.toArray(new Node[] {});
		// call Tims algorithm
		
		ArrayList<Rectangle2D> myRectangles = new ArrayList<Rectangle2D>();
		ArrayList<Vector2d> offsets = new ArrayList<Vector2d>();
		HashSet<String> knownPositions = new HashSet<String>();
		for (Node n : workNodes) {
			double x, y, w, h;
			x = AttributeHelper.getPositionX(n);
			y = AttributeHelper.getPositionY(n);
			// the algorithm does not work correctly, if nodes are exactly on the same position
			w = AttributeHelper.getWidth(n);
			h = AttributeHelper.getHeight(n);
			Rectangle2D newRect = new Rectangle2D.Double(x - w / 2d, y - h / 2d, w, h);
			if (considerGraphViewComponents && view != null) {
				GraphElementComponent gvc = view.getComponentForElement(n);
				if (gvc != null)
					for (Object o : gvc.getAttributeComponents()) {
						if (o instanceof JComponent) {
							JComponent jc = (JComponent) o;
							newRect.add(jc.getBounds());
						}
					}
				double nX = x - (newRect.getCenterX());
				double nY = y - (newRect.getCenterY());
				offsets.add(new Vector2d(nX, nY));
			}
			int xCheck = (int) (newRect.getCenterX());
			int yCheck = (int) (newRect.getCenterY());
			while (knownPositions.contains(xCheck + "$" + yCheck)) {
				xCheck += 1;
				newRect = new Rectangle2D.Double(newRect.getX() + 1d, newRect.getY(), newRect.getWidth(), newRect.getHeight());
				System.out.println("Node position correction");
			}
			knownPositions.add(x + "$" + y);
			myRectangles.add(newRect);
		}
		try {
			QPRectanglePlacement rp = new QPRectanglePlacement(true, false, false,
								false, spaceX, spaceY, false);
			rp.place(myRectangles);
			
			HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
			for (Node n : workNodes) {
				Rectangle2D.Double r = (Double) myRectangles.remove(0);
				
				double xx = r.getX() + r.width / 2d;
				double yy = r.getY() + r.height / 2d;
				
				if (considerGraphViewComponents && view != null) {
					Vector2d correction = offsets.remove(0);
					xx += correction.x;
					yy += correction.y;
				}
				nodes2newPositions.put(n, new Vector2d(xx, yy));
			}
			GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, getName());
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
		}
	}
	
	/**
	 * Returns the parameter object for the radius.
	 * 
	 * @return the parameter array
	 */
	@Override
	public Parameter[] getParameters() {
		if (doNotAskForParameters)
			return null;
		DoubleParameter spaceParamX = new DoubleParameter(spaceX, "Gap between Nodes (X)", "Specify the minimum horizontal space between all nodes");
		DoubleParameter spaceParamY = new DoubleParameter(spaceY, "Gap between Nodes (Y)", "Specify the minimum vertical space between all nodes");
		BooleanParameter considerView = new BooleanParameter(considerGraphViewComponents, "Consider View Components",
							"If enabled, graphical annotations, like the node labels will be considered and processed - Enable");
		return new Parameter[] { spaceParamX, spaceParamY, considerView };
	}
	
	/**
	 * Sets the radius parameter to the given value.
	 * 
	 * @param params
	 *           An array with exact one DoubleParameter.
	 */
	@Override
	public void setParameters(Parameter[] params) {
		if (doNotAskForParameters)
			return;
		this.parameters = params;
		int i = 0;
		spaceX = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		spaceY = ((DoubleParameter) params[i++]).getDouble().doubleValue();
		considerGraphViewComponents = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getCategory()
	 */
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}
