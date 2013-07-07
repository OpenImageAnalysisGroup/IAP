package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

public enum FisherOperationMode {
	selectSignificantNodes1s, selectInsignificant1s, pruneTree1s,
	selectSignificantNodes2s, selectInsignificant2s, pruneTree2s;
	
	@Override
	public String toString() {
		switch (this) {
			case selectSignificantNodes1s:
				return "One Sided Fisher: Select nodes with any p<=alpha";
			case selectInsignificant1s:
				return "One Sided Fisher: Select nodes with all p>alpha (insignificant)";
			case pruneTree1s:
				return "One Sided Fisher: Hide lower parts of the hierarchy with insignificant results";
				
			case selectSignificantNodes2s:
				return "Two Sided Fisher: Select nodes with any p<=alpha";
			case selectInsignificant2s:
				return "Two Sided Fisher: Select nodes with all p>alpha (insignificant)";
			case pruneTree2s:
				return "Two Sided Fisher: Hide lower parts of the hierarchy with insignificant results";
				
			default:
				return "internal error, unknown enum constant!";
		}
	}
}
