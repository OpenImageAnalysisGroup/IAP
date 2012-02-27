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
import de.ipk.ag_ba.commands.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
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
			res.append("<li>Selected input file: " + excelFile.getAbsolutePath());
			TableData myData = ExperimentDataFileReader.getExcelTableData(excelFile);
			// A1: Uhrzeit, B1: Datum
			myData.showDataDialog();
			String check1time = myData.getCellData(1, 1, "") + "";
			String check2date = myData.getCellData(1, 1, "") + "";
			if (!check1time.equals("Uhrzeit"))
				res.append("<li>Cell A1 needs to contain the string 'Uhrzeit'");
			else
				if (!check2date.equals("Uhrzeit"))
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
						DateFormat dateFormat = DateFormat.getDateInstance(
								DateFormat.DEFAULT, Locale.GERMAN);
						GregorianCalendar gc = new GregorianCalendar();
						ArrayList<Double> hours = new ArrayList<Double>();
						ArrayList<Date> days = new ArrayList<Date>();
						ArrayList<Double> temps = new ArrayList<Double>();
						for (int row = 4; row <= myData.getMaximumRow(); row++) {
							Double time = (Double) myData.getCellData(1, row, null);
							String dateString = (String) myData.getCellData(2, row, "");
							Date date = dateFormat.parse(dateString);
							Double temp = (Double) myData.getCellData(tempCol, row, null);
							if (time != null && date != null && temp != null) {
								gc.setTime(date);
								double hourProgress = time * 24d;
								hours.add(hourProgress);
								days.add(date);
								temps.add(temp);
							}
						}
						myData = null;
						int nmax = hours.size();
						if (nmax > 0) {
							TreeMap<Date, Double> day2avg = new TreeMap<Date, Double>();
							TreeMap<Date, Double> day2min = new TreeMap<Date, Double>();
							TreeMap<Date, Double> day2max = new TreeMap<Date, Double>();
							for (int i = 0; i < nmax - 1; i++) {
								double hourDiff = hours.get(i + 1) - hours.get(i);
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
							ExperimentHeaderInterface ehi = processData(excelFile, gc, day2avg, day2min, day2max, tm, e);
							ehi.setDatabase("");
							ehi.setDatabaseId("");
							ehi.setOriginDbId(excelFile.getAbsolutePath());
							ehi.setImportusername(SystemAnalysis.getUserName());
							ehi.setNumberOfFiles(0);
							ehi.setExperimentname(excelFile.getName());
							for (SubstanceInterface si : e)
								for (ConditionInterface ci : si)
									ci.setExperimentHeader(ehi);
							MyExperimentInfoPanel info = new MyExperimentInfoPanel();
							info.setExperimentInfo(null, ehi, true, e);
							mpc = new MainPanelComponent(info, true);
							
						}
					}
				}
		} else
			res.append("<li>Selected input file: no file selected!");
	}
	
	private ExperimentHeaderInterface processData(File excelFile, GregorianCalendar gc, TreeMap<Date, Double> day2avg, TreeMap<Date, Double> day2min,
			TreeMap<Date, Double> day2max,
			DataMappingTypeManagerInterface tm, Experiment e) {
		ExperimentHeaderInterface ehi = new ExperimentHeader();
		ehi.setExperimentname(excelFile.getName());
		ehi.setDatabase("");
		ehi.setCoordinator(SystemAnalysis.getUserName());
		ehi.setStartdate(day2avg.firstKey());
		ehi.setImportdate(day2avg.lastKey());
		ehi.setStorageTime(new Date());
		ehi.setRemark("Import at " + SystemAnalysis.getCurrentTime() + " by " + SystemAnalysis.getUserName());
		e.setHeader(ehi);
		
		SubstanceInterface s = tm.getNewSubstance();
		e.add(s);
		s.setName("temp.air");
		
		{
			ConditionInterface ci = tm.getNewCondition(s);
			s.add(ci);
			ci.setSpecies("avg");
			
			for (Date date : day2avg.keySet()) {
				gc.setTime(date);
				SampleInterface si = tm.getNewSample(ci);
				ci.add(si);
				si.setTime((int) SystemAnalysis.getUnixDay(date.getTime(), gc));
				si.setTimeUnit("unix day");
				
				NumericMeasurementInterface nmi = new NumericMeasurement3D(si);
				nmi.setValue(day2avg.get(date));
				nmi.setUnit("°C");
				si.add(nmi);
			}
		}
		{
			ConditionInterface ci = tm.getNewCondition(s);
			s.add(ci);
			ci.setSpecies("min");
			
			for (Date date : day2avg.keySet()) {
				gc.setTime(date);
				SampleInterface si = tm.getNewSample(ci);
				ci.add(si);
				si.setTime((int) SystemAnalysis.getUnixDay(date.getTime(), gc));
				si.setTimeUnit("unix day");
				
				NumericMeasurementInterface nmi = new NumericMeasurement3D(si);
				nmi.setValue(day2min.get(date));
				nmi.setUnit("°C");
				si.add(nmi);
			}
		}
		{
			ConditionInterface ci = tm.getNewCondition(s);
			s.add(ci);
			ci.setSpecies("max");
			
			for (Date date : day2avg.keySet()) {
				gc.setTime(date);
				SampleInterface si = tm.getNewSample(ci);
				ci.add(si);
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
		
		MongoDB m = null;
		
		ActionMongoOrLemnaTecExperimentNavigation.getDefaultActions(res, experimentResult, experimentResult.getHeader(),
				false, src != null ? src.getGUIsetting() : null, m);
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