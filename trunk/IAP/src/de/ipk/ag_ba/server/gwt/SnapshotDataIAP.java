package de.ipk.ag_ba.server.gwt;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import de.ipk.ag_ba.gui.actions.DateDoubleString;

public class SnapshotDataIAP {
	private static final Double NO_ANGLE = -720d;
	
	public String plantId;
	
	public transient StringBuilder rgbUrl = new StringBuilder();
	public transient StringBuilder fluoUrl = new StringBuilder();
	public transient StringBuilder nirUrl = new StringBuilder();
	public transient StringBuilder unknownUrl = new StringBuilder();
	
	public transient StringBuilder rgbUrlAngle = new StringBuilder();
	public transient StringBuilder fluoUrlAngle = new StringBuilder();
	public transient StringBuilder nirUrlAngle = new StringBuilder();
	public transient StringBuilder unknownUrlAngle = new StringBuilder();
	
	public String dataTransport;
	public TreeMap<Double, ArrayList<Double>> storeValues;
	
	public Long snapshotTime;
	public int day;
	
	public Double weight_before, weight_after;
	
	public Integer water_amount;
	
	public String condition, timePoint;
	
	public String species, genotype, variety, growthCondition, treatment, sequence;
	
	private transient TreeMap<Double, TreeMap<Integer, Double>> position2store;
	
	public SnapshotDataIAP() {
		// empty
	}
	
	public String getPlantId() {
		return plantId;
	}
	
	public Integer getDay() {
		return day;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public String getTimePoint() {
		return timePoint;
	}
	
	public Long[] getRgbUrlArr() {
		return getLongArray(rgbUrl);
	}
	
	public Long[] getFluoUrlArr() {
		return getLongArray(fluoUrl);
	}
	
	public Long[] getNirUrlArr() {
		return getLongArray(nirUrl);
	}
	
	public Long[] getUnknownUrlArr() {
		return getLongArray(unknownUrl);
	}
	
	public int[] getRgbUrlAngleArr() {
		return getIntArray(getRgbUrlAngle());
	}
	
	public int[] getFluoUrlAngleArr() {
		return getIntArray(getFluoUrlAngle());
	}
	
	public int[] getNirUrlAngleArr() {
		return getIntArray(getNirUrlAngle());
	}
	
	public int[] getUnknownUrlAngleArr() {
		return getIntArray(getUnknownUrlAngle());
	}
	
	// public void setRgbUrl(Long[] url) {
	// rgbUrl = new ArrayList<Long>(url.length);
	// for (Long s : url)
	// rgbUrl.add(s);
	// }
	//
	// public void setFluoUrl(Long[] url) {
	// fluoUrl = new ArrayList<Long>(url.length);
	// for (Long s : url)
	// fluoUrl.add(s);
	// }
	//
	// public void setNirUrl(Long[] url) {
	// nirUrl = new ArrayList<Long>(url.length);
	// for (Long s : url)
	// nirUrl.add(s);
	// }
	//
	// public void setUnknownUrl(Long[] url) {
	// unknownUrl = new ArrayList<Long>(url.length);
	// for (Long s : url)
	// unknownUrl.add(s);
	// }
	//
	public void setSnapshotTime(Long rowId) {
		this.snapshotTime = rowId;
	}
	
	public Long getSnapshotTime() {
		return snapshotTime;
	}
	
	public Double getWeightBefore() {
		return weight_before;
	}
	
	public Double getWeightOfWatering() {
		return weight_after;
	}
	
	public void setWeightBefore(Double weight_before) {
		this.weight_before = weight_before;
	}
	
	public void setWeightAfter(Double weight_after) {
		this.weight_after = weight_after;
	}
	
	public Integer getWaterAmount() {
		return water_amount;
	}
	
	public void setTargetWaterAmount(Integer water_amount) {
		this.water_amount = water_amount;
	}
	
	public void setCondition(String conditionName) {
		this.condition = conditionName;
	}
	
	public void setTimePoint(String sampleTime) {
		this.timePoint = sampleTime;
	}
	
	public void setPlantId(String plantId) {
		this.plantId = plantId;
	}
	
	public void setDay(Integer day) {
		this.day = day;
	}
	
	public void addRgb(long url, int angle) {
		if (rgbUrl.length() > 0) {
			rgbUrl.append(":");
			rgbUrlAngle.append(":");
		}
		rgbUrl.append(url);
		rgbUrlAngle.append(angle);
	}
	
	public void addFluo(long url, int angle) {
		if (fluoUrl.length() > 0) {
			fluoUrl.append(":");
			fluoUrlAngle.append(":");
		}
		fluoUrl.append(url);
		fluoUrlAngle.append(angle);
	}
	
	public void addNir(long url, int angle) {
		if (nirUrl.length() > 0) {
			nirUrl.append(":");
			nirUrlAngle.append(":");
		}
		nirUrl.append(url);
		nirUrlAngle.append(angle);
	}
	
	public boolean hasImages() {
		return rgbUrl.length() > 0 || fluoUrl.length() > 0 || nirUrl.length() > 0;
	}
	
	public void addUnknown(long url, Integer angle) {
		if (unknownUrl.length() > 0) {
			unknownUrl.append(":");
			unknownUrlAngle.append(":");
		}
		unknownUrl.append(url);
		unknownUrlAngle.append(angle);
	}
	
	@Override
	public String toString() {
		return getPlantId() + ": " + getCondition() + ": " + getSnapshotTime();
	}
	
	// public void setRgbUrlAngle(Integer[] pos) {
	// rgbUrlAngle = new ArrayList<Integer>(pos.length);
	// for (Integer s : pos)
	// rgbUrlAngle.add(s);
	// }
	//
	// public void setFluoUrlAngle(Integer[] pos) {
	// fluoUrlAngle = new ArrayList<Integer>(pos.length);
	// for (Integer s : pos)
	// fluoUrlAngle.add(s);
	// }
	//
	// public void setNirUrlAngle(Integer[] pos) {
	// nirUrlAngle = new ArrayList<Integer>(pos.length);
	// for (Integer s : pos)
	// nirUrlAngle.add(s);
	// }
	//
	private Long[] getLongArray(StringBuilder urlString) {
		String[] oo = urlString.length() > 0 ? urlString.toString().split(":") : new String[] {};
		Long[] rl = new Long[oo.length];
		int i = 0;
		for (String o : oo)
			if (!o.isEmpty())
				rl[i++] = Long.parseLong(o);
		return rl;
	}
	
	private int[] getIntArray(StringBuilder urlAngleString) {
		String[] oo = urlAngleString.length() > 0 ? urlAngleString.toString().split(":") : new String[] {};
		int[] rl = new int[oo.length];
		int i = 0;
		for (String o : oo)
			if (!o.isEmpty())
				rl[i++] = Integer.parseInt(o);
		return rl;
	}
	
	public int getRgbUrlCnt() {
		if (rgbUrl.length() == 0)
			return 0;
		else
			return 1 + (rgbUrl.length() - rgbUrl.toString().replace(":", "").length());
	}
	
	public int getFluoUrlCnt() {
		if (fluoUrl.length() == 0)
			return 0;
		else
			
			return 1 + (fluoUrl.length() - fluoUrl.toString().replace(":", "").length());
	}
	
	public int getNirUrlCnt() {
		if (nirUrl.length() == 0)
			return 0;
		else
			return 1 + (nirUrl.length() - nirUrl.toString().replace(":", "").length());
	}
	
	public int getUnknownUrlCnt() {
		if (unknownUrl.length() == 0)
			return 0;
		else
			return 1 + (unknownUrl.length() - unknownUrl.toString().replace(":", "").length());
	}
	
	public void setRgbUrl(StringBuilder rgbUrl) {
		this.rgbUrl = rgbUrl;
	}
	
	public void setFluoUrl(StringBuilder fluoUrl) {
		this.fluoUrl = fluoUrl;
	}
	
	public void setNirUrl(StringBuilder nirUrl) {
		this.nirUrl = nirUrl;
	}
	
	public void setUnknownUrl(StringBuilder unknownUrl) {
		this.unknownUrl = unknownUrl;
	}
	
	public void setRgbUrlAngle(StringBuilder rgbUrlAngle) {
		this.rgbUrlAngle = rgbUrlAngle;
	}
	
	public StringBuilder getRgbUrlAngle() {
		return rgbUrlAngle;
	}
	
	public void setFluoUrlAngle(StringBuilder fluoUrlAngle) {
		this.fluoUrlAngle = fluoUrlAngle;
	}
	
	public StringBuilder getFluoUrlAngle() {
		return fluoUrlAngle;
	}
	
	public void setNirUrlAngle(StringBuilder nirUrlAngle) {
		this.nirUrlAngle = nirUrlAngle;
	}
	
	public StringBuilder getNirUrlAngle() {
		return nirUrlAngle;
	}
	
	public void setUnknownUrlAngle(StringBuilder unknownUrlAngle) {
		this.unknownUrlAngle = unknownUrlAngle;
	}
	
	public StringBuilder getUnknownUrlAngle() {
		return unknownUrlAngle;
	}
	
	public void setDataTransport(String dataTransport) {
		this.dataTransport = dataTransport;
	}
	
	public String getDataTransport() {
		return dataTransport;
	}
	
	public void prepareFieldsForDataTransport() {
		String s = rgbUrl.toString() + "$" + fluoUrl.toString() + "$" + nirUrl.toString() + "$" + unknownUrl.toString() + "$" +
				rgbUrlAngle.toString() + "$" + fluoUrlAngle.toString() + "$" + nirUrlAngle.toString() + "$" + unknownUrlAngle.toString();
		setDataTransport(s);
		
		prepareStore();
	}
	
	private void prepareStore() {
		storeValues = new TreeMap<Double, ArrayList<Double>>();
		if (position2store != null && !position2store.isEmpty())
			for (Double angle : position2store.keySet())
				for (Integer i : position2store.get(angle).keySet()) {
					if (!storeValues.containsKey(angle))
						storeValues.put(angle, new ArrayList<Double>());
					while (storeValues.get(angle).size() <= i)
						storeValues.get(angle).add(null);
					storeValues.get(angle).set(i, position2store.get(angle).get(i));
				}
	}
	
	public void prepareFieldsForUsageAfterDataTransport() {
		if (getDataTransport() != null) {
			String[] values = getDataTransport().split("\\$");
			int i = 0;
			if (i < values.length)
				rgbUrl.append(values[i++]);
			if (i < values.length)
				fluoUrl.append(values[i++]);
			if (i < values.length)
				nirUrl.append(values[i++]);
			if (i < values.length)
				unknownUrl.append(values[i++]);
			if (i < values.length)
				rgbUrlAngle.append(values[i++]);
			if (i < values.length)
				fluoUrlAngle.append(values[i++]);
			if (i < values.length)
				nirUrlAngle.append(values[i++]);
			if (i < values.length)
				unknownUrlAngle.append(values[i++]);
			setDataTransport(null);
		}
		
		if (storeValues != null && storeValues.size() > 0) {
			position2store = new TreeMap<Double, TreeMap<Integer, Double>>();
			for (Double angle : storeValues.keySet()) {
				position2store.put(angle, new TreeMap<Integer, Double>());
				int n = storeValues.get(angle).size();
				for (int i = 0; i < n; i++) {
					Double d = storeValues.get(angle).get(i);
					if (d != null)
						position2store.get(angle).put(i, d);
				}
			}
		}
	}
	
	public String getCSVvalue(boolean numberFormat_deTrue_enFalse, String separator) {
		SnapshotDataIAP s = this;
		
		String weightBeforeWatering = enDe(numberFormat_deTrue_enFalse, s.getWeightBefore() != null ? s.getWeightBefore() + "" : "");
		String waterWeight = enDe(numberFormat_deTrue_enFalse, s.getWeightOfWatering() != null ? s.getWeightOfWatering() + "" : "");
		String sumBA = enDe(numberFormat_deTrue_enFalse, s.getWeightBefore() != null && s.getWeightOfWatering() != null ?
				(s.getWeightBefore() + s.getWeightOfWatering()) + "" : "");
		String waterAmount = enDe(numberFormat_deTrue_enFalse, s.getWaterAmount() != null ? s.getWaterAmount() + "" : "");
		if (position2store == null) {
			// Species;Genotype;Variety;GrowthCondition;Treatment;Sequence;
			return "-720" + separator + s.getPlantId() + separator
					+ s.getCondition() + separator
					+ replaceNull(s.getSpecies()) + separator
					+ replaceNull(s.getGenotype()) + separator
					+ replaceNull(s.getVariety()) + separator
					+ replaceNull(s.getGrowthCondition()) + separator
					+ replaceNull(s.getTreatment()) + separator
					+ replaceNull(s.getSequence()) + separator
					+ s.getTimePoint() + separator + new Date(s.getSnapshotTime()).toString() + separator
					+ getNumbersFromString(s.getTimePoint())
					+ separator + weightBeforeWatering + separator + sumBA + separator
					+ waterWeight + separator + waterAmount
					+ separator + s.getRgbUrlCnt() + separator + s.getFluoUrlCnt()
					+ separator + s.getNirUrlCnt() + separator + s.getUnknownUrlCnt()
					+ "\r\n";
		} else {
			StringBuilder result = new StringBuilder();
			int nmax = 0;
			for (Double angle : storeValues.keySet()) {
				int n = storeValues.get(angle).size();
				if (n > nmax)
					nmax = n;
			}
			for (Double angle : storeValues.keySet()) {
				StringBuilder columnData = new StringBuilder();
				for (int i = 0; i < nmax; i++) {
					columnData.append(separator);
					Double v = storeValues.get(angle).get(i);
					if (v != null && !Double.isNaN(v) && !Double.isInfinite(v)) {
						if (!numberFormat_deTrue_enFalse)
							columnData.append(v);
						else {
							String ss = v + "";
							ss = ss.replace(".", ",");
							columnData.append(ss);
						}
					}
				}
				String res = enDe(numberFormat_deTrue_enFalse, angle + "") + separator +
						s.getPlantId() + separator
						+ replaceNull(s.getCondition()) + separator
						+ replaceNull(s.getSpecies()) + separator
						+ replaceNull(s.getGenotype()) + separator
						+ replaceNull(s.getVariety()) + separator
						+ replaceNull(s.getGrowthCondition()) + separator
						+ replaceNull(s.getTreatment()) + separator
						+ replaceNull(s.getSequence()) + separator
						+ s.getTimePoint() + separator +
						new Date(s.getSnapshotTime() != null ? s.getSnapshotTime() : 0).toString() + separator
						+ getNumbersFromString(s.getTimePoint()) + separator
						+ weightBeforeWatering + separator
						+ sumBA + separator
						+ waterAmount
						+ columnData
						+ "\r\n";
				result.append(res);
			}
			return result.toString();
		}
	}
	
	private String replaceNull(String s) {
		if (s == null || s.equals("null"))
			return "";
		else {
			if (s.contains(";"))
				return "\"" + s + "\"";
			else
				return s;
		}
	}
	
	private String enDe(boolean numberFormat_deTrue_enFalse, String v) {
		if (numberFormat_deTrue_enFalse) {
			String ss = v + "";
			ss = ss.replace(".", ",");
			return ss;
		} else
			return v + "";
	}
	
	public void storeValue(Integer idx, Double value) {
		if (position2store == null)
			position2store = new TreeMap<Double, TreeMap<Integer, Double>>();
		if (!position2store.containsKey(NO_ANGLE))
			position2store.put(NO_ANGLE, new TreeMap<Integer, Double>());
		position2store.get(NO_ANGLE).put(idx, value);
	}
	
	public Double getStoreValue(Integer idx) {
		if (position2store == null || !position2store.containsKey(NO_ANGLE))
			return null;
		return position2store.get(NO_ANGLE).get(idx);
	}
	
	public String getConditionLineByLine() {
		String res = getCondition();
		if (res != null) {
			res = res.replaceAll(":", "<br>");
			res = res.replaceAll("\\)", "");
			res = res.replaceAll("\\(", "<br>");
			res = res.replaceAll("/", "<br>");
		}
		return res;
	}
	
	public void setSpecies(String species) {
		this.species = species;
	}
	
	public String getSpecies() {
		return species;
	}
	
	public void setGenotype(String genotype) {
		this.genotype = genotype;
	}
	
	public String getGenotype() {
		return genotype;
	}
	
	public void setVariety(String variety) {
		this.variety = variety;
	}
	
	public String getVariety() {
		return variety;
	}
	
	public void setGrowthCondition(String growthConition) {
		this.growthCondition = growthConition;
	}
	
	public String getGrowthCondition() {
		return growthCondition;
	}
	
	public void setTreatment(String treatment) {
		this.treatment = treatment;
	}
	
	public String getTreatment() {
		return treatment;
	}
	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	final static String[] numbers = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	
	public static String getNumbersFromString(String s) {
		StringBuilder res = new StringBuilder();
		for (Character c : s.toCharArray()) {
			for (String n : numbers) {
				if (n.equals(c + "")) {
					res.append(n);
					break;
				}
			}
		}
		return res.toString();
	}
	
	public void storeAngleValue(int idx, Double position, double value) {
		if (position == null)
			position = 0d;
		if (position2store == null)
			position2store = new TreeMap<Double, TreeMap<Integer, Double>>();
		if (!position2store.containsKey(position))
			position2store.put(position, new TreeMap<Integer, Double>());
		position2store.get(position).put(idx, value);
	}
	
	public ArrayList<ArrayList<DateDoubleString>> getCSVobjects() {
		SnapshotDataIAP s = this;
		
		ArrayList<ArrayList<DateDoubleString>> result = new ArrayList<ArrayList<DateDoubleString>>();
		if (position2store == null) {
			ArrayList<DateDoubleString> row = new ArrayList<DateDoubleString>();
			row.add(new DateDoubleString(-720d));
			row.add(new DateDoubleString(s.getPlantId()));
			row.add(new DateDoubleString(s.getCondition()));
			row.add(new DateDoubleString(s.getSpecies()));
			row.add(new DateDoubleString(s.getGenotype()));
			row.add(new DateDoubleString(s.getVariety()));
			row.add(new DateDoubleString(s.getGrowthCondition()));
			row.add(new DateDoubleString(s.getTreatment()));
			row.add(new DateDoubleString(s.getSequence()));
			row.add(new DateDoubleString(s.getTimePoint()));
			row.add(new DateDoubleString(new Date(s.getSnapshotTime())));
			row.add(new DateDoubleString(Double.parseDouble(getNumbersFromString(s.getTimePoint()))));
			row.add(new DateDoubleString(s.getWeightBefore()));
			row.add(new DateDoubleString(s.getWeightBefore() != null && s.getWeightOfWatering() != null ? s.getWeightBefore() + s.getWeightOfWatering() : null));
			row.add(new DateDoubleString(s.getWeightOfWatering()));
			row.add(new DateDoubleString(s.getWaterAmount()));
			row.add(new DateDoubleString(s.getRgbUrlCnt()));
			row.add(new DateDoubleString(s.getFluoUrlCnt()));
			row.add(new DateDoubleString(s.getNirUrlCnt()));
			row.add(new DateDoubleString(s.getUnknownUrlCnt()));
			result.add(row);
		} else {
			for (Double angle : storeValues.keySet()) {
				ArrayList<DateDoubleString> row = new ArrayList<DateDoubleString>();
				row.add(new DateDoubleString(angle));
				row.add(new DateDoubleString(s.getPlantId()));
				row.add(new DateDoubleString(s.getCondition()));
				row.add(new DateDoubleString(s.getSpecies()));
				row.add(new DateDoubleString(s.getGenotype()));
				row.add(new DateDoubleString(s.getVariety()));
				row.add(new DateDoubleString(s.getGrowthCondition()));
				row.add(new DateDoubleString(s.getTreatment()));
				row.add(new DateDoubleString(s.getSequence()));
				row.add(new DateDoubleString(s.getTimePoint()));
				row.add(new DateDoubleString(new Date(s.getSnapshotTime())));
				row.add(new DateDoubleString(Double.parseDouble(getNumbersFromString(s.getTimePoint()))));
				row.add(new DateDoubleString(s.getWeightBefore()));
				row.add(new DateDoubleString(s.getWeightBefore() != null && s.getWeightOfWatering() != null ? s.getWeightBefore() + s.getWeightOfWatering() : null));
				row.add(new DateDoubleString(s.getWeightOfWatering()));
				row.add(new DateDoubleString(s.getWaterAmount()));
				row.add(new DateDoubleString(s.getRgbUrlCnt()));
				row.add(new DateDoubleString(s.getFluoUrlCnt()));
				row.add(new DateDoubleString(s.getNirUrlCnt()));
				row.add(new DateDoubleString(s.getUnknownUrlCnt()));
				int n = storeValues.get(angle).size();
				for (int i = 0; i < n; i++) {
					Double v = storeValues.get(angle).get(i);
					if (v != null && !Double.isNaN(v) && !Double.isInfinite(v)) {
						row.add(new DateDoubleString(v));
					} else
						row.add(new DateDoubleString());
				}
				result.add(row);
			}
		}
		return result;
	}
}
