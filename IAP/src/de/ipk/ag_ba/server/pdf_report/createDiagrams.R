# Author: Entzian, Klukas
###############################################################################

cat(paste("used R-Version: ", sessionInfo()$R.version$major, ".", sessionInfo()$R.version$minor, "\n", sep=""))

# multi threaded (4, ba-09: 48sec)
# not threaded   (ba-09:    33sec)

threaded <- FALSE
innerThreaded = FALSE
cpuCNT <- 2
cpuAutoDetected <- TRUE
debug <- TRUE

########## Constants ###########

## plot typs
NBOX.PLOT <- "nboxplot"
BOX.PLOT <- "boxplot"
STACKBOX.PLOT <- "boxplotstacked"
SPIDER.PLOT <- "spiderplot"
VIOLIN.PLOT <- "violinplot"
BAR.PLOT <- "barplot"
LINERANGE.PLOT <- "lineRangePlot"

## colum names
NAME <- "name"
PRIMAER.TREATMENT <- "primaerTreatment"

## fail values
NONE <- "none"
NONE.TREATMENT <- "noneTreatment"

## errors
NOT.NUMERIC.OR.ALL.ZERO <- "NotNumericOrAllZero"
NOT.EXISTS <- "notExists"

##path
PLOTTING.LISTS <- "plottingLists"

############## Flags for debugging ####################

calculateNothing <- FALSE
calculateOnlyNBox <- FALSE
calculateOnlyViolin <- FALSE
calculateOnlyStacked <- FALSE
calculateOnlySpider <- FALSE
calculateOnlyBoxplot <- FALSE




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
	if (typOfBreak == 0) {
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
		
		return(NONE)
	} else {
		return(seconTreat)
	}
}

"%errorReport%" <- function(errorDescriptor, typOfError = NOT.EXISTS) {
	#overallList$debug %debug% "%errorReport%"
	if (length(errorDescriptor) > 0) {
		if (typOfError == NOT.EXISTS) {
			ownCat(paste("Descriptor '", errorDescriptor, "' is missing!", sep=""))
			
		} else if (typOfError == NOT.NUMERIC.OR.ALL.ZERO) {
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
		
		if (allCharacterSeparated["."] > allCharacterSeparated[","]) {
			
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
		if (k == NONE) {
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
	#print(text)
	
#	while (class(text) == "list") {
#		text <- unlist(text)
#	}
	
	if (sfParallel()) {
		sfCat(text, master=TRUE)
		if (endline)
			sfCat("\n", master=TRUE)
	} else {
		cat(unlist(text))
		if (endline)
			cat("\n")
	}
	
#tryCatch({	
#	if (sfParallel()) {
#		sfCat(text, master=TRUE)
#		if (endline)
#			sfCat("\n", master=TRUE)
#	} else {
#		cat(unlist(text))
#		if (endline)
#			cat("\n")
#	}
#	},
#	error = function(e) {
#		print("aaaaaaaaaaaaaaaaaaaaaaaaaaaa")
#		print(text)
#		print("hhhhhhhhhhhhhhhhhhhhhhhhhhhh")})
}

loadFiles <- function(path, pattern = "\\.[Rr]$", trace = TRUE) {
	for (nm in list.files(path, pattern = pattern)) {
		if(trace) ownCat(paste("load", nm, sep=""))           
		source(file.path(path, nm))
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
		
			if (length(ins) > 0) {
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
	
	if (update) {
		#installedOldPackages <- names(old.packages()[, 'Package'])
		ownCat("Check for package updates...")
		
		for(n in repos) {
			update.packages(lib.loc = libPath, checkBuilt=TRUE, ask=FALSE,	repos=n)
		}
	}
	
	if (length(libraries) > 0) {
		ownCat("Load libraries:")
		for(n in libraries) {
			ownCat(n)
			if (sfParallel())
				sfLibrary(n, character.only = TRUE)
			else
				library(n, character.only = TRUE)
		}
	
		if (useDev) {
			library("devtools")
		}	
	}	
}

buildDataSet <- function(workingDataSet, overallResult, colname, index, diagramTyp = "") {
	if (length(colname) > 0) {
		for (jj in seq(along=colname)) {
			if (diagramTyp == SPIDER.PLOT) {
				searchVector <- gsub("\\.[0-9]*","",colnames(overallResult)) %in% paste(colname[jj], index, sep="")	
			} else {
				searchVector <- paste(colname[jj], index, sep="")
			}		
			workingDataSet = cbind(workingDataSet, overallResult[searchVector])
		}	
		return(workingDataSet)
	}
}

reNameSpin <- function(colNameWichMustBind, colNames) {
	
	descriptorListIndex <- strsplit(substr(colNameWichMustBind,nchar(colNames$colOfMean)+1, nchar(colNameWichMustBind)),"\\.")
	
	if (descriptorListIndex[[1]][1] != "" & descriptorListIndex[[1]][2] != "") {
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

reNameColumn <-  function(plotThisValues, columnNameReplace = NAME, columnNameWhichUsedToReplace = PRIMAER.TREATMENT) {
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
	if (listOfTreat[[1]] == NONE & listOfTreat[[2]] != NONE) {
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

	treatment <- treatment %exists% args[3]
	secondTreatment <- secondTreatment %exists% args[4]
	secondTreatment <- treatment %checkEqual% secondTreatment
	
	listOfTreat <- list(treatment=treatment, secondTreatment=secondTreatment)
	listOfFilterTreat <- list(filterTreatment = filterTreatment, filterSecondTreatment = filterSecondTreatment)	## wird erstmal noch nichts weiter mit gemacht! nur geswapt falls notwendig

	if (treatment == NONE & secondTreatment == NONE) {
		listOfTreat$treatment <- NONE.TREATMENT
		listOfTreatAndFilterTreat <- list(listOfTreat, listOfFilterTreat)
	} else {	
		for (k in names(listOfTreat)) {
			if (listOfTreat[[k]] != NONE) {
				#overallTreat = list(iniDataSet=workingDataSet, descriptor=listOfTreat[[k]], debug=debug, stoppTheCalculation = FALSE, errorDescriptor=character())
				#overallTreat = list(iniDataSet=workingDataSet, descriptor=listOfTreat[[k]], debug=debug, stoppTheCalculation = FALSE)
				descriptorVector <- getVector(preprocessingOfDescriptor(listOfTreat[[k]], workingDataSet))
				
				if (!is.null(descriptorVector)) {
					descriptorVector <- getVector(checkIfDescriptorIsNaOrAllZero(descriptorVector, workingDataSet, FALSE))
					
					if (!is.null(descriptorVector)) {
						listOfTreat[[k]] <- descriptorVector
					} 
				} 
				
				if (is.null(descriptorVector)) {
					ownCat(paste(k, " set to '",NONE,"'", sep=""))
					
					listOfTreat[[k]] <- NONE
				}
			}
		}
	
		listOfTreatAndFilterTreat <- changeWhenTreatmentNoneAndSecondTreatmentNotNone(listOfTreat, listOfFilterTreat)
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
				overallList$nBoxDes[n] <- checkIfDescriptorIsNaOrAllZero(overallList$nBoxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$nBoxDes) <- c(1:length(overallList$nBoxDes))
	} else {
		ownCat("All values for nBoxplot are 'NA'")
		
	}
	
	if (sum(!is.na(overallList$boxDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxDes), "Boxplots..."))}
		for (n in 1:length(overallList$boxDes)) {
			if (!is.na(overallList$boxDes[[n]][1])) {
				overallList$boxDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$boxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$boxDes) <- c(1:length(overallList$boxDes))
	} else {
		ownCat("All values for Boxplot are 'NA'")
		
	}

	if (sum(!is.na(overallList$boxStackDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxStackDes), "stacked boxplots..."))}
		for (n in 1:length(overallList$boxStackDes)) {
			if (!is.na(overallList$boxStackDes[[n]][1])) {
				overallList$boxStackDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$boxStackDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$boxStackDes) <- c(1:length(overallList$boxStackDes))
	} else {
		ownCat("All values for stackedBoxplot are 'NA'")
		
	}

	if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxSpiderDes), "spiderplots..."))}
		for (n in 1:length(overallList$boxSpiderDes)) {
			if (!is.na(overallList$boxSpiderDes[[n]][1])) {
				initDescriptor <- overallList$boxSpiderDes[[n]]
				overallList$boxSpiderDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$boxSpiderDes[[n]], overallList$iniDataSet)
				booleanVector <- unlist(initDescriptor) %in% unlist(overallList$boxSpiderDes[[n]])
				overallList$boxSpiderDesName[[n]] <- as.data.frame(overallList$boxSpiderDesName[[n]][booleanVector])
				
			}
		}
		names(overallList$boxSpiderDes) <- c(1:length(overallList$boxSpiderDes))
		names(overallList$boxSpiderDesName) <- c(1:length(overallList$boxSpiderDesName))
	} else {
		ownCat("All values for spider plot are 'NA'")
		
	}
	
	if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
		if (overallList$debug) {ownCat(paste(length(overallList$violinBoxDes), "violinplot..."))}
		for (n in 1:length(overallList$violinBoxDes)) {
			if (!is.na(overallList$violinBoxDes[[n]][1])) {
				overallList$violinBoxDes[n] <- checkIfDescriptorIsNaOrAllZero(overallList$violinBoxDes[[n]], overallList$iniDataSet)
			}
		}
		names(overallList$violinBoxDes) <- c(1:length(overallList$violinBoxDes))
	} else {
		ownCat("All values for violin plot are 'NA'")
		
	}
	
	
	if ((!sum(!is.na(overallList$boxStackDes)) > 0 && 
		 !sum(!is.na(overallList$boxDes)) > 0 && 
		 !sum(!is.na(overallList$nBoxDes)) > 0 && 
		 !sum(!is.na(overallList$boxSpiderDes)) > 0 && 
		 !sum(!is.na(overallList$violinBoxDes))) > 0) {
		ownCat("No descriptor set (all descriptors are zero or NA) - the program needs to stop!")
		overallList$stoppTheCalculation <- TRUE 
	}
	
	return(overallList)
}

checkIfDescriptorIsNaOrAllZero <- function(descriptorVector, iniDataSet, isDescriptor = TRUE) {
	#overallList$debug %debug% "checkIfDescriptorIsNaOrAllZero()"
	descriptorVector <- as.vector(descriptorVector)
	#descriptorVector = getVector(descriptorVector)
	tempDescriptor <- descriptorVector 
	
	if (isDescriptor) {
		descriptorVector <- descriptorVector[colSums(!is.na(iniDataSet[descriptorVector])) != 0 & colSums(iniDataSet[descriptorVector] *1, na.rm = TRUE) > 0]
	} else {
		descriptorVector <- descriptorVector[colSums(!is.na(iniDataSet[descriptorVector])) != 0]
	}
	errorDescriptor <- tempDescriptor %GetDescriptorAfterCheckIfDescriptorNotExists% descriptorVector

	if (length(errorDescriptor) > 0) {
		errorDescriptor %errorReport% NOT.NUMERIC.OR.ALL.ZERO	
	}
	
	if (length(descriptorVector) > 0) {
		return(as.data.frame(descriptorVector))
	} else {
		return(NA)
	}
}

overallChangeName <- function(overallList) {
	overallList$debug %debug% "overallChangeName()"	
	
	if (!is.null(overallList$imageFileNames_nBoxplots)) {
		if (overallList$debug) {ownCat("line plots..."); }
		overallList$imageFileNames_nBoxplots <- changefileName(overallList$imageFileNames_nBoxplots, NBOX.PLOT)
		names(overallList$imageFileNames_nBoxplots) <- c(1:length(overallList$imageFileNames_nBoxplots))
		
		overallList$nBoxDesName <- as.list(overallList$nBoxDesName)
		names(overallList$nBoxDesName) <- c(1:length(overallList$nBoxDesName))
	}
	
	if (!is.null(overallList$imageFileNames_Boxplots)) {
		if (overallList$debug) {ownCat("boxplots...");}
		overallList$imageFileNames_Boxplots <- changefileName(overallList$imageFileNames_Boxplots, BOX.PLOT)
		names(overallList$imageFileNames_Boxplots) <- c(1:length(overallList$imageFileNames_Boxplots))
		
		overallList$boxDesName <- as.list(overallList$boxDesName)
		names(overallList$boxDesName) <- c(1:length(overallList$boxDesName))
	}
	
	if (!is.null(overallList$imageFileNames_StackedPlots)) {
		if (overallList$debug) {ownCat("Stacked boxplots...");}
		overallList$imageFileNames_StackedPlots <- changefileName(overallList$imageFileNames_StackedPlots, STACKBOX.PLOT)
		names(overallList$imageFileNames_StackedPlots) <- c(1:length(overallList$imageFileNames_StackedPlots))
		
		overallList$boxStackDesName = as.list(overallList$boxStackDesName)
		names(overallList$boxStackDesName) = c(1:length(overallList$boxStackDesName))
	}
	
	if (!is.null(overallList$imageFileNames_SpiderPlots)) {
		if (overallList$debug) {ownCat("spiderplots...");}
		overallList$imageFileNames_SpiderPlots <- changefileName(overallList$imageFileNames_SpiderPlots, SPIDER.PLOT)
		names(overallList$imageFileNames_SpiderPlots) <- c(1:length(overallList$imageFileNames_SpiderPlots))
		
		#overallList$boxSpiderDesName = as.list(overallList$boxSpiderDesName)
		#names(overallList$boxSpiderDesName) = c(1:length(overallList$boxSpiderDesName))
	}

	if (!is.null(overallList$imageFileNames_violinPlots) & overallList$isRatio) {
		if (overallList$debug) {ownCat("violinplots...");}
		overallList$imageFileNames_violinPlots <- changefileName(overallList$imageFileNames_violinPlots, VIOLIN.PLOT)
		names(overallList$imageFileNames_violinPlots) <- c(1:length(overallList$imageFileNames_violinPlots))
		
		overallList$violinBoxDesName <- as.list(overallList$violinBoxDesName)
		names(overallList$violinBoxDesName) <- c(1:length(overallList$violinBoxDesName))
	}

	return(overallList)
}

setOptions <- function(overallList, typOfPlot, typOfOptions, listOfExtraOptions) {
	overallList$debug %debug% "setOptions()"
	
	for (values in names(listOfExtraOptions[[typOfPlot]])) {
		if (values %in% names(overallList[[typOfOptions]])) {
			overallList[[typOfOptions]][[values]] <- c(overallList[[typOfOptions]][[values]], listOfExtraOptions[[typOfPlot]][[values]])
		} else {
			overallList[[typOfOptions]][[values]] <- c(listOfExtraOptions[[typOfPlot]][[values]])
		}
	}
	return(overallList)
}


setSomePrintingOptions <- function(overallList) {
	overallList$debug %debug% "setSomePrintingOptions()"

	requestList = 	getSpecialRequestDependentOfUserAndTypOfExperiment()
	if (!(overallList$user == NONE & overallList$typOfExperiment == NONE)) {
		listOfExtraOptions = requestList[[overallList$user]][[overallList$typOfExperiment]]	
		for (n in names(listOfExtraOptions)) {
			if (n == BOX.PLOT) {
				return(setOptions(overallList, BOX.PLOT, "boxOptions", listOfExtraOptions))				
			} else if (n == NBOX.PLOT) {
				return(setOptions(overallList, NBOX.PLOT, "nBoxOptions", listOfExtraOptions))
			} else if (n == STACKBOX.PLOT) {
				return(setOptions(overallList, STACKBOX.PLOT, "stackedBarOptions", listOfExtraOptions))
			} else if (n == SPIDER.PLOT) {
				return(setOptions(overallList, SPIDER.PLOT, "spiderOptions", listOfExtraOptions))
			} else if (n == VIOLIN.PLOT & overallList$isRatio) {
				return(setOptions(overallList, VIOLIN.PLOT, "violinOptions", listOfExtraOptions))
			}			
		}		
	}
	
	return(overallList)
}

checkUserOfExperiment <- function(overallList) {
	overallList$debug %debug% "checkUserOfExperiment()"

	user = NONE
	if ("Plant.ID" %in% colnames(overallList$iniDataSet)) {
		user = substr(overallList$iniDataSet$'Plant.ID'[1], 5, 6)		
	}
	overallList$user = user
	return(overallList)
}


checkTypOfExperiment <- function(overallList) {
	overallList$debug %debug% "checkTypOfExperiment()"

	typ = NONE
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


changefileName <- function(fileNameVector, typOfPlot) {
	#Sollten hier nicht noch die Leerzeichen durch Punkte ersetzt werden?
	
	fileNameVector = gsub("\\$", ";", substr(fileNameVector, 1, 70))
	fileNameVector = gsub("\\^", "", fileNameVector)

	if(typOfPlot == STACKBOX.PLOT) {
		for(i in 1:length(fileNameVector)) {
			if (length(grep("\\.bin\\.",fileNameVector[i], ignore.case=TRUE)) > 0){
				stringSplit <- paste(strsplit(fileNameVector[i], '\\.bin\\.')[[1]][1], ".bin.", sep="")
				fileNameVector[i] <- stringSplit
			} 
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
		
		if (!isColName){
			for (n in 1:length(value)) {
				value[[n]] = gsub(regExpressionCol, replaceString, value[[n]])
			}
		}
	} else {
		return(NONE)
	}
	return(value)
}

overallPreprocessingOfDescriptor <- function(overallList) {
	overallList$debug %debug% "overallPreprocessingOfDescriptor()"	
	
	#columnsAfterCheckOfNormalized <- checkForNormalizedColumns(descriptorSet_nBoxplot)
	if (!is.null(overallList$nBoxDes)) {
		if (overallList$debug) {ownCat(NBOX.PLOT)}
		for (n in 1:length(overallList$nBoxDes)) {
			overallList$nBoxDes[n] = preprocessingOfDescriptor(overallList$nBoxDes[[n]], overallList$iniDataSet)
		}
	} else {
		ownCat(paste(NBOX.PLOT," is NULL", sep=""))
		
	} 
	
	if (!is.null(overallList$boxDes)) {
		if (overallList$debug) {ownCat(BOX.PLOT)}
		for (n in 1:length(overallList$boxDes)) {
			overallList$boxDes[n] = preprocessingOfDescriptor(overallList$boxDes[[n]], overallList$iniDataSet)
		}
	} else {
		ownCat(paste(BOX.PLOT, " is NULL", sep=""))
		
	} 
	
	if (!is.null(overallList$boxStackDes)) {
		if (overallList$debug) {ownCat(STACKBOX.PLOT)}
		for (n in 1:length(overallList$boxStackDes)) {
			overallList$boxStackDes[n] = preprocessingOfDescriptor(overallList$boxStackDes[[n]], overallList$iniDataSet)
		}
	} else {
		ownCat(paste(STACKBOX.PLOT, " is NULL", sep=""))
		
	} 

	if (!is.null(overallList$boxSpiderDes)) {
		if (overallList$debug) {ownCat(SPIDER.PLOT)}
		for (n in 1:length(overallList$boxSpiderDes)) {
			initDescriptor <- preprocessingOfValues(overallList$boxSpiderDes[n], isColValue = TRUE)
			overallList$boxSpiderDes[n] = preprocessingOfDescriptor(overallList$boxSpiderDes[[n]], overallList$iniDataSet)
			booleanVector <- initDescriptor[[1]] %in% overallList$boxSpiderDes[n][[1]]
			overallList$boxSpiderDesName[n] = as.data.frame(preprocessingOfValues(overallList$boxSpiderDesName[[n]], isColName=TRUE)[[1]][booleanVector])
		}
	} else {
		ownCat(paste(SPIDER.PLOT, " is NULL", sep=""))
		
	} 

	if (!is.null(overallList$violinBoxDes) & overallList$isRatio) {
		if (overallList$debug) {ownCat(VIOLIN.PLOT)}
		for (n in 1:length(overallList$violinBoxDes)) {
			initDescriptor <- preprocessingOfValues(overallList$violinBoxDes[n], isColValue = TRUE)
			overallList$violinBoxDes[n] = preprocessingOfDescriptor(overallList$violinBoxDes[[n]], overallList$iniDataSet)
			booleanVector <- initDescriptor[[1]] %in% overallList$violinBoxDes[n][[1]]
			overallList$violinBoxDesName[n] = as.data.frame(preprocessingOfValues(overallList$violinBoxDesName[[n]], isColName=TRUE)[[1]][booleanVector])
		}
	} else {
		ownCat("No ratio data set/violin plot")
		
	} 
	
	if ((!sum(!is.na(overallList$boxStackDes)) > 0 && 
		 !sum(!is.na(overallList$boxDes)) > 0 && 
		 !sum(!is.na(overallList$nBoxDes)) > 0 && 
		 !sum(!is.na(overallList$boxSpiderDes)) > 0 && 
		 !sum(!is.na(overallList$violinBoxDes))) > 0) {
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

	for(nn in descriptorVector) {	
		if((length(grep("normalized.",nn , ignore.case=TRUE)) > 0) ||
		   (length(grep("norm.",nn , ignore.case=TRUE)) > 0)){
   			nnUn <- gsub("normalized."," ",nn)
			nnUn <- gsub("norm."," ",nnUn)
			if(nnUn %in% descriptorVector) {
				errorDescriptor <- c(errorDescriptor, nnUn)
				descriptorVector[descriptorVector!=nnUn]
			} 
  		}
	}
	
	if (length(errorDescriptor)>0) {
		errorDescriptor %errorReport% NOT.EXISTS
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
	
	if (overallList$filterXaxis != NONE) {
		overallList$filterXaxis = as.numeric(strsplit(overallList$filterXaxis, "$", fixed=TRUE)[[1]])
	} else {
		overallList$filterXaxis = as.numeric(unique(overallList$iniDataSet[overallList$xAxis])[[1]])	#xAxis muss einen Wert enthalten ansonsten Bricht das Program weiter oben ab
	}
	return(overallList)
}

getSingelFilter <- function(filter, treatment, dataSet) {
	if (filter != NONE) {
		return(strsplit(filter, "$", fixed=TRUE)[[1]])
	} else {
		return(as.character(unique(dataSet[treatment])[[1]]))
	}
}

preprocessingOfTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheTreatment()"
	
	if (!is.null(overallList$treatment)) {
		overallList$treatment = preprocessingOfValues(overallList$treatment)
		
		if (overallList$treatment != NONE & (overallList$treatment %checkIfDescriptorExists% overallList$iniDataSet)) {	
			overallList$filterTreatment = getSingelFilter(overallList$filterTreatment, overallList$treatment, overallList$iniDataSet)

		} else {
			overallList$treatment = NONE
			overallList$filterTreatment = NONE
			ownCat(paste("Set 'filterTreatment' and 'treatment' to '",NONE,"'!", sep=""))
					
		}			
	} else {
		overallList$treatment = NONE
		overallList$filterTreatment = NONE
		ownCat(paste("Set 'filterTreatment' and 'treatment' to '",NONE,"'!", sep=""))
		
	}
	return(overallList)
}

preprocessingOfSecondTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheSecondTreatment()"
	
	if (!is.null(overallList$secondTreatment)) {
		overallList$secondTreatment <- preprocessingOfValues(overallList$secondTreatment)

		if (overallList$secondTreatment != NONE & (overallList$secondTreatment %checkIfDescriptorExists% overallList$iniDataSet)) {
			overallList$filterSecondTreatment <- getSingelFilter(overallList$filterSecondTreatment, overallList$secondTreatment, overallList$iniDataSet)
			
			if (length(overallList$filterSecondTreatment) == 1) {
				overallList$secondTreatment <- NONE
				overallList$filterSecondTreatment <- NONE
				overallList$split.Treatment.Second <- FALSE
				ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '",NONE,"' and 'split.Treatment.Second' to 'FALSE'!", sep=""))
			}
			
		} else {
			overallList$secondTreatment <- NONE
			overallList$filterSecondTreatment <- NONE
			overallList$split.Treatment.Second <- FALSE
			ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '",NONE,"' and 'split.Treatment.Second' to 'FALSE'!", sep=""))
			
		}	
	} else {
		overallList$secondTreatment <- NONE
		overallList$filterSecondTreatment <- NONE
		overallList$split.Treatment.Second <- FALSE
		ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '",NONE,"' and 'split.Treatment.Second' to 'FALSE'!", sep=""))
		
	}
	return(overallList)
}

check <- function(value, checkValue=c(NONE, NA)) {
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
	if (overallList$isRatio) {
		overallList$iniDataSet = overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(getVector(overallList$boxSpiderDes)), check(getVector(overallList$violinBoxDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
	} else {
		overallList$iniDataSet = overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(getVector(overallList$boxSpiderDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
	}
	return(overallList)
}

groupByFunction <- function(groupByList) {
######
#groupByList <- list(overallList$treatment, overallList$secondTreatment)
######
	groupByList = unlist(groupByList)
	return(unlist(groupByList[ifelse(groupByList != NONE, TRUE, FALSE)]))
}

getBooleanVectorForFilterValues <- function(groupedDataFrame, listOfValues, plot=FALSE) {
###############
#groupedDataFrame <- groupedDataFrameMean
#listOfValues <- booleanVectorList
#plot=FALSE
###############
	
	iniType = !plot
	tempVector = rep.int(iniType, times=length(groupedDataFrame[, 1]))
	
	for (h in names(listOfValues)) {
		if (h != NONE & !is.null(groupedDataFrame[[h]])) {
			if (plot) {
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
		if (k != NONE) {
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
				if (tolower(overallList$diagramTyp) != STACKBOX.PLOT)
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

buildRowName <- function(mergeDataSet, groupBy, contactTheValues, yName = "value") {	
#####
#mergeDataSet <- iniDataSet
#####
		
	if (length(groupBy) == 0) {
		return(data.frame(name=rep.int(yName, length(mergeDataSet[, 1])), mergeDataSet))
	} else if (length(groupBy) == 1) {
		return(data.frame(name=mergeDataSet[, groupBy], mergeDataSet[, !(colnames(mergeDataSet) %in% groupBy)]))
	} else {		
		#temp = mergeDataSet[, groupBy[2]]
	
		if(contactTheValues) {
			temp = mergeDataSet[, groupBy[1]]
			for (h in 2:length(groupBy)) {
				temp = paste(temp, mergeDataSet[, groupBy[h]], sep = "/") #  #/#
			}
		} else {
			temp = mergeDataSet[, groupBy[2]]
			if (length(groupBy) > 2) {
				reduceGroupBy <- groupBy[3:length(groupBy)]
				for (h in seq(along=reduceGroupBy)) {
					temp = paste(temp, mergeDataSet[, reduceGroupBy[h]], sep = "/") #  #/#
				}
			}
		}
	
		return(data.frame(name=temp, primaerTreatment= mergeDataSet[, groupBy[1]], mergeDataSet[, mergeDataSet %allColnamesWithoutThisOnes% groupBy]))
	}	
}

checkTheNumberOfEntriesOfTheDay <- function(iniPosition, values, uniqueDays) {
	
}


getToPlottedDays <- function(values, nameOfValueColumn, minimumNumberOfValuesOfEachDays, changes=NULL) {
##########
#
##########
#tempValues <- values
	uniqueDays = unique(getVector(values[values %allColnamesWithoutThisOnes% nameOfValueColumn]))
	
	if(minimumNumberOfValuesOfEachDays > 1) {
		newValues <- data.frame()
		for(dd in uniqueDays) {
			if(sum(values$xAxis==dd) >= minimumNumberOfValuesOfEachDays) {
				newValues <- rbind(newValues, values[values$xAxis== dd,])
			}
		}
		values <- newValues
	}
	
	if(length(values)>0) {
		uniqueDays = unique(getVector(values[values %allColnamesWithoutThisOnes% nameOfValueColumn]))
		medianPosition = floor(median(1:length(uniqueDays)))
		
		days = uniqueDays[floor(median(1:length(uniqueDays[uniqueDays<=uniqueDays[medianPosition]])))]
		
		days = c(days, uniqueDays[medianPosition])
		days = c(days, uniqueDays[floor(median(length(uniqueDays[uniqueDays>=uniqueDays[medianPosition]]):length(uniqueDays)))])
		days = c(days, uniqueDays[length(uniqueDays)])
		 
		if (!is.null(changes)) {
			days = c(as.numeric(changes), days[(length(changes)+1):4])
		}
	} else {
		days <- NULL
	}
	
	return(days)
	
}

setxAxisfactor <- function(xAxisName, values, nameOfValueColumn, options = NULL, typOfPlot = "") {
##############
#xAxisName <- overallList$xAxisName
#xAxisValue <- overallResult$xAxis
#nameOfValueColumn <- "value"
#values <- overallResult[c("xAxis","value")]
##############


	minimumNumberOfValuesOfEachDays <- 1
	if(typOfPlot == BOX.PLOT) {
		minimumNumberOfValuesOfEachDays <- 2
	}

	
	if (!is.null(options$daysOfBoxplotNeedsReplace)) {
		whichDayShouldBePlot = getToPlottedDays(values, nameOfValueColumn, minimumNumberOfValuesOfEachDays, options$daysOfBoxplotNeedsReplace)
	} else {
		whichDayShouldBePlot = getToPlottedDays(values, nameOfValueColumn, minimumNumberOfValuesOfEachDays)
	}
	if(!is.null(whichDayShouldBePlot)) {
		xAxisfactor = factor(getVector(values[values %allColnamesWithoutThisOnes% nameOfValueColumn]), levels=unique(whichDayShouldBePlot))
	
		xAxisfactor = paste(xAxisName, xAxisfactor)
		naString = paste(xAxisName, "NA")
		xAxisfactor[xAxisfactor == naString] = NA
		
	#	xAxisfactor = paste("DAS", xAxisfactor)
	#	xAxisfactor[xAxisfactor == "DAS NA"] = NA
	} else {
		xAxisfactor <- NULL
	}
		
	return(xAxisfactor)
}


overallGetResultDataFrame <- function(overallList) {
	overallList$debug %debug% "overallGetResultDataFrame()"	

	if (!calculateNothing) {	
			groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
			colNames = list(colOfXaxis="xAxis", colOfMean="mean", colOfSD="se", colName="name", xAxis=overallList$xAxis)
			booleanVectorList = buildList(overallList, colNames$colOfXaxis)
			columnsStandard = c(check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment))
			contactTheValues <- FALSE
			
			if((!overallList$split.Treatment.First && !overallList$split.Treatment.Second) && overallList$secondTreatment != NONE) {
				contactTheValues <- TRUE
				ownCat("... there should be no split, so the data have to be conected now!")
			}
			
			if (sum(!is.na(overallList$nBoxDes)) > 0) {
				if (overallList$debug) {ownCat(NBOX.PLOT)}
				columns = c(columnsStandard, check(getVector(overallList$nBoxDes)))
				overallList$overallResult_nBoxDes = getResultDataFrame(NBOX.PLOT, overallList$nBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
			} else {
				ownCat(paste("All values for ",NBOX.PLOT," are 'NA'", sep=""))
			}
			
			if (sum(!is.na(overallList$boxDes)) > 0) {
				if (overallList$debug) {ownCat(BOX.PLOT)}
				colNames$colOfMean = "value"
				columns = c(columnsStandard, check(getVector(overallList$boxDes)))
				overallList$overallResult_boxDes = getResultDataFrame(BOX.PLOT, overallList$boxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
			} else {
				ownCat(paste("All values for ",BOX.PLOT," are 'NA'", sep=""))
			}
			
			if (sum(!is.na(overallList$boxStackDes)) > 0) {
				if (overallList$debug) {ownCat(STACKBOX.PLOT)}
				colNames$colOfMean = check(getVector(overallList$boxStackDes))
				colNames$colOfXaxis = overallList$xAxis
				columns = c(columnsStandard, check(getVector(overallList$boxStackDes)))
				overallList$overallResult_boxStackDes = getResultDataFrame(STACKBOX.PLOT, overallList$boxStackDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
			} else {
				ownCat(paste("All values for ",STACKBOX.PLOT," are 'NA'", sep=""))
			}
		
			if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
				if (overallList$debug) {ownCat(SPIDER.PLOT)}
				#colNames$colOfMean = check(getVector(overallList$boxSpiderDes))
				colNames$colOfMean = "value"
				colNames$colOfXaxis = overallList$xAxis
				colNames$desNames = overallList$boxSpiderDesName
				columns = c(columnsStandard, check(getVector(overallList$boxSpiderDes)))
				overallList$overallResult_boxSpiderDes = getResultDataFrame(SPIDER.PLOT, overallList$boxSpiderDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
			} else {
				ownCat(paste("All values for ",SPIDER.PLOT," are 'NA'", sep=""))
			}

			if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
				if (overallList$debug) {ownCat(VIOLIN.PLOT)}
				colNames$colOfMean = "mean"
				colNames$colOfXaxis = "xAxis"
				columns = c(columnsStandard, check(getVector(overallList$violinBoxDes)))
				overallList$overallResult_violinBoxDes = getResultDataFrame(VIOLIN.PLOT, overallList$violinBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
			}
			
			
			if (is.null(overallList$boxStackDes) && 
				is.null(overallList$boxDes) && 
				is.null(overallList$nBoxDes) && 
				is.null(overallList$boxSpiderDes) && 
				is.null(overallList$violinBoxDes)) {
				ownCat("No descriptor set - this run needs to stop!")
				overallList$stoppTheCalculation = TRUE
			}
	}
	return(overallList)
}

getPlotNumber <- function(colNameWichMustBind, descriptorList, diagramTyp) {
	
	if (diagramTyp == STACKBOX.PLOT) {
	
		for (n in names(descriptorList)) {
			if (colNameWichMustBind %in% as.vector(unlist(descriptorList[[n]]))) {
				return(n)
			}
		}
		return(-1)
	} else if (diagramTyp == SPIDER.PLOT) {
		return(strsplit(substring(colNameWichMustBind,nchar("value")+1),"\\.")[[1]][1])
	}
}


getResultDataFrame <- function(diagramTyp, descriptorList, iniDataSet, groupBy, colNames, booleanVectorList, debug, contactTheValues) {	
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
#	diagramTyp = "boxplot"
#	descriptorList = overallList$boxDes
#	iniDataSet = overallList$iniDataSet[columns]
#	debug = overallList$debug	
#########################
#split.Treatment.First <- overallList$split.Treatment.First
#split.Treatment.Second <- overallList$split.Treatment.Second
#########################

	debug %debug% "getResultDataFrame()"
	
	#contactTheValues <- FALSE
	descriptor = getVector(descriptorList)

	if (diagramTyp == SPIDER.PLOT) {
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

	
	if (diagramTyp != BOX.PLOT) {
		groupedDataFrame = data.table(iniDataSet)
		#key(groupedDataFrame) = c(groupBy, colNames$xAxis)
		setkeyv(groupedDataFrame,c(groupBy, colNames$xAxis))
	}
	
	if (diagramTyp == BOX.PLOT) {
		if(length(groupBy)>0) {
			groupedDataFrameMean <- iniDataSet[groupBy[1]]
			groupByReduce <- groupBy[groupBy!=groupBy[1]]
		
			for (n in c(groupByReduce, colNames$xAxis, descriptor)) {
				groupedDataFrameMean <- cbind(groupedDataFrameMean, iniDataSet[n])
			}		
		} else {
			groupedDataFrameMean <- iniDataSet
		}
	} else {
		groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(.SD, mean, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		#groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(colnames(groupedDataFrame), mean, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
	}
	
	if (diagramTyp == NBOX.PLOT || diagramTyp == BOX.PLOT || diagramTyp == SPIDER.PLOT || diagramTyp == VIOLIN.PLOT) {
		#colNamesOfTheRest = paste(colNames$colOfMean, seq(1:length(descriptor)), sep="")	
		colNamesOfTheRest = paste(colNames$colOfMean, descriptorName, sep="")	
	} else {
		colNamesOfTheRest = groupedDataFrameMean %allColnamesWithoutThisOnes% c(groupBy, colNames$xAxis)
	}

	colnames(groupedDataFrameMean) = c(groupBy, colNames$colOfXaxis, colNamesOfTheRest)
	
	if (diagramTyp == NBOX.PLOT) {
		groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(.SD, sd, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		#groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(colnames(groupedDataFrame), sd, na.rm=TRUE), by=c(groupBy, colNames$xAxis)])
		colnames(groupedDataFrameSD) = c(groupBy, colNames$colOfXaxis, paste(colNames$colOfSD, descriptorName, sep=""))
	}
	
	booleanVector = getBooleanVectorForFilterValues(groupedDataFrameMean, booleanVectorList)
	
	if (diagramTyp == NBOX.PLOT) {
		iniDataSet = merge(sort=FALSE, groupedDataFrameMean[booleanVector, ], groupedDataFrameSD[booleanVector, ], by = c(groupBy, colNames$colOfXaxis))
		overallResult = buildRowName(iniDataSet, groupBy, contactTheValues)
		
	} else	if (diagramTyp == BOX.PLOT || diagramTyp == VIOLIN.PLOT) {
		#|| diagramTyp == "spiderplot"
		iniDataSet = groupedDataFrameMean[booleanVector, ]
		overallResult = buildRowName(iniDataSet, groupBy, contactTheValues)
	} else if (diagramTyp == SPIDER.PLOT) {
		iniDataSet = groupedDataFrameMean[booleanVector, ]
		buildRowNameDataSet = buildRowName(iniDataSet, groupBy, contactTheValues)
		temp = data.frame()

		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, PRIMAER.TREATMENT)) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList, diagramTyp)
			
			colNameWichMustBindReNamed <- reNameSpin(colNameWichMustBind, colNames)
		
			if (is.null(buildRowNameDataSet$primaerTreatment)) {	
				temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot=plot))			
			} else {
				temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), primaerTreatment=buildRowNameDataSet[, PRIMAER.TREATMENT], values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot = plot))			
			}
		}
		overallResult = temp
		
	} else {
		iniDataSet <- groupedDataFrameMean[booleanVector, ]	
		buildRowNameDataSet <- buildRowName(iniDataSet, groupBy, contactTheValues)
		temp = data.frame()
		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, PRIMAER.TREATMENT)) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList, diagramTyp)
		
			if (plot!=-1) {
				colNameWichMustBindReNamed = reNameHist(colNameWichMustBind)
		
				if (is.null(buildRowNameDataSet$primaerTreatment)) {	
					temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot=plot))			
				} else {
					temp = rbind(temp, data.frame(hist=rep.int(x=colNameWichMustBindReNamed, times=length(buildRowNameDataSet[, colNameWichMustBind])), primaerTreatment=buildRowNameDataSet[, PRIMAER.TREATMENT], values=buildRowNameDataSet[, colNameWichMustBind], xAxis=buildRowNameDataSet[, colNames$colOfXaxis], name=buildRowNameDataSet[, colNames$colName], plot = plot))			
				}
			}
		}
		overallResult = temp		
	}
	return(overallResult)
}

setDefaultAxisNames <- function(overallList) {
	overallList$debug %debug% "setDefaultAxisNames()"
	
	if (overallList$xAxisName == NONE) {
		overallList$xAxisName = gsub('[[:punct:]]', " ", overallList$xAxis)
	}
#	if (overallList$yAxisName == "none") {
#		overallList$yAxisName = gsub('[[:punct:]]', " ", overallList$descriptor)
#	}
	return(overallList)
}

setColorListHist <- function(descriptorList, colorVector) {
	interval = seq(0.05, 0.95, by=0.1)
	intervalSat = rep.int(c(0.8, 1.0), 5)
	intervalFluo = seq(0, 0.166666666666, by=0.0185185185)

	interval20 = seq(0.025, 0.975, by=0.05)
	intervalSat20 = 1 #rep.int(c(0.8, 1.0), 10)
	intervalFluo20 = seq(0, 0.166666666666, by=0.008771929789)

	if (length(grep("fluo", getVector(descriptorList), ignore.case=TRUE)) > 0) { #rot			
		colorList = as.list(hsv(h=c(rev(intervalFluo), rev(intervalFluo20)), s=c(intervalSat, intervalSat20), v=1))
	} else if (length(grep("phenol", getVector(descriptorList), ignore.case=TRUE)) > 0) { #gelb
		colorList = as.list(hsv(h=c(intervalFluo, intervalFluo20), s=c(intervalSat, intervalSat20), v=1))
	} else if (length(grep("vis.hsv.h", getVector(descriptorList), ignore.case=TRUE)) > 0) {
		colorList = as.list(hsv(h=c(interval, interval20), s=1, v=c(intervalSat, intervalSat20)))
	} else if (length(grep("vis.hsv.s", getVector(descriptorList), ignore.case=TRUE)) > 0) {
		colorList = colorRampPalette(colorVector)(30)
	} else if ((length(grep("nir", getVector(descriptorList), ignore.case=TRUE)) > 0) ||
			   (length(grep("vis.hsv.v", getVector(descriptorList), ignore.case=TRUE)) > 0)) {
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

setColorList <- function(diagramTyp, descriptorList, overallResult, isGray, first, second) {
######################
#diagramTyp <- "spiderplot"
#descriptorList <- overallList$boxSpiderDes
#overallResult <- overallList$overallResult_boxSpiderDes
#isGray <- overallList$isGray
######################	
	whichColumShouldUse <- checkWhichColumShouldUseForPlot(first, second, colnames(overallResult), diagramTyp)	

	if (!as.logical(isGray)) {
		#colorVector = c(brewer.pal(8, "Set1"))
		colorVector = c(brewer.pal(7, "Dark2"))
		#colorVector = c(brewer.pal(11, "Spectral")) # sometimes very pale colors
	} else {
		colorVector = c(brewer.pal(9, "Greys"))
	}
	
	colorList = list()
	if (diagramTyp == NBOX.PLOT || diagramTyp == BOX.PLOT || diagramTyp == VIOLIN.PLOT || diagramTyp == SPIDER.PLOT) {
		for (n in names(descriptorList)) {
			#if (!is.na(descriptorList[[n]])) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult[[whichColumShouldUse]])))
				#print(colorList[[n]])
#				if (isOtherTyp) {
#					colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
#				} else {
#					if("primaerTreatment" %in% colnames(overallResult)) {
#						colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$primaerTreatment)))
#					} else {
#						colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
#					}
#				}
			} else {
				#ownCat("All values are 'NA'")
			}
		}
	} else {
		for (n in names(descriptorList)) {
			if (sum(!is.na(descriptorList[[n]])) > 0) {
				colorList[[n]] = setColorListHist(descriptorList[n], colorVector)
			} else {
				#ownCat("All values are 'NA'")
			}
		}
	}
	return(colorList)
}

setColor <- function(overallList) {
	overallList$debug %debug% "setColor()" 
	
	#isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$split.Treatment.First, overallList$split.Treatment.Second)
					
	overallList$color_nBox = setColorList(NBOX.PLOT, overallList$nBoxDes, overallList$overallResult_nBoxDes, overallList$isGray, overallList$split.Treatment.First, overallList$split.Treatment.Second)
	overallList$color_box = setColorList(BOX.PLOT, overallList$boxDes, overallList$overallResult_boxDes, overallList$isGray, overallList$split.Treatment.First, overallList$split.Treatment.Second)
	overallList$color_boxStack = setColorList(STACKBOX.PLOT, overallList$boxStackDes, overallList$overallResult_boxStackDes, overallList$isGray, overallList$split.Treatment.First, overallList$split.Treatment.Second)
	overallList$color_spider = setColorList(SPIDER.PLOT, overallList$boxSpiderDes, overallList$overallResult_boxSpiderDes, overallList$isGray, overallList$split.Treatment.First, overallList$split.Treatment.Second)
	#overallList$color_violin = setColorList("violinplot", overallList$violinBoxDes, overallList$overallResult_violinBoxDes, overallList$isGray, isOtherTyp)
	return(overallList)
}

normalizeToHundredPercent =  function(whichRows, overallResult) {
	return(t(apply(overallResult[whichRows, ], 1, function(x, y) {(100*x)/y}, y=colSums(overallResult[whichRows, ]))))
}

renameYForSubsection <- function(label) {
	
	label <- gsub("\\\\% ","percent", label)
	label <- gsub("\\^2","$^2$", label)
	label <- paste(toupper(substr(label,0,1)),substr(label, 2, nchar(label)), sep="")
			
	return(label)
}

writeLatexFile <- function(fileNameLatexFile, fileNameImageFile="", ylabel="", subsectionDepth=1, saveFormatImage = "pdf") { #insertSubsections=FALSE,
	#o=""
	#fileNameImageFile = preprocessingOfValues(fileNameImageFile, FALSE, "_")
	fileNameLatexFile = preprocessingOfValues(fileNameLatexFile, FALSE, "_")
	#o = gsub('[[:punct:]]', "_", o)
	#print(fileNameImageFile)
	latexText <- ""
	if (nchar(ylabel) > 0) {
		ylabel <- renameYForSubsection(ylabel)
		if (subsectionDepth == 1) {
			latexText = paste(latexText, "\\subsection{",ylabel,"}\n", sep="" )
		} else if (subsectionDepth == 2) {
			latexText = paste(latexText, "\\subsubsection{",ylabel,"}\n", sep="" )
		} else if (subsectionDepth == 3) {
			latexText = paste(latexText, "\\paragraph{",ylabel,"}~", sep="" )
		} else if (subsectionDepth == 4) {
			latexText = paste(latexText, "\\subparagraph{",ylabel,"}~", sep="" )
		}
	}
	
#	if (insertSubsections & nchar(ylabel) > 0) {
#		ylabel <- renameYForAppendix(ylabel)
#		latexText = paste(latexText, "\\subsection{",ylabel,"}\n", sep="" )
#	}

	latexText = paste(latexText,
					 "\\loadImage{", 
					   ifelse(fileNameImageFile == "", fileNameLatexFile, fileNameImageFile), 
					  #ifelse(o == "", "", paste("_", o , sep="")), 
					  #ifelse(o == "", "", o), 
					   ".",saveFormatImage,"}", sep="")
	
	write(x=latexText, append=TRUE, file=paste(fileNameLatexFile, "tex", sep="."))
}


writeLatexTable <- function(fileNameLatexFile, columnName=NULL, value=NULL, columnWidth=NULL) {
	latexText <- ""

	if (length(columnName) > 0) {
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
				if (n != length(columnName)) {
					latexText <- paste(latexText, "& ", sep=" ")
				}
			}
			latexText <- paste(latexText,
								"\\tabularnewline",
								"\\hline",
								"\\hline", sep=" ")
			if (n == 1) {
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
		
	} else if (length(value) > 0){
		if (!is.null(value)) {
			for(n in 1:length(value)) {
				latexText <- paste(latexText, parseString2Latex(renameFilterOutput(as.character(value[n]))))
				if (n != length(value)) {
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
	
	if (latexText != "") {
		write(x=latexText, append=TRUE, file=paste(fileNameLatexFile, "tex", sep="."))
	}	
}


saveImageFile <- function(overallList, plot, filename, newHeight = "") {

	#filename = preprocessingOfValues(paste(filename, extraString, sep=""), FALSE, replaceString = "_")	
	if(newHeight == "") {
		height <- 5
	} else {
		height <- newHeight
	}
	#print(filename)
	#ggsave (filename=paste(paste(filename, runif(1, 0.0, 1.0)), overallList$saveFormat, sep="."), plot = plot, dpi=as.numeric(overallList$dpi), width=8, height=5)
	ggsave (filename=paste(filename, overallList$saveFormat, sep="."), plot = plot, dpi=as.numeric(overallList$dpi), width=8, height=height)

}

#makeDepthBoxplotDiagram <- function(h, overallList) {
#	overallList$debug %debug% "makeDepthBoxplotDiagram()"
#	overallList$symbolParameter = 15
#	
#	if (h == 1) {
#		openImageFile(overallList)
#	}
#	par(mar=c(4.1, 4.1, 2.1, 2.1))
#	plot.depth(as.matrix(overallList$overallResult), plot.type=h, xlabel=overallList$xAxisName, l.width=12, lp.color=overallList$color)
#	
#	grid()
#	if (h == 1) {
#		dev.off()
#	}
#	if (overallList$appendix) {
#		writeLatexFile("appendixImage", overallList$fileName)
#	}
#	
#	return(overallList)
#}

CheckIfOneColumnHasOnlyValues <- function(overallResult, descriptor="", diagramTyp = NBOX.PLOT) {	
	max = -1	
	for (index in levels(overallResult$name)) {
		if (diagramTyp == NBOX.PLOT || diagramTyp == BOX.PLOT) {
			temp = sum(!is.na(overallResult$mean[overallResult$name == index]))
		} else {
			boolVec = overallResult$name == index
			temp = sum(!is.na(overallResult[boolVec, descriptor]))
		}
		max = ifelse(temp > max, temp, max)
	}	
	return(ifelse(max == 1, TRUE, FALSE))
}

#buildMyStats <- function(values, means, se) {
#	means = as.data.frame(as.vector(means))
#	colnames(means) = "means"
#	
#	se = as.data.frame(as.vector(se))
#	colnames(se) = "se"
#
#	return(data.frame(value=values, means=means, se=se))
#}
#
#buildMyStats2 <- function(values, means, se, rowName) {
#	means = as.data.frame(as.vector(means))
#	colnames(means) = "means"
#	
#	rowName = as.data.frame(as.vector(rowName))
#	colnames(rowName) = Name
#	
#	se = as.data.frame(as.vector(se))
#	colnames(se) = "se"
#	
#	return(data.frame(value=values, means=means, se=se, rowName=rowName))
#}

reduceOverallResult <- function(tempOverallList, imagesIndex) {
	tempOverallList$debug %debug% "reduceOverallResult()"

	workingDataSet = buildDataSet(tempOverallList$overallResult[, 1:2], tempOverallList, c("mean", "se"), imagesIndex)
	colnames(workingDataSet) = c(colnames(workingDataSet)[1:2], "mean", "se")
	return(workingDataSet)	
}


reduceWholeOverallResultToOneValue <- function(tempOverallResult, imagesIndex, debug, diagramTyp = NBOX.PLOT) {
####################
#debug <- overallList$debug
#diagramTyp <- "nboxplot"
#####################
	
	
	debug %debug% "reduceWholeOverallResultToOneValue()"
	
	if (diagramTyp == STACKBOX.PLOT || diagramTyp == SPIDER.PLOT) {
		workingDataSet = tempOverallResult[tempOverallResult$plot == imagesIndex, ]
		workingDataSet$hist = factor(workingDataSet$hist, unique(workingDataSet$hist))
	} else {
		colNames = vector()
		if (diagramTyp == NBOX.PLOT || diagramTyp == BAR.PLOT) {
			colNames = c("mean", "se")
		} else if (diagramTyp == BOX.PLOT) {
			colNames = c("value")
		} else if (diagramTyp == VIOLIN.PLOT) {
			colNames = c("mean")
		}
		
		if (PRIMAER.TREATMENT %in% colnames(tempOverallResult)) {
			standardColumnName = c("name", PRIMAER.TREATMENT, "xAxis")
		} else {
			standardColumnName = c("name", "xAxis")
		}

		if (sum(!(colNames %in% colnames(tempOverallResult)))>0) {

			workingDataSet = buildDataSet(tempOverallResult[, standardColumnName], tempOverallResult, colNames, imagesIndex, diagramTyp)
			lengthOfNewColumns <- length(colnames(workingDataSet[,-c(1:length(standardColumnName))]))
			
#			if (diagramTyp == "spiderplot") {
#				if (lengthOfNewColumns > 1) {
#					colnames(workingDataSet) = c(standardColumnName, paste(rep.int(colNames,lengthOfNewColumns),1:lengthOfNewColumns, sep=""))
#				}else {
#					colnames(workingDataSet) = c(standardColumnName, colNames)
#				}
#			} else {
				colnames(workingDataSet) = c(standardColumnName, colNames)
#			}
		
#			if (lengthOfNewColumns > 1) {
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

newTreatmentNameFunction <- function(seq, n, numberCharAfterSeparate, tooLong, maxValues) {
	#numberCharAfterSeparate <- 8
	if(tooLong) {
#		if(maxValues > 10)
		
		if (nchar(n) > (numberCharAfterSeparate + 4)) {
			newTreatmentName <- paste(seq, ". ", substr(n,1,numberCharAfterSeparate), " ...", sep="")
		} else {
			newTreatmentName <- paste(seq, ". ", n, sep="")
		}
	} else {
		newTreatmentName <- n
	}
	return(newTreatmentName)
}


renameOfTheTreatments <- function(overallList) {
	overallList$debug %debug% "renameOfTheTreatments()"
	
	if (!overallList$appendix) {
		
		#newTreatmentName <- character()
		columnName <- c("Short name", "Full Name")
		numberCharAfterSeparate <- 8
		
		if (overallList$filterTreatment[1] != NONE) {
			seq <- 0;
			FileName <- "conditionsFirstFilter"
			writeLatexTable(FileName, columnName, columnWidth=c("3cm","13cm"))
		
			tooLong <- FALSE
			for(n in overallList$filterTreatment) {
				if (nchar(n) > (numberCharAfterSeparate+4))
					tooLong <- TRUE
			}
			
			for(n in overallList$filterTreatment) {
				seq <- seq+1
				overallList$filterTreatmentRename[[n]] <- newTreatmentNameFunction(seq, n, numberCharAfterSeparate, tooLong, length(overallList$filterTreatment))
				writeLatexTable(FileName, value=c(overallList$filterTreatmentRename[[n]],n))
			}
			writeLatexTable(FileName)
		}
	
		if (overallList$filterSecondTreatment[1] != NONE) {
			seq <- 0
			FileName <- "conditionsSecondFilter"
			writeLatexTable(FileName, columnName, columnWidth=c("3cm","13cm"))
			
			tooLong <- FALSE
			for(n in overallList$filterSecondTreatment) {
				if (nchar(n) > (numberCharAfterSeparate+4))
					tooLong <- TRUE
			}
			
			for(n in overallList$filterSecondTreatment) {
				seq <- seq+1
				overallList$secondFilterTreatmentRename[[n]] <- newTreatmentNameFunction(letters[seq], n, numberCharAfterSeparate, tooLong, length(overallList$filterTreatment))
				writeLatexTable(FileName, value=c(overallList$secondFilterTreatmentRename[[n]],n))
			}
			writeLatexTable(FileName)
		}
	}

	return(overallList)
}

replaceTreatmentNamesOverallOneValue <- function(overallList, title, typOfPlot="") {
######
#listForGetTitle <- optionListForGetBoolean
######
	overallList$debug %debug% "replaceTreatmentNamesOverallOneValue()"

	if((typOfPlot == SPIDER.PLOT || typOfPlot == BOX.PLOT || typOfPlot == STACKBOX.PLOT) && 
		!overallList$split.Treatment.First && overallList$split.Treatment.Second) {		
		title <- replaceTreatmentNames(overallList, title, onlySecondTreatment = TRUE, oneValue = TRUE)
	} else {
		title <- replaceTreatmentNames(overallList, title, onlyFirstTreatment = TRUE, oneValue = TRUE)
	}
	
	return(title)
}


replaceTreatmentNamesOverall <- function(overallList, overallResult) {	
	overallList$debug %debug% "replaceTreatmentNamesOverall()"
	if(!overallList$appendix) {

		if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
			if(!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
				overallResult[[NAME]] <- replaceTreatmentNames(overallList, overallResult[[NAME]])
			} else {
				overallResult[[NAME]] <- replaceTreatmentNames(overallList, overallResult[[NAME]], onlySecondTreatment = TRUE)
				overallResult[[PRIMAER.TREATMENT]] <- replaceTreatmentNames(overallList, overallResult[[PRIMAER.TREATMENT]], onlyFirstTreatment = TRUE)
			}
		} else {
			overallResult[[NAME]] <- replaceTreatmentNames(overallList, overallResult[[NAME]], onlyFirstTreatment = TRUE)
		}
	}
	return(overallResult)
}


replaceTreatmentNames <- function(overallList, columnWhichShouldReplace, onlyFirstTreatment=FALSE, onlySecondTreatment=FALSE, oneValue=FALSE) {
##########
#columnWhichShouldReplace <- overallResult$name
#onlyFirstTreatment <- TRUE
#onlySecondTreatment <- TRUE
##########

#print(columnWhichShouldReplace)
#print(onlyFirstTreatment)
#print(onlySecondTreatment)

	overallList$debug %debug% "replaceTreatmentNames()"
	
	columnWhichShouldReplace <- as.character(columnWhichShouldReplace)
	
	if (overallList$filterSecondTreatment[1] != NONE && !onlyFirstTreatment && !onlySecondTreatment) {
		if(oneValue) {
			columnWhichShouldReplace <- paste(overallList$filterTreatmentRename[[columnWhichShouldReplace]],"/", overallList$secondFilterTreatmentRename[[columnWhichShouldReplace]], sep="")
		} else {
			for(n in overallList$filterTreatment) {
				for(k in overallList$filterSecondTreatment) {
					columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==paste(n,"/",k, sep=""), paste(overallList$filterTreatmentRename[[n]],"/", overallList$secondFilterTreatmentRename[[k]], sep=""))
				}
			} 
		}
	} 
	
	if (overallList$filterTreatment[1] != NONE && onlyFirstTreatment) {
		if(oneValue) {
			columnWhichShouldReplace <- overallList$filterTreatmentRename[[columnWhichShouldReplace]]
		} else {
			for(n in overallList$filterTreatment) {
				columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==n, overallList$filterTreatmentRename[[n]])
			}
		}
	}
	
	if (overallList$filterSecondTreatment[1] != NONE && onlySecondTreatment) {
		if(oneValue) {
			columnWhichShouldReplace <- overallList$secondFilterTreatmentRename[[columnWhichShouldReplace]]
		} else {
			for(n in overallList$filterSecondTreatment) {
				columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace==n, overallList$secondFilterTreatmentRename[[n]])
			}
		}
	}
	#ownCat(unique(columnWhichShouldReplace))
	return(as.factor(columnWhichShouldReplace))
}

createOuputOverview <- function(typ, actualImage, maxImage, imageName) {
	typString <- ""

	if(typ == NBOX.PLOT) {
		typString <- "line plot"
	} else if (typ == STACKBOX.PLOT) {
		typString <- "stacked barplot"
	} else if(typ == BOX.PLOT) {
		typString <- "box plot"
	} else if(typ == SPIDER.PLOT) {
		typString <- "spider/linerange plot"
	} else if(typ == VIOLIN.PLOT) {
		typString <- "violin plot"
	}
	
	
	
	ownCat(paste("Create ", typString, " ", actualImage, "/", maxImage, ": '",imageName, "'", sep=""))
	
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
	
	if (length(grep("\\.\\.",label, ignore.case=TRUE)) > 0){
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
	label <- str_trim(label)

	if(nchar(label) > 4) {
		if(substring(tolower(label), 1,4) == "top ") {
			label <- paste(substring(label, 4), "top", sep=" - ")
		}
	}
	
	if(nchar(label) > 5) {
		if(substring(tolower(label), 1,5) == "side ") {
			label <- paste(substring(label, 5), "side", sep=" - ")
		}
	}

	return(label)		
}

cleanSubtitle <- function(label) {
	
	#print(label)
	label <- sub("\\(g\\)","",label)
	label <- sub("\\(mm\\)","",label)
	label <- sub("\\(mm\\^2\\)","",label)	
	label <- sub("\\(%\\)", "", label)
	label <- sub("\\(px\\)", "", label)
	label <- sub("\\(px\\^3\\)", "", label)
	label <- sub("\\(c/p\\)", "", label)
	label <- sub("\\(relative/px\\)", "", label)
	label <- sub("\\(relative intensity/pixel\\)", "", label)
	label <- sub("\\(percent/day\\)", "", label)
	
	label <- str_trim(label)
	#print(label)
	return(label)
}

checkFileName <- function(filename, extraString) {
	filename <- preprocessingOfValues(paste(filename, extraString, sep=""), FALSE, replaceString = "_")
	if(nchar(filename) > 90) {
		filename <- substr(filename, 1, 90)
	}
	return(filename)
}

writeTheData  <- function(overallList, plot, fileName, extraString, writeLatexFileFirstValue="", subSectionTitel="", makeOverallImage=FALSE, isAppendix=FALSE, subsectionDepth=1, typOfPlot = "") {
	#writeLatexFileSecondValue="",
	overallList$debug %debug% "writeTheData()"		

	fileName <- checkFileName(fileName, extraString)
	
	if (subSectionTitel != "") {
		subSectionTitel <- parseString2Latex(subSectionTitel)
	}

#	if(typOfPlot == LINERANGE.PLOT || (overallList$split.Treatment.First && overallList$split.Treatment.Second && typOfPlot == SPIDER.PLOT)) {
	if(typOfPlot == LINERANGE.PLOT || typOfPlot == SPIDER.PLOT) {
		saveImageFile(overallList, plot, fileName, 12)
	} else {
		saveImageFile(overallList, plot, fileName)
	}
	
	if (makeOverallImage) {
		if (subSectionTitel != "") {
			writeLatexFile(writeLatexFileFirstValue, fileName, ylabel=subSectionTitel, subsectionDepth=subsectionDepth, saveFormatImage = overallList$saveFormat)	
		} else {
			writeLatexFile(writeLatexFileFirstValue, fileName, saveFormatImage = overallList$saveFormat)
		}
	} 
	
#	else {
#		writeLatexFile(fileName, writeLatexFileSecondValue)	
#	}
#print(isAppendix)
	if (isAppendix) {
		if (subSectionTitel != "") {
			writeLatexFile("appendixImage", fileName, ylabel=subSectionTitel, subsectionDepth=subsectionDepth)
		} else {
			writeLatexFile("appendixImage", fileName)
		}
	}
}

loadLibs <- function(installAndUpdate = FALSE) {
	libraries  <- c(
		  "Cairo", "RColorBrewer", "data.table", "ggplot2",
		 "fmsb", "methods", "grid", "snow", "snowfall", "stringr") #, "mvoutlier")
	loadInstallAndUpdatePackages(libraries, installAndUpdate, installAndUpdate, FALSE)
}

myBreaks <- function(value){
	minV <- min(value)
	maxV <- max(value)
	
	if((maxV - minV) <= 10) {
		steps <- 1
	} else if((maxV - minV) > 10 && (maxV - minV) < 30) {
		steps <- 5
	} else {
		steps <- 10
	}
	
	breaks <- seq(minV, maxV, steps)
	names(breaks) <- attr(breaks,"labels")
	return(breaks)
}

shapeTransparence <- function(column) {
	
	alpha <- 0.1
	
	numberOfDescriptors <- length(unique(column))
	
	if(numberOfDescriptors > 5) {
		alpha <- 0.1 - (numberOfDescriptors - 5) * 0.0036
	}
	
	if(alpha < 0) {
		alpha = 0
	}
	
	return(alpha)
}

makeLinearDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title = "") {
#############
#overallResult <- overallResultSplit
#title <- nn
#title <- ""	
############	
	
#  c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")	
#	overallList$stress.Start <- c(10,20,30,37)
#	overallList$stress.End <- c(13, 23, 33, 40)
#	overallList$stress.Typ <- c("001","001","004", "003")
#	overallList$stress.Label <- c(-1, -1, -1, -1)
#	
#	overallList$stress.Start <- c(10,20,30)
#	overallList$stress.End <- c(13, 23,33)
#	overallList$stress.Typ <- c("002","001","002")
#	overallList$stress.Label <- c(-1, -1,-1)
	
	overallList$debug %debug% "makeLinearDiagram()"	
	ylabelForAppendix <- ""
	stressArea <- data.frame()
	
	overallFileName <- overallList$imageFileNames_nBoxplots
	overallColor <- overallList$color_nBox
	color <- overallColor[[imagesIndex]]
	
	if(overallList$stress.Start != -1) {
		stressArea <- buildStressArea(overallList$stress.Start, overallList$stress.End, overallList$stress.Typ, overallList$stress.Label, overallResult$mean, typOfPlot, overallResult$se)
		color <- addColorForStressPhaseAndOther(stressArea, color, typOfPlot)
	}

	#isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$split.Treatment.First, overallList$split.Treatment.Second)
	
	if (length(overallResult[, 1]) > 0) {
	
		if (!CheckIfOneColumnHasOnlyValues(overallResult)) {
			whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, colnames(overallResult), typOfPlot)
		#print(whichColumShouldUse)
			overallResult <- cbind(overallResult, ymin=overallResult$mean-overallResult$se, ymax=overallResult$mean+overallResult$se)
			
			reorderList <- reorderThePlotOrder(overallResult, typOfPlot, whichColumShouldUse)
			overallResult <- reorderList$overallResult
#			print(whichColumShouldUse)
#			print(head(overallResult))
			plot <-	ggplot() 
					
			if(length(stressArea) >0) {
				plot <- plot + 
						geom_rect(data=stressArea, aes(xmin=xmin, xmax=xmax, ymin=ymin, ymax=ymax, fill=typ)) +
						#geom_text(data=stressArea, aes(x=xmin, y=(ymax-ymax*0.15), label=label), size=2, hjust=0, vjust=1, angle = 90, colour="grey")
						geom_text(data=stressArea, aes(x=xmin, y=ymin, label=label), size=2, hjust=0, vjust=1, angle = 90, colour="grey")
				
			}	
			
			if ((length(grep("%/day",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0 || 
						overallList$isRatio || 
						length(grep("relative",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0 || 
						length(grep("average",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0) &&
						(length(grep("blue marker",overallDesName[[imagesIndex]], ignore.case=TRUE)) <= 0)) {
					plot <- plot + 
							geom_smooth(data=overallResult, aes_string(x="xAxis", y="mean", shape=whichColumShouldUse, ymin="ymin", ymax="ymax", colour=whichColumShouldUse, fill=whichColumShouldUse), method="loess", stat="smooth", alpha=shapeTransparence(overallResult[[whichColumShouldUse]]))
#				if(isOtherTyp) {
#					plot <- plot +
#							geom_smooth(data=overallResult, aes(x=xAxis, y=mean, shape=primaerTreatment, ymin=mean-se, ymax=mean+se, colour=primaerTreatment, fill=primaerTreatment), method="loess", stat="smooth", alpha=0.1)
#				} else {
#					plot <- plot +
#							geom_smooth(data=overallResult, aes(x=xAxis, y=mean, shape=name, ymin=mean-se, ymax=mean+se, colour=name, fill=name), method="loess", stat="smooth", alpha=0.1) 
#				}
						
						#plot <- plot + geom_ribbon(aes(ymin=mean-se, ymax=mean+se, fill=name), alpha=0.1)
			} else if(length(grep("blue marker",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0) {
				plot <- plot + 
						geom_smooth(data=overallResult, aes(x=xAxis, y=mean, ymin=ymin, ymax=ymax), method="loess", stat="smooth", alpha=shapeTransparence(overallResult[[whichColumShouldUse]]))	
			} else {
				plot <- plot + 
						#plot <-	ggplot()+
						geom_ribbon(data=overallResult, aes_string(x="xAxis", y="mean", ymin="ymin", ymax="ymax", fill=whichColumShouldUse), stat="identity", alpha=shapeTransparence(overallResult[[whichColumShouldUse]])) +
						geom_line(data=overallResult, aes_string(x="xAxis", y="mean", colour=whichColumShouldUse), alpha=0.95)
						#geom_point(data=overallResult, aes_string(x="xAxis", y="mean", color=whichColumShouldUse), size=3)
				#print(plot)
#				if(isOtherTyp) {
#					plot <- plot + 
#	#						geom_ribbon(data=overallResult, aes(x=xAxis, y=mean, shape=name, ymin=mean-se, ymax=mean+se, fill=name), stat="identity", alpha=0.1) +
#	#						geom_line(data=overallResult, aes(x=xAxis, y=mean, color=name), alpha=0.2)
#						geom_ribbon(data=overallResult, aes(x=xAxis, y=mean, shape=primaerTreatment, ymin=mean-se, ymax=mean+se, fill=primaerTreatment), stat="identity", alpha=0.1) +
#						geom_line(data=overallResult, aes(x=xAxis, y=mean, color=primaerTreatment), alpha=0.2)
#				} else {
#					plot <- plot + 
#							#						geom_ribbon(data=overallResult, aes(x=xAxis, y=mean, shape=name, ymin=mean-se, ymax=mean+se, fill=name), stat="identity", alpha=0.1) +
#							#						geom_line(data=overallResult, aes(x=xAxis, y=mean, color=name), alpha=0.2)
#							geom_ribbon(data=overallResult, aes(x=xAxis, y=mean, shape=name, ymin=mean-se, ymax=mean+se, fill=name), stat="identity", alpha=0.1) +
#							geom_line(data=overallResult, aes(x=xAxis, y=mean, color=name), alpha=0.2)
#				}
			}
			#print(plot)
			
			if(length(grep("blue marker",overallDesName[[imagesIndex]], ignore.case=TRUE)) <= 0) {
					plot <- plot +	
							geom_point(data=overallResult, aes_string(x="xAxis", y="mean", colour=whichColumShouldUse, shape=whichColumShouldUse), size=3)
#				if(isOtherTyp) {
#					plot <- plot +	
#							geom_point(data=overallResult, aes(x=xAxis, y=mean, color=primaerTreatment), size=3)
#				} else {
#					plot <- plot +	
#							geom_point(data=overallResult, aes(x=xAxis, y=mean, color=name), size=3)
#				}
			} else {
				plot <- plot +	
						geom_point(data=overallResult, aes(x=xAxis, y=mean), size=3)
			}
							#ownCat("drinne")
#							ownCat(overallResult$xAxis)
#							ownCat(min(as.numeric(as.character(overallResult$xAxis))))
#							ownCat(max(as.numeric(as.character(overallResult$xAxis))))
			plot <-  plot + 
					#scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis))), limits=min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis))))					
					scale_x_continuous(name=overallList$xAxisName, breaks=myBreaks(as.numeric(as.character(overallResult$xAxis))))		
					if (overallList$appendix) {
						ylabelForAppendix <- renameY(overallDesName[[imagesIndex]])
						plot <- plot + 
								ylab(ylabelForAppendix)
					} else {
						plot <- plot + 
								ylab(overallDesName[[imagesIndex]])
					}
					
			colorReorder <- overallColor[[imagesIndex]][reorderList$sortList]
			shapeReorder <- c(1:length(overallColor[[imagesIndex]]))[reorderList$sortList]
								
			
			plot <- plot +
					scale_fill_manual(values = color, guide="none") +
					scale_colour_manual(values= colorReorder) +
					scale_shape_manual(values = shapeReorder) +
					theme_bw()
					
			if((overallList$secondTreatment == NONE && overallList$split.Treatment.First) || 
				(overallList$split.Treatment.First && overallList$split.Treatment.Second)) {
				plot <- plot +
						opts(legend.position = "none")
			} else {
				plot <- plot +
						opts(legend.position = "right")
			}	
			plot <- plot +	
					opts(axis.title.x = theme_text(face="bold", size=11), 
							axis.title.y = theme_text(face="bold", size=11, angle=90), 
							#panel.grid.major = theme_blank(), # switch off major gridlines
							#panel.grid.minor = theme_blank(), # switch off minor gridlines
							#legend.position = "right", # manually position the legend (numbers being from 0, 0 at bottom left of whole plot to 1, 1 at top right)
							legend.title = theme_blank(), # switch off the legend title						
							#legend.key.size = unit(1.5, "lines"), 
							legend.key = theme_blank(), # switch off the rectangle around symbols in the legend
							panel.border = theme_rect(colour="Grey", size=0.1)
					) 
					#+ guides(colour = guide_legend("none"))
			plot <- setFontSize(plot, overallColor[[imagesIndex]], typOfPlot)
					
			plot <-  plot + guides(
					shape=guide_legend(ncol=calculateLegendRowAndColNumber(overallColor[[imagesIndex]], typOfPlot),
							byrow=FALSE),
					colour=guide_legend(ncol=calculateLegendRowAndColNumber(overallColor[[imagesIndex]], typOfPlot),
							byrow=FALSE)
			)
			
#			if (length(overallColor[[imagesIndex]]) > 18 & length(overallColor[[imagesIndex]]) < 31) {
#				plot = plot + 
#					   opts(legend.text = theme_text(size=6),
#							legend.key.size = unit(0.7, "lines"),
#							strip.text.x = theme_text(size=6)
#						)
#			} else if (length(overallColor[[imagesIndex]]) >= 31) {
#				plot = plot + 
#					   opts(legend.text = theme_text(size=4),
#							legend.key.size = unit(0.4, "lines"),
#							strip.text.x = theme_text(size=4)
#						)
#			} else {
#				plot <- plot + 
#					   opts(legend.text = theme_text(size=11),
#							strip.text.x = theme_text(size=11)
#						)
#			}
			
			if(FALSE) {
				if (title != "") {
					plot = plot + opts(title = title)
				}
			}
			
			if(length(grep("blue marker",overallDesName[[imagesIndex]], ignore.case=TRUE)) <= 0) {
								
				if (!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
					# no facet_wrap!
				} else if (overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
					
					if(PRIMAER.TREATMENT %in% colnames(overallResult)) {
						plot <- plot + 
								facet_wrap(~ primaerTreatment)
					} else {
						plot <- plot + 
								facet_wrap(~ name)
					}
				} else {
					plot <- plot + 
							facet_wrap(~ name)
				}
			}
								
			#print(plot)

			subtitle <- ""
			overallImage <- TRUE
			
			if(title != "") {
				sep <- " - "
			} else {
				sep <- ""
			}
			
			if (!overallList$appendix) {
				if (overallList$split.Treatment.First && overallList$split.Treatment.Second) {
					subtitle <- paste(cleanSubtitle(overallDesName[[imagesIndex]]), title, sep=sep)
					
					if((length(grep("convex",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0) ||
					   (length(grep("maximum extension",overallDesName[[imagesIndex]], ignore.case=TRUE)) > 0)){
						subsectionDepth <- 3
					} else if((length(grep("_vis_hsv_",overallFileName[[imagesIndex]], ignore.case=TRUE)) > 0)) {
						subsectionDepth <- 4
					} else {
						subsectionDepth <- 2
					}
				}
			} else {
				subtitle <- paste(cleanSubtitle(ylabelForAppendix), title, sep=sep)
					
				subsectionDepth <- 1
				overallImage <- FALSE
			}
			#print(subtitle)
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
																																																						
			writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(title, typOfPlot, sep=""), paste(overallFileName[[imagesIndex]], typOfPlot, "OverallImage", sep=""), subtitle, overallImage, isAppendix=overallList$appendix, subsectionDepth=subsectionDepth)
					
		} else {
			ownCat("Only one column has values, create barplot!")
	
			day = overallResult$xAxis[!is.na(overallResult$mean)][1]
			tempXaxisName = overallList$xAxisName
			overallList$xAxisName = paste(overallList$xAxisName, day)
			#overallList$overallResult = overallList$overallResult[!is.na(overallList$overallResult$mean), ]
			makeBarDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, TRUE)
			overallList$xAxisName = tempXaxisName
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

getTheColumWhichShouldUse <- function(colNames) {

	if (PRIMAER.TREATMENT %in% colNames) {
		return(PRIMAER.TREATMENT)
	} else {
		return(NAME)
	}
}

setFontSize <- function(plot, value, typOfPlot) {
	
	
	if(typOfPlot == STACKBOX.PLOT) {
		if(PRIMAER.TREATMENT %in% colnames(value)) {
			numerOfDescriptors <- length(unique(value[[PRIMAER.TREATMENT]]))
		} else {
			numerOfDescriptors <- length(unique(value[[NAME]]))
		}
	} else if(typOfPlot == NBOX.PLOT) {
		numerOfDescriptors <- length(value)
	}
	#print(numerOfDescriptors)
	
	if(numerOfDescriptors > 50) {
		if(typOfPlot == STACKBOX.PLOT) {
			plot <- plot + 
					opts(strip.text.x = theme_text(size=5))
		} else if(typOfPlot == NBOX.PLOT) {
			plot = plot + 
					opts(legend.text = theme_text(size=4),
							legend.key.size = unit(0.1, "lines"),
							strip.text.x = theme_text(size= 4)
					)
			
			#grid.gedit(size=unit(3, "mm"), "key.points", grep=T) 
		}
	} else if(numerOfDescriptors > 30) {
		if(typOfPlot == STACKBOX.PLOT) {
			plot <- plot + 
					opts(strip.text.x = theme_text(size=6))
		} else if(typOfPlot == NBOX.PLOT) {
			plot = plot + 
					opts(legend.text = theme_text(size=5),
						 legend.key.size = unit(0.5, "lines"),
						 strip.text.x = theme_text(size=6)
					)
		}
	} else if(numerOfDescriptors > 12 && numerOfDescriptors <= 30) {
		if(typOfPlot == STACKBOX.PLOT) {
			plot <- plot + 
					opts(strip.text.x = theme_text(size=8))
		} else if(typOfPlot == NBOX.PLOT) {
			plot = plot + 
					opts(legend.text = theme_text(size=6),
						 legend.key.size = unit(0.7, "lines"),
						 strip.text.x = theme_text(size=8)
					)
		}
	} 
	

	return(plot)
}

#plotStackedImage <- function(overallResult, overallDesName, overallList, imagesIndex, title = "", legende=TRUE, minor_breaks=FALSE) {
makeStackedDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
#####
#legende <- TRUE
#minor_breaks <- FALSE
#overallResult <- plotThisValues
#positionType <- overallList$stackedBarOptions$typOfGeomBar[2]
#title <- ""
#title <- nn
#####


#makeStress <- FALSE


#  c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")	
#	overallList$stress.Start <- c(10,20,30,37)
#	overallList$stress.End <- c(13, 23, 33, 40)
#	overallList$stress.Typ <- c("001","001","004", "003")
#	overallList$stress.Label <- c(-1, -1, -1, -1)
#	
#	overallList$stress.Start <- c(10,20,30)
#	overallList$stress.End <- c(13, 23,33)
#	overallList$stress.Typ <- c("002","001","002")
#	overallList$stress.Label <- c(-1, -1,-1)


	overallList$debug %debug% "makeStackedDiagram()"	
	
	legende <- TRUE
	overallColor <- overallList$color_boxStack
	overallFileName <- overallList$imageFileNames_StackedPlots	
	
	if (length(overallResult[, 1]) > 0) {
	
		isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$split.Treatment.First, overallList$split.Treatment.Second)
		
		if (length(overallList$stackedBarOptions$typOfGeomBar) == 0) {
			overallList$stackedBarOptions$typOfGeomBar = c("fill")
		}
		
		overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, colnames(overallResult), typOfPlot)
		reorderList <- reorderThePlotOrder(overallResult, typOfPlot, whichColumShouldUse)
		overallResult <- reorderList$overallResult
#		if ("primaerTreatment" %in% colnames(overallResult)) {
#			overallResult$primaerTreatment <-  replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
#		} else {
#			overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name, TRUE)
#		}
		
		for (positionType in overallList$stackedBarOptions$typOfGeomBar) {			
			makeStress <- TRUE
			stressArea <- data.frame()	
			colorWithoutStress <- getColor(overallColor[[imagesIndex]], overallResult)
			if(positionType == "stack") {
				makeStress <- FALSE
			}
			
			if(overallList$stress.Start != -1 && positionType != "fill" && makeStress) {
				stressArea <- buildStressArea(overallList$stress.Start, overallList$stress.End, overallList$stress.Typ, overallList$stress.Label, overallResult$values, typOfPlot, positionType, overallResult[, c("xAxis", getTheColumWhichShouldUse(colnames(overallResult)))])
				color <- addColorForStressPhaseAndOther(stressArea, colorWithoutStress, positionType)
			}
#			print(stressArea)
#			print(overallList$stress.Start)
			plot <- ggplot()
			
			if(length(stressArea) >0) {
				plot <- plot + 
						geom_rect(data=stressArea, aes(xmin=xmin, xmax=xmax, ymin=ymin, ymax=ymax, fill=typ), guides="none") +
						geom_text(data=stressArea, aes(x=xmin, y=(ymax-ymax*0.15), label=label), size=2, hjust=0, vjust=1, angle = 90, colour="grey")
				
			}	
			
				if (positionType == "dodge") {				
					plot <- plot +
							geom_line(data= overallResult, aes(x=xAxis, y=values, colour=hist), position="identity") + 
							#scale_fill_manual(values = overallColor[[imagesIndex]]) +
							#scale_colour_manual(values= getColor(overallColor[[imagesIndex]], overallResult))
							scale_colour_manual(values= colorWithoutStress)
					
				} else {
					plot <- plot +
							geom_bar(data = overallResult, aes(x=xAxis, y=values, fill=hist), stat="identity", position = positionType)
							
				}
								
				if (positionType == "dodge" || positionType == "stack") {
					name <- sub("%", "px", overallDesName[[imagesIndex]])
				} else {
					name <- sub("(zoom corrected) ", "", overallDesName[[imagesIndex]])
				}

					plot <- plot + 
						   ylab(name) +
						   xlab(overallList$xAxisName)	
					
			if(positionType == "dodge" && length(stressArea) >0) {
				plot <- plot +
						scale_fill_manual(values = color[!(color %in% colorWithoutStress)], name="", labels = c(unique(as.character(stressArea$label))), guide="none")
			} else if(positionType == "stack" && length(stressArea) >0) {
#				if(length(stressArea) >0) {
					plot <- plot + 
							scale_fill_manual(values = color, name="", labels = c(unique(as.character(stressArea$label)),unique(as.character(overallResult$hist))))
#				} else {
#					plot <- plot +
#						scale_fill_manual(values = colorWithoutStress, name="")
#				}
			} else {
				plot <- plot +
					scale_fill_manual(values = colorWithoutStress, name="")
			}
				#	scale_fill_manual(values = color, name="") +
				#	scale_color_manual(value=c("red", "blue")) +
					#scale_fill_manual(values = getColor(overallColor[[imagesIndex]], overallResult), name="") +
			plot <- plot +
					theme_bw() +
					opts(axis.title.x = theme_text(face="bold", size=11), 
							axis.title.y = theme_text(face="bold", size=11, angle=90), 
							#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
							#panel.background = theme_rect(linetype = "dotted"), 
							panel.border = theme_rect(colour="Grey", size=0.1), 
							strip.background = theme_rect(colour=NA),
							#plot.title = theme_text(size=5) 
	#						plot.title = theme_rect(colour="Pink", size=0.1)
							panel.grid.minor = theme_blank()
					) 
			plot <- setFontSize(plot, overallResult, typOfPlot)
			
			if (!legende) {
				plot = plot + opts(legend.position="none")
			} else {
				plot = plot + opts(legend.position="right", 
									legend.title = theme_blank(), 
									legend.text = theme_text(size=11), 
									legend.key = theme_blank())
			}
			if(FALSE) {
				if (title != "") {
					plot = plot + opts(title = title)
				}
			}
						
			if (positionType == "fill") {
				plot = plot + scale_y_continuous(labels=seq(0, 100, 20), breaks=seq(0, 1, 0.2))
			}
			
		
#			if (!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
#				# no facet_wrap!
#			} else
#print(head(overallResult))
			if (!overallList$split.Treatment.First && overallList$split.Treatment.Second) {		
				if(PRIMAER.TREATMENT %in% colnames(overallResult)) {
					plot <- plot + 
							facet_wrap(~ primaerTreatment)
				} else {
					plot <- plot + 
							facet_wrap(~ name)
				}
			} else {
				plot <- plot + 
						facet_wrap(~ name)
			}
		
			
#			if ("primaerTreatment" %in% colnames(overallResult)) {				
#				plot = plot + facet_wrap(~ primaerTreatment)
#			} else {
#				plot = plot + facet_wrap(~ name)
#			}
			
		#	print(plot)
			
			subtitle <- ""
			if (positionType == overallList$stackedBarOptions$typOfGeomBar[1] || length(overallList$stackedBarOptions$typOfGeomBar) == 1) {
				subtitle <- title
			}
	
			
			writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste("overall", title, positionType, sep=""), paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep=""), subtitle, TRUE,subsectionDepth=2)
			
#			saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste("overall", title, positionType, sep=""))
#			if (makeOverallImage) {
#				if (title != "") {
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

removeNAsSpider <- function(overallResult, xAxisPosition) {
	overallResultStart <- overallResult[1:xAxisPosition]
	overallResult <- overallResult[(xAxisPosition+1):length(colnames(overallResult))]
	booleanVector <- !apply(overallResult,1,function(x)all(is.na(x)))
	
	return(cbind(overallResultStart[booleanVector,], overallResult[booleanVector,]))
}


openPlotDevice <- function(overallList, fileName, extraString, h) {
	if (h==1) {
		filename = preprocessingOfValues(paste(fileName, extraString, sep=""), FALSE, replaceString = "_")
		Cairo(width=10, height=7, file=paste(filename, overallList$saveFormat, sep="."), type=overallList$saveFormat, bg="transparent", units="in", dpi=as.numeric(overallList$dpi))
	}
}

closePlotDevice <- function(h) {
	if (h==1) {
		dev.off()
	}
}

transferIntoPercentValues <- function(overallResult, xAxisPosition) {
	overallResultCalulate <- overallResult[(xAxisPosition+1):(length(colnames(overallResult))-1)]
	overallResultCalulate <- (overallResultCalulate * 100) / max(overallResultCalulate)
	
	return(data.frame(overallResult[c(1:xAxisPosition)], overallResultCalulate, overallResult[length(colnames(overallResult))]))
}

checkIfShouldSplitAfterPrimaryAndSecondaryTreatment <- function(first, second) {
	isOtherTyp <- FALSE
	if(!first && second) {
		isOtherTyp <- TRUE
	}
	return(isOtherTyp)
}

normalizeEachDescriptor <- function(overallResult) {

	for(name in unique(overallResult$hist)) {
		overallResult[overallResult$hist == name,]$values <- sapply(overallResult[overallResult$hist == name,]$values,  function(x,y) {(x/y)}, y=max(overallResult[overallResult$hist == name,]$values))
	}
	return(overallResult)
}

makeSpiderDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	overallList$debug %debug% "makeSpiderDiagram()"		

	if (length(overallResult[, 1]) > 0) {
		test <- c("side fluo intensity", "side nir intensity", "side visible hue average value", "top visible hue average value")
		if (sum(!getVector(overallDesName[[imagesIndex]]) %in% test) > 1) {
			plotSpiderImage(overallList, overallResult, overallDesName, imagesIndex, typOfPlot, title)
		} 
		plotLineRangeImage(overallList, overallResult, overallDesName, imagesIndex, LINERANGE.PLOT, title)
	}
}	


#PreWorkForMakeBigOverallImageSpin <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, doSpiderPlot) {
########
##value <- overallList$filterSecondTreatment[1]
########
#	
#	overallList$debug %debug% "PreWorkForMakeBigOverallImageSpin()"	
#	
#	overallFileName <- overallList$imageFileNames_SpiderPlots
#	overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult$xAxis, overallList$spiderOptions)
#	overallResult <- na.omit(overallResult)
#	overallResult <- normalizeEachDescriptor(overallResult)	
#	groupBy = groupByFunction(list(overallList$treatment, overallList$secondTreatment))
#
#	if (length(groupBy) == 0 || length(groupBy) == 1) {
#		
#		if (doSpiderPlot) {
#			plotSpiderImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex= imagesIndex, overallFileName =overallFileName, typOfPlot=typOfPlot)	
#		}
#		plotLineRangeImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex= imagesIndex, overallFileName =overallFileName, typOfPlot="lineRangePlot")	
#	} else {
#		for (value in overallList$filterSecondTreatment) {			
#			title = overallList$secondFilterTreatmentRename[[value]]
#			#plottedName = overallList$filterTreatment %contactAllWithAll% value
#			#booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = plottedName))
#			booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = value))
#			plotThisValues = overallResult[booleanVector, ]
##			usedOverallColor <- overallColor[[imagesIndex]][1:length(unique(plotThisValues["primaerTreatment"])[,1])]
##			overallColor[[imagesIndex]] <- overallColor[[imagesIndex]][(length(unique(plotThisValues["primaerTreatment"])[,1])+1):length(overallColor[[imagesIndex]])]
#			
#			if (doSpiderPlot) {
#				plotSpiderImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex=imagesIndex, overallFileName=overallFileName, typOfPlot=typOfPlot)
#			}
#			plotLineRangeImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende=TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex=imagesIndex, overallFileName=overallFileName, typOfPlot="lineRangePlot")
#		}	 
#	}
#}


plotSpiderImage <- function(overallList, overallResult, overallDesName, imagesIndex, typOfPlot,  title, legende=TRUE) {
################
##overallColor <- usedOverallColor 
##	typOfPlot <- "spiderplot"
##	makeOverallImage = TRUE
##	usedoverallColor = overallColor[[imagesIndex]]
##	overallResult <- plotThisValues
#legende=TRUE
#positionType <- overallList$spiderOptions$typOfGeomBar[1]
#title <- nn
#title <- ""
#overallResult <- overallResultSplit
#################

	overallList$debug %debug% "plotSpiderImage()"	
	if (length(overallResult[, 1]) > 0) {
			
		overallFileName <- overallList$imageFileNames_SpiderPlots
		options <- overallList$spiderOptions

		overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName,  overallResult[c("xAxis","values")], "values", options)
		overallResult <- na.omit(overallResult)
		overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
		overallResult <- normalizeEachDescriptor(overallResult)	
		overallColor <- overallList$color_spider
		
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, colnames(overallResult), typOfPlot)
		histVec <- levels(overallResult$hist)
		for(kk in seq(along=histVec)) {
			histVec[kk] <- paste(kk,histVec[kk])
		}
		overallResult$hist <- factor((overallResult$hist), levels((overallResult$hist)), seq(along=unique(overallResult$hist)))
		nameString <- unique(as.character(overallResult[[whichColumShouldUse]]))
#		print(histVec)
#		print(usedoverallColor)
		for (positionType in options$typOfGeomBar) {			
 		
		
#			if (!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
#				overallResult <- cbind(overallResult,plot=rep.int(1, length(overallResult[,1])))
#				plot <- ggplot(overallResult, aes(plot, value, fill=plot))
#				#} else if (overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
#			} else {
				#print(head(overallResult))
				plot <- ggplot(overallResult, aes_string(x="hist", y="values", group=whichColumShouldUse, shape=whichColumShouldUse, color=whichColumShouldUse, fill="hist")) +
						geom_point(size=3) +
						geom_line()  
			#}

		#	print(plot)
	
#			if ("primaerTreatment" %in% colnames(overallResult)) {
#				overallResult$primaerTreatment <-  replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
#				nameString <- unique(as.character(overallResult$primaerTreatment))
#				
##				plot = ggplot(data=overallResult, aes(x=hist, y=values, group=primaerTreatment)) +
##						geom_point(aes(color=as.character(primaerTreatment), shape=hist), size=3) +
##						geom_line(aes(colour=as.character(primaerTreatment))) 
#				
#				plot = ggplot(data=overallResult, aes(x=hist, y=values, group=as.character(primaerTreatment), shape=as.character(primaerTreatment), color=as.character(primaerTreatment), fill=hist)) +
#						geom_point(size=3) +
#						geom_line()  
#			} else {
#				overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name, TRUE)
#				nameString <- unique(as.character(overallResult$name))
#				
#				plot = ggplot(data=overallResult, aes(x=hist, y=values, group=as.character(name), color=as.character(name), shape=as.character(name), fill=hist)) +
#						geom_point(size=3) +
#						geom_line() 
#			}
	
			plot <- plot +
					#geom_point(aes(color=as.character(name), shape=hist), size=3) +
#					scale_shape_manual(values = c(1:length(unique(overallResult$hist))), name="Property") +
					#scale_shape_manual(values = c(1:length(unique(as.character(overallResult$primaerTreatment)))), name="Property") +
					scale_shape_manual(values = c(1:length(nameString)), name="Property") +
					#geom_line(aes(colour=as.character(name))) +
				#	scale_colour_manual(name="Condition", values=usedoverallColor)
					scale_colour_manual(values=overallColor[[imagesIndex]], name="Property") + 
					scale_fill_manual(values=rep.int("black",length(histVec)), name="Condition", breaks=unique(overallResult$hist), labels=histVec)
			#print(plot)
			if (positionType == "x") {			
				#plot <- plot + coord_polar(theta="x", expand=TRUE)
				plot <- plot + coord_polar(theta="x")
			} else {
				#plot <- plot + coord_polar(theta="y", expand=TRUE)
				plot <- plot + coord_polar(theta="y")
			}
			#print(plot)
				plot <- plot + 
						scale_y_continuous() +
						theme_bw() +
						opts(#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), # Rand geht nicht in ggplot 0.9
								axis.title.x = theme_blank(), 
								axis.title.y = theme_blank(),
#								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1)
								#axis.text.x = theme_blank(),
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
							   legend.direction="horizontal",
							   legend.position="bottom",
							   legend.box = "vertical",
							   #legend.position=c(0.5,0),
							  # legend.title = theme_blank(),
							   legend.key = theme_blank()
			   			)			
				
			
#				if(overallList$split.Treatment.First && !overallList$split.Treatment.Second && overallList$secondTreatment == "none") {
#					plot <-  plot + guides(fill=guide_legend(title.position= "top", 
#									ncol=calculateLegendRowAndColNumber(histVec), 
#									byrow=FALSE)
#					)
#				} else {
					plot <-  plot + guides(
							shape=guide_legend(title.position= "top", 
									ncol=calculateLegendRowAndColNumber(nameString, typOfPlot),
									byrow=FALSE),
							fill=guide_legend(title.position= "top", 
									ncol=calculateLegendRowAndColNumber(histVec, typOfPlot), 
									byrow=FALSE)
					)
				#}
				
			}		
			
#			if (title != "") {
#				plot = plot + opts(title = title)
#			}
			
			if(overallList$split.Treatment.First && overallList$split.Treatment.Second) {
				if(whichColumShouldUse == NAME) {
					plot = plot + facet_grid(name ~ xAxisfactor)
				} else {
					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
				}
				
			} else {
				plot = plot + facet_grid(~ xAxisfactor)
			}
#				if ("primaerTreatment" %in% colnames(overallResult)) {				
#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
#					
#				} else {
#					plot = plot + facet_grid(name ~ xAxisfactor)
#				}

#			print("drinne")
			#print(plot)
			
			subtitle <- ""
			if (positionType == options$typOfGeomBar[1] || length(options$typOfGeomBar) == 1) {
				subtitle <- title
			}
			
			writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(typOfPlot, title, positionType, sep=""), paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep=""), subtitle, TRUE, subsectionDepth=2, typOfPlot=typOfPlot)
																													
#			saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste(typOfPlot, title, positionType, sep=""))
#			if (makeOverallImage) {
#				if (title != "") {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep=""), paste(overallFileName[[imagesIndex]], typOfPlot, title, positionType, sep=""), TRUE, title)	
#				} else {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep=""), paste(overallFileName[[imagesIndex]], typOfPlot, title, positionType, sep=""))
#				}
#			} else {
#				writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], typOfPlot, positionType, title, sep=""))	
#			}

		}
	}				
}

calculateLegendRowAndColNumber <- function(legendText, typOfPlot) {
########	
#legendText <- unique(overallResult$name)
#legendText <- unique(overallResult$hist)	
#######	
	legendText <- as.character(getVector(legendText))
	
	if(typOfPlot == NBOX.PLOT) {
		ncol <- ceiling(length(legendText) / 40)
	} else {
		lengthOfOneRow <- 90
	
		averageLengthOfSet <- round(sum(nchar(legendText),na.rm=TRUE) / length(legendText))
	
		ncol <- floor(lengthOfOneRow / averageLengthOfSet) -1
		if (ncol == 0) {
			ncol <- 1
		}
	}
	return(ncol)
} 

plotLineRangeImage <- function(overallList, overallResult, overallDesName, imagesIndex, typOfPlot,  title, legende=TRUE) {
	################
##	makeOverallImage = TRUE
##	legende=TRUE
##	usedoverallColor <- overallColor[[imagesIndex]]
##	overallResult <- plotThisValues
##	positionType <- overallList$spiderOptions$typOfGeomBar[1]
#typOfPlot <- "lineRangePlot"
#legende=TRUE
##positionType <- overallList$spiderOptions$typOfGeomBar[1]
#title <- nn
#title <- ""
#overallResult <- overallResultSplit
	#################
	
	#ownCat(overallResult[1,])
#tempoverallResult <- overallResult
#overallResult <- tempoverallResult
	overallList$debug %debug% "plotLineRangeImage()"

	
	
	if (length(overallResult[, 1]) > 0) {
		overallFileName <- overallList$imageFileNames_SpiderPlots
		options <- overallList$spiderOptions
		overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult[c("xAxis","values")], "values", options)
		overallResult <- na.omit(overallResult)
		overallResult <- normalizeEachDescriptor(overallResult)
		overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
		overallColor <- overallList$color_spider
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, colnames(overallResult), typOfPlot)
		nameString <- unique(as.character(overallResult[[whichColumShouldUse]]))
		
#		if ("primaerTreatment" %in% colnames(overallResult)) {
#			overallResult$primaerTreatment <-  replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
#			nameString <-  unique(as.character(overallResult$primaerTreatment))
#		} else {
#			overallResult$name <-  replaceTreatmentNames(overallList, overallResult$name, TRUE)
#			nameString <-  unique(as.character(overallResult$name))
#		}
		#print(nameString)
		
		
		plot <- ggplot(data=overallResult, aes(x=hist, y=values)) +
				geom_line() +
				geom_point(aes_string(color=whichColumShouldUse, shape=whichColumShouldUse, fill=whichColumShouldUse), size=3)
		#print(plot)
#		if ("primaerTreatment" %in% colnames(overallResult)) {				
#			plot <- plot + geom_point(aes(color=as.character(primaerTreatment), shape=as.character(primaerTreatment)), size=3)
#			
#		} else {
#			plot <- plot + geom_point(aes(color=as.character(name), shape=as.character(name)), size=3)
#		}
		#print(plot)
		#print(usedoverallColor)
		plot <- plot +
				scale_colour_manual(values=overallColor[[imagesIndex]]) +
				#scale_fill_manual(values=overallColor[[imagesIndex]]) +
				scale_shape_manual(values = c(1:length(nameString))) +
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

			plot <-  plot + guides(colour=guide_legend(title.position= "top", 
									ncol=calculateLegendRowAndColNumber(nameString, typOfPlot),
									byrow=T)			
							) 
			
#			if (length(overallColor[[imagesIndex]]) > 3 & length(overallColor[[imagesIndex]]) < 6) {
#				size <- 6
#				unit <- 0.7
#			} else if (length(overallColor[[imagesIndex]]) >= 6) {
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
		if(FALSE) {
			if (title != "") {
				plot = plot + 
						opts(title = title)
			}
		}
		
		
		if(overallList$split.Treatment.First && overallList$split.Treatment.Second) {
			if(whichColumShouldUse == NAME) {
				plot = plot + facet_grid(name ~ xAxisfactor)
			} else {
				plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
			}
			
		} else {
			plot = plot + facet_grid(~ xAxisfactor)
		}
#				if ("primaerTreatment" %in% colnames(overallResult)) {				
#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
#					
#				} else {
#					plot = plot + facet_grid(name ~ xAxisfactor)
#				}
		
		
		#print(plot)

		writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(typOfPlot, title, sep=""), paste(overallFileName[[imagesIndex]], "lineRangeOverallImage", sep=""), title, TRUE, subsectionDepth=2, typOfPlot=typOfPlot)

#		saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste(typOfPlot, title, sep=""))
#		if (makeOverallImage) {
#			writeLatexFile(paste(overallFileName[[imagesIndex]], "lineRangeOverallImage", sep=""), paste(overallFileName[[imagesIndex]], typOfPlot, title, sep=""))	
#		} else {
#			writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], typOfPlot, title, sep="_"))	
#		}			
		
	}		
}

makeBarDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, isOnlyOneValue = FALSE) {
	overallList$debug %debug% "makeBarDiagram()"	
	
	overallFileName <- overallList$imageFileNames_nBoxplots
	overallColor <- overallList$color_nBox
	maxMean <- max(overallResult$mean)
	maxSe <- max(overallResult$se)
	whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, colnames(overallResult), typOfPlot)
	
	
	if (length(overallResult[, 1]) > 0) {
		if (isOnlyOneValue) {
			plot = ggplot(data=overallResult, aes_string(x=whichColumShouldUse, y="mean"))
		} else {
			plot = ggplot(data=overallResult, aes(x=xAxis, y=mean))
		}
	
		if (overallList$appendix) {
			ylabelForAppendix <- renameY(overallDesName[[imagesIndex]])
			plot <- plot + 
					ylab(ylabelForAppendix)
		} else {
			plot <- plot + 
					ylab(overallDesName[[imagesIndex]])
		}
		
		plot = plot + 						
				geom_bar(stat="identity", aes_string(fill=whichColumShouldUse), colour="Grey", size=0.1) +
				geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.2, colour="black")+
				#geom_errorbar(aes(ymax=mean+se, ymin=mean-se), width=0.5, colour="Pink")+
				#ylab(overallDesName[[imagesIndex]]) +
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
		
		subtitle <- ""
		overallImage <- TRUE
		
		if(title != "") {
			sep <- " - "
		} else {
			sep <- ""
		}
		
		if (!overallList$appendix) {
			if (overallList$split.Treatment.First && overallList$split.Treatment.Second) {
				subtitle <- paste(cleanSubtitle(overallDesName[[imagesIndex]]), title, sep=sep)
				subsectionDepth <- 2
			}
		} else {
			subtitle <- paste(cleanSubtitle(ylabelForAppendix), title, sep=sep)
			
			subsectionDepth <- 1
			overallImage <- FALSE
		}
			
		if(typOfPlot == BOX.PLOT) {
			overallImageText <- "BarplotOverallImage"										
		} else {
			overallImageText <- "OverallImage"
		}
		
		writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(title, typOfPlot, sep=""), paste(overallFileName[[imagesIndex]], typOfPlot, overallImageText, sep=""), subtitle, overallImage, isAppendix=overallList$appendix, subsectionDepth=subsectionDepth)
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
	
	overallResultTemp$group <- as.factor(overallResultTemp$group)
	return(overallResultTemp)
	#return(overallResult)
}


setColorDependentOfGroup <- function(overallResult) {
	
#	lastColorPositiv <- ifelse(overallResult$mean[1] < 0, TRUE, FALSE)
#	color <- vector()
#	for(n in 1:length(unique(overallResult$group))) {
#		if (lastColorPositiv) {
#			color <- c(color, "light gray")
#			lastColorPositiv <- FALSE
#		} else {
#			color <- c(color, "green")
#			lastColorPositiv <- TRUE
#		}
#	}
	lastColorPositiv <- ifelse(overallResult$mean[1] < 0, TRUE, FALSE)
	color <- vector()
	if (lastColorPositiv) {
		color <- c(color, "light grey")
	} else {
		color <- c(color, "palegreen2")
	}

	if(length(as.character(unique(overallResult$group))) > 1) {
		if (lastColorPositiv) {
			color <- c(color, "palegreen2")
		} else {
			color <- c(color, "light grey")
		}
	}

	return(color)
}

OneMinusTheValue <- function(overallResult) {
	
	for(nn in colnames(overallResult)) {
		if(length(grep("nir",nn, ignore.case=TRUE)) > 0){
			notOneMinus <- c(notOneMinus, nn)
		}
	}
	colMinus <- overallResult %allColnamesWithoutThisOnes% notOneMinus
		if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
			#overallResult[,4:length(colnames(overallResult))] <- 1-overallResult[,4:length(colnames(overallResult))]
			colMinus <- colMinus[-c(1:3)]
		} else {
			#overallResult[,3:length(colnames(overallResult))] <- 1-overallResult[,3:length(colnames(overallResult))]
			colMinus <- colMinus[-c(1:2)]
		}
		overallResult[,colMinus] <- 1-overallResult[,colMinus]
	return(overallResult)
}

reorderThePlotOrder <- function(overallResult, typOfPlot, whichColumShouldUse = NAME) {
	groupedOverallResult <- data.table(overallResult)
	#sortString <- NAME
	
#	if(PRIMAER.TREATMENT %in% colnames(overallResult)) {
#		sortString <- PRIMAER.TREATMENT
#	}
#print(head(groupedOverallResult))
#print(whichColumShouldUse)

	if(typOfPlot == STACKBOX.PLOT) {
		sumVector <- as.data.frame(groupedOverallResult[, lapply(list(values), sum, na.rm=TRUE), by=c(whichColumShouldUse)])
	} else {
		sumVector <- as.data.frame(groupedOverallResult[, lapply(list(mean), mean, na.rm=TRUE), by=c(whichColumShouldUse)])
	}
	sumVector$c <- levels(overallResult[[whichColumShouldUse]])
	
	#print(head(sumVector))
	#print(levels(overallResult[[sortString]]))
#	print(sumVector[order(sumVector$V1),]$c)
	
	if(typOfPlot == VIOLIN.PLOT) {
		for(n in levels(overallResult[[whichColumShouldUse]])) {
			overallResult[[whichColumShouldUse]] <- replace(as.character(overallResult[[whichColumShouldUse]]), overallResult[[whichColumShouldUse]] == n, paste(sumVector[sumVector$c == n,]$c, " (", round(sumVector[sumVector$c == n,]$V1, digits=1), ")", sep=""))
			sumVector[sumVector$c == n,]$c <- paste(sumVector[sumVector$c == n,]$c, " (", round(sumVector[sumVector$c == n,]$V1, digits=1), ")", sep="")
		}
	}
	sortList <- order(sumVector$V1, decreasing = TRUE)
	overallResult[[whichColumShouldUse]] <- factor(overallResult[[whichColumShouldUse]], levels = sumVector[sortList, ]$c)
	#print(levels(overallResult[[whichColumShouldUse]]))
	#print(sumVector[sortList, ]$c)
	#print(head(sortList))

	return(list(overallResult = overallResult, sortList = sortList))
}

buildStressArea <- function(stress.Start, stress.End, stress.Typ, stress.Label, yValues, typOfPlot, additionalValues = NONE, additionalDataFrameValues = NONE) {
#############
#stress.Start <- overallList$stress.Start
#stress.End <- overallList$stress.End
#yValues <- overallResult$mean
#typOfPlot <- typOfPlot
#############
#yValues <- overallResult$values
#additionalValues <-  "stack"
#additionalDataFrameValues <- overallResult[, c("xAxis", "plot")]
#additionalDataFrameValues <- overallResult[, c("xAxis", "primaerTreatment")]
############


	stress.Area <- data.frame()
	#possible.Stress.Values <- c("0", "1", "2", "3", "4") # c("n", "d", "w", "c", "s")
	standard.Stress.Labels <- list("000" = "normal", "001" = "drought stress", "002" = "moisture stress", "003" = "chemical stress", "004" = "salt stress")
	
	ymin <- min(yValues,na.rm = TRUE)
	ymax <- max(yValues, na.rm = TRUE)
	
	if (typOfPlot == VIOLIN.PLOT)  {
		if (abs(ymin) >= abs(ymax)) {
			ymax <- abs(ymin)
		} else {
			ymin <- (-1*ymax)
		}
	} else if (typOfPlot == STACKBOX.PLOT) {
		if (additionalValues != NONE) {
			if (additionalValues == "fill") {
				ymin <- 0
				ymax <- 1.00
			} else if (additionalValues == "stack") {
				if(additionalDataFrameValues != NONE) {
					newDataFrame <- data.table(yValues, additionalDataFrameValues)
					newDataFrame <- as.data.frame(newDataFrame[, lapply(.SD, sum, na.rm=TRUE), by=colnames(additionalDataFrameValues)])
					ymin <- 0
					ymax <- max(newDataFrame$yValues, na.rm = TRUE)
				}
			} 			
		}
	} else if (typOfPlot == NBOX.PLOT) {
		if (additionalValues != NONE) {
			
			ymin <- ymin - max(additionalValues,na.rm = TRUE)
			ymax <- ymax + max(additionalValues, na.rm = TRUE)	
		}
	}

#	ymin <- (ymin - ymin * 0.01)
#	ymax <- (ymax + ymax * 0.01)
#	print(stress.Start)
#	print(stress.End)
#	print(stress.Typ)
#	print(stress.Label)
	
	for (kk in seq(along=stress.Start)) {
		if (stress.Start[kk] != -1 && stress.End[kk] != -1) {
			stress.Start <- as.numeric(stress.Start)
			stress.End <- as.numeric(stress.End)
			if (stress.Start[kk] >= stress.End[kk]) {
				xmin <- stress.End[kk]
				xmax <- stress.Start[kk]
			} else {
				xmin <- stress.Start[kk]
				xmax <- stress.End[kk]
			}
			
			if(!(stress.Typ[kk] %in% names(standard.Stress.Labels))) {
				ownCat("... unknown stresstyp, change to \"normal\" -> (0)")
				stress.Typ[kk] <- names(standard.Stress.Labels)[1]
			}
			
			if(stress.Label[kk] == -1) {
				ownCat("... no stresslabel are set, change to standard label for the stresstyp")
				stress.Label[kk] <- standard.Stress.Labels[[stress.Typ[kk]]]
			}
			
			stress.Area <- rbind(stress.Area, data.frame(xmin=xmin, xmax=xmax, ymin=ymin, ymax=ymax, typ=stress.Typ[kk], label=stress.Label[kk]))
		}
	}
	#dCNV_Biom[order(attr(dCNV_Bio, "Labels")),order(attr(dCNV_Bio, "Labels"))]
#	stress.Area <- sort(stress.Area)
	#print(stress.Area)
	return(stress.Area)
}

addColorForStressPhaseAndOther <- function(stressArea, color, typOfPlot = "none") {
	# c("000", "001", "002", "003", "004") entspricht c("n", "d", "w", "c", "s")
	
	if(typOfPlot == NBOX.PLOT || typOfPlot == "dodge") { # || typOfPlot == "stack") {
		stressAreaTyp <- rev(sort(as.character(unique(stressArea$typ))))
	} else {
		stressAreaTyp <- sort(as.character(unique(stressArea$typ)))
	}
	
	for (kk in stressAreaTyp) {
		if (kk == "000")
			color <- c("darkolivegreen1", color)
		else if (kk == "001")
			color <- c("cornsilk1", color)
		else if (kk == "002")
			color <- c("lightskyblue1", color)
		else if (kk == "003")
			color <- c("papayawhip", color)
		else if (kk == "004")
			color <- c("seashell1", color)
	}	
	return(color)
}

#plotViolinPlotDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title="") {
makeViolinDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
########
#overallResult <- plotThisValues
#title <- ""
########
	overallList$debug %debug% "makeViolinDiagram()"

	overallResult <- reownCategorized(overallResult)
	overallFileName <- overallList$imageFileNames_violinPlots
	color <- setColorDependentOfGroup(overallResult)
	overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, onlySecondTreatment = TRUE)
	overallResult <- reorderThePlotOrder(overallResult, typOfPlot)
	stressArea <- data.frame()

	#print(head(overallResult))
	
#  c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")	
#	overallList$stress.Start <- c(10,20,30,37)
#	overallList$stress.End <- c(13, 23, 33, 40)
#	overallList$stress.Typ <- c("d","d","s", "c")
#	overallList$stress.Label <- c(-1, -1, -1, -1)
#	
#	overallList$stress.Start <- c(10,20,30)
#	overallList$stress.End <- c(13, 23,33)
#	overallList$stress.Typ <- c("2","1","2")
#	overallList$stress.Label <- c(-1, -1,-1)
	
	if(overallList$stress.Start[1] != -1) {
		stressArea <- buildStressArea(overallList$stress.Start, overallList$stress.End, overallList$stress.Typ, overallList$stress.Label, overallResult$mean, typOfPlot)
		color <- addColorForStressPhaseAndOther(stressArea, color)
	}
	
#	print(color)
#	print(stressArea)
	if (length(overallResult[, 1]) > 0) {
						
		plot <-	ggplot()				
				
		if(length(stressArea) >0) {
			plot <- plot + 
				geom_rect(data=stressArea, aes(xmin=xmin, xmax=xmax, ymin=ymin, ymax=ymax, fill=typ)) +
				geom_text(data=stressArea, aes(x=xmin, y=ymin, label=label), size=2, hjust=0, vjust=1, angle = 90, colour="grey")
			
		}	
			plot <- plot +
				geom_ribbon(data=overallResult, aes(x=xAxis, fill=mean>=0, group=group, ymin=-mean, ymax=mean)) +						
				scale_fill_manual(values = color) +
				scale_x_continuous(name=overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis)))) + 
				scale_y_continuous(name=overallDesName[[imagesIndex]],  minor_breaks = min(as.numeric(as.character(overallResult$mean))):max(as.numeric(as.character(overallResult$mean)))) +
				#ylab(overallDesName[[imagesIndex]]) +
				#label=c("0.4", "0.2", "0.0", "0.2", "0.4")
				#scale_y_discrete(aes(factor(c(0.4, 0.2, 0.0, 0.2, 0.4))), name=overallDesName[[imagesIndex]],limits=c(-10,1)) +
				#scale_fill_manual(values = overallColor[[imagesIndex]]) +
				#scale_colour_manual(values= overallColor[[imagesIndex]]) +
				coord_flip()+
				theme_bw() +
				opts(axis.title.x = theme_text(face="bold", size=11), 
						axis.title.y = theme_text(face="bold", size=11, angle=90), 
						#panel.grid.major = theme_blank(), # switch off major gridlines
						#panel.grid.minor = theme_blank(), # switch off minor gridlines
						legend.position = "none", # manually position the legend (numbers being from 0, 0 at bottom left of whole plot to 1, 1 at top right)
						#legend.title = theme_blank(), # switch off the legend title						
						#legend.key.size = unit(1.5, "lines"), 
						#legend. key = theme_blank(), # switch off the rectangle around symbols in the legend
						panel.border = theme_rect(colour="Grey", size=0.1)
				)
		if(FALSE) {
			if (title != "") {
				plot <- plot + opts(title = title)
			}
		}
		
		if (length(unique(overallResult$name)) > 4) {
			plot = plot + opts(
					axis.text.x = theme_text(size = 8),
					strip.text.x = theme_text(size = 7)
			)
		} else {
			plot = plot + opts(
					axis.text.x = theme_text(size = 9),
					strip.text.x = theme_text(size = 10)
			)
		}
		
		plot <- plot + facet_wrap(~ name, ncol=5) 
	
		#print(plot)
				
#		if (overallList$split.Treatment.First && overallList$split.Treatment.Second) {
#			subsectionDepth <- 3
#		} else {
#			subsectionDepth <- 2
#		}		
#		print(title)
#		print(overallDesName[[imagesIndex]])
			
		writeTheData(overallList, plot, overallFileName[imagesIndex], paste(title, typOfPlot, sep=""), paste(overallFileName[[imagesIndex]], "violinOverallImage", sep=""), overallDesName[[imagesIndex]], TRUE, subsectionDepth=2)
	}
}

checkWhichColumShouldUseForPlot <- function(first, second, colNames, plotTyp = "", prePlot=FALSE) {
	
	if (first && second) {
		if(plotTyp == BOX.PLOT || plotTyp == SPIDER.PLOT || plotTyp == LINERANGE.PLOT || plotTyp == BAR.PLOT || plotTyp == NBOX.PLOT) {
			columName <- NAME
		} else {
			columName <- PRIMAER.TREATMENT
		}
	} else if (first && !second) {
		if((plotTyp == NBOX.PLOT || plotTyp == BOX.PLOT || plotTyp == SPIDER.PLOT || plotTyp == LINERANGE.PLOT) && !prePlot) {
			columName <- NAME	
		} else {
			columName <- getTheColumWhichShouldUse(colNames)
#			if("primaerTreatment" %in% colNames) {
#				columName <- "primaerTreatment"
#			} else {
#				columName <- "name"
#			}
		}
	} else if (!first && second) {
		if((plotTyp == NBOX.PLOT || plotTyp == BOX.PLOT || plotTyp == SPIDER.PLOT || plotTyp == LINERANGE.PLOT) && !prePlot) {
			columName <- getTheColumWhichShouldUse(colNames)
#			if("primaerTreatment" %in% colNames) {
#				columName <- "primaerTreatment"
#			} else {
#				columName <- "name"
#			}
		} else {
			columName <- NAME
		}
	} else {
		columName <- NAME
	}			
	return(columName)
}

#makeBoxplotDiagram <- function(overallResult, overallDescriptor, overallDesName, overallList, imagesIndex, typOfPlot, title) {
makeBoxplotDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	#############
#overallResult <- overallResultSplit
##overallResultTemp <- overallResultSplitSecondTime
##title <- paste(kk, nn, sep=" / ")
#title <- nn
##title <- ""	
	############
	
	overallList$debug %debug% "makeBoxplotDiagram()"
 
	overallFileName <- overallList$imageFileNames_Boxplots
	overallColor <- overallList$color_box
	options <- overallList$boxOptions

	#isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$split.Treatment.First, overallList$split.Treatment.Second)
	whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, colnames(overallResult), typOfPlot)
	#print(whichColumShouldUse)
	#if (!is.na(overallDescriptor[[imagesIndex]])) {
		#ownCat(paste("Process ", overallDesName[[imagesIndex]]))
		#createOuputOverview(BOX.PLOT, imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
		
		if (length(overallResult[, 1]) > 0) {
			xAxisfactor <- setxAxisfactor(overallList$xAxisName, overallResult[c("xAxis","value")], "value", options, typOfPlot)	
			
			if(!is.null(xAxisfactor)) {
				overallResult$xAxisfactor <- xAxisfactor 	
				overallResult <- na.omit(overallResult)
				overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
				
				#myPlot = ggplot(overallList$overallResult, aes(factor(name), value, fill=name, colour=name)) + 
				#myPlot = ggplot(overallResult, aes(factor(name), value, fill=name)) +
			
				
	#			if (!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
	#				overallResult <- cbind(overallResult,plot=rep.int(1, length(overallResult[,1])))
	#				plot <- ggplot(overallResult, aes(plot, value, fill=plot))
	#			#} else if (overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
	#			} else {
					#print(head(overallResult))
					plot <- ggplot(overallResult, aes_string(x=whichColumShouldUse, y="value", fill=whichColumShouldUse))
				#}
				
				
	#			else if (overallList$split.Treatment.First && overallList$split.Treatment.Second) {
	#				plot <- ggplot(overallResult, aes(factor(name), value, fill=name))
	#			} else if (!isOtherTyp) {
	#				if("primaerTreatment" %in% colnames(overallResult)) {
	#					plot <- ggplot(overallResult, aes(factor(primaerTreatment), value, fill=primaerTreatment))
	#				} else {
	#					plot <- ggplot(overallResult, aes(factor(name), value, fill=name))
	#				}
	#			} else {
	#				plot <- ggplot(overallResult, aes(factor(name), value, fill=name))
	#			}
				
		
	#			if(!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
	#				overallResult <- cbind(overallResult,plot=rep.int(1, length(overallResult[,1])))
	#				plot <- ggplot(overallResult, aes(plot, value, fill=plot))
	#			} else {
	#				if(isOtherTyp) {
	#					plot <- ggplot(overallResult, aes(factor(name), value, fill=name))
	#				} else {
	#					plot <- ggplot(overallResult, aes(factor(primaerTreatment), value, fill=primaerTreatment))
	#				}
	#			}
				if(FALSE) {
					if (title != "" && !(!overallList$split.Treatment.First && !overallList$split.Treatment.Second)) {
						plot = plot + opts(title = title)
					}
				}
				
				plot = plot +
						geom_boxplot() +
						ylab(overallDesName[[imagesIndex]]) +
						scale_fill_manual(values = overallColor[[imagesIndex]])
						#coord_cartesian(ylim=c(0, max(overallList$overallResult$mean + overallList$overallResult$se + 10, na.rm=TRUE))) +
						#xlab(paste(min(overallResult$xAxis), overallList$xAxisName, "..", max(overallResult$xAxis), overallList$xAxisName)) +
	#print(plot)		
	#			if(!(!overallList$split.Treatment.First && !overallList$split.Treatment.Second)) {	
	#				plot = plot +
	#						scale_fill_manual(values = overallColor[[imagesIndex]])
	#			}
						#stat_summary(fun.data = f, geom = "crossbar", height = 0.1, 	colour = NA, fill = "skyblue", width = 0.8, alpha = 0.5) +
				plot = plot +			
						theme_bw() +
						opts(legend.position="none", 
								#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
								axis.title.x = theme_blank(), 
								axis.title.y = theme_text(face="bold", size=11, angle=90), 
								panel.grid.minor = theme_blank(), 
								panel.border = theme_rect(colour="Grey", size=0.1)
								
						)
#				if(overallList$split.Treatment.First && overallList$secondTreatment == "none") {
#					plot = plot +
#							opts(axis.text.x =theme_blank())
#				} else {
					plot = plot + 
							opts(axis.text.x = theme_text(size=5, angle=90))	
				#}
				
				plot = plot +	
						facet_wrap(~ xAxisfactor, drop=TRUE)
	
				#sprint(plot)
				
				subtitle <- title
				
				if (!overallList$split.Treatment.First && !overallList$split.Treatment.Second) {
					subsectionDepth <- 1
				} else {
					subsectionDepth <- 2
				}		
				
				writeTheData(overallList, plot, overallFileName[[imagesIndex]], paste(title, typOfPlot, sep=""), paste(overallFileName[[imagesIndex]], typOfPlot, "OverallImage", sep=""), subtitle, TRUE, subsectionDepth=subsectionDepth)
							
	#				saveImageFile(overallList, plot, overallFileName[[imagesIndex]], typOfPlot)
	#
	#				if (overallList$appendix) {
	#					writeLatexFile("appendixImage", overallFileName[[imagesIndex]], typOfPlot)
	#				}
			}
		}
	#}
}

plotDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	overallList$debug %debug% "plotDiagram()"	
	
	if(typOfPlot == NBOX.PLOT) {
		makeLinearDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if(typOfPlot == BOX.PLOT) {
		makeBoxplotDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if(typOfPlot == STACKBOX.PLOT) {
		makeStackedDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if(typOfPlot == SPIDER.PLOT) {
		makeSpiderDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if(typOfPlot == VIOLIN.PLOT) {
		makeViolinDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if(typOfPlot == BAR.PLOT) {
		makeBarDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	}
	
#	switch(typOfPlot,
#			NBOX.PLOT = makeLinearDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title),
#			BOX.PLOT = makeBoxplotDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title),
#			STACKBOX.PLOT = makeStackedDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title),
#			SPIDER.PLOT = makeSpiderDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title),
#			VIOLIN.PLOT = makeViolinDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title),
#			BAR.PLOT = makeBarDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)) 
	
	
}

makeSplitDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot) {
	overallList$debug %debug% "makeSplitDiagram()"
	
	if((!overallList$split.Treatment.First && !overallList$split.Treatment.Second && !(typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT)) || 
		(overallList$split.Treatment.First && overallList$secondTreatment == NONE && !(typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT)) || 
		(typOfPlot == VIOLIN.PLOT) ||
		(!(overallList$split.Treatment.First && overallList$split.Treatment.Second) && (typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT))) {
			plotDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title = "")	
	} else {
		extraPlot <- checkWhichColumShouldUseForPlot(overallList$split.Treatment.First, overallList$split.Treatment.Second, typOfPlot, colnames(overallResult), TRUE)
		for(nn in unique(as.character(overallResult[[extraPlot]]))) {
			optionListForGetBoolean <- list(value = nn)
			names(optionListForGetBoolean) <- extraPlot
			booleanVector <- getBooleanVectorForFilterValues(overallResult, optionListForGetBoolean)
			overallResultSplit <- overallResult[booleanVector, ]
			nn <- replaceTreatmentNamesOverallOneValue(overallList, nn, typOfPlot)
			
			plotDiagram(overallResultSplit, overallDesName, overallList, imagesIndex, typOfPlot, nn)
		}
	}
}


paralleliseDiagramming <- function(overallList, tempOverallResult, overallDescriptor, overallDesName, typOfPlot) {
	overallList$debug %debug% "paralleliseDiagramming()"
	
	for (imagesIndex in names(overallDescriptor)) {
		if (!is.na(overallDescriptor[[imagesIndex]][1])) {
			createOuputOverview(typOfPlot, imagesIndex, length(names(overallDescriptor)),  overallDesName[[imagesIndex]])
			
			if(typOfPlot == BOX.PLOT || typOfPlot == STACKBOX.PLOT || typOfPlot == SPIDER.PLOT) {
				tempOverallResult <- na.omit(tempOverallResult)
			}
			
			overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, overallList$debug, typOfPlot)
		
			if(typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT) {
				overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
				overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0		
				overallResult <-  replaceTreatmentNamesOverall(overallList, overallResult)
				
			} else if(typOfPlot == VIOLIN.PLOT) {
				overallResult <- overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
				overallResult <- OneMinusTheValue(overallResult)
			} 

			
			if (innerThreaded) {
				sfClusterCall(makeSplitDiagram, 
						overallResult, overallDesName, 
						overallList, imagesIndex, typOfPlot,
						stopOnError=FALSE)
			} else {
				makeSplitDiagram( 
						overallResult, overallDesName, 
						overallList, imagesIndex, typOfPlot)
			}
		}
	}
}

makeDiagrams <- function(overallList) {
	overallList$debug %debug% "makeDiagrams()"
	if (threaded)
		sfExport("overallList")
	
	if (!calculateNothing) {			
if(!calculateOnlyViolin) {	
if(!calculateOnlySpider) {
if(!calculateOnlyStacked) {
if(!calculateOnlyBoxplot) {

		if (sum(!is.na(overallList$nBoxDes)) > 0) {
			if (overallList$debug) {ownCat("nBoxplot...")}
				sfClusterEval(paralleliseDiagramming(overallList, overallList$overallResult_nBoxDes,  overallList$nBoxDes, overallList$nBoxDesName, NBOX.PLOT), 
						stopOnError = FALSE)
		} else {
			ownCat("All values for nBoxplot are 'NA'")
		}
}


if(!calculateOnlyNBox) {
		if (sum(!is.na(overallList$boxDes)) > 0) {
			if (overallList$debug) {ownCat("Boxplot...")}						
				sfClusterEval(paralleliseDiagramming(overallList, overallList$overallResult_boxDes,  overallList$boxDes, overallList$boxDesName, BOX.PLOT), 
						stopOnError = FALSE)			
		} else {
			ownCat("All values for Boxplot are 'NA'...")
		}
}}

if(!calculateOnlyNBox) {
if(!calculateOnlyBoxplot) {
		if (sum(!is.na(overallList$boxStackDes)) > 0) {
			if (overallList$debug) {ownCat("Stacked Boxplot...")}
				sfClusterEval(paralleliseDiagramming(overallList, overallList$overallResult_boxStackDes,  overallList$boxStackDes, overallList$boxStackDesName, STACKBOX.PLOT), 
						stopOnError = FALSE)	
		} else {
			ownCat("All values for stacked Boxplot are 'NA'...")
			}
}}}


if(!calculateOnlyStacked) {
if(!calculateOnlyBoxplot) {
if(!calculateOnlyNBox) {
		if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
			if (overallList$debug) {ownCat("Spider plot...")}
				sfClusterEval(paralleliseDiagramming(overallList, overallList$overallResult_boxSpiderDes,  overallList$boxSpiderDes, overallList$boxSpiderDesName, SPIDER.PLOT), 
						stopOnError = FALSE)
		} else {
			ownCat("All values for stacked Boxplot are 'NA'...")
		}
}}}}


if(!calculateOnlyStacked) {
if(!calculateOnlyBoxplot) {
if(!calculateOnlyNBox) {
if(!calculateOnlySpider) {
		if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
			if (overallList$debug) {ownCat("Violin plot...")}
				sfClusterEval(paralleliseDiagramming(overallList, overallList$overallResult_violinBoxDes,  overallList$violinBoxDes, overallList$violinBoxDesName, VIOLIN.PLOT), 
						stopOnError = FALSE)
		} else {
			ownCat("All values for violin Boxplot are 'NA'...")
		}
}}}}
		if (FALSE) {	# falls auch mal barplots erstellt werden sollen (ausser wenn nur ein Tag vorhanden ist!)
			if (overallList$debug) {ownCat("Barplot...")}
				sfClusterEval(paralleliseDiagramming(overallList, overallList$overallResult_nBoxDes,  overallList$nBoxDes, overallList$nBoxDesName, BAR.PLOT), 
						stopOnError = FALSE)
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
	
	if (length(addDescSet) > 0) {
		return(list(desSet=addDescSet, desName = addDescSetNames))
	} else {
		return(list(desSet=descriptorSet_boxplotStacked, desName = descriptorSetName_boxplotStacked))
	}
	#descriptorSet_boxplotStacked = c(descriptorSet_boxplotStacked, addDescSetNames) 	
	#return(descriptorSet_boxplotStacked)
}

changeXAxisName <- function(overallList) {
	if (length(overallList$iniDataSet$Day..Int.) > 1 ){
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


loadStressPeriod <- function(stress.Value, arg) {
	stress.Value <- stress.Value %exists% arg
	stress.Value <- unlist(preprocessingOfValues(stress.Value, isColName = TRUE))

	return(stress.Value)
}

checkStressTypValues <- function(stress.Typ) {
	
	new.Values <- list("n" = "000", "d" = "001", "w" = "002", "c" = "003", "s" = "004",
					   "0" = "000", "1" = "001", "2" = "002", "3" = "003", "4" = "004")
	
	for (kk in seq(along=stress.Typ)) {
		if (stress.Typ[kk] %in% names(new.Values)) {
			stress.Typ[kk] <- new.Values[[stress.Typ[kk]]]
			ownCat("... change old stresstyp value to new!")
		}
	}
	return(stress.Typ)
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
	if (memory.limit() < 3500) {
		memory.limit(size=10000)
	}
	
	while(!is.null(dev.list())) {
		dev.off()
	}
	
	loadLibs(debug)
}


startOptions <- function(typOfStartOptions = "test", debug=FALSE) {
	initRfunction(debug)
	#typOfStartOptions = "test"
	typOfStartOptions = tolower(typOfStartOptions)
		
	args = commandArgs(TRUE)
#	for(nn in seq(along=args)) {
#		ownCat(paste(nn, ".: ", args[nn], sep=""))
#	}
	
	saveFormat = "pdf"
	dpi = "90" ##90 ## CK: seems to change nothing for ggplot2 instead the output size should be modified, if needed // 17.2.2012	
	
	isGray = FALSE
	#showResultInR = FALSE
	
	# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")
	stress.Start <- -1 #"10" # -1
	stress.End <- -1 #"15" # -1
	stress.Typ <- -1 #"1" # -1 ## 1 -> dry; 2 -> wet; 0 -> normal; 4 -> salt; 3 -> chemical
	stress.Label <- -1 #"drought stress" # -1
	
	treatment = "Treatment"
	filterTreatment = NONE
	split.Treatment.First = TRUE
	
	secondTreatment = NONE
	filterSecondTreatment = NONE
	split.Treatment.Second= FALSE
	
	xAxis = "Day (Int)" 
	xAxisName = "DAS"
	filterXaxis = NONE
	
	should.Clustered <- FALSE
	bootstrap.N <- -1
	
#	diagramTypVector = vector()
	descriptorSet = vector()
	descriptorSetName = vector()
	
	fileName = NONE

	appendix = FALSE
	#appendix = TRUE
	
	separation = ";"

	if (typOfStartOptions == "all" | typOfStartOptions == "report" | typOfStartOptions == "allmanual") {
		fileName <- fileName %exists% args[1]
		
		should.Clustered <- should.Clustered %exists% args[7]
		bootstrap.N <- bootstrap.N %exists% args[8] 
		stress.Start <- loadStressPeriod(stress.Start, args[9])
		stress.End <- loadStressPeriod(stress.End, args[10])
		stress.Typ <- loadStressPeriod(stress.Typ, args[11])
		stress.Typ <- checkStressTypValues(stress.Typ)	
		stress.Label <- loadStressPeriod(stress.Label, args[12])
		split.Treatment.First <- as.logical(split.Treatment.First %exists% args[13])
		split.Treatment.Second <- as.logical(split.Treatment.Second %exists% args[14])
		
		if (fileName != NONE) {
			workingDataSet <- separation %readInputDataFile% fileName
			descriptorSet_nBoxplot <- vector()
			descriptorSet_boxplot <- vector()
			descriptorSet_boxplotStacked <- vector()
			
			if (length(workingDataSet[,1]) > 0) {
				
				loadFiles(path = ".", pattern = "PlotList\\.[Rr]$")

				if (typOfStartOptions == "all") {
					descriptorSet_nBoxplot <- colnames(workingDataSet)
					descriptorSetName_nBoxplot <- descriptorSet
					
				} else { #Report		
					descriptorSet_nBoxplot <- names(nBox.plot.list)
					descriptorSetName_nBoxplot <- getVector(nBox.plot.list)		
				}
							
				descriptorSet_boxplot <- names(box.plot.list)
				descriptorSetName_boxplot <- getVector(box.plot.list)
				
				descriptorSet_spiderplot <- names(spider.plot.list)
				descriptorSetName_spiderplot <- getVector(spider.plot.list)
				
				descriptorSet_violinBox <- names(violin.plot.list)
				descriptorSetName_violinBox <- getVector(violin.plot.list)
								
				descriptorList <- addDesSet(names(stacked.plot.list), getVector(stacked.plot.list), workingDataSet)
				descriptorSet_boxplotStacked <- descriptorList$desSet
				descriptorSetName_boxplotStacked <- descriptorList$desName

				
				appendix = as.logical(appendix %exists% args[5])
			
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

				if (treatment == NONE.TREATMENT) {
					workingDataSet <- cbind(workingDataSet, noneTreatment=rep.int("average", times = length(workingDataSet[,1])))	
				}

				isRatio	= as.logical(isRatio %exists% args[6])
			} else {
				fileName = NONE
			}
		}
		
	}  else if (typOfStartOptions == "test"){
		
		library("snowfall")
		debug <- TRUE
		initRfunction(debug)
		sfStop()
		
		#treatment <- "Species"
		#filterTreatment  <- "Athletico$Fernandez$Weisse Zarin"
		
		treatment <- "Treatment"
		#treatment <- "Genotype"
		filterTreatment <- "dry / normal"
		#filterTreatment <- "dry$normal"
		#filterTreatment <- "Trockentress$normal bewaessert"
		#filterTreatment <- "N661230.3 x IL$N323525.9 x IL$N590895.3 x IL"

		#secondTreatment <- "none"
		#filterSecondTreatment  <- "none"
		
		secondTreatment <- "Species"
		filterSecondTreatment  <- "Athletico$Fernandez$Weisse Zarin"
		
		#secondTreatment <- "Treatment"
		#filterSecondTreatment <- "dry / normal"
		
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
		
		# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")
		stress.Start <- 27
		stress.End <- 44
		stress.Typ <- "001"
		stress.Label <- "-1"
		
		split.Treatment.First <- TRUE
		split.Treatment.Second <- FALSE
		isRatio <- TRUE
		calculateNothing <- FALSE
		stoppTheCalculation <- FALSE
		iniDataSet = workingDataSet
		
		loadFiles(path = ".", pattern = "PlotList\\.[Rr]$")
		
		descriptorSet_nBoxplot <- names(nBox.plot.list)
		descriptorSetName_nBoxplot <- getVector(nBox.plot.list)		
		
		descriptorSet_boxplot <- names(box.plot.list)
		descriptorSetName_boxplot <- getVector(box.plot.list)
		
		escriptorSet_spiderplot <- names(spider.plot.list)
		descriptorSetName_spiderplot <- getVector(spider.plot.list)
		
		descriptorSet_violinBox <- names(violin.plot.list)
		descriptorSetName_violinBox <- getVector(violin.plot.list)
		
		descriptorList <- addDesSet(names(spider.plot.list), getVector(spider.plot.list), workingDataSet)
		descriptorSet_boxplotStacked <- descriptorList$desSet
		descriptorSetName_boxplotStacked <- descriptorList$desName
		
		###################
		
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
		
		if (treatment == NONE.TREATMENT) {
			workingDataSet <- cbind(workingDataSet, noneTreatment=rep.int("average", times = length(workingDataSet[,1])))	
		}
		
	}
	
	if (typOfStartOptions != "test"){
		secondRun = appendix
		appendix =  FALSE
		
		if (fileName != NONE & (length(descriptorSet_nBoxplot) > 0 || length(descriptorSet_boxplot) > 0 || length(descriptorSet_boxplotStacked) > 0)) {
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
										xAxisName = xAxisName, debug = debug, appendix=appendix, isRatio=isRatio,
										stress.Start=stress.Start, stress.End = stress.End, stress.Typ = stress.Typ, stress.Label = stress.Label,
										split.Treatment.First=split.Treatment.First, split.Treatment.Second=split.Treatment.Second)
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
		
		if (debug) {
			ownCat(warnings())
			
		}
	} 
#### Bis hier einlesen beim manuellen testen ##############	
}

createDiagrams <- function(iniDataSet, saveFormat="pdf", dpi="90", isGray="false", 
		nBoxDes = NULL, boxDes = NULL, boxStackDes = NULL, boxSpiderDes = NULL, violinBoxDes = NULL,
		nBoxDesName = NULL, boxDesName = NULL, boxStackDesName = NULL, boxSpiderDesName = NULL, violinBoxDesName = NULL,
		nBoxOptions= NULL, boxOptions= NULL, stackedBarOptions = NULL, spiderOptions = NULL, violinOptions = NULL,
		treatment = "Treatment", filterTreatment = NONE, 
		secondTreatment = NONE, filterSecondTreatment = NONE, filterXaxis = NONE, xAxis = "Day (Int)", 
		xAxisName = NONE, debug = FALSE, appendix = FALSE, stoppTheCalculation = FALSE, isRatio = FALSE,
		stress.Start = -1, stress.End = -1, stress.Typ = -1, stress.Label = -1,
		split.Treatment.First = TRUE, split.Treatment.Second = FALSE) {		

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
						filterTreatmentRename = list(), secondFilterTreatmentRename = list(),
						stress.Start = stress.Start, stress.End = stress.End, stress.Typ = stress.Typ, stress.Label = stress.Label,
						split.Treatment.First = split.Treatment.First, split.Treatment.Second = split.Treatment.Second
						)	
	
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

initSnow <- function() {
	# loadLibs(TRUE)
	library("snowfall")
	
	if(cpuAutoDetected) {
		cpuCNT <- parallel::detectCores()
	}
	
	sfInit(parallel=threaded, cpus=cpuCNT)
	
	if (sfParallel()) {
		ownCat(c("Running in parallel mode on", sfCpus(), "nodes."))
		threaded <- TRUE
	} else {
		ownCat("Running in sequential mode.")
		threaded <- FALSE
	}
	if (threaded)
		sfExportAll()
}

stopSnow <- function() {
	sfStop()
}

#sapply(list.files(pattern="[.]R$", path=getwd(), full.names=TRUE), source);
######### START #########
#rm(list=ls(all=TRUE))
#startOptions("test", TRUE)
#startOptions("allmanual", TRUE)


initSnow()
startOptions("report", debug)
ownCat("Completing diagram creation...")
stopSnow()

rm(list=ls(all=TRUE))