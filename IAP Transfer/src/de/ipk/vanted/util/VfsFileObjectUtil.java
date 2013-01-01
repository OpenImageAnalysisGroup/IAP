package de.ipk.vanted.util;

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
		return VfsFileObjectUtil.createVfsFileObject(protocol, host, filePath,
				null, null, null);
	}
	
	public static VfsFileObject createVfsFileObject(VfsFileProtocol protocol,
			String host, String filePath, String username, String password)
			throws Exception {
		return VfsFileObjectUtil.createVfsFileObject(protocol, host, filePath,
				username, password, null);
	}
	
	public synchronized static VfsFileObject createVfsFileObject(VfsFileProtocol protocol,
			String host, String filePath, String username, String password,
			Integer port) throws Exception {
		if (StringUtils.isBlank(host) && protocol != VfsFileProtocol.LOCAL) {
			throw new Exception("Host name can not be empty!");
		}
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
				break;
		}
		if (StringUtils.isNotBlank(username)) {
			con += username;
			if (StringUtils.isNotBlank(password)) {
				con += ":" + password;
			}
			con += "@";
		}
		con += host;
		
		if (port != null) {
			con += ":" + port;
		}
		if (StringUtils.isNotBlank(filePath)) {
			con += "/" + filePath;
		}
		FileSystemOptions opts = new FileSystemOptions();
		// Timeout is count by Milliseconds
		SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
		if (StringUtils.isNotBlank(username)) {
			StaticUserAuthenticator auth = new StaticUserAuthenticator(
					username, password, null);
			DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(
					opts, auth);
		}
		FileSystemManager fsm = VFS.getManager();
		FileObject fo = fsm.resolveFile(con, opts);
		VfsFileObject vfsFileObj = new VfsFileObjectImpl(fo);
		return vfsFileObj;
	}
}
