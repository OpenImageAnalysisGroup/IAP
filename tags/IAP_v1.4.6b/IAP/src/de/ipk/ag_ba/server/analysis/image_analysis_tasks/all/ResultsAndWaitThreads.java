package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import java.util.ArrayList;
import java.util.HashMap;

import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;

/**
 * @author Christian Klukas
 */
public class ResultsAndWaitThreads {
	
	private final HashMap<String, BlockResultSet> well2result;
	private final ArrayList<LocalComputeJob> waitThreads;
	
	public ResultsAndWaitThreads(HashMap<String, BlockResultSet> well2result, ArrayList<LocalComputeJob> waitThreads) {
		this.well2result = well2result;
		this.waitThreads = waitThreads;
	}
	
	/**
	 * @return well to results
	 */
	public HashMap<String, BlockResultSet> getResults() {
		return well2result;
	}
	
	public ArrayList<LocalComputeJob> getWaitThreads() {
		return waitThreads;
	}
	
}
