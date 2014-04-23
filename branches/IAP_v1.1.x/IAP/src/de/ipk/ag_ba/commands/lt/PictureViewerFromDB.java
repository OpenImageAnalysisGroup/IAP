package de.ipk.ag_ba.commands.lt;

import info.clearthought.layout.TableLayout;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MarkComponent;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import com.mongodb.gridfs.GridFS;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;

public class PictureViewerFromDB extends JPanel implements HierarchyListener {
	private static final long serialVersionUID = 1L;
	
	ThreadSafeOptions tso = new ThreadSafeOptions();
	
	public PictureViewerFromDB(final MongoDB dc, String filename,
			final BackgroundTaskStatusProviderSupportingExternalCall status, GridFS gridfs_screenshots) {
		setLayout(TableLayout.getLayout(650, 800));
		final JLabel lbl = new JLabel("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--&gt;" +
				"Screenshot time: n/a" +
				"<br>&nbsp;<br>");
		BackgroundTaskConsoleLogger stat = new BackgroundTaskConsoleLogger("", "", false) {
			@Override
			public void setCurrentStatusText1(String status) {
				super.setCurrentStatusText1(status);
				lbl.setText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--&gt;" +
						"Screenshot time: " + status +
						"<br>&nbsp;<br>");
			}
		};
		
		final UpdatingImagePanelForDB view = new UpdatingImagePanelForDB(dc, filename, stat,
				gridfs_screenshots);
		final MarkComponent mark = new MarkComponent(view, true, 640, false, 480);
		boolean markBothsides = false;
		if (markBothsides)
			mark.setMarkColor2(null);
		
		add(TableLayout.getSplitVertical(
				lbl,
				mark,
				TableLayout.PREFERRED, TableLayout.FILL)
				, "0,0");
		
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				String host = SystemAnalysisExt.getHostNameNiceNoError();
				try {
					do {
						view.refresh(mark);
						Thread.sleep(1000);
						dc.updateScreenshotObserver(host, System.currentTimeMillis());
					} while (tso.getBval(0, true));
					dc.updateScreenshotObserver(host, 0);
					status.setCurrentStatusText1("connection closed");
					Thread.sleep(3000);
					status.setCurrentStatusText1("");
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		});
		t.setName("Screenshot view update thread (" + filename + ")");
		t.start();
		
		addHierarchyListener(this);
	}
	
	@Override
	public void hierarchyChanged(HierarchyEvent arg0) {
		if (!isDisplayable())
			tso.setBval(0, false);
	}
}
