/*******************************************************************************
 * Copyright (c) 2011 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import de.ipk.ag_ba.gui.navigation_actions.Library;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class VantedNewsLinksSource extends HTTPfolderSource {
	
	public VantedNewsLinksSource() {
		super(getLib(),
							"News and Links", "http://vanted.ipk-gatersleben.de/links/", new String[] { ".webloc" },
							IAPmain.loadIcon("img/public.png"), IAPmain.loadIcon("img/dataset.png"));
		setDescription("<h2>Bookmarks</h2>"
							+ "At this place interesting links regarding interesting bioinformatics " +
								"research activities are listed.");
	}
	
	private static Library getLib() {
		Library libMetaCrop = new Library();
		// libMetaCrop.add(new Book("MetaCrop", "Website", "http://metacrop.ipk-gatersleben.de", "img/browser.png"));
		return libMetaCrop;
	}
	
}
