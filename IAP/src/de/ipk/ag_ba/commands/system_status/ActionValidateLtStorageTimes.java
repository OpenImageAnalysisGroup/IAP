package de.ipk.ag_ba.commands.system_status;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.mongodb.ActionMongoOrLTexperimentNavigation;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.postgresql.LTdataExchange;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class ActionValidateLtStorageTimes extends AbstractNavigationAction {
	
	public static final long MILLISECS_PER_MINUTE = 60 * 1000;
	public static final long MILLISECS_PER_HOUR = 60 * MILLISECS_PER_MINUTE;
	protected static final long MILLISECS_PER_DAY = 24 * MILLISECS_PER_HOUR;
	
	private final ArrayList<String> res = new ArrayList<String>();
	private final ArrayList<ExperimentHeaderInterface> errorExp = new ArrayList<ExperimentHeaderInterface>();
	private NavigationButton src;
	
	public ActionValidateLtStorageTimes(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public String getDefaultTitle() {
		return "Validate LT storage times";
	}
	
	@Override
	public String getDefaultTooltip() {
		return "Load every LT experiment and compare snapshot time with storage path time.";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/DBE2_logo_s.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		res.clear();
		HashSet<String> printed = new HashSet<String>();
		Calendar cal = new GregorianCalendar();
		Calendar filePathCal = new GregorianCalendar();
		filePathCal.set(Calendar.HOUR, 11);
		filePathCal.set(Calendar.MINUTE, 0);
		filePathCal.set(Calendar.SECOND, 0);
		
		int n = 0;
		for (String database : new LTdataExchange().getDatabases()) {
			ArrayList<ExperimentHeaderInterface> ell = new ArrayList<ExperimentHeaderInterface>();
			try {
				ell = new LTdataExchange().getExperimentsInDatabase(null, database, getStatusProvider());
			} catch (Exception e) {
				String s = "Could not analyze DB " + database + ": " + e.getMessage();
				System.out.println(s);
				res.add(s);
			}
			for (ExperimentHeaderInterface ehi : ell) {
				n++;
				boolean foundError = false;
				ExperimentInterface exp = new LTdataExchange().getExperiment(ehi, false, getStatusProvider());
				for (NumericMeasurementInterface nmi : Substance3D.getAllFiles(exp, MeasurementNodeType.IMAGE)) {
					ImageData id = (ImageData) nmi;
					Date snapshotTime = new Date(id.getParentSample().getSampleFineTimeOrRowId());
					cal.setTime(snapshotTime);
					String expectedStorageDate =
							cal.get(Calendar.YEAR) + "-" +
									StringManipulationTools.formatNumber(cal.get(Calendar.MONTH) + 1, "00") + "-" +
									StringManipulationTools.formatNumber(cal.get(Calendar.DAY_OF_MONTH), "00");
					String fileName = id.getURL().getDetail();
					String fileStorageDate = fileName.split("/")[2];
					try {
						filePathCal.set(Calendar.YEAR, Integer.parseInt(fileStorageDate.split("-")[0]));
						filePathCal.set(Calendar.MONTH, Integer.parseInt(fileStorageDate.split("-")[1]) - 1);
						filePathCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(fileStorageDate.split("-")[2]));
						
						if (filePathCal.getTimeInMillis() < cal.getTimeInMillis() - MILLISECS_PER_DAY) {
							String s = "Filepath/expected: " + fileStorageDate + " - " + expectedStorageDate + " (experiment: " + ehi.getExperimentName() +
									", id: " + ehi.getDatabaseId() + ")";
							if (!printed.contains(s)) {
								System.out.println(s);
								printed.add(s);
								res.add(s);
							}
							foundError = true;
						}
					} catch (Exception e) {
						if (!fileStorageDate.startsWith("blobs")) {
							String s = "Invalid Filepath/expected: " + fileStorageDate + " - " + expectedStorageDate + " (experiment: " + ehi.getExperimentName() +
									", id: " + ehi.getDatabaseId() + ") // file name: " + fileName;
							System.out.println(s);
							printed.add(s);
							res.add(s);
							foundError = true;
						}
					}
				}
				if (foundError)
					errorExp.add(ehi);
			}
		}
		res.add(0, "<b>Checked " + n + " experiments.</b> Problematic file names:");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> ns = new ArrayList<NavigationButton>(currentSet);
		ns.add(src);
		return ns;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> ra = new ArrayList<NavigationButton>();
		for (ExperimentHeaderInterface ee : errorExp) {
			ra.add(new NavigationButton(new ActionMongoOrLTexperimentNavigation(new ExperimentReference(ee)), src.getGUIsetting()));
		}
		return ra;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(StringManipulationTools.getStringList(res, "<br>"));
	}
}
