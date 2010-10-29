/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;

/**
 * @author entzian
 *
 */
public class pixel_segmentierung {

	private final int [][] original_image;
	private boolean nb; //true = 8er; false = 4er
	private final int zaehlerK = 1;
	private int zaehler = zaehlerK;
	private HashMap<Integer, ArrayList<Integer>> clusterVerweis = new HashMap<Integer, ArrayList<Integer>>();
	
	private int [] zaehlerArray;
	private int [][] image;
	
	public static void main(String[] args) {
	
		
//		int [][] eingabe_image = { { 0, 1 },
//						   		   { 0, 0 } };
//		
//		int [][] eingabe_image = { { 0, 1, 1 },
//						   		   { 1, 0, 0 } };
//		
//		int [][] eingabe_image = { { 0, 0, 0 },
//								   { 0, 0, 0 } };
//		
//		int [][] eingabe_image = { { 1, 1, 1 },
//				   				   { 1, 1, 1 } };
//		
		int [][] eingabe_image = { { 0, 1, 1, 0, 1, 0, 1, 0 },
						   		   { 1, 1, 0, 0, 1, 1, 1, 0 },
						   		   { 0, 1, 1, 1, 1, 0, 1, 0 },
						   		   { 0, 0, 0, 0, 0, 1, 1, 0 },
						   		   { 0, 1, 1, 1, 0, 0, 0, 1 },
						   		   { 1, 1, 1, 1, 1, 0, 0, 0 } };
//		
//		//boolean nachbarschaft = false; //true = 8er; false = 4er
//		
		pixel_segmentierung test = new pixel_segmentierung(eingabe_image, false);
		test.doPixelSegmentation();
		test.printOriginalImage();
		test.printImage();
		test.printClusterArray();
		System.out.println("Number of Clusters: " + test.getNumberOfCluster());
		System.out.println("Number of Pixel: " + test.getNumberOfPixel());
		
		pixel_segmentierung test2 = new pixel_segmentierung(eingabe_image, true);
		test2.doPixelSegmentation();
		test2.printOriginalImage();
		test2.printImage();
		test2.printClusterArray();
		System.out.println("Number of Clusters: " + test2.getNumberOfCluster());
		System.out.println("Number of Pixel: " + test2.getNumberOfPixel());
	}
	
	
	public pixel_segmentierung(int [][] image, boolean nb){
		original_image = image;
		this.image = new int[image.length][image[0].length];
		this.nb = nb;
	}
	
	
	//###############  Public ####################
	
	public int [] getClusterCounts(){
		return zaehlerArray;
	}
	
	public int [][] getImageMask(){
		return image;
	}
	
	public int getNumberOfCluster(){
		int clusterNumbers = 0;
		for(int pixelIndex = 1; pixelIndex < zaehlerArray.length; pixelIndex++)
			if(zaehlerArray[pixelIndex] != 0)
				clusterNumbers++;
		return clusterNumbers;
	}
	
	public int getNumberOfPixel(){
		int pixelNumbers = 0;
		for(int pixelIndex = 1; pixelIndex < zaehlerArray.length; pixelIndex++)
			pixelNumbers = pixelNumbers + zaehlerArray[pixelIndex];
		return pixelNumbers;
	}
	
	public void doPixelSegmentation(){
		firstPass();
		mergeHashMap();
		secondPass();
	}
	
	
	//###############	Print-Methoden	######################
	
	public void printOriginalImage()
	{
		printOriginalImage(this.original_image);
	}
	
	private void printOriginalImage(int[][] original_image) {
		// TODO Auto-generated method stub
		printImage(original_image, "OriginalImage");
	}


	public void printImage()
	{
		printImage(this.image);
	}
	
	public void printImage(int [][] image)
	{
		printImage(image, "ClusterImage");
	}
	
	public void printImage(int [][] image, String text){
		System.out.println(text);
		for(int i = 0; i < image.length; i++){ 
			for(int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}
	
	public void printHashMap(){
		printHashMap(this.clusterVerweis);
	}
	
	public void printHashMap(HashMap<Integer, ArrayList<Integer>> hashM){
		
		if(!hashM.isEmpty())
			for( int clusterID : hashM.keySet() )
				System.out.println("To cluster " + clusterID + " belongs Cluster: " + hashM.get(clusterID));
		else
			System.out.println("No cluster has to be merge!");
	}
	
	public void printClusterArray(){
		printClusterArray(this.zaehlerArray);
	}
	
	public void printClusterArray(int[] zaehlerArray2) {
		
		if(zaehlerArray2.length > zaehlerK)
			for( int arrayID = zaehlerK; arrayID < zaehlerArray2.length; arrayID++ )
				System.out.println("Cluster " + arrayID + " contains " + zaehlerArray2[arrayID] + " pixel");
		else
			System.out.println("No cluster available!");
	}
	
	//kann eigentlich weg
	public void printList(ArrayList<Integer> liste)
	{
		for(int i = zaehlerK; i < liste.size(); i++)
			System.out.println("Cluster " + i + " contains " + liste.get(i) + " pixel");
	}
	
	
	//############# Private #################
	
	private void firstPass(){
		
		for (int i = 0; i < original_image.length; i++)
			for (int j = 0; j < original_image[i].length; j++) {
				if(original_image[i][j] == 1){
					if (i == 0 && j == 0)
						parse(Position.FIRST_FIELD);
					else if (i == 0)
						parse(Position.FIRST_ROW, 0, j);
					else if (j == 0)
						parse(Position.FIRST_COLUMN, i);
					else if (j == original_image[i].length-1 && nb)
						parse(Position.LAST_COLUMN, i, j);
					else
						parse(Position.REMAINING, i, j);
				}
			}
	}
	
	private void secondPass(){
		
		int [] clusterMap = new int [zaehler];
		zaehlerArray = new int [zaehler];
		for (int i = 0; i < zaehler; i++)
			clusterMap[i] = i;
		
		if(!clusterVerweis.isEmpty())
			for( int clusterID : clusterVerweis.keySet() )
				for(int arrayID : clusterVerweis.get(clusterID))
					clusterMap[arrayID] = clusterID;		
		
		for (int i = 0; i < original_image.length; i++)
			for (int j = 0; j < original_image[i].length; j++) {
				image[i][j] = clusterMap[image[i][j]];
				zaehlerArray[image[i][j]]++;			
			}
		
	}
	

	private void parse(Position zahl){
		parse(zahl, 0, 0);
	}
	
	private void parse(Position zahl, int i){
		parse(zahl, i, 0);
	}
	
	private void parse(Position position, int i, int j){
		
//		|pixel3|pixel2|pixel1|
//		|pixel4|  x   |		 |
		
		int pixel1, pixel2, pixel3, pixel4;
		
		
		switch(position)
		{		//Ecke links oben und rechts oben
			case FIRST_FIELD: 
					image[i][j]= zaehler; 
					zaehler++;

					break;
				
				//erste Zeile oben, nicht die Ecke links aber die Ecke rechts
			case FIRST_ROW: 
					pixel4 = image[i][j-1];
				
					if(pixel4 < zaehlerK){
						image[i][j] = zaehler; 
						zaehler++;
					}
					else
						image[i][j] = pixel4;
						
					break;
			
				//erste Spalte links, nicht die Ecke oben
			case FIRST_COLUMN:					
					if(!nb){	//4er
						pixel2 = image[i-1][j];
						
						if (pixel2 < zaehlerK) {
							image[i][j] = zaehler;
							zaehler++;
						} else 
							image[i][j] = pixel2;
					} else {	//8er
						pixel1 = image[i-1][j+1];
						pixel2 = image[i-1][j];
						
						if(pixel2 < zaehlerK && pixel1 < zaehlerK){
							image[i][j] = zaehler;
							zaehler++;
							
						} else if(pixel2 < zaehlerK && pixel1 > zaehlerK-1){
							image[i][j] = pixel1;
							
						} else if(pixel2 > zaehlerK-1 && pixel1 < zaehlerK){
							image[i][j] = pixel2;
						
						} else {
							image[i][j] = pixel2;
							
							//hashMapFuellen(image[i-1][j], image[i-1][j+1]);
						}
					}
					
					break;
			
				//alles bis auf den linken, rechten (bei 8er Nachbarschaft) und oberen Rand
			case REMAINING: 				
					if(!nb){		//4er
						
						pixel2 = image[i-1][j];
						pixel4 = image[i][j-1];
						
						if (pixel2 < zaehlerK && pixel4 < zaehlerK) {
							image[i][j] = zaehler;
							zaehler++;
						
						} else if(pixel2 < zaehlerK && pixel4 > zaehlerK-1){
							image[i][j] = pixel4;
								
						} else if(pixel2 > zaehlerK-1 && pixel4 < zaehlerK){
							image[i][j] = pixel2;
								
						} else {	
							image[i][j] = pixel4;
							hashMapFuellen(pixel2, pixel4);
						}
					} else { //8er
						
						pixel1 = image[i-1][j+1];
						pixel2 = image[i-1][j];
						pixel3 = image[i-1][j-1];
						pixel4 = image[i][j-1];
						
						if(pixel1 < zaehlerK){
							if(pixel2 < zaehlerK){
								if(pixel3 < zaehlerK){
									if(pixel4 < zaehlerK){
										image[i][j] = zaehler;
										zaehler++;
									} else {
										image[i][j] = pixel4;
									}
								} else {
									if(pixel4 < zaehlerK){
										image[i][j] = pixel3;
									} else {
										image[i][j] = pixel4;
									}
								}
							} else {
								if(pixel3 < zaehlerK){
									if(pixel4 < zaehlerK){
										image[i][j] = pixel2;
									} else {
										image[i][j] = pixel4;
										hashMapFuellen(pixel2, pixel4);
									}
								} else {
									if(pixel4 < zaehlerK){
										image[i][j] = pixel3;
									} else {
										image[i][j] = pixel4;
									}
								}
							}
						} else {
							if(pixel2 < zaehlerK){
								if(pixel3 < zaehlerK){
									if(pixel4 < zaehlerK){
										image[i][j] = pixel1;
									} else {
										image[i][j] = pixel4;
										hashMapFuellen(pixel1, pixel4);
									}
								} else {
									if(pixel4 < zaehlerK){
										image[i][j] = pixel3;
										hashMapFuellen(pixel1, pixel3);
									} else {
										image[i][j] = pixel4;
										hashMapFuellen(pixel1,pixel4);
									}
								}
							} else {
								if(pixel3 < zaehlerK){
									if(pixel4 < zaehlerK){
										image[i][j] = pixel2;
									} else {
										image[i][j] = pixel4;
										hashMapFuellen(pixel2, pixel4);
									}
								} else {
									if(pixel4 < zaehlerK){
										image[i][j] = pixel3;
									} else {
										image[i][j] = pixel4;
									}
								}
							}
						}
						
						
//						if(pixel1 < zaehlerK && pixel2 < zaehlerK && pixel3 < zaehlerK && pixel4 < zaehlerK){
//							image[i][j] = zaehler;
//							zaehler++;
//						} else if(pixel1 < zaehlerK && pixel2 < zaehlerK && pixel3 < zaehlerK && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//								
//						} else if(pixel1 < zaehlerK && pixel2 < zaehlerK && pixel3 > zaehlerK-1 && pixel4 < zaehlerK){
//							image[i][j] = pixel3;
//								
//						} else if(pixel1 < zaehlerK && pixel2 > zaehlerK-1 && pixel3 < zaehlerK && pixel4 < zaehlerK){
//							image[i][j] = pixel2;
//								
//						} else if(pixel1 > zaehlerK-1 && pixel2 < zaehlerK && pixel3 < zaehlerK && pixel4 < zaehlerK){
//							image[i][j] = pixel1;
//						//###########  1er		
//						} else if(pixel1 > zaehlerK-1 && pixel2 > zaehlerK-1 && pixel3 < zaehlerK && pixel4 < zaehlerK){
//							image[i][j] = pixel2;
//								
//						} else if(pixel1 > zaehlerK-1 && pixel2 < zaehlerK && pixel3 > zaehlerK-1 && pixel4 < zaehlerK){
//							image[i][j] = pixel3;
//							hashMapFuellen(pixel1, pixel3);
//								
//						} else if(pixel1 > zaehlerK-1 && pixel2 < zaehlerK && pixel3 < zaehlerK && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//							hashMapFuellen(pixel1, pixel4);
//						//#######	 2er P1	
//						} else if(pixel1 < zaehlerK && pixel2 > zaehlerK-1 && pixel3 > zaehlerK-1 && pixel4 < zaehlerK){
//							image[i][j] = pixel3;
//								
//						} else if(pixel1 < zaehlerK && pixel2 > zaehlerK-1 && pixel3 < zaehlerK && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//							hashMapFuellen(pixel2, pixel4);
//						//######## 2er P2		
//						} else if(pixel1 < zaehlerK && pixel2 < zaehlerK && pixel3 > zaehlerK-1 && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//						//######## 2er P3		
//						} else if(pixel1 > zaehlerK-1 && pixel2 > zaehlerK-1 && pixel3 < zaehlerK && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//							hashMapFuellen(pixel2, pixel4);
//								
//						} else if(pixel1 > zaehlerK-1 && pixel2 < zaehlerK && pixel3 > zaehlerK-1 && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//							hashMapFuellen(pixel1,pixel4);
//								
//						} else if(pixel1 > zaehlerK-1 && pixel2 > zaehlerK-1 && pixel3 > zaehlerK-1 && pixel4 < zaehlerK){
//							image[i][j] = pixel3;
//								
//						} else if(pixel1 < zaehlerK && pixel2 > zaehlerK-1 && pixel3 > zaehlerK-1 && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//						//####### 3er		
//						} else {
//							image[i][j] = pixel4;
//						//####### 4er
//						}
					}
					
					break;

					
			//letzte Spalte rechts, nicht die Ecke oben
			case LAST_COLUMN:
					pixel2 = image[i-1][j];
					pixel3 = image[i-1][j-1];
					pixel4 = image[i][j-1];
				
						if(pixel2 < zaehlerK){
							if(pixel3 < zaehlerK){
								if(pixel4 < zaehlerK){
									image[i][j] = zaehler;
									zaehler++;
								} else {
									image[i][j] = pixel4;
								}
							} else {
								if(pixel4 < zaehlerK){
									image[i][j] = pixel3;
								} else {
									image[i][j] = pixel4;
								}
							}	
						} else {
							if(pixel3 < zaehlerK){
								if(pixel4 < zaehlerK){
									image[i][j] = pixel2;
								} else {
									image[i][j] = pixel4;
									hashMapFuellen(pixel2, pixel4);
								}
							} else {
								if(pixel4 < zaehlerK){
									image[i][j] = pixel3;
								} else {
									image[i][j] = pixel4;
								}
							}
						}		
					
//					
//						if(pixel2 < zaehlerK && pixel3 < zaehlerK && pixel4 < zaehlerK){
//							image[i][j] = zaehler;
//							zaehler++;
//							
//						} else if(pixel2 < zaehlerK && pixel3 < zaehlerK && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//							
//						} else if(pixel2 < zaehlerK && pixel3 > zaehlerK-1 && pixel4 < zaehlerK){
//							image[i][j] = pixel3;
//						
//						} else if(pixel2 > zaehlerK-1 && pixel3 < zaehlerK && pixel4 < zaehlerK){
//							image[i][j] = pixel2;
//						
//						} else if(pixel2 < zaehlerK && pixel3 > zaehlerK-1 && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//						
//						} else if(pixel2 > zaehlerK-1 && pixel3 < zaehlerK && pixel4 > zaehlerK-1){
//							image[i][j] = pixel4;
//							hashMapFuellen(pixel2, pixel4);
//						
//						} else if(pixel2 > zaehlerK-1 && pixel3 > zaehlerK-1 && pixel4 < zaehlerK){
//							image[i][j] = pixel3;
//						
//						} else {	
//							image[i][j] = pixel4;
//							
//						}
					break;
		}
		
	}
	
	
	private void hashMapFuellen(int Pixel1, int Pixel2){
		if(Pixel1 != Pixel2){
			if (!clusterVerweis.containsKey(Pixel2)){
				clusterVerweis.put(Pixel2, new ArrayList<Integer>());
				clusterVerweis.get(Pixel2).add(Pixel1);
			} else if (!clusterVerweis.get(Pixel2).contains(Pixel1)) {
				clusterVerweis.get(Pixel2).add(Pixel1);
			}
		}
	}
	
		
	private void mergeHashMap(){
	
		ArrayList<Integer> keysByValue, temp, ersterFund;
		HashMap<Integer, ArrayList<Integer>> clusterVerweis_temp = new HashMap<Integer, ArrayList<Integer>>();

		for( int clusterID : clusterVerweis.keySet() ){
			keysByValue = getKeyByValue2(clusterVerweis, clusterID);
			
			if(!keysByValue.isEmpty()){
			
				//alle Cluster die zur "clusterID" gehoeren zum ersten Fund von "keysByValue" hinzufuegen
				temp = clusterVerweis.get(clusterID);
				Integer firstClusterID = keysByValue.get(0);
				ersterFund = clusterVerweis.get(firstClusterID);
				
				for(int i = 0; i < temp.size(); i++)
					if(!ersterFund.contains(temp.get(i)) && firstClusterID != temp.get(i)){
						ersterFund.add(temp.get(i));
						if(clusterVerweis_temp.containsKey(temp.get(i)))
							clusterVerweis_temp.remove(temp.get(i));
					}
				
				//alle weiteren Cluster aus "keysByValue" ebenfalls zum ersten Fund hinzufuegen
				if(keysByValue.size() > 1)
					for(int i = 1; i < keysByValue.size(); i++){
						
						temp = clusterVerweis.get(keysByValue.get(i));

						for(int tempJ : temp) {
							if(!ersterFund.contains(tempJ) && firstClusterID != tempJ){
								ersterFund.add(tempJ);
								if(clusterVerweis_temp.containsKey(tempJ))
									clusterVerweis_temp.remove(tempJ);
							}
						}
						
						if(!ersterFund.contains(keysByValue.get(i)) && firstClusterID != keysByValue.get(i)){
							ersterFund.add(keysByValue.get(i));
							if(clusterVerweis_temp.containsKey(keysByValue.get(i)))
								clusterVerweis_temp.remove(keysByValue.get(i));
						}					
					}
				clusterVerweis_temp.put(firstClusterID, ersterFund);
			} else 
				clusterVerweis_temp.put(clusterID,clusterVerweis.get(clusterID));

		}
		
		clusterVerweis.clear();
		clusterVerweis.putAll(clusterVerweis_temp);
	}
	
	
	private ArrayList<Integer> getKeyByValue2(HashMap<Integer, ArrayList<Integer>> hashM, Integer value){
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(Integer i : hashM.keySet())
			if(hashM.get(i).contains(value))
				list.add(i);

		return list;
	}
}
