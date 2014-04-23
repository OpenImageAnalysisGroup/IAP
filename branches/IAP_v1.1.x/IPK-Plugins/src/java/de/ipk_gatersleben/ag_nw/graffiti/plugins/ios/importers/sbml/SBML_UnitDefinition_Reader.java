/**
 * This class reads in UnitDefinitions
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLUnit;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLUnitDefinition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLUnitDefinitionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_UnitDefinition_Reader {
	
	/**
	 * Method reads in unit definitions and is called from class
	 * SBML_XML_Reader.java
	 * 
	 * @param unitDefinitionList
	 *        contains the unit definitions for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	public void addUnitDefinitions(ListOf<UnitDefinition> unitDefinitionList,
			Graph g) {
		Iterator<UnitDefinition> itUnitDefinition = unitDefinitionList
				.iterator();
		int unitCount = 1;
		SBMLUnitDefinitionHelper unitDefinitionHelperObject = new SBMLUnitDefinitionHelper();
		while (itUnitDefinition.hasNext()) {
			int subUnitCount = 1;
			UnitDefinition unitDefinition = itUnitDefinition.next();
			String internHeadline = new StringBuffer(
					SBML_Constants.SBML_UNIT_DEFINITION).append(unitCount)
					.toString();
			String presentedHeadline = new StringBuffer("SBML Unit Definition ")
					.append(unitCount).toString();
			SBMLUnitDefinition unitDefinitionHelper = unitDefinitionHelperObject
					.addUnitDefinition(g, internHeadline, presentedHeadline);
			
			String unitDefinitionID = unitDefinition.getId();
			String unitDefinitionName = unitDefinition.getName();
			String unitDefinitionSBOTerm = "";
			if (unitDefinition.isSetSBOTerm()) {
				unitDefinitionSBOTerm = unitDefinition.getSBOTermID();
			}
			String unitDefinitionMetaID = "";
			if (unitDefinition.isSetMetaId()) {
				unitDefinitionMetaID = unitDefinition.getMetaId();
			}
			
			if (unitDefinition.isSetNotes()) {
				unitDefinitionHelper.setNotes(unitDefinition.getNotesString(),
						unitDefinition.getNotes());
			}
			if (unitDefinition.isSetId()
					&& UnitDefinition.isValidId(unitDefinitionID,
							unitDefinition.getLevel(),
							unitDefinition.getVersion())) {
				;
				unitDefinitionHelper.setID(unitDefinitionID);
			}
			if (unitDefinition.isSetName()) {
				unitDefinitionHelper.setName(unitDefinitionName);
			}
			if (unitDefinition.isSetMetaId()) {
				unitDefinitionHelper.setMetaID(unitDefinitionMetaID);
			}
			if (unitDefinition.isSetSBOTerm()) {
				unitDefinitionHelper.setSBOTerm(unitDefinitionSBOTerm);
			}
			if (unitDefinition.isSetAnnotation()) {
				if (unitDefinition.getAnnotation().isSetRDFannotation()) {
					unitDefinitionHelper.setAnnotation(unitDefinition
							.getAnnotation());
				}
				if (unitDefinition.getAnnotation().isSetNonRDFannotation()) {
					unitDefinitionHelper.setNonRDFAnnotation(unitDefinition
							.getAnnotation().getNonRDFannotation());
				}
			}
			// The sub units are collected in this list
			List<String> ListOfSubUnits = new ArrayList<String>();
			if (unitDefinition.isSetListOfUnits()) {
				List<Unit> unitList = unitDefinition.getListOfUnits();
				Iterator<Unit> itUnit = unitList.iterator();
				SBMLUnit unitHelper = null;
				while (itUnit.hasNext()) {
					unitHelper = unitDefinitionHelper.addUnit(subUnitCount);
					Unit unit = itUnit.next();
					int scale = unit.getScale();
					Double exponent = unit.getExponent();
					if (exponent.equals(Double.NaN)) {
						ErrorMsg.addErrorMessage("Attribute exponent of unit definition "
								+ unitCount
								+ " sub unit "
								+ subUnitCount
								+ " with the id "
								+ unitDefinitionID
								+ " is not a valid double value.");
					}
					Double multiplier = unit.getMultiplier();
					if (multiplier.equals(Double.NaN)) {
						ErrorMsg.addErrorMessage("Attribute multiplier of unit definition "
								+ unitCount
								+ " sub unit "
								+ subUnitCount
								+ " with the id "
								+ unitDefinitionID
								+ " is not a valid double value.");
					}
					String kind = unit.getKind().getName();
					String composedSubUnit = "(" + multiplier + " * 10^"
							+ scale + " * " + kind + ")^" + exponent;
					
					unitHelper.setComposedSubUnit(composedSubUnit);
					if (unit.isSetMetaId()) {
						unitHelper.setMetaID(unit.getMetaId());
					}
					if (unit.isSetSBOTerm()) {
						unitHelper.setSBOTerm(unit.getSBOTermID());
					}
					if (unit.isSetNotes()) {
						unitHelper.setNotes(unit.getNotesString(),
								unit.getNotes());
					}
					if (unit.isSetAnnotation()) {
						if (unit.getAnnotation().isSetRDFannotation()) {
							unitHelper.setAnnotation(unit.getAnnotation());
						}
						if (unit.getAnnotation().isSetNonRDFannotation()) {
							unitHelper.setNonRDFAnnotation(unit.getAnnotation()
									.getNonRDFannotation());
						}
					}
					ListOfSubUnits.add(composedSubUnit);
					
					subUnitCount++;
				}
				String composedUnit = "";
				int size = ListOfSubUnits.size();
				int count = 0;
				Iterator<String> itSubUnits = ListOfSubUnits.iterator();
				while (itSubUnits.hasNext()) {
					String subUnit = itSubUnits.next();
					composedUnit = composedUnit + subUnit;
					count++;
					if (count < size) {
						composedUnit = composedUnit + " * ";
					}
				}
				unitHelper.setComposedUnit(composedUnit);
			}
			
			unitCount++;
		}
		
	}
}