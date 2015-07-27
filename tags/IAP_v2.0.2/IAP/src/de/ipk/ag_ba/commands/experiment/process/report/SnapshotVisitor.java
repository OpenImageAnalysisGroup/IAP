package de.ipk.ag_ba.commands.experiment.process.report;

import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;

/**
 * @author klukas
 */
public interface SnapshotVisitor {
	void visit(SnapshotDataIAP s);
}
