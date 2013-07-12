package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.complexAssembly;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.UtilityClassSelectorToGraph;

public class BPcomplexAssembly extends BPinteraction
{

	public BPcomplexAssembly(Graph Graph, Hashtable<String, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Level2Element i)
	{
		complexAssembly ca = (complexAssembly) i;

		Set<physicalEntityParticipant> left = ca.getLEFT();
		Set<physicalEntityParticipant> right = ca.getRIGHT();

		// set center node of the reaction
		Node center = graph.addNode(centerAttribute);
		UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(center, ca);
		nodes.put(ca.getRDFId(), center);

		for (physicalEntityParticipant l : left)
		{
			Node node = findORcreateNode(l);
			Edge e = addEdge(node, center);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117"));

			sW.writeParticipantStoichiometry(node, center, e, ca.getLEFT());
		}

		for (physicalEntityParticipant r : right)
		{
			Node node = findORcreateNode(r);
			Edge e = addEdge(center, node);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.118"));
			sW.writeParticipantStoichiometry(node, center, e, ca.getRIGHT());
		}
	}
}