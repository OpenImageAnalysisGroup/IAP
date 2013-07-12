package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.FileSystemHandler;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.vanted.plugin.VfsFileObject;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk.vanted.util.VfsFileObjectUtil;

public class VirtualFileSystemFolderStorage extends VirtualFileSystem {
	
	private final String name;
	private final String path;
	private final String protocolDescription;
	private final String prefix;
	
	public VirtualFileSystemFolderStorage(
			String prefix,
			String protocolDescription, String name, String path) {
		this.prefix = prefix;
		this.protocolDescription = protocolDescription;
		this.name = name;
		this.path = path;
		if (!new File(path).exists())
			new File(path).mkdirs();
	}
	
	@Override
	public String getTargetName() {
		return name;
	}
	
	@Override
	public String getTransferProtocolName() {
		return protocolDescription;
	}
	
	@Override
	public String getTargetPathName() {
		return path;
	}
	
	@Override
	public String getPrefix() {
		return prefix;
	}
	
	@Override
	public ArrayList<String> listFiles(String optSubDirectory) {
		ArrayList<String> res = new ArrayList<String>();
		File ff = new File(path + (optSubDirectory != null ? File.separator + optSubDirectory : ""));
		if (!ff.exists()) {
			boolean createSubFolder = false; // not needed
			if (createSubFolder) {
				ff.mkdirs();
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Directory " + ff.getAbsolutePath() + " has been created!");
			}
		} else
			for (String f : ff.list())
				res.add(f);
		return res;
	}
	
	@Override
	public ArrayList<String> listFolders(String optSubDirectory) {
		ArrayList<String> res = new ArrayList<String>();
		File ff = new File(path + (optSubDirectory != null ? File.separator + optSubDirectory : ""));
		if (!ff.exists()) {
			boolean createSubFolder = false; // not needed
			if (createSubFolder) {
				ff.mkdirs();
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Directory " + ff.getAbsolutePath() + " has been created!");
			}
		} else
			for (String f : ff.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return new File(dir, name).isDirectory();
				}
			})) {
				res.add(f);
			}
		return res;
	}
	
	@Override
	public String toString() {
		return name + " (" + protocolDescription + ")";
	}
	
	@Override
	public IOurl getIOurlFor(String fileName) {
		return FileSystemHandler.getURL(new File(path + File.separator + fileName));
	}
	
	@Override
	public InputStream getInputStream(IOurl url) {
		throw new UnsupportedOperationException("Not needed, the returned IOurl is from FileSystemHandler");
	}
	
	@Override
	public InputStream getPreviewInputStream(IOurl url) {
		throw new UnsupportedOperationException("Not needed, the returned IOurl is from FileSystemHandler");
	}
	
	@Override
	public long getFileLength(IOurl url) {
		throw new UnsupportedOperationException("Not needed, the returned IOurl is from FileSystemHandler");
	}
	
	@Override
	public VfsFileObject getFileObjectFor(String fileName) throws Exception {
		String path = fileName.substring(0, fileName.lastIndexOf("/"));
		String fn = fileName.substring(fileName.lastIndexOf("/") + "/".length());
		return VfsFileObjectUtil.createVfsFileObject(
				VfsFileProtocol.LOCAL,
				path, fn, null, null);
	}
	
	@Override
	public boolean isAbleToSaveData() {
		return true;
	}
}
