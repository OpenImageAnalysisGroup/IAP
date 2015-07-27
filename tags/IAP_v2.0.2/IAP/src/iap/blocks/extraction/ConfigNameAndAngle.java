package iap.blocks.extraction;

/**
 * @author Christian Klukas
 */
public class ConfigNameAndAngle {
	public String name;
	public double angle;
	public double correctedAngle;
	
	public ConfigNameAndAngle(String name, double angle) {
		this.name = name;
		this.angle = angle;
		this.correctedAngle = angle;
		if (correctedAngle >= 180)
			correctedAngle = correctedAngle - 180;
	}
}
