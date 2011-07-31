/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.services;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.ObjectRef;
import org.OpenFileDialogService;
import org.ReleaseInfo;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.FileDownloadStatusInformationProvider;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class GUIhelper {
	
	public static Component getHelpTextComponent(String plainText, String title,
			String helpTopic) {
		JPanel result = new JPanel();
		plainText = plainText.replaceAll("<br>", "\n");
		plainText = plainText.replaceAll("<html>", "");
		plainText = plainText.replaceAll("<small>", "");
		FolderPanel fp = new FolderPanel(title, false, false, false,
				JLabelJavaHelpLink.getHelpActionListener(helpTopic));
		JTextArea helpText = new JTextArea();
		helpText.setLineWrap(true);
		helpText.setWrapStyleWord(true);
		helpText.setText(plainText);
		helpText.setEditable(false);
		fp.addGuiComponentRow(new JLabel(""), helpText, false);
		fp.layoutRows();
		fp.setFrameColor(Color.LIGHT_GRAY, Color.WHITE, 1, 5);
		
		double border = 2;
		double topBorder = 12;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ topBorder, TableLayoutConstants.PREFERRED, border } }; // Rows
		result.setLayout(new TableLayout(size));
		result.add(fp, "1,1");
		return result;
	}
	
	// returns index of pressed button or -1 if no button was clicked but the
	// window is
	// closed
	public static int showMessageDialog(Object guiComponentOrText, String title,
			String[] buttons) {
		
		JComponent content = null;
		
		if (guiComponentOrText instanceof String) {
			content = new JLabel((String) guiComponentOrText);
			((JLabel) content).setOpaque(false);
			((JLabel) content).setBackground(null);
		} else
			content = (JComponent) guiComponentOrText;
		
		MyDialog md = new MyDialog(MainFrame.getInstance(), title, buttons,
				content, 5);
		md.setBounds(MainFrame.getRelativeCenterPosition(md));
		md.setVisible(true);
		return md.getReturnValue();
	}
	
	public static JComponent getWebsiteButton(String title, final String url,
			final String opt_local_folder, final String optIntroText,
			final String optIntroDialogTitle) {
		JButton res = new JButton(title);
		res.setToolTipText("<html>Click button to open URL:<br><code><b>" + url);
		res.addActionListener(getDialogAction(url, opt_local_folder,
				optIntroText, optIntroDialogTitle));
		return res;
	}
	
	private static ActionListener getDialogAction(final String url,
			final String opt_local_folder, final String optIntroText,
			final String optIntroDialogTitle) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (optIntroText != null) {
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								// empty
							}
							AttributeHelper.showInBrowser(url);
							if (opt_local_folder != null
									&& opt_local_folder.length() > 0)
								AttributeHelper.showInBrowser(opt_local_folder);
						}
					};
					t.start();
					showMessageDialog(optIntroText, optIntroDialogTitle,
							new String[] { "OK" });
				} else {
					AttributeHelper.showInBrowser(url);
					if (opt_local_folder != null && opt_local_folder.length() > 0)
						AttributeHelper.showInBrowser(opt_local_folder);
				}
			}
		};
	}
	
	public static JComponent getWebsiteDownloadButton(final String title,
			final String optUrlManualDownloadWebsite,
			final String target_dir_null_ask_user, final String optIntroText,
			final String[] downloadURLs, final String optIntroDialogTitle,
			final FileDownloadStatusInformationProvider statusProvider) {
		return getWebsiteDownloadButton(title, optUrlManualDownloadWebsite,
				target_dir_null_ask_user, optIntroText, downloadURLs,
				optIntroDialogTitle, statusProvider, null);
	}
	
	public static JComponent getWebsiteDownloadButton(final String title,
			final String optUrlManualDownloadWebsite,
			final String target_dir_null_ask_user, final String optIntroText,
			final String[] downloadURLs, final String optIntroDialogTitle,
			final FileDownloadStatusInformationProvider statusProvider,
			final Runnable optFinishSwingTask) {
		
		final JButton res = new JMButton("Download/Update");
		res
				.setToolTipText("<html>Click button to start automatic download<br><code><b>Check License/Disclaimers first!");
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String opt_local_folder;
				if (target_dir_null_ask_user == null) {
					File file = OpenFileDialogService
							.getDirectoryFromUser("Select folder");
					if (file == null)
						return;
					else
						opt_local_folder = file.getAbsolutePath();
				} else {
					opt_local_folder = target_dir_null_ask_user;
				}
				
				res.setEnabled(false);
				res.setText("Downloading");
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
						"Please wait...", "Downloading files...");
				BackgroundTaskHelper.issueSimpleTask(title, "Please wait...",
						new Runnable() {
							public void run() {
								boolean allOK = true;
								for (String downloadURL : downloadURLs) {
									allOK = performDownload(downloadURL,
											opt_local_folder, status);
									if (status.wantsToStop()) {
										break;
									}
								}
								if (status.wantsToStop()) {
									allOK = true;
									try {
										status.setCurrentStatusText1("Cancel...");
										for (String downloadURL : downloadURLs) {
											String fileName = downloadURL.substring(downloadURL.lastIndexOf("/") + 1);
											if (downloadURL.contains("|"))
												fileName = downloadURL.substring(downloadURL.lastIndexOf("|") + 1);
											String targetFileName = ReleaseInfo.getAppFolderWithFinalSep()
													+ fileName;
											if (new File(targetFileName).exists()) {
												new File(targetFileName).delete();
												status.setCurrentStatusText2("Delete "
														+ targetFileName);
											}
										}
									} catch (Exception e) {
										//
									}
								}
								
								if (!allOK) {
									res.setEnabled(true);
									res
											.setText("<html><small>Automatic download failure<br>Click here for manual download");
									res
											.removeActionListener(res.getActionListeners()[0]);
									res.addActionListener(getDialogAction(
											optUrlManualDownloadWebsite, opt_local_folder,
											optIntroText, optIntroDialogTitle));
									res.requestFocus();
									final JDialog jd = (JDialog) ErrorMsg
											.findParentComponent(res, JDialog.class);
									if (jd != null) {
										SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												jd.pack();
											}
										});
									}
								} else {
									if (status.wantsToStop()) {
										res.setText("Canceled");
										status.pleaseContinueRun();
										status.setCurrentStatusText2("Canceled!");
									} else {
										res.setText("Downloaded");
									}
									res.setEnabled(false);
									if (statusProvider != null)
										statusProvider.finishedNewDownload();
									if (optFinishSwingTask != null)
										SwingUtilities.invokeLater(optFinishSwingTask);
								}
							}
						}, null, status);
			}
		});
		return res;
	}
	
	public static boolean performDownload(String downloadURL,
			String opt_local_folder,
			BackgroundTaskStatusProviderSupportingExternalCallImpl status) {
		return performDownload(downloadURL, opt_local_folder, status, null);
	}
	
	public static ArrayList<String> performDirectoryListing(String downloadURL,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (status == null)
			status = new BackgroundTaskConsoleLogger(
					"downloading: " + downloadURL, "please wait", false);
		String server, remote;
		
		runIdx++;
		final int thisRun = runIdx;
		
		server = downloadURL.substring("ftp://".length());
		remote = server.substring(server.indexOf("/") + "/".length());
		server = server.substring(0, server.indexOf("/"));
		
		ArrayList<String> result = null;
		
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
		
		try {
			result = listFiles(
					status, downloadURL, server, remote, ftp);
			status.setCurrentStatusText2("Finished: " + downloadURL);
			
			BackgroundTaskHelper.executeLaterOnSwingTask(10000, new Runnable() {
				public void run() {
					try {
						synchronized (GUIhelper.class) {
							if (runIdx == thisRun) {
								System.out.println("Disconnect FTP connection");
								ftp.disconnect();
							}
						}
					} catch (Exception err) {
						ErrorMsg.addErrorMessage(err);
					}
				}
			});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	public static boolean performDownload(String downloadURL,
			String opt_local_folder,
			BackgroundTaskStatusProviderSupportingExternalCallImpl status,
			String targetfilename) {
		if (status == null)
			status = new BackgroundTaskConsoleLogger(
					"downloading: " + downloadURL, "target folder: "
							+ opt_local_folder, false);
		String fileName = targetfilename;
		if (fileName == null)
			fileName = downloadURL.substring(downloadURL.lastIndexOf("/") + 1);
		if (downloadURL.contains("|")) {
			fileName = downloadURL.substring(downloadURL.lastIndexOf("|") + 1);
			downloadURL = downloadURL.substring(0, downloadURL.indexOf("|"));
		}
		String target = opt_local_folder;
		String targetFileName = target + fileName;
		boolean downloadOK = true;
		
		OutputStream out = null;
		URLConnection conn = null;
		InputStream in = null;
		long numWritten = 0;
		boolean ftp = false;
		ObjectRef lastStatus = new ObjectRef();
		try {
			boolean downloadNeeded = true;
			if (downloadURL.indexOf("ftp://") == 0) {
				ftp = true;
				try {
					if (!processFTPdownload(status, downloadURL, targetFileName,
							lastStatus))
						downloadOK = false;
					else
						downloadNeeded = false;
				} catch (Exception err) {
					ftp = false;
					System.out.println("ERROR: FTP download failed: " + err.getMessage());
					System.out.println("INFO: TRYING DEFAULT URL DOWNLOAD METHOD");
				}
			};
			if (downloadNeeded) {
				URL url = new URL(downloadURL);
				conn = url.openConnection();
				int contentLength = conn.getContentLength();
				in = conn.getInputStream();
				byte[] buffer = new byte[512 * 1024];
				int numRead;
				out = new BufferedOutputStream(new FileOutputStream(targetFileName));
				status.setCurrentStatusText1(downloadURL);
				status.setCurrentStatusText2("Received " + (numWritten / 1024) + " KB");
				while ((numRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, numRead);
					numWritten += numRead;
					status.setCurrentStatusText1(downloadURL);
					status.setCurrentStatusText2("Received " + (numWritten / 1024) + " KB");
					if (contentLength > 0) {
						status.setCurrentStatusValueFine(100d * numWritten / contentLength);
					}
					if (status.wantsToStop()) {
						break;
					}
				}
				downloadOK = true;
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			downloadOK = false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (ftp) {
					if (lastStatus != null && lastStatus.getObject() != null
							&& (lastStatus.getObject() instanceof String)) {
						status.setCurrentStatusText1((String) lastStatus.getObject());
					} else
						status.setCurrentStatusText1(status
								.getCurrentStatusMessage2());
					status.setCurrentStatusText2("Finished: " + fileName);
				} else {
					status.setCurrentStatusText1("Received " + (numWritten / 1024)
							+ " KB");
					status.setCurrentStatusText2("Finished: " + fileName);
				}
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				downloadOK = false;
			}
		}
		return downloadOK;
	}
	
	private static final HashMap<String, ThreadSafeOptions> host2ftp = new HashMap<String, ThreadSafeOptions>();
	private static int runIdx = 0;
	
	private static boolean processFTPdownload(
			final BackgroundTaskStatusProviderSupportingExternalCallImpl status,
			String downloadURL, String targetFileName, ObjectRef lastStatus) throws InterruptedException {
		runIdx++;
		final int thisRun = runIdx;
		status.setCurrentStatusText1(downloadURL);
		status.setCurrentStatusText2("FTP DOWNLOAD...");
		String server, remote;
		
		server = downloadURL.substring("ftp://".length());
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
		
		try {
			boolean wait = false;
			
			BackgroundTaskHelper.lockAquire(server, 1);
			if (wait) {
				while (wait) {
					status.setCurrentStatusText1("Waiting for shared FTP connection");
					status.setCurrentStatusText2("Server: " + server);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					synchronized (tso) {
						wait = tso.getBval(1, false);
					}
				}
			}
			status.setCurrentStatusValue(-1);
			status.setCurrentStatusText1(downloadURL);
			status.setCurrentStatusText2("FTP DOWNLOAD...");
			res = processDownload(status, downloadURL, targetFileName, lastStatus, thisRun, server, remote, ftp);
		} finally {
			BackgroundTaskHelper.lockRelease(server);
		}
		return res;
	}
	
	private static boolean processDownload(final BackgroundTaskStatusProviderSupportingExternalCallImpl status, String downloadURL,
			String targetFileName, ObjectRef lastStatus, final int thisRun, String server, String remote, final FTPClient ftp) {
		String username;
		String password;
		String local;
		username = "anonymous@" + server;
		password = "anonymous";
		local = targetFileName;
		
		final ObjectRef myoutputstream = new ObjectRef();
		
		ftp.addProtocolCommandListener(new ProtocolCommandListener() {
			public void protocolCommandSent(ProtocolCommandEvent arg0) {
				// System.out.print("out: " + arg0.getMessage());
				status.setCurrentStatusText1("Command: " + arg0.getMessage());
			}
			
			public void protocolReplyReceived(ProtocolCommandEvent arg0) {
				// System.out.print("in : " + arg0.getMessage());
				status.setCurrentStatusText2("Message: " + arg0.getMessage());
				if (myoutputstream.getObject() != null) {
					String msg = arg0.getMessage();
					if (msg.indexOf("Opening BINARY mode") >= 0) {
						if (msg.indexOf("(") > 0) {
							msg = msg.substring(msg.indexOf("(") + "(".length());
							if (msg.indexOf(" ") > 0) {
								msg = msg.substring(0, msg.indexOf(" "));
								try {
									long max = Long.parseLong(msg);
									MyOutputStream os = (MyOutputStream) myoutputstream
											.getObject();
									os.setMaxBytes(max);
								} catch (Exception e) {
									System.out.println("Could not determine file length for detailed progress information");
								}
							}
						}
					}
				}
			}
		});
		
		System.out.println("FTP DOWNLOAD: " + downloadURL);
		
		try {
			if (ftp.isConnected()) {
				status.setCurrentStatusText2("Using open FTP connection");
				System.out.println("Reusing open FTP connection");
			} else {
				ftp.connect(server);
				int reply = ftp.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftp.disconnect();
					status.setCurrentStatusText1("Can't connect to FTP server");
					status.setCurrentStatusText2("ERROR");
					return false;
				}
				if (!ftp.login("anonymous", "anonymous")) {
					if (!ftp.login(username, password)) {
						ftp.disconnect();
						status.setCurrentStatusText1("Can't login to FTP server");
						status.setCurrentStatusText2("ERROR");
						return false;
					}
				}
				status.setCurrentStatusText1("Set Binary Transfer Mode");
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				status.setCurrentStatusText2("Activate Passive Transfer Mode");
				ftp.enterLocalPassiveMode();
			}
			status.setCurrentStatusText1("Start download...");
			status.setCurrentStatusText2("Please Wait.");
			
			// ftp.listFiles(pathname);
			
			OutputStream output = new MyOutputStream(lastStatus, status,
					new FileOutputStream(local));
			myoutputstream.setObject(output);
			ftp.setRemoteVerificationEnabled(false);
			long tA = System.currentTimeMillis();
			boolean result = ftp.retrieveFile(remote, output);
			output.close();
			long tB = System.currentTimeMillis();
			if (!result) {
				new File(local).delete();
				MainFrame.showMessage("Can't download " + downloadURL + ". File not available.", MessageType.INFO);
			} else {
				File f = new File(local);
				System.out.println("Download completed (" + f.getAbsolutePath() + ", " +
						(f.length() / 1024) + " KB, " + (int) ((f.length() / 1024d / (tB - tA) * 1000d)) + " KB/s).");
			}
			BackgroundTaskHelper.executeLaterOnSwingTask(10000, new Runnable() {
				public void run() {
					try {
						synchronized (GUIhelper.class) {
							if (runIdx == thisRun) {
								System.out.println("Disconnect FTP connection");
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
			System.out.println("ERROR: FTP DOWNLOAD ERROR: " + err.getMessage());
			if (ftp != null && ftp.isConnected()) {
				try {
					System.out.println("Disconnect FTP connection (following error condition)");
					ftp.disconnect();
				} catch (Exception err2) {
					ErrorMsg.addErrorMessage(err2);
				}
			}
			return false;
		}
	}
	
	private static ArrayList<String> listFiles(final BackgroundTaskStatusProviderSupportingExternalCall status,
			String downloadURL,
			String server, String remotePath, final FTPClient ftp) {
		String username;
		String password;
		username = "anonymous@" + server;
		password = "anonymous";
		
		final ObjectRef myoutputstream = new ObjectRef();
		
		ftp.addProtocolCommandListener(new ProtocolCommandListener() {
			public void protocolCommandSent(ProtocolCommandEvent arg0) {
				status.setCurrentStatusText1("Command: " + arg0.getMessage());
			}
			
			public void protocolReplyReceived(ProtocolCommandEvent arg0) {
				status.setCurrentStatusText2("Message: " + arg0.getMessage());
				if (myoutputstream.getObject() != null) {
					String msg = arg0.getMessage();
					if (msg.indexOf("Opening BINARY mode") >= 0) {
						if (msg.indexOf("(") > 0) {
							msg = msg.substring(msg.indexOf("(") + "(".length());
							if (msg.indexOf(" ") > 0) {
								msg = msg.substring(0, msg.indexOf(" "));
								try {
									long max = Long.parseLong(msg);
									MyOutputStream os = (MyOutputStream) myoutputstream
											.getObject();
									os.setMaxBytes(max);
								} catch (Exception e) {
									System.out.println("Could not determine file length for detailed progress information");
								}
							}
						}
					}
				}
			}
		});
		
		System.out.println("FTP LIST DIRECTORY: " + downloadURL);
		
		try {
			if (ftp.isConnected()) {
				status.setCurrentStatusText2("Using open FTP connection");
				System.out.println("Reusing open FTP connection");
			} else {
				ftp.connect(server);
				int reply = ftp.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftp.disconnect();
					status.setCurrentStatusText1("Can't connect to FTP server");
					status.setCurrentStatusText2("ERROR");
					return new ArrayList<String>();
				}
				if (!ftp.login("anonymous", "anonymous")) {
					if (!ftp.login(username, password)) {
						ftp.disconnect();
						status.setCurrentStatusText1("Can't login to FTP server");
						status.setCurrentStatusText2("ERROR");
						return new ArrayList<String>();
					}
				}
				status.setCurrentStatusText1("Set Binary Transfer Mode");
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				status.setCurrentStatusText2("Activate Passive Transfer Mode");
				ftp.enterLocalPassiveMode();
			}
			status.setCurrentStatusText1("Start download...");
			status.setCurrentStatusText2("Please Wait.");
			
			ftp.setRemoteVerificationEnabled(false);
			
			FTPFile[] res = ftp.listFiles(remotePath);
			
			ArrayList<String> result = new ArrayList<String>();
			
			for (FTPFile r : res) {
				result.add(r.getName());
			}
			
			return result;
		} catch (Exception err) {
			ErrorMsg.addErrorMessage(err);
			if (ftp != null && ftp.isConnected()) {
				try {
					System.out.println("Disconnect FTP connection (following error condition)");
					ftp.disconnect();
				} catch (Exception err2) {
					ErrorMsg.addErrorMessage(err2);
				}
			}
			return new ArrayList<String>();
		}
	}
}

class MyDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private String title;
	private String[] buttons;
	private int returnValue;
	private JComponent mainView;
	private double border;
	
	public MyDialog(Frame instance, String title, String[] buttons,
			JComponent mainView, int border) {
		super(instance);
		this.title = title;
		this.buttons = buttons;
		this.mainView = mainView;
		this.border = 5;
		setReturnValue(-1);
		myInit();
	}
	
	private void myInit() {
		super.dialogInit();
		setTitle(title);
		
		double[][] size = { { border, TableLayoutConstants.PREFERRED, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, border } }; // Rows
		setLayout(new TableLayout(size));
		
		JButton[] commandButtons = new JButton[buttons.length];
		ArrayList<JComponent> buttonArr = new ArrayList<JComponent>();
		for (int i = 0; i < buttons.length; i++) {
			JButton button = new JButton(buttons[i]);
			final int fi = i;
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					returnValue = fi;
					dispose();
				}
			});
			commandButtons[i] = button;
			buttonArr.add(button);
		}
		// final JScrollPane sp = new JScrollPane(mainView);
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// sp.getVerticalScrollBar().setValue(0);
		// }});
		add(TableLayout.getSplitVertical(mainView, TableLayout.getMultiSplit(
				buttonArr, TableLayoutConstants.PREFERRED, 5, 0, 5, 3),
				TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED), "1,1");
		validate();
		
		setResizable(false);
		// setSize(getPreferredSize());
		setModal(true);
		getRootPane().setDefaultButton(commandButtons[0]);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		final String ESC_ACTION_KEY = "ESC_ACTION_KEY";
		getRootPane().getActionMap().put(ESC_ACTION_KEY, new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESC_ACTION_KEY);
		pack();
	}
	
	public void setReturnValue(int returnValue) {
		this.returnValue = returnValue;
	}
	
	public int getReturnValue() {
		return returnValue;
	}
}
