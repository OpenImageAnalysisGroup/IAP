/**
 * This class reads in FunctionDefinitions
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.ListOf;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLFunctionDefinition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLFunctionDefinitionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_FunctionDefinition_Reader {
	
	/**
	 * Method reads in function definitions and is called from class SBML_XML_Reader.java
	 * 
	 * @param functionDefinitionList contains the function definitions for the import
	 * @param g the data structure for reading in the information
	 */
	public void addFunktionDefinition(ListOf<FunctionDefinition> functionDefinitionList, Graph g) {
		Iterator<FunctionDefinition> itFunctionDefinition = functionDefinitionList.iterator();
		int functionCount = 1;
		SBMLFunctionDefinitionHelper functionDefinitionHelperObject = new SBMLFunctionDefinitionHelper();
		while (itFunctionDefinition.hasNext()) {
			try {
				FunctionDefinition functionDefinition = itFunctionDefinition.next();
				String internHeadline = new StringBuffer(SBML_Constants.SBML_FUNCTION_DEFINITION).append(functionCount).toString();
				String presentedHeadline = new StringBuffer("SBML Function Definition ").append(functionCount).toString();
				SBMLFunctionDefinition functionDefinitionHelper = functionDefinitionHelperObject.
						addFunctionDefinition(g, internHeadline, presentedHeadline);
				String functionID = functionDefinition.getId();
				String functionName = functionDefinition.getName();
				String sboTerm = functionDefinition.getSBOTermID();
				String metaID = functionDefinition.getMetaId();
				
				if (functionDefinition.isSetId()) {
					functionDefinitionHelper.setID(functionID);
				}
				if (functionDefinition.isSetMath()) {
					ASTNode math = functionDefinition.getMath();
					functionDefinitionHelper.setFunction(math.toString());
				}
				if (functionDefinition.isSetName()) {
					functionDefinitionHelper.setName(functionName);
				}
				if (functionDefinition.isSetSBOTerm()) {
					functionDefinitionHelper.setSBOTerm(sboTerm);
				}
				if (functionDefinition.isSetMetaId()) {
					functionDefinitionHelper.setMetaID(metaID);
				}
				if (functionDefinition.isSetNotes()) {
					functionDefinitionHelper.setNotes(functionDefinition.getNotesString(), functionDefinition.getNotes());
				}
				if (functionDefinition.isSetAnnotation()) {
					if (functionDefinition.getAnnotation().isSetRDFannotation()) {
						functionDefinitionHelper.setAnnotation(functionDefinition.getAnnotation());
					}
					if (functionDefinition.getAnnotation().isSetNonRDFannotation()) {
						if (functionDefinition.getAnnotation().isSetNonRDFannotation()) {
							functionDefinitionHelper.setNonRDFAnnotation(functionDefinition.getAnnotation().getNonRDFannotation());
						}
					}
				}
				
				functionCount++;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}