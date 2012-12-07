package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility;

import java.util.Set;

import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

/**
 * writes on one edge a Stochiometry and reads it again
 * 
 * @author ricardo
 * 
 */
public class StoichiometryWriter extends HelperClass
{

	public void writeParticipantStoichiometry(Node node, Node center, Edge e, Set<physicalEntityParticipant> set)
	{
		String nodeRDFId = node.getAttribute(Messages.getString("UtilitySuperClassToGraph.82")).getValue().toString();
		for (physicalEntityParticipant part : set)
		{

			String physicalEntityRDFId = part.getPHYSICAL_ENTITY().getRDFId();
			if (physicalEntityRDFId.matches(nodeRDFId))
			{
				// Stoichiometry found
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.144"), physicalEntityRDFId);
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.145"), String.valueOf(part.getSTOICHIOMETRIC_COEFFICIENT()));
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.146"), physicalEntityRDFId + "_stoich");
				break;
			}
		}

	}
}
