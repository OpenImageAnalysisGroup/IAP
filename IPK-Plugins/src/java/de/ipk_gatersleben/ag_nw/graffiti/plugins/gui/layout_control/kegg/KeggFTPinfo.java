package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class KeggFTPinfo {
	
	public static final boolean keggFTPavailable = false;
	
	private static final KeggFTPinfo instance = new KeggFTPinfo();
	
	private KeggFTPinfo() {
		// empty
	}
	
	public static KeggFTPinfo getInstance() {
		return instance;
	}
	
	// ftp://ftp.genome.jp/pub/kegg/xml/kgml/metabolic/organisms/hsa/
	// ftp://ftp.genome.jp/pub/kegg/xml/kgml/metabolic/ko/
	// ftp://ftp.genome.jp/pub/kegg/xml/kgml/non-metabolic/organisms/hsa/
	// ftp://ftp.genome.jp/pub/kegg/xml/kgml/non-metabolic/ko/
	
	HashMap<String, TreeSet<String>> org2list = new HashMap<String, TreeSet<String>>();
	
	private void init(String mapOrOrganismName, BackgroundTaskStatusProviderSupportingExternalCall status) {
		String val = mapOrOrganismName;
		if (val.startsWith("path:"))
			val = val.substring("path:".length());
		val = val.replaceAll("&amp;", "&");
		String map = StringManipulationTools.removeNumbersFromString(val);
		
		if (map.equals("map"))
			map = "ko";
		
		String keyM = map + "_metab";
		String keyNM = map + "_nonmetab";
		
		if (!org2list.containsKey(keyM) || !org2list.containsKey(keyNM)) {
			// String mapNumber = StringManipulationTools.getNumbersFromString(val);
			String mapID = map;
			if (!mapID.equals("ko") && !mapID.equals("ec"))
				mapID = "organisms/" + mapID;
			
			String metabolic = "metabolic";
			
			String url1 = "ftp://ftp.genome.jp"
					+ "/pub/kegg/xml/kgml/" + metabolic + "/" + mapID;// + "/" + map + mapNumber + ".xml";
			metabolic = "non-metabolic";
			
			String url2 = "ftp://ftp.genome.jp"
					+ "/pub/kegg/xml/kgml/" + metabolic + "/" + mapID;// + "/" + map + mapNumber + ".xml";
			
			if (keggFTPavailable) {
				BackgroundTaskHelper.lockAquire("ftpPathwayLookup", 1);
				if (!org2list.containsKey(keyM)) {
					ArrayList<String> filesMetabolic = GUIhelper.performDirectoryListing(url1, status);
					TreeSet<String> files = new TreeSet<String>(filesMetabolic);
					org2list.put(keyM, files);
				}
				if (!org2list.containsKey(keyNM)) {
					ArrayList<String> filesNonMetabolic = GUIhelper.performDirectoryListing(url2, status);
					TreeSet<String> files = new TreeSet<String>(filesNonMetabolic);
					org2list.put(map + "_nonmetab", files);
				}
				BackgroundTaskHelper.lockRelease("ftpPathwayLookup");
			}
		}
	}
	
	public boolean isMetabolic(String mapNumber, BackgroundTaskStatusProviderSupportingExternalCall status) {
		init(mapNumber, status);
		if (!keggFTPavailable)
			return false;
		String fn = getMapFromName(mapNumber) + StringManipulationTools.getNumbersFromString(mapNumber) + ".xml";
		boolean isMetabolic = org2list.get(getMapFromName(mapNumber) + "_metab").contains(fn);
		boolean isNonMetabolic = org2list.get(getMapFromName(mapNumber) + "_nonmetab").contains(fn);
		
		if (!isMetabolic && !isNonMetabolic) {
			if (keggFTPavailable)
				System.out.println("WARNING: pathway unknown: " + fn + " - decision about metabolic can't be made");
		}
		if (isMetabolic && isNonMetabolic) {
			System.out.println("WARNING: pathway is known as both, metabolic and non-metabolic: " + fn + " - decision about metabolic can't be made");
		}
		return isMetabolic;
	}
	
	public boolean isKnown(String mapNumber, BackgroundTaskStatusProviderSupportingExternalCall status) {
		init(mapNumber, status);
		String fn = getMapFromName(mapNumber) + StringManipulationTools.getNumbersFromString(mapNumber) + ".xml";
		boolean isMetabolic = org2list.get(getMapFromName(mapNumber) + "_metab") != null && org2list.get(getMapFromName(mapNumber) + "_metab").contains(fn);
		boolean isNonMetabolic = org2list.get(getMapFromName(mapNumber) + "_nonmetab") != null
				&& org2list.get(getMapFromName(mapNumber) + "_nonmetab").contains(fn);
		
		return isMetabolic || isNonMetabolic;
	}
	
	private String getMapFromName(String mapOrOrganismName) {
		String val = mapOrOrganismName;
		if (val.startsWith("path:"))
			val = val.substring("path:".length());
		val = val.replaceAll("&amp;", "&");
		String map = StringManipulationTools.removeNumbersFromString(val);
		
		if (map.equals("map"))
			map = "ko";
		return map;
	}
	
	public boolean isMetabolicOld(String mapNumber) {
		String[] group = KoService.getPathwayGroupFromMapNumber(mapNumber);
		if (group != null && group.length > 0) {
			return group[0].equals("Metabolism");
		}
		return false;
	}
}
