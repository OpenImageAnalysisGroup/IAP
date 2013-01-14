package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.ImageAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class BlCutZoomedImages extends AbstractBlock {
	
	boolean debugValues;
	boolean preventDebugValues;
	
	@Override
	protected void prepare() {
		super.prepare();
		debugValues = !preventDebugValues && getBoolean("debug", false);
		if (debugValues) {
			if (input().images().vis() != null && input().images().fluo() != null)
				debugIt(this.getClass(), FlexibleImageType.VIS, input(), getProperties(), options, getBlockPosition());
			if (input().images().nir() != null && input().images().fluo() != null)
				debugIt(this.getClass(), FlexibleImageType.NIR, input(), getProperties(), options, getBlockPosition());
			if (input().images().ir() != null && input().images().fluo() != null)
				debugIt(this.getClass(), FlexibleImageType.IR, input(), getProperties(), options, getBlockPosition());
		}
	}
	
	private static void debugIt(final Class blockType, final FlexibleImageType inpImageType,
			final FlexibleMaskAndImageSet inputSet,
			final BlockResultSet brs, final ImageProcessorOptions options,
			final int blockPos) {
		
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
					ImageAnalysisBlockFIS inst = (ImageAnalysisBlockFIS) blockType.newInstance();
					FlexibleImageSet a = inputSet.images().copy();
					FlexibleImageSet b = inputSet.masks().copy();
					FlexibleMaskAndImageSet ab = new FlexibleMaskAndImageSet(a, b);
					((BlCutZoomedImages) inst).preventDebugValues = true;
					inst.setInputAndOptions(ab, options, brs, blockPos, null);
					ab = inst.process();
					FlexibleImageSet in = ab.images();
					
					FlexibleImage vis = in.getImage(inpImageType).copy();
					
					int vs = jsp.getVerticalScrollBar().getValue();
					int hs = jsp.getHorizontalScrollBar().getValue();
					FlexibleImage selImage = ab.images().getImage(inpImageType);
					if (selImage == null)
						throw new Exception("Input image not available");
					int f1 = options.getIntSetting(inst, "Debug-Crossfade-F1_5", 5);
					int f2 = options.getIntSetting(inst, "Debug-Crossfade-F2_2", 2);
					int f3 = options.getIntSetting(inst, "Debug-Crossfade-F3_1", 1);
					ImageOperation visFluo = vis.io().crossfade(
							in.fluo().copy().resize(vis.getWidth(), vis.getHeight()),
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
		MainFrame.showMessageWindow(inpImageType.name() + " & FLUO", v);
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		if (image == null)
			return image;
		return cut(image.io());
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		return cut(mask.io());
	}
	
	private FlexibleImage cut(ImageOperation img) {
		double zoomX = Double.NaN;
		double zoomY = Double.NaN;
		double scaleY = Double.NaN;
		double offX = Double.NaN;
		double offY = Double.NaN;
		String prefix = "UNKNOWN";
		switch (img.getType()) {
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
		scaleY = getDouble(prefix + " Scale Vertical", 100) / 100d;
		offX = getDouble(prefix + " Shift X", 0);
		offY = getDouble(prefix + " Shift Y", 0);
		
		// add border or cut outside
		int horTooTooMuch = (int) ((1d - zoomX) * img.getWidth());
		int horTm = -horTooTooMuch / 2;
		int verTooTooMuch = (int) ((1d - zoomY) * img.getHeight());
		int verTm = -verTooTooMuch / 2;
		if (debugValues || preventDebugValues) {
			System.out.println("IMAGE TYPE " + img.getType() + ":");
			System.out.println("ZOOM  X =" + StringManipulationTools.formatNumber(1d / zoomX, "#.##"));
			System.out.println("ZOOM  Y =" + StringManipulationTools.formatNumber(1d / zoomY, "#.##"));
			System.out.println("SCALE Y =" + StringManipulationTools.formatNumber(scaleY, "#.##"));
			System.out.println("OFF X   =" + StringManipulationTools.formatNumber(offX, "#"));
			System.out.println("OFF Y   =" + StringManipulationTools.formatNumber(offY, "#"));
		}
		return img
				.scale(1, scaleY, false)
				.addBorder(horTm, verTm, (int) offX, (int) offY, ImageOperation.BACKGROUND_COLORint)
				.getImage();
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
