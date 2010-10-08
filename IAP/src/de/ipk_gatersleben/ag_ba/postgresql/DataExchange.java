/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk_gatersleben.ag_ba.postgresql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author entzian, klukas
 * 
 */
public class DataExchange {

	/**
	 * @param args
	 */

	// Zum testen werden hier die Zugangsdaten eingetragen
	// sollte sp�ter anders abgelegt werden

	private final String password = "LemnaTec";
	private final String user = "postgres";
	private final String port = "5432";
	private final String host = "192.168.32.203";
	private final String driver = "org.postgresql.Driver";

	// private ArrayList<String> DB_Exp_Data;

	private String experiment[]; // das kann dann weg

	DataExchange() {
		this("DH-MB1");
	}

	DataExchange(String database) {
		try {
			loadJdbcDriver();
			Connection connection = openConnection(database);
			ExperimentDerDatenbankAuslesen(connection);
			closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// ExperimentHeader H1 = new ExperimentHeader();
		// H1.setExperimentLable("hallo");
		// H1.setExperimentLable(this.getString(mesuremtn_lable));

		DataExchange Test = new DataExchange("DH-MB1");
		String testArray[] = Test.getExperimentLabels();

		if (Debug.TEST)
			Debug.print("Größe des TestArrays: ", testArray.length);

		for (int za = testArray.length - 1; za >= 0; za--)
			System.out.println(testArray[za]);

	}

	/*
	 * private ExperimentHeader H1[];
	 * 
	 * public String test() {
	 * 
	 * H1[ZeilenAnzahl] = new ExperimentHeader(); for(int anzahl = ZeilenAnzahl;
	 * anzahl >= 0; anzahl--) ExperimentHeader H1[anzahl]
	 * 
	 * 
	 * return result; }
	 */

	public String[] getExperimentLabels() {
		return experiment;
	}

	public String getSingelExperimentLabel(int position) {
		if (position < 0 || position > experiment.length)
			return "";
		else
			return experiment[position];
	}

	private void ExperimentDerDatenbankAuslesen(Connection connection) throws SQLException {
		if (Debug.TEST) {
			DatabaseMetaData meta = connection.getMetaData(); // Metadata
			// abfragen
			Debug.print("Connection successful:", meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
		}

		Statement statm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 0);

		String sqlText = "Select distinct measurement_label from import_data";

		if (Debug.TEST)
			Debug.print(sqlText);

		ResultSet result = statm.executeQuery(sqlText);

		result.last();
		int rows = result.getRow();
		result.beforeFirst();

		if (Debug.TEST)
			Debug.print("Länge result:", rows);

		experiment = new String[rows];

		while (result.next()) {
			if (Debug.TEST) {
				Debug.print("aktuelle Zeile: ", result.getString(1));
			}

			experiment[result.getRow() - 1] = result.getString(1);

		}
		result.close();
		statm.close();
	}

	private void loadJdbcDriver() throws ClassNotFoundException {
		Class.forName(driver);
	}

	private Connection openConnection(String database) throws SQLException {
		String path = "jdbc:postgresql:" + (host != null ? ("//" + host) + (port != null ? ":" + port : "") + "/" : "")
				+ database;
		return DriverManager.getConnection(path, user, password);
	}

	private void closeConnection(Connection connection) throws SQLException {
		connection.close();
	}

}
