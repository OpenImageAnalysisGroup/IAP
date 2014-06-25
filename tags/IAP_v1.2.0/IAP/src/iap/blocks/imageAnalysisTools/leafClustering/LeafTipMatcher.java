package iap.blocks.imageAnalysisTools.leafClustering;

import iap.blocks.extraction.Normalisation;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * @author pape
 */
public class LeafTipMatcher {
	
	private final ArrayList<LinkedList<LeafTip>> leafTipList;
	private final Plant matchedPlant;
	private double maxDistanceBetweenLeafTips = 50.0;
	private final int fac = 0;
	
	public LeafTipMatcher(String path) throws IOException {
		leafTipList = readCsv(path);
		if (leafTipList.isEmpty())
			System.out.println("No tips in file!!!!!");
		matchedPlant = new Plant();
	}
	
	public LeafTipMatcher(Collection<LinkedList<BorderFeature>> tipPositionsForEachDay, Normalisation norm) {
		leafTipList = convert(tipPositionsForEachDay, norm);
		matchedPlant = new Plant();
	}
	
	public LeafTipMatcher(Plant plant, LinkedList<BorderFeature> tipPositionsForOneDay,
			long timepoint, Normalisation norm) {
		leafTipList = convert(tipPositionsForOneDay, timepoint, norm);
		matchedPlant = plant;
	}
	
	public LeafTipMatcher(LinkedList<BorderFeature> leafTipListForOneDay, long timepoint, Normalisation norm) {
		leafTipList = convert(leafTipListForOneDay, timepoint, norm);
		matchedPlant = new Plant();
	}
	
	public void draw(Vismode vm, int w, int h, Normalisation norm) throws InterruptedException {
		int[][] img2d = new int[w][h];
		ImageOperation.fillArray(img2d, ImageOperation.BACKGROUND_COLORint);
		ImageStack is = new ImageStack();
		
		switch (vm) {
			case PERLEAF:
				is = visPerLeaf(img2d, matchedPlant, norm);
				break;
			case PERDAY:
				is = visPerDay(img2d, matchedPlant, norm);
				break;
		}
		is.show(vm.toString());
	}
	
	private ImageStack visPerDay(int[][] img2d, Plant plant, Normalisation norm) {
		Image img_all = new Image(img2d);
		ImageStack is = new ImageStack();
		
		LinkedList<Integer> days = new LinkedList<Integer>();
		LinkedList<Leaf> leafList = plant.getLeafList();
		
		// get days TODO save in plant object
		for (Leaf l : leafList) {
			int time = 0;
			for (LeafTip lt : l) {
				time = (int) lt.getTime();
				if (!days.contains(time))
					days.add(time);
			}
		}
		
		java.util.Collections.sort(days);
		
		int growTime = plant.getGrowTime();
		
		for (int day : days) {
			Image temp = new Image(img2d.clone());
			for (Leaf l : leafList) {
				for (LeafTip lt : l) {
					int currentTime = (int) lt.getTime();
					if (day == currentTime) {
						int r = (int) (255 * ((growTime - currentTime) / (double) (growTime)));
						int x = lt.getImageX(norm) + fac;
						int y = lt.getImageY(norm) + fac;
						
						img_all.io().canvas()
								.drawCircle(x, y, 15, new Color(255 - r, 0, r).getRGB(), 0.5, 3)
								.text(x + 15, y, new String("d: " + currentTime), new Color(0, 0, 0), 16);
						temp.io().canvas().drawCircle(x, y, 15, new Color(255 - r, 0, r).getRGB(), 0.5, 3)
								.fillRect(50, 50, 100, 30, Color.WHITE.getRGB())
								.text(65, 65, "DAY: " + currentTime, Color.BLACK);
					}
				}
				temp.io().canvas()
						.fillRect(50, 50, 100, 30, Color.WHITE.getRGB())
						.text(65, 65, "DAY: " + day, Color.BLACK, 24);
			}
			is.addImage("Day: " + day, temp);
		}
		img_all.io().canvas().text(65, 65, "All", Color.BLACK, 24);
		is.addImage("all", img_all);
		return is;
	}
	
	private ImageStack visPerLeaf(int[][] img2d, Plant plant, Normalisation norm) {
		Image img_all = new Image(img2d);
		ImageStack is = new ImageStack();
		Image temp = new Image(img2d);
		LinkedList<Leaf> leafList = plant.getLeafList();
		int growTime = plant.getGrowTime();
		int numLeafs = plant.getNumberOfLeaves();
		
		for (Leaf l : leafList) {
			int time = 0;
			for (LeafTip lt : l) {
				time = (int) lt.getTime();
				float hue = (float) (0.7 * ((numLeafs - l.leafID) / (double) (numLeafs)));
				int hsb = new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f)).getRGB();
				// Vector2d vec = (Vector2d) lt.getFeature("angle");
				// if (vec == null)
				// continue;
				int x = lt.getImageX(norm) + fac;
				int y = lt.getImageY(norm) + fac;
				img_all.io().canvas()
						.drawCircle(x, y, 10, hsb, 0.5, 2)
						// .drawLine(x, y, (int) (vec.x), (int) (vec.y), Color.BLUE.getRGB(), 0.5, 1)
						// .drawCircle(lt.x, lt.y, 10, new Color(lt.getLeafID() * 10, 100, 255 - lt.getLeafID()).getRGB(), 0.5, 2)
						.text(x - 5, y + 5, new String("" + lt.getTime()), new Color(0, 0, 0))
				// .text(lt.x - 5, lt.y + 12, new String("" + lt.getLeafID()), new Color(0, 0, 0));
				;
				temp.io().canvas().drawCircle(x, y, 10, hsb, 0.5, 2)
						.fillRect(50, 50, 100, 30, Color.WHITE.getRGB())
						.text(65, 65, "DAY: " + time, Color.BLACK);
				
			}
			temp.io().canvas()
					.fillRect(50, 50, 100, 30, Color.WHITE.getRGB())
					.text(65, 65, "Leaf: " + l.leafID, Color.BLACK, 24);
			is.addImage("leaf: " + l.leafID, temp.copy());
			temp = new Image(img2d);
		}
		img_all = img_all.io().canvas()
				.fillRect(50, 50, 100, 30, Color.WHITE.getRGB())
				.text(65, 65, "All Leaves", Color.BLACK, 24).getImage();
		is.addImage("all", img_all);
		return is;
	}
	
	public void matchLeafTips() {
		int snapshotIndex = 0;
		long time = 0;
		for (LinkedList<LeafTip> tempTipListIn : leafTipList) {
			if (tempTipListIn.isEmpty())
				continue;
			int tipIndex = 0;
			if (time == 0)
				time = tempTipListIn.getFirst().getTime();
			else
				time += (tempTipListIn.getFirst().getTime() - time);
			
			// first run, create new leaves
			if (snapshotIndex == 0 && matchedPlant.getNumberOfLeaves() <= 0) {
				for (LeafTip lt : tempTipListIn) {
					lt.setLeafID(tipIndex);
					matchedPlant.addNewLeaf(lt);
					tipIndex++;
				}
			} else {
				LinkedList<LeafTip> lastMatchedTips = matchedPlant.getLastTips();
				LinkedList<LeafTip> toMatch = tempTipListIn;
				while (!tempTipListIn.isEmpty() && !lastMatchedTips.isEmpty()) {
					LeafTip[] bestPair = new LeafTip[2];
					double bestDist = Double.MAX_VALUE;
					for (LeafTip lastMatchedTip : lastMatchedTips) {
						for (LeafTip tempTipIn : toMatch) {
							double dist = tempTipIn.dist(lastMatchedTip);
							if (dist < bestDist) {
								bestDist = dist;
								bestPair[0] = lastMatchedTip;
								bestPair[1] = tempTipIn;
							}
						}
					}
					lastMatchedTips.remove(bestPair[0]);
					tempTipListIn.remove(bestPair[1]);
					if (bestDist < maxDistanceBetweenLeafTips) {
						bestPair[1].setDist(bestDist);
						bestPair[1].setLeafID(bestPair[0].getLeafID());
						matchedPlant.addLeafTip(bestPair[1]);
					} else {
						bestPair[1].setDist(bestDist);
						bestPair[1].setLeafID(matchedPlant.getNumberOfLeaves() + 1);
						matchedPlant.addNewLeaf(bestPair[1]);
					}
				}
				if (!toMatch.isEmpty())
					for (LeafTip lt : toMatch) {
						lt.setLeafID(matchedPlant.getNumberOfLeaves() + 1);
						matchedPlant.addNewLeaf(lt);
					}
				tipIndex++;
			}
			snapshotIndex++;
		}
		matchedPlant.setGrowTime((int) time);
		this.leafTipList.clear();
	}
	
	private ArrayList<LinkedList<LeafTip>> readCsv(String pathInput) throws IOException {
		String path = System.getProperty("user.home") + pathInput;
		String cam = "\\.VIS";// "\\.FLUO";
		path = path.replace("\\", "/");
		ArrayList<LinkedList<LeafTip>> out = new ArrayList<LinkedList<LeafTip>>();
		File f = new File(path);
		
		try {
			java.io.BufferedReader FileReader = new java.io.BufferedReader(new java.io.FileReader(f));
			String line = "";
			String[] head = null;
			
			while (null != (line = FileReader.readLine())) {
				String[] split = line.split(";");
				// if split empty, than break up
				if (split.length == 0)
					break;
				
				String temp = "start";
				int idx = 0;
				LinkedList<LeafTip> tipListforOneSnapshot = new LinkedList<LeafTip>();
				
				// save head
				if (head == null) {
					head = split;
					continue;
				}
				
				// time, leaf_number, position x/y
				long time = -1;
				double xPos = -1.0;
				double yPos = -1.0;
				int numberOfLt = -1;
				
				while (idx != split.length) {
					temp = split[idx];
					// if (temp.isEmpty())
					// continue;
					
					LeafTip lt = null;
					
					if (head != null) {
						String temp_head = head[idx];
						if (temp_head.equals("Day (Float)")) {
							System.out.println("Day (Float): " + temp);
							time = (long) (Float.valueOf(temp) * 24 * 60 * 60 * 1000);
						}
						if (Pattern.matches(".*side" + cam + "\\.leaf\\.\\d+\\.position.*", temp_head)) {
							// System.out.println(temp_head);
							// get current number of leaf-tip
							Pattern p = Pattern.compile("\\d+");
							Matcher m = p.matcher(temp_head);
							String temp_num = "";
							
							while (m.find()) {
								temp_num += temp_head.substring(m.start(), m.end());
								numberOfLt = Integer.valueOf(temp_num);
							}
							
							numberOfLt = Integer.valueOf(temp_num);
							// System.out.println(numberOfLt);
							
							String temp_x = "";
							String temp_y = "";
							
							if (Pattern.matches(".*" + temp_num + ".*\\.position\\.x.*", temp_head)) {
								temp_x = temp;
								if (!temp_x.equals("EMPTY") && !temp_x.equals(""))
									xPos = Double.valueOf(temp_x);
							}
							if (Pattern.matches(".*" + temp_num + ".*\\.position\\.y.*", temp_head)) {
								temp_y = temp;
								if (!temp_y.equals("EMPTY") && !temp_y.equals(""))
									yPos = Double.valueOf(temp_y);
							}
						}
					}
					if (time != -1.0 && xPos != -1.0 && yPos != -1.0 && numberOfLt != -1) {
						lt = new LeafTip(time, (int) xPos, (int) yPos);
						tipListforOneSnapshot.add(lt);
						xPos = -1.0;
						yPos = -1.0;
						numberOfLt = -1;
					}
					idx++;
				}
				out.add(tipListforOneSnapshot);
			}
			FileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;
	}
	
	private ArrayList<LinkedList<LeafTip>> convert(Collection<LinkedList<BorderFeature>> list, Normalisation norm) {
		ArrayList<LinkedList<LeafTip>> tiplistForEachDay = new ArrayList<LinkedList<LeafTip>>();
		LinkedList<LeafTip> tiplist;
		long time = 0;
		for (LinkedList<BorderFeature> l : list) {
			tiplist = new LinkedList<LeafTip>();
			if (l != null) {
				time += 24 * 60 * 60 * 1000;
				for (BorderFeature p : l) {
					if (p != null) {
						Vector2D pos = p.getPosition();
						tiplist.add(new LeafTip(time, pos, p.getFeatureMap(), norm));
					}
				}
			}
			tiplistForEachDay.add(tiplist);
		}
		return tiplistForEachDay;
	}
	
	private ArrayList<LinkedList<LeafTip>> convert(LinkedList<BorderFeature> leafTipListForOneDay,
			long timepoint, Normalisation norm) {
		LinkedList<LeafTip> ll = new LinkedList<LeafTip>();
		for (BorderFeature bf : leafTipListForOneDay) {
			ll.add(new LeafTip(timepoint, ((Integer) bf.getFeature("x")).doubleValue(), ((Integer) bf.getFeature("y")).doubleValue(), bf.getFeatureMap(), norm));
		}
		ArrayList<LinkedList<LeafTip>> al = new ArrayList<LinkedList<LeafTip>>();
		al.add(ll);
		return al;
	}
	
	public Plant getMatchedPlant() {
		return matchedPlant;
	}
	
	public void setMaxDistanceBetweenLeafTips(double val) {
		maxDistanceBetweenLeafTips = val;
	}
	
	public enum Vismode {
		PERLEAF, PERDAY
	}
}
