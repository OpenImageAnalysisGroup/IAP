package org;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ExternalPasswordStorage {
	
	private static ExternalPasswordStorage instance = new ExternalPasswordStorage();
	private BASE64Encoder base64encoder;
	private BASE64Decoder base64decoder;
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
			this.base64encoder = new BASE64Encoder();
			this.base64decoder = new BASE64Decoder();
			this.encCipher = Cipher.getInstance("DES"); // cipher is not thread safe
			this.decCipher = Cipher.getInstance("DES"); // cipher is not thread safe
			this.encCipher.init(Cipher.ENCRYPT_MODE, key);
			this.decCipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public synchronized static String encryptValueIfNeeded(String group, String setting, String value) {
		if (!needsToBeStoredEncrypted(group, setting, value))
			return value;
		else {
			byte[] cleartext;
			try {
				cleartext = value.getBytes("UTF8");
				String encrypedPwd = instance.base64encoder.encode(instance.encCipher.doFinal(cleartext));
				return "encrypted:" + encrypedPwd;
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				return null;
			}
		}
	}
	
	public synchronized static String decryptValueIfNeeded(String group, String setting, String value) {
		if (!hasBeenStoredEncrypted(group, setting, value)) {
			return value;
		} else {
			byte[] encrypedPwdBytes;
			try {
				value = value.substring("encrypted:".length());
				encrypedPwdBytes = instance.base64decoder.decodeBuffer(value);
				byte[] plainTextPwdBytes = (instance.decCipher.doFinal(encrypedPwdBytes));
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
	
	private static boolean hasBeenStoredEncrypted(String group, String setting, String value) {
		if (setting != null && setting.contains("password") && value != null && value.startsWith("encrypted:"))
			return true;
		else
			return false;
	}
}
