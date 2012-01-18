package de.ipk.ag_ba.server.task_management.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.junit.Test;

public class Encryption {
	SecretKeySpec key;
	Cipher cipher;
	
	public Encryption() throws Exception {
		// empty
	}
	
	public void setKey(String keyPass) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		while (keyPass.length() < (128 / 8))
			keyPass += keyPass;
		if (keyPass.length() > 128 / 8)
			keyPass = keyPass.substring(0, 128 / 8);
		byte[] keyBytes = keyPass.getBytes();
		// new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
		// 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
		
		key = new SecretKeySpec(keyBytes, "AES");
		cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
	}
	
	public byte[] encrypt(byte[] input) throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
		int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
		ctLength += cipher.doFinal(cipherText, ctLength);
		return cipherText;
	}
	
	public byte[] decrypt(byte[] cipherText) throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
		int ctLength = cipherText.length;
		
		byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
		cipher.init(Cipher.DECRYPT_MODE, key);
		int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
		ptLength += cipher.doFinal(plainText, ptLength);
		return plainText;
	}
	
	@Test
	public void test() throws Exception {
		String inputMsg = "Secret message";
		byte[] input = inputMsg.getBytes();
		System.out.println("input text : " + new String(input));
		setKey("pass");
		byte[] enc = encrypt(inputMsg.getBytes());
		String encS = new String(enc);
		
		Assert.assertTrue("encrypted text needs to differ from input",
				!inputMsg.equals(encS));
		
		byte[] decrypted = decrypt(enc);
		String decryptedMsg = new String(decrypted);
		System.out.println(inputMsg + " ==> " + decryptedMsg + " (EQUALS? " + inputMsg.equals(decryptedMsg) + ")");
		
		Assert.assertTrue("decrypted encrypted text needs to be the same as the source input text",
				inputMsg.equals(decryptedMsg));
	}
}
