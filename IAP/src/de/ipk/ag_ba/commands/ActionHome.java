package de.ipk.ag_ba.commands;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ReleaseInfo;
import org.SystemOptions;

import de.ipk.ag_ba.commands.about.ActionAbout;
import de.ipk.ag_ba.commands.bookmarks.BookmarkAction;
import de.ipk.ag_ba.commands.exit.ActionExit;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.DataSourceGroup;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.EmptyNavigationAction;
import de.ipk.ag_ba.gui.webstart.Bookmark;
import de.ipk.ag_ba.gui.webstart.IAPgui;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public final class ActionHome extends AbstractNavigationAction {
	private ArrayList<NavigationButton> homePrimaryActions = new ArrayList<NavigationButton>();
	private final BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus;
	
	public ActionHome(BackgroundTaskStatusProviderSupportingExternalCallImpl myStatus, GUIsetting guiSetting) {
		super("<html>IAP Home<br><font color='gray'>V" + ReleaseInfo.IAP_VERSION_STRING);
		this.myStatus = myStatus;
		this.guiSetting = guiSetting;
		assert guiSetting != null;
	}
	
	private void initializeHomeActions(GUIsetting guiSetting) {
		homePrimaryActions = new ArrayList<NavigationButton>();
		
		for (NavigationAction na : IAPpluginManager.getInstance().getHomeActions()) {
			homePrimaryActions.add(new NavigationButton(na, guiSetting));
		}
		
		addDataSourceAndDataSourceGroups(guiSetting);
		
		boolean vfs = SystemOptions.getInstance().getBoolean("VFS", "enabled", true);
		if (vfs) {
			// add VFS entries
			for (VirtualFileSystem vfsEntry : VirtualFileSystem.getKnown(true, false)) {
				homePrimaryActions.add(vfsEntry.getNavigationButton(guiSetting));
			}
		}
		
		boolean vanted = SystemOptions.getInstance().getBoolean("VANTED", "show_icon", true);
		if (vanted)
			homePrimaryActions.add(new NavigationButton(new ActionShowVANTED(), guiSetting));
		
		NavigationButton serverStatusEntity = Other.getServerStatusEntity(guiSetting);
		homePrimaryActions.add(serverStatusEntity);
		
		boolean showSetting = true;// SystemOptions.getInstance().getBoolean("IAP", "show_settings_icon", true);
		if (showSetting) {
			homePrimaryActions.add(new NavigationButton(new ActionSettings(null, null, "Configure the system", "Settings"), guiSetting));
		}
		
		homePrimaryActions.add(new NavigationButton(new ActionAbout("Version and Copyright information"), guiSetting));
		if (guiSetting == null || guiSetting.getInstance() == 1)
			homePrimaryActions.add(new NavigationButton(new ActionExit("Close the application", true), guiSetting));
		else
			homePrimaryActions.add(new NavigationButton(new ActionExit("Close this window", false), guiSetting));
	}
	
	private void addDataSourceAndDataSourceGroups(GUIsetting guiSetting) {
		LinkedHashSet<DataSourceGroup> dsgList = new LinkedHashSet<DataSourceGroup>();
		for (DataSource ds : IAPpluginManager.getInstance().getHomeDataSources()) {
			if (ds.getDataSourceGroup() == null)
				homePrimaryActions.add(new NavigationButton(new ActionNavigateDataSource(ds), src != null ? src.getGUIsetting() : null));
			else
				dsgList.add(ds.getDataSourceGroup());
		}
		for (final DataSourceGroup dsg : dsgList) {
			EmptyNavigationAction ipkBioInf = new EmptyNavigationAction(dsg.getTitle(), dsg.getTooltip(),
					dsg.getImage(), dsg.getNavigationImage()) {
				@Override
				public void performActionCalculateResults(NavigationButton src) throws Exception {
					IAPmain.prepareVantedPlugins();
					super.performActionCalculateResults(src);
				}
				
				@Override
				public MainPanelComponent getResultMainPanel() {
					if (dsg.getIntroductionText() != null)
						return new MainPanelComponent(dsg.getIntroductionText());
					else
						return null;
				}
				
			};
			for (NavigationAction na : dsg.getAdditionalActions())
				ipkBioInf.addAdditionalEntity(new NavigationButton(na, guiSetting));
			for (DataSource ds : IAPpluginManager.getInstance().getHomeDataSources()) {
				if (ds.getDataSourceGroup() == null || dsg != ds.getDataSourceGroup())
					continue;
				ipkBioInf.addAdditionalEntity(new NavigationButton(new ActionNavigateDataSource(ds), src != null ? src.getGUIsetting() : null));
			}
			homePrimaryActions.add(new NavigationButton(ipkBioInf, guiSetting));
		}
	}
	
	ArrayList<NavigationButton> bookmarks;
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		bookmarks = new ArrayList<NavigationButton>();
		if (src != null) {
			try {
				for (Bookmark b : Bookmark.getBookmarks()) {
					BookmarkAction ba = new BookmarkAction(b, this);
					NavigationButton nge = new NavigationButton(ba, ba.getImage(), src.getGUIsetting(), ba.getStaticIconId());
					bookmarks.add(nge);
				}
			} catch (Throwable e) {
				// ErrorMsg.addErrorMessage(e);
				System.out.println("Can't access bookmarks: " + e.getMessage());
			}
		}
		initializeHomeActions(src != null ? src.getGUIsetting() : null);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/iap.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Start";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> homeNavigation = new ArrayList<NavigationButton>();
		homeNavigation.add(new NavigationButton(this, src != null ? src.getGUIsetting() : guiSetting));
		
		if (bookmarks != null)
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
}