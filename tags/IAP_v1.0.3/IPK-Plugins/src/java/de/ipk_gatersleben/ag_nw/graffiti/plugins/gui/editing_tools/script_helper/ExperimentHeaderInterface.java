package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.ExperimentHeaderHelper;

import unit_test_support.TestValueRequired;

@TestValueRequired("experimentname")
public interface ExperimentHeaderInterface extends Comparable<ExperimentHeaderInterface>, Cloneable, FileAttachmentSupport {
	
	@Override
	public boolean equals(Object obj);
	
	public void setExperimentname(String experimentname);
	
	public String getExperimentName();
	
	public void setRemark(String remark);
	
	public String getRemark();
	
	public void setSettings(String settings);
	
	public String getSettings();
	
	public void setCoordinator(String coordinator);
	
	public String getCoordinator();
	
	// /**
	// * Use setDatabaseId instead (both methods set the same property).
	// *
	// * @param excelfileid
	// */
	// @Deprecated
	// public void setExcelfileid(String excelfileid);
	
	public void setDatabaseId(String excelfileid);
	
	// /**
	// * Use getDatabaseId instead (both methods return the same value).
	// *
	// * @return
	// */
	// @Deprecated
	// public String getExcelfileid();
	
	public String getDatabaseId();
	
	/**
	 * Warning: The ID is not retained in many situations.
	 */
	public void setExperimentId(int experimentId);
	
	public int getExperimentId();
	
	public void setImportusername(String importusername);
	
	public String getImportusername();
	
	public void setImportusergroup(String importusergroup);
	
	public String getImportusergroup();
	
	public void setImportdate(Date importdate);
	
	public Date getImportdate();
	
	public void setStartdate(Date startdate);
	
	public Date getStartdate();
	
	public void setNumberOfFiles(int imagefiles);
	
	public int getNumberOfFiles();
	
	public void setSizekb(long sizekb);
	
	public long getSizekb();
	
	public void setExperimenttype(String experimenttype);
	
	public String getExperimentType();
	
	public String getSequence();
	
	public void setSequence(String sequence);
	
	public void toString(StringBuilder r, int measurementcount);
	
	public void fillAttributeMap(Map<String, Object> attributeValueMap, int measurementcount);
	
	public String toStringLines();
	
	public boolean inTrash();
	
	public String getDatabase();
	
	public void setDatabase(String database);
	
	@Override
	public int hashCode();
	
	public ExperimentHeaderInterface clone();
	
	public Date getStorageTime();
	
	public Map<String, Object> getAttributeMap();
	
	public void setStorageTime(Date time);
	
	public TreeMap<Long, ExperimentHeaderInterface> getHistory();
	
	public void addHistoryItem(long time, ExperimentHeaderInterface exp);
	
	public void addHistoryItems(TreeMap<Long, ExperimentHeaderInterface> experiments);
	
	public void clearHistory();
	
	/**
	 * Stores information about the source ID of analyzed data.
	 * Makes it possible to later look for analysis results for a given DB id.
	 * 
	 * @param databaseId
	 */
	public void setOriginDbId(String databaseId);
	
	public String getOriginDbId();
	
	/**
	 * Stores information about a list of global outliers, e.g. everything from a certain day,
	 * everything until a certain day, from a certain day. Or all data for a certain replicate.
	 * This information is currently utilized only by selected and few system functions.
	 * 
	 * @param outliers
	 */
	public void setGlobalOutlierInfo(String outliers);
	
	public String getGlobalOutlierInfo();
	
	public String getExperimentname();
	
	void setAttributesFromMap(Map map);
	
	public ExperimentHeaderHelper getExperimentHeaderHelper();
	
	public void setExperimentHeaderHelper(ExperimentHeaderHelper ehh);
}