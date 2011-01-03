package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class DataSourceNavigationAction extends AbstractNavigationAction {
	
	private NavigationButton src;
	private final DataSourceLevel dataSourceLevel;
	private ArrayList<NavigationButton> actions;
	
	public DataSourceNavigationAction(DataSourceLevel dataSourceLevel) {
		super("Open " + dataSourceLevel.getName());
		this.dataSourceLevel = dataSourceLevel;
	}
	
	@Override
	public String getDefaultTitle() {
		return dataSourceLevel.getName();
	}
	
	@Override
	public NavigationImage getImageIcon() {
		return dataSourceLevel.getIcon();
	}
	
	@Override
	public String getDefaultImage() {
		return null;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		actions = new ArrayList<NavigationButton>();
		for (Book b : dataSourceLevel.getReferenceInfos()) {
			actions.add(b.getNavigationButton(src));
		}
		for (DataSourceLevel dsl : dataSourceLevel.getSubLevels()) {
			actions.add(new NavigationButton(new DataSourceNavigationAction(dsl), src.getGUIsetting()));
		}
		for (PathwayWebLinkItem wl : dataSourceLevel.getPathways()) {
			NavigationButton ne = IAPservice.getPathwayViewEntity(wl, src.getGUIsetting());
			actions.add(ne);
		}
		for (ExperimentReference er : dataSourceLevel.getExperiments()) {
			NavigationButton ne = MongoExperimentsNavigationAction.getMongoExperimentButton(er.getHeader(), src.getGUIsetting(), null);
			actions.add(ne);
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (dataSourceLevel.getDescription() != null)
			return new MainPanelComponent(dataSourceLevel.getDescription());
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
	
}
