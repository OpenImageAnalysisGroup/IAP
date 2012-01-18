/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.analysis;

import org.SystemAnalysis;

/**
 * @author klukas
 */
public class ServerInfo {
	
	public WorkerInfo imageAnalysis, inputOutput;
	public int cpuCount;
	
	public ServerInfo() {
		this.cpuCount = SystemAnalysis.getNumberOfCPUs();
	}
	
	public void getInfoFrom(IOmodule io, IAmodule ia) {
		inputOutput = io.getWorkerInfo();
		imageAnalysis = ia.getWorkerInfo();
	}
	
}
