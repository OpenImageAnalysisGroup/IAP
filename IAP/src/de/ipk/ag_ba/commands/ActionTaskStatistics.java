package de.ipk.ag_ba.commands;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import org.SystemAnalysis;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.system.RunnerThread;
import de.ipk.ag_ba.gui.picture_gui.system.ThreadManager;

/**
 * @author Christian Klukas
 */
public class ActionTaskStatistics extends AbstractNavigationAction {
	
	private NavigationButton src;
	
	private final LinkedList<JComponent> htmlTextPanels = new LinkedList<JComponent>();
	
	private int shown_groups = -1;
	
	public ActionTaskStatistics(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		htmlTextPanels.clear();
		
		JLabelUpdateReady r = new JLabelUpdateReady() {
			@Override
			public void update() {
				Collection<RunnerThread> jobs = ThreadManager.getInstance().getRunningTasks();
				if (jobs.isEmpty()) {
					setText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>No Background-Task active.</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br><br>");
				} else {
					StringBuilder t = new StringBuilder();
					t.append("<html><table>"
							+ "<tr><th colspan=7 bgcolor='#EE9977'>" + WordUtils.capitalize("Background Threads") + "</th></tr>"
							+ "<tr>"
							+ "<th bgcolor='#DDDDDD'>No.</th>"
							+ "<th bgcolor='#DDDDDD'>Thread State</th>"
							+ "<th bgcolor='#DDDDDD'>Thread Uptime</th>"
							+ "<th bgcolor='#DDDDDD'>Stop Requested?</th>"
							+ "<th bgcolor='#DDDDDD'>Current Task</th>"
							+ "<th bgcolor='#DDDDDD'>Task Runtime</th>"
							+ "<th bgcolor='#DDDDDD'>Task Exceptions</th>"
							+ "</tr>");
					int n = 0;
					int nWait = 0, nBlocked = 0, nSleep = 0, nRun = 0, nTerm = 0;
					for (RunnerThread job : jobs) {
						if (job != null) {
							State st = job.getState();
							if (st == State.WAITING)
								nWait++;
							if (st == State.BLOCKED)
								nBlocked++;
							if (st == State.TIMED_WAITING)
								nSleep++;
							if (st == State.RUNNABLE)
								nRun++;
							if (st == State.TERMINATED)
								nTerm++;
							t.append("<tr>"
									+ "<td bgcolor='#FFFFFF'>" + (1 + job.getIndex()) + "</td>"
									+ "<td bgcolor='#FFFFFF'>" + st + "</td>"
									+ "<td bgcolor='#FFFFFF'>" + SystemAnalysis.getWaitTime(job.getUptime()) + "</td>"
									+ "<td bgcolor='#FFFFFF'>" + (job.stopRequested() ? "yes" : "-") + "</td>"
									+ "<td bgcolor='#FFFFFF'>" + job.getCurrentTaskName() + "</td>"
									+ "<td bgcolor='#FFFFFF'>" + SystemAnalysis.getWaitTime(job.getTaskUptime()) + "</td>"
									+ "<td bgcolor='#FFFFFF'>" + (job.getTaskExceptionCount() > 0 ? job.getTaskExceptionCount() + "" : "-") + "</td>"
									
									+ "</tr>");
							n++;
						}
					}
					if (n == 0)
						t.append("<tr><td colspan=7 bgcolor='#FFFFFF'><center><br>- No background tasks scheduled.-<br><br></center></td></tr>");
					else
						t.append("<tr><td colspan=7 bgcolor='#FFFFFF'><center><br>" + nRun + " running, " + nBlocked + " blocked, " + nWait + " waiting, " + nSleep
								+ " sleeping, " + nTerm
								+ " terminated.<br><br></center></td></tr>");
					
					t.append("</table>");
					String txt = t.toString();
					setText(txt);
				}
			}
		};
		r.setBorder(BorderFactory.createBevelBorder(1));
		htmlTextPanels.add(r);
		
		shown_groups = htmlTextPanels.size();
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
		return "Multi-Threading Task Status";
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Utilities-System-Monitor-64.png";
	}
}
