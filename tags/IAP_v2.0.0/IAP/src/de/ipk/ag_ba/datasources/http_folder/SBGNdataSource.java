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
public class SBGNdataSource extends HTTPfolderSource {
	
	public SBGNdataSource(DataSourceGroup dsg) {
		super(dsg, getLibrary(), "SBGN-ED", "http://vanted.ipk-gatersleben.de/aip/sbgn-examples/",
				new String[] { ".gml", ".graphml" }, IAPmain.loadIcon("img/sbgn.png"),
				IAPmain.loadIcon(IAPimages.getFolderRemoteClosed()),
				IAPmain.loadIcon(IAPimages.getFolderRemoteOpen()));
		setDescription("<h2>SBGN-ED - Editing, Translating and Validating of SBGN Maps</h2>"
				+ ""
				+ "SBGN-ED is a VANTED Add-on which allows to create and edit all three types of SBGN maps, "
				+ "that is Process Description, Entity Relationship and Activity Flow, to validate these "
				+ "maps according to the SBGN specifications, to translate maps from the KEGG and MetaCrop "
				+ "pathway databases into SBGN, and to export SBGN maps into several file and image formats.<br><br>"
				+ "SBGN-ED editing, translation and validation functions are available from within VANTED and IAP as "
				+ "soon as the SBGN-ED Add-on available from the mentioned website is downloaded and installed. "
				+ "The SBGN-ED website additionally contains documentation and additional background information.");
	}
	
	private static Library getLibrary() {
		Library lib = new Library();
		lib.add(new Book("SBGN-ED", "SBGN-ED", new IOurl("http://vanted.ipk-gatersleben.de/addons/sbgn-ed/"), "img/browser.png"));
		lib.add(new Book("SBGN-ED", "SBGN Specification", new IOurl("http://www.nature.com/nbt/journal/v27/n8/full/nbt.1558.html"), "img/dataset.png"));
		lib.add(new Book("SBGN-ED", "Reference", new IOurl("http://bioinformatics.oxfordjournals.org/content/26/18/2340.short"), "img/dataset.png"));
		lib.add(new Book("Activity Flow", "Nat. Proc. (AF)", new IOurl("http://precedings.nature.com/documents/3724")));
		lib.add(new Book("Entity Relationship", "Nat. Proc. (ER)", new IOurl("http://precedings.nature.com/documents/3719"), "img/dataset.png"));
		lib.add(new Book("Process Description", "Nat. Proc. (PD)", new IOurl("http://precedings.nature.com/documents/3721"), "img/dataset.png"));
		return lib;
	}
}
