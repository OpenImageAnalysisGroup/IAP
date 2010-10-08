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
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author entzian, klukas
 */
public class DataExchange2 {
	private final String password = "LemnaTec";
	private final String user = "postgres";
	private final String port = "5432";
	private final String host = "192.168.32.203";

	private final String driver = "org.postgresql.Driver";
	private Connection connection = null;
	private Statement statm = null;

	private String experiment[];
	private String databaseA[];

	private Collection<Collection<Snapshot>> collectionSnapshot = new ArrayList<Collection<Snapshot>>();

	DataExchange2() {
		/*
		 * openConnection("postgres"); SelectAllDatabase(); closeConnection();
		 * 
		 * for(int i = database.length-1; i >= 0; i--) {
		 * openConnection(database[i]); ExperimentDerDatenbankAuslesen();
		 * 
		 * for(int j = experiment.length-1; j>=0; j--) collectionSnapshot =
		 * getSnapshots(experiment[j]);
		 * 
		 * closeConnection(); }
		 */
	}

	public static void main(String[] args) {

		// ExperimentHeader H1 = new ExperimentHeader();
		// H1.setExperimentLable("hallo");
		// H1.setExperimentLable(this.getString(mesuremtn_lable));

		// DataExchange Test = new DataExchange("DH-MB1");

		DataExchange2 Test = new DataExchange2();

		try {
			Collection<Collection<Snapshot>> test2 = Test.getSnapshots("DH-MB1", "DH-MB_Reihe_01");
			for (Collection<Snapshot> test : test2)
				for (Snapshot test3 : test)
					System.out.println("Creator: " + test3.getCreator() + "Bild: " + test3.getPath_image());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public Collection<Collection<Snapshot>> getSnapshots() throws SQLException, ClassNotFoundException {

		openConnection("postgres");
		SelectAllDatabase();
		closeConnection();

		collectionSnapshot = null;

		for (int i = databaseA.length - 1; i >= 0; i--) {
			openConnection(databaseA[i]);
			ExperimentDerDatenbankAuslesen();

			for (int j = experiment.length - 1; j >= 0; j--)
				collectionSnapshot.add(Snapshots(experiment[j]));

			closeConnection();
		}

		return collectionSnapshot;

	}

	public Collection<Collection<Snapshot>> getSnapshots(String database) throws SQLException, ClassNotFoundException {
		openConnection(database);
		databaseA = new String[1];
		databaseA[0] = database;
		ExperimentDerDatenbankAuslesen();

		for (int j = experiment.length - 1; j >= 0; j--)
			collectionSnapshot.add(Snapshots(experiment[j]));

		closeConnection();

		return collectionSnapshot;
	}

	public Collection<Collection<Snapshot>> getSnapshots(String database, String measurementLabel) throws SQLException,
			ClassNotFoundException {
		openConnection(database);
		databaseA = new String[1];
		databaseA[0] = database;
		experiment = new String[1];
		experiment[0] = measurementLabel;

		collectionSnapshot.add(Snapshots(measurementLabel));

		closeConnection();

		return collectionSnapshot;

	}

	// private Collection<Snapshot> getSnapshots(String database, String
	// measurementLabel) {
	private Collection<Snapshot> Snapshots(String measurementLabel) {
		ArrayList<Snapshot> result = new ArrayList<Snapshot>(); // in result
		// befindet sich
		// ein Experiment

		try {
			String sqlText;
			ResultSet rs;

			sqlText = "Select s.creator, s.measurement_label, t.camera_label, s.id_tag, f.path, s.time_stamp, s.water_amount, s.weight_after, s.weight_before "
					+ "from Snapshot AS s, tiled_image AS t, tile AS e, image_file_table AS f "
					+ "where s.measurement_label = '"
					+ measurementLabel
					+ "' and "
					+ "s.id = t.snapshot_id and t.id = e.tiled_image_id and e.image_oid = f.id;";

			rs = SelectAnfrage(sqlText);

			// for(int d = AnzahlTupel(rs)-1; d >= 0; d--)
			while (rs.next()) {

				Snapshot snapshot = new Snapshot();

				snapshot.setCreator(rs.getString(1));
				snapshot.setMeasurement_label(rs.getString(2));
				snapshot.setCamera_label(rs.getString(3));
				snapshot.setId_tag(rs.getString(4));
				snapshot.setPath_image(rs.getString(5));
				// snapshot.setPath_null_image(rs.getString(6));
				snapshot.setTime_stamp(rs.getTimestamp(6));
				snapshot.setWater_amount(rs.getInt(7));
				snapshot.setWeight_after(rs.getDouble(8));
				snapshot.setWeight_before(rs.getDouble(9));

				result.add(snapshot);
			}

			closeSQL(rs);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

		/*
		 * Snapshot snapshot = new Snapshot(); snapshot.setCreator("hallo");
		 * 
		 * System.out.println(snapshot.getCreator());
		 * System.out.println(snapshot); result.add(snapshot);
		 * System.out.println(result.get(0));
		 */
		return result;
	}

	private ResultSet SelectAnfrage(String sqlText) throws SQLException {

		statm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 0);

		if (Debug.TEST)
			Debug.print(sqlText);

		return statm.executeQuery(sqlText);

	}

	private int AnzahlTupel(ResultSet result) {

		int rows = 0;

		try {

			result.last();
			rows = result.getRow();
			result.beforeFirst(); // muss hier drinne nicht gemacht werden

			if (Debug.TEST)
				Debug.print("Tupellänge:", rows);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rows;

	}

	private void SelectAllDatabase() throws SQLException {
		String sqlText = "Select datname from pg_database where datname <> 'template1' and datname <> 'template0' and datname <> 'postgres';";

		ResultSet result = SelectAnfrage(sqlText);

		int rows = AnzahlTupel(result);

		databaseA = new String[rows];

		while (result.next()) {
			if (Debug.TEST)
				Debug.print("aktuelle Zeile: ", result.getString(1));

			databaseA[result.getRow() - 1] = result.getString(1);

		}
		closeSQL(result);
	}

	private void ExperimentDerDatenbankAuslesen() throws SQLException {
		String sqlText = "Select distinct measurement_label from import_data;";

		ResultSet result = SelectAnfrage(sqlText);

		int rows = AnzahlTupel(result);

		experiment = new String[rows];

		while (result.next()) {
			if (Debug.TEST)
				Debug.print("aktuelle Zeile: ", result.getString(1));

			experiment[result.getRow() - 1] = result.getString(1);

		}

		closeSQL(result);
	}

	private void closeSQL(ResultSet result) {
		try {

			result.close();
			statm.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void loadJdbcDriver() throws ClassNotFoundException {
		Class.forName(driver);
	}

	private void openConnection(String database) throws SQLException, ClassNotFoundException {

		loadJdbcDriver();

		String path = "jdbc:postgresql:" + (host != null ? ("//" + host) + (port != null ? ":" + port : "") + "/" : "")
				+ database;
		connection = DriverManager.getConnection(path, user, password);

		if (Debug.TEST) {
			DatabaseMetaData meta = connection.getMetaData(); // Metadata
			// abfragen
			Debug.print("Connection successful:", meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
		}
	}

	private void closeConnection() throws SQLException {
		connection.close();
	}
}
