package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.UtilityClassSelectorToGraph;

public class BPtransportWithBiochemicalReaction extends BPinteraction
{

	public BPtransportWithBiochemicalReaction(Graph Graph, Hashtable<String, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Level2Element i)
	{
		transportWithBiochemicalReaction trans = (transportWithBiochemicalReaction) i;

		Set<physicalEntityParticipant> left = trans.getLEFT();
		Set<physicalEntityParticipant> right = trans.getRIGHT();

		// set center node of the reaction
		Node center = graph.addNode(centerAttribute);
		UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(center, trans);
		nodes.put(trans.getRDFId(), center);
		for (physicalEntityParticipant l : left)
		{
			Node node = findORcreateNode(l);

			Edge e = addEdge(node, center);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117"));
			sW.writeParticipantStoichiometry(node, center, e, trans.getLEFT());
		}

		for (physicalEntityParticipant r : right)
		{
			Node node = findORcreateNode(r);

			Edge e = addEdge(center, node);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.118"));

			sW.writeParticipantStoichiometry(node, center, e, trans.getRIGHT());
		}
	}
}
