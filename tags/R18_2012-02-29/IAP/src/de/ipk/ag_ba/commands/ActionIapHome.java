package de.ipk.ag_ba.commands;

import java.io.File;
import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.commands.hsm.ActionHsmDataSourceNavigation;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.datasources.http_folder.HTTPfolderSource;
import de.ipk.ag_ba.datasources.http_folder.IAPnewsLinksSource;
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
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public final class ActionIapHome extends AbstractNavigationAction {
	private ArrayList<NavigationButton> homePrimaryActions;
	private final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus;
	
	public ActionIapHome(BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus) {
		super("IAP Home");
		this.myStatus = myStatus;
		
	}
	
	private void initializeHomeActions(GUIsetting guiSetting) {
		ArrayList<NavigationButton> homeActions = new ArrayList<NavigationButton>();
		
		homePrimaryActions = new ArrayList<NavigationButton>();
		for (NavigationButton ne : new ActionAccessDataProcessing(guiSetting).getResultNewActionSet()) {
			homePrimaryActions.add(ne);
		}
		
		HTTPfolderSource news = new IAPnewsLinksSource();
		NavigationButton newsButton = new NavigationButton(new DataSourceNavigationAction(news), guiSetting);
		homeActions.add(newsButton);
		
		NavigationButton rimas = RimasNav.getRimas(src != null ? src.getGUIsetting() : null);
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
		
		NavigationButton serverStatusEntity = Other.getServerStatusEntity(false, src != null ? src.getGUIsetting() : null);
		homePrimaryActions.add(serverStatusEntity);

		String hsm = IAPmain.getHSMfolder();
		if (hsm != null && new File(hsm).exists()) {
			// add HSM entry
			Library lib = new Library();
			DataSource dataSourceHsm = new HsmFileSystemSource(lib, "HSM Archive", hsm,
					IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
					IAPmain.loadIcon("img/ext/folder-remote.png"));
			NavigationButton hsmSrc = new NavigationButton(new ActionHsmDataSourceNavigation(dataSourceHsm), guiSetting);
			hsmSrc.setToolTipText("Target: " + hsm);
			homePrimaryActions.add(hsmSrc);
		}
		
		{
			EmptyNavigationAction ipkBioInf = new EmptyNavigationAction("Bioinformatics@IPK",
								"General Bioinformatics Ressources", "img/dbelogo2.png", "img/dbelogo2.png");
			ipkBioInf.setIntroductionText(
						"<h2>Bioinformatics@IPK</h2>IAP additionally provides access and links to various bioinformatics ressources, "
								+ "developed at the IPK. The included data sources and tools have been "
								+ "mainly developed by members of the group Plant Bioinformatics and Image Analysis, "
								+ "partly with contributions from the group Bioinformatics and Information Technology. "
								+ "To get details about the included data sources and information systems, click the included Website- and Reference-Links."
						);
			ipkBioInf.addAdditionalEntity(WebFolder.getURLentity("Website", "http://bioinformatics.ipk-gatersleben.de",
								"img/browser.png", src != null ? src.getGUIsetting() : null));
			for (NavigationButton nge : homeActions)
				ipkBioInf.addAdditionalEntity(nge);
			homePrimaryActions.add(new NavigationButton(ipkBioInf, guiSetting));
		}
		
		homePrimaryActions.add(new NavigationButton(new ActionShowVANTED(), guiSetting));
	}
	
	ArrayList<NavigationButton> bookmarks;
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		initializeHomeActions(src != null ? src.getGUIsetting() : null);
		bookmarks = new ArrayList<NavigationButton>();
		if (src != null) {
			try {
				for (Bookmark b : Bookmark.getBookmarks()) {
					BookmarkAction ba = new BookmarkAction(b);
					NavigationButton nge = new NavigationButton(ba, ba.getImage(), src.getGUIsetting(), ba.getStaticIconId());
					bookmarks.add(nge);
				}
			} catch (Throwable e) {
				// ErrorMsg.addErrorMessage(e);
				System.out.println("Can't access bookmarks: " + e.getMessage());
			}
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
		homeNavigation.add(new NavigationButton(this, src != null ? src.getGUIsetting() : null));
		
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
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return myStatus;
	}
	
	public ArrayList<NavigationButton> getActionEntitySet() {
		return homePrimaryActions;
	}
}