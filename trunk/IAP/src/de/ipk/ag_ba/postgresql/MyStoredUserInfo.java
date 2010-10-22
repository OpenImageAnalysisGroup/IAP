package de.ipk.ag_ba.postgresql;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class MyStoredUserInfo implements UserInfo, UIKeyboardInteractive {
	public String getPassword() {
		return passwd;
	}

	public boolean promptYesNo(String str) {
		System.out.println(str);
		return true;
	}

	String passwd;

	public String getPassphrase() {
		return null;
	}

	public boolean promptPassphrase(String message) {
		System.out.println(message);
		return true;
	}

	public boolean promptPassword(String message) {
		System.out.println(message);
		passwd = null; // specify password
		return true;
	}

	public void showMessage(String message) {
		System.out.println(message);
	}

	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
			boolean[] echo) {
		return new String[] { null }; // specify password
	}
}
