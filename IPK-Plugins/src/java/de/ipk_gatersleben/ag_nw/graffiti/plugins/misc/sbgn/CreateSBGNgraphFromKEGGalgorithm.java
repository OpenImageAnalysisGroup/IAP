package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.sbgn;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugins.attributecomponents.simplelabel.LabelComponent;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn.SBGNarc;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn.SBGNgraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn.SBGNitem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

public class CreateSBGNgraphFromKEGGalgorithm extends AbstractAlgorithm {
	
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		try {
			processNodes();
		} finally {
			graph.getListenerManager().transactionFinished(this, true);
			GraphHelper.issueCompleteRedrawForGraph(graph);
		}
	}
	
	private void processNodes() {
		boolean autoInterpet = true;
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.addAll(getSelectedOrAllNodes());
		HashSet<String> knownCompounds = new HashSet<String>();
		HashSet<String> multipleCompounds = new HashSet<String>();
		for (Node n : nodes) {
			String keggtype = KeggGmlHelper.getKeggType(n);
			if (keggtype.equals("compound")) {
				String lbl = AttributeHelper.getLabel(n, "");
				if (lbl.length() > 0) {
					if (knownCompounds.contains(lbl))
						multipleCompounds.add(lbl);
					knownCompounds.add(lbl);
				}
			}
		}
		for (Node n : nodes) {
			String keggtype = KeggGmlHelper.getKeggType(n);
			if (keggtype == null || keggtype.length() <= 0)
				continue;
			if (keggtype.equals("map")) {
				SBGNgraphHelper.addItemOrSetStyle(n, SBGNitem.Submap, null, null, null, null, null, false, null, true);
				continue;
			}
			if (keggtype.equals("compound")) {
				String lbl = AttributeHelper.getLabel(n, "");
				SBGNgraphHelper.addItemOrSetStyle(n, SBGNitem.SimpleChemical, null, null, null, null, null,
									lbl.length() > 0 && multipleCompounds.contains(lbl),
									null, true);
				continue;
			}
			if (keggtype.equals("enzyme") || keggtype.equals("gene") || keggtype.equals("gene")
								|| keggtype.equals("ortholog")) {
				processEnzyme(n);
				continue;
			}
			if (autoInterpet) {
				String label = AttributeHelper.getLabel(n, null);
				if (label != null) {
					EnzymeEntry enzymeEntry = EnzymeService.getEnzymeInformation(label, false);
					if (enzymeEntry != null && enzymeEntry.isValid()) {
						processEnzyme(n);
						continue;
					} else {
						CompoundEntry compoundEntry = CompoundService.getInformation(label);
						if (compoundEntry != null && compoundEntry.isValid()) {
							if (compoundEntry.isValid()) {
								SBGNgraphHelper.addItemOrSetStyle(n, SBGNitem.SimpleChemical, null, null, null, null, null, false, null, true);
								continue;
							}
						}
					}
				}
			}
		}
	}
	
	private void processEnzyme(Node n) {
		String sbgntype = (String) AttributeHelper.getAttributeValue(n, "sbgn", "role", "", "", false);
		if (sbgntype.equals("MacroMolecule"))
			return; // already processed
		Node newEnzymeNode = n.getGraph().addNodeCopy(n);
		
		SBGNgraphHelper.addItemOrSetStyle(newEnzymeNode, SBGNitem.MacroMolecule, null, null, null, null, null, false, null, true);
		SBGNgraphHelper.addItemOrSetStyle(n, SBGNitem.Transition, null, null, null, null, null, false, null, true);
		
		boolean reversible = !allOutgoingEdges(n);
		
		String setting = LabelComponent.getBestAutoOutsideSetting(n);
		boolean vert;
		if (setting != null && (setting.equals(GraphicAttributeConstants.LEFT) || setting.equals(GraphicAttributeConstants.RIGHT))) {
			SBGNgraphHelper.setEdgeStyle(this, n.getEdges(), SBGNarc.ConsumptionProductionVert, "", "", reversible);
			vert = true;
		} else {
			SBGNgraphHelper.setEdgeStyle(this, n.getEdges(), SBGNarc.ConsumptionProductionHor, "", "", reversible);
			vert = false;
		}
		
		Edge newEdge = n.getGraph().addEdge(newEnzymeNode, n, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
		ArrayList<Edge> newEdges = new ArrayList<Edge>();
		newEdges.add(newEdge);
		SBGNgraphHelper.setEdgeStyle(this, newEdges, SBGNarc.Catalysis, "", "", reversible);
		
		org.Vector2d pos = AttributeHelper.getPositionVec2d(newEnzymeNode);
		org.Vector2d size = AttributeHelper.getSize(newEnzymeNode);
		if (vert) {
			pos.x += 70;
			int o1 = GraphHelper.countOverlapps(n.getGraph(), pos, size);
			pos.x -= 140;
			int o2 = GraphHelper.countOverlapps(n.getGraph(), pos, size);
			if (o1 <= o2)
				pos.x += 140;
		} else {
			pos.y -= 50;
			int o1 = GraphHelper.countOverlapps(n.getGraph(), pos, size);
			pos.y += 100;
			int o2 = GraphHelper.countOverlapps(n.getGraph(), pos, size);
			if (o1 <= o2)
				pos.y -= 100;
			int om = o1 < o2 ? o1 : o2;
			pos.x += 70;
			int o3 = GraphHelper.countOverlapps(n.getGraph(), pos, size);
			if (o3 < om) {
				//
			} else
				pos.x -= 70;
		}
		AttributeHelper.setPosition(newEnzymeNode, pos);
	}
	
	private boolean allOutgoingEdges(Node n) {
		boolean result = true;
		for (Edge e : n.getEdges()) {
			if (e.getTarget() == n && hasTargetArrowShape(e))
				return false;
			if (e.getSource() == n && hasTailArrowShape(e))
				return false;
		}
		return result;
	}
	
	private boolean hasTailArrowShape(Edge e) {
		String tail = AttributeHelper.getArrowtail(e);
		return tail != null && tail.length() > 0;
	}
	
	private boolean hasTargetArrowShape(Edge e) {
		String head = AttributeHelper.getArrowhead(e);
		return head != null && head.length() > 0;
	}
	
	public String getName() {
		return "Create SBGN Graph from KEGG Pathway";
	}
	
}
