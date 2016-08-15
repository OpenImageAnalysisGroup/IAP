package de.ipk.ag_ba.datasources.sub_folder_datasource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.mongodb.ActionDomainLogout;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.file_system.FileSystemSource;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author klukas
 */
public class SubFolderDatasource extends FileSystemSource {
	private boolean readOnly;
	
	public SubFolderDatasource(String dataSourceName, String folder, boolean readOnly) {
		super(new Library(), dataSourceName, folder, new String[] {},
				IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
				IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
				IAPmain.loadIcon("Gnome-Folder-documents.png"),
				IAPmain.loadIcon("Gnome-Folder-documents.png"));
		this.readOnly = readOnly;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) throws Exception {
		Collection<NavigationButton> res = new ArrayList<NavigationButton>();
		if (IAPmain.getRunMode() == IAPrunMode.WEB)
			res.add(new NavigationButton(new ActionDomainLogout(), src.getGUIsetting()));
		try {
			readDataSource();
			for (String f : expFolders) {
				VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2("hsm_" + f, VfsFileProtocol.LOCAL, f,
						"file", "localhost", SystemAnalysis.getUserName(), null, url + File.separator + f, true, false, f);
				
				if (readOnly)
					vfs.forceReadOnly = true;
				
				res.add(vfs.getNavigationButton(src.getGUIsetting()));
			}
		} catch (Exception e) {
			if (e.getCause() != null && e.getCause().getCause() != null)
				throw new RuntimeException(e.getCause().getCause() + "");
			else
				throw new RuntimeException(e);
		}
		
		return res;
	}
	
	private List<String> expFolders = new ArrayList<>();
	
	@Override
	public void readDataSource() throws Exception {
		this.read = true;
		expFolders.clear();
		File dir = new File(url);
		String[] entries = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return new File(dir + File.separator + name).exists() && new File(dir + File.separator + name).isDirectory()
						&& new File(dir + File.separator + name + File.separator + VirtualFileSystemVFS2.DIRECTORY_FOLDER_NAME).exists();
			}
		});
		for (String f : entries) {
			expFolders.add(f);
		}
	}
	
	@Override
	public String getName() {
		return dataSourceName;
	}
	
	@Override
	public void setLogin(String login, String password) {
		super.setLogin(login, password);
	}
}
