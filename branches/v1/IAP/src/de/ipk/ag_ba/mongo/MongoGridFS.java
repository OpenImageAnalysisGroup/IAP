package de.ipk.ag_ba.mongo;

import java.util.ArrayList;

import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public enum MongoGridFS {
	FIELD_FILENAME("filename"),
	FS_IMAGES("fs_images"), FS_IMAGES_FILES("fs_images.files"),
	FS_IMAGE_LABELS("fs_image_labels"), FS_IMAGE_LABELS_FILES("fs_image_labels.files"),
	FS_PREVIEW("fs_preview"), FS_PREVIEW_FILES("fs_preview.files"),
	FS_VOLUMES("fs_volumes"), FS_VOLUMES_FILES("fs_volumes.files"),
	FS_VOLUME_LABELS("fs_volume_labels"), FS_VOLUME_LABELS_FILES("fs_volume_labels.files"),
	FS_NETWORKS("fs_networks"), FS_NETWORKS_FILES("fs_networks.files"),
	FS_NETWORK_LABELS("fs_network_labels"), fs_networks_labels_files("fs_network_labels.files"),
	FS_ANNOTATION_FILES("fs_annotation_files");
	
	private String collection_or_field;
	
	private MongoGridFS(String collection_or_filed) {
		this.collection_or_field = collection_or_filed;
	}
	
	@Override
	public String toString() {
		return collection_or_field;
	}
	
	public static ArrayList<String> getFileCollections() {
		ArrayList<String> res = new ArrayList<String>();
		for (MongoGridFS fs : values()) {
			if (fs != FIELD_FILENAME)
				if (!fs.toString().contains("."))
					if (!fs.toString().contains("preview"))
						res.add(fs.toString());
		}
		return res;
	}
	
	public static ArrayList<String> getFileCollectionsInclPreview() {
		ArrayList<String> res = new ArrayList<String>();
		for (MongoGridFS fs : values()) {
			if (fs != FIELD_FILENAME)
				if (!fs.toString().contains("."))
					res.add(fs.toString());
		}
		return res;
	}
	
	public static ArrayList<String> getFileCollectionsFor(NumericMeasurementInterface nmd) {
		ArrayList<String> res = new ArrayList<String>();
		if (nmd == null) {
			res.add(FS_ANNOTATION_FILES.toString());
		} else
			if (nmd instanceof ImageData) {
				res.add(FS_IMAGES.toString());
				res.add(FS_IMAGE_LABELS.toString());
			} else
				if (nmd instanceof VolumeData) {
					res.add(FS_VOLUMES.toString());
					res.add(FS_VOLUME_LABELS.toString());
				} else
					if (nmd instanceof NetworkData) {
						res.add(FS_NETWORKS.toString());
						res.add(FS_NETWORK_LABELS.toString());
					} else
						res.addAll(getFileCollections());
		return res;
	}
	
	public static ArrayList<GridFS> getGridFsFileCollectionsFor(DB db, NumericMeasurementInterface nmd) {
		ArrayList<GridFS> res = new ArrayList<GridFS>();
		for (String s : getFileCollectionsFor(nmd))
			res.add(new GridFS(db, s));
		return res;
	}
	
	public static ArrayList<String> getPreviewFileCollections() {
		ArrayList<String> res = new ArrayList<String>();
		for (MongoGridFS fs : values()) {
			if (fs != FIELD_FILENAME)
				if (!fs.toString().contains("."))
					if (fs.toString().contains("preview"))
						res.add(fs.toString());
		}
		return res;
	}
}
