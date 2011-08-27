package de.ipk.ag_ba.image.operations.blocks.cmds.debug;

import java.util.HashMap;

import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

/**
 * @author entzian, klukas
 */

public class BlockLoadImagesIfNeeded extends AbstractSnapshotAnalysisBlockFIS {
	
	public enum BlockInfoProperty {
		CONSOLE_OUTPUT, HEIGHT, WIDTH, CONSOLE_OUTPUT_VIS, CONSOLE_OUTPUT_FLUO, CONSOLE_OUTPUT_NIR
	}
	
	HashMap<BlockInfoProperty, Boolean> infoMap = new HashMap<BlockInfoProperty, Boolean>() {
		private static final long serialVersionUID = 1L;
		
		{
			put(BlockInfoProperty.CONSOLE_OUTPUT, false);
			put(BlockInfoProperty.CONSOLE_OUTPUT_VIS, false);
			put(BlockInfoProperty.CONSOLE_OUTPUT_FLUO, true);
			put(BlockInfoProperty.CONSOLE_OUTPUT_NIR, false);
			put(BlockInfoProperty.HEIGHT, true);
			put(BlockInfoProperty.WIDTH, true);
		}
	};
	
	@Override
	protected boolean isChangingImages() {
		return false;
	}
	
	@Override
	protected void prepare() {
		if (getInput() != null) {
			synchronized (options) {
				if (getInput().getImages() != null)
					getInput().setImages(new FlexibleImageSet(getInput().getImages()));
				if (getInput().getMasks() != null)
					getInput().setMasks(new FlexibleImageSet(getInput().getMasks()));
				
				if (getInput().getImages().getVis() == null && getInput().getImages().getVisInfo() != null) {
					IOurl url = getInput().getImages().getVisInfo().getURL();
					try {
						FlexibleImage fi = new FlexibleImage(url);
						if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
							getInput().getImages().setVis(fi);
					} catch (Exception e) {
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: VIS-MAIN: " + e.getMessage());
					}
					if (getInput().getMasks() == null) {
						url = getInput().getImages().getVisInfo().getLabelURL();
						try {
							FlexibleImage fi = new FlexibleImage(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								getInput().getMasks().setVis(fi);
						} catch (Exception e) {
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: VIS-REFERENCE: " + e.getMessage());
						}
					}
				}
				
				if (getInput().getImages().getFluo() == null && getInput().getImages().getFluoInfo() != null) {
					IOurl url = getInput().getImages().getFluoInfo().getURL();
					try {
						FlexibleImage fi = new FlexibleImage(url);
						if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
							getInput().getImages().setFluo(fi);
					} catch (Exception e) {
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: FLUO-MAIN: " + e.getMessage());
					}
					if (getInput().getMasks() == null) {
						url = getInput().getImages().getFluoInfo().getLabelURL();
						try {
							FlexibleImage fi = new FlexibleImage(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								getInput().getMasks().setFluo(fi);
						} catch (Exception e) {
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: FLUO-REFERENCE: " + e.getMessage());
						}
					}
				}
				
				if (getInput().getImages().getNir() == null && getInput().getImages().getNirInfo() != null) {
					IOurl url = getInput().getImages().getNirInfo().getURL();
					try {
						FlexibleImage fi = new FlexibleImage(url);
						if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
							getInput().getImages().setNir(fi);
					} catch (Exception e) {
						System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: NIR-MAIN: " + e.getMessage());
					}
					if (getInput().getMasks() == null) {
						url = getInput().getImages().getNirInfo().getLabelURL();
						try {
							FlexibleImage fi = new FlexibleImage(url);
							if (fi != null && fi.getWidth() > 1 && fi.getHeight() > 1)
								getInput().getMasks().setNir(fi);
						} catch (Exception e) {
							System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: NIR-REFERENCE: " + e.getMessage());
						}
					}
				}
			}
		}
		
		printInfo(getInput().getMasks().getVis(), BlockPrintInfosTyp.VisMask);
		printInfo(getInput().getImages().getVis(), BlockPrintInfosTyp.VisImage);
		
		printInfo(getInput().getMasks().getFluo(), BlockPrintInfosTyp.FluoMask);
		printInfo(getInput().getImages().getFluo(), BlockPrintInfosTyp.FluoImage);
		
		printInfo(getInput().getMasks().getNir(), BlockPrintInfosTyp.NirMask);
		printInfo(getInput().getImages().getNir(), BlockPrintInfosTyp.NirImage);
	}
	
	private void printInfo(FlexibleImage workImage, BlockPrintInfosTyp type) {
		if (workImage == null) {
			if (infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT)) {
				System.out.println("Input Image is NULL");
			}
			return;
		}
		switch (type) {
			case FluoImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_FLUO_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_FLUO_IMAGE, workImage.getWidth());
				break;
			case FluoMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_FLUO_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_FLUO_MASK, workImage.getWidth());
				break;
			case NirImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_NIR_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_NIR_IMAGE, workImage.getWidth());
				break;
			case NirMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_NIR_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_NIR_MASK, workImage.getWidth());
				break;
			case VisImage:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_VIS_IMAGE, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_VIS_IMAGE, workImage.getWidth());
				break;
			case VisMask:
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.HEIGHT_VIS_MASK, workImage.getHeight());
				getProperties().setNumericProperty(getBlockPosition(), PropertyNames.WIDTH_VIS_MASK, workImage.getWidth());
				break;
			
		}
		
		if (infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT)) {
			
			if (type.name().contains("Vis") && infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT_VIS) ||
					type.name().contains("Fluo") && infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT_FLUO) ||
					type.name().contains("Nir") && infoMap.get(BlockInfoProperty.CONSOLE_OUTPUT_NIR)) {
				
				System.out.println(type + " - Infos: ##### Start #####");
				
				if (infoMap.get(BlockInfoProperty.HEIGHT))
					System.out.println("height: " + workImage.getHeight());
				if (infoMap.get(BlockInfoProperty.WIDTH))
					System.out.println("width: " + workImage.getWidth());
				
				System.out.println(type + " - Infos: **** End ****");
			}
		}
	}
}
