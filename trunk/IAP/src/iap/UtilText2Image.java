package iap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.Vector2i;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class UtilText2Image {
	
	public static void convertRstatFileToImage(ArrayList<String> inputFiles, ArrayList<String> outputFiles, ArrayList<String> imageSizeDefinition,
			int background, int correctMatch0, int correctMatch1, int incorrectMatch0, int incorrectMatch1)
			throws IOException {
		int n = min(inputFiles.size(), outputFiles.size(), imageSizeDefinition.size());
		LinkedList<Vector2i> cm0, cm1, im0, im1;
		cm0 = new LinkedList<Vector2i>();
		cm1 = new LinkedList<Vector2i>();
		im0 = new LinkedList<Vector2i>();
		im1 = new LinkedList<Vector2i>();
		
		for (int index = 0; index < n; index++) {
			String fileName = inputFiles.get(index);
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String s = in.readLine(); // skip first line
			while ((s = in.readLine()) != null) {
				String[] columns = s.split(" ");
				int x = Integer.parseInt(columns[0]);
				int y = Integer.parseInt(columns[1]);
				int real = Integer.parseInt(rq(columns[2]));
				int pred = Integer.parseInt(rq(columns[3]));
				if (real == 0 && real == pred)
					cm0.add(new Vector2i(x, y));
				if (real == 1 && real == pred)
					cm1.add(new Vector2i(x, y));
				if (real == 0 && real != pred)
					im0.add(new Vector2i(x, y));
				if (real == 1 && real != pred)
					im1.add(new Vector2i(x, y));
			}
			in.close();
			
			// create image
			String imageSizeDef = imageSizeDefinition.get(index);
			if (imageSizeDef == null || imageSizeDef.equalsIgnoreCase("NULL")) {
				imageSizeDef = detectImageSize(cm0, cm1, im0, im1);
			}
			int width = Integer.parseInt(imageSizeDef.split("x")[0]) + 1;
			int height = Integer.parseInt(imageSizeDef.split("x")[1]) + 1;
			int[] img = new int[width * height];
			Arrays.fill(img, background);
			for (Vector2i p : cm0)
				img[p.x + p.y * width] = correctMatch0;
			for (Vector2i p : cm1)
				img[p.x + p.y * width] = correctMatch1;
			for (Vector2i p : im0)
				img[p.x + p.y * width] = incorrectMatch0;
			for (Vector2i p : im1)
				img[p.x + p.y * width] = incorrectMatch1;
			new Image(width, height, img).saveToFile(outputFiles.get(index));
		}
	}
	
	/**
	 * Remove quotes (if contained in argument)
	 */
	private static String rq(String string) {
		if (string != null && string.startsWith("\"") && string.endsWith("\""))
			return string.substring(1, string.length() - 1);
		else
			return string;
	}
	
	@SuppressWarnings("unchecked")
	private static String detectImageSize(LinkedList<Vector2i> cm0, LinkedList<Vector2i> cm1, LinkedList<Vector2i> im0, LinkedList<Vector2i> im1) {
		// detect image size
		int maxX = 0;
		int maxY = 0;
		for (LinkedList<Vector2i> definedPixels : new LinkedList[] { cm0, cm1, im0, im1 }) {
			for (Vector2i p : definedPixels) {
				if (p.x > maxX)
					maxX = p.x;
				if (p.y > maxY)
					maxY = p.y;
			}
		}
		return maxX + "x" + maxY;
	}
	
	private static int min(int size1, int size2, int size3) {
		return Math.min(Math.min(size1, size2), size3);
	}
	
}
