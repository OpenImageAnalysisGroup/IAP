package de.ipk_gatersleben.ag_pbi.mmd.fluxdata;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.Colors;
import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.view.View;
import org.graffiti.plugins.editcomponents.defaults.EdgeArrowShapeEditComponent;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.SplitNodeForSingleMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.EdgeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.circle.DotLayoutAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.svg_exporter.PngJpegAlgorithm;
import de.ipk_gatersleben.ag_pbi.mmd.JComboBoxAutoCompleteAndSelectOnTab;

public class VisualiseFluxDataAlgorithm extends AbstractEditorAlgorithm {
	private ConditionInterface selectedCondition;
	private SampleInterface selectedSample;
	private double globalMultiplicator = 1.0;
	private double headTailRatio = 1.5;
	private boolean showMeasurmentQuality = true;
	private Color lowestUncertainy = Color.BLACK;
	private Color highestUncertainy = Color.RED;
	private double minQuality;
	private double maxQuality;
	private TreeSet<ConditionInterface> uniqueconditions;
	private ArrayList<SampleInterface> samples;
	public boolean removeEdgeCharts = new Boolean(true);
	public boolean removeEdgeBends = new Boolean(true);
	public boolean circReactNodes = new Boolean(false);
	
	@Override
	public boolean activeForView(View v) {
		return v != null;
	}
	
	@Override
	public void check() throws PreconditionException {
		if (graph == null)
			throw new PreconditionException("Graph is null!");
		
		boolean atleastOneEdgeHasMappingData = false;
		
		maxQuality = Double.NEGATIVE_INFINITY;
		minQuality = Double.MAX_VALUE;
		HashSet<ConditionInterface> seriesIDCount = new HashSet<ConditionInterface>();
		TreeSet<SampleInterface> samplessorted = new TreeSet<SampleInterface>();
		
		for (Edge edge : graph.getEdges()) {
			
			ExperimentInterface exp = getDataMappings(edge);
			if (exp != null)
				for (SubstanceInterface s : exp)
					for (ConditionInterface c : s) {
						for (SampleInterface sa : c) {
							samplessorted.add(sa);
							if (!sa.iterator().hasNext())
								continue;
							NumericMeasurementInterface m = sa.iterator().next();
							
							if (s.getName().contains("^"))
								atleastOneEdgeHasMappingData = true;
							
							if (m.getQualityAnnotation() != null) {
								try {
									double val = Double.valueOf(m.getQualityAnnotation());
									if (val > maxQuality)
										maxQuality = val;
									if (val < minQuality)
										minQuality = val;
								} catch (Exception e) {
									ErrorMsg.addErrorMessage("Could not parse quality annotation of edge " + edge);
								}
							}
						}
						seriesIDCount.add(c);
					}
		}
		
		if (!atleastOneEdgeHasMappingData)
			throw new PreconditionException("<html>Graph does not contain flux data. Please use the template<br>" +
																"from \"Experiments\" tab -> Data Input Templates");
		
		samples = new ArrayList<SampleInterface>(samplessorted);
		uniqueconditions = new TreeSet<ConditionInterface>(seriesIDCount);
		
	}
	
	@Override
	public String getName() {
		return "Flux Visualisation";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public void execute() {
		MyInputHelper.getInput("[nonmodal,Close]", "Flux Visualisation Options", new Object[] { null, createPanel() });
		redraw(false);
	}
	
	@SuppressWarnings("unchecked")
	private JPanel createPanel() {
		selectedCondition = uniqueconditions.iterator().next();
		selectedSample = samples.get(0);
		int leftSize = 120;
		
		JPanel fluxPanel = new JPanel();
		fluxPanel.setLayout(new BoxLayout(fluxPanel, BoxLayout.Y_AXIS));
		
		final JSpinner mulSpinner = new JSpinner(new SpinnerNumberModel(globalMultiplicator, -100d, 100d, 0.1d));
		mulSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				globalMultiplicator = (Double) mulSpinner.getValue();
				redraw(false);
			}
		});
		fluxPanel.add(TableLayout.getSplit(new JLabel("Multiplicator"), mulSpinner, leftSize, TableLayout.FILL));
		
		final JSpinner arrowSpinner = new JSpinner(new SpinnerNumberModel(headTailRatio, 0d, 10d, 0.1d));
		arrowSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				headTailRatio = (Double) arrowSpinner.getValue();
				redraw(false);
			}
		});
		fluxPanel.add(TableLayout.getSplit(new JLabel("Head/Tail ratio"), arrowSpinner, leftSize, TableLayout.FILL));
		fluxPanel.add(new JPanel());
		
		boolean qualityFound = minQuality != Double.NEGATIVE_INFINITY && minQuality != Double.MAX_VALUE;
		
		final JCheckBox activateQualityColor = new JCheckBox("Show", showMeasurmentQuality);
		activateQualityColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMeasurmentQuality = activateQualityColor.isSelected();
				redraw(false);
			}
		});
		JButton colorChooserBtn = new JButton("Colors");
		colorChooserBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] res = MyInputHelper.getInput(
							"<html>Please specify the colors for the<br>" +
									"respective quality values.<br>&nbsp;",
							"Choose Quality Colors",
							new Object[] {
											"" + minQuality, lowestUncertainy,
											"" + maxQuality, highestUncertainy,
					});
				
				if (res != null) {
					int i = 0;
					lowestUncertainy = (Color) res[i++];
					highestUncertainy = (Color) res[i++];
				}
				if (showMeasurmentQuality)
					redraw(false);
			}
		});
		
		activateQualityColor.setEnabled(qualityFound);
		colorChooserBtn.setEnabled(qualityFound);
		fluxPanel.add(TableLayout.get3Split(new JLabel("Quality options"), activateQualityColor, colorChooserBtn, leftSize, TableLayout.PREFERRED,
					TableLayout.FILL));
		
		final JButton validate = new JButton("Validate Flux Balance");
		validate.setToolTipText("<html>Checks for all reactions, if the ingoing fluxes equals the ingoing fluxes.<br>" +
												"If this is not the case for a reaction, this might indicate an error in the<br>" +
												"template (e.g. substance weight missing, typing errors: Co2 instead of CO2,...)");
		validate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<Node> unbrnds = new ArrayList<Node>();
				Collection<Node> rnds = getReactionNodes();
				for (Node nd : rnds) {
					double in = 0d, out = 0d;
					for (Edge ed : nd.getEdges()) {
						// ignore all edges with dashed lines, because the fluxes are 0 then
						if (AttributeHelper.getDashInfo(ed) != null)
							continue;
						// no arrow-head -> flux going into reaction node
						if (AttributeHelper.getArrowhead(ed).length() < 1 && AttributeHelper.getArrowtail(ed).length() < 1) {
							in += AttributeHelper.getFrameThickNess(ed);
							continue;
						}
						boolean directsaway = ed.getSource() == nd;
						boolean directionishead = AttributeHelper.getArrowhead(ed).length() > 0;
						if ((directsaway && directionishead) || (!directsaway && !directionishead))
							out += AttributeHelper.getFrameThickNess(ed);
						else
							in += AttributeHelper.getFrameThickNess(ed);
					}
					if (Math.abs(in - out) > 0.001) {
						MainFrame.showMessageDialog("" +
								"<html>Found and selected unbalanced reaction \"" + AttributeHelper.getLabel(nd, "error") + "\"<br>" +
								"Ingoing mass: " + in + ", outgoing mass: " + out, "Unbalanced Reaction(s) Found");
						unbrnds.add(nd);
					}
				}
				if (unbrnds.size() > 0) {
					Selection sel = new Selection("unbalanced reactions");
					sel.addAll(unbrnds);
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
				} else
					MainFrame.showMessageDialog("All " + rnds.size() + " reactions are balanced", "Validation Successful");
			}
		});
		fluxPanel.add(new JPanel());
		fluxPanel.add(TableLayout.getSplit(null, validate, 0, TableLayout.FILL));
		
		final JButton makeSnapshot = new JButton("Create Snapshot");
		makeSnapshot.setToolTipText("<html>Creates a PNG-snapshot of the actual graph");
		makeSnapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GravistoService.getInstance().runAlgorithm(new PngJpegAlgorithm(false), getGraph(), new Selection(), null);
			}
		});
		fluxPanel.add(TableLayout.getSplit(null, makeSnapshot, 0, TableLayout.FILL));
		
		fluxPanel.add(new JPanel());
		
		JPanel optionPanel = new JPanel();
		optionPanel.setBorder(BorderFactory.createTitledBorder("Graph options"));
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		
		final JButton splitSelectedNodes = new JButton("Split frequent nodes");
		splitSelectedNodes.setToolTipText("<html>By default a substance is represented by exactly one node.<br>" +
																"If your model contains a common substance (such as ATP or CO2),<br>" +
																"many edges will be connected to the node and thereby cluttering<br>" +
																"the graph. This command will create new node copies for each edge");
		splitSelectedNodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Collection<Node> substnds = new ArrayList<Node>(getGraph().getNodes());
				substnds.removeAll(getReactionNodes());
				
				ArrayList<Node> node2edgeCnt = new ArrayList<Node>();
				int degree = 10;
				while (degree > 2 && node2edgeCnt.size() < 5) {
					node2edgeCnt.clear();
					degree--;
					for (Node nd : substnds)
						if (nd.getDegree() > degree)
							node2edgeCnt.add(nd);
				}
				
				Object[] param = new Object[node2edgeCnt.size() * 2];
				int cnt = 0;
				for (Node nd : node2edgeCnt) {
					param[cnt++] = AttributeHelper.getLabel(nd, "error");
					param[cnt++] = new Boolean(false);
				}
				
				Object[] res = MyInputHelper.getInput(
							"<html>A list of substances is given, which show a high degree<br>" +
									"of interconnection. These might be common substances such as<br>" +
									"ATP or CO2. By selecting a substance, the corresponding node will<br>" +
									"be split into many nodes, improving the layout of the graph.<br>&nbsp;",
							"Choose Nodes For Splitting",
							param);
				
				if (res != null && res.length > 0) {
					getGraph().getListenerManager().transactionStarted(splitSelectedNodes);
					try {
						for (int i = 0; i < res.length; i++)
							if ((Boolean) res[i])
								SplitNodeForSingleMappingData.splitNodes(node2edgeCnt.get(i), 1, getGraph(), true, true);
					} finally {
						getGraph().getListenerManager().transactionFinished(splitSelectedNodes);
					}
				}
				
			}
		});
		// final JButton rotategraph = new JButton("Rotate Graph");
		// rotategraph.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// GravistoService.getInstance().runAlgorithm(new RotateAlgorithm(), getGraph(), new Selection(), null);
		// }
		// });
		final JButton prettifyEdges = new JButton("Prettify Graph");
		prettifyEdges.setToolTipText("<html>Graph may be prettified by:<br><ul>" +
				"<li>Removing charts mapped onto the edges (because fluxes will be represented as thickness)</li>" +
				"<li>All edge bends will be deleted (use \"Edge - Bends...\" menu to introduce bends)</li>" +
				"<li>Switch between the representation of reaction nodes: normal or circular</li>");
		prettifyEdges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] result = MyInputHelper.getInput(
						"Please choose what to prettify",
						"Specify Parameters",
						new Object[] {
								"Remove Edge Charts", removeEdgeCharts,
								"Remove Edge Bends", removeEdgeBends,
								"Nice reaction nodes", circReactNodes,
						}
						);
				if (result != null) {
					removeEdgeCharts = (Boolean) result[0];
					removeEdgeBends = (Boolean) result[1];
					circReactNodes = (Boolean) result[2];
					if (removeEdgeCharts)
						for (Edge ed : getGraph().getEdges())
							if (EdgeHelper.hasMappingData(ed))
								NodeTools.setNodeComponentType(ed, GraffitiCharts.HIDDEN.getName());
					if (removeEdgeBends)
						GraphHelper.removeBends(getGraph(), getGraph().getEdges(), true);
					
					// circReactNodes will be treated in redraw()
					redraw(true);
				}
				
			}
		});
		
		optionPanel.add(TableLayout.getSplit(splitSelectedNodes, prettifyEdges, TableLayout.FILL, TableLayout.FILL));
		
		// final JButton decreasenodesistance = new JButton("Decrease node distance");
		// decreasenodesistance.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// ExpandReduceLayouterAlgorithm.doOperation(getSelectedNodes(), 1 / 1.1, 1 / 1.1, "Decrease Space");
		// }
		// });
		// final JButton increasenodesistance = new JButton("Increase node distance");
		// increasenodesistance.addActionListener(new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// ExpandReduceLayouterAlgorithm.doOperation(getSelectedNodes(), 1.1, 1.1, "Increase Space");
		// }
		// });
		// optionPanel.add(TableLayout.getSplit(decreasenodesistance, increasenodesistance, TableLayout.FILL, TableLayout.FILL));
		
		final JButton selectReactionNodes = new JButton("Select reaction nodes");
		selectReactionNodes.setToolTipText("<html>All reaction nodes will be selected. By using this selection,<br>" +
																"properties such as color and size may be altered.");
		selectReactionNodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Selection sel = new Selection("reactionnodes");
				sel.addAll(getReactionNodes());
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(sel);
				MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
			}
		});
		
		final JButton layout = new JButton("Layout Graph");
		layout.setToolTipText("<html>Layouts the graph with the DOT-Layout from http://www.graphviz.org/");
		layout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (DotLayoutAlgorithm.isInstalled()) {
					GravistoService.getInstance().runAlgorithm(getDotLayouter(), getGraph(), new Selection(), null);
				} else {
					JPanel message = new JPanel();
					message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
					JLabel text = new JLabel("<html>" +
										"Could not detect DOT-Layout.<br>" +
										"Please use the \"Layout\" tab or download and install<br>" +
										"the GraphViz package from the following website:<br><br>");
					
					JLabelHTMLlink link = new JLabelHTMLlink("http://www.graphviz.org/", "http://www.graphviz.org/");
					message.add(text);
					message.add(link);
					JOptionPane.showMessageDialog(MainFrame.getInstance(), message, "DOT-Layout not found!", JOptionPane.ERROR_MESSAGE);
				}
				
			}
		});
		optionPanel.add(TableLayout.getSplit(selectReactionNodes, layout, TableLayout.FILL, TableLayout.FILL));
		
		fluxPanel.add(optionPanel);
		
		fluxPanel.add(new JPanel());
		
		if (uniqueconditions.size() > 1) {
			
			final JComboBoxAutoCompleteAndSelectOnTab conditionBox = new JComboBoxAutoCompleteAndSelectOnTab();
			for (ConditionInterface chosencondition : uniqueconditions)
				conditionBox.addItem(chosencondition, "<html>" + chosencondition.getSpecies() + "<br>" + chosencondition.getGenotype() + "<br>"
						+ chosencondition.getTreatment());
			
			conditionBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					selectedCondition = (ConditionInterface) conditionBox.getSelectedItem();
					redraw(false);
				}
			});
			fluxPanel.add(TableLayout.getSplit(new JLabel("Conditions"), conditionBox, leftSize, TableLayout.FILL));
			fluxPanel.add(new JPanel());
		}
		
		if (samples.size() > 1) {
			final JSlider jslider = new JSlider(0, samples.size() - 1, 0);
			Dictionary<Integer, JLabel> labels = jslider.getLabelTable();
			if (labels == null)
				labels = new Hashtable<Integer, JLabel>();
			int cnt = 0;
			for (SampleInterface sa : samples)
				labels.put(cnt++, new JLabel(sa.getSampleTime()));
			
			jslider.setLabelTable(labels);
			jslider.setMajorTickSpacing(1);
			jslider.setPaintTicks(true);
			jslider.setPaintLabels(true);
			jslider.setSnapToTicks(true);
			jslider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent arg0) {
					selectedSample = samples.get(jslider.getValue());
					redraw(false);
				}
			});
			fluxPanel.add(TableLayout.getSplit(new JLabel("Timepoints"), jslider, leftSize, TableLayout.FILL));
			fluxPanel.add(new JPanel());
		}
		
		return fluxPanel;
	}
	
	protected Graph getGraph() {
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		if (es != null && es.getGraph() != null)
			return es.getGraph();
		else {
			MainFrame.showMessageDialog("Please close flux visualisation dialog.", "All graphs closed");
			return new AdjListGraph();
		}
	}
	
	protected Algorithm getDotLayouter() {
		DotLayoutAlgorithm alg = new DotLayoutAlgorithm();
		Parameter[] params = alg.getParameters();
		params[3].setValue("Top-Down");
		return alg;
	}
	
	protected ArrayList<Node> getReactionNodes() {
		HashSet<String> reactionNames = new HashSet<String>();
		for (Edge edge : getGraph().getEdges()) {
			ExperimentInterface exp = getDataMappings(edge);
			if (exp != null)
				for (SubstanceInterface s : exp)
					reactionNames.add(s.getInfo());
		}
		ArrayList<Node> rnds = new ArrayList<Node>();
		for (Node nd : getGraph().getNodes()) {
			String lbl = AttributeHelper.getLabel(nd, null);
			if (lbl != null && reactionNames.contains(lbl))
				rnds.add(nd);
		}
		return rnds;
	}
	
	private void redraw(boolean forceredraw) {
		if (forceredraw || getGraph().getEdges().size() > 500) {
			getGraph().getListenerManager().transactionStarted(this);
			try {
				doRedraw();
			} finally {
				getGraph().getListenerManager().transactionFinished(this, true);
				GraphHelper.issueCompleteRedrawForGraph(getGraph());
			}
		} else
			doRedraw();
	}
	
	private void doRedraw() {
		getGraph().setModified(true);
		ArrayList<Node> nds = getReactionNodes();
		
		for (Edge ed : getGraph().getEdges()) {
			boolean edgechanged = false;
			boolean edgepointstoReactionnode = nds.contains(ed.getTarget());
			ExperimentInterface exp = getDataMappings(ed);
			for (SubstanceInterface sub : exp)
				for (ConditionInterface con : sub)
					if (con.compareTo(selectedCondition) == 0) {
						for (SampleInterface sam : con)
							if (sam.compareTo(selectedSample) == 0) {
								if (!sam.iterator().hasNext())
									continue;
								NumericMeasurementInterface m = sam.iterator().next();
								
								double absoluteValue = Math.abs(m.getValue() * globalMultiplicator);
								
								if (absoluteValue < 0.000001) { // flux is nearly 0 -> gets dashed line
									AttributeHelper.setDashInfo(ed, 5, 10);
									AttributeHelper.setFrameThickNess(ed, 3);
									AttributeHelper.setArrowSize(ed, 3 * headTailRatio);
								} else {
									AttributeHelper.setDashInfo(ed, null);
									AttributeHelper.setFrameThickNess(ed, absoluteValue);
									AttributeHelper.setArrowSize(ed, absoluteValue * headTailRatio);
								}
								
								if (m.getValue() * globalMultiplicator < 0) {
									AttributeHelper.setArrowhead(ed, "");
									AttributeHelper.setArrowtail(ed, edgepointstoReactionnode ? EdgeArrowShapeEditComponent.standardArrow : "");
								} else {
									AttributeHelper.setArrowhead(ed, edgepointstoReactionnode ? "" : EdgeArrowShapeEditComponent.standardArrow);
									AttributeHelper.setArrowtail(ed, "");
								}
								
								if (m.getQualityAnnotation() != null && showMeasurmentQuality) {
									Color color = Colors.getColor(new Float((Double.parseDouble(m.getQualityAnnotation()) - minQuality) / maxQuality),
											1d, lowestUncertainy, highestUncertainy);
									AttributeHelper.setFillColor(ed, color);
									AttributeHelper.setOutlineColor(ed, color);
								} else {
									AttributeHelper.setFillColor(ed, Color.BLACK);
									AttributeHelper.setOutlineColor(ed, Color.BLACK);
								}
								edgechanged = true;
								break;
							}
					}
			if (!edgechanged && exp != null && !exp.isEmpty()) {
				AttributeHelper.setDashInfo(ed, 5, 10);
				AttributeHelper.setFrameThickNess(ed, 3);
				AttributeHelper.setArrowSize(ed, 3 * headTailRatio);
				AttributeHelper.setFillColor(ed, Color.green);
				AttributeHelper.setOutlineColor(ed, Color.green);
			}
			
		}
		
		for (Node rnd : nds)
			if (circReactNodes) {
				double size = 0;
				for (Edge ed : rnd.getAllInEdges())
					size += AttributeHelper.getFrameThickNess(ed);
				FluxreactionAttribute.setNiceReaction(rnd, size);
				AttributeHelper.setSize(rnd, 5, 5);
			} else {
				FluxreactionAttribute.removeNicereaction(rnd);
				AttributeHelper.setSize(rnd, 25, 25);
			}
		
	}
	
	private ExperimentInterface getDataMappings(Edge edge) {
		return new GraphElementHelper(edge).getDataMappings();
	}
	
	// private Collection<Node> getSelectedNodes() {
	// Collection<Node> nds = ((EditorSession) MainFrame.getInstance().getEditorSessionForGraph(getGraph())).getSelectionModel().getActiveSelection()
	// .getNodes();
	// if (nds.size() <= 0)
	// return getGraph.getNodes();
	// else
	// return nds;
	// }
}