/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.10.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class EnzymeEntry {
	private String id = null;
	private String de = null;
	private List<String> an = new ArrayList<String>();
	private List<String> ca = new ArrayList<String>();
	private List<String> cf = new ArrayList<String>();
	private List<String> cc = new ArrayList<String>();
	private List<String> di = new ArrayList<String>();
	
	private static String idTag = "ID";
	private static String deTag = "DE";
	private static String anTag = "AN";
	private static String caTag = "CA";
	private static String cfTag = "CF";
	private static String ccTag = "CC   -!-";
	private static String ccTag2 = "CC";
	private static String diTag = "DI";
	
	public static boolean isValidEnzymeStart(String line) {
		boolean res = (line != null && line.startsWith(idTag));
		return res;
	}
	
	public void processInputLine(String line) {
		if (line == null || line.length() < 3)
			return;
		if (line.endsWith("."))
			line = line.substring(0, line.length() - 1); // remove last point
		if (line.startsWith(idTag))
			id = line.substring(idTag.length()).trim();
		else
			if (line.startsWith(deTag))
				de = line.substring(deTag.length()).trim();
			else
				if (line.startsWith(anTag))
					an.add(line.substring(anTag.length()).trim());
				else
					if (line.startsWith(caTag))
						ca.add(line.substring(caTag.length()).trim());
					else
						if (line.startsWith(cfTag))
							cf.add(line.substring(cfTag.length()).trim());
						else
							if (line.startsWith(ccTag))
								cc.add(line.substring(ccTag.length()).trim());
							else
								if (line.startsWith(diTag))
									di.add(line.substring(diTag.length()).trim());
		// else if (line.startsWith(prTag))
		// pr.add(line.substring(prTag.length()).trim());
		// else if (line.startsWith(drTag))
		// dr.add(line.substring(drTag.length()).trim());
		// all other types are ignored
	}
	
	public boolean isValid() {
		return id != null && de != null;
	}
	
	/**
	 * @return Identification (EC Number)
	 */
	public String getID() {
		return id;
	}
	
	/**
	 * @return Description (Official name)
	 */
	public String getDE() {
		return de;
	}
	
	/**
	 * @return Alternative names
	 */
	public List<String> getAN() {
		return an;
	}
	
	/**
	 * @return Catalytic activities
	 */
	public List<String> getCA() {
		return ca;
	}
	
	/**
	 * @return Cofactor(s)
	 */
	public List<String> getCF() {
		return cf;
	}
	
	/**
	 * @return Comments
	 */
	public List<String> getCC() {
		return cc;
	}
	
	/**
	 * @return Diesease(s) associated with this enzyme (human)
	 */
	public List<String> getDI() {
		return di;
	}
	
	public static String trimKnownPrefixes(String inp) {
		if (inp.startsWith(idTag))
			inp = inp.substring(idTag.length());
		else
			if (inp.startsWith(deTag))
				inp = inp.substring(deTag.length());
			else
				if (inp.startsWith(anTag))
					inp = inp.substring(anTag.length());
				else
					if (inp.startsWith(caTag))
						inp = inp.substring(caTag.length());
					else
						if (inp.startsWith(cfTag))
							inp = inp.substring(cfTag.length());
						else
							if (inp.startsWith(ccTag))
								inp = inp.substring(ccTag.length());
							else
								if (inp.startsWith(ccTag2))
									inp = inp.substring(ccTag2.length());
								else
									if (inp.startsWith(diTag))
										inp = inp.substring(diTag.length());
		return inp;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EnzymeEntry))
			return super.equals(obj);
		EnzymeEntry oe = (EnzymeEntry) obj;
		return id.equals(oe.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
}
