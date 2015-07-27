package de.ipk.ag_ba.image.structures;

/**
 * @author klukas
 */
public class ImageSetConfig {
	int knownImages = 0;
	public boolean dVis;
	public boolean dFluo;
	public boolean dNir;
	public boolean dIr;
	
	public boolean doVis() {
		return dVis;
	}
	
	public boolean doFluo() {
		return dFluo;
	}
	
	public boolean doNir() {
		return dNir;
	}
	
	public boolean doIr() {
		return dIr;
	}
	
	public int getN() {
		int n = 0;
		if (doVis())
			n++;
		if (doFluo())
			n++;
		if (doNir())
			n++;
		if (doIr())
			n++;
		return n;
	}
	
}
