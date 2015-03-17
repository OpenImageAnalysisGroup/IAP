package de.ipk.ag_ba.gui.util;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.HttpBasicAuth;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import util.Screenshot;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

public class IAPmail {
	
	private static final String FILE_NAME_MAIL_SERVER_CONFIG = ReleaseInfo.getAppSubdirFolderWithFinalSep("watch") + "mail-server.txt";
	
	// http://www.javapractices.com/topic/TopicAction.do?Id=144
	public void sendEmail(
			String aToEmailAddr, String aSubject,
			String aBody, final String optImageSource1, final String fileName1, final String contentType1,
			final String optImageSource2, final String fileName2, final String contentType2,
			final ExperimentHeaderInterface optEHI
			) {
		Session session = Session.getDefaultInstance(fMailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		try {
			// the "from" address may be set in code, or set in the
			// config file under "mail.from" ; here, the latter style is used
			// message.setFrom( new InternetAddress(aFromEmailAddr) );
			for (String to : aToEmailAddr.split(":"))
				message.addRecipient(
						Message.RecipientType.TO, new InternetAddress(to)
						);
			message.setSubject(aSubject);
			
			Multipart mp = new MimeMultipart();
			if (optImageSource1 != null && fileName1 != null && contentType1 != null && !optImageSource1.isEmpty() && !fileName1.isEmpty()
					&& !contentType1.isEmpty())
				addImageOrWebCamScreenshotToMail(aBody, optImageSource1, fileName1, message, mp, contentType1, false);
			if (optImageSource2 != null && fileName2 != null && contentType2 != null && !optImageSource2.isEmpty() && !fileName2.isEmpty()
					&& !contentType2.isEmpty())
				addImageOrWebCamScreenshotToMail(aBody, optImageSource2, fileName2, message, mp, contentType2, false);
			try {
				boolean takeScreenShot = SystemOptions.getInstance().getBoolean("Watch-Service", "Include Screenshot in e-Mail", true);
				if (takeScreenShot) {
					if (!GraphicsEnvironment.isHeadless()) {
						createScreenshotAndAttachToMail(mp);
					}
				}
			} catch (Exception e) {
				System.err.println("Could not create desktop screenshot!");
				e.printStackTrace();
			}
			String errMsg = "";
			StringBuilder latestNumericData = new StringBuilder();
			if (optEHI != null) {
				try {
					ExperimentInterface e = new ExperimentReference(optEHI).getData(((BackgroundTaskStatusProviderSupportingExternalCall) null));
					long newestSnapshotTime = -1;
					ArrayList<SampleInterface> newestSamples = new ArrayList<SampleInterface>();
					for (SubstanceInterface s : e) {
						for (ConditionInterface ci : s) {
							for (SampleInterface si : ci) {
								if (si.getSampleFineTimeOrRowId() > newestSnapshotTime) {
									newestSamples.clear();
									newestSamples.add(si);
									newestSnapshotTime = si.getSampleFineTimeOrRowId();
								} else {
									if (si.getSampleFineTimeOrRowId() == newestSnapshotTime) {
										newestSamples.add(si);
									}
								}
							}
						}
					}
					if (newestSamples.size() > 0) {
						latestNumericData.append("Most recent measurements (" + SystemAnalysis.getCurrentTime(newestSnapshotTime) + "):\n\n");
						for (SampleInterface ns : newestSamples) {
							int imgCnt = 0;
							for (NumericMeasurementInterface nmi : ns) {
								if (nmi instanceof BinaryMeasurement) {
									if (imgCnt == 0) {
										BinaryMeasurement bm = (BinaryMeasurement) nmi;
										String attachmentName = StringManipulationTools.getFileSystemName(
												SystemAnalysis.getCurrentTime(newestSnapshotTime)
														+ "__"
														+ nmi.getParentSample().getParentCondition().getParentSubstance().getName()
														+ "__"
														+ bm.getURL().getFileName());
										int size = addImageOrWebCamScreenshotToMail(aBody, bm.getURL().toString(),
												attachmentName, message, mp, "image/jpeg", true);
										int sizeKB = size / 1024;
										latestNumericData
												.append("Image of "
														+ nmi.getQualityAnnotation()
														+ ": "
														+ nmi.getParentSample().getParentCondition().getParentSubstance().getName()
														+ ", input size"
														+ " = "
														+ sizeKB
														+ " KB, to save e-mail space the image has been resized (if larger than 640x480) and is included as attachment, filename = "
														+ attachmentName + "\n");
									}
									imgCnt++;
								} else {
									latestNumericData.append("Numeric value for "
											+ nmi.getQualityAnnotation()
											+ ": "
											+ nmi.getParentSample().getParentCondition().getParentSubstance().getName()
											+ " = " + nmi.getValue() + " " + nmi.getUnit() + "\n");
								}
							}
						}
					}
				} catch (Exception e) {
					errMsg = "Could not retrieve or attach experiment images. Error: " + e;
				}
			}
			
			MimeBodyPart txt = new MimeBodyPart();
			txt.setText("\n" + aBody + "\n" + errMsg + "\n\n" + latestNumericData);
			mp.addBodyPart(txt);
			message.setContent(mp);
			
			Transport.send(message);
		} catch (MessagingException ex) {
			System.err.println("Cannot send email. " + ex);
		}
	}
	
	private int addImageOrWebCamScreenshotToMail(
			String aBody,
			final String optImageSource,
			final String fileName,
			MimeMessage message, Multipart mp,
			final String contentType, boolean resize)
			throws MessagingException {
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		if (optImageSource != null && !optImageSource.isEmpty()) {
			// load remote image and add it to the mail
			try {
				InputStream is = null;
				try {
					if (optImageSource.contains("@")) {
						String userPass = optImageSource.split("@")[0];
						String urlStr = optImageSource.split("@")[1];
						String user = userPass.split(":")[0];
						String pass = userPass.split(":")[1];
						is = HttpBasicAuth.downloadFileWithAuth(urlStr, user, pass);
					} else
						is = new IOurl(optImageSource).getInputStream();
					is = ResourceIOManager.getInputStreamMemoryCached(is);
					tso.setInt(((MyByteArrayInputStream) is).getCount());
					// SystemAnalysis.simulateHeadless = false;
					if (resize) {
						Image img = new Image(is);
						if (img.getWidth() > 640 || img.getHeight() > 480) {
							double sc1 = 640d / img.getWidth();
							double sc2 = 480d / img.getHeight();
							double sc = Math.min(sc1, sc2);
							is = img.io().resize(sc, sc).getImage().getAsJPGstream();
						}
					}
				} catch (Exception e) {
					throw new IOException(e.getMessage());
				}
				
				final InputStream fis = is;
				
				MimeBodyPart img = new MimeBodyPart();
				img.setDataHandler(new DataHandler(new DataSource() {
					
					@Override
					public OutputStream getOutputStream() throws IOException {
						return null;
					}
					
					@Override
					public String getName() {
						String n = StringManipulationTools.getFileSystemName(fileName);
						return n;
					}
					
					@Override
					public InputStream getInputStream() throws IOException {
						return ((MyByteArrayInputStream) fis).getNewStream();
					}
					
					@Override
					public String getContentType() {
						return contentType;
					}
				}));
				img.setFileName(StringManipulationTools.getFileSystemName(fileName));
				mp.addBodyPart(img);
			} catch (Exception e) {
				message.setText(aBody + "\n\nImage could not be loaded. " +
						"Eventually the image source can not be read or the webcam is turned off.\nError: "
						+ e.getMessage());
			}
		}
		return tso.getInt();
	}
	
	private void createScreenshotAndAttachToMail(Multipart mp) throws AWTException, MessagingException, IOException {
		final Screenshot screenshot = SystemAnalysisExt.getScreenshot();
		
		MimeBodyPart img = new MimeBodyPart();
		img.setDataHandler(new DataHandler(new DataSource() {
			
			@Override
			public OutputStream getOutputStream() throws IOException {
				return null;
			}
			
			@Override
			public String getName() {
				return screenshot.getScreenshotFileName();
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				return screenshot.getScreenshotImage();
			}
			
			@Override
			public String getContentType() {
				return "image/jpeg";
			}
		}));
		img.setFileName(screenshot.getScreenshotFileName());
		mp.addBodyPart(img);
	}
	
	private static Properties fMailServerConfig = new Properties();
	
	static {
		fetchConfig();
	}
	
	private static void fetchConfig() {
		InputStream input = null;
		try {
			// If possible, one should try to avoid hard-coding a path in this
			// manner; in a web application, one should place such a file in
			// WEB-INF, and access it using ServletContext.getResourceAsStream.
			// Another alternative is Class.getResourceAsStream.
			// This file contains the javax.mail config properties mentioned above.
			input = new FileInputStream(FILE_NAME_MAIL_SERVER_CONFIG);
			fMailServerConfig.load(input);
		} catch (IOException ex) {
			TextFile tf = new TextFile();
			tf.add("# Configuration file for javax.mail");
			tf.add("# If a value for an item is not provided, then");
			tf.add("# system defaults will be used. These items can");
			tf.add("# also be set in code.");
			tf.add("");
			tf.add("# Host whose mail services will be used");
			tf.add("# (Default value : localhost)");
			tf.add("mail.host=mail.ipk-gatersleben.de");
			tf.add("");
			tf.add("# Return address to appear on emails");
			tf.add("mail.from=user@host");
			tf.add("");
			tf.add("# Other possible items include:");
			tf.add("# mail.user=");
			tf.add("# mail.store.protocol=");
			tf.add("# mail.transport.protocol=");
			tf.add("mail.smtp.host=");
			tf.add("# mail.smtp.user=");
			tf.add("# mail.debug=");
			try {
				tf.write(FILE_NAME_MAIL_SERVER_CONFIG);
				AttributeHelper.showInBrowser(FILE_NAME_MAIL_SERVER_CONFIG);
				System.err.println("Cannot open or read mail server properties file.");
			} catch (IOException e) {
				System.err.println("Cannot create mail server properties file.");
			}
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ex) {
				System.err.println("Cannot close mail server properties file.");
			}
		}
	}
}
