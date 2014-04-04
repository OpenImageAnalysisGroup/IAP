package tests.JMP.methods;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;

import de.ipk.ag_ba.image.structures.Image;

public class HelperMethods {
	
	public static Image readImageAbsPath(String inp) {
		inp = inp.replace("\\", "/");
		File fff = new File(inp);
		Image img = null;
		
		try {
			img = new Image(ImageIO.read(fff));
		} catch (IOException e) {
			System.err.println("Can't read image file '" + inp + "'!");
		}
		return img;
	}
	
	public static Image readImage(String inp) {
		inp = inp.replace("\\", "/");
		inp = System.getProperty("user.home") + inp;
		File fff = new File(inp);
		Image img = null;
		
		try {
			img = new Image(ImageIO.read(fff));
		} catch (IOException e) {
			System.err.println("Can't read image file '" + inp + "'!");
		}
		return img;
	}
	
	public static int[][] normalize(int[][] img) {
		int[][] res = new int[img.length][img[0].length];
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		double temp = 0.0;
		int val = 0;
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				temp = img[x][y];
				if (temp < min)
					min = temp;
				if (temp > max)
					max = temp;
			}
		}
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				val = (int) (255 * ((img[x][y] - min) / (max - min)));
				if (val > 0)
					res[x][y] = new Color(val, val, val).getRGB();
			}
		}
		return res;
	}
	
	public static int[][] getGrayImageAs2dArray(Image grayImage) {
		int[] img1d = grayImage.getAs1A();
		int c, r, y = 0;
		int w = grayImage.getWidth();
		int h = grayImage.getHeight();
		int[][] res = new int[w][h];
		
		for (int idx = 0; idx < img1d.length; idx++) {
			c = img1d[idx];
			r = ((c & 0xff0000) >> 16);
			if (idx % w == 0 && idx > 0)
				y++;
			res[idx % w][y] = r;
		}
		return res;
	}
	
	public static int getNumOfFilesAbsPath(String pathname, String string) {
		File directory = new File(pathname);
		String[] list = directory.list(); // optional: filter
		int count = list.length;
		return count;
	}
	
	public static int getNumOfFiles(String pathname, String string) {
		pathname = System.getProperty("user.home") + pathname;
		File directory = new File(pathname);
		String[] list = directory.list(); // optional: filter
		int count = list.length;
		return count;
	}
	
	public static void write(String pathname, String filename, String data) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(System.getProperty("user.home") + pathname + filename + ".txt")));
			out.write(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void write(String pathname, String filename, double[] data) {
		String str = Arrays.toString(data);
		write(pathname, filename, str);
	}
	
	/**
	 * Test: Location of 3 points
	 * return: - 1, if p2 is "right/left" to p1 and p3 else 1 (0 = points are collinear)
	 **/
	public static int ccw(Point2d p1, Point2d p2, Point2d p3) {
		int val = (int) ((p3.x - p1.x) * (p2.y - p1.y) - (p2.x - p1.x) * (p3.y - p1.y));
		return val > 1 ? 1 : (val < 1 ? -1 : 0);
	}
	
	public static int[][] crop(int[][] img, int w, int h, int pLeft, int pRight, int pTop,
			int pBottom) {
		int[][] res = new int[pRight - pLeft][pBottom - pTop];
		pLeft = Math.max(pLeft, 0);
		pRight = Math.min(pRight, w);
		pTop = Math.max(pTop, 0);
		pBottom = Math.min(pBottom, h);
		
		for (int x = pLeft; x < pRight; x++) {
			for (int y = pTop; y < pBottom; y++) {
				res[x - pLeft][y - pTop] = img[x][y];
			}
		}
		return res;
	}
	
	public static double[] convert(Object[] array) {
		double[] out = new double[array.length];
		int idx = 0;
		for (Object num : array) {
			out[idx] = Double.parseDouble(String.valueOf(num));
			idx++;
		}
		return out;
	}
}
