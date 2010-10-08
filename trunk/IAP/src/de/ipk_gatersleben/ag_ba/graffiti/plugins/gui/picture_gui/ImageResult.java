package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

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
	public File downloadedFile;

	public String getFileName() {
		return bfi.getFileName().getFileName();
	}

	public MyImageIcon getPreviewIcon() {
		return previewIcon;
	}

	public MappingDataEntity getTargetTable() {
		return bfi.getEntity();
	}

	public String getMd5() {
		return bfi.getMD5();
	}

	public BinaryFileInfo getBinaryFileInfo() {
		return bfi;
	}

	public File getDownloadedFile() {
		return downloadedFile;
	}
}
