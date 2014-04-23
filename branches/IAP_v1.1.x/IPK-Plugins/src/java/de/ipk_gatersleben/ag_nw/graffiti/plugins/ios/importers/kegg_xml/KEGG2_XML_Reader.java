/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kegg_xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

/**
 * @author Christian Klukas
 *         (c) 2004-2006 IPK-Gatersleben
 */
public class KEGG2_XML_Reader extends AbstractInputSerializer {
	
	private final String fileNameExt = ".xml";
	
	/**
	 *
	 */
	public KEGG2_XML_Reader() {
		super();
	}
	
	@Override
	public void read(String filename, Graph g) throws IOException {
		super.read(filename, g);
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * org.graffiti.plugin.io.AbstractInputSerializer#read(java.io.InputStream,
	 * org.graffiti.graph.Graph)
	 */
	@Override
	public void read(InputStream in, Graph g) throws IOException {
		Pathway p = Pathway.getPathwayFromKGML(in);
		// Pathway.testShowPathwayInfo(p);
		p.getGraph(g);
		in.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { fileNameExt };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "KGML File" };
	}
	
	public void read(Reader reader, Graph g) throws Exception {
		Pathway p = Pathway.getPathwayFromKGML(reader);
		p.getGraph(g);
		reader.close();
	}
	
	@Override
	public boolean validFor(InputStream reader) {
		try {
			int maxAnalyze = 5;
			TextFile tf = new TextFile(reader, maxAnalyze);
			for (String line : tf) {
				if (line.toUpperCase().indexOf("<PATHWAY") >= 0)
					return true;
				maxAnalyze--;
				if (maxAnalyze == 0)
					break;
			}
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return false;
	}
}
