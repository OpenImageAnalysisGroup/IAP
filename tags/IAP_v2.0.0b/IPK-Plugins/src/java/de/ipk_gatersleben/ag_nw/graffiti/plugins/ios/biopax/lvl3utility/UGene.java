package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Gene;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UGene extends UtilitySuperClassToGraph
{
	/**
	 * adds all information within the biopax class to the attribute set of the
	 * node
	 * 
	 * @param elem
	 * @param i
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void addAttributesToNode(GraphElement elem, Gene i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.149")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAvailability());
		setComment(elem, i.getComment());
		setDataSource(elem, i.getDataSource());
		setEvidence(elem, i.getEvidence());
		setName(elem, i.getName());
		setOrganism(elem, i.getOrganism());
		setRDFId(elem, i.getRDFId());
		setStandardName(elem, i.getStandardName());
		setXRef(elem, i.getXref());

	}

	public static void readAttributesFromNode(GraphElement elem, Graph g, Model model)
	{
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));
		if (!model.containsID(RDFID))
		{
			Gene interaction = model.addNew(Gene.class, RDFID);
			UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
			UtilitySuperClassFromGraph.getAvailability(elem, interaction);
			UtilitySuperClassFromGraph.getComment(elem, interaction);
			UtilitySuperClassFromGraph.getDataSource(elem, interaction, model);
			UtilitySuperClassFromGraph.getEvidence(elem, interaction, model);
			UtilitySuperClassFromGraph.getName(elem, interaction);
			UtilitySuperClassFromGraph.getOrganism(elem, interaction, model);
			UtilitySuperClassFromGraph.getStandardName(elem, interaction);
			UtilitySuperClassFromGraph.getXRef(elem, interaction, model);
		}

	}
}
