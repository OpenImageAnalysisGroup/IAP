package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;

public class Condition implements ConditionInterface {
	
	public enum ConditionInfo {
		IGNORED_FIELD("---"), SPECIES("Species"), GENOTYPE("Genotype"), VARIETY("Variety"), GROWTHCONDITIONS(
				"Growth Condititions"), TREATMENT("Treatment"), SEQUENCE("Sequence");
		
		private String desc;
		
		ConditionInfo(String desc) {
			this.desc = desc;
		}
		
		@Override
		public String toString() {
			return desc;
		}
		
		public static ArrayList<ConditionInfo> getList() {
			ArrayList<ConditionInfo> result = new ArrayList<ConditionInfo>();
			for (ConditionInfo ci : ConditionInfo.values())
				result.add(ci);
			return result;
		}
	}
	
	/**
	 * If list of state variables is modified/extended, check and modify equals,
	 * hashCode and eventually compareTo method implementation.
	 */
	private String species, genotype, growthconditions, treatment, variety;
	private int rowId;
	
	private final Set<SampleInterface> samples = new LinkedHashSet<SampleInterface>();
	private SubstanceInterface parent;
	private ExperimentHeaderInterface header;
	
	private static final String[] attributeNames = new String[] { "experimentname", "database", "experimenttype",
			"coordinator", "startdate", "importdate", "storagedate", "remark", "genotype", "growthconditions", "id", "name", "treatment",
			"variety" };
	
	private static final String[] attributeNamesForDocument = new String[] { "genotype", "growthconditions", "id",
			"name", "treatment", "variety" };
	
	public Condition(SubstanceInterface md) {
		parent = md;
		header = new ExperimentHeader();
	}
	
	@SuppressWarnings("unchecked")
	public Condition(SubstanceInterface s, Map attributemap) {
		this(s);
		for (Object o : attributemap.keySet()) {
			if (o instanceof String) {
				String key = (String) o;
				if (attributemap.get(key) != null && attributemap.get(key) instanceof String)
					setAttribute(new Attribute(key, (String) attributemap.get(key)));
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
	
	private Object[] getAttributeValues() {
		return new Object[] { getExperimentName(), getDatabase(), getExperimentType(), getCoordinator(),
				getExperimentStartDate(), getExperimentImportDate(), getExperimentStorageDate(), getExperimentRemark(), getGenotype(),
				getGrowthconditions(), getRowId(), getSpecies(), getTreatment(), getVariety() };
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
			String res = /* getExperimentName() + ": " + */getSpecies() + " / " + getGenotype() + " / " + getTreatment() + " / "
					+ getGrowthconditions() + " / " + getVariety() + "/" + getSequence();
			if (cached)
				conditionCache = res;
			return res;
		} else {
			if (cached && conditionCache != null)
				return conditionCache;
			String serie;
			
			String species = getSpecies();
			String genotype = getGenotype();
			String treatment = getTreatment();
			
			serie = species;
			if (genotype != null && genotype.length() > 0
					&& !genotype.equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING))
				serie += "/" + genotype;
			if (treatment != null && treatment.length() > 0 && !treatment.equals("null")
					&& !treatment.equals(ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING)
					&& !checkForSameGenoTypeAndTreatment(genotype, treatment))
				serie += " (" + treatment + ")";
			
			String res = getRowId() + ": " + serie;
			if (cached)
				conditionCache = res;
			return res;
		}
	}
	
	private static boolean checkForSameGenoTypeAndTreatment(String genotype, String treatment) {
		if (genotype == null || treatment == null)
			return false;
		if (genotype.equalsIgnoreCase(treatment))
			return true;
		else
			return false;
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
	public Collection<MyComparableDataPoint> getMeanMCDPs() {
		ArrayList<MyComparableDataPoint> result = new ArrayList<MyComparableDataPoint>();
		for (SampleInterface s : this) {
			SampleAverageInterface m = s.getSampleAverage();
			boolean ismean = false;
			double mean = m.getParentSample().getSampleAverage().getValue();
			double stddev = m.getParentSample().getSampleAverage().getStdDev();
			String serie = m.getParentSample().getParentCondition().getConditionName();
			String timeUnitAndTime = m.getParentSample().getSampleTime();
			String measurementUnit = m.getUnit();
			long timeValueForComparison = m.getParentSample().getSampleFineTimeOrRowId() != null && m.getParentSample().getSampleFineTimeOrRowId() > 0 ? m.getParentSample().getSampleFineTimeOrRowId() : m
					.getParentSample().getTime();
			TtestInfo ttestInfo = m.getParentSample().getTtestInfo();
			String timeUnit = m.getParentSample().getTimeUnit();
			int seriesID = m.getParentSample().getParentCondition().getSeriesId();
			int replicate = m.getParentSample().size();
			Measurement reference = m;
			MyComparableDataPoint mcdp = new MyComparableDataPoint(ismean, mean, stddev, serie, timeUnitAndTime,
					measurementUnit, timeValueForComparison, m.getParentSample().getTime(), ttestInfo == TtestInfo.REFERENCE, ttestInfo == TtestInfo.H1,
					timeUnit, seriesID, replicate, reference);
			result.add(mcdp);
		}
		return result;
	}
	
	@Override
	public ArrayList<MyComparableDataPoint> getMCDPs() {
		ArrayList<MyComparableDataPoint> result = new ArrayList<MyComparableDataPoint>();
		for (SampleInterface s : this) {
			for (NumericMeasurementInterface m : s) {
				boolean ismean = false;
				double mean = m.getValue();
				double stddev = Double.NaN;
				String serie = m.getParentSample().getParentCondition().getConditionName();
				String timeUnitAndTime = m.getParentSample().getSampleTime();
				String measurementUnit = m.getUnit();
				long timeValueForComparison = m.getParentSample().getSampleFineTimeOrRowId() > 0 ? m.getParentSample().getSampleFineTimeOrRowId() : m.getParentSample().getTime();
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
	public ArrayList<Double> getMeanValues() {
		ArrayList<Double> result = new ArrayList<Double>();
		for (SampleInterface s : this)
			result.add(s.getSampleAverage().getValue());
		return result;
	}
	
	@Override
	public ArrayList<Integer> getMeanTimePoints() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (SampleInterface s : this)
			result.add(s.getTime());
		return result;
	}
	
	@Override
	public ArrayList<String> getMeanTimeUnits() {
		ArrayList<String> result = new ArrayList<String>();
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
		ArrayList<Integer> x = getMeanTimePoints();
		ArrayList<Double> y = getMeanValues();
		double n = x.size();
		if (x.size() != y.size())
			ErrorMsg.addErrorMessage("Internal Error: Series Data Number Count <> Time Point Count");
		
		double beta, sum_xi_yi, x_, y_, sum_xi2; // , x_2;
		
		x_ = getAvgI(x);
		y_ = getAvgD(y);
		
		sum_xi_yi = 0;
		for (int i = 0; i < n; i++)
			sum_xi_yi += x.get(i) * y.get(i);
		
		sum_xi2 = 0;
		for (int i = 0; i < n; i++)
			sum_xi2 += x.get(i) * x.get(i);
		
		beta = (sum_xi_yi - n * x_ * y_) / (sum_xi2 - n * x_ * x_);
		
		return beta;
	}
	
	public static double getSum(ArrayList<Double> values) {
		double sum = 0;
		for (double n : values)
			sum += n;
		return sum;
	}
	
	private double getAvgI(ArrayList<Integer> meanTimePoints) {
		double sum = 0;
		for (Integer i : meanTimePoints)
			sum += i;
		return sum / meanTimePoints.size();
	}
	
	private double getAvgD(ArrayList<Double> values) {
		return getSum(values) / values.size();
	}
	
	@Override
	public String getSpecies() {
		if (species == null || species.equalsIgnoreCase(""))
			species = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return species;
	}
	
	@Override
	public String getGenotype() {
		if (genotype == null || genotype.equalsIgnoreCase(""))
			genotype = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
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
				setAttribute(a);
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
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		if (attr.getName().equals("id")) {
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
																		if (attr.getName().equals("name"))
																			setSpecies(attr.getValue());
																		else
																			if (attr.getName().equals("genotype"))
																				setGenotype(attr.getValue());
																			else
																				if (attr.getName().equals("growthconditions"))
																					setGrowthconditions(attr.getValue());
																				else
																					if (attr.getName().equals("treatment"))
																						setTreatment(attr.getValue());
																					else
																						if (attr.getName().equals("variety"))
																							setVariety(attr.getValue());
																						else
																							if (attr.getName().equals("experimenttype"))
																								setExperimentType(attr.getValue());
																							else
																								if (attr.getName().equals("sequence"))
																									setSequence(attr.getValue());
																								else
																									System.err.println("Internal Error: Unknown Condition Attribute: " + attr.getName());
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
		header.setStartdate(experimentStartDate != null ? experimentStartDate : null);
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
		return header.getSequence();
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
		header.setExperimenttype(experimenttype != null ? experimenttype.intern() : null);
	}
	
	@Override
	public void setSequence(String sequence) {
		header.setSequence(sequence != null ? sequence.intern() : null);
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
		Substance.getAttributeString(r, attributeNamesForDocument, new Object[] { getGenotype(), getGrowthconditions(),
				getRowId(), getSpecies(), getTreatment(), getVariety() });
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
		return getConditionName(false, true).compareTo(((Condition) otherSeries).getConditionName(false, true));
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
		header.setImportdate(experimentimportdate);
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
	public SampleInterface addAndMerge(SampleInterface samplenew, boolean ignoreSnapshotFineTime) {
		SampleInterface save = null;
		for (SampleInterface s : this)
			if (s.compareTo(samplenew, ignoreSnapshotFineTime) == 0) {
				save = s;
				break;
			}
		
		if (save == null) {
			add(samplenew);
			return samplenew;
		} else {
			for (NumericMeasurementInterface m : samplenew.toArray(new NumericMeasurementInterface[] {})) {
				m.setParentSample(save);
				save.add(m);
			}
			return save;
		}
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
		return samples.remove(arg0);
	}
	
	@Override
	public boolean removeAll(Collection<?> arg0) {
		return samples.removeAll(arg0);
	}
	
	@Override
	public boolean retainAll(Collection<?> arg0) {
		return samples.retainAll(arg0);
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
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		Object[] values = getAttributeValues();
		int idx = 0;
		for (String name : attributeNames) {
			attributeValueMap.put(name, values[idx++]);
		}
	}
	
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
			case SEQUENCE:
				setSequence(value);
			case IGNORED_FIELD:
				// intentionally empty
				break;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Condition))
			return false;
		String s1 = species + ";" + genotype + ";" + growthconditions + ";" + treatment + ";" + variety + ";" + rowId;
		Condition c = (Condition) obj;
		String s2 = c.species + ";" + c.genotype + ";" + c.growthconditions + ";" + c.treatment + ";" + c.variety + ";"
				+ c.rowId;
		return s1.equals(s2);
	}
	
	@Override
	public int hashCode() {
		String s1 = species + ";" + genotype + ";" + growthconditions + ";" + treatment + ";" + variety + ";" + rowId;
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
}
