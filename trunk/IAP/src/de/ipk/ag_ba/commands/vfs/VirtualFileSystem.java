package de.ipk.ag_ba.commands.vfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ReleaseInfo;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public abstract class VirtualFileSystem {
	
	public static Collection<VirtualFileSystem> getKnown() {
		ArrayList<VirtualFileSystem> res = new ArrayList<VirtualFileSystem>();
		res.add(new VirtualFileSystemFolderStorage(
				"file-desktop",
				"File I/O",
				"Desktop" + File.separator + "VFS",
				ReleaseInfo.getDesktopFolder() + File.separator + "VFS"));
		return res;
	}
	
	public abstract String getTargetName();
	
	public abstract String getTransferProtocolName();
	
	public abstract String getTargetPathName();
	
	public abstract String getPrefix();
	
	public String getResultPathNameForUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @return List of file names found at root of VFS source
	 */
	public abstract ArrayList<String> listFiles();
	
	public abstract IOurl getIOurlFor(String fileName);
	
	public void saveExperiment(ExperimentReference experimentReference,
			BackgroundTaskStatusProviderSupportingExternalCall statusProvider) throws Exception {
		ActionDataExportToVfs a = new ActionDataExportToVfs(null, experimentReference, this);
		a.performActionCalculateResults(null);
	}
}
