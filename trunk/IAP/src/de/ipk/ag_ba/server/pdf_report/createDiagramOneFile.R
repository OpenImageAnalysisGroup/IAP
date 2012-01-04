# TODO: Add comment
# 
# Author: Entzian
###############################################################################

"%debug%" <- function(debug, debugNumber) {
	if (debug) {
		print(paste("DebugBreakPoint: ", debugNumber))
	}
}

"%errorReport%" <- function(overallList, typOfError="notExists") {
	overallList$debug %debug% "%errorReport%"
	if (length(overallList$errorDescriptor) > 0) {
		if (tolower(typOfError) == "notExists"){
			print(paste("No plotting, because the descriptor(s) (",overallList$errorDescriptor,") don't exists!"))
			
		} else if (tolower(typOfError) == "NotNumericOrAllZero") {
			plotDummy(overallList)
			print(paste("Dummy plotting for this descriptor(s) ((",overallList$errorDescriptor,"), because all zero or not numeric!"))
		}
	}
}

"%exists%" <- function(standardValue, argsValue){
	
	if(!is.na(argsValue) & argsValue != "")	{
		return(argsValue)
	} else {
		return(standardValue)
	}
}

"%getData%" <- function(englischVersion, fileName){	
	#seperator <- ";"
	if (englischVersion) {
		return(read.csv(fileName, header=TRUE, sep=";", fileEncoding="ISO-8859-1", encoding="UTF-8"))
	} else {
		return(read.csv2(fileName, header=TRUE, sep=";", fileEncoding="ISO-8859-1", encoding="UTF-8"))
	}
}

"%checkIfDescriptorExists%" <- function(descriptor, dataSet) {
	if (is.data.frame(dataSet)) {
		return(descriptor %in% colnames(dataSet))
	} else {
		return(descriptor %in% dataSet)
	}	
}

"%GetDescriptorsAfterCheckIfDescriptorExists%" <- function(descriptor, dataSet) {
	return(descriptor[descriptor %checkIfDescriptorExists% dataSet])
	
}

"%GetDescriptorAfterCheckIfDescriptorNotExists%" <- function(descriptor, dataSet) {
	return(descriptor[!descriptor %checkIfDescriptorExists% dataSet])
	
}

"%contactAllWithAll%" <- function(vector1, vector2) {
	vectorTemp <- character(0)
	for(k in vector2) {
		if(k=="none") {
			vectorTemp <- c(vectorTemp,vector1)
		} else {
			vectorTemp <- c(vectorTemp,paste(vector1,k))
		}
	}
	return(vectorTemp)
}

checkIfDescriptorIsNaOrAllZero <- function(overallList){
	overallList$debug %debug% "checkIfDescriptorIsNaOrAllZero()"
	
	tempDescriptor <- overallList$descriptor 
	#& overallList$descriptor %in% colnames(overallList$iniDataSet)
	overallList$descriptor <- overallList$descriptor[colSums(!is.na(overallList$iniDataSet[overallList$descriptor])) != 0 & colSums(overallList$iniDataSet[overallList$descriptor] *1, na.rm = TRUE) > 0]
	overallList$errorDescriptor <- tempDescriptor %GetDescriptorAfterCheckIfDescriptorNotExists% overallList$descriptor
	#print(paste("descriptor:",overallList$descriptor))
	#print(paste("errorDescriptor:",overallList$errorDescriptor))
	if (length(overallList$errorDescriptor) > 0) {
		overallList %errorReport% "NotNumericOrAllZero"
	}
	
	if (length(overallList$descriptor) == 0) {
		print("... no descriptor set (all descriptors are zero or NA) - the program stopp!")
		overallList$stoppTheCalculation <- TRUE 
		#quitOwn(print("... no descriptor set - the program stopp!"))
	}
	
	return(overallList)
}

#quitOwn <- function(errorText) {
#	print(errorText)
#	#print("#########################exit########################")
#	#quit("no",0)
#	
#}

changeSaveName <- function(saveName) {

	regExpressionSpezialCharacter <- "\\$"
	if (nchar(saveName)> 70) {
		saveName <- gsub(regExpressionSpezialCharacter,";",substr(saveName,1,70))
	} else {
		saveName <- gsub(regExpressionSpezialCharacter,";",saveName)
	}
	saveName <- gsub("\\^", "", saveName);
	
	return(saveName)
}

preprocessingOfValues <- function(value, quit = FALSE, isColValue=FALSE) {

	if (!is.null(value)) {
		regExpressionCol <- "[^[:alnum:]|^_]|[[:space:]|\\^]"
		if(isColValue) {
			value <- unlist(strsplit(value, "$", fixed=TRUE))
		}
		value <- gsub(regExpressionCol,".",value)
	} else {
		if(quit) {
			quitOwn(print("... no value for x or y axis is selected - the program stopp!"))
		} else {
			return("none")
		}
	}
	return(value)
}

preprocessingOfDescriptor <- function(overallList) {
	overallList$debug %debug% "preprocessingOfDescriptor()"
	overallList$descriptor <- preprocessingOfValues(overallList$descriptor, TRUE, TRUE)	#descriptor is value for yAxis
	overallList$errorDescriptor <- overallList$descriptor %GetDescriptorAfterCheckIfDescriptorNotExists% overallList$iniDataSet 
	overallList$descriptor <- overallList$descriptor %GetDescriptorsAfterCheckIfDescriptorExists% overallList$iniDataSet
#	print(colnames(overallList$iniDataSet))
#	print(paste("desc:",overallList$descriptor))
#	print(paste("error:", overallList$errorDescriptor))
	if (length(overallList$errorDescriptor)>0) {
		overallList %errorReport% "notExists"
	}
	
	if(length(overallList$descriptor)==0) {
		print("... no descriptor set - this pass stopped!")
		overallList$stoppTheCalculation <- TRUE 
		#quitOwn(print("... no descriptor set - the program stopp!"))
	}
	
	return(overallList)
}

preprocessingOfxAxisValue <- function(overallList) {
	overallList$debug %debug% "preprocessingOfxAxisValue()"
	overallList$xAxis <- preprocessingOfValues(overallList$xAxis, TRUE, TRUE)
	
	if (overallList$filterXaxis != "none") {
		overallList$filterXaxis <- as.numeric(strsplit(overallList$filterXaxis, "$", fixed=TRUE)[[1]])
	} else {
		overallList$filterXaxis <- as.numeric(unique(overallList$iniDataSet[overallList$xAxis])[[1]])	#xAxis muss einen Wert enthalten ansonsten Bricht das Program weiter oben ab
	}
	
	return(overallList)
}

getSingelFilter <- function(filter, treatment, dataSet) {
	if(filter != "none") {
		return(strsplit(filter, "$", fixed=TRUE)[[1]])
	} else {
		return(as.character(unique(dataSet[treatment])[[1]]))
	}
}

preprocessingOfTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheTreatment()"
	
	if(!is.null(overallList$treatment)){
		overallList$treatment <- preprocessingOfValues(overallList$treatment)
		
		if(overallList$treatment != "none" & (overallList$treatment %checkIfDescriptorExists% overallList$iniDataSet)) {	
			overallList$filterTreatment <- getSingelFilter(overallList$filterTreatment, overallList$treatment, overallList$iniDataSet)

		} else {
			#overallList$treatment <- overallList$xAxis
			overallList$treatment <- "none"
			overallList$filterTreatment <- "none"
			print("... set 'filterTreatment' to 'none'!")		
		}
			
	} else {
		#overallList$treatment <- overallList$xAxis
		overallList$treatment <- "none"
		overallList$filterTreatment <- "none"
		print("... set 'filterTreatment' to 'none'!")
	}

	return(overallList)
}

preprocessingOfSecondTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheSecondTreatment()"
	
	if(!is.null(overallList$secondTreatment)){
		overallList$secondTreatment <- preprocessingOfValues(overallList$secondTreatment)

		if (overallList$secondTreatment != "none" & (overallList$secondTreatment %checkIfDescriptorExists% overallList$iniDataSet)) {
			overallList$filterSecondTreatment <- getSingelFilter(overallList$filterSecondTreatment, overallList$secondTreatment, overallList$iniDataSet)
			
		} else {
			overallList$secondTreatment <- "none"
			overallList$filterSecondTreatment <- "none"
			print("... set 'filterSecondTreatment' to 'none'!")
		}	
	} else {
		overallList$secondTreatment <- "none"
		overallList$filterSecondTreatment <- "none"
		print("... set 'filterSecondTreatment' to 'none'!")
	}
	return(overallList)
}

check <- function(value, checkValue="none"){
	if(!is.null(value)){
		return(value %GetDescriptorAfterCheckIfDescriptorNotExists% checkValue)
	} else {
		return(character(0))
	}
}

reduceWorkingDataSize <- function(overallList){
	overallList$debug %debug% "reduceWorkingDataSize()"
	overallList$iniDataSet <- overallList$iniDataSet[c(check(overallList$descriptor), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment))]
	return(overallList)
}

plotDummy <- function(overallList) {
	overallList$debug %debug% "plotDummy()"

	for(sN in overallList$saveName){
		openImageFile(overallList)
		#Cairo(width=as.numeric(imageWidth), height=as.numeric(imageHeight),file=paste(sN,saveFormat,sep="."),type=tolower(saveFormat),bg=bg,units="px",dpi=as.numeric(dpi), pointsize=20)
		plot(0,0);
		dev.off();
		print(paste("... create dummy-plot for", sN))
	}
}

setRowAndColNameOfFinalDataFrame <- function(overallList) {
	overallList$debug %debug% "setRowAndColNameOfFinalDataFrame()"
#	if (tolower(overallList$diagramTyp) == "boxplotstacked") {
#		overallList$rowName <- (overallList$yAxisName %contactAllWithAll% overallList$filterTreatment) %contactAllWithAll% overallList$filterSecondTreatment
#	} else {
		overallList$rowName <- (overallList$descriptor %contactAllWithAll% overallList$filterTreatment) %contactAllWithAll% overallList$filterSecondTreatment
#	}
	overallList$colName <- as.character(overallList$filterXaxis)
	
	return(overallList)
}

#alt wird nicht mehr gebraucht
#determineWhichRowOfTheIniDataAreTaken <- function(overallList) {
#	overallList$debug %debug% "determineWhichRowOfTheIniDataAreTaken()"	
#	filterTyp <- list(overallList$filterTreatment, overallList$filterSecondTreatment, overallList$filterXaxis)		
#	column <- c(overallList$treatment, overallList$secondTreatment, overallList$xAxis) 
#
#	tempWorkingDatasetSum <- as.data.frame(mapply(rep.int, times=length(rownames(overallList$iniDataSet)), x=ifelse(filterTyp[column!="none"] != "none",FALSE,TRUE)) |
#							 		   	   mapply("%in%", overallList$iniDataSet[column[column!="none"]], filterTyp[filterTyp!="none"]))
#	tempWorkingDatasetNA <- !is.na(overallList$iniDataSet[overallList$descriptor]!=is.na("NA"))
#						   
#   for(column in 1:length(tempWorkingDatasetSum)) {
#	   tempWorkingDatasetSum[,1] <- tempWorkingDatasetSum[,1] & tempWorkingDatasetSum[,column] & tempWorkingDatasetNA[,column]
#   }
#   overallList$overallResult <- tempWorkingDatasetSum[,1]
#						   
##						   L <- data.table(exampleDataFrame)
##						   M <- L[,lapply(.SD, mean), by=list(Days)]					   
#						   
#	#overallList$overallResult <- as.data.frame(apply(tempWorkingDatasetSum,2,'%&%',tempWorkingDatasetSum)[,1] & !is.na(overallList$iniDataSet[overallList$descriptor]!=is.na("NA")))
#
#	return(overallList)
#}

groupByFunction <- function(groupByList) {
	
	groupByList <- unlist(groupByList)
	return(unlist(groupByList[ifelse(groupByList != "none",TRUE,FALSE)]))
	
}

#getBooleanVectorForFilterValues <- function(groupedDataFrame, overallList) {
#	
#	#overallList$debug %debug% "getBooleanVectorForFilterValues()"
#	tempVector <- rep.int(TRUE,times=length(groupedDataFrame[,1]))
#	
#	if(overallList$treatment != "none") {
#		tempVector <- tempVector & groupedDataFrame[[overallList$treatment]] %in% overallList$filterTreatment
#	}
#	
#	if(overallList$secondTreatment != "none") {
#		tempVector <- tempVector & groupedDataFrame[[overallList$secondTreatment]] %in% overallList$filterSecondTreatment
#	}
#	
#	if(overallList$xAxis != "none") {
#		tempVector <- tempVector & groupedDataFrame[[overallList$xAxis]] %in% overallList$filterXaxis
#	}
#	
#	return(tempVector)
#}

getBooleanVectorForFilterValues <- function(groupedDataFrame, listOfValues) {
	
	tempVector <- rep.int(TRUE,times=length(groupedDataFrame[,1]))
	for(h in names(listOfValues)) {
		if(h != "none") {
			tempVector <- tempVector & groupedDataFrame[[h]] %in% listOfValues[[h]]
		}
	}
	return(tempVector)
}

#getRow <- function(rowOfOverallListRowName, isSecondTreatment) {
#	row <- list(first=substr(rowOfOverallListRowName,1,regexpr(" ",rowOfOverallListRowName)[1]-1), second=character())
#	
#	if(isSecondTreatment) {
#		row$second <- substr(rowOfOverallListRowName,regexpr(" ",rowOfOverallListRowName)[1]+1,nchar(rowOfOverallListRowName))
#	}
#	return(row)
#}

buildRowForOverallList <- function(i, des, listOfValues, dataSet, day) {
	rowString <- list(row=des, day=numeric())
	#for(k in 1:length(listOfValues)){
	for(k in listOfValues){
		if(k != "none") {
			rowString$row <- paste(rowString$row,dataSet[i,k])
		}
	}
	#rowString$day <- as.character(dataSet[i,day])
	return(rowString)
} 
##########################!!!!! hier ist ein  Fehler !!!!!!########################
fillOverallResult <- function(groupedDataFrame, overallList) {
	overallList$debug %debug% "fillOverallResult()"
	if(length(overallList$iniDataSet[,1]) > 0){
		for(i in 1:length(overallList$iniDataSet[,1])) {
		#for(i in 1:length(overallList$filterXaxis)) {
			for(des in overallList$descriptor) {
				rowAndColumn <- buildRowForOverallList(i,des, c(overallList$treatment, overallList$secondTreatment),overallList$iniDataSet, overallList$xAxis)
				
	#			if(overallList$secondTreatment != "none") {
	#				
	#				row <- paste(des, overallList$iniDataSet[i,overallList$treatment], overallList$iniDataSet[i,overallList$secondTreatment])
	#			} else {
	#				row <- paste(des, overallList$iniDataSet[i,overallList$treatment])
	#			}
				####!!!!!! FEHLER IN overallList$iniDataSet[i,des] !!!!!!!######
				###as.character(overallList$iniDataSet[i,overallList$xAxis])
				overallList$overallResult[rowAndColumn$row, as.character(overallList$iniDataSet[i,overallList$xAxis])] <- overallList$iniDataSet[i,des]
			}
		}
	} else {
		print("... no Value for the OverallResult-DataFrame - Wrong filter!")
		overallList$stoppTheCalculation <- TRUE
	}
	return(overallList)
}

buildList <- function(overallList) {
	overallList$debug %debug% "buildList()"
	newList <- list()
	
	newList[[overallList$treatment]] <- overallList$filterTreatment
	newList[[overallList$secondTreatment]] <- overallList$filterSecondTreatment
	newList[[overallList$xAxis]] <- overallList$filterXaxis
	
	return(newList)
}

getResultDataFrame <- function(overallList) {	
	overallList$debug %debug% "getResultDataFrame()"
	
	overallResult <- matrix(ncol = length(overallList$filterXaxis), nrow = length(overallList$rowName))
	colnames(overallResult) <- overallList$filterXaxis
	rownames(overallResult) <- overallList$rowName 
	overallList$overallResult <- as.data.frame(overallResult)
	
	groupBy <- groupByFunction(list(overallList$treatment, overallList$secondTreatment, overallList$xAxis))
	groupedDataFrame <- data.table(overallList$iniDataSet)
	groupedDataFrame <- as.data.frame(groupedDataFrame[,lapply(.SD, mean, na.rm=TRUE), by=groupBy])
	overallList$iniDataSet <- groupedDataFrame[getBooleanVectorForFilterValues(groupedDataFrame, buildList(overallList)),]
	
	return(fillOverallResult(groupedDataFrame, overallList))
}

setDefaultAxisNames <- function(overallList) {
	overallList$debug %debug% "setDefaultAxisNames()"
	
	if (overallList$xAxisName == "none") {
		overallList$xAxisName <- gsub('[[:punct:]]'," ",overallList$xAxis)
	}
	if (overallList$yAxisName == "none") {
		overallList$yAxisName <- gsub('[[:punct:]]'," ",overallList$descriptor)
	}
	return(overallList)
}


setColor <- function(overallList) {
	overallList$debug %debug% "setColor()"
	numberOfColors <- ifelse(tolower(overallList$diagramTyp) == "boxplotstacked", length(overallList$descriptor), length(overallList$rowName))
	
#	if(tolower(overallList$diagramTyp) == "boxplotstacked") {
#		numberOfColors <- length(overallList$descriptor)
#	} else {
#		numberOfColors <- length(overallList$rowName)
#	}
	
	if (!as.logical(overallList$isGray)) {
		#11 Spectral
		#8  Dark2
		return(colorRampPalette(c(brewer.pal(11, "Spectral")))(numberOfColors))
	} else {
		return(colorRampPalette(c(brewer.pal(9, "Greys")))(numberOfColors))
	}
}

normalizeToHundredPercent <-  function(whichRows, overallResult) {
	return(t(apply(overallResult[whichRows,], 1, function(x,y){(100*x)/y}, y=colSums(overallResult[whichRows,]))))
}

writeLatexFile <- function(saveNameLatexFile, saveNameImageFile="", o="") {

	latexText <- paste( "\\item [{\\includegraphics[height=\\ScaleIfNeeded,width=\\ConstForImageWidth, keepaspectratio]{",
						"\\string\"",
						gsub("\\.", "\\\\lyxdot ", ifelse(saveNameImageFile=="",saveNameLatexFile,saveNameImageFile),),
						ifelse(o=="", "", paste("\\lyxdot ",gsub('[[:punct:]]'," ",o),sep="")),
						"\\string\"",
						"}}]~","\n", sep="")
	
	write(x=latexText, append=TRUE, file=paste(saveNameLatexFile,"tex",sep="."))
	
}

openImageFile <- function(overallList, extraString="") {
	overallList$debug %debug% "openImageFile()"
#print(overallList)
	filename <- preprocessingOfValues(paste(overallList$saveName,extraString,sep=""), FALSE, FALSE)
	Cairo(width=as.numeric(overallList$imageWidth), height=as.numeric(overallList$imageHeight),file=paste(filename,overallList$saveFormat,sep="."),type=tolower(overallList$saveFormat),bg=overallList$bgColor,units="px",dpi=as.numeric(overallList$dpi), pointsize=20)			
}

#plotDiagram <- function(rowIndex, overallList) {
#	if (y==1) {
#		plot(overallList$filterXaxis, overallList$overallResult[rowIndex,], main="", type="b", xlab=overallList$xAxisName, col=overallList$color[rowIndex], ylab=overallList$yAxisName, pch=rowIndex, lty=1, lwd=3, ylim=c(min(overallList$overallResult,na.rm=TRUE),max(overallList$overallResult,na.rm=TRUE)))
#	} else {	
#		points(overallList$filterXaxis, overallList$overallResult[rowIndex,], type="b", col=overallList$color[rowIndex], pch=rowIndex, lty=1, lwd=3 )
#	}	
#}

makeDepthBoxplotDiagram <- function(h, overallList) {

	overallList$debug %debug% "makeDepthBoxplotDiagram()"
	overallList$symbolParameter <- 15
	
	if (h==1) {
		openImageFile(overallList)
	}
	par(mar=c(4.1,4.1,2.1,2.1))
	plot.depth(as.matrix(overallList$overallResult), plot.type=h, xlabel=overallList$xAxisName, l.width=12, lp.color=overallList$color)
	
	grid()
	if (h==1) {
		dev.off()
	}
	if(overallList$appendix) {
		writeLatexFile("appendixImage", overallList$saveName)
	}
	
	return(overallList)
}


CheckIfOneColumnHasOnlyValues <- function(overallResult) {	
	return(ifelse((sum((colSums(overallResult,na.rm=TRUE)) == 0)-length(overallResult)) == -1,TRUE, FALSE))
}

makeLinearDiagram <- function(h, overallList) {
	overallList$debug %debug% "makeLinearDiagram()"
		overallList$symbolParameter <- 1:length(overallList$rowName)
		
	if(!CheckIfOneColumnHasOnlyValues(overallList$overallResult)) {
		if (h==1) {
			openImageFile(overallList)
		}
		par(mar=c(4.1,4.1,1.1,2.1))
		#overallList$debug %debug% "makeLinearDiagram() nach openImageFile"
		#for(y in 1:length(overallList$rowName)) {
		
		#numberOfNaNOfEachColumn <- CheckIfOneColumnHasValues(overallList$overallResult)
	
		firstPlot <- TRUE
		for(y in overallList$symbolParameter) {	
			numberOfNaN <- length(overallList$overallResult[y,]) - (length(overallList$overallResult[y,]) - sum(is.na(overallList$overallResult[y,]),na.rm=TRUE))
			
			if(numberOfNaN == length(overallList$overallResult[y,])) {
				print(paste("... No Plotting of the treatment '",overallList$rowName[y],"'(No Values)"))
				
			#} else if (numberOfNaN > 1 & numberOfNaN < (length(overallList$overallResult[y,])-1)) {
			} else if (numberOfNaN > 0 & numberOfNaN < (length(overallList$overallResult[y,])-1)) {
				#test[y,is.nan(test[y,])]  <- NA
				#sum(match(is.na(test[y,]),FALSE),na.rm=TRUE)
				
				newCoords <- seq(min(overallList$filterXaxis,na.rm=TRUE),max(overallList$filterXaxis,na.rm=TRUE),1)
				newValue <- approx(overallList$filterXaxis, overallList$overallResult[y,],xout=newCoords,method="linear")
				
				naVector <- is.na(overallList$overallResult[y,])
				overallResultWithNaValues <- overallList$overallResult[y,]
				overallList$overallResult[y,naVector] <- newValue$y[overallList$filterXaxis[naVector]]
				#overallList$filterXaxis[naVector]
				
				if (firstPlot) {
					firstPlot <- FALSE
					plot(overallList$filterXaxis, overallList$overallResult[y,], main="", type="c", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult,na.rm=TRUE),max(overallList$overallResult,na.rm=TRUE)))
				} else {
					points(overallList$filterXaxis, overallList$overallResult[y,], type="c", col=overallList$color[y], pch=y, lty=1, lwd=3 )	
				}
				
	#			if (y == 1) {
	#				plot(newValue$x[!is.na(match(newCoords, overallList$filterXaxis))], newValue$y[!is.na(match(newCoords, overallList$filterXaxis))], main="", type="c", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult,na.rm=TRUE),max(overallList$overallResult,na.rm=TRUE)))
	#			} else {
	#				points(newValue$x[!is.na(match(newCoords, overallList$filterXaxis))], newValue$y[!is.na(match(newCoords, overallList$filterXaxis))], type="c", col=overallList$color[y], pch=y, lty=1, lwd=3 )	
	#			}
				points(overallList$filterXaxis, overallResultWithNaValues, type="p", col=overallList$color[y], pch=y, lty=1, lwd=3 )
			} else if (numberOfNaN == 0 | (length(overallList$overallResult[y,])-1) == numberOfNaN){
				#plotDiagram(y, overallList, type)
				if (firstPlot) {
					firstPlot <- FALSE
					plot(overallList$filterXaxis, overallList$overallResult[y,], main="", type="b", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult,na.rm=TRUE),max(overallList$overallResult,na.rm=TRUE)))
				} else {	
					points(overallList$filterXaxis, overallList$overallResult[y,], type="b", col=overallList$color[y], pch=y, lty=1, lwd=3 )
				}	
			}
		}
	
		grid()
		#overallList$debug %debug% "makeLinearDiagram() nach for-Schleife"
		if(h==1) {
			dev.off()
		}
		if(overallList$appendix) {
			writeLatexFile("appendixImage", overallList$saveName)
		}

	} else {
		print("... only one column has values, so it will be plot as depth plot!")
		overallList$overallResult <- t(overallList$overallResult)
		overallList$diagramTyp <- "boxplothorizontal"
		overallList$xAxisName <- paste(overallList$xAxisName,rownames(overallList$overallResult)[!is.na(overallList$overallResult)])
		overallList <- makeBoxplotDiagram(h, overallList, TRUE)
	}
	
	return(overallList)
}


makeBoxplotStackedDiagram <- function(h, overallList) {
	overallList$debug %debug% "makeBoxplotStackedDiagram()"
	overallList$overallResult[is.na(overallList$overallResult)] <- 0
	#overallList$overallResult <- normalizeToHundredPercent(overallList$overallResult)
	overallList$symbolParameter <- 15
	par(mar=c(4.1,4.1,2.1,2.1))

	stackedImages <- overallList$filterTreatment %contactAllWithAll% overallList$filterSecondTreatment
	
	for(o in stackedImages) {
		overallList$debug %debug% paste("makeBoxplotStackedDiagram with the descriptor: ",overallList$saveName,o)
		if (h==1) {
			#print(paste(overallList$saveName,o,"tex",sep="."))
			#write(x="", append=FALSE, file=paste(overallList$saveName,o,"tex",sep="."))
			openImageFile(overallList, o)			
		}
		
		whichRowsShouldBePlotted <- overallList$descriptor %contactAllWithAll% o
		plotThisValues <- normalizeToHundredPercent(whichRowsShouldBePlotted,overallList$overallResult)
		
		barplot(plotThisValues, col=rev(overallList$color), main=o, xlab=overallList$xAxisName, ylab=overallList$yAxisName, ylim=c(0,100), mar=c(4.1,4.1,2.1,2.1))
		#barplot(overallList$overallResult[match(whichRowsShouldBePlotted,rownames(overallList$overallResult)),], col=rev(overallList$color), main=o, xlab=overallList$xAxisName, ylab=overallList$yAxisName, ylim=c(0,100), mar=c(4.1,4.1,2.1,2.1))
		
		if (h==1) {
			dev.off()
			#print(paste("savename:",overallList$saveName))
			writeLatexFile(overallList$saveName, preprocessingOfValues(paste(overallList$saveName,o,sep=""), FALSE, FALSE))
		}
	}

	return(overallList)
}	

makeBoxplotDiagram <- function(h, overallList, horizontal=FALSE) {
	overallList$debug %debug% "makeBoxplotDiagram()"
	overallList$symbolParameter <- 15
	
	if (h==1) {
		openImageFile(overallList)
	}
	par(mar=c(4.1,4.1,2.1,2.1))
	#print(rownames(overallList$overallResult)[!is.na(overallList$overallResult)])
	#print(overallList$overallResult <- overallList$overallResult[!is.na(overallList$overallResult)])
	if(horizontal) {
		barplot(as.matrix(overallList$overallResult), beside= TRUE, axisnames=FALSE, main="", xlab=overallList$xAxisName, ylab=overallList$yAxisName, col=overallList$color, space=c(0,1), ylim=c(0,max(overallList$overallResult,na.rm=TRUE)))
	} else {
		barplot(as.matrix(overallList$overallResult), beside= TRUE, main="", xlab=overallList$xAxisName, ylab=overallList$yAxisName, col=overallList$color, width=12, space=c(0,1), ylim=c(0,max(overallList$overallResult,na.rm=TRUE)))
	}
	
	grid()
	if (h==1) {
		dev.off()
	}
	if(overallList$appendix) {
		writeLatexFile("appendixImage", overallList$saveName)
	}
	
	return(overallList)
}

buildLegend <- function(overallList) {
	overallList$debug %debug% "buildLegend()"
	
	if(tolower(overallList$diagramTyp) == "boxplotstacked") {
		legendeFileName <- paste("legendBoxStacked",overallList$descriptor[1])
		legendText <- overallList$descriptor
		writeLatexFile("legendBoxStacked", legendeFileName)
		
	} else {
		legendeFileName <- "legend"
		legendText <- overallList$filterTreatment %contactAllWithAll% overallList$filterSecondTreatment
	}
	file <- paste(legendeFileName,overallList$saveFormat,sep=".")
	
	if(!file.exists(file)){
		Cairo(width=as.numeric(overallList$imageWidth), height=length(legendText)*(2.4*20),file=file,type=tolower(overallList$saveFormat),bg=overallList$bg,units="px",dpi=as.numeric(overallList$dpi), pointsize=20)
		par(mar = c(0,0,0,0))
		plot.new()
		legend("left", legendText, col= rev(overallList$color), pch=overallList$symbolParameter, bty="n",cex=2.0)
		dev.off()
	}
	##length(legendText)*(as.numeric(overallList$dpi)-20)
}

makeDiagrams <- function(overallList) {
	overallList$debug %debug% "makeDiagrams()"
	durchlauf <- ifelse(overallList$showResultInR, 2, 1)
	
	#standardPar <- par(no.readonly = TRUE)
	for(h in 1:durchlauf) {
		
		if (tolower(overallList$diagramTyp) == "boxplothorizontal") {
			overallList <- makeBoxplotDiagram(h, overallList, TRUE)
		} else if (tolower(overallList$diagramTyp) == "boxplot") {
			overallList <- makeBoxplotDiagram(h, overallList, FALSE)
			
		} else if (tolower(overallList$diagramTyp) == "boxplotstacked") {
			overallList <- makeBoxplotStackedDiagram(h, overallList)
			
		} else if(tolower(overallList$diagramTyp) == "!boxplot"){
			overallList <- makeLinearDiagram(h, overallList)
			
		} else {
			print("Error - overallList$diagramTyp is undefined!")
		}
	}

	buildLegend(overallList)
	#par(standardPar)
	#dev.off()
}

createDefaultImage <- function(file) {
	print(paste("... create dummy '",file,"'",sep=""))
	Cairo(width=900, height=70,file=file,type="png",bg="transparent",units="px",dpi=90)
	par(mar = c(0,0,0,0))
	plot.new()
	legend("left", "no values", col= c("black"), pch=1, bty="n")
	dev.off()
}

checkIfAllNecessaryFilesAreThere <- function(saveFormat) {
	
	#if(typOfStartOptions == "report") {
		print("... check if the legend are there")
		
		file <- paste("legend",saveFormat,sep=".")
		if(!file.exists(file)){
			createDefaultImage("legend.png")
		}
		
		file <- c("legendBoxStacked", "side.fluo.normalized.histogram.bin.1.0_25;side.fluo.normalized.histogr", "side.nir.normalized.histogram.bin.1.0_25;side.nir.normalized.histogram", "appendixImage")
		
		for(i in file){
			if(!file.exists(paste(i,".tex",sep=""))){
				if(!file.exists("noValues.png")){
					createDefaultImage("noValues.png")
				}
				print(paste("... create dummy: '",paste(i,".tex",sep=""),"'",sep=""))
				writeLatexFile(i, "noValues")
			}
		}		
	#}
}

buildBlacklist <- function(workingDataSet, descriptorSet) {
	
	searchString <- ".histogram."
	searchString <- paste(searchString,"mark",sep = "|")
	#searchString <- paste(searchString,"someOneElse",sep = "|")
	
	additionalDescriptors <- c(descriptorSet, "Day (Int)","Day","Time", "Plant ID", "vis.side", "fluo.side", "nir.side", "vis.top", "fluo.top", "nir.top")
	
	return(c(colnames(workingDataSet)[grep(searchString,colnames(workingDataSet), ignore.case = TRUE)], preprocessingOfValues(additionalDescriptors, FALSE, TRUE)))
}

initRfunction <- function(DEBUG = FALSE){
	
	if(DEBUG) {
		
		options(error = quote({
			#sink(file="error.txt", split = TRUE);
			dump.frames();
			print(attr(last.dump,"error.message"));
			#x <- attr(last.dump,"error.message")
			traceback();
			#sink(file=NULL);		
			#q()
		}))
	} else {	
		options(error = NULL)
	}
	memory.limit(size=3500)

	while(!is.null(dev.list())) {
		dev.off()
	}
}

startOptions <- function(typOfStartOptions = "test", DEBUG=FALSE){
	
	initRfunction(DEBUG)
	#typOfStartOptions = "test"
	typOfStartOptions <- tolower(typOfStartOptions)
	
	args <- commandArgs(TRUE)
	print("#### Arguments")
	print(args)
	print("####")
	
	saveFormat <- "png"
	imageWidth <- "1280"
	imageHeight <- "768"
	dpi <- "216" ##90
	
	isGray="FALSE"
	bgColor <- "transparent"
	showResultInR <- "FALSE"
	
	treatment <- "Treatment"
	filterTreatment <- "none"
	
	secondTreatment <- "none"
	filterSecondTreatment <- "none"
	
	xAxis <- "Day (Int)" 
	xAxisName <- "Day"
	filterXaxis <- "none"
	
	diagramTypVector <- vector()
	descriptorSet <- vector()
	descriptorSetName <- vector()
	
	fileName <- "error"

	appendix <- FALSE
	#appendix <- TRUE
	
	if (length(args) < 1) {
		englischVersion <- FALSE
	} else {
		englischVersion <- TRUE
	}

	if (typOfStartOptions == "all" | typOfStartOptions == "report" | typOfStartOptions == "allmanual") {
		
		if (typOfStartOptions != "allmanual") {
			fileName <- fileName %exists% args[1]
		} else {
			fileName <- "numeric_data.MaizeAnalysisAction_ 1116BA_new3.csv"
			#fileName <- "report.csv" ## englischVersion <- TRUE setzen!!
			#fileName <- "testDataset3.csv"
		}
		
		saveFormat <- saveFormat %exists% args[2]
		appendix <- appendix %exists% args[3]
		treatment <- treatment %exists% args[4]
		secondTreatment <- secondTreatment %exists% args[5]
		
		if (fileName != "error") {
			workingDataSet <- englischVersion %getData% fileName
			
			#!boxplot
			if (typOfStartOptions == "all") {
				descriptorSet <- colnames(workingDataSet)
				descriptorSetName <- descriptorSet
				
			} else { #Report
				descriptorSet <- c("Weight A (g)","Weight B (g)","Water (weight-diff)","side.height.norm (mm)","side.width.norm (mm)","side.area.norm (mm^2)", "top.area.norm (mm^2)",
						"side.fluo.intensity.chlorophyl.average (relative)","side.fluo.intensity.phenol.average (relative)",
						"side.nir.intensity.average (relative)","side.leaf.count.median (leafs)","side.bloom.count (tassel)",
						"side.leaf.length.sum.norm.max (mm)", "volume.fluo.iap","volume.iap (px^3)", "volume.iap_max", "volume.lt (px^3)",
						"volume.iap.wue", "side.nir.wetness.plant_weight_drought_loss", "top.nir.wetness.plant_weight_drought_loss", "side.nir.wetness.av", "top.nir.wetness.av",
						"side.area.relative", "side.height.norm.relative", "side.width.norm.relative", "top.area.relative", "side.area.relative", "volume.iap.relative")
			
				# "digital biomass IAP (pixel^3)","digital biomass KeyGene (pixel^3)", 
				descriptorSetName <- c("weight before watering (g)","weight after watering (g)", "water weight (g)", "height (mm)", "width (mm)", "side area (mm^2)", "top area (mm^2)",
						"chlorophyl intensity (relative intensity/pixel)", "fluorescence intensity (relative intensity/pixel)", "nir intensity (relative intensity/pixel)",
						"number of leafs (leaf)", "number of tassels (tassel)", "length of leafs plus stem (mm)", "volume based on FLUO (IAP) (px^3)", "volume based on RGB (IAP) (px^3)", "volume based on max RGB-image (IAP) (px^3)", "volume based on RGB (LemnaTec) (px^3)",
						"volume based water use efficiency", "weighted loss through drought stress (side)", "weighted loss through drought stress (top)", "Average wetness of side image", "Average wetness of top image",
						"relative projected side area (%)", "relative plant height (%)", "relative plant width (%)", "relative projected top area (%)", "relative projected side area (%)", "relative volume (IAP based formular - RGB) (%)")			
	
		}
			diagramTypVector <- rep.int("!boxplot", times=length(descriptorSetName))

			#boxplotStacked
			descriptorSet <- c(descriptorSet, "side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255",
							   				  "side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
			descriptorSetName <- c(descriptorSetName, "NIR absorption class (%)", "red fluorescence histogram (%)")
			diagramTypVector <- c(diagramTypVector, "boxplotStacked", "boxplotStacked")
			
			if(appendix) {
				blacklist <- buildBlacklist(workingDataSet)
				descriptorSet <- colnames(workingDataSet[!as.data.frame(sapply(colnames(workingDataSet),'%in%', blacklist))[,1]])
				descriptorSetName <- descriptorSet
				diagramTypVector <- rep.int("!boxplot", times=length(descriptorSetName))
			}
		
		
		}
		
	} else if (typOfStartOptions == "test"){
		
		
		#fileName <- "1107BA_Corn_new2.csv"
			
		#workingDataSet <- read.csv2(fileName, header=TRUE, sep=";", fileEncoding="ISO-8859-1", encoding="UTF-8")                   
		#descriptorSet <- c("side.nir.intensity.average.norm","side.nir.intensity.average","side.hull.area.norm","side.height.norm","Gewicht.B","water.consumption")                   
		#descriptorSetName <- c("nir intensity (rel. intensity/px)","nir intensity (rel. intensity/px)","convex hull area (mm^2)","height (mm)","target weight (g)","water consumption")
			
		#descriptorSet <- c("side.nir.intensity.average.norm","Gewicht.B")                   
		#descriptorSetName <- c("nir intensity (rel. intensity/px)","target weight (g)")
				
		#descriptorSet <- c("digital.biomass.unnormal","digital.biomass.normal","mark3.y")                   
		#descriptorSetName <- c("digital biomass (mm^3)","digital biomass (mm^3)","mark (% from image height)")
			
		#descriptorSet <- c("digital.biomass.keygene.norm","side.area","top.area")                   
		#descriptorSetName <- c("digital biomass (mm^3)","test1", "test2")
			
#		descriptorSet <- c("side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255",
#					"side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
#		descriptorSetName <- c("NIR absorption class (%)", "chlorophyll fluorescence histogram (%)")
			
#		descriptorSet <- c("side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255")
#		descriptorSetName <- c("NIR absorption class (%)")
			
		#descriptorSet <- c("Plant ID$Treatment$Hallo$Wert1$Repl ID")
		#descriptorSetName <- c("VariableMix")
	
		#descriptorSet <- c("side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
		#descriptorSetName <- c("red fluorescence histogram (%)")
	
	
		#descriptorSet <- c("Weight B (g)","side.height.norm (mm)","side.width.norm (mm)","side.area.norm (mm^2)",
		#		"side.fluo.intensity.chlorophyl.average (relative)","side.fluo.intensity.phenol.average (relative)",
		#		"side.nir.intensity.average (relative)",
		#		"side.leaf.count.median (leafs)","side.bloom.count (tassel)","side.leaf.length.sum.norm.max (mm)")
			
		# "digital biomass IAP (pixel^3)","digital biomass KeyGene (pixel^3)", 
		#descriptorSetName <- c("weight (g)","height (mm)", "width (mm)", "side area (pixel^2)",
		#		"chlorophyl intensity (relative intensity/pixel)", "fluorescence intensity (relative intensity/pixel)", "nir intensity (relative intensity/pixel)",
		#		"number of leafs (leaf)", "number of tassels (tassel)", "length of leafs plus stem (mm)")
		
#		descriptorSet <- c("side.area (px)","side.area.norm (mm^2)")
#		descriptorSetName <- c("side area uncorrected (mm^2)", "side area corrected (mm^2)")
		
		#descriptorSet <- c("Hallo2")
		#descriptorSetName <- c("Test")
		
		#descriptorSet <- colnames(workingDataSet)
		#descriptorSetName <- colnames(workingDataSet)
		
		#treatment <- "Treatment"
		treatment <- "Condition"
		#filterTreatment <- "dry$normal$wet"
		#filterTreatment <- "dry$normal"
		#filterTreatment <- "normal bewaessert$Trockentress"
		#filterTreatment <- "ganz"
		filterTreatment <- "none"
		##filterTreatment <- "Deutschland$Spanien$Italien$China"
		
		secondTreatment <- "none"
		#secondTreatment <- "Variety"
		filterSecondTreatment <- "none"
		
		#secondTreatment <- "secondTreatment"
		#filterSecondTreatment <- "a$c"
		
		###1116BA#########6 8 10 12 13 14 15 16 17 19 20 21 22 23 24 25 26 27 28 29 30 31 33 34 35 36 37 38 39 40
		#filterXaxis <- c("6$8$10$12$13$14$15$16$20$21$22$23$26$27$28$29$30$31$33$34$35$36$37$38")
		#filterXaxis <- c("6$8$13")
		###1107BA#########2$4$6$8$10$12$13$15$16$21$22$25$27$29$30$31$33$35$36$37$39$41$42$43$45$47$49$50$51$55$57$59$61$63$64
		#filterXaxis <- c("2$4$6$8$10$12$13$15$16$21$22$25$27$29$30$31$33$35$36$37$39$41$43$45$47$49$50$51$55$57$59$61$63$64")
		#filterXaxis <- c("6$8$10")
		filterXaxis <- "none"
		#filterXaxis <- c("6$8$10$12$13$14$15$16$20$21$22$23$26$27$28$29$30$31$33$34$35$36$37$38")
		
		#treatment <- "Treatment"
		##treatment <- "Variety"
		#treatment <- "none"
		
		#diagramTyp="boxplotStacked"
		diagramTyp="!boxplot"
		#diagramTyp="boxplot"
		
		bgColor <- "transparent"
		isGray="FALSE"
		#transparent <- "TRUE"
		#legendUnderImage <- "TRUE"
		showResultInR <- FALSE
		
		fileName <- "numeric_data.MaizeAnalysisAction_ 1116BA_new3.csv"
		#fileName <- "testDataset2.csv"
		#fileName <- "report.csv"
		#fileName <- "testDatenset3.csv"
		englischVersion <- FALSE
		workingDataSet <- englischVersion %getData% fileName
		
		#descriptor <- c("Hallo2")
		#descriptor <- c("Plant ID","Treatment","Hallo","Wert1", "Repl ID")
		#descriptor <- c("Repl ID")		
		#descriptorSet <- c("Plant ID")
		descriptorSet <- c("side.area.norm (mm^2)")
		descriptorSetName <- c("Das ist ein Testname")

		#descriptorSet <- c("Plant ID$Treatment$Hallo$Wert1$Repl ID")
		
		diagramTypVector <- rep.int(diagramTyp, times=length(descriptorSetName))
		
		
		saveName <- "test"
		yAxisName <- "test2"
		debug <- TRUE
		iniDataSet = workingDataSet
		#descriptor <- c("Plant ID$Treatment$Hallo$Wert1$Repl ID")
		#descriptor <- c("side.area.norm (mm^2)")
		#descriptorSet <- c("side.fluo.bloom.area.size (mm^2)")
		#descriptor <- c("side.fluo.bloom.area.size (mm^2)")
		descriptor <- descriptorSet
		#descriptor <- c("Repl ID")
		#descriptor <- c("side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255")
		appendix <- FALSE
		stoppTheCalculation <- FALSE
		
}

	if(fileName != "error" & length(descriptorSet) > 0){
		time <- system.time({
			for (y in 1:length(descriptorSet)) {	
				print(paste("... generate diagram of '", descriptorSet[y],"'",sep=""))
				valuesAsDiagram(iniDataSet = workingDataSet, saveName = descriptorSet[y], saveFormat = saveFormat, imageWidth = imageWidth,
								imageHeight = imageHeight, dpi = dpi, diagramTyp = diagramTypVector[y], isGray = isGray, treatment = treatment,
								filterTreatment = filterTreatment, secondTreatment = secondTreatment, filterSecondTreatment = filterSecondTreatment,
								filterXaxis = filterXaxis, xAxis = xAxis, descriptor = descriptorSet[y], showResultInR = showResultInR, 
								xAxisName = xAxisName, yAxisName = descriptorSetName[y], bgColor = bgColor,	debug = DEBUG, appendix=appendix)
				print("... ready")
				
			}
			checkIfAllNecessaryFilesAreThere(saveFormat)
		},TRUE)
	print("... All ready")		
	print(time)	
	
	} else {
		print("No filename or no descriptor!")
	}
}

valuesAsDiagram <- function(iniDataSet, saveName="OutputDiagramm", saveFormat="png", imageWidth="1280",
		imageHeight="768", dpi="90", diagramTyp="!boxplot", isGray="false", treatment="Treatment",
		filterTreatment="none", secondTreatment="none", filterSecondTreatment="none", 
		filterXaxis="none", xAxis="Day (Int)", descriptor="side.area", showResultInR=FALSE, xAxisName="none", yAxisName="none",
		bgColor="transparent", debug = FALSE, appendix=FALSE, stoppTheCalculation=FALSE) {			

	overallList <- list(iniDataSet=iniDataSet, saveName=saveName, saveFormat=saveFormat, imageWidth=imageWidth, imageHeight=imageHeight, dpi=dpi,
						diagramTyp=diagramTyp, isGray=isGray, treatment=treatment, filterTreatment=filterTreatment,
						secondTreatment=secondTreatment, filterSecondTreatment=filterSecondTreatment, filterXaxis=filterXaxis, xAxis=xAxis, descriptor=descriptor,
						showResultInR=showResultInR, xAxisName=xAxisName, yAxisName=yAxisName, debug=debug, 
						bgColor=bgColor, errorDescriptor=character(), rowName=character(), colName=character(),
						overallResult=data.frame(), color=numeric(), symbolParameter=15, appendix=appendix, stoppTheCalculation=stoppTheCalculation)
		
	#library for save images
	#install.packages(c("Cairo"), repos="http://cran.r-project.org", dependencies = TRUE)
	library("Cairo")
	#library for colors
	#install.packages(c("RColorBrewer"), repos="http://cran.r-project.org", dependencies = TRUE)
	library("RColorBrewer")
	#install.packages(c("data.table"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(data.table)
	#install.packages(c(mvbutils), repos="http://cran.r-project.org", dependencies = TRUE)
	#library(mvbutils)
	
	#library(Hmisc)
	#source("plotDepth.R")
	
	overallList$debug %debug% 1
	
	overallList$saveName <- changeSaveName(overallList$saveName)
	overallList <- preprocessingOfDescriptor(overallList)
	
	if(!overallList$stoppTheCalculation) {
		overallList <- preprocessingOfxAxisValue(overallList)
		overallList <- preprocessingOfTreatment(overallList)
		#backupOverallList <- overallList
		overallList <- preprocessingOfSecondTreatment(overallList)
		#overallList$iniDataSet <- reduceWorkingDataSize(overallList$descriptor, overallList$xAxis, overallList$treatment, overallList$secondTreatment)
		overallList <- checkIfDescriptorIsNaOrAllZero(overallList)
		if(!overallList$stoppTheCalculation) {
			overallList <- reduceWorkingDataSize(overallList)
			#backupOverallList2 <- overallList		
			overallList <- setRowAndColNameOfFinalDataFrame(overallList)
			overallList <- getResultDataFrame(overallList)
			if(!overallList$stoppTheCalculation) {
				overallList$color <- setColor(overallList) 
				overallList <- setDefaultAxisNames(overallList)			
				#backupOverallList3 <- overallList
				makeDiagrams(overallList)
			}
		}
	}
	if(overallList$stoppTheCalculation) {
		plotDummy(overallList)
	}
}

######### START #########
#rm(list=ls(all=TRUE))
#startOptions("test", TRUE)
#startOptions("allmanual", TRUE)
startOptions("report", FALSE)
rm(list=ls(all=TRUE))