/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.io.IOException;
import java.io.OutputStream;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractOutputSerializer;

public class SBML_XML_Writer extends AbstractOutputSerializer {
	
	private String fileNameExt = ".sbml";
	
	public SBML_XML_Writer() {
		super();
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
		return new String[] { "SBML2 File" };
	}
	
	public void write(OutputStream stream, Graph g) throws IOException {
		//
		
	}
}
