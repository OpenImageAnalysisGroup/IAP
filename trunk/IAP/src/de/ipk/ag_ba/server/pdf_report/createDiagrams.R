# Author: Entzian, Klukas
###############################################################################


source("inc.R")

parMakeLinearDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="nboxplot") {
	########
#overallResult <- overallList$overallResult_nBoxDes
#overallDescriptor <- overallList$nBoxDes
#overallColor <- overallList$color_nBox
#overallDesName <-overallList$nBoxDesName
#overallFileName <- overallList$imageFileNames_nBoxplots
#diagramTypSave="nboxplot"
#imagesIndex <- "1"
	#############
	
	overallList$debug %debug% "makeLinearDiagram()"	
	
	#tempOverallResult =  na.omit(overallResult)
	
	tempOverallResult =  overallResult	
	
		
	
	cluster <- NULL
	tryCatch( cluster <- makeCluster(c(rep.int("localhost", times=6))),
			error = function(e) {
			ownCat("... could not create the cluster")
		} )
		
	for (imagesIndex in names(overallDescriptor)) {	
		if(!is.null(cluster)) {
			clusterCall(cluster, makeLinearDiagram, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="nboxplot", imagesIndex=imagesIndex, tempOverallResult=tempOverallResult)
		} else {
			makeLinearDiagram(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="nboxplot", imagesIndex=imagesIndex, tempOverallResult=tempOverallResult)
		}
	}
	stopCluster(cluster)
}

parMakeBoxplotStackedDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList) {
	overallList$debug %debug% "makeBoxplotStackedDiagram()"
	#overallResult[is.na(overallResult)] = 0
	tempOverallResult =  na.omit(overallResult)
	#tempOverallResult = overallResult
	
	cluster <- NULL
	tryCatch( cluster <- makeCluster(c(rep.int("localhost", times=6))),
			error = function(e) {
			ownCat("... could not create the cluster")
		} )


	for (imagesIndex in names(overallDescriptor)) {
		createOuputOverview("stacked barplot", imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
		overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "boxplotstacked")
		if (length(overallResult[, 1]) > 0) {
			if(!is.null(cluster)) {
				clusterCall(cluster, PreWorkForMakeBigOverallImage, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex)
			} else {
				PreWorkForMakeBigOverallImage(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex)
			}
		}
	}
	
	stopCluster(cluster)
}	


makeLinearDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="nboxplot", imagesIndex, tempOverallResult) {

		source("inc.R")	

		if (!is.na(overallDescriptor[[imagesIndex]])) {
			ylabelForAppendix <- ""
			createOuputOverview("line plot", imagesIndex, length(names(overallDescriptor)),  overallDesName[[imagesIndex]])
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "nboxplot")
			overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
			overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0

			overallResult <-  replaceTreatmentNamesOverall(overallList, overallResult)	

			if (length(overallResult[, 1]) > 0) {
				
				if (!CheckIfOneColumnHasOnlyValues(overallResult)) {

					plot <-	ggplot(data=overallResult, aes(x=xAxis, y=mean, shape=name)) 
							#geom_smooth(aes(ymin=mean-se, ymax=mean+se, colour=name, fill=name), stat="identity", alpha=0.1) +
					
					if(length(grep("%/day",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0 || overallList$isRatio || length(grep("relative",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0 || length(grep("average",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0) {
						plot <- plot + geom_smooth(aes(ymin=mean-se, ymax=mean+se, colour=name, fill=name), method="loess", stat="smooth", alpha=0.1)
						#plot <- plot + geom_ribbon(aes(ymin=mean-se, ymax=mean+se, fill=name), alpha=0.1)
					} else {
						plot <- plot + geom_ribbon(aes(ymin=mean-se, ymax=mean+se, fill=name), stat="identity", alpha=0.1) +
								geom_line(aes(color=name), alpha=0.2)
					}
							
					plot <- plot +	
							geom_point(aes(color=name), size=3) +
#							ownCat("drinne")
#							ownCat(overallResult$xAxis)
#							ownCat(min(as.numeric(as.character(overallResult$xAxis))))
#							ownCat(max(as.numeric(as.character(overallResult$xAxis))))
#							plot <-  plot + 
							scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis))))					
							
							if(overallList$appendix) {
								ylabelForAppendix <- renameY(overallDesName[[imagesIndex]])
								plot <- plot + ylab(ylabelForAppendix)
							} else {
								plot <- plot + ylab(overallDesName[[imagesIndex]])
							}
							
										
						plot <- plot +
							scale_fill_manual(values = overallColor[[imagesIndex]]) +
							scale_colour_manual(values= overallColor[[imagesIndex]]) +
							scale_shape_manual(values = c(1:length(overallColor[[imagesIndex]]))) +
							theme_bw() +
							opts(axis.title.x = theme_text(face="bold", size=11), 
									axis.title.y = theme_text(face="bold", size=11, angle=90), 
									#panel.grid.major = theme_blank(), # switch off major gridlines
									#panel.grid.minor = theme_blank(), # switch off minor gridlines
									legend.position = "right", # manually position the legend (numbers being from 0, 0 at bottom left of whole plot to 1, 1 at top right)
									legend.title = theme_blank(), # switch off the legend title						
									legend.key.size = unit(1.5, "lines"), 
									legend.key = theme_blank(), # switch off the rectangle around symbols in the legend
									panel.border = theme_rect(colour="Grey", size=0.1)
							)
					
					if (length(overallColor[[imagesIndex]]) > 18 & length(overallColor[[imagesIndex]]) < 31) {
						plot = plot + opts(legend.text = theme_text(size=6),
										   legend.key.size = unit(0.7, "lines"),
										   strip.text.x = theme_text(size=6)
											)
					} else if(length(overallColor[[imagesIndex]]) >= 31) {
						plot = plot + opts(legend.text = theme_text(size=4),
										   legend.key.size = unit(0.4, "lines"),
										   strip.text.x = theme_text(size=4)
						)
					} else {
						plot = plot + opts(legend.text = theme_text(size=11),
										   strip.text.x = theme_text(size=11))
					}
					
					#Überlegen ob das sinn macht!!
					#if(length(unique(overallResult$name)) > 18) {
						if ("primaerTreatment" %in% colnames(overallResult)) {				
							plot = plot + facet_wrap(~ primaerTreatment)
						} else {
							plot = plot + facet_wrap(~ name)
						} 
					#}
					
				
								
					print(plot)
		
		##!# nicht löschen, ist die interpolation (alles in dieser if Abfrage mit #!# makiert)
		##!#				newCoords = seq(min(overallList$filterXaxis, na.rm=TRUE), max(overallList$filterXaxis, na.rm=TRUE), 1)
		##!#				newValue = approx(overallList$filterXaxis, overallList$overallResult[y, ], xout=newCoords, method="linear")
		##!#				
		##!#				naVector = is.na(overallList$overallResult[y, ])
		##!#				overallResultWithNaValues = overallList$overallResult[y, ]
		##!#				overallList$overallResult[y, naVector] = newValue$y[overallList$filterXaxis[naVector]]
		#				
		#				if (firstPlot) {
		#					firstPlot = FALSE
		##!#				plot(overallList$filterXaxis, overallList$overallResult[y, ], main="", type="c", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult, na.rm=TRUE), max(overallList$overallResult, na.rm=TRUE)))
		#					plot(overallList$filterXaxis, overallList$overallResult[y, ], main="", type="b", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult, na.rm=TRUE), max(overallList$overallResult, na.rm=TRUE)))
		#				} else {
		##!#				points(overallList$filterXaxis, overallList$overallResult[y, ], type="c", col=overallList$color[y], pch=y, lty=1, lwd=3 )	
		#					points(overallList$filterXaxis, overallList$overallResult[y, ], type="b", col=overallList$color[y], pch=y, lty=1, lwd=3 )
		#				}
		##!#				points(overallList$filterXaxis, overallResultWithNaValues, type="p", col=overallList$color[y], pch=y, lty=1, lwd=3 )
		#			} 

					writeTheData(overallList, plot, overallFileName[[imagesIndex]], diagramTypSave, isAppendix=overallList$appendix, subSectionTitel=ylabelForAppendix, subsectionDepth=1)
	
	
#					saveImageFile(overallList, plot, overallFileName[[imagesIndex]], diagramTypSave)
#
#					if (overallList$appendix) {
#						writeLatexFile("appendixImage", overallFileName[[imagesIndex]], diagramTypSave, TRUE, ylabelForAppendix)
#					}	
				} else {
					ownCat("Only one column has values, create barplot!")
			
					day = overallResult$xAxis[!is.na(overallResult$mean)][1]
					tempXaxisName = overallList$xAxisName
					overallList$xAxisName = paste(overallList$xAxisName, day)
					#overallList$overallResult = overallList$overallResult[!is.na(overallList$overallResult$mean), ]
					makeBarDiagram(overallResult, overallDescriptor[imagesIndex], overallColor[imagesIndex], overallDesName[imagesIndex], overallFileName[imagesIndex], overallList, TRUE, diagramTypSave)
					overallList$xAxisName = tempXaxisName
				}
			}
		}

}

PreWorkForMakeBigOverallImage <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex) {

	source("inc.R")

	overallList$debug %debug% "PreWorkForMakeBigOverallImage()"	
		
	groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
	if (length(groupBy) == 0 || length(groupBy) == 1) {
		plotStackedImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende=TRUE, minor_breaks=FALSE, overallColor = overallColor, overallDesName = overallDesName, imagesIndex= imagesIndex, overallFileName =overallFileName)
	} else {
		for (value in overallList$filterSecondTreatment) { 
			title = overallList$secondFilterTreatmentRename[[value]]
			#ownCat(title)
			plottedName = overallList$filterTreatment %contactAllWithAll% value
			booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = plottedName))
			plotThisValues = overallResult[booleanVector, ]
			#overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name)
		#	plotThisValues = reNameColumn(plotThisValues, "name", "primaerTreatment")
			plotStackedImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende=TRUE, minor_breaks=FALSE, overallColor = overallColor, overallDesName = overallDesName, imagesIndex=imagesIndex, overallFileName=overallFileName)
		}	 
	}
}

PreWorkForMakeNormalImages <- function(h, overallList) {
	overallList$debug %debug% "PreWorkForMakeNormalImages()"
	stackedImages = unlist(unique(overallList$overallResult["name"]))
	
	for (o in stackedImages) {
		overallList$debug %debug% paste("makeBoxplotStackedDiagram with the descriptor: ", overallList$fileName, o)
		plotThisValues = overallList$overallResult[overallList$overallResult["name"] == o, ]
		plotStackedImage(h, overallList, plotThisValues, o, FALSE, TRUE, TRUE)
	}
}




makeBoxplotStackedDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList) {
	overallList$debug %debug% "makeBoxplotStackedDiagram()"
	#overallResult[is.na(overallResult)] = 0
	tempOverallResult =  na.omit(overallResult)
	#tempOverallResult = overallResult
	
	for (imagesIndex in names(overallDescriptor)) {
		createOuputOverview("stacked barplot", imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
		overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "boxplotstacked")
		if (length(overallResult[, 1]) > 0) {
			PreWorkForMakeBigOverallImage(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex)
		}
	}
}	


removeNAsSpider <- function(overallResult, xAxisPosition) {
	overallResultStart <- overallResult[1:xAxisPosition]
	overallResult <- overallResult[(xAxisPosition+1):length(colnames(overallResult))]
	booleanVector <- !apply(overallResult,1,function(x)all(is.na(x)))
	
	return(cbind(overallResultStart[booleanVector,], overallResult[booleanVector,]))
}


openPlotDevice <- function(overallList, fileName, extraString, h) {
	if(h==1) {
		filename = preprocessingOfValues(paste(fileName, extraString, sep=""), FALSE, replaceString = "_")
		Cairo(width=10, height=7, file=paste(filename, overallList$saveFormat, sep="."), type=overallList$saveFormat, bg="transparent", units="in", dpi=as.numeric(overallList$dpi))
	}
}

closePlotDevice <- function(h) {
	if(h==1) {
		dev.off()
	}
}

transferIntoPercentValues <- function(overallResult, xAxisPosition) {
	overallResultCalulate <- overallResult[(xAxisPosition+1):(length(colnames(overallResult))-1)]
	overallResultCalulate <- (overallResultCalulate * 100) / max(overallResultCalulate)
	
	return(data.frame(overallResult[c(1:xAxisPosition)], overallResultCalulate, overallResult[length(colnames(overallResult))]))
}

normalizeEachDescriptor <- function(overallResult) {

	for(name in unique(overallResult$hist)) {
		overallResult[overallResult$hist == name,]$values <- sapply(overallResult[overallResult$hist == name,]$values,  function(x,y) {(x/y)}, y=max(overallResult[overallResult$hist == name,]$values))
	}
	return(overallResult)
}

makeSpiderPlotDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, options, overallList, diagramTypSave="spiderplot") {
	################
#	overallResult <- overallList$overallResult_boxSpiderDes
#	overallDescriptor <- overallList$boxSpiderDes
#	overallColor <- overallList$color_spider
#	overallDesName <- overallList$boxSpiderDesName
#	overallFileName <- overallList$imageFileNames_SpiderPlots
#	options <- overallList$spiderOptions
#	diagramTypSave <- "spiderplot"
#	imagesIndex <- "1"
	####################	
	
	overallList$debug %debug% "makeSpiderPlotDiagram()"

	tempOverallResult =  na.omit(overallResult)
	
	for (imagesIndex in names(overallDescriptor)) {
		createOuputOverview("spider/linerange plot", imagesIndex, length(names(overallDescriptor)), getVector(overallDesName[[imagesIndex]]))
		overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, diagramTypSave)
		
		if (length(overallResult[, 1]) > 0) {
			test <- c("side fluo intensity", "side nir intensity", "side visible hue average value", "top visible hue average value")
			if(sum(!getVector(overallDesName[[imagesIndex]]) %in% test) > 1) {
				doSpiderPlot <- TRUE
			} else {
				doSpiderPlot <- FALSE
			}
			PreWorkForMakeBigOverallImageSpin(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex, options, diagramTypSave, doSpiderPlot)		
		}
	}
}	


PreWorkForMakeBigOverallImageSpin <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex, options, diagramTypSave, doSpiderPlot) {
	overallList$debug %debug% "PreWorkForMakeBigOverallImageSpin()"	
	
	overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult$xAxis, options)
	overallResult <- na.omit(overallResult)
	overallResult <- normalizeEachDescriptor(overallResult)	
	groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
	
	if (length(groupBy) == 0 || length(groupBy) == 1) {
		
		if(doSpiderPlot) {
			plotSpiderImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex= imagesIndex, overallFileName =overallFileName, diagramTypSave=diagramTypSave)	
		}
		plotLineRangeImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex= imagesIndex, overallFileName =overallFileName, diagramTypSave="lineRangePlot")	
	} else {
		for (value in overallList$filterSecondTreatment) {			
			title = overallList$secondFilterTreatmentRename[[value]]
			plottedName = overallList$filterTreatment %contactAllWithAll% value
			booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = plottedName))
			plotThisValues = overallResult[booleanVector, ]
#			usedOverallColor <- overallColor[[imagesIndex]][1:length(unique(plotThisValues["primaerTreatment"])[,1])]
#			overallColor[[imagesIndex]] <- overallColor[[imagesIndex]][(length(unique(plotThisValues["primaerTreatment"])[,1])+1):length(overallColor[[imagesIndex]])]
			
			if(doSpiderPlot) {
				plotSpiderImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex=imagesIndex, overallFileName=overallFileName, diagramTypSave=diagramTypSave)
			}
			plotLineRangeImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex=imagesIndex, overallFileName=overallFileName, diagramTypSave="lineRangePlot")
		}	 
	}
}




plotSpiderImage <- function(overallList, overallResult, title = "", makeOverallImage = FALSE, legende=TRUE, usedoverallColor, overallDesName, imagesIndex, overallFileName, diagramTypSave) {
################
##overallColor <- usedOverallColor 
#
#	makeOverallImage = TRUE
#	legende=TRUE
#	usedoverallColor = overallColor[[imagesIndex]]
#	overallResult <- plotThisValues
#	positionType <- overallList$spiderOptions$typOfGeomBar[1]
#################

#tempoverallResult <- overallResult
#overallResult <- tempoverallResult
	overallList$debug %debug% "plotSpiderImage()"	
	if (length(overallResult[, 1]) > 0) {		
		for (positionType in overallList$spiderOptions$typOfGeomBar) {			
 
			if ("primaerTreatment" %in% colnames(overallResult)) {
				overallResult$primaerTreatment <-  replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
				
				plot = ggplot(data=overallResult, aes(x=hist, y=values, group=primaerTreatment)) +
						geom_point(aes(color=as.character(primaerTreatment), shape=hist), size=3) +
						geom_line(aes(colour=as.character(primaerTreatment))) 
				
			} else {
				overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name, TRUE)
				
				plot = ggplot(data=overallResult, aes(x=hist, y=values, group=name)) +
						geom_point(aes(color=as.character(name), shape=hist), size=3) +
						geom_line(aes(colour=as.character(name))) 
			}
			plot <- plot +
					#geom_point(aes(color=as.character(name), shape=hist), size=3) +
					scale_shape_manual(values = c(1:length(unique(overallResult$hist))), name="Property") +
					#geom_line(aes(colour=as.character(name))) +
					scale_colour_manual(name="Condition", values=usedoverallColor)

			if (positionType == "x") {			
				#plot <- plot + coord_polar(theta="x", expand=TRUE)
				plot <- plot + coord_polar(theta="x")
			} else {
				#plot <- plot + coord_polar(theta="y", expand=TRUE)
				plot <- plot + coord_polar(theta="y")
			}
				
				plot <- plot + 
						scale_y_continuous() +
						theme_bw() +
						opts(#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), # Rand geht nicht in ggplot 0.9
								axis.title.x = theme_blank(), 
								axis.title.y = theme_blank(),
#								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1),
								axis.text.x = theme_blank()
								#axis.text.y = theme_blank()
						) 
			if (positionType == "y") {
				plot <- plot + 
						opts(axis.text.y = theme_blank(),
								axis.ticks	= theme_blank()	
						)
			}	
				
			if (!legende) {
				plot = plot + opts(legend.position="none")
			} else {
				plot = plot + 
					   opts(#legend.justifiownCation = 'bottom', 
							  # legend.direction="vertical",
							   legend.position="bottom",
							   #legend.position=c(0.5,0),
							  # legend.title = theme_blank(),
							   legend.key = theme_blank()
			   			)
				#if(as.numeric(sessionInfo()[1]$R.version$minor) > 13 & as.numeric(sessionInfo()[1]$R.version$major) > 1) {
				if(sessionInfo()$otherPkgs$ggplot2$Version != "0.8.9") {
						
					plot <-  plot + guides(col=guide_legend(ncol=calculateLegendRowAndColNumber(unique(overallResult$name), "Condition"), byrow=T)) 
					plot <-  plot + guides(shape=guide_legend(ncol=calculateLegendRowAndColNumber(unique(overallResult$hist), "Property"), byrow=T))
					
				
#				if (numberOfHist > 3 & numberOfHist < 10) {
#					plot = plot + opts(legend.text = theme_text(size=6), 
#							legend.key.size = unit(0.7, "lines")
#					)
#				} else if(numberOfHist >= 10) {
#					plot = plot + opts(legend.text = theme_text(size=5), 
#							legend.key.size = unit(0.3, "lines")
#					)
#				} else {
#					plot = plot + opts(legend.text = theme_text(size=11))
#				}
				}
			}		
			
#			if (title != "") {
#				plot = plot + opts(title = title)
#			}
			
			if (makeOverallImage) {
				plot = plot + facet_grid(~ xAxisfactor)
#				if ("primaerTreatment" %in% colnames(overallResult)) {				
#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
#					
#				} else {
#					plot = plot + facet_grid(name ~ xAxisfactor)
#				}
			}
			print(plot)
			
			subtitle <- ""
			if(positionType == overallList$spiderOptions$typOfGeomBar[1] || length(overallList$spiderOptions$typOfGeomBar) == 1) {
				subtitle <- title
			}

			writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(diagramTypSave, title, positionType, sep=""), paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep=""), paste(overallFileName[[imagesIndex]], diagramTypSave, title, positionType, sep=""), subtitle, makeOverallImage, subsectionDepth=2)
																													
#			saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste(diagramTypSave, title, positionType, sep=""))
#			if (makeOverallImage) {
#				if(title != "") {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep=""), paste(overallFileName[[imagesIndex]], diagramTypSave, title, positionType, sep=""), TRUE, title)	
#				} else {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep=""), paste(overallFileName[[imagesIndex]], diagramTypSave, title, positionType, sep=""))
#				}
#			} else {
#				writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], diagramTypSave, positionType, title, sep=""))	
#			}

		}
	}				
}

calculateLegendRowAndColNumber <- function(legendText, heading) {
########	
#legendText <- unique(overallResult$name)
#######	
	lengthOfOneRow <- 90
	legendText <- as.character(legendText)
	
	averageLengthOfSet <- round(sum(nchar(legendText),na.rm=TRUE) / length(legendText))

	ncol <- floor(lengthOfOneRow / averageLengthOfSet)
	if(ncol == 0) {
		ncol <- 1
	}
	return(ncol)
} 

plotLineRangeImage <- function(overallList, overallResult, title = "", makeOverallImage = FALSE, legende=TRUE, usedoverallColor, overallDesName, imagesIndex, overallFileName, diagramTypSave) {
	################
#	makeOverallImage = TRUE
#	legende=TRUE
#	usedoverallColor <- overallColor[[imagesIndex]]
#	overallResult <- plotThisValues
#	positionType <- overallList$spiderOptions$typOfGeomBar[1]
#	diagramTypSave <- "lineRangePlot"
	#################
	
	#ownCat(overallResult[1,])
#tempoverallResult <- overallResult
#overallResult <- tempoverallResult
	overallList$debug %debug% "plotLineRangeImage()"	
	if (length(overallResult[, 1]) > 0) {		
		if ("primaerTreatment" %in% colnames(overallResult)) {
			overallResult$primaerTreatment <-  replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
		} else {
			overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name, TRUE)
		}
		
		
		plot <- ggplot(data=overallResult, aes(x=hist, y=values)) +
				geom_line()
		
		if ("primaerTreatment" %in% colnames(overallResult)) {				
			plot <- plot + geom_point(aes(color=as.character(primaerTreatment)), size=3)
			
		} else {
			plot <- plot + geom_point(aes(color=as.character(name)), size=3)
		}
		
		plot <- plot +
				scale_colour_manual(values=usedoverallColor) +
				scale_y_continuous() +
				theme_bw() +
				opts(#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), # Rand geht nicht in ggplot 0.9
						axis.title.x = theme_blank(), 
						axis.title.y = theme_blank(),
						axis.text.x = theme_text(angle=90),
						panel.grid.minor = theme_blank(), 
						panel.border = theme_rect(colour="Grey", size=0.1)
						#axis.text.x = theme_blank()
				#axis.text.y = theme_blank()
				) 
		
		if (!legende) {
			plot = plot + opts(legend.position="none")
		} else {
			plot = plot + 
					opts(#legend.justifiownCation = 'bottom', 
							#legend.direction="horizontal",
							legend.position="bottom",
							#legend.position=c(0.5,0),
							legend.title = theme_blank(),
							legend.key = theme_blank()
					)
			
			if(sessionInfo()$otherPkgs$ggplot2$Version != "0.8.9") {
				
				nRowCrowList <- calculateLegendRowAndColNumber(unique(overallResult$hist))

				plot <-  plot + guides(col=guide_legend(nrow=nRowCrowList$nrow, ncol=nRowCrowList$ncol, byrow=T)) 
			}
				
			
#			if (length(overallColor[[imagesIndex]]) > 3 & length(overallColor[[imagesIndex]]) < 6) {
#				size <- 6
#				unit <- 0.7
#			} else if(length(overallColor[[imagesIndex]]) >= 6) {
#				size <- 5
#				unit <- 0.4
#			} else {
#				size <- 11
#				unit <- 1.0
#			}
#			
#			plot = plot + opts(legend.text = theme_text(size=size), 
#					legend.key.size = unit(unit, "lines"),
#					axis.text.x = theme_text(face="bold", size=size, angle=90)
#			)
		}		
		
		if (title != "") {
			plot = plot + opts(title = title)
		}
		
		if (makeOverallImage) {
			plot = plot + facet_grid(~ xAxisfactor)
#				if ("primaerTreatment" %in% colnames(overallResult)) {				
#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
#					
#				} else {
#					plot = plot + facet_grid(name ~ xAxisfactor)
#				}
		}
				
		writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(diagramTypSave, title, sep=""), paste(overallFileName[[imagesIndex]], "lineRangeOverallImage", sep=""), paste(overallFileName[[imagesIndex]], diagramTypSave, title, sep=""), title, makeOverallImage, subsectionDepth=2)

#		saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste(diagramTypSave, title, sep=""))
#		if (makeOverallImage) {
#			writeLatexFile(paste(overallFileName[[imagesIndex]], "lineRangeOverallImage", sep=""), paste(overallFileName[[imagesIndex]], diagramTypSave, title, sep=""))	
#		} else {
#			writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], diagramTypSave, title, sep="_"))	
#		}			
		
	}		
}

makeBarDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, isOnlyOneValue = FALSE, diagramTypSave="barplot") {
	########
#overallResult <- overallList$overallResult_nBoxDes
#overallDescriptor <- overallList$nBoxDes
#overallColor <- overallList$color_nBox
#overallDesName <-overallList$nBoxDesName
#overallFileName <- overallList$imageFileNames_nBoxplots
#diagramTypSave="nboxplot"
#imagesIndex <- "1"
#isOnlyOneValue <- FALSE
	#############	
	
	
	overallList$debug %debug% "makeBarDiagram()"

	tempOverallResult =  overallResult

	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {	
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "barplot")
			overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
			overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0
			overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name)
			
			if (length(overallResult[, 1]) > 0) {
				if (isOnlyOneValue) {
					plot = ggplot(data=overallResult, aes(x=name, y=mean))
				} else {
					plot = ggplot(data=overallResult, aes(x=xAxis, y=mean))
				}
				
				maxMean <- max(overallResult$mean)
				maxSe <- max(overallResult$se)
				
				plot = plot + 						
						geom_bar(stat="identity", aes(fill=name), colour="Grey", size=0.1) +
						geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.2, colour="black")+
						#geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.5, colour="Pink")+
						ylab(overallDesName[[imagesIndex]]) +
						coord_cartesian(ylim=c(0, maxMean + maxSe + (110*maxMean)/100)) +
						xlab(overallList$xAxisName) +
						scale_fill_manual(values = overallColor[[imagesIndex]]) +
						theme_bw() +
						opts(legend.position="none", 
								#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
								axis.title.x = theme_text(face="bold", size=11), 
								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								axis.text.x = theme_text(angle=90),
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1)
						)
				
				writeTheData(overallList, plot, overallFileName[[imagesIndex]], diagramTypSave, title, makeOverallImage, isAppendix=overallList$appendix)										
			}
		}
	}
}


reownCategorized <- function(overallResult) {
	
#	column <- "name"
#	
#	if ("primaerTreatment" %in% colnames(overallResult)) {	
#		column <- "primaerTreatment"
#		ownList <- list(primaerTreatment = character())
#	} else {
#		ownList <- list(name = character())
#	}
	
	overallResult <- cbind(overallResult, group=rbind(-1))
	overallResultTemp <- overallResult
	
	for(n in as.character(unique(unlist(overallResultTemp$name)))) {
		
	#	ownList[1] <- n
		booleanVector = getBooleanVectorForFilterValues(overallResultTemp, list(name=n))
		overallResult <- overallResultTemp[booleanVector,]
		
		lin_interp = function(x, y, length.out=length(overallResult$xAxis)) {
			approx(x, y, xout=seq(min(x), max(x), length.out=length.out))$y
		}
		
		overallResult$xAxis <- lin_interp(overallResult$xAxis, overallResult$xAxis)
		overallResult$mean <- lin_interp(overallResult$xAxis, overallResult$mean)
		
		ownCatRle = rle(overallResult$mean < 0)
		overallResult$group = rep.int(1:length(ownCatRle$lengths), times=ownCatRle$lengths)
		overallResultTemp[booleanVector, ] <- overallResult
	}
	
	return(overallResultTemp)
	#return(overallResult)
}


setColorDependentOfGroup <- function(overallResult) {
	
	lastColorPositiv <- ifelse(overallResult$mean[1] < 0, TRUE, FALSE)
	color <- vector()
	for(n in 1:length(unique(overallResult$group))) {
		if(lastColorPositiv) {
			color <- c(color, "light gray")
			lastColorPositiv <- FALSE
		} else {
			color <- c(color, "green")
			lastColorPositiv <- TRUE
		}
	}
	return(color)
}

makeViolinPlotDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="violinplot") {
	########
#overallResult <- overallList$overallResult_violinBoxDes
#overallDescriptor <- overallList$violinBoxDes
#overallColor <- overallList$color_violin
#overallDesName <-overallList$violinBoxDesName
#overallFileName <- overallList$imageFileNames_violinPlots
#diagramTypSave="violinplot"
#imagesIndex <- "1"
#isOnlyOneValue <- FALSE
	#############	
	
	
	
	overallList$debug %debug% "makeViolinPlotDiagram()"	
	
	if ("primaerTreatment" %in% colnames(overallResult)) {
		overallResult[,4:length(colnames(overallResult))] <- 1-overallResult[,4:length(colnames(overallResult))]
	} else {
		overallResult[,3:length(colnames(overallResult))] <- 1-overallResult[,3:length(colnames(overallResult))]
	}
	
	tempOverallResult =  overallResult	
	
	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {
			createOuputOverview("violin plot", imagesIndex, length(names(overallDescriptor)),  overallDesName[[imagesIndex]])
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, diagramTypSave)
			overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
			
			if("primaerTreatment" %in% colnames(overallResult)) {
				
				for (value in unique(as.character(overallResult$primaerTreatment))) { 
					title = overallList$filterTreatmentRename[[value]]			
					booleanVector = getBooleanVectorForFilterValues(overallResult, list(primaerTreatment = value))
					plotThisValues = overallResult[booleanVector, ]
					plotThisValues$name <- factor(substr(plotThisValues$name,nchar(value)+2, nchar(as.character(plotThisValues$name))))
					plotViolinPlotDiagram(plotThisValues, overallDesName, overallFileName, overallList, imagesIndex, title)
				}	 
				
			} else {
					plotViolinPlotDiagram(overallResult, overallDesName, overallFileName, overallList, imagesIndex)
			}
		}
	}
	
}

reorderThePlotOrder <- function(overallResult) {
	groupedOverallResult <- data.table(overallResult)
	sumVector <- as.data.frame(groupedOverallResult[, lapply(list(mean), sum, na.rm=TRUE), by=c(name)])
	sumVector$c <- levels(overallResult$name)
	
	for(n in levels(overallResult$name)) {
		overallResult$name <- replace(as.character(overallResult$name), overallResult$name==n, paste(sumVector[sumVector$c==n,]$c, " (", round(sumVector[sumVector$c==n,]$V1, digits=1), ")", sep=""))
		sumVector[sumVector$c==n,]$c <- paste(sumVector[sumVector$c==n,]$c, " (", round(sumVector[sumVector$c==n,]$V1, digits=1), ")", sep="")
	}
	
	return(factor(overallResult$name, levels = sumVector[order(sumVector$V1),]$c))
}


plotViolinPlotDiagram <- function(overallResult, overallDesName, overallFileName, overallList, imagesIndex, title="", diagramTypSave="violinplot") {
########
#overallResult <- plotThisValues	
########
	overallResult <- reownCategorized(overallResult)
	color <- setColorDependentOfGroup(overallResult)
	overallResult$name <- replaceTreatmentNames(overallList, overallResult$name,onlySecondTreatment = TRUE)
	overallResult$name <- reorderThePlotOrder(overallResult)
		
	if (length(overallResult[, 1]) > 0) {
						
		plot <-	ggplot(data=overallResult, aes(x=xAxis, fill=mean>=0, group=group)) +				
				geom_ribbon(aes(ymin=-mean, ymax=mean)) +						
				scale_fill_manual(values = color) +
				guides(fill=FALSE) +
				coord_flip()+
				scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis)))) +					
				ylab(overallDesName[[imagesIndex]])			+	
				#scale_fill_manual(values = overallColor[[imagesIndex]]) +
				#scale_colour_manual(values= overallColor[[imagesIndex]]) +
				theme_bw() +
				opts(axis.title.x = theme_text(face="bold", size=11), 
						axis.title.y = theme_text(face="bold", size=11, angle=90), 
						#panel.grid.major = theme_blank(), # switch off major gridlines
						#panel.grid.minor = theme_blank(), # switch off minor gridlines
						legend.position = "right", # manually position the legend (numbers being from 0, 0 at bottom left of whole plot to 1, 1 at top right)
						legend.title = theme_blank(), # switch off the legend title						
						#legend.key.size = unit(1.5, "lines"), 
						legend.key = theme_blank(), # switch off the rectangle around symbols in the legend
						panel.border = theme_rect(colour="Grey", size=0.1)
				)
		if (title != "") {
			plot = plot + opts(title = title)
		}
		
		plot = plot + facet_wrap(~ name, ncol=5)
		writeTheData(overallList, plot, overallFileName[imagesIndex], diagramTypSave, writeLatexFileFirstValue= paste(overallFileName[imagesIndex], "violinOverallImage", sep=""), writeLatexFileSecondValue= paste(overallFileName[imagesIndex],diagramTypSave,sep=""), makeOverallImage=TRUE, subSectionTitel = overallDesName[[imagesIndex]], subsectionDepth=2)
	}
}


makeBoxplotDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, options, overallList, diagramTypSave="boxplot") {	
	overallList$debug %debug% "makeBoxplotDiagram()"
	tempOverallResult =  na.omit(overallResult)
	
	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {
			#ownCat(paste("Process ", overallDesName[[imagesIndex]]))
			createOuputOverview("boxplot", imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
			
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "boxplot")
			if (length(overallResult[, 1]) > 0) {
				overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult$xAxis, options)	
				overallResult$name <- replaceTreatmentNames(overallList, overallResult$name)
				#myPlot = ggplot(overallList$overallResult, aes(factor(name), value, fill=name, colour=name)) + 
				#myPlot = ggplot(overallResult, aes(factor(name), value, fill=name)) +
			
				plot = ggplot(overallResult, aes(factor(name), value, fill=name)) +
						geom_boxplot() +
						ylab(overallDesName[[imagesIndex]]) +
						#coord_cartesian(ylim=c(0, max(overallList$overallResult$mean + overallList$overallResult$se + 10, na.rm=TRUE))) +
						#xlab(paste(min(overallResult$xAxis), overallList$xAxisName, "..", max(overallResult$xAxis), overallList$xAxisName)) +
						scale_fill_manual(values = overallColor[[imagesIndex]]) +
						#stat_summary(fun.data = f, geom = "crossbar", height = 0.1, 	colour = NA, fill = "skyblue", width = 0.8, alpha = 0.5) +
						theme_bw() +
						opts(legend.position="none", 
								#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
								axis.title.x = theme_blank(), 
								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1)
								
						) +
						opts(axis.text.x = theme_text(size=6, angle=90)) +
						facet_wrap(~ xAxisfactor, drop=FALSE)
						
				writeTheData(overallList, plot, overallFileName[[imagesIndex]], diagramTypSave, isAppendix=overallList$appendix)
				
#				saveImageFile(overallList, plot, overallFileName[[imagesIndex]], diagramTypSave)
#
#				if (overallList$appendix) {
#					writeLatexFile("appendixImage", overallFileName[[imagesIndex]], diagramTypSave)
#				}					
			}
		}
	}
	#return(overallList)
}

makeDiagrams <- function(overallList) {
	overallList$debug %debug% "makeDiagrams()"
	if(!calculateNothing) {			

			if (sum(!is.na(overallList$nBoxDes)) > 0) {
				if (overallList$debug) {ownCat("nBoxplot...")}
				parMakeLinearDiagram(overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$color_nBox, overallDesName=overallList$nBoxDesName, overallList$imageFileNames_nBoxplots , overallList)
			} else {
				ownCat("All values for nBoxplot are 'NA'")
			}
			
			if (sum(!is.na(overallList$boxDes)) > 0) {
				if (overallList$debug) {ownCat("Boxplot...")}
				makeBoxplotDiagram(overallList$overallResult_boxDes, overallList$boxDes, overallList$color_box, overallDesName=overallList$boxDesName, overallList$imageFileNames_Boxplots, overallList$boxOptions, overallList)
			} else {
				ownCat("All values for Boxplot are 'NA'...")
			}
			
			if (sum(!is.na(overallList$boxStackDes)) > 0) {
				if (overallList$debug) {ownCat("Stacked Boxplot...")}
				parMakeBoxplotStackedDiagram(overallList$overallResult_boxStackDes, overallList$boxStackDes, overallList$color_boxStack, overallDesName=overallList$boxStackDesName, overallList$imageFileNames_StackedPlots, overallList)
			} else {
				ownCat("All values for stacked Boxplot are 'NA'...")
				}
				
			if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
					if (overallList$debug) {ownCat("Spider plot...")}
				makeSpiderPlotDiagram(overallList$overallResult_boxSpiderDes, overallList$boxSpiderDes, overallList$color_spider, overallDesName=overallList$boxSpiderDesName, overallList$imageFileNames_SpiderPlots, overallList$spiderOptions, overallList)
			} else {
				ownCat("All values for stacked Boxplot are 'NA'...")
			}

			if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
				if (overallList$debug) {ownCat("Violin plot...")}
				makeViolinPlotDiagram(overallList$overallResult_violinBoxDes, overallList$violinBoxDes, overallList$color_violin, overallDesName=overallList$violinBoxDesName, overallList$imageFileNames_violinPlots , overallList)
			} else {
				ownCat("All values for violin Boxplot are 'NA'...")
			}
			
			if (FALSE) {	# falls auch mal barplots erstellt werden sollen (ausser wenn nur ein Tag vorhanden ist!)
				if (overallList$debug) {ownCat("Barplot...")}
				makeBarDiagram(overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$color_nBox, overallDesName=overallList$nBoxDesName, overallList$imageFileNames_nBoxplots, overallList)
			}
	}
}

addDesSet <- function(descriptorSet_boxplotStacked, descriptorSetName_boxplotStacked, workingDataSet) {
	
	addDescSet = character()
	addDescSetNames = character()
	i = 0
	for (ds in descriptorSet_boxplotStacked) {
		addCol = ""
		#addColDesc = ""
		for (col in colnames(workingDataSet)) {	
			if (nchar(ds)>5) {
				last4chars = substr(col, nchar(ds)-4, nchar(ds))
				if (last4chars == ".bin.") {
					col_substring = substr(col, 1, nchar(ds))
					if (col_substring == ds) {
						if (nchar(addCol)>0) {
							addCol = paste(addCol, "$", sep="")
						}
						addCol= paste(addCol, col, sep="")
					} 
				}
			}
		}
		i=i+1
		addColDesc = descriptorSetName_boxplotStacked[i]
		if (nchar(addCol)>0) {
			#ownCat(paste("Adding ", addCol, "with description", addColDesc))
			addDescSet = c(addDescSet, addCol)
			addDescSetNames = c(addDescSetNames, addColDesc)
		}
	}
	
	if(length(addDescSet) > 0) {
		return(list(desSet=addDescSet, desName = addDescSetNames))
	} else {
		return(list(desSet=descriptorSet_boxplotStacked, desName = descriptorSetName_boxplotStacked))
	}
	#descriptorSet_boxplotStacked = c(descriptorSet_boxplotStacked, addDescSetNames) 	
	#return(descriptorSet_boxplotStacked)
}

changeXAxisName <- function(overallList) {
	
	if(length(overallList$iniDataSet$Day..Int.) > 1 ){
		day_int <- as.character(overallList$iniDataSet$Day..Int.[1])
		day <- as.character(overallList$iniDataSet$Day[1])
		overallList$xAxisName <- substr(day, 1, nchar(day)-(nchar(day_int)+1))
	}
	return(overallList)
}


checkIfAllNecessaryFilesAreThere <- function() {
		ownCat("Check if the noValues-Image is there")
		file = "noValues.pdf"
		if (!file.exists(file)) {
			library("Cairo")
			ownCat(paste("Create defaultImage '", file, "'", sep=""))
			Cairo(width=900, height=70, file=file, type="pdf", bg="transparent", units="px", dpi=90)
			par(mar = c(0, 0, 0, 0))
			plot.new()
			legend("left", "no values", col= c("black"), pch=1, bty="n")
			dev.off()
		}	
}

buildBlacklist <- function(workingDataSet, descriptorSet) {
	
	searchString = ".histogram."
#	searchString = paste(searchString, "mark", sep = "|")	
	additionalDescriptors = c(descriptorSet, "Day (Int)", "Day", "Time", "Plant ID", "vis.side", "fluo.side", "nir.side", "vis.top", "fluo.top", "nir.top")
	
	return(c(colnames(workingDataSet)[grep(searchString, colnames(workingDataSet), ignore.case = TRUE)], preprocessingOfValues(additionalDescriptors, TRUE)))
}

initRfunction <- function(DEBUG = FALSE) {
	#"LC_COLLATE=German_Germany.1252;LC_CTYPE=German_Germany.1252;LC_MONETARY=German_Germany.1252;LC_NUMERIC=C;LC_TIME=German_Germany.1252"
	#Sys.setlocale(locale="de_DE.ISO8859-15")
	#Sys.setlocale("LC_ALL", "en_US.UTF-8")
	
	if (DEBUG) {
		options(error = quote({
			#sink(file="error.txt", split = TRUE);
			dump.frames();
			ownCat(attr(last.dump, "error.message"));
			#x = attr(last.dump, "error.message")
			traceback();
			#sink(file=NULL);		
			#q()
		}))
		options(show.error.messages = TRUE)
		#options(showWarnCalls = TRUE)
		#options(showErrorCalls = TRUE)
		options(warn = 0)
		#options(warn = 2)
	} else {	
		options(error = NULL)
		#options(showWarnCalls = FALSE)
		#options(showErrorCalls = FALSE)
		options(warn = -1)
		options(show.error.messages = FALSE)
	}
	if(memory.limit() < 3500) {
		memory.limit(size=10000)
	}
	
	while(!is.null(dev.list())) {
		dev.off()
	}
	
	#"psych",
	libraries  <- c("Cairo", "RColorBrewer", "data.table", "ggplot2", "fmsb", "methods", "grid", "snow", "pvclust") #, "mvoutlier")
	loadInstallAndUpdatePackages(libraries, TRUE, TRUE, FALSE)
}

startOptions <- function(typOfStartOptions = "test", debug=FALSE) {
	initRfunction(debug)
	#typOfStartOptions = "test"
	typOfStartOptions = tolower(typOfStartOptions)
	ownCat(paste("used R-Version: ", sessionInfo()$R.version$major, ".", sessionInfo()$R.version$minor, sep=""))
	
		
	
	args = commandArgs(TRUE)

	saveFormat = "pdf"
	dpi = "90" ##90 ## CK: seems to change nothing for ggplot2 instead the output size should be modified, if needed // 17.2.2012	
	
	isGray = FALSE
	#showResultInR = FALSE
	
	treatment = "Treatment"
	filterTreatment = "none"
	
	secondTreatment = "none"
	filterSecondTreatment = "none"
	
	xAxis = "Day (Int)" 
	xAxisName = "DAS"
	filterXaxis = "none"
	
#	diagramTypVector = vector()
	descriptorSet = vector()
	descriptorSetName = vector()
	
	fileName = "error"

	appendix = FALSE
	#appendix = TRUE
	
	separation = ";"

	if (typOfStartOptions == "all" | typOfStartOptions == "report" | typOfStartOptions == "allmanual") {
		fileName = fileName %exists% args[1]
		
		if (fileName != "error") {
			workingDataSet = separation %readInputDataFile% fileName
			descriptorSet_nBoxplot <- vector()
			descriptorSet_boxplot <- vector()
			descriptorSet_boxplotStacked <- vector()
			
			if (length(workingDataSet[,1]) > 0) {
				#nboxplot

				if (typOfStartOptions == "all") {
					descriptorSet_nBoxplot = colnames(workingDataSet)
					descriptorSetName_nBoxplot = descriptorSet
					
				} else { #Report	
					descriptorSet_nBoxplot = c(#"volume.my", "volume.fluo.plant_weight.iap"
							 					"Weight A (g)", 
												"Weight B (g)", 
												"Water (weight-diff)", 
												"side.height.norm (mm)", 
												"side.width.norm (mm)", 
												"side.area.norm (mm^2)", 
												"top.area.norm (mm^2)", 											
												"side.fluo.intensity.chlorophyl.average (relative)", 
												"side.fluo.intensity.phenol.average (relative)", 
												"side.nir.intensity.average (relative)",
												"top.nir.intensity.average (relative / pix)",
												
												"side.leaf.count.median (leafs)", 
												"side.bloom.count (tassel)", 
												"side.leaf.length.sum.norm.max (mm)", 
												"volume.iap (px^3)", 
												"volume.lt (px^3)", 
												"volume.iap.wue", 
												"side.nir.wetness.plant_weight_drought_loss", 
												"top.nir.wetness.plant_weight_drought_loss", 
												"side.nir.wetness.average (percent)", 
												"top.nir.wetness.average (percent)", 
												"side.area.relative", 
												"side.height.norm.relative", 
												"side.width.norm.relative", 
												"top.area.relative", 
												"side.area.relative", 
												"volume.iap.relative", 
												"side.height (px)", 
												"side.width (px)", 
												"side.area (px)", 
												"top.area (px)",
												############ new #######
												"side.hull.area (px)",
												"side.hull.area.norm (mm^2)",
												"side.hull.pc1 (px)",
												"side.hull.pc1.norm",
												"side.hull.pc2",
												"side.hull.pc2.norm",
												"side.hull.fillgrade (percent)",		
												"top.hull.area (px)",
												"top.hull.area.norm (mm^2)",
												"top.hull.pc1",
												"top.hull.pc1.norm",
												"top.hull.pc2",
												"top.hull.pc2.norm",
												"top.hull.fillgrade (percent)",
												"side.vis.hue.average",
												"top.vis.hue.average",
												"mark1.y (percent)",
												"mark2.y (percent)",
												"mark3.y (percent)",
												"top.ir.intensity.average",
												"side.ir.intensity.average"
												)
				
					descriptorSetName_nBoxplot = c(#"digital biomass (visible light images, IAP formula) (px^3)", "yellow spectra normed to the realtionship between dry and normal"
													"weight before watering (g)", 
													"weight after watering (g)", 
													"water weight (g)", 
													"height (zoom corrected) (mm)", 
													"width (zoom corrected) (mm)", 
													"side area (zoom corrected) (mm^2)", 
													"top area (zoom corrected) (mm^2)", 
													"chlorophyll intensity (relative intensity/pixel)", 
													"fluorescence intensity (relative intensity/pixel)", 
													"side nir intensity (relative intensity/pixel)",
													"top nir intensity (relative intensity/pixel)",
													"number of leafs", 
													"number of tassel florets", 
													"length of leafs plus stem (mm)", 			 
													"digital biomass (visible light images, IAP formula) (px^3)", 
													"digital biomass (visible light, LemnaTec 0,90 formula) (px^3)", 
													"volume based water use efficiency", 
													"weighted loss through drought stress (side)", 
													"weighted loss through drought stress (top)", 
													"Average wetness of side image", 
													"Average wetness of top image", 
													"growth in %/day", 
													"plant height growth rate (%/day)", 
													"plant width growth rate (%/day)", 
													"top area growth rate (%/day)", 
													"side area growth rate (%/day)", 
													"volume growth (visible light images, IAP based formula) (%/day)", 
													"height (px)", 
													"width (px)", 
													"side area (px)", 
													"top area (px)",
													####### new #######
													"side area of convex hull (px)",
													"side area of convex hull (zoom corrected) (mm^2)",
													"side maximum extension (px)",
													"side maximum extension (zoom corrected) (mm)",
													"opposite direction of the side maximum extension (px)",
													"opposite direction of the side maximum extension (zoom corrected) (mm)",
													"fillgrade of side convex hull (%)",
													"top area of convex hull (px)",
													"top area of convex hull (zoom corrected) (mm^2)",
													"top maximum extension (px)",
													"top maximum extension (zoom corrected) (mm)",
													"opposite direction of the top maximum extension (px)",
													"opposite direction of the top maximum extension (zoom corrected) (mm)",
													"fillgrade of top convex hull (%)",
													"side visible hue average value",
													"top visible hue average value",
													"blue marker position from top (%)",
													"blue marker position from middle (%)",
													"blue marker position from bottom  (%)",
													"top ir intensity",
													"side ir intensity"
													)		
				}
	
				nBoxOptions= NULL
				#diagramTypVector = rep.int("nboxplot", times=length(descriptorSetName))
		
				#boxplot
				descriptorSet_boxplot = c(#"volume.my"
										   "side.height.norm (mm)", 
										   "side.width.norm (mm)", 
										   "side.area.norm (mm^2)", 
										   "top.area.norm (mm^2)", 
										   "volume.fluo.iap", 
										   "volume.iap (px^3)", 
										   "volume.lt (px^3)", 
										   "side.height (px)", 
										   "side.width (px)", 
										   "side.area (px)", 
										   "top.area (px)"
											)
				
				descriptorSetName_boxplot = c(#"digital biomass (visible light images, IAP formula) (px^3)"
											   "height (zoom corrected) (mm)", 
										  	   "width (zoom corrected) (mm)", 
											   "side area (zoom corrected) (mm^2)", 
											   "top area (zoom corrected) (mm^2)", 
											   "digital biomass (fluorescence images, IAP formula) (px^3)", 
											   "digital biomass (visible light images, IAP formula) (px^3)", 
											   "digital biomass (visible light, LemnaTec 0,90 formula) (px^3)", 
											   "height (px)", 
											   "width (px)", 
											   "side area (px)", 
											   "top area (px)"
												)	
				#boxOptions= list(daysOfBoxplotNeeds=c("phase4"))
				boxOptions= NULL
				
				#spiderplot
				descriptorSet_spiderplot = c(#"volume.my"
						"side.height.norm (mm)$side.width.norm (mm)$side.area.norm (mm^2)$top.area.norm (mm^2)$side.fluo.intensity.average (relative)$side.nir.intensity.average (relative)$side.vis.hue.average$top.vis.hue.average",
						"side.height (px)$side.width (px)$side.area (px)$top.area (px)$side.fluo.intensity.average (relative)$side.nir.intensity.average (relative)$side.vis.hue.average$top.vis.hue.average"

				)
				
				descriptorSetName_spiderplot = c(#"digital biomass (visible light images, IAP formula) (px^3)"
						"height (zoom corrected) (mm)$width (zoom corrected) (mm)$side area (zoom corrected) (mm^2)$top area (zoom corrected) (mm^2)$side fluo intensity$side nir intensity$side visible hue average value$top visible hue average value",
						"height (px)$width (px)$side area (px)$top area (px)$side fluo intensity$side nir intensity$side visible hue average value$top visible hue average value"
#						"Zoom corrected Spiderchart", 
#						"Spiderchart"
				)	

				#spiderOptions= list(typOfGeomBar=c("x", "y"))
				spiderOptions= list(typOfGeomBar=c("x"))
				
				
				descriptorSet_violinBox = c(
						"side.height.norm (mm)",
						"side.width.norm (mm)",
						"side.area.norm (mm^2)",
						"top.area.norm (mm^2)",
						"side.fluo.intensity.average (relative)",
						"side.nir.intensity.average (relative)",
						"side.vis.hue.average",
						"top.vis.hue.average",
						"top.ir.intensity.average",
						"side.ir.intensity.average"
				)	
				
				descriptorSetName_violinBox = c(
						"height (zoom corrected) (mm)",
						"width (zoom corrected) (mm)",
						"side area (zoom corrected) (mm^2)",
						"top area (zoom corrected) (mm^2)",
						"side fluo intensity",
						"side nir intensity",
						"side visible hue average value",
						"top visible hue average value",
						"top ir intensity",
						"side ir intensity"
				)	
				
				violinOptions= NULL
				
				
				#boxplotStacked
				descriptorSet_boxplotStacked = c("side.nir.normalized.histogram.bin.", 
								   				  "side.fluo.histogram.bin.", 
										  		  "top.nir.histogram.bin.", 
												  "side.fluo.histogram.ratio.bin.", 
												  "side.nir.normalized.histogram.bin.", 
												  "side.fluo.normalized.histogram.bin.", 
												  "side.fluo.normalized.histogram.ratio.bin.", 
												  "side.vis.hue.histogram.ratio.bin.", 
												  "side.vis.normalized.histogram.ratio.bin.", 
												  "top.fluo.histogram.bin.", 
												  "top.fluo.histogram.ratio.bin.", 
												  "top.nir.histogram.bin.", 
												  "top.vis.hue.histogram.ratio.bin.",
												  "top.ir.histogram.bin.",
												  "side.ir.histogram.bin."
												)
												  
										  
				descriptorSetName_boxplotStacked = c("side near-infrared intensities (zoom corrected) (%)", 
													  "side fluorescence colour spectra (%)", 
													  "top near-infrared intensities (%)", 
													  "side fluorescence ratio histogram (%)", 
													  "side near-infrared (zoom corrected) (%)", 
													  "side fluorescence colour spectra (zoom corrected) (%)", 
													  "side fluorescence  colour spectra (%)", 
													  "side visible light colour histogram (%)", 
													  "side visible light ratio histogram (zoom corrected) (%)", 
													  "top fluorescence colour spectra (%)", 
													  "top fluo ratio histogram (%)", 
													  "NIR top histogram (%)", 
													  "top visible light color histogram (%)",
													  "top infrared light heat histogram (%)",
													  "side infrared light heat histogram (%)"
														)
				
				descriptorList <- addDesSet(descriptorSet_boxplotStacked, descriptorSetName_boxplotStacked, workingDataSet)
				descriptorSet_boxplotStacked <- descriptorList$desSet
				descriptorSetName_boxplotStacked <- descriptorList$desName
				
				stackedBarOptions = list(typOfGeomBar=c("fill", "stack", "dodge"))
				#diagramTypVector = c(diagramTypVector, "boxplotStacked", "boxplotStacked")
				
				appendix = as.logical(appendix %exists% args[3])
			
				if (appendix) {
					blacklist = buildBlacklist(workingDataSet, descriptorSet_nBoxplot)
					descriptorSetAppendix = colnames(workingDataSet[!as.data.frame(sapply(colnames(workingDataSet), '%in%', blacklist))[, 1]])
					descriptorSetNameAppendix = descriptorSetAppendix
					#diagramTypVectorAppendix = rep.int("nboxplot", times=length(descriptorSetNameAppendix))
				}
			
				saveFormat = saveFormat %exists% args[2]
			
				listOfTreatAndFilterTreat = checkOfTreatments(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug)
				treatment = listOfTreatAndFilterTreat[[1]][[1]]
				secondTreatment = listOfTreatAndFilterTreat[[1]][[2]]
				filterTreatment = listOfTreatAndFilterTreat[[2]][[1]]
				filterSecondTreatment = listOfTreatAndFilterTreat[[2]][[2]]

				if(treatment == "noneTreatment") {
					workingDataSet <- cbind(workingDataSet, noneTreatment=rep.int("average", times = length(workingDataSet[,1])))	
				}

				isRatio	= as.logical(isRatio %exists% args[4])
			} else {
				fileName = "error"
			}
		}
		
	}  else if (typOfStartOptions == "test"){
		
		debug <- TRUE
		initRfunction(debug)
		
		treatment <- "Species"
		filterTreatment <- "none"
		
		secondTreatment <- "Treatment"
		filterSecondTreatment  <- "normal"
		#filterSecondTreatment <- "Athletico$Weisse Zarin"
		#filterSecondTreatment <- "BCC_1367_Apex$BCC_1391_Isaria$BCC_1403_Perun$BCC_1433_HeilsFranken$BCC_1441_PflugsIntensiv$Wiebke$BCC_1413_Sissy$BCC_1417_Trumpf"
		filterXaxis <- "none"

		bgColor <- "transparent"
		isGray="FALSE"
		#showResultInR <- FALSE
		
		fileName <- "report.csv"
		separation <- ";"
		workingDataSet <- separation %readInputDataFile% fileName
		
		saveName <- "test2"
		yAxisName <- "test2"
		
		
		stoppTheCalculation <- FALSE
		iniDataSet = workingDataSet
		
		
		descriptorSet_nBoxplot = c(#"volume.my", "volume.fluo.plant_weight.iap"
				"Weight A (g)", 
				"Weight B (g)", 
				"Water (weight-diff)", 
				"side.height.norm (mm)", 
				"side.width.norm (mm)", 
				"side.area.norm (mm^2)", 
				"top.area.norm (mm^2)", 											
				"side.fluo.intensity.chlorophyl.average (relative)", 
				"side.fluo.intensity.phenol.average (relative)", 
				"side.nir.intensity.average (relative)",
				"top.nir.intensity.average (relative / pix)",
				
				"side.leaf.count.median (leafs)", 
				"side.bloom.count (tassel)", 
				"side.leaf.length.sum.norm.max (mm)", 
				"volume.iap (px^3)", 
				"volume.lt (px^3)", 
				"volume.iap.wue", 
				"side.nir.wetness.plant_weight_drought_loss", 
				"top.nir.wetness.plant_weight_drought_loss", 
				"side.nir.wetness.average (percent)", 
				"top.nir.wetness.average (percent)", 
				"side.area.relative", 
				"side.height.norm.relative", 
				"side.width.norm.relative", 
				"top.area.relative", 
				"side.area.relative", 
				"volume.iap.relative", 
				"side.height (px)", 
				"side.width (px)", 
				"side.area (px)", 
				"top.area (px)",
				############ new #######
				"side.hull.area (px)",
				"side.hull.area.norm (mm^2)",
				"side.hull.pc1 (px)",
				"side.hull.pc1.norm",
				"side.hull.pc2",
				"side.hull.pc2.norm",
				"side.hull.fillgrade (percent)",		
				"top.hull.area (px)",
				"top.hull.area.norm (mm^2)",
				"top.hull.pc1",
				"top.hull.pc1.norm",
				"top.hull.pc2",
				"top.hull.pc2.norm",
				"top.hull.fillgrade (percent)",
				"side.vis.hue.average",
				"top.vis.hue.average",
				"mark1.y (percent)",
				"mark2.y (percent)",
				"mark3.y (percent)",
				"top.ir.intensity.average",
				"side.ir.intensity.average"
		)
		
		descriptorSetName_nBoxplot = c(#"digital biomass (visible light images, IAP formula) (px^3)", "yellow spectra normed to the realtionship between dry and normal"
				"weight before watering (g)", 
				"weight after watering (g)", 
				"water weight (g)", 
				"height (zoom corrected) (mm)", 
				"width (zoom corrected) (mm)", 
				"side area (zoom corrected) (mm^2)", 
				"top area (zoom corrected) (mm^2)", 
				"chlorophyll intensity (relative intensity/pixel)", 
				"fluorescence intensity (relative intensity/pixel)", 
				"side nir intensity (relative intensity/pixel)",
				"top nir intensity (relative intensity/pixel)",
				"number of leafs", 
				"number of tassel florets", 
				"length of leafs plus stem (mm)", 			 
				"digital biomass (visible light images, IAP formula) (px^3)", 
				"digital biomass (visible light, LemnaTec 0,90 formula) (px^3)", 
				"volume based water use efficiency", 
				"weighted loss through drought stress (side)", 
				"weighted loss through drought stress (top)", 
				"Average wetness of side image", 
				"Average wetness of top image", 
				"growth in %/day", 
				"plant height growth rate (%/day)", 
				"plant width growth rate (%/day)", 
				"top area growth rate (%/day)", 
				"side area growth rate (%/day)", 
				"volume growth (visible light images, IAP based formula) (%/day)", 
				"height (px)", 
				"width (px)", 
				"side area (px)", 
				"top area (px)",
				####### new #######
				"side area of convex hull (px)",
				"side area of convex hull (zoom corrected) (mm^2)",
				"side maximum extension (px)",
				"side maximum extension (zoom corrected) (mm)",
				"opposite direction of the side maximum extension (px)",
				"opposite direction of the side maximum extension (zoom corrected) (mm)",
				"fillgrade of side convex hull (%)",
				"top area of convex hull (px)",
				"top area of convex hull (zoom corrected) (mm^2)",
				"top maximum extension (px)",
				"top maximum extension (zoom corrected) (mm)",
				"opposite direction of the top maximum extension (px)",
				"opposite direction of the top maximum extension (zoom corrected) (mm)",
				"fillgrade of top convex hull (%)",
				"side visible hue average value",
				"top visible hue average value",
				"blue marker position from top (%)",
				"blue marker position from middle (%)",
				"blue marker position from bottom  (%)",
				"top ir intensity",
				"side ir intensity"
		)		
	
	nBoxOptions= NULL
	
	#boxplot
	descriptorSet_boxplot = c(#"volume.my"
			"side.height.norm (mm)", 
			"side.width.norm (mm)", 
			"side.area.norm (mm^2)", 
			"top.area.norm (mm^2)", 
			"volume.fluo.iap", 
			"volume.iap (px^3)", 
			"volume.lt (px^3)", 
			"side.height (px)", 
			"side.width (px)", 
			"side.area (px)", 
			"top.area (px)"
	)
	
	descriptorSetName_boxplot = c(#"digital biomass (visible light images, IAP formula) (px^3)"
			"height (zoom corrected) (mm)", 
			"width (zoom corrected) (mm)", 
			"side area (zoom corrected) (mm^2)", 
			"top area (zoom corrected) (mm^2)", 
			"digital biomass (flurescence images, IAP formula) (px^3)", 
			"digital biomass (visible light images, IAP formula) (px^3)", 
			"digital biomass (visible light, LemnaTec 0,90 formula) (px^3)", 
			"height (px)", 
			"width (px)", 
			"side area (px)", 
			"top area (px)"
	)	
	
	#boxOptions= list(daysOfBoxplotNeeds=c("phase4"))
	boxOptions= NULL
	
	#violinplot
	descriptorSet_violinBox = c(
			"side.height.norm (mm)",
			"side.width.norm (mm)",
			"side.area.norm (mm^2)",
			"top.area.norm (mm^2)",
			"side.fluo.intensity.average (relative)",
			"side.nir.intensity.average (relative)",
			"side.vis.hue.average",
			"top.vis.hue.average",
			"top.ir.intensity.average",
			"side.ir.intensity.average"
	)	
	
	descriptorSetName_violinBox = c(
			"height (zoom corrected) (mm)",
			"width (zoom corrected) (mm)",
			"side area (zoom corrected) (mm^2)",
			"top area (zoom corrected) (mm^2)",
			"side fluo intensity",
			"side nir intensity",
			"side visible hue average value",
			"top visible hue average value",
			"top ir intensity",
			"side ir intensity"
	)	
	
	violinOptions= NULL
	
	
	#boxplotStacked
	descriptorSet_boxplotStacked = c("side.nir.normalized.histogram.bin.", 
			"side.fluo.histogram.bin.", 
			"top.nir.histogram.bin.", 
			"side.fluo.histogram.ratio.bin.", 
			"side.nir.normalized.histogram.bin.", 
			"side.fluo.normalized.histogram.bin.", 
			"side.fluo.normalized.histogram.ratio.bin.", 
			"side.vis.hue.histogram.ratio.bin.", 
			"side.vis.normalized.histogram.ratio.bin.", 
			"top.fluo.histogram.bin.", 
			"top.fluo.histogram.ratio.bin.", 
			"top.nir.histogram.bin.", 
			"top.vis.hue.histogram.ratio.bin.",
			"top.ir.histogram.bin.",
			"side.ir.histogram.bin."
	)
	
	
	descriptorSetName_boxplotStacked = c("side near-infrared intensities (zoom corrected) (%)", 
			"side fluorescence colour spectra (%)", 
			"top near-infrared intensities (%)", 
			"side fluorescence ratio histogram (%)", 
			"side near-infrared (zoom corrected) (%)", 
			"side fluorescence colour spectra (zoom corrected) (%)", 
			"side fluorescence  colour spectra (%)", 
			"side visible light colour histogram (%)", 
			"side visible light ratio histogram (zoom corrected) (%)", 
			"top fluorescence colour spectra (%)", 
			"top fluo ratio histogram (%)", 
			"NIR top histogram (%)", 
			"top visible light color histogram (%)",
			"top infrared light heat histogram (%)",
			"side infrared light heat histogram (%)"
	)
	
	#spiderplot
	descriptorSet_spiderplot = c(#"volume.my"
			"side.height.norm (mm)$side.width.norm (mm)$side.area.norm (mm^2)$top.area.norm (mm^2)$side.fluo.intensity.average (relative)$side.nir.intensity.average (relative)$side.vis.hue.average$top.vis.hue.average",
			"side.height (px)$side.width (px)$side.area (px)$top.area (px)$side.fluo.intensity.average (relative)$side.nir.intensity.average (relative)$side.vis.hue.average$top.vis.hue.average"
	
	)
	
	descriptorSetName_spiderplot = c(#"digital biomass (visible light images, IAP formula) (px^3)"
			"height (zc) (mm)$width (zc) (mm)$side area (zc) (mm^2)$top area (zc) (mm^2)$side fluo intensity$side nir intensity$side visible hue average value$top visible hue average value",
			"height (px)$width (px)$side area (px)$top area (px)$side fluo intensity$side nir intensity$side visible hue average value$top visible hue average value"
#						"Zoom corrected Spiderchart", 
#						"Spiderchart"
	)	
	
	#spiderOptions= list(typOfGeomBar=c("x", "y"))
	spiderOptions= list(typOfGeomBar=c("x"))
	
	descriptorList <- addDesSet(descriptorSet_boxplotStacked, descriptorSetName_boxplotStacked, workingDataSet)
	descriptorSet_boxplotStacked <- descriptorList$desSet
	descriptorSetName_boxplotStacked <- descriptorList$desName
	
	stackedBarOptions = list(typOfGeomBar=c("fill", "stack", "dodge"))
		
		
		boxDes = descriptorSet_boxplot
		boxStackDes = descriptorSet_boxplotStacked 
		boxDesName = descriptorSetName_boxplot
		boxStackDesName = descriptorSetName_boxplotStacked 
		nBoxOptions= nBoxOptions
		boxOptions= boxOptions
		stackedBarOptions = stackedBarOptions
		nBoxDes <- descriptorSet_nBoxplot
		nBoxDesName <- descriptorSetName_nBoxplot
		boxSpiderDes <- descriptorSet_spiderplot
		boxSpiderDesName <- descriptorSetName_spiderplot
		violinBoxDes <- descriptorSet_violinBox
		violinBoxDesName <- descriptorSetName_violinBox
		
		appendix <- FALSE
		if (appendix) {
			blacklist = buildBlacklist(workingDataSet, descriptorSet_nBoxplot)
			descriptorSetAppendix = colnames(workingDataSet[!as.data.frame(sapply(colnames(workingDataSet), '%in%', blacklist))[, 1]])
			descriptorSetNameAppendix = descriptorSetAppendix
			#diagramTypVectorAppendix = rep.int("nboxplot", times=length(descriptorSetNameAppendix))		
			descriptorSet_nBoxplot = descriptorSetAppendix
			descriptorSetName_nBoxplot = descriptorSetNameAppendix
			#diagramTypVector = diagramTypVectorAppendix
			descriptorSet_boxplot = NULL
			descriptorSetName_boxplot = NULL
			descriptorSet_boxplotStacked = NULL
			descriptorSetName_boxplotStacked = NULL
			descriptorSet_spiderplot = NULL
			descriptorSetName_spiderplot = NULL
			descriptorSet_violinBox = NULL
		}
		
		listOfTreatAndFilterTreat = checkOfTreatments(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug)
		treatment = listOfTreatAndFilterTreat[[1]][[1]]
		secondTreatment = listOfTreatAndFilterTreat[[1]][[2]]
		filterTreatment = listOfTreatAndFilterTreat[[2]][[1]]
		filterSecondTreatment = listOfTreatAndFilterTreat[[2]][[2]]
		
		if(treatment == "noneTreatment") {
			workingDataSet <- cbind(workingDataSet, noneTreatment=rep.int("average", times = length(workingDataSet[,1])))	
		}
		
		
		isRatio <- TRUE
		calculateNothing <- FALSE
	}
	
	if (typOfStartOptions != "test"){
		secondRun = appendix
		appendix =  FALSE
		
		if (fileName != "error" & (length(descriptorSet_nBoxplot) > 0 || length(descriptorSet_boxplot) > 0 || length(descriptorSet_boxplotStacked) > 0)) {
			time = system.time({
				repeat {					
					if (appendix) 
						ownCat("Generate diagrams for annotation descriptors...")
					else
						ownCat("Generate diagrams for main descriptors...")
					createDiagrams(iniDataSet = workingDataSet, saveFormat = saveFormat, dpi = dpi, isGray = isGray,
										nBoxDes = descriptorSet_nBoxplot, boxDes = descriptorSet_boxplot, boxStackDes = descriptorSet_boxplotStacked, boxSpiderDes = descriptorSet_spiderplot, violinBoxDes = descriptorSet_violinBox,
										nBoxDesName = descriptorSetName_nBoxplot, boxDesName = descriptorSetName_boxplot, boxStackDesName = descriptorSetName_boxplotStacked, boxSpiderDesName = descriptorSetName_spiderplot, violinBoxDesName = descriptorSetName_violinBox,
										nBoxOptions= nBoxOptions, boxOptions= boxOptions, stackedBarOptions = stackedBarOptions, spiderOptions = spiderOptions, violinOptions = violinOptions,
										treatment = treatment, filterTreatment = filterTreatment, 
										secondTreatment = secondTreatment, filterSecondTreatment = filterSecondTreatment, filterXaxis = filterXaxis, xAxis = xAxis, 
										xAxisName = xAxisName, debug = debug, appendix=appendix, isRatio=isRatio)
					if (secondRun) {
						appendix = TRUE
						secondRun = FALSE
						descriptorSet_nBoxplot = descriptorSetAppendix
						descriptorSetName_nBoxplot = descriptorSetNameAppendix
						#diagramTypVector = diagramTypVectorAppendix
						descriptorSet_boxplot = NULL
						descriptorSetName_boxplot = NULL
						descriptorSet_boxplotStacked = NULL
						descriptorSetName_boxplotStacked = NULL
						descriptorSet_spiderplot = NULL
						descriptorSetName_spiderplot = NULL
						descriptorSet_violinBox = NULL
						descriptorSetName_violinBox = NULL
						
					} else {
						break
					}
				}
				checkIfAllNecessaryFilesAreThere()
			}, TRUE)
			
			ownCat("Processing finished")		
			ownCat(time)
			
		} else {
			ownCat("No filename or no descriptor!")
			checkIfAllNecessaryFilesAreThere()
		}
		
		if(debug) {
			ownCat(warnings())
			
		}
	} 
}

createDiagrams <- function(iniDataSet, saveFormat="pdf", dpi="90", isGray="false", 
		nBoxDes = NULL, boxDes = NULL, boxStackDes = NULL, boxSpiderDes = NULL, violinBoxDes=NULL,
		nBoxDesName = NULL, boxDesName = NULL, boxStackDesName = NULL, boxSpiderDesName = NULL, violinBoxDesName = NULL,
		nBoxOptions= NULL, boxOptions= NULL, stackedBarOptions = NULL, spiderOptions = NULL, violinOptions = NULL,
		treatment="Treatment", filterTreatment="none", 
		secondTreatment="none", filterSecondTreatment="none", filterXaxis="none", xAxis="Day (Int)", 
		xAxisName="none", debug = FALSE, appendix=FALSE, stoppTheCalculation=FALSE, isRatio=FALSE) {		

	overallList = list(iniDataSet=iniDataSet, saveFormat=saveFormat, dpi=dpi, isGray=isGray, 
						nBoxDes = nBoxDes, boxDes = boxDes, boxStackDes = boxStackDes, boxSpiderDes = boxSpiderDes, violinBoxDes = violinBoxDes,
						imageFileNames_nBoxplots = nBoxDes, imageFileNames_Boxplots = boxDes, imageFileNames_StackedPlots = boxStackDes, imageFileNames_SpiderPlots = boxSpiderDes, imageFileNames_violinPlots =violinBoxDes,
						nBoxDesName = nBoxDesName, boxDesName = boxDesName, boxStackDesName = boxStackDesName, boxSpiderDesName = boxSpiderDesName, violinBoxDesName=violinBoxDesName,
						nBoxOptions= nBoxOptions, boxOptions= boxOptions, stackedBarOptions = stackedBarOptions, spiderOptions = spiderOptions, violinOptions=violinOptions,
						treatment=treatment, filterTreatment=filterTreatment, 
						secondTreatment=secondTreatment, filterSecondTreatment=filterSecondTreatment, filterXaxis=filterXaxis, xAxis=xAxis, 
						xAxisName=xAxisName, debug=debug, 
						appendix=appendix, stoppTheCalculation=stoppTheCalculation, isRatio = isRatio,
						overallResult_nBoxDes=data.frame(), overallResult_boxDes=data.frame(), overallResult_boxStackDes=data.frame(), overallResult_boxSpiderDes=data.frame(), overallResult_violinBoxDes = data.frame(),
						color_nBox = list(), color_box=list(), color_boxStack=list(), color_spider = list(), color_violin= list(), user="none", typ="none",
						filterTreatmentRename = list(), secondFilterTreatmentRename = list())	
				
	overallList$debug %debug% "Start"
	
	overallList = checkTypOfExperiment(overallList)
	overallList = checkUserOfExperiment(overallList)
	overallList = setSomePrintingOptions(overallList)
	overallList = overallChangeName(overallList)
	overallList = changeXAxisName(overallList)
	overallList = overallPreprocessingOfDescriptor(overallList)

	#####
#	overallList = preprocessingOfxAxisValue(overallList)
#	overallList = preprocessingOfTreatment(overallList)
#	overallList = preprocessingOfSecondTreatment(overallList)
#	overallList = renameOfTheTreatments(overallList)
#	overallList = overallCheckIfDescriptorIsNaOrAllZero(overallList)
#	overallList = reduceWorkingDataSize(overallList)
#	overallList = setDefaultAxisNames(overallList)
#	
#	#overallList = overallOutlierDetection(overallList)
#	overallList = overallGetResultDataFrame(overallList)
#	overallList = setColor(overallList) 
#	makeDiagrams(overallList)
	#######
	
	if (!overallList$stoppTheCalculation) {
		overallList = preprocessingOfxAxisValue(overallList)
		overallList = preprocessingOfTreatment(overallList)
		overallList = preprocessingOfSecondTreatment(overallList)
		overallList = renameOfTheTreatments(overallList)
		overallList = overallCheckIfDescriptorIsNaOrAllZero(overallList)
		if (!overallList$stoppTheCalculation) {
			overallList = reduceWorkingDataSize(overallList)
			overallList = setDefaultAxisNames(overallList)
			#ownCat(overallList)
		#	overallList = overallOutlierDetection(overallList)
			overallList = overallGetResultDataFrame(overallList)
			if (!overallList$stoppTheCalculation) {
				overallList = setColor(overallList) 
				
				makeDiagrams(overallList)
			}
		}
	}
}
#sapply(list.files(pattern="[.]R$", path=getwd(), full.names=TRUE), source);
calculateNothing <- FALSE
######### START #########
#rm(list=ls(all=TRUE))
#startOptions("test", TRUE)
#startOptions("allmanual", TRUE)
startOptions("report", TRUE)
rm(list=ls(all=TRUE))