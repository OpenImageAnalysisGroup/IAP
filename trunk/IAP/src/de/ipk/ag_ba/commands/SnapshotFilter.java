package de.ipk.ag_ba.commands;

import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;

public interface SnapshotFilter {
	
	boolean filterOut(SnapshotDataIAP s);
	
}
