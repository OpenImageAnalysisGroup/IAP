# TODO: Add comment
# 
# Author: Entzian
###############################################################################

"%debug%" <- function(debug, debugNumber) {
	if (debug) {
		print(paste("DebugBreakPoint: ", debugNumber))
	}
}

"%checkEqual%" <- function(treat, seconTreat) {
	if(treat == seconTreat) {
		print("SecondTreatment are the same value as Treatment so it set to \"none\"")
		return("none")
	} else {
		return(seconTreat)
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

"%getData%" <- function(separation, fileName){	
	#separation <- ";"
	print("... Read input file")
	
	preScanForPointOrComma <- scan(file=fileName, what=character(0), nlines=2, sep="\n")
	preScanForPointOrComma <- paste(preScanForPointOrComma[2],",.", sep="")
	allCharacterSeparated <- table(strsplit(toupper(preScanForPointOrComma), '')[[1]])
	
	if(allCharacterSeparated["."] > allCharacterSeparated[","]) {
		print("... english Version")
		return(read.csv(fileName, header=TRUE, sep=separation, fileEncoding="ISO-8859-1", encoding="UTF-8"))
	} else {
		print("... german Version")
		return(read.csv2(fileName, header=TRUE, sep=separation, fileEncoding="ISO-8859-1", encoding="UTF-8"))
	}
	
#	if (englischVersion) {
#		return(read.csv(fileName, header=TRUE, sep=separation, fileEncoding="ISO-8859-1", encoding="UTF-8"))
#	} else {
#		return(read.csv2(fileName, header=TRUE, sep=separation, fileEncoding="ISO-8859-1", encoding="UTF-8"))
#	}
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
			vectorTemp <- c(vectorTemp,paste(vector1,k, sep = "#"))
		}
	}
	return(vectorTemp)
}

"%allColnamesWithoutThisOnes%" <- function(dataSet, withoutColNamesVector) {
	return(colnames(dataSet)[!colnames(dataSet) %in% withoutColNamesVector])
}

reNameHist <-  function(colNameWichMustBind) {
	colNameWichMustBind <- as.character(colNameWichMustBind)
	positions <- which(strsplit(colNameWichMustBind, '')[[1]]=='.')
	colNameWichMustBind <- substr(colNameWichMustBind,positions[length(positions)]+1,nchar(colNameWichMustBind))
	
	regExpressionSpezialCharacter <- "\\_"
	colNameWichMustBind <- gsub(regExpressionSpezialCharacter,"..",colNameWichMustBind)
	
	return(colNameWichMustBind)	
}

reNameColumn <-  function(plotThisValues, columnNameReplace="name", columnNameWhichUsedToReplace="primaerTreatment") {
	if(!is.null(plotThisValues[columnNameWhichUsedToReplace])){
		plotThisValues[columnNameReplace] <- plotThisValues[columnNameWhichUsedToReplace]
	}
	
	return(plotThisValues)
}

swap <- function(listWithTwoParameter) {
	temp <- listWithTwoParameter[[1]]
	listWithTwoParameter[[1]] <- listWithTwoParameter[[2]]
	listWithTwoParameter[[2]] <- temp
	return(listWithTwoParameter)
}

changeWhenTreatmentNoneAndSecondTreatmentNotNone <- function(listOfTreat, listOfFilterTreat) {
	if(listOfTreat[[1]] == "none" & listOfTreat[[2]] != "none") {
		print("The values of Treatment and SecondTreamt are changed (filter values also)")
		return(list(swap(listOfTreat),swap(listOfFilterTreat)))	
	} else {
		return(list(listOfTreat,listOfFilterTreat))
	}
}

checkOfTreatments <- function(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug) {
	debug %debug% "Start of checkOfTreatments()"
	
	treatment <- treatment %exists% args[4]
	secondTreatment <- secondTreatment %exists% args[5]
	secondTreatment <- treatment %checkEqual% secondTreatment
	
	listOfTreat <- list(treatment=treatment, secondTreatment=secondTreatment)
	listOfFilterTreat <- list(filterTreatment=filterTreatment, filterSecondTreatment=filterSecondTreatment)	## wird erstmal noch nichts weiter mit gemacht! nur geswapt falls notwendig

	for(k in names(listOfTreat)){
		if(listOfTreat[[k]] != "none") {
			overallTreat <- list(iniDataSet=workingDataSet, descriptor=listOfTreat[[k]], debug=debug, stoppTheCalculation = FALSE, errorDescriptor=character())
			overallTreat <- preprocessingOfDescriptor(overallTreat)
			
			if(!overallTreat$stoppTheCalculation) {
				overallTreat <- checkIfDescriptorIsNaOrAllZero(overallTreat, FALSE)
				
				if(!overallTreat$stoppTheCalculation) {
					listOfTreat[[k]] <- overallTreat$descriptor
				} 
			} 
			
			if(overallTreat$stoppTheCalculation) {
				print(paste(k, "set to \"none\""))
				listOfTreat[[k]] <- "none"
			}
		}
	}
	
	listOfTreatAndFilterTreat <- changeWhenTreatmentNoneAndSecondTreatmentNotNone(listOfTreat, listOfFilterTreat)
	debug %debug% "End of checkOfTreatments()"
	return(listOfTreatAndFilterTreat)
}

checkIfDescriptorIsNaOrAllZero <- function(overallList, isDescriptor = TRUE){
	overallList$debug %debug% "checkIfDescriptorIsNaOrAllZero()"
	
	tempDescriptor <- overallList$descriptor 
	if(isDescriptor) {
		overallList$descriptor <- overallList$descriptor[colSums(!is.na(overallList$iniDataSet[overallList$descriptor])) != 0 & colSums(overallList$iniDataSet[overallList$descriptor] *1, na.rm = TRUE) > 0]
	} else {
		overallList$descriptor <- overallList$descriptor[colSums(!is.na(overallList$iniDataSet[overallList$descriptor])) != 0]
	}
	overallList$errorDescriptor <- tempDescriptor %GetDescriptorAfterCheckIfDescriptorNotExists% overallList$descriptor

	if (length(overallList$errorDescriptor) > 0) {
		overallList %errorReport% "NotNumericOrAllZero"
	}
	
	if (length(overallList$descriptor) == 0) {
		print("... no descriptor set (all descriptors are zero or NA) - the program stopp!")
		overallList$stoppTheCalculation <- TRUE 
	}
	
	return(overallList)
}

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

preprocessingOfValues <- function(value, quit = FALSE, isColValue=FALSE, replaceString=".") {

	if (!is.null(value)) {
		regExpressionCol <- "[^[:alnum:]|^_]|[[:space:]|\\^]"
		if(isColValue) {
			value <- unlist(strsplit(value, "$", fixed=TRUE))
		}
		value <- gsub(regExpressionCol,replaceString,value)
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
	if (length(overallList$errorDescriptor)>0) {
		overallList %errorReport% "notExists"
	}
	
	if(length(overallList$descriptor)==0) {
		print("... no descriptor set - this pass stopped!")
		overallList$stoppTheCalculation <- TRUE 
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
			overallList$treatment <- "none"
			overallList$filterTreatment <- "none"
			print("... set 'filterTreatment' to 'none'!")		
		}
			
	} else {
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

setRowAndColNameOfFinalDataFrame <- function(overallList) {
	overallList$debug %debug% "setRowAndColNameOfFinalDataFrame()"

	overallList$rowName <- (overallList$descriptor %contactAllWithAll% overallList$filterTreatment) %contactAllWithAll% overallList$filterSecondTreatment
	overallList$colName <- as.character(overallList$filterXaxis)
	
	return(overallList)
}

groupByFunction <- function(groupByList) {
	
	groupByList <- unlist(groupByList)
	return(unlist(groupByList[ifelse(groupByList != "none",TRUE,FALSE)]))
	
}

getBooleanVectorForFilterValues <- function(groupedDataFrame, listOfValues) {
	
	tempVector <- rep.int(TRUE,times=length(groupedDataFrame[,1]))
	for(h in names(listOfValues)) {
		if(h != "none") {
			tempVector <- tempVector & groupedDataFrame[[h]] %in% listOfValues[[h]]
		}
	}
	return(tempVector)
}

buildRowForOverallList <- function(i, des, listOfValues, dataSet, day) {
	rowString <- list(row=des, day=numeric())
	for(k in listOfValues){
		if(k != "none") {
			rowString$row <- paste(rowString$row,dataSet[i,k])
		}
	}
	return(rowString)
} 

fillOverallResult <- function(overallList, preErrorBars) {
	overallList$debug %debug% "fillOverallResult()"
	if(length(overallList$iniDataSet[,1]) > 0){
		for(i in 1:length(overallList$iniDataSet[,1])) {
			for(des in overallList$descriptor) {
				rowAndColumn <- buildRowForOverallList(i,des, c(overallList$treatment, overallList$secondTreatment),overallList$iniDataSet, overallList$xAxis)
				overallList$overallResult[rowAndColumn$row, as.character(overallList$iniDataSet[i,overallList$xAxis])] <- overallList$iniDataSet[i,des]
				if(tolower(overallList$diagramTyp) != "boxplotstacked")
					overallList$errorBars[rowAndColumn$row, as.character(overallList$iniDataSet[i,overallList$xAxis])] <- preErrorBars[i,des]
			}
		}
	} else {
		print("... no Value for the OverallResult-DataFrame - Wrong filter!")
		overallList$stoppTheCalculation <- TRUE
	}
	return(overallList)
}

buildList <- function(overallList, colOfXaxis) {
	overallList$debug %debug% "buildList()"
	newList <- list()
	
	newList[[overallList$treatment]] <- overallList$filterTreatment
	newList[[overallList$secondTreatment]] <- overallList$filterSecondTreatment
	newList[[colOfXaxis]] <- overallList$filterXaxis
	
	return(newList)
}

conactAllWithAll <- function(value1, value2) {
	
	conactRow <- character()
	for(k1 in value1){
		if(k1 != "none") {
			for(k2 in value2){
				if(k2 != "none") {
					conactRow <- c(conactRow, paste(k1,k2,sep = "#"))
				}
			}
		}
	}
	return(conactRow)
}

buildRowName <- function(mergeDataSet,groupBy, yName = NULL) {
	
	if(length(groupBy) == 0) {
		return(data.frame(name=rep.int(yName, length(mergeDataSet[,1])), mergeDataSet))
	} else if (length(groupBy) == 1) {
		return(data.frame(name=mergeDataSet[,groupBy], mergeDataSet[,!(colnames(mergeDataSet) %in% groupBy)]))
	} else {		
		temp <- mergeDataSet[,groupBy[1]]
		for(h in 2:length(groupBy)) {
			temp <- paste(temp, mergeDataSet[,groupBy[h]], sep = "#")
		}
		return(data.frame(name=temp, primaerTreatment= mergeDataSet[,groupBy[1]], mergeDataSet[,mergeDataSet %allColnamesWithoutThisOnes% groupBy]))
	}	
}

getResultDataFrame <- function(overallList) {	
	overallList$debug %debug% "getResultDataFrame()"
	groupBy <- groupByFunction(list(overallList$treatment, overallList$secondTreatment))
	
	if(tolower(overallList$diagramTyp) == "!boxplot") {
		colOfXaxis <- "xAxis"
		colOfMean <- "mean"
		colOfSD <- "se"
	} else {			
		colOfXaxis <- overallList$xAxis
		colOfMean <- overallList$descriptor
		colOfName <- "name"
	}
	
	groupedDataFrame <- data.table(overallList$iniDataSet)
	groupedDataFrameMean <- as.data.frame(groupedDataFrame[,lapply(.SD, mean, na.rm=TRUE), by=c(groupBy,overallList$xAxis)])
	
	if(tolower(overallList$diagramTyp) == "!boxplot") {
		colNamesOfTheRest <- colOfMean
	} else {
		colNamesOfTheRest <- groupedDataFrameMean %allColnamesWithoutThisOnes% c(groupBy, overallList$xAxis)
	}
	
	colnames(groupedDataFrameMean) <- c(groupBy, colOfXaxis, colNamesOfTheRest)
	
	if(tolower(overallList$diagramTyp) == "!boxplot") {
		groupedDataFrameSD <- as.data.frame(groupedDataFrame[,lapply(.SD, sd, na.rm=TRUE), by=c(groupBy,overallList$xAxis)])
		colnames(groupedDataFrameSD) <- c(groupBy, colOfXaxis, colOfSD)
	}
	
	booleanVector <- getBooleanVectorForFilterValues(groupedDataFrameMean, buildList(overallList, colOfXaxis))
	
	if(tolower(overallList$diagramTyp) == "!boxplot") {
		overallList$iniDataSet <- merge(sort=FALSE, groupedDataFrameMean[booleanVector,], groupedDataFrameSD[booleanVector,], by = c(groupBy, colOfXaxis))
		overallList$overallResult <- buildRowName(overallList$iniDataSet,groupBy, overallList$yAxisName)
		
	} else {
		overallList$iniDataSet <- groupedDataFrameMean[booleanVector,]		
	
		buildRowNameDataSet <- buildRowName(overallList$iniDataSet, groupBy, overallList$yAxisName)
		temp <- data.frame()
		
		for(colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(overallList$xAxis, colOfName, "primaerTreatment")) {
			
			colNameWichMustBindReNamed <- reNameHist(colNameWichMustBind)
			
			if(is.null(buildRowNameDataSet$primaerTreatment)){	
				temp <- rbind(temp,data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[,colNameWichMustBind])), values=buildRowNameDataSet[,colNameWichMustBind], xAxis=buildRowNameDataSet[,colOfXaxis], name=buildRowNameDataSet[,colOfName]))			
			} else {
				temp <- rbind(temp,data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[,colNameWichMustBind])), primaerTreatment=buildRowNameDataSet[,"primaerTreatment"], values=buildRowNameDataSet[,colNameWichMustBind], xAxis=buildRowNameDataSet[,colOfXaxis], name=buildRowNameDataSet[,colOfName]))			
			}
		}
		overallList$overallResult <- temp		
	}	

	return(overallList)
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
	numberOfColors <- ifelse(tolower(overallList$diagramTyp) == "boxplotstacked", length(overallList$descriptor), length(unique(overallList$overallResult$name)))
	
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
	
	saveNameImageFile <- preprocessingOfValues(saveNameImageFile, FALSE, FALSE, "_")
	saveNameLatexFile <- preprocessingOfValues(saveNameLatexFile, FALSE, FALSE, "_")
	o <- gsub('[[:punct:]]',"_",o)
	
	latexText <- paste("\\loadImage{",
					   ifelse(saveNameImageFile=="",saveNameLatexFile,saveNameImageFile),
					   ifelse(o=="", "", paste("_",o ,sep="")),
					   ".pdf}", sep="")
	
	write(x=latexText, append=TRUE, file=paste(saveNameLatexFile,"tex",sep="."))
	
}

saveImageFile <- function(overallList, plot, extraString="") {
	filename <- preprocessingOfValues(paste(overallList$saveName,extraString,sep=""), FALSE, FALSE,replaceString = "_")	
	ggsave (filename=paste(filename,overallList$saveFormat,sep="."), plot = plot, dpi=as.numeric(overallList$dpi))

}

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

CheckIfOneColumnHasOnlyValues <- function(overallList) {	
	
	max <- -1	
	for(index in 1:length(unlist(levels(overallList$overallResult$name)))){
		
		if(tolower(overallList$diagramTyp) == "!boxplot") {
			temp <- sum(!is.na(overallList$overallResult$mean[overallList$overallResult$name == unlist(levels(overallList$overallResult$name))[[index]]]))
		} else {
			boolVec <- overallList$overallResult$name == unlist(levels(overallList$overallResult$name))[[index]]
			temp <- sum(!is.na(overallList$overallResult[boolVec,overallList$descriptor]))
		}
		max <- ifelse(temp > max, temp, max)
	}
	
	return(ifelse(max == 1,TRUE, FALSE))
}


buildMyStats <- function(values, means, se) {
	means <- as.data.frame(as.vector(means))
	colnames(means) <- "means"
	
	se <- as.data.frame(as.vector(se))
	colnames(se) <- "se"

	return(data.frame(value=values,means=means, se=se))
}

buildMyStats2 <- function(values, means, se, rowName) {
	means <- as.data.frame(as.vector(means))
	colnames(means) <- "means"
	
	rowName <- as.data.frame(as.vector(rowName))
	colnames(rowName) <- Name
	
	se <- as.data.frame(as.vector(se))
	colnames(se) <- "se"
	
	return(data.frame(value=values,means=means, se=se, rowName=rowName))
}

makeLinearDiagram <- function(h, overallList) {
	overallList$debug %debug% "makeLinearDiagram()"
		
	if(!CheckIfOneColumnHasOnlyValues(overallList)) {	
		if(tolower(overallList$diagramTyp) == "!boxplot") {
	
#			myDataSet <- data.frame(name=c("normal","wet","dry","normal","wet","dry","normal","wet","dry"), 
#									xAxis=c(6,6,6,8,8,8,10,10,10),		
#									mean=c(4883,6224,4630,6047,5790,7758,7349,7778,9725), 
#									se=c(1515,1190,1670,1831,2013,1318,2387,2182,1499))
			
			plot <-	ggplot(data=overallList$overallResult, aes(x=xAxis, y=mean, shape=name)) +
					#geom_smooth(aes(ymin=mean-se, ymax=mean+se, colour=name, fill=name), stat="identity", alpha=0.1) +
					geom_ribbon(aes(ymin=mean-se, ymax=mean+se, fill=name), stat="identity", alpha=0.1) +
					geom_line(aes(color=name), alpha=0.2) +
					geom_point(aes(color=name), size=3) +
					scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(overallList$overallResult$xAxis):max(overallList$overallResult$xAxis)) +
					ylab(overallList$yAxisName) +
					scale_fill_manual(values = overallList$color) +
					scale_colour_manual(values=overallList$color) +
					scale_shape_manual(values = c(1:length(overallList$color))) +
					theme_bw() +
					opts(axis.title.x = theme_text(face="bold", size=11),
							axis.title.y = theme_text(face="bold", size=11, angle=90),
							#panel.grid.major = theme_blank(), # switch off major gridlines
							#panel.grid.minor = theme_blank(), # switch off minor gridlines
							legend.position = "right", # manually position the legend (numbers being from 0,0 at bottom left of whole plot to 1,1 at top right)
							legend.title = theme_blank(), # switch off the legend title						
							#legend.key.size = unit(1.5, "lines"),
							legend.key = theme_blank(), # switch off the rectangle around symbols in the legend
							panel.border = theme_rect(colour="Grey", size=0.1)
					)
			
			if(length(overallList$color) > 25) {
				plot <- plot + opts(legend.text = theme_text(size=6),
									legend.key.size = unit(0.7, "lines")
									)
			} else {
				plot <- plot + opts(legend.text = theme_text(size=11))
			}
				
		} else {
			
			plot <-	ggplot(data=overallList$overallResult, aes_string(x=overallList$xAxis, y=overallList$descriptor, shape="name")) +
				geom_point(aes(colour=name)) + 
				opts(legend.position="none", plot.margin = unit(c(0.1, 0.1, 0, 0), "cm")) + 
				scale_x_continuous(name=overallList$xAxisName, expand=c(0,1), minor_breaks = min(overallList$overallResult[overallList$xAxis]):max(overallList$overallResult[overallList$xAxis])) +
				ylab(overallList$yAxisName) +
				ylim(c(min(overallList$overallResult[overallList$descriptor] - overallList$overallResult[(overallList$descriptor %contactAllWithAll% "SD")] - 10,na.rm=TRUE),max(overallList$overallResult[overallList$descriptor] + overallList$overallResult[(overallList$descriptor %contactAllWithAll% "SD")] + 10,na.rm=TRUE))) +
				#geom_smooth(aes(ymin=overallList$overallResult[overallList$descriptor]-overallList$overallResult[(overallList$descriptor %contactAllWithAll% "SD")], ymax=overallList$overallResult[overallList$descriptor]+overallList$overallResult[(overallList$descriptor %contactAllWithAll% "SD")], colour=name, fill=name), stat="identity", alpha=0.1) +
				#geom_smooth(data=overallList$overallResult, aes(colour=name, fill=name), stat="identity", alpha=0.1) +
				#geom_smooth(aes_string(ymin=yMin, ymax=yMax), alpha=0.1) +
				#geom_smooth(stat="identity", alpha=0.1) +
				geom_errorbar(aes(ymax=overallList$overallResult[overallList$descriptor]+overallList$overallResult[(overallList$descriptor %contactAllWithAll% "SD")], ymin=overallList$overallResult[overallList$descriptor]-overallList$overallResult[(overallList$descriptor %contactAllWithAll% "SD")]), width=0.2)
				scale_colour_manual(values=overallList$color) + 
				scale_fill_manual(values = overallList$color)	
		}

		

##!# nicht löschen, ist die interpolation (alles in dieser if Abfrage mit #!# makiert)
##!#				newCoords <- seq(min(overallList$filterXaxis,na.rm=TRUE),max(overallList$filterXaxis,na.rm=TRUE),1)
##!#				newValue <- approx(overallList$filterXaxis, overallList$overallResult[y,],xout=newCoords,method="linear")
##!#				
##!#				naVector <- is.na(overallList$overallResult[y,])
##!#				overallResultWithNaValues <- overallList$overallResult[y,]
##!#				overallList$overallResult[y,naVector] <- newValue$y[overallList$filterXaxis[naVector]]
#				
#				if (firstPlot) {
#					firstPlot <- FALSE
##!#				plot(overallList$filterXaxis, overallList$overallResult[y,], main="", type="c", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult,na.rm=TRUE),max(overallList$overallResult,na.rm=TRUE)))
#					plot(overallList$filterXaxis, overallList$overallResult[y,], main="", type="b", xlab=overallList$xAxisName, col=overallList$color[y], ylab=overallList$yAxisName, pch=y, lty=1, lwd=3, ylim=c(min(overallList$overallResult,na.rm=TRUE),max(overallList$overallResult,na.rm=TRUE)))
#				} else {
##!#				points(overallList$filterXaxis, overallList$overallResult[y,], type="c", col=overallList$color[y], pch=y, lty=1, lwd=3 )	
#					points(overallList$filterXaxis, overallList$overallResult[y,], type="b", col=overallList$color[y], pch=y, lty=1, lwd=3 )
#				}
##!#				points(overallList$filterXaxis, overallResultWithNaValues, type="p", col=overallList$color[y], pch=y, lty=1, lwd=3 )
#			} 

		if(h==1) {
			saveImageFile(overallList, plot)
		}
		if(overallList$appendix) {
			writeLatexFile("appendixImage", overallList$saveName)
		}
	
	} else {
		print("... only one column has values, so it will be plot as barplot!")

		day <- overallList$overallResult$xAxis[!is.na(overallList$overallResult$mean)][1]
		overallList$xAxisName <- paste(overallList$xAxisName,day)
		overallList$overallResult <- overallList$overallResult[!is.na(overallList$overallResult$mean),]
		overallList <- makeBoxplotDiagram(h, overallList, TRUE)
	}
	return(overallList)
}

plotStackedImage <- function(h, overallList, plotThisValues, title = "", makeOverallImage = FALSE, legende=TRUE, minor_breaks=TRUE) {
	overallList$debug %debug% "plotStackedImage()"	
	if(length(plotThisValues[,1]) > 0) {

		plot <- ggplot(plotThisValues, aes(xAxis, values, fill=hist)) + 
				geom_bar(stat="identity", position = "fill") +
			 	ylab(overallList$yAxisName) 
				#coord_cartesian(ylim=c(0,1)) +
			
		if(minor_breaks) {
			plot <- plot + scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(overallList$overallResult$xAxis):max(overallList$overallResult$xAxis))
		} else {
			plot <- plot + xlab(overallList$xAxisName)
		}
				
		plot <- plot +		
				scale_fill_manual(values = rev(overallList$color), name="") +
				theme_bw() +
				opts(axis.title.x = theme_text(face="bold", size=11),
						axis.title.y = theme_text(face="bold", size=11, angle=90),
						plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"),
						#panel.background = theme_rect(linetype = "dotted"),
						panel.border = theme_rect(colour="Grey", size=0.1),
						strip.background = theme_rect(colour=NA)
#						plot.title = theme_text(size=10),
#						plot.title = theme_rect(colour="Pink", size=0.1),
				) 
		
		if(!legende) {
			plot <- plot + opts(legend.position="none")
		} else {
			plot <- plot + opts(legend.position="right", 
								legend.title = theme_blank(),
								legend.text = theme_text(size=11),
								legend.key = theme_blank())
		}
		
		if(title != "") {
			plot <- plot + opts(title = title)
		}
		
		if(!minor_breaks) {
			plot <- plot + opts(panel.grid.minor = theme_blank())
		}
		
		if(makeOverallImage) {
			#plot <- plot + facet_wrap(~ name, drop=TRUE)
			plot <- plot + facet_wrap(~ name)
		}
		
		if (h==1) {
			saveImageFile(overallList, plot, paste("overall", title, sep=""))
			if(makeOverallImage) {
				writeLatexFile(paste(overallList$saveName, "stackedOverallImage", sep=""), paste(overallList$saveName,"overall",title, sep=""))	
			} else {
				writeLatexFile(overallList$saveName, paste(overallList$saveName,"overall",title, sep=""))	
			}
		} else {
			print(plot)
		}
	}
}

PreWorkForMakeBigOverallImage <- function(h, overallList) {
	overallList$debug %debug% "PreWorkForMakeBigOverallImage()"	
	
	groupBy <- groupByFunction(list(overallList$treatment, overallList$secondTreatment))
	if(length(groupBy) == 0 || length(groupBy) == 1) {
		plotStackedImage(h = h, overallList = overallList, plotThisValues = overallList$overallResult, makeOverallImage = TRUE, legende=TRUE, minor_breaks=FALSE)
		
	} else {
		for(value in overallList$filterSecondTreatment) {
			title <- value
			plottedName <- overallList$filterTreatment %contactAllWithAll% value
			booleanVector <- getBooleanVectorForFilterValues(overallList$overallResult, list(name = plottedName))
			plotThisValues <- overallList$overallResult[booleanVector,]
			plotThisValues <- reNameColumn(plotThisValues, "name", "primaerTreatment")
			plotStackedImage(h, overallList, plotThisValues, title = title, makeOverallImage = TRUE,  legende=TRUE, minor_breaks=FALSE)
		}	 
	}
}

PreWorkForMakeNormalImages <- function(h, overallList) {
	overallList$debug %debug% "PreWorkForMakeNormalImages()"
	stackedImages <- unlist(unique(overallList$overallResult["name"]))
	
	for(o in stackedImages) {
		overallList$debug %debug% paste("makeBoxplotStackedDiagram with the descriptor: ",overallList$saveName,o)
		plotThisValues <- overallList$overallResult[overallList$overallResult["name"] == o,]
		plotStackedImage(h, overallList, plotThisValues, o, FALSE, TRUE, TRUE)
	}
}


makeBoxplotStackedDiagram <- function(h, overallList) {
	overallList$debug %debug% "makeBoxplotStackedDiagram()"
	overallList$overallResult[is.na(overallList$overallResult)] <- 0

	PreWorkForMakeBigOverallImage(h, overallList)
	#PreWorkForMakeNormalImages(h,overallList)

	return(overallList)
}	


makeBoxplotDiagram <- function(h, overallList, isOnlyOneValue = FALSE) {
	overallList$debug %debug% "makeBoxplotDiagram()"
	
	if(isOnlyOneValue) {
		myPlot <- ggplot(data=overallList$overallResult, aes(x=name, y=mean))
	} else {
		myPlot <- ggplot(data=overallList$overallResult, aes(x=xAxis, y=mean))
	}
	
	myPlot <- myPlot + 				
#	myPlot <-ggplot(data=myDataSet, aes(x=name, y=mean)) +		
	geom_bar(stat="identity", aes(fill=name), colour="Grey", size=0.1) +
	geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.2, colour="black")+
	#geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.5, colour="Pink")+
	ylab(overallList$yAxisName) +
	coord_cartesian(ylim=c(0,max(overallList$overallResult$mean + overallList$overallResult$se + 10,na.rm=TRUE))) +
	xlab(overallList$xAxisName) +
	scale_fill_manual(values = overallList$color) +
	theme_bw() +
	opts(legend.position="none",
			plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"),
			axis.title.x = theme_text(face="bold", size=11),
			axis.title.y = theme_text(face="bold", size=11, angle=90),
			panel.grid.minor = theme_blank(),
			panel.border = theme_rect(colour="Grey", size=0.1)
	)

	if(length(overallList$color) > 10) {
		myPlot <- myPlot + opts(axis.text.x = theme_text(size=6, angle=90))
	} 
	
	if (h==1) {
		saveImageFile(overallList, myPlot)
	} else {
		print(myPlot)
	}
	if(overallList$appendix) {
		writeLatexFile("appendixImage", overallList$saveName)
	}
	
	return(overallList)
}

makeDiagrams <- function(overallList) {
	overallList$debug %debug% "makeDiagrams()"
	durchlauf <- ifelse(overallList$showResultInR, 2, 1)
	
	for(h in 1:durchlauf) {
				
		if (tolower(overallList$diagramTyp) == "boxplot") {
			overallList <- makeBoxplotDiagram(h, overallList)
			
		} else if (tolower(overallList$diagramTyp) == "boxplotstacked") {
			overallList <- makeBoxplotStackedDiagram(h, overallList)
			
		} else if(tolower(overallList$diagramTyp) == "!boxplot"){
			overallList <- makeLinearDiagram(h, overallList)
			
		} else {
			print("Error - overallList$diagramTyp is undefined!")
		}
	}
}

checkIfAllNecessaryFilesAreThere <- function() {
		print("... check if the noValues-Image is there")
		file <- "noValues.pdf"
		if(!file.exists(file)){
			print(paste("... create defaultImage '",file,"'",sep=""))
			Cairo(width=900, height=70,file=file,type="pdf",bg="transparent",units="px",dpi=90)
			par(mar = c(0,0,0,0))
			plot.new()
			legend("left", "no values", col= c("black"), pch=1, bty="n")
			dev.off()
		}	
}

buildBlacklist <- function(workingDataSet, descriptorSet) {
	
	searchString <- ".histogram."
	searchString <- paste(searchString,"mark",sep = "|")	
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
	
	saveFormat <- "pdf"	
	dpi <- "300" ##90
	
	isGray <- FALSE
	showResultInR <- FALSE
	
	treatment <- "Treatment"
	filterTreatment <- "none"
	
	secondTreatment <- "none"
	filterSecondTreatment <- "none"
	
	xAxis <- "Day (Int)" 
	xAxisName <- "DAS"
	filterXaxis <- "none"
	
	diagramTypVector <- vector()
	descriptorSet <- vector()
	descriptorSetName <- vector()
	
	fileName <- "error"

	appendix <- FALSE
	#appendix <- TRUE
	
	separation <- ";"
#	if (length(args) < 1) {
#		englischVersion <- FALSE
#		#englischVersion <- TRUE
#	} else {
#		englischVersion <- TRUE
#	}

	if (typOfStartOptions == "all" | typOfStartOptions == "report" | typOfStartOptions == "allmanual") {

		if (typOfStartOptions != "allmanual") {
			fileName <- fileName %exists% args[1]
		} else {
			fileName <- "numeric_data.MaizeAnalysisAction_ 1116BA_new3.csv"
			#fileName <- "report.csv" ## englischVersion <- TRUE setzen!!
			#fileName <- "testDataset3.csv"
		}
		
		if (fileName != "error") {
			workingDataSet <- separation %getData% fileName
			
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
						"side.area.relative", "side.height.norm.relative", "side.width.norm.relative", "top.area.relative", "side.area.relative", "volume.iap.relative",
						"side.height (mm)","side.width (mm)","side.area (px)", "top.area (px)")
			
				descriptorSetName <- c("weight before watering (g)","weight after watering (g)", "water weight (g)", "normalized height (mm)", "normalized width (mm)", "normalized side area (mm^2)", "normalized top area (mm^2)",
						"chlorophyl intensity (relative intensity/pixel)", "fluorescence intensity (relative intensity/pixel)", "nir intensity (relative intensity/pixel)",
						"number of leafs (leaf)", "number of tassels (tassel)", "length of leafs plus stem (mm)", "volume based on FLUO (IAP) (px^3)", "volume based on RGB (IAP) (px^3)", "volume based on max RGB-image (IAP) (px^3)", "volume based on RGB (LemnaTec) (px^3)",
						"volume based water use efficiency", "weighted loss through drought stress (side)", "weighted loss through drought stress (top)", "Average wetness of side image", "Average wetness of top image",
						"relative projected side area (%)", "relative plant height (%)", "relative plant width (%)", "relative projected top area (%)", "relative projected side area (%)", "relative volume (IAP based formular - RGB) (%)",
						"height (px)", "width (px)", "side area (px)", "top area (px)")		
		}
			diagramTypVector <- rep.int("!boxplot", times=length(descriptorSetName))

			#boxplotStacked
			descriptorSet <- c(descriptorSet, "side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255",
							   				  "side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
			descriptorSetName <- c(descriptorSetName, "NIR absorption class (%)", "red fluorescence histogram (%)")
			diagramTypVector <- c(diagramTypVector, "boxplotStacked", "boxplotStacked")
			
			appendix <- appendix %exists% args[3]
			
			if(appendix) {
				blacklist <- buildBlacklist(workingDataSet, descriptorSet)
				descriptorSetAppendix <- colnames(workingDataSet[!as.data.frame(sapply(colnames(workingDataSet),'%in%', blacklist))[,1]])
				descriptorSetNameAppendix <- descriptorSetAppendix
				diagramTypVectorAppendix <- rep.int("!boxplot", times=length(descriptorSetNameAppendix))
			}
		
			saveFormat <- saveFormat %exists% args[2]
						
			listOfTreatAndFilterTreat <- checkOfTreatments(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, DEBUG)
			treatment <- listOfTreatAndFilterTreat[[1]][[1]]
			secondTreatment <- listOfTreatAndFilterTreat[[1]][[2]]
			filterTreatment <- listOfTreatAndFilterTreat[[2]][[1]]
			filterSecondTreatment <- listOfTreatAndFilterTreat[[2]][[2]]
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
	
		descriptorSet <- c("side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
		#descriptorSet <- c("side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
		descriptorSetName <- c("red fluorescence histogram (%)")
	
	
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
		
		#treatment <- "none"
		treatment <- "Treatment"
		#treatment <- "Condition"
		#filterTreatment <- "dry$normal$wet"
		#filterTreatment <- "dry$normal"
		#filterTreatment <- "normal bewaessert$Trockentress"
		#filterTreatment <- "ganz"
		filterTreatment <- "none"
		##filterTreatment <- "Deutschland$Spanien$Italien$China"
		
		#secondTreatment <- "Treatment"
		secondTreatment <- "none"
		#secondTreatment <- "Species"
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
		
		diagramTyp="boxplotStacked"
		#diagramTyp="!boxplot"
		#diagramTyp="boxplot"
		
		bgColor <- "transparent"
		isGray="FALSE"
		#transparent <- "TRUE"
		#legendUnderImage <- "TRUE"
		#showResultInR <- TRUE
		showResultInR <- FALSE
		
		#fileName <- "numeric_data.MaizeAnalysisAction_ 1116BA_new3.csv"
		#fileName <- "testDataset2.csv"
		fileName <- "report.csv"
		#fileName <- "testDataset3.csv"
		#englischVersion <- FALSE
		#englischVersion <- TRUE
		separation <- ";"
		workingDataSet <- separation %getData% fileName
		
		#descriptor <- c("Hallo2")
		#descriptor <- c("Plant ID","Treatment","Hallo","Wert1", "Repl ID")
		#descriptor <- c("Repl ID")		
		#descriptorSet <- c("nir.top")
		#descriptorSet <- c("Plant ID")
#		descriptorSet <- c("side.height.norm (mm)")

		#descriptorSet <- c("side.area.norm (mm^2)")
#		descriptorSetName <- c("Das ist ein Testname")

		#descriptorSet <- c("Plant ID$Treatment$Hallo$Wert1$Repl ID")
		
		diagramTypVector <- rep.int(diagramTyp, times=length(descriptorSetName))
		
		
		saveName <- "test2"
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
	
	secondRun <- appendix
	appendix <-  FALSE
	
	if(fileName != "error" & length(descriptorSet) > 0){
		time <- system.time({
			repeat {
				for (y in 1:length(descriptorSet)) {	
					print(paste("... generate diagram of '", descriptorSet[y],"'",sep=""))
					valuesAsDiagram(iniDataSet = workingDataSet, saveName = descriptorSet[y], saveFormat = saveFormat, 
									dpi = dpi, diagramTyp = diagramTypVector[y], isGray = isGray, treatment = treatment,
									filterTreatment = filterTreatment, secondTreatment = secondTreatment, filterSecondTreatment = filterSecondTreatment,
									filterXaxis = filterXaxis, xAxis = xAxis, descriptor = descriptorSet[y], showResultInR = showResultInR, 
									xAxisName = xAxisName, yAxisName = descriptorSetName[y], debug = DEBUG, appendix=appendix)
					print("... ready")
					
				}
				
				if(secondRun) {
					appendix = TRUE
					secondRun = FALSE
					print("... start with the Appendix")
					descriptorSet <- descriptorSetAppendix
					descriptorSetName <- descriptorSetNameAppendix
					diagramTypVector <- diagramTypVectorAppendix
				} else {
					break
				}
			}
			checkIfAllNecessaryFilesAreThere()
		},TRUE)
	print("... All ready")		
	print(time)	
	
	} else {
		print("No filename or no descriptor!")
	}
}

valuesAsDiagram <- function(iniDataSet, saveName="OutputDiagramm", saveFormat="pdf", dpi="90", diagramTyp="!boxplot", isGray="false", treatment="Treatment",
		filterTreatment="none", secondTreatment="none", filterSecondTreatment="none", 
		filterXaxis="none", xAxis="Day (Int)", descriptor="side.area", showResultInR=FALSE, xAxisName="none", yAxisName="none",
		debug = FALSE, appendix=FALSE, stoppTheCalculation=FALSE) {			

#	overallList <- list(iniDataSet=iniDataSet, saveName=saveName, saveFormat=saveFormat, imageWidth=imageWidth, imageHeight=imageHeight, dpi=dpi,
#						diagramTyp=diagramTyp, isGray=isGray, treatment=treatment, filterTreatment=filterTreatment,
#						secondTreatment=secondTreatment, filterSecondTreatment=filterSecondTreatment, filterXaxis=filterXaxis, xAxis=xAxis, descriptor=descriptor,
#						showResultInR=showResultInR, xAxisName=xAxisName, yAxisName=yAxisName, debug=debug, 
#						bgColor=bgColor, errorDescriptor=character(), rowName=character(), colName=character(),
#						overallResult=data.frame(), errorBars=data.frame(), color=numeric(), symbolParameter=15, appendix=appendix, stoppTheCalculation=stoppTheCalculation)
				
	overallList <- list(iniDataSet=iniDataSet, saveName=saveName, saveFormat=saveFormat, dpi=dpi,
						diagramTyp=diagramTyp, isGray=isGray, treatment=treatment, filterTreatment=filterTreatment,
						secondTreatment=secondTreatment, filterSecondTreatment=filterSecondTreatment, filterXaxis=filterXaxis, xAxis=xAxis, descriptor=descriptor,
						showResultInR=showResultInR, xAxisName=xAxisName, yAxisName=yAxisName, debug=debug, 
						errorDescriptor=character(), overallResult=data.frame(), color=numeric(), appendix=appendix, stoppTheCalculation=stoppTheCalculation)
		
	#install.packages(c("Cairo"), repos="http://cran.r-project.org", dependencies = TRUE)
	library("Cairo")
	#install.packages(c("RColorBrewer"), repos="http://cran.r-project.org", dependencies = TRUE)
	library("RColorBrewer")
	#install.packages(c("data.table"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(data.table)
	#install.packages(c("ggplot2"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(ggplot2)
	
	overallList$debug %debug% "Start"
	
	overallList$saveName <- changeSaveName(overallList$saveName)
	overallList <- preprocessingOfDescriptor(overallList)
	
	if(!overallList$stoppTheCalculation) {
		overallList <- preprocessingOfxAxisValue(overallList)
		overallList <- preprocessingOfTreatment(overallList)
		overallList <- preprocessingOfSecondTreatment(overallList)
		overallList <- checkIfDescriptorIsNaOrAllZero(overallList)
		if(!overallList$stoppTheCalculation) {
			overallList <- reduceWorkingDataSize(overallList)
			overallList <- setDefaultAxisNames(overallList)	
			overallList <- getResultDataFrame(overallList)
			if(!overallList$stoppTheCalculation) {
				overallList$color <- setColor(overallList) 
				makeDiagrams(overallList)
			}
		}
	}
}

######### START #########
#rm(list=ls(all=TRUE))
#startOptions("test", TRUE)
#startOptions("allmanual", TRUE)
startOptions("report", FALSE)
rm(list=ls(all=TRUE))