/**
 * This class set the Parameter attributes
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLParameterHelper;

public class SBML_Parameter_Writer extends SBML_SBase_Writer {
	
	/**
	 * Adds a parameter and its variables to the model
	 * 
	 * @param g contains the values for the export
	 * @param model the parameters will be added to this model
	 * @param headline indicates where the information should be read from
	 * @param interHeadline intern representation of headline
	 */
	public void addParameter(Graph g, Model model, String interHeadline, SBMLParameterHelper parameterHelperObject) {
		SBMLParameter parameterHelper = parameterHelperObject.addParameter(g, interHeadline);
		Parameter parameter = model.createParameter();
		addSBaseAttributes(parameter, g, interHeadline);
		if (parameterHelper.isSetID()) {
			String id = parameterHelper.getID();
			if (Parameter.isValidId(id, parameter.getLevel(), parameter.getVersion())) {
				parameter.setId(parameterHelper.getID());
			}
			
		}
		if (parameterHelper.isSetName()) {
			parameter.setName(parameterHelper.getName());
		}
		if (parameterHelper.isSetValue()) {
			parameter.setValue(parameterHelper.getValue());
		}
		if (parameterHelper.isSetUnits()) {
			parameter.setUnits(parameterHelper.getUnits());
		}
		if (parameterHelper.isSetConstant()) {
			parameter.setConstant(parameterHelper.getConstant());
		}
	}
}