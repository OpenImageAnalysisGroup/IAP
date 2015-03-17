package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.Hashtable;
import java.util.Set;

import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

public class BPTemplateReactionRegulation extends BPInteraction
{

	public BPTemplateReactionRegulation(Graph Graph, Hashtable<Entity, Node> Nodes)
	{
		super(Graph, Nodes);
	}

	public void read(Interaction i)
	{
		TemplateReactionRegulation trr = (TemplateReactionRegulation) i;
		// always TemplateReaction
		Set<Process> controlled = trr.getControlled();
		// always Physical Entity
		Set<Controller> controller = trr.getController();

		for (Controller c : controller)
		{
			for (Process p : controlled)
			{
				TemplateReaction TR = (TemplateReaction) p;
				Node process = findORcreateNode(TR);

				PhysicalEntity entity = (PhysicalEntity) c;
				Node controllNode = findORcreateNode(entity);

				Edge e = addEdge(controllNode, process);
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.116"), Messages.getString("UtilitySuperClassToGraph.117")); //$NON-NLS-1$ //$NON-NLS-2$
				UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(e, trr);
			}
		}
	}
}
