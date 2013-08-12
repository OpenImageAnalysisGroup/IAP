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
 * --------------------------
 * CategorySeriesHandler.java
 * --------------------------
 * (C) Copyright 2003, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: CategorySeriesHandler.java,v 1.1 2011-01-31 09:02:55 klukas Exp $
 * Changes
 * -------
 * 23-Jan-2003 : Version 1 (DG);
 */

package org.jfree.data.xml;

import java.util.Iterator;

import org.jfree.data.DefaultKeyedValues;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A handler for reading a series for a category dataset.
 */
public class CategorySeriesHandler extends DefaultHandler implements DatasetTags {

	/** The root handler. */
	private RootHandler root;

	/** The series name. */
	private String seriesName;

	/** The values. */
	private DefaultKeyedValues values;

	/**
	 * Creates a new item handler.
	 * 
	 * @param root
	 *           the root handler.
	 */
	public CategorySeriesHandler(final RootHandler root) {
		this.root = root;
		this.values = new DefaultKeyedValues();
	}

	/**
	 * Sets the series name.
	 * 
	 * @param name
	 *           the name.
	 */
	public void setSeriesName(final String name) {
		this.seriesName = name;
	}

	/**
	 * Adds an item to the temporary storage for the series.
	 * 
	 * @param key
	 *           the key.
	 * @param value
	 *           the value.
	 */
	public void addItem(final Comparable key, final Number value) {
		this.values.addValue(key, value);
	}

	/**
	 * The start of an element.
	 * 
	 * @param namespaceURI
	 *           the namespace.
	 * @param localName
	 *           the element name.
	 * @param qName
	 *           the element name.
	 * @param atts
	 *           the attributes.
	 * @throws SAXException
	 *            for errors.
	 */
	public void startElement(final String namespaceURI,
										final String localName,
										final String qName,
										final Attributes atts) throws SAXException {

		if (qName.equals(SERIES_TAG)) {
			setSeriesName(atts.getValue("name"));
			final ItemHandler subhandler = new ItemHandler(this.root, this);
			this.root.pushSubHandler(subhandler);
		} else
			if (qName.equals(ITEM_TAG)) {
				final ItemHandler subhandler = new ItemHandler(this.root, this);
				this.root.pushSubHandler(subhandler);
				subhandler.startElement(namespaceURI, localName, qName, atts);
			}

			else {
				throw new SAXException("Expecting <Series> or <Item> tag...found " + qName);
			}
	}

	/**
	 * The end of an element.
	 * 
	 * @param namespaceURI
	 *           the namespace.
	 * @param localName
	 *           the element name.
	 * @param qName
	 *           the element name.
	 */
	public void endElement(final String namespaceURI,
									final String localName,
									final String qName) {

		if (this.root instanceof CategoryDatasetHandler) {
			final CategoryDatasetHandler handler = (CategoryDatasetHandler) this.root;

			final Iterator iterator = this.values.getKeys().iterator();
			while (iterator.hasNext()) {
				final Comparable key = (Comparable) iterator.next();
				final Number value = this.values.getValue(key);
				handler.addItem(this.seriesName, key, value);
			}

			this.root.popSubHandler();
		}

	}

}
