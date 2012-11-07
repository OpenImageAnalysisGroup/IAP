package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Stoichiometry;
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

	public void writeParticipantStoichiometry(Node physicalEntityNode, Node reactionNode, Edge e, Set<Stoichiometry> Stoichiometry)
	{
		String nodeRDFId = reactionNode.getAttribute(Messages.getString("UtilitySuperClassToGraph.82")).getValue().toString();
		for (Stoichiometry s : Stoichiometry)
		{
			String physicalEntityRDFId = s.getPhysicalEntity().getRDFId();
			if (physicalEntityRDFId.matches(nodeRDFId))
			{
				// Stoichiometry found
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.144"), physicalEntityRDFId);
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.145"), String.valueOf(s.getStoichiometricCoefficient()));
				setAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.146"), s.getRDFId());
				break;
			}
		}
	}

	public void readParticipantStoichiometry(PhysicalEntity phys, Conversion interaction, Edge edge, Model model)
	{
		String PhysicalEntityRDFId = getAttributeSecure(edge, Messages.getString("UtilitySuperClassToGraph.144"));
		if (!getAttributeSecure(edge, Messages.getString("UtilitySuperClassToGraph.145")).matches(""))
		{
			float coefficient = Float.valueOf(getAttributeSecure(edge, Messages.getString("UtilitySuperClassToGraph.145")));
			String RDFId = getAttributeSecure(edge, Messages.getString("UtilitySuperClassToGraph.146"));

			if (PhysicalEntityRDFId.matches(phys.getRDFId()))
			{
				Stoichiometry stoich;
				if (!model.containsID(RDFId))
				{
					stoich = model.addNew(Stoichiometry.class, RDFId);
					stoich.setPhysicalEntity(phys);
					stoich.setStoichiometricCoefficient(coefficient);
				} else
				{
					stoich = (Stoichiometry) model.getByID(RDFId);
				}
				interaction.addParticipantStoichiometry(stoich);
			}

		}
	}
}
