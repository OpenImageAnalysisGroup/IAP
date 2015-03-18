/**
 * This class sets the attributes of SBML
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.ext.layout.ExtendedLayoutModel;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLCompartmentHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLConstraintHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLFunctionDefinitionHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLInitialAssignmentHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLParameterHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBMLSpeciesHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_Constants;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml.SBML_XML_ReaderWriterPlugin;
@SuppressWarnings("unused")
public class SBML_SBML_Writer extends SBML_SBase_Writer {
	private Model _model;
	
	/**
	 * Processes the reading in an model
	 * 
	 * @param stream
	 *        the data will be written into this stream
	 * @param g
	 *        contains the data
	 */
	public void addSBML(OutputStream stream, Graph g) {
		SBML_Constants.init();
		PrintStream ps = null;
		try {
			ps = new PrintStream(stream, false, "iso-8859-1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		SBMLWriter writer = new SBMLWriter();
		int level = 0;
		int version = 1;
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.LEVEL)) {
			level = (Integer) AttributeHelper.getAttributeValue(g,
					SBML_Constants.SBML, SBML_Constants.LEVEL, null, null);
		}
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.VERSION)) {
			version = (Integer) getAttribute(g, SBML_Constants.SBML,
					SBML_Constants.VERSION);
		}
		// L3V1 is the current spec
		if (level < 3) {
			level = 3;
			version = 1;
		}
		SBMLDocument doc = new SBMLDocument(level, version);
		addSBaseAttributes(doc, g);
		
		_model = null;
		
		if (AttributeHelper.hasAttribute(g, SBML_Constants.SBML,
				SBML_Constants.MODEL_ID)) {
			_model = doc.createModel((String) getAttribute(g,
					SBML_Constants.SBML, SBML_Constants.MODEL_ID));
		} else {
			_model = doc.createModel();
		}
		
		SBML_Model_Writer writeModel = new SBML_Model_Writer();
		writeModel.addModel(_model, g);
		
		ArrayList<String> functionDefinitions = headlineHelper(g,
				SBML_Constants.SBML_FUNCTION_DEFINITION);
		if (functionDefinitions.size() > 0) {
			Iterator<String> itFunctionDefinitions = functionDefinitions
					.iterator();
			int i = 1;
			SBMLFunctionDefinitionHelper functionDefinitionHelper = new SBMLFunctionDefinitionHelper();
			while (itFunctionDefinitions.hasNext()) {
				String functionDefinitionHeadline = (String) itFunctionDefinitions
						.next();
				SBML_FunctionDefinition_Writer writeFunctionDefinition = new SBML_FunctionDefinition_Writer();
				writeFunctionDefinition.addFunctionDefinition(_model, g,
						functionDefinitionHeadline, functionDefinitionHelper);
				i++;
			}
		}
		
		ArrayList<String> unitDefinitions = headlineHelper(g,
				SBML_Constants.SBML_UNIT_DEFINITION);
		if (unitDefinitions.size() > 0) {
			Iterator<String> itUnitDefinition = unitDefinitions.iterator();
			int i = 1;
			while (itUnitDefinition.hasNext()) {
				String unitDefinition = (String) itUnitDefinition.next();
				// String presentedHeadline = "SBML Unit Definition "+i;
				// SBML_Constants.put(ATT+unitDefinition, presentedHeadline);
				SBML_UnitDefinition_Writer writeUnitDefinition = new SBML_UnitDefinition_Writer();
				writeUnitDefinition.addUnitDefinition(_model, g, unitDefinition);
				i++;
			}
		}
		
		ArrayList<String> compartments = headlineHelper(g,
				SBML_Constants.SBML_COMPARTMENT);
		if (compartments.size() > 0) {
			SBMLCompartmentHelper compartmentHelperObject = new SBMLCompartmentHelper();
			Iterator<String> itCompartments = compartments.iterator();
			int i = 1;
			while (itCompartments.hasNext()) {
				String compartmentHeadline = itCompartments.next();
				SBML_Compartment_Writer writeCompartmentDefinition = new SBML_Compartment_Writer();
				writeCompartmentDefinition.addCompartment(_model, g,
						compartmentHeadline, compartmentHelperObject);
				i++;
			}
		}
		
		SBML_Species_Writer writeSpeciesDefinition = new SBML_Species_Writer();
		writeSpeciesDefinition.addSpecies(_model, g);
		createExtendedLayoutModel(g);
		writeSpeciesDefinition.addSpeciesGlyph(_model, g);
		
		ArrayList<String> parameters = headlineHelper(g,
				SBML_Constants.SBML_PARAMETER);
		if (parameters.size() > 0) {
			Iterator<String> itParameters = parameters.iterator();
			int i = 1;
			SBMLParameterHelper parameterHelperObject = new SBMLParameterHelper();
			while (itParameters.hasNext()) {
				String parameterHeadline = itParameters.next();
				// String presentedHeadline = "SBML Parameter "+i;
				SBML_Parameter_Writer writeParameter = new SBML_Parameter_Writer();
				writeParameter.addParameter(g, _model, parameterHeadline,
						parameterHelperObject);
				i++;
			}
		}
		
		ArrayList<String> initialAssignments = headlineHelper(g,
				SBML_Constants.SBML_INITIAL_ASSIGNMENT);
		if (initialAssignments.size() > 0) {
			Iterator<String> itInitialAssignments = initialAssignments
					.iterator();
			int i = 1;
			SBMLInitialAssignmentHelper iaHelperObject = new SBMLInitialAssignmentHelper();
			while (itInitialAssignments.hasNext()) {
				String initialAssignmentHeadline = (String) itInitialAssignments
						.next();
				// String presentedHeadline = "SBML Initial Assignment " + i;
				SBML_InitialAssignment_Writer writeInitialAssignment = new SBML_InitialAssignment_Writer();
				writeInitialAssignment.addInitialAssignment(g, _model,
						initialAssignmentHeadline, iaHelperObject);
				i++;
			}
		}
		
		SBML_Rule_Writer writeRuleDefinition = new SBML_Rule_Writer();
		writeRuleDefinition.addRules(g, _model);
		
		ArrayList<String> constraints = headlineHelper(g,
				SBML_Constants.SBML_CONSTRAINT);
		if (constraints.size() > 0) {
			Iterator<String> itConstraints = constraints.iterator();
			SBMLConstraintHelper constraintHelperObject = new SBMLConstraintHelper();
			int i = 1;
			while (itConstraints.hasNext()) {
				String constraintHeadline = (String) itConstraints.next();
				// String presentedHeadline = "SBML Constraint " + i;
				SBML_Constraint_Writer writeConstraint = new SBML_Constraint_Writer();
				writeConstraint.addConstraint(g, _model, constraintHeadline,
						constraintHelperObject);
				i++;
			}
		}
		
		SBML_Reaction_Writer writeReaction = new SBML_Reaction_Writer();
		writeReaction.addReactions(g, _model);
		
		ArrayList<String> events = headlineHelper(g, SBML_Constants.SBML_EVENT);
		if (events.size() > 0) {
			Iterator<String> itEvents = events.iterator();
			int i = 1;
			while (itEvents.hasNext()) {
				String eventHeadline = (String) itEvents.next();
				// String presentedHeadline = "SBML Event " + i;
				SBML_Event_Writer writeEvent = new SBML_Event_Writer();
				writeEvent.addEvent(g, _model, eventHeadline);
				i++;
			}
		}
		
		int eventCount = _model.getNumEvents();
		
		boolean write = false;
		try {
			URL url = new URL("http://sbml.org/Facilities/Validator/");
			URLConnection connection = url.openConnection();
			InputStream is = null;
			try {
				is = connection.getInputStream();
			} catch (Exception e) {
				ErrorMsg.addErrorMessage("No internet connection. Can not validate document.");
				/*
				 * JOptionPane .showMessageDialog(null,
				 * "No internet connection. Can not validate document before saving."
				 * );
				 */
				write = true;
			}
			if (null != is) {
				int validate = 1;
				if (!SBML_XML_ReaderWriterPlugin.isTestintMode)
					validate = JOptionPane
							.showConfirmDialog(
									null,
									"Do you want to validate the SBML file against the Level 3 Version 1 specification?");
				if (validate == 0) {
					
					doc.setConsistencyChecks(
							CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
					doc.setConsistencyChecks(
							CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
					doc.setConsistencyChecks(CHECK_CATEGORY.UNITS_CONSISTENCY,
							true);
					doc.setConsistencyChecks(CHECK_CATEGORY.MATHML_CONSISTENCY,
							true);
					doc.setConsistencyChecks(CHECK_CATEGORY.MODELING_PRACTICE,
							true);
					doc.setConsistencyChecks(CHECK_CATEGORY.SBO_CONSISTENCY,
							true);
					doc.setConsistencyChecks(
							CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
					
					int numberOfErrors = doc.checkConsistency();
					if (numberOfErrors > 0) {
						SBMLErrorLog errorLog = doc.getListOfErrors();
						for (int i = 0; i < numberOfErrors; i++) {
							ErrorMsg.addErrorMessage(errorLog.getError(i));
						}
					}
					if (numberOfErrors > 0) {
						int load = JOptionPane
								.showConfirmDialog(null,
										"The online validator detected mistakes in the file. Save anyway?");
						if (load == 0) {
							write = true;
						}
					}
					if (numberOfErrors == 0) {
						write = true;
					}
				}
				if (validate == 1) {
					write = true;
				}
			} else {
				write = true;
				JOptionPane.showMessageDialog(null,
						"Online validation not possible.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			writer.write(doc, ps);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (SBMLException e) {
			e.printStackTrace();
		}
		ps.flush();
		ps.close();
	}
	
	private void createExtendedLayoutModel(Graph g) {
		ExtendedLayoutModel extendedLayoutModel = new ExtendedLayoutModel(_model);
		_model.addExtension(SBMLHelper.SBML_LAYOUT_EXTENSION_NAMESPACE, extendedLayoutModel);
		SBMLSpeciesHelper speciesHelper = new SBMLSpeciesHelper(g);
		List<Node> nodeList = speciesHelper.getSpeciesNodes();
		Iterator<Node> itSpeicesNodes = nodeList.iterator();
		Set<String> allLayoutIDs = new HashSet<String>();
		while (itSpeicesNodes.hasNext()) {
			Node speicesNode = itSpeicesNodes.next();
			String layoutID = (String) AttributeHelper.getAttributeValue(speicesNode, SBML_Constants.SBML, SBML_Constants.SBML_LAYOUT_ID, null, null);
			if (allLayoutIDs.add(layoutID)) {
				Layout layout = extendedLayoutModel.createLayout();
				layout.setId(layoutID);
				System.out.println("in create extended layout model: " + layoutID);
			}
		}
		
	}
}