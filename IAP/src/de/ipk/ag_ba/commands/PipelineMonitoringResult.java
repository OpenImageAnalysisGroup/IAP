package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PipelineMonitoringResult {
	private final Collection<BlockMonitoringResult> results = new ArrayList<BlockMonitoringResult>();
	private final String plantID;
	private final Long snapshotTime;
	private final long creationTime;
	
	public PipelineMonitoringResult(String plantID, Long snapshotTime) {
		this.plantID = plantID;
		this.snapshotTime = snapshotTime;
		this.creationTime = System.currentTimeMillis();
	}
	
	public boolean isEmpty() {
		synchronized (results) {
			return results.isEmpty();
		}
	}
	
	public Collection<BlockMonitoringResult> getBlockResults() {
		synchronized (results) {
			return new ArrayList<BlockMonitoringResult>(results);
		}
	}
	
	public void addBlockResult(BlockMonitoringResult pipelineMonitoringResult) {
		synchronized (results) {
			String n = pipelineMonitoringResult.getBlockName();
			boolean updated = false;
			for (BlockMonitoringResult bmr : results) {
				if (bmr.getBlockName().equals(n)) {
					updated = true;
					bmr.setImages(bmr.getImages());
					bmr.setMasks(bmr.getMasks());
				}
			}
			if (!updated)
				results.add(pipelineMonitoringResult);
		}
	}
	
	public String getPlantID() {
		return plantID;
	}
	
	public String getSnapshotTime() {
		return snapshotTime != null ? SystemAnalysis.getCurrentTime(snapshotTime) : "[snapshot time unknown]";
	}
	
}
