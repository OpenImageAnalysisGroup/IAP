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
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class LemnaTecFTPhandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "lemnatec-ftp";
	
	private static boolean useSCP = SystemOptions.getInstance().getBoolean("LemnaTec - Image File Transfer", "Use SCP instead of FTP", false);
	private static String ftpLocalFolder = SystemOptions.getInstance()
			.getString("LemnaTec - Image File Transfer", "FTP directory prefix", "/../../data0/pgftp/");
	private static String ftpUser = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "FTP user", "lemnatec");
	private static String ftpPassword = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "FTP password", "LemnaTec");
	
	private static String scpHost = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP host", "lemna-db.ipk-gatersleben.de");
	private static String scpLocalFolder = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP local storage folder", "/data0/pgftp/");
	private static String scpUser = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP user", "root");
	private static String scpPassword = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP password", "LemnaTec");
	
	public static boolean useCachedCloudDataIfAvailable = SystemOptions.getInstance().getBoolean("LemnaTec - Image File Transfer",
			"Use MongoDB data if available", true);
	
	private static boolean useCachedLocalDataIfAvailable = SystemOptions.getInstance().getBoolean("LemnaTec - Image File Transfer",
			"Use local file access if available", true);
	
	private static String cachedLocalDataDirectory = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer",
			"Local copy or mount point", "/data0/pgftp/");
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		useSCP = SystemOptions.getInstance().getBoolean("LemnaTec - Image File Transfer", "Use SCP instead of FTP", false);
		ftpLocalFolder = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "FTP directory prefix", "/../../data0/pgftp/");
		ftpUser = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "FTP user", "lemnatec");
		ftpPassword = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "FTP password", "LemnaTec");
		
		scpHost = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP host", "lemna-db.ipk-gatersleben.de");
		scpLocalFolder = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP local storage folder", "/data0/pgftp/");
		scpUser = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP user", "root");
		scpPassword = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer", "SCP password", "LemnaTec");
		
		useCachedCloudDataIfAvailable = SystemOptions.getInstance().getBoolean("LemnaTec - Image File Transfer",
				"Use MongoDB data if available", true);
		
		useCachedLocalDataIfAvailable = SystemOptions.getInstance().getBoolean("LemnaTec - Image File Transfer",
				"Use local file access if available", true);
		cachedLocalDataDirectory = SystemOptions.getInstance().getString("LemnaTec - Image File Transfer",
				"Local copy or mount point", "/data0/pgftp/");
		
		if (url.toString().contains(",")) {
			url = new IOurl(url.toString().split(",")[0]);
		}
		if (useCachedCloudDataIfAvailable) {
			try {
				for (MongoDB dc : MongoDB.getMongos()) {
					IOurl urlForCopiedData = dc.getURLforStoredData(url);
					if (urlForCopiedData != null) {
						InputStream is = urlForCopiedData.getInputStream();
						if (is != null) {
							// System.out.println(SystemAnalysis.getCurrentTime() + ">Use cache for " + url);
							return is;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("ERROR: Could not check mongodb for cached input stream data url " + url + ", message: " + e.getMessage());
			}
		}
		if (url.isEqualPrefix(getPrefix())) {
			boolean lokalCache = useCachedLocalDataIfAvailable;
			if (lokalCache) {
				for (String path : cachedLocalDataDirectory.split(";")) {
					String detail = url.getDetail();
					detail = path + detail.split("/", 2)[1];
					String fn = detail;
					File fff = new File(fn);
					if (fff.exists()) {
						IOurl u = FileSystemHandler.getURL(fff);
						MyByteArrayInputStream is = ResourceIOManager.getInputStreamMemoryCached(u);
						if (is != null)
							return is;
					}
				}
			}
			
			if (useSCP) {
				InputStream iss = null;
				// synchronized (PREFIX) {
				ChannelSftp c = getNewChannel(scpUser, scpPassword, scpHost);
				c.getSession().setTimeout(60 * 60 * 1000); // set timeout of 60 minutes
				String detail = url.getDetail();
				detail = scpLocalFolder + detail.split("/", 2)[1];
				String dir = detail.substring(0, detail.lastIndexOf("/"));
				System.out.println(SystemAnalysis.getCurrentTime() + ">SCP change directory: " + dir);
				c.cd(dir);
				String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
				InputStream is = c.get(fn);
				System.out.println(SystemAnalysis.getCurrentTime() + ">SCP request initiated: " + fn);
				iss = ResourceIOManager.getInputStreamMemoryCached(is);
				System.out.println(SystemAnalysis.getCurrentTime() + ">SCP request finished: " + fn);
				// }
				
				c.getSession().disconnect();
				
				c.disconnect();
				
				return iss;
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
		if (useCachedCloudDataIfAvailable) {
			try {
				if (MongoDB.getDefaultCloud() != null) {
					MongoDB dc = MongoDB.getDefaultCloud();
					InputStream urlForCopiedDataStream = dc.getURLforStoredData_PreviewStream(url);
					if (urlForCopiedDataStream != null)
						return urlForCopiedDataStream;
				}
			} catch (Exception e) {
				System.out.println("ERROR: Could not check default cloud for cached input stream data url: " + e.getMessage());
			}
		}
		return super.getPreviewInputStream(url);
	}
	
	private Session session = null;
	private Channel channel = null;
	
	private synchronized ChannelSftp getChannel(String user, String password, String host) throws Exception {
		if (session == null || !session.isConnected()) {
			JSch jsch = new JSch();
			int port = 22;
			session = jsch.getSession(user, host, port);
			UserInfo ui = new MyStoredUserInfo();
			session.setUserInfo(ui);
			session.setPassword(password);
			session.connect();
		}
		
		if (channel == null || !channel.isConnected()) {
			channel = session.openChannel("sftp");
			channel.connect(30);
		}
		
		ChannelSftp c = (ChannelSftp) channel;
		return c;
	}
	
	private ChannelSftp getNewChannel(String user, String password, String host) throws Exception {
		JSch jsch = new JSch();
		int port = 22;
		Session session = jsch.getSession(user, host, port);
		UserInfo ui = new MyStoredUserInfo();
		session.setUserInfo(ui);
		session.setPassword(password);
		session.connect();
		
		Channel channel = session.openChannel("sftp");
		channel.connect(30);
		
		ChannelSftp c = (ChannelSftp) channel;
		return c;
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
