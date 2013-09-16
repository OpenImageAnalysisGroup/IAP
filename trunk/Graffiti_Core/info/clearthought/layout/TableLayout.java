package info.clearthought.layout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.FolderPanel;

/**
 * TableLayout is a layout manager that arranges components in rows and columns
 * like a spreadsheet. TableLayout allows each row or column to be a different
 * size. A row or column can be given an absolute size in pixels, a percentage
 * of the available space, or it can grow and shrink to fill the remaining space
 * after other rows and columns have been resized.
 * <p>
 * Using spreadsheet terminology, a cell is the intersection of a row and column. Cells have finite, non-negative sizes measured in pixels. The dimensions of a
 * cell depend solely upon the dimensions of its row and column.
 * </p>
 * <p>
 * A component occupies a rectangular group of one or more cells. If the component occupies more than one cell, the component is resized to fit perfectly in the
 * rectangular region of cells. If the component occupies a single cell, it can be aligned in four ways within that cell.
 * </p>
 * <p>
 * A single cell component can be stretched horizontally to fit the cell (full justification), or it can be placed in the center of the cell. The component
 * could also be left justified or right justified. Similarly, the component can be full, center, top, or bottom justified in the vertical.
 * </p>
 * 
 * <pre>
 * public static void main (String args[])
 * {
 *     // Create a frame
 *     Frame frame = new Frame("Example of TableLayout");
 *     frame.setBounds (100, 100, 300, 300);
 * <spc>
 *     // Create a TableLayout for the frame
 *     double border = 10;
 *     double size[][] =
 *         {{border, 0.10, 20, TableLayout.FILL, 20, 0.20, border},  // Columns
 *          {border, 0.20, 20, TableLayout.FILL, 20, 0.20, border}}; // Rows
 * <spc>
 *     frame.setLayout (new TableLayout(size));
 * <spc>
 *     // Create some buttons
 *     String label[] = {"Top", "Bottom", "Left", "Right", "Center", "Overlap"};
 *     Button button[] = new Button[label.length];
 * <spc>
 *     for (int i = 0; i < label.length; i++)
 *         button[i] = new Button(label[i]);
 * <spc>
 *     // Add buttons
 *     frame.add (button[0], "1, 1, 5, 1"); // Top
 *     frame.add (button[1], "1, 5, 5, 5"); // Bottom
 *     frame.add (button[2], "1, 3      "); // Left
 *     frame.add (button[3], "5, 3      "); // Right
 *     frame.add (button[4], "3, 3, c, c"); // Center
 *     frame.add (button[5], "3, 3, 3, 5"); // Overlap
 * <spc>
 *     // Allow user to close the window to terminate the program
 *     frame.addWindowListener
 *         (new WindowListener()
 *             {
 *                 public void windowClosing (WindowEvent e)
 *                 {
 *                     System.exit (0);
 *                 }
 * <spc>
 *                 public void windowOpened (WindowEvent e) {}
 *                 public void windowClosed (WindowEvent e) {}
 *                 public void windowIconified (WindowEvent e) {}
 *                 public void windowDeiconified (WindowEvent e) {}
 *                 public void windowActivated (WindowEvent e) {}
 *                 public void windowDeactivated (WindowEvent e) {}
 *             }
 *         );
 * <spc>
 *     // Show frame
 *     frame.show();
 * }
 * </pre>
 * 
 * @author Daniel E. Barbalace
 */

public class TableLayout implements java.awt.LayoutManager2, java.io.Serializable, TableLayoutConstants {
	
	private static final long serialVersionUID = 1L;
	
	/** Default row/column size */
	protected static final double defaultSize[][] = { {}, {} };
	
	/** Widths of columns expressed in absolute and relative terms */
	protected double columnSpec[];
	
	/** Heights of rows expressed in absolute and relative terms */
	protected double rowSpec[];
	
	/** Widths of columns in pixels */
	protected int columnSize[];
	
	/** Heights of rows in pixels */
	protected int rowSize[];
	
	/**
	 * Offsets of columns in pixels. The left boarder of column n is at
	 * columnOffset[n] and the right boarder is at columnOffset[n + 1] for all
	 * columns including the last one. columnOffset.length = columnSize.length +
	 * 1
	 */
	protected int columnOffset[];
	
	/**
	 * Offsets of rows in pixels. The left boarder of row n is at rowOffset[n]
	 * and the right boarder is at rowOffset[n + 1] for all rows including the
	 * last one. rowOffset.length = rowSize.length + 1
	 */
	protected int rowOffset[];
	
	/** List of components and their sizes */
	protected LinkedList<Entry> list;
	
	/**
	 * Indicates whether or not the size of the cells are known for the last
	 * known size of the container. If dirty is true or the container has been
	 * resized, the cell sizes must be recalculated using calculateSize.
	 */
	protected boolean dirty;
	
	/** Previous known width of the container */
	protected int oldWidth;
	
	/** Previous known height of the container */
	protected int oldHeight;
	
	// ******************************************************************************
	// ** Constructors ***
	// ******************************************************************************
	
	/**
	 * Constructs an instance of TableLayout. This TableLayout will have one row
	 * and one column.
	 */
	
	public TableLayout() {
		this(defaultSize);
	}
	
	/**
	 * Constructs an instance of TableLayout.
	 * 
	 * @param size
	 *           widths of columns and heights of rows in the format, {{col0,
	 *           col1, col2, ..., colN}, {row0, row1, row2, ..., rowM}} If this
	 *           parameter is invalid, the TableLayout will have exactly one row
	 *           and one column.
	 */
	
	public TableLayout(double size[][]) {
		// Make sure rows and columns and nothing else is specified
		if ((size != null) && (size.length == 2)) {
			// Get the rows and columns
			double tempCol[] = size[0];
			double tempRow[] = size[1];
			
			// Create new rows and columns
			columnSpec = new double[tempCol.length];
			rowSpec = new double[tempRow.length];
			
			// Copy rows and columns
			System.arraycopy(tempCol, 0, columnSpec, 0, columnSpec.length);
			System.arraycopy(tempRow, 0, rowSpec, 0, rowSpec.length);
			
			// Make sure rows and columns are valid
			for (int counter = 0; counter < columnSpec.length; counter++)
				if ((columnSpec[counter] < 0.0) && (columnSpec[counter] != FILL) && (columnSpec[counter] != PREFERRED)
						&& (columnSpec[counter] != MINIMUM)) {
					columnSpec[counter] = 0.0;
				}
			
			for (int counter = 0; counter < rowSpec.length; counter++)
				if ((rowSpec[counter] < 0.0) && (rowSpec[counter] != FILL) && (rowSpec[counter] != PREFERRED)
						&& (rowSpec[counter] != MINIMUM)) {
					rowSpec[counter] = 0.0;
				}
		} else {
			double tempCol[] = { FILL };
			double tempRow[] = { FILL };
			
			setColumn(tempCol);
			setRow(tempRow);
		}
		
		// Create an empty list of components
		list = new LinkedList<Entry>();
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	// ******************************************************************************
	// ** Get/Set methods ***
	// ******************************************************************************
	
	/**
	 * Gets the constraints of a given component.
	 * 
	 * @param component
	 *           desired component
	 * @return If the given component is found, the constraints associated with
	 *         that component. If the given component is null or is not found,
	 *         null is returned.
	 */
	
	public TableLayoutConstraints getConstraints(Component component) {
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			
			if (entry.component == component)
				return new TableLayoutConstraints(entry.col1, entry.row1, entry.col2, entry.row2, entry.hAlign,
						entry.vAlign);
		}
		
		return null;
	}
	
	/**
	 * Sets the constraints of a given component.
	 * 
	 * @param component
	 *           desired component. This parameter cannot be null.
	 * @param constraint
	 *           new set of constraints. This parameter cannot be null.
	 * @return If the given component is found, the constraints associated with
	 *         that component. If the given component is null or is not found,
	 *         null is returned.
	 */
	
	public void setConstraints(Component component, TableLayoutConstraints constraint) {
		// Check parameters
		if (component == null)
			throw new IllegalArgumentException("Parameter component cannot be null.");
		else
			if (constraint == null)
				throw new IllegalArgumentException("Parameter constraint cannot be null.");
		
		// Find and update constraints for the given component
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			
			if (entry.component == component)
				iterator.set(new Entry(component, constraint));
		}
	}
	
	/**
	 * Adjusts the number and sizes of rows in this layout. After calling this
	 * method, the caller should request this layout manager to perform the
	 * layout. This can be done with the following code:
	 * 
	 * <pre>
	 * layout.layoutContainer(container);
	 * container.repaint();
	 * </pre>
	 * 
	 * or
	 * 
	 * <pre>
	 * window.pack()
	 * </pre>
	 * 
	 * If this is not done, the changes in the layout will not be seen until the
	 * container is resized.
	 * 
	 * @param column
	 *           heights of each of the columns
	 * @see getColumn
	 */
	
	public void setColumn(double column[]) {
		// Copy columns
		columnSpec = new double[column.length];
		System.arraycopy(column, 0, columnSpec, 0, columnSpec.length);
		
		// Make sure columns are valid
		for (int counter = 0; counter < columnSpec.length; counter++)
			if ((columnSpec[counter] < 0.0) && (columnSpec[counter] != FILL) && (columnSpec[counter] != PREFERRED)
					&& (columnSpec[counter] != MINIMUM)) {
				columnSpec[counter] = 0.0;
			}
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Adjusts the number and sizes of rows in this layout. After calling this
	 * method, the caller should request this layout manager to perform the
	 * layout. This can be done with the following code: <code>
	 *     layout.layoutContainer(container);
	 *     container.repaint();
	 * </code> or
	 * 
	 * <pre>
	 * window.pack()
	 * </pre>
	 * 
	 * If this is not done, the changes in the layout will not be seen until the
	 * container is resized.
	 * 
	 * @param row
	 *           widths of each of the rows. This parameter cannot be null.
	 * @see getRow
	 */
	
	public void setRow(double row[]) {
		// Copy rows
		rowSpec = new double[row.length];
		System.arraycopy(row, 0, rowSpec, 0, rowSpec.length);
		
		// Make sure rows are valid
		for (int counter = 0; counter < rowSpec.length; counter++)
			if ((rowSpec[counter] < 0.0) && (rowSpec[counter] != FILL) && (rowSpec[counter] != PREFERRED)
					&& (rowSpec[counter] != MINIMUM)) {
				rowSpec[counter] = 0.0;
			}
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Adjusts the width of a single column in this layout. After calling this
	 * method, the caller should request this layout manager to perform the
	 * layout. This can be done with the following code: <code>
	 *     layout.layoutContainer(container);
	 *     container.repaint();
	 * </code> or
	 * 
	 * <pre>
	 * window.pack()
	 * </pre>
	 * 
	 * If this is not done, the changes in the layout will not be seen until the
	 * container is resized.
	 * 
	 * @param i
	 *           zero-based index of column to set. If this parameter is not
	 *           valid, an ArrayOutOfBoundsException will be thrown.
	 * @param size
	 *           width of the column. This parameter cannot be null.
	 * @see getColumn
	 */
	
	public void setColumn(int i, double size) {
		// Make sure size is valid
		if ((size < 0.0) && (size != FILL) && (size != PREFERRED) && (size != MINIMUM)) {
			size = 0.0;
		}
		
		// Copy new size
		columnSpec[i] = size;
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Adjusts the height of a single row in this layout. After calling this
	 * method, the caller should request this layout manager to perform the
	 * layout. This can be done with the following code: <code>
	 *     layout.layoutContainer(container);
	 *     container.repaint();
	 * </code> or
	 * 
	 * <pre>
	 * window.pack()
	 * </pre>
	 * 
	 * If this is not done, the changes in the layout will not be seen until the
	 * container is resized.
	 * 
	 * @param i
	 *           zero-based index of row to set. If this parameter is not valid,
	 *           an ArrayOutOfBoundsException will be thrown.
	 * @param size
	 *           height of the row. This parameter cannot be null.
	 * @see getRow
	 */
	
	public void setRow(int i, double size) {
		// Make sure size is valid
		if ((size < 0.0) && (size != FILL) && (size != PREFERRED) && (size != MINIMUM)) {
			size = 0.0;
		}
		
		// Copy new size
		rowSpec[i] = size;
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Gets the sizes of columns in this layout.
	 * 
	 * @return widths of each of the columns
	 * @see setColumn
	 */
	
	public double[] getColumn() {
		// Copy columns
		double column[] = new double[columnSpec.length];
		System.arraycopy(columnSpec, 0, column, 0, column.length);
		
		return column;
	}
	
	/**
	 * Gets the height of a single row in this layout.
	 * 
	 * @return height of the requested row
	 * @see setRow
	 */
	
	public double[] getRow() {
		// Copy rows
		double row[] = new double[rowSpec.length];
		System.arraycopy(rowSpec, 0, row, 0, row.length);
		
		return row;
	}
	
	/**
	 * Gets the width of a single column in this layout.
	 * 
	 * @param i
	 *           zero-based index of row to get. If this parameter is not valid,
	 *           an ArrayOutOfBoundsException will be thrown.
	 * @return width of the requested column
	 * @see setRow
	 */
	
	public double getColumn(int i) {
		return columnSpec[i];
	}
	
	/**
	 * Gets the sizes of a row in this layout.
	 * 
	 * @param i
	 *           zero-based index of row to get. If this parameter is not valid,
	 *           an ArrayOutOfBoundsException will be thrown.
	 * @return height of each of the requested row
	 * @see setRow
	 */
	
	public double getRow(int i) {
		return rowSpec[i];
	}
	
	/**
	 * Gets the number of columns in this layout.
	 * 
	 * @return the number of columns
	 */
	
	public int getNumColumn() {
		return columnSpec.length;
	}
	
	/**
	 * Gets the number of rows in this layout.
	 * 
	 * @return the number of rows
	 */
	
	public int getNumRow() {
		return rowSpec.length;
	}
	
	// ******************************************************************************
	// ** Insertion/Deletion methods ***
	// ******************************************************************************
	
	/**
	 * Inserts a column in this layout. All components to the right of the
	 * insertion point are moved right one column. The container will need to be
	 * laid out after this method returns. See <code>setColumn</code>.
	 * 
	 * @param i
	 *           zero-based index at which to insert the column.
	 * @param size
	 *           size of the column to be inserted
	 * @see setColumn
	 * @see deleteColumn
	 */
	
	public void insertColumn(int i, double size) {
		// Make sure position is valid
		if ((i < 0) || (i > columnSpec.length))
			throw new IllegalArgumentException("Parameter i is invalid.  i = " + i + ".  Valid range is [0, "
					+ columnSpec.length + "].");
		
		// Make sure column size is valid
		if ((size < 0.0) && (size != FILL) && (size != PREFERRED) && (size != MINIMUM)) {
			size = 0.0;
		}
		
		// Copy columns
		double column[] = new double[columnSpec.length + 1];
		System.arraycopy(columnSpec, 0, column, 0, i);
		System.arraycopy(columnSpec, i, column, i + 1, columnSpec.length - i);
		
		// Insert column
		column[i] = size;
		columnSpec = column;
		
		// Move all components that are to the right of new row
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Is the first column to the right of the new column
			if (entry.col1 >= i)
				// Move first column
				entry.col1++;
			
			// Is the second column to the right of the new column
			if (entry.col2 >= i)
				// Move second column
				entry.col2++;
		}
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Inserts a row in this layout. All components below the insertion point are
	 * moved down one row. The container will need to be laid out after this
	 * method returns. See <code>setRow</code>.
	 * 
	 * @param i
	 *           zero-based index at which to insert the column.
	 * @param size
	 *           size of the row to be inserted
	 * @see setRow
	 * @see deleteRow
	 */
	
	public void insertRow(int i, double size) {
		// Make sure position is valid
		if ((i < 0) || (i > rowSpec.length))
			throw new IllegalArgumentException("Parameter i is invalid.  i = " + i + ".  Valid range is [0, "
					+ rowSpec.length + "].");
		
		// Make sure row size is valid
		if ((size < 0.0) && (size != FILL) && (size != PREFERRED) && (size != MINIMUM)) {
			size = 0.0;
		}
		
		// Copy rows
		double row[] = new double[rowSpec.length + 1];
		System.arraycopy(rowSpec, 0, row, 0, i);
		System.arraycopy(rowSpec, i, row, i + 1, rowSpec.length - i);
		
		// Insert row
		row[i] = size;
		rowSpec = row;
		
		// Move all components that are below the new row
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Is the first row to the right of the new row
			if (entry.row1 >= i)
				// Move first row
				entry.row1++;
			
			// Is the second row to the right of the new row
			if (entry.row2 >= i)
				// Move second row
				entry.row2++;
		}
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Deletes a column in this layout. All components to the right of the
	 * deletion point are moved left one column. The container will need to be
	 * laid out after this method returns. See <code>setColumn</code>.
	 * 
	 * @param i
	 *           zero-based index of column to delete
	 * @see setColumn
	 * @see deleteColumn
	 */
	
	public void deleteColumn(int i) {
		// Make sure position is valid
		if ((i < 0) || (i >= columnSpec.length))
			throw new IllegalArgumentException("Parameter i is invalid.  i = " + i + ".  Valid range is [0, "
					+ (columnSpec.length - 1) + "].");
		
		// Copy columns
		double column[] = new double[columnSpec.length - 1];
		System.arraycopy(columnSpec, 0, column, 0, i);
		System.arraycopy(columnSpec, i + 1, column, i, columnSpec.length - i - 1);
		
		// Delete column
		columnSpec = column;
		
		// Move all components that are to the right of row deleted
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Is the first column to the right of the new column
			if (entry.col1 >= i)
				// Move first column
				entry.col1--;
			
			// Is the second column to the right of the new column
			if (entry.col2 >= i)
				// Move second column
				entry.col2--;
		}
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	/**
	 * Deletes a row in this layout. All components below the deletion point are
	 * moved up one row. The container will need to be laid out after this method
	 * returns. See <code>setRow</code>. There must be at least two rows in order
	 * to delete a row.
	 * 
	 * @param i
	 *           zero-based index of column to delete
	 * @see setRow
	 * @see deleteRow
	 */
	
	public void deleteRow(int i) {
		// Make sure position is valid
		if ((i < 0) || (i >= rowSpec.length))
			throw new IllegalArgumentException("Parameter i is invalid.  i = " + i + ".  Valid range is [0, "
					+ (rowSpec.length - 1) + "].");
		
		// Copy rows
		double row[] = new double[rowSpec.length - 1];
		System.arraycopy(rowSpec, 0, row, 0, i);
		System.arraycopy(rowSpec, i + 1, row, i, rowSpec.length - i - 1);
		
		// Delete row
		rowSpec = row;
		
		// Move all components that are to below the row deleted
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Is the first row below the new row
			if (entry.row1 >= i)
				// Move first row
				entry.row1--;
			
			// Is the second row below the new row
			if (entry.row2 >= i)
				// Move second row
				entry.row2--;
		}
		
		// Indicate that the cell sizes are not known
		dirty = true;
	}
	
	// ******************************************************************************
	// ** Misc methods ***
	// ******************************************************************************
	
	/**
	 * Converts this TableLayout to a string.
	 * 
	 * @return a string representing the columns and row sizes in the form
	 *         "{{col0, col1, col2, ..., colN}, {row0, row1, row2, ..., rowM}}"
	 */
	
	@Override
	public String toString() {
		int counter;
		
		String value = "TableLayout {{";
		
		if (columnSpec.length > 0) {
			for (counter = 0; counter < columnSpec.length - 1; counter++)
				value += columnSpec[counter] + ", ";
			
			value += columnSpec[columnSpec.length - 1] + "}, {";
		} else
			value += "}, {";
		
		if (rowSpec.length > 0) {
			for (counter = 0; counter < rowSpec.length - 1; counter++)
				value += rowSpec[counter] + ", ";
			
			value += rowSpec[rowSpec.length - 1] + "}}";
		} else
			value += "}}";
		
		return value;
	}
	
	/**
	 * Draws a grid on the given container. This is useful for seeing where the
	 * rows and columns go. In the container's paint method, call this method.
	 * 
	 * @param container
	 *           container using this TableLayout
	 * @param g
	 *           graphics content of container (can be offscreen)
	 */
	
	public void drawGrid(Container container, Graphics g) {
		// Calculate the sizes of the rows and columns
		Dimension d = container.getSize();
		
		if (dirty || (d.width != oldWidth) || (d.height != oldHeight))
			calculateSize(container);
		
		// Initialize y
		int y = 0;
		
		for (int row = 0; row < rowSize.length; row++) {
			// Initialize x
			int x = 0;
			
			for (int column = 0; column < columnSize.length; column++) {
				// Use a random color to make things easy to see
				Color color = new Color((int) (Math.random() * 0xFFFFFFL));
				g.setColor(color);
				
				// Draw the cell as a solid rectangle
				g.fillRect(x, y, columnSize[column], rowSize[row]);
				
				// Increment x
				x += columnSize[column];
			}
			
			// Increment y
			y += rowSize[row];
		}
	}
	
	/**
	 * Determines whether or not there are any hidden components. A hidden
	 * component is one that will not be shown with this layout's current
	 * configuration. Such a component is, at least partly, in an invalid row or
	 * column. For example, on a table with five rows, row -1 and row 5 are both
	 * invalid. Valid rows are 0 through 4, inclusively.
	 * 
	 * @return True, if there are any hidden components. False, otherwise.
	 * @see overlapping
	 */
	
	public boolean hidden() {
		// Assume no components are hidden
		boolean hidden = false;
		
		// Check all components
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Is this component valid
			if ((entry.row1 < 0) || (entry.col1 < 0) || (entry.row2 > rowSpec.length) || (entry.col2 > columnSpec.length)) {
				hidden = true;
				break;
			}
		}
		
		return hidden;
	}
	
	/**
	 * Determines whether or not there are any overlapping components. Two
	 * components overlap if they cover at least one common cell.
	 * 
	 * @return True, if there are any overlapping components. False, otherwise.
	 * @see hidden
	 */
	
	public boolean overlapping() {
		// Count contraints
		int numEntry = list.size();
		
		// If there are no components, they can't be overlapping
		if (numEntry == 0)
			return false;
		
		// Assume no components are overlapping
		boolean overlapping = false;
		
		// Put entries in an array
		Entry entry[] = list.toArray(new Entry[numEntry]);
		
		// Check all components
		for (int knowUnique = 1; knowUnique < numEntry; knowUnique++)
			for (int checking = knowUnique - 1; checking >= 0; checking--)
				if (((entry[checking].col1 >= entry[knowUnique].col1) && (entry[checking].col1 <= entry[knowUnique].col2)
						&& (entry[checking].row1 >= entry[knowUnique].row1) && (entry[checking].row1 <= entry[knowUnique].row2))
						|| ((entry[checking].col2 >= entry[knowUnique].col1)
								&& (entry[checking].col2 <= entry[knowUnique].col2)
								&& (entry[checking].row2 >= entry[knowUnique].row1) && (entry[checking].row2 <= entry[knowUnique].row2))) {
					overlapping = true;
					break;
				}
		
		return overlapping;
	}
	
	/**
	 * Calculates the sizes of the rows and columns based on the absolute and
	 * relative sizes specified in <code>rowSpec</code> and <code>columnSpec</code> and the size of the container. The result is
	 * stored in <code>rowSize</code> and <code>columnSize</code>.
	 * 
	 * @param container
	 *           container using this TableLayout
	 */
	
	protected void calculateSize(Container container) {
		int counter; // Counting variable;
		
		// Get number of rows and columns
		int numColumn = columnSpec.length;
		int numRow = rowSpec.length;
		
		// Create array to hold actual sizes in pixels
		columnSize = new int[numColumn];
		rowSize = new int[numRow];
		
		// Get the container's insets
		Insets inset = container.getInsets();
		
		// Get the size of the container's available space
		Dimension d = container.getSize();
		int totalWidth = d.width - inset.left - inset.right;
		int totalHeight = d.height - inset.top - inset.bottom;
		
		// Initially, the available space is the total space
		int availableWidth = totalWidth;
		int availableHeight = totalHeight;
		
		// Assign absolute widths; this reduces available width
		for (counter = 0; counter < numColumn; counter++)
			// Is the current column an absolue size
			if ((columnSpec[counter] >= 1.0) || (columnSpec[counter] == 0.0)) {
				// Assign absolute width
				columnSize[counter] = (int) (columnSpec[counter] + 0.5);
				
				// Reduce available width
				availableWidth -= columnSize[counter];
			}
		
		// Assign absolute heights; this reduces available height
		for (counter = 0; counter < numRow; counter++)
			// Is the current column an absolue size
			if ((rowSpec[counter] >= 1.0) || (rowSpec[counter] == 0.0)) {
				// Assign absolute width
				rowSize[counter] = (int) (rowSpec[counter] + 0.5);
				
				// Reduce available width
				availableHeight -= rowSize[counter];
			}
		
		// Assign preferred and minimum widths; this reduces available width.
		// Assignment of preferred/minimum with is like assignment of absolute
		// widths except that each column must determine the maximum
		// preferred/minimum width of the components that are completely contained
		// within the column.
		for (counter = 0; counter < numColumn; counter++)
			// Is the current column a preferred size
			if ((columnSpec[counter] == PREFERRED) || (columnSpec[counter] == MINIMUM)) {
				// Assume a maximum width of zero
				int maxWidth = 0;
				
				// Find maximum preferred width of all components completely
				// contained within this column
				ListIterator<Entry> iterator = list.listIterator(0);
				
				while (iterator.hasNext()) {
					Entry entry = iterator.next();
					
					if ((entry.col1 == counter) && (entry.col2 == counter)) {
						Dimension p = (columnSpec[counter] == PREFERRED) ? entry.component.getPreferredSize()
								: entry.component.getMinimumSize();
						
						int width = (p == null) ? 0 : p.width;
						
						if (maxWidth < width)
							maxWidth = width;
					}
				}
				
				// Assign preferred width
				columnSize[counter] = maxWidth;
				
				// Reduce available width
				availableWidth -= maxWidth;
			}
		
		// Assign preferred and minimum heights; this reduces available height.
		// Assignment of preferred/minimum with is like assignment of absolute
		// heights except that each row must determine the maximum
		// preferred/minimum height of the components that are completely
		// contained
		// within the row.
		for (counter = 0; counter < numRow; counter++)
			// Is the current row a preferred size
			if ((rowSpec[counter] == PREFERRED) || (rowSpec[counter] == MINIMUM)) {
				// Assume a maximum height of zero
				int maxHeight = 0;
				
				// Find maximum preferred height of all components completely
				// contained within this row
				ListIterator<Entry> iterator = list.listIterator(0);
				
				while (iterator.hasNext()) {
					Entry entry = iterator.next();
					
					if ((entry.row1 == counter) && (entry.row2 == counter)) {
						Dimension p = (rowSpec[counter] == PREFERRED) ? entry.component.getPreferredSize() : entry.component
								.getMinimumSize();
						
						int height = (p == null) ? 0 : p.height;
						
						if (maxHeight < height)
							maxHeight = height;
					}
				}
				
				// Assign preferred height
				rowSize[counter] = maxHeight;
				
				// Reduce available height
				availableHeight -= maxHeight;
			}
		
		// Remember how much space is available for relatively sized cells
		int relativeWidth = availableWidth;
		int relativeHeight = availableHeight;
		
		// Make sure relativeWidth and relativeHeight are non-negative
		if (relativeWidth < 0)
			relativeWidth = 0;
		
		if (relativeHeight < 0)
			relativeHeight = 0;
		
		// Assign relative widths
		for (counter = 0; counter < numColumn; counter++)
			// Is the current column an relative size
			if ((columnSpec[counter] > 0.0) && (columnSpec[counter] < 1.0)) {
				// Assign relative width
				columnSize[counter] = (int) (columnSpec[counter] * relativeWidth + 0.5);
				
				// Reduce available width
				availableWidth -= columnSize[counter];
			}
		
		// Assign relative widths
		for (counter = 0; counter < numRow; counter++)
			// Is the current column an relative size
			if ((rowSpec[counter] > 0.0) && (rowSpec[counter] < 1.0)) {
				// Assign relative width
				rowSize[counter] = (int) (rowSpec[counter] * relativeHeight + 0.5);
				
				// Reduce available width
				availableHeight -= rowSize[counter];
			}
		
		// Make sure availableWidth and availableHeight are non-negative
		if (availableWidth < 0)
			availableWidth = 0;
		
		if (availableHeight < 0)
			availableHeight = 0;
		
		// Count the number of "fill" cells
		int numFillWidth = 0;
		int numFillHeight = 0;
		
		for (counter = 0; counter < numColumn; counter++)
			if (columnSpec[counter] == FILL)
				numFillWidth++;
		
		for (counter = 0; counter < numRow; counter++)
			if (rowSpec[counter] == FILL)
				numFillHeight++;
		
		// If numFillWidth (numFillHeight) is zero, the cooresponding if
		// statements
		// will always evaluate to false and the division will not occur.
		
		// If there are more than one "fill" cell, slack may occur due to rounding
		// errors
		int slackWidth = availableWidth;
		int slackHeight = availableHeight;
		
		// Assign "fill" cells equal amounts of the remaining space
		for (counter = 0; counter < numColumn; counter++)
			if (columnSpec[counter] == FILL) {
				columnSize[counter] = availableWidth / numFillWidth;
				slackWidth -= columnSize[counter];
			}
		
		for (counter = 0; counter < numRow; counter++)
			if (rowSpec[counter] == FILL) {
				rowSize[counter] = availableHeight / numFillHeight;
				slackHeight -= rowSize[counter];
			}
		
		// Add slack to the last "fill" cell
		for (counter = numColumn - 1; counter >= 0; counter--) {
			if (columnSpec[counter] == FILL) {
				// columnSize[counter] += slackWidth;
				break;
			}
		}
		
		for (counter = numRow - 1; counter >= 0; counter--) {
			if (rowSpec[counter] == FILL) {
				// rowSize[counter] += slackHeight;
				break;
			}
		}
		
		// Calculate offsets of each column (done for effeciency)
		columnOffset = new int[numColumn + 1];
		columnOffset[0] = inset.left;
		
		for (counter = 0; counter < numColumn; counter++)
			columnOffset[counter + 1] = columnOffset[counter] + columnSize[counter];
		
		// Calculate offsets of each row (done for effeciency)
		rowOffset = new int[numRow + 1];
		rowOffset[0] = inset.top;
		
		for (counter = 0; counter < numRow; counter++)
			rowOffset[counter + 1] = rowOffset[counter] + rowSize[counter];
		
		// Indicate that the size of the cells are known for the container's
		// current size
		dirty = false;
		oldWidth = totalWidth;
		oldHeight = totalHeight;
	}
	
	// ******************************************************************************
	// ** java.awt.event.LayoutManager methods ***
	// ******************************************************************************
	
	/**
	 * To lay out the specified container using this layout. This method reshapes
	 * the components in the specified target container in order to satisfy the
	 * constraints of all components.
	 * <p>
	 * User code should not have to call this method directly.
	 * </p>
	 * 
	 * @param container
	 *           container being served by this layout manager
	 */
	
	@Override
	public void layoutContainer(Container container) {
		int x, y; // Coordinates of the currnet component in pixels
		int w, h; // Width and height of the current component in pixels
		
		// Calculate sizes if container has changed size or components were added
		Dimension d = container.getSize();
		
		if (dirty || (d.width != oldWidth) || (d.height != oldHeight))
			calculateSize(container);
		
		// Get components
		Component component[] = container.getComponents();
		
		// Layout components
		for (int counter = 0; counter < component.length; counter++) {
			try {
				// Get the entry entry for the next component
				ListIterator<Entry> iterator = list.listIterator(0);
				Entry entry = null;
				
				while (iterator.hasNext()) {
					entry = iterator.next();
					
					if (entry.component == component[counter])
						break;
					else
						entry = null;
				}
				
				// Skip any components that have not been place in a specific cell
				if (entry == null)
					break;
				
				// Does the entry occupy a single cell
				if (entry.singleCell) {
					// The following block of code has been optimized so that the
					// preferred size of the component is only obtained if it is
					// needed. There are components in which the getPreferredSize
					// method is extremely expensive, such as data driven controls
					// with a large amount of data.
					
					// Get the preferred size of the component
					int preferredWidth = 0;
					int preferredHeight = 0;
					
					if ((entry.hAlign != FULL) || (entry.vAlign != FULL)) {
						Dimension preferredSize = component[counter].getPreferredSize();
						
						preferredWidth = preferredSize.width;
						preferredHeight = preferredSize.height;
					}
					
					// Determine cell width and height
					int cellWidth = columnSize[entry.col1];
					int cellHeight = rowSize[entry.row1];
					
					// Determine the width of the component
					if ((entry.hAlign == FULL) || (cellWidth < preferredWidth))
						// Use the width of the cell
						w = cellWidth;
					else
						// Use the prefered width of the component
						w = preferredWidth;
					
					// Determine left and right boarders
					switch (entry.hAlign) {
						case LEFT:
							// Align left side along left edge of cell
							x = columnOffset[entry.col1];
							break;
						
						case RIGHT:
							// Align right side along right edge of cell
							x = columnOffset[entry.col1 + 1] - w;
							break;
						
						case CENTER:
							// Center justify component
							x = columnOffset[entry.col1] + ((cellWidth - w) >> 1);
							break;
						
						case FULL:
							// Align left side along left edge of cell
							x = columnOffset[entry.col1];
							break;
						
						default:
							// This is a never should happen case, but just in case
							x = 0;
					}
					
					// Determine the height of the component
					if ((entry.vAlign == FULL) || (cellHeight < preferredHeight))
						// Use the height of the cell
						h = cellHeight;
					else
						// Use the prefered height of the component
						h = preferredHeight;
					
					// Determine top and bottom boarders
					switch (entry.vAlign) {
						case TOP:
							// Align top side along top edge of cell
							y = rowOffset[entry.row1];
							break;
						
						case BOTTOM:
							// Align right side along right edge of cell
							y = rowOffset[entry.row1 + 1] - h;
							break;
						
						case CENTER:
							// Center justify component
							y = rowOffset[entry.row1] + ((cellHeight - h) >> 1);
							break;
						
						case FULL:
							// Align right side along right edge of cell
							y = rowOffset[entry.row1];
							break;
						
						default:
							// This is a never should happen case, but just in case
							y = 0;
					}
				} else {
					// Align left side with left boarder of first column
					x = columnOffset[entry.col1];
					
					// Align top side along top edge of first row
					y = rowOffset[entry.row1];
					
					// Align right side with right boarder of second column
					w = columnOffset[entry.col2 + 1] - columnOffset[entry.col1];
					
					// Align bottom side with bottom boarder of second row
					h = rowOffset[entry.row2 + 1] - rowOffset[entry.row1];
				}
				
				// Move and resize component
				component[counter].setBounds(x, y, w, h);
			} catch (Exception error) {
				// If any error occurs, skip this component
				continue;
			}
		}
	}
	
	/**
	 * Determines the preferred size of the container argument using this layout.
	 * The preferred size is the smallest size that, if used for the container's
	 * size, will ensure that all components are at least as large as their
	 * preferred size. This method cannot guarantee that all components will be
	 * their preferred size. For example, if component A and component B are each
	 * allocate half of the container's width and component A wants to be 10
	 * pixels wide while component B wants to be 100 pixels wide, they cannot
	 * both be accommodated. Since in general components rather be larger than
	 * their preferred size instead of smaller, component B's request will be
	 * fulfilled. The preferred size of the container would be 200 pixels.
	 * 
	 * @param container
	 *           container being served by this layout manager
	 * @return a dimension indicating the container's preferred size
	 */
	
	@Override
	public Dimension preferredLayoutSize(Container container) {
		Dimension size; // Preferred size of current component
		int scaledWidth = 0; // Preferred width of scalled components
		int scaledHeight = 0; // Preferred height of scalled components
		int temp; // Temporary variable used to compare sizes
		int counter; // Counting variable
		
		// Determine percentage of space allocated to fill components. This is
		// one minus the sum of all scalable components.
		double fillWidthRatio = 1.0;
		double fillHeightRatio = 1.0;
		int numFillWidth = 0;
		int numFillHeight = 0;
		
		for (counter = 0; counter < columnSpec.length; counter++)
			if ((columnSpec[counter] > 0.0) && (columnSpec[counter] < 1.0))
				fillWidthRatio -= columnSpec[counter];
			else
				if (columnSpec[counter] == FILL)
					numFillWidth++;
		
		for (counter = 0; counter < rowSpec.length; counter++)
			if ((rowSpec[counter] > 0.0) && (rowSpec[counter] < 1.0))
				fillHeightRatio -= rowSpec[counter];
			else
				if (rowSpec[counter] == FILL)
					numFillHeight++;
		
		// Adjust fill ratios to reflect number of fill rows/columns
		if (numFillWidth > 1)
			fillWidthRatio /= numFillWidth;
		
		if (numFillHeight > 1)
			fillHeightRatio /= numFillHeight;
		
		// Cap fill ratio bottoms to 0.0
		if (fillWidthRatio < 0.0)
			fillWidthRatio = 0.0;
		
		if (fillHeightRatio < 0.0)
			fillHeightRatio = 0.0;
		
		// Calculate preferred/minimum column widths
		int columnPrefMin[] = new int[columnSpec.length];
		
		for (counter = 0; counter < columnSpec.length; counter++)
			// Is the current column a preferred/minimum size
			if ((columnSpec[counter] == PREFERRED) || (columnSpec[counter] == MINIMUM)) {
				// Assume a maximum width of zero
				int maxWidth = 0;
				
				// Find maximum preferred/minimum width of all components completely
				// contained within this column
				ListIterator<Entry> iterator = list.listIterator(0);
				
				while (iterator.hasNext()) {
					Entry entry = iterator.next();
					
					if ((entry.col1 == counter) && (entry.col2 == counter)) {
						Dimension p = (columnSpec[counter] == PREFERRED) ? entry.component.getPreferredSize()
								: entry.component.getMinimumSize();
						
						int width = (p == null) ? 0 : p.width;
						
						if (maxWidth < width)
							maxWidth = width;
					}
				}
				
				// Set column's preferred/minimum width
				columnPrefMin[counter] = maxWidth;
			}
		
		// Calculate preferred/minimum row heights
		int rowPrefMin[] = new int[rowSpec.length];
		
		for (counter = 0; counter < rowSpec.length; counter++)
			// Is the current row a preferred/minimum size
			if ((rowSpec[counter] == PREFERRED) || (rowSpec[counter] == MINIMUM)) {
				// Assume a maximum height of zero
				int maxHeight = 0;
				
				// Find maximum preferred height of all components completely
				// contained within this row
				ListIterator<Entry> iterator = list.listIterator(0);
				
				while (iterator.hasNext()) {
					Entry entry = iterator.next();
					
					if ((entry.row1 == counter) && (entry.row1 == counter)) {
						Dimension p = (rowSpec[counter] == PREFERRED) ? entry.component.getPreferredSize() : entry.component
								.getMinimumSize();
						
						int height = (p == null) ? 0 : p.height;
						
						if (maxHeight < height)
							maxHeight = height;
					}
				}
				
				// Add preferred height
				rowPrefMin[counter] += maxHeight;
			}
		
		// Find maximum preferred size of all scaled components
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Make sure entry is in valid rows and columns
			if ((entry.col1 < 0) || (entry.col1 >= columnSpec.length) || (entry.col2 >= columnSpec.length)
					|| (entry.row1 < 0) || (entry.row1 >= rowSpec.length) || (entry.row2 >= rowSpec.length)) {
				// Skip the bad component
				continue;
			}
			
			// Get preferred size of current component
			size = entry.component.getPreferredSize();
			
			// Calculate portion of component that is not absolutely sized
			int scalableWidth = size.width;
			int scalableHeight = size.height;
			
			for (counter = entry.col1; counter <= entry.col2; counter++)
				if (columnSpec[counter] >= 1.0)
					scalableWidth -= columnSpec[counter];
				else
					if ((columnSpec[counter] == PREFERRED) || (columnSpec[counter] == MINIMUM)) {
						scalableWidth -= columnPrefMin[counter];
					}
			
			for (counter = entry.row1; counter <= entry.row2; counter++)
				if (rowSpec[counter] >= 1.0)
					scalableHeight -= rowSpec[counter];
				else
					if ((rowSpec[counter] == PREFERRED) || (rowSpec[counter] == MINIMUM)) {
						scalableHeight -= rowPrefMin[counter];
					}
			
			// ----------------------------------------------------------------------
			
			// Determine total percentage of scalable space that the component
			// occupies by adding the relative columns and the fill columns
			double relativeWidth = 0.0;
			
			for (counter = entry.col1; counter <= entry.col2; counter++) {
				// Column is scaled
				if ((columnSpec[counter] > 0.0) && (columnSpec[counter] < 1.0))
					// Add scaled size to relativeWidth
					relativeWidth += columnSpec[counter];
				// Column is fill
				else
					if ((columnSpec[counter] == FILL) && (fillWidthRatio != 0.0))
						// Add fill size to relativeWidth
						relativeWidth += fillWidthRatio;
			}
			
			// Determine the total scaled width as estimated by this component
			if (relativeWidth == 0)
				temp = 0;
			else
				temp = (int) (scalableWidth / relativeWidth + 0.5);
			
			// If the container needs to be bigger, make it so
			if (scaledWidth < temp)
				scaledWidth = temp;
			
			// ----------------------------------------------------------------------
			
			// Determine total percentage of scalable space that the component
			// occupies by adding the relative columns and the fill columns
			double relativeHeight = 0.0;
			
			for (counter = entry.row1; counter <= entry.row2; counter++) {
				// Row is scaled
				if ((rowSpec[counter] > 0.0) && (rowSpec[counter] < 1.0))
					// Add scaled size to relativeHeight
					relativeHeight += rowSpec[counter];
				// Row is fill
				else
					if ((rowSpec[counter] == FILL) && (fillHeightRatio != 0.0))
						// Add fill size to relativeHeight
						relativeHeight += fillHeightRatio;
			}
			
			// Determine the total scaled width as estimated by this component
			if (relativeHeight == 0)
				temp = 0;
			else
				temp = (int) (scalableHeight / relativeHeight + 0.5);
			
			// If the container needs to be bigger, make it so
			if (scaledHeight < temp)
				scaledHeight = temp;
		}
		
		// totalWidth is the scaledWidth plus the sum of all absolute widths and
		// all
		// preferred widths
		int totalWidth = scaledWidth;
		
		for (counter = 0; counter < columnSpec.length; counter++)
			// Is the current column an absolute size
			if (columnSpec[counter] >= 1.0)
				totalWidth += (int) (columnSpec[counter] + 0.5);
			// Is the current column a preferred/minimum size
			else
				if ((columnSpec[counter] == PREFERRED) || (columnSpec[counter] == MINIMUM)) {
					// Add preferred/minimum width
					totalWidth += columnPrefMin[counter];
				}
		
		// totalHeight is the scaledHeight plus the sum of all absolute heights
		// and
		// all preferred widths
		int totalHeight = scaledHeight;
		
		for (counter = 0; counter < rowSpec.length; counter++)
			// Is the current row an absolute size
			if (rowSpec[counter] >= 1.0)
				totalHeight += (int) (rowSpec[counter] + 0.5);
			// Is the current row a preferred size
			else
				if ((rowSpec[counter] == PREFERRED) || (rowSpec[counter] == MINIMUM)) {
					// Add preferred/minimum width
					totalHeight += rowPrefMin[counter];
				}
		
		// Compensate for container's insets
		Insets inset = container.getInsets();
		totalWidth += inset.left + inset.right;
		totalHeight += inset.top + inset.bottom;
		
		return new Dimension(totalWidth, totalHeight);
	}
	
	/**
	 * Determines the minimum size of the container argument using this layout.
	 * The minimum size is the smallest size that, if used for the container's
	 * size, will ensure that all components are at least as large as their
	 * minimum size. This method cannot guarantee that all components will be
	 * their minimum size. For example, if component A and component B are each
	 * allocate half of the container's width and component A wants to be 10
	 * pixels wide while component B wants to be 100 pixels wide, they cannot
	 * both be accommodated. Since in general components rather be larger than
	 * their minimum size instead of smaller, component B's request will be
	 * fulfilled. The minimum size of the container would be 200 pixels.
	 * 
	 * @param container
	 *           container being served by this layout manager
	 * @return a dimension indicating the container's minimum size
	 */
	
	@Override
	public Dimension minimumLayoutSize(Container container) {
		Dimension size; // Minimum size of current component
		int scaledWidth = 0; // Minimum width of scalled components
		int scaledHeight = 0; // Minimum height of scalled components
		int temp; // Temporary variable used to compare sizes
		int counter; // Counting variable
		
		// Determine percentage of space allocated to fill components. This is
		// one minus the sum of all scalable components.
		double fillWidthRatio = 1.0;
		double fillHeightRatio = 1.0;
		int numFillWidth = 0;
		int numFillHeight = 0;
		
		for (counter = 0; counter < columnSpec.length; counter++)
			if ((columnSpec[counter] > 0.0) && (columnSpec[counter] < 1.0))
				fillWidthRatio -= columnSpec[counter];
			else
				if (columnSpec[counter] == FILL)
					numFillWidth++;
		
		for (counter = 0; counter < rowSpec.length; counter++)
			if ((rowSpec[counter] > 0.0) && (rowSpec[counter] < 1.0))
				fillHeightRatio -= rowSpec[counter];
			else
				if (rowSpec[counter] == FILL)
					numFillHeight++;
		
		// Adjust fill ratios to reflect number of fill rows/columns
		if (numFillWidth > 1)
			fillWidthRatio /= numFillWidth;
		
		if (numFillHeight > 1)
			fillHeightRatio /= numFillHeight;
		
		// Cap fill ratio bottoms to 0.0
		if (fillWidthRatio < 0.0)
			fillWidthRatio = 0.0;
		
		if (fillHeightRatio < 0.0)
			fillHeightRatio = 0.0;
		
		// Find maximum minimum size of all scaled components
		ListIterator<Entry> iterator = list.listIterator(0);
		
		while (iterator.hasNext()) {
			// Get next entry
			Entry entry = iterator.next();
			
			// Make sure entry is in valid rows and columns
			if ((entry.col1 < 0) || (entry.col1 >= columnSpec.length) || (entry.col2 >= columnSpec.length)
					|| (entry.row1 < 0) || (entry.row1 >= rowSpec.length) || (entry.row2 >= rowSpec.length)) {
				// Skip the bad component
				continue;
			}
			
			// Get minimum size of current component
			size = entry.component.getMinimumSize();
			
			// Calculate portion of component that is not absolutely sized
			int scalableWidth = size.width;
			int scalableHeight = size.height;
			
			for (counter = entry.col1; counter <= entry.col2; counter++)
				if (columnSpec[counter] >= 1.0)
					scalableWidth -= columnSpec[counter];
			
			for (counter = entry.row1; counter <= entry.row2; counter++)
				if (rowSpec[counter] >= 1.0)
					scalableHeight -= rowSpec[counter];
			
			// ----------------------------------------------------------------------
			
			// Determine total percentage of scalable space that the component
			// occupies by adding the relative columns and the fill columns
			double relativeWidth = 0.0;
			
			for (counter = entry.col1; counter <= entry.col2; counter++) {
				// Column is scaled
				if ((columnSpec[counter] > 0.0) && (columnSpec[counter] < 1.0))
					// Add scaled size to relativeWidth
					relativeWidth += columnSpec[counter];
				// Column is fill
				else
					if ((columnSpec[counter] == FILL) && (fillWidthRatio != 0.0))
						// Add fill size to relativeWidth
						relativeWidth += fillWidthRatio;
			}
			
			// Determine the total scaled width as estimated by this component
			if (relativeWidth == 0)
				temp = 0;
			else
				temp = (int) (scalableWidth / relativeWidth + 0.5);
			
			// If the container needs to be bigger, make it so
			if (scaledWidth < temp)
				scaledWidth = temp;
			
			// ----------------------------------------------------------------------
			
			// Determine total percentage of scalable space that the component
			// occupies by adding the relative columns and the fill columns
			double relativeHeight = 0.0;
			
			for (counter = entry.row1; counter <= entry.row2; counter++) {
				// Row is scaled
				if ((rowSpec[counter] > 0.0) && (rowSpec[counter] < 1.0))
					// Add scaled size to relativeHeight
					relativeHeight += rowSpec[counter];
				// Row is fill
				else
					if ((rowSpec[counter] == FILL) && (fillHeightRatio != 0.0))
						// Add fill size to relativeHeight
						relativeHeight += fillHeightRatio;
			}
			
			// Determine the total scaled width as estimated by this component
			if (relativeHeight == 0)
				temp = 0;
			else
				temp = (int) (scalableHeight / relativeHeight + 0.5);
			
			// If the container needs to be bigger, make it so
			if (scaledHeight < temp)
				scaledHeight = temp;
		}
		
		// totalWidth is the scaledWidth plus the sum of all absolute widths and
		// all
		// preferred widths
		int totalWidth = scaledWidth;
		
		for (counter = 0; counter < columnSpec.length; counter++)
			// Is the current column an absolute size
			if (columnSpec[counter] >= 1.0)
				totalWidth += (int) (columnSpec[counter] + 0.5);
			// Is the current column a preferred size
			else
				if ((columnSpec[counter] == PREFERRED) || (columnSpec[counter] == MINIMUM)) {
					// Assume a maximum width of zero
					int maxWidth = 0;
					
					// Find maximum preferred width of all components completely
					// contained within this column
					iterator = list.listIterator(0);
					
					while (iterator.hasNext()) {
						Entry entry = iterator.next();
						
						if ((entry.col1 == counter) && (entry.col2 == counter)) {
							Dimension p = (columnSpec[counter] == PREFERRED) ? entry.component.getPreferredSize()
									: entry.component.getMinimumSize();
							
							int width = (p == null) ? 0 : p.width;
							
							if (maxWidth < width)
								maxWidth = width;
						}
					}
					
					// Add preferred width
					totalWidth += maxWidth;
				}
		
		// totalHeight is the scaledHeight plus the sum of all absolute heights
		// and
		// all preferred widths
		int totalHeight = scaledHeight;
		
		for (counter = 0; counter < rowSpec.length; counter++)
			// Is the current row an absolute size
			if (rowSpec[counter] >= 1.0)
				totalHeight += (int) (rowSpec[counter] + 0.5);
			// Is the current row a preferred size
			else
				if ((rowSpec[counter] == PREFERRED) || (rowSpec[counter] == MINIMUM)) {
					// Assume a maximum height of zero
					int maxHeight = 0;
					
					// Find maximum preferred height of all components completely
					// contained within this row
					iterator = list.listIterator(0);
					
					while (iterator.hasNext()) {
						Entry entry = iterator.next();
						
						if ((entry.row1 == counter) && (entry.row1 == counter)) {
							Dimension p = (rowSpec[counter] == PREFERRED) ? entry.component.getPreferredSize() : entry.component
									.getMinimumSize();
							
							int height = (p == null) ? 0 : p.height;
							
							if (maxHeight < height)
								maxHeight = height;
						}
					}
					
					// Add preferred height
					totalHeight += maxHeight;
				}
		
		// Compensate for container's insets
		Insets inset = container.getInsets();
		totalWidth += inset.left + inset.right;
		totalHeight += inset.top + inset.bottom;
		
		return new Dimension(totalWidth, totalHeight);
	}
	
	/**
	 * Adds the specified component with the specified name to the layout.
	 * 
	 * @param name
	 *           indicates entry's position and anchor
	 * @param component
	 *           component to add
	 */
	
	@Override
	public void addLayoutComponent(String name, Component component) {
		addLayoutComponent(component, name);
	}
	
	// ******************************************************************************
	// ** java.awt.event.LayoutManager2 methods ***
	// ******************************************************************************
	
	/**
	 * Adds the specified component with the specified name to the layout.
	 * 
	 * @param component
	 *           component to add
	 * @param constraint
	 *           indicates entry's position and alignment
	 */
	
	@Override
	public void addLayoutComponent(Component component, Object constraint) {
		if (constraint instanceof String) {
			// Create an entry to associate component with its constraints
			constraint = new TableLayoutConstraints((String) constraint);
			
			// Add component and constraints to the list
			list.add(new Entry(component, (TableLayoutConstraints) constraint));
		} else
			if (constraint instanceof TableLayoutConstraints) {
				// Add component and constraints to the list
				list.add(new Entry(component, (TableLayoutConstraints) constraint));
			} else
				if (constraint == null)
					throw new IllegalArgumentException("No constraint for the component");
				else
					throw new IllegalArgumentException("Cannot accept a constraint of class " + constraint.getClass());
	}
	
	/**
	 * Removes the specified component from the layout.
	 * 
	 * @param component
	 *           component being removed
	 */
	
	@Override
	public void removeLayoutComponent(Component component) {
		list.remove(component);
	}
	
	/**
	 * Returns the maximum dimensions for this layout given the components in the
	 * specified target container.
	 * 
	 * @param target
	 *           the component which needs to be laid out
	 * @return unconditionally, a Dimension of Integer.MAX_VALUE by
	 *         Integer.MAX_VALUE since TableLayout does not limit the maximum
	 *         size of a container
	 */
	
	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	/**
	 * Returns the alignment along the x axis. This specifies how the component
	 * would like to be aligned relative to other components. The value should be
	 * a number between 0 and 1 where 0 represents alignment along the origin, 1
	 * is aligned the furthest away from the origin, 0.5 is centered, etc.
	 * 
	 * @return unconditionally, 0.5
	 */
	
	@Override
	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}
	
	/**
	 * Returns the alignment along the y axis. This specifies how the component
	 * would like to be aligned relative to other components. The value should be
	 * a number between 0 and 1 where 0 represents alignment along the origin, 1
	 * is aligned the farthest away from the origin, 0.5 is centered, etc.
	 * 
	 * @return unconditionally, 0.5
	 */
	
	@Override
	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}
	
	/**
	 * Invalidates the layout, indicating that if the layout manager has cached
	 * information it should be discarded.
	 */
	
	@Override
	public void invalidateLayout(Container target) {
		dirty = true;
	}
	
	// ******************************************************************************
	// *** Inner Class ***
	// ******************************************************************************
	
	// The following inner class is used to bind components to their constraints
	protected class Entry extends TableLayoutConstraints {
		/** Component bound by the constraints */
		protected Component component;
		
		/** Does the component occupy a single cell */
		protected boolean singleCell;
		
		/**
		 * Constructs an Entry that binds a component to a set of constraints.
		 * 
		 * @param component
		 *           component being bound
		 * @param constranit
		 *           constraints being applied
		 */
		
		public Entry(Component component, TableLayoutConstraints constraint) {
			super(constraint.col1, constraint.row1, constraint.col2, constraint.row2, constraint.hAlign, constraint.vAlign);
			
			singleCell = ((row1 == row2) && (col1 == col2));
			this.component = component;
		}
		
		/**
		 * Determines whether or not two entries are equal.
		 * 
		 * @param object
		 *           object being compared to; must be a Component if it is equal
		 *           to this TableLayoutConstraints.
		 * @return True, if the entries refer to the same component object. False,
		 *         otherwise.
		 */
		
		@Override
		public boolean equals(Object object) {
			boolean equal = false;
			
			if (object instanceof Component) {
				Component component = (Component) object;
				equal = (this.component == component);
			}
			
			return equal;
		}
	}
	
	public static JComponent getSplit(JComponent leftComponent, JComponent rightComponent, double leftSize,
			double rightSize) {
		
		if (leftComponent == null)
			leftComponent = new JLabel("");
		if (rightComponent == null)
			rightComponent = new JLabel("");
		
		JPanel result = new JPanel();
		result.setOpaque(false);
		double border = 0;
		double[][] size = { { border, leftSize, rightSize, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		result.setLayout(new TableLayout(size));
		result.add(leftComponent, "1,1");
		result.add(rightComponent, "2,1");
		result.validate();
		return result;
	}
	
	public static JComponent getSplitSecondVerticalAlignmentMiddle(JComponent leftComponent, JComponent rightComponent, double leftSize,
			double rightSize) {
		
		if (leftComponent == null)
			leftComponent = new JLabel("");
		if (rightComponent == null)
			rightComponent = new JLabel("");
		
		JPanel result = new JPanel();
		result.setOpaque(false);
		double border = 0;
		double[][] size = { { border, leftSize, rightSize, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		result.setLayout(new TableLayout(size));
		result.add(leftComponent, "1,1");
		result.add(
				get3SplitVertical(new JLabel(), rightComponent, new JLabel(), FILL, PREFERRED, FILL), "2,1");
		result.validate();
		return result;
	}
	
	public static JPanel getSplitVertical(Component topComponent, Component bottomComponent, double topSize,
			double bottomSize) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double border = 0;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, topSize, bottomSize, border } }; // Rows
		result.setLayout(new TableLayout(size));
		result.add((topComponent == null ? new JLabel() : topComponent), "1,1");
		result.add((bottomComponent == null ? new JLabel() : bottomComponent), "1,2");
		result.validate();
		return result;
	}
	
	public static JComponent getDoubleRow(JComponent label, JComponent row2, Color color) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double border = 0;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED, border } }; // Rows
		result.setLayout(new TableLayout(size));
		label.setOpaque(true);
		label.setBackground(color);
		result.add(label, "1,1");
		row2.setBorder(BorderFactory.createLineBorder(color));
		result.add(row2, "1,2");
		result.validate();
		return result;
	}
	
	public static JComponent get3Split(JComponent left, JComponent middle, JComponent right, double sizeLeft,
			double sizeMiddle, double sizeRight) {
		return get3Split(left, middle, right, sizeLeft, sizeMiddle, sizeRight, 0, 0);
	}
	
	public static JComponent get3Split(JComponent left, JComponent middle, JComponent right, double sizeLeft,
			double sizeMiddle, double sizeRight, double spaceBetween, double border) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[][] size = { { border, sizeLeft, spaceBetween, sizeMiddle, spaceBetween, sizeRight, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		result.setLayout(new TableLayout(size));
		if (left != null)
			result.add(left, "1,1");
		if (middle != null)
			result.add(middle, "3,1");
		if (right != null)
			result.add(right, "5,1");
		result.validate();
		return result;
	}
	
	public static JComponent get4Split(JComponent left, JComponent middle1, JComponent middle2, JComponent right,
			double width, double spaceBetween, double border) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[][] size = { { border, width, spaceBetween, width, spaceBetween, width, spaceBetween, width, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		result.setLayout(new TableLayout(size));
		if (left != null)
			result.add(left, "1,1");
		if (middle1 != null)
			result.add(middle1, "3,1");
		if (middle2 != null)
			result.add(middle2, "5,1");
		if (right != null)
			result.add(right, "7,1");
		result.validate();
		return result;
	}
	
	public static JComponent get4Split(JComponent left, JComponent middle1, JComponent middle2, JComponent right,
			double w1, double w2, double w3, double w4, double spaceBetween, double border) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[][] size = { { border, w1, spaceBetween, w2, spaceBetween, w3, spaceBetween, w4, border }, // Columns
				{ border, TableLayoutConstants.FILL, border } }; // Rows
		result.setLayout(new TableLayout(size));
		if (left != null)
			result.add(left, "1,1");
		if (middle1 != null)
			result.add(middle1, "3,1");
		if (middle2 != null)
			result.add(middle2, "5,1");
		if (right != null)
			result.add(right, "7,1");
		result.validate();
		return result;
	}
	
	public static JComponent get3SplitVertical(JComponent top, JComponent middle, JComponent bottom, double sizeTop,
			double sizeMiddle, double sizeBottom) {
		return get3SplitVertical(top, middle, bottom, sizeTop, sizeMiddle, sizeBottom, 0, 0);
	}
	
	public static JComponent get3SplitVertical(JComponent top, JComponent middle, JComponent bottom, double sizeTop,
			double sizeMiddle, double sizeBottom, double spaceBetween, double border) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[][] size = { { border, TableLayoutConstants.FILL, border },
				{ border, sizeTop, spaceBetween, sizeMiddle, spaceBetween, sizeBottom, border }, // row
				
		}; // Rows
		result.setLayout(new TableLayout(size));
		if (top != null)
			result.add(top, "1,1");
		if (middle != null)
			result.add(middle, "1,3");
		if (bottom != null)
			result.add(bottom, "1,5");
		result.validate();
		return result;
	}
	
	public static JComponent get4SplitVertical(JComponent top, JComponent middle1, JComponent middle2,
			JComponent bottom, double sizeTop, double sizeMiddle1, double sizeMiddle2, double sizeBottom,
			double spaceBetween, double border) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[][] size = {
				{ border, TableLayoutConstants.FILL, border },
				{ border, sizeTop, spaceBetween, sizeMiddle1, spaceBetween, sizeMiddle2, spaceBetween, sizeBottom, border }, // row
				
		}; // Rows
		result.setLayout(new TableLayout(size));
		if (top != null)
			result.add(top, "1,1");
		if (middle1 != null)
			result.add(middle1, "1,3");
		if (middle2 != null)
			result.add(middle2, "1,5");
		if (bottom != null)
			result.add(bottom, "1,7");
		result.validate();
		return result;
	}
	
	public static LayoutManager getLayout(int width, int height) {
		double[][] size = { { width }, { height }, // row
				
		}; // Rows
		return new TableLayout(size);
	}
	
	public static LayoutManager getLayout(double width, double height) {
		double[][] size = { { width }, { height }, // row
				
		}; // Rows
		return new TableLayout(size);
	}
	
	public static LayoutManager getLayout(double[] width, double height) {
		double[][] size = { width, { height }, // row
				
		}; // Rows
		return new TableLayout(size);
	}
	
	public static LayoutManager getLayout(double width, double[] height) {
		double[][] size = { { width }, height, // row
				
		}; // Rows
		return new TableLayout(size);
	}
	
	public static JComponent getMultiSplit(ArrayList<JComponent> guiElements) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[] width = new double[guiElements.size()];
		for (int i = 0; i < width.length; i++)
			width[i] = TableLayoutConstants.FILL;
		result.setLayout(getLayout(width, TableLayoutConstants.PREFERRED));
		int idx = 0;
		for (JComponent jc : guiElements) {
			if (jc != null)
				result.add(jc, idx + ",0");
			idx++;
		}
		result.validate();
		return result;
	}
	
	public static JComponent getMultiSplit(ArrayList<JComponent> guiElements, double componentsWidth, int spaceT,
			int spaceL, int spaceB, int spaceR) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		double[] width = new double[guiElements.size()];
		for (int i = 0; i < width.length; i++)
			width[i] = componentsWidth;
		result.setLayout(getLayout(width, TableLayoutConstants.PREFERRED));
		int idx = 0;
		for (JComponent jc : guiElements) {
			result.add(FolderPanel.getBorderedComponent(jc, spaceT, spaceL, spaceB, spaceR), idx + ",0");
			idx++;
		}
		result.validate();
		return result;
	}
	
	public static JComponent getMultiSplitVertical(Collection<?> jComponentList) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setBackground(null);
		double[] height = new double[jComponentList.size()];
		for (int i = 0; i < height.length; i++)
			height[i] = TableLayoutConstants.PREFERRED;
		result.setLayout(getLayout(TableLayoutConstants.FILL, height));
		int idx = 0;
		for (Object o : jComponentList) {
			JComponent jc = (JComponent) o;
			result.add(jc, "0," + idx);
			idx++;
		}
		result.validate();
		return result;
	}
	
	public static JPanel getMultiSplitVerticalNonPrefHeight(Collection<?> jComponentList, double h) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setBackground(null);
		setMultiSplitVerticalNonPrefHeight_Contents(jComponentList, h, result);
		return result;
	}
	
	public static void setMultiSplitVerticalNonPrefHeight_Contents(Collection<?> jComponentList, double h, JPanel result) {
		double[] height = new double[jComponentList.size()];
		for (int i = 0; i < height.length; i++)
			height[i] = h;
		result.setLayout(getLayout(TableLayoutConstants.FILL, height));
		int idx = 0;
		for (Object o : jComponentList) {
			JComponent jc = (JComponent) o;
			result.add(jc, "0," + idx);
			idx++;
		}
		result.validate();
	}
	
	public static JComponent getMultiSplitVertical(Collection<?> jComponentList, int spaceBetween) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setBackground(null);
		double[] height = new double[jComponentList.size()];
		for (int i = 0; i < height.length; i++)
			height[i] = TableLayoutConstants.PREFERRED;
		result.setLayout(getLayout(TableLayoutConstants.FILL, height));
		int idx = 0;
		int max = jComponentList.size();
		for (Object o : jComponentList) {
			JComponent jc = (JComponent) o;
			if (idx + 1 < max)
				jc = FolderPanel.getBorderedComponent(jc, 0, 0, spaceBetween, 0);
			result.add(jc, "0," + idx);
			idx++;
		}
		result.validate();
		return result;
	}
	
	public static JComponent getMultiSplitVertical(Collection<?> jComponentList, double width, int spaceBetween) {
		JPanel result = new JPanel();
		result.setOpaque(false);
		result.setBackground(null);
		double[] height = new double[jComponentList.size()];
		for (int i = 0; i < height.length; i++)
			height[i] = TableLayoutConstants.PREFERRED;
		result.setLayout(getLayout(width, height));
		int idx = 0;
		int max = jComponentList.size();
		for (Object o : jComponentList) {
			JComponent jc = (JComponent) o;
			if (idx + 1 < max)
				jc = FolderPanel.getBorderedComponent(jc, 0, 0, spaceBetween, 0);
			result.add(jc, "0," + idx);
			idx++;
		}
		result.validate();
		return result;
	}
	
	public static JComponent getLeftDivider(JComponent gui, double layout, Color color, int width) {
		JLabel divider = new JLabel("");
		divider.setBackground(color);
		divider.setOpaque(true);
		return TableLayout.getSplit(divider, gui, width, layout);
	}
	
	public static LayoutManager getLayout(double width, double height, int columns, int rows) {
		double[] widths = new double[columns];
		for (int x = 0; x < columns; x++) {
			widths[x] = width;
		}
		double[] heights = new double[rows];
		for (int y = 0; y < rows; y++) {
			heights[y] = height;
		}
		return new TableLayout(new double[][] { widths, heights });
	}
}
