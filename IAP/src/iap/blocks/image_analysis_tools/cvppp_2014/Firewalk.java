package iap.blocks.image_analysis_tools.cvppp_2014;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

import org.Colors;
import org.Vector2i;
import org.Vector3i;

import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */
public class Firewalk {
	private static final int BURNING_RED = Color.RED.getRGB();
	private final Image img;
	private final float[][] labimg;
	private final float firePowerPerStep = 30f;
	private final int[] intImg;
	
	private final int[] intImgRes;
	private final boolean debug;
	private Image debugImage, debugImageWindow;
	private final int w;
	private final int h;
	private boolean simpleFillMode;
	
	public Firewalk(Image img, boolean debug) {
		this.img = img;
		this.debug = debug;
		this.w = img.getWidth();
		this.h = img.getHeight();
		this.labimg = img.getLab(false);
		this.intImg = img.copy().getAs1A();
		this.intImgRes = new int[intImg.length];
		for (int i = 0; i < intImgRes.length; i++)
			intImgRes[i] = ImageOperation.BACKGROUND_COLORint;
		
		if (debug) {
			this.debugImage = new Image(img.getWidth() * 3, img.getHeight(), ImageOperation.BACKGROUND_COLORint);
			debugImageWindow = debugImage.show("Debug View Fire");
		}
	}
	
	public Image igniteFireAndBurnColorsDown(ArrayList<Vector2i> startPoints, int radius, double minFill) {
		double saturation = 0.8;
		return igniteFireAndBurnColorsDown(startPoints, Colors.get(startPoints.size(), saturation), radius, minFill);
	}
	
	public Image igniteFireAndBurnColorsDown(ArrayList<Vector2i> startPoints, ArrayList<Color> startColors, int radius, double minFill) {
		ArrayList<LinkedList<Vector3i>> todoLists = new ArrayList<LinkedList<Vector3i>>();
		for (Vector2i p : startPoints) {
			LinkedList<Vector3i> queue = new LinkedList<Vector3i>();
			queue.add(new Vector3i(p.x, p.y, 100));
			todoLists.add(queue);
		}
		boolean allEmpty;
		final ColorSpaceConverter convert = new ColorSpaceConverter();
		do {
			allEmpty = true;
			int todoIdx = 0;
			for (LinkedList<Vector3i> todo : todoLists) {
				Color segmentResultColor = startColors.get(todoIdx);
				todoIdx++;
				LinkedList<Vector3i> finished = new LinkedList<Vector3i>();
				LinkedList<Vector3i> ignited = new LinkedList<Vector3i>();
				for (Vector3i burningPoint : todo) {
					int pidx = burningPoint.x + burningPoint.y * w;
					boolean l_burned = false, a_burned = false, b_burned = false;
					if (!simpleFillMode) {
						float l = 255f - labimg[0][pidx];
						float a = labimg[1][pidx];
						float b = labimg[2][pidx];
						int regionFillGrade = burningPoint.z;
						
						double burnSpeed = firePowerPerStep * regionFillGrade / 100d;
						// System.out.println(regionFillGrade);
						
						if (l > burnSpeed)
							l -= burnSpeed;
						else {
							l_burned = true;
							l = 0;
						}
						if (Math.abs(a - 128) > burnSpeed) {
							if (a > 128)
								a -= burnSpeed;
							else
								a += burnSpeed;
						} else {
							a_burned = true;
							a = 128;
						}
						if (Math.abs(b - 128) > burnSpeed) {
							if (b > 128)
								b -= burnSpeed;
							else
								b += burnSpeed;
						} else {
							b_burned = true;
							b = 128;
						}
						labimg[0][pidx] = 255f - l;
						labimg[1][pidx] = a;
						labimg[2][pidx] = b;
					}
					if (simpleFillMode || (l_burned && a_burned && b_burned)) {
						// place is burned
						finished.add(burningPoint);
						intImg[pidx] = ImageOperation.BACKGROUND_COLORint;
						intImgRes[pidx] = segmentResultColor.getRGB();
						// spread fire
						for (Vector3i p : new Vector3i[] { up(burningPoint), left(burningPoint), right(burningPoint), down(burningPoint) })
							if (p != null && !isBackgroundOrBurnedOrBurning(p)) {
								ignited.add(p);
								intImg[p.x + p.y * w] = BURNING_RED;
							}
					}
				}
				for (Vector3i burned : finished)
					todo.remove(burned);
				for (Vector3i newFire : ignited) {
					boolean found = false;
					for (Vector3i p : todo) {
						if (p.x == newFire.x && p.y == newFire.y) {
							found = true;
							break;
						}
					}
					if (!found) {
						int regionColor = segmentResultColor.getRGB();
						newFire.z = (int) ((100d - minFill) * getFillGradeOfRegion(newFire.x, newFire.y, regionColor, radius) + minFill);// minimum speed 1
						todo.add(newFire);
					}
				}
				if (!todo.isEmpty())
					allEmpty = false;
			}
			if (debug) {
				debugImage = debugImage.io().drawAndFillRect(w * 0, 0, img.getAs2A()).getImage();
				int[] labImg = new int[intImg.length];
				for (int i = 0; i < labImg.length; i++) {
					int[] rgb = convert.LABtoRGB(labimg[0][i] / 2.55f, labimg[1][i] - 128f, labimg[2][i] - 128f);;
					rgb[0] = rgb[0] > 255 ? 255 : rgb[0];
					rgb[0] = rgb[0] < 0 ? 0 : rgb[0];
					rgb[1] = rgb[1] > 255 ? 255 : rgb[1];
					rgb[1] = rgb[1] < 0 ? 0 : rgb[1];
					rgb[2] = rgb[2] > 255 ? 255 : rgb[2];
					rgb[2] = rgb[2] < 0 ? 0 : rgb[2];
					labImg[i] = new Color(rgb[0], rgb[1], rgb[2]).getRGB();
				}
				debugImage = debugImage.io().drawAndFillRect(w * 1, 0, new Image(w, h, labImg).getAs2A()).getImage();
				debugImage = debugImage.io().drawAndFillRect(w * 2, 0, new Image(w, h, intImgRes).getAs2A()).getImage();
				debugImageWindow.update(debugImage);
			}
		} while (!allEmpty);
		return new Image(w, h, intImgRes);
	}
	
	/**
	 * @return 0.0 ... 1.0
	 */
	private double getFillGradeOfRegion(int x, int y, int regionColor, int radius) {
		int n = 0;
		int sameColor = 0;
		double r = radius;
		int minX = x - radius;
		int maxX = x + radius;
		int minY = y - radius;
		int maxY = y + radius;
		if (minX < 0)
			minX = 0;
		if (minY < 0)
			minY = 0;
		if (maxX >= w)
			maxX = w - 1;
		if (maxY >= h)
			maxY = h - 1;
		for (int xi = minX; xi <= maxX; xi++)
			for (int yi = minY; yi <= maxY; yi++) {
				double dist = Math.sqrt((xi - x) * (xi - x) + (yi - y) * (yi - y));
				if (dist > r)
					continue;
				if (intImgRes[xi + yi * w] == regionColor)
					sameColor++;
				n++;
			}
		return sameColor / (double) n;
	}
	
	private boolean isBackgroundOrBurnedOrBurning(Vector3i p) {
		int c = intImg[p.x + p.y * w];
		return c == ImageOperation.BACKGROUND_COLORint ||
				c == Color.BLACK.getRGB() ||
				c == BURNING_RED;
	}
	
	private Vector3i up(Vector3i p) {
		if (p.y == 0)
			return null;
		else
			return new Vector3i(p.x, p.y - 1);
	}
	
	private Vector3i left(Vector3i p) {
		if (p.x == 0)
			return null;
		else
			return new Vector3i(p.x - 1, p.y);
	}
	
	private Vector3i right(Vector3i p) {
		if (p.x == img.getWidth() - 1)
			return null;
		else
			return new Vector3i(p.x + 1, p.y);
	}
	
	private Vector3i down(Vector3i p) {
		if (p.y == img.getHeight() - 1)
			return null;
		else
			return new Vector3i(p.x, p.y + 1);
	}
	
	public void setSimpleFillMode(boolean b) {
		this.simpleFillMode = b;
	}
}
