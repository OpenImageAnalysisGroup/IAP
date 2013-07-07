package de.ipk.ag_ba.datasources.file_system;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class FileSystemAccess {
	
	public static Collection<PathwayWebLinkItem> getWebDirectoryFileListItems(String webAddress,
			String[] validExtensions, boolean showGraphExtensions) throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = webAddress;
		String pref = address;
		
		// Create a URL for the desired page
		File url = new File(address);
		
		// Read all the text returned by the server
		HashSet<String> knownImages = new HashSet<String>();
		HashMap<String, PathwayWebLinkItem> knownPWL = new HashMap<String, PathwayWebLinkItem>();
		for (String str : url.list()) {
			{
				boolean containsImageExtension = false;
				for (String ext : new String[] { ".png", ".jpg" })
					if (str.endsWith(ext)) {
						containsImageExtension = true;
						break;
					}
				if (containsImageExtension) {
					knownImages.add(str);
				}
			}
			{
				boolean containsValidExtension = false;
				for (String ext : validExtensions)
					if (str.endsWith(ext)) {
						containsValidExtension = true;
						break;
					}
				if (!containsValidExtension)
					continue;
				String fileName = str;
				PathwayWebLinkItem pwl = new PathwayWebLinkItem(fileName, new IOurl(pref + fileName), showGraphExtensions);
				result.add(pwl);
				
				knownPWL.put(fileName, pwl);
			}
		}
		
		return result;
	}
	
	public static Collection<PathwayWebLinkItem> getWebDirectoryFileListItems(VirtualFileSystem webAddress,
			String[] validExtensions, boolean showGraphExtensions) throws Exception {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		
		// Create a URL for the desired page
		
		// Read all the text returned by the server
		HashSet<String> knownImages = new HashSet<String>();
		HashMap<String, PathwayWebLinkItem> knownPWL = new HashMap<String, PathwayWebLinkItem>();
		for (String str : webAddress.listFiles(null)) {
			{
				boolean containsImageExtension = false;
				for (String ext : new String[] { ".png", ".jpg" })
					if (str.endsWith(ext)) {
						containsImageExtension = true;
						break;
					}
				if (containsImageExtension) {
					knownImages.add(str);
				}
			}
			{
				boolean containsValidExtension = false;
				for (String ext : validExtensions)
					if (str.endsWith(ext)) {
						containsValidExtension = true;
						break;
					}
				if (!containsValidExtension)
					continue;
				PathwayWebLinkItem pwl = new PathwayWebLinkItem(str, webAddress.getIOurlFor(str), showGraphExtensions);
				result.add(pwl);
				
				knownPWL.put(str, pwl);
			}
		}
		
		return result;
	}
}
