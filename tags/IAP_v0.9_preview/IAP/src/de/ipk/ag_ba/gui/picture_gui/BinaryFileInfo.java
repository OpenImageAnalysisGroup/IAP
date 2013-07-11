/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
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
 */
public class BinaryFileInfo {
	
	private IOurl fileNameMain, fileNameLabel;
	private final boolean primary;
	final MappingDataEntity entity;
	private String additionalItemInfo;
	private boolean isAttachment;
	
	public BinaryFileInfo(IOurl fileNameMain, IOurl fileNameLabel, boolean primary, MappingDataEntity entity, String additionalItemInfo) {
		this(fileNameMain, fileNameLabel, primary, entity);
		this.additionalItemInfo = additionalItemInfo;
	}
	
	public BinaryFileInfo(IOurl fileNameMain, IOurl fileNameLabel, boolean primary, MappingDataEntity entity) {
		this.fileNameMain = fileNameMain;
		this.fileNameLabel = fileNameLabel;
		this.primary = primary;
		this.entity = entity;
	}
	
	public String getHashMain() {
		return ((BinaryMeasurement) entity).getURL().getDetail();
	}
	
	public String getHashLabel() {
		if ((entity instanceof BinaryMeasurement) && ((BinaryMeasurement) entity).getLabelURL() != null)
			return ((BinaryMeasurement) entity).getLabelURL().getDetail();
		else
			return null;
	}
	
	public IOurl getFileNameMain() {
		return fileNameMain;
	}
	
	public IOurl getFileNameLabel() {
		return fileNameLabel;
	}
	
	public boolean isPrimary() {
		return primary;
	}
	
	public MappingDataEntity getEntity() {
		return entity;
	}
	
	public void setFileNameMain(IOurl fileName) {
		this.fileNameMain = fileName;
	}
	
	public void setFileNameLabel(IOurl fileName) {
		this.fileNameLabel = fileName;
	}
	
	public String getAdditionalFileNameInfo() {
		return additionalItemInfo;
	}
	
	public void setIsAttachment(boolean isAttachment) {
		this.isAttachment = isAttachment;
	}
	
	public boolean isAttachment() {
		return isAttachment;
	}
}
