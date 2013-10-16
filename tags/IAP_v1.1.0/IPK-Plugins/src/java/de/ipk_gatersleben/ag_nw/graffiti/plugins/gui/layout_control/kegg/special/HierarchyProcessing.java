package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.special;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;

public class HierarchyProcessing {
	
	public static ActionListener getSpecialCommandHierarchyCoverageListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Graph g = null;
				try {
					g = MainFrame.getInstance().getActiveSession().getGraph();
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog("No active graph editor window found!", "Error");
				}
				if (g != null)
					processCommandKo(g);
			}
		};
	}
	
	private static void addAttributes(Node n, KoStatisticInformation info) {
		if (info == null)
			return;
		int nKegg = info.getNumberOfKeggKos();
		int nAnn = info.getNumberOfKoAnnotations();
		AttributeHelper.setAttribute(n, "hiearchy", "nKoKegg", nKegg);
		AttributeHelper.setAttribute(n, "hiearchy", "nKoAnnotation", nAnn);
		AttributeHelper.setAttribute(n, "hiearchy", "ratioKoAnnKegg", (double) nAnn / (double) nKegg);
	}
	
	private static void processCommandKo(Graph g) {
		HashMap<Node, KoStatisticInformation> node2info =
							new HashMap<Node, KoStatisticInformation>();
		
		HashMap<Node, KoStatisticInformation> node2upstreamInfo =
							new HashMap<Node, KoStatisticInformation>();
		
		// process map nodes
		for (Node n : g.getNodes()) {
			if (KeggHelper.isMapNode(n)) {
				// use KEGG SOAP API to retrieve reactions and enzymes
				KoStatisticInformation info = new KoStatisticInformation(n);
				info.setKeggMapNumber(KeggHelper.getKeggId(n));
				info.loadKeggKoInformation();
				
				info.enumerateChildNodesKo();
				
				node2info.put(n, info);
			}
		}
		
		// process upstream nodes of map nodes and combine information
		for (Node n : g.getNodes()) {
			if (node2info.containsKey(n))
				continue;
			if (node2upstreamInfo.containsKey(n))
				continue;
			Set<Node> downstreamNodes = new HashSet<Node>();
			GraphHelper.getConnectedNodes(n, true, downstreamNodes);
			KoStatisticInformation info = new KoStatisticInformation(n);
			for (Node down : downstreamNodes) {
				if (node2info.containsKey(down)) {
					KoStatisticInformation pathwayNodeInfo = node2info.get(down);
					info.addInformation(pathwayNodeInfo);
				}
			}
			node2upstreamInfo.put(n, info);
		}
		
		// add frequency count attributes
		for (Node n : node2info.keySet()) {
			KoStatisticInformation info = node2info.get(n);
			addAttributes(n, info);
		}
		for (Node n : node2upstreamInfo.keySet()) {
			KoStatisticInformation info = node2upstreamInfo.get(n);
			addAttributes(n, info);
		}
	}
}
