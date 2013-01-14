/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Dec 12, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.datasources.http_folder;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

/**
 * @author klukas
 */
public class VANTEDdataSource extends HTTPfolderSource {
	private static final String VANTED = "VANTED Resources";
	
	public VANTEDdataSource() {
		super(getLibrary(),
				VANTED,
				"http://vanted.ipk-gatersleben.de/examplefiles/",
				new String[] { ".gml", ".graphml" },
				IAPmain.loadIcon("img/vanted1_0_doku.png"), // vanted1_0.png"),
				IAPmain.loadIcon(IAPimages.getFolderRemoteClosed()));
		setDescription("<h2>Welcome to VANTED - Visualization and Analysis of Networks containing Experimental Data</h2>"
				+ "This system makes it possible to load and edit graphs, which may represent biological pathways or functional hierarchies. "
				+ "It is possible to map experimental datasets onto the graph elements and visualize time series data or data of different "
				+ "genotypes or environmental conditions in the context of a the underlying biological processes. Built-in statistic "
				+ "functions allow a fast evaluation of the data (e.g. t-Test or correlation analysis).");
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		final Collection<DataSourceLevel> subLevels = super.getSubLevels();
		final Collection<PathwayWebLinkItem> subPathways = super.getPathways();
		final Collection<ExperimentReference> subExp = super.getExperiments();
		
		Collection<DataSourceLevel> res = new ArrayList<DataSourceLevel>();
		res.add(new DataSourceLevel() {
			
			@Override
			public void setDescription(String description) {
				// empty
			}
			
			@Override
			public Collection<DataSourceLevel> getSubLevels() {
				return subLevels;
			}
			
			@Override
			public ArrayList<Book> getReferenceInfos() {
				return new ArrayList<Book>();
			}
			
			@Override
			public Collection<PathwayWebLinkItem> getPathways() {
				return subPathways;
			}
			
			@Override
			public String getName() {
				return "Examples";
			}
			
			@Override
			public NavigationImage getIconInactive() {
				return IAPmain.loadIcon(IAPimages.getFolderRemoteClosed());
			}
			
			@Override
			public NavigationImage getIconActive() {
				return IAPmain.loadIcon(IAPimages.getFolderRemoteOpen());
			}
			
			@Override
			public Collection<ExperimentReference> getExperiments() {
				return subExp;
			}
			
			@Override
			public String getDescription() {
				return null;
			}
			
			@Override
			public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) {
				return new ArrayList<NavigationButton>();
			}
		});
		return res;
	}
	
	private static Library getLibrary() {
		Library liblVANTED = new Library();
		liblVANTED.add(new Book(VANTED, "Website", new IOurl("http://vanted.ipk-gatersleben.de/", "img/browser.png")));
		liblVANTED.add(new Book(VANTED, "Source Code", new IOurl("http://vanted.ipk-gatersleben.de/#ui-tabs-9")));
		liblVANTED.add(new Book(VANTED, "Add-ons", new IOurl("http://vanted.ipk-gatersleben.de/#ui-tabs-11")));
		liblVANTED.add(new Book(VANTED, "Lit. References", new IOurl("http://vanted.ipk-gatersleben.de/#ui-tabs-13")));
		return liblVANTED;
	}
	
	@Override
	public Collection<PathwayWebLinkItem> getPathways() {
		return new ArrayList<PathwayWebLinkItem>();
	}
}
