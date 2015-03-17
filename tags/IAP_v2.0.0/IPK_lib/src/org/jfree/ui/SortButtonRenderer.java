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
 * -----------------------
 * SortButtonRenderer.java
 * -----------------------
 * (C) Copyright 2000-2004, by Nobuo Tamemasa and Contributors.
 * Original Author: Nobuo Tamemasa;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Gareth Davis;
 * $Id: SortButtonRenderer.java,v 1.1 2011-01-31 09:02:23 klukas Exp $
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.* (DG);
 * 26-Jun-2002 : Removed unnecessary import (DG);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.ui;

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer for table headings - uses one of three JButton instances to indicate the
 * sort order for the table column.
 * <P>
 * This class (and also BevelArrowIcon) is adapted from original code by Nobuo Tamemasa (version 1.0, 26-Feb-1999) posted on www.codeguru.com.
 */
public class SortButtonRenderer implements TableCellRenderer {

	/** Useful constant indicating NO sorting. */
	public static final int NONE = 0;

	/** Useful constant indicating ASCENDING (that is, arrow pointing down) sorting in the table. */
	public static final int DOWN = 1;

	/** Useful constant indicating DESCENDING (that is, arrow pointing up) sorting in the table. */
	public static final int UP = 2;

	/** The current pressed column (-1 for no column). */
	private int pressedColumn = -1;

	/** The three buttons that are used to render the table header cells. */
	private JButton normalButton, ascendingButton, descendingButton;

	/**
	 * Used to allow the class to work out whether to use the buttuns
	 * or labels. Labels are required when using the aqua look and feel cos the
	 * buttons won't fit.
	 */
	private boolean useLabels;

	/** The normal label (only used with MacOSX). */
	private JLabel normalLabel;

	/** The ascending label (only used with MacOSX). */
	private JLabel ascendingLabel;

	/** The descending label (only used with MacOSX). */
	private JLabel descendingLabel;

	/**
	 * Creates a new button renderer.
	 */
	public SortButtonRenderer() {

		this.pressedColumn = -1;
		this.useLabels = UIManager.getLookAndFeel().getID().equals("Aqua");

		Border border = UIManager.getBorder("TableHeader.cellBorder");

		if (this.useLabels) {
			this.normalLabel = new JLabel();
			this.normalLabel.setHorizontalAlignment(JLabel.LEADING);

			this.ascendingLabel = new JLabel();
			this.ascendingLabel.setHorizontalAlignment(JLabel.LEADING);
			this.ascendingLabel.setHorizontalTextPosition(JLabel.LEFT);
			this.ascendingLabel.setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));

			this.descendingLabel = new JLabel();
			this.descendingLabel.setHorizontalAlignment(JLabel.LEADING);
			this.descendingLabel.setHorizontalTextPosition(JLabel.LEFT);
			this.descendingLabel.setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));

			this.normalLabel.setBorder(border);
			this.ascendingLabel.setBorder(border);
			this.descendingLabel.setBorder(border);
		} else {
			this.normalButton = new JButton();
			this.normalButton.setMargin(new Insets(0, 0, 0, 0));
			this.normalButton.setHorizontalAlignment(JButton.LEADING);

			this.ascendingButton = new JButton();
			this.ascendingButton.setMargin(new Insets(0, 0, 0, 0));
			this.ascendingButton.setHorizontalAlignment(JButton.LEADING);
			this.ascendingButton.setHorizontalTextPosition(JButton.LEFT);
			this.ascendingButton.setIcon(new BevelArrowIcon(BevelArrowIcon.DOWN, false, false));
			this.ascendingButton.setPressedIcon(
								new BevelArrowIcon(BevelArrowIcon.DOWN, false, true)
								);

			this.descendingButton = new JButton();
			this.descendingButton.setMargin(new Insets(0, 0, 0, 0));
			this.descendingButton.setHorizontalAlignment(JButton.LEADING);
			this.descendingButton.setHorizontalTextPosition(JButton.LEFT);
			this.descendingButton.setIcon(new BevelArrowIcon(BevelArrowIcon.UP, false, false));
			this.descendingButton.setPressedIcon(
								new BevelArrowIcon(BevelArrowIcon.UP, false, true)
								);

			this.normalButton.setBorder(border);
			this.ascendingButton.setBorder(border);
			this.descendingButton.setBorder(border);

		}

	}

	/**
	 * Returns the renderer component.
	 * 
	 * @param table
	 *           the table.
	 * @param value
	 *           the value.
	 * @param isSelected
	 *           selected?
	 * @param hasFocus
	 *           focussed?
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 * @return the renderer.
	 */
	public Component getTableCellRendererComponent(JTable table,
																	Object value,
																	boolean isSelected,
																	boolean hasFocus,
																	int row, int column) {

		JComponent component = (this.useLabels ? (JComponent) this.normalLabel
																: (JComponent) this.normalButton);
		int cc = table.convertColumnIndexToModel(column);

		if (table != null) {
			SortableTableModel model = (SortableTableModel) table.getModel();
			if (model.getSortingColumn() == cc) {
				if (model.getAscending()) {
					component = (this.useLabels ? (JComponent) this.ascendingLabel
															: (JComponent) this.ascendingButton);
				} else {
					component = (this.useLabels ? (JComponent) this.descendingLabel
															: (JComponent) this.descendingButton);
				}
			}
		}

		JTableHeader header = table.getTableHeader();
		boolean isPressed = (cc == this.pressedColumn);

		if (header != null) {
			component.setForeground(header.getForeground());
			component.setBackground(header.getBackground());
			component.setFont(header.getFont());
		}

		if (this.useLabels) {
			JLabel label = (JLabel) component;
			label.setText((value == null) ? "" : value.toString());
		} else {
			JButton button = (JButton) component;
			button.setText((value == null) ? "" : value.toString());
			button.getModel().setPressed(isPressed);
			button.getModel().setArmed(isPressed);
		}
		return component;
	}

	/**
	 * Sets the pressed column.
	 * 
	 * @param column
	 *           the column.
	 */
	public void setPressedColumn(final int column) {
		this.pressedColumn = column;
	}

}
