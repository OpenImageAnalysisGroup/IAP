/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kegg_xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.OutputSerializer;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

public class KEGG2_XML_Writer implements OutputSerializer {
	private String fileNameExt = ".xml";
	
	public void write(OutputStream stream, Graph g) throws IOException {
		Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
		Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
		Pathway p = Pathway.getPathwayFromGraph(g, warnings, errors, null);
		if (warnings.size() > 0 || errors.size() > 0)
			Pathway.showKgmlErrors(errors, warnings);
		Document d = p.getKgmlDocument();
		XMLOutputter out = new XMLOutputter();
		out.setFormat(Format.getPrettyFormat());
		out.output(d, stream);
		stream.close();
	}
	
	public String[] getExtensions() {
		return new String[] { fileNameExt };
	}
	
	public String[] getFileTypeDescriptions() {
		return new String[] { "KGML File" };
	}
	
}
