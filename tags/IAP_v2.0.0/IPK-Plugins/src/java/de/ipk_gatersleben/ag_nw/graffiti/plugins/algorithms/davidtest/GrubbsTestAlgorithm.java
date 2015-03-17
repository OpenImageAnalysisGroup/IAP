/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.AttributeHelper;
import org.ErrorMsg;
import org.apache.commons.math3.distribution.TDistribution;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

public class GrubbsTestAlgorithm extends AbstractAlgorithm {
	
	private double alphavalue = 0.05d;
	private boolean doRemoveOutliers = false;
	
	@Override
	public String getName() {
		return "Grubbs' Test (detect outliers)";
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Grubbs' Test for the identification of outliers";
	}
	
	@Override
	public void execute() {
		graph.getListenerManager().transactionStarted(this);
		
		Selection sel = new Selection("id");
		
		sel
				.addAll(doGrubbsTest(GraphHelper.getSelectedOrAllNodes(selection, graph), graph, alphavalue,
						doRemoveOutliers));
		
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
		
		graph.getListenerManager().transactionFinished(this, true);
		if (doRemoveOutliers)
			GraphHelper.issueCompleteRedrawForGraph(graph);
	}
	
	@Override
	public Parameter[] getParameters() {
		if (alphavalue < 0 || alphavalue > 1)
			alphavalue = 0.05d;
		return new Parameter[] {
				new DoubleParameter(alphavalue, "alpha", "Use any alpha value, e.g. 0.05"),
				new BooleanParameter(doRemoveOutliers, "Remove Outliers",
						"If selected, all identified outliers will be removed from the dataset.") };
	}
	
	private static org.apache.commons.math3.distribution.TDistribution td = new org.apache.commons.math3.distribution.TDistribution(10);
	
	public static List<Node> doGrubbsTest(List<Node> nodes, Graph g, double alpha, boolean removeOutliers) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : nodes) {
			int removedPoints = 0;
			XMLAttribute xa;
			try {
				CollectionAttribute ca = (CollectionAttribute) node.getAttribute(Experiment2GraphHelper.mapFolder);
				xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
			} catch (AttributeNotFoundException anfe) {
				xa = null;
			}
			if (xa == null)
				continue;
			
			for (SubstanceInterface xmldata : xa.getMappedData()) {
				List<MyComparableDataPoint> allvalues = NodeTools.getSortedDataSetValues(xmldata, null);
				
				HashSet<String> knownSeries = new HashSet<String>();
				for (MyComparableDataPoint value : allvalues) {
					knownSeries.add(value.serie);
				}
				
				for (String serie : knownSeries) {
					List<MyComparableDataPoint> valuesForSeries = new ArrayList<MyComparableDataPoint>();
					HashSet<String> knownTimePoints = new HashSet<String>();
					for (MyComparableDataPoint mcdp : allvalues) {
						if (mcdp.serie.equals(serie)) {
							valuesForSeries.add(mcdp);
							knownTimePoints.add(mcdp.timeUnitAndTime);
						}
					}
					boolean outlierIdentified;
					do {
						outlierIdentified = false;
						for (String timePoint : knownTimePoints) {
							List<MyComparableDataPoint> values = getValidValues(valuesForSeries, timePoint);
							// G = (max {|Yi - Yavg|} ) / s
							// Yavg = Average value
							// s = StdDev
							// calculate s (StdDev)
							double sum = 0d;
							double min = Double.MAX_VALUE;
							double max = Double.NEGATIVE_INFINITY;
							MyComparableDataPoint min_value = null;
							MyComparableDataPoint max_value = null;
							int n = 0;
							for (MyComparableDataPoint value : values) {
								sum += value.mean;
								if (value.mean > max) {
									max = value.mean;
									max_value = value;
								}
								if (value.mean < min) {
									min = value.mean;
									min_value = value;
								}
								n++; // System.out.println(value.toString());
							}
							double avg = sum / n;
							double sumDiff = 0;
							for (MyComparableDataPoint value : values) {
								sumDiff += (value.mean - avg) * (value.mean - avg);
							}
							double stdDev = Math.sqrt(sumDiff / (n - 1));
							double m1 = Math.abs(max - avg);
							double m2 = Math.abs(min - avg);
							double maxYi_Yavg = (m1 > m2 ? m1 : m2);
							boolean isMaxPotentialOutlier = m1 > m2;
							double G = maxYi_Yavg / stdDev;
							// critical region (from Engineering Statistics Handbook)
							// G > (N-1) / N^.5 * ( (t(a/(2N), N-2))^2
							// /
							// (N-2+(t(a/(2N),N-2))^2)
							// )^0.5
							if (n - 2 > 0) {
								try {
									td = new TDistribution(n - 2);
									double t1 = td.inverseCumulativeProbability(1 - (1 - alpha) / (2 * n));
									double testG = (n - 1) / Math.sqrt(n) * Math.sqrt(t1 * t1 / (n - 2 + t1));
									if (G > testG) {
										if (isMaxPotentialOutlier)
											max_value.setIsOutlier(true, removeOutliers);
										else
											min_value.setIsOutlier(true, removeOutliers);
										removedPoints++;
										outlierIdentified = true;
									}
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
						}
					} while (outlierIdentified);
				}
			}
			if (removeOutliers)
				AttributeHelper.setToolTipText(node, "Outliers removed: " + removedPoints);
			else
				AttributeHelper.setToolTipText(node, "Outliers identified: " + removedPoints);
			if (removedPoints > 0)
				result.add(node);
		}
		return result;
	}
	
	private static List<MyComparableDataPoint> getValidValues(List<MyComparableDataPoint> valuesForSeries,
			String timePoint) {
		List<MyComparableDataPoint> values = new ArrayList<MyComparableDataPoint>();
		for (MyComparableDataPoint mcdp : valuesForSeries) {
			if (mcdp.timeUnitAndTime.equals(timePoint) && !mcdp.isOutlier())
				values.add(mcdp);
		}
		return values;
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		DoubleParameter ip = (DoubleParameter) params[0];
		alphavalue = ip.getDouble().doubleValue();
		BooleanParameter bp = (BooleanParameter) params[1];
		doRemoveOutliers = bp.getBoolean().booleanValue();
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (alphavalue < 0 || alphavalue > 1)
			throw new PreconditionException("Invalid alpha value (0..1 is valid)!");
		if (graph == null || graph.getNodes().size() <= 0)
			throw new PreconditionException("No graph available or graph empty!");
	}
	
	@Override
	public void reset() {
		super.reset();
	}
	
}
