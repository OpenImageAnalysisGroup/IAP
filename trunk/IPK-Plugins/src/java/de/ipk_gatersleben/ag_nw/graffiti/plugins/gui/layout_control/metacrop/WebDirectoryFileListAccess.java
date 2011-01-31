package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.io.resources.IOurl;

public class WebDirectoryFileListAccess {
	
	public static Collection<PathwayWebLinkItem> getMetaCropListItems() throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = "http://vanted.ipk-gatersleben.de/addons/metacrop/";
		String pref = address;
		
		// Create a URL for the desired page
		URL url = new URL(address);
		
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str;
		while ((str = in.readLine()) != null) {
			// str is one line of text; readLine() strips the newline character(s)
			String a = "<a href=\"";
			String b = "\">";
			String c1 = ".gml";
			String c2 = ".graphml";
			if (!str.contains(a) || !str.substring(str.indexOf(a)).contains(b) || (!str.contains(c1) && !str.contains(c2)))
				continue;
			String fileName = str.substring(str.indexOf(a) + a.length());
			fileName = fileName.substring(0, fileName.indexOf(b));
			result.add(new PathwayWebLinkItem(fileName, new IOurl(pref + fileName)));
		}
		in.close();
		
		return result;
	}
	
	public static Collection<PathwayWebLinkItem> getWebDirectoryFileListItems(String webAddress,
						String[] validExtensions, boolean showGraphExtensions) throws IOException {
		ArrayList<PathwayWebLinkItem> result = new ArrayList<PathwayWebLinkItem>();
		String address = webAddress;
		String pref = address;
		
		// Create a URL for the desired page
		URL url = new URL(address);
		
		// Read all the text returned by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		String str;
		while ((str = in.readLine()) != null) {
			// str is one line of text; readLine() strips the newline character(s)
			String a = "<a href=\"";
			String b = "\">";
			boolean containsExtension = false;
			for (String ext : validExtensions)
				if (str.contains(ext + "\">")) {
					containsExtension = true;
					break;
				}
			if (!str.contains(a) || !str.substring(str.indexOf(a)).contains(b) || !containsExtension)
				continue;
			String fileName = str.substring(str.indexOf(a) + a.length());
			fileName = fileName.substring(0, fileName.indexOf(b));
			PathwayWebLinkItem pwl = new PathwayWebLinkItem(fileName, new IOurl(pref + fileName), showGraphExtensions);
			result.add(pwl);
		}
		in.close();
		
		return result;
	}
}
