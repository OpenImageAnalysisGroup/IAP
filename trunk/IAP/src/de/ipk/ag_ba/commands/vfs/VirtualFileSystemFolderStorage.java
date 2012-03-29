package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.IOurl;

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
	public ArrayList<String> listFiles() {
		ArrayList<String> res = new ArrayList<String>();
		for (String f : new File(path).list())
			res.add(f);
		return res;
	}
	
	@Override
	public String toString() {
		return name + " (" + protocolDescription + ")";
	}
	
	@Override
	public IOurl getIOurlFor(String fileName) {
		return null;
	}
	
}
