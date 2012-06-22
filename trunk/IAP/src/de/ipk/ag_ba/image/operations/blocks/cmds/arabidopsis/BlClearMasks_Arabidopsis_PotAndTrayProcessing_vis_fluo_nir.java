package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.awt.geom.Rectangle2D;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Clears all images around a circle in the middle
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_Arabidopsis_PotAndTrayProcessing_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	boolean multiTray = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		if (options.getTrayCnt() > 1) {
			multiTray = true;
		}
		double vertFillGrade = 0.95d;
		if (options.getTrayCnt() == 6) {
			// 3x2
			FlexibleImage vis = input().images().vis();
			if (vis != null)
				processCuttingOfImage(vis, FlexibleImageType.VIS, -30, vertFillGrade, 3, 2);
			
			FlexibleImage fluo = input().images().fluo();
			if (fluo != null)
				processCuttingOfImage(fluo, FlexibleImageType.FLUO, -30, vertFillGrade, 3, 2);
			
			FlexibleImage nir = input().images().nir();
			if (nir != null)
				processCuttingOfImage(nir, FlexibleImageType.NIR, 0, vertFillGrade, 3, 2);
			
			FlexibleImage ir = input().images().ir();
			if (ir != null) {
				ir = ir.io().rotate(180).getImage();
				processCuttingOfImage(ir, FlexibleImageType.IR, 0, vertFillGrade, 3, 2);
			}
		}
		if (options.getTrayCnt() == 12) {
			// 4x3
			FlexibleImage vis = input().images().vis();
			if (vis != null)
				processCuttingOfImage(vis, FlexibleImageType.VIS, -30, vertFillGrade, 4, 3);
			
			FlexibleImage fluo = input().images().fluo();
			if (fluo != null)
				processCuttingOfImage(fluo, FlexibleImageType.FLUO, -30, vertFillGrade, 4, 3);
			
			FlexibleImage nir = input().images().nir();
			if (nir != null)
				processCuttingOfImage(nir, FlexibleImageType.NIR, 0, vertFillGrade, 4, 3);
			
			FlexibleImage ir = input().images().ir();
			if (ir != null) {
				ir = ir.io().rotate(180).getImage();
				processCuttingOfImage(ir, FlexibleImageType.IR, 0, vertFillGrade, 4, 3);
			}
		}
	}
	
	private void processCuttingOfImage(FlexibleImage img, FlexibleImageType type, int offY, double vertFillGrade, int cols, int rows) {
		Rectangle2D.Double r = getGridPos(options.getTrayIdx(), cols, rows, img.getWidth(), (int) (img.getHeight() * vertFillGrade), img.getWidth() / 2,
				img.getHeight() / 2);
		r.y = r.y + offY;
		
		// double b = 30;
		// r.x += b;
		// r.y += b;
		// r.width -= 2 * b;
		// r.height -= 2 * b;
		
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
								img.getWidth() / 2,
								img.getHeight() / 2 - 30,
								(int) (img.getHeight() / 2.45d)).getImage();
			else
				return img;
			/*
			 * .copy().io().
			 * clearOutsideRectangle(0, 0, img.getWidth() - 1, (int) (img.getHeight() * 0.65)).getImage();
			 */
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
								img.getWidth() / 2,
								img.getHeight() / 2,
								(int) (img.getHeight() / 2.45d)).getImage();
			else
				return img;
			/*
			 * .copy().io().
			 * clearOutsideRectangle(0, 0, img.getWidth() - 1, (int) (img.getHeight() * 0.65)).getImage();
			 */
		} else
			return img;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage img = input().images().nir();
		if (img != null && !multiTray) {
			if (options.getCameraPosition() == CameraPosition.TOP)
				return img.copy().io().translate(-3, 0).
						clearOutsideCircle(
								img.getWidth() / 2,
								img.getHeight() / 2,
								(int) (img.getHeight() / 2.45d)).getImage();
			else
				return img;
			/*
			 * .copy().io().
			 * clearOutsideRectangle(0, 0, img.getWidth() - 1, (int) (img.getHeight() * 0.65)).getImage();
			 */
		} else
			return img;
	}
	
	@Override
	protected FlexibleImage processIRimage() {
		FlexibleImage img = input().images().ir();
		if (img != null && !multiTray) {
			if (options.getCameraPosition() == CameraPosition.TOP)
				return img.copy().io().rotate(180).translate(-3, 0).
						clearOutsideCircle(
								img.getWidth() / 2,
								img.getHeight() / 2,
								(int) (img.getHeight() / 2.45d)).getImage();
			else
				return img;
			/*
			 * .copy().io().
			 * clearOutsideRectangle(0, 0, img.getWidth() - 1, (int) (img.getHeight() * 0.65)).getImage();
			 */
		} else
			return img;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().images().vis();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage img = input().images().fluo();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage img = input().images().nir();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processIRmask() {
		FlexibleImage img = input().images().ir();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
}
