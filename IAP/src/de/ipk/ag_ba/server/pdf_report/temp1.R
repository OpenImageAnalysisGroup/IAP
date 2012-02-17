# TODO: Add comment
# 
# Author: Entzian
###############################################################################


overallResult <- matrix(ncol = length(days), nrow = length(rowName))
colnames(overallResult) <- colName
#rownames(overallResult) <- multiDescriptor
rownames(overallResult) <- rowName
debug %debug% 9
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

debug %debug% 10
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
debug %debug% 11
for(h in 1:durchlauf) {
	symbolParameter <- numeric()
	
	
	if (h==1) {
		Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(saveName,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
	}
	
	if (legendUnderImage) {
		layout(matrix(c(1,2), nrow = 2, ncol = 1, byrow = TRUE), heights=c(2,1))
		par(mar=c(4.1,4.1,2.1,2.1))
	}
	
	if (tolower(diagramTyp) == "boxplot") {
		barplot(workingDataSet, beside= TRUE, main="", xlab=xAxisName, ylab=yAxisName, col=usedColor, space=c(0,2), width=0.1, ylim=c(0,max(workingDataSet,na.rm=TRUE)))
		symbolParameter <- 15
	} else if (tolower(diagramTyp) == "boxplotstacked") {
		
		workingDataSet[is.na(workingDataSet)] <- 0
		
		if (length(resFilterTreatment) > 1) {
			
			layoutMatrix <- c(1:(2*length(resFilterTreatment)))
			
			layout(matrix(layoutMatrix, nrow = 2, ncol = length(resFilterTreatment), byrow = FALSE), heights=c(2,1))
			symbolParameter <- 15
			
			
			for(o in 1:length(resFilterTreatment)) {
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
				par(mar=c(0.1,0.1,0.1,0.1))
				barplot(1:1, main="", col=NA, border="NA", axes=FALSE)	#dummy plot -> ist notwendig
				legend("left", rownames(workingDataSet[rowWhichPlotInOneDiagram,]), col= usedColor, pch=symbolParameter, bty="n")
				par(mar=c(4.1,4.1,2.1,2.1))
			}
		} else {
			
			
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
		}
		
		
		
	} else {
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
	
	if (!(tolower(diagramTyp) == "boxplotstacked" & length(resFilterTreatment) > 1)) {
		grid()
		#split.screen(c(1,1))
		
		if(legendUnderImage){
			par(mar=c(0.1,0.1,0.1,0.1))
			barplot(1:1, main="", col=NA, border="NA", axes=FALSE)	#dummy plot -> ist notwendig
			legend("left", rownames(workingDataSet), col= usedColor, pch=symbolParameter, bty="n")
		} else {
			legend("topleft", resFilterTreatment, col= usedColor, pch=symbolParameter)
			#legend("topleft", rownames(workingDataSet), col= usedColor, pch=symbolParameter)
			#legend("bottomleft", resFilterTreatment, col= usedColor, pch=symbolParameter)
		}
		
	} 
	#close.screen(all=TRUE)
	if (h==1) {
		dev.off()
	}
	par(mar=standardPar$mar, oma=standardPar$oma, xpd=standardPar$xpd)
}


} 
#		else {


#			plotDummy(imageWidth, imageHeight, saveName, saveFormat, bg, dpi)
#			if (isNum) {
#				print(paste("No plotting, because all values of",descriptor,"are 'NA'"))
#			} else {	
#				print(paste("'", descriptor,"' contains no digits!",sep=""))
#			}
#		}

#} 
#	else {
#plotDummy(imageWidth, imageHeight, saveName, saveFormat, bg, dpi)
#print(paste("No plotting, because all descriptor(s) (",descriptorsWhichNotExists,") don't exists!"))
#	descriptorsWhichNotExists %errorReport% "notExists"
#	}
#}
