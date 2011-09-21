/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import java.io.File;
import java.io.IOException;

import org.SystemAnalysis;
import org.bson.types.ObjectId;

import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecDataExchange;
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
		this.experimentName = header.getExperimentName();
		this.header = header;
	}
	
	public ExperimentReference(String databaseID) {
		if (databaseID.startsWith("lemnatec:")) {
			String db = databaseID.split(":")[1];
			try {
				for (ExperimentHeaderInterface ehi : new LemnaTecDataExchange().getExperimentsInDatabase(SystemAnalysis.getUserName(), db)) {
					if (ehi.getDatabaseId().equals(databaseID)) {
						header = ehi;
						break;
					}
				}
			} catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		} else {
			if (databaseID.startsWith("hsm:")) {
				String fileName = databaseID.substring("hsm:".length());
				try {
					if (new File(fileName).exists())
						header = HsmFileSystemSource.getHSMexperimentHeaderFromFullyQualifiedFileName(fileName);
					else
						header = null;
				} catch (IOException e) {
					throw new UnsupportedOperationException(e);
				}
			} else {
				for (MongoDB m : MongoDB.getMongos()) {
					header = m.getExperimentHeader(new ObjectId(databaseID));
					if (header != null)
						break;
				}
			}
		}
		this.experimentName = header != null ? header.getExperimentName() : null;
	}
	
	public ExperimentReference(ExperimentInterface experiment) {
		this.experimentName = experiment.getName();
		this.experiment = experiment;
		this.header = experiment.getHeader();
	}
	
	public ExperimentInterface getData(MongoDB m) throws Exception {
		return getData(m, false);
	}
	
	public ExperimentInterface getData(MongoDB m, boolean interactiveGetExperimentSize) throws Exception {
		if (experiment != null)
			return experiment;
		else {
			if (header.getDatabaseId().startsWith("lemnatec:"))
				return new LemnaTecDataExchange().getExperiment(header, null);
			else
				if (header.getDatabaseId().startsWith("hsm:"))
					return HSMfolderTargetDataManager.getExperiment(header.getDatabaseId());
				else
					return m.getExperiment(header, interactiveGetExperimentSize, null);
		}
	}
	
	public String getExperimentName() {
		return experimentName;
	}
	
	public void setExperimentData(ExperimentInterface data) {
		this.experiment = data;
	}
	
	public ExperimentHeaderInterface getHeader() {
		if (header == null) {
			if (experiment != null)
				return experiment.getHeader();
			else
				return null;
		} else
			return header;
	}
}
