/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.07.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.SystemInfo;
import org.graffiti.editor.SplashScreenInterface;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DBEsplashScreen implements SplashScreenInterface {
	
	CenterFrame splash = null;
	private String applicationName;
	private JProgressBar progressBar = null;
	private JLabel statusLabel = null;
	private Runnable endTask = null;
	
	private JLabel infoLabel = null;
	
	public static ImageIcon getStartLogo() {
		ClassLoader cl = DBEsplashScreen.class.getClassLoader();
		String path = DBEsplashScreen.class.getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/pattern_graffiti_logo.png"));
		return icon;
	}
	
	public DBEsplashScreen(String applicationName, String copyright, Runnable endTask) {
		this.applicationName = applicationName;
		this.endTask = endTask;
		splash = new CenterFrame(applicationName);
		splash.setUndecorated(true);
		
		Color bc = Color.WHITE; // new Color(235,255,235);
		splash.setBackground(bc);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path;
		try {
			path = this.getClass().getPackage().getName().replace('.', '/');
		} catch (Exception e) {
			path = "de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart".replace('.', '/');
		}
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/pattern_graffiti_logo.png"));
		
		infoLabel = new JLabel(getSplashScreenlabel(applicationName, -1));
		infoLabel.setVerticalTextPosition(SwingConstants.CENTER);
		double border = 5;
		
		if (!SystemInfo.isMac() || !UIManager.getLookAndFeel().isNativeLookAndFeel()) {
		}
		
		double sizeInfoPart[][] =
		{ { border, TableLayoutConstants.PREFERRED, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, border } // 20, xs,
		}; // Rows
		
		border = 2;
		double sizeParent[][] =
		{ { border, 240, TableLayoutConstants.PREFERRED, border },
							{ border, TableLayoutConstants.PREFERRED, border }
		}; // Rows
		
		border = 0;
		double sizeGlobal[][] =
		{ { border, TableLayoutConstants.PREFERRED, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, border }
		}; // Rows
		
		JPanel parent = new JPanel();
		parent.setLayout(new TableLayout(sizeParent));
		parent.setOpaque(true);
		parent.setBackground(bc);
		// parent.setBorder(BorderFactory.createEtchedBorder(Color.BLACK, Color.LIGHT_GRAY));
		parent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		progressBar = new JProgressBar();
		
		progressBar.putClientProperty("JProgressBar.style", "circular");
		
		progressBar.setIndeterminate(true);
		
		statusLabel = new JLabel("Initializing...");
		
		progressBar.setVisible(false);
		statusLabel.setVisible(false);
		
		JPanel rightComp = new JPanel();
		rightComp.setBackground(bc);
		rightComp.setOpaque(true);
		rightComp.setLayout(new TableLayout(sizeInfoPart));
		rightComp.add(infoLabel, "1,1");
		// rightComp.add(statusLabel, "1,2");
		// rightComp.add(progressBar, "1,3");
		rightComp.revalidate();
		
		parent.add(new JLabel(icon), "1,1");
		parent.add(rightComp, "2,1");
		
		parent.validate();
		
		// splash.setBounds(0, 0, 600, 200);
		
		splash.setBounds(0, 0,
							(int) (parent.getPreferredSize().width + border * 2),
							(int) (parent.getPreferredSize().height + border * 2));
		
		// splash.centerFrame();
		
		splash.setLayout(new TableLayout(sizeGlobal));
		
		splash.add(parent, "1,1");
		splash.validate();
		
		// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice gd = ge.getDefaultScreenDevice();
		// Rectangle virtualBounds = new Rectangle();
		// GraphicsConfiguration[] gc = gd.getConfigurations();
		// virtualBounds = virtualBounds.union(gc[0].getBounds());
		// int w, h;
		// int xoff = 0;
		// int yoff = 0;
		// try {
		// w = gc[0].getDevice().getDisplayMode().getWidth();
		// h = gc[0].getDevice().getDisplayMode().getHeight();
		// xoff = virtualBounds.x;
		// yoff = virtualBounds.y;
		// } catch(NullPointerException npe) {
		// w = 800;
		// h = 600;
		// }
		// // Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		// // int xpos = (virtualBounds.width / 2) - (splash.getWidth() / 2);
		// // int ypos = (virtualBounds.height / 2) - (splash.getHeight() / 2);
		// int xpos = (w / 2) - (splash.getWidth() / 2)+xoff;
		// int ypos = (h / 2) - (splash.getHeight() / 2)+yoff;
		
		// splash.setLocation(xpos, ypos);
		
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		p.x = p.x - splash.getWidth() / 2;
		p.y = p.y - splash.getHeight() / 2;
		splash.setLocation(p);
	}
	
	private String getSplashScreenlabel(String appname, double progress) {
		
		progress = progress * 1.2d;
		
		String grayA = "<font color='black'>";
		String grayB = "</font>";
		
		String blackA = "<font color='gray'>";
		String blackB = "</font>";
		
		StringBuilder nn = new StringBuilder();
		double maxLen = appname.length();
		boolean gray = false;
		boolean black = false;
		int idx = 0;
		
		gray = true;
		nn.append(grayA);
		
		for (char c : appname.toCharArray()) {
			idx++;
			double appnameProgress = 100d * idx / maxLen;
			
			if (appnameProgress > progress && gray) {
				gray = false;
				nn.append(grayB);
				nn.append(blackA);
			}
			nn.append(c);
		}
		
		if (gray)
			nn.append(grayB);
		if (black)
			nn.append(blackB);
		
		return "<html>" +
							"<br><br>" +
							"<h2>" + nn.toString() + "</h2>" +
							"<p>" + DBEgravistoHelper.DBE_GRAVISTO_NAME + "<br><br><br>";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#setMaximum(int)
	 */
	public void setMaximum(int maximum) {
		progressBar.setMaximum(maximum);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#setText(java.lang.String)
	 */
	public void setText(String text) {
		statusLabel.setText(text);
	}
	
	int outputlen = 0;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#setValue(int)
	 */
	public void setValue(final int value) {
		if (!infoLabel.isShowing()) {
			if (value * 43 / getMaximum() > outputlen) {
				System.out.print("#");
				outputlen++;
			}
			
		}
		progressBar.setIndeterminate(false);
		progressBar.setValue(value);
		if (infoLabel != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					infoLabel.setText(getSplashScreenlabel(applicationName, value));
					infoLabel.repaint();
				}
			});
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#getValue()
	 */
	public int getValue() {
		return progressBar.getValue();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.editor.SplashScreenInterface#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		splash.setVisible(visible);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.editor.SplashScreenInterface#setInitialisationFinished()
	 */
	public void setInitialisationFinished() {
		if (endTask != null)
			SwingUtilities.invokeLater(endTask);
		if (!infoLabel.isShowing())
			System.out.println("");
	}
	
	public void setIconImage(Image image) {
		splash.setIconImage(image);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#getMaximum()
	 */
	public int getMaximum() {
		return progressBar.getMaximum();
	}
	
}
