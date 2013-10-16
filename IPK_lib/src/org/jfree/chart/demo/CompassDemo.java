/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ----------------
 * CompassDemo.java
 * ----------------
 * (C) Copyright 2002-2004, by the Australian Antarctic Division and Contributors.
 * Original Author: Bryan Scott (for the Australian Antarctic Division);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: CompassDemo.java,v 1.1 2011-01-31 09:01:48 klukas Exp $
 * Changes
 * -------
 * 25-Sep-2002 : Version 1, contributed by Bryan Scott (DG);
 * 10-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 27-Mar-2003 : Changed dataset to ValueDataset (DG);
 * 16-Mar-2004 : Fixed null data display issue (BRS);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CompassPlot;
import org.jfree.data.DefaultValueDataset;
import org.jfree.ui.Spinner;

/**
 * A demo application showing how to use the {@link CompassPlot} class.
 * 
 * @author Bryan Scott
 */
public class CompassDemo extends JPanel {

	/** Whether to output messages to the console **/
	public static final boolean DEBUG = true;

	/** The available needle types. */
	public static final String[] NEEDLE_TYPES = { "Arrow", "Line", "Long", "Pin", "Plum", "Pointer", "Ship", "Wind", "Arrow" };

	/** Dataset 1. */
	private DefaultValueDataset compassData = new DefaultValueDataset(new Double(0.0));

	/** Dataset 2. */
	private DefaultValueDataset shipData = new DefaultValueDataset(new Double(0.0));

	/** The compass plot. */
	private CompassPlot compassPlot = new CompassPlot(this.compassData);

	/** The chart. */
	private JFreeChart compassChart = new JFreeChart("Compass Test",
																		JFreeChart.DEFAULT_TITLE_FONT,
																		this.compassPlot, false);

	/** The chart panel. */
	private ChartPanel panelCompass = new ChartPanel(this.compassChart);

	/** A grid layout. */
	private GridLayout gridLayout1 = new GridLayout();

	/** A panel. */
	private JPanel panelCompassHolder = new JPanel();

	/** A border layout. */
	private BorderLayout borderLayout = new BorderLayout();

	/** A panel. */
	private JPanel jPanel12 = new JPanel();

	/** A checkbox. */
	private JCheckBox windNullCheckBox = new JCheckBox();

	/** A checkbox. */
	private JCheckBox shipNullCheckBox = new JCheckBox();

	// SpinnerNumberModel modelWind = new SpinnerNumberModel(0, -1, 361, 1);
	// SpinnerNumberModel modelShip = new SpinnerNumberModel(0, -1, 361, 1);
	// JSpinner spinWind = new JSpinner(modelWind);
	// JSpinner spinShip = new JSpinner(modelShip);

	/** The wind spinner control. */
	private Spinner spinWind = new Spinner(270);

	/** The ship spinner control. */
	private Spinner spinShip = new Spinner(45);

	/** A panel. */
	private JPanel jPanel1 = new JPanel();

	/** A combo box. */
	private JComboBox pick1Pointer = new JComboBox(NEEDLE_TYPES);

	/** A panel. */
	private JPanel jPanel2 = new JPanel();

	/** A combo box. */
	private JComboBox pick2Pointer = new JComboBox(NEEDLE_TYPES);

	/** A titled border. */
	private TitledBorder titledBorder1;

	/** A titled border. */
	private TitledBorder titledBorder2;

	/** A grid bag layout. */
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	/** A grid bag layout. */
	private GridBagLayout gridBagLayout2 = new GridBagLayout();

	/** A titled border. */
	private TitledBorder titledBorder3;

	/** A grid layout. */
	private GridLayout gridLayout2 = new GridLayout();

	/**
	 * Default constructor.
	 */
	public CompassDemo() {
		try {
			// this.compassPlot.addData(this.compassData);
			this.compassPlot.addData(this.shipData);
			this.compassPlot.setSeriesNeedle(0, 7);
			this.compassPlot.setSeriesNeedle(1, 5);
			this.compassPlot.setSeriesPaint(0, Color.blue);
			this.compassPlot.setSeriesOutlinePaint(0, Color.blue);
			this.compassPlot.setSeriesPaint(1, Color.red);
			this.compassPlot.setSeriesOutlinePaint(1, Color.red);
			this.pick1Pointer.setSelectedIndex(7);
			this.pick2Pointer.setSelectedIndex(5);
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initialises the user interface.
	 * 
	 * @throws Exception
	 *            if there are any exceptions.
	 */
	void jbInit() throws Exception {
		this.titledBorder1 = new TitledBorder("");
		this.titledBorder2 = new TitledBorder("");
		this.titledBorder3 = new TitledBorder("");
		setLayout(this.gridLayout1);
		this.panelCompassHolder.setLayout(this.borderLayout);
		this.windNullCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
		this.windNullCheckBox.setText("Null");
		this.windNullCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				checkWindNullActionPerformed(e);
			}
		});
		this.shipNullCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
		this.shipNullCheckBox.setText("Null");
		this.shipNullCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				checkShipNullActionPerformed(e);
			}
		});

		this.spinShip.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				if (DEBUG) {
					System.out.println("compassDemo:spinShipPropertyChange");
				}
				final Spinner spinner = (Spinner) evt.getSource();
				if (spinner.isEnabled()) {
					shipData.setValue(new Double(spinner.getValue()));
				}
			}
		});

		this.spinWind.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				if (DEBUG) {
					System.out.println("compassDemo:spinWindPropertyChange");
				}
				final Spinner spinner = (Spinner) evt.getSource();
				if (spinner.isEnabled()) {
					compassData.setValue(new Double(spinner.getValue()));
				}
			}
		});
		this.jPanel12.setLayout(this.gridLayout2);
		this.jPanel2.setBorder(this.titledBorder1);
		this.jPanel2.setLayout(this.gridBagLayout2);
		this.jPanel1.setBorder(this.titledBorder2);
		this.jPanel1.setLayout(this.gridBagLayout1);
		this.titledBorder1.setTitle("Second Pointer");
		this.titledBorder2.setTitle("First Pointer");
		this.titledBorder3.setTitle("Plot Options");
		this.pick2Pointer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				pick2PointerActionPerformed(e);
			}
		});
		this.pick1Pointer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				pick1PointerActionPerformed(e);
			}
		});
		add(this.panelCompassHolder, null);
		this.panelCompassHolder.add(this.jPanel12, BorderLayout.SOUTH);
		this.jPanel12.add(this.jPanel1, null);

		this.jPanel1.add(this.pick1Pointer,
							new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
															GridBagConstraints.CENTER,
															GridBagConstraints.HORIZONTAL,
															new Insets(0, 0, 0, 0),
															0, 0));

		this.jPanel1.add(this.windNullCheckBox,
							new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
															GridBagConstraints.CENTER,
															GridBagConstraints.NONE,
															new Insets(0, 0, 0, 0),
															0, 0));

		this.jPanel1.add(this.spinWind,
							new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0,
															GridBagConstraints.CENTER,
															GridBagConstraints.BOTH,
															new Insets(0, 0, 0, 0),
															0, 0));

		this.jPanel12.add(this.jPanel2, null);

		this.jPanel2.add(this.pick2Pointer,
							new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
															GridBagConstraints.CENTER,
															GridBagConstraints.HORIZONTAL,
															new Insets(0, 0, 0, 0),
															0, 0));

		this.jPanel2.add(this.shipNullCheckBox,
							new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
															GridBagConstraints.CENTER,
															GridBagConstraints.NONE,
															new Insets(0, 0, 0, 0),
															0, 0));

		this.jPanel2.add(this.spinShip,
							new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0,
												GridBagConstraints.CENTER,
												GridBagConstraints.BOTH,
												new Insets(0, 0, 0, 0),
												0, 0));

		this.panelCompassHolder.add(this.panelCompass, BorderLayout.CENTER);

	}

	/**
	 * Entry point for the demo application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final CompassDemo panel = new CompassDemo();

		final JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout(5, 5));
		frame.setDefaultCloseOperation(3);
		frame.setTitle("Compass Demo");
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setSize(700, 400);
		final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getSize().width) / 2,
									(d.height - frame.getSize().height) / 2);
		frame.setVisible(true);
	}

	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available *
	// * to purchase from Object Refinery Limited: *
	// * *
	// * http://www.object-refinery.com/jfreechart/guide.html *
	// * *
	// * Sales are used to provide funding for the JFreeChart project - please *
	// * support us so that we can continue developing free software. *
	// ****************************************************************************

	/**
	 * Updates the data.
	 * 
	 * @param value
	 *           the value.
	 */
	public void adjustData(final double value) {

		final Number val = this.compassData.getValue();
		double newVal = value;

		if (val != null) {
			newVal += val.doubleValue();
		}

		if (newVal > 360) {
			newVal = 0;
		}

		if (newVal < 0) {
			newVal = 360;
		}

		this.compassData.setValue(new Double(newVal));

	}

	/**
	 * Handles an action event.
	 * 
	 * @param e
	 *           the event.
	 */
	void checkWindNullActionPerformed(final ActionEvent e) {
		if (CompassDemo.DEBUG) {
			System.out.println("CompassDemo:checkWindNull: " + this.windNullCheckBox.isSelected());
		}
		if (this.windNullCheckBox.isSelected()) {
			this.compassData.setValue(null);
			this.spinWind.setEnabled(false);
		} else {
			// compassData.setValue((new Double(((Integer)spinWind.getValue()).intValue())));
			this.compassData.setValue(new Double(this.spinWind.getValue()));
			this.spinWind.setEnabled(true);
		}
		if (CompassDemo.DEBUG) {
			System.out.println("CompassDemo:checkWindNull: " + this.compassData.getValue());
		}

	}

	/**
	 * Handles an action event.
	 * 
	 * @param e
	 *           the event.
	 */
	void checkShipNullActionPerformed(final ActionEvent e) {
		if (CompassDemo.DEBUG) {
			System.out.println("CompassDemo:checkShipNull: " + this.shipNullCheckBox.isSelected());
		}
		if (this.shipNullCheckBox.isSelected()) {
			this.shipData.setValue(null);
			this.spinShip.setEnabled(false);
		} else {
			// shipData.setValue((new Double(((Integer)spinShip.getValue()).intValue())));
			this.shipData.setValue(new Double(this.spinShip.getValue()));
			this.spinShip.setEnabled(true);
		}
	}

	/**
	 * Handles an action event.
	 * 
	 * @param e
	 *           the event.
	 */
	void pick2PointerActionPerformed(final ActionEvent e) {
		if (CompassDemo.DEBUG) {
			System.out.println("compassDemo:pick2PointActionPerformed " + e.getActionCommand() + ",");
		}
		this.compassPlot.setSeriesNeedle(1, this.pick2Pointer.getSelectedIndex());
		this.compassPlot.setSeriesPaint(1, Color.red);
		this.compassPlot.setSeriesOutlinePaint(1, Color.red);
	}

	/**
	 * Handles an action event.
	 * 
	 * @param e
	 *           the event.
	 */
	void pick1PointerActionPerformed(final ActionEvent e) {
		if (CompassDemo.DEBUG) {
			System.out.println("compassDemo:pick1PointActionPerformed " + e.getActionCommand() + ",");
		}

		this.compassPlot.setSeriesNeedle(0, this.pick1Pointer.getSelectedIndex());
		this.compassPlot.setSeriesPaint(0, Color.blue);
		this.compassPlot.setSeriesOutlinePaint(0, Color.blue);
	}

}
