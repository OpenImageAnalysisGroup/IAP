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
public class RimasDataSource extends HTTPfolderSource {
	
	public RimasDataSource(DataSourceGroup dsg) {
		super(dsg, getLib(),
				"RIMAS", "http://rimas.ipk-gatersleben.de/Pathways/",
				new String[] { ".gml" },
				IAPmain.loadIcon("img/rimas.png"),
				IAPmain.loadIcon(IAPimages.getFolderRemoteClosed()),
				IAPmain.loadIcon(IAPimages.getFolderRemoteOpen()));
		setDescription("<h2>RIMAS - Regulatory Interaction Maps of Arabidopsis Seed Development</h2>"
				+ "RIMAS contains detailed SBGN conforming network diagrams which reflect the interactions "
				+ "of transcription factor hierarchies, gene promoter elements, hormonal pathways, epigenetic "
				+ "processes and chromatin remodelling and provides an easy access to the relevant references.<br><br>"
				+ "IAP provides interactive access to the RIMAS pathways. RIMAS itself is a web-based information "
				+ "portal with additional detailed descriptions and background information about the investigated "
				+ "biological phenomena (please click the Website button above for access to detailed pathway descriptions).");
	}
	
	private static Library getLib() {
		Library rimasDoku = new Library();
		rimasDoku.add(new Book("RIMAS", "Website", new IOurl("http://rimas.ipk-gatersleben.de"), "img/browser.png"));
		rimasDoku.add(new Book("RIMAS", "Lit. Reference",
				new IOurl("http://www.cell.com/trends/plant-science/abstract/S1360-1385(10)00061-0")));
		return rimasDoku;
	}
	
}
