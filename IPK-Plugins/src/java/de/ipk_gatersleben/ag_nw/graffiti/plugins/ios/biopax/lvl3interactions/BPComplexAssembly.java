package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

public class BPComplexAssembly extends BPInteraction
{

	public BPComplexAssembly(Graph Graph, Hashtable<Entity, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Interaction i)
	{
		ComplexAssembly ca = (ComplexAssembly) i;

		Set<PhysicalEntity> left = ca.getLeft();
		Set<PhysicalEntity> right = ca.getRight();

		// set center node of the reaction
		Node center = graph.addNode(centerAttribute);
		UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(center, ca);
		nodes.put(ca, center);

		for (PhysicalEntity l : left)
		{
			Node node = findORcreateNode(l);
			Edge e = addEdge(node, center);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
			sW.writeParticipantStoichiometry(node, center, e, ca.getParticipantStoichiometry());
		}

		for (PhysicalEntity r : right)
		{
			Node node = findORcreateNode(r);
			Edge e = addEdge(center, node);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.118")); //$NON-NLS-1$ //$NON-NLS-2$
			sW.writeParticipantStoichiometry(node, center, e, ca.getParticipantStoichiometry());
		}
	}
}
