package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.util.ArrayList;
import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class PathWayHandler extends HelperClass
{

	private Model model;
	private Graph graph;
	private ArrayList<MyPathWay> pathways;

	public PathWayHandler(Model model, Graph graph)
	{

		this.model = model;
		this.graph = graph;
	}

	/**
	 * use to write PathWays in an own data structure
	 */
	public void getPathWaysfromModel()
	{
		this.pathways = new ArrayList<MyPathWay>();

		Set<pathway> pathWays = this.model.getObjects(pathway.class);
		for (pathway p : pathWays)
		{
			MyPathWay temp = new MyPathWay(p);
			pathways.add(temp);
		}
		System.gc();

	}

	/**
	 * returns all PathWays found by getPathWaysfromModel
	 */
	public ArrayList<MyPathWay> getPathWays()
	{
		return pathways;

	}

	/**
	 * use while import to write PathWays as attributes on the graph
	 */
	public void writePathWaysToGraph()
	{
		getPathWaysfromModel();

		int i = 1;
		for (MyPathWay p : pathways)
		{
			setAttributeWithOneInnerReplacement(graph, Messages.getString("UtilitySuperClassToGraph.123"), i, p.getRDFId());
			if (!p.getDisplayName().matches(""))
			{
				setAttributeWithOneInnerReplacement(graph, Messages.getString("UtilitySuperClassToGraph.124"), i, p.getDisplayName());
			}
			int j = 1;
			for (pathwayComponent process : p.getPathwayComponents())
			{
				setAttributeWithTwoInnerReplacements(graph, Messages.getString("UtilitySuperClassToGraph.125"), i, j, process.getRDFId());
				j++;
			}
			i++;
		}

	}

}
