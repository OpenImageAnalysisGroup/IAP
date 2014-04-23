package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax;

import java.util.List;
import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

/**
 * This Class sets the related pathway information to each node of the graph
 * 
 * @author ricardo
 * 
 */
public class PathWayLoaderLvL2 extends HelperClass
{
	public PathWayLoaderLvL2(Model model, Graph g)
	{
		Set<pathway> paths = model.getObjects(pathway.class);
		List<Node> nodes = g.getNodes();
		for (pathway pathway : paths)
		{
			for (pathwayComponent p : pathway.getPATHWAY_COMPONENTS())
			{
				String processRDFId = p.getRDFId();
				for (Node node : nodes)
				{
					String nodeRDFId = getAttributeSecure(node, Messages.getString("UtilitySuperClassToGraph.82"));
					if (nodeRDFId.matches(processRDFId))
					{
						setAttributeSecure(node, Messages.getString("UtilityClassSelectorFromGraph.180"), pathway.getRDFId());
					}

				}
			}
		}
	}
}
