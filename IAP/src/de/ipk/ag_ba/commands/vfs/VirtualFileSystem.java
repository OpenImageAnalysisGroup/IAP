package de.ipk.ag_ba.commands.vfs;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.io.resources.IOurl;

/**
 * @author klukas
 */
public class VirtualFileSystem {
	
	public static Collection<VirtualFileSystem> getKnown() {
		ArrayList<VirtualFileSystem> res = new ArrayList<VirtualFileSystem>();
		res.add(new VirtualFileSystem());
		return res;
	}
	
	public String getTargetName() {
		return "My FTP Server 1";
	}
	
	public String getTransferProtocolName() {
		return "FTP";
	}
	
	public String getTargetPathName() {
		return "storage_1";
	}
	
	public String getPrefix() {
		return "vfs-ftp";
	}
	
	public String getResultPathNameForUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @return List of file names found at root of VFS source
	 */
	public ArrayList<String> listFiles() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IOurl getIOurlFor(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
