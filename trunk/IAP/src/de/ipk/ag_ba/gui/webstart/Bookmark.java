/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
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

	public Bookmark(int position, String title, String target, BufferedImage icon) {
		this.position = position;
		this.title = title;
		this.target = target;
		this.icon = icon;
	}

	public Bookmark(int position) {
		this.position = position;
		try {
			TextFile tf = new TextFile(getFileName());
			title = tf.get(0);
			target = tf.get(1);
			icon = ImageIO.read(new File(getFileName(".png")));
		} catch (IOException e) {
			valid = false;
			System.out.println("Could not process bookmark " + position + " (" + getFileName() + ")");
		}
	}

	public void save() throws IOException {
		// save icon
		OutputStream fos = new FileOutputStream(getFileName(".png"));
		MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(fos);
		ImageIO.write(icon, "png", ios);

		// save link info
		TextFile tf = new TextFile(getFileName());
		tf.clear();
		tf.add(title);
		tf.add(target);
		tf.write(new File(getFileName()));
	}

	public boolean delete() {
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
		int pos = getNextFreePosition();
		if (pos < 0)
			return false;
		Bookmark b = new Bookmark(pos, title, target, icon);
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
}
