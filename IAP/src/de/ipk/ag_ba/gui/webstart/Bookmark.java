/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Oct 18, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.webstart;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.ErrorMsg;
import org.ReleaseInfo;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class Bookmark {
	private String title;
	private String target;
	private final int position;
	private BufferedImage icon;
	private boolean valid = true;
	private String optStaticIconId;
	
	public Bookmark(int position, String title, String target, BufferedImage icon, String optStaticIconId) {
		this.position = position;
		this.title = title;
		this.target = target;
		this.icon = icon;
		this.optStaticIconId = optStaticIconId;
	}
	
	public Bookmark(int position) {
		this.position = position;
		try {
			TextFile tf = new TextFile(getFileName());
			title = tf.get(0);
			try {
				// scan for titles such as "test (3)", which should be displayed as "test"
				if (title != null && title.contains("(")) {
					String titleNo = title.substring(title.lastIndexOf("(") + "(".length());
					if (titleNo.contains(")")) {
						titleNo = titleNo.substring(0, titleNo.indexOf(")"));
						titleNo = StringManipulationTools.stringReplace(titleNo, "/", "");
						int n = Integer.parseInt(titleNo);
						if (n < Integer.MAX_VALUE) {
							title = title.substring(0, title.lastIndexOf("(")).trim();
						}
					}
				}
			} catch (Exception e) {
				
			}
			target = tf.get(1);
			icon = ImageIO.read(new File(getFileName(".png")));
			optStaticIconId = getFileName(".png");
		} catch (IOException e) {
			valid = false;
			System.out.println("Could not process bookmark " + position + " (" + getFileName() + ")");
		}
	}
	
	public void save() throws IOException {
		// save icon
		OutputStream fos = new FileOutputStream(getFileName(".png"));
		MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(fos);
		try {
			ImageIO.write(icon, "png", ios);
		} finally {
			fos.close();
		}
		
		// save link info
		TextFile tf = new TextFile();
		tf.clear();
		tf.add(title);
		tf.add(target);
		tf.write(new File(getFileName()));
		System.out.println("Saved " + getFileName());
	}
	
	public boolean delete() {
		if (new File(getFileName(".png")).exists() && new File(getFileName()).exists()) {
			return new File(getFileName(".png")).delete() && new File(getFileName()).delete();
		} else
			if (new File(getFileName()).exists()) {
				return new File(getFileName()).delete();
			} else
				return false;
	}
	
	private String getFileName() {
		return getFileName(".txt");
	}
	
	private String getFileName(String extension) {
		return getFileName(extension, position);
	}
	
	private static String getFileName(String extension, int position) {
		String folder = ReleaseInfo.getAppSubdirFolderWithFinalSep("iap-bookmarks");
		String filename = folder + "bookmark" + position + extension;
		return filename;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public static ArrayList<Bookmark> getBookmarks() {
		ArrayList<Bookmark> res = new ArrayList<Bookmark>();
		// if (!ReleaseInfo.isRunningAsApplet())
		for (int i = 0; i < 99; i++) {
			if (new File(getFileName(".txt", i)).exists())
				res.add(new Bookmark(i));
		}
		return res;
	}
	
	private static int getNextFreePosition() {
		int res = 0;
		for (int i = 0; i < 99; i++) {
			if (new File(getFileName(".txt", i)).exists())
				res = i + 1;
		}
		if (res <= 99)
			return res;
		else
			return -1;
	}
	
	public static boolean add(String title, String target, BufferedImage icon) {
		for (Bookmark b : getBookmarks()) {
			if (b.getTarget().equals(target)) {
				b.icon = icon;
				b.title = title;
				try {
					b.save();
					return true;
				} catch (IOException e) {
					ErrorMsg.addErrorMessage(e);
					return false;
				}
			}
		}
		int pos = getNextFreePosition();
		if (pos < 0)
			return false;
		Bookmark b = new Bookmark(pos, title, target, icon, title + "/" + target);
		if (!b.isValid())
			return false;
		try {
			b.save();
			return true;
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
	}
	
	public String getTarget() {
		return target;
	}
	
	public BufferedImage getImage() {
		return icon;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getStaticIconId() {
		return optStaticIconId;
	}
}
