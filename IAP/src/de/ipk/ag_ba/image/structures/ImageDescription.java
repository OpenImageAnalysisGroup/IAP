package de.ipk.ag_ba.image.structures;

public class ImageDescription {
	
	boolean isNull = false;
	int w, h;
	
	public ImageDescription(Image baseForDescription) {
		if (baseForDescription == null) {
			isNull = true;
			w = 0;
			h = 0;
		} else {
			isNull = false;
			w = baseForDescription.getWidth();
			h = baseForDescription.getHeight();
		}
	}
	
	public String getChange(String desc, Image updated) {
		String res = "";
		if (!isNull && updated == null) {
			res = res += ", " + desc + " set to null";
		}
		if (updated != null) {
			if (!isNull) {
				if (w != updated.getWidth())
					res = ", " + desc + " has new width (" + w + " ==> " + updated.getWidth() + ")";
				if (h != updated.getHeight())
					res = ", " + desc + " has new height (" + h + " ==> " + updated.getHeight() + ")";
			}
		}
		if (res != null && res.length() > ", ".length())
			res = res.substring(", ".length());
		return res;
	}
	
}
