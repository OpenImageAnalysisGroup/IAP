/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Oct 11, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.IOurl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementIOConfigObject;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementIOHandler;

/**
 * @author klukas
 * 
 */
public class LemnaTecFTPhandler implements MeasurementIOHandler {

	public static final String PREFIX = "lemnatec-ftp";

	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.isEqualPrefix(getPrefix())) {
			String detail = url.getDetail();
			detail = detail.split("/", 2)[0] + "/../../data0/pgftp/" + detail.split("/", 2)[1] + "/";
			String ur = "ftp://lemnatec:LemnaTec@" + detail.substring(0, detail.length() - "/".length());
			return new BufferedInputStream(new URL(ur).openStream());
		} else
			return null;
	}

	public static IOurl getLemnaTecFTPurl(String host, String filename, String displayFileName) {
		if (filename.contains("/")) {
			host += "/" + filename.substring(0, filename.lastIndexOf("/"));
			filename = filename.substring(filename.lastIndexOf("/") + "/".length());
			host += "/" + filename;
		}
		return new IOurl(PREFIX, host, displayFileName);
	}

	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, MeasurementIOConfigObject config)
			throws Exception {
		throw new Exception("LemnaTec FTP Output is not supported!");
	}

	public static IOurl getURL(File file) {
		return new IOurl(PREFIX, file.getParent() + "/", file.getName());
	}

	public static boolean isLemnaTecFtpUrl(IOurl fileName) {
		if (fileName != null && fileName.getPrefix() != null)
			return fileName.getPrefix().equals(PREFIX);
		else
			return false;
	}
}
