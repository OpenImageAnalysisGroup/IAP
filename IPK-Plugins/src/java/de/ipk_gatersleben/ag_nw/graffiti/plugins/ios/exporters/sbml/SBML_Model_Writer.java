/**
 * This class sets the attributes of Model
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.sbml.jsbml.Model;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Model_Writer extends SBML_SBase_Writer {

	/**
	 * Add attributes to the model. The ID has already been set when it exists
	 * 
	 * @param model
	 *            has the attribute which will be set
	 * @param g
	 *            contains the values for the export
	 */
	public void addModel(Model model, Graph g) {
		addSBaseAttributes(model, g);
		// String id = (String)getAttribute(g, SBML_Constants.SBML_MODEL,
		// "SBML Model"+ATT+"ID");
		// if(id.length()>0)
		// model.setId(id);

		/*
		 * if(AttributeHelper.hasAttribute(annotationNode, "SBML",
		 * "modelannotation")){ Annotation anno =
		 * (Annotation)AttributeHelper.getAttributeValue(annotationNode, "SBML",
		 * "modelannotation", SBML_Constants.EMPTY, null);
		 * model.setAnnotation(anno); }
		 */
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.MODEL_NAME)) {
			String name = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_NAME);
			if (name.length() > 0)
				model.setName(name);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.MODEL_ID)) {
			String id = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.MODEL_ID);
			if (id.length() > 0)
				model.setId(id);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.SUBSTANCE_UNITS)) {
			String substranceUnits = (String) getAttribute(g,
					SBML_Constants.SBML, SBML_Constants.SUBSTANCE_UNITS);
			if (substranceUnits.length() > 0)
				model.setSubstanceUnits(substranceUnits);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.TIME_UNITS)) {
			String timeUnits = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.TIME_UNITS);
			if (timeUnits.length() > 0)
				model.setTimeUnits(timeUnits);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.VOLUME_UNITS)) {
			String volumeUnits = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.VOLUME_UNITS);
			if (volumeUnits.length() > 0)
				model.setVolumeUnits(volumeUnits);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.AREA_UNITS)) {
			String areaUnits = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.AREA_UNITS);
			if (areaUnits.length() > 0)
				model.setAreaUnits(areaUnits);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.LENGTH_UNITS)) {
			String lengthUnits = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.LENGTH_UNITS);
			if (lengthUnits.length() > 0)
				model.setLengthUnits(lengthUnits);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.EXTENT_UNITS)) {
			String extentUnits = (String) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.EXTENT_UNITS);
			if (extentUnits.length() > 0)
				model.setExtentUnits(extentUnits);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.CONVERSION_FACTOR)) {
			String conversionFactor = (String) getAttribute(g,
					SBML_Constants.SBML, SBML_Constants.CONVERSION_FACTOR);
			if (conversionFactor.length() > 0)
				model.setConversionFactor(conversionFactor);
		}
	}
}