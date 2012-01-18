/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 6.06.2006 by Christian Klukas
 * (c) 2006 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.kegg_reaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_reaction.ReactionEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_reaction.ReactionService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Id;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.MapNumber;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.MapOrg;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.ReactionType;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         (c) 2006 IPK Gatersleben, Group Network Analysis
 */
public class CreateKeggReactionNetworkAlgorithm extends AbstractAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			return null; // "Create KEGG Reaction Network";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "KEGG";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		final BackgroundTaskStatusProviderSupportingExternalCallImpl sp = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
							"Create KEGG Reaction Network...", "Please wait");
		BackgroundTaskHelper.issueSimpleTask("Create KEGG Reaction Network", "Create KEGG Reaction Network...", new Runnable() {
			public void run() {
				// KgmlIdGenerator idGen = new KgmlIdGenerator();
				// PositionGridGenerator pgg = new PositionGridGenerator(250, 30, 250);
				ArrayList<Entry> entries = new ArrayList<Entry>();
				ArrayList<Reaction> reactions = new ArrayList<Reaction>();
				ArrayList<Relation> relations = new ArrayList<Relation>();
				
				createReactionNetwork(sp, entries, reactions);
				if (sp.wantsToStop())
					sp.setCurrentStatusText2("Processing aborted");
				else {
					sp.setCurrentStatusText2("Create graph...");
					Pathway p = new Pathway(
										new KeggId("map00000"),
										new MapOrg("map"),
										new MapNumber("00000"),
										"Reaction Network",
										null,
										null,
										entries,
										reactions,
										relations);
					final Graph g = p.getGraph();
					System.out.println("Graph: " + g.getNumberOfNodes() + " nodes, " + g.getNumberOfEdges() + " edges. Create view...");
					sp.setCurrentStatusText2("Create graph view...");
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							MainFrame.getInstance().showGraph(g, getActionEvent());
						}
					});
				}
				sp.setCurrentStatusValue(100);
			}
		}, new Runnable() {
			
			public void run() {
			}
		}, sp);
	}
	
	private static Collection<Entry> getCompoundEntries(
						HashMap<String, Entry> compoundId2entry,
						Collection<String> compIds) {
		ArrayList<Entry> result = new ArrayList<Entry>();
		for (String compId : compIds) {
			if (compoundId2entry.containsKey(compId))
				result.add(compoundId2entry.get(compId));
			else {
				Entry e = new Entry(
									new Id(Pathway.getNextID() + ""),
									new KeggId(compId), EntryType.compound, null, null, new ArrayList<KeggId>(), new ArrayList<IdRef>(), null);
				result.add(e);
				compoundId2entry.put(compId, e);
			}
		}
		return result;
	}
	
	// private void connectNodeWithNodes(Node goNode, Node newGoNode) {
	// if (goNode==null || newGoNode==null)
	// return;
	// if (!goNode.getNeighbors().contains(newGoNode)) {
	// Edge e = goNode.getGraph().addEdge(goNode, newGoNode, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, true));
	// AttributeHelper.setBorderWidth(e, 3d);
	// }
	// }
	
	private static void createReactionNetwork(final BackgroundTaskStatusProviderSupportingExternalCallImpl sp, ArrayList<Entry> entries,
						ArrayList<Reaction> reactions) {
		HashMap<String, Entry> compoundId2entry = new HashMap<String, Entry>();
		Collection<String> reationIds = ReactionService.getAllReactionIds();
		int i = 0;
		int workLoad = reationIds.size();
		for (String rid : reationIds) {
			ReactionEntry re = ReactionService.getReactionFromId(rid);
			Collection<String> sub = re.getSubstrateNames();
			Collection<String> enz = re.getEnzymeNames();
			Collection<String> prod = re.getProductNames();
			KeggId rref = new KeggId(rid);
			
			if (enz != null && enz.size() > 0)
				for (String enzymeId : enz) {
					KeggId kid = new KeggId(enzymeId);
					Entry enzymeEntry = new Entry(
										new Id(Pathway.getNextID() + ""),
										kid,
										EntryType.enzyme, null, null,
										new ArrayList<KeggId>(), null, null);
					kid.setReference(enzymeEntry);
					enzymeEntry.addReaction(rref);
					entries.add(enzymeEntry);
				}
			
			Collection<Entry> subEntries, prodEntries;
			subEntries = new ArrayList<Entry>();
			prodEntries = new ArrayList<Entry>();
			
			if (sub != null && sub.size() > 0)
				subEntries.addAll(getCompoundEntries(compoundId2entry, sub));
			if (prod != null && prod.size() > 0)
				prodEntries.addAll(getCompoundEntries(compoundId2entry, prod));
			if (subEntries.size() > 0 && prodEntries.size() > 0) {
				Reaction r = new Reaction(rid, ReactionType.reversible, subEntries, prodEntries);
				reactions.add(r);
				rref.setReference(r);
			}
			if (sp != null) {
				sp.setCurrentStatusValueFine(100d * i / workLoad);
				sp.setCurrentStatusText2("Evaluate " + rid + " (" + (++i) + "/" + workLoad + ")");
				if (sp.wantsToStop())
					break;
			}
		}
		entries.addAll(compoundId2entry.values());
		if (sp != null)
			sp.setCurrentStatusText1("Finish transaction (update view, please wait)...");
	}
	
	public static Graph getReactionNetwork() {
		Pathway.resetIdGen();
		// PositionGridGenerator pgg = new PositionGridGenerator(250, 30, 250);
		ArrayList<Entry> entries = new ArrayList<Entry>();
		ArrayList<Reaction> reactions = new ArrayList<Reaction>();
		ArrayList<Relation> relations = new ArrayList<Relation>();
		
		createReactionNetwork(null, entries, reactions);
		Pathway p = new Pathway(
							new KeggId("map00000"),
							new MapOrg("map"),
							new MapNumber("00000"),
							"Reaction Network",
							null,
							null,
							entries,
							reactions,
							relations);
		Graph g = p.getGraph();
		System.out.println("Graph: " + g.getNumberOfNodes() + " nodes, " + g.getNumberOfEdges() + " edges. Create view...");
		return g;
	}
}
