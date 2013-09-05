/*******************************************************************************
 * Copyright (c) 2003-2009 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.swig_cola;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

import colajava.AlignmentConstraint;
import colajava.AlignmentConstraintPair;
import colajava.CompoundConstraintsVector;
import colajava.ConstrainedMajorizationLayout;
import colajava.DistributionConstraint;
import colajava.EdgeVector;
import colajava.MultiSeparationConstraint;
import colajava.OffsetPair;
import colajava.RectPtrVector;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn.SBGNgraphHelper;

/**
 * @author Christian Klukas
 */
public class SBGNcolaLayouterAlgorithm
					extends ColaLayouterAlgorithm {
	@Override
	public String getName() {
		return null;
		// return "External SBGN COLA Layout";
	}
	
	@Override
	public String getDescription() {
		return "This command is not yet implemented.";
	}
	
	@Override
	protected ConstrainedMajorizationLayout defineConstraints(
						HashMap<Node, Integer> node2idx, RectPtrVector rs, EdgeVector es) {
		
		ConstrainedMajorizationLayout alg = new ConstrainedMajorizationLayout(rs, es, null, getRvalue());
		
		CompoundConstraintsVector ccsx = new CompoundConstraintsVector();
		CompoundConstraintsVector ccsy = new CompoundConstraintsVector();
		
		for (Node n : node2idx.keySet()) {
			AttributeHelper.setFillColor(n, Color.WHITE);
			AttributeHelper.setOutlineColor(n, Color.BLACK);
		}
		
		for (Node n : node2idx.keySet()) {
			String role = SBGNgraphHelper.getRole(n);
			if (role.equals("Transition")) {
				AttributeHelper.setFillColor(n, Color.LIGHT_GRAY);
				// setup alignment constraint for input and output substances
				ArrayList<Node> inputNodesDegreeGreater1 = new ArrayList<Node>();
				ArrayList<Node> inputNodesDegreeEquals1 = new ArrayList<Node>();
				HashSet<Node> modulationNodes = new LinkedHashSet<Node>();
				ArrayList<Node> outputNodesDegreeEquals1 = new ArrayList<Node>();
				ArrayList<Node> outputNodesDegreeGreater1 = new ArrayList<Node>();
				
				String edgeRole;
				for (Edge e : n.getAllInEdges()) {
					edgeRole = SBGNgraphHelper.getRole(e);
					Node inputNode = e.getSource();
					if (edgeRole.startsWith("ConsumptionProduction")) {
						if (inputNode == n)
							continue;
						if (inputNode.getDegree() > 1)
							inputNodesDegreeGreater1.add(inputNode);
						else
							inputNodesDegreeEquals1.add(inputNode);
					} else {
						if (inputNode.getDegree() == 1)
							modulationNodes.add(inputNode);
					}
				}
				for (Edge e : n.getAllOutEdges()) {
					edgeRole = SBGNgraphHelper.getRole(e);
					Node outputNode = e.getTarget();
					if (edgeRole.startsWith("ConsumptionProduction")) {
						if (outputNode == n)
							continue;
						if (outputNode.getDegree() > 1)
							outputNodesDegreeGreater1.add(outputNode);
						else
							outputNodesDegreeEquals1.add(outputNode);
					} else {
						if (outputNode.getDegree() == 1)
							modulationNodes.add(outputNode);
					}
				}
				
				int defaultDistance = 100;
				colorize(inputNodesDegreeGreater1, Color.BLUE);
				colorize(inputNodesDegreeEquals1, Color.GREEN);
				colorize(modulationNodes, Color.YELLOW);
				colorize(outputNodesDegreeEquals1, Color.RED);
				colorize(outputNodesDegreeGreater1, Color.CYAN);
				defineDistributionConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, inputNodesDegreeGreater1, false);
				defineDistributionConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, inputNodesDegreeEquals1, false);
				
				defineDistributionConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, modulationNodes, true);
				
				defineDistributionConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, outputNodesDegreeEquals1, false);
				defineDistributionConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, outputNodesDegreeGreater1, false);
				
				defaultDistance = 150;
				
				ArrayList<Node> singleNnode = new ArrayList<Node>();
				singleNnode.add(n);
				
				ArrayList<Collection<Node>> list1 = new ArrayList<Collection<Node>>();
				list1.add(inputNodesDegreeGreater1);
				list1.add(inputNodesDegreeEquals1);
				list1.add(singleNnode);
				list1.add(outputNodesDegreeEquals1);
				list1.add(outputNodesDegreeGreater1);
				defineSeparationConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, list1, true);
				
				ArrayList<Collection<Node>> list2 = new ArrayList<Collection<Node>>();
				list2.add(modulationNodes);
				list2.add(singleNnode);
				defineSeparationConstraints(defaultDistance, alg, node2idx, rs, es, ccsx, ccsy, list2, false);
			}
		}
		
		alg.setXConstraints(ccsx);
		alg.setYConstraints(ccsy);
		// alg.setScaling(true);
		// alg.setAvoidOverlaps(false);
		return alg;
	}
	
	private void colorize(Collection<Node> nodes, Color color) {
		for (Node n : nodes)
			AttributeHelper.setOutlineColor(n, color);
	}
	
	private DistributionConstraint defineDistributionConstraints(double defaultDistance,
						ConstrainedMajorizationLayout alg, HashMap<Node, Integer> node2idx,
						RectPtrVector rs, EdgeVector es, CompoundConstraintsVector ccsx,
						CompoundConstraintsVector ccsy,
						Collection<Node> nodes, boolean alongXaxisTrueAlongYaxisFalse) {
		if (nodes.size() < 2)
			return null; // no alignment needed
			
		ArrayList<Node> nodeArray = new ArrayList<Node>(nodes);
		
		DistributionConstraint dc = new DistributionConstraint();
		dc.setSeparation(defaultDistance);
		
		AlignmentConstraint oldAc = null;
		
		for (int i = 0; i < nodeArray.size(); i++) {
			Node a = nodeArray.get(i);
			
			int idxA = node2idx.get(a);
			
			AlignmentConstraint acA = new AlignmentConstraint(getRvalue());
			acA.getOffsets().add(new OffsetPair(idxA, 0));
			
			if (alongXaxisTrueAlongYaxisFalse) {
				ccsx.add(acA);
			} else {
				ccsy.add(acA);
			}
			
			if (oldAc != null) {
				System.out.println(">>> Add Distribution Constraint between previous node and node " + AttributeHelper.getLabel(a, ""));
				dc.getAcs().add(new AlignmentConstraintPair(oldAc, acA));
			} else
				System.out.println(">>> First node is " + AttributeHelper.getLabel(a, ""));
			oldAc = acA;
		}
		
		if (alongXaxisTrueAlongYaxisFalse) {
			System.out.println("^^^ Add Distribution Constraint along X");
			ccsx.add(dc);
		} else {
			System.out.println("^^^ Add Distribution Constraint along Y");
			ccsy.add(dc);
		}
		
		return dc;
	}
	
	private void defineSeparationConstraints(int defaultDistance,
						ConstrainedMajorizationLayout alg, HashMap<Node, Integer> node2idx,
						RectPtrVector rs, EdgeVector es, CompoundConstraintsVector ccsx,
						CompoundConstraintsVector ccsy, ArrayList<Collection<Node>> nodeLists,
						boolean alongXaxisTrueAlongYaxisFalse) {
		
		System.out.println("___ ___ multi Separation Constraint will be defined:");
		
		MultiSeparationConstraint msc = new MultiSeparationConstraint(defaultDistance, true);
		
		AlignmentConstraint oldAlignment = null;
		
		for (int i = 0; i < nodeLists.size(); i++) {
			Collection<Node> nodeList = nodeLists.get(i);
			
			if (nodeList != null && nodeList.size() > 0) {
				AlignmentConstraint ac = new AlignmentConstraint(getRvalue());
				for (Node n : nodeList) {
					System.out.println("Add Alignment Constraint for Node " + AttributeHelper.getLabel(n, ""));
					ac.getOffsets().add(new OffsetPair(node2idx.get(n), 0));
				}
				if (alongXaxisTrueAlongYaxisFalse) {
					System.out.println("^^^ Alignment Constraint as X constraint");
					ccsx.add(ac);
				} else {
					System.out.println("^^^ Alignment Constraint as Y constraint");
					ccsy.add(ac);
				}
				
				if (oldAlignment != null) {
					System.out.println(">>> Separation Constraint between Alignment Constraint current and previous");
					msc.getAcs().add(new AlignmentConstraintPair(oldAlignment, ac));
				}
				oldAlignment = ac;
			}
		}
		
		if (alongXaxisTrueAlongYaxisFalse) {
			System.out.println("^^^ ^^^ multi Separation Constraint as X constraint");
			ccsx.add(msc);
		} else {
			System.out.println("^^^ ^^^ multi Separation Constraint as Y constraint");
			ccsy.add(msc);
		}
	}
	
	private double getRvalue() {
		return Math.random() * 100 + 100;
	}
}
