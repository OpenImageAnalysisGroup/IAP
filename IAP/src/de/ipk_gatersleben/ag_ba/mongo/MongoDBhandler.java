/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Oct 12, 2010 by Christian Klukas
 */
package de.ipk_gatersleben.ag_ba.mongo;

import java.io.InputStream;

import org.ErrorMsg;
import org.ObjectRef;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.IOurl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementIOConfigObject;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementIOHandler;

public class MongoDBhandler implements MeasurementIOHandler {

	// mongo://c3fd77bc7b74388d9dcff9d09d1c16fc/000Grad.png
	public static final String PREFIX = "mongo";

	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, MeasurementIOConfigObject config)
			throws Exception {
		return null;
	}

	@Override
	public InputStream getInputStream(final IOurl url) throws Exception {
		final ObjectRef or = new ObjectRef();

		new MongoDB().processDB(new RunnableOnDB() {
			private DB db;

			@Override
			public void run() {
				GridFS gridfs_preview_images = new GridFS(db, "images");
				GridFSDBFile fff = gridfs_preview_images.findOne(url.getDetail());
				if (fff != null) {
					try {
						InputStream is = fff.getInputStream();
						or.setObject(is);
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}

			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		return (InputStream) or.getObject();
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
}