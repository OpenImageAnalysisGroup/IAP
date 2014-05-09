package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax;

import java.util.List;
import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Process;
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
public class PathWayLoaderLvL3 extends HelperClass
{

	public PathWayLoaderLvL3(Model model, Graph g)
	{
		Set<Pathway> paths = model.getObjects(Pathway.class);
		List<Node> nodes = g.getNodes();
		for (Pathway pathway : paths)
		{
			for (Process p : pathway.getPathwayComponent())
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
