/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.operations.segmentation;

import info.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.Vector2d;

import de.ipk.ag_ba.image.operations.Position;

/**
 * @author entzian
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
	
	private int[] cluster_min_x;
	private int[] cluster_max_x;
	private int[] cluster_min_y;
	private int[] cluster_max_y;
	
	/**
	 * Circuit ratio lambda = (A/(U*U))*4*Pi
	 */
	private double[] cluster_lambda;
	
	private final int[][] perimeterMask = new int[][] { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } };
	
	private int[][] tableLinks;
	private int[] clusterMap;
	private boolean[] linesRun;
	
	private final boolean calculatePerimeterAndRatio;
	
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
		
		calculatePerimeterAndRatio = true;
	}
	
	// ############### Public ####################
	
	public int[] getClusterSize() {
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
	
	public void doPixelSegmentation(int durchlauf) {
		
		if (durchlauf == 3) {
			firstPass(); // Each pixel is assigned to a cluster
			StopWatch s = new StopWatch("Merge New");
			mergeHashMapDritteVariante();
			s.printTime();
			// secondPass();
			secondPassDritteVariante(); // Cluster are renumbered
			if (calculatePerimeterAndRatio) {
				calculatePerimeterOfEachCluster();
				calculateCircuitRatio();
			}
			calculateClusterDimension();
		} else
			if (durchlauf == 2) {
				firstPass(); // Each pixel is assigned to a cluster
				
				StopWatch s = new StopWatch("Merge ALEX");
				mergeHashMapRecursive();
				
				s.printTime();
				secondPass(); // Cluster are renumbered
				
				if (calculatePerimeterAndRatio) {
					calculatePerimeterOfEachCluster();
					calculateCircuitRatio();
				}
				
				calculateClusterDimension();
			} else {
				firstPass(); // Each pixel is assigned to a cluster
				// StopWatch s = new StopWatch("Merge Chris");
				
				mergeHashMapToepfe();
				
				// s.printTime();
				secondPassToepfe(); // Cluster are renumbered
				
				if (calculatePerimeterAndRatio) {
					calculatePerimeterOfEachCluster();
					calculateCircuitRatio();
				}
				
				calculateClusterDimension();
			}
		
	}
	
	private void calculateClusterDimension() {
		
		cluster_min_x = new int[zaehler];
		cluster_max_x = new int[zaehler];
		cluster_min_y = new int[zaehler];
		cluster_max_y = new int[zaehler];
		
		Arrays.fill(cluster_min_x, src_image.length);
		Arrays.fill(cluster_min_y, src_image[0].length);
		Arrays.fill(cluster_max_x, 0);
		Arrays.fill(cluster_max_y, 0);
		
		for (int i = 0; i < src_image.length; i++) {
			for (int j = 0; j < src_image[i].length; j++) {
				if (!(src_image[i][j] < foreground)) {
					int clusterId = image_cluster_ids[i][j];
					
					int x = i;
					int y = j;
					
					if (x < cluster_min_x[clusterId])
						cluster_min_x[clusterId] = x;
					if (y < cluster_min_y[clusterId])
						cluster_min_y[clusterId] = y;
					
					if (x > cluster_max_x[clusterId])
						cluster_max_x[clusterId] = x;
					if (y > cluster_max_y[clusterId])
						cluster_max_y[clusterId] = y;
				}
			}
		}
		
		// System.out.println("Länge von cluster_min_x: " + cluster_min_x.length);
		// System.out.println("Länge von cluster_max_x: " + cluster_max_x.length);
		// System.out.println("Länge von cluster_min_y: " + cluster_min_y.length);
		// System.out.println("Länge von cluster_max_y: " + cluster_max_y.length);
		
		// for (int i = 0; i <= cluster_min_x.length; i++)
		// System.out.println("Cluster: " + i + " min_x: " + cluster_min_x[i] + " min_y: " + cluster_min_y[i] + " max_x: " + cluster_max_x[i] + " max_y: "
		// + cluster_max_y[i]);
		
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
		printClusterArray(image_cluster_size);
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
					else
						if (i == 0)
							parse(Position.FIRST_ROW, 0, j);
						else
							if (j == 0)
								parse(Position.FIRST_COLUMN, i);
							else
								if (j == src_image[i].length - 1 && nb)
									parse(Position.LAST_COLUMN, i, j);
								else
									parse(Position.REMAINING, i, j);
				}
			}
		}
	}
	
	private void secondPass() {
		
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
						
					} else
						if (pixelT < foreground && pixelTR > foreground - 1) {
							image_cluster_ids[i][j] = pixelTR;
							
						} else
							if (pixelT > foreground - 1 && pixelTR < foreground) {
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
						if (pixelL < foreground) {
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
			} else
				if (!clusterMapping.get(Pixel2).contains(Pixel1)) {
					clusterMapping.get(Pixel2).add(Pixel1);
				}
		}
	}
	
	private void mergeHashMapRecursive() {
		
		tableLinks = new int[zaehler][zaehler];
		clusterMap = new int[zaehler];
		linesRun = new boolean[zaehler];
		
		for (int key : clusterMapping.keySet()) {
			for (int value : clusterMapping.get(key)) {
				tableLinks[key][value] = -1;
				tableLinks[value][key] = -1;
			}
		}
		
		for (int i = 0; i < zaehler; i++) {
			clusterMap[i] = i;
			linesRun[i] = true;
		}
		// long ersteZeit = System.currentTimeMillis();
		recursiveMerge(1, 1, zaehler, 0, 0);
		// long zweiteZeit = System.currentTimeMillis();
		// System.out.println("Dauer: " + (zweiteZeit - ersteZeit));
	}
	
	private void recursiveMerge(int zeile, int spalte, int zaehlerJ, int missachten, int aktuellerCluster) {
		for (int j = zeile; j < zaehlerJ; j++) {
			for (int i = spalte; i < zaehler && linesRun[j]; i++) {
				// for(int i = spalte; i < zaehler; i++){
				if (i != missachten) {
					if (tableLinks[i][j] == -1) {
						if (missachten == 0)
							aktuellerCluster = 0;
						
						tableLinks[i][j] = -2;
						if (aktuellerCluster == 0) {
							clusterMap[i] = j;
							aktuellerCluster = j;
						} else
							clusterMap[i] = aktuellerCluster;
						
						if (i != zaehler)
							recursiveMerge(i, 1, i + 1, j, aktuellerCluster);
					}
				} else
					tableLinks[i][j] = -2;
			}
			linesRun[j] = false;
		}
	}
	
	private ArrayList<Integer> getKeyByValue2(HashMap<Integer, ArrayList<Integer>> hashM, Integer value) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer i : hashM.keySet())
			if (hashM.get(i).contains(value))
				list.add(i);
		
		return list;
	}
	
	public static void main(String[] args) throws IOException, Exception {
		
		// int [][] eingabe_image = new int [2000][2000];
		// for(int i = 0; i<2000; i++)
		// for(int j = 0; j<2000; j++)
		// eingabe_image[i][j] = i;
		
		// int[][] eingabe_image = { { 0, 1, 1, 0, 1, 0, 1, 0 },
		// { 1, 1, 0, 0, 1, 1, 1, 0 },
		// { 0, 1, 1, 1, 1, 0, 1, 0 },
		// { 0, 0, 0, 0, 0, 1, 1, 0 },
		// { 0, 1, 1, 1, 0, 0, 0, 1 },
		// { 1, 1, 1, 1, 1, 0, 0, 0 } };
		
		// IOurl testURL = new IOurl("file:///Users/entzian/Desktop/test.png");
		// BufferedImage testBI = ImageIO.read(testURL.getInputStream());
		// int[][] testArray = ImageConverter.convertBIto2A(testBI);
		//
		// for (int i = 0; i < testArray.length; i++)
		// for (int j = 0; j < testArray[0].length; j++)
		// if (testArray[i][j] == Color.WHITE.getRGB())
		// testArray[i][j] = 0;
		// else
		// testArray[i][j] = 1;
		
		int[][] eingabe_image = { { 0, 1, 1, 0, 1, 1, 1 },
											{ 1, 1, 1, 1, 1, 0, 1 },
											{ 0, 0, 0, 0, 0, 0, 1 },
											{ 0, 0, 0, 0, 0, 1, 1 },
											{ 0, 1, 0, 0, 0, 0, 0 },
											{ 1, 1, 0, 0, 0, 0, 0 } };
		//
		PixelSegmentation test = new PixelSegmentation(eingabe_image, NeighbourhoodSetting.NB4);
		// PixelSegmentation test = new PixelSegmentation(testArray, NeighbourhoodSetting.NB4);
		test.doPixelSegmentation(3);
		// PrintImage.printImage(ImageConverter.convert2ABto2AcolorFull(test.getImageMask()));
		test.printOriginalImage();
		System.out.println("ClusterIds:");
		test.printImage();
		test.printClusterArray();
		System.out.println("Number of Clusters: " +
							test.getNumberOfCluster());
		System.out.println("Number of Pixel: " + test.getNumberOfPixel());
		// System.out.println("Area:");
		// test.printArray(test.getArea());
		// System.out.println("Perimeter: ");
		// test.printArray(test.getPerimeter());
		// System.out.println("Ratio: ");
		// test.printArray(test.getCircuitRatio());
	}
	
	private void mergeHashMapDritteVariante() {
		
		int[] fuellGrad = new int[zaehler]; // enspricht x
		clusterMap = new int[zaehler]; // entspricht y
		tableLinks = new int[zaehler][zaehler];// entspricht z
		
		// for (int key : clusterMapping.keySet()) {
		// for (int value : clusterMapping.get(key)) {
		// tableLinks[key][value] = -1;
		// tableLinks[value][key] = -1;
		// }
		// }
		
		for (int i = 0; i < zaehler; i++) {
			clusterMap[i] = -1; // überall wo -1 steht wird dann der Index als Cluster gesetzt
		}
		
		for (int key : clusterMapping.keySet()) {
			
			if (clusterMap[key] != -1) {
				
				fuellGrad = anpassen(key, key, fuellGrad);
				
			} else {
				clusterMap[key] = key;
			}
			
			for (int value : clusterMapping.get(key)) {
				
				tableLinks[key][fuellGrad[key]] = value;
				fuellGrad[key] += 1;
				
				if (clusterMap[value] != -1) {
					fuellGrad = anpassen(value, key, fuellGrad);
				} else {
					clusterMap[value] = key;
					tableLinks[value][fuellGrad[value]] = key;
					fuellGrad[value] += 1;
				}
				for (int value2 : clusterMapping.get(key))
					if (value != value2) {
						// System.out.println("value -> value2: " + value + " -> " + value2);
						tableLinks[value][fuellGrad[value]] = value2;
						fuellGrad[value] += 1;
					}
			}
		}
		
	}
	
	private int[] anpassen(int value, int key, int[] fuellGrad) {
		
		for (int i = 0; i < fuellGrad[value]; i++) {
			
			clusterMap[tableLinks[value][i]] = key;
			tableLinks[tableLinks[value][i]][fuellGrad[tableLinks[value][i]]] = key;
			fuellGrad[tableLinks[value][i]] += 1;
		}
		
		clusterMap[value] = key;
		tableLinks[value][fuellGrad[value]] = key;
		fuellGrad[value] += 1;
		
		// System.out.println("drausen");
		// System.out.println("FuellGrad");
		// printArray(fuellGrad);
		// System.out.println("clusterMap");
		// printArray(clusterMap);
		// System.out.println("TableLinks");
		// printImage(tableLinks);
		
		return fuellGrad;
	}
	
	private void secondPassDritteVariante() {
		
		image_cluster_size = new int[zaehler];
		
		for (int i = 0; i < src_image.length; i++)
			for (int j = 0; j < src_image[i].length; j++) {
				if (clusterMap[image_cluster_ids[i][j]] != -1)
					image_cluster_ids[i][j] = clusterMap[image_cluster_ids[i][j]];
				
				image_cluster_size[image_cluster_ids[i][j]]++;
			}
	}
	
	private void secondPassToepfe() {
		int[] clusterMap = new int[zaehler];
		image_cluster_size = new int[zaehler];
		for (int i = 0; i < zaehler; i++)
			clusterMap[i] = i;
		
		if (!clusterMapping.isEmpty())
			for (int clusterID : clusterMapping.keySet())
				for (int arrayID : clusterMapping.get(clusterID))
					clusterMap[arrayID] = clusterID;
		
		for (int i = 0; i < src_image.length; i++)
			for (int j = 0; j < src_image[i].length; j++) {
				image_cluster_ids[i][j] = clusterMap[image_cluster_ids[i][j]];
				image_cluster_size[image_cluster_ids[i][j]]++;
			}
	}
	
	private void mergeHashMapToepfe() {
		
		ArrayList<HashSet<Integer>> toepfe = new ArrayList<HashSet<Integer>>(clusterMapping.size());
		for (int key : clusterMapping.keySet()) {
			HashSet<Integer> topf = new HashSet<Integer>();
			topf.add(key);
			toepfe.add(topf);
			for (int value : clusterMapping.get(key)) {
				topf.add(value);
			}
		}
		
		// toepfe ineinander
		
		for (int a = 0; a < toepfe.size(); a++) {
			HashSet<Integer> topfA = toepfe.get(a);
			if (!topfA.isEmpty())
				for (int b = a; b < toepfe.size(); b++) {
					HashSet<Integer> topfB = toepfe.get(b);
					if (topfA != topfB && !topfB.isEmpty()) {
						boolean foundInTopfB = false;
						for (Integer inTopfA : topfA) {
							if (topfB.contains(inTopfA)) {
								foundInTopfB = true;
								break;
							}
						}
						if (foundInTopfB) {
							// schuette in topfB
							topfB.addAll(topfA);
							topfA.clear();
						}
					}
				}
		}
		
		clusterMapping.clear();
		for (HashSet<Integer> topf : toepfe) {
			if (topf.isEmpty())
				continue;
			Integer key = topf.iterator().next();
			topf.remove(key);
			clusterMapping.put(key, new ArrayList<Integer>(topf));
		}
	}
	
	public Vector2d[] getClusterCenterPoints() {
		
		Vector2d[] res = new Vector2d[cluster_min_x.length];
		for (int i = 0; i < cluster_min_x.length; i++) {
			int w = cluster_max_x[i] - cluster_min_x[i];
			int h = cluster_max_y[i] - cluster_min_y[i];
			
			int cx = cluster_min_x[i] + w / 2;
			int cy = cluster_min_y[i] + h / 2;
			
			res[i] = new Vector2d(cx, cy);
			
			// System.out.println("Center of Cluster = " + i + " Point X = " + cx + " Y = " + cy);
		}
		
		return res;
	}
	
	public int[] getClusterSizeNormalized(int w, int h) {
		
		Vector2d[] clusterCenters = getClusterCenterPoints();
		int[] normalizedClusterAreaSizes = new int[getClusterSize().length];
		int[] clusterAreaSizes = getClusterSize();
		
		// maximum acceptible circle has size of half of circle around the complete image
		double distanceFromCenterToLeftTopEdge = new Vector2d(0, 0).distance(w / 2d, h / 2d) / 2;
		
		if (true)
			for (int cluster = 1; cluster < clusterAreaSizes.length; cluster++) {
				Vector2d centerOfCluster = clusterCenters[cluster];
				
				double distanceFromCenterOfClusterToCenterOfImage = centerOfCluster.distance(w / 2.5d, h / 2.5d);
				
				double d = distanceFromCenterToLeftTopEdge - distanceFromCenterOfClusterToCenterOfImage;
				
				// if outside of maximum acceptable circle
				if (d < 0)
					d = 0;
				
				normalizedClusterAreaSizes[cluster] = (int) (clusterAreaSizes[cluster] *
						(d / distanceFromCenterToLeftTopEdge));
				
				// System.out.println("cluster = " + cluster + " newPixel = " + output[cluster] + " oldPixel = " + input[cluster] + " Abstand zum Zentrum: " +
				// distanceFromCenterToCluster);
			}
		
		if (false)
			for (int cluster = 1; cluster < clusterAreaSizes.length; cluster++) {
				Vector2d center = clusterCenters[cluster];
				
				double distanceFromCenterToCluster = clusterCenters[0].distance(center);
				
				distanceFromCenterToLeftTopEdge = clusterCenters[0].distance(new Vector2d(0, 0));
				
				double d = ((distanceFromCenterToLeftTopEdge - distanceFromCenterToCluster) < 0) ? 0
						: (distanceFromCenterToLeftTopEdge - distanceFromCenterToCluster);
				
				normalizedClusterAreaSizes[cluster] = (int) (clusterAreaSizes[cluster] * (d / distanceFromCenterToLeftTopEdge));
				
				// System.out.println("cluster = " + cluster + " newPixel = " + output[cluster] + " oldPixel = " + input[cluster] + " Abstand zum Zentrum: " +
				// distanceFromCenterToCluster);
			}
		
		return normalizedClusterAreaSizes;
	}
	
}
