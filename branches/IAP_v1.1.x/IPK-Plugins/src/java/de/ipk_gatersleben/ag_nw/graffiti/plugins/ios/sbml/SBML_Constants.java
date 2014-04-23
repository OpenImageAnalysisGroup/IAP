package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;

public class SBML_Constants {
	
	private static boolean _isInitialized = false;
	
	/**
	 * switch, to activate readout of layout information
	 */
	public static final boolean isLayoutActive = false;
	
	// Helpful constants
	public static final String EMPTY = "";
	public static final String UNDERLINE = "_";
	private static final String ATT = AttributeHelper.attributeSeparator;
	
	public static final String SBML_LAYOUT_ID = "sbml_layout_id";
	
	public static final String LOCALPARAMETER_HEADLINE = "Local Parameter ";
	public static final String COMARTMENT_HEADLINE = "SBML Compartment ";
	public static final String SBML_HEADLINE = "SBML";
	
	public static final String ROLE_REACTANT = "reactant";
	public static final String ROLE_PRODUCT = "product";
	public static final String ROLE_MODIFIER = "modifier";
	public static final String ROLE_SPECIES = "species";
	public static final Object ROLE_REACTION = "reaction";
	public static final String SBML_ROLE = "sbmlRole";
	
	public static final String SBOTERM = "_sboterm";
	public static final String META_ID = "_meta_id";
	public static final String NOTES = "_notes";
	public static final String ANNOTATION = "_annotation";
	public static final String NON_RDF_ANNOTATION = "_non_rdf_annotation";
	
	// SBML attribute constants
	public static final String VERSION = "version";
	public static final String LEVEL = "level";
	public static final String NAMESPACE = "namespace";
	public static final String SBML = "sbml";
	public static final String SBML_NOTES = "sbml_notes";
	public static final String SBML_META_ID = "sbml_meta_id";
	public static final String SBML_SBOTERM = "sbml_sboterm";
	public static final String SBML_ANNOTATION = "sbml_annotation";
	public static final String SBML_NON_RDF_ANNOTATION = "sbml_non_rdf_annotation";
	
	// Model attribute constants
	public static final String MODEL_ID = "model_sbml_id";
	public static final String MODEL_NAME = "model_name";
	public static final String SUBSTANCE_UNITS = "substance_units";
	public static final String TIME_UNITS = "time_units";
	public static final String VOLUME_UNITS = "volume_units";
	public static final String AREA_UNITS = "area_units";
	public static final String LENGTH_UNITS = "length_units";
	public static final String EXTENT_UNITS = "extent_units";
	public static final String CONVERSION_FACTOR = "conversion_factor";
	public static final String MODEL_NOTES = "model_notes";
	public static final String MODEL_META_ID = "model_meta_id";
	public static final String MODEL_SBOTERM = "model_sboterm";
	public static final String MODEL_ANNOTATION = "model_annotation";
	public static final String MODEL_NON_RDF_ANNOTATION = "model_non_rdf_annotation";
	
	// FunctionDefinition attribute constants
	public static final String SBML_FUNCTION_DEFINITION = "sbml_function_definition_";
	public static final String FUNCTION_DEFINITION_FUNCTION = "_function";
	public static final String FUNCTION_DEFINITION_ID = "_id";
	public static final String FUNCTION_DEFINITION_NAME = "_name";
	
	// UnitDefinition attribute constants
	public static final String SBML_UNIT_DEFINITION = "sbml_unit_definition_";
	public static final String UNIT_DEFINITION_ID = "_id";
	public static final String UNIT_DEFINITION_NAME = "_name";
	public static final String SUB_UNIT = "_sub_unit_";
	public static final String UNIT = "unit";
	
	// Compartment attribute constants
	public static final String SBML_COMPARTMENT = "sbml_compartment_";
	public static final String COMPARTMENT_ID = "_id";
	public static final String COMPARTMENT_NAME = "_name";
	public static final String SPATIAL_DIMENSIONS = "_spatial_Dimensions";
	public static final String SIZE = "_size";
	public static final String UNITS = "_units";
	public static final String CONSTANT = "_constant";
	public static final String OUTSIDE = "_outside"; // Level 2
	
	// Species attribute constants
	public static final String SPECIES_ID = "species_id";
	public static final String SPECIES_NAME = "species_name";
	public static final String COMPARTMENT = "compartment";
	public static final String INITIAL_AMOUNT = "initial_amount";
	public static final String INITIAL_CONCENTRATION = "initial_concentration";
	public static final String SPECIES_SUBSTANCE_UNITS = "species_substance_units";
	public static final String HAS_ONLY_SUBSTANCE_UNITS = "has_only_substance_units";
	public static final String BOUNDARY_CONDITION = "boundary_condition";
	public static final String SPECIES_CONSTANT = "_species_constant";
	public static final String SPECIES_CONVERSION_FACTOR = "species_conversion_factor";
	public static final String SPECIES_META_ID = "species_meta_id";
	public static final String SPECIES_SBOTERM = "species_sboterm";
	public static final String SPECIES_NOTES = "species_notes";
	public static final String CHARGE = "charge"; // Level 2
	public static final String SPECIES_COMPARTMENT_NAME = "compartment_name";
	public static final String SPECIES_ANNOTATION = "species_annotation";
	public static final String SPECIES_NON_RDF_ANNOTATION = "species_non_rdf_annotation";
	
	// Parameter attribute constants
	public static final String SBML_PARAMETER = "sbml_parameter_";
	public static final String PARAMETER_ID = "_id";
	public static final String PARAMETER_NAME = "_name";
	public static final String VALUE = "_value";
	public static final String PARAMETER_UNITS = "_units";
	public static final String PARAMETER_CONSTANT = "_constant";
	
	// InitialAssignment attribute constants
	public static final String SBML_INITIAL_ASSIGNMENT = "sbml_initial_assignment_";
	public static final String SYMBOL = "_symbol";
	public static final String INITIAL_ASSIGNMENT_FUNCTION = "_function";
	
	// Rule attribute constants
	public static final String SBML_RATE_RULE = "sbml_rate_rule_";
	public static final String SBML_ASSIGNMENT_RULE = "sbml_assignment_rule_";
	public static final String SBML_ALGEBRAIC_RULE = "sbml_algebraic_rule_";
	public static final String ASSIGNMENT_VARIABLE = "_assignmnet_variable";
	public static final String RATE_VARIABLE = "_rate_variable";
	public static final String RATE_FUNCTION = "_function";
	public static final String ASSIGNMENT_FUNCTION = "_function";
	public static final String ALGEBRAIC_FUNCTION = "_function";
	
	// Constraint attribute constants
	public static final String SBML_CONSTRAINT = "sbml_constraint_";
	public static final String CONSTRAINT = "_constraint";
	public static final String MESSAGE = "_message";
	
	// Reaction attribute constants
	public static final String REACTION_ID = "reaction_id";
	public static final String REACTION_NAME = "reaction_name";
	public static final String REACTION_COMPARTMENT = "reaction_compartment";
	public static final String REACTION_CONSTANT = "reaction_constant";
	public static final String LOCAL_PARAMETER = "local_parameter_";
	public static final String FAST = "fast";
	public static final String REVERSIBLE = "reversible";
	public static final String STOICHIOMETRY = "stoichiometry";
	public static final String SPECIES = "species";
	public static final String SPECIES_REFERENCE_ID = "species_reference_id";
	public static final String SPECIES_REFERENCE_NAME = "species_reference_name";
	public static final String SBML_KINETIC_LAW = "sbml_kinetic_law";
	public static final String REACTION_META_ID = "reaction_meta_id";
	public static final String REACTION_SBOTERM = "reaction_sboterm";
	public static final String REACTION_NOTES = "reaction_notes";
	public static final String REACTANT_META_ID = "reactant_meta_id";
	public static final String REACTANT_SBOTERM = "reactant_sboterm";
	public static final String REACTANT_NOTES = "reactant_notes";
	public static final String PRODUCT_META_ID = "product_meta_id";
	public static final String PRODUCT_SBOTERM = "product_sboterm";
	public static final String PRODUCT_NOTES = "product_notes";
	public static final String MODIFIER_META_ID = "modifier_meta_id";
	public static final String MODIFIER_SBOTERM = "modifier_sboterm";
	public static final String MODIFIER_NOTES = "modifier_notes";
	public static final String KINETIC_LAW_NOTES = "kinetic_law_notes";
	public static final String KINETIC_LAW_SBOTERM = "kinetic_law_sboterm";
	public static final String KINETIC_LAW_META_ID = "kinetic_law_meta_id";
	public static final String KINETIC_LAW_FUNCTION = "kinetic_law_function";
	public static final String LOCAL_PARAMETER_ID = "_id";
	public static final String LOCAL_PARAMETER_NAME = "_name";
	public static final String LOCAL_PARAMETER_VALUE = "_value";
	public static final String LOCAL_PARAMETER_UNITS = "_units";
	public static final String REACTION_ANNOTATION = "reaction_annotation";
	public static final String REACTION_NON_RDF_ANNOTATION = "reaction_non_rdf_annotation";
	public static final String REACTANT_ANNOTATION = "reactant_annotation";
	public static final String REACTANT_NON_RDF_ANNOTATION = "reactant_non_rdf_annotation";
	public static final String PRODUCT_ANNOTATION = "product_annotation";
	public static final String PRODUCT_NON_RDF_ANNOTATION = "product_non_rdf_annotation";
	public static final String MODIFIER_ANNOTATION = "modifier_annotation";
	public static final String MODIFIER_NON_RDF_ANNOTATION = "modifier_non_rdf_annotation";
	public static final String KINETIC_LAW_ANNOTATION = "kinetic_law_annotation";
	public static final String KINETIC_LAW_NON_RDF_ANNOTATION = "kinetic_law_non_rdf_annotation";
	
	// Event attribute constants
	public static final String SBML_EVENT = "sbml_event_";
	public static final String EVENT_ID = "_event_id";
	public static final String EVENT_NAME = "_event_name";
	public static final String USE_VALUES_FROM_TRIGGER_TIME = "_use_values_from_trigger_time";
	public static final String PRIORITY_META_ID = "_priority_meta_id";
	public static final String PRIORITY_NOTES = "_priority_notes";
	public static final String PRIORITY_SBOTERM = "_priority_sboterm";
	public static final String PRIORITY_FUNCTION = "_priority_function";
	public static final String PRIORITY_ANNOTATION = "_priority_annotation";
	public static final String PRIORITY_NON_RDF_ANNOTATION = "_priority_non_rdf_annotation";
	public static final String DELAY_META_ID = "_delay_meta_id";
	public static final String DELAY_NOTES = "_delay_notes";
	public static final String DELAY_SBOTERM = "_delay_sboterm";
	public static final String DELAY_ANNOTATION = "_delay_annotation";
	public static final String DELAY_NON_RDF_ANNOTATION = "_delay_non_rdf_annotation";
	public static final String EVENT_ASSIGNMENT = "_event_assignment_";
	public static final String DELAY_FUNCTION = "_delay_function";
	public static final String TRIGGER_FUNCTION = "_trigger_function";
	public static final String PERSISTENT = "_persistent";
	public static final String INITIAL_VALUE = "_initial_value";
	public static final String VARIABLE = "_variable";
	public static final String FUNCTION = "_function";
	
	// those stings only used with attached numbers.
	public static final String SBML_SPECIES = "sbml_species";
	public static final String SBML_Cluster = "cluster";
	public static final String SBML_Label = "Label";
	
	public static void init() {
		if (!_isInitialized) {
			AttributeHelper.setNiceId(SBML_LAYOUT_ID, "SBML: Layout ID");
			// initialize SBML niceIds
			AttributeHelper.setNiceId(LEVEL, "SBML: Level");
			AttributeHelper.setNiceId(VERSION, "SBML: Version");
			AttributeHelper.setNiceId(NAMESPACE, "SBML: Namespace");
			AttributeHelper.setNiceId(SBML_META_ID, "SBML: SBML Meta ID");
			AttributeHelper.setNiceId(SBML_NOTES, "SBML: SBML Notes");
			AttributeHelper.setNiceId(SBML_SBOTERM, "SBML: SBML SBOTerm");
			
			// initialize Model niceId
			
			AttributeHelper.setNiceId(MODEL_ID, "SBML: Model ID");
			AttributeHelper.setNiceId(MODEL_NAME, "SBML: Model Name");
			AttributeHelper.setNiceId(SUBSTANCE_UNITS, "SBML: Substance Units");
			AttributeHelper.setNiceId(TIME_UNITS, "SBML: Time Units");
			AttributeHelper.setNiceId(VOLUME_UNITS, "SBML: Volume Units");
			AttributeHelper.setNiceId(AREA_UNITS, "SBML: Area Units");
			AttributeHelper.setNiceId(LENGTH_UNITS, "SBML: Lenght Units");
			AttributeHelper.setNiceId(EXTENT_UNITS, "SBML: Extent Units");
			AttributeHelper.setNiceId(CONVERSION_FACTOR, "SBML: Conversion Faktor");
			AttributeHelper.setNiceId(MODEL_META_ID, "SBML: Model Meta ID");
			AttributeHelper.setNiceId(MODEL_NOTES, "SBML: Model Notes");
			AttributeHelper.setNiceId(MODEL_SBOTERM, "SBML: Model SBOTerm");
			
			/*
			 * List of Attributes to hide from the information panel in Vanted
			 */
			Collection<String> colDiscardedRowIDs = DefaultEditPanel
					.getDiscardedRowIDs();
			HashSet<String> discardedRowIDs = new HashSet<String>(
					colDiscardedRowIDs);
			discardedRowIDs.add(MODEL_META_ID);
//			discardedRowIDs.add(SBML_NOTES);
//			discardedRowIDs.add(MODEL_NOTES);
//			discardedRowIDs.add(REACTION_NOTES);
//			discardedRowIDs.add(SPECIES_NOTES);
//			discardedRowIDs.add(REACTANT_NOTES);
//			discardedRowIDs.add(PRODUCT_NOTES);
//			discardedRowIDs.add(MODIFIER_NOTES);
//			discardedRowIDs.add(KINETIC_LAW_NOTES);
//			discardedRowIDs.add(PRIORITY_NOTES);
//			discardedRowIDs.add(DELAY_NOTES);
			discardedRowIDs.add(SBML_ANNOTATION);
			discardedRowIDs.add(SBML_NON_RDF_ANNOTATION);
			discardedRowIDs.add(MODEL_ANNOTATION);
			discardedRowIDs.add(MODEL_NON_RDF_ANNOTATION);
			discardedRowIDs.add(SPECIES_ANNOTATION);
			discardedRowIDs.add(SPECIES_NON_RDF_ANNOTATION);
			discardedRowIDs.add(REACTION_ANNOTATION);
			discardedRowIDs.add(REACTION_NON_RDF_ANNOTATION);
			discardedRowIDs.add(REACTANT_ANNOTATION);
			discardedRowIDs.add(REACTANT_NON_RDF_ANNOTATION);
			discardedRowIDs.add(PRODUCT_ANNOTATION);
			discardedRowIDs.add(PRODUCT_NON_RDF_ANNOTATION);
			discardedRowIDs.add(MODIFIER_ANNOTATION);
			discardedRowIDs.add(MODIFIER_NON_RDF_ANNOTATION);
			discardedRowIDs.add(KINETIC_LAW_ANNOTATION);
			discardedRowIDs.add(KINETIC_LAW_NON_RDF_ANNOTATION);
			discardedRowIDs.add(SBML_LAYOUT_ID);
			discardedRowIDs.add(SBML_ROLE);
			DefaultEditPanel.setDiscardedRowIDs(discardedRowIDs);
			
			/*
			 * AttributeHelper.setNiceId(SBML_Constants.sbml+ATT+SBML_Constants.meta_id
			 * , "SBML: Meta ID");
			 * AttributeHelper.setNiceId(SBML_Constants.sbml+ATT+
			 * SBML_Constants.SBOTERM, "SBML: SBOTerm");
			 * AttributeHelper.setNiceId(SBML_Constants
			 * .sbml+ATT+SBML_Constants.NAMESPACE, "SBML: Namespaces");
			 * AttributeHelper
			 * .setNiceId(SBML_Constants.sbml+ATT+SBML_Constants.TOOLTIP,
			 * "SBML: ToolTip");
			 * AttributeHelper.setNiceId(SBML_Constants.sbml+ATT+SBML_Constants.LEVEL
			 * ,"SBML:  Level");
			 * AttributeHelper.setNiceId(SBML_Constants.sbml+ATT+SBML_Constants
			 * .VERSION,"SBML: Version");
			 * AttributeHelper.setNiceId(SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .ID,"SBML Model"+ATT+"ID");
			 * AttributeHelper.setNiceId(SBML_Constants.sbml_model
			 * +ATT+SBML_Constants.NAME,"SBML Model"+ATT+"Name");
			 * AttributeHelper.setNiceId
			 * (SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .SUBSTANCE_UNITS,"SBML Model"+ATT+"Substance Units");
			 * AttributeHelper.
			 * setNiceId(SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .TIME_UNITS,"SBML Model"+ATT+"Time Units");
			 * AttributeHelper.setNiceId(
			 * SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .VOLUME_UNITS,"SBML Model"+ATT+"Volume Units");
			 * AttributeHelper.setNiceId
			 * (SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .AREA_UNITS,"SBML Model"+ATT+"Area Units");
			 * AttributeHelper.setNiceId(
			 * SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .EXTENT_UNITS,"SBML Model"+ATT+"Extent Units");
			 * AttributeHelper.setNiceId
			 * (SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .LENGTH_UNITS,"SBML Model"+ATT+"Length Units");
			 * AttributeHelper.setNiceId
			 * (SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .CONVERSION_FACTOR,"SBML Model"+ATT+"Conversion Factor");
			 * AttributeHelper
			 * .setNiceId(SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .meta_id,"Meta ID");
			 * AttributeHelper.setNiceId(SBML_Constants.sbml_model
			 * +ATT+SBML_Constants.SBOTERM,"SBML Model"+ATT+"SBOTerm");
			 * AttributeHelper
			 * .setNiceId(SBML_Constants.sbml_model+ATT+SBML_Constants
			 * .TOOLTIP,"SBML Model"+ATT+"Tooltip");
			 */
			_isInitialized = true;
		}
	}
}
