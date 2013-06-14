package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.ErrorMsg;
import org.PositionGridGenerator;
import org.Vector2d;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Priority;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.text.parser.ParseException;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.exporters.sbml.SBML_SBase_Writer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.sbml.SBML_SBase_Reader;

@SuppressWarnings({"deprecation", "unused"})
public class SBMLHelper {
	
	/**
	 * Provides necessary methods
	 */
	public static SBML_SBase_Writer attWriter = new SBML_SBase_Writer();
	
	public static PositionGridGenerator _pgg = new PositionGridGenerator(100, 100, 1000);
	
	public static SBML_SBase_Reader attReader = new SBML_SBase_Reader();
	
	public static final String SBML_LAYOUT_EXTENSION_NAMESPACE = "http://www.sbml.org/sbml/level3/version1/layout/version1";
	
	private static boolean isInitializedReaction = false;
	private static boolean isInitializedKineticLaw = false;

	private static boolean isInitializedLocalParameter = false;
	
	private static int parameterCount = 1;
	private static int initialAssignmentCount = 1;
	private static int functionDefinitionCount = 1;
	private static int unitDefinitionCount = 1;
	private static int subUnitCount = 1;
	private static int constraintCount = 1;
	private static int assignmentRuleCount = 1;
	private static int rateRuleCount = 1;
	private static int algebraicRuleCount = 1;
	private static int eventCount = 1;
	
	private static PositionGridGenerator pgg = new PositionGridGenerator(100, 100,
			1000);
	
	public static FunctionDefinition createFunctionDefinition(Graph g, String id) {
		String presentedHeadline = new StringBuffer("SBML Function Definition ").append(functionDefinitionCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_FUNCTION_DEFINITION).append(functionDefinitionCount).toString();
		setFunctionDefinitionID(g, internHeadline, id);
		FunctionDefinition fd = new FunctionDefinition();
		fd.setLevel(3);
		fd.setId(id);
		initFunctionDefinitionNiceIDs(internHeadline, presentedHeadline);
		++functionDefinitionCount;
		return fd;
	}
	
	public static void createFunctionDefinition(Graph g, FunctionDefinition fd) {
		String presentedHeadline = new StringBuffer("SBML Function Definition ").append(functionDefinitionCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_FUNCTION_DEFINITION).append(functionDefinitionCount).toString();
		initFunctionDefinitionNiceIDs(internHeadline, presentedHeadline);
		
		fd.setLevel(3);
		if (fd.isSetId()) {
			setFunctionDefinitionID(g, internHeadline, fd.getId());
		}
		if (fd.isSetName()) {
			setFunctionDefinitionName(g, internHeadline, fd.getName());
		}
		String formula = "";
		try {
			if (fd.isSetMath()) {
				ASTNode mathTree = fd.getMath();
				formula = mathTree.toFormula();
				setFunctionDefinitionFunction(g, internHeadline, formula);
			}
		} catch (SBMLException e) {
		}
		++functionDefinitionCount;
	}
	
	public static void addFunctionDefinitionName(Graph g, String id, String name) {
		setFunctionDefinitionName(g, returnFunctionDefinitionWithID(g, id), name);
	}
	
	public static void addFunctionDefinitionFunction(Graph g, String id, String function) {
		setFunctionDefinitionFunction(g, returnFunctionDefinitionWithID(g, id), function);
	}
	
	public static void deleteFunctionDefinitionID(Graph g, String id) {
		String headline = returnFunctionDefinitionWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.FUNCTION_DEFINITION_ID).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.FUNCTION_DEFINITION_ID).toString());
		}
	}
	
	public static void deleteFunctionDefinitionName(Graph g, String id) {
		String headline = returnFunctionDefinitionWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.FUNCTION_DEFINITION_NAME).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.FUNCTION_DEFINITION_NAME).toString());
		}
	}
	
	public static void deleteFunctionDefinitionFunction(Graph g, String id) {
		String headline = returnFunctionDefinitionWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.FUNCTION_DEFINITION_FUNCTION).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.FUNCTION_DEFINITION_FUNCTION).toString());
		}
	}
	
	public static void deleteFunctionDefinition(Graph g, String id) {
		deleteFunctionDefinitionName(g, id);
		deleteFunctionDefinitionFunction(g, id);
		deleteFunctionDefinitionID(g, id);
	}
	
	public static boolean isSetFunctionDefinitionName(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnFunctionDefinitionWithID(g, id),
				new StringBuffer(returnFunctionDefinitionWithID(g, id)).append(SBML_Constants.FUNCTION_DEFINITION_NAME).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetFunctionDefinitionID(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnFunctionDefinitionWithID(g, id),
				new StringBuffer(returnFunctionDefinitionWithID(g, id)).append(SBML_Constants.FUNCTION_DEFINITION_ID).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetFunctionDefinitionFunction(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnFunctionDefinitionWithID(g, id),
				new StringBuffer(returnFunctionDefinitionWithID(g, id)).append(SBML_Constants.FUNCTION_DEFINITION_FUNCTION).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Indicates if the id of a specific function definition is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return true if the value is set.
	 */
	static private Boolean isFunctionDefinitionID(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.FUNCTION_DEFINITION_ID)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the name of a specific function definition is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return true if the value is set.
	 */
	private static Boolean isFunctionDefinitionName(Graph g,
			String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.FUNCTION_DEFINITION_NAME)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the function of a specific function definition is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return true if the value is set.
	 */
	private static Boolean isFunctionDefinitionFunction(Graph g,
			String internHeadline) {
		if (AttributeHelper
				.hasAttribute(
						g,
						internHeadline,
						new StringBuffer(internHeadline).append(
								SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
								.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Deletes the id of a specific function definition
	 * 
	 * @param g
	 *        the graph where the information is deleted from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 */
	/*
	 * public static void deleteFunctionDefinitionID(Graph g, String internHeadline) {
	 * if (isFunctionDefinitionID(g, internHeadline)) {
	 * AttributeHelper.deleteAttribute(g, internHeadline, new StringBuffer(internHeadline).append(
	 * SBML_Constants.FUNCTION_DEFINITION_ID).toString());
	 * }
	 * }
	 */
	
	/**
	 * Deletes the name of a specific function definition
	 * 
	 * @param g
	 *        the graph where the information is deleted from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 */
	/*
	 * public static void deleteFunctionDefinitionName(Graph g, String internHeadline) {
	 * if (isFunctionDefinitionName(g, internHeadline)) {
	 * AttributeHelper.deleteAttribute(g, internHeadline, new StringBuffer(internHeadline).append(
	 * SBML_Constants.FUNCTION_DEFINITION_NAME).toString());
	 * }
	 * }
	 */
	
	/**
	 * Deletes the function of a specific function definition
	 * 
	 * @param g
	 *        the graph where the information is deleted from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 */
	/*
	 * public static void deleteFunctionDefinitionFunction(Graph g, String internHeadline) {
	 * if (isFunctionDefinitionFunction(g, internHeadline)) {
	 * AttributeHelper.deleteAttribute(g, internHeadline, new StringBuffer(internHeadline).append(
	 * SBML_Constants.FUNCTION_DEFINITION_FUNCTION).toString());
	 * }
	 * }
	 */
	
	/**
	 * Returns the function definition id if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return the id if it is set. Else the empty string
	 */
	private static String getFunctionDefinitionID(Graph g, String internHeadline) {
		if (isFunctionDefinitionID(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.FUNCTION_DEFINITION_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the function definition name if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return the name if it is set. Else the empty string
	 */
	private static String getFunctionDefinitionName(Graph g,
			String internHeadline) {
		if (isFunctionDefinitionName(g, internHeadline)) {
			return (String) attWriter
					.getAttribute(
							g,
							internHeadline,
							new StringBuffer(internHeadline).append(
									SBML_Constants.FUNCTION_DEFINITION_NAME)
									.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the function if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return the function if it is set. Else null
	 */
	private static ASTNode getFunctionDefinitionFunction(Graph g,
			String internHeadline) {
		if (isFunctionDefinitionFunction(g, internHeadline)) {
			try {
				return ASTNode.parseFormula((String) attWriter.getAttribute(
						g,
						internHeadline,
						new StringBuffer(internHeadline).append(
								SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
								.toString()));
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the id of a function definition
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @param ID
	 *        the value that will be read in
	 */
	private static void setFunctionDefinitionID(Graph g, String internHeadline,
			String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.FUNCTION_DEFINITION_ID).toString(),
					ID);
		}
	}
	
	/**
	 * Sets the name of a function definition
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @param name
	 *        the value that will be read in
	 */
	private static void setFunctionDefinitionName(Graph g,
			String internHeadline, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper
					.setAttribute(
							g,
							internHeadline,
							new StringBuffer(internHeadline).append(
									SBML_Constants.FUNCTION_DEFINITION_NAME)
									.toString(), name);
			
		}
	}
	
	/**
	 * Sets the function of a function definition
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @param function
	 *        the value that will be read in
	 */
	private static void setFunctionDefinitionFunction(Graph g,
			String internHeadline, String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
							.toString(), function);
		}
	}
	
	/**
	 * Returns all intern function definition headlines
	 * 
	 * @param g
	 *        the headlines are stored in this graph
	 * @return a list of intern headlines
	 */
	private static ArrayList<String> getFunctionDefinitionHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer
				.headlineHelper(g, SBML_Constants.SBML_FUNCTION_DEFINITION);
	}
	
	/**
	 * Returns the number of function definitions in the graph
	 * 
	 * @param g
	 *        where the information is read from
	 * @return number of function definitios
	 */
	public static int countFunctionDefinitions(Graph g) {
		return getFunctionDefinitionHeadlines(g).size();
	}
	
	/**
	 * Returns a filled JSBML FunctionDefinition object. The function writes the attributes from a function definition in the graph into a JSBML function
	 * definition.
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current function definition
	 * @return a filled FunctionDefinition object
	 */
	private static FunctionDefinition getFunctionDefinition(Graph g,
			String internHeadline) {
		FunctionDefinition fd = new FunctionDefinition();
		fd.setLevel(3);
		if (isFunctionDefinitionID(g, internHeadline)) {
			fd.setId(getFunctionDefinitionID(g, internHeadline));
		}
		if (isFunctionDefinitionName(g, internHeadline)) {
			fd.setName(getFunctionDefinitionName(g, internHeadline));
		}
		if (isFunctionDefinitionFunction(g, internHeadline)) {
			fd.setMath(getFunctionDefinitionFunction(g, internHeadline));
		}
		return fd;
	}
	
	/**
	 * Returns a list of filled JSBML FunctionDefinition objects
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        contains the number of the current function definition
	 * @return a filled list of function definitions
	 */
	private static List<FunctionDefinition> getAllFunctionDefinitions(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<FunctionDefinition> functionDefinitionList = new ArrayList<FunctionDefinition>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			functionDefinitionList
					.add(getFunctionDefinition(g, internHeadline));
		}
		return functionDefinitionList;
	}
	
	/**
	 * Returns a list of filled JSBML FunctionDefinition objects
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of function definitions
	 */
	public static List<FunctionDefinition> getAllFunctionDefinitions(Graph g) {
		List<String> internHeadlines = getFunctionDefinitionHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<FunctionDefinition> functionDefinitionList = new ArrayList<FunctionDefinition>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			functionDefinitionList
					.add(getFunctionDefinition(g, internHeadline));
		}
		return functionDefinitionList;
	}
	
	public static UnitDefinition createUnitDefinition(Graph g, String id) {
		String presentedHeadline = new StringBuffer("SBML Unit Definition ")
				.append(unitDefinitionCount).toString();
		String internHeadline = new StringBuffer(
				SBML_Constants.SBML_UNIT_DEFINITION).append(unitDefinitionCount)
				.toString();
		setUnitDefinitionID(g, internHeadline, id);
		UnitDefinition ud = new UnitDefinition();
		ud.setId(id);
		initUnitDefinitionNideIDs(internHeadline, presentedHeadline);
		++unitDefinitionCount;
		return ud;
	}
	
	public static void createUnitDefinition(Graph g, UnitDefinition ud) {
		String presentedHeadline = new StringBuffer("SBML Unit Definition ")
				.append(unitDefinitionCount).toString();
		String internHeadline = new StringBuffer(
				SBML_Constants.SBML_UNIT_DEFINITION).append(unitDefinitionCount)
				.toString();
		
		if (ud.isSetId()) {
			ud.setLevel(3);
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNIT_DEFINITION_ID)
					.toString(), ud.getId());
		}
		if (ud.isSetName()) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNIT_DEFINITION_NAME)
					.toString(), ud.getName());
		}
		
		List<String> ListOfSubUnits = new ArrayList<String>();
		if (ud.isSetListOfUnits()) {
			List<Unit> unitList = ud.getListOfUnits();
			Iterator<Unit> itUnit = unitList.iterator();
			while (itUnit.hasNext()) {
				initSubUnitDefinitionNideIDs(internHeadline, presentedHeadline, subUnitCount);
				Unit unit = itUnit.next();
				int scale = unit.getScale();
				Double exponent = unit.getExponent();
				if (exponent.equals(Double.NaN)) {
					ErrorMsg.addErrorMessage("Attribute exponent of unit definition "
							+ subUnitCount
							+ " sub unit "
							+ subUnitCount
							+ " with the id "
							+ ud.getId()
							+ " is not a valid double value.");
				}
				Double multiplier = unit.getMultiplier();
				if (multiplier.equals(Double.NaN)) {
					ErrorMsg.addErrorMessage("Attribute multiplier of unit definition "
							+ subUnitCount
							+ " sub unit "
							+ subUnitCount
							+ " with the id "
							+ ud.getId()
							+ " is not a valid double value.");
				}
				String kind = unit.getKind().getName();
				String composedSubUnit = "(" + multiplier + " * 10^"
						+ scale + " * " + kind + ")^" + exponent;
				setComposedSubUnit(g, internHeadline, composedSubUnit, subUnitCount);
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
			setComposedUnit(g, internHeadline, composedUnit);
		}
		initUnitDefinitionNideIDs(internHeadline, presentedHeadline);
		subUnitCount = 1;
		++unitDefinitionCount;
	}
	
	public static void addUnitToUnitDefinition(Graph g, String id, Unit unit) {
		String headline = returnUnitDefinitionWithID(g, id);
		char index = headline.charAt(headline.length() - 1);
		String presentedHeadline = new StringBuffer("SBML Unit Definition ")
				.append(index).toString();
		initSubUnitDefinitionNideIDs(headline, presentedHeadline, getSubUnitCount(g, headline) + 1);
		
		int scale = unit.getScale();
		Double exponent = unit.getExponent();
		if (exponent.equals(Double.NaN)) {
			ErrorMsg.addErrorMessage("Attribute exponent of unit definition "
					+ getSubUnitCount(g, headline)
					+ " sub unit "
					+ getSubUnitCount(g, headline)
					+ " is not a valid double value.");
		}
		Double multiplier = unit.getMultiplier();
		if (multiplier.equals(Double.NaN)) {
			ErrorMsg.addErrorMessage("Attribute multiplier of unit definition "
					+ getSubUnitCount(g, headline)
					+ " sub unit "
					+ getSubUnitCount(g, headline)
					+ " is not a valid double value.");
		}
		String kind = unit.getKind().getName();
		String composedSubUnit = "(" + multiplier + " * 10^"
				+ scale + " * " + kind + ")^" + exponent;
		setComposedSubUnit(g, headline, composedSubUnit, getSubUnitCount(g, headline) + 1);
		String composedUnit = getComposedUnit(g, headline);
		String nextComposedUnit = "";
		if (composedUnit != "") {
			nextComposedUnit = composedUnit + " * " + composedSubUnit;
		}
		else {
			nextComposedUnit = composedSubUnit;
		}
		setComposedUnit(g, headline, nextComposedUnit);
		
	}
	
	public static void addUnitToUnitDefinition(Graph g, String id, Unit.Kind kind, double exponent, double multiplier, int scale) {
		String headline = returnUnitDefinitionWithID(g, id);
		char index = headline.charAt(headline.length() - 1);
		String presentedHeadline = new StringBuffer("SBML Unit Definition ")
				.append(index).toString();
		initSubUnitDefinitionNideIDs(headline, presentedHeadline, getSubUnitCount(g, headline) + 1);
		
		String composedSubUnit = "(" + multiplier + " * 10^"
				+ scale + " * " + kind + ")^" + exponent;
		setComposedSubUnit(g, headline, composedSubUnit, getSubUnitCount(g, headline) + 1);
		String composedUnit = getComposedUnit(g, headline);
		String nextComposedUnit = "";
		if (composedUnit != "") {
			nextComposedUnit = composedUnit + " * " + composedSubUnit;
		}
		else {
			nextComposedUnit = composedSubUnit;
		}
		setComposedUnit(g, headline, nextComposedUnit);
		
	}
	
	public static void addUnitDefinitionName(Graph g, String id, String name) {
		setUnitDefinitionName(g, returnUnitDefinitionWithID(g, id), name);
	}
	
	public static void addUnitDefinitionID(Graph g, String oldID, String newID) {
		setUnitDefinitionID(g, returnUnitDefinitionWithID(g, oldID), newID);
	}
	
	public static void deleteUnitDefinitionID(Graph g, String id) {
		String headline = returnUnitDefinitionWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(
				headline).append(SBML_Constants.UNIT_DEFINITION_ID)
				.toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.UNIT_DEFINITION_ID)
					.toString());
		}
	}
	
	public static void deleteUnitDefinitionName(Graph g, String id) {
		String headline = returnUnitDefinitionWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(
				headline).append(SBML_Constants.UNIT_DEFINITION_NAME)
				.toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.UNIT_DEFINITION_NAME)
					.toString());
		}
	}
	
	public static boolean isSetUnitDefinitionName(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnUnitDefinitionWithID(g, id), new StringBuffer(
				returnUnitDefinitionWithID(g, id)).append(SBML_Constants.UNIT_DEFINITION_NAME)
				.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetUnitDefinitionID(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnUnitDefinitionWithID(g, id), new StringBuffer(
				returnUnitDefinitionWithID(g, id)).append(SBML_Constants.UNIT_DEFINITION_ID)
				.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Indicates if the unit definition id is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return true if the value is set.
	 */
	private static Boolean isUnitDefinitionID(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.UNIT_DEFINITION_ID)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the unit definition name is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return true if the value is set.
	 */
	private static Boolean isUnitDefinitionName(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.UNIT_DEFINITION_NAME)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the unit definition id if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return the id if it is set. Else the empty string
	 */
	private static String getUnitDefinitionID(Graph g, String internHeadline) {
		if (isUnitDefinitionID(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.UNIT_DEFINITION_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the unit definition name if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return the name if it is set. Else the empty string
	 */
	private static String getUnitDefinitionName(Graph g, String internHeadline) {
		if (isUnitDefinitionName(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.UNIT_DEFINITION_NAME).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Sets the id of a unit definition
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @param ID
	 *        the value that will be read in
	 */
	private static void setUnitDefinitionID(Graph g, String internHeadline,
			String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNIT_DEFINITION_ID)
					.toString(), ID);
		}
	}
	
	/**
	 * Sets the name of a unit definition
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @param name
	 *        the value that will be read in
	 */
	private static void setUnitDefinitionName(Graph g, String internHeadline,
			String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNIT_DEFINITION_NAME)
					.toString(), name);
		}
	}
	
	/**
	 * Sets a composed sub unit. All composed sub units will merge to a composed
	 * unit
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @param composedSubUnit
	 *        the information that will be read in
	 * @param subUnitCount
	 *        the number of the current sub unit
	 */
	private static void setComposedSubUnit(Graph g, String internHeadline,
			String composedSubUnit, int subUnitCount) {
		if (!composedSubUnit.equals(SBML_Constants.EMPTY)) {
			System.out.println("drin");
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SUB_UNIT)
							.append(subUnitCount).append("_").toString(),
					composedSubUnit);
		}
	}
	
	/**
	 * Sets the composed unit which consists of the composed sub units
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @param composedUnit
	 *        the information that will be set
	 */
	private static void setComposedUnit(Graph g, String internHeadline,
			String composedUnit) {
		if (!composedUnit.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNIT).toString(),
					composedUnit);
		}
	}
	
	/**
	 * Indicates if a composed sub unit with a certain index is set
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @param unitCount
	 *        the index of the sub unit
	 * @return true if a sub unit with a certain index is set
	 */
	private static boolean isComposedSubUnit(Graph g, String internHeadline,
			int unitCount) {
		if (AttributeHelper.hasAttribute(g, internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(unitCount)
						.append(SBML_Constants.UNDERLINE).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if there is a composed unit
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return true if the value is set. Else false
	 */
	private static boolean isComposedUnit(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.UNIT).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return the sub unit with a certain number
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @param unitCount
	 *        the number of the sub unit
	 * @return the sub unit if it is set. Else the empty string
	 */
	private static String getComposedSubUnit(Graph g, String internHeadline,
			int unitCount) {
		if (isComposedSubUnit(g, internHeadline, unitCount)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SUB_UNIT).append(unitCount)
							.append(SBML_Constants.UNDERLINE).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the composed unit which consists of the composed sub units
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return the composed unit if it is set. Else the empty string
	 */
	private static String getComposedUnit(Graph g, String internHeadline) {
		if (isComposedUnit(g, internHeadline)) {
			return (String) attWriter.getAttribute(g, internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.UNIT).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the amount of sub units of a certain unit definition
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return the amount of sub units
	 */
	private static int getSubUnitCount(Graph g, String internHeadline) {
		int suCount = 0;
		while (isComposedSubUnit(g, internHeadline, suCount + 1)) {
			suCount += 1;
		}
		return suCount;
	}
	
	/**
	 * Returns a filled JSBML UnitDefinition object
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current unit definition
	 * @return a filled UnitDefinition object
	 */
	private static UnitDefinition getUnitDefinition(Graph g,
			String internHeadline) {
		UnitDefinition ud = new UnitDefinition();
		ud.setLevel(3);
		if (isUnitDefinitionID(g, internHeadline)) {
			ud.setId(getUnitDefinitionID(g, internHeadline));
		}
		if (isUnitDefinitionName(g, internHeadline)) {
			ud.setName(getUnitDefinitionName(g, internHeadline));
		}
		for (int i = 1; i <= getSubUnitCount(g, internHeadline); i++) {
			String subUnit = getComposedSubUnit(g, internHeadline, i);
			String[] subUnitArray = subUnit.split(Pattern.quote(")"));
			String exponent = subUnitArray[1].replace("^", "");
			String[] subUnitArray2 = subUnitArray[0].split("\\*");
			String multiplier = subUnitArray2[0].replace("(", "");
			String[] subUnitArray3 = subUnitArray2[1].split("\\^");
			String scale = subUnitArray3[1];
			String kind = subUnitArray2[2];
			Unit u = new Unit();
			u.setLevel(3);
			u.setKind(Kind.valueOf(kind.trim().toUpperCase()));
			u.setExponent(Double.parseDouble(exponent.trim()));
			u.setMultiplier(Double.parseDouble(multiplier.trim()));
			u.setScale(Integer.parseInt(scale.trim()));
			ud.addUnit(u);
		}
		return ud;
	}
	
	/**
	 * Returns a filled list of UnitDefinition objects
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadlines
	 *        contains the number of the current unit definition
	 * @return a filled UnitDefinition list
	 */
	private static List<UnitDefinition> getAllUnitDefinitions(Graph g,
			List<String> internHeadlines) {
		List<UnitDefinition> udList = new ArrayList<UnitDefinition>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while (internHeadlineIt.hasNext()) {
			String internHeadline = internHeadlineIt.next();
			udList.add(getUnitDefinition(g, internHeadline));
		}
		return udList;
	}
	
	/**
	 * Returns a list of all unit definition headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getUnitDefinitionHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_UNIT_DEFINITION);
	}
	
	/**
	 * Returns a filled list of UnitDefinition objects
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return a filled UnitDefinition list
	 */
	public static List<UnitDefinition> getAllUnitDefinitions(Graph g) {
		ArrayList<String> internHeadlines = getUnitDefinitionHeadlines(g);
		List<UnitDefinition> udList = new ArrayList<UnitDefinition>();
		Iterator<String> internHeadlineIt = internHeadlines.iterator();
		while (internHeadlineIt.hasNext()) {
			String internHeadline = internHeadlineIt.next();
			udList.add(getUnitDefinition(g, internHeadline));
		}
		return udList;
	}
	
	/**
	 * Creates an compartment in the graph tab with an specific id
	 * 
	 * @return the Compartment object will be returned
	 */
	public static Compartment createCompartment(Graph g, String id) {
		String presentedHeadline = new StringBuffer(
				SBML_Constants.COMARTMENT_HEADLINE).append(id)
				.toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_COMPARTMENT)
				.append(id).toString();
		setCompartmentID(g, internHeadline, id);
		Compartment comp = new Compartment();
		comp.setId(id);
		initCompartmentNideIDs(internHeadline, presentedHeadline);
		return comp;
	}
	
	/**
	 * @param compartment
	 */
	public static void createCompartment(Graph g, Compartment compartment) {
		String internHeadline = new StringBuffer(SBML_Constants.SBML_COMPARTMENT)
				.append(compartment.getId()).toString();
		String presentedHeadline = SBML_Constants.EMPTY;
		if (compartment.isSetName()) {
			presentedHeadline = new StringBuffer(
					SBML_Constants.COMARTMENT_HEADLINE).append(compartment.getName())
					.toString();
		} else if (compartment.isSetId()) {
			presentedHeadline = new StringBuffer(
					SBML_Constants.COMARTMENT_HEADLINE).append(compartment.getId())
					.toString();
		}
		initCompartmentNideIDs(internHeadline, presentedHeadline);
		
		if (compartment.isSetId()
				&& Compartment.isValidId(compartment.getId(), compartment.getLevel(),
						compartment.getVersion())) {
			setCompartmentID(g, internHeadline, compartment.getId());
		}
		if (compartment.isSetName() && (compartment.getName() != SBML_Constants.EMPTY)) {
			setCompartmentName(g, internHeadline, compartment.getName());
		}
		if (compartment.isSetSpatialDimensions()) {
			setCompartmentSpatialDimensions(g, internHeadline, compartment.getSpatialDimensions());
		}
		if (compartment.isSetSize()) {
			setCompartmentSize(g, internHeadline, compartment.getSize());
		}
		if (compartment.isSetUnits()) {
			setCompartmentUnits(g, internHeadline, compartment.getUnits());
		}
		if (compartment.isSetConstant()) {
			setCompartmentConstant(g, internHeadline, compartment.getConstant());
		}
	}
	
	public static boolean isSetCompartmentName(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnCommpartmentHeadlineWithID(g, id),
				new StringBuffer(returnCommpartmentHeadlineWithID(g, id)).append(SBML_Constants.COMPARTMENT_NAME)
						.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetCompartmentID(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnCommpartmentHeadlineWithID(g, id),
				new StringBuffer(returnCommpartmentHeadlineWithID(g, id)).append(SBML_Constants.COMPARTMENT_ID)
						.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetCompartmentSpatialDimensions(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnCommpartmentHeadlineWithID(g, id),
				new StringBuffer(returnCommpartmentHeadlineWithID(g, id)).append(SBML_Constants.SPATIAL_DIMENSIONS)
						.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetCompartmentSize(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnCommpartmentHeadlineWithID(g, id),
				new StringBuffer(returnCommpartmentHeadlineWithID(g, id)).append(SBML_Constants.SIZE)
						.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetCompartmentUnits(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnCommpartmentHeadlineWithID(g, id),
				new StringBuffer(returnCommpartmentHeadlineWithID(g, id)).append(SBML_Constants.UNITS)
						.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetCompartmentConstants(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnCommpartmentHeadlineWithID(g, id),
				new StringBuffer(returnCommpartmentHeadlineWithID(g, id)).append(SBML_Constants.CONSTANT)
						.toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void addCompartmentName(Graph g, String id, String name) {
		setCompartmentName(g, returnCommpartmentHeadlineWithID(g, id), name);
	}
	
	public static void addCompartmentSpatialDimensions(Graph g, String id, Double value) {
		setCompartmentSpatialDimensions(g, returnCommpartmentHeadlineWithID(g, id), value);
	}
	
	public static void addCompartmentSize(Graph g, String id, Double value) {
		setCompartmentSize(g, returnCommpartmentHeadlineWithID(g, id), value);
	}
	
	public static void addCompartmentUnits(Graph g, String id, String units) {
		setCompartmentUnits(g, returnCommpartmentHeadlineWithID(g, id), units);
	}
	
	public static void addCompartmentConstant(Graph g, String id, boolean constant) {
		setCompartmentConstant(g, returnCommpartmentHeadlineWithID(g, id), constant);
	}
	
	public static void deleteCompartmentID(Graph g, String id) {
		String headline = returnCommpartmentHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.COMPARTMENT_ID).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.COMPARTMENT_ID).toString());
		}
	}
	
	public static void deleteCompartmentName(Graph g, String id) {
		String headline = returnCommpartmentHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.COMPARTMENT_NAME).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.COMPARTMENT_NAME).toString());
		}
	}
	
	public static void deleteCompartmentSpatialDimensions(Graph g, String id) {
		String headline = returnCommpartmentHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.SPATIAL_DIMENSIONS).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.SPATIAL_DIMENSIONS).toString());
		}
	}
	
	public static void deleteCompartmentSize(Graph g, String id) {
		String headline = returnCommpartmentHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.SIZE).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.SIZE).toString());
		}
	}
	
	public static void deleteCompartmentUnits(Graph g, String id) {
		String headline = returnCommpartmentHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.UNITS).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.UNITS).toString());
		}
	}
	
	public static void deleteCompartmentConstant(Graph g, String id) {
		String headline = returnCommpartmentHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.CONSTANT).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.CONSTANT).toString());
		}
	}
	
	public static void deleteCompartment(Graph g, String id) {
		deleteCompartmentSpatialDimensions(g, id);
		deleteCompartmentName(g, id);
		deleteCompartmentSize(g, id);
		deleteCompartmentUnits(g, id);
		deleteCompartmentConstant(g, id);
		deleteCompartmentID(g, id);
	}
	
	private static String returnCommpartmentHeadlineWithID(Graph g, String id) {
		ArrayList<String> headlines = getCompartmentHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Compartment comp = getCompartment(g, headline);
			if (comp.getId() == id) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnParameterHeadlineWithID(Graph g, String id) {
		ArrayList<String> headlines = getParameterHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Parameter para = getParameter(g, headline);
			if (para.getId() == id) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnInitialAssignmentWithSymbol(Graph g, String symbol) {
		ArrayList<String> headlines = getInitialAssignmentHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			InitialAssignment ia = getInitialAssignment(g, headline);
			if (ia.getVariable() == symbol) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnFunctionDefinitionWithID(Graph g, String id) {
		ArrayList<String> headlines = getFunctionDefinitionHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			FunctionDefinition fd = getFunctionDefinition(g, headline);
			if (fd.getId() == id) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnUnitDefinitionWithID(Graph g, String id) {
		ArrayList<String> headlines = getUnitDefinitionHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			UnitDefinition ud = getUnitDefinition(g, headline);
			ud.setLevel(3);
			if (ud.getId() == id) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnAssignmentRuleWithID(Graph g, String variable) {
		ArrayList<String> headlines = getAssignmentRuleHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			AssignmentRule ar = getAssignmentRule(g, headline);
			ar.setLevel(3);
			if (ar.getVariable() == variable) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnRateRuleWithID(Graph g, String variable) {
		ArrayList<String> headlines = getRateRuleHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			RateRule rr = getRateRule(g, headline);
			rr.setLevel(3);
			if (rr.getVariable() == variable) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnAlgebraicRuleWithFormula(Graph g, String formula) {
		ArrayList<String> headlines = getAlgebraicRuleHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			AlgebraicRule ar = getAlgebraicRule(g, headline);
			ar.setLevel(3);
			if (ar.getMath().toFormula().equals(formula)) {
				return headline;
			}
		}
		return "";
	}
	
	private static String returnEventWithID(Graph g, String id) {
		ArrayList<String> headlines = getEventHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Event event = getEvent(g, headline);
			event.setLevel(3);
			if (event.getId() == id) {
				return headline;
			}
		}
		return "";
	}
	
	/**
	 * Indicates if the compartment id is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return true if the value is set.
	 */
	private static Boolean isCompartmentID(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.COMPARTMENT_ID)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the compartment name is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return true if the value is set.
	 */
	private static Boolean isCompartmentName(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.COMPARTMENT_NAME)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the compartment spatial dimensions is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return true if the value is set.
	 */
	private static Boolean isCompartmentSpatialDimensions(Graph g,
			String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.SPATIAL_DIMENSIONS)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the compartment size is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return true if the value is set.
	 */
	private static Boolean isCompartmentSize(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.SIZE).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the compartment units is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return true if the value is set.
	 */
	private static Boolean isCompartmentUnits(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.UNITS).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the compartment constant is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return true if the value is set.
	 */
	private static Boolean isCompartmentConstant(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.CONSTANT).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the compartment id if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return the id if it is set. Else the empty string
	 */
	private static String getCompartmentID(Graph g, String internHeadline) {
		if (isCompartmentID(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.COMPARTMENT_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the compartment name if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return the name if it is set. Else the empty string
	 */
	private static String getCompartmentName(Graph g, String internHeadline) {
		if (isCompartmentName(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.COMPARTMENT_NAME).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the compartment spatialDimensions if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return the spatialDimensions if it is set. Else null
	 */
	private static Double getCompartmentSpatialDimensions(Graph g,
			String internHeadline) {
		if (isCompartmentSpatialDimensions(g, internHeadline)) {
			return (Double) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.SPATIAL_DIMENSIONS).toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the compartment size if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return the size if it is set. Else null the empty string
	 */
	private static Double getCompartmentSize(Graph g, String internHeadline) {
		if (isCompartmentSize(g, internHeadline)) {
			return Double.parseDouble((String) attWriter.getAttribute(g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.SIZE).toString()));
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the compartment units if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return the units if it is set. Else the empty string
	 */
	private static String getCompartmentUnits(Graph g, String internHeadline) {
		if (isCompartmentUnits(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.UNITS).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the compartment attribute constant if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return constant if it is set. Else null
	 */
	private static Boolean getCompartmentConstant(Graph g, String internHeadline) {
		if (isCompartmentConstant(g, internHeadline)) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.CONSTANT).toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the id of a compartment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @param id
	 *        the value that will be read in
	 */
	private static void setCompartmentID(Graph g, String internHeadline,
			String id) {
		if (!id.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.COMPARTMENT_ID)
					.toString(), id);
		}
	}
	
	/**
	 * Sets the name of a compartment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @param name
	 *        the value that will be read in
	 */
	private static void setCompartmentName(Graph g, String internHeadline,
			String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.COMPARTMENT_NAME)
					.toString(), name);
		}
	}
	
	/**
	 * Sets the spatial dimension of a compartment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @param spatialDimension
	 *        the value that will be read in
	 */
	private static void setCompartmentSpatialDimensions(Graph g,
			String internHeadline, Double spatialDimensions) {
		if (!spatialDimensions.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SPATIAL_DIMENSIONS)
					.toString(), spatialDimensions);
		}
	}
	
	/**
	 * Sets the size of a compartment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @param size
	 *        the value that will be read in
	 */
	private static void setCompartmentSize(Graph g, String internHeadline,
			Double size) {
		if (!size.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SIZE).toString(),
					Double.toString(size));
		}
	}
	
	/**
	 * Sets the units of a compartment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @param units
	 *        the value that will be read in
	 */
	private static void setCompartmentUnits(Graph g, String internHeadline,
			String units) {
		if (!units.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.UNITS).toString(),
					units);
		}
	}
	
	/**
	 * Sets the attribute constant of a compartment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @param constant
	 *        the value that will be read in
	 */
	private static void setCompartmentConstant(Graph g, String internHeadline,
			Boolean constant) {
		if (!constant.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.CONSTANT).toString(),
					constant);
		}
	}
	
	/**
	 * Returns a list of all compartment headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getCompartmentHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_COMPARTMENT);
	}
	
	/**
	 * Returns a filled JSBML Compartment object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current compartment
	 * @return a filled Compartment object
	 */
	private static Compartment getCompartment(Graph g, String internHeadline) {
		Compartment compartment = new Compartment();
		compartment.setId(getCompartmentID(g, internHeadline));
		compartment.setName(getCompartmentName(g, internHeadline));
		
		if (isCompartmentSpatialDimensions(g, internHeadline) && compartment.getLevel() == 3) {
			compartment.setSpatialDimensions(getCompartmentSpatialDimensions(g,
					internHeadline));
		}
		if (isCompartmentSize(g, internHeadline)) {
			compartment.setSize(getCompartmentSize(g, internHeadline));
		}
		compartment.setUnits(getCompartmentUnits(g, internHeadline));
		if (isCompartmentConstant(g, internHeadline)) {
			compartment.setConstant(getCompartmentConstant(g, internHeadline));
		}
		return compartment;
	}
	
	/**
	 * Returns a list of compartments with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current compartment
	 * @return a filled list of compartments
	 */
	private static List<Compartment> getAllCompartments(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Compartment> compartmentList = new ArrayList<Compartment>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			compartmentList.add(getCompartment(g, internHeadline));
		}
		return compartmentList;
	}
	
	/**
	 * Returns a list of compartments with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of compartments
	 */
	public static List<Compartment> getAllCompartments(Graph g) {
		ArrayList<String> internHeadlines = getCompartmentHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Compartment> compartmentList = new ArrayList<Compartment>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			compartmentList.add(getCompartment(g, internHeadline));
		}
		return compartmentList;
	}
	
	/**
	 * Indicates if the id of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the id is set else false
	 */
	public static Boolean isSpeciesID(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the name of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the name is set else false
	 */
	public static Boolean isSpeciesName(Node speciesNode) {
		if (!AttributeHelper.getLabel(speciesNode, SBML_Constants.EMPTY)
				.equals(SBML_Constants.EMPTY) || AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_NAME)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the compartment id of a species is set
	 * 
	 * @param node
	 *        the node where the information is read from
	 * @return true if the compartment id is set else false
	 */
	public static Boolean isSpeciesCompartment(Node speciesNode) {
		if (!NodeTools.getClusterID(speciesNode, SBML_Constants.EMPTY).equals(
				SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the initial amount of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the initial amount is set else false
	 */
	public static Boolean isSpeciesInitialAmount(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.INITIAL_AMOUNT)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the initial concentration of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if the initial concentration is set else false
	 */
	public static Boolean isSpeciesInitialConcentration(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.INITIAL_CONCENTRATION)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if substance units of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if substance units is set else false
	 */
	public static Boolean isSpeciesSubstanceUnits(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_SUBSTANCE_UNITS)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if hasOnlySubstanceUnits of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if hasOnlySubstanceUnits is set else false
	 */
	public static Boolean isSpeciesHasOnlySubstanceUnits(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if boundary condition of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if boundary condition is set else false
	 */
	public static Boolean isSpeciesBoundaryCondition(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.BOUNDARY_CONDITION)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if constant of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if constant is set else false
	 */
	public static Boolean isSpeciesConstant(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_CONSTANT)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if conversion factor of a species is set
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return true if conversion factor is set else false
	 */
	public static Boolean isSpeciesConversionFactor(Node speciesNode) {
		if (AttributeHelper.hasAttribute(speciesNode, SBML_Constants.SBML,
				SBML_Constants.SPECIES_CONVERSION_FACTOR)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the id of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return the species id if it is set else the empty string
	 */
	public static String getSpeciesID(Node speciesNode) {
		if (isSpeciesID(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the name of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return the species name if it is set else the empty string
	 */
	public static String getSpeciesName(Node speciesNode) {
		if (!isSpeciesName(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_NAME);
		}
		return AttributeHelper.getLabel(speciesNode, (String) attWriter.getAttribute(speciesNode, SBML_Constants.SBML, SBML_Constants.SPECIES_NAME));
	}
	
	/**
	 * Returns the compartment id of a species
	 * 
	 * @param Node
	 *        the node where the information is read from
	 * @return the compartment id if it is set else the empty string
	 */
	public static String getSpeciesCompartment(Node speciesNode) {
		return NodeTools.getClusterID(speciesNode, SBML_Constants.EMPTY);
	}
	
	/**
	 * Returns the initial amount of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return initial amount if it is set else null
	 */
	public static Double getSpeciesInitialAmount(Node speciesNode) {
		if (isSpeciesInitialAmount(speciesNode)) {
			return (Double) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.INITIAL_AMOUNT);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the initial concentration of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return initial concentration if it is set else null
	 */
	public static Double getSpeciesInitialConcentration(Node speciesNode) {
		if (isSpeciesInitialConcentration(speciesNode)) {
			return (Double) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.INITIAL_CONCENTRATION);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the substance units of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return substance Units if it is set else the empty string
	 */
	public static String getSpeciesSubstanceUnits(Node speciesNode) {
		if (isSpeciesSubstanceUnits(speciesNode)) {
			return (String) attWriter
					.getAttribute(speciesNode, SBML_Constants.SBML,
							SBML_Constants.SPECIES_SUBSTANCE_UNITS);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the boolean value hasOnlySubstanceUnits of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return a boolean value if the attribute hasOnlySubstanceUnits is set
	 *         else null
	 */
	public static Boolean getSpeciesHasOnlySubstanceUnits(Node speciesNode) {
		if (isSpeciesHasOnlySubstanceUnits(speciesNode)) {
			return (Boolean) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML,
					SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the boolean value boundaryCondition of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return a boolean value if the attribute boundaryCondition is set else
	 *         null
	 */
	public static Boolean getSpeciesBoundaryCondition(Node speciesNode) {
		if (isSpeciesBoundaryCondition(speciesNode)) {
			return (Boolean) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.BOUNDARY_CONDITION);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the boolean value constant of the species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return a boolean value if the attribute constant is set else null
	 */
	public static Boolean getSpeciesConstant(Node speciesNode) {
		if (isSpeciesConstant(speciesNode)) {
			return (Boolean) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML, SBML_Constants.SPECIES_CONSTANT);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the conversion factor units of a species
	 * 
	 * @param speciesNode
	 *        the node where the information is read from
	 * @return conversion factor if it is set else null
	 */
	public static String getSpeciesConversionFactor(Node speciesNode) {
		if (isSpeciesConversionFactor(speciesNode)) {
			return (String) attWriter.getAttribute(speciesNode,
					SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONVERSION_FACTOR);
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the id of a species node
	 * 
	 * @param speciesNode
	 *        where the information should be read in
	 * @param id
	 *        the id to set
	 */
	public static void addSpeciesID(Node speciesNode, String id) {
		if (!id.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_ID, id);
		}
	}
	
	public static void addSpeciesName(Node speciesNode, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_NAME, name);
		}
	}
	
	public static void setSpeciesAttributes(Node sbmlNode, String niceID, Object value) {
		if (!value.equals(SBML_Constants.EMPTY) && !(value == null)) {
			if (value instanceof Boolean) {
				AttributeHelper.setAttribute(sbmlNode, SBML_Constants.SBML,
						niceID, (Boolean) value);
			}
			else {
				AttributeHelper.setAttribute(sbmlNode, SBML_Constants.SBML,
						niceID, (String) value);
			}
		}
	}
	
	/**
	 * Sets the label of a node. The id string will be the label if name is not
	 * set
	 * 
	 * @param speciesNode
	 *        where the information should be read in
	 * @param name
	 *        the name to set
	 * @param id
	 *        will be set if name is empty
	 * @param _pgg
	 *        helps to set the position of the node
	 */
	private static void setSpeciesLabel(Node speciesNode, String name,
			String id) {
		String label = null;
		if (!name.equals(SBML_Constants.EMPTY)) {
			label = name;
		} else {
			label = id;
		}
		if (!label.equals(SBML_Constants.EMPTY)) {
			attReader.setAttributes(speciesNode, Color.white, label,
					_pgg.getNextPosition(), label.length() + 7);
		}
	}
	
	/**
	 * Sets the compartment of a node
	 * 
	 * @param speciesNode
	 *        the compartment belongs to this node
	 * @param compartment
	 *        the id of the compartment that will be set
	 */
	public static void addSpeciesCompartment(Node speciesNode,
			String compartment) {
		if (!compartment.equals(SBML_Constants.EMPTY)) {
			NodeTools.setClusterID(speciesNode, compartment);
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.COMPARTMENT, compartment);
		}
	}
	
	/**
	 * Sets the initial amount of a node
	 * 
	 * @param speciesNode
	 *        the initial amount belongs to this node
	 * @param initialAmount
	 *        the value that will be set
	 */
	public static void addSpeciesInitialAmount(Node speciesNode,
			Double initialAmount) {
		if (!initialAmount.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.INITIAL_AMOUNT, initialAmount);
		}
	}
	
	/**
	 * Sets the initial concentration of a node
	 * 
	 * @param speciesNode
	 *        the initial concentration belongs to this node
	 * @param initialConcentration
	 *        the value that will be set
	 */
	public static void addSpeciesInitialConcentration(Node speciesNode,
			Double initialConcentration) {
		if (!initialConcentration.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.INITIAL_CONCENTRATION, initialConcentration);
		}
	}
	
	/**
	 * Sets the substance units of a species node
	 * 
	 * @param speciesNode
	 *        the substance units belong to this node
	 * @param substanceUnits
	 *        the substance units to set
	 */
	public static void addSpeciesSubstanceUnits(Node speciesNode,
			String substanceUnits) {
		if (!substanceUnits.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_SUBSTANCE_UNITS, substanceUnits);
		}
	}
	
	/**
	 * Sets the attribute hasOnlySubstanceUnits of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param hasOnlySubstanceUnits
	 *        the value that will be set
	 */
	public static void addSpeciesHasOnlySubstanceUnits(Node speciesNode,
			Boolean hasOnlySubstanceUnits) {
		if (!hasOnlySubstanceUnits.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS,
					hasOnlySubstanceUnits);
		}
	}
	
	/**
	 * Sets the attribute boundary condition of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param boundaryCondition
	 *        the value that will be set
	 */
	public static void addSpeciesBoundaryConsition(Node speciesNode,
			Boolean boundaryCondition) {
		if (!boundaryCondition.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.BOUNDARY_CONDITION, boundaryCondition);
		}
	}
	
	/**
	 * Sets the attribute constant of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param constant
	 *        the value that will be set
	 */
	public static void addSpeciesConstant(Node speciesNode, Boolean constant) {
		if (!constant.equals(null)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONSTANT, constant);
		}
	}
	
	/**
	 * Sets the attribute conversion factor of a species node
	 * 
	 * @param speciesNode
	 *        where the attribute is going to be added
	 * @param conversionFactor
	 *        the value that will be set
	 */
	public static void addSpeciesConversionFactor(Node speciesNode,
			String conversionFactor) {
		if (!conversionFactor.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(speciesNode, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONVERSION_FACTOR, conversionFactor);
		}
	}
	
	public static void deleteSpeciesID(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_ID)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_ID);
		}
	}
	
	public static void deleteSpeciesName(Node node) {
		if (!AttributeHelper.getLabel(node, SBML_Constants.EMPTY)
				.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setLabel(node, "");
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_NAME);
		}
	}
	
	public static void deleteSpeciesCompartment(Node node) {
		if (!NodeTools.getClusterID(node, SBML_Constants.EMPTY).equals(
				SBML_Constants.EMPTY)) {
			NodeTools.setClusterID(node, "");
		}
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_COMPARTMENT_NAME)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_COMPARTMENT_NAME);
		}
	}
	
	public static void deleteSpeciesInitialAmount(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.INITIAL_AMOUNT)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.INITIAL_AMOUNT);
		}
	}
	
	public static void deleteSpeciesInitialConcentration(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.INITIAL_CONCENTRATION)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.INITIAL_CONCENTRATION);
		}
	}
	
	public static void deleteSpeciesSubstanceUnits(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_SUBSTANCE_UNITS)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_SUBSTANCE_UNITS);
		}
	}
	
	public static void deleteSpeciesHasOnlySubstanceUnits(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS);
		}
	}
	
	public static void deleteSpeciesBoundaryCondition(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.BOUNDARY_CONDITION)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.BOUNDARY_CONDITION);
		}
	}
	
	public static void deleteSpeciesConstant(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_CONSTANT)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONSTANT);
		}
	}
	
	public static void deleteSpeciesConversionFactor(Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SPECIES_CONVERSION_FACTOR)) {
			AttributeHelper.deleteAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONVERSION_FACTOR);
		}
	}
	
	public static void deleteSpecies(Node node) {
		deleteSpeciesID(node);
		deleteSpeciesName(node);
		deleteSpeciesCompartment(node);
		deleteSpeciesInitialConcentration(node);
		deleteSpeciesInitialAmount(node);
		deleteSpeciesSubstanceUnits(node);
		deleteSpeciesHasOnlySubstanceUnits(node);
		deleteSpeciesBoundaryCondition(node);
		deleteSpeciesConstant(node);
		deleteSpeciesConversionFactor(node);
		Graph g = node.getGraph();
		g.deleteNode(node);
	}
	
	/**
	 * Returns all species nodes of the graph
	 * 
	 * @return a list of all species nodes of the graph
	 */
	public static List<Node> getSpeciesNodes(Graph g) {
		List<Node> speciesNodeList = new ArrayList<Node>();
		Iterator<Node> itNode = g.getNodesIterator();
		while (itNode.hasNext()) {
			Node node = itNode.next();
			if (AttributeHelper.getSBMLrole(node).equals("species")) {
				speciesNodeList.add(node);
			}
		}
		return speciesNodeList;
	}
	
	/**
	 * Returns the species node with a distinct id
	 * 
	 * @param id
	 *        the id of the asked node
	 * @return the node with a certain id or null if no node has this id
	 */
	public static Node getSpeciesNode(Graph g, String id) {
		List<Node> nodeList = getSpeciesNodes(g);
		Iterator<Node> itNode = nodeList.iterator();
		Node currentNode = null;
		while (itNode.hasNext()) {
			currentNode = itNode.next();
			if (currentNode.equals(id)) {
				return currentNode;
			}
		}
		return null;
	}
	
	/**
	 * Returns a JSBML Species object belonging to a species node
	 * 
	 * @param node
	 *        the species node
	 * @return a JSBML Species object
	 */
	public static Species getSpecies(Node node) {
		if (AttributeHelper.getSBMLrole(node).equals("species")) {
			Species species = new Species();
			species.setId(getSpeciesID(node));
			species.setName(getSpeciesName(node));
			species.setCompartment(getSpeciesCompartment(node));
			if (isSpeciesInitialAmount(node)) {
				species.setInitialAmount(getSpeciesInitialAmount(node));
			}
			if (isSpeciesInitialConcentration(node)) {
				species.setInitialConcentration(getSpeciesInitialConcentration(node));
			}
			species.setSubstanceUnits(getSpeciesSubstanceUnits(node));
			if (isSpeciesHasOnlySubstanceUnits(node)) {
				species.setHasOnlySubstanceUnits(getSpeciesHasOnlySubstanceUnits(node));
			}
			if (isSpeciesBoundaryCondition(node)) {
				species.setBoundaryCondition(getSpeciesBoundaryCondition(node));
			}
			if (isSpeciesConstant(node)) {
				species.setConstant(getSpeciesConstant(node));
			}
			if (isSpeciesConversionFactor(node)) {
				species.setConversionFactor(getSpeciesConversionFactor(node));
			}
			return species;
		} else {
			return null;
		}
	}
	
	/**
	 * Creates a new species node and sets its id
	 * 
	 * @param g
	 *        the node is added to this graph
	 * @param id
	 *        the value of the id that will be set
	 * @return a node or null if the id is the empty string
	 */
	public static Node createSpecies(Graph g, String id, String name) {
		if (!SBML_Constants.EMPTY.equals(id) || !SBML_Constants.EMPTY.equals(name)) {
			
			Node node = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(new Vector2d(pgg.getNextPosition())));
			// AttributeHelper.setDefaultGraphicsAttribute(node, pgg.getNextPosition());
			AttributeHelper.setLabel(node, id);
			AttributeHelper.setSize(node, id.length() * id.length() + 7, 20d);
			AttributeHelper.setFillColor(node, Color.WHITE);
			AttributeHelper.setShapeEllipse(node);
			AttributeHelper.setBorderWidth(node, 1);
			
			AttributeHelper.setSBMLrole(node, SBML_Constants.ROLE_SPECIES);
			addSpeciesID(node, id);
			if (!name.equals(SBML_Constants.EMPTY)) {
				addSpeciesName(node, name);
			}
			if (SBMLSpeciesHelper.speciesMap == null) {
				SBMLSpeciesHelper.speciesMap = new HashMap<String, Node>();
			}
			SBMLSpeciesHelper.speciesMap.put(id, node);
			return node;
		}
		else {
			return null;
		}
	}
	
	/**
	 * This method converts a node into a species node
	 * 
	 * @param n the node that will be transformed
	 * @return the species node that will be returned
	 */
	public static Node initSpeciesNode(Node n) {
		String label = AttributeHelper.getLabel(n, SBML_Constants.EMPTY);
		if (label == SBML_Constants.EMPTY) {
			return null;
		}
		else {
			addSpeciesName(n, label);
			AttributeHelper.setShape(n, "circle");
			AttributeHelper.setSBMLrole(n, "species");
			return n;
		}
	}
	
	public static Node createSpecies(Graph g, Species species) {
		Node node = g.addNode();
		AttributeHelper.setSBMLrole(node, SBML_Constants.ROLE_SPECIES);
		String label = null;
		if (!species.getName().equals(SBML_Constants.EMPTY)) {
			label = species.getName();
		} else {
			label = species.getId();
		}
		if (SBMLSpeciesHelper.speciesMap == null) {
			SBMLSpeciesHelper.speciesMap = new HashMap<String, Node>();
		}
		SBMLSpeciesHelper.speciesMap.put(species.getId(), node);
		if (!label.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setSize(node, label.length() * label.length() + 7, 20d);
			AttributeHelper.setDefaultGraphicsAttribute(node, pgg.getNextPosition());
			AttributeHelper.setLabel(node, label);
			AttributeHelper.setFillColor(node, Color.WHITE);
			AttributeHelper.setShapeEllipse(node);
			AttributeHelper.setBorderWidth(node, 1);
		}
		if (species.isSetId()) {
			SBMLSpeciesHelper.speciesMap.put(species.getId(), node);
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_ID, species.getId());
			AttributeHelper.setLabel(AttributeHelper.getLabels(node).size(),
					node, species.getId(), null, AlignmentSetting.HIDDEN.toGMLstring());
		}
		if (species.isSetCompartment()) {
			NodeTools.setClusterID(node, species.getCompartment());
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.COMPARTMENT, node);
		}
		if (species.isSetInitialConcentration()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.INITIAL_CONCENTRATION, species.getInitialConcentration());
		}
		if (species.isSetInitialAmount()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.INITIAL_AMOUNT, species.getInitialAmount());
		}
		if (species.isSetSubstanceUnits()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_SUBSTANCE_UNITS, species.getSubstanceUnits());
		}
		if (species.isSetHasOnlySubstanceUnits()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS,
					species.getHasOnlySubstanceUnits());
		}
		if (species.isSetBoundaryCondition()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.BOUNDARY_CONDITION, species.getBoundaryCondition());
		}
		if (species.isSetConstant()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONSTANT, species.getConstant());
		}
		if (species.isSetConversionFactor()) {
			AttributeHelper.setAttribute(node, SBML_Constants.SBML,
					SBML_Constants.SPECIES_CONVERSION_FACTOR, species.getConversionFactor());
		}
		
		return node;
	}
	
	public static Parameter createParameter(Graph g, String id) {
		String presentedHeadline = new StringBuffer("SBML Parameter ")
				.append(parameterCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_PARAMETER).append(parameterCount).toString();
		setCompartmentID(g, internHeadline, id);
		Parameter para = new Parameter();
		para.setId(id);
		initParameterNideIDs(internHeadline, presentedHeadline);
		++parameterCount;
		return para;
	}
	
	public static void createParameter(Graph g, Parameter parameter) {
		String presentedHeadline = new StringBuffer("SBML Parameter ")
				.append(parameterCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_PARAMETER).append(parameterCount).toString();
		initParameterNideIDs(internHeadline, presentedHeadline);
		
		if (parameter.isSetId()) {
			setParameterID(g, internHeadline, parameter.getId());
		}
		if (parameter.isSetName()) {
			setParameterName(g, internHeadline, parameter.getName());
		}
		if (parameter.isSetValue()) {
			setParameterValue(g, internHeadline, parameter.getValue());
		}
		if (parameter.isSetUnits()) {
			setParameterUnits(g, internHeadline, parameter.getUnits());
		}
		if (parameter.isSetConstant()) {
			setParameterConstant(g, internHeadline, parameter.isConstant());
		}
		++parameterCount;
	}
	
	public static void addParameterName(Graph g, String id, String name) {
		setParameterName(g, returnParameterHeadlineWithID(g, id), name);
	}
	
	public static void addParameterValue(Graph g, String id, Double value) {
		setParameterValue(g, returnParameterHeadlineWithID(g, id), value);
	}
	
	public static void addParameterUnits(Graph g, String id, String units) {
		setParameterUnits(g, returnParameterHeadlineWithID(g, id), units);
	}
	
	public static void addParameterConstant(Graph g, String id, boolean constant) {
		setParameterConstant(g, returnParameterHeadlineWithID(g, id), constant);
	}
	
	public static void deleteParameterName(Graph g, String id) {
		String headline = returnParameterHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_NAME).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_NAME).toString());
		}
	}
	
	public static void deleteParameterID(Graph g, String id) {
		String headline = returnParameterHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_ID).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_ID).toString());
		}
	}
	
	public static void deleteParameterValue(Graph g, String id) {
		String headline = returnParameterHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.VALUE).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.VALUE).toString());
		}
	}
	
	public static void deleteParameterUnits(Graph g, String id) {
		String headline = returnParameterHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_UNITS).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_UNITS).toString());
		}
	}
	
	public static void deleteParameterConstant(Graph g, String id) {
		String headline = returnParameterHeadlineWithID(g, id);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_CONSTANT).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.PARAMETER_CONSTANT).toString());
		}
	}
	
	public static void deleteParameter(Graph g, String id) {
		deleteParameterName(g, id);
		deleteParameterValue(g, id);
		deleteParameterUnits(g, id);
		deleteParameterConstant(g, id);
		deleteParameterID(g, id);
	}
	
	public static boolean isSetParameterName(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnParameterHeadlineWithID(g, id),
				new StringBuffer(returnParameterHeadlineWithID(g, id)).append(SBML_Constants.PARAMETER_NAME).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetParameterValue(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnParameterHeadlineWithID(g, id),
				new StringBuffer(returnParameterHeadlineWithID(g, id)).append(SBML_Constants.VALUE).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetParameterUnits(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnParameterHeadlineWithID(g, id),
				new StringBuffer(returnParameterHeadlineWithID(g, id)).append(SBML_Constants.PARAMETER_UNITS).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetParameterConstant(Graph g, String id) {
		if (AttributeHelper.hasAttribute(g, returnParameterHeadlineWithID(g, id),
				new StringBuffer(returnParameterHeadlineWithID(g, id)).append(SBML_Constants.PARAMETER_CONSTANT).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Indicates if the parameter id is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return true if the value is set.
	 */
	private static Boolean isParameterID(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PARAMETER_ID).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the parameter name is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return true if the value is set.
	 */
	private static Boolean isParameterName(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PARAMETER_NAME)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the parameter value is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return true if the value is set.
	 */
	private static Boolean isParameterValue(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.VALUE).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the parameter units is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return true if the value is set.
	 */
	private static Boolean isParameterUnits(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PARAMETER_UNITS)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the attribute constant is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return true if the value is set.
	 */
	private static Boolean isParameterConstant(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PARAMETER_CONSTANT)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the parameter id if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return the id if it is set. Else the empty string
	 */
	private static String getParameterID(Graph g, String internHeadline) {
		if (isParameterID(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PARAMETER_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the parameter name if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return the name if it is set. Else the empty string
	 */
	private static String getParameterName(Graph g, String internHeadline) {
		if (isParameterName(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PARAMETER_NAME).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the parameter value if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return the value if it is set. Else null
	 */
	private static Double getParameterValue(Graph g, String internHeadline) {
		if (isParameterValue(g, internHeadline)) {
			return (Double) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.VALUE).toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the parameter units if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return the units if it is set. Else the empty string
	 */
	private static String getParameterUnits(Graph g, String internHeadline) {
		if (isParameterUnits(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PARAMETER_UNITS).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the attribute constant if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return constant if it is set. Else null
	 */
	private static Boolean getParameterConstant(Graph g, String internHeadline) {
		if (isParameterConstant(g, internHeadline)) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PARAMETER_CONSTANT).toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the id of a parameter
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @param ID
	 *        the value that will be read in
	 */
	private static void setParameterID(Graph g, String internHeadline, String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PARAMETER_ID)
					.toString(), ID);
		}
	}
	
	/**
	 * Sets the name of a parameter
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @param name
	 *        the value that will be read in
	 */
	private static void setParameterName(Graph g, String internHeadline,
			String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PARAMETER_NAME)
					.toString(), name);
		}
	}
	
	/**
	 * Sets the value of a parameter
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @param value
	 *        the value that will be read in
	 */
	private static void setParameterValue(Graph g, String internHeadline,
			Double value) {
		if (!value.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.VALUE).toString(),
					value);
		}
	}
	
	/**
	 * Sets the units of a parameter
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @param units
	 *        the value that will be read in
	 */
	private static void setParameterUnits(Graph g, String internHeadline,
			String units) {
		if (!units.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PARAMETER_UNITS)
					.toString(), units);
		}
	}
	
	/**
	 * Sets the attribute constant of a parameter
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @param constant
	 *        the value that will be read in
	 */
	private static void setParameterConstant(Graph g, String internHeadline,
			Boolean constant) {
		if (!constant.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PARAMETER_CONSTANT)
					.toString(), constant);
		}
	}
	
	/**
	 * Returns a list of all parameter headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getParameterHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_PARAMETER);
	}
	
	/**
	 * Returns a filled JSBML Parameter object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current parameter
	 * @return a filled Parameter object
	 */
	private static Parameter getParameter(Graph g, String internHeadline) {
		Parameter parameter = new Parameter();
		parameter.setId(getParameterID(g, internHeadline));
		parameter.setName(getParameterName(g, internHeadline));
		if (isParameterValue(g, internHeadline)) {
			parameter.setValue(getParameterValue(g, internHeadline));
		}
		parameter.setUnits(getParameterUnits(g, internHeadline));
		if (isParameterConstant(g, internHeadline)) {
			parameter.setConstant(getParameterConstant(g, internHeadline));
		}
		return parameter;
	}
	
	/**
	 * Returns a list of parameter with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current parameter
	 * @return a filled list of parameters
	 */
	private static List<Parameter> getAllParameters(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Parameter> parameterList = new ArrayList<Parameter>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			parameterList.add(getParameter(g, internHeadline));
		}
		return parameterList;
	}
	
	/**
	 * Returns a list of parameter with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of parameters
	 */
	public static List<Parameter> getAllParameters(Graph g) {
		ArrayList<String> internHeadlines = getParameterHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Parameter> parameterList = new ArrayList<Parameter>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			parameterList.add(getParameter(g, internHeadline));
		}
		return parameterList;
	}
	
	public static InitialAssignment createInitialAssignment(Graph g, String symbol) {
		String presentedHeadline = new StringBuffer("SBML Initial Assignment ").append(initialAssignmentCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_INITIAL_ASSIGNMENT).append(initialAssignmentCount).toString();
		setInitialAssignmentSymbol(g, internHeadline, symbol);
		InitialAssignment ia = new InitialAssignment();
		ia.setLevel(3);
		ia.setVariable(symbol);
		initInitialAssignmentNiceIDs(internHeadline, presentedHeadline);
		++initialAssignmentCount;
		return ia;
	}
	
	public static void createInitialAssignment(Graph g, InitialAssignment ia) {
		String presentedHeadline = new StringBuffer("SBML Initial Assignment ").append(initialAssignmentCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_INITIAL_ASSIGNMENT).append(initialAssignmentCount).toString();
		initInitialAssignmentNiceIDs(internHeadline, presentedHeadline);
		
		if (ia.isSetSymbol()) {
			setInitialAssignmentSymbol(g, internHeadline, ia.getVariable());
		}
		if (!ia.isSetLevel()) {
			ia.setLevel(3);
		}
		String formula = "";
		try {
			if (ia.isSetMath()) {
				ASTNode mathTree = ia.getMath();
				formula = mathTree.toFormula();
				setInitialAssignmentFunction(g, internHeadline, formula);
			}
		} catch (SBMLException e) {
		}
		++initialAssignmentCount;
		
	}
	
	public static void addInitialAssignmentFunction(Graph g, String symbol, String function) {
		setInitialAssignmentFunction(g, returnInitialAssignmentWithSymbol(g, symbol), function);
	}
	
	public static void deleteInitialAssignmentSymbol(Graph g, String symbol) {
		String headline = returnInitialAssignmentWithSymbol(g, symbol);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.SYMBOL).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.SYMBOL).toString());
		}
	}
	
	public static void deleteInitialAssignmentFunction(Graph g, String symbol) {
		String headline = returnInitialAssignmentWithSymbol(g, symbol);
		if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION).toString())) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION).toString());
		}
	}
	
	public static void deleteInitialAssignment(Graph g, String symbol) {
		deleteInitialAssignmentFunction(g, symbol);
		deleteInitialAssignmentSymbol(g, symbol);;
	}
	
	public static boolean isSetInitialAssignmentFunction(Graph g, String symbol) {
		if (AttributeHelper.hasAttribute(g, returnInitialAssignmentWithSymbol(g, symbol),
				new StringBuffer(returnInitialAssignmentWithSymbol(g, symbol)).append(SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetInitialAssignmentSymbol(Graph g, String symbol) {
		if (AttributeHelper.hasAttribute(g, returnInitialAssignmentWithSymbol(g, symbol),
				new StringBuffer(returnInitialAssignmentWithSymbol(g, symbol)).append(SBML_Constants.SYMBOL).toString())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Indicates if the attribute symbol is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @return true if the value is set.
	 */
	private static Boolean isInitialAssignmentSymbol(Graph g,
			String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.SYMBOL).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @return true if the value is set.
	 */
	private static Boolean isInitialAssignmentFunction(Graph g,
			String internHeadline) {
		if (AttributeHelper.hasAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline).append(
						SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Sets the symbol of a initial assignment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @param symbol
	 *        the value that will be read in
	 */
	private static void setInitialAssignmentSymbol(Graph g,
			String internHeadline, String symbol) {
		if (!symbol.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.SYMBOL).toString(),
					symbol);
		}
	}
	
	/**
	 * Sets the function of a initial assignment
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @param function
	 *        the value that will be read in
	 */
	private static void setInitialAssignmentFunction(Graph g,
			String internHeadline, String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION)
							.toString(), function);
		}
	}
	
	/**
	 * Returns the initial assignment symbol if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @return the symbol if it is set. Else the empty string
	 */
	private static String getInitialAssignmentSymbol(Graph g,
			String internHeadline) {
		if (isInitialAssignmentSymbol(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.SYMBOL).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the initial assignment function if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @return the function if it is set. Else the empty string
	 */
	private static String getInitialAssignmentFunction(Graph g,
			String internHeadline) {
		if (isInitialAssignmentFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns a list of all initial assignment headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getInitialAssignmentHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_INITIAL_ASSIGNMENT);
	}
	
	/**
	 * Returns a filled JSBML initial assignment object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current initial assignment
	 * @return a filled initial assignment object
	 */
	private static InitialAssignment getInitialAssignment(Graph g,
			String internHeadline) {
		InitialAssignment ia = new InitialAssignment();
		ia.setLevel(3);
		if (isInitialAssignmentSymbol(g, internHeadline)) {
			ia.setVariable(getInitialAssignmentSymbol(g, internHeadline));
		}
		if (isInitialAssignmentFunction(g, internHeadline)) {
			try {
				ia.setFormula(getInitialAssignmentFunction(g, internHeadline));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return ia;
	}
	
	/**
	 * Returns a list of initial assignment with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current initial
	 *        assignment
	 * @return a filled list of initial assignment
	 */
	private static List<InitialAssignment> getAllInitialAssignment(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<InitialAssignment> iaList = new ArrayList<InitialAssignment>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			iaList.add(getInitialAssignment(g, internHeadline));
		}
		return iaList;
	}
	
	/**
	 * Returns a list of initial assignment with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of initial assignment
	 */
	public static List<InitialAssignment> getAllInitialAssignment(Graph g) {
		List<String> internHeadlines = getInitialAssignmentHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<InitialAssignment> iaList = new ArrayList<InitialAssignment>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			iaList.add(getInitialAssignment(g, internHeadline));
		}
		return iaList;
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @return true if the value is set.
	 */
	private static Boolean isRateRuleFunction(Graph g, String internHeadline) {
		if (AttributeHelper
				.hasAttribute(g, internHeadline, new StringBuffer(
						internHeadline).append(SBML_Constants.RATE_FUNCTION)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the variable is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @return true if the value is set.
	 */
	private static Boolean isRateRuleVariable(Graph g, String internHeadline) {
		if (AttributeHelper
				.hasAttribute(g, internHeadline, new StringBuffer(
						internHeadline).append(SBML_Constants.RATE_VARIABLE)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the rate rule variable if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @return the variable if it is set. Else the empty string
	 */
	private static String getRateRuleVariable(Graph g, String internHeadline) {
		if (isRateRuleVariable(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.RATE_VARIABLE).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the rate rule function if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @return the function if it is set. Else the empty string
	 */
	private static String getRateRuleFunction(Graph g, String internHeadline) {
		if (isRateRuleFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.RATE_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Sets the function of a rate rule
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @param function
	 *        the value that will be read in
	 */
	private static void setRateRuleFunction(Graph g, String internHeadline,
			String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.RATE_FUNCTION)
					.toString(), function);
		}
	}
	
	/**
	 * Sets the variable of a rate rule
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @param variable
	 *        the value that will be read in
	 */
	private static void setRateRuleVariable(Graph g, String internHeadline,
			String variable) {
		if (!variable.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.RATE_VARIABLE)
					.toString(), variable);
		}
	}
	
	/**
	 * Returns a list of all rate rule headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getRateRuleHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_RATE_RULE);
	}
	
	/**
	 * Returns a filled JSBML rate rule object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current rate rule
	 * @return a filled rate rule object
	 */
	private static RateRule getRateRule(Graph g, String internHeadline) {
		RateRule rr = new RateRule();
		if (isRateRuleVariable(g, internHeadline)) {
			rr.setVariable(getRateRuleVariable(g, internHeadline));
		}
		if (isRateRuleFunction(g, internHeadline)) {
			try {
				rr.setFormula(getRateRuleFunction(g, internHeadline));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return rr;
	}
	
	/**
	 * Returns a list of rate rules with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current rate rule
	 * @return a filled list of rate rules
	 */
	private static List<RateRule> getAllRateRules(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<RateRule> rrList = new ArrayList<RateRule>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			rrList.add(getRateRule(g, internHeadline));
		}
		return rrList;
	}
	
	/**
	 * Returns a list of rate rules with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of rate rules
	 */
	private static List<RateRule> getAllRateRules(Graph g) {
		List<String> internHeadlines = getRateRuleHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<RateRule> rrList = new ArrayList<RateRule>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			rrList.add(getRateRule(g, internHeadline));
		}
		return rrList;
	}
	
	public static void deleteAssignmentRule(Graph g, String variable) {
		deleteAssignmentRuleFunction(g, variable);
		deleteAssignmentRuleVariable(g, variable);
	}
	
	public static void deleteRateRule(Graph g, String variable) {
		deleteRateRuleFunction(g, variable);
		deleteRateRuleVariable(g, variable);
	}
	
	public static void deleteAlgebraicRule(Graph g, String formula) {
		deleteAlgebraicRuleFunction(g, formula);
	}
	
	public static AssignmentRule createAssignmentRule(Graph g, String variable) {
		String presentedHeadline = new StringBuffer("SBML Assignment Rule ").append(assignmentRuleCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_ASSIGNMENT_RULE).append(assignmentRuleCount).toString();
		setAssignmentRuleVariable(g, internHeadline, variable);
		AssignmentRule ar = new AssignmentRule();
		ar.setLevel(3);
		ar.setVariable(variable);
		initAssignmnetRuleNiceIDs(internHeadline, presentedHeadline);
		++assignmentRuleCount;
		return ar;
	}
	
	public static RateRule createRateRule(Graph g, String variable) {
		String presentedHeadline = new StringBuffer("SBML Rate Rule ").append(rateRuleCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_RATE_RULE).append(rateRuleCount).toString();
		setRateRuleVariable(g, internHeadline, variable);
		RateRule rr = new RateRule();
		rr.setLevel(3);
		rr.setVariable(variable);
		initRateRuleNiceIDs(internHeadline, presentedHeadline);
		++rateRuleCount;
		return rr;
	}
	
	public static AlgebraicRule createAlgebraicRule(Graph g, String formula) {
		String internHeadline = new StringBuffer(SBML_Constants.SBML_ALGEBRAIC_RULE).append(algebraicRuleCount).toString();
		String presentedHeadline = new StringBuffer("SBML Algebraic Rule ").append(algebraicRuleCount).toString();
		setAlgebraicRuleFunction(g, internHeadline, formula);
		AlgebraicRule ar = new AlgebraicRule();
		ar.setLevel(3);
		try {
			ar.setMath(ASTNode
					.parseFormula(formula));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		initAlgebraicRuleNiceIDs(internHeadline, presentedHeadline);
		++algebraicRuleCount;
		return ar;
	}
	
	public static AssignmentRule createAssignmentRule(Graph g, String variable, String formula) {
		String presentedHeadline = new StringBuffer("SBML Assignment Rule ").append(assignmentRuleCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_ASSIGNMENT_RULE).append(assignmentRuleCount).toString();
		setAssignmentRuleVariable(g, internHeadline, variable);
		setAssignmentRuleFunction(g, internHeadline, formula);
		AssignmentRule ar = new AssignmentRule();
		ar.setLevel(3);
		ar.setVariable(variable);
		try {
			ar.setMath(ASTNode
					.parseFormula(formula));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		initAssignmnetRuleNiceIDs(internHeadline, presentedHeadline);
		++assignmentRuleCount;
		return ar;
	}
	
	public static RateRule createRateRule(Graph g, String variable, String formula) {
		String presentedHeadline = new StringBuffer("SBML Rate Rule ").append(rateRuleCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_RATE_RULE).append(rateRuleCount).toString();
		setRateRuleVariable(g, internHeadline, variable);
		setRateRuleFunction(g, internHeadline, formula);
		RateRule rr = new RateRule();
		rr.setLevel(3);
		rr.setVariable(variable);
		try {
			rr.setMath(ASTNode.parseFormula(formula));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		initRateRuleNiceIDs(internHeadline, presentedHeadline);
		++rateRuleCount;
		return rr;
	}
	
	public static void createAssignmentRule(Graph g, AssignmentRule ar) {
		String presentedHeadline = new StringBuffer("SBML Assignment Rule ").append(assignmentRuleCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_ASSIGNMENT_RULE).append(assignmentRuleCount).toString();
		initAssignmnetRuleNiceIDs(internHeadline, presentedHeadline);
		ar.setLevel(3);
		if (ar.isSetMath()) {
			String math = "";
			try {
				if (null != ar.getMath()) {
					math = ar.getMath().toFormula();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			setAssignmentRuleFunction(g, internHeadline, math);
		}
		if (ar.isSetVariable()) {
			setAssignmentRuleVariable(g, internHeadline, ar.getVariable());
		}
		++assignmentRuleCount;
	}
	
	public static void createRateRule(Graph g, RateRule rr) {
		String presentedHeadline = new StringBuffer("SBML Rate Rule ").append(rateRuleCount).toString();
		String internHeadline = new StringBuffer(SBML_Constants.SBML_RATE_RULE).append(rateRuleCount).toString();
		initRateRuleNiceIDs(internHeadline, presentedHeadline);
		rr.setLevel(3);
		if (rr.isSetMath()) {
			String math = "";
			try {
				if (null != rr.getMath()) {
					math = rr.getMath().toFormula();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			setRateRuleFunction(g, internHeadline, math);
		}
		if (rr.isSetVariable()) {
			setRateRuleVariable(g, internHeadline, rr.getVariable());
		}
		++rateRuleCount;
	}
	
	public static void createAlgebraicRule(Graph g, AlgebraicRule ar) {
		String internHeadline = new StringBuffer(SBML_Constants.SBML_ALGEBRAIC_RULE).append(algebraicRuleCount).toString();
		String presentedHeadline = new StringBuffer("SBML Algebraic Rule ").append(algebraicRuleCount).toString();
		initAlgebraicRuleNiceIDs(internHeadline, presentedHeadline);
		ar.setLevel(3);
		if (ar.isSetMath()) {
			String math = "";
			try {
				if (null != ar.getMath()) {
					math = ar.getMath().toFormula();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			setAlgebraicRuleFunction(g, internHeadline, math);
		}
		++algebraicRuleCount;
	}
	
	public static boolean isSetAssignmentRuleFunction(Graph g, String variable) {
		if (isAssignmentRuleFunction(g, returnAssignmentRuleWithID(g, variable))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetAssignmentRuleVariable(Graph g, String variable) {
		if (isAssignmentRuleVariable(g, returnAssignmentRuleWithID(g, variable))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetRateRuleFunction(Graph g, String variable) {
		if (isRateRuleFunction(g, returnRateRuleWithID(g, variable))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetRateRuleVariable(Graph g, String variable) {
		if (isRateRuleVariable(g, returnRateRuleWithID(g, variable))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void deleteAssignmentRuleVariable(Graph g, String variable) {
		String headline = returnAssignmentRuleWithID(g, variable);
		if (isAssignmentRuleVariable(g, headline)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.ASSIGNMENT_VARIABLE).toString());
		}
	}
	
	public static void deleteAssignmentRuleFunction(Graph g, String variable) {
		String headline = returnAssignmentRuleWithID(g, variable);
		if (isAssignmentRuleFunction(g, headline)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.ASSIGNMENT_FUNCTION).toString());
		}
	}
	
	public static void deleteRateRuleVariable(Graph g, String variable) {
		String headline = returnRateRuleWithID(g, variable);
		if (isRateRuleVariable(g, headline)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.RATE_VARIABLE).toString());
		}
	}
	
	public static void deleteRateRuleFunction(Graph g, String variable) {
		String headline = returnRateRuleWithID(g, variable);
		if (isRateRuleFunction(g, headline)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.RATE_FUNCTION).toString());
		}
	}
	
	public static void deleteAlgebraicRuleFunction(Graph g, String function) {
		String headline = returnAlgebraicRuleWithFormula(g, function);
		if (isAlgebraicRuleFunction(g, headline)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.ALGEBRAIC_FUNCTION).toString());
		}
	}
	
	public static void addAssignmentRuleFunction(Graph g, String variable, String function) {
		setAssignmentRuleFunction(g, returnAssignmentRuleWithID(g, variable), function);
	}
	
	public static void addAssignmentRuleVariable(Graph g, String variable) {
		createAssignmentRule(g, variable);
	}
	
	public static void addRateRuleFunction(Graph g, String variable, String function) {
		setRateRuleFunction(g, returnRateRuleWithID(g, variable), function);
	}
	
	public static void addRateRuleVariable(Graph g, String variable) {
		createRateRule(g, variable);
	}
	
	public static void addAlgebraicRule(Graph g, String function) {
		createAlgebraicRule(g, function);
	}
	
	/**
	 * Indicates if the variable is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @return true if the value is set.
	 */
	private static Boolean isAssignmentRuleVariable(Graph g,
			String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.ASSIGNMENT_VARIABLE)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @return true if the value is set.
	 */
	private static Boolean isAssignmentRuleFunction(Graph g,
			String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.ASSIGNMENT_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the assignment rule variable if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @return the variable if it is set. Else the empty string
	 */
	private static String getAssignmentRuleVariable(Graph g,
			String internHeadline) {
		if (isAssignmentRuleVariable(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.ASSIGNMENT_VARIABLE).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the assignment rule function if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @return the function if it is set. Else the empty string
	 */
	private static String getAssignmentRuleFunction(Graph g,
			String internHeadline) {
		if (isAssignmentRuleFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.ASSIGNMENT_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Sets the function of a assignment rule
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @param function
	 *        the value that will be read in
	 */
	private static void setAssignmentRuleFunction(Graph g,
			String internHeadline, String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.ASSIGNMENT_FUNCTION)
					.toString(), function);
		}
	}
	
	/**
	 * Sets the variable of a assignment rule
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @param variable
	 *        the value that will be read in
	 */
	private static void setAssignmentRuleVariable(Graph g,
			String internHeadline, String variable) {
		if (!variable.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.ASSIGNMENT_VARIABLE)
					.toString(), variable);
		}
	}
	
	/**
	 * Returns a list of all assignment rule headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	public static ArrayList<String> getAssignmentRuleHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_ASSIGNMENT_RULE);
	}
	
	/**
	 * Returns a filled JSBML assignment rule object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current assignment rule
	 * @return a filled assignment rule object
	 */
	private static AssignmentRule getAssignmentRule(Graph g,
			String internHeadline) {
		AssignmentRule ar = new AssignmentRule();
		if (isAssignmentRuleVariable(g, internHeadline)) {
			ar.setVariable(getAssignmentRuleVariable(g, internHeadline));
		}
		if (isAssignmentRuleFunction(g, internHeadline)) {
			try {
				ar.setFormula(getAssignmentRuleFunction(g, internHeadline));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return ar;
	}
	
	/**
	 * Returns a list of assignment rules with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current assignment
	 *        rule
	 * @return a filled list of assignment rules
	 */
	private static List<AssignmentRule> getAllAssignmentRules(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<AssignmentRule> arList = new ArrayList<AssignmentRule>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			arList.add(getAssignmentRule(g, internHeadline));
		}
		return arList;
	}
	
	/**
	 * Returns a list of assignment rules with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current assignment
	 *        rule
	 * @return a filled list of assignment rules
	 */
	public static List<AssignmentRule> getAllAssignmentRules(Graph g) {
		List<String> internHeadlines = getAssignmentRuleHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<AssignmentRule> arList = new ArrayList<AssignmentRule>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			arList.add(getAssignmentRule(g, internHeadline));
		}
		return arList;
	}
	
	/**
	 * Sets the function of a algebraic rule
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current algebraic rule
	 * @param function
	 *        the value that will be read in
	 */
	private static void setAlgebraicRuleFunction(Graph g, String internHeadline,
			String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.ALGEBRAIC_FUNCTION)
					.toString(), function);
		}
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current algebraic rule
	 * @return true if the value is set.
	 */
	private static Boolean isAlgebraicRuleFunction(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.FUNCTION).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the algebraic rule function if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current algebraic rule
	 * @return the function if it is set. Else the empty string
	 */
	private static String getAlgebraicRuleFunction(Graph g, String internHeadline) {
		if (isAlgebraicRuleFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns a list of all algebraic rule headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getAlgebraicRuleHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_ALGEBRAIC_RULE);
	}
	
	/**
	 * Returns a filled JSBML algebraic rule object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current algebraic rule
	 * @return a filled algebraic rule object
	 */
	private static AlgebraicRule getAlgebraicRule(Graph g, String internHeadline) {
		AlgebraicRule ar = new AlgebraicRule();
		if (isAlgebraicRuleFunction(g, internHeadline)) {
			try {
				ar.setFormula(getAlgebraicRuleFunction(g, internHeadline));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return ar;
	}
	
	/**
	 * Returns a list of algebraic rules with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current algebraic rule
	 * @return a filled list of algebraic rules
	 */
	private static List<AlgebraicRule> getAllAlgebraicRules(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<AlgebraicRule> arList = new ArrayList<AlgebraicRule>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			arList.add(getAlgebraicRule(g, internHeadline));
		}
		return arList;
	}
	
	/**
	 * Returns a list of algebraic rules with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of algebraic rules
	 */
	private static List<AlgebraicRule> getAllAlgebraicRules(Graph g) {
		List<String> internHeadlines = getAlgebraicRuleHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<AlgebraicRule> arList = new ArrayList<AlgebraicRule>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			arList.add(getAlgebraicRule(g, internHeadline));
		}
		return arList;
	}
	
	public static Constraint createConstraint(Graph g, String math) {
		String internHeadline = new StringBuffer(
				SBML_Constants.SBML_CONSTRAINT).append(constraintCount)
				.toString();
		String presentedHeadline = new StringBuffer("SBML Constraint ")
				.append(constraintCount).toString();
		initConstraintNiceIDs(internHeadline, presentedHeadline);
		setConstraintFunction(g, internHeadline, math);
		Constraint constraint = new Constraint();
		try {
			constraint.setMath(ASTNode.parseFormula(math));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		++constraintCount;
		return constraint;
	}
	
	public static void createConstraint(Graph g, Constraint constraint) {
		String internHeadline = new StringBuffer(
				SBML_Constants.SBML_CONSTRAINT).append(constraintCount)
				.toString();
		String presentedHeadline = new StringBuffer("SBML Constraint ")
				.append(constraintCount).toString();
		initConstraintNiceIDs(internHeadline, presentedHeadline);
		if (constraint.isSetMath()) {
			setConstraintFunction(g, internHeadline, constraint.getMath().toString());
		}
		if (constraint.isSetMessage()) {
			String message = removeTagFromString(constraint.getMessageString());
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.MESSAGE).toString(),
					message);
		}
		++constraintCount;
	}
	
	public static void deleteConstraint(Graph g, String formula) {
		ArrayList<String> headlines = getConstraintHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Constraint constraint = getConstraint(g, headline);
			if (constraint.getMath().toString().trim().equals(formula.trim())) {
				AttributeHelper.deleteAttribute(g, headline, new StringBuffer(headline).append(SBML_Constants.CONSTRAINT).toString());
				if (isConstraintMessage(g, headline)) {
					AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
							headline).append(SBML_Constants.MESSAGE).toString());
				}
			}
		}
	}
	
	public static void addConstraintMessage(Graph g, String formula, String message) {
		ArrayList<String> headlines = getConstraintHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Constraint constraint = getConstraint(g, headline);
			if (constraint.getMath().toString().trim().equals(formula.trim())) {
				AttributeHelper.setAttribute(g, headline, new StringBuffer(
						headline).append(SBML_Constants.MESSAGE).toString(), message);
			}
		}
	}
	
	public static void addConstraintConstraint(Graph g, String formula) {
		ArrayList<String> headlines = getConstraintHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Constraint constraint = getConstraint(g, headline);
			if (constraint.getMath().toString().trim().equals(formula.trim())) {
				AttributeHelper.setAttribute(g, headline, new StringBuffer(
						headline).append(SBML_Constants.CONSTRAINT)
						.toString(), formula);
			}
		}
	}
	
	public static void deleteConstraintMessage(Graph g, String constraint) {
		ArrayList<String> headlines = getConstraintHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Constraint con = getConstraint(g, headline);
			if (con.getMath().toString().trim().equals(constraint.trim())) {
				AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
						headline).append(SBML_Constants.MESSAGE).toString());
			}
		}
	}
	
	public static boolean isSetConstraintMessage(Graph g, String constraint) {
		ArrayList<String> headlines = getConstraintHeadlines(g);
		Iterator<String> it = headlines.iterator();
		while (it.hasNext()) {
			String headline = it.next();
			Constraint con = getConstraint(g, headline);
			if (con.getMath().toString().trim().equals(constraint.trim())) {
				if (AttributeHelper.hasAttribute(g, headline, new StringBuffer(
						headline).append(SBML_Constants.MESSAGE).toString())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static String removeTagFromString(String xhtml) {
		String content = xhtml.replace("\n", "").replace("\t", "").trim();
		// Replace anything between script or style tags
		// A regular expression to match anything in between <>
		// Reads as: Match a "<"
		// Match one or more characters that are not ">"
		// Match "<";
		String tagregex = "<[^>]*>";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(content);
		// Replace any matches with nothing
		content = m2.replaceAll("");
		return content.trim();
	}
	
	/**
	 * Sets the message of a constraint
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @param message
	 *        the value that will be read in
	 */
	private static void setConstraintMessage(Graph g, String internHeadline,
			String message) {
		if (!message.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.MESSAGE).toString(),
					message);
		}
	}
	
	/**
	 * Sets the function of a constraint
	 * 
	 * @param g
	 *        the graph where the information will be set
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @param function
	 *        the value that will be read in
	 */
	private static void setConstraintFunction(Graph g, String internHeadline,
			String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.CONSTRAINT)
					.toString(), function);
		}
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @return true if the value is set.
	 */
	private static Boolean isConstraintFunction(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.CONSTRAINT).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the message is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @return true if the value is set.
	 */
	private static Boolean isConstraintMessage(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.MESSAGE).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the constraint function if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @return the function if it is set. Else the empty string
	 */
	private static String getConstraintFunction(Graph g, String internHeadline) {
		if (isConstraintFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.CONSTRAINT).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the constraint message if it is set
	 * 
	 * @param g
	 *        the graph where the information is read from
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @return the message if it is set. Else the empty string
	 */
	private static String getConstraintMessage(Graph g, String internHeadline) {
		if (isConstraintMessage(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.MESSAGE).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns a list of all constraint headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getConstraintHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_CONSTRAINT);
	}
	
	/**
	 * Returns a filled JSBML constraint object
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        contains the number of the current constraint
	 * @return a filled constraint object
	 */
	private static Constraint getConstraint(Graph g, String internHeadline) {
		Constraint con = new Constraint();
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.MESSAGE).toString())) {
			String message = getConstraintMessage(g, internHeadline);
			String completeMessage = "<message><body xmlns=\"http://www.w3.org/1999/xhtml\"><p>"
					+ message + "</p></body></message>";
			con.setMessage(completeMessage);
		}
		try {
			con.setMath(ASTNode.parseFormula(getConstraintFunction(g, internHeadline)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return con;
	}
	
	/**
	 * Returns a list of constraint with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current constraint
	 * @return a filled list of constraint
	 */
	private static List<Constraint> getAllConstraints(Graph g,
			List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Constraint> conList = new ArrayList<Constraint>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			conList.add(getConstraint(g, internHeadline));
		}
		return conList;
	}
	
	/**
	 * Returns a list of constraint with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @return a filled list of constraint
	 */
	public static List<Constraint> getAllConstraints(Graph g) {
		List<String> internHeadlines = getCompartmentHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Constraint> conList = new ArrayList<Constraint>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			conList.add(getConstraint(g, internHeadline));
		}
		return conList;
	}
	
	/**
	 * Indicates if compartment of a reaction is set
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return true if compartment is set else false
	 */
	public static Boolean isReactionCompartment(Node reactionNode) {
		if (!NodeTools.getClusterID(reactionNode, SBML_Constants.EMPTY).equals(
				SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if fast of a reaction is set
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return true if fast is set else false
	 */
	public static Boolean isReactionFast(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.FAST)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if reversible of a reaction is set
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return true if reversible is set else false
	 */
	public static Boolean isReactionReversible(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE)) {
			
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if name of a reaction is set
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return true if name is set else false
	 */
	public static Boolean isReactionName(Node reactionNode) {
		if (!AttributeHelper.getLabel(reactionNode, SBML_Constants.EMPTY)
				.equals(SBML_Constants.EMPTY)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if id of a reaction is set
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return true if id is set else false
	 */
	public static Boolean isReactionID(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the id of a reaction
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return id if it is set else the empty string
	 */
	public static String getReactionID(Node reactionNode) {
		if (isReactionID(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REACTION_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the name of a reaction
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return name if it is set else the empty string
	 */
	public static String getReactionName(Node reactionNode) {
		return AttributeHelper.getLabel(reactionNode, SBML_Constants.EMPTY);
	}
	
	/**
	 * Returns the attribute reversible of a reaction
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return reversible if it is set else null
	 */
	public static Boolean getReactionReversible(Node reactionNode) {
		if (isReactionReversible(reactionNode)) {
			return (Boolean) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.REVERSIBLE);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the attribute fast of a reaction
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return fast if it is set else the empty string
	 */
	public static Boolean getReactionFast(Node reactionNode) {
		if (isReactionFast(reactionNode)) {
			return (Boolean) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML, SBML_Constants.FAST);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the compartment of a reaction
	 * 
	 * @param reactionNode
	 *        the node where the information is read from
	 * @return compartment if it is set else the empty string
	 */
	public static String getReactionCompartment(Node reactionNode) {
		return NodeTools.getClusterID(reactionNode, SBML_Constants.EMPTY);
	}
	
	/**
	 * Sets the attribute id of a node
	 * 
	 * @param reactionNode
	 *        the attribute belongs to this node
	 * @param id
	 *        the value that will be set
	 */
	public static void setReactionID(Node reactionNode, String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REACTION_ID, ID);
		}
	}
	
	public static void deleteReactionID(Node reactionNode) {
		AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_ID);
	}
	
	public static void setReactionName(Node reactionNode, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REACTION_NAME, name);
		}
	}
	
	public static void deleteReactionName(Node reactionNode) {
		AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REACTION_NAME);
	}
	
	/**
	 * Sets the attribute name of a node
	 * 
	 * @param reactionNode
	 *        the attribute belongs to this node
	 * @param name
	 *        the value that will be set
	 */
	public static void setReactionLabel(Node reactionNode, String name,
			String id, PositionGridGenerator pgg) {
		String label = null;
		if (!name.equals(SBML_Constants.EMPTY)) {
			label = name;
		} else {
			label = id;
		}
		if (!label.equals(SBML_Constants.EMPTY)) {
			attReader.setAttributes(reactionNode, Color.white, label,
					pgg.getNextPosition(), 7);
		}
	}
	
	/**
	 * Sets the attribute reversible of a node
	 * 
	 * @param reactionNode
	 *        the attribute belongs to this node
	 * @param reversible
	 *        the value that will be set
	 */
	public static void setReactionReversible(Node reactionNode,
			Boolean reversible) {
		if (!reversible.equals(null)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.REVERSIBLE, reversible);
		}
	}
	
	public static void deleteReactionReversible(Node reactionNode) {
		AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE);
	}
	
	/**
	 * Sets the attribute fast of a node
	 * 
	 * @param reactionNode
	 *        the attribute belongs to this node
	 * @param fast
	 *        the value that will be set
	 */
	public static void setReactionFast(Node reactionNode, Boolean fast) {
		if (!fast.equals(null)) {
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.FAST, fast);
		}
	}
	
	public static void deleteReactionFast(Node reactionNode) {
		AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.FAST);
	}
	
	/**
	 * Sets the compartment of a node
	 * 
	 * @param reactionNode
	 *        the compartment belongs to this node
	 * @param compartment
	 *        the id of the compartment that will be set
	 */
	public static void setReactionCompartment(Node reactionNode,
			String compartment) {
		if (!compartment.equals(SBML_Constants.EMPTY)) {
			;
			NodeTools.setClusterID(reactionNode, compartment);
			AttributeHelper.setAttribute(reactionNode, SBML_Constants.SBML,
					SBML_Constants.COMPARTMENT, compartment);
		}
	}
	
	public static void deleteReactionCompartment(Node reactionNode) {
		AttributeHelper.deleteAttribute(reactionNode, SBML_Constants.SBML,
				SBML_Constants.COMPARTMENT);
		NodeTools.setClusterID(reactionNode, "");
	}
	
	/**
	 * Sets the species of a reaction
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read in
	 * @param species
	 *        the value that will be read in
	 */
	public static void setReactionSpecies(Edge reactionEdge, String species) {
		if (!species.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SPECIES, species);
		}
	}
	
	/**
	 * The id of the species of an reaction
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read in
	 * @param ID
	 *        the value that will be read in
	 */
	public static void setReactionSpeciesID(Edge reactionEdge, String ID) {
		if (!reactionEdge.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SPECIES_REFERENCE_ID, ID);
		}
	}
	
	/**
	 * The name of the species of a reaction
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read in
	 * @param name
	 *        the value that will be read in
	 */
	public static void setReactionSpeciesName(Edge reactionEdge, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SPECIES_REFERENCE_NAME, name);
		}
	}
	
	/**
	 * Indicates if the species attribute is set
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return true if the value is set
	 */
	public static Boolean isReactionSpecies(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SPECIES)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the species id attribute is set
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return true if the value is set
	 */
	public static Boolean isReactionSpeciesID(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SPECIES_REFERENCE_ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the species name attribute is set
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return true if the value is set
	 */
	public static Boolean isReactionSpeciesName(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SPECIES_REFERENCE_NAME)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the species
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return the value of the attribute species
	 */
	public static String getReactionSpecies(Edge reactionEdge) {
		if (isReactionSpecies(reactionEdge)) {
			return (String) attWriter.getAttribute(reactionEdge,
					SBML_Constants.SBML, SBML_Constants.SPECIES);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the species name
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return the value of the attribute name
	 */
	public static String getReactionSpeciesName(Edge reactionEdge) {
		if (isReactionSpeciesName(reactionEdge)) {
			return (String) attWriter.getAttribute(reactionEdge,
					SBML_Constants.SBML, SBML_Constants.SPECIES_REFERENCE_NAME);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Return the species id
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return the value of the attribute id
	 */
	public static String getReactionSpeciesID(Edge reactionEdge) {
		if (isReactionSpeciesID(reactionEdge)) {
			return (String) attWriter.getAttribute(reactionEdge,
					SBML_Constants.SBML, SBML_Constants.SPECIES_REFERENCE_ID);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Indicates if there is set a value for stoichiometry
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return true if the attribute stoichiometry is set
	 */
	public static Boolean isReactionStoichiometry(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.STOICHIOMETRY)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if there is set a value for constant
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return true if the attribute constant is set
	 */
	public static Boolean isReactionConstant(Edge reactionEdge) {
		if (AttributeHelper.hasAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.REACTION_CONSTANT)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the value of the attribute stoichiometry
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return the value of stoichiometry if the value is set. Else null
	 */
	public static String getStoichiometry(Edge reactionEdge) {
		if (!AttributeHelper.getSBMLrole(reactionEdge).equals("modifier")) {
			if (isReactionStoichiometry(reactionEdge)) {
				Object obj = attWriter.getAttribute(reactionEdge,
						SBML_Constants.SBML, SBML_Constants.STOICHIOMETRY);
				if (obj instanceof String)
					return (String) obj;
				else if (obj instanceof Double)
					return obj.toString();
				else
					return SBML_Constants.EMPTY;
			} else {
				return SBML_Constants.EMPTY;
			}
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the value of the attribute constant
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read from
	 * @return the value of constant if the value is set. Else null
	 */
	public static Boolean getReactionConstant(Edge reactionEdge) {
		if (!AttributeHelper.getSBMLrole(reactionEdge).equals("modifier")) {
			if (isReactionConstant(reactionEdge)) {
				return (Boolean) attWriter.getAttribute(reactionEdge,
						SBML_Constants.SBML, SBML_Constants.REACTION_CONSTANT);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Sets a value for the attribute stoichiometry
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read in
	 * @param stoichiometry
	 *        the value that will be set
	 */
	public static void setStoichiometry(Edge reactionEdge,
			Double stoichiometry) {
		if (!AttributeHelper.getSBMLrole(reactionEdge).equals("modifier")) {
			if (!stoichiometry.equals(null)) {
				if (isSetStoichiometry(reactionEdge)) {
					deleteStoichiometry(reactionEdge);
				}
				AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
						SBML_Constants.STOICHIOMETRY, stoichiometry);
				AttributeHelper.setLabel(reactionEdge, Double.toString(stoichiometry));
			}
		}
	}
	
	// todo: die set methodenfür product, reactant , modifier.
	public static void deleteStoichiometry(Edge edge) {
		if (isSetStoichiometry(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML,
					SBML_Constants.STOICHIOMETRY);
			AttributeHelper.setLabel(edge, "");
		}
	}
	
	public static void deleteConstant(Edge edge) {
		if (isSetConstant(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML,
					SBML_Constants.REACTION_CONSTANT);
		}
	}
	
	public static void deleteReactionSpecies(Edge edge) {
		if (isSetReactionSpecies(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML,
					SBML_Constants.SPECIES);
		}
	}
	
	public static void deleteReactionSpeciesID(Edge edge) {
		if (isSetReactionSpeciesID(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML,
					SBML_Constants.SPECIES_REFERENCE_ID);
		}
	}
	
	public static void deleteReactionSpeciesName(Edge edge) {
		if (isSetReactionSpeciesName(edge)) {
			AttributeHelper.deleteAttribute(edge, SBML_Constants.SBML,
					SBML_Constants.SPECIES_REFERENCE_NAME);
		}
	}
	
	public static boolean isSetStoichiometry(Edge edge) {
		if (isReactionStoichiometry(edge)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSetConstant(Edge edge) {
		if (isReactionConstant(edge)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSetReactionSpecies(Edge edge) {
		if (isReactionSpecies(edge)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSetReactionSpeciesID(Edge edge) {
		if (isReactionSpeciesID(edge)) {
			return true;
		}
		return false;
	}
	
	public static boolean isSetReactionSpeciesName(Edge edge) {
		if (isReactionSpeciesName(edge)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sets a value for the attribute constant
	 * 
	 * @param reactionEdge
	 *        the edge where the information will be read in
	 * @param constant
	 *        the value that will be set
	 */
	public static void setReactionConstant(Edge reactionEdge, Boolean constant) {
		if (!AttributeHelper.getSBMLrole(reactionEdge).equals("modifier")) {
			if (!constant.equals(null)) {
				AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
						SBML_Constants.REACTION_CONSTANT, constant);
			}
		}
	}
	
	/**
	 * Indicates if the function of a kinetic law is set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @return true id the value is set. Else false
	 */
	public static Boolean isKineticLawFunction(Node reactionNode) {
		if (AttributeHelper.hasAttribute(reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_FUNCTION)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the function of a kinetic law
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @return the function string if it is set. Else the empty string
	 */
	public static String getKineticLawFunction(Node reactionNode) {
		if (isKineticLawFunction(reactionNode)) {
			return (String) attWriter.getAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_FUNCTION);
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Sets the function of a kinetic law
	 * 
	 * @param reactionNode
	 *        the node where the information will be read in
	 * @param function
	 *        contains the value that will be read in
	 */
	private static void setKineticLawFunction(Node reactionNode, String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					SBML_Constants.KINETIC_LAW_FUNCTION, function);
		}
	}
	
	public static void addKineticLaw(Node reactionNode, String function) {
		if (!isInitializedKineticLaw) {
			initKineticLawNideIDs();
			isInitializedKineticLaw = true;
		}
		setKineticLawFunction(reactionNode, function);
	}
	
	public static void addKineticLaw(Node reactionNode, KineticLaw kineticLaw) {
		if (!isInitializedKineticLaw) {
			initKineticLawNideIDs();
			isInitializedKineticLaw = true;
		}
		if (kineticLaw.isSetMath()) {
			setKineticLawFunction(reactionNode, kineticLaw.getMath().toFormula());
		}
	}
	
	public static void deleteKineticLawFunction(Node node) {
		AttributeHelper.deleteAttribute(node,
				SBML_Constants.SBML_KINETIC_LAW,
				SBML_Constants.KINETIC_LAW_FUNCTION);
	}
	
	/**
	 * The attribute id will be set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read in
	 * @param ID
	 *        contains the information
	 * @param internAttributeName
	 *        contains the number of the current local parameter
	 */
	private static void setLocalParameterID(Node reactionNode, String ID,
			String internAttributeName) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_ID).toString(), ID);
		}
	}
	
	/**
	 * The attribute name will be set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read in
	 * @param name
	 *        contains the information
	 * @param internAttributeName
	 *        contains the number of the current local parameter
	 */
	private static void setLocalParameterName(Node reactionNode, String name,
			String internAttributeName) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_NAME).toString(),
					name);
		}
	}
	
	/**
	 * The attribute value that will be set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read in
	 * @param value
	 *        contains the information
	 * @param internAttributeName
	 *        contains the number of the current local parameter
	 */
	private static void setLocalParameterValue(Node reactionNode, Double value,
			String internAttributeName) {
		if (!value.equals(null)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_VALUE).toString(),
					value);
		}
	}
	
	/**
	 * The attribute units that will be set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read in
	 * @param units
	 *        contains the information
	 * @param internAttributeName
	 *        contains the number of the current local parameter
	 */
	private static void setLocalParameterUnits(Node reactionNode, String units,
			String internAttributeName) {
		if (!units.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(internAttributeName).append(
							SBML_Constants.LOCAL_PARAMETER_UNITS).toString(),
					units);
		}
	}
	
	/**
	 * Indicates if the attribute id is set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return true if the value is set
	 */
	public static Boolean isLocalParameterID(Node reactionNode,
			int localParameterCount) {
		if (AttributeHelper.hasAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.LOCAL_PARAMETER_ID).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the attribute name is set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return true if the value is set
	 */
	public static Boolean isLocalParameterName(Node reactionNode,
			int localParameterCount) {
		if (AttributeHelper
				.hasAttribute(
						reactionNode,
						SBML_Constants.SBML_KINETIC_LAW,
						new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
								.append(localParameterCount)
								.append(SBML_Constants.LOCAL_PARAMETER_NAME)
								.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the attribute value is set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return true if the value is set
	 */
	public static Boolean isLocalParameterValue(Node reactionNode,
			int localParameterCount) {
		if (AttributeHelper.hasAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.LOCAL_PARAMETER_VALUE)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the attribute units is set
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return true if the value is set
	 */
	public static Boolean isLocalParameterUnits(Node reactionNode,
			int localParameterCount) {
		if (AttributeHelper.hasAttribute(
				reactionNode,
				SBML_Constants.SBML_KINETIC_LAW,
				new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
						.append(localParameterCount)
						.append(SBML_Constants.LOCAL_PARAMETER_UNITS)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the value of the attribute id
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return the value if it is set. Else the empty string
	 */
	public static String getLocalParameterID(Node reactionNode,
			int localParameterCount) {
		if (isLocalParameterID(reactionNode, localParameterCount)) {
			return (String) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_ID)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the value of the attribute name
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return the value if it is set. Else the empty string
	 */
	public static String getLocalParameterName(Node reactionNode,
			int localParameterCount) {
		if (isLocalParameterName(reactionNode, localParameterCount)) {
			return (String) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_NAME)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns the value of the attribute value
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return the value if it is set. Else the empty string
	 */
	public static Double getLocalParameterValue(Node reactionNode,
			int localParameterCount) {
		if (isLocalParameterValue(reactionNode, localParameterCount)) {
			return (Double) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_VALUE)
							.toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Returns the value of the attribute units
	 * 
	 * @param reactionNode
	 *        the node where the information will be read from
	 * @param localParameterCount
	 *        the number of the current local parameter
	 * @return the value if it is set. Else the empty string
	 */
	public static String getLocalParameterUnits(Node reactionNode,
			int localParameterCount) {
		if (isLocalParameterUnits(reactionNode, localParameterCount)) {
			return (String) attWriter.getAttribute(
					reactionNode,
					SBML_Constants.SBML_KINETIC_LAW,
					new StringBuffer(SBML_Constants.LOCAL_PARAMETER)
							.append(localParameterCount)
							.append(SBML_Constants.LOCAL_PARAMETER_UNITS)
							.toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns all reaction nodes of the graph
	 * 
	 * @return a list of all reaction nodes of the graph
	 */
	public static List<Node> getReactionNodes(Graph g) {
		List<Node> reactionNodeList = new ArrayList<Node>();
		Iterator<Node> itNode = g.getNodesIterator();
		while (itNode.hasNext()) {
			Node node = itNode.next();
			if (AttributeHelper.getSBMLrole(node).equals("reaction")) {
				reactionNodeList.add(node);
			}
		}
		return reactionNodeList;
	}
	
	/**
	 * Returns the reaction node with a distinct id
	 * 
	 * @param id
	 *        the id of the asked node
	 * @return the node with a certain id or null if no node has this id
	 */
	public static Node getReactionNode(Graph g, String id) {
		List<Node> nodeList = getReactionNodes(g);
		Iterator<Node> itNode = nodeList.iterator();
		Node currentNode = null;
		while (itNode.hasNext()) {
			currentNode = itNode.next();
			if (getReactionID(currentNode).equals(id)) {
				return currentNode;
			}
		}
		return null;
	}
	
	/**
	 * Creates an reaction Node
	 * 
	 * @param g
	 *        the graph where the node will be added
	 * @return the created reaction node
	 */
	private static Node createReactionNode(Graph g, String id) {
		Node node = g.addNode();
		AttributeHelper.setSBMLrole(node, (String) SBML_Constants.ROLE_REACTION);
		AttributeHelper.setDefaultGraphicsAttribute(node, pgg.getNextPosition());
		AttributeHelper.setLabel(node, id);
		AttributeHelper.setSize(node, id.length() * id.length() + 7, 20d);
		AttributeHelper.setFillColor(node, Color.WHITE);
		AttributeHelper.setShapeEllipse(node);
		AttributeHelper.setBorderWidth(node, 1);
		return node;
	}
	
	public static Node createReaction(Graph g, String id, String name) {
		if (!SBML_Constants.EMPTY.equals(id) || !SBML_Constants.EMPTY.equals(name)) {
			if (!isInitializedReaction) {
				initReactionNideIDs("sbml", "SBML");
				isInitializedReaction = true;
			}
			Node node = createReactionNode(g, id);
			initReactionNideIDs("sbml", "SBML");
			if (!name.equals(SBML_Constants.EMPTY))
			{
				setReactionName(node, name);
			}
			setReactionID(node, id);
			return node;
		}
		else {
			return null;
		}
	}
	
	public static Node initReactionNode(Node n) {
		String label = AttributeHelper.getLabel(n, SBML_Constants.EMPTY);
		if (label == SBML_Constants.EMPTY) {
			return null;
		}
		else {
			setReactionName(n, label);
			AttributeHelper.setShapeRectangle(n);
			AttributeHelper.setSBMLrole(n, "reaction");
			return n;
		}
	}
	
	public static Edge addReactant(Node node, String speciesID) {
		Node reactantNode = SBMLSpeciesHelper.getSpeciesNode(speciesID);
		Edge newReactionEdge = node.getGraph().addEdge(reactantNode, node, true,
				AttributeHelper.getDefaultGraphicsAttributeForEdge(
						Color.BLACK, Color.BLACK, true));
		setReactionSpecies(newReactionEdge, "speciesID");
		
		AttributeHelper.setLabel(newReactionEdge, Integer.toString(1));
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_REACTANT);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE, false);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.STOICHIOMETRY, 1);
		return newReactionEdge;
	}
	
	public static Edge initReactantEdge(Edge e) {
		Node node = e.getSource();
		if (isSpeciesID(node)) {
			setReactionSpecies(e, getSpeciesID(node));
		}
		
		String label = AttributeHelper.getLabel(e, "-1");
		if (label.equals("-1")) {
			AttributeHelper.setLabel(e, "1");
			setStoichiometry(e, Double.parseDouble("1"));
		}
		else if (label.equals("")) {
			AttributeHelper.setLabel(e, "1");
			setStoichiometry(e, Double.parseDouble("1"));
		}
		else {
			AttributeHelper.setLabel(e, label);
			setStoichiometry(e, Double.parseDouble(label));
		}
		AttributeHelper.setSBMLrole(e, SBML_Constants.ROLE_REACTANT);
		return e;
	}
	
	public static Edge initProductEdge(Edge e) {
		Node node = e.getTarget();
		if (isSpeciesID(node)) {
			setReactionSpecies(e, getSpeciesID(node));
		}
		
		String label = AttributeHelper.getLabel(e, "-1");
		if (label.equals("-1")) {
			AttributeHelper.setLabel(e, "1");
			setStoichiometry(e, Double.parseDouble("1"));
		}
		else if (label.equals("")) {
			AttributeHelper.setLabel(e, "1");
			setStoichiometry(e, Double.parseDouble("1"));
		}
		else {
			AttributeHelper.setLabel(e, label);
			setStoichiometry(e, Double.parseDouble(label));
		}
		AttributeHelper.setSBMLrole(e, SBML_Constants.ROLE_PRODUCT);
		return e;
	}
	
	public static Edge addProduct(Node node, String speciesID) {
		Node productNode = SBMLSpeciesHelper.getSpeciesNode(speciesID);
		Edge newReactionEdge = node.getGraph().addEdge(node, productNode, true,
				AttributeHelper.getDefaultGraphicsAttributeForEdge(
						Color.BLACK, Color.BLACK, true));
		setReactionSpecies(newReactionEdge, speciesID);
		
		AttributeHelper.setLabel(newReactionEdge, Integer.toString(1));
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_PRODUCT);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE, false);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.STOICHIOMETRY, 1);
		return newReactionEdge;
	}
	
	public static Edge addModifier(Node node, String speciesID) {
		Node modifierNode = SBMLSpeciesHelper.getSpeciesNode(speciesID);
		Edge reactionEdge = node.getGraph().addEdge(modifierNode, node, false,
				AttributeHelper.getDefaultGraphicsAttributeForEdge(
						Color.DARK_GRAY, Color.DARK_GRAY, true));
		setReactionSpecies(reactionEdge, speciesID);
		
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_MODIFIER);
		AttributeHelper.setDashInfo(reactionEdge, 5, 5);
		AttributeHelper.setBorderWidth(reactionEdge, 1d);
		return reactionEdge;
	}
	
	public static Edge addReactant(Node node, SpeciesReference ref) {
		if (SBMLSpeciesHelper.speciesMap == null) {
			SBMLSpeciesHelper.speciesMap = new HashMap<String, Node>();
		}
		if (!SBMLSpeciesHelper.speciesMap.containsKey(ref.getSpecies())) {
			createSpecies(node.getGraph(), ref.getSpecies(), "");
		}
		Node reactantNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
		String stoichiometry = Double.toString(ref.getStoichiometry());
		if (ref.getStoichiometry() == Double.NaN) {
			ErrorMsg.addErrorMessage("Attribute stochiometry of reaction "
					+ getReactionID(node) + " species " + ref.getSpecies()
					+ " is not a valid double value.");
		}
		Edge newReactionEdge = node.getGraph().addEdge(reactantNode, node, true,
				AttributeHelper.getDefaultGraphicsAttributeForEdge(
						Color.BLACK, Color.BLACK, true));
		if (isReactionReversible(node)) {
			AttributeHelper.setArrowtail(newReactionEdge, true);
		}
		
		AttributeHelper.setLabel(newReactionEdge, stoichiometry);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_REACTANT);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE, isReactionReversible(node));
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.STOICHIOMETRY, stoichiometry);
		setSimpleSpeciesReferences(ref, newReactionEdge);
		return newReactionEdge;
	}
	
	private static void deleteEdge(Edge edge) {
		edge.getGraph().deleteEdge(edge);
	}
	
	public static void deleteReactant(Edge edge) {
		deleteEdge(edge);
	}
	
	public static void deleteProduct(Edge edge) {
		deleteEdge(edge);
	}
	
	public static void deleteModifier(Edge edge) {
		deleteEdge(edge);
	}
	
	public static Edge addModifier(Node node, ModifierSpeciesReference ref) {
		if (SBMLSpeciesHelper.speciesMap == null) {
			SBMLSpeciesHelper.speciesMap = new HashMap<String, Node>();
		}
		if (!SBMLSpeciesHelper.speciesMap.containsKey(ref.getSpecies())) {
			createSpecies(node.getGraph(), ref.getSpecies(), "");
		}
		Node modifierNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
		Edge reactionEdge = node.getGraph().addEdge(modifierNode, node, false,
				AttributeHelper.getDefaultGraphicsAttributeForEdge(
						Color.DARK_GRAY, Color.DARK_GRAY, true));
		
		AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
				SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_MODIFIER);
		AttributeHelper.setDashInfo(reactionEdge, 5, 5);
		AttributeHelper.setBorderWidth(reactionEdge, 1d);
		setSimpleSpeciesReferences(ref, reactionEdge);
		return reactionEdge;
	}
	
	public static Edge addProduct(Node node, SpeciesReference ref) {
		if (SBMLSpeciesHelper.speciesMap == null) {
			SBMLSpeciesHelper.speciesMap = new HashMap<String, Node>();
		}
		if (!SBMLSpeciesHelper.speciesMap.containsKey(ref.getSpecies())) {
			createSpecies(node.getGraph(), ref.getSpecies(), "");
		}
		
		Node productNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
		String stoichiometry = Double.toString(ref.getStoichiometry());
		String label = stoichiometry;
		Edge newReactionEdge = node.getGraph().addEdge(node, productNode, true,
				AttributeHelper.getDefaultGraphicsAttributeForEdge(
						Color.BLACK, Color.BLACK, true));
		if (isReactionReversible(node)) {
			AttributeHelper.setArrowtail(newReactionEdge, true);
		}
		if (ref.getStoichiometry() == Double.NaN) {
			ErrorMsg.addErrorMessage("Attribute stochiometry of reaction "
					+ getReactionID(node) + " species " + ref.getSpecies()
					+ " is not a valid double value.");
		}
		
		AttributeHelper.setLabel(newReactionEdge, label);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_PRODUCT);
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.REVERSIBLE, isReactionReversible(node));
		AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
				SBML_Constants.STOICHIOMETRY, stoichiometry);
		setSimpleSpeciesReferences(ref, newReactionEdge);
		return newReactionEdge;
		
	}
	
	public static Node createReaction(Graph g, Reaction reaction) {
		Node node = createReactionNode(g, reaction.getId());
		if (!isInitializedReaction) {
			initReactionNideIDs("sbml", "SBML");
			isInitializedReaction = true;
		}
		setReactionID(node, reaction.getId());
		reaction.setLevel(3);
		reaction.setVersion(1);
		if (reaction.isSetCompartment()) {
			setReactionCompartment(node, reaction.getCompartment());
		}
		if (reaction.isSetId()) {
			setReactionID(node, reaction.getId());
		}
		if (reaction.isReversible()) {
			setReactionReversible(node, reaction.isReversible());
		}
		if (reaction.isSetFast()) {
			setReactionFast(node, reaction.getFast());
		}
		if (reaction.isSetName()) {
			setReactionName(node, reaction.getName());
		}
		
		// Adds the edges between reactant node and reaction node
		ListOf<SpeciesReference> reactants = reaction.getListOfReactants();
		Iterator<SpeciesReference> it = reactants.iterator();
		while (it.hasNext()) {
			SpeciesReference ref = it.next();
			Node reactantNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			String stoichiometry = Double.toString(ref.getStoichiometry());
			if (ref.getStoichiometry() == Double.NaN) {
				ErrorMsg.addErrorMessage("Attribute stochiometry of reaction "
						+ reaction.getId() + " species " + ref.getSpecies()
						+ " is not a valid double value.");
			}
			Edge newReactionEdge = g.addEdge(reactantNode, node, true,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(
							Color.BLACK, Color.BLACK, true));
			if (reaction.isReversible()) {
				AttributeHelper.setArrowtail(newReactionEdge, true);
			}
			
			AttributeHelper.setLabel(newReactionEdge, stoichiometry);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_REACTANT);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.REVERSIBLE, reaction.isReversible());
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.STOICHIOMETRY, stoichiometry);
			setSimpleSpeciesReferences(ref, newReactionEdge);
		}
		
		ListOf<SpeciesReference> products = reaction.getListOfProducts();
		Iterator<SpeciesReference> itProduct = products.iterator();
		while (itProduct.hasNext()) {
			SpeciesReference ref = itProduct.next();
			Node productNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			String stoichiometry = Double.toString(ref.getStoichiometry());
			String label = stoichiometry;
			Edge newReactionEdge = g.addEdge(node, productNode, true,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(
							Color.BLACK, Color.BLACK, true));
			if (reaction.isReversible()) {
				AttributeHelper.setArrowtail(newReactionEdge, true);
			}
			if (ref.getStoichiometry() == Double.NaN) {
				ErrorMsg.addErrorMessage("Attribute stochiometry of reaction "
						+ reaction.getId() + " species " + ref.getSpecies()
						+ " is not a valid double value.");
			}
			
			AttributeHelper.setLabel(newReactionEdge, label);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_PRODUCT);
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.REVERSIBLE, reaction.getReversible());
			AttributeHelper.setAttribute(newReactionEdge, SBML_Constants.SBML,
					SBML_Constants.STOICHIOMETRY, stoichiometry);
			setSimpleSpeciesReferences(ref, newReactionEdge);
		}
		
		ListOf<ModifierSpeciesReference> modifiers = reaction.getListOfModifiers();
		Iterator<ModifierSpeciesReference> itModifier = modifiers.iterator();
		while (itModifier.hasNext()) {
			SpeciesReference ref = it.next();
			Node modifierNode = SBMLSpeciesHelper.getSpeciesNode(ref.getSpecies());
			Edge reactionEdge = g.addEdge(modifierNode, node, false,
					AttributeHelper.getDefaultGraphicsAttributeForEdge(
							Color.DARK_GRAY, Color.DARK_GRAY, true));
			
			AttributeHelper.setAttribute(reactionEdge, SBML_Constants.SBML,
					SBML_Constants.SBML_ROLE, SBML_Constants.ROLE_MODIFIER);
			AttributeHelper.setDashInfo(reactionEdge, 5, 5);
			AttributeHelper.setBorderWidth(reactionEdge, 1d);
			setSimpleSpeciesReferences(ref, reactionEdge);
		}
		
		if (reaction.isSetKineticLaw()) {
			if (!isInitializedKineticLaw) {
				initKineticLawNideIDs();
				isInitializedKineticLaw = true;
			}
			KineticLaw kineticLaw = reaction.getKineticLaw();
			String kineticFormula = "";
			try {
				if (kineticLaw.isSetMath()) {
					if (null != kineticLaw.getMath()) {
						kineticFormula = kineticLaw.getMath().toFormula();
					}
				}
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			
			if (kineticLaw.isSetMath()) {
				setKineticLawFunction(node, kineticFormula);
			}
			
			// Two ways to read in a Local Parameter. One way is deprecated.
			if (kineticLaw.isSetListOfLocalParameters()
					|| kineticLaw.isSetListOfParameters()) {
				List<LocalParameter> listLocalParameter = null;
				if (reaction.getModel().getLevel() == 3
						&& reaction.getModel().getVersion() == 1) {
					if (kineticLaw.isSetListOfLocalParameters()) {
						listLocalParameter = kineticLaw
								.getListOfLocalParameters();
					}
				} else {
					if (kineticLaw.isSetListOfParameters()) {
						listLocalParameter = kineticLaw
								.getListOfParameters();
					}
				}
				Iterator<LocalParameter> itLP = listLocalParameter.iterator();
				int countLocalParameter = 1;
				while (itLP.hasNext()) {
					LocalParameter localParameter = itLP.next();
					String internAttributeName = new StringBuffer(
							SBML_Constants.LOCAL_PARAMETER).append(
							countLocalParameter).toString();
					String presentedAttributeName = new StringBuffer(
							SBML_Constants.LOCALPARAMETER_HEADLINE).append(
							countLocalParameter).toString();
					initLocalParameterNideIDs(presentedAttributeName, internAttributeName, "Kinetic Law");
					
					String id = localParameter.getId();
					String name = localParameter.getName();
					Double value = localParameter.getValue();
					if (value.equals(Double.NaN)) {
						
						ErrorMsg.addErrorMessage("Attribute value of reaction "
								+ reaction.getId()
								+ " "
								+ presentedAttributeName
								+ " is not a valid double value.");
						
					}
					String unit = localParameter.getUnits();
					
					if (localParameter.isSetId()) {
						setLocalParameterID(node, localParameter.getId(), internAttributeName);
					}
					if (localParameter.isSetName()) {
						setLocalParameterName(node, localParameter.getName(), internAttributeName);
					}
					if (localParameter.isSetValue()) {
						setLocalParameterValue(node, localParameter.getValue(), internAttributeName);
					}
					if (localParameter.isSetUnits()) {
						setLocalParameterUnits(node, localParameter.getUnits(), internAttributeName);
					}
					countLocalParameter++;
				}
			}
		}
		AttributeHelper.setLabel(AttributeHelper.getLabels(node)
				.size(), node, reaction.getId(), null,
				AlignmentSetting.HIDDEN.toGMLstring());
		return node;
	}
	
	public static void addLocalParameterName(Node node, String lpID, String name) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				String internAttributeName = new StringBuffer(
						SBML_Constants.LOCAL_PARAMETER).append(
						i).toString();
				String presentedAttributeName = new StringBuffer(
						SBML_Constants.LOCALPARAMETER_HEADLINE).append(
						i).toString();
				setLocalParameterName(node, name, internAttributeName);
				initLocalParameterNideIDs(presentedAttributeName, internAttributeName, "Kinetic Law");
			}
			
		}
	}
	
	public static boolean isSetLocalParameterName(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterName(node, i)) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isSetLocalParameterUnits(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterUnits(node, i)) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	
	public static boolean isSetLocalParameterValue(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterValue(node, i)) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	
	public static void deleteLocalParameterName(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterName(node, i)) {
					String internAttributeName = new StringBuffer(
							SBML_Constants.LOCAL_PARAMETER).append(
							i).toString();
					AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
							new StringBuffer(internAttributeName).append(
									SBML_Constants.LOCAL_PARAMETER_NAME).toString());
				}
			}
		}
	}
	
	public static void deleteLocalParameterValue(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterValue(node, i)) {
					String internAttributeName = new StringBuffer(
							SBML_Constants.LOCAL_PARAMETER).append(
							i).toString();
					AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
							new StringBuffer(internAttributeName).append(
									SBML_Constants.LOCAL_PARAMETER_VALUE).toString());
				}
			}
		}
	}
	
	public static void deleteLocalParameterUnits(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterUnits(node, i)) {
					String internAttributeName = new StringBuffer(
							SBML_Constants.LOCAL_PARAMETER).append(
							i).toString();
					AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
							new StringBuffer(internAttributeName).append(
									SBML_Constants.LOCAL_PARAMETER_UNITS).toString());
				}
			}
		}
	}
	
	public static void deleteLocalParameter(Node node, String lpID) {
		deleteLocalParameterValue(node, lpID);
		deleteLocalParameterUnits(node, lpID);
		deleteLocalParameterName(node, lpID);
		deleteLocalParameterID(node, lpID);
	}
	
	public static void deleteLocalParameterID(Node node, String lpID) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				if (isLocalParameterID(node, i)) {
					String internAttributeName = new StringBuffer(
							SBML_Constants.LOCAL_PARAMETER).append(
							i).toString();
					AttributeHelper.deleteAttribute(node, SBML_Constants.SBML_KINETIC_LAW,
							new StringBuffer(internAttributeName).append(
									SBML_Constants.LOCAL_PARAMETER_ID).toString());
				}
			}
		}
	}
	
	public static void addLocalParameterValue(Node node, String lpID, Double value) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				String internAttributeName = new StringBuffer(
						SBML_Constants.LOCAL_PARAMETER).append(
						i).toString();
				String presentedAttributeName = new StringBuffer(
						SBML_Constants.LOCALPARAMETER_HEADLINE).append(
						i).toString();
				setLocalParameterValue(node, value, internAttributeName);
				initLocalParameterNideIDs(presentedAttributeName, internAttributeName, "Kinetic Law");
			}
			
		}
	}
	
	public static void addLocalParameterUnits(Node node, String lpID, String units) {
		int lpCount = localParameterCount(node);
		for (int i = 1; i <= lpCount; i++) {
			if (getLocalParameterID(node, i) == lpID) {
				String internAttributeName = new StringBuffer(
						SBML_Constants.LOCAL_PARAMETER).append(
						i).toString();
				String presentedAttributeName = new StringBuffer(
						SBML_Constants.LOCALPARAMETER_HEADLINE).append(
						i).toString();
				setLocalParameterUnits(node, units, internAttributeName);
				initLocalParameterNideIDs(presentedAttributeName, internAttributeName, "Kinetic Law");
			}
		}
	}
	
	private static void setSimpleSpeciesReferences(SimpleSpeciesReference simpleRef,
			Edge edge) {
		if (simpleRef instanceof org.sbml.jsbml.SpeciesReference) {
			if (((SpeciesReference) simpleRef).isSetStoichiometry()) {
				setStoichiometry(edge, ((SpeciesReference) simpleRef).getStoichiometry());
			}
			if (((SpeciesReference) simpleRef).isSetConstant()) {
				setReactionConstant(edge, ((SpeciesReference) simpleRef).getConstant());
			}
		}
		if (simpleRef.isSetSpecies()) {
			setReactionSpecies(edge, simpleRef.getSpecies());
		}
		if (simpleRef.isSetId()) {
			setReactionSpeciesID(edge, simpleRef.getId());
		}
		if (simpleRef.isSetName()) {
			setReactionSpeciesName(edge, simpleRef.getName());
		}
		if (AttributeHelper.getSBMLrole(edge).equals(
				SBML_Constants.ROLE_REACTANT)) {
		}
	}
	
	/**
	 * The edges with the simple species references will be returned
	 * 
	 * @param node
	 *        the reaction node
	 * @return all outgoing edges
	 */
	public static List<Edge> getProducts(Node node) {
		List<Edge> edgeList = new ArrayList<Edge>();
		Iterator<Edge> edgeIt = node.getEdgesIterator();
		while (edgeIt.hasNext()) {
			Edge nextEdge = edgeIt.next();
			if (AttributeHelper.getSBMLrole(node).equals("product")) {
				edgeList.add(nextEdge);
			}
		}
		return edgeList;
	}
	
	/**
	 * The edges with the simple species references will be returned
	 * 
	 * @param node
	 *        the reaction node
	 * @return all ingoing edges
	 */
	public static List<Edge> getReactants(Node node) {
		List<Edge> edgeList = new ArrayList<Edge>();
		Iterator<Edge> edgeIt = node.getEdgesIterator();
		while (edgeIt.hasNext()) {
			Edge nextEdge = edgeIt.next();
			if (AttributeHelper.getSBMLrole(node).equals("reactant")) {
				edgeList.add(nextEdge);
			}
		}
		return edgeList;
	}
	
	/**
	 * Returns the modifier edges
	 * 
	 * @param node
	 *        the reaction node
	 * @return all modifier edges
	 */
	public static List<Edge> getModifier(Node node) {
		List<Edge> edgeList = new ArrayList<Edge>();
		Iterator<Edge> edgeIt = node.getEdgesIterator();
		while (edgeIt.hasNext()) {
			Edge nextEdge = edgeIt.next();
			if (AttributeHelper.getSBMLrole(node).equals("modifier")) {
				edgeList.add(nextEdge);
			}
		}
		return edgeList;
	}
	
	/**
	 * Returns a JSBML Reaction object belonging to a reaction node
	 * 
	 * @param node
	 *        the reaction node
	 * @return a JSBML Reaction object
	 */
	public static Reaction getReaction(Node node) {
		if (AttributeHelper.getSBMLrole(node).equals("reaction")) {
			Reaction reaction = new Reaction();
			reaction.setId(getReactionID(node));
			reaction.setName(getReactionName(node));
			if (isReactionReversible(node)) {
				reaction.setReversible(getReactionReversible(node));
			}
			if (isReactionFast(node)) {
				reaction.setFast(getReactionFast(node));
			}
			reaction.setCompartment(getReactionCompartment(node));
			
			Iterator<Edge> itEdges = node.getEdgesIterator();
			while (itEdges.hasNext()) {
				Edge edge = itEdges.next();
				if (AttributeHelper.getSBMLrole(edge).equals("reactant")) {
					SpeciesReference reactant = new SpeciesReference();
					if (isReactionStoichiometry(edge)) {
						reactant.setStoichiometry(Double
								.parseDouble(getStoichiometry(edge)));
					}
					if (isReactionConstant(edge)) {
						reactant.setConstant(getReactionConstant(edge));
					}
					reactant.setId(getReactionSpeciesID(edge));
					reactant.setName(getReactionSpeciesName(edge));
					reactant.setSpecies(getReactionSpecies(edge));
					reaction.addReactant(reactant);
				}
				if (AttributeHelper.getSBMLrole(edge).equals("product")) {
					SpeciesReference product = new SpeciesReference();
					if (isReactionStoichiometry(edge)) {
						product.setStoichiometry(Double
								.parseDouble(getStoichiometry(edge)));
					}
					if (isReactionConstant(edge)) {
						product.setConstant(getReactionConstant(edge));
					}
					product.setId(getReactionSpeciesID(edge));
					product.setName(getReactionSpeciesName(edge));
					product.setSpecies(getReactionSpecies(edge));
					reaction.addProduct(product);
				}
				if (AttributeHelper.getSBMLrole(edge).equals("modifier")) {
					ModifierSpeciesReference modifier = new ModifierSpeciesReference();
					modifier.setId(getReactionSpeciesID(edge));
					modifier.setName(getReactionSpeciesName(edge));
					modifier.setSpecies(getReactionSpecies(edge));
					reaction.addModifier(modifier);
				}
			}
			
			if (isKineticLawFunction(node)) {
				KineticLaw kl = new KineticLaw();
				try {
					kl.setFormula(getKineticLawFunction(node));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				int lpCount = localParameterCount(node);
				for (int i = 1; i <= lpCount; i++) {
					if (isLocalParameterID(node, i)) {
						LocalParameter lp = new LocalParameter();
						lp.setId(getLocalParameterID(node, i));
						lp.setName(getLocalParameterName(node, i));
						if (isLocalParameterValue(node, lpCount)) {
							lp.setValue(getLocalParameterValue(node, i));
						}
						lp.setUnits(getLocalParameterUnits(node, i));
						kl.addLocalParameter(lp);
					}
				}
				reaction.setKineticLaw(kl);
			}
			return reaction;
		} else {
			return null;
		}
	}
	
	public static void addLocalParameter(Node node, LocalParameter lp) {
		int lpCount = localParameterCount(node);
		String internAttributeName = new StringBuffer(
				SBML_Constants.LOCAL_PARAMETER).append(
				lpCount + 1).toString();
		String presentedAttributeName = new StringBuffer(
				SBML_Constants.LOCALPARAMETER_HEADLINE).append(
				lpCount + 1).toString();
		initLocalParameterNideIDs(presentedAttributeName, internAttributeName, "Kinetic Law");
		if (lp.isSetId()) {
			setLocalParameterID(node, lp.getId(), internAttributeName);
		}
		if (lp.isSetName()) {
			setLocalParameterName(node, lp.getName(), internAttributeName);
		}
		if (lp.isSetValue()) {
			setLocalParameterValue(node, lp.getValue(), internAttributeName);
		}
		if (lp.isSetUnits()) {
			setLocalParameterUnits(node, lp.getUnits(), internAttributeName);
		}
	}
	
	public static void addLocalParameter(Node node, String id) {
		int lpCount = localParameterCount(node);
		String internAttributeName = new StringBuffer(
				SBML_Constants.LOCAL_PARAMETER).append(
				lpCount + 1).toString();
		String presentedAttributeName = new StringBuffer(
				SBML_Constants.LOCALPARAMETER_HEADLINE).append(
				lpCount + 1).toString();
		initLocalParameterNideIDs(presentedAttributeName, internAttributeName, "Kinetic Law");
		setLocalParameterID(node, id, internAttributeName);
	}
	
	/**
	 * Returns the number of local parameter
	 * 
	 * @param node
	 *        the current node
	 * @return the amount of local parameter
	 */
	public static int localParameterCount(Node node) {
		int lpCount = 0;
		while (isLocalParameterID(node, lpCount + 1)) {
			lpCount++;
		}
		return lpCount;
	}
	
	public static Event createEvent(Graph g, String id) {
		String internHeadline = new StringBuffer(SBML_Constants.SBML_EVENT).append(eventCount).toString();
		String presentedHeadline = new StringBuffer("SBML Event ").append(eventCount).toString();
		setEventID(g, internHeadline, id);
		Event event = new Event();
		event.setLevel(3);
		event.setId(id);
		initEventNiceIDs(internHeadline, presentedHeadline);
		++eventCount;
		return event;
	}
	
	public static void createEvent(Graph g, Event event) {
		String internHeadline = new StringBuffer(SBML_Constants.SBML_EVENT).append(eventCount).toString();
		String presentedHeadline = new StringBuffer("SBML Event ").append(eventCount).toString();
		initEventNiceIDs(internHeadline, presentedHeadline);
		
		event.setLevel(3);
		if (event.isSetId()) {
			setEventID(g, internHeadline, event.getId());
		}
		if (event.isSetName()) {
			setEventName(g, internHeadline, event.getName());
		}
		if (event.isSetUseValuesFromTriggerTime()) {
			setEventUseValuesFromTriggerTime(g, internHeadline, event.getUseValuesFromTriggerTime());
		}
		Trigger trigger = event.getTrigger();
		String triggerFormula = "";
		try {
			if (trigger.isSetMath()) {
				if (null != trigger.getMath()) {
					triggerFormula = trigger.getMath().toFormula();
				}
			}
		} catch (SBMLException e) {
			e.printStackTrace();
		}
		
		Boolean triggerInitialValue = false;
		Boolean triggerPersistent = false;
		try {
			if (trigger.isSetInitialValue())
				triggerInitialValue = trigger.getInitialValue();
			if (trigger.isSetPersistent())
				triggerPersistent = trigger.getPersistent();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (trigger.isSetInitialValue()) {
			setTriggerInitialValue(g, internHeadline, triggerInitialValue);
		}
		if (trigger.isSetPersistent()) {
			setTriggerPersistent(g, internHeadline, triggerPersistent);
		}
		if (trigger.isSetMath()) {
			setTriggerFunction(g, internHeadline, triggerFormula);
		}
		if (event.isSetPriority()) {
			Priority priority = event.getPriority();
			String priorityFormula = "";
			try {
				if (priority.isSetMath()) {
					if (null != priority.getMath()) {
						priorityFormula = priority.getMath().toFormula();
					}
				}
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			if (!priorityFormula.equals("")) {
				setPriorityFunction(g, internHeadline, priorityFormula);
			}
		}
		String delayFormula = "";
		if (event.isSetDelay()) {
			Delay delay = event.getDelay();
			try {
				if (delay.isSetMath()) {
					if (null != delay.getMath()) {
						delayFormula = delay.getMath().toFormula();
					}
				}
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			if (!delayFormula.equals("")) {
				setDelayFunction(g, internHeadline, delayFormula);
			}
		}
		List<EventAssignment> listEventAssignment = event
				.getListOfEventAssignments();
		Iterator<EventAssignment> itEventAssignment = listEventAssignment
				.iterator();
		int eventAssignmentCount = 1;
		while (itEventAssignment.hasNext()) {
			initEventAssignmentNideIDs(eventAssignmentCount, internHeadline, presentedHeadline);
			EventAssignment eventAssignment = itEventAssignment.next();
			
			String variable = eventAssignment.getVariable();
			String eventAssignmentFormula = "";
			try {
				if (eventAssignment.isSetMath()) {
					if (null != eventAssignment.getMath()) {
						eventAssignmentFormula = eventAssignment.getMath()
								.toFormula();
					}
				}
			} catch (SBMLException e) {
				e.printStackTrace();
			}
			
			if (eventAssignment.isSetVariable()) {
				setEventAssignmentVariable(g, internHeadline, variable, eventAssignmentCount);
			}
			if (eventAssignment.isSetMath()) {
				setEventAssignmentFunction(g, internHeadline, eventAssignmentFormula, eventAssignmentCount);
			}
			eventAssignmentCount++;
		}
		
		++eventCount;
	}
	
	public static void addEventName(Graph g, String id, String name) {
		setEventName(g, returnEventWithID(g, id), name);
	}
	
	public static void addEventUseValuesFromTriggerTime(Graph g, String id, boolean useValuesFromTriggerTime) {
		setEventUseValuesFromTriggerTime(g, returnEventWithID(g, id), useValuesFromTriggerTime);
	}
	
	public static void addTriggerInitialValue(Graph g, String eventID, boolean initialValue) {
		setTriggerInitialValue(g, returnEventWithID(g, eventID), initialValue);
	}
	
	public static void addTriggerPersistent(Graph g, String eventID, boolean persistent) {
		setTriggerPersistent(g, returnEventWithID(g, eventID), persistent);
	}
	
	public static void addTriggerFunction(Graph g, String eventID, String function) {
		setTriggerFunction(g, returnEventWithID(g, eventID), function);
	}
	
	public static boolean isSetEventName(Graph g, String id) {
		if (isEventName(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetEventUseValuesFromTriggerTime(Graph g, String id) {
		if (isEventUseValuesFromTriggerTime(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetTriggerInitialValue(Graph g, String id) {
		if (isTriggerInitialValue(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetTriggerPersistent(Graph g, String id) {
		if (isTriggerPersistent(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetTriggerFunction(Graph g, String id) {
		if (isTriggerFunction(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetDelayFunction(Graph g, String id) {
		if (isDelayFunction(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetPriority(Graph g, String id) {
		if (isPriorityFunction(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isSetDelay(Graph g, String id) {
		if (isDelayFunction(g, returnEventWithID(g, id))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void deleteEventName(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetEventName(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.EVENT_NAME).toString());
		}
	}
	
	public static void deleteEventUseValuesFromTriggerTime(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetEventUseValuesFromTriggerTime(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME).toString());
		}
	}
	
	public static void deleteEventID(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
				headline).append(SBML_Constants.EVENT_ID).toString());
	}
	
	public static void deleteTriggeInitialValue(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetTriggerInitialValue(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.INITIAL_VALUE)
					.toString());
		}
	}
	
	public static void deleteTriggerPersistent(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetTriggerPersistent(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.PERSISTENT)
					.toString());
		}
	}
	
	public static void deleteTriggerFunction(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetTriggerFunction(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.TRIGGER_FUNCTION)
					.toString());
		}
	}
	
	public static void deleteDelayFunction(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetDelayFunction(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.DELAY_FUNCTION)
					.toString());
		}
	}
	
	public static void deletePriorityFunction(Graph g, String id) {
		String headline = returnEventWithID(g, id);
		if (isSetPriority(g, id)) {
			AttributeHelper.deleteAttribute(g, headline, new StringBuffer(
					headline).append(SBML_Constants.PRIORITY_FUNCTION)
					.toString());
		}
	}
	
	public static void deleteTrigger(Graph g, String id) {
		deleteTriggeInitialValue(g, id);
		deleteTriggerPersistent(g, id);
		deleteTriggerFunction(g, id);
	}
	
	public static void deletePriority(Graph g, String id) {
		deletePriorityFunction(g, id);
	}
	
	public static void addTriggerToEvent(Graph g, String id, Trigger trigger) {
		String headline = returnEventWithID(g, id);
		if (trigger.isSetInitialValue()) {
			setTriggerInitialValue(g, headline, trigger.getInitialValue());
		}
		if (trigger.isSetPersistent()) {
			setTriggerPersistent(g, headline, trigger.getPersistent());
		}
		if (trigger.isSetMath()) {
			setTriggerFunction(g, headline, trigger.getMath().toFormula());
		}
	}
	
	public static void addTriggerToEvent(Graph g, String id, boolean initialValue, boolean persistent, String formula) {
		String headline = returnEventWithID(g, id);
		setTriggerInitialValue(g, headline, initialValue);
		setTriggerPersistent(g, headline, persistent);
		if (formula != "" && formula != null) {
			setTriggerFunction(g, headline, formula);
		}
	}
	
	public static void addPriorityToEvent(Graph g, String id, Priority priority) {
		String headline = returnEventWithID(g, id);
		if (priority.isSetMath()) {
			setPriorityFunction(g, headline, priority.getMath().toFormula());
		}
	}
	
	public static void addDelayToEvent(Graph g, String id, Delay delay) {
		String headline = returnEventWithID(g, id);
		if (delay.isSetMath()) {
			setDelayFunction(g, headline, delay.getMath().toFormula());
		}
	}
	
	public static void addPriorityToEvent(Graph g, String id, String function) {
		String headline = returnEventWithID(g, id);
		if (function != "" && function != null) {
			setPriorityFunction(g, headline, function);
		}
	}
	
	public static void addDelayToEvent(Graph g, String id, String function) {
		String headline = returnEventWithID(g, id);
		if (function != "" && function != null) {
			setDelayFunction(g, headline, function);
		}
	}
	
	public static void addEventAssignmentToEvent(Graph g, String id, String variable, String function) {
		String internHeadline = returnEventWithID(g, id);
		char index = internHeadline.charAt(internHeadline.length() - 1);
		String presentedHeadline = new StringBuffer("SBML Event ")
				.append(index).toString();
		
		int eventAssignmentCount = eventAssignmentCount(g, internHeadline);
		initEventAssignmentNideIDs(eventAssignmentCount + 1, internHeadline, presentedHeadline);
		if (variable != null) {
			setEventAssignmentVariable(g, internHeadline, variable, eventAssignmentCount + 1);
		}
		if (function != null && function != "") {
			setEventAssignmentFunction(g, internHeadline, function, eventAssignmentCount + 1);
		}
	}
	
	public static void addEventAssignmentToEvent(Graph g, String id, EventAssignment eventAssignment) {
		String internHeadline = returnEventWithID(g, id);
		char index = internHeadline.charAt(internHeadline.length() - 1);
		String presentedHeadline = new StringBuffer("SBML Event ")
				.append(index).toString();
		
		int eventAssignmentCount = eventAssignmentCount(g, internHeadline);
		initEventAssignmentNideIDs(eventAssignmentCount + 1, internHeadline, presentedHeadline);
		if (eventAssignment.isSetVariable()) {
			setEventAssignmentVariable(g, internHeadline, eventAssignment.getVariable(), eventAssignmentCount + 1);
		}
		if (eventAssignment.isSetMath()) {
			setEventAssignmentFunction(g, internHeadline, eventAssignment.getMath().toFormula(), eventAssignmentCount + 1);
		}
	}
	
	public static void deleteEventAssignment(Graph g, String id, String variable) {
		String internHeadline = returnEventWithID(g, id);
		char index = internHeadline.charAt(internHeadline.length() - 1);
		String presentedHeadline = new StringBuffer("SBML Event ")
				.append(index).toString();
		int eventAssignmentCount = eventAssignmentCount(g, internHeadline);
		
		for (int i = eventAssignmentCount; i >= 1; i--) {
			if (isEventAssignmentVariable(g, internHeadline, i)) {
				System.out.println("variable: " + variable);
				System.out.println("Wert: " + getEventAssignmentVariable(g, internHeadline, i));
				if (variable.equals(getEventAssignmentVariable(g, internHeadline, i))) {
					AttributeHelper.deleteAttribute(g, internHeadline, new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(i)
							.append(SBML_Constants.VARIABLE).toString());
					if (isEventAssignmentFunction(g, internHeadline, i)) {
						AttributeHelper.deleteAttribute(g, internHeadline, new StringBuffer(internHeadline)
								.append(SBML_Constants.EVENT_ASSIGNMENT)
								.append(i)
								.append(SBML_Constants.FUNCTION).toString());
					}
				}
			}
		}
	}
	
	public static void deleteEvent(Graph g, String id) {
		deleteEventUseValuesFromTriggerTime(g, id);
		deleteEventName(g, id);
		deleteTrigger(g, id);
		deleteDelayFunction(g, id);
		deletePriority(g, id);
		
		String internHeadline = returnEventWithID(g, id);
		int eventAssignmentCount = eventAssignmentCount(g, internHeadline);
		for (int i = eventAssignmentCount; i >= 1; i--) {
			deleteEventAssignment(g, id, getEventAssignmentVariable(g, internHeadline, i));
		}
		deleteEventID(g, id);
	}
	
	/**
	 * Set the value of the attribute UseValuesFromTriggerTime
	 * 
	 * @param g
	 *        the graph where the information should be read in
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param useValuesFromTriggerTime
	 *        the value that will be set
	 */
	private static void setEventUseValuesFromTriggerTime(Graph g,
			String internHeadline, Boolean useValuesFromTriggerTime) {
		if (!useValuesFromTriggerTime.equals(null)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
							.toString(), useValuesFromTriggerTime);
		}
	}
	
	/**
	 * Set the value of the attribute id
	 * 
	 * @param g
	 *        the graph where the information should be read in
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param id
	 *        the value that will be set
	 */
	private static void setEventID(Graph g, String internHeadline, String ID) {
		if (!ID.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.EVENT_ID).toString(),
					ID);
		}
	}
	
	/**
	 * Set the value of the attribute name
	 * 
	 * @param g
	 *        the graph where the information should be read in
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param name
	 *        the value that will be set
	 */
	private static void setEventName(Graph g, String internHeadline, String name) {
		if (!name.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.EVENT_NAME)
					.toString(), name);
		}
	}
	
	/**
	 * Indicate if the attribute name is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if the name is set. Else false
	 */
	private static Boolean isEventName(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.EVENT_NAME).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicate if the attribute id is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if the id is set. Else false
	 */
	private static Boolean isEventID(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.EVENT_ID).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicate if the attribute UseValuesFromTriggerTime is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if UseValuesFromTriggerTime is set. Else false
	 */
	private static Boolean isEventUseValuesFromTriggerTime(Graph g,
			String internHeadline) {
		if (AttributeHelper
				.hasAttribute(
						g,
						internHeadline,
						new StringBuffer(internHeadline).append(
								SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
								.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return the value of the attribute id
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the id if it is set. Else the empty string
	 */
	private static String getEventID(Graph g, String internHeadline) {
		if (isEventID(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.EVENT_ID).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Return the value of the attribute name
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the name if it is set. Else the empty string
	 */
	private static String getEventName(Graph g, String internHeadline) {
		if (isEventName(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.EVENT_NAME).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Return the value of the attribute UseValuesFromTriggerTime
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return UseValuesFromTriggerTime if it is set. Else the empty string
	 */
	private static Boolean getEventUseValuesFromTriggerTime(Graph g,
			String internHeadline) {
		if (isEventUseValuesFromTriggerTime(g, internHeadline)) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
							.toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the initial value
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param initialValue
	 *        the value that will be set
	 */
	private static void setTriggerInitialValue(Graph g, String internHeadline,
			Boolean initialValue) {
		if (!initialValue.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.INITIAL_VALUE)
					.toString(), initialValue);
		}
	}
	
	/**
	 * Sets the attribute persistent
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param persistent
	 *        the value that will be set
	 */
	private static void setTriggerPersistent(Graph g, String internHeadline,
			Boolean persistent) {
		if (!persistent.equals(null)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PERSISTENT)
					.toString(), persistent);
		}
	}
	
	/**
	 * Sets the function
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param function
	 *        the String that will be set
	 */
	private static void setTriggerFunction(Graph g, String internHeadline,
			String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.TRIGGER_FUNCTION)
					.toString(), function);
		}
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if the function is set. Else false
	 */
	private static Boolean isTriggerFunction(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.TRIGGER_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the initial value is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if the initial value is set. Else false
	 */
	private static Boolean isTriggerInitialValue(Graph g, String internHeadline) {
		if (AttributeHelper
				.hasAttribute(g, internHeadline, new StringBuffer(
						internHeadline).append(SBML_Constants.INITIAL_VALUE)
						.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if persistent is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if persistent is set. Else false
	 */
	private static Boolean isTriggerPersistent(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PERSISTENT).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return the function string
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the function string if it is set. Else the empty string
	 */
	private static String getTriggerFunction(Graph g, String internHeadline) {
		if (isTriggerFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.TRIGGER_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Return initial value
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the initial value if it is set. Else null
	 */
	private static Boolean getTriggerInitialValue(Graph g, String internHeadline) {
		if (isTriggerInitialValue(g, internHeadline)) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.INITIAL_VALUE).toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Return the value of the attribute persistent
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the value persistent if it is set. Else null
	 */
	private static Boolean getTriggerPersistent(Graph g, String internHeadline) {
		if (isTriggerPersistent(g, internHeadline)) {
			return (Boolean) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PERSISTENT).toString());
		} else {
			return null;
		}
	}
	
	/**
	 * Set the function of the current priority
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param function
	 *        the value that will be set
	 */
	private static void setPriorityFunction(Graph g, String internHeadline,
			String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.PRIORITY_FUNCTION)
					.toString(), function);
		}
	}
	
	/**
	 * Indicates if the function is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if the function is set. Else false
	 */
	private static Boolean isPriorityFunction(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.PRIORITY_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the function string of priority
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the function string if it is set. Else the empty string
	 */
	private static String getPriorityFunction(Graph g, String internHeadline) {
		if (isPriorityFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.PRIORITY_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Sets the value of a function
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param function
	 *        the value that will be set
	 */
	private static void setDelayFunction(Graph g, String internHeadline,
			String function) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(g, internHeadline, new StringBuffer(
					internHeadline).append(SBML_Constants.DELAY_FUNCTION)
					.toString(), function);
		}
	}
	
	/**
	 * Indicates if the value of function is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return true if the function is set. Else false
	 */
	private static Boolean isDelayFunction(Graph g, String internHeadline) {
		if (AttributeHelper.hasAttribute(g, internHeadline, new StringBuffer(
				internHeadline).append(SBML_Constants.DELAY_FUNCTION)
				.toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the value of the function
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return the value of the function if it is set. Else the empty string
	 */
	private static String getDelayFunction(Graph g, String internHeadline) {
		if (isDelayFunction(g, internHeadline)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline).append(
							SBML_Constants.DELAY_FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Set the variable of an event assignment
	 * 
	 * @param g
	 *        the graph where the information should be read in
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param variable
	 *        the value that will be set
	 * @param eventAssignmentCount
	 *        the number of the current event assignment
	 */
	private static void setEventAssignmentVariable(Graph g,
			String internHeadline, String variable, int eventAssignmentCount) {
		if (!variable.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.VARIABLE).toString(),
					variable);
		}
	}
	
	/**
	 * Set the function of an event assignment
	 * 
	 * @param g
	 *        the graph where the information should be read in
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param function
	 *        the value that will be set
	 * @param eventAssignmentCount
	 *        the number of the current event assignment
	 */
	private static void setEventAssignmentFunction(Graph g,
			String internHeadline, String function, int eventAssignmentCount) {
		if (!function.equals(SBML_Constants.EMPTY)) {
			AttributeHelper.setAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.FUNCTION).toString(),
					function);
		}
	}
	
	/**
	 * Indicates if the variable of an event assignment is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param eventAssignmentCount
	 *        the number of the current event assignment
	 * @return true if the variable is set. Else false
	 */
	private static Boolean isEventAssignmentVariable(Graph g,
			String internHeadline, int eventAssignmentCount) {
		if (AttributeHelper.hasAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.VARIABLE).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Indicates if the function of an event assignment is set
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param eventAssignmentCount
	 *        the number of the current event assignment
	 * @return true if the function is set. Else false
	 */
	private static Boolean isEventAssignmentFunction(Graph g,
			String internHeadline, int eventAssignmentCount) {
		if (AttributeHelper.hasAttribute(
				g,
				internHeadline,
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.FUNCTION).toString())) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return the variable of an event assignment
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param eventAssignmentCount
	 *        the number of the current event assignment
	 * @return the variable if it is set. Else the empty string
	 */
	private static String getEventAssignmentVariable(Graph g,
			String internHeadline, int eventAssignmentCount) {
		if (isEventAssignmentVariable(g, internHeadline, eventAssignmentCount)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.VARIABLE).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Return the function of an event assignment
	 * 
	 * @param g
	 *        the graph where the information should be read from
	 * @param internHeadline
	 *        contains the number of the current event
	 * @param eventAssignmentCount
	 *        the number of the current event assignment
	 * @return the function if it is set. Else the empty string
	 */
	private static String getEventAssignmentFunction(Graph g,
			String internHeadline, int eventAssignmentCount) {
		if (isEventAssignmentFunction(g, internHeadline, eventAssignmentCount)) {
			return (String) attWriter.getAttribute(
					g,
					internHeadline,
					new StringBuffer(internHeadline)
							.append(SBML_Constants.EVENT_ASSIGNMENT)
							.append(eventAssignmentCount)
							.append(SBML_Constants.FUNCTION).toString());
		} else {
			return SBML_Constants.EMPTY;
		}
	}
	
	/**
	 * Returns a list of all event headlines in the graph
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @return list of headlines
	 */
	private static ArrayList<String> getEventHeadlines(Graph g) {
		SBML_SBase_Writer writer = new SBML_SBase_Writer();
		return writer.headlineHelper(g, SBML_Constants.SBML_EVENT);
	}
	
	/**
	 * Returns a JSBML Event object
	 * 
	 * @param g
	 *        the graph where the information can be found
	 * @param internHeadline
	 *        contains the number of the current event
	 * @return an Event object
	 */
	private static Event getEvent(Graph g, String internHeadline) {
		Event event = new Event();
		event.setLevel(3);
		event.setVersion(1);
		if (isEventID(g, internHeadline)) {
			event.setId(getEventID(g, internHeadline));
		}
		if (isEventName(g, internHeadline)) {
			event.setName(getEventName(g, internHeadline));
		}
		if (isEventUseValuesFromTriggerTime(g, internHeadline)) {
			Boolean useValues = getEventUseValuesFromTriggerTime(g,
					internHeadline);
			event.setUseValuesFromTriggerTime(useValues);
		}
		Trigger trigger = new Trigger(3, 1);
		if (isTriggerInitialValue(g, internHeadline)) {
			trigger.setInitialValue(getTriggerInitialValue(g, internHeadline));
		}
		if (isTriggerPersistent(g, internHeadline)) {
			trigger.setPersistent(getTriggerPersistent(g, internHeadline));
		}
		try {
			if (isTriggerFunction(g, internHeadline)) {
				trigger.setFormula(getTriggerFunction(g, internHeadline));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		event.setTrigger(trigger);
		if (isPriorityFunction(g, internHeadline)) {
			Priority prio = new Priority();
			try {
				prio.setFormula(getPriorityFunction(g, internHeadline));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			event.setPriority(prio);
		}
		if (isDelayFunction(g, internHeadline)) {
			Delay delay = new Delay();
			try {
				delay.setFormula(getDelayFunction(g, internHeadline));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			event.setDelay(delay);
		}
		
		int eaCount = eventAssignmentCount(g, internHeadline);
		for (int i = 1; i <= eaCount; i++) {
			EventAssignment ea = new EventAssignment();
			ea.setVariable(getEventAssignmentVariable(g, internHeadline, i));
			try {
				ea.setFormula(getEventAssignmentFunction(g, internHeadline, i));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			event.addEventAssignment(ea);
		}
		return event;
	}
	
	/**
	 * Returns the number of event assignments of an event
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadline
	 *        a list which contains the number of the current event
	 * @return the number of event assignments
	 */
	private static int eventAssignmentCount(Graph g, String internHeadline) {
		int eaCount = 0;
		while (isEventAssignmentVariable(g, internHeadline, eaCount + 1)) {
			eaCount++;
		}
		return eaCount;
	}
	
	/**
	 * Returns a list of event with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current event
	 * @return a filled list of event
	 */
	private static List<Event> getAllEvent(Graph g, List<String> internHeadlines) {
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Event> eventList = new ArrayList<Event>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			eventList.add(getEvent(g, internHeadline));
		}
		return eventList;
	}
	
	/**
	 * Returns a list of event with distinct headlines
	 * 
	 * @param g
	 *        contains the information
	 * @param internHeadlines
	 *        a list which contains the number of the current event
	 * @return a filled list of event
	 */
	public static List<Event> getAllEvent(Graph g) {
		List<String> internHeadlines = getEventHeadlines(g);
		Iterator<String> internHeadlinesIt = internHeadlines.iterator();
		List<Event> eventList = new ArrayList<Event>();
		while (internHeadlinesIt.hasNext()) {
			String internHeadline = internHeadlinesIt.next();
			eventList.add(getEvent(g, internHeadline));
		}
		return eventList;
	}
	
	private static void initCompartmentNideIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.COMPARTMENT_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.COMPARTMENT_NAME).toString(),
				presentedHeadline + ": Name");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.SPATIAL_DIMENSIONS).toString(),
				presentedHeadline + ": Spatial Dimensions");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SIZE)
						.toString(), presentedHeadline + ": Size");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.UNITS)
						.toString(), presentedHeadline + ": Units");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.CONSTANT).toString(),
				presentedHeadline + ": Constant");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.OUTSIDE)
						.toString(), presentedHeadline + ": Outside");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		
	}
	
	private static void initParameterNideIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PARAMETER_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PARAMETER_NAME).toString(),
				presentedHeadline + ": Name");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.VALUE)
						.toString(), presentedHeadline + ": Value");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PARAMETER_UNITS).toString(),
				presentedHeadline + ": Units");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PARAMETER_CONSTANT).toString(),
				presentedHeadline + ": Constant");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
	}
	
	private static void initInitialAssignmentNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SYMBOL)
						.toString(), presentedHeadline + ": Symbol");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.INITIAL_ASSIGNMENT_FUNCTION).toString(),
				presentedHeadline + ": Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		
	}
	
	private static void initFunctionDefinitionNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		AttributeHelper
				.setNiceId(
						new StringBuffer(internHeadline).append(
								SBML_Constants.FUNCTION_DEFINITION_FUNCTION)
								.toString(), presentedHeadline + ": Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.FUNCTION_DEFINITION_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.FUNCTION_DEFINITION_NAME).toString(),
				presentedHeadline + ": Name");
	}
	
	private static void initUnitDefinitionNideIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.UNIT_DEFINITION_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.UNIT_DEFINITION_NAME).toString(),
				presentedHeadline + ": Name");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
	}
	
	private static void initSubUnitDefinitionNideIDs(String internHeadline, String presentedHeadline, int subUnitCount) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append("_").toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.UNIT)
						.toString(), new StringBuffer(presentedHeadline)
						.append(": Unit").toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.META_ID).toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).append(" Meta ID").toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.SBOTERM).toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).append(" SBOTerm").toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.SUB_UNIT).append(subUnitCount)
						.append(SBML_Constants.NOTES).toString(),
				new StringBuffer(presentedHeadline).append(": Sub Unit ")
						.append(subUnitCount).append(" Notes").toString());
	}
	
	private static void initConstraintNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.CONSTRAINT).toString(),
				presentedHeadline + ": Constraint");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.MESSAGE)
						.toString(), presentedHeadline + ": Message");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		
	}
	
	private static void initAssignmnetRuleNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.ASSIGNMENT_VARIABLE).toString(),
				presentedHeadline + ": Variable");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.ASSIGNMENT_FUNCTION).toString(),
				presentedHeadline + ": Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		
	}
	
	private static void initRateRuleNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.RATE_VARIABLE).toString(),
				presentedHeadline + ": Variable");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.RATE_FUNCTION).toString(),
				presentedHeadline + ": Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		
	}
	
	private static void initAlgebraicRuleNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.ALGEBRAIC_FUNCTION).toString(),
				presentedHeadline + ": Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		
	}
	
	private static void initEventNiceIDs(String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ID).toString(),
				presentedHeadline + ": ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.EVENT_NAME).toString(),
				presentedHeadline + ": Name");
		AttributeHelper
				.setNiceId(
						new StringBuffer(internHeadline).append(
								SBML_Constants.USE_VALUES_FROM_TRIGGER_TIME)
								.toString(), presentedHeadline
								+ ": UseValuesFromTriggerTime");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.META_ID)
						.toString(), presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.NOTES)
						.toString(), presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(SBML_Constants.SBOTERM)
						.toString(), presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.INITIAL_VALUE).toString(),
				presentedHeadline + ": Trigger Initial Value");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PERSISTENT).toString(),
				presentedHeadline + ": Trigger Persistent");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.TRIGGER_FUNCTION).toString(),
				presentedHeadline + ": Trigger Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_META_ID).toString(),
				presentedHeadline + ": Priority Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_NOTES).toString(),
				presentedHeadline + ": Priority Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_SBOTERM).toString(),
				presentedHeadline + ": Priority SBOTerm");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.PRIORITY_FUNCTION).toString(),
				presentedHeadline + ": Priority Function");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_META_ID).toString(),
				presentedHeadline + ": Delay Meta ID");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_NOTES).toString(),
				presentedHeadline + ": Delay Notes");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_SBOTERM).toString(),
				presentedHeadline + ": Delay SBOTerm");
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline).append(
						SBML_Constants.DELAY_FUNCTION).toString(),
				presentedHeadline + ": Delay Function");
	}
	
	private static void initEventAssignmentNideIDs(int eventAssignmentCount, String internHeadline, String presentedHeadline) {
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.VARIABLE).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Variable");
		
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.FUNCTION).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Function");
		
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.META_ID).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Meta ID");
		
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.SBOTERM).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " SBOTerm");
		
		AttributeHelper.setNiceId(
				new StringBuffer(internHeadline)
						.append(SBML_Constants.EVENT_ASSIGNMENT)
						.append(eventAssignmentCount)
						.append(SBML_Constants.NOTES).toString(),
				presentedHeadline + ": Event Assignment "
						+ eventAssignmentCount + " Notes");
		
	}
	
	private static void initReactionNideIDs(String internHeadline,
			String presentedHeadline) {
		// AttributeHelper.setNiceId(SBML_Constants.REACTION_NAME,
		// presentedHeadline+": Name");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_ID, presentedHeadline
				+ ": ID");
		AttributeHelper.setNiceId(SBML_Constants.REVERSIBLE, presentedHeadline
				+ ": Reversible");
		AttributeHelper.setNiceId(SBML_Constants.FAST, presentedHeadline
				+ ": Fast");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_COMPARTMENT,
				presentedHeadline + ": Compartment ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_SBOTERM,
				presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.STOICHIOMETRY,
				presentedHeadline + ": Stoichiometry");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_CONSTANT,
				presentedHeadline + ": Constant");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES, presentedHeadline
				+ ": Species");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_REFERENCE_NAME,
				presentedHeadline + ": Species Reference Name");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_REFERENCE_ID,
				presentedHeadline + ": Species Reference ID");
		AttributeHelper.setNiceId(SBML_Constants.PRODUCT_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.PRODUCT_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.PRODUCT_SBOTERM,
				presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.REACTANT_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTANT_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.REACTANT_SBOTERM,
				presentedHeadline + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.MODIFIER_META_ID,
				presentedHeadline + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.MODIFIER_NOTES,
				presentedHeadline + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.MODIFIER_SBOTERM,
				presentedHeadline + ": SBOTerm");
	}
	
	private static void initKineticLawNideIDs() {
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_NOTES,
				"SBML Kinetic Law: Notes");
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_META_ID,
				"SBML Kinetic Law: Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_SBOTERM,
				"SBML Kinetic Law: SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.KINETIC_LAW_FUNCTION,
				"SBML Kinetic Law: Function");
	}
	
	private static void initLocalParameterNideIDs(String presentedAttributeName,
			String internAttributeName, String presentedHeadline) {
		presentedHeadline = "SBML " + presentedHeadline;
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.META_ID).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Meta ID")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.SBOTERM).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" SBOTerm")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.NOTES).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Notes")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_ID).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" ID")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_NAME).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Name")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_VALUE).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Value")
						.toString());
		AttributeHelper.setNiceId(
				new StringBuffer(internAttributeName).append(
						SBML_Constants.LOCAL_PARAMETER_UNITS).toString(),
				new StringBuffer(presentedHeadline).append(": ")
						.append(presentedAttributeName).append(" Units")
						.toString());
	}
	
	public static boolean isSetLayoutID(Graph g, Node node) {
		if (AttributeHelper.hasAttribute(node, SBML_Constants.SBML,
				SBML_Constants.SBML_LAYOUT_ID)) {
			return true;
		}
		return false;
	}
	
}
