/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.add_attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.CorrelationResult;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.MyXML_XYDataset;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.TabStatistics;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class AddInterestingAttributes extends AbstractAlgorithm {
	
	private boolean doSetDegree, doSetInDegree, doSetOutDegree, doSetClusteringCoeffUndir, doSetClusteringCoeffDir,
			doSetDataMappingCnt, doSetSampleCnt, doSetNumberOfSignificantDifferences,
			doSetNumberOfInSignificantDifferences, doSetAvgDataMappingValue, doSetMinimumSampleValue,
			doSetMaximumSampleValue, doSetAvgSampleStdDev, doSetMinimumSampleReplicateValue,
			doSetMaximumSampleReplicateValue, doSetMinimumValue, doSetMaximumValue, doSetTimePointCount, doSetLineCount,
			doCalcAlpha, doCalcBeta, doCalcRatio, doCalcLineCorr;
	
	private String seriesA, seriesB;
	
	@Override
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			return "Compute Properties";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return null;
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "With this command new attributes are added to the graph nodes and<br>"
				+ "edges, depending on the settings you choose below.<br><br>"
				+ "Uncheck any setting, to remove the result of prior calculations from the<br>"
				+ "working-set of nodes and edges.<br>"
				+ "Do not forget to perform this command again, as soon as new information<br>"
				+ "should be processed.<br><br>";
	}
	
	@Override
	public Parameter[] getParameters() {
		// degree, in- and out-degree
		// data mapping statistics (number of mappings and lines)
		// significant mean differences
		// sample statistics (cnt, min, max, avg)
		// data values statistics (min, max)
		// time series statistics (timepoint-count, (alpha, beta) - min, max, avg)
		
		ArrayList<String> allLines = new ArrayList<String>();
		HashSet<String> knownLines = new HashSet<String>();
		boolean hasMappingData = false;
		// TreeSet<String> seriesNames = new TreeSet<String>();
		for (GraphElement ge : getSelectedOrAllGraphElements()) {
			GraphElementHelper geh = new GraphElementHelper(ge);
			for (SubstanceInterface md : geh.getDataMappings()) {
				hasMappingData = true;
				for (ConditionInterface sd : md) {
					String s = sd.toString();
					if (!knownLines.contains(s)) {
						knownLines.add(s);
						allLines.add(s);
					}
				}
			}
		}
		// allLines.addAll(seriesNames);
		if (seriesA != null && allLines.contains(seriesA) && seriesB != null && allLines.contains(seriesB)) {
			// dont change this variables in this case.
		} else {
			if (allLines.size() >= 2) {
				seriesA = allLines.get(0);
				seriesB = allLines.get(1);
			} else {
				seriesA = null;
				seriesB = null;
			}
		}
		
		// allLines.clear();
		
		return new Parameter[] {
				new BooleanParameter(true, "<html>" + "Node Centralities<small><ul><li>degree, clustering coeff.", ""),
				hasMappingData ? new BooleanParameter(true, "<html>"
						+ "Mapping Statistics<small><ul><li>number of mappings and number of lines", "") : null,
				hasMappingData ? new BooleanParameter(true, "<html>" + "Significant Mean-Differences<br>"
						+ "(requires prior t- or U-test)<small><ul>"
						+ "<li>number of samples with (in)significant mean differences in comparison to control", "") : null,
				hasMappingData ? new BooleanParameter(true, "<html>" + "Sample Statistics<small><ul>"
						+ "<li>Number of samples" + "<li>Minimum/maximum/sum of sample averages"
						+ "<li>Average of sample averages" + "<li>Average of sample standard deviations"
						+ "<li>Minimum/maximum sample replicate count", "") : null,
				hasMappingData ? new BooleanParameter(true, "<html>" + "Replicate-Value Statistics<small><ul>"
						+ "<li>Minimum/maximum of all mapped values", "") : null,
				hasMappingData ? new BooleanParameter(true,
						"<html>" + "Time-Series Statistics<small><ul>" + "<li>Number of different time points"
								+ "<li>Linear regression: calculate &#945; and &#946; values", "") : null,
				allLines.size() >= 2 ? new BooleanParameter(true, "<html>"
						+ "Ratio and correlation calculation for selected lines<small><ul>" + "<li>"
						+ "For each time point the ratio of the sample values of the two selected<br>"
						+ "lines (A/B) is calculated. The individual ratios as well as the<br>"
						+ "min/max/avg ratios are calculated and stored.", "") : null,
				allLines.size() >= 2 ? new BooleanParameter(true, "<html><small><ul>" + "<li>"
						+ "For each substance the correlation (different types and settings) between<br>"
						+ "the data for the selected lines is calculated.", "") : null,
				allLines.size() >= 2 ? new ObjectListParameter(seriesA, "<html><small><ul><li>Line Selection: Line A",
						"",
						allLines) : null,
				allLines.size() >= 2 ? new ObjectListParameter(seriesB,
						"<html><small><ul><li>Line Selection: Line B", "",
						allLines) : null };
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int degree = 0;
		int mapstat = 1;
		int meandiff = 2;
		int samplestat = 3;
		int valuestat = 4;
		int timestat = 5;
		
		doSetDegree = ((BooleanParameter) params[degree]).getBoolean();
		doSetInDegree = ((BooleanParameter) params[degree]).getBoolean();
		doSetOutDegree = ((BooleanParameter) params[degree]).getBoolean();
		doSetClusteringCoeffUndir = ((BooleanParameter) params[degree]).getBoolean();
		doSetClusteringCoeffDir = ((BooleanParameter) params[degree]).getBoolean();
		if (params[1] == null) {
			doSetDataMappingCnt = false;
			doSetNumberOfSignificantDifferences = false;
			doSetNumberOfInSignificantDifferences = false;
			doSetSampleCnt = false;
			doSetMinimumSampleValue = false;
			doSetAvgDataMappingValue = false;
			doSetMaximumSampleValue = false;
			doSetAvgSampleStdDev = false;
			doSetMinimumSampleReplicateValue = false;
			doSetMaximumSampleReplicateValue = false;
			doSetMinimumValue = false;
			doSetMaximumValue = false;
			doSetTimePointCount = false;
			doSetLineCount = false;
			doCalcAlpha = false;
			doCalcBeta = false;
			doCalcRatio = false;
			doCalcLineCorr = false;
		} else {
			doSetDataMappingCnt = ((BooleanParameter) params[mapstat]).getBoolean();
			doSetNumberOfSignificantDifferences = ((BooleanParameter) params[meandiff]).getBoolean();
			doSetNumberOfInSignificantDifferences = ((BooleanParameter) params[meandiff]).getBoolean();
			doSetSampleCnt = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetMinimumSampleValue = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetAvgDataMappingValue = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetMaximumSampleValue = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetAvgSampleStdDev = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetMinimumSampleReplicateValue = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetMaximumSampleReplicateValue = ((BooleanParameter) params[samplestat]).getBoolean();
			doSetMinimumValue = ((BooleanParameter) params[valuestat]).getBoolean();
			doSetMaximumValue = ((BooleanParameter) params[valuestat]).getBoolean();
			doSetTimePointCount = ((BooleanParameter) params[timestat]).getBoolean();
			doSetLineCount = ((BooleanParameter) params[mapstat]).getBoolean();
			doCalcAlpha = ((BooleanParameter) params[timestat]).getBoolean();
			doCalcBeta = ((BooleanParameter) params[timestat]).getBoolean();
			int i = timestat + 1;
			if (params[i] != null) {
				doCalcRatio = ((BooleanParameter) params[i++]).getBoolean();
				doCalcLineCorr = ((BooleanParameter) params[i++]).getBoolean();
				seriesA = (String) ((ObjectListParameter) params[i++]).getValue();
				seriesB = (String) ((ObjectListParameter) params[i++]).getValue();
			} else {
				doCalcRatio = false;
				doCalcLineCorr = false;
			}
		}
	}
	
	@Override
	public void execute() {
		final Graph graph2 = graph;
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"Evaluate Properties", "");
		final Collection<GraphElement> worklist = getSelectedOrAllGraphElements();
		BackgroundTaskHelper.issueSimpleTask(getName(), "Please wait...", new Runnable() {
			@Override
			public void run() {
				try {
					status.setCurrentStatusText1("Process Data...");
					status.setCurrentStatusText2("Please wait");
					String folder = "properties";
					graph2.getListenerManager().transactionStarted(this);
					int sz = worklist.size();
					int i = 0;
					for (GraphElement ge : worklist) {
						status.setCurrentStatusText1("Processing Graph Element " + (i + 1) + "/" + sz);
						status.setCurrentStatusText2("Please wait");
						processDegreeAttribute(folder, ge, doSetDegree);
						processInDegreeAttribute(folder, ge, doSetInDegree);
						processOutDegreeAttribute(folder, ge, doSetOutDegree);
						processClusteringCoeffDirAttribute(folder, ge, doSetClusteringCoeffDir);
						processClusteringCoeffUndirAttribute(folder, ge, doSetClusteringCoeffUndir);
						processDataMappingCountAttribute(folder, ge, doSetDataMappingCnt);
						processSampleCountAttribute(folder, ge, doSetSampleCnt);
						processSignificantDifferencesCountAttribute(folder, ge, doSetNumberOfSignificantDifferences);
						processInSignificantDifferencesCountAttribute(folder, ge, doSetNumberOfInSignificantDifferences);
						processSampleMinimumValueAttribute(folder, ge, doSetMinimumSampleValue);
						processSampleAvgAndSumValueAttribute(folder, ge, doSetAvgDataMappingValue);
						processSampleMaximumValueAttribute(folder, ge, doSetMaximumSampleValue);
						processSampleAvgStdDevValueAttribute(folder, ge, doSetAvgSampleStdDev);
						processSampleMinimumReplicateCountValueAttribute(folder, ge, doSetMinimumSampleReplicateValue);
						processSampleMaximumReplicateCountValueAttribute(folder, ge, doSetMaximumSampleReplicateValue);
						processReplicatesMinimumValueAttribute(folder, ge, doSetMinimumValue);
						processReplicatesMaximumValueAttribute(folder, ge, doSetMaximumValue);
						processDifferentTimpointsCountAttribute(folder, ge, doSetTimePointCount);
						processDifferentLinesCountAttribute(folder, ge, doSetLineCount);
						try {
							calculateAlpha(folder, ge, doCalcAlpha);
							calculateBeta(folder, ge, doCalcBeta);
							calculateRatio(folder, ge, doCalcRatio);
							calculateLineCorr(folder, ge, doCalcLineCorr);
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
						i++;
						double d = (double) i / (double) sz * 100d;
						status.setCurrentStatusValueFine(d);
					}
					status.setCurrentStatusText1("Processing Finished");
					status.setCurrentStatusValueFine(100d);
				} finally {
					graph2.getListenerManager().transactionFinished(this, true);
				}
			}
		}, null, status);
	}
	
	private void processDegreeAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "degree");
			return;
		}
		if (ge instanceof Edge) {
			Edge e = (Edge) ge;
			if (e.getSource() == e.getTarget())
				setAttribute(ge, folder, "degree", new Integer(1));
			else
				setAttribute(ge, folder, "degree", new Integer(2));
		}
		if (ge instanceof Node) {
			Node n = (Node) ge;
			setAttribute(ge, folder, "degree", new Integer(n.getDegree()));
		}
	}
	
	private void processInDegreeAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "degree_in");
			return;
		}
		if (ge instanceof Node) {
			Node n = (Node) ge;
			setAttribute(ge, folder, "degree_in", new Integer(n.getInDegree()));
		}
	}
	
	private void processOutDegreeAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "degree_out");
			return;
		}
		if (ge instanceof Node) {
			Node n = (Node) ge;
			setAttribute(ge, folder, "degree_out", new Integer(n.getOutDegree()));
		}
	}
	
	private void processClusteringCoeffUndirAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "clustering_coeff_undir");
			return;
		}
		if (ge instanceof Node) {
			Node n = (Node) ge;
			Double res = GraphHelper.getClusteringCoefficientUndirected(n);
			if (res != null)
				setAttribute(ge, folder, "clustering_coeff_undir", res);
		}
	}
	
	private void processClusteringCoeffDirAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "clustering_coeff_dir");
			return;
		}
		if (ge instanceof Node) {
			Node n = (Node) ge;
			Double res = GraphHelper.getClusteringCoefficientDirected(n);
			if (res != null)
				setAttribute(ge, folder, "clustering_coeff_dir", res);
		}
	}
	
	private void processDataMappingCountAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "datamapping_cnt");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		setAttribute(ge, folder, "datamapping_cnt", new Integer(geh.getDataMappings().size()));
	}
	
	private void processSampleCountAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_cnt");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int samples = geh.getMappedSampleData().size();
		setAttribute(ge, folder, "sample_cnt", new Integer(samples));
	}
	
	private void processSignificantDifferencesCountAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "significant_different_cnt");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int cnt = 0;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					if (!mcdp.ttestIsReference)
						if (mcdp.ttestIsSignificantDifferent)
							cnt++;
				}
			}
		}
		setAttribute(ge, folder, "significant_different_cnt", new Integer(cnt));
	}
	
	private void processInSignificantDifferencesCountAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "significant_not_different_cnt");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int cnt = 0;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					if (!mcdp.ttestIsReference)
						if (!mcdp.ttestIsSignificantDifferent)
							cnt++;
				}
			}
		}
		setAttribute(ge, folder, "significant_not_different_cnt", new Integer(cnt));
	}
	
	private void processSampleMinimumValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_values_min");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		double min = Double.MAX_VALUE;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					if (mcdp.mean < min)
						min = mcdp.mean;
				}
			}
		}
		if (!(min < Double.MAX_VALUE))
			min = Double.NaN;
		setAttribute(ge, folder, "sample_values_min", new Double(min));
	}
	
	private void processSampleAvgAndSumValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_values_avg");
			AttributeHelper.deleteAttribute(ge, folder, "sample_values_sum");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		double sum = 0;
		int cnt = 0;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					sum += mcdp.mean;
					cnt++;
				}
			}
		}
		double avg;
		if (cnt <= 0)
			avg = Double.NaN;
		else
			avg = sum / cnt;
		setAttribute(ge, folder, "sample_values_avg", new Double(avg));
		setAttribute(ge, folder, "sample_values_sum", new Double(sum));
	}
	
	private void processSampleMaximumValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_values_max");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		double max = Double.NEGATIVE_INFINITY;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					if (mcdp.mean > max)
						max = mcdp.mean;
				}
			}
		}
		if (!(max > Double.NEGATIVE_INFINITY))
			max = Double.NaN;
		setAttribute(ge, folder, "sample_values_max", new Double(max));
	}
	
	private void processSampleAvgStdDevValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_stddev_avg");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		double sum = 0;
		int cnt = 0;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					sum += mcdp.getStddev();
					cnt++;
				}
			}
		}
		double avg = Double.NaN;
		if (cnt > 0)
			avg = sum / cnt;
		setAttribute(ge, folder, "sample_stddev_avg", new Double(avg));
	}
	
	private void processSampleMinimumReplicateCountValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_replicate_cnt_min");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int minReplCnt = Integer.MAX_VALUE;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					if (mcdp.getReplicateCount() < minReplCnt)
						minReplCnt = mcdp.getReplicateCount();
				}
			}
		}
		if (minReplCnt == Integer.MAX_VALUE)
			minReplCnt = 0;
		setAttribute(ge, folder, "sample_replicate_cnt_min", new Integer(minReplCnt));
	}
	
	private void processSampleMaximumReplicateCountValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_replicate_cnt_max");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int maxReplCnt = Integer.MIN_VALUE;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (MyComparableDataPoint mcdp : sd.getMeanMCDPs()) {
					if (mcdp.getReplicateCount() > maxReplCnt)
						maxReplCnt = mcdp.getReplicateCount();
				}
			}
		}
		if (maxReplCnt == Integer.MIN_VALUE)
			maxReplCnt = 0;
		setAttribute(ge, folder, "sample_replicate_cnt_max", new Integer(maxReplCnt));
	}
	
	private void processReplicatesMinimumValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_replicate_values_min");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		double min = Double.MAX_VALUE;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface co : md) {
				for (SampleInterface s : co) {
					for (NumericMeasurementInterface m : s) {
						if (m.getValue() < min)
							min = m.getValue();
					}
				}
			}
		}
		if (!(min < Double.MAX_VALUE))
			min = Double.NaN;
		setAttribute(ge, folder, "sample_replicate_values_min", new Double(min));
	}
	
	private void processReplicatesMaximumValueAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "sample_replicate_values_max");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		double max = Double.NEGATIVE_INFINITY;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface co : md) {
				for (SampleInterface s : co) {
					for (NumericMeasurementInterface m : s) {
						if (m.getValue() > max)
							max = m.getValue();
					}
				}
			}
		}
		if (!(max > Double.NEGATIVE_INFINITY))
			max = Double.NaN;
		setAttribute(ge, folder, "sample_replicate_values_max", new Double(max));
	}
	
	private void processDifferentTimpointsCountAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "samples_different_timepoints_cnt");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		HashSet<String> timePoints = new HashSet<String>();
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				for (SampleInterface s : sd) {
					timePoints.add(s.getSampleTime());
				}
			}
		}
		setAttribute(ge, folder, "samples_different_timepoints_cnt", new Integer(timePoints.size()));
	}
	
	private void processDifferentLinesCountAttribute(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "lines_cnt");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int cnt = 0;
		for (SubstanceInterface md : geh.getDataMappings()) {
			cnt += md.size();
		}
		setAttribute(ge, folder, "lines_cnt", new Integer(cnt));
	}
	
	private void calculateAlpha(String folder, GraphElement ge, boolean calc) {
		if (!calc) {
			AttributeHelper.deleteAttribute(ge, folder, "series_alpha_*");
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int cnt = 0;
		double sum = 0;
		double minAlpha = Double.MAX_VALUE;
		double maxAlpha = Double.NEGATIVE_INFINITY;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				double alpha = sd.calcAlpha();
				if (alpha < minAlpha)
					minAlpha = alpha;
				if (alpha > maxAlpha)
					maxAlpha = alpha;
				setAttribute(ge, folder, "series_alpha_" + cnt, new Double(alpha));
				sum += alpha;
				cnt++;
			}
		}
		double aa;
		if (cnt <= 0)
			aa = Double.NaN;
		else
			aa = sum / cnt;
		if (cnt > 1) {
			if (!Double.isNaN(aa))
				AttributeHelper.setAttribute(ge, folder, "series_alpha_avg", new Double(aa));
			if ((minAlpha < Double.MAX_VALUE))
				AttributeHelper.setAttribute(ge, folder, "series_alpha_min", new Double(minAlpha));
			if ((maxAlpha > Double.NEGATIVE_INFINITY))
				AttributeHelper.setAttribute(ge, folder, "series_alpha_max", new Double(maxAlpha));
		}
	}
	
	private void calculateBeta(String folder, GraphElement ge, boolean calc) {
		AttributeHelper.deleteAttribute(ge, folder, "series_beta_*");
		if (!calc) {
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		int cnt = 0;
		double sum = 0;
		double minBeta = Double.MAX_VALUE;
		double maxBeta = Double.NEGATIVE_INFINITY;
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				double beta = sd.calcBeta();
				if (beta < minBeta)
					minBeta = beta;
				if (beta > maxBeta)
					maxBeta = beta;
				setAttribute(ge, folder, "series_beta_" + cnt, new Double(beta));
				sum += beta;
				cnt++;
			}
		}
		double avgbeta;
		if (cnt <= 0)
			avgbeta = Double.NaN;
		else
			avgbeta = sum / cnt;
		if (cnt > 0) {
			if (!Double.isNaN(avgbeta))
				AttributeHelper.setAttribute(ge, folder, "series_beta_avg", new Double(avgbeta));
			if ((minBeta < Double.MAX_VALUE))
				AttributeHelper.setAttribute(ge, folder, "series_beta_min", new Double(minBeta));
			if ((maxBeta > Double.NEGATIVE_INFINITY))
				AttributeHelper.setAttribute(ge, folder, "series_beta_max", new Double(maxBeta));
		}
	}
	
	private void calculateRatio(String folder, GraphElement ge, boolean calc) {
		AttributeHelper.deleteAttribute(ge, folder, "sample_ratio_*");
		if (!calc) {
			return;
		}
		GraphElementHelper geh = new GraphElementHelper(ge);
		HashMap<Integer, Double> timePoint2avgA = new HashMap<Integer, Double>();
		HashMap<Integer, Double> timePoint2avgB = new HashMap<Integer, Double>();
		for (SubstanceInterface md : geh.getDataMappings()) {
			for (ConditionInterface sd : md) {
				boolean isA = false;
				if (sd.toString().equals(seriesA))
					isA = true;
				else
					isA = false;
				if (!isA && !sd.toString().equals(seriesB))
					continue;
				ArrayList<Double> mv = sd.getMeanValues();
				ArrayList<Integer> mvtimes = sd.getMeanTimePoints();
				for (int i = 0; i < mvtimes.size(); i++) {
					if (isA)
						timePoint2avgA.put(mvtimes.get(i), mv.get(i));
					else
						timePoint2avgB.put(mvtimes.get(i), mv.get(i));
				}
			}
		}
		double sum = 0;
		int cnt = 0;
		double minRatio = Double.MAX_VALUE;
		double maxRatio = Double.NEGATIVE_INFINITY;
		setAttribute(ge, folder, "sample_ratio_name_a", seriesA);
		setAttribute(ge, folder, "sample_ratio_name_b", seriesB);
		for (Integer time : timePoint2avgA.keySet()) {
			Double a = timePoint2avgA.get(time);
			Double b = timePoint2avgB.get(time);
			if (a != null && b != null) {
				double ratio = a / b;
				if (ratio < minRatio)
					minRatio = ratio;
				if (ratio > maxRatio)
					maxRatio = ratio;
				if (time.intValue() >= 0) {
					String tt = getZeros(time.intValue(), 3);
					setAttribute(ge, folder, "sample_ratio_" + tt, new Double(ratio));
				} else
					setAttribute(ge, folder, "sample_ratio_", new Double(ratio));
				sum += ratio;
				cnt++;
			}
		}
		if (cnt > 0) {
			setAttribute(ge, folder, "sample_ratio_avg", new Double(sum / cnt));
			if ((minRatio < Double.MAX_VALUE))
				AttributeHelper.setAttribute(ge, folder, "sample_ratio_min", new Double(minRatio));
			if ((maxRatio > Double.NEGATIVE_INFINITY))
				AttributeHelper.setAttribute(ge, folder, "sample_ratio_max", new Double(maxRatio));
			
		}
	}
	
	private void calculateLineCorr(String folder, GraphElement ge, boolean calc) {
		AttributeHelper.deleteAttribute(ge, folder, "corr_*");
		if (!calc) {
			return;
		}
		MyXML_XYDataset datasetAvg = new MyXML_XYDataset();
		MyXML_XYDataset datasetVal = new MyXML_XYDataset();
		
		Iterable<SubstanceInterface> mappedDataList = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge);
		if (mappedDataList != null) {
			SubstanceInterface xmldata1 = null;
			SubstanceInterface xmldata2 = null;
			int series = 0;
			for (SubstanceInterface xmldata : mappedDataList) {
				series++;
				Collection<ConditionInterface> lines = xmldata;
				// iterate lines
				for (ConditionInterface c : lines) {
					String seriesName = c.getExpAndConditionName();
					if (seriesName.equals(seriesA))
						xmldata1 = xmldata;
					if (seriesName.equals(seriesB))
						xmldata2 = xmldata;
					if (xmldata1 != null && xmldata2 != null)
						break;
				}
				if (xmldata1 != null && xmldata2 != null)
					break;
			}
			if (xmldata1 != null && xmldata2 != null) {
				datasetAvg.addXmlDataSeriesXY(xmldata1, xmldata2, seriesA + " - " + seriesB, true);
				datasetVal.addXmlDataSeriesXY(xmldata1, xmldata2, seriesA + " - " + seriesB, false);
			}
		}
		CorrelationResult cr1 = TabStatistics.calculateCorrelation(datasetAvg, seriesA, seriesB, false, 0, 0.05, false);
		// String hist1 = cr1.getCalculationHistoryForMaxR();
		CorrelationResult cr2 = TabStatistics.calculateCorrelation(datasetVal, seriesA, seriesB, false, 0, 0.05, false);
		// String hist2 = cr2.getCalculationHistoryForMaxR();
		CorrelationResult cr3 = TabStatistics.calculateCorrelation(datasetAvg, seriesA, seriesB, false, 0, 0.05, true);
		// String hist3 = cr3.getCalculationHistoryForMaxR();
		CorrelationResult cr4 = TabStatistics.calculateCorrelation(datasetVal, seriesA, seriesB, false, 0, 0.05, true);
		// String hist4 = cr4.getCalculationHistoryForMaxR();
		// AttributeHelper.setToolTipText(ge,
		// "<html><small><small>"+hist1+"<hr>"+hist2+"<hr>"+hist3+"<hr>"+hist4);
		setAttribute(ge, folder, "corr_sample_avg_over_time_r", cr1.getMaxR());
		setAttribute(ge, folder, "corr_sample_avg_over_time_prob", cr1.getMaxTrueCorrProb());
		setAttribute(ge, folder, "corr_repl_values_r", cr2.getMaxR());
		setAttribute(ge, folder, "corr_repl_values_prob", cr2.getMaxTrueCorrProb());
		setAttribute(ge, folder, "corr_rank_sample_avg_over_time_r", cr3.getMaxR());
		setAttribute(ge, folder, "corr_rank_sample_avg_over_time_prob", cr3.getMaxTrueCorrProb());
		setAttribute(ge, folder, "corr_rank_repl_values_r", cr4.getMaxR());
		setAttribute(ge, folder, "corr_rank_repl_values_prob", cr4.getMaxTrueCorrProb());
	}
	
	private void setAttribute(GraphElement ge, String folder, String name, Double value) {
		if (value != null && !Double.isNaN(value))
			AttributeHelper.setAttribute(ge, folder, name, value);
	}
	
	private void setAttribute(GraphElement ge, String folder, String name, Float value) {
		if (value != null && !Float.isNaN(value))
			AttributeHelper.setAttribute(ge, folder, name, value);
	}
	
	private void setAttribute(GraphElement ge, String folder, String name, String value) {
		if (value != null)
			AttributeHelper.setAttribute(ge, folder, name, value);
	}
	
	private void setAttribute(GraphElement ge, String folder, String name, Integer value) {
		if (value != null)
			AttributeHelper.setAttribute(ge, folder, name, value);
	}
	
	private String getZeros(int value, int len) {
		String res = value + "";
		if (value >= 0) {
			while (res.length() < len)
				res = "0" + res;
		}
		return res;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
