package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

public class BPCatalysis extends BPInteraction
{
	public BPCatalysis(Graph Graph, Hashtable<Entity, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Interaction i)
	{
		Catalysis ca = (Catalysis) i;
		Set<Process> controlled = ca.getControlled();
		Set<Controller> controller = ca.getController();

		for (Controller c : controller)
		{
			for (Process p : controlled)
			{
				Node processNode = findORcreateNode(p);
				Node controllerNode = findORcreateNode(c);

				if (ca.getCofactor().contains(c))
				{
					setAttributeSecure(controllerNode, Messages.getString("UtilitySuperClassToGraph.120"), Messages.getString("UtilitySuperClassToGraph.121"));
				} else
				{
					setAttributeSecure(controllerNode, Messages.getString("UtilitySuperClassToGraph.120"), Messages.getString("UtilitySuperClassToGraph.122"));
				}

				Edge catalyse = addEdge(controllerNode, processNode);
				setAttributeSecure(catalyse, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
				UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(catalyse, ca);
			}
		}
	}
}