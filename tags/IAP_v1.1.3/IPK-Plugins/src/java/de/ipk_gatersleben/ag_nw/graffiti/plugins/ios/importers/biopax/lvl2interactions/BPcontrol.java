package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.UtilityClassSelectorToGraph;

public class BPcontrol extends BPinteraction
{

	public BPcontrol(Graph Graph, Hashtable<String, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Level2Element i)
	{
		control control = (control) i;

		Set<physicalEntityParticipant> controller = control.getCONTROLLER();
		Set<process> controlled = control.getCONTROLLED();

		for (physicalEntityParticipant c : controller)
		{
			for (process p : controlled)
			{
				Node processNode = findORcreateNode(p);
				Node controllerNode = findORcreateNode(c);

				Edge controlEdge = addEdge(controllerNode, processNode);
				setAttributeSecure(controlEdge, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117"));
				UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(controlEdge, control);
			}
		}
	}
}
