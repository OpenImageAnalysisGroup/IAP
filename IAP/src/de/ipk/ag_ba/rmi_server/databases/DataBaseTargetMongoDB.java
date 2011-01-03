/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.rmi_server.databases;

import java.io.InputStream;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.DB;

import de.ipk.ag_ba.mongo.DatabaseStorageResult;
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
	
	public DataBaseTargetMongoDB(boolean store, MongoDB m) {
		this.store = store;
		this.m = m;
	}
	
	@Override
	public LoadedImage saveImage(final LoadedImage limg) throws Exception {
		if (!store)
			return null;
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			public void run() {
				try {
					DatabaseStorageResult dsr = m.saveImageFile(db, limg, null);
					tso.setParam(0, dsr);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			
			public void setDB(DB db) {
				this.db = db;
			}
		});
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
	public void saveVolume(final LoadedVolume volume, Sample3D s3d, final MongoDB m, DBTable sample,
						InputStream threeDvolumePreviewIcon, final BackgroundTaskStatusProviderSupportingExternalCall optStatus) throws Exception {
		if (!store)
			return;
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			public void run() {
				m.saveVolumeFile(db, volume, null, optStatus);
			}
			
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
