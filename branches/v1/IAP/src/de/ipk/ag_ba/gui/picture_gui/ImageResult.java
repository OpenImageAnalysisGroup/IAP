package de.ipk.ag_ba.gui.picture_gui;

import java.io.File;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;

/**
 * @author Christian Klukas
 */
public class ImageResult {
	
	private final MyImageIcon previewIcon;
	
	private final BinaryFileInfo bfi;
	
	public ImageResult(MyImageIcon previewIcon, BinaryFileInfo bfi) {
		this.previewIcon = previewIcon;
		this.bfi = bfi;
	}
	
	/**
	 * The file that is created temporarily for filling data from the database.
	 * This file is used for the "Show Image" command and "Save As..." commands
	 * for the <code>MyImageButton</code>. This might be NULL if it is not yet
	 * downloaded or unavailable.
	 */
	public File downloadedFileMain, downloadedFileLabel;
	
	public String getFileNameMain() {
		String fn = bfi.getFileNameMain().getFileName();
		if (fn != null && fn.indexOf("#") > 0)
			return fn.substring(fn.indexOf("#") + 1);
		return fn;
	}
	
	public String getFileNameLabel() {
		return (bfi != null && bfi.getFileNameLabel() != null) ? bfi.getFileNameLabel().getFileName() : null;
	}
	
	public MyImageIcon getPreviewIcon() {
		return previewIcon;
	}
	
	public MappingDataEntity getTargetTable() {
		return bfi.getEntity();
	}
	
	public String getHashMain() {
		return bfi.getHashMain();
	}
	
	public String getHashLabel() {
		return bfi.getHashLabel();
	}
	
	public BinaryFileInfo getBinaryFileInfo() {
		return bfi;
	}
	
	public File getDownloadedFileMain() {
		return downloadedFileMain;
	}
	
	public File getDownloadedFileLabel() {
		return downloadedFileLabel;
	}
}
