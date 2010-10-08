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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.IOurl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

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
		host = "lemna-db.ipk-gatersleben.de";
	}

	public Collection<String> getDatabases() throws SQLException, ClassNotFoundException {
		HashSet<String> invalidDBs = new HashSet<String>();
		invalidDBs.add("template1");
		invalidDBs.add("template0");
		invalidDBs.add("postgres");
		invalidDBs.add("LTSystem");
		invalidDBs.add("bacula");
		invalidDBs.add("LTTestDB");
		invalidDBs.add("LemnaTest");

		String sqlText = "SELECT datname FROM pg_database";

		Connection connection = openConnectionToDatabase("postgres");

		PreparedStatement ps = connection.prepareStatement(sqlText);

		if (Debug.TEST)
			Debug.print(sqlText);

		ResultSet rs = ps.executeQuery();

		Collection<String> result = new TreeSet<String>();

		while (rs.next()) {
			if (Debug.TEST)
				Debug.print("aktuelle Zeile: ", rs.getString(1));

			String dbName = rs.getString(1);

			if (!invalidDBs.contains(dbName))
				result.add(dbName);

		}
		rs.close();
		ps.close();

		return result;
	}

	public Collection<String> getExperimentInDatabase(String database) throws SQLException, ClassNotFoundException {
		String sqlText = "SELECT distinct(measurement_label) FROM import_data";

		Connection connection = openConnectionToDatabase(database);
		PreparedStatement ps = connection.prepareStatement(sqlText);

		ResultSet rs = ps.executeQuery();

		Collection<String> result = new TreeSet<String>();

		while (rs.next()) {
			result.add(rs.getString(1));
		}

		rs.close();
		ps.close();
		closeDatabaseConnection(connection);

		return result;
	}

	public Collection<Snapshot> getSnapshotsOfExperiment(String database, String experiment) throws SQLException,
			ClassNotFoundException {
		Collection<Snapshot> result = new ArrayList<Snapshot>();
		Connection connection = openConnectionToDatabase(database);

		String sqlText = "Select s.creator, s.measurement_label, t.camera_label, s.id_tag, f.path, s.time_stamp, s.water_amount, s.weight_after, s.weight_before "
				+ "from Snapshot AS s, tiled_image AS t, tile AS e, image_file_table AS f "
				+ "where s.measurement_label = ? and "
				+ "s.id = t.snapshot_id and t.id = e.tiled_image_id and e.image_oid = f.id;";

		PreparedStatement ps = connection.prepareStatement(sqlText);
		ps.setString(1, experiment);

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
			for (Snapshot snapshot : new LemnaTecDataExchange().getSnapshotsOfExperiment("DH-MB1", "DH-MB_Reihe_01"))
				System.out.println("Creator: " + snapshot.getCreator() + "Bild: " + snapshot.getPath_image());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public ExperimentInterface getExperiment(String database, String experimentname) throws SQLException,
			ClassNotFoundException {
		LinkedHashSet<String> experimentators = new LinkedHashSet<String>();

		ArrayList<NumericMeasurementInterface> measurements = new ArrayList<NumericMeasurementInterface>();

		String species = "Arabidopsis";
		String genotype = "WT";
		String variety = "";
		String growthconditions = "";
		String treatment = "";

		Collection<Snapshot> snapshots = getSnapshotsOfExperiment(database, experimentname);
		for (Snapshot sn : snapshots) {
			experimentators.add(sn.getCreator());

			treatment = (sn.getWater_amount() > 0) ? "normal" : "wasser stress";

			{
				// process weight_before
				Substance s = new Substance();
				s.setSubstanceName("weight_before");

				Condition condition = new Condition(s);
				condition.setExperimentName(experimentname);
				condition.setSpecies(species);
				condition.setGenotype(genotype);
				condition.setVariety(variety);
				condition.setGrowthconditions(growthconditions);
				condition.setTreatment(treatment);

				Sample sample = new Sample(condition);

				Calendar gc = GregorianCalendar.getInstance();
				gc.setTime(new Date(sn.getTime_stamp().getTime()));

				sample.setTime(gc.get(Calendar.DAY_OF_YEAR));
				sample.setTimeUnit("day");

				NumericMeasurement weightBefore = new NumericMeasurement(sample);
				weightBefore.setReplicateID(Integer.parseInt(StringManipulationTools.getNumbersFromString(sn.getId_tag())));
				weightBefore.setUnit("g");
				weightBefore.setValue(sn.getWeight_before());

				measurements.add(weightBefore);
			}
			{
				// process water_weight
				Substance s = new Substance();
				s.setSubstanceName("water_weight");

				Condition condition = new Condition(s);
				condition.setExperimentName(experimentname);
				condition.setSpecies(species);
				condition.setGenotype(genotype);
				condition.setVariety(variety);
				condition.setGrowthconditions(growthconditions);
				condition.setTreatment(treatment);

				Sample sample = new Sample(condition);

				Calendar gc = GregorianCalendar.getInstance();
				gc.setTime(new Date(sn.getTime_stamp().getTime()));

				sample.setTime(gc.get(Calendar.DAY_OF_YEAR));
				sample.setTimeUnit("day");

				NumericMeasurement weightBefore = new NumericMeasurement(sample);
				weightBefore.setReplicateID(Integer.parseInt(StringManipulationTools.getNumbersFromString(sn.getId_tag())));
				weightBefore.setUnit("g");
				weightBefore.setValue(sn.getWeight_after() - sn.getWeight_before());

				measurements.add(weightBefore);
			}
			{
				// process water_amount
				Substance s = new Substance();
				s.setSubstanceName("water_amount");

				Condition condition = new Condition(s);
				condition.setExperimentName(experimentname);
				condition.setSpecies(species);
				condition.setGenotype(genotype);
				condition.setVariety(variety);
				condition.setGrowthconditions(growthconditions);
				condition.setTreatment(treatment);

				Sample sample = new Sample(condition);

				Calendar gc = GregorianCalendar.getInstance();
				gc.setTime(new Date(sn.getTime_stamp().getTime()));

				sample.setTime(gc.get(Calendar.DAY_OF_YEAR));
				sample.setTimeUnit("day");

				NumericMeasurement water = new NumericMeasurement(sample);
				water.setReplicateID(Integer.parseInt(StringManipulationTools.getNumbersFromString(sn.getId_tag())));
				water.setUnit("ml");
				water.setValue(sn.getWater_amount());

				measurements.add(water);
			}
			{
				// process image
				if (sn.getCamera_label() != null) {
					Substance s = new Substance();
					s.setSubstanceName(sn.getCamera_label());

					Condition condition = new Condition(s);
					condition.setExperimentName(experimentname);
					condition.setSpecies(species);
					condition.setGenotype(genotype);
					condition.setVariety(variety);
					condition.setGrowthconditions(growthconditions);
					condition.setTreatment(treatment);

					Sample sample = new Sample(condition);

					Calendar gc = GregorianCalendar.getInstance();
					gc.setTime(new Date(sn.getTime_stamp().getTime()));

					sample.setTime(gc.get(Calendar.DAY_OF_YEAR));
					sample.setTimeUnit("day");

					ImageData image = new ImageData(sample);
					image.setPixelsizeX(1);
					image.setPixelsizeY(1);
					image.setReplicateID(Integer.parseInt(StringManipulationTools.getNumbersFromString(sn.getId_tag())));
					image.setUnit("");
					IOurl url = new IOurl("ftp", "lemnatec:LemnaTec@" + host, "../../data0/pgftp/" + database + "/"
							+ sn.getPath_image());
					// System.out.println(url.toString());
					image.setURL(url);
					measurements.add(image);
				}
			}

		}

		ExperimentInterface experiment = NumericMeasurement3D.getExperiment(measurements);

		ArrayList experimentatorsArray = new ArrayList();
		for (String s : experimentators)
			experimentatorsArray.add(s);
		String experimentator = StringManipulationTools.getStringList(experimentatorsArray, ",");
		experiment.getHeader().setCoordinator(experimentator);
		experiment.getHeader().setImportusername("ToDo");
		experiment.getHeader().setExcelfileid("lemnatec:" + database + ":" + experimentname);
		experiment.getHeader().setExperimentname(experimentname);
		experiment.getHeader().setExperimenttype("Phenotyping");
		experiment.getHeader().setImagefiles(snapshots.size());
		experiment.getHeader().setImportdate(new Date()); // ToDo: last snapshot
		// time
		experiment.getHeader().setStartdate(new Date()); // ToDo: first snapshot
		// time
		experiment.getHeader().setImportusergroup("LemnaTec Import");
		experiment.getHeader().setImportusername(SystemAnalysis.getUserName()); // todo:
		// creator
		// of
		// experiment
		// series
		experiment.getHeader().setSequence("");
		experiment.getHeader().setRemark(
				"Transfered from LemnaTec-DB " + database + "/" + experimentname + " to IAP Cloud");
		return experiment;
	}
}
