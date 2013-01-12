package de.ipk.ag_ba.commands.vfs;

import java.io.InputStream;
import java.util.ArrayList;

import org.SystemAnalysis;
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
	private final boolean useForMongoFileStorage;
	private final boolean useOnlyForMongoFileStorage;
	private final String useForMongoFileStorageCloudName;
	
	public VirtualFileSystemVFS2(
			String prefix,
			VfsFileProtocol vfs_type,
			String description,
			String protocoll,
			String host,
			String user,
			String pass,
			String folder,
			boolean useForMongoFileStorage,
			boolean useOnlyForMongoFileStorage,
			String useForMongoFileStorageCloudName) {
		this.prefix = prefix;
		this.vfs_type = vfs_type;
		this.description = description;
		this.protocoll = protocoll;
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.folder = folder;
		this.useForMongoFileStorage = useForMongoFileStorage;
		this.useOnlyForMongoFileStorage = useOnlyForMongoFileStorage;
		this.useForMongoFileStorageCloudName = useForMongoFileStorageCloudName;
		if (this.vfs_type == VfsFileProtocol.LOCAL) {
			ResourceIOManager.registerIOHandler(new FileSystemHandler(this.prefix, this.folder));
		} else
			ResourceIOManager.registerIOHandler(new VirtualFileSystemHandler(this));
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
	public ArrayList<String> listFiles(String optSubDirectory) throws Exception {
		String path = folder;
		if (optSubDirectory != null)
			path = path + "/" + optSubDirectory;
		
		VfsFileObject file = VfsFileObjectUtil.createVfsFileObject(vfs_type,
				host, path, user, pass);
		if (!file.exists()) {
			System.out.println(">>>>>> create directory " + path);
			file.mkdir();
		}
		ArrayList<String> res = new ArrayList<String>();
		for (String s : file.list()) {
			res.add(s);
		}
		return res;
	}
	
	@Override
	public IOurl getIOurlFor(String fileNameInclSubFolderPathName) {
		try {
			return new IOurl(prefix, "", fileNameInclSubFolderPathName);
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
	}
	
	public VfsFileObject newVfsFile(String fileNameInclSubFolderPathName) throws Exception {
		return newVfsFile(fileNameInclSubFolderPathName, false);
	}
	
	public VfsFileObject newVfsFile(String fileNameInclSubFolderPathName, boolean absoluteDirName) throws Exception {
		return VfsFileObjectUtil.createVfsFileObject(
				vfs_type, host,
				(absoluteDirName ? "" : folder + "/") +
						fileNameInclSubFolderPathName, user, pass);
	}
	
	public VfsFileProtocol getProtocolType() {
		return vfs_type;
	}
	
	public boolean isUseForMongoFileStorage() {
		return useForMongoFileStorage;
	}
	
	public boolean isUseOnlyForMongoFileStorage() {
		return useOnlyForMongoFileStorage;
	}
	
	public String getUseForMongoFileStorageCloudName() {
		return useForMongoFileStorageCloudName;
	}
	
	public long saveStream(String fileNameInclSubFolderPathName, InputStream is, boolean skipKnown, long expectedLengthIfKnown) throws Exception {
		VfsFileObject file = newVfsFile(fileNameInclSubFolderPathName);
		if (skipKnown && file.exists()) {
			long l = file.length();
			if (l > 0 && l == expectedLengthIfKnown) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">Skipping known file in VFS: " + fileNameInclSubFolderPathName);
				return -l;
			}
		}
		return ResourceIOManager.copyContent(is, file.getOutputStream());
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		VfsFileObject file = newVfsFile(url.getFileName());
		if (file == null)
			return null;
		if (!file.exists())
			return null;
		InputStream is = file.getInputStream();
		return is;
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) throws Exception {
		VfsFileObject file = newVfsFile(url.getFileName());
		if (file == null)
			return null;
		if (!file.exists())
			return null;
		InputStream is = file.getInputStream();
		return is;
	}
	
	@Override
	public long getFileLength(IOurl url) throws Exception {
		VfsFileObject file = newVfsFile(url.getFileName());
		return file.length();
	}
	
	public int countFiles(String optSubDirectory) throws Exception {
		return listFiles(optSubDirectory).size();
	}
	
	@Override
	public VfsFileObject getFileObjectFor(String fileName) throws Exception {
		VfsFileObject file = newVfsFile(fileName);
		return file;
	}
}
