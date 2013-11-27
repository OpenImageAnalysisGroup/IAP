package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;
import java.util.TreeMap;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.postgresql.LTdataExchange;

public class ActionMetaData extends AbstractNavigationAction {
	
	private final ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
	
	public ActionMetaData(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		res.clear();
		LTdataExchange lt = new LTdataExchange();
		status.setCurrentStatusText1("Enumerate MetaData Information...");
		final TreeMap<String, ArrayList<NavigationButton>> db2el = new TreeMap<String, ArrayList<NavigationButton>>();
		for (String db : lt.getDatabases()) {
			try {
				for (String ml : lt.getMetaDataMeasurementLabels(db)) {
					if (!db2el.containsKey(db))
						db2el.put(db, new ArrayList<NavigationButton>());
					db2el.get(db).add(new NavigationButton(new ActionMetaDataInfo(db, ml), src.getGUIsetting()));
				}
			} catch (Exception e) {
				// empty
			}
		}
		for (final String db : db2el.keySet()) {
			NavigationAction listAction = new AbstractNavigationAction("Show Experiments with Meta-Data for DB " + db) {
				@Override
				public void performActionCalculateResults(NavigationButton src) throws Exception {
					//
				}
				
				@Override
				public ArrayList<NavigationButton> getResultNewActionSet() {
					return db2el.get(db);
				}
				
				@Override
				public String getDefaultTitle() {
					return db + " (" + db2el.get(db).size() + ")";
				}
				
				@Override
				public String getDefaultImage() {
					return IAPimages.getArchive();
				}
				
			};
			res.add(new NavigationButton(listAction, src.getGUIsetting()));
		}
		status.setCurrentStatusText1("Finished Processing");
		status.setCurrentStatusText2("");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "View Meta-Data";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getBookIcon();
	}
}
