/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sif;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.AbstractOutputSerializer;

public class SIFWriter extends AbstractOutputSerializer {
	
	private final String fileNameExt = ".sif";
	
	public SIFWriter() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	@Override
	public String[] getExtensions() {
		return new String[] { fileNameExt };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	@Override
	public String[] getFileTypeDescriptions() {
		return new String[] { "SIF (src interac. tgt)" };
	}
	
	@Override
	public void write(OutputStream out, Graph g) throws IOException {
		g.numberGraphElements();
		PrintStream stream = new PrintStream(out);
		for (Edge ed : g.getEdges())
			stream.print(getLabel(ed.getSource()) + " " + getLabel(ed) + " " + getLabel(ed.getTarget()) + "\n");
		for (Node nd : g.getNodes())
			if (nd.getInDegree() <= 0)
				stream.print(getLabel(nd) + "\n");
	}
	
	private String getLabel(GraphElement ge) {
		String s = null;
		if (ge instanceof Node) {
			s = AttributeHelper.getLabel(ge, null);
			if (s == null || s.length() <= 0)
				s = "node_" + ge.getID();
		} else {
			s = AttributeHelper.getLabel(ge, null);
			if (s == null || s.length() <= 0)
				s = "unknown";
		}
		return StringManipulationTools.stringReplace(s, " ", "_");
	}
}
