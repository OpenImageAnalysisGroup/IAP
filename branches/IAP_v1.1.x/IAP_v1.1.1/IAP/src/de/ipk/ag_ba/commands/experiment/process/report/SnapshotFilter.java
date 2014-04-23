package de.ipk.ag_ba.commands.experiment.process.report;

import org.MeasurementFilter;

import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;

public interface SnapshotFilter extends MeasurementFilter {
	boolean filterOut(SnapshotDataIAP s);
}
