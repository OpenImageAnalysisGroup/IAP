package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.url_attribute_context_commands;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.core.ImageBundle;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.ProvidesDirectMouseClickContextMenu;
import org.graffiti.plugins.views.defaults.EdgeComponent;
import org.graffiti.plugins.views.defaults.NodeComponent;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute.URLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute.URLAttributeEditor;

public class URLattributeContextMenuCommandPlugin
					extends IPK_PluginAdapter
					implements ProvidesDirectMouseClickContextMenu {
	private ImageBundle iBundle = ImageBundle.getInstance();
	
	private ArrayList<JMenuItem> getContextCommandForURLattributes(MouseEvent lastMouseE, Component lastMouseSrc, Graph g, boolean modifyCommand) {
		ArrayList<JMenuItem> result = new ArrayList<JMenuItem>();
		Attributable attr;
		if (lastMouseSrc != null && (lastMouseSrc instanceof NodeComponent)) {
			NodeComponent nc = (NodeComponent) lastMouseSrc;
			attr = nc.getGraphElement();
		} else
			if (lastMouseSrc != null && (lastMouseSrc instanceof EdgeComponent)) {
				EdgeComponent ec = (EdgeComponent) lastMouseSrc;
				attr = ec.getGraphElement();
			} else {
				attr = g;
				if (modifyCommand)
					return result;
			}
		if (attr != null) {
			CollectionAttribute ca = attr.getAttributes();
			Stack<CollectionAttribute> catts = new Stack<CollectionAttribute>();
			catts.push(ca);
			while (!catts.empty()) {
				CollectionAttribute ccc = catts.pop();
				for (Object o : ccc.getCollection().values()) {
					if (o instanceof CollectionAttribute) {
						catts.push((CollectionAttribute) o);
					} else {
						Attribute a = (Attribute) o;
						if (a instanceof URLAttribute) {
							URLAttribute urlAttribute = (URLAttribute) a;
							if (modifyCommand && !URLAttributeEditor.supportsModifyCommand(urlAttribute))
								continue;
							String dsc = "Node";
							if (attr instanceof Edge)
								dsc = "Edge";
							String desc = AttributeHelper.getDefaultAttributeDescriptionFor(a.getId(), dsc, a);
							if (desc.indexOf(":") >= 0)
								desc = desc.substring(desc.indexOf(":") + ":".length()).trim();
							String title = getMenuItemTitle(a, modifyCommand, urlAttribute, desc);
							JMenuItem mi = new JMenuItem(title);
							mi.addActionListener(URLAttributeEditor.getActionListener(urlAttribute, modifyCommand));
							
							if (desc.indexOf("Reference") > 0 || desc.indexOf("URL") > 0)
								mi.setIcon(iBundle.getImageIcon("tool.infobulb"));
							else {
								if (!modifyCommand)
									mi.setIcon(iBundle.getImageIcon("menu.file.open.icon"));
								else
									mi.setIcon(iBundle.getImageIcon("tool.addNodeSmall"));
							}
							
							result.add(mi);
						}
					}
				}
			}
		}
		return result;
	}
	
	private String getMenuItemTitle(Attribute a, boolean modifyCommand,
						URLAttribute ua, String desc) {
		String title = URLAttributeEditor.getDescription(ua, true, modifyCommand).trim() + " " + desc;
		title = StringManipulationTools.removeHTMLtags(title);
		if (a.getValue() != null) {
			String val = a.getValue().toString();
			if (val != null) {
				val = StringManipulationTools.stringReplace(val, "%", "&#");
				if (val.startsWith(AttributeHelper.preFilePath))
					val = val.substring(AttributeHelper.preFilePath.length());
				if (val.indexOf("://") >= 0) {
					if (val.substring(val.indexOf("://") + "://".length()).indexOf("/") > 0) {
						val = val.substring(0, val.lastIndexOf("/")).trim() + "/...";
					}
				} else {
					if (val.indexOf(".") > 0)
						val = val.substring(0, val.lastIndexOf("."));
					val = StringManipulationTools.removeHTMLtags(val).trim();
				}
				if (val.length() > 0)
					title = "<html>" + title + " (" + val + ")";
			}
		}
		return title;
	}
	
	public JMenuItem[] getContextCommand(MouseEvent lastMouseE,
						Component lastMouseSrc, Graph graph) {
		JMenu a = new JMenu("Navigate");
		JMenu b = new JMenu("Add Link");
		int ia = 0;
		int ib = 0;
		for (JMenuItem mi : getContextCommandForURLattributes(
							org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseE(),
							org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseSrc(),
							graph, false)) {
			a.add(mi);
			ia++;
		}
		for (JMenuItem mi : getContextCommandForURLattributes(
							org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseE(),
							org.graffiti.plugins.modes.defaults.MegaTools.getLastMouseSrc(),
							graph, true)) {
			b.add(mi);
			ib++;
		}
		ArrayList<JMenuItem> result = new ArrayList<JMenuItem>();
		if (ia > 0)
			result.add(a);
		if (ib > 0)
			result.add(b);
		return result.toArray(new JMenuItem[] {});
	}
	
}
