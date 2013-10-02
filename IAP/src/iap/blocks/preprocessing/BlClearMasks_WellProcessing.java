package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Processes individual well parts from top images (removes other parts).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_WellProcessing extends AbstractSnapshotAnalysisBlock implements WellProcessor {
	
	boolean multiTray = false;
	
	@Override
	protected void prepare() {
		
		super.prepare();
		
		int gridHn;
		int gridVn;
		multiTray = false;
		if (options.getWellCnt() == 1) {
			gridHn = getInt("Well Grid Horizontal", 1);
			gridVn = getInt("Well Grid Vertical", 1);
		} else
			if (options.getWellCnt() == 6) {
				gridHn = getInt("Well Grid Horizontal", 3);
				gridVn = getInt("Well Grid Vertical", 2);
				multiTray = true;
			} else
				if (options.getWellCnt() == 12) {
					gridHn = getInt("Well Grid Horizontal", 4);
					gridVn = getInt("Well Grid Vertical", 3);
					multiTray = true;
				} else {
					gridHn = getInt("Well Grid Horizontal", 1);
					gridVn = getInt("Well Grid Vertical", 1);
				}
		
		if (gridHn != 1 || gridVn != 1) {
			double vertFillGrade = getDouble("Vertical Grid Extend Percent", 95) / 100;
			int well_border = getInt("Additional Well Border", 0);
			// 3x2
			Image vis = input().images().vis();
			if (vis != null)
				processCuttingOfImage(vis, CameraType.VIS, vertFillGrade * vis.getHeight() / 2d, vertFillGrade, gridHn, gridVn, well_border);
			// processCuttingOfImage(vis, FlexibleImageType.VIS, 10, vertFillGrade, 4, 3);
			Image fluo = input().images().fluo();
			if (fluo != null)
				processCuttingOfImage(fluo, CameraType.FLUO, vertFillGrade * fluo.getHeight() / 2d, vertFillGrade, gridHn, gridVn, well_border);
			
			Image nir = input().images().nir();
			if (nir != null)
				processCuttingOfImage(nir, CameraType.NIR, vertFillGrade * nir.getHeight() / 2d, vertFillGrade, gridHn, gridVn, well_border);
			
			Image ir = input().images().ir();
			if (ir != null) {
				ir = ir.io().getImage();
				processCuttingOfImage(ir, CameraType.IR, vertFillGrade * ir.getHeight() / 2d, vertFillGrade, gridHn, gridVn, well_border);
			}
		}
	}
	
	private void processCuttingOfImage(Image img, CameraType type, double offY, double vertFillGrade, int cols, int rows, int well_border) {
		Rectangle2D.Double r = getGridPos(options.getTrayIdx(), cols, rows, img.getWidth(), (int) (img.getHeight() * vertFillGrade),
				img.getWidth() / 2,
				img.getHeight() / 2);
		
		int le = (int) r.getMinX() + well_border;
		int to = (int) r.getMinY() + well_border;
		int ri = (int) r.getMaxX() - well_border;
		int bo = (int) r.getMaxY() - well_border;
		Image res = img.io().clearOutsideRectangle(le, to, ri, bo).getImage();
		res.setCameraType(type);
		input().images().set(res);
	}
	
	private Rectangle2D.Double getGridPos(int trayIdx, int columns, int rows, int w, int h, int centerX, int centerY) {
		int co = trayIdx % columns;
		int ro = trayIdx / columns;
		
		int trayWidth = w / columns;
		int trayHeight = h / rows;
		
		int x = centerX * (centerX * 2 - w) / (centerX * 2);
		int y = centerY * (centerY * 2 - h) / (centerY * 2);
		
		for (int c = 0; c < co; c++)
			x += trayWidth;
		for (int r = 0; r < ro; r++)
			y += trayHeight;
		
		Rectangle2D.Double res = new Rectangle2D.Double(x, y, trayWidth, trayHeight);
		
		return res;
	}
	
	@Override
	protected Image processVISimage() {
		Image img = input().images().vis();
		if (img != null && !multiTray) {
			if (options.getCameraPosition() == CameraPosition.TOP)
				return img.copy().io().
						clearOutsideCircle(
								img.getWidth() / 2 - getInt("VIS Circle Center Shift X", 0),
								img.getHeight() / 2 - getInt("VIS Circle Center Shift Y", 0),
								(int) (img.getHeight() * getDouble("VIS Circle Radius Percent of Height", 40.8) / 100d)).getImage();
			else
				return img;
		} else
			return img;
	}
	
	@Override
	protected Image processFLUOimage() {
		Image img = input().images().fluo();
		if (img != null && !multiTray) {
			if (options.getCameraPosition() == CameraPosition.TOP)
				return img.copy().io().
						clearOutsideCircle(
								img.getWidth() / 2 - getInt("FLUO Circle Center Shift X", 0),
								img.getHeight() / 2 - getInt("FLUO Circle Center Shift Y", 0),
								(int) (img.getHeight() * getDouble("FLUO Circle Radius Percent of Height", 40.8) / 100d)).getImage();
			else
				return img;
		} else
			return img;
	}
	
	@Override
	protected Image processNIRimage() {
		Image img = input().images().nir();
		if (img != null && !multiTray) {
			if (options.getCameraPosition() == CameraPosition.TOP)
				return img.copy().io().
						clearOutsideCircle(
								img.getWidth() / 2 - getInt("NIR Circle Center Shift X", 0),
								img.getHeight() / 2 - getInt("NIR Circle Center Shift Y", 0),
								(int) (img.getHeight() * getDouble("NIR Circle Radius Percent of Height", 40.8) / 100d)).getImage();
			else
				return img;
		} else
			return img;
	}
	
	@Override
	protected Image processIRimage() {
		Image img = input().images().ir();
		if (img != null && !multiTray) {
			if (options.getCameraPosition() == CameraPosition.TOP)
				return img.copy().io()
						.clearOutsideCircle(
								img.getWidth() / 2 - getInt("IR Circle Center Shift X", 0),
								img.getHeight() / 2 - getInt("IR Circle Center Shift Y", 0),
								(int) (img.getHeight() * getDouble("IR Circle Radius Percent of Height", 40.8) / 100d)).getImage();
			else
				return img;
		} else
			return img;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Cut Well Parts";
	}
	
	@Override
	public String getDescription() {
		return "Processes individual well parts from top images (removes other parts).";
	}
	
	@Override
	public int getDefinedWellCount(ImageProcessorOptions options) {
		int hg = options.getIntSetting(this, "Well Grid Horizontal", 1);
		int wg = options.getIntSetting(this, "Well Grid Vertical", 1);
		int n = hg * wg;
		
		return n;
	}
}
