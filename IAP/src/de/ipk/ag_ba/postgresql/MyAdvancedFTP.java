/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 21, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.postgresql;

import java.io.OutputStream;
import java.util.HashMap;

import org.ErrorMsg;
import org.ObjectRef;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class MyAdvancedFTP {
	private static final HashMap<String, ThreadSafeOptions> host2ftp = new HashMap<String, ThreadSafeOptions>();
	private static int runIdx = 0;
	
	public static boolean processFTPdownload(final BackgroundTaskStatusProviderSupportingExternalCallImpl status,
						String downloadURL, MyByteArrayOutputStream target) {
		runIdx++;
		final int thisRun = runIdx;
		status.setCurrentStatusText1(downloadURL);
		status.setCurrentStatusText2("FTP DOWNLOAD...");
		String server, remote;
		
		server = downloadURL.substring("ftp://".length());
		if (server.contains("@"))
			server = server.substring(server.indexOf("@") + "@".length());
		remote = server.substring(server.indexOf("/") + "/".length());
		server = server.substring(0, server.indexOf("/"));
		
		final FTPClient ftp;
		
		ThreadSafeOptions tso;
		synchronized (host2ftp) {
			if (!host2ftp.containsKey(server)) {
				ThreadSafeOptions ttt = new ThreadSafeOptions();
				ttt.setParam(0, new FTPClient());
				host2ftp.put(server, ttt);
			}
			tso = host2ftp.get(server);
			ftp = (FTPClient) tso.getParam(0, null);
		}
		status.setCurrentStatusValue(0);
		boolean res;
		
		status.setCurrentStatusValue(-1);
		status.setCurrentStatusText1(downloadURL);
		status.setCurrentStatusText2("FTP DOWNLOAD...");
		res = processDownload(status, downloadURL, target, thisRun, server, remote, ftp);
		return res;
	}
	
	private synchronized static boolean processDownload(final BackgroundTaskStatusProviderSupportingExternalCallImpl status,
						String downloadURL, MyByteArrayOutputStream target, final int thisRun, final String server, String remote,
						final FTPClient ftp) {
		String username;
		String password;
		username = "anonymous@" + server;
		password = "anonymous";
		if (downloadURL.contains("@")) {
			String s = downloadURL.substring("ftp://".length());
			s = s.substring(0, s.indexOf("@"));
			username = s.split(":")[0];
			password = s.split(":")[1];
			downloadURL = "ftp://" + downloadURL.substring(downloadURL.indexOf("@") + "@".length());
		}
		
		final ObjectRef myoutputstream = new ObjectRef();
		BackgroundTaskHelper.lockAquire(server, 1);
		try {
			status.setCurrentStatusText1("Waiting for shared FTP connection");
			status.setCurrentStatusText2("Server: " + server);
			status.setCurrentStatusValue(-1);
			
			return performDownload(status, downloadURL, target, thisRun, server, remote, ftp, username, password, myoutputstream);
			
		} finally {
			BackgroundTaskHelper.lockRelease(server);
		}
	}
	
	private static boolean performDownload(final BackgroundTaskStatusProviderSupportingExternalCallImpl status, String downloadURL,
			MyByteArrayOutputStream target, final int thisRun, final String server, String remote, final FTPClient ftp, String username, String password,
			final ObjectRef myoutputstream) {
		try {
			if (ftp.isConnected()) {
				status.setCurrentStatusText2("Using open FTP connection");
			} else {
				System.out.println("Connecting to FTP server: " + server);
				ftp.connect(server);
				int reply = ftp.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					System.out.println("Non-Positivy Completion Result -> Disconnecting from FTP server: " + server);
					ftp.disconnect();
					status.setCurrentStatusText1("Can't connect to FTP server");
					status.setCurrentStatusText2("ERROR");
					return false;
				}
				// if (!ftp.login("anonymous", "anonymous")) {
				if (!ftp.login(username, password)) {
					System.out.println("Login not successful -> Disconnecting from FTP server: " + server);
					ftp.disconnect();
					status.setCurrentStatusText1("Can't login to FTP server");
					status.setCurrentStatusText2("ERROR");
					return false;
				}
				// }
				status.setCurrentStatusText1("Set Binary Transfer Mode");
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				status.setCurrentStatusText2("Activate Passive Transfer Mode");
				ftp.enterLocalPassiveMode();
			}
			status.setCurrentStatusText1("Download started..");
			status.setCurrentStatusText2("Please Wait.");
			
			OutputStream output = target;
			myoutputstream.setObject(output);
			ftp.setRemoteVerificationEnabled(false);
			boolean result = ftp.retrieveFile(remote, output);
			output.close();
			if (!result) {
				target.setBuf(null);
				MainFrame.showMessage("Can't download " + downloadURL + ". File not available.", MessageType.INFO);
			}
			boolean autoClose = true;
			if (autoClose)
				BackgroundTaskHelper.executeLaterOnSwingTask(5*6*10000, new Runnable() {
					public void run() {
						try {
							synchronized (GUIhelper.class) {
								if (runIdx == thisRun) {
									System.out.println("Disconnecting from FTP server: " + server);
									ftp.disconnect();
								}
							}
						} catch (Exception err) {
							ErrorMsg.addErrorMessage(err);
						}
					}
				});
			return result;
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
			if (ftp != null && ftp.isConnected()) {
				try {
					System.out.println("Disconnect FTP connection");
					ftp.disconnect();
				} catch (Exception err2) {
					ErrorMsg.addErrorMessage(err2);
				}
			}
			return false;
		}
	}
}
