/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

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
		invalidDBs.add("CornTest2");
		invalidDBs.add("CornTest3");
		
		String sqlText = "SELECT datname FROM pg_database";
		
		Connection connection = openConnectionToDatabase("postgres");
		
		Collection<String> result = new TreeSet<String>();
		
		try {
			PreparedStatement ps = connection.prepareStatement(sqlText);
			
			if (Debug.TEST)
				Debug.print(sqlText);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				if (Debug.TEST)
					Debug.print("aktuelle Zeile: ", rs.getString(1));
				
				String dbName = rs.getString(1);
				
				if (!invalidDBs.contains(dbName))
					result.add(dbName);
				
			}
			rs.close();
			ps.close();
		} finally {
			closeDatabaseConnection(connection);
		}
		return result;
	}
	
	private static HashMap<String, Collection<ExperimentHeaderInterface>> memRes1 = new HashMap<String, Collection<ExperimentHeaderInterface>>();
	private static long updateTime = -1;
	
	public synchronized Collection<ExperimentHeaderInterface> getExperimentsInDatabase(String user, String database)
			throws SQLException, ClassNotFoundException {
		Collection<ExperimentHeaderInterface> res = memRes1.get(user + ";" + database);
		if (res == null || System.currentTimeMillis() - updateTime > 2 * 60 * 1000) {
			res = getExperimentsInDatabaseIC(user, database);
			updateTime = System.currentTimeMillis();
			memRes1.put(user + ";" + database, res);
		}
		return res;
	}
	
	private Collection<ExperimentHeaderInterface> getExperimentsInDatabaseIC(String user, String database)
			throws SQLException, ClassNotFoundException {
		System.out.println("GET EXP LIST LT");
		String sqlText = "SELECT distinct(measurement_label) FROM snapshot ORDER BY measurement_label";
		
		Collection<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
		
		Connection connection = openConnectionToDatabase(database);
		try {
			PreparedStatement ps = connection.prepareStatement(sqlText);
			
			ResultSet rs = ps.executeQuery();
			
			// Collection<String> result = new TreeSet<String>();
			
			while (rs.next()) {
				ExperimentHeaderInterface ehi = new ExperimentHeader();
				ehi.setExperimentname(rs.getString(1));
				ehi.setDatabase(database);
				ehi.setDatabaseId("lemnatec:" + database + ":" + ehi.getExperimentName());
				ehi.setOriginDbId("lemnatec:" + database + ":" + ehi.getExperimentName());
				ehi.setImportusername(user != null ? user : SystemAnalysis.getUserName());
				ehi.setImportusergroup("LemnaTec");
				LemnaTecSystem system = LemnaTecSystem.getTypeFromDatabaseName(database);
				if (system == LemnaTecSystem.Barley) {
					ehi.setExperimenttype(IAPexperimentTypes.BarleyGreenhouse);
					ehi.setImportusergroup("LemnaTec (BGH)");
				} else
					if (system == LemnaTecSystem.Maize) {
						ehi.setExperimenttype(IAPexperimentTypes.MaizeGreenhouse);
						ehi.setImportusergroup("LemnaTec (CGH)");
					} else
						if (system == LemnaTecSystem.Phytochamber) {
							ehi.setExperimenttype(IAPexperimentTypes.Phytochamber);
							ehi.setImportusergroup("LemnaTec (APH)");
						} else {
							ehi.setExperimenttype(IAPexperimentTypes.UnknownGreenhouse);
							ehi.setImportusergroup("LemnaTec (Other)");
						}
				ehi.setSequence("");
				ehi.setSizekb(-1);
				result.add(ehi);
			}
			
			rs.close();
			ps.close();
			
			sqlText = "" +
					"SELECT min(time_stamp), max(time_stamp), count(*) " +
					"FROM snapshot " + // ,tiled_image " +
					"WHERE measurement_label=?";// ; AND tiled_image.snapshot_id=snapshot.id";
			ps = connection.prepareStatement(sqlText);
			for (ExperimentHeaderInterface ehi : result) {
				ps.setString(1, ehi.getExperimentName());
				rs = ps.executeQuery();
				while (rs.next()) {
					Timestamp min = rs.getTimestamp(1);
					Timestamp max = rs.getTimestamp(2);
					if (min == null || max == null)
						System.out.println("Warning: No snapshot times stored for experiment " + ehi.getExperimentName()
								+ " in database " + ehi.getDatabase() + "!");
					if (min != null)
						ehi.setStartdate(new Date(min.getTime()));
					else
						ehi.setStartdate(new Date());
					if (max != null)
						ehi.setImportdate(new Date(max.getTime()));
					else
						ehi.setImportdate(new Date());
					ehi.setNumberOfFiles(rs.getInt(3));
					break;
				}
				rs.close();
			}
			
			HashMap<ExperimentHeaderInterface, HashSet<String>> people = new HashMap<ExperimentHeaderInterface, HashSet<String>>();
			
			ArrayList<String> names = new ArrayList<String>();
			try {
				sqlText = "SELECT distinct(creator) FROM import_data WHERE measurement_label=?";
				ps = connection.prepareStatement(sqlText);
				for (ExperimentHeaderInterface ehi : result) {
					if (getCoordinatorFromExperimentName(ehi.getExperimentName()) != null) {
						ehi.setCoordinator(getCoordinatorFromExperimentName(ehi.getExperimentName()));
					} else {
						if (!people.containsKey(ehi))
							people.put(ehi, new HashSet<String>());
						ps.setString(1, ehi.getExperimentName());
						rs = ps.executeQuery();
						names.clear();
						while (rs.next()) {
							String name = rs.getString(1);
							names.add(getNiceNameFromLoginName(name));
							people.get(ehi).add(rs.getString(1));
						}
						rs.close();
						if (names.size() > 1) {
							names.remove(getNiceNameFromLoginName("muecke"));
						}
						if (names.size() > 0)
							ehi.setCoordinator(names.iterator().next());
					}
				}
			} catch (Exception e) {
				// System.out.println("Info: Database " + database + " has no import_data table (" + e.getMessage() + ")");
				for (ExperimentHeaderInterface ehi : result) {
					ehi.setCoordinator(null);
				}
			}
			
			sqlText = "SELECT distinct(creator) FROM snapshot WHERE measurement_label=?";
			ps = connection.prepareStatement(sqlText);
			for (ExperimentHeaderInterface ehi : result) {
				if (!people.containsKey(ehi))
					people.put(ehi, new HashSet<String>());
				ps.setString(1, ehi.getExperimentName());
				rs = ps.executeQuery();
				names.clear();
				while (rs.next()) {
					String name = rs.getString(1);
					names.add(name);
					people.get(ehi).add(name);
				}
				rs.close();
				String importers = StringManipulationTools.getStringList(names, ";");
				ehi.setRemark("Snapshot creators: " + importers);
				if (ehi.getCoordinator() == null)
					ehi.setCoordinator(importers);
			}
			
			if (user != null && !getAdministrators().contains(user)) {
				// remove experiments from result which should not be visible to users
				LemnaTecSystem system = LemnaTecSystem.getTypeFromDatabaseName(database);
				if (!system.isPreAuthenticated(user))
					for (ExperimentHeaderInterface ehi : people.keySet()) {
						if (!people.get(ehi).contains(user)) {
							result.remove(ehi);
						}
					}
			}
			ps.close();
		} finally {
			closeDatabaseConnection(connection);
		}
		return result;
	}
	
	private HashMap<String, String> login2niceName = null;
	
	private String getNiceNameFromLoginName(String name) {
		if (login2niceName == null) {
			login2niceName = new HashMap<String, String>();
			login2niceName.put("Fernando", "Arana, Dr. Fernando (HET)");
			login2niceName.put("Gramel-Eikenroth", "Gramel-Eikenroth (LemnaTec)");
			login2niceName.put("LTAdmin", "LTAdmin (LemnaTec)");
			login2niceName.put("LemnaTec Support", "LemnaTec Support (LemnaTec)");
			login2niceName.put("entzian", "Entzian, Alexander (BA)");
			login2niceName.put("neumannk", "Neumann, Kerstin (GED)");
			login2niceName.put("hartmann", "Hartmann, Anja (PBI)");
			login2niceName.put("mary", "Ziems, Mary (GED");
			login2niceName.put("Ziems", "Ziems, Mary (GED");
			login2niceName.put("stein", "Stein, Dr. Nils (GED");
			login2niceName.put("altmann", "Altmann, Prof. Dr. Thomas (MOG)");
			login2niceName.put("meyer", "Meyer, Dr. Rhonda (HET)");
			login2niceName.put("muraya", "Muraya, Moses Mahugu (HET)");
			login2niceName.put("Muraya", "Muraya, Moses Mahugu (HET)");
			login2niceName.put("weigelt", "Weigelt-Fischer, Dr. Kathleen (HET)");
			login2niceName.put("Muecke", "Muecke, Ingo (BA)");
			login2niceName.put("muecke", "Muecke, Ingo (BA)");
			login2niceName.put("seyfarth", "Seyfarth, Monique (HET)");
			login2niceName.put("klukas", "Klukas, Dr. Christian (BA)");
		}
		String res = login2niceName.get(name);
		if (res != null)
			return res;
		else
			return name;
	}
	
	private String getCoordinatorFromExperimentName(String experimentname) {
		try {
			if (experimentname == null || experimentname.isEmpty())
				return null;
			else {
				if (experimentname.length() >= 6) {
					// int id = Integer.parseInt(experimentname.substring(0, 4));
					String kuerzel = experimentname.substring(4, 6);
					String coor = getCoordinatorFromNameID(kuerzel);
					return coor;
				}
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	private HashMap<String, String> id2coo = null;
	
	private String getCoordinatorFromNameID(String kuerzel) {
		if (id2coo == null) {
			id2coo = new HashMap<String, String>();
			id2coo.put("AC", "Arana, Dr. Fernando (HET)");
			id2coo.put("MM", "Muraya, Moses Mahugu (HET)");
			id2coo.put("KN", "Neumann, Kerstin (GED)");
			id2coo.put("KW", "Weigelt-Fischer, Dr. Kathleen (HET)");
			id2coo.put("BA", "Klukas, Dr. Christian (BA)");
		}
		return id2coo.get(kuerzel);
	}
	
	public static HashSet<String> getAdministrators() {
		HashSet<String> res = new HashSet<String>();
		res.add("klukas");
		res.add("entzian");
		res.add("muecke");
		res.add("Muecke");
		res.add("pape");
		return res;
	}
	
	public static HashSet<String> getGroupLock() {
		HashSet<String> res = new HashSet<String>();
		res.add("PBI");
		res.add("SYS");
		res.add("BIT");
		return res;
	}
	
	public Collection<Snapshot> getSnapshotsOfExperiment(String database, String experiment) throws SQLException,
			ClassNotFoundException {
		Collection<Snapshot> result = new ArrayList<Snapshot>();
		Connection connection = openConnectionToDatabase(database);
		try {
			HashMap<Long, String> id2path = new HashMap<Long, String>();
			String sqlReadImageFileTable = "SELECT "
					+ "image_file_table.id as image_file_tableID, path FROM image_file_table";
			//
			// +
			// "FROM snapshot, tiled_image, tile, image_file_table, image_unit_configuration "
			// + "WHERE snapshot.measurement_label = ? and "
			// + "("
			// + "snapshot.id = tiled_image.snapshot_id and "
			// + "tiled_image.id = tile.tiled_image_id and "
			// +
			// "(tile.image_oid = image_file_table.id OR tile.null_image_oid = image_file_table.id)) OR ("
			// +
			// "snapshot.configuration_id = image_unit_configuration.compconfigid and snapshot.id = tiled_image.snapshot_id and tiled_image.camera_label = image_unit_configuration.gid and image_unit_configuration.image_parameter_oid = image_file_table.id)";
			
			PreparedStatement ps = connection.prepareStatement(sqlReadImageFileTable);
			// ps.setString(1, experiment);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				id2path.put(rs.getLong("image_file_tableID"), rs.getString("path"));
			}
			rs.close();
			ps.close();
			HashSet<Long> knownSnaphotIds = new HashSet<Long>();
			getProperImageSnapshots(experiment, result, connection, id2path, knownSnaphotIds);
			if (result.size() == 0) {
				getImageSnapshotsWithUnknownImageUnitConfiguration(experiment, result, connection, id2path, knownSnaphotIds);
			}
			{
				// load snapshots without images
				String sqlText = "SELECT "
						+ "	creator, measurement_label, id_tag, "
						+ "	time_stamp, water_amount, weight_after, weight_before, "
						+ "	snapshot.id as snapshotID "
						+ "FROM "
						+ "	snapshot "
						+ "WHERE "
						+ "	snapshot.measurement_label = ?";
				
				ps = connection.prepareStatement(sqlText);
				ps.setString(1, experiment);
				
				rs = ps.executeQuery();
				
				while (rs.next()) {
					Snapshot snapshot = new Snapshot();
					
					Long id = rs.getLong("snapshotID");
					
					if (knownSnaphotIds.contains(id))
						continue;
					
					snapshot.setCreator(rs.getString("creator"));
					snapshot.setMeasurement_label(rs.getString("measurement_label"));
					snapshot.setId_tag(rs.getString("id_tag"));
					
					snapshot.setTime_stamp(rs.getTimestamp("time_stamp"));
					snapshot.setWater_amount(rs.getInt("water_amount"));
					snapshot.setWeight_after(rs.getDouble("weight_after"));
					snapshot.setWeight_before(rs.getDouble("weight_before"));
					
					result.add(snapshot);
				}
				rs.close();
				ps.close();
			}
		} finally {
			closeDatabaseConnection(connection);
		}
		checkForAndCorrectEqualSnapshotTimes(result);
		return result;
	}
	
	private void getProperImageSnapshots(String experiment, Collection<Snapshot> result, Connection connection, HashMap<Long, String> id2path,
			HashSet<Long> knownSnaphotIds) throws SQLException {
		PreparedStatement ps;
		ResultSet rs;
		{
			// load snapshots with images
			String sqlText = "SELECT "
					+ "	creator, measurement_label, camera_label, id_tag, path, "
					+ "	time_stamp, water_amount, weight_after, weight_before, compname, xfactor, yfactor, "
					+ "	image_parameter_oid, image_oid, null_image_oid, snapshot.id as snapshotID, "
					+ "	image_file_table.id as image_file_tableID, compname "
					+ "FROM "
					+ "	snapshot, tiled_image, tile, image_file_table, image_unit_configuration "
					+ "WHERE "
					+ "	snapshot.measurement_label = ? and "
					+ "	snapshot.id = tiled_image.snapshot_id and "
					+ "	tiled_image.id = tile.tiled_image_id and "
					+ "	tile.image_oid = image_file_table.id and "
					+ "	snapshot.configuration_id = image_unit_configuration.id and"
					// + "	image_unit_configuration.id = tiled_image.id";
					+ "	image_unit_configuration.gid = tiled_image.camera_label";
			// and "
			// + "	tiled_image.camera_label = image_unit_configuration.gid";
			// + "	snapshot.configuration_id = image_unit_configuration.id and "
			// + "	tiled_image.snapshot_id = snapshot.id";
			
			ps = connection.prepareStatement(sqlText);
			ps.setString(1, experiment);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				Snapshot snapshot = new Snapshot();
				
				knownSnaphotIds.add(rs.getLong("snapshotID"));
				
				snapshot.setCreator(rs.getString("creator"));
				snapshot.setMeasurement_label(rs.getString("measurement_label"));
				snapshot.setUserDefinedCameraLabeL(rs.getString("camera_label"));
				snapshot.setId_tag(rs.getString("id_tag"));
				
				snapshot.setTime_stamp(rs.getTimestamp("time_stamp"));
				snapshot.setWater_amount(rs.getInt("water_amount"));
				double w = rs.getDouble("weight_after");
				if (w >= 0 && w <= 100000)
					snapshot.setWeight_after(w);
				w = rs.getDouble("weight_before");
				if (w >= 0 && w <= 100000)
					snapshot.setWeight_before(w);
				
				snapshot.setCamera_label(rs.getString("compname"));
				snapshot.setXfactor(rs.getDouble("xfactor"));
				snapshot.setYfactor(rs.getDouble("yfactor"));
				
				String s1 = id2path.get(rs.getLong("image_oid"));
				snapshot.setPath_image(s1);
				String s2 = id2path.get(rs.getLong("null_image_oid"));
				snapshot.setPath_null_image(s2);
				String s3 = id2path.get(rs.getLong("image_parameter_oid"));
				// System.out.println(s3);
				snapshot.setPath_image_config_blob(s3);
				
				result.add(snapshot);
			}
			rs.close();
			ps.close();
		}
	}
	
	private void getImageSnapshotsWithUnknownImageUnitConfiguration(String experiment, Collection<Snapshot> result, Connection connection,
			HashMap<Long, String> id2path,
			HashSet<Long> knownSnaphotIds) throws SQLException {
		PreparedStatement ps;
		ResultSet rs;
		{
			// load snapshots with images
			String sqlText = "SELECT "
					+ "	creator, measurement_label, camera_label, id_tag, path, "
					+ "	time_stamp, water_amount, weight_after, weight_before, "
					+ "	image_oid, null_image_oid, snapshot.id as snapshotID, "
					+ "	image_file_table.id as image_file_tableID "
					+ "FROM "
					+ "	snapshot, tiled_image, tile, image_file_table "
					+ "WHERE "
					+ "	snapshot.measurement_label = ? and "
					+ "	snapshot.id = tiled_image.snapshot_id and "
					+ "	tiled_image.id = tile.tiled_image_id and "
					+ "	tile.image_oid = image_file_table.id";
			
			ps = connection.prepareStatement(sqlText);
			ps.setString(1, experiment);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				Snapshot snapshot = new Snapshot();
				
				knownSnaphotIds.add(rs.getLong("snapshotID"));
				
				snapshot.setCreator(rs.getString("creator"));
				snapshot.setMeasurement_label(rs.getString("measurement_label"));
				snapshot.setUserDefinedCameraLabeL(rs.getString("camera_label"));
				snapshot.setId_tag(rs.getString("id_tag"));
				
				snapshot.setTime_stamp(rs.getTimestamp("time_stamp"));
				snapshot.setWater_amount(rs.getInt("water_amount"));
				double w = rs.getDouble("weight_after");
				if (w >= 0 && w <= 100000)
					snapshot.setWeight_after(w);
				w = rs.getDouble("weight_before");
				if (w >= 0 && w <= 100000)
					snapshot.setWeight_before(w);
				
				snapshot.setCamera_label(getCompNameFromConfigLabel(rs.getString("camera_label")));// rs.getString("compname"));
				snapshot.setXfactor(0);// rs.getDouble("xfactor"));
				snapshot.setYfactor(0);// rs.getDouble("yfactor"));
				
				String s1 = id2path.get(rs.getLong("image_oid"));
				snapshot.setPath_image(s1);
				String s2 = id2path.get(rs.getLong("null_image_oid"));
				snapshot.setPath_null_image(s2);
				String s3 = getCompAngleFromConfigLabel(rs.getString("camera_label"));
				// System.out.println(rs.getString("camera_label") + " ==> " + s3);
				snapshot.setPath_image_config_blob(s3);
				
				result.add(snapshot);
			}
			rs.close();
			ps.close();
		}
		
	}
	
	private String getCompNameFromConfigLabel(String conf) {
		String res = "";
		if (conf.toUpperCase().contains("NIR"))
			res += "nir.";
		if (conf.toUpperCase().contains("RGB"))
			res += "vis.";
		if (conf.toUpperCase().contains("FLU"))
			res += "fluo.";
		if (conf.toUpperCase().contains("TOP"))
			res += "top";
		if (conf.toUpperCase().contains("SIDE"))
			res += "side";
		return res;
	}
	
	private static HashMap<String, String> config2numbers = new HashMap<String, String>();
	
	private synchronized String getCompAngleFromConfigLabel(String conf) {
		if (!config2numbers.containsKey(conf))
			config2numbers.put(conf, StringManipulationTools.getNumbersFromString(conf));
		String res = config2numbers.get(conf);
		return res;
	}
	
	private void checkForAndCorrectEqualSnapshotTimes(Collection<Snapshot> result) {
		HashSet<Long> snapshotTimes = new HashSet<Long>();
		for (Snapshot s : result) {
			Timestamp ts = s.getTimestamp();
			long t = ts.getTime();
			if (snapshotTimes.contains(t)) {
				do {
					t += 1;
				} while (snapshotTimes.contains(t));
				snapshotTimes.add(t);
				ts.setTime(t);
			}
		}
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
	
	private static final HashMap<String, Double> blob2angle = new HashMap<String, Double>();
	
	public ExperimentInterface getExperiment(ExperimentHeaderInterface experimentReq,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws SQLException, ClassNotFoundException {
		ArrayList<NumericMeasurementInterface> measurements = new ArrayList<NumericMeasurementInterface>();
		
		String species = "";
		String genotype = "";
		String variety = "";
		String growthconditions = "";
		String treatment = "";
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Read database");
		if (optStatus != null)
			optStatus.setCurrentStatusValue(-1);
		
		Collection<Snapshot> snapshots = getSnapshotsOfExperiment(experimentReq.getDatabase(), experimentReq
				.getExperimentName());
		HashMap<String, Integer> idtag2replicateID = new HashMap<String, Integer>();
		
		Timestamp earliest = null;
		{
			Timestamp ts = null;
			TreeSet<String> ids = new TreeSet<String>();
			for (Snapshot sn : snapshots) {
				ids.add(sn.getId_tag());
				if (ts == null || sn.getTimestamp().before(ts))
					ts = sn.getTimestamp();
			}
			earliest = ts;
			int replID = 0;
			for (String id : ids) {
				replID++;
				idtag2replicateID.put(id, replID);
				// System.out.println(id + ";" + replID);
			}
		}
		
		HashMap<String, Condition> idtag2condition = getPlantIdAnnotation(experimentReq);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Process snapshots (" + snapshots.size() + ")");
		
		if (optStatus != null)
			optStatus.setCurrentStatusValue(0);
		int workload = snapshots.size();
		HashMap<String, NumericMeasurement> knownDayAndReplicateIDs = new HashMap<String, NumericMeasurement>();
		int idxx = 0;
		HashSet<Long> processedSnapshotTimes = new HashSet<Long>();
		for (Snapshot sn : snapshots) {
			if (sn.getId_tag().length() <= 0) {
				System.out.println("Warning: snapshot with empty ID tag is ignored.");
				continue;
			}
			
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine((double) idxx * 100 / workload);
			
			idxx++;
			
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Process snapshots (" + idxx + "/" + snapshots.size() + ")");
			
			Condition conditionTemplate = idtag2condition.get(sn.getId_tag());
			
			species = conditionTemplate != null ? conditionTemplate.getSpecies() : "not specified";
			genotype = conditionTemplate != null ? conditionTemplate.getGenotype() : "not specified";
			variety = conditionTemplate != null ? conditionTemplate.getVariety() : "not specified";
			growthconditions = conditionTemplate != null ? conditionTemplate.getGrowthconditions() : "not specified";
			treatment = conditionTemplate != null ? conditionTemplate.getTreatment() : "not specified";
			
			{
				String lbl = sn.getCamera_label();
				
				if (lbl != null && lbl.startsWith("imagingunits."))
					lbl = lbl.substring("imagingunits.".length());
				if (lbl != null && lbl.contains("#"))
					lbl = lbl.substring(0, lbl.indexOf("#"));
				
				// if (lbl.endsWith("_HL2"))
				// lbl = lbl.substring(0, lbl.length() - "_HL2".length());
				// if (lbl.endsWith("HL2"))
				// lbl = lbl.substring(0, lbl.length() - "HL2".length());
				//
				// for (int d = 0; d <= 360; d += 5) {
				// if (lbl.endsWith("_" + d + "_Grad")) {
				// lbl = lbl.substring(0, lbl.length() - ("_" + d +
				// "_Grad").length());
				// position = new Double(d);
				// break;
				// }
				// }
				// lbl = StringManipulationTools.stringReplace(lbl, "_", "");
				
				sn.setCamera_label(lbl);
			}
			
			String idTag = sn.getId_tag();
			
			Integer replicateID = idtag2replicateID.get(idTag);
			
			if (replicateID == null) {
				System.out.println("Warning: internal IAP error. Could not create get replicate ID for ID tag '"
						+ sn.getId_tag() + "'. Snapshot is ignored.");
				continue;
			}
			
			int day = DateUtil.getElapsedDays(earliest, new Date(sn.getTimestamp().getTime())) + 1;
			
			boolean firstSnapshotInfoForTimePoint = !processedSnapshotTimes.contains(sn.getTimestamp().getTime());
			
			if (firstSnapshotInfoForTimePoint) {
				// if (sn.getWeight_before() > 0) {
				// process weight_before
				Substance s = new Substance();
				s.setName("weight_before");
				
				Condition condition = new Condition(s);
				condition.setExperimentInfo(experimentReq);
				condition.setSpecies(species);
				condition.setGenotype(genotype);
				condition.setVariety(variety);
				condition.setGrowthconditions(growthconditions);
				condition.setTreatment(treatment);
				
				Sample sample = new Sample(condition);
				sample.setTime(day);
				sample.setTimeUnit("day");
				sample.setRowId(sn.getTimestamp().getTime());
				
				double w = sn.getWeight_before();
				if (!Double.isNaN(w)) {
					NumericMeasurement weightBefore = new NumericMeasurement(sample);
					weightBefore.setReplicateID(replicateID);
					weightBefore.setUnit("g");
					weightBefore.setValue(w);
					weightBefore.setQualityAnnotation(idTag);
					measurements.add(weightBefore);
				}
				// }
			}
			
			if (firstSnapshotInfoForTimePoint) {
				// if (sn.getWeight_after() > sn.getWeight_before()) {
				// process water_weight
				double wa = sn.getWeight_after();
				double wb = sn.getWeight_before();
				
				if (!Double.isNaN(wb) && !Double.isNaN(wa)) {
					Substance s = new Substance();
					s.setName("water_weight");
					
					Condition condition = new Condition(s);
					condition.setExperimentInfo(experimentReq);
					condition.setSpecies(species);
					condition.setGenotype(genotype);
					condition.setVariety(variety);
					condition.setGrowthconditions(growthconditions);
					condition.setTreatment(treatment);
					
					Sample sample = new Sample(condition);
					sample.setTime(day);
					sample.setTimeUnit("day");
					sample.setRowId(sn.getTimestamp().getTime());
					
					NumericMeasurement weightBefore = new NumericMeasurement(sample);
					weightBefore.setReplicateID(replicateID);
					weightBefore.setUnit("g");
					weightBefore.setValue(wa - wb);
					weightBefore.setQualityAnnotation(idTag);
					
					measurements.add(weightBefore);
				}
				// }
			}
			
			if (firstSnapshotInfoForTimePoint) {
				// if (sn.getWater_amount() > 0) {
				// process water_amount
				String iidd = day + "/" + replicateID;
				
				double wa = sn.getWeight_after();
				double wb = sn.getWeight_before();
				
				if (!Double.isNaN(wb) && !Double.isNaN(wa)) {
					
					if (knownDayAndReplicateIDs.containsKey(iidd)) {
						NumericMeasurement water = knownDayAndReplicateIDs.get(iidd);
						water.setValue(water.getValue() + (wa - wb));// sn.getWater_amount());
					} else {
						Substance s = new Substance();
						s.setName("water_sum");
						
						Condition condition = new Condition(s);
						condition.setExperimentInfo(experimentReq);
						condition.setSpecies(species);
						condition.setGenotype(genotype);
						condition.setVariety(variety);
						condition.setGrowthconditions(growthconditions);
						condition.setTreatment(treatment);
						
						Sample sample = new Sample(condition);
						sample.setTime(day);
						sample.setTimeUnit("day");
						sample.setRowId(sn.getTimestamp().getTime());
						
						NumericMeasurement water = new NumericMeasurement(sample);
						water.setReplicateID(replicateID);
						water.setUnit("g");
						water.setValue(wa - wb);// sn.getWater_amount());
						water.setQualityAnnotation(idTag);
						
						measurements.add(water);
						
						knownDayAndReplicateIDs.put(iidd, water);
					}
				}
				// }
			}
			{
				// process image
				if (sn.getCamera_label() != null) {
					
					Substance s = new Substance();
					s.setName(sn.getCamera_label());
					
					Condition condition = new Condition(s);
					condition.setExperimentInfo(experimentReq);
					condition.setSpecies(species);
					condition.setGenotype(genotype);
					condition.setVariety(variety);
					condition.setGrowthconditions(growthconditions);
					condition.setTreatment(treatment);
					
					Sample sample = new Sample(condition);
					sample.setTime(day);
					sample.setTimeUnit("day");
					sample.setRowId(sn.getTimestamp().getTime());
					
					ImageData image = new ImageData(sample);
					image.setPixelsizeX(sn.getXfactor());
					image.setPixelsizeY(sn.getYfactor());
					image.setReplicateID(replicateID);
					image.setUnit("");
					image.setQualityAnnotation(idTag);
					
					String fn = sn.getPath_image();
					if (fn != null && fn.contains("/"))
						fn = fn.substring(fn.lastIndexOf("/") + "/".length());
					
					Double position = null;
					
					fn = sn.getPath_image_config_blob();
					if (fn != null) {
						try {
							String a = sn.getUserDefinedCameraLabel();
							String b = StringManipulationTools.getNumbersFromString(a);
							if (b.length() == 0)
								position = 0d;
							else
								position = Double.parseDouble(b);
							if (position > 360)
								throw new NumberFormatException("Number too large");
						} catch (NumberFormatException nfe) {
							try {
								position = Double.parseDouble(fn);
							} catch (NumberFormatException e) {
								if (fn.contains("/"))
									fn = fn.substring(fn.lastIndexOf("/") + "/".length());
								IOurl url = LemnaTecFTPhandler.getLemnaTecFTPurl(host, experimentReq.getDatabase() + "/"
										+ sn.getPath_image_config_blob(), sn.getId_tag()
										+ (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
								if (optStatus != null)
									optStatus.setCurrentStatusText1("Process snapshots (" + idxx + "/" + snapshots.size() + ") (FTP)");
								position = processConfigBlobToGetRotationAngle(blob2angle, sn, url);
								if (optStatus != null)
									optStatus.setCurrentStatusText1("Process snapshots (" + idxx + "/" + snapshots.size() + ")");
							}
						}
					}
					
					if (position != null)
						if (Math.abs(position) < 0.00001)
							position = null;
					if (position != null) {
						image.setPosition(position);
						image.setPositionUnit("degree");
					}
					
					IOurl url = LemnaTecFTPhandler.getLemnaTecFTPurl(host, experimentReq.getDatabase() + "/"
							+ sn.getPath_image(), sn.getId_tag() + (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
					image.setURL(url);
					fn = sn.getPath_null_image();
					if (fn != null) {
						if (fn.contains("/"))
							fn = fn.substring(fn.lastIndexOf("/") + "/".length());
						url = LemnaTecFTPhandler.getLemnaTecFTPurl(host, experimentReq.getDatabase() + "/"
								+ sn.getPath_null_image(), sn.getId_tag()
								+ (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
						image.setLabelURL(url);
					}
					
					measurements.add(image);
				}
				processedSnapshotTimes.add(sn.getTimestamp().getTime());
			}
			
		}
		if (optStatus != null)
			optStatus.setCurrentStatusValue(-1);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Sort measurements (" + measurements.size() + ")");
		
		Collections.sort(measurements, new Comparator<NumericMeasurementInterface>() {
			@Override
			public int compare(NumericMeasurementInterface arg0, NumericMeasurementInterface arg1) {
				int timeComparison = arg0.getParentSample().getRowId() < arg1.getParentSample().getRowId() ? -1 : (arg0.getParentSample().getRowId() > arg1
						.getParentSample().getRowId() ? 1 : 0);
				if (timeComparison != 0)
					return timeComparison;
				else
					return arg0.getQualityAnnotation().compareTo(arg1.getQualityAnnotation());
			}
		});
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Create experiment (" + measurements.size() + " measurements)");
		
		ExperimentInterface experiment = NumericMeasurement3D.getExperiment(measurements, true, false, true);
		
		int numberOfImages = countMeasurementValues(experiment, new MeasurementNodeType[] { MeasurementNodeType.IMAGE });
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Experiment created (" + numberOfImages + " images)");
		experimentReq.setNumberOfFiles(numberOfImages);
		String seq = experiment.getSequence();
		experiment.setHeader(new ExperimentHeader(experimentReq));
		experiment.getHeader().setSequence(seq);
		if (seq != null && seq.contains("SeedDate")) {
			String[] values = seq.split(";");
			seedDateLookupLoop: for (String v : values) {
				if (v.contains("SeedDate") && v.contains(":")) {
					String[] descAndVal = v.split(":", 2);
					String seedDate = descAndVal[1];
					String[] dayMonthYear = seedDate.split("\\.", 3);
					int year = Integer.parseInt(dayMonthYear[2].trim());
					int month = Integer.parseInt(dayMonthYear[1].trim());
					int day = Integer.parseInt(dayMonthYear[0].trim());
					GregorianCalendar cal = new GregorianCalendar(year, month - 1, day);
					Date seedDateDate = cal.getTime();
					Date startDate = experiment.getHeader().getStartdate();
					int days = DateUtil.getElapsedDays(seedDateDate, startDate);
					if (startDate.before(seedDateDate))
						days = -days;
					updateSnapshotTimes(experiment, days, "das");
					break seedDateLookupLoop;
				}
			}
		}
		if (optStatus != null)
			optStatus.setCurrentStatusValue(100);
		// Collections.sort(experiment, new Comparator<SubstanceInterface>() {
		// @Override
		// public int compare(SubstanceInterface arg0, SubstanceInterface arg1) {
		// return arg0.getName().compareTo(arg1.getName());
		// }
		// });
		// for (SubstanceInterface si : experiment) {
		// Collections.sort(si, new Comparator<ConditionInterface>() {
		// @Override
		// public int compare(ConditionInterface o1, ConditionInterface o2) {
		// return o1.toString().compareTo(o2.toString());
		// }
		// });
		// }
		return experiment;
	}
	
	private void updateSnapshotTimes(ExperimentInterface experiment, int add, String newTimeUnit) {
		for (SubstanceInterface si : experiment) {
			for (ConditionInterface ci : si) {
				for (SampleInterface s : ci) {
					int day = s.getTime() - 1;
					s.setTime(day + add);
					s.setTimeUnit(newTimeUnit);
				}
			}
		}
	}
	
	private static String digit3(int i) {
		if (i < 10)
			return "00" + i;
		else
			if (i < 100)
				return "0" + i;
			else
				return "" + i;
	}
	
	private double processConfigBlobToGetRotationAngle(HashMap<String, Double> blob2angle, Snapshot sn, IOurl url) {
		try {
			byte[] buf;
			if (blob2angle.containsKey(url.toString()))
				return blob2angle.get(url.toString());
			else {
				InputStream in = url.getInputStream();
				// read configuration object in order to detect rotation angle
				try {
					MyByteArrayOutputStream out = new MyByteArrayOutputStream();
					byte[] temp = new byte[1024];
					int read = in.read(temp);
					while (read > 0) {
						out.write(temp, 0, read);
						read = in.read(temp);
					}
					buf = out.getBuff();
				} catch (Exception err) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: NO ROTATION ANGLE FOR URL " + url);
					return Double.NaN;
				}
			}
			TextFile tf = new TextFile(new MyByteArrayInputStream(buf, buf.length), 0);
			// System.out.println(url.toString());
			byte[] b = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			double angle = 0;
			for (String ss : tf) {
				if (ss.indexOf("angle") > 0) {
					ss = ss.substring(ss.indexOf("angle"));
					if (ss.length() > 30)
						ss = ss.substring(0, 30);
					ss = ss.substring("angle".length());
					int idx = 0;
					for (char c : ss.toCharArray()) {
						// System.out.print((byte) c + ",");
						b[idx++] = (byte) c;
						if (idx >= b.length) {
							// BinaryBlobCameraConfig.reverse(
							angle = BinaryBlobCameraConfig.arr2double(b, 0);
							break;
						}
					}
				}
			}
			// System.out.println("Config: " + sn.getCamera_label() + " / " +
			// sn.getUserDefinedCameraLabel() + ", angle: "
			// + angle);
			// System.out.println("");
			blob2angle.put(url.toString(), angle);
			return angle;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return 0;
		}
	}
	
	private HashMap<String, Condition> getPlantIdAnnotation(ExperimentHeaderInterface header) throws SQLException,
			ClassNotFoundException {
		HashMap<String, Condition> res = new HashMap<String, Condition>();
		
		Connection connection = openConnectionToDatabase(header.getDatabase());
		try {
			String sqlText = "SELECT id_tag, meta_data_name, meta_data_value, meta_data_type " + "FROM  meta_info_src "
					+ "WHERE measure_label = ?";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			ps.setString(1, header.getExperimentName());
			try {
				ResultSet rs = ps.executeQuery();
				
				while (rs.next()) {
					
					String plantID = rs.getString(1);
					
					String metaName = rs.getString(2);
					String metaValue = rs.getString(3);
					System.out.println("plantID: " + plantID + " metaName: " + metaName + " metaValue: " + metaValue);
					if (!res.containsKey(plantID)) {
						res.put(plantID, new Condition(null));
						if (header.getDatabase().contains("BGH_"))
							res.get(plantID).setSpecies("Barley");
						if (header.getDatabase().contains("APH_"))
							res.get(plantID).setSpecies("Arabidopsis");
						if (header.getDatabase().contains("CGH_"))
							res.get(plantID).setSpecies("Maize");
					}
					
					if (metaName.equalsIgnoreCase("Species") || metaName.equalsIgnoreCase("Pflanzenart"))
						// res.get(plantID).setSpecies(filterName(metaValue));
						res.get(plantID).setSpecies(metaValue);
					else
						if (metaName.equalsIgnoreCase("Genotype") || metaName.equalsIgnoreCase("Pflanzenname") || metaName.equalsIgnoreCase("Name")
								|| metaName.equalsIgnoreCase("GENOTYP"))
							res.get(plantID).setGenotype(metaValue);
						else
							if (metaName.equalsIgnoreCase("Variety"))
								res.get(plantID).setVariety(metaValue);
							else
								if (metaName.equalsIgnoreCase("Treatment") || metaName.equalsIgnoreCase("Typ"))
									res.get(plantID).setTreatment(metaValue);
								else
									if (metaName.equalsIgnoreCase("Growthconditions") || metaName.equalsIgnoreCase("Pot"))
										res.get(plantID).setGrowthconditions(metaValue);
									else
										if (metaName.equalsIgnoreCase("Sequence") || metaName.equalsIgnoreCase("SEEDDATE") || metaName.equalsIgnoreCase("seed date"))
											addSequenceInfo(res.get(plantID), "SeedDate: " + metaValue, header);
										else
											addSequenceInfo(res.get(plantID), metaName + ": " + metaValue, header);
					
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				System.err.println("ERROR: " + e.getMessage());
			}
		} finally {
			closeDatabaseConnection(connection);
		}
		return res;
	}
	
	private void addSequenceInfo(Condition condition, String value, ExperimentHeaderInterface header) {
		String current = (condition.getSequence() != null) ? condition.getSequence() : "";
		if (current.length() > 0)
			current += ";";
		current += value;
		condition.setSequence(current);
		header.setSequence(current);
	}
	
	private String filterName(String metaValue) {
		String result = metaValue;
		if (result.indexOf("_") > 0)
			result = result.substring(0, result.indexOf("_"));
		return result;
	}
	
	private int countMeasurementValues(ExperimentInterface experiment, MeasurementNodeType[] measurementNodeTypes) {
		int res = 0;
		for (MeasurementNodeType m : measurementNodeTypes) {
			List<NumericMeasurementInterface> rr = Substance3D.getAllFiles(experiment, m);
			res += rr.size();
		}
		return res;
	}
	
	public boolean isUserKnown(String u, String p) throws Exception {
		Connection connection = openConnectionToDatabase("LTSystem");
		boolean ok = false;
		try {
			String sqlText = "SELECT role, db_ids, removed " + "FROM  ltuser "
					+ "WHERE name = ? AND passwd = ?";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			ps.setString(1, u);
			ps.setString(2, p);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				int role = rs.getInt(1);
				String db_ids = rs.getString(2);
				boolean removed = rs.getBoolean(3);
				
				if (!removed) {
					ok = true;
				}
			}
			rs.close();
			ps.close();
		} finally {
			closeDatabaseConnection(connection);
		}
		return ok;
	}
}
