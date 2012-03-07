package de.ipk.ag_ba.server.task_management;

import java.util.Date;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class IdTime {
	
	public Date time;
	public String Id;
	private final ExperimentHeaderInterface exp;
	private final MongoDB m;
	
	public IdTime(MongoDB m, String Id, Date date, ExperimentHeaderInterface exp) {
		this.m = m;
		this.Id = Id;
		if (Id == null) {
			System.out.println("ERR");
			this.Id = "";
		}
		if (date == null) {
			System.out.println("ERR 2");
			date = new Date();
		}
		this.time = date;
		this.exp = exp;
	}
	
	public ExperimentHeaderInterface getExperimentHeader() {
		return exp;
	}
	
	@Override
	public boolean equals(Object obj) {
		return Id.equals(((IdTime) obj).Id);
	}
	
	public MongoDB getMongoDB() {
		return m;
	}
}
