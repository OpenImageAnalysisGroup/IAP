/*******************************************************************************
 * The DBE2 Add-on is (c) 2009-2010 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project which is developed by our group is available
 * under the GPL license v2.0 (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
 * By using this Add-on and VANTED you need to accept the terms and conditions of
 * this license, the below stated disclaimer of warranties and the licenses of the used
 * libraries. For further details see license.txt in the root folder of this project.
 ******************************************************************************/
package de.ipk.ag_ba.rmi_server.databases;

/**
 * enumeration containing all important tables in the DBE2 database with its
 * properties
 * 
 * @author Mehlhorn
 */
public enum DBTable {
	ACCOUNT("account", "<html>table storing accounts</html>"), USERGROUP("user-group",
						"<html>table storing user-groups</html>"), USER2GROUP("user to group",
						"<html>table storing the account-userGroup - association</html>"),

	EXPTYPE("experiment-type", "<html>table storing experiment-types</html>"), SPECIES("species",
						"<html>table storing species-names</html>", "NEWT"), MEASUREMENTUNIT("measurement-unit",
						"<html>table storing units of measurement-data-values</html>", "EFO", "UO"), MEASUREMENTINDEXUNIT("time-unit",
						"<html>table storing units of sample-time-steps</html>", "EFO", "UO"), POSITIONUNIT("position-unit",
						"<html>table storing units of spatial values</html>", "EFO", "UO"), SUBSTANCE("substance",
						"<html>table storing substance-names</html>", "CHEBI"), SUBSTANCEGROUP("substance-group",
						"<html>table storing substance-groups</html>", "CHEBI"),

	EXPERIMENT("experiment", "<html>table storing whole experiments</html>"), CONDITION("condition",
						"<html>table storing conditions of one experiment</html>"), SAMPLE("sample",
						"<html>table storing samples of the same conditions</html>"), MEASUREMENT("measurement",
						"<html>table storing simple measurements</html>"), IMAGE("image", "<html>table storing 2D-data</html>"), VOLUME(
						"volume", "<html>table storing 3D-data</html>"), NETWORK("network",
						"<html>table storing networks as binaries</html>"), ALTSUBSTID("alt. substance-IDs",
						"<html>table storing alternative substance-identifier</html>"), SUPPLEMENTARYFILE("supplementary-file",
						"<html>table storing optional binary-data</html>");
	
	private final String desc;
	private final String naturalName;
	private final String[] ontologies;
	
	private DBTable(String naturalName, String desc, String... ontologies) {
		this.naturalName = naturalName;
		this.desc = desc;
		this.ontologies = ontologies;
	}
	
	/**
	 * @return the description of the table
	 */
	public String getDescription() {
		return this.desc;
	}
	
	@Override
	public String toString() {
		return this.naturalName;
	}
	
	public String[] getRelevantOntologies() {
		return this.ontologies;
	}
	
	/**
	 * @return account, userGroup
	 */
	public static DBTable[] getUserManagementTables() {
		return new DBTable[] { DBTable.ACCOUNT, DBTable.USERGROUP };
	}
	
	/**
	 * @return all basis data tables
	 */
	public static DBTable[] getBasisDataTables() {
		return new DBTable[] { DBTable.EXPTYPE, DBTable.SPECIES, DBTable.MEASUREMENTINDEXUNIT, DBTable.MEASUREMENTUNIT,
							DBTable.POSITIONUNIT, DBTable.SUBSTANCEGROUP, DBTable.SUBSTANCE };
	}
	
	/**
	 * @return experiment-tables and altSubstID + supplementaryFile
	 */
	public static DBTable[] getExperimentDataTables() {
		return new DBTable[] { DBTable.EXPERIMENT, DBTable.CONDITION, DBTable.SAMPLE, DBTable.MEASUREMENT, DBTable.IMAGE,
							DBTable.VOLUME, DBTable.NETWORK, DBTable.ALTSUBSTID, DBTable.SUPPLEMENTARYFILE };
	}
}
