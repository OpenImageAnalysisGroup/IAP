package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.actions.URLattributeAction;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggPathwayEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;

public class LoadPathwayAttributeAction implements URLattributeAction {
	
	public ActionListener getActionListener(final Attribute displayable,
						final Graph graph, final GraphElement ge, final boolean performAltCommand) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String val = (String) displayable.getValue();
				Node n = null;
				if (ge != null && ge instanceof Node) {
					n = (Node) ge;
				}
				loadMap(val, graph, n, true);
			}
		};
	}
	
	public String getPreIdentifyer() {
		return "path:";
	}
	
	public static void loadMap(String mapName, Graph g, Node initalMapNode, boolean askForNewWindow) {
		String val = mapName;
		val = val.substring("path:".length());
		val = val.replaceAll("&amp;", "&");
		String map = val.substring(0, 5);
		map = StringManipulationTools.stringReplace(map, "0", "");
		map = StringManipulationTools.stringReplace(map, "1", "");
		map = StringManipulationTools.stringReplace(map, "2", "");
		map = StringManipulationTools.stringReplace(map, "3", "");
		map = StringManipulationTools.stringReplace(map, "4", "");
		map = StringManipulationTools.stringReplace(map, "5", "");
		map = StringManipulationTools.stringReplace(map, "6", "");
		map = StringManipulationTools.stringReplace(map, "7", "");
		map = StringManipulationTools.stringReplace(map, "8", "");
		map = StringManipulationTools.stringReplace(map, "9", "");
		String mapNumber = val;
		KeggPathwayEntry kpe = new KeggPathwayEntry(mapName + " - " + mapNumber,
							false, mapNumber,
							KeggHelper.getGroupFromMapNumber(mapNumber, "")
							// KeggHelper.getGroupFromMapName(mapName)
							);
		Vector2d targetPosition = null;
		if (initalMapNode != null) {
			targetPosition = AttributeHelper.getPositionVec2d(initalMapNode);
		}
		kpe.setTargetPosition(targetPosition);
		
		KeggService.loadPathway(kpe, g, initalMapNode, askForNewWindow, true, true);
	}
	
	public String getCommandDescription(boolean shortDesc, boolean altDesc) {
		if (shortDesc)
			return "Load: ";
		else
			return "Load Pathway";
	}
	
	public boolean supportsModifyCommand() {
		return false;
	}
}
