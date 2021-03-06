package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MergeCompareRequirements;
import org.RunnableExecutor;
import org.StringManipulationTools;
import org.graffiti.plugin.XMLHelper;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

public class Substance implements SubstanceInterface {
	
	private String rowId, name, funcat, info, formula, substancegroup, cluster_id, spot, new_blast, new_blast_e_val,
			new_blast_score, affy_hit, score, secure, files;
	
	HashMap<Integer, String> synonyms = null; // new HashMap<Integer, String>()
	
	private final LinkedList<ConditionInterface> conditions = new LinkedList<ConditionInterface>();
	
	public Substance() {
		// empty
	}
	
	@Override
	public String getHTMLdescription() {
		StringBuilder res = new StringBuilder();
		res.append("<html><table border='0'>");
		res.append("<tr><th>Property</th><th>Value</th></tr>");
		if (name != null)
			res.append("<tr><td>name</td><td>" + name + "</td></tr>");
		if (funcat != null)
			res.append("<tr><td>funcat</td><td>" + funcat + "</td></tr>");
		if (info != null)
			res.append("<tr><td>info</td><td>" + info + "</td></tr>");
		if (formula != null)
			res.append("<tr><td>formula</td><td>" + formula + "</td></tr>");
		if (substancegroup != null)
			res.append("<tr><td>group</td><td>" + substancegroup + "</td></tr>");
		if (cluster_id != null)
			res.append("<tr><td>cluster ID</td><td>" + cluster_id + "</td></tr>");
		if (spot != null)
			res.append("<tr><td>spot</td><td>" + spot + "</td></tr>");
		if (new_blast != null)
			res.append("<tr><td>new_blast</td><td>" + new_blast + "</td></tr>");
		if (new_blast_e_val != null)
			res.append("<tr><td>new_blast_e_val</td><td>" + new_blast_e_val + "</td></tr>");
		if (new_blast_score != null)
			res.append("<tr><td>new_blast_score</td><td>" + new_blast_score + "</td></tr>");
		if (affy_hit != null)
			res.append("<tr><td>affy_hit</td><td>" + affy_hit + "</td></tr>");
		if (score != null)
			res.append("<tr><td>score</td><td>" + score + "</td></tr>");
		if (secure != null)
			res.append("<tr><td>secure</td><td>" + secure + "</td></tr>");
		if (rowId != null)
			res.append("<tr><td>row ID</td><td>" + rowId + "</td></tr>");
		res.append("</table></html>");
		return res.toString();
	}
	
	/**
	 * Whole XML dataset: <experimentdata> <experiment experimentid="-1">
	 * <experimentname>experiment name</experimentname>
	 * <remark/> <coordinator>name</coordinator> <excelfileid/>
	 * <importusername/> <importdate>Fri Apr 08 12:32:35 CEST 2005</importdate>
	 * <startdate>Tue Apr 01 00:00:00 CEST 2003</startdate>
	 * <measurements>1057</measurements> <imagefiles>0</imagefiles>
	 * <sizekb>0</sizekb> </experiment> <measurements> <substance id="column 19"
	 * name="Arg"> <line genotype="wild type" growthconditions="" id="1"
	 * name="Hordeum vulgare" treatment="Tagproben" variety=""> <sample id="1"
	 * measurementtool="HPLC" time="4" unit="day"> <average
	 * max="0.1720235400633771" min="0.07136485280999108" replicates="2"
	 * stddev="0.07117644034220516"
	 * unit="µmol / g FW">0.1216941964366841</average> <data replicates="3"
	 * unit="µmol / g FW">0.1720235400633771</data> <data replicates="3"
	 * unit="µmol / g FW">0.07136485280999108</data> </sample> ...
	 * 
	 * @param optStatus
	 */
	static Experiment getData(Element e, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		ArrayList<SubstanceInterface> result = new ArrayList<SubstanceInterface>();
		boolean debug = false;
		try {
			if (debug)
				new XMLOutputter().output(e.getDocument(), System.out);
			// only difference to setMappedData, is, that we need to receive the
			// experimentname
			// from the experiment entity instead of the line entity.
			
			List<?> childrenList = e.getChildren();
			Element experimentChildElement = null;
			for (Object o : childrenList) {
				if (o instanceof Element) {
					Element childElement = (Element) o;
					if (childElement.getName().equals("experiment")) {
						experimentChildElement = childElement;
					} else
						if (childElement.getName().equals("measurements")) {
							List cl = childElement.getChildren();
							if (optStatus != null)
								optStatus.setCurrentStatusText1("Process " + cl.size() + " substance entries");
							int idx = 0;
							int max = cl.size();
							while (!cl.isEmpty()) {
								Object o2 = cl.remove(0);
								if (o2 instanceof Element) {
									Element substanceElement = (Element) o2;
									SubstanceInterface m = Experiment.getTypeManager().getNewSubstance();
									
									if (m.setMappedData(substanceElement, experimentChildElement))
										result.add(m);
									if (optStatus != null)
										optStatus.setCurrentStatusText1("Processed " + m.getName());
								}
								idx++;
								if (optStatus != null)
									optStatus.setCurrentStatusValueFine(100d * idx / max);
							}
						}
				}
			}
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
		}
		
		return new Experiment(result);
	}
	
	/**
	 * Dataset, transformed for data mapping: <substance id="" name=""> <line
	 * experimentname="" genotype="WT" growthconditions="" id="1"
	 * name="Unknown 1" treatment="" variety=""> <sample id="" measurementtool=""
	 * time="26" unit="day"> <average max="" min="" replicates="1" stddev="0.0"
	 * unit="units">48.5313748658488</average> <data replicates=""
	 * unit="">48.5313748658488</data> </sample> ...
	 */
	@Override
	public boolean setMappedData(Element e, Element experimentChildElement) {
		// setMappedData
		
		boolean debug = false;
		try {
			if (debug)
				new XMLOutputter().output(e.getDocument(), System.out);
			
			List<?> attributeList = e.getAttributes();
			setRowId("");
			setName("");
			setInfo("");
			setFuncat("");
			setFormula("");
			clearSynonyms();
			for (Object o : attributeList) {
				if (o instanceof Attribute) {
					Attribute a = (Attribute) o;
					if (!a.getName().equalsIgnoreCase("new_blast"))
						setAttribute(new MyAttribute(a));
				}
			}
			
			List<?> childrenList = e.getChildren();
			for (Object o : childrenList) {
				if (o instanceof Element) {
					Element childElement = (Element) o;
					setDataOfChildElement(childElement, experimentChildElement);
				}
			}
			return true;
		} catch (IOException e1) {
			ErrorMsg.addErrorMessage(e1);
			return false;
		}
	}
	
	public static void addAndMergeB(ExperimentInterface result, Collection<NumericMeasurementInterface> newMeasurementsOfSingleSample,
			boolean ignoreSnapshotFineTime, MergeCompareRequirements mcr) {
		SubstanceInterface targetSubstance = null;
		SubstanceInterface substanceWithNewData;
		synchronized (result) {
			substanceWithNewData = newMeasurementsOfSingleSample.iterator().next().getParentSample().getParentCondition().getParentSubstance();
			for (SubstanceInterface m : result)
				if (substanceWithNewData.equals(m)) {
					targetSubstance = m;
					break;
				}
			
			if (targetSubstance == null) {
				// completely new substance with all new data
				targetSubstance = substanceWithNewData.clone();
				result.add(targetSubstance);
			}
		}
		processCondition(result, targetSubstance,
				newMeasurementsOfSingleSample.iterator().next().getParentSample().getParentCondition(),
				ignoreSnapshotFineTime, true, newMeasurementsOfSingleSample, mcr);
	}
	
	public static void addAndMergeD(ExperimentInterface result, NumericMeasurementInterface newMeasurement, boolean ignoreSnapshotFineTime) {
		addAndMergeC(result, newMeasurement, ignoreSnapshotFineTime, false);
	}
	
	public static void addAndMergeC(ExperimentInterface result, NumericMeasurementInterface newMeasurement, boolean ignoreSnapshotFineTime,
			boolean ignoreSubstanceInfo) {
		SubstanceInterface targetSubstance = null;
		SubstanceInterface substanceWithNewData;
		synchronized (result) {
			substanceWithNewData = newMeasurement.getParentSample().getParentCondition().getParentSubstance();
			for (SubstanceInterface m : result)
				if (ignoreSubstanceInfo) {
					if (substanceWithNewData.getName().equals(m.getName())) {
						targetSubstance = m;
						break;
					}
				} else {
					if (substanceWithNewData.equals(m)) {
						targetSubstance = m;
						break;
					}
				}
			
			if (targetSubstance == null) {
				// completely new substance with all new data
				targetSubstance = substanceWithNewData.clone();
				result.add(targetSubstance);
			}
		}
		processCondition(result, targetSubstance, newMeasurement.getParentSample().getParentCondition(), ignoreSnapshotFineTime, true, newMeasurement);
	}
	
	public static void addAndMergeA(ExperimentInterface result, SubstanceInterface substanceWithNewData,
			boolean ignoreSnapshotFineTime, RunnableExecutor re, MergeCompareRequirements mcr) {
		if (re == null) {
			for (ConditionInterface ci : substanceWithNewData)
				for (SampleInterface si : ci)
					addAndMergeB(result, si, ignoreSnapshotFineTime, mcr);
		} else {
			ArrayList<Runnable> todo = new ArrayList<>();
			for (ConditionInterface ci : substanceWithNewData)
				todo.add(() -> {
					for (SampleInterface si : ci)
						addAndMergeB(result, si, ignoreSnapshotFineTime, mcr);
				});
			re.execInParallel(todo, "Merge condition data", null);
		}
	}
	
	private static void processCondition(ExperimentInterface targetExperiment, SubstanceInterface targetSubstance,
			ConditionInterface condition,
			boolean ignoreSnapshotFineTime, boolean forSureNewMeasurement,
			Collection<NumericMeasurementInterface> newMeasurementsOfSingleSample, MergeCompareRequirements mcr) {
		ConditionInterface targetCondition = null;
		synchronized (targetSubstance) {
			if (mcr.needsCompareConditions())
				for (ConditionInterface cond : targetSubstance)
					if (cond.equals(condition)) {
						targetCondition = cond;
						break;
					}
				
			if (targetCondition == null) {
				// completely new substance with all new data
				targetCondition = condition.clone(targetSubstance);
				targetSubstance.add(targetCondition);
			}
		}
		processSample(targetExperiment, targetSubstance, targetCondition,
				newMeasurementsOfSingleSample.iterator().next().getParentSample(),
				ignoreSnapshotFineTime,
				forSureNewMeasurement,
				newMeasurementsOfSingleSample, mcr);
	}
	
	private static void processCondition(ExperimentInterface targetExperiment, SubstanceInterface targetSubstance,
			ConditionInterface condition,
			boolean ignoreSnapshotFineTime, boolean forSureNewMeasurement, NumericMeasurementInterface newMeasurement) {
		ConditionInterface targetCondition = null;
		synchronized (targetSubstance) {
			for (ConditionInterface cond : targetSubstance)
				if (cond.equals(condition)) {
					targetCondition = cond;
					break;
				}
			
			if (targetCondition == null) {
				// completely new substance with all new data
				targetCondition = condition.clone(targetSubstance);
				targetSubstance.add(targetCondition);
			}
		}
		targetCondition.setParent(targetSubstance);
		targetCondition.setExperimentHeader(targetExperiment.getHeader());
		processSample(targetExperiment, targetSubstance, targetCondition, newMeasurement.getParentSample(),
				ignoreSnapshotFineTime,
				forSureNewMeasurement,
				newMeasurement);
	}
	
	private static void processSample(ExperimentInterface targetExperiment, SubstanceInterface targetSubstance,
			ConditionInterface targetCondition,
			SampleInterface sample, boolean ignoreSnapshotFineTime, boolean forSureNewMeasurement,
			Collection<NumericMeasurementInterface> newMeasurementsOfSingleSample, MergeCompareRequirements mcr) {
		SampleInterface targetSample = null;
		synchronized (targetCondition) {
			int day = sample.getTime();
			if (mcr.needsCompareSamples())
				for (SampleInterface s : targetCondition)
					if (s.getTime() == day) {
						if (ignoreSnapshotFineTime) {
							targetSample = s;
							break;
						} else {
							long a = s.getSampleFineTimeOrRowId();
							long b = sample.getSampleFineTimeOrRowId();
							if (a == b) {
								targetSample = s;
								break;
							}
						}
					}
			if (targetSample == null) {
				// completely new substance with all new data
				targetSample = sample.clone(targetCondition);
				if (ignoreSnapshotFineTime) {
					targetSample.setSampleFineTimeOrRowId(null);
				}
				synchronized (targetCondition) {
					targetCondition.add(targetSample);
				}
			} else
				targetSample.setSampleAverage(null);
		}
		synchronized (targetSample) {
			for (NumericMeasurementInterface newMeasurement : newMeasurementsOfSingleSample)
				if (targetSample != sample && (!mcr.needsCompareSamples() || forSureNewMeasurement || !targetSample.contains(newMeasurement))) {
					NumericMeasurementInterface nmn = newMeasurement.clone(targetSample);
					targetSample.add(nmn);
				}
		}
	}
	
	private static void processSample(ExperimentInterface targetExperiment, SubstanceInterface targetSubstance,
			ConditionInterface targetCondition,
			SampleInterface sample, boolean ignoreSnapshotFineTime, boolean forSureNewMeasurement, NumericMeasurementInterface newMeasurement) {
		SampleInterface targetSample = aa(targetCondition, sample, ignoreSnapshotFineTime, null);
		bb(sample, forSureNewMeasurement, newMeasurement, targetSample);
	}
	
	private static void bb(SampleInterface sample, boolean forSureNewMeasurement, NumericMeasurementInterface newMeasurement, SampleInterface targetSample) {
		synchronized (targetSample) {
			newMeasurement.setParentSample(targetSample);
			if (targetSample != sample && (forSureNewMeasurement || !targetSample.contains(newMeasurement)))
				targetSample.add(newMeasurement);
		}
	}
	
	private static SampleInterface aa(ConditionInterface targetCondition, SampleInterface sample, boolean ignoreSnapshotFineTime, SampleInterface targetSample) {
		synchronized (targetCondition) {
			if (targetSample == null)
				targetSample = findTargetSample(targetCondition, sample, ignoreSnapshotFineTime, targetSample);
			if (targetSample == null) {
				// completely new substance with all new data
				targetSample = sample.clone(targetCondition);
				synchronized (targetCondition) {
					targetCondition.add(targetSample);
				}
				
			}
			targetSample.setParent(targetCondition);
		}
		return targetSample;
	}
	
	private static SampleInterface findTargetSample(ConditionInterface targetCondition, SampleInterface sample,
			boolean ignoreSnapshotFineTime, SampleInterface targetSample) {
		Long sFT = sample.getSampleFineTimeOrRowId();
		int sDay = ((Sample) sample).time;
		for (SampleInterface s : targetCondition) {
			if (sDay != ((Sample) s).time)
				continue;
			Long ssFT = s.getSampleFineTimeOrRowId();
			if (sFT != null && ssFT != null) {
				if (sFT.longValue() == ssFT.longValue()) {
					targetSample = s;
					break;
				}
			} else
				if (s.compareTo(sample, ignoreSnapshotFineTime) == 0) {
					targetSample = s;
					break;
				}
		}
		return targetSample;
	}
	
	@Override
	public String getXMLstring() {
		return toString();
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		getString(s);
		return s.toString();
	}
	
	private static final String[] attributeNames = new String[] { "id", "name", "funcat", "info", "formula",
			"substancegroup", "cluster_id", "spot", "new_blast", "new_blast_e_val", "new_blast_score", "affy_hit",
			"score", "secure", "files" };
	
	private void getString(StringBuilder s) {
		s.append("<substance");
		getXMLAttributeString(s);
		s.append(">");
		getStringOfChildren(s);
		s.append("</substance>");
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder s) {
		getSynonymsString(s);
		getAttributeString(s, attributeNames, getAttributeValues());
	}
	
	private Object[] getAttributeValues() {
		return new Object[] { getRowId(), getName(), getFuncat(), getInfo(), getFormula(), getSubstancegroup(),
				getClusterId(), getSpot(), getNewBlast(), getNewBlastEval(), getNewBlastScore(), getAffyHit(), getScore(),
				getSecure(), getFiles() };
	}
	
	@Override
	public void getStringOfChildren(StringBuilder s) {
		for (ConditionInterface c : conditions)
			c.getString(s);
	}
	
	public static void getAttributeString(StringBuilder s, String[] attributeNames, Object[] attributeValues) {
		getAttributeString(s, attributeNames, attributeValues, false);
	}
	
	public static boolean includeEmptyAttributes = false;
	
	/**
	 * @param asElements
	 *           indicates, that the attribute shall be written as single element, eg<br>
	 *           <code>&lt;attrname>attrvalue&lt;/attrname&gt;</code> instead <code>attrname='attrvalue'</code>
	 */
	public static void getAttributeString(StringBuilder s, String[] attributeNames, Object[] attributeValues, boolean asElements) {
		int idx = 0;
		for (String attribute : attributeNames) {
			Object v = attributeValues[idx];
			if (v != null && v instanceof Date)
				v = AttributeHelper.getDateString((Date) v);
			
			if (v != null && (!(v.equals("") && asElements)) && !v.equals("null")) { //
				if (includeEmptyAttributes || !v.equals(""))
					if (asElements)
						s.append("<" + attribute + ">" + (v instanceof String ? escapeBadCharacters((String) v) : v) + "</" + attribute + ">");
					else
						s.append(" " + attribute + "='" + (v instanceof String ? escapeBadCharacters((String) v) : v) + "'");
			} else
				if (asElements)
					s.append("<" + attribute + "/>");
			idx++;
		}
	}
	
	private static String escapeBadCharacters(String value) {
		String s = StringManipulationTools.UnicodeToHtml(value, getBad());// .replaceAll("&#", "~");
		return s;
	}
	
	private static HashSet<Character> badChars = null;
	
	private static HashSet<Character> getBad() {
		if (badChars != null)
			return badChars;
		else {
			badChars = new HashSet<Character>();
			badChars.add('<');
			badChars.add('>');
			badChars.add('\'');
			badChars.add('"');
			badChars.add('&');
			return badChars;
		}
	}
	
	private void getSynonymsString(StringBuilder res) {
		if (synonyms == null || synonyms.size() == 0)
			return;
		else {
			for (Entry<Integer, String> e : synonyms.entrySet()) {
				res.append(" name" + e.getKey().toString() + "='" + escapeBadCharacters(e.getValue()) + "'");
			}
		}
	}
	
	@Override
	public Collection<MyComparableDataPoint> getDataPoints(boolean returnAvgValues, String optTreatmentFilter) {
		ArrayList<MyComparableDataPoint> result = new ArrayList<MyComparableDataPoint>();
		for (ConditionInterface c : conditions) {
			if (optTreatmentFilter != null && !(c.getTreatment() + "").equals(optTreatmentFilter))
				continue;
			if (returnAvgValues)
				result.addAll(c.getMeanMCDPs());
			else
				result.addAll(c.getMCDPs());
		}
		return result;
	}
	
	@Override
	public Collection<MyComparableDataPoint> getDataPoints(boolean returnAvgValues, boolean removeEmptyConditions) {
		ArrayList<MyComparableDataPoint> result = new ArrayList<MyComparableDataPoint>();
		if (removeEmptyConditions) {
			for (ConditionInterface c : conditions)
				if (returnAvgValues)
					result.addAll(c.getMeanMCDPs());
				else
					result.addAll(c.getMCDPs());
		} else {
			// collect all timepoints of this substance
			HashSet<SampleTimeAndUnit> timesandtimeunits = new HashSet<SampleTimeAndUnit>();
			for (ConditionInterface c : this)
				for (SampleInterface s : c)
					timesandtimeunits.add(new SampleTimeAndUnit(s.getSampleTime(), s.getTime(), s.getTimeUnit()));
				
			Collection<MyComparableDataPoint> resultPerCond = new ArrayList<MyComparableDataPoint>();
			for (ConditionInterface c : conditions) {
				resultPerCond.clear();
				if (returnAvgValues)
					resultPerCond = c.getMeanMCDPs();
				else
					resultPerCond = c.getMCDPs();
				
				// add a bar for all conditions without measurements, do this for all timepoints of the substance
				if (resultPerCond.size() <= 0) {
					for (SampleTimeAndUnit ms : timesandtimeunits) {
						result.add(new MyComparableDataPoint(!returnAvgValues,
								Double.NaN, Double.NaN, c.getConditionName(),
								ms.sampleTime, "[no unit]", ms.time, ms.time,
								false,
								false, ms.timeUnit, c.getConditionId(), -1, new NumericMeasurement(new Sample(new Condition(new Substance())))));
					}
				}
				
				result.addAll(resultPerCond);
			}
		}
		
		return result;
	}
	
	private class SampleTimeAndUnit {
		
		private final String sampleTime;
		private final int time;
		private final String timeUnit;
		
		public SampleTimeAndUnit(String sampleTime, int time, String timeUnit) {
			this.sampleTime = sampleTime;
			this.time = time;
			this.timeUnit = timeUnit;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SampleTimeAndUnit))
				return false;
			return sampleTime.equals(((SampleTimeAndUnit) obj).sampleTime);
		}
		
	}
	
	/**
	 * @return Null or eventually empty list of synonyms.
	 */
	@Override
	public Collection<String> getSynonyms() {
		if (synonyms == null)
			synonyms = new HashMap<Integer, String>();
		return synonyms.values();
	}
	
	@Override
	public HashMap<Integer, String> getSynonymMap() {
		return synonyms;
	}
	
	@Override
	public int getNumberOfDifferentTimePoints() {
		HashSet<Integer> times = new HashSet<Integer>();
		for (ConditionInterface c : conditions)
			c.getTimes(times);
		return times.size();
	}
	
	@Override
	public int clearSynonyms() {
		if (synonyms == null)
			return 0;
		int result = synonyms.size();
		synonyms.clear();
		return result;
	}
	
	@Override
	public void setSynonyme(int idx, String value) {
		if (synonyms == null)
			synonyms = new HashMap<Integer, String>();
		synonyms.put(idx, value);
	}
	
	@Override
	public void setSynonyme(HashMap<Integer, String> hashMap) {
		synonyms = hashMap;
	}
	
	@Override
	public String getSynonyme(int idx) {
		return synonyms.get(idx);
	}
	
	@Override
	public String getFuncat() {
		return funcat;
	}
	
	@Override
	public String getInfo() {
		return info;
	}
	
	@Override
	public int getMaximumSynonymeIndex(int returnIfNoSynonymes) {
		if (synonyms == null || synonyms.size() == 0)
			return returnIfNoSynonymes;
		else {
			return synonyms.size();
		}
	}
	
	@Override
	public double getAverage() {
		double sum = 0;
		double cnt = 0;
		for (ConditionInterface c : conditions)
			for (SampleInterface s : c) {
				sum += s.getSampleAverage().getValue();
				cnt++;
			}
		return sum / cnt;
	}
	
	private HashMap<String, Consumer<String>> stringSetters = null;
	
	private synchronized HashMap<String, Consumer<String>> getStringSetterFunctions() {
		if (stringSetters == null) {
			stringSetters = new HashMap<String, Consumer<String>>();
			stringSetters.put("id", this::setRowId);
			stringSetters.put("name", this::setName);
			stringSetters.put("info", this::setInfo);
			stringSetters.put("substancegroup", this::setSubstancegroup);
			stringSetters.put("funcat", this::setFuncat);
			stringSetters.put("new_blast", this::setNewBlast);
			stringSetters.put("spot", this::setSpot);
			stringSetters.put("cluster_id", this::setClusterId);
			stringSetters.put("new_blast_e_val", this::setNewBlastEval);
			stringSetters.put("new_blast_score", this::setNewBlastScore);
			stringSetters.put("affy_hit", this::setAffyHit);
			stringSetters.put("score", this::setScore);
			stringSetters.put("secure", this::setSecure);
			stringSetters.put("files", this::setFiles);
			stringSetters.put("formula", this::setFormula);
		}
		return stringSetters;
	}
	
	@Override
	public Object getAttributeField(String id) {
		throw new UnsupportedOperationException("not yet implemented for this data structure!");
	}
	
	@Override
	public void setAttributeField(String id, Object value) {
		HashMap<String, Consumer<String>> mm = getStringSetterFunctions();
		if (mm.containsKey(id)) {
			mm.get(id).accept((String) value);
		} else
			if (id.startsWith("name")) {
				String index = id.substring("name".length());
				try {
					int idx = Integer.parseInt(index);
					if (synonyms == null)
						synonyms = new HashMap<Integer, String>();
					synonyms.put(idx, (String) value);
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			} else
				throw new RuntimeException("Internal Error: Unknown Substance Attribute: " + id);
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		if (attr == null)
			return;
		if (attr.getValue() == null)
			return;
		attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		
		HashMap<String, Consumer<String>> mm = getStringSetterFunctions();
		if (mm.containsKey(attr.getName())) {
			mm.get(attr.getName()).accept(attr.getValue());
		} else
			if (attr.getName().startsWith("name")) {
				String index = attr.getName().substring("name".length());
				try {
					int idx = Integer.parseInt(index);
					if (synonyms == null)
						synonyms = new HashMap<Integer, String>();
					synonyms.put(idx, attr.getValue());
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			} else
				System.err.println("Internal Error: Unknown Substance Attribute: " + attr.getName());
	}
	
	@Override
	public boolean setData(Element experimentElement) {
		return setMappedData(experimentElement, null);
	}
	
	@Override
	public void setDataOfChildElement(Element childElement) {
		setDataOfChildElement(childElement, null);
	}
	
	@Override
	public void setDataOfChildElement(Element childElement, Element experimentChildElement) {
		if (childElement.getName().equals("line")) {
			ConditionInterface c = Experiment.getTypeManager().getNewCondition(this);
			if (c.setData(childElement, experimentChildElement)) {
				if (c.getExperimentHeader() != null)
					for (ConditionInterface cc : conditions) {
						if (cc.getExperimentHeader() != null)
							if (c.getExperimentHeader().compareTo(cc.getExperimentHeader()) == 0) {
								c.setExperimentHeader(cc.getExperimentHeader());
								break;
							}
					}
				conditions.add(c);
			}
		}
	}
	
	@Override
	public void getSubstanceString(StringBuilder r) {
		r.append("<substance");
		getXMLAttributeString(r);
		r.append(">");
	}
	
	@Override
	public int compareTo(SubstanceInterface o) {
		return getName().compareTo(o.getName());
	}
	
	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	
	@Override
	public String getRowId() {
		return rowId;
	}
	
	@Override
	public void setName(String name) {
		assert name != null;
		// assert name.length() > 0;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setFuncat(String funcat) {
		this.funcat = funcat;
	}
	
	@Override
	public void setInfo(String info) {
		if (info != null)
			info = info.trim();
		this.info = info;
	}
	
	@Override
	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	@Override
	public String getFormula() {
		return formula;
	}
	
	@Override
	public void setSubstancegroup(String substancegroup) {
		this.substancegroup = substancegroup;
	}
	
	@Override
	public String getSubstancegroup() {
		return substancegroup;
	}
	
	@Override
	public void setClusterId(String cluster_id) {
		this.cluster_id = cluster_id;
	}
	
	@Override
	public String getClusterId() {
		return cluster_id;
	}
	
	@Override
	public void setSpot(String spot) {
		this.spot = spot;
	}
	
	@Override
	public String getSpot() {
		return spot;
	}
	
	@Override
	public void setNewBlast(String new_blast) {
		// if (new_blast!=null && new_blast.indexOf("~")>0)
		// new_blast = ErrorMsg.stringReplace(new_blast, "~", "_");
		// this.new_blast = new_blast;
	}
	
	@Override
	public String getNewBlast() {
		return new_blast;
	}
	
	@Override
	public void setNewBlastEval(String new_blast_e_val) {
		this.new_blast_e_val = new_blast_e_val;
	}
	
	@Override
	public String getNewBlastEval() {
		return new_blast_e_val;
	}
	
	@Override
	public void setNewBlastScore(String new_blast_score) {
		this.new_blast_score = new_blast_score;
	}
	
	@Override
	public String getNewBlastScore() {
		return new_blast_score;
	}
	
	@Override
	public void setAffyHit(String affy_hit) {
		this.affy_hit = affy_hit;
	}
	
	@Override
	public String getAffyHit() {
		return affy_hit;
	}
	
	@Override
	public void setScore(String score) {
		this.score = score;
	}
	
	@Override
	public String getScore() {
		return score;
	}
	
	@Override
	public void setSecure(String secure) {
		this.secure = secure;
	}
	
	@Override
	public String getSecure() {
		return secure;
	}
	
	@Override
	public void setFiles(String files) {
		this.files = files;
	}
	
	@Override
	public String getFiles() {
		return files;
	}
	
	public static void validate(Document doc) throws Exception {
		URL xsd = Substance.class.getClassLoader().getResource(
				Substance.class.getPackage().getName().replace('.', '/') + "/" + "vanted.xsd");
		XMLHelper.validate(doc, xsd);
	}
	
	@Override
	public int getDataPointCount(boolean returnAvgValues) {
		int res = 0;
		for (ConditionInterface c : conditions)
			if (returnAvgValues)
				res += c.size();
			else
				for (SampleInterface sd : c)
					res += sd.size();
		return res;
	}
	
	@Override
	public double getSum() {
		double sum = 0;
		for (ConditionInterface c : conditions)
			for (SampleInterface s : c) {
				for (NumericMeasurementInterface md : s) {
					sum += md.getValue();
				}
			}
		return sum;
	}
	
	@Override
	public Collection<ConditionInterface> getConditions(Collection<String> validConditons) {
		if (validConditons == null)
			return conditions;
		else {
			Collection<ConditionInterface> result = new ArrayList<ConditionInterface>();
			HashSet<String> valid = new HashSet<String>();
			valid.addAll(validConditons);
			for (ConditionInterface s : conditions) {
				if (valid.contains(s.getExpAndConditionName()))
					result.add(s);
			}
			return result;
		}
	}
	
	/*
	 * Delegate Methods
	 */
	@Override
	public boolean add(ConditionInterface e) {
		return conditions.add(e);
	}
	
	@Override
	public Iterator<ConditionInterface> iterator() {
		return conditions.iterator();
	}
	
	@Override
	public boolean addAll(Collection<? extends ConditionInterface> c) {
		return conditions.addAll(c);
	}
	
	@Override
	public void clear() {
		conditions.clear();
	}
	
	@Override
	public boolean contains(Object o) {
		return conditions.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return conditions.containsAll(c);
	}
	
	@Override
	public boolean isEmpty() {
		return conditions.isEmpty();
	}
	
	@Override
	public boolean remove(Object o) {
		return conditions.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return conditions.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		return conditions.retainAll(c);
	}
	
	@Override
	public int size() {
		return conditions.size();
	}
	
	@Override
	public Object[] toArray() {
		return conditions.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return conditions.toArray(a);
	}
	
	public Substance(Map<?, ?> map) {
		this();
		for (Object o : map.keySet()) {
			if (o instanceof String) {
				String key = (String) o;
				Object v = map.get(key);
				if (v instanceof String) {
					String s = (String) v;
					// s = StringManipulationTools.UnicodeToHtml(s);
					setAttribute(new MyAttribute(key, s));
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		if (synonyms == null || synonyms.size() == 0) {
			// empty
		} else {
			for (Entry<Integer, String> e : synonyms.entrySet()) {
				attributeValueMap.put("name" + e.getKey().toString(), e.getValue());
			}
		}
		
		int idx = 0;
		Object[] attributeValues = getAttributeValues();
		for (String attribute : attributeNames) {
			Object value = attributeValues[idx];
			if (value != null && !value.equals("null")) { // !value.equals("") &&
				attributeValueMap.put(attribute, value);
			}
			idx++;
		}
	}
	
	@Override
	public SubstanceInterface clone() {
		SubstanceInterface s = Experiment.getTypeManager().getNewSubstance();
		s.setRowId(getRowId());
		s.setName(getName());
		s.setFuncat(getFuncat());
		s.setInfo(getInfo());
		s.setFormula(getFormula());
		s.setSubstancegroup(getSubstancegroup());
		s.setClusterId(getClusterId());
		s.setSpot(getSpot());
		s.setNewBlast(getNewBlast());
		s.setNewBlastEval(getNewBlastEval());
		s.setNewBlastScore(getNewBlastScore());
		s.setAffyHit(getAffyHit());
		s.setScore(getScore());
		s.setSecure(getSecure());
		s.setFiles(getFiles());
		s.setSynonyme(getSynonymMap() == null ? null : new HashMap<Integer, String>(getSynonymMap()));
		return s;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Substance))
			return false;
		
		Substance s = (Substance) obj;
		
		if (rowId != null || s.rowId != null)
			if (rowId != null) {
				if (!rowId.equals(s.rowId))
					return false;
			} else
				return false;
			
		if (name != null || s.name != null)
			if (name != null) {
				if (!name.equals(s.name))
					return false;
			} else
				return false;
			
		if (funcat != null || s.funcat != null)
			if (funcat != null) {
				if (!funcat.equals(s.funcat))
					return false;
			} else
				return false;
			
		if (info != null || s.info != null)
			if (info != null) {
				if (!info.equals(s.info))
					return false;
			} else
				return false;
			
		if (formula != null || s.formula != null)
			if (formula != null) {
				if (!formula.equals(s.formula))
					return false;
			} else
				return false;
			
		if (substancegroup != null || s.substancegroup != null)
			if (substancegroup != null) {
				if (!substancegroup.equals(s.substancegroup))
					return false;
			} else
				return false;
			
		if (cluster_id != null || s.cluster_id != null)
			if (cluster_id != null) {
				if (!cluster_id.equals(s.cluster_id))
					return false;
			} else
				return false;
			
		if (spot != null || s.spot != null)
			if (spot != null) {
				if (!spot.equals(s.spot))
					return false;
			} else
				return false;
			
		if (new_blast != null || s.new_blast != null)
			if (new_blast != null) {
				if (!new_blast.equals(s.new_blast))
					return false;
			} else
				return false;
			
		if (new_blast_e_val != null || s.new_blast_e_val != null)
			if (new_blast_e_val != null) {
				if (!new_blast_e_val.equals(s.new_blast_e_val))
					return false;
			} else
				return false;
			
		if (new_blast_score != null || s.new_blast_score != null)
			if (new_blast_score != null) {
				if (!new_blast_score.equals(s.new_blast_score))
					return false;
			} else
				return false;
			
		if (affy_hit != null || s.affy_hit != null)
			if (affy_hit != null) {
				if (!affy_hit.equals(s.affy_hit))
					return false;
			} else
				return false;
			
		if (score != null || s.score != null)
			if (score != null) {
				if (!score.equals(s.score))
					return false;
			} else
				return false;
			
		if (secure != null || s.secure != null)
			if (secure != null) {
				if (!secure.equals(s.secure))
					return false;
			} else
				return false;
			
		return true;
	}
	
	@Override
	public int hashCode() {
		String s1 = rowId + ";" + name + ";" + funcat + ";" + info + ";" + formula + ";" + substancegroup + ";" + cluster_id + ";" + spot + ";" +
				new_blast + ";" + new_blast_e_val + ";" + new_blast_score + ";" + affy_hit + ";" + score + ";" + secure;
		return s1.hashCode();
	}
	
	@Override
	public void add(int index, ConditionInterface element) {
		conditions.add(index, element);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends ConditionInterface> c) {
		return conditions.addAll(index, c);
	}
	
	@Override
	public ConditionInterface get(int index) {
		return conditions.get(index);
	}
	
	@Override
	public int indexOf(Object o) {
		return conditions.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return conditions.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<ConditionInterface> listIterator() {
		return conditions.listIterator();
	}
	
	@Override
	public ListIterator<ConditionInterface> listIterator(int index) {
		return conditions.listIterator(index);
	}
	
	@Override
	public ConditionInterface remove(int index) {
		return conditions.remove(index);
	}
	
	@Override
	public ConditionInterface set(int index, ConditionInterface element) {
		return conditions.set(index, element);
	}
	
	@Override
	public List<ConditionInterface> subList(int fromIndex, int toIndex) {
		return subList(fromIndex, toIndex);
	}
	
	/**
	 * @see See also #getDataPoints(boolean)
	 */
	@Override
	public int getNumberOfMeasurements() {
		return getDataPointCount(false);
	}
	
	@Override
	public void sortConditions() {
		Collections.sort(conditions, new Comparator<ConditionInterface>() {
			@Override
			public int compare(ConditionInterface o1, ConditionInterface o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
	}
	
}
