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
						"coordinator", "startdate", "importdate", "remark", "genotype", "growthconditions", "id", "name", "treatment",
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
	
	public void getString(StringBuilder r) {
		r.append("<line");
		getXMLAttributeString(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</line>");
	}
	
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, attributeNames, getAttributeValues());
	}
	
	private Object[] getAttributeValues() {
		return new Object[] { getExperimentName(), getDatabase(), getExperimentType(), getCoordinator(),
							getExperimentStartDate(), getExperimentImportdate(), getExperimentRemark(), getGenotype(),
							getGrowthconditions(), getRowId(), getSpecies(), getTreatment(), getVariety() };
	}
	
	public void getStringOfChildren(StringBuilder r) {
		for (SampleInterface s : this)
			s.getString(r);
	}
	
	public String getExpAndConditionName() {
		return getExperimentName() + ": " + getConditionName();
	}
	
	@Override
	public String toString() {
		return getExpAndConditionName();
	}
	
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
			String res = getExperimentName() + ": " + getSpecies() + " / " + getGenotype() + " / " + getTreatment() + " / "
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
	
	public String getExperimentName() {
		return header.getExperimentName();
	}
	
	public String getDatabase() {
		return header.getDatabase();
	}
	
	public String getCoordinator() {
		return getExperimentCoordinator();
	}
	
	public Date getExperimentStartDate() {
		return header.getStartdate();
	}
	
	public int getConditionId() {
		return getRowId();
	}
	
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
			int timeValueForComparison = m.getParentSample().getTime();
			TtestInfo ttestInfo = m.getParentSample().getTtestInfo();
			String timeUnit = m.getParentSample().getTimeUnit();
			int seriesID = m.getParentSample().getParentCondition().getSeriesId();
			int replicate = m.getParentSample().size();
			Measurement reference = m;
			MyComparableDataPoint mcdp = new MyComparableDataPoint(ismean, mean, stddev, serie, timeUnitAndTime,
								measurementUnit, timeValueForComparison, ttestInfo == TtestInfo.REFERENCE, ttestInfo == TtestInfo.H1,
								timeUnit, seriesID, replicate, reference);
			result.add(mcdp);
		}
		return result;
	}
	
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
				int timeValueForComparison = m.getParentSample().getTime();
				TtestInfo ttestInfo = m.getParentSample().getTtestInfo();
				String timeUnit = m.getParentSample().getTimeUnit();
				int seriesID = m.getParentSample().getParentCondition().getSeriesId();
				int replicate = m.getReplicateID();
				Measurement reference = m;
				MyComparableDataPoint mcdp = new MyComparableDataPoint(ismean, mean, stddev, serie, timeUnitAndTime,
									measurementUnit, timeValueForComparison, ttestInfo == TtestInfo.REFERENCE, ttestInfo == TtestInfo.H1,
									timeUnit, seriesID, replicate, reference);
				result.add(mcdp);
			}
		}
		return result;
	}
	
	public ArrayList<Double> getMeanValues() {
		ArrayList<Double> result = new ArrayList<Double>();
		for (SampleInterface s : this)
			result.add(s.getSampleAverage().getValue());
		return result;
	}
	
	public ArrayList<Integer> getMeanTimePoints() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (SampleInterface s : this)
			result.add(s.getTime());
		return result;
	}
	
	public ArrayList<String> getMeanTimeUnits() {
		ArrayList<String> result = new ArrayList<String>();
		for (SampleInterface s : this)
			result.add(s.getTimeUnit());
		return result;
	}
	
	public double calcAlpha() {
		double alpha, y_, beta, x_;
		x_ = getAvgI(getMeanTimePoints());
		y_ = getAvgD(getMeanValues());
		beta = calcBeta();
		alpha = y_ - beta * x_;
		return alpha;
	}
	
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
	
	public String getSpecies() {
		if (species == null || species.equalsIgnoreCase(""))
			species = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return species;
	}
	
	public String getGenotype() {
		if (genotype == null || genotype.equalsIgnoreCase(""))
			genotype = ExperimentInterface.UNSPECIFIED_ATTRIBUTE_STRING;
		return genotype;
	}
	
	public String getTreatment() {
		return treatment;
	}
	
	public int getSeriesId() {
		return getRowId();
	}
	
	public boolean setData(Element condition) {
		return setData(condition, null);
	}
	
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
	
	public String getName() {
		return getConditionName();
	}
	
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
												if (attr.getName().equals("measurements"))
													;// ignore
												else
													if (attr.getName().equals("imagefiles"))
														;// ignore
													else
														if (attr.getName().equals("sizekb"))
															;// ignore
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
	
	public void setDataOfChildElement(Element childElement) {
		if (childElement.getName().equals("sample")) {
			SampleInterface s = Experiment.getTypeManager().getNewSample(this);
			if (s.setData(childElement))
				add(s);
		}
	}
	
	public void setExperimentName(String experimentName) {
		header.setExperimentname(experimentName != null ? experimentName.intern() : null);
	}
	
	public void setDatabase(String database) {
		header.setDatabase(database != null ? database.intern() : null);
	}
	
	public void setExperimentCoordinator(String experimentCoordinator) {
		header.setCoordinator(experimentCoordinator != null ? experimentCoordinator.intern() : null);
	}
	
	public void setExperimentDatabaseId(String databaseId) {
		header.setDatabaseId(databaseId != null ? databaseId.intern() : null);
	}
	
	public String getExperimentDatabaseId() {
		return header.getDatabaseId();
	}
	
	public String getExperimentCoordinator() {
		return header.getCoordinator();
	}
	
	public void setExperimentStartDate(Date experimentStartDate) {
		header.setStartdate(experimentStartDate != null ? experimentStartDate : null);
	}
	
	public void setSpecies(String species) {
		this.species = species != null ? species.intern() : null;
	}
	
	public void setGenotype(String genotype) {
		this.genotype = genotype != null ? genotype.intern() : genotype;
	}
	
	public void setGrowthconditions(String growthconditions) {
		this.growthconditions = growthconditions != null ? growthconditions.intern() : null;
	}
	
	public String getGrowthconditions() {
		return growthconditions;
	}
	
	public void setTreatment(String treatment) {
		this.treatment = treatment != null ? treatment.intern() : null;
	}
	
	public String getExperimentType() {
		return header.getExperimentType();
	}
	
	public String getSequence() {
		return header.getSequence();
	}
	
	public void setVariety(String variety) {
		this.variety = variety != null ? variety.intern() : null;
	}
	
	public String getVariety() {
		return variety;
	}
	
	public void setExperimentType(String experimenttype) {
		header.setExperimenttype(experimenttype != null ? experimenttype.intern() : null);
	}
	
	public void setSequence(String sequence) {
		header.setSequence(sequence != null ? sequence.intern() : null);
	}
	
	public void getExperimentHeader(StringBuilder r, int measurementcount) {
		header.toString(r, measurementcount);
	}
	
	public SubstanceInterface getParentSubstance() {
		return parent;
	}
	
	public void getXMLAttributeStringForDocument(StringBuilder r) {
		Substance.getAttributeString(r, attributeNamesForDocument, new Object[] { getGenotype(), getGrowthconditions(),
							getRowId(), getSpecies(), getTreatment(), getVariety() });
	}
	
	public void getStringForDocument(StringBuilder r) {
		r.append("<line");
		getXMLAttributeStringForDocument(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</line>");
	}
	
	public int compareTo(ConditionInterface otherSeries) {
		return getConditionName(false, true).compareTo(((Condition) otherSeries).getConditionName(false, true));
	}
	
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	
	public int getRowId() {
		return rowId;
	}
	
	public void setExperimentImportdate(Date experimentimportdate) {
		header.setImportdate(experimentimportdate);
	}
	
	public Date getExperimentImportdate() {
		return header.getImportdate();
	}
	
	public void setExperimentRemark(String experimentRemark) {
		header.setRemark(experimentRemark != null ? experimentRemark.intern() : null);
	}
	
	public String getExperimentRemark() {
		return header.getRemark();
	}
	
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
			for (NumericMeasurementInterface m : samplenew) {
				m.setParentSample(save);
				save.add(m);
			}
			return save;
		}
	}
	
	public void setParent(SubstanceInterface md) {
		parent = md;
	}
	
	public void setExperimentInfo(ExperimentHeaderInterface header) {
		this.header = header;
	}
	
	public ExperimentHeaderInterface getExperimentHeader() {
		return header;
	}
	
	public boolean add(SampleInterface arg0) {
		return samples.add(arg0);
	}
	
	/*
	 * Delegate Methods
	 */

	public boolean addAll(Collection<? extends SampleInterface> arg0) {
		return samples.addAll(arg0);
	}
	
	public void clear() {
		samples.clear();
	}
	
	public boolean contains(Object arg0) {
		return samples.contains(arg0);
	}
	
	public boolean containsAll(Collection<?> arg0) {
		return samples.containsAll(arg0);
	}
	
	public boolean isEmpty() {
		return samples.isEmpty();
	}
	
	public boolean remove(Object arg0) {
		return samples.remove(arg0);
	}
	
	public boolean removeAll(Collection<?> arg0) {
		return samples.removeAll(arg0);
	}
	
	public boolean retainAll(Collection<?> arg0) {
		return samples.retainAll(arg0);
	}
	
	public int size() {
		return samples.size();
	}
	
	public Object[] toArray() {
		return samples.toArray();
	}
	
	public <T> T[] toArray(T[] arg0) {
		return samples.toArray(arg0);
	}
	
	public Iterator<SampleInterface> iterator() {
		return samples.iterator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
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
		return c;
	}
	
	@Override
	public ArrayList<SampleInterface> getSortedSamples() {
		ArrayList<SampleInterface> res = new ArrayList<SampleInterface>(samples);
		Collections.sort(res);
		return res;
	}
	
}
