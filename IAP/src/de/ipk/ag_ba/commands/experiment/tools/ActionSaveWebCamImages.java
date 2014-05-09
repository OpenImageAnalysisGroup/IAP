package de.ipk.ag_ba.commands.experiment.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import org.AttributeHelper;
import org.OpenFileDialogService;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionSaveWebCamImages extends AbstractNavigationAction {
	
	private NavigationButton src;
	private long numberOfImagesInRange;
	private String fs;
	private long saved = 0;
	private long storageSize = 0;
	private MongoDB m;
	private Date startdate;
	private Date enddate;
	
	public ActionSaveWebCamImages(String tooltip) {
		super(tooltip);
	}
	
	public ActionSaveWebCamImages(MongoDB m, String fs, long numberOfImagesInRange, Date startdate, Date enddate) {
		this("Save " + numberOfImagesInRange + " from camera " + fs);
		this.m = m;
		this.fs = fs;
		this.numberOfImagesInRange = numberOfImagesInRange;
		this.startdate = startdate;
		this.enddate = enddate;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		saved = 0;
		storageSize = 0;
		File tf = OpenFileDialogService.getDirectoryFromUser("Select Output Folder");
		if (tf != null) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">Saving images in " + tf.getCanonicalPath());
			AttributeHelper.showInFileBrowser(tf.getCanonicalPath(), null);
			long idx = 0;
			ArrayList<String> fnl = m.getWebCamStorageFileNames(fs, startdate, enddate);
			long start = System.currentTimeMillis();
			String fsn = StringManipulationTools.stringReplace(fs, ".files", "");
			GridFS gfs = m.getGridFS(fsn);
			ArrayList<GridFSDBFile> fll = new ArrayList<GridFSDBFile>();
			if (gfs != null)
				for (String fn : fnl) {
					GridFSDBFile f = gfs.findOne(fn);
					fll.add(f);
				}
			Collections.sort(fll, new Comparator<GridFSDBFile>() {
				@Override
				public int compare(GridFSDBFile o1, GridFSDBFile o2) {
					return o1.getUploadDate().compareTo(o2.getUploadDate());
				}
			});
			HashSet<Long> known = new HashSet<Long>();
			for (GridFSDBFile f : fll) {
				if (known.contains(f.getUploadDate().getTime()))
					continue;
				else
					known.add(f.getUploadDate().getTime());
				idx++;
				status.setCurrentStatusValueFine(100d * idx / fnl.size());
				String fn = f.getFilename();
				status.setCurrentStatusText1("Save image " + idx + " \"" + fn + "\"");
				File target = new File(tf.getCanonicalPath() + File.separator + "img" + StringManipulationTools.getFileSystemName(
						StringManipulationTools.formatNumber(idx, "000000") + fn.substring(fn.lastIndexOf("."))));
				OutputStream fos = new FileOutputStream(target);
				long stored = ResourceIOManager.copyContent(f.getInputStream(), fos);
				target.setLastModified(f.getUploadDate().getTime());
				long curr = System.currentTimeMillis();
				storageSize += stored;
				status.setCurrentStatusText2(storageSize / 1024 / 1024 + " MB (" +
						StringManipulationTools.formatNumber(
								storageSize / 1024d / 1024d / (curr - start) * 1000d,
								"#.#") + " MB/s)");
				saved = idx;
			}
			status.setCurrentStatusText1("Processing completed");
			status.setCurrentStatusText2("Saved " + storageSize / 1024 / 1024 + " MB");
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(saved + " images have been saved (" + storageSize / 1024 / 1024 + " MB)");
	}
	
	@Override
	public String getDefaultTitle() {
		String fsn = StringManipulationTools.stringReplace(fs, "fs_", "");
		fsn = StringManipulationTools.stringReplace(fsn, ".files", "");
		fsn = StringManipulationTools.stringReplace(fsn, "_", " ");
		return "Save images from " + fsn + " (" + numberOfImagesInRange + ")";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return IAPimages.saveAsArchive();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Save-64.png";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
}
