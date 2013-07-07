package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Random;

import org.AttributeHelper;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.StoichiometryWriter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2utility.UtilityClassSelectorToGraph;

/**
 * interface for all interactions when a model is read and a graph is created
 * each class has an own read and "write" method for exporting and importing.
 * those provide calls to functions to set attributes either in the graph or in
 * the OWL file
 * 
 * @author ricardo
 * 
 */
public class BPinteraction extends HelperClass
{
	protected Random rand;
	protected StoichiometryWriter sW;
	protected CollectionAttribute centerAttribute;
	protected Graph graph;
	protected Hashtable<String, Node> nodes;

	public BPinteraction(Graph Graph, Hashtable<String, Node> Nodes)
	{
		super();
		rand = new Random();
		sW = new StoichiometryWriter();
		graph = Graph;
		nodes = Nodes;

		int posx = rand.nextInt(100);
		int posy = rand.nextInt(100);
		centerAttribute = AttributeHelper.getDefaultGraphicsAttributeForNode(posx, posy);
	}

	/**
	 * used for linking nodes with other nodes via edges
	 * 
	 * @param entity
	 * @return
	 */
	protected Node findORcreateNode(physicalEntityParticipant entity)
	{
		Node node;
		// check whether the entity is already in the graph or not
		if (nodes.get(entity.getPHYSICAL_ENTITY().getRDFId()) == null)
		{
			int posX = rand.nextInt(100);
			int posY = rand.nextInt(100);
			node = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(posX, posY));
			UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(node, entity.getPHYSICAL_ENTITY());
			nodes.put(entity.getPHYSICAL_ENTITY().getRDFId(), node);
		} else
		{
			node = nodes.get(entity.getPHYSICAL_ENTITY().getRDFId());
		}
		return node;
	}

	/**
	 * used for linking nodes with other nodes via edges
	 * 
	 * @param entity
	 * @return
	 */
	protected Node findORcreateNode(process p)
	{
		Node processNode;
		// check whether the entity is already in the graph or not
		if (nodes.get(p.getRDFId()) == null)
		{
			int posX = rand.nextInt(100);
			int posY = rand.nextInt(100);
			processNode = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(posX, posY));
			UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(processNode, p);
			nodes.put(p.getRDFId(), processNode);
		} else
		{
			processNode = nodes.get(p.getRDFId());
		}
		return processNode;
	}

	/**
	 * used for linking nodes with other nodes via edges
	 * 
	 * @param entity
	 * @return
	 */
	protected Node findORcreateNode(InteractionParticipant l)
	{
		Node node;
		// check whether the entity is already in the graph or not
		if (nodes.get(l.getRDFId()) == null)
		{
			int posX = rand.nextInt(100);
			int posY = rand.nextInt(100);
			node = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(posX, posY));
			UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(node, l);
			nodes.put(l.getRDFId(), node);
		} else
		{
			node = nodes.get(l.getRDFId());
		}
		return node;
	}

	/**
	 * shortcut to add an edge without calling each time such a long method
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	protected Edge addEdge(Node from, Node to)
	{
		return graph.addEdge(from, to, Boolean.TRUE, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.black, Color.black, true));
	}
}