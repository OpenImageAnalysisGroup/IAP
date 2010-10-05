/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 17, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

/**
 * @author klukas
 * 
 */
public class BinaryFileInfo {

	private String fileName;
	private final boolean primary;
	private final MappingDataEntity entity;

	public BinaryFileInfo(String fileName, boolean primary, MappingDataEntity entity) {
		this.fileName = fileName;
		this.primary = primary;
		this.entity = entity;
	}

	public String getMD5() {
		return ((BinaryMeasurement) entity).getURL().getDetail();
	}

	public String getFileName() {
		return fileName;
	}

	public boolean isPrimary() {
		return primary;
	}

	public MappingDataEntity getEntity() {
		return entity;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
