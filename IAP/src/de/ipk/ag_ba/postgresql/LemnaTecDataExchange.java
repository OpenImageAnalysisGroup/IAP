/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

import java.io.File;
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
import java.util.TreeMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.datasources.ExperimentLoader;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
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
public class LemnaTecDataExchange implements ExperimentLoader {
	private final String user;
	private final String password;
	private final String port;
	private final String host;
	
	private static final String driver = "org.postgresql.Driver";
	
	public LemnaTecDataExchange() {
		user = IAPoptions.getInstance().getString("LemnaTec-DB", "user", "postgres");
		password = IAPoptions.getInstance().getString("LemnaTec-DB", "password", "LemnaTec");
		port = IAPoptions.getInstance().getString("LemnaTec-DB", "port", "5432");
		host = IAPoptions.getInstance().getString("LemnaTec-DB", "host", "lemna-db.ipk-gatersleben.de");
	}
	
	public Collection<String> getDatabases() throws SQLException, ClassNotFoundException {
		HashSet<String> invalidDBs = new HashSet<String>();
		
		String[] def = new String[] {
				"postgres",
				"bacula",
				"LTSystem",
				"LTTestDB",
				"template0",
				"template1",
				"LemnaTest",
				"CornTest2",
				"CornTest3"
		};
		
		for (String invalid : IAPoptions.getInstance().getStringAll("LemnaTec-DB", "ignore_db", def)) {
			invalidDBs.add(invalid);
		}
		
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
	
	// private static HashMap<String, Collection<ExperimentHeaderInterface>> memRes1 = new HashMap<String, Collection<ExperimentHeaderInterface>>();
	
	public synchronized Collection<ExperimentHeaderInterface> getExperimentsInDatabase(String user, String database) throws ClassNotFoundException, SQLException {
		return getExperimentsInDatabase(user, database, null);
	}
	
	public static boolean known(String dbName) {
		return dbName != null && (dbName.startsWith("CGH_") || dbName.startsWith("BGH_") || dbName.startsWith("APH_"));
	}
	
	public synchronized ArrayList<ExperimentHeaderInterface> getExperimentsInDatabase(String user, String database,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws SQLException, ClassNotFoundException {
		ArrayList<ExperimentHeaderInterface> res = null;// memRes1.get(user + ";" + database);
		// if (res == null || System.currentTimeMillis() - updateTime > 2 * 60 * 1000) {
		res = getExperimentsInDatabaseIC(user, database, optStatus);
		// updateTime = System.currentTimeMillis();
		// memRes1.put(user + ";" + database, res);
		// }
		return res;
	}
	
	private ArrayList<ExperimentHeaderInterface> getExperimentsInDatabaseIC(String user, String database,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws SQLException, ClassNotFoundException {
		// System.out.println("GET EXP LIST LT");
		String sqlText = "SELECT distinct(measurement_label) FROM snapshot"; // ORDER BY measurement_label
		
		ArrayList<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Connect to database...");
		Connection connection = openConnectionToDatabase(database);
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Get list of experiments");
		try {
			PreparedStatement ps = connection.prepareStatement(sqlText);
			
			ResultSet rs = ps.executeQuery();
			
			// Collection<String> result = new TreeSet<String>();
			
			while (rs.next()) {
				ExperimentHeaderInterface ehi = new ExperimentHeader();
				String name = rs.getString(1);
				
				ehi.setExperimentname(name);
				
				ehi.setDatabase(database);
				ehi.setDatabaseId("lemnatec:" + database + ":" + ehi.getExperimentName());
				ehi.setOriginDbId("lemnatec:" + database + ":" + ehi.getExperimentName());
				ehi.setImportusername(user != null ? user : SystemAnalysis.getUserName());
				ehi.setImportusergroup("LemnaTec");
				LemnaTecSystem system = LemnaTecSystem.getTypeFromDatabaseName(database);
				if (system == LemnaTecSystem.Barley) {
					ehi.setExperimenttype(IAPexperimentTypes.BarleyGreenhouse + "");
					ehi.setImportusergroup("LemnaTec (BGH)");
				} else
					if (system == LemnaTecSystem.Maize) {
						if (name.length() >= 6 && name.toUpperCase().endsWith("RAPS"))
							ehi.setExperimenttype(IAPexperimentTypes.Raps + "");
						else
							if (name.length() >= 6 && name.substring(4, 6).equals("RM"))
								ehi.setExperimenttype(IAPexperimentTypes.BarleyGreenhouse + "");
							else
								ehi.setExperimenttype(IAPexperimentTypes.MaizeGreenhouse + "");
						ehi.setImportusergroup("LemnaTec (CGH)");
					} else
						if (system == LemnaTecSystem.Phytochamber) {
							ehi.setExperimenttype(IAPexperimentTypes.Phytochamber + "");
							ehi.setImportusergroup("LemnaTec (APH)");
						} else {
							ehi.setExperimenttype(IAPexperimentTypes.UnknownGreenhouse + "");
							ehi.setImportusergroup("LemnaTec (Other)");
						}
				// ehi.setSequence("");
				ehi.setSizekb(-1);
				result.add(ehi);
			}
			
			rs.close();
			ps.close();
			
			sqlText = "" +
					"SELECT min(time_stamp), max(time_stamp) " + // , count(*)
					"FROM snapshot " + // ,tiled_image " +
					"WHERE measurement_label=?";// ; AND tiled_image.snapshot_id=snapshot.id";
			ps = connection.prepareStatement(sqlText);
			for (ExperimentHeaderInterface ehi : result) {
				if (ehi.getExperimentName() == null)
					continue;
				ps.setString(1, ehi.getExperimentName());
				if (optStatus != null)
					optStatus.setCurrentStatusText2("Determine time stamps (" + ehi.getExperimentName() + ")");
				
				rs = ps.executeQuery();
				while (rs.next()) {
					Timestamp min = rs.getTimestamp(1);
					Timestamp max = rs.getTimestamp(2);
					if (min == null || max == null) {
						System.out.println("Warning: No snapshot times stored for experiment " + ehi.getExperimentName()
								+ " in database " + ehi.getDatabase() + "!");
					}
					if (min != null)
						ehi.setStartdate(new Date(min.getTime()));
					else
						ehi.setStartdate(new Date());
					if (max != null)
						ehi.setImportdate(new Date(max.getTime()));
					else
						ehi.setImportdate(new Date());
					// ehi.setNumberOfFiles(rs.getInt(3));
					break;
				}
				rs.close();
			}
			ps.close();
			
			sqlText = "SELECT "
					+ "	count(*) "
					+ "FROM "
					+ "	snapshot, tiled_image "
					+ "WHERE "
					+ "	snapshot.measurement_label = ? and "
					+ "	snapshot.id = tiled_image.snapshot_id";
			ps = connection.prepareStatement(sqlText);
			for (ExperimentHeaderInterface ehi : result) {
				if (ehi.getExperimentName() == null)
					continue;
				ps.setString(1, ehi.getExperimentName());
				if (optStatus != null)
					optStatus.setCurrentStatusText2("Determine image count (" + ehi.getExperimentName() + ")");
				rs = ps.executeQuery();
				while (rs.next()) {
					ehi.setNumberOfFiles(rs.getInt(1));
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
						if (optStatus != null)
							optStatus.setCurrentStatusText2("Determine user names (" + ehi.getExperimentName() + ")");
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
				if (optStatus != null)
					optStatus.setCurrentStatusText2("Determine user names (" + ehi.getExperimentName() + ")");
				rs = ps.executeQuery();
				names.clear();
				while (rs.next()) {
					String name = rs.getString(1);
					names.add(getNiceNameFromLoginName(name));
					people.get(ehi).add(name);
				}
				if (names.size() > 1) {
					names.remove(getNiceNameFromLoginName("muecke"));
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
		} finally {
			closeDatabaseConnection(connection);
		}
		for (ExperimentHeaderInterface ehi : result) {
			String db = ehi.getDatabase();
			String name = ehi.getExperimentName();
			if (!known(db))
				ehi.setExperimentname(name + " (" + db + ")");
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Found " + result.size() + " experiments in db " + database);
		
		return result;
	}
	
	private HashMap<String, String> login2niceName = null;
	
	private String getNiceNameFromLoginName(String name) {
		// System.out.println("Request nice name for " + name);
		if (login2niceName == null) {
			login2niceName = new HashMap<String, String>();
			login2niceName.put("Fernando", "Arana, Dr. Fernando (HET)");
			login2niceName.put("Gramel-Eikenroth", "Gramel-Eikenroth (LemnaTec)");
			login2niceName.put("LTAdmin", "LTAdmin (LemnaTec)");
			login2niceName.put("LemnaTec Support", "LemnaTec Support (LemnaTec)");
			login2niceName.put("entzian", "Entzian, Dr. Alexander (BA)");
			login2niceName.put("Neumannk", "Neumann, Dr. Kerstin (GED)");
			login2niceName.put("neumannk", "Neumann, Dr. Kerstin (GED)");
			login2niceName.put("hartmann", "Hartmann, Anja (PBI)");
			login2niceName.put("mary", "Ziems, Mary (GED");
			login2niceName.put("Ziems", "Ziems, Mary (GED");
			login2niceName.put("stein", "Stein, Dr. Nils (GED");
			login2niceName.put("altmann", "Altmann, Prof. Dr. Thomas (MOG)");
			login2niceName.put("meyer", "Meyer, Dr. Rhonda (HET)");
			login2niceName.put("muraya", "Muraya, Dr. Moses Mahugu (HET)");
			login2niceName.put("Muraya", "Muraya, Dr. Moses Mahugu (HET)");
			login2niceName.put("weigelt", "Weigelt-Fischer, Dr. Kathleen (HET)");
			login2niceName.put("Muecke", "Muecke, Ingo (BA)");
			login2niceName.put("muecke", "Muecke, Ingo (BA)");
			login2niceName.put("seyfarth", "Seyfarth, Monique (HET)");
			login2niceName.put("klukas", "Klukas, Dr. Christian (BA)");
		}
		String res = login2niceName.get(name);
		// System.out.println("Result: " + res);
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
			id2coo.put("IL", "Lermontova, Dr. Inna (KTE)");
			id2coo.put("AC", "Arana, Dr. Fernando (HET)");
			id2coo.put("FA", "Arana, Dr. Fernando (HET)");
			id2coo.put("RM", "Meyer, Dr. Rhonda (HET)");
			id2coo.put("MM", "Muraya, Dr. Moses Mahugu (HET)");
			id2coo.put("KN", "Neumann, Kerstin (GED)");
			id2coo.put("KW", "Weigelt-Fischer, Dr. Kathleen (HET)");
			id2coo.put("KWF", "Weigelt-Fischer, Dr. Kathleen (HET)");
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
		if (experiment != null && experiment.endsWith(" (" + database + ")"))
			experiment = experiment.substring(0, experiment.length() - (" (" + database + ")").length());
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
			// getProperImageSnapshots(experiment, result, connection, id2path, knownSnaphotIds);
			// if (result.size() == 0) {
			getImageSnapshotsWithUnknownImageUnitConfiguration(experiment, result, connection, id2path, knownSnaphotIds);
			// }
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
					
					boolean printWater = false;
					if (printWater)
						System.out.println("Water data: " +
								snapshot.getWater_amount() + " // " + (snapshot.getWeight_after() - snapshot.getWeight_before()));
					
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
			
			boolean b = true;
			if (b)
				sqlText = "SELECT "
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
						+ "	tile.image_oid = image_file_table.id and"
						// and "
						// + "	snapshot.configuration_id = image_unit_configuration.id";// and"
						+ "	image_unit_configuration.gid = tiled_image.camera_label";
			ps = connection.prepareStatement(sqlText);
			ps.setString(1, experiment);
			
			rs = ps.executeQuery();
			HashSet<Long> known = new HashSet<Long>();
			while (rs.next()) {
				
				long ll = rs.getLong("image_oid");
				if (known.contains(ll))
					continue;
				else
					known.add(ll);
				
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
				// System.out.println("LABLAB: " + rs.getString("compname"));
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
			System.out.println("SNAPSHOTS: " + result.size());
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
				
				String camLbl = rs.getString("camera_label");
				String lbl = getIAPcameraNameFromConfigLabel(camLbl);
				snapshot.setCamera_label(lbl);// rs.getString("compname"));
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
	
	public static String getIAPcameraNameFromConfigLabel(String conf) {
		String res = "";
		for (String id : SystemOptions.getInstance().getStringAll("Import", "NIR-Camera-Config-Substrings", new String[] { "NIR" }))
			if (conf.toUpperCase().contains(id.toUpperCase())) {
				res += "nir.";
				break;
			}
		if (res.isEmpty())
			for (String id : SystemOptions.getInstance().getStringAll("Import", "VIS-Camera-Config-Substrings", new String[] { "VIS", "RGB" }))
				if (conf.toUpperCase().contains(id.toUpperCase())) {
					res += "vis.";
					break;
				}
		if (res.isEmpty())
			for (String id : SystemOptions.getInstance().getStringAll("Import", "FLUO-Camera-Config-Substrings", new String[] { "FLU" }))
				if (conf.toUpperCase().contains(id.toUpperCase())) {
					res += "fluo.";
					break;
				}
		boolean topFound = false;
		for (String id : SystemOptions.getInstance().getStringAll("Import", "Top-View-Camera-Config-Substrings", new String[] { "TOP", " TV" }))
			if (conf.toUpperCase().contains(id)) {
				res += "top";
				topFound = true;
				break;
			}
		if (!topFound)
			for (String id : SystemOptions.getInstance().getStringAll("Import", "Side-View-Camera-Config-Substrings", new String[] { "SIDE", " SV" }))
				if (conf.toUpperCase().contains(id)) {
					res += "side";
					break;
				}
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
	
	@Override
	public ExperimentInterface getExperiment(ExperimentHeaderInterface experimentReq,
			boolean interactiveGetExperimentSize_notUsedHere,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws SQLException, ClassNotFoundException {
		return getExperiment(experimentReq, interactiveGetExperimentSize_notUsedHere, optStatus, null, null);
	}
	
	private ExperimentInterface getExperiment(ExperimentHeaderInterface experimentReq,
			boolean interactiveGetExperimentSize_notUsedHere,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			Collection<Snapshot> optSnapshots, HashMap<String,
			Condition> optIdTag2condition) throws SQLException, ClassNotFoundException {
		ArrayList<NumericMeasurementInterface> measurements = new ArrayList<NumericMeasurementInterface>();
		
		String species = "";
		String genotype = "";
		String variety = "";
		String growthconditions = "";
		String treatment = "";
		String sequence = "";
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Read database");
		if (optStatus != null)
			optStatus.setCurrentStatusValue(-1);
		
		Collection<Snapshot> snapshots = optSnapshots;
		if (optSnapshots == null)
			snapshots = getSnapshotsOfExperiment(
					experimentReq.getDatabase(), experimentReq.getExperimentName());
		HashMap<String, Integer> idtag2replicateID = new HashMap<String, Integer>();
		
		Timestamp earliest = null;
		Timestamp latest = null;
		{
			TreeSet<String> ids = new TreeSet<String>();
			for (Snapshot sn : snapshots) {
				ids.add(sn.getId_tag());
				if (earliest == null || sn.getTimestamp().before(earliest))
					earliest = sn.getTimestamp();
				if (latest == null || sn.getTimestamp().after(latest))
					latest = sn.getTimestamp();
			}
			int replID = 0;
			for (String id : ids) {
				replID++;
				idtag2replicateID.put(id, replID);
				// System.out.println(id + ";" + replID);
			}
		}
		if (experimentReq.getStartdate() == null && earliest != null)
			experimentReq.setStartdate(new Date(earliest.getTime()));
		if (experimentReq.getImportdate() == null && earliest != null)
			experimentReq.setImportdate(new Date(latest.getTime()));
		
		HashMap<String, Condition> idtag2condition = experimentReq.getDatabaseId() != null ? getPlantIdAnnotation(experimentReq)
				: null;
		
		if (idtag2condition == null)
			idtag2condition = optIdTag2condition;
		
		if (idtag2condition == null) {
			idtag2condition = new HashMap<String, Condition>();
			for (Snapshot s : snapshots) {
				if (s.getId_tag() != null && !s.getId_tag().isEmpty()) {
					// derive genotype from plant ID information by removing trailing numbers until the first letter
					// and by removing '-' from the result and by converting everything to upper case
					String id = s.getId_tag();
					if (idtag2condition.containsKey(id))
						continue;
					StringBuilder idPretty = new StringBuilder();
					boolean numbersAllowed = false;
					for (char c : id.toCharArray()) {
						if (c == '-')
							continue;
						if (numbersAllowed || !(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9')) {
							numbersAllowed = true;
							idPretty.append(c);
						}
					}
					if (idPretty.length() > 0) {
						Condition ct = new Condition(null);
						ct.setGenotype(idPretty.toString().toUpperCase());
						idtag2condition.put(id, ct);
					}
				}
			}
		}
		
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
			
			String idTag = sn.getId_tag();
			
			Integer replicateID = idtag2replicateID.get(idTag);
			
			if (replicateID == null) {
				System.out.println("Warning: internal IAP error. Could not create get replicate ID for ID tag '"
						+ sn.getId_tag() + "'. Snapshot is ignored.");
				continue;
			}
			
			HashSet<String> printedInvalidIdTags = new HashSet<String>();
			Condition conditionTemplate = idtag2condition.get(sn.getId_tag());
			if (conditionTemplate == null) {
				if (!printedInvalidIdTags.contains(sn.getId_tag())) {
					System.out.println("No meta-data for ID " + sn.getId_tag());
					printedInvalidIdTags.add(sn.getId_tag());
					
				}
				Integer targetGroup = null;
				try {
					String s = sn.getId_tag();
					s = StringManipulationTools.getNumbersFromString(s);
					targetGroup = Integer.parseInt(s);
					targetGroup = targetGroup % 10;
				} catch (Exception e) {
					// empty
				}
				Condition ct = new Condition(null);
				ct.setSequence("");
				ct.setSpecies("not specified");
				if (targetGroup == null)
					ct.setGenotype("not specified (random " +
							StringManipulationTools.formatNumber(Math.floor(Math.random() * 10 + 1d), "00") + ")");
				else
					ct.setGenotype("not specified (group " +
							StringManipulationTools.formatNumber(targetGroup.doubleValue(), "00") + ")");
				
				ct.setVariety("");
				ct.setGrowthconditions("");
				ct.setSequence("");
				ct.setTreatment("");
				idtag2condition.put(sn.getId_tag(), ct);
				conditionTemplate = idtag2condition.get(sn.getId_tag());
			}
			sequence = conditionTemplate.getSequence();
			species = conditionTemplate.getSpecies();
			genotype = conditionTemplate.getGenotype();
			variety = conditionTemplate.getVariety();
			growthconditions = conditionTemplate.getGrowthconditions();
			treatment = conditionTemplate.getTreatment();
			
			{
				String lbl = sn.getCamera_label();
				if (lbl != null && lbl.startsWith("imagingunits."))
					lbl = lbl.substring("imagingunits.".length());
				if (lbl != null && lbl.contains("#"))
					lbl = lbl.substring(0, lbl.indexOf("#"));
				// if (lbl != null && lbl.equals("top"))
				// lbl = "ir.top";
				sn.setCamera_label(lbl);
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
				condition.setSequence(sequence);
				
				Sample sample = new Sample(condition);
				sample.setTime(day);
				sample.setTimeUnit("day");
				sample.setSampleFineTimeOrRowId(sn.getTimestamp().getTime());
				
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
			
			if (sn.getWater_amount() > 0) {
				// process water_amount
				String iidd = day + "/" + replicateID;
				
				double wa = sn.getWeight_after();
				double wb = sn.getWeight_before();
				
				if (!Double.isNaN(wb) && !Double.isNaN(wa)) {
					
					if (knownDayAndReplicateIDs.containsKey(iidd)) {
						NumericMeasurement water = knownDayAndReplicateIDs.get(iidd);
						water.setValue(water.getValue() + (wa - wb));// sn.getWater_amount());
					} else {
						{
							Substance s = new Substance();
							s.setName("water_weight");
							s.setFormula("H2O");
							
							Condition condition = new Condition(s);
							condition.setExperimentInfo(experimentReq);
							condition.setSpecies(species);
							condition.setGenotype(genotype);
							condition.setVariety(variety);
							condition.setGrowthconditions(growthconditions);
							condition.setTreatment(treatment);
							condition.setSequence(sequence);
							
							Sample sample = new Sample(condition);
							sample.setTime(day);
							sample.setTimeUnit("day");
							sample.setSampleFineTimeOrRowId(sn.getTimestamp().getTime());
							
							NumericMeasurement weightBefore = new NumericMeasurement(sample);
							weightBefore.setReplicateID(replicateID);
							weightBefore.setUnit("g");
							weightBefore.setValue(wa - wb);
							weightBefore.setQualityAnnotation(idTag);
							
							measurements.add(weightBefore);
						}
						{
							Substance s = new Substance();
							s.setName("water_sum");
							s.setFormula("H2O");
							
							Condition condition = new Condition(s);
							condition.setExperimentInfo(experimentReq);
							condition.setSpecies(species);
							condition.setGenotype(genotype);
							condition.setVariety(variety);
							condition.setGrowthconditions(growthconditions);
							condition.setTreatment(treatment);
							condition.setSequence(sequence);
							
							Sample sample = new Sample(condition);
							sample.setTime(day);
							sample.setTimeUnit("day");
							sample.setSampleFineTimeOrRowId(sn.getTimestamp().getTime());
							
							NumericMeasurement water = new NumericMeasurement(sample);
							water.setReplicateID(replicateID);
							water.setUnit("g");
							water.setValue(wa - wb);// sn.getWater_amount());
							water.setQualityAnnotation(idTag);
							
							measurements.add(water);
							
							knownDayAndReplicateIDs.put(iidd, water);
						}
					}
				}
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
					condition.setSequence(sequence);
					
					Sample sample = new Sample(condition);
					sample.setTime(day);
					sample.setTimeUnit("day");
					sample.setSampleFineTimeOrRowId(sn.getTimestamp().getTime());
					
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
					try {
						String a = sn.getUserDefinedCameraLabel();
						if (a != null && a.length() > 3)
							a = a.substring(2);
						String b = StringManipulationTools.getNumbersFromString(a);
						if (b.length() == 0)
							position = 0d;
						else
							position = Double.parseDouble(b);
						if (position > 360)
							throw new NumberFormatException("Number too large");
					} catch (NumberFormatException nfe) {
						if (fn != null) {
							try {
								position = Double.parseDouble(fn);
							} catch (NumberFormatException e) {
								if (fn.contains("/"))
									fn = fn.substring(fn.lastIndexOf("/") + "/".length());
								IOurl url = LemnaTecFTPhandler.getLemnaTecFTPurl(experimentReq.getDatabase() + "/"
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
					
					IOurl url;
					if (experimentReq.getDatabase() != null)
						url = LemnaTecFTPhandler.getLemnaTecFTPurl(experimentReq.getDatabase() + "/"
								+ sn.getPath_image(), sn.getId_tag() + (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
					else {
						url = FileSystemHandler.getURL(new File(sn.getPath_image()));
						url.setFileName(url.getFileName() + "#" + sn.getId_tag() + (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
					}
					image.setURL(url);
					fn = sn.getPath_null_image();
					if (fn != null) {
						if (fn.contains("/"))
							fn = fn.substring(fn.lastIndexOf("/") + "/".length());
						url = LemnaTecFTPhandler.getLemnaTecFTPurl(experimentReq.getDatabase() + "/"
								+ sn.getPath_null_image(),
								"ref_" + sn.getPath_null_image().substring(sn.getPath_null_image().lastIndexOf("/") + "/".length()) + ".png"
								// sn.getId_tag() + (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png")
								);
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
				int timeComparison = arg0.getParentSample().getSampleFineTimeOrRowId() < arg1.getParentSample().getSampleFineTimeOrRowId() ? -1 : (arg0
						.getParentSample().getSampleFineTimeOrRowId() > arg1
						.getParentSample().getSampleFineTimeOrRowId() ? 1 : 0);
				if (timeComparison != 0)
					return timeComparison;
				else
					return arg0.getQualityAnnotation().compareTo(arg1.getQualityAnnotation());
			}
		});
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Create experiment (" + measurements.size() + " measurements)");
		
		ExperimentInterface experiment = NumericMeasurement3D.getExperiment(
				measurements, true, false, true, optStatus);
		
		int numberOfImages = countMeasurementValues(experiment, new MeasurementNodeType[] { MeasurementNodeType.IMAGE });
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Experiment created (" + numberOfImages + " images)");
		if (experimentReq != null)
			experimentReq.setNumberOfFiles(numberOfImages);
		String seq = experimentReq != null ? experimentReq.getSequence() : null;
		experiment.setHeader(new ExperimentHeader(experimentReq));
		experiment.getHeader().setSequence(seq);
		
		if (experimentReq != null) {
			experiment.getHeader().setDatabaseId("lemnatec:" + experimentReq.getDatabase() + ":" + experimentReq.getExperimentName());
			experiment.getHeader().setOriginDbId("lemnatec:" + experimentReq.getDatabase() + ":" + experimentReq.getExperimentName());
		}
		
		if (seq != null && StringManipulationTools.containsAny(seq, getMetaNamesSeedDates())) {
			String[] values = seq.split("//");
			seedDateLookupLoop: for (String v : values) {
				v = v.trim();
				if (StringManipulationTools.containsAny(v, getMetaNamesSeedDates()) && v.contains(":")) {
					try {
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
					} catch (Exception err) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Invalid seed-date definition in plant mapping: " + v);
					}
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
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: NO ROTATION ANGLE FOR URL " + url);
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
		
		HashSet<String> globalEnvironmentMetaNames = getEnvironmentMetaNames();
		
		Connection connection = openConnectionToDatabase(header.getDatabase());
		try {
			String sqlText = "SELECT id_tag, meta_data_name, meta_data_value, meta_data_type " + "FROM  meta_info_src "
					+ "WHERE measure_label = ?";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			
			String expType = header.getExperimentType();
			String species = IAPexperimentTypes.getSpeciesFromExperimentType(expType);
			
			ps.setString(1, header.getExperimentName());
			HashSet<String> printedMetaData = new HashSet<String>();
			try {
				ResultSet rs = ps.executeQuery();
				
				HashSet<String> seedDateNames = getMetaNamesSeedDates();
				
				while (rs.next()) {
					
					String plantID = rs.getString(1);
					String metaName = rs.getString(2);
					String metaValue = rs.getString(3);
					if (metaValue != null)
						metaValue = metaValue.trim();
					String info = "metaName: " + metaName + " metaValue: " + metaValue;
					if (!printedMetaData.contains(info)) {
						System.out.println(info);
						printedMetaData.add(info);
					}
					
					if (!res.containsKey(plantID)) {
						// System.out.println(plantID);
						res.put(plantID, new Condition(null));
						res.get(plantID).setSpecies(species);
					}
					
					if (seedDateNames.contains(metaName)) {
						addSequenceInfoToExperiment(header, metaName + ": " + metaValue);
					} else
						if (metaName.equalsIgnoreCase("Sequence") || metaName.equalsIgnoreCase("Transplantingdate")) {
							addSequenceInfoToCondition(res.get(plantID), metaName, metaValue);
						} else
							if (metaName.equalsIgnoreCase("Species") || metaName.equalsIgnoreCase("Pflanzenart") ||
									metaName.equalsIgnoreCase("Plant"))
								// res.get(plantID).setSpecies(filterName(metaValue));
								res.get(plantID).setSpecies(metaValue);
							else
								if (metaName.equalsIgnoreCase("Genotype") || metaName.equalsIgnoreCase("Pflanzenname")
										|| metaName.equalsIgnoreCase("Name")
										|| metaName.equalsIgnoreCase("GENOTYP")
								// || metaName.equalsIgnoreCase("TYP")
								)
									res.get(plantID).setGenotype(metaValue);
								else
									if (metaName.equalsIgnoreCase("Variety") || metaName.equalsIgnoreCase("variety-tax")
											|| metaName.equalsIgnoreCase("ID genotype") || metaName.equalsIgnoreCase("ID Genotyp"))
										res.get(plantID).setVariety(metaValue);
									else
										if (metaName.equalsIgnoreCase("Growthconditions") || metaName.equalsIgnoreCase("Pot") ||
												metaName.equalsIgnoreCase("Topf") ||
												metaName.equalsIgnoreCase("Covering") ||
												metaName.equalsIgnoreCase("Potting")) {
											addGrowthCondition(res.get(plantID), metaName, metaValue);
										} else
											if (globalEnvironmentMetaNames.contains(metaName)) {
												addSequenceInfoToExperiment(header, metaName + ": " + metaValue);
											} else {
												if (metaValue != null && metaValue.trim().length() > 0) {
													String oldTreatment = res.get(plantID).getTreatment();
													if (oldTreatment == null)
														oldTreatment = "";
													if (oldTreatment.length() > 0 && !oldTreatment.endsWith(";"))
														oldTreatment = oldTreatment + ";";
													
													String oldVariety = res.get(plantID).getVariety();
													if (oldVariety == null)
														oldVariety = "";
													if (oldVariety.length() > 0)
														oldVariety = oldVariety + ";";
													
													// if (metaName.startsWith("ID Genotyp")) {
													// ignore this annotation, it was used by KFW, AG HET to identify the replicate
													// the plant ID is used for identification of the replicate, already. So this
													// information is not needed and disturbs the definition of the treatment information
													// } else
													if (metaName.startsWith("old ID")) {
														if (!oldTreatment.contains("old IDs defined"))
															res.get(plantID).setTreatment(oldTreatment + "old IDs defined");
													} else
														if (metaName.startsWith("Sorte")) {
															if (!oldTreatment.contains(metaValue.trim()))
																res.get(plantID).setTreatment(oldTreatment + metaValue.trim());
														} else {
															if (metaName.equalsIgnoreCase("Typ")) {
																// addSequenceInfoToCondition(res.get(plantID), metaName + ": " + metaValue.trim());
																res.get(plantID).setTreatment(oldTreatment + metaValue.trim());
															} else {
																if (metaName.startsWith("conditions "))
																	metaName = metaName.substring("conditions ".length()).trim();
																
																if (!oldTreatment.contains(metaName + ": " + metaValue.trim()))
																	res.get(plantID).setTreatment(oldTreatment + metaName + ": " + metaValue.trim());
																else {
																	if (metaValue != null && metaValue.trim().length() > 0) {
																		String ss = metaName + ": " + metaValue;
																		if (oldTreatment != null && oldTreatment.length() > 0) {
																			if (!oldTreatment.contains(ss))
																				res.get(plantID).setTreatment(oldTreatment + ss);
																		} else
																			res.get(plantID).setTreatment(ss);
																	}
																}
															}
														}
													String currentTreatment = res.get(plantID).getTreatment();
													if (currentTreatment != null) {
														TreeSet<String> content = new TreeSet<String>();
														for (String s : currentTreatment.split(";")) {
															s = s.trim();
															if (!s.isEmpty())
																content.add(s);
														}
														res.get(plantID).setTreatment(StringManipulationTools.getStringList(content, ";"));
													}
												}
											}
					
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("ERROR: " + e.getMessage());
			}
		} finally {
			closeDatabaseConnection(connection);
		}
		return res;
	}
	
	private void addGrowthCondition(Condition condition, String metaName, String metaValue) {
		String con = condition.getGrowthconditions();
		if (con == null || con.trim().isEmpty()) {
			if (metaName.equalsIgnoreCase("Growthconditions"))
				condition.setGrowthconditions(metaValue);
			else
				condition.setGrowthconditions(metaName + ":" + metaValue);
		} else {
			condition.setGrowthconditions(con + " // " + metaName + ":" + metaValue);
		}
	}
	
	private HashSet<String> getMetaNamesSeedDates() {
		HashSet<String> res = new HashSet<String>();
		res.add("SEEDDATE");
		res.add("seed date");
		res.add("Aussaat");
		res.add("Sowing");
		res.add("Seeddate");
		return res;
	}
	
	private HashSet<String> getEnvironmentMetaNames() {
		HashSet<String> res = new HashSet<String>();
		res.add("SEEDDATE");
		res.add("seed date");
		res.add("Aussaat");
		res.add("Kühlung");
		res.add("Keimung");
		res.add("Start Experiment");
		res.add("Ende Experiment");
		res.add("Bewässerung");
		res.add("Aufnahme");
		res.add("Lichtintensität");
		res.add("Lichtzeitraum");
		res.add("Tageslänge");
		res.add("Temperatur (Tag)");
		res.add("Temperatur (Nacht)");
		res.add("rel.Luftfeuchte (Tag)");
		res.add("rel.Luftfeuchte (Nacht)");
		res.add("Day Length");
		res.add("Finish Experiment");
		res.add("Germination"); // ?? eventually plant specific
		res.add("Imaging");
		res.add("Light Intensity");
		res.add("Light Period");
		res.add("Sowing");
		res.add("Stratification");
		res.add("Temperature (Day)");
		res.add("Temperature (Night)");
		// res.add("Watering"); // used as treatment by 1207RM
		res.add("rel.Humidity (Day)");
		res.add("rel.Humidity (Night)");
		return res;
	}
	
	private void addSequenceInfoToExperiment(ExperimentHeaderInterface header, String value) {
		String current = (header.getSequence() != null) ? header.getSequence() : "";
		if (current.length() > 0)
			current += " // ";
		if (!current.contains(value + " // ")) {
			current += value;
			header.setSequence(current);
		}
	}
	
	private void addSequenceInfoToCondition(Condition condition, String metaName, String value) {
		String current = (condition.getSequence() != null) ? condition.getSequence() : "";
		if (current.length() > 0)
			current += " // ";
		if (!metaName.equals("Sequence"))
			value = metaName + ":" + value;
		if (!current.contains(value + " // ")) {
			current += value;
			condition.setSequence(current);
		}
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
	
	@Override
	public boolean canHandle(String databaseId) {
		return databaseId.startsWith(LemnaTecFTPhandler.PREFIX + ":");
	}
	
	public ArrayList<String> getMetaDataMeasurementLabels(String db) throws ClassNotFoundException, SQLException {
		ArrayList<String> measureLabels = new ArrayList<String>();
		Connection connection = openConnectionToDatabase(db);
		try {
			String sqlText = "SELECT distinct(measure_label) FROM  meta_info_src";
			PreparedStatement ps = connection.prepareStatement(sqlText);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				measureLabels.add(rs.getString(1));
			}
			rs.close();
			ps.close();
		} finally {
			closeDatabaseConnection(connection);
		}
		return measureLabels;
	}
	
	public ArrayList<MetaDataType> getMetaDataIdsForMeasureLabel(String db, String ml) throws Exception {
		ArrayList<MetaDataType> measureLabels = new ArrayList<MetaDataType>();
		Connection connection = openConnectionToDatabase(db);
		try {
			String sqlText = "SELECT distinct(meta_data_name), meta_data_type FROM  meta_info_src "
					+ "WHERE measure_label = ?";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			ps.setString(1, ml);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				measureLabels.add(new MetaDataType(rs.getString(1), rs.getString(2)));
			}
			rs.close();
			ps.close();
		} finally {
			closeDatabaseConnection(connection);
		}
		return measureLabels;
	}
	
	public ArrayList<String> getMetaDataValues(String db, String ml, String meta_data_name, boolean includeNumberInResult) throws Exception {
		ArrayList<String> res = new ArrayList<String>();
		Connection connection = openConnectionToDatabase(db);
		try {
			String sqlText = "SELECT meta_data_value FROM  meta_info_src "
					+ "WHERE measure_label = ? AND meta_data_name = ?";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			ps.setString(1, ml);
			ps.setString(2, meta_data_name);
			
			TreeMap<String, Integer> value2cnt = new TreeMap<String, Integer>();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String val = rs.getString(1);
				if (!value2cnt.containsKey(val))
					value2cnt.put(val, 0);
				value2cnt.put(val, value2cnt.get(val) + 1);
			}
			rs.close();
			ps.close();
			
			for (String key : value2cnt.keySet()) {
				if (includeNumberInResult)
					res.add(key + " (" + value2cnt.get(key) + ")");
				else
					res.add(key);
			}
		} finally {
			closeDatabaseConnection(connection);
		}
		return res;
	}
	
	public ArrayList<String> getMetaDataPlantIDs(String db, String ml) throws Exception {
		ArrayList<String> res = new ArrayList<String>();
		Connection connection = openConnectionToDatabase(db);
		try {
			String sqlText = "SELECT distinct(id_tag) FROM  meta_info_src "
					+ "WHERE measure_label = ?";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			ps.setString(1, ml);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String val = rs.getString(1);
				res.add(val);
			}
			rs.close();
			ps.close();
		} finally {
			closeDatabaseConnection(connection);
		}
		return res;
		
	}
	
	public static ExperimentInterface getExperimentFromSnapshots(ExperimentHeader eh,
			ArrayList<Snapshot> snapshots, HashMap<String, Condition> optIdTag2condition) throws ClassNotFoundException, SQLException {
		return new LemnaTecDataExchange().getExperiment(eh, false, null, snapshots, optIdTag2condition);
	}
}
