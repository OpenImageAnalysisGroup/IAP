package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

public class BPConversion extends BPInteraction
{

	public BPConversion(Graph Graph, Hashtable<Entity, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Interaction i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Conversion con = (Conversion) i;

		Set<PhysicalEntity> left = con.getLeft();
		Set<PhysicalEntity> right = con.getRight();

		// set center node of the reaction
		Node center = graph.addNode(centerAttribute);
		UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(center, con);
		nodes.put(con, center);

		for (PhysicalEntity l : left)
		{
			Node node = findORcreateNode(l);
			Edge e = addEdge(node, center);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
			sW.writeParticipantStoichiometry(node, center, e, con.getParticipantStoichiometry());
		}

		for (PhysicalEntity r : right)
		{
			Node node = findORcreateNode(r);
			Edge e = addEdge(center, node);
			setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.118")); //$NON-NLS-1$ //$NON-NLS-2$tion", "right");
			sW.writeParticipantStoichiometry(node, center, e, con.getParticipantStoichiometry());
		}
	}
}
