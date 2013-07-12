/**
 * This class writes the SBase attributes of UnitDefinition
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.util.regex.Pattern;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_UnitDefinition_Writer extends SBML_SBase_Writer{
	
	/**
	 * Adds an unit definition and its variables to the model
	 * @param model the unit definition will be added to this model
	 * @param g contains the values for the export
	 * @param headline indicates where the information should be read from
	 * @param intern representation of headline
	 */
	public void addUnitDefinition(Model model, Graph g, String niceID) {
		UnitDefinition unitDefinition = model.createUnitDefinition();
		addSBaseAttributes(unitDefinition, g, niceID);
		if(AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID).append(SBML_Constants.UNIT_DEFINITION_ID).toString())){
			String id = (String) getAttribute(g, niceID,new StringBuffer(niceID).append(SBML_Constants.UNIT_DEFINITION_ID).toString());
			if (!SBML_Constants.EMPTY.equals(id) && UnitDefinition.isValidId(id, unitDefinition.getLevel(), unitDefinition.getVersion())) {
				unitDefinition.setId(id);
			}
		}
		if(AttributeHelper.hasAttribute(g, niceID,new StringBuffer(niceID).append(SBML_Constants.UNIT_DEFINITION_NAME).toString())){
			String name = (String) getAttribute(g, niceID,new StringBuffer(niceID).append(SBML_Constants.UNIT_DEFINITION_NAME).toString());
			if (!SBML_Constants.EMPTY.equals(name)) {
				unitDefinition.setName(name);
			}
		}
		
		
		String subUnit = "";
		int unitCount = 1;
		Unit unit = null;
		while (AttributeHelper.hasAttribute(g, niceID, new StringBuffer(niceID).append(SBML_Constants.SUB_UNIT).
				append(unitCount).append(SBML_Constants.UNDERLINE).toString())){
			subUnit = (String) getAttribute(g, niceID,new StringBuffer(niceID).append(SBML_Constants.SUB_UNIT).
					append(unitCount).append(SBML_Constants.UNDERLINE).toString());
			unit = unitDefinition.createUnit();
			addSBaseAttributes(unit, g, niceID);	
			String[] subUnitArray = subUnit.split(Pattern.quote(")"));
			String exponent = subUnitArray[1].replace("^", "");
			String[] subUnitArray2 = subUnitArray[0].split("\\*");
			String multiplier = subUnitArray2[0].replace("(", "");
			String[] subUnitArray3 = subUnitArray2[1].split("\\^");
			String scale = subUnitArray3[1];
			String kind = subUnitArray2[2];
			unit.setExponent(Double.parseDouble(exponent.trim()));
			unit.setMultiplier(Double.parseDouble(multiplier.trim()));
			unit.setScale(Integer.parseInt(scale.trim()));
			unit.setKind(Kind.valueOf(kind.trim().toUpperCase()));
			++unitCount;
			subUnit = "";
		}
	}
}