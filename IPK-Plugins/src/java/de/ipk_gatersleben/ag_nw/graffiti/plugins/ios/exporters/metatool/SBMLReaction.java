package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.metatool;

import java.util.HashMap;
import java.util.Map;

import org.AttributeHelper;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

public class SBMLReaction {
	HashMap<Node, Edge> reactants, products;
	String reactantStoichiometry, productStoichiometry;
	private final String enzymename;
	private boolean notEnoughMetabolites = false;
	private boolean stoiWrong = false;
	
	public SBMLReaction(Node enzyme) {
		reactants = new HashMap<Node, Edge>();
		products = new HashMap<Node, Edge>();
		enzymename = MetatoolWriter.getNodeLabel(enzyme);
		
		for (Edge ed : enzyme.getEdges()) {
			Node othernode = null;
			
			if (ed.getSource().equals(enzyme)) {
				othernode = ed.getTarget();
			} else {
				othernode = ed.getSource();
			}
			
			if (AttributeHelper.getSBMLrole(ed).equals("reactant")) {
				reactants.put(othernode, ed);
				checkEdge(ed);
			}
			
			if (AttributeHelper.getSBMLrole(ed).equals("product")) {
				products.put(othernode, ed);
				checkEdge(ed);
			}
		}
		
		if (reactants.size() <= 0 && products.size() <= 0)
			notEnoughMetabolites = true;
		
		if (reactants.size() <= 0 || products.size() <= 0)
			notEnoughMetabolites = true;// System.err.println("Reaction of enzyme " + enzymename + " detected with no products or reactants");
	}
	
	private void checkEdge(Edge ed) {
		if (stoiWrong)
			return;
		String label = MetatoolWriter.getEdgeLabel(ed);
		if (label == null || label.equals(""))
			return;
		
		boolean isInt = true, isDouble = true;
		try {
			Integer.parseInt(label);
			return;
		} catch (NumberFormatException e) {
			isInt = false;
		}
		try {
			Double.parseDouble(label);
			return;
		} catch (NumberFormatException e) {
			isDouble = false;
		}
		
		if (!isDouble && !isInt)
			stoiWrong = true;
	}
	
	public String write() {
		if (notEnoughMetabolites)
			return "errors occured in creating reaction of " + enzymename;
		
		String text = enzymename + " : ";
		
		for (Map.Entry<Node, Edge> entry : reactants.entrySet()) {
			text += MetatoolWriter.getEdgeLabel(entry.getValue()) + " " + MetatoolWriter.getNodeLabel(entry.getKey()) + " + ";
		}
		if (reactants.size() > 0)
			text = text.substring(0, text.length() - " + ".length()) + " = ";
		
		for (Map.Entry<Node, Edge> entry : products.entrySet()) {
			text += MetatoolWriter.getEdgeLabel(entry.getValue()) + " " + MetatoolWriter.getNodeLabel(entry.getKey()) + " + ";
		}
		if (reactants.size() > 0)
			text = text.substring(0, text.length() - " + ".length());
		
		return text;
	}
	
	public boolean isNotEnoughMetabolites() {
		return notEnoughMetabolites;
	}
	
	public boolean isStoiWrong() {
		return stoiWrong;
	}
	
}