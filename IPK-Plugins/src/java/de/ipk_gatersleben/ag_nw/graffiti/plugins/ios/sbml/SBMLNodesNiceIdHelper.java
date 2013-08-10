package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

import org.AttributeHelper;


public class SBMLNodesNiceIdHelper {
	
	private static boolean isInitialized = false;
	
	/**
	 * Sets the niceIDs if they are not initialized yet.
	 */
	public static void initNiceIds() {
		if (!isInitialized) {
			initSpeciesNiceIDs();
			initReactionNiceIDs();
			isInitialized = true;
		}
	}
	
	public static boolean isInitialized() {
		return isInitialized;
	}
	
	private static void initReactionNiceIDs() {
		AttributeHelper.setNiceId(SBML_Constants.FAST, SBML_Constants.SBML_HEADLINE
				+ ": Fast");
		AttributeHelper.setNiceId(SBML_Constants.REVERSIBLE, SBML_Constants.SBML_HEADLINE
				+ ": Reversible");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_ID, SBML_Constants.SBML_HEADLINE
				+ ": ID");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_NAME, SBML_Constants.SBML_HEADLINE
				+ ": Name");
		AttributeHelper.setNiceId(SBML_Constants.REACTION_COMPARTMENT, SBML_Constants.SBML_HEADLINE
				+ ": Compartment ID");
	}
	
	/**
	 * Sets the nice id
	 * 
	 * @param internHeadline
	 *        how the headline is represented intern
	 * @param presentedHeadline
	 *        how the user will see the headline
	 */
	private static void initSpeciesNiceIDs() {
		AttributeHelper.setNiceId(SBML_Constants.COMPARTMENT, SBML_Constants.SBML_HEADLINE
				+ ": Compartment ID");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_ID, SBML_Constants.SBML_HEADLINE
				+ ": ID");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_NAME, SBML_Constants.SBML_HEADLINE
				+ ": Name");
		AttributeHelper.setNiceId(SBML_Constants.INITIAL_AMOUNT,
				SBML_Constants.SBML_HEADLINE + ": Initial Amount");
		AttributeHelper.setNiceId(SBML_Constants.INITIAL_CONCENTRATION,
				SBML_Constants.SBML_HEADLINE + ": Initial Concentration");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_SUBSTANCE_UNITS,
				SBML_Constants.SBML_HEADLINE + ": Substance Units");
		AttributeHelper.setNiceId(SBML_Constants.HAS_ONLY_SUBSTANCE_UNITS,
				SBML_Constants.SBML_HEADLINE + ": HasOnlySubstanceUnits");
		AttributeHelper.setNiceId(SBML_Constants.BOUNDARY_CONDITION,
				SBML_Constants.SBML_HEADLINE + ": Boundary Condition");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_CONSTANT,
				SBML_Constants.SBML_HEADLINE + ": Constant");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_CONVERSION_FACTOR,
				SBML_Constants.SBML_HEADLINE + ": Conversion Faktor");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_META_ID,
				SBML_Constants.SBML_HEADLINE + ": Meta ID");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_SBOTERM,
				SBML_Constants.SBML_HEADLINE + ": SBOTerm");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_NOTES,
				SBML_Constants.SBML_HEADLINE + ": Notes");
		AttributeHelper.setNiceId(SBML_Constants.SPECIES_COMPARTMENT_NAME,
				SBML_Constants.SBML_HEADLINE + ": Compartment ID");
		
	}
	
}
