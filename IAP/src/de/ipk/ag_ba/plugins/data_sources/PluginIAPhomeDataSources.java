package de.ipk.ag_ba.plugins.data_sources;

import java.util.ArrayList;
import java.util.Collection;

import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.DataSourceGroup;
import de.ipk.ag_ba.datasources.http_folder.IAPnewsLinksSource;
import de.ipk.ag_ba.datasources.http_folder.MetaCropDataSource;
import de.ipk.ag_ba.datasources.http_folder.RimasDataSource;
import de.ipk.ag_ba.datasources.http_folder.SBGNdataSource;
import de.ipk.ag_ba.datasources.http_folder.VANTEDdataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.WebFolder;
import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPhomeDataSources extends AbstractIAPplugin {
	private final DataSourceGroup dsg;
	
	public PluginIAPhomeDataSources() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP data sources plugin is beeing loaded");
		
		this.dsg = new DataSourceGroup() {
			@Override
			public String getTitle() {
				return "Bioinformatics Tools";
			}
			
			@Override
			public String getTooltip() {
				return "External Tools and Resources";
			}
			
			@Override
			public String getImage() {
				return "img/ext/gpl2/Gnome-Preferences-Desktop-Remote-Desktop-64.png";// dbelogo2.png";
			}
			
			@Override
			public String getNavigationImage() {
				return "img/ext/gpl2/Gnome-Preferences-Desktop-Remote-Desktop-64.png";// dbelogo2.png";
			}
			
			@Override
			public Collection<NavigationAction> getAdditionalActions() {
				Collection<NavigationAction> res = new ArrayList<NavigationAction>();
				res.add(WebFolder.getURLaction("Website", new IOurl("http://bioinformatics.ipk-gatersleben.de"), "img/browser.png"));
				return res;
			}
			
			@Override
			public String getIntroductionText() {
				return "<h2>Bioinformatics@IPK</h2>IAP additionally provides access and links to various bioinformatics resources, "
						+ "developed at the IPK. The included data sources and tools have been "
						+ "mainly developed by members of the group Plant Bioinformatics and Image Analysis. "
						+ "MetaCrop has been partly co-developed by BIT. "
						+ "To get details about the included data sources and information systems, click the included Website- and Reference-Links.";
			}
		};
	}
	
	@Override
	public DataSource[] getDataSources() {
		ArrayList<DataSource> result = new ArrayList<DataSource>();
		
		boolean toolLinks = SystemOptions.getInstance().getBoolean("IAP", "Show Bioinformatics Tools", true);
		if (toolLinks) {
			result.add(new IAPnewsLinksSource(dsg));
			result.add(new RimasDataSource(dsg));
			result.add(new MetaCropDataSource(dsg));
			result.add(new SBGNdataSource(dsg));
			result.add(new VANTEDdataSource(dsg));
		}
		return result.toArray(new DataSource[] {});
	}
}