/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
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
 * ---------------------
 * DateChooserPanel.java
 * ---------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DateChooserPanel.java,v 1.1 2011-01-31 09:02:26 klukas Exp $
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 08-Dec-2001 : Dropped the getMonths() method (DG);
 * 13-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.date.SerialDate;

/**
 * A panel that allows the user to select a date.
 */
public class DateChooserPanel extends JPanel implements ActionListener {

	/** The date selected in the panel. */
	private Calendar chosenDate = null;

	/** The color for the selected date; */
	private Color chosenDateButtonColor = Color.red;

	/** The color for dates in the current month; */
	private Color chosenMonthButtonColor = Color.lightGray;

	/** The color for dates that are visible, but not in the current month; */
	private Color chosenOtherButtonColor = Color.darkGray;

	/** The first day-of-the-week; */
	private int firstDayOfWeek = Calendar.SUNDAY;

	/** The range used for selecting years; */
	private int yearSelectionRange = 20;

	/** The font used to display the date; */
	private Font dateFont = new Font("SansSerif", Font.PLAIN, 10);

	/** A combo for selecting the month; */
	private JComboBox monthSelector = null;

	/** A combo for selecting the year; */
	private JComboBox yearSelector = null;

	/** A button for selecting today's date; */
	private JButton todayButton = null;

	/** An array of buttons used to display the days-of-the-month; */
	private JButton[] buttons = null;

	/** A flag that indicates whether or not we are currently refreshing the buttons; */
	private boolean refreshing = false;

	/**
	 * Constructs a new date chooser panel, using today's date as the initial selection.
	 */
	public DateChooserPanel() {
		this(Calendar.getInstance(), false);
	}

	/**
	 * Constructs a new date chooser panel.
	 * 
	 * @param calendar
	 *           the calendar controlling the date.
	 * @param controlPanel
	 *           a flag that indicates whether or not the 'today' button should
	 *           appear on the panel.
	 */
	public DateChooserPanel(final Calendar calendar, final boolean controlPanel) {

		super(new BorderLayout());
		add(constructSelectionPanel(), BorderLayout.NORTH);
		add(getCalendarPanel(), BorderLayout.CENTER);
		if (controlPanel) {
			add(constructControlPanel(), BorderLayout.SOUTH);
		}
		// the default date is today...
		this.chosenDate = calendar;
		setDate(calendar.getTime());
	}

	/**
	 * Sets the date chosen in the panel.
	 * 
	 * @param theDate
	 *           the new date.
	 */
	public void setDate(final Date theDate) {

		this.chosenDate.setTime(theDate);
		this.monthSelector.setSelectedIndex(this.chosenDate.get(Calendar.MONTH));
		refreshYearSelector();
		refreshButtons();

	}

	/**
	 * Returns the date selected in the panel.
	 * 
	 * @return the selected date.
	 */
	public Date getDate() {
		return this.chosenDate.getTime();
	}

	/**
	 * Handles action-events from the date panel.
	 * 
	 * @param e
	 *           information about the event that occurred.
	 */
	public void actionPerformed(final ActionEvent e) {

		if (e.getActionCommand().equals("monthSelectionChanged")) {
			final JComboBox c = (JComboBox) e.getSource();
			this.chosenDate.set(Calendar.MONTH, c.getSelectedIndex());
			refreshButtons();
		} else
			if (e.getActionCommand().equals("yearSelectionChanged")) {
				if (!this.refreshing) {
					final JComboBox c = (JComboBox) e.getSource();
					final Integer y = (Integer) c.getSelectedItem();
					this.chosenDate.set(Calendar.YEAR, y.intValue());
					refreshYearSelector();
					refreshButtons();
				}
			} else
				if (e.getActionCommand().equals("todayButtonClicked")) {
					setDate(new Date());
				} else
					if (e.getActionCommand().equals("dateButtonClicked")) {
						final JButton b = (JButton) e.getSource();
						final int i = Integer.parseInt(b.getName());
						final Calendar cal = getFirstVisibleDate();
						cal.add(Calendar.DATE, i);
						setDate(cal.getTime());
					}
	}

	/**
	 * Returns a panel of buttons, each button representing a day in the month. This is a
	 * sub-component of the DatePanel.
	 * 
	 * @return the panel.
	 */
	private JPanel getCalendarPanel() {

		final JPanel p = new JPanel(new GridLayout(7, 7));
		p.add(new JLabel("Sun", JLabel.CENTER));
		p.add(new JLabel("Mon", JLabel.CENTER));
		p.add(new JLabel("Tue", JLabel.CENTER));
		p.add(new JLabel("Wed", JLabel.CENTER));
		p.add(new JLabel("Thu", JLabel.CENTER));
		p.add(new JLabel("Fri", JLabel.CENTER));
		p.add(new JLabel("Sat", JLabel.CENTER));

		this.buttons = new JButton[42];
		for (int i = 0; i < 42; i++) {
			final JButton b = new JButton("");
			b.setMargin(new Insets(1, 1, 1, 1));
			b.setName(new Integer(i).toString());
			b.setFont(this.dateFont);
			b.setFocusPainted(false);
			b.setActionCommand("dateButtonClicked");
			b.addActionListener(this);
			this.buttons[i] = b;
			p.add(b);
		}
		return p;

	}

	/**
	 * Returns the button color according to the specified date.
	 * 
	 * @param theDate
	 *           the date.
	 * @return the color.
	 */
	private Color getButtonColor(final Calendar theDate) {
		if (equalDates(theDate, this.chosenDate)) {
			return this.chosenDateButtonColor;
		} else
			if (theDate.get(Calendar.MONTH) == this.chosenDate.get(Calendar.MONTH)) {
				return this.chosenMonthButtonColor;
			} else {
				return this.chosenOtherButtonColor;
			}
	}

	/**
	 * Returns true if the two dates are equal (time of day is ignored).
	 * 
	 * @param c1
	 *           the first date.
	 * @param c2
	 *           the second date.
	 * @return boolean.
	 */
	private boolean equalDates(final Calendar c1, final Calendar c2) {
		if ((c1.get(Calendar.DATE) == c2.get(Calendar.DATE))
							&& (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
							&& (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns the first date that is visible in the grid. This should always be in the month
	 * preceding the month of the selected date.
	 * 
	 * @return the date.
	 */
	private Calendar getFirstVisibleDate() {
		final Calendar c = Calendar.getInstance();
		c.set(this.chosenDate.get(Calendar.YEAR), this.chosenDate.get(Calendar.MONTH), 1);
		c.add(Calendar.DATE, -1);
		while (c.get(Calendar.DAY_OF_WEEK) != getFirstDayOfWeek()) {
			c.add(Calendar.DATE, -1);
		}
		return c;
	}

	/**
	 * Returns the first day of the week (controls the labels in the date panel).
	 * 
	 * @return the first day of the week.
	 */
	private int getFirstDayOfWeek() {
		return this.firstDayOfWeek;
	}

	/**
	 * Update the button labels and colors to reflect date selection.
	 */
	private void refreshButtons() {
		final Calendar c = getFirstVisibleDate();
		for (int i = 0; i < 42; i++) {
			final JButton b = this.buttons[i];
			b.setText(new Integer(c.get(Calendar.DATE)).toString());
			b.setBackground(getButtonColor(c));
			c.add(Calendar.DATE, 1);
		}
	}

	/**
	 * Changes the contents of the year selection JComboBox to reflect the chosen date and the
	 * year range.
	 */
	private void refreshYearSelector() {
		if (!this.refreshing) {
			this.refreshing = true;
			this.yearSelector.removeAllItems();
			final Vector v = getYears(this.chosenDate.get(Calendar.YEAR));
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				this.yearSelector.addItem(e.nextElement());
			}
			this.yearSelector.setSelectedItem(new Integer(this.chosenDate.get(Calendar.YEAR)));
			this.refreshing = false;
		}
	}

	/**
	 * Returns a vector of years preceding and following the specified year. The number of
	 * years preceding and following is determined by the yearSelectionRange attribute.
	 * 
	 * @param chosenYear
	 *           the selected year.
	 * @return a vector of years.
	 */
	private Vector getYears(final int chosenYear) {
		final Vector v = new Vector();
		for (int i = chosenYear - this.yearSelectionRange; i <= chosenYear + this.yearSelectionRange; i++) {
			v.addElement(new Integer(i));
		}
		return v;
	}

	/**
	 * Constructs a panel containing two JComboBoxes (for the month and year) and a button (to
	 * reset the date to TODAY).
	 * 
	 * @return the panel.
	 */
	private JPanel constructSelectionPanel() {
		final JPanel p = new JPanel();
		this.monthSelector = new JComboBox(SerialDate.getMonths());
		this.monthSelector.addActionListener(this);
		this.monthSelector.setActionCommand("monthSelectionChanged");
		p.add(this.monthSelector);

		this.yearSelector = new JComboBox(getYears(0));
		this.yearSelector.addActionListener(this);
		this.yearSelector.setActionCommand("yearSelectionChanged");
		p.add(this.yearSelector);

		return p;
	}

	/**
	 * Returns a panel that appears at the bottom of the calendar panel - contains a button for
	 * selecting today's date.
	 * 
	 * @return the panel.
	 */
	private JPanel constructControlPanel() {

		final JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		this.todayButton = new JButton("Today");
		this.todayButton.addActionListener(this);
		this.todayButton.setActionCommand("todayButtonClicked");
		p.add(this.todayButton);
		return p;

	}

}
