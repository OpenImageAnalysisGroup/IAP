/*******************************************************************************
 * Copyright (c) 2011 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import de.ipk.ag_ba.gui.actions.Book;
import de.ipk.ag_ba.gui.actions.Library;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class LemnaTecDokuSource extends HTTPfolderSource {
	
	public LemnaTecDokuSource() {
		super(getLib(),
							"Documentation", "http://ba-13.ipk-gatersleben.de/LemnaTec/", new String[] { ".pdf" },
							IAPmain.loadIcon("img/dataset.png"), IAPmain.loadIcon("img/ext/folder.png"));
		setDescription("<h2>LemnaTec Technical Documentation</h2>"
							+ "This function provides access to the dokumentation delivered with the LemnaTec systems.");
	}
	
	@Override
	public void readDataSource() throws Exception {
		super.readDataSource();
	}
	
	private static Library getLib() {
		Library libLemnaTecDocu = new Library();
		libLemnaTecDocu.add(new Book("Documentation", "IPK Naming Standards", "http://ba-13.ipk-gatersleben.de/standards.pdf", "img/ext/paper.png"));
		return libLemnaTecDocu;
	}
	
}
