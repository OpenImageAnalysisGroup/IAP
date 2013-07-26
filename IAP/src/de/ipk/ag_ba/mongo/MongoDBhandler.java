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

import org.ErrorMsg;
import org.ObjectRef;
import org.SystemOptions;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.postgresql.LTftpHandler;

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
		int size = is.available();
		String hash = GravistoService.getHashFromInputStream(is, resultFileSize, m.getHashType());
		is = new MyByteArrayInputStream(is.getBuff(), size);
		GridFS fs = m.getGridFS(c);
		long res = m.saveStream(hash, is, fs, resultFileSize.getLong());
		if (res >= 0)
			return new IOurl(getPrefix(), hash, targetFilename);
		else
			return null;
	}
	
	@Override
	public InputStream getInputStream(final IOurl url) throws Exception {
		final ObjectRef or = new ObjectRef();
		final ObjectRef err = new ObjectRef();
		final boolean getRemoteLTdataNotSavedInMongo = SystemOptions.getInstance().getBoolean("GRID-STORAGE", "Load missing input from LT storage", true);
		
		m.processDB(new RunnableOnDB() {
			private DB db;
			
			@Override
			public void run() {
				if (getRemoteLTdataNotSavedInMongo) {
					try {
						if (url.getDetail().indexOf(".") > 0) {
							for (ResourceIOHandler rh : ResourceIOManager.getInstance().getHandlers())
								if (rh instanceof LTftpHandler) {
									InputStream is;
									IOurl url2 = new IOurl(url);
									url2.setPrefix(rh.getPrefix());
									is = rh.getInputStream(url2);
									if (is != null) {
										if (is != null) {
											if (is instanceof MyByteArrayInputStream) {
												VirtualFileSystemVFS2.readCounter.addLong(((MyByteArrayInputStream) is).getCount());
											}
											or.setObject(is);
											return;
										}
									}
								}
						}
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				for (String fs : MongoGridFS.getFileCollections()) {
					InputStream vfs_is = m.getVFSinputStream(fs, url.getDetail());
					if (vfs_is != null) {
						if (vfs_is instanceof MyByteArrayInputStream) {
							VirtualFileSystemVFS2.readCounter.addLong(((MyByteArrayInputStream) vfs_is).getCount());
						}
						
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
							if (is != null && is instanceof MyByteArrayInputStream) {
								VirtualFileSystemVFS2.readCounter.addLong(((MyByteArrayInputStream) is).getCount());
							} else
								if (is != null) {
									is = ResourceIOManager.getInputStreamMemoryCached(is);
									VirtualFileSystemVFS2.readCounter.addLong(((MyByteArrayInputStream) is).getCount());
								}
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
		return res;
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
	
	public boolean hasInputStreamForHash(final String hash) {
		final ObjectRef found = new ObjectRef();
		found.setObject(Boolean.FALSE);
		
		try {
			m.processDB(new RunnableOnDB() {
				private DB db;
				
				@Override
				public void run() {
					for (String fs : MongoGridFS.getFileCollections()) {
						if (m.hasVFSinputStream(fs, hash)) {
							found.setObject(Boolean.TRUE);
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
						
						GridFSDBFile fff = gridfs.findOne(hash);
						if (fff != null) {
							found.setObject(Boolean.TRUE);
						}
					}
				}
				
				@Override
				public void setDB(DB db) {
					this.db = db;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return (Boolean) found.getObject();
	}
}