/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis;

import java.util.ArrayList;

/**
 * @author klukas
 *         Image Analysis Module
 */
public class IAmodule {
	
	private ArrayList<AbstractImageAnalysisTask> todo = new ArrayList<AbstractImageAnalysisTask>();
	
	public WorkerInfo getWorkerInfo() {
		return new WorkerInfo(todo.size(), 0, 0, 0, "Images / Min");
	}
	
}
