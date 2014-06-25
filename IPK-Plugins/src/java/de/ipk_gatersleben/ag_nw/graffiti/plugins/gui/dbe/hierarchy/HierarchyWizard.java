package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.hierarchy;

import java.util.ArrayList;
import java.util.Collection;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.CreateFuncatGraphAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.ClusterHistogramFisherTest;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram.CreateGOchildrenClustersHistogramAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class HierarchyWizard extends AbstractAlgorithm {
	
	double lowerLimit = -1, upperLimit = 1;
	
	public void execute() {
		// 0. Calculate average sample value
		// 1. Assign cluster IDs, according to specified limits
		// up/unchanged/down regulated
		// 2. Create Hierarchy Tree
		// 3. Hide Gene-Nodes
		// 4. Layout Tree
		// 5. Calculate Fisher test
		// 6. Highlight selection (if not empty)
		// 7. If selection after Fisher test is empty, layout tree again
		
		final Graph g = graph;
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		
		final String taskID = "Analysis Pipeline";
		
		final Collection<Node> nodeList = getSelectedOrAllNodes();
		
		Runnable bt = new Runnable() {
			
			public void run() {
				status.setCurrentStatusValue(-1);
				status.setCurrentStatusText1("Step 1/5: Group data (up/down/unchanged)");
				status.setCurrentStatusText2("(next: create hierarchy tree)");
				ArrayList<NodeHelper> geneNodes = new ArrayList<NodeHelper>();
				for (Node n : nodeList) {
					NodeHelper nh = new NodeHelper(n);
					geneNodes.add(nh);
					double avg = nh.getAverage();
					if (!Double.isNaN(avg)) {
						if (avg < lowerLimit) {
							nh.setClusterID("down");
						} else
							if (avg > upperLimit) {
								nh.setClusterID("up");
							} else {
								nh.setClusterID("unchanged");
							}
					}
				}
				if (status.wantsToStop())
					return;
				status.setCurrentStatusText1("Step 2/5: Create hierarchy tree");
				status.setCurrentStatusText2("(next: hide graph data nodes)");
				GravistoService.getInstance().runAlgorithm(
									new CreateFuncatGraphAlgorithm(), g, new Selection(""),
									HierarchyWizard.this.getActionEvent());
				while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning("Create Hierarchy")) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// empty
					}
					if (status.wantsToStop())
						return;
					
				}
				if (status.wantsToStop())
					return;
				status.setCurrentStatusText1("Step 3/5: Hide data nodes");
				status.setCurrentStatusText2("(next: analysis of cluster frequency)");
				if (geneNodes != null && geneNodes.size() > 0) {
					Graph graph = geneNodes.iterator().next().getGraph();
					if (graph != null)
						graph.getListenerManager().transactionStarted(this);
					for (Node n : geneNodes)
						AttributeHelper.setHidden(true, n, true, true, true);
					if (graph != null)
						graph.getListenerManager().transactionFinished(this, true);
					GraphHelper.issueCompleteRedrawForGraph(graph);
				}
				if (status.wantsToStop())
					return;
				status.setCurrentStatusText1("Step 4/5: Detect cluster frequency distribution");
				status.setCurrentStatusText2("(next: optional enrichment analysis)");
				GravistoService.getInstance().runAlgorithm(
									new CreateGOchildrenClustersHistogramAlgorithm(), g, new Selection(""), HierarchyWizard.this.getActionEvent());
				
				if (status.wantsToStop())
					return;
				status.setCurrentStatusText1("Step 5/5: Perform significance analysis?");
				status.setCurrentStatusText2("Press OK or Cancel (this step is optional)");
				status.setCurrentStatusValue(100);
				status.setPluginWaitsForUser(true);
				
				while (status.pluginWaitsForUser()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// empty
					}
				}
				status.setCurrentStatusValue(-1);
				if (!status.wantsToStop()) {
					status.setCurrentStatusText1("Step 5/5: Analyse enrichment?");
					status.setCurrentStatusText2("");
					GravistoService.getInstance().runAlgorithm(
										new ClusterHistogramFisherTest(), g, new Selection(""), HierarchyWizard.this.getActionEvent());
				}
				
				String id = new ClusterHistogramFisherTest().getName();
				while (BackgroundTaskHelper.isTaskWithGivenReferenceRunning(id)) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// empty
					}
				}
				// status.setCurrentStatusText1("Step 5/6: Layout hierarchy tree");
				// status.setCurrentStatusText2("(next: update graph view)");
				// GravistoService.getInstance().runAlgorithm(
				// new DotLayoutAlgorithm(), g, new Selection(""), HierarchyWizard.this.getActionEvent());
				// GravistoService.getInstance().runAlgorithm(
				// new CenterLayouterAlgorithm(), g, new Selection(""), HierarchyWizard.this.getActionEvent());
				// GraphHelper.moveGraph(g, 100d, 0);
				
				pretifyNodes();
				
				status.setCurrentStatusText1("Processing finished");
				status.setCurrentStatusText2("");
				status.setCurrentStatusValue(100);
			}
			
			private void pretifyNodes() {
				for (Node n : g.getNodes()) {
					if (!AttributeHelper.isHiddenGraphElement(n)) {
						NodeHelper nh = new NodeHelper(n);
						if (nh.getMappedSampleData().size() > 0) {
							if (nh.getOutDegree() > 0) {
								nh.setSize(30, 30);
								AttributeHelper.setShapeEllipse(nh);
								if (nh.getOutNeighborsIterator().next().getOutDegree() > 0)
									AttributeHelper.setLabelAlignment(-1, nh, AlignmentSetting.LEFT);
								else
									AttributeHelper.setLabelAlignment(-1, nh, AlignmentSetting.RIGHT);
								AttributeHelper.setBorderWidth(nh, 0);
								AttributeHelper.setAttribute(nh, "charting", "empty_border_width", 0d);
								AttributeHelper.setAttribute(nh, "charting", "empty_border_width_vert", 0d);
								AttributeHelper.setAttribute(nh, "graphics", "component", "chart2d_type4");
							}
						}
					}
				}
			}
		};
		
		Runnable st = new Runnable() {
			public void run() {
				GraphHelper.issueCompleteRedrawForGraph(g);
			}
		};
		
		BackgroundTaskHelper.issueSimpleTaskInWindow(taskID, "<html><br><br><br><br>", bt, st, status);
	}
	
	@Override
	public String getCategory() {
		return null;// "Hierarchy";
	}
	
	public String getName() {
		return "Analysis Pipeline";
	}
	
	@Override
	public String getDescription() {
		return "<html>Analysis Pipeline Overview:<ol>" +
							"<li>Calculation of mapping sample average value" +
							"<li>Assignment of cluster ID (up/down/unchanged) according to sample average" +
							"<li>Construction of hierarchy tree" +
							"<li>Hide data nodes" +
							"<li>Layout tree" +
							"<li>Calculation of Fisher test to detect 'unusual' cluster distribution" +
							"<li>Highlight significant nodes or hide insignificant ones";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new DoubleParameter(lowerLimit,
												"Lower Limit (down regulated)",
												"All nodes with a average data mapping value below or equal this value will be grouped into the cluster 'down'."),
							new DoubleParameter(upperLimit,
												"Upper Limit (up regulated)",
												"All nodes with a average data mapping value above or equal this value will be grouped into the cluster 'up'.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		lowerLimit = ((DoubleParameter) params[i++]).getDouble();
		upperLimit = ((DoubleParameter) params[i++]).getDouble();
	}
	
}
