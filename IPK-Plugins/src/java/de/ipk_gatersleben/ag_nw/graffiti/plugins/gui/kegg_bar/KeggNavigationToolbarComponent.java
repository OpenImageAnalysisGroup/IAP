/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import info.clearthought.layout.SingleFiledLayout;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.xml.rpc.ServiceException;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelperBio;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.url_attribute.LoadPathwayAttributeAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggPathwayEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.KeggService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.OrganismEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg.TabKegg;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

/**
 * DOCUMENT ME!
 */
public class KeggNavigationToolbarComponent extends JToolBar implements
		GraffitiComponent, ActionListener, ViewListener, SessionListener,
		SelectionListener {
	
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	private JButton jbLoadKegg, jbLoadPathway, jbCollapsePathway;
	
	/** active session */
	private static Session activeSession;
	
	private String prefComp;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for ZoomChangeComponent.
	 * 
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	public KeggNavigationToolbarComponent(String prefComp) {
		super("KEGG");
		this.prefComp = prefComp;
		// myContent.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.SINGLE);
		setLayout(new SingleFiledLayout(SingleFiledLayout.ROW,
				SingleFiledLayout.FULL, 0));// double border = 0;
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		
		ImageIcon iconLoadKegg = new ImageIcon(cl.getResource(path
				+ "/images/load_kegg.gif"));
		ImageIcon iconLoadPathway = new ImageIcon(cl.getResource(path
				+ "/images/load_pathway.gif"));
		ImageIcon iconCollapsePathway = new ImageIcon(cl.getResource(path
				+ "/images/collapse_pathway.gif"));
		int s = 0;
		
		jbLoadKegg = new JButton(iconLoadKegg);
		jbLoadKegg.setMargin(new Insets(s, s, s, s));
		jbLoadKegg.addActionListener(this);
		jbLoadKegg.setToolTipText("Create KEGG Pathay-Overview");
		add(jbLoadKegg);
		
		jbLoadPathway = new JButton(iconLoadPathway);
		jbLoadPathway.setMargin(new Insets(s, s, s, s));
		jbLoadPathway.addActionListener(this);
		jbLoadPathway.setToolTipText("Load selected Pathway(s) into view");
		add(jbLoadPathway);
		
		jbCollapsePathway = new JButton(iconCollapsePathway);
		jbCollapsePathway.setMargin(new Insets(s, s, s, s));
		jbCollapsePathway.addActionListener(this);
		jbCollapsePathway.setToolTipText("Collapse selected Pathway(s)");
		add(jbCollapsePathway);
		
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		
		validate();
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(jbLoadKegg)) {
			KeggHelper kegg = new KeggHelper();
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
				OrganismEntry org = organismSelections[0];
				Collection<Graph> gc = getKeggPathways(kegg, org, true, null);
				if (gc != null && gc.size() == 1)
					MainFrame.getInstance().showGraph(gc.iterator().next(), e);
				else {
					MainFrame.showMessageDialog("KEGG Super-Pathway could not be constructed!", "Error");
				}
			} catch (Exception e1) {
				ErrorMsg.addErrorMessage(e1);
			}
			
			// Graph g = KeggHelper.loadKeggOverview(result[0]);
		}
		if (e.getSource().equals(jbLoadPathway)) {
			EditorSession es = MainFrame.getInstance().getActiveEditorSession();
			if (es != null) {
				List<Node> nl = GraphHelper.getSelectedOrAllNodes(es);
				for (Node n : nl) {
					String kegg_type = (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, "", false);
					String kegg_map_link = KeggGmlHelper.getKeggId(n);
					if (kegg_map_link != null && kegg_map_link.startsWith("path:")) {
						kegg_map_link = kegg_map_link.substring("path:".length());
						if (kegg_type != null &&
								(kegg_type.equalsIgnoreCase("map") || kegg_type.equalsIgnoreCase("ko"))) {
							if (kegg_map_link != null && kegg_map_link.length() > 0) {
								LoadPathwayAttributeAction.loadMap(kegg_map_link, n.getGraph(), n, false);
							}
						}
					}
				}
			}
		}
		if (e.getSource().equals(jbCollapsePathway)) {
			Graph graph = activeSession.getGraph();
			EditorSession session = GravistoService.getInstance()
					.getMainFrame().getActiveEditorSession();
			Selection selection = session.getSelectionModel()
					.getActiveSelection();
			if (selection == null || selection.getNodes().size() <= 0) {
				graph.getNodes();
			} else {
				selection.getNodes();
			}
		}
	}
	
	/**
	 * @param kegg
	 * @param org
	 * @param returnSuperPathway
	 *           If set to true, only one pathway, which represents all KEGG Pathways is returned,
	 *           otherwise, all pathway graphs are returned.
	 * @param status
	 * @return Kegg pathway(s)
	 * @throws IOException
	 * @throws ServiceException
	 */
	private Collection<Graph> getKeggPathways(KeggHelper kegg, OrganismEntry org, boolean returnSuperPathway,
			BackgroundTaskStatusProviderSupportingExternalCall status) throws Exception {
		List<Graph> result = new ArrayList<Graph>();
		Collection<KeggPathwayEntry> pathways = kegg.getXMLpathways(org, false, status);
		
		Graph superGraph = new AdjListGraph();
		HashMap<String, Node> mapNumber2superGraphNode = new HashMap<String, Node>();
		
		superGraph.setName("KEGG Pathway Overview");
		for (KeggPathwayEntry kpe : pathways) {
			Graph keggPathwayGraph = new AdjListGraph();
			KeggService.loadKeggPathwayIntoEditor(kpe, keggPathwayGraph, KeggService.getDefaultEnzymeColor(), false, false);
			// System.out.println("Pathway created: "+kpe);
			if (returnSuperPathway) {
				if (!mapNumber2superGraphNode.containsKey(kpe.getMapName())) {
					Node mapNode = GraphHelperBio.addMapNode(superGraph, kpe);
					Vector2d minXY = NodeTools.getMinimumXY(keggPathwayGraph.getNodes(), 1d, 0, 0, true);
					Vector2d maxXY = NodeTools.getMaximumXY(keggPathwayGraph.getNodes(), 1d, 0, 0, true);
					double width = maxXY.x - minXY.x;
					double height = maxXY.y - minXY.y;
					AttributeHelper.setSize(mapNode, width, height);
					mapNumber2superGraphNode.put(kpe.getMapName(), mapNode);
				}
				Node thisPathwayNode = mapNumber2superGraphNode.get(kpe.getMapName());
				Collection<Node> mapNodes = GraphHelperBio.getMapLinkNodes(keggPathwayGraph);
				for (Node referencedMapNode : mapNodes) {
					String referencedMap = (String) AttributeHelper.getAttributeValue(referencedMapNode, "kegg", "kegg_map_link", null, "", false);
					if (referencedMap != null && referencedMap.length() > 0) {
						referencedMap = StringManipulationTools.stringReplace(referencedMap, "path:", "");
						if (mapNumber2superGraphNode.containsKey(referencedMap)) {
							Node superPathwayMapNode = mapNumber2superGraphNode.get(referencedMap);
							if (!thisPathwayNode.getNeighbors().contains(superPathwayMapNode) && (!kpe.getMapName().equalsIgnoreCase(referencedMap))) {
								Edge edge = superGraph.addEdge(thisPathwayNode, superPathwayMapNode, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(
										Color.BLACK, Color.BLACK, false));
								AttributeHelper.setDashInfo(edge, 5f, 5f);
								// System.out.println("Connect Map Nodes: "+kpe.getMapName()+" <==> "+referencedMap);
							}
						}
					}
				}
			}
			if (!returnSuperPathway)
				result.add(keggPathwayGraph);
		}
		superGraph.numberGraphElements();
		if (returnSuperPathway) {
			result.clear();
			result.add(superGraph);
		}
		return result;
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		activeSession = s;
		
		if (s != null) {
			viewChanged(s.getActiveView());
		}
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		activeSession = s;
		viewChanged(s.getActiveView());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.ViewListener#viewChanged(org.graffiti.plugin.view.View)
	 */
	public void viewChanged(View newView) {
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.selection.SelectionListener#selectionChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return prefComp;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
