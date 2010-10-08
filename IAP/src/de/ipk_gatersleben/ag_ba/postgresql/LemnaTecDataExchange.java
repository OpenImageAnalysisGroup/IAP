/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk_gatersleben.ag_ba.postgresql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author entzian, klukas
 */
public class LemnaTecDataExchange {
	private final String user;
	private final String password;
	private final String port;
	private final String host;

	private static final String driver = "org.postgresql.Driver";

	public LemnaTecDataExchange() {
		user = "postgres";
		password = "LemnaTec";
		port = "5432";
		host = "192.168.32.203";
	}

	public Collection<String> getDatabases(Connection connection) throws SQLException {
		String sqlText = "Select datname from pg_database where datname <> 'template1' and datname <> 'template0' and datname <> 'postgres';";

		PreparedStatement ps = connection.prepareStatement(sqlText);

		if (Debug.TEST)
			Debug.print(sqlText);

		ResultSet rs = ps.executeQuery();

		Collection<String> result = new ArrayList<String>();

		while (rs.next()) {
			if (Debug.TEST)
				Debug.print("aktuelle Zeile: ", rs.getString(1));

			result.add(rs.getString(1));

		}
		rs.close();
		ps.close();

		return result;
	}

	public Collection<String> getExperiments(String database) throws SQLException, ClassNotFoundException {
		String sqlText = "SELECT distinct(measurement_label) FROM import_data";

		Connection connection = openConnectionToDatabase(database);
		PreparedStatement ps = connection.prepareStatement(sqlText);

		ResultSet rs = ps.executeQuery();

		Collection<String> result = new ArrayList<String>();

		while (rs.next()) {
			result.add(rs.getString(1));
		}

		rs.close();
		ps.close();
		closeDatabaseConnection(connection);

		return result;
	}

	public Collection<Snapshot> getSnapshots(String database, String measurementLabel) throws SQLException,
			ClassNotFoundException {
		Collection<Snapshot> result = new ArrayList<Snapshot>();
		Connection connection = openConnectionToDatabase(database);

		String sqlText = "Select s.creator, s.measurement_label, t.camera_label, s.id_tag, f.path, s.time_stamp, s.water_amount, s.weight_after, s.weight_before "
				+ "from Snapshot AS s, tiled_image AS t, tile AS e, image_file_table AS f "
				+ "where s.measurement_label = ? and "
				+ "s.id = t.snapshot_id and t.id = e.tiled_image_id and e.image_oid = f.id;";

		PreparedStatement ps = connection.prepareStatement(sqlText);
		ps.setString(1, measurementLabel);

		ResultSet rs = ps.executeQuery();

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
		rs.close();
		ps.close();
		return result;
	}

	private static void loadJdbcDriver() throws ClassNotFoundException {
		Class.forName(driver);
	}

	private Connection openConnectionToDatabase(String database) throws SQLException, ClassNotFoundException {

		loadJdbcDriver();

		String path = "jdbc:postgresql:" + (host != null ? ("//" + host) + (port != null ? ":" + port : "") + "/" : "")
				+ database;
		Connection connection = DriverManager.getConnection(path, user, password);

		if (Debug.TEST) {
			DatabaseMetaData meta = connection.getMetaData(); // Metadata
			Debug.print("Connection successful:", meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
		}

		return connection;
	}

	private void closeDatabaseConnection(Connection connection) throws SQLException {
		connection.close();
	}

	public static void main(String[] args) {
		try {
			for (Snapshot snapshot : new LemnaTecDataExchange().getSnapshots("DH-MB1", "DH-MB_Reihe_01"))
				System.out.println("Creator: " + snapshot.getCreator() + "Bild: " + snapshot.getPath_image());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
