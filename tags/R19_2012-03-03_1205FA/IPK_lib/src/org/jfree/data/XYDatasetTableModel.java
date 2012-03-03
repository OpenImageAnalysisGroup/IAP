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
 * ------------------------
 * XYDatasetTableModel.java
 * ------------------------
 * (C)opyright 2003, 2004, by Bryan Scott and Contributors.
 * Original Author: Bryan Scott ;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Changes
 * -------
 * 01-Jul-2003 : Version 1 contributed by Bryan Scott (DG);
 */

package org.jfree.data;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A READ-ONLY wrapper around an {@link XYDataset} to convert it to a
 * table model for use in a JTable.
 * <P>
 * TO DO:
 * <ul>
 * <li>implement proper naming for x axis (getColumnName)</li>
 * <li>implement setValueAt to remove READ-ONLY constraint (not sure how)</li>
 * </ul>
 * 
 * @author Bryan Scott
 */

public class XYDatasetTableModel extends AbstractTableModel
											implements TableModel, DatasetChangeListener {

	/** The dataset. */
	XYDataset model = null;

	/**
	 * Default constructor.
	 */
	public XYDatasetTableModel() {
		super();
	}

	/**
	 * Creates a new table model based on the specified dataset.
	 * 
	 * @param dataset
	 *           the dataset.
	 */
	public XYDatasetTableModel(final XYDataset dataset) {
		this();
		setModel(dataset);
	}

	/**
	 * Sets the model (dataset).
	 * 
	 * @param dataset
	 *           the dataset.
	 */
	public void setModel(final XYDataset dataset) {
		this.model = dataset;
		this.model.addChangeListener(this);
		fireTableDataChanged();
	}

	/**
	 * Returns the number of rows.
	 * 
	 * @return The row count.
	 */
	public int getRowCount() {
		if (this.model == null) {
			return 0;
		}
		return this.model.getItemCount(0);
	}

	/**
	 * Gets the number of columns in the model.
	 * 
	 * @return The number of columns in the model.
	 */
	public int getColumnCount() {
		if (this.model == null) {
			return 0;
		}
		return this.model.getSeriesCount() + 1;
	}

	/**
	 * Returns the column name.
	 * 
	 * @param column
	 *           the column index.
	 * @return The column name.
	 */
	public String getColumnName(final int column) {
		if (this.model == null) {
			return super.getColumnName(column);
		}
		if (column < 1) {
			return "X Value";
		} else {
			return this.model.getSeriesName(column - 1);
		}
	}

	/**
	 * Returns a value of the specified cell.
	 * Column 0 is the X axis, Columns 1 and over are the Y axis
	 * 
	 * @param row
	 *           the row number.
	 * @param column
	 *           the column number.
	 * @return the value of the specified cell.
	 */
	public Object getValueAt(final int row, final int column) {
		if (this.model == null) {
			return null;
		}
		if (column < 1) {
			return this.model.getXValue(0, row);
		} else {
			return this.model.getYValue(row, column - 1);
		}
	}

	/**
	 * Notify listeners that the underlying dataset has changed.
	 * 
	 * @param datasetChangeEvent
	 *           the event
	 * @see DatasetChangeListener
	 */
	public void datasetChanged(final DatasetChangeEvent datasetChangeEvent) {
		fireTableDataChanged();
	}

	/**
	 * Returns a flag indicating whether or not the specified cell is editable.
	 * 
	 * @param row
	 *           the row number.
	 * @param column
	 *           the column number.
	 * @return <code>true</code> if the specified cell is editable.
	 */
	public boolean isCellEditable(final int row, final int column) {
		return false;
	}

	/**
	 * Updates the {@link XYDataset} if allowed.
	 * 
	 * @param value
	 *           the new value.
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 */
	public void setValueAt(final Object value, final int row, final int column) {
		if (this.isCellEditable(row, column)) {
			// XYDataset only provides methods for reading a dataset...
		}
	}

	// /**
	// * Run a demonstration of the table model interface.
	// *
	// * @param args ignored.
	// *
	// * @throws exception when an error occurs.
	// */
	// public static void main(String args[]) throws Exception {
	// JFrame frame = new JFrame();
	// JPanel panel = new JPanel();
	// JScrollPane scroll = new JScrollPane();
	// panel.setLayout(new BorderLayout());
	//
	// XYDataset xyDataset = DemoDatasetFactory.createSampleXYDataset();
	// XYDatasetTableModel tablemodel = new XYDatasetTableModel();
	//
	// tablemodel.setModel(xyDataset);
	//
	// JTable dataTable = new JTable(tablemodel);
	// scroll.getViewport().add(dataTable, null);
	//
	// JFreeChart chart = ChartFactory.createXYLineChart(
	// "XY Series Demo",
	// "X", "Y", xyDataset, PlotOrientation.VERTICAL,
	// true,
	// true,
	// false
	// );
	//
	// ChartPanel chartPanel = new ChartPanel(chart);
	//
	// panel.add(chartPanel, BorderLayout.CENTER);
	// panel.add(scroll, BorderLayout.SOUTH);
	//
	// frame.setContentPane(panel);
	// frame.setSize(300, 500);
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// frame.show();
	// RefineryUtilities.centerFrameOnScreen(frame);
	// }

}
