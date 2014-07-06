package org;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HomeFolder implements HelperClass {
	
	public static String getTemporaryFolder() {
		String s = ReleaseInfo.getAppFolder() + SystemAnalysis.getFileSeparator() + "tmp";
		File f = new File(s);
		f.mkdirs();
		f.deleteOnExit();
		return s;
	}
	
	public static String getHomeFolder() {
		return ReleaseInfo.getAppFolder();
	}
	
	public static String getTemporaryFolderWithFinalSep() {
		return getTemporaryFolder() + SystemAnalysis.getFileSeparator();
	}
	
	public static boolean copyFileToTemporaryFolder(File f) {
		return copyFileToTemporaryFolder(null, f);
	}
	
	public static boolean copyFileToTemporaryFolder(String subfolder, File f) {
		try {
			String outfile = getTemporaryFolderWithFinalSep() + f.getName();
			
			if (subfolder != null && !subfolder.equalsIgnoreCase("")) {
				File newDir = new File(getTemporaryFolderWithFinalSep() + SystemAnalysis.getFileSeparator() + subfolder);
				newDir.mkdirs();
				newDir.deleteOnExit();
				outfile = newDir + SystemAnalysis.getFileSeparator() + f.getName();
			}
			
			copyFile(f, new File(outfile));
			
			new File(outfile).deleteOnExit();
			return true;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
		
	}
	
	public static String WIN_MAC_HOMEFOLDER = "IAP_1.2";
	public static String LINUX_HOMEFOLDER = ".iap_1.2";
	public static String WIN_MAC_HOMEFOLDER_OLD = ".iap_1.2";
	
	public static void copyFile(File oldfile, File newfile) throws IOException {
		if (oldfile.compareTo(newfile) != 0) {
			InputStream in = new BufferedInputStream(new FileInputStream(oldfile));
			copyFile(in, newfile);
		}
	}
	
	public static void copyFile(InputStream in, File newfile) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(newfile, false));
		
		byte[] buffer = new byte[0xFFFF];
		for (int len; (len = in.read(buffer)) != -1;)
			out.write(buffer, 0, len);
		
		in.close();
		out.close();
		
	}
	
	public static void copyContent(InputStream in, OutputStream out) throws IOException {
		if (in == null || out == null)
			return;
		
		byte[] buffer = new byte[0xFFFF];
		for (int len; (len = in.read(buffer)) != -1;)
			out.write(buffer, 0, len);
		
		in.close();
		out.close();
		
	}
	
}
