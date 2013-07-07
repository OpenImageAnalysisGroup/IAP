/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JColorChooser;
import javax.swing.JDialog;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.color.ColorUtil;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.TabStatistics;

public class ColorizeAlgorithm extends AbstractAlgorithm {
	
	private double userMinValue = Double.NaN;
	private double userMaxValue = Double.NaN;
	private boolean useUserMinMax = false;
	private boolean useRatio = false;
	private Color minColor = Color.WHITE;
	private Color maxColor = Color.RED;
	private double gamma = 1d;
	
	@Override
	public String getName() {
		return null; // "Average Substance-Level > Background Color";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "With this algorithm the selected nodes will be colorized accordingly<br>"
				+ "to the average of the mapped measurement values.<br>" + "<br>"
				+ "The minimum and maximum value will be determined by the minimum<br>"
				+ "and maximum of the average measurement for the selected nodes.<br>" + "<br>"
				+ "<small>For details on this command open the documentation (? Button).<br><br>"
				+ "The colors for the minimum and maximum values will be specified<br>"
				+ "separately, after choosing OK.<br>" + "<br>"
				+ "The diagrams, shown inside each node will be hidden after performing<br>" + "this command.<br>"
				+ "Use the node-sidepanel, to re-enable the diagram view, if needed!";
	}
	
	@Override
	public void execute() {
		JDialog dialog;
		final FinalBoolean set = new FinalBoolean(false);
		final JColorChooser colorChooser = new JColorChooser(minColor);
		dialog = JColorChooser.createDialog(MainFrame.getInstance(), "Target Color for Minimum Value", true,
				colorChooser, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						minColor = colorChooser.getColor();
						set.set(true);
					}
				}, null);
		dialog.setVisible(true);
		if (!set.isSet())
			return;
		final JColorChooser colorChooserMax = new JColorChooser(maxColor);
		dialog = JColorChooser.createDialog(MainFrame.getInstance(), "Target Color for Maximum Value", true,
				colorChooserMax, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						maxColor = colorChooserMax.getColor();
						set.set(true);
					}
				}, null);
		dialog.setVisible(true);
		if (!set.isSet())
			return;
		
		graph.getListenerManager().transactionStarted(this);
		try {
			double min = Double.NaN;
			double max = Double.NaN;
			if (useUserMinMax) {
				min = userMinValue;
				max = userMaxValue;
			}
			colorizeNodes(GraphHelper.getSelectedOrAllNodes(selection, graph), minColor, maxColor, gamma, min, max,
					useRatio);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
				new DoubleParameter(gamma, "Gamma (1..n)", "Gamma (>=1), use 1 to disable gamma correction."),
				new BooleanParameter(useUserMinMax, "<html>Disable Autoscaling?<br>"
						+ "<small><small><font color=\"gray\">" + "(if the autoscaling is disabled the "
						+ "user min./max values will be used)",
						"If selected, the user provided minimum/maximum values are used"),
				new DoubleParameter(userMinValue, "User Minimum Value", "User provided minimum value"),
				new DoubleParameter(userMaxValue, "User Maximum Value", "User provided maximum value"),
				new BooleanParameter(useRatio, "<html>Use ratio instead of average value?<br>"
						+ "<small><small><font color=\"gray\">"
						+ "(if selected, the average ratio from sample to sample will be used instead "
						+ "of the average sample value)",
						"If selected, the average ratio from sample to sample instead of the average sample value will be used"), };
	}
	
	public static void colorizeNodes(List<Node> nodes, Color minC, Color maxC, double gamma, double min, double max,
			boolean useRatioCalculation) {
		double minimum = Double.MAX_VALUE;
		double maximum = Double.NEGATIVE_INFINITY;
		if (Double.isNaN(min) || Double.isNaN(max)) {
			for (Node node : nodes) {
				try {
					CollectionAttribute ca = (CollectionAttribute) node.getAttribute(Experiment2GraphHelper.mapFolder);
					XMLAttribute xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
					List<MyComparableDataPoint> allvalues = new ArrayList<MyComparableDataPoint>();
					for (SubstanceInterface xmldata : xa.getMappedData()) {
						List<MyComparableDataPoint> mapvalues = NodeTools.getSortedAverageDataSetValues(xmldata, null);
						allvalues.addAll(mapvalues);
					}
					if (allvalues != null
							&& ((!useRatioCalculation && allvalues.size() > 0) || (useRatioCalculation && allvalues.size() > 1))) {
						double sum = 0d;
						double sumratio = 0d;
						boolean first = true;
						MyComparableDataPoint last_mcdp = null;
						for (MyComparableDataPoint mcdp : allvalues) {
							sum += mcdp.mean;
							if (!first) {
								sumratio += last_mcdp.mean / mcdp.mean;
							}
							last_mcdp = mcdp;
							first = false;
						}
						double avg;
						if (!useRatioCalculation)
							avg = sum / allvalues.size();
						else
							avg = sumratio / (allvalues.size() - 1);
						if (avg < minimum)
							minimum = avg;
						if (avg > maximum)
							maximum = avg;
					}
				} catch (AttributeNotFoundException anfe) {
					// emtpy
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		if (minimum == Double.MAX_VALUE || maximum == Double.NEGATIVE_INFINITY) {
			MainFrame.showMessageDialog("<html>Minimum and/or maximum average sample values could not be determined!<br>"
					+ "Select graph nodes with mapped measurement data.", "Missing sample data");
		}
		if (Math.abs(maximum - minimum) < 0.000000000001d) {
			MainFrame.showMessageDialog(
					"<html>The minimum sample value equals the maximum value, node coloring can not proceed.<br>"
							+ "Please select a number of nodes with non-constant average sample values.",
					"Missing sample data");
		} else {
			for (Node node : nodes) {
				try {
					CollectionAttribute ca = (CollectionAttribute) node.getAttribute(Experiment2GraphHelper.mapFolder);
					XMLAttribute xa = (XMLAttribute) ca.getAttribute(Experiment2GraphHelper.mapVarName);
					List<MyComparableDataPoint> allvalues = new ArrayList<MyComparableDataPoint>();;
					for (SubstanceInterface xmldata : xa.getMappedData()) {
						List<MyComparableDataPoint> mapvalues = NodeTools.getSortedAverageDataSetValues(xmldata, null);
						allvalues.addAll(mapvalues);
					}
					if (allvalues != null && allvalues.size() > 0) {
						double sum = 0;
						double sumratio = 0d;
						boolean first = true;
						MyComparableDataPoint last_mcdp = null;
						for (MyComparableDataPoint mcdp : allvalues) {
							sum += mcdp.mean;
							if (!first) {
								sumratio += last_mcdp.mean / mcdp.mean;
							}
							last_mcdp = mcdp;
							first = false;
						}
						double avg;
						if (!useRatioCalculation)
							avg = sum / allvalues.size();
						else
							avg = sumratio / (allvalues.size() - 1);
						if (avg < minimum)
							avg = minimum;
						if (avg > maximum)
							avg = maximum;
						Double avg_scaled = (avg - minimum) / (maximum - minimum);
						// System.out.println("AVG: "+avg_scaled+" [0..1 < "+minimum+".."+maximum+"]");
						Color c = TabStatistics.getRcolor(avg_scaled.floatValue(), gamma, Color.BLACK, minC, maxC);
						AttributeHelper.setFillColor(node, c);
						NodeTools.setNodeComponentType(node, GraffitiCharts.HIDDEN.getName());
						AttributeHelper.setToolTipText(node, (useRatioCalculation ? "Ratio: " : "Avg: ") + avg);
					}
				} catch (AttributeNotFoundException anfe) {
					// empty
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
			MainFrame.showMessage("<html>Selected nodes are now colored according to the average of the "
					+ "mapped average sample values.<br>" + "<font color=\"" + ColorUtil.getHexFromColor(minC)
					+ "\"><b>Color Min</b></font> for samples &lt;= " + StringManipulationTools.formatNumber(minimum, "#.####")
					+ ", <font color=\"" + ColorUtil.getHexFromColor(maxC) + "\">Color Max</b></font> for samples &gt;= "
					+ StringManipulationTools.formatNumber(maximum, "#.####") + ", "
					+ " values inbetween are colored with a linear gradient.", MessageType.INFO);
			// if (nodes.size()>0)
			// GraphHelper.issueCompleteRedrawForGraph(nodes.iterator().next().getGraph());
		}
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		DoubleParameter dpgamma = (DoubleParameter) params[i++];
		BooleanParameter bp = (BooleanParameter) params[i++];
		useUserMinMax = bp.getBoolean().booleanValue();
		DoubleParameter dpmin = (DoubleParameter) params[i++];
		DoubleParameter dpmax = (DoubleParameter) params[i++];
		BooleanParameter ur = (BooleanParameter) params[i++];
		userMinValue = dpmin.getDouble().doubleValue();
		userMaxValue = dpmax.getDouble().doubleValue();
		gamma = dpgamma.getDouble().doubleValue();
		useRatio = ur.getBoolean().booleanValue();
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null || graph.getNodes().size() <= 0)
			throw new PreconditionException("No graph available or graph empty!");
		if (graph == null || graph.getNodes().size() <= 1)
			throw new PreconditionException("More than one node with mapped measurement data needs to be selected!");
	}
	
	@Override
	public void reset() {
		super.reset();
	}
	
}
