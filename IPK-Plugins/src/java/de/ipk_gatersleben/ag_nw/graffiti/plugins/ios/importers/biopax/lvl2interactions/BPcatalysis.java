package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.UtilityClassSelectorToGraph;

public class BPcatalysis extends BPinteraction
{

	public BPcatalysis(Graph Graph, Hashtable<String, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Level2Element i)
	{
		catalysis convers = (catalysis) i;

		Set<physicalEntityParticipant> controller = convers.getCONTROLLER();
		Set<process> controlled = convers.getCONTROLLED();

		for (physicalEntityParticipant c : controller)
		{
			for (process p : controlled)
			{
				Node processNode = findORcreateNode(p);
				Node controllerNode = findORcreateNode(c);

				if (convers.getCOFACTOR().contains(c))
				{
					setAttributeSecure(controllerNode, Messages.getString("UtilitySuperClassToGraph.120"), Messages.getString("UtilitySuperClassToGraph.121"));
				} else
				{
					setAttributeSecure(controllerNode, Messages.getString("UtilitySuperClassToGraph.120"), Messages.getString("UtilitySuperClassToGraph.122"));
				}

				Edge catalyse = addEdge(controllerNode, processNode);
				setAttributeSecure(catalyse, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
				UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(catalyse, convers);
			}
		}
	}

}