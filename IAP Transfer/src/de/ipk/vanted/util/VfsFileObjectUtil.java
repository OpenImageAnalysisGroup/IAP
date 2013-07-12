package de.ipk.vanted.util;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileObjectImpl;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class VfsFileObjectUtil {
	
	public static VfsFileObject createVfsFileObject(VfsFileProtocol protocol,
			String host, String filePath) throws Exception {
		return createVfsFileObject(protocol, host, filePath,
				null, null, null);
	}
	
	public static VfsFileObject createVfsFileObject(VfsFileProtocol protocol,
			String host, String filePath, String username, String password)
			throws Exception {
		return createVfsFileObject(protocol, host, filePath,
				username, password, null);
	}
	
	public static VfsFileObject createVfsFileObject(VfsFileProtocol protocol,
			String host, String filePath, String username, String password,
			Integer port) throws Exception {
		if (StringUtils.isBlank(host) && protocol != VfsFileProtocol.LOCAL) {
			throw new Exception("Host name can not be empty!");
		}
		boolean authOK = true;
		boolean local = false;
		String con = "file://";
		switch (protocol) {
			case FTP:
				con = "ftp://";
				break;
			case SFTP:
				con = "sftp://";
				break;
			case FTPS:
				con = "ftps://";
				break;
			case WebDAV:
				con = "webdav://";
				break;
			case HTTP:
				con = "http://";
				break;
			case HTTPS:
				con = "https://";
				break;
			default:
				con = "file://";
				authOK = false;
				local = true;
				break;
		}
		if (authOK && StringUtils.isNotBlank(username)) {
			con += username;
			if (StringUtils.isNotBlank(password)) {
				con += ":" + password;
			}
			con += "@";
		}
		if (!local)
			con += host;
		
		if (port != null) {
			con += ":" + port;
		}
		if (StringUtils.isNotBlank(filePath)) {
			con += "/" + filePath;
		}
		FileSystemOptions opts = new FileSystemOptions();
		// Timeout is count by Milliseconds
		SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, SystemOptions.getInstance().getInteger("VFS", "timeout_s", 60) * 1000);
		if (authOK && StringUtils.isNotBlank(username)) {
			StaticUserAuthenticator auth = new StaticUserAuthenticator(
					username, password, null);
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
					opts, auth);
		}
		FileSystemManager fsm = VFS.getManager();
		FileObject fo = null;
		// for (int i = 0; i < 10; i++) {
		// try {
		// fo = fsm.resolveFile(con, opts);
		// if (fo == null)
		// System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: COULD NOT RESOLVE FILE: " + filePath);
		// VfsFileObject vfsFileObj = new VfsFileObjectImpl(fo);
		// return vfsFileObj;
		// } catch (Exception e) {
		// e.printStackTrace();
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: COULD NOT RESOLVE FILE (" + e.getMessage() + "). TRYING AGAIN...");
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }
		// }
		// }
		fo = fsm.resolveFile(con, opts);
		if (fo == null) {
			System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: COULD NOT RESOLVE FILE: " + filePath);
			ErrorMsg.addErrorMessage("COULD NOT RESOLVE FILE: " + filePath);
			return null;
		}
		VfsFileObject vfsFileObj = new VfsFileObjectImpl(fo);
		return vfsFileObj;
	}
}
