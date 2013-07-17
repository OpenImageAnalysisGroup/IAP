/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.FeatureSet;
import org.FolderPanel;
import org.GuiRow;
import org.JMButton;
import org.MarkComponent;
import org.Release;
import org.ReleaseInfo;
import org.SearchFilter;
import org.SystemAnalysis;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.AttributeListener;
import org.graffiti.event.ListenerRegistrationException;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionModel;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelperBio;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.DatabaseBasedLabelReplacementService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.MergeNodes;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectCompoundsAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectEnzymesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectGenesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectMapNodesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands.SelectMapTitleNodesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar.ColorizeSuperGraphAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar.CreateOrgSpecificSuperGraphsAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.special.HierarchyProcessing;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.IndexAndString;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Relation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.kgmlGraphicsType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.gui.KGMLerrorWindow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.sbgn.CreateSBGNgraphFromKEGGalgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author $author$
 * @version $Revision: 1.6 $
 */
public class TabKegg2 extends InspectorTab
		implements SessionListener, AttributeListener {
	private static final long serialVersionUID = 1L;
	
	ArrayList<KeggPathwayEntry> keggPathways = new ArrayList<KeggPathwayEntry>();
	HashMap<KeggPathwayEntry, DefaultMutableTreeNode> pathwayToTreeNode = new HashMap<KeggPathwayEntry, DefaultMutableTreeNode>();
	
	final String noNode = "No node is selected.";
	
	private MarkComponent mcUpdateModel;
	
	/**
	 * Initialize GUI
	 */
	private void initComponents() {
		MainFrame.getInstance().addSessionListener(this);
		int border = 5;
		this.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, border));
		this.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
		
		// JComponent helpButton = FolderPanel.getHelpButton(
		// JLabelJavaHelpLink.getHelpActionListener("panel_kegg"), getBackground());
		
		JButton mapOverview = new JButton("<html>Create map overview (Load+Analyze)");
		JButton mapOverviewSOAP = new JMButton("<html>Create Pathway-Map-Overview");
		JButton superPathway = new JMButton("<html>Load all maps in one view");
		JButton orgSpecificSuperPathway = new JMButton("<html>Turn reference map into organism-specific");
		JButton createAllOrgSpecificSuperPathways = new JMButton("<html>Create organism specific pathways from active reference map");
		JButton kegg2sbgnPathway = new JMButton("<html>Set SBGN Style");
		JButton condenseItems = new JMButton("<html>Condense into single entities");
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			condenseItems.setText("<html>Merge nodes");
		JButton selectDuplicateItems = new JMButton("<html>Find duplicate entries");
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			selectDuplicateItems.setText("<html>Select multiple occurring nodes");
		}
		JButton connectItems = new JMButton("<html>Connect entities with same ID");
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			connectItems.setText("<html>Add edge connection between nodes");
		
		JButton addAllElementsToMapNode = new JMButton("<html>Add and connect pathway elements");
		JButton addEnzymesToMapNode = new JMButton("<html>Add and connect enzymes");
		JButton addCompoundsToMapNode = new JMButton("<html>Add and connect compounds");
		JButton addGenesToMapNode = new JMButton("<html>Add and connect genes");
		
		JButton specialCalculateHierarchyEnzymeRatio = new JMButton("<html>Hierarchy Enzyme Coverage");
		JButton specialCalculateHierarchyGenesRatio = new JMButton("<html>Hierarchy Gene Coverage");
		
		JButton selEnzymes = new JMButton("<html>Select enzymes");
		JButton selCompounds = new JMButton("<html>Select compounds");
		JButton selGenes = new JMButton("<html>Select genes");
		JButton selMapNodes = new JMButton("<html>Select map nodes");
		JButton selMapTitleNodes = new JMButton("<html>Select map reference nodes");
		
		JButton downloadMol = new JMButton("<html>Download compound structures");
		
		JButton kgmlErrors = new JMButton("<html>Update KGML model");
		
		FolderPanel fpWarnings = KGMLerrorWindow.createFolderPaneWarnings(false);
		FolderPanel fpErrors = KGMLerrorWindow.createFolderPaneErrors(false);
		FolderPanel fpEntries = new FolderPanel("Entries", true, true, false, null);
		FolderPanel fpRelations = new FolderPanel("Relations", true, true, false, null);
		FolderPanel fpReactions = new FolderPanel("Reactions", true, true, false, null);
		pretifyFolderPanel(fpWarnings, false);
		pretifyFolderPanel(fpErrors, false);
		pretifyFolderPanel(fpEntries, true);
		pretifyFolderPanel(fpRelations, true);
		pretifyFolderPanel(fpReactions, true);
		
		fpEntries.addSearchFilter(new SearchFilter() {
			@Override
			public boolean accept(GuiRow gr, String searchText) {
				if (gr == null || gr.left == null || searchText == null)
					return true;
				searchText = searchText.toUpperCase();
				EntryLabel el = (EntryLabel) gr.left;
				Entry entry = el.getEntry();
				return entry.getVisibleName().toUpperCase().contains(searchText) ||
						entry.getName().getId().toUpperCase().contains(searchText);
			}
		});
		fpRelations.addSearchFilter(new SearchFilter() {
			@Override
			public boolean accept(GuiRow gr, String searchText) {
				if (gr == null || gr.left == null)
					return true;
				searchText = searchText.toUpperCase();
				RelationLabel rl = (RelationLabel) gr.left;
				Relation rel = rl.getRelation();
				return rel.toStringWithKeggIDs().toUpperCase().contains(searchText)
						|| rel.toStringWithKeggNames().toUpperCase().contains(searchText);
			}
		});
		fpReactions.addSearchFilter(new SearchFilter() {
			@Override
			public boolean accept(GuiRow gr, String searchText) {
				if (gr == null || gr.left == null)
					return true;
				searchText = searchText.toUpperCase();
				ReactionLabel rl = (ReactionLabel) gr.left;
				Reaction rea = rl.getReaction();
				return rea.toStringWithDetails(false, false).toUpperCase().contains(searchText);
			}
		});
		
		initButtonCommands(mapOverview, mapOverviewSOAP, superPathway, orgSpecificSuperPathway,
				createAllOrgSpecificSuperPathways, condenseItems, connectItems, selectDuplicateItems, selEnzymes,
				selCompounds, selGenes, selMapNodes, selMapTitleNodes, addAllElementsToMapNode,
				addEnzymesToMapNode, addCompoundsToMapNode, addGenesToMapNode,
				kgmlErrors,
				fpWarnings, fpErrors, fpEntries, fpRelations, fpReactions, downloadMol,
				specialCalculateHierarchyEnzymeRatio, specialCalculateHierarchyGenesRatio, kegg2sbgnPathway);
		
		// this.add(new JLabel("<html><br>"));
		
		FolderPanel fp1pathways = new FolderPanel("KEGG Pathways", false, true, false, null);
		FolderPanel fp4mapnodes = new FolderPanel("KEGG Map Reference Nodes", false, true, false, null);
		String lll;
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			lll = "Process Entries with same ID";
		else
			lll = "Process nodes with the same label";
		FolderPanel fp2processentities = new FolderPanel(lll, false, true, false, null);
		FolderPanel fp3selectge = new FolderPanel("Select Graph Elements",
				ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR, true, false, null);
		FolderPanel fp5kgmlinterpret = new FolderPanel("KGML Pathway Interpretation", false, true, false, null);
		
		FolderPanel fp6molProcessing = new FolderPanel("KEGG Compound Images", false, true, false, null);
		
		// fp1.setBackground(Color.BLACK);
		// fp1.setFrameColor(null, Color.BLACK, 1, 1);
		// fp1.setEmptyBorderWidth(0);
		// fp2.setBackgrouroond(Color.BLACK);
		// fp2.setFrameColor(null, Color.BLACK, 1, 1);
		// fp2.setEmptyBorderWidth(0);
		// fp3.setBackground(Color.BLACK);
		// fp3.setFrameColor(null, Color.BLACK, 1, 1);
		// fp3.setEmptyBorderWidth(0);
		// fp4.setBackground(Color.BLACK);
		// fp4.setFrameColor(null, Color.BLACK, 1, 1);
		// fp4.setEmptyBorderWidth(0);
		// fp5.setBackground(Color.BLACK);
		// fp5.setFrameColor(null, Color.BLACK, 1, 1);
		// fp5.setEmptyBorderWidth(0);
		Color bc = getBackground();
		fp1pathways.setBackground(bc);
		fp2processentities.setBackground(bc);
		fp3selectge.setBackground(bc);
		fp4mapnodes.setBackground(bc);
		fp5kgmlinterpret.setBackground(bc);
		fp6molProcessing.setBackground(bc);
		Color c1 = new JTabbedPane().getBackground();
		Color c2 = Color.BLACK;
		fp1pathways.setFrameColor(c1, c2, 0, 2);
		fp2processentities.setFrameColor(c1, c2, 0, 2);
		fp3selectge.setFrameColor(c1, c2, 0, 2);
		fp4mapnodes.setFrameColor(c1, c2, 0, 2);
		fp6molProcessing.setFrameColor(c1, c2, 0, 2);
		
		int spX = 3;
		
		mcUpdateModel = new MarkComponent(kgmlErrors, false, TableLayout.FILL, true);
		mcUpdateModel.setOpaque(false);
		mcUpdateModel.setBackground(null);
		kgmlErrors.setOpaque(false);
		kgmlErrors.setBackground(null);
		
		fp5kgmlinterpret.addGuiComponentRow(null, mcUpdateModel, false, spX);
		// fp5kgmlinterpret.addGuiComponentRow(null,
		// new JLabel("<html><font color='gray'><small>" +
		// "^^^ update KGML model before accessing model elements in the following lists:"), false, spX);
		fp5kgmlinterpret.addGuiComponentRow(null, fpWarnings, false, spX);
		fp5kgmlinterpret.addGuiComponentRow(null, fpErrors, false, spX);
		fp5kgmlinterpret.addGuiComponentRow(null, fpEntries, false, spX);
		fp5kgmlinterpret.addGuiComponentRow(null, fpRelations, false, spX);
		fp5kgmlinterpret.addGuiComponentRow(null, fpReactions, false, spX);
		fp5kgmlinterpret.addGuiComponentRow(null,
				new JLabel("<html><font color='gray'><small>" +
						"<b>Hints</b><br> " +
						"(1) Select graph elements and use the Node and Edge side panels to easily modify certain model properties<br>" +
						"(2) Double click graph nodes or edges, to easily review or edit related KGML model  entities"), false, spX);
		
		// fp1.addGuiComponentRow(null, mapOverview, false, 2);
		
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			// fp1pathways.addGuiComponentRow(null, superPathway, false, 2);
			fp1pathways.addGuiComponentRow(null, orgSpecificSuperPathway, false, 2);
			fp1pathways.addGuiComponentRow(null, createAllOrgSpecificSuperPathways, false, 2);
		} else
			fp1pathways.addGuiComponentRow(null, mapOverviewSOAP, false, 2);
		// if (ReleaseInfo.getIsAllowedFeature(FeatureSet.SBGN))
		// fp1pathways.addGuiComponentRow(null, kegg2sbgnPathway, false, 2);
		
		// this.add(new
		// JLabel("<html><font color=\"gray\"><small>^^ These commands may require a longer processing time. Patience and 1 GB RAM are recommended.<br><br>"));
		
		fp4mapnodes.addGuiComponentRow(null, addAllElementsToMapNode, false, 2);
		fp4mapnodes.addGuiComponentRow(null, addEnzymesToMapNode, false, 2);
		fp4mapnodes.addGuiComponentRow(null, addCompoundsToMapNode, false, 2);
		fp4mapnodes.addGuiComponentRow(null, addGenesToMapNode, false, 2);
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
			fp4mapnodes.addGuiComponentRow(null, specialCalculateHierarchyEnzymeRatio, false, 2);
		
		fp2processentities.addGuiComponentRow(null, condenseItems, false, 2);
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			fp2processentities.addGuiComponentRow(null, connectItems, false, 2);
		
		fp2processentities.addGuiComponentRow(null, selectDuplicateItems, false, 2);
		
		// this.add(new JLabel("<html><font color=\"gray\"><small>^^ These commands consider the selected nodes or all nodes, if no node is selected.<br><br>"));
		
		fp3selectge.addGuiComponentRow(null, selEnzymes, false, 2);
		fp3selectge.addGuiComponentRow(null, selCompounds, false, 2);
		fp3selectge.addGuiComponentRow(null, selGenes, false, 2);
		fp3selectge.addGuiComponentRow(null, selMapNodes, false, 2);
		fp3selectge.addGuiComponentRow(null, selMapTitleNodes, false, 2);
		
		fp6molProcessing.addGuiComponentRow(null, downloadMol, false, 2);
		
		fp1pathways.layoutRows();
		fp2processentities.layoutRows();
		fp3selectge.layoutRows();
		fp4mapnodes.layoutRows();
		fp5kgmlinterpret.layoutRows();
		fp6molProcessing.layoutRows();
		
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			this.add(FolderPanel.getBorderedComponent(fp5kgmlinterpret, 4, 2, 4, 2));
		}
		this.add(FolderPanel.getBorderedComponent(fp1pathways, 4, 2, 4, 2));
		
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			this.add(FolderPanel.getBorderedComponent(fp4mapnodes, 4, 2, 4, 2));
		}
		this.add(FolderPanel.getBorderedComponent(fp2processentities, 4, 2, 4, 2));
		this.add(FolderPanel.getBorderedComponent(fp3selectge, 4, 2, 4, 2));
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR &&
				ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS))
			this.add(FolderPanel.getBorderedComponent(fp6molProcessing, 4, 2, 4, 2));
	}
	
	private void pretifyFolderPanel(FolderPanel fp, boolean enableSearch) {
		fp.setColumnStyle(TableLayout.FILL, TableLayout.PREFERRED);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 5, 5);
		fp.setMaximumRowCount(4);
		fp.setRowBackground0(Color.WHITE);
		fp.setRowBackground1(new Color(240, 240, 240));
		fp.enableSearch(enableSearch);
		fp.layoutRows();
	}
	
	private void initButtonCommands(JButton mapOverview, JButton mapOverviewSOAP,
			JButton superPathway, JButton orgSpecificSuperPathway,
			JButton createAllOrgSpecificSuperPathways, JButton condenseItems,
			JButton connectItems, JButton selectDuplicateItems,
			JButton selEnzymes, JButton selCompounds,
			JButton selGenes, JButton selMapNodes,
			JButton selMapTitleNodes, JButton addAllElementsToMapNode, JButton addEnzymesToMapNode,
			JButton addCompoundsToMapNode,
			JButton addGenesToMapNode,
			JButton kgmlErrors,
			FolderPanel fpWarnings,
			FolderPanel fpErrors,
			FolderPanel fpEntries,
			FolderPanel fpRelations,
			FolderPanel fpReactions, JButton downloadMol,
			JButton specialCalculateHierachyEnzymeCoverage,
			JButton specialCalculateHierachyGeneCoverage,
			JButton kegg2sbgnPathway) {
		mapOverview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final KeggHelper kegg = new KeggHelper();
				Collection<OrganismEntry> orgs;
				try {
					orgs = kegg.getOrganisms();
					if (orgs == null)
						return;
					OrganismEntry[] organismSelections = TabKegg.getKEGGorganismFromUser(orgs);
					if (organismSelections == null)
						return;
					if (organismSelections.length < 1) {
						MainFrame.showMessageDialog(
								"No organism has been selected. Operation aborted.",
								"Information");
						return;
					}
					if (organismSelections.length > 1) {
						MainFrame.showMessageDialog(
								"More than one organism has been selected, processing the first: " + organismSelections[0].toString(),
								"Information");
					}
					final OrganismEntry org = organismSelections[0];
					final BackgroundTaskStatusProviderSupportingExternalCallImpl statusProvider =
							new BackgroundTaskStatusProviderSupportingExternalCallImpl("Construct Map Overview",
									"Load pathways to determine connectivity...");
					
					BackgroundTaskHelper.issueSimpleTask(
							"Construct Map Overview", "Load Pathways to determine connectivity...",
							new Runnable() {
								@Override
								public void run() {
									Collection<Graph> gc = null;
									try {
										gc = GraphHelperBio.getKeggPathways(kegg, org, true, false, true, statusProvider);
									} catch (Exception e1) {
										ErrorMsg.addErrorMessage(e1);
									}
									if (gc != null && gc.size() == 1)
										MainFrame.getInstance().showGraph(gc.iterator().next(), null);
									else {
										MainFrame.showMessageDialog("KEGG Pathway Overview could not be constructed!", "Error");
									}
								}
							}, null, statusProvider);
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			}
		});
		
		mapOverviewSOAP.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final KeggHelper kegg = new KeggHelper();
				Collection<OrganismEntry> orgs;
				try {
					orgs = kegg.getOrganisms();
					if (orgs == null)
						return;
					final OrganismEntry[] organismSelections = TabKegg.getKEGGorganismFromUser(orgs);
					if (organismSelections == null)
						return;
					if (organismSelections.length < 1) {
						MainFrame.showMessageDialog(
								"No organism has been selected. Operation aborted.",
								"Information");
						return;
					}
					if (organismSelections.length > 1) {
						MainFrame.showMessageDialog(
								"More than one organism has been selected, processing the first: " + organismSelections[0].toString(),
								"Information");
					}
					
					final OrganismEntry org = organismSelections[0];
					
					if (org.getShortName().equalsIgnoreCase("ko")) {
						int res = JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>" +
								"Processing of KO based pathway overview may not work correctly.<br>" +
								"At the moment the reason for that is not clear, you could use the<br>" +
								"&quot;normal&quot; reference maps, instead.<br>" +
								"Do you want to proceed, anyways?", "Organism Selection", JOptionPane.YES_NO_OPTION);
						if (res == JOptionPane.NO_OPTION)
							return;
					}
					final BackgroundTaskStatusProviderSupportingExternalCallImpl statusProvider =
							new BackgroundTaskStatusProviderSupportingExternalCallImpl("Construct Map Overview",
									"Use SOAP-calls to determine connectivity...");
					BackgroundTaskHelper.issueSimpleTask(
							"Construct Map Overview", "Use SOAP-calls to determine connectivity...",
							new Runnable() {
								@Override
								public void run() {
									Collection<Graph> gc = null;
									try {
										gc = GraphHelperBio.getKeggPathways(kegg, org, true, false, false, statusProvider);
									} catch (Exception e1) {
										ErrorMsg.addErrorMessage(e1);
									}
									if (gc != null && gc.size() == 1) {
										final Graph g = gc.iterator().next();
										BackgroundTaskHelper.executeLaterOnSwingTask(100, new Runnable() {
											
											@Override
											public void run() {
												MainFrame.getInstance().showGraph(g, e);
											}
										});
									} else {
										MainFrame.showMessageDialog("KEGG Pathway Overview could not be constructed!", "Error");
									}
								}
							}, null, statusProvider);
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			}
		});
		
		kegg2sbgnPathway.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GravistoService.getInstance().runAlgorithm(new CreateSBGNgraphFromKEGGalgorithm(), arg0);
			}
		});
		
		superPathway.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final KeggHelper kegg = new KeggHelper();
				Collection<OrganismEntry> orgs;
				try {
					orgs = kegg.getOrganisms();
					if (orgs == null)
						return;
					final OrganismEntry[] organismSelections = TabKegg.getKEGGorganismFromUser(orgs);
					if (organismSelections == null)
						return;
					if (organismSelections.length < 1) {
						MainFrame.showMessageDialog(
								"No organism has been selected. Operation aborted.",
								"Information");
						return;
					}
					if (organismSelections.length > 1) {
						MainFrame.showMessageDialog(
								"More than one organism has been selected, processing the first: " + organismSelections[0].toString(),
								"Information");
					}
					final OrganismEntry org = organismSelections[0];
					final BackgroundTaskStatusProviderSupportingExternalCallImpl statusProvider =
							new BackgroundTaskStatusProviderSupportingExternalCallImpl("Create Super-Pathway",
									"Loading and merging KEGG pathways...");
					BackgroundTaskHelper.issueSimpleTask(
							"Create Super-Pathway",
							"Loading and merging KEGG pathways...",
							new Runnable() {
								@Override
								public void run() {
									Collection<Graph> gc = null;
									try {
										gc = GraphHelperBio.getKeggPathways(kegg, org, false, true, true, statusProvider);
									} catch (Exception e1) {
										ErrorMsg.addErrorMessage(e1);
									}
									if (gc != null && gc.size() == 1)
										MainFrame.getInstance().showGraph(gc.iterator().next(), null);
									else {
										MainFrame.showMessageDialog("KEGG Pathway Overview could not be constructed!", "Error");
									}
								}
							}, null, statusProvider);
					
				} catch (Exception e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			}
		});
		
		orgSpecificSuperPathway.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new ColorizeSuperGraphAlgorithm(), e);
			}
		});
		createAllOrgSpecificSuperPathways.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new CreateOrgSpecificSuperGraphsAlgorithm(), e);
			}
		});
		
		condenseItems.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
					Object[] res = MyInputHelper.getInput(
							"<html>" +
									"Please specify if nodes with different cluster IDs should be<br>" +
									"processed idependently:<br><br>" +
									"<small>If the consider node positions option is enabled,<br>" +
									"only nodes with nearly the same position (x/y) will be merged.<br>",
							"Consider Cluster Information?",
							new Object[] {
									"Consider Cluster IDs", new Boolean(false),
									"Consider Node Positions", new Boolean(false),
									"Retain different Cluster IDs", new Boolean(false)
							});
					if (res == null)
						return;
					final Boolean considerCluster = (Boolean) res[0];
					final Boolean considerPosition = (Boolean) res[1];
					final Boolean retainClusterIDs = (Boolean) res[2];
					BackgroundTaskHelper.issueSimpleTask(
							"Identify nodes with same label",
							"Merge multiple occurring nodes...",
							new Runnable() {
								@Override
								public void run() {
									List<Node> nodes = null;
									Graph ggg = MainFrame.getInstance().getActiveEditorSession().getGraph();
									try {
										nodes = GraphHelper.getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
									} catch (NullPointerException npe) {
										MainFrame.showMessageDialog("No active graph editor window found!", "Error");
									}
									if (nodes != null) {
										GraphHelperBio.mergeNodesWithSameLabel(nodes, false, false, considerCluster, considerPosition, retainClusterIDs);
										if (nodes.size() > 0)
											MergeNodes.convertIDs(ggg);
									}
								}
							}, null);
				} else {
					kgmlEdDuplicateIDsMerging(true);
				}
			}
		});
		
		selectDuplicateItems.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
					List<Node> nodes = null;
					boolean extendSelection = false;
					boolean considerClusterID = false;
					boolean considerNodePositions = false;
					try {
						nodes = GraphHelper.getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
						if (nodes.size() < MainFrame.getInstance().getActiveEditorSession().getGraph().getNumberOfNodes()) {
							Object[] res = MyInputHelper.getInput("<html>" +
									"Based on the current node selection<br>" +
									"this command may either work strictly on the current selection<br>" +
									"by reducing the selection to nodes with the same node label<br>" +
									"(do not extend selection), or by processing all nodes of the graph<br>" +
									"and locating nodes which the same label, based on the current node<br>" +
									"selection (extend selection option).<br>" +
									"<br>" +
									"Example 1 (Extend Selection? Yes): You could select two nodes<br>" +
									"with the labels &quot;ATP&quot; and &quot;CO2&quot;. Then using<br>" +
									"this command all nodes in the current graph with any such label<br>" +
									"would be selected, afterwards. If &quot;CO2&quot; could not be found<br>" +
									"more than one time, it would not be included in the selection.<br>" +
									"<br>" +
									"Example 2 (Extend Selection? No): You could select a larger part of<br>" +
									"the current network and then use this command to locate any nodes<br>" +
									"within the current selection, which occurs several times.<br>" +
									"You could for example first locate and select all compounds in the<br>" +
									"graph and then use this command to locate and reduce the selection<br>" +
									"to all compounds which occur more than once.",
									"Locate nodes with the same label",
									"Extend selection?", extendSelection,
									"Consider Cluster IDs?", considerClusterID,
									"Consider approximate node positions?", considerNodePositions);
							if (res == null)
								return;
							extendSelection = (Boolean) res[0];
							considerClusterID = (Boolean) res[1];
							considerNodePositions = (Boolean) res[2];
						} else {
							Object[] res = MyInputHelper.getInput("<html>" +
									"",
									"Locate nodes with the same label",
									"Consider Cluster IDs?", considerClusterID,
									"Consider approximate node positions?", considerNodePositions);
							if (res == null)
								return;
							considerClusterID = (Boolean) res[0];
							considerNodePositions = (Boolean) res[1];
						}
					} catch (NullPointerException npe) {
						MainFrame.showMessageDialog("No active graph editor window found!", "Error");
					}
					final List<Node> fNodes = nodes;
					final boolean fExtendSelection = extendSelection;
					final boolean fConsiderClusterID = considerClusterID;
					final boolean fConsiderNodePositions = considerNodePositions;
					if (nodes != null)
						BackgroundTaskHelper.issueSimpleTask(
								"Identify nodes with same label",
								"Select nodes with same label...",
								new Runnable() {
									@Override
									public void run() {
										GraphHelperBio
												.mergeNodesWithSameLabel(fNodes, true, fExtendSelection, fConsiderClusterID, fConsiderNodePositions,
														false);
									}
								}, null);
				} else
					kgmlEdDuplicateIDsMerging(false);
			}
		});
		
		connectItems.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BackgroundTaskHelper.issueSimpleTask(
						"Identify nodes with same label",
						"Connect multiple identical nodes...",
						new Runnable() {
							@Override
							public void run() {
								List<Node> nodes = null;
								try {
									nodes = GraphHelper.getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
								} catch (NullPointerException npe) {
									MainFrame.showMessageDialog("No active graph editor window found!", "Error");
								}
								if (nodes != null)
									GraphHelperBio.connectNodesWithSameLabel(nodes);
							}
						}, null);
			}
		});
		
		selEnzymes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new SelectEnzymesAlgorithm(), e);
			}
		});
		selCompounds.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new SelectCompoundsAlgorithm(), e);
			}
		});
		selGenes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new SelectGenesAlgorithm(), e);
			}
		});
		selMapNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new SelectMapNodesAlgorithm(), e);
			}
		});
		selMapTitleNodes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new SelectMapTitleNodesAlgorithm(), e);
			}
		});
		
		addAllElementsToMapNode.addActionListener(getMapNodeProcessingActionListener(true, true, true));
		addEnzymesToMapNode.addActionListener(getMapNodeProcessingActionListener(true, false, false));
		addCompoundsToMapNode.addActionListener(getMapNodeProcessingActionListener(false, true, false));
		addGenesToMapNode.addActionListener(getMapNodeProcessingActionListener(false, false, true));
		
		specialCalculateHierachyEnzymeCoverage.addActionListener(HierarchyProcessing.getSpecialCommandHierarchyCoverageListener());
		// specialCalculateHierachyGeneCoverage.addActionListener(HierarchyProcessing.getSpecialCommandHierarchyGeneCoverageListener());
		
		kgmlErrors.addActionListener(getKGMLerrorsCmd(
				fpWarnings, fpErrors, fpEntries, fpRelations, fpReactions));
		kgmlErrors.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (mcUpdateModel != null)
					mcUpdateModel.setMark(false);
			}
		});
		
		downloadMol.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
				BackgroundTaskHelper.issueSimpleTask(
						"Download Compound Images from KEGG",
						"Assign Image URL Attribute...",
						new Runnable() {
							@Override
							public void run() {
								status.setCurrentStatusText1("Download Compound Images from KEGG");
								status.setCurrentStatusText2("");
								List<Node> nodes = null;
								try {
									nodes = GraphHelper.getSelectedOrAllNodes(MainFrame.getInstance().getActiveEditorSession());
								} catch (NullPointerException npe) {
									MainFrame.showMessageDialog("No active graph editor window found!", "Error");
									return;
								}
								int i = 0;
								for (Node n : nodes) {
									String keggId = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", "", "");
									if (keggId.startsWith("cpd:")) {
										String comp = keggId.substring("cpd:".length());
										status.setCurrentStatusText2("Compound ID present: " + comp);
										AttributeHelper.setAttribute(n, "image", "image_url", comp);
										AttributeHelper.setAttribute(n, "image", "image_position",
												GraphicAttributeConstants.AUTO_OUTSIDE);
										CompoundEntry ce = CompoundService.getInformation(comp);
										if (ce != null && ce.isValid()) {
											DatabaseBasedLabelReplacementService.setCompoundAnnotation(n, ce);
										}
									} else {
										String lbl = AttributeHelper.getLabel(n, "");
										if (lbl.length() > 0) {
											CompoundEntry ce = CompoundService.getInformation(lbl);
											if (ce != null && ce.isValid()) {
												status.setCurrentStatusText2("Compound recognized: " + ce.getID());
												AttributeHelper.setAttribute(n, "image", "image_url", ce.getID());
												AttributeHelper.setAttribute(n, "image", "image_position",
														GraphicAttributeConstants.AUTO_OUTSIDE);
												DatabaseBasedLabelReplacementService.setCompoundAnnotation(n, ce);
											}
										}
									}
									i++;
									status.setCurrentStatusValueFine(100d * i / nodes.size());
								}
								status.setCurrentStatusText2("Processing Finished");
							}
						}, null, status);
			}
		});
	}
	
	public static ActionListener getKGMLerrorsCmd(
			final FolderPanel fpWarnings, final FolderPanel fpErrors,
			final FolderPanel fpEntries, final FolderPanel fpRelations,
			final FolderPanel fpReactions) {
		ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				EditorSession es = MainFrame.getInstance().getActiveEditorSession();
				if (es == null) {
					MainFrame.showMessageDialog("No active editor session!", "Error");
					return;
				}
				Graph graph = es.getGraph();
				if (graph == null) {
					MainFrame.showMessageDialog("Editor session contains no valid graph (null)!", "Error");
					return;
				}
				Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
				Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
				HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
				Pathway p = Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
				KGMLerrorWindow.updatePanels(fpWarnings, fpErrors, warnings, errors);
				int i = 1;
				fpEntries.clearGuiComponentList();
				for (Entry e : p.getEntries()) {
					EntryLabel el = new EntryLabel(e, entry2graphNode, p, warnings, errors, graph);
					fpEntries.addGuiComponentRow(
							el,
							new JLabel(i + ""), false);
					i++;
				}
				if (p.getEntries() != null && p.getEntries().size() > 0)
					fpEntries.setTitle("Entries (" + p.getEntries().size() + ")");
				else
					fpEntries.setTitle("No Entries");
				fpEntries.layoutRows();
				
				i = 1;
				fpRelations.clearGuiComponentList();
				for (Relation r : p.getRelations()) {
					RelationLabel rl = new RelationLabel(r, entry2graphNode, p, warnings, errors, graph);
					fpRelations.addGuiComponentRow(rl, new JLabel(i + ""), false);
					i++;
				}
				if (p.getRelations() != null && p.getRelations().size() > 0)
					fpRelations.setTitle("Relations (" + p.getRelations().size() + ")");
				else
					fpRelations.setTitle("No Relations");
				fpRelations.layoutRows();
				
				i = 1;
				fpReactions.clearGuiComponentList();
				for (Reaction r : p.getReactions()) {
					ReactionLabel rl = new ReactionLabel(r, entry2graphNode, p, warnings, errors, graph);
					fpReactions.addGuiComponentRow(rl, new JLabel(i + ""), false);
					i++;
				}
				if (p.getReactions() != null && p.getReactions().size() > 0)
					fpReactions.setTitle("Reactions (" + p.getReactions().size() + ")");
				else
					fpReactions.setTitle("No Reactions");
				fpReactions.layoutRows();
			}
		};
		return result;
	}
	
	private ActionListener getMapNodeProcessingActionListener(
			final boolean enzymes, final boolean compounds, final boolean genes) {
		ActionListener result = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
						"Prepare Data Processing...", "");
				BackgroundTaskHelper.issueSimpleTask(
						"Map-Node Processing", "Initialize...",
						new Runnable() {
							@Override
							public void run() {
								if (compounds)
									status.setCurrentStatusText1("<html>Add and connect compounds and enzymes of maps to map nodes...");
								else
									if (enzymes)
										status.setCurrentStatusText1("<html>Add and connect enzymes of maps to map nodes...");
									else
										if (compounds)
											status.setCurrentStatusText1("<html>Add and connect compounds of maps to map nodes...");
										else
											if (compounds)
												status.setCurrentStatusText1("<html>Add and connect genes of maps to map nodes...");
								try {
									EditorSession workSession = MainFrame.getInstance().getActiveEditorSession();
									if (workSession == null) {
										MainFrame.showMessageDialog("No active graph editor window found!", "Error");
										return;
									}
									
									Graph g = workSession.getGraph();
									Collection<Node> allNewNodes = new ArrayList<Node>();
									Collection<Node> workNodes = new ArrayList<Node>(GraphHelper.getSelectedOrAllNodes(workSession));
									try {
										g.getListenerManager().transactionStarted(this);
										status.setCurrentStatusValue(0);
										double toDo = workNodes.size();
										double current = 0;
										for (Node n : workNodes) {
											System.out.println("Check node " + AttributeHelper.getLabel(n, ""));
											ArrayList<IndexAndString> ids = KeggGmlHelper.getKeggIds(n);
											String keggID = KeggGmlHelper.getKeggId(n);
											if (keggID != null && keggID.length() > 0)
												ids.add(new IndexAndString(-1, keggID));
											String referencedMap = null;
											for (IndexAndString ias : ids) {
												System.out.println(ias);
												if (ias.getValue() != null && ias.getValue().length() > 0) {
													if (referencedMap == null || referencedMap.indexOf("ko") >= 0)
														referencedMap = ias.getValue();
												}
											}
											if (referencedMap == null || referencedMap.length() <= 0)
												continue;
											if (!referencedMap.startsWith("path:"))
												continue;
											// referencedMap = referencedMap.substring("path:".length());
											status.setCurrentStatusText2("Evaluate elements for map " + referencedMap + " (" + allNewNodes.size()
													+ " added so far)");
											
											String[] compOrEnz1 = null;
											String[] compOrEnz2 = null;
											String[] compOrEnz3 = null;
											if (enzymes)
												compOrEnz1 = KeggHelper.getKeggEnzymesOfMap(referencedMap);
											if (compounds)
												compOrEnz2 = KeggHelper.getKeggCompoundsOfMap(referencedMap);
											if (genes)
												compOrEnz3 = KeggHelper.getKeggGenesOfMap(referencedMap);
											ArrayList<Node> newNodes = new ArrayList<Node>();
											if (compOrEnz1 != null)
												for (String element : compOrEnz1) {
													Node nn = addNewEnzymeNode(g, element, n);
													if (nn != null)
														newNodes.add(nn);
												}
											if (compOrEnz2 != null)
												for (String element : compOrEnz2) {
													Node nn = addNewCompoundNode(g, element, n);
													if (nn != null)
														newNodes.add(nn);
												}
											if (compOrEnz3 != null)
												for (String element : compOrEnz3) {
													Node nn = addNewGeneNode(g, element, n);
													if (nn != null)
														newNodes.add(nn);
												}
											// referencedMap = referencedMap.replaceFirst("path:", "");
											for (Node nn : newNodes) {
												NodeHelper nnh = new NodeHelper(nn);
												nnh.setClusterID(referencedMap);
												
											}
											processNewNodeLayout(newNodes, n);
											allNewNodes.addAll(newNodes);
											current += 1;
											status.setCurrentStatusValueFine(100d * current / toDo);
											if (status.wantsToStop())
												break;
										}
										if (status.wantsToStop()) {
											status.setCurrentStatusText2("Processing aborted");
										}
										workSession.getSelectionModel().getActiveSelection().addAll(allNewNodes);
										workSession.getSelectionModel().selectionChanged();
									} finally {
										g.getListenerManager().transactionFinished(this, false, null);
									}
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
							
							private Node addNewCompoundNode(Graph g, String element, Node refN) {
								Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(10, 10));
								KeggGmlHelper.setKeggGraphicsType(n, kgmlGraphicsType.circle);
								KeggGmlHelper.setKeggId(n, element);
								if (element != null && element.startsWith("cpd:"))
									element = element.substring("cpd:".length());
								KeggGmlHelper.setKeggGraphicsTitle(n, element);
								KeggGmlHelper.setKeggType(n, "compound");
								NodeHelper nh = new NodeHelper(n);
								nh.setLabel(element);
								nh.setSize(8, 8);
								nh.setBorderWidth(1);
								LabelAttribute la = AttributeHelper.getLabel(-1, n);
								if (la != null) {
									la.setFontSize(10);
								}
								g.addEdge(refN, n, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.GRAY, Color.GRAY, true));
								return n;
							}
							
							private Node addNewGeneNode(Graph g, String element, Node refN) {
								Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(10, 10));
								KeggGmlHelper.setKeggGraphicsType(n, kgmlGraphicsType.circle);
								KeggGmlHelper.setKeggId(n, element);
								// if (element!=null && element.startsWith("cpd:"))
								// element = element.substring("cpd:".length());
								KeggGmlHelper.setKeggGraphicsTitle(n, element);
								KeggGmlHelper.setKeggType(n, "gene");
								NodeHelper nh = new NodeHelper(n);
								nh.setLabel(element);
								nh.setSize(8, 8);
								nh.setBorderWidth(1);
								LabelAttribute la = AttributeHelper.getLabel(-1, n);
								if (la != null) {
									la.setFontSize(10);
								}
								g.addEdge(refN, n, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.GRAY, Color.GRAY, true));
								return n;
							}
							
							private Node addNewEnzymeNode(Graph g, String element, Node refN) {
								Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(10, 10));
								KeggGmlHelper.setKeggGraphicsType(n, kgmlGraphicsType.rectangle);
								KeggGmlHelper.setKeggId(n, element);
								if (element != null && element.startsWith("ec:"))
									element = element.substring("ec:".length());
								KeggGmlHelper.setKeggGraphicsTitle(n, element);
								KeggGmlHelper.setKeggType(n, "enzyme");
								NodeHelper nh = new NodeHelper(n);
								nh.setLabel(element);
								nh.setSize(45, 17);
								nh.setBorderWidth(1);
								LabelAttribute la = AttributeHelper.getLabel(-1, n);
								if (la != null) {
									la.setFontSize(10);
								}
								g.addEdge(refN, n, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(Color.GRAY, Color.GRAY, true));
								return n;
							}
							
							private void processNewNodeLayout(ArrayList<Node> newNodes, Node centerN) {
								// place new nodes around n (circle layout)
								if (newNodes == null || newNodes.size() <= 0)
									return;
								NodeHelper cn = new NodeHelper(centerN);
								double xc = cn.getX();
								double yc = cn.getY();
								int numberOfNodes = newNodes.size();
								double singleStep = 2 * Math.PI / numberOfNodes;
								int i = 0;
								double defaultRadius = 50 * numberOfNodes / 2 / Math.PI;
								if (defaultRadius < 50)
									defaultRadius = 50;
								Vector2d ctr = new Vector2d(xc, yc);
								for (Node n : newNodes) {
									double newX = Math.sin(singleStep * i) * defaultRadius + ctr.x;
									double newY = Math.cos(singleStep * i) * defaultRadius + ctr.y;
									CoordinateAttribute ca = (CoordinateAttribute) n
											.getAttribute(GraphicAttributeConstants.COORD_PATH);
									ca.setX(newX);
									ca.setY(newY);
									i = i + 1;
								}
							}
						},
						null, status);
			}
		};
		return result;
	}
	
	public static void loadPathway(KeggPathwayEntry myEntry, boolean processLabels) {
		KeggService.loadPathway(myEntry, processLabels);
	}
	
	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabKegg2() {
		super();
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			this.title = "Process";
		else
			this.title = "Node Processing";
		
		if (!SystemAnalysis.isHeadless())
			initComponents();
	}
	
	@Override
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	@Override
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	@Override
	public void postAttributeRemoved(AttributeEvent e) {
		if (mcUpdateModel != null)
			mcUpdateModel.setMark(true);
	}
	
	@Override
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	@Override
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	@Override
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	@Override
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		if (mcUpdateModel != null)
			mcUpdateModel.setMark(true);
	}
	
	@Override
	public void transactionStarted(TransactionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	@Override
	public void sessionChanged(Session s) {
		if (mcUpdateModel != null)
			mcUpdateModel.setMark(true);
		try {
			if (s != null)
				s.getGraph().getListenerManager().addDelayedAttributeListener(this);
		} catch (ListenerRegistrationException lre) {
			// empty
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	@Override
	public void sessionDataChanged(Session s) {
		if (mcUpdateModel != null)
			mcUpdateModel.setMark(true);
	}
	
	private void kgmlEdDuplicateIDsMerging(boolean trueIsMergeFalsIsSelect) {
		ArrayList<EntryType> etl = new ArrayList<EntryType>();
		for (EntryType et : EntryType.values()) {
			etl.add(et);
		}
		Object[] res;
		if (trueIsMergeFalsIsSelect)
			res = MyInputHelper.getInput(
					"You may limit the scope for the entry merge-operations,<br>" +
							"which removes duplicate entries, based on their ID.",
					"Merge duplicate entries",
					new Object[] {
							"Limit Scope to Node-Selection?", new Boolean(false),
							"Limit Scope to Selected Entry-Type:", new Boolean(false),
							"Entry-Type", etl
					});
		else
			res = new Object[] { new Boolean(true), new Boolean(false), EntryType.compound };
		
		if (res != null) {
			Boolean limitFromNodes = (Boolean) res[0];
			Boolean limitScope = (Boolean) res[1];
			EntryType et = (EntryType) res[2];
			if (!limitScope)
				et = null;
			Graph graph = null;
			Selection selection = null;
			try {
				graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
				selection = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
			} catch (Exception err) {
				if (err != null)
					MainFrame.showMessageDialog("No active graph window found!", "Error");
			}
			if (graph != null && selection != null) {
				try {
					graph.getListenerManager().transactionStarted(graph);
					Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
					Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
					HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
					Pathway p = Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
					ArrayList<Entry> selectedEntries = new ArrayList<Entry>();
					if (!limitFromNodes || selection == null || (selection != null && selection.getNodes().size() <= 0)) {
						selectedEntries.addAll(entry2graphNode.keySet());
					} else {
						Collection<Node> selN = selection.getNodes();
						for (Entry entr : entry2graphNode.keySet()) {
							if (selN.contains(entry2graphNode.get(entr)))
								selectedEntries.add(entr);
						}
					}
					if (trueIsMergeFalsIsSelect)
						p.mergeMultipleEntriesOfType(et, selectedEntries);
					else {
						Collection<Entry> duplicteEntries = p.findMultipleEntries(selectedEntries);
						selection.clear();
						HashSet<Node> ns = new HashSet<Node>();
						for (Entry se : duplicteEntries) {
							Node n = entry2graphNode.get(se);
							if (n != null)
								ns.add(n);
						}
						SelectionModel sm = MainFrame.getInstance().getActiveEditorSession().getSelectionModel();
						sm.setActiveSelection(new Selection("Duplicate Entries", ns));
						sm.selectionChanged();
						MainFrame.showMessage(ns.size() + " nodes added to selection", MessageType.INFO);
					}
					if (trueIsMergeFalsIsSelect) {
						graph.clear();
						p.getGraph(graph);
					}
				} finally {
					graph.getListenerManager().transactionFinished(graph);
				}
			}
		}
	}
	
	@Override
	public boolean visibleForView(View v) {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return v != null && v instanceof GraphView;
		else
			return true;
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
