package de.ipk.ag_ba.datasources.ftp_file_system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class HsmFtpMainDataSourceLevel implements DataSourceLevel {
	
	private final HashMap<String, TreeMap<Long, ExperimentHeader>> experimentName2saveTime2data;
	private String description;
	private int n = 0;
	
	public HsmFtpMainDataSourceLevel(HashMap<String, TreeMap<Long, ExperimentHeader>> experimentName2saveTime2data) {
		this.experimentName2saveTime2data = experimentName2saveTime2data;
		for (TreeMap<Long, ExperimentHeader> tmm : experimentName2saveTime2data.values())
			n += 1; // tmm.values().size(); // only one item is counted (the remaining are older exp. versions)
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
				type2level.put(type, new HsmFtpDataSourceLevelForExperimentType(type));
			((HsmFtpDataSourceLevelForExperimentType) type2level.get(type)).addExperiment(newestExp);
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
	public NavigationImage getIconInactive() {
		return IAPmain.loadIcon("img/ext/Gnome-Media-Tape-64.png");
	}
	
	@Override
	public NavigationImage getIconActive() {
		return IAPmain.loadIcon("img/ext/Gnome-Media-Tape-64.png");
	}
	
	@Override
	public String getName() {
		if (n > 0)
			return IAPoptions.getInstance().getString("ARCHIVE", "title", "HSM Archive") + " (" + n + ")";
		else
			return IAPoptions.getInstance().getString("ARCHIVE", "title", "HSM Archive");
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
	
	@Override
	public Collection<NavigationButton> getAdditionalEntitiesShownAtEndOfList(NavigationButton src) {
		return new ArrayList<NavigationButton>();
	}
	
}
