package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.AbstractImageAnalysisBlockFIS;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.ImageAnalysisBlock;
import iap.pipelines.ImageProcessorOptionsAndResults;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import org.SystemAnalysis;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

/**
 * Move and zoom images of the different camera systems so that they align.
 * 
 * @author klukas
 */
public class BlAlign extends AbstractBlock {
	
	@Override
	protected void debugPipelineBlock(final Class<?> blockType, final CameraType inpImageType,
			final MaskAndImageSet in,
			final BlockResultSet brs, final ImageProcessorOptionsAndResults options,
			final int blockPos, final AbstractImageAnalysisBlockFIS inst) {
		
		final MaskAndImageSet inputSet = in.copy();
		final ZoomedImage ic = new ZoomedImage(null);
		final JScrollPane jsp = new JScrollPane(ic);
		jsp.setBorder(BorderFactory.createLoweredBevelBorder());
		
		final JButton okButton = new JButton();
		JLabel textField = new JLabel("");
		
		okButton.setAction(new AbstractAction("Update View") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					ImageAnalysisBlock inst = (ImageAnalysisBlock) blockType.newInstance();
					ImageSet a = inputSet.images().copy();
					ImageSet b = inputSet.masks().copy();
					MaskAndImageSet ab = new MaskAndImageSet(a, b);
					((BlAlign) inst).preventDebugValues = true;
					inst.setInputAndOptions(getWellIdx(), ab, options, brs, blockPos, null);
					ab = inst.process();
					ImageSet in = ab.images();
					
					Image vis = in.getImage(inpImageType).copy();
					
					int vs = jsp.getVerticalScrollBar().getValue();
					int hs = jsp.getHorizontalScrollBar().getValue();
					Image selImage = ab.images().getImage(inpImageType);
					if (selImage == null)
						throw new Exception("Input image not available");
					int f1 = options.getIntSetting(inst, "Debug-Crossfade-F1_5", 5);
					int f2 = options.getIntSetting(inst, "Debug-Crossfade-F2_2", 2);
					int f3 = options.getIntSetting(inst, "Debug-Crossfade-F3_1", 1);
					vis = vis.io().enhanceContrast().getImage();
					ImageOperation visFluo = vis.io().crossfade(
							in.fluo().copy().io().enhanceContrast().getImage().resize(vis.getWidth(), vis.getHeight()),
							f1, f2, f3);
					
					ic.setImage(visFluo.getAsBufferedImage());
					jsp.setViewportView(ic);
					jsp.revalidate();
					jsp.getVerticalScrollBar().setValue(vs);
					jsp.getHorizontalScrollBar().setValue(hs);
					okButton.setText("<html><center>Update View<br>Updated " + SystemAnalysis.getCurrentTimeInclSec());
				} catch (Exception e) {
					e.printStackTrace();
					MainFrame.showMessageDialog("Error: " + e.getMessage(), "Error");
					okButton.setText("<html><center>Update View<br>Error " + e.getMessage());
				}
			}
		});
		
		JComponent editAndUpdate = TableLayout.get3Split(
				textField,
				null,
				TableLayout.get3Split(
						ic.getZoomSlider(), null, okButton, TableLayout.PREFERRED, 5, TableLayout.PREFERRED),
				TableLayout.FILL, 5, TableLayout.PREFERRED);
		JComponent v = TableLayout.get3SplitVertical(
				jsp, null, editAndUpdate, TableLayout.FILL, 5, TableLayout.PREFERRED);
		v.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		final Timer timer = new Timer(500, new ActionListener() {
			double lastSum = 0;
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double zoomX = Double.NaN;
				double zoomY = Double.NaN;
				double offX = Double.NaN;
				double offY = Double.NaN;
				String prefix = "UNKNOWN";
				switch (inpImageType) {
					case VIS:
						prefix = "VIS";
						break;
					case FLUO:
						prefix = "FLUO";
						break;
					case NIR:
						prefix = "NIR";
						break;
					case IR:
						prefix = "IR";
						break;
				}
				zoomX = 100d / inst.getDouble(prefix + " Zoom X", 100);
				zoomY = 100d / inst.getDouble(prefix + " Zoom Y", 100);
				offX = inst.getDouble(prefix + " Shift X", 0);
				offY = inst.getDouble(prefix + " Shift Y", 0);
				int f1 = options.getIntSetting(inst, "Debug-Crossfade-F1_5", 5);
				int f2 = options.getIntSetting(inst, "Debug-Crossfade-F2_2", 2);
				int f3 = options.getIntSetting(inst, "Debug-Crossfade-F3_1", 1);
				double currentSum = zoomX + zoomY + offX + offY + f1 + f2 + f3;
				if (Math.abs(lastSum - currentSum) > 0.0001) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Detected cut settings change, updating view...");
					okButton.doClick();
					lastSum = currentSum;
				}
			}
		});
		
		if (inpImageType != CameraType.FLUO) {
			final JFrame jf = MainFrame.showMessageWindow(inpImageType.name() + " & FLUO", v);
			jf.addHierarchyListener(new HierarchyListener() {
				
				@Override
				public void hierarchyChanged(HierarchyEvent arg0) {
					if (!jf.isVisible())
						timer.stop();
				}
			});
			timer.setRepeats(true);
			timer.start();
		}
	}
	
	@Override
	protected Image processImage(Image image) {
		if (image == null)
			return image;
		return cut(image.io());
	}
	
	@Override
	protected Image processMask(Image mask) {
		return cut(mask.io());
	}
	
	private Image cut(ImageOperation img) {
		double zoomX = Double.NaN;
		double zoomY = Double.NaN;
		double offX = Double.NaN;
		double offY = Double.NaN;
		String prefix = "UNKNOWN";
		switch (img.getCameraType()) {
			case VIS:
				prefix = "VIS";
				break;
			case FLUO:
				prefix = "FLUO";
				break;
			case NIR:
				prefix = "NIR";
				break;
			case IR:
				prefix = "IR";
				break;
		}
		zoomX = 100d / getDouble(prefix + " Zoom X", 100);
		zoomY = 100d / getDouble(prefix + " Zoom Y", 100);
		offX = getDouble(prefix + " Shift X", 0);
		offY = getDouble(prefix + " Shift Y", 0);
		
		// add border or cut outside
		int horTooTooMuch = (int) ((1d - zoomX) * img.getWidth());
		int horTm = -horTooTooMuch / 2;
		int verTooTooMuch = (int) ((1d - zoomY) * img.getHeight());
		int verTm = -verTooTooMuch / 2;
		return img
				.addBorder(horTm, verTm, (int) offX, (int) offY, ImageOperation.BACKGROUND_COLORint)
				.getImage();
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
		return "Align Camera Images";
	}
	
	@Override
	public String getDescription() {
		return "Moves and resizes camera images, so that they can be used for overlaying each other.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul>" +
				"<li>Increase X values to move an image to the right." +
				"<li>Increase Y values to move an image down." +
				"<li>Increase Zoom-X values to increase horizontal size." +
				"<li>Increase Zoom-Y values to increase vertical size." +
				"</ul>";
	}
}
