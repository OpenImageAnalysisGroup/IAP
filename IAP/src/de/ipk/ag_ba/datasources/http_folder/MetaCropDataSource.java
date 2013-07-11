/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.datasources.DataSourceGroup;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class MetaCropDataSource extends HTTPfolderSource {
	
	public MetaCropDataSource(DataSourceGroup dsg) {
		super(dsg, getLib(),
				"MetaCrop", "http://vanted.ipk-gatersleben.de/addons/metacrop/gml/", new String[] { ".gml" },
				IAPmain.loadIcon("img/metacrop.png"), IAPmain.loadIcon(IAPimages.getFolderRemoteClosed()));
		setDescription("<h2>MetaCrop</h2>"
				+ "MetaCrop is a web accessible database that summarizes diverse information about metabolic pathways "
				+ "in crop plants and allows automatic export of information for the creation of detailed metabolic models.<br><br>"
				+ "IAP as well as VANTED provide access to the exported MetaCrop pathways in a graphical and interactive way.<br>"
				+ "For background information and further information please visit the MetaCrop website, accessible by using the "
				+ "Website button, shown above.");
	}
	
	private static Library getLib() {
		Library libMetaCrop = new Library();
		libMetaCrop.add(new Book("MetaCrop", "Website", new IOurl("http://metacrop.ipk-gatersleben.de"), "img/browser.png"));
		return libMetaCrop;
	}
	
}
