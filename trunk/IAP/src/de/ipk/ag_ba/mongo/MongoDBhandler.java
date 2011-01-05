/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 12, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.mongo;

import java.io.InputStream;

import org.ErrorMsg;
import org.ObjectRef;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoDBhandler extends AbstractResourceIOHandler {
	private final String PREFIX;
	private final MongoDB m;
	
	public MongoDBhandler(String ip, MongoDB m) {
		this.m = m;
		PREFIX = "mongo_" + ip + "_" + m.getDatabaseName();
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
						throws Exception {
		return null;
	}
	
	@Override
	public InputStream getInputStream(final IOurl url) throws Exception {
		final ObjectRef or = new ObjectRef();
		
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				GridFS gridfs_images = new GridFS(db, "images");
				GridFSDBFile fff = gridfs_images.findOne(url.getDetail());
				if (fff != null) {
					try {
						InputStream is = fff.getInputStream();
						or.setObject(is);
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				} else {
					GridFS gridfs_volumes = new GridFS(db, "volumes");
					fff = gridfs_volumes.findOne(url.getDetail());
					if (fff != null) {
						try {
							System.out.println("Input stream for " + url + ", length: " + fff.getLength());
							InputStream is = fff.getInputStream();
							or.setObject(is);
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
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