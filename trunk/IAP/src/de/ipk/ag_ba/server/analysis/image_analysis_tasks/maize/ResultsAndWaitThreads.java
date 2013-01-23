package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import java.util.ArrayList;
import java.util.HashMap;

import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;

/**
 * @author Christian Klukas
 */
public class ResultsAndWaitThreads {
	
	private final HashMap<Integer, BlockResultSet> result;
	private final ArrayList<MyThread> waitThreads;
	
	public ResultsAndWaitThreads(HashMap<Integer, BlockResultSet> result, ArrayList<MyThread> waitThreads) {
		this.result = result;
		this.waitThreads = waitThreads;
	}
	
	public HashMap<Integer, BlockResultSet> getResults() {
		return result;
	}
	
	public ArrayList<MyThread> getWaitThreads() {
		return waitThreads;
	}
	
}
