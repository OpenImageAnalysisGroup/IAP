package de.ipk.ag_ba.datasources.ftp_file_system;

import java.util.ArrayList;
import java.util.Collection;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.datasources.DataSourceLevel;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.metacrop.PathwayWebLinkItem;

public class HsmFtpDataSourceLevelForUser implements DataSourceLevel {
	
	private final String user;
	private final ArrayList<ExperimentHeader> experiments;
	private String description;
	private final int n;
	
	public HsmFtpDataSourceLevelForUser(String user, ArrayList<ExperimentHeader> experiments) {
		this.user = user;
		this.experiments = experiments;
		this.n = experiments.size();
	}
	
	@Override
	public Collection<DataSourceLevel> getSubLevels() {
		return new ArrayList<DataSourceLevel>();
	}
	
	@Override
	public Collection<ExperimentReference> getExperiments() {
		Collection<ExperimentReference> res = new ArrayList<ExperimentReference>();
		for (ExperimentHeader eh : experiments) {
			res.add(new ExperimentReference(eh));
		}
		return res;
	}
	
	@Override
	public Collection<PathwayWebLinkItem> getPathways() {
		return new ArrayList<PathwayWebLinkItem>();
	}
	
	@Override
	public NavigationImage getIconInactive() {
		return IAPmain.loadIcon("img/ext/user-user_t.png");
	}
	
	@Override
	public NavigationImage getIconActive() {
		return IAPmain.loadIcon("img/ext/user-user_t.png");
	}
	
	@Override
	public String getName() {
		return user + " (" + n + ")";
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
