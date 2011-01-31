package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Date;
import java.util.Map;

import org.AttributeHelper;

public class ExperimentHeader implements ExperimentHeaderInterface {
	
	/**
	 * If list of state variables is modified/extended, check and modify equals,
	 * hashCode and eventually compareTo method implementation.
	 */
	private String experimentName, remark, coordinator, databaseId, importUserName, importUserGroup;
	private Date importDate;
	private Date startDate;
	private Integer imageFiles = new Integer(0);
	private String experimentType, sequence;
	private long sizekb;
	private int experimentID = -1;
	private String database;
	
	public ExperimentHeader() {
		//
	}
	
	public ExperimentHeader(ExperimentHeaderInterface copyFrom) {
		this();
		if (copyFrom == null)
			return;
		experimentName = copyFrom.getExperimentname();
		remark = copyFrom.getRemark();
		coordinator = copyFrom.getCoordinator();
		databaseId = copyFrom.getDatabaseId();
		importUserName = copyFrom.getImportusername();
		importUserGroup = copyFrom.getImportusergroup();
		importDate = copyFrom.getImportdate();
		startDate = copyFrom.getStartdate();
		sizekb = copyFrom.getSizekb();
		experimentType = copyFrom.getExperimentType();
		sequence = copyFrom.getSequence();
		database = copyFrom.getDatabase();
		imageFiles = copyFrom.getNumberOfFiles();
	}
	
	public ExperimentHeader(ConditionInterface copyFrom) {
		this();
		experimentName = copyFrom.getExperimentName();
		remark = copyFrom.getExperimentRemark();
		coordinator = copyFrom.getCoordinator();
		importDate = copyFrom.getExperimentImportdate();
		startDate = copyFrom.getExperimentStartDate();
		experimentType = copyFrom.getExperimentType();
		sequence = copyFrom.getSequence();
		database = copyFrom.getDatabase();
	}
	
	public void setExperimentname(String experimentname) {
		this.experimentName = experimentname;
	}
	
	public String getExperimentname() {
		return experimentName;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public void setCoordinator(String coordinator) {
		this.coordinator = coordinator;
	}
	
	public String getCoordinator() {
		return coordinator;
	}
	
	public void setImportusername(String importusername) {
		this.importUserName = importusername;
	}
	
	public String getImportusername() {
		return importUserName;
	}
	
	public void setImportusergroup(String importusergroup) {
		this.importUserGroup = importusergroup;
	}
	
	public String getImportusergroup() {
		return importUserGroup;
	}
	
	public void setImportdate(Date importdate) {
		this.importDate = importdate;
	}
	
	public Date getImportdate() {
		return importDate;
	}
	
	public void setStartdate(Date startdate) {
		this.startDate = startdate;
	}
	
	public Date getStartdate() {
		return startDate;
	}
	
	public void setNumberOfFiles(int imagefiles) {
		this.imageFiles = imagefiles;
	}
	
	public int getNumberOfFiles() {
		return imageFiles;
	}
	
	public void setSizekb(long sizekb) {
		this.sizekb = sizekb;
	}
	
	public long getSizekb() {
		return sizekb;
	}
	
	public void setExperimenttype(String experimenttype) {
		this.experimentType = experimenttype;
	}
	
	public String getExperimentType() {
		return experimentType;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, -1);
		return sb.toString();
	}
	
	public void toString(StringBuilder r, int measurementcount) {
		r.append("<experiment experimentid=\"" + experimentID + "\">");
		Substance.getAttributeString(r, new String[] {
				"experimentname", "database", "remark", "coordinator", "experimenttype", "sequence", "excelfileid",
				"importusername", "importusergroup", "importdate", "startdate", "measurements", "imagefiles", "sizekb"
		}, new Object[] {
				experimentName, database, remark, coordinator, experimentType, sequence, databaseId, importUserName,
				importUserGroup, AttributeHelper.getDateString(importDate), AttributeHelper.getDateString(startDate),
				measurementcount, (imageFiles == null ? 0 : imageFiles), sizekb
		}, true);
		r.append("</experiment>");
	}
	
	@SuppressWarnings("unchecked")
	public ExperimentHeader(Map map) {
		this();
		setExperimentname((String) map.get("experimentname"));
		if (getExperimentname() == null)
			setExperimentname(ExperimentInterface.UNSPECIFIED_EXPERIMENTNAME);
		setDatabase((String) map.get("database"));
		setRemark((String) map.get("remark"));
		setExcelfileid(map.get("_id").toString()); // (String)
		// map.get("excelfileid"));
		setCoordinator((String) map.get("coordinator"));
		setExperimenttype((String) map.get("experimenttype"));
		setSequence((String) map.get("sequence"));
		setImportusername((String) map.get("importusername"));
		setImportusergroup((String) map.get("importusergroup"));
		setImportdate((Date) map.get("importdate"));
		setStartdate((Date) map.get("startdate"));
		if (map.get("imagefiles") != null && map.get("imagefiles") instanceof String)
			setNumberOfFiles(Integer.parseInt(((String) map.get("imagefiles"))));
		else
			setNumberOfFiles((Integer) map.get("imagefiles"));
		if (map.get("sizekb") != null && map.get("sizekb") instanceof String)
			setSizekb(Integer.parseInt(((String) map.get("sizekb"))));
		else
			setSizekb(((Long) map.get("sizekb")));
	}
	
	public ExperimentHeader(String experimentname) {
		this();
		setExperimentname(experimentname);
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap, int measurementcount) {
		attributeValueMap.put("experimentname", experimentName);
		if (database != null)
			attributeValueMap.put("database", database);
		attributeValueMap.put("remark", remark);
		attributeValueMap.put("coordinator", getCoordinator());
		attributeValueMap.put("experimenttype", experimentType);
		attributeValueMap.put("sequence", sequence);
		attributeValueMap.put("excelfileid", databaseId);
		attributeValueMap.put("importusername", importUserName);
		attributeValueMap.put("importusergroup", importUserGroup);
		attributeValueMap.put("importdate", importDate);
		attributeValueMap.put("startdate", startDate);
		attributeValueMap.put("measurements", measurementcount);
		attributeValueMap.put("imagefiles", (imageFiles == null ? 0 : imageFiles));
		attributeValueMap.put("sizekb", sizekb);
	}
	
	public int getExperimentId() {
		return experimentID;
	}
	
	public void setExperimentId(int experimentId) {
		this.experimentID = experimentId;
	}
	
	public boolean inTrash() {
		return getExperimentType() != null && getExperimentType().startsWith("Trash");
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}
	
	public String getDatabase() {
		return database;
	}
	
	@Override
	public ExperimentHeader clone() {
		return new ExperimentHeader(this);
	}
	
	@Override
	public int compareTo(ExperimentHeaderInterface o) {
		return getExperimentname().compareTo(o.getExperimentname());
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean oldStyle = false;
		if (oldStyle)
			return getExperimentname().equals(((ExperimentHeaderInterface) obj).getExperimentname());
		else {
			if (obj == null)
				return false;
			if (!(obj instanceof ExperimentHeader))
				return false;
			ExperimentHeader e = (ExperimentHeader) obj;
			String s1 = experimentName + ";" + remark + ";" + coordinator + ";" + databaseId + ";" + importUserName + ";"
								+ importUserGroup + ";" + imageFiles + ";" + sizekb + ";" + experimentType + ";" + sequence + ";"
								+ experimentID + ";" + database + ";" + (importDate != null ? importDate.getTime() : "") + ";"
					+ (startDate != null ? startDate.getTime() : "");
			String s2 = e.experimentName + ";" + e.remark + ";" + e.coordinator + ";" + e.databaseId + ";"
								+ e.importUserName + ";" + e.importUserGroup + ";" + e.imageFiles + ";" + e.sizekb + ";"
								+ e.experimentType + ";" + e.sequence + ";" + e.experimentID + ";" + e.database + ";"
								+ (e.importDate != null ? e.importDate.getTime() : "") + ";" + (e.startDate != null ? e.startDate.getTime() : "");
			return s1.equals(s2);
		}
	}
	
	@Override
	public int hashCode() {
		String s1 = experimentName + ";" + remark + ";" + coordinator + ";" + databaseId + ";" + importUserName + ";"
							+ importUserGroup + ";" + imageFiles + ";" + sizekb + ";" + experimentType + ";" + sequence + ";"
							+ experimentID + ";" + database + ";" + (importDate != null ? importDate.getTime() : "") + ";"
				+ (startDate != null ? startDate.getTime() : "");
		return s1.hashCode();
	}
	
	@Deprecated
	public void setExcelfileid(String excelfileid) {
		setDatabaseId(excelfileid);
	}
	
	@Deprecated
	public String getExcelfileid() {
		return getDatabaseId();
	}
	
	@Override
	public void setDatabaseId(String excelfileid) {
		databaseId = excelfileid;
		
	}
	
	@Override
	public String getDatabaseId() {
		return databaseId;
	}
}
