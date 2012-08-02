// ==============================================================================
//
// SplashScreen.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: SplashScreen.java,v 1.1 2011-01-31 09:04:26 klukas Exp $

package org.graffiti.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.graffiti.core.ImageBundle;

/**
 * A frame that is displayed while Graffiti is loading. The progress of loading
 * is displayed with a progress bar and a description of the current loading
 * action.
 * 
 * @author Michael Forster
 * @version $Revision: 1.1 $ $Date: 2011-01-31 09:04:26 $
 */
public class SplashScreen
					extends JFrame
					implements SplashScreenInterface {
	// ~ Static fields/initializers =============================================
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** Display for the progress made in loading Graffiti */
	private static JProgressBar progressBar;
	
	/**
	 * Display for the description of the current action for loading Graffiti
	 */
	private static JLabel progressLabel;
	
	private String copyright = "<html>Copyright &copy; 2001-2003 University of Passau";
	
	// ~ Constructors ===========================================================
	
	public SplashScreen(String applicationName, String copyright) {
		super(applicationName);
		this.copyright = copyright;
		init();
	}
	
	/**
	 * Creates a new SplashScreen object.
	 */
	public SplashScreen() {
		super("Start up Graffiti...");
		init();
	}
	
	// ~ Methods ================================================================
	
	private void init() {
		setUndecorated(true);
		setBackground(Color.WHITE);
		
		// content pane
		JComponent contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new LineBorder(Color.BLACK, 1));
		contentPane.setBackground(null);
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);
		
		// logo image
		Image image = ImageBundle.getInstance().getImage("editor.splash");
		ImageIcon icon = new ImageIcon(image);
		JLabel imageLabel = new JLabel(icon);
		imageLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagConstraints c = new GridBagConstraints();
		contentPane.add(imageLabel, c);
		
		// copyright label
		JLabel copyrightLabel = new JLabel(copyright);
		copyrightLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new GridBagConstraints();
		c.gridx = 0;
		contentPane.add(copyrightLabel, c);
		
		// progress bar
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setMaximum(100);
		Dimension prefSize = progressBar.getPreferredSize();
		prefSize.width = 0;
		progressBar.setPreferredSize(prefSize);
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(5, 10, 0, 10);
		contentPane.add(progressBar, c);
		
		// progress label
		progressLabel = new JLabel("Initializing...");
		progressLabel.setBackground(null);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.insets = new Insets(2, 0, 2, 0);
		contentPane.add(progressLabel, c);
		
		pack();
		
		// center on display
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenDim.width - getWidth()) / 2,
							(screenDim.height - getHeight()) / 2);
	}
	
	/*
	 * @see org.graffiti.util.ProgressViewer#setMaximum(int)
	 */
	public void setMaximum(int maximum) {
		progressBar.setMaximum(maximum);
	}
	
	/*
	 * @see org.graffiti.util.ProgressViewer#setText(java.lang.String)
	 */
	public void setText(String text) {
		progressLabel.setText(text);
	}
	
	/*
	 * @see org.graffiti.util.ProgressViewer#setValue(int)
	 */
	public void setValue(int value) {
		progressBar.setValue(value);
	}
	
	/*
	 * @see org.graffiti.util.ProgressViewer#getValue()
	 */
	public int getValue() {
		return progressBar.getValue();
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

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
