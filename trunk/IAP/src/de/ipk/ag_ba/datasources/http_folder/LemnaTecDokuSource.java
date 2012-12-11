/*******************************************************************************
 * Copyright (c) 2011 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.Book;
import de.ipk.ag_ba.commands.Library;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class LemnaTecDokuSource extends HTTPfolderSource {
	
	public LemnaTecDokuSource() {
		super(getLib(),
				IAPoptions.getInstance().getString("LemnaTec-Site-Documentation", "title",
						"Documentation"),
				IAPoptions.getInstance().getString("LemnaTec-Site-Documentation", "url",
						"http://ba-13.ipk-gatersleben.de/LemnaTec/"),
				IAPoptions.getInstance().getStringAll("LemnaTec-Site-Documentation", "filename_mask",
						new String[] { ".pdf" }),
				IAPmain.loadIcon("img/dataset.png"), IAPmain.loadIcon("img/ext/folder.png"));
		setDescription(IAPoptions.getInstance().getString("LemnaTec-Site-Documentation", "description",
				"<h2>LemnaTec Technical Documentation</h2>"
						+ "This function provides access to the dokumentation delivered with the LemnaTec systems."));
	}
	
	@Override
	public void readDataSource() throws Exception {
		super.readDataSource();
	}
	
	private static Library getLib() {
		Library libLemnaTecDocu = new Library();
		if (IAPoptions.getInstance().getBoolean("LemnaTec-Site-Documentation",
				"user_doc_show_icon", true)) {
			libLemnaTecDocu.add(new Book(
					IAPoptions.getInstance().getString("LemnaTec-Site-Documentation",
							"title", "Documentation"), // warning: needs to be the same title as above
					IAPoptions.getInstance().getString("LemnaTec-Site-Documentation",
							"user_doc_title", "IPK Naming Standards"),
					new IOurl(IAPoptions.getInstance().getString("LemnaTec-Site-Documentation",
							"user_doc_url", "http://ba-13.ipk-gatersleben.de/standards.pdf")),
					"img/ext/paper.png"));
		}
		return libLemnaTecDocu;
	}
	
}
