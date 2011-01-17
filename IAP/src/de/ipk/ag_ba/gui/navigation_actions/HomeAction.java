package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.datasources.http_folder.HTTPfolderSource;
import de.ipk.ag_ba.datasources.http_folder.MetaCropDataSource;
import de.ipk.ag_ba.datasources.http_folder.SBGNdataSource;
import de.ipk.ag_ba.datasources.http_folder.VANTEDdataSource;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.nav.RimasNav;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.EmptyNavigationAction;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.gui.webstart.Bookmark;
import de.ipk.ag_ba.gui.webstart.IAPgui;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public final class HomeAction extends AbstractNavigationAction {
	private ArrayList<NavigationButton> homePrimaryActions;
	private final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus;
	
	public HomeAction(BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {
		super("IAP Home");
		this.myStatus = myStatus;
		
	}
	
	private void initializeHomeActions(GUIsetting guiSetting) {
		ArrayList<NavigationButton> homeActions = new ArrayList<NavigationButton>();
		
		homePrimaryActions = new ArrayList<NavigationButton>();
		for (NavigationButton ne : new Phenotyping(guiSetting).getResultNewActionSet()) {
			homePrimaryActions.add(ne);
		}
		
		NavigationButton rimas = RimasNav.getRimas(src.getGUIsetting());
		homeActions.add(rimas);
		
		HTTPfolderSource dataSource = new MetaCropDataSource();
		NavigationButton metaCrop = new NavigationButton(new DataSourceNavigationAction(dataSource), guiSetting);
		homeActions.add(metaCrop);
		
		HTTPfolderSource sbgn = new SBGNdataSource();
		NavigationButton sbgned = new NavigationButton(new DataSourceNavigationAction(sbgn), guiSetting);
		homeActions.add(sbgned);
		
		HTTPfolderSource van = new VANTEDdataSource();
		NavigationButton vantedNB = new NavigationButton(new DataSourceNavigationAction(van), guiSetting);
		// NavigationButton startVanted0 = new NavigationButton(new ShowVANTED(), guiSetting);
		// vantedNB.getAction().addAdditionalEntity(startVanted0);
		homeActions.add(vantedNB);
		
		NavigationButton serverStatusEntity = Other.getServerStatusEntity(true, src.getGUIsetting());
		homePrimaryActions.add(serverStatusEntity);
		
		{
			EmptyNavigationAction ipkBioInf = new EmptyNavigationAction("Bioinformatics@IPK",
								"General Bioinformatics Ressources", "img/pattern_graffiti_logo.png", "img/pattern_graffiti_logo.png");
			ipkBioInf.addAdditionalEntity(WebFolder.getURLentity("Website", "http://bioinformatics.ipk-gatersleben.de",
								"img/browser.png", src.getGUIsetting()));
			for (NavigationButton nge : homeActions)
				ipkBioInf.addAdditionalEntity(nge);
			homePrimaryActions.add(new NavigationButton(ipkBioInf, guiSetting));
		}
		{
			EmptyNavigationAction ipkBioInf = new EmptyNavigationAction("Sino/German Network",
								"Sino/German Network of Computational & Integrative Biology", "img/CIB_logo.png", "img/CIB_logo.png");
			ipkBioInf.addAdditionalEntity(WebFolder.getURLentity("Website", "http://www.imbio.de/forschung2/",
								"img/browser.png", src.getGUIsetting()));
			// for (NavigationButton nge : homeActions)
			// ipkBioInf.addAdditionalEntity(nge);
			homePrimaryActions.add(new NavigationButton(ipkBioInf, guiSetting));
		}
		
		homePrimaryActions.add(new NavigationButton(new ShowVANTED(), guiSetting));
	}
	
	ArrayList<NavigationButton> bookmarks;
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		initializeHomeActions(src.getGUIsetting());
		bookmarks = new ArrayList<NavigationButton>();
		try {
			for (Bookmark b : Bookmark.getBookmarks()) {
				BookmarkAction ba = new BookmarkAction(b);
				NavigationButton nge = new NavigationButton(ba, ba.getImage(), src.getGUIsetting());
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
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> homeNavigation = new ArrayList<NavigationButton>();
		homeNavigation.add(new NavigationButton(this, src.getGUIsetting()));
		
		for (NavigationButton n : bookmarks)
			homeNavigation.add(n);
		
		return homeNavigation;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return homePrimaryActions;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(IAPgui.getIntroTxt());
	}
	
	@Override
	public BackgroundTaskStatusProvider getStatusProvider() {
		return myStatus;
	}
	
	public ArrayList<NavigationButton> getActionEntitySet() {
		return homePrimaryActions;
	}
}