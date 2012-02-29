/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.hamming_distance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.graffiti.graph.Graph;

public class WorkTask {
	
	public HashSet<String> nodesIn1;
	public String message2;
	public int j;
	public int i;
	public File graph2fn;
	public int[][] hammingDistances;
	public Graph graph1;
	public WorkSettings ws;
	public ArrayList<WorkTask> runningThreads;
	
	public WorkTask(HashSet<String> nodesIn1, File graph2fn, int i, int j, String message2, int[][] hammingDistances, Graph graph1, WorkSettings ws,
						ArrayList<WorkTask> runningThreads) {
		this.nodesIn1 = nodesIn1;
		this.graph2fn = graph2fn;
		this.i = i;
		this.j = j;
		this.message2 = message2;
		this.hammingDistances = hammingDistances;
		this.graph1 = graph1;
		this.ws = ws;
		this.runningThreads = runningThreads;
	}
	
}
