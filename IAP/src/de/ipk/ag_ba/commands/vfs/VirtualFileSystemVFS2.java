package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;

public class VirtualFileSystemVFS2 extends VirtualFileSystem {
	
	private final VfsFileProtocol vfs_type;
	private final String description;
	private final String protocoll;
	private final String host;
	private final String user;
	private final String pass;
	private final String folder;
	private final String prefix;
	
	public VirtualFileSystemVFS2(
			String prefix,
			VfsFileProtocol vfs_type,
			String description,
			String protocoll,
			String host,
			String user,
			String pass,
			String folder) {
		this.prefix = prefix;
		this.vfs_type = vfs_type;
		this.description = description;
		this.protocoll = protocoll;
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.folder = folder;
		if (this.vfs_type == VfsFileProtocol.LOCAL) {
			ResourceIOManager.registerIOHandler(new FileSystemHandler(this.prefix, this.folder));
		}
	}
	
	@Override
	public String getTargetName() {
		return description;
	}
	
	@Override
	public String getTransferProtocolName() {
		return protocoll;
	}
	
	@Override
	public String getTargetPathName() {
		return folder;
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public ArrayList<String> listFiles(String optSubDirectory) {
		try {
			String path = folder;
			if (optSubDirectory != null)
				path = path + "/" + optSubDirectory;
			
			VfsFileObject file = VfsFileObjectUtil.createVfsFileObject(vfs_type,
					host, path, user, pass);
			if (!file.exists()) {
				System.out.println(">>>>>>");
				file.mkdir();
			}
			ArrayList<String> res = new ArrayList<String>();
			for (String s : file.list()) {
				res.add(s);
			}
			return res;
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	@Override
	public IOurl getIOurlFor(String fileName) {
		try {
			VfsFileObject file = VfsFileObjectUtil.createVfsFileObject(vfs_type,
					host, folder + "/" + fileName, user, pass);
			File tempFile = File.createTempFile("iap_vfs", ".tmp");
			
			String tmpFolder = tempFile.getParent();
			String tempFileName = tempFile.getName();
			
			VfsFileObject to = VfsFileObjectUtil.createVfsFileObject(
					VfsFileProtocol.LOCAL, tmpFolder,
					tempFileName);
			
			file.download(to);
			
			return FileSystemHandler.getURL(tempFile);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public VfsFileObject newVfsFile(String fileNameInclSubFolderPathName) throws Exception {
		return VfsFileObjectUtil.createVfsFileObject(
				vfs_type, host,
				fileNameInclSubFolderPathName, user, pass);
	}
	
	public VfsFileProtocol getProtocolType() {
		return vfs_type;
	}
	
}
