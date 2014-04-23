package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import org.FolderPanel;
import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.NewsHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.RSSFeedManager;

public class ActionShowRSS extends AbstractNavigationAction {
	
	private ArrayList<NavigationButton> resActions;
	
	public ActionShowRSS(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		status.setCurrentStatusText1("Retrieve RSS news feed contents");
		resActions = new ArrayList<NavigationButton>();
		final RSSFeedManager rfm = RSSFeedManager.getInstance();
		rfm.loadRegisteredFeeds();
		rfm.setWordWrap(120);
		
		new NewsHelper(null).refreshNews(rfm, null, null);
		for (FolderPanel fp : rfm.getNewsComponents()) {
			checkIfFolderPanel(src, fp);
		}
		
		status.setCurrentStatusText1("News content received");
	}
	
	private void checkIfFolderPanel(NavigationButton src, final FolderPanel fp) {
		
		NavigationAction na = new AbstractNavigationAction("Show " + StringManipulationTools.removeHTMLtags(fp.getTitle()) + " news feed") {
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				// empty
			}
			
			@Override
			public String getDefaultTitle() {
				return StringManipulationTools.removeHTMLtags(fp.getTitle());
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
			
			@Override
			public boolean isProvidingActions() {
				return false;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				fp.setCondensedState(false);
				fp.setShowCondenseButton(false);
				fp.setMaximumRowCount(3, true);
				fp.layoutRows();
				return new MainPanelComponent(fp.getBorderedComponent(15, 30, 0, 30));
			}
			
			@Override
			public String getDefaultImage() {
				return "img/dataset.png";
			}
		};
		resActions.add(new NavigationButton(na, src.getGUIsetting()));
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return resActions;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return null;
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getBookIcon();
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>News<br>" +
				"<font color='gray'><small>(RSS feeds)</small></font></center>";
	}
}
