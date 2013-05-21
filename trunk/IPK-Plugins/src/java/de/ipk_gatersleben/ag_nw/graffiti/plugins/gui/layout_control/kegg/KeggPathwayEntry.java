/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.11.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.plugin.io.resources.IOurl;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class KeggPathwayEntry implements Comparable<Object> {
	private URL pathwayURL;
	
	private String pathwayName;
	private String mapName;
	
	private boolean stripOrganismName;
	private boolean colorEnzymesAndUseReferencePathway;
	private String mappingCount = "";
	
	private String[] group;
	
	private InputStream openInputStream = null;
	
	private Vector2d targetPosition;
	
	public KeggPathwayEntry(String name, boolean stripOrganismName, String mapName, String[] group) {
		try {
			pathwayURL = getWebURL(mapName);
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage("Could not create URL for map: " + mapName);
		}
		setPathwayName(name.trim());
		setStripOrganismName(stripOrganismName);
		setMapName(mapName);
		setGroupName(group);
	}
	
	public static URL getWebURL(String mapName) throws MalformedURLException {
		String val = mapName;
		if (val.startsWith("path:"))
			val = val.substring("path:".length());
		val = val.replaceAll("&amp;", "&");
		String map = StringManipulationTools.removeNumbersFromString(val);
		String mapNumber = StringManipulationTools.getNumbersFromString(val);
		if (!KeggFTPinfo.keggFTPavailable) {
			// http://www.genome.jp/kegg-bin/download?entry=hsa00010&format=kgml
			URL url = new URL("http://rest.kegg.jp/get/" + map + mapNumber + "/kgml");
			return url;
		} else {
			URL url = new URL("http://rest.kegg.jp"
					+ "/get/" + map + "/" + map + mapNumber + "/kgml");
			
			if (KeggHelper.getKgmlVersion().equals("0.7.0")) {
				boolean isMetab = KeggFTPinfo.getInstance().isMetabolic(mapName, null);
				// ftp://ftp.genome.jp/pub/kegg/xml/kgml/metabolic/organisms/hsa/
				// ftp://ftp.genome.jp/pub/kegg/xml/kgml/metabolic/ko/
				// ftp://ftp.genome.jp/pub/kegg/xml/kgml/non-metabolic/organisms/hsa/
				// ftp://ftp.genome.jp/pub/kegg/xml/kgml/non-metabolic/ko/
				if (map.equals("map") && !isMetab)
					map = "ko";
				if (map.equals("map") && isMetab)
					map = "ko"; // "ec"
				String mapID = map;
				if (!mapID.equals("ko") && !mapID.equals("ec"))
					mapID = "organisms/" + mapID;
				
				// String metabolic = isMetab ? "metabolic" : "non-metabolic";
				
				url = new URL("http://rest.kegg.jp"
						+ "/get/" + map + mapNumber + "/kgml");
			}
			return url;
		}
	}
	
	public static KeggPathwayEntry getKeggPathwayEntryFromMap(String mapName) {
		String val = mapName;
		if (val.startsWith("path:"))
			val = val.substring("path:".length());
		val = val.replaceAll("&amp;", "&");
		
		getOrganismLettersFromMapId(val);
		String mapNumber = val;
		return new KeggPathwayEntry(mapName + " - " + mapNumber,
				false, mapNumber,
				KeggHelper.getGroupFromMapNumber(mapNumber, mapName)
		// KeggHelper.getGroupFromMapName(mapName)
		);
	}
	
	public KeggPathwayEntry(InputStream inputStream) {
		this.openInputStream = inputStream;
	}
	
	public KeggPathwayEntry(KeggPathwayEntry copyThisEntry, boolean colorEnzymesAndUseReferencePathway) {
		this(
				copyThisEntry.getPathwayName(),
				copyThisEntry.isStripOrganismName(),
				copyThisEntry.getMapName(),
				copyThisEntry.getGroupName());
		setColorEnzymesAndUseReferencePathway(colorEnzymesAndUseReferencePathway);
	}
	
	public String[] getGroupName() {
		return group;
	}
	
	public void setGroupName(String[] group) {
		this.group = group;
	}
	
	public String getPathwayURLstring() {
		if (pathwayURL == null)
			return null;
		// if (!FileDownloadCache.isCacheURL(pathwayURL))
		// pathwayURL = FileDownloadCache.getCacheURL(pathwayURL, mapName);
		
		if (pathwayURL == null)
			return null;
		else
			return pathwayURL.toString();
	}
	
	public URL getPathwayURL() {
		return getPathwayURL(false);
	}
	
	public URL getPathwayURL(boolean useReferencePathwayURL) {
		if (useReferencePathwayURL)
			return getReferencePathwayURLfromURL(pathwayURL);
		else {
			if (!FileDownloadCache.isCacheURL(pathwayURL))
				pathwayURL = FileDownloadCache.getCacheURL(pathwayURL, mapName);
			
			return pathwayURL;
		}
	}
	
	// public Object getPathwayURLstring(boolean returnOrganismSpecificURL) {
	// if (returnOrganismSpecificURL) {
	// if (!FileDownloadCache.isCacheURL(pathwayURL))
	// pathwayURL = FileDownloadCache.getCacheURL(pathwayURL, mapName);
	// return pathwayURL;
	// }
	// else
	// return getPathwayURL();
	// }
	
	private URL getReferencePathwayURLfromURL(URL pathwayURL) {
		try {
			String organismName3letters = mapName.substring(0, 3);
			String file = pathwayURL.getFile();
			file = StringManipulationTools.stringReplace(file, "/" + organismName3letters, "/map");
			URL url = new URL(
					pathwayURL.getProtocol(),
					pathwayURL.getHost(),
					pathwayURL.getPort(),
					file
					);
			if (!FileDownloadCache.isCacheURL(url))
				url = FileDownloadCache.getCacheURL(url, mapName);
			return url;
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public String toString() {
		String tempPathwayName = getPathwayName();
		if (isStripOrganismName() && tempPathwayName.lastIndexOf(" -") > 0 && tempPathwayName.length() > 0)
			tempPathwayName = tempPathwayName.substring(0, tempPathwayName.lastIndexOf(" -"));
		if (mappingCount.equalsIgnoreCase(""))
			return tempPathwayName + "                         ";
		else
			return tempPathwayName + " (" + mappingCount + ")";
	}
	
	public void setMappingCount(String mappingCount) {
		this.mappingCount = mappingCount;
	}
	
	public InputStream getOpenInputStream() throws Exception {
		if (openInputStream != null)
			return openInputStream;
		else {
			if (!isColorEnzymesAndUseReferencePathway()) {
				URL url = getPathwayURL();
				if (url != null)
					return new IOurl(url.toString()).getInputStream();
				else
					return null;
			} else
				return new IOurl(getPathwayURL(true).toString()).getInputStream();
		}
	}
	
	/**
	 * @param string
	 * @return
	 */
	public String getMappingCountDescription(String pre) {
		if (mappingCount != null && mappingCount.length() > 0)
			return pre + mappingCount;
		else
			return "";
	}
	
	public void setPathwayName(String pathwayName) {
		this.pathwayName = pathwayName;
	}
	
	public String getPathwayName() {
		return pathwayName;
	}
	
	public void setMapName(String mapName) {
		this.mapName = mapName;
	}
	
	public String getMapName() {
		return mapName;
	}
	
	private void setStripOrganismName(boolean stripOrganismName) {
		this.stripOrganismName = stripOrganismName;
	}
	
	private boolean isStripOrganismName() {
		return stripOrganismName;
	}
	
	public void setColorEnzymesAndUseReferencePathway(boolean colorEnzymesAndUseReferencePathway) {
		this.colorEnzymesAndUseReferencePathway = colorEnzymesAndUseReferencePathway;
	}
	
	public boolean isColorEnzymesAndUseReferencePathway() {
		return colorEnzymesAndUseReferencePathway;
	}
	
	public boolean isReferencePathway() {
		String organismName3letters = getOrganismLetters();
		return organismName3letters.equalsIgnoreCase("map") || organismName3letters.equalsIgnoreCase("ko");
	}
	
	public String getOrganismLetters() {
		return getOrganismLettersFromMapId(mapName);
	}
	
	public static String getOrganismLettersFromMapId(String mapName) {
		String id = mapName;
		if (id.length() >= 2) {
			char[] name = id.toCharArray();
			int lastDigit = id.length() - 1;
			while (lastDigit >= 0 && Character.isDigit(name[lastDigit]))
				lastDigit--;
			if (!Character.isDigit(name[lastDigit]))
				lastDigit++;
			if (lastDigit < id.length()) {
				id.substring(lastDigit);
				id = id.substring(0, lastDigit);
			}
		}
		return id;
	}
	
	public Vector2d getTargetPosition() {
		return targetPosition;
	}
	
	public void setTargetPosition(Vector2d targetPosition) {
		this.targetPosition = targetPosition;
	}
	
	public URL getWebURL() {
		try {
			return getWebURL(mapName);
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public int compareTo(Object o) {
		KeggPathwayEntry kpe = (KeggPathwayEntry) o;
		if (getOrganismLetters().equals("map") && kpe.getOrganismLetters().equals("ko"))
			return -1;
		if (getOrganismLetters().equals("ko") && kpe.getOrganismLetters().equals("map"))
			return 1;
		if (getOrganismLetters().equals("map") && !kpe.getOrganismLetters().equals("map"))
			return -1;
		if (getOrganismLetters().equals("ko") && !kpe.getOrganismLetters().equals("ko"))
			return -1;
		return getMapName().compareTo(kpe.getMapName());
	}
}
