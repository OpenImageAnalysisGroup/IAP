package de.ipk.ag_ba.commands.control;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.ErrorMsg;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ActionViewData;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemHandler;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import iap.blocks.postprocessing.BlSaveResultImages;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

public class AddCapturedImagesToExperiment extends AbstractNavigationAction {
	
	private ArrayList<ThreadSafeOptions> conditionEditorList;
	private CaptureLiveView cl;
	private ExperimentReferenceInterface experimentReference;
	private DatabaseTarget databaseTarget;
	private SelectTimeScaleAction timeAction;
	private long captured = 0;
	private SettingStringEditorAction barcodeAction;
	private ImageInfo info;
	
	public AddCapturedImagesToExperiment(String tooltip) {
		super(tooltip);
	}
	
	public AddCapturedImagesToExperiment(ArrayList<ThreadSafeOptions> conditionEditorList, CaptureLiveView cl, ExperimentReferenceInterface experimentReference, SelectTimeScaleAction timeAction, SettingStringEditorAction barcodeAction) {
		this("Add captured image to experiment");
		this.conditionEditorList = conditionEditorList;
		this.cl = cl;
		this.experimentReference = experimentReference;
		this.timeAction = timeAction;
		this.barcodeAction = barcodeAction;
		
		try {
			this.databaseTarget = AbstractPhenotypingTask.determineDatabaseTarget(experimentReference.getHeader(), experimentReference.getM());
		} catch (Exception e) {
			this.databaseTarget = null;
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		if (!mayRun()) {
			if (info != null && info.isAnyCameraConfiguredForCaptureAndNotLiveView()) {
				info = cl.requestImageCaptureAndWaitUntilFinished();
				skipInfoRead = true;
				if (!mayRun())
					return;
			} else {
				return;
			}
		}
		
		ImageInfo imginfo = info;
		
		Integer[] imageKeys = imginfo.getImages().keySet().toArray(new Integer[] {});
		String tray = null;
		Long imageTimePoint = System.currentTimeMillis();
		for (Integer cameraId : imageKeys) {
			BufferedImage image = imginfo.getImages().get(cameraId);
			String substance = imginfo.getImageSubstances().get(cameraId);
			Double rotation = imginfo.getImageRotation().get(cameraId);
			if (image != null) {
				ConditionInterface condition = findActiveCondition(imginfo.getImageSubstances().get(cameraId));
				Sample3D sample = new Sample3D(condition);
				sample.setSampleFineTimeOrRowId(imageTimePoint);
				sample.setTime(timeAction.getTimeValue());
				sample.setTimeUnit(timeAction.getUnit());
				condition.add(sample);
				ImageData id = new ImageData(sample);
				LoadedImage loadedImage = new LoadedImage(id, image);
				loadedImage.setQualityAnnotation(cl.getCurrentBarcode());
				if (rotation != null)
					loadedImage.setPosition(rotation);
				
				CameraType ct = CameraType.fromString(substance.split("\\.", 2)[0]);
				CameraPosition cp = CameraPosition.fromString(substance.split("\\.", 2)[1]);
				saveImageAndUpdateURL(ct, cp, loadedImage, databaseTarget, false, tray);
				sample.add(loadedImage.getImageDataReference());
				captured++;
			}
		}
		ExperimentReferenceInterface experiment = experimentReference;
		
		if (experiment.getM() != null) {
			new ActionCopyToMongo(experiment.getM(), experiment, true).performActionCalculateResults(src);
		} else {
			String dbId = experiment != null && experiment.getHeader() != null ? experiment.getHeader().getDatabaseId() : null;
			if (dbId != null) {
				String id = dbId.contains(":") ? dbId.substring(0, dbId.indexOf(":")) : null;
				if (id != null && !id.isEmpty()) {
					ResourceIOHandler vfs = ResourceIOManager.getHandlerFromPrefix(id);
					if (vfs instanceof VirtualFileSystemHandler) {
						VirtualFileSystemHandler vv = (VirtualFileSystemHandler) vfs;
						if (vv.getVFS() instanceof VirtualFileSystemVFS2) {
							VirtualFileSystemVFS2 vv2 = (VirtualFileSystemVFS2) vv.getVFS();
							if (vv2.isAbleToSaveData()) {
								experiment.getHeader().setImportDate(new Date());
								ActionViewData.getSaveAnnotationsInVfsCommand(experiment, vv2).performActionCalculateResults(src);
							}
						}
					}
				}
			}
		}
	}
	
	private ConditionInterface findActiveCondition(String substanceName) {
		ConditionInterface activeCondition = null;
		for (ThreadSafeOptions tso : conditionEditorList) {
			if (tso.getBval(0, false)) {
				activeCondition = (ConditionInterface) tso.getParam(11, null);
			}
		}
		
		if (activeCondition != null) {
			for (SubstanceInterface substance : experimentReference.getExperiment()) {
				if (substance.getName().equalsIgnoreCase(substanceName)) {
					for (ConditionInterface condition : substance) {
						if (condition.equals(activeCondition))
							return condition;
					}
					ConditionInterface newCon = activeCondition.clone(substance);
					substance.add(newCon);
					return newCon;
				}
			}
			SubstanceInterface newSubstance = new Substance3D();
			newSubstance.setName(substanceName);
			experimentReference.getExperiment().add(newSubstance);
			ConditionInterface newCon = activeCondition.clone(newSubstance);
			newSubstance.add(newCon);
			return newCon;
		}
		return null;
	}
	
	protected LoadedImage saveImageAndUpdateURL(CameraType ct, CameraPosition cp, LoadedImage result,
			DatabaseTarget databaseTarget, boolean processLabelUrl,
			String tray) throws Exception {
		
		if (result.getURL() == null) {
			UUID uuid = UUID.randomUUID();
			String randomUUIDString = uuid.toString();
			result.setURL(new IOurl(null, randomUUIDString + SystemOptions.getInstance().getString("IAP", "Result File Type", "png")));
		}
		
		result.getURL().setFileName(BlSaveResultImages.addTrayInfo(ct, cp, tray, result.getURL().getFileName(), result));
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (databaseTarget != null)
			return databaseTarget.saveImage(new String[] { "", "label_" }, result, false, true);
		else
			return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	private String oldBC = "";
	private boolean skipInfoRead;
	
	@Override
	public boolean mayRun() {
		if (!skipInfoRead)
			info = cl.getImageInfo();
		String bc = cl.getCurrentBarcode();
		if (oldBC != null && !oldBC.equals(bc)) {
			ml: for (SubstanceInterface sub : experimentReference.getExperiment()) {
				for (ConditionInterface ci : sub) {
					for (SampleInterface sai : ci) {
						for (NumericMeasurementInterface nmi : sai) {
							if (nmi.getQualityAnnotation() != null && nmi.getQualityAnnotation().equals(bc)) {
								for (ThreadSafeOptions tso : conditionEditorList) {
									ConditionInterface ciTso = (ConditionInterface) tso.getParam(1, null);
									tso.setBval(0, ciTso != null && ciTso.equals(ci));
								}
								break ml;
							}
						}
					}
				}
			}
		}
		oldBC = bc;
		return info != null && info.getImages() != null && !info.getImages().isEmpty() && bc != null && bc.length() > 0;
	}
	
	@Override
	public String getDefaultTitle() {
		String post = "";
		this.info = cl.getImageInfo();
		if (captured > 0)
			post = "<br>(" + captured + " captured)";
		if (info.getImages() == null || info.getImages().isEmpty()) {
			if (info.isAnyCameraConfiguredForCaptureAndNotLiveView())
				return "<html><center>Ready to capture" + post;
			else
				return "<html><center>Not ready" + post;
		} else
			if (!mayRun())
				return "<html><center>No sample ID" + post;
			else {
				if (info.getImages().size() == 1)
					return "<html><center>Capture image" + post;
				else
					return "<html><center>Capture " + info.getImages().size() + " images" + post;
			}
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	@Override
	public String getDefaultImage() {
		if (!mayRun() && !(info != null && info.isAnyCameraConfiguredForCaptureAndNotLiveView()))
			return "img/ext/gpl2/Gnome-Media-Flash-64_disabled.png";
		else
			return "img/ext/gpl2/Gnome-Media-Flash-64.png";
	}
}
