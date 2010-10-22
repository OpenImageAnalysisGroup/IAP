package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.HashMap;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.nav.RimasNav;
import de.ipk.ag_ba.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk.ag_ba.gui.util.EmptyNavigationAction;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.gui.webstart.AIPgui;
import de.ipk.ag_ba.gui.webstart.Bookmark;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public final class HomeAction extends AbstractNavigationAction {
	private final ArrayList<NavigationGraphicalEntity> homePrimaryActions;
	private final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus;

	public HomeAction(BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {
		super("IAP Home");
		homePrimaryActions = new ArrayList<NavigationGraphicalEntity>();
		this.myStatus = myStatus;

		initializeHomeActions();
	}

	private void initializeHomeActions() {
		ArrayList<NavigationGraphicalEntity> homeActions = new ArrayList<NavigationGraphicalEntity>();
		// homePrimaryActions.add(new NavigationGraphicalEntity(new Phenotyping(),
		// "Phenotyping", "img/000Grad_3.png"));
		for (NavigationGraphicalEntity ne : new Phenotyping().getResultNewActionSet()) {
			homePrimaryActions.add(ne);
		}

		// homeActions.add(new NavigationGraphicalEntity(new DBElogin2(),
		// "DBE Database", "img/dbelogo2.png"));

		NavigationGraphicalEntity rimas = RimasNav.getRimas();
		homeActions.add(rimas);

		NavigationGraphicalEntity metaCrop = WebFolder
				.getBrowserNavigationEntity(
						null,
						"MetaCrop",
						"img/metacrop.png",
						"http://pgrc-16.ipk-gatersleben.de/wgrp/nwg/metacrop/",
						"Website",
						"img/browser.png",
						"http://metacrop.ipk-gatersleben.de",
						new String[] { ".gml", ".graphml" },
						""
								+ "<h2>MetaCrop</h2>"
								+ "MetaCrop is a web accessible database that summarizes diverse information about metabolic pathways "
								+ "in crop plants and allows automatic export of information for the creation of detailed metabolic models.<br><br>"
								+ "IAP as well as VANTED provide access to the exported MetaCrop pathways in a graphical and interactive way.<br>"
								+ "For background information and further information please visit the MetaCrop website, accessible by using the "
								+ "Website button, shown above.", null);
		homeActions.add(metaCrop);

		HashMap<String, String> folder2url = new HashMap<String, String>();
		folder2url.put("", "SBGN Specification:http://www.nature.com/nbt/journal/v27/n8/full/nbt.1558.html");
		folder2url.put("", "Reference:http://bioinformatics.oxfordjournals.org/content/26/18/2340.short");
		folder2url.put("Activity Flow", "Nat. Proc. (AF):http://precedings.nature.com/documents/3724/version/1");
		folder2url.put("Entity Relationship", "Nat. Proc. (ER):http://precedings.nature.com/documents/3724/version/1");
		folder2url.put("Process Description", "Nat. Proc. (PD):http://precedings.nature.com/documents/3724/version/1");
		NavigationGraphicalEntity sbgn = WebFolder
				.getBrowserNavigationEntity(
						folder2url,
						"SBGN-ED",
						"img/sbgn.png",
						"http://vanted.ipk-gatersleben.de/aip/sbgn-examples/",
						"SBGN-ED",
						"img/browser.png",
						"http://vanted.ipk-gatersleben.de/addons/sbgn-ed/",
						new String[] { ".gml", ".graphml" },
						"<h2>SBGN-ED - Editing, Translating and Validating of SBGN Maps</h2>"
								+ ""
								+ "SBGN-ED is a VANTED Add-on which allows to create and edit all three types of SBGN maps, "
								+ "that is Process Description, Entity Relationship and Activity Flow, to validate these "
								+ "maps according to the SBGN specifications, to translate maps from the KEGG and MetaCrop "
								+ "pathway databases into SBGN, and to export SBGN maps into several file and image formats.<br><br>"
								+ "SBGN-ED editing, translation and validation functions are available from within VANTED and IAP as "
								+ "soon as the SBGN-ED Add-on available from the mentioned website is downloaded and installed. "
								+ "The SBGN-ED website additionally contains documentation and additional background information.",
						null);
		homeActions.add(sbgn);

		HashMap<String, String> folder2urlVANTED = new HashMap<String, String>();
		folder2urlVANTED
				.put(
						"",
						"Source Code:http://vanted.ipk-gatersleben.de/#ui-tabs-9^Add-ons:http://vanted.ipk-gatersleben.de/#ui-tabs-11^Lit. References:http://vanted.ipk-gatersleben.de/#ui-tabs-13");
		NavigationGraphicalEntity vanted = WebFolder
				.getBrowserNavigationEntity(
						folder2urlVANTED,
						"VANTED",
						// "img/vanted_examples.png",
						"img/vanted1_0.png",
						"http://vanted.ipk-gatersleben.de/examplefiles/",
						"Website",
						"img/browser.png",
						"http://vanted.ipk-gatersleben.de/",
						new String[] { ".gml", ".graphml" },
						"<h2>Welcome to VANTED - Visualization and Analysis of Networks containing Experimental Data</h2>"
								+ "This system makes it possible to load and edit graphs, which may represent biological pathways or functional hierarchies. "
								+ "It is possible to map experimental datasets onto the graph elements and visualize time series data or data of different "
								+ "genotypes or environmental conditions in the context of a the underlying biological processes. Built-in statistic "
								+ "functions allow a fast evaluation of the data (e.g. t-Test or correlation analysis).",
						"Examples");

		NavigationGraphicalEntity startVanted = new NavigationGraphicalEntity(new ShowVANTED());

		vanted.getAction().addAdditionalEntity(startVanted);

		homeActions.add(vanted);

		NavigationGraphicalEntity serverStatusEntity = Other.getServerStatusEntity(true);
		homePrimaryActions.add(serverStatusEntity);

		EmptyNavigationAction ipkBioInf = new EmptyNavigationAction("Bioinformatics@IPK",
				"General Bioinformatics Ressources", "img/pattern_graffiti_logo.png", "img/pattern_graffiti_logo.png");
		ipkBioInf.addAdditionalEntity(WebFolder.getURLentity("Website", "http://bioinformatics.ipk-gatersleben.de",
				"img/browser.png"));
		for (NavigationGraphicalEntity nge : homeActions)
			ipkBioInf.addAdditionalEntity(nge);
		homePrimaryActions.add(new NavigationGraphicalEntity(ipkBioInf));
	}

	ArrayList<NavigationGraphicalEntity> bookmarks;

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
		bookmarks = new ArrayList<NavigationGraphicalEntity>();
		try {
			for (Bookmark b : Bookmark.getBookmarks()) {
				BookmarkAction ba = new BookmarkAction(b);
				NavigationGraphicalEntity nge = new NavigationGraphicalEntity(ba, ba.getImage());
				bookmarks.add(nge);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}

	@Override
	public String getDefaultImage() {
		// return "img/pattern_graffiti_logo.png";
		return "img/iap.png";
	}

	@Override
	public String getDefaultTitle() {
		return "IAP";
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> homeNavigation = new ArrayList<NavigationGraphicalEntity>();
		homeNavigation.add(new NavigationGraphicalEntity(this));

		for (NavigationGraphicalEntity n : bookmarks)
			homeNavigation.add(n);

		return homeNavigation;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return homePrimaryActions;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(AIPgui.getIntroTxt());
	}

	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return myStatus;
	}

	public ArrayList<NavigationGraphicalEntity> getActionEntitySet() {
		return homePrimaryActions;
	}
}