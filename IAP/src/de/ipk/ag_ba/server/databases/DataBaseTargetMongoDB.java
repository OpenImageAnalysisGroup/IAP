/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.databases;

import java.io.InputStream;
import java.util.HashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.DB;

import de.ipk.ag_ba.mongo.CollectionStorage;
import de.ipk.ag_ba.mongo.DatabaseStorageResult;
import de.ipk.ag_ba.mongo.ExperimentSaver;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableOnDB;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;

/**
 * @author klukas
 */
public class DataBaseTargetMongoDB implements DatabaseTarget {
	
	private final boolean store;
	private final MongoDB m;
	private final CollectionStorage cols;
	
	public DataBaseTargetMongoDB(boolean store, MongoDB m, CollectionStorage cols) {
		this.store = store;
		this.m = m;
		this.cols = cols;
	}
	
	@Override
	public LoadedImage saveImage(
			final String[] optFileNameMainAndLabelPrefix,
			final LoadedImage limg,
			final boolean keepRemoteURLs_safe_space,
			final boolean skipLabelProcessing) throws Exception {
		if (!store)
			return null;
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		m.processDB(new RunnableOnDB() {
			
			private DB db;
			
			@Override
			public void run() {
				try {
					DatabaseStorageResult dsr =
							ExperimentSaver.saveImageFileDirect(
									cols, db, limg, null,
									keepRemoteURLs_safe_space,
									skipLabelProcessing,
									m.getMongoHandler(), m.getHashType(), m,
									new HashSet<String>());
					tso.setParam(0, dsr);
				} catch (Exception e) {
					tso.setParam(1, e);
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		
		if (tso.getParam(1, null) != null)
			throw (Exception) tso.getParam(1, null);
		DatabaseStorageResult dsr = (DatabaseStorageResult) tso.getParam(0, DatabaseStorageResult.IO_ERROR_SEE_ERRORMSG);
		if (dsr == DatabaseStorageResult.STORED_IN_DB || dsr == DatabaseStorageResult.EXISITING_NO_STORAGE_NEEDED)
			return limg;
		else
			return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @seermi_server.databases.DatabaseTarget#saveVolume(rmi_server.analysis.
	 * image_analysis_tasks.LoadedVolume,
	 * de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D, java.lang.String,
	 * java.lang.String, de.ipk_gatersleben.ag_pbi.dbe2.webService.enums.DBTable,
	 * java.lang.String, java.io.InputStream, java.lang.String, int,
	 * java.io.InputStream)
	 */
	@Override
	public void saveVolume(final LoadedVolume volume, Sample3D s3d, final MongoDB m,
			InputStream threeDvolumePreviewIcon, final BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		if (!store)
			return;
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				ExperimentSaver.saveVolumeFile(db, volume, null, optStatus, m.getHashType());
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
	}
	
	@Override
	public String getPrefix() {
		return m.getPrimaryHandler().getPrefix();
	}
}
