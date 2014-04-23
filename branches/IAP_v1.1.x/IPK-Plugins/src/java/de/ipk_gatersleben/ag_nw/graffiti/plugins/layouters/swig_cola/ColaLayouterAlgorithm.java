/*******************************************************************************
 * Copyright (c) 2003-2009 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.swig_cola;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import colajava.AlignmentConstraint;
import colajava.AlignmentConstraintPair;
import colajava.BoundaryConstraint;
import colajava.ColaEdge;
import colajava.CompoundConstraintsVector;
import colajava.ConstrainedMajorizationLayout;
import colajava.DistributionConstraint;
import colajava.EdgeVector;
import colajava.OffsetList;
import colajava.OffsetPair;
import colajava.RectPtrVector;
import colajava.Rectangle;
import colajava.SeparationConstraint;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author Christian Klukas
 */
public class ColaLayouterAlgorithm
					extends AbstractAlgorithm {
	
	private double idealLength = 80;
	
	/**
	 * Returns the name of the algorithm.
	 * 
	 * @return the name of the algorithm
	 */
	public String getName() {
		return null;
		// return "External COLA Layout";
	}
	
	@Override
	public String getCategory() {
		return "Layout";
	}
	
	@Override
	public String getDescription() {
		return "This command is not yet implemented.";
	}
	
	private static boolean libLoaded = false;
	
	@Override
	public void check()
						throws PreconditionException {
		PreconditionException errors = new PreconditionException();
		
		if (graph == null) {
			errors.add("No graph available!");
			throw errors;
		}
		
		if (!libLoaded) {
			String lib = "/Users/klukas/Documents/Programmierung/Adaptagrams/cola/libcolajni.dylib";
			// String lib = "/home/klukas/Desktop/tims_daten/cola/libcolajni.so";
			try {
				System.load(lib);
				libLoaded = true;
			} catch (java.lang.UnsatisfiedLinkError e) {
				errors.add("Can't load external library: " + lib);
			}
		}
		
		if (!errors.isEmpty()) {
			throw errors;
		}
	}
	
	public void execute() {
		if (getSelectedOrAllNodes().size() < 5) {
			MainFrame.showMessage("At least 4 nodes need to be laid out using this demo.", MessageType.INFO);
			return;
		}
		
		HashMap<Node, Vector2d> nodes2newPositions = new HashMap<Node, Vector2d>();
		Collection<Node> work = getSelectedOrAllNodes();
		
		RectPtrVector rs = new RectPtrVector();
		HashMap<Node, Integer> node2idx = Graph2ColaHelper.getNode2IdxMap(work);
		HashMap<Rectangle, Node> rect2node = new HashMap<Rectangle, Node>();
		HashSet<Edge> edges = new HashSet<Edge>();
		for (Node n : work) {
			if (AttributeHelper.isHiddenGraphElement(n))
				continue;
			Rectangle rr = Graph2ColaHelper.getRectangle(n);
			System.out.println(rr.getMinX() + "/" + rr.getMinY() + "/" + rr.getMaxX() + "/" + rr.getMaxY());
			rs.add(rr);
			rect2node.put(rr, n);
			for (Edge e : n.getEdges()) {
				Integer a = node2idx.get(e.getSource());
				Integer b = node2idx.get(e.getTarget());
				if (a != null && b != null)
					edges.add(e);
			}
		}
		EdgeVector es = new EdgeVector();
		for (Edge e : edges) {
			ColaEdge ce = Graph2ColaHelper.getEdge(node2idx, e);
			if (ce != null)
				es.add(ce);
		}
		
		ConstrainedMajorizationLayout alg = defineConstraints(node2idx, rs, es);
		
		try {
			alg.run();
		} catch (RuntimeException err) {
			ErrorMsg.addErrorMessage(err);
		}
		
		for (Map.Entry<Rectangle, Node> entry : rect2node.entrySet()) {
			double newX = entry.getKey().getCentreX();
			double newY = entry.getKey().getCentreY();
			nodes2newPositions.put(entry.getValue(), new Vector2d(newX, newY));
		}
		
		GraphHelper.applyUndoableNodePositionUpdate(nodes2newPositions, "COLA Layout");
	}
	
	protected ConstrainedMajorizationLayout defineConstraints(HashMap<Node, Integer> node2idx, RectPtrVector rs,
						EdgeVector es) {
		// ConstrainedFDLayout alg = new ConstrainedFDLayout(rs, es, idealLength);
		ConstrainedMajorizationLayout alg = new ConstrainedMajorizationLayout(rs, es, null, idealLength * 2);
		
		AlignmentConstraint ac03x = new AlignmentConstraint(200);
		ac03x.setIsFixed(false);
		OffsetList ol = new OffsetList();
		ol.add(new OffsetPair(0, 0));
		ol.add(new OffsetPair(3, 0));
		ac03x.setOffsets(ol);
		
		AlignmentConstraint ac14x = new AlignmentConstraint(400);
		ac14x.setIsFixed(false);
		OffsetList ol2 = new OffsetList();
		ol2.add(new OffsetPair(1, 0));
		ol2.add(new OffsetPair(4, 0));
		ac14x.setOffsets(ol2);
		
		BoundaryConstraint bc = new BoundaryConstraint(300);
		
		bc.getLeftOffsets().add(new OffsetPair(6, 150));
		bc.getRightOffsets().add(new OffsetPair(5, 250));
		bc.getRightOffsets().add(new OffsetPair(1, 50));
		
		SeparationConstraint sc = new SeparationConstraint(ac03x, ac14x, 500, true);
		// SeparationConstraint sc = new SeparationConstraint(1, 3, 500, true);
		
		CompoundConstraintsVector ccsx = new CompoundConstraintsVector();
		ccsx.add(ac03x);
		ccsx.add(ac14x);
		
		ccsx.add(bc);
		
		// CompoundConstraintsVector ccsy = new CompoundConstraintsVector();
		ccsx.add(sc);
		
		DistributionConstraint dt = new DistributionConstraint();
		dt.setSeparation(50);
		// dt.setSep(20);
		
		AlignmentConstraint g1, g2, g3;
		
		g1 = getAlignmentConstraint(50, new int[] { 7, 8 });
		g2 = getAlignmentConstraint(50, new int[] { 9 });
		g3 = getAlignmentConstraint(50, new int[] { 10 });
		// // g1.getOffsets().add(new OffsetPair())
		//
		ccsx.add(g1);
		ccsx.add(g2);
		ccsx.add(g3);
		
		dt.getAcs().add(new AlignmentConstraintPair(g1, g2));
		dt.getAcs().add(new AlignmentConstraintPair(g2, g3));
		
		ccsx.add(dt);
		
		// for (AlignmentConstraintPair acp : getAlignmentPairs(new AlignmentConstraint[]{g1,g2,g3}))
		// dt.getAcs().add(acp);
		
		// sc.getAl().getOffsets().a
		
		alg.setXConstraints(ccsx);
		alg.setScaling(true);
		alg.setAvoidOverlaps();
		return alg;
	}
	
	private AlignmentConstraint getAlignmentConstraint(int pos, int[] indexlist) {
		AlignmentConstraint result = new AlignmentConstraint(pos);
		for (int i : indexlist)
			result.getOffsets().add(new OffsetPair(i, 0));
		return result;
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return true;
	}
}
