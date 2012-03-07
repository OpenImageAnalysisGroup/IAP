# Author: Entzian, Klukas
###############################################################################

getSpecialRequestDependentOfUserAndTypOfExperiment <- function() {
	requestList = list(
			KN = list(barley = list(boxplot = list(daysOfBoxplotNeedsReplace = c("27", "44", "45")))
			)
	)				
	return(requestList)
}

"%break%" <- function(typOfBreak, breakValue) {
	if(typOfBreak == 0) {
		stop(call. = FALSE)
	} else {
		print(paste("Script will stopped for ",breakValue, " sec!", sep=""))
		Sys.sleep(breakValue)
		print("Break ends!")
	}
}

"%debug%" <- function(debug, debugNumber) {
	if (debug) {
		print(paste("DebugBreakPoint: ", debugNumber))
	}
}

"%checkEqual%" <- function(treat, seconTreat) {
	if (treat == seconTreat) {
		print("Second filter has the same value as first filter so it set to \"none\"")
		return("none")
	} else {
		return(seconTreat)
	}
}

"%errorReport%" <- function(errorDescriptor, typOfError="notExists") {
	#overallList$debug %debug% "%errorReport%"
	if (length(errorDescriptor) > 0) {
		if (tolower(typOfError) == "notexists") {
			print(paste("the descriptor '", errorDescriptor, "' won´t plot because he is missing!", sep=""))
		} else if (tolower(typOfError) == "notnumericorallzero") {
			print(paste("the values of the descriptor(s) '", errorDescriptor, "', are all zero or not numeric!", sep=""))
		}
	}
}

"%exists%" <- function(standardValue, argsValue) {
	if (!is.na(argsValue) & argsValue != "") {
		return(argsValue)
	} else {
		return(standardValue)
	}
}

"%readInputDataFile%" <- function(separation, fileName) {
	loadAndInstallPackages(TRUE, FALSE)
	
	#separation = ";"
	print(paste("Read input file", fileName))
	if (file.exists(fileName)) {
		
		preScanForPointOrComma <- scan(file=fileName, what=character(0), nlines=2, sep="\n")
		preScanForPointOrComma <- paste(preScanForPointOrComma[2],",.", sep="")
		allCharacterSeparated <- table(strsplit(toupper(preScanForPointOrComma), '')[[1]])
		
		if(allCharacterSeparated["."] > allCharacterSeparated[","]) {
	#		print("... English version")
			return(read.csv(fileName, header=TRUE, sep=separation, fileEncoding="UTF-8", encoding="UTF-8"))
		} else {
	#		print("... German version")
			return(read.csv2(fileName, header=TRUE, sep=separation, fileEncoding="UTF-8", encoding="UTF-8"))
		}
	} else {
		return(NULL)
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
	vectorTemp = character(0)
	for (k in vector2) {
		if (k == "none") {
			vectorTemp = c(vectorTemp, vector1)
		} else {
			vectorTemp = c(vectorTemp, paste(vector1, k, sep = "/")) #    #/#
		}
	}
	return(vectorTemp)
}

"%allColnamesWithoutThisOnes%" <- function(dataSet, withoutColNamesVector) {
	return(colnames(dataSet)[!colnames(dataSet) %in% withoutColNamesVector])
}

InstalledPackage  <- function(package) {
    available  <- suppressMessages(suppressWarnings(sapply(package, require, quietly = TRUE, character.only = TRUE, warn.conflicts = FALSE)))
    missing  <- package[!available]
    if (length(missing) > 0) return(FALSE)
    return(TRUE)
}

CRANChoosen  <- function() {
    return(getOption("repos")["CRAN"] != "@CRAN@")
}

UsePackage  <- function(package, defaultCRANmirror = "http://cran.at.r-project.org") {
    if(!InstalledPackage(package)) {
        options(repos = c(CRAN = defaultCRANmirror))
        suppressMessages(suppressWarnings(install.packages(package)))
        if(!InstalledPackage(package)) return(FALSE)
    }
    return(TRUE)
}

loadAndInstallPackages <- function(install=FALSE, useDev=FALSE) {	
	if (install) {
		libraries  <- c("Cairo", "RColorBrewer", "data.table", "ggplot2", "psych")
		for(library in libraries) { 
    		if(!UsePackage(library)) {
        		stop("Error!", library)
    		}
		}
		if (useDev) {
			install.packages(c("devtools"), repos="http://cran.r-project.org", dependencies = TRUE)
			dev_mode()
			install_github("ggplot2")
		}
	}
		
	library("Cairo")
	library("RColorBrewer")
	library("data.table")
	library("ggplot2")
	library("psych")
	
	if (useDev) {
		library("devtools")
	}	
}

buildDataSet <- function(workingDataSet, overallResult, colname, index) {
	if (length(colname) > 0) {
		for (n in 1:length(colname)) {
			workingDataSet = cbind(workingDataSet, overallResult[paste(colname[n], index, sep="")])
		}	
		return(workingDataSet)
	}
}

reNameHist =  function(colNameWichMustBind) {
	colNameWichMustBind = as.character(colNameWichMustBind)
	positions = which(strsplit(colNameWichMustBind, '')[[1]] == '.')
	colNameWichMustBind = substr(colNameWichMustBind, positions[length(positions)]+1, nchar(colNameWichMustBind))
	
	regExpressionSpezialCharacter = "\\_"
	colNameWichMustBind = gsub(regExpressionSpezialCharacter, "..", colNameWichMustBind)
	
	return(colNameWichMustBind)	
}

reNameColumn =  function(plotThisValues, columnNameReplace="name", columnNameWhichUsedToReplace="primaerTreatment") {
	if (!is.null(plotThisValues[columnNameWhichUsedToReplace])) {
		plotThisValues[columnNameReplace] = plotThisValues[columnNameWhichUsedToReplace]
	}
	return(plotThisValues)
}

swap <- function(listWithTwoParameter) {
	temp = listWithTwoParameter[[1]]
	listWithTwoParameter[[1]] = listWithTwoParameter[[2]]
	listWithTwoParameter[[2]] = temp
	return(listWithTwoParameter)
}

changeWhenTreatmentNoneAndSecondTreatmentNotNone <- function(listOfTreat, listOfFilterTreat) {
	if (listOfTreat[[1]] == "none" & listOfTreat[[2]] != "none") {
		print("The values of Treatment and SecondTreamt are changed (filter values also)")
		return(list(swap(listOfTreat), swap(listOfFilterTreat)))	
	} else {
		return(list(listOfTreat, listOfFilterTreat))
	}
}

checkOfTreatments <- function(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug) {
	debug %debug% "Start of checkOfTreatments()"
	
	treatment = treatment %exists% args[4]
	secondTreatment = secondTreatment %exists% args[5]
	secondTreatment = treatment %checkEqual% secondTreatment
	
	listOfTreat = list(treatment=treatment, secondTreatment=secondTreatment)
	listOfFilterTreat = list(filterTreatment=filterTreatment, filterSecondTreatment=filterSecondTreatment)	## wird erstmal noch nichts weiter mit gemacht! nur geswapt falls notwendig

	if(treatment == "none" & secondTreatment == "none") {
		listOfTreat$treatment = "noneTreatment"
		listOfTreatAndFilterTreat <- list(listOfTreat, listOfFilterTreat)
	} else {	
		for (k in names(listOfTreat)) {
			if (listOfTreat[[k]] != "none") {
				#overallTreat = list(iniDataSet=workingDataSet, descriptor=listOfTreat[[k]], debug=debug, stoppTheCalculation = FALSE, errorDescriptor=character())
				#overallTreat = list(iniDataSet=workingDataSet, descriptor=listOfTreat[[k]], debug=debug, stoppTheCalculation = FALSE)
				descriptorVector = getVector(preprocessingOfDescriptor(listOfTreat[[k]], workingDataSet))
				
				if (!is.null(descriptorVector)) {
					descriptorVector = getVector(checkIfDescriptorIsNaOrAllZero(descriptorVector, workingDataSet, FALSE))
					
					if (!is.null(descriptorVector)) {
						listOfTreat[[k]] = descriptorVector
					} 
				} 
				
				if (is.null(descriptorVector)) {
					print(paste(k, "set to \"none\""))
					listOfTreat[[k]] = "none"
				}
			}
		}
	
		listOfTreatAndFilterTreat = changeWhenTreatmentNoneAndSecondTreatmentNotNone(listOfTreat, listOfFilterTreat)
	}
	debug %debug% "End of checkOfTreatments()"

	return(listOfTreatAndFilterTreat)
}

overallCheckIfDescriptorIsNaOrAllZero <- function(overallList) {
	overallList$debug %debug% "overallCheckIfDescriptorIsNaOrAllZero()"	
	
	if (sum(!is.na(overallList$nBoxDes)) > 0) {
		if (overallList$debug) {print(paste(length(overallList$nBoxDes), "nBoxplots..."))}
		for (n in 1:length(overallList$nBoxDes)) {
			if (!is.na(overallList$nBoxDes[[n]][1])) {
				overallList$nBoxDes[n] = checkIfDescriptorIsNaOrAllZero(overallList$nBoxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$nBoxDes) = c(1:length(overallList$nBoxDes))
	} else {
		print("All values for nBoxplot are 'NA'")
	}
	
	if (sum(!is.na(overallList$boxDes)) > 0) {
		if (overallList$debug) {print(paste(length(overallList$boxDes), "Boxplots..."))}
		for (n in 1:length(overallList$boxDes)) {
			if (!is.na(overallList$boxDes[[n]][1])) {
				overallList$boxDes[[n]] = checkIfDescriptorIsNaOrAllZero(overallList$boxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$boxDes) = c(1:length(overallList$boxDes))
	} else {
		print("All values for Boxplot are 'NA'")
	}

	if (sum(!is.na(overallList$boxStackDes)) > 0) {
		if (overallList$debug) {print(paste(length(overallList$boxStackDes), "stacked boxplots..."))}
		for (n in 1:length(overallList$boxStackDes)) {
			if (!is.na(overallList$boxStackDes[[n]][1])) {
				overallList$boxStackDes[[n]] = checkIfDescriptorIsNaOrAllZero(overallList$boxStackDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$boxStackDes) = c(1:length(overallList$boxStackDes))
	} else {
		print("All values in stackedBoxplot are 'NA'")
	}
	
	if (!sum(!is.na(overallList$boxStackDes)) > 0 && !sum(!is.na(overallList$boxDes)) > 0 && !sum(!is.na(overallList$nBoxDes)) > 0) {
		print("No descriptor set (all descriptors are zero or NA) - the program needs to stop!")
		overallList$stoppTheCalculation = TRUE 
	}
	
	return(overallList)
}

checkIfDescriptorIsNaOrAllZero <- function(descriptorVector, iniDataSet, isDescriptor = TRUE) {
	#overallList$debug %debug% "checkIfDescriptorIsNaOrAllZero()"
	descriptorVector = as.vector(descriptorVector)
	#descriptorVector = getVector(descriptorVector)
	tempDescriptor = descriptorVector 
	
	if (isDescriptor) {
		descriptorVector = descriptorVector[colSums(!is.na(iniDataSet[descriptorVector])) != 0 & colSums(iniDataSet[descriptorVector] *1, na.rm = TRUE) > 0]
	} else {
		descriptorVector = descriptorVector[colSums(!is.na(iniDataSet[descriptorVector])) != 0]
	}
	errorDescriptor = tempDescriptor %GetDescriptorAfterCheckIfDescriptorNotExists% descriptorVector

	if (length(errorDescriptor) > 0) {
		errorDescriptor %errorReport% "NotNumericOrAllZero"	
	}
	
	if (length(descriptorVector) > 0) {
		return(as.data.frame(descriptorVector))
	} else {
		return(NA)
	}
}

overallChangeName <- function(overallList) {
	overallList$debug %debug% "overallChangefileName()"	
	
	if (!is.null(overallList$imageFileNames_nBoxplots)) {
		if (overallList$debug) {print("line plots...")}
		overallList$imageFileNames_nBoxplots = changefileName(overallList$imageFileNames_nBoxplots)
		names(overallList$imageFileNames_nBoxplots) = c(1:length(overallList$imageFileNames_nBoxplots))
		
		overallList$nBoxDesName = as.list(overallList$nBoxDesName)
		names(overallList$nBoxDesName) = c(1:length(overallList$nBoxDesName))
	}
	
	if (!is.null(overallList$imageFileNames_Boxplots)) {
		if (overallList$debug) {print("boxplots...")}
		overallList$imageFileNames_Boxplots = changefileName(overallList$imageFileNames_Boxplots)
		names(overallList$imageFileNames_Boxplots) = c(1:length(overallList$imageFileNames_Boxplots))
		
		overallList$boxDesName = as.list(overallList$boxDesName)
		names(overallList$boxDesName) = c(1:length(overallList$boxDesName))
	}
	
	if (!is.null(overallList$imageFileNames_StackedPlots)) {
		if (overallList$debug) {print("stacked boxplots...")}
		overallList$imageFileNames_StackedPlots = changefileName(overallList$imageFileNames_StackedPlots)
		names(overallList$imageFileNames_StackedPlots) = c(1:length(overallList$imageFileNames_StackedPlots))
		
		overallList$boxStackDesName = as.list(overallList$boxStackDesName)
		names(overallList$boxStackDesName) = c(1:length(overallList$boxStackDesName))
	}
	
	return(overallList)
}

setOptions <- function(overallList, typOfPlot, typOfOptions, listOfExtraOptions) {
	overallList$debug %debug% "setOptions()"
	
	for (values in names(listOfExtraOptions[[typOfPlot]])) {
		if (values %in% names(overallList[[typOfOptions]])) {
			overallList[[typOfOptions]][[values]] = c(overallList[[typOfOptions]][[values]], listOfExtraOptions[[typOfPlot]][[values]])
		} else {
			overallList[[typOfOptions]][[values]] = c(listOfExtraOptions[[typOfPlot]][[values]])
		}
	}
	return(overallList)
}


setSomePrintingOptions <- function(overallList) {
	overallList$debug %debug% "setSomePrintingOptions()"

	requestList = 	getSpecialRequestDependentOfUserAndTypOfExperiment()
	if (!(overallList$user == "none" & overallList$typOfExperiment == "none")) {
		listOfExtraOptions = requestList[[overallList$user]][[overallList$typOfExperiment]]	
		for (n in names(listOfExtraOptions)) {
			if (n == "boxplot") {
				return(setOptions(overallList, "boxplot", "boxOptions", listOfExtraOptions))				
			} else if (n == "nBoxplot") {
				return(setOptions(overallList, "nBoxplot", "nBoxOptions", listOfExtraOptions))
			} else if (n == "stackBoxplot") {
				return(setOptions(overallList, "stackBoxplot", "stackedBarOptions", listOfExtraOptions))
			}	
		}		
	}
	
	return(overallList)
}

checkUserOfExperiment <- function(overallList) {
	overallList$debug %debug% "checkUserOfExperiment()"

	user = "none"
	if ("Plant.ID" %in% colnames(overallList$iniDataSet)) {
		user = substr(overallList$iniDataSet$'Plant.ID'[1], 5, 6)		
	}
	overallList$user = user
	return(overallList)
}


checkTypOfExperiment <- function(overallList) {
	overallList$debug %debug% "checkTypOfExperiment()"

	typ = "none"
	if ("Species" %in% colnames(overallList$iniDataSet)) {
		if (length(grep("barley", overallList$iniDataSet$Species[1], ignore.case=TRUE)) > 0) {
			typ = "barley"
		} else if (length(grep("maize", overallList$iniDataSet$Species[1], ignore.case=TRUE)) > 0) {
			typ = "maize"
		} else if (length(grep("arabidopsis", overallList$iniDataSet$Species[1], ignore.case=TRUE)) > 0) {
			typ = "arabidopsis"
		}	
	}
	
	overallList$typOfExperiment = typ
	return(overallList)	
}


changefileName <- function(fileNameVector) {
	#Sollten hier nicht noch die Leerzeichen durch Punkte ersetzt werden?
	
	fileNameVector = gsub("\\$", ";", substr(fileNameVector, 1, 70))
	fileNameVector = gsub("\\^", "", fileNameVector)

	for(i in 1:length(fileNameVector)) {
		if(length(grep("\\.bin\\.",fileNameVector[i], ignore.case=TRUE)) > 0){
			stringSplit <- paste(strsplit(fileNameVector[i], '\\.bin\\.')[[1]][1], ".bin.", sep="")
			fileNameVector[i] <- stringSplit
		} 
	}

	return(as.list(fileNameVector))
}

preprocessingOfValues <- function(value, isColValue=FALSE, replaceString=".") {
	if (!is.null(value)) {
		regExpressionCol = "[^[:alnum:]|^_]|[[:space:]|\\^]"
		if (isColValue) {
			value = strsplit(value, "$", fixed=TRUE)
		}
		for (n in 1:length(value)) {
			value[[n]] = gsub(regExpressionCol, replaceString, value[[n]])
		}
	} else {
		return("none")
	}
	return(value)
}

overallPreprocessingOfDescriptor <- function(overallList) {
	overallList$debug %debug% "overallPreprocessingOfDescriptor()"	
	
	if (!is.null(overallList$nBoxDes)) {
		if (overallList$debug) {print("nBoxplot")}
		for (n in 1:length(overallList$nBoxDes)) {
			overallList$nBoxDes[n] = preprocessingOfDescriptor(overallList$nBoxDes[[n]], overallList$iniDataSet)
		}
	} else {
		print("nBoxplot is NULL")
	} 
	
	if (!is.null(overallList$boxDes)) {
		if (overallList$debug) {print("boxplot")}
		for (n in 1:length(overallList$boxDes)) {
			overallList$boxDes[n] = preprocessingOfDescriptor(overallList$boxDes[[n]], overallList$iniDataSet)
		}
	} else {
		print("Boxplot is NULL")
	} 
	
	if (!is.null(overallList$boxStackDes)) {
		if (overallList$debug) {print("stacked boxplot")}
		for (n in 1:length(overallList$boxStackDes)) {
			overallList$boxStackDes[n] = preprocessingOfDescriptor(overallList$boxStackDes[[n]], overallList$iniDataSet)
		}
	} else {
		print("stackedBoxplot is NULL")
	} 
	
	if (!sum(!is.na(overallList$boxStackDes)) > 0 && !sum(!is.na(overallList$boxDes)) > 0 && !sum(!is.na(overallList$nBoxDes)) > 0) {
		print("No descriptor set - this run needs to stop!")
		overallList$stoppTheCalculation = TRUE
	}
	return(overallList)
}

preprocessingOfDescriptor <- function(descriptorVector, iniDataSet) {
	#overallList$debug %debug% "preprocessingOfDescriptor()"
	descriptorVector = unlist(preprocessingOfValues(descriptorVector, TRUE))	#descriptor is value for yAxis
	
	errorDescriptor = descriptorVector %GetDescriptorAfterCheckIfDescriptorNotExists% iniDataSet 
	descriptorVector = descriptorVector %GetDescriptorsAfterCheckIfDescriptorExists% iniDataSet
	if (length(errorDescriptor)>0) {
		errorDescriptor %errorReport% "notExists"
	} 
	
	if (length(descriptorVector) > 0) {
		return(as.data.frame(descriptorVector))
	} else {
		return(NA)
	}	
}

preprocessingOfxAxisValue <- function(overallList) {
	overallList$debug %debug% "preprocessingOfxAxisValue()"
	overallList$xAxis = unlist(preprocessingOfValues(overallList$xAxis, TRUE))
	
	if (overallList$filterXaxis != "none") {
		overallList$filterXaxis = as.numeric(strsplit(overallList$filterXaxis, "$", fixed=TRUE)[[1]])
	} else {
		overallList$filterXaxis = as.numeric(unique(overallList$iniDataSet[overallList$xAxis])[[1]])	#xAxis muss einen Wert enthalten ansonsten Bricht das Program weiter oben ab
	}
	return(overallList)
}

getSingelFilter <- function(filter, treatment, dataSet) {
	if (filter != "none") {
		return(strsplit(filter, "$", fixed=TRUE)[[1]])
	} else {
		return(as.character(unique(dataSet[treatment])[[1]]))
	}
}

preprocessingOfTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheTreatment()"
	
	if (!is.null(overallList$treatment)) {
		overallList$treatment = preprocessingOfValues(overallList$treatment)
		
		if (overallList$treatment != "none" & (overallList$treatment %checkIfDescriptorExists% overallList$iniDataSet)) {	
			overallList$filterTreatment = getSingelFilter(overallList$filterTreatment, overallList$treatment, overallList$iniDataSet)

		} else {
			overallList$treatment = "none"
			overallList$filterTreatment = "none"
			print("Set 'filterTreatment' and 'treatment' to 'none'!")		
		}			
	} else {
		overallList$treatment = "none"
		overallList$filterTreatment = "none"
		print("Set 'filterTreatment' and 'treatment' to 'none'!")
	}
	return(overallList)
}

preprocessingOfSecondTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheSecondTreatment()"
	
	if (!is.null(overallList$secondTreatment)) {
		overallList$secondTreatment = preprocessingOfValues(overallList$secondTreatment)

		if (overallList$secondTreatment != "none" & (overallList$secondTreatment %checkIfDescriptorExists% overallList$iniDataSet)) {
			overallList$filterSecondTreatment = getSingelFilter(overallList$filterSecondTreatment, overallList$secondTreatment, overallList$iniDataSet)
			
		} else {
			overallList$secondTreatment = "none"
			overallList$filterSecondTreatment = "none"
			print("Set 'filterSecondTreatment' and 'secondTreatment' to 'none'!")
		}	
	} else {
		overallList$secondTreatment = "none"
		overallList$filterSecondTreatment = "none"
		print("Set 'filterSecondTreatment' and 'secondTreatment' to 'none'!")
	}
	return(overallList)
}

check <- function(value, checkValue=c("none", NA)) {
	if (!is.null(value)) {
		return(value %GetDescriptorAfterCheckIfDescriptorNotExists% checkValue)
	} else {
		return(character(0))
	}
}

getVector <- function(descriptorSet) {
	if (!is.null(descriptorSet)) {
		vector =  vector()
		for (n in 1:length(descriptorSet)) {
			vector = c(vector, as.vector(unlist(descriptorSet[[n]])))
		}
		return(vector)
	}
	return(character(0))
}

reduceWorkingDataSize <- function(overallList) {
	overallList$debug %debug% "reduceWorkingDataSize()"
	overallList$iniDataSet = overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
	return(overallList)
}

setRowAndColNameOfFinalDataFrame <- function(overallList) {
	overallList$debug %debug% "setRowAndColNameOfFinalDataFrame()"

	overallList$rowName = (overallList$descriptor %contactAllWithAll% overallList$filterTreatment) %contactAllWithAll% overallList$filterSecondTreatment
	overallList$colName = as.character(overallList$filterXaxis)
	
	return(overallList)
}

groupByFunction <- function(groupByList) {
	groupByList = unlist(groupByList)
	return(unlist(groupByList[ifelse(groupByList != "none", TRUE, FALSE)]))
}

getBooleanVectorForFilterValues <- function(groupedDataFrame, listOfValues, plot=FALSE) {
	
	iniType = !plot
	tempVector = rep.int(iniType, times=length(groupedDataFrame[, 1]))
	
	for (h in names(listOfValues)) {
		if (h != "none" & !is.null(groupedDataFrame[[h]])) {
			if(plot) {
				tempVector = tempVector | groupedDataFrame[[h]] %in% listOfValues[[h]]
			} else {
				tempVector = tempVector & groupedDataFrame[[h]] %in% listOfValues[[h]]
			}
		}
	}
	return(tempVector)
}

buildRowForOverallList <- function(i, des, listOfValues, dataSet, day) {
	rowString = list(row=des, day=numeric())
	for (k in listOfValues) {
		if (k != "none") {
			rowString$row = paste(rowString$row, dataSet[i, k])
		}
	}
	return(rowString)
} 

fillOverallResult <- function(overallList, preErrorBars) {
	overallList$debug %debug% "fillOverallResult()"
	if (length(overallList$iniDataSet[, 1]) > 0) {
		for (i in 1:length(overallList$iniDataSet[, 1])) {
			for (des in overallList$descriptor) {
				rowAndColumn = buildRowForOverallList(i, des, c(overallList$treatment, overallList$secondTreatment), overallList$iniDataSet, overallList$xAxis)
				overallList$overallResult[rowAndColumn$row, as.character(overallList$iniDataSet[i, overallList$xAxis])] = overallList$iniDataSet[i, des]
				if (tolower(overallList$diagramTyp) != "boxplotstacked")
					overallList$errorBars[rowAndColumn$row, as.character(overallList$iniDataSet[i, overallList$xAxis])] = preErrorBars[i, des]
			}
		}
	} else {
		print("No Value for the OverallResult-DataFrame - Wrong filter!")
		overallList$stoppTheCalculation = TRUE
	}
	return(overallList)
}

buildList <- function(overallList, colOfXaxis) {
	overallList$debug %debug% "buildList()"
	newList = list()
	
	newList[[overallList$treatment]] = overallList$filterTreatment
	newList[[overallList$secondTreatment]] = overallList$filterSecondTreatment
	newList[[colOfXaxis]] = overallList$filterXaxis
	
	return(newList)
}

buildRowName <- function(mergeDataSet, groupBy, yName = "value") {	
	if (length(groupBy) == 0) {
		return(data.frame(name=rep.int(yName, length(mergeDataSet[, 1])), mergeDataSet))
	} else if (length(groupBy) == 1) {
		return(data.frame(name=mergeDataSet[, groupBy], mergeDataSet[, !(colnames(mergeDataSet) %in% groupBy)]))
	} else {		
		temp = mergeDataSet[, groupBy[1]]
		for (h in 2:length(groupBy)) {
			temp = paste(temp, mergeDataSet[, groupBy[h]], sep = "/") #  #/#
		}
		return(data.frame(name=temp, primaerTreatment= mergeDataSet[, groupBy[1]], mergeDataSet[, mergeDataSet %allColnamesWithoutThisOnes% groupBy]))
	}	
}

getToPlottedDays <- function(xAxis, changes=NULL) {
	uniqueDays = unique(xAxis)
	medianPosition = floor(median(1:length(uniqueDays)))

	days = uniqueDays[floor(median(1:length(uniqueDays[uniqueDays<=uniqueDays[medianPosition]])))]
	days = c(days, uniqueDays[medianPosition])
	days = c(days, uniqueDays[floor(median(length(uniqueDays[uniqueDays>=uniqueDays[medianPosition]]):length(uniqueDays)))])
	days = c(days, uniqueDays[length(uniqueDays)])
	 
	if (!is.null(changes)) {
		days = c(as.numeric(changes), days[(length(changes)+1):4])
	}
	
	return(days)
}

setxAxisfactor <- function(xAxisName, xAxisValue, options) {
	if (!is.null(options$daysOfBoxplotNeedsReplace)) {
		whichDayShouldBePlot = getToPlottedDays(xAxisValue, options$daysOfBoxplotNeedsReplace)
	} else {
		whichDayShouldBePlot = getToPlottedDays(xAxisValue)
	}
		
	xAxisfactor = factor(xAxisValue, levels=whichDayShouldBePlot)

	xAxisfactor = paste(xAxisName, xAxisfactor)
	naString = paste(xAxisName, "NA")
	xAxisfactor[xAxisfactor == naString] = NA
	
#	xAxisfactor = paste("DAS", xAxisfactor)
#	xAxisfactor[xAxisfactor == "DAS NA"] = NA
	return(xAxisfactor)
}


overallGetResultDataFrame <- function(overallList) {
	overallList$debug %debug% "overallGetResultDataFrame()"	
	groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
	colNames = list(colOfXaxis="xAxis", colOfMean="mean", colOfSD="se", colName="name", xAxis=overallList$xAxis)
	booleanVectorList = buildList(overallList, colNames$colOfXaxis)
	columnsStandard = c(check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment))
		
	if (sum(!is.na(overallList$nBoxDes)) > 0) {
		if (overallList$debug) {print("nBoxplot")}
		columns = c(columnsStandard, check(getVector(overallList$nBoxDes)))
		overallList$overallResult_nBoxDes = getResultDataFrame("nboxplot", overallList$nBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
	} else {
		print("All values for nBoxplot are 'NA'")
	}
	
	if (sum(!is.na(overallList$boxDes)) > 0) {
		if (overallList$debug) {print("Boxplot")}
		colNames$colOfMean = "value"
		columns = c(columnsStandard, check(getVector(overallList$boxDes)))
		overallList$overallResult_boxDes = getResultDataFrame("boxplot", overallList$boxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
	} else {
		print("All values for Boxplot are 'NA'")
	}
	
	if (sum(!is.na(overallList$boxStackDes)) > 0) {
		if (overallList$debug) {print("stackedBoxplot")}
		colNames$colOfMean = check(getVector(overallList$boxStackDes))
		colNames$colOfXaxis = overallList$xAxis
		columns = c(columnsStandard, check(getVector(overallList$boxStackDes)))
		overallList$overallResult_boxStackDes = getResultDataFrame("boxplotStacked", overallList$boxStackDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
	} else {
		print("All values for stackedBoxplot are 'NA'")
	}
	
	if (is.null(overallList$boxStackDes) && is.null(overallList$boxDes) && is.null(overallList$nBoxDes)) {
		print("No descriptor set - this run needs to stop!")
		overallList$stoppTheCalculation = TRUE
	}
	return(overallList)
}

getPlotNumber <- function(colNameWichMustBind, descriptorList) {
	for (n in names(descriptorList)) {
		if (colNameWichMustBind %in% as.vector(unlist(descriptorList[[n]]))) {
			return(n)
		}
	}
	return(-1)
}
#descriptorName kann entfernt werden
getResultDataFrame <- function(diagramTyp, descriptorList, iniDataSet, groupBy, colNames, booleanVectorList, debug) {	
	debug %debug% "getResultDataFrame()"
	
	descriptor = getVector(descriptorList)
	descriptorName = seq(1:length(descriptor))
	
	descriptorName = descriptorName[!is.na(descriptor)]
	descriptor = descriptor[!is.na(descriptor)]
	
	if (diagramTyp != "boxplot") {
		groupedDataFrame = data.table(iniDataSet)
		key(groupedDataFrame) = c(groupBy, colNames$xAxis)		
	}
	
	if (diagramTyp == "boxplot") {
		groupedDataFrameMean = iniDataSet[groupBy[1]]
		
		groupByReduce = groupBy[groupBy!=groupBy[1]]
		for (n in c(groupByReduce, colNames$xAxis, descriptor)) {
			groupedDataFrameMean = cbind(groupedDataFrameMean, iniDataSet[n])
		}		
	} else {
		groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(.SD, mean, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
	}
	
	if (diagramTyp == "nboxplot" || diagramTyp == "boxplot") {
		#colNamesOfTheRest = paste(colNames$colOfMean, seq(1:length(descriptor)), sep="")	
		colNamesOfTheRest = paste(colNames$colOfMean, descriptorName, sep="")	
	} else {
		colNamesOfTheRest = groupedDataFrameMean %allColnamesWithoutThisOnes% c(groupBy, colNames$xAxis)
	}

	colnames(groupedDataFrameMean) = c(groupBy, colNames$colOfXaxis, colNamesOfTheRest)
	
	if (diagramTyp == "nboxplot") {
		groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(.SD, sd, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		colnames(groupedDataFrameSD) = c(groupBy, colNames$colOfXaxis, paste(colNames$colOfSD, descriptorName, sep=""))
	}
	
	booleanVector = getBooleanVectorForFilterValues(groupedDataFrameMean, booleanVectorList)
	
	if (diagramTyp == "nboxplot") {
		iniDataSet = merge(sort=FALSE, groupedDataFrameMean[booleanVector, ], groupedDataFrameSD[booleanVector, ], by = c(groupBy, colNames$colOfXaxis))
		#overallResult = buildRowName(iniDataSet, groupBy, descriptorName)
		overallResult = buildRowName(iniDataSet, groupBy)
	} else
	if (diagramTyp == "boxplot") {
		iniDataSet = groupedDataFrameMean[booleanVector, ]
		#overallResult = buildRowName(iniDataSet, groupBy, descriptorName)
		overallResult = buildRowName(iniDataSet, groupBy)
	} else {
		iniDataSet = groupedDataFrameMean[booleanVector, ]		
		#buildRowNameDataSet = buildRowName(iniDataSet, groupBy, descriptorName)
		buildRowNameDataSet = buildRowName(iniDataSet, groupBy)
		temp = data.frame()
		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, "primaerTreatment")) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList)
		
			if (plot!=-1) {
				colNameWichMustBindReNamed = reNameHist(colNameWichMustBind)
		
				if (is.null(buildRowNameDataSet$primaerTreatment)) {	
					temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot=plot))			
				} else {
					temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), primaerTreatment=buildRowNameDataSet[, "primaerTreatment"], values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot = plot))			
				}
			}
		}
		overallResult = temp		
	}
	return(overallResult)
}

setDefaultAxisNames <- function(overallList) {
	overallList$debug %debug% "setDefaultAxisNames()"
	
	if (overallList$xAxisName == "none") {
		overallList$xAxisName = gsub('[[:punct:]]', " ", overallList$xAxis)
	}
#	if (overallList$yAxisName == "none") {
#		overallList$yAxisName = gsub('[[:punct:]]', " ", overallList$descriptor)
#	}
	return(overallList)
}

setColorListHist <- function(descriptorList) {
	interval = seq(0.05, 0.95, by=0.1)
	intervalSat = rep.int(c(0.8, 1.0), 5)
	intervalFluo = seq(0, 0.166666666666, by=0.0185185185)

	interval20 = seq(0.025, 0.975, by=0.05)
	intervalSat20 = rep.int(c(0.8, 1.0), 10)
	intervalFluo20 = seq(0, 0.166666666666, by=0.008771929789)
	
	if (length(grep("fluo", getVector(descriptorList), ignore.case=TRUE)) > 0) { #rot			
		colorList = as.list(hsv(h=c(rev(intervalFluo), rev(intervalFluo20)), s=c(intervalSat, intervalSat20), v=1))
	} else 
	if (length(grep("phenol", getVector(descriptorList), ignore.case=TRUE)) > 0) { #gelb
		colorList = as.list(hsv(h=c(intervalFluo, intervalFluo20), s=c(intervalSat, intervalSat20), v=1))
	} else 
	if (length(grep("vis", getVector(descriptorList), ignore.case=TRUE)) > 0) {
		colorList = as.list(hsv(h=c(interval, interval20), s=1, v=c(intervalSat, intervalSat20)))
	} else 
	if (length(grep("nir", getVector(descriptorList), ignore.case=TRUE)) > 0) {
		colorList = as.list(rgb(c(rev(interval), rev(interval20)), c(rev(interval), rev(interval20)), c(rev(interval),rev(interval20)), max = 1))		
	} else {
		return(list(0))
	}
	names(colorList) = c(
		"0..25", "25..51", "51..76", "76..102", "102..127", 
		"127..153", "153..178", "178..204", "204..229", "229..255", 
		"0..12", "12..25", "25..38", "38..51", "51..63", "63..76", "76..89", 
		"89..102", "102..114", "114..127", "127..140", "140..153", "153..165", 
		"165..178", "178..191", "191..204", "204..216", "216..229", "229..242", 
		"242..255")
	return(colorList)
}

setColorList <- function(diagramTyp, descriptorList, overallResult, isGray) {
	if (!as.logical(isGray)) {
		colorVector = c(brewer.pal(11, "Spectral"))
	} else {
		colorVector = c(brewer.pal(9, "Greys"))
	}
	
	colorList = list()
	if (diagramTyp == "nboxplot" || diagramTyp == "boxplot") {
		for (n in names(descriptorList)) {
			#if (!is.na(descriptorList[[n]])) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
			} else {
				#print("All values are 'NA'")
			}
		}
	} else {
		for (n in names(descriptorList)) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				colorList[[n]] = setColorListHist(descriptorList[n])
			} else {
				#print("All values are 'NA'")
			}
		}
	}
	return(colorList)
}

setColor <- function(overallList) {
	overallList$debug %debug% "setColor()"  
	overallList$color_nBox = setColorList("nboxplot", overallList$nBoxDes, overallList$overallResult_nBoxDes, overallList$isGray)
	overallList$color_box = setColorList("boxplot", overallList$boxDes, overallList$overallResult_boxDes, overallList$isGray)
	overallList$color_boxStack = setColorList("boxplotStacked", overallList$boxStackDes, overallList$overallResult_boxStackDes, overallList$isGray)
	return(overallList)
}

normalizeToHundredPercent =  function(whichRows, overallResult) {
	return(t(apply(overallResult[whichRows, ], 1, function(x, y) {(100*x)/y}, y=colSums(overallResult[whichRows, ]))))
}

writeLatexFile <- function(fileNameLatexFile, fileNameImageFile="", o="") {
	fileNameImageFile = preprocessingOfValues(fileNameImageFile, FALSE, "_")
	fileNameLatexFile = preprocessingOfValues(fileNameLatexFile, FALSE, "_")
	o = gsub('[[:punct:]]', "_", o)
	
	latexText = paste("\\loadImage{", 
					   ifelse(fileNameImageFile == "", fileNameLatexFile, fileNameImageFile), 
					  #ifelse(o == "", "", paste("_", o , sep="")), 
					   ifelse(o == "", "", o), 
					   ".pdf}", sep="")
	
	write(x=latexText, append=TRUE, file=paste(fileNameLatexFile, "tex", sep="."))
}

saveImageFile <- function(overallList, plot, fileName, extraString="") {
	filename = preprocessingOfValues(paste(fileName, extraString, sep=""), FALSE, replaceString = "_")	
	ggsave (filename=paste(filename, overallList$saveFormat, sep="."), plot = plot, dpi=as.numeric(overallList$dpi), width=8, height=5)
}

makeDepthBoxplotDiagram <- function(h, overallList) {
	overallList$debug %debug% "makeDepthBoxplotDiagram()"
	overallList$symbolParameter = 15
	
	if (h == 1) {
		openImageFile(overallList)
	}
	par(mar=c(4.1, 4.1, 2.1, 2.1))
	plot.depth(as.matrix(overallList$overallResult), plot.type=h, xlabel=overallList$xAxisName, l.width=12, lp.color=overallList$color)
	
	grid()
	if (h == 1) {
		dev.off()
	}
	if (overallList$appendix) {
		writeLatexFile("appendixImage", overallList$fileName)
	}
	
	return(overallList)
}

CheckIfOneColumnHasOnlyValues <- function(overallResult, descriptor="", diagramTyp="nboxplot") {	
	max = -1	
	for (index in levels(overallResult$name)) {
		if (diagramTyp == "nboxplot" || diagramTyp == "boxplot") {
			temp = sum(!is.na(overallResult$mean[overallResult$name == index]))
		} else {
			boolVec = overallResult$name == index
			temp = sum(!is.na(overallResult[boolVec, descriptor]))
		}
		max = ifelse(temp > max, temp, max)
	}	
	return(ifelse(max == 1, TRUE, FALSE))
}

buildMyStats <- function(values, means, se) {
	means = as.data.frame(as.vector(means))
	colnames(means) = "means"
	
	se = as.data.frame(as.vector(se))
	colnames(se) = "se"

	return(data.frame(value=values, means=means, se=se))
}

buildMyStats2 <- function(values, means, se, rowName) {
	means = as.data.frame(as.vector(means))
	colnames(means) = "means"
	
	rowName = as.data.frame(as.vector(rowName))
	colnames(rowName) = Name
	
	se = as.data.frame(as.vector(se))
	colnames(se) = "se"
	
	return(data.frame(value=values, means=means, se=se, rowName=rowName))
}

reduceOverallResult <- function(tempOverallList, imagesIndex) {
	tempOverallList$debug %debug% "reduceOverallResult()"

	workingDataSet = buildDataSet(tempOverallList$overallResult[, 1:2], tempOverallList, c("mean", "se"), imagesIndex)
	colnames(workingDataSet) = c(colnames(workingDataSet)[1:2], "mean", "se")
	return(workingDataSet)	
}


reduceWholeOverallResultToOneValue <- function(tempOverallResult, imagesIndex, debug, diagramTyp="nboxplot") {
	debug %debug% "reduceWholeOverallResultToOneValue()"
	
	if (diagramTyp == "boxplotstacked") {
		workingDataSet = tempOverallResult[tempOverallResult$plot == imagesIndex, ]
		workingDataSet$hist = factor(workingDataSet$hist, unique(workingDataSet$hist))
	} else {
		colNames = vector()
		if (diagramTyp == "nboxplot" || diagramTyp == "barplot") {
			colNames = c("mean", "se")
		} else if (diagramTyp == "boxplot") {
			colNames = c("value")
		} 
		
		if ("primaerTreatment" %in% tempOverallResult) {
			standardColumnName = c("name", "primaerTreatment", "xAxis")
		} else {
			standardColumnName = c("name", "xAxis")
		}
				
		if (sum(!(colNames %in% colnames(tempOverallResult)))>0) {
			workingDataSet = buildDataSet(tempOverallResult[, standardColumnName], tempOverallResult, colNames, imagesIndex)
			colnames(workingDataSet) = c(standardColumnName, colNames)
		} else {
			workingDataSet = tempOverallResult
		}
	}
	return(workingDataSet)	
}

createOuputOverview <- function(typ, actualImage, maxImage, imageName) {
	print(paste("create ", typ, " ", actualImage, "/", maxImage, ": '",imageName, "'", sep=""))
}

makeLinearDiagram <- function(h, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="nboxplot") {
	overallList$debug %debug% "makeLinearDiagram()"	
	print("line plot...")
	
	#tempOverallResult =  na.omit(overallResult)
	tempOverallResult =  overallResult

	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {
			createOuputOverview("line plot", imagesIndex, length(names(overallDescriptor)),  overallDesName[[imagesIndex]])
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "nboxplot")
			overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
			overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0
			
			if (length(overallResult[, 1]) > 0) {
				
				if (!CheckIfOneColumnHasOnlyValues(overallResult)) {

					plot <-	ggplot(data=overallResult, aes(x=xAxis, y=mean, shape=name)) +
							#geom_smooth(aes(ymin=mean-se, ymax=mean+se, colour=name, fill=name), stat="identity", alpha=0.1) +
							geom_ribbon(aes(ymin=mean-se, ymax=mean+se, fill=name), stat="identity", alpha=0.1) +
							geom_line(aes(color=name), alpha=0.2) +
							geom_point(aes(color=name), size=3) +
#							print("drinne")
#							print(overallResult$xAxis)
#							print(min(as.numeric(as.character(overallResult$xAxis))))
#							print(max(as.numeric(as.character(overallResult$xAxis))))
#							plot <-  plot + 
							scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis)))) +					
							ylab(overallDesName[[imagesIndex]]) +
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
									#legend.key.size = unit(1.5, "lines"), 
									legend.key = theme_blank(), # switch off the rectangle around symbols in the legend
									panel.border = theme_rect(colour="Grey", size=0.1)
							)
					
					if (length(overallColor[[imagesIndex]]) > 25 & length(overallColor[[imagesIndex]]) < 35) {
						plot = plot + opts(legend.text = theme_text(size=6), 
											legend.key.size = unit(0.7, "lines")
											)
					} else if(length(overallColor[[imagesIndex]]) >= 35) {
						plot = plot + opts(legend.text = theme_text(size=4), 
											legend.key.size = unit(0.4, "lines")
						)
					} else {
						plot = plot + opts(legend.text = theme_text(size=11))
					}
								
	#			print(plot)
		
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
	


					if (h == 1) {
						saveImageFile(overallList, plot, overallFileName[[imagesIndex]], diagramTypSave)
					} else {
						print(plot)
					}
					if (overallList$appendix) {
						writeLatexFile("appendixImage", overallFileName[[imagesIndex]], diagramTypSave)
					}
				} else {
					print("Only one column has values, create barplot!")
			
					day = overallResult$xAxis[!is.na(overallResult$mean)][1]
					tempXaxisName = overallList$xAxisName
					overallList$xAxisName = paste(overallList$xAxisName, day)
					#overallList$overallResult = overallList$overallResult[!is.na(overallList$overallResult$mean), ]
					makeBarDiagram(h, overallResult, overallDescriptor[imagesIndex], overallColor[imagesIndex], overallDesName[imagesIndex], overallFileName[imagesIndex], overallList, TRUE, diagramTypSave)
					overallList$xAxisName = tempXaxisName
				}
			}
		}
	}
}

getColor <- function(overallColorIndex, overallResult) {
	input = as.vector(unique(overallResult$hist))
	
	color = vector()
	for (n in input) {
		color = c(color, overallColorIndex[[n]])
	}
	return(color)
}


plotStackedImage <- function(h, overallList, overallResult, title = "", makeOverallImage = FALSE, legende=TRUE, minor_breaks=TRUE, overallColor, overallDesName, imagesIndex, overallFileName) {
	overallList$debug %debug% "plotStackedImage()"	
	if (length(overallResult[, 1]) > 0) {

		if (length(overallList$stackedBarOptions$typOfGeomBar) == 0) {
			overallList$stackedBarOptions$typOfGeomBar = c("fill")
		}
		
		for (positionType in overallList$stackedBarOptions$typOfGeomBar) {			
				if (positionType == "dodge") {				
					plot = ggplot(overallResult, aes(x=xAxis, y=values, colour=hist)) +
							geom_line(position="identity") + 
							#scale_fill_manual(values = overallColor[[imagesIndex]]) +
							scale_colour_manual(values= getColor(overallColor[[imagesIndex]], overallResult)) 
					
				} else {
					plot = ggplot(overallResult, aes(xAxis, values, fill=hist)) +
							geom_bar(stat="identity", position = positionType)
				}
								
				if (positionType == "dodge" || positionType == "stack") {
					name = sub("%", "px", overallDesName[[imagesIndex]])
				} else {
					name = overallDesName[[imagesIndex]]
				}

				 	plot = plot + ylab(name) 
					#coord_cartesian(ylim=c(0, 1)) +
			
			if (minor_breaks) {
				plot = plot + scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(overallResult$xAxis):max(overallResult$xAxis))
			} else {
				plot = plot + xlab(overallList$xAxisName)
			}
							
			plot = plot +		
					scale_fill_manual(values = getColor(overallColor[[imagesIndex]], overallResult), name="") +
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
			
			if (!legende) {
				plot = plot + opts(legend.position="none")
			} else {
				plot = plot + opts(legend.position="right", 
									legend.title = theme_blank(), 
									legend.text = theme_text(size=11), 
									legend.key = theme_blank())
			}
			
			if (title != "") {
				plot = plot + opts(title = title)
			}
			
			if (!minor_breaks) {
				plot = plot + opts(panel.grid.minor = theme_blank())
			}
			
			if (positionType == "fill") {
				plot = plot + scale_y_continuous(labels=seq(0, 100, 20), breaks=seq(0, 1, 0.2))
			}
			
			if (makeOverallImage) {
				#plot = plot + facet_wrap(~ name, drop=TRUE)
				#plot = plot + facet_wrap(~ name)
				if ("primaerTreatment" %in% colnames(overallResult)) {				
					plot = plot + facet_wrap(~ primaerTreatment)
				} else {
					plot = plot + facet_wrap(~ name)
				}
			}
			
			if (h == 1) {
				saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste("overall", title, positionType, sep=""))
				if (makeOverallImage) {
					writeLatexFile(paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep=""), paste(overallFileName[[imagesIndex]], "overall", title, positionType, sep=""))	
				} else {
					writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], "overall", positionType, title, sep="_"))	
				}
			} else {
				print(plot)
			}
		}
	}
}

PreWorkForMakeBigOverallImage <- function(h, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex) {
	overallList$debug %debug% "PreWorkForMakeBigOverallImage()"	
	groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
	if (length(groupBy) == 0 || length(groupBy) == 1) {
		plotStackedImage(h = h, overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende=TRUE, minor_breaks=FALSE, overallColor = overallColor, overallDesName = overallDesName, imagesIndex= imagesIndex, overallFileName =overallFileName)	
	} else {
		for (value in overallList$filterSecondTreatment) {
			title = value
			plottedName = overallList$filterTreatment %contactAllWithAll% value
			booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = plottedName))
			plotThisValues = overallResult[booleanVector, ]
		#	plotThisValues = reNameColumn(plotThisValues, "name", "primaerTreatment")
			plotStackedImage(h, overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende=TRUE, minor_breaks=FALSE, overallColor = overallColor, overallDesName = overallDesName, imagesIndex=imagesIndex, overallFileName=overallFileName)
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

makeBoxplotStackedDiagram <- function(h, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList) {
	overallList$debug %debug% "makeBoxplotStackedDiagram()"
	print("stacked barplot...")
	#overallResult[is.na(overallResult)] = 0
	tempOverallResult =  na.omit(overallResult)
	#tempOverallResult = overallResult
	
	for (imagesIndex in names(overallDescriptor)) {
		createOuputOverview("stacked barplot", imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
		overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "boxplotstacked")

		if (length(overallResult[, 1]) > 0) {
			PreWorkForMakeBigOverallImage(h, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex)
		}
		#PreWorkForMakeNormalImages(h, overallList)
	}
	#return(overallList)
}	

makeBarDiagram <- function(h, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, isOnlyOneValue = FALSE, diagramTypSave="barplot") {
	overallList$debug %debug% "makeBarDiagram()"

	tempOverallResult =  overallResult

	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {	
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "barplot")
			overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
			overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0
			
			if (length(overallResult[, 1]) > 0) {
				if (isOnlyOneValue) {
					myPlot = ggplot(data=overallResult, aes(x=name, y=mean))
				} else {
					myPlot = ggplot(data=overallResult, aes(x=xAxis, y=mean))
				}
				
				myPlot = myPlot + 						
						geom_bar(stat="identity", aes(fill=name), colour="Grey", size=0.1) +
						geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.2, colour="black")+
						#geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.5, colour="Pink")+
						ylab(overallDesName[[imagesIndex]]) +
						coord_cartesian(ylim=c(0, max(overallResult$mean + overallResult$se + 10, na.rm=TRUE))) +
						xlab(overallList$xAxisName) +
						scale_fill_manual(values = overallColor[[imagesIndex]]) +
						theme_bw() +
						opts(legend.position="none", 
								plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
								axis.title.x = theme_text(face="bold", size=11), 
								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1)
						)
				
				if (length(overallColor[[imagesIndex]]) > 10) {
					myPlot = myPlot + opts(axis.text.x = theme_text(size=6, angle=90))
				}
				#print(myPlot)
				
				if (h == 1) {
					saveImageFile(overallList, myPlot, overallFileName[[imagesIndex]], diagramTypSave)
				} else {
					print(myPlot)
				}
				if (overallList$appendix) {
					writeLatexFile("appendixImage", overallFileName[[imagesIndex]], diagramTypSave)
				}
			}
		}
	}
}

makeBoxplotDiagram <- function(h, overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, options, overallList, diagramTypSave="boxplot") {	
	overallList$debug %debug% "makeBoxplotDiagram()"
	print("create boxplot...")
	tempOverallResult =  na.omit(overallResult)
	
	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {
			#print(paste("Process ", overallDesName[[imagesIndex]]))
			createOuputOverview("boxplot", imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
			
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "boxplot")
			if (length(overallResult[, 1]) > 0) {
				overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult$xAxis, options)	
			
				#myPlot = ggplot(overallList$overallResult, aes(factor(name), value, fill=name, colour=name)) + 
				#myPlot = ggplot(overallResult, aes(factor(name), value, fill=name)) +
			
				myPlot = ggplot(overallResult, aes(factor(name), value, fill=name)) +
						geom_boxplot() +
						ylab(overallDesName[[imagesIndex]]) +
						#coord_cartesian(ylim=c(0, max(overallList$overallResult$mean + overallList$overallResult$se + 10, na.rm=TRUE))) +
						#xlab(paste(min(overallResult$xAxis), overallList$xAxisName, "..", max(overallResult$xAxis), overallList$xAxisName)) +
						scale_fill_manual(values = overallColor[[imagesIndex]]) +
						#stat_summary(fun.data = f, geom = "crossbar", height = 0.1, 	colour = NA, fill = "skyblue", width = 0.8, alpha = 0.5) +
						theme_bw() +
						opts(legend.position="none", 
								plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
								axis.title.x = theme_blank(), 
								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1)
								
						) +
						opts(axis.text.x = theme_text(size=6, angle=90)) +
						facet_wrap(~ xAxisfactor, drop=FALSE)
						
				if (h == 1) {
					saveImageFile(overallList, myPlot, overallFileName[[imagesIndex]], diagramTypSave)
				} else {
					print(myPlot)
				}
				if (overallList$appendix) {
					writeLatexFile("appendixImage", overallFileName[[imagesIndex]], diagramTypSave)
				}
			}
		}
	}
	#return(overallList)
}

makeDiagrams <- function(overallList) {
	overallList$debug %debug% "makeDiagrams()"
	run = ifelse(overallList$showResultInR, 2, 1)
	
	for (h in 1:run) {
		if (sum(!is.na(overallList$nBoxDes)) > 0) {
			if (overallList$debug) {print("nBoxplot...")}
			makeLinearDiagram(h, overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$color_nBox, overallDesName=overallList$nBoxDesName, overallList$imageFileNames_nBoxplots , overallList)
		} else {
			print("All values for nBoxplot are 'NA'")
		}
		
		if (sum(!is.na(overallList$boxDes)) > 0) {
			if (overallList$debug) {print("Boxplot...")}
			makeBoxplotDiagram(h, overallList$overallResult_boxDes, overallList$boxDes, overallList$color_box, overallDesName=overallList$boxDesName, overallList$imageFileNames_Boxplots, overallList$boxOptions, overallList)
		} else {
			print("All values for Boxplot are 'NA'...")
		}
		
		if (sum(!is.na(overallList$boxStackDes)) > 0) {
			if (overallList$debug) {print("Stacked Boxplot...")}
			makeBoxplotStackedDiagram(h, overallList$overallResult_boxStackDes, overallList$boxStackDes, overallList$color_boxStack, overallDesName=overallList$boxStackDesName, overallList$imageFileNames_StackedPlots, overallList)
		} else {
			print("All values for stacked Boxplot are 'NA'...")
		}
		
		if (FALSE) {	# falls auch mal barplots erstellt werden sollen (ausser wenn nur ein Tag vorhanden ist!)
			if (overallList$debug) {print("Barplot...")}
			makeBarDiagram(h, overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$color_nBox, overallDesName=overallList$nBoxDesName, overallList$imageFileNames_nBoxplots, overallList)
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
			#print(paste("Adding ", addCol, "with description", addColDesc))
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
		print("Check if the noValues-Image is there")
		file = "noValues.pdf"
		if (!file.exists(file)) {
			library("Cairo")
			print(paste("Create defaultImage '", file, "'", sep=""))
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
	if (DEBUG) {
		options(error = quote({
			#sink(file="error.txt", split = TRUE);
			dump.frames();
			print(attr(last.dump, "error.message"));
			#x = attr(last.dump, "error.message")
			traceback();
			#sink(file=NULL);		
			#q()
		}))
		options(show.error.messages = TRUE)
		#options(showWarnCalls = TRUE)
		#options(showErrorCalls = TRUE)
		options(warn = 0)
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
}

startOptions <- function(typOfStartOptions = "test", debug=FALSE) {
	initRfunction(debug)
	#typOfStartOptions = "test"
	typOfStartOptions = tolower(typOfStartOptions)
	
	args = commandArgs(TRUE)
	
	saveFormat = "pdf"
	dpi = "90" ##90 ## CK: seems to change nothing for ggplot2 instead the output size should be modified, if needed // 17.2.2012	
	
	isGray = FALSE
	showResultInR = FALSE
	
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
					descriptorSet_nBoxplot = c("Weight A (g)", 
												"Weight B (g)", 
												"Water (weight-diff)", 
												"side.height.norm (mm)", 
												"side.width.norm (mm)", 
												"side.area.norm (mm^2)", 
												"top.area.norm (mm^2)", 											
												"side.fluo.intensity.chlorophyl.average (relative)", 
												"side.fluo.intensity.phenol.average (relative)", 
												"side.nir.intensity.average (relative)", 
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
												"side.height (mm)", 
												"side.width (mm)", 
												"side.area (px)", 
												"top.area (px)")
				
					descriptorSetName_nBoxplot = c("weight before watering (g)", 
													"weight after watering (g)", 
													"water weight (g)", 
													"height (mm)", 
													"width (zoom corrected) (mm)", 
													"side area (zoom corrected) (mm^2)", 
													"top area (zoome corrected) (mm^2)", 
													"chlorophyl intensity (relative intensity/pixel)", 
													"fluorescence intensity (relative intensity/pixel)", 
													"nir intensity (relative intensity/pixel)", 
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
													"volume groeth (visible light images, IAP based formula) (%/day)", 
													"height (px)", 
													"width (px)", 
													"side area (px)", 
													"top area (px)")		
				}
	
				nBoxOptions= NULL
				#diagramTypVector = rep.int("nboxplot", times=length(descriptorSetName))
		
				#boxplot
				descriptorSet_boxplot = c("side.height.norm (mm)", 
										   "side.width.norm (mm)", 
										   "side.area.norm (mm^2)", 
										   "top.area.norm (mm^2)", 
										   "volume.fluo.iap", 
										   "volume.iap (px^3)", 
										   "volume.lt (px^3)", 
										   "side.height (mm)", 
										   "side.width (mm)", 
										   "side.area (px)", 
										   "top.area (px)")
				
				descriptorSetName_boxplot = c("height (zoom corrected) (mm)", 
										  	   "width (zoom corrected) (mm)", 
											   "side area (zoom corrected) (mm^2)", 
											   "top area (zoom corrected) (mm^2)", 
											   "digital biomass (flurescence images, IAP formula) (px^3)", 
											   "digital biomass (visible light images, IAP formula) (px^3)", 
											   "digital biomass (visible light, LemnaTec 0,90 formula) (px^3)", 
											   "height (px)", 
											   "width (px)", 
											   "side area (px)", 
											   "top area (px)")	
				
				boxOptions= list(daysOfBoxplotNeeds=c("phase4"))
				#diagramTypVectorBox = rep.int("boxplot", times=length(descriptorSetBox))
				
				#descriptorSet = c(descriptorSet, descriptorSetBox)
				#descriptorSetName = c(descriptorSetName, descriptorSetNameBox)
				#diagramTypVector = c(diagramTypVector, diagramTypVectorBox)
				
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
												  "top.vis.hue.histogram.ratio.bin.")
												  
										  
				descriptorSetName_boxplotStacked = c("side near-infrared intensities (zoom corrected) (%)", 
													  "side fluorescence color spectra (%)", 
													  "top near-infrared intensities (%)", 
													  "side fluorescence ratio histogramm (%)", 
													  "side near-infrared (zoom corrected) (%)", 
													  "side fluorescence color spectra (zoom corrected) (%)", 
													  "side fluoresence color spectra (%)", 
													  "side visible light color histogramm (%)", 
													  "side visible light ratio histogramm (zoom correcte) (%)", 
													  "top fluorescence color spectra (%)", 
													  "top fluo ratio histogramm (%)", 
													  "NIR top histogramm (%)", 
													  "VIS HUE top ratio histogramm (%)")
				
				descriptorList <- addDesSet(descriptorSet_boxplotStacked, descriptorSetName_boxplotStacked, workingDataSet)
				descriptorSet_boxplotStacked <- descriptorList$desSet
				descriptorSetName_boxplotStacked <- descriptorList$desName
				
				stackedBarOptions = list(typOfGeomBar=c("fill", "stack", "dodge"))
				#diagramTypVector = c(diagramTypVector, "boxplotStacked", "boxplotStacked")
					
				appendix = appendix %exists% args[3]

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

			} else {
				fileName = "error"
			}
		}
		
	} 
	
	secondRun = appendix
	appendix =  FALSE
	
	if (fileName != "error" & (length(descriptorSet_nBoxplot) > 0 || length(descriptorSet_boxplot) > 0 || length(descriptorSet_boxplotStacked) > 0)) {
		time = system.time({
			repeat {					
				if (appendix) 
					print("Generate diagrams for annotation descriptors...")
				else
					print("Generate diagrams for main descriptors...")
				createDiagrams(iniDataSet = workingDataSet, saveFormat = saveFormat, dpi = dpi, isGray = isGray, 
									nBoxDes = descriptorSet_nBoxplot, boxDes = descriptorSet_boxplot, boxStackDes = descriptorSet_boxplotStacked, 
									nBoxDesName = descriptorSetName_nBoxplot, boxDesName = descriptorSetName_boxplot, boxStackDesName = descriptorSetName_boxplotStacked, 
									nBoxOptions= nBoxOptions, boxOptions= boxOptions, stackedBarOptions = stackedBarOptions, 
									treatment = treatment, filterTreatment = filterTreatment, 
									secondTreatment = secondTreatment, filterSecondTreatment = filterSecondTreatment, filterXaxis = filterXaxis, xAxis = xAxis, 
									showResultInR = showResultInR, xAxisName = xAxisName, debug = debug, appendix=appendix)
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
				} else {
					break
				}
			}
			checkIfAllNecessaryFilesAreThere()
		}, TRUE)
		
		print("Processing finished")		
		print(time)
	} else {
		print("No filename or no descriptor!")
		checkIfAllNecessaryFilesAreThere()
	}
	
	if(debug) {
		print(warnings())
	}
}

createDiagrams <- function(iniDataSet, saveFormat="pdf", dpi="90", isGray="false", 
		nBoxDes = NULL, boxDes = NULL, boxStackDes = NULL, 
		#imageFileNames_nBoxplots = NULL, imageFileNames_Boxplots = NULL, imageFileNames_StackedPlots = NULL, 
		nBoxDesName = NULL, boxDesName = NULL, boxStackDesName = NULL, 
		nBoxOptions= NULL, boxOptions= NULL, stackedBarOptions = NULL, 
		treatment="Treatment", filterTreatment="none", 
		secondTreatment="none", filterSecondTreatment="none", filterXaxis="none", xAxis="Day (Int)", 
		showResultInR=FALSE, xAxisName="none", debug = FALSE, appendix=FALSE, stoppTheCalculation=FALSE) {		
			
	overallList = list(iniDataSet=iniDataSet, saveFormat=saveFormat, dpi=dpi, isGray=isGray, 
						nBoxDes = nBoxDes, boxDes = boxDes, boxStackDes = boxStackDes, 
						imageFileNames_nBoxplots = nBoxDes, imageFileNames_Boxplots = boxDes, imageFileNames_StackedPlots = boxStackDes, 
						nBoxDesName = nBoxDesName, boxDesName = boxDesName, boxStackDesName = boxStackDesName, 
						nBoxOptions= nBoxOptions, boxOptions= boxOptions, stackedBarOptions = stackedBarOptions, 
						treatment=treatment, filterTreatment=filterTreatment, 
						secondTreatment=secondTreatment, filterSecondTreatment=filterSecondTreatment, filterXaxis=filterXaxis, xAxis=xAxis, 
						showResultInR=showResultInR, xAxisName=xAxisName, debug=debug, 
						appendix=appendix, stoppTheCalculation=stoppTheCalculation, 
						overallResult_nBoxDes=data.frame(), overallResult_boxDes=data.frame(), overallResult_boxStackDes=data.frame(), 
						color_nBox = list(), color_box=list(), color_boxStack=list(), user="none", typ="none")	
	
	loadAndInstallPackages(FALSE, FALSE)
	
	overallList$debug %debug% "Start"
	
	overallList = checkTypOfExperiment(overallList)
	overallList = checkUserOfExperiment(overallList)
	overallList = setSomePrintingOptions(overallList)
	overallList = overallChangeName(overallList)
	overallList = changeXAxisName(overallList)
	overallList = overallPreprocessingOfDescriptor(overallList)
	
	if (!overallList$stoppTheCalculation) {
		overallList = preprocessingOfxAxisValue(overallList)
		overallList = preprocessingOfTreatment(overallList)
		overallList = preprocessingOfSecondTreatment(overallList)
		overallList = overallCheckIfDescriptorIsNaOrAllZero(overallList)
		if (!overallList$stoppTheCalculation) {
			overallList = reduceWorkingDataSize(overallList)
			overallList = setDefaultAxisNames(overallList)
			#print(overallList)
			overallList = overallGetResultDataFrame(overallList)
			if (!overallList$stoppTheCalculation) {
				overallList = setColor(overallList) 
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