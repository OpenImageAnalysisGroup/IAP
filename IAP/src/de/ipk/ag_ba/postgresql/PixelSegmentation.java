/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

import java.util.ArrayList;
import java.util.HashMap;

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

	public void doPixelSegmentation() {
		firstPass();
		mergeHashMap();
		secondPass();
	}

	// ############### Print-Methoden ######################

	public void printOriginalImage() {
		printOriginalImage(this.src_image);
	}

	private void printOriginalImage(int[][] original_image) {
		printImage(original_image, "OriginalImage");
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

				if (pixelT < foreground && pixelL < foreground) {
					image_cluster_ids[i][j] = zaehler;
					zaehler++;

				} else if (pixelT < foreground && pixelL > foreground - 1) {
					image_cluster_ids[i][j] = pixelL;

				} else if (pixelT > foreground - 1 && pixelL < foreground) {
					image_cluster_ids[i][j] = pixelT;

				} else {
					image_cluster_ids[i][j] = pixelL;
					addHashMapEntry(pixelT, pixelL);
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

	private void mergeHashMap() {
		HashMap<Integer, ArrayList<Integer>> clusterVerweis_temp = new HashMap<Integer, ArrayList<Integer>>();

		for (int clusterID : clusterMapping.keySet()) {
			ArrayList<Integer> keysByValue = getKeyByValue2(clusterMapping, clusterID);

			if (!keysByValue.isEmpty()) {

				// alle Cluster die zur "clusterID" gehoeren zum ersten Fund von
				// "keysByValue" hinzufuegen
				ArrayList<Integer> temp = clusterMapping.get(clusterID);
				Integer firstClusterID = keysByValue.get(0);
				ArrayList<Integer> ersterFund = clusterMapping.get(firstClusterID);

				for (int i = 0; i < temp.size(); i++)
					if (!ersterFund.contains(temp.get(i)) && firstClusterID != temp.get(i)) {
						ersterFund.add(temp.get(i));
						if (clusterVerweis_temp.containsKey(temp.get(i)))
							clusterVerweis_temp.remove(temp.get(i));
					}

				// alle weiteren Cluster aus "keysByValue" ebenfalls zum ersten Fund
				// hinzufuegen
				if (keysByValue.size() > 1)
					for (int i = 1; i < keysByValue.size(); i++) {

						temp = clusterMapping.get(keysByValue.get(i));

						for (int tempJ : temp) {
							if (!ersterFund.contains(tempJ) && firstClusterID != tempJ) {
								ersterFund.add(tempJ);
								if (clusterVerweis_temp.containsKey(tempJ))
									clusterVerweis_temp.remove(tempJ);
							}
						}

						if (!ersterFund.contains(keysByValue.get(i)) && firstClusterID != keysByValue.get(i)) {
							ersterFund.add(keysByValue.get(i));
							if (clusterVerweis_temp.containsKey(keysByValue.get(i)))
								clusterVerweis_temp.remove(keysByValue.get(i));
						}
					}
				clusterVerweis_temp.put(firstClusterID, ersterFund);
			} else
				clusterVerweis_temp.put(clusterID, clusterMapping.get(clusterID));

		}

		clusterMapping.clear();
		clusterMapping.putAll(clusterVerweis_temp);
	}

	private ArrayList<Integer> getKeyByValue2(HashMap<Integer, ArrayList<Integer>> hashM, Integer value) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer i : hashM.keySet())
			if (hashM.get(i).contains(value))
				list.add(i);

		return list;
	}
}
