package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.util.ArrayList;
import java.util.Set;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Process;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Graph;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.HelperClass;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;

public class PathWayHandler extends HelperClass
{

	private ArrayList<MyPathWay> pathways;
	private Graph graph;
	private Model model;

	/**
	 * constructor for import
	 * 
	 * @param model
	 */
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

		Set<Pathway> pathWays = this.model.getObjects(Pathway.class);
		for (Pathway p : pathWays)
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
			for (Process process : p.getPathwayComponents())
			{
				setAttributeWithTwoInnerReplacements(graph, Messages.getString("UtilitySuperClassToGraph.125"), i, j, process.getRDFId());
				j++;
			}
			i++;
		}

	}

	/**
	 * use while export to write PathWays into the model
	 */
	public void readPathwaysFromGraphAndWriteToModel()
	{
		ArrayList<Attribute> set = getAttributeOfSetOfString(Messages.getString("UtilitySuperClassToGraph.123"), graph);
		for (int i = 1; i <= set.size(); i++)
		{
			Attribute RDFIdAttr = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.123"), graph, i);
			String RDFId = RDFIdAttr.getValue().toString();
			Attribute DisplayNameAttr = getAttributeWithOneSpecificInnerReplacement(Messages.getString("UtilitySuperClassToGraph.124"), graph, i);

			Pathway p;
			if (!model.containsID(RDFId))
			{
				p = model.addNew(Pathway.class, RDFId);

				if (DisplayNameAttr != null)
					p.setDisplayName(DisplayNameAttr.getValue().toString());

				/*
				 * find all names belonging to that provenance
				 */
				ArrayList<Attribute> secondset = getAttributeOfSetWithTwoInnerReplacements(Messages.getString("UtilitySuperClassToGraph.125"), graph, i);
				for (Attribute A : secondset)
				{
					p.addPathwayComponent((Process) model.getByID(A.getValue().toString()));
				}

			} else
			{
				p = (Pathway) model.getByID(RDFId);
			}

		}
	}
}
