/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 11, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class LTftpHandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "lt-ftp";
	
	private static boolean useMongoDB = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use MongoDB data if available", true);
	
	private static boolean useSCP = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use SCP instead of FTP", false);
	// private static String ftpHost = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP host", "lemna-db.ipk-gatersleben.de");
	private static String ftpLocalFolder = SystemOptions.getInstance()
			.getString("LT-DB", "Image File Transfer//FTP directory prefix", "/../../data0/pgftp/");
	private static String ftpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP user", "");
	private static String ftpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP password", "");
	
	private static String scpHost = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP host", "lemna-db.ipk-gatersleben.de");
	private static String scpLocalFolder = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP local storage folder", "/data0/pgftp/");
	private static String scpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP user", "root");
	private static String scpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP password", "");
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		useMongoDB = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use MongoDB data if available", true);
		
		useSCP = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use SCP instead of FTP", false);
		ftpLocalFolder = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP directory prefix", "/../../data0/pgftp/");
		ftpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP user", "");
		ftpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//FTP password", "");
		
		scpHost = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP host", "lemna-db.ipk-gatersleben.de");
		scpLocalFolder = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP local storage folder", "/data0/pgftp/");
		scpUser = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP user", "root");
		scpPassword = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//SCP password", "");
		
		boolean useLocalCopyIfAvail = SystemOptions.getInstance().getBoolean("LT-DB", "Image File Transfer//Use local file access if available", false);
		String localCopyPathList = SystemOptions.getInstance().getString("LT-DB", "Image File Transfer//Local copy or mount point", "Z:\\");
		
		if (url.toString().contains(",")) {
			url = new IOurl(url.toString().split(",")[0]);
		}
		
		try {
			if (useLocalCopyIfAvail) {
				for (String localCopyPath : localCopyPathList.split(";")) {
					if (localCopyPath.trim().isEmpty())
						continue;
					String detail = url.getDetail();
					detail = detail.split("/", 2)[1];
					String dir = detail.substring(0, detail.lastIndexOf("/"));
					if (!localCopyPath.endsWith(File.separator) && !localCopyPath.endsWith("/"))
						localCopyPath = localCopyPath + File.separator;
					String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
					File f = new File(localCopyPath + dir + File.separator + fn);
					if (f.exists())
						return new FileInputStream(f);
				}
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not access local file copy for LT file transfer. Error: " + e.getMessage());
		}
		try {
			if (useMongoDB) {
				for (MongoDB m : MongoDB.getMongos()) {
					IOurl mu = m.getURLforStoredData(url);
					if (mu != null)
						return mu.getInputStream();
				}
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not access MongoDB for cached LT file transfer. Error: " + e.getMessage());
		}
		if (url.isEqualPrefix(getPrefix())) {
			if (useSCP) {
				String detail = url.getDetail();
				detail = scpLocalFolder + detail.split("/", 2)[1];
				String dir = detail.substring(0, detail.lastIndexOf("/"));
				String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
				VfsFileObject fo = VfsFileObjectUtil.createVfsFileObject(VfsFileProtocol.SFTP, scpHost, dir + "/" + fn, scpUser, scpPassword);
				return ResourceIOManager.getInputStreamMemoryCached(fo.getInputStream());
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
		
		try {
			if (useMongoDB) {
				for (MongoDB m : MongoDB.getMongos()) {
					InputStream muis = m.getURLforStoredData_PreviewStream(url);
					if (muis != null)
						return muis;
				}
			}
		} catch (Exception e) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Could not access MongoDB for cached LT file transfer. Error: " + e.getMessage());
		}
		
		return null;
	}
	
	public static IOurl getImagingSystemFTPurl(String filename, String displayFileName) {
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
		throw new Exception("Imaging System server FTP Output is not supported!");
	}
	
	public static IOurl getURL(File file) {
		return new IOurl(PREFIX, file.getParent() + "/", file.getName());
	}
	
	public static boolean isImagingSystemFtpUrl(IOurl fileName) {
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
