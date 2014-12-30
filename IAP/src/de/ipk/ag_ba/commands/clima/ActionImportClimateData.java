package de.ipk.ag_ba.commands.clima;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TreeMap;

import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.DataMappingTypeManagerInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataFileReader;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.OpenExcelFileDialogService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;

public class ActionImportClimateData extends AbstractNavigationAction {
	
	private StringBuilder res = new StringBuilder();
	
	MainPanelComponent mpc;
	
	private ExperimentInterface experimentResult = null;
	
	private NavigationButton src;
	
	public ActionImportClimateData(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		if (experimentResult != null && mpc != null)
			return;
		res = new StringBuilder();
		File excelFile = OpenExcelFileDialogService.getExcelFile();
		if (excelFile != null) {
			DateFormat dateFormat = DateFormat.getDateInstance(
					DateFormat.DEFAULT, Locale.GERMAN);
			GregorianCalendar gc = new GregorianCalendar();
			
			ArrayList<Double> hours = new ArrayList<Double>();
			ArrayList<Date> days = new ArrayList<Date>();
			ArrayList<Double> temps = new ArrayList<Double>();
			
			res.append("<li>Selected input file: " + excelFile.getAbsolutePath());
			TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
			// A1: Uhrzeit, B1: Datum
			String check1time = myData.getCellData(2, 1, "") + "";
			String check2date = myData.getCellData(3, 1, "") + "";
			
			if (check1time.equals("Datum von") && check2date.equals("bis")) {
				res.append("<li>Read template type 2 (barley GH) ('Datum von', 'bis')");
				int tempCol_Day = -1;
				int tempCol_Night = -1;
				for (int col = 1; col <= myData.getMaximumCol(); col++) {
					String heading = myData.getCellData(col, 1, null) + "";
					if (tempCol_Day < 0 && heading.equals("Ti Tag")) {
						tempCol_Day = col;
					}
					if (tempCol_Night < 0 && heading.equals("Ti Nacht")) {
						tempCol_Night = col;
					}
				}
				if (tempCol_Day == -1 || tempCol_Night == -1)
					res.append("<li>Greenhouse temperature not contained in the data set! " +
							"Did not find column headings 'Ti Tag' and 'Ti Nacht' in fields row 1!");
				else {
					for (int row = 2; row <= myData.getMaximumRow(); row++) {
						Date date = myData.getCellDataDateObject(2, row, null);
						Double time_Sun_Morning = (Double) myData.getCellData(4, row, null);
						Double time_Sun_Evening = (Double) myData.getCellData(5, row, null);
						Double temp_Day = (Double) myData.getCellData(tempCol_Day, row, null);
						Double temp_Night = (Double) myData.getCellData(tempCol_Night, row, null);
						if (time_Sun_Morning != null && time_Sun_Evening != null && temp_Day != null && temp_Night != null && date != null) {
							double hourProgress_Day = (time_Sun_Evening - time_Sun_Morning) * 24d;
							hours.add(-13d);
							days.add(date);
							temps.add(temp_Day);
							
							double hourProgress_Night = 24d - (time_Sun_Evening - time_Sun_Morning) * 24d;
							hours.add(-11d);
							days.add(date);
							temps.add(temp_Night);
						}
					}
					processTemperatureAndTimeData(excelFile, gc, hours, days, temps, false);
				}
			} else
				if (!check1time.equals("Uhrzeit")) {
					res.append("<li>Cell A1 needs to contain the string 'Uhrzeit' (or 'Datum von' for template 2)");
				} else
					if (!check2date.equals("Datum"))
						res.append("<li>Cell B1 needs to contain the string 'Datum'");
					else {
						// C2 ... N2: Data columns, e.g.
						// Aussentemperatur Licht Regen Sollwert Heizung Sollwert Lüftung Înnentemperatur
						// rel. Luftfeuchte Lüftung Ost Lüftung west Schattierung Aussenschatten
						// Zeit Assilicht
						// >> Înnentemperatur <<
						int tempCol = -1;
						for (int col = 1; col <= myData.getMaximumCol(); col++) {
							String heading = myData.getCellData(col, 2, null) + "";
							if (heading.equals("Înnentemperatur")) {
								tempCol = col;
							}
						}
						if (tempCol == -1)
							res.append("<li>Greenhouse temperature not contained in the data set! " +
									"Did not find column heading 'Înnentemperatur' in fields row 2!");
						else {
							for (int row = 4; row <= myData.getMaximumRow(); row++) {
								Double time = (Double) myData.getCellData(1, row, null);
								String dateString = (String) myData.getCellData(2, row, "");
								Date date = dateFormat.parse(dateString);
								Double temp = (Double) myData.getCellData(tempCol, row, null);
								if (time != null && date != null && temp != null) {
									double hourProgress = time * 24d;
									hours.add(hourProgress);
									days.add(date);
									temps.add(temp);
								}
							}
							processTemperatureAndTimeData(excelFile, gc, hours, days, temps, true);
						}
					}
		} else
			res.append("<li>No input file file selected!");
	}
	
	private void processTemperatureAndTimeData(File excelFile, GregorianCalendar gc, ArrayList<Double> hours, ArrayList<Date> days, ArrayList<Double> temps,
			boolean addMinMax) {
		int nmax = hours.size();
		if (nmax > 0) {
			TreeMap<Date, Double> day2avg = new TreeMap<Date, Double>();
			TreeMap<Date, Double> day2min = new TreeMap<Date, Double>();
			TreeMap<Date, Double> day2max = new TreeMap<Date, Double>();
			int foundNegative = 0;
			for (int i = 0; i < nmax - 1 + foundNegative; i++) {
				double hourDiff;
				if (hours.get(i) < 0) {
					hourDiff = -hours.get(i);
					foundNegative = 1;
				} else
					hourDiff = hours.get(i + 1) - hours.get(i);
				if (hourDiff < 0)
					hourDiff = hours.get(i + 1) + 24 - hours.get(i);
				double temp = temps.get(i) * hourDiff / 24;
				Date day = days.get(i);
				double tempSumOfDay = day2avg.containsKey(day) ?
						day2avg.get(day) : 0;
				day2avg.put(day, tempSumOfDay + temp);
				if (!day2min.containsKey(day) || day2min.get(day) > temps.get(i))
					day2min.put(day, temps.get(i));
				if (!day2max.containsKey(day) || day2max.get(day) < temps.get(i))
					day2max.put(day, temps.get(i));
			}
			
			DataMappingTypeManagerInterface tm = Experiment.getTypeManager();
			
			Experiment e = new Experiment();
			experimentResult = e;
			ExperimentHeaderInterface ehi = processData(excelFile, gc, day2avg, day2min, day2max, tm, e, addMinMax);
			ehi.setDatabase("");
			ehi.setDatabaseId("");
			ehi.setExperimentType("Climate");
			ehi.setOriginDbId(excelFile.getAbsolutePath());
			ehi.setImportUserName(SystemAnalysis.getUserName());
			ehi.setNumberOfFiles(0);
			String fn = excelFile.getName();
			if (fn.contains("."))
				fn = fn.substring(0, fn.lastIndexOf("."));
			ehi.setExperimentname(fn);
			for (SubstanceInterface si : e)
				for (ConditionInterface ci : si)
					ci.setExperimentHeader(ehi);
			e.numberConditions();
			ExperimentHeaderInfoPanel info = new ExperimentHeaderInfoPanel();
			info.setExperimentInfo(null, ehi, true, e);
			mpc = new MainPanelComponent(info, true);
			
		}
	}
	
	private ExperimentHeaderInterface processData(File excelFile, GregorianCalendar gc, TreeMap<Date, Double> day2avg, TreeMap<Date, Double> day2min,
			TreeMap<Date, Double> day2max,
			DataMappingTypeManagerInterface tm, Experiment e, boolean addMinMax) {
		ExperimentHeaderInterface ehi = new ExperimentHeader();
		ehi.setExperimentname(excelFile.getName());
		ehi.setDatabase("");
		ehi.setCoordinator(SystemAnalysis.getUserName());
		ehi.setStartDate(day2avg.firstKey());
		ehi.setImportDate(day2avg.lastKey());
		ehi.setStorageTime(new Date());
		ehi.setRemark("Import at " + SystemAnalysis.getCurrentTime() + " by " + SystemAnalysis.getUserName());
		e.setHeader(ehi);
		
		{
			SubstanceInterface s = tm.getNewSubstance();
			e.add(s);
			s.setName("temp.air.mean");
			
			ConditionInterface ci = tm.getNewCondition(s);
			s.add(ci);
			ci.setSpecies("average");
			
			for (Date date : day2avg.keySet()) {
				gc.setTime(date);
				SampleInterface si = tm.getNewSample(ci);
				ci.add(si);
				si.setSampleFineTimeOrRowId(date.getTime());
				si.setTime((int) SystemAnalysis.getUnixDay(date.getTime(), gc));
				si.setTimeUnit("unix day");
				
				NumericMeasurementInterface nmi = new NumericMeasurement3D(si);
				nmi.setValue(day2avg.get(date));
				nmi.setUnit("°C");
				si.add(nmi);
			}
		}
		if (addMinMax)
		{
			SubstanceInterface s = tm.getNewSubstance();
			e.add(s);
			s.setName("temp.air.min");
			
			ConditionInterface ci = tm.getNewCondition(s);
			s.add(ci);
			ci.setSpecies("minimum");
			
			for (Date date : day2avg.keySet()) {
				gc.setTime(date);
				SampleInterface si = tm.getNewSample(ci);
				ci.add(si);
				si.setSampleFineTimeOrRowId(date.getTime());
				si.setTime((int) SystemAnalysis.getUnixDay(date.getTime(), gc));
				si.setTimeUnit("unix day");
				
				NumericMeasurementInterface nmi = new NumericMeasurement3D(si);
				nmi.setValue(day2min.get(date));
				nmi.setUnit("°C");
				si.add(nmi);
			}
		}
		if (addMinMax)
		{
			SubstanceInterface s = tm.getNewSubstance();
			e.add(s);
			s.setName("temp.air.max");
			
			ConditionInterface ci = tm.getNewCondition(s);
			s.add(ci);
			ci.setSpecies("maximum");
			
			for (Date date : day2avg.keySet()) {
				gc.setTime(date);
				SampleInterface si = tm.getNewSample(ci);
				ci.add(si);
				si.setSampleFineTimeOrRowId(date.getTime());
				si.setTime((int) SystemAnalysis.getUnixDay(date.getTime(), gc));
				si.setTimeUnit("unix day");
				
				NumericMeasurementInterface nmi = new NumericMeasurement3D(si);
				nmi.setValue(day2max.get(date));
				nmi.setUnit("°C");
				si.add(nmi);
			}
		}
		return ehi;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		if (experimentResult != null)
			res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		if (experimentResult == null)
			return null;
		
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		ExperimentReference ref = new ExperimentReference(experimentResult);
		
		for (ActionDataProcessing adp : IAPpluginManager.getInstance().getExperimentProcessingActions(ref, true))
			res.add(new NavigationButton(adp, src != null ? src.getGUIsetting() : null));
		
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (experimentResult != null)
			return mpc;
		else
			return new MainPanelComponent(res.toString());
	}
	
	@Override
	public String getDefaultTitle() {
		return "Add temp. data";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getClimaImport();
	}
	
}