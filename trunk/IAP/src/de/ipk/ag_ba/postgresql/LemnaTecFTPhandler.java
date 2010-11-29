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

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;
import org.graffiti.plugin.io.resources.ResourceIOHandler;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class LemnaTecFTPhandler implements ResourceIOHandler {

	public static final String PREFIX = "lemnatec-ftp";

	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.toString().contains(",")) {
			url = new IOurl(url.toString().split(",")[0]);
		}
		if (url.isEqualPrefix(getPrefix())) {
			boolean useSCP = false;
			if (useSCP) {
				ChannelSftp c = getChannel();
				String detail = url.getDetail();
				detail = "/data0/pgftp/" + detail.split("/", 2)[1];
				c.cd(detail.substring(0, detail.lastIndexOf("/")));
				// Vector<?> o = c.ls(detail.substring(0, detail.lastIndexOf("/")));
				// for (Object i : o)
				// System.out.println(i);
				String fn = detail.substring(detail.lastIndexOf("/") + "/".length());
				InputStream is = c.get(fn);
				return is;
			} else {
				boolean advancedFTP = true;

				String detail = url.getDetail();
				detail = detail.split("/", 2)[0] + "/../../data0/pgftp/" + detail.split("/", 2)[1] + "/";
				String ur = "ftp://lemnatec:LemnaTec@" + detail.substring(0, detail.length() - "/".length());

				if (advancedFTP) {
					MyByteArrayOutputStream bos = new MyByteArrayOutputStream();
					BackgroundTaskStatusProviderSupportingExternalCallImpl status = new CommandLineBackgroundTaskStatusProvider(
										false);
					MyAdvancedFTP.processFTPdownload(status, ur, bos);
					return new MyByteArrayInputStream(bos.getBuff());
				} else {
					return new BufferedInputStream(new URL(ur).openStream());
				}
			}
		} else
			return null;
	}

	private Session session = null;
	private final Channel channel = null;

	private ChannelSftp getChannel() throws Exception {
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

		// if (channel == null || !channel.isConnected()) {
		// channel = session.openChannel("sftp");
		// channel.connect(30);
		// }

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
}
