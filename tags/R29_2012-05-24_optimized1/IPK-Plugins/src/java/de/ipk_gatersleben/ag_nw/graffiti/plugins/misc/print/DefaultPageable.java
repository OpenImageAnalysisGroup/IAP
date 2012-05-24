/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $ID:$
 * Created on 12.08.2003
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.print;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

/**
 * Default pageable implementation
 * 
 * @author <a href="mailto:sell@nesoft.de">Burkhard Sell</a>
 * @version $Revision: 1.1 $
 */
public class DefaultPageable implements Pageable {
	private Printable printable;
	private PageFormat format;
	private int pages;
	
	public DefaultPageable(Printable printable, PageFormat format, int pages) {
		this.printable = printable;
		this.format = format;
		this.pages = pages;
	}
	
	public int getNumberOfPages() {
		return pages;
	}
	
	public void setNumberOfPages(int pages) {
		this.pages = pages;
	}
	
	public Printable getPrintable(int index) {
		if (index >= pages)
			throw new IndexOutOfBoundsException();
		return printable;
	}
	
	public PageFormat getPageFormat(int index) {
		if (index >= pages)
			throw new IndexOutOfBoundsException();
		return format;
	}
}
