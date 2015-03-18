package de.ipk.ag_ba.commands.lt;

import java.util.ArrayList;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk.ag_ba.postgresql.MetaDataType;

public class ActionMetaDataInfo extends AbstractNavigationAction {
	
	private String db;
	private String ml;
	
	private final ArrayList<String> metaInfo = new ArrayList<String>();
	
	public ActionMetaDataInfo(String tooltip) {
		super(tooltip);
	}
	
	public ActionMetaDataInfo(String db, String ml) {
		this("Show overview of meta-data for experiment " + ml + ", stored in DB " + db);
		this.db = db;
		this.ml = ml;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		metaInfo.clear();
		StringBuilder res = new StringBuilder();
		res.append("<b>Meta-Data for Experiment " + ml + ", stored in " + db + ":</b><br><br>");
		res.append("<table border='1'>");
		res.append("<tr><th>ID</th><th>Type</th><th>Values (Number of Plant-IDs)</th></tr>");
		LTdataExchange lt = new LTdataExchange();
		for (MetaDataType metaInfoId : lt.getMetaDataIdsForMeasureLabel(db, ml)) {
			ArrayList<String> ids = lt.getMetaDataValues(db, ml, metaInfoId.getMeta_data_name(), true);
			res.append("<tr><th>" + metaInfoId.getMeta_data_name() + " (" + ids.size() + ")</th><td>" + metaInfoId.getMeta_data_type() + "</td><td>"
					+ StringManipulationTools.getStringList("", ids, ", ", 10, "<br>")
					+ "</td></tr>");
		}
		res.append("</table>");
		metaInfo.add(res.toString());
		
		res = new StringBuilder();
		// res.append("<table border='1'>");
		// res.append("<tr><th colspan='1'><h3>Meta-Data Plant-IDs for Experiment " + ml + ", stored in database " + db + "</h3></th>");
		// res.append("<tr><td>"
		// + StringManipulationTools.getStringList(lt.getMetaDataPlantIDs(db, ml), ", ")
		// + "</td></tr>");
		// res.append("</table>");
		ArrayList<String> ids = lt.getMetaDataPlantIDs(db, ml);
		res.append("<b>Plant-IDs (" + ids.size() + "):</b><br><br>" + StringManipulationTools.getStringList("", ids, ", ", 10, "<br>"));
		metaInfo.add(res.toString());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(metaInfo);
	}
	
	@Override
	public String getDefaultTitle() {
		return ml;
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getAdressBookClearFront();
	}
}
