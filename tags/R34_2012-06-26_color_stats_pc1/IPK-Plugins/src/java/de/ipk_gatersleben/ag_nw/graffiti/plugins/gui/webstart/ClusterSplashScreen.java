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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.graffiti.editor.SplashScreenInterface;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ClusterSplashScreen implements SplashScreenInterface {
	
	CenterFrame splash = null;
	private JProgressBar progressBar = null;
	private JLabel statusLabel = null;
	
	public ClusterSplashScreen(String applicationName, String copyright) {
		splash = new CenterFrame(applicationName);
		splash.setUndecorated(true);
		
		Color bc = Color.WHITE; // new Color(235,255,235);
		splash.setBackground(bc);
		
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/pattern_graffiti_logo.png"));
		
		JLabel infoLabel = new JLabel("<html>" +
							"<h2>" + DBEgravistoHelper.CLUSTER_ANALYSIS_VERSION + "</h2>" +
							"<p>" + DBEgravistoHelper.CLUSTER_ANALYSIS_NAME + " (c) 2004-2007 IPK-Gatersleben<br>" +
							"- based on Gravisto (c) 2001-2004 University of Passau<p><p><p><p>"
							/*
							 * +"<p><p>"+
							 * PatternGraffitiHelper.DBE_GRAVISTO_NAME+
							 * " is part of the "+
							 * PatternGraffitiHelper.DBE_INFORMATIONSYSTEM_NAME
							 * +"<br>- developed by Christian Klukas<p><p>"
							 */);
		infoLabel.setVerticalTextPosition(SwingConstants.TOP);
		double border = 5;
		
		double sizeInfoPart[][] =
		{ { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border }
		}; // Rows
		
		border = 2;
		double sizeParent[][] =
		{ { border, 240, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border }
		}; // Rows
		
		border = 0;
		double sizeGlobal[][] =
		{ { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.FILL, border }
		}; // Rows
		
		JPanel parent = new JPanel();
		parent.setLayout(new TableLayout(sizeParent));
		parent.setOpaque(true);
		parent.setBackground(bc);
		// parent.setBorder(BorderFactory.createEtchedBorder(Color.BLACK, Color.LIGHT_GRAY));
		parent.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		progressBar = new JProgressBar();
		// progressBar.setIndeterminate(true);
		
		statusLabel = new JLabel("Initializing...");
		
		splash.setBounds(0, 0, 580, 200);
		splash.centerFrame();
		splash.setLayout(new TableLayout(sizeGlobal));
		
		JPanel rightComp = new JPanel();
		rightComp.setBackground(bc);
		rightComp.setOpaque(true);
		rightComp.setLayout(new TableLayout(sizeInfoPart));
		rightComp.add(infoLabel, "1,1");
		rightComp.add(statusLabel, "1,2");
		rightComp.add(progressBar, "1,3");
		rightComp.revalidate();
		
		parent.add(new JLabel(icon), "1,1");
		parent.add(rightComp, "2,1");
		splash.add(parent, "1,1");
		splash.validate();
		
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
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#setValue(int)
	 */
	public void setValue(int value) {
		progressBar.setIndeterminate(false);
		progressBar.setValue(value);
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
		//
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.util.ProgressViewer#getMaximum()
	 */
	public int getMaximum() {
		return progressBar.getMaximum();
	}
	
}
