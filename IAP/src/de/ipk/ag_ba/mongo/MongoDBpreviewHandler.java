/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

import java.io.InputStream;

import org.ErrorMsg;
import org.ObjectRef;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOHandler;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author klukas
 * 
 */
public class MongoDBpreviewHandler implements ResourceIOHandler {
	// mongo-preview://c3fd77bc7b74388d9dcff9d09d1c16fc/000Grad.png
	public static final String PREFIX = "mongo-preview";

	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
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
				GridFS gridfs_preview_images = new GridFS(db, "preview_files");
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
