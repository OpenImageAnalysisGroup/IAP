package de.ipk.ag_ba.mongo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.AttributeHelper;
import org.ReleaseInfo;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class IAPmail {
	
	private static final String FILE_NAME_MAIL_SERVER_CONFIG = ReleaseInfo.getAppFolderWithFinalSep() + "mail-server.txt";
	
	// http://www.javapractices.com/topic/TopicAction.do?Id=144
	public void sendEmail(
			String aFromEmailAddr, String aToEmailAddr,
			String aSubject, String aBody
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
			message.addRecipient(
					Message.RecipientType.TO, new InternetAddress(aToEmailAddr)
					);
			message.setSubject(aSubject);
			message.setText(aBody);
			Transport.send(message);
		} catch (MessagingException ex) {
			System.err.println("Cannot send email. " + ex);
		}
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
			tf.add("# mail.smtp.host=");
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
