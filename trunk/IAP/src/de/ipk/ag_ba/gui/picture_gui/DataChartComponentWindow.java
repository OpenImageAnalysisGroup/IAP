package de.ipk.ag_ba.gui.picture_gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemOptions;
import org.color.ColorUtil;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AbstractAttributeListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
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
		setSize(SystemOptions.getInstance("charts.ini", null).getInteger("Window", "Width", 800),
				SystemOptions.getInstance("charts.ini", null).getInteger("Window", "Height", 600));
		setLayout(TableLayout.getLayout(TableLayout.FILL, TableLayout.FILL));
		final Graph graph = new AdjListGraph();
		final Node ge = graph.addNode(AttributeHelper.getDefaultGraphicsAttributeForKeggNode(100, 100));
		NodeHelper nh = new NodeHelper(ge);
		// nh.setLabel(experiment.iterator().next().getName());
		nh.addDataMapping(experiment.iterator().next());
		nh.removeAttribute("graphics");
		graph.removeAttribute("directed");
		
		setDefaultChartDisplay(graph, ge, experiment.iterator().next().getName());
		
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
	
	private void setDefaultChartDisplay(Graph graph, Node ge, String title) {
		int idx = 0;
		Boolean mFalse = new Boolean(false);
		Boolean mTrue = new Boolean(true);
		
		graph.setName("chartUI");
		
		String chartTitle = (String) AttributeHelper.getAttributeValue(ge, "charting", "chartTitle" + idx, title, title);
		boolean showCategoryAxis = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showCategoryAxis",
				SystemOptions.getInstance("charts.ini", null).getBoolean("Axis", "Show-X", true), mFalse)).booleanValue();
		boolean showRangeAxis = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showRangeAxis",
				SystemOptions.getInstance("charts.ini", null).getBoolean("Axis", "Show-Y", true),
					mFalse)).booleanValue();
		Boolean showRangeAxis2 = ((Boolean) AttributeHelper.getAttributeValue(ge, "charting", "showRangeAxis", null,
				SystemOptions.getInstance("charts.ini", null).getBoolean("Axis", "Show-Y2", true), false));
		int axisFontSize = ((Integer) AttributeHelper.getAttributeValue(graph, "", "node_plotAxisFontSize", new Integer(
				SystemOptions.getInstance("charts.ini", null).getInteger("Axis", "Font Size", 10)), new Integer(30))).intValue();
		boolean showGridCategory = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showGridCategory",
				SystemOptions.getInstance("charts.ini", null).getBoolean("Axis", "Show-Grid-X", false), mTrue)).booleanValue();
		Double temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_outlineBorderWidth",
				SystemOptions.getInstance("charts.ini", null).getDouble("Plot", "Outline Line Width", 1),
					new Double(4d));
		temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_chartStdDevLineWidth",
				SystemOptions.getInstance("charts.ini", null).getDouble("Plot", "Std-Dev Line Width", 1),
					new Double(4d));
		boolean showLegend = ((Boolean) AttributeHelper.getAttributeValue(ge, "charting", "show_legend",
				SystemOptions.getInstance("charts.ini", null).getBoolean("Legend", "Show", true),
					new Boolean(false))).booleanValue();
		String rangeAxis = (String) AttributeHelper.getAttributeValue(ge, "charting", "rangeAxis", "[unit]", "[unit]");
		String domainAxis = (String) AttributeHelper.getAttributeValue(ge, "charting", "domainAxis",
					new String("[unit]"), new String("[unit]"));
		String add = "";
		Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.CHARTBACKGROUNDCOLOR
				+ add, ColorUtil.getHexFromColor(Color.WHITE));
		ge.addAttribute(newAtt, "charting");
	}
}
