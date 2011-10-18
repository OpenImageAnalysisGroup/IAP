# TODO: Add comment
# 
# Author: Entzian
###############################################################################


####################################################################
##
#		FOR TEST, Don´t remove it!!
##
if(FALSE){
	
	rm(list=ls(all=TRUE))
	
	while(!is.null(dev.list())) {
		dev.off()
	}
	
	options(error = quote({
		sink(file="error.txt", split = TRUE);
		dump.frames();
		print(attr(last.dump,"error.message"));
		#x <- attr(last.dump,"error.message")
		traceback();
		sink(file=NULL);		
		#q()
	}))
	
	
	#fileName <- "report.csv"
	fileName <- "numeric_data.MaizeAnalysisAction_ 1116BA_new3.csv"
	#saveName="OutputDiagramm"
	saveFormat="png"
	imageWidth="1280"
	imageHeight="768"
	dpi="90"
	
	#boxplotStacked <- für Intensitätsdarstellungen
	#boxplot <- Standard Boxplot
	#!boxplot <- für xy-Diagramm (vorerst Descriptor vs. Day)
	
	diagramTyp="boxplotStacked"
	#diagramTyp="!boxplot"
	isGray="false"
	#isGray="true"
	treatment="Treatment"
	#treatment="none"
	#filterTreatment="none"
	filterTreatment="normal$dry"
	secondTreatment="none"
	filterSecondTreatment="none"
	filterXaxis="none"
	#filterXaxis=c("6$8$10$12")
	xAxis="Day (Int)"
	#descriptor="Water (weight-diff)"
	#descriptor="Repl.ID"
	#descriptor <- c("side.nir.histogram.bin.1.0_36$side.nir.histogram.bin.2.36_72$side.nir.histogram.bin.3.72_109$side.nir.histogram.bin.4.109_145$side.nir.histogram.bin.5.145_182$side.nir.histogram.bin.6.182_218$side.nir.histogram.bin.7.218_255")
	#descriptor <- c("side.fluo.chlorophyl.normalized.histogram.bin.1.0_36$side.fluo.chlorophyl.normalized.histogram.bin.2.36_72$side.fluo.chlorophyl.normalized.histogram.bin.3.72_109$side.fluo.chlorophyl.normalized.histogram.bin.4.109_145$side.fluo.chlorophyl.normalized.histogram.bin.5.145_182$side.fluo.chlorophyl.normalized.histogram.bin.6.182_218$side.fluo.chlorophyl.normalized.histogram.bin.7.218_255")
	#descriptor <- c("Wert1$Wert2$Wert3$Wert4")
	
	#descriptor <- "side.leaf.length.avg (px)"
	descriptor <- c("side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255")
	yAxisName <- c("NIR absorption class (%)")
	
	showResultInR=TRUE
	xAxisName="Day"
	#yAxisName="digital biomass (mm^3)"
	#yAxisName <- c("test")
	transparent=TRUE
	legendUnderImage = TRUE
	saveName = descriptor
	#iniDataSet <- read.csv(fileName, header=TRUE, sep="\t", fileEncoding="ISO-8859-1", encoding="UTF-8")
	iniDataSet <- read.csv(fileName, header=TRUE, sep=";", fileEncoding="ISO-8859-1", encoding="UTF-8")
}
##
#		END Of Test
##
###############################################################################

valuesAsDiagram <- function(iniDataSet, saveName="OutputDiagramm", saveFormat="png", imageWidth="1280",
		imageHeight="768", dpi="90", diagramTyp="!boxplot", isGray="false", treatment="Treatment",
		filterTreatment="none", secondTreatment="none", filterSecondTreatment="none", 
		filterXaxis="none", xAxis="Day (Int)", descriptor="side.area", showResultInR=FALSE, xAxisName="none", yAxisName="none",
		transparent=TRUE, legendUnderImage=TRUE) {			

#library for save images
#install.packages(c("Cairo"), repos="http://cran.r-project.org", dependencies = TRUE)
	library("Cairo")
#library for colors
#install.packages(c("RColorBrewer"), repos="http://cran.r-project.org", dependencies = TRUE)
	library("RColorBrewer")

	
	isNA <- TRUE
	isNum <- FALSE
	
	regExpressionSpezialCharacter <- "\\$"
	if (nchar(saveName)> 70) {
		saveName <- gsub(regExpressionSpezialCharacter,";",substr(saveName,1,70))
	} else {
		saveName <- gsub(regExpressionSpezialCharacter,";",saveName)
	}
	saveName <- gsub("\\^", "", saveName);
#	iniDataSet <- read.csv2(fileName, header=TRUE, sep=";", fileEncoding="ISO-8859-1", encoding="UTF-8")
	
	
	if (!is.null(descriptor)) {
		regExpressionCol <- "[^[:alnum:]|^_]|[[:space:]|\\^]"
		descriptor <- strsplit(descriptor, "$", fixed=TRUE)[[1]]
		descriptor <- gsub(regExpressionCol,".",descriptor)
	}
#x[!x == remove.value]
#descriptor[!descriptor == descriptor[2]]
# x <- x[which(x==remove.value)] 
	
#is.na(iniDataSet[descriptor])
	descriptorDontExits <- vector()
	notDeleteDescriptorNumber <- 0
	deleteDescriptorNumber <- 0
	
	for(d in 1:length(descriptor)) {
		if (!is.null(iniDataSet[[descriptor[d]]])) {
			notDeleteDescriptorNumber <- c(notDeleteDescriptorNumber, d)		
		} else {
			deleteDescriptorNumber <- c(deleteDescriptorNumber, d)		
		}
	}
	descriptor <- descriptor[notDeleteDescriptorNumber]
	deleteDescriptor <- descriptor[deleteDescriptorNumber]
	
	if (transparent) {
		bg <- "transparent"
	} else {
		bg <- "white"
	}
	
	
	
#match(descriptor, descriptor[deleteDescriptorNumber],nomatch = FALSE)
#if (!is.null(iniDataSet[[descriptor]])) {
	if (length(descriptor)>0) {	
		
		notDeleteDescriptorNumber <- 0
		deleteDescriptorNumber <- 0
		errorMatrix <- matrix(nrow = length(descriptor), ncol = 2)
		colnames(errorMatrix) <- c("isNA", "isNum")
		rownames(errorMatrix) <- descriptor
		for(d in 1:length(descriptor)) {
			
			if (is.na(sum(match(is.na(iniDataSet[[descriptor[d]]]),TRUE)))) {
				isNA <- FALSE
			}
			
			if (sum(as.numeric(as.vector(iniDataSet[[descriptor[d]]])),na.rm=TRUE) > -1) {
				isNum <- TRUE
			}
			
			if (!isNum | isNA) {
				isNA <- TRUE
				deleteDescriptorNumber <- c(deleteDescriptorNumber, d)
			} else {
				notDeleteDescriptorNumber <- c(notDeleteDescriptorNumber, d)
			}
			
			errorMatrix[d,] <- c(isNA, isNum)
		}
		
		descriptor <- descriptor[notDeleteDescriptorNumber]
		deleteDescriptor <- c(deleteDescriptor, descriptor[deleteDescriptorNumber])
		
		if (length(deleteDescriptor) > 0) {
			
			if (length(deleteDescriptor)-deleteDescriptorNumber > 0) {
				print(paste("No plotting of",deleteDescriptor[1:length(deleteDescriptor)-deleteDescriptorNumber],"because the descriptor(s) don't exist!"))
			}
			
			if (sum(match(errorMatrix[,1],TRUE),na.rm = TRUE)>0) {
				print(paste("No plotting of",descriptor[as.data.frame(errorMatrix)$isNum==TRUE], "because all values are 'NA'"))
			} 
			if (sum(match(errorMatrix[,2],TRUE),na.rm = TRUE)>0) {	
				print(paste("'", descriptor[as.data.frame(errorMatrix)$isNA==TRUE],"' contains no digits!",sep=""))
			}		
		}
		
		
		
		if (sum(match(errorMatrix[,1],FALSE),na.rm = TRUE)>0) {
			
			#workingDataSet <- calculateMeanValues(iniDataSet, treatment, filterTreatment, secondTreatment, filterSecondTreatment, xAxis, filterXaxis, descriptor)
			CalculateMeanWorkingDataSet <- iniDataSet
			treatmentName <- treatment
			descriptorName <- descriptor
			
			##########################################  calculateMeanValues ###############################
			result <- numeric()
			overallResult <- data.frame()
			rowName <- character()
			colName <- character()
			digits <- 2 
			regExpressionCol <- "[^[:alnum:]|^_]|[[:space:]]"
			regExpressionUnit <- "[^[:digit:]|^\\.,]"
			
			
			xAxis <- gsub(regExpressionCol,".",xAxis)
			treatmentName <- gsub(regExpressionCol,".",treatmentName)
			
			if (treatmentName == "none") {
				noTreatment = TRUE
				treatmentName <- xAxis	#"Treatment"
				filterTreatment = "none"
				print("... set 'filterTreatment' to 'none'!")
			} else {
				noTreatment = FALSE
			}
			#tempDescriptorName <- descriptorName
			#descriptorName <- gsub(regExpressionCol,".",descriptorName) <- passiert schon eher
			
			resFilterTreatment <- numeric()
			if (!noTreatment) {
				if (filterTreatment != "none") {
					resFilterTreatment <- strsplit(filterTreatment, "$", fixed=TRUE)[[1]]
					treatment <- resFilterTreatment
				} else {
					treatment <- as.character(unique(CalculateMeanWorkingDataSet[treatmentName])[[1]])
					resFilterTreatment <- treatment
				}
			} else {	
				#treatment <- tempDescriptorName
				treatment <- descriptorName[1]
				resFilterTreatment <- treatment
			}
			
			resFilterSecondTreatment <- numeric()
			if (secondTreatment != "none") {
				secondTreatment <- gsub(regExpressionCol,".",secondTreatment)
				
				if(filterSecondTreatment != "none") {
					resFilterSecondTreatment <- strsplit(filterSecondTreatment, "$", fixed=TRUE)[[1]]
				} else {
					resFilterSecondTreatment <- as.character(unique(CalculateMeanWorkingDataSet[secondTreatment])[[1]])
				}
				
			} else {
				secondTreatment <- treatmentName
			}
			
			resfilterXaxis <- numeric()
			if (filterXaxis != "none") {
				resfilterXaxis <- as.numeric(strsplit(filterXaxis, "$", fixed=TRUE)[[1]])
				days <- resfilterXaxis
			} else {
				days <- as.character(unique(CalculateMeanWorkingDataSet[xAxis])[[1]])
			}
			
			for (i in 1:length(descriptorName)) {
				if (!noTreatment) {
					for(j in 1:length(treatment))
						rowName <- c(rowName, paste(descriptorName[i], treatment[j]))
				} else {
					rowName <- c(rowName, descriptorName[i])
				}
				
			}
			
			for (i in 1:length(days)) {
				colName <- c(colName, as.character(days[i]))
			}
			
			filterTyp <- list()
			filterTyp[[1]] <- filterTreatment
			filterTyp[[2]] <- filterSecondTreatment
			filterTyp[[3]] <- filterXaxis
			
			column <- list()
			column[[1]] <- treatmentName
			column[[2]] <- secondTreatment
			column[[3]] <- xAxis
			
			filterValues<- list()
			filterValues[[1]] <- resFilterTreatment
			filterValues[[2]] <- resFilterSecondTreatment
			filterValues[[3]] <- resfilterXaxis
			
			#print("Hallo")
			#print(filterTyp)
			#print(length(filterTyp))
			#print(column)
			#print(filterValues)
			
			for(d in 1:length(filterTyp)) {
				
				if (filterTyp[[d]] != "none") {
					
					set <- rep(FALSE, times=length(CalculateMeanWorkingDataSet[[column[[d]]]])) | match(CalculateMeanWorkingDataSet[[column[[d]]]],filterValues[[d]],nomatch = 0)
				
				} else {
					
					set <- rep(TRUE, times=length(CalculateMeanWorkingDataSet[[column[[d]]]]))
				}
				
				if (d==1) {
					tempWorkingDatasetSum <- set
				} else {
					tempWorkingDatasetSum <- tempWorkingDatasetSum & set
				}
			}

			multiDescriptor <- descriptorName
			tempWorkingDatasetDesc <- matrix(ncol = length(multiDescriptor), nrow = length(CalculateMeanWorkingDataSet[[treatmentName]]))
			colnames(tempWorkingDatasetDesc) <- multiDescriptor 
				
			for (d in 1:length(multiDescriptor)) {
				tempWorkingDatasetDesc[,multiDescriptor[d]] <- rep(FALSE, times=length(CalculateMeanWorkingDataSet[[treatmentName]])) 
				tempWorkingDatasetDesc[,multiDescriptor[d]] <- tempWorkingDatasetSum & (tempWorkingDatasetDesc[,multiDescriptor[d]] | !is.na(CalculateMeanWorkingDataSet[multiDescriptor[d]]!=is.na("NA")))

			}
			overallResult <- matrix(ncol = length(days), nrow = length(rowName))
			colnames(overallResult) <- colName
			#rownames(overallResult) <- multiDescriptor
			rownames(overallResult) <- rowName
			
			#print(tempWorkingDatasetDesc)
			for (d in 1:length(multiDescriptor)) {
				for (y in 1:length(treatment)) {
					for (i in 1:length(days)) {
						if (!noTreatment) {
							result <- c(result,mean(as.numeric(CalculateMeanWorkingDataSet[(CalculateMeanWorkingDataSet[treatmentName]==treatment[y] & CalculateMeanWorkingDataSet[xAxis]==days[i] & tempWorkingDatasetDesc[,multiDescriptor[d]]),descriptorName[d]]),na.rm=TRUE))
						} else {
							result <- c(result,mean(as.numeric(CalculateMeanWorkingDataSet[(CalculateMeanWorkingDataSet[xAxis]==days[i] & tempWorkingDatasetDesc[,multiDescriptor[d]]),descriptorName[d]]),na.rm=TRUE))
						}
					}
					#overallResult[,multiDescriptor[d]] <- rbind(overallResult[,multiDescriptor[d]], result)
					overallResult[rowName[(d-1)*length(treatment)+y],] <- result
					result <- NULL
				}
			}
			
			#overallResult <- as.matrix(overallResult)
			#rownames(overallResult) <- rowName
			#colnames(overallResult) <- colName
			workingDataSet <- overallResult
			
			##################################### valuesAsDiagram #############################################
			
			
			if (!as.logical(isGray)) {
				#11 Spectral
				#8  Dark2
				usedColor <- colorRampPalette(c(brewer.pal(11, "Spectral")))(length(workingDataSet[,1]))
			} else {
				usedColor <- colorRampPalette(c(brewer.pal(9, "Greys")))(length(workingDataSet[,1]))
			}
			
			if (xAxisName == "none") {
				xAxisName <- gsub('[[:punct:]]'," ",xAxis)
			}
			if (yAxisName == "none") {
				yAxisName <- gsub('[[:punct:]]'," ",descriptor)
			}
			
			if (showResultInR) {
				durchlauf <- 2;	
			} else {
				durchlauf <- 1
			}
			
			standardPar <- par()
		
			for(h in 1:durchlauf) {
				symbolParameter <- numeric()
							
			
				if (tolower(diagramTyp) == "boxplot") {
					
					if (h==1) {
						Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
					}
					par(mar=c(4.1,4.1,2.1,2.1))
#					if (legendUnderImage) {
#						layout(matrix(c(1,2), nrow = 2, ncol = 1, byrow = TRUE), heights=c(2,1))
#						par(mar=c(4.1,4.1,2.1,2.1))
#					}
					
					
					barplot(workingDataSet, beside= TRUE, main="", xlab=xAxisName, ylab=yAxisName, col=usedColor, space=c(0,2), width=0.1, ylim=c(0,max(workingDataSet,na.rm=TRUE)))
					symbolParameter <- 15
				} else if (tolower(diagramTyp) == "boxplotstacked") {	
					
					workingDataSet[is.na(workingDataSet)] <- 0
					
					if (length(resFilterTreatment) > 1) {
										
						#layoutMatrix <- c(1:(2*length(resFilterTreatment)))
						#layout(matrix(layoutMatrix, nrow = 2, ncol = length(resFilterTreatment), byrow = FALSE), heights=c(2,1))
						symbolParameter <- 15
						
						write(x="", append=FALSE, file=paste(saveName,"tex",sep="."))
						
						for(o in 1:length(resFilterTreatment)) {
							
							if (h==1) {
								Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,o,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
							}
							par(mar=c(4.1,4.1,2.1,2.1))
							
							rowWhichPlotInOneDiagram <- grep(paste(" ",resFilterTreatment[o],sep=""), rownames(workingDataSet))
							
							for (d in 1:length(workingDataSet[1,])) {
								hundredPercentValue	 <- sum(workingDataSet[rowWhichPlotInOneDiagram,d],na.rm=TRUE)
								if (hundredPercentValue > 0) {
									for(k in 1:length(workingDataSet[rowWhichPlotInOneDiagram,d])) {
										workingDataSet[rowWhichPlotInOneDiagram[k],d] <- (100*workingDataSet[rowWhichPlotInOneDiagram[k],d])/hundredPercentValue
									}
								}
							}
							
							if (!as.logical(isGray)) {
								#11 Spectral
								#8  Dark2
								usedColor <- colorRampPalette(c(brewer.pal(11, "Spectral")))(length(rowWhichPlotInOneDiagram))
							} else {
								usedColor <- colorRampPalette(c(brewer.pal(9, "Greys")))(length(rowWhichPlotInOneDiagram))
							}
							
							barplot(workingDataSet[rowWhichPlotInOneDiagram,], col=usedColor, main=resFilterTreatment[o], xlab=xAxisName, ylab=yAxisName, ylim=c(0,100))
							#par(mar=c(0.1,0.1,0.1,0.1))
							#barplot(1:1, main="", col=NA, border="NA", axes=FALSE)	#dummy plot -> ist notwendig
							#legend("left", rownames(workingDataSet[rowWhichPlotInOneDiagram,]), col= usedColor, pch=symbolParameter, bty="n")
							#par(mar=c(4.1,4.1,2.1,2.1))
							
							if (h==1) {
								dev.off()
							}
													
							
							### latex Datei erweitern
							
							latexText <- paste("\\begin{lyxlist}{00.00.0000}","\n",
											   "\\item [{\\includegraphics[width=13cm]{",
											   "\\string\"",
											   gsub("\\.", "\\\\lyxdot ", saveName,),
											   paste("\\lyxdot ",o,sep=""),
											   "\\string\"",
											   "}}]~","\n",
										   	   "\\end{lyxlist}","\n",sep="")
										
							write(x=latexText, append=TRUE, file=paste(saveName,"tex",sep="."))								
							
						}
						
						##Legende
					
						if (h==1) {
							Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste("legendeBoxStacked",saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
							barplot(1:1, main="", col=NA, border="NA", axes=FALSE)	#dummy plot -> ist notwendig			
							legend("left", substr(rownames(workingDataSet[rowWhichPlotInOneDiagram,]),1, str_locate(rownames(workingDataSet[rowWhichPlotInOneDiagram,])," ")-1), col= usedColor, pch=symbolParameter, bty="n")
							#legend("topleft", rownames(workingDataSet), col= usedColor, pch=symbolParameter)
							#legend("bottomleft", resFilterTreatment, col= usedColor, pch=symbolParameter)
							dev.off()
						}
						
						
					} else {
						
						if (h==1) {
							Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
						}
						par(mar=c(4.1,4.1,2.1,2.1))
						
						#print(workingDataSet)
						for (d in 1:length(workingDataSet[1,])) {
							hundredPercentValue	 <- sum(workingDataSet[,d],na.rm=TRUE)
							if (hundredPercentValue > 0) {
								for(k in 1:length(workingDataSet[,d])) {
									workingDataSet[k,d] <- (100*workingDataSet[k,d])/hundredPercentValue
								}
							}
						}
						
						#print(workingDataSet)
						barplot(workingDataSet, col=usedColor, main="", xlab=xAxisName, ylab=yAxisName, ylim=c(0,100))
						symbolParameter <- 15
						
						latexText <- paste("\\begin{lyxlist}{00.00.0000}","\n",
								"\\item [{\\includegraphics[width=13cm]{",
								"\\string\"",
								gsub("\\.", "\\\\lyxdot ", saveName),
								"\\string\"",
								"}}]~","\n",
								"\\end{lyxlist}","\n",sep="")
						
						write(x=latexText, append=FALSE, file=paste(saveName,"tex",sep="."))
					}
					
					
					
				} else {
					
					if (h==1) {
						Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
					}
					par(mar=c(4.1,4.1,1.1,2.1))
#					if (legendUnderImage) {
#						layout(matrix(c(1,2), nrow = 2, ncol = 1, byrow = TRUE), heights=c(2,1))
#						par(mar=c(4.1,4.1,2.1,2.1))
#					}
					
					if (filterXaxis == "none") {
						xCoords <- as.numeric(colnames(workingDataSet))
					} else {
						xCoords <- sort(as.numeric(strsplit(filterXaxis, "$", fixed=TRUE)[[1]]))
					}	
					for(y in 1:length(workingDataSet[,1])) {
						
						zaehlerNAN <- 0
						zaehlerNAN <- sum(match(is.nan(workingDataSet[y,]),FALSE),na.rm=TRUE)			
						
						if (zaehlerNAN > 1) {	
							
							newCoords <- seq(min(xCoords,na.rm=TRUE),max(xCoords,na.rm=TRUE),1)
							newValue <- approx(xCoords, workingDataSet[y,],xout=newCoords,method="linear")
							
							if (y==1) {
								plot(newValue$x[!is.na(match(newCoords, xCoords))], newValue$y[!is.na(match(newCoords, xCoords))], main="", type="c", xlab=xAxisName, col=usedColor[y], ylab=yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(workingDataSet,na.rm=TRUE),max(workingDataSet,na.rm=TRUE)),)
							} else {
								points(newValue$x[!is.na(match(newCoords, xCoords))], newValue$y[!is.na(match(newCoords, xCoords))], type="c", col=usedColor[y], pch=y, lty=1, lwd=3 )	
							}
							points(xCoords, workingDataSet[y,], type="p", col=usedColor[y], pch=y, lty=1, lwd=3 )
						} else {
							
							if (y==1) {
								plot(xCoords, workingDataSet[y,], main="", type="b", xlab=xAxisName, col=usedColor[y], ylab=yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(workingDataSet,na.rm=TRUE),max(workingDataSet,na.rm=TRUE)))
							} else {	
								points(xCoords, workingDataSet[y,], type="b", col=usedColor[y], pch=y, lty=1, lwd=3 )
							}
						}	
					}
					symbolParameter <- 1:length(workingDataSet[,1])
				}
				
				
				
				
#				if (!is.null(dev.list())) {
#					if (h==1) {
#						dev.off()
#					}
#				}
				
				###Legende
				if (!(tolower(diagramTyp) == "boxplotstacked" & length(resFilterTreatment) > 1)) {
					grid()
					if(h==1){
						dev.off()
						Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste("legende",saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
						barplot(1:1, main="", col=NA, border="NA", axes=FALSE)	#dummy plot -> ist notwendig			
						legend("left", resFilterTreatment, col= usedColor, pch=symbolParameter, bty="n")
						#legend("topleft", rownames(workingDataSet), col= usedColor, pch=symbolParameter)
						#legend("bottomleft", resFilterTreatment, col= usedColor, pch=symbolParameter)
						dev.off()
					}
				}
					
					
				
#				if (!(tolower(diagramTyp) == "boxplotstacked" & length(resFilterTreatment) > 1)) {
#					grid()
#					#split.screen(c(1,1))
#					
#					if(legendUnderImage){
#						par(mar=c(0.1,0.1,0.1,0.1))
#						barplot(1:1, main="", col=NA, border="NA", axes=FALSE)	#dummy plot -> ist notwendig
#						legend("left", rownames(workingDataSet), col= usedColor, pch=symbolParameter, bty="n")
#					} else {
#						legend("topleft", resFilterTreatment, col= usedColor, pch=symbolParameter)
#						#legend("topleft", rownames(workingDataSet), col= usedColor, pch=symbolParameter)
#						#legend("bottomleft", resFilterTreatment, col= usedColor, pch=symbolParameter)
#					}
#					
#				} 
				#close.screen(all=TRUE)
				
				par(mar=standardPar$mar, oma=standardPar$oma, xpd=standardPar$xpd)
			}
			
			
		} else {
			Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
			plot(0,0);
			dev.off();
			if (isNum) {
				print(paste("No plotting, because all values of",descriptor,"are 'NA'"))
			} else {	
				#write.table(x=descriptorName, col.names = FALSE, row.names=FALSE, quote=FALSE, append=TRUE, file=paste(getwd(),"/IAP/blacklist.txt",sep=""))	
				#print(paste("'", descriptor,"' contais no digits. The value added to the blacklist!",sep=""))
				print(paste("'", descriptor,"' contains no digits!",sep=""))
			}
		}
		
	} else {
		Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
		plot(0,0);
		dev.off();
		print(paste("No plotting, because the descriptor(s)",descriptor,"don't exist!"))
	}
}
