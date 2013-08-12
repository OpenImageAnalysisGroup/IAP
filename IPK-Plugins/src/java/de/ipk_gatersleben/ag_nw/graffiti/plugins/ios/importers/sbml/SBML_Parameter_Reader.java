/**
 * This class reads in Parameters
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLParameter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLParameterHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Parameter_Reader {
	
	/**
	 * Method reads in parameter and is called from class SBML_XML_Reader.java
	 * 
	 * @param parameterList
	 *           contains the parameter for the import
	 * @param g
	 *           the data structure for reading in the information
	 */
	public void addParameter(ListOf<Parameter> parameterList, Graph g) {
		Iterator<Parameter> itParameter = parameterList.iterator();
		int parameterCount = 1;
		SBMLParameterHelper parameterHelperObject = new SBMLParameterHelper();
		while (itParameter.hasNext()) {
			Parameter parameter = itParameter.next();
			
			String internHeadline = new StringBuffer(
					SBML_Constants.SBML_PARAMETER).append(parameterCount)
					.toString();
			String presentedHeadline = new StringBuffer("SBML Parameter ")
					.append(parameterCount).toString();
			
			SBMLParameter parameterHelper = parameterHelperObject.addParameter(
					g, internHeadline, presentedHeadline);
			
			String parameterID = parameter.getId();
			String parameterName = parameter.getName();
			Double parameterValue = parameter.getValue();
			
			// in BIOMD0000000012.xml from BioModels.org this error message would be shown
			// nevertheless the model is valid
			// so this message is not necessary
			// if (parameterValue.equals(Double.NaN)) {
			// ErrorMsg.addErrorMessage("Attribute value of parameter "
			// + parameterCount + " with the id " + parameterID
			// + " is not a valid double value.");
			// }
			
			String parameterUnits = parameter.getUnits();
			Boolean parameterConstant = parameter.isConstant();
			String metaID = parameter.getMetaId();
			String sboTerm = parameter.getSBOTermID();
			if (parameter.isSetId()
					&& Parameter.isValidId(parameterID, parameter.getLevel(),
							parameter.getVersion())) {
				parameterHelper.setID(parameterID);
			}
			if (!Parameter.isValidId(parameterID, parameter.getLevel(),
					parameter.getVersion())) {
				ErrorMsg.addErrorMessage("ID of parameter " + parameterCount
						+ " is not valid.");
			}
			if (parameter.isSetName()) {
				parameterHelper.setName(parameterName);
			}
			if (parameter.isSetValue()) {
				parameterHelper.setValue(parameterValue);
			}
			if (parameter.isSetUnits()) {
				parameterHelper.setUnits(parameterUnits);
			}
			if (parameter.isSetConstant()) {
				parameterHelper.setConstant(parameterConstant);
			}
			if (parameter.isSetMetaId()) {
				parameterHelper.setMetaID(metaID);
			}
			if (parameter.isSetSBOTerm()) {
				parameterHelper.setSBOTerm(sboTerm);
			}
			if (parameter.isSetNotes()) {
				parameterHelper.setNotes(parameter.getNotesString(),
						parameter.getNotes());
			}
			if (parameter.isSetAnnotation()) {
				if (parameter.getAnnotation().isSetRDFannotation()) {
					parameterHelper.setAnnotation(parameter.getAnnotation());
				}
				if (parameter.getAnnotation().isSetNonRDFannotation()) {
					parameterHelper.setNonRDFAnnotation(parameter
							.getAnnotation().getNonRDFannotation());
				}
			}
			parameterCount++;
		}
	}
}