/**
 * This class reads in Species
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.Point;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.xml.XMLNode;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;

public class SBML_Species_Reader {
	
	/**
	 * Provides useful methods
	 */
	SBMLSpeciesHelper speciesHelper;
	
	/**
	 * reads in species and is called from class SBML_XML_Reader.java. The
	 * method adds species to the graph and to the node tab
	 * 
	 * @param g
	 *        the data structure for reading in the information
	 * @param speciesList
	 *        contains the species for the import
	 * @param pgg
	 *        is needed for drawing the graph
	 */
	public void addSpecies(Graph g, ListOf<Species> speciesList,
			PositionGridGenerator pgg, SBMLSpeciesHelper specieshelper) {
		if (!speciesList.isEmpty()) {
			speciesHelper = specieshelper;
			Iterator<Species> itSpecies = speciesList.iterator();
			Node speciesNode;
			Species speciesJSBML;
			String id;
			String name;
			String compartment;
			Double initialAmount;
			Double initialConcentration;
			String substanceUnits;
			Boolean hasOnlySubstanceUnits;
			Boolean boundaryCondition;
			Boolean constant;
			String conversionFactor;
			String metaID;
			XMLNode notesObj;
			String notes;
			Integer charge;
			String sboTerm;
			
			while (itSpecies.hasNext()) {
				speciesJSBML = itSpecies.next();
				speciesNode = g.addNode();
				id = speciesJSBML.getId();
				name = speciesJSBML.getName();
				speciesHelper.setLabel(speciesNode, name, id, pgg);
				AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
						SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_SPECIES);
				AttributeHelper.setShapeEllipse(speciesNode);
				AttributeHelper.setBorderWidth(speciesNode, 1d);
				compartment = speciesJSBML.getCompartment();
				initialAmount = speciesJSBML.getInitialAmount();
				if (initialAmount.equals(Double.NaN)) {
					ErrorMsg.addErrorMessage("Attribute initialAmount of species "
							+ id
							+ " is not a valid double value or it is coexistent with the initialConcentration value.");
				}
				initialConcentration = speciesJSBML.getInitialConcentration();
				if (initialConcentration.equals(Double.NaN)
						&& !(speciesJSBML.isSetInitialAmount())) {
					ErrorMsg.addErrorMessage("Attribute initialConcentration of species "
							+ id
							+ " is not a valid double value or it is coexistent with the initialAmount value.");
				}
				substanceUnits = speciesJSBML.getSubstanceUnits();
				hasOnlySubstanceUnits = speciesJSBML.getHasOnlySubstanceUnits();
				boundaryCondition = speciesJSBML.getBoundaryCondition();
				constant = speciesJSBML.getConstant();
				conversionFactor = speciesJSBML.getConversionFactor();
				metaID = speciesJSBML.getMetaId();
				sboTerm = speciesJSBML.getSBOTermID();
				notesObj = speciesJSBML.getNotes();
				notes = speciesJSBML.getNotesString();
				charge = speciesJSBML.getCharge();
				
				if (speciesJSBML.isSetId()
						&& Species.isValidId(id, speciesJSBML.getLevel(),
								speciesJSBML.getVersion())) {
					speciesHelper.setID(speciesNode, id);
					speciesHelper.setHiddenLabel(speciesNode, id);
				}
				if (speciesJSBML.isSetCompartment()) {
					speciesHelper.setCompartment(speciesNode, compartment);
				}
				if (speciesJSBML.isSetInitialAmount()) {
					speciesHelper.setInitialAmount(speciesNode, initialAmount);
				}
				if (speciesJSBML.isSetInitialConcentration()) {
					speciesHelper.setInitialConcentration(speciesNode,
							initialConcentration);
				}
				if (speciesJSBML.isSetSubstanceUnits()) {
					speciesHelper
							.setSubstanceUnits(speciesNode, substanceUnits);
				}
				if (speciesJSBML.isSetHasOnlySubstanceUnits()) {
					speciesHelper.setHasOnlySubstanceUnits(speciesNode,
							hasOnlySubstanceUnits);
				}
//				if (speciesJSBML.isSetBoundaryCondition()) {
					speciesHelper.setBoundaryConsition(speciesNode,
							boundaryCondition);
//				}
				if (speciesJSBML.isSetConstant()) {
					speciesHelper.setConstant(speciesNode, constant);
				}
				if (speciesJSBML.isSetConversionFactor()) {
					speciesHelper.setConversionFactor(speciesNode,
							conversionFactor);
				}
				if (speciesJSBML.isSetMetaId()) {
					speciesHelper.setMetaID(speciesNode, metaID);
				}
				if (speciesJSBML.isSetSBOTerm()) {
					speciesHelper.setSBOTerm(speciesNode, sboTerm);
				}
				if (speciesJSBML.isSetNotes()) {
					speciesHelper.setNotes(speciesNode, notes, notesObj);
				}
				if (speciesJSBML.isSetAnnotation()) {
					if (speciesJSBML.getAnnotation().isSetRDFannotation()) {
						speciesHelper.setAnnotation(speciesNode,
								speciesJSBML.getAnnotation());
					}
					if (speciesJSBML.getAnnotation().isSetNonRDFannotation()) {
						speciesHelper.setNonRDFAnnotation(speciesNode,
								speciesJSBML.getAnnotation()
										.getNonRDFannotation());
					}
				}
				if (speciesJSBML.isSetCharge()) {
					speciesHelper.setCharge(speciesNode, charge);
				}
				if (speciesJSBML.isSetCompartment()) {
					speciesHelper.setCompartmentName(speciesNode);
				}
				if (SBML_Constants.isLayoutActive) {
					processLayoutInformation(g, speciesJSBML, speciesNode);
				}
			}
		}
	}
	
	private void processLayoutInformation(Graph g, Species speciesJSBML, Node speciesNode) {
		String id = speciesJSBML.getId();
		ExtendedLayoutModel layoutModel = (ExtendedLayoutModel) speciesJSBML.getModel().getExtension(SBMLHelper.SBML_LAYOUT_EXTENSION_NAMESPACE);
		if (layoutModel != null) {
			Layout layout = layoutModel.getListOfLayouts().iterator().next();
			ListOf<SpeciesGlyph> currentSpeciesGlyphs = new ListOf<SpeciesGlyph>();
			currentSpeciesGlyphs.setLevel(layout.getLevel());
			currentSpeciesGlyphs.setVersion(layout.getVersion());
			Iterator<SpeciesGlyph> speciesGlyphListIt = layout.getListOfSpeciesGlyphs().iterator();
			while (speciesGlyphListIt.hasNext()) {
				SpeciesGlyph nextSpeciesGlyph = speciesGlyphListIt.next();
				if (nextSpeciesGlyph.getSpecies().equals(id)) {
					nextSpeciesGlyph.setVersion(1);
					currentSpeciesGlyphs.add(nextSpeciesGlyph);
					
				}
			}
			for (int i = 0; i < currentSpeciesGlyphs.size(); i++) {
				SpeciesGlyph speciesGlyph = currentSpeciesGlyphs.get(i);
				
				if (i == 0) {
					speciesHelper.addCloneToList(id, speciesNode);
				}
				if (i >= 1) {
					Node speciesNodeClone = g.addNodeCopy(speciesNode);
					speciesHelper.addCloneToList(id, speciesNodeClone);
					speciesNode = speciesNodeClone;
				}
				
				AttributeHelper.setSize(speciesNode, 27, 27);
				// SpeciesGlyph speciesGlyph = speciesGlyphs.get(i);
				BoundingBox boundingBox = speciesGlyph.getBoundingBox();
				if (boundingBox != null) {
					Dimensions dimensions = boundingBox.getDimensions();
					if (dimensions != null) {
						double width = dimensions.getWidth();
						double height = dimensions.getHeight();
						AttributeHelper.setSize(speciesNode, width, height);
						
						if (speciesGlyph.isSetLayoutId()) {
							String layoutId = speciesGlyph.getLayoutId();
							AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, layoutId);
						}
						else {
							AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, SBML_Constants.EMPTY);
						}
					} else
					{
						AttributeHelper.setSize(speciesNode, 27, 27);
					}
					Point position = boundingBox.getPosition();
					if (position != null) {
						double x = position.getX();
						double y = position.getY();
						AttributeHelper.setPosition(speciesNode, x, y);
						String layoutId = speciesGlyph.getLayoutId();
						AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, layoutId);
					}
				}
			}
		}
		
	}
	
	private void printAllSBMLAttributes(Node species) {
		CollectionAttribute collection = species.getAttributes();
		Map<String, Attribute> map = collection.getCollection();
		for (Entry<String, Attribute> entry : map.entrySet()) {
			Attribute attr = entry.getValue();
			HashMapAttribute attrHashMap = (HashMapAttribute) attr;
			TreeMap<String, Attribute> collectionAttr = (TreeMap) attr.getValue();
			for (Entry<String, Attribute> entryAttr : collectionAttr.entrySet()) {
				
				String[] attrSeparates = entryAttr.getValue().getPath().split("\\" + Attribute.SEPARATOR);
				for (String name : attrSeparates) {
				}
				
			}
		}
	}
}