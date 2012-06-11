package de.ipk.ag_ba.commands.lemnatec;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.gui.util.IAPwebcam;

public class UpdatingImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private final IAPwebcam webcam;
	private final BackgroundTaskStatusProviderSupportingExternalCall status;
	
	public UpdatingImagePanel(IAPwebcam webcam,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.webcam = webcam;
		this.status = status;
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
	
	public void refresh() {
		BufferedImage img;
		try {
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
			img = webcam.getSnapshot().getAsBufferedImage();
			if (doIt)
				status.setCurrentStatusText1(s.substring(0, s.length() - 1) + "#");
			
			image = img;
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}