package de.ipk.ag_ba.commands.lemnatec;

import info.clearthought.layout.TableLayout;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.MarkComponent;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.mongo.IAPwebcam;
import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;

public class PictureViewer extends JPanel implements HierarchyListener {
	private static final long serialVersionUID = 1L;
	
	ThreadSafeOptions tso = new ThreadSafeOptions();
	
	public PictureViewer(final IAPwebcam webcam, String url,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {
		setLayout(TableLayout.getLayout(650, 800));
		final UpdatingImagePanel view = new UpdatingImagePanel(webcam, status);
		final MarkComponent mark = new MarkComponent(view, true, 640, false, 480);
		boolean markBothsides = false;
		if (markBothsides)
			mark.setMarkColor2(null);
		add(	
				
				TableLayout.getSplitVertical(
						new JLabelHTMLlink("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;--&gt;" +
								"Click here, to load " + (webcam.hasVideoStream() ? "video" : "image") + " in web-browser" +
								"<br>&nbsp;<br>", url, "Open " + url),
						mark,
						TableLayout.PREFERRED, TableLayout.FILL)
				, "0,0");
		
		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					do {
						mark.setMark(true);
						view.refresh();
						mark.setMark(false);
						Thread.sleep(1000);
					} while (tso.getBval(0, true));
					status.setCurrentStatusText1("connection closed");
					Thread.sleep(3000);
					status.setCurrentStatusText1("");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t.setName("Webcam view update thread (" + webcam + ")");
		t.start();
		
		addHierarchyListener(this);
	}
	
	@Override
	public void hierarchyChanged(HierarchyEvent arg0) {
		if (!isDisplayable())
			tso.setBval(0, false);
	}
}
