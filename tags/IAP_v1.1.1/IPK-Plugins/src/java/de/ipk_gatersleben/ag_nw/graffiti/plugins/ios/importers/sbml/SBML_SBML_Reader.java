/**
 * This class reads in the SBML Tag
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.HashMap;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.SBMLDocument;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_SBML_Reader extends SBML_SBase_Reader {
	
	/**
	 * Method reads in the model tag information and is called from class
	 * SBML_XML_Reader.java
	 * 
	 * @param document
	 *           contains the model for the import
	 * @param g
	 *           the data structure for reading in the information
	 */
	public void addSBML(SBMLDocument document, Graph g) {
		SBML_Constants.init();
		namespaceCollector = new HashMap<String, String>();
		addNamespaces(document.getSBMLDocumentNamespaces(), g,
				SBML_Constants.SBML, SBML_Constants.NAMESPACE);
		
		int level = document.getLevel();
		int version = document.getVersion();
		String metaID = document.getMetaId();
		String sboTerm = document.getSBOTermID();
		
		if (document.isSetNotes()) {
			addNotes(document.getNotes(), document.getNotesString(), g,
					SBML_Constants.SBML, SBML_Constants.SBML_NOTES);
		}
		if (!metaID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, SBML_Constants.SBML,
					SBML_Constants.SBML_META_ID, metaID);
		}
		if (!sboTerm.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, SBML_Constants.SBML,
					SBML_Constants.SBML_SBOTERM, sboTerm);
		}
		
		if (document.isSetAnnotation()) {
			if (document.getAnnotation().isSetRDFannotation()) {
				if (null != document.getAnnotation()) {
					AttributeHelper.setAttribute(g, SBML_Constants.SBML,
							SBML_Constants.SBML_ANNOTATION,
							document.getAnnotation());
				}
			}
			if (document.getAnnotation().isSetNonRDFannotation()) {
				AttributeHelper.setAttribute(g, SBML_Constants.SBML,
						SBML_Constants.SBML_NON_RDF_ANNOTATION, document
								.getAnnotation().getNonRDFannotation());
				
			}
		}
		AttributeHelper.setAttribute(g, SBML_Constants.SBML,
				SBML_Constants.LEVEL, level);
		AttributeHelper.setAttribute(g, SBML_Constants.SBML,
				SBML_Constants.VERSION, version);
	}
}