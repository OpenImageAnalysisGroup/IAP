package de.ipk.ag_ba.commands;

public enum AnalysisStatus {
	CURRENT, NON_CURRENT, NOT_FOUND;
	
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
	
}
