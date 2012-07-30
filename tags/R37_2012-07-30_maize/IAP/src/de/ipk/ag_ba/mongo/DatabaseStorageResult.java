/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.mongo;

/**
 * @author klukas
 */
public enum DatabaseStorageResult {
	EXISITING_NO_STORAGE_NEEDED("known"), IO_ERROR_SEE_ERRORMSG("I/O error"), STORED_IN_DB("stored"),
	IO_ERROR_INPUT_NOT_AVAILABLE("INPUT error");
	
	private String desc;
	
	DatabaseStorageResult(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() {
		return desc;
	}
	
	private String md5;
	
	public void setMD5(String md5) {
		this.md5 = md5;
	}
	
	public String getMD5() {
		return md5;
	}
}
