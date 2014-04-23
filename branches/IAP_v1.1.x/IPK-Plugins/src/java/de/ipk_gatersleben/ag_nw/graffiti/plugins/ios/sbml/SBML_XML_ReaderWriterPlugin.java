/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import org.apache.log4j.Logger;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_XML_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_XML_Reader;

/**
 * DOCUMENT ME!
 * 
 * @author Christian Klukas
 */
public class SBML_XML_ReaderWriterPlugin extends AddonAdapter {
	private static Logger logger = Logger.getRootLogger();
	
	/*
	 * this static variable is to be set to true,when this plugin is doing
	 * a JUnit test.
	 * So in the plugin several code will not be executed which includes GUI dialogs etc.
	 */
	public static boolean isTestintMode = false;
	
	@Override
	protected void initializeAddon() {
		try {
			// SimpleLayout layout = new SimpleLayout();
			// ConsoleAppender consoleAppender = new ConsoleAppender( layout );
			// logger.addAppender( consoleAppender );
			// FileAppender fileAppender = new FileAppender( layout,
			// "log/jsbml.log", false );
			// logger.addAppender( fileAppender );
			SBML_XML_Reader reader = new SBML_XML_Reader();
			this.inputSerializers = new InputSerializer[] { reader };
			this.outputSerializers = new OutputSerializer[] { new SBML_XML_Writer() };
			
			System.out.println("SBML_XML_Reader Writer Plugin started");
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}	
	}

	public SBML_XML_ReaderWriterPlugin() {
		super();
		initializeAddon();
	}
}
