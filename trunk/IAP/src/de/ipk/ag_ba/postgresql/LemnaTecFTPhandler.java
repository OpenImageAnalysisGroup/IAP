/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 11, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class LemnaTecFTPhandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "lemnatec-ftp";
	
	private static boolean useSCP = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use SCP instead of FTP", false);
	private static String ftpHost = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP host", "lemna-db.ipk-gatersleben.de");
	private static String ftpLocalFolder = SystemOptions.getInstance()
			.getString("LT-DB", "Image File Transfer//FTP directory prefix", "/../../data0/pgftp/");
	private static String ftpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP user", "lemnatec");
	private static String ftpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP password", "LemnaTec");
	
	private static String scpHost = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP host", "lemna-db.ipk-gatersleben.de");
	private static String scpLocalFolder = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP local storage folder", "/data0/pgftp/");
	private static String scpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP user", "root");
	private static String scpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP password", "LemnaTec");
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		useSCP = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use SCP instead of FTP", false);
		ftpLocalFolder = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP directory prefix", "/../../data0/pgftp/");
		ftpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP user", "lemnatec");
		ftpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP password", "LemnaTec");
		
		scpHost = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP host", "lemna-db.ipk-gatersleben.de");
		scpLocalFolder = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP local storage folder", "/data0/pgftp/");
		scpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP user", "root");
		scpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP password", "LemnaTec");
		
		if (url.toString().contains(",")) {
			url = new IOurl(url.toString().split(",")[0]);
		}
		
		if (url.isEqualPrefix(getPrefix())) {
			if (useSCP) {
				String detail = url.getDetail();
				detail = scpLocalFolder + detail.split("/", 2)[1];
				String dir = detail.substring(0, detail.lastIndexOf("/"));
				String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
				VfsFileObject fo = VfsFileObjectUtil.createVfsFileObject(VfsFileProtocol.SFTP, scpHost, dir + "/" + fn, scpUser, scpPassword);
				return ResourceIOManager.getInputStreamMemoryCached(fo.getInputStream());
				// InputStream iss = null;
				// // synchronized (PREFIX) {
				// ChannelSftp c = getNewChannel(scpUser, scpPassword, scpHost);
				// c.getSession().setTimeout(60 * 60 * 1000); // set timeout of 60 minutes
				// String detail = url.getDetail();
				// detail = scpLocalFolder + detail.split("/", 2)[1];
				// String dir = detail.substring(0, detail.lastIndexOf("/"));
				// System.out.println(SystemAnalysis.getCurrentTime() + ">SCP change directory: " + dir);
				// c.cd(dir);
				// String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
				// InputStream is = c.get(fn);
				// System.out.println(SystemAnalysis.getCurrentTime() + ">SCP request initiated: " + fn);
				// iss = ResourceIOManager.getInputStreamMemoryCached(is);
				// System.out.println(SystemAnalysis.getCurrentTime() + ">SCP request finished: " + fn);
				// // }
				//
				// c.getSession().disconnect();
				//
				// c.disconnect();
				//
				// return iss;
			} else {
				boolean advancedFTP = true;
				
				String detail = url.getDetail();
				detail = detail.split("/", 2)[0] + ftpLocalFolder + detail.split("/", 2)[1] + "/";
				String ur = "ftp://" + ftpUser + ":" + ftpPassword + "@" + detail.substring(0, detail.length() - "/".length());
				
				if (advancedFTP) {
					System.out.print(SystemAnalysis.getCurrentTimeInclSec() + ">" + url);
					MyByteArrayOutputStream bos = new MyByteArrayOutputStream();
					BackgroundTaskStatusProviderSupportingExternalCallImpl status = new CommandLineBackgroundTaskStatusProvider(
							false);
					try {
						MyAdvancedFTP.processFTPdownload(status, ur, bos);
					} catch (Error e) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: ERROR: FTP DOWNLOAD FAILED (" + e.getMessage() + ") // "
								+ ur.substring(ur.indexOf("@")));
						return null;
					} catch (Exception e) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: FTP DOWNLOAD FAILED (" + e.getMessage() + ") // "
								+ ur.substring(ur.indexOf("@")));
						return null;
					}
					MyByteArrayInputStream res = new MyByteArrayInputStream(bos.getBuff(), bos.size());
					System.out.println(".");
					return res;
				} else {
					return new BufferedInputStream(new URL(ur).openStream());
				}
			}
		} else
			return null;
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		
		if (url.toString().contains(",")) {
			url = new IOurl(url.toString().split(",")[0]);
		}
		
		return null;
	}
	
	public static IOurl getLemnaTecFTPurl(String filename, String displayFileName) {
		String host = SystemOptions.getInstance().getString(
				"LT-DB", "Image File Transfer//FTP host", "lemna-db.ipk-gatersleben.de");
		if (filename.contains("/")) {
			host += "/" + filename.substring(0, filename.lastIndexOf("/"));
			filename = filename.substring(filename.lastIndexOf("/") + "/".length());
			host += "/" + filename;
		}
		return new IOurl(PREFIX, host, displayFileName);
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config)
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
	
	@Override
	public Long getStreamLength(IOurl url) throws Exception {
		return null;
	}
}
