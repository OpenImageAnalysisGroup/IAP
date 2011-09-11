package de.ipk.ag_ba.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.ErrorMsg;
import org.ObjectRef;
import org.graffiti.editor.HashType;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;

public class GravistoServiceExt {
	
	public static String[] getHashFromInputStream(final InputStream[] iss, final ObjectRef[] optFileSize, final HashType type, boolean threaded)
			throws Exception {
		if (iss == null)
			return null;
		
		final ArrayList<ObjectRef> resultList = new ArrayList<ObjectRef>();
		for (int i = 0; i < iss.length; i++)
			resultList.add(new ObjectRef());
		ArrayList<MyThread> wait = new ArrayList<MyThread>();
		for (int i = 0; i < iss.length; i++) {
			final int iii = i;
			wait.add(
					BackgroundThreadDispatcher.addTask(new Runnable() {
						@Override
						public void run() {
							InputStream is = iss[iii];
							if (is == null)
								return;
							MessageDigest digest;
							try {
								digest = MessageDigest.getInstance(type.toString());
							} catch (NoSuchAlgorithmException e1) {
								throw new Error(e1);
							}
							byte[] buffer = new byte[1024 * 1024];
							int read = 0;
							long len = 0;
							try {
								while ((read = is.read(buffer)) > 0) {
									len += read;
									digest.update(buffer, 0, read);
								}
								if (optFileSize[iii] != null)
									optFileSize[iii].addLong(len);
							} catch (IOException e) {
								ErrorMsg.addErrorMessage(e);
							} finally {
								try {
									is.close();
								} catch (IOException e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
							
							byte[] md5sum = digest.digest();
							BigInteger bigInt = new BigInteger(1, md5sum);
							String output = bigInt.toString(16);
							resultList.get(iii).setObject(output);
						}
					}, "Hash Calculation (Stream " + i + ")", 30, 29));
		}
		BackgroundThreadDispatcher.waitFor(wait);
		String[] res = new String[iss.length];
		for (int i = 0; i < iss.length; i++)
			res[i] = (String) resultList.get(i).getObject();
		return res;
	}
}
