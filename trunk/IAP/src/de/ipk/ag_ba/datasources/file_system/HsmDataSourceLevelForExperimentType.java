package de.ipk.ag_ba.datasources.file_system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.actions.Book;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class HsmDataSourceLevelForExperimentType implements DataSourceLevel {
	
	private final String type;
	private String description;
	private final TreeMap<String, ArrayList<ExperimentHeader>> user2experimentList;
	private int n;
	
	public HsmDataSourceLevelForExperimentType(String type) {
		this.type = type;
		this.user2experimentList = new TreeMap<String, ArrayList<ExperimentHeader>>();
		n = 0;
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		Collection<DataSourceLevel> res = new ArrayList<DataSourceLevel>();
		for (String type : user2experimentList.keySet()) {
			res.add(new HsmDataSourceLevelForUser(type, user2experimentList.get(type)));
		}
		return res;
	}
	
	@Override
	public Collection<ExperimentReference> getExperiments() {
		return new ArrayList<ExperimentReference>();
	}
	
	@Override
	public Collection<PathwayWebLinkItem> getPathways() {
		return new ArrayList<PathwayWebLinkItem>();
	}
	
	@Override
	public NavigationImage getIcon() {
		String group = type;
		if (group.toUpperCase().startsWith("APH_") || group.toUpperCase().contains("(APH)") || group.equals(IAPexperimentTypes.Phytochamber))
			return IAPmain.loadIcon(IAPimages.getPhytochamber());
		else
			if (group.toUpperCase().startsWith("BGH_") || group.toUpperCase().contains("(BGH)") || group.equals(IAPexperimentTypes.BarleyGreenhouse))
				return IAPmain.loadIcon(IAPimages.getBarleyGreenhouse());
			else
				if (group.toUpperCase().startsWith("CGH_") || group.toUpperCase().contains("(CGH)") || group.equals(IAPexperimentTypes.MaizeGreenhouse))
					return IAPmain.loadIcon(IAPimages.getMaizeGreenhouse());
				else
					return IAPmain.loadIcon(IAPimages.getFolderRemoteClosed());
	}
	
	@Override
	public String getName() {
		return type + " (" + n + ")";
	}
	
	@Override
	public ArrayList<Book> getReferenceInfos() {
		return new ArrayList<Book>();
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) {
		return new ArrayList<NavigationButton>();
	}
	
	public void addExperiment(ExperimentHeader newestExp) {
		String user = newestExp.getCoordinator();
		if (user == null || user.isEmpty())
			user = "[Unknown Coordinator, Import by " + newestExp.getImportusername() + "]";
		if (!user2experimentList.containsKey(user))
			user2experimentList.put(user, new ArrayList<ExperimentHeader>());
		user2experimentList.get(user).add(newestExp);
		
		n++;
	}
	
}
