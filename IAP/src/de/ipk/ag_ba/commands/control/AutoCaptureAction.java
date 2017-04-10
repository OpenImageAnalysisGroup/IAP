package de.ipk.ag_ba.commands.control;

import java.util.ArrayList;

import javax.swing.JLabel;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public class AutoCaptureAction extends AbstractNavigationAction {
	
	private AddCapturedImagesToExperiment capture;
	
	public AutoCaptureAction(String tooltip) {
		super(tooltip);
	}
	
	public AutoCaptureAction(AddCapturedImagesToExperiment capture) {
		this("Create time-series (auto-capture)");
		this.capture = capture;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] res = MyInputHelper.getInput("Time-series settings:", "Create time series", new Object[] {
				"Number of time points", 0,
				"<html>Time interval, time<br>between imaging:", new JLabel(),
				"Hours", 0,
				"Minutes", 0,
				"Seconds", 0,
				"Initial delay (sec)", 10
		});
		if (res != null) {
			int n = (int) res[0];
			int h = (int) res[2];
			int m = (int) res[3];
			int s = (int) res[4];
			int is = (int) res[5];
			
			status.setCurrentStatusText1("About to capture " + n + " time points");
			long st = System.currentTimeMillis();
			while (System.currentTimeMillis() - st < 1000 * is) {
				status.setCurrentStatusText2("<html><center><small>(start capture in " + (int) (is - (System.currentTimeMillis() - st) / 1000) + " sec.)");
				Thread.sleep(100);
				if (status.wantsToStop())
					break;
			}
			
			int overallS = s + m * 60 + h * 60 * 60;
			long intervallTime = overallS * 1000;
			long startTime = System.currentTimeMillis();
			long endTime = startTime + intervallTime * n;
			
			if (!status.wantsToStop())
				mainLoop: for (int i = 0; i < n; i++) {
					status.setCurrentStatusValueFine(i / n * 100d);
					capture.performActionCalculateResults(src);
					status.setCurrentStatusText1("time point " + (i + 1) + "/" + n);
					while (System.currentTimeMillis() < startTime + (i + 1) * intervallTime) {
						status.setCurrentStatusText2("<html><center><small>(next capture in " + (startTime + (i + 1) * intervallTime - System.currentTimeMillis()) / 1000 + " sec.)");
						Thread.sleep(100);
						if (status.wantsToStop())
							break mainLoop;
					}
				}
			status.setCurrentStatusValueFine(100d);
			status.setCurrentStatusText2("");
		}
		status.reset();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>Create time<br>series";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Video-X-Generic-64.png";
	}
	
	@Override
	public boolean mayRun() {
		return capture.mayRun();
	}
}
