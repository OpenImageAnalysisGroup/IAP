package org;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class ExternalPasswordStorage {
	
	private static ExternalPasswordStorage instance = new ExternalPasswordStorage();
	
	private Cipher encCipher, decCipher;
	
	private ExternalPasswordStorage() {
		SystemOptions so = SystemOptions.getInstance("secret", null);
		String defaultValue = java.util.UUID.randomUUID().toString();
		String passphrase = so.getString("Symmetric Encryption", "key", defaultValue);
		so.getString("Symmetric Encryption", "hint", "Restart application if this file is manually changed");
		DESKeySpec keySpec;
		try {
			keySpec = new DESKeySpec(passphrase.getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			this.encCipher = Cipher.getInstance("DES"); // cipher is not thread safe
			this.decCipher = Cipher.getInstance("DES"); // cipher is not thread safe
			this.encCipher.init(Cipher.ENCRYPT_MODE, key);
			this.decCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public synchronized static String encryptAndConvertValueIfNeeded(String group, String setting, String value) {
		return encryptValueIfNeeded(group, setting, convertValueIfNeeded(group, setting, value));
	}
	
	private synchronized static String encryptValueIfNeeded(String group, String setting, String value) {
		if (!needsToBeStoredEncrypted(group, setting, value))
			return value;
		else {
			byte[] cleartext;
			try {
				cleartext = value.getBytes("UTF8");
				String encrypedPwd = Base64.getEncoder().encodeToString(instance.encCipher.doFinal(cleartext));
				return "encrypted:" + encrypedPwd;
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	public synchronized static String decryptAndUnconvertValueIfNeeded(String group, String setting, String value) {
		return unconvertValueIfNeeded(group, setting, decryptValueIfNeeded(group, setting, value));
	}
	
	private synchronized static String decryptValueIfNeeded(String group, String setting, String value) {
		if (!hasBeenStoredEncrypted(group, setting, value)) {
			return value;
		} else {
			byte[] encrypedPwdBytes;
			try {
				value = value.substring("encrypted:".length());
				encrypedPwdBytes = Base64.getDecoder().decode(value);
				byte[] plainTextPwdBytes = (instance.decCipher.doFinal(encrypedPwdBytes));
				return new String(plainTextPwdBytes, "UTF8");
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	private synchronized static String convertValueIfNeeded(String group, String setting, String value) {
		if (!needsToBeStoredConverted(group, setting, value))
			return value;
		else {
			byte[] cleartext;
			try {
				cleartext = value.getBytes("UTF8");
				return "base64:" + Base64.getEncoder().encodeToString(cleartext);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	private synchronized static String unconvertValueIfNeeded(String group, String setting, String value) {
		if (!hasBeenStoredConverted(group, setting, value)) {
			return value;
		} else {
			try {
				value = value.substring("base64:".length());
				byte[] plainTextPwdBytes = Base64.getDecoder().decode(value);
				return new String(plainTextPwdBytes, "UTF8");
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	private static boolean needsToBeStoredEncrypted(String group, String setting, String value) {
		if (setting != null && setting.contains("password") && value != null && !value.isEmpty() && !value.equals("?"))
			return true;
		else
			return false;
	}
	
	private static boolean needsToBeStoredConverted(String group, String setting, String value) {
		if (setting != null && setting.contains("base64") && value != null && !value.isEmpty() && !value.equals("?"))
			return true;
		else
			return false;
	}
	
	private static boolean hasBeenStoredEncrypted(String group, String setting, String value) {
		if (setting != null && setting.contains("password") && value != null && value.startsWith("encrypted:"))
			return true;
		else
			return false;
	}
	
	private static boolean hasBeenStoredConverted(String group, String setting, String value) {
		if (setting != null && setting.contains("base64") && value != null && value.startsWith("base64:"))
			return true;
		else
			return false;
	}
}
