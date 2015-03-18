package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.Date;
import java.util.LinkedHashSet;

import org.SystemAnalysis;

public class ExperimentDataAnnotation {
	
	private LinkedHashSet<String> expname;
	private LinkedHashSet<String> expsrc;
	private LinkedHashSet<String> expcoord;
	private LinkedHashSet<String> expstartdate;
	private LinkedHashSet<String> expimportdate;
	private LinkedHashSet<String> condspecies;
	private LinkedHashSet<String> condgenotype;
	private LinkedHashSet<String> condtreatment;
	private LinkedHashSet<String> condvariety;
	private LinkedHashSet<String> samptimepoint;
	private LinkedHashSet<String> samptimeunit;
	private LinkedHashSet<String> sampcomp;
	private LinkedHashSet<String> sampmeas;
	private LinkedHashSet<Integer> replIDs;
	private LinkedHashSet<String> qualityIDs;
	private LinkedHashSet<String> substances;
	private LinkedHashSet<Double> positions;
	private LinkedHashSet<String> positionUnits;
	private boolean setAsDefault = false;
	
	public ExperimentDataAnnotation() {
		super();
		expname = new LinkedHashSet<String>();
		expsrc = new LinkedHashSet<String>();
		expcoord = new LinkedHashSet<String>();
		expstartdate = new LinkedHashSet<String>();
		expimportdate = new LinkedHashSet<String>();
		condspecies = new LinkedHashSet<String>();
		condgenotype = new LinkedHashSet<String>();
		condtreatment = new LinkedHashSet<String>();
		condvariety = new LinkedHashSet<String>();
		samptimepoint = new LinkedHashSet<String>();
		samptimeunit = new LinkedHashSet<String>();
		sampcomp = new LinkedHashSet<String>();
		sampmeas = new LinkedHashSet<String>();
		replIDs = new LinkedHashSet<Integer>();
		qualityIDs = new LinkedHashSet<String>();
		substances = new LinkedHashSet<String>();
		positions = new LinkedHashSet<Double>();
		positionUnits = new LinkedHashSet<String>();
		
	}
	
	public void mergeAnnotations(ExperimentDataAnnotation annotations) {
		if (annotations != null) {
			expname.addAll(annotations.getExpname());
			expsrc.addAll(annotations.getExpsrc());
			expcoord.addAll(annotations.getExpcoord());
			expstartdate.addAll(annotations.getExpstartdate());
			expimportdate.addAll(annotations.getExpimportdate());
			condspecies.addAll(annotations.getCondspecies());
			condgenotype.addAll(annotations.getCondgenotype());
			condtreatment.addAll(annotations.getCondtreatment());
			condvariety.addAll(annotations.getCondvariety());
			samptimepoint.addAll(annotations.getSamptimepoint());
			samptimeunit.addAll(annotations.getSamptimeunit());
			sampcomp.addAll(annotations.getSampcomp());
			sampmeas.addAll(annotations.getSampmeas());
			replIDs.addAll(annotations.getReplicateIDs());
			qualityIDs.addAll(annotations.getQualityIDs());
			substances.addAll(annotations.getSubstances());
			positions.addAll(annotations.getPositions());
			positionUnits.addAll(annotations.getPositionUnits());
		}
	}
	
	public void setExpname(LinkedHashSet<String> expname) {
		this.expname = expname;
	}
	
	public LinkedHashSet<String> getExpname() {
		return expname;
	}
	
	public void setExpsrc(LinkedHashSet<String> expsrc) {
		this.expsrc = expsrc;
	}
	
	public LinkedHashSet<String> getExpsrc() {
		return expsrc;
	}
	
	public void setExpcoord(LinkedHashSet<String> expcoord) {
		this.expcoord = expcoord;
	}
	
	public LinkedHashSet<String> getExpcoord() {
		return expcoord;
	}
	
	public void setExpstartdate(LinkedHashSet<String> expstartdate) {
		this.expstartdate = expstartdate;
	}
	
	public LinkedHashSet<String> getExpstartdate() {
		return expstartdate;
	}
	
	public void setExpimportdate(LinkedHashSet<String> expimportdate) {
		this.expimportdate = expimportdate;
	}
	
	public LinkedHashSet<String> getExpimportdate() {
		return expimportdate;
	}
	
	public void setCondspecies(LinkedHashSet<String> condspecies) {
		this.condspecies = condspecies;
	}
	
	public LinkedHashSet<String> getCondspecies() {
		return condspecies;
	}
	
	public void setCondgenotype(LinkedHashSet<String> condgenotype) {
		this.condgenotype = condgenotype;
	}
	
	public LinkedHashSet<String> getCondgenotype() {
		return condgenotype;
	}
	
	public void setCondtreatment(LinkedHashSet<String> condtreatment) {
		this.condtreatment = condtreatment;
	}
	
	public void setCondvariety(LinkedHashSet<String> condvariety) {
		this.condtreatment = condvariety;
	}
	
	public LinkedHashSet<String> getCondtreatment() {
		return condtreatment;
	}
	
	public LinkedHashSet<String> getCondvariety() {
		return condvariety;
	}
	
	public void setSamptimepoint(LinkedHashSet<String> samptimepoint) {
		this.samptimepoint = samptimepoint;
	}
	
	public LinkedHashSet<String> getSamptimepoint() {
		return samptimepoint;
	}
	
	public void setSamptimeunit(LinkedHashSet<String> samptimeunit) {
		this.samptimeunit = samptimeunit;
	}
	
	public LinkedHashSet<String> getSamptimeunit() {
		return samptimeunit;
	}
	
	public void setSampcomp(LinkedHashSet<String> sampcomp) {
		this.sampcomp = sampcomp;
	}
	
	public LinkedHashSet<String> getSampcomp() {
		return sampcomp;
	}
	
	public void setSampmeas(LinkedHashSet<String> sampmeas) {
		this.sampmeas = sampmeas;
	}
	
	public LinkedHashSet<String> getSampmeas() {
		return sampmeas;
	}
	
	public void setReplicateIDs(LinkedHashSet<Integer> replIDs) {
		this.replIDs = replIDs;
	}
	
	public LinkedHashSet<Integer> getReplicateIDs() {
		return replIDs;
	}
	
	public void setQualityIDs(LinkedHashSet<String> qualityIDs) {
		this.qualityIDs = qualityIDs;
	}
	
	public LinkedHashSet<String> getQualityIDs() {
		return qualityIDs;
	}
	
	public void setSubstances(LinkedHashSet<String> substances) {
		this.substances = substances;
	}
	
	public LinkedHashSet<String> getSubstances() {
		return substances;
	}
	
	public void setPositions(LinkedHashSet<Double> positions) {
		this.positions = positions;
	}
	
	public LinkedHashSet<Double> getPositions() {
		return positions;
	}
	
	public void setPositionUnits(LinkedHashSet<String> positionUnits) {
		this.positionUnits = positionUnits;
	}
	
	public LinkedHashSet<String> getPositionUnits() {
		return positionUnits;
	}
	
	public void setSetAsDefault(boolean setAsDefault) {
		this.setAsDefault = setAsDefault;
	}
	
	public boolean isSetAsDefault() {
		return setAsDefault;
	}
	
	public static ExperimentDataAnnotation getEmptyAnnotation() {
		ExperimentDataAnnotation ea = new ExperimentDataAnnotation();
		ea.expname = getSet("Imported Dataset " + SystemAnalysis.getCurrentTime());
		ea.expsrc = null;
		ea.expcoord = getSet(SystemAnalysis.getUserName());
		ea.expstartdate = getSet(new Date() + "");
		ea.expimportdate = getSet(new Date() + "");
		ea.condspecies = getSet("Unspecified Species");
		ea.condgenotype = getSet("Unspecified Genotype");
		ea.condtreatment = getSet("Unspecified Treatment");
		ea.condvariety = getSet("Unspecified Variety");
		ea.samptimepoint = getSet("-1");
		ea.samptimeunit = getSet("-1");
		ea.sampcomp = null;
		ea.sampmeas = null;
		ea.replIDs = null;
		ea.qualityIDs = null;
		ea.substances = null;
		ea.positions = null;
		ea.positionUnits = null;
		return ea;
	}
	
	private static LinkedHashSet<String> getSet(String string) {
		LinkedHashSet<String> res = new LinkedHashSet<String>();
		res.add(string);
		return res;
	}
}
