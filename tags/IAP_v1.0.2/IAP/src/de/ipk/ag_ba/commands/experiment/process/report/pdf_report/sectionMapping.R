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

#BEGIN.LOAD.IMAGE <- paste(NEWLINE.TEX, "\\loadImage{", sep="")
BEGIN.LOAD.IMAGE <- paste(NEWLINE.TEX, "\\loadImageCap{", sep="")
MIDDLE.LOAD.IMAGE <- "}{"
END.LOAD.IMAGE <- paste("}", NEWLINE.TEX, sep="")
BEGIN.URL <- "\\url{"
END.URL <- "}"
SEPARATOR.ITEM <- " "

PAR.TEXT <- "\\par"
NO.INDENT.TEXT <- "\\noindent"
NEW.PAR.INDENT <- paste(PAR.TEXT, NO.INDENT.TEXT, sep=SEPARATOR.ITEM)
V.SPACE.TEXT <- "\\vspace{10px}"


FLUO.BIN.PNG <- "fluo_bin.png"
NIR.BIN.PNG <- "nir_bin.png"
HSB.PNG <- "HSB.png"
SECTION.PNG <- "section.png"
LAB.A.B.BIN.PNG <- "lab_a_b_bin.png"
HUE.BIN.PNG <- "hue_bin.png"

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
						text = paste(NEW.PAR.INDENT,
								"The violin plots shows the ratio between the stressed and unstress values. The calculated means are subtracted from one. Results close to \"1\" mean that there are no differences.  
								 When the result is negative it is marked as green area, when it is positive than as gray area.", NEWLINE.TEX, 
								sep = SEPARATOR.ITEM),
						
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
						text = paste(NEW.PAR.INDENT,
								"Following data are calculated:", NEWLINE.TEX,
								BEGIN.ITEM,
								LINE.ITEM, "Quality of the initial data", NEWLINE.TEX,
								LINE.ITEM, "First stress day", NEWLINE.TEX,
								LINE.ITEM, "Stress reaction magnitude (SRM)", NEWLINE.TEX,
								LINE.ITEM, "Velocity of the stress reaction (SRV)", NEWLINE.TEX,
								LINE.ITEM, "First recovery day", NEWLINE.TEX,
								LINE.ITEM, "Stress recovery ability (SRA)", NEWLINE.TEX,
								END.ITEM,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"Quality of the initial data\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"It is the ratio between stressed and unstressed plants should be \"1\" before the stress startet in the first growth period. 
								If the values are differs greatly from \"1\" then some preliminary investigations are nessary!", NEWLINE.TEX, 
									
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"First stress day\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"Is the day at which the pre stress and the stress function are intersect.", NEWLINE.TEX,
					
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"Stress reaction magnitude (SRM)\" can be described as following:", PAR.TEXT,NEWLINE.TEX, 
								"Is the value of the day at which the pre stress and stress functions are intersect.", NEWLINE.TEX,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"Velocity of the stress reaction (SRV)\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"SRV represent the slope of the stress function. The greater this value the faster is the stress reaction.", NEWLINE.TEX,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"First recovery day\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"Is the day at which the stress and the recovery function are intersect.", NEWLINE.TEX,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"Stress recovery ability (SRA)\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"SRA represent the slope of the recovery function. The greater this value the faster is the recovery reaction.", NEWLINE.TEX,

								sep = SEPARATOR.ITEM)
				),
				
				"20" = list(
						newSection = 5,
						title = "Biomass and water use efficiency",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"Following data are calculated:", NEWLINE.TEX,
								BEGIN.ITEM,
								LINE.ITEM, "Water use efficiency", NEWLINE.TEX,
								LINE.ITEM, "Digital Biomass", NEWLINE.TEX,
								END.ITEM,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"water use efficiency\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"Ratio of daily plant growth, determined by increasing projected side
										area and/or digital biomass, and water ussaged per day.", NEWLINE.TEX,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
								"The \"Digital Biomass\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
								"It is based on the projected side and top area of the plant. We use two equations:", NEWLINE.TEX,
								
								BEGIN.ITEM,
								LINE.ITEM, "$Biomass_{IAP}=\\sqrt{side.area_{average}^{2}*top.area}$", NEWLINE.TEX,
								LINE.ITEM, "$Biomass_{LemnaTec}=\\sqrt{side.area_{0^{\\circ}}*side.area_{90^{\\circ}}*top.area}$", NEWLINE.TEX,
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
						text = paste(NEW.PAR.INDENT,
								"Presented values in relative dependence per day.", NEWLINE.TEX, #Unit: $\\%/day$"
								sep = SEPARATOR.ITEM)
				),
			
				"7" = list(
						newSection = 8,
						title = "Visible light color analysis",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"In this section the plant pixels color shade values are investigated in detail. 
								 The color components color hue, color saturation or brightness are analyzed independently.", NEWLINE.TEX,
						 		"The three components based on the HSB color model, following classifications were made:", NEWLINE.TEX,
						 		paste(BEGIN.LOAD.IMAGE, HSB.PNG, MIDDLE.LOAD.IMAGE, "HSB color space" , END.LOAD.IMAGE, NEWLINE.TEX, sep=""), 	
#								paste(BEGIN.LOAD.IMAGE, HSB.PNG, END.LOAD.IMAGE, NEWLINE.TEX, sep=""), NEWLINE.TEX, 	
								"The following statistical analysis is performed for each color component of
								 the plant pixel values:", NEWLINE.TEX,
						 		BEGIN.ITEM,
						 		LINE.ITEM, "average", NEWLINE.TEX,
								LINE.ITEM, "standard deviation", NEWLINE.TEX,
								LINE.ITEM, "skewness", NEWLINE.TEX,
								LINE.ITEM, "kurtosis", NEWLINE.TEX,
								LINE.ITEM, "DGCI", NEWLINE.TEX,
								LINE.ITEM, "20 bin histogramm",NEWLINE.TEX,
								END.ITEM,
								
								NEW.PAR.INDENT, NEWLINE.TEX,
										"The skewness can be described as following:", PAR.TEXT, NEWLINE.TEX, 
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
										PAR.TEXT,
								NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term kurtosis is described as following:", PAR.TEXT, NEWLINE.TEX, 
										NEWLINE.TEX,
										"\"In probability theory and statistics, kurtosis (from the Greek word
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
										
								NEW.PAR.INDENT,	NEWLINE.TEX,
									"The term dark green color index (DGCI) is described as following:", PAR.TEXT, NEWLINE.TEX,
												"\"Hue, saturation, and brightness (HSB) values from digital images are processed
														into a dark green color index (DGCI), which combines HSB values into one composite number.
														Leaf color has been recognized as one of the most sensitive indicators of nutrient
														deficiencies (Blinn et al., 1988). Nitrogen is directly related to leaf color
														because it is a key component of the chlorophyll molecule (Tracy et al., 1992)
														Karcher and Richardson (2003) found that amounts of red and blue may alter how
														green an image appears. They suggested using a dark green color index (DGCI),
														which is derived from values of hue, saturation, and brightness (HSB). They found
														significant DGCI differences due to N treatments and that DGCI was a more consistent 
														measure of dark green color than were individual HSB values.\"", NEWLINE.TEX,
										BEGIN.TINY, NEWLINE.TEX,
										"Robert L. Rorie, et al., Association of “Greenness” in Corn with Yield and Leaf Nitrogen Concentration, Agronomy Journal, 2010, Vol. 103 No. 2, p. 529-535, doi: 10.2134/agronj2010.0296", END.TINY,
										
										
								sep = SEPARATOR.ITEM)
#						text = paste(NEW.PAR.INDENT,BEGIN.ITEM,
#								LINE.ITEM, "In this section the plant pixels color shade values are investigated in detail. 
#											The color components color hue, color saturation or brightness are analyzed independently.", NEWLINE.TEX,
#								LINE.ITEM, "The following statistical analysis is performed for each color component of
#											the plant pixel values: average, standard deviation, skewness and kurtosis.", NEWLINE.TEX,
#								END.ITEM,
#								sep = SEPARATOR.ITEM)
				),
				
				"60" = list(
						takeRestValuesFrom = "7"
				),
				
				"8" = list(
						newSection = 9,
						title = "Fluorescence activity",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"\"Fluorescence is the emission of light by a substance that has absorbed light or other electromagnetic radiation. 
								   It is a form of luminescence. In most cases, the emitted light has a longer wavelength, and therefore lower energy, than the absorbed radiation.\"",NEWLINE.TEX,
								BEGIN.TINY, NEWLINE.TEX,
								"Wikipedia contributors, \"Fluorescence\",", NEWLINE.TEX,
								"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
								paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Fluorescence&oldid=519374378", END.URL, sep=""), NEWLINE.TEX,
								"(accessed March 30, 2012).", END.TINY, NEWLINE.TEX,
								
								"Following classifications were made for the histogram (420 – 750 nm):", PAR.TEXT, NEWLINE.TEX,
#								paste(BEGIN.LOAD.IMAGE, FLUO.BIN.PNG, END.LOAD.IMAGE, sep=""), NEWLINE.TEX,
								paste(BEGIN.LOAD.IMAGE, FLUO.BIN.PNG, MIDDLE.LOAD.IMAGE, "Fluorescence activity range and bins", END.LOAD.IMAGE, sep=""),
								sep = SEPARATOR.ITEM)
#						text = paste(NEW.PAR.INDENT,BEGIN.ITEM,
#								LINE.ITEM, "Average and histogram of observed fluorescence colors (Fluo)", NEWLINE.TEX,
#								END.ITEM,
#								sep = SEPARATOR.ITEM)
				),
				
				"61" = list(
						takeRestValuesFrom = "8"
				),
				
				"9" = list(
						newSection = 10,
						title = "Near-infrared intensity",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"The water content of a plant is analysed with IR-B subspectrum (1450 – 1550 nm) of the NIR.", NEWLINE.TEX,
								"Following classifications were made for the histogram:",PAR.TEXT,  NEWLINE.TEX,
#								paste(BEGIN.LOAD.IMAGE, NIR.BIN.PNG, END.LOAD.IMAGE, sep=""), NEWLINE.TEX,
								paste(BEGIN.LOAD.IMAGE, NIR.BIN.PNG, MIDDLE.LOAD.IMAGE, "plant water content",END.LOAD.IMAGE, sep=""), 
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
						text = paste(NEW.PAR.INDENT,
								"Average intensity of infrared (LWIR, 8000 – 14000 nm).", NEWLINE.TEX,
								"Represents the relative temperature of the plant in comparison to the background.
								 The higher the value, the colder the leafs. High values may be the result of high transpiration. For temperature measurements, the upper 50 percent of
								 the background temperature values are used. If there is a blue rubber mat, the result of this calculation scheme is, that the warmer temperature of the
								 rubber mat is used for comparison, not the possibly colder soil (it may be colder because of transpiration).", NEWLINE.TEX,
								
								sep = SEPARATOR.ITEM)
				),
				
				"63" = list(
						takeRestValuesFrom = "10"
				),
				
				"80" = list(
						newSection = 12,
						title = "Detailed Section analysis",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,"The plant is separated in 5 Sections. The plant on the side image is divided from the lowest 20 \\% up to the highest 20 \\%. 
									  At the top image the sections represents circles based on the plant middle.", NEWLINE.TEX,
#									  paste(BEGIN.LOAD.IMAGE, SECTION.PNG, END.LOAD.IMAGE, sep=""), NEWLINE.TEX,
									  paste(BEGIN.LOAD.IMAGE, SECTION.PNG, MIDDLE.LOAD.IMAGE, "Sections of the plant; right: top view; left: side view", END.LOAD.IMAGE, sep=""),
									  "Different properties are calculated for this sections:", NEWLINE.TEX,
									 	BEGIN.ITEM,
										LINE.ITEM, "NDVI", NEWLINE.TEX,
										LINE.ITEM, "Skewness", NEWLINE.TEX,
										LINE.ITEM, "Kurtosis", NEWLINE.TEX,
										LINE.ITEM, "Mean", NEWLINE.TEX,
										LINE.ITEM, "Average", NEWLINE.TEX,
										LINE.ITEM, "standard deviation", NEWLINE.TEX,
										LINE.ITEM, "Relationships between special Bins", NEWLINE.TEX,
										END.ITEM,
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"Normalized Differenced Vegetation Index\" (NDVI) is described as following:", PAR.TEXT,  NEWLINE.TEX,
										  "NDVI can be used to make a statement on the health of the plant.
										   Healthy plants reflect few lights in the visible and much in the near infrared spectrum. The value can be calculated as follows:", NEWLINE.TEX,
										  "$NDVI = \\frac{(average NIR intensity - average RGB intensity)}{(average NIR intensity + average RGB intensity)}$", NEWLINE.TEX,
										  BEGIN.ITEM,
										  LINE.ITEM, "1 = rainforest", NEWLINE.TEX,
										  LINE.ITEM, "0.5 = grassland", NEWLINE.TEX,
										  LINE.ITEM, "0 = rock, sand or snow", NEWLINE.TEX,
										  LINE.ITEM, "-1 = water", NEWLINE.TEX,
										  END.ITEM,
										  
								
								sep = SEPARATOR.ITEM)

				),
				
				"14" = list(
						newSection = 13,
						title = "Relationships of stress related color groups",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,"All pixels from one color group (e.g. \"brown\", bin 3) are divided by an other color group (e.g. \"green\", bins 5-8). The groups are sums based of the showing bins:", NEWLINE.TEX,
#								paste(BEGIN.LOAD.IMAGE, HUE.BIN.PNG, END.LOAD.IMAGE, sep=""), NEWLINE.TEX, 
								PAR.TEXT, 
								paste(BEGIN.LOAD.IMAGE, HUE.BIN.PNG, MIDDLE.LOAD.IMAGE, "color shade of the HSB color space", END.LOAD.IMAGE, sep=""),
								sep = SEPARATOR.ITEM)
				),
				
				"15" = list(
						newSection = 14,
						title = "Leaf curling",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"The \"wave of the leafs\" is described under leaf curling. For this the fast Fourier transform (FFT) is used. Following was calculated:", NEWLINE.TEX,
							BEGIN.ITEM,
							LINE.ITEM, "The number of leaf segments", NEWLINE.TEX,
							LINE.ITEM, "Average frequency of “leaf curling waves” for leaf segments", NEWLINE.TEX,	
							LINE.ITEM, "Standard deviation of frequencies of \"leaf curling waves\" for the different leaf segments", NEWLINE.TEX,
							LINE.ITEM, "Average amplitude of \"leaf curling waves\" for leaf segments", NEWLINE.TEX,
							LINE.ITEM, "Standard deviation of amplitudes of “leaf curling waves” for the different leaf segments", NEWLINE.TEX,
							END.ITEM,	
				
							NEW.PAR.INDENT, NEWLINE.TEX,
							"The \"The number of leaf segments\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
							"This are alle segments which are larger than 100 pixels count.", NEWLINE.TEX,
							
							NEW.PAR.INDENT, NEWLINE.TEX,
							"The \"Average frequency of “leaf curling waves” for leaf segments\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
							"The higher the frequency the bigger is the \"wave\".", NEWLINE.TEX,
							
							NEW.PAR.INDENT, NEWLINE.TEX,
							"The \"Average amplitude of \"leaf curling waves\" for leaf segments\" can be described as following:", PAR.TEXT, NEWLINE.TEX, 
							"The higher the amplitude the higher the \"wave\".", NEWLINE.TEX,
							
				
						sep = SEPARATOR.ITEM)
				
				),
				
				"11" = list(
						newSection = 15,
						title = "Plant structures",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"Following data are calculated:", NEWLINE.TEX,
								BEGIN.ITEM,
								LINE.ITEM, "Number of leafs-tips", NEWLINE.TEX,
								LINE.ITEM, "Average length of all leafs plus stem", NEWLINE.TEX,
								LINE.ITEM, "Summed length of all leafs plus stem", NEWLINE.TEX,
								LINE.ITEM, "Average leaf width of all leafs", NEWLINE.TEX,
								LINE.ITEM, "Max leaf width of all leafs", NEWLINE.TEX,
								END.ITEM,
															
								sep = SEPARATOR.ITEM)
				),
			
				
				"12" = list(
						newSection = 16,
						title = "Wetness",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,
								"Following data are calculated:", NEWLINE.TEX,
								BEGIN.ITEM,
								LINE.ITEM, "Weighted loss through drought stress", NEWLINE.TEX,
								LINE.ITEM, "Average wetness", NEWLINE.TEX,
								END.ITEM,
								
								NEW.PAR.INDENT,	NEWLINE.TEX,
								"The term \"Weighted loss through drought stress\" is described as following:", PAR.TEXT,NEWLINE.TEX,
								"Number of foreground pixels from NIR side camera minus the weighted value of the plant", NEWLINE.TEX,
								"weightOfPlant = fully wet: 1 unit, fully dry: 1/7 unit", NEWLINE.TEX,

								sep = SEPARATOR.ITEM)
				),
				
				"13" = list(
						newSection = 17,
						title = "Convex hull",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = paste(NEW.PAR.INDENT,NEWLINE.TEX,
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
										"(accessed March 30, 2012).", END.TINY,NEWLINE.TEX,
										
										"Different properties are calculated for this sections:", NEWLINE.TEX,
										BEGIN.ITEM,
										LINE.ITEM, "Area", NEWLINE.TEX,
										LINE.ITEM, "First principle component - PC1", NEWLINE.TEX,
										LINE.ITEM, "Second principle component - PC2", NEWLINE.TEX,
										LINE.ITEM, "Fillgrade", NEWLINE.TEX,
										LINE.ITEM, "Compactness", NEWLINE.TEX,
										LINE.ITEM, "Circularity", NEWLINE.TEX,
										LINE.ITEM, "Circumcircle diameter", NEWLINE.TEX,
										END.ITEM,
										NEW.PAR.INDENT,	NEWLINE.TEX,
										
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"Area\" is described as following:", PAR.TEXT, NEWLINE.TEX,
										"It is the number of pixel  which is enclosed of the convex hull", NEWLINE.TEX,
											
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"first principle component\" (PC1) is described as following:", PAR.TEXT,NEWLINE.TEX,
										 "It is described as the largest part of the statistical dispersion. 
										This means, it described the direction with the largest information.", NEWLINE.TEX,
									
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"second principle component\" (PC2) is described as following:", PAR.TEXT,NEWLINE.TEX,
										"It is the opposite direction of the maximum extension of the plant", NEWLINE.TEX,
								
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"fillgrade\" is described as following:", PAR.TEXT,NEWLINE.TEX,
										"It is the ratio of the area of the plant (green pixel) divided by the area of the whole convex hull.", NEWLINE.TEX,
										"\"1\" means that the plant is circular and fill the whole convex hull", NEWLINE.TEX,
							
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"compactness\" is described as following:", PAR.TEXT,NEWLINE.TEX,
										"\"The compactness measure of a shape, sometimes called the shape factor, is a
										numerical quantity representing the degree to which a shape is compact.
										\\ldots A common compactness measure is the Isoperimetric quotient, the ratio
										of the area of the shape to the area of a circle (the most compact shape) having the same perimeter.\"", NEWLINE.TEX,
										BEGIN.TINY, NEWLINE.TEX,
										"Wikipedia contributors, \"Compactness measure of a shape\",", NEWLINE.TEX,
										"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
										paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Compactness_measure_of_a_shape&oldid=485591387", END.URL, sep=""), NEWLINE.TEX,
										"(accessed June 25, 2012).", END.TINY,
									
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"Circularity\" is described as following:", PAR.TEXT, NEWLINE.TEX,
										"\"Roundness is the measure of the sharpness of a particle's edges and corners.\"", NEWLINE.TEX,
										BEGIN.TINY, NEWLINE.TEX,
										"Wikipedia contributors, \"Roundness (object)\",", NEWLINE.TEX,
										"Wikipedia, The Free Encyclopedia,", NEWLINE.TEX,
										paste(BEGIN.URL, "http://en.wikipedia.org/w/index.php?title=Roundness_(object)&oldid=487251484", END.URL, sep=""), NEWLINE.TEX,
										"(accessed June 25, 2012).", END.TINY,
									
										NEW.PAR.INDENT,	NEWLINE.TEX,
										"The term \"Circumcircle diameter\" is described as following:", PAR.TEXT,NEWLINE.TEX,
										"The diameter of a minimum circle surrounding the plant.", NEWLINE.TEX,

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
				),
				
				"error" = list(
						newSection = 100,
						title = "Error-Messages",
						typOfReset = RESET.PAGE,
						typOfClear = CLEAR.PAGE,
						text = ""
				)
		),
		subsection = list(
				
				"1.1" = list(
						newSection = 1,
						title = "Weights", #(before and after watering)
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"1.2" = list(
						newSection = 2,
						title = "Daily watering amounts",
						text = paste(NEW.PAR.INDENT,
									"The sum of the watering amount of the day.", NEWLINE.TEX,
									 sep = SEPARATOR.ITEM),
						takeRestValuesFrom = "1.1"
				),		
				
				"30.1" = list(
						newSection = 1,
						title = "Difference between special descriptors (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"30.2" = list(
						title = "Difference between special descriptors",
						takeRestValuesFrom = "30.1"
				),
				
				"70.1" = list(
						newSection = 2,
						title = "Difference within special descriptors (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"70.2" = list(
						title = "Difference within special descriptors",
						takeRestValuesFrom = "70.1"
				),
				
				"2.1" = list(
						newSection = 3,
						title = "Zoom changes",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"40.1" = list(
						newSection = 4,
						title = "Stress ratio",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"3.1" = list(
						newSection = 1,
						title = "First growth period",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"3.2" = list(
						newSection = 2,
						title = "Modelling the behaviour during stress",
						takeRestValuesFrom = "3.1"
				),
				
				"3.3" = list(
						newSection = 3,
						title = "Stress recovery periode",
						takeRestValuesFrom = "3.1"
				),
				
				"20.1" = list(
						newSection = 1,
						title = "IAP formula",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						#typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"4.1" = list(
						takeRestValuesFrom = "20.1"
				),
				
				"20.2" = list(
						newSection = 2,
						title = "LemnaTec formula",
						text = "",
						takeRestValuesFrom = "20.1"
				),
				
				"4.2" = list(
						takeRestValuesFrom = "20.2"
				),
				
				"4.3" = list(
						newSection = 3,
						title = "Water use efficiency",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"21.1" = list(
						newSection = 1,
						title = "Height (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"5.1" = list(
						takeRestValuesFrom = "21.1"
				),
				
				"21.2" = list(
						title = "Height",
						text = "",
						takeRestValuesFrom = "21.1"
				),
				
				"5.2" = list(
						takeRestValuesFrom = "21.2"
				),
				
				
				"21.3" = list(
						newSection = 2,
						title = "Width (zoom corrected)",
						text = "",
						takeRestValuesFrom = "21.1"
				),
				
				"5.3" = list(
						takeRestValuesFrom = "21.3"
				),
				
				"21.4" = list(
						title = "Width",
						text = "",
						takeRestValuesFrom = "21.3"
				),
				
				"5.4" = list(
						takeRestValuesFrom = "21.4"
				),
				
				
				"21.5" = list(
						newSection = 3,
						title = "Projected side area (zoom corrected)",
						text = "",
						takeRestValuesFrom = "21.1"
				),
				
				"5.5" = list(
						takeRestValuesFrom = "21.5"
				),
				
				"21.6" = list(
						title = "Projected side area",
						text = "",
						takeRestValuesFrom = "21.5"
				),
				
				"5.6" = list(
						takeRestValuesFrom = "21.6"
				),
				
				"21.7" = list(
						newSection = 4,
						title = "Projected top area (zoom corrected)",
						text = "",
						takeRestValuesFrom = "21.1"
				),
				
				"5.7" = list(
						takeRestValuesFrom = "21.7"
				),
				
				"21.8" = list(
						title = "Projected top area",
						text = "",
						takeRestValuesFrom = "21.7"
				),
				
				"5.8" = list(
						takeRestValuesFrom = "21.8"
				),
				
				"21.9" = list(
						newSection = 5,
						title = "Side border length (zoom corrected)",
						text = "",
						takeRestValuesFrom = "21.1"
				),
				
				"5.9" = list(
						takeRestValuesFrom = "21.9"
				),
				
				"21.10" = list(
						title = "Side border length",
						text = "",
						takeRestValuesFrom = "21.9"
				),
				
				"5.10" = list(
						takeRestValuesFrom = "21.10"
				),
				
				"21.11" = list(
						newSection = 6,
						title = "Top border length (zoom corrected)",
						text = "",
						takeRestValuesFrom = "21.1"
				),
				
				"5.11" = list(
						takeRestValuesFrom = "21.11"
				),
				
				"21.12" = list(
						title = "Top border length",
						text = "",
						takeRestValuesFrom = "21.11"
				),
				
				"5.12" = list(
						takeRestValuesFrom = "21.12"
				),
				
				
				"6.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Projected side area",
						text = ""
				),
				
				"6.2" = list(
						newSection = 2,
						title = "Height",
						text = "",
						takeRestValuesFrom = "6.1"
				),
				
				"6.3" = list(
						newSection = 3,
						title = "Width",
						text = "",
						takeRestValuesFrom = "6.1"
				),
				
				"6.4" = list(
						newSection = 4,
						title = "Projected top area",
						text = "",
						takeRestValuesFrom = "6.1"
				),
				
				"6.5" = list(
						newSection = 5,
						title = "Digital biomass (IAP formula)",
						text = "",
						takeRestValuesFrom = "6.1"
				),

				"8.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Side",
						text = ""
				),
				
				"9.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.1"
				),				
				
				"10.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.1"
				),	
				
				"7.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.1"
				),
				
				"60.1" = list(
						takeRestValuesFrom = "7.1"
				),
				
				"61.1" = list(
						takeRestValuesFrom = "8.1"
				),
				
				"62.1" = list(
						takeRestValuesFrom = "9.1"
				),
				
				"63.1" = list(
						takeRestValuesFrom = "10.1"
				),
				
				
				"8.2" = list(
						newSection = 2,
						title = "Top",
						takeRestValuesFrom = "8.1"		
				),
				
				"9.2" = list(
						newSection = 2,
						takeRestValuesFrom = "8.2"
				
				),
				
				"10.2" = list(
						newSection = 2,
						takeRestValuesFrom = "8.2"
				),
				
				
				"7.2" = list(
						newSection = 2,
						takeRestValuesFrom = "8.2"
				),
				
				"60.2" = list(
						takeRestValuesFrom = "7.2"
				),
								
				"61.2" = list(
						takeRestValuesFrom = "8.2"
				),
								
				"62.2" = list(
						takeRestValuesFrom = "9.2"
				),
				
				"63.2" = list(
						takeRestValuesFrom = "10.2"
				),			
				
				"80.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Side",
						text = ""
				),
				
				"80.2" = list(
						newSection = 2,
						title = "Top",
						takeRestValuesFrom = "80.1"
				),		
				
				
				"14.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Side",
						text = ""
				),
				
				"14.2" = list(
						newSection = 2,
						title = "Top",
						takeRestValuesFrom = "14.1"
				),	
				
				
				"15.1" = list(
						newSection = 1,
						takeRestValuesFrom = "14.1"
					
				),
				
				"15.2" = list(
						newSection = 2,
						takeRestValuesFrom = "14.2"
				),	
				
				"11.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Side",
						text = ""
				),
				
				"11.2" = list(
						newSection = 2,
						title = "Top",
						takeRestValuesFrom = "11.2"
				),	
				
				"12.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Average wetness of side image",
						text = ""
				),
				
				"12.2" = list(
						newSection = 2,
						title = "Average wetness of top image",
						takeRestValuesFrom = "12.1"	
				),
				
				"12.3" = list(
						newSection = 3,
						title = "Weighted loss through drought stress - side image",
						text = "",
						takeRestValuesFrom = "12.1"
				),
	
				"12.4" = list(
						newSection = 4,
						title = "Weighted loss through drought stress - top image",
						takeRestValuesFrom = "12.2"
				),
				
				"13.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
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
				
				"1.1.1" = list(
						newSection = 1,
						title = "Before watering",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"1.1.2" = list(
						newSection = 2,
						title = "After watering",
						takeRestValuesFrom = "1.1.1"
				),
				
				"2.1.1" = list(
						newSection = 1,
						title = "Top marker",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"2.1.2" = list(
						newSection = 2,
						title = "Bottom marker",
						takeRestValuesFrom = "2.1.1"
				),
				####################### stress start ######################
				#
				"3.1.1" = list(
						newSection = 1,
						title = "Quality of the initial data",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
			
#				"3.1.2" = list(
#						newSection = 2,
#						title = "",
#						typOfReset = RESET.PAGE.NO,
#						typOfClear = CLEAR.PAGE.NO,
#						text = paste(NEW.PAR.INDENT,
#								"SRV represent the slope of the stress function. The greater this value the faster is the stress reaction.", NEWLINE.TEX,
#								sep = SEPARATOR.ITEM)
#				),
		
				#####################  stress period ###################
				"3.2.1" = list(
						newSection = 1,
						title = "First stress day",
						text = "",
						takeRestValuesFrom = "3.1.1"
				),
				
				"3.2.2" = list(
						newSection = 2,
						title = "stress reaction magnitude (SRM)",
						text = "",
						takeRestValuesFrom = "3.1.1"
				),
				
				"3.2.3" = list(
						newSection = 3,
						title = "Velocity of the stress reaction (SRV)",
						text = "",
						takeRestValuesFrom = "3.1.1"
				),
				
				
				############ stress recovery ##########################
				
				"3.3.1" = list(
						newSection = 1,
						title = "First recovery day",
						text = "",
						takeRestValuesFrom = "3.1.1"
				),
				
				"3.3.2" = list(
						newSection = 2,
						title = "Stress recovery ability (SRA)",
						text = "",
						takeRestValuesFrom = "3.1.1"
				),
				
				#
				################### stress end #####################
			
				"20.1.1" = list(
						newSection = 1,
						title = "RGB",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"20.1.2" = list(
						newSection = 2,
						title = "Fluorescence",
						takeRestValuesFrom = "20.1.1"
				),
				
				"4.1.1" = list(
						takeRestValuesFrom = "20.1.1"
				),
				
				"4.1.2" = list(
						takeRestValuesFrom = "20.1.2"
				),
				
					
#				"20.1.1" = list(
#						newSection = 1,
#						title = "Fluctuation at special days",
#						typOfReset = RESET.PAGE.NO,
#						typOfClear = CLEAR.PAGE.NO,
#						text = ""
#				),
#				
#				"4.1.1" = list(
#						newSection = 1,
#						title = "value over the time",
#						takeRestValuesFrom = "20.1.1"
#				),
				
				"20.2.1" = list(
#						takeRestValuesFrom = "20.1.1"
						newSection = 1,
						title = "Fluctuation at special days",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"4.2.1" = list(
#						takeRestValuesFrom = "4.1.1"
						newSection = 1,
						title = "Value over the time",
						takeRestValuesFrom = "20.2.1"
				),
				
				
				"4.3.1" = list(
						newSection = 1,
						title = "Based on digital biomass",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text =""
				),
				
				"4.3.2" = list(
						newSection = 2,
						title = "Based on projected side area",
						takeRestValuesFrom = "4.3.1"
				),
				
				"21.1.1" = list(
						newSection = 1,
						takeRestValuesFrom = "20.2.1"
				),
				
				"21.2.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.3.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.4.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.5.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.6.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.7.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.8.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.9.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.10.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.11.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"21.12.1" = list(
						takeRestValuesFrom = "21.1.1"
				),
				
				"5.1.1" = list(
						newSection = 1,
						takeRestValuesFrom = "4.2.1"
				),
				
				"5.2.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.3.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.4.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.5.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.6.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.7.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.8.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.9.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.10.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.11.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"5.12.1" = list(
						takeRestValuesFrom = "5.1.1"
				),
				
				"7.1.1" = list(
						newSection = 1,
						title = "Color shade (hue)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
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
				
				"7.1.4" = list(
						newSection = 4,
						title = "NDVI, RGB",
						takeRestValuesFrom = "7.1.1"
				),			
				
				"7.1.5" = list(
						newSection = 5,
						title = "LAB - Lightness (L)",
						takeRestValuesFrom = "7.1.1"
				),
				
				"7.1.6" = list(
						newSection = 6,
						title = "LAB - Green-Red-Values (A)",
						takeRestValuesFrom = "7.1.1"
				),
				
				"7.1.7" = list(
						newSection = 7,
						title = "LAB - Blue-Yellow-Values (B)",
						takeRestValuesFrom = "7.1.1"
				),				
				
				"7.2.1" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1"
				),
				
				"60.2.1" = list(
						takeRestValuesFrom = "7.2.1"
				),
				
				"7.2.2" = list(
						newSection = 2,
						takeRestValuesFrom = "7.1.2"
				),
				
				"60.2.2" = list(
						takeRestValuesFrom = "7.2.2"
				),
				
				"7.2.3" = list(
						newSection = 3,
						takeRestValuesFrom = "7.1.3"
				),
				
				
				"60.2.3" = list(
						takeRestValuesFrom = "7.2.3"
				),
				
				"7.2.4" = list(
						newSection = 4,
						takeRestValuesFrom = "7.1.4"
				),
				
				"7.2.5" = list(
						newSection = 5,
						takeRestValuesFrom = "7.1.5"
				),
				
				"7.2.6" = list(
						newSection = 6,
						takeRestValuesFrom = "7.1.6"
				),
				
				"7.2.7" = list(
						newSection = 7,
						takeRestValuesFrom = "7.1.7"
				),	
				
				"8.1.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Average intensity",
						text = ""
				),
				
				"8.1.2" = list(
						newSection = 3,
						title = "Ratio of phenol and chlorophyll",
						takeRestValuesFrom = "8.1.1"
				),
				
				
				"8.1.3" = list(
						newSection = 4,
						title = "Chlorophyll",
						takeRestValuesFrom = "8.1.1"
				),
				
				"8.1.4" = list(
						newSection = 5,
						title = "Classic",
						takeRestValuesFrom = "8.1.1"
				),
				
				"8.1.5" = list(
						newSection = 6,
						title = "Phenol",
						takeRestValuesFrom = "8.1.1"
				),
				
				
				"9.1.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.1.1"
				),
				
				"9.1.2" = list(
						newSection = 2,
						title = "Sum of the Intensity",
						takeRestValuesFrom = "9.1.1"
				),
				
				"9.1.3" = list(
						newSection = 3,
						title = "Intensity of the skeleton",
						takeRestValuesFrom = "9.1.1"
				),
				
				"10.1.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.1.1"
				),
				
				"10.1.2" = list(
						newSection = 2,
						takeRestValuesFrom = "9.1.2"
				),
				
				"10.1.3" = list(
						newSection = 3,
						takeRestValuesFrom = "9.1.3"
				),
				
				"61.1.1" = list(
						newSection = 2,
						title = "Intensity histogram side view",	#Fluorescence spectra side view
						takeRestValuesFrom = "8.1.1"
				),
				
				"61.1.2" = list(
						title = "Intensity histogram side view (zoom corrected)", #Fluorescence spectra side view (zoom corrected)
						takeRestValuesFrom = "61.1.1"
				),
				
				"62.1.1" = list(
						newSection = 3,
						takeRestValuesFrom = "61.1.1"
				),
				
				"62.1.2" = list(
						newSection = 3,
						takeRestValuesFrom = "61.1.2"
				),
				
				"63.1.1" = list(
						newSection = 2,
						takeRestValuesFrom = "61.1.1"
				),
				
				"63.1.2" = list(
						newSection = 2,
						takeRestValuesFrom = "61.1.2"
				),
			
#				"61.1.1" = list(
#						newSection = 2,
#						title = "Fluorescence spectra side view",
#						takeRestValuesFrom = "8.1.1"
#				),
#				
#				"61.1.2" = list(
#						title = "Fluorescence spectra side view (zoom corrected)",
#						takeRestValuesFrom = "61.1.1"
#				),
				
				
				
				
				
				"8.2.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.1.1"
				),
				
				"8.2.2" = list(
						newSection = 3,
						takeRestValuesFrom = "8.1.2"
				),
				
				"8.2.3" = list(
						newSection = 4,
						takeRestValuesFrom = "8.1.3"
				),
				
				"8.2.4" = list(
						newSection = 5,
						takeRestValuesFrom = "8.1.4"
				),
				
				"8.2.5" = list(
						newSection = 6,
						takeRestValuesFrom = "8.1.5"
				),
				
				"9.2.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.2.1"
				),
				
				"9.2.2" = list(
						takeRestValuesFrom = "9.1.2"
				),
				
				"9.2.3" = list(
						takeRestValuesFrom = "9.1.3"
				),
				
				"10.2.1" = list(
						newSection = 1,
						takeRestValuesFrom = "8.2.1"
				),
				
				"10.2.2" = list(
						newSection = 2,
						takeRestValuesFrom = "10.1.2"
				),
				
				"10.2.3" = list(
						newSection = 3,
						takeRestValuesFrom = "10.1.3"
				),
				
				"61.2.1" = list(
						newSection = 2,
						title = "Intensity histogram top view",	# Fluorescence spectra top view
						takeRestValuesFrom = "8.2.1"
				),
				
				"61.2.2" = list(
						title = "Intensity histogram top view (zoom corrected)",	# Fluorescence spectra top view (zoom corrected)
						takeRestValuesFrom = "61.2.1"
				),
				
				"62.2.1" = list(
						newSection = 3,
						takeRestValuesFrom = "61.2.1"
				),
				
				"62.2.2" = list(
						newSection = 3,
						takeRestValuesFrom = "61.2.1"
				),
				
				"63.2.1" = list(
						newSection = 2,
						takeRestValuesFrom = "61.2.1"
				),
				
				"63.2.2" = list(
						newSection = 2,
						takeRestValuesFrom = "61.2.1"
				),
				
				
				"80.1.1" = list(
						newSection = 1,
						title = "NDVI",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"80.1.2" = list(
						newSection = 2,
						title = "DGCI",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.3" = list(
						newSection = 3,
						title = "HSB - Color shade",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.4" = list(
						newSection = 4,
						title = "HSB - Saturation",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.5" = list(
						newSection = 5,
						title = "HSB - Brightness",
						takeRestValuesFrom = "80.1.1"
				),
			
				"80.1.6" = list(
						newSection = 6,
						title = "LAB - Lightness (L)",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.7" = list(
						newSection = 7,
						title = "LAB - Green-Red-Values (A)",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.8" = list(
						newSection = 8,
						title = "LAB - Blue-Yellow-Values (B)",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.9" = list(
						newSection = 9,
						title = " Brown bin divided by green bins",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.10" = list(
						newSection = 10,
						title = "Red bins divided by green bins",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.1.11" = list(
						newSection = 11,
						title = "Yellow bins divided by green bins",
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.2.1" = list(
						takeRestValuesFrom = "80.1.1"
				),
				
				"80.2.2" = list(
						takeRestValuesFrom = "80.1.2"
				),
				
				"80.2.3" = list(
						takeRestValuesFrom = "80.1.3"
				),
				
				"80.2.4" = list(
						takeRestValuesFrom = "80.1.4"
				),
				
				"80.2.5" = list(
						takeRestValuesFrom = "80.1.5"
				),
				
				"80.2.6" = list(
						takeRestValuesFrom = "80.1.6"
				),
				
				"80.2.7" = list(
						takeRestValuesFrom = "80.1.7"
				),
				
				"80.2.8" = list(
						takeRestValuesFrom = "80.1.8"
				),
				
				"80.2.9" = list(
						takeRestValuesFrom = "80.1.9"
				),
				
				"80.2.10" = list(
						takeRestValuesFrom = "80.1.10"
				),
				
				"80.2.11" = list(
						takeRestValuesFrom = "80.1.11"
				),
#				
#				"11.1" = list(
#						newSection = 1,
#						typOfReset = RESET.PAGE.NO,
#						typOfClear = CLEAR.PAGE.NO,
#						title = "Number of leafs",
#						text = paste(NEW.PAR.INDENT,BEGIN.ITEM,
#								LINE.ITEM, "Number of leafs-tips", NEWLINE.TEX,
#								END.ITEM,
#								sep = SEPARATOR.ITEM)
#				),		
#				
#				"11.2" = list(
#						newSection = 2,
#						title = "Leaf lengths",
#						text = paste(NEW.PAR.INDENT,BEGIN.ITEM,
#								LINE.ITEM, "Length of all leafs plus stem", NEWLINE.TEX,
#								END.ITEM,
#								sep = SEPARATOR.ITEM),
#						takeRestValuesFrom = "11.1"
#				),
#				
#				"11.3" = list(
#						newSection = 3,
#						title = "Flower detection",
#						text = paste(NEW.PAR.INDENT,BEGIN.ITEM,
#								LINE.ITEM, "Number of tassel florets", NEWLINE.TEX,
#								END.ITEM,
#								sep = SEPARATOR.ITEM),
#						takeRestValuesFrom = "11.1"
#				),
#				


				"11.1.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Leafs",
						text = ""
				),
				
				
				"11.1.2" = list(
						newSection = 2,
						title = "Bloom",
						takeRestValuesFrom = "11.1.1"
				),
				
				"11.2.1" = list(
						newSection = 1,
						takeRestValuesFrom = "11.1.1"
				),
				
				
				"11.2.2" = list(
						newSection = 2,
						takeRestValuesFrom = "11.1.2"
				),
				
				

				
				"14.1.1" = list(
						newSection = 1,
						title = "Brown divided by green",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"14.1.2" = list(
						newSection = 2,
						title = "Red divided by green",
						takeRestValuesFrom = "14.1.1"
				),
				
				"14.1.3" = list(
						newSection = 3,
						title = "Yellow divided by green",
						takeRestValuesFrom = "14.1.1"
				),
				
				
				"14.2.1" = list(
						takeRestValuesFrom = "14.1.1"
				),
				
				"14.2.2" = list(
						takeRestValuesFrom = "14.1.2"
				),
				
				"14.2.3" = list(
						takeRestValuesFrom = "14.1.3"
				),
				
				
				
				"15.1.1" = list(
						newSection = 1,
						title = "Number of leaf segments ",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"15.1.2" = list(
						newSection = 2,
						title = "Average frequency of \"leaf curling waves\"",
						takeRestValuesFrom = "15.1.1"
				),
				
				"15.1.3" = list(
						newSection = 3,
						title = "Standard deviation of frequencies of \"leaf curling waves\"",
						takeRestValuesFrom = "15.1.1"
				),
				
				"15.1.4" = list(
						newSection = 4,
						title = "Average amplitude of \"leaf curling waves\"",
						takeRestValuesFrom = "15.1.1"
				),
				
				"15.1.5" = list(
						newSection = 5,
						title = "Standard deviation of amplitudes of \"leaf curling waves\"",
						takeRestValuesFrom = "15.1.1"
				),
				
				"15.2.1" = list(
						takeRestValuesFrom = "15.1.1"
				),
				
				"15.2.2" = list(
						takeRestValuesFrom = "15.1.2"
				),
				
				"15.2.3" = list(
						takeRestValuesFrom = "15.1.3"
				),
				
				"15.2.4" = list(
						takeRestValuesFrom = "15.1.4"
				),
				
				
				"15.2.5" = list(
						takeRestValuesFrom = "15.1.5"
				),

				"13.1.1" = list(
						newSection = 1,
						title = "Area (zoom corrected)",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"13.1.2" = list(
						title = "Area",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.3" = list(
						newSection = 4,
						title = "PC1 - maximum distance (zoom corrected)",
						text = "",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.4" = list(
						title = "PC1 - maximum distance",
						takeRestValuesFrom = "13.1.3"
				),
				
				"13.1.5" = list(
						newSection = 5,
						title = "PC2 - Opposite direction maxmium distance (zoom corrected)",
						text = "",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.6" = list(
						title = "PC2 - Opposite direction maxmium distance",
						takeRestValuesFrom = "13.1.5"
				),
				
				"13.1.7" = list(
						newSection = 6,
						title = "Fillgrade",
						text = "",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.8" = list(
						newSection = 7,
						title = "Compactness",
						text = "",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.9" = list(
						newSection = 8,
						title = "Circularity",
						text = "",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.10" = list(
						newSection = 2,
						title = "Circumcircle diameter",
						text = "",
						takeRestValuesFrom = "13.1.1"
				),
				
				"13.1.11" = list(
						newSection = 3,
						title = "Average distance to center",
						text = "",
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
				),
				"13.2.11" = list(
						takeRestValuesFrom = "13.1.11"
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
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.1.1.3" = list(
						newSection = 3,
						title = "NIR intensity",
						takeRestValuesFrom = "3.1.1.1"
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
				
				"3.2.3.1" = list(
						takeRestValuesFrom = "3.1.1.1"
				),
				
				"3.2.3.2" = list(
						takeRestValuesFrom = "3.1.1.2"
				),
				
				"3.2.3.3" = list(
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
				
				"20.1.1.1" = list(
						newSection = 1,
						title = "Fluctuation at special days",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"20.1.2.1" = list(
						newSection = 1,
						takeRestValuesFrom = "20.1.1.1"
				),
				
				"4.1.1.1" = list(
						newSection = 2,
						title = "Value over the time",
						takeRestValuesFrom = "20.1.1.1"
				),
				
				"4.1.2.1" = list(
						newSection = 2,
						takeRestValuesFrom = "4.1.1.1"
				),
							
				
				"80.1.1.1" = list(
						newSection = 1,
						title = "Based on RGB",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"80.1.1.2" = list(
						newSection = 2,
						title = "Blue intensity",
						takeRestValuesFrom = "80.1.1.1"
				),
				
				"80.1.1.3" = list(
						newSection = 3,
						title = "Green intensity",
						takeRestValuesFrom = "80.1.1.1"
				),
				
				"80.1.1.4" = list(
						newSection = 4,
						title = "Red intensity",
						takeRestValuesFrom = "80.1.1.1"
				),
				
								
				"80.1.3.1" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"80.1.3.2" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"80.1.3.3" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"80.1.3.4" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				
				"80.1.4.1" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"80.1.4.2" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"80.1.4.3" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"80.1.4.4" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"80.1.5.1" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"80.1.5.2" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"80.1.5.3" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"80.1.5.4" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"80.1.6.1" = list(
						newSection = 1,
#						title = "Mean",
						takeRestValuesFrom = "7.1.5.1"
				),
				
				"80.1.6.2" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"80.1.6.3" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"80.1.6.4" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				
				"80.1.7.1" = list(
						newSection = 1,
#						title = "Mean",
						takeRestValuesFrom = "7.1.5.1"
				),
				
				"80.1.7.2" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"80.1.7.3" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"80.1.7.4" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				
				"80.1.8.1" = list(
						newSection = 1,
#						title = "Mean",
						takeRestValuesFrom = "7.1.5.1"
				),
				
				"80.1.8.2" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"80.1.8.3" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"80.1.8.4" = list(
						newSection = 1,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				
				"80.2.1.1" = list(
						takeRestValuesFrom = "80.1.1.1"
				),
				
				"80.2.1.2" = list(
						takeRestValuesFrom = "80.1.1.2"
				),
				
				"80.2.1.3" = list(
						takeRestValuesFrom = "80.1.1.3"
				),
				
				"80.2.1.4" = list(
						takeRestValuesFrom = "80.1.1.4"
				),
				
				"80.2.2.1" = list(
						takeRestValuesFrom = "80.1.2.1"
				),
				
				"80.2.2.2" = list(
						takeRestValuesFrom = "80.1.2.2"
				),
				
				"80.2.2.3" = list(
						takeRestValuesFrom = "80.1.2.3"
				),
				
				"80.2.2.4" = list(
						takeRestValuesFrom = "80.1.2.4"
				),	
				
				"80.2.3.1" = list(
						takeRestValuesFrom = "80.1.3.1"
				),
				
				"80.2.3.2" = list(
						takeRestValuesFrom = "80.1.3.2"
				),
				
				"80.2.3.3" = list(
						takeRestValuesFrom = "80.1.3.3"
				),
				
				"80.2.3.4" = list(
						takeRestValuesFrom = "80.1.3.4"
				),
				
				
				"80.2.4.1" = list(
						takeRestValuesFrom = "80.1.4.1"
				),
				
				"80.2.4.2" = list(
						takeRestValuesFrom = "80.1.4.2"
				),
				
				"80.2.4.3" = list(
						takeRestValuesFrom = "80.1.4.3"
				),
				
				"80.2.4.4" = list(
						takeRestValuesFrom = "80.1.4.4"
				),
				
				"80.2.5.1" = list(
						takeRestValuesFrom = "80.1.5.1"
				),
				
				"80.2.5.2" = list(
						takeRestValuesFrom = "80.1.5.2"
				),
				
				"80.2.5.3" = list(
						takeRestValuesFrom = "80.1.5.3"
				),
				
				"80.2.5.4" = list(
						takeRestValuesFrom = "80.1.5.4"
				),
				
				"80.2.6.1" = list(
						title = "Mean",
						takeRestValuesFrom = "80.1.6.1"
				),
				
				"80.2.6.2" = list(
						takeRestValuesFrom = "80.1.6.2"
				),
				
				"80.2.6.3" = list(
						takeRestValuesFrom = "80.1.6.3"
				),
				
				"80.2.6.4" = list(
						takeRestValuesFrom = "80.1.6.4"
				),
				
				"80.2.7.1" = list(
						title = "Mean",
						takeRestValuesFrom = "80.1.7.1"
				),
				
				"80.2.7.2" = list(
						takeRestValuesFrom = "80.1.7.2"
				),
				
				"80.2.7.3" = list(
						takeRestValuesFrom = "80.1.7.3"
				),
				
				"80.2.7.4" = list(
						takeRestValuesFrom = "80.1.7.4"
				),
				
				"80.2.8.1" = list(
						title = "Mean",
						takeRestValuesFrom = "80.1.8.1"
				),
				
				"80.2.8.2" = list(
						takeRestValuesFrom = "80.1.8.2"
				),
				
				"80.2.8.3" = list(
						takeRestValuesFrom = "80.1.8.3"
				),
				
				"80.2.8.4" = list(
						takeRestValuesFrom = "80.1.8.4"
				),			
				
				"7.1.1.1" = list(
						newSection = 1,
						title = "Average hue",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
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
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.6" = list(
						title = "Skewness (zoom corrected)",
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"7.1.1.7" = list(
						newSection = 4,
						title = "Kurtosis",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.8" = list(
						title = "Kurtosis (zoom corrected)",
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"7.1.1.9" = list(
						newSection = 5,
						title = "Bin 1 [01-12]",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.10" = list(
						title = "Bin 1 [01-12] (zoom corrected)",
						takeRestValuesFrom = "7.1.1.9"
				),
				
				"7.1.1.11" = list(
						newSection = 6,
						title = "Bin 2 [12-25]",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.12" = list(
						title = "Bin 2 [12-25] (zoom corrected)",
						takeRestValuesFrom = "7.1.1.11"
				),
				
				"7.1.1.13" = list(
						newSection = 7,
						title = "Bin 3 [25-38]",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.14" = list(
						title = "Bin 3 [25-38] (zoom corrected)",
						takeRestValuesFrom = "7.1.1.13"
				),
				
				"7.1.1.15" = list(
						newSection = 8,
						title = "Bin 4 [38-51]",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.16" = list(
						title = "Bin 4 [38-51] (zoom corrected)",
						takeRestValuesFrom = "7.1.1.15"
				),
				
				"7.1.1.17" = list(
						newSection = 9,
						title = "Bin 5 [51-63]",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.18" = list(
						title = "Bin 5 [51-63] (zoom corrected)",
						takeRestValuesFrom = "7.1.1.17"
				),
				
				"7.1.1.19" = list(
						newSection = 10,
						title = "Bin 6 [63-76]",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.1.20" = list(
						title = "Bin 6 [63-76] (zoom corrected)",
						takeRestValuesFrom = "7.1.1.19"
				),
			
				"7.1.1.21" = list(
						newSection = 11,
						title = "DGCI, dark green color index",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"60.1.1.1" = list(
						newSection = 12,
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
				
				"7.1.2.21" = list(
						takeRestValuesFrom = "7.1.1.21"
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
				
				
				"7.1.4.1" = list(
						newSection = 1,
						title = "NDVI value",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				
				"7.1.4.2" = list(
						title = "Red intensity average",
						takeRestValuesFrom = "7.1.4.1"
				),
				
				"7.1.4.3" = list(
						newSection = 3,
						title = "Green intensity average",
						takeRestValuesFrom = "7.1.4.1"
				),
				
				"7.1.4.4" = list(
						title = "Blue intensity average",
						takeRestValuesFrom = "7.1.4.3"
				),

				
				"7.1.5.1" = list(
						newSection = 1,
						title = "Mean value",
						takeRestValuesFrom = "7.1.1.1"
				),
				
				"7.1.5.2" = list(
						newSection = 2,
						takeRestValuesFrom = "7.1.1.3"
				),
				
				"7.1.5.3" = list(
						newSection = 3,
						takeRestValuesFrom = "7.1.1.5"
				),
				
				"7.1.5.4" = list(
						newSection = 4,
						takeRestValuesFrom = "7.1.1.7"
				),
				
				"7.1.6.1" = list(
						takeRestValuesFrom = "7.1.5.1"
				),
				
				"7.1.6.2" = list(
						takeRestValuesFrom = "7.1.5.2"
				),
				
				"7.1.6.3" = list(
						takeRestValuesFrom = "7.1.5.3"
				),
				
				"7.1.6.4" = list(
						takeRestValuesFrom = "7.1.5.4"
				),
				
				"7.1.7.1" = list(
						takeRestValuesFrom = "7.1.5.1"
				),
				
				"7.1.7.2" = list(
						takeRestValuesFrom = "7.1.5.2"
				),
				
				"7.1.7.3" = list(
						takeRestValuesFrom = "7.1.5.3"
				),
				
				"7.1.7.4" = list(
						takeRestValuesFrom = "7.1.5.4"
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
				
				"7.2.1.21" = list(
						takeRestValuesFrom = "7.1.1.21"
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
				),
				
				"7.2.4.1" = list(
						takeRestValuesFrom = "7.1.4.1"
				),
				
				"7.2.4.2" = list(
						takeRestValuesFrom = "7.1.4.2"
				),
				
				"7.2.4.3" = list(
						takeRestValuesFrom = "7.1.4.3"
				),
				
				"7.2.4.4" = list(
						takeRestValuesFrom = "7.1.4.4"
				),
			
				"7.2.5.1" = list(
						takeRestValuesFrom = "7.1.5.1"
				),
				
				"7.2.5.2" = list(
						newSection = 2,
						takeRestValuesFrom = "7.1.5.2"
				),
				
				"7.2.5.3" = list(
						newSection = 3,
						takeRestValuesFrom = "7.1.5.3"
				),
				
				"7.2.5.4" = list(
						newSection = 4,
						takeRestValuesFrom = "7.1.5.4"
				),
				
				"7.2.6.1" = list(
						takeRestValuesFrom = "7.1.6.1"
				),
				
				"7.2.6.2" = list(
						takeRestValuesFrom = "7.1.6.2"
				),
				
				"7.2.6.3" = list(
						takeRestValuesFrom = "7.1.6.3"
				),
				
				"7.2.6.4" = list(
						takeRestValuesFrom = "7.1.6.4"
				),
				
				"7.2.7.1" = list(
						takeRestValuesFrom = "7.1.7.1"
				),
				
				"7.2.7.2" = list(
						takeRestValuesFrom = "7.1.7.2"
				),
				
				"7.2.7.3" = list(
						takeRestValuesFrom = "7.1.7.3"
				),
				
				"7.2.7.4" = list(
						takeRestValuesFrom = "7.1.7.4"
				),














#				"7.2.4.5" = list(
#						takeRestValuesFrom = "7.1.4.5"
#				),
#				
#				"7.2.4.6" = list(
#						takeRestValuesFrom = "7.1.4.6"
#				),
#				
#				"7.2.4.7" = list(
#						takeRestValuesFrom = "7.1.4.7"
#				),
#				
#				"7.2.4.8" = list(
#						takeRestValuesFrom = "7.1.4.8"
#				),
#				
#				"7.2.4.9" = list(
#						takeRestValuesFrom = "7.1.4.9"
#				),
#				
#				"7.2.4.10" = list(
#						takeRestValuesFrom = "7.1.4.10"
#				),

				"8.1.3.1" = list(
						newSection = 1,
						title = "Average intensity",
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						text = ""
				),
				
				"8.1.3.2" = list(
						newSection = 4,
						title = "Intensity sum",
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.1.3.3" = list(
						newSection = 5,
						title = "Plant weight",
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.1.3.4" = list(
						newSection = 5,
						title = "Weight loss by drought",
						takeRestValuesFrom = "8.1.3.1"
				),
				
				
				"8.1.4.1" = list(
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.1.4.2" = list(
						takeRestValuesFrom = "8.1.3.2"
				),
				
				"8.1.4.3" = list(
						takeRestValuesFrom = "8.1.3.3"
				),
				
				"8.1.4.4" = list(
						takeRestValuesFrom = "8.1.3.4"
				),
				
				"8.1.5.1" = list(
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.1.5.2" = list(
						takeRestValuesFrom = "8.1.3.2"
				),
				
				"8.1.5.3" = list(
						takeRestValuesFrom = "8.1.3.3"
				),
				
				"8.1.5.4" = list(
						takeRestValuesFrom = "8.1.3.4"
				),
				
				
				"8.2.3.1" = list(
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.2.3.2" = list(
						takeRestValuesFrom = "8.1.3.2"
				),
				
				"8.2.3.3" = list(
						takeRestValuesFrom = "8.1.3.3"
				),
				
				"8.2.3.4" = list(
						takeRestValuesFrom = "8.1.3.4"
				),
				
				"8.2.3.1" = list(
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.2.4.1" = list(
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.2.4.2" = list(
						takeRestValuesFrom = "8.1.3.2"
				),
				
				"8.2.4.3" = list(
						takeRestValuesFrom = "8.1.3.3"
				),
				
				"8.2.4.4" = list(
						takeRestValuesFrom = "8.1.3.4"
				),
				
				"8.2.5.1" = list(
						takeRestValuesFrom = "8.1.3.1"
				),
				
				"8.2.5.2" = list(
						takeRestValuesFrom = "8.1.3.2"
				),
				
				"8.2.5.3" = list(
						takeRestValuesFrom = "8.1.3.3"
				),
				
				"8.2.5.4" = list(
						takeRestValuesFrom = "8.1.3.4"
				),
				
				
				"11.1.1.1" = list(
						newSection = 1,
						typOfReset = RESET.PAGE.NO,
						typOfClear = CLEAR.PAGE.NO,
						title = "Number of leafs",
						text = ""
				),		
				
				"11.1.1.2" = list(
						newSection = 2,
						title = "Average leaf length",
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.1.1.3" = list(
						newSection = 3,
						title = "Leaf lengths sum",
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.1.1.4" = list(
						newSection = 4,
						title = "Average leaf width",
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.1.1.5" = list(
						newSection = 5,
						title = "Whole max leaf width",
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.1.1.6" = list(
						newSection = 6,
						title = "Outer region max leaf width",
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.2.1.1" = list(
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.2.1.2" = list(
						takeRestValuesFrom = "11.1.1.2"
				),
				
				"11.2.1.3" = list(
						takeRestValuesFrom = "11.1.1.3"
				),
				
				"11.2.1.4" = list(
						takeRestValuesFrom = "11.1.1.4"
				),
				
				"11.2.1.5" = list(
						takeRestValuesFrom = "11.1.1.5"
				),
				
				"11.2.1.6" = list(
						takeRestValuesFrom = "11.1.1.6"
				),
				
				"11.1.2.1" = list(
						newSection = 1,
						title = "Flower detection",
						takeRestValuesFrom = "11.1.1.1"
				),
				
				"11.2.2.1" = list(
						takeRestValuesFrom = "11.1.2.1"
				)
		)
)