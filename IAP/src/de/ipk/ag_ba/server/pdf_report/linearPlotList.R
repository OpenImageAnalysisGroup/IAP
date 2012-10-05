# TODO: Add comment
# 
# Author: Entzian
###############################################################################
### section start with: 01

nBoxOptions= NULL

nBoxPlotList <- list(	
		#############################  new Section ##############################
		"Weight A (g)" = list(
							plotName = "weight before watering (g)",
							section = 1,
							subsection = 1,
							subsubsection = 1), 
		"Weight B (g)" = list(
							plotName = "weight after watering (g)", 
							section = 1,
							subsection = 1,
							subsubsection = 2),
		"Water (sum of day)" = list(
									plotName = "water weight (g)", 
									section = 1,
									subsection = 2),
		
				#############################  new SubSection ##############################
		"mark1.y (percent)" = list(
								plotName = "blue marker position from top (%)", 
								section = 2,
								subsection = 1,
								subsubsection = 1),
		"mark3.y (percent)" = list(
								plotName = "blue marker position from bottom  (%)", 
								section = 2,
								subsection = 1,
								subsubsection = 2),
						
		#############################  new Section ##############################
				
		"lm3s_nostress_ratio.side.area.norm (%)" = list(
				plotName = "initial quality - side area", 
				section = 3,
				subsection = 1,
				subsubsection = 1,
				paragraph = 1),
		"lm3s_nostress_ratio.side.hull.pc2.norm (%)" = list(
				plotName = "initial quality - side pc2", 
				section = 3,
				subsection = 1,
				subsubsection = 1,
				paragraph = 2),
		"lm3s_nostress_ratio.side.nir.intensity.average (%)" = list(
				plotName = "initial quality - side nir intensity", 
				section = 3,
				subsection = 1,
				subsubsection = 1,
				paragraph = 3),
		
		
#		"lm3s_nostress_slope.side.area.norm (%)" = list(
#				plotName = "velocity of the stress reaction - side area", 
#				section = 3,
#				subsection = 1,
#				subsubsection = 2,
#				paragraph = 1),
#		"lm3s_nostress_slope.side.hull.pc2.norm (%)" = list(
#				plotName = "velocity of the stress reaction - side pc2", 
#				section = 3,
#				subsection = 1,
#				subsubsection = 2,
#				paragraph = 2),
#		"lm3s_nostress_solpe.side.nir.intensity.average (%)" = list(
#				plotName = "velocity of the stress reaction - side nir intensity", 
#				section = 3,
#				subsection = 1,
#				subsubsection = 2,
#				paragraph = 3),
		
		"lm3s_stress_start.side.area.norm (day)" = list(
				plotName = "first day of stress - side area", 
				section = 3,
				subsection = 2,
				subsubsection = 1,
				paragraph = 1),
		"lm3s_stress_start.side.hull.pc2.norm (day)" = list(
				plotName = "first day of stress - side pc2", 
				section = 3,
				subsection = 2,
				subsubsection = 1,
				paragraph = 2),												
		"lm3s_stress_start.side.nir.intensity.average (day)" = list(
				plotName = "first day of stress - side nir intensity", 
				section = 3,
				subsection = 2,
				subsubsection = 1,
				paragraph = 3),
	
		"lm3s_stress_max_extend.side.area.norm (%)" = list(
				plotName = "stress reaction magnitude - side area", 
				section = 3, 
				subsection = 2,
				subsubsection = 2,
				paragraph = 1), 
		"lm3s_stress_max_extend.side.hull.pc2.norm (%)" = list(
				plotName = "stress reaction magnitude - side pc2", 
				section = 3,
				subsection = 2,
				subsubsection = 2,
				paragraph = 2), 
		"lm3s_stress_max_extend.side.nir.intensity.average (%)" = list(
				plotName = "stress reaction magnitude - side nir intensity", 
				section = 3,
				subsection = 2,
				subsubsection = 2,
				paragraph = 3), 
		
		"lm3s_stress_slope.side.area.norm (%/day)" = list(
				plotName = "velocity of the stress reaction - side area", 
				section = 3, 
				subsection = 2,
				subsubsection = 3,
				paragraph = 1), 											
		"lm3s_stress_slope.side.hull.pc2.norm (%/day)" = list(
				plotName = "velocity of the stress reaction - side pc2", 
				section = 3,
				subsection = 2,
				subsubsection = 3, 
				paragraph = 2), 
		"lm3s_stress_slope.side.nir.intensity.average (%/day)" = list(
				plotName = "velocity of the stress reaction - side nir intensity", 
				section = 3, 
				subsection = 2,
				subsubsection = 3, 
				paragraph = 3), 
		
		"lm3s_recovery_start.side.area.norm (day)" = list(
				plotName = "Recovery periode start time - side area", 
				section = 3,  
				subsection = 3, 
				subsubsection = 1,
				paragraph = 1), 
		"lm3s_recovery_start.side.hull.pc2.norm (day)" = list(
				plotName = "Recovery periode start time - side pc2", 
				section = 3, 
				subsection = 3, 
				subsubsection = 1,
				paragraph = 2),
		"lm3s_recovery_start.side.nir.intensity.average (day)" = list(
				plotName = "Recovery periode start time - nir intensity", 
				section = 3, 
				subsection = 3, 
				subsubsection = 1,
				paragraph = 3),
		
		"lm3s_recovery_slope.side.hull.pc2.norm (%/day)" = list(
				plotName = "Stress recovery ability - side pc2", 
				section = 3, 
				subsection = 3, 
				subsubsection = 2,
				paragraph = 1),
		"lm3s_recovery_slope.side.nir.intensity.average (%/day)" = list(
				plotName = "Stress recovery ability - nir intensity", 
				section = 3, 
				subsection = 3, 
				subsubsection = 2,
				paragraph = 2),
		"lm3s_recovery_slope.side.area.norm (%/day)" = list(
				plotName = "Stress recovery ability - side area", 
				section = 3, 
				subsection = 3, 
				subsubsection = 2,
				paragraph = 3),
		
		
		#############################  new Section ##############################
		
		"volume.iap (px^3)" = list(
				plotName = "digital biomass (visible light images, IAP formula) sqrt(px^3)", 
				section = 4,
				subsection = 1,
				subsubsection = 1,
				paragraph = 1), 
		"volume.lt (px^3)" = list(
				plotName = "digital biomass (visible light, LemnaTec 0,90 formula) sqrt(px^3)", 
				section = 4,
				subsection = 2,
				subsubsection = 1), 
						
		"volume.iap.wue" = list(
				plotName = "volume based water use efficiency", 
				section = 4,
				subsection = 3,
				subsubsection = 1),	
		"side.area.avg.wue" = list(
				plotName = "digital side area based water use efficiency",
				section = 4,
				subsection = 3,
				subsubsection = 2),	
		
		"volume.fluo.iap" = list(
				plotName = "digital biomass (fluorescence images, IAP formula) sqrt(px^3)", 
				section = 4,
				subsection = 1,
				subsubsection = 2,
				paragraph = 1),
		
		#############################  new Section ##############################										
		
		"side.height.norm (mm)" = list(
				plotName = "height (zoom corrected) (mm)", 
				section = 5,
				subsection = 1,
				subsubsection = 1), 						
		"side.height (px)" = list(
				plotName = "height (px)", 
				section = 5,
				subsection = 2,
				subsubsection = 1),
	
		"side.width.norm (mm)" = list(
				plotName = "width (zoom corrected) (mm)", 
				section = 5,
				subsection = 3,
				subsubsection = 1),
		"side.width (px)" = list(
				plotName = "width (px)", 
				section = 5,
				subsection = 4,
				subsubsection = 1),
 
		"side.area.norm (mm^2)" = list(
				plotName = "side area (zoom corrected) (mm^2)", 
				section = 5,
				subsection = 5,
				subsubsection = 1),
		"side.area (px)" = list(
				plotName = "side area (px)", 
				section = 5,
				subsection = 6,
				subsubsection = 1),
 
		"top.area.norm (mm^2)" = list(
				plotName = "top area (zoom corrected) (mm^2)", 
				section = 5,
				subsection = 7,
				subsubsection = 1),
		"top.area (px)" = list(
				plotName = "top area (px)", 
				section = 5,
				subsection = 8,
				subsubsection = 1),
		
		"side.border.length.norm (mm)" = list(
				plotName = "side border length (zoom corrected) (mm)", 
				section = 5,
				subsection = 9,
				subsubsection = 1),
		"side.border.length (px)" = list(
				plotName = "side border length (px)", 
				section = 5,
				subsection = 10,
				subsubsection = 1),
		
		
		 
		 "side.area.relative" = list(
				 plotName = "side area growth rate (%/day)", 
				 section = 6,
				 subsection = 1), 	
		 "side.height.norm.relative" = list(
				 plotName = "plant height growth rate (%/day)", 
				 section = 6,
				 subsection = 2), 
		 "side.width.norm.relative" = list(
				 plotName = "plant width growth rate (%/day)", 
				 section = 6,
				 subsection = 7), 
		 "top.area.relative" = list(
				 plotName = "top area growth rate (%/day)", 
				 section = 6,
				 subsection = 4), 
		 "volume.iap.relative" = list(
				 plotName = "volume growth (visible light images, IAP based formula) (%/day)", 
				 section = 6,
				 subsection = 5), 
		
		"side.vis.hsv.h.average" = list(plotName = "color shade side - average", section = 7, subsection = 1, subsubsection = 1, paragraph = 1),
		"side.vis.hsv.h.normalized.average" = list(plotName = "color shade side - average (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 2),
		"side.vis.hsv.h.stddev" = list(plotName = "color shade side - stddev", section = 7, subsection = 1, subsubsection = 1, paragraph = 3),
		"side.vis.hsv.h.normalized.stddev" = list(plotName = "color shade side - stddev (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 4),
		"side.vis.hsv.h.skewness" = list(plotName = "color shade side - skewness", section = 7, subsection = 1, subsubsection = 1, paragraph = 5),
		"side.vis.hsv.h.normalized.skewness" = list(plotName = "color shade side - skewness (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 6),
		"side.vis.hsv.h.kurtosis" = list(plotName = "color shade side - kurtosis", section = 7, subsection = 1, subsubsection = 1, paragraph = 7),
		"side.vis.hsv.h.normalized.kurtosis" = list(plotName = "color shade side - kurtosis (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 8),
		"side.vis.hsv.h.histogram.bin.01.0_12" = list(plotName = "color shade side - bin1", section = 7, subsection = 1, subsubsection = 1, paragraph = 9),
		"side.vis.hsv.h.normalized.histogram.bin.01.0_12" = list(plotName = "color shade side - bin1 (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 10),
		"side.vis.hsv.h.histogram.bin.02.12_25" = list(plotName = "color shade side - bin2", section = 7,	subsection = 1,	subsubsection = 1, paragraph = 11),
		"side.vis.hsv.h.normalized.histogram.bin.02.12_25" = list(plotName = "color shade side - bin2 (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 12),
		"side.vis.hsv.h.histogram.bin.03.25_38" = list(plotName = "color shade side - bin3", section = 7,	subsection = 1,	subsubsection = 1, paragraph = 13),
		"side.vis.hsv.h.normalized.histogram.bin.03.25_38" = list(plotName = "color shade side - bin3 (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 14),
		"side.vis.hsv.h.histogram.bin.04.38_51" = list(plotName = "color shade side - bin4", section = 7, subsection = 1, subsubsection = 1, paragraph = 15),
		"side.vis.hsv.h.normalized.histogram.bin.04.38_51" = list(plotName = "color shade side - bin4 (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 16),
		"side.vis.hsv.h.histogram.bin.05.51_63" = list(plotName = "color shade side - bin5", section = 7, subsection = 1, subsubsection = 1, paragraph = 17),
		"side.vis.hsv.h.normalized.histogram.bin.05.51_63" = list(plotName = "color shade side - bin5 (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 18),
		"side.vis.hsv.h.histogram.bin.06.63_76" = list(plotName = "color shade side - bin6", section = 7, subsection = 1, subsubsection = 1, paragraph = 19),
		"side.vis.hsv.h.normalized.histogram.bin.06.63_76" = list(plotName = "color shade side - bin6 (zoom corrected)", section = 7, subsection = 1, subsubsection = 1, paragraph = 20),		
		"side.vis.hsv.dgci.average" = list(plotName = "side dark green color index", section = 7, subsection = 1, subsubsection = 1, paragraph = 21),
		
		"side.vis.hsv.s.average" = list(plotName = "saturation side - average", section = 7, subsection = 1, subsubsection = 2, paragraph = 1),
		"side.vis.hsv.s.normalized.average" = list(plotName = "saturation side - average (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 2),
		"side.vis.hsv.s.stddev" = list(plotName = "saturation side - stddev", section = 7, subsection = 1, subsubsection = 2, paragraph = 3),
		"side.vis.hsv.s.normalized.stddev" = list(plotName = "saturation side - stddev (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 4),
		"side.vis.hsv.s.skewness" = list(plotName = "saturation side - skewness", section = 7, subsection = 1, subsubsection = 2, paragraph = 5),
		"side.vis.hsv.s.normalized.skewness" = list(plotName = "saturation side - skewness (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 6),
		"side.vis.hsv.s.kurtosis" = list(plotName = "saturation side - kurtosis", section = 7, subsection = 1, subsubsection = 2, paragraph = 7),
		"side.vis.hsv.s.normalized.kurtosis" = list(plotName = "saturation side - kurtosis (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 8),
		"side.vis.hsv.s.histogram.bin.01.0_12" = list(plotName = "saturation side - bin1", section = 7, subsection = 1, subsubsection = 2, paragraph = 9),
		"side.vis.hsv.s.normalized.histogram.bin.01.0_12" = list(plotName = "saturation side - bin1 (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 10),
		"side.vis.hsv.s.histogram.bin.02.12_25" = list(plotName = "saturation side - bin2", section = 7, subsection = 1, subsubsection = 2, paragraph = 11),
		"side.vis.hsv.s.normalized.histogram.bin.02.12_25" = list(plotName = "saturation side - bin2 (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 12),
		"side.vis.hsv.s.histogram.bin.03.25_38" = list(plotName = "saturation side - bin3", section = 7, subsection = 1, subsubsection = 2, paragraph = 13),
		"side.vis.hsv.s.normalized.histogram.bin.03.25_38" = list(plotName = "saturation side - bin3 (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 14),
		"side.vis.hsv.s.histogram.bin.04.38_51" = list(plotName = "saturation side - bin4", section = 7, subsection = 1, subsubsection = 2, paragraph = 15),
		"side.vis.hsv.s.normalized.histogram.bin.04.38_51" = list(plotName = "saturation side - bin4 (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 16),
		"side.vis.hsv.s.histogram.bin.05.51_63" = list(plotName = "saturation side - bin5", section = 7, subsection = 1, subsubsection = 2, paragraph = 17),
		"side.vis.hsv.s.normalized.histogram.bin.05.51_63" = list(plotName = "saturation side - bin5 (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 18),
		"side.vis.hsv.s.histogram.bin.06.63_76" = list(plotName = "saturation side - bin6", section = 7, subsection = 1, subsubsection = 2, paragraph = 19),
		"side.vis.hsv.s.normalized.histogram.bin.06.63_76" = list(plotName = "saturation side - bin6 (zoom corrected)", section = 7, subsection = 1, subsubsection = 2, paragraph = 20),
		
		"side.vis.hsv.v.average" = list(plotName = "brightness side - average", section = 7, subsection = 1, subsubsection = 3, paragraph = 1),
		"side.vis.hsv.v.normalized.average" = list(plotName = "brightness side - average (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 2),
		"side.vis.hsv.v.stddev" = list(plotName = "brightness side - stddev", section = 7, subsection = 1, subsubsection = 3, paragraph = 3),
		"side.vis.hsv.v.normalized.stddev" = list(plotName = "brightness side - stddev (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 4),
		"side.vis.hsv.v.skewness" = list(plotName = "brightness side - skewness", section = 7, subsection = 1, subsubsection = 3, paragraph = 5),
		"side.vis.hsv.v.normalized.skewness" = list(plotName = "brightness side - skewness (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 6),
		"side.vis.hsv.v.kurtosis" = list(plotName = "brightness side - kurtosis", section = 7, subsection = 1, subsubsection = 3, paragraph = 7),
		"side.vis.hsv.v.normalized.kurtosis" = list(plotName = "brightness side - kurtosis (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 8),
		"side.vis.hsv.v.histogram.bin.01.0_12" = list(plotName = "brightness side - bin1", section = 7, subsection = 1, subsubsection = 3, paragraph = 9),
		"side.vis.hsv.v.normalized.histogram.bin.01.0_12" = list(plotName = "brightness side - bin1 (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 10),
		"side.vis.hsv.v.histogram.bin.02.12_25" = list(plotName = "brightness side - bin2", section = 7, subsection = 1, subsubsection = 3, paragraph = 11),
		"side.vis.hsv.v.normalized.histogram.bin.02.12_25" = list(plotName = "brightness side - bin2 (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 12),
		"side.vis.hsv.v.histogram.bin.03.25_38" = list(plotName = "brightness side - bin3", section = 7, subsection = 1, subsubsection = 3, paragraph = 13),
		"side.vis.hsv.v.normalized.histogram.bin.03.25_38" = list(plotName = "brightness side - bin3 (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 14),
		"side.vis.hsv.v.histogram.bin.04.38_51" = list(plotName = "brightness side - bin4", section = 7, subsection = 1, subsubsection = 3, paragraph = 15),
		"side.vis.hsv.v.normalized.histogram.bin.04.38_51" = list(plotName = "brightness side - bin4 (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 16),
		"side.vis.hsv.v.histogram.bin.05.51_63" = list(plotName = "brightness side - bin5", section = 7, subsection = 1, subsubsection = 3, paragraph = 17),
		"side.vis.hsv.v.normalized.histogram.bin.05.51_63" = list(plotName = "brightness side - bin5 (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 18),
		"side.vis.hsv.v.histogram.bin.06.63_76" = list(plotName = "brightness side - bin6", section = 7, subsection = 1, subsubsection = 3, paragraph = 19),
		"side.vis.hsv.v.normalized.histogram.bin.06.63_76" = list(plotName = "brightness side - bin6 (zoom corrected)", section = 7, subsection = 1, subsubsection = 3, paragraph = 20),
		
		"top.vis.hsv.h.average" = list(plotName = "color shade top - average", section = 7, subsection = 2, subsubsection = 1, paragraph = 1),
		"top.vis.hsv.h.normalized.average" = list(plotName = "color shade top - average (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 2),
		"top.vis.hsv.h.stddev" = list(plotName = "color shade top - stddev", section = 7, subsection = 2, subsubsection = 1, paragraph = 3),
		"top.vis.hsv.h.normalized.stddev" = list(plotName = "color shade top - stddev (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 4),
		"top.vis.hsv.h.skewness" = list(plotName = "color shade top - skewness", section = 7, subsection = 2, subsubsection = 1, paragraph = 5),
		"top.vis.hsv.h.normalized.skewness" = list(plotName = "color shade top - skewness (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 6),
		"top.vis.hsv.h.kurtosis" = list(plotName = "color shade top - kurtosis", section = 7, subsection = 2, subsubsection = 1, paragraph = 7),
		"top.vis.hsv.h.normalized.kurtosis" = list(plotName = "color shade top - kurtosis (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 8),
		"top.vis.hsv.h.histogram.bin.01.0_12" = list(plotName = "color shade top - bin1", section = 7, subsection = 2, subsubsection = 1, paragraph = 9),
		"top.vis.hsv.h.normalized.histogram.bin.01.0_12" = list(plotName = "color shade top - bin1 (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 10),
		"top.vis.hsv.h.histogram.bin.02.12_25" = list(plotName = "color shade top - bin2", section = 7,	subsection = 2,	subsubsection = 1, paragraph = 11),
		"top.vis.hsv.h.normalized.histogram.bin.02.12_25" = list(plotName = "color shade top - bin2 (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 12),
		"top.vis.hsv.h.histogram.bin.03.25_38" = list(plotName = "color shade top - bin3", section = 7,	subsection =2,	subsubsection = 1, paragraph = 13),
		"top.vis.hsv.h.normalized.histogram.bin.03.25_38" = list(plotName = "color shade top - bin3 (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 14),
		"top.vis.hsv.h.histogram.bin.04.38_51" = list(plotName = "color shade top - bin4", section = 7, subsection = 2, subsubsection = 1, paragraph = 15),
		"top.vis.hsv.h.normalized.histogram.bin.04.38_51" = list(plotName = "color shade top - bin4 (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 16),
		"top.vis.hsv.h.histogram.bin.05.51_63" = list(plotName = "color shade top - bin5", section = 7, subsection = 2, subsubsection = 1, paragraph = 17),
		"top.vis.hsv.h.normalized.histogram.bin.05.51_63" = list(plotName = "color shade top - bin5 (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 18),
		"top.vis.hsv.h.histogram.bin.06.63_76" = list(plotName = "color shade top - bin6", section = 7, subsection = 2, subsubsection = 1, paragraph = 19),
		"top.vis.hsv.h.normalized.histogram.bin.06.63_76" = list(plotName = "color shade top - bin6 (zoom corrected)", section = 7, subsection = 2, subsubsection = 1, paragraph = 20),				
		"top.vis.hsv.dgci.average" = list(plotName = "top dark green color index", section = 7, subsection = 2, subsubsection = 1, paragraph = 21),
		
		"top.vis.hsv.s.average" = list(plotName = "saturation top - average", section = 7, subsection = 2, subsubsection = 2, paragraph = 1),
		"top.vis.hsv.s.normalized.average" = list(plotName = "saturation top - average (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 2),
		"top.vis.hsv.s.stddev" = list(plotName = "saturation top - stddev", section = 7, subsection = 2, subsubsection = 2, paragraph = 3),
		"top.vis.hsv.s.normalized.stddev" = list(plotName = "saturation top - stddev (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 4),
		"top.vis.hsv.s.skewness" = list(plotName = "saturation top - skewness", section = 7, subsection = 2, subsubsection = 2, paragraph = 5),
		"top.vis.hsv.s.normalized.skewness" = list(plotName = "saturation top - skewness (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 6),
		"top.vis.hsv.s.kurtosis" = list(plotName = "saturation top - kurtosis", section = 7, subsection = 2, subsubsection = 2, paragraph = 7),
		"top.vis.hsv.s.normalized.kurtosis" = list(plotName = "saturation top - kurtosis (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 8),
		"top.vis.hsv.s.histogram.bin.01.0_12" = list(plotName = "saturation top - bin1", section = 7, subsection = 2, subsubsection = 2, paragraph = 9),
		"top.vis.hsv.s.normalized.histogram.bin.01.0_12" = list(plotName = "saturation top - bin1 (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 10),
		"top.vis.hsv.s.histogram.bin.02.12_25" = list(plotName = "saturation top - bin2", section = 7, subsection = 2, subsubsection = 2, paragraph = 11),
		"top.vis.hsv.s.normalized.histogram.bin.02.12_25" = list(plotName = "saturation top - bin2 (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 12),
		"top.vis.hsv.s.histogram.bin.03.25_38" = list(plotName = "saturation top - bin3", section = 7, subsection = 2, subsubsection = 2, paragraph = 13),
		"top.vis.hsv.s.normalized.histogram.bin.03.25_38" = list(plotName = "saturation top - bin3 (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 14),
		"top.vis.hsv.s.histogram.bin.04.38_51" = list(plotName = "saturation top - bin4", section = 7, subsection = 2, subsubsection = 2, paragraph = 15),
		"top.vis.hsv.s.normalized.histogram.bin.04.38_51" = list(plotName = "saturation top - bin4 (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 16),
		"top.vis.hsv.s.histogram.bin.05.51_63" = list(plotName = "saturation top - bin5", section = 7, subsection = 2, subsubsection = 2, paragraph = 17),
		"top.vis.hsv.s.normalized.histogram.bin.05.51_63" = list(plotName = "saturation top - bin5 (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 18),
		"top.vis.hsv.s.histogram.bin.06.63_76" = list(plotName = "saturation top - bin6", section = 7, subsection = 2, subsubsection = 2, paragraph = 19),
		"top.vis.hsv.s.normalized.histogram.bin.06.63_76" = list(plotName = "saturation top - bin6 (zoom corrected)", section = 7, subsection = 2, subsubsection = 2, paragraph = 20),
		
		"top.vis.hsv.v.average" = list(plotName = "brightness top - average", section = 7, subsection = 2, subsubsection = 3, paragraph = 1),
		"top.vis.hsv.v.normalized.average" = list(plotName = "brightness top - average (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 2),
		"top.vis.hsv.v.stddev" = list(plotName = "brightness top - stddev", section = 7, subsection = 2, subsubsection = 3, paragraph = 3),
		"top.vis.hsv.v.normalized.stddev" = list(plotName = "brightness top - stddev (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 4),
		"top.vis.hsv.v.skewness" = list(plotName = "brightness top - skewness", section = 7, subsection = 2, subsubsection = 3, paragraph = 5),
		"top.vis.hsv.v.normalized.skewness" = list(plotName = "brightness top - skewness (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 6),
		"top.vis.hsv.v.kurtosis" = list(plotName = "brightness top - kurtosis", section = 7, subsection = 2, subsubsection = 3, paragraph = 7),
		"top.vis.hsv.v.normalized.kurtosis" = list(plotName = "brightness top - kurtosis (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 8),
		"top.vis.hsv.v.histogram.bin.01.0_12" = list(plotName = "brightness top - bin1", section = 7, subsection = 2, subsubsection = 3, paragraph = 9),
		"top.vis.hsv.v.normalized.histogram.bin.01.0_12" = list(plotName = "brightness top - bin1 (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 10),
		"top.vis.hsv.v.histogram.bin.02.12_25" = list(plotName = "brightness top - bin2", section = 7, subsection = 2, subsubsection = 3, paragraph = 11),
		"top.vis.hsv.v.normalized.histogram.bin.02.12_25" = list(plotName = "brightness top - bin2 (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 12),
		"top.vis.hsv.v.histogram.bin.03.25_38" = list(plotName = "brightness top - bin3", section = 7, subsection = 2, subsubsection = 3, paragraph = 13),
		"top.vis.hsv.v.normalized.histogram.bin.03.25_38" = list(plotName = "brightness top - bin3 (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 14),
		"top.vis.hsv.v.histogram.bin.04.38_51" = list(plotName = "brightness top - bin4", section = 7, subsection = 2, subsubsection = 3, paragraph = 15),
		"top.vis.hsv.v.normalized.histogram.bin.04.38_51" = list(plotName = "brightness top - bin4 (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 16),
		"top.vis.hsv.v.histogram.bin.05.51_63" = list(plotName = "brightness top - bin5", section = 7, subsection = 2, subsubsection = 3, paragraph = 17),
		"top.vis.hsv.v.normalized.histogram.bin.05.51_63" = list(plotName = "brightness top - bin5 (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 18),
		"top.vis.hsv.v.histogram.bin.06.63_76" = list(plotName = "brightness top - bin6", section = 7, subsection = 2, subsubsection = 3, paragraph = 19),
		"top.vis.hsv.v.normalized.histogram.bin.06.63_76" = list(plotName = "brightness top - bin6 (zoom corrected)", section = 7, subsection = 2, subsubsection = 3, paragraph = 20),	
		
		"side.fluo.intensity.average (relative)" = list(plotName = "side fluo intensity (relative intensity/pixel)", section = 8, subsection = 1, subsubsection = 1),
		"top.fluo.intensity.average (relative / pix)" = list(plotName = "top fluo intensity (relative intensity/pixel)", section = 8, subsection = 2, subsubsection = 1),
		
		"side.nir.intensity.average (relative)" = list(plotName = "side nir intensity (relative intensity/pixel)", section = 9, subsection = 1, subsubsection = 1),
		"top.nir.intensity.average (relative / pix)" = list(plotName = "top nir intensity (relative intensity/pixel)", section = 9, subsection = 2, subsubsection = 1),
		
		"side.nir.skeleton.intensity.average (relative)" = list(plotName = "side skeleton nir intensity (relative intensity/pixel)", section = 9, subsection = 1, subsubsection = 2),
		"top.nir.skeleton.intensity.average" = list(plotName = "top skeleton nir intensity (relative intensity/pixel)", section = 9, subsection = 2, subsubsection = 2),	
		
		"side.ir.intensity.average" = list(plotName = "side ir intensity", section = 10, subsection = 1),
		"top.ir.intensity.average" = list(plotName = "top ir intensity", section = 10, subsection = 1),
				
		"side.leaf.count.median (leafs)" = list(plotName = "number of leafs", section = 11, subsection = 1), 
		"side.leaf.length.sum.norm.max (mm)" = list(plotName = "length of leafs plus stem (mm)", section = 11, subsection = 2),
		"side.bloom.count (tassel)" = list(plotName = "number of tassel florets", section = 11, subsection = 3), 
				
		"side.nir.wetness.plant_weight_drought_loss" = list(plotName = "weighted loss through drought stress (side)", section = 12, subsection = 1), 
		"top.nir.wetness.plant_weight_drought_loss" = list(plotName = "weighted loss through drought stress (top)",section = 12, subsection = 2),
		
		"side.nir.wetness.average (percent)" = list(plotName = "Average wetness of side image", section = 12, subsection = 3), 
		"top.nir.wetness.average (percent)" = list(plotName = "Average wetness of top image", section = 12, subsection = 4), 
		
		"side.hull.area.norm (mm^2)" = list(plotName = "side area of convex hull (zoom corrected) (mm^2)", section = 13, subsection = 1, subsubsection = 1),
		"side.hull.area (px)" = list(plotName = "side area of convex hull (px)", section = 13, subsection = 1, subsubsection = 2),
		"side.hull.circumcircle.d (px)" = list(plotName = "side circumcircle diameter (px)", section = 13, subsection = 1, subsubsection = 10),
		"side.hull.pc1.norm" = list(plotName = "side maximum extension (zoom corrected) (mm)", section = 13, subsection = 1, subsubsection = 3),
		"side.hull.pc1" = list(plotName = "side maximum extension (px)", section = 13, subsection = 1, subsubsection = 4),
		"side.hull.pc2.norm" = list(plotName = "opposite direction of the side maximum extension (zoom corrected) (mm)", section = 13, subsection = 1, subsubsection = 5),
		"side.hull.pc2" = list(plotName = "opposite direction of the side maximum extension (px)", section = 13, subsection = 1, subsubsection = 6),
		"side.hull.fillgrade (percent)" = list(plotName = "fillgrade of side convex hull (%)", section = 13, subsection = 1, subsubsection = 7),		

		"side.compactness.16 (relative)" = list(plotName = "side compactness (16-inf)", section = 13, subsection = 1, subsubsection = 8),
		"side.compactness.01 (relative)" = list(plotName = "side compactness (0-1)", section = 13, subsection = 1, subsubsection = 8),
		"side.hull.circularity (relative)" = list(plotName = "side circularity", section = 13, subsection = 1, subsubsection = 9),
		
		"top.hull.area.norm (mm^2)" = list(plotName = "top area of convex hull (zoom corrected) (mm^2)", section = 13, subsection = 2, subsubsection = 1),
		"top.hull.area (px)" = list(plotName = "top area of convex hull (px)", section = 13, subsection = 2, subsubsection = 2),	
		"top.hull.circumcircle.d (px)" = list(plotName = "top circumcircle diameter (px)", section = 13, subsection = 2, subsubsection = 10),
		"top.hull.pc1.norm" = list(plotName = "top maximum extension (zoom corrected) (mm)", section = 13, subsection = 2, subsubsection = 3),
		"top.hull.pc1" = list(plotName = "top maximum extension (px)", section = 13, subsection = 2, subsubsection = 4),
		"top.hull.pc2.norm" = list(plotName = "opposite direction of the top maximum extension (zoom corrected) (mm)", section = 13, subsection = 2, subsubsection = 5),
		"top.hull.pc2" = list(plotName = "opposite direction of the top maximum extension (px)", section = 13, subsection = 2, subsubsection = 6),
		"top.hull.fillgrade (percent)" = list(plotName = "fillgrade of top convex hull (%)", section = 13, subsection = 2, subsubsection = 7),
		
		"top.compactness.16 (relative)" = list(plotName = "top compactness (16-inf)", section = 13, subsection = 2, subsubsection = 8),
		"top.compactness.01 (relative)" = list(plotName = "top compactness (0-1)", section = 13, subsection = 2, subsubsection = 8),
		"top.hull.circularity (relative)" = list(plotName = "top circularity", section = 13, subsection = 2, subsubsection = 9),
		
		
		"side.vis.stress.hue.brown2green" = list(plotName = "ratio between the brown and green side values", section = 14, subsection = 1, subsubsection = 1),
		"side.vis.stress.hue.red2green" = list(plotName = "ratio between the red and green side values", section = 14, subsection = 1, subsubsection = 2),
		"side.vis.stress.hue.yellow2green" = list(plotName = "ratio between the yellow and green side values", section = 14, subsection = 1, subsubsection = 3),
		
		"top.vis.stress.hue.brown2green" = list(plotName = "ratio between the brown and green top values", section = 14, subsection = 2, subsubsection = 1),
		"top.vis.stress.hue.red2green" = list(plotName = "ratio between the red and green top values", section = 14, subsection = 2, subsubsection = 2),
		"top.vis.stress.hue.yellow2green" = list(plotName = "ratio between the yellow and green top values", section = 14, subsection = 2, subsubsection = 3),
		
		"side.leaf.curling.n" = list(plotName = "number of leaf segments (> 100 px)", section = 15, subsection = 1, subsubsection = 1),
		"side.leaf.curling.frequency.avg" = list(plotName = "Average frequency of leaf curling waves", section = 15, subsection = 1, subsubsection = 2),
		"side.leaf.curling.frequency.stddev" = list(plotName = "Standard deviation of frequencies of leaf curling waves", section = 15, subsection = 1, subsubsection = 3),
		"side.leaf.curling.amplitude.avg" = list(plotName = "Average amplitude of leaf curling waves", section = 15, subsection = 1, subsubsection = 4),
		"side.leaf.curling.amplitude.stddev" = list(plotName = "– Standard deviation of amplitudes of leaf curling waves", section = 15, subsection = 1, subsubsection = 5),
		
		"top.leaf.curling.n" = list(plotName = "number of leaf segments (> 100 px)", section = 15, subsection = 2, subsubsection = 1),
		"top.leaf.curling.frequency.avg" = list(plotName = "Average frequency of leaf curling waves", section = 15, subsection = 2, subsubsection = 2),
		"top.leaf.curling.frequency.stddev" = list(plotName = "Standard deviation of frequencies of leaf curling waves", section = 15, subsection = 2, subsubsection = 3),
		"top.leaf.curling.amplitude.avg" = list(plotName = "Average amplitude of leaf curling waves", section = 15, subsection = 2, subsubsection = 4),
		"top.leaf.curling.amplitude.stddev" = list(plotName = "– Standard deviation of amplitudes of leaf curling waves", section = 15, subsection = 2, subsubsection = 5)
		
#		"side.fluo.intensity.chlorophyl.average (relative)" = list(plotName = "chlorophyll intensity (relative intensity/pixel)", section = 2), 
#		"side.fluo.intensity.phenol.average (relative)" = list(plotName = "fluorescence intensity (relative intensity/pixel)", section = 2), 													
#		 
#		"volume.iap.wue" = list(plotName = "volume based water use efficiency", section = 2),
#
#		"side.vis.hue.average" = list(plotName = "side visible hue average value", section = 2),
#		"top.vis.hue.average" = list(plotName = "top visible hue average value", section = 2),	
		
)

