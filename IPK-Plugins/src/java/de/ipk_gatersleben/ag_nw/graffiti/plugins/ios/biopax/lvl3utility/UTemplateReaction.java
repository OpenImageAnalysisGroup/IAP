package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.ErrorMsg;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.NucleicAcid;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UTemplateReaction extends UtilitySuperClassToGraph
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
	public static void addAttributesToNode(GraphElement elem, TemplateReaction i) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.164")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAvailability());
		setComment(elem, i.getComment());
		setDataSource(elem, i.getDataSource());
		setEvidence(elem, i.getEvidence());
		setInteractionType(elem, i.getInteractionType());
		setName(elem, i.getName());
		setRDFId(elem, i.getRDFId());
		setStandardName(elem, i.getStandardName());
		setXRef(elem, i.getXref());
		
	}
	
	public static void readAttributesFromNode(GraphElement node, Graph g, Model model)
	{
		Node elem = (Node) node;
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));
		TemplateReaction interaction = model.addNew(TemplateReaction.class, RDFID);
		
		UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
		UtilitySuperClassFromGraph.getAvailability(elem, interaction);
		UtilitySuperClassFromGraph.getComment(elem, interaction);
		UtilitySuperClassFromGraph.getDataSource(elem, interaction, model);
		UtilitySuperClassFromGraph.getEvidence(elem, interaction, model);
		UtilitySuperClassFromGraph.getInteractionType(elem, interaction, model);
		UtilitySuperClassFromGraph.getName(elem, interaction);
		UtilitySuperClassFromGraph.getStandardName(elem, interaction);
		UtilitySuperClassFromGraph.getXRef(elem, interaction, model);
		
		for (Edge outgoing : elem.getAllOutEdges())
		{
			
			Node out = outgoing.getTarget();
			String RDFId = getAttributeSecure(out, Messages.getString("UtilitySuperClassToGraph.82"));
			try
			{
				PhysicalEntity p = (PhysicalEntity) model.getByID(RDFId);
				interaction.addProduct(p);
			} catch (ClassCastException e)
			{
				ErrorMsg.addErrorMessage(e);
			}
			
		}
		for (Edge ingoing : elem.getAllInEdges())
		{
			
			Node in = ingoing.getSource();
			
			String RDFId = getAttributeSecure(in, Messages.getString("UtilitySuperClassToGraph.82"));
			try
			{
				NucleicAcid NA = (NucleicAcid) model.getByID(RDFId);
				interaction.setTemplate(NA);
				
			} catch (ClassCastException e)
			{
				ErrorMsg.addErrorMessage(e);
			}
			
		}
	}
}
