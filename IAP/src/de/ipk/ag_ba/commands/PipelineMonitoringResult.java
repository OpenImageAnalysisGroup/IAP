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
	
	public PipelineMonitoringResult(String plantID, Long snapshotTime) {
		this.plantID = plantID;
		this.snapshotTime = snapshotTime;
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
			results.add(pipelineMonitoringResult);
		}
	}
	
	public String getPlantID() {
		return plantID;
	}
	
	public String getSnapshotTime() {
		return SystemAnalysis.getCurrentTime(snapshotTime);
	}
	
}
