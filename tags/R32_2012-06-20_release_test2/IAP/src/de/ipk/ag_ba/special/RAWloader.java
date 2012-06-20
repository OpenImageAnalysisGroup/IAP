package de.ipk.ag_ba.special;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.OpenFileDialogService;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class RAWloader {
	
	public static void main(String[] args) throws IOException, Exception {
		ArrayList<File> files;
		if (args == null || args.length < 1) {
			files = OpenFileDialogService.getFiles(new String[] { ".raw" }, "RAW Images");
			if (files == null || files.size() == 0)
				System.exit(1);
		} else {
			files = new ArrayList<File>();
			for (String a : args)
				files.add(new File(a));
		}
		while (files.size() > 0) {
			File fi1 = files.remove(0);
			for (int w : new int[] { 1285 }) {
				for (int h : new int[] { 1000 }) {
					for (int rowOff : new int[] { -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }) {
						int[] img = new int[w * h];
						
						long size = fi1.length();
						byte[] contents = new byte[(int) size];
						FileInputStream in = new FileInputStream(fi1);
						in.read(contents);
						in.close();
						int x = 0;
						for (int offset : new int[] { 0 }) {
							int pixel = 0;
							for (int fileIndex = 0; fileIndex < img.length; fileIndex++) {
								img[pixel] = byteArrayToInt(contents, pixel * 4 + offset);
								// int g = contents[pixel * 4 + offset + 2] + 128;
								// img[pixel] = new Color(g, g, g).getRGB();
								pixel++;
								x++;
								if (x == w) {
									x = 0;
									offset += rowOff;
								}
							}
							
							FlexibleImage f1 = new FlexibleImage(w, h, img);
							f1.print("RAW file w=" + w + ", h=" + h + ", off=" + offset + ", rowOff=" + rowOff + " " + fi1.getName());
						}
					}
				}
			}
		}
	}
	
	public static final int byteArrayInvToInt(byte[] b, int offset) {
		return (b[3 + offset] << 24)
				+ ((b[2 + offset] & 0xFF) << 16)
				+ ((b[1 + offset] & 0xFF) << 8)
				+ (b[0 + offset] & 0xFF);
	}
	
	public static final int byteArrayToInt(byte[] b, int offset) {
		// int r = 128 + (b[3 + offset]);
		// return new Color(r, r, r).getRGB();
		return (b[0 + offset] << 24)
				+ ((b[1 + offset] & 0xFF) << 16)
				+ ((b[2 + offset] & 0xFF) << 8)
				+ (b[3 + offset] & 0xFF);
	}
}
