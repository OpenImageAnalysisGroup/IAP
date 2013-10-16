package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JOptionPane;

import org.AttributeHelper;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.level3.Catalysis;
import org.biopax.paxtools.model.level3.ComplexAssembly;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.Degradation;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.Modulation;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.model.level3.TemplateReactionRegulation;
import org.biopax.paxtools.model.level3.Transport;
import org.biopax.paxtools.model.level3.TransportWithBiochemicalReaction;
import org.graffiti.attributes.Attribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.Messages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3utility.UtilityClassSelectorFromGraph;

/**
 * takes an OWL model and fills the graph with edges, nodes and attributes
 * 
 * @author ricardo
 * 
 */
public class LVL3ModelConverter
{

	private Graph graph;
	private Hashtable<Entity, Node> nodes;
	private boolean debug = false;

	public void convertLVL3Model(Model model, Graph g, boolean all) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{

		graph = g;
		nodes = new Hashtable<Entity, Node>();
		if (all)
			readInteractionsFromModel(model);
	}

	public Graph getGraph()
	{
		return this.graph;
	}

	/**
	 * reads all nodes and edges of the graph first all physical entities will
	 * be read so pointing in interactions to them is possible afterwards the
	 * interactions will be read
	 * 
	 * @param model
	 * @param g
	 */
	public void readGraph(Model model, Graph g)
	{

		// first export all PhysicalEntities

		Set<String> physicalEntities_1 = new HashSet<String>();
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.150")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.151")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.152")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.153")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.154")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.155")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.156")); //$NON-NLS-1$
		physicalEntities_1.add(Messages.getString("UtilitySuperClassToGraph.157")); //$NON-NLS-1$

		for (int i = 0; i < g.getNodes().size(); i++)
		{
			Node node = g.getNodes().get(i);
			Attribute attr = node.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")); //$NON-NLS-1$
			String NodeType = attr.getValue().toString();

			if (physicalEntities_1.contains(NodeType))
			{
				UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(node, g, model);
			}

		}
		// check whether there are plain physical entities in the graph
		Set<String> physicalEntities_2 = new HashSet<String>();
		physicalEntities_2.add(Messages.getString("UtilitySuperClassToGraph.158")); //$NON-NLS-1$

		for (int i = 0; i < g.getNodes().size(); i++)
		{
			Node node = g.getNodes().get(i);
			Attribute attr = node.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")); //$NON-NLS-1$
			String NodeType = attr.getValue().toString();

			if (physicalEntities_2.contains(NodeType) && !physicalEntities_1.contains(NodeType))
			{
				UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(node, g, model);
			}

		}
		// read all interactions now
		for (int i = 0; i < g.getNodes().size(); i++)
		{
			Node node = g.getNodes().get(i);
			Attribute attr = node.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")); //$NON-NLS-1$
			String NodeType = attr.getValue().toString();

			if (!physicalEntities_2.contains(NodeType) && !physicalEntities_1.contains(NodeType))
			{
				UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(node, g, model);
			}
		}
		// read all controls now
		for (Edge edge : g.getEdges())
		{
			if (AttributeHelper.hasAttribute(edge, Messages.getString("UtilitySuperClassToGraph.126"))) //$NON-NLS-1$
			{
				Attribute attr = edge.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")); //$NON-NLS-1$
				String NodeType = attr.getValue().toString();
				if (!NodeType.matches(Messages.getString("UtilitySuperClassToGraph.159"))) //$NON-NLS-1$
					UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(edge, g, model);
			}
		}
		// at last modulations because they depend on catalyses
		for (Edge edge : g.getEdges())
		{
			if (AttributeHelper.hasAttribute(edge, Messages.getString("UtilitySuperClassToGraph.126"))) //$NON-NLS-1$
			{
				Attribute attr = edge.getAttribute(Messages.getString("UtilitySuperClassToGraph.126")); //$NON-NLS-1$
				String NodeType = attr.getValue().toString();
				if (NodeType.matches(Messages.getString("UtilitySuperClassToGraph.160"))) //$NON-NLS-1$
					UtilityClassSelectorFromGraph.chooseClassToPutAttributesToModell(edge, g, model);
			}
		}
		// read all PathWays

		PathWayHandler myPathWayHandler = new PathWayHandler(model, g);
		myPathWayHandler.readPathwaysFromGraphAndWriteToModel();

	}

	/**
	 * made for reading all interactions in a biopax level 3 model and writing
	 * it to the graph
	 * 
	 * @param model
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void readInteractionsFromModel(Model model) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Set<Class> Interactions = new HashSet<Class>();
		// look for top-level interactions

		// Interactions.add(Interaction.class);
		Interactions.add(GeneticInteraction.class);
		Interactions.add(MolecularInteraction.class);
		Interactions.add(TemplateReaction.class);
		Interactions.add(Control.class);
		// and their subclasses
		Interactions.add(Catalysis.class);
		Interactions.add(TemplateReactionRegulation.class);
		Interactions.add(Modulation.class);
		// <--------------------
		Interactions.add(Conversion.class);
		// and their subclasses
		Interactions.add(ComplexAssembly.class);
		Interactions.add(BiochemicalReaction.class);
		Interactions.add(Degradation.class);
		Interactions.add(Transport.class);
		// and their Subclasses
		Interactions.add(TransportWithBiochemicalReaction.class);

		// second: add to a set every Entity you are looking for
		Set<Interaction> inters = new HashSet<Interaction>();
		for (Class c : Interactions)
		{
			inters.addAll(model.getObjects(c));
		}

		writeInteractionsToGraph(inters);

		PathWayHandler PathWH = new PathWayHandler(model, graph);
		PathWH.writePathWaysToGraph();
	}

	/**
	 * made for partial import of one or more pathway components
	 * 
	 * @param originalModel
	 * @param processes
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public void readInteractionsFromPathway(Model originalModel, ArrayList<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> arrayList) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{

		Set<Interaction> inters = new HashSet<Interaction>();
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay path : arrayList)
		{
			if (debug)
				System.out.println("Number of Components: " + path.getPathwayComponents().size());
			if (path.isSuperPathWay())
			{
				getSubProcesses(path.getSubPathWays(), inters);

				for (Process proc : path.getPathwayComponents())
				{
					inters.add((Interaction) proc);
				}
			} else
			{
				for (Process proc : path.getPathwayComponents())
				{
					inters.add((Interaction) proc);
				}
			}

		}
		System.out.println("here");
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
	private void mayLoadAdditionalControls(Set<Interaction> inters, Model originalModel)
	{
		int answer = JOptionPane.showOptionDialog(JOptionPane.getRootFrame().getFocusOwner(), "Do you want to import corresponding BioPax-Control-Elements into the Graph too?", "Please choose an option!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No" }, null);
		if (answer == 0)
		{
			for (Control c : originalModel.getObjects(Control.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (Process p : c.getControlled())
				{
					for (Interaction participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add((Interaction) p);
						}
					}
				}
			}
			for (Control c : originalModel.getObjects(Catalysis.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (Process p : c.getControlled())
				{
					for (Interaction participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add((Interaction) p);
						}
					}
				}
			}
			for (Control c : originalModel.getObjects(Modulation.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (Process p : c.getControlled())
				{
					for (Interaction participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add((Interaction) p);
						}
					}
				}
			}
			for (Control c : originalModel.getObjects(TemplateReactionRegulation.class))
			{
				// look for each control in model whether it is used in a
				// process in inters
				for (Process p : c.getControlled())
				{
					for (Interaction participant : inters)
					{
						if (p.getRDFId().matches(participant.getRDFId()))
						{
							inters.add((Interaction) p);
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
	private void getSubProcesses(Set<de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay> set, Set<Interaction> inters)
	{
		for (de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.lvl3interactions.MyPathWay mypath : set)
		{
			if (mypath.isSuperPathWay())
			{
				getSubProcesses(mypath.getSubPathWays(), inters);

				for (Process proc : mypath.getPathwayComponents())
				{
					inters.add((Interaction) proc);
				}
			} else
			{
				for (Process proc : mypath.getPathwayComponents())
				{
					inters.add((Interaction) proc);
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
	private void writeInteractionsToGraph(Set<Interaction> inters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		if (debug)
			System.out.println("Number of Interactions: " + inters.size());
		// read all non-control interactions
		for (Interaction i : inters)
		{
			// CONVERSIONS
			if (i instanceof BiochemicalReaction & !(i instanceof TransportWithBiochemicalReaction))
			{
				BPBiochemicalReaction BP = new BPBiochemicalReaction(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof ComplexAssembly)
			{
				BPComplexAssembly BP = new BPComplexAssembly(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof Degradation)
			{
				BPDegradation BP = new BPDegradation(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof Transport & !(i instanceof TransportWithBiochemicalReaction))
			{
				BPTransport BP = new BPTransport(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof TransportWithBiochemicalReaction)
			{
				BPTransportWithBiochemicalReaction BP = new BPTransportWithBiochemicalReaction(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof Conversion)
			{
				BPConversion BP = new BPConversion(this.graph, this.nodes);
				BP.read(i);
			}

			// INTERACTION TOP CLASSES
			else if (i instanceof GeneticInteraction)
			{
				BPGeneticInteraction BP = new BPGeneticInteraction(this.graph, this.nodes);
				BP.read(i);
			}

			else if (i instanceof MolecularInteraction)
			{
				BPMolecularInteraction BP = new BPMolecularInteraction(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof TemplateReaction && !(i instanceof TemplateReactionRegulation))
			{
				BPTemplateReaction BP = new BPTemplateReaction(this.graph, this.nodes);
				BP.read(i);
			}
			if (debug)
			{
				System.out.println("Gelesen: " + i.toString() + " " + i.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
			}

		}
		// now read the controls that point to the interactions already read
		for (Interaction i : inters)
		{
			// CONTROLS
			if (i instanceof Catalysis)
			{
				BPCatalysis BP = new BPCatalysis(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof TemplateReactionRegulation)
			{
				BPTemplateReactionRegulation BP = new BPTemplateReactionRegulation(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof Modulation)
			{
				BPModulation BP = new BPModulation(this.graph, this.nodes);
				BP.read(i);
			} else if (i instanceof Control)
			{
				BPControl BP = new BPControl(this.graph, this.nodes);
				BP.read(i);
			}
			if (debug)
			{
				System.out.println("Gelesen: " + i.toString() + " " + i.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

	}

}
