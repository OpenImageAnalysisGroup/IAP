package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.AttributeHelper;
import org.ExperimentHeaderHelper;
import org.StringAnnotationProcessor;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.dc.DCelement;

public class ExperimentHeader implements ExperimentHeaderInterface {
	
	/**
	 * If list of state variables is modified/extended, check and modify equals,
	 * hashCode and eventually compareTo method implementation.
	 */
	private String experimentName, remark, coordinator, databaseId, importUserName, importUserGroup;
	private Date importDate;
	private Date startDate;
	private Date storageTime;
	private Integer imageFiles = new Integer(0);
	private String experimentType, sequence, files;
	private long sizekb;
	private int experimentID = -1;
	private String database, originDatabaseId, globalOutliers, settings, annotation;
	
	public ExperimentHeader() {
		//
	}
	
	public ExperimentHeader(ExperimentHeaderInterface copyFrom) {
		this(copyFrom != null ? copyFrom.getAttributeMap() : null);
	}
	
	public ExperimentHeader(ConditionInterface copyFrom) {
		this();
		setExperimentName(copyFrom.getExperimentName());
		remark = copyFrom.getExperimentRemark();
		coordinator = copyFrom.getCoordinator();
		importDate = copyFrom.getExperimentImportDate();
		startDate = copyFrom.getExperimentStartDate();
		storageTime = copyFrom.getExperimentStorageDate();
		experimentType = copyFrom.getExperimentType();
		sequence = copyFrom.getSequence();
		files = copyFrom.getFiles();
		database = copyFrom.getDatabase();
		originDatabaseId = copyFrom.getExperimentOriginDbId();
		globalOutliers = copyFrom.getExperimentGlobalOutlierInfo();
		settings = copyFrom.getExperimentSettings();
		annotation = copyFrom.getExperimentAnnotation();
	}
	
	@Override
	public void setExperimentname(String experimentname) {
		this.setExperimentName(experimentname);
	}
	
	@Override
	public String getExperimentName() {
		return experimentName;
	}
	
	@Override
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	@Override
	public String getRemark() {
		return remark;
	}
	
	@Override
	public void setSettings(String settings) {
		this.settings = settings;
	}
	
	@Override
	public String getSettings() {
		return settings;
	}
	
	@Override
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	
	@Override
	public String getAnnotation() {
		return annotation;
	}
	
	@Override
	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
	}
	
	@Override
	public String getCoordinator() {
		return coordinator;
	}
	
	@Override
	public void setImportusername(String importusername) {
		this.importUserName = importusername;
	}
	
	@Override
	public String getImportusername() {
		return importUserName;
	}
	
	@Override
	public void setImportusergroup(String importusergroup) {
		this.importUserGroup = importusergroup;
	}
	
	@Override
	public String getImportusergroup() {
		return importUserGroup;
	}
	
	@Override
	public void setImportdate(Date importdate) {
		this.importDate = importdate;
	}
	
	@Override
	public Date getImportdate() {
		return importDate;
	}
	
	@Override
	public void setStartdate(Date startdate) {
		this.startDate = startdate;
	}
	
	@Override
	public Date getStartdate() {
		return startDate;
	}
	
	@Override
	public void setNumberOfFiles(int imagefiles) {
		this.imageFiles = imagefiles;
	}
	
	@Override
	public int getNumberOfFiles() {
		return imageFiles;
	}
	
	@Override
	public void setSizekb(long sizekb) {
		this.sizekb = sizekb;
	}
	
	@Override
	public long getSizekb() {
		return sizekb;
	}
	
	@Override
	public void setExperimenttype(String experimenttype) {
		this.experimentType = experimenttype;
	}
	
	@Override
	public String getExperimentType() {
		return experimentType;
	}
	
	@Override
	public String getSequence() {
		return sequence;
	}
	
	@Override
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public String getFiles() {
		return files;
	}
	
	@Override
	public void setFiles(String files) {
		this.files = files;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, -1);
		return sb.toString();
	}
	
	@Override
	public void toString(StringBuilder r, int measurementcount) {
		r.append("<experiment experimentid=\"" + experimentID + "\">");
		Substance.getAttributeString(r, new String[] {
				"experimentname", "database", "remark", "coordinator", "experimenttype", "sequence", "excelfileid",
				"importusername", "importusergroup", "importdate", "startdate", "storagetime", "measurements", "imagefiles", "sizekb",
				"origin", "outlier", "files", "settings", "annotation"
		}, new Object[] {
				getExperimentName(), database, remark, coordinator, experimentType, sequence, databaseId, importUserName,
				importUserGroup, AttributeHelper.getDateString(importDate), AttributeHelper.getDateString(startDate), AttributeHelper.getDateString(storageTime),
				measurementcount, (imageFiles == null ? 0 : imageFiles), sizekb,
				originDatabaseId, globalOutliers, files, settings, annotation
		}, true);
		r.append("</experiment>");
	}
	
	@SuppressWarnings("unchecked")
	public ExperimentHeader(Map map) {
		this();
		if (map == null)
			return;
		setAttributesFromMap(map);
	}
	
	@Override
	public void setAttributesFromMap(Map map) {
		ArrayList<Object> del = new ArrayList<Object>();
		for (Object key : map.keySet()) {
			Object val = map.get(key);
			if (val != null && val instanceof String && val.equals("null"))
				del.add(key);
		}
		for (Object d : del)
			map.remove(d);
		setExperimentname((String) map.get("experimentname"));
		if (getExperimentName() == null)
			setExperimentname(ExperimentInterface.UNSPECIFIED_EXPERIMENTNAME);
		setDatabase((String) map.get("database"));
		setRemark((String) map.get("remark"));
		setSettings((String) map.get("settings"));
		{ // map individual annotation fields to single combined annotation field
			StringAnnotationProcessor anno = new StringAnnotationProcessor("");
			for (DCelement dc : DCelement.values()) {
				if (dc.isNativeField())
					continue;
				String dcValue = (String) map.get(DCelement.getTermPrefix() + dc.getTermName());
				if (dcValue != null && !dcValue.trim().isEmpty())
					anno.addAnnotationField(dc.getTermName(), dcValue);
			}
			if (!anno.getValue().isEmpty())
				setAnnotation(anno.getValue());
			else
				setAnnotation(null);
		}
		if (map.containsKey("_id"))
			setDatabaseId(map.get("_id") + "");
		else
			if (map.containsKey("excelfileid"))
				setDatabaseId(map.get("excelfileid") + "");
		setCoordinator((String) map.get("coordinator"));
		setExperimenttype((String) map.get("experimenttype"));
		setSequence((String) map.get("sequence"));
		setFiles((String) map.get("files"));
		setImportusername((String) map.get("importusername"));
		setImportusergroup((String) map.get("importusergroup"));
		if (map.get("importdate") != null && map.get("importdate") instanceof String) {
			Date aDate = getDate((String) map.get("importdate"));
			if (aDate != null)
				setImportdate(aDate);
		} else
			setImportdate((Date) map.get("importdate"));
		if (map.get("startdate") != null && map.get("startdate") instanceof String) {
			Date aDate = getDate((String) map.get("startdate"));
			if (aDate != null)
				setStartdate(aDate);
		} else
			setStartdate((Date) map.get("startdate"));
		if (map.get("storagetime") != null && map.get("storagetime") instanceof String) {
			if (!((String) map.get("storagetime")).equals("null")) {
				Date aDate = getDate((String) map.get("storagetime"));
				if (aDate != null)
					setStorageTime(aDate);
			}
		} else
			setStorageTime((Date) map.get("storagetime"));
		if (map.get("imagefiles") != null && map.get("imagefiles") instanceof String)
			setNumberOfFiles(Integer.parseInt(((String) map.get("imagefiles"))));
		else
			setNumberOfFiles(map.get("imagefiles") != null ? (Integer) map.get("imagefiles") : 0);
		if (map.get("sizekb") != null && map.get("sizekb") instanceof String)
			setSizekb(Integer.parseInt(((String) map.get("sizekb"))));
		else
			setSizekb(map.get("sizekb") != null ? ((Long) map.get("sizekb")) : 0);
		if (map.get("origin") != null && map.get("origin") instanceof String)
			setOriginDbId((String) map.get("origin"));
		if (map.get("outliers") != null && map.get("outliers") instanceof String)
			setGlobalOutlierInfo((String) map.get("outliers"));
	}
	
	@SuppressWarnings("deprecation")
	private Date getDate(String dateString) {
		String[] knownDateFormats = new String[] {
				"E MMM d HH:mm:ss z yyyy",
				"dd.MM.yy HH:mm",
				"MM/dd/yy h:mm a" // 5/4/11 8:42 AM
		};
		for (String dateFormat : knownDateFormats) {
			try {
				DateFormat format = new SimpleDateFormat(dateFormat, new Locale("en"));
				Date aDate = format.parse(dateString);
				return aDate;
			} catch (Exception e) {
				// empty
			}
		}
		try {
			Date aDate = new Date(dateString);
			return aDate;
		} catch (Exception e) {
			// empty
		}
		System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Date string can't be interpreted: " + dateString);
		return null;
	}
	
	public ExperimentHeader(String experimentname) {
		this();
		setExperimentname(experimentname);
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		fillAttributeMap(attributeValueMap, -1);
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap, int measurementcount) {
		attributeValueMap.put("experimentname", getExperimentName());
		if (database != null)
			attributeValueMap.put("database", database);
		attributeValueMap.put("remark", remark);
		attributeValueMap.put("coordinator", getCoordinator());
		attributeValueMap.put("experimenttype", experimentType);
		attributeValueMap.put("sequence", sequence);
		attributeValueMap.put("files", files);
		attributeValueMap.put("excelfileid", databaseId);
		attributeValueMap.put("importusername", importUserName);
		attributeValueMap.put("importusergroup", importUserGroup);
		attributeValueMap.put("importdate", importDate);
		attributeValueMap.put("startdate", startDate);
		attributeValueMap.put("storagetime", storageTime);
		attributeValueMap.put("measurements", measurementcount);
		attributeValueMap.put("imagefiles", (imageFiles == null ? 0 : imageFiles));
		attributeValueMap.put("sizekb", sizekb);
		attributeValueMap.put("origin", originDatabaseId);
		attributeValueMap.put("outliers", globalOutliers);
		attributeValueMap.put("settings", settings);
		
		{ // map single combined annotation field to individual annotation fields
			StringAnnotationProcessor anno = new StringAnnotationProcessor(annotation);
			for (DCelement dc : DCelement.values()) {
				if (dc.isNativeField())
					continue;
				String dcValue = anno.getAnnotationField(dc.getTermName());
				if (dcValue != null && !dcValue.trim().isEmpty())
					attributeValueMap.put(DCelement.getTermPrefix() + dc.getTermName(), dcValue);
			}
		}
	}
	
	@Override
	public Object getAttributeField(String id) {
		switch (id) {
			case "experimentname":
				return getExperimentName();
			case "database":
				return database;
			case "remark":
				return remark;
			case "coordinator":
				return coordinator;
			case "experimenttype":
				return experimentType;
			case "sequence":
				return sequence;
			case "files":
				return files;
			case "excelfileid":
				return databaseId;
			case "importusername":
				return importUserName;
			case "importusergroup":
				return importUserGroup;
			case "importdate":
				return importDate;
			case "startdate":
				return startDate;
			case "storagetime":
				return storageTime;
			case "measurements":
				return null;
			case "imagefiles":
				return (imageFiles == null ? 0 : imageFiles);
			case "sizekb":
				return sizekb;
			case "origin":
				return originDatabaseId;
			case "outliers":
				return globalOutliers;
			case "settings":
				return settings;
		}
		throw new UnsupportedOperationException("Can't return field value from id '" + id + "'!");
	}
	
	public static HashMap<String, String> getNiceHTMLfieldNameMapping() {
		HashMap<String, String> res = new HashMap<String, String>();
		
		res.put("experimentname", "<!-- AA -->Name of Experiment");
		res.put("experimenttype", "<!-- AB -->Type of Experiment");
		res.put("startdate", "<!-- C BR -->Experiment Start");
		
		res.put("database", "<!-- D-->Database");
		res.put("origin", "<!-- D-->Origin");
		res.put("importdate", "<!-- E -->Import Date");
		res.put("storagetime", "<!-- F -->Storage Time");
		res.put("excelfileid", "<!-- G BR -->Experiment ID");
		
		res.put("importusername", "<!-- H -->Owner");
		res.put("coordinator", "<!-- I -->Coordinator");
		res.put("importusergroup", "<!-- J BR -->Data Visibility");
		
		res.put("remark", "<!-- K -->Remark");
		res.put("sequence", "<!-- L BR -->Sequence");
		
		res.put("measurements", "<!-- M -->Numeric Measurements");
		res.put("imagefiles", "<!-- N BR -->Binary Files");
		res.put("outliers", "<!-- O -->Outliers");
		res.put("sizekb", "<!-- P -->Storage Requirements (KB)");
		
		{ // add nice names for individual annotation field names
			for (DCelement dc : DCelement.values()) {
				if (dc.isNativeField())
					continue;
				String key = DCelement.getTermPrefix() + dc.getTermName();
				res.put(key, "<!-- Q:" + key + " -->" + dc.getLabel());
			}
		}
		
		return res;
	}
	
	@Override
	public String toStringLines() {
		StringBuilder sb = new StringBuilder();
		LinkedHashMap<String, Object> val = new LinkedHashMap<String, Object>();
		fillAttributeMap(val, -1);
		
		for (String key : val.keySet()) {
			if (val.get(key) != null)
				sb.append(key + ": " + val.get(key) + "<br>");
		}
		
		return sb.toString();
	}
	
	@Override
	public int getExperimentId() {
		return experimentID;
	}
	
	@Override
	public void setExperimentId(int experimentId) {
		this.experimentID = experimentId;
	}
	
	@Override
	public boolean inTrash() {
		return getExperimentType() != null && getExperimentType().startsWith("Trash");
	}
	
	@Override
	public void setDatabase(String database) {
		this.database = database;
	}
	
	@Override
	public String getDatabase() {
		return database;
	}
	
	@Override
	public ExperimentHeader clone() {
		ExperimentHeader r = new ExperimentHeader(this);
		r.setDatabaseId(getDatabaseId());
		return r;
	}
	
	@Override
	public int compareTo(ExperimentHeaderInterface o) {
		return (getExperimentName() + "").compareTo("" + o.getExperimentName());
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean oldStyle = false;
		if (oldStyle)
			return getExperimentName().equals(((ExperimentHeaderInterface) obj).getExperimentName());
		else {
			if (obj == null)
				return false;
			if (!(obj instanceof ExperimentHeader))
				return false;
			ExperimentHeader e = (ExperimentHeader) obj;
			String s1 = getExperimentName() + ";" + remark + ";" + coordinator + ";" + databaseId + ";" + importUserName + ";"
					+ importUserGroup + ";" + imageFiles + ";" + sizekb + ";" + experimentType + ";" + sequence + ";"
					+ experimentID + ";" + database + ";" + (importDate != null ? importDate.getTime() : "") + ";"
					+ (startDate != null ? startDate.getTime() : "")
					+ ";" + originDatabaseId + ";" + globalOutliers + ";" + files + ";" + annotation;
			String s2 = e.getExperimentName() + ";" + e.remark + ";" + e.coordinator + ";" + e.databaseId + ";"
					+ e.importUserName + ";" + e.importUserGroup + ";" + e.imageFiles + ";" + e.sizekb + ";"
					+ e.experimentType + ";" + e.sequence + ";" + e.experimentID + ";" + e.database + ";"
					+ (e.importDate != null ? e.importDate.getTime() : "") + ";" + (e.startDate != null ? e.startDate.getTime() : "")
					+ ";" + originDatabaseId + ";" + globalOutliers + ";" + e.files + ";" + e.annotation;
			return s1.equals(s2);
		}
	}
	
	@Override
	public int hashCode() {
		// String s1 = experimentName + ";" + remark + ";" + coordinator + ";" + databaseId + ";" + importUserName + ";"
		// + importUserGroup + ";" + imageFiles + ";" + sizekb + ";" + experimentType + ";" + sequence + ";"
		// + experimentID + ";" + database + ";" + (importDate != null ? importDate.getTime() : "") + ";"
		// + (startDate != null ? startDate.getTime() : "");
		// return s1.hashCode();
		return super.hashCode();
	}
	
	@Override
	public void setDatabaseId(String excelfileid) {
		databaseId = excelfileid;
		
	}
	
	@Override
	public String getDatabaseId() {
		return databaseId;
	}
	
	@Override
	public Date getStorageTime() {
		return storageTime;
	}
	
	@Override
	public Map<String, Object> getAttributeMap() {
		Map<String, Object> res = new HashMap<String, Object>();
		fillAttributeMap(res, -1);
		return res;
	}
	
	@Override
	public void setStorageTime(Date time) {
		storageTime = time;
	}
	
	TreeMap<Long, ExperimentHeaderInterface> history = new TreeMap<Long, ExperimentHeaderInterface>();
	private ExperimentHeaderHelper ehh;
	
	@Override
	public TreeMap<Long, ExperimentHeaderInterface> getHistory() {
		return history;
	}
	
	@Override
	public void addHistoryItem(long time, ExperimentHeaderInterface exp) {
		if (exp != this)
			history.put(time, exp);
	}
	
	@Override
	public void addHistoryItems(TreeMap<Long, ExperimentHeaderInterface> experiments) {
		for (Entry<Long, ExperimentHeaderInterface> e : experiments.entrySet()) {
			if (e.getValue() != this) {
				history.put(e.getKey(), e.getValue());
			}
		}
	}
	
	@Override
	public void clearHistory() {
		history.clear();
	}
	
	@Override
	public void setOriginDbId(String databaseId) {
		this.originDatabaseId = databaseId;
	}
	
	@Override
	public String getOriginDbId() {
		return originDatabaseId;
	}
	
	private void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}
	
	@Override
	public void setGlobalOutlierInfo(String outliers) {
		this.globalOutliers = outliers;
	}
	
	@Override
	public String getGlobalOutlierInfo() {
		return globalOutliers;
	}
	
	@Override
	@Deprecated
	public String getExperimentname() {
		return getExperimentName();
	}
	
	@Override
	public void setExperimentHeaderHelper(ExperimentHeaderHelper ehh) {
		this.ehh = ehh;
	}
	
	@Override
	public ExperimentHeaderHelper getExperimentHeaderHelper() {
		return ehh;
	}
	
	@Override
	public String toHtmlString() {
		StringBuilder s = new StringBuilder();
		s.append("<html><table border='1'><th>Property</th><th>Value</th></tr>");
		HashMap<String, String> field2niceName = ExperimentHeader.getNiceHTMLfieldNameMapping();
		TreeSet<String> resultRows = new TreeSet<String>();
		Map<String, Object> am = getAttributeMap();
		for (String id : am.keySet()) {
			String idC = id;
			if (field2niceName.containsKey(idC))
				idC = field2niceName.get(idC);
			String v = "" + am.get(id);
			if (v != null && !v.trim().isEmpty() && !v.equalsIgnoreCase("NULL"))
				if (id.equals("settings"))
					resultRows.add("<tr><td>" + idC + "</td><td>"
							+ (v != null && !v.equals("null") && !v.isEmpty() ? "(defined)" : "(not defined)")
							+ "</td></tr>");
				else {
					if (v.contains("|")) {
						for (String vv : v.split("\\|"))
							resultRows.add("<tr><td>" + idC + "</td><td>" + StringManipulationTools.trimString(vv, 40) + "</td></tr>");
					} else
						resultRows.add("<tr><td>" + idC + "</td><td>" + StringManipulationTools.trimString(v, 40) + "</td></tr>");
				}
		}
		for (String r : resultRows)
			s.append(r);
		s.append("</table></html>");
		return s.toString();
	}
	
	@Override
	public boolean hasAnalysSettings() {
		return settings != null && !settings.isEmpty();
	}
}
