/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.xml.rpc.ServiceException;

import keggapi.Definition;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.PathwayElement;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_brite.BriteService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;

/**
 * HTML Parser
 * 
 * @author Christian Klukas
 * @version $Revision: 1.3 $
 */
public class KeggHelper implements HelperClass {
	
	private static String kgmlVersion = "0.6.1";
	
	/**
	 * @return True, if the pathways for this version are provided by KEGG via FTP.
	 */
	private boolean isKEGGftpDownloadVersion(String kgmlVersion) {
		if (KeggFTPinfo.keggFTPavailable)
			return kgmlVersion.equals("0.7.0");
		else
			return false;
	}
	
	/**
	 * @param status
	 * @return Vector full of <code>KeggPathwayEntry</code>s
	 * @throws IOException
	 *            An exception.
	 * @throws ServiceException
	 * @throws ServiceException
	 * @throws ServiceException
	 */
	public Collection<KeggPathwayEntry> getXMLpathways(
			String serverURL, OrganismEntry organism, boolean stripOrganismName,
			BackgroundTaskStatusProviderSupportingExternalCall status)
			throws IOException, ServiceException {
		
		KEGGLocator locator = new KEGGLocator();
		KEGGPortType serv = locator.getKEGGPort();
		Definition[] def = serv.list_pathways(organism.getShortName());
		ArrayList<KeggPathwayEntry> result = new ArrayList<KeggPathwayEntry>();
		int unknown = 0;
		if (def != null)
			for (int i = 0; i < def.length; i++) {
				String mapNumber = def[i].getEntry_id().replaceFirst("path:", "");
				boolean ok = true;
				if (isKEGGftpDownloadVersion(kgmlVersion)) {
					ok = KeggFTPinfo.getInstance().isKnown(mapNumber, status);
					if (!KeggFTPinfo.keggFTPavailable)
						ok = true;
					if (!ok)
						unknown++;
				}
				
				if (ok)
					result.add(
							new KeggPathwayEntry(
									def[i].getDefinition(),
									stripOrganismName,
									mapNumber,
									getGroupFromMapNumber(mapNumber, def[i].getDefinition())
							// getGroupFromMapName(def[i].getDefinition())
							));
			}
		if (unknown > 0) {
			System.out.println("Information: based on FTP directory listing " + unknown + " patways, included in SOAP return are filtered out for display.");
		}
		return result;
	}
	
	public static String[] getGroupFromMapNumber(String mapNumber, String mapName) {
		if (!KoService.isExternalKoFileAvailable()) {
			String[] rr = BriteService.getPathwayGroupFromMapNumber(getDigits(mapNumber));
			if (rr != null)
				return rr;
			
			if (mapName.indexOf(" - ") > 0) {
				mapName = mapName.substring(0, mapName.lastIndexOf(" - "));
			}
			if (mapName.indexOf(" - ") > 0) {
				String s1 = mapName.substring(0, mapName.indexOf(" - ")).trim();
				return new String[] { "KO db file not available!", s1 };
			} else
				return new String[] { "KO db file not available!", "unknown group" };
		}
		String[] result = KoService.getPathwayGroupFromMapNumber(getDigits(mapNumber));
		if (result != null && result.length > 0)
			return result;
		else {
			if (mapName.indexOf(" - ") > 0) {
				mapName = mapName.substring(0, mapName.lastIndexOf(" - "));
			}
			if (mapName.indexOf(" - ") > 0) {
				String s1 = mapName.substring(0, mapName.indexOf(" - ")).trim();
				return new String[] { "not found", s1 };
			} else
				return new String[] { "not found", "unknown group" };
		}
	}
	
	public static PathwayElement[] getKeggElmentsOfMap(String pathwayId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			PathwayElement[] pe = serv.get_elements_by_pathway(pathwayId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggEnzymesOfMap(String pathwayId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			String[] pe = serv.get_enzymes_by_pathway(pathwayId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggKOsOfMap(String pathwayId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			String[] pe = serv.get_kos_by_pathway(pathwayId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggEnzymesByReactionId(String reactionId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			String[] pe = serv.get_enzymes_by_reaction(reactionId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggReactionsOfMap(String pathwayId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			String[] pe = serv.get_reactions_by_pathway(pathwayId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String callKeggDBGETbfind(String search) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			return serv.bfind(search);
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String callKeggDBGETbget(String search) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			return serv.bget(search);
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String callKeggDBGETbtit(String search) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			return serv.btit(search);
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String callKeggDBGETbinfo(String search) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			return serv.binfo(search);
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggCompoundsOfMap(String pathwayId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			String[] pe = serv.get_compounds_by_pathway(pathwayId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggGenesOfMap(String pathwayId) {
		KEGGLocator locator = new KEGGLocator();
		try {
			KEGGPortType serv = locator.getKEGGPort();
			String[] pe = serv.get_genes_by_pathway(pathwayId);
			return pe;
		} catch (ServiceException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (RemoteException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	// public static String[] getGroupFromMapName(String mapName) {
	// if (mapName.contains("-"))
	// mapName = mapName.substring(0, mapName.indexOf("-"));
	// mapName = mapName.trim();
	// ClassLoader cl = KeggHelper.class.getClassLoader();
	// String path = KeggHelper.class.getPackage().getName().replace('.', '/');
	// InputStream inp = cl.getResourceAsStream(path+"/ko.txt");
	// BufferedReader br = new BufferedReader(new InputStreamReader(inp));
	// String line = null;
	// String lastH1 = null;
	// String lastSubCatH2 = null;
	// String lastPathwayH3 = null;
	// try {
	// while ((line = br.readLine())!=null) {
	// if (line.startsWith("	")) {
	// // "	"
	// lastPathwayH3 = line.substring("	".length());
	// if (lastPathwayH3.indexOf("	")>0)
	// lastPathwayH3 = lastPathwayH3.substring(0, lastPathwayH3.indexOf("	"));
	// if (lastPathwayH3.indexOf(mapName)>=0) {
	// String[] result = { lastH1, lastSubCatH2 };
	// br.close();
	// return result;
	// }
	// } else
	// if (line.indexOf(". ")>0)
	// lastH1 = line;
	// else
	// lastSubCatH2 = line;
	//
	// }
	// br.close();
	// } catch (IOException e) {
	// ErrorMsg.addErrorMessage(e);
	// return new String[] { "KO not available", "Unknown Category (IO Error)"};
	// }
	// return new String[] { "KO not available", "(non-specific)"};
	// }
	
	private static String getDigits(String mapNumber) {
		String result = "";
		for (char c : mapNumber.toCharArray()) {
			if (Character.isDigit(c))
				result = result + c;
		}
		return result;
	}
	
	private static Collection<OrganismEntry> cachedOrganismList = new ArrayList<OrganismEntry>();
	
	public synchronized Collection<OrganismEntry> getOrganisms()
			throws IOException, ServiceException {
		if (cachedOrganismList.size() > 1) {
			return cachedOrganismList;
		}
		KEGGLocator locator = new KEGGLocator();
		KEGGPortType serv = locator.getKEGGPort();
		ArrayList<OrganismEntry> result = new ArrayList<OrganismEntry>();
		if (KeggFTPinfo.keggFTPavailable) {
			OrganismEntry mapEnty = new OrganismEntry("map", "Reference Pathways (MAP)");
			result.add(mapEnty);
		}
		OrganismEntry koEnty = new OrganismEntry("ko", "Reference Pathways (KO)");
		result.add(koEnty);
		OrganismEntry rnEnty = new OrganismEntry("ec", "Reference Pathways (EC)");
		result.add(rnEnty);
		// OrganismEntry otEnty = new OrganismEntry("ko", "Reference Pathways (OT)");
		// result.add(otEnty);
		try {
			Definition[] def = serv.list_organisms();
			for (int i = 0; i < def.length; i++)
				result.add(new OrganismEntry(def[i].getEntry_id(), def[i].getDefinition()));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(StringManipulationTools.stringReplace(e.getMessage(), "java.net.", ""));
		}
		cachedOrganismList.clear();
		cachedOrganismList.addAll(result);
		return result;
	}
	
	public Collection<String> getLinkedPathwayIDs(String pathway_id)
			throws IOException, ServiceException {
		ArrayList<String> result = new ArrayList<String>();
		KEGGLocator locator = new KEGGLocator();
		KEGGPortType serv = locator.getKEGGPort();
		if (!pathway_id.startsWith("path:"))
			pathway_id = "path:" + pathway_id;
		try {
			String[] links = serv.get_linked_pathways(pathway_id);
			for (String l : links)
				result.add(l.replaceFirst("path:", ""));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(StringManipulationTools.stringReplace(e.getMessage(), "java.net.", ""));
		}
		return result;
	}
	
	public static boolean isEnzymeNode(Node n) {
		String type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", "", "");
		return type.equalsIgnoreCase("enzyme");
	}
	
	public static boolean isMapNode(Node n) {
		String type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", "", "");
		return type.equalsIgnoreCase("map");
	}
	
	public static String getKeggId(Node n) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", "", "");
	}
	
	public static boolean isMapTitleNode(Node n) {
		String type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", "", "");
		return type.equalsIgnoreCase("map") && AttributeHelper.getLabel(n, "").startsWith("TITLE:");
	}
	
	public static void setKgmlVersion(String kgmlVersion) {
		KeggHelper.kgmlVersion = kgmlVersion;
	}
	
	public static String getKgmlVersion() {
		return kgmlVersion;
	}
	
	private static ArrayList<JComboBox> knownLists = new ArrayList<JComboBox>();
	
	public static JComponent getKGMLversionSelectionCombobox() {
		final JComboBox result = new JComboBox(new String[] { "0.7.0", "0.6.1", "0.6", "0.5", "0.4", "0.3", "0.2", "0.1" });
		result.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String sel = (String) result.getSelectedItem();
				setKgmlVersion(sel);
				for (JComboBox jc : knownLists) {
					if (jc != result)
						jc.setSelectedItem(sel);
				}
			}
		});
		knownLists.add(result);
		JLabel lbl = new JLabel(" v");
		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		return TableLayout.getSplit(
				KeggFTPinfo.keggFTPavailable ? lbl : null,
				KeggFTPinfo.keggFTPavailable ?
						result : null, // new JLabel("<html><small>&nbsp;(in case of problems,<br>&nbsp;select 0.6 instead of 0.6.1)"),
				TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED); // , TableLayout.PREFERRED);
	}
}
