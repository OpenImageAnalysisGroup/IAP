/*
 * Created on 06.12.2004 by Christian Klukas
 */
package org.graffiti.plugins.ios.importers.gml;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MyBoolean {
	boolean value = false;
	
	public MyBoolean(String val) {
		if (val.equals("0"))
			value = false;
		else
			if (val.equals("1"))
				value = true;
			else
				value = Boolean.parseBoolean(val);
	}
	
	public boolean booleanValue() {
		return value;
	}
}
