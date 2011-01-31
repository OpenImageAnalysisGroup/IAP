package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

public class PathwayWebLinkItem implements Comparable<PathwayWebLinkItem> {
	
	private final IOurl pathwayURL;
	private String pathwayName;
	private String group1, group2;
	private final boolean showGraphExtensions;
	
	public PathwayWebLinkItem(String pathwayName, IOurl pathwayURL) {
		this(pathwayName, pathwayURL, false);
	}
	
	public PathwayWebLinkItem(String pathwayName, IOurl pathwayURL, boolean showGraphExtensions) {
		this.pathwayName = pathwayName.replaceAll("%20", " ");
		this.pathwayName = this.pathwayName.replaceAll("%3b", ";");
		// this.pathwayName =
		// pathwayName.substring(pathwayName.lastIndexOf("/")+"/".length());
		
		this.pathwayURL = pathwayURL;
		this.showGraphExtensions = showGraphExtensions;
		
		String s = this.pathwayName;
		if (!showGraphExtensions) {
			s = StringManipulationTools.stringReplace(s, ".gml.gz", "");
			s = StringManipulationTools.stringReplace(s, ".gml", "");
			s = StringManipulationTools.stringReplace(s, ".graphml.gz", "");
			s = StringManipulationTools.stringReplace(s, ".graphml", "");
		}
		group1 = "";
		group2 = null;
		
		if (s.indexOf(";") < 0) {// old style
			if (s.indexOf(".") > 0)
				group1 = s.substring(0, s.indexOf("."));
			else
				group1 = null;
		} else {
			
			if (s.indexOf(";;") > 0) {// only one group
				group1 = s.substring(0, s.indexOf(";"));
				group2 = null;
			} else {
				if (s.indexOf(";") > 0) {
					group1 = s.substring(0, s.indexOf(";"));
					String s2 = s.substring(group1.length() + 1);
					if (s2.indexOf(";") > 0)
						group2 = s2.substring(0, s2.indexOf(";"));
					else
						group2 = null;
				}
			}
		}
	}
	
	public IOurl getURL() {
		return pathwayURL;
	}
	
	public String getGroup1() {
		return group1;
	}
	
	public String getGroup2() {
		return group2;
	}
	
	@Override
	public String toString() {
		String s = pathwayName.trim();
		if (!showGraphExtensions) {
			s = StringManipulationTools.stringReplace(s, ".gml.gz", "");
			s = StringManipulationTools.stringReplace(s, ".graphml.gz", "");
			s = StringManipulationTools.stringReplace(s, ".gml", "");
			s = StringManipulationTools.stringReplace(s, ".graphml", "");
		}
		if (group1 != null)
			s = StringManipulationTools.stringReplace(s, group1, "");
		if (group2 != null)
			s = StringManipulationTools.stringReplace(s, group2, "");
		s = StringManipulationTools.stringReplace(s, "..", "");
		s = StringManipulationTools.stringReplace(s, ";;", "");
		if (s.substring(0, 1).equals("."))
			s = s.substring(1, s.length());
		
		if (s.length() > 1)
			s = s.substring(0, 1).toUpperCase() + s.substring(1);
		return s;
	}
	
	public String getFileName() {
		return pathwayName; // +".GML";
	}
	
	public int compareTo(PathwayWebLinkItem o) {
		return pathwayName.compareTo(o.getFileName());
	}
}
