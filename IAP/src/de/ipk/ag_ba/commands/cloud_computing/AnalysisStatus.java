package de.ipk.ag_ba.commands.cloud_computing;

import java.util.Date;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public enum AnalysisStatus {
	CURRENT, NON_CURRENT, NOT_FOUND;
	
	private Date importdate;
	private String databaseId;
	private ExperimentHeaderInterface res;
	
	@Override
	public String toString() {
		switch (this) {
			case CURRENT:
				return "current result available";
			case NON_CURRENT:
				return "out-dated result available";
			case NOT_FOUND:
				return "no result available";
		}
		return super.toString();
	}
	
	public void setNewestKnownDatapoint(Date importdate, String databaseId, ExperimentHeaderInterface res) {
		if (this.importdate == null || importdate == null || importdate.compareTo(this.importdate) > 0) {
			this.importdate = importdate;
			this.databaseId = databaseId;
			this.res = res;
		}
	}
	
	public Date getNewestImportDate() {
		return importdate;
	}
	
	public String getDatabaseIdOfNewestResultData() {
		return databaseId;
	}
	
	public ExperimentHeaderInterface getRes() {
		return res;
	}
}
