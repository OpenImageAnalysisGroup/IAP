/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.xml.rpc.ServiceException;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.LineProcessor;
import org.graffiti.graph.Node;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_brite.BriteService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;

/**
 * HTML Parser
 * 
 * @author Christian Klukas
 * @version $Revision: 1.6 $
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
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
	 * @throws ServiceException
	 * @throws ServiceException
	 * @throws Exception
	 */
	public Collection<KeggPathwayEntry> getXMLpathways(
			OrganismEntry organism, boolean stripOrganismName,
			BackgroundTaskStatusProviderSupportingExternalCall status)
			throws Exception {
		Collection<KeggPathwayEntry> result = list_pathways(organism.getShortName(), stripOrganismName);
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
	
	// public static PathwayElement[] getKeggElmentsOfMap(String pathwayId) {
	// KEGGLocator locator = new KEGGLocator();
	// try {
	// KEGGPortType serv = locator.getKEGGPort();
	// PathwayElement[] pe = serv.get_elements_by_pathway(pathwayId);
	// return pe;
	// } catch (ServiceException e) {
	// ErrorMsg.addErrorMessage(e);
	// } catch (RemoteException e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// return null;
	// }
	//
	public static String[] getKeggEnzymesOfMap(String pathwayId) {
		try {
			String[] pe = KeggHelper.get_enzymes_by_pathway(pathwayId);
			return pe;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggKOsOfMap(String pathwayId) {
		try {
			String[] pe = get_kos_by_pathway(pathwayId);
			return pe;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggEnzymesByReactionId(String reactionId) {
		try {
			String[] pe = get_enzymes_by_reaction(reactionId);
			return pe;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggReactionsOfMap(String pathwayId) {
		try {
			String[] pe = get_reactions_by_pathway(pathwayId);
			return pe;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	// public static String callKeggDBGETbfind(String search) {
	// KEGGLocator locator = new KEGGLocator();
	// try {
	// KEGGPortType serv = locator.getKEGGPort();
	// return serv.bfind(search);
	// } catch (ServiceException e) {
	// ErrorMsg.addErrorMessage(e);
	// } catch (RemoteException e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// return null;
	// }
	//
	// public static String callKeggDBGETbget(String search) {
	// KEGGLocator locator = new KEGGLocator();
	// try {
	// KEGGPortType serv = locator.getKEGGPort();
	// return serv.bget(search);
	// } catch (ServiceException e) {
	// ErrorMsg.addErrorMessage(e);
	// } catch (RemoteException e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// return null;
	// }
	//
	// public static String callKeggDBGETbtit(String search) {
	// KEGGLocator locator = new KEGGLocator();
	// try {
	// KEGGPortType serv = locator.getKEGGPort();
	// return serv.btit(search);
	// } catch (ServiceException e) {
	// ErrorMsg.addErrorMessage(e);
	// } catch (RemoteException e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// return null;
	// }
	//
	// public static String callKeggDBGETbinfo(String search) {
	// KEGGLocator locator = new KEGGLocator();
	// try {
	// KEGGPortType serv = locator.getKEGGPort();
	// return serv.binfo(search);
	// } catch (ServiceException e) {
	// ErrorMsg.addErrorMessage(e);
	// } catch (RemoteException e) {
	// ErrorMsg.addErrorMessage(e);
	// }
	// return null;
	// }
	//
	public static String[] getKeggCompoundsOfMap(String pathwayId) {
		try {
			String[] pe = get_compounds_by_pathway(pathwayId);
			return pe;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	public static String[] getKeggGenesOfMap(String pathwayId) {
		try {
			String[] pe = get_genes_by_pathway(pathwayId);
			return pe;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	private static String getDigits(String mapNumber) {
		String result = "";
		for (char c : mapNumber.toCharArray()) {
			if (Character.isDigit(c))
				result = result + c;
		}
		return result;
	}
	
	private static Collection<OrganismEntry> cachedOrganismList = new ArrayList<OrganismEntry>();
	
	public synchronized Collection<OrganismEntry> getOrganisms() throws Exception {
		if (cachedOrganismList.size() > 2) {
			return cachedOrganismList;
		}
		ArrayList<OrganismEntry> result = new ArrayList<OrganismEntry>();
		if (KeggFTPinfo.keggFTPavailable) {
			OrganismEntry mapEnty = new OrganismEntry("map", "Reference Pathways (MAP)", "Reference");
			result.add(mapEnty);
		}
		OrganismEntry koEnty = new OrganismEntry("ko", "Reference Pathways (KO)", "Reference");
		result.add(koEnty);
		OrganismEntry rnEnty = new OrganismEntry("ec", "Reference Pathways (EC)", "Reference");
		result.add(rnEnty);
		// OrganismEntry otEnty = new OrganismEntry("ko", "Reference Pathways (OT)");
		// result.add(otEnty);
		try {
			// http://rest.kegg.jp/list/organism
			// T01001 hsa Homo sapiens (human) Eukaryotes;Animals;Vertebrates;Mammals
			// T01005 ptr Pan troglodytes (chimpanzee) Eukaryotes;Animals;Vertebrates;Mammals
			// T02283 pps Pan paniscus (bonobo) Eukaryotes;Animals;Vertebrates;Mammals
			// T02442 ggo Gorilla gorilla gorilla (western lowland gorilla) Eukaryotes;Animals;Vertebrates;Mammals
			// T01416 pon Pongo abelii (Sumatran orangutan) Eukaryotes;Animals;Vertebrates;Mammals
			GravistoService.setProxy();
			Collection<OrganismEntry> orgs = list_organisms();
			for (OrganismEntry oe : orgs)
				result.add(oe);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e.getMessage());
		}
		cachedOrganismList.clear();
		cachedOrganismList.addAll(result);
		return result;
	}
	
	public Collection<String> getLinkedPathwayIDs(String pathway_id)
			throws IOException, ServiceException {
		ArrayList<String> result = new ArrayList<String>();
		if (!pathway_id.startsWith("path:"))
			pathway_id = "path:" + pathway_id;
		try {
			String[] links = get_linked_pathways(pathway_id);
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
			@Override
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
	
	private Collection<KeggPathwayEntry> list_pathways(String shortName,
			final boolean stripOrganismName) throws Exception {
		// REST API: http://rest.kegg.jp/list/pathway/hsa
		// http://rest.kegg.jp/list/pathway
		final Collection<KeggPathwayEntry> pathways = new ArrayList<KeggPathwayEntry>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/list/pathway/" + shortName),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						pathways.add(new KeggPathwayEntry(fields[1], stripOrganismName, fields[0],
								new String[] { "Unknwon", "Pathway Group" }));
					}
				});
		
		return pathways;
	}
	
	public Collection<OrganismEntry> list_organisms() throws Exception {
		// http://rest.kegg.jp/list/organism
		final Collection<OrganismEntry> orgs = new ArrayList<OrganismEntry>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/list/organism"),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						orgs.add(new OrganismEntry(fields[1], fields[2], fields[3].replace(";", "/")));
					}
				});
		return orgs;
	}
	
	public static String[] get_kos_by_pathway(String pathwayId) throws Exception {
		// REST API: http://rest.kegg.jp/link/ko/map00010
		final Collection<String> kos = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/link/ko/" + pathwayId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						kos.add(fields[1]);
					}
				});
		return kos.toArray(new String[] {});
	}
	
	public static String[] get_enzymes_by_pathway(String pathwayId) throws Exception {
		// REST API: http://rest.kegg.jp/link/enzyme/map00010
		final Collection<String> enzymes = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/link/enzyme/" + pathwayId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						enzymes.add(fields[1]);
					}
				});
		return enzymes.toArray(new String[] {});
	}
	
	public static String[] get_reactions_by_pathway(String pathwayId) throws Exception {
		// REST API: http://rest.kegg.jp/link/rn/map00010
		final Collection<String> rns = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/link/rn/" + pathwayId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						rns.add(fields[1]);
					}
				});
		return rns.toArray(new String[] {});
	}
	
	public static String[] get_genes_by_pathway(String pathwayId) throws Exception {
		// REST API: http://rest.kegg.jp/link/genes/hsa00010
		final Collection<String> genes = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/link/genes/" + pathwayId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						genes.add(fields[1]);
					}
				});
		return genes.toArray(new String[] {});
	}
	
	public static String[] get_compounds_by_pathway(String pathwayId) throws Exception {
		// REST API: http://rest.kegg.jp/link/compound/map00010
		final Collection<String> compounds = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/link/compound/" + pathwayId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						compounds.add(fields[1]);
					}
				});
		return compounds.toArray(new String[] {});
	}
	
	public static String[] get_glycans_by_pathway(String pathwayId) throws Exception {
		// REST API: (does not work: http://rest.kegg.jp/link/glycan/hsa00020 )
		if (true)
			throw new UnsupportedOperationException("ToDo: add correct call to remote API");
		final Collection<String> glycans = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/link/glycan/" + pathwayId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						glycans.add(fields[1]);
					}
				});
		return glycans.toArray(new String[] {});
	}
	
	public static String[] get_genes_by_ko(String keggID, String org) throws Exception {
		// REST API: http://rest.kegg.jp/find/genes/K00400+mmp
		final Collection<String> genes = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl("http://rest.kegg.jp/find/genes/" + keggID + "+" + org),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						genes.add(fields[0]);
					}
				});
		return genes.toArray(new String[] {});
	}
	
	public static String[] get_enzymes_by_reaction(String reactionId) throws Exception {
		// REST API: http://rest.kegg.jp/link/enzyme/rn:R01070
		final Collection<String> enzymes = new ArrayList<String>();
		GravistoService.processUrlTextContent(new IOurl(" http://rest.kegg.jp/link/enzyme/" + reactionId),
				new LineProcessor() {
					@Override
					public void process(String line) {
						String[] fields = line.split("\t");
						enzymes.add(fields[1]);
					}
				});
		return enzymes.toArray(new String[] {});
	}
	
	private String[] get_linked_pathways(String pathway_id) {
		// REST API: Unclear (http://rest.kegg.jp/link/pathway/map00010 does not work)
		throw new UnsupportedOperationException("ToDo: add correct call to remote API");
	}
}
