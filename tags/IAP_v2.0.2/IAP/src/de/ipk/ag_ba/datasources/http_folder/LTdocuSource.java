/*******************************************************************************
 * Copyright (c) 2011 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class LTdocuSource extends HTTPfolderSource {
	
	public LTdocuSource() {
		super(null, getLib(),
				SystemOptions.getInstance().getString("Imaging-System-Documentation", "title",
						"Documentation"),
				SystemOptions.getInstance().getString("Imaging-System-Documentation", "url",
						"http://ba-13.ipk-gatersleben.de/LemnaTec/"),
				SystemOptions.getInstance().getStringAll("Imaging-System-Documentation", "filename_mask",
						new String[] { ".pdf" }),
				IAPmain.loadIcon("img/dataset.png"),
				IAPmain.loadIcon(IAPimages.getFolderRemoteClosed()),
				IAPmain.loadIcon(IAPimages.getFolderRemoteOpen()));
		setDescription(SystemOptions.getInstance().getString("Imaging-System-Documentation", "description",
				"<h2>Technical Documentation</h2>"
						+ "This function provides access to the dokumentation delivered with the automated imaging systems."));
	}
	
	@Override
	public void readDataSource() throws Exception {
		super.readDataSource();
	}
	
	private static Library getLib() {
		Library libImagingSystemDocu = new Library();
		if (SystemOptions.getInstance().getBoolean("Imaging-System-Documentation",
				"user_doc_show_icon", true)) {
			libImagingSystemDocu.add(new Book(
					SystemOptions.getInstance().getString("Imaging-System-Documentation",
							"title", "Documentation"), // warning: needs to be the same title as above
					SystemOptions.getInstance().getString("Imaging-System-Documentation",
							"user_doc_title", "Naming Standards"),
					new IOurl(SystemOptions.getInstance().getString("Imaging-System-Documentation",
							"user_doc_url", "http://ba-13.ipk-gatersleben.de/standards.pdf")),
					"img/ext/paper.png"));
		}
		return libImagingSystemDocu;
	}
	
}
