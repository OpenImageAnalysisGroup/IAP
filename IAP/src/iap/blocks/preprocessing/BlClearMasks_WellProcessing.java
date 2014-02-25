package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageCanvas;
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
		if (optionsAndResults.getWellCnt() == 1) {
			gridHn = getInt("Well Grid Horizontal", 1);
			gridVn = getInt("Well Grid Vertical", 1);
		} else
			if (optionsAndResults.getWellCnt() == 6) {
				gridHn = getInt("Well Grid Horizontal", 3);
				gridVn = getInt("Well Grid Vertical", 2);
				multiTray = true;
			} else
				if (optionsAndResults.getWellCnt() == 12) {
					gridHn = getInt("Well Grid Horizontal", 4);
					gridVn = getInt("Well Grid Vertical", 3);
					multiTray = true;
				} else {
					gridHn = getInt("Well Grid Horizontal", 1);
					gridVn = getInt("Well Grid Vertical", 1);
				}
		
		if (gridHn != 1 || gridVn != 1) {
			double horFillGrade = getDouble("Horizontal Grid Extend Percent", 100) / 100d;
			double vertFillGrade = getDouble("Vertical Grid Extend Percent", 95) / 100d;
			double well_border = getDouble("Additional Well Border Percent", 0) / 100d;
			if (well_border < 0) {
				well_border = -well_border;
				double daysUntilZero = (well_border * 100d - Math.floor(well_border * 100d)) * 100d;
				well_border = Math.floor(well_border * 100d);
				if (input().images().getAnyInfo().getParentSample().getTime() != 0 && daysUntilZero > 0.000001)
					well_border = well_border - well_border * input().images().getAnyInfo().getParentSample().getTime() / daysUntilZero;
				well_border = well_border / 100d;
				if (well_border < 0)
					well_border = 0d;
			}
			
			Image vis = input().images().vis();
			if (vis != null)
				input().images().set(processCuttingOfImage(vis, CameraType.VIS, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
			
			Image fluo = input().images().fluo();
			if (fluo != null)
				input().images().set(processCuttingOfImage(fluo, CameraType.FLUO, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
			
			Image nir = input().images().nir();
			if (nir != null)
				input().images().set(processCuttingOfImage(nir, CameraType.NIR, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
			
			Image ir = input().images().ir();
			if (ir != null)
				input().images().set(processCuttingOfImage(ir, CameraType.IR, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
			
			if (getBoolean("Cut Masks", false)) {
				vis = input().masks().vis();
				if (vis != null)
					input().masks().set(processCuttingOfImage(vis, CameraType.VIS, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
				
				fluo = input().masks().fluo();
				if (fluo != null)
					input().masks().set(processCuttingOfImage(fluo, CameraType.FLUO, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
				
				nir = input().masks().nir();
				if (nir != null)
					input().masks().set(processCuttingOfImage(nir, CameraType.NIR, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
				
				ir = input().masks().ir();
				if (ir != null)
					input().masks().set(processCuttingOfImage(ir, CameraType.IR, horFillGrade, vertFillGrade, gridHn, gridVn, well_border));
			}
		}
	}
	
	private Image processCuttingOfImage(Image img, CameraType type, double horFillGrade, double vertFillGrade, int cols, int rows, double well_border) {
		int offX = getInt("Offset X (" + type + ")", 0);
		int offY = getInt("Offset Y (" + type + ")", 0);
		Rectangle2D.Double r = getGridPos(getWellIdx(), cols, rows, (int) (img.getWidth() * horFillGrade), (int) (img.getHeight() * vertFillGrade),
				img.getWidth() / 2 + offX,
				img.getHeight() / 2 + offY);
		
		int le = (int) ((int) r.getMinX() + well_border * r.getWidth());
		int to = (int) ((int) r.getMinY() + well_border * r.getHeight());
		int ri = (int) ((int) r.getMaxX() - well_border * r.getWidth());
		int bo = (int) ((int) r.getMaxY() - well_border * r.getHeight());
		Image oimg = getBoolean("debug", false) ? img.copy() : null;
		Image res = img.io().clearOutsideRectangle(le, to, ri, bo).getImage();
		if (getBoolean("debug", false)) {
			Image ic = res.io().or(oimg.io().gamma(2).getImage()).getImage();
			ImageCanvas icc = ic.io().canvas();
			for (int wellIDX = 0; wellIDX < cols * rows; wellIDX++) {
				r = getGridPos(wellIDX, cols, rows, (int) (img.getWidth() * horFillGrade), (int) (img.getHeight() * vertFillGrade),
						img.getWidth() / 2 + offX,
						img.getHeight() / 2 + offY);
				icc = icc.drawRectangle((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight(), Color.YELLOW, 2);
			}
			res = icc.getImage();
		}
		
		res.setCameraType(type);
		
		return res;
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
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP)
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
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP)
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
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP)
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
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP)
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
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Cut Well Parts";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul><li>Additional Well Border Percent - if negaive values are specified, the actual number is processed specially. "
				+ "In this case the whole number specifies the cutting percent at day 0 and the next two digits of the fractional part "
				+ "specify the day at which the border falls to 0, "
				+ "days inbetween are linearily interpolated according to these two cut-off values.</ul>";
	}
	
	@Override
	public String getDescription() {
		return "Processes individual well parts from top images (removes other parts).";
	}
	
	@Override
	public int getDefinedWellCount(ImageProcessorOptionsAndResults options) {
		int hg = options.getIntSetting(this, "Well Grid Horizontal", 1);
		int wg = options.getIntSetting(this, "Well Grid Vertical", 1);
		int n = hg * wg;
		
		return n;
	}
}
