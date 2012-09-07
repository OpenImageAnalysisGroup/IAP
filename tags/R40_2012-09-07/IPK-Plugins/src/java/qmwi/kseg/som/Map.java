/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.util.ArrayList;

import org.ErrorMsg;
import org.ObjectRef;

public class Map {
	public double[] eudist; // Euklidische Distanz
	public double[][] weights; // Eingabegewichte
	boolean[] ignoreNodes;
	int[] targetClusterIDforSomNode;
	double bias;
	public double beta = 0.1;
	public double gamma = 2;
	double[] gewinnFrequenz;
	
	double nachbar[]; // "Erregungsausbreitung"
	public double inputv[]; // Eingabevektor
	
	int breite; // "Breite" der Karte
	int minEuDist; // knoten mit der geringsten Euklidischen Distanz
	int nodes; // Anzahl an Neuronen
	private double lernrate;
	public double initLernrate = 0.3;
	
	Map(int somNodes, int kartenbr, int inputNodes) {
		eudist = new double[somNodes];
		ignoreNodes = new boolean[somNodes];
		for (int i = 0; i < ignoreNodes.length; i++)
			ignoreNodes[i] = false;
		targetClusterIDforSomNode = new int[somNodes];
		for (int i = 0; i < targetClusterIDforSomNode.length; i++)
			targetClusterIDforSomNode[i] = i + 1;
		gewinnFrequenz = new double[somNodes];
		inputv = new double[inputNodes];
		nachbar = new double[somNodes];
		weights = new double[inputNodes][somNodes];
		breite = kartenbr;
		nodes = somNodes;
		// i: inputNodesLäufer, j: somNodesLäufer
	}
	
	public int getSomWidth() {
		return breite;
	}
	
	public int getInputVectorSize() {
		return inputv.length;
	}
	
	public int getNeuronNodeCount() {
		return nodes;
	}
	
	public void analyzeNewInput(int t, int tmax, int nachbarF, double maxN, double oNew[]) {
		
		inputv = oNew;
		
		calcEuDis();
		findNetZ();
		berechneGewinnFrequenz();
		calcNachbarschaft(t, nachbarF, maxN);
		setLernrate(t, tmax);
		calcNewWeights(t);
		
		// printMatrix();
	}
	
	private void berechneGewinnFrequenz() {
		for (int i = 0; i < nodes; i++) {
			if (i == minEuDist)
				gewinnFrequenz[i] += beta * (1 - gewinnFrequenz[i]); // für das
			// Gewinnerneuron
			else
				gewinnFrequenz[i] += beta * (0 - gewinnFrequenz[i]); // für die
			// anderen
		}
	}
	
	private ArrayList<Double> calcEuDis() {
		double sumEuDist; // Summe Euklidische Distanz
		ArrayList<Double> differences = new ArrayList<Double>();
		for (int j = 0; j < nodes; j++) {
			sumEuDist = 0;
			for (int i = 0; i < inputv.length; i++) {
				if (!Double.isNaN(inputv[i]))
					sumEuDist += (inputv[i] - weights[i][j]) * (inputv[i] - weights[i][j]);
				// Math.pow(inputv[i] - weights[i][j], 2);
			}
			bias = gamma * (nodes * gewinnFrequenz[j] - 1);
			eudist[j] = sumEuDist + bias;
			differences.add(sumEuDist + bias);
		}
		return differences;
	}
	
	private ArrayList<Double> calcEuDisOhneBIAS() {
		double sumEuDist; // Summe Euklidische Distanz
		ArrayList<Double> differences = new ArrayList<Double>();
		for (int j = 0; j < nodes; j++) {
			sumEuDist = 0;
			// System.out.println("Centroid "+(j+1));
			for (int i = 0; i < inputv.length; i++) {
				if (!new Double(inputv[i]).isNaN()) {
					sumEuDist += Math.pow(inputv[i] - weights[i][j], 2);
				}
			}
			eudist[j] = sumEuDist;
			differences.add(sumEuDist);
		}
		return differences;
	}
	
	private void calcNachbarschaft(int time, int type, double maxNachbar) {
		
		int xZ, yZ, xJ, yJ;
		// Position des Gewinnerneurons und des aktuellen Neurons auf
		// der nun als 2d Karte angenommenen Karte
		
		xZ = minEuDist % breite; // x-Koordinate entspricht index mod breite
		yZ = minEuDist / breite; // y-Koordinate entspricht index div breite
		
		for (int j = 0; j < nodes; j++) {
			xJ = j % breite;
			yJ = j / breite;
			
			double abstand = Math.sqrt(Math.pow(xZ - xJ, 2) + Math.pow(yZ - yJ, 2));
			
			if ((j == minEuDist) || (abstand <= maxNachbar) || (maxNachbar < 0)) {
				
				abstand = abstand / (maxNachbar + 1); // Normieren auf
				// Ausbreitungsradius;
				
				if (type == 1) { // ZYLINDER
					if (abstand < 1)
						nachbar[j] = 1;
					else
						nachbar[j] = 0;
				} else
					if (type == 2) { // KEGEL
						if (abstand < 1)
							nachbar[j] = 1 - abstand;
						else
							nachbar[j] = 0;
					} else
						if (type == 3) { // GAUSS
							nachbar[j] = Math.exp(-Math.pow(abstand, 2));
						} else
							if (type == 4) { // MEXICAN HAT
								nachbar[j] = (1 - Math.pow(abstand, 2)) * Math.exp(-Math.pow(abstand, 2));
							} else
								if (type == 5) { // COSINUS
									if (abstand < 1)
										nachbar[j] = Math.cos(abstand * Math.PI / 2);
									else
										nachbar[j] = 0;
								} else
									ErrorMsg.addErrorMessage("FEHLER: ungültige Nachbarschaftsfunktion gewählt");
			} else
				nachbar[j] = 0;
			// Gaus-Funktion: e^(-abstand^2);
		}
	}
	
	private void calcNewWeights(int time) {
		for (int j = 0; j < nodes; j++) {
			for (int inpL = 0; inpL < inputv.length; inpL++) {
				if (!new Double(inputv[inpL]).isNaN())
					weights[inpL][j] += lernrate * nachbar[j] * (inputv[inpL] - weights[inpL][j]);
			}
		}
	}
	
	private int findNetZ() {
		minEuDist = 0;
		double curMin = Double.MAX_VALUE;
		for (int nodeIndex = 0; nodeIndex < eudist.length; nodeIndex++) {
			if (eudist[nodeIndex] < curMin && !ignoreNodes[nodeIndex]) {
				minEuDist = nodeIndex;
				curMin = eudist[nodeIndex];
			}
		}
		return minEuDist;
	}
	
	// ermittelt das Eregungszentrum der SOM anhand der Eingabedaten
	public int getNetZ(double oNew[], ObjectRef optDifferenceToCentroids) {
		inputv = oNew;
		ArrayList<Double> diffs = calcEuDisOhneBIAS();
		if (optDifferenceToCentroids != null)
			optDifferenceToCentroids.setObject(diffs);
		return findNetZ();
	}
	
	public void printMatrix() {
		System.out.println("Gewichtsmatrix >>");
		for (int j = 0; j < eudist.length; j++) {
			System.out.print("> ");
			for (int inpL = 0; inpL < inputv.length; inpL++) {
				double dummy = Math.round(weights[inpL][j] * 10000);
				System.out.print((dummy / 10000) + " ");
			}
			System.out.println("");
		}
		System.out.println("Ausbreitung >>");
		System.out.print("> ");
		for (int j = 0; j < eudist.length; j++) {
			double dummy = Math.round(nachbar[j] * 10000);
			System.out.print((dummy / 10000) + " ");
		}
		System.out.println("");
		System.out.println("EuDist >>");
		System.out.print("> ");
		for (int j = 0; j < eudist.length; j++) {
			double dummy = Math.round(eudist[j] * 10000);
			System.out.print((dummy / 10000) + " ");
		}
		System.out.println("");
	}
	
	// Zufallsinitialisierung der Gewichte innerhalb der Matrix
	void randomize() {
		for (int j = 0; j < eudist.length; j++) {
			for (int inpL = 0; inpL < inputv.length; inpL++) {
				weights[inpL][j] = Math.random() * 2 - 1; // zwischen -1 und 1
			}
			
			gewinnFrequenz[j] = 1d / nodes;
			
		}
	}
	
	private void setLernrate(int time, int maxtime) {
		lernrate = initLernrate * (maxtime - time) / maxtime;
	}
	
	public double[][] getWeights() {
		return weights;
	}
	
	public void setIgnoreNode(int nodeIndex, boolean ignore) {
		ignoreNodes[nodeIndex] = ignore;
	}
	
	public void setTargetClusterForNode(int nodeIndex, int targetID) {
		targetClusterIDforSomNode[nodeIndex] = targetID;
	}
	
	public int getTargetClusterForNode(int nodeIndex) {
		return targetClusterIDforSomNode[nodeIndex];
	}
}
