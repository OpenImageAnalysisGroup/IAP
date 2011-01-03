/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

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
