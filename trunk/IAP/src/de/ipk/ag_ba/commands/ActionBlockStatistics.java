package de.ipk.ag_ba.commands;

import iap.blocks.data_structures.AbstractImageAnalysisBlockFIS;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.ImageAnalysisBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.commons.lang3.text.WordUtils;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

/**
 * @author Christian Klukas
 */
public class ActionBlockStatistics extends AbstractNavigationAction {
	
	private NavigationButton src;
	
	private final LinkedList<JComponent> htmlTextPanels = new LinkedList<JComponent>();
	
	public ActionBlockStatistics(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		htmlTextPanels.clear();
		
		JLabelUpdateReady r = new JLabelUpdateReady() {
			@Override
			public void update() {
				LinkedHashMap<String, ThreadSafeOptions> property2exectime = AbstractImageAnalysisBlockFIS.getBlockStatistics();
				StringBuilder t = new StringBuilder();
				t.append("<html><center><h3>"
						+ WordUtils.capitalize("block processing steps statistics")
						+ "</h3></center><hr><table><tr><th>Property</th><th>Execution Time</th><th>Processed Blocks</th><th>Average</th></tr>");
				for (String key : property2exectime.keySet()) {
					if (!key.contains("//")) {
						// statistics within the blocks (prepare/vis/fluo/nir/ir/postprocess)
						ThreadSafeOptions o = property2exectime.get(key);
						t.append("<tr><td>" + key + "</td><td>" + o.getLong() + " ms (" + SystemAnalysis.getWaitTime(o.getLong()) + ")</td>"
								+ "<td>" + o.getInt() + "</td><td>" + (o.getInt() > 0 ? (o.getLong() / o.getInt()) : "-") + " ms</td></tr>");
						// type of stat | overall sum of execution time: x h y m (xx ms) | execution count: 5000 | average execution time: <1 s (200 ms)
					}
					
				}
				t.append("</table>");
				String txt = t.toString();
				setText(txt);
			}
		};
		r.setBorder(BorderFactory.createEtchedBorder());
		htmlTextPanels.add(r);
		
		LinkedHashMap<String, ThreadSafeOptions> property2exectime = AbstractImageAnalysisBlockFIS.getBlockStatistics();
		LinkedHashSet<String> blockStat = new LinkedHashSet<String>();
		for (String key : property2exectime.keySet()) {
			if (key.contains("//"))
				blockStat.add(key);
		}
		
		LinkedHashSet<String> types = new LinkedHashSet<String>();
		for (String key : blockStat) {
			String type = key.split("//")[0];
			types.add(type);
		}
		
		for (String type : types) {
			
			final BlockType valid_bt = BlockType.valueOf(type);
			JLabelUpdateReady rr = new JLabelUpdateReady() {
				@Override
				public void update() {
					LinkedHashMap<String, ThreadSafeOptions> property2exectime = AbstractImageAnalysisBlockFIS.getBlockStatistics();
					LinkedHashSet<String> blockStat = new LinkedHashSet<String>();
					for (String key : property2exectime.keySet()) {
						if (key.contains("//"))
							blockStat.add(key);
					}
					StringBuilder t = new StringBuilder();
					TreeMap<BlockType, LinkedHashMap<String, ThreadSafeOptions>> blockType2block = new TreeMap<BlockType, LinkedHashMap<String, ThreadSafeOptions>>();
					for (String key : blockStat) {
						String type = key.split("//")[0];
						String block = key.split("//")[1];
						
						BlockType bt = BlockType.valueOf(type);
						if (!blockType2block.containsKey(bt))
							blockType2block.put(bt, new LinkedHashMap<String, ThreadSafeOptions>());
						
						blockType2block.get(bt).put(block, property2exectime.get(key));
					}
					
					BlockType bt = valid_bt;
					String nn = bt + "";
					nn = StringManipulationTools.stringReplace(nn, "_", "-") + "";
					nn = nn.toLowerCase();
					nn = WordUtils.capitalize(nn);
					
					t.append("<html><table><tr><th colspan=4 bgcolor='"
							+ bt.getColor()
							+ "'><b>" + nn + "</b></th><tr>"
							+ "<th bgcolor='#DDDDDD'>Property</th>"
							+ "<th bgcolor='#DDDDDD'>Execution Time</th>"
							+ "<th bgcolor='#DDDDDD'>Runs</th>"
							+ "<th bgcolor='#DDDDDD'>Average</th></tr>");
					for (String key : blockType2block.get(bt).keySet()) {
						ThreadSafeOptions o = blockType2block.get(bt).get(key);
						String title = key;
						try {
							Class c = Class.forName(title);
							Object oo = c.newInstance();
							ImageAnalysisBlock iab = (ImageAnalysisBlock) oo;
							title = iab.getName();
						} catch (Exception e) {
							title = title + " (name could not be determined)";
						}
						t.append("<tr><td bgcolor='#FFFFFF'>" + title + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + o.getLong() + " ms (" + SystemAnalysis.getWaitTime(o.getLong()) + ")</td>"
								+ "<td bgcolor='#FFFFFF'>" + o.getInt() + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + (o.getInt() > 0 ? o.getLong() / o.getInt() : "-") + " ms</td></tr>");
					}
					t.append("</table>");
					String txt = t.toString();
					setText(txt);
				}
			};
			rr.setBorder(BorderFactory.createBevelBorder(1));
			htmlTextPanels.add(rr);
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(new ActionResetBlockTimings("Reset block timing information"), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.addAll(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(htmlTextPanels, 15);
	}
	
	long lastRequest = 0;
	
	@Override
	public String getDefaultTitle() {
		if (System.currentTimeMillis() - lastRequest > 1000) {
			for (JComponent jc : htmlTextPanels) {
				JLabelUpdateReady ur = (JLabelUpdateReady) jc;
				ur.update();
			}
			lastRequest = System.currentTimeMillis();
		}
		return "Block Execution Statistics";
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Appointment-Soon-64_flipped.png";
	}
}
