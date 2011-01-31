/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import org.BackgroundTaskStatusProvider;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.selection.Selection;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MyNonInteractiveSpringEmb
					implements Runnable, BackgroundTaskStatusProvider {
	
	private Graph graph;
	private Selection selection;
	private int initMaxMove = 30;
	ThreadSafeOptions tso;
	
	/**
	 * @param gi
	 * @param s
	 */
	public MyNonInteractiveSpringEmb(Graph gi, Selection s, ThreadSafeOptions tso) {
		this.graph = gi;
		this.selection = s;
		this.tso = tso;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValue()
	 */
	public int getCurrentStatusValue() {
		return (int) getCurrentStatusValueFine();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusValueFine()
	 */
	public double getCurrentStatusValueFine() {
		if (tso.temperature_max_move > 0 && tso.temperature_max_move <= initMaxMove && tso.runStatus != 3)
			return 100f * (initMaxMove - tso.temperature_max_move) / initMaxMove;
		else
			return 100f;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage1()
	 */
	public String getCurrentStatusMessage1() {
		String nme = graph.getName();
		if (nme == null)
			nme = "[unnamed]";
		if (tso.runStatus == 3)
			return "Layout for " + nme + " complete";
		else
			return "Layout " + nme + " (" + tso.getRunStatus() + ")";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#getCurrentStatusMessage2()
	 */
	public String getCurrentStatusMessage2() {
		return "";
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseStop()
	 */
	public void pleaseStop() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final PatternSpringembedder pse = new PatternSpringembedder();
		// pse.setControlInterface(tso, new JPanel());
		tso.setGraphInstance(graph);
		tso.setSelection(selection);
		pse.executeThreadSafe(tso);
	}
	
	public static ThreadSafeOptions getNewThreadSafeOptionsWithDefaultSettings() {
		ThreadSafeOptions tso = new ThreadSafeOptions();
		tso.borderForce = false;
		tso.doRandomInit = false;
		tso.doFinishMoveToTop = false;
		tso.redraw = false;
		tso.setDval(myOp.DvalIndexSliderZeroLength, 40);
		tso.setBval(myOp.BvalIndexStopWhenIdle, true);
		tso.setBval(myOp.BvalIndexEnableEdgeWeightProcessing, false);
		tso.temperature_max_move = 30;
		tso.temp_alpha = 0.99;
		tso.doFinishRemoveOverlapp = false;
		tso.doCopyPatternLayout = false;
		tso.doRemoveAllBends = false;
		return tso;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pluginWaitsForUser()
	 */
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProvider#pleaseContinueRun()
	 */
	public void pleaseContinueRun() {
		// empty
	}
	
	public void setCurrentStatusValue(int value) {
		// empty
	}
	
}
