package de.ipk.ag_ba.server.gwt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.TreeMap;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.experiment.process.report.DateDoubleString;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

public class SnapshotDataIAP {
	private static final Double NO_ANGLE = -720d;
	
	public String plantId;
	
	public transient StringBuilder rgbUrl = new StringBuilder();
	public transient StringBuilder fluoUrl = new StringBuilder();
	public transient StringBuilder nirUrl = new StringBuilder();
	public transient StringBuilder irUrl = new StringBuilder();
	public transient StringBuilder unknownUrl = new StringBuilder();
	
	public transient StringBuilder rgbUrlAngle = new StringBuilder();
	public transient StringBuilder fluoUrlAngle = new StringBuilder();
	public transient StringBuilder nirUrlAngle = new StringBuilder();
	public transient StringBuilder irUrlAngle = new StringBuilder();
	public transient StringBuilder unknownUrlAngle = new StringBuilder();
	
	public String dataTransport;
	public TreeMap<Double, LinkedList<BooleanDouble>> storeAngleToValues;
	
	public Long snapshotTime;
	public int day;
	
	public Double weight_before, weight_after;
	
	public Integer water_amount_whole_day;
	
	public String condition, timePoint;
	
	public String species, genotype, variety, growthCondition, treatment, sequence;
	
	private transient TreeMap<Double, TreeMap<Integer, Double>> position2store;
	private transient TreeMap<Double, TreeMap<Integer, Boolean>> position2isUpperTrueIsLowerFalse;
	
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
	
	public long[] getRgbUrlArr() {
		return getLongArray(rgbUrl);
	}
	
	public long[] getFluoUrlArr() {
		return getLongArray(fluoUrl);
	}
	
	public long[] getNirUrlArr() {
		return getLongArray(nirUrl);
	}
	
	public long[] getIrUrlArr() {
		return getLongArray(irUrl);
	}
	
	public long[] getUnknownUrlArr() {
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
	
	public int[] getIrUrlAngleArr() {
		return getIntArray(getIrUrlAngle());
	}
	
	public int[] getUnknownUrlAngleArr() {
		return getIntArray(getUnknownUrlAngle());
	}
	
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
	
	public Integer getWholeDayWaterAmount() {
		return water_amount_whole_day;
	}
	
	public void setWholeDayWaterAmount(Integer water_amount) {
		this.water_amount_whole_day = water_amount;
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
		addTo(rgbUrl, rgbUrlAngle, url, angle);
	}
	
	public void addFluo(long url, int angle) {
		addTo(fluoUrl, fluoUrlAngle, url, angle);
	}
	
	private void addTo(StringBuilder urlSB, StringBuilder urlAngle, long url, int angle) {
		if (urlSB.length() > 0) {
			urlSB.append(":");
			urlAngle.append(":");
		}
		urlSB.append(url);
		urlAngle.append(angle);
	}
	
	public void addNir(long url, int angle) {
		addTo(nirUrl, nirUrlAngle, url, angle);
	}
	
	public void addIr(long url, int angle) {
		addTo(irUrl, irUrlAngle, url, angle);
	}
	
	public boolean hasImages() {
		return rgbUrl.length() > 0 || fluoUrl.length() > 0 || nirUrl.length() > 0;
	}
	
	public void addUnknown(long url, Integer angle) {
		addTo(unknownUrl, unknownUrlAngle, url, angle);
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
	private long[] getLongArray(StringBuilder urlString) {
		String[] oo = urlString.length() > 0 ? urlString.toString().split(":") : new String[] {};
		long[] rl = new long[oo.length];
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
	
	public String getUrlList(UrlCacheManager urlManager, CameraType ct, double angle) {
		return getUrlInfo(urlManager, ct, angle, 0);
	}
	
	private String getConfigList(UrlCacheManager urlManager, CameraType ct, double angle) {
		return getUrlInfo(urlManager, ct, angle, 1);
	}
	
	public ArrayList<BinaryMeasurement> getBinaryList(UrlCacheManager urlManager, CameraType ct, double angle) {
		ArrayList<BinaryMeasurement> bms = new ArrayList<BinaryMeasurement>();
		StringBuilder s = getUrlFromType(ct);
		if (s.length() == 0)
			return bms;
		else {
			StringBuilder sAngle = getUrlAngleFromType(ct);
			
			long[] urlArray = getLongArray(s);
			int[] urlAngles = getIntArray(sAngle);
			for (int i = 0; i < Math.min(urlArray.length, urlAngles.length); i++) {
				String realUrl = urlManager.getUrl(urlArray[i]);
				int urlAngle = urlAngles[i];
				if (realUrl != null && Math.abs(urlAngle - angle) < 0.00001) {
					BinaryMeasurement b = urlManager.getImage(urlArray[i]);
					bms.add(b);
				}
			}
		}
		return bms;
	}
	
	private String getUrlInfo(UrlCacheManager urlManager, CameraType ct, double angle, int infoIndex) {
		StringBuilder s = getUrlFromType(ct);
		if (s.length() == 0)
			return null;
		else {
			StringBuilder sAngle = getUrlAngleFromType(ct);
			
			StringBuilder r = new StringBuilder();
			
			long[] urlArray = getLongArray(s);
			int[] urlAngles = getIntArray(sAngle);
			for (int i = 0; i < Math.min(urlArray.length, urlAngles.length); i++) {
				String realUrl = urlManager.getUrl(urlArray[i]);
				int urlAngle = urlAngles[i];
				if (realUrl != null && Math.abs(urlAngle - angle) < 0.00001) {
					if (r.length() > 0)
						r.append("|");
					r.append(realUrl.split("\\+\\+\\+")[infoIndex]);
				}
			}
			return r.toString();
		}
	}
	
	private StringBuilder getUrlFromType(CameraType ct) {
		switch (ct) {
			case VIS:
				return rgbUrl;
			case FLUO:
				return fluoUrl;
			case NIR:
				return nirUrl;
			case IR:
				return irUrl;
			case UNKNOWN:
				return unknownUrl;
			default:
				break;
		}
		return null;
	}
	
	private StringBuilder getUrlAngleFromType(CameraType ct) {
		switch (ct) {
			case VIS:
				return rgbUrlAngle;
			case FLUO:
				return fluoUrlAngle;
			case NIR:
				return nirUrlAngle;
			case IR:
				return irUrlAngle;
			case UNKNOWN:
				return unknownUrlAngle;
			default:
				break;
		}
		return null;
	}
	
	public int getRgbUrlCount() {
		if (rgbUrl.length() == 0)
			return 0;
		else
			return 1 + (rgbUrl.length() - rgbUrl.toString().replace(":", "").length());
	}
	
	public int getFluoUrlCount() {
		if (fluoUrl.length() == 0)
			return 0;
		else
			
			return 1 + (fluoUrl.length() - fluoUrl.toString().replace(":", "").length());
	}
	
	public int getNirUrlCount() {
		if (nirUrl.length() == 0)
			return 0;
		else
			return 1 + (nirUrl.length() - nirUrl.toString().replace(":", "").length());
	}
	
	public int getUnknownUrlCount() {
		if (unknownUrl.length() == 0)
			return 0;
		else
			return 1 + (unknownUrl.length() - unknownUrl.toString().replace(":", "").length());
	}
	
	public StringBuilder getRgbUrlAngle() {
		return rgbUrlAngle;
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
	
	public StringBuilder getIrUrlAngle() {
		return irUrlAngle;
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
	
	public void prepareStore() {
		storeAngleToValues = new TreeMap<Double, LinkedList<BooleanDouble>>();
		if (position2store != null && !position2store.isEmpty())
			for (Double angle : position2store.keySet())
				for (Integer i : position2store.get(angle).keySet()) {
					if (!storeAngleToValues.containsKey(angle))
						storeAngleToValues.put(angle, new LinkedList<BooleanDouble>());
					while (storeAngleToValues.get(angle).size() <= i)
						storeAngleToValues.get(angle).add(null);
					{
						Boolean flag = null;
						if (position2isUpperTrueIsLowerFalse != null && position2isUpperTrueIsLowerFalse.get(angle) != null)
							flag = position2isUpperTrueIsLowerFalse.get(angle).get(i);
						storeAngleToValues.get(angle).set(i, new BooleanDouble(position2store.get(angle).get(i), flag));
					}
				}
		
		for (CameraType ct : CameraType.values()) {
			StringBuilder ar = getUrlAngleFromType(ct);
			if (ar != null) {
				int[] ia = getIntArray(ar);
				for (int angle : ia) {
					if (!storeAngleToValues.containsKey((double) angle))
						storeAngleToValues.put((double) angle, new LinkedList<BooleanDouble>());
				}
			}
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
		
		if (storeAngleToValues != null && storeAngleToValues.size() > 0) {
			position2store = new TreeMap<Double, TreeMap<Integer, Double>>();
			for (Double angle : storeAngleToValues.keySet()) {
				position2store.put(angle, new TreeMap<Integer, Double>());
				int n = storeAngleToValues.get(angle).size();
				for (int i = 0; i < n; i++) {
					BooleanDouble d = storeAngleToValues.get(angle).get(i);
					if (d != null)
						position2store.get(angle).put(i, d.value);
				}
			}
		}
		
		if (storeAngleToValues != null && storeAngleToValues.size() > 0) {
			position2isUpperTrueIsLowerFalse = new TreeMap<>();
			for (Double angle : storeAngleToValues.keySet()) {
				position2isUpperTrueIsLowerFalse.put(angle, new TreeMap<Integer, Boolean>());
				int n = storeAngleToValues.get(angle).size();
				for (int i = 0; i < n; i++) {
					BooleanDouble d = storeAngleToValues.get(angle).get(i);
					if (d != null && d.flag != null)
						position2isUpperTrueIsLowerFalse.get(angle).put(i, d.flag);
				}
			}
		}
	}
	
	public String getCSVvalue(boolean numberFormat_deTrue_enFalse, String separator, UrlCacheManager urlManager, boolean ommit_RowType_Angle_ImageConfigs) {
		SnapshotDataIAP s = this;
		
		String weightBeforeWatering = enDe(numberFormat_deTrue_enFalse, s.getWeightBefore() != null ? s.getWeightBefore() + "" : "");
		String waterWeight = enDe(numberFormat_deTrue_enFalse, s.getWeightOfWatering() != null ? s.getWeightOfWatering() + "" : "");
		String waterAmount = enDe(numberFormat_deTrue_enFalse, s.getWholeDayWaterAmount() != null ? s.getWholeDayWaterAmount() + "" : "");
		String sumBA = enDe(numberFormat_deTrue_enFalse, s.getWeightBefore() != null && s.getWeightOfWatering() != null ?
				(s.getWeightBefore() + s.getWeightOfWatering()) + "" : "");
		
		Double fineTime;
		if (s.getSnapshotTime() != null) {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(new Date(s.getSnapshotTime()));
			
			fineTime = new Double(Integer.parseInt(StringManipulationTools.getNumbersFromString(s.getTimePoint())) +
					calendar.get(Calendar.HOUR_OF_DAY) / 24d +
					calendar.get(Calendar.MINUTE) / 60d / 24d +
					calendar.get(Calendar.SECOND) / 60d / 60d / 24d +
					calendar.get(Calendar.MILLISECOND) / 60d / 60d / 1000d / 24d);
		} else {
			fineTime = Double.parseDouble(StringManipulationTools.getNumbersFromString(s.getTimePoint()));
		}
		
		if (position2store == null) {
			// Species;Genotype;Variety;GrowthCondition;Treatment;Sequence;
			return (ommit_RowType_Angle_ImageConfigs ? "" : "no imaging" + separator + "" + separator)
					+ replaceNull(s.getPlantId()) + separator
					+ replaceNull(s.getCondition()) + separator
					+ replaceNull(s.getSpecies()) + separator
					+ replaceNull(s.getGenotype()) + separator
					+ replaceNull(s.getVariety()) + separator
					+ replaceNull(s.getGrowthCondition()) + separator
					+ replaceNull(s.getTreatment()) + separator
					+ replaceNull(s.getSequence()) + separator
					+ s.getTimePoint() + separator
					+ (s.getSnapshotTime() != null ? new Date(s.getSnapshotTime()).toString() : "") + separator
					+ StringManipulationTools.getNumbersFromString(s.getTimePoint()) + separator
					+ enDe(numberFormat_deTrue_enFalse, fineTime.toString()) + separator
					+ weightBeforeWatering + separator
					+ sumBA + separator
					+ waterWeight + separator + waterAmount
					+ (ommit_RowType_Angle_ImageConfigs ? "" :
							separator +
									replaceNull(s.getUrlList(urlManager, CameraType.VIS, NO_ANGLE)) + separator
									+ replaceNull(s.getUrlList(urlManager, CameraType.FLUO, NO_ANGLE)) + separator
									+ replaceNull(s.getUrlList(urlManager, CameraType.NIR, NO_ANGLE)) + separator
									+ replaceNull(s.getUrlList(urlManager, CameraType.IR, NO_ANGLE)) + separator
									+ replaceNull(s.getUrlList(urlManager, CameraType.UNKNOWN, NO_ANGLE)) + separator
									+ replaceNull(s.getConfigList(urlManager, CameraType.VIS, NO_ANGLE)) + separator
									+ replaceNull(s.getConfigList(urlManager, CameraType.FLUO, NO_ANGLE)) + separator
									+ replaceNull(s.getConfigList(urlManager, CameraType.NIR, NO_ANGLE)) + separator
									+ replaceNull(s.getConfigList(urlManager, CameraType.IR, NO_ANGLE)) + separator
									+ replaceNull(s.getConfigList(urlManager, CameraType.UNKNOWN, NO_ANGLE)))
					+ "\r\n";
		} else {
			StringBuilder result = new StringBuilder();
			int nmax = 0;
			for (Double angle : storeAngleToValues.keySet()) {
				int n = storeAngleToValues.get(angle).size();
				if (n > nmax)
					nmax = n;
			}
			for (Double angle : storeAngleToValues.keySet()) {
				StringBuilder columnData = new StringBuilder();
				for (int i = 0; i < nmax; i++) {
					columnData.append(separator);
					Double v = null;
					v = i < storeAngleToValues.get(angle).size() &&
							storeAngleToValues.get(angle).get(i) != null ? storeAngleToValues.get(angle).get(i).value : null;
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
				boolean addWater = false;
				String rowType = "";
				if (angle >= 0) {
					rowType = "side" + separator + enDe(numberFormat_deTrue_enFalse, angle + "");
				} else
					if (Math.abs(-angle - 1) < 0.00001) {
						rowType = "top" + separator + enDe(numberFormat_deTrue_enFalse, 0d + "");
					} else
						if (Math.abs(-angle + NO_ANGLE) < 0.00001) {
							rowType = "combined" + separator;
							addWater = true;
						} else {
							rowType = "top" + separator + enDe(numberFormat_deTrue_enFalse, (-angle) + "");
						}
				String res =
						(ommit_RowType_Angle_ImageConfigs ? "" : rowType + separator)
								+ replaceNull(s.getPlantId()) + separator
								+ replaceNull(s.getCondition()) + separator
								+ replaceNull(s.getSpecies()) + separator
								+ replaceNull(s.getGenotype()) + separator
								+ replaceNull(s.getVariety()) + separator
								+ replaceNull(s.getGrowthCondition()) + separator
								+ replaceNull(s.getTreatment()) + separator
								+ replaceNull(s.getSequence()) + separator
								+ s.getTimePoint() + separator
								+ (s.getSnapshotTime() != null ? new Date(s.getSnapshotTime()).toString() : "") + separator
								+ StringManipulationTools.getNumbersFromString(s.getTimePoint()) + separator
								+ enDe(numberFormat_deTrue_enFalse, fineTime.toString()) + separator
								+ (addWater ? weightBeforeWatering : "") + separator
								+ (addWater ? sumBA : "") + separator
								+ (addWater ? waterWeight : "") + separator
								+ (addWater ? waterAmount : "")
								+ (ommit_RowType_Angle_ImageConfigs ? "" :
										separator
												+ replaceNull(s.getUrlList(urlManager, CameraType.VIS, angle)) + separator
												+ replaceNull(s.getUrlList(urlManager, CameraType.FLUO, angle)) + separator
												+ replaceNull(s.getUrlList(urlManager, CameraType.NIR, angle)) + separator
												+ replaceNull(s.getUrlList(urlManager, CameraType.IR, angle)) + separator
												+ replaceNull(s.getUrlList(urlManager, CameraType.UNKNOWN, angle)) + separator
												+ replaceNull(s.getConfigList(urlManager, CameraType.VIS, angle)) + separator
												+ replaceNull(s.getConfigList(urlManager, CameraType.FLUO, angle)) + separator
												+ replaceNull(s.getConfigList(urlManager, CameraType.NIR, angle)) + separator
												+ replaceNull(s.getConfigList(urlManager, CameraType.IR, angle)) + separator
												+ replaceNull(s.getConfigList(urlManager, CameraType.UNKNOWN, angle)))
								+ columnData + "\r\n";
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
				return "\"" + StringManipulationTools.stringReplace(s, ";", "/") + "\""; // "\"" + s + "\"";
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
	
	public void storeValue(Integer idx, Double value, boolean isLowerOutlier, boolean isUpperOutlier) {
		if (position2store == null)
			position2store = new TreeMap<Double, TreeMap<Integer, Double>>();
		if (!position2store.containsKey(NO_ANGLE))
			position2store.put(NO_ANGLE, new TreeMap<Integer, Double>());
		position2store.get(NO_ANGLE).put(idx, value);
		
		if (isLowerOutlier || isUpperOutlier) {
			if (position2isUpperTrueIsLowerFalse == null)
				position2isUpperTrueIsLowerFalse = new TreeMap<Double, TreeMap<Integer, Boolean>>();
			if (!position2isUpperTrueIsLowerFalse.containsKey(NO_ANGLE))
				position2isUpperTrueIsLowerFalse.put(NO_ANGLE, new TreeMap<Integer, Boolean>());
			position2isUpperTrueIsLowerFalse.get(NO_ANGLE).put(idx, isUpperOutlier);
		}
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
	
	public void storeAngleValue(int idx, Double position, double value, boolean isTop, boolean isLowerOutlier, boolean isUpperOutlier) {
		if (position == null)
			if (isTop)
				position = -1d;
			else
				position = 0d;
		if (isTop && position > 0) {
			position = -position;
		}
		if (position2store == null)
			position2store = new TreeMap<Double, TreeMap<Integer, Double>>();
		if (!position2store.containsKey(position))
			position2store.put(position, new TreeMap<Integer, Double>());
		position2store.get(position).put(idx, value);
		if (isLowerOutlier || isUpperOutlier) {
			if (position2isUpperTrueIsLowerFalse == null)
				position2isUpperTrueIsLowerFalse = new TreeMap<Double, TreeMap<Integer, Boolean>>();
			if (!position2isUpperTrueIsLowerFalse.containsKey(position))
				position2isUpperTrueIsLowerFalse.put(position, new TreeMap<Integer, Boolean>());
			position2isUpperTrueIsLowerFalse.get(position).put(idx, isUpperOutlier);
		}
	}
	
	public ArrayList<ArrayList<DateDoubleString>> getCSVobjects(UrlCacheManager urlManager) {
		SnapshotDataIAP s = this;
		
		Double fineTime;
		if (s.getSnapshotTime() != null) {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(new Date(s.getSnapshotTime()));
			
			fineTime = new Double(Integer.parseInt(StringManipulationTools.getNumbersFromString(s.getTimePoint())) +
					calendar.get(Calendar.HOUR_OF_DAY) / 24d +
					calendar.get(Calendar.MINUTE) / 60d / 24d +
					calendar.get(Calendar.SECOND) / 60d / 60d / 24d +
					calendar.get(Calendar.MILLISECOND) / 60d / 60d / 1000d / 24d);
		} else {
			fineTime = Double.parseDouble(StringManipulationTools.getNumbersFromString(s.getTimePoint()));
		}
		ArrayList<ArrayList<DateDoubleString>> result = new ArrayList<ArrayList<DateDoubleString>>();
		if (position2store == null) {
			ArrayList<DateDoubleString> row = new ArrayList<DateDoubleString>();
			row.add(new DateDoubleString("no imaging"));
			row.add(null);
			row.add(new DateDoubleString(s.getPlantId()));
			row.add(new DateDoubleString(s.getCondition()));
			row.add(new DateDoubleString(s.getSpecies()));
			row.add(new DateDoubleString(s.getGenotype()));
			row.add(new DateDoubleString(s.getVariety()));
			row.add(new DateDoubleString(s.getGrowthCondition()));
			row.add(new DateDoubleString(s.getTreatment()));
			row.add(new DateDoubleString(s.getSequence()));
			row.add(new DateDoubleString(s.getTimePoint()));
			if (s.getSnapshotTime() != null)
				row.add(new DateDoubleString(new Date(s.getSnapshotTime())));
			else
				row.add(new DateDoubleString(-1));
			row.add(new DateDoubleString(Double.parseDouble(StringManipulationTools.getNumbersFromString(s.getTimePoint()))));
			row.add(new DateDoubleString(fineTime));
			row.add(new DateDoubleString(s.getWeightBefore()));
			row.add(new DateDoubleString(s.getWeightBefore() != null && s.getWeightOfWatering() != null ? s.getWeightBefore() + s.getWeightOfWatering() : null));
			row.add(new DateDoubleString(s.getWeightOfWatering()));
			row.add(new DateDoubleString(s.getWholeDayWaterAmount()));
			row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.VIS, NO_ANGLE))));
			row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.FLUO, NO_ANGLE))));
			row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.NIR, NO_ANGLE))));
			row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.IR, NO_ANGLE))));
			row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.UNKNOWN, NO_ANGLE))));
			row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.VIS, NO_ANGLE)));
			row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.FLUO, NO_ANGLE)));
			row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.NIR, NO_ANGLE)));
			row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.IR, NO_ANGLE)));
			row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.UNKNOWN, NO_ANGLE)));
			result.add(row);
		} else {
			for (Double angle : storeAngleToValues.keySet()) {
				ArrayList<DateDoubleString> row = new ArrayList<DateDoubleString>();
				boolean addWater = false;
				if (angle >= 0) {
					row.add(new DateDoubleString("side"));
					row.add(new DateDoubleString(angle));
				} else
					if (Math.abs(-angle - 1) < 0.00001) {
						row.add(new DateDoubleString("top"));
						row.add(new DateDoubleString(0));
					} else
						if (Math.abs(-angle + NO_ANGLE) < 0.00001) {
							row.add(new DateDoubleString("combined"));
							row.add(null);
							addWater = true;
						} else {
							row.add(new DateDoubleString("top"));
							row.add(new DateDoubleString(-angle));
						}
				
				row.add(new DateDoubleString(s.getPlantId()));
				row.add(new DateDoubleString(s.getCondition()));
				row.add(new DateDoubleString(s.getSpecies()));
				row.add(new DateDoubleString(s.getGenotype()));
				row.add(new DateDoubleString(s.getVariety()));
				row.add(new DateDoubleString(s.getGrowthCondition()));
				row.add(new DateDoubleString(s.getTreatment()));
				row.add(new DateDoubleString(s.getSequence()));
				row.add(new DateDoubleString(s.getTimePoint()));
				row.add(s.getSnapshotTime() != null ? new DateDoubleString(new Date(s.getSnapshotTime())) : null);
				row.add(new DateDoubleString(Double.parseDouble(StringManipulationTools.getNumbersFromString(s.getTimePoint()))));
				row.add(new DateDoubleString(fineTime));
				row.add(addWater ? new DateDoubleString(s.getWeightBefore()) : null);
				row.add(addWater ? new DateDoubleString(s.getWeightBefore() != null && s.getWeightOfWatering() != null ? s.getWeightBefore()
						+ s.getWeightOfWatering() : null) : null);
				row.add(addWater ? new DateDoubleString(s.getWeightOfWatering()) : null);
				row.add(addWater ? new DateDoubleString(s.getWholeDayWaterAmount()) : null);
				row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.VIS, angle), s.getBinaryList(urlManager, CameraType.VIS, angle))));
				row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.FLUO, angle), s.getBinaryList(urlManager, CameraType.FLUO, angle))));
				row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.NIR, angle), s.getBinaryList(urlManager, CameraType.NIR, angle))));
				row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.IR, angle), s.getBinaryList(urlManager, CameraType.IR, angle))));
				row.add(addLinks(new DateDoubleString(s.getUrlList(urlManager, CameraType.UNKNOWN, angle), s.getBinaryList(urlManager, CameraType.UNKNOWN, angle))));
				row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.VIS, angle)));
				row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.FLUO, angle)));
				row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.NIR, angle)));
				row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.IR, angle)));
				row.add(new DateDoubleString(s.getConfigList(urlManager, CameraType.UNKNOWN, angle)));
				int n = storeAngleToValues.get(angle).size();
				for (int i = 0; i < n; i++) {
					BooleanDouble v = storeAngleToValues.get(angle).get(i);
					if (v != null && !Double.isNaN(v.value) && !Double.isInfinite(v.value)) {
						row.add(new DateDoubleString(v.value, v.flag));
					} else
						row.add(null);
				}
				result.add(row);
			}
		}
		return result;
	}
	
	private DateDoubleString addLinks(DateDoubleString dateDoubleString) {
		dateDoubleString.checkForLink();
		return dateDoubleString;
	}
	
	public String getFieldValue(ConditionInfo param) {
		switch (param) {
			case FILES:
				return null;
			case GENOTYPE:
				return genotype;
			case GROWTHCONDITIONS:
				return growthCondition;
			case IGNORED_FIELD:
				return null;
			case SEQUENCE:
				return sequence;
			case SPECIES:
				return species;
			case TREATMENT:
				return treatment;
			case VARIETY:
				return variety;
			default:
				break;
		}
		return null;
	}
}
