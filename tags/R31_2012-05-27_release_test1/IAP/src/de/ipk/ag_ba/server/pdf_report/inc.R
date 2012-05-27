debug <- TRUE


getColor <- function(overallColorIndex, overallResult) {
	input = as.vector(unique(overallResult$hist))
	
	color = vector()
	for (n in input) {
		color = c(color, overallColorIndex[[n]])
	}
	return(color)
}



getSpecialRequestDependentOfUserAndTypOfExperiment <- function() {
	requestList = list(
			KN = list(barley = list(boxplot = list(daysOfBoxplotNeedsReplace = c("27", "44", "45")),
									spiderplot = list(daysOfBoxplotNeedsReplace = c("27", "44", "45")))
			)
	)
	return(requestList)
}

"%break%" <- function(typOfBreak, breakValue) {
	# 0 %break% 1 --> stopp the code
	# 1 %break% 10 --> stopp the code for 10 sec
	if(typOfBreak == 0) {
		stop("The code stopps manual by the \"break\" function", call. = FALSE)
	} else {
		ownCat(paste("Script will stopped for ",breakValue, " sec!", sep=""))
		
		Sys.sleep(breakValue)
		ownCat("Break ends!")
		
	}
}

"%debug%" <- function(debug, debugNumber) {
	if (debug) {
		ownCat(paste("DebugBreakPoint: ", debugNumber))
		
	}
}

"%checkEqual%" <- function(treat, seconTreat) {
	if (treat == seconTreat) {
		ownCat("Second filter has the same value as first filter so it set to \"none\"")
		
		return("none")
	} else {
		return(seconTreat)
	}
}

"%errorReport%" <- function(errorDescriptor, typOfError="notExists") {
	#overallList$debug %debug% "%errorReport%"
	if (length(errorDescriptor) > 0) {
		if (tolower(typOfError) == "notexists") {
			ownCat(paste("Descriptor '", errorDescriptor, "' is missing!", sep=""))
			
		} else if (tolower(typOfError) == "notnumericorallzero") {
#			ownCat(paste("the values of the descriptor(s) '", errorDescriptor, "', are all zero or not numeric!", sep=""))
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
	#loadAndInstallPackages(TRUE, FALSE)
	
	#separation = ";"
	ownCat(paste("Read input file", fileName))
	if (file.exists(fileName)) {
		
		preScanForPointOrComma <- scan(file=fileName, what=character(0), nlines=2, sep="\n")
		preScanForPointOrComma <- paste(preScanForPointOrComma[2],",.", sep="")
		allCharacterSeparated <- table(strsplit(toupper(preScanForPointOrComma), '')[[1]])
		
		if(allCharacterSeparated["."] > allCharacterSeparated[","]) {
			
			ownCat("Read input (English number format)...")
			
			return(read.csv(fileName, header=TRUE, sep=separation, fileEncoding="UTF-8")) #encoding="UTF-8"
		} else {
			
			ownCat("Read input (German number format)...")
			
			return(read.csv2(fileName, header=TRUE, sep=separation, fileEncoding="UTF-8")) #, encoding="UTF-8"
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

ownCat <- function(text, endline=TRUE){
	cat(text)
	if(endline) {
		cat("\n")
	}
}

overallOutlierDetection <- function(overallList) {
	overallList$debug %debug% "overallOutlierDetection()"	
	
	
	workingDataSet <- overallList$iniDataSet[,!colnames(overallList$iniDataSet) %in% c(overallList$treatment, overallList$secondTreatment, overallList$xAxis)]
	workingDataSet[is.na(workingDataSet)] <- 0

	
	test <- cbind(overallList$iniDataSet[overallList$xAxis], workingDataSet[,1])
	test2 <- cbind(workingDataSet[,c(1,21,22)])
	aq.plot(test2, alpha=0.1)
	color.plot(test, quan=0.75)
	
	
#	sehr gut
	# create data:
	set.seed(134)
	x <- cbind(rnorm(80), rnorm(80), rnorm(80))
	y <- cbind(rnorm(10, 5, 1), rnorm(10, 5, 1), rnorm(10, 5, 1))
	z <- rbind(x,y)
# execute:
	aq.plot(z, alpha=0.05)
	
	
	###################

	data(humus)
	res <-chisq.plot(log(humus[,c("Co","Cu","Ni")]))
	res <-chisq.plot(z)
	res$outliers # these are the potential outliers
	
	################
	
#	geht nur mit zwei dimensionen
	# create data:
	x <- cbind(rnorm(100), rnorm(100))
	y <- cbind(rnorm(10, 5, 1), rnorm(10, 5, 1))
	z <- rbind(x,y)
# execute:
	color.plot(z, quan=0.75)
	
	################
	
	# create data:
	x <- cbind(rnorm(100), rnorm(100))
	y <- cbind(rnorm(10, 3, 1), rnorm(10, 3, 1))
	z <- rbind(x,y)
# execute:
	dd.plot(z)

	##################

# Geostatistical data:
	data(humus) # Load humus data
	uni.plot(log(humus[, c("As", "Cd", "Co", "Cu", "Mg", "Pb", "Zn")]),symb=TRUE)
}


loadInstallAndUpdatePackages <- function(libraries, install=FALSE, update = FALSE, useDev=FALSE) {	
#	libraries  <- c("Cairo", "RColorBrewer", "data.table", "ggplot2", "psych", "fmsb", "plotrix")	
	repos <- c("http://cran.r-project.org","http://www.rforge.net/")
	#libPath <- ".libPaths()[1]"
	libPath <- Sys.getenv("R_LIBS_USER")
	
	if (install & length(libraries) > 0) {
		ownCat("Check for new packages...")
		
		for(n in repos) {
			
			installedPackages <- names(installed.packages()[, 'Package'])
			availablePackagesOnTheRepos <- available.packages(contriburl= contrib.url(n))[,1]
			ins <- libraries[!libraries %in% installedPackages & libraries %in% availablePackagesOnTheRepos]
		
			if(length(ins) > 0) {
				ownCat(paste("The following packages will be installed: ", ins, sep=""))
				
				install.packages(ins, lib=libPath, repos=n, dependencies = TRUE)
			}
		
		}
		
		if (useDev) {
			#install.packages(c("devtools"), lib=libPath, repos=repos, dependencies = TRUE)
			#dev_mode()
			#install_github("ggplot2")
		}
	}
	
	if(update) {
		#installedOldPackages <- names(old.packages()[, 'Package'])
		ownCat("Check for package updates...")
		
		for(n in repos) {
			update.packages(lib.loc = libPath, checkBuilt=TRUE, ask=FALSE,	repos=n)
		}
	}
	
	if(length(libraries) > 0) {
		for(n in libraries) {
			library(n, character.only = TRUE)
#			library("Cairo") # save images (default image)
#			library("RColorBrewer") #color space
#			library("data.table") #fast grouping
#			library("ggplot2") #plotting system (also save images)
#			library("psych")
#			library("fmsb")	#radarchart
#			library("plotrix") # second radarchart -> radial.plot
		}
	
		if (useDev) {
			library("devtools")
		}	
	}	
}

buildDataSet <- function(workingDataSet, overallResult, colname, index, diagramTyp = "none") {
	if (length(colname) > 0) {
		for (n in 1:length(colname)) {
			if(diagramTyp == "spiderplot") {
				searchVector <- gsub("\\.[0-9]*","",colnames(overallResult)) %in% paste(colname[n], index, sep="")	
			} else {
				searchVector <- paste(colname[n], index, sep="")
			}		
			workingDataSet = cbind(workingDataSet, overallResult[searchVector])
		}	
		return(workingDataSet)
	}
}

reNameSpin <- function(colNameWichMustBind, colNames) {
	
	descriptorListIndex <- strsplit(substr(colNameWichMustBind,nchar(colNames$colOfMean)+1, nchar(colNameWichMustBind)),"\\.")
	
	if(descriptorListIndex[[1]][1] != "" & descriptorListIndex[[1]][2] != "") {
		return(colNames$desNames[[descriptorListIndex[[1]][1]]][descriptorListIndex[[1]][2],])
	} else {
		return(colNameWichMustBind)
	}
}


reNameHist <-  function(colNameWichMustBind) {
	colNameWichMustBind = as.character(colNameWichMustBind)
	positions = which(strsplit(colNameWichMustBind, '')[[1]] == '.')
	colNameWichMustBind = substr(colNameWichMustBind, positions[length(positions)]+1, nchar(colNameWichMustBind))
	
	regExpressionSpezialCharacter = "\\_"
	colNameWichMustBind = gsub(regExpressionSpezialCharacter, "..", colNameWichMustBind)
	
	return(colNameWichMustBind)	
}

reNameColumn <-  function(plotThisValues, columnNameReplace="name", columnNameWhichUsedToReplace="primaerTreatment") {
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
		ownCat("The values of Treatment and SecondTreamt are changed (filter values also)")
		
		return(list(swap(listOfTreat), swap(listOfFilterTreat)))	
	} else {
		return(list(listOfTreat, listOfFilterTreat))
	}
}

checkOfTreatments <- function(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug) {
	debug %debug% "Start of checkOfTreatments()"
#	ownCat(args[5])
#	ownCat(args[6])

	treatment = treatment %exists% args[5]
	secondTreatment = secondTreatment %exists% args[6]
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
					ownCat(paste(k, "set to \"none\""))
					
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
		if (overallList$debug) {ownCat(paste(length(overallList$nBoxDes), "nBoxplots..."));}
		for (n in 1:length(overallList$nBoxDes)) {
			if (!is.na(overallList$nBoxDes[[n]][1])) {
				overallList$nBoxDes[n] = checkIfDescriptorIsNaOrAllZero(overallList$nBoxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$nBoxDes) = c(1:length(overallList$nBoxDes))
	} else {
		ownCat("All values for nBoxplot are 'NA'")
		
	}
	
	if (sum(!is.na(overallList$boxDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxDes), "Boxplots..."))}
		for (n in 1:length(overallList$boxDes)) {
			if (!is.na(overallList$boxDes[[n]][1])) {
				overallList$boxDes[[n]] = checkIfDescriptorIsNaOrAllZero(overallList$boxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$boxDes) = c(1:length(overallList$boxDes))
	} else {
		ownCat("All values for Boxplot are 'NA'")
		
	}

	if (sum(!is.na(overallList$boxStackDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxStackDes), "stacked boxplots..."))}
		for (n in 1:length(overallList$boxStackDes)) {
			if (!is.na(overallList$boxStackDes[[n]][1])) {
				overallList$boxStackDes[[n]] = checkIfDescriptorIsNaOrAllZero(overallList$boxStackDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$boxStackDes) = c(1:length(overallList$boxStackDes))
	} else {
		ownCat("All values for stackedBoxplot are 'NA'")
		
	}

	if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxSpiderDes), "spiderplots..."))}
		for (n in 1:length(overallList$boxSpiderDes)) {
			if (!is.na(overallList$boxSpiderDes[[n]][1])) {
				initDescriptor <- overallList$boxSpiderDes[[n]]
				overallList$boxSpiderDes[[n]] = checkIfDescriptorIsNaOrAllZero(overallList$boxSpiderDes[[n]], overallList$iniDataSet)
				booleanVector <- unlist(initDescriptor) %in% unlist(overallList$boxSpiderDes[[n]])
				overallList$boxSpiderDesName[[n]] = as.data.frame(overallList$boxSpiderDesName[[n]][booleanVector])
				
			}
		}
		names(overallList$boxSpiderDes) = c(1:length(overallList$boxSpiderDes))
		names(overallList$boxSpiderDesName) = c(1:length(overallList$boxSpiderDesName))
	} else {
		ownCat("All values for spider plot are 'NA'")
		
	}
	
	if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
		if (overallList$debug) {ownCat(paste(length(overallList$violinBoxDes), "violinplot..."))}
		for (n in 1:length(overallList$violinBoxDes)) {
			if (!is.na(overallList$violinBoxDes[[n]][1])) {
				overallList$violinBoxDes[n] = checkIfDescriptorIsNaOrAllZero(overallList$violinBoxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$violinBoxDes) = c(1:length(overallList$violinBoxDes))
	} else {
		ownCat("All values for violin plot are 'NA'")
		
	}
	
	
	if ((!sum(!is.na(overallList$boxStackDes)) > 0 && !sum(!is.na(overallList$boxDes)) > 0 && !sum(!is.na(overallList$nBoxDes)) > 0 && !sum(!is.na(overallList$boxSpiderDes)) && !sum(!is.na(overallList$violinBoxDes))) > 0) {
		ownCat("No descriptor set (all descriptors are zero or NA) - the program needs to stop!")
		
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
		if (overallList$debug) {ownCat("line plots..."); }
		overallList$imageFileNames_nBoxplots = changefileName(overallList$imageFileNames_nBoxplots)
		names(overallList$imageFileNames_nBoxplots) = c(1:length(overallList$imageFileNames_nBoxplots))
		
		overallList$nBoxDesName = as.list(overallList$nBoxDesName)
		names(overallList$nBoxDesName) = c(1:length(overallList$nBoxDesName))
	}
	
	if (!is.null(overallList$imageFileNames_Boxplots)) {
		if (overallList$debug) {ownCat("boxplots...");}
		overallList$imageFileNames_Boxplots = changefileName(overallList$imageFileNames_Boxplots)
		names(overallList$imageFileNames_Boxplots) = c(1:length(overallList$imageFileNames_Boxplots))
		
		overallList$boxDesName = as.list(overallList$boxDesName)
		names(overallList$boxDesName) = c(1:length(overallList$boxDesName))
	}
	
	if (!is.null(overallList$imageFileNames_StackedPlots)) {
		if (overallList$debug) {ownCat("stacked boxplots...");}
		overallList$imageFileNames_StackedPlots = changefileName(overallList$imageFileNames_StackedPlots)
		names(overallList$imageFileNames_StackedPlots) = c(1:length(overallList$imageFileNames_StackedPlots))
		
		overallList$boxStackDesName = as.list(overallList$boxStackDesName)
		names(overallList$boxStackDesName) = c(1:length(overallList$boxStackDesName))
	}
	
	if (!is.null(overallList$imageFileNames_SpiderPlots)) {
		if (overallList$debug) {ownCat("spiderplots...");}
		overallList$imageFileNames_SpiderPlots = changefileName(overallList$imageFileNames_SpiderPlots)
		names(overallList$imageFileNames_SpiderPlots) = c(1:length(overallList$imageFileNames_SpiderPlots))
		
		#overallList$boxSpiderDesName = as.list(overallList$boxSpiderDesName)
		#names(overallList$boxSpiderDesName) = c(1:length(overallList$boxSpiderDesName))
	}

	if (!is.null(overallList$imageFileNames_violinPlots) & overallList$isRatio) {
		if (overallList$debug) {ownCat("violinplots...");}
		overallList$imageFileNames_violinPlots = changefileName(overallList$imageFileNames_violinPlots)
		names(overallList$imageFileNames_violinPlots) = c(1:length(overallList$imageFileNames_violinPlots))
		
		overallList$violinBoxDesName = as.list(overallList$violinBoxDesName)
		names(overallList$violinBoxDesName) = c(1:length(overallList$violinBoxDesName))
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
			} else if (n == "spiderplot") {
				return(setOptions(overallList, "spiderplot", "spiderOptions", listOfExtraOptions))
			} else if (n == "violinplot" & overallList$isRatio) {
				return(setOptions(overallList, "violinplot", "violinOptions", listOfExtraOptions))
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

preprocessingOfValues <- function(value, isColValue=FALSE, replaceString=".", isColName=FALSE) {
	if (!is.null(value)) {
		regExpressionCol = "[^[:alnum:]|^_]|[[:space:]|\\^]"
		#regExpressionCol = "[^[:alnum:]]|[[:space:]|\\^]"
		if (isColValue || isColName) {
			value = strsplit(as.character(value), "$", fixed=TRUE)
		}
		
		if(!isColName){
			for (n in 1:length(value)) {
				value[[n]] = gsub(regExpressionCol, replaceString, value[[n]])
			}
		}
	} else {
		return("none")
	}
	return(value)
}

overallPreprocessingOfDescriptor <- function(overallList) {
	overallList$debug %debug% "overallPreprocessingOfDescriptor()"	
	
	if (!is.null(overallList$nBoxDes)) {
		if (overallList$debug) {ownCat("nBoxplot")}
		for (n in 1:length(overallList$nBoxDes)) {
			overallList$nBoxDes[n] = preprocessingOfDescriptor(overallList$nBoxDes[[n]], overallList$iniDataSet)
		}
	} else {
		ownCat("nBoxplot is NULL")
		
	} 
	
	if (!is.null(overallList$boxDes)) {
		if (overallList$debug) {ownCat("boxplot")}
		for (n in 1:length(overallList$boxDes)) {
			overallList$boxDes[n] = preprocessingOfDescriptor(overallList$boxDes[[n]], overallList$iniDataSet)
		}
	} else {
		ownCat("Boxplot is NULL")
		
	} 
	
	if (!is.null(overallList$boxStackDes)) {
		if (overallList$debug) {ownCat("stacked boxplot")}
		for (n in 1:length(overallList$boxStackDes)) {
			overallList$boxStackDes[n] = preprocessingOfDescriptor(overallList$boxStackDes[[n]], overallList$iniDataSet)
		}
	} else {
		ownCat("stackedBoxplot is NULL")
		
	} 

	if (!is.null(overallList$boxSpiderDes)) {
		if (overallList$debug) {ownCat("spider plot")}
		for (n in 1:length(overallList$boxSpiderDes)) {
			initDescriptor <- preprocessingOfValues(overallList$boxSpiderDes[n], isColValue = TRUE)
			overallList$boxSpiderDes[n] = preprocessingOfDescriptor(overallList$boxSpiderDes[[n]], overallList$iniDataSet)
			booleanVector <- initDescriptor[[1]] %in% overallList$boxSpiderDes[n][[1]]
			overallList$boxSpiderDesName[n] = as.data.frame(preprocessingOfValues(overallList$boxSpiderDesName[[n]], isColName=TRUE)[[1]][booleanVector])
		}
	} else {
		ownCat("Spider plot is NULL")
		
	} 

	if (!is.null(overallList$violinBoxDes) & overallList$isRatio) {
		if (overallList$debug) {ownCat("violin plot")}
		for (n in 1:length(overallList$violinBoxDes)) {
			initDescriptor <- preprocessingOfValues(overallList$violinBoxDes[n], isColValue = TRUE)
			overallList$violinBoxDes[n] = preprocessingOfDescriptor(overallList$violinBoxDes[[n]], overallList$iniDataSet)
			booleanVector <- initDescriptor[[1]] %in% overallList$violinBoxDes[n][[1]]
			overallList$violinBoxDesName[n] = as.data.frame(preprocessingOfValues(overallList$violinBoxDesName[[n]], isColName=TRUE)[[1]][booleanVector])
		}
	} else {
		ownCat("Violin plot is NULL")
		
	} 
	
	if ((!sum(!is.na(overallList$boxStackDes)) > 0 && !sum(!is.na(overallList$boxDes)) > 0 && !sum(!is.na(overallList$nBoxDes)) > 0 && !sum(!is.na(overallList$boxSpiderDes)) && !sum(!is.na(overallList$violinBoxDes))) > 0) {
		ownCat("No descriptor set - this run needs to stop!")
		
		overallList$stoppTheCalculation = TRUE
	}
	return(overallList)
}

preprocessingOfDescriptor <- function(descriptorVector, iniDataSet) {
	#overallList$debug %debug% "preprocessingOfDescriptor()"
	descriptorVector = unlist(preprocessingOfValues(descriptorVector, isColValue = TRUE))	#descriptor is value for yAxis
	
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
			ownCat("Set 'filterTreatment' and 'treatment' to 'none'!")
					
		}			
	} else {
		overallList$treatment = "none"
		overallList$filterTreatment = "none"
		ownCat("Set 'filterTreatment' and 'treatment' to 'none'!")
		
	}
	return(overallList)
}

preprocessingOfSecondTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheSecondTreatment()"
	
	if (!is.null(overallList$secondTreatment)) {
		overallList$secondTreatment = preprocessingOfValues(overallList$secondTreatment)

		if (overallList$secondTreatment != "none" & (overallList$secondTreatment %checkIfDescriptorExists% overallList$iniDataSet)) {
			overallList$filterSecondTreatment = getSingelFilter(overallList$filterSecondTreatment, overallList$secondTreatment, overallList$iniDataSet)
			
			if(length(overallList$filterSecondTreatment) == 1) {
				overallList$secondTreatment = "none"
				overallList$filterSecondTreatment = "none"
				ownCat("Set 'filterSecondTreatment' and 'secondTreatment' to 'none' because only one filter is there!")
			}
			
		} else {
			overallList$secondTreatment = "none"
			overallList$filterSecondTreatment = "none"
			ownCat("Set 'filterSecondTreatment' and 'secondTreatment' to 'none'!")
			
		}	
	} else {
		overallList$secondTreatment = "none"
		overallList$filterSecondTreatment = "none"
		ownCat("Set 'filterSecondTreatment' and 'secondTreatment' to 'none'!")
		
	}
	return(overallList)
}

check <- function(value, checkValue=c("none", NA)) {
	if (!is.null(value)) {
		return(value %GetDescriptorAfterCheckIfDescriptorNotExists% checkValue)
		#return(unique(value %GetDescriptorAfterCheckIfDescriptorNotExists% checkValue))
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
	if(overallList$isRatio) {
		overallList$iniDataSet = overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(getVector(overallList$boxSpiderDes)), check(getVector(overallList$violinBoxDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
	} else {
		overallList$iniDataSet = overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(getVector(overallList$boxSpiderDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
	}
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
		ownCat("No Value for the OverallResult-DataFrame - Wrong filter!")
		
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
#####
#mergeDataSet <- iniDataSet
#####
		
	if (length(groupBy) == 0) {
		return(data.frame(name=rep.int(yName, length(mergeDataSet[, 1])), mergeDataSet))
	} else if (length(groupBy) == 1) {
		return(data.frame(name=mergeDataSet[, groupBy], mergeDataSet[, !(colnames(mergeDataSet) %in% groupBy)]))
	} else {		
		#temp = mergeDataSet[, groupBy[2]]
		temp = mergeDataSet[, groupBy[2]]
		if(length(groupBy) > 2) {
			reduceGroupBy <- groupBy[3:length(groupBy)]
			for (h in seq(along=reduceGroupBy)) {
				temp = paste(temp, mergeDataSet[, reduceGroupBy[h]], sep = "/") #  #/#
			}
		}
		
		return(data.frame(name=temp, primaerTreatment= mergeDataSet[, groupBy[1]], mergeDataSet[, mergeDataSet %allColnamesWithoutThisOnes% groupBy]))
	}	
}

getToPlottedDays <- function(xAxis, changes=NULL) {
##########
#xAxis <- xAxisValue
##########
	
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
##############
#xAxisName <- overallList$xAxisName
#xAxisValue <- overallResult$xAxis
##############
	
	
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

	if(!calculateNothing) {	
			groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
			colNames = list(colOfXaxis="xAxis", colOfMean="mean", colOfSD="se", colName="name", xAxis=overallList$xAxis)
			booleanVectorList = buildList(overallList, colNames$colOfXaxis)
			columnsStandard = c(check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment))
	
			if (sum(!is.na(overallList$nBoxDes)) > 0) {
				if (overallList$debug) {ownCat("nBoxplot")}
				columns = c(columnsStandard, check(getVector(overallList$nBoxDes)))
				overallList$overallResult_nBoxDes = getResultDataFrame("nboxplot", overallList$nBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
			} else {
				ownCat("All values for nBoxplot are 'NA'")
			}
			
			if (sum(!is.na(overallList$boxDes)) > 0) {
				if (overallList$debug) {ownCat("Boxplot")}
				colNames$colOfMean = "value"
				columns = c(columnsStandard, check(getVector(overallList$boxDes)))
				overallList$overallResult_boxDes = getResultDataFrame("boxplot", overallList$boxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
			} else {
				ownCat("All values for Boxplot are 'NA'")
			}
			
			if (sum(!is.na(overallList$boxStackDes)) > 0) {
				if (overallList$debug) {ownCat("stackedBoxplot")}
				colNames$colOfMean = check(getVector(overallList$boxStackDes))
				colNames$colOfXaxis = overallList$xAxis
				columns = c(columnsStandard, check(getVector(overallList$boxStackDes)))
				overallList$overallResult_boxStackDes = getResultDataFrame("boxplotStacked", overallList$boxStackDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
			} else {
				ownCat("All values for stackedBoxplot are 'NA'")
			}
		
			if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
				if (overallList$debug) {ownCat("spider plot")}
				#colNames$colOfMean = check(getVector(overallList$boxSpiderDes))
				colNames$colOfMean = "value"
				colNames$colOfXaxis = overallList$xAxis
				colNames$desNames = overallList$boxSpiderDesName
				columns = c(columnsStandard, check(getVector(overallList$boxSpiderDes)))
				overallList$overallResult_boxSpiderDes = getResultDataFrame("spiderplot", overallList$boxSpiderDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
			} else {
				ownCat("All values for spider plot are 'NA'")
			}

			if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
				if (overallList$debug) {ownCat("violin plot")}
				colNames$colOfMean = "mean"
				colNames$colOfXaxis = "xAxis"
				columns = c(columnsStandard, check(getVector(overallList$violinBoxDes)))
				overallList$overallResult_violinBoxDes = getResultDataFrame("violinplot", overallList$violinBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug)
			}
			
			
			if (is.null(overallList$boxStackDes) && is.null(overallList$boxDes) && is.null(overallList$nBoxDes) && is.null(overallList$boxSpiderDes) && is.null(overallList$violinBoxDes)) {
				ownCat("No descriptor set - this run needs to stop!")
				overallList$stoppTheCalculation = TRUE
			}
	}
	return(overallList)
}

getPlotNumber <- function(colNameWichMustBind, descriptorList, diagramTyp) {
	
	if(diagramTyp == "boxplotStacked") {
	
		for (n in names(descriptorList)) {
			if (colNameWichMustBind %in% as.vector(unlist(descriptorList[[n]]))) {
				return(n)
			}
		}
		return(-1)
	} else if(diagramTyp == "spiderplot") {
		return(strsplit(substring(colNameWichMustBind,nchar("value")+1),"\\.")[[1]][1])
	}
}


getResultDataFrame <- function(diagramTyp, descriptorList, iniDataSet, groupBy, colNames, booleanVectorList, debug) {	
#############################
#	diagramTyp = "spiderplot"
#	descriptorList = overallList$boxSpiderDes
#	iniDataSet = overallList$iniDataSet[columns]
#	debug = overallList$debug
#########################	
#	diagramTyp = "boxplotStacked"
#	descriptorList = overallList$boxStackDes
#	iniDataSet = overallList$iniDataSet[columns]
#	debug = overallList$debug	
#########################
#	diagramTyp = "nboxplot"
#	descriptorList = overallList$nBoxDes
#	iniDataSet = overallList$iniDataSet[columns]
#	debug = overallList$debug
#########################
#	diagramTyp = "violinplot"
#	descriptorList = overallList$violinBoxDes
#	iniDataSet = overallList$iniDataSet[columns]
#	debug = overallList$debug
#########################



	debug %debug% "getResultDataFrame()"
	
	descriptor = getVector(descriptorList)

	if(diagramTyp == "spiderplot") {
		descriptorName <- character()
		for(n in 1:length(descriptorList)){
			lengthVector <- length(descriptorList[[n]][,1])
			descriptorName <- c(descriptorName, paste(rep.int(n, lengthVector),c(1:lengthVector), sep="."))		
		}
	} else {	
		descriptorName = seq(1:length(descriptor))
	}
	
	descriptorName = descriptorName[!is.na(descriptor)]
	descriptor = descriptor[!is.na(descriptor)]

	
	if (diagramTyp != "boxplot") {
		groupedDataFrame = data.table(iniDataSet)
		#key(groupedDataFrame) = c(groupBy, colNames$xAxis)
		setkeyv(groupedDataFrame,c(groupBy, colNames$xAxis))
	}
	
	if (diagramTyp == "boxplot") {
		groupedDataFrameMean = iniDataSet[groupBy[1]]
		
		groupByReduce = groupBy[groupBy!=groupBy[1]]
		for (n in c(groupByReduce, colNames$xAxis, descriptor)) {
			groupedDataFrameMean = cbind(groupedDataFrameMean, iniDataSet[n])
		}		
	} else {
		groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(.SD, mean, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		#groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(colnames(groupedDataFrame), mean, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
	}
	
	if (diagramTyp == "nboxplot" || diagramTyp == "boxplot" || diagramTyp == "spiderplot" || diagramTyp == "violinplot") {
		#colNamesOfTheRest = paste(colNames$colOfMean, seq(1:length(descriptor)), sep="")	
		colNamesOfTheRest = paste(colNames$colOfMean, descriptorName, sep="")	
	} else {
		colNamesOfTheRest = groupedDataFrameMean %allColnamesWithoutThisOnes% c(groupBy, colNames$xAxis)
	}

	colnames(groupedDataFrameMean) = c(groupBy, colNames$colOfXaxis, colNamesOfTheRest)
	
	if (diagramTyp == "nboxplot") {
		groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(.SD, sd, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		#groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(colnames(groupedDataFrame), sd, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		colnames(groupedDataFrameSD) = c(groupBy, colNames$colOfXaxis, paste(colNames$colOfSD, descriptorName, sep=""))
	}
	
	booleanVector = getBooleanVectorForFilterValues(groupedDataFrameMean, booleanVectorList)
	
	if (diagramTyp == "nboxplot") {
		iniDataSet = merge(sort=FALSE, groupedDataFrameMean[booleanVector, ], groupedDataFrameSD[booleanVector, ], by = c(groupBy, colNames$colOfXaxis))
		overallResult = buildRowName(iniDataSet, groupBy)
		
	} else	if (diagramTyp == "boxplot" || diagramTyp == "violinplot") {
		#|| diagramTyp == "spiderplot"
		iniDataSet = groupedDataFrameMean[booleanVector, ]
		overallResult = buildRowName(iniDataSet, groupBy)
	} else if (diagramTyp == "spiderplot") {
		iniDataSet = groupedDataFrameMean[booleanVector, ]
		buildRowNameDataSet = buildRowName(iniDataSet, groupBy)
		temp = data.frame()

		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, "primaerTreatment")) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList, diagramTyp)
			
			colNameWichMustBindReNamed <- reNameSpin(colNameWichMustBind, colNames)
		
			if (is.null(buildRowNameDataSet$primaerTreatment)) {	
				temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot=plot))			
			} else {
				temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), primaerTreatment=buildRowNameDataSet[, "primaerTreatment"], values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot = plot))			
			}
		}
		overallResult = temp
		
	} else {
		iniDataSet <- groupedDataFrameMean[booleanVector, ]	
		buildRowNameDataSet <- buildRowName(iniDataSet, groupBy)
		temp = data.frame()
		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, "primaerTreatment")) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList, diagramTyp)
		
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
	intervalSat20 = 1 #rep.int(c(0.8, 1.0), 10)
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
######################
#diagramTyp <- "spiderplot"
#descriptorList <- overallList$boxSpiderDes
#overallResult <- overallList$overallResult_boxSpiderDes
#isGray <- overallList$isGray
######################	
		
	if (!as.logical(isGray)) {
		#colorVector = c(brewer.pal(8, "Set1"))
		colorVector = c(brewer.pal(7, "Dark2"))
		#colorVector = c(brewer.pal(11, "Spectral")) # sometimes very pale colors
	} else {
		colorVector = c(brewer.pal(9, "Greys"))
	}
	
	colorList = list()
	if (diagramTyp == "nboxplot" || diagramTyp == "boxplot" || diagramTyp == "violinplot") {
		for (n in names(descriptorList)) {
			#if (!is.na(descriptorList[[n]])) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
			} else {
				#ownCat("All values are 'NA'")
			}
		}
	} else if (diagramTyp == "spiderplot") {
		for (n in names(descriptorList)) {
			#if (!is.na(descriptorList[[n]])) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				#colorList[[n]] = colorRampPalette(colorVector)(length(descriptorList[[n]][,1]))
				#colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
				if ("primaerTreatment" %in% colnames(overallResult)) {	
					colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$primaerTreatment)))	
				} else {
					colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
				}
				##################### Anpassen huier werden noch zuviel Farbwerte ausgelesen ###############
			} else {
				#ownCat("All values are 'NA'")
			}
		}
	} else {
		for (n in names(descriptorList)) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				colorList[[n]] = setColorListHist(descriptorList[n])
			} else {
				#ownCat("All values are 'NA'")
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
	overallList$color_spider = setColorList("spiderplot", overallList$boxSpiderDes, overallList$overallResult_boxSpiderDes, overallList$isGray)
	#overallList$color_violin = setColorList("violinplot", overallList$violinBoxDes, overallList$overallResult_violinBoxDes, overallList$isGray)
	return(overallList)
}

normalizeToHundredPercent =  function(whichRows, overallResult) {
	return(t(apply(overallResult[whichRows, ], 1, function(x, y) {(100*x)/y}, y=colSums(overallResult[whichRows, ]))))
}

renameYForSubsection <- function(label) {
	
	label <- gsub("\\\\% ","percent", label)
	label <- gsub("\\^2","$^2$", label)
	
	return(label)
}

writeLatexFile <- function(fileNameLatexFile, fileNameImageFile="", o="", ylabel="", subsectionDepth=1) { #insertSubsections=FALSE,
	fileNameImageFile = preprocessingOfValues(fileNameImageFile, FALSE, "_")
	fileNameLatexFile = preprocessingOfValues(fileNameLatexFile, FALSE, "_")
	o = gsub('[[:punct:]]', "_", o)
	
	latexText <- ""
	if(nchar(ylabel) > 0) {
		ylabel <- renameYForSubsection(ylabel)
		if(subsectionDepth == 1) {
			latexText = paste(latexText, "\\subsection{",ylabel,"}\n", sep="" )
		} else if(subsectionDepth == 2) {
			latexText = paste(latexText, "\\subsubsection{",ylabel,"}\n", sep="" )
		} else if(subsectionDepth == 3) {
			latexText = paste(latexText, "\\subsubsubsection{",ylabel,"}\n", sep="" )
		}
	}
	
#	if(insertSubsections & nchar(ylabel) > 0) {
#		ylabel <- renameYForAppendix(ylabel)
#		latexText = paste(latexText, "\\subsection{",ylabel,"}\n", sep="" )
#	}
	latexText = paste(latexText,
					 "\\loadImage{", 
					   ifelse(fileNameImageFile == "", fileNameLatexFile, fileNameImageFile), 
					  #ifelse(o == "", "", paste("_", o , sep="")), 
					   ifelse(o == "", "", o), 
					   ".pdf}", sep="")
	
	write(x=latexText, append=TRUE, file=paste(fileNameLatexFile, "tex", sep="."))
}


writeLatexTable <- function(fileNameLatexFile, columnName=NULL, value=NULL, columnWidth=NULL) {
	latexText <- ""

	if(length(columnName) > 0) {
		#latexText <- "\\begin{tabular}{|"
		latexText <- "\\begin{longtable}{|"
		
		for(n in 1:length(columnName)) {
			latexText <- paste(latexText, "p{",columnWidth[n],"}|", sep="")
		}
		latexText <- paste(latexText, "}", sep="")
		
		#This is the header for the first page of the table... --> endfirsthead
		#This is the header for the remaining page(s) of the table... --> endhead
		for(n in 1:2) {
			
			latexText <- paste(latexText, " \\hline ", sep="")
			for(n in 1:length(columnName)) {
				latexText <- paste(latexText, "{\\textbf{",
						parseString2Latex(renameFilterOutput(as.character(columnName[n]))),
					"}}", sep="")
				if(n != length(columnName)) {
					latexText <- paste(latexText, "& ", sep=" ")
				}
			}
			latexText <- paste(latexText,
								"\\tabularnewline",
								"\\hline",
								"\\hline", sep=" ")
			if(n == 1) {
				latexText <- paste(latexText, "\\endfirsthead", sep=" ")
			} else {
				latexText <- paste(latexText, "\\endhead", sep=" ")
			}
		}
		
		#This is the footer for all pages except the last page of the table...		
		latexText <- paste(latexText, "\\multicolumn{", length(columnName), "}{l}{{Continued on Next Page\\ldots}} ",
							"\\tabularnewline ",
							"\\endfoot ", sep="")
		
		#This is the footer for the last page of the table...
		latexText <- paste(latexText,"\\hline \\hline \\endlastfoot", sep=" ")
		
	} else if(length(value) > 0){
		if(!is.null(value)) {
			for(n in 1:length(value)) {
				latexText <- paste(latexText, parseString2Latex(renameFilterOutput(as.character(value[n]))))
				if(n != length(value)) {
					latexText <- paste(latexText, " &", sep="")
				}
			}
			latexText <- paste(latexText, "\\tabularnewline \\hline")
		}
	} else {
		latexText <- paste(latexText, 
#						"\\hline",
#						"\\hline",
#						"\\end{tabular}", sep=" ")
						"\\end{longtable}", sep=" ")
	}
	
	if(latexText != "") {
		write(x=latexText, append=TRUE, file=paste(fileNameLatexFile, "tex", sep="."))
	}	
}


saveImageFile <- function(overallList, plot, filename, extraString="") {
	filename = preprocessingOfValues(paste(filename, extraString, sep=""), FALSE, replaceString = "_")	
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
####################
#debug <- overallList$debug
#diagramTyp <- "nboxplot"
#####################
	
	
	debug %debug% "reduceWholeOverallResultToOneValue()"
	
	if (diagramTyp == "boxplotstacked" || diagramTyp == "spiderplot") {
		workingDataSet = tempOverallResult[tempOverallResult$plot == imagesIndex, ]
		workingDataSet$hist = factor(workingDataSet$hist, unique(workingDataSet$hist))
	} else {
		colNames = vector()
		if (diagramTyp == "nboxplot" || diagramTyp == "barplot") {
			colNames = c("mean", "se")
		} else if (diagramTyp == "boxplot") {
			colNames = c("value")
		} else if (diagramTyp == "violinplot") {
			colNames = c("mean")
		}
		
		if ("primaerTreatment" %in% colnames(tempOverallResult)) {
			standardColumnName = c("name", "primaerTreatment", "xAxis")
		} else {
			standardColumnName = c("name", "xAxis")
		}

		if (sum(!(colNames %in% colnames(tempOverallResult)))>0) {

			workingDataSet = buildDataSet(tempOverallResult[, standardColumnName], tempOverallResult, colNames, imagesIndex, diagramTyp)
			lengthOfNewColumns <- length(colnames(workingDataSet[,-c(1:length(standardColumnName))]))
			
#			if(diagramTyp == "spiderplot") {
#				if(lengthOfNewColumns > 1) {
#					colnames(workingDataSet) = c(standardColumnName, paste(rep.int(colNames,lengthOfNewColumns),1:lengthOfNewColumns, sep=""))
#				}else {
#					colnames(workingDataSet) = c(standardColumnName, colNames)
#				}
#			} else {
				colnames(workingDataSet) = c(standardColumnName, colNames)
#			}
		
#			if(lengthOfNewColumns > 1) {
#				colnames(workingDataSet) = c(standardColumnName, paste(rep.int(colNames,lengthOfNewColumns),1:lengthOfNewColumns, sep=""))
#			} else {
#				colnames(workingDataSet) = c(standardColumnName, colNames)
#			}
			
		} else {
			workingDataSet = tempOverallResult
		}
	}
	return(workingDataSet)	
}

newTreatmentNameFunction <- function(seq, n) {
	numberCharAfterSeparate <- 8
	if(nchar(n) > (numberCharAfterSeparate+4)) {
		newTreatmentName <- paste(seq, ".) ", substr(n,1,numberCharAfterSeparate), " ...", sep="")
	} else {
		newTreatmentName <- paste(seq, ".) ", n, sep="")
	}
	return(newTreatmentName)
}


renameOfTheTreatments <- function(overallList) {
	overallList$debug %debug% "renameOfTheTreatments()"
	
	if(!overallList$appendix) {
		
		#newTreatmentName <- character()
		columnName <- c("Short name", "Full Name")
		
		if(overallList$filterTreatment[1] != "none") {
			seq <- 0;
			FileName <- "conditionsFirstFilter"
			writeLatexTable(FileName, columnName, columnWidth=c("3cm","13cm"))
			for(n in overallList$filterTreatment) {
				seq <- seq+1
				overallList$filterTreatmentRename[[n]] <- newTreatmentNameFunction(seq, n)
				writeLatexTable(FileName, value=c(overallList$filterTreatmentRename[[n]],n))
			}
			writeLatexTable(FileName)
		}
	
		if(overallList$filterSecondTreatment[1] != "none") {
			seq <- 0
			FileName <- "conditionsSecondFilter"
			writeLatexTable(FileName, columnName, columnWidth=c("3cm","13cm"))
			for(n in overallList$filterSecondTreatment) {
				seq <- seq+1
				overallList$secondFilterTreatmentRename[[n]] <- newTreatmentNameFunction(letters[seq], n)
				writeLatexTable(FileName, value=c(overallList$secondFilterTreatmentRename[[n]],n))
			}
			writeLatexTable(FileName)
		}
	}

	return(overallList)
}


replaceTreatmentNamesOverall <- function(overallList, overallResult) {
	overallList$debug %debug% "replaceTreatmentNamesOverall()"

	if ("primaerTreatment" %in% colnames(overallResult)) {				
		overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, onlySecondTreatment = TRUE)
		overallResult$primaerTreatment <- replaceTreatmentNames(overallList, overallResult$primaerTreatment, onlyFirstTreatment = TRUE)
	} else {
		overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, onlyFirstTreatment = TRUE)
	}
	
	return(overallResult)
}


replaceTreatmentNames <- function(overallList, columnWhichShouldReplace, onlyFirstTreatment=FALSE, onlySecondTreatment=FALSE) {
##########
#columnWhichShouldReplace <- overallResult$name
#onlyFirstTreatment <- TRUE
#onlySecondTreatment <- TRUE
##########
	
	
	overallList$debug %debug% "replaceTreatmentNames()"
	
	columnWhichShouldReplace <- as.character(columnWhichShouldReplace)
	
	if(overallList$filterSecondTreatment[1] != "none" & !onlyFirstTreatment & !onlySecondTreatment) {
		for(n in overallList$filterTreatment) {
			for(k in overallList$filterSecondTreatment) {
				columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==paste(n,"/",k, sep=""), paste(overallList$filterTreatmentRename[[n]],"/", overallList$secondFilterTreatmentRename[[k]], sep=""))
			}
		} 
	} 
	
	if(overallList$filterTreatment[1] != "none" & onlyFirstTreatment) {
		for(n in overallList$filterTreatment) {
			columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==n, overallList$filterTreatmentRename[[n]])
		}
	}
	
	if(overallList$filterSecondTreatment[1] != "none" & onlySecondTreatment) {
		for(n in overallList$filterSecondTreatment) {
			columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==n, overallList$secondFilterTreatmentRename[[n]])
		}
	}
	#ownCat(unique(columnWhichShouldReplace))
	return(as.factor(columnWhichShouldReplace))
}

createOuputOverview <- function(typ, actualImage, maxImage, imageName) {
	ownCat(paste("Create ", typ, " ", actualImage, "/", maxImage, ": '",imageName, "'", sep=""))
	
}

parseString2Latex <- function(text) {

	##text <- gsub("\\", "\\textbackslash ", text)
	text <- gsub("{", "\\{ ", text, fixed=TRUE)
	text <- gsub("}", "\\} ", text, fixed=TRUE)
	text <- gsub("ä", "{\\\"a}", text, fixed=TRUE)
	text <- gsub("ö", "{\\\"o}", text, fixed=TRUE)
	text <- gsub("ü", "{\\\"u}", text, fixed=TRUE)
	text <- gsub("ß", "{\\ss}", text, fixed=TRUE)
	text <- gsub("_", "{\\_}", text, fixed=TRUE)
	text <- gsub("<", "\\textless " , text, fixed=TRUE)
	text <- gsub(">", "\\textgreater ", text, fixed=TRUE)
	text <- gsub("§", "\\S ", text, fixed=TRUE)
	text <- gsub("$", "\\$ ", text, fixed=TRUE)
	text <- gsub("&", "\\& ", text, fixed=TRUE)
	text <- gsub("#", "\\# ", text, fixed=TRUE)
	
	text <- gsub("%", "\\% ", text, fixed=TRUE)
	text <- gsub("~", "\\textasciitilde ", text, fixed=TRUE)
	text <- gsub("€", "\\texteuro ", text, fixed=TRUE)

	return(text)
}

renameFilterOutput <- function(text) {
	
	text <- gsub("=",": ",text, fixed = TRUE)
	text <- gsub("/", ", ", text, fixed = TRUE)
	text <- gsub("(", " (", text, fixed = TRUE)
	text <- gsub("  ", " ", text, fixed = TRUE)
	
	return(text)
}

renameY <- function(label) {
	
	if(length(grep("\\.\\.",label, ignore.case=TRUE)) > 0){
		label <- sub("\\.\\.", " (",label)
		label <- paste(substring(label,1,nchar(label)-1),")",sep="")
	}
	
	label <- sub("mm\\.2","mm^2",label)	
	label <- sub("percent", "(%)", label)
	label <- sub("pixels", "(px)", label)
	label <- sub("(c p)", "(c/p)", label)
	label <- gsub("_", "-", label)
	label <- sub("(relative pix)", "(relative/px)", label)
	
	label <- gsub("\\."," ",label)
	label <- gsub("\\(\\(","(", label)
	label <- gsub("\\)\\)",")", label)
	return(label)		
}

writeTheData  <- function(overallList, plot, fileName, extraString, writeLatexFileFirstValue="", writeLatexFileSecondValue="", subSectionTitel="", makeOverallImage=FALSE, isAppendix=FALSE, subsectionDepth=1) {
	
	overallList$debug %debug% "writeTheData()"		

	if(subSectionTitel != "") {
		subSectionTitel <- parseString2Latex(subSectionTitel)
	}

	saveImageFile(overallList, plot, fileName, extraString)
	if (makeOverallImage) {
		if(subSectionTitel != "") {
			writeLatexFile(writeLatexFileFirstValue, writeLatexFileSecondValue, ylabel=subSectionTitel, subsectionDepth=subsectionDepth)	
		} else {
			writeLatexFile(writeLatexFileFirstValue, writeLatexFileSecondValue)
		}
	} 
	
#	else {
#		writeLatexFile(fileName, writeLatexFileSecondValue)	
#	}
	
	if(isAppendix) {
		if(subSectionTitel != "") {
			writeLatexFile("appendixImage", fileName, extraString, ylabel=subSectionTitel, subsectionDepth=subsectionDepth)
		} else {
			writeLatexFile("appendixImage", fileName, extraString)
		}
	}
}




plotStackedImage <- function(overallList, overallResult, title = "", makeOverallImage = FALSE, legende=TRUE, minor_breaks=TRUE, overallColor, overallDesName, imagesIndex, overallFileName) {
	overallList$debug %debug% "plotStackedImage()"	
	if (length(overallResult[, 1]) > 0) {

		if (length(overallList$stackedBarOptions$typOfGeomBar) == 0) {
			overallList$stackedBarOptions$typOfGeomBar = c("fill")
		}
		
		if ("primaerTreatment" %in% colnames(overallResult)) {
			overallResult$primaerTreatment <-  replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
		} else {
			overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name, TRUE)
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
					name = sub("(zoom corrected) ", "", overallDesName[[imagesIndex]])
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
							#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
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
			
			subtitle <- ""
			if(positionType == overallList$stackedBarOptions$typOfGeomBar[1] || length(overallList$stackedBarOptions$typOfGeomBar) == 1) {
				subtitle <- title
			}
			
			
			writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste("overall", title, positionType, sep=""), paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep=""), paste(overallFileName[[imagesIndex]], "overall", title, positionType, sep=""), subtitle, makeOverallImage,subsectionDepth=2)
			
#			saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste("overall", title, positionType, sep=""))
#			if (makeOverallImage) {
#				if(title != "") {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep=""), paste(overallFileName[[imagesIndex]], "overall", title, positionType, sep=""), TRUE, title)	
#				} else {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep=""), paste(overallFileName[[imagesIndex]], "overall", title, positionType, sep=""))
#				}
#			} else {
#				writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], "overall", positionType, title, sep="_"))	
#			}			
		}
	}
}



libraries  <- c("Cairo", "RColorBrewer", "data.table", "ggplot2", "fmsb", "methods", "grid", "snow", "pvclust") #, "mvoutlier")
loadInstallAndUpdatePackages(libraries, FALSE, FALSE, FALSE)
