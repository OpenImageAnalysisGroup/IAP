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

	public Collection<ExperimentHeaderInterface> getExperimentInDatabase(String database) throws SQLException,
						ClassNotFoundException {
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
			ehi.setExcelfileid("lemnatec:" + database + ":" + ehi.getExperimentname());
			ehi.setImportusername("todo import user name");
			ehi.setImportusergroup("Imported");
			ehi.setExperimenttype("Phenotyping"); // todo welches system?
			ehi.setRemark("LemnaTec-DB dataset");
			ehi.setSequence("");
			ehi.setSizekb("?");
			result.add(ehi);
		}

		rs.close();
		ps.close();

		sqlText = "SELECT min(time_stamp), max(time_stamp), count(*) FROM snapshot,tiled_image WHERE measurement_label=? AND tiled_image.snapshot_id=snapshot.id";
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

		ArrayList<String> names = new ArrayList<String>();
		try {
			sqlText = "SELECT distinct(creator) FROM import_data WHERE measurement_label=?";
			ps = connection.prepareStatement(sqlText);
			for (ExperimentHeaderInterface ehi : result) {
				ps.setString(1, ehi.getExperimentname());
				rs = ps.executeQuery();
				names.clear();
				while (rs.next()) {
					names.add(rs.getString(1));
				}
				rs.close();
				ehi.setCoordinator(StringManipulationTools.getStringList(names, ","));
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
			ps.setString(1, ehi.getExperimentname());
			rs = ps.executeQuery();
			names.clear();
			while (rs.next()) {
				names.add(rs.getString(1));
			}
			rs.close();
			String importers = StringManipulationTools.getStringList(names, ",");
			ehi.setImportusername(importers);
			if (ehi.getCoordinator() == null)
				ehi.setCoordinator(importers);
		}

		// ArrayList experimentatorsArray = new ArrayList();
		// for (String s : experimentators)
		// experimentatorsArray.add(s);
		// String experimentator =
		// StringManipulationTools.getStringList(experimentatorsArray, ",");

		ps.close();
		closeDatabaseConnection(connection);

		return result;
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

		String sqlText = "SELECT "
							+ "creator, measurement_label, camera_label, id_tag, path, "
							+ "time_stamp, water_amount, weight_after, weight_before, compname, xfactor, yfactor, "
							+ "image_parameter_oid, image_oid, null_image_oid, snapshot.id as snapshotID, image_file_table.id as image_file_tableID "
							+ "FROM snapshot, tiled_image, tile, image_file_table, image_unit_configuration "
							+ "WHERE snapshot.measurement_label = ? and snapshot.id = tiled_image.snapshot_id and "
							+ "tiled_image.id = tile.tiled_image_id and "
							+ "tile.image_oid = image_file_table.id and "
							+ "snapshot.configuration_id = image_unit_configuration.compconfigid and tiled_image.camera_label = image_unit_configuration.gid";

		ps = connection.prepareStatement(sqlText);
		ps.setString(1, experiment);

		rs = ps.executeQuery();

		while (rs.next()) {
			Snapshot snapshot = new Snapshot();

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

		HashMap<String, byte[]> blob2buf = new HashMap<String, byte[]>();

		if (optStatus != null)
			optStatus.setCurrentStatusText1("Process snapshots (" + snapshots.size() + ")");

		if (optStatus != null)
			optStatus.setCurrentStatusValue(0);
		int workload = snapshots.size();
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
			Integer replicateID = idtag2replicateID.get(sn.getId_tag());
			if (replicateID == null) {
				System.out.println("Warning: internal IAP error. Could not create get replicate ID for ID tag '"
									+ sn.getId_tag() + "'. Snapshot is ignored.");
				continue;
			}

			int day = DateUtil.getElapsedDays(earliest, new Date(sn.getTimestamp().getTime()));

			{
				if (sn.getWeight_before() > 0) {
					// process weight_before
					Substance s = new Substance();
					s.setSubstanceName("weight_before");

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

					NumericMeasurement weightBefore = new NumericMeasurement(sample);
					weightBefore.setReplicateID(replicateID);
					weightBefore.setUnit("g");
					weightBefore.setValue(sn.getWeight_before());

					measurements.add(weightBefore);
				}
			}
			{
				if (sn.getWeight_after() > sn.getWeight_before()) {
					// process water_weight
					Substance s = new Substance();
					s.setSubstanceName("water_weight");

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

					NumericMeasurement weightBefore = new NumericMeasurement(sample);
					weightBefore.setReplicateID(replicateID);
					weightBefore.setUnit("g");
					weightBefore.setValue(sn.getWeight_after() - sn.getWeight_before());

					measurements.add(weightBefore);
				}
			}
			{
				if (sn.getWater_amount() > 0) {
					// process water_amount
					Substance s = new Substance();
					s.setSubstanceName("water_amount");

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

					NumericMeasurement water = new NumericMeasurement(sample);
					water.setReplicateID(replicateID);
					water.setUnit("ml");
					water.setValue(sn.getWater_amount());

					measurements.add(water);
				}
			}
			{
				// process image
				if (sn.getCamera_label() != null) {

					Substance s = new Substance();
					s.setSubstanceName(sn.getCamera_label());

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

					ImageData image = new ImageData(sample);
					image.setPixelsizeX(sn.getXfactor());
					image.setPixelsizeY(sn.getYfactor());
					image.setReplicateID(replicateID);
					image.setUnit("");

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
		ExperimentInterface experiment = NumericMeasurement3D.getExperiment(measurements);

		int numberOfImages = countMeasurementValues(experiment, new MeasurementNodeType[] { MeasurementNodeType.IMAGE });
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Experiment created (" + numberOfImages + " images)");

		experiment.setHeader(new ExperimentHeader(experimentReq));
		if (optStatus != null)
			optStatus.setCurrentStatusValue(100);
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
			TextFile tf = new TextFile(new MyByteArrayInputStream(buf), 0);
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

				if (!res.containsKey(plantID))
					res.put(plantID, new Condition(null));

				if (metaName.equals("Pflanzenart"))
					res.get(plantID).setSpecies(metaValue);
				if (metaName.equals("Pflanzenname"))
					res.get(plantID).setGenotype(metaValue);
				if (metaName.equals("Typ"))
					res.get(plantID).setTreatment(metaValue);
				if (metaName.equals("SeedDate"))
					res.get(plantID).setSequence("SeedDate: " + metaValue);

			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			if (!e.getMessage().endsWith("does not exist"))
				ErrorMsg.addErrorMessage(e);
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
}
