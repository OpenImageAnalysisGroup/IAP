/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author entzian
 * 
 */
public class PixelSegmentation {

	private final int[][] src_image;

	/**
	 * True = 8-neighbourhood, False = 4-neighbourhood.
	 */
	private final boolean nb; // true = 8er; false = 4er

	/**
	 * Specifies the first pixel value which is treated as foreground, values
	 * below are treated as background.
	 */
	private final int foreground = 1;
	private int zaehler = foreground;
	private final HashMap<Integer, ArrayList<Integer>> clusterMapping = new HashMap<Integer, ArrayList<Integer>>();

	private int[] image_cluster_size;
	private final int[][] image_cluster_ids;
	private int[] cluster_border_size;

	/**
	 * Circuit ratio lambda = (A/(U*U))*4*Pi
	 */
	private double[] cluster_lambda;

	private final int[][] perimeterMask = new int[][] { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } };

	private int[][] tableLinks;
	private int[] clusterMap;
	
	
	public PixelSegmentation(int[][] image) {
		this(image, NeighbourhoodSetting.NB4);
	}

	public PixelSegmentation(int[][] image, NeighbourhoodSetting setting) {
		src_image = image;
		this.image_cluster_ids = new int[image.length][image[0].length];
		switch (setting) {
		case NB4:
			nb = false;
			break;
		case NB8:
			nb = true;
			break;
		default:
			nb = false;
		}
	}

	// ############### Public ####################

	public int[] getClusterCounts() {
		return image_cluster_size;
	}

	public int[][] getImageMask() {
		return image_cluster_ids;
	}

	public int getNumberOfCluster() {
		int clusterNumbers = 0;
		for (int pixelIndex = 1; pixelIndex < image_cluster_size.length; pixelIndex++)
			if (image_cluster_size[pixelIndex] != 0)
				clusterNumbers++;
		return clusterNumbers;
	}

	public int getNumberOfPixel() {
		int pixelNumbers = 0;
		for (int pixelIndex = 1; pixelIndex < image_cluster_size.length; pixelIndex++)
			pixelNumbers = pixelNumbers + image_cluster_size[pixelIndex];
		return pixelNumbers;
	}

	public int[] getArea() {
		return image_cluster_size;
	}

	public int getArea(int position) {
		return image_cluster_size[position];
	}

	public int[] getPerimeter() {
		return cluster_border_size;
	}

	public int getPerimeter(int position) {
		return cluster_border_size[position];
	}

	public double[] getCircuitRatio() {
		return cluster_lambda;
	}

	public double getCircuitRatio(int position) {
		return cluster_lambda[position];
	}

	public void doPixelSegmentation() {
		firstPass(); // Each pixel is assigned to a cluster
		mergeHashMap();
		secondPass(); // Cluster are renumbered
		calculatePerimeterOfEachCluster();
		calculateCircuitRatio();
	}

	private void calculateCircuitRatio() {
		cluster_lambda = new double[zaehler];

		for (int i = 0; i < zaehler; i++)
			if (cluster_border_size[i] > 0) {
				cluster_lambda[i] = (double) image_cluster_size[i] / (double) cluster_border_size[i]
						/ cluster_border_size[i] * 4 * Math.PI;
			}

	}

	// ############### Print-Methoden ######################

	public void printOriginalImage() {
		printOriginalImage(this.src_image);
	}

	private void printOriginalImage(int[][] original_image) {
		printImage(original_image, "OriginalImage");
	}

	public void printArray(int[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.println(array[i]);
		}
	}

	public void printArray(double[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.println(array[i]);
		}
	}

	public void printImage() {
		printImage(this.image_cluster_ids);
	}

	public void printImage(int[][] image) {
		printImage(image, "ClusterImage");
	}

	public void printImage(int[][] image, String text) {
		System.out.println(text);
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}

	public void printHashMap() {
		printHashMap(this.clusterMapping);
	}

	public void printHashMap(HashMap<Integer, ArrayList<Integer>> hashM) {

		if (!hashM.isEmpty())
			for (int clusterID : hashM.keySet())
				System.out.println("To cluster " + clusterID + " belongs Cluster: " + hashM.get(clusterID));
		else
			System.out.println("No cluster has to be merge!");
	}

	public void printClusterArray() {
		printClusterArray(this.image_cluster_size);
	}

	public void printClusterArray(int[] zaehlerArray2) {
		if (zaehlerArray2.length > foreground)
			for (int arrayID = foreground; arrayID < zaehlerArray2.length; arrayID++)
				System.out.println("Cluster " + arrayID + " contains " + zaehlerArray2[arrayID] + " pixel");
		else
			System.out.println("No cluster available!");
	}

	// ############# Private #################

	private void firstPass() {
		for (int i = 0; i < src_image.length; i++) {
			for (int j = 0; j < src_image[i].length; j++) {
				if (src_image[i][j] == 1) {
					if (i == 0 && j == 0)
						parse(Position.FIRST_FIELD);
					else if (i == 0)
						parse(Position.FIRST_ROW, 0, j);
					else if (j == 0)
						parse(Position.FIRST_COLUMN, i);
					else if (j == src_image[i].length - 1 && nb)
						parse(Position.LAST_COLUMN, i, j);
					else
						parse(Position.REMAINING, i, j);
				}
			}
		}
	}

	private void secondPass() {
//		int[] clusterMap = new int[zaehler];
//		image_cluster_size = new int[zaehler];
//		for (int i = 0; i < zaehler; i++)
//			clusterMap[i] = i;
//
//		if (!clusterMapping.isEmpty())
//			for (int clusterID : clusterMapping.keySet())
//				for (int arrayID : clusterMapping.get(clusterID))
//					clusterMap[arrayID] = clusterID;

		image_cluster_size = new int[zaehler];
		
		for (int i = 0; i < src_image.length; i++)
			for (int j = 0; j < src_image[i].length; j++) {
				image_cluster_ids[i][j] = clusterMap[image_cluster_ids[i][j]];
				image_cluster_size[image_cluster_ids[i][j]]++;
			}
	}

	private void calculatePerimeterOfEachCluster() {

		cluster_border_size = new int[zaehler];

		for (int i = 0; i < src_image.length; i++) {
			for (int j = 0; j < src_image[i].length; j++) {
				if (!(src_image[i][j] < foreground)) {
					controlEdges(i, j);
				}
			}
		}
	}

	private void controlEdges(int currentPositionI, int currentPositionJ) {

		for (int l = 0; l < perimeterMask.length; l++) {
			for (int k = 0; k < perimeterMask[l].length; k++) {
				if (perimeterMask[l][k] == 1) {
					if (currentPositionI - 1 + l >= 0 && currentPositionJ - 1 + k >= 0
							&& currentPositionI - 1 + l <= src_image.length - 1
							&& currentPositionJ - 1 + k <= src_image[currentPositionI].length - 1) {
						if (image_cluster_ids[currentPositionI - 1 + l][currentPositionJ - 1 + k] != image_cluster_ids[currentPositionI][currentPositionJ]) {
							cluster_border_size[image_cluster_ids[currentPositionI][currentPositionJ]]++;

						}
					} else {
						cluster_border_size[image_cluster_ids[currentPositionI][currentPositionJ]]++;
					}
				}
			}
		}

	}

	private void parse(Position zahl) {
		parse(zahl, 0, 0);
	}

	private void parse(Position zahl, int i) {
		parse(zahl, i, 0);
	}

	private void parse(Position position, int i, int j) {
		int pixelTR, pixelT, pixelTL, pixelL;

		switch (position) { // Ecke links oben und rechts oben
		case FIRST_FIELD:
			image_cluster_ids[i][j] = zaehler;
			zaehler++;

			break;

		// erste Zeile oben, nicht die Ecke links aber die Ecke rechts
		case FIRST_ROW:
			pixelL = image_cluster_ids[i][j - 1];

			if (pixelL < foreground) {
				image_cluster_ids[i][j] = zaehler;
				zaehler++;
			} else
				image_cluster_ids[i][j] = pixelL;

			break;

		// erste Spalte links, nicht die Ecke oben
		case FIRST_COLUMN:
			if (!nb) { // 4er
				pixelT = image_cluster_ids[i - 1][j];

				if (pixelT < foreground) {
					image_cluster_ids[i][j] = zaehler;
					zaehler++;
				} else
					image_cluster_ids[i][j] = pixelT;
			} else { // 8er
				pixelTR = image_cluster_ids[i - 1][j + 1];
				pixelT = image_cluster_ids[i - 1][j];

				if (pixelT < foreground && pixelTR < foreground) {
					image_cluster_ids[i][j] = zaehler;
					zaehler++;

				} else if (pixelT < foreground && pixelTR > foreground - 1) {
					image_cluster_ids[i][j] = pixelTR;

				} else if (pixelT > foreground - 1 && pixelTR < foreground) {
					image_cluster_ids[i][j] = pixelT;

				} else {
					image_cluster_ids[i][j] = pixelT;

					// hashMapFuellen(image[i-1][j], image[i-1][j+1]);
				}
			}

			break;

		// alles bis auf den linken, rechten (bei 8er Nachbarschaft) und oberen
		// Rand
		case REMAINING:
			if (!nb) { // 4er

				pixelT = image_cluster_ids[i - 1][j];
				pixelL = image_cluster_ids[i][j - 1];

				if (pixelT < foreground) {
					if(pixelL < foreground){
						image_cluster_ids[i][j] = zaehler;
						zaehler++;
					} else {
						image_cluster_ids[i][j] = pixelL;
					}
				} else {
					if (pixelL < foreground) {
						image_cluster_ids[i][j] = pixelT;
					} else {
					image_cluster_ids[i][j] = pixelL;
					addHashMapEntry(pixelT, pixelL);
					}
				}
			} else { // 8er

				pixelTR = image_cluster_ids[i - 1][j + 1];
				pixelT = image_cluster_ids[i - 1][j];
				pixelTL = image_cluster_ids[i - 1][j - 1];
				pixelL = image_cluster_ids[i][j - 1];

				if (pixelTR < foreground) {
					if (pixelT < foreground) {
						if (pixelTL < foreground) {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = zaehler;
								zaehler++;
							} else {
								image_cluster_ids[i][j] = pixelL;
							}
						} else {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelTL;
							} else {
								image_cluster_ids[i][j] = pixelL;
							}
						}
					} else {
						if (pixelTL < foreground) {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelT;
							} else {
								image_cluster_ids[i][j] = pixelL;
								addHashMapEntry(pixelT, pixelL);
							}
						} else {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelTL;
							} else {
								image_cluster_ids[i][j] = pixelL;
							}
						}
					}
				} else {
					if (pixelT < foreground) {
						if (pixelTL < foreground) {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelTR;
							} else {
								image_cluster_ids[i][j] = pixelL;
								addHashMapEntry(pixelTR, pixelL);
							}
						} else {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelTL;
								addHashMapEntry(pixelTR, pixelTL);
							} else {
								image_cluster_ids[i][j] = pixelL;
								addHashMapEntry(pixelTR, pixelL);
							}
						}
					} else {
						if (pixelTL < foreground) {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelT;
							} else {
								image_cluster_ids[i][j] = pixelL;
								addHashMapEntry(pixelT, pixelL);
							}
						} else {
							if (pixelL < foreground) {
								image_cluster_ids[i][j] = pixelTL;
							} else {
								image_cluster_ids[i][j] = pixelL;
							}
						}
					}
				}
			}

			break;

		// letzte Spalte rechts, nicht die Ecke oben
		case LAST_COLUMN:
			pixelT = image_cluster_ids[i - 1][j];
			pixelTL = image_cluster_ids[i - 1][j - 1];
			pixelL = image_cluster_ids[i][j - 1];

			if (pixelT < foreground) {
				if (pixelTL < foreground) {
					if (pixelL < foreground) {
						image_cluster_ids[i][j] = zaehler;
						zaehler++;
					} else {
						image_cluster_ids[i][j] = pixelL;
					}
				} else {
					if (pixelL < foreground) {
						image_cluster_ids[i][j] = pixelTL;
					} else {
						image_cluster_ids[i][j] = pixelL;
					}
				}
			} else {
				if (pixelTL < foreground) {
					if (pixelL < foreground) {
						image_cluster_ids[i][j] = pixelT;
					} else {
						image_cluster_ids[i][j] = pixelL;
						addHashMapEntry(pixelT, pixelL);
					}
				} else {
					if (pixelL < foreground) {
						image_cluster_ids[i][j] = pixelTL;
					} else {
						image_cluster_ids[i][j] = pixelL;
					}
				}
			}
			break;
		}

	}

	private void addHashMapEntry(int Pixel1, int Pixel2) {
		if (Pixel1 != Pixel2) {
			if (!clusterMapping.containsKey(Pixel2)) {
				clusterMapping.put(Pixel2, new ArrayList<Integer>());
				clusterMapping.get(Pixel2).add(Pixel1);
			} else if (!clusterMapping.get(Pixel2).contains(Pixel1)) {
				clusterMapping.get(Pixel2).add(Pixel1);
			}
		}
	}

	private void mergeHashMap(){
		
		tableLinks = new int[zaehler][zaehler];
		clusterMap = new int[zaehler];
		
		//initilisierung der tableLinks entsprechen den Zuweisungen
		for(int key : clusterMapping.keySet()){
			for(int value : clusterMapping.get(key)){
				tableLinks[key][value] = -1;
				tableLinks[value][key] = -1;
			}
		}
		
		for (int i = 0; i < zaehler; i++)
			clusterMap[i] = i;

//		if (!clusterMapping.isEmpty())
//			for (int clusterID : clusterMapping.keySet())
//				for (int arrayID : clusterMapping.get(clusterID))
//					clusterMap[arrayID] = clusterID;
		
		System.out.println("HashMap");
		printHashMap();
		printImage(tableLinks, "TableLinks");
		
		System.out.println("(3,2): " + tableLinks[3][2]);
		System.out.println("(2,1): " + tableLinks[2][1]);
		
		recursiveMerge(1,1, zaehler, 0,0);
		
	}
	
	private void recursiveMerge(int zeile, int spalte, int zaehlerJ, int missachten, int aktuellerCluster) {
		
		for(int j = zeile; j < zaehlerJ; j++)
			for(int i = spalte; i < zaehler; i++)
				if(!(i == missachten)){
					if(tableLinks[i][j] == -1){
						if(missachten == 0)
							aktuellerCluster = 0;
						
						tableLinks[i][j] = -2;
						if(aktuellerCluster == 0){
							clusterMap[i] = j;
							aktuellerCluster = j;
						} else {
							clusterMap[i] = aktuellerCluster;
						}
						
						if(!(i == zaehler))
							recursiveMerge(i,1,i+1,j, aktuellerCluster);
					}
				} else {
					tableLinks[i][j] = -2;
				}
		
	}
	
	
//	private void mergeHashMap() {
//
//		ArrayList<HashSet<Integer>> toepfe = new ArrayList<HashSet<Integer>>(clusterMapping.size());
//		for (int key : clusterMapping.keySet()) {
//			HashSet<Integer> topf = new HashSet<Integer>();
//			topf.add(key);
//			toepfe.add(topf);
//			for (int value : clusterMapping.get(key)) {
//				topf.add(value);
//			}
//		}
//
//		// toepfe ineinander
//
//		for (int a = 0; a < toepfe.size(); a++) {
//			HashSet<Integer> topfA = toepfe.get(a);
//			if (!topfA.isEmpty())
//				for (int b = a; b < toepfe.size(); b++) {
//					HashSet<Integer> topfB = toepfe.get(b);
//					if (topfA != topfB && !topfB.isEmpty()) {
//						boolean foundInTopfB = false;
//						for (Integer inTopfA : topfA) {
//							if (topfB.contains(inTopfA)) {
//								foundInTopfB = true;
//								break;
//							}
//						}
//						if (foundInTopfB) {
//							// schuette in topfB
//							topfB.addAll(topfA);
//							topfA.clear();
//						}
//					}
//				}
//		}
//
//		clusterMapping.clear();
//		for (HashSet<Integer> topf : toepfe) {
//			if (topf.isEmpty())
//				continue;
//			Integer key = topf.iterator().next();
//			topf.remove(key);
//			clusterMapping.put(key, new ArrayList<Integer>(topf));
//		}
//	}

	

	private ArrayList<Integer> getKeyByValue2(HashMap<Integer, ArrayList<Integer>> hashM, Integer value) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer i : hashM.keySet())
			if (hashM.get(i).contains(value))
				list.add(i);

		return list;
	}
	
	
	 public static void main(String[] args) {
//		 int[][] eingabe_image = { { 0, 1, 1, 0, 1, 0, 1, 0 },
//				 				   { 1, 1, 0, 0, 1, 1, 1, 0 }, 
//				 				   { 0, 1, 1, 1, 1, 0, 1, 0 },
//				 				   { 0, 0, 0, 0, 0, 1, 1, 0 }, 
//				 				   { 0, 1, 1, 1, 0, 0, 0, 1 }, 
//				 				   { 1, 1, 1, 1, 1, 0, 0, 0 } };
		 
		 int[][] eingabe_image = { { 0, 1, 1, 0, 1, 1, 1 },
				 				   { 1, 1, 1, 1, 1, 0, 1 }, 
								   { 0, 0, 0, 0, 0, 0, 1 },
								   { 0, 0, 0, 0, 0, 1, 1 },
								   { 0, 1, 0, 0, 0, 0, 0 }, 
								   { 1, 1, 0, 0, 0, 0, 0 } };
		 
		 PixelSegmentation test = new PixelSegmentation(eingabe_image, NeighbourhoodSetting.NB4);
		 test.doPixelSegmentation();
		 test.printOriginalImage();
		 System.out.println("ClusterIds:");
		 test.printImage();
		 test.printClusterArray();
		 System.out.println("Number of Clusters: " +
		 test.getNumberOfCluster());
		 System.out.println("Number of Pixel: " + test.getNumberOfPixel());
//		 System.out.println("Area:");
//		 test.printArray(test.getArea());
//		 System.out.println("Perimeter: ");
//		 test.printArray(test.getPerimeter());
//		 System.out.println("Ratio: ");
//		 test.printArray(test.getCircuitRatio());
	 }
}
