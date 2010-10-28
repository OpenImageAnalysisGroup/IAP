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
		
//		int [][] eingabe_image = { { 0, 1, 1 },
//						   		   { 1, 0, 0 } };
		
		
		
		int [][] eingabe_image = { { 0, 1, 1, 0, 1, 0, 1, 0 },
						   		   { 1, 1, 0, 0, 1, 1, 1, 0 },
						   		   { 0, 1, 1, 1, 1, 0, 1, 0 },
						   		   { 0, 0, 0, 0, 0, 1, 1, 0 },
						   		   { 0, 1, 1, 1, 0, 0, 0, 1 },
						   		   { 1, 1, 1, 1, 1, 0, 0, 0 } };
		
		//boolean nachbarschaft = false; //true = 8er; false = 4er
		
		pixel_segmentierung test = new pixel_segmentierung(eingabe_image, false);
		pixel_segmentierung test2 = new pixel_segmentierung(eingabe_image, true);
	}
	
	
	public pixel_segmentierung(int [][] image, boolean nb){
		original_image = image;
		this.image = new int[image.length][image[0].length];
		this.nb = nb;
		
		System.out.println("Originalbild 1");
		printImage(original_image);
	
		//die Ausgangscluster werden bestimmt 
		ersterDurchlauf();
		
//		System.out.println("HashMap");
//		printHashMap(clusterVerweis);
		
		//es wird festgelegt welche Cluster einen gro�en Cluster zusammen ergeben
		mergeHashMap();
		
		//die Endcluster werden beschrieben und es wird die Gr��e der Cluster bestimmt
		zweiterDurchlauf();
		

		System.out.println("neues Bild:");
		printImage();
		
		System.out.println("Clustergr��e:");
		printArray(zaehlerArray);
	}
	
	private void ersterDurchlauf(){
		
		for (int i = 0; i < original_image.length; i++)
			for (int j = 0; j < original_image[i].length; j++) {
				if(original_image[i][j] == 1){
					if (i == 0 && j == 0)
						parse(1);
					else if (i == 0)
						parse(2, 0, j);
					else if (j == 0)
						parse(3, i);
					else if (j == original_image[i].length-1 && nb)
						parse(5, i, j);
					else
						parse(4, i, j);
				}
			}
	}
	
	private void zweiterDurchlauf(){
		
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
	

	private void parse(int zahl){
		parse(zahl, 0, 0);
	}
	
	private void parse(int zahl, int i){
		parse(zahl, i, 0);
	}
	
	private void parse(int zahl, int i, int j){
		
		
//		|Pixel3|Pixel2|Pixel1|
//		|Pixel4|  x   |		 |	
		
		int Pixel1, Pixel2, Pixel3, Pixel4;
		
		
		switch(zahl)
		{		//Ecke links oben und rechts oben
			case 1: 
					image[i][j]= zaehler; 
					zaehler++;

					break;
				
				//erste Zeile oben, nicht die Ecke links aber die Ecke rechts
			case 2: 
					Pixel4 = image[i][j-1];
				
					if(Pixel4 < zaehlerK){
						image[i][j] = zaehler; 
						zaehler++;
					}
					else
						image[i][j] = Pixel4;
						
					break;
			
				//erste Spalte links, nicht die Ecke oben
			case 3:					
					if(!nb){	//4er
						
						Pixel2 = image[i-1][j];
						
						if (Pixel2 < zaehlerK) {
							image[i][j] = zaehler;
							zaehler++;
						} else 
							image[i][j] = Pixel2;
					} else {	//8er
						
						Pixel1 = image[i-1][j+1];
						Pixel2 = image[i-1][j];
						
						if(Pixel2 < zaehlerK && Pixel1 < zaehlerK){
							image[i][j] = zaehler;
							zaehler++;
							
						} else if(Pixel2 < zaehlerK && Pixel1 > zaehlerK-1){
							image[i][j] = Pixel1;
							
						} else if(Pixel2 > zaehlerK-1 && Pixel1 < zaehlerK){
							image[i][j] = Pixel2;
						
						} else {
							image[i][j] = Pixel2;
							
							//hashMapFuellen(image[i-1][j], image[i-1][j+1]);
						}
					}
					
					break;
			
				//alles bis auf den linken, rechten (bei 8er Nachbarschaft) und oberen Rand
			case 4: 				
					if(!nb){		//4er
						
						Pixel2 = image[i-1][j];
						Pixel4 = image[i][j-1];
						
						if (Pixel2 < zaehlerK && Pixel4 < zaehlerK) {
							image[i][j] = zaehler;
							zaehler++;
						
						} else if(Pixel2 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
								
						} else if(Pixel2 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel2;
								
						} else {	
							image[i][j] = Pixel4;
							hashMapFuellen(Pixel2, Pixel4);
						}
					} else { //8er
						
						Pixel1 = image[i-1][j+1];
						Pixel2 = image[i-1][j];
						Pixel3 = image[i-1][j-1];
						Pixel4 = image[i][j-1];
						
						if(Pixel1 < zaehlerK && Pixel2 < zaehlerK && Pixel3 < zaehlerK && Pixel4 < zaehlerK){
							image[i][j] = zaehler;
							zaehler++;
						} else if(Pixel1 < zaehlerK && Pixel2 < zaehlerK && Pixel3 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
								
						} else if(Pixel1 < zaehlerK && Pixel2 < zaehlerK && Pixel3 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel3;
								
						} else if(Pixel1 < zaehlerK && Pixel2 > zaehlerK-1 && Pixel3 < zaehlerK && Pixel4 < zaehlerK){
							image[i][j] = Pixel2;
								
						} else if(Pixel1 > zaehlerK-1 && Pixel2 < zaehlerK && Pixel3 < zaehlerK && Pixel4 < zaehlerK){
							image[i][j] = Pixel1;
						//###########  1er		
						} else if(Pixel1 > zaehlerK-1 && Pixel2 > zaehlerK-1 && Pixel3 < zaehlerK && Pixel4 < zaehlerK){
							image[i][j] = Pixel2;
								
						} else if(Pixel1 > zaehlerK-1 && Pixel2 < zaehlerK && Pixel3 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel3;
							hashMapFuellen(Pixel1, Pixel3);
								
						} else if(Pixel1 > zaehlerK-1 && Pixel2 < zaehlerK && Pixel3 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							hashMapFuellen(Pixel1, Pixel4);
						//#######	 2er P1	
						} else if(Pixel1 < zaehlerK && Pixel2 > zaehlerK-1 && Pixel3 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel3;
								
						} else if(Pixel1 < zaehlerK && Pixel2 > zaehlerK-1 && Pixel3 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							hashMapFuellen(Pixel2, Pixel4);
						//######## 2er P2		
						} else if(Pixel1 < zaehlerK && Pixel2 < zaehlerK && Pixel3 > zaehlerK-1 && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
						//######## 2er P3		
						} else if(Pixel1 > zaehlerK-1 && Pixel2 > zaehlerK-1 && Pixel3 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							hashMapFuellen(Pixel2, Pixel4);
								
						} else if(Pixel1 > zaehlerK-1 && Pixel2 < zaehlerK && Pixel3 > zaehlerK-1 && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							hashMapFuellen(Pixel1,Pixel4);
								
						} else if(Pixel1 > zaehlerK-1 && Pixel2 > zaehlerK-1 && Pixel3 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel3;
								
						} else if(Pixel1 < zaehlerK && Pixel2 > zaehlerK-1 && Pixel3 > zaehlerK-1 && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
						//####### 3er		
						} else {
							image[i][j] = Pixel4;
						//####### 4er
						}
					}
					
					break;

					
			//letzte Spalte rechts, nicht die Ecke oben
			case 5:
					Pixel2 = image[i-1][j];
					Pixel3 = image[i-1][j-1];
					Pixel4 = image[i][j-1];
				
						if(Pixel2 < zaehlerK && Pixel3 < zaehlerK && Pixel4 < zaehlerK){
							image[i][j] = zaehler;
							zaehler++;
							
						} else if(Pixel2 < zaehlerK && Pixel3 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							
						} else if(Pixel2 < zaehlerK && Pixel3 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel3;
						
						} else if(Pixel2 > zaehlerK-1 && Pixel3 < zaehlerK && Pixel4 < zaehlerK){
							image[i][j] = Pixel2;
						
						} else if(Pixel2 < zaehlerK && Pixel3 > zaehlerK-1 && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							//hashMapFuellen(i-1, j-1, i, j-1);
						
						} else if(Pixel2 > zaehlerK-1 && Pixel3 < zaehlerK && Pixel4 > zaehlerK-1){
							image[i][j] = Pixel4;
							hashMapFuellen(Pixel2, Pixel4);
						
						//Pixel1 und Pixel2 m�ssten immer im selben Cluster liegen
						} else if(Pixel2 > zaehlerK-1 && Pixel3 > zaehlerK-1 && Pixel4 < zaehlerK){
							image[i][j] = Pixel3;
							//hashMapFuellen(i-1, j, i-1, j-1);
						
						} else {	
							//wenn alle dei Pixel zu einem Cluster geh�ren, dann wird der linke Cluster f�r den aktuellen Pixel genommen
							//es muss keine hashMap zuweisung stattfinden, da bereits alle drei Pixel um den aktuellen Pixel verkn�pft wurden
							image[i][j] = Pixel4;
							//hashMapFuellen(i-1, j, i-1, j-1, i, j-1);
						}
					break;
				
			default: break;
		
		}
		
	}
	
	
	private void hashMapFuellen(int Pixel1, int Pixel2){
			
		if(Pixel1 != Pixel2){
			
			if (!clusterVerweis.containsKey(Pixel2)){
				clusterVerweis.put(Pixel2, new ArrayList<Integer>());
				clusterVerweis.get(Pixel2).add(Pixel1);
					
			}else if(!clusterVerweis.get(Pixel2).contains(Pixel1)){
				ArrayList<Integer> temp = clusterVerweis.get(Pixel2);
				temp.add(Pixel1);
				
			}
		}
	}
	
		
	private void mergeHashMap(){
	
		ArrayList<Integer> keysByValue, temp, ersterFund;
		HashMap<Integer, ArrayList<Integer>> clusterVerweis_temp = new HashMap<Integer, ArrayList<Integer>>();

		for( int clusterID : clusterVerweis.keySet() ){
			keysByValue = getKeyByValue2(clusterVerweis, clusterID);
			
			if(!keysByValue.isEmpty()){
			
				//alle Cluster die zur "clusterID" geh�ren zum ersten Fund von "keysByValue" hinzuf�gen
				temp = clusterVerweis.get(clusterID);
				ersterFund = clusterVerweis.get(keysByValue.get(0));
				
				for(int i = 0; i < temp.size(); i++)
					if(!ersterFund.contains(temp.get(i)) && keysByValue.get(0) != temp.get(i)){
						ersterFund.add(temp.get(i));
						if(clusterVerweis_temp.containsKey(temp.get(i)))
							clusterVerweis_temp.remove(temp.get(i));
					}
				
				//alle weiteren Cluster aus "keysByValue" ebenfalls zum ersten Fund hinzuf�gen
				if(keysByValue.size() > 1)
					for(int i = 1; i < keysByValue.size(); i++){
						
						temp = clusterVerweis.get(keysByValue.get(i));

						for(int j = 0; j < temp.size(); j++)
							if(!ersterFund.contains(temp.get(j)) && keysByValue.get(0) != temp.get(j)){
								ersterFund.add(temp.get(j));
								if(clusterVerweis_temp.containsKey(temp.get(j)))
									clusterVerweis_temp.remove(temp.get(j));
							}
						
						if(!ersterFund.contains(keysByValue.get(i)) && keysByValue.get(0) != keysByValue.get(i)){
							ersterFund.add(keysByValue.get(i));
							if(clusterVerweis_temp.containsKey(keysByValue.get(i)))
								clusterVerweis_temp.remove(keysByValue.get(i));
						}					
					}
				clusterVerweis_temp.put(keysByValue.get(0), ersterFund);
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
	
	
	//###############	Print-Methoden	######################
	
	public void printImage()
	{
		printImage(this.image);
	}
	
	public void printImage(int [][] image)
	{
		for(int i = 0; i < image.length; i++){ 
			for(int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}

	
	public void printList(ArrayList<Integer> liste)
	{
		for(int i = zaehlerK; i < liste.size(); i++)
			System.out.println("Cluster " + i + " enth�lt " + liste.get(i) + " Pixel");
	}
	
	public void printHashMap(HashMap<Integer, ArrayList<Integer>> hashM){
		
		if(!hashM.isEmpty())
			for( int clusterID : hashM.keySet() )
				System.out.println("Zu Cluster " + clusterID + " geh�ren folgenden Cluster: " + hashM.get(clusterID));
		else
			System.out.println("Es m�ssen keine Cluster fusioniert werden!");
	}
	

	public void printArray(int[] zaehlerArray2) {
		
		for( int arrayID = zaehlerK; arrayID < zaehlerArray2.length; arrayID++ )
			System.out.println("Cluster " + arrayID + " enth�lt " + zaehlerArray2[arrayID] + " Pixel");
	
	}

}
