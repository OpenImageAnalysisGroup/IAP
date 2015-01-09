package de.ipk.ag_ba.mongo;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFS;

public class CollectionStorage {
	
	public DBCollection constantSrc2hash;
	
	public GridFS gridfs_images;
	public DBCollection fs_image_files;
	
	public GridFS gridfs_label_files;
	public DBCollection fs_image_labels;
	
	public GridFS gridfs_preview_files;
	public DBCollection fs_preview_files;
	
	public CollectionStorage(DB db, boolean ensureIndex) {
		constantSrc2hash = db.getCollection("constantSrc2hash");
		gridfs_images = new GridFS(db, MongoGridFS.FS_IMAGES.toString());
		fs_image_files = db.getCollection(MongoGridFS.FS_IMAGES_FILES.toString());
		if (ensureIndex)
			fs_image_files.createIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		gridfs_label_files = new GridFS(db, MongoGridFS.FS_IMAGE_LABELS.toString());
		fs_image_labels = db.getCollection(MongoGridFS.FS_IMAGE_LABELS_FILES.toString());
		if (ensureIndex)
			fs_image_labels.createIndex(MongoGridFS.FIELD_FILENAME.toString());
		
		gridfs_preview_files = new GridFS(db, MongoGridFS.FS_PREVIEW.toString());
		fs_preview_files = db.getCollection(MongoGridFS.FS_PREVIEW_FILES.toString());
		if (ensureIndex)
			fs_preview_files.createIndex(MongoGridFS.FIELD_FILENAME.toString());
		
	}
}
