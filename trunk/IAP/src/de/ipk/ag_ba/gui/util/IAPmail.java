package de.ipk.ag_ba.gui.util;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.HttpBasicAuth;
import org.ReleaseInfo;
import org.Screenshot;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class IAPmail {
	
	private static final String FILE_NAME_MAIL_SERVER_CONFIG = ReleaseInfo.getAppSubdirFolderWithFinalSep("watch") + "mail-server.txt";
	
	// http://www.javapractices.com/topic/TopicAction.do?Id=144
	public void sendEmail(
			String aToEmailAddr, String aSubject,
			String aBody, final String optImageSource1, final String fileName1, final String contentType1,
			final String optImageSource2, final String fileName2, final String contentType2
			) {
		// Here, no Authenticator argument is used (it is null).
		// Authenticators are used to prompt the user for user
		// name and password.
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
				addWebCamScreenshotToMail(aBody, optImageSource1, fileName1, message, mp, contentType1);
			if (optImageSource2 != null && fileName2 != null && contentType2 != null && !optImageSource2.isEmpty() && !fileName2.isEmpty()
					&& !contentType2.isEmpty())
				addWebCamScreenshotToMail(aBody, optImageSource2, fileName2, message, mp, contentType2);
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
			MimeBodyPart txt = new MimeBodyPart();
			txt.setText("\n" + aBody);
			mp.addBodyPart(txt);
			message.setContent(mp);
			
			// } else {
			// message.setText(aBody);
			// }
			
			Transport.send(message);
		} catch (MessagingException ex) {
			System.err.println("Cannot send email. " + ex);
		}
	}
	
	private void addWebCamScreenshotToMail(
			String aBody,
			final String optImageSource,
			final String fileName,
			MimeMessage message, Multipart mp,
			final String contentType)
			throws MessagingException {
		if (optImageSource != null && !optImageSource.isEmpty()) {
			// load remote image and add it to the mail
			try {
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
						try {
							InputStream is;
							if (optImageSource.contains("@")) {
								String userPass = optImageSource.split("@")[0];
								String urlStr = optImageSource.split("@")[1];
								String user = userPass.split(":")[0];
								String pass = userPass.split(":")[1];
								is = HttpBasicAuth.downloadFileWithAuth(urlStr, user, pass);
							} else
								is = new IOurl(optImageSource).getInputStream();
							return is;
						} catch (Exception e) {
							throw new IOException(e.getMessage());
						}
					}
					
					@Override
					public String getContentType() {
						return contentType;
					}
				}));
				img.setFileName(StringManipulationTools.getFileSystemName(fileName));
				mp.addBodyPart(img);
			} catch (Exception e) {
				message.setText(aBody + "\n\nWebcam-Image could not be loaded. Eventually the Webcam is turned off.\nError: " + e.getMessage());
			}
		}
	}
	
	private void createScreenshotAndAttachToMail(Multipart mp) throws AWTException, MessagingException, IOException {
		final Screenshot screenshot = SystemAnalysis.getScreenshot();
		
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
				return "image/png";
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
			tf.add("# (Default value : username@host)");
			tf.add("mail.from=klukas@ipk-gatersleben.de");
			tf.add("");
			tf.add("# Other possible items include:");
			tf.add("# mail.user=");
			tf.add("# mail.store.protocol=");
			tf.add("# mail.transport.protocol=");
			tf.add("mail.smtp.host=mail.ipk-gatersleben.de");
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
