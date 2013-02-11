package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Clears all images around a circle in the middle
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_Arabidopsis_PotAndTrayProcessing extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean multiTray = false;
	
	@Override
	protected void prepare() {
		
		super.prepare();
		
		int gridHn;
		int gridVn;
		multiTray = false;
		if (options.getTrayCnt() == 1) {
			gridHn = getInt("Well Grid Horizontal", 1);
			gridVn = getInt("Well Grid Vertical", 1);
		} else
			if (options.getTrayCnt() == 6) {
				gridHn = getInt("Well Grid Horizontal", 3);
				gridVn = getInt("Well Grid Vertical", 2);
				multiTray = true;
			} else
				if (options.getTrayCnt() == 12) {
					gridHn = getInt("Well Grid Horizontal", 4);
					gridVn = getInt("Well Grid Vertical", 3);
					multiTray = true;
				} else {
					gridHn = getInt("Well Grid Horizontal", 1);
					gridVn = getInt("Well Grid Vertical", 1);
				}
		
		if (gridHn != 1 || gridVn != 1) {
			double vertFillGrade = getDouble("Vertical Grid Extend Percent", 95) / 100;
			// 3x2
			FlexibleImage vis = input().images().vis();
			if (vis != null)
				processCuttingOfImage(vis, FlexibleImageType.VIS, vertFillGrade * vis.getHeight() / 2d, vertFillGrade, gridHn, gridVn);
			// processCuttingOfImage(vis, FlexibleImageType.VIS, 10, vertFillGrade, 4, 3);
			FlexibleImage fluo = input().images().fluo();
			if (fluo != null)
				processCuttingOfImage(fluo, FlexibleImageType.FLUO, vertFillGrade * fluo.getHeight() / 2d, vertFillGrade, gridHn, gridVn);
			
			FlexibleImage nir = input().images().nir();
			if (nir != null)
				processCuttingOfImage(nir, FlexibleImageType.NIR, vertFillGrade * nir.getHeight() / 2d, vertFillGrade, gridHn, gridVn);
			
			FlexibleImage ir = input().images().ir();
			if (ir != null) {
				ir = ir.io().rotate(180).getImage();
				processCuttingOfImage(ir, FlexibleImageType.IR, vertFillGrade * ir.getHeight() / 2d, vertFillGrade, gridHn, gridVn);
			}
		}
	}
	
	private void processCuttingOfImage(FlexibleImage img, FlexibleImageType type, double offY, double vertFillGrade, int cols, int rows) {
		Rectangle2D.Double r = getGridPos(options.getTrayIdx(), cols, rows, img.getWidth(), (int) (img.getHeight() * vertFillGrade),
				img.getWidth() / 2,
				img.getHeight() / 2);
		// r.y = r.y + offY;
		
		int le = (int) r.getMinX();
		int to = (int) r.getMinY();
		int ri = (int) r.getMaxX();
		int bo = (int) r.getMaxY();
		FlexibleImage res = img.io().clearOutsideRectangle(le, to, ri, bo).getImage();
		res.setType(type);
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
	protected FlexibleImage processVISimage() {
		FlexibleImage img = input().images().vis();
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
	protected FlexibleImage processFLUOimage() {
		FlexibleImage img = input().images().fluo();
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
	protected FlexibleImage processNIRimage() {
		FlexibleImage img = input().images().nir();
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
	protected FlexibleImage processIRimage() {
		FlexibleImage img = input().images().ir();
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
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
