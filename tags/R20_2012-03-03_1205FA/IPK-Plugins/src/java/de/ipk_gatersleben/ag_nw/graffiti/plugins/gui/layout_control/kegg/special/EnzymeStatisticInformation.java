package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.special;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.StringManipulationTools;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;

public class EnzymeStatisticInformation {
	
	private String pathwayId;
	
	private HashSet<String> reactions = new HashSet<String>();
	private HashSet<String> enzymes = new HashSet<String>();
	private HashSet<String> enzymeAnnotationOfLeafNodes = new HashSet<String>();
	
	private Node node;
	
	public EnzymeStatisticInformation(Node n) {
		this.node = n;
	}
	
	private static HashMap<String, HashSet<String>> reactionId2enzymeId =
						new HashMap<String, HashSet<String>>();
	
	public void setKeggMapNumber(String keggId) {
		keggId = StringManipulationTools.stringReplace(keggId, ":ko", ":ehvu");
		// keggId = ErrorMsg.stringReplace(keggId, "path:", "");
		this.pathwayId = keggId;
	}
	
	public void loadKeggReactionAndEnzymeInformation() {
		System.out.println("[SOAP] Retrieve Reactions of KEGG Map " + pathwayId + "...");
		String[] reactionIdList = KeggHelper.getKeggReactionsOfMap(pathwayId);
		if (reactionIdList != null && reactionIdList.length > 0) {
			for (String reactionId : reactionIdList) {
				reactions.add(reactionId);
				if (!reactionId2enzymeId.containsKey(reactionId)) {
					System.out.println("[SOAP] Retrieve Enzymes of KEGG Reaction " + reactionId + "...");
					String[] enzymes = KeggHelper.getKeggEnzymesByReactionId(reactionId);
					HashSet<String> enzymeSet = new HashSet<String>();
					if (enzymes != null && enzymes.length > 0) {
						for (String enzyme : enzymes)
							enzymeSet.add(enzyme);
					}
					reactionId2enzymeId.put(reactionId, enzymeSet);
				}
				enzymes.addAll(reactionId2enzymeId.get(reactionId));
			}
		}
	}
	
	public void enumerateChildNodeEnzymes() {
		// Find all leaf nodes
		// interpret label and alternative identifiers and check if there is a
		// valid enzyme identifier
		// store set of enyzme ids in enzymeAnnotationOfLeafNodes
		Collection<Node> sourceNodes = new ArrayList<Node>();
		sourceNodes.add(node);
		Set<Node> leafNodes = GraphHelper.getLeafNodes(sourceNodes);
		leafNodes.add(node);
		Set<EnzymeEntry> enzymeInformation = getEnzymeInformation(leafNodes);
		for (EnzymeEntry ee : enzymeInformation) {
			enzymeAnnotationOfLeafNodes.add(ee.getID());
		}
	}
	
	private Set<EnzymeEntry> getEnzymeInformation(Set<Node> leafNodes) {
		HashSet<EnzymeEntry> result = new HashSet<EnzymeEntry>();
		for (Node n : leafNodes) {
			NodeHelper nh = new NodeHelper(n);
			String lbl = nh.getLabel();
			ArrayList<String> altIds = nh.getAlternativeIDs();
			EnzymeEntry ee = EnzymeService.getEnzymeInformation(lbl, false);
			if (ee != null && ee.isValid())
				result.add(ee);
			if (altIds != null && altIds.size() > 0) {
				for (String altId : altIds) {
					EnzymeEntry eee = EnzymeService.getEnzymeInformation(altId, false);
					if (eee != null && eee.isValid())
						result.add(eee);
				}
			}
		}
		return result;
	}
	
	public int getNumberOfKeggEnzymes() {
		return enzymes.size();
	}
	
	public int getNumberOfEnzymeAnnotations() {
		return enzymeAnnotationOfLeafNodes.size();
	}
	
	public void addInformation(EnzymeStatisticInformation pathwayNodeInfo) {
		this.reactions.addAll(pathwayNodeInfo.reactions);
		this.enzymes.addAll(pathwayNodeInfo.enzymes);
		this.enzymeAnnotationOfLeafNodes.addAll(pathwayNodeInfo.enzymeAnnotationOfLeafNodes);
	}
	
}
