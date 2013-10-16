package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.ErrorMsg;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Transport;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UTransport extends UtilitySuperClassToGraph
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
	public static void addAttributesToNode(GraphElement elem, Transport i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.142")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		setAvailability(elem, i.getAvailability());
		setComment(elem, i.getComment());
		setConversionDirection(elem, i.getConversionDirection());
		setDataSource(elem, i.getDataSource());
		setEvidence(elem, i.getEvidence());
		setInteractionType(elem, i.getInteractionType());
		setName(elem, i.getName());
		setRDFId(elem, i.getRDFId());
		setSpontaneous(elem, i.getSpontaneous());
		setStandardName(elem, i.getStandardName());
		setXRef(elem, i.getXref());
		
	}
	
	public static void readAttributesFromNode(GraphElement node, Graph g, Model model)
	{
		Node elem = (Node) node;
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));
		Transport interaction = model.addNew(Transport.class, RDFID);
		
		UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
		UtilitySuperClassFromGraph.getAvailability(elem, interaction);
		UtilitySuperClassFromGraph.getConversionDirection(elem, interaction);
		UtilitySuperClassFromGraph.getComment(elem, interaction);
		UtilitySuperClassFromGraph.getDataSource(elem, interaction, model);
		UtilitySuperClassFromGraph.getEvidence(elem, interaction, model);
		UtilitySuperClassFromGraph.getInteractionType(elem, interaction, model);
		UtilitySuperClassFromGraph.getName(elem, interaction);
		UtilitySuperClassFromGraph.getSpontaneous(elem, interaction);
		UtilitySuperClassFromGraph.getStandardName(elem, interaction);
		UtilitySuperClassFromGraph.getXRef(elem, interaction, model);
		StoichiometryWriter sW = new StoichiometryWriter();
		
		for (Edge ingoing : elem.getAllInEdges())
		{
			
			CollectionAttribute map = ingoing.getAttributes();
			boolean isAControl = false;
			try
			{
				if (null != map.getAttribute(Messages.getString("UtilitySuperClassToGraph.82")))
				{
					// only controls put RDFIds on edges
					isAControl = true;// do nothing
				}
				
			} catch (AttributeNotFoundException e)
			{
				ErrorMsg.addErrorMessage(e);
			} finally
			{
				Node in = ingoing.getSource();
				
				String RDFId = getAttributeSecure(in, Messages.getString("UtilitySuperClassToGraph.82"));
				PhysicalEntity p = (PhysicalEntity) model.getByID(RDFId);
				
				if (!isAControl)
				{
					// so the node can be added to the conversion
					interaction.addLeft(p);
				}
				sW.readParticipantStoichiometry(p, interaction, ingoing, model);
			}
			
		}
		for (Edge outgoing : elem.getAllOutEdges())
		{
			CollectionAttribute map = outgoing.getAttributes();
			boolean isAControl = false;
			try
			{
				if (null != map.getAttribute(Messages.getString("UtilitySuperClassToGraph.82")))
				{
					// only controls put RDFIds on edges
					isAControl = true;// do nothing
				}
				
			} catch (AttributeNotFoundException e)
			{
				ErrorMsg.addErrorMessage(e);
			} finally
			{
				Node in = outgoing.getTarget();
				
				String RDFId = getAttributeSecure(in, Messages.getString("UtilitySuperClassToGraph.82"));
				PhysicalEntity p = (PhysicalEntity) model.getByID(RDFId);
				
				if (!isAControl)
				{
					// so the node can be added to the conversion
					interaction.addRight(p);
				}
				sW.readParticipantStoichiometry(p, interaction, outgoing, model);
			}
		}
	}
	
}
