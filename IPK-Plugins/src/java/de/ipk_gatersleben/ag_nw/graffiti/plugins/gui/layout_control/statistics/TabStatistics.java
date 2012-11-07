/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import info.clearthought.layout.SingleFiledLayout;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.StringManipulationTools;
import org.SystemInfo;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.inspector.ContainsTabbedPane;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.CategoryItemRenderer;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute.XMLAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.TtestInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 * @version $Revision: 1.13 $
 */
public class TabStatistics extends InspectorTab implements ActionListener,
		ContainsTabbedPane {
	private static final long serialVersionUID = 1L;
	
	private Component lastScatterPlot = null;
	
	private JComponent placeForScatter;
	
	private JSlider gammaSlider1vis;
	private JSlider gammaSlider2scatter;
	private JSlider gammaSlider3edgeCorr;
	
	private boolean plotAverage = false;
	private boolean mergeDataset = true;
	private boolean rankOrder = false;
	private boolean showStatusResult = true;
	
	/**
	 * If set to false, the t-test/U-test will be performed 3 times with 3
	 * different alpha levels (5,1,01%), if set to true, one specified alpha
	 * level (<code>alpha</code>) will be used.
	 */
	private boolean alphaSpecified = true;
	
	private final ArrayList<Edge> correlationEdges = new ArrayList<Edge>();
	
	private double prob = 0.95;
	private double minimumR = 0;
	
	private int sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 2;
	
	private double alpha = 0.05;
	private double ratioL = 0.8;
	private double ratioU = 1.2;
	private boolean colorCodeEdgesWithCorrelationValue = true;
	
	private Color colR_1 = Color.RED;
	private Color colR0 = Color.WHITE;
	private Color colR1 = Color.BLUE;
	
	JCheckBox checkBoxPlotAverage1;
	JCheckBox checkBoxPlotAverage2;
	JCheckBox checkBoxPlotAverage3;
	
	JCheckBox addStatusText1 = new JCheckBox(
			"<html>Add calculation details to node status");
	JCheckBox addStatusText2 = new JCheckBox(
			"<html>Add calculation details to edge status");
	JCheckBox addStatusText3 = new JCheckBox(
			"<html>Add calculation details to node status");
	JCheckBox onlyUpdateExistingEdges = new JCheckBox(
			"<html>Update existing edges, disable edge-creation");
	
	private JTextField jTextFieldAlpha;
	
	private JTextField jTextFieldProb1findCorr;
	private JTextField jTextFieldProb2visCorr;
	private JTextField jTextFieldProb3scatter;
	private JTextField jTextFieldMinR1;
	private JTextField jTextFieldMinR2;
	private JTextField jTextFieldMinR3;
	
	JButton doTest;
	JButton resetColorAndBorder;
	JButton removeCorrelationEdges;
	JButton selectCorrelationEdges;
	
	JButton findCorrButton;
	
	JButton visCorrButton;
	
	JButton doScatterPlotButton;
	
	JTabbedPane stat = null;
	
	String referenceSelection;
	HashSet<String> validConditions;
	
	ArrayList<JButton> col1buttons = new ArrayList<JButton>();
	ArrayList<JButton> col2buttons = new ArrayList<JButton>();
	ArrayList<JButton> col3buttons = new ArrayList<JButton>();
	
	private int currGammaValue = 1;
	
	private boolean considerTimeShifts = false;
	
	private boolean dontAddNewEdgesUpdateOld = false;
	
	private boolean showRangeAxis = false;
	private boolean tickMarksVisible = false;
	private boolean showLegend = false;
	private float outlineBorderWidth = 10f;
	
	/**
	 * Initialize GUI
	 */
	private void initComponents() {
		stat = new JTabbedPane();
		// stat.setTabPlacement(JTabbedPane.RIGHT);
		// stat.addTab(null, new VTextIcon(stat, "Compare Samples",
		// VTextIcon.ROTATE_RIGHT), getStudentPanel());
		// stat.addTab(null, new VTextIcon(stat, "Scatter Matrix",
		// VTextIcon.ROTATE_RIGHT), getPlotPanel());
		// stat.addTab(null, new VTextIcon(stat, "Correlate 1:n",
		// VTextIcon.ROTATE_RIGHT), getInteractiveAnalysisPanel());
		// stat.addTab(null, new VTextIcon(stat, "Correlate n:n",
		// VTextIcon.ROTATE_RIGHT), getAnalysisPanel());
		stat.addTab("<html><small>Compare Samples", getStudentPanel());
		stat.addTab("<html><small>Scatter Matrix", getPlotPanel());
		stat.addTab("<html><small>Correlate 1:n", getAnalysisPanelOneToN());
		stat.addTab("<html><small>Correlate n:n", getAnalysisPanel());
		stat.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				refreshEditComponents();
			}
		});
		double border = 0;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		this.setLayout(new TableLayout(size));
		this.add(stat, "1,1");
		this.validate();
	}
	
	@Override
	public JTabbedPane getTabbedPane() {
		return stat;
	}
	
	private JComponent getAnalysisPanel() {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN,
				SingleFiledLayout.FULL, 0));
		int border = 5;
		result.setBorder(BorderFactory.createEmptyBorder(border, border,
				border, border));
		findCorrButton = new JMButton("<html>Find Significant Correlations");
		findCorrButton.setOpaque(false);
		findCorrButton.addActionListener(this);
		
		removeCorrelationEdges = new JMButton("<html><small>Remove Edges");
		removeCorrelationEdges.addActionListener(this);
		removeCorrelationEdges.setOpaque(false);
		
		selectCorrelationEdges = new JMButton("<html><small>Select Edges");
		selectCorrelationEdges.addActionListener(this);
		selectCorrelationEdges.setOpaque(false);
		
		result.add(TableLayout.getSplit(findCorrButton, TableLayout
				.getSplitVertical(removeCorrelationEdges,
						selectCorrelationEdges, TableLayout.PREFERRED,
						TableLayout.PREFERRED), TableLayout.FILL,
				TableLayout.PREFERRED), "1,1");
		
		FolderPanel fp = new FolderPanel("Calculation Settings", false, true,
				false, JLabelJavaHelpLink.getHelpActionListener("stat_corr"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);
		
		checkBoxPlotAverage1 = new JCheckBox(
				"<html>Use average values<br><small>(recommended for time series data with few replicates per time point)",
				plotAverage);
		checkBoxPlotAverage1.setOpaque(false);
		checkBoxPlotAverage1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				plotAverage = src.isSelected();
			}
		});
		checkBoxPlotAverage1.setSelected(plotAverage);
		JComponent mergeOptionEditor = getMergeOptionEditor(1);
		
		final JCheckBox checkBoxFindTimeShifts = new JCheckBox(
				"Find time-shifted (index -3..3) correlations",
				considerTimeShifts);
		checkBoxFindTimeShifts.setOpaque(false);
		checkBoxFindTimeShifts.setSelected(considerTimeShifts);
		checkBoxFindTimeShifts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				considerTimeShifts = src.isSelected();
			}
		});
		
		jTextFieldProb1findCorr = new JTextField(new Double(prob).toString());
		jTextFieldMinR1 = new JTextField(new Double(minimumR).toString());
		
		JComponent corrType = getCorrelationTypeEditor(1);
		
		JComponent panelProb = getProbabilitySettingPanel(
				jTextFieldProb1findCorr, jTextFieldMinR1, corrType);
		
		final JComponent colPanel = getNewColorPanel();
		JCheckBox colorCodeEdgesCorrelation = new JCheckBox(
				"Change edge color dependent on correlation:");
		
		colorCodeEdgesCorrelation.setOpaque(false);
		
		colorCodeEdgesCorrelation
				.setSelected(colorCodeEdgesWithCorrelationValue);
		checkColPanel(colPanel, colorCodeEdgesWithCorrelationValue);
		colorCodeEdgesCorrelation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				colorCodeEdgesWithCorrelationValue = ((JCheckBox) e.getSource())
						.isSelected();
				checkColPanel(colPanel, colorCodeEdgesWithCorrelationValue);
			}
		});
		JLabel descGammaLabel = new JLabel();
		gammaSlider3edgeCorr = getNewGammaSlider(descGammaLabel);
		
		fp.addComp(checkBoxPlotAverage1);
		fp.addComp(checkBoxFindTimeShifts);
		fp.addComp(mergeOptionEditor);
		fp.addComp(panelProb);
		
		FolderPanel fp2 = new FolderPanel("Visualization Settings", false,
				true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);
		
		addStatusText2.setOpaque(false);
		addStatusText2.setSelected(showStatusResult);
		addStatusText2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showStatusResult = addStatusText2.isSelected();
				addStatusText1.setSelected(showStatusResult);
				addStatusText3.setSelected(showStatusResult);
			}
		});
		
		JButton clearStatus = new JMButton(
				"<html><small>Clear Edge-<br>Status Text");
		clearStatus.setOpaque(false);
		clearStatus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					List<Node> nodes = GraphHelper
							.getSelectedOrAllNodes(MainFrame.getInstance()
									.getActiveEditorSession());
					for (Node n : nodes) {
						for (Edge edge : n.getEdges())
							AttributeHelper.setToolTipText(edge, "");
					}
					if (correlationEdges != null && correlationEdges.size() > 0)
						for (Edge edge : correlationEdges)
							AttributeHelper.setToolTipText(edge, "");
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog(
							"No active graph editor window found!", "Error");
				}
			}
		});
		fp2.addComp(TableLayout.get3Split(addStatusText2, new JLabel(""),
				clearStatus, TableLayout.FILL, 5, TableLayout.PREFERRED));
		
		onlyUpdateExistingEdges.setOpaque(false);
		onlyUpdateExistingEdges.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (onlyUpdateExistingEdges.isSelected()) {
					checkBoxFindTimeShifts.setSelected(false);
					checkBoxFindTimeShifts.setEnabled(false);
					dontAddNewEdgesUpdateOld = true;
				} else {
					checkBoxFindTimeShifts.setEnabled(true);
					checkBoxFindTimeShifts.setSelected(considerTimeShifts);
					dontAddNewEdgesUpdateOld = false;
				}
			}
		});
		
		fp2.addComp(onlyUpdateExistingEdges);
		
		fp2.addComp(colorCodeEdgesCorrelation);
		fp2.addComp(colPanel);
		fp2.addComp(TableLayout.getSplit(descGammaLabel, gammaSlider3edgeCorr,
				TableLayout.PREFERRED, TableLayout.FILL));
		
		fp.layoutRows();
		fp2.layoutRows();
		
		result.add(fp.getBorderedComponent(5, 0, 0, 0));
		result.add(fp2.getBorderedComponent(5, 0, 0, 0));
		
		result.validate();
		return result;
	}
	
	HashMap<Integer, ButtonGroup> datasetButtonGroups = new HashMap<Integer, ButtonGroup>();
	
	private JComponent getMergeOptionEditor(Integer i) {
		
		JRadioButton completeButton = new JRadioButton(
				"All substance values in one step");
		JRadioButton individualButton = new JRadioButton(
				"Each plant/genotype individually");
		completeButton.setSelected(mergeDataset == true);
		individualButton.setSelected(mergeDataset == false);
		completeButton.setOpaque(false);
		individualButton.setOpaque(false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(completeButton);
		bg.add(individualButton);
		
		datasetButtonGroups.put(i, bg);
		
		completeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mergeDataset = true;
				for (ButtonGroup b : datasetButtonGroups.values()) {
					b.getElements().nextElement().setSelected(true);
				}
			}
		});
		individualButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mergeDataset = false;
				for (ButtonGroup b : datasetButtonGroups.values()) {
					Enumeration<AbstractButton> en = b.getElements();
					Object o = en.nextElement();
					if (o != null)
						en.nextElement().setSelected(true);
				}
			}
		});
		
		JComponent resultPanel = TableLayout.getSplitVertical(completeButton,
				individualButton, TableLayout.PREFERRED, TableLayout.PREFERRED);
		
		resultPanel.setOpaque(false);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Correlate"));
		return resultPanel;
	}
	
	HashMap<Integer, ButtonGroup> correlationTypeButtonGroups = new HashMap<Integer, ButtonGroup>();
	
	private JComponent getCorrelationTypeEditor(Integer i) {
		
		JRadioButton pearsonButton = new JRadioButton(
				"Pearson's product-moment correlation");
		JRadioButton spearmanButton = new JRadioButton(
				"Spearman's rank correlation");
		JRadioButton quadrantButton = new JRadioButton("Quadrant correlation");
		JRadioButton kendallButton = new JRadioButton("Kendall's correlation");
		pearsonButton.setSelected(rankOrder == false);
		spearmanButton.setSelected(rankOrder == true);
		pearsonButton.setOpaque(false);
		spearmanButton.setOpaque(false);
		quadrantButton.setOpaque(false);
		kendallButton.setOpaque(false);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(pearsonButton);
		bg.add(spearmanButton);
		bg.add(quadrantButton);
		bg.add(kendallButton);
		
		correlationTypeButtonGroups.put(i, bg);
		
		pearsonButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rankOrder = false;
				for (ButtonGroup b : correlationTypeButtonGroups.values()) {
					b.getElements().nextElement().setSelected(true);
				}
			}
		});
		spearmanButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rankOrder = true;
				for (ButtonGroup b : correlationTypeButtonGroups.values()) {
					Enumeration<AbstractButton> en = b.getElements();
					Object o = en.nextElement();
					if (o != null)
						en.nextElement().setSelected(true);
				}
			}
		});
		
		quadrantButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("Not yet implemented!", "Error");
			}
		});
		kendallButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("Not yet implemented!", "Error");
			}
		});
		
		JComponent resultPanel = TableLayout.getSplitVertical(TableLayout
				.getSplitVertical(pearsonButton, spearmanButton,
						TableLayout.PREFERRED, TableLayout.PREFERRED),
				/*
				 * TableLayout.getSplitVertical( quadrantButton, kendallButton,
				 * TableLayout.PREFERRED, TableLayout.PREFERRED)
				 */null, TableLayout.PREFERRED, TableLayout.PREFERRED);
		
		resultPanel.setOpaque(false);
		resultPanel.setBorder(BorderFactory.createTitledBorder("Calculate"));
		return resultPanel;
	}
	
	private void checkColPanel(Component colPanel, boolean enabled) {
		JPanel jp = (JPanel) colPanel;
		for (int i = 0; i < jp.getComponentCount(); i++) {
			Object o = jp.getComponent(i);
			if (o instanceof JButton) {
				JButton jb = (JButton) o;
				jb.setEnabled(enabled);
			}
		}
	}
	
	private JComponent getProbabilitySettingPanel(JTextField textFieldProb,
			JTextField textFieldMinR, JComponent rank) {
		JLabel l2 = new JLabel("Significance >=");
		JLabel l3 = new JLabel("and |r| >=");
		
		l2.setHorizontalAlignment(JLabel.CENTER);
		l3.setHorizontalAlignment(JLabel.CENTER);
		
		JComponent c2 = TableLayout.getSplitVertical(l2, textFieldProb,
				TableLayout.FILL, TableLayout.FILL);
		c2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		
		return TableLayout
				.getSplitVertical(rank, TableLayout.getSplit(c2, TableLayout
						.getSplitVertical(l3, textFieldMinR, TableLayout.FILL,
								TableLayout.FILL), TableLayout.FILL,
						TableLayout.FILL), TableLayout.PREFERRED,
						TableLayout.PREFERRED);
	}
	
	private JComponent getAnalysisPanelOneToN() {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED, border } }; // Rows
		result.setLayout(new TableLayout(size));
		
		visCorrButton = new JMButton(
				"<html>Calculate and visualize correlations");
		visCorrButton.addActionListener(this);
		visCorrButton.setOpaque(false);
		
		resetColorAndBorder = new JMButton(
				"<html>Reset Node/Edge-<br>Color/Border");
		resetColorAndBorder.addActionListener(this);
		resetColorAndBorder.setOpaque(false);
		
		result.add(TableLayout.getSplit(visCorrButton, resetColorAndBorder,
				TableLayout.FILL, TableLayout.PREFERRED), "1,1");
		
		FolderPanel fp = new FolderPanel("Calculation Settings", false, true,
				false, JLabelJavaHelpLink.getHelpActionListener("stat_vis"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);
		
		checkBoxPlotAverage2 = new JCheckBox(
				"<html>Use average values<br>"
						+ "<small>(recommended for time series data with few replicates per time point)",
				plotAverage);
		checkBoxPlotAverage2.setOpaque(false);
		checkBoxPlotAverage2.setSelected(plotAverage);
		checkBoxPlotAverage2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				plotAverage = src.isSelected();
			}
		});
		
		JComponent mergeEditor = getMergeOptionEditor(2);
		
		fp.addComp(TableLayout.getSplitVertical(checkBoxPlotAverage2,
				mergeEditor, TableLayout.PREFERRED, TableLayout.PREFERRED));
		jTextFieldProb2visCorr = new JTextField(new Double(prob).toString());
		jTextFieldMinR2 = new JTextField(new Double(minimumR).toString());
		JComponent corrType = getCorrelationTypeEditor(2);
		JComponent panelProb = getProbabilitySettingPanel(
				jTextFieldProb2visCorr, jTextFieldMinR2, corrType);
		fp.addComp(panelProb);
		
		FolderPanel fp2 = new FolderPanel("Visualization Settings", false,
				true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);
		
		addStatusText3.setOpaque(false);
		addStatusText3.setSelected(showStatusResult);
		addStatusText3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showStatusResult = addStatusText3.isSelected();
				addStatusText1.setSelected(showStatusResult);
				addStatusText2.setSelected(showStatusResult);
			}
		});
		
		JButton clearStatus = new JMButton("<html><small>Clear<br>Status Text");
		clearStatus.setOpaque(false);
		clearStatus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<GraphElement> graphElements = GraphHelper
							.getSelectedOrAllGraphElements();
					for (GraphElement ge : graphElements)
						AttributeHelper.setToolTipText(ge, "");
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog(
							"No active graph editor window found!", "Error");
				}
			}
		});
		fp2.addComp(TableLayout.get3Split(addStatusText3, new JLabel(""),
				clearStatus, TableLayout.FILL, 5, TableLayout.PREFERRED));
		
		fp2.addComp(getNewColorPanel());
		JLabel descGammaLabel = new JLabel();
		gammaSlider1vis = getNewGammaSlider(descGammaLabel);
		
		fp2.addComp(TableLayout.getSplit(descGammaLabel, gammaSlider1vis,
				TableLayout.PREFERRED, TableLayout.FILL));
		
		fp.layoutRows();
		fp2.layoutRows();
		
		result.add(fp.getBorderedComponent(5, 0, 0, 0), "1,2");
		result.add(fp2.getBorderedComponent(5, 0, 0, 0), "1,3");
		
		result.validate();
		return result;
	}
	
	private JSlider getNewGammaSlider(final JLabel jLabelDesc) {
		JSlider gammaSlider = new JSlider(1, 100);
		if (SystemInfo.isMac())
			gammaSlider.setPaintTrack(false);
		gammaSlider.setOpaque(false);
		Dictionary<Integer, JLabel> d = new Hashtable<Integer, JLabel>();
		d.put(new Integer(1), new JLabel("r^1", JLabel.LEFT));
		d.put(new Integer(50), new JLabel("r^50"));
		gammaSlider.setLabelTable(d);
		gammaSlider.setPaintLabels(true);
		gammaSlider.setValue(currGammaValue);
		final String gammaDesc = "<html>Gamma&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		jLabelDesc.setText(gammaDesc + "<br>correction (" + currGammaValue
				+ ")");
		gammaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				currGammaValue = ((JSlider) e.getSource()).getValue();
				jLabelDesc.setText(gammaDesc + "<br>correction ("
						+ currGammaValue + ")");
			}
		});
		return gammaSlider;
	}
	
	private JComponent getNewColorPanel() {
		double border2 = 0;
		double[][] size2 = {
				{ border2, TableLayoutConstants.PREFERRED,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, border2 }, // Columns
				{ border2, TableLayout.PREFERRED, border2 } }; // Rows
		
		JPanel colorPanel = new JPanel();
		colorPanel.setOpaque(false);
		colorPanel.setLayout(new TableLayout(size2));
		
		JLabel descColPanel = new JLabel("Color-Code for r=");
		descColPanel.setOpaque(false);
		colorPanel.add(descColPanel, "1,1");
		JButton jBcol_1 = new JButton("-1");
		JButton jBcol0 = new JButton("0");
		JButton jBcol1 = new JButton("1");
		
		col1buttons.add(jBcol_1);
		col2buttons.add(jBcol0);
		col3buttons.add(jBcol1);
		
		colorPanel.add(jBcol_1, "2,1");
		colorPanel.add(jBcol0, "3,1");
		colorPanel.add(jBcol1, "4,1");
		
		jBcol_1.setBorder(BorderFactory.createLineBorder(colR_1, 3));
		jBcol0.setBorder(BorderFactory.createLineBorder(colR0, 3));
		jBcol1.setBorder(BorderFactory.createLineBorder(colR1, 3));
		jBcol_1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = getChoosenColor(colR_1);
				if (c != null) {
					colR_1 = c;
					((JButton) e.getSource()).setBorder(BorderFactory
							.createLineBorder(c, 3));
				}
			}
		});
		jBcol0.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = getChoosenColor(colR0);
				if (c != null) {
					colR0 = c;
					((JButton) e.getSource()).setBorder(BorderFactory
							.createLineBorder(c, 3));
				}
			}
		});
		jBcol1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = getChoosenColor(colR1);
				if (c != null) {
					colR1 = c;
					((JButton) e.getSource()).setBorder(BorderFactory
							.createLineBorder(c, 3));
				}
			}
		});
		return colorPanel;
	}
	
	public static Color getChoosenColor(Color refCol) {
		MainFrame mf = GravistoService.getInstance().getMainFrame();
		Color c = JColorChooser.showDialog(mf, "Select Color", refCol);
		return c;
	}
	
	private JComponent getPlotPanel() {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED, TableLayoutConstants.FILL,
						border } }; // Rows
		result.setLayout(new TableLayout(size));
		
		doScatterPlotButton = new JMButton("(Re)Create Scatter-Plot Matrix");
		doScatterPlotButton.setOpaque(false);
		doScatterPlotButton.addActionListener(this);
		result.add(doScatterPlotButton, "1,1");
		lastScatterPlot = new JLabel("");
		result.add(lastScatterPlot, "1,4");
		
		FolderPanel fp = new FolderPanel("Calculation Settings", false, true,
				false, JLabelJavaHelpLink.getHelpActionListener("stat_scatter"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);
		
		FolderPanel fp2 = new FolderPanel("Visualization Settings", false,
				true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);
		
		checkBoxPlotAverage3 = new JCheckBox(
				"<html>Plot average values<br>"
						+ "<small>(recommended for time series data with few replicates per time point)",
				plotAverage);
		checkBoxPlotAverage3.setOpaque(false);
		checkBoxPlotAverage3.setSelected(plotAverage);
		checkBoxPlotAverage3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox src = (JCheckBox) arg0.getSource();
				plotAverage = src.isSelected();
			}
		});
		
		JComponent mergeEditor = getMergeOptionEditor(3);
		
		fp.addComp(checkBoxPlotAverage3);
		fp.addComp(mergeEditor);
		
		jTextFieldProb3scatter = new JTextField(new Double(prob).toString());
		jTextFieldMinR3 = new JTextField(new Double(minimumR).toString());
		JComponent corrType = getCorrelationTypeEditor(3);
		Component panelProb = getProbabilitySettingPanel(
				jTextFieldProb3scatter, jTextFieldMinR3, corrType);
		
		fp.addComp((JComponent) panelProb);
		
		fp2.addComp(getNewColorPanel());
		
		JLabel descGammaLabel = new JLabel("");
		gammaSlider2scatter = getNewGammaSlider(descGammaLabel);
		
		fp2.addComp(TableLayout.getSplit(descGammaLabel, gammaSlider2scatter,
				TableLayout.PREFERRED, TableLayout.FILL));
		
		final SpinnerModel sm = new SpinnerNumberModel(outlineBorderWidth, 0,
				100, 0.5);
		sm.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				outlineBorderWidth = ((Double) sm.getValue()).floatValue();
			}
		});
		JSpinner dataSizeSpinner = new JSpinner(sm);
		
		JLabel dpsdesc = new JLabel("Datapoint Size");
		dpsdesc.setOpaque(false);
		
		fp2.addComp(TableLayout.getSplit(dpsdesc, dataSizeSpinner,
				TableLayout.PREFERRED, TableLayout.FILL));
		
		final JCheckBox checkLegend = new JCheckBox("Show Legend", showLegend);
		checkLegend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLegend = checkLegend.isSelected();
			}
		});
		checkLegend.setOpaque(false);
		
		fp2.addComp(checkLegend);
		
		final JCheckBox checkShowRangeAxis = new JCheckBox("Show X-Axis",
				showRangeAxis);
		checkShowRangeAxis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showRangeAxis = checkShowRangeAxis.isSelected();
			}
		});
		checkShowRangeAxis.setOpaque(false);
		
		fp2.addComp(checkShowRangeAxis);
		
		final JCheckBox checkShowTicks = new JCheckBox("Show Y-Axis",
				tickMarksVisible);
		checkShowTicks.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tickMarksVisible = checkShowTicks.isSelected();
			}
		});
		checkShowTicks.setOpaque(false);
		
		fp2.addComp(checkShowTicks);
		
		fp.layoutRows();
		fp2.layoutRows();
		
		result.add(fp.getBorderedComponent(5, 0, 0, 0), "1,2");
		result.add(fp2.getBorderedComponent(5, 0, 0, 0), "1,3");
		
		result.validate();
		placeForScatter = result;
		return result;
	}
	
	private JComponent getStudentPanel() {
		JPanel result = new JPanel();
		
		double border = 5;
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayout.PREFERRED, TableLayout.PREFERRED, 5,
						TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayoutConstants.FILL, border } }; // Rows
		result.setLayout(new TableLayout(size));
		result.setOpaque(false);
		
		jTextFieldAlpha = new JTextField();
		
		if (alphaSpecified) {
			jTextFieldAlpha.setText(alpha + "");
			jTextFieldAlpha.setEnabled(true);
		} else {
			jTextFieldAlpha
					.setText("(using automatic setting, 0.05 / 0.01 / 0.001)");
			jTextFieldAlpha.setEnabled(false);
		}
		
		// add action button
		doTest = new JMButton("<html>Compare Conditions");
		doTest.setOpaque(false);
		doTest.addActionListener(this);
		
		result.add(doTest, "1,2");
		
		final JRadioButton ttestSel = new JRadioButton(
				"<html>Unpaired T-Test<br>"
						+ "<small>StdDev is unknown but expected to be equal (homoscedastic), "
						+ "assuming a normal distribution of independent samples");
		final JRadioButton welchSel = new JRadioButton(
				"<html>Welch-Satterthwaite T-Test<br>"
						+ "<small>StdDev is unknown (heteroscedastic), "
						+ "assuming a normal distribution of independent samples");
		final JRadioButton wilcoxonSel = new JRadioButton(
				"<html>Wilcoxon, Mann-Whitney U-Test<br>"
						+ "<small>Rank sum test for two independent samples");
		
		final JRadioButton ratioSel = new JRadioButton(
				"<html>Ratio Difference<br>"
						+ "<small>Check if the ratio of the mean values is above or below the specified threshold");
		
		ttestSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 2);
		ttestSel.setOpaque(false);
		welchSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 3);
		welchSel.setOpaque(false);
		wilcoxonSel
				.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 4);
		wilcoxonSel.setOpaque(false);
		ratioSel.setSelected(sampleCalcType_2doublet_3welch_4wilcoxon_5ratio == 5);
		ratioSel.setOpaque(false);
		ButtonGroup typeOfCalculation = new ButtonGroup();
		typeOfCalculation.add(ttestSel);
		typeOfCalculation.add(welchSel);
		typeOfCalculation.add(wilcoxonSel);
		typeOfCalculation.add(ratioSel);
		ttestSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ttestSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 2;
			}
		});
		welchSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (welchSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 3;
			}
		});
		wilcoxonSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (wilcoxonSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 4;
			}
		});
		
		ratioSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ratioSel.isSelected())
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio = 5;
			}
		});
		
		SpinnerModel sm1 = new SpinnerNumberModel(ratioL, 0, 1, 0.05d);
		final JSpinner minRatio = new JSpinner(sm1);
		minRatio.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ratioL = (Double) minRatio.getValue();
			}
		});
		SpinnerModel sm2 = new SpinnerNumberModel(ratioU, 1, Double.MAX_VALUE,
				0.05d);
		final JSpinner maxRatio = new JSpinner(sm2);
		minRatio.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ratioU = (Double) maxRatio.getValue();
			}
		});
		
		JComponent calcTypePanel = TableLayout.get3SplitVertical(ttestSel,
				TableLayout.getSplitVertical(welchSel, wilcoxonSel,
						TableLayout.PREFERRED, TableLayout.PREFERRED),
				ratioSel, TableLayout.PREFERRED, TableLayout.PREFERRED,
				TableLayout.PREFERRED);
		
		calcTypePanel.setOpaque(false);
		calcTypePanel.setBorder(BorderFactory
				.createTitledBorder("Type of test"));
		
		FolderPanel fp = new FolderPanel("Calculation Settings", false, true,
				false, JLabelJavaHelpLink.getHelpActionListener("stat_ttest"));
		fp.setBackground(null);
		fp.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp.setEmptyBorderWidth(0);
		
		fp.addComp(calcTypePanel);
		
		// add significance value selection
		JCheckBox specifyAlpha = new JCheckBox("<html>Specify &#945; value:",
				alphaSpecified);
		specifyAlpha.setBackground(null);
		specifyAlpha.setOpaque(false);
		specifyAlpha.setSelected(true);
		specifyAlpha.setEnabled(false);
		specifyAlpha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				alphaSpecified = ((JCheckBox) e.getSource()).isSelected();
				if (alphaSpecified) {
					jTextFieldAlpha.setText(alpha + "");
					jTextFieldAlpha.setEnabled(true);
				} else {
					jTextFieldAlpha
							.setText("(not yet implemented, using 0.05)"); // /
					// 0.01
					// /
					// 0.001
					jTextFieldAlpha.setEnabled(false);
				}
			}
		});
		JComponent panelProb = TableLayout.getSplit(specifyAlpha,
				jTextFieldAlpha, TableLayout.PREFERRED, TableLayout.FILL);
		fp.addComp(panelProb);
		
		fp.addComp(TableLayout.get3Split(new JLabel(
				"<html>Ratio (Lower / Upper limit): "), minRatio, maxRatio,
				TableLayout.FILL, 50, 50));
		
		fp.layoutRows();
		
		result.add(fp, "1,4");
		
		FolderPanel fp2 = new FolderPanel("Visualization Settings", false,
				true, false, null);
		fp2.setBackground(null);
		fp2.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 0);
		fp2.setEmptyBorderWidth(0);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path
				+ "/ttestCircleSize.png"));
		JLabel ttestCircleSize = new JLabel("T-Test-Marker Size", icon,
				JLabel.RIGHT);
		ttestCircleSize.setBackground(Color.WHITE);
		ttestCircleSize.setOpaque(true);
		
		double curVal = 10d;
		Graph graph = null;
		try {
			EditorSession session = GravistoService.getInstance()
					.getMainFrame().getActiveEditorSession();
			graph = session.getGraph();
			curVal = ((Double) AttributeHelper.getAttributeValue(graph, "",
					AttributeHelper.id_ttestCircleSize, new Double(10.0d),
					new Double(10.0d))).doubleValue();
		} catch (Exception e) {
			// empty
		}
		
		final SpinnerNumberModel numberModel = new SpinnerNumberModel(curVal,
				0d, Double.MAX_VALUE, 0.5d);
		numberModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					EditorSession session = GravistoService.getInstance()
							.getMainFrame().getActiveEditorSession();
					Graph g = session.getGraph();
					AttributeHelper.setAttribute(g, "",
							AttributeHelper.id_ttestCircleSize, new Double(
									numberModel.getNumber().doubleValue()));
				} catch (Exception err) {
					// empty
				}
			}
		});
		JSpinner circleSize = new JSpinner(numberModel);
		
		fp2.addComp(TableLayout.get3Split(ttestCircleSize, new JLabel(""),
				circleSize, TableLayout.PREFERRED, 3, TableLayout.FILL));
		
		addStatusText1.setOpaque(false);
		addStatusText1.setSelected(showStatusResult);
		addStatusText1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showStatusResult = addStatusText1.isSelected();
				addStatusText2.setSelected(showStatusResult);
				addStatusText3.setSelected(showStatusResult);
			}
		});
		
		JButton clearStatus = new JMButton("<html><small>Clear<br>Status Text");
		clearStatus.setOpaque(false);
		clearStatus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Collection<GraphElement> graphElements = GraphHelper
							.getSelectedOrAllGraphElements();
					for (GraphElement ge : graphElements)
						AttributeHelper.setToolTipText(ge, "");
				} catch (NullPointerException npe) {
					MainFrame.showMessageDialog(
							"No active graph editor window found!", "Error");
				}
			}
		});
		fp2.addComp(TableLayout.get3Split(addStatusText1, new JLabel(""),
				clearStatus, TableLayout.FILL, 5, TableLayout.PREFERRED));
		
		fp2.layoutRows();
		result.add(fp2.getBorderedComponent(5, 0, 0, 0), "1,5");
		
		result.validate();
		return result;
	}
	
	/**
	 * Constructs a <code>PatternTab</code> and sets the title.
	 */
	public TabStatistics() {
		super();
		this.title = "Statistics";
		initComponents();
	}
	
	public void postAttributeAdded(AttributeEvent e) {
	}
	
	public void postAttributeChanged(AttributeEvent e) {
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
	}
	
	public void preAttributeAdded(AttributeEvent e) {
	}
	
	public void preAttributeChanged(AttributeEvent e) {
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
	}
	
	public void transactionFinished(TransactionEvent e) {
	}
	
	public void transactionStarted(TransactionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == findCorrButton) {
			checkProbabilityInput(jTextFieldProb1findCorr);
			checkRinput(jTextFieldMinR1);
		}
		if (e.getSource() == visCorrButton) {
			checkProbabilityInput(jTextFieldProb2visCorr);
			checkRinput(jTextFieldMinR2);
		}
		if (e.getSource() == doScatterPlotButton) {
			checkProbabilityInput(jTextFieldProb3scatter);
			checkRinput(jTextFieldMinR3);
		}
		
		try {
			double temp = Double.parseDouble(jTextFieldAlpha.getText());
			alpha = temp;
		} catch (NumberFormatException nfe) {
			// probability remains unchanged
		}
		if (e.getSource() == doTest && alphaSpecified)
			jTextFieldAlpha.setText(new Double(alpha).toString());
		
		EditorSession session = GravistoService.getInstance().getMainFrame()
				.getActiveEditorSession();
		
		Selection selection = null;
		if (session != null)
			selection = session.getSelectionModel().getActiveSelection();
		
		Graph graph = null;
		if (session != null)
			graph = session.getGraph();
		
		if (e.getSource() == findCorrButton) {
			Collection<Node> nodes = null;
			if (selection != null)
				nodes = selection.getNodes();
			if (nodes == null || nodes.size() == 0) {
				if (graph != null)
					nodes = graph.getNodes();
			}
			if (nodes == null || nodes.size() < 2) {
				MainFrame
						.showMessageDialog(
								"Please select at least two nodes which have experimental data assigned.",
								"More than one node needs to be selected");
			} else {
				findCorrelations(nodes, graph, session);
			}
		}
		if (e.getSource() == visCorrButton) {
			Collection<GraphElement> graphElements = null;
			if (selection != null)
				graphElements = selection.getElements();
			if (graphElements == null || graphElements.size() != 1) {
				MainFrame
						.showMessageDialog(
								"Please select a single node or edge which has experimental data assigned.",
								"One element needs to be selected");
			} else
				visualiseCorrelation(graphElements.iterator().next(), graph);
		}
		if (e.getSource() == resetColorAndBorder) {
			if (graph == null) {
				MainFrame.showMessageDialog("No graph available",
						"No graph available");
			} else
				resetColorAndBorder(graph);
		}
		if (e.getSource() == removeCorrelationEdges) {
			if (graph == null) {
				MainFrame.showMessageDialog("No graph available",
						"No graph available");
			} else
				removeCorrelationEdges(graph);
		}
		if (e.getSource() == selectCorrelationEdges) {
			if (graph == null) {
				MainFrame.showMessageDialog("No graph available",
						"No graph available");
			} else
				selectCorrelationEdges(graph, session);
		}
		if (e.getSource() == doScatterPlotButton) {
			Collection<GraphElement> graphElements = null;
			if (selection != null)
				graphElements = selection.getElements();
			if (graphElements == null || graphElements.size() < 2) {
				MainFrame
						.showMessageDialog(
								"Please select at least two nodes or edges which have experimental data assigned.",
								"More than one node needs to be selected");
			} else
				lastScatterPlot = createScatterPlotBlock(plotAverage, tickMarksVisible,
						showRangeAxis, showLegend, minimumR,
						outlineBorderWidth, mergeDataset, prob, rankOrder,
						currGammaValue, colR_1, colR0, colR1, graphElements,
						graph, false, lastScatterPlot, placeForScatter);
		}
		if (e.getSource() == doTest) {
			refreshReferenceInfo(GraphHelper.getSelectedOrAllNodes(selection,
					graph));
			doTtest(GraphHelper.getSelectedOrAllGraphElements(selection, graph),
					sampleCalcType_2doublet_3welch_4wilcoxon_5ratio, graph,
					showStatusResult);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JComponent getScatterPlot(Graph graph) {
		Collection<GraphElement> graphElements = (Collection) graph.getNodes();
		
		boolean plotAverage = false;
		boolean mergeDataset = true;
		boolean rankOrder = false;
		
		double prob = 0.95;
		
		Color colR_1 = Color.RED;
		Color colR0 = Color.WHITE;
		Color colR1 = Color.BLUE;
		
		boolean showRangeAxis = false;
		boolean tickMarksVisible = false;
		boolean showLegend = false;
		float outlineBorderWidth = 10f;
		
		int currGammaValue = 1;
		
		double minimumR = 0;
		
		return (JComponent) createScatterPlotBlock(plotAverage, tickMarksVisible,
				showRangeAxis, showLegend, minimumR, outlineBorderWidth,
				mergeDataset, prob, rankOrder, currGammaValue, colR_1, colR0,
				colR1, graphElements, graph, true, null, null);
	}
	
	// private double findR(int n, double alpha, double a, double b) {
	// double r = (a+b)/2;
	// double t = r/Math.sqrt((1-r*r)/(n-2));
	// double alphaCalc = tcall(buzz( t, n-2));
	//
	// if (Math.abs(a-b)<0.000001)
	// return r;
	//
	// if (alphaCalc<alpha)
	// return findR(n, alpha, a, (a+b)/2);
	// else
	// return findR(n, alpha, (a+b)/2, b);
	// }
	//
	
	private void checkProbabilityInput(JTextField textFieldProb) {
		try {
			double temp = Double.parseDouble(textFieldProb.getText());
			prob = temp;
		} catch (NumberFormatException nfe) {
			// probability remains unchanged
		}
		textFieldProb.setText(new Double(prob).toString());
	}
	
	private void checkRinput(JTextField textFieldR) {
		try {
			double temp = Double.parseDouble(textFieldR.getText());
			minimumR = temp;
		} catch (NumberFormatException nfe) {
			// probability remains unchanged
		}
		textFieldR.setText(new Double(minimumR).toString());
	}
	
	private void removeCorrelationEdges(Graph graph) {
		ArrayList<Edge> toBeDeleted = new ArrayList<Edge>();
		for (Edge e : graph.getEdges()) {
			if (correlationEdges.contains(e)) {
				toBeDeleted.add(e);
				correlationEdges.remove(e);
			}
		}
		graph.getListenerManager().transactionStarted(this);
		for (Edge e : toBeDeleted) {
			graph.deleteEdge(e);
		}
		graph.getListenerManager().transactionFinished(this);
	}
	
	private void selectCorrelationEdges(Graph graph, EditorSession session) {
		Selection s = session.getSelectionModel().getActiveSelection();
		if (s == null)
			s = new Selection("new edges");
		for (Edge e : graph.getEdges()) {
			if (correlationEdges.contains(e)) {
				s.add(e);
			}
		}
		graph.getListenerManager().transactionStarted(this);
		session.getSelectionModel().selectionChanged();
		graph.getListenerManager().transactionFinished(this);
	}
	
	/**
	 * Sets the border and color to the old values, that where active at the
	 * time the tab "Statistics" got visible.
	 */
	private void resetColorAndBorder(Graph graph) {
		graph.getListenerManager().transactionStarted(this);
		for (Node n : graph.getNodes()) {
			AttributeHelper.setBorderWidth(n, 1);
			AttributeHelper.setFillColor(n, Color.WHITE);
			AttributeHelper.setToolTipText(n, "");
			AttributeHelper.deleteAttribute(n, "statistics", "correlation_r");
		}
		for (Edge e : graph.getEdges()) {
			AttributeHelper.setBorderWidth(e, 1);
			AttributeHelper.setFillColor(e, Color.BLACK);
			AttributeHelper.setToolTipText(e, "");
			AttributeHelper.deleteAttribute(e, "statistics", "correlation_r");
		}
		graph.getListenerManager().transactionFinished(this);
	}
	
	@SuppressWarnings("unchecked")
	private void refreshReferenceInfo(List<Node> nodes) {
		referenceSelection = null;
		ArrayList<String> conditions = new ArrayList<String>();
		for (Iterator<Node> itNodes = nodes.iterator(); itNodes.hasNext();) {
			Node node = itNodes.next();
			ExperimentInterface mappedDataList = Experiment2GraphHelper
					.getMappedDataListFromGraphElement(node);
			if (mappedDataList != null)
				for (Iterator<SubstanceInterface> itXml = mappedDataList
						.iterator(); itXml.hasNext();) {
					SubstanceInterface xmldata = itXml.next();
					for (ConditionInterface c : xmldata) {
						if (!conditions.contains(c.getExpAndConditionName()))
							conditions.add(c.getExpAndConditionName());
					}
				}
		}
		
		ArrayList params = new ArrayList();
		
		params.add("Reference Dataset:");
		final JComboBox jc = new JComboBox(conditions.toArray());
		params.add(jc);
		params.add("<html><br>Compare to:");
		params.add(new JLabel());
		
		final ArrayList<JCheckBox> bpl = new ArrayList<JCheckBox>();
		
		for (String c : conditions) {
			params.add("");
			JCheckBox bp = new JCheckBox(c, true);
			bp.setToolTipText("If selected, the reference dataset samples will be compared with samples from this condition.");
			params.add(bp);
			
			bpl.add(bp);
		}
		
		ActionListener all = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String sel = (String) jc.getSelectedItem();
				for (JCheckBox bp : bpl) {
					if (bp.getText().equals(sel))
						bp.setEnabled(false);
					else
						bp.setEnabled(true);
				}
			}
		};
		all.actionPerformed(null);
		
		jc.addActionListener(all);
		
		Object[] res = MyInputHelper
				.getInput(
						"<html>"
								+ "Please select the reference dataset and the conditions.<br><br>",
						"Select Reference Dataset", params.toArray());
		
		if (res == null) {
			referenceSelection = null;
			validConditions = null;
		} else {
			int idx = 0;
			referenceSelection = (String) ((JComboBox) res[idx++])
					.getSelectedItem();
			idx++;
			validConditions = new HashSet<String>();
			for (String c : conditions) {
				if (((JCheckBox) res[idx++]).isSelected())
					validConditions.add(c);
			}
		}
	}
	
	private void doTtest(Collection<GraphElement> graphElements,
			int type_2doublet_3welch_4wilcoxon, Graph g,
			boolean addStatusMessage) {
		String referenceMeasurement = referenceSelection;
		if (referenceMeasurement == null)
			MainFrame.showMessageDialog(
					"Please select a reference measurement.",
					"No reference dataset selected");
		else
			if (validConditions == null || validConditions.size() < 1) {
				MainFrame
						.showMessageDialog(
								"<html>"
										+ "At least two conditions (reference and one additional condition)<br>"
										+ "need to be selected.",
								"No reference dataset selected");
			} else {
				String referenceLineDesc = referenceMeasurement;
				for (GraphElement ge : graphElements) {
					ExperimentInterface mappedDataList = Experiment2GraphHelper
							.getMappedDataListFromGraphElement(ge);
					List<SampleInterface> samplesInNode = new ArrayList<SampleInterface>();
					HashMap<String, SampleInterface> timeAndConditionNames2sample = new HashMap<String, SampleInterface>();
					if (mappedDataList != null) {
						for (Iterator<SubstanceInterface> itXml = mappedDataList
								.iterator(); itXml.hasNext();) {
							SubstanceInterface xmldata = itXml.next();
							for (ConditionInterface c : xmldata)
								samplesInNode.addAll(c);
						}
						for (SampleInterface s : samplesInNode) {
							timeAndConditionNames2sample.put(s.getSampleTime()
									+ "/"
									+ s.getParentCondition()
											.getExpAndConditionName(), s);
						}
					}
					String testDesc = "";
					if (type_2doublet_3welch_4wilcoxon == 2)
						testDesc = "homoscedastic Students t-test";
					if (type_2doublet_3welch_4wilcoxon == 4)
						testDesc = "U-test";
					if (type_2doublet_3welch_4wilcoxon == 3)
						testDesc = "Welch's test";
					if (type_2doublet_3welch_4wilcoxon == 5)
						testDesc = "Ratio Check";
					String statusLineText = "<html>[Press <b>F2</b> if text does not completely fit into view]<br>"
							+ "<b>"
							+ testDesc
							+ ", reference: "
							+ referenceLineDesc
							+ "</b>, "
							+ "alpha (two sided): "
							+ alpha
							+ "<br><small>"
							+ "<table border=\"1\">"
							+ "<tr>";
					// now all samples for the current node are gathered
					// if the sample is not a reference measurement, do t-test
					// calculation
					int sampleIdx = 0;
					for (SampleInterface sampleNode : samplesInNode) {
						String line = sampleNode.getParentCondition()
								.getExpAndConditionName();
						sampleIdx++;
						// System.out.println(line);
						if (referenceMeasurement.equals(line)) {
							sampleNode.setSampleTtestInfo(TtestInfo.REFERENCE);
						} else {
							if (validConditions.contains(line)) {
								// search reference sample with the same time value
								String compareTime = sampleNode.getSampleTime();
								SampleInterface refSampleNode = timeAndConditionNames2sample
										.get(compareTime + "/"
												+ referenceMeasurement);
								if (refSampleNode == null) {
									statusLineText += "<td>No reference sample to compare sample with time point \""
											+ compareTime
											+ "\" and<br>"
											+ "line name \""
											+ line
											+ "\" found!</td>";
									sampleNode.setSampleTtestInfo(TtestInfo.H0);
								} else {
									// do t-test and add result to node
									statusLineText += "<td>"
											+ line
											+ (compareTime.length() > 0 ? " ["
													+ compareTime + "]" : "")
											+ "<br><small>";
									StringBuilder statusResult = new StringBuilder();
									
									if (type_2doublet_3welch_4wilcoxon == 2) {
										if (calcuteTtest(
												refSampleNode.getDataList(),
												sampleNode.getDataList(), alpha,
												statusResult, sampleIdx))
											sampleNode
													.setSampleTtestInfo(TtestInfo.H1);
										else
											sampleNode
													.setSampleTtestInfo(TtestInfo.H0);
									} else
										if (type_2doublet_3welch_4wilcoxon == 3) {
											boolean useApache = true;
											if (calcuteTestVonWelch(
													refSampleNode.getDataList(),
													sampleNode.getDataList(), alpha,
													useApache, statusResult, sampleIdx))
												sampleNode
														.setSampleTtestInfo(TtestInfo.H1);
											else
												sampleNode
														.setSampleTtestInfo(TtestInfo.H0);
										} else
											if (type_2doublet_3welch_4wilcoxon == 4) {
												if (calcuteWilcoxonTest(
														refSampleNode.getDataList(),
														sampleNode.getDataList(), alpha,
														statusResult, sampleIdx))
													sampleNode
															.setSampleTtestInfo(TtestInfo.H1);
												else
													sampleNode
															.setSampleTtestInfo(TtestInfo.H0);
											} else
												if (type_2doublet_3welch_4wilcoxon == 5) {
													if (calcuteRatioTest(
															refSampleNode.getDataList(),
															sampleNode.getDataList(), ratioL,
															ratioU, statusResult, sampleIdx))
														sampleNode
																.setSampleTtestInfo(TtestInfo.H1);
													else
														sampleNode
																.setSampleTtestInfo(TtestInfo.H0);
												} else
													ErrorMsg.addErrorMessage("Calculation type not implemented!");
									statusLineText += statusResult.toString()
											+ "</small></td>";
								}
							}
						}
					}
					statusLineText += "</tr></table>";
					if (addStatusMessage)
						AttributeHelper.setToolTipText(ge, statusLineText);
				}
				GraphHelper.issueCompleteRedrawForGraph(g);
			}
	}
	
	private boolean calcuteTtest(Double[] X, Double[] Y, double alpha,
			StringBuilder statusResult, int sampleIdx) { // boolean
		// useApache,
		// DescriptiveStatistics stat = DescriptiveStatistics.newInstance();
		Boolean res1;
		TTest ttest = new TTest();
		double[] xd = new double[X.length];
		int i = 0;
		for (Double v : X)
			xd[i++] = v.doubleValue();
		double[] yd = new double[Y.length];
		i = 0;
		for (Double v : Y)
			yd[i++] = v.doubleValue();
		try {
			res1 = ttest.homoscedasticTTest(xd, yd, alpha);
		} catch (Exception me) {
			ErrorMsg.addErrorMessage("Statistical calculation failed: "
					+ me.getMessage());
			statusResult.append("<b>Sample " + sampleIdx
					+ " : Calculation Error: " + me.getLocalizedMessage()
					+ "</b><br>");
			res1 = null;
		}
		/*
		 * System.out.println("-------------- alpha: " + alpha); print(X,
		 * "L1: "); print(Y, "L2: "); System.out.println("--------------");
		 */
		int n1 = X.length;
		int n2 = Y.length;
		
		double x_ = getAVG(X);
		double y_ = getAVG(Y);
		double s2_1 = getStd(X, x_);
		double s2_2 = getStd(Y, y_);
		
		Math.sqrt(((n1 - 1) * s2_1 + (n2 - 1) * s2_2) /
				// -----------------------------
				(n1 + n2 - 2));
		
		double d_ = Math.abs(x_ - y_);
		statusResult.append("n A= " + n1 + ", " + "n B= " + n2 + ", "
				+ "avg SAMPLE A= "
				+ StringManipulationTools.formatNumber(x_, "#.###") + ", "
				+ "avg SAMPLE B="
				+ StringManipulationTools.formatNumber(y_, "#.###") + ", "
				+ "variance A="
				+ StringManipulationTools.formatNumber(s2_1, "#.###") + ", "
				+ "variance B="
				+ StringManipulationTools.formatNumber(s2_2, "#.###") + ", "
				+ "|avg A - avg B|="
				+ StringManipulationTools.formatNumber(d_, "#.###") + ", "
				+ "df=" + (n1 + n2 - 2) + "<br>");
		try {
			statusResult.append("<b>P ["
					+ StringManipulationTools.formatNumber(
							ttest.homoscedasticTTest(xd, yd), "#.###")
					+ "] &lt; alpha ["
					+ StringManipulationTools.formatNumber(alpha, "#.###")
					+ "]? " + (res1 != null && res1 ? "YES" : "NO")
					+ " : Sample " + sampleIdx + "</b>");
		} catch (Exception e) {
			// empty
		}
		// if (useApache)
		return res1 == null ? false : res1;
		/*
		 * else return res2;
		 */
	}
	
	private boolean calcuteTestVonWelch(Double[] X, Double[] Y, double alpha,
			boolean useApache, StringBuilder statusResult, int sampleIdx) {
		Boolean res1, res2;
		
		TTest ttest = new TTest();
		double[] xd = new double[X.length];
		int i = 0;
		for (Double v : X)
			xd[i++] = v.doubleValue();
		double[] yd = new double[Y.length];
		i = 0;
		for (Double v : Y)
			yd[i++] = v.doubleValue();
		try {
			res1 = ttest.tTest(xd, yd, alpha);
		} catch (Exception me) {
			ErrorMsg.addErrorMessage(me);
			statusResult.append("<b>Sample " + sampleIdx
					+ " : Calculation Error: " + me.getLocalizedMessage()
					+ "</b><br>");
			res1 = null;
		}
		
		int n1 = X.length;
		int n2 = Y.length;
		
		double x_ = getAVG(X);
		double y_ = getAVG(Y);
		double s2_1 = getStd(X, x_);
		double s2_2 = getStd(Y, y_);
		
		double u = (s2_1 / n1) /
				// -------------------------
				(s2_1 / n1 + s2_2 / n2);
		
		double v = 1 / (u * u / (n1 - 1) + (1 - u) * (1 - u) / (n2 - 1));
		double d_ = Math.abs(x_ - y_);
		double t_ = ttab(v, 1 - alpha / 2) * Math.sqrt(s2_1 / n1 + s2_2 / n2);
		
		res2 = d_ > t_;
		if (res1 == null || res2 == null
				|| res2.booleanValue() != res1.booleanValue()) {
			// MainFrame.showMessageDialog("Statistic calculation difference!",
			// "CHECK");
		}
		statusResult.append("n A= " + n1 + ", " + "n B= " + n2 + ", "
				+ "avg SAMPLE A= "
				+ StringManipulationTools.formatNumber(x_, "#.###") + ", "
				+ "avg SAMPLE B="
				+ StringManipulationTools.formatNumber(y_, "#.###") + ", "
				+ "variance A="
				+ StringManipulationTools.formatNumber(s2_1, "#.###") + ", "
				+ "variance B="
				+ StringManipulationTools.formatNumber(s2_2, "#.###") + ", "
				+ "|avg A - avg B|="
				+ StringManipulationTools.formatNumber(d_, "#.###") + ", "
				+ "df=" + (n1 + n2 - 2) + "<br>");
		try {
			statusResult.append("<b>P ["
					+ StringManipulationTools.formatNumber(ttest.tTest(xd, yd),
							"#.###") + "] &lt; alpha ["
					+ StringManipulationTools.formatNumber(alpha, "#.###")
					+ "]? " + (res1 != null && res1 ? "YES" : "NO")
					+ " : Sample " + sampleIdx + "</b>");
		} catch (IllegalArgumentException e) {
			// empty
		} catch (Exception e) {
			// empty
		}
		if (useApache)
			return (res1 == null ? false : res1);
		else
			return res2;
	}
	
	private static NormalDistribution normalDistribution = new NormalDistribution();
	
	private boolean calcuteWilcoxonTest(Double[] X, Double[] Y, double alpha,
			StringBuilder statusResult, int sampleIdx) {
		double R1, R2;
		R1 = 0;
		R2 = 0;
		
		DoubleAndSourceList[] ranks = getRankValues(X, Y);
		ArrayList<Double> Xranks = new ArrayList<Double>();
		ArrayList<Double> Yranks = new ArrayList<Double>();
		for (DoubleAndSourceList d : ranks) {
			if (d.getSourceListIndex01() == 0)
				Xranks.add(d.getRangValue());
			else
				Yranks.add(d.getRangValue());
		}
		
		for (Double d : Xranks)
			R1 += d;
		for (Double d : Yranks)
			R2 += d;
		statusResult.append("<table><tr><th>RANKS A</th><th>RANKS B</th></tr>");
		statusResult.append("<tr><td>");
		for (Double d : Xranks)
			statusResult.append(d + "<br>");
		statusResult.append("</td><td>");
		for (Double d : Yranks)
			statusResult.append(d + "<br>");
		statusResult.append("</tr></table>");
		int m = Xranks.size();
		int n = Yranks.size();
		double U1, U2;
		U1 = m * n + (m * (m + 1) / 2d) - R1;
		U2 = m * n + (n * (n + 1) / 2d) - R2;
		
		double PG; // Prüfgröße = U1, if U1<U2, else U2
		if (U1 < U2)
			PG = U1;
		else
			PG = U2;
		
		double U = PG;
		statusResult.append("PG=" + U + ", U1=" + U1 + ", U2=" + U2 + ", m="
				+ m + ", n=" + n + ", R1=" + R1 + ", R2=" + R2 + "<br>");
		double epsilon = 0.0000001d;
		if (Math.abs((U1 + U2) - (m * n)) > epsilon) {
			ErrorMsg.addErrorMessage("Sample "
					+ sampleIdx
					+ " : Internal Error, Wilcoxon Test might be calculated incorrectly!");
			statusResult
					.append("Sample "
							+ sampleIdx
							+ " : Internal Error, Wilcoxon Test might be calculated incorrectly!");
		}
		
		ArrayList<Tie> bindungen = ermittleBindungen(ranks);
		
		double special_sum = 0d;
		double S = m + n;
		
		// für jede Bindung (Anzahl Bindungen = r)
		// t_i = Vielfachheit der Bindung i
		// siehe "Lothar Sachs, Angewandte Statistik, S. 235
		// Walther 1951, nach einem Vorschlag von Kendalls 1945
		int bi = 1;
		for (Tie b : bindungen) {
			double t_i = b.getVielfachheit();
			special_sum += t_i * t_i * t_i - t_i;
			statusResult.append("TIE " + (bi++) + " " + t_i + " times value "
					+ b.getValue() + " with rank " + b.getRankValue() + "<br>");
		}
		// if (Math.abs(special_sum)>epsilon)
		// ErrorMsg.addErrorMessage("Some Ties: "+special_sum);
		special_sum = special_sum / 12d;
		
		double z_ = Math.abs(U - m * n / 2d)
				/ Math.sqrt((m * n / (S * (S - 1)))
						* ((S * S * S - S) / 12d - special_sum));
		statusResult.append("TIE CORRECTION=" + special_sum + ", " + "z ["
				+ StringManipulationTools.formatNumber(z_, "#.###") + "]");
		double compare_z;
		try {
			compare_z = normalDistribution
					.inverseCumulativeProbability(1 - alpha / 2);
			statusResult.append(" &gt; z_alpha ["
					+ StringManipulationTools.formatNumber(compare_z, "#.###")
					+ "]");
			statusResult.append("? <b>" + (z_ > compare_z ? "YES" : "NO")
					+ " : Sample " + sampleIdx + "</b>");
			return z_ > compare_z;
		} catch (Exception e) {
			statusResult.append(", MATH ERROR FOR SAMPLE " + sampleIdx);
			ErrorMsg.addErrorMessage(e);
			return false;
		}
	}
	
	private boolean calcuteRatioTest(Double[] X, Double[] Y,
			double belowThisRatio, double overThisRatio,
			StringBuilder statusResult, int sampleIdx) {
		double avgA, avgB;
		double sum = 0;
		for (double xv : X)
			sum += xv;
		avgA = sum / X.length;
		
		sum = 0;
		for (double yv : Y)
			sum += yv;
		avgB = sum / Y.length;
		
		double ratio = avgB / avgA;
		
		boolean result = false;
		
		if (ratio <= belowThisRatio || ratio >= overThisRatio)
			result = true;
		
		statusResult.append("Ratio = " + ratio + " (avg Y/avg X: " + avgB + "/"
				+ avgA + ") : Sample " + sampleIdx + " : "
				+ (ratio < 0 ? "-" : "+") + "<br>");
		
		return result;
	}
	
	public static double epsilon = 0.0000001;
	
	public static DoubleAndSourceList[] getRankValues(Collection<Double> x) {
		return getRankValues(x.toArray(new Double[] {}), new Double[] {});
	}
	
	public static DoubleAndSourceList[] getRankValues(Double[] x, Double[] y) {
		ArrayList<DoubleAndSourceList> values = new ArrayList<DoubleAndSourceList>();
		// System.out.println("n1="+x.length+", n2="+y.length+"\nSample A");
		for (Double d : x) {
			// System.out.println(d);
			values.add(new DoubleAndSourceList(d, 0));
		}
		// System.out.println("----\nSample B");
		for (Double d : y) {
			// System.out.println(d);
			values.add(new DoubleAndSourceList(d, 1));
		}
		DoubleAndSourceList[] valueArray = values
				.toArray(new DoubleAndSourceList[] {});
		Arrays.sort(valueArray, new Comparator<DoubleAndSourceList>() {
			@Override
			public int compare(DoubleAndSourceList o1, DoubleAndSourceList o2) {
				return o1.getDoubleValue().compareTo(o2.getDoubleValue());
			}
		});
		
		Stack<DoubleAndSourceList> todo = new Stack<DoubleAndSourceList>();
		int nextRank = 0; // rangs will be still set to 1...n
		for (DoubleAndSourceList testDasl : valueArray) {
			if (todo.size() == 0
					|| Math.abs(testDasl.getDoubleValue()
							- todo.peek().getDoubleValue()) < epsilon) {
				todo.push(testDasl);
			} else {
				double rang = (nextRank - todo.size() + 1 + nextRank) / 2d;
				while (!todo.empty()) {
					DoubleAndSourceList dasl = todo.pop();
					dasl.setRank(rang);
				}
				todo.push(testDasl);
			}
			nextRank++;
		}
		double rang = (nextRank + (nextRank + todo.size() - 1)) / 2d;
		while (!todo.empty()) {
			DoubleAndSourceList dasl = todo.pop();
			dasl.setRank(rang);
		}
		// int errorCnt = 0;
		// for (DoubleAndSourceList dasl : values) {
		// if (Double.isNaN(dasl.getRangValue())) {
		// errorCnt++;
		// }
		// }
		// System.out.println("ERRORS: "+errorCnt+" / "+values.size());
		return valueArray;
	}
	
	private ArrayList<Tie> ermittleBindungen(DoubleAndSourceList[] rangs) {
		ArrayList<Tie> result = new ArrayList<Tie>();
		double epsilon = 0.0000001;
		for (DoubleAndSourceList rankA : rangs) {
			int cntVielfachheit = 0;
			for (DoubleAndSourceList rankB : rangs) {
				if (rankA != rankB) {
					if (Math.abs(rankB.getRangValue() - rankA.getRangValue()) < epsilon)
						cntVielfachheit++;
				}
			}
			if (cntVielfachheit > 0) {
				boolean rangFound = false;
				for (Tie b : result) {
					if (Math.abs(b.getRankValue() - rankA.getRangValue()) < epsilon) {
						rangFound = true;
						break;
					}
				}
				if (!rangFound)
					result.add(new Tie(rankA.getRangValue(),
							cntVielfachheit + 1, rankA.getDoubleValue()));
			}
		}
		return result;
	}
	
	/**
	 * Returns all nodes which contain samples which are not normaly
	 * distributed.
	 * 
	 * @param nodes
	 * @param g
	 * @param probab_123
	 * @return Nodes which contain samples which are not normaly distributed.
	 */
	public static List<Node> doDavidSchnellTest(List<Node> nodes, Graph g,
			int probab_123) {
		ArrayList<Node> result = new ArrayList<Node>();
		for (Node node : nodes) {
			String resultForNode = "";// "Not normally distributed {pg=r/s, r=max-min}: ";
			XMLAttribute xa = null;
			try {
				CollectionAttribute ca = (CollectionAttribute) node
						.getAttribute(Experiment2GraphHelper.mapFolder);
				xa = (XMLAttribute) ca
						.getAttribute(Experiment2GraphHelper.mapVarName);
			} catch (AttributeNotFoundException anfe) {
				// emtpy
			}
			boolean allInRange = true;
			if (xa != null)
				for (SubstanceInterface xmldata : xa.getMappedData()) {
					List<MyComparableDataPoint> allvalues = NodeTools
							.getSortedDataSetValues(xmldata, null);
					
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
						for (String timePoint : knownTimePoints) {
							List<MyComparableDataPoint> values = new ArrayList<MyComparableDataPoint>();
							for (MyComparableDataPoint mcdp : valuesForSeries) {
								if (mcdp.timeUnitAndTime.equals(timePoint))
									values.add(mcdp);
							}
							
							// PG = R/s
							// R = Spannweite
							// s = Standardabweichung
							// PG = Prüfgröße
							
							// calculate s (StdDev)
							double sum = 0d;
							double min = Double.MAX_VALUE;
							double max = Double.NEGATIVE_INFINITY;
							int n = 0;
							for (MyComparableDataPoint value : values) {
								sum += value.mean;
								if (value.mean > max)
									max = value.mean;
								if (value.mean < min)
									min = value.mean;
								n++; // System.out.println(value.toString());
							}
							double avg = sum / n;
							double sumDiff = 0;
							for (MyComparableDataPoint value : values) {
								sumDiff += (value.mean - avg)
										* (value.mean - avg);
							}
							double stdDev = Math.sqrt(sumDiff / (n - 1));
							double r = max - min;
							double pg = r / stdDev;
							boolean inRange = inDavidRange(pg, n, probab_123);
							// System.out.println("--------------- PG (n="+n+")= "+pg+" ==> "+(inRange
							// ? "NORMALVERTEILT" : "Nicht normalverteilt!"));
							if (!inRange)
								resultForNode = resultForNode
										+ "// "
										+ ("Time="
												+ timePoint
												+ ", Series="
												+ serie
												+ " ("
												+ "PG="
												+ StringManipulationTools
														.formatNumber(pg,
																"#.##")
												+ ", "
												+ "r="
												+ StringManipulationTools
														.formatNumber(r, "#.##")
												+ ", "
												+ "s="
												+ StringManipulationTools
														.formatNumber(stdDev,
																"#.##") + ", "
												+ "n=" + n + ")");
							if (!inRange)
								allInRange = false;
						}
					}
				}
			AttributeHelper.setToolTipText(node, resultForNode);
			if (!allInRange)
				result.add(node);
			/*
			 * AttributeHelper.setFillColor(node, Color.LIGHT_GRAY); else
			 * AttributeHelper.setFillColor(node, Color.WHITE);
			 */
		}
		return result;
	}
	
	public static boolean inDavidRange(double pg, int n,
			int columnIdx123_5_1_01percent) {
		if (columnIdx123_5_1_01percent < 0 || columnIdx123_5_1_01percent > 2) {
			ErrorMsg.addErrorMessage("Invalid column index! Use 0 for 5 percent, 1 for 1 percent and 2 for 0.1 percent probability!");
			return false;
		}
		if (n > 500) {
			ErrorMsg.addErrorMessage("Too many datapoints (more than 500) for this implementation of the David et. al. normality test.");
			return false;
		}
		double[][] davidTable = {
				// n lower bound upper bound
				// 5% 1% 0.1% 5% 1% 0.1%
				{ 5, 2.15, 2.02, 1.83, 2.83, 2.80, 2.80 },
				{ 6, 2.28, 2.15, 1.83, 3.16, 3.16, 3.10 },
				{ 7, 2.40, 2.26, 1.87, 3.46, 3.46, 3.34 },
				{ 8, 2.50, 2.35, 1.87, 3.74, 3.74, 3.54 },
				{ 9, 2.59, 2.44, 1.90, 4.00, 4.00, 3.72 },
				{ 10, 2.67, 2.51, 1.90, 4.24, 4.24, 3.88 },
				
				{ 11, 2.74, 2.58, 1.92, 4.47, 4.01, 3.80 },
				{ 12, 2.80, 2.64, 1.92, 4.69, 4.13, 3.91 },
				{ 13, 2.85, 2.70, 1.93, 4.90, 4.24, 4.00 },
				{ 14, 2.92, 2.75, 1.93, 5.10, 4.34, 4.09 },
				{ 15, 2.97, 2.80, 1.94, 5.29, 4.44, 4.17 },
				
				{ 16, 3.01, 2.84, 1.94, 5.48, 4.52, 4.24 },
				{ 17, 3.05, 2.88, 1.94, 5.65, 4.60, 4.31 },
				{ 18, 3.10, 2.92, 1.94, 5.83, 4.67, 4.37 },
				{ 19, 3.14, 2.96, 1.95, 6.00, 4.74, 4.43 },
				{ 20, 3.18, 2.99, 1.95, 6.16, 4.80, 4.49 },
				
				{ 25, 3.34, 3.15, 1.96, 6.93, 5.06, 4.71 },
				{ 30, 3.47, 3.27, 1.97, 7.62, 5.26, 4.89 },
				{ 35, 3.58, 3.38, 1.97, 8.25, 5.42, 5.04 },
				{ 40, 3.67, 3.47, 1.98, 8.83, 5.56, 5.16 },
				{ 45, 3.75, 3.55, 1.98, 9.38, 5.67, 5.26 },
				
				{ 50, 3.83, 3.62, 1.98, 9.90, 5.77, 5.35 },
				{ 55, 3.90, 3.69, 1.98, 10.39, 5.86, 5.43 },
				{ 60, 3.96, 3.75, 1.98, 10.86, 5.94, 5.51 },
				{ 65, 4.01, 3.80, 1.98, 11.31, 6.01, 5.57 },
				{ 70, 4.06, 3.85, 1.99, 11.75, 6.07, 5.63 },
				
				{ 75, 4.11, 3.90, 1.99, 12.17, 6.13, 5.68 },
				{ 80, 4.16, 3.94, 1.99, 12.57, 6.18, 5.73 },
				{ 85, 4.20, 3.99, 1.99, 12.96, 6.23, 5.78 },
				{ 90, 4.24, 4.02, 1.99, 13.34, 6.27, 5.82 },
				{ 95, 4.27, 4.06, 1.99, 13.71, 6.32, 5.86 },
				
				{ 100, 4.31, 4.10, 1.99, 14.07, 6.36, 5.90 },
				{ 150, 4.59, 4.38, 1.99, 17.26, 6.64, 6.18 },
				{ 200, 4.78, 4.59, 2.00, 19.95, 6.84, 6.39 },
				{ 500, 5.37, 5.13, 2.00, 31.59, 7.42, 6.94 }
		
		};
		int row = 0;
		for (int i = 0; i < davidTable.length && n > davidTable[i][0]; i++) {
			row++;
		}
		// System.out.println("Range for n="
		// +n+" ["+davidTable[row][columnIdx123_5_1_01percent]
		// +" / "
		// +davidTable[row][columnIdx123_5_1_01percent+3]+"]");
		
		if (pg > davidTable[row][columnIdx123_5_1_01percent]
				&& pg < davidTable[row][columnIdx123_5_1_01percent + 3])
			return true;
		else
			return false;
	}
	
	private static double ttab(double v, double p) {
		return StatisticTable.backwardT(p, (int) v);
	}
	
	private static double getStd(Double[] X, double x_) {
		double sumQuadDiff = 0;
		int n = X.length;
		for (int i = 0; i < n; i++)
			sumQuadDiff += (X[i].doubleValue() - x_)
					* (X[i].doubleValue() - x_);
		return 1 / ((double) n - 1) * sumQuadDiff;
	}
	
	private static double getAVG(Double[] X) {
		double sum = 0;
		int n = X.length;
		for (int i = 0; i < n; i++)
			sum += X[i].doubleValue();
		return sum / n;
	}
	
	private void findCorrelations(final Collection<Node> nodes,
			final Graph graph, final EditorSession session) {
		MyCorrlationFinder cf = new MyCorrlationFinder(nodes, graph, session,
				considerTimeShifts, mergeDataset,
				colorCodeEdgesWithCorrelationValue, minimumR, currGammaValue,
				colR_1, colR0, colR1, correlationEdges, prob, plotAverage,
				rankOrder, showStatusResult, dontAddNewEdgesUpdateOld);
		BackgroundTaskHelper bth = new BackgroundTaskHelper(cf, cf,
				"Find Correlations", "Find Correlations", true, false);
		bth.startWork(this);
	}
	
	public static CorrelationResult calculateCorrelation(
			MyXML_XYDataset dataset, String dataset1, String dataset2,
			boolean mergeDataset, int dataset2offset, double prob,
			boolean rankOrder) {
		return calculateCorrelation(null, dataset, dataset1, dataset2,
				mergeDataset, dataset2offset, prob, rankOrder);
	}
	
	public static CorrelationResult calculateCorrelation(
			SimpleRegression optRegressionModel, MyXML_XYDataset dataset,
			String dataset1, String dataset2, boolean mergeDataset,
			int dataset2offset, double prob, boolean rankOrder) {
		CorrelationResult corrRes = new CorrelationResult(dataset1, dataset2);
		StringBuilder calculationHistory = new StringBuilder();
		double sum_x = 0;
		double sum_y = 0;
		double sum_x_x = 0; // sum x*x
		double sum_y_y = 0; // sum x*x
		double sum_x_y = 0; // sum x*y
		String mergedSeries = "Series: ";
		int n = 0;
		String initString = "<html>Highest significant (>="
				+ (prob * 100)
				+ "%) correlation"
				+ (dataset2offset == 0 ? "" : " (timeshift=" + dataset2offset
						+ ")")
				+ ":<br>"
				+ "<!-- optstart --><small><small><table border=\"1\"><tr><td><b>Series</b></td><td><b>Index "
				+ dataset1 + "</b></td><td><b>Index " + dataset2
				+ "</b></td><td><b>Value " + dataset1
				+ "</b></td><td><b>Value " + dataset2 + "</b></td></tr>\n";
		int maxROW = 1000;
		calculationHistory.append(initString);
		int rowDescription = 0;
		// System.out.println("---");
		for (int series = 0; series < dataset.getSeriesCount(); series++) {
			if (!mergeDataset) {
				calculationHistory = new StringBuilder();
				calculationHistory.append(initString);
				rowDescription = 0;
				sum_x = 0;
				sum_y = 0;
				sum_x_x = 0;
				sum_y_y = 0;
				sum_x_y = 0;
			} else
				mergedSeries += dataset.getSeriesName(series)
						+ (series < dataset.getSeriesCount() - 1 ? ", " : "");
			if (!mergeDataset)
				n = dataset.getItemCount(series);
			else
				n += dataset.getItemCount(series);
			
			for (int item = 0; item < dataset.getItemCount(series); item++) {
				try {
					double x = dataset.getX(series, item, rankOrder,
							mergeDataset, dataset2offset);
					double y = dataset.getY(series, item + dataset2offset,
							rankOrder, mergeDataset, dataset2offset);
					double x_not_ranked;
					if (rankOrder)
						x_not_ranked = dataset.getX(series, item, false,
								mergeDataset, dataset2offset);
					else
						x_not_ranked = x;
					double y_not_ranked;
					if (rankOrder)
						y_not_ranked = dataset.getY(series, item
								+ dataset2offset, false, mergeDataset,
								dataset2offset);
					else
						y_not_ranked = y;
					if (Double.isNaN(x) || Double.isNaN(y))
						n--;
					else {
						if (rowDescription < maxROW)
							calculationHistory
									.append("<tr><td>"
											+ dataset.getSeriesName(series)
											+ "</td><td>"
											+ item
											+ " ("
											+ dataset
													.getXsrcValue(series, item).timeUnitAndTime
											+ ") </td><td>"
											+ (item + dataset2offset)
											+ " ("
											+ dataset.getYsrcValue(series, item
													+ dataset2offset).timeUnitAndTime
											+ ")"
											+ "</td>"
											+ "<td>"
											+ (rankOrder ? StringManipulationTools
													.formatNumber(x_not_ranked,
															"#.###")
													+ " -> " : "")
											+ StringManipulationTools
													.formatNumber(x, "#.###")
											+ "</td>"
											+ "<td>"
											+ (rankOrder ? StringManipulationTools
													.formatNumber(y_not_ranked,
															"#.###")
													+ " -> " : "")
											+ StringManipulationTools
													.formatNumber(y, "#.###")
											+ "</td></tr>\n");
						rowDescription++;
						sum_x += x;
						sum_y += y;
						sum_x_x += x * x;
						sum_y_y += y * y;
						sum_x_y += x * y;
						// System.out.println(x+"\t"+y);
						
						if (optRegressionModel != null)
							optRegressionModel.addData(x, y);
					}
				} catch (IndexOutOfBoundsException ioob) {
					n--;
				}
			}
			if (!mergeDataset) {
				if (n - 2 < 1) {
					ErrorMsg.addErrorMessage("Number of degrees of freedom is too low (DF="
							+ (n - 2)
							+ "; to few datapoints+"
							+ (mergeDataset ? "!"
									: "with matching replicate index!")
							+ "+).<br>Correlation between "
							+ dataset1
							+ " and " + dataset2 + " not calculated!");
				} else {
					calcAndAddResult(dataset2offset, prob, corrRes,
							calculationHistory, sum_x, sum_y, sum_x_x, sum_y_y,
							sum_x_y, dataset.getSeriesName(series), n, maxROW, rowDescription,
							rankOrder);
				}
			}
		}
		if (mergeDataset) {
			calcAndAddResult(dataset2offset, prob, corrRes, calculationHistory,
					sum_x, sum_y, sum_x_x, sum_y_y, sum_x_y, mergedSeries, n,
					maxROW, rowDescription, rankOrder);
		}
		System.out.println("N=" + n);// + " // " + calculationHistory.toString());
		return corrRes;
	}
	
	private static TDistribution tDistribution = new TDistribution(100);
	
	// private static NormalDistribution nDistribution =
	// factory.createNormalDistribution();
	
	private static void calcAndAddResult(int dataset2offset, double prob,
			CorrelationResult corrRes, StringBuilder calculationHistory,
			double sum_x, double sum_y, double sum_x_x, double sum_y_y,
			double sum_x_y, String mergedSeries, int n, int maxROW,
			int rowDescription, boolean spearMan) {
		double sum_d_xx = sum_x_x - sum_x * sum_x / n;
		double sum_d_yy = sum_y_y - sum_y * sum_y / n;
		double sum_dx_dy = sum_x_y - sum_x * sum_y / n;
		float r = (float) (sum_dx_dy / Math.sqrt(sum_d_xx * sum_d_yy));
		// double rtab = getRtabVal(n - 2, 1 - prob);
		double t_or_z = Double.NaN;
		double myp = Double.NaN;
		try {
			double p2;
			if (spearMan) {
				/*
				 * t_or_z = r*Math.sqrt(n-1); p2 =
				 * nDistribution.cumulativeProbability(Math.abs(t_or_z));
				 */
				tDistribution = new TDistribution(n - 2);
				t_or_z = Math.abs(r) / Math.sqrt((1 - r * r) / (n - 2));
				p2 = tDistribution.cumulativeProbability(Math.abs(t_or_z));
			} else {
				tDistribution = new TDistribution(n - 2);
				t_or_z = r / Math.sqrt((1 - r * r) / (n - 2));
				p2 = tDistribution.cumulativeProbability(Math.abs(t_or_z));
			}
			/*
			 * System.out.println("Significance of Pearson:"); for (int tn=4;
			 * tn<30; tn++) { double tdf = tn-2;
			 * tDistribution.setDegreesOfFreedom(tdf);
			 * System.out.println(tn+" "+ AttributeHelper
			 * .formatNumber(transform
			 * (tDistribution.inverseCumulativeProbability (0.05d), tdf),
			 * "#.###")+" "+
			 * AttributeHelper.formatNumber(transform(tDistribution
			 * .inverseCumulativeProbability(0.025d), tdf), "#.###")+" "+
			 * AttributeHelper
			 * .formatNumber(transform(tDistribution.inverseCumulativeProbability
			 * (0.01d), tdf), "#.###")+" "+
			 * AttributeHelper.formatNumber(transform(tDistribution
			 * .inverseCumulativeProbability(0.005d), tdf), "#.###")); }
			 * System.out.println("Significance of Spearman:"); for (int tn=4;
			 * tn<30; tn++) { double tdf = tn-2;
			 * tDistribution.setDegreesOfFreedom(tdf);
			 * System.out.println(tn+" "+ AttributeHelper
			 * .formatNumber(transformspear
			 * (nDistribution.inverseCumulativeProbability (0.05d), tn),
			 * "#.###")+" "+ AttributeHelper.formatNumber(transformspear
			 * (nDistribution.inverseCumulativeProbability(0.025d), tn),
			 * "#.###")+" "+
			 * AttributeHelper.formatNumber(transformspear(nDistribution
			 * .inverseCumulativeProbability(0.01d), tn), "#.###")+" "+
			 * AttributeHelper .formatNumber(transformspear(nDistribution.
			 * inverseCumulativeProbability (0.005d), tn), "#.###")); }
			 */
			myp = 2 * (1 - p2);
		} catch (IllegalArgumentException iae) {
			calculationHistory
					.append("<tr><td colspan=\"5\">CALCULATION (to few datapoints): "
							+ iae.getLocalizedMessage() + "</td></tr>\n");
		} catch (Exception e) {
			calculationHistory
					.append("<tr><td colspan=\"5\">CALCULATION ERROR: "
							+ e.getLocalizedMessage() + "</td></tr>\n");
			ErrorMsg.addErrorMessage(e);
		}
		
		if (rowDescription > maxROW) {
			calculationHistory.append("<tr><td colspan=\"5\">("
					+ (rowDescription - maxROW)
					+ " more rows omitted)</td></tr>\n");
		}
		String warningHeading = "";
		String warningText = "";
		if (spearMan && n < 10) {
			warningHeading = "<td><b>Imprecise P Value!</b></td>";
			warningText = "<small>P value might be imprecise for n&lt;10,<br>it is calculated with an approximation to the t-distribution.";
		}
		calculationHistory
				.append("</table><!-- optend --><br>" + "<table border=\"1\">"
						+ "<tr>" + "<td><b>n</b></td>" + "<td><b>"
						+ (spearMan ? "rs" : "r")
						+ "</b></td>"
						+
						// "<td><b>r-tab</b></td>" +
						(spearMan ?
								// "<td><b>~z</b> = rs<*(n-1)^0.5</td>"
								"<td><b>~t</b> = |rs|/((1-rs^2)/df)^0.5</td>"
								: "<td><b>~t</b> = r/((1-r^2)/df)^0.5</td>")
						+ "<td><b>df</b></td>"
						+ (spearMan ? "<td><b>Probability (non-directional)</b><br><small><small>(approximated to t-distribution)</small><small></td>"
								: "<td><b>Probability (non-directional)</b><br><small><small>(approximated to t-distribution)</small><small></td>")
						+ warningHeading
						+ "</tr>"
						+ "<tr>"
						+ "<td>"
						+ n
						+ "</td>"
						+ "<td>"
						+ StringManipulationTools.formatNumber(r, "#.######")
						+ "</td>"
						+
						// "<td>"+AttributeHelper.formatNumber(rtab,
						// "#.###")+"</td>"
						// +
						"<td>"
						+ StringManipulationTools.formatNumber(t_or_z,
								"#.######")
						+ "</td>"
						+ "<td>"
						+ (n - 2)
						+ "</td>"
						+ "<td>"
						+ StringManipulationTools.formatNumber(myp, "#.######")
						+ "</td>" + warningText + "</tr>" + "</table>");
		corrRes.addR(r, prob, dataset2offset, calculationHistory.toString(),
				mergedSeries, 1 - myp);
	}
	
	// private static double transform(double d, double tdf) {
	// return Math.sqrt(1/(tdf/d/d+1));
	// }
	//
	// private static double transformspear(double z, double tn) {
	// return z/Math.sqrt(tn-1);
	// }
	
	private void visualiseCorrelation(GraphElement referenceGraphElement,
			Graph graph) {
		Collection<GraphElement> allGraphElements = graph.getGraphElements();
		GraphElement ge1 = referenceGraphElement;
		String node1desc = AttributeHelper.getLabel(ge1, "-unnamed-");
		ExperimentInterface mappedDataList1 = Experiment2GraphHelper
				.getMappedDataListFromGraphElement(ge1);
		graph.getListenerManager().transactionStarted(this);
		
		for (GraphElement ge2 : allGraphElements) {
			String node2desc = AttributeHelper.getLabel(ge2, "-unnamed-");
			ExperimentInterface mappedDataList2 = Experiment2GraphHelper
					.getMappedDataListFromGraphElement(ge2);
			if (mappedDataList1 != null && mappedDataList2 != null) {
				Iterator<SubstanceInterface> itXml1 = mappedDataList1
						.iterator();
				Iterator<SubstanceInterface> itXml2 = mappedDataList2
						.iterator();
				MyXML_XYDataset dataset = new MyXML_XYDataset();
				int series = 0;
				while (itXml1.hasNext() && itXml2.hasNext()) {
					series++;
					SubstanceInterface xmldata1 = itXml1.next();
					SubstanceInterface xmldata2 = itXml2.next();
					dataset.addXmlDataSeries(xmldata2, xmldata1, "M" + series,
							plotAverage, null);
				}
				if (ge1 != ge2) {
					CorrelationResult cr = calculateCorrelation(dataset,
							node2desc, node1desc, mergeDataset, 0, prob,
							rankOrder);
					if (showStatusResult)
						AttributeHelper.setToolTipText(ge2, cr.getRlist());
					
					AttributeHelper.setAttribute(ge2, "statistics", "correlation_r", new Double(cr.getMaxR()));
					AttributeHelper.setAttribute(ge2, "statistics", "correlation_prob", new Double(cr.getMaxTrueCorrProb()));
					
					float r = cr.getMaxR();
					AttributeHelper.setFillColor(ge2,
							getRcolor(r, currGammaValue, colR_1, colR0, colR1));
					if (cr.isAnyOneSignificant(minimumR)) {
						AttributeHelper.setBorderWidth(ge2, 5);
					} else
						AttributeHelper.setBorderWidth(ge2, 1);
				} else {
					AttributeHelper.setFillColor(ge2, Color.YELLOW); // getRcolor(0)
					AttributeHelper.setBorderWidth(ge2, 3);
					if (showStatusResult)
						AttributeHelper.setToolTipText(ge2,
								"Reference element for correlation analysis");
				}
			}
		}
		graph.getListenerManager().transactionFinished(this);
		// GraphHelper.issueCompleteRedrawForGraph(graph);
	}
	
	public static Component createScatterPlotBlock(boolean plotAverage,
			boolean tickMarksVisible, boolean showRangeAxis,
			boolean showLegend, double minimumR, float outlineBorderWidth,
			boolean mergeDataset, double prob, boolean rankOrder,
			double currGammaValue, Color colR_1, Color colR0, Color colR1,
			Collection<GraphElement> gEe, Graph graph, boolean returnResult,
			Component lastScatterPlot, JComponent placeForScatter) {
		int x = 0;
		
		ArrayList<GraphElement> graphElements = new ArrayList<GraphElement>();
		for (GraphElement g : gEe) {
			GraphElementHelper geh = new GraphElementHelper(g);
			if (geh.getDataMappings() != null
					&& geh.getDataMappings().size() > 0)
				graphElements.add(g);
		}
		
		int axisFontSize = ((Integer) AttributeHelper.getAttributeValue(graph,
				"", "node_plotAxisFontSize", new Integer(10), new Integer(10)))
				.intValue();
		
		MyScatterBlock scatterBlock = new MyScatterBlock(true, axisFontSize);
		for (Iterator<GraphElement> it1 = graphElements.iterator(); it1
				.hasNext();) {
			x++;
			GraphElement ge1 = it1.next();
			String node1desc;
			if (!(ge1 instanceof Edge))
				node1desc = AttributeHelper.getLabel(ge1, "-?-");
			else {
				Edge nge1 = (Edge) ge1;
				node1desc = AttributeHelper.getLabel(
						ge1,
						AttributeHelper.getLabel(nge1.getSource(), "?")
								+ (nge1.isDirected() ? "->" : "--")
								+ AttributeHelper.getLabel(nge1.getTarget(),
										"?"));
			}
			Iterable<SubstanceInterface> mappedDataList1 = Experiment2GraphHelper
					.getMappedDataListFromGraphElement(ge1);
			int y = 0;
			for (Iterator<GraphElement> it2 = graphElements.iterator(); it2
					.hasNext();) {
				y++;
				GraphElement ge2 = it2.next();
				String node2desc;
				if (!(ge2 instanceof Edge))
					node2desc = AttributeHelper.getLabel(ge2, "-?-");
				else {
					node2desc = AttributeHelper
							.getNiceEdgeOrNodeLabel(ge2, "?");
				}
				System.out.println("INFO: Create scatter block " + x + ":" + y + " (size: " + graphElements.size() + "x" + graphElements.size() + "): " + node1desc
						+ " vs " + node2desc);
				List<SubstanceInterface> mappedDataList2 = Experiment2GraphHelper
						.getMappedDataListFromGraphElement(ge2);
				if (mappedDataList1 != null && mappedDataList2 != null) {
					Iterator<SubstanceInterface> itXml1 = mappedDataList1
							.iterator();
					Iterator<SubstanceInterface> itXml2 = mappedDataList2
							.iterator();
					MyXML_XYDataset dataset = new MyXML_XYDataset();
					if (ge1 != ge2) {
						int series = 0;
						while (itXml1.hasNext() && itXml2.hasNext()) {
							series++;
							SubstanceInterface xmldata1 = itXml1.next();
							SubstanceInterface xmldata2 = itXml2.next();
							dataset.addXmlDataSeries(xmldata2, xmldata1, "M"
									+ series, plotAverage, null);
						}
					}
					JFreeChart chart;
					if (graphElements.size() > 2) {
						// do not include axis labels in case only two
						// substances are compares
						chart = createScatterChart(dataset, null, null, null,
								graph, tickMarksVisible, showRangeAxis,
								showLegend, outlineBorderWidth);
					} else {
						chart = createScatterChart(dataset, null, node2desc,
								node1desc, graph, tickMarksVisible,
								showRangeAxis, showLegend, outlineBorderWidth);
					}
					
					Font af = new Font(
							Axis.DEFAULT_AXIS_LABEL_FONT.getFontName(),
							Axis.DEFAULT_AXIS_LABEL_FONT.getStyle(),
							axisFontSize);
					chart.getXYPlot().getRangeAxis().setTickLabelFont(af);
					chart.getXYPlot().getDomainAxis().setTickLabelFont(af);
					chart.getXYPlot().getDomainAxis().setLabelFont(af);
					chart.getXYPlot().getRangeAxis().setLabelFont(af);
					
					final ChartPanel chartPanel = new ChartPanel(chart, true,
							true, true, true, true);
					
					if (ge1 != ge2) {
						final CorrelationResult cr = calculateCorrelation(
								dataset, node2desc, node1desc, mergeDataset, 0,
								prob, rankOrder);
						chartPanel.setToolTipText("r=" + cr.getMaxOrMinR2());
						chartPanel.addMouseListener(new MouseListener() {
							@Override
							public void mouseClicked(MouseEvent e) {
								if (e.getButton() == MouseEvent.BUTTON1)
									MainFrame.showMessageDialogWithScrollBars(
											cr.getRlist(),
											"Correlation Calculation Result");
								else
									chartPanel.mouseClicked(e);
								if (e.getButton() == MouseEvent.BUTTON3)
									chartPanel.getPopupMenu().show(chartPanel,
											e.getX(), e.getY());
							}
							
							@Override
							public void mousePressed(MouseEvent e) {
								chartPanel.mousePressed(e);
							}
							
							@Override
							public void mouseReleased(MouseEvent e) {
								chartPanel.mouseReleased(e);
							}
							
							@Override
							public void mouseEntered(MouseEvent e) {
								chartPanel.mouseEntered(e);
							}
							
							@Override
							public void mouseExited(MouseEvent e) {
								chartPanel.mouseExited(e);
							}
						});
						if (cr.isAnyOneSignificant(minimumR))
							chartPanel.setBorder(BorderFactory
									.createLineBorder(
											getRcolor(cr.getMaxR(),
													currGammaValue, colR_1,
													colR0, colR1), 3));
						
						else
							chartPanel.setBorder(BorderFactory
									.createLineBorder(
											getRcolor(cr.getMaxR(),
													currGammaValue, colR_1,
													colR0, colR1), 1));
					}
					scatterBlock.addChartPanel(chartPanel, x, y, node1desc,
							node2desc);
				}
			}
		}
		if (returnResult)
			return scatterBlock.getChartPanel();
		
		if (lastScatterPlot != null) {
			if (lastScatterPlot instanceof JPanel) {
				JPanel lsp = (JPanel) lastScatterPlot;
				lsp.removeAll();
			}
			placeForScatter.remove(lastScatterPlot);
		}
		lastScatterPlot = scatterBlock.getChartPanel();
		placeForScatter.add(lastScatterPlot, "1,4");
		lastScatterPlot.getParent().doLayout();
		return lastScatterPlot;
	}
	
	public static Color getRcolor(float maxOrMinR) {
		return getRcolor(maxOrMinR, 1, Color.red, Color.WHITE, Color.blue);
	}
	
	/**
	 * Returns col1 if maxOrMinR is -1, returns col2 if maxOrMinR is 1, returns
	 * a color between these colors if marOrMin is between -1 and 1.
	 * 
	 * @param maxOrMinR
	 *           a value between -1 and 1
	 * @param gamma
	 *           Instead of r, r^gamma is used for determining the color. This
	 *           makes it possible to stay longer near col_0.
	 * @param col1
	 *           The returned color in case maxOrMinR is -1
	 * @param col2
	 *           The returned color in case maxOrMinR is 1
	 * @return A average color depending on maxOrMinR
	 */
	public static Color getRcolor(float maxOrMinR, double gamma, Color col__1,
			Color col_0, Color col_1) {
		Color col1;
		Color col2;
		if (maxOrMinR >= 0) {
			col1 = col_0;
			col2 = col_1;
		} else {
			col1 = col_0;
			col2 = col__1;
		}
		maxOrMinR = Math.abs(maxOrMinR);
		maxOrMinR = (float) Math.pow(maxOrMinR, gamma);
		float red = (col2.getRed() - col1.getRed()) * maxOrMinR + col1.getRed();
		float green = (col2.getGreen() - col1.getGreen()) * maxOrMinR
				+ col1.getGreen();
		float blue = (col2.getBlue() - col1.getBlue()) * maxOrMinR
				+ col1.getBlue();
		float alpha = (col2.getAlpha() - col1.getAlpha()) * maxOrMinR
				+ col1.getAlpha();
		
		if (red < 0)
			red = 0;
		if (green < 0)
			green = 0;
		if (blue < 0)
			blue = 0;
		if (red > 255)
			red = 255;
		if (green > 255)
			green = 255;
		if (blue > 255)
			blue = 255;
		if (alpha < 0)
			alpha = 0;
		if (alpha > 255)
			alpha = 255;
		
		return new Color(red / 255f, green / 255f, blue / 255f, alpha / 255f);
	}
	
	// private Color getRalphaColor(double maxOrMinR, Color color) {
	// int val = (int) (255d * maxOrMinR);
	// if (val > 0)
	// return new Color(color.getRed(), color.getGreen(), color.getBlue(),
	// val);
	// else
	// return new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
	// }
	
	private static JFreeChart createScatterChart(MyXML_XYDataset dataset,
			String title, String labelX, String labelY, Graph graph,
			boolean tickMarksVisible, boolean showRangeAxis,
			boolean showLegend, float outlineBorderWidth) {
		
		// ChartColorAttribute cca = (ChartColorAttribute) AttributeHelper
		// .getAttributeValue(graph, ChartColorAttribute.attributeFolder,
		// ChartColorAttribute.attributeName, new ChartColorAttribute(),
		// new ChartColorAttribute());
		// ArrayList<Color> seriesColors = cca.getSeriesColors();
		// ArrayList<Color> seriesOutlineColors = cca.getSeriesOutlineColors();
		final JFreeChart chart = ChartFactory.createScatterPlot(title, // chart
				// title
				labelX, // domain axis label
				labelY, // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				showLegend, // include legend
				true, // tooltips
				false // urls
				);
		XYPlot p = chart.getXYPlot();
		p.getDomainAxis().setTickLabelsVisible(tickMarksVisible);
		p.getRangeAxis().setTickLabelsVisible(showRangeAxis);
		p.getDomainAxis().setTickMarksVisible(tickMarksVisible);
		p.getRangeAxis().setTickMarksVisible(tickMarksVisible);
		p.setDomainGridlinesVisible(tickMarksVisible);
		p.setRangeGridlinesVisible(showRangeAxis);
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(MyScatterBlock.getBackCol());
		chart.setAntiAlias(IPKGraffitiView.getUseAntialiasingSetting());
		if (p.getRenderer() instanceof CategoryItemRenderer)
			XmlDataChartComponent.setSeriesColorsAndStroke(
					(CategoryItemRenderer) p.getRenderer(), outlineBorderWidth,
					graph); // seriesColors, seriesOutlineColors
		return chart;
	}
	
	protected void refreshEditComponents() {
		if (alphaSpecified)
			jTextFieldAlpha.setText(new Double(alpha).toString());
		jTextFieldProb1findCorr.setText(new Double(prob).toString());
		jTextFieldProb2visCorr.setText(new Double(prob).toString());
		jTextFieldProb3scatter.setText(new Double(prob).toString());
		jTextFieldMinR1.setText(new Double(minimumR).toString());
		jTextFieldMinR2.setText(new Double(minimumR).toString());
		jTextFieldMinR3.setText(new Double(minimumR).toString());
		gammaSlider1vis.setValue(currGammaValue);
		gammaSlider2scatter.setValue(currGammaValue);
		checkBoxPlotAverage1.setSelected(plotAverage);
		checkBoxPlotAverage2.setSelected(plotAverage);
		checkBoxPlotAverage3.setSelected(plotAverage);
		gammaSlider1vis.setValue(currGammaValue);
		gammaSlider2scatter.setValue(currGammaValue);
		gammaSlider3edgeCorr.setValue(currGammaValue);
		for (JButton col1 : col1buttons)
			col1.setBorder(BorderFactory.createLineBorder(colR_1, 3));
		for (JButton col2 : col2buttons)
			col2.setBorder(BorderFactory.createLineBorder(colR0, 3));
		for (JButton col3 : col3buttons)
			col3.setBorder(BorderFactory.createLineBorder(colR1, 3));
	}
	
	@Override
	public boolean visibleForView(View v) {
		return v != null && v instanceof GraphView;
	}
	
}
