/*******************************************************************************
 * Copyright (c) 2011 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.datasources.DataSourceGroup;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author klukas
 */
public class IAPnewsLinksSource extends HTTPfolderSource {
	
	public IAPnewsLinksSource(DataSourceGroup dsg) {
		super(dsg, getLib(),
				"News and Links", "http://iap.ipk-gatersleben.de/links/", new String[] { ".webloc" },
				IAPmain.loadIcon("img/public.png"), IAPmain.loadIcon("img/dataset.png"), IAPmain.loadIcon("img/dataset.png"));
		setDescription("<h2>Bookmarks</h2>"
				+ "At this place interesting links regarding interesting bioinformatics " +
				"research activities are listed.");
	}
	
	private static Library getLib() {
		Library libIAPnews = new Library();
		// libMetaCrop.add(new Book("MetaCrop", "Website", "http://metacrop.ipk-gatersleben.de", "img/browser.png"));
		
		return libIAPnews;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) {
		// EmptyNavigationAction ipkBioInf = new EmptyNavigationAction(
		// "Sino/German Network",
		// "Sino/German Network of Computational & Integrative Biology",
		// "img/CIB_logo.png", "img/CIB_logo.png");
		// ipkBioInf.addAdditionalEntity(WebFolder.getURLactionButtton(
		// "Website",
		// new IOurl("http://www.imbio.de/forschung2/"),
		// "img/browser.png", src != null ? src.getGUIsetting() : null));
		//
		// ipkBioInf.addAdditionalEntity(WebFolder.getURLactionButtton(
		// "MyBioNet",
		// new IOurl("http://bis.zju.edu.cn/mybionet/"),
		// "img/ext/MyBioNet48.png", src != null ? src.getGUIsetting() : null));
		
		Collection<NavigationButton> nbl = new ArrayList<NavigationButton>();
		// nbl.add(new NavigationButton(ipkBioInf, src.getGUIsetting()));
		nbl.add(WebFolder.getURLactionButtton(
				"MyBioNet",
				new IOurl("http://bis.zju.edu.cn/mybionet/"),
				"img/ext/MyBioNet48.png", src != null ? src.getGUIsetting() : null));
		return nbl;
	}
}
