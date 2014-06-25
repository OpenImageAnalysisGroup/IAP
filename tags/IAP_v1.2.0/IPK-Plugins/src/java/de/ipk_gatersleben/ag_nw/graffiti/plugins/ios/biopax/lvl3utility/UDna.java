package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Dna;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UDna extends UtilitySuperClassToGraph
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
	public static void addAttributesToNode(GraphElement elem, Dna i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.134")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAvailability());
		setCellularLocation(elem, i.getCellularLocation());
		setComment(elem, i.getComment());
		setDataSource(elem, i.getDataSource());
		setEntityReference(elem, i.getEntityReference());
		setEvidence(elem, i.getEvidence());
		setMemberPhysicalEntity(elem, i.getMemberPhysicalEntity());
		setFeature(elem, i.getFeature());
		setName(elem, i.getName());
		setNotFeature(elem, i.getNotFeature());
		setRDFId(elem, i.getRDFId());
		setStandardName(elem, i.getStandardName());
		setXRef(elem, i.getXref());

	}

	public static void readAttributesFromNode(GraphElement elem, Graph g, Model model)
	{
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));

		if (!model.containsID(RDFID))
		{
			Dna interaction = model.addNew(Dna.class, RDFID);
			UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
			UtilitySuperClassFromGraph.getAvailability(elem, interaction);
			UtilitySuperClassFromGraph.getCellularLocation(elem, interaction, model);
			UtilitySuperClassFromGraph.getComment(elem, interaction);
			UtilitySuperClassFromGraph.getDataSource(elem, interaction, model);
			UtilitySuperClassFromGraph.getEntityReference(elem, interaction, model);
			UtilitySuperClassFromGraph.getEvidence(elem, interaction, model);
			UtilitySuperClassFromGraph.getMemberPhysicalEntity(elem, interaction, model);
			UtilitySuperClassFromGraph.getFeature(elem, interaction, model);
			UtilitySuperClassFromGraph.getName(elem, interaction);
			UtilitySuperClassFromGraph.getNotFeature(elem, interaction, model);
			UtilitySuperClassFromGraph.getStandardName(elem, interaction);
			UtilitySuperClassFromGraph.getXRef(elem, interaction, model);
		}

	}

}
