/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 19.05.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import java.util.Iterator;
import java.util.List;

import org.AttributeHelper;
import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsFalse;
import org.StringManipulationTools;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.tool.AbstractTool;
import org.graffiti.plugins.modes.defaults.MegaMoveTool;
import org.graffiti.plugins.views.defaults.NodeComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;

/**
 * @author klukas
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class IPKnodeComponent extends NodeComponent {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public String getToolTipText() {
		String t = getTooltipForNode((Node) graphElement, true);
		
		String result = altLabels(graphElement, t != null && !t.contains("<table>")) + (t == null ? "" : t);
		return result;
	}
	
	private String altLabels(GraphElement graphElement, boolean br) {
		StringBuilder sb = new StringBuilder();
		for (String s : AttributeHelper.getLabels(graphElement, false)) {
			if (s == null || s.trim().length() == 0)
				continue;
			if (sb.length() > 0)
				sb.append(" / ");
			sb.append(s);
		}
		String ss = sb.toString();
		return "<html>" + ss + (ss.length() > 0 && br ? "<br>" : "");
	}
	
	public static String getTooltipForNode(Node n, boolean includeClickHelp) {
		String nodeToolTip = AttributeHelper.getToolTipText(n);
		
		if (nodeToolTip != null && nodeToolTip.length() > 0)
			return nodeToolTip;
		
		String compOrEnzInfo = getEnzymeToolTip(n, null);
		if (compOrEnzInfo == null)
			compOrEnzInfo = getCompoundToolTip(n, null);
		
		String result;
		if (nodeToolTip != null || compOrEnzInfo != null) {
			result = "<table>";
			if (nodeToolTip != null)
				result += "<tr><td>" + nodeToolTip + "</td></tr>";
			if (compOrEnzInfo != null)
				result += "<tr><td>" + compOrEnzInfo + "</td></tr>";
			
			if (n.getDegree() > 0 && n.getDegree() <= 100)
				result += "<tr><td><small>" + getNeighbourInformation(n, includeClickHelp) + "</small></td><tr>";
			result += "</table></html>";
		} else {
			if (n.getDegree() > 0) {
				if (n.getDegree() > 100) {
					result = doubleClickHelp(n, includeClickHelp) + "<small>Degree: " + n.getDegree() + "</small>";
				} else {
					result = "<small>" + getNeighbourInformation(n, includeClickHelp) + "</small>";
				}
			} else
				result = doubleClickHelp(n, includeClickHelp).length() > 0 ? StringManipulationTools.stringReplace(doubleClickHelp(n, includeClickHelp), " |", "")
						: null;
		}
		return result;
	}
	
	private static String doubleClickHelp(Node n, boolean doubleClickHelp) {
		if (n == null || ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR || !doubleClickHelp)
			return "";
		try {
			boolean isMapNode = KeggHelper.isMapNode(n);
			if (isMapNode && KeggHelper.isMapTitleNode(n))
				return "Double-click to fold pathway / edit entries | ";
			if (isMapNode)
				return "Double-click to unfold pathway / edit entries | ";
			else
				return "Double-click to edit entry | ";
		} catch (Exception e) {
			return "";
		}
	}
	
	private static String getNeighbourInformation(Node n, boolean includeClickHelp) {
		String result;
		StringBuilder nl = new StringBuilder();
		String mainlbl = AttributeHelper.getLabel(n, "node");
		if (mainlbl.length() <= 0)
			mainlbl = "[]";
		mainlbl = mainlbl.replaceAll("<p>", "");
		mainlbl = mainlbl.replaceAll("<center>", "");
		mainlbl = mainlbl.replaceAll("<br>", "");
		mainlbl = mainlbl.replaceAll("<html>", "");
		int hiddenNodes = 0;
		for (Node m : n.getNeighbors()) {
			String lbl = AttributeHelper.getLabel(m, "[n/a]");
			if (lbl.length() <= 0)
				lbl = "[]";
			lbl = lbl.replaceAll("<p>", "");
			lbl = lbl.replaceAll("<center>", "");
			lbl = lbl.replaceAll("<br>", "");
			lbl = lbl.replaceAll("<html>", "");
			if (nl.length() > 0)
				nl.append(", ");
			if (AttributeHelper.isHiddenGraphElement(m)) {
				hiddenNodes++;
				nl.append(lbl + " (hidden)");
			} else
				nl.append(lbl);
		}
		result = doubleClickHelp(n, includeClickHelp) + "Degree: " + n.getDegree() + ", " + mainlbl + " is connected to: " + nl.toString()
				+ (hiddenNodes > 0 ? " (" +
						getHiddenText(hiddenNodes, includeClickHelp) + ")" : "");
		return result;
	}
	
	private static String getHiddenText(int hiddenNodes, boolean includeHiddenTextInformation) {
		if (!includeHiddenTextInformation)
			return "";
		if (AbstractTool.getActiveTool() == null || (!(AbstractTool.getActiveTool() instanceof MegaMoveTool))) {
			if (hiddenNodes == 0)
				return "0 nodes are hidden";
			if (hiddenNodes == 1)
				return "1 connected node is hidden, enable Move-Tool and use Shift+Double Mouse Click to make child node(s) visible";
			return hiddenNodes + " conntected nodes are hidden, enable Move-Tool and use Shift+Double Mouse Click to make child nodes visible";
		}
		if (hiddenNodes == 0)
			return "0 nodes are hidden";
		if (hiddenNodes == 1)
			return "1 conntected node is hidden, use Shift+Double Mouse Click to make child node(s) visible";
		return hiddenNodes + " conntected nodes are hidden, use Shift+Double Mouse Click to make child nodes visible";
	}
	
	private static String getEnzymeToolTip(Node n, String useThisId) {
		if (!new SettingsHelperDefaultIsFalse().isEnabled("grav_view_database_node_status"))
			return null;
		String resultAenzymeClassInfo;
		String resultBenzymeEntryInfo;
		
		String substanceName;
		if (useThisId == null)
			substanceName = AttributeHelper.getLabel(n, null);
		else
			substanceName = useThisId;
		
		if (substanceName != null) {
			// Retrieve enzyme-class information
			EnzymeEntry eze = EnzymeService.getEnzymeInformation(substanceName, true);
			List<String> classes = EnzymeService.getEnzymeClasses(substanceName, true);
			if (classes.size() <= 0) {
				resultAenzymeClassInfo = null;
			} else {
				String showName = substanceName;
				if (eze != null)
					showName = "EC " + eze.getID();
				resultAenzymeClassInfo = "<b>" + showName + "</b> ";
				for (Iterator<String> it = classes.iterator(); it.hasNext();) {
					String entry = (String) it.next();
					if (it.hasNext()) {
						resultAenzymeClassInfo += entry + " // ";
					} else
						resultAenzymeClassInfo += entry;
				}
				resultAenzymeClassInfo += "<br><font color=gray>"
						+ "<small>ENZYME nomenclature database - Swiss Institute of Bioinformatics ("
						+ EnzymeService.getReleaseVersionForEnzymeClasses() + ")"
						+ "</small></font>";
			}
			// Retrieve enzyme information
			if (eze == null) {
				resultBenzymeEntryInfo = null;
				String keggId = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", null, "");
				if (keggId != null && keggId.length() > 0 && useThisId == null)
					return getEnzymeToolTip(n, keggId);
			} else {
				resultBenzymeEntryInfo = "<b>" + eze.getDE() + "</b><br>";
				String syn = "";
				for (Iterator<?> it = eze.getAN().iterator(); it.hasNext();) {
					String entry = (String) it.next();
					if (it.hasNext()) {
						syn += entry + ", ";
					} else
						syn += entry;
				}
				if (syn.length() > 0)
					resultBenzymeEntryInfo += "<small>Synonyms: " + syn
							+ "</small><br>";
				
				String ca = "";
				for (Iterator<?> it = eze.getCA().iterator(); it.hasNext();) {
					String entry = (String) it.next();
					if (it.hasNext()) {
						ca += entry + " // ";
					} else
						ca += entry;
				}
				if (ca.length() > 0)
					resultBenzymeEntryInfo += "<small>Catalytic act.: " + ca
							+ "</small><br>";
				
				String cf = "";
				for (Iterator<?> it = eze.getCF().iterator(); it.hasNext();) {
					String entry = (String) it.next();
					if (it.hasNext()) {
						cf += entry + " // ";
					} else
						cf += entry;
				}
				if (cf.length() > 0)
					resultBenzymeEntryInfo += "<small>Cofactor(s): " + cf
							+ "</small><br>";
				
				String cc = "";
				for (Iterator<?> it = eze.getCC().iterator(); it.hasNext();) {
					String entry = (String) it.next();
					if (it.hasNext()) {
						cc += entry + " // ";
					} else
						cc += entry;
				}
				if (cc.length() > 0)
					resultBenzymeEntryInfo += "<small>Comments: " + cc
							+ "</small><br>";
				
				resultBenzymeEntryInfo += "<font color=gray><small>SIB "
						+ EnzymeService.getReleaseVersionForEnzymeInformation()
						+ "</small></font>";
			}
		} else {
			resultAenzymeClassInfo = null;
			resultBenzymeEntryInfo = null;
		}
		if (resultAenzymeClassInfo == null && resultBenzymeEntryInfo == null)
			return null;
		else {
			String tabA;
			String tabB;
			if (resultAenzymeClassInfo != null)
				tabA = resultAenzymeClassInfo;
			else
				tabA = "<small><font color=gray>- Enzyme class unknown -</font></small>";
			if (resultBenzymeEntryInfo != null)
				tabB = resultBenzymeEntryInfo;
			else
				tabB = "<small><font color=gray>- Unspecific or invalid enzyme -</font></small>";
			
			return "<table>" + "<tr><td valign=\"top\">" + tabA
					+ "</td>" + "<td valign=\"top\">" + tabB
					+ "</td></tr></table></html>";
		}
	}
	
	private static String getCompoundToolTip(Node n, String useThisId) {
		if (!new SettingsHelperDefaultIsFalse().isEnabled("grav_view_database_node_status"))
			return null;
		String resultCompoundInfo;
		
		String substanceName;
		if (useThisId == null)
			substanceName = AttributeHelper.getLabel(n, null);
		else
			substanceName = useThisId;
		
		if (substanceName != null) {
			// Retrieve enzyme-class information
			CompoundEntry eze = CompoundService.getInformationLazy(substanceName);
			if (eze == null) {
				resultCompoundInfo = null;
				String keggId = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", null, "");
				if (keggId != null && keggId.length() > 0 && useThisId == null)
					return getCompoundToolTip(n, keggId);
			} else {
				resultCompoundInfo = "<b>" + eze.getID() + "</b><br>";
				String syn = "";
				for (Iterator<?> it = eze.getNames().iterator(); it.hasNext();) {
					String entry = (String) it.next();
					if (it.hasNext()) {
						syn += entry + ", ";
					} else
						syn += entry;
				}
				if (syn.length() > 0)
					resultCompoundInfo += "<small>Names: " + syn
							+ "</small><br>";
				resultCompoundInfo += "<font color=gray><small>KEGG-LIGAND "
						+ CompoundService.getReleaseVersionForCompoundInformation()
						+ "</small></font>";
			}
		} else {
			resultCompoundInfo = null;
		}
		if (resultCompoundInfo == null)
			return null;
		else
			return "<table><tr><td valign=\"top\">"
					+ resultCompoundInfo + "</td></tr></table></html>";
	}
	
	public IPKnodeComponent(GraphElement ge) {
		super(ge);
		// int a = getBackground().getAlpha();
		// setOpaque(a==255);
		// setOpaque(false); // for round shapes
	}
	/*
	 * public static IPKnodeComponent getNewAndMatchingNodeComponent(Node node, Graph graph) {
	 * // String componentType=(String)AttributeHelper.getAttributeValue(node,
	 * // "graphics", "component", nodeTypeDefault, null);
	 * // if (componentType.equals(nodeTypeSubstrate))
	 * // return new NodeComponentSubstrate(node);
	 * // else
	 * // if (componentType.equals(nodeTypeChart2D_type1_line)) {
	 * // String newTitle;
	 * // newTitle=AttributeHelper.getSubstanceName(node, null);
	 * // return new NodeComponentChartXMLdata_type1(node, newTitle);
	 * // } else
	 * // if (componentType.equals(nodeTypeChart2D_type2_bar)) {
	 * // String newTitle;
	 * // newTitle=AttributeHelper.getSubstanceName(node, null);
	 * // return new NodeComponentChartXMLdata_type2(node, newTitle);
	 * // } else
	 * // if (componentType.equals(nodeTypeChart2D_type3_bar_flat)) {
	 * // String newTitle;
	 * // newTitle=AttributeHelper.getSubstanceName(node, null);
	 * // return new NodeComponentChartXMLdata_type3(node, newTitle);
	 * // } else
	 * // if (componentType.equals(nodeTypeFastSimple))
	 * // return new NodeComponentFast(node);
	 * // else {
	 * // NodeTools.setNodeComponentType(node, nodeTypeDefault);
	 * return new IPKnodeComponent(node);
	 * // }
	 * }
	 */
	/*
	 * protected void recreate() throws ShapeNotFoundException {
	 * //System.out.print("R");
	 * super.recreate();
	 * // int a = getBackground().getAlpha();
	 * // setOpaque(a==255);
	 * setOpaque(false);
	 * }
	 */
}
