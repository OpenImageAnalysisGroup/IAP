package de.ipk.ag_ba.commands.load_lt;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * this a info-file, e.g.: <br>
 * 
 * <pre>
 * Example 1:
 * IdTag: Plant ID
 * Color: 0
 * Creator: User Name
 * Comment:
 * Measurement: Measurement Label
 * Timestamp: 9/20/2010 2:26:09 PM
 * Weight before [g]: 5
 * Weight after [g]: 5
 * Water amount [ml]: 0
 * </pre>
 * 
 * Example 2: <br>
 * 
 * <pre>
 * Camera label: FLUO SV1
 * MM pro pixel X: 0
 * MM pro pixel Y: 0
 * </pre>
 * 
 * @author klukas
 */
public class InfoFile extends LinkedHashMap<String, String> {
	private static final long serialVersionUID = 1L;
	
	public InfoFile(TextFile textFile) {
		for (String s : textFile) {
			if (s.contains(":")) {
				String[] keyAndValue = s.split(":", 2);
				put(keyAndValue[0].trim(), keyAndValue[1].trim());
			}
		}
	}
	
	public Integer getWaterAmountData() {
		try {
			int waterAmount = Integer.parseInt(get("Water amount [ml]"));
			return waterAmount;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Double getWeightBeforeData() {
		try {
			double weightBefore = Double.parseDouble(get("Weight before [g]"));
			return weightBefore;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Double getWeightAfterData() {
		try {
			double weightAfter = Double.parseDouble(get("Weight after [g]"));
			return weightAfter;
		} catch (Exception e) {
			return null;
		}
	}
	
	private static DateFormat df = new SimpleDateFormat("M/d/yyyy kk:mm:ss aa", Locale.US);
	
	public long getTimestampTime() throws ParseException {
		// example: 9/20/2010 2:26:09 PM
		String time = get("Timestamp");
		Date result = df.parse(time);
		return result.getTime();
	}
	
	public Double getXfactor() {
		try {
			double xFactor = Double.parseDouble(get("MM pro pixel X"));
			return xFactor;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Double getYfactor() {
		try {
			double yFactor = Double.parseDouble(get("MM pro pixel Y"));
			return yFactor;
		} catch (Exception e) {
			return null;
		}
	}
}
