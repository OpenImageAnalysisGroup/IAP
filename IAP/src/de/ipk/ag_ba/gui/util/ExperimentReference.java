/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ExperimentReference {

	private final String experimentName;
	private ExperimentInterface experiment;
	private ExperimentHeaderInterface header;

	public ExperimentReference(ExperimentHeaderInterface header) {
		this.experimentName = header.getExperimentname();
		this.header = header;
	}

	public ExperimentReference(ExperimentInterface experiment) {
		this.experimentName = experiment.getName();
		this.experiment = experiment;
	}

	public ExperimentInterface getData() throws Exception {
		if (experiment != null)
			return experiment;
		else
			return new MongoDB().getExperiment(header);
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentData(ExperimentInterface data) {
		this.experiment = data;
	}

	public ExperimentHeaderInterface getHeader() {
		return header;
	}
}
