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
	
	public String getPrefix() {
		return PREFIX;
	}
	
	public static boolean useCachedCloudDataIfAvailable = false;
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.toString().contains(",")) {
			url = new IOurl(url.toString().split(",")[0]);
		}
		if (useCachedCloudDataIfAvailable) {
			try {
				if (MongoDB.getDefaultCloud() != null) {
					MongoDB dc = MongoDB.getDefaultCloud();
					IOurl urlForCopiedData = dc.getURLforStoredData(url);
					if (urlForCopiedData != null) {
						InputStream is = urlForCopiedData.getInputStream();
						if (is != null)
							return is;
					}
				}
			} catch (Exception e) {
				System.out.println("ERROR: Could not check default cloud for cached input stream data url: " + e.getMessage());
			}
		}
		if (url.isEqualPrefix(getPrefix())) {
			boolean lokalCache = true;
			if (lokalCache) {
				for (String path : new String[] { "/Volumes/3TB_1/pgftp/", "I:/", "J:/", "/data0/pgftp/" }) {
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
			boolean useSCP = true;
			if (useSCP) {
				InputStream iss = null;
				synchronized (PREFIX) {
					ChannelSftp c = getChannel();
					String detail = url.getDetail();
					detail = "/data0/pgftp/" + detail.split("/", 2)[1];
					c.cd(detail.substring(0, detail.lastIndexOf("/")));
					String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
					InputStream is = c.get(fn);
					iss = ResourceIOManager.getInputStreamMemoryCached(is);
				}
				return iss;
			} else {
				boolean advancedFTP = true;
				
				String detail = url.getDetail();
				detail = detail.split("/", 2)[0] + "/../../data0/pgftp/" + detail.split("/", 2)[1] + "/";
				String ur = "ftp://lemnatec:LemnaTec@" + detail.substring(0, detail.length() - "/".length());
				
				if (advancedFTP) {
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
	
	private synchronized ChannelSftp getChannel() throws Exception {
		if (session == null || !session.isConnected()) {
			JSch jsch = new JSch();
			String host = "lemna-db.ipk-gatersleben.de";
			String user = "root";
			int port = 22;
			session = jsch.getSession(user, host, port);
			UserInfo ui = new MyStoredUserInfo();
			session.setUserInfo(ui);
			session.setPassword("LemnaTec");
			session.connect();
		}
		
		if (channel == null || !channel.isConnected()) {
			channel = session.openChannel("sftp");
			channel.connect(30);
		}
		
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
