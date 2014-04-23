package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

public class BPModulation extends BPInteraction
{

	public BPModulation(Graph Graph, Hashtable<Entity, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Interaction i)
	{
		Modulation mo = (Modulation) i;
		// always Process
		Set<Process> controlled = mo.getControlled();
		// always Physical Entity
		Set<Controller> controller = mo.getController();

		for (Controller c : controller)
		{
			for (Process p : controlled)
			{
				// modulierendes Entity
				PhysicalEntity entity = (PhysicalEntity) c;
				Node controllerNode = findORcreateNode(entity);

				// zeigt auf modulierende Entities
				Catalysis catalysis = (Catalysis) p;
				Set<Process> setOfControlled = catalysis.getControlled();
				for (Process controlledByCatalysis : setOfControlled)
				{
					Node catalysisNode = findORcreateNode(controlledByCatalysis);

					Edge e = addEdge(controllerNode, catalysisNode);
					setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.115"), catalysis.getRDFId()); //$NON-NLS-1$
					setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
					UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(e, mo);
				}
			}
		}
	}
}
