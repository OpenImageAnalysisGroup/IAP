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
		return results.isEmpty();
	}
	
	public Collection<BlockMonitoringResult> getBlockResults() {
		return results;
	}
	
	public void addBlockResult(BlockMonitoringResult pipelineMonitoringResult) {
		results.add(pipelineMonitoringResult);
	}
	
	public String getPlantID() {
		return plantID;
	}
	
	public String getSnapshotTime() {
		return SystemAnalysis.getCurrentTime(snapshotTime);
	}
	
}
