# TODO: Add comment
# 
# Author: Entzian
###############################################################################

CLEAR.PAGE 			<- "\\clearpage"
CLEAR.PAGE.OWN 		<- "\\ownClearPage"
CLEAR.PAGE.OWN.SUB 	<- "\\ownClearPageSub"
CLEAR.PAGE.NO		<- ""

RESET.PAGE			<- "\\resetClear"
RESET.PAGE.OWN		<- "\\resetClearSub"
RESET.PAGE.OWN.SUB 	<- ""
RESET.PAGE.NO		<- ""

BEGIN.ITEM <- paste("\\begin{itemize}", NEWLINE.TEX, sep="")
LINE.ITEM <- "\\item"
END.ITEM <- paste("\\end{itemize}", NEWLINE.TEX, sep="")

BEGIN.TINY <- paste("\\begin{tiny}", NEWLINE.TEX, sep="")
END.TINY <- paste("\\end{tiny}", NEWLINE.TEX, sep="")
BEGIN.URL <- "\\url{"
END.URL <- "}"
SEPARATOR.ITEM <- " "

sectionMappingList <- list(
		section = list(
				"1" = list(
						newSection = 1,
						title = "Weights and water consumption",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				),
						
				"30" = list(
						newSection = 3,
						title = "Properties - Overview",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				),
				
				"2" = list(
						takeRestValuesFrom = "30"
				),
				
				"40" = list(
						takeRestValuesFrom = "30"
				),
				
				"70" = list(
						takeRestValuesFrom = "30"
				),
				
				"3" = list(
						newSection = 4,
						title = "Modelling of stress",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				),
				
				"20" = list(
						newSection = 5,
						title = "Modelling of stress",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Digital Biomass based on the side.area and top.area", NEWLINE.TEX,
								LINE.ITEM, "Unit: $pixel^3$", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"4" = list(
						takeRestValuesFrom = "20"
				),
				
				
				"21" = list(
						newSection = 6,
						title = "General growth related plant properties",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				),
				
				"5" = list(
						takeRestValuesFrom = "21"
				),
				
				"6" = list(
						newSection = 7,
						title = "Relative changes per day",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Presented values in relative dependence per day.", NEWLINE.TEX,
								LINE.ITEM, "Unit: $\\%/day$", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"7" = list(
						newSection = 8,
						title = "Visible light color analysis",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "In this section the plant pixels color shade values are investigated in detail. 
											The color components color hue, color saturation or brightness are analyzed independently.", NEWLINE.TEX,
								LINE.ITEM, "The following statistical analysis is performed for each color component of
											the plant pixel values: average, standard deviation, skewness and kurtosis.", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"60" = list(
						takeRestValuesFrom = "7"
				),
				
				"8" = list(
						newSection = 9,
						title = "Fluorescence activity",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Average and histogram of observed fluorescence colors (Fluo)", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"61" = list(
						takeRestValuesFrom = "8"
				),
				
				"9" = list(
						newSection = 10,
						title = "Near-infrared intensity",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Average intensity of near infrared (NIR)", NEWLINE.TEX,
								LINE.ITEM, "Represents the water content of the plant", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"62" = list(
						takeRestValuesFrom = "9"
				),
				
				"10" = list(
						newSection = 11,
						title = "Infrared intensity",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Average intensity of infrared (IR)", NEWLINE.TEX,
								LINE.ITEM, "Represents the relative temperature of the plant in comparison to the background.
											The higher the value, the colder the leafs. High values may be the result of high transpiration. For temperature measurements, the upper 50 percent of
											the background temperature values are used. If there is a blue rubber mat, the result of this calculation scheme is, that the warmer temperature of the
											rubber mat is used for comparison, not the possibly colder soil (it may be colder because of transpiration).", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"63" = list(
						takeRestValuesFrom = "10"
				),
				
				"11" = list(
						newSection = 12,
						title = "Plant structures",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				),
				
				"12" = list(
						newSection = 13,
						title = "Wetness",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Near-infrared analysis", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"13" = list(
						newSection = 14,
						title = "Convex hull",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEWLINE.TEX,
										"\"In mathematics, the convex hull or convex envelope for a set of points X in a
											real vector space V (for example, usual 2- or 3-dimensional space) is the
											minimal convex set containing X. When the set X is a finite subset of the
											plane, we may imagine stretching a rubber band so that it surrounds the entire
											set X and then releasing it, allowing it to contract; when it becomes taut, it
											encloses the convex hull of X.\"", NEWLINE.TEX,
										BEGIN.TINY, NEWLINE.TEX,
										"Wikipedia contributors, \"Convex hull\",", NEWLINE.TEX,
										"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
										paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Convex_hull&oldid=482955324", END.URL, sep=""), NEWLINE.TEX,
										"(accessed March 30, 2012).", END.TINY,
										sep = SEPARATOR.ITEM)
				),
				
				"stress" = list(
						newSection = 98,
						title = "Stress",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				),	
				
				"appendix" = list(
						newSection = 99,
						title = "Appendix",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				)
		),
		subsection = list(
				
				"1.1" = list(
						newSection = 1,
						title = "Weights (before and after watering",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = paste(BEGIN.ITEM,
									 LINE.ITEM, "Weight before and after watering.", NEWLINE.TEX,
									 LINE.ITEM, "Colnum name: Weight A (g), Weight B (g)", NEWLINE.TEX,
									 LINE.ITEM, "Unit: g", NEWLINE.TEX,
									 END.ITEM,
									 sep = SEPARATOR.ITEM)
				),
				
				"1.2" = list(
						newSection = 2,
						title = "Daily watering amounts",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = paste(BEGIN.ITEM,
									 LINE.ITEM, "The sum of the watering amount of the day.", NEWLINE.TEX,
									 LINE.ITEM, "Colnum name: Water (sum of day)", NEWLINE.TEX,
									 LINE.ITEM, "Unit: g", NEWLINE.TEX,
									 END.ITEM,
									 sep = SEPARATOR.ITEM)
				),		
				
				"30.1" = list(
						newSection = 1,
						title = "Growth parameters, fluorescence, near-infrared and visible light color (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"30.2" = list(
						title = "Growth parameters, fluorescence, near-infrared and visible light color",
						takeRestValuesFrom = "30.1"
				),
				
				"70.1" = list(
						newSection = 2,
						title = "Difference within special descriptors (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"70.2" = list(
						title = "Difference within special descriptors",
						takeRestValuesFrom = "70.1"
				),
				
				"2.1" = list(
						newSection = 2,
						title = "Zoom changes",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"40.1" = list(
						newSection = 4,
						title = "Stress ratio",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"3.1" = list(
						newSection = 1,
						title = "Before stress phase",
						typOfReset = RESET.PAGE.OWN,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"3.2" = list(
						newSection = 2,
						title = "Stress period",
						typOfReset = RESET.PAGE.OWN,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"3.3" = list(
						newSection = 3,
						title = "Recovery periode",
						typOfReset = RESET.PAGE.OWN,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"20.1" = list(
						newSection = 1,
						title = "IAP formula",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
									LINE.ITEM, "Equation: $Biomass_{IAP}=\\sqrt{side.area_{average}^{2}*top.area}$", NEWLINE.TEX,
									LINE.ITEM, "Column name: volume.iap", NEWLINE.TEX,
									END.ITEM,
									sep = SEPARATOR.ITEM)
				),
				
				"4.1" = list(
						takeRestValuesFrom = "20.1"
				),
				
				"20.2" = list(
						newSection = 2,
						title = "LemnaTec formula",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Equation: $Biomass_{LemnaTec}=\\sqrt{side.area_{0^{\\circ}}*side.area_{90^{\\circ}}*top.area}$", NEWLINE.TEX,
								LINE.ITEM, "Column name: volume.lt", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"4.2" = list(
						takeRestValuesFrom = "20.2"
				),
				
				"4.3" = list(
						newSection = 3,
						title = "Water use efficiency",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Ratio of daily plant growth, determined by increasing projected side
											area and/or digital biomass, and water ussaged per day.", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"21.1" = list(
						newSection = 1,
						title = "Height (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Plant height (mm) (normalized to distance of left and right marker)", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.height.norm", NEWLINE.TEX,
								LINE.ITEM, "Unit: mm", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"5.1" = list(
						takeRestValuesFrom = "21.1"
				),
				
				"21.2" = list(
						title = "Height",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Plant height (px)", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.height", NEWLINE.TEX,
								LINE.ITEM, "Unit: px", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "21.1"
				),
				
				"5.2" = list(
						takeRestValuesFrom = "21.2"
				),
				
				
				"21.3" = list(
						newSection = 2,
						title = "Width (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Plant width (mm) (normalized to distance of left and right marker)", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.width.norm", NEWLINE.TEX,
								LINE.ITEM, "Unit: mm", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"5.3" = list(
						takeRestValuesFrom = "21.3"
				),
				
				"21.4" = list(
						title = "Width",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Plant width (px)", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.width", NEWLINE.TEX,
								LINE.ITEM, "Unit: px", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "21.3"
				),
				
				"5.4" = list(
						takeRestValuesFrom = "21.4"
				),
				
				
				"21.5" = list(
						newSection = 3,
						title = "Projected side area (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of foreground pixels from side camera (normalized to distance of left and right marker)", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.area.norm", NEWLINE.TEX,
								LINE.ITEM, "Unit: $mm^2$", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"5.5" = list(
						takeRestValuesFrom = "21.5"
				),
				
				"21.6" = list(
						title = "Projected side area",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of foreground pixels", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.area", NEWLINE.TEX,
								LINE.ITEM, "Unit: px", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "21.5"
				),
				
				"5.6" = list(
						takeRestValuesFrom = "21.6"
				),
				
				"21.7" = list(
						newSection = 4,
						title = "Projected top area (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of foreground pixels from top camera (normalized to distance of left and right marker)", NEWLINE.TEX,
								LINE.ITEM, "Column name: top.area.norm", NEWLINE.TEX,
								LINE.ITEM, "Unit: $mm^2$", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"5.7" = list(
						takeRestValuesFrom = "21.7"
				),
				
				"21.8" = list(
						title = "Projected top area",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of foreground pixels", NEWLINE.TEX,
								LINE.ITEM, "Column name: top.area", NEWLINE.TEX,
								LINE.ITEM, "Unit: px", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "21.7"
				),
				
				"5.8" = list(
						takeRestValuesFrom = "21.8"
				),
				
				"6.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Projected side area",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Relative growth of number of foreground pixels from side camera", NEWLINE.TEX,
								LINE.ITEM, "Colnum name: side.area.relative", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"6.2" = list(
						newSection = 2,
						title = "Height",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Relative growth of plant height in percent (normalized to distance of left and right marker)", NEWLINE.TEX,
								LINE.ITEM, "Colnum name: side.height.norm.relative", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "6.1"
				),
				
				"6.3" = list(
						newSection = 3,
						title = "Width",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Relative plant width growth in percent (normalized to distance of left and right marker)", NEWLINE.TEX,
								LINE.ITEM, "Colnum name: side.width.norm.relative", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "6.1"
				),
				
				"6.4" = list(
						newSection = 4,
						title = "Projected top area",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Relative growth of number of foreground pixels from top camera", NEWLINE.TEX,
								LINE.ITEM, "Colnum name: top.area.relative", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "6.1"
				),
				
				"6.5" = list(
						newSection = 5,
						title = "Digital biomass (IAP formula)",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Digital biomass growth in percent (per day) based on the side.area and top.area observed from the visible light camera.", NEWLINE.TEX,
								LINE.ITEM, "Colnum name: volume.iap.relative", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "6.1"
				),

				"7.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Side",
						text = ""
				),
				
				"60.1" = list(
						takeRestValuesFrom = "7.1"
				),
				
				"7.2" = list(
						newSection = 2,
						title = "Top",
						takeRestValuesFrom = "7.1"
						
				),
				
				"60.2" = list(
						takeRestValuesFrom = "7.2"
				),
				
				"8.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Average fluorescence activity intensity",
						text = ""
				),
				
				"61.1" = list(
						newSection = 2,
						title = "Fluorescence spectra side view (zoom corrected)",
						takeRestValuesFrom = "8.1"
				),
				
				"61.2" = list(
						title = "Fluorescence spectra side view",
						takeRestValuesFrom = "61.1"
				),
				
				"61.3" = list(
						newSection = 3,
						title = "Fluorescence spectra top view (zoom corrected)",
						takeRestValuesFrom = "8.1"
				),
				
				"61.4" = list(
						title = "Fluorescence spectra top view",
						takeRestValuesFrom = "61.3"
				),
				
				"9.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Average near-infrared intensity",
						text = ""
				),
				
				"62.1" = list(
						newSection = 2,
						title = "Intensity histogram side view (zoom corrected)",
						takeRestValuesFrom = "9.1"
				),
				
				"62.2" = list(
						title = "Intensity histogram side view",
						takeRestValuesFrom = "62.1"
				),
				
				"62.3" = list(
						newSection = 3,
						title = "Intensity histogram top view (zoom corrected)",
						takeRestValuesFrom = "9.1"
				),
				
				"62.4" = list(
						title = "Intensity histogram top view",
						takeRestValuesFrom = "62.3"
				),
				
				"10.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Average infrared intensity",
						text = ""
				),
				
				"63.1" = list(
						newSection = 2,
						title = "Intensity histogram side view (zoom corrected)",
						takeRestValuesFrom = "10.1"
				),
				
				"63.2" = list(
						title = "Intensity histogram side view",
						takeRestValuesFrom = "63.1"
				),
				
				"63.3" = list(
						newSection = 3,
						title = "Intensity histogram top view (zoom corrected)",
						takeRestValuesFrom = "10.1"
				),
				
				"63.4" = list(
						title = "Intensity histogram top view",
						takeRestValuesFrom = "63.3"
				),
				
				"11.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Number of leafs",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of leafs-tips", NEWLINE.TEX,
								LINE.ITEM, "Colum name: side.leaf.count.median", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),		
				
				"11.2" = list(
						newSection = 2,
						title = "Leaf lengths",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Length of all leafs plus stem", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.leaf.length.sum.norm.max", NEWLINE.TEX,
								LINE.ITEM, "Unit: mm", NEWLINE.TEX,
								LINE.ITEM, "Hint: the yellow line in the image", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "11.1"
				),
				
				"11.3" = list(
						newSection = 3,
						title = "Flower detection",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of tassel florets", NEWLINE.TEX,
								LINE.ITEM, "Colum name: side.bloom.count", NEWLINE.TEX,
								LINE.ITEM, "Hint: the number of blue retangles in the image", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "11.1"
				),
				
				"12.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Average wetness of side image",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Average wetness of the plants from NIR side camera", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.nir.wetness.av", NEWLINE.TEX,
								LINE.ITEM, "Unit: \\%", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"12.2" = list(
						newSection = 2,
						title = "Average wetness of top image",
						takeRestValuesFrom = "12.1"	
				),
				
				"12.3" = list(
						newSection = 3,
						title = "Weighted loss through drought stress - side image",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Number of foreground pixels from NIR side camera minus the weighted value of the plant", NEWLINE.TEX,
								LINE.ITEM, "weightOfPlant = fully wet: 1 unit, fully dry: 1/7 unit", NEWLINE.TEX,
								LINE.ITEM, "Column name: side.nir.wetness.plant\\_weight\\_drought\\_loss", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "12.1"
				),
				
				"12.4" = list(
						newSection = 4,
						title = "Weighted loss through drought stress - top image",
						takeRestValuesFrom = "12.2"
				),
				
				"13.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.OWN,
						typOfClear = CLEAR.PAGE.OWN,
						title = "Side",
						text = ""
				),
				
				"13.2" = list(
						newSection = 2,
						title = "Top",
						takeRestValuesFrom = "13.1"
				)			
				
				
		),
		subsubsection = list(
								
				"3.1.1" = list(
						newSection = 1,
						title = "ratio",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"3.1.2" = list(
						newSection = 2,
						title = "slope",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
		
				"3.2.1" = list(
						newSection = 1,
						title = "Starting point",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"3.2.2" = list(
						newSection = 2,
						title = "maximal stress impact",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"3.2.3" = list(
						newSection = 3,
						title = "slope",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"3.3.1" = list(
						newSection = 1,
						title = "Starting point",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"3.3.2" = list(
						newSection = 2,
						title = "slope",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"4.3.1" = list(
						newSection = 1,
						title = "Based on digital biomass",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Colnum name: volume.iap.wue", NEWLINE.TEX,
								LINE.ITEM, "Unit: $pixel^3/g$", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"4.3.2" = list(
						newSection = 2,
						title = "Based on projected side area",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Colnum name: side.area.avg.wue", NEWLINE.TEX,
								LINE.ITEM, "Unit: $pixel^2/g$", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"7.1.1" = list(
						newSection = 1,
						title = "Color shade",
						typOfReset = RESET.PAGE.OWN,
						typOfClear = CLEAR.PAGE.OWN,
						text = ""
				),
				
				"60.1.1" = list(
						takeRestValuesFrom = "7.1.1"
				),
				
				"7.1.2" = list(
						newSection = 2,
						title = "Saturation",
						takeRestValuesFrom = "7.1.1"
				),
				
				"60.1.2" = list(
						takeRestValuesFrom = "7.1.2"
				),
				
				"7.1.3" = list(
						newSection = 3,
						title = "Brightness",
						takeRestValuesFrom = "7.1.1"
				),
				
				"60.1.3" = list(
						takeRestValuesFrom = "7.1.3"
				),
				
				"7.2.1" = list(
						takeRestValuesFrom = "7.1.1"
				),
				
				"60.2.1" = list(
						takeRestValuesFrom = "7.2.1"
				),
				
				"7.2.2" = list(
						takeRestValuesFrom = "7.1.2"
				),
				
				"60.2.2" = list(
						takeRestValuesFrom = "7.2.2"
				),
				
				"7.2.3" = list(
						takeRestValuesFrom = "7.1.3"
				),
				
				"60.2.3" = list(
						takeRestValuesFrom = "7.2.3"
				),
				
				"13.1.1" = list(
						newSection = 1,
						title = "Area (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Area which is enclosed of the convex hull", NEWLINE.TEX,
								LINE.ITEM, "Unit: px", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM)
				),
				
				"13.1.2" = list(
						title = "Area",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.3" = list(
						newSection = 3,
						title = "PC1 - maximum distance (zoom corrected)",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Maximum extension of the plant", NEWLINE.TEX,
								LINE.ITEM, "Unit: mm", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.4" = list(
						title = "PC1 - maximum distance",
						takeRestValuesFrom = "13.1.3"
				),
				
				"13.1.5" = list(
						newSection = 4,
						title = "PC2 - Opposite direction maxmium distance (zoom corrected)",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Opposite direction of the maximum extension of the plant", NEWLINE.TEX,
								LINE.ITEM, "Unit: mm", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.6" = list(
						title = "PC2 - Opposite direction maxmium distance",
						takeRestValuesFrom = "13.1.5"
				),
				
				"13.1.7" = list(
						newSection = 5,
						title = "Fillgrade",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "Ratio of the area of the whole convex hull and the area of the plant (green pixel)", NEWLINE.TEX,
								LINE.ITEM, "Unit: none", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.8" = list(
						newSection = 6,
						title = "Compactness",
						text = paste(NEWLINE.TEX,
								"\"The compactness measure of a shape, sometimes called the shape factor, is a
									numerical quantity representing the degree to which a shape is compact.
									\\ldots A common compactness measure is the Isoperimetric quotient, the ratio
									of the area of the shape to the area of a circle (the most compact shape) having the same perimeter.\"", NEWLINE.TEX,
								BEGIN.TINY, NEWLINE.TEX,
								"Wikipedia contributors, \"Compactness measure of a shape\",", NEWLINE.TEX,
								"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
								paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Compactness_measure_of_a_shape&oldid=485591387", END.URL, sep=""), NEWLINE.TEX,
								"(accessed June 25, 2012).", END.TINY,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.9" = list(
						newSection = 7,
						title = "Circularity",
						text = paste(NEWLINE.TEX,
								"\"Roundness is the measure of the sharpness of a particle's edges and corners.\"", NEWLINE.TEX,
								BEGIN.TINY, NEWLINE.TEX,
								"Wikipedia contributors, \"Roundness (object)\",", NEWLINE.TEX,
								"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
								paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Roundness_(object)&oldid=487251484", END.URL, sep=""), NEWLINE.TEX,
								"(accessed June 25, 2012).", END.TINY,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.10" = list(
						newSection = 2,
						title = "Circumcircle diameter",
						text = paste(BEGIN.ITEM,
								LINE.ITEM, "The minimum diameter of a circle surrounding the plant.", NEWLINE.TEX,
								LINE.ITEM, "Unit: px", NEWLINE.TEX,
								END.ITEM,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.2.1" = list(
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.2.2" = list(
						takeRestValuesFrom = "13.1.2"
				),
				
				"13.2.3" = list(
						takeRestValuesFrom = "13.1.3"
				),
				
				"13.2.4" = list(
						takeRestValuesFrom = "13.1.4"
				),
				
				"13.2.5" = list(
						takeRestValuesFrom = "13.1.5"
				),
				
				"13.2.6" = list(
						takeRestValuesFrom = "13.1.6"
				),
				
				"13.2.7" = list(
						takeRestValuesFrom = "13.1.7"
				),
				
				"13.2.8" = list(
						takeRestValuesFrom = "13.1.8"
				),
				
				"13.2.9" = list(
						takeRestValuesFrom = "13.1.9"
				),
				
				"13.2.10" = list(
						takeRestValuesFrom = "13.1.10"
				)
				
				
		),
		paragraph = list(
				
				"3.1.1.1" = list(
						newSection = 1,
						title = "Side area (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"3.1.1.2" = list(
						newSection = 2,
						title = "PC2 (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"3.1.1.3" = list(
						newSection = 3,
						title = "NIR intensity",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"3.1.2.1" = list(
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.1.2.2" = list(
						takeRestValuesFrom = "3.1.1.2"
				),
				
				"3.1.2.3" = list(
						takeRestValuesFrom = "3.1.1.3"
				),
				
				"3.2.1.1" = list(
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.2.1.2" = list(
						takeRestValuesFrom = "3.1.1.2"
				),
				
				"3.2.1.3" = list(
						takeRestValuesFrom = "3.1.1.3"
				),
				
				"3.2.2.1" = list(
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.2.2.2" = list(
						takeRestValuesFrom = "3.1.1.2"
				),
				
				"3.2.2.3" = list(
						takeRestValuesFrom = "3.1.1.3"
				),
				
				"3.3.1.1" = list(
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.3.1.2" = list(
						takeRestValuesFrom = "3.1.1.2"
				),
				
				"3.3.1.3" = list(
						takeRestValuesFrom = "3.1.1.3"
				),
				
				"3.3.2.1" = list(
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.3.2.2" = list(
						takeRestValuesFrom = "3.1.1.2"
				),
				
				"3.3.2.3" = list(
						takeRestValuesFrom = "3.1.1.3"
				),
				
				"7.1.1.1" = list(
						newSection = 1,
						title = "Average hue",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.OWN.SUB,
						text = ""
				),
				
				"7.1.1.2" = list(
						title = "Average hue (zoom corrected)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.3" = list(
						newSection = 2,
						title = "Standard deviation",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.4" = list(
						title = "Standard deviation (zoom corrected)",
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"7.1.1.5" = list(
						newSection = 3,
						title = "Skewness",
						text = paste(
								NEWLINE.TEX,
								"The skewness can be described as following:", NEWLINE.TEX, 
								NEWLINE.TEX,
								"\"In probability theory and statistics, skewness is a measure of the
								asymmetry of the probability distributionof a real-valued random variable.
								The skewness value can be positive or negative, or even undefined.
								Qualitatively, a negative skew indicates that the tail on the left side of
								the probability density function islonger than the right side and the bulk
								of the values (possibly including the median) lie to the right of the mean.
								A positive skew indicates that the tail on the right side is longer than the
								left side and the bulk of the values lie to the left of the mean. A zero
								value indicates that the values are relatively evenly distributed on both
								sides of the mean, typically but not necessarily implying a symmetric
								distribution.\"", NEWLINE.TEX,
								BEGIN.TINY, NEWLINE.TEX,
								"Wikipedia contributors, \"Skewness\",", NEWLINE.TEX,
								"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
								paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Skewness&oldid=499258725", END.URL, sep=""), NEWLINE.TEX,
								"(accessed June 27, 2012).", END.TINY,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.6" = list(
						title = "Skewness (zoom corrected)",
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"7.1.1.7" = list(
						newSection = 4,
						title = "Kurtosis",
						text = paste(
								NEWLINE.TEX,
								"The term kurtosis is described as following:", NEWLINE.TEX, 
								NEWLINE.TEX,
								"\"In probability theory and statistics, kurtosis (from the Greek word κυρτός,
								kyrtos or kurtos, meaning bulging) is any measure of the \"peakedness\" of the
								probability distribution of a real-valued random variable. In a similar
								way to the concept of skewness, kurtosis is a descriptor of the shape of a
								probability distribution and, just as for skewness, there are different ways
								of quantifying it for a theoretical distribution and corresponding ways of
								estimating it from a sample from a population.
				
								One common measure of kurtosis, originating with Karl Pearson, is based on a
								scaled version of the fourth moment of the data or population, but it has
								been argued that this measure really measures heavy tails, and not
								peakedness. For this measure, higher kurtosis means more of the variance
								is the result of infrequent extreme deviations, as opposed to frequent
								modestly sized deviations.\"", NEWLINE.TEX,
								BEGIN.TINY, NEWLINE.TEX,
								"Wikipedia contributors, \"Kurtosis\",", NEWLINE.TEX,
								"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
								paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Kurtosis&oldid=496203029", END.URL, sep=""), NEWLINE.TEX,
								"(accessed June 27, 2012).", END.TINY,
								sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.8" = list(
						title = "Kurtosis (zoom corrected)",
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"7.1.1.9" = list(
						newSection = 5,
						title = "Hue bin 1 (01-12)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.10" = list(
						title = "Hue bin 1 (01-12) (zoom corrected)",
						takeRestValuesFrom = "7.1.1.9"
				),
				
				"7.1.1.11" = list(
						newSection = 6,
						title = "Hue bin 2 (12-25)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.12" = list(
						title = "Hue bin 2 (12-25) (zoom corrected)",
						takeRestValuesFrom = "7.1.1.11"
				),
				
				"7.1.1.13" = list(
						newSection = 7,
						title = "Hue bin 3 (25-38)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.14" = list(
						title = "Hue bin 3 (25-38) (zoom corrected)",
						takeRestValuesFrom = "7.1.1.13"
				),
				
				"7.1.1.15" = list(
						newSection = 8,
						title = "Hue bin 4 (38-51)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.16" = list(
						title = "Hue bin 4 (38-51) (zoom corrected)",
						takeRestValuesFrom = "7.1.1.15"
				),
				
				"7.1.1.17" = list(
						newSection = 9,
						title = "Hue bin 5 (51-63)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.18" = list(
						title = "Hue bin 5 (51-63) (zoom corrected)",
						takeRestValuesFrom = "7.1.1.17"
				),
				
				"7.1.1.19" = list(
						newSection = 10,
						title = "Hue bin 6 (63-76)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.20" = list(
						title = "Hue bin 6 (63-76) (zoom corrected)",
						takeRestValuesFrom = "7.1.1.19"
				),
				
				"60.1.1.1" = list(
						newSection = 11,
						title = "Color histogram (zoom corrected)",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"60.1.1.2" = list(
						title = "Color histogram",
						takeRestValuesFrom = "60.1.1.1"
				),
				
				
				"7.1.2.1" = list(
						title = "Average saturation",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.2.2" = list(
						title = "Average saturation (zoom corrected)",
						takeRestValuesFrom = "7.1.1.2"
				),
				
				"7.1.2.3" = list(
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"7.1.2.4" = list(
						takeRestValuesFrom = "7.1.1.4"
				),
				
				"7.1.2.5" = list(
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"7.1.2.6" = list(
						takeRestValuesFrom = "7.1.1.6"
				),
				
				"7.1.2.7" = list(
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"7.1.2.8" = list(
						takeRestValuesFrom = "7.1.1.8"
				),
				
				"7.1.2.9" = list(
						takeRestValuesFrom = "7.1.1.9"
				),
				
				"7.1.2.10" = list(
						takeRestValuesFrom = "7.1.1.10"
				),
				
				"7.1.2.11" = list(
						takeRestValuesFrom = "7.1.1.11"
				),
				
				"7.1.2.12" = list(
						takeRestValuesFrom = "7.1.1.12"
				),
				
				"7.1.2.13" = list(
						takeRestValuesFrom = "7.1.1.13"
				),
				
				"7.1.2.14" = list(
						takeRestValuesFrom = "7.1.1.14"
				),
				
				"7.1.2.15" = list(
						takeRestValuesFrom = "7.1.1.15"
				),
				
				"7.1.2.16" = list(
						takeRestValuesFrom = "7.1.1.16"
				),
				
				"7.1.2.17" = list(
						takeRestValuesFrom = "7.1.1.17"
				),
				
				"7.1.2.18" = list(
						takeRestValuesFrom = "7.1.1.18"
				),
				
				"7.1.2.19" = list(
						takeRestValuesFrom = "7.1.1.19"
				),
				
				"7.1.2.20" = list(
						takeRestValuesFrom = "7.1.1.20"
				),
				
				"60.1.2.1" = list(
						takeRestValuesFrom = "60.1.1.1"
				),
				
				"60.1.2.2" = list(
						takeRestValuesFrom = "60.1.1.2"
				),
				
				"7.1.3.1" = list(
						title = "Average brightness",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.3.2" = list(
						title = "Average brightness (zoom corrected)",
						takeRestValuesFrom = "7.1.1.2"
				),
				
				"7.1.3.3" = list(
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"7.1.3.4" = list(
						takeRestValuesFrom = "7.1.1.4"
				),
				
				"7.1.3.5" = list(
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"7.1.3.6" = list(
						takeRestValuesFrom = "7.1.1.6"
				),
				
				"7.1.3.7" = list(
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"7.1.3.8" = list(
						takeRestValuesFrom = "7.1.1.8"
				),
				
				"7.1.3.9" = list(
						takeRestValuesFrom = "7.1.1.9"
				),
				
				"7.1.3.10" = list(
						takeRestValuesFrom = "7.1.1.10"
				),
				
				"7.1.3.11" = list(
						takeRestValuesFrom = "7.1.1.11"
				),
				
				"7.1.3.12" = list(
						takeRestValuesFrom = "7.1.1.12"
				),
				
				"7.1.3.13" = list(
						takeRestValuesFrom = "7.1.1.13"
				),
				
				"7.1.3.14" = list(
						takeRestValuesFrom = "7.1.1.14"
				),
				
				"7.1.3.15" = list(
						takeRestValuesFrom = "7.1.1.15"
				),
				
				"7.1.3.16" = list(
						takeRestValuesFrom = "7.1.1.16"
				),
				
				"7.1.3.17" = list(
						takeRestValuesFrom = "7.1.1.17"
				),
				
				"7.1.3.18" = list(
						takeRestValuesFrom = "7.1.1.18"
				),
				
				"7.1.3.19" = list(
						takeRestValuesFrom = "7.1.1.19"
				),
				
				"7.1.3.20" = list(
						takeRestValuesFrom = "7.1.1.20"
				),
				
				"60.1.3.1" = list(
						takeRestValuesFrom = "60.1.1.1"
				),
				
				"60.1.3.2" = list(
						takeRestValuesFrom = "60.1.1.2"
				),
				
				"7.2.1.1" = list(
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.2.1.2" = list(
						takeRestValuesFrom = "7.1.1.2"
				),
				
				"7.2.1.3" = list(
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"7.2.1.4" = list(
						takeRestValuesFrom = "7.1.1.4"
				),
				
				"7.2.1.5" = list(
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"7.2.1.6" = list(
						takeRestValuesFrom = "7.1.1.6"
				),
				
				"7.2.1.7" = list(
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"7.2.1.8" = list(
						takeRestValuesFrom = "7.1.1.8"
				),
				
				"7.2.1.9" = list(
						takeRestValuesFrom = "7.1.1.9"
				),
				
				"7.2.1.10" = list(
						takeRestValuesFrom = "7.1.1.10"
				),
				
				"7.2.1.11" = list(
						takeRestValuesFrom = "7.1.1.11"
				),
				
				"7.2.1.12" = list(
						takeRestValuesFrom = "7.1.1.12"
				),
				
				"7.2.1.13" = list(
						takeRestValuesFrom = "7.1.1.13"
				),
				
				"7.2.1.14" = list(
						takeRestValuesFrom = "7.1.1.14"
				),
				
				"7.2.1.15" = list(
						takeRestValuesFrom = "7.1.1.15"
				),
				
				"7.2.1.16" = list(
						takeRestValuesFrom = "7.1.1.16"
				),
				
				"7.2.1.17" = list(
						takeRestValuesFrom = "7.1.1.17"
				),
				
				"7.2.1.18" = list(
						takeRestValuesFrom = "7.1.1.18"
				),
				
				"7.2.1.19" = list(
						takeRestValuesFrom = "7.1.1.19"
				),
				
				"7.2.1.20" = list(
						takeRestValuesFrom = "7.1.1.20"
				),
				
				"60.2.1.1" = list(
						takeRestValuesFrom = "60.1.1.1"
				),
				
				"60.2.1.2" = list(
						takeRestValuesFrom = "60.1.1.2"
				),
				
				"7.2.2.1" = list(
						takeRestValuesFrom = "7.1.2.1"
				),
				
				"7.2.2.2" = list(
						takeRestValuesFrom = "7.1.2.2"
				),
				
				"7.2.2.3" = list(
						takeRestValuesFrom = "7.1.2.3"
				),
				
				"7.2.2.4" = list(
						takeRestValuesFrom = "7.1.2.4"
				),
				
				"7.2.2.5" = list(
						takeRestValuesFrom = "7.1.2.5"
				),
				
				"7.2.2.6" = list(
						takeRestValuesFrom = "7.1.2.6"
				),
				
				"7.2.2.7" = list(
						takeRestValuesFrom = "7.1.2.7"
				),
				
				"7.2.2.8" = list(
						takeRestValuesFrom = "7.1.2.8"
				),
				
				"7.2.2.9" = list(
						takeRestValuesFrom = "7.1.2.9"
				),
				
				"7.2.2.10" = list(
						takeRestValuesFrom = "7.1.2.10"
				),
				
				"7.2.2.11" = list(
						takeRestValuesFrom = "7.1.2.11"
				),
				
				"7.2.2.12" = list(
						takeRestValuesFrom = "7.1.2.12"
				),
				
				"7.2.2.13" = list(
						takeRestValuesFrom = "7.1.2.13"
				),
				
				"7.2.2.14" = list(
						takeRestValuesFrom = "7.1.2.14"
				),
				
				"7.2.2.15" = list(
						takeRestValuesFrom = "7.1.2.15"
				),
				
				"7.2.2.16" = list(
						takeRestValuesFrom = "7.1.2.16"
				),
				
				"7.2.2.17" = list(
						takeRestValuesFrom = "7.1.2.17"
				),
				
				"7.2.2.18" = list(
						takeRestValuesFrom = "7.1.2.18"
				),
				
				"7.2.2.19" = list(
						takeRestValuesFrom = "7.1.2.19"
				),
				
				"7.2.2.20" = list(
						takeRestValuesFrom = "7.1.2.20"
				),
				
				"60.2.2.1" = list(
						takeRestValuesFrom = "60.1.2.1"
				),
				
				"60.2.2.2" = list(
						takeRestValuesFrom = "60.1.2.2"
				),
				
				"7.2.3.1" = list(
						takeRestValuesFrom = "7.1.3.1"
				),
				
				"7.2.3.2" = list(
						takeRestValuesFrom = "7.1.3.2"
				),
				
				"7.2.3.3" = list(
						takeRestValuesFrom = "7.1.3.3"
				),
				
				"7.2.3.4" = list(
						takeRestValuesFrom = "7.1.3.4"
				),
				
				"7.2.3.5" = list(
						takeRestValuesFrom = "7.1.3.5"
				),
				
				"7.2.3.6" = list(
						takeRestValuesFrom = "7.1.3.6"
				),
				
				"7.2.3.7" = list(
						takeRestValuesFrom = "7.1.3.7"
				),
				
				"7.2.3.8" = list(
						takeRestValuesFrom = "7.1.3.8"
				),
				
				"7.2.3.9" = list(
						takeRestValuesFrom = "7.1.3.9"
				),
				
				"7.2.3.10" = list(
						takeRestValuesFrom = "7.1.3.10"
				),
				
				"7.2.3.11" = list(
						takeRestValuesFrom = "7.1.3.11"
				),
				
				"7.2.3.12" = list(
						takeRestValuesFrom = "7.1.3.12"
				),
				
				"7.2.3.13" = list(
						takeRestValuesFrom = "7.1.3.13"
				),
				
				"7.2.3.14" = list(
						takeRestValuesFrom = "7.1.3.14"
				),
				
				"7.2.3.15" = list(
						takeRestValuesFrom = "7.1.3.15"
				),
				
				"7.2.3.16" = list(
						takeRestValuesFrom = "7.1.3.16"
				),
				
				"7.2.3.17" = list(
						takeRestValuesFrom = "7.1.3.17"
				),
				
				"7.2.3.18" = list(
						takeRestValuesFrom = "7.1.3.18"
				),
				
				"7.2.3.19" = list(
						takeRestValuesFrom = "7.1.3.19"
				),
				
				"7.2.3.20" = list(
						takeRestValuesFrom = "7.1.3.20"
				),
				
				"60.2.3.1" = list(
						takeRestValuesFrom = "60.1.3.1"
				),
				
				"60.2.3.2" = list(
						takeRestValuesFrom = "60.1.3.2"
				)			
		)
)