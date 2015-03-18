package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.special;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.StringManipulationTools;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_ko.KoService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;

public class KoStatisticInformation {
	
	private String pathwayId;
	
	private HashSet<String> kos = new HashSet<String>();
	private HashSet<String> koAnnotationOfLeafNodes = new HashSet<String>();
	
	private Node node;
	
	public KoStatisticInformation(Node n) {
		this.node = n;
	}
	
	public void setKeggMapNumber(String keggId) {
		keggId = StringManipulationTools.stringReplace(keggId, ":ko", ":ehvu");
		// keggId = ErrorMsg.stringReplace(keggId, "path:", "");
		this.pathwayId = keggId;
	}
	
	public void loadKeggKoInformation() {
		System.out.print("[SOAP] Retrieve KOs of KEGG Map " + pathwayId + "...");
		String[] koIdList = KeggHelper.getKeggKOsOfMap(pathwayId);
		if (koIdList != null && koIdList.length > 0) {
			System.out.println(koIdList.length);
			for (String koId : koIdList) {
				kos.add(koId);
			}
		} else
			System.out.println("0");
	}
	
	public void enumerateChildNodesKo() {
		// Find all leaf nodes
		// interpret label and alternative identifiers and check if there is a
		// valid ko identifier
		// store set of ko ids in koAnnotationOfLeafNodes
		Collection<Node> sourceNodes = new ArrayList<Node>();
		sourceNodes.add(node);
		Set<Node> leafNodes = GraphHelper.getLeafNodes(sourceNodes);
		leafNodes.add(node);
		Set<KoEntry> koInformation = getKoInformation(leafNodes);
		for (KoEntry ee : koInformation) {
			koAnnotationOfLeafNodes.add(ee.getKoID());
		}
	}
	
	private Set<KoEntry> getKoInformation(Set<Node> leafNodes) {
		HashSet<KoEntry> result = new HashSet<KoEntry>();
		for (Node n : leafNodes) {
			NodeHelper nh = new NodeHelper(n);
			String lbl = nh.getLabel();
			ArrayList<String> altIds = nh.getAlternativeIDs();
			EnzymeEntry ee = EnzymeService.getEnzymeInformation(lbl, false);
			if (ee != null && ee.isValid()) {
				Collection<KoEntry> res = KoService.getKoFromEnzyme(ee.getID());
				if (res != null)
					for (KoEntry ko : res)
						result.add(ko);
			}
			if (altIds != null && altIds.size() > 0) {
				for (String altId : altIds) {
					EnzymeEntry eee = EnzymeService.getEnzymeInformation(altId, false);
					if (eee != null && eee.isValid()) {
						Collection<KoEntry> res = KoService.getKoFromEnzyme(eee.getID());
						if (res != null)
							for (KoEntry ko : res)
								result.add(ko);
					};
				}
			}
		}
		return result;
	}
	
	public int getNumberOfKeggKos() {
		return kos.size();
	}
	
	public int getNumberOfKoAnnotations() {
		return koAnnotationOfLeafNodes.size();
	}
	
	public void addInformation(KoStatisticInformation pathwayNodeInfo) {
		this.kos.addAll(pathwayNodeInfo.kos);
		this.koAnnotationOfLeafNodes.addAll(pathwayNodeInfo.koAnnotationOfLeafNodes);
	}
	
}
