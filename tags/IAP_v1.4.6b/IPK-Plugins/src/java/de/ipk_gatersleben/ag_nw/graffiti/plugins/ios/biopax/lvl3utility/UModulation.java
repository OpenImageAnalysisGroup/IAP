package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility;

import java.lang.reflect.InvocationTargetException;

import org.AttributeHelper;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Controller;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.Process;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class UModulation extends UtilitySuperClassToGraph
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
	public static void addAttributesToNode(GraphElement elem, Modulation i) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// first set label to node
		// first set label to node
		setLabels(elem, i);
		elem.setString(Messages.getString("UtilitySuperClassToGraph.127"), Messages.getString("UtilitySuperClassToGraph.135")); //$NON-NLS-1$ //$NON-NLS-2$
		// set attribute paths
		// set attribute paths
		setAvailability(elem, i.getAvailability());
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
		Edge elem = (Edge) edge;
		String RDFID = getAttributeSecure(elem, Messages.getString("UtilitySuperClassToGraph.82"));
		Modulation interaction = model.addNew(Modulation.class, RDFID);

		UtilitySuperClassFromGraph.getDisplayName(elem, interaction);
		UtilitySuperClassFromGraph.getAvailability(elem, interaction);
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
		// suche die entsprechenden CatalysenIds
		for (Edge e : elem.getGraph().getEdges())
		{

			if (AttributeHelper.hasAttribute(e, Messages.getString("UtilitySuperClassToGraph.82")))
			{
				String currentEdgeRDFId = getAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.82"));

				if (currentEdgeRDFId.matches(RDFID))
				{
					Node controlNode = e.getSource();

					String controlNodeRDFId = getAttributeSecure(controlNode, Messages.getString("UtilitySuperClassToGraph.82"));
					String processNodeRDFId = getAttributeSecure(e, Messages.getString("UtilitySuperClassToGraph.115"));

					Controller control = (Controller) model.getByID(controlNodeRDFId);
					Process process = (Process) model.getByID(processNodeRDFId);

					interaction.addController(control);
					interaction.addControlled(process);

				}
			}

		}
	}
}
