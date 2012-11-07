/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.io.AbstractOutputSerializer;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.SupportsWriterOutput;

public class SBML_XML_Writer extends AbstractOutputSerializer implements OutputSerializer, SupportsWriterOutput {
	
	/**
	 * Constructor
	 */
	public SBML_XML_Writer() {
		super();
	}
	
	/**
	 * Implemented method of interface InputSerializer.java
	 */
	public void write(Writer writer, Graph g) {
	}
	
	/**
	 * Return the file extension that can be read in.
	 * Implemented method of interface InputSerializer.java
	 */
	public String[] getExtensions() {
		return new String[] { ".sbml" };
	}
	
	/**
	 * returns the file type description.
	 * Implemented method of interface InputSerializer.java
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "SBML" };
	}
	
	/**
	 * starts the reading in of the model
	 * 
	 * @param stream
	 *           contains the model
	 * @param g
	 *           the data will be read into this data structure
	 */
	public void write(OutputStream stream, Graph g) throws IOException {
		SBML_SBML_Writer writeSBML = new SBML_SBML_Writer();
		writeSBML.addSBML(stream, g);
	}
}
