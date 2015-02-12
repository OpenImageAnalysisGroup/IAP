package iap;

import java.io.File;

import org.HomeFolder;
import org.SystemAnalysis;

import com.mongodb.gridfs.GridFSDBFile;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author Christian Klukas
 */
public class SaveWebcasts {
	
	public void save() {
		try {
			for (MongoDB m : MongoDB.getMongos())
				for (GridFSDBFile sf : m.getSavedScreenshots()) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Save " + sf.getFilename());
					HomeFolder.copyFile(sf.getInputStream(), new File(sf.getFilename()));
				}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void saveScreenshot() {
		IAPservice.storeDesktopImage(true);
	}
}
