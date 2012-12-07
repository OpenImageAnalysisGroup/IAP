package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.metatool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.OutputSerializer;

public class MetatoolWriter implements OutputSerializer {
	
	public static String getNodeLabel(Node nd) {
		return StringManipulationTools.removeHTMLtags(AttributeHelper.getLabel(nd, "<no label>"));
	}
	
	public static String getEdgeLabel(Edge ed) {
		String label = StringManipulationTools.removeHTMLtags(AttributeHelper.getLabel(-1, ed).getLabel());
		if (label == null || label.equals("1"))
			label = "";
		return label;
	}
	
	@Override
	public void write(OutputStream stream, Graph g) throws IOException {
		
		// get enzymes of reactions
		HashSet<Node> reversibles = new HashSet<Node>();
		HashSet<Node> irreversibles = new HashSet<Node>();
		// get metabolites of reaction
		HashSet<Node> internalMetab = new HashSet<Node>();
		HashSet<Node> externalMetab = new HashSet<Node>();
		
		for (Node nd : g.getNodes()) {
			MetatoolNodeType type = getNodeType(nd);
			if (type == null)
				ErrorMsg.addErrorMessage("Could not determine type of node \"" + nd + "\"!");
			else {
				switch (type) {
					case REVERSIBLE_REACTION:
						reversibles.add(nd);
						break;
					case IRREVERSIBLE_REACTION:
						irreversibles.add(nd);
						break;
					case EXTERNAL_METABOLITE:
						externalMetab.add(nd);
						break;
					case INTERNAL_METABOLITE:
						internalMetab.add(nd);
						break;
				}
			}
		}
		
		// get reactions
		
		HashSet<SBMLReaction> reactions = new HashSet<SBMLReaction>();
		
		for (Node nd : reversibles)
			reactions.add(new SBMLReaction(nd));
		for (Node nd : irreversibles)
			reactions.add(new SBMLReaction(nd));
		
		// write file
		PrintStream p;
		try {
			p = new PrintStream(stream, true, StringManipulationTools.Unicode);
		} catch (UnsupportedEncodingException e) {
			ErrorMsg.addErrorMessage(e);
			p = new PrintStream(stream, false);
		}
		
		HashSet<String> unique = new HashSet<String>();
		p.println("-ENZREV");
		for (Node nd : reversibles) {
			String lbl = getNodeLabel(nd);
			if (!unique.contains(lbl)) {
				p.print(lbl + " ");
				unique.add(lbl);
			}
		}
		p.println();
		p.println();
		
		p.println("-ENZIRREV");
		unique.clear();
		for (Node nd : irreversibles) {
			String lbl = getNodeLabel(nd);
			if (!unique.contains(lbl)) {
				p.print(lbl + " ");
				unique.add(lbl);
			}
		}
		p.println();
		p.println();
		
		p.println("-METINT");
		unique.clear();
		for (Node nd : internalMetab) {
			String lbl = getNodeLabel(nd);
			if (!unique.contains(lbl)) {
				p.print(lbl + " ");
				unique.add(lbl);
			}
		}
		p.println();
		p.println();
		
		p.println("-METEXT");
		unique.clear();
		for (Node nd : externalMetab) {
			String lbl = getNodeLabel(nd);
			if (!unique.contains(lbl)) {
				p.print(lbl + " ");
				unique.add(lbl);
			}
		}
		
		p.println();
		p.println();
		
		boolean wronmgmetabolites = false, stoiwrong = false;
		p.println("-CAT");
		for (SBMLReaction r : reactions) {
			p.println(r.write());
			if (r.isNotEnoughMetabolites())
				wronmgmetabolites = true;
			if (r.isStoiWrong())
				stoiwrong = true;
		}
		p.println();
		
		if (stoiwrong)
			ErrorMsg.addErrorMessage("Stoichiometric coefficient is not a number in some reaction");
		
		if (wronmgmetabolites)
			ErrorMsg.addErrorMessage("<html>Some products or reactants not well defined! It is inevitable to assign SBML-roles to the edges.<br>" +
					"This may be achieved using the FBASimVis command \"Create SBML-file\".");
		
	}
	
	private MetatoolNodeType getNodeType(Node nd) {
		String lbl = getNodeLabel(nd);
		if (lbl == null)
			return null;
		
		if (isEnzyme(nd)) {
			if (AttributeHelper.isSBMLreversibleReaction(nd))
				return MetatoolNodeType.REVERSIBLE_REACTION;
			else
				return MetatoolNodeType.IRREVERSIBLE_REACTION;
		} else {
			if (isExternalMetabolite(lbl))
				return MetatoolNodeType.EXTERNAL_METABOLITE;
			else
				return MetatoolNodeType.INTERNAL_METABOLITE;
		}
	}
	
	private boolean isExternalMetabolite(String lbl) {
		// TODO: or something else? errorcheck!
		return lbl != null && lbl.endsWith("_ex");
	}
	
	private boolean isEnzyme(Node nd) {
		String shape = AttributeHelper.getShape(nd);
		// Vanted may have nodes without a shape-attribute, which will be visualised as rectangles
		return shape == null || shape.length() <= 0 || shape.contains("Rectangle") || shape.contains("rectangle");
	}
	
	@Override
	public String[] getExtensions() {
		return new String[] { ".dat" };
	}
	
	@Override
	public String[] getFileTypeDescriptions() {
		return new String[] { "Metatool" };
	}
	
	private enum MetatoolNodeType {
		REVERSIBLE_REACTION, IRREVERSIBLE_REACTION, EXTERNAL_METABOLITE, INTERNAL_METABOLITE
		
	}
	
}
