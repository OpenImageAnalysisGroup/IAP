package de.ipk.ag_ba.commands;

import java.util.Date;

public enum AnalysisStatus {
	CURRENT, NON_CURRENT, NOT_FOUND;
	
	private Date importdate;
	private String databaseId;
	
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
	
	public void setNewestKnownDatapoint(Date importdate, String databaseId) {
		if (this.importdate == null || importdate.compareTo(this.importdate) > 0) {
			this.importdate = importdate;
			this.databaseId = databaseId;
		}
	}
	
	public Date getNewestImportDate() {
		return importdate;
	}
	
	public String getDatabaseIdOfNewestResultData() {
		return databaseId;
	}
}
