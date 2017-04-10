package de.ipk.ag_ba.datasources.sub_folder_datasource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.ipk.ag_ba.commands.ActionNavigateDataSource;
import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.commands.mongodb.ActionDomainLogout;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.datasources.file_system.FileSystemSource;
import de.ipk.ag_ba.datasources.file_system.VfsFileSystemSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
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
	
	public SubFolderDatasource(String dataSourceName, String folder, boolean readOnly, boolean isSubFolder) {
		super(new Library(), dataSourceName, folder, new String[] {},
				isSubFolder ? IAPmain.loadIcon("img/ext/gpl2/Gnome-Folder-64.png") : IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
				isSubFolder ? IAPmain.loadIcon("img/ext/gpl2/Gnome-Folder-64.png") : IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
				IAPmain.loadIcon("img/ext/gpl2/Gnome-Folder-64.png"),
				IAPmain.loadIcon("img/ext/gpl2/Gnome-Folder-64.png"));
		this.readOnly = readOnly;
	}
	
	@Override
	public Collection<NavigationButton> getAdditionalEntities(NavigationButton src) throws Exception {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (IAPmain.getRunMode() == IAPrunMode.WEB)
			res.add(new NavigationButton(new ActionDomainLogout(), src.getGUIsetting()));
		try {
			readDataSource();
			for (String f : expFolders) {
				VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2("hsm_" + f, VfsFileProtocol.LOCAL, f,
						"file", "localhost", SystemAnalysis.getUserName(), null, urlFSS + File.separator + f, true, false, f);
				
				if (readOnly)
					vfs.forceReadOnly = true;
				
				vfs.setIcon("img/ext/gpl2/Gnome-Folder-pictures.png");// Gnome-Folder-publicshare.png");// Gnome-Folder-videos.png");//
																						// Gnome-Folder-documents.png");
				
				res.add(vfs.getNavigationButton(src.getGUIsetting()));
			}
			for (String fn : filesOrDirectories) {
				if (new File(urlFSS + File.separator + fn).isDirectory()) {
					DataSource ds = new SubFolderDatasource(fn, urlFSS + File.separator + fn, readOnly, true);
					res.add(new NavigationButton(new ActionNavigateDataSource(ds), src.getGUIsetting()));
				}
			}
			for (String fn : filesOrDirectories) {
				if (!new File(urlFSS + File.separator + fn).isDirectory()) {
					VirtualFileSystem vfs = new VirtualFileSystemFolderStorage("temp-fs", "local.io", fn, urlFSS + File.separator + fn);
					ArrayList<NavigationAction> nal = new ArrayList<>();
					Library ll = new Library();
					VfsFileSystemSource.createActionOrLibEntryForGivenFilename(vfs, fn, null, nal, ll);
					for (NavigationAction na : nal) {
						res.add(new NavigationButton(na, src.getGUIsetting()));
					}
					for (Book b : ll.getBooksInFolder("")) {
						res.add(b.getNavigationButton(src));
					}
				}
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
	private List<String> filesOrDirectories = new ArrayList<>();
	
	@Override
	public void readDataSource() throws Exception {
		this.read = true;
		expFolders.clear();
		filesOrDirectories.clear();
		File dir = new File(urlFSS);
		if (dir.exists()) {
			String[] entries = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					for (String knownExt : new String[] { ".txt", ".url", ".webloc", ".gml", ".graphml", ".pdf", ".html", ".htm" })
						if (name.endsWith(knownExt))
							return true;
					return new File(dir + File.separator + name).exists() && new File(dir + File.separator + name).isDirectory();
				}
			});
			for (String name : entries) {
				if (new File(dir + File.separator + name + File.separator + VirtualFileSystemVFS2.DIRECTORY_FOLDER_NAME).exists())
					expFolders.add(name);
				else
					filesOrDirectories.add(name);
			}
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
