/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Klukas
 */
public class BlLabFilter_vis_v2 extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = true;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null
				|| getInput().getImages().getVis() == null)
			return null;
		else {
			FlexibleImage ref = getInput().getImages().getVis();
			FlexibleImage mask = getInput().getMasks().getVis();
			int w = mask.getWidth();
			int h = mask.getHeight();
			
			// remove blue markers at the side
			double hhhh = options.isBarleyInBarleySystem() ? 1d : 0.9d;
			mask = mask.getIO().hq_thresholdLAB_multi_color_or_and_not(
					new int[] { 110 }, new int[] { 190 },
					new int[] { 127 - 5 }, new int[] { 127 + 5 },
					new int[] { 70 - 5 }, new int[] { 90 + 5 },
					options.getBackground(), Integer.MAX_VALUE, false,
					new int[] {}, new int[] {},
					new int[] {}, new int[] {},
					new int[] {}, new int[] {},
					0, 1).dilate(20).
					print("removed blue markers at side", debug).getImage();
			
			mask = mask.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(ref, options.getBackground()).getImage();
			
			double blueCurbWidthBarley0_1 = options.isBarleyInBarleySystem() ? 0.15 : 0.28;
			double blueCurbHeightEndBarly0_8 = options.isBarleyInBarleySystem() ? 0.71 : 0.7;
			if (options.getCameraPosition() == CameraPosition.SIDE)
				mask = mask.getIO()
						.hq_thresholdLAB_multi_color_or_and_not(
								// noise colors
								new int[] {
										215 - 5, 225, 146 - 5, 250, 170 - 10, 151 - 20, 188 - 20, 220 - 5, 195 - 5, 100 - 5, 197 - 5, 47 - 5, 205 - 5, 110 - 5,
										50 - 5,
										146 - 5, 184 - 5, 155 - 5, 155 - 5, 171 - 5, 153 - 5, 116 - 5, 115 - 5, 168 - 5, 0, 161 - 5,
										options.isBarleyInBarleySystem() ? 255 - 5 : 135 - 5,
										options.isBarleyInBarleySystem() ? 120 - 5 : 120 - 5,
										options.isBarleyInBarleySystem() ? 255 : 235 - 5 },
								new int[] {
										256, 256, 146 + 5, 257, 230 + 10, 151 + 4, 211 + 20, 220 + 5, 195 + 5, 218 + 5, 197 + 5, 91 + 5, 245 + 5, 144 + 5,
										50 + 5,
										146 + 5, 185 + 5, 155 + 5, 155 + 5, 199 + 5, 161 + 5, 172 + 5, 126 + 5, 168 + 5, 110 + 5, 161 + 5, 135 + 5,
										250 + 5, 255 + 5 },
								new int[] {
										120 - 5, 120 - 5, 127 - 5, 118 - 10, 129 - 5, 129 - 4, 121 - 15, 120 - 5, 123 - 5, 124 - 5, 121 - 4, 126 - 5, 117 - 5,
										120 - 5,
										138 - 5, 125 - 5, 113 - 5, 121 - 5, 118 - 5, 116 - 5, 128 - 5, 120 - 5, 130 - 5, 121 - 5, 137 - 10, 122 - 5, 127 - 5,
										105 - 5, 113 - 5 },
								new int[] {
										120 + 5, 120 + 6, 127 + 5, 118 + 10, 129 + 5, 129 + 4, 121 + 5, 120 + 5, 123 + 5, 137 + 5, 121 + 4, 132 + 5, 123 + 5,
										122 + 5,
										138 + 5, 125 + 5, 113 + 5, 121 + 5, 118 + 5, 121 + 5, 132 + 5, 136 + 5, 134 + 5, 121 + 5, 137 + 10, 122 + 5, 127 + 5,
										134 + 5, 118 + 5 },
								new int[] {
										117 - 2, 122 - 14, 144 - 5, 124 - 10, 117 - 5, 114 - 4, 100 - 5, 120 - 5, 118 - 5, 121 - 5, 123 - 4, 117 - 5, 116 - 5,
										106 - 5,
										96 - 5, 100 - 5, 116 - 5, 109 - 5, 119 - 5, 116 - 5, 107 - 5, 110 - 5, 131 - 5, 105 - 5, 118 - 10, 103 - 5, 99 - 5,
										95 - 5, 121 - 5 },
								new int[] {
										124, 122 + 5, 144 + 5, 124 + 10, 117 + 5, 114 + 4, 100 + 8, 120 + 5, 118 + 5, 126 + 5, 123 + 4, 128 + 5, 123 + 5,
										113 + 5,
										96 + 5, 100 + 5, 116 + 5, 109 + 5, 119 + 5, 119 + 5, 111 + 5, 114 + 5, 131 + 5, 105 + 5, 118 + 10, 103 + 5, 99 + 5,
										128, 133 + 5 },
								options.getBackground(), 1, false,
								// plant colors
								new int[] {}, new int[] {},
								new int[] {}, new int[] {},
								new int[] {}, new int[] {},
								blueCurbWidthBarley0_1,
								blueCurbHeightEndBarly0_8).
						border_left_right((int) (options.isBarleyInBarleySystem() ? 0 : w * 0.05), Color.red.getRGB()).
						print("removed noise", debug).getImage();
			else
				mask = mask
						.getIO()
						.hq_thresholdLAB_multi_color_or_and_not(
								// noise colors
								new int[] { 215 - 5, 225, -1, 250, 170 - 10, 151 - 20, 188 - 20, 220 - 5, 195 - 5, 100 - 5, 197 - 5, 47 - 5, 205 - 5, 110 - 5,
										50 - 5,
										146 - 5, 184 - 5, 155 - 5, 155 - 5, 171 - 5, 153 - 5, 116 - 5, 115 - 5, 168 - 5, 0, 161 - 5, 135 - 5,
										80 - 5,
										options.isBarleyInBarleySystem() ? 255 : 0, 160 },
								new int[] { 256, 256, 146 + 5, 257, 230 + 10, 151 + 4, 211 + 20, 220 + 5, 195 + 5, 218 + 5, 197 + 5, 91 + 5, 245 + 5, 144 + 5,
										50 + 5,
										146 + 5, 185 + 5, 155 + 5, 155 + 5, 199 + 5, 161 + 5, 172 + 5, 126 + 5, 168 + 5, 110 + 5, 161 + 5, 135 + 5,
										255, 190 + 5, 255 },
								new int[] { 120 - 5, 120 - 5, 127 - 5, 118 - 10, 129 - 5, 129 - 4, 121 - 15, 120 - 5, 123 - 5, 124 - 5, 121 - 4, 126 - 5, 117 - 5,
										120 - 5,
										138 - 5, 125 - 5, 113 - 5, 121 - 5, 118 - 5, 116 - 5, 128 - 5, 120 - 5, 130 - 5, 121 - 5, 137 - 10, 122 - 5, 127 - 5,
										110 - 5, 115 - 5, 118 - 5 },
								new int[] { 120 + 5, 120 + 6, 127 + 5, 118 + 10, 129 + 5, 129 + 4, 121 + 5, 120 + 5, 123 + 5, 137 + 5, 121 + 4, 132 + 5, 123 + 5,
										122 + 5,
										138 + 5, 125 + 5, 123 + 5, 121 + 5, 118 + 5, 121 + 5, 132 + 5, 136 + 5, 134 + 5, 121 + 5, 137 + 10, 122 + 5, 127 + 5,
										127 + 5, 134 + 5, 135 + 5 },
								new int[] { 117 - 2, 122 - 14, 144 - 5, 124 - 10, 117 - 5, 114 - 4, 100 - 5, 120 - 5, 118 - 5, 121 - 5, 123 - 4, 117 - 5, 116 - 5,
										106 - 5,
										96 - 5, 100 - 5, 116 - 5, 109 - 5, 119 - 5, 116 - 5, 107 - 5, 110 - 5, 131 - 5, 105 - 5, 118 - 10, 103 - 5, 99 - 5,
										90 - 5, 90 - 5, 80 - 5 },
								new int[] { 125, 122 + 5, 144 + 5, 124 + 10, 117 + 5, 114 + 4, 100 + 8, 120 + 5, 118 + 5, 126 + 5, 123 + 4, 128 + 5, 123 + 5,
										113 + 5,
										96 + 5, 100 + 5, 120 + 5, 109 + 5, 119 + 5, 119 + 5, 111 + 5, 114 + 5, 131 + 5, 105 + 5, 118 + 10, 103 + 5, 99 + 5,
										110 + 5, 149 + 5, 102 + 5 },
								options.getBackground(), Integer.MAX_VALUE, false,
								// plant colors
								options.isBarleyInBarleySystem() && false ? new int[] { 120 - 5, 167 - 5 } : new int[] {},
								options.isBarleyInBarleySystem() && false ? new int[] { 150 + 5, 191 + 5 } : new int[] {},
								options.isBarleyInBarleySystem() && false ? new int[] { 120 - 5, 118 - 5 } : new int[] {},
								options.isBarleyInBarleySystem() && false ? new int[] { 120 + 5, 122 + 5 } : new int[] {},
								options.isBarleyInBarleySystem() && false ? new int[] { 128 - 5, 117 - 5 } : new int[] {},
								options.isBarleyInBarleySystem() && false ? new int[] { 128 + 5, 123 } : new int[] {},
								blueCurbWidthBarley0_1,
								blueCurbHeightEndBarly0_8).
						print("removed noise", debug).getImage();
			
			return mask;
		}
	}
}
