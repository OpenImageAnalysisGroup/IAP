/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.util.ArrayList;

public class TranspathGene extends TranspathEntity {
	public String NAME, CREATOR, UPDATOR, SECID, SPECIES;
	public ArrayList<String> SYNONYMS = new ArrayList<String>();
	public ArrayList<String> GROUPS_ITEM = new ArrayList<String>();
	public ArrayList<String> RKOUTS_ITEM = new ArrayList<String>();
	public ArrayList<String> RKINS_ITEM = new ArrayList<String>();
	public ArrayList<String> REFERENCES_ITEM = new ArrayList<String>();
	public ArrayList<String> MEMBERS_ITEM = new ArrayList<String>();
	public ArrayList<String> ACCNOS = new ArrayList<String>();
	
	public String getKey() {
		return NAME; // e.g. TFPA36117
	}
	
	public String getXMLstartEndEntity() {
		return "Gene";
	}
	
	@Override
	public void processXMLentityValue(String environment, String value) {
		if (environment.equals("/groups/item"))
			GROUPS_ITEM.add(value);
		else
			if (environment.equals("/name"))
				NAME = checkAndSet(NAME, value);
			else
				if (environment.equals("/updator"))
					UPDATOR = checkAndSet(UPDATOR, value);
				else
					if (environment.equals("/rkouts/item"))
						RKOUTS_ITEM.add(value);
					else
						if (environment.equals("/secid"))
							SECID = checkAndSet(SECID, value);
						else
							if (environment.equals("/rkins/item"))
								RKINS_ITEM.add(value);
							else
								if (environment.equals("/species"))
									SPECIES = checkAndSet(SPECIES, value);
								else
									if (environment.equals("/references/item"))
										REFERENCES_ITEM.add(value);
									else
										if (environment.equals("/members/item"))
											MEMBERS_ITEM.add(value);
										else
											if (environment.equals("/accnos"))
												ACCNOS.add(value);
											else
												if (environment.equals("/synonyms"))
													SYNONYMS.add(value);
												else
													if (environment.equals("/creator"))
														CREATOR = checkAndSet(CREATOR, value);
													else {
														System.out.println("UNKNOWN TRANSPATH GENE XML INFORMATION: " + environment + " (value: " + value + ")");
													}
	}
}
