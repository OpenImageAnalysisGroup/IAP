package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.data_mapping;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.ReleaseInfo;
import org.apache.commons.collections.set.ListOrderedSet;
import org.color.ColorUtil;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.WeightedShortestPathSelectionAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartComponentManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.MyChartCellRenderer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.MyDiagramPlacementSettingCellRenderer;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.kegg_reaction.CreateKeggReactionNetworkAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.AbstractExperimentDataProcessor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.MapResult;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

public class DataMapping extends AbstractExperimentDataProcessor {
	
	ExperimentInterface experimentData;
	
	boolean addNewNodesForNotMappedSubstances;
	int minimumLineCount = 0;
	int numberOfChartsInRow = -1;
	
	boolean considerCompoundDb;
	boolean considerEnzymeDb;
	boolean considerKoDb;
	boolean considerMappingToKEGGmapNodes;
	
	private boolean supressReset = false;
	
	ChartComponent diagramStyleRef = GraffitiCharts.AUTOMATIC;
	
	private ActionEvent ae;
	
	private JComboBox dropDownChartStyle;
	
	private View activeView;
	
	private ShowMappingResults doShowResult = ShowMappingResults.NORMAL;
	
	public DataMapping() {
		super();
	}
	
	public DataMapping(boolean register) {
		super(register);
	}
	
	@Override
	public void setExperimentData(ExperimentInterface md) {
		experimentData = md;
	}
	
	@Override
	public void execute() {
		processData();
		// cleanup is called by background thread
	}
	
	private void processDataResult() {
		for (Runnable r : postProcessors)
			r.run();
		setExperimentData(null);
	}
	
	@Override
	public boolean activeForView(View v) {
		activeView = v;
		return true;
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "Data mapping is performed, by connecting measured data with<br>"
				+ "corresponding network nodes or eges of the current graph.<br><br><small>"
				+ "If no graph window is open, a new graph will be created.<br><br>"
				+ "By default the connection is established, in case a substance name<br>"
				+ "is equal to a node label. Optionally additional data annotations<br>"
				+ "or build-in synonyme databases may be used to connect data.<br>"
				+ "To map data to edges, edge labels could be specified, or experiment data<br>"
				+ "substance names specify source and target node label, divided by '^'.<br><br>" + "";
	}
	
	@Override
	public Parameter[] getParameters() {
		
		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
		
		if (activeView != null)
			parameters.add(new BooleanParameter(true,
					"<html>Create new nodes or edges for measured<br>substances that can not be mapped", "<html>"
							+ "Hint: Use substance IDs like A^B to specify edge datamapping<br>"
							+ "for edges with no edge label, connecting node A and B."));
		
		if (activeView != null) {
			parameters.add(new BooleanParameter(true, "<html>Consider compound synonyms", null));
			
			parameters.add(new BooleanParameter(true, "<html>Consider enzyme synonyms", null));
			
			if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS)) {
				parameters.add(new BooleanParameter(false, "<html>Consider KO database IDs, Gene IDs", null));
				
				parameters.add(new BooleanParameter(false, "<html>Map to KEGG map nodes (requires SOAP access)", "<html>"
						+ "If enabled, KO, Compound and Enzyme elements of a map link node<br>"
						+ "are retrieved via KEGG SOAP API, to enable mapping onto map nodes."));
			}
		}
		parameters.add(new IntegerParameter(0, "Minimum condition count", "<html>"
				+ "Omit mapping in case minimum<br>condition count is not met."));
		
		dropDownChartStyle = new JComboBox();
		dropDownChartStyle.setOpaque(false);
		
		// ArrayList<String> chartOptions = new ArrayList<String>();
		// // for(GraffitiCharts c : )
		// chartOptions.add(GraffitiCharts.LINE.getName());
		// chartOptions.add(GraffitiCharts.BAR.getName());
		// chartOptions.add(GraffitiCharts.BAR_FLAT.getName());
		// chartOptions.add(GraffitiCharts.PIE.getName());
		// chartOptions.add(GraffitiCharts.PIE3D.getName());
		// chartOptions.add(GraffitiCharts.HEATMAP.getName());
		// chartOptions.add(GraffitiCharts.HIDDEN.getName());
		
		String[] times = Experiment.getTimes(experimentData);
		String[] plants = Experiment.getConditionsAsString(experimentData);
		
		ChartComponent initChartStyle = GraffitiCharts.BAR_FLAT;
		boolean shouldBeAutomatic = false;
		
		// if data already mapped onto graph, select the automatic option in order to keep
		// chart styles of previous mappings
		if (graph != null && activeView != null) {
			for (GraphElement ge : graph.getGraphElements())
				try {
					Attribute a = ge.getAttribute(Experiment2GraphHelper.mapFolder + Attribute.SEPARATOR + Experiment2GraphHelper.mapVarName);
					if (a != null) {
						shouldBeAutomatic = true;
						initChartStyle = GraffitiCharts.AUTOMATIC;
						break;
					}
				} catch (AttributeNotFoundException anfe) {
					// empty
				}
		}
		if (!shouldBeAutomatic) {
			
			if (times != null && times.length > 1)
				initChartStyle = GraffitiCharts.LINE;
			else
				if (times != null && times.length == 1 && plants != null && plants.length == 1)
					initChartStyle = GraffitiCharts.HEATMAP;
				else
					initChartStyle = GraffitiCharts.BAR_FLAT;
		}
		
		Collection<ChartComponent> cc = ChartComponentManager.getInstance().getChartComponents();
		cc.remove(GraffitiCharts.LEGEND_ONLY);
		
		ObjectListParameter chartOption = new ObjectListParameter(initChartStyle, "Initial charting-style",
				"You may later use the Node-/Edge-Sidepanels to modify the charting style.", cc);
		
		chartOption.setRenderer(new MyChartCellRenderer());
		
		parameters.add(chartOption);
		
		if (activeView != null) {
			ArrayList<Integer> validOptions = new ArrayList<Integer>();
			for (int i = -2; i < 5; i++)
				if (i != 0)
					validOptions.add(i);
			ObjectListParameter chartNumbers = new ObjectListParameter(numberOfChartsInRow, "<html>"
					+ "Number of charts in a row inside drawing area<br>" + "(in case of multiple data mappings)",
					"Specifies the display configuration for multiple data mappings.", validOptions);
			MyDiagramPlacementSettingCellRenderer rr2 = new MyDiagramPlacementSettingCellRenderer();
			chartNumbers.setRenderer(rr2);
			parameters.add(chartNumbers);
		}
		
		return parameters.toArray(new Parameter[] {});
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		if (activeView != null)
			addNewNodesForNotMappedSubstances = ((BooleanParameter) params[i++]).getBoolean();
		else
			addNewNodesForNotMappedSubstances = true;
		
		if (activeView != null) {
			considerCompoundDb = ((BooleanParameter) params[i++]).getBoolean();
			considerEnzymeDb = ((BooleanParameter) params[i++]).getBoolean();
			if (ReleaseInfo.getIsAllowedFeature(FeatureSet.KEGG_ACCESS)) {
				considerKoDb = ((BooleanParameter) params[i++]).getBoolean();
				considerMappingToKEGGmapNodes = ((BooleanParameter) params[i++]).getBoolean();
			} else {
				considerKoDb = false;
				considerMappingToKEGGmapNodes = false;
			}
		}
		minimumLineCount = ((IntegerParameter) params[i++]).getInteger();
		diagramStyleRef = (ChartComponent) ((ObjectListParameter) params[i++]).getValue();
		if (activeView != null) {
			numberOfChartsInRow = (Integer) ((ObjectListParameter) params[i++]).getValue();
		} else
			numberOfChartsInRow = -1;
	}
	
	@Override
	public void setActionEvent(ActionEvent a) {
		this.ae = a;
	}
	
	@Override
	public void processData() {
		boolean doLayoutP = graph == null;
		final boolean doLayout = doLayoutP;
		final Collection<GraphElement> selectedGraphElements = selection != null ? selection.getElements()
				: new ArrayList<GraphElement>();
		final int diagramsPerRow = numberOfChartsInRow;
		
		Graph workGraph = this.graph;
		
		if (selectedGraphElements.size() == 0 && workGraph != null)
			selectedGraphElements.addAll(workGraph.getGraphElements());
		
		final Experiment2GraphHelper mappingService = new Experiment2GraphHelper();
		BackgroundTaskHelper bth = new BackgroundTaskHelper(getMappingTask(experimentData,
				addNewNodesForNotMappedSubstances, doLayout, selectedGraphElements, mappingService, minimumLineCount,
				diagramsPerRow, considerCompoundDb, considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes,
				workGraph), mappingService, "Data Mapping", "Data Mapping Task", true, false);
		bth.startWork(this);
	}
	
	private Runnable getMappingTask(final ExperimentInterface doc, final boolean createNodesIfNotMapped,
			final boolean doLayout, final Collection<GraphElement> selectedGraphElements,
			final Experiment2GraphHelper mappingService, final int minimumLineCount, final int diagramsPerRow,
			final boolean considerCompoundDb, final boolean considerEnzymeDb, final boolean considerKoDb,
			final boolean considerMappingToKEGGmapNodes, final Graph workGraph) {
		return new Runnable() {
			@Override
			public void run() {
				doMapping(doc, createNodesIfNotMapped,
						doLayout,// workSession,
						selectedGraphElements, mappingService, minimumLineCount, diagramsPerRow, considerCompoundDb,
						considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes, workGraph);
			}
		};
	}
	
	private ArrayList<Node> getMappingNodes(List<Node> networkNodes, List<Node> searchThis) {
		HashSet<String> searchIds = new HashSet<String>();
		for (Node n : searchThis) {
			String lbl = AttributeHelper.getLabel(n, null);
			if (lbl != null && lbl.length() > 0)
				searchIds.add(lbl);
		}
		ArrayList<Node> res = new ArrayList<Node>();
		for (Node n : networkNodes) {
			String keggId = KeggGmlHelper.getKeggId(n);
			boolean m = false;
			for (String s : searchIds) {
				if (keggId.indexOf(s) >= 0) {
					m = true;
					break;
				}
			}
			if (m)
				res.add(n);
		}
		return res;
	}
	
	@Override
	public String getName() {
		if (activeView != null)
			return "<html><center>Perform data mapping<br><small>(integrate data into network)";
		else
			return "<html><center>Show data in new window<br><small>(open a graph to integrate data into network)";
		
	}
	
	@Override
	public String getShortName() {
		if (activeView != null)
			return "Integrate data into network";
		else
			return "Show all data in new window";
		
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
	
	private synchronized void doMapping(final ExperimentInterface md, final boolean createNodesIfNotMapped,
			final boolean doLayout, final Collection<GraphElement> selectedGraphElements,
			final Experiment2GraphHelper mappingService, final int minimumLineCount, final int diagramsPerRow,
			final boolean considerCompoundDb, final boolean considerEnzymeDb, final boolean considerKoDb,
			final boolean considerMappingToKEGGmapNodes, Graph workGraph) {
		final EditorSession newlyCreatedWorkSession;
		if (workGraph == null && showResult() != ShowMappingResults.MAP_WITHOUT_VIEW) {
			newlyCreatedWorkSession = GravistoService.getInstance().getMainFrame().createNewSession();
			workGraph = newlyCreatedWorkSession.getGraph();
		} else
			newlyCreatedWorkSession = null;
		
		try {
			workGraph.getListenerManager().transactionStarted(this);
			
			final MapResult mapResult = mappingService.mapDataToGraphElements(true, md, selectedGraphElements,
					(createNodesIfNotMapped ? workGraph : null), false, diagramStyleRef.getName(), minimumLineCount,
					diagramsPerRow, considerCompoundDb, considerEnzymeDb, considerKoDb, considerMappingToKEGGmapNodes, true);
			
			boolean colorize = true;
			
			if (colorize) {
				HashMap<String, ArrayList<Node>> coloredNodes = new HashMap<String, ArrayList<Node>>();
				try {
					for (Node n : workGraph.getNodes()) {
						String label = AttributeHelper.getLabel(n, null);
						if (label != null && label.contains(": hue=")) {
							String imageType = label.substring(0, label.indexOf(": hue="));
							if (imageType == null || imageType.length() == 0)
								continue;
							if (!coloredNodes.containsKey(imageType))
								coloredNodes.put(imageType, new ArrayList<Node>());
							coloredNodes.get(imageType).add(n);
							String color = label.substring(label.indexOf(": hue=") + ": hue=".length());
							float hue = Float.parseFloat(color) / 360f;
							float saturation = 1;
							float brightness = 1;
							int rgb = Color.HSBtoRGB(hue, saturation, brightness);
							// AttributeHelper.setFillColor(n, new Color(rgb));
							AttributeHelper.setAttribute(n, "charting", "background_color", ColorUtil
									.getHexFromColor(new Color(rgb)));
							AttributeHelper.setOutlineColor(n, new Color(rgb));
							AttributeHelper.setSize(n, 80, 120);
						}
					}
					int idx = 0;
					for (String type : coloredNodes.keySet()) {
						ArrayList<Node> nodes = coloredNodes.get(type);
						
						for (Node n : nodes)
							AttributeHelper.setPosition(n, 100, 70 + 120 * idx);
						GridLayouterAlgorithm.layoutOnGrid(nodes, Double.MAX_VALUE, 0, 0);
						idx++;
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			
			if (!mappingService.wantsToStop()) {
				if (doLayout) {
					boolean reactionMapping = false;
					if (reactionMapping) {
						final Graph rg = CreateKeggReactionNetworkAlgorithm.getReactionNetwork();
						ArrayList<Node> mappedNodesA = getMappingNodes(rg.getNodes(), workGraph.getNodes());
						ListOrderedSet mappedNodes = new ListOrderedSet();
						mappedNodes.addAll(mappedNodesA);
						ArrayList<GraphElement> elements = new ArrayList<GraphElement>();
						for (GraphElement ge : mappedNodesA) {
							if (ge instanceof Node) {
								Node n = (Node) ge;
								Collection<GraphElement> shortestPathNodesAndEdges = WeightedShortestPathSelectionAlgorithm
										.getShortestPathElements(rg.getGraphElements(), n, mappedNodes, false, false, false,
												Double.MAX_VALUE, null, false, false, false, true);
								elements.addAll(shortestPathNodesAndEdges);
							}
						}
						ArrayList<GraphElement> allElements = new ArrayList<GraphElement>();
						allElements.addAll(rg.getGraphElements());
						for (GraphElement ge : allElements) {
							if (ge instanceof Node) {
								if (!elements.contains(ge))
									rg.deleteNode((Node) ge);
							}
						}
						MainFrame.getInstance().showGraph(rg, ae);
					}
					final Selection sel = new Selection();
					sel.addAll(selectedGraphElements);
					if (showResult() == ShowMappingResults.NORMAL)
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								MainFrame.getInstance().setActiveSession(newlyCreatedWorkSession, activeView);
								MainFrame.getInstance().showViewChooserDialog(newlyCreatedWorkSession, false, ae);
								MainFrame.showMessageDialog("<html>" + mapResult.substanceCount
										+ " substance IDs have been used to create a new target graph.<br>"
										+ "Mapping Details:<ul>" + "<li>" + mapResult.newNodes + " new nodes and "
										+ mapResult.newEdges + " edges have been created.<br>" + "<li>"
										+ mapResult.targetCountNodes + " nodes and " + mapResult.targetCountEdges
										+ " edges contain data mappings from this operation.<br>" + "<li>At least "
										+ mapResult.minMappingCount + " and at most " + mapResult.maxMappingCount + " substance"
										+ (mapResult.maxMappingCount > 1 ? "s" : "") + " map"
										+ (mapResult.maxMappingCount > 1 ? "" : "s") + " to a single graph element." + "</ul>",
										"Results of Data Mapping");
							}
						});
				} else {
					if (showResult() == ShowMappingResults.NORMAL)
						if (mapResult != null)
							MainFrame.showMessageDialog("<html>" + mapResult.substanceCount
									+ " substances have been mapped onto graph " + workGraph.getName() + ".<br>" + "<br>"
									+ "Mapping Details:<ul>" + "<li>" + mapResult.newNodes + " new nodes and " + mapResult.newEdges
									+ " edges have been created." + "<li>" + mapResult.targetCountNodes + " nodes and "
									+ mapResult.targetCountEdges + " edges contain data mappings from this operation.<br>"
									+ "<li>At least " + mapResult.minMappingCount + " and at most " + mapResult.maxMappingCount
									+ " substance" + (mapResult.maxMappingCount > 1 ? "s" : "") + " map"
									+ (mapResult.maxMappingCount > 1 ? "" : "s") + " to a single graph element." + "</ul><br>"
									+ "Graph elements with a new data mapping have been selected.<br><br>"
									+ "Hint: To limit the target scope of this operation you may select graph<br>"
									+ "elements before performing the data mapping.", "Data Mapping Results");
				}
			}
		} catch (NullPointerException e) {
			ErrorMsg.addErrorMessage(e);
		} finally {
			workGraph.getListenerManager().transactionFinished(this, true);
			if (showResult() != ShowMappingResults.MAP_WITHOUT_VIEW)
				GraphHelper.issueCompleteRedrawForGraph(workGraph);
		}
		processDataResult();
	}
	
	public void setDoShowResult(ShowMappingResults showResult) {
		this.doShowResult = showResult;
	}
	
	public ShowMappingResults showResult() {
		return doShowResult;
	}
	
	@Override
	public void reset() {
		if (!isSupressReset()) {
			super.reset();
			doShowResult = ShowMappingResults.NORMAL;
		}
	}
	
	public void setSupressReset(boolean supressReset) {
		this.supressReset = supressReset;
	}
	
	public boolean isSupressReset() {
		return supressReset;
	}
	
	@Override
	public ImageIcon getIcon() {
		return new ImageIcon(GravistoService.getResource(getClass(), "ExperimentdataInContextOfNetwork", "png"));
	}
	
	public enum ShowMappingResults {
		NORMAL, DONT_SHOW_RESULTDIALOG, MAP_WITHOUT_VIEW
		
	}
}
