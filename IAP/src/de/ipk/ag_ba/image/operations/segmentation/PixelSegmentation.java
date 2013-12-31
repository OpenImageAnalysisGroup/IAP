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
import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageConverter;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.Position;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author entzian
 */
@Deprecated
public class PixelSegmentation implements Segmentation {
	
	private final int[][] src_image;
	
	/**
	 * True = 8-neighbourhood, False = 4-neighbourhood.
	 */
	private final boolean neighbourhood8; // true = 8er; false = 4er
	
	/**
	 * Specifies the first pixel value which is treated as foreground, values
	 * below are treated as background.
	 */
	private final int foreground = 1;
	private int currentClusterId = foreground;
	private final HashMap<Integer, HashSet<Integer>> equalClusterIds = new HashMap<Integer, HashSet<Integer>>();
	
	private int[] imageClusterSize;
	private final int[][] image_cluster_ids;
	private int[] clusterBorderSize;
	
	private int[] clusterMinX;
	private int[] clusterMaxX;
	private int[] clusterMinY;
	private int[] clusterMaxY;
	
	/**
	 * Circuit ratio lambda = (A/(U*U))*4*Pi
	 */
	private double[] cluster_lambda;
	
	private final int[][] perimeterMask = new int[][] { { 0, 1, 0 }, { 1, 1, 1 }, { 0, 1, 0 } };
	
	private int[][] tableLinks;
	private int[] clusterMap;
	private boolean[] linesRun;
	
	private final boolean calculatePerimeterAndRatio;
	
	public PixelSegmentation(Image in, NeighbourhoodSetting setting) {
		int[] rgbArray = in.getAs1A();
		int w = in.getWidth();
		int h = in.getHeight();
		int[][] image = new int[w][h];
		int iBackgroundFill = ImageOperation.BACKGROUND_COLORint;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				if (color != iBackgroundFill) {
					image[x][y] = 1;
				} else {
					image[x][y] = 0;
				}
			}
		}
		
		src_image = image;
		this.image_cluster_ids = new int[image.length][image[0].length];
		switch (setting) {
			case NB4:
				neighbourhood8 = false;
				break;
			case NB8:
				neighbourhood8 = true;
				break;
			default:
				neighbourhood8 = false;
		}
		
		calculatePerimeterAndRatio = true;
	}
	
	// ############### Public ####################
	
	@Override
	public int[] getClusterSize() {
		return imageClusterSize;
	}
	
	public int[][] getImageMask() {
		return image_cluster_ids;
	}
	
	public int getNumberOfCluster() {
		int clusterNumbers = 0;
		for (int pixelIndex = 1; pixelIndex < imageClusterSize.length; pixelIndex++)
			if (imageClusterSize[pixelIndex] != 0)
				clusterNumbers++;
		return clusterNumbers;
	}
	
	public int getNumberOfPixel() {
		int pixelNumbers = 0;
		for (int pixelIndex = 1; pixelIndex < imageClusterSize.length; pixelIndex++)
			pixelNumbers = pixelNumbers + imageClusterSize[pixelIndex];
		return pixelNumbers;
	}
	
	public int[] getArea() {
		return imageClusterSize;
	}
	
	public int getArea(int position) {
		return imageClusterSize[position];
	}
	
	public int[] getPerimeter() {
		return clusterBorderSize;
	}
	
	public int getPerimeter(int position) {
		return clusterBorderSize[position];
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
		
		clusterMinX = new int[currentClusterId];
		clusterMaxX = new int[currentClusterId];
		clusterMinY = new int[currentClusterId];
		clusterMaxY = new int[currentClusterId];
		
		Arrays.fill(clusterMinX, src_image.length);
		Arrays.fill(clusterMinY, src_image[0].length);
		Arrays.fill(clusterMaxX, 0);
		Arrays.fill(clusterMaxY, 0);
		
		for (int x = 0; x < src_image.length; x++) {
			for (int y = 0; y < src_image[x].length; y++) {
				if (!(src_image[x][y] < foreground)) {
					int clusterId = image_cluster_ids[x][y];
					
					if (x < clusterMinX[clusterId])
						clusterMinX[clusterId] = x;
					if (y < clusterMinY[clusterId])
						clusterMinY[clusterId] = y;
					
					if (x > clusterMaxX[clusterId])
						clusterMaxX[clusterId] = x;
					if (y > clusterMaxY[clusterId])
						clusterMaxY[clusterId] = y;
				}
			}
		}
	}
	
	private void calculateCircuitRatio() {
		cluster_lambda = new double[currentClusterId];
		
		for (int i = 0; i < currentClusterId; i++)
			if (clusterBorderSize[i] > 0) {
				cluster_lambda[i] = (double) imageClusterSize[i] / (double) clusterBorderSize[i]
						/ clusterBorderSize[i] * 4 * Math.PI;
			}
		
	}
	
	// ############### Print-Methoden ######################
	
	@Override
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
		printHashMap(this.equalClusterIds);
	}
	
	public void printHashMap(HashMap<Integer, HashSet<Integer>> hashM) {
		
		if (!hashM.isEmpty())
			for (int clusterID : hashM.keySet())
				System.out.println("To cluster " + clusterID + " belongs Cluster: " + hashM.get(clusterID));
		else
			System.out.println("No cluster has to be merge!");
	}
	
	public void printClusterArray() {
		printClusterArray(imageClusterSize);
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
		int w = src_image.length;
		int h = src_image[0].length;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (src_image[x][y] == 1) {
					if (x == 0 && y == 0)
						inspectPixel(Position.TOP_LEFT_PIXEL, x, y);
					else
						if (x == 0)
							inspectPixel(Position.FIRST_COL, x, y);
						else
							if (y == 0)
								inspectPixel(Position.FIRST_ROW, x, y);
							else
								if (y == h - 1 && neighbourhood8)
									inspectPixel(Position.LAST_ROW, x, y);
								else
									inspectPixel(Position.INNER_REGION, x, y);
				}
			}
		}
	}
	
	private void secondPass() {
		
		imageClusterSize = new int[currentClusterId];
		
		for (int i = 0; i < src_image.length; i++)
			for (int j = 0; j < src_image[i].length; j++) {
				image_cluster_ids[i][j] = clusterMap[image_cluster_ids[i][j]];
				imageClusterSize[image_cluster_ids[i][j]]++;
			}
	}
	
	private void calculatePerimeterOfEachCluster() {
		
		clusterBorderSize = new int[currentClusterId];
		
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
							clusterBorderSize[image_cluster_ids[currentPositionI][currentPositionJ]]++;
							
						}
					} else {
						clusterBorderSize[image_cluster_ids[currentPositionI][currentPositionJ]]++;
					}
				}
			}
		}
		
	}
	
	private void inspectPixel(Position position, int x, int y) {
		int pixelTR, pixelT, pixelTL, pixelL;
		
		switch (position) { // Ecke links oben und rechts oben
			case TOP_LEFT_PIXEL:
				image_cluster_ids[x][y] = currentClusterId;
				currentClusterId++;
				
				break;
			
			// erste Zeile oben, nicht die Ecke links aber die Ecke rechts
			case FIRST_COL:
				pixelL = image_cluster_ids[x][y - 1];
				
				if (pixelL < foreground) {
					image_cluster_ids[x][y] = currentClusterId;
					currentClusterId++;
				} else
					image_cluster_ids[x][y] = pixelL;
				
				break;
			
			// erste Spalte links, nicht die Ecke oben
			case FIRST_ROW:
				if (!neighbourhood8) { // 4er
					pixelT = image_cluster_ids[x - 1][y];
					
					if (pixelT < foreground) {
						image_cluster_ids[x][y] = currentClusterId;
						currentClusterId++;
					} else
						image_cluster_ids[x][y] = pixelT;
				} else { // 8er
					pixelTR = image_cluster_ids[x - 1][y + 1];
					pixelT = image_cluster_ids[x - 1][y];
					
					if (pixelT < foreground && pixelTR < foreground) {
						image_cluster_ids[x][y] = currentClusterId;
						currentClusterId++;
						
					} else
						if (pixelT < foreground && pixelTR > foreground - 1) {
							image_cluster_ids[x][y] = pixelTR;
							
						} else
							if (pixelT > foreground - 1 && pixelTR < foreground) {
								image_cluster_ids[x][y] = pixelT;
								
							} else {
								image_cluster_ids[x][y] = pixelT;
								
								// hashMapFuellen(image[i-1][j], image[i-1][j+1]);
							}
				}
				
				break;
			
			// alles bis auf den linken, rechten (bei 8er Nachbarschaft) und oberen
			// Rand
			case INNER_REGION:
				if (!neighbourhood8) { // 4er
				
					pixelT = image_cluster_ids[x - 1][y];
					pixelL = image_cluster_ids[x][y - 1];
					
					if (pixelT < foreground) {
						if (pixelL < foreground) {
							image_cluster_ids[x][y] = currentClusterId;
							currentClusterId++;
						} else {
							image_cluster_ids[x][y] = pixelL;
						}
					} else {
						if (pixelL < foreground) {
							image_cluster_ids[x][y] = pixelT;
						} else {
							image_cluster_ids[x][y] = pixelL;
							memorizeEqualClusterIds(pixelT, pixelL);
						}
					}
				} else { // 8er
				
					pixelTR = image_cluster_ids[x - 1][y + 1];
					pixelT = image_cluster_ids[x - 1][y];
					pixelTL = image_cluster_ids[x - 1][y - 1];
					pixelL = image_cluster_ids[x][y - 1];
					
					if (pixelTR < foreground) {
						if (pixelT < foreground) {
							if (pixelTL < foreground) {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = currentClusterId;
									currentClusterId++;
								} else {
									image_cluster_ids[x][y] = pixelL;
								}
							} else {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelTL;
								} else {
									image_cluster_ids[x][y] = pixelL;
								}
							}
						} else {
							if (pixelTL < foreground) {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelT;
								} else {
									image_cluster_ids[x][y] = pixelL;
									memorizeEqualClusterIds(pixelT, pixelL);
								}
							} else {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelTL;
								} else {
									image_cluster_ids[x][y] = pixelL;
								}
							}
						}
					} else {
						if (pixelT < foreground) {
							if (pixelTL < foreground) {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelTR;
								} else {
									image_cluster_ids[x][y] = pixelL;
									memorizeEqualClusterIds(pixelTR, pixelL);
								}
							} else {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelTL;
									memorizeEqualClusterIds(pixelTR, pixelTL);
								} else {
									image_cluster_ids[x][y] = pixelL;
									memorizeEqualClusterIds(pixelTR, pixelL);
								}
							}
						} else {
							if (pixelTL < foreground) {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelT;
								} else {
									image_cluster_ids[x][y] = pixelL;
									memorizeEqualClusterIds(pixelT, pixelL);
								}
							} else {
								if (pixelL < foreground) {
									image_cluster_ids[x][y] = pixelTL;
								} else {
									image_cluster_ids[x][y] = pixelL;
								}
							}
						}
					}
				}
				
				break;
			
			// letzte Spalte rechts, nicht die Ecke oben
			case LAST_ROW:
				pixelT = image_cluster_ids[x - 1][y];
				pixelTL = image_cluster_ids[x - 1][y - 1];
				pixelL = image_cluster_ids[x][y - 1];
				
				if (pixelT < foreground) {
					if (pixelTL < foreground) {
						if (pixelL < foreground) {
							image_cluster_ids[x][y] = currentClusterId;
							currentClusterId++;
						} else {
							image_cluster_ids[x][y] = pixelL;
						}
					} else {
						if (pixelL < foreground) {
							image_cluster_ids[x][y] = pixelTL;
						} else {
							image_cluster_ids[x][y] = pixelL;
						}
					}
				} else {
					if (pixelTL < foreground) {
						if (pixelL < foreground) {
							image_cluster_ids[x][y] = pixelT;
						} else {
							image_cluster_ids[x][y] = pixelL;
							memorizeEqualClusterIds(pixelT, pixelL);
						}
					} else {
						if (pixelL < foreground) {
							image_cluster_ids[x][y] = pixelTL;
						} else {
							image_cluster_ids[x][y] = pixelL;
						}
					}
				}
				break;
		}
		
	}
	
	private void memorizeEqualClusterIds(int clusterId1, int clusterId2) {
		if (clusterId1 != clusterId2) {
			if (!equalClusterIds.containsKey(clusterId2)) {
				equalClusterIds.put(clusterId2, new HashSet<Integer>());
				equalClusterIds.get(clusterId2).add(clusterId1);
			} else
				if (!equalClusterIds.get(clusterId2).contains(clusterId1)) {
					equalClusterIds.get(clusterId2).add(clusterId1);
				}
		}
	}
	
	private void mergeHashMapRecursive() {
		
		tableLinks = new int[currentClusterId][currentClusterId];
		clusterMap = new int[currentClusterId];
		linesRun = new boolean[currentClusterId];
		
		for (int key : equalClusterIds.keySet()) {
			for (int value : equalClusterIds.get(key)) {
				tableLinks[key][value] = -1;
				tableLinks[value][key] = -1;
			}
		}
		
		for (int i = 0; i < currentClusterId; i++) {
			clusterMap[i] = i;
			linesRun[i] = true;
		}
		// long ersteZeit = System.currentTimeMillis();
		recursiveMerge(1, 1, currentClusterId, 0, 0);
		// long zweiteZeit = System.currentTimeMillis();
		// System.out.println("Dauer: " + (zweiteZeit - ersteZeit));
	}
	
	private void recursiveMerge(int zeile, int spalte, int zaehlerJ, int missachten, int aktuellerCluster) {
		for (int j = zeile; j < zaehlerJ; j++) {
			for (int i = spalte; i < currentClusterId && linesRun[j]; i++) {
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
						
						if (i != currentClusterId)
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
		int[][] eingabe_image = { { 0, 1, 1, 0, 1, 1, 1 },
				{ 1, 1, 1, 1, 1, 0, 1 },
				{ 0, 0, 0, 0, 0, 0, 1 },
				{ 0, 0, 0, 0, 0, 1, 1 },
				{ 0, 1, 0, 0, 0, 0, 0 },
				{ 1, 1, 0, 0, 0, 0, 0 } };
		//
		PixelSegmentation test = new PixelSegmentation(new Image(eingabe_image), NeighbourhoodSetting.NB4);
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
	}
	
	private void mergeHashMapDritteVariante() {
		
		int[] fuellGrad = new int[currentClusterId]; // enspricht x
		clusterMap = new int[currentClusterId]; // entspricht y
		tableLinks = new int[currentClusterId][currentClusterId];// entspricht z
		
		for (int i = 0; i < currentClusterId; i++) {
			clusterMap[i] = -1; // überall wo -1 steht wird dann der Index als Cluster gesetzt
		}
		
		for (int key : equalClusterIds.keySet()) {
			
			if (clusterMap[key] != -1) {
				
				fuellGrad = anpassen(key, key, fuellGrad);
				
			} else {
				clusterMap[key] = key;
			}
			
			for (int value : equalClusterIds.get(key)) {
				
				tableLinks[key][fuellGrad[key]] = value;
				fuellGrad[key] += 1;
				
				if (clusterMap[value] != -1) {
					fuellGrad = anpassen(value, key, fuellGrad);
				} else {
					clusterMap[value] = key;
					tableLinks[value][fuellGrad[value]] = key;
					fuellGrad[value] += 1;
				}
				for (int value2 : equalClusterIds.get(key))
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
		
		imageClusterSize = new int[currentClusterId];
		
		for (int i = 0; i < src_image.length; i++)
			for (int j = 0; j < src_image[i].length; j++) {
				if (clusterMap[image_cluster_ids[i][j]] != -1)
					image_cluster_ids[i][j] = clusterMap[image_cluster_ids[i][j]];
				
				imageClusterSize[image_cluster_ids[i][j]]++;
			}
	}
	
	private void secondPassToepfe() {
		int[] clusterMap = new int[currentClusterId];
		imageClusterSize = new int[currentClusterId];
		for (int i = 0; i < currentClusterId; i++)
			clusterMap[i] = i;
		
		if (!equalClusterIds.isEmpty())
			for (int clusterID : equalClusterIds.keySet())
				for (int arrayID : equalClusterIds.get(clusterID))
					clusterMap[arrayID] = clusterID;
		
		for (int i = 0; i < src_image.length; i++)
			for (int j = 0; j < src_image[i].length; j++) {
				image_cluster_ids[i][j] = clusterMap[image_cluster_ids[i][j]];
				imageClusterSize[image_cluster_ids[i][j]]++;
			}
	}
	
	private void mergeHashMapToepfe() {
		
		ArrayList<HashSet<Integer>> toepfe = new ArrayList<HashSet<Integer>>(); // equalClusterIds.size()
		for (int key : equalClusterIds.keySet()) {
			HashSet<Integer> topf = new HashSet<Integer>();
			topf.add(key);
			toepfe.add(topf);
			for (int value : equalClusterIds.get(key)) {
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
		
		equalClusterIds.clear();
		for (HashSet<Integer> topf : toepfe) {
			if (topf.isEmpty())
				continue;
			Integer key = topf.iterator().next();
			// topf.remove(key);
			equalClusterIds.put(key, topf);// new ArrayList<Integer>(topf));
		}
	}
	
	@Override
	public Vector2i[] getClusterCenterPoints() {
		
		Vector2i[] res = new Vector2i[clusterMinX.length];
		for (int i = 0; i < clusterMinX.length; i++) {
			int w = clusterMaxX[i] - clusterMinX[i];
			int h = clusterMaxY[i] - clusterMinY[i];
			
			int cx = clusterMinX[i] + w / 2;
			int cy = clusterMinY[i] + h / 2;
			
			res[i] = new Vector2i(cx, cy);
			
			// System.out.println("Center of Cluster = " + i + " Point X = " + cx + " Y = " + cy);
		}
		
		return res;
	}
	
	@Override
	public Vector2i[] getClusterDimension() {
		
		Vector2i[] res = new Vector2i[clusterMinX.length];
		for (int i = 0; i < clusterMinX.length; i++) {
			int w = clusterMaxX[i] - clusterMinX[i];
			int h = clusterMaxY[i] - clusterMinY[i];
			
			res[i] = new Vector2i(w, h);
		}
		
		return res;
	}
	
	@SuppressWarnings("unused")
	public int[] getClusterSizeNormalized(int w, int h) {
		
		Vector2i[] clusterCenters = getClusterCenterPoints();
		int[] normalizedClusterAreaSizes = new int[getClusterSize().length];
		int[] clusterAreaSizes = getClusterSize();
		
		// maximum acceptible circle has size of half of circle around the complete image
		double distanceFromCenterToLeftTopEdge = new Vector2d(0, 0).distance(w / 2d, h / 2d) / 2;
		
		if (true)
			for (int cluster = 1; cluster < clusterAreaSizes.length; cluster++) {
				Vector2i centerOfCluster = clusterCenters[cluster];
				
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
				Vector2i center = clusterCenters[cluster];
				
				double distanceFromCenterToCluster = clusterCenters[0].distance(center);
				
				distanceFromCenterToLeftTopEdge = clusterCenters[0].distance(new Vector2i(0, 0));
				
				double d = ((distanceFromCenterToLeftTopEdge - distanceFromCenterToCluster) < 0) ? 0
						: (distanceFromCenterToLeftTopEdge - distanceFromCenterToCluster);
				
				normalizedClusterAreaSizes[cluster] = (int) (clusterAreaSizes[cluster] * (d / distanceFromCenterToLeftTopEdge));
				
				// System.out.println("cluster = " + cluster + " newPixel = " + output[cluster] + " oldPixel = " + input[cluster] + " Abstand zum Zentrum: " +
				// distanceFromCenterToCluster);
			}
		
		return normalizedClusterAreaSizes;
	}
	
	@Override
	public int[] getClusterDimensionMinWH() {
		Vector2i[] vector2ds = getClusterDimension();
		int[] res = new int[vector2ds.length];
		for (int index = 0; index < vector2ds.length; index++) {
			int w = vector2ds[index].x;
			int h = vector2ds[index].y;
			if (w > h)
				res[index] = h;
			else
				res[index] = w;
		}
		return res;
	}
	
	@Override
	public int[] getImage1A() {
		return ImageConverter.convert2Ato1A(src_image);
	}
	
	@Override
	public int[] getImageClusterIdMask() {
		return ImageConverter.convert2Ato1A(image_cluster_ids);
	}
	
	@Override
	public void detectClusters() {
		doPixelSegmentation(1);
	}
	
	@Override
	public int getClusterCount() {
		return getNumberOfCluster();
	}
	
	@Override
	public int getForegroundPixelCount() {
		return getNumberOfPixel();
	}
	
	@Override
	public void printClusterIds() {
		printClusterArray();
	}
	
}
