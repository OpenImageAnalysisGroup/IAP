package de.ipk.ag_ba.commands;

import java.util.HashMap;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

/**
 * @author Christian Klukas
 */
public class BlockMonitoringResult {
	
	private final long processingTime;
	private final String blockName;
	
	private final HashMap<CameraType, Image> resultImages = new HashMap<>();
	private final HashMap<CameraType, Image> resultMasks = new HashMap<>();
	
	public BlockMonitoringResult(MaskAndImageSet workset, int pipelineMonitoringResultImageSize, String blockName, long processingTime) {
		this.blockName = blockName;
		this.processingTime = processingTime;
		if (workset != null && workset.images() != null)
			for (CameraType ct : CameraType.values()) {
				Image inp = workset.images().getImage(ct);
				if (inp != null)
					resultImages.put(ct, inp.copy().resize(pipelineMonitoringResultImageSize, pipelineMonitoringResultImageSize, true));
			}
		if (workset != null && workset.masks() != null)
			for (CameraType ct : CameraType.values()) {
				Image inp = workset.masks().getImage(ct);
				if (inp != null)
					resultMasks.put(ct, inp.copy().resize(pipelineMonitoringResultImageSize, pipelineMonitoringResultImageSize, true));
			}
	}
	
	public String getBlockName() {
		return blockName;
	}
	
	public String getImageHTML(CameraType ct) {
		try {
			Image i = resultImages.get(ct);
			if (i != null)
				return i.getHTMLimageTag();
			else
				return "";
		} catch (Exception e) {
			return "[conversion error: " + e.getMessage() + "]";
		}
	}
	
	public String getMaskHTML(CameraType ct) {
		try {
			Image i = resultMasks.get(ct);
			if (i != null)
				return i.getHTMLimageTag();
			else
				return "";
		} catch (Exception e) {
			return "[conversion error: " + e.getMessage() + "]";
		}
	}
	
	public String getProcessTime() {
		return SystemAnalysis.getWaitTime(processingTime);
	}
	
}
