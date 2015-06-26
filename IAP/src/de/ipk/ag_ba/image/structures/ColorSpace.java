package de.ipk.ag_ba.image.structures;

import org.NiceNameSupport;

import de.ipk.ag_ba.image.operation.channels.Channel;

public enum ColorSpace implements NiceNameSupport {
	RGB, LAB, HSV, XYZ, CMYK, GRAYSCALE_RGB_BLUE, LAB_UNSHIFTED;
	
	public Channel[] getChannels() {
		switch (this) {
			case CMYK:
				throw new RuntimeException("Not implemented yet.");
			case GRAYSCALE_RGB_BLUE:
				throw new RuntimeException("Not implemented yet.");
			case HSV:
				return new Channel[] { Channel.HSV_H, Channel.HSV_S, Channel.HSV_V };
			case LAB:
				return new Channel[] { Channel.LAB_L, Channel.LAB_A, Channel.LAB_B };
			case LAB_UNSHIFTED:
				return new Channel[] { Channel.LAB_L, Channel.LAB_A, Channel.LAB_B };
			case RGB:
				return new Channel[] { Channel.RGB_R, Channel.RGB_G, Channel.RGB_B };
			case XYZ:
				return new Channel[] { Channel.XYZ_X, Channel.XYZ_Y, Channel.XYZ_Z };
		}
		return null;
	}
	
	@Override
	public String getNiceName() {
		if (this == GRAYSCALE_RGB_BLUE)
			return "GRAYSCALE";
		else
			return this.name();
	}
	
	public static ColorSpace valueOfNiceString(String calculationMode) {
		if ("GRAYSCALE".equals(calculationMode))
			return GRAYSCALE_RGB_BLUE;
		else
			return valueOf(calculationMode);
	}
	
	public boolean isImplemented() {
		if (this == GRAYSCALE_RGB_BLUE || this == CMYK)
			return false;
		else
			return true;
	}
}
