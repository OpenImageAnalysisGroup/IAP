package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

public class BPControl extends BPInteraction
{

	public BPControl(Graph Graph, Hashtable<Entity, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Interaction i)
	{
		Control con = (Control) i;
		Set<Process> controlled = con.getControlled();
		Set<Controller> controller = con.getController();

		for (Controller c : controller)
		{
			for (Process p : controlled)
			{
				Node processNode = findORcreateNode(p);
				Node controllNode = findORcreateNode(c);

				Edge e = addEdge(controllNode, processNode);
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
				UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(e, con);
			}
		}

	}
}
