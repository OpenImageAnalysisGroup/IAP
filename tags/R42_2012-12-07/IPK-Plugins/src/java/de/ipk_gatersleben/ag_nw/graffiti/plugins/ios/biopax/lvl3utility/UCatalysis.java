package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.AttributeHelper;
import org.ErrorMsg;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UCatalysis extends UtilitySuperClassToGraph
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
	public static void addAttributesToNode(GraphElement elem, Catalysis i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.129")); //$NON-NLS-1$ //$NON-NLS-2$
		setAvailability(elem, i.getAvailability());
		setCatalysisDirection(elem, i.getCatalysisDirection());
		setCofactor(elem, i.getCofactor());
		setControlType(elem, i.getControlType());
		setComment(elem, i.getComment());
		setDataSource(elem, i.getDataSource());
		setEvidence(elem, i.getEvidence());
		setInteractionType(elem, i.getInteractionType());
		setName(elem, i.getName());
		setRDFId(elem, i.getRDFId());
		setStandardName(elem, i.getStandardName());
		setXRef(elem, i.getXref());
		
	}
	
	public static void readAttributesFromNode(GraphElement edge, Graph g, Model model)
	{
		Edge elem = null;
		try
		{
			elem = (Edge) edge;
		} catch (ClassCastException e)
		{
			ErrorMsg.addErrorMessage(getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82")));
		}
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));
		if (!model.containsID(RDFID))
		{
			Catalysis interaction = model.addNew(Catalysis.class, RDFID);
			
			UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
			UtilitySuperClassFromGraph.getAvailability(elem, interaction);
			UtilitySuperClassFromGraph.getCatalysisDirection(elem, interaction);
			UtilitySuperClassFromGraph.getCofactor(elem, interaction, model);
			UtilitySuperClassFromGraph.getControlType(elem, interaction);
			UtilitySuperClassFromGraph.getComment(elem, interaction);
			UtilitySuperClassFromGraph.getDataSource(elem, interaction, model);
			UtilitySuperClassFromGraph.getEvidence(elem, interaction, model);
			UtilitySuperClassFromGraph.getInteractionType(elem, interaction, model);
			UtilitySuperClassFromGraph.getName(elem, interaction);
			UtilitySuperClassFromGraph.getStandardName(elem, interaction);
			UtilitySuperClassFromGraph.getXRef(elem, interaction, model);
			
			// iteriere �ber alle Kanten und finde alle mit der gleichen RDFId
			// f�ge bei gleicher RDFId die Controller und die Processes in eine
			// Menge
			for (Edge e : elem.getGraph().getEdges())
			{
				
				if (AttributeHelper.hasAttribute(e, Messages.getString("UtilitySuperClassToGraph.82")))
				{
					String currentEdgeRDFId = getAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.82"));
					
					if (currentEdgeRDFId.matches(RDFID))
					{
						Node controlNode = e.getSource();
						Node processNode = e.getTarget();
						
						String controlNodeRDFId = getAttributeSecure(controlNode, Messages.getString("UtilitySuperClassToGraph.82"));
						String processNodeRDFId = getAttributeSecure(processNode, Messages.getString("UtilitySuperClassToGraph.82"));
						
						Controller control = (Controller) model.getByID(controlNodeRDFId);
						Process process = (Process) model.getByID(processNodeRDFId);
						
						interaction.addController(control);
						interaction.addControlled(process);
						
						String cofactor = getAttributeSecure(controlNode, Messages.getString("UtilitySuperClassToGraph.82"));
						if (cofactor.matches("true"))
						{
							interaction.addCofactor((PhysicalEntity) control);
						}
					}
				}
			}
		}
	}
}
