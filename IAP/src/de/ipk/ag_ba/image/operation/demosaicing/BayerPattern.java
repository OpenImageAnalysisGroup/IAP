package de.ipk.ag_ba.image.operation.demosaicing;

import org.Vector2i;

/**
 * @author klukas
 */
public enum BayerPattern {
	RGGB, GRBG, GBRG, BGGR;
	
	public int ParamRedX() {
		return ParamRedXY().x;
	}
	
	public int ParamRedY() {
		return ParamRedXY().y;
	}
	
	public Vector2i ParamRedXY() {
		switch (this) {
			case RGGB:
				return new Vector2i(0, 0);
			case GRBG:
				return new Vector2i(1, 0);
			case GBRG:
				return new Vector2i(0, 1);
			case BGGR:
				return new Vector2i(1, 1);
		}
		throw new RuntimeException("Internal error: unknown Bayer pattern '" + this.toString() + "'");
	}
}
