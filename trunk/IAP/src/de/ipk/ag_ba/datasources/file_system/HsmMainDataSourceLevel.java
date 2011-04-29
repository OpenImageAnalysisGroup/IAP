package de.ipk.ag_ba.datasources.file_system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_actions.Book;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class HsmMainDataSourceLevel implements DataSourceLevel {
	
	private final HashMap<String, TreeMap<Long, ExperimentHeader>> experimentName2saveTime2data;
	private String description;
	
	public HsmMainDataSourceLevel(HashMap<String, TreeMap<Long, ExperimentHeader>> experimentName2saveTime2data) {
		this.experimentName2saveTime2data = experimentName2saveTime2data;
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		TreeMap<String, DataSourceLevel> type2level = new TreeMap<String, DataSourceLevel>();
		for (String expName : experimentName2saveTime2data.keySet()) {
			TreeMap<Long, ExperimentHeader> time2exp = experimentName2saveTime2data.get(expName);
			ExperimentHeader newestExp = time2exp.lastEntry().getValue();
			String type = newestExp.getExperimentType();
			if (type == null || type.length() == 0)
				type = "[Unknown Experimenttype]";
			if (!type2level.containsKey(type))
				type2level.put(type, new HsmDataSourceLevelForExperimentType(type));
			((HsmDataSourceLevelForExperimentType) type2level.get(type)).addExperiment(newestExp);
		}
		return type2level.values();
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
		return IAPmain.loadIcon("img/ext/Gnome-Media-Tape-64.png");
	}
	
	@Override
	public String getName() {
		return "HSM Archive";
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
	
}
