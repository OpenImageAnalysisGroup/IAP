/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 12, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.ObjectRef;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoDBhandler extends AbstractResourceIOHandler {
	private final String prefix;
	private final MongoDB m;
	
	private final HashMap<String, GridFS> cachedCollection = new HashMap<String, GridFS>();
	
	public MongoDBhandler(String ip, MongoDB m) {
		this.m = m;
		this.prefix = "mongo_" + ip + "_" + m.getDatabaseName();
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream srcIS, String targetFilename, ResourceIOConfigObject config)
			throws Exception {
		MongoResourceIOConfigObject c = (MongoResourceIOConfigObject) config;
		
		MyByteArrayInputStream is = ResourceIOManager.getInputStreamMemoryCached(srcIS);
		ObjectRef resultFileSize = new ObjectRef();
		String hash = GravistoService.getHashFromInputStream(is, resultFileSize, m.getHashType());
		is = new MyByteArrayInputStream(is.getBuff(), is.available());
		
		GridFS fs = m.getGridFS(c);
		long res = m.saveStream(hash, is, fs);
		if (res >= 0)
			return new IOurl(getPrefix(), hash, targetFilename);
		else
			return null;
	}
	
	@Override
	public InputStream getInputStream(final IOurl url) throws Exception {
		final ObjectRef or = new ObjectRef();
		final ObjectRef err = new ObjectRef();
		
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				for (String fs : MongoGridFS.getFileCollectionsInclPreview()) {
					InputStream vfs_is = m.getVFSinputStream(fs, url.getDetail());
					if (vfs_is != null) {
						or.setObject(vfs_is);
						return;
					}
				}
				// check all gridFS file collections and look for matching hash value...
				boolean ensureIndex = false;
				if (ensureIndex)
					for (String fs : MongoGridFS.getFileCollections()) {
						DBCollection collectionChunks = db.getCollection(fs.toString() + ".chunks");
						collectionChunks.ensureIndex("files_id");
					}
				for (String fs : MongoGridFS.getFileCollections()) {
					GridFS gridfs = new GridFS(db, fs);
					
					GridFSDBFile fff = gridfs.findOne(url.getDetail());
					if (fff != null) {
						try {
							InputStream is = fff.getInputStream();
							// is = decompressStream(is);
							or.setObject(is);
							return;
						} catch (Exception e) {
							err.setObject(e);
						}
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (err.getObject() != null)
			throw (Exception) err.getObject();
		return (InputStream) or.getObject();
	}
	
	public static InputStream decompressStream(InputStream input) throws IOException {
		PushbackInputStream pb = new PushbackInputStream(input, 2);
		byte[] signature = new byte[2];
		pb.read(signature); // read the signature
		pb.unread(signature); // push back the signature to the stream
		if (signature[0] == 0x1f && signature[1] == 0x8b) // check if matches standard gzip magic number
			return new GZIPInputStream(pb);
		else
			return pb;
	}
	
	@Override
	public InputStream getPreviewInputStream(final IOurl url) throws Exception {
		final ObjectRef or = new ObjectRef();
		final ObjectRef err = new ObjectRef();
		
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				for (String fs : MongoGridFS.getPreviewFileCollections()) {
					
					InputStream vfs_is = m.getVFSinputStream(fs, url.getDetail());
					if (vfs_is != null) {
						or.setObject(vfs_is);
						return;
					}
				}
				
				// check all gridFS file collections and look for matching hash value...
				for (String fs : MongoGridFS.getPreviewFileCollections()) {
					DBCollection collectionChunks = db.getCollection(fs.toString() + ".chunks");
					collectionChunks.ensureIndex("files_id");
				}
				for (String fs : MongoGridFS.getPreviewFileCollections()) {
					GridFS gridfs;
					synchronized (cachedCollection) {
						if (!cachedCollection.containsKey(fs))
							cachedCollection.put(fs, new GridFS(db, fs));
						gridfs = cachedCollection.get(fs);
					}
					GridFSDBFile fff = gridfs.findOne(url.getDetail());
					if (fff != null) {
						try {
							InputStream is = fff.getInputStream();
							or.setObject(is);
							return;
						} catch (Exception e) {
							err.setObject(e);
						}
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (err.getObject() != null)
			throw (Exception) err.getObject();
		InputStream res = (InputStream) or.getObject();
		if (res != null) {
			return res;
		} else {
			MyByteArrayInputStream is = ((MyByteArrayInputStream) super.getPreviewInputStream(url));
			if (is != null) {
				final byte[] rrr = is.getBuffTrimmed();
				
				m.processDB(new RunnableOnDB() {
					
					private DB db;
					
					@Override
					public void run() {
						try {
							m.saveStream(
									url.getDetail(),
									new MyByteArrayInputStream(rrr, rrr.length),
									new GridFS(db, MongoGridFS.getPreviewFileCollections().get(0)));
						} catch (Exception e) {
							err.setObject(e);
						}
					}
					
					@Override
					public void setDB(DB db) {
						this.db = db;
					}
				});
				
				if (err.getObject() != null)
					throw (Exception) err.getObject();
				
				return new MyByteArrayInputStream(rrr, rrr.length);
			}
		}
		return null;
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public Long getStreamLength(final IOurl url) throws Exception {
		final ObjectRef orSize = new ObjectRef();
		final ObjectRef err = new ObjectRef();
		
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				// check all gridFS file collections and look for matching hash value...
				for (String fs : MongoGridFS.getFileCollections()) {
					GridFS gridfs = new GridFS(db, fs);
					GridFSDBFile fff = gridfs.findOne(url.getDetail());
					if (fff != null) {
						try {
							orSize.setObject(fff.getLength());
							return;
						} catch (Exception e) {
							err.setObject(e);
						}
					}
				}
			}
			
			@Override
			public void setDB(DB db) {
				this.db = db;
			}
		});
		if (err.getObject() != null)
			throw (Exception) err.getObject();
		return (Long) orSize.getObject();
	}
}