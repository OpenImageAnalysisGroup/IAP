package de.ipk.ag_ba.mongo;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public enum IAPwebcam {
	
	MAIZE, BARLEY;
	
	public String getFileName() {
		switch (this) {
			case MAIZE:
				return "maize " + SystemAnalysis.getCurrentTimeInclSec() + ".jpg";
			case BARLEY:
				return "barley " + SystemAnalysis.getCurrentTimeInclSec() + ".jpg";
		}
		return null;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case MAIZE:
				return "maize";
			case BARLEY:
				return "barley";
		}
		return null;
	}
	
	public FlexibleImage getSnapshot() throws Exception {
		InputStream is = getSnapshotJPGdata();
		BufferedImage img = ImageIO.read(is);
		return new FlexibleImage(img);
	}
	
	public FlexibleImage getSnapshotLR() throws Exception {
		InputStream is = getSnapshotJPGdataLR();
		BufferedImage img = ImageIO.read(is);
		return new FlexibleImage(img);
	}
	
	public InputStream getSnapshotJPGdataLR() throws Exception {
		String imageSrc = null;
		if (this == MAIZE)
			imageSrc = "http://ba-10.ipk-gatersleben.de/SnapshotJPEG?Resolution=640x480&Quality=Clarity";
		if (this == BARLEY)
			imageSrc = "root:lemnatec@http://lemnacam.ipk-gatersleben.de/jpg/image.jpg?timestamp=" + System.currentTimeMillis();
		
		InputStream is;
		if (imageSrc.contains("@")) {
			String userPass = imageSrc.split("@")[0];
			String urlStr = imageSrc.split("@")[1];
			String user = userPass.split(":")[0];
			String pass = userPass.split(":")[1];
			is = HttpBasicAuth.downloadFileWithAuth(urlStr, user, pass);
		} else
			is = new IOurl(imageSrc).getInputStream();
		return is;
	}
	
	public InputStream getSnapshotJPGdata() throws Exception {
		String imageSrc = null;
		if (this == MAIZE)
			imageSrc = "http://ba-10.ipk-gatersleben.de/SnapshotJPEG?Resolution=1280x960&Quality=Clarity";
		if (this == BARLEY)
			imageSrc = "root:lemnatec@http://lemnacam.ipk-gatersleben.de/jpg/image.jpg?timestamp=" + System.currentTimeMillis();
		
		InputStream is;
		if (imageSrc.contains("@")) {
			String userPass = imageSrc.split("@")[0];
			String urlStr = imageSrc.split("@")[1];
			String user = userPass.split(":")[0];
			String pass = userPass.split(":")[1];
			is = HttpBasicAuth.downloadFileWithAuth(urlStr, user, pass);
		} else
			is = new IOurl(imageSrc).getInputStream();
		return is;
	}
	
	public boolean hasVideoStream() {
		return this == BARLEY;
	}
	
}
