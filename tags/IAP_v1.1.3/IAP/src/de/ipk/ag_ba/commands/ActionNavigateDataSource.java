package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class ActionNavigateDataSource extends AbstractNavigationAction {
	private NavigationButton src;
	private final DataSourceLevel dataSourceLevel;
	private ArrayList<NavigationButton> actions;
	private Object errorMsg;
	
	public ActionNavigateDataSource(DataSourceLevel dataSourceLevel) {
		super("Open " + dataSourceLevel.getName());
		this.dataSourceLevel = dataSourceLevel;
	}
	
	@Override
	public String getDefaultTitle() {
		String n = getDataSourceLevel().getName();
		return n;
	}
	
	@Override
	public NavigationImage getImageIconInactive() {
		return getDataSourceLevel().getIconInactive();
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		return getDataSourceLevel().getIconActive();
	}
	
	@Override
	public String getDefaultImage() {
		return null;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		errorMsg = null;
		actions = new ArrayList<NavigationButton>();
		
		try {
			for (NavigationButton nb : getDataSourceLevel().getAdditionalEntities(src)) {
				actions.add(nb);
			}
		} catch (RuntimeException e) {
			errorMsg = e.getMessage();
			return;
		}
		
		for (Book b : getDataSourceLevel().getReferenceInfos()) {
			actions.add(b.getNavigationButton(src));
		}
		for (PathwayWebLinkItem wl : getDataSourceLevel().getPathways()) {
			if (wl.isBookmark()) {
				Book b = new Book(wl.getGroup1(),
						makePretty(wl.getFileName()),
						wl.getURL());
				actions.add(b.getNavigationButton(src, "img/browser.png"));
			}
			if (wl.isPDF()) {
				Book b = new Book(wl.getGroup1(),
						makePretty(wl.getFileName()),
						wl.getURL());
				actions.add(b.getNavigationButton(src, "img/ext/paper.png"));
			}
		}
		for (DataSourceLevel dsl : getDataSourceLevel().getSubLevels()) {
			actions.add(new NavigationButton(new ActionNavigateDataSource(dsl), src.getGUIsetting()));
		}
		for (PathwayWebLinkItem wl : getDataSourceLevel().getPathways()) {
			if (!wl.isBookmark() && !wl.isPDF()) {
				NavigationButton ne = IAPservice.getPathwayViewEntity(wl, src.getGUIsetting());
				actions.add(ne);
			}
		}
		for (ExperimentReference er : getDataSourceLevel().getExperiments()) {
			NavigationButton ne = ActionMongoExperimentsNavigation.getMongoExperimentButton(
					er, src.getGUIsetting());
			actions.add(ne);
		}
		try {
			for (NavigationButton nb : getDataSourceLevel().getAdditionalEntitiesShownAtEndOfList(src)) {
				actions.add(nb);
			}
		} catch (RuntimeException e) {
			errorMsg = e.getMessage();
			return;
		}
	}
	
	private String makePretty(String fileName) {
		String s = StringManipulationTools.stringReplace(fileName, ".webloc", "");
		s = StringManipulationTools.stringReplace(fileName, ".pdf", "");
		for (int i = 0; i < 2; i++) {
			if (!(s.indexOf(";") >= 0))
				break;
			s = s.substring(s.indexOf(";") + ";".length());
		}
		return s;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (errorMsg != null)
			return new MainPanelComponent("<h3>Data source could not be accessed</h3>Error Message: " + errorMsg);
		if (getDataSourceLevel().getDescription() != null)
			return new MainPanelComponent(getDataSourceLevel().getDescription());
		else
			return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return actions;
	}
	
	public DataSourceLevel getDataSourceLevel() {
		return dataSourceLevel;
	}
}
