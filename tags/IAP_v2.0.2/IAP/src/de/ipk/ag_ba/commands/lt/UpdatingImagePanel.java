package de.ipk.ag_ba.commands.lt;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.plugin.io.resources.IOurl;

public class UpdatingImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private final BackgroundTaskStatusProviderSupportingExternalCall status;
	private final IOurl url;
	
	public UpdatingImagePanel(IOurl url,
			BackgroundTaskStatusProviderSupportingExternalCall status) {
		this.url = url;
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
			InputStream is = url.getInputStream();
			try {
				img = ImageIO.read(is);
			} finally {
				is.close();
			}
			if (doIt)
				status.setCurrentStatusText1(s.substring(0, s.length() - 1) + "#");
			
			image = img;
			repaint();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}