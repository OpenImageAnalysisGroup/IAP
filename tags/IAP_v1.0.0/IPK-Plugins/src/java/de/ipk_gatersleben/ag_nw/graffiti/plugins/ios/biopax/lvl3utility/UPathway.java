package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UPathway extends UtilitySuperClassToGraph
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
	public static void addAttributesToNode(GraphElement elem, Pathway i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.136")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAvailability());
		setComment(elem, i.getComment());
		setDataSource(elem, i.getDataSource());
		setEvidence(elem, i.getEvidence());
		setName(elem, i.getName());
		setRDFId(elem, i.getRDFId());
		setStandardName(elem, i.getStandardName());
		setXRef(elem, i.getXref());

	}

	public static void readAttributesFromNode(GraphElement node, Graph g, Model model) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Node elem = (Node) node;
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));
		Pathway interaction = model.addNew(Pathway.class, RDFID);

		UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
		UtilitySuperClassFromGraph.getAvailability(elem, interaction);
		UtilitySuperClassFromGraph.getComment(elem, interaction);
		UtilitySuperClassFromGraph.getDataSource(elem, interaction, model);
		UtilitySuperClassFromGraph.getEvidence(elem, interaction, model);
		UtilitySuperClassFromGraph.getName(elem, interaction);
		UtilitySuperClassFromGraph.getStandardName(elem, interaction);
		UtilitySuperClassFromGraph.getXRef(elem, interaction, model);

	}

}
