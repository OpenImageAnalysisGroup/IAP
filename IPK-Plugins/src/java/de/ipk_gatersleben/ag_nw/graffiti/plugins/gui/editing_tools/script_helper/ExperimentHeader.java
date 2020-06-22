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
import java.util.function.Consumer;

import org.AttributeHelper;
import org.ExperimentHeaderHelper;
import org.StringAnnotationProcessor;
import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.dc.DCelement;

public class ExperimentHeader implements ExperimentHeaderInterface {
	
	public static final String ATTRIBUTE_KEY_ANNOTATION = "annotation";
	public static final String ATTRIBUTE_KEY_SETTINGS = "settings";
	public static final String ATTRIBUTE_KEY_FILES = "files";
	public static final String ATTRIBUTE_KEY_OUTLIER = "outliers"; // "outliers" 's' or no 's' at the end?
	public static final String ATTRIBUTE_KEY_ORIGIN = "origin";
	public static final String ATTRIBUTE_KEY_SIZEKB = "sizekb";
	public static final String ATTRIBUTE_KEY_IMAGEFILES = "imagefiles";
	public static final String ATTRIBUTE_KEY_MEASUREMENT = "measurements";
	public static final String ATTRIBUTE_KEY_STORAGETIME = "storagetime";
	public static final String ATTRIBUTE_KEY_STARTDATE = "startdate";
	public static final String ATTRIBUTE_KEY_IMPORTDATE = "importdate";
	public static final String ATTRIBUTE_KEY_IMPORTUSERGROUP = "importusergroup";
	public static final String ATTRIBUTE_KEY_IMPORTUSERNAME = "importusername";
	public static final String ATTRIBUTE_KEY_EXCELFILEID = "excelfileid";
	public static final String ATTRIBUTE_KEY_SEQUENCE = "sequence";
	public static final String ATTRIBUTE_KEY_EXPERIMENTTYPE = "experimenttype";
	public static final String ATTRIBUTE_KEY_COORDINATOR = "coordinator";
	public static final String ATTRIBUTE_KEY_REMARK = "remark";
	public static final String ATTRIBUTE_KEY_DATABASE = "database";
	public static final String ATTRIBUTE_KEY_EXPERIMENTNAME = "experimentname";
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
	public void setImportUserName(String importusername) {
		this.importUserName = importusername;
	}
	
	@Override
	public String getImportusername() {
		return importUserName;
	}
	
	@Override
	public void setImportUserGroup(String importusergroup) {
		this.importUserGroup = importusergroup;
	}
	
	@Override
	public String getImportusergroup() {
		return importUserGroup;
	}
	
	@Override
	public void setImportDate(Date importdate) {
		this.importDate = importdate;
	}
	
	@Override
	public Date getImportdate() {
		return importDate;
	}
	
	@Override
	public void setStartDate(Date startdate) {
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
	public void setExperimentType(String experimenttype) {
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
				ATTRIBUTE_KEY_EXPERIMENTNAME, ATTRIBUTE_KEY_DATABASE, ATTRIBUTE_KEY_REMARK, ATTRIBUTE_KEY_COORDINATOR,
				ATTRIBUTE_KEY_EXPERIMENTTYPE, ATTRIBUTE_KEY_SEQUENCE, ATTRIBUTE_KEY_EXCELFILEID,
				ATTRIBUTE_KEY_IMPORTUSERNAME, ATTRIBUTE_KEY_IMPORTUSERGROUP, ATTRIBUTE_KEY_IMPORTDATE,
				ATTRIBUTE_KEY_STARTDATE, ATTRIBUTE_KEY_STORAGETIME, ATTRIBUTE_KEY_MEASUREMENT, ATTRIBUTE_KEY_IMAGEFILES,
				ATTRIBUTE_KEY_SIZEKB, ATTRIBUTE_KEY_ORIGIN, ATTRIBUTE_KEY_OUTLIER, ATTRIBUTE_KEY_FILES,
				ATTRIBUTE_KEY_SETTINGS, ATTRIBUTE_KEY_ANNOTATION
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
		setExperimentname((String) map.get(ATTRIBUTE_KEY_EXPERIMENTNAME));
		if (getExperimentName() == null)
			setExperimentname(ExperimentInterface.UNSPECIFIED_EXPERIMENTNAME);
		setDatabase((String) map.get(ATTRIBUTE_KEY_DATABASE));
		setRemark((String) map.get(ATTRIBUTE_KEY_REMARK));
		setSettings((String) map.get(ATTRIBUTE_KEY_SETTINGS));
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
			if (map.containsKey(ATTRIBUTE_KEY_EXCELFILEID))
				setDatabaseId(map.get(ATTRIBUTE_KEY_EXCELFILEID) + "");
		setCoordinator((String) map.get(ATTRIBUTE_KEY_COORDINATOR));
		setExperimentType((String) map.get(ATTRIBUTE_KEY_EXPERIMENTTYPE));
		setSequence((String) map.get(ATTRIBUTE_KEY_SEQUENCE));
		setFiles((String) map.get(ATTRIBUTE_KEY_FILES));
		setImportUserName((String) map.get(ATTRIBUTE_KEY_IMPORTUSERNAME));
		setImportUserGroup((String) map.get(ATTRIBUTE_KEY_IMPORTUSERGROUP));
		if (map.get(ATTRIBUTE_KEY_IMPORTDATE) != null && map.get(ATTRIBUTE_KEY_IMPORTDATE) instanceof String) {
			Date aDate = getDate((String) map.get(ATTRIBUTE_KEY_IMPORTDATE));
			if (aDate != null)
				setImportDate(aDate);
		} else
			setImportDate((Date) map.get(ATTRIBUTE_KEY_IMPORTDATE));
		if (map.get(ATTRIBUTE_KEY_STARTDATE) != null && map.get(ATTRIBUTE_KEY_STARTDATE) instanceof String) {
			Date aDate = getDate((String) map.get(ATTRIBUTE_KEY_STARTDATE));
			if (aDate != null)
				setStartDate(aDate);
		} else
			setStartDate((Date) map.get(ATTRIBUTE_KEY_STARTDATE));
		if (map.get(ATTRIBUTE_KEY_STORAGETIME) != null && map.get(ATTRIBUTE_KEY_STORAGETIME) instanceof String) {
			if (!((String) map.get(ATTRIBUTE_KEY_STORAGETIME)).equals("null")) {
				Date aDate = getDate((String) map.get(ATTRIBUTE_KEY_STORAGETIME));
				if (aDate != null)
					setStorageTime(aDate);
			}
		} else
			setStorageTime((Date) map.get(ATTRIBUTE_KEY_STORAGETIME));
		if (map.get(ATTRIBUTE_KEY_IMAGEFILES) != null && map.get(ATTRIBUTE_KEY_IMAGEFILES) instanceof String)
			setNumberOfFiles(Integer.parseInt(((String) map.get(ATTRIBUTE_KEY_IMAGEFILES))));
		else
			setNumberOfFiles(map.get(ATTRIBUTE_KEY_IMAGEFILES) != null ? (Integer) map.get(ATTRIBUTE_KEY_IMAGEFILES) : 0);
		if (map.get(ATTRIBUTE_KEY_SIZEKB) != null && map.get(ATTRIBUTE_KEY_SIZEKB) instanceof String)
			setSizekb(Long.parseLong(((String) map.get(ATTRIBUTE_KEY_SIZEKB))));
		else
			setSizekb(map.get(ATTRIBUTE_KEY_SIZEKB) != null ? ((Long) map.get(ATTRIBUTE_KEY_SIZEKB)) : 0);
		if (map.get(ATTRIBUTE_KEY_ORIGIN) != null && map.get(ATTRIBUTE_KEY_ORIGIN) instanceof String)
			setOriginDbId((String) map.get(ATTRIBUTE_KEY_ORIGIN));
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
		attributeValueMap.put(ATTRIBUTE_KEY_EXPERIMENTNAME, getExperimentName());
		if (database != null)
			attributeValueMap.put(ATTRIBUTE_KEY_DATABASE, database);
		attributeValueMap.put(ATTRIBUTE_KEY_REMARK, remark);
		attributeValueMap.put(ATTRIBUTE_KEY_COORDINATOR, getCoordinator());
		attributeValueMap.put(ATTRIBUTE_KEY_EXPERIMENTTYPE, experimentType);
		attributeValueMap.put(ATTRIBUTE_KEY_SEQUENCE, sequence);
		attributeValueMap.put(ATTRIBUTE_KEY_FILES, files);
		attributeValueMap.put(ATTRIBUTE_KEY_EXCELFILEID, databaseId);
		attributeValueMap.put(ATTRIBUTE_KEY_IMPORTUSERNAME, importUserName);
		attributeValueMap.put(ATTRIBUTE_KEY_IMPORTUSERGROUP, importUserGroup);
		attributeValueMap.put(ATTRIBUTE_KEY_IMPORTDATE, importDate);
		attributeValueMap.put(ATTRIBUTE_KEY_STARTDATE, startDate);
		attributeValueMap.put(ATTRIBUTE_KEY_STORAGETIME, storageTime);
		attributeValueMap.put(ATTRIBUTE_KEY_MEASUREMENT, measurementcount);
		attributeValueMap.put(ATTRIBUTE_KEY_IMAGEFILES, (imageFiles == null ? 0 : imageFiles));
		attributeValueMap.put(ATTRIBUTE_KEY_SIZEKB, sizekb);
		attributeValueMap.put(ATTRIBUTE_KEY_ORIGIN, originDatabaseId);
		attributeValueMap.put("outliers", globalOutliers);
		attributeValueMap.put(ATTRIBUTE_KEY_SETTINGS, settings);
		
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
			case ATTRIBUTE_KEY_EXPERIMENTNAME:
				return getExperimentName();
			case ATTRIBUTE_KEY_DATABASE:
				return database;
			case ATTRIBUTE_KEY_REMARK:
				return remark;
			case ATTRIBUTE_KEY_COORDINATOR:
				return coordinator;
			case ATTRIBUTE_KEY_EXPERIMENTTYPE:
				return experimentType;
			case ATTRIBUTE_KEY_SEQUENCE:
				return sequence;
			case ATTRIBUTE_KEY_FILES:
				return files;
			case ATTRIBUTE_KEY_EXCELFILEID:
				return databaseId;
			case ATTRIBUTE_KEY_IMPORTUSERNAME:
				return importUserName;
			case ATTRIBUTE_KEY_IMPORTUSERGROUP:
				return importUserGroup;
			case ATTRIBUTE_KEY_IMPORTDATE:
				return importDate;
			case ATTRIBUTE_KEY_STARTDATE:
				return startDate;
			case ATTRIBUTE_KEY_STORAGETIME:
				return storageTime;
			case ATTRIBUTE_KEY_MEASUREMENT:
				return null;
			case ATTRIBUTE_KEY_IMAGEFILES:
				return (imageFiles == null ? 0 : imageFiles);
			case ATTRIBUTE_KEY_SIZEKB:
				return sizekb;
			case ATTRIBUTE_KEY_ORIGIN:
				return originDatabaseId;
			case ATTRIBUTE_KEY_OUTLIER:
				return globalOutliers;
			case ATTRIBUTE_KEY_SETTINGS:
				return settings;
		}
		throw new UnsupportedOperationException("Can't return field value from id '" + id + "'!");
	}
	
	private HashMap<String, Consumer<String>> stringSetters = null;
	
	private synchronized HashMap<String, Consumer<String>> getStringSetterFunctions() {
		if (stringSetters == null) {
			stringSetters = new HashMap<String, Consumer<String>>();
			stringSetters.put(ATTRIBUTE_KEY_EXPERIMENTNAME, this::setExperimentName);
			stringSetters.put(ATTRIBUTE_KEY_DATABASE, this::setDatabase);
			stringSetters.put(ATTRIBUTE_KEY_REMARK, this::setRemark);
			stringSetters.put(ATTRIBUTE_KEY_COORDINATOR, this::setCoordinator);
			stringSetters.put(ATTRIBUTE_KEY_EXPERIMENTTYPE, this::setExperimentType);
			stringSetters.put(ATTRIBUTE_KEY_SEQUENCE, this::setSequence);
			stringSetters.put(ATTRIBUTE_KEY_FILES, this::setFiles);
			stringSetters.put(ATTRIBUTE_KEY_EXCELFILEID, this::setDatabaseId);
			stringSetters.put(ATTRIBUTE_KEY_IMPORTUSERNAME, this::setImportUserName);
			stringSetters.put(ATTRIBUTE_KEY_IMPORTUSERGROUP, this::setImportUserGroup);
			stringSetters.put(ATTRIBUTE_KEY_ORIGIN, this::setOriginDbId);
			stringSetters.put(ATTRIBUTE_KEY_OUTLIER, this::setGlobalOutlierInfo);
			stringSetters.put(ATTRIBUTE_KEY_SETTINGS, this::setSettings);
		}
		return stringSetters;
	}
	
	@Override
	public void setAttributeField(String id, Object value) {
		HashMap<String, Consumer<String>> mm = getStringSetterFunctions();
		if (mm.containsKey(id)) {
			mm.get(id).accept((String) value);
			return;
		}
		switch (id) {
			case ATTRIBUTE_KEY_IMPORTDATE:
				if (value == null)
					setImportDate(null);
				else
					if (value instanceof String)
						setImportDate(getDate((String) value));
					else
						if (value instanceof Date)
							setImportDate((Date) value);
						else
							throw new RuntimeException("Can't set field importdate with given attribute type!");
				return;
			case ATTRIBUTE_KEY_STARTDATE:
				if (value == null)
					setStartDate(null);
				else
					if (value instanceof String)
						setStartDate(getDate((String) value));
					else
						if (value instanceof Date)
							setStartDate((Date) value);
						else
							throw new RuntimeException("Can't set field startdate with given attribute type!");
				return;
			case ATTRIBUTE_KEY_STORAGETIME:
				if (value == null)
					setStorageTime(null);
				else
					if (value instanceof String)
						setStorageTime(getDate((String) value));
					else
						if (value instanceof Date)
							setStorageTime((Date) value);
						else
							throw new RuntimeException("Can't set field storagetime with given attribute type!");
				return;
			case ATTRIBUTE_KEY_MEASUREMENT:
				// empty
				return;
			case ATTRIBUTE_KEY_IMAGEFILES:
				if (value == null)
					imageFiles = null;
				else
					imageFiles = Integer.parseInt((String) value);
				return;
			case ATTRIBUTE_KEY_SIZEKB:
				if (value == null)
					setSizekb(-1l);
				else
					setSizekb(Long.parseLong((String) value));
				return;
			
		}
		throw new UnsupportedOperationException("Can't set field value for id '" + id + "'!");
	}
	
	public static HashMap<String, String> getNiceHTMLfieldNameMapping() {
		HashMap<String, String> res = new HashMap<String, String>();
		
		res.put(ATTRIBUTE_KEY_EXPERIMENTNAME, "<!-- AA -->Name of Experiment");
		res.put(ATTRIBUTE_KEY_EXPERIMENTTYPE, "<!-- AB -->Type of Experiment");
		res.put(ATTRIBUTE_KEY_STARTDATE, "<!-- C BR -->Experiment Start");
		
		res.put(ATTRIBUTE_KEY_DATABASE, "<!-- D-->Database");
		res.put(ATTRIBUTE_KEY_ORIGIN, "<!-- D-->Origin");
		res.put(ATTRIBUTE_KEY_IMPORTDATE, "<!-- E -->Import Date");
		res.put(ATTRIBUTE_KEY_STORAGETIME, "<!-- F -->Storage Time");
		res.put(ATTRIBUTE_KEY_EXCELFILEID, "<!-- G BR -->Experiment ID");
		
		res.put(ATTRIBUTE_KEY_IMPORTUSERNAME, "<!-- H -->Owner");
		res.put(ATTRIBUTE_KEY_COORDINATOR, "<!-- I -->Coordinator");
		res.put(ATTRIBUTE_KEY_IMPORTUSERGROUP, "<!-- J BR -->Data Visibility");
		
		res.put(ATTRIBUTE_KEY_REMARK, "<!-- K -->Remark");
		res.put(ATTRIBUTE_KEY_SEQUENCE, "<!-- L BR -->Sequence");
		
		res.put(ATTRIBUTE_KEY_MEASUREMENT, "<!-- M -->Numeric Measurements");
		res.put(ATTRIBUTE_KEY_IMAGEFILES, "<!-- N BR -->Binary Files");
		res.put(ATTRIBUTE_KEY_OUTLIER, "<!-- O -->Outliers");
		res.put(ATTRIBUTE_KEY_SIZEKB, "<!-- P -->Storage Requirements (KB)");
		
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
		s.append("<html><table border='0'><th>Property</th><th>Value</th></tr>");
		HashMap<String, String> field2niceName = ExperimentHeader.getNiceHTMLfieldNameMapping();
		TreeSet<String> resultRows = new TreeSet<String>();
		Map<String, Object> am = getAttributeMap();
		for (String id : am.keySet()) {
			String idC = id;
			if (field2niceName.containsKey(idC))
				idC = field2niceName.get(idC);
			String v = "" + am.get(id);
			if (v != null && !v.trim().isEmpty() && !v.equalsIgnoreCase("NULL"))
				if (id.equals(ATTRIBUTE_KEY_SIZEKB)) {
					String ss = "n/a";
					if (v != null && !v.isEmpty() && !v.equals("null")) {
						long kb = Long.parseLong(v);
						if (kb > 0) {
							ss = SystemAnalysis.getDataAmountString(kb * 1024);
						}
					}
					resultRows.add("<tr><td>" + idC + "</td><td>"
							+ ss
							+ "</td></tr>");
				} else
					if (id.equals(ATTRIBUTE_KEY_SETTINGS))
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
