package de.ipk.ag_ba.commands.experiment.tools;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.AttributeHelper;
import org.ErrorMsg;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.Vector2i;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.commands.experiment.charting.ActionChartingGroupBySettings;
import de.ipk.ag_ba.commands.experiment.charting.ExperimentTransformationPipeline;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class ActionGenerateFieldInfoFromWeight extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReferenceInterface experiment;
	private NavigationButton src;
	
	public ActionGenerateFieldInfoFromWeight() {
		super("Generate placement field information");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		ChartSettings set = new ChartSettings(true);
		set.setUseLocalSettings(true);
		
		try {
			this.src = src;
			ExperimentInterface res = experiment.getData();
			ExperimentTransformationPipeline pipeline = new ExperimentTransformationPipeline(res);
			ActionChartingGroupBySettings gb = new ActionChartingGroupBySettings(null, pipeline, set, null, null);
			pipeline.setSteps(gb);
			
			gb.performActionCalculateResults(src);
			res = gb.transform(res);
			HashSet<String> plantIDs = new HashSet<>();
			res.visitNumericMeasurements(null, (nmi) -> {
				plantIDs.add(fix(nmi.getQualityAnnotation()));
			});
			Object[] param = MyInputHelper.getInput("Field Layout:", "Field Layout", new Object[] {
					"Number of carriers", plantIDs.size(),
					"Init offset", 0,
					"Carriers in lane", 33,
					"Lanes", 13,
					"Directions of lanes", new String[] {
							DirectionMode.ALL_ONE_DIR.toString(),
							DirectionMode.HALF_ONE_DIR.toString(),
							DirectionMode.ALTERNATING.toString() }
			});
			if (param == null)
				return;
			int carriers = (Integer) param[0];
			int initoffset = (Integer) param[1];
			int carriersInLane = (Integer) param[2];
			int lanes = (Integer) param[3];
			DirectionMode direction = DirectionMode.fromString((String) param[4]);
			status.setCurrentStatusText1("Generate Rank Data");
			TreeMap<Integer, TreeMap<Long, String>> day2time2plant = new TreeMap<>();
			boolean resOK = false;
			for (SubstanceInterface s : res) {
				if (s.getName().equals("weight_before")) {
					for (ConditionInterface ci : s) {
						for (SampleInterface sai : ci) {
							Long time = sai.getSampleFineTimeOrRowId();
							if (time != null) {
								for (NumericMeasurementInterface nmi : sai) {
									if (fix(nmi.getQualityAnnotation()) != null) {
										if (!day2time2plant.containsKey(sai.getTime()))
											day2time2plant.put(sai.getTime(), new TreeMap<>());
										day2time2plant.get(sai.getTime()).put(time, fix(nmi.getQualityAnnotation()));
									}
								}
							}
						}
					}
					resOK = true;
					break;
				}
			}
			if (!resOK)
				throw new RuntimeException("Could not find 'weight_before' data!");
			
			TreeMap<Integer, TreeMap<String, Integer>> day2plant2index = new TreeMap<>();
			for (Integer day : day2time2plant.keySet()) {
				day2plant2index.put(day, new TreeMap<>());
				int idx = 0;
				for (String plant : day2time2plant.get(day).values())
					day2plant2index.get(day).put(plant, (idx++));
			}
			
			status.setCurrentStatusText1("Calculate overall offsets");
			TreeMap<String, TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>> substance2day2x2y2diff = new TreeMap<>();
			for (SubstanceInterface s : res) {
				HashMap<String, TreeMap<Integer, SummaryStatistics>> group2day2values = new HashMap<>();
				int n = 0;
				for (ConditionInterface ci : s) {
					String group = ci.getSpecies();
					if (!group2day2values.containsKey(group))
						group2day2values.put(group, new TreeMap<Integer, SummaryStatistics>());
					for (SampleInterface sai : ci) {
						if (!group2day2values.get(group).containsKey(sai.getTime()))
							group2day2values.get(group).put(sai.getTime(), new SummaryStatistics());
						for (NumericMeasurementInterface nmi : sai) {
							double v = nmi.getValue();
							if (!Double.isNaN(v) && !Double.isInfinite(v))
								group2day2values.get(group).get(sai.getTime()).addValue(v);
						}
					}
					for (SampleInterface sai : ci) {
						for (NumericMeasurementInterface nmi : sai) {
							if (fix(nmi.getQualityAnnotation()) != null) {
								if (Double.isNaN(nmi.getValue()))
									continue;
								Vector2i pos = getPos(fix(nmi.getQualityAnnotation()), day2plant2index, sai.getTime(), carriers,
										initoffset, carriersInLane, lanes, direction);
								if (pos != null) {
									if (!substance2day2x2y2diff.containsKey(s.getName()))
										substance2day2x2y2diff.put(s.getName(), new TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, Double>>>());
									if (!substance2day2x2y2diff.get(s.getName()).containsKey(sai.getTime()))
										substance2day2x2y2diff.get(s.getName()).put(sai.getTime(), new TreeMap<Integer, TreeMap<Integer, Double>>());
									TreeMap<Integer, TreeMap<Integer, Double>> x2y2diff = substance2day2x2y2diff.get(s.getName()).get(sai.getTime());
									int x = pos.x;
									int y = pos.y;
									if (!x2y2diff.containsKey(x))
										x2y2diff.put(x, new TreeMap<>());
									if (!x2y2diff.get(x).containsKey(y))
										if (group2day2values.get(group).get(sai.getTime()).getN() > 0) {
											x2y2diff.get(x).put(y, nmi.getValue() - group2day2values.get(group).get(sai.getTime()).getMean());
											n++;
										}
								}
							}
						}
					}
					if (n == 0)
						substance2day2x2y2diff.remove(s.getName());
				}
			}
			
			TextFile html = new TextFile();
			html.add("<html><head><title>Field Position Differences Map</title></head><head><body>");
			for (String substance : substance2day2x2y2diff.keySet()) {
				html.add("<h1>" + substance + "</h1>");
				TreeMap<Integer, TreeMap<Integer, Double>> x2y2diffSum = new TreeMap<Integer, TreeMap<Integer, Double>>();
				for (Integer day : substance2day2x2y2diff.get(substance).keySet()) {
					TreeMap<Integer, TreeMap<Integer, Double>> x2y2diff = substance2day2x2y2diff.get(substance).get(day);
					for (Integer x : x2y2diff.keySet()) {
						if (!x2y2diffSum.containsKey(x))
							x2y2diffSum.put(x, new TreeMap<Integer, Double>());
						for (Integer y : x2y2diff.get(x).keySet()) {
							if (!x2y2diffSum.get(x).containsKey(y))
								x2y2diffSum.get(x).put(y, 0d);
							x2y2diffSum.get(x).put(y, x2y2diffSum.get(x).get(y) + x2y2diff.get(x).get(y));
						}
					}
				}
				html.add("<table border='1'>");
				int minx = x2y2diffSum.firstKey();
				int maxx = x2y2diffSum.lastKey();
				int miny = Integer.MAX_VALUE;
				int maxy = 0;
				double minval = Double.MAX_VALUE;
				double maxval = -Double.MAX_VALUE;
				for (int x : x2y2diffSum.keySet()) {
					int miy = x2y2diffSum.get(x).firstKey();
					int may = x2y2diffSum.get(x).lastKey();
					if (miy < miny)
						miny = miy;
					if (may > maxy)
						maxy = may;
					for (int y : x2y2diffSum.get(x).keySet()) {
						double v = x2y2diffSum.get(x).get(y);
						if (v < minval)
							minval = v;
						if (v > maxval)
							maxval = v;
					}
				}
				for (int y = miny; y <= maxy; y++) {
					StringBuilder line = new StringBuilder();
					line.append("<tr>");
					for (int x = minx; x <= maxx; x++) {
						if (x2y2diffSum.containsKey(x) && x2y2diffSum.get(x).get(y) != null) {
							float red = 0;
							float green = 0;
							float blue = 0;
							double val = x2y2diffSum.get(x).get(y);
							if (val <= 0) {
								blue = (float) (Math.abs(val) / Math.max(Math.abs(minval), Math.abs(maxval)));
								red = 1f - blue;
								green = 1f - blue;
								blue = 1f;
							} else {
								red = (float) (Math.abs(val) / Math.max(Math.abs(minval), Math.abs(maxval)));
								green = 1f - red;
								blue = 1f - red;
								red = 1f;
							}
							if (Double.isNaN(val))
								line.append("<td style=\"empty-cells: hide\"></td>");
							else
								line.append("<td bgcolor=\"" + StringManipulationTools.getColorHTMLdef(new Color(red, green, blue)) + "\">" +
										(int) val
										+ "</td>");
						} else {
							line.append("<td style=\"empty-cells: hide\"></td>");
						}
					}
					line.append("</tr>");
					html.add(line.toString());
				}
				html.add("</table>");
			}
			html.add("</body></html>");
			status.setCurrentStatusText1("Generate output files");
			{
				File target = OpenFileDialogService.getSaveFile(new String[] { ".html" }, "Result HTML (.html)");
				if (target != null) {
					html.write(target);
					AttributeHelper.showInFileBrowser(target.getParent(), target.getName());
				}
			}
			TextFile t = new TextFile();
			for (Integer day : day2time2plant.keySet()) {
				int idx = 0;
				for (String plantID : day2time2plant.get(day).values()) {
					t.add(day + ";" + (idx++) + ";" + plantID);
				}
			}
			File target = OpenFileDialogService.getSaveFile(new String[] { ".csv" }, "Result Table (.csv)");
			if (target != null) {
				t.write(target);
				AttributeHelper.showInFileBrowser(target.getParent(), target.getName());
			}
			status.setCurrentStatusText1("Processing finished");
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private String fix(String id) {
		if (id == null || !id.contains("_"))
			return id;
		else
			return id.split("_")[0];
	}
	
	private Vector2i getPos(String plantID, TreeMap<Integer, TreeMap<String, Integer>> day2plant2index, int time,
			int carriers,
			int initoffset, int carriersInLane, int lanes,
			DirectionMode direction) {
		Vector2i r = new Vector2i();
		TreeMap<String, Integer> plant2idx = day2plant2index.get(time);
		if (plant2idx == null || !plant2idx.containsKey(plantID))
			return null;
		int idx = plant2idx.get(plantID) + initoffset;
		idx = idx % carriers;
		int resLane = idx / carriersInLane;
		int posInLane = idx % carriersInLane;
		r.x = resLane;
		switch (direction) {
			case ALL_ONE_DIR:
				r.y = posInLane;
				break;
			case ALTERNATING:
				if (resLane % 2 == 0)
					r.y = posInLane;
				else
					r.y = carriersInLane - posInLane;
				break;
			case HALF_ONE_DIR:
				if (resLane < lanes / 2)
					r.y = posInLane;
				else
					r.y = carriersInLane - posInLane;
				break;
			default:
				throw new RuntimeException("Internal Error: Unknown Direction Mode!");
				
		}
		return r;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Placement field generated!");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Format-Justify-Fill-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Generate Placement Table";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experiment = experimentReference;
	}
}