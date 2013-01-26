package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AbstractAttributeListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.inspectors.defaults.DefaultEditPanel;
import org.graffiti.plugins.inspectors.defaults.GraphTab;
import org.graffiti.plugins.inspectors.defaults.NodeTab;
import org.graffiti.selection.Selection;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionModel;
import org.graffiti.session.EditorSession;

import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;

public class DataChartComponentWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private final ExperimentInterface experiment;
	
	public DataChartComponentWindow(ExperimentInterface experiment) {
		this.experiment = experiment;
		initGui();
	}
	
	private void initGui() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Data Chart - " + experiment.iterator().next().getName());
		setIconImage(IAPimages.getImage(IAPimages.getHistogramIcon()));
		setSize(SystemOptions.getInstance().getInteger("Data Chart", "Window Width", 800),
				SystemOptions.getInstance().getInteger("Data Chart", "Window Height", 600));
		setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		final Graph graph = new AdjListGraph();
		final Node ge = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForKeggNode(100, 100));
		NodeHelper nh = new NodeHelper(ge);
		// nh.setLabel(experiment.iterator().next().getName());
		nh.addDataMapping(experiment.iterator().next());
		nh.removeAttribute("graphics");
		graph.removeAttribute("directed");
		final XmlDataChartComponent chart = new XmlDataChartComponent(experiment, GraffitiCharts.AUTOMATIC.getName(), graph, ge);
		chart.setBorder(BorderFactory.createRaisedBevelBorder());
		EditorSession s = new EditorSession(graph);
		s.getGraph().getListenerManager().addDelayedAttributeListener(new AbstractAttributeListener() {
			@Override
			public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
				chart.removeAll();
				chart.initGUI(GraffitiCharts.AUTOMATIC.getName(), graph, ge, experiment);
			}
		});
		
		GraphTab graphSettingsEditPanel = new GraphTab();
		{
			((DefaultEditPanel) graphSettingsEditPanel.editPanel).setShowCompleteRedrawCommand(false);
			((DefaultEditPanel) graphSettingsEditPanel.editPanel).removeAll();
			((DefaultEditPanel) graphSettingsEditPanel.editPanel).initGUI();
			
			graphSettingsEditPanel.setEditPanelInformation(
					MainFrame.getInstance().getEditComponentManager().getEditComponents(), null);
			graphSettingsEditPanel.sessionChanged(s);
		}
		NodeTab nodeSettingsEditPanel = new NodeTab();
		{
			((DefaultEditPanel) nodeSettingsEditPanel.editPanel).setShowCompleteRedrawCommand(false);
			((DefaultEditPanel) nodeSettingsEditPanel.editPanel).removeAll();
			((DefaultEditPanel) nodeSettingsEditPanel.editPanel).initGUI();
			
			nodeSettingsEditPanel.setEditPanelInformation(
					MainFrame.getInstance().getEditComponentManager().getEditComponents(), null);
			Selection sel = new Selection("Settings Selection");
			sel.add(ge);
			s.setSelectionModel(new SelectionModel());
			s.getSelectionModel().add(sel);
			nodeSettingsEditPanel.sessionChanged(s);
			nodeSettingsEditPanel.selectionChanged(new SelectionEvent(sel));
		}
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Settings A", graphSettingsEditPanel);
		tabbedPane.addTab("Settings B", nodeSettingsEditPanel);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, chart, tabbedPane);
		splitPane.setDividerLocation(getWidth() - 300);
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);
		add(TableLayout.getSplitVertical(new JLabel("ToDo - some settings and filter options"),
				splitPane, TableLayout.PREFERRED, TableLayout.FILL), "0,0");
	}
}
