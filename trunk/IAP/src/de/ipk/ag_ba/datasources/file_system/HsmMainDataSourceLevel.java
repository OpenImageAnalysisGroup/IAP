package de.ipk.ag_ba.datasources.file_system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.actions.Book;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class HsmMainDataSourceLevel implements DataSourceLevel {
	
	final HashMap<String, TreeMap<Long, ExperimentHeaderInterface>> experimentName2saveTime2data;
	private String description;
	private int n = 0;
	
	// private HsmFileSystemSource hsmFileSystemSource;
	
	public HsmMainDataSourceLevel(HashMap<String, TreeMap<Long, ExperimentHeaderInterface>> experimentName2saveTime2data) {
		this.experimentName2saveTime2data = experimentName2saveTime2data;
		for (TreeMap<Long, ExperimentHeaderInterface> tmm : experimentName2saveTime2data.values())
			n += 1; // tmm.values().size(); // only one item is counted (the remaining are older exp. versions)
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		TreeMap<String, DataSourceLevel> type2level = new TreeMap<String, DataSourceLevel>();
		for (String expName : experimentName2saveTime2data.keySet()) {
			TreeMap<Long, ExperimentHeaderInterface> time2exp = experimentName2saveTime2data.get(expName);
			ExperimentHeaderInterface newestExp = time2exp.lastEntry().getValue();
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
		if (n > 0)
			return "HSM Archive (" + n + ")";
		else
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
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		return result;
	}
	
	public void setHsmFileSystemSource(HsmFileSystemSource hsmFileSystemSource) {
		// this.hsmFileSystemSource = hsmFileSystemSource;
	}
	
}
