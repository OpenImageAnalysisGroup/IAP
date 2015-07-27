package de.ipk.vanted.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;

public class FTPFileObjectTest {
	
	private static String host = "bis.zju.edu.cn";
	private static String user = "clone";
	private static String passwd = "clone";
	private static String path = "/home/clone/";
	
	private static VfsFileObject file = null;
	
	static {
		try {
			file = VfsFileObjectUtil.createVfsFileObject(VfsFileProtocol.FTP,
					host, path, user, passwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws Exception {
		System.out.println("**********************************\n");
	}
	
	@Test
	public void testMkdir() throws Exception {
		System.out.println("making dir");
		VfsFileObject dir = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, path + "/test/test", user, passwd);
		assertTrue(dir.mkdir());
	}
	
	@Test
	public void testUpload() throws Exception {
		System.out.println("uploading ...");
		VfsFileObject from = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.SFTP, "10.71.115.165", "/test/test.pl",
				"chendijun", "chendijun");
		VfsFileObject to = VfsFileObjectUtil
				.createVfsFileObject(VfsFileProtocol.FTP, host, path
						+ "/test/test.pl", user, passwd);
		from.upload(to);
	}
	
	@Test
	public void testExist() throws Exception {
		System.out.println("existing ...");
		assertTrue(file.exists());
	}
	
	@Test
	public void testDelete() throws Exception {
		System.out.println("deleting ...");
		VfsFileObject to = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, path + "/test/test", user, passwd);
		assertTrue(to.delete());
	}
	
	@Test
	public void testDownload() throws Exception {
		System.out.println("downloaing ...");
		VfsFileObject from = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, path + "/Complementary.tif", user,
				passwd);
		VfsFileObject to = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.LOCAL, VfsFileObjectTest.getDesktopFolder() + File.separator + "tmp", "Complementary.tif", null,
				null);
		from.download(to);
	}
	
	@Test
	public void testGetInputStream() throws Exception {
		System.out.println("getInputStream ...");
		VfsFileObject from = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, path + "/test.txt", user, passwd);
		InputStream is = from.getInputStream();
		int tempbyte;
		while ((tempbyte = is.read()) != -1) {
			System.out.write(tempbyte);
		}
	}
	
	@Test
	@Ignore
	public void testGetOutputStream() throws Exception {
		System.out.println("getOutputStream ...");
		VfsFileObject in = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, "/test.txt", user, passwd);
		InputStream is = in.getInputStream();
		VfsFileObject out = VfsFileObjectUtil
				.createVfsFileObject(VfsFileProtocol.FTP, host, path
						+ "/test.txt.bak", user, passwd);
		OutputStream os = out.getOutputStream();
		copyStream(is, os);
	}
	
	@Test
	public void testIsDirectory() throws Exception {
		System.out.println("isDirectory ...");
		System.out.println("isDirectory: " + file.isDirectory());
	}
	
	@Test
	public void testIsFile() throws Exception {
		System.out.println("isFile ...");
		System.out.println("isFile: " + file.isFile());
	}
	
	@Test
	public void testIsReadable() throws Exception {
		System.out.println("isReadable ...");
		System.out.println("isReadable: " + file.isReadable());
	}
	
	@Test
	public void testIsWriteable() throws Exception {
		System.out.println("isWriteable ...");
		System.out.println("isWriteable: " + file.isWriteable());
	}
	
	@Test
	public void testLength() throws Exception {
		System.out.println("getLength ...");
		VfsFileObject from = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, path + "/Complementary.tif", user,
				passwd);
		System.out.println("length: " + from.length());
	}
	
	@Test
	public void testList() throws Exception {
		System.out.println("listing ...");
		String[] list = file.list();
		int index = 0;
		for (String f : list) {
			System.out.println((index++) + ": " + f);
		}
	}
	
	public void testRenameTo() throws Exception {
		VfsFileObject from = VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.FTP, host, path + "/test.tif", user, passwd);
		VfsFileObject to = VfsFileObjectUtil
				.createVfsFileObject(VfsFileProtocol.FTP, host, path
						+ "/test.tif.bak", user, passwd);
		from.renameTo(to, true);
	}
	
	@Test
	public void testGetName() throws Exception {
		System.out.println("getting name ...");
		System.out.println("name: " + file.getName());
	}
	
	@Test
	public void testGetURL() throws Exception {
		System.out.println("getting URL ...");
		System.out.println("name: " + file.getURL());
	}
	
	@Test
	public void testGetFile() throws Exception {
		System.out.println("getting File ...");
		System.out.println("file: " + file.getFile());
	}
	
	public void copyStream(InputStream is, OutputStream os) throws IOException {
		String line;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
	}
}
