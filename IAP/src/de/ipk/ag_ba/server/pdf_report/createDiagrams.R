# Author: Entzian, Klukas
###############################################################################

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

replaceTreatmentNames <- function(overallList, columnWhichShouldReplace, onlyFirstTreatment=FALSE, onlySecondTreatment=FALSE) {
##########
#columnWhichShouldReplace <- overallResult$name
#onlyFirstTreatment <- FALSE
#onlyFirstTreatment <- FALSE
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
	} else if(overallList$filterTreatment[1] != "none" & onlyFirstTreatment) {
		for(n in overallList$filterTreatment) {
			columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==n, overallList$filterTreatmentRename[[n]])
		}
	} else if(overallList$filterSecondTreatment[1] != "none" & onlySecondTreatment) {
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

makeLinearDiagram <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, diagramTypSave="nboxplot") {
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
	
	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]])) {
			ylabelForAppendix <- ""
			createOuputOverview("line plot", imagesIndex, length(names(overallDescriptor)),  overallDesName[[imagesIndex]])
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, "nboxplot")
			overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
			overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0

			overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name)

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
									#legend.key.size = unit(1.5, "lines"), 
									legend.key = theme_blank(), # switch off the rectangle around symbols in the legend
									panel.border = theme_rect(colour="Grey", size=0.1)
							)
					
					if (length(overallColor[[imagesIndex]]) > 18 & length(overallColor[[imagesIndex]]) < 31) {
						plot = plot + opts(legend.text = theme_text(size=6)
											#,legend.key.size = unit(0.7, "lines")
											)
					} else if(length(overallColor[[imagesIndex]]) >= 31) {
						plot = plot + opts(legend.text = theme_text(size=4)
											#,legend.key.size = unit(0.4, "lines")
						)
					} else {
						plot = plot + opts(legend.text = theme_text(size=11))
					}
					
					#Überlegen ob das sinn macht!!
					if(length(unique(overallResult$name)) > 18) {
						if ("primaerTreatment" %in% colnames(overallResult)) {				
							plot = plot + facet_wrap(~ primaerTreatment)
						} else {
							plot = plot + facet_wrap(~ name)
						} 
					}
					
				
								
					#ownCat(plot)
		
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
}

getColor <- function(overallColorIndex, overallResult) {
	input = as.vector(unique(overallResult$hist))
	
	color = vector()
	for (n in input) {
		color = c(color, overallColorIndex[[n]])
	}
	return(color)
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

PreWorkForMakeBigOverallImage <- function(overallResult, overallDescriptor, overallColor, overallDesName, overallFileName, overallList, imagesIndex) {
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
					
					nRowCrowList <- calculateLegendRowAndColNumber(unique(overallResult$name))	
					plot <-  plot + guides(col=guide_legend(nrow=nRowCrowList$nrow, ncol=nRowCrowList$ncol, byrow=T)) 
					
					nRowCrowList <- calculateLegendRowAndColNumber(unique(overallResult$hist))	
					plot <-  plot + guides(shape=guide_legend(nrow=nRowCrowList$nrow, ncol=nRowCrowList$ncol, byrow=T))
					
				
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

calculateLegendRowAndColNumber <- function(legendText) {
	
	maxLengthOfSet <- max(nchar(as.character(legendText)))
	if(maxLengthOfSet >= 75) {
		return(list(nrow=length(legendText), ncol=NULL))
	} else if (maxLengthOfSet >= 25 & maxLengthOfSet < 75) {
		return(list(nrow=NULL, ncol=2))
	} else if (maxLengthOfSet > 10 & maxLengthOfSet < 25) {
		return(list(nrow=NULL, ncol=4))
	} else {
		return(list(nrow=NULL, ncol=floor(150/maxLengthOfSet)))
	}
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
				makeLinearDiagram(overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$color_nBox, overallDesName=overallList$nBoxDesName, overallList$imageFileNames_nBoxplots , overallList)
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
				makeBoxplotStackedDiagram(overallList$overallResult_boxStackDes, overallList$boxStackDes, overallList$color_boxStack, overallDesName=overallList$boxStackDesName, overallList$imageFileNames_StackedPlots, overallList)
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
	libraries  <- c("Cairo", "RColorBrewer", "data.table", "ggplot2", "fmsb", "methods") #, "mvoutlier")
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
		
		treatment <- "Treatment"
		filterTreatment <- "dry / normal"
		
		secondTreatment <- "Species"
		filterSecondTreatment  <- "none"
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
startOptions("report", FALSE)
rm(list=ls(all=TRUE))