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
 * ------------------
 * DatasetReader.java
 * ------------------
 * (C) Copyright 2002, 2003, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: DatasetReader.java,v 1.1 2011-01-31 09:02:54 klukas Exp $
 * Changes
 * -------
 * 20-Nov-2002 : Version 1 (DG);
 */

package org.jfree.data.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jfree.data.CategoryDataset;
import org.jfree.data.PieDataset;
import org.xml.sax.SAXException;

/**
 * A utility class for reading datasets from XML.
 */
public class DatasetReader {

	/**
	 * Reads a {@link PieDataset} from an XML file.
	 * 
	 * @param file
	 *           the file.
	 * @return A dataset.
	 * @throws IOException
	 *            if there is a problem reading the file.
	 */
	public static PieDataset readPieDatasetFromXML(final File file) throws IOException {

		final InputStream in = new FileInputStream(file);
		return readPieDatasetFromXML(in);

	}

	/**
	 * Reads a {@link PieDataset} from a stream.
	 * 
	 * @param in
	 *           the input stream.
	 * @return A dataset.
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	public static PieDataset readPieDatasetFromXML(final InputStream in) throws IOException {

		PieDataset result = null;

		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			final SAXParser parser = factory.newSAXParser();
			final PieDatasetHandler handler = new PieDatasetHandler();
			parser.parse(in, handler);
			result = handler.getDataset();
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e2) {
			System.out.println(e2.getMessage());
		}

		return result;

	}

	/**
	 * Reads a {@link CategoryDataset} from a file.
	 * 
	 * @param file
	 *           the file.
	 * @return A dataset.
	 * @throws IOException
	 *            if there is a problem reading the file.
	 */
	public static CategoryDataset readCategoryDatasetFromXML(final File file) throws IOException {

		final InputStream in = new FileInputStream(file);
		return readCategoryDatasetFromXML(in);

	}

	/**
	 * Reads a {@link CategoryDataset} from a stream.
	 * 
	 * @param in
	 *           the stream.
	 * @return A dataset.
	 * @throws IOException
	 *            if there is a problem reading the file.
	 */
	public static CategoryDataset readCategoryDatasetFromXML(final InputStream in) throws IOException {

		CategoryDataset result = null;

		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			final SAXParser parser = factory.newSAXParser();
			final CategoryDatasetHandler handler = new CategoryDatasetHandler();
			parser.parse(in, handler);
			result = handler.getDataset();
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		} catch (ParserConfigurationException e2) {
			System.out.println(e2.getMessage());
		}
		return result;

	}

}
