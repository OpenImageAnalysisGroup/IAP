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
 * ----------
 * Title.java
 * ----------
 * (C) Copyright 2000-2004, by David Berry and Contributors.
 * Original Author: David Berry;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Nicolas Brodu;
 * $Id: Title.java,v 1.1 2011-01-31 09:03:14 klukas Exp $
 * Changes (from 21-Aug-2001)
 * --------------------------
 * 21-Aug-2001 : Added standard header (DG);
 * 18-Sep-2001 : Updated header (DG);
 * 14-Nov-2001 : Package com.jrefinery.common.ui.* changed to com.jrefinery.ui.* (DG);
 * 07-Feb-2002 : Changed blank space around title from Insets --> Spacer, to allow for relative
 * or absolute spacing (DG);
 * 25-Jun-2002 : Removed unnecessary imports (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 14-Oct-2002 : Changed the event listener storage structure (DG);
 * 11-Sep-2003 : Took care of listeners while cloning (NB);
 * 22-Sep-2003 : Spacer cannot be null. Added nullpointer checks for this (TM);
 * 08-Jan-2003 : Renamed AbstractTitle --> Title and moved to separate package (DG);
 */

package org.jfree.chart.title;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.event.EventListenerList;

import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Spacer;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.ObjectUtils;

/**
 * The base class for all chart titles. A chart can have multiple titles, appearing at the top,
 * bottom, left or right of the chart.
 * <P>
 * Concrete implementations of this class will render text and images, and hence do the actual work of drawing titles.
 * 
 * @author David Berry
 */
public abstract class Title extends Object implements Cloneable, Serializable {

	/** Useful constant for the title position (also used for vertical alignment). */
	public static final int TOP = 0;

	/** Useful constant for the title position (also used for vertical alignment). */
	public static final int BOTTOM = 1;

	/** Useful constant for the title position (also used for horizontal alignment). */
	public static final int RIGHT = 2;

	/** Useful constant for the title position (also used for horizontal alignment). */
	public static final int LEFT = 3;

	/** Useful constant for the title position. */
	public static final int NORTH = 0;

	/** Useful constant for the title position. */
	public static final int SOUTH = 1;

	/** Useful constant for the title position. */
	public static final int EAST = 2;

	/** Useful constant for the title position. */
	public static final int WEST = 3;

	/** Useful constant for the title alignment (horizontal or vertical). */
	public static final int CENTER = 4;

	/** Useful constant for the title alignment (horizontal or vertical). */
	public static final int MIDDLE = 4;

	/** The default title position. */
	public static final RectangleEdge DEFAULT_POSITION = RectangleEdge.TOP;

	/** The default horizontal alignment. */
	public static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER;

	/** The default vertical alignment. */
	public static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER;

	/** Default title spacer. */
	public static final Spacer DEFAULT_SPACER = new Spacer(Spacer.RELATIVE, 0.01, 0.15, 0.01, 0.15);

	/** The title position. */
	private RectangleEdge position;

	/** The horizontal alignment of the title. */
	private HorizontalAlignment horizontalAlignment;

	/** The vertical alignment of the title. */
	private VerticalAlignment verticalAlignment;

	/** The amount of blank space to leave around the title. */
	private Spacer spacer;

	/** Storage for registered change listeners. */
	private transient EventListenerList listenerList;

	/** A flag that can be used to temporarily disable the listener mechanism. */
	private boolean notify;

	/**
	 * Creates a new title, using default attributes where necessary.
	 */
	protected Title() {

		this(Title.DEFAULT_POSITION,
							Title.DEFAULT_HORIZONTAL_ALIGNMENT,
							Title.DEFAULT_VERTICAL_ALIGNMENT,
							Title.DEFAULT_SPACER);

	}

	/**
	 * Creates a new title, using default attributes where necessary.
	 * 
	 * @param position
	 *           the position of the title (<code>null</code> not permitted).
	 * @param horizontalAlignment
	 *           the horizontal alignment of the title
	 *           (<code>null</code> not permitted).
	 * @param verticalAlignment
	 *           the vertical alignment of the title
	 *           (<code>null</code> not permitted).
	 */
	protected Title(RectangleEdge position,
							HorizontalAlignment horizontalAlignment,
							VerticalAlignment verticalAlignment) {

		this(position,
							horizontalAlignment, verticalAlignment,
							Title.DEFAULT_SPACER);

	}

	/**
	 * Creates a new title.
	 * <P>
	 * This class defines constants for the valid position and alignment values --- an IllegalArgumentException will be thrown if invalid values are passed to
	 * this constructor.
	 * 
	 * @param position
	 *           the position of the title (<code>null</code> not permitted).
	 * @param horizontalAlignment
	 *           the horizontal alignment of the title (LEFT, CENTER or RIGHT, <code>null</code> not permitted).
	 * @param verticalAlignment
	 *           the vertical alignment of the title (TOP, MIDDLE or BOTTOM, <code>null</code> not permitted).
	 * @param spacer
	 *           the amount of space to leave around the outside of the title
	 *           (<code>null</code> not permitted).
	 */
	protected Title(RectangleEdge position,
							HorizontalAlignment horizontalAlignment,
							VerticalAlignment verticalAlignment,
							Spacer spacer) {

		// check arguments...
		if (position == null) {
			throw new IllegalArgumentException("Argument 'position' cannot be null.");
		}
		if (horizontalAlignment == null) {
			throw new IllegalArgumentException("Argument 'horizontalAlignment' cannot be null.");
		}

		if (verticalAlignment == null) {
			throw new IllegalArgumentException("Argument 'verticalAlignment' cannot be null.");
		}
		if (spacer == null) {
			throw new IllegalArgumentException("Argument 'spacer' cannot be null.");
		}

		// initialise...
		this.position = position;
		this.horizontalAlignment = horizontalAlignment;
		this.verticalAlignment = verticalAlignment;
		this.spacer = spacer;
		this.listenerList = new EventListenerList();
		this.notify = true;

	}

	/**
	 * Returns the position of the title.
	 * 
	 * @return the title position (never <code>null</code>).
	 */
	public RectangleEdge getPosition() {
		return this.position;
	}

	/**
	 * Sets the position for the title and sends a {@link TitleChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param position
	 *           the position (<code>null</code> not permitted).
	 */
	public void setPosition(RectangleEdge position) {
		if (position == null) {
			throw new IllegalArgumentException("Null 'position' argument.");
		}
		if (this.position != position) {
			this.position = position;
			notifyListeners(new TitleChangeEvent(this));
		}
	}

	/**
	 * Returns the horizontal alignment of the title.
	 * 
	 * @return the horizontal alignment (never <code>null</code>).
	 */
	public HorizontalAlignment getHorizontalAlignment() {
		return this.horizontalAlignment;
	}

	/**
	 * Sets the horizontal alignment for the title and sends a {@link TitleChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param alignment
	 *           the horizontal alignment (<code>null</code> not permitted).
	 */
	public void setHorizontalAlignment(HorizontalAlignment alignment) {
		if (alignment == null) {
			throw new IllegalArgumentException("Null 'alignment' argument.");
		}
		if (this.horizontalAlignment != alignment) {
			this.horizontalAlignment = alignment;
			notifyListeners(new TitleChangeEvent(this));
		}
	}

	/**
	 * Returns the vertical alignment of the title.
	 * 
	 * @return the vertical alignment (never <code>null</code>).
	 */
	public VerticalAlignment getVerticalAlignment() {
		return this.verticalAlignment;
	}

	/**
	 * Sets the vertical alignment for the title, and notifies any registered
	 * listeners of the change.
	 * 
	 * @param alignment
	 *           the new vertical alignment (TOP, MIDDLE or BOTTOM, <code>null</code> not permitted).
	 */
	public void setVerticalAlignment(VerticalAlignment alignment) {
		if (alignment == null) {
			throw new IllegalArgumentException("Argument 'alignment' cannot be null.");
		}
		if (this.verticalAlignment != alignment) {
			this.verticalAlignment = alignment;
			notifyListeners(new TitleChangeEvent(this));
		}
	}

	/**
	 * Returns the spacer which determines the blank space around the edges of the title.
	 * 
	 * @return The spacer (never <code>null</code>).
	 */
	public Spacer getSpacer() {
		return this.spacer;
	}

	/**
	 * Sets the spacer for the title and sends a {@link TitleChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param spacer
	 *           the new spacer (<code>null</code> not permitted).
	 */
	public void setSpacer(Spacer spacer) {
		if (spacer == null) {
			throw new NullPointerException("AbstractTitle.setSpacer(): null not permitted.");
		}
		if (!this.spacer.equals(spacer)) {
			this.spacer = spacer;
			notifyListeners(new TitleChangeEvent(this));
		}

	}

	/**
	 * Returns the flag that indicates whether or not the notification mechanism is enabled.
	 * 
	 * @return the flag.
	 */
	public boolean getNotify() {
		return this.notify;
	}

	/**
	 * Sets the flag that indicates whether or not the notification mechanism
	 * is enabled. There are certain situations (such as cloning) where you
	 * want to turn notification off temporarily.
	 * 
	 * @param flag
	 *           the new value of the flag.
	 */
	public void setNotify(boolean flag) {
		this.notify = flag;
	}

	/**
	 * Returns the preferred width of the title. When a title is displayed at
	 * the left or right of a chart, the chart will attempt to give the title
	 * enough space for it's preferred width.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param height
	 *           the height.
	 * @return the preferred width of the title.
	 */
	public abstract float getPreferredWidth(Graphics2D g2, float height);

	/**
	 * Returns the preferred height of the title. When a title is displayed at
	 * the top or bottom of a chart, the chart will attempt to give the title
	 * enough space for it's preferred height.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param width
	 *           the width.
	 * @return the preferred height of the title.
	 */
	public abstract float getPreferredHeight(Graphics2D g2, float width);

	/**
	 * Draws the title on a Java 2D graphics device (such as the screen or a printer).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area allocated for the title.
	 */
	public abstract void draw(Graphics2D g2, Rectangle2D area);

	/**
	 * Returns a clone of the title.
	 * <P>
	 * One situation when this is useful is when editing the title properties - you can edit a clone, and then it is easier to cancel the changes if necessary.
	 * 
	 * @return a clone of the title.
	 * @throws CloneNotSupportedException
	 *            not thrown by this class, but it may be thrown by
	 *            subclasses.
	 */
	public Object clone() throws CloneNotSupportedException {

		Title duplicate = (Title) super.clone();
		duplicate.listenerList = new EventListenerList();
		// Spacer is immutable => same reference in clone OK
		return duplicate;
	}

	/**
	 * Registers an object for notification of changes to the title.
	 * 
	 * @param listener
	 *           the object that is being registered.
	 */
	public void addChangeListener(TitleChangeListener listener) {
		this.listenerList.add(TitleChangeListener.class, listener);
	}

	/**
	 * Unregisters an object for notification of changes to the chart title.
	 * 
	 * @param listener
	 *           the object that is being unregistered.
	 */
	public void removeChangeListener(TitleChangeListener listener) {
		this.listenerList.remove(TitleChangeListener.class, listener);
	}

	/**
	 * Notifies all registered listeners that the chart title has changed in some way.
	 * 
	 * @param event
	 *           an object that contains information about the change to the title.
	 */
	protected void notifyListeners(TitleChangeEvent event) {

		if (this.notify) {

			Object[] listeners = this.listenerList.getListenerList();
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == TitleChangeListener.class) {
					((TitleChangeListener) listeners[i + 1]).titleChanged(event);
				}
			}
		}

	}

	/**
	 * Tests an object for equality with this title.
	 * 
	 * @param obj
	 *           the object.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof Title) {

			Title t = (Title) obj;

			if (this.position != t.position) {
				return false;
			}
			if (this.horizontalAlignment != t.horizontalAlignment) {
				return false;
			}
			if (this.verticalAlignment != t.verticalAlignment) {
				return false;
			}
			if (!ObjectUtils.equal(this.spacer, t.spacer)) {
				return false;
			}
			if (this.notify != t.notify) {
				return false;
			}

			return true;

		}

		return false;

	}

	/**
	 * Returns a hashcode for the title.
	 * 
	 * @return the hashcode.
	 */
	public int hashCode() {
		int result = 193;
		result = 37 * result + ObjectUtils.hashCode(this.position);
		result = 37 * result + ObjectUtils.hashCode(this.horizontalAlignment);
		result = 37 * result + ObjectUtils.hashCode(this.verticalAlignment);
		result = 37 * result + ObjectUtils.hashCode(this.spacer);
		return result;
	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the output stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the input stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 * @throws ClassNotFoundException
	 *            if there is a classpath problem.
	 */
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		this.listenerList = new EventListenerList();
	}

}
