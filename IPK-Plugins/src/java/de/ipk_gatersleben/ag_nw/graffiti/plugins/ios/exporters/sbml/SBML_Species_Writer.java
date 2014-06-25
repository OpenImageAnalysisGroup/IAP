/**
 * This class writes the Species object
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Species_Writer extends SBML_SBase_Writer {
	
	/**
	 * Provides helpful methods
	 */
	SBMLSpeciesHelper speciesHelper;
	
	/**
	 * Adds a species and its variables to the model
	 * 
	 * @param g
	 *        contains the values for the export
	 * @param model
	 *        the species will be added to this model
	 */
	public void addSpecies(Model model, Graph g) {
		speciesHelper = new SBMLSpeciesHelper(g);
		List<Node> nodeList = speciesHelper.getSpeciesNodes();
		Iterator<Node> nodeIt = nodeList.iterator();
		Set<String> speciesIds = new HashSet<String>();
		while (nodeIt.hasNext()) {
			Node speciesNode = nodeIt.next();
			Species species = model.createSpecies();
			if (speciesHelper.isSetID(speciesNode)) {
				String id = speciesHelper.getID(speciesNode);
				if (!speciesIds.contains(id)) {
					addSBaseAttributes(species, speciesNode);
					speciesIds.add(id);
					if (Species.isValidId(id, species.getLevel(),
							species.getVersion()))
					{
						species.setId(id);
					}
					
					if (speciesHelper.isSetName(speciesNode)) {
						species.setName(speciesHelper.getName(speciesNode));
					}
					if (speciesHelper.isSetCompartment(speciesNode)) {
						species.setCompartment(speciesHelper
								.getCompartment(speciesNode));
					}
					if (speciesHelper.isSetInitialAmount(speciesNode)) {
						species.setInitialAmount(speciesHelper
								.getInitialAmount(speciesNode));
					}
					if (speciesHelper.isSetInitialConcentration(speciesNode)) {
						species.setInitialConcentration(speciesHelper
								.getInitialConcentration(speciesNode));
					}
					if (speciesHelper.isSetSubstanceUnits(speciesNode)) {
						species.setSubstanceUnits(speciesHelper
								.getSubstanceUnits(speciesNode));
					}
					if (speciesHelper.isSetHasOnlySubstanceUnits(speciesNode)) {
						species.setHasOnlySubstanceUnits(speciesHelper
								.getHasOnlySubstanceUnits(speciesNode));
					}
					if (speciesHelper.isSetBoundaryCondition(speciesNode)) {
						species.setBoundaryCondition(speciesHelper
								.getBoundaryCondition(speciesNode));
					}
					if (speciesHelper.isSetConstant(speciesNode)) {
						species.setConstant(speciesHelper.getConstant(speciesNode));
					}
					if (speciesHelper.isSetConversionFactor(speciesNode)) {
						species.setConversionFactor(speciesHelper
								.getConversionFactor(speciesNode));
					}
				}
			}
			/*
			 * if(AttributeHelper.hasAttribute(annotationNode, "SBML", "species"
			 * +species.getId()+"annotation")){ Annotation anno =
			 * (Annotation)AttributeHelper.getAttributeValue(annotationNode,
			 * "SBML", "species" +species.getId()+"annotation",
			 * SBML_Constants.EMPTY, null); species.setAnnotation(anno); }
			 */
		}
	}
	
	public void addSpeciesGlyph(Model model, Graph g) {
		speciesHelper = new SBMLSpeciesHelper(g);
		List<Node> nodeList = speciesHelper.getSpeciesNodes();
		Iterator<Node> nodeIt = nodeList.iterator();
		ExtendedLayoutModel layoutModel = (ExtendedLayoutModel) model.getExtension(SBMLHelper.SBML_LAYOUT_EXTENSION_NAMESPACE);
		if(layoutModel == null)
			return;
		while (nodeIt.hasNext()) {
			Node node = nodeIt.next();
			String layoutID = (String) AttributeHelper.getAttributeValue(node, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, null, null);
			for (Layout layout : layoutModel.getListOfLayouts()) {
				if (layoutID != null && layoutID.equals(layout.getId())) {
					System.out.println("layout id in addSpeciesGlyph: " + layout.getId());
					if (speciesHelper.isSetID(node)) {
						String speciesID = speciesHelper.getID(node);
						double x = AttributeHelper.getPositionX(node);
						double y = AttributeHelper.getPositionY(node);
						double width = AttributeHelper.getWidth(node);
						double height = AttributeHelper.getHeight(node);
						SpeciesGlyph speciesGlyph = layout.createSpeciesGlyph(speciesID);
						speciesGlyph.createBoundingBox(width, height, 1, x, y, 0);
					} else {
						ErrorMsg
								.addErrorMessage("Species id must be set! The Node will not be exported to the SBML-Layout-Model.");
					}
				}
			}
		}
	}
}
