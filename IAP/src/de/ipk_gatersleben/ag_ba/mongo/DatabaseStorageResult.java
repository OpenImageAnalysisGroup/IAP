/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.mongo;

/**
 * @author klukas
 */
public enum DatabaseStorageResult {
	EXISITING_NO_STORAGE_NEEDED, IO_ERROR_SEE_ERRORMSG, STORED_IN_DB;

	private String md5;

	public void setMD5(String md5) {
		this.md5 = md5;
	}

	public String getMD5() {
		return md5;
	}
}
