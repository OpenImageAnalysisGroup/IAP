/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.WeakHashMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
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
	public ExperimentInterface experiment;
	private ExperimentHeaderInterface header;
	public MongoDB m;
	
	private static WeakHashMap<String, ExperimentInterface> weakId2exp =
			new WeakHashMap<String, ExperimentInterface>();
	
	public ExperimentReference(ExperimentHeaderInterface header) {
		this.experimentName = header.getExperimentName();
		this.header = header;
	}
	
	public ExperimentReference(String databaseID) {
		if (databaseID.startsWith("lemnatec:")) {
			String db = databaseID.split(":")[1];
			try {
				Collection<ExperimentHeaderInterface> res = new LemnaTecDataExchange().getExperimentsInDatabase(
						SystemAnalysis.getUserName(), db);
				for (ExperimentHeaderInterface ehi : res) {
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
	
	public ExperimentReference(ExperimentHeaderInterface ehi, MongoDB m) {
		this(ehi);
		this.m = m;
	}
	
	public ExperimentInterface getData(MongoDB m) throws Exception {
		return getData(m, false, null);
	}
	
	public synchronized ExperimentInterface getData(MongoDB m,
			boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (experiment != null)
			return experiment;
		else {
			synchronized (ExperimentReference.class) {
				ExperimentInterface res = weakId2exp.get(header.getDatabaseId());
				if (res != null)
					return res;
				if (header.getDatabaseId().startsWith("lemnatec:"))
					res = new LemnaTecDataExchange().getExperiment(header, status);
				else
					if (header.getDatabaseId().startsWith("hsm:")) {
						res = HSMfolderTargetDataManager.getExperiment(header.getDatabaseId());
					} else
						if (m != null)
							res = m.getExperiment(header, interactiveGetExperimentSize, status);
						else
							res = this.m.getExperiment(header, interactiveGetExperimentSize, status);
				weakId2exp.put(header.getDatabaseId(), res);
				return res;
			}
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
	
	public ExperimentInterface getData(BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		return getData(m, status != null, status);
	}
}
