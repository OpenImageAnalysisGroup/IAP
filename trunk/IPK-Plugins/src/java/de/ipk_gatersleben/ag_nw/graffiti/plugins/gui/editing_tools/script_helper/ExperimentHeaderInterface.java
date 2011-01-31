package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Date;
import java.util.Map;

import unit_test_support.TestValueRequired;

@TestValueRequired("experimentname")
public interface ExperimentHeaderInterface extends Comparable<ExperimentHeaderInterface>, Cloneable {
	
	public boolean equals(Object obj);
	
	public void setExperimentname(String experimentname);
	
	public String getExperimentname();
	
	public void setRemark(String remark);
	
	public String getRemark();
	
	public void setCoordinator(String coordinator);
	
	public String getCoordinator();
	
	/**
	 * Use setDatabaseId instead (both methods set the same property).
	 * 
	 * @param excelfileid
	 */
	@Deprecated
	public void setExcelfileid(String excelfileid);
	
	public void setDatabaseId(String excelfileid);
	
	/**
	 * Use getDatabaseId instead (both methods return the same value).
	 * 
	 * @return
	 */
	@Deprecated
	public String getExcelfileid();
	
	public String getDatabaseId();
	
	/**
	 * This command does not yet work as intended (the ID is not retained in many
	 * situations).
	 */
	@Deprecated
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
	
	public boolean inTrash();
	
	public String getDatabase();
	
	public void setDatabase(String database);
	
	public int hashCode();
	
	public ExperimentHeaderInterface clone();
}