/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 17, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.util;

import info.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.IniIoProvider;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.bson.types.ObjectId;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.experiment.process.ExperimentAnalysisSettingsIOprovder;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.ExperimentLoader;
import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ExperimentReference {
	
	private final String experimentName;
	private ExperimentInterface experiment;
	private ExperimentHeaderInterface header;
	public MongoDB m;
	
	private static ArrayList<ExperimentLoader> knownExperimentLoaders = new ArrayList<ExperimentLoader>();
	
	// private static WeakHashMap<String, ExperimentInterface> weakId2exp =
	// new WeakHashMap<String, ExperimentInterface>();
	
	public ExperimentReference(ExperimentHeaderInterface header) {
		this.experimentName = header.getExperimentName();
		this.header = header;
	}
	
	public ExperimentReference(String databaseID) {
		if (databaseID.startsWith("lt:")) {
			String db = databaseID.split(":")[1];
			try {
				Collection<ExperimentHeaderInterface> res = new LTdataExchange().getExperimentsInDatabase(
						null/* SystemAnalysis.getUserName() */, db);
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
			boolean vfsFound = false;
			if (databaseID.indexOf(":") > 0) {
				String pre = databaseID.substring(0, databaseID.indexOf(":"));
				for (Boolean ignorePrefix : new Boolean[] { Boolean.FALSE, Boolean.TRUE })
					for (VirtualFileSystem vfs : VirtualFileSystem.getKnown(true, false)) {
						if (pre.equals(vfs.getPrefix()) | ignorePrefix) {
							if (ignorePrefix)
								databaseID = databaseID.substring(databaseID.indexOf(":") + ":".length());
							Library lib = new Library();
							String ico = IAPimages.getFolderRemoteClosed();
							String ico2 = IAPimages.getFolderRemoteOpen();
							String ico3 = IAPimages.getFolderRemoteClosed();
							VfsFileSystemSource dataSource = new VfsFileSystemSource(lib, vfs.getTargetName(), vfs, new String[] {}, IAPmain.loadIcon(ico),
									IAPmain.loadIcon(ico2), IAPmain.loadIcon(ico3));
							try {
								databaseID = StringManipulationTools.stringReplace(databaseID, "\\", "/");
								for (ExperimentReference ehi : dataSource.getAllExperiments()) {
									if (ehi != null) {
										String dbi = ehi.getHeader().getDatabaseId();
										if (ignorePrefix && dbi.indexOf(":") > 0)
											dbi = dbi.substring(dbi.indexOf(":") + ":".length());
										dbi = StringManipulationTools.stringReplace(dbi, "\\", "/");
										if (dbi.equals(databaseID)) {
											header = ehi.getHeader();
											vfsFound = true;
											break;
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
			}
			if (!vfsFound)
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
						if (header != null) {
							setIniIoProvider(new ExperimentAnalysisSettingsIOprovder(this.getHeader(), m));
							break;
						}
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
		setIniIoProvider(new ExperimentAnalysisSettingsIOprovder(this.getHeader(), m));
	}
	
	public ExperimentInterface getData() throws Exception {
		return getData(false, null);
	}
	
	public static void registerExperimentLoader(ExperimentLoader loader) {
		if (!knownExperimentLoaders.contains(loader))
			knownExperimentLoaders.add(loader);
	}
	
	public ExperimentInterface getExperiment() {
		if (isLoading) {
			try {
				StopWatch s = new StopWatch("Wait for data loading...", true);
				do {
					Thread.sleep(50);
				} while (isLoading);
				s.printTime();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return experiment;
	}
	
	public ExperimentInterface getData(
			boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (experiment != null)
			return experiment;
		else {
			synchronized (ExperimentReference.class) {
				if (experiment != null)
					return experiment;
				String databaseId = header.getDatabaseId();
				ExperimentInterface res = null; // weakId2exp.get(databaseId);
				// if (res != null)
				// return res;
				for (ExperimentLoader loader : knownExperimentLoaders) {
					if (loader.canHandle(databaseId)) {
						res = loader.getExperiment(header, interactiveGetExperimentSize, status);
						if (res != null) {
							experiment = res;
							return res;
						}
					}
				}
				if (databaseId != null)
					if (databaseId.startsWith("lt:"))
						res = new LTdataExchange().getExperiment(header, interactiveGetExperimentSize, status);
					else
						if (databaseId.startsWith("hsm:")) {
							res = HSMfolderTargetDataManager.getExperiment(databaseId);
						} else
							if (m != null)
								res = m.getExperiment(header, interactiveGetExperimentSize, status);
							else
								res = null;
				// weakId2exp.put(databaseId, res);
				this.experiment = res;
				if (res == null)
					System.out.println(SystemAnalysis.getCurrentTime() + ">Experiment could not be loaded. DB ID: " + header.getDatabaseId());
				// else
				// System.out.println(SystemAnalysis.getCurrentTime() + ">Loaded experiment with " +
				// experiment.size() + " substances (DB ID " + res.getHeader().getDatabaseId() + ").");
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
		return getData(status != null, status);
	}
	
	private boolean isLoading = false;
	private BackgroundTaskStatusProviderSupportingExternalCall loaderStatus = null;
	
	public void loadDataInBackground(final BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (experiment != null)
			return;
		synchronized (todoOnceDataIsAvailable) {
			isLoading = true;
			loaderStatus = status;
			BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					try {
						experiment = getData(status);
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					} finally {
						isLoading = false;
						loaderStatus = null;
						synchronized (todoOnceDataIsAvailable) {
							for (Runnable r : todoOnceDataIsAvailable) {
								try {
									r.run();
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
							todoOnceDataIsAvailable.clear();
						}
					}
				}
			}, "Load experiment " + header.getExperimentName());
		}
	}
	
	private final ArrayList<Runnable> todoOnceDataIsAvailable = new ArrayList<Runnable>();
	private IniIoProvider storedIniProvider;
	private DataSourceLevel dataSourceLevel;
	
	public synchronized void runAsDataBecomesAvailable(Runnable r) {
		synchronized (todoOnceDataIsAvailable) {
			if (isLoading) {
				todoOnceDataIsAvailable.add(r);
			} else
				r.run();
		}
	}
	
	public ExperimentInterface getExperimentPeek() {
		return experiment;
	}
	
	public IniIoProvider getIniIoProvider() {
		if (storedIniProvider == null) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">INFO: Add generic INI-Provider info to experiment reference");
			storedIniProvider = new ExperimentAnalysisSettingsIOprovder(this.getHeader(), null);
		}
		return storedIniProvider;
	}
	
	public void setIniIoProvider(IniIoProvider iniProvider) {
		storedIniProvider = iniProvider;
	}
	
	public ExperimentIoHelper getIoHelper() {
		return new ExperimentIoHelper(m);
	}
}
