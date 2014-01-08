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
import org.apache.commons.lang3.StringUtils;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.commands.load_lt.TableDataHeadingRow;
import de.ipk.ag_ba.datasources.ExperimentLoader;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableDataStringRow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas, entzian
 */
public class LTdataExchange implements ExperimentLoader {
	private final String user;
	private final String password;
	private final String port;
	private final String host;
	
	private static final String driver = "org.postgresql.Driver";
	
	private static boolean debug;
	/**
	 * specifies if the camera images are stored as "top.vis", ... instead of the old style "vis.top", ...
	 */
	public static boolean positionFirst = true;
	
	public LTdataExchange() {
		user = IAPoptions.getInstance().getString("LT-DB", "PostgreSQL//user", "postgres");
		password = IAPoptions.getInstance().getString("LT-DB", "PostgreSQL//password", "");
		port = IAPoptions.getInstance().getString("LT-DB", "PostgreSQL//port", "5432");
		host = IAPoptions.getInstance().getString("LT-DB", "PostgreSQL//host", "lemna-db.ipk-gatersleben.de");
	}
	
	public Collection<String> getDatabases() throws SQLException, ClassNotFoundException {
		HashSet<String> invalidDBs = new HashSet<String>();
		
		String[] defaultIgnored = new String[] {
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
		
		String[] ig = IAPoptions.getInstance().getStringAll("LT-DB", "DBs//ignore_db", defaultIgnored);
		if (ig != null)
			for (String invalid : ig) {
				invalidDBs.add(invalid);
			}
		
		String sqlText = "SELECT datname FROM pg_database";
		
		Connection connection = openConnectionToDatabase("postgres");
		
		Collection<String> result = new TreeSet<String>();
		
		try {
			PreparedStatement ps = connection.prepareStatement(sqlText);
			
			if (isDebug())
				System.out.println(sqlText);
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				if (isDebug())
					System.out.println("Current Row: " + rs.getString(1));
				
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
	
	public synchronized Collection<ExperimentHeaderInterface> getExperimentsInDatabase(String user, String database) throws ClassNotFoundException, SQLException {
		return getExperimentsInDatabase(user, database, null);
	}
	
	public static boolean known(String dbName) {
		return dbName != null && (dbName.startsWith("CGH_") || dbName.startsWith("BGH_") || dbName.startsWith("APH_"));
	}
	
	public synchronized ArrayList<ExperimentHeaderInterface> getExperimentsInDatabase(String user, String database,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus)
			throws SQLException, ClassNotFoundException {
		if (database == null || database.equals("null")) {
			ArrayList<ExperimentHeaderInterface> resForAll = new ArrayList<ExperimentHeaderInterface>();
			for (String db : getDatabases()) {
				try {
					ArrayList<ExperimentHeaderInterface> res = null;
					res = getExperimentsInDatabaseIC(user, db, optStatus);
					if (res != null && res.size() > 0)
						resForAll.addAll(res);
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Could not get list of experiments for LT database " + db + ". Error: "
							+ e.getMessage());
				}
			}
			return resForAll;
		} else {
			ArrayList<ExperimentHeaderInterface> res = null;
			res = getExperimentsInDatabaseIC(user, database, optStatus);
			return res;
		}
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
				ehi.setDatabaseId("lt:" + database + ":" + ehi.getExperimentName());
				ehi.setOriginDbId("lt:" + database + ":" + ehi.getExperimentName());
				ehi.setImportusername(user != null ? user : SystemAnalysis.getUserName());
				ehi.setImportusergroup("Imaging System");
				LTsystem system = LTsystem.getTypeFromDatabaseName(database);
				if (system == LTsystem.Barley) {
					ehi.setExperimenttype(IAPexperimentTypes.BarleyGreenhouse + "");
					ehi.setImportusergroup("Imaging System (BGH)");
				} else
					if (system == LTsystem.Maize) {
						if (name.length() >= 6 && name.toUpperCase().endsWith("RAPS"))
							ehi.setExperimenttype(IAPexperimentTypes.Raps + "");
						else
							if (name.length() >= 6 && name.substring(4, 6).equals("RM"))
								ehi.setExperimenttype(IAPexperimentTypes.BarleyGreenhouse + "");
							else
								ehi.setExperimenttype(IAPexperimentTypes.MaizeGreenhouse + "");
						ehi.setImportusergroup("Imaging System (CGH)");
					} else
						if (system == LTsystem.Phytochamber) {
							ehi.setExperimenttype(IAPexperimentTypes.Phytochamber + "");
							ehi.setImportusergroup("Imaging System (APH)");
						} else {
							ehi.setExperimenttype(IAPexperimentTypes.UnknownGreenhouse + "");
							ehi.setImportusergroup("Imaging System (Other)");
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
				LTsystem system = LTsystem.getTypeFromDatabaseName(database);
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
	
	private String getNiceNameFromLoginName(String name) {
		String niceName = SystemOptions.getInstance().getString("Import", "Long User Names//Full name of " + name, name);
		return niceName;
	}
	
	private String getCoordinatorFromExperimentName(String experimentname) {
		try {
			if (experimentname == null || experimentname.isEmpty())
				return null;
			else {
				try {
					int p0 = SystemOptions.getInstance().getInteger("Import", "User Mapping//Experiment User ID Start", 4);
					int p2 = SystemOptions.getInstance().getInteger("Import", "User Mapping//Experiment User ID Length", 2);
					if (experimentname.length() >= p0 + p2) {
						String kuerzel = experimentname.substring(p0, p0 + p2);
						String coor = getCoordinatorFromNameID(kuerzel);
						return coor;
					}
				} catch (Exception err) {
					System.out
							.println(SystemAnalysis.getCurrentTime() + "ERROR: Could not process experiment name and coordinator name. Error: " + err.getMessage());
				}
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	private String getCoordinatorFromNameID(String kuerzel) {
		if (kuerzel == null || !kuerzel.toUpperCase().equals(kuerzel) ||
				!StringUtils.isAlpha(kuerzel))
			return "(coordinator can not be determined, non-standard measurement label)";
		else
			return SystemOptions.getInstance().getString("Import", "User Mapping//Coordinator " + kuerzel, kuerzel);
	}
	
	public static HashSet<String> getAdministrators() {
		HashSet<String> res = new HashSet<String>();
		String[] admins = SystemOptions.getInstance().getStringAll("LT-DB", "Administrator Users",
				new String[] { "klukas", "muecke", "Muecke" });
		if (admins != null)
			for (String a : admins)
				res.add(a);
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
				snapshot.setCamera_config(rs.getString("compname"));
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
				if (lbl.isEmpty())
					System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: NO CAMERA CONFIG FOR IMAGE-CONFIG '" + camLbl + "'!");
				snapshot.setCamera_label(lbl);// rs.getString("compname"));
				snapshot.setCamera_config(camLbl);
				snapshot.setXfactor(0);// rs.getDouble("xfactor"));
				snapshot.setYfactor(0);// rs.getDouble("yfactor"));
				
				String s1 = id2path.get(rs.getLong("image_oid"));
				snapshot.setPath_image(s1);
				String s2 = id2path.get(rs.getLong("null_image_oid"));
				snapshot.setPath_null_image(s2);
				
				result.add(snapshot);
			}
			rs.close();
			ps.close();
		}
		
	}
	
	public static String getIAPcameraNameFromConfigLabel(String conf) {
		String res = "";
		for (String id : SystemOptions.getInstance().getStringAll(
				"Import", "NIR-Camera-Config-Substrings", new String[] { "NIR" }))
			if (!id.isEmpty() && conf.toUpperCase().contains(id.toUpperCase())) {
				res += "nir.";
				break;
			}
		if (res.isEmpty())
			for (String id : SystemOptions.getInstance().getStringAll(
					"Import", "VIS-Camera-Config-Substrings", new String[] { "VIS", "RGB" }))
				if (!id.isEmpty() && conf.toUpperCase().contains(id.toUpperCase())) {
					res += "vis.";
					break;
				}
		if (res.isEmpty())
			for (String id : SystemOptions.getInstance().getStringAll(
					"Import", "FLUO-Camera-Config-Substrings", new String[] { "FLU" }))
				if (!id.isEmpty() && conf.toUpperCase().contains(id.toUpperCase())) {
					res += "fluo.";
					break;
				}
		if (res.isEmpty())
			for (String id : SystemOptions.getInstance().getStringAll(
					"Import", "IR-Camera-Config-Substrings", new String[] { "IR ", "IR_" }))
				if (!id.isEmpty() && conf.toUpperCase().contains(id.toUpperCase())) {
					res += "ir.";
					break;
				}
		boolean topFound = false;
		for (String id : SystemOptions.getInstance().getStringAll(
				"Import", "Top-View-Camera-Config-Substrings", new String[] { "TOP", " TV", "_TV_" }))
			if (!id.isEmpty() && conf.toUpperCase().contains(id)) {
				if (positionFirst) {
					res = "top." + res;
					if (res.endsWith("."))
						res = res.substring(0, res.length() - 1);
				} else {
					res += "top";
				}
				topFound = true;
				break;
			}
		if (!topFound) {
			boolean sideFound = false;
			for (String id : SystemOptions.getInstance().getStringAll(
					"Import", "Side-View-Camera-Config-Substrings", new String[] { "SIDE", " SV" }))
				if (!id.isEmpty() && conf.toUpperCase().contains(id)) {
					sideFound = true;
					if (positionFirst) {
						res = "side." + res;
						if (res.endsWith("."))
							res = res.substring(0, res.length() - 1);
					} else {
						res += "side";
					}
					break;
				}
			if (!sideFound) {
				boolean t = SystemOptions.getInstance().getBoolean(
						"Import", "Use Top-Config, if position can not be determined", true);
				String sideOrTop = t ? "top" : "side";
				if (positionFirst) {
					res = sideOrTop + "." + res;
					if (res.endsWith("."))
						res = res.substring(0, res.length() - 1);
				} else {
					res += sideOrTop;
				}
			}
		}
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
		
		if (isDebug()) {
			DatabaseMetaData meta = connection.getMetaData(); // Metadata
			System.out.println("Connection successful: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
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
		
		HashMap<String, Condition> idtag2condition = experimentReq.getDatabaseId() != null ?
				getPlantIdAnnotation(experimentReq)
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
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: snapshot with empty ID tag is ignored.");
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
			Condition conditionTemplate = idtag2condition.get(idTag);
			if (conditionTemplate == null) {
				if (!printedInvalidIdTags.contains(sn.getId_tag())) {
					System.out.println("No meta-data for ID " + idTag);
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
			
			int day = DateUtil.getElapsedDays(earliest, new Date(sn.getTimestamp().getTime())) + 1;
			
			boolean firstSnapshotInfoForTimePoint = !processedSnapshotTimes.contains(sn.getTimestamp().getTime());
			
			if (firstSnapshotInfoForTimePoint) {
				// if (sn.getWeight_before() > 0) {
				// process weight_before
				
				Substance s = new Substance();
				s.setName("weight_before");
				
				Condition condition = new Condition(s, conditionTemplate.getAttributeMap());
				condition.setExperimentInfo(experimentReq);
				
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
							
							Condition condition = new Condition(s, conditionTemplate.getAttributeMap());
							condition.setExperimentInfo(experimentReq);
							
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
							
							Condition condition = new Condition(s, conditionTemplate.getAttributeMap());
							condition.setExperimentInfo(experimentReq);
							
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
					s.setInfo(sn.getCamera_config());
					
					Condition condition = new Condition(s, conditionTemplate.getAttributeMap());
					condition.setExperimentInfo(experimentReq);
					
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
					
					Double position = null;
					String pre = "Side";
					if (sn.getCamera_label() != null && sn.getCamera_label().contains("top"))
						pre = "Top";
					String a = sn.getUserDefinedCameraLabel();
					try {
						Double defPos = null;
						String b = StringManipulationTools.getNumbersFromString(a);
						if (b.length() == 0)
							defPos = 0d;
						else
							defPos = Double.parseDouble(b);
						position = SystemOptions.getInstance().getDouble("Import", pre + " Rotation//" + a, defPos);
					} catch (Exception e) {
						if (a != null && !a.isEmpty())
							position = SystemOptions.getInstance().getDouble("Import", pre + " Rotation//" + a, 0d);
					}
					if (position == null || position < 0) {
						String fn = sn.getPath_image_config_blob();
						if (fn != null) {
							if (fn.contains("/"))
								fn = fn.substring(fn.lastIndexOf("/") + "/".length());
							IOurl url = LTftpHandler.getImagingSystemFTPurl(experimentReq.getDatabase() + "/"
									+ sn.getPath_image_config_blob(), sn.getId_tag()
									+ (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
							if (optStatus != null)
								optStatus.setCurrentStatusText1("Process snapshots (" + idxx + "/" + snapshots.size() + ") (FTP)");
							position = processConfigBlobToGetRotationAngle(blob2angle, sn, url);
							if (optStatus != null)
								optStatus.setCurrentStatusText1("Process snapshots (" + idxx + "/" + snapshots.size() + ")");
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
						url = LTftpHandler.getImagingSystemFTPurl(experimentReq.getDatabase() + "/"
								+ sn.getPath_image(), sn.getId_tag() + (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
					else {
						url = FileSystemHandler.getURL(new File(sn.getPath_image()));
						url.setFileName(url.getFileName() + "#" + sn.getId_tag() + (position != null ? " (" + digit3(position.intValue()) + ").png" : " (000).png"));
					}
					image.setURL(url);
					
					String fn = sn.getPath_null_image();
					if (fn != null) {
						if (fn.contains("/"))
							fn = fn.substring(fn.lastIndexOf("/") + "/".length());
						url = LTftpHandler.getImagingSystemFTPurl(experimentReq.getDatabase() + "/"
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
			experiment.getHeader().setDatabaseId("lt:" + experimentReq.getDatabase() + ":" + experimentReq.getExperimentName());
			experiment.getHeader().setOriginDbId("lt:" + experimentReq.getDatabase() + ":" + experimentReq.getExperimentName());
		}
		for (SubstanceInterface si : experiment)
			for (ConditionInterface ci : si) {
				String sq = ci.getSequence();
				if (sq != null && StringManipulationTools.containsAny(sq, getMetaNamesSeedDates())) {
					String[] values = sq.split("//");
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
								updateSnapshotTimes(ci, days, "das");
							} catch (Exception err) {
								System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Invalid seed-date definition in plant mapping: " + v);
							}
							break seedDateLookupLoop;
						}
					}
				}
			}
		if (optStatus != null)
			optStatus.setCurrentStatusValue(100);
		
		return experiment;
	}
	
	private HashSet<String> getMetaNamesSeedDates() {
		HashSet<String> res = new HashSet<String>();
		String[] seedDataIDs = SystemOptions.getInstance().getStringAll("Metadata",
				"Seed Date IDs", new String[] { "SEEDDATE", "seed date", "Aussaat", "Sowing", "Seeddate", "SeedDate" });
		if (seedDataIDs != null)
			for (String s : seedDataIDs)
				if (s != null && !s.isEmpty())
					res.add(s);
		return res;
	}
	
	private void updateSnapshotTimes(ExperimentInterface experiment, int add, String newTimeUnit) {
		for (SubstanceInterface si : experiment) {
			for (ConditionInterface ci : si) {
				updateSnapshotTimes(ci, add, newTimeUnit);
			}
		}
	}
	
	private void updateSnapshotTimes(ConditionInterface ci, int add, String newTimeUnit) {
		for (SampleInterface s : ci) {
			int day = s.getTime() - 1;
			s.setTime(day + add);
			s.setTimeUnit(newTimeUnit);
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
					out.close();
				} catch (Exception err) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: NO ROTATION ANGLE FOR URL " + url);
					return Double.NaN;
				}
			}
			TextFile tf = new TextFile(new MyByteArrayInputStream(buf, buf.length), 0);
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
					+ "WHERE measure_label = ? ORDER BY meta_data_name";
			
			PreparedStatement ps = connection.prepareStatement(sqlText);
			
			String expType = header.getExperimentType();
			String species = IAPexperimentTypes.getSpeciesFromExperimentType(expType);
			String initSpecies = species;
			
			ps.setString(1, header.getExperimentName());
			HashSet<String> printedMetaData = new HashSet<String>();
			try {
				ResultSet rs = ps.executeQuery();
				
				ArrayList<String> possibleValues = new ArrayList<String>();
				possibleValues.add("Ignored Column");
				possibleValues.add("Plant ID");
				for (ConditionInfo ci : ConditionInfo.values())
					if (ci != ConditionInfo.IGNORED_FIELD)
						if (ci != ConditionInfo.FILES)
							possibleValues.add(ci + "");
				
				while (rs.next()) {
					String plantID = rs.getString(1);
					String metaName = rs.getString(2);
					String metaValue = rs.getString(3);
					if (metaValue == null)
						continue;
					metaValue = metaValue.trim();
					if (metaValue.isEmpty())
						continue;
					String o = plantID + " / " + metaName + " / " + metaValue;
					if (!printedMetaData.contains(o))
						System.out.println(o);
					printedMetaData.add(o);
					if (!res.containsKey(plantID)) {
						// System.out.println(plantID);
						res.put(plantID, new Condition(null));
						res.get(plantID).setSpecies(species);
					}
					
					String sel = SystemOptions.getInstance().getStringRadioSelection(
							"Metadata",
							"Columns//" + metaName,
							possibleValues, getDefaultSelection(null, metaName, possibleValues), true);
					boolean includeMetaDataType = SystemOptions.getInstance().getBoolean(
							"Metadata", "Columns//" + metaName + " - use as prefix", sel != null && sel.equals("Ignored Column"));
					if (sel != null && !sel.equals("Ignored Column") && !sel.equals("Plant ID")) {
						ConditionInfo ciSel = ConditionInfo.valueOfString(sel);
						if (ciSel != null) {
							if (includeMetaDataType)
								metaValue = metaName + ": " + metaValue;
							String currentVal = res.get(plantID).getField(ciSel);
							if (ciSel == ConditionInfo.SPECIES && (currentVal != null && currentVal.equals(initSpecies))) {
								res.get(plantID).setField(ciSel, null);
								currentVal = null;
							}
							if (currentVal != null && currentVal.equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
								currentVal = null;
							if (currentVal == null || currentVal.trim().isEmpty())
								res.get(plantID).setField(ciSel, metaValue);
							else
								if (!currentVal.contains(metaValue)) {
									String newVal = currentVal.trim() + " // " + metaValue;
									TreeSet<String> elements = new TreeSet<String>();
									for (String s : newVal.split(" // "))
										elements.add(s);
									newVal = StringManipulationTools.getStringListMerge(elements, " // ");
									res.get(plantID).setField(ciSel, newVal);
								}
						}
					}
				}
				rs.close();
				ps.close();
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">" + e.getMessage());
			}
		} finally {
			closeDatabaseConnection(connection);
		}
		return res;
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
		return databaseId.startsWith(LTftpHandler.PREFIX + ":");
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
	
	public static void extendId2ConditionList(HashMap<String, Condition> optIdTag2condition, TableDataHeadingRow heading,
			ArrayList<TableDataStringRow> md, boolean fileImport_useConditionIdForReplicateIdStorage) {
		for (TableDataStringRow tdsr : md) {
			String id = heading.getPlantID(tdsr);
			if (id != null) {
				Condition c = new Condition(null);
				c.setSpecies(heading.getSpecies(tdsr));
				c.setGenotype(heading.getGenotype(tdsr));
				c.setVariety(heading.getVariety(tdsr));
				c.setSequence(heading.getSequence(tdsr));
				c.setTreatment(heading.getTreatment(tdsr));
				c.setGrowthconditions(heading.getGrowthconditions(tdsr));
				if (fileImport_useConditionIdForReplicateIdStorage) {
					try {
						c.setRowId(Integer.parseInt(heading.getReplicateID(tdsr)));
					} catch (Exception e) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: " + heading.getReplicateID(tdsr)
								+ " is no valid whole number numeric replicate ID.");
					}
				}
				optIdTag2condition.put(id, c);
			}
		}
	}
	
	public static ExperimentInterface getExperimentFromSnapshots(ExperimentHeader eh,
			ArrayList<Snapshot> snapshots, HashMap<String, Condition> optIdTag2condition) throws ClassNotFoundException, SQLException {
		return new LTdataExchange().getExperiment(eh, false, null, snapshots, optIdTag2condition);
	}
	
	public static String getDefaultSelection(Integer optCol, String heading, ArrayList<String> possibleValues) {
		if (optCol != null && optCol == 1)
			return possibleValues.get(1);
		for (String p : possibleValues)
			if (p.equalsIgnoreCase(heading))
				return p;
		return "Ignored Column";
	}
	
	private static boolean isDebug() {
		return IAPoptions.getInstance().getBoolean("LT-DB", "PostgreSQL//Print Debug Messages", false);
	}
}
