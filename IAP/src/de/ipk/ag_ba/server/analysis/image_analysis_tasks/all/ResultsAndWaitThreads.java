package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import java.util.ArrayList;
import java.util.HashMap;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;

/**
 * @author Christian Klukas
 */
public class ResultsAndWaitThreads {
	
	private final HashMap<Integer, BlockResultSet> result;
	private final ArrayList<LocalComputeJob> waitThreads;
	
	public ResultsAndWaitThreads(HashMap<Integer, BlockResultSet> result, ArrayList<LocalComputeJob> waitThreads) {
		this.result = result;
		this.waitThreads = waitThreads;
	}
	
	public HashMap<Integer, BlockResultSet> getResults() {
		return result;
	}
	
	public ArrayList<LocalComputeJob> getWaitThreads() {
		return waitThreads;
	}
	
}
