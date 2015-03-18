package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.NiceNameSupport;
import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

public class Condition implements ConditionInterface {
	
	public static final String ATTRIBUTE_KEY_FILES = "files";
	public static final String ATTRIBUTE_KEY_SEQUENCE = "sequence";
	public static final String ATTRIBUTE_KEY_VARIETY = "variety";
	public static final String ATTRIBUTE_KEY_TREATMENT = "treatment";
	public static final String ATTRIBUTE_KEY_NAME = "name";
	public static final String ATTRIBUTE_KEY_ID = "id";
	public static final String ATTRIBUTE_KEY_GROWTHCONDITIONS = "growthconditions";
	public static final String ATTRIBUTE_KEY_GENOTYPE = "genotype";
	
	public enum ConditionInfo implements NiceNameSupport {
		IGNORED_FIELD("---"), SPECIES("Species"), GENOTYPE("Genotype"), VARIETY("Variety"),
		GROWTHCONDITIONS("Growth Condititions"), TREATMENT("Treatment"),
		SEQUENCE("Sequence"), FILES("Files");
		
		private String desc;
		
		ConditionInfo(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString() {
			return desc;
		}
		
		public static ConditionInfo valueOfString(String v) {
			for (ConditionInfo ci : values())
				if (v.equals(ci + ""))
					return ci;
			return valueOf(v);
		}
		
		public static ArrayList<ConditionInfo> getList() {
			ArrayList<ConditionInfo> result = new ArrayList<ConditionInfo>();
			for (ConditionInfo ci : ConditionInfo.values())
				result.add(ci);
			return result;
		}
		
		@Override
		public String getNiceName() {
			return org.apache.commons.lang.WordUtils.capitalize(this.name().toLowerCase());
		}
	}
	
	/**
	 * If list of state variables is modified/extended, check and modify equals,
	 * hashCode and eventually compareTo method implementation.
	 */
	private String species, genotype, growthconditions, treatment, variety, sequence, files;
	private int rowId;
	
	private final LinkedList<SampleInterface> samples = new LinkedList<SampleInterface>();
	private SubstanceInterface parent;
	private ExperimentHeaderInterface header;
	
	private static final String[] attributeNames = new String[] {
			"experimentname", "database", "experimenttype",
			"coordinator", "startdate", "importdate", "storagedate", "remark",
			ATTRIBUTE_KEY_ID, ATTRIBUTE_KEY_NAME, ATTRIBUTE_KEY_GENOTYPE,
			ATTRIBUTE_KEY_GROWTHCONDITIONS, ATTRIBUTE_KEY_TREATMENT,
			ATTRIBUTE_KEY_VARIETY, ATTRIBUTE_KEY_SEQUENCE, ATTRIBUTE_KEY_FILES, "settings" };
	
	private static final String[] attributeNamesForDocument = new String[] { ATTRIBUTE_KEY_GENOTYPE,
			ATTRIBUTE_KEY_GROWTHCONDITIONS, ATTRIBUTE_KEY_ID,
			ATTRIBUTE_KEY_NAME, ATTRIBUTE_KEY_TREATMENT, ATTRIBUTE_KEY_VARIETY, ATTRIBUTE_KEY_SEQUENCE, ATTRIBUTE_KEY_FILES };
	
	private static final String[] attributeNameWithoutDocumentFields = remove(attributeNames, attributeNamesForDocument, ATTRIBUTE_KEY_SEQUENCE);
	
	public Condition(SubstanceInterface md) {
		parent = md;
		header = new ExperimentHeader();
	}
	
	private Object[] getAttributeValues() {
		return new Object[] { getExperimentName(), getDatabase(), getExperimentType(), getCoordinator(),
				getExperimentStartDate(), getExperimentImportDate(), getExperimentStorageDate(),
				getExperimentRemark(),
				getRowId(), getSpecies(), getGenotype(), getVariety(),
				getGrowthconditions(), getTreatment(), getSequence(), getFiles(), getExperimentSettings() };
	}
	
	@Override
	public Object getAttributeField(String id) {
		switch (id) {
			case ATTRIBUTE_KEY_NAME:
				return getSpecies();
			case ATTRIBUTE_KEY_GENOTYPE:
				return getGenotype();
			case ATTRIBUTE_KEY_VARIETY:
				return getVariety();
			case ATTRIBUTE_KEY_SEQUENCE:
				return getSequence();
			case ATTRIBUTE_KEY_GROWTHCONDITIONS:
				return getGrowthconditions();
			case ATTRIBUTE_KEY_TREATMENT:
				return getTreatment();
			case ATTRIBUTE_KEY_FILES:
				return getFiles();
		}
		throw new UnsupportedOperationException("Can't return field value from id '" + id + "'!");
	}
	
	@Override
	public void setAttributeField(String id, Object value) {
		switch (id) {
			case ATTRIBUTE_KEY_NAME:
				setSpecies((String) value);
				return;
			case ATTRIBUTE_KEY_GENOTYPE:
				setGenotype((String) value);
				return;
			case ATTRIBUTE_KEY_VARIETY:
				setVariety((String) value);
				return;
			case ATTRIBUTE_KEY_SEQUENCE:
				setSequence((String) value);
				return;
			case ATTRIBUTE_KEY_GROWTHCONDITIONS:
				setGrowthconditions((String) value);
				return;
			case ATTRIBUTE_KEY_TREATMENT:
				setTreatment((String) value);
				return;
			case ATTRIBUTE_KEY_FILES:
				setFiles((String) value);
				return;
		}
		throw new UnsupportedOperationException("Can't set field value with id '" + id + "'!");
	}
	
	private static String[] remove(String[] a, String[] rem, String... rrr) {
		HashSet<String> r = new HashSet<String>();
		for (String rs : rem)
			r.add(rs);
		HashSet<String> p = new HashSet<String>();
		for (String ps : rrr)
			p.add(ps);
		ArrayList<String> res = new ArrayList<String>();
		for (String as : a) {
			if (!r.contains(as) && !p.contains(as))
				res.add(as);
		}
		return res.toArray(new String[] {});
	}
	
	@SuppressWarnings("unchecked")
	public Condition(SubstanceInterface s, Map attributemap) {
		this(s);
		for (Object o : attributemap.keySet()) {
			if (o instanceof String) {
				String key = (String) o;
				if (attributemap.get(key) != null && attributemap.get(key) instanceof String)
					setAttribute(new MyAttribute(key, (String) attributemap.get(key)));
			}
		}
	}
	
	@Override
	public void getString(StringBuilder r) {
		r.append("<line");
		getXMLAttributeString(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</line>");
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, attributeNames, getAttributeValues());
	}
	
	@Override
	public String getHTMLdescription() {
		StringBuilder res = new StringBuilder();
		res.append("<html><table border='1'>");
		res.append("<tr><th>Property</th><th>Value</th></tr>");
		res.append("<tr><td>Species</td><td>" + species + "</td></tr>");
		res.append("<tr><td>Genotype</td><td>" + genotype + "</td></tr>");
		res.append("<tr><td>Variety</td><td>" + variety + "</td></tr>");
		res.append("<tr><td>Sequence</td><td>" + sequence + "</td></tr>");
		res.append("<tr><td>Growth conditions</td><td>" + growthconditions + "</td></tr>");
		res.append("<tr><td>Treatment</td><td>" + treatment + "</td></tr>");
		res.append("</table></html>");
		return res.toString();
	}
	
	@Override
	public void getStringOfChildren(StringBuilder r) {
		for (SampleInterface s : this)
			s.getString(r);
	}
	
	@Override
	public String getExpAndConditionName() {
		return getExperimentName() + ": " + getConditionName();
	}
	
	@Override
	public String toString() {
		return getExpAndConditionName();
	}
	
	@Override
	public String getConditionName() {
		return getConditionName(true);
	}
	
	public String getConditionName(boolean oldStyleIgnoringGrowthcondition) {
		return getConditionName(oldStyleIgnoringGrowthcondition, false);
	}
	
	private String conditionCache = null;
	
	public String getConditionName(boolean oldStyleIgnoringGrowthcondition, boolean cached) {
		if (!oldStyleIgnoringGrowthcondition) {
			if (cached && conditionCache != null)
				return conditionCache;
			// String res = /* getExperimentName() + ": " + */getSpecies() + " / " + getGenotype() + " / " + getTreatment() + " / "
			// + getGrowthconditions() + " / " + getVariety() + "/" + getSequence();
			// getExperimentName() + ": " +
			String res = getSpecies() + " / " + getGenotype() + " / " + getTreatment() + " / "
					+ getGrowthconditions() + " / " + getVariety();
			
			if (cached)
				conditionCache = res;
			return res;
		} else {
			if (cached && conditionCache != null)
				return conditionCache;
			String serie;
			
			String species = getSpecies();
			String genotype = getGenotype();
			String variety = getVariety();
			String treatment = getTreatment();
			
			serie = species;
			if (genotype != null && genotype.length() > 0
					&& !genotype.equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING)) {
				serie += "/" + genotype;
				if (serie.startsWith(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING + "/"))
					serie = serie.substring((ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING + "/").length());
			}
			if (variety != null && variety.length() > 0
					&& !variety.equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING)) {
				serie += "/" + variety;
				if (serie.startsWith(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING + "/"))
					serie = serie.substring((ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING + "/").length());
			}
			if (treatment != null && treatment.length() > 0 && !treatment.equals("null")
					&& !treatment.equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
				serie += " (" + treatment + ")";
			
			String res = getRowId() + ": " + serie;
			if (cached)
				conditionCache = res;
			return res;
		}
	}
	
	@Override
	public void getTimes(Set<Integer> times) {
		for (SampleInterface s : this)
			times.add(s.getTime());
	}
	
	// public Collection<Sample> getSamples() {
	// return samples;
	// }
	//
	// public Collection<Sample> getAverageSamples() {
	// return samples;
	// }
	
	@Override
	public String getExperimentName() {
		return header.getExperimentName();
	}
	
	@Override
	public String getDatabase() {
		return header.getDatabase();
	}
	
	@Override
	public String getCoordinator() {
		return getExperimentCoordinator();
	}
	
	@Override
	public Date getExperimentStartDate() {
		return header.getStartdate();
	}
	
	@Override
	public int getConditionId() {
		return getRowId();
	}
	
	@Override
	public String getExperimentSettings() {
		return header.getSettings();
	}
	
	@Override
	public Collection<MyComparableDataPoint> getMeanMCDPs() {
		LinkedList<MyComparableDataPoint> result = new LinkedList<MyComparableDataPoint>();
		for (SampleInterface s : this) {
			SampleAverageInterface m = s.getSampleAverage();
			boolean ismean = true;
			double mean = m.getValue();
			double stddev = m.getStdDev();
			String serie = s.getParentCondition().getConditionName();
			String timeUnitAndTime = s.getSampleTime();
			String measurementUnit = m.getUnit();
			long timeValueForComparison = m.getParentSample().getSampleFineTimeOrRowId() != null &&
					s.getSampleFineTimeOrRowId() != null && s.getSampleFineTimeOrRowId() > 0 ? s.getSampleFineTimeOrRowId() : s.getTime();
			TtestInfo ttestInfo = s.getTtestInfo();
			String timeUnit = s.getTimeUnit();
			int seriesID = s.getParentCondition().getSeriesId();
			int replicate = s.size();
			Measurement reference = m;
			MyComparableDataPoint mcdp = new MyComparableDataPoint(ismean, mean, stddev, serie, timeUnitAndTime,
					measurementUnit, timeValueForComparison, m.getParentSample().getTime(), ttestInfo == TtestInfo.REFERENCE, ttestInfo == TtestInfo.H1,
					timeUnit, seriesID, replicate, reference);
			result.add(mcdp);
		}
		return result;
	}
	
	@Override
	public Collection<MyComparableDataPoint> getMCDPs() {
		LinkedList<MyComparableDataPoint> result = new LinkedList<MyComparableDataPoint>();
		for (SampleInterface s : this) {
			for (NumericMeasurementInterface m : s) {
				boolean ismean = false;
				double mean = m.getValue();
				double stddev = Double.NaN;
				String serie = m.getParentSample().getParentCondition().getConditionName();
				String timeUnitAndTime = m.getParentSample().getSampleTime();
				String measurementUnit = m.getUnit();
				long timeValueForComparison = m.getParentSample().getSampleFineTimeOrRowId() != null && m.getParentSample().getSampleFineTimeOrRowId() > 0 ? m
						.getParentSample().getSampleFineTimeOrRowId() : m
						.getParentSample().getTime();
				TtestInfo ttestInfo = m.getParentSample().getTtestInfo();
				String timeUnit = m.getParentSample().getTimeUnit();
				int seriesID = m.getParentSample().getParentCondition().getSeriesId();
				int replicate = m.getReplicateID();
				Measurement reference = m;
				MyComparableDataPoint mcdp = new MyComparableDataPoint(ismean, mean, stddev, serie, timeUnitAndTime,
						measurementUnit, timeValueForComparison, m.getParentSample().getTime(), ttestInfo == TtestInfo.REFERENCE, ttestInfo == TtestInfo.H1,
						timeUnit, seriesID, replicate, reference);
				result.add(mcdp);
			}
		}
		return result;
	}
	
	@Override
	public Collection<Double> getMeanValues() {
		LinkedList<Double> result = new LinkedList<Double>();
		for (SampleInterface s : this)
			result.add(s.getSampleAverage().getValue());
		return result;
	}
	
	@Override
	public Collection<Integer> getMeanTimePoints() {
		LinkedList<Integer> result = new LinkedList<Integer>();
		for (SampleInterface s : this)
			result.add(s.getTime());
		return result;
	}
	
	@Override
	public Collection<String> getMeanTimeUnits() {
		LinkedList<String> result = new LinkedList<String>();
		for (SampleInterface s : this)
			result.add(s.getTimeUnit());
		return result;
	}
	
	@Override
	public double calcAlpha() {
		double alpha, y_, beta, x_;
		x_ = getAvgI(getMeanTimePoints());
		y_ = getAvgD(getMeanValues());
		beta = calcBeta();
		alpha = y_ - beta * x_;
		return alpha;
	}
	
	@Override
	public double calcBeta() {
		Collection<Integer> x = getMeanTimePoints();
		Collection<Double> y = getMeanValues();
		double n = x.size();
		if (x.size() != y.size())
			ErrorMsg.addErrorMessage("Internal Error: Series Data Number Count <> Time Point Count");
		
		double beta, sum_xi_yi, x_, y_, sum_xi2; // , x_2;
		
		x_ = getAvgI(x);
		y_ = getAvgD(y);
		
		sum_xi_yi = 0;
		sum_xi2 = 0;
		Iterator<Integer> xI = x.iterator();
		Iterator<Double> yI = y.iterator();
		while (xI.hasNext() && yI.hasNext()) {
			double xx = xI.next();
			sum_xi_yi += xx * yI.next();
			sum_xi2 += xx * xx;
		}
		
		beta = (sum_xi_yi - n * x_ * y_) / (sum_xi2 - n * x_ * x_);
		
		return beta;
	}
	
	public static double getSum(Collection<Double> values) {
		double sum = 0;
		for (double n : values)
			sum += n;
		return sum;
	}
	
	private double getAvgI(Collection<Integer> meanTimePoints) {
		double sum = 0;
		for (Integer i : meanTimePoints)
			sum += i;
		return sum / meanTimePoints.size();
	}
	
	private double getAvgD(Collection<Double> values) {
		return getSum(values) / values.size();
	}
	
	@Override
	public String getSpecies() {
		// if (species == null || species.equalsIgnoreCase(""))
		// species = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return species;
	}
	
	@Override
	public String getGenotype() {
		// if (genotype == null || genotype.equalsIgnoreCase(""))
		// genotype = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return genotype;
	}
	
	@Override
	public String getTreatment() {
		return treatment;
	}
	
	@Override
	public int getSeriesId() {
		return getRowId();
	}
	
	@Override
	public boolean setData(Element condition) {
		return setData(condition, null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean setData(Element conditionElement, Element experimentChildElement) {
		
		List attributeList = conditionElement.getAttributes();
		
		if (experimentChildElement != null)
			for (Object o : experimentChildElement.getContent())
				if (o instanceof Element) {
					Element exp = (Element) o;
					if (exp.getName() != null && exp.getText() != null)
						attributeList.add(new Attribute(exp.getName(), exp.getText()));
				}
		
		for (Object o : attributeList) {
			if (o instanceof Attribute) {
				Attribute a = (Attribute) o;
				setAttribute(new MyAttribute(a));
			}
		}
		List childrenList = conditionElement.getChildren();
		for (Object o : childrenList) {
			if (o instanceof Element) {
				Element childElement = (Element) o;
				setDataOfChildElement(childElement);
			}
		}
		return true;
	}
	
	@Override
	public String getName() {
		return getConditionName();
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		if (attr.getValue().contains("~"))
			attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		if (attr.getName().equals(ATTRIBUTE_KEY_ID)) {
			try {
				setRowId(Integer.parseInt(attr.getValue()));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			if (attr.getName().equals("experimentname"))
				setExperimentName(attr.getValue());
			else
				if (attr.getName().equals("database"))
					setDatabase(attr.getValue());
				else
					if (attr.getName().equals("remark"))
						setExperimentRemark(attr.getValue());
					else
						if (attr.getName().equals("coordinator"))
							setExperimentCoordinator(attr.getValue());
						else
							if (attr.getName().equals("excelfileid"))
								setExperimentDatabaseId(attr.getValue());
							else
								if (attr.getName().equals("importusername"))
									;// ignore
								else
									if (attr.getName().equals("importusergroup"))
										;// ignore
									else
										if (attr.getName().equals("importdate"))
											setExperimentImportdate(AttributeHelper.getDateFromString(attr.getValue()));
										else
											if (attr.getName().equals("startdate"))
												setExperimentStartDate(AttributeHelper.getDateFromString(attr.getValue()));
											else
												if (attr.getName().equals("storagedate") || attr.getName().equals("storagetime"))
													setExperimentStorageDate(AttributeHelper.getDateFromString(attr.getValue()));
												else
													if (attr.getName().equals("measurements"))
														;// ignore
													else
														if (attr.getName().equals("imagefiles"))
															;// ignore
														else
															if (attr.getName().equals("sizekb"))
																;// ignore
															else
																if (attr.getName().equals("origin"))
																	setExperimentDatabaseOriginId(attr.getValue());
																else
																	if (attr.getName().equals("outlier"))
																		setExperimentGlobalOutlierInfo(attr.getValue());
																	else
																		if (attr.getName().equals(ATTRIBUTE_KEY_NAME))
																			setSpecies(attr.getValue());
																		else
																			if (attr.getName().equals(ATTRIBUTE_KEY_GENOTYPE))
																				setGenotype(attr.getValue());
																			else
																				if (attr.getName().equals(ATTRIBUTE_KEY_GROWTHCONDITIONS))
																					setGrowthconditions(attr.getValue());
																				else
																					if (attr.getName().equals(ATTRIBUTE_KEY_TREATMENT))
																						setTreatment(attr.getValue());
																					else
																						if (attr.getName().equals(ATTRIBUTE_KEY_VARIETY))
																							setVariety(attr.getValue());
																						else
																							if (attr.getName().equals("experimenttype"))
																								setExperimentType(attr.getValue());
																							else
																								if (attr.getName().equals(ATTRIBUTE_KEY_SEQUENCE))
																									setSequence(attr.getValue());
																								else
																									if (attr.getName().equals(ATTRIBUTE_KEY_FILES))
																										setFiles(attr.getValue());
																									else
																										if (attr.getName().equals("settings"))
																											setExperimentSettings(attr.getValue());
																										else
																											if (attr.getName().equals("annotation"))
																												setExperimentAnnotation(attr.getValue());
																											else
																												System.err.println("Internal Error: Unknown Condition Attribute: "
																														+ attr.getName());
	}
	
	@Override
	public void setDataOfChildElement(Element childElement) {
		if (childElement.getName().equals("sample")) {
			SampleInterface s = Experiment.getTypeManager().getNewSample(this);
			if (s.setData(childElement))
				add(s);
		}
	}
	
	@Override
	public void setExperimentName(String experimentName) {
		header.setExperimentname(experimentName != null ? experimentName.intern() : null);
	}
	
	@Override
	public void setDatabase(String database) {
		header.setDatabase(database != null ? database.intern() : null);
	}
	
	@Override
	public void setExperimentCoordinator(String experimentCoordinator) {
		header.setCoordinator(experimentCoordinator != null ? experimentCoordinator.intern() : null);
	}
	
	@Override
	public void setExperimentDatabaseId(String databaseId) {
		header.setDatabaseId(databaseId != null ? databaseId.intern() : null);
	}
	
	public void setExperimentDatabaseOriginId(String originId) {
		header.setOriginDbId(originId != null ? originId.intern() : null);
	}
	
	public void setExperimentGlobalOutlierInfo(String outlier) {
		header.setGlobalOutlierInfo(outlier != null ? outlier.intern() : null);
	}
	
	public String getExperimentDatabaseOriginId() {
		return header.getOriginDbId();
	}
	
	@Override
	public String getExperimentGlobalOutlierInfo() {
		return header.getGlobalOutlierInfo();
	}
	
	@Override
	public String getExperimentDatabaseId() {
		return header.getDatabaseId();
	}
	
	@Override
	public String getExperimentCoordinator() {
		return header.getCoordinator();
	}
	
	@Override
	public void setExperimentStartDate(Date experimentStartDate) {
		header.setStartDate(experimentStartDate != null ? experimentStartDate : null);
	}
	
	@Override
	public void setExperimentSettings(String settings) {
		header.setSettings(settings);
	}
	
	@Override
	public void setExperimentAnnotation(String annotation) {
		header.setAnnotation(annotation);
	}
	
	@Override
	public String getExperimentAnnotation() {
		return header.getAnnotation();
	}
	
	@Override
	public void setExperimentStorageDate(Date experimentStorageDate) {
		header.setStorageTime(experimentStorageDate != null ? experimentStorageDate : null);
	}
	
	@Override
	public Date getExperimentStorageDate() {
		return header.getStorageTime();
	}
	
	@Override
	public void setSpecies(String species) {
		this.species = species != null ? species.intern() : null;
	}
	
	@Override
	public void setGenotype(String genotype) {
		this.genotype = genotype != null ? genotype.intern() : genotype;
	}
	
	@Override
	public void setGrowthconditions(String growthconditions) {
		this.growthconditions = growthconditions != null ? growthconditions.intern() : null;
	}
	
	@Override
	public String getGrowthconditions() {
		return growthconditions;
	}
	
	@Override
	public void setTreatment(String treatment) {
		this.treatment = treatment != null ? treatment.intern() : null;
	}
	
	@Override
	public String getExperimentType() {
		return header.getExperimentType();
	}
	
	@Override
	public String getSequence() {
		return sequence;
	}
	
	@Override
	public String getFiles() {
		return files;
	}
	
	@Override
	public void setFiles(String files) {
		this.files = files;
	}
	
	@Override
	public void setVariety(String variety) {
		this.variety = variety != null ? variety.intern() : null;
	}
	
	@Override
	public String getVariety() {
		return variety;
	}
	
	@Override
	public void setExperimentType(String experimenttype) {
		header.setExperimentType(experimenttype != null ? experimenttype.intern() : null);
	}
	
	@Override
	public void setSequence(String sequence) {
		this.sequence = sequence != null ? sequence.intern() : null;
	}
	
	@Override
	public void getExperimentHeader(StringBuilder r, int measurementcount) {
		header.toString(r, measurementcount);
	}
	
	@Override
	public SubstanceInterface getParentSubstance() {
		return parent;
	}
	
	@Override
	public void getXMLAttributeStringForDocument(StringBuilder r) {
		Substance.getAttributeString(r, attributeNamesForDocument,
				new Object[] {
						getGenotype(), getGrowthconditions(),
						getRowId(), getSpecies(), getTreatment(),
						getVariety(), getSequence(), getFiles() });
	}
	
	@Override
	public void getStringForDocument(StringBuilder r) {
		r.append("<line");
		getXMLAttributeStringForDocument(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</line>");
	}
	
	@Override
	public int compareTo(ConditionInterface otherSeries) {
		String a = getConditionName(false, true);
		String b = ((Condition) otherSeries).getConditionName(false, true);
		int res = a.compareTo(b);
		// System.out.println("Compare: " + res + " / " + a + " / " + b);
		return res;
	}
	
	@Override
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	
	@Override
	public int getRowId() {
		return rowId;
	}
	
	@Override
	public void setExperimentImportdate(Date experimentimportdate) {
		header.setImportDate(experimentimportdate);
	}
	
	@Override
	public Date getExperimentImportDate() {
		return header.getImportdate();
	}
	
	@Override
	public void setExperimentRemark(String experimentRemark) {
		header.setRemark(experimentRemark != null ? experimentRemark.intern() : null);
	}
	
	@Override
	public String getExperimentRemark() {
		return header.getRemark();
	}
	
	@Override
	public void setParent(SubstanceInterface md) {
		parent = md;
	}
	
	@Override
	public void setExperimentInfo(ExperimentHeaderInterface header) {
		this.header = header;
	}
	
	@Override
	public ExperimentHeaderInterface getExperimentHeader() {
		return header;
	}
	
	@Override
	public boolean add(SampleInterface arg0) {
		return samples.add(arg0);
	}
	
	/*
	 * Delegate Methods
	 */
	
	@Override
	public boolean addAll(Collection<? extends SampleInterface> arg0) {
		return samples.addAll(arg0);
	}
	
	@Override
	public void clear() {
		samples.clear();
	}
	
	@Override
	public boolean contains(Object arg0) {
		return samples.contains(arg0);
	}
	
	@Override
	public boolean containsAll(Collection<?> arg0) {
		return samples.containsAll(arg0);
	}
	
	@Override
	public boolean isEmpty() {
		return samples.isEmpty();
	}
	
	@Override
	public boolean remove(Object arg0) {
		((SampleInterface) arg0).setParent(null);
		return samples.remove(arg0);
	}
	
	@Override
	public boolean removeAll(Collection<?> arg0) {
		for (Object o : arg0) {
			((SampleInterface) o).setParent(null);
		}
		return samples.removeAll(arg0);
	}
	
	@Override
	public boolean retainAll(Collection<?> arg0) {
		for (SampleInterface o : samples)
			o.setParent(null);
		
		boolean res = samples.retainAll(arg0);
		
		for (SampleInterface o : samples)
			o.setParent(this);
		
		return res;
	}
	
	@Override
	public int size() {
		return samples.size();
	}
	
	@Override
	public Object[] toArray() {
		return samples.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] arg0) {
		return samples.toArray(arg0);
	}
	
	@Override
	public Iterator<SampleInterface> iterator() {
		return samples.iterator();
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		Object[] values = getAttributeValues();
		int idx = 0;
		for (String name : attributeNames) {
			attributeValueMap.put(name, values[idx++]);
		}
	}
	
	@Override
	public void setField(ConditionInfo field, String value) {
		switch (field) {
			case SPECIES:
				setSpecies(value);
				break;
			case GENOTYPE:
				setGenotype(value);
				break;
			case VARIETY:
				setVariety(value);
				break;
			case GROWTHCONDITIONS:
				setGrowthconditions(value);
				break;
			case TREATMENT:
				setTreatment(value);
				break;
			case SEQUENCE:
				setSequence(value);
				break;
			case FILES:
				setFiles(value);
				break;
			case IGNORED_FIELD:
				// intentionally empty
				break;
		}
	}
	
	@Override
	public String getField(ConditionInfo field) {
		switch (field) {
			case SPECIES:
				return getSpecies();
			case GENOTYPE:
				return getGenotype();
			case VARIETY:
				return getVariety();
			case GROWTHCONDITIONS:
				return getGrowthconditions();
			case TREATMENT:
				return getTreatment();
			case SEQUENCE:
				return getSequence();
			case FILES:
				return getFiles();
			case IGNORED_FIELD:
				// intentionally empty
				return null;
		}
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Condition))
			return false;
		Condition c = (Condition) obj;
		return (species == null && c.species == null || (species != null && species.equals(c.species))) &&
				(genotype == null && c.genotype == null || (genotype != null && genotype.equals(c.genotype))) &&
				(growthconditions == null && c.growthconditions == null || (growthconditions != null && growthconditions.equals(c.growthconditions))) &&
				(treatment == null && c.treatment == null || (treatment != null && treatment.equals(c.treatment))) &&
				(variety == null && c.variety == null || (variety != null && variety.equals(c.variety))) &&
				(sequence == null && c.sequence == null || (sequence != null && sequence.equals(c.sequence))) &&
				(sequence == null && c.sequence == null || (sequence != null && sequence.equals(c.sequence))) &&
				(rowId == c.rowId);
	}
	
	@Override
	public int hashCode() {
		String s1 = species + ";" + genotype + ";" + growthconditions + ";" + treatment + ";" + variety + ";" + rowId + ";" + files;
		return s1.hashCode();
	}
	
	@Override
	public ConditionInterface clone(SubstanceInterface parent) {
		ConditionInterface c = Experiment.getTypeManager().getNewCondition(parent);
		c.setExperimentInfo(getExperimentHeader().clone());
		c.setSpecies(getSpecies());
		c.setGenotype(getGenotype());
		c.setGrowthconditions(getGrowthconditions());
		c.setTreatment(getTreatment());
		c.setTreatment(getTreatment());
		c.setVariety(getVariety());
		c.setRowId(getRowId());
		c.setSequence(getSequence());
		c.setFiles(getFiles());
		return c;
	}
	
	@Override
	public ArrayList<SampleInterface> getSortedSamples() {
		ArrayList<SampleInterface> res = new ArrayList<SampleInterface>(samples);
		Collections.sort(res);
		return res;
	}
	
	@Override
	public String getExperimentOriginDbId() {
		return header.getOriginDbId();
	}
	
	@Override
	public void setExperimentHeader(ExperimentHeaderInterface header) {
		this.header = header;
	}
	
	public Map<String, Object> getAttributeMap() {
		Map<String, Object> attributeValueMap = new HashMap<String, Object>();
		fillAttributeMap(attributeValueMap);
		return attributeValueMap;
	}
	
	public static String[] getExperimentFields() {
		return attributeNameWithoutDocumentFields;
	}
	
	@Override
	public void visitNumericValues(NumericValueVisitor visitor) {
		for (SampleInterface sa : this)
			for (NumericMeasurementInterface nmi : sa) {
				if (nmi instanceof NumericMeasurement) {
					visitor.visit(nmi);
				}
			}
	}
}
