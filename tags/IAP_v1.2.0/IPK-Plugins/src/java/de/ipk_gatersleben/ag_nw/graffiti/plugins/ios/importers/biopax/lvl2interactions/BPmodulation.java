package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.UtilityClassSelectorToGraph;

public class BPmodulation extends BPinteraction
{

	public BPmodulation(Graph Graph, Hashtable<String, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Level2Element i)
	{
		modulation modul = (modulation) i;

		Set<physicalEntityParticipant> controller = modul.getCONTROLLER();
		Set<process> controlled = modul.getCONTROLLED();

		for (physicalEntityParticipant c : controller)
		{
			for (process p : controlled)
			{
				// modulierendes Entity
				Node controllerNode = findORcreateNode(c);

				// zeigt auf modulierende Entities
				catalysis catalysis = (catalysis) p;
				Set<process> setOfControlled = catalysis.getCONTROLLED();
				for (process controlledByCatalysis : setOfControlled)
				{
					Node catalysisNode = findORcreateNode(controlledByCatalysis);

					Edge edge = addEdge(controllerNode, catalysisNode);
					setAttributeSecure(edge, Messages.getString("UtilitySuperClassToGraph.115"), catalysis.getRDFId()); //$NON-NLS-1$
					setAttributeSecure(edge, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$

					UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(edge, modul);
				}
			}
		}
	}
}
