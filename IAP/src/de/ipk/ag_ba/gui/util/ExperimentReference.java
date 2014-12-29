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
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ExperimentReference implements ExperimentReferenceInterface {
	
	private final String experimentName;
	protected ExperimentInterface experiment;
	private ExperimentHeaderInterface header;
	private MongoDB m;
	
	private static ArrayList<ExperimentLoader> knownExperimentLoaders = new ArrayList<ExperimentLoader>();
	
	// private static WeakHashMap<String, ExperimentInterface> weakId2exp =
	// new WeakHashMap<String, ExperimentInterface>();
	
	public ExperimentReference(ExperimentHeaderInterface header) {
		this.experimentName = header.getExperimentName();
		this.setHeader(header);
	}
	
	public ExperimentReference(String databaseID) {
		if (databaseID.startsWith("lt:")) {
			String db = databaseID.split(":")[1];
			try {
				Collection<ExperimentHeaderInterface> res = new LTdataExchange().getExperimentsInDatabase(
						null/* SystemAnalysis.getUserName() */, db);
				for (ExperimentHeaderInterface ehi : res) {
					if (ehi.getDatabaseId().equals(databaseID)) {
						setHeader(ehi);
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
							String ico4 = IAPimages.getFolderRemoteOpen();
							VfsFileSystemSource dataSource = new VfsFileSystemSource(lib, vfs.getTargetName(), vfs, new String[] {}, IAPmain.loadIcon(ico),
									IAPmain.loadIcon(ico2), IAPmain.loadIcon(ico3), IAPmain.loadIcon(ico4));
							try {
								databaseID = StringManipulationTools.stringReplace(databaseID, "\\", "/");
								for (ExperimentReferenceInterface ehi : dataSource.getAllExperiments()) {
									if (ehi != null) {
										String dbi = ehi.getHeader().getDatabaseId();
										if (ignorePrefix && dbi.indexOf(":") > 0)
											dbi = dbi.substring(dbi.indexOf(":") + ":".length());
										dbi = StringManipulationTools.stringReplace(dbi, "\\", "/");
										if (dbi.equals(databaseID)) {
											setHeader(ehi.getHeader());
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
							setHeader(HsmFileSystemSource.getHSMexperimentHeaderFromFullyQualifiedFileName(fileName));
						else
							setHeader(null);
					} catch (IOException e) {
						throw new UnsupportedOperationException(e);
					}
				} else {
					for (MongoDB m : MongoDB.getMongos()) {
						setHeader(m.getExperimentHeader(new ObjectId(databaseID)));
						if (getHeader() != null) {
							setIniIoProvider(new ExperimentAnalysisSettingsIOprovder(this.getHeader(), m));
							break;
						}
					}
				}
		}
		this.experimentName = getHeader() != null ? getHeader().getExperimentName() : null;
	}
	
	public ExperimentReference(ExperimentInterface experiment) {
		this.experimentName = experiment.getName();
		this.setExperiment(experiment);
		this.setHeader(experiment.getHeader());
	}
	
	public ExperimentReference(ExperimentHeaderInterface ehi, MongoDB m) {
		this(ehi);
		this.setM(m);
		setIniIoProvider(new ExperimentAnalysisSettingsIOprovder(this.getHeader(), m));
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getData()
	 */
	@Override
	public ExperimentInterface getData() throws Exception {
		return getData(false, null);
	}
	
	public static void registerExperimentLoader(ExperimentLoader loader) {
		if (!knownExperimentLoaders.contains(loader))
			knownExperimentLoaders.add(loader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getExperiment()
	 */
	@Override
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
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getData(boolean, org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public ExperimentInterface getData(
			boolean interactiveGetExperimentSize,
			BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (getExperimentPeek() != null)
			return getExperimentPeek();
		else {
			synchronized (ExperimentReference.class) {
				if (getExperimentPeek() != null)
					return getExperimentPeek();
				String databaseId = getHeader().getDatabaseId();
				ExperimentInterface res = null; // weakId2exp.get(databaseId);
				// if (res != null)
				// return res;
				for (ExperimentLoader loader : knownExperimentLoaders) {
					if (loader.canHandle(databaseId)) {
						res = loader.getExperiment(getHeader(), interactiveGetExperimentSize, status);
						if (res != null) {
							setExperiment(res);
							return res;
						}
					}
				}
				if (databaseId != null)
					if (databaseId.startsWith("lt:"))
						res = new LTdataExchange().getExperiment(getHeader(), interactiveGetExperimentSize, status);
					else
						if (databaseId.startsWith("hsm:")) {
							res = HSMfolderTargetDataManager.getExperiment(databaseId);
						} else
							if (getM() != null)
								res = getM().getExperiment(getHeader(), interactiveGetExperimentSize, status);
							else
								res = null;
				// weakId2exp.put(databaseId, res);
				this.setExperiment(res);
				if (res == null)
					System.out.println(SystemAnalysis.getCurrentTime() + ">Experiment could not be loaded. DB ID: " + getHeader().getDatabaseId());
				// else
				// System.out.println(SystemAnalysis.getCurrentTime() + ">Loaded experiment with " +
				// experiment.size() + " substances (DB ID " + res.getHeader().getDatabaseId() + ").");
				return res;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getExperimentName()
	 */
	@Override
	public String getExperimentName() {
		return experimentName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#setExperimentData(de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.
	 * ExperimentInterface)
	 */
	@Override
	public void setExperimentData(ExperimentInterface data) {
		this.setExperiment(data);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getHeader()
	 */
	@Override
	public ExperimentHeaderInterface getHeader() {
		if (header == null) {
			if (getExperimentPeek() != null)
				return getExperimentPeek().getHeader();
			else
				return null;
		} else
			return header;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getData(org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public ExperimentInterface getData(BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		return getData(status != null, status);
	}
	
	private boolean isLoading = false;
	private BackgroundTaskStatusProviderSupportingExternalCall loaderStatus = null;
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#loadDataInBackground(org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public void loadDataInBackground(final BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		if (getExperimentPeek() != null)
			return;
		synchronized (todoOnceDataIsAvailable) {
			isLoading = true;
			loaderStatus = status;
			BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					try {
						setExperiment(getData(status));
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
			}, "Load experiment " + getHeader().getExperimentName());
		}
	}
	
	private final ArrayList<Runnable> todoOnceDataIsAvailable = new ArrayList<Runnable>();
	private IniIoProvider storedIniProvider;
	private DataSourceLevel dataSourceLevel;
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#runAsDataBecomesAvailable(java.lang.Runnable)
	 */
	@Override
	public synchronized void runAsDataBecomesAvailable(Runnable r) {
		synchronized (todoOnceDataIsAvailable) {
			if (isLoading) {
				todoOnceDataIsAvailable.add(r);
			} else
				r.run();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getExperimentPeek()
	 */
	@Override
	public ExperimentInterface getExperimentPeek() {
		return experiment;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getIniIoProvider()
	 */
	@Override
	public IniIoProvider getIniIoProvider() {
		if (storedIniProvider == null) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">INFO: Add generic INI-Provider info to experiment reference");
			storedIniProvider = new ExperimentAnalysisSettingsIOprovder(this.getHeader(), null);
		}
		return storedIniProvider;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#setIniIoProvider(org.IniIoProvider)
	 */
	@Override
	public void setIniIoProvider(IniIoProvider iniProvider) {
		storedIniProvider = iniProvider;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getIoHelper()
	 */
	@Override
	public ExperimentIoHelper getIoHelper() {
		return new ExperimentIoHelper(getM());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#resetStoredHeader()
	 */
	@Override
	public void resetStoredHeader() {
		this.setHeader(null);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#setHeader(de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.
	 * ExperimentHeaderInterface)
	 */
	@Override
	public void setHeader(ExperimentHeaderInterface header) {
		this.header = header;
		setIniIoProvider(new ExperimentAnalysisSettingsIOprovder(this.getHeader(), getM()));
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#getM()
	 */
	@Override
	public MongoDB getM() {
		return m;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#visitConditions(java.lang.String, de.ipk.ag_ba.gui.util.ConditionVisitor)
	 */
	@Override
	public void visitConditions(String optSubstanceFilter, ConditionVisitor cv) throws Exception {
		for (SubstanceInterface si : getData().getSubstances())
			if (optSubstanceFilter == null || optSubstanceFilter.equals(si.getName()))
				for (ConditionInterface ci : si)
					cv.visit(ci);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#visitSamples(java.lang.String, de.ipk.ag_ba.gui.util.SampleVisitor)
	 */
	@Override
	public void visitSamples(String optSubstanceFilter, SampleVisitor nmi) throws Exception {
		for (SubstanceInterface si : getData().getSubstances())
			if (optSubstanceFilter == null || optSubstanceFilter.equals(si.getName()))
				for (ConditionInterface ci : si)
					for (SampleInterface sa : ci)
						nmi.visit(sa);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.ipk.ag_ba.gui.util.ExperimentReferenceInterface#visitNumericMeasurements(java.lang.String, de.ipk.ag_ba.gui.util.NumericMeasurementVisitor)
	 */
	@Override
	public void visitNumericMeasurements(String optSubstanceFilter, NumericMeasurementVisitor nmi) throws Exception {
		for (SubstanceInterface si : getData().getSubstances())
			if (optSubstanceFilter == null || optSubstanceFilter.equals(si.getName()))
				for (ConditionInterface ci : si)
					for (SampleInterface sa : ci)
						for (NumericMeasurementInterface n : sa)
							nmi.visit(n);
	}
	
	protected void setExperiment(ExperimentInterface experiment) {
		this.experiment = experiment;
	}
	
	@Override
	public void setM(MongoDB m) {
		this.m = m;
	}
}
