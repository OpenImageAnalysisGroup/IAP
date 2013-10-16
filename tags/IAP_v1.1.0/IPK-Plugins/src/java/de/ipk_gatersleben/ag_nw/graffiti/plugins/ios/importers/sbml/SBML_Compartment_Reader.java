/**
 * This class reads in Compartments
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;

import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLCompartment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLCompartmentHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Compartment_Reader {
	
	/**
	 * Method reads in compartments and is called from class
	 * SBML_XML_Reader.java
	 * 
	 * @param compartmentList
	 *        contains the compartments for the import
	 * @param g
	 *        the data structure for reading in the information
	 */
	public void addCompartment(ListOf<Compartment> compartmentList, Graph g) {
		Iterator<Compartment> itComp = compartmentList.iterator();
		int compartmentCount = 1;
		SBMLCompartmentHelper compartmentHelperObject = new SBMLCompartmentHelper();
		Compartment compartment;
		String name;
		String id;
		Double spatialDimensions;
		Double size;
		String units;
		Boolean constant;
		String metaID;
		String sboTerm;
		String outside;
		XMLNode notesObj;
		String notes;
		String internHeadline;
		String presentedHeadline;
		while (itComp.hasNext()) {
			compartment = itComp.next();
			name = compartment.getName();
			id = compartment.getId();
			spatialDimensions = compartment.getSpatialDimensions();
			size = compartment.getSize();
			units = compartment.getUnits();
			constant = compartment.getConstant();
			metaID = compartment.getMetaId();
			sboTerm = compartment.getSBOTermID();
			outside = compartment.getOutside();
			notesObj = compartment.getNotes();
			notes = compartment.getNotesString();
			internHeadline = new StringBuffer(SBML_Constants.SBML_COMPARTMENT)
					.append(id).toString();
			presentedHeadline = SBML_Constants.EMPTY;
			if (compartment.isSetName()) {
				presentedHeadline = new StringBuffer(
						SBML_Constants.COMARTMENT_HEADLINE).append(name)
						.toString();
			} else if (compartment.isSetId()) {
				presentedHeadline = new StringBuffer(
						SBML_Constants.COMARTMENT_HEADLINE).append(id)
						.toString();
			} else {
				presentedHeadline = new StringBuffer(
						SBML_Constants.COMARTMENT_HEADLINE).append(
						compartmentCount).toString();
			}
			if (size.equals(Double.NaN) && compartment.isSetSize()) {
				ErrorMsg.addErrorMessage("Attribute size of "
						+ presentedHeadline + " is not a valid double value.");
			}
			SBMLCompartment compartmentHelper = compartmentHelperObject
					.addCompartment(g, internHeadline, presentedHeadline);
			
			if (compartment.isSetId()
					&& Compartment.isValidId(id, compartment.getLevel(),
							compartment.getVersion())) {
				compartmentHelper.setID(id);
			}
			if (compartment.isSetName() && (name != SBML_Constants.EMPTY)) {
				compartmentHelper.setName(name);
			}
			if (compartment.isSetSpatialDimensions()) {
				compartmentHelper.setSpatialDimensions(spatialDimensions);
			}
			if (compartment.isSetSize()) {
				compartmentHelper.setSize(size);
			}
			if (compartment.isSetUnits()) {
				compartmentHelper.setUnits(units);
			}
			if (compartment.isSetConstant()) {
				compartmentHelper.setConstant(constant);
			}
			if (compartment.isSetMetaId()) {
				compartmentHelper.setMetaID(metaID);
			}
			if (compartment.isSetSBOTerm()) {
				compartmentHelper.setSBOTerm(sboTerm);
			}
			if (compartment.isSetOutside()) {
				compartmentHelper.setOutside(outside);
			}
			if (compartment.isSetNotes()) {
				compartmentHelper.setNotes(notes, notesObj);
			}
			if (compartment.isSetAnnotation()) {
				if (compartment.getAnnotation().isSetRDFannotation()) {
					compartmentHelper
							.setAnnotation(compartment.getAnnotation());
				}
				if (compartment.getAnnotation().isSetNonRDFannotation()) {
					compartmentHelper.setNonRDFAnnotation(compartment
							.getAnnotation().getNonRDFannotation());
				}
			}
			
			compartmentCount++;
		}
	}
}