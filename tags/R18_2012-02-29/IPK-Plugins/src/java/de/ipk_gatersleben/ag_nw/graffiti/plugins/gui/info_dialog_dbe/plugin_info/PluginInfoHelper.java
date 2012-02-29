package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.info_dialog_dbe.plugin_info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ErrorMsg;
import org.HelperClass;
import org.StringManipulationTools;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.managers.pluginmgr.PluginEntry;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.ProvidesDirectMouseClickContextMenu;
import org.graffiti.plugin.algorithm.ProvidesEdgeContextMenu;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugin.algorithm.ProvidesNodeContextMenu;
import org.graffiti.plugin.extension.Extension;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.io.InputSerializer;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.tool.Tool;
import org.graffiti.plugin.view.GraffitiShape;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class PluginInfoHelper implements HelperClass {
	public static String pretifyPluginList(Collection<PluginEntry> pluginEntries) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><font face='arial'>" +
							"<h3><font face='arial'>" + pluginEntries.size() + "  plugins are loaded, content overview:</h3>");
		
		sb.append(getPluginContentStatistics(pluginEntries));
		
		sb.append("<h3><font face='arial'>List of loaded plugins:</h3>" +
							"<table border='1'>");
		getPluginDescriptionTableHeader(sb);
		for (PluginEntry dpe : pluginEntries) {
			getPluginDescriptionHTMLtableRow(sb, dpe);
		}
		sb.append("</table>");
		
		return sb.toString();
	}
	
	public static String getPluginDescriptionTable(PluginEntry dpe) {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1'>");
		getPluginDescriptionTableHeader(sb);
		getPluginDescriptionHTMLtableRow(sb, dpe);
		sb.append("</table>");
		return sb.toString();
	}
	
	private static void getPluginDescriptionTableHeader(StringBuilder sb) {
		sb.append("<tr><th><font face='arial'>Name, Author, Package</th><th><font face='arial'>Content</th>");
	}
	
	private static void getPluginDescriptionHTMLtableRow(StringBuilder sb, PluginEntry dpe) {
		GenericPlugin gp = dpe.getPlugin();
		PluginDescription pd = dpe.getDescription();
		sb.append("<tr>");
		sb.append("<td><font face='arial'>" + pd.getName() + "<br><small><br>" + pd.getAuthor() + "<br><br>" +
							"" + pretifyPackageName(gp.getClass().getPackage().getName()) + "</small></td>");
		sb.append("<td><font face='arial'>" +
							getSummaryInfo(true, pd, gp) +
							"</td>");
		sb.append("</tr>");
	}
	
	private static String getPluginContentStatistics(
						Collection<PluginEntry> pluginEntries) {
		StringBuilder result = new StringBuilder();
		HashMap<String, ArrayList<PluginEntry>> pluginsByPackage = new HashMap<String, ArrayList<PluginEntry>>();
		for (PluginEntry pe : pluginEntries) {
			String cn = pe.getPlugin().getClass().getCanonicalName();
			String[] p = cn.split("\\.");
			if (p.length >= 2) {
				if (p[0].equalsIgnoreCase("vanted_feature"))
					cn = p[0];
				else
					cn = p[0] + "." + p[1];
			}
			if (!pluginsByPackage.containsKey(cn))
				pluginsByPackage.put(cn, new ArrayList<PluginEntry>());
			pluginsByPackage.get(cn).add(pe);
			// if (!pluginsByPackage.containsKey(""))
			// pluginsByPackage.put("", new ArrayList<DefaultPluginEntry>());
			// pluginsByPackage.get("").add(pe);
		}
		for (Entry<String, ArrayList<PluginEntry>> entry : pluginsByPackage.entrySet()) {
			result.append("<h3>Package " + entry.getKey() + ".*</h3>");
			int algorithms = 0;
			int tools = 0;
			int serializersIn = 0;
			int serializersOut = 0;
			int views = 0;
			int ext = 0;
			int attributes = 0;
			int guiComp = 0;
			int modes = 0;
			int shapes = 0;
			int valueEditComps = 0;
			int sidePanels = 0;
			for (PluginEntry pe : entry.getValue()) {
				if (pe.getPlugin() == null)
					continue;
				algorithms += pe.getPlugin().getAlgorithms().length;
				algorithms += getAddAlgNumber(pe.getPlugin());
				serializersIn += pe.getPlugin().getInputSerializers().length;
				serializersOut += pe.getPlugin().getOutputSerializers().length;
				views += pe.getPlugin().getViews().length;
				if (pe.getPlugin().getExtensions() != null)
					ext += pe.getPlugin().getExtensions().length;
				attributes += pe.getPlugin().getAttributes().length;
				if (pe.getPlugin() instanceof EditorPlugin) {
					EditorPlugin ep = (EditorPlugin) pe.getPlugin();
					if (ep.getTools() != null)
						tools += ep.getTools().length;
					if (ep.getGUIComponents() != null)
						guiComp += ep.getGUIComponents().length;
					if (ep.getModes() != null)
						modes += ep.getModes().length;
					if (ep.getShapes() != null)
						shapes += ep.getShapes().length;
					if (ep.getValueEditComponents() != null)
						valueEditComps += ep.getValueEditComponents().size();
					if (ep.getInspectorTabs() != null)
						sidePanels += ep.getInspectorTabs().length;
				}
			}
			result.append("<table border=\"1\"><tr><th>Content</th><th>Elements</th></tr>");
			if (algorithms > 0)
				result.append("<tr><td>Algorithms</td><td>" + algorithms + "</td></tr>");
			if (tools > 0)
				result.append("<tr><td>Tools</td><td>" + tools + "</td></tr>");
			if (serializersIn > 0)
				result.append("<tr><td>Serializers (In)</td><td>" + serializersIn + "</td></tr>");
			if (serializersOut > 0)
				result.append("<tr><td>Serializers (Out)</td><td>" + serializersOut + "</td></tr>");
			if (views > 0)
				result.append("<tr><td>Views</td><td>" + views + "</td></tr>");
			if (ext > 0)
				result.append("<tr><td>Extensions</td><td>" + views + "</td></tr>");
			if (attributes > 0)
				result.append("<tr><td>Attributes</td><td>" + attributes + "</td></tr>");
			if (guiComp > 0)
				result.append("<tr><td>GUI Components</td><td>" + guiComp + "</td></tr>");
			if (modes > 0)
				result.append("<tr><td>Modes</td><td>" + modes + "</td></tr>");
			if (shapes > 0)
				result.append("<tr><td>Shapes</td><td>" + shapes + "</td></tr>");
			if (valueEditComps > 0)
				result.append("<tr><td>Value Edit Comp.</td><td>" + valueEditComps + "</td></tr>");
			if (valueEditComps > 0)
				result.append("<tr><td>Inspector Tabs</td><td>" + sidePanels + "</td></tr>");
			result.append("</table>");
		}
		return result.toString();
	}
	
	private static String getInfo(String pre, String check, String post) {
		if (check == null || check.length() <= 0)
			return "";
		else
			return pre + check + post;
	}
	
	private static String pretifyPackageName(String name) {
		return "" + StringManipulationTools.stringReplace(
							StringManipulationTools.stringReplace(name,
												".ag_nw.", ".<br>ag_nw."),
							".plugins", ".<br>plugins") + "";
	}
	
	private static String getListenerList(GenericPlugin gp) {
		ArrayList<String> l = new ArrayList<String>();
		if (gp.isSelectionListener())
			l.add("Selection");
		if (gp.isSessionListener())
			l.add("Session");
		if (gp.isViewListener())
			l.add("View");
		return pretifyList(l);
	}
	
	private static String getFeatures(String pre, GenericPlugin gp) {
		StringBuilder sb = new StringBuilder();
		sb.append(pre);
		String pre2 = "&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;";
		if (gp.getAlgorithms() != null && gp.getAlgorithms().length > 0) {
			sb.append(gp.getAlgorithms().length + getAddAlgNumber(gp) + " Algorithm(s):<br>");
			for (Algorithm a : gp.getAlgorithms()) {
				sb.append(pre2);
				sb.append("" + getCategoryDesc(a.getCategory(), " / ") + getAlgorithmName(a, a.getName()) + "");
				sb.append(getAlgorithmSelectionInfo(a));
				checkContextMenuProcessing(a, sb, "", "&nbsp;&nbsp;&nbsp;&nbsp;" + pre2);
				
				// sb.append(pre2);
				// sb.append("Description: "+a.getDescription()+"<br>");
			}
			sb.append("<br>");
		}
		if (gp.getAttributes() != null && gp.getAttributes().length > 0) {
			sb.append(gp.getAttributes().length + " defined Attribute(s):<br>");
			for (Class<?> a : gp.getAttributes()) {
				sb.append(pre2);
				sb.append(a.getSimpleName() + "<br>");
			}
			sb.append("<br>");
		}
		if (gp.getExtensions() != null && gp.getExtensions().length > 0) {
			sb.append(gp.getExtensions().length + " Extension(s):<br>");
			for (Extension a : gp.getExtensions()) {
				sb.append(pre2);
				sb.append("Name: " + a.getName() + "<br>");
				sb.append(pre2);
				sb.append("Category: " + a.getCategory() + "<br>");
				sb.append(pre2);
				sb.append("Menu Items: " + pretifyList(a.getMenuItems()) + "<br>");
			}
			sb.append("<br>");
		}
		if (gp.getInputSerializers() != null && gp.getInputSerializers().length > 0) {
			sb.append(gp.getInputSerializers().length + " Input Serializer(s):<br>");
			for (InputSerializer a : gp.getInputSerializers()) {
				sb.append(pre2);
				sb.append("File Extension(s): " + pretifyList(a.getExtensions()) + "<br>");
				sb.append(pre2);
				sb.append("Extension Description(s): " + pretifyList(a.getFileTypeDescriptions()) + "<br>");
			}
			sb.append("<br>");
		}
		if (gp.getOutputSerializers() != null && gp.getOutputSerializers().length > 0) {
			sb.append(gp.getOutputSerializers().length + " Output Serializer(s):<br>");
			for (OutputSerializer a : gp.getOutputSerializers()) {
				sb.append(pre2);
				sb.append("File Extension(s): " + pretifyList(a.getExtensions()) + "<br>");
				sb.append(pre2);
				sb.append("Extension Description(s): " + pretifyList(a.getFileTypeDescriptions()) + "<br>");
			}
			sb.append("<br>");
		}
		if (gp.getViews() != null && gp.getViews().length > 0) {
			sb.append(gp.getViews().length + " View(s):<br>");
			for (String a : gp.getViews()) {
				sb.append(pre2);
				sb.append("View: " + a + "<br>");
			}
			sb.append("<br>");
		}
		
		checkContextMenuProcessing(gp, sb, "Other:<br>", pre2);
		
		if (gp instanceof EditorPlugin) {
			sb.append("<br>EditorPlugin Features:<br>");
			EditorPlugin ep = (EditorPlugin) gp;
			if (ep.getTools() != null && ep.getTools().length > 0) {
				sb.append(ep.getTools().length + " Tools(s):<br>");
				for (Tool t : ep.getTools()) {
					sb.append(pre2);
					sb.append("Tool: " + t.getClass().getName() + "<br>");
				}
				sb.append("<br>");
			}
			if (ep.getGUIComponents() != null && ep.getGUIComponents().length > 0) {
				sb.append(ep.getGUIComponents().length + " GUI Component(s):<br>");
				for (GraffitiComponent gc : ep.getGUIComponents()) {
					sb.append(pre2);
					sb.append("" + lastElement(gc.getClass().getName(), ".") + " (target: " + gc.getPreferredComponent() + ")<br>");
				}
				sb.append("<br>");
			}
			if (ep.getModes() != null && ep.getModes().length > 0) {
				sb.append(ep.getModes().length + " Mode(s):<br>");
				for (Mode mo : ep.getModes()) {
					sb.append(pre2);
					sb.append("" + mo.getClass().getName() + " (ID: " + mo.getId() + ")<br>");
				}
				sb.append("<br>");
			}
			if (ep.getShapes() != null && ep.getShapes().length > 0) {
				sb.append(ep.getShapes().length + " Shape(s):<br>");
				for (GraffitiShape sh : ep.getShapes()) {
					sb.append(pre2);
					sb.append("" + sh.getClass().getName() + "<br>");
				}
				sb.append("<br>");
			}
			if (ep.getValueEditComponents() != null && ep.getValueEditComponents().size() > 0) {
				sb.append(ep.getValueEditComponents().size() + " ValueEditComponent(s):<br>");
				for (Object o : ep.getValueEditComponents().keySet()) {
					sb.append(pre2);
					try {
						sb.append("" + lastElement(o.toString(), ".") + " -- " + lastElement(ep.getValueEditComponents().get(o).toString(), ".") + "<br>");
					} catch (ClassCastException cce) {
						ErrorMsg.addErrorMessage(cce);
					}
				}
				sb.append("<br>");
			}
			if (ep.getInspectorTabs() != null && ep.getInspectorTabs().length > 0) {
				sb.append(ep.getInspectorTabs().length + " InspectorTab(s):<br>");
				for (InspectorTab it : ep.getInspectorTabs()) {
					sb.append(pre2);
					sb.append("Tab " + it.getTitle() + "<br>");
				}
				sb.append("<br>");
			}
		}
		return sb.toString();
	}
	
	private static int getAddAlgNumber(GenericPlugin gp) {
		int add = 0;
		if (gp.getAlgorithms() != null && gp.getAlgorithms().length > 0) {
			for (Algorithm lga : gp.getAlgorithms()) {
				if (lga instanceof LaunchGui) {
					LaunchGui lg = (LaunchGui) lga;
					if (lg.getAlgorithmList() != null)
						add += lg.getAlgorithmList().size();
				}
			}
		}
		return add;
	}
	
	private static String getAlgorithmSelectionInfo(Algorithm lga) {
		StringBuilder res = new StringBuilder("");
		String pre = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;*&nbsp;";
		if (lga instanceof LaunchGui) {
			LaunchGui lg = (LaunchGui) lga;
			if (lg.getAlgorithmList() != null)
				for (Algorithm a : lg.getAlgorithmList())
					if (a != null)
						res.append(pre + getCategoryDesc(a.getCategory(), " / ") + getAlgorithmName(a, a.getName()) + "<br>");
		}
		if (res.length() > 0)
			return " - algorithm provides access to:<br>" + res.toString();
		else
			return "<br>";
	}
	
	private static void checkContextMenuProcessing(Object gp,
						StringBuilder sb, String in, String pre2) {
		if (gp instanceof ProvidesGeneralContextMenu
							|| gp instanceof ProvidesNodeContextMenu
							|| gp instanceof ProvidesEdgeContextMenu
							|| gp instanceof ProvidesDirectMouseClickContextMenu) {
			
			sb.append(in);
			
			if (gp instanceof ProvidesGeneralContextMenu) {
				sb.append(pre2);
				sb.append("provides network context menu<br>");
			}
			if (gp instanceof ProvidesNodeContextMenu) {
				sb.append(pre2);
				sb.append("provides node context menu<br>");
			}
			if (gp instanceof ProvidesEdgeContextMenu) {
				sb.append(pre2);
				sb.append("provides node context menu<br>");
			}
			if (gp instanceof ProvidesDirectMouseClickContextMenu) {
				sb.append(pre2);
				sb.append("provides context menu at main level<br>");
			}
			sb.append("<br>");
		}
	}
	
	private static String lastElement(String val, String div) {
		if (val.indexOf(".") >= 0)
			return val.substring(val.lastIndexOf(".") + ".".length());
		else
			return val;
	}
	
	private static String getCategoryDesc(String category, String post) {
		if (category != null && category.length() > 0)
			return category + post;
		else
			return "";
	}
	
	public static String getSummaryInfo(boolean includeVersionAndAvailability, PluginDescription pd, GenericPlugin gp) {
		String ddd = pd.getDescription();
		if (ddd != null) {
			ddd = StringManipulationTools.stringReplace(ddd, "(break)", "<br>");
			ddd = StringManipulationTools.stringReplace(ddd, "\n", "");
			ddd = StringManipulationTools.stringReplace(ddd, "\t", "");
			if (ddd.length() > 100)
				ddd = StringManipulationTools.getWordWrap(ddd, 60);
		}
		return (includeVersionAndAvailability ? getInfo("Version: ", pd.getVersion(), "<br>") : "") +
							(includeVersionAndAvailability ? getInfo("Availability: ", pd.getAvailable(), "<br>") : "") +
							getInfo("Description: ", ddd, "<br>") +
							getInfo("Depends on: ", pretifyList(pd.getDependencies()), "<br>") +
							getInfo("Reacts on: ", getListenerList(gp), "<br>") +
							getInfo("Plugin Features:<br>", getFeatures("", gp), "");
	}
	
	private static String getAlgorithmName(Algorithm a, String name) {
		if (name == null || name.length() <= 0)
			return "(inactive: " + a.getClass().getSimpleName() + ")";
		else
			return StringManipulationTools.removeHTMLtags(name);
	}
	
	@SuppressWarnings("unchecked")
	private static String pretifyList(List list) {
		StringBuilder sb = new StringBuilder();
		if (list != null)
			for (Object o : list) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(o.toString());
			}
		return sb.toString();
	}
	
	private static String pretifyList(String[] list) {
		StringBuilder sb = new StringBuilder();
		if (list != null)
			for (Object o : list) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(o.toString());
			}
		return sb.toString();
	}
}
