/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 17, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.picture_gui;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

/**
 * @author klukas
 * 
 */
public class BinaryFileInfo {

	private IOurl fileName;
	private final boolean primary;
	private final MappingDataEntity entity;

	public BinaryFileInfo(IOurl fileName, boolean primary, MappingDataEntity entity) {
		this.fileName = fileName;
		this.primary = primary;
		this.entity = entity;
	}

	public String getMD5() {
		return ((BinaryMeasurement) entity).getURL().getDetail();
	}

	public IOurl getFileName() {
		return fileName;
	}

	public boolean isPrimary() {
		return primary;
	}

	public MappingDataEntity getEntity() {
		return entity;
	}

	public void setFileName(IOurl fileName) {
		this.fileName = fileName;
	}
}
