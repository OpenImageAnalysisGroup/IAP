/**
 * This class writes function definitions
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.Model;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLFunctionDefinition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLFunctionDefinitionHelper;

public class SBML_FunctionDefinition_Writer extends SBML_SBase_Writer {
	
	/**
	 * Adds a function definition and its variables to the model
	 * 
	 * @param model the function definition will be added to this model
	 * @param g contains the values for the export
	 * @param headline indicates where the information should be read from
	 */
	public void addFunctionDefinition(Model model, Graph g, String internHeadline, SBMLFunctionDefinitionHelper
			functionDefinitionHelperObject) {
		SBMLFunctionDefinition functionDefinitionHelper = functionDefinitionHelperObject.addFunctionDefinition(g, internHeadline);
		FunctionDefinition functionDefinition = model.createFunctionDefinition();
		addSBaseAttributes(functionDefinition, g, internHeadline);
		if (functionDefinitionHelper.isSetID()) {
			functionDefinition.setId(functionDefinitionHelper.getID());
		}
		if (functionDefinitionHelper.isSetName()) {
			functionDefinition.setName(functionDefinitionHelper.getName());
		}
		if (functionDefinitionHelper.isSetFunction()) {
			functionDefinition.setMath(functionDefinitionHelper.getFunction());
		}
		/*
		 * if(functionDefinitionHelper.isSetFunction()){
		 * String formel = (String)getAttribute(g, internHeadline, SBML_Constants.FUNCTION_DEFINITION_FUNCTION);
		 * try{
		 * ASTNode h = ASTNode.parseFormula(formel);//"lambda("+formel+")");
		 * //ASTNode math = (ASTNode)getAttribute(g, niceID, headline+ATT+"ASTNode Math");
		 * //functionDefinition.setMath(math);
		 * functionDefinition.setMath(h);
		 * }catch(ParseException e){e.printStackTrace();}
		 * }
		 */
	}
}