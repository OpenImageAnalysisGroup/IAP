package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class Star extends JComponent {
	private static final long serialVersionUID = -6254542211584050448L;
	
	private String currentmessage = "";
	
	public Star() {
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {
				currentmessage = "MM " + e.getX() + "/" + e.getY();
				repaint();
			}
			
			public void mouseDragged(MouseEvent e) {
				currentmessage = "MD " + e.getX() + "/" + e.getY();
				repaint();
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawLine(getX(), getY(), getX() + getWidth(), getY() + getHeight());
		g.drawLine(getX() + getWidth(), getY(), getX(), getY() + getHeight());
		g.drawString(currentmessage, 20, 20);
	}
	
}
