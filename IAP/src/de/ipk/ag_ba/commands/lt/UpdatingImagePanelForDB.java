package de.ipk.ag_ba.commands.lt;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MarkComponent;

import com.mongodb.gridfs.GridFS;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.mongo.MongoDB;

public class UpdatingImagePanelForDB extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private final BackgroundTaskStatusProviderSupportingExternalCall status;
	private final MongoDB dc;
	private final String filename;
	private final GridFS gridfs_webcam_files;
	
	public UpdatingImagePanelForDB(MongoDB dc, String filename,
			BackgroundTaskStatusProviderSupportingExternalCall status,
			GridFS gridfs_webcam_files) {
		this.dc = dc;
		this.filename = filename;
		this.status = status;
		this.gridfs_webcam_files = gridfs_webcam_files;
		setSize(800, 600);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (image != null) {
			synchronized (image) {
				int w = image.getWidth();
				int h = image.getHeight();
				g.drawImage(image, 0, 0, w, h, 0, 0, w, h, null);
			}
		}
	}
	
	int n = 1;
	
	public void refresh(MarkComponent mark) {
		try {
			boolean newImageAvailable = true;
			if (newImageAvailable) {
				mark.setMark(true);
				StringBuilder s = new StringBuilder();
				while (s.length() < n)
					s.append("#");
				n++;
				if (n >= 1)
					n = 0;
				s.append("-");
				boolean doIt = false;
				if (doIt)
					status.setCurrentStatusText1(s + "");
				if (doIt)
					status.setCurrentStatusText1(s.substring(0, s.length() - 1) + "#");
				
				BufferedImage img;
				// load img
				image = new Image(dc.getSavedScreenshot(filename, gridfs_webcam_files, status)).io()
						.blurImageJ(1)
						.resize(getWidth(), getHeight()).sharpen().getAsBufferedImage();
				repaint();
				Thread.sleep(50);
				mark.setMark(false);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
}