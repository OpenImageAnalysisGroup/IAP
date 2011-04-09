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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
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
	
	public Collection<ExperimentHeaderInterface> getExperimentsInDatabase(String user, String database)
			throws SQLException, ClassNotFoundException {
		
		String sqlText = "SELECT distinct(measurement_label) FROM snapshot ORDER BY measurement_label";
		
		Connection connection = openConnectionToDatabase(database);
		PreparedStatement ps = connection.prepareStatement(sqlText);
		
		ResultSet rs = ps.executeQuery();
		
		// Collection<String> result = new TreeSet<String>();
		
		Collection<ExperimentHeaderInterface> result = new ArrayList<ExperimentHeaderInterface>();
		while (rs.next()) {
			ExperimentHeaderInterface ehi = new ExperimentHeader();
			ehi.setExperimentname(rs.getString(1));
			ehi.setDatabase(database);
			ehi.setDatabaseId("lemnatec:" + database + ":" + ehi.getExperimentname());
			ehi.setImportusername(user != null ? user : SystemAnalysis.getUserName());
			ehi.setImportusergroup("LemnaTec");
			LemnaTecSystem system = LemnaTecSystem.getTypeFromDatabaseName(database);
			if (system == LemnaTecSystem.Barley) {
				ehi.setExperimenttype("Barley Greenhouse Experiment");
				ehi.setImportusergroup("LemnaTec (BGH)");
			} else
				if (system == LemnaTecSystem.Maize) {
					ehi.setExperimenttype("Maize Greenhouse Experiment");
					ehi.setImportusergroup("LemnaTec (CGH)");
				} else
					if (system == LemnaTecSystem.Phytochamber) {
						ehi.setExperimenttype("Phytochamber Experiment");
						ehi.setImportusergroup("LemnaTec (APH)");
					} else {
						ehi.setExperimenttype("Phenotyping Experiment (unknown greenhouse)");
						ehi.setImportusergroup("LemnaTec");
					}
			ehi.setSequence("");
			ehi.setSizekb(-1);
			result.add(ehi);
		}
		
		rs.close();
		ps.close();
		
		sqlText = "" +
				"SELECT min(time_stamp), max(time_stamp), count(*) " +
				"FROM snapshot,tiled_image " +
				"WHERE measurement_label=? AND tiled_image.snapshot_id=snapshot.id";
		ps = connection.prepareStatement(sqlText);
		for (ExperimentHeaderInterface ehi : result) {
			ps.setString(1, ehi.getExperimentname());
			rs = ps.executeQuery();
			while (rs.next()) {
				Timestamp min = rs.getTimestamp(1);
				Timestamp max = rs.getTimestamp(2);
				if (min == null || max == null)
					System.out.println("Warning: No snapshot times stored for experiment " + ehi.getExperimentname()
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
				if (!people.containsKey(ehi))
					people.put(ehi, new HashSet<String>());
				ps.setString(1, ehi.getExperimentname());
				rs = ps.executeQuery();
				names.clear();
				while (rs.next()) {
					names.add(rs.getString(1));
					people.get(ehi).add(rs.getString(1));
				}
				rs.close();
				if (names.size() > 1) {
					names.remove("muecke");
				}
				ehi.setCoordinator(StringManipulationTools.getStringList(names, ", "));
			}
		} catch (Exception e) {
			System.out.println("Info: Database " + database + " has no import_data table (" + e.getMessage() + ")");
			for (ExperimentHeaderInterface ehi : result) {
				ehi.setCoordinator(null);
			}
		}
		
		sqlText = "SELECT distinct(creator) FROM snapshot WHERE measurement_label=?";
		ps = connection.prepareStatement(sqlText);
		for (ExperimentHeaderInterface ehi : result) {
			if (!people.containsKey(ehi))
				people.put(ehi, new HashSet<String>());
			ps.setString(1, ehi.getExperimentname());
			rs = ps.executeQuery();
			names.clear();
			while (rs.next()) {
				names.add(rs.getString(1));
				people.get(ehi).add(rs.getString(1));
			}
			rs.close();
			String importers = StringManipulationTools.getStringList(names, ", ");
			ehi.setRemark("Snapshot creator(s): " + importers);
			if (ehi.getCoordinator() == null)
				ehi.setCoordinator(importers);
		}
		
		if (user != null && !getAdministrators().contains(user)) {
			// remove experiments from result which should not be visible to users
			LemnaTecSystem system = LemnaTecSystem.getTypeFromDatabaseName(database);
			if (system != LemnaTecSystem.Unknown)
				for (ExperimentHeaderInterface ehi : people.keySet()) {
					if (!people.get(ehi).contains(user)) {
						result.remove(ehi);
					}
				}
		}
		ps.close();
		closeDatabaseConnection(connection);
		
		return result;
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
		Collection<Snapshot> result = new ArrayList<Snapshot>();
		Connection connection = openConnectionToDatabase(database);
		
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
		{
			// load snapshots with images
			String sqlText = "SELECT "
							+ "	creator, measurement_label, camera_label, id_tag, path, "
							+ "	time_stamp, water_amount, weight_after, weight_before, compname, xfactor, yfactor, "
							+ "	image_parameter_oid, image_oid, null_image_oid, snapshot.id as snapshotID, "
							+ "	image_file_table.id as image_file_tableID "
							+ "FROM "
							+ "	snapshot, tiled_image, tile, image_file_table, image_unit_configuration "
							+ "WHERE "
							+ "	snapshot.measurement_label = ? and "
							+ "	snapshot.id = tiled_image.snapshot_id and "
							+ "	tiled_image.id = tile.tiled_image_id and "
							+ "	tile.image_oid = image_file_table.id and "
							+ "	snapshot.configuration_id = image_unit_configuration.compconfigid and "
							+ "	tiled_image.camera_label = image_unit_configuration.gid";
			
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
				snapshot.setWeight_after(rs.getDouble("weight_after"));
				snapshot.setWeight_before(rs.getDouble("weight_before"));
				
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
	
	private static final HashMap<String, byte[]> blob2buf = new HashMap<String, byte[]>();
	
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
							.getExperimentname());
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
		for (Snapshot sn : snapshots) {
			if (sn.getId_tag().length() <= 0) {
				System.out.println("Warning: snapshot with empty ID tag is ignored.");
				continue;
			}
			
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine((double) idxx * 100 / workload);
			
			idxx++;
			
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
			
			{
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
				
				NumericMeasurement weightBefore = new NumericMeasurement(sample);
				weightBefore.setReplicateID(replicateID);
				weightBefore.setUnit("g");
				weightBefore.setValue(sn.getWeight_before());
				weightBefore.setQualityAnnotation(idTag);
				
				measurements.add(weightBefore);
				// }
			}
			{
				// if (sn.getWeight_after() > sn.getWeight_before()) {
				// process water_weight
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
				weightBefore.setValue(sn.getWeight_after() - sn.getWeight_before());
				weightBefore.setQualityAnnotation(idTag);
				
				measurements.add(weightBefore);
				// }
			}
			{
				// if (sn.getWater_amount() > 0) {
				// process water_amount
				String iidd = day + "/" + replicateID;
				
				if (knownDayAndReplicateIDs.containsKey(iidd)) {
					NumericMeasurement water = knownDayAndReplicateIDs.get(iidd);
					water.setValue(water.getValue() + sn.getWater_amount());
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
					water.setUnit("ml");
					water.setValue(sn.getWater_amount());
					water.setQualityAnnotation(idTag);
					
					measurements.add(water);
					
					knownDayAndReplicateIDs.put(iidd, water);
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
					if (fn.contains("/"))
						fn = fn.substring(fn.lastIndexOf("/") + "/".length());
					
					Double position = null;
					
					fn = sn.getPath_image_config_blob();
					if (fn != null) {
						if (fn.contains("/"))
							fn = fn.substring(fn.lastIndexOf("/") + "/".length());
						IOurl url = LemnaTecFTPhandler.getLemnaTecFTPurl(host, experimentReq.getDatabase() + "/"
											+ sn.getPath_image_config_blob(), sn.getId_tag()
											+ (position != null ? " (" + position.intValue() + ")" : ""));
						position = processConfigBlobToGetRotationAngle(blob2buf, sn, url);
						if (Math.abs(position) < 0.00001)
							position = null;
					}
					
					if (position != null) {
						image.setPosition(position);
						image.setPositionUnit("degree");
					}
					
					IOurl url = LemnaTecFTPhandler.getLemnaTecFTPurl(host, experimentReq.getDatabase() + "/"
										+ sn.getPath_image(), sn.getId_tag() + (position != null ? " (" + position.intValue() + ")" : ""));
					image.setURL(url);
					fn = sn.getPath_null_image();
					if (fn != null) {
						if (fn.contains("/"))
							fn = fn.substring(fn.lastIndexOf("/") + "/".length());
						url = LemnaTecFTPhandler.getLemnaTecFTPurl(host, experimentReq.getDatabase() + "/"
											+ sn.getPath_null_image(), sn.getId_tag()
											+ (position != null ? " (" + position.intValue() + ")" : ""));
						image.setLabelURL(url);
					}
					
					measurements.add(image);
				}
			}
			
		}
		if (optStatus != null)
			optStatus.setCurrentStatusValue(-1);
		
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Create experiment (" + measurements.size() + " measurements)");
		
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
		
		ExperimentInterface experiment = NumericMeasurement3D.getExperiment(measurements, false);
		
		int numberOfImages = countMeasurementValues(experiment, new MeasurementNodeType[] { MeasurementNodeType.IMAGE });
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Experiment created (" + numberOfImages + " images)");
		experimentReq.setNumberOfFiles(numberOfImages);
		experiment.setHeader(new ExperimentHeader(experimentReq));
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
	
	private double processConfigBlobToGetRotationAngle(HashMap<String, byte[]> blob2buf, Snapshot sn, IOurl url) {
		try {
			byte[] buf;
			if (blob2buf.containsKey(url.toString()))
				buf = blob2buf.get(url.toString());
			else {
				InputStream in = url.getInputStream();
				// read configuration object in order to detect rotation angle
				MyByteArrayOutputStream out = new MyByteArrayOutputStream();
				byte[] temp = new byte[1024];
				int read = in.read(temp);
				while (read > 0) {
					out.write(temp, 0, read);
					read = in.read(temp);
				}
				buf = out.getBuff();
				blob2buf.put(url.toString(), buf);
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
		
		String sqlText = "SELECT id_tag, meta_data_name, meta_data_value, meta_data_type " + "FROM  meta_info_src "
							+ "WHERE measure_label = ?";
		
		PreparedStatement ps = connection.prepareStatement(sqlText);
		ps.setString(1, header.getExperimentname());
		try {
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				
				String plantID = rs.getString(1);
				
				String metaName = rs.getString(2);
				String metaValue = rs.getString(3);
				// System.out.println("plantID: " + plantID + " metaName: " + metaName + " metaValue: " + metaValue);
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
										addSequenceInfo(res.get(plantID), "SeedDate: " + metaValue);
									else
										addSequenceInfo(res.get(plantID), metaName + ": " + metaValue);
				
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
		}
		return res;
	}
	
	private void addSequenceInfo(Condition condition, String value) {
		String current = (condition.getSequence() != null) ? condition.getSequence() : "";
		if (current.length() > 0)
			current += ";";
		current += value;
		condition.setSequence(current);
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
		
		String sqlText = "SELECT role, db_ids, removed " + "FROM  ltuser "
							+ "WHERE name = ? AND passwd = ?";
		
		PreparedStatement ps = connection.prepareStatement(sqlText);
		ps.setString(1, u);
		ps.setString(2, p);
		
		ResultSet rs = ps.executeQuery();
		
		boolean ok = false;
		
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
		
		return ok;
	}
}
