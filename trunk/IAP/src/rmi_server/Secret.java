/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Jul 9, 2010 by Christian Klukas
 */
package rmi_server;

import java.io.IOException;

import org.ReleaseInfo;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 *
 */
public class Secret {
	static String getDBurl() throws IOException {
		try {
			return new TextFile(ReleaseInfo.getAppFolderWithFinalSep()+"database_secret").get(1);
		} catch (IOException e) {
			initSecret();
			return "";
		}
	}

	static String getDBuser() throws IOException {
		try {
			return new TextFile(ReleaseInfo.getAppFolderWithFinalSep()+"database_secret").get(2);
		} catch (IOException e) {
			initSecret();
			return "";
		}
	}

	static String getDBpass() throws IOException {
		try {
			return new TextFile(getSecretFileName()).get(3);
		} catch (IOException e) {
			initSecret();
			return "";
		}
	}

	static void initSecret() throws IOException {
		TextFile tf = new TextFile();
		tf.add("#following lines: url, login, pass");
		tf.add("jdbc:oracle:thin:@oradb.ipk-gatersleben.de:1521:genophen");
		tf.add("user");
		tf.add("password");
		tf.write(getSecretFileName());
	}

	private static String getSecretFileName() {
		return ReleaseInfo.getAppFolderWithFinalSep()+"database_secret";
	}
}
