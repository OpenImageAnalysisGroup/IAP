package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.SystemOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.plugins.outlier.data.Handler;

/**
 * @author Christian Klukas
 */
public class ActionPipelineStatus extends AbstractNavigationAction {
	
	private NavigationButton src;
	
	private final LinkedList<JComponent> htmlTextPanels = new LinkedList<JComponent>();
	
	public ActionPipelineStatus(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		htmlTextPanels.clear();
		
		JLabelUpdateReady r = new JLabelUpdateReady() {
			private PipelineMonitoringResult lastRes = null;
			
			@Override
			public void update() {
				BlockPipeline.activateBlockResultMonitoring(SystemOptions.getInstance()
						.getInteger("Pipeline-Debugging", "Block Result-Monitoring//Image-Size", 256), 2000);
				PipelineMonitoringResult pr = BlockPipeline.getLastPipelineMonitoringResults();
				if (pr == null || pr.isEmpty()) {
					setText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Pipeline-Monitoring activated. Waiting for block-rResults.</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br><br>");
				} else {
					if (lastRes == pr)
						return;
					lastRes = pr;
					StringBuilder t = new StringBuilder();
					t.append("<html><table>"
							+ "<tr><th colspan=6 bgcolor='#EE9977'>Block Results (" + pr.getPlantID() + " " + pr.getSnapshotTime() + ")</th></tr>"
							+ "<tr>"
							+ "<th bgcolor='#DDDDDD'>Block</th>"
							+ "<th bgcolor='#DDDDDD'>Visible-Light</th>"
							+ "<th bgcolor='#DDDDDD'>Fluorescence</th>"
							+ "<th bgcolor='#DDDDDD'>Near-Infrared</th>"
							+ "<th bgcolor='#DDDDDD'>Infrared</th>"
							+ "<th bgcolor='#DDDDDD'>Processing time</th>"
							+ "</tr>");
					int n = 0;
					for (BlockMonitoringResult bmr : pr.getBlockResults()) {
						t.append("<tr>"
								+ "<td bgcolor='#FFFFFF'>" + bmr.getBlockName() + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + bmr.getImageHTML(CameraType.VIS) + "&nbsp;" + bmr.getMaskHTML(CameraType.VIS) + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + bmr.getImageHTML(CameraType.FLUO) + "&nbsp;" + bmr.getMaskHTML(CameraType.FLUO) + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + bmr.getImageHTML(CameraType.NIR) + "&nbsp;" + bmr.getMaskHTML(CameraType.NIR) + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + bmr.getImageHTML(CameraType.IR) + "&nbsp;" + bmr.getMaskHTML(CameraType.IR) + "</td>"
								+ "<td bgcolor='#FFFFFF'>" + bmr.getProcessTime() + "</td>"
								+ "</tr>");
						n++;
					}
					if (n == 0)
						t.append("<tr><td colspan=6 bgcolor='#FFFFFF'><center><br>Pipeline-Monitoring Activated. Waiting for Block-Results.<br><br></center></td></tr>");
					
					t.append("</table>");
					String txt = t.toString();
					setText(txt);
				}
			}
		};
		r.setBorder(BorderFactory.createBevelBorder(1));
		htmlTextPanels.add(r);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<>();
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
		Handler.install();
		LinkedList<JComponent> res = new LinkedList<JComponent>(htmlTextPanels);
		return new MainPanelComponent(res, 15);
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
		return "Block Result-Monitoring";
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Video-X-Generic-64.png";
	}
}
