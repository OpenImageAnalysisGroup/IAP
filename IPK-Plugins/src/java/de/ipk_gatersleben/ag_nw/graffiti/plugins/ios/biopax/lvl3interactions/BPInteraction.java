package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.awt.Color;
import java.util.Hashtable;
import java.util.Random;

import org.AttributeHelper;
import org.biopax.paxtools.model.level3.Entity;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.StoichiometryWriter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorToGraph;

/**
 * interface for all interactions when a model is read and a graph is created
 * 
 * @author ricardo
 * 
 */
public class BPInteraction extends HelperClass
{

	protected Graph graph;
	protected Hashtable<Entity, Node> nodes;
	protected Random rand;
	protected StoichiometryWriter sW;
	protected CollectionAttribute centerAttribute;

	public BPInteraction(Graph Graph, Hashtable<Entity, Node> Nodes)
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
	protected Node findORcreateNode(Entity entity)
	{
		Node node;
		// check whether the entity is already in the graph or not
		if (nodes.get(entity) == null)
		{
			int posX = rand.nextInt(100);
			int posY = rand.nextInt(100);
			node = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(posX, posY));
			UtilityClassSelectorToGraph.chooseClassToPutAttributesToNodes(node, entity);
			nodes.put(entity, node);
		} else
		{
			node = nodes.get(entity);
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