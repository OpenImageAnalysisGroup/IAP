/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on May 14, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.analysis;

/**
 * @author klukas
 */
public class WorkerInfo {

	public int todo, active, processed, speed;
	public String speedUnit;

	public WorkerInfo(int todo, int active, int processed, int speed, String speedUnit) {
		this.todo = todo;
		this.active = active;
		this.processed = processed;
		this.speed = speed;
		this.speedUnit = speedUnit;
	}
}
