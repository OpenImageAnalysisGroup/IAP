package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.FolderPanel;
import org.JMButton;
import org.Vector2d;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.View2D;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

public class TabSBGN
					extends InspectorTab
					implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private JTextField unitOfInformation1, unitOfInformation2, stateVariable1, stateVariable2;
	private String info1, info2, state1, state2;
	
	private JCheckBox cloneMarker;
	private boolean clone;
	private JTextField cloneMarkerTextEditor;
	private String cloneMarkerText;
	
	private JTextField entityPoolNodeLabelEditor;
	private String entityPoolNodeLabel;
	
	private JTextField consumptionEditor, productionEditor;
	private String consumption, production;
	private JCheckBox reversibleSelection;
	private boolean reversible;
	
	public TabSBGN() {
		ArrayList<JComponent> gui = new ArrayList<JComponent>();
		
		initSettingsEditors();
		
		FolderPanel fpSettings = new FolderPanel("<html>Auxiliary Units<small> (new node setting)", false, true, false, null);
		fpSettings.addGuiComponentRow(new JLabel("Label"), entityPoolNodeLabelEditor, false);
		fpSettings.addGuiComponentRow(new JLabel("Unit of information: "), unitOfInformation1, false);
		fpSettings.addGuiComponentRow(new JLabel("Unit of information: "), unitOfInformation2, false);
		fpSettings.addGuiComponentRow(new JLabel("State variable: "), stateVariable1, false);
		fpSettings.addGuiComponentRow(new JLabel("State variable: "), stateVariable2, false);
		fpSettings.addGuiComponentRow(new JLabel("Add clone marker: "), cloneMarker, false);
		fpSettings.addGuiComponentRow(new JLabel("Add clone marker: "), cloneMarkerTextEditor, false);
		fpSettings.mergeRowsWithSameLeftLabel();
		fpSettings.layoutRows();
		gui.add(fpSettings);
		
		FolderPanel fpContainerNodes = new FolderPanel("<html>Container Nodes<small> (add node/set style)", false, true, false, null);
		fpContainerNodes.addGuiComponentRow(new JLabel("Complex, Compartment, Submap"), getAddButton(SBGNitem.Complex), false);
		fpContainerNodes.addGuiComponentRow(new JLabel("Complex, Compartment, Submap"), getAddButton(SBGNitem.Compartment), false);
		fpContainerNodes.addGuiComponentRow(new JLabel("Complex, Compartment, Submap"), getAddButton(SBGNitem.Submap), false);
		fpContainerNodes.mergeRowsWithSameLeftLabel();
		fpContainerNodes.layoutRows();
		gui.add(fpContainerNodes);
		
		FolderPanel fpEntityNodes = new FolderPanel("<html>Entity Pool Nodes<small> (add node/set style)", false, true, false, null);
		fpEntityNodes.addGuiComponentRow(new JLabel("Unspecified entity"), getAddButton(SBGNitem.UnspecifiedEntityNode), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Simple chemical,Source/sink"), getAddButton(SBGNitem.SimpleChemical), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Simple chemical,Source/sink"), getAddButton(SBGNitem.SourceSink), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Macromolecule,Genetic entity"), getAddButton(SBGNitem.MacroMolecule), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Macromolecule,Genetic entity"), getAddButton(SBGNitem.GeneticEntity), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Multimer"), getAddButton(SBGNitem.MultimerMacrolecule), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Multimer"), getAddButton(SBGNitem.MultimerSimpleChemical), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Multimer"), getAddButton(SBGNitem.MultimerGeneticEntity), false);
		ArrayList<JComponent> tags = new ArrayList<JComponent>();
		tags.add(getAddButton(SBGNitem.TagRight));
		tags.add(getAddButton(SBGNitem.TagLeft));
		tags.add(getAddButton(SBGNitem.TagUp));
		tags.add(getAddButton(SBGNitem.TagDown));
		fpEntityNodes.addGuiComponentRow(new JLabel("Tag"), TableLayout.getMultiSplit(tags), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Observable,Pertubation"), getAddButton(SBGNitem.Observable), false);
		fpEntityNodes.addGuiComponentRow(new JLabel("Observable,Pertubation"), getAddButton(SBGNitem.Pertubation), false);
		fpEntityNodes.mergeRowsWithSameLeftLabel();
		fpEntityNodes.layoutRows();
		gui.add(fpEntityNodes);
		
		FolderPanel fpProcessNodes = new FolderPanel("<html>Process Nodes<small> (add node/set style)", false, true, false, null);
		fpProcessNodes.addGuiComponentRow(new JLabel("Transition"), getAddButton(SBGNitem.Transition), false);
		fpProcessNodes.addGuiComponentRow(new JLabel("Omitted,Uncertain process"), getAddButton(SBGNitem.Omitted), false);
		fpProcessNodes.addGuiComponentRow(new JLabel("Omitted,Uncertain process"), getAddButton(SBGNitem.Uncertain), false);
		fpProcessNodes.addGuiComponentRow(new JLabel("Association,Dissociation"), getAddButton(SBGNitem.Associaction), false);
		fpProcessNodes.addGuiComponentRow(new JLabel("Association,Dissociation"), getAddButton(SBGNitem.Dissociation), false);
		fpProcessNodes.mergeRowsWithSameLeftLabel();
		fpProcessNodes.layoutRows();
		gui.add(fpProcessNodes);
		
		FolderPanel fpLogicalNodes = new FolderPanel("<html>Logical Operators<small> (add node/set style)", false, true, false, null);
		fpLogicalNodes.addGuiComponentRow(new JLabel("AND,OR,NOT"), getAddButton(SBGNitem.AND), false);
		fpLogicalNodes.addGuiComponentRow(new JLabel("AND,OR,NOT"), getAddButton(SBGNitem.OR), false);
		fpLogicalNodes.addGuiComponentRow(new JLabel("AND,OR,NOT"), getAddButton(SBGNitem.NOT), false);
		fpLogicalNodes.mergeRowsWithSameLeftLabel();
		fpLogicalNodes.layoutRows();
		gui.add(fpLogicalNodes);
		
		FolderPanel fpConnectionsArcs = new FolderPanel("<html>Connecting Arcs<small> (set edge style)", false, true, false, null);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Consumption,Production"), consumptionEditor, false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Consumption,Production"), productionEditor, false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Consumption,Production"), reversibleSelection, false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Consumption/Production"), getEdgeButton(SBGNarc.ConsumptionProductionHorOrVert), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Consumption/Production"), TableLayout.getSplit(getEdgeButton(SBGNarc.ConsumptionProductionHor),
							getEdgeButton(SBGNarc.ConsumptionProductionVert), TableLayout.FILL, TableLayout.FILL), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Modulation, Stimulation"), getEdgeButton(SBGNarc.Modulation), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Modulation, Stimulation"), getEdgeButton(SBGNarc.Stimulation), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Catalysis, Inhibition, Trigger"), getEdgeButton(SBGNarc.Catalysis), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Catalysis, Inhibition, Trigger"), getEdgeButton(SBGNarc.Inhibition), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Catalysis, Inhibition, Trigger"), getEdgeButton(SBGNarc.Trigger), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Logic/Equivalence Arc"), getEdgeButton(SBGNarc.Logic), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("Logic/Equivalence Arc"), getEdgeButton(SBGNarc.Equivalence), false);
		fpConnectionsArcs.addGuiComponentRow(new JLabel("<html>" +
							"Redraw (click in case<br>" +
							"view is not auto-updated)"), getRedrawButton(), false);
		fpConnectionsArcs.mergeRowsWithSameLeftLabel();
		fpConnectionsArcs.layoutRows();
		gui.add(fpConnectionsArcs);
		
		setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		
		JComponent jc = TableLayout.getMultiSplitVertical(gui);
		jc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		add(jc, "0,0");
	}
	
	private JComponent getRedrawButton() {
		JButton res = new JMButton("Update Graph View");
		
		res.setOpaque(false);
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditorSession es = MainFrame.getInstance().getActiveEditorSession();
				if (es == null) {
					MainFrame.showMessageDialog("<html>" +
										"No active editor session!<br>" +
										"Please open a graph file or create a new one.", "Error");
					return;
				}
				Graph g = es.getGraph();
				GraphHelper.issueCompleteRedrawForGraph(g);
			}
		});
		return res;
	}
	
	private JComponent getEdgeButton(final SBGNarc item) {
		JButton res = new JButton(getEdgeStyleIcon(item));
		res.putClientProperty("JButton.buttonType", "square");
		res.setToolTipText(item.name());
		res.setOpaque(false);
		final ActionListener aa = this;
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aa.actionPerformed(e);
				EditorSession es = MainFrame.getInstance().getActiveEditorSession();
				if (es == null) {
					MainFrame.showMessageDialog("<html>" +
										"No active editor session!<br>" +
										"Please open a graph file or create a new one." +
										"", "Error");
					return;
				}
				if (es.getSelectionModel().getActiveSelection() == null ||
									es.getSelectionModel().getActiveSelection().getEdges().size() <= 0) {
					MainFrame.showMessageDialog("<html>" +
										"This command requires the selection of at least one<br>" +
										"edge. The style of the edge-selection will be modifed<br>" +
										"to reflect the requested edge type.", "Errorr");
					return;
				}
				Graph g = es.getGraph();
				g.getListenerManager().transactionStarted(e);
				try {
					SBGNgraphHelper.setEdgeStyle(e, es.getSelectionModel().getActiveSelection().getEdges(), item, consumption, production, reversible);
				} finally {
					g.getListenerManager().transactionFinished(e);
				}
			}
		});
		return res;
	}
	
	private Icon getEdgeStyleIcon(SBGNarc arc) {
		String value = null;
		switch (arc) {
			case ConsumptionProductionHorOrVert:
				value = "StandardArrowShape";
				break;
			case ConsumptionProductionHor:
				value = "StandardArrowShape";
				break;
			case ConsumptionProductionVert:
				value = "StandardArrowShapeVert";
				break;
			case Modulation:
				value = "ThinDiamondArrowShape";
				break;
			case Stimulation:
				value = "ThinStandardArrowShape";
				break;
			case Catalysis:
				value = "ThinCircleArrowShape";
				break;
			case Inhibition:
				value = "InhibitorArrowShape";
				break;
			case Trigger:
				value = "ThinTriggerArrowShape";
				break;
			case Logic:
			case Equivalence:
				value = "NoArrowShape";
				break;
		}
		if (value == null)
			return null;
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/') + "/images";
		ImageIcon icon = null;
		URL u = cl.getResource(path + "/" + value + ".png");
		if (u != null)
			icon = new ImageIcon(u);
		return icon;
	}
	
	private JComponent getAddButton(final SBGNitem item) {
		JButton res = new JButton(getIcon(item));
		res.putClientProperty("JButton.buttonType", "square");
		res.setToolTipText(item.toString());
		res.setOpaque(false);
		final ActionListener aa = this;
		res.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				aa.actionPerformed(e);
				EditorSession es = MainFrame.getInstance().getActiveEditorSession();
				if (es == null) {
					MainFrame.showMessageDialog("<html>" +
										"No active editor session!<br>" +
										"Please open a graph file or create a new one.", "Error");
					return;
				}
				boolean setStyle = es.getSelectionModel().getActiveSelection().getElements().size() > 0;
				Graph g = es.getGraph();
				if (setStyle) {
					if (es.getSelectionModel().getActiveSelection().getNodes().size() == 0 &&
										es.getSelectionModel().getActiveSelection().getEdges().size() > 0) {
						g.numberGraphElements();
						Collection<Edge> toBeDeleted = new ArrayList<Edge>();
						final Collection<Node> newNodes = new ArrayList<Node>();
						for (Edge edge : es.getSelectionModel().getActiveSelection().getEdges()) {
							Vector2d pa = AttributeHelper.getPositionVec2d(edge.getSource());
							Vector2d pb = AttributeHelper.getPositionVec2d(edge.getTarget());
							Vector2d pos = new Vector2d((pa.x + pb.x) / 2, (pa.y + pb.y) / 2);
							Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x, pos.y));
							SBGNgraphHelper.addItemOrSetStyle(n, item, entityPoolNodeLabel,
												info1, info2, state1, state2, clone, cloneMarkerText, false);
							newNodes.add(n);
							g.addEdgeCopy(edge, edge.getSource(), n);
							g.addEdgeCopy(edge, n, edge.getTarget());
							SBGNgraphHelper.addItemOrSetStyle(n, item, entityPoolNodeLabel,
												info1, info2, state1, state2, clone, cloneMarkerText, setStyle);
							toBeDeleted.add(edge);
						}
						g.deleteAll((Collection) toBeDeleted);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								GraphHelper.selectNodes(newNodes);
							}
						});
					} else {
						for (Node n : es.getSelectionModel().getActiveSelection().getNodes()) {
							SBGNgraphHelper.addItemOrSetStyle(n, item, entityPoolNodeLabel,
												info1, info2, state1, state2, clone, cloneMarkerText, setStyle);
						}
					}
					GraphHelper.issueCompleteRedrawForGraph(es.getGraph());
				} else {
					g.getListenerManager().transactionStarted(e);
					try {
						Vector2d pos = SBGNgraphHelper.getTargetPositionForNewElement(g);
						Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(pos.x, pos.y));
						SBGNgraphHelper.addItemOrSetStyle(n, item, entityPoolNodeLabel,
											info1, info2, state1, state2, clone, cloneMarkerText, setStyle);
					} finally {
						g.getListenerManager().transactionFinished(e);
					}
				}
			}
		});
		return res;
	}
	
	private Icon getIcon(SBGNitem item) {
		String value = null;
		switch (item) {
			case UnspecifiedEntityNode:
				value = "Ellipse";
				break;
			case SimpleChemical:
				value = "Simple Chemical";
				break;
			case MacroMolecule:
				value = "Rectangle";
				break;
			case GeneticEntity:
				value = "Nucleic Acid Feature";
				break;
			case MultimerMacrolecule:
				value = "Multi Rectangle";
				break;
			case MultimerSimpleChemical:
				value = "Multi Oval";
				break;
			case MultimerGeneticEntity:
				value = "Multi Nucleic Acid Feature";
				break;
			case SourceSink:
				value = "Source or Sink";
				break;
			case TagRight:
				value = "Tag (right)";
				break;
			case TagLeft:
				value = "Tag (left)";
				break;
			case TagUp:
				value = "Tag (up)";
				break;
			case TagDown:
				value = "Tag (down)";
				break;
			case Observable:
				value = "Observable";
				break;
			case Pertubation:
				value = "Pertubation";
				break;
			case Complex:
				value = "Complex";
				break;
			case Submap:
				value = "Rectangle";
				break;
			
			case Transition:
				value = "Transition";
				break;
			case Omitted:
				value = "Omitted";
				break;
			case Uncertain:
				value = "Uncertain";
				break;
			case Associaction:
				value = "Association";
				break;
			case Dissociation:
				value = "Dissociation";
				break;
			case Compartment:
				value = "Compartment";
				break;
			case AND:
				value = "AND";
				break;
			case OR:
				value = "OR";
				break;
			case NOT:
				value = "NOT";
				break;
		}
		if (value == null)
			return null;
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/') + "/shapes";
		ImageIcon icon = null;
		URL u = cl.getResource(path + "/" + value + ".png");
		if (u != null)
			icon = new ImageIcon(u);
		return icon;
	}
	
	private void initSettingsEditors() {
		unitOfInformation1 = new JTextField("pre:label");
		unitOfInformation2 = new JTextField();
		stateVariable1 = new JTextField("value");
		stateVariable2 = new JTextField();
		cloneMarker = new JCheckBox("Add (text:)", clone);
		cloneMarkerTextEditor = new JTextField("marker");
		entityPoolNodeLabelEditor = new JTextField("LABEL");
		consumptionEditor = new JTextField("N");
		productionEditor = new JTextField("N");
		reversibleSelection = new JCheckBox("reversible", reversible);
		
		unitOfInformation1.setOpaque(false);
		unitOfInformation2.setOpaque(false);
		stateVariable1.setOpaque(false);
		stateVariable2.setOpaque(false);
		cloneMarker.setOpaque(false);
		cloneMarkerTextEditor.setOpaque(false);
		entityPoolNodeLabelEditor.setOpaque(false);
		consumptionEditor.setOpaque(false);
		productionEditor.setOpaque(false);
		reversibleSelection.setOpaque(false);
	}
	
	@Override
	public String getTitle() {
		return "SBGN";
	}
	
	public void actionPerformed(ActionEvent e) {
		info1 = unitOfInformation1.getText();
		info2 = unitOfInformation2.getText();
		state1 = stateVariable1.getText();
		state2 = stateVariable2.getText();
		clone = cloneMarker.isSelected();
		cloneMarkerText = cloneMarkerTextEditor.getText();
		entityPoolNodeLabel = entityPoolNodeLabelEditor.getText();
		
		consumption = consumptionEditor.getText();
		production = productionEditor.getText();
		
		reversible = reversibleSelection.isSelected();
		
		if (info1.length() <= 0)
			info1 = null;
		if (info2.length() <= 0)
			info2 = null;
		if (state1.length() <= 0)
			state1 = null;
		if (state2.length() <= 0)
			state2 = null;
		if (cloneMarkerText.length() <= 0)
			cloneMarkerText = null;
		
		if (consumption.length() <= 0)
			consumption = null;
		if (production.length() <= 0)
			production = null;
	}
	
	@Override
	public boolean visibleForView(View v) {
		if (v != null) {
			return (v instanceof View2D) && !SGBNaddOnLoaded();
		} else
			return false;
	}
	
	private boolean SGBNaddOnLoaded() {
		return MainFrame.getInstance().getPluginManager().isInstalled("SBGN-ED");
	}
	
}
