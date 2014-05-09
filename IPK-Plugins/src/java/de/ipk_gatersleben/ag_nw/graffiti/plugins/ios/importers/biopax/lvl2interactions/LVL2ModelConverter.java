package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.biopax.lvl2interactions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JOptionPane;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.complexAssembly;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalInteraction;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.model.level2.transport;
import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.PathWayHandler;

/**
 * takes an OWL model and fills the graph with edges, nodes and attributes
 * 
 * @author ricardo
 * 
 */
public class LVL2ModelConverter
{

	private Graph graph;
	private Hashtable<String, Node> nodes;
	private boolean debug = false;

	public void convertLVL2Model(Model model, Graph g, boolean all) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		graph = g;
		nodes = new Hashtable<String, Node>();
		if (all)
			readInteractionsFromModel(model);
	}

	public Graph getGraph()
	{
		return this.graph;
	}

	/**
	 * reads all interactions defined by biopax level 2 documentation
	 * 
	 * Attention: Controls will be read when every other physical entity and
	 * every other interaction is read so pointing to them is possible
	 * 
	 * @param model
	 *            comes from the input stream
	 * @param g
	 *            is filled by this method
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readInteractionsFromModel(Model model) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Set<Class> Interactions = new HashSet<Class>();
		// look for top-level interactions

		// Interactions.add(Interaction.class);
		Interactions.add(interaction.class);
		Interactions.add(physicalInteraction.class);
		// and their subclasses
		Interactions.add(conversion.class);
		Interactions.add(control.class);
		// <--------------------
		// and their subclasses
		Interactions.add(biochemicalReaction.class);
		Interactions.add(transport.class);
		Interactions.add(complexAssembly.class);
		// ############################################
		Interactions.add(transportWithBiochemicalReaction.class);
		// <--------------------
		// and their subclasses
		Interactions.add(catalysis.class);
		Interactions.add(modulation.class);

		// second: add to a set every Entity you are looking for
		Set<Level2Element> inters = new HashSet<Level2Element>();
		for (Class c : Interactions)
		{
			inters.addAll(model.getObjects(c));

		}
		if (debug)
		{
			System.out.println("LVL2 Objekte gelesen: " + inters.size());
		}
		writeInteractionsToGraph(inters);

		PathWayHandler PathWH = new PathWayHandler(model, graph);
		PathWH.writePathWaysToGraph();
	}

	/**
	 * made for reading all interactions in a biopax level 3 model and writing
	 * it to the graph
	 * 
	 * @param originalModel
	 * @param processes
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void readInteractionsFromPathway(Model originalModel, ArrayList<MyPathWay> arrayList) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Set<Level2Element> inters = new HashSet<Level2Element>();
		for (MyPathWay path : arrayList)
		{
			if (path.isSuperPathWay())
			{
				getSubProcesses(path.getSubPathWays(), inters);

				for (pathwayComponent proc : path.getPathwayComponents())
				{

					inters.add(proc);
				}
			} else
			{
				for (pathwayComponent proc : path.getPathwayComponents())
				{
					inters.add(proc);
				}
			}

		}

		mayLoadAdditionalControls(inters, originalModel);
		writeInteractionsToGraph(inters);

		PathWayHandler PathWH = new PathWayHandler(originalModel, graph);
		PathWH.writePathWaysToGraph();
	}

	/**
	 * some times subset pathways aren't constructed write and contain only the
	 * main interactions. if so on can simply iterate over all control
	 * statements and find related ones and add them
	 * 
	 * @param inters
	 * @param originalModel
	 */
	private void mayLoadAdditionalControls(Set<Level2Element> inters, Model originalModel)
	{
		int answer = JOptionPane.showOptionDialog(JOptionPane.getRootFrame().getFocusOwner(), "Do you want to import corresponding BioPax-Control-Elements into the Graph too?", "Please choose an option!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No" }, null);
		if (answer == 0)
		{
			for (control c : originalModel.getObjects(control.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (process p : c.getCONTROLLED())
				{
					for (Level2Element participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add(p);
						}
					}
				}
			}
			for (control c : originalModel.getObjects(catalysis.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (process p : c.getCONTROLLED())
				{
					for (Level2Element participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add(p);
						}
					}
				}
			}
			for (control c : originalModel.getObjects(modulation.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (process p : c.getCONTROLLED())
				{
					for (Level2Element participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add(p);
						}
					}
				}
			}
		}
	}

	/**
	 * if it is a main pathway that is loaded this function looks for all
	 * subpathway compenents and loads them as well
	 * 
	 * @param set
	 * @param inters
	 */
	private void getSubProcesses(Set<MyPathWay> subPathWays, Set<Level2Element> inters)
	{
		for (MyPathWay mypath : subPathWays)
		{
			if (mypath.isSuperPathWay())
			{
				getSubProcesses(mypath.getSubPathWays(), inters);

				for (pathwayComponent proc : mypath.getPathwayComponents())
				{
					inters.add(proc);
				}
			} else
			{
				for (pathwayComponent proc : mypath.getPathwayComponents())
				{
					inters.add(proc);
				}
			}
		}
	}

	/**
	 * function to read all interactions and writing them to the graph
	 * independent of the model
	 * 
	 * @param inters
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void writeInteractionsToGraph(Set<Level2Element> inters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{

		Set<process> newinters = new HashSet<process>();
		for (Level2Element i : inters)
		{
			if (i instanceof pathwayStep)
			{
				for (process p : ((pathwayStep) i).getSTEP_INTERACTIONS())
				{
					newinters.add(p);
				}
			} else
			{
				newinters.add((process) i);
			}

		}
		// read all non-control interactions
		for (process i : newinters)
		{

			// CONVERSIONS
			if (i instanceof biochemicalReaction & !(i instanceof transportWithBiochemicalReaction))
			{
				BPbiochemicalReaction BP = new BPbiochemicalReaction(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof complexAssembly)
			{
				BPcomplexAssembly BP = new BPcomplexAssembly(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof transport & !(i instanceof transportWithBiochemicalReaction))
			{
				BPtransport BP = new BPtransport(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof transportWithBiochemicalReaction)
			{
				BPtransportWithBiochemicalReaction BP = new BPtransportWithBiochemicalReaction(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof conversion)
			{
				BPconversion BP = new BPconversion(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof physicalInteraction && !(i instanceof control))
			{
				BPphysicalInteraction BP = new BPphysicalInteraction(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			}

		}
		// now read the controls that point to the interactions already read
		for (Level2Element i : inters)
		{
			// CONTROLS
			if (i instanceof catalysis)
			{
				BPcatalysis BP = new BPcatalysis(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof modulation)
			{
				BPmodulation BP = new BPmodulation(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			} else if (i instanceof control)
			{
				BPcontrol BP = new BPcontrol(graph, nodes);
				BP.read(i);
				if (debug)
				{
					System.out.println("Gelesen: " + i.toString() + " " + i.getClass());
				}
			}
		}

	}
}
