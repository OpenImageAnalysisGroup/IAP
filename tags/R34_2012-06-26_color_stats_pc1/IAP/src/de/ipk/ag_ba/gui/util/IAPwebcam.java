package de.ipk.ag_ba.gui.util;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.mongo.HttpBasicAuth;

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
		if (this == MAIZE)
			;// 640x480
		if (this == BARLEY)
			;// 640x480
		return getSnapshotJPGdataLRintern();
	}
	
	public InputStream getSnapshotJPGdata() throws Exception {
		int n = 1;
		if (n == 1)
			return getSnapshotJPGdataIntern();
		else {
			ArrayList<FlexibleImage> il = new ArrayList<FlexibleImage>();
			for (int i = 0; i < n; i++) {
				InputStream is = getSnapshotJPGdataIntern();
				if (is != null) {
					BufferedImage img = ImageIO.read(is);
					il.add(new FlexibleImage(img).io().resize(0.33d).getImage());
				}
			}
			
			ImageOperation i = ImageOperation.median(il);
			return i.multiplicateImageChannelsWithFactors(new double[] { 10d, 10d, 10d }).getImage().getAsPNGstream();
		}
	}
	
	private InputStream getSnapshotJPGdataLRintern() throws Exception {
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
	
	private InputStream getSnapshotJPGdataIntern() throws Exception {
		String imageSrc = null;
		if (this == MAIZE)
			imageSrc = "http://ba-10.ipk-gatersleben.de/SnapshotJPEG?Resolution=640x480&Quality=Clarity"; // 1280 x 960
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
