package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import javax.swing.ImageIcon;

import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public class DataSourceNavigationAction extends AbstractNavigationAction {

	private DataSource dataSource;

	public DataSourceNavigationAction(String tooltip) {
		super(tooltip);
	}

	public DataSourceNavigationAction(DataSource dataSource) {
		this("Open " + dataSource.getMainLevelInfo().getName());
		this.dataSource = dataSource;
	}

	@Override
	public String getDefaultTitle() {
		return dataSource.getMainLevelInfo().getName();
	}

	@Override
	public ImageIcon getImageIcon() {
		return new ImageIcon(dataSource.getMainLevelInfo().getIconForThisLevel());
	}

	@Override
	public String getDefaultImage() {
		return null;
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		// TODO Auto-generated method stub
		return null;
	}

}
