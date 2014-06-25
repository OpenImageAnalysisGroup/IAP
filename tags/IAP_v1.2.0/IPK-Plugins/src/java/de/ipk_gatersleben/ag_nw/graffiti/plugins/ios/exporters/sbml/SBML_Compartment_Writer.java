/**
 * This class writes a Compartment of a model
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.graffiti.graph.Graph;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLCompartment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLCompartmentHelper;

public class SBML_Compartment_Writer extends SBML_SBase_Writer {
	
	/**
	 * Adds a compartment and its attributes to the model.
	 * 
	 * @param model the compartments will be added to this model
	 * @param g contains the values for the export
	 * @param headline indicates where the information should be read from
	 * @param internHeadline the intern representation of headline
	 */
	public void addCompartment(Model model, Graph g, String internHeadline, SBMLCompartmentHelper compartmentHelperObject) {
		SBMLCompartment compartmentHelper = compartmentHelperObject.addCompartment(g, internHeadline);
		Compartment compartment = model.createCompartment();
		addSBaseAttributes(compartment, g, internHeadline);
		
		if (compartmentHelper.isSetID()) {
			String id = compartmentHelper.getID();
			if (Compartment.isValidId(id, compartment.getLevel(), compartment.getVersion())) {
				compartment.setId(compartmentHelper.getID());
			}
		}
		if (compartmentHelper.isSetName()) {
			compartment.setName(compartmentHelper.getName());
		}
		if (compartmentHelper.isSetSpatialDimensions()) {
			compartment.setSpatialDimensions(compartmentHelper.getSpatialDimensions());
		}
		if (compartmentHelper.isSetSize()) {
			compartment.setSize(compartmentHelper.getSize());
		}
		if (compartmentHelper.isSetUnits()) {
			compartment.setUnits(compartmentHelper.getUnits());
		}
		if (compartmentHelper.isSetConstant()) {
			compartment.setConstant(compartmentHelper.getConstant());
		}
	}
}