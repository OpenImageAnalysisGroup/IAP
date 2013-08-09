/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.awt.Color;
import java.util.HashMap;

public enum MeasurementNodeType {
	OMICS("Omics", new Color(255, 153, 153)),
	VOLUME("Volume", new Color(100, 190, 250)),
	IMAGE("Image", new Color(160, 200, 125)),
	NETWORK("Network", new Color(204, 204, 0));
	
	private String name;
	private Color color;
	
	MeasurementNodeType(String name, Color color) {
		this.name = name;
		this.color = color;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public Color getColor() {
		return color;
	}
	
	public static HashMap<MeasurementNodeType, Integer> getAttributeType(String name) {
		HashMap<MeasurementNodeType, Integer> types = new HashMap<MeasurementNodeType, Integer>();
		if (name == null)
			return types;
		for (String type : name.split(";")) {
			if (type.indexOf(":") < 0)
				continue;
			String typestring = type.substring(0, type.indexOf(":"));
			for (MeasurementNodeType t : values())
				if (t.toString().equalsIgnoreCase(typestring))
					types.put(t, Integer.parseInt(type.substring(type.indexOf(":") + 1)));
		}
		return types;
	}
	
	public static String toString(HashMap<MeasurementNodeType, Integer> types) {
		String text = "";
		for (MeasurementNodeType t : types.keySet()) {
			text += t.toString() + ":" + types.get(t) + ";";
		}
		return text;
	}
	
	public static MeasurementNodeType getTypeFromColor(Color color) {
		for (MeasurementNodeType t : values())
			if (t.color == color)
				return t;
		return null;
	}
	
	public String toPluralString() {
		return this.toString() + (this == MeasurementNodeType.OMICS ? "" : "s");
	}
	
	public static MeasurementNodeType[] binaryTypes() {
		return new MeasurementNodeType[] {
				VOLUME, IMAGE, NETWORK
		};
	}
	
}
