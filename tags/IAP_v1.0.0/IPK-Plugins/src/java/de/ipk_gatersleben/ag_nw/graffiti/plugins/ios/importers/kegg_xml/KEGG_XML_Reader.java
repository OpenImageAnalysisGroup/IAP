/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kegg_xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractInputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author Christian Klukas
 *         (c) 2004-2006 IPK-Gatersleben
 */
public class KEGG_XML_Reader
					extends AbstractInputSerializer {
	
	private String fileNameExt = ".xml";
	
	/**
	 *
	 */
	public KEGG_XML_Reader() {
		super();
	}
	
	@Override
	public void read(String filename, Graph g) throws IOException {
		super.read(filename, g);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.AbstractInputSerializer#read(java.io.InputStream, org.graffiti.graph.Graph)
	 */
	@Override
	public void read(InputStream in, Graph g)
						throws IOException {
		KeggService.loadKeggPathwayIntoGraph(in, g, KeggService.getDefaultEnzymeColor());
		in.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { fileNameExt };
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
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "KEGG XML Pathway" };
	}
	
	public void read(Reader reader, Graph g) throws Exception {
		MainFrame.showMessageDialog("Not implemented!", "Error");
	}
}
