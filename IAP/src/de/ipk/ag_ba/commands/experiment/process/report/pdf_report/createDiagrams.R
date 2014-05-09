# Author: Entzian, Klukas
###############################################################################
cat(paste("used R-Version: ", sessionInfo()$R.version$major, ".", sessionInfo()$R.version$minor, "\n", sep = ""))


############## Flags for debugging ####################
DEBUG <- TRUE
CATCH.ERROR <- FALSE

calculateNothing <- FALSE
plotNothing <- FALSE

plotOnlyNBox <- FALSE
plotOnlyNBoxMulti <- FALSE
plotOnlyViolin <- FALSE
plotOnlyStacked <- FALSE
plotOnlySpider <- FALSE
plotOnlyLineRange <- FALSE
plotOnlyBoxplot <- FALSE
plotOnlyStressValues <- FALSE


############# Modelling ######################

DO.MODELLING.OF.STRESS <- FALSE
STRESS.MODELL.INPUT <- "report_1121KN.csv"
STRESS.MODELL.TREATMENT <- "Treatment"
descriptorStressVector <- vector()


DO.PARALLELISATION <- FALSE
SHOULD.THREADED <- FALSE
INNER.THREADED = FALSE
CPU.CNT <- 2
CPU.ATUO.DETECTED <- TRUE
CHECK.INPUT.FILE.IF.ENG.OR.GER <- FALSE
CHECK.FOR.UPDATE <- FALSE
INSTALL.PACKAGE <- FALSE

########## Constants ###########

ERROR <- "error"
LIB <- "lib"
UPDATE <- "update"

## long table name
LONG.UNIT.TABLE <- "unitTable"

## value typs
GET.OVERALL.FILE.NAME <- "overallFileName"
GET.SECTION.VALUE <- "section"
GET.OVERALL.RESULT.RESET <- "overallResultReset"

## plot typs
NBOX.PLOT <- "nboxplot"
NBOX.MULTI.PLOT <- "nboxmultiplot"
BOX.PLOT <- "boxplot"
STACKBOX.PLOT <- "boxplotstacked"
SPIDER.PLOT <- "spiderplot"
VIOLIN.PLOT <- "violinplot"
BAR.PLOT <- "barplot"
LINERANGE.PLOT <- "lineRangePlot"
STRESS.PLOT <- "stressplot"

## colum names
NAME <- "name"
PRIMAER.TREATMENT <- "primaerTreatment"
X.AXIS <- "xAxis"
MEAN <- "mean"
STRESS <- "stress"
VALUES <- "values"
HIST <- "hist"
SE <- "se"
COLUMN <- "column"


## fail values
NONE <- "none"
NONE.TREATMENT <- "noneTreatment"

## errors
NOT.NUMERIC.OR.ALL.ZERO <- "NotNumericOrAllZero"
NOT.EXISTS <- "notExists"

## path
PLOTTING.LISTS <- "plottingLists"

## fileName
SECTION.SEPARATOR <- "."
TEX <- "tex"
PDF <- "pdf"

## sectionList Mapping Values
NEW.SECTION <- "newSection"

## section
FIRST.SECTION <- "section"
SECOND.SECTION <- "subsection"
THIRD.SECTION <- "subsubsection"
FOURTH.SECTION <- "paragraph"
APPENDIX.SECTION <- "appendix"
ERROR.SECTION <- ERROR


REDUCE.SECTION <- "reduceSection"
GET.INT.AS.SECTION <- "getIntAsSection"
GET.NUMBER.OF.SECTION <- "getNumberOfSection"
FILL.WITH.NULL.SECTION <- "fillWithNullSection" 

DIRECTORY.SECTION <- "section"
DIRECTORY.PLOTS <- "plots"
DIRECTORY.PLOTSTEX <- "plotTex"
DIRECTORY.SEPARATOR <- "/"

## fileName Pattern
PRE.AND.POST.VALUE <- "_"
SECTION.TEX <- paste(PRE.AND.POST.VALUE, "sec", PRE.AND.POST.VALUE, sep = "")
NEW.SECTION.TEX <- paste(PRE.AND.POST.VALUE, NEW.SECTION, PRE.AND.POST.VALUE, sep = "")
OVERALL.FILENAME.TEX <- "overallFileNameTex"
OVERALL.FIRST.SECTION.TEX <- paste(PRE.AND.POST.VALUE, FIRST.SECTION, PRE.AND.POST.VALUE, sep = "")
OVERALL.SECOND.SECTION.TEX <- paste(PRE.AND.POST.VALUE, SECOND.SECTION, PRE.AND.POST.VALUE, sep = "")
OVERALL.THIRD.SECTION.TEX <- paste(PRE.AND.POST.VALUE, THIRD.SECTION, PRE.AND.POST.VALUE, sep = "")
OVERALL.FOURTH.SECTION.TEX <- paste(PRE.AND.POST.VALUE, FOURTH.SECTION, PRE.AND.POST.VALUE, sep = "")
OVERALL.APPENDIX.SECTION.TEX <- paste(PRE.AND.POST.VALUE, APPENDIX.SECTION, PRE.AND.POST.VALUE, sep = "")
POINT.PATTERN <- "\\."
DIGITS.PATTERN <- "[[:digit:]]+"
TEX.PATTERN <- paste(POINT.PATTERN, "[Tt][Ee][Xx]$", sep = "")
SECTION.PATTERN <- paste(SECTION.TEX, DIGITS.PATTERN, POINT.PATTERN, DIGITS.PATTERN, POINT.PATTERN, DIGITS.PATTERN, POINT.PATTERN, DIGITS.PATTERN, TEX.PATTERN, sep = "")

OVERALL.FILENAME.PATTERN <- "overallFileNamePattern"

## build new Columns
BUILD.TOP <- "top"
BUILD.SIDE <- "side"
BUILD.SECTION <- "section"
BUILD.SEPARATOR.ONE <- "."
BUILD.SEPARATOR.TWO <- "_"
BUILD.SECTION.FIRST.INDEX <- 5
BUILD.SECTION.SECOND.INDEX <- 5

## Tex
NEWLINE.TEX.INCLUDE <- " \\newline "
NEWLINE.TEX <- " \n"
NEWLINE.PARA.TEX <- "~"
TABULATOR.TEX <- " "

## default values
VALUE.AVERAGE <- "average"
START.TYP.TEST <- "test"
START.TYP.REPORT <- "report"
START.TYP.STRESS <- "stress"

## file names
REPORT <- "report"
REPORT.FILE <- paste(REPORT, TEX, sep = ".")
REPORT.PDF <- paste(REPORT, PDF, sep = ".")
ERROR.FILE <- paste(ERROR, TEX, sep = ".")
LIB.ERROR.FILE <- paste(LIB, ERROR, TEX, sep = ".")
ERROR.TOTAL.FILE <- paste(ERROR, "Total.txt", sep = "")

LIB.UPDATE <- paste(LIB, UPDATE, sep = "")

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
		ownCat(paste("Script will stopped for ", breakValue, " sec!", sep = ""))
		
		Sys.sleep(breakValue)
		ownCat("Break ends!")
	}
}

##
#	print the value with the "DebugBreakPoint" flag if debug is TRUE
##
"%debug%" <- function(debug, value) {
	debug %print% paste("DebugBreakPoint: ", value)
}

##
#	print value if debug is TRUE
##
"%print%" <- function(debug, value) {
	if (debug) {
		ownCat(as.character(value), endline = TRUE)
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
			ownCat(paste("Descriptor '", errorDescriptor, "' is missing!", sep = ""))
			
		} else if (typOfError == NOT.NUMERIC.OR.ALL.ZERO) {
#			ownCat(paste("the values of the descriptor(s) '", errorDescriptor, "', are all zero or not numeric!", sep = ""))
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
	ownCat(paste("Read input file", fileName))
	if (file.exists(fileName)) {
		allCharacterSeparated <- vector()
		
		if (CHECK.INPUT.FILE.IF.ENG.OR.GER) {
			preScanForPointOrComma <- scan(file = fileName, what = character(0), nlines = 2, sep = "\n")
			preScanForPointOrComma <- paste(preScanForPointOrComma[2], ", .", sep = "")
			allCharacterSeparated <- table(strsplit(toupper(preScanForPointOrComma), '')[[1]])
		} else {
			allCharacterSeparated["."] <- 1
			allCharacterSeparated[", "] <- 0
		}
		if (allCharacterSeparated["."] > allCharacterSeparated[", "]) {
			
			TRUE %print% "Read input (English number format)..."
			data <- read.csv(fileName, header = TRUE, sep = separation, fileEncoding = "UTF-8")
			colnames(data)[14:length(colnames(data))] <- replaceBrakesWithNothing(colnames(data)[14:length(colnames(data))], "..")
			return(data)
		} else {
			
			TRUE %print% "Read input (German number format)..."
			data <- read.csv2(fileName, header = TRUE, sep = separation, fileEncoding = "UTF-8")
			colnames(data) <- replaceBrakesWithNothing(colnames(data), "..")
			return(data)
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
			vectorTemp = c(vectorTemp, paste(vector1, k, sep = "/")) # #/#
		}
	}
	return(vectorTemp)
}

"%allColnamesWithoutThisOnes%" <- function(dataSet, withoutColNamesVector) {
	return(colnames(dataSet)[!colnames(dataSet) %in% withoutColNamesVector])
}

"%isIdentical%" <- function(value1, value2) {
	return(identical(value1, value2))
}

"%checkRowLengthOfDataFrame%" <- function(dataFrame, len) {
	if (nrow(dataFrame) > len) {
		return(TRUE)
	} else {
		return(FALSE)
	}
	
}


##
#	print text with/without wordwrap also in the parallel modus
##
ownCat <- function(text, endline = TRUE) {
	if (DO.PARALLELISATION) {
		if (sfParallel()) {
			sfCat(text, master = TRUE)
			if (endline)
				sfCat("\n", master = TRUE)
		} else {
			cat(unlist(text))
			if (endline)
				cat("\n")
		}
	} else {
		cat(unlist(text))
		if (endline)
			cat("\n")
	}
}

getFileNames <- function(path, pattern = TEX.PATTERN, debug = debug) {
	debug %debug% "getFileNames()"
	
	fileNames <- list.files(path, pattern = pattern)
	#workingFileNames <- grep(SECTION, fileNames, ignore.case = TRUE)
	
#	if (length(workingFileNames) > 0) {
#		return(fileNames[workingFileNames])
#	}
	
	if (length(fileNames) > 0) {
		return(fileNames)
	} else {
		return(NULL)
	}
}

loadFiles <- function(path, pattern = "\\.[Rr]$", trace = TRUE, debug = FALSE) {
	debug %debug% "loadFiles()"
	
	for (nm in list.files(path, pattern = pattern)) {
		if (trace) ownCat(paste("load: ", nm, sep = ""))
		source(file.path(path, nm))
	}
}

##
#	install, update and load the submitted libs
#	@install: install missing libs if TRUE
#	@update: update existing libs if TRUE
##
loadInstallAndUpdatePackages <- function(libraries, install = FALSE, update = FALSE, check = FALSE, catch = FALSE, debug = FALSE) {	
	debug %debug% "loadInstallAndUpdatePackages()"
	
	repos <- c("http://cran.r-project.org", "http://www.rforge.net/")
	libPath <- Sys.getenv("R_LIBS_USER")
	
	
	installLibs(install = install, libraries = libraries, repos = repos, libPath = libPath, debug = debug)
	updateLibs(update = update, libraries = libraries, repos = repos, libPath = libPath, debug = debug)
	checkLibs(check = check, libraries = libraries, debug = debug)
	loadLibs(libraries = libraries, catch = catch, debug = debug)
}


##
#	install missing Libs if "install" is TRUE
##
installLibs <- function(install = FALSE, libraries, repos, libPath, debug = FALSE) {
	debug %debug% "installLibs()"
	
	if (install & length(libraries) > 0) {
		TRUE %print% "Check for new packages..."
		
		for(n in repos) {		
			installedPackages <- names(installed.packages()[, 'Package'])
			availablePackagesOnTheRepos <- available.packages(contriburl = contrib.url(n))[, 1]
			ins <- libraries[!libraries %in% installedPackages & libraries %in% availablePackagesOnTheRepos]
			
			if (length(ins) > 0) {
				TRUE %print% paste("The following packages will be installed: ", ins, sep = "")	
				install.packages(ins, lib = libPath, repos = n, dependencies = TRUE)
			}	
		}	
	}
}

##
#	update out of date libs if "update" is TRUE
##
updateLibs <- function(update = FALSE, libraries, repos, libPath, debug = FALSE) {
	debug %debug% "updateLibs()"
	
	if (update) {
		TRUE %print% "Check for package updates ..."
		
		for(n in repos) {
			update.packages(lib.loc = libPath, checkBuilt = TRUE, ask = FALSE, 	repos = n)
		}
	}
}

##
#	check the version of the installed libs and compare this with the necessary libs for the report function
#	if "check" is true the the comparison is performed
#	the program stops when older version are found
##
checkLibs <- function(check = FALSE, libraries, debug = FALSE) {
	debug %debug% "checkLibs()"
	
	if(check) {
		TRUE %print% "Check for package versions ..."		
		
		outOfDateLib <- NULL
		for(nn in libraries) {
			outOfDateLib <- checkVersionsOfUsedPackages(nn, outOfDateLib, debug = debug)
		}		
		
		if (length(outOfDateLib) > 0) {
			ckeckIfReportTexIsThere(outOfDateLib, typ = LIB.UPDATE, debug = debug)
			stop("Libs not up to date!", call. = FALSE)
		}	
	}
}


##
#	load the libs
#	if errors then stopp the program
##
loadLibs <- function(libraries, catch = FALSE, debug = FALSE) {
	debug %debug% "checkLibsForErrorsAndLoad()"
	
	if (length(libraries) > 0) {
		TRUE %print% "Load libraries:"

		libErrorText <- NULL
		
		for(nn in libraries) {
			TRUE %print% paste("load library: ", nn, sep = "")
			if (catch) { #CATCH.ERROR (((())))))
				error <- try(iniLibrary(nn), silent = debug)
				if (checkOfTryError(error, typ = LIB)) {
					libErrorText <- c(libErrorText, nn)
				} 
			} else {
				iniLibrary(nn, debug = debug)
			}
		}	
		
		if (length(libErrorText) > 0) {
			ckeckIfReportTexIsThere(libErrorText, typ = LIB, debug = debug)
#			stop("Not all necessary libs are installed!", call. = FALSE)
			stop("There were problems loading the Libs!", call. = FALSE)
		}
	}	
}

##
#	check if the necessary libs have at least the required basic version
##
checkVersionsOfUsedPackages <- function(lib, outOfDateLib, debug = FALSE) {
	debug %debug% "checkVersionsOfUsedPackages()"
	
	if (!is.null(sessionInfo()$otherPkgs[[lib]]) && !is.null(sessionInfo()$otherPkgs[[lib]]$Version)) {
		localVersion <- sessionInfo()$otherPkgs[[lib]]$Version
		
		minVersion <- switch(lib, 
				Cairo = "1.5-2", 
				RColorBrewer = "1.0-5", 
				data.table = "1.8.6", 
				ggplot2 = "0.9.2.1", 
				fmsb = "0.3.4", 
				snow = "0.3-10", 
				snowfall = "1.84", 
				stringr = "0.6.1")
		
		if (localVersion >= minVersion) {
			return(outOfDateLib)
		} else {
			return(c(outOfDateLib, lib))
		}
	}
	return(outOfDateLib)
}

##
#	load libs
##
iniLibrary <- function(lib, debug = FALSE) {
	debug %debug% "iniLibrary()"
	
	if (DO.PARALLELISATION) {
		if (sfParallel()) {
			sfLibrary(lib, character.only = TRUE)
		} else {
			library(lib, character.only = TRUE)
		}
	} else {
		library(lib, character.only = TRUE)
	}
}

buildDataSet <- function(workingDataSet, overallResult, colname, index, diagramTyp = "") {
	if (length(colname) > 0) {
		for (jj in seq(along = colname)) {
			if (diagramTyp == SPIDER.PLOT) {
				searchVector <- gsub("\\.[0-9]*", "", colnames(overallResult)) %in% paste(colname[jj], index, sep = "")	
			} else {
				searchVector <- paste(colname[jj], index, sep = "")
			}		
			workingDataSet = cbind(workingDataSet, overallResult[searchVector])
		}	
		return(workingDataSet)
	}
}

reNameSpin <- function(colNameWichMustBind, colNames) {
	
	descriptorListIndex <- strsplit(substr(colNameWichMustBind, nchar(colNames$colOfMean)+1, nchar(colNameWichMustBind)), "\\.")
	
	if (descriptorListIndex[[1]][1] != "" & descriptorListIndex[[1]][2] != "") {
		return(colNames$desNames[[descriptorListIndex[[1]][1]]][descriptorListIndex[[1]][2], ])
	} else {
		return(colNameWichMustBind)
	}
}


reNameHist <- function(colNameWichMustBind) {
	colNameWichMustBind = as.character(colNameWichMustBind)
	positions = which(strsplit(colNameWichMustBind, '')[[1]] == '.')
	colNameWichMustBind = substr(colNameWichMustBind, positions[length(positions)]+1, nchar(colNameWichMustBind))
	
	regExpressionSpezialCharacter = "\\_"
	colNameWichMustBind = gsub(regExpressionSpezialCharacter, "..", colNameWichMustBind)
	
	return(colNameWichMustBind)	
}

reNameColumn <- function(plotThisValues, columnNameReplace = NAME, columnNameWhichUsedToReplace = PRIMAER.TREATMENT) {
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

checkOfTreatments <- function(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug = FALSE) {
	debug %debug% "checkOfTreatments()"
	
	treatment <- treatment %exists% args[3]
	secondTreatment <- secondTreatment %exists% args[4]
	secondTreatment <- treatment %checkEqual% secondTreatment
	
	listOfTreat <- list(treatment = treatment, secondTreatment = secondTreatment)
	listOfFilterTreat <- list(filterTreatment = filterTreatment, filterSecondTreatment = filterSecondTreatment)
	
	if (treatment == NONE && secondTreatment == NONE) {
		listOfTreat$treatment <- NONE.TREATMENT
		listOfTreatAndFilterTreat <- list(listOfTreat, listOfFilterTreat)
	} else {	
		for (k in names(listOfTreat)) {
			if (listOfTreat[[k]] != NONE) {
				descriptorVector <- getVector(preprocessingOfDescriptor(listOfTreat[[k]], workingDataSet, debug = debug), debug = debug)
				
				if (!is.null(descriptorVector)) {
					descriptorVector <- getVector(checkIfDescriptorIsNaOrAllZero(descriptorVector, workingDataSet, FALSE), debug = debug)
					
					if (all(!is.na(descriptorVector))) { #!is.null(descriptorVector)
						listOfTreat[[k]] <- descriptorVector
					} 
				} 
				
				if (all(is.na(descriptorVector))) { # is.null(descriptorVector)
					ownCat(paste(k, " set to '", NONE, "'", sep = ""))
					
					listOfTreat[[k]] <- NONE
				}
			}
		}
		
		listOfTreatAndFilterTreat <- changeWhenTreatmentNoneAndSecondTreatmentNotNone(listOfTreat, listOfFilterTreat)
	}	
	return(listOfTreatAndFilterTreat)
}

overallCheckIfDescriptorIsNaOrAllZero <- function(overallList) {
	overallList$debug %debug% "overallCheckIfDescriptorIsNaOrAllZero()"	
	
	if (!is.null(overallList$nBoxDes) && sum(!is.na(overallList$nBoxDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$nBoxDes), "nBoxplots..."));}	## <--- debug %print% Text anpassen!!!
		for (n in seq(along = overallList$nBoxDes)) {
			if (!is.na(overallList$nBoxDes[[n]][1])) {
				overallList$nBoxDes[n] <- checkIfDescriptorIsNaOrAllZero(overallList$nBoxDes[[n]], overallList$iniDataSet, debug = overallList$debug)
			}
		}
		names(overallList$nBoxDes) <- c(1:length(overallList$nBoxDes))
	} else {
		ownCat("All values for nBoxplot are 'NA'")
		
	}
	
	
	if (!is.null(overallList$nBoxMultiDes) && sum(!is.na(overallList$nBoxMultiDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$nBoxMultiDes), "nBoxMultiPlots..."));}
		for (n in seq(along = overallList$nBoxMultiDes)) {
			if (!is.na(overallList$nBoxMultiDes[[n]][1])) {
				overallList$nBoxMultiDes[n] <- checkIfDescriptorIsNaOrAllZero(overallList$nBoxMultiDes[[n]], overallList$iniDataSet, debug = overallList$debug)
			}
		}
		names(overallList$nBoxMultiDes) <- c(1:length(overallList$nBoxMultiDes))
	} else {
		ownCat("All values for nBoxMultiPlot are 'NA'")
		
	}
	
	if (!is.null(overallList$boxDes) && sum(!is.na(overallList$boxDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxDes), "Boxplots..."))}
		for (n in seq(along = overallList$boxDes)) {
			if (!is.na(overallList$boxDes[[n]][1])) {
				overallList$boxDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$boxDes[[n]], overallList$iniDataSet, debug = overallList$debug)
			}
		}
		names(overallList$boxDes) <- c(1:length(overallList$boxDes))
	} else {
		ownCat("All values for Boxplot are 'NA'")
		
	}
	
	if (!is.null(overallList$boxStackDes) && sum(!is.na(overallList$boxStackDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxStackDes), "stacked boxplots..."))}
		for (n in seq(along = overallList$boxStackDes)) {
			if (!is.na(overallList$boxStackDes[[n]][1])) {
				overallList$boxStackDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$boxStackDes[[n]], overallList$iniDataSet, debug = overallList$debug)
			}
		}
		names(overallList$boxStackDes) <- c(1:length(overallList$boxStackDes))
	} else {
		ownCat("All values for stackedBoxplot are 'NA'")
		
	}
	
	if (!is.null(overallList$boxSpiderDes) && sum(!is.na(overallList$boxSpiderDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$boxSpiderDes), "spiderplots..."))}
		for (n in seq(along = overallList$boxSpiderDes)) {
			if (!is.na(overallList$boxSpiderDes[[n]][1])) {
				initDescriptor <- overallList$boxSpiderDes[[n]]
				overallList$boxSpiderDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$boxSpiderDes[[n]], overallList$iniDataSet, debug = overallList$debug)
				booleanVector <- unlist(initDescriptor) %in% unlist(overallList$boxSpiderDes[[n]])
				overallList$boxSpiderDesName[[n]] <- as.data.frame(overallList$boxSpiderDesName[[n]][booleanVector])
				
			}
		}
		names(overallList$boxSpiderDes) <- c(1:length(overallList$boxSpiderDes))
		names(overallList$boxSpiderDesName) <- c(1:length(overallList$boxSpiderDesName))
	} else {
		ownCat("All values for spider plot are 'NA'")
		
	}
	
	if (!is.null(overallList$linerangeDes) && sum(!is.na(overallList$linerangeDes)) > 0) {
		if (overallList$debug) {ownCat(paste(length(overallList$linerangeDes), "linerange..."))}
		for (n in seq(along = overallList$linerangeDes)) {
			if (!is.na(overallList$linerangeDes[[n]][1])) {
				initDescriptor <- overallList$linerangeDes[[n]]
				overallList$linerangeDes[[n]] <- checkIfDescriptorIsNaOrAllZero(overallList$linerangeDes[[n]], overallList$iniDataSet, debug = overallList$debug)
				booleanVector <- unlist(initDescriptor) %in% unlist(overallList$linerangeDes[[n]])
				overallList$linerangeDesName[[n]] <- as.data.frame(overallList$linerangeDesName[[n]][booleanVector])
				
			}
		}
		names(overallList$linerangeDes) <- c(1:length(overallList$linerangeDes))
		names(overallList$linerangeDesName) <- c(1:length(overallList$linerangeDesName))
	} else {
		ownCat("All values for spider plot are 'NA'")
		
	}
	
	if (!is.null(overallList$violinBoxDes) && sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
		if (overallList$debug) {ownCat(paste(length(overallList$violinBoxDes), "violinplot..."))}
		for (n in seq(along = overallList$violinBoxDes)) {
			if (!is.na(overallList$violinBoxDes[[n]][1])) {
				overallList$violinBoxDes[n] <- checkIfDescriptorIsNaOrAllZero(overallList$violinBoxDes[[n]], overallList$iniDataSet, debug = overallList$debug)
			}
		}
		names(overallList$violinBoxDes) <- c(1:length(overallList$violinBoxDes))
	} else {
		ownCat("All values for violin plot are 'NA'")
		
	}
	
	
	if ((!sum(!is.na(overallList$nBoxDes)) > 0 && 
				!sum(!is.na(overallList$nBoxMultiDes)) > 0 && 
				!sum(!is.na(overallList$boxStackDes)) > 0 && 
				!sum(!is.na(overallList$boxDes)) > 0 && 
				!sum(!is.na(overallList$boxSpiderDes)) > 0 && 
				!sum(!is.na(overallList$violinBoxDes))) > 0) {
		ownCat("No descriptor set (all descriptors are zero or NA) - the program needs to stop!")
		overallList$stoppTheCalculation <- TRUE 
	}
	
	return(overallList)
}


checkIfDescriptorIsNaOrAllZero <- function(descriptorVector, iniDataSet, isDescriptor = TRUE, debug = FALSE) {
	#############
#descriptorVector <- overallList$nBoxDes[[n]]
#iniDataSet <- overallList$iniDataSet
#isDescriptor <- TRUE
	#############
	
	#overallList$debug %debug% "checkIfDescriptorIsNaOrAllZero()"
	descriptorVector <- as.vector(descriptorVector)
	#descriptorVector = getVector(descriptorVector)
	tempDescriptor <- descriptorVector 
	
	if (isDescriptor) {
		descriptorVector <- descriptorVector[colSums(!is.na(iniDataSet[descriptorVector])) != 0 & colSums(iniDataSet[descriptorVector] *1, na.rm = TRUE) != 0]
	} else {
		descriptorVector <- descriptorVector[colSums(!is.na(iniDataSet[descriptorVector])) != 0]
	}
	errorDescriptor <- tempDescriptor %GetDescriptorAfterCheckIfDescriptorNotExists% descriptorVector
	
	if (length(errorDescriptor) > 0 && debug) {
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
	
	if (!is.null(overallList$imageFileNames_nBoxMultiPlots)) {
		if (overallList$debug) {ownCat("multi line plots..."); }
		overallList$imageFileNames_nBoxMultiPlots <- changefileName(overallList$imageFileNames_nBoxMultiPlots, NBOX.MULTI.PLOT)
		names(overallList$imageFileNames_nBoxMultiPlots) <- c(1:length(overallList$imageFileNames_nBoxMultiPlots)) ########################################################
		
		overallList$nBoxMultiDesName <- as.list(overallList$nBoxMultiDesName)
		names(overallList$nBoxMultiDesName) <- c(1:length(overallList$nBoxMultiDesName))
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
	
	if (!is.null(overallList$imageFileNames_LinerangePlots)) {
		if (overallList$debug) {ownCat("linerangeplots...");}
		overallList$imageFileNames_LinerangePlots <- changefileName(overallList$imageFileNames_LinerangePlots, LINERANGE.PLOT)
		names(overallList$imageFileNames_LinerangePlots) <- c(1:length(overallList$imageFileNames_LinerangePlots))
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
			} else if (n == NBOX.MULTI.PLOT) {
				return(setOptions(overallList, NBOX.MULTI.PLOT, "nBoxMultiOptions", listOfExtraOptions))
			}else if (n == STACKBOX.PLOT) {
				return(setOptions(overallList, STACKBOX.PLOT, "stackedBarOptions", listOfExtraOptions))
			} else if (n == SPIDER.PLOT) {
				return(setOptions(overallList, SPIDER.PLOT, "spiderOptions", listOfExtraOptions))
			} else if (n == LINERANGE.PLOT) {
				return(setOptions(overallList, LINERANGE.PLOT, "linerangeOptions", listOfExtraOptions))
			}else if (n == VIOLIN.PLOT & overallList$isRatio) {
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
		if (length(grep("barley", overallList$iniDataSet$Species[1], ignore.case = TRUE)) > 0) {
			typ = "barley"
		} else if (length(grep("maize", overallList$iniDataSet$Species[1], ignore.case = TRUE)) > 0) {
			typ = "maize"
		} else if (length(grep("arabidopsis", overallList$iniDataSet$Species[1], ignore.case = TRUE)) > 0) {
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
	
	if (typOfPlot == STACKBOX.PLOT) {
		for(i in 1:length(fileNameVector)) {
			if (length(grep("\\.bin\\.", fileNameVector[i], ignore.case = TRUE)) > 0) {
				stringSplit <- paste(strsplit(fileNameVector[i], '\\.bin\\.')[[1]][1], ".bin.", sep = "")
				fileNameVector[i] <- stringSplit
			} 
		}
	}
	
	return(as.list(fileNameVector))
}
############ hier geht es weiter #########################
preprocessingOfValues <- function(value, isColValue = FALSE, replaceString = ".", isColName = FALSE, debug = FALSE) {
	debug %debug% "preprocessingOfValues()"	
	
	if (!is.null(value)) {
		regExpressionCol = "[^[:alnum:]|^_]|[[:space:]|\\^]"
		if (isColValue || isColName) {
			value <- strsplit(as.character(value), "$", fixed = TRUE)
		}
		
		if (!isColName) {
			for (n in seq(along = value)) {
				value[[n]] <- gsub(regExpressionCol, replaceString, value[[n]])
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
		if (overallList$debug) {ownCat(NBOX.PLOT)} ## <---- das noch anpassen "overallList$debug %print% NBOX.PLOT"
		for (n in seq(along = overallList$nBoxDes)) {
			overallList$nBoxDes[n] = preprocessingOfDescriptor(overallList$nBoxDes[[n]], overallList$iniDataSet, overallList$debug)
		}
		overallList$nBoxDes <- checkOfNormalizedAndUnnormalized(overallList$nBoxDes)
	} else {
		ownCat(paste(NBOX.PLOT, " is NULL", sep = ""))
		
	} 
	
	if (!is.null(overallList$nBoxMultiDes)) {
		if (overallList$debug) {ownCat(NBOX.MULTI.PLOT)}
		for (n in seq(along = overallList$nBoxMultiDes)) {
			overallList$nBoxMultiDes[n] = preprocessingOfDescriptor(overallList$nBoxMultiDes[[n]], overallList$iniDataSet, overallList$debug)
		}
		overallList$nBoxMultiDes <- checkOfNormalizedAndUnnormalized(overallList$nBoxMultiDes)
	} else {
		ownCat(paste(NBOX.MULTI.PLOT, " is NULL", sep = ""))
		
	} 
	
	if (!is.null(overallList$boxDes)) {
		if (overallList$debug) {ownCat(BOX.PLOT)}
		for (n in seq(along = overallList$boxDes)) {
			overallList$boxDes[n] = preprocessingOfDescriptor(overallList$boxDes[[n]], overallList$iniDataSet, overallList$debug)
		}
		overallList$boxDes <- checkOfNormalizedAndUnnormalized(overallList$boxDes)
	} else {
		ownCat(paste(BOX.PLOT, " is NULL", sep = ""))
		
	} 
	
	if (!is.null(overallList$boxStackDes)) {
		if (overallList$debug) {ownCat(STACKBOX.PLOT)}
		for (n in seq(along = overallList$boxStackDes)) {
			overallList$boxStackDes[n] <- preprocessingOfDescriptor(overallList$boxStackDes[[n]], overallList$iniDataSet, overallList$debug)
		}	
#		print("test_stacked")
#		print(overallList$boxStackDes)
		overallList$boxStackDes <- checkOfNormalizedAndUnnormalized(overallList$boxStackDes)			
		
	} else {
		ownCat(paste(STACKBOX.PLOT, " is NULL", sep = ""))
		
	} 
	
	if (!is.null(overallList$boxSpiderDes)) {
		if (overallList$debug) {ownCat(SPIDER.PLOT)}
		for (n in seq(along = overallList$boxSpiderDes)) {
			initDescriptor <- preprocessingOfValues(overallList$boxSpiderDes[n], isColValue = TRUE)
			overallList$boxSpiderDes[n] = preprocessingOfDescriptor(overallList$boxSpiderDes[[n]], overallList$iniDataSet, overallList$debug)
			booleanVector <- initDescriptor[[1]] %in% overallList$boxSpiderDes[n][[1]]
			#print(booleanVector)
			#print(as.data.frame(preprocessingOfValues(overallList$boxSpiderDesName[[n]], isColName = TRUE)[[1]][booleanVector]))
			if (sum(booleanVector) > 0) {
				overallList$boxSpiderDesName[n] = as.data.frame(preprocessingOfValues(overallList$boxSpiderDesName[[n]], isColName = TRUE)[[1]][booleanVector])
			} else {
				overallList$boxSpiderDesName[n] <- NA
			}
		}
		#print(overallList$boxSpiderDesName)
		overallList$boxSpiderDesName <- checkOfNormalizedAndUnnormalized(overallList$boxSpiderDesName)
		
	} else {
		ownCat(paste(SPIDER.PLOT, " is NULL", sep = ""))
		
	} 
	
	if (!is.null(overallList$linerangeDes)) {
		if (overallList$debug) {ownCat(LINERANGE.PLOT)}
		for (n in seq(along = overallList$linerangeDes)) {
			initDescriptor <- preprocessingOfValues(overallList$linerangeDes[n], isColValue = TRUE)
			overallList$linerangeDes[n] = preprocessingOfDescriptor(overallList$linerangeDes[[n]], overallList$iniDataSet, overallList$debug)
			booleanVector <- initDescriptor[[1]] %in% overallList$linerangeDes[n][[1]]
			if (sum(booleanVector) > 0) {
				overallList$linerangeDesName[n] = as.data.frame(preprocessingOfValues(overallList$linerangeDesName[[n]], isColName = TRUE)[[1]][booleanVector])
			} else {
				overallList$linerangeDesName[n] <- NA
			}
		}
		overallList$linerangeDes <- checkOfNormalizedAndUnnormalized(overallList$linerangeDes)
		
	} else {
		ownCat(paste(LINERANGE.PLOT, " is NULL", sep = ""))
		
	} 
	
	if (!is.null(overallList$violinBoxDes) & overallList$isRatio) {
		if (overallList$debug) {ownCat(VIOLIN.PLOT)}
		for (n in seq(along = overallList$violinBoxDes)) {
			initDescriptor <- preprocessingOfValues(overallList$violinBoxDes[n], isColValue = TRUE)
			overallList$violinBoxDes[n] = preprocessingOfDescriptor(overallList$violinBoxDes[[n]], overallList$iniDataSet, overallList$debug)
			booleanVector <- initDescriptor[[1]] %in% overallList$violinBoxDes[n][[1]]
			if (sum(booleanVector) > 0) {
				overallList$violinBoxDesName[n] = as.data.frame(preprocessingOfValues(overallList$violinBoxDesName[[n]], isColName = TRUE)[[1]][booleanVector])
			} else {
				overallList$violinBoxDesName[n] <- NA
			}
		}
		overallList$violinBoxDes <- checkOfNormalizedAndUnnormalized(overallList$violinBoxDes)
		
	} else {
		ownCat("No ratio data set/violin plot")
		
	} 
	
	if ((!sum(!is.na(overallList$nBoxDes)) > 0 &&
				!sum(!is.na(overallList$nBoxMultiDes)) > 0 &&
				!sum(!is.na(overallList$boxStackDes)) > 0 && 
				!sum(!is.na(overallList$boxDes)) > 0 && 
				!sum(!is.na(overallList$boxSpiderDes)) > 0 && 
				!sum(!is.na(overallList$violinBoxDes))) > 0) {
		ownCat("No descriptor set - this run needs to stop!")
		overallList$stoppTheCalculation = TRUE
	}
	return(overallList)
}

parseToR <- function(text) {
	text <- gsub("(", ".", text, fixed = TRUE)
	text <- gsub(")", ".", text, fixed = TRUE)
	text <- gsub(" ", ".", text, fixed = TRUE)
	text <- gsub("^", ".", text, fixed = TRUE)
	text <- gsub("/", ".", text, fixed = TRUE)
	text <- gsub("%", ".", text, fixed = TRUE)
	
	return(text)
}

searchUnit <- function(desString, unitList) {
	######
#desString <- nn
	######
	unit <- "no unit"
	desString <- parseToR(desString)
	unitPosition <- str_locate(desString, fixed(getVector(unitList$unitR)))
	unitPosition <- as.data.frame(na.omit(unitPosition))
	if (nrow(unitPosition) != 0) {
		unit <- as.character(unitList[unitList$unitR == str_sub(desString, unitPosition$start[1], nchar(desString)), "unitTex"])
		if (length(unit) == 0 || unit == "0") {
			unit <- "no unit"
		}
	}	
	return(unit)
}

buildLongUnitTable <- function(descAndUnitDataFrame) {
	ownCat("... create unit table")
	columnName <- c("Descriptor", "Unit")
	writeLatexTable(LONG.UNIT.TABLE, columnName, columnWidth = c("13cm", "3cm"))
	
	for(kk in seq(along = descAndUnitDataFrame$des)) {
		writeLatexTable(LONG.UNIT.TABLE, value = c(descAndUnitDataFrame$desName[[kk]], descAndUnitDataFrame$des[[kk]]))
	}
	
#	apply(descAndUnitDataFrame, 1, function(x, y) {
#					print(y)
#					print(x)
#					print("####")
#					#writeLatexTable(y, getVector(x))
#				}, LONG.UNIT.TABLE)
	writeLatexTable(LONG.UNIT.TABLE)
}


buildDataFrame <- function(allDesNN, allDesNameNN, allValues) {
	des <- getVector(allDesNN)
	boolV <- is.na(des)
	des <- des[!boolV]
	desName <- getVector(allDesNameNN)[!boolV]
	allValues <- rbind(allValues, data.frame(des = des, desName = desName, stringsAsFactors = FALSE))
	return(allValues)
}

builAllUnitVector <- function(allDes, allDesName, allTypOfPlot) {
	
	allValues <- data.frame()
	for(nn in seq(along = allTypOfPlot)) {
		if (allTypOfPlot[[nn]] == STACKBOX.PLOT) {
			for(kk in seq(along = allDes[[nn]])) {
				if (!(all(is.na(allDes[[nn]][[kk]])) || all(is.na(allDesName[[nn]][[kk]])))) {
					allValues <- buildDataFrame(allDes[[nn]][[kk]], rep.int(allDesName[[nn]][[kk]], nrow(allDes[[nn]][[kk]])), allValues)
				}
			}		
		} else {
			allValues <- buildDataFrame(allDes[[nn]], allDesName[[nn]], allValues)
		}
	}
	return(allValues)
}



getUnits <- function(overallList) {
	overallList$debug %debug% "getUnits()"
	
	allDes <- list(overallList$boxDes, overallList$violinBoxDes, overallList$linerangeDes, overallList$boxSpiderDes, overallList$boxStackDes, overallList$nBoxDes, overallList$nBoxMultiDes)
	allDesName <- list(overallList$boxDesName, overallList$violinBoxDesName, overallList$linerangeDesName, overallList$boxSpiderDesName, overallList$boxStackDesName, overallList$nBoxDesName, overallList$nBoxMultiDesName)
	allTypOfPlot <- list(BOX.PLOT, VIOLIN.PLOT, LINERANGE.PLOT, SPIDER.PLOT, STACKBOX.PLOT, NBOX.PLOT, NBOX.MULTI.PLOT)
	
	allDes <- builAllUnitVector(allDes, allDesName, allTypOfPlot)	
	allDes <- allDes[-which(duplicated(allDes$des)), ]
	
	unitList <- getUnitList()
	
#	test <- apply(allDes, 1, function(x, unitList) {
#			x[1] <- searchUnit(x[1], unitList)
#			x[2] <- cleanSubtitle(getVector(x[2]))
#			return(x)
#	}, unitList)
	
	
	for(kk in seq(along = allDes$des)) {
		allDes$des[[kk]] <- searchUnit(allDes$des[[kk]], unitList)
		allDes$desName[[kk]] <- cleanSubtitle(allDes$desName[[kk]])
	}
	allDes <- na.omit(allDes)
	allDes <- unique(allDes)
	buildLongUnitTable(allDes)
	return(overallList)
}



getUnitList <- function(column = NULL) {
	unitList <- data.frame(
			unitR = 
					c(".percent.", 
							".mm.2.", 
							".mm.", 
							".g.", 
							".relative...pix.", 
							".relative.", 
							"sqrt.px.3.", 
							".px.3.", 
							".px.", 
							".tassel.", 
							".sum.of.day.", 
							".leafs.", 
							"...day.", 
							"...", #entspricht (%)
							".c.p."
					), 
			unitTex = 
					c("%", 
							"mm^2", 
							"mm", 
							"g", 
							"relative/px", 
							"relative", 
							"sqrt(px^3)", 
							"px^3", 
							"px", 
							"tassel", 
							"g", 
							"leafs", 
							"%/day", 
							"%", #entspricht (%)
							"c/p"
					), 
			unitExcel = 
					c("(percent.", 
							"(mm^2)", 
							"(mm)", 
							"(g)", 
							"(relative / pix)", 
							"(relative)", 
							"sqrt(px^3)", 
							"(px^3)", 
							"(px)", 
							".tassel)", 
							"(sum of day)", 
							"(leafs)", 
							"(%/day)", 
							"(%)", #entspricht (%)
							"(c/p)"
					)
	)
	if (is.null(column)) {
		return(unitList)
	} else {
		return(getVector(unitList[, column]))
	}
}

replaceBrakesWithNothing <- function(label, unit = "(", debug = FALSE) {
	debug %debug% "replaceBrakesWithNothing()"

	endValue <- str_locate(label, fixed(unit))[, "start"]-1
	endValue[is.na(endValue)] <- -1L
	label <- str_sub(label, 1L, endValue)
	label <- str_trim(label)
	return(label)
}


replaceUnits <- function(label, typ = "unitR") {
	
	unitList <- getUnitList(typ)
	
	for(nn in unitList) {
		label <- str_replace_all(label, fixed(nn), "")
	}
	
#	label <- str_replace_all(label, fixed(".percent."), "")
#	label <- str_replace_all(label, fixed(".mm.2."), "")
#	label <- str_replace_all(label, fixed(".mm."), "")
#	label <- str_replace_all(label, fixed(".g."), "")
#	label <- str_replace_all(label, fixed(".relative...pix."), "")
#	label <- str_replace_all(label, fixed(".relative."), "")
#	label <- str_replace_all(label, fixed(".px.3."), "")
#	label <- str_replace_all(label, fixed(".px."), "")
#	label <- str_replace_all(label, fixed(".tassel."), "")
#	label <- str_replace_all(label, fixed(".sum.of.day."), "")
#	label <- str_replace_all(label, fixed(".leafs."), "")
#	label <- str_replace_all(label, fixed("...day."), "")
#	label <- str_replace_all(label, fixed("..."), "") #entspricht (%)
	label <- str_trim(label)
	
	return(label)
}


reduceAllUnits <- function(allDes) {
	
	for(nn in seq(along = allDes)) {
		label <- getVector(allDes[[nn]])
		label <- replaceUnits(label)
		allDes[[nn]] <- label
	}
	
	return(allDes)
}

checkOfNormalizedAndUnnormalized <- function(allDes) {
	#########
#allDes <- overallList$boxStackDes
#allDes <- overallList$nBoxDes
#allDes <- overallList$linerangeDes
	#########
	
	tempAllDes <- allDes
	allDes <- reduceAllUnits(allDes)
	for(kk in seq(along = allDes)) {
		## das unlist bei nbox überprüfen
		vector <- unlist(allDes[kk])
		if ((length(grep("normalized.", vector , ignore.case = TRUE)) > 0) ||
				(length(grep("norm.", vector , ignore.case = TRUE)) > 0) ||
				(length(grep(".normalized", vector , ignore.case = TRUE)) > 0) ||
				(length(grep(".norm", vector , ignore.case = TRUE)) > 0)) {
			nnUn <- gsub("normalized.", "", vector)
			nnUn <- gsub("norm.", "", nnUn)
			nnUn <- gsub(".normalized", "", nnUn)
			nnUn <- gsub(".norm", "", nnUn)
			
			for(nn in seq(along = allDes)) {
				#if (sum(nnUn %in% unlist(allDes[nn])) == length(unlist(allDes[nn]))) {
				if (identical(as.character(nnUn), as.character(allDes[[nn]]))) {
					#	print("raus damit")
					tempAllDes[[nn]] <- NA
				} 
			}
		}
	}	
	return(tempAllDes)
}


preprocessingOfDescriptor <- function(descriptorVector, iniDataSet, debug = FALSE) {
	debug %debug% "preprocessingOfDescriptor()"	
	
	descriptorVector = unlist(preprocessingOfValues(descriptorVector, isColValue = TRUE))
	errorDescriptor = descriptorVector %GetDescriptorAfterCheckIfDescriptorNotExists% iniDataSet 
	descriptorVector = descriptorVector %GetDescriptorsAfterCheckIfDescriptorExists% iniDataSet
	
	if (length(errorDescriptor)>0 && debug) {
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
	overallList$xAxis = unlist(preprocessingOfValues(overallList$xAxis, TRUE, debug = overallList$debug))
	
	if (overallList$filterXaxis != NONE) {
#		print(overallList$filterXaxis)
#		print(strsplit(overallList$filterXaxis, "$", fixed = TRUE))
#		print(strsplit(overallList$filterXaxis, "$", fixed = TRUE)[[1]])
#		print(as.numeric(strsplit(overallList$filterXaxis, "$", fixed = TRUE)[[1]]))
		overallList$filterXaxis = as.numeric(strsplit(overallList$filterXaxis, "$", fixed = TRUE)[[1]])
	} else {
		overallList$filterXaxis = as.numeric(unique(overallList$iniDataSet[overallList$xAxis])[[1]])	#xAxis muss einen Wert enthalten ansonsten Bricht das Program weiter oben ab
	}
	return(overallList)
}

getSingelFilter <- function(filter, treatment, dataSet) {
	if (filter != NONE) {
		return(strsplit(filter, "$", fixed = TRUE)[[1]])
	} else {
		if (all(is.na(as.character(unique(dataSet[treatment])[[1]])))) {
			return("")
		} else {
			return(as.character(unique(dataSet[treatment])[[1]]))
		}
	}
}

preprocessingOfTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheTreatment()"
	
	if (!is.null(overallList$treatment)) {
		overallList$treatment = preprocessingOfValues(overallList$treatment)
		
		if (overallList$treatment != NONE & (overallList$treatment %checkIfDescriptorExists% overallList$iniDataSet)) {	
			overallList$filterTreatment = getSingelFilter(overallList$filterTreatment, overallList$treatment, overallList$iniDataSet)
			if (any(overallList$filterTreatment == "")) {
				if (all(overallList$filterTreatment == "")) {
					overallList <- setTreatmentToNull(overallList)
				} else {
					overallList$filterTreatment <- overallList$filterTreatment[!overallList$filterTreatment == ""]
					ownCat(paste("reduce \"''\" in 'filterTreatment'!", sep = ""))
				}
			}
		} else {
			overallList <- setTreatmentToNull(overallList)
#			overallList$treatment = NONE
#			overallList$filterTreatment = NONE
#			ownCat(paste("Set 'filterTreatment' and 'treatment' to '", NONE, "'!", sep = ""))
			
		}			
	} else {
		overallList <- setTreatmentToNull(overallList)
#		overallList$treatment = NONE
#		overallList$filterTreatment = NONE
#		ownCat(paste("Set 'filterTreatment' and 'treatment' to '", NONE, "'!", sep = ""))
		
	}
	return(overallList)
}

setTreatmentToNull <- function(overallList) {
	overallList$treatment = NONE
	overallList$filterTreatment = NONE
	ownCat(paste("Set 'filterTreatment' and 'treatment' to '", NONE, "'!", sep = ""))
	return(overallList)
}

setSecondTreatmentToNull <- function(overallList) {
	overallList$secondTreatment <- NONE
	overallList$filterSecondTreatment <- NONE
	overallList$splitTreatmentSecond <- FALSE
	ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '", NONE, "' and 'splitTreatmentSecond' to 'FALSE'!", sep = ""))
	return(overallList)
}

preprocessingOfSecondTreatment <- function(overallList) {
	overallList$debug %debug% "preprocessingOfTheSecondTreatment()"
	
	if (!is.null(overallList$secondTreatment)) {
		overallList$secondTreatment <- preprocessingOfValues(overallList$secondTreatment)
		
		if (overallList$secondTreatment != NONE & (overallList$secondTreatment %checkIfDescriptorExists% overallList$iniDataSet)) {
			overallList$filterSecondTreatment <- getSingelFilter(overallList$filterSecondTreatment, overallList$secondTreatment, overallList$iniDataSet)
			
			if (any(overallList$filterSecondTreatment == "")) {
				if (all(overallList$filterSecondTreatment == "")) {
					overallList <- setSecondTreatmentToNull(overallList)
				} else {
					overallList$filterSecondTreatment <- overallList$filterSecondTreatment[!overallList$filterSecondTreatment == ""]
					ownCat(paste("reduce \"''\" in 'filterSecondTreatment'!", sep = ""))
				}
			}
			
			if (length(overallList$filterSecondTreatment) == 1) {
				overallList <- setSecondTreatmentToNull(overallList)
#				overallList$secondTreatment <- NONE
#				overallList$filterSecondTreatment <- NONE
#				overallList$splitTreatmentSecond <- FALSE
#				ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '", NONE, "' and 'splitTreatmentSecond' to 'FALSE'!", sep = ""))
			}
			
		} else {
			overallList <- setSecondTreatmentToNull(overallList)
#			overallList$secondTreatment <- NONE
#			overallList$filterSecondTreatment <- NONE
#			overallList$splitTreatmentSecond <- FALSE
#			ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '", NONE, "' and 'splitTreatmentSecond' to 'FALSE'!", sep = ""))
			
		}	
	} else {
		overallList <- setSecondTreatmentToNull(overallList)
#		overallList$secondTreatment <- NONE
#		overallList$filterSecondTreatment <- NONE
#		overallList$splitTreatmentSecond <- FALSE
#		ownCat(paste("Set 'filterSecondTreatment', 'secondTreatment' to '", NONE, "' and 'splitTreatmentSecond' to 'FALSE'!", sep = ""))
		
	}
	return(overallList)
}

check <- function(value, checkValue = c(NONE, NA)) {
	if (!is.null(value)) {
		return(value %GetDescriptorAfterCheckIfDescriptorNotExists% checkValue)
		#return(unique(value %GetDescriptorAfterCheckIfDescriptorNotExists% checkValue))
	} else {
		return(character(0))
	}
}

getVector <- function(descriptorSet, specialList = NULL, debug = FALSE) {
	debug %debug% "getVector()"
	
	if (!is.null(descriptorSet)) {
		vector <- vector()
		if (class(descriptorSet) == "data.frame") {
			lengthVec <- length(descriptorSet[, 1])
		} else {
			lengthVec <- length(descriptorSet)
		}
		
		for (n in 1:lengthVec) {
			if (class(descriptorSet) == "data.frame") {
				vector = c(vector, as.vector(descriptorSet[n, ]))
			} else {
				if (!is.null(specialList)) {
					addVector <- as.vector(unlist(descriptorSet[[n]][specialList]))
					if (is.null(addVector)) {
						addVector <- 0
					}		
					
					vector = c(vector, addVector)
				} else {
					vector = c(vector, as.vector(unlist(descriptorSet[[n]])))
				}
			}
		}
		return(vector)
	}
	return(character(0))
}


getRealNameAndPrintSection <- function(descriptorSet, debug = FALSE) {
	debug %debug% "getRealNameAndPrintSection()"
	
	columName <- names(descriptorSet)
	columName <- replaceBrakesWithNothing(columName, debug = debug)
	plotName <- getVector(descriptorSet, "plotName", debug = debug)

	section <- getVector(descriptorSet, FIRST.SECTION, debug = debug)		
	subsection <- getVector(descriptorSet, SECOND.SECTION, debug = debug)
	subsubsection <- getVector(descriptorSet, THIRD.SECTION, debug = debug)
	paragraph <- getVector(descriptorSet, FOURTH.SECTION, debug = debug)
	sectionList <- list(section = section, subsection = subsection, subsubsection = subsubsection, paragraph = paragraph)
	
	return(list(columName = columName, plotName = plotName, section = sectionList))
}

reduceWorkingDataSize <- function(overallList) {
	overallList$debug %debug% "reduceWorkingDataSize()"

	if (overallList$isRatio) {
		overallList$iniDataSet <- overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$nBoxMultiDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(getVector(overallList$boxSpiderDes)), check(getVector(overallList$linerangeDes)), check(getVector(overallList$violinBoxDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
	} else {
		overallList$iniDataSet <- overallList$iniDataSet[unique(c(check(getVector(overallList$nBoxDes)), check(getVector(overallList$nBoxMultiDes)), check(getVector(overallList$boxDes)), check(getVector(overallList$boxStackDes)), check(getVector(overallList$boxSpiderDes)), check(getVector(overallList$linerangeDes)), check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment)))]
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

getBooleanVectorForFilterValues <- function(groupedDataFrame, listOfValues, plot = FALSE) {
	###############
#groupedDataFrame <- groupedDataFrameMean
#listOfValues <- booleanVectorList
#plot = FALSE
	###############
	
	iniType = !plot
	tempVector = rep.int(iniType, times = length(groupedDataFrame[, 1]))
	
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
	rowString = list(row = des, day = numeric())
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

buildRowName <- function(mergeDataSet, groupBy, contactTheValues, yName = VALUES) {	
	#####
#mergeDataSet <- iniDataSet
	#####
	
	if (length(groupBy) == 0) {
		return(data.frame(name = rep.int(yName, length(mergeDataSet[, 1])), mergeDataSet))
	} else if (length(groupBy) == 1) {
		return(data.frame(name = mergeDataSet[, groupBy], mergeDataSet[, !(colnames(mergeDataSet) %in% groupBy)]))
	} else {		
		#temp = mergeDataSet[, groupBy[2]]
		temp <- vector()	
		first <- FALSE
		for (hh in seq(along = groupBy)) {
			if (!contactTheValues && !first) {
				first <- TRUE
			} else {
				if (length(unique(mergeDataSet[, groupBy[hh]])) > 1) {
					if (length(temp) > 0) {
						temp <- paste(temp, mergeDataSet[, groupBy[hh]], sep = "/") # #/#
					} else {
						temp <- mergeDataSet[, groupBy[hh]]
					}
				}
			}				
		}
		
		return(data.frame(name = temp, primaerTreatment = mergeDataSet[, groupBy[1]], mergeDataSet[, mergeDataSet %allColnamesWithoutThisOnes% groupBy]))
	}	
}

checkTheNumberOfEntriesOfTheDay <- function(iniPosition, values, uniqueDays) {
	
}


getToPlottedDays <- function(values, nameOfValueColumn, minimumNumberOfValuesOfEachDays, changes = NULL) {
	##########
#
	##########
#tempValues <- values
	uniqueDays = unique(getVector(values[values %allColnamesWithoutThisOnes% nameOfValueColumn]))
	
	if (minimumNumberOfValuesOfEachDays > 1) {
		newValues <- data.frame()
		for(dd in uniqueDays) {
			if (sum(values$xAxis == dd) >= minimumNumberOfValuesOfEachDays) {
				newValues <- rbind(newValues, values[values$xAxis == dd, ])
			}
		}
		values <- newValues
	}
	
	if (length(values)>0) {
		uniqueDays = unique(getVector(values[values %allColnamesWithoutThisOnes% nameOfValueColumn]))
		medianPosition = floor(median(1:length(uniqueDays)))
		
		days = uniqueDays[floor(median(1:length(uniqueDays[uniqueDays <= uniqueDays[medianPosition]])))]
		
		days = c(days, uniqueDays[medianPosition])
		days = c(days, uniqueDays[floor(median(length(uniqueDays[uniqueDays >= uniqueDays[medianPosition]]):length(uniqueDays)))])
		days = c(days, uniqueDays[length(uniqueDays)])
		
		if (!is.null(changes)) {
			days = c(as.numeric(changes), days[(length(changes)+1):4])
		}
	} else {
		days <- NULL
	}
	
	return(days)
	
}

fillWhichDayShouldBePlottedWithNull <- function(whichDayShouldBePlot) {
	
	longestValue <- max(str_length(unique(whichDayShouldBePlot)[!is.na(unique(whichDayShouldBePlot))]))

	for(nn in unique(whichDayShouldBePlot)) {
		if(!is.na(nn)) {	
			if(str_length(nn) < longestValue) {
				whichDayShouldBePlot[whichDayShouldBePlot == nn] <- paste(rep.int("0", longestValue-str_length(nn)), as.character(nn), sep="")
			}
		}
	}
	return(whichDayShouldBePlot)
}


setxAxisfactor <- function(xAxisName, values, nameOfValueColumn, options = NULL, typOfPlot = "") {
	##############
#xAxisName <- overallList$xAxisName
#xAxisValue <- overallResult$xAxis
#nameOfValueColumn <- "value"
#values <- overallResult[c("xAxis", "value")]
	##############
	
	
	minimumNumberOfValuesOfEachDays <- 1
	if (typOfPlot == BOX.PLOT) {
		minimumNumberOfValuesOfEachDays <- 2
	}
	
	
	if (!is.null(options$daysOfBoxplotNeedsReplace)) {
		whichDayShouldBePlot <- getToPlottedDays(values, nameOfValueColumn, minimumNumberOfValuesOfEachDays, options$daysOfBoxplotNeedsReplace)
	} else {
		whichDayShouldBePlot <- getToPlottedDays(values, nameOfValueColumn, minimumNumberOfValuesOfEachDays)
	}
	if (!is.null(whichDayShouldBePlot)) {
		
		
		xAxisfactor <- factor(getVector(values[values %allColnamesWithoutThisOnes% nameOfValueColumn]), levels = unique(whichDayShouldBePlot))
#		levels(xAxisfactor) <- unique(whichDayShouldBePlot)
		
		xAxisfactor <- fillWhichDayShouldBePlottedWithNull(as.character(xAxisfactor))
		xAxisfactor <- paste(xAxisName, xAxisfactor)
		
		naString <- paste(xAxisName, "NA")
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
		groupBy <- groupByFunction(list(overallList$treatment, overallList$secondTreatment))
		colNames <- list(colOfXaxis = "xAxis", colOfMean = "mean", colOfSD = "se", colName = "name", xAxis = overallList$xAxis)
		booleanVectorList <- buildList(overallList, colNames$colOfXaxis)
		columnsStandard <- c(check(overallList$xAxis), check(overallList$treatment), check(overallList$secondTreatment))
		contactTheValues <- FALSE
		
		if ((!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) && overallList$secondTreatment != NONE) {
			contactTheValues <- TRUE
			ownCat("... there should be no split, so the data have to be conected now!")
		}
		
		if (!is.null(overallList$nBoxDes) && sum(!is.na(overallList$nBoxDes)) > 0) {
			if (overallList$debug) {ownCat(NBOX.PLOT)}
			columns <- c(columnsStandard, check(getVector(overallList$nBoxDes)))
			overallList$overallResult_nBoxDes <- getResultDataFrame(NBOX.PLOT, overallList$nBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		} else {
			ownCat(paste("All values for ", NBOX.PLOT, " are 'NA'", sep = ""))
		}
		
		if (!is.null(overallList$nBoxMultiDes) && sum(!is.na(overallList$nBoxMultiDes)) > 0) {
			if (overallList$debug) {ownCat(NBOX.MULTI.PLOT)}
			columns <- c(columnsStandard, check(getVector(overallList$nBoxMultiDes)))
			overallList$overallResult_nBoxMultiDes <- getResultDataFrame(NBOX.MULTI.PLOT, overallList$nBoxMultiDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		} else {
			ownCat(paste("All values for ", NBOX.MULTI.PLOT, " are 'NA'", sep = ""))
		}
		
		if (!is.null(overallList$boxDes) && sum(!is.na(overallList$boxDes)) > 0) {
			if (overallList$debug) {ownCat(BOX.PLOT)}
			colNames$colOfMean = VALUES
			columns = c(columnsStandard, check(getVector(overallList$boxDes)))
			overallList$overallResult_boxDes <- getResultDataFrame(BOX.PLOT, overallList$boxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		} else {
			ownCat(paste("All values for ", BOX.PLOT, " are 'NA'", sep = ""))
		}
		
		if (!is.null(overallList$boxStackDes) && sum(!is.na(overallList$boxStackDes)) > 0) {
			if (overallList$debug) {ownCat(STACKBOX.PLOT)}
			colNames$colOfMean = check(getVector(overallList$boxStackDes))
			colNames$colOfXaxis = overallList$xAxis
			columns = c(columnsStandard, check(getVector(overallList$boxStackDes)))
			overallList$overallResult_boxStackDes = getResultDataFrame(STACKBOX.PLOT, overallList$boxStackDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		} else {
			ownCat(paste("All values for ", STACKBOX.PLOT, " are 'NA'", sep = ""))
		}
		
		if (!is.null(overallList$boxSpiderDes) && sum(!is.na(overallList$boxSpiderDes)) > 0) {
			if (overallList$debug) {ownCat(SPIDER.PLOT)}
			#colNames$colOfMean = check(getVector(overallList$boxSpiderDes))
			colNames$colOfMean = VALUES
			colNames$colOfXaxis = overallList$xAxis
			colNames$desNames = overallList$boxSpiderDesName
			columns = c(columnsStandard, check(getVector(overallList$boxSpiderDes)))
			overallList$overallResult_boxSpiderDes = getResultDataFrame(SPIDER.PLOT, overallList$boxSpiderDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		} else {
			ownCat(paste("All values for ", SPIDER.PLOT, " are 'NA'", sep = ""))
		}
		
		if (!is.null(overallList$linerangeDes) && sum(!is.na(overallList$linerangeDes)) > 0) {
			if (overallList$debug) {ownCat(LINERANGE.PLOT)}
			#colNames$colOfMean = check(getVector(overallList$boxSpiderDes))
			colNames$colOfMean = VALUES
			colNames$colOfXaxis = overallList$xAxis
			colNames$desNames = overallList$linerangeDesName
			columns = c(columnsStandard, check(getVector(overallList$linerangeDes)))
			overallList$overallResult_linerangeDes = getResultDataFrame(LINERANGE.PLOT, overallList$linerangeDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		} else {
			ownCat(paste("All values for ", LINERANGE.PLOT , " are 'NA'", sep = ""))
		}
		
		if (!is.null(overallList$violinBoxDes) && sum(!is.na(overallList$violinBoxDes)) > 0 && overallList$isRatio) {
			if (overallList$debug) {ownCat(VIOLIN.PLOT)}
			colNames$colOfMean = "mean"
			colNames$colOfXaxis = "xAxis"
			columns = c(columnsStandard, check(getVector(overallList$violinBoxDes)))
			overallList$overallResult_violinBoxDes = getResultDataFrame(VIOLIN.PLOT, overallList$violinBoxDes, overallList$iniDataSet[columns], groupBy, colNames, booleanVectorList, overallList$debug, contactTheValues)
		}
		
		
		if (overallList$deleteNboxplot) {
			overallList$iniDataSet <- NULL
		}
		
		if (is.null(overallList$boxStackDes) && 
				is.null(overallList$nBoxMultiDes) &&
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
	} else if (diagramTyp == SPIDER.PLOT || diagramTyp == LINERANGE.PLOT) {
		return(strsplit(substring(colNameWichMustBind, nchar(VALUES)+1), "\\.")[[1]][1])
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
#	diagramTyp = NBOX.MULTI.PLOT
#	descriptorList = overallList$nBoxMultiDes
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
#splitTreatmentFirst <- overallList$splitTreatmentFirst
#splitTreatmentSecond <- overallList$splitTreatmentSecond
	#########################
	
	debug %debug% "getResultDataFrame()"
	
	#contactTheValues <- FALSE
	descriptor = getVector(descriptorList)
	
	if (diagramTyp == SPIDER.PLOT || diagramTyp == LINERANGE.PLOT) {
		descriptorName <- character()
		for(n in 1:length(descriptorList)) {
			lengthVector <- length(descriptorList[[n]][, 1])
			descriptorName <- c(descriptorName, paste(rep.int(n, lengthVector), c(1:lengthVector), sep = "."))		
		}
	} else {	
		descriptorName <- seq(1:length(descriptor))
	}
	
	descriptorName <- descriptorName[!is.na(descriptor)]
	descriptor <- descriptor[!is.na(descriptor)]
	
	
	if (diagramTyp != BOX.PLOT) {
		groupedDataFrame = data.table(iniDataSet)
		#key(groupedDataFrame) = c(groupBy, colNames$xAxis)
		setkeyv(groupedDataFrame, c(groupBy, colNames$xAxis))
	}
	
	if (diagramTyp == BOX.PLOT) {
		if (length(groupBy)>0) {
			groupedDataFrameMean <- iniDataSet[groupBy[1]]
			groupByReduce <- groupBy[groupBy != groupBy[1]]
			
			for (n in c(groupByReduce, colNames$xAxis, descriptor)) {
				groupedDataFrameMean <- cbind(groupedDataFrameMean, iniDataSet[n])
			}		
		} else {
			groupedDataFrameMean <- iniDataSet
		}
	} else {
		groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(.SD, mean, na.rm = TRUE), by = c(groupBy, colNames$xAxis)])
		#groupedDataFrameMean = as.data.frame(groupedDataFrame[, lapply(colnames(groupedDataFrame), mean, na.rm = TRUE), by = c(groupBy, colNames$xAxis)])
	}
	
	if (diagramTyp == NBOX.PLOT || diagramTyp == BOX.PLOT || diagramTyp == SPIDER.PLOT || diagramTyp == VIOLIN.PLOT || diagramTyp == LINERANGE.PLOT) {
		#colNamesOfTheRest = paste(colNames$colOfMean, seq(1:length(descriptor)), sep = "")	
		colNamesOfTheRest <- paste(colNames$colOfMean, descriptorName, sep = "")	
	} else {
		colNamesOfTheRest <- groupedDataFrameMean %allColnamesWithoutThisOnes% c(groupBy, colNames$xAxis)
	}
	
	colnames(groupedDataFrameMean) <- c(groupBy, colNames$colOfXaxis, colNamesOfTheRest)
	
	if (diagramTyp == NBOX.PLOT || diagramTyp == NBOX.MULTI.PLOT) {
		groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(.SD, sd, na.rm = TRUE), by = c(groupBy, colNames$xAxis)])
		#groupedDataFrameSD = as.data.frame(groupedDataFrame[, lapply(colnames(groupedDataFrame), sd, na.rm = TRUE), by = c(groupBy, colNames$xAxis)])
		if (diagramTyp == NBOX.PLOT) {
			colnames(groupedDataFrameSD) <- c(groupBy, colNames$colOfXaxis, paste(colNames$colOfSD, descriptorName, sep = ""))
		} else {
			colnames(groupedDataFrameSD) <- c(groupBy, colNames$colOfXaxis, paste(colNames$colOfSD, colNamesOfTheRest, sep = ""))
		}
	}
	
	booleanVector = getBooleanVectorForFilterValues(groupedDataFrameMean, booleanVectorList)
	
	if (diagramTyp == NBOX.PLOT || diagramTyp == NBOX.MULTI.PLOT) {
		iniDataSet <- merge(sort = FALSE, groupedDataFrameMean[booleanVector, ], groupedDataFrameSD[booleanVector, ], by = c(groupBy, colNames$colOfXaxis))
		overallResult <- buildRowName(iniDataSet, groupBy, contactTheValues)
		
		if (diagramTyp == NBOX.MULTI.PLOT) {
			addValue <- 0
			newDataSet <- data.frame()
			firstnewDataSet <- TRUE
			firstAddColumn <- TRUE
			for(nn in seq(along = descriptorList)) {
				if (!is.na(descriptorList[[nn]][1])) {
					tempDesMorThanOne <- character()
					actualDataSet <- overallResult[, c(getPrimAndOrName(colnames(overallResult)), colNames$colOfXaxis, getVector(descriptorList[nn]), paste(colNames$colOfSD, getVector(descriptorList[nn]), sep = ""))]
					newDataSetKK <- data.frame()
					firstNewDataSetTemp <- TRUE
					for(kk in seq(along = descriptorList[[nn]])) {							
						if (!is.na(descriptorList[[nn]][kk][1])) {
							usedColumn <- c(as.character(descriptorList[[nn]][kk]), paste(colNames$colOfSD, as.character(descriptorList[[nn]][kk]), sep = ""))
							if (firstAddColumn) {
								additionalColumns <- c(getPrimAndOrName(colnames(overallResult)), colNames$colOfXaxis)
							} else {
								additionalColumns <- NULL
							}
							usedColumn <- c(additionalColumns, usedColumn)
							
							newDataSetTemp <- actualDataSet[, usedColumn]
							
							if (str_detect(descriptorList[[nn]][kk], fixed("section_"))) {
								newName <- str_split(descriptorList[[nn]][kk], fixed("section_"))[[1]][2]
								newName <- str_split(newName, fixed("_"))[[1]][1]
								newName <- paste("Section", newName, sep = " ")
							} else {
								newName <- descriptorList[[nn]][kk]
							}
							newDataSetTemp <- cbind(newDataSetTemp, column = rep.int(newName, nrow(actualDataSet))) # as.character(descriptorList[[nn]][kk])
							colnames(newDataSetTemp) <- c(additionalColumns, paste(MEAN, nn, sep = ""), paste(SE, nn, sep = ""), paste(COLUMN, nn, sep = ""))
							
							if (firstNewDataSetTemp) { #kk == 1
								newDataSetKK <- newDataSetTemp
								firstNewDataSetTemp <- FALSE
							} else {	
								newDataSetKK <- rbind(newDataSetKK, newDataSetTemp)
							}
						}
					}
					
					if (length(newDataSetKK) != 0 ) {
						firstAddColumn <- FALSE
					}				
					
					if (firstnewDataSet) { # nn == 1
						newDataSet <- newDataSetKK
						firstnewDataSet <- FALSE
					} else {
						print(head(newDataSet))
						print(head(newDataSetKK))
						newDataSet <- cbind(newDataSet, newDataSetKK)
					}
				}
			}
			overallResult <- newDataSet
		}
	} else if (diagramTyp == BOX.PLOT || diagramTyp == VIOLIN.PLOT) {
		#|| diagramTyp == "spiderplot"
		iniDataSet <- groupedDataFrameMean[booleanVector, ]
		overallResult = buildRowName(iniDataSet, groupBy, contactTheValues)
	} else if (diagramTyp == SPIDER.PLOT || diagramTyp == LINERANGE.PLOT) {
		iniDataSet <- groupedDataFrameMean[booleanVector, ]
		buildRowNameDataSet = buildRowName(iniDataSet, groupBy, contactTheValues)
		temp = data.frame()
		
		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, PRIMAER.TREATMENT)) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList, diagramTyp)
			
			colNameWichMustBindReNamed <- reNameSpin(colNameWichMustBind, colNames)
			
			if (is.null(buildRowNameDataSet$primaerTreatment)) {	
				temp = rbind(temp, data.frame(hist = rep.int(x = colNameWichMustBindReNamed, times = length(buildRowNameDataSet[, colNameWichMustBind])), values = buildRowNameDataSet[, colNameWichMustBind], xAxis = buildRowNameDataSet[, colNames$colOfXaxis], name = buildRowNameDataSet[, colNames$colName], plot = plot))			
			} else {
				temp = rbind(temp, data.frame(hist = rep.int(x = colNameWichMustBindReNamed, times = length(buildRowNameDataSet[, colNameWichMustBind])), primaerTreatment = buildRowNameDataSet[, PRIMAER.TREATMENT], values = buildRowNameDataSet[, colNameWichMustBind], xAxis = buildRowNameDataSet[, colNames$colOfXaxis], name = buildRowNameDataSet[, colNames$colName], plot = plot))			
			}
		}
		overallResult = temp
		
	} else {
		iniDataSet <- groupedDataFrameMean[booleanVector, ]	
		buildRowNameDataSet <- buildRowName(iniDataSet, groupBy, contactTheValues)
		temp = data.frame()
		
		for (colNameWichMustBind in buildRowNameDataSet %allColnamesWithoutThisOnes% c(colNames$xAxis, colNames$colName, PRIMAER.TREATMENT)) {
			plot = getPlotNumber(colNameWichMustBind, descriptorList, diagramTyp)
			
			if (plot != -1) {
				colNameWichMustBindReNamed = reNameHist(colNameWichMustBind)
				
				if (is.null(buildRowNameDataSet$primaerTreatment)) {	
					temp = rbind(temp, data.frame(hist = rep.int(x = colNameWichMustBindReNamed, times = length(buildRowNameDataSet[, colNameWichMustBind])), values = buildRowNameDataSet[, colNameWichMustBind], xAxis = buildRowNameDataSet[, colNames$colOfXaxis], name = buildRowNameDataSet[, colNames$colName], plot = plot))			
				} else {
					temp = rbind(temp, data.frame(hist = rep.int(x = colNameWichMustBindReNamed, times = length(buildRowNameDataSet[, colNameWichMustBind])), primaerTreatment = buildRowNameDataSet[, PRIMAER.TREATMENT], values = buildRowNameDataSet[, colNameWichMustBind], xAxis = buildRowNameDataSet[, colNames$colOfXaxis], name = buildRowNameDataSet[, colNames$colName], plot = plot))			
				}
			}
		}
		overallResult = temp
#		print(overallResult)
		print(head(overallResult))
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
	interval = seq(0.05, 0.95, by = 0.1)
	intervalSat = rep.int(c(0.8, 1.0), 5)
	intervalFluo = seq(0, 0.166666666666, by = 0.0185185185)
	
	interval20 = seq(0.025, 0.975, by = 0.05)
	intervalSat20 = 1 #rep.int(c(0.8, 1.0), 10)
	intervalFluo20 = seq(0, 0.166666666666, by = 0.008771929789)
	
	if (length(grep("fluo", getVector(descriptorList), ignore.case = TRUE)) > 0) { #rot			
#		colorList = as.list(hsv(h = c(rev(intervalFluo), rev(intervalFluo20)), s = c(intervalSat, intervalSat20), v = 1))
		colorList = as.list(hsv(h = c(intervalFluo, intervalFluo20), s = c(intervalSat, intervalSat20), v = 1))
	} else if (length(grep("phenol", getVector(descriptorList), ignore.case = TRUE)) > 0) { #gelb
		colorList = as.list(hsv(h = c(intervalFluo, intervalFluo20), s = c(intervalSat, intervalSat20), v = 1))
	} else if (length(grep("vis.hsv.h", getVector(descriptorList), ignore.case = TRUE)) > 0) {
		colorList = as.list(hsv(h = c(interval, interval20), s = 1, v = c(intervalSat, intervalSat20)))
	} else if (length(grep("vis.hsv.s", getVector(descriptorList), ignore.case = TRUE)) > 0) {
		colorList = colorRampPalette(colorVector)(30)
	} else if ((length(grep("vis.hsv.v", getVector(descriptorList), ignore.case = TRUE)) > 0)) {
		colorList = as.list(rgb(c(interval, interval20), c(interval, interval20), c(interval, interval20), max = 1))
	} else if ((length(grep("nir", getVector(descriptorList), ignore.case = TRUE)) > 0)) {
		colorList = as.list(rgb(c(rev(interval), rev(interval20)), c(rev(interval), rev(interval20)), c(rev(interval), rev(interval20)), max = 1))
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

getColorVector <- function(isGray) {
	if (!as.logical(isGray)) {
		#colorVector = c(brewer.pal(8, "Set1"))
		return(c(brewer.pal(7, "Dark2")))
		#colorVector = c(brewer.pal(11, "Spectral")) # sometimes very pale colors
	} else {
		return(c(brewer.pal(9, "Greys")))
	}
}

getColorRampPalette <- function(colorVector, overallResult = NULL, whichColumShouldUse = NULL, typOfPlot = NULL) {
	if (!is.null(typOfPlot) && !is.null(whichColumShouldUse)) {
		return(colorRampPalette(colorVector)(length(unique(overallResult[[whichColumShouldUse]]))))
	} else if (is.null(whichColumShouldUse)) {
		return(colorRampPalette(colorVector)(length(overallResult)))
	} else {
		return(colorRampPalette(colorVector)(whichColumShouldUse))
	}
}


setColorList <- function(diagramTyp, descriptorList, overallResult, isGray, first, second) {
	######################
#diagramTyp <- "spiderplot"
#descriptorList <- overallList$boxSpiderDes
#overallResult <- overallList$overallResult_boxSpiderDes
#isGray <- overallList$isGray
#first <- overallList$splitTreatmentFirst
#second <- overallList$splitTreatmentSecond
	######################
	
	
#	if (!first && second) {
#		prePlot <- TRUE
#	} else {
#		prePlot <- FALSE
#	}
	
	whichColumShouldUse <- getColumnForColor(first, second, colnames(overallResult))
	#whichColumShouldUse <- checkWhichColumShouldUseForPlot(first, second, colnames(overallResult), diagramTyp, FALSE)	
	colorVector <- getColorVector(isGray)
	
	colorList = list()
	if (diagramTyp == NBOX.PLOT || diagramTyp == BOX.PLOT || diagramTyp == VIOLIN.PLOT || diagramTyp == SPIDER.PLOT || diagramTyp == LINERANGE.PLOT) {
		if (!is.null(descriptorList)) {
			for (n in names(descriptorList)) {
				#if (!is.na(descriptorList[[n]])) {
				if (sum(!is.na(descriptorList[[n]])) > 0) {
					
					colorList[[n]] = getColorRampPalette(colorVector, overallResult, whichColumShouldUse, diagramTyp)
					#print(colorList[[n]])
					#				if (isOtherTyp) {
					#					colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
					#				} else {
					#					if ("primaerTreatment" %in% colnames(overallResult)) {
					#						colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$primaerTreatment)))
					#					} else {
					#						colorList[[n]] = colorRampPalette(colorVector)(length(unique(overallResult$name)))
					#					}
					#				}
				} else {
					#ownCat("All values are 'NA'")
				}
			}
		}
	} else {
		if (!is.null(descriptorList)) {
			for (n in names(descriptorList)) {
				if (sum(!is.na(descriptorList[[n]])) > 0) {
					colorList[[n]] = setColorListHist(descriptorList[n], colorVector)
				} else {
					#ownCat("All values are 'NA'")
				}
			}
		}
	}
	return(colorList)
}

setColorForAll <- function(overallList, first, second) {
	#########
#first <- overallList$splitTreatmentFirst 
#second <- overallList$splitTreatmentSecond
	########
	
	overallList$debug %debug% "setColorForAll()" 
	
	des <- ""
	if ((first && second)) {
		des <- overallList$secondFilterTreatmentRename
	} else if ((!first && second)) {
		des <- overallList$filterTreatmentRename
	} else if ((first && !second)) {
		if (overallList$secondTreatment == NONE) {
			des <- overallList$filterTreatmentRename
		} else {
			des <- overallList$secondFilterTreatmentRename
		}
	} else {
		if (length(overallList$filterTreatmentRename) == 0 && length(overallList$secondFilterTreatmentRename) == 0) {
			des <- VALUES
		} else if (length(overallList$secondFilterTreatmentRename) == 0 || length(overallList$filterSecondTreatment) < 2) {
			des <- overallList$filterTreatmentRename
		} else if (length(overallList$filterTreatmentRename) == 0 || length(overallList$filterTreatment) < 2) {
			des <- overallList$secondFilterTreatmentRename
		} else {
			des <- overallList$filterTreatmentRename %contactAllWithAll% overallList$secondFilterTreatmentRename
		}
	}
	
	colorVector <- getColorVector(overallList$isGray)
	color <- getColorRampPalette(colorVector, des)
	color <- as.list(color)
	names(color) <- des
	return(color)
}



setColor <- function(overallList) {
	overallList$debug %debug% "setColor()" 
	
	#isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
	
#	overallList$color_nBox = setColorList(NBOX.PLOT, overallList$nBoxDes, overallList$overallResult_nBoxDes, overallList$isGray, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
#	overallList$color_box = setColorList(BOX.PLOT, overallList$boxDes, overallList$overallResult_boxDes, overallList$isGray, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
#	overallList$color_spider = setColorList(SPIDER.PLOT, overallList$boxSpiderDes, overallList$overallResult_boxSpiderDes, overallList$isGray, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
#	overallList$color_linerange = setColorList(LINERANGE.PLOT, overallList$linerangeDes, overallList$overallResult_linerangeDes, overallList$isGray, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
	
	overallList$color <- setColorForAll(overallList, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
	overallList$color_boxStack <- setColorList(STACKBOX.PLOT, overallList$boxStackDes, overallList$overallResult_boxStackDes, overallList$isGray, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
	
	#overallList$color_violin = setColorList("violinplot", overallList$violinBoxDes, overallList$overallResult_violinBoxDes, overallList$isGray, isOtherTyp)
	return(overallList)
}

normalizeToHundredPercent = function(whichRows, overallResult) {
	return(t(apply(overallResult[whichRows, ], 1, function(x, y) {(100*x)/y}, y = colSums(overallResult[whichRows, ]))))
}

renameYForSubsection <- function(label) {
	
	label <- gsub("\\\\% ", "percent", label)
	label <- gsub("\\^2", "$^2$", label)
	label <- paste(toupper(substr(label, 0, 1)), substr(label, 2, nchar(label)), sep = "")
	
	return(label)
}

trimSectionSeparator <- function(string, sepE = SECTION.SEPARATOR) {
	if (nchar(string) != 0) {
		if (substr(string, 1, 1) == sepE) {
			string <- substring(string, 2)
		}
		
		if (substring(string, nchar(string)) == sepE) {
			string <- substr(string, 1, nchar(string)-1)
		}
		
		return(string)
	}
	return("")
}

buildSectionString <- function(sectionList, imagesIndex, appendix) {
	#sectionList <- overallList$nBoxSection
	if (appendix) {
		return(APPENDIX.SECTION)
	} else {
		sectionVector <- vector()
		for(nn in sectionList) {
			#print(nn)
			#print(imagesIndex)
			sectionVector <- paste(sectionVector, nn[as.numeric(imagesIndex)], sep = SECTION.SEPARATOR)
		}
		return(trimSectionSeparator(sectionVector))
	}
}

firstLetterBig <- function(string) {
	return(paste(toupper(substring(string, 1, 1)), substring(string, 2), sep = ""))
}


writeLatexFile <- function(preFileNameLatexFile, fileNameImageFile = "", ylabel = "", subsectionDepth = 1, saveFormatImage = "pdf", section = 99) { #insertSubsections = FALSE, 
	#o = ""
	#fileNameImageFile = preprocessingOfValues(fileNameImageFile, FALSE, "_")
	
	fileNameLatexFile = paste(preprocessingOfValues(preFileNameLatexFile, FALSE, "_"), SECTION.TEX, section, sep = "")
	
	#o = gsub('[[:punct:]]', "_", o)
	#print(fileNameImageFile)
	latexText <- ""
	if (nchar(ylabel) > 0) {
		ylabel <- renameYForSubsection(ylabel)
		if (subsectionDepth == 1) {
			latexText = paste(latexText, "\\subsection{", ylabel, "}\n", sep = "" )
		} else if (subsectionDepth == 2) {
			latexText = paste(latexText, "\\subsubsection{", ylabel, "}\n", sep = "" )
		} else if (subsectionDepth == 3) {
			latexText = paste(latexText, "\\paragraph{", ylabel, "}~", sep = "" )
		} else if (subsectionDepth == 4) {
			latexText = paste(latexText, "\\subparagraph{", ylabel, "}~", sep = "" )
		}
	}
	
#	if (insertSubsections & nchar(ylabel) > 0) {
#		ylabel <- renameYForAppendix(ylabel)
#		latexText = paste(latexText, "\\subsection{", ylabel, "}\n", sep = "" )
#	}
	
	if (fileNameImageFile == "") {
		latexText <- paste(latexText, "\\loadTex{", DIRECTORY.PLOTS, DIRECTORY.SEPARATOR, preFileNameLatexFile, sep = "")
		latexText = paste(latexText, ".", TEX, "}", sep = "")
	} else {
		latexText <- paste(latexText, "\\loadImage{", DIRECTORY.PLOTS, DIRECTORY.SEPARATOR, fileNameImageFile, sep = "")
		latexText = paste(latexText, ".", saveFormatImage, "}", sep = "")
	}
	
	
	
	fileNameLatexFile <- paste(DIRECTORY.PLOTSTEX, fileNameLatexFile, sep = DIRECTORY.SEPARATOR)
	if (!(section == ERROR && file.exists(paste(fileNameLatexFile, TEX, sep = ".")))) {
		write(x = latexText, append = TRUE, file = paste(fileNameLatexFile, TEX, sep = "."))
	}
}

writeLatexTable <- function(fileNameLatexFile, columnName = NULL, value = NULL, columnWidth = NULL) {
	latexText <- ""
	
	if (length(columnName) > 0) {
		#latexText <- "\\begin{tabular}{|"
		latexText <- "\\begin{longtable}{|"
		
		for(n in 1:length(columnName)) {
			latexText <- paste(latexText, "p{", columnWidth[n], "}|", sep = "")
		}
		latexText <- paste(latexText, "}", sep = "")
		
		#This is the header for the first page of the table... --> endfirsthead
		#This is the header for the remaining page(s) of the table... --> endhead
		for(n in 1:2) {
			
			latexText <- paste(latexText, " \\hline ", sep = "")
			for(n in 1:length(columnName)) {
				latexText <- paste(latexText, "{\\textbf{", 
						parseString2Latex(renameFilterOutput(as.character(columnName[n]))), 
						"}}", sep = "")
				if (n != length(columnName)) {
					latexText <- paste(latexText, "& ", sep = " ")
				}
			}
			latexText <- paste(latexText, 
					"\\tabularnewline", 
					"\\hline", 
					"\\hline", sep = " ")
			if (n == 1) {
				latexText <- paste(latexText, "\\endfirsthead", sep = " ")
			} else {
				latexText <- paste(latexText, "\\endhead", sep = " ")
			}
		}
		
		#This is the footer for all pages except the last page of the table...		
		latexText <- paste(latexText, "\\multicolumn{", length(columnName), "}{l}{{Continued on Next Page\\ldots}} ", 
				"\\tabularnewline ", 
				"\\endfoot ", sep = "")
		
		#This is the footer for the last page of the table...
		latexText <- paste(latexText, "\\hline \\hline \\endlastfoot", sep = " ")
		
	} else if (length(value) > 0) {
		if (!is.null(value)) {
			for(n in 1:length(value)) {
				latexText <- paste(latexText, parseString2Latex(renameFilterOutput(as.character(value[n]))))
				if (n != length(value)) {
					latexText <- paste(latexText, " &", sep = "")
				}
			}
			latexText <- paste(latexText, "\\tabularnewline \\hline")
		}
	} else {
		latexText <- paste(latexText, 
#						"\\hline", 
#						"\\hline", 
#						"\\end{tabular}", sep = " ")
				"\\end{longtable}", sep = " ")
	}
	
	if (latexText != "") {
		write(x = latexText, append = TRUE, file = paste(fileNameLatexFile, TEX, sep = "."))
	}	
}


getSectionAsMatrix <- function(sectionOverall) {
	return(str_split_fixed(sectionOverall, fixed(SECTION.SEPARATOR), str_count(sectionOverall[1], fixed(SECTION.SEPARATOR))+2))
}


getSectionMatrix <- function(sectionOverall, fileNames, debug = debug) {
	debug %debug% "getSectionMatrix()"
	sectionMatrix <- getSectionAsMatrix(sectionOverall)
	sectionMatrix[, str_count(sectionOverall[1], fixed(SECTION.SEPARATOR))+2] <- fileNames
	
	return(sectionMatrix)
}

getConstance <- function(sectionTyp, typ) {
	
	if (sectionTyp == FIRST.SECTION || sectionTyp == 1) {
		if (typ == OVERALL.FILENAME.TEX || typ == OVERALL.FILENAME.PATTERN) {
			return(OVERALL.FIRST.SECTION.TEX)
		} else if (typ == REDUCE.SECTION) {
			return(NONE)
		} else if (typ == GET.INT.AS.SECTION) {
			return(FIRST.SECTION)
		} else if (typ == GET.NUMBER.OF.SECTION) {
			return(1)
		} else if (typ == FILL.WITH.NULL.SECTION) {
			return(3)
		}
	} else if (sectionTyp == SECOND.SECTION || sectionTyp == 2) {
		if (typ == OVERALL.FILENAME.TEX || typ == OVERALL.FILENAME.PATTERN) {
			return(OVERALL.SECOND.SECTION.TEX)
		} else if (typ == REDUCE.SECTION) {
			return(FIRST.SECTION)
		} else if (typ == GET.INT.AS.SECTION) {
			return(SECOND.SECTION)
		} else if (typ == GET.NUMBER.OF.SECTION) {
			return(2)
		} else if (typ == FILL.WITH.NULL.SECTION) {
			return(2)
		}
	} else if (sectionTyp == THIRD.SECTION || sectionTyp == 3) {
		if (typ == OVERALL.FILENAME.TEX || typ == OVERALL.FILENAME.PATTERN) {
			return(OVERALL.THIRD.SECTION.TEX)
		} else if (typ == REDUCE.SECTION) {
			return(SECOND.SECTION)
		} else if (typ == GET.INT.AS.SECTION) {
			return(THIRD.SECTION)
		} else if (typ == GET.NUMBER.OF.SECTION) {
			return(3)
		} else if (typ == FILL.WITH.NULL.SECTION) {
			return(1)
		}
	} else if (sectionTyp == FOURTH.SECTION || sectionTyp == 4) {
		if (typ == OVERALL.FILENAME.TEX || typ == OVERALL.FILENAME.PATTERN) {
			return(OVERALL.FOURTH.SECTION.TEX)
		} else if (typ == REDUCE.SECTION) {
			return(THIRD.SECTION)
		} else if (typ == GET.INT.AS.SECTION) {
			return(FOURTH.SECTION)
		} else if (typ == GET.NUMBER.OF.SECTION) {
			return(4)
		} else if (typ == FILL.WITH.NULL.SECTION) {
			return(0)
		}
	}
	return(NONE)
}

writeOnlyALatexFile <- function(latexText, fileName, append = FALSE) {
	#fileName <- paste("tex", fileName, sep = "/")
	write(x = latexText, append = append, file = paste(fileName, TEX, sep = "."))
}


writeLatexSectionFile <- function(latexText, sectionTyp, sectionID, debug = FALSE) {
	debug %debug% "writeLatexSectionFile()"
	
	const <- getConstance(sectionTyp, OVERALL.FILENAME.TEX)
	newSection <- checkValue(sectionMappingList, sectionTyp, sectionID, NEW.SECTION, NONE, TRUE)
	newSection <- paste(NEW.SECTION.TEX, newSection, sep = "")
	
	fileName <- paste(const, sectionID, newSection, sep = "")
	fileName <- paste(DIRECTORY.SECTION, fileName, sep = DIRECTORY.SEPARATOR)
	write(x = latexText, append = TRUE, file = paste(fileName, TEX, sep = "."))
}


setReset <- function(latexText, tabText = "") {
	return(paste(latexText, tabText, "\\resetBoolean", NEWLINE.TEX, sep = ""))
}


getBooleanVectorForWholeSection <- function(sectionMatrix, sectionID, identicalMatrix, debug = debug) {
	#########
#sectionID <- subsubsectionID
#identicalMatrix <- identicalMatrixSubSubSection
	#########
	debug %debug% "getBooleanVectorForWholeSection()"
	
	#splitId <- unlist(str_split(sectionIDVector[[1]], POINT.PATTERN))
	#booleanVectorForWholeSectionIni <- rep.int(FALSE, length(sectionMatrix[, 1]))
	addValues <- c(sectionID, rownames(identicalMatrix)[as.vector(identicalMatrix[, sectionID])])	
	addValues <- str_split(addValues, POINT.PATTERN)
	
	booleanVec <- data.frame(rep.int(FALSE, length(sectionMatrix[, 1])))
	
	for(kk in seq(along = addValues)) {
		booleanVectorForWholeSection <- rep.int(FALSE, length(sectionMatrix[, 1]))
		for(hh in seq(along = addValues[[kk]])) {
			if (hh == 1) {
				booleanVectorForWholeSection <- booleanVectorForWholeSection | (sectionMatrix[, hh] %in% addValues[[kk]][hh])
			} else {
				booleanVectorForWholeSection <- booleanVectorForWholeSection & (sectionMatrix[, hh] %in% addValues[[kk]][hh])
			}	
		}
#		if (kk == 1) {	
#			booleanVec <- booleanVectorForWholeSection
#		} else {
		booleanVec <- cbind(booleanVec, booleanVectorForWholeSection)
#		}
	}
	
	return(rowSums(booleanVec) > 0)
	#return(rowSums(booleanVec) == length(booleanVec[1, ]))
}


setCheckFiles <- function(latexText, fileVector, tabText = "") {
	
	for(file in fileVector) {
		file <- paste(DIRECTORY.PLOTSTEX, file, sep = DIRECTORY.SEPARATOR)
		latexText <- paste(latexText, tabText, "\\checkFileNoReset{", file , "}", NEWLINE.TEX, sep = "")
	}
	return(latexText)
}

setIfThenElse <- function(latexText, tabText = "") {
	return(paste(latexText, tabText, "\\ifthenelse{\\boolean{isFile1}}{", NEWLINE.TEX, sep = ""))
}



conectEachRowNotForSection <- function(matrixVector) {
	
}


conectEachRow <- function(matrixVector, sepE = SECTION.SEPARATOR) {
	#########
#matrixVector <- unique(sectionMatrix[booleanVectorForWholeSection, c(1, 2, 3, 4)])
	#########
	if (class(matrixVector) == "character") {
		return(conectSectionIDVector(matrixVector, sepE))
	} else if (class(matrixVector) == "matrix") {
		conectVector <- vector()
		
		for(nn in seq(along = matrixVector[, 1])) {
			conectVector <- c(conectVector, conectSectionIDVector(matrixVector[nn, ], sepE))
		}
		
		return(conectVector)
	} else {
		return(matrixVector)
	}
}


conectSectionIDVectorList <- function(sectionIDVector, conectionList) {
	if (nchar(sectionIDVector) > 0) {
		return(paste(conectSectionIDVector(sectionIDVector), conectionList, sep = SECTION.SEPARATOR))
	} else {
		return(conectionList)
	}
}


conectSectionIDVector <- function(sectionIDVector, sepE = SECTION.SEPARATOR) {
	if (length(sectionIDVector) > 1) {
		con <- ""
		for(nn in sectionIDVector) {
			con <- paste(con, nn, sep = sepE)
		}		
		return(trimSectionSeparator(con, sepE))
	} else {
		return(sectionIDVector)
	}
}

setTextOrTypOfClearOrReset <- function(latexText, text, tabText) {
	return(paste(latexText, tabText, text, NEWLINE.TEX, sep = ""))
}

setTitle <- function(latexText, sectionTyp, title, tabText) {
	return(paste(latexText, tabText, "\\", sectionTyp, "{", title, "}", #\\noindent
					ifelse(sectionTyp != FOURTH.SECTION, NEWLINE.TEX, NEWLINE.PARA.TEX), 
					sep = ""))
}

isThereAPreRelation <- function(sectionMappingList, oldPosition, newPosition, whichValueShouldBeChecked, defaultValue, getNewSection) {
	#######
#newPosition <- position
	#######	
	
	#print(oldPosition)
	#print(newPosition)
	
	toCheckMother <- unlist(str_split(oldPosition, POINT.PATTERN))
	toCheckSon <- unlist(str_split(newPosition, POINT.PATTERN))
	#print("check if there a pre relation")
	if (length(toCheckMother) == length(toCheckSon)) {
		for(nn in 1:(length(toCheckMother)-1)) {
			if (toCheckMother[nn] != toCheckSon[nn]) {
				if (nn == 1) {
					tempMother <- toCheckMother[1]
					tempSon <- toCheckSon[1]
				} else {
					tempMother <- conectEachRow(toCheckMother[1:nn])
					tempSon <- conectEachRow(toCheckSon[1:nn])
				}
				#print("########")
				#print(tempMother)
				#print(checkValue(sectionMappingList, getConstance(nn, GET.INT.AS.SECTION), tempMother, whichValueShouldBeChecked, defaultValue, getNewSection))
				if (tempSon %in% checkValue(sectionMappingList, getConstance(nn, GET.INT.AS.SECTION), tempMother, whichValueShouldBeChecked, defaultValue, getNewSection)) {
					#print("interner Check")
					return(TRUE)
				}
				#print("geschafft")
			}
			#print(nn)
			#print(toCheckMother[nn])
			#print(toCheckSon[nn])
		}
	} else {
		print("not the same length!")
		return(FALSE)	
	}
	return(FALSE)
}

checkValue <- function(sectionMappingList, sectionTyp, position, whichValueShouldBeChecked, defaultValue = "", getNewSection = FALSE) {
	############
#sectionMappingListSectionTyp <- sectionMappingList[[sectionTyp]]
#position <- "13.2.7"
#whichValueShouldBeChecked <- "newSection"
#defaultValue <- CLEAR.PAGE.NO
#getNewSection <- FALSE
	############
	sectionMappingListSectionTyp <- sectionMappingList[[sectionTyp]]
	
	positionVector <- vector()
	repeat {
		if (!is.null(sectionMappingListSectionTyp[[position]][[whichValueShouldBeChecked]])) {				 
			#section[[sectionVector[sectionNameIndex]]][concatSection[[sectionVector[sectionNameIndex]]] == nn] <- sectionMappingList[[sectionVector[sectionNameIndex]]][[values[nn]]]$newSection
			if (whichValueShouldBeChecked == NEW.SECTION && !getNewSection) {
				if (length(positionVector) > 0) {
					return(positionVector)
				} else {
					return(defaultValue)
				}
			}
			return(sectionMappingListSectionTyp[[position]][[whichValueShouldBeChecked]])
			break;
		} else if (!is.null(sectionMappingListSectionTyp[[position]]$takeRestValuesFrom)) {
			if (sectionMappingListSectionTyp[[position]]$takeRestValuesFrom == position) {
				break;
			}
			oldPosition <- position
			position <- sectionMappingListSectionTyp[[position]]$takeRestValuesFrom
			if (whichValueShouldBeChecked == NEW.SECTION && !getNewSection) {
				if (getConstance(sectionTyp, REDUCE.SECTION) != NONE) {
					if (isThereAPreRelation(sectionMappingList, oldPosition, position, whichValueShouldBeChecked, defaultValue, getNewSection)) {
						positionVector <- c(positionVector, position)
					}
				} else {
					positionVector <- c(positionVector, position)
				}
			}
		} else {
			break;
		}
	}
	return(defaultValue)
}


setAdditionInfos <- function(latexText, sectionTyp, sectionID, tabText = "") {
	###########
#sectionTyp <- THIRD.SECTION
	###########
	
	#sectionIDVector <- conectSectionIDVector(sectionIDVector)
	
#	if (!sectionIDVector %in% names(sectionMappingList[[sectionTyp]])) {
#		latexText <- setTextOrTypOfClearOrReset(latexText, CLEAR.PAGE.NO, tabText)
#		latexText <- setTextOrTypOfClearOrReset(latexText, RESET.PAGE.NO, tabText) 
#		latexText <- setTitle(latexText, sectionTyp, paste(sectionTyp, sectionIDVector, sep = " "), tabText)
#		latexText <- setTextOrTypOfClearOrReset(latexText, "", tabText)
#		
#	} else {
	
	latexText <- setTextOrTypOfClearOrReset(latexText, checkValue(sectionMappingList, sectionTyp, sectionID, "typOfClear", CLEAR.PAGE.NO), tabText)
	latexText <- setTextOrTypOfClearOrReset(latexText, checkValue(sectionMappingList, sectionTyp, sectionID, "typOfReset", RESET.PAGE.NO), tabText) 
	latexText <- setTitle(latexText, sectionTyp, checkValue(sectionMappingList, sectionTyp, sectionID, "title", paste(sectionTyp, sectionID, sep = " ")), tabText)
	latexText <- setTextOrTypOfClearOrReset(latexText, checkValue(sectionMappingList, sectionTyp, sectionID, "text", ""), tabText)
	
#	}
	return(latexText)
}

setClosedBraces <- function(latexText, tabText) {
	return(paste(latexText, tabText, "}{}", NEWLINE.TEX, NEWLINE.TEX, sep = ""))
}


setResetCheckAndIFPart <- function(latexText, fileVector, tabText = "") {
	latexText <- setReset(latexText, tabText)
	latexText <- setCheckFiles(latexText, fileVector, tabText)
	latexText <- setIfThenElse(latexText, tabText)
	
	return(latexText)
}

setLoadFile <- function(latexText, file, isTex = TRUE, tabText = "") {
	
	
	
	if (isTex) {
		file <- paste(DIRECTORY.PLOTSTEX, file, sep = DIRECTORY.SEPARATOR)
		latexText <- paste(latexText, tabText, "\\loadTex{", str_sub(file, 1, str_locate(file, TEX.PATTERN)[, "start"]-1), "}", NEWLINE.TEX, sep = "")
	} else {
		file <- paste(DIRECTORY.PLOTS, file, sep = DIRECTORY.SEPARATOR)
		latexText <- paste(latexText, tabText, "\\loadImage{", file, "}", NEWLINE.TEX, sep = "")
	}
	
	return(latexText)
}


setLoadImageAndLoadTex <- function(latexText, fileVector, tabText = "") {
	##########
#fileVector <- sectionMatrix[booleanVectorForWholeSection, "file"]
	##########
	
	texFiles <- as.logical(lapply(fileVector, str_count, TEX.PATTERN))
	for(signleFileIndex in seq(along = fileVector)) {
		latexText <- setResetCheckAndIFPart(latexText, fileVector[signleFileIndex], tabText)
		tabText <- increaseTabText(tabText)
		latexText <- setLoadFile(latexText, fileVector[signleFileIndex], texFiles[signleFileIndex], tabText)
		tabText <- reduceTabText(tabText)
		latexText <- setClosedBraces(latexText, tabText)
	}
	
	return(latexText)
}

increaseTabText <- function(tabText) {
	return(paste(tabText, TABULATOR.TEX, sep = ""))
}


reduceTabText <- function(tabText) {
	
	if (nchar(tabText) >= nchar(TABULATOR.TEX)) {
		return(str_sub(tabText, 1, -(nchar(TABULATOR.TEX)+1)))
	} else {
		return("")
	}
}


makeIdenticalMatrix <- function(sectionTyp, uniqueVector) {
	##########
#sectionTyp <- FIRST.SECTION
#uniqueVector <- uniqueSectionVector
#sectionTyp <- SECOND.SECTION
#uniqueVector <- uniqueSubSectionVector
#sectionTyp <- THIRD.SECTION 
#uniqueVector <- uniqueSubSubSectionVector
#sectionTyp <- FOURTH.SECTION
#uniqueVector <- uniqueParagraphVector
	##########	
	
	identicalMatrix <- matrix(FALSE, nrow = length(uniqueVector), ncol = length(uniqueVector), dimnames = list(uniqueVector, uniqueVector))
	for(kk in uniqueVector) {
#		sectionIDVector <- conectSectionIDVector(c(preSectionIDVector, kk))
		identicalVector <- checkValue(sectionMappingList, sectionTyp, kk, NEW.SECTION, NONE)
		
		if (identicalVector != NONE) {	
			newIdenticalVector <- identicalVector[!(identicalVector %in% colnames(identicalMatrix))]
			newMatrix <- matrix(FALSE, nrow = length(identicalMatrix[1, ]), ncol = length(newIdenticalVector), dimnames = list(rownames(identicalMatrix), newIdenticalVector))
			identicalMatrix <- cbind(identicalMatrix, newMatrix)
			newMatrix <- matrix(FALSE, ncol = length(identicalMatrix[1, ]), nrow = length(newIdenticalVector), dimnames = list(newIdenticalVector, colnames(identicalMatrix)))
			identicalMatrix <- rbind(identicalMatrix, newMatrix)
			
#			if (!(length(identicalVector) > 0)) {
#				identicalVector <- NONE
#			}
			
			identicalMatrix[kk, identicalVector] <- TRUE
			identicalMatrix[identicalVector, kk] <- TRUE
		}
	}
	
	checkVector <- vector()
	goThroughVector <- vector()
	allColumns <- colnames(identicalMatrix)
	
	repeat {
		if (length(allColumns) > 0 ) {	
			checkVector <- rownames(identicalMatrix)[identicalMatrix[, allColumns[1]]]
			goThroughVector <- allColumns[1]
			repeat {
				if (length(checkVector) > 0) {
					newValues <- rownames(identicalMatrix)[identicalMatrix[, checkVector[1]]]
					goThroughVector <- c(goThroughVector, checkVector[1])
					checkVector <- checkVector[checkVector != checkVector[1]]
					checkVector <- c(checkVector, newValues[!(newValues %in% goThroughVector) & !(newValues %in% checkVector)])
				} else {
					break
				}
			}
			
			for(kk in goThroughVector) {
				identicalMatrix[goThroughVector[goThroughVector != kk], kk] <- TRUE
			}
			allColumns <- allColumns[!(allColumns %in% goThroughVector)]
		} else {
			break
		}	
	}
#print(identicalMatrix)
	return(identicalMatrix)
}

checkIfNull <- function(value) {
	splitStr <- unlist(str_split(value, POINT.PATTERN))
	return(splitStr[length(splitStr)] == "0")
	
}

getUniqueSectionsValues <- function(booleanVectorForWholeSection = NULL, sectionMatrix, sectionTyp) {
	########
#sectionTyp <- FOURTH.SECTION
	########
	if (!is.null(booleanVectorForWholeSection)) {
		tempVector <- sectionMatrix[booleanVectorForWholeSection, c(1:getConstance(sectionTyp, GET.NUMBER.OF.SECTION))]
		
		if (class(tempVector) == "matrix") {
			return(conectEachRow(unique(tempVector)))
		} else {
			return(conectEachRow(tempVector))
		}
	} else {
		if (class(sectionMatrix) == "matrix") {
			return(unique(sectionMatrix[, 1]))
		} else {
			return(sectionMatrix[, 1])
		}
	}
}

buildSectionTexFile <- function(sectionMatrix, debug = debug) {
	#########
#sectionID <- unique(sectionMatrix[, 1])[1]
#subsectionID <- conectEachRow(unique(sectionMatrix[booleanVectorForWholeSection, c(1, 2)]))[1]
	#########
	debug %debug% "buildSectionTexFile()"
	#uniqueSectionVector <- unique(sectionMatrix[, 1])
	alreadyWrittenSection <- matrix(rep.int(FALSE, length(sectionMatrix[, 1])), dimnames = c(list(cbind(conectEachRow(sectionMatrix[, 1:4]))), "alreadyWritten"))
	uniqueSectionVector <- getUniqueSectionsValues(sectionMatrix = sectionMatrix, sectionTyp = FIRST.SECTION)
	identicalMatrixSection <- makeIdenticalMatrix(FIRST.SECTION, uniqueSectionVector)
	alreadyBeenProcessedSection <- vector()
	alreadyWrittenFilesSSS <- vector()
	alreadyWrittenFilesSS <- vector()
	alreadyWrittenFilesS <- vector()
	alreadyWrittenFilesP <- vector()
	for(sectionID in uniqueSectionVector) {
		debug %debug% paste("... sectionID", sectionID)
		if (!checkIfNull(sectionID) && !((sectionID %in% alreadyBeenProcessedSection) || checkAlreadyWrittenSection(alreadyWrittenSection , sectionID, FIRST.SECTION))) {
			additionalSection <- colnames(identicalMatrixSection)[getVector(identicalMatrixSection[, sectionID])]
			alreadyBeenProcessedSection <- c(alreadyBeenProcessedSection, additionalSection)
			
			#print(paste("sectionID: ", sectionID, sep = ""))
			latexTextS <- ""
			tabTextS <- ""
			#sectionIDVector <- c(sectionID, list(additionalSection))
			booleanVectorForWholeSection <- getBooleanVectorForWholeSection(sectionMatrix, sectionID, identicalMatrixSection, debug = debug)
			latexTextS <- setResetCheckAndIFPart(latexTextS, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextS)
			tabTextS <- increaseTabText(tabTextS)
			latexTextS <- setAdditionInfos(latexTextS, FIRST.SECTION, sectionID, tabTextS)
			
			if (sum(!("0" %in% unique(sectionMatrix[booleanVectorForWholeSection, 2]))) == 0) {
				latexTextS <- setLoadImageAndLoadTex(latexTextS, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextS)
			} else {
				
				#	uniqueSubSectionVector <- conectEachRow(unique(sectionMatrix[booleanVectorForWholeSection, c(1, 2)]))
				uniqueSubSectionVector <- getUniqueSectionsValues(booleanVectorForWholeSection, sectionMatrix, SECOND.SECTION)
				identicalMatrixSubSection <- makeIdenticalMatrix(SECOND.SECTION, uniqueSubSectionVector)
				alreadyBeenProcessedSubSection <- vector()
				
				for(subsectionID in uniqueSubSectionVector) {
					debug %debug% paste("... subsectionID", subsectionID)
					if (!checkIfNull(subsectionID) && !((subsectionID %in% alreadyBeenProcessedSubSection) || checkAlreadyWrittenSection(alreadyWrittenSection , subsectionID, SECOND.SECTION))) {
						
						additionalSubSection <- colnames(identicalMatrixSubSection)[getVector(identicalMatrixSubSection[, subsectionID])]
						alreadyBeenProcessedSubSection <- c(alreadyBeenProcessedSubSection, additionalSubSection)
						
						latexTextSS <- ""
						tabTextSS <- ""
						
						#print(paste("subsectionID: ", subsectionID, sep = ""))
						#subsectionIDShort <- str_sub(subsectionID, 1, str_locate(subsectionID, "\\.")[, "start"]-1)
						#sectionIDVector <- c(subsectionID, list(additionalSection, additionalSubSection))
						#() hier was ändern
						booleanVectorForWholeSection <- getBooleanVectorForWholeSection(sectionMatrix, subsectionID, identicalMatrixSubSection, debug = debug)
						
						latexTextSS <- setResetCheckAndIFPart(latexTextSS, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextSS)
						tabTextSS <- increaseTabText(tabTextSS)
						latexTextSS <- setAdditionInfos(latexTextSS, SECOND.SECTION, subsectionID, tabTextSS)
						
						if (sum(!("0" %in% unique(sectionMatrix[booleanVectorForWholeSection, 3]))) == 0) {
							latexTextSS <- setLoadImageAndLoadTex(latexTextSS, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextSS)
						} else {
							
							#uniqueSubSubSectionVector <- conectEachRow(unique(sectionMatrix[booleanVectorForWholeSection, c(1, 2, 3)]))
							uniqueSubSubSectionVector <- getUniqueSectionsValues(booleanVectorForWholeSection, sectionMatrix, THIRD.SECTION)
							identicalMatrixSubSubSection <- makeIdenticalMatrix(THIRD.SECTION, uniqueSubSubSectionVector)
							alreadyBeenProcessedSubSubSection <- vector()
							
							for(subsubsectionID in uniqueSubSubSectionVector) {
								debug %debug% paste("... subsubsectionID", subsubsectionID)
								if (!checkIfNull(subsubsectionID) && !((subsubsectionID %in% alreadyBeenProcessedSubSubSection) || checkAlreadyWrittenSection(alreadyWrittenSection , subsubsectionID, THIRD.SECTION))) {
									
									additionalSubSubSection <- colnames(identicalMatrixSubSubSection)[getVector(identicalMatrixSubSubSection[, subsubsectionID])]
									alreadyBeenProcessedSubSubSection <- c(alreadyBeenProcessedSubSubSection, additionalSubSubSection)
									latexTextSSS <- ""
									tabTextSSS <- ""
									#sectionIDVector <- c(sectionID, subsectionIDShort, subsubsectionID)
									
									#sectionIDVector <- c(subsubsectionID, list(additionalSection, additionalSubSection, additionalSubSubSection))
									booleanVectorForWholeSection <- getBooleanVectorForWholeSection(sectionMatrix, subsubsectionID, identicalMatrixSubSubSection, debug = debug)
									latexTextSSS <- setResetCheckAndIFPart(latexTextSSS, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextSSS)
									tabTextSSS <- increaseTabText(tabTextSSS)
									latexTextSSS <- setAdditionInfos(latexTextSSS, THIRD.SECTION, subsubsectionID, tabTextSSS)
									
									if (sum(!("0" %in% unique(sectionMatrix[booleanVectorForWholeSection, 4]))) == 0) {
										latexTextSSS <- setLoadImageAndLoadTex(latexTextSSS, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextSSS)
									} else {
										
										#uniqueParagraphVector <- conectEachRow(unique(sectionMatrix[booleanVectorForWholeSection, c(1, 2, 3, 4)]))
										uniqueParagraphVector <- getUniqueSectionsValues(booleanVectorForWholeSection, sectionMatrix, FOURTH.SECTION)
										identicalMatrixParagraph <- makeIdenticalMatrix(FOURTH.SECTION, uniqueParagraphVector)
										alreadyBeenProcessedParagraph <- vector()
										
										for(paragraphID in uniqueParagraphVector) {
											debug %debug% paste("... paragraphID", paragraphID)
											if (!checkIfNull(paragraphID) && !((paragraphID %in% alreadyBeenProcessedParagraph) || checkAlreadyWrittenSection(alreadyWrittenSection , paragraphID, FOURTH.SECTION))) {
												
												additionalParagraph <- colnames(identicalMatrixParagraph)[getVector(identicalMatrixParagraph[, paragraphID])]
												alreadyBeenProcessedParagraph <- c(alreadyBeenProcessedParagraph, additionalParagraph)
												latexTextP <- ""
												tabTextP <- ""
												
												#sectionIDVector <- c(sectionID, subsectionIDShort, subsubsectionID, paragraphID)
												#sectionIDVector <- c(paragraphID, list(additionalSection, additionalSubSection, additionalSubSubSection, additionalParagraph))
												booleanVectorForWholeSection <- getBooleanVectorForWholeSection(sectionMatrix, paragraphID, identicalMatrixParagraph, debug = debug)
												latexTextP <- setResetCheckAndIFPart(latexTextP, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextP)
												tabTextP <- increaseTabText(tabTextP)
												latexTextP <- setAdditionInfos(latexTextP, FOURTH.SECTION, paragraphID, tabTextP)
												latexTextP <- setLoadImageAndLoadTex(latexTextP, sectionMatrix[booleanVectorForWholeSection, "file"], tabTextP)
												tabTextP <- reduceTabText(tabTextP)
												latexTextP <- setClosedBraces(latexTextP, tabTextP)
												#if (paragraphID == "7.1.1.1") {
												#print("##################################### here we are ###############################")
												#print(alreadyWrittenSection[paragraphID, 1])
												#}
												writeLatexSectionFile(latexTextP, FOURTH.SECTION, paragraphID, debug = debug)
												alreadyWrittenSection <- setAlreadyWrittenSectionMatrix(alreadyWrittenSection, paragraphID, FOURTH.SECTION)
											}
										}
									}
									tempList <- writeInclude(latexTextSSS, tabTextSSS, FOURTH.SECTION, alreadyWrittenFilesP, debug = debug)
									latexTextSSS <- tempList$latexText
									alreadyWrittenFilesP <- tempList$alreadyWrittenFiles
									
									tabTextSSS <- reduceTabText(tabTextSSS)
									latexTextSSS <- setClosedBraces(latexTextSSS, tabTextSSS)
									
									writeLatexSectionFile(latexTextSSS, THIRD.SECTION, subsubsectionID, debug = debug)
									alreadyWrittenSection <- setAlreadyWrittenSectionMatrix(alreadyWrittenSection, subsubsectionID, THIRD.SECTION)
								}
							}
						}
						tempList <- writeInclude(latexTextSS, tabTextSS, THIRD.SECTION, alreadyWrittenFilesSSS, debug = debug)
						latexTextSS <- tempList$latexText
						alreadyWrittenFilesSSS <- tempList$alreadyWrittenFiles
						
						tabTextSS <- reduceTabText(tabTextSS)
						latexTextSS <- setClosedBraces(latexTextSS, tabTextSS)
						
						writeLatexSectionFile(latexTextSS, SECOND.SECTION, subsectionID, debug = debug)
						alreadyWrittenSection <- setAlreadyWrittenSectionMatrix(alreadyWrittenSection, subsectionID, SECOND.SECTION)
						
					}
				}
			}
			tempList <- writeInclude(latexTextS, tabTextS, SECOND.SECTION, alreadyWrittenFilesSS, debug = debug)
			latexTextS <- tempList$latexText
			alreadyWrittenFilesSS <- tempList$alreadyWrittenFiles
			
			tabTextS <- reduceTabText(tabTextS)
			latexTextS <- setClosedBraces(latexTextS, tabTextS)
			
			writeLatexSectionFile(latexTextS, FIRST.SECTION, sectionID, debug = debug)
			alreadyWrittenSection <- setAlreadyWrittenSectionMatrix(alreadyWrittenSection, sectionID, FIRST.SECTION)
		}
	}
}

checkAlreadyWrittenSection <- function(alreadyWrittenSection , sectionID, sectionTyp) {
	
	repInt <- getConstance(sectionTyp, FILL.WITH.NULL.SECTION)
	sectionID <- conectSectionIDVector(c(sectionID, rep.int(0, repInt)))
	
	if (sectionID %in% rownames(alreadyWrittenSection)) {
		return(alreadyWrittenSection[sectionID, ])
	} else {
		return(FALSE)
	}
}


setAlreadyWrittenSectionMatrix <- function(alreadyWrittenSection, sectionID, sectionTyp) {
	##########
#sectionID <- subsectionID
#sectionTyp <- SECOND.SECTION
	##########
	
	repInt <- getConstance(sectionTyp, FILL.WITH.NULL.SECTION)
	sectionID <- conectSectionIDVector(c(sectionID, rep.int(0, repInt)))
	
	if (sectionID %in% rownames(alreadyWrittenSection)) {
		alreadyWrittenSection[sectionID, 1] <- TRUE
	} else {
		
		alreadyWrittenSection <- rbind(alreadyWrittenSection, TRUE)
		rownames(alreadyWrittenSection)[length(alreadyWrittenSection)] <- sectionID
	}
	return(alreadyWrittenSection)
}


writeInclude <- function(latexText, tabText, sectionTyp, alreadyWrittenFiles, debug = FALSE) {
	########
#sectionTyp <- THIRD.SECTION
	########
	
	
	debug %debug% "writeInclude()"	
	
	fileNames <- getFileNames(path = paste(DIRECTORY.SECTION, "", sep = DIRECTORY.SEPARATOR), pattern = getConstance(sectionTyp, OVERALL.FILENAME.PATTERN), debug = debug)
	fileNames <- fileNames[!(fileNames %in% alreadyWrittenFiles)]
	alreadyWrittenFiles <- c(alreadyWrittenFiles, fileNames)
	
	if (!is.null(fileNames)) {
		sectionIDVector <- str_sub(fileNames, str_locate(fileNames, fixed(NEW.SECTION.TEX))[, "end"]+1, -(nchar(TEX)+2))
		fileNames <- fileNames[order(as.numeric(sectionIDVector))]
		if (sectionTyp == FIRST.SECTION) {
			fileNames <- str_sub(fileNames, 1, str_locate(fileNames, TEX.PATTERN)[, "start"]-1)
		}
		for(nn in fileNames) {
			
			
			nn <- paste(DIRECTORY.SECTION, nn, sep = DIRECTORY.SEPARATOR)
			#if (sectionTyp == FIRST.SECTION) {
			#	latexText <- includeFile(latexText, tabText, nn)
			#} else {
			latexText <- inputFile(latexText, tabText, nn)
			#}
		}
	}
	return(list(latexText = latexText, alreadyWrittenFiles = alreadyWrittenFiles))
}


includeFile <- function(latexText, tabText, file) {
	return(paste(latexText, tabText, "\\include{", file, "}", NEWLINE.TEX, sep = ""))
}

inputFile <- function(latexText, tabText, file) {
	return(paste(latexText, tabText, "\\input{", file, "}", NEWLINE.TEX, sep = ""))
}

includeAppendixAndError <- function(buildAppendixTexPart, tabText, pattern, debug = debug) {
	debug %debug% "includeAppendixAndError()"
	
	fileName <- getFileNames(path = paste(DIRECTORY.PLOTSTEX, "", sep = DIRECTORY.SEPARATOR), pattern = paste(SECTION.TEX, pattern, sep = ""), debug = debug)
	if (length(fileName) > 0) {
		buildAppendixTexPart <- setResetCheckAndIFPart(buildAppendixTexPart, fileName, tabText)
		tabText <- increaseTabText(tabText)
		buildAppendixTexPart <- setAdditionInfos(buildAppendixTexPart, FIRST.SECTION, pattern, tabText)
		buildAppendixTexPart <- setLoadImageAndLoadTex(buildAppendixTexPart, fileName, tabText)
		tabText <- reduceTabText(tabText)
		buildAppendixTexPart <- setClosedBraces(buildAppendixTexPart, tabText)		
		return(buildAppendixTexPart)
	} else {
		return(buildAppendixTexPart)
	}
}

writeReportFile <- function(tabText = "", debug = FALSE) {
	debug %debug% "writeReportFile()"
	
	buildReportFileText <- inputFile("", tabText, "reportDefHead") 
	buildReportFileText <- inputFile(buildReportFileText, tabText, "reportDefGeneralSection")
	buildReportFileText <- inputFile(buildReportFileText, tabText, "reportCluster")
	buildReportFileText <- writeInclude(buildReportFileText, tabText, FIRST.SECTION, "", debug = debug)$latexText
	buildReportFileText <- includeAppendixAndError(buildReportFileText, tabText, APPENDIX.SECTION, debug = debug)
	buildReportFileText <- includeAppendixAndError(buildReportFileText, tabText, ERROR.SECTION, debug = debug)
	buildReportFileText <- inputFile(buildReportFileText, tabText, "reportFooter")
	writeOnlyALatexFile(buildReportFileText, REPORT)
}

buildReportTex <- function(debug) {
	debug %debug% "buildReportTex()"
	##################
#linePlot		01		
#barPlot		50
#boxPlot		20
#spiderPlot		30
#violinPlot		40
#stackedPlot	60
#linerangePlot	70
#lineMultiPlot 80
	##################	
	
	fileNames <- getFileNames(path = paste(DIRECTORY.PLOTSTEX, "", sep = DIRECTORY.SEPARATOR), pattern = SECTION.PATTERN, debug = debug)
	#print(fileNames)
	if (!is.null(fileNames)) {
		sectionMatrix <- getSectionMatrix(str_sub(fileNames, str_locate(fileNames, fixed(SECTION.TEX))[, "end"]+1, -(nchar(TEX)+2)), fileNames, debug = debug)
		colnames(sectionMatrix) <- c(names(sectionMappingList), "file")
		buildSectionTexFile(sectionMatrix, debug = debug)	
	}
	
	#fileNames <- getFileNames(path = ".", pattern = OVERALL.SECTION.PATTERN, debug = debug)
	#if (!is.null(fileNames)) {
	#	sectionIDVector <- str_sub(fileNames, str_locate(fileNames, fixed(OVERALL.SECTION.TEX))[, "end"]+1, -(nchar(TEX)+2))
	writeReportFile(debug = debug)
	#}
}

saveImageFile <- function(overallList, plot, fileName, newHeight = "") {
	
	#filename = preprocessingOfValues(paste(filename, extraString, sep = ""), FALSE, replaceString = "_")	
	if (newHeight == "") {
		height <- 5
	} else {
		height <- newHeight
	}
	
	#print(filename)
	#ggsave (filename = paste(paste(filename, runif (1, 0.0, 1.0)), overallList$saveFormat, sep = "."), plot = plot, dpi = as.numeric(overallList$dpi), width = 8, height = 5)
	fileName <- getPlotFileName(fileName)
#	print(paste("before Max Memory: ", memory.size(TRUE), sep = ""))
#	print(paste("before actually used Memory: ", memory.size(FALSE), sep = ""))
	
#print(filename)
	ggsave (filename = paste(fileName, overallList$saveFormat, sep = "."), plot = plot, dpi = as.numeric(overallList$dpi), width = 8, height = height)
	
}

getPlotFileName <- function(filename) {
	return(paste(DIRECTORY.PLOTS, filename, sep = DIRECTORY.SEPARATOR))
}

#makeDepthBoxplotDiagram <- function(h, overallList) {
#	overallList$debug %debug% "makeDepthBoxplotDiagram()"
#	overallList$symbolParameter = 15
#	
#	if (h == 1) {
#		openImageFile(overallList)
#	}
#	par(mar = c(4.1, 4.1, 2.1, 2.1))
#	plot.depth(as.matrix(overallList$overallResult), plot.type = h, xlabel = overallList$xAxisName, l.width = 12, lp.color = overallList$color)
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

conact <- function(data, whichColumShouldUse) {
	if (length(whichColumShouldUse) == 1) {
		data <- t(t(data))
	}
	conactVector <- data.frame()
	for(nn in seq(along = whichColumShouldUse)) {
		if (nn > 1) {
			conactVector <- paste(conactVector, as.character(data[, nn]))
		} else {
			conactVector <- as.character(data[, nn])
		}
	}
	return(conactVector)
}

getPrimAndOrName <- function(colNames) {
	if (PRIMAER.TREATMENT %in% colNames) {
		if (NAME %in% colNames) {
			return(c(PRIMAER.TREATMENT, NAME))
		} else {
			return(PRIMAER.TREATMENT)
		}
	} else {
		return(c(NAME))
	}
}


checkIfOneColumnHasOnlyValues <- function(overallResult, descriptor = "", typOfPlot = NBOX.PLOT, first = FALSE, second = FALSE) {	
	#########
#first <- overallList$splitTreatmentFirst
#second <- overallList$splitTreatmentSecond
#index <- levels(overallResult[, whichColumShouldUse])[1]
	#########
	if ((first && second) || (first && !second) || (!first && second)) {
		if (typOfPlot == NBOX.PLOT) {
			whichColumShouldUse <- getPrimAndOrName(colnames(overallResult))
		} else {
			whichColumShouldUse <- checkWhichColumShouldUseForPlot(first, second, colnames(overallResult), typOfPlot, FALSE) # TRUE ## bei true true -> FALSE ansosnten TRUE?
		}
	} else {
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(first, second, colnames(overallResult), typOfPlot, TRUE) # TRUE ## bei true true -> FALSE ansosnten TRUE?		
	}
	
	if (typOfPlot == NBOX.MULTI.PLOT) {
		whichColumShouldUse <- c(whichColumShouldUse, COLUMN)
	}
	overallResultTemp <- overallResult
	overallResultTemp$check <- conact(overallResult[, whichColumShouldUse], whichColumShouldUse)
	
	max <- -1	
	for (index in unique(overallResultTemp$check)) {
		if (typOfPlot == NBOX.PLOT || typOfPlot == BOX.PLOT || typOfPlot == NBOX.MULTI.PLOT) {	
			temp <- sum(!is.na(overallResult$mean[overallResultTemp[, "check"] == index]))
		} else {
			boolVec <- overallResultTemp[, "check"] == index
			temp <- sum(!is.na(overallResultTemp[boolVec, descriptor]))
		}
		max <- ifelse(temp > max, temp, max)
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
#	return(data.frame(value = values, means = means, se = se))
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
#	return(data.frame(value = values, means = means, se = se, rowName = rowName))
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
	
	if (diagramTyp == STRESS.PLOT) {
		workingDataSet <- tempOverallResult[tempOverallResult$descriptor == imagesIndex, ]
		stressColumns <- grep("stress", colnames(workingDataSet), ignore.case = TRUE)
		standardValues <- seq(min(stressColumns)-1)
		
		#newWorkingDataSet <- matrix(ncol = length(c(standardValues, "values", "stress")), dimnames = list("", c(colnames(workingDataSet)[standardValues], "values", "stress")))
		
		for(nn in seq(along = stressColumns)) {
			if (nn == 1) {
				newWorkingDataSet <- cbind(workingDataSet[, c(standardValues)], values = as.numeric(as.character(workingDataSet[, stressColumns[nn]])), stress = colnames(workingDataSet)[stressColumns[nn]])
			} else {
				newWorkingDataSet <- rbind(newWorkingDataSet, cbind(workingDataSet[, c(standardValues)], values = as.numeric(as.character(workingDataSet[, stressColumns[nn]])), stress = colnames(workingDataSet)[stressColumns[nn]]))
			}
		}
		
		workingDataSet <- newWorkingDataSet
		
	} else if (diagramTyp == STACKBOX.PLOT || diagramTyp == SPIDER.PLOT || diagramTyp == LINERANGE.PLOT) {
		workingDataSet = tempOverallResult[tempOverallResult$plot == imagesIndex, ]
		workingDataSet$hist = factor(workingDataSet$hist, unique(workingDataSet$hist))
	} else {
		colNames = vector()
		
		if (diagramTyp == NBOX.PLOT || diagramTyp == BAR.PLOT) {
			colNames = c(MEAN, SE)
		} else if (diagramTyp == BOX.PLOT) {
			colNames = c(VALUES)
		} else if (diagramTyp == VIOLIN.PLOT) {
			colNames = c(MEAN)
		} else if (diagramTyp == NBOX.MULTI.PLOT) {
			colNames = c(MEAN, SE, COLUMN)
		}
		
		standardColumnName = getStandardColnames(tempOverallResult)
		
#		if (PRIMAER.TREATMENT %in% colnames(tempOverallResult)) {
#			standardColumnName = c("name", PRIMAER.TREATMENT, "xAxis")
#		} else {
#			standardColumnName = c("name", "xAxis")
#		}
		
		if (sum(!(colNames %in% colnames(tempOverallResult)))>0) {
			
			workingDataSet = buildDataSet(tempOverallResult[, standardColumnName], tempOverallResult, colNames, imagesIndex, diagramTyp)
			lengthOfNewColumns <- length(colnames(workingDataSet[, -c(1:length(standardColumnName))]))
			
#			if (diagramTyp == "spiderplot") {
#				if (lengthOfNewColumns > 1) {
#					colnames(workingDataSet) = c(standardColumnName, paste(rep.int(colNames, lengthOfNewColumns), 1:lengthOfNewColumns, sep = ""))
#				} else {
#					colnames(workingDataSet) = c(standardColumnName, colNames)
#				}
#			} else {
			colnames(workingDataSet) = c(standardColumnName, colNames)
#			}
			
#			if (lengthOfNewColumns > 1) {
#				colnames(workingDataSet) = c(standardColumnName, paste(rep.int(colNames, lengthOfNewColumns), 1:lengthOfNewColumns, sep = ""))
#			} else {
#				colnames(workingDataSet) = c(standardColumnName, colNames)
#			}
			
		} else {
			workingDataSet = tempOverallResult
		}
	}
	return(workingDataSet)	
}

#newTreatmentNameFunction <- function(seq, n, numberCharAfterSeparate, tooLong, maxValues) {
#	#numberCharAfterSeparate <- 8
#	if (tooLong) {
##		if (maxValues > 10)
#		
#		if (nchar(n) > (numberCharAfterSeparate + 4)) {
#			newTreatmentName <- paste(seq, ". ", substr(n, 1, numberCharAfterSeparate), " ...", sep = "")
#		} else {
#			newTreatmentName <- paste(seq, ". ", n, sep = "")
#		}
#	} else {
#		newTreatmentName <- n
#	}
#	return(newTreatmentName)
#}

newTreatmentNameFunction <- function(seq, n, numberCharAfterSeparate, lengthForPoints) {
	
	if (nchar(n) > (numberCharAfterSeparate + lengthForPoints)) {
		return(paste(seq, ". ", substr(n, 1, numberCharAfterSeparate), "...", sep = ""))
	} else {
		return(paste(seq, ". ", n, sep = ""))
	}	
	#return(newTreatmentName)
}

renameOfTheTreatments <- function(overallList) {
	overallList$debug %debug% "renameOfTheTreatments()"
	
	if (!overallList$appendix) {		
		if (overallList$filterTreatment[1] != NONE) {
			overallList$filterTreatmentRename <- setFilterTreatmentRename(overallList$filterTreatment, overallList$filterTreatmentRename, "conditionsFirstFilter")
		}
		
		if (overallList$filterSecondTreatment[1] != NONE) {
			overallList$secondFilterTreatmentRename <- setFilterTreatmentRename(overallList$filterSecondTreatment, overallList$secondFilterTreatmentRename, "conditionsSecondFilter")
		}
	}
	return(overallList)
}

setFilterTreatmentRename <- function(oldNames, newNames, fileName) {
	columnName <- c("Short name", "Full Name")
	numberCharAfterSeparate <- 8
	lengthForPoints <- 4
	
	if (length(oldNames) > 10) {
		numberCharAfterSeparate <- 6
	}
	
	tooLong <- checkIfTooLong(oldNames, (numberCharAfterSeparate + lengthForPoints))
	
	if (tooLong) {
		writeLatexTable(fileName, columnName, columnWidth = c("3cm", "13cm"))
	}
	
	seq <- 0;
	for(n in oldNames) {	
		if (tooLong) {
			seq <- seq+1
#			if (n == "") {
#				n <- " "
#			}
			newNames[[n]] <- newTreatmentNameFunction(seq, n, numberCharAfterSeparate, lengthForPoints)
			writeLatexTable(fileName, value = c(newNames[[n]], n))
		} else {
			newNames[[n]] <- n
		}
	}
	if (tooLong) {
		writeLatexTable(fileName)
	}
	return(newNames)
}


checkIfTooLong <- function(nameVector, cutline) {
	for(n in nameVector) {
		if (nchar(n) > cutline) {
			return(TRUE)
			break
		}
	}
	return(FALSE)
}

replaceTreatmentNamesOverallOneValue <- function(overallList, title, typOfPlot = "") {
	######
	##listForGetTitle <- optionListForGetBoolean
#title <- nn
	######
	overallList$debug %debug% "replaceTreatmentNamesOverallOneValue()"
	#if (!overallList$appendix) {
	if ((typOfPlot == SPIDER.PLOT || typOfPlot == BOX.PLOT || typOfPlot == STACKBOX.PLOT || typOfPlot == LINERANGE.PLOT || typOfPlot == NBOX.PLOT || typOfPlot == NBOX.MULTI.PLOT) && 
			!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {		
		title <- replaceTreatmentNames(overallList, title, onlySecondTreatment = TRUE, oneValue = TRUE)
	} else {
		title <- replaceTreatmentNames(overallList, title, onlyFirstTreatment = TRUE, oneValue = TRUE)
	}
	#}
	
	return(title)
}

#replaceEmptyStringWithSpace <- function(overallResult) {
#	if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
#		overallResult[overallResult[[PRIMAER.TREATMENT]] == "", PRIMAER.TREATMENT] <- " "
#	}
#	if (NAME %in% colnames(overallResult)) {
#		overallResult[overallResult[[NAME]] == "", NAME] <- " "
#	}
#}


replaceTreatmentNamesOverall <- function(overallList, overallResult) {	
	overallList$debug %debug% "replaceTreatmentNamesOverall()"
	#if (!overallList$appendix) {
	
	if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
		if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
			
#				if (length(overallList$filterTreatmentRename) == 0 && length(overallList$secondFilterTreatmentRename) == 0) {
			overallResult[[NAME]] <- replaceTreatmentNames(overallList, overallResult[[NAME]])
#				} else {
#					overallResult[[NAME]] <- replaceTreatmentNames(overallList, conectEachRow(as.matrix(overallResult[, c(PRIMAER.TREATMENT, NAME)]), "/"))
#				}
		} else {
			overallResult[[NAME]] <- replaceTreatmentNames(overallList, overallResult[[NAME]], onlySecondTreatment = TRUE)
			overallResult[[PRIMAER.TREATMENT]] <- replaceTreatmentNames(overallList, overallResult[[PRIMAER.TREATMENT]], onlyFirstTreatment = TRUE)
		}
	} else {
		overallResult[[NAME]] <- replaceTreatmentNames(overallList, overallResult[[NAME]], onlyFirstTreatment = TRUE)
	}
	#}
	return(overallResult)
}

replaceTreatmentNames <- function(overallList, columnWhichShouldReplace, onlyFirstTreatment = FALSE, onlySecondTreatment = FALSE, oneValue = FALSE) {
	##########
	##columnWhichShouldReplace <- overallResult$name
	##onlyFirstTreatment <- TRUE
	##onlySecondTreatment <- TRUE
	##n <- overallList$filterTreatment[1]
	##k <- overallList$filterSecondTreatment[1]
#columnWhichShouldReplace <- title
	##########
	
#print(columnWhichShouldReplace)
#print(onlyFirstTreatment)
#print(onlySecondTreatment)
	
	overallList$debug %debug% "replaceTreatmentNames()"
	
	columnWhichShouldReplace <- as.character(columnWhichShouldReplace)
	
	if (overallList$filterSecondTreatment[1] != NONE && !onlyFirstTreatment && !onlySecondTreatment) {
		if (oneValue) {
			columnWhichShouldReplace <- paste(overallList$filterTreatmentRename[[columnWhichShouldReplace]], "/", overallList$secondFilterTreatmentRename[[columnWhichShouldReplace]], sep = "")
		} else {
			
			if (length(overallList$filterTreatment) < 2) {
				#columnWhichShouldReplace <- overallList$secondFilterTreatmentRename[[columnWhichShouldReplace]]
#				for(k in overallList$filterSecondTreatment) {
#					replace(columnWhichShouldReplace, columnWhichShouldReplace == k, as.character(overallList$secondFilterTreatmentRename[[k]]))
#				}
				columnWhichShouldReplace <- replaceOnlySecond(overallList, columnWhichShouldReplace, TRUE, oneValue)
			} else if (length(overallList$filterSecondTreatment) < 2) {
				columnWhichShouldReplace <- replaceOnlyFirst(overallList, columnWhichShouldReplace, TRUE, oneValue)
			} else {
				for(n in overallList$filterTreatment) {
					for(k in overallList$filterSecondTreatment) {
						columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace == paste(n, "/", k, sep = ""), paste(overallList$filterTreatmentRename[[n]], "/", overallList$secondFilterTreatmentRename[[k]], sep = ""))
					}
				}
			}
		}
	} 
	
#	if (overallList$filterTreatment[1] != NONE && onlyFirstTreatment) {
#		if (oneValue) {
#			columnWhichShouldReplace <- overallList$filterTreatmentRename[[columnWhichShouldReplace]]
#		} else {
#			for(n in overallList$filterTreatment) {
#				columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace == n, overallList$filterTreatmentRename[[n]])
#			}
#		}
	columnWhichShouldReplace <- replaceOnlyFirst(overallList, columnWhichShouldReplace, onlyFirstTreatment, oneValue)
#	}
	
#	if (overallList$filterSecondTreatment[1] != NONE && onlySecondTreatment) {
#		if (oneValue) {
#			columnWhichShouldReplace <- overallList$secondFilterTreatmentRename[[columnWhichShouldReplace]]
#		} else {
#			for(n in overallList$filterSecondTreatment) {
#				columnWhichShouldReplace <- replace(columnWhichShouldReplace, columnWhichShouldReplace == n, overallList$secondFilterTreatmentRename[[n]])
#			}
#		}
	columnWhichShouldReplace <- replaceOnlySecond(overallList, columnWhichShouldReplace, onlySecondTreatment, oneValue)
#	}
	#ownCat(unique(columnWhichShouldReplace))
	return(as.factor(columnWhichShouldReplace))
}


replaceOnlyFirst <- function(overallList, columnWhichShouldReplace, onlyFirstTreatment, oneValue) {
	if (overallList$filterTreatment[1] != NONE && onlyFirstTreatment) {
		if (oneValue) {
			columnWhichShouldReplace <- overallList$filterTreatmentRename[[columnWhichShouldReplace]]
		} else {
			for(n in overallList$filterTreatment) {
				columnWhichShouldReplace <- replace(as.character(columnWhichShouldReplace), columnWhichShouldReplace == n, as.character(overallList$filterTreatmentRename[[n]]))
			}
		}
	}
	return(columnWhichShouldReplace)
}


replaceOnlySecond <- function(overallList, columnWhichShouldReplace, onlySecondTreatment, oneValue) {
	if (overallList$filterSecondTreatment[1] != NONE && onlySecondTreatment) {
		if (oneValue) {
			columnWhichShouldReplace <- overallList$secondFilterTreatmentRename[[columnWhichShouldReplace]]
		} else {
			for(n in overallList$filterSecondTreatment) {
				columnWhichShouldReplace <- replace(as.character(columnWhichShouldReplace), columnWhichShouldReplace == n, as.character(overallList$secondFilterTreatmentRename[[n]]))
			}
		}
	}
	return(columnWhichShouldReplace)
}


getString <- function(value, sep = ", ") {
	str <- character(0)
	#if (class(value) == data.frame) {
	value <- getVector(value)
	#}
	
	for(nn in value) {
		str <- paste(str, nn, sep = sep)
	}
	return(str)
}

createOuputOverview <- function(typ, actualImage, maxImage, imageName) {
	typString <- ""
	
	if (typ == NBOX.PLOT) {
		typString <- "line plot"
	} else if (typ == STACKBOX.PLOT) {
		typString <- "stacked barplot"
	} else if (typ == BOX.PLOT) {
		typString <- "box plot"
	} else if (typ == SPIDER.PLOT) {
		imageName <- getString(imageName)
		typString <- "spider plot"
	} else if (typ == LINERANGE.PLOT) {
		imageName <- getString(imageName)	
		typString <- "linerange plot"
	} else if (typ == VIOLIN.PLOT) {
		typString <- "violin plot"
	} else if (typ == STRESS.PLOT) {
		typString <- "stress plot"
	} else if(typ == NBOX.MULTI.PLOT) {
		typString <- "section plot"
	}
	
	ownCat(paste("Create ", typString, " ", actualImage, "/", maxImage, ": '", imageName, "'", sep = ""))
	
}

parseString2Latex <- function(text) {
	
	##text <- gsub("\\", "\\textbackslash ", text)
	text <- gsub("{", "\\{ ", text, fixed = TRUE)
	text <- gsub("}", "\\} ", text, fixed = TRUE)
	text <- gsub("ä", "{\\\"a}", text, fixed = TRUE)
	text <- gsub("ö", "{\\\"o}", text, fixed = TRUE)
	text <- gsub("ü", "{\\\"u}", text, fixed = TRUE)
	text <- gsub("ß", "{\\ss}", text, fixed = TRUE)
	text <- gsub("_", "{\\_}", text, fixed = TRUE)
	text <- gsub("<", "\\textless " , text, fixed = TRUE)
	text <- gsub(">", "\\textgreater ", text, fixed = TRUE)
	text <- gsub("§", "\\S ", text, fixed = TRUE)
	text <- gsub("$", "\\$ ", text, fixed = TRUE)
	text <- gsub("&", "\\& ", text, fixed = TRUE)
	text <- gsub("#", "\\# ", text, fixed = TRUE)
	text <- gsub("%", "\\% ", text, fixed = TRUE)
	text <- gsub("~", "\\textasciitilde ", text, fixed = TRUE)
	text <- gsub("€", "\\texteuro ", text, fixed = TRUE)
	
	return(text)
}

renameFilterOutput <- function(text) {
	
	text <- gsub(" = ", ": ", text, fixed = TRUE)
	text <- gsub("/", ", ", text, fixed = TRUE)
	text <- gsub("(", " (", text, fixed = TRUE)
	text <- gsub(" ", " ", text, fixed = TRUE)
	
	return(text)
}

renameY <- function(label) {
	
	if (length(grep("\\.\\.", label, ignore.case = TRUE)) > 0) {
		label <- sub("\\.\\.", " (", label)
		label <- paste(substring(label, 1, nchar(label)-1), ")", sep = "")
	}
	
	label <- sub("mm\\.2", "mm^2", label)	
	label <- sub("percent", "(%)", label)
	label <- sub("pixels", "(px)", label)
	label <- sub("(c p)", "(c/p)", label)
	label <- gsub("_", "-", label)
	label <- sub("(relative pix)", "(relative/px)", label)
	label <- gsub("\\.", " ", label)
	label <- gsub("\\(\\(", "(", label)
	label <- gsub("\\)\\)", ")", label)
	label <- str_trim(label)
	
	if (nchar(label) > 4) {
		if (substring(tolower(label), 1, 4) == "top ") {
			label <- paste(substring(label, 4), "top", sep = " - ")
		}
	}
	
	if (nchar(label) > 5) {
		if (substring(tolower(label), 1, 5) == "side ") {
			label <- paste(substring(label, 5), "side", sep = " - ")
		}
	}
	
	return(label)		
}

cleanSubtitle <- function(label) {
	
	#print(label)
	label <- sub("\\(g\\)", "", label)
	label <- sub("\\(mm\\)", "", label)
	label <- sub("\\(mm\\^2\\)", "", label)	
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
	filename <- preprocessingOfValues(paste(filename, extraString, sep = ""), FALSE, replaceString = "_")
	if (nchar(filename) > 90) {
		filename <- substr(filename, 1, 90)
	}
	return(filename)
}

writeTheData <- function(overallList, plot, fileName, extraString, writeLatexFileFirstValue = "", subSectionTitel = "", makeOverallImage = FALSE, isAppendix = FALSE, subsectionDepth = 1, typOfPlot = "", section) {
	########
#fileName <- overallFileName
#extraString <- paste(title, typOfPlot, sep = "")
#writeLatexFileFirstValue <- paste(overallFileName, typOfPlot, "OI", sep = "")
#subSectionTitel <- subtitle
#makeOverallImage <- overallImage
#isAppendix <- overallList$appendix
	########	
	#writeLatexFileSecondValue = "", 
	overallList$debug %debug% "writeTheData()"		
	
	fileName <- checkFileName(fileName, extraString)
	#print(fileName)
	if (subSectionTitel != "") {
		subSectionTitel <- parseString2Latex(subSectionTitel)
	}
#print("fileName gecheckt")
#	if (typOfPlot == LINERANGE.PLOT || (overallList$splitTreatmentFirst && overallList$splitTreatmentSecond && typOfPlot == SPIDER.PLOT)) {
	if (typOfPlot == LINERANGE.PLOT || typOfPlot == SPIDER.PLOT) {
		saveImageFile(overallList, plot, fileName, 12)
	} else {
		saveImageFile(overallList, plot, fileName)
	}
#print("bild gespeichert")
	if (typOfPlot != STRESS.PLOT) {
		if (makeOverallImage) {
			if (subSectionTitel != "") {
				writeLatexFile(writeLatexFileFirstValue, fileName, ylabel = subSectionTitel, subsectionDepth = subsectionDepth, saveFormatImage = overallList$saveFormat, section = section)	
			} else {
				writeLatexFile(writeLatexFileFirstValue, fileName, saveFormatImage = overallList$saveFormat, section = section)
			}
		}
	}
#print("latexFile geschrieben")
	
#	else {
#		writeLatexFile(fileName, writeLatexFileSecondValue)	
#	}
#print(isAppendix)
	if (isAppendix) {
		if (subSectionTitel != "") {
			writeLatexFile("appendixImage", fileName, ylabel = subSectionTitel, subsectionDepth = subsectionDepth, section = section)
		} else {
			writeLatexFile("appendixImage", fileName, section = section)
		}
	}
}

##
#	determine which libraries should be loaded/install
##
processLibs <- function(install = FALSE, update = FALSE, check = FALSE, catch = FALSE, debug = FALSE) {
	debug %debug% "loadLibs()"
	
	libraries <- c(
			"Cairo", "RColorBrewer", "data.table", "ggplot2", 
			"fmsb", "methods", "grid", "snow", "snowfall", "stringr")
#	loadInstallAndUpdatePackages(libraries, INSTALL.PACKAGE, CHECK.FOR.UPDATE, debug = debug)
	loadInstallAndUpdatePackages(libraries, install = install, update = update,  check = check, catch = catch, debug = debug)
}

myBreaks <- function(value) {
	minV <- min(value)
	maxV <- max(value)
	
	if ((maxV - minV) <= 10) {
		steps <- 1
	} else if ((maxV - minV) > 10 && (maxV - minV) < 30) {
		steps <- 5
	} else {
		steps <- 10
	}
	
	breaks <- seq(minV, maxV, steps)
	names(breaks) <- attr(breaks, "labels")
	return(breaks)
}


shapeTransparence <- function(column) {
	
	alpha <- 0.1
	
	numberOfDescriptors <- length(unique(column))
	
	if (numberOfDescriptors > 5) {
		alpha <- 0.1 - (numberOfDescriptors - 5) * 0.0036
	}
	
	if (alpha < 0) {
		alpha = 0
	}
	
	return(alpha)
}

testfunction <- function(parameter) {
	sum(abs(numbers - x) < 1e-6)
}

checkSumOfXAxis <- function(typOfPlot, sumOfXAxis, number) {
	if (typOfPlot == SPIDER.PLOT) {
		return(any(sumOfXAxis < number & sumOfXAxis != 0)) # es ist egal wenn etwas 0 ist ; | sumOfXAxis == 0
	} else if (typOfPlot == LINERANGE.PLOT) {
		return(all(sumOfXAxis < number & sumOfXAxis != 0))
	}
}

checkIfOnePlotTypeHasLessThanNumberValues <- function(overallResult, whichColumShouldUse, number, typOfPlot, remove = FALSE) {
	######
#number <- 2
#whichColumShouldUse <- c("name", "hist")
#remove <- TRUE
	######
	bool <- FALSE
	removeVector <- rep.int(TRUE, nrow(overallResult))
	
	if (typOfPlot == SPIDER.PLOT) {
		sumOfXAxis <- table(overallResult[, c(whichColumShouldUse, X.AXIS)])
		#	if (all(sumOfXAxis < number) && !all(sumOfXAxis > 0)) {# <- das gilt bei linerange
		if (checkSumOfXAxis(typOfPlot, sumOfXAxis, number)) {
			if (remove) {
				if (length(dim(sumOfXAxis)) == 1) {
					sumOfXAxis <- t(sumOfXAxis)
				}
				removeVector <- apply(overallResult, 1, function(x, y, z) {
							
							for(nn in seq(along = z)) {
								if (length(z) > nn) {
									y <- y[as.character(x[z[nn]]), ]
								} else {
									return(y[as.character(x[z[nn]])])
								}	
							}
							
						}, !(sumOfXAxis < number & sumOfXAxis != 0), c(whichColumShouldUse, X.AXIS))
				
				#overallResult <- overallResult[!(overallResult[, X.AXIS] == colnames(sumOfXAxis)[apply(sumOfXAxis < number & sumOfXAxis > 0, 2, "any")]), ]
			}
			bool <- TRUE
		}
	} else if (typOfPlot == NBOX.PLOT || typOfPlot == LINERANGE.PLOT || typOfPlot == NBOX.MULTI.PLOT) {
		
		dataTableOverallResult <- data.table(overallResult)
		
		if (typOfPlot == LINERANGE.PLOT) {
			sumOfXAxis <- as.data.frame(dataTableOverallResult[, lapply(list(xAxis), length), by = c(whichColumShouldUse, X.AXIS)])
			
			if (checkSumOfXAxis(typOfPlot, sumOfXAxis$V1, number)) {
#				if (remove) {
#					
#					removeVector <- apply(overallResult, 1, function(x, y, z) {
#								boolV <- rep.int(TRUE, nrow(y))
#								for(nn in seq(along = z)) {
#									boolV <- boolV & y[z[nn]] == as.character(x[z[nn]])
#								}
#								if (any(boolV)) {
#									return(FALSE)
#								} else {
#									return(TRUE)
#								}							
#							}, sumOfXAxis, c(whichColumShouldUse, X.AXIS))			
#				}
				bool <- TRUE
			}
			
		} else if (typOfPlot == NBOX.PLOT) {
			#		removeVector <- rep.int(TRUE, nrow(overallResult))
			#		dataTableOverallResult <- data.table(overallResult)
			sumOfXAxis <- as.data.frame(dataTableOverallResult[, lapply(list(xAxis), sum, na.rm = TRUE), by = c(whichColumShouldUse, X.AXIS)])
			for(n in unique(sumOfXAxis[, whichColumShouldUse])) {
				if (sum(as.character(sumOfXAxis[, whichColumShouldUse]) == as.character(n)) < number) {			
#					if (remove) {
#						removeVector <- removeVector & !(as.character(sumOfXAxis[, whichColumShouldUse]) == as.character(n))
#					} 			
					bool <- TRUE
				}
			}	
		}
		#	
	}
	
	overallResult <- overallResult[removeVector, ]	
	overallResult <- reduceLevels(overallResult)
	return(list(overallResult = overallResult, bool = bool))
}

makeStressDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title = "") {
	overallList$debug %debug% "makeStressDiagram()"	
	
	#overallFileName <- overallList$imageFileNames_nBoxplots[[imagesIndex]]
	#overallColor <- getColorRampPalette(colorVector = getColorVector(overallList$isGray), whichColumShouldUse = 5)
	#section <- buildSectionString(overallList$nBoxSection, imagesIndex, overallList$appendix)
	
	makeBarDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, TRUE)
}

makeLinearDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title = "") {
	#############
#title <- nn
#title <- ""	
	############	
	
# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")	
#	overallList$stressStart <- c(10, 20, 30, 37)
#	overallList$stressEnd <- c(13, 23, 33, 40)
#	overallList$stressTyp <- c("001", "001", "004", "003")
#	overallList$stressLabel <- c(-1, -1, -1, -1)
#	
#	overallList$stressStart <- c(10, 20, 30)
#	overallList$stressEnd <- c(13, 23, 33)
#	overallList$stressTyp <- c("002", "001", "002")
#	overallList$stressLabel <- c(-1, -1, -1)
	
	overallList$debug %debug% "makeLinearDiagram()"	
	ylabelForAppendix <- ""
	stressArea <- data.frame()
	hasLessThanThreeValues <- FALSE
	
	
	#overallColor <- overallList$color_nBox
#	if (typOfPlot == NBOX.MULTI.PLOT) {
#		overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)[[imagesIndex]]
#	} else {
	overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
	#}
	#color <- overallColor[[imagesIndex]]
	section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
	
#	if (length(grep("2147483647", overallResult$xAxis, ignore.case = TRUE))) {
#		if (length(unique(overallResult$xAxis)) >1 ) {
#			overallResult$xAxis[overallResult$xAxis == 2147483647] <- sort(unique(overallResult$xAxis), decreasing = TRUE)[2]+1
#		} else {
#			overallResult$xAxis[overallResult$xAxis == 2147483647] <- 1
#		}
#	}
	
	#isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		if (!checkIfOneColumnHasOnlyValues(overallResult, typOfPlot = typOfPlot, first = overallList$splitTreatmentFirst, second = overallList$splitTreatmentSecond)) {
			overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
			
			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
				whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, TRUE)
			} else {
				whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot)
			}
			
#			if (typOfPlot == NBOX.MULTI.PLOT) {
#				whichColumShouldUse <- c(whichColumShouldUse, COLUMN)
#			}
			
			#print(whichColumShouldUse)
			overallResult <- cbind(overallResult, ymin = overallResult$mean-overallResult$se, ymax = overallResult$mean+overallResult$se)
			
			hasLessThanThreeValues <- checkIfOnePlotTypeHasLessThanNumberValues(overallResult, whichColumShouldUse, 3, typOfPlot)$bool
			#hasLessThanTwoValues <- checkIfOnePlotTypeHasLessThanNumberValues(overallResult, whichColumShouldUse, 2, typOfPlot)$bool
			
			reorderList <- reorderThePlotOrder(overallResult, typOfPlot, whichColumShouldUse)
			overallResult <- reorderList$overallResult
			
#			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
#				colorReorder <- color[overallList$secondFilterTreatmentRename %in% title]
#				shapeReorder <- c(1:length(color))[overallList$secondFilterTreatmentRename %in% title]
#			} else {
			#colorReorder <- color[reorderList$sortList]
			#shapeReorder <- c(1:length(color))[reorderList$sortList]
			colorReorder <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
			overallColor <- colorReorder
			shapeReorder <- c(1:length(colorReorder))[reorderList$sortList]
			
			#}
			
			if (overallList$stressStart != -1) {
				stressArea <- buildStressArea(overallList$stressStart, overallList$stressEnd, overallList$stressTyp, overallList$stressLabel, overallResult$mean, typOfPlot, overallResult$se)
				colorReorder <- addColorForStressPhaseAndOther(stressArea, colorReorder, typOfPlot)
			}
			
			
			
#			print(whichColumShouldUse)
#			print(head(overallResult))
			plot <- 	ggplot() 
			
			if (length(stressArea) > 0) {
				plot <- plot + 
						geom_rect(data = stressArea, aes(xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax, fill = typ)) +
						#geom_text(data = stressArea, aes(x = xmin, y = (ymax-ymax*0.15), label = label), size = 2, hjust = 0, vjust = 1, angle = 90, colour = "grey")
						geom_text(data = stressArea, aes(x = xmin, y = ymin, label = label), size = 2, hjust = 0, vjust = 1, angle = 90, colour = "grey")
				
			}	
			
			if (((length(grep("%/day", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0 || 
							overallList$isRatio || 
							length(grep("relative", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0 || 
							length(grep("average", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0) &&
						(length(grep("blue marker", overallDesName[[imagesIndex]], ignore.case = TRUE)) <= 0)) &&
					!hasLessThanThreeValues) {
				plot <- plot + 
						geom_smooth(data = overallResult, aes_string(x = "xAxis", y = "mean", shape = whichColumShouldUse, ymin = "ymin", ymax = "ymax", colour = whichColumShouldUse, fill = whichColumShouldUse), method = "loess", stat = "smooth", alpha = shapeTransparence(overallResult[[whichColumShouldUse]]))
				
				#print(plot)
#				if (isOtherTyp) {
#					plot <- plot +
#							geom_smooth(data = overallResult, aes(x = xAxis, y = mean, shape = primaerTreatment, ymin = mean-se, ymax = mean+se, colour = primaerTreatment, fill = primaerTreatment), method = "loess", stat = "smooth", alpha = 0.1)
#				} else {
#					plot <- plot +
#							geom_smooth(data = overallResult, aes(x = xAxis, y = mean, shape = name, ymin = mean-se, ymax = mean+se, colour = name, fill = name), method = "loess", stat = "smooth", alpha = 0.1) 
#				}
				
				#plot <- plot + geom_ribbon(aes(ymin = mean-se, ymax = mean+se, fill = name), alpha = 0.1)
			} else if (length(grep("blue marker", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0) {
				plot <- plot + 
						geom_smooth(data = overallResult, aes(x = xAxis, y = mean, ymin = ymin, ymax = ymax), method = "loess", stat = "smooth", alpha = shapeTransparence(overallResult[[whichColumShouldUse]]))	
			} else {
				plot <- plot + 
						#plot <- 	ggplot()+
						geom_ribbon(data = overallResult, aes_string(x = "xAxis", y = "mean", ymin = "ymin", ymax = "ymax", fill = whichColumShouldUse), stat = "identity", alpha = shapeTransparence(overallResult[[whichColumShouldUse]])) +
						geom_line(data = overallResult, aes_string(x = "xAxis", y = "mean", colour = whichColumShouldUse), alpha = 0.95)
				#geom_point(data = overallResult, aes_string(x = "xAxis", y = "mean", color = whichColumShouldUse), size = 3)
				#print(plot)
#				if (isOtherTyp) {
#					plot <- plot + 
#	#						geom_ribbon(data = overallResult, aes(x = xAxis, y = mean, shape = name, ymin = mean-se, ymax = mean+se, fill = name), stat = "identity", alpha = 0.1) +
#	#						geom_line(data = overallResult, aes(x = xAxis, y = mean, color = name), alpha = 0.2)
#						geom_ribbon(data = overallResult, aes(x = xAxis, y = mean, shape = primaerTreatment, ymin = mean-se, ymax = mean+se, fill = primaerTreatment), stat = "identity", alpha = 0.1) +
#						geom_line(data = overallResult, aes(x = xAxis, y = mean, color = primaerTreatment), alpha = 0.2)
#				} else {
#					plot <- plot + 
#							#						geom_ribbon(data = overallResult, aes(x = xAxis, y = mean, shape = name, ymin = mean-se, ymax = mean+se, fill = name), stat = "identity", alpha = 0.1) +
#							#						geom_line(data = overallResult, aes(x = xAxis, y = mean, color = name), alpha = 0.2)
#							geom_ribbon(data = overallResult, aes(x = xAxis, y = mean, shape = name, ymin = mean-se, ymax = mean+se, fill = name), stat = "identity", alpha = 0.1) +
#							geom_line(data = overallResult, aes(x = xAxis, y = mean, color = name), alpha = 0.2)
#				}
			}
			#print(plot)
			
			if (length(grep("blue marker", overallDesName[[imagesIndex]], ignore.case = TRUE)) <= 0) {
				plot <- plot +	
						geom_point(data = overallResult, aes_string(x = "xAxis", y = "mean", colour = whichColumShouldUse, shape = whichColumShouldUse), size = 3)
#				if (isOtherTyp) {
#					plot <- plot +	
#							geom_point(data = overallResult, aes(x = xAxis, y = mean, color = primaerTreatment), size = 3)
#				} else {
#					plot <- plot +	
#							geom_point(data = overallResult, aes(x = xAxis, y = mean, color = name), size = 3)
#				}
			} else {
				plot <- plot +	
						geom_point(data = overallResult, aes(x = xAxis, y = mean), size = 3)
			}
			#ownCat("drinne")
#							ownCat(overallResult$xAxis)
#							ownCat(min(as.numeric(as.character(overallResult$xAxis))))
#							ownCat(max(as.numeric(as.character(overallResult$xAxis))))
			plot <- plot + 
					#scale_x_continuous(name = overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis))), limits = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis))))					
					scale_x_continuous(name = overallList$xAxisName, breaks = myBreaks(as.numeric(as.character(overallResult$xAxis))))		
			if (overallList$appendix) {
				ylabelForAppendix <- renameY(overallDesName[[imagesIndex]])
				plot <- plot + 
						ylab(ylabelForAppendix)
			} else {
				plot <- plot + 
						ylab(overallDesName[[imagesIndex]])
			}
			
			if (length(grep("blue marker", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0) {
				if ((length(colorReorder) - length(stressArea)) == 1) {
					colorReorder <- c(colorReorder, colorReorder)
				}
				
				shapeReorder <- c(1:2)
			}
			
			plot <- plot +
					scale_fill_manual(values = colorReorder, guide = "none") +
					scale_colour_manual(values = colorReorder) +
					scale_shape_manual(values = shapeReorder) +
					theme_bw()
			#print(plot)		
			if ((overallList$secondTreatment == NONE && overallList$splitTreatmentFirst) || 
					(overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) ||
					((!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) && 
						length(unique(overallResult[, whichColumShouldUse])) == 1)) {
				plot <- plot +
						theme(legend.position = "none", 
								legend.direction = "horizontal")
			} else {
				if (typOfPlot == NBOX.MULTI.PLOT) {
					plot <- plot +
							theme(legend.position = "bottom")
				} else {
					plot <- plot +
							theme(legend.position = "right")
				}
			}	
			plot <- plot +	
					theme(axis.title.x = element_text(face = "bold", size = 11), 
							axis.title.y = element_text(face = "bold", size = 11, angle = 90), 
							#panel.grid.major = element_blank(), # switch off major gridlines
							#panel.grid.minor = element_blank(), # switch off minor gridlines
							#legend.position = "right", # manually position the legend (numbers being from 0, 0 at bottom left of whole plot to 1, 1 at top right)
							legend.title = element_blank(), # switch off the legend title						
							#legend.key.size = unit(1.5, "lines"), 
							legend.key = element_blank(), # switch off the rectangle around symbols in the legend
							panel.border = element_rect(colour = "Grey", size = 0.1)
					) 
			#+ guides(colour = guide_legend("none"))
			plot <- setFontSize(plot, overallColor, typOfPlot)
			
			plot <- plot + guides(
					shape = guide_legend(ncol = calculateLegendRowAndColNumber(unique(as.character(overallResult[[whichColumShouldUse]])), typOfPlot), 
							byrow = FALSE), 
					colour = guide_legend(ncol = calculateLegendRowAndColNumber(unique(as.character(overallResult[[whichColumShouldUse]])), typOfPlot), 
							byrow = FALSE)
			)
			#print(plot)
#			if (length(overallColor[[imagesIndex]]) > 18 & length(overallColor[[imagesIndex]]) < 31) {
#				plot = plot + 
#					 theme(legend.text = element_text(size = 6), 
#							legend.key.size = unit(0.7, "lines"), 
#							strip.text.x = element_text(size = 6)
#						)
#			} else if (length(overallColor[[imagesIndex]]) >= 31) {
#				plot = plot + 
#					 theme(legend.text = element_text(size = 4), 
#							legend.key.size = unit(0.4, "lines"), 
#							strip.text.x = element_text(size = 4)
#						)
#			} else {
#				plot <- plot + 
#					 theme(legend.text = element_text(size = 11), 
#							strip.text.x = element_text(size = 11)
#						)
#			}
			
			if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
					(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
					(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
				if (title != "") {
					titleText <- title
					if (length(unique(overallResult[, whichColumShouldUse])) == 1 && !(overallList$secondTreatment == NONE && !(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond))) {
						titleText <- paste(titleText, " - ", unique(overallResult[, whichColumShouldUse]), sep = "")
					}
					plot = plot + ggtitle(titleText)
				}
			}
			
			if (length(grep("blue marker", overallDesName[[imagesIndex]], ignore.case = TRUE)) <= 0) {
				
				plot <- facetWrap(plot, overallResult, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
				
				
#				if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
#					# no facet_wrap!
#				} else 
#				if (overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
#					
#					if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
#						if (length(unique(overallResult$primaerTreatment)) > 1) {
#							plot <- plot + 
#									facet_wrap(~ primaerTreatment)
#						}
#					} else {
#						if (length(unique(overallResult$name)) > 1) {
#							plot <- plot + 
#									facet_wrap(~ name)
#						}
#					}
#				} else if (!(!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
#					if (length(unique(overallResult$name)) > 1) {
#						plot <- plot + 
#								facet_wrap(~ name)
#					}
#				}
			}
			
			#print(plot)
			
			subtitle <- ""
			overallImage <- TRUE
			subsectionDepth <- 1
			
			if (title != "") {
				sep <- " - "
			} else {
				sep <- ""
			}
			
			if (!overallList$appendix) {
				
#				if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
#					(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
#					(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
				
#				if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) && 
#					((length(grep("convex", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0) ||
#					 (length(grep("maximum extension", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0) ||
#					 (length(grep("_vis_hsv_", overallFileName, ignore.case = TRUE)) > 0))) {
#			 
#					subtitle <- paste(cleanSubtitle(overallDesName[[imagesIndex]]), title, sep = sep)
#					
#					if ((length(grep("convex", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0) ||
#					 (length(grep("maximum extension", overallDesName[[imagesIndex]], ignore.case = TRUE)) > 0)) {
#						subsectionDepth <- 3
#					} else if ((length(grep("_vis_hsv_", overallFileName, ignore.case = TRUE)) > 0)) {
#						subsectionDepth <- 4
#					} else {
#						subsectionDepth <- 2
#					}
#				}
			} else {
				subtitle <- paste(cleanSubtitle(ylabelForAppendix), title, sep = sep)
				
				subsectionDepth <- 1
				overallImage <- FALSE
			}
			#print(subtitle)
			##!# nicht löschen, ist die interpolation (alles in dieser if Abfrage mit #!# makiert)
			##!#				newCoords = seq(min(overallList$filterXaxis, na.rm = TRUE), max(overallList$filterXaxis, na.rm = TRUE), 1)
			##!#				newValue = approx(overallList$filterXaxis, overallList$overallResult[y, ], xout = newCoords, method = "linear")
			##!#				
			##!#				naVector = is.na(overallList$overallResult[y, ])
			##!#				overallResultWithNaValues = overallList$overallResult[y, ]
			##!#				overallList$overallResult[y, naVector] = newValue$y[overallList$filterXaxis[naVector]]
#				
#				if (firstPlot) {
#					firstPlot = FALSE
			##!#				plot(overallList$filterXaxis, overallList$overallResult[y, ], main = "", type = "c", xlab = overallList$xAxisName, col = overallList$color[y], ylab = overallList$yAxisName, pch = y, lty = 1, lwd = 3, ylim = c(min(overallList$overallResult, na.rm = TRUE), max(overallList$overallResult, na.rm = TRUE)))
#					plot(overallList$filterXaxis, overallList$overallResult[y, ], main = "", type = "b", xlab = overallList$xAxisName, col = overallList$color[y], ylab = overallList$yAxisName, pch = y, lty = 1, lwd = 3, ylim = c(min(overallList$overallResult, na.rm = TRUE), max(overallList$overallResult, na.rm = TRUE)))
#				} else {
			##!#				points(overallList$filterXaxis, overallList$overallResult[y, ], type = "c", col = overallList$color[y], pch = y, lty = 1, lwd = 3 )	
#					points(overallList$filterXaxis, overallList$overallResult[y, ], type = "b", col = overallList$color[y], pch = y, lty = 1, lwd = 3 )
#				}
			##!#				points(overallList$filterXaxis, overallResultWithNaValues, type = "p", col = overallList$color[y], pch = y, lty = 1, lwd = 3 )
#			} 
			
			#print(overallResult)
			writeTheData(overallList, plot, overallFileName, paste(title, typOfPlot, sep = ""), paste(overallFileName, typOfPlot, "OI", sep = ""), subtitle, overallImage, isAppendix = overallList$appendix, subsectionDepth = subsectionDepth, section = section)
			
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
	
	#print(input)
	color = vector()
	for (n in input) {
		color = c(color, overallColorIndex[[n]])
		#print(color)
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

setFontSize <- function(plot, value, typOfPlot, first = FALSE, second = FALSE) {
	
	
	if (typOfPlot == STACKBOX.PLOT) {
		if (PRIMAER.TREATMENT %in% colnames(value)) {
			numerOfDescriptors <- length(unique(value[[PRIMAER.TREATMENT]]))
		} else {
			numerOfDescriptors <- length(unique(value[[NAME]]))
		}
	} else if (typOfPlot == NBOX.PLOT || typOfPlot == NBOX.MULTI.PLOT || typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT || typOfPlot == BOX.PLOT) {
		numerOfDescriptors <- length(value)
	}
	#print(numerOfDescriptors)
	
	legendSize <- numeric()
	unitSize <- numeric()
	textSize <- numeric()
	
	if (numerOfDescriptors > 40) {
		if (typOfPlot == STACKBOX.PLOT) {
			textSize <- 5
		} else if (typOfPlot == NBOX.PLOT) {
			legendSize <- 4
			unitSize <- 0.1
			textSize <- 4
		} else if (typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
			if ((first && second)) {
				textSize <- 5
			}
		} else if (typOfPlot == BOX.PLOT) {
			textSize <- 5
		}
	} else if (numerOfDescriptors > 30) {
		if (typOfPlot == STACKBOX.PLOT) {
			textSize <- 6
		} else if (typOfPlot == NBOX.PLOT) {
			legendSize <- 5
			unitSize <- 0.5
			textSize <- 6
		} else if (typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
			if ((first && second)) {
				textSize <- 6
			}
		} else if (typOfPlot == BOX.PLOT) {
			textSize <- 6
		}
	} else if (numerOfDescriptors > 12 && numerOfDescriptors <= 30) {
		if (typOfPlot == STACKBOX.PLOT) {
			textSize <- 8
		} else if (typOfPlot == NBOX.PLOT) {
			legendSize <- 6
			unitSize <- 0.7
			textSize <- 8
		} else if (typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
			if ((first && second)) {
				textSize <- 8
			}
		} else if (typOfPlot == BOX.PLOT) {
			textSize <- 8
		}
	} else if (numerOfDescriptors > 8 && numerOfDescriptors <= 12) {
		if (typOfPlot == BOX.PLOT) {
			textSize <- 8
		}	
	}
	
	if (length(textSize) != 0) {
		if (typOfPlot == STACKBOX.PLOT) {
			plot <- plot + 
					theme(strip.text.x = element_text(size = 8))
		} else if (typOfPlot == NBOX.PLOT) { 
			plot = plot + 
					theme(legend.text = element_text(size = legendSize), 
							legend.key.size = unit(unitSize, "lines"), 
							strip.text.x = element_text(size = textSize)
					)
		} else if (typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
			if ((first && second)) {
				plot <- plot + 
						theme(strip.text.x = element_text(size = textSize), 
								strip.text.y = element_text(size = textSize))
			}
		} else if (typOfPlot == BOX.PLOT) {	
			plot <- plot +
					theme(axis.text.x = element_text(size = textSize, angle = 90))	
		}
	}
	return(plot)
}

#plotStackedImage <- function(overallResult, overallDesName, overallList, imagesIndex, title = "", legende = TRUE, minor_breaks = FALSE) {
makeStackedDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	#####
#legende <- TRUE
#positionType <- overallList$stackedBarOptions$typOfGeomBar[1]
#title <- ""
#title <- nn
	#####
	
	
#makeStress <- FALSE
	
	
# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")	
#	overallList$stressStart <- c(10, 20, 30, 37)
#	overallList$stressEnd <- c(13, 23, 33, 40)
#	overallList$stressTyp <- c("001", "001", "004", "003")
#	overallList$stressLabel <- c(-1, -1, -1, -1)
#	
#	overallList$stressStart <- c(10, 20, 30)
#	overallList$stressEnd <- c(13, 23, 33)
#	overallList$stressTyp <- c("002", "001", "002")
#	overallList$stressLabel <- c(-1, -1, -1)
	
	
	overallList$debug %debug% "makeStackedDiagram()"	
	
	legende <- TRUE
	overallColor <- overallList$color_boxStack
	overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)	
	section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
#print(section)
#print(overallDesName[[imagesIndex]])
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		
		isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
		
		if (length(overallList$stackedBarOptions$typOfGeomBar) == 0) {
			overallList$stackedBarOptions$typOfGeomBar = c("fill")
		}
		
		overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot)
		reorderList <- reorderThePlotOrder(overallResult, typOfPlot, whichColumShouldUse)
		overallResult <- reorderList$overallResult
				
#		if ("primaerTreatment" %in% colnames(overallResult)) {
#			overallResult$primaerTreatment <- replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
#		} else {
#			overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, TRUE)
#		}
#		print(overallList$stackedBarOptions$typOfGeomBar)
		for (positionType in overallList$stackedBarOptions$typOfGeomBar) {	
			#print(positionType)
			makeStress <- TRUE
			stressArea <- data.frame()
			#print(overallColor[[imagesIndex]])
			#print(overallResult)
			colorWithoutStress <- getColor(overallColor[[imagesIndex]], overallResult)
			#print(colorWithoutStress)
			if (positionType == "stack") {
				makeStress <- FALSE
			}
			
			if (overallList$stressStart != -1 && positionType != "fill" && makeStress) {
				stressArea <- buildStressArea(overallList$stressStart, overallList$stressEnd, overallList$stressTyp, overallList$stressLabel, overallResult$values, typOfPlot, positionType, overallResult[, c("xAxis", whichColumShouldUse)]) # getTheColumWhichShouldUse(colnames(overallResult))
				color <- addColorForStressPhaseAndOther(stressArea, colorWithoutStress, positionType)
			}
#			print(stressArea)
#			print(overallList$stressStart)
			plot <- ggplot()
			
			if (length(stressArea) >0) {
				plot <- plot + 
						geom_rect(data = stressArea, aes(xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax, fill = typ), guides = "none") +
						geom_text(data = stressArea, aes(x = xmin, y = (ymax-ymax*0.15), label = label), size = 2, hjust = 0, vjust = 1, angle = 90, colour = "grey")
				
			}	
			
			if (positionType == "dodge") {				
				plot <- plot +
						geom_line(data = overallResult, aes(x = xAxis, y = values, colour = hist), position = "identity") + 
						#scale_fill_manual(values = overallColor[[imagesIndex]]) +
						#scale_colour_manual(values = getColor(overallColor[[imagesIndex]], overallResult))
						scale_colour_manual(values = colorWithoutStress)
				
			} else {
				plot <- plot +
						geom_bar(data = overallResult, aes(x = xAxis, y = values, fill = hist), stat = "identity", position = positionType)
				
			}
			#print(plot)				
			if (positionType == "dodge" || positionType == "stack") {
				name <- sub("%", "px", overallDesName[[imagesIndex]])
			} else {
				name <- sub("(zoom corrected) ", "", overallDesName[[imagesIndex]])
			}
			
			plot <- plot + 
					ylab(name) +
					xlab(overallList$xAxisName)	
			
			if (positionType == "dodge" && length(stressArea) >0) {
				#	print("No1")
				plot <- plot +
						scale_fill_manual(values = color[!(color %in% colorWithoutStress)], name = "", labels = c(unique(as.character(stressArea$label))), guide = "none")
			} else if (positionType == "stack" && length(stressArea) >0) {
				#	print("No2")
				#if (length(stressArea) >0) {
				plot <- plot + 
						scale_fill_manual(values = color, name = "", labels = c(unique(as.character(stressArea$label)), unique(as.character(overallResult$hist))))
#				} else {
#					plot <- plot +
#						scale_fill_manual(values = colorWithoutStress, name = "")
#				}
			} else {
				#print("No3")
				#print(colorWithoutStress)
				plot <- plot +
						scale_fill_manual(values = colorWithoutStress, name = "")
			}
#			print("dadad")
#			 print(plot)
#			print("bis hier")
			#	scale_fill_manual(values = color, name = "") +
			#	scale_color_manual(value = c("red", "blue")) +
			#scale_fill_manual(values = getColor(overallColor[[imagesIndex]], overallResult), name = "") +
			plot <- plot +
					theme_bw() +
					theme(axis.title.x = element_text(face = "bold", size = 11), 
							axis.title.y = element_text(face = "bold", size = 11, angle = 90), 
							#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
							#panel.background = element_rect(linetype = "dotted"), 
							panel.border = element_rect(colour = "Grey", size = 0.1), 
							strip.background = element_rect(colour = NA), 
							#plot.title = element_text(size = 5) 
							#						plot.title = element_rect(colour = "Pink", size = 0.1)
							panel.grid.minor = element_blank()
					) 
			plot <- setFontSize(plot, overallResult, typOfPlot)
			
			if (!legende) {
				plot = plot + theme(legend.position = "none")
			} else {
				plot = plot + theme(legend.position = "right", 
						legend.title = element_blank(), 
						legend.text = element_text(size = 11), 
						legend.key = element_blank())
			}
			if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
					(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
					(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
				if (title != "") {
					titleText <- title
					whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, TRUE)
					if (length(unique(overallResult[, whichColumShouldUse])) == 1) {
						titleText <- paste(titleText, " - ", unique(overallResult[, whichColumShouldUse]), sep = "")
					}
					plot = plot + ggtitle(titleText)
				}
			}
			
			if (positionType == "fill") {
				plot = plot + scale_y_continuous(labels = seq(0, 100, 20), breaks = seq(0, 1, 0.2))
			}
			
			
#			if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
#				# no facet_wrap!
#			} else
#print(head(overallResult))
			
			plot <- facetWrap(plot, overallResult, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
#			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {		
#				if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
#					plot <- plot + 
#							facet_wrap(~ primaerTreatment)
#				} else {
#					plot <- plot + 
#							facet_wrap(~ name)
#				}
#			} else {
#				plot <- plot + 
#						facet_wrap(~ name)
#			}
			
			
#			if ("primaerTreatment" %in% colnames(overallResult)) {				
#				plot = plot + facet_wrap(~ primaerTreatment)
#			} else {
#				plot = plot + facet_wrap(~ name)
#			}
			
			#	print(plot)
			
			subtitle <- ""
#			if (positionType == overallList$stackedBarOptions$typOfGeomBar[1] || length(overallList$stackedBarOptions$typOfGeomBar) == 1) {
#				subtitle <- title
#			}
			
			
			writeTheData(overallList, plot, overallFileName, paste("overall", title, positionType, sep = ""), paste(overallFileName, "stackedOI", sep = ""), subtitle, TRUE, subsectionDepth = 2, section = section)
			
#			saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste("overall", title, positionType, sep = ""))
#			if (makeOverallImage) {
#				if (title != "") {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep = ""), paste(overallFileName[[imagesIndex]], "overall", title, positionType, sep = ""), TRUE, title)	
#				} else {
#					writeLatexFile(paste(overallFileName[[imagesIndex]], "stackedOverallImage", sep = ""), paste(overallFileName[[imagesIndex]], "overall", title, positionType, sep = ""))
#				}
#			} else {
#				writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], "overall", positionType, title, sep = "_"))	
#			}			
		}
	}
}

removeNAsSpider <- function(overallResult, xAxisPosition) {
	overallResultStart <- overallResult[1:xAxisPosition]
	overallResult <- overallResult[(xAxisPosition+1):length(colnames(overallResult))]
	booleanVector <- !apply(overallResult, 1, function(x)all(is.na(x)))
	
	return(cbind(overallResultStart[booleanVector, ], overallResult[booleanVector, ]))
}


openPlotDevice <- function(overallList, fileName, extraString, h) {
	if (h == 1) {
		filename = preprocessingOfValues(paste(fileName, extraString, sep = ""), FALSE, replaceString = "_")
		Cairo(width = 10, height = 7, file = paste(filename, overallList$saveFormat, sep = "."), type = overallList$saveFormat, bg = "transparent", units = "in", dpi = as.numeric(overallList$dpi))
	}
}

closePlotDevice <- function(h) {
	if (h == 1) {
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
	if (!first && second) {
		isOtherTyp <- TRUE
	}
	return(isOtherTyp)
}

normalizeEachDescriptor <- function(overallResult) {
	
	for(name in unique(overallResult$hist)) {
		overallResult[overallResult$hist == name, ]$values <- sapply(overallResult[overallResult$hist == name, ]$values, function(x, y) {(x/y)}, y = max(overallResult[overallResult$hist == name, ]$values))
	}
	return(overallResult)
}

makeSpiderDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	overallList$debug %debug% "makeSpiderDiagram()"		
	
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		#test <- c("side fluo intensity", "side nir intensity", "side visible hue average value", "top visible hue average value")
		#if (sum(!getVector(overallDesName[[imagesIndex]]) %in% test) > 1) {
		if (length(getVector(overallDesName[[imagesIndex]])) > 1) {
			plotSpiderImage(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
		} 
		#plotLineRangeImage(overallList, overallResult, overallDesName, imagesIndex, LINERANGE.PLOT, title)
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
#			plotSpiderImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende = TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex = imagesIndex, overallFileName = overallFileName, typOfPlot = typOfPlot)	
#		}
#		plotLineRangeImage(overallList = overallList, overallResult = overallResult, makeOverallImage = TRUE, legende = TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex = imagesIndex, overallFileName = overallFileName, typOfPlot = "lineRangePlot")	
#	} else {
#		for (value in overallList$filterSecondTreatment) {			
#			title = overallList$secondFilterTreatmentRename[[value]]
#			#plottedName = overallList$filterTreatment %contactAllWithAll% value
#			#booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = plottedName))
#			booleanVector = getBooleanVectorForFilterValues(overallResult, list(name = value))
#			plotThisValues = overallResult[booleanVector, ]
##			usedOverallColor <- overallColor[[imagesIndex]][1:length(unique(plotThisValues["primaerTreatment"])[, 1])]
##			overallColor[[imagesIndex]] <- overallColor[[imagesIndex]][(length(unique(plotThisValues["primaerTreatment"])[, 1])+1):length(overallColor[[imagesIndex]])]
#			
#			if (doSpiderPlot) {
#				plotSpiderImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende = TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex = imagesIndex, overallFileName = overallFileName, typOfPlot = typOfPlot)
#			}
#			plotLineRangeImage(overallList, plotThisValues, title = title, makeOverallImage = TRUE, legende = TRUE, usedoverallColor = overallColor[[imagesIndex]], overallDesName = overallDesName, imagesIndex = imagesIndex, overallFileName = overallFileName, typOfPlot = "lineRangePlot")
#		}	 
#	}
#}

reduceLevels <- function(overallResult) {
	
	for(nn in colnames(overallResult)) {
		if (class(overallResult[, nn]) == "factor") {
			overallResult[, nn] <- overallResult[, nn][, drop = TRUE]
		}
	}
	return(overallResult)
}


plotSpiderImage <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, legende = TRUE) {
	################
	##overallColor <- usedOverallColor 
	##	typOfPlot <- "spiderplot"
	##	makeOverallImage = TRUE
	##	usedoverallColor = overallColor[[imagesIndex]]
	##	overallResult <- plotThisValues
#legende = TRUE
#positionType <- overallList$spiderOptions$typOfGeomBar[1]
#title <- nn
#title <- ""
#overallResult <- overallResultSplit
	#################
	
	overallList$debug %debug% "plotSpiderImage()"	
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		
		overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
		options <- overallList$spiderOptions
		overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult[c(X.AXIS, VALUES)], VALUES, options) 
		overallResult <- na.omit(overallResult)
		
		if (overallResult %checkRowLengthOfDataFrame% 0) {
			overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
			overallResult <- normalizeEachDescriptor(overallResult)	
			
			#overallColor <- overallList$color_spider
			section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
			
			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
				pre <- TRUE
			} else {
				pre <- FALSE
			}
			
			whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, pre)		
			#overallResult <- checkIfOnePlotTypeHasLessThanNumberValues(overallResult, getColumnsForCheckIfOnePlotTypeHasLess(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, typOfPlot, whichColumShouldUse), 2, typOfPlot, TRUE)$overallResult
			overallResult <- checkIfOnePlotTypeHasLessThanNumberValues(overallResult, whichColumShouldUse, 2, typOfPlot, TRUE)$overallResult
			
#			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
#				overallColor <- getVector(overallList$color[levels(overallResult[[checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, TRUE)]])])
#			} else {
#				overallColor <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
#			}
			
			
			
			if (overallResult %checkRowLengthOfDataFrame% 0) {
				overallColor <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
				
				histVec <- levels(overallResult$hist)
				for(kk in seq(along = histVec)) {
					histVec[kk] <- paste(kk, histVec[kk])
				}
				overallResult$hist <- factor((overallResult$hist), levels((overallResult$hist)), seq(along = unique(overallResult$hist)))
				nameString <- unique(as.character(overallResult[[whichColumShouldUse]]))
				
				#		print(histVec)
				#		print(usedoverallColor)
				for (positionType in options$typOfGeomBar) {			
					
					
					#			if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
					#				overallResult <- cbind(overallResult, plot = rep.int(1, length(overallResult[, 1])))
					#				plot <- ggplot(overallResult, aes(plot, value, fill = plot))
					#				#} else if (overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
					#			} else {
					#print(head(overallResult))
					plot <- ggplot(overallResult, aes_string(x = HIST, y = VALUES, group = whichColumShouldUse, shape = whichColumShouldUse, color = whichColumShouldUse, fill = HIST)) +
							geom_point(size = 3) +
							geom_line()
					#}
					
					#	print(plot)
					
					#			if ("primaerTreatment" %in% colnames(overallResult)) {
					#				overallResult$primaerTreatment <- replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
					#				nameString <- unique(as.character(overallResult$primaerTreatment))
					#				
					##				plot = ggplot(data = overallResult, aes(x = hist, y = values, group = primaerTreatment)) +
					##						geom_point(aes(color = as.character(primaerTreatment), shape = hist), size = 3) +
					##						geom_line(aes(colour = as.character(primaerTreatment))) 
					#				
					#				plot = ggplot(data = overallResult, aes(x = hist, y = values, group = as.character(primaerTreatment), shape = as.character(primaerTreatment), color = as.character(primaerTreatment), fill = hist)) +
					#						geom_point(size = 3) +
					#						geom_line() 
					#			} else {
					#				overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, TRUE)
					#				nameString <- unique(as.character(overallResult$name))
					#				
					#				plot = ggplot(data = overallResult, aes(x = hist, y = values, group = as.character(name), color = as.character(name), shape = as.character(name), fill = hist)) +
					#						geom_point(size = 3) +
					#						geom_line() 
					#			}
					
					plot <- plot +
							#geom_point(aes(color = as.character(name), shape = hist), size = 3) +
							#					scale_shape_manual(values = c(1:length(unique(overallResult$hist))), name = "Property") +
							#scale_shape_manual(values = c(1:length(unique(as.character(overallResult$primaerTreatment)))), name = "Property") +
							scale_shape_manual(values = c(1:length(nameString)), name = "Property") +
							#geom_line(aes(colour = as.character(name))) +
							#	scale_colour_manual(name = "Condition", values = usedoverallColor)
							scale_colour_manual(values = overallColor, name = "Property") + 
							scale_fill_manual(values = rep.int("black", length(histVec)), name = "Condition", breaks = unique(overallResult$hist), labels = histVec)
					#print(plot)
					if (positionType == "x") {			
						#plot <- plot + coord_polar(theta = "x", expand = TRUE)
						plot <- plot + coord_polar(theta = "x")
					} else {
						#plot <- plot + coord_polar(theta = "y", expand = TRUE)
						plot <- plot + coord_polar(theta = "y")
					}
					#print(plot)
					plot <- plot + 
							scale_y_continuous() +
							theme_bw() +
							theme(#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), # Rand geht nicht in ggplot 0.9
									axis.title.x = element_blank(), 
									axis.title.y = element_blank(), 
									#								axis.title.y = element_text(face = "bold", size = 11, angle = 90), 
									panel.grid.minor = element_blank(), 
									panel.border = element_rect(colour = "Grey", size = 0.1)
							#axis.text.x = element_blank(), 
							#axis.text.y = element_blank()
							)
					
					plot <- setFontSize(plot, overallColor, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)	
					#print(plot)
					if (positionType == "y") {
						plot <- plot + 
								Theme(axis.text.y = element_blank(), 
										axis.ticks	 = element_blank()	
								)
					}	
					
					if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
							(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
							(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
						if (title != "") {
							plot = plot + ggtitle(title)
						}
					}
					
					
					if (!legende) {
						plot = plot + theme(legend.position = NONE)
					} else {
						plot = plot + 
								theme(#legend.justification = "center", 
										legend.direction = "horizontal", 
										legend.position = "bottom", 
										legend.box = "vertical", 
										legend.title.align = 0.5, 
										#legend.position = c(0.5, 0), 
										# legend.title = element_blank(), 
										legend.key = element_blank()
								)			
						
						
						#				if (overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond && overallList$secondTreatment == "none") {
						#					plot <- plot + guides(fill = guide_legend(title.position = "top", 
						#									ncol = calculateLegendRowAndColNumber(histVec), 
						#									byrow = FALSE)
						#					)
						#				} else {
						plot <- plot + guides(
								shape = guide_legend(title.position = "top", 
										ncol = calculateLegendRowAndColNumber(nameString, typOfPlot), 
										byrow = FALSE), 
								fill = guide_legend(title.position = "top", 
										ncol = calculateLegendRowAndColNumber(histVec, typOfPlot), 
										byrow = FALSE)
						)
						#}
						
					}		
					#print(plot)
					#			if (title != "") {
					#				plot = plot + theme(title = title)
					#			}
					plot <- facetWrap(plot, overallResult, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, whichColumShouldUse)
					#			if (overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
					#				if (whichColumShouldUse == NAME) {
					#					plot = plot + facet_grid(name ~ xAxisfactor)
					#				} else {
					#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
					#				}
					#				
					#			} else {
					#				plot = plot + facet_grid(~ xAxisfactor)
					#			}
					#				if ("primaerTreatment" %in% colnames(overallResult)) {				
					#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
					#					
					#				} else {
					#					plot = plot + facet_grid(name ~ xAxisfactor)
					#				}
					
					#			print("drinne")
					#print(plot)
					
					subtitle <- ""
#					if (positionType == options$typOfGeomBar[1] || length(options$typOfGeomBar) == 1) {
#						subtitle <- title
#					}
					
					writeTheData(overallList, plot, overallFileName, paste(typOfPlot, title, positionType, sep = ""), paste(overallFileName, "spiderOI", sep = ""), subtitle, TRUE, subsectionDepth = 2, typOfPlot = typOfPlot, section = section)
					
					#			saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste(typOfPlot, title, positionType, sep = ""))
					#			if (makeOverallImage) {
					#				if (title != "") {
					#					writeLatexFile(paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep = ""), paste(overallFileName[[imagesIndex]], typOfPlot, title, positionType, sep = ""), TRUE, title)	
					#				} else {
					#					writeLatexFile(paste(overallFileName[[imagesIndex]], "spiderOverallImage", sep = ""), paste(overallFileName[[imagesIndex]], typOfPlot, title, positionType, sep = ""))
					#				}
					#			} else {
					#				writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], typOfPlot, positionType, title, sep = ""))	
					#			}
				}
			}
		}
	}				
}

calculateLegendRowAndColNumber <- function(legendText, typOfPlot) {
	########	
#legendText <- unique(overallResult$name)
#legendText <- unique(overallResult$hist)
#legendText <- overallColor[[imagesIndex]]
	#######	
	legendText <- as.character(getVector(legendText))
	
	if (typOfPlot == NBOX.PLOT) {
		ncol <- ceiling(length(legendText) / 40)
	} else {
		lengthOfOneRow <- 90
		
		averageLengthOfSet <- round(sum(nchar(legendText), na.rm = TRUE) / length(legendText))
		
		ncol <- floor(lengthOfOneRow / averageLengthOfSet) -1
		if (ncol == 0) {
			ncol <- 1
		} else if (ncol > length(legendText)) {
			ncol <- length(legendText)
		}	
	}
	return(ncol)
} 

getColumnsForCheckIfOnePlotTypeHasLess <- function(first, second, typOfPlot, whichColumShouldUse) {
	if (typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
		if (first && second) {
			return(c(whichColumShouldUse, HIST))	
		} else {
			return(HIST)
		}
	} 
}


makeLinerangeDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, legende = TRUE) {
	################
	##	makeOverallImage = TRUE
	##	legende = TRUE
	##	usedoverallColor <- overallColor[[imagesIndex]]
	##	overallResult <- plotThisValues
	##	positionType <- overallList$spiderOptions$typOfGeomBar[1]
#typOfPlot <- "lineRangePlot"
#legende <- TRUE
	##positionType <- overallList$spiderOptions$typOfGeomBar[1]
#title <- nn
#title <- ""
#overallResult <- overallResultSplit
	#################
	
	#ownCat(overallResult[1, ])
#tempoverallResult <- overallResult
#overallResult <- tempoverallResult
	overallList$debug %debug% "plotLineRangeImage()"
	
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
		options <- overallList$linerangeOptions
		overallResult$xAxisfactor = setxAxisfactor(overallList$xAxisName, overallResult[c("xAxis", "values")], "values", options)
		overallResult <- na.omit(overallResult)
		if (overallResult %checkRowLengthOfDataFrame% 0) {
			overallResult <- normalizeEachDescriptor(overallResult)
			overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
			#overallColor <- overallList$color_linerange
			
			section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
			
			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
				pre <- TRUE
			} else {
				pre <- FALSE
			}
			
			whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, pre)
			overallColor <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
			
#			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
#				overallColor <- getVector(overallList$color[levels(overallResult[[checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, TRUE)]])])
#			} else {
#				overallColor <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
#			}			
			
			isOnlyOneValue <- checkIfOnePlotTypeHasLessThanNumberValues(overallResult, getColumnsForCheckIfOnePlotTypeHasLess(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, typOfPlot, whichColumShouldUse), 2, typOfPlot)$bool
			nameString <- unique(as.character(overallResult[[whichColumShouldUse]]))
			
			#		if ("primaerTreatment" %in% colnames(overallResult)) {
			#			overallResult$primaerTreatment <- replaceTreatmentNames(overallList, overallResult$primaerTreatment, TRUE)
			#			nameString <- unique(as.character(overallResult$primaerTreatment))
			#		} else {
			#			overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, TRUE)
			#			nameString <- unique(as.character(overallResult$name))
			#		}
			#print(nameString)
			
			plot <- ggplot(data = overallResult, aes(x = hist, y = values))
			if (!isOnlyOneValue) {
				plot <- plot +
						geom_line()
			}
			plot <- plot +
					geom_point(aes_string(color = whichColumShouldUse, shape = whichColumShouldUse, fill = whichColumShouldUse), size = 3)
			#print(plot)
			#		if ("primaerTreatment" %in% colnames(overallResult)) {				
			#			plot <- plot + geom_point(aes(color = as.character(primaerTreatment), shape = as.character(primaerTreatment)), size = 3)
			#			
			#		} else {
			#			plot <- plot + geom_point(aes(color = as.character(name), shape = as.character(name)), size = 3)
			#		}
			#print(plot)
			#print(usedoverallColor)
			plot <- plot +
					scale_colour_manual(values = overallColor) +
					#scale_fill_manual(values = overallColor[[imagesIndex]]) +
					scale_shape_manual(values = c(1:length(nameString))) +
					scale_y_continuous() +
					theme_bw() +
					theme(#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), # Rand geht nicht in ggplot 0.9
							axis.title.x = element_blank(), 
							axis.title.y = element_blank(), 
							axis.text.x = element_text(angle = 90), 
							panel.grid.minor = element_blank(), 
							panel.border = element_rect(colour = "Grey", size = 0.1)
					#axis.text.x = element_blank()
					#axis.text.y = element_blank()
					) 
			
			plot <- setFontSize(plot, overallColor, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
			
			if (!legende) {
				plot = plot + theme(legend.position = "none")
			} else {
				plot = plot + 
						theme(#legend.justifiownCation = 'bottom', 
								legend.direction = "horizontal", 
								legend.position = "bottom", 
								legend.box = "vertical", 
								#legend.position = c(0.5, 0), 
								legend.title = element_blank(), 
								legend.key = element_blank()
						)
				
				plot <- plot + guides(colour = guide_legend(title.position = "top", 
								ncol = calculateLegendRowAndColNumber(nameString, typOfPlot), 
								byrow = T)			
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
				#			plot = plot + theme(legend.text = element_text(size = size), 
				#					legend.key.size = unit(unit, "lines"), 
				#					axis.text.x = element_text(face = "bold", size = size, angle = 90)
				#			)
			}		
			if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
					(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
					(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
				if (title != "") {
					plot = plot + ggtitle(title)
				}
			}
			
			plot <- facetWrap(plot, overallResult, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, whichColumShouldUse)
			#		if (overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
			#			if (whichColumShouldUse == NAME) {
			#				plot = plot + facet_grid(name ~ xAxisfactor)
			#			} else {
			#				plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
			#			}
			#			
			#		} else {
			#			plot = plot + facet_grid(~ xAxisfactor)
			#		}
			#				if ("primaerTreatment" %in% colnames(overallResult)) {				
			#					plot = plot + facet_grid(primaerTreatment ~ xAxisfactor)
			#					
			#				} else {
			#					plot = plot + facet_grid(name ~ xAxisfactor)
			#				}
			
			
			#print(plot)
			
			writeTheData(overallList, plot, overallFileName, paste(typOfPlot, title, sep = ""), paste(overallFileName, "lineRangeOI", sep = ""), "", TRUE, subsectionDepth = 2, typOfPlot = typOfPlot, section = section)
			
			#		saveImageFile(overallList, plot, overallFileName[[imagesIndex]], paste(typOfPlot, title, sep = ""))
			#		if (makeOverallImage) {
			#			writeLatexFile(paste(overallFileName[[imagesIndex]], "lineRangeOverallImage", sep = ""), paste(overallFileName[[imagesIndex]], typOfPlot, title, sep = ""))	
			#		} else {
			#			writeLatexFile(overallFileName[[imagesIndex]], paste(overallFileName[[imagesIndex]], typOfPlot, title, sep = "_"))	
			#		}			
		}
	}		
}

lim <- function(value, newValue) {
	if (value != 0) {
		limValue <- (newValue/value) + 0.1
		
		if (limValue > 1.15 || limValue < 0.85) {
			return(FALSE)
		} else {
			return(TRUE)
		}
	} else {
		return(FALSE)
	}
}

checkIfNaForMinMax <- function(overallResult, column) {
	if (all(is.na(overallResult[column])) || all(is.null(overallResult[column]))) {
		minVal <- 0
		maxVal <- 0
	} else {
		minVal <- min(overallResult[column], na.rm = TRUE)
		maxVal <- max(overallResult[column], na.rm = TRUE)
	}
	return(list(minVal = minVal, maxVal = maxVal))
}


scallyAxis <- function(factor, overallResult) {
	
#	if (all(is.na(overallResult$se))) {
#		minSe <- 0
#		maxSe <- 0
#	} else {
#		minSe <- min(overallResult$se, na.rm = TRUE)
#		maxSe <- max(overallResult$se, na.rm = TRUE)
#	}
	seV <- checkIfNaForMinMax(overallResult, SE)
	meanV <- checkIfNaForMinMax(overallResult, MEAN)
	
	minValue <- meanV$minVal - seV$minVal
	maxValue <- meanV$maxVal + seV$maxVal
	middelBorder <- 0.5 * factor
	minBorder <- 0.1 * factor
	
	if (minValue > -minBorder && maxValue < minBorder) {
		factor <- minBorder
	} else if (minValue > -middelBorder && maxValue < middelBorder) {
		factor <- middelBorder
	}
	
	yminValue <- floor(minValue / factor) * factor
	ymaxValue <- ceiling(maxValue / factor) * factor
	
	if (lim(minValue, yminValue)) {
		yminValue <- yminValue - factor
	}
	
	if (lim(maxValue, ymaxValue)) {
		ymaxValue <- ymaxValue + factor
	}
	
	return(coord_cartesian(ylim = c(yminValue, ymaxValue)))
	
}

makeBarDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title, isOnlyOneValue = FALSE) {
	overallList$debug %debug% "makeBarDiagram()"	
	#######
#isOnlyOneValue <- TRUE
	#######
	overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
	prePlot <- FALSE
	if (typOfPlot == NBOX.PLOT || typOfPlot == NBOX.MULTI.PLOT) {
		overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
		#	overallColor <- overallList$color_nBox
		
		section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
		if ((!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond)) { #|| (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) # (overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) ||
			prePlot <- TRUE
		} else {
			prePlot <- FALSE
		}
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, prePlot)
		yValue <- MEAN
	} else if (typOfPlot == STRESS.PLOT) {
		overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
		#overallColor <- getColorRampPalette(colorVector = getColorVector(overallList$isGray), whichColumShouldUse = 5)
		#overallColor <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
		section <- getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE)
		#section <- buildSectionString(overallList$nBoxSection, imagesIndex, overallList$appendix)
		whichColumShouldUse <- NAME
		yValue <- VALUES
	}
	
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		if (isOnlyOneValue) {
			
#			if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
#				prePlot <- TRUE
#			} else {
#				prePlot <- FALSE
#			}
			#prePlot <- FALSE
			reorderList <- reorderThePlotOrder(overallResult, typOfPlot, checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, prePlot))
			overallResult <- reorderList$overallResult
			overallColor <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
			
			plot = ggplot(data = overallResult, aes_string(x = whichColumShouldUse, y = yValue))
		} else {
			plot = ggplot(data = overallResult, aes(x = xAxis, y = mean))
		}
		
		if (overallList$appendix) {
			ylabelForAppendix <- renameY(overallDesName[[imagesIndex]])
			plot <- plot + 
					ylab(ylabelForAppendix)
		} else {
			plot <- plot + 
					ylab(overallDesName[[imagesIndex]])
		}
		
		plot <- plot + 						
				geom_bar(stat = "identity", aes_string(fill = NAME), size = 0.1) ## colour = "Grey", 
		
		
		if (typOfPlot != STRESS.PLOT) {
			plot <- plot + 
					geom_errorbar(aes(ymax = mean+se, ymin = mean-se), width = 0.2, colour = "black")
		}
		#geom_errorbar(aes(ymax = mean+se, ymin = mean-se), width = 0.5, colour = "Pink")+
		#ylab(overallDesName[[imagesIndex]]) +
		
		if (length(grep("_start", overallFileName, ignore.case = TRUE)) > 0) {
			factor <- 10
			plot <- plot + 
					scallyAxis(factor, overallResult)
		} else if (length(grep("lm3s_", overallFileName, ignore.case = TRUE)) > 0) {
			factor <- 0.2
			plot <- plot + 
					scallyAxis(factor, overallResult)
		} else if (typOfPlot != STRESS.PLOT) {
			maxMean <- max(overallResult$mean)
			maxSe <- max(overallResult$se)
			
			plot <- plot +
					coord_cartesian(ylim = c(0, maxMean + maxSe+ (110*maxMean)/100)) 
		}				
		
		
		plot <- plot +	
				scale_fill_manual(values = overallColor)
		
#		if (isOnlyOneValue) {
#			if (typOfPlot == STRESS.PLOT) {
#				plot <- plot +	
#						scale_fill_manual(values = overallColor[reorderList$sortList])
#			} else {
#				plot <- plot +	
#						scale_fill_manual(values = overallColor[[imagesIndex]])
#			}
#		} else {
#			plot <- plot +	
#					scale_fill_manual(values = overallColor[[imagesIndex]][reorderList$sortList])
#		}
		
		plot <- plot +	
				theme_bw() +
				theme(legend.position = "none", 
						#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
						#axis.title.x = element_text(face = "bold", size = 11), 
						axis.title.y = element_text(face = "bold", size = 11, angle = 90), 
						axis.text.x = element_text(angle = 90), 
						panel.grid.minor = element_blank(), 
						panel.border = element_rect(colour = "Grey", size = 0.1)
				)
		
		if ((length(grep("-1", overallList$xAxisName, ignore.case = TRUE)) < 1) &&		#length(grep("2147483647", overallList$xAxisName, ignore.case = TRUE)) < 1 ||
				typOfPlot != STRESS.PLOT) {
			plot <- plot + 
					xlab(overallList$xAxisName) +
					theme(axis.title.x = element_text(face = "bold", size = 11))
		} else {
			plot <- plot +
					theme(axis.title.x = element_blank())
			
		}
		
		if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
				(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
				(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
			if (title != "") {
				titleText <- title
				if (length(unique(overallResult[, whichColumShouldUse])) == 1 && !(overallList$secondTreatment == NONE && !(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond))) {
					titleText <- paste(titleText, " - ", unique(overallResult[, whichColumShouldUse]), sep = "")
				}
				plot = plot + ggtitle(titleText)
			}
		}
		
		#	print(plot)
		plot <- facetWrap(plot, overallResult, typOfPlot, overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
#		if (typOfPlot == STRESS.PLOT) {
#			plot <- plot +
#					facet_wrap(~ stress)
#		}	
		#print(plot)
		subtitle <- ""
		overallImage <- TRUE
		subsectionDepth <- 1
		
		if (title != "") {
			sep <- " - "
		} else {
			sep <- ""
		}
		
		if (!overallList$appendix) {
#			if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) ||
#				(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) ||
#				(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
#				subtitle <- paste(cleanSubtitle(overallDesName[[imagesIndex]]), title, sep = sep)
#				
#				if ((length(grep("lm3s_", overallFileName, ignore.case = TRUE)) > 0)) {
#					subsectionDepth <- 5
#				} else {
#					subsectionDepth <- 2
#				}
#			}
		} else {
			subtitle <- paste(cleanSubtitle(ylabelForAppendix), title, sep = sep)
			
			subsectionDepth <- 1
			overallImage <- FALSE
		}
		
		if (typOfPlot == BOX.PLOT) {
			overallImageText <- "BarplotOI"										
		} else if (typOfPlot == STRESS.PLOT) {
			overallImageText <- "StressOI"	
		} else {
			overallImageText <- "OI"
		}
		
		#print(plot)
		writeTheData(overallList, plot, overallFileName, paste(title, typOfPlot, sep = ""), paste(overallFileName, typOfPlot, overallImageText, sep = ""), subtitle, overallImage, isAppendix = overallList$appendix, subsectionDepth = subsectionDepth, section = section)
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
	
	overallResult <- cbind(overallResult, group = rbind(-1))
	overallResultTemp <- overallResult
	
	for(n in as.character(unique(unlist(overallResultTemp$name)))) {
		
		#	ownList[1] <- n
		booleanVector = getBooleanVectorForFilterValues(overallResultTemp, list(name = n))
		overallResult <- overallResultTemp[booleanVector, ]
		
		lin_interp = function(x, y, length.out = length(overallResult$xAxis)) {
			approx(x, y, xout = seq(min(x), max(x), length.out = length.out))$y
		}
		
		overallResult$xAxis <- lin_interp(overallResult$xAxis, overallResult$xAxis)
		overallResult$mean <- lin_interp(overallResult$xAxis, overallResult$mean)
		
		ownCatRle = rle(overallResult$mean < 0)
		overallResult$group = rep.int(1:length(ownCatRle$lengths), times = ownCatRle$lengths)
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
	
	if (length(as.character(unique(overallResult$group))) > 1) {
		if (lastColorPositiv) {
			color <- c(color, "palegreen2")
		} else {
			color <- c(color, "light grey")
		}
	}
	
	return(color)
}

OneMinusTheValue <- function(tempOverallResult, overallDescriptor) {
	
	notOneMinus <- vector()
#	for(nn in names(overallDescriptor)) {
#		if (length(grep("nir", overallDescriptor[[nn]], ignore.case = TRUE)) > 0) {
#			notOneMinus <- c(notOneMinus, paste("mean", nn, sep = ""))
#		}
#	}
	colMinus <- tempOverallResult %allColnamesWithoutThisOnes% notOneMinus
	if (PRIMAER.TREATMENT %in% colnames(tempOverallResult)) {
		#overallResult[, 4:length(colnames(overallResult))] <- 1-overallResult[, 4:length(colnames(overallResult))]
		colMinus <- colMinus[-c(1:3)]
	} else {
		#overallResult[, 3:length(colnames(overallResult))] <- 1-overallResult[, 3:length(colnames(overallResult))]
		colMinus <- colMinus[-c(1:2)]
	}
	tempOverallResult[, colMinus] <- 1-tempOverallResult[, colMinus]
	return(tempOverallResult)
}

reorderThePlotOrder <- function(overallResult, typOfPlot, whichColumShouldUse = NAME) {
	groupedOverallResult <- data.table(overallResult)
	#sortString <- NAME
	
#	if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
#		sortString <- PRIMAER.TREATMENT
#	}
#print(head(groupedOverallResult))
#print(whichColumShouldUse)
	
	if (typOfPlot == STACKBOX.PLOT || typOfPlot == STRESS.PLOT) {
		sumVector <- as.data.frame(groupedOverallResult[, lapply(list(values), sum, na.rm = TRUE), by = c(whichColumShouldUse)])
	} else {
		sumVector <- as.data.frame(groupedOverallResult[, lapply(list(mean), mean, na.rm = TRUE), by = c(whichColumShouldUse)])
	}
	#sumVector$c <- levels(overallResult[[whichColumShouldUse]])
	sumVector$c <- as.character(unique(overallResult[[whichColumShouldUse]]))
	
	#print(head(sumVector))
	#print(levels(overallResult[[sortString]]))
#	print(sumVector[order(sumVector$V1), ]$c)
	
#	if (typOfPlot == VIOLIN.PLOT) {
#		for(n in levels(overallResult[[whichColumShouldUse]])) {
#			overallResult[[whichColumShouldUse]] <- replace(as.character(overallResult[[whichColumShouldUse]]), overallResult[[whichColumShouldUse]] == n, paste(sumVector[sumVector$c == n, ]$c, " (", round(sumVector[sumVector$c == n, ]$V1, digits = 1), ")", sep = ""))
#			sumVector[sumVector$c == n, ]$c <- paste(sumVector[sumVector$c == n, ]$c, " (", round(sumVector[sumVector$c == n, ]$V1, digits = 1), ")", sep = "")
#		}
#	}
	
	if (typOfPlot == VIOLIN.PLOT) {
		# n <- unique(overallResult[[whichColumShouldUse]])[1]
		for(n in unique(overallResult[[whichColumShouldUse]])) {
			overallResult[[whichColumShouldUse]] <- replace(as.character(overallResult[[whichColumShouldUse]]), overallResult[[whichColumShouldUse]] == n, paste(sumVector[sumVector$c == n, ]$c, " (", round(sumVector[sumVector$c == n, ]$V1, digits = 1), ")", sep = ""))
			sumVector[sumVector$c == n, ]$c <- paste(sumVector[sumVector$c == n, ]$c, " (", round(sumVector[sumVector$c == n, ]$V1, digits = 1), ")", sep = "")
		}
	}
	
	sortList <- order(sumVector$V1, decreasing = TRUE)
	overallResult[[whichColumShouldUse]] <- factor(overallResult[[whichColumShouldUse]], levels = sumVector[sortList, ]$c)
	#print(levels(overallResult[[whichColumShouldUse]]))
	#print(sumVector[sortList, ]$c)
	#print(head(sortList))
	
	return(list(overallResult = overallResult, sortList = sortList))
}

buildStressArea <- function(stressStart, stressEnd, stressTyp, stressLabel, yValues, typOfPlot, additionalValues = NONE, additionalDataFrameValues = NONE) {
	#############
#stressStart <- overallList$stressStart
#stressEnd <- overallList$stressEnd
#yValues <- overallResult$mean
#typOfPlot <- typOfPlot
	#############
#yValues <- overallResult$values
#additionalValues <- "stack"
#additionalDataFrameValues <- overallResult[, c("xAxis", "plot")]
#additionalDataFrameValues <- overallResult[, c("xAxis", "primaerTreatment")]
	############
	
	
	stress.Area <- data.frame()
	#possible.Stress.Values <- c("0", "1", "2", "3", "4") # c("n", "d", "w", "c", "s")
	standard.stressLabels <- list("000" = "normal", "001" = "drought stress", "002" = "moisture stress", "003" = "chemical stress", "004" = "salt stress")
	
	ymin <- min(yValues, na.rm = TRUE)
	ymax <- max(yValues, na.rm = TRUE)
	
	if (typOfPlot == VIOLIN.PLOT) {
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
				if (additionalDataFrameValues != NONE) {
					newDataFrame <- data.table(yValues, additionalDataFrameValues)
					newDataFrame <- as.data.frame(newDataFrame[, lapply(.SD, sum, na.rm = TRUE), by = colnames(additionalDataFrameValues)])
					ymin <- 0
					ymax <- max(newDataFrame$yValues, na.rm = TRUE)
				}
			} 			
		}
	} else if (typOfPlot == NBOX.PLOT) {
		if (additionalValues != NONE) {
			
			ymin <- ymin - max(additionalValues, na.rm = TRUE)
			ymax <- ymax + max(additionalValues, na.rm = TRUE)	
		}
	}
	
#	ymin <- (ymin - ymin * 0.01)
#	ymax <- (ymax + ymax * 0.01)
#	print(stressStart)
#	print(stressEnd)
#	print(stressTyp)
#	print(stressLabel)
	
	for (kk in seq(along = stressStart)) {
		if (stressStart[kk] != -1 && stressEnd[kk] != -1) {
			stressStart <- as.numeric(stressStart)
			stressEnd <- as.numeric(stressEnd)
			if (stressStart[kk] >= stressEnd[kk]) {
				xmin <- stressEnd[kk]
				xmax <- stressStart[kk]
			} else {
				xmin <- stressStart[kk]
				xmax <- stressEnd[kk]
			}
			
			if (!(stressTyp[kk] %in% names(standard.stressLabels))) {
				ownCat("... unknown stresstyp, change to \"normal\" -> (0)")
				stressTyp[kk] <- names(standard.stressLabels)[1]
			}
			
			if (stressLabel[kk] == -1) {
				ownCat("... no stresslabel are set, change to standard label for the stresstyp")
				stressLabel[kk] <- standard.stressLabels[[stressTyp[kk]]]
			}
			
			stress.Area <- rbind(stress.Area, data.frame(xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax, typ = stressTyp[kk], label = stressLabel[kk]))
		}
	}
	#dCNV_Biom[order(attr(dCNV_Bio, "Labels")), order(attr(dCNV_Bio, "Labels"))]
#	stress.Area <- sort(stress.Area)
	#print(stress.Area)
	return(stress.Area)
}

addColorForStressPhaseAndOther <- function(stressArea, color, typOfPlot = "none") {
	# c("000", "001", "002", "003", "004") entspricht c("n", "d", "w", "c", "s")
	
	if (typOfPlot == NBOX.PLOT || typOfPlot == "dodge") { # || typOfPlot == "stack") {
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

#plotViolinPlotDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title = "") {
makeViolinDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	########
#overallResult <- plotThisValues
#title <- ""
	########
	overallList$debug %debug% "makeViolinDiagram()"
	
	overallResult <- reownCategorized(overallResult)
	overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
	color <- setColorDependentOfGroup(overallResult)
	overallResult$name <- replaceTreatmentNames(overallList, overallResult$name, onlySecondTreatment = TRUE)
	section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
	reorderList <- reorderThePlotOrder(overallResult, typOfPlot)
	overallResult <- reorderList$overallResult
	
	#overallResult <- reorderThePlotOrder(overallResult, typOfPlot)
	stressArea <- data.frame()
	
	#print(head(overallResult))
	
# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")	
#	overallList$stressStart <- c(10, 20, 30, 37)
#	overallList$stressEnd <- c(13, 23, 33, 40)
#	overallList$stressTyp <- c("d", "d", "s", "c")
#	overallList$stressLabel <- c(-1, -1, -1, -1)
#	
#	overallList$stressStart <- c(10, 20, 30)
#	overallList$stressEnd <- c(13, 23, 33)
#	overallList$stressTyp <- c("2", "1", "2")
#	overallList$stressLabel <- c(-1, -1, -1)
	
	if (overallList$stressStart[1] != -1) {
		stressArea <- buildStressArea(overallList$stressStart, overallList$stressEnd, overallList$stressTyp, overallList$stressLabel, overallResult$mean, typOfPlot)
		color <- addColorForStressPhaseAndOther(stressArea, color)
	}
	
#	print(color)
#	print(stressArea)
	if (overallResult %checkRowLengthOfDataFrame% 0) {
		
		plot <- 	ggplot()				
		
		if (length(stressArea) >0) {
			plot <- plot + 
					geom_rect(data = stressArea, aes(xmin = xmin, xmax = xmax, ymin = ymin, ymax = ymax, fill = typ)) +
					geom_text(data = stressArea, aes(x = xmin, y = ymin, label = label), size = 2, hjust = 0, vjust = 1, angle = 90, colour = "grey")
			
		}	
		#print(plot)
		plot <- plot +
				geom_ribbon(data = overallResult, aes(x = xAxis, fill = mean >= 0, group = group, ymin = -mean, ymax = mean)) +						
				scale_fill_manual(values = color) +
				scale_x_continuous(name = overallList$xAxisName, minor_breaks = min(as.numeric(as.character(overallResult$xAxis))):max(as.numeric(as.character(overallResult$xAxis)))) + 
				scale_y_continuous(name = overallDesName[[imagesIndex]], minor_breaks = min(as.numeric(as.character(overallResult$mean))):max(as.numeric(as.character(overallResult$mean)))) +
				#ylab(overallDesName[[imagesIndex]]) +
				#label = c("0.4", "0.2", "0.0", "0.2", "0.4")
				#scale_y_discrete(aes(factor(c(0.4, 0.2, 0.0, 0.2, 0.4))), name = overallDesName[[imagesIndex]], limits = c(-10, 1)) +
				#scale_fill_manual(values = overallColor[[imagesIndex]]) +
				#scale_colour_manual(values = overallColor[[imagesIndex]]) +
				coord_flip()+
				theme_bw() +
				theme(axis.title.x = element_text(face = "bold", size = 11), 
						axis.title.y = element_text(face = "bold", size = 11, angle = 90), 
						#panel.grid.major = element_blank(), # switch off major gridlines
						#panel.grid.minor = element_blank(), # switch off minor gridlines
						legend.position = "none", # manually position the legend (numbers being from 0, 0 at bottom left of whole plot to 1, 1 at top right)
						#legend.title = element_blank(), # switch off the legend title						
						#legend.key.size = unit(1.5, "lines"), 
						#legend. key = element_blank(), # switch off the rectangle around symbols in the legend
						panel.border = element_rect(colour = "Grey", size = 0.1)
				)
		if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
				(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
				(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
			if (title != "") {
				plot = plot + ggtitle(title)
			}
		}
		
		if (length(unique(overallResult$name)) > 4) {
			plot = plot + theme(
					axis.text.x = element_text(size = 8), 
					strip.text.x = element_text(size = 7)
			)
		} else {
			plot = plot + theme(
					axis.text.x = element_text(size = 9), 
					strip.text.x = element_text(size = 10)
			)
		}
		
		plot <- facetWrap(plot, overallResult, typOfPlot)
#		plot <- plot + facet_wrap(~ name, ncol = 5) 
		
		#print(plot)
		
#		if (overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
#			subsectionDepth <- 3
#		} else {
#			subsectionDepth <- 2
#		}		
#		print(title)
#		print(overallDesName[[imagesIndex]])
		
		writeTheData(overallList, plot, overallFileName, paste(title, typOfPlot, sep = ""), paste(overallFileName, "violinOI", sep = ""), overallDesName[[imagesIndex]], TRUE, subsectionDepth = 2, section = section)
	}
}

getExtraPlot <- function(first, second, colNames) {
	
	if (first && !second || (first && second)) {
		columName <- getTheColumWhichShouldUse(colNames)
	} else if (!first && second) {
		columName <- NAME
	} else {
		columName <- NAME
	}			
	return(columName)
}

getColumnForColor <- function(first, second, colNames) {
	
	if (!first && second) {
		columName <- getTheColumWhichShouldUse(colNames)
	} else {
		columName <- NAME
	}			
	return(columName)
}


checkWhichColumShouldUseForPlot <- function(first, second, colNames, plotTyp = "", prePlot = FALSE) {
	#######
#first <- overallList$splitTreatmentFirst
#second <- overallList$splitTreatmentSecond
#colNames <- colnames(overallResult)
#plotTyp <- typOfPlot
#prePlot <- TRUE
	######
	
#	if (first && second) {
#		if ((plotTyp == BOX.PLOT || plotTyp == SPIDER.PLOT || plotTyp == LINERANGE.PLOT || plotTyp == BAR.PLOT || plotTyp == NBOX.PLOT)) {
#			columName <- NAME
#		} else {
#			columName <- PRIMAER.TREATMENT
#		}
#	} else 
	
	if (first && !second || (first && second) || (!first && second)) {
		if ((plotTyp == NBOX.PLOT || plotTyp == NBOX.MULTI.PLOT || plotTyp == BOX.PLOT || plotTyp == SPIDER.PLOT || plotTyp == LINERANGE.PLOT) && !prePlot) {
			columName <- NAME	
		} else {
			columName <- getTheColumWhichShouldUse(colNames)
#			if ("primaerTreatment" %in% colNames) {
#				columName <- "primaerTreatment"
#			} else {
#				columName <- "name"
#			}
		}
	} 
#	else if (!first && second) {
#		if ((plotTyp == NBOX.PLOT || plotTyp == BOX.PLOT || plotTyp == SPIDER.PLOT || plotTyp == LINERANGE.PLOT) && !prePlot) {
#			columName <- getTheColumWhichShouldUse(colNames)
	##			if ("primaerTreatment" %in% colNames) {
	##				columName <- "primaerTreatment"
	##			} else {
	##				columName <- "name"
	##			}
#		} else {
#			columName <- NAME
#		}
#	} 
	else {
		columName <- NAME
	}			
	return(columName)
}

#makeBoxplotDiagram <- function(overallResult, overallDescriptor, overallDesName, overallList, imagesIndex, typOfPlot, title) {
makeBoxplotDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	#############
#overallResult <- overallResultSplit
	##overallResultTemp <- overallResultSplitSecondTime
	##title <- paste(kk, nn, sep = " / ")
#title <- nn
	##title <- ""	
	############
	
	overallList$debug %debug% "makeBoxplotDiagram()"
	
	overallFileName <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
#	overallColor <- overallList$color_box[[imagesIndex]]
#	if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
#		overallColor <- overallColor[overallList$secondFilterTreatmentRename %in% title]
#	}
	
	options <- overallList$boxOptions
	section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
	overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
	#isOtherTyp <- checkIfShouldSplitAfterPrimaryAndSecondaryTreatment(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond)
	if (!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, TRUE)
	} else {
		whichColumShouldUse <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, FALSE)
	}
	
	#print(whichColumShouldUse)
	#if (!is.na(overallDescriptor[[imagesIndex]])) {
	#ownCat(paste("Process ", overallDesName[[imagesIndex]]))
	#createOuputOverview(BOX.PLOT, imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
	
	if (length(overallResult[, 1]) > 0) {
		xAxisfactor <- setxAxisfactor(overallList$xAxisName, overallResult[c("xAxis", VALUES)], VALUES, options, typOfPlot)	
		
		if (!is.null(xAxisfactor)) {
			overallResult$xAxisfactor <- xAxisfactor 	
			overallResult <- na.omit(overallResult)
			if (overallResult %checkRowLengthOfDataFrame% 0) {
				overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
				
				reorderList <- reorderThePlotOrder(overallResult, typOfPlot, whichColumShouldUse)
				overallResult <- reorderList$overallResult			
				#colorReorder <- overallColor[reorderList$sortList]
				colorReorder <- getVector(overallList$color[levels(overallResult[[whichColumShouldUse]])])
				
				
				#myPlot = ggplot(overallList$overallResult, aes(factor(name), value, fill = name, colour = name)) + 
				#myPlot = ggplot(overallResult, aes(factor(name), value, fill = name)) +
				
				
				#			if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
				#				overallResult <- cbind(overallResult, plot = rep.int(1, length(overallResult[, 1])))
				#				plot <- ggplot(overallResult, aes(plot, value, fill = plot))
				#			#} else if (overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
				#			} else {
				#print(head(overallResult))
				plot <- ggplot(overallResult, aes_string(x = whichColumShouldUse, y = VALUES, fill = whichColumShouldUse))
				#}
				
				
				#			else if (overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) {
				#				plot <- ggplot(overallResult, aes(factor(name), value, fill = name))
				#			} else if (!isOtherTyp) {
				#				if ("primaerTreatment" %in% colnames(overallResult)) {
				#					plot <- ggplot(overallResult, aes(factor(primaerTreatment), value, fill = primaerTreatment))
				#				} else {
				#					plot <- ggplot(overallResult, aes(factor(name), value, fill = name))
				#				}
				#			} else {
				#				plot <- ggplot(overallResult, aes(factor(name), value, fill = name))
				#			}
				
				
				#			if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
				#				overallResult <- cbind(overallResult, plot = rep.int(1, length(overallResult[, 1])))
				#				plot <- ggplot(overallResult, aes(plot, value, fill = plot))
				#			} else {
				#				if (isOtherTyp) {
				#					plot <- ggplot(overallResult, aes(factor(name), value, fill = name))
				#				} else {
				#					plot <- ggplot(overallResult, aes(factor(primaerTreatment), value, fill = primaerTreatment))
				#				}
				#			}
				
				if ((overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
						(!overallList$splitTreatmentFirst && overallList$splitTreatmentSecond) || 
						(overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {
					if (title != "") {
						plot = plot + ggtitle(title)
					}
				}
				plot <- plot +
						geom_boxplot() +
						ylab(overallDesName[[imagesIndex]]) +
						scale_fill_manual(values = colorReorder)
				#coord_cartesian(ylim = c(0, max(overallList$overallResult$mean + overallList$overallResult$se + 10, na.rm = TRUE))) +
				#xlab(paste(min(overallResult$xAxis), overallList$xAxisName, "..", max(overallResult$xAxis), overallList$xAxisName)) +
				#print(plot)		
				#			if (!(!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) {	
				#				plot = plot +
				#						scale_fill_manual(values = overallColor[[imagesIndex]])
				#			}
				#stat_summary(fun.data = f, geom = "crossbar", height = 0.1, 	colour = NA, fill = "skyblue", width = 0.8, alpha = 0.5) +
				plot <- plot +			
						theme_bw() +
						theme(legend.position = "none", 
								#plot.margin = unit(c(0.1, 0.1, 0, 0), "cm"), 
								axis.title.x = element_blank(), 
								axis.title.y = element_text(face = "bold", size = 11, angle = 90), 
								panel.grid.minor = element_blank(), 
								panel.border = element_rect(colour = "Grey", size = 0.1)
						
						)
				#				if (overallList$splitTreatmentFirst && overallList$secondTreatment == "none") {
				#					plot = plot +
				#							theme(axis.text.x = element_blank())
				#				} else {
				plot <- setFontSize(plot, colorReorder, typOfPlot)
#						plot = plot + 
#								theme(axis.text.x = element_text(size = 11, angle = 90))	#size = 5
				#}
				#				if (length(unique(overallResult$xAxisfactor)) > 1) {
				#					plot = plot +	
				#							facet_wrap(~ xAxisfactor, drop = TRUE)
				#				}
				plot <- facetWrap(plot, overallResult, typOfPlot, first = overallList$splitTreatmentFirst, second = overallList$splitTreatmentSecond, whichColumShouldUse = whichColumShouldUse)	
				
				#print(plot)
				
				subsectionDepth <- 1
				subtitle <- ""
#					subtitle <- title
#					
#					if (!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond) {
#						subsectionDepth <- 1
#					} else {
#						subsectionDepth <- 2
#					}		
				
				writeTheData(overallList, plot, overallFileName, paste(title, typOfPlot, sep = ""), paste(overallFileName, typOfPlot, "OI", sep = ""), subtitle, TRUE, subsectionDepth = subsectionDepth, section = section)
				
				#				saveImageFile(overallList, plot, overallFileName[[imagesIndex]], typOfPlot)
				#
				#				if (overallList$appendix) {
				#					writeLatexFile("appendixImage", overallFileName[[imagesIndex]], typOfPlot)
				#				}
			}
		}
	}
	#}
}

facetWrap <- function(plot, overallResult, typOfPlot, first = FALSE, second = FALSE, whichColumShouldUse = NULL) {
	
	if (typOfPlot == BOX.PLOT) {
#		if ((!first && second)) {
#			plot <- facetWrapXFactorAndNameOrPri(plot, overallResult, whichColumShouldUse)
#		} else {		
		plot <- facetWarapOnlyXFactor(plot, overallResult)
#		}
	} else if (typOfPlot == NBOX.PLOT) {
		if ((first && !second) || (!first && second)) {		
			plot <- facetWrapPriAndName(plot, overallResult)
		} else if (first && second) {
			plot <- facetWarapOnlyName(plot, overallResult)
		}	
		
	} else if (typOfPlot == NBOX.MULTI.PLOT) {
		if ((first && !second) || (!first && second)) {		
			plot <- facetWrapPriAndNameAndCol(plot, overallResult)
		} else if (first && second) {
			plot <- facetWarapOnlyNameAndCol(plot, overallResult)
		} else {
			plot <- facetWarapOnlyCol(plot, overallResult)
		}	
		
	} else if (typOfPlot == STACKBOX.PLOT) {
		if (!first && second) {		
			plot <- facetWrapPriAndName(plot, overallResult)
			#plot <- facetWarapOnlyName(plot, overallResult)
		} else {
			plot <- facetWarapOnlyName(plot, overallResult)
		}
	} else if (typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
		if ((first && second) || (!first && second)) {
			plot <- facetWrapXFactorAndNameOrPri(plot, overallResult, whichColumShouldUse)
#			if (whichColumShouldUse == NAME) {
#				plot <- plot + facet_grid(name ~ xAxisfactor)
#			} else {
#				plot <- plot + facet_grid(primaerTreatment ~ xAxisfactor)
#			}
			
		} else {
			plot <- facetWarapOnlyXFactor(plot, overallResult)
			#plot = plot + facet_grid(~ xAxisfactor)
		}
	} else if (typOfPlot == VIOLIN.PLOT) {
		plot <- facetWarapOnlyName(plot, overallResult, 5)
		#plot <- plot + facet_wrap(~ name, ncol = 5) 
	} else if (typOfPlot == STRESS.PLOT) {
		if (length(unique(overallResult$stress)) > 1) {
			plot <- plot +	
					facet_wrap(~ stress)
		}	
	}
	return(plot)
}


facetWrapPriAndNameAndCol <- function(plot, overallResult) {
	
	if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
		if (length(unique(overallResult$primaerTreatment)) == 1) {
			plot <- facetWarapOnlyCol(plot, overallResult)
		} else {
			plot <- plot + 
					facet_grid(column ~ primaerTreatment, drop = FALSE)
		}
	} else {
		if (length(unique(overallResult$name)) == 1) {
			plot <- facetWarapOnlyCol(plot, overallResult)
		} else {
			plot <- plot + 
					facet_grid(column ~ name, drop = FALSE)
		}
	}
	
	return(plot)
}


facetWrapPriAndName <- function(plot, overallResult) {
	
	if (PRIMAER.TREATMENT %in% colnames(overallResult)) {
		if (length(unique(overallResult$primaerTreatment)) > 1) {
			plot <- plot + 
					facet_wrap(~ primaerTreatment, drop = FALSE)
		}
	} else {
		if (length(unique(overallResult$name)) > 1) {
			plot <- plot + 
					facet_wrap(~ name, drop = FALSE)
		}
	}
	
	return(plot)
}

facetWarapOnlyNameAndCol <- function(plot, overallResult, ncol = NULL) {
	if (length(unique(overallResult$name)) > 1) {
		plot <- plot + 
				facet_wrap(column ~ name, ncol = ncol, drop = FALSE)	
	}
	return(plot)
}

facetWarapOnlyCol <- function(plot, overallResult) {
	if (length(unique(overallResult$column)) > 1) {
		plot <- plot + 
				facet_wrap(~ column, drop = FALSE)	
	}
	return(plot)
}


facetWarapOnlyName <- function(plot, overallResult, ncol = NULL) {
	if (length(unique(overallResult$name)) > 1) {
		plot <- plot + 
				facet_wrap(~ name, ncol = ncol, drop = FALSE)	
	}
	return(plot)
}

facetWarapOnlyXFactor <- function(plot, overallResult) {
	if (length(unique(overallResult$xAxisfactor)) > 1) {
		plot <- plot +	
				facet_wrap(~ xAxisfactor, drop = FALSE)
	}
	return(plot)
} 

facetWrapXFactorAndNameOrPri <- function(plot, overallResult, whichColumShouldUse) {	
	if (whichColumShouldUse == NAME) {
		if (length(unique(overallResult$name)) > 1 && length(unique(overallResult$xAxisfactor)) > 1) {
			plot <- plot + facet_grid(name ~ xAxisfactor)
		}
	} else {
		if (length(unique(overallResult$primaerTreatment)) > 1 && length(unique(overallResult$xAxisfactor)) > 1) {
			plot <- plot + facet_grid(primaerTreatment ~ xAxisfactor)
		}
	}
	return(plot)
}


plotDiagram <- function(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title) {
	#########
#title <- ""
#overallResult <- overallResultSplit
#title	 <- nn
	#########
	
	overallList$debug %debug% "plotDiagram()"	
	
	if (typOfPlot == NBOX.PLOT || typOfPlot == NBOX.MULTI.PLOT) {
		makeLinearDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} 
#	else if (typOfPlot == NBOX.MULTI.PLOT) {
#		makeLinearDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
#	} 
	else if (typOfPlot == BOX.PLOT) {
		makeBoxplotDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if (typOfPlot == STACKBOX.PLOT) {
		makeStackedDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if (typOfPlot == SPIDER.PLOT) {
		makeSpiderDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if (typOfPlot == LINERANGE.PLOT) {
		makeLinerangeDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if (typOfPlot == VIOLIN.PLOT) {
		makeViolinDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if (typOfPlot == BAR.PLOT) {
		makeBarDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
	} else if (typOfPlot == STRESS.PLOT) {
		makeStressDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title)
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
	
	if ((!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond && !(typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT)) || 
			(overallList$splitTreatmentFirst && overallList$secondTreatment == NONE && !(typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT)) || 
			(typOfPlot == VIOLIN.PLOT) ||
			(!overallList$splitTreatmentFirst && !overallList$splitTreatmentSecond)) { #&& (typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT)) 
		plotDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot, title = "")	
	} else {
		#nn <- unique(as.character(overallResult[[extraPlot]]))[1]
		##extraPlot <- checkWhichColumShouldUseForPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult), typOfPlot, TRUE)
		extraPlot <- getExtraPlot(overallList$splitTreatmentFirst, overallList$splitTreatmentSecond, colnames(overallResult))
		for(nn in unique(as.character(overallResult[[extraPlot]]))) {
			overallList$debug %debug% paste("... subplot: ", nn, sep = "") 
			optionListForGetBoolean <- list(value = nn)
			names(optionListForGetBoolean) <- extraPlot
			booleanVector <- getBooleanVectorForFilterValues(overallResult, optionListForGetBoolean)
			overallResultSplit <- overallResult[booleanVector, ]
			
			if (length(unique(as.character(overallResult[[extraPlot]]))) > 0) {
				nn <- replaceTreatmentNamesOverallOneValue(overallList, nn, typOfPlot)
			} else {
				nn <- ""
			}
			
			plotDiagram(overallResultSplit, overallDesName, overallList, imagesIndex, typOfPlot, nn)
		}
	}
}

startDiagramming <- function(overallList, tempOverallResult, overallDescriptor, overallDesName, typOfPlot, catch = FALSE, debug = FALSE) {
	debug %debug% "startDiagramming()"
	
	if (DO.PARALLELISATION) {
		sfClusterEval(paralleliseDiagramming(overallList, tempOverallResult, overallDescriptor, overallDesName, typOfPlot, catch = catch, debug = debug), 
				stopOnError = FALSE)
	} else {
		paralleliseDiagramming(overallList, tempOverallResult, overallDescriptor, overallDesName, typOfPlot, catch = catch, debug = debug)
	}	
}


paralleliseDiagramming <- function(overallList, tempOverallResult, overallDescriptor, overallDesName, typOfPlot, catch = FALSE, debug = FALSE) {
	###############
#tempOverallResult <- overallList$overallResult_nBoxDes 
#overallDescriptor <- overallList$nBoxDes 
#overallDesName <- overallList$nBoxDesName
#typOfPlot <- NBOX.PLOT	
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_nBoxMultiDes 
#overallDescriptor <- overallList$nBoxMultiDes 
#overallDesName <- overallList$nBoxMultiDesName
#typOfPlot <- NBOX.MULTI.PLOT	
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_boxSpiderDes 
#overallDescriptor <- overallList$boxSpiderDes 
#overallDesName <- overallList$boxSpiderDesName 
#typOfPlot <- SPIDER.PLOT
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_violinBoxDes
#overallDescriptor <- overallList$violinBoxDes
#overallDesName <- overallList$violinBoxDesName
#typOfPlot <- VIOLIN.PLOT
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_boxDes
#overallDescriptor <- overallList$boxDes
#overallDesName <- overallList$boxDesName
#typOfPlot <- BOX.PLOT
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_boxStackDes
#overallDescriptor <- overallList$boxStackDes
#overallDesName <- overallList$boxStackDesName
#typOfPlot <- STACKBOX.PLOT
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_linerangeDes 
#overallDescriptor <- overallList$linerangeDes 
#overallDesName <- overallList$linerangeDesName 
#typOfPlot <- LINERANGE.PLOT
#imagesIndex <- "1"
	###############
#tempOverallResult <- overallList$overallResult_stressDes 
#overallDescriptor <- overallList$stressDes 
#overallDesName <- overallList$stressDesName 
#typOfPlot <- STRESS.PLOT
#imagesIndex <- "1"
	###############
	
	debug %debug% "paralleliseDiagramming()"
	index <- 0;
	maxIndex <- sum(!is.na(overallDescriptor))
#	if (typOfPlot == BOX.PLOT || typOfPlot == STACKBOX.PLOT || typOfPlot == SPIDER.PLOT) {
#		tempOverallResult <- na.omit(tempOverallResult)
#	} else 
	
	if (typOfPlot == VIOLIN.PLOT) {
		tempOverallResult <- OneMinusTheValue(tempOverallResult, overallDescriptor)
	}
	
	for (imagesIndex in names(overallDescriptor)) {
		if (all(!is.na(overallDescriptor[[imagesIndex]]))) {
			#if (!is.na(overallDescriptor[[imagesIndex]][1])) {
			plot <- TRUE
			if (DO.MODELLING.OF.STRESS && length(descriptorStressVector) > 0) {
				if (!(overallDescriptor[[imagesIndex]] %in% descriptorStressVector)) {
					plot <- FALSE	
				}
			}
			if (plot) {
				index <- index + 1
#				createOuputOverview(typOfPlot, imagesIndex, length(names(overallDescriptor)), overallDesName[[imagesIndex]])
				createOuputOverview(typOfPlot, index, maxIndex, overallDesName[[imagesIndex]])
				overallResult = reduceWholeOverallResultToOneValue(tempOverallResult, imagesIndex, debug, typOfPlot)
				
				if (typOfPlot == BOX.PLOT || typOfPlot == STACKBOX.PLOT || typOfPlot == SPIDER.PLOT || typOfPlot == LINERANGE.PLOT) {
					overallResult <- na.omit(overallResult)
				} else if (typOfPlot == NBOX.PLOT || typOfPlot == BAR.PLOT || typOfPlot == NBOX.MULTI.PLOT) {
					overallResult = overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
					overallResult[is.na(overallResult)] = 0 #second if there are values where the se are NA (because only one Value are there) -> the se are set to 0	
					#overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)
				} else if (typOfPlot == VIOLIN.PLOT) {
					overallResult <- overallResult[!is.na(overallResult$mean), ]	#first all values where "mean" != NA are taken
				} else if (typOfPlot == STRESS.PLOT) {
					#overallResult <- replaceTreatmentNamesOverall(overallList, overallResult)	
				}
				
				if (overallResult %checkRowLengthOfDataFrame% 0) {
					if ("2147483647" %in% overallResult[, X.AXIS]) {
						if (length(unique(overallResult[, X.AXIS])) > 1) {
							overallResult[overallResult[, X.AXIS] == 2147483647, X.AXIS] <- sort(unique(overallResult[, X.AXIS]), decreasing = TRUE)[2]+1
						} else {
							overallResult[overallResult[, X.AXIS] == 2147483647, X.AXIS] <- -1
						}
					}
					
					if (catch) {
						if (INNER.THREADED && DO.PARALLELISATION) {
							
							error <- try(sfClusterCall(makeSplitDiagram, overallResult, overallDesName, overallList, imagesIndex, typOfPlot, 
											stopOnError = FALSE), silent = !debug)
						} else {
							error <- try(makeSplitDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot), silent = !debug)
						}
						checkOfTryError(error, overallList, imagesIndex, typOfPlot)
					} else {
						if (INNER.THREADED && DO.PARALLELISATION) {
							
							sfClusterCall(makeSplitDiagram, overallResult, overallDesName, overallList, imagesIndex, typOfPlot, 
									stopOnError = FALSE)
						} else {
							makeSplitDiagram(overallResult, overallDesName, overallList, imagesIndex, typOfPlot)
						}
					}
				}
			}
		}
	}
	
#	if (!(typOfPlot == NBOX.PLOT && !overallList$deleteNboxplot)) {
#		overallList <- reduceOverallListForMemorySave(overallList, typOfPlot)
#		gc()
#	}
}


reduceOverallListForMemorySave <- function(overallList, typOfPlot) {
	overallList[[getOverallValuesPerType(typOfPlot, GET.OVERALL.RESULT.RESET)]] <- NULL
	return(overallList)
}


getOverallValuesPerType <- function(typOfPlot, typOfValues) {
	if (typOfPlot == NBOX.PLOT) {
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_nBoxplots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("nBoxSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_nBoxDes")
		}
	} else if (typOfPlot == NBOX.MULTI.PLOT) {
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_nBoxMultiPlots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("nBoxMultiSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_nBoxMultiDes")
		}
	} else if (typOfPlot == STRESS.PLOT) {
		if (typOfValues == GET.SECTION.VALUE) {
			return(STRESS)
		}	
	} else if (typOfPlot == BAR.PLOT) {	
		#empty	
	} else if (typOfPlot == BOX.PLOT) {	
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_Boxplots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("boxSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_boxDes")
		}
	} else if (typOfPlot == STACKBOX.PLOT) {	
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_StackedPlots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("boxStackSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_boxStackDes")
		}
	} else if (typOfPlot == SPIDER.PLOT) {
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_SpiderPlots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("boxSpiderSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_boxSpiderDes")
		}		
	} else if (typOfPlot == LINERANGE.PLOT) {
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_LinerangePlots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("linerangeSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_linerangeDes")
		}		
	} else if (typOfPlot == VIOLIN.PLOT) {
		if (typOfValues == GET.OVERALL.FILE.NAME) {
			return("imageFileNames_violinPlots")
		} else if (typOfValues == GET.SECTION.VALUE) {
			return("violinBoxSection")
		} else if (typOfValues == GET.OVERALL.RESULT.RESET) {
			return("overallResult_violinBoxDes")
		}		
	}
}


getOverallValues <- function(overallList, typOfPlot, typOfValues, imagesIndex = -1) {
	overallList$debug %debug% "getOverallValues()"
	
	if (imagesIndex == -1 || typOfValues == GET.SECTION.VALUE) {
		return(overallList[[getOverallValuesPerType(typOfPlot, typOfValues)]])
	} else {
		return(overallList[[getOverallValuesPerType(typOfPlot, typOfValues)]][[imagesIndex]])
	}
}

checkOfTryError <- function(error, overallList = NULL, imagesIndex = NULL, typOfPlot = NULL, typ = NULL) {
	if (!is.null(overallList)) {
		overallList$debug %debug% "checkOfTryError()"
	}
	
	if (isTRUE(all.equal(class(error), "try-error"))) {
		if (!(is.null(overallList) || is.null(imagesIndex) || is.null(typOfPlot))) {		
			preFilename <- getOverallValues(overallList, typOfPlot, GET.OVERALL.FILE.NAME, imagesIndex)
			filename <- checkFileName(preFilename, typOfPlot)
			filenamePlot <- getPlotFileName(filename)
			createErrorPlot(filenamePlot, filename)
			
			section <- buildSectionString(getOverallValues(overallList, typOfPlot, GET.SECTION.VALUE), imagesIndex, overallList$appendix)
			writeLatexFile(paste(preFilename, typOfPlot, "OI", sep = ""), filename, saveFormatImage = overallList$saveFormat, section = section)
			
			text <- paste("Error in file: ", filenamePlot, NEWLINE.TEX.INCLUDE, 
					"Error-Message: ", NEWLINE.TEX.INCLUDE, 
					geterrmessage(), NEWLINE.TEX.INCLUDE, 
					"ooooooooooooooooooooooooooo", NEWLINE.TEX.INCLUDE, NEWLINE.TEX.INCLUDE, sep = "")
			text <- parseString2Latex(text)	
			writeLatexFile(ERROR, saveFormatImage = overallList$saveFormat, section = ERROR)
			#errorFile <- paste(ERROR, SECTION.TEX, ERROR, sep = "")
			
			writeOnlyALatexFile(text, getPlotFileName(ERROR), TRUE)
		} else if (!is.null(typ) && typ == LIB) {
			return(TRUE)
		} else {
			errorText <- geterrmessage()
			ckeckIfReportTexIsThere(errorText)		############# DEBUG
			write(x = "No output! Diagram creation and report generation needs to stop. Please check the console output and error logs for error details.", append = TRUE, file = ERROR.TOTAL.FILE)
			write(x = c("Error Message: ", errorText), append = TRUE, file = ERROR.TOTAL.FILE)
		}
	}
	
	if (!is.null(typ) && typ == LIB) {
		return(FALSE)
	}
}


getStandardColnames <- function(tempOverallResult) {
	if (PRIMAER.TREATMENT %in% colnames(tempOverallResult)) {
		return(c(NAME, PRIMAER.TREATMENT, X.AXIS))
	} else {
		return(c(NAME, X.AXIS))
	}
}

#####
# possible Columns in overallResult(for NBOX.PLOT <- linear Diagrams) :
# "name": either a combination from the primär Treatment and the second one or only the primär Treamtent (only if no secondary treatment was selected)
# "primaerTreatment": the colum is only there when a second treatment was selected and represent than the primär Treatment
# "xAxis": all the days that are available
# "mean": average value on the descriptor
# "se": standard deviation
######
calculateSomeStressForTheGivenTyp <- function(overallResult, descriptorName) {
	plantName <- overallResult[, NAME]
	days <- overallResult[, X.AXIS]
	valuesMean <- overallResult[, "mean"]
	valuesSd <- overallResult[, "se"]
	
	stressName <- vector()
	for(nn in 1:5) {
		stressName <- c(stressName, paste("stress", nn, sep = ""))
	}	
	
	stress1 <- mean(valuesMean, na.rm = TRUE)
	stress2 <- max(valuesMean, na.rm = TRUE)
	stress3 <- min(valuesMean, na.rm = TRUE)
	stress4 <- sd(valuesMean, na.rm = TRUE)
	stress5 <- sum(valuesMean, na.rm = TRUE)
	
	#stressMatrix <- matrix(c(stress1, stress2, stress3, stress4, stress5), dimnames = list(stressName, "value"))
	stressVector <- c(stress1, stress2, stress3, stress4, stress5)
	names(stressVector) <- stressName
	
	return(stressVector)	
}

#############
# How you can access the stress values
# overallList$stressMatrixList <- this is the whole list of the stress values
# overallList$stressMatrixList$genevalue1 <- here you get all values for all descriptors for the genotype "genevalue1" the same access is: overallList$stressMatrixList[["genevalue1"]]
# overallList$stressMatrixList$genevalue1$descriptor1 <- here you get a matrix with all stress values of the genotype "genevalue1" and the descriptor "descriptor1"
# overallList$stressMatrixList$genevalue1$descriptor1[1, 1] <- here you get the first stress value
# overallList$stressMatrixList$genevalue1$descriptor1[2, 1] <- the second ...
###########
calculateStressValues <- function(overallList) {
	overallList$debug %debug% "calculateStressValues()"
	
	if (DO.MODELLING.OF.STRESS) {
		workingData <- overallList$overallResult_nBoxDes	
		uniqueValues <- unique(as.character(workingData[, NAME]))
		desVec <- na.omit(unlist(overallList$nBoxDes))
		
		#stressResultMatrix <- matrix(-1, nrow = length(desVec), ncol = length(uniqueValues), dimnames = list(names(desVec), uniqueValues))
		##stressMatrixList <- list()
		numberOfStressValues <- 5
		numberOfColumns <- 2 + numberOfStressValues
		stressResultMatrix <- matrix(-1, nrow = length(uniqueValues) * length(desVec), ncol = numberOfColumns)
		colnames(stressResultMatrix) <- c("name", "descriptor", paste("stress", seq(1:5), sep = ""))
		
		index <- 1
		for(pp in uniqueValues) {
			newWorkingDataSet <- workingData[workingData[, NAME] == pp, ]
			for (descriptorIndex in names(desVec)) {	
				overallResult <- reduceWholeOverallResultToOneValue(newWorkingDataSet, descriptorIndex, overallList$debug, NBOX.PLOT)
				
				stressResultMatrix[index, ] <- c(pp, descriptorIndex, calculateSomeStressForTheGivenTyp(overallResult, as.character(overallList$nBoxDes[[descriptorIndex]])))
				#overallList$nBoxDesName[[descriptorIndex]]
				
				
				#stressResultMatrix[descriptorIndex, nn] <- calculateSomeStressForTheGivenTyp(overallResult)
				##stressMatrixList[[nn]][[overallList$nBoxDesName[[descriptorIndex]]]] <- calculateSomeStressForTheGivenTyp(overallResult)
				index <- index +1
			}
		}
		#rownames(stressResultMatrix) <- getVector(overallList$nBoxDesName[names(desVec)])
		overallList$overallResult_stressDes <- as.data.frame(stressResultMatrix)
		overallList$stressDes <- overallList$nBoxDes 
		overallList$stressDesName <- overallList$nBoxDesName
		overallList$stressOptions <- NULL
		
		#tempOverallResult <- overallList$overallResult_nBoxDes 
		#overallDescriptor <- overallList$nBoxDes 
		#overallDesName <- overallList$nBoxDesName
		#typOfPlot <- NBOX.PLOT	
	}
	
	return(overallList)
}


makeDiagrams <- function(overallList) {
	overallList$debug %debug% "makeDiagrams()"
	
	if (SHOULD.THREADED && DO.PARALLELISATION) {
		sfExport("overallList")
	}
	if (!calculateNothing) {
		
		if (!plotOnlyViolin) {	
			if (!plotOnlySpider) {
				if (!plotOnlyLineRange) {
					if (!plotOnlyStacked) {
						if (!plotOnlyBoxplot) {
							if (!plotOnlyNBoxMulti) {
								if (!plotOnlyNBox) {
									
									if (DO.MODELLING.OF.STRESS && sum(!is.na(overallList$stressDes)) > 0) {
										if (overallList$debug) {ownCat("stress modelling...")}
										startDiagramming(overallList, overallList$overallResult_stressDes, overallList$stressDes, overallList$stressDesName, STRESS.PLOT, catch = overallList$catch, debug = overallList$debug)				
									} else {
										ownCat("All values for stress modelling are 'NA'")
									}
								}
								if (!plotOnlyStressValues) {
									if (sum(!is.na(overallList$nBoxDes)) > 0) {
										if (overallList$debug) {ownCat("nBoxplot...")}
										startDiagramming(overallList, overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$nBoxDesName, NBOX.PLOT, catch = overallList$catch, debug = overallList$debug)
									} else {
										ownCat("All values for nBoxplot are 'NA'")
									}
								}}
							if (!plotOnlyNBox) {
								if (sum(!is.na(overallList$nBoxMultiDes)) > 0) {
									if (overallList$debug) {ownCat("nBoxMultiPlot...")}
									startDiagramming(overallList, overallList$overallResult_nBoxMultiDes, overallList$nBoxMultiDes, overallList$nBoxMultiDesName, NBOX.MULTI.PLOT, catch = overallList$catch, debug = overallList$debug)
								} else {
									ownCat("All values for nBoxMultiPlot are 'NA'")
								}
							}}
						
						if (!plotOnlyStressValues) {
							if (!plotOnlyNBox) {
								if (!plotOnlyNBoxMulti) {
									if (sum(!is.na(overallList$boxDes)) > 0) {
										if (overallList$debug) {ownCat("Boxplot...")}						
										startDiagramming(overallList, overallList$overallResult_boxDes, overallList$boxDes, overallList$boxDesName, BOX.PLOT, catch = overallList$catch, debug = overallList$debug)		
									} else {
										ownCat("All values for Boxplot are 'NA'...")
									}
								}}}}
					
					if (!plotOnlyStressValues) {
						if (!plotOnlyNBox) {
							if (!plotOnlyNBoxMulti) {
								if (!plotOnlyBoxplot) {
									if (sum(!is.na(overallList$boxStackDes)) > 0) {
										if (overallList$debug) {ownCat("Stacked Boxplot...")}
										startDiagramming(overallList, overallList$overallResult_boxStackDes, overallList$boxStackDes, overallList$boxStackDesName, STACKBOX.PLOT, catch = overallList$catch, debug = overallList$debug)
									} else {
										ownCat("All values for stacked Boxplot are 'NA'...")
									}
								}}}}}}
			
			if (!plotOnlyStressValues) {
				if (!plotOnlyStacked) {
					if (!plotOnlyBoxplot) {
						if (!plotOnlyNBox) {
							if (!plotOnlyNBoxMulti) {
								if (!plotOnlyLineRange) {
									if (sum(!is.na(overallList$boxSpiderDes)) > 0) {
										if (overallList$debug) {ownCat("Spider plot...")}
										startDiagramming(overallList, overallList$overallResult_boxSpiderDes, overallList$boxSpiderDes, overallList$boxSpiderDesName, SPIDER.PLOT, catch = overallList$catch, debug = overallList$debug)
									} else {
										ownCat("All values for spider plot are 'NA'...")
									}
								}
								if (sum(!is.na(overallList$linerangeDes)) > 0) {
									if (overallList$debug) {ownCat("Linerange plot...")}
									startDiagramming(overallList, overallList$overallResult_linerangeDes, overallList$linerangeDes, overallList$linerangeDesName, LINERANGE.PLOT, catch = overallList$catch, debug = overallList$debug)
								} else {
									ownCat("All values for linerange plot are 'NA'...")
								}
							}}}}}}
		
		if (!plotOnlyStressValues) {
			if (!plotOnlyStacked) {
				if (!plotOnlyBoxplot) {
					if (!plotOnlyNBox) {
						if (!plotOnlyNBoxMulti) {
							if (!plotOnlySpider) {
								if (!plotOnlyLineRange) {
									if (sum(!is.na(overallList$violinBoxDes)) > 0 & overallList$isRatio) {
										if (overallList$debug) {ownCat("Violin plot...")}
										startDiagramming(overallList, overallList$overallResult_violinBoxDes, overallList$violinBoxDes, overallList$violinBoxDesName, VIOLIN.PLOT, catch = overallList$catch, debug = overallList$debug)
									} else {
										ownCat("All values for violin Boxplot are 'NA'...")
									}
								}}}}}}}
		if (FALSE) {	# falls auch mal barplots erstellt werden sollen (ausser wenn nur ein Tag vorhanden ist!)
			if (overallList$debug) {ownCat("Barplot...")}
			startDiagramming(overallList, overallList$overallResult_nBoxDes, overallList$nBoxDes, overallList$nBoxDesName, BAR.PLOT, catch = overallList$catch, debug = overallList$debug)
		}
	}
}

addDesSetLineSec <- function(descriptorSet, descriptorSetName, workingDataSet, descriptorSet_section, debug = FALSE) {
	debug %debug% "addDesSetLineSec()"
	
	for(nn in seq(along = descriptorSet)) {
		if (str_detect(descriptorSet[nn], fixed("#"))) {
			newDesAll <- character()
			split <- unlist(str_split(descriptorSet[nn], fixed("#")))
			for(kk in seq(1:5)) {
				newDes <- paste(split[1], "_", kk, "_", "5.", split[2], sep = "")
				if (kk == 1) {
					sep <- ""
				} else {
					sep <- "$"
				}
				newDesAll <- paste(newDesAll, newDes, sep = sep)
			}			
			descriptorSet[nn] <- newDesAll
		}
	}
	
	return(list(desSet = descriptorSet, desName = descriptorSetName, desSec = descriptorSet_section))
}




addDesSet <- function(descriptorSet_boxplotStacked, descriptorSetName_boxplotStacked, workingDataSet, descriptorSet_section = NULL, debug = FALSE) {
	debug %debug% "addDesSet()"
		
	addDescSet <- character()
	addDescSetNames <- character()
	addDesSection <- list()
	i <- 0
	for (ds in descriptorSet_boxplotStacked) {
		addCol <- ""
		for (col in colnames(workingDataSet)) {	
			if (nchar(ds)>5) {
				last4chars = substr(col, nchar(ds)-4, nchar(ds))
				if (last4chars == ".bin.") {
					col_substring = substr(col, 1, nchar(ds))
					if (col_substring == ds) {
						if (nchar(addCol)>0) {
							addCol = paste(addCol, "$", sep = "")
						}
						addCol <- paste(addCol, col, sep = "")
					} 
				}
			}
		}
		i <- i+1
		if (nchar(addCol)>0) {
			if (!is.null(descriptorSet_section)) {
				addDesSection <- list(section = c(addDesSection$section, descriptorSet_section$section[i]), 
						subsection = c(addDesSection$subsection, descriptorSet_section$subsection[i]), 
						subsubsection = c(addDesSection$subsubsection, descriptorSet_section$subsubsection[i]), 
						paragraph = c(addDesSection$paragraph, descriptorSet_section$paragraph[i]))
			}
			
			addDescSet <- c(addDescSet, addCol)
			addDescSetNames <- c(addDescSetNames, descriptorSetName_boxplotStacked[i])
		}
	}
	
	if (length(addDescSet) > 0) {
		return(list(desSet = addDescSet, desName = addDescSetNames, desSec = addDesSection))
	} else {
		return(list(desSet = descriptorSet_boxplotStacked, desName = descriptorSetName_boxplotStacked, desSec = addDesSection))
	}
}

isOwnNull <- function(value) {
	return(getVector(lapply(value, is.null)))	
}

replaceNullWithRealNull <- function(value) {
	value[isOwnNull(value)] <- 0
	return(value)
}

replaceNullWithRealNullAndSetName <- function(value, names) {
	value <- replaceNullWithRealNull(value)
	names(value) <- names
	return(value)
}


changeSectionToRealSection <- function(section, sectionMappingList, lengthAppendix = 0, debug = FALSE) {
	debug %debug% "changeSectionToRealSection()"
	
	if (!is.null(section)) {
		for(con in seq(along = section)) {
			if (con != 1) {
				if (length(section[[1]]) != length(section[[con]])) {
					section[[con]] <- c(section[[con]], rep.int(0, length(section[[1]]) - length(section[[con]])))
				}
			}
		}
		
		concatSection <- section
		sectionVector <- names(concatSection)
		for(sectionNameIndex in seq(along = sectionVector)) {
			values <- vector()
			for(con2 in seq(along = 1:sectionNameIndex)) {
				if (con2 == 1) {
					values <- concatSection[[sectionVector[con2]]]
				} else {
					values <- paste(values, concatSection[[sectionVector[con2]]], sep = ".")
				}
			}
			
			for(nn in seq(along = concatSection[[sectionVector[sectionNameIndex]]])) {
				if (values[nn] %in% sectionMappingList[[sectionVector[sectionNameIndex]]]) {
					if (section[[sectionVector[sectionNameIndex]]][nn] != 0 && !is.null(sectionMappingList[[sectionVector[sectionNameIndex]]][[values[nn]]])) {
						position <- values[nn]
						
						repeat {
							if (!is.null(sectionMappingList[[sectionVector[sectionNameIndex]]][[position]]$newSection)) {				 
								section[[sectionVector[sectionNameIndex]]][nn] <- sectionMappingList[[sectionVector[sectionNameIndex]]][[position]]$newSection
								break;
							} else if (!is.null(sectionMappingList[[sectionVector[sectionNameIndex]]][[position]]$takeRestValuesFrom)) {
								position <- sectionMappingList[[sectionVector[sectionNameIndex]]][[position]]$takeRestValuesFrom	
							} else {
								break;
							}
						}	
					}
				}
			}	
		}
		return(section)	
	} else {
		return(rep.int(sectionMappingList$section[[APPENDIX.SECTION]]$newSection, lengthAppendix))
	}
}

changeXAxisName <- function(overallList) {
	if (length(overallList$iniDataSet$Day..Int.) > 1 ) {
		day_int <- as.character(overallList$iniDataSet$Day..Int.[1])
		day <- as.character(overallList$iniDataSet$Day[1])
		overallList$xAxisName <- substr(day, 1, nchar(day)-(nchar(day_int)+1))
	}
	return(overallList)
}

createErrorPlot <- function(file, name) {
	
	library("Cairo")
	if (length(file) == 0) {
		file <- paste(DIRECTORY.PLOTS, paste("errorTempFile", format(Sys.time(), "%d%H%M%S%Y"), sep = ""), sep = DIRECTORY.SEPARATOR)
	}
	ownCat(paste("Create error plot '", file, "'", sep = ""))
	Cairo(width = 900, height = 70, file = file, type = "pdf", bg = "transparent", units = "px", dpi = 90)
	par(mar = c(0, 0, 0, 0))
	plot.new()
	legend("left", paste("error in ", name, sep = ""), col = c("black"), pch = 1, bty = "n")
	dev.off()
}

ckeckIfNoValuesImagesIsThere <- function(file = "noValues.pdf") {
	ownCat("Check if the noValues-Image is there")
	
	if (!file.exists(file)) {
		library("Cairo")
		ownCat(paste("Create defaultImage '", file, "'", sep = ""))
		Cairo(width = 900, height = 70, file = file, type = "pdf", bg = "transparent", units = "px", dpi = 90)
		par(mar = c(0, 0, 0, 0))
		plot.new()
		legend("left", "no values", col = c("black"), pch = 1, bty = "n")
		dev.off()
	}
}

ckeckIfReportTexIsThere <- function(errorText = "", typ = NULL, debug = FALSE) {
	debug %debug% "ckeckIfReportTexIsThere()"
	
	if(is.null(typ)) {
		debug %debug% "Check if the report.tex file is there"
	}
	
	if (!file.exists(REPORT.FILE)) {
		text <- "\\documentclass{article}
\\usepackage[T1]{fontenc}
\\begin{document}"

		if (!(!is.null(typ) && (typ == LIB.UPDATE || typ == LIB))) {
			text <- paste(text, "There was an error! \\newline", sep="")
		}
		
		if (!is.null(typ) && typ == LIB) {
			text <- paste(text, "The report function requires installation of additional R packages. Please install the following packages (using the ``install.packages'' command): \\vspace{1cm} \\newline install.packages(c(", sep = "")
			for(nn in seq(along = errorText)) {
				text <- paste(text, "\\textquotedbl ", errorText[nn], "\\textquotedbl ", sep = "")
				if (nn < length(errorText)) {
					text <- paste(text, ", ", sep = "")
				}
			} 
			text <- paste(text, "))", sep = "")
		} else if (!is.null(typ) && typ == LIB.UPDATE) {
			text <- paste(text, "The report function requires updated versions of some packages. Please update the following packages (using the \"update.packages\" command): \\vspace{1cm} \\newline ", sep = "")
			for(nn in seq(along = errorText)) {
				repos <- c("http://cran.r-project.org", "http://www.rforge.net/")
				for(rep in repos) {
					text <- paste(text, "update.packages(\"", errorText[nn], "\", repos = \"", rep, "\") \\newline ", sep = "")
				}
			}			
		} else {
			if (errorText != "") {
				text <- paste(text, errorText, sep = "")
			} 
		}
		text <- paste(text, "\\end{document}", sep = "")		
		write(x = text, append = FALSE, file = REPORT.FILE)
	}
}

checkIfAllNecessaryFilesAreThere <- function() {
	ownCat("... check if all necessary files are there")
	ckeckIfNoValuesImagesIsThere()
	ckeckIfReportTexIsThere()		###########debug
}

buildBlacklist <- function(workingDataSet, descriptorSet, debug = FALSE) {
	debug %debug% "initRfunction()"

	additionalDescriptors <- c(descriptorSet, "Day (Int)", "Day (Float)", "Day", "Time", "Plant ID", "vis.side", "fluo.side", "nir.side", "vis.top", "fluo.top", "nir.top")	
	searchString <- c(".histogram.")
	
	for(nn in seq(1:5)) {
		searchString <- c(searchString, paste("section_", nn, "_5", sep = ""))
	}
	additionalDescriptors <- preprocessingOfValues(additionalDescriptors, TRUE, debug = debug)
	for(kk in searchString) {
		additionalDescriptors <- c(additionalDescriptors, colnames(workingDataSet)[grep(kk, colnames(workingDataSet), ignore.case = TRUE)])
	}
	
	return(additionalDescriptors)
}


loadStressPeriod <- function(stressValue, arg, debug = FALSE) {
	debug %debug% "initRfunction()"
	
	stressValue <- stressValue %exists% arg
	stressValue <- unlist(preprocessingOfValues(stressValue, isColName = TRUE, debug = debug))
	return(stressValue)
}

checkStressTypValues <- function(stressTyp, debug = FALSE) {
	debug %debug% "checkStressTypValues()"
	
	newValues <- list("n" = "000", "d" = "001", "w" = "002", "c" = "003", "s" = "004", 
			"0" = "000", "1" = "001", "2" = "002", "3" = "003", "4" = "004")
	
	for (kk in seq(along = stressTyp)) {
		if (stressTyp[kk] %in% names(newValues)) {
			stressTyp[kk] <- newValues[[stressTyp[kk]]]
			debug %debug% "... change old stresstyp value to new!"
		}
	}
	return(stressTyp)
}

##
#	set the system ini values for error-reporting,
#	close all open devs and
#	load the necessary libraries
##
initRfunction <- function(install = FALSE, update = FALSE, check = FALSE, catch = FALSE, debug = FALSE) {
	debug %debug% "initRfunction()"
	
	if (debug) {
		options(error = quote({
							dump.frames();
							ownCat(attr(last.dump, "error.message"));
							traceback();
						}))
		options(show.error.messages = TRUE)
		options(warn = 0)
	} else {	
		options(error = NULL)
		options(warn = -1)
		options(show.error.messages = FALSE)
	}
	
	while(!is.null(dev.list())) {
		dev.off()
	}
	
	if (debug) {
		processLibs(install = install, update = update, check = check, catch = catch, debug = debug)
	} else {
		suppressMessages(processLibs(install = install, update = update, check = check, catch = catch, debug = debug))
	}
}

createLibPath <- function(debug) {
	debug %debug% "createLibPath()"
	
	dir.create(paste(".", DIRECTORY.SECTION, sep = DIRECTORY.SEPARATOR))
	
}
###################################################################################################################################################################
createInitDirectories <- function(debug) {
	debug %debug% "createInitDirectories()"
	
	dir.create(paste(".", DIRECTORY.SECTION, sep = DIRECTORY.SEPARATOR))
	dir.create(paste(".", DIRECTORY.PLOTS, sep = DIRECTORY.SEPARATOR))
	dir.create(paste(".", DIRECTORY.PLOTSTEX, sep = DIRECTORY.SEPARATOR))
}

##
#	Set the initial values and start the report function
##
startOptions <- function(args, typOfStartOptions = START.TYP.TEST, catch = FALSE, debug = FALSE) {
	debug %debug% "startOptions()"
	
	checkRPackagesVersions <- as.logical(FALSE %exists% args[15])
	installMissingRPackages <- as.logical(FALSE %exists% args[16])
	updateOldRPackages <- as.logical(FALSE %exists% args[17])

	initRfunction(install = installMissingRPackages, update = updateOldRPackages, check = checkRPackagesVersions, catch = catch, debug = debug)
	
	#typOfStartOptions <- START.TYP.TEST; debug = TRUE;
	createInitDirectories(debug)
	typOfStartOptions <- tolower(typOfStartOptions)
	
#	args <- commandArgs(TRUE)

	saveFormat = "pdf"
	dpi = "90"
	
	isGray = FALSE
	
	# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")
	# stressTyp: 1 -> dry; 2 -> wet; 0 -> normal; 4 -> salt; 3 -> chemical; -1 <- none
	stressStart <- -1
	stressEnd <- -1
	stressTyp <- -1 
	stressLabel <- -1	
	
	if (DO.MODELLING.OF.STRESS) {
		treatment <- STRESS.MODELL.TREATMENT
	} else {
		treatment = "Treatment"
	}
	
	filterTreatment = NONE
	splitTreatmentFirst = TRUE
	
	secondTreatment = NONE
	filterSecondTreatment = NONE
	splitTreatmentSecond = FALSE
	
	xAxis = "Day..Int." 
	xAxisName = "DAS"
	filterXaxis = NONE
	
	isRatio <- FALSE
	
	should.Clustered <- FALSE
	bootstrap.N <- -1
	
	descriptorSet = vector()
	descriptorSetName = vector()
	
	if (DO.MODELLING.OF.STRESS) {
		fileName <- STRESS.MODELL.INPUT
	} else {
		fileName = NONE
	}
	
	appendix = FALSE
	
	separation = ";"
		
	if (typOfStartOptions == START.TYP.REPORT) {
		fileName <- fileName %exists% args[1]
		
		should.Clustered <- should.Clustered %exists% args[7]
		bootstrap.N <- bootstrap.N %exists% args[8] 
		stressStart <- loadStressPeriod(stressStart, args[9], debug = debug)
		stressEnd <- loadStressPeriod(stressEnd, args[10], debug = debug)
		stressTyp <- loadStressPeriod(stressTyp, args[11], debug = debug)
		stressTyp <- checkStressTypValues(stressTyp, debug = debug)	
		stressLabel <- loadStressPeriod(stressLabel, args[12], debug = debug)
		splitTreatmentFirst <- as.logical(splitTreatmentFirst %exists% args[13])
		splitTreatmentSecond <- as.logical(splitTreatmentSecond %exists% args[14])
		
		if (fileName != NONE) {
			workingDataSet <- separation %readInputDataFile% fileName
			descriptorSet_nBoxplot <- vector()
			descriptorSet_boxplot <- vector()
			descriptorSet_boxplotStacked <- vector()
			
			if (length(workingDataSet[, 1]) > 0) {
				
				loadFiles(path = ".", pattern = "PlotList\\.[Rr]$", debug = debug) ## load nBoxPlotList, boxPlotList, ...
				loadFiles(path = ".", pattern = "sectionMapping\\.[Rr]$", debug = debug)	## load sectionMappingList
				
				descriptorSet_temp <- getRealNameAndPrintSection(nBoxPlotList, debug = debug)
				descriptorSet_nBoxplot <- descriptorSet_temp$columName
				descriptorSetName_nBoxplot <- descriptorSet_temp$plotName	
				descriptorSection_nBoxplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList, debug = debug)
				
				descriptorSet_temp <- getRealNameAndPrintSection(nBoxMultiPlotList, debug = debug)
				descriptorList <- addDesSetLineSec(descriptorSet_temp$columName, descriptorSet_temp$plotName, workingDataSet, descriptorSet_temp$section, debug = debug)
				descriptorSet_nBoxMultiplot <- descriptorList$desSet
				descriptorSetName_nBoxMultiplot <- descriptorList$desName
				descriptorSection_nBoxMultiplot <- changeSectionToRealSection(descriptorList$desSec, sectionMappingList, debug = debug)
				
				descriptorSet_temp <- getRealNameAndPrintSection(boxPlotList, debug = debug)
				descriptorSet_boxplot <- descriptorSet_temp$columName
				descriptorSetName_boxplot <- descriptorSet_temp$plotName	
				descriptorSection_boxplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList, debug = debug)
				
				descriptorSet_temp <- getRealNameAndPrintSection(spiderPlotList, debug = debug)
				descriptorSet_spiderplot <- descriptorSet_temp$columName
				descriptorSetName_spiderplot <- descriptorSet_temp$plotName	
				descriptorSection_spiderplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList, debug = debug)
				
				descriptorSet_temp <- getRealNameAndPrintSection(linerangePlotList, debug = debug)
				descriptorSet_linerangeplot <- descriptorSet_temp$columName
				descriptorSetName_linerangeplot <- descriptorSet_temp$plotName	
				descriptorSection_linerangeplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList, debug = debug)
				
				
				descriptorSet_temp <- getRealNameAndPrintSection(violinPlotList, debug = debug)
				descriptorSet_violinBox <- descriptorSet_temp$columName
				descriptorSetName_violinBox <- descriptorSet_temp$plotName	
				descriptorSection_violinBox <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList, debug = debug)
				
				descriptorSet_temp <- getRealNameAndPrintSection(stackedPlotList, debug = debug)
				descriptorList <- addDesSet(descriptorSet_temp$columName, descriptorSet_temp$plotName, workingDataSet, descriptorSet_temp$section, debug = debug)
				descriptorSet_boxplotStacked <- descriptorList$desSet
				descriptorSetName_boxplotStacked <- descriptorList$desName
				descriptorSection_boxplotStacked <- changeSectionToRealSection(descriptorList$desSec, sectionMappingList, debug = debug)
				
				appendix = as.logical(appendix %exists% args[5])
				
				if (appendix) {
					blacklist = buildBlacklist(workingDataSet, descriptorSet_nBoxplot, debug = debug)
					descriptorSetAppendix = colnames(workingDataSet[!as.data.frame(sapply(colnames(workingDataSet), '%in%', blacklist))[, 1]])
					descriptorSetNameAppendix = descriptorSetAppendix
					descriptorSectionAppendix <- changeSectionToRealSection(NULL, sectionMappingList, length(descriptorSetAppendix), debug = debug)
				}
				
				saveFormat = saveFormat %exists% args[2]
				
				listOfTreatAndFilterTreat = checkOfTreatments(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug = debug)
				treatment = listOfTreatAndFilterTreat[[1]][[1]]
				secondTreatment = listOfTreatAndFilterTreat[[1]][[2]]
				filterTreatment = listOfTreatAndFilterTreat[[2]][[1]]
				filterSecondTreatment = listOfTreatAndFilterTreat[[2]][[2]]
				
				if (treatment == NONE.TREATMENT) {
					workingDataSet <- cbind(workingDataSet, noneTreatment = rep.int(VALUE.AVERAGE, times = length(workingDataSet[, 1])))	
				}
				
				isRatio	 = as.logical(isRatio %exists% args[6])
			} else {
				fileName = NONE
			}
		}
		
	} else if (typOfStartOptions == START.TYP.TEST) {
		
		if (DO.PARALLELISATION) {
			library("snowfall")
		}
		debug <- TRUE
		checkRPackagesVersions <- FALSE
		installMissingRPackages <- FALSE
		updateOldRPackages <- FALSE
		catchErrorAndReport <- TRUE
		
		initRfunction(install = installMissingRPackages, update = updateOldRPackages, check = checkRPackagesVersions, catch = catchErrorAndReport, debug = debug)
		if (DO.PARALLELISATION) {
			sfStop()
		}
		#treatment <- "Species"
		#filterTreatment <- "Athletico$Fernandez$Weisse Zarin"
		
		#treatment <- "none"
		treatment <- "Treatment"
		#treatment <- "Genotype"
		#filterTreatment <- "stress / control"
		#filterTreatment <- "none"
		#filterTreatment <- "control"
		filterTreatment <- "dry$normal"
		#filterTreatment <- "Trockentress$normal bewaessert"
		#filterTreatment <- "N661230.3 x IL$N323525.9 x IL$N590895.3 x IL"
		
		#secondTreatment <- "none"
		#filterSecondTreatment <- "none"
		
		secondTreatment <- "none"
		#secondTreatment <- "Genotype"
		#secondTreatment <- "Treatment"
		filterSecondTreatment <- "none"
		#filterSecondTreatment <- "S 250$S 280"
		#filterSecondTreatment <- "Wiebke$MorexPE$Streif"
		
		#secondTreatment <- "Treatment"
		#filterSecondTreatment <- "dry / normal"
		
		#filterSecondTreatment <- "Athletico$Weisse Zarin"
		#filterSecondTreatment <- "BCC_1367_Apex$BCC_1391_Isaria$BCC_1403_Perun$BCC_1433_HeilsFranken$BCC_1441_PflugsIntensiv$Wiebke$BCC_1413_Sissy$BCC_1417_Trumpf"
		filterXaxis <- "none"
		
		bgColor <- "transparent"
		isGray = "FALSE"
		#showResultInR <- FALSE
		
		fileName <- "report.csv"
		separation <- ";"
		workingDataSet <- separation %readInputDataFile% fileName
		
		saveName <- "test2"
		yAxisName <- "test2"
		
		# c("0", "1", "2", "3", "4") entspricht c("n", "d", "w", "c", "s")
		
		if (TRUE) {
			stressStart <- -1
			stressEnd <- -1
			stressTyp <- "001"
			stressLabel <- "-1"
		} else {
			stressStart <- 20
			stressEnd <- 80
			stressTyp <- "001"
			stressLabel <- "TestLabel"
		}
		
		
		splitTreatmentFirst <- FALSE
		splitTreatmentSecond <- FALSE
		isRatio <- FALSE
		calculateNothing <- FALSE
		stoppTheCalculation <- FALSE
		iniDataSet <- workingDataSet
		
		
		removeAllFilesWithoutTheReport <- FALSE
		
		
		loadFiles(path = ".", pattern = "PlotList\\.[Rr]$")
		loadFiles(path = ".", pattern = "sectionMapping\\.[Rr]$")
		
		descriptorSet_temp <- getRealNameAndPrintSection(nBoxPlotList)
#		descriptorList <- addDesSetLineSec(descriptorSet_temp$columName, descriptorSet_temp$plotName, workingDataSet, descriptorSet_temp$section)
#		descriptorSet_nBoxplot <- descriptorList$desSet
#		descriptorSetName_nBoxplot <- descriptorList$desName
#		descriptorSection_nBoxplot <- changeSectionToRealSection(descriptorList$desSec, sectionMappingList)
		descriptorSet_nBoxplot <- descriptorSet_temp$columName
		descriptorSetName_nBoxplot <- descriptorSet_temp$plotName	
		descriptorSection_nBoxplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList)
		
		
		descriptorSet_temp <- getRealNameAndPrintSection(nBoxMultiPlotList)
		descriptorList <- addDesSetLineSec(descriptorSet_temp$columName, descriptorSet_temp$plotName, workingDataSet, descriptorSet_temp$section)
		descriptorSet_nBoxMultiplot <- descriptorList$desSet
		descriptorSetName_nBoxMultiplot <- descriptorList$desName
		descriptorSection_nBoxMultiplot <- changeSectionToRealSection(descriptorList$desSec, sectionMappingList)
		
		
		descriptorSet_temp <- getRealNameAndPrintSection(boxPlotList)
		descriptorSet_boxplot <- descriptorSet_temp$columName
		descriptorSetName_boxplot <- descriptorSet_temp$plotName	
		descriptorSection_boxplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList)
		
		descriptorSet_temp <- getRealNameAndPrintSection(spiderPlotList)
		descriptorSet_spiderplot <- descriptorSet_temp$columName
		descriptorSetName_spiderplot <- descriptorSet_temp$plotName	
		descriptorSection_spiderplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList)
		
		descriptorSet_temp <- getRealNameAndPrintSection(linerangePlotList)
		descriptorSet_linerangeplot <- descriptorSet_temp$columName
		descriptorSetName_linerangeplot <- descriptorSet_temp$plotName	
		descriptorSection_linerangeplot <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList)
		
		descriptorSet_temp <- getRealNameAndPrintSection(violinPlotList)
		descriptorSet_violinBox <- descriptorSet_temp$columName
		descriptorSetName_violinBox <- descriptorSet_temp$plotName	
		descriptorSection_violinBox <- changeSectionToRealSection(descriptorSet_temp$section, sectionMappingList)
		
		descriptorSet_temp <- getRealNameAndPrintSection(stackedPlotList)
		descriptorList <- addDesSet(descriptorSet_temp$columName, descriptorSet_temp$plotName, workingDataSet, descriptorSet_temp$section)
		descriptorSet_boxplotStacked <- descriptorList$desSet
		descriptorSetName_boxplotStacked <- descriptorList$desName
		descriptorSection_boxplotStacked <- changeSectionToRealSection(descriptorList$desSec, sectionMappingList)
		
		filterTreatmentRename <- list()
		secondFilterTreatmentRename <- list()
		###################
		
		boxDes = descriptorSet_boxplot
		boxStackDes = descriptorSet_boxplotStacked 
		boxDesName = descriptorSetName_boxplot
		boxStackDesName = descriptorSetName_boxplotStacked 
		nBoxOptions = nBoxOptions
		nBoxMultiOptions <- nBoxMultiOptions
		boxOptions = boxOptions
		stackedBarOptions = stackedBarOptions
		linerangeOptions = linerangeOptions
		nBoxDes <- descriptorSet_nBoxplot
		nBoxDesName <- descriptorSetName_nBoxplot
		nBoxMultiDes <- descriptorSet_nBoxMultiplot
		nBoxMultiDesName <- descriptorSetName_nBoxMultiplot
		boxSpiderDes <- descriptorSet_spiderplot
		boxSpiderDesName <- descriptorSetName_spiderplot
		violinBoxDes <- descriptorSet_violinBox
		violinBoxDesName <- descriptorSetName_violinBox
		linerangeDes <- descriptorSet_linerangeplot
		linerangeDesName <- descriptorSetName_linerangeplot
		
		nBoxSection <- descriptorSection_nBoxplot
		nBoxMultiSection <- descriptorSection_nBoxMultiplot
		boxSection <- descriptorSection_boxplot
		boxStackSection <- descriptorSection_boxplotStacked 
		boxSpiderSection <- descriptorSection_spiderplot 
		violinBoxSection <- descriptorSection_violinBox
		linerangeSection <- descriptorSection_linerangeplot
		
		appendix <- FALSE
		deleteNboxplot <- !appendix
		if (appendix) {
			blacklist <- buildBlacklist(workingDataSet, descriptorSet_nBoxplot)
			descriptorSetAppendix <- colnames(workingDataSet[!as.data.frame(sapply(colnames(workingDataSet), '%in%', blacklist))[, 1]])
			descriptorSetNameAppendix <- descriptorSetAppendix
			descriptorSectionAppendix <- changeSectionToRealSection(NULL, sectionMappingList, length(descriptorSetAppendix))
			#descriptorSectionAppendix <- rep.int(99, length(descriptorSetAppendix))
			#diagramTypVectorAppendix = rep.int("nboxplot", times = length(descriptorSetNameAppendix))		
			nBoxDes <- descriptorSetAppendix
			nBoxDesName <- descriptorSetNameAppendix
			nBoxSection <- descriptorSectionAppendix
			#diagramTypVector = diagramTypVectorAppendix
			nBoxMultiDes <- NULL
			nBoxMultiDesName <- NULL
			boxDes <- NULL
			boxStackDes <- NULL 
			boxDesName <- NULL
			boxStackDesName <- NULL
			nBoxOptions <- NULL
			nBoxMultiOptions <- NULL
			boxOptions <- NULL
			stackedBarOptions <- NULL
			linerangeOptions <- NULL
			boxSpiderDes <- NULL
			boxSpiderDesName <- NULL
			violinBoxDes <- NULL
			violinBoxDesName <- NULL
			linerangeDes <- NULL
			linerangeDesName <- NULL
			boxSection <- NULL
			boxStackSection <- NULL 
			boxSpiderSection <- NULL 
			violinBoxSection <- NULL
			linerangeSection <- NULL
		}
		
		listOfTreatAndFilterTreat = checkOfTreatments(args, treatment, filterTreatment, secondTreatment, filterSecondTreatment, workingDataSet, debug = debug)
		treatment = listOfTreatAndFilterTreat[[1]][[1]]
		secondTreatment = listOfTreatAndFilterTreat[[1]][[2]]
		filterTreatment = listOfTreatAndFilterTreat[[2]][[1]]
		filterSecondTreatment = listOfTreatAndFilterTreat[[2]][[2]]
		
		if (treatment == NONE.TREATMENT) {
			workingDataSet <- cbind(workingDataSet, noneTreatment = rep.int(VALUE.AVERAGE, times = length(workingDataSet[, 1])))	
		}
		
	}
	
	if (typOfStartOptions != START.TYP.TEST) {
		secondRun = appendix
		appendix = FALSE
		renameList <- list(filterTreatmentRename = list(), secondFilterTreatmentRename = list())
		
		if (fileName != NONE) {## & (length(descriptorSet_nBoxplot) > 0 || length(descriptorSet_boxplot) > 0 || length(descriptorSet_boxplotStacked) > 0)
			time = system.time({
						repeat {					
							if (appendix) 
								ownCat("Generate diagrams for annotation descriptors...")
							else
								ownCat("Generate diagrams for main descriptors...")
							renameList <- createDiagrams(iniDataSet = workingDataSet, saveFormat = saveFormat, dpi = dpi, isGray = isGray, 
									nBoxDes = descriptorSet_nBoxplot, nBoxMultiDes = descriptorSet_nBoxMultiplot, boxDes = descriptorSet_boxplot, boxStackDes = descriptorSet_boxplotStacked, boxSpiderDes = descriptorSet_spiderplot, violinBoxDes = descriptorSet_violinBox, 	linerangeDes = descriptorSet_linerangeplot, 
									nBoxDesName = descriptorSetName_nBoxplot, nBoxMultiDesName = descriptorSetName_nBoxMultiplot, boxDesName = descriptorSetName_boxplot, boxStackDesName = descriptorSetName_boxplotStacked, boxSpiderDesName = descriptorSetName_spiderplot, violinBoxDesName = descriptorSetName_violinBox, linerangeDesName = descriptorSetName_linerangeplot, 
									nBoxSection = descriptorSection_nBoxplot, nBoxMultiSection = descriptorSection_nBoxMultiplot, boxSection = descriptorSection_boxplot, boxStackSection = descriptorSection_boxplotStacked, boxSpiderSection = descriptorSection_spiderplot, violinBoxSection = descriptorSection_violinBox, linerangeSection = descriptorSection_linerangeplot, 
									nBoxOptions = nBoxOptions, nBoxMultiOptions = nBoxMultiOptions, boxOptions = boxOptions, stackedBarOptions = stackedBarOptions, spiderOptions = spiderOptions, violinOptions = violinOptions, linerangeOptions = linerangeOptions, 
									treatment = treatment, filterTreatment = filterTreatment, 
									secondTreatment = secondTreatment, filterSecondTreatment = filterSecondTreatment, filterXaxis = filterXaxis, xAxis = xAxis, 
									xAxisName = xAxisName, debug = debug, appendix = appendix, isRatio = isRatio, 
									filterTreatmentRename = renameList$filterTreatmentRename, secondFilterTreatmentRename = renameList$secondFilterTreatmentRename, 
									stressStart = stressStart, stressEnd = stressEnd, stressTyp = stressTyp, stressLabel = stressLabel, 
									splitTreatmentFirst = splitTreatmentFirst, splitTreatmentSecond = splitTreatmentSecond, 
									deleteNboxplot = !secondRun,
									catch = catch)
							if (secondRun) {
								appendix <- TRUE
								secondRun <- FALSE
								descriptorSet_nBoxplot <- descriptorSetAppendix
								descriptorSetName_nBoxplot <- descriptorSetNameAppendix
								descriptorSection_nBoxplot <- descriptorSectionAppendix
								#diagramTypVector = diagramTypVectorAppendix
								descriptorSet_nBoxMultiplot <- NULL
								descriptorSetName_nBoxMultiplot <- NULL
								descriptorSection_nBoxMultiplot <- NULL
								descriptorSet_boxplot <- NULL
								descriptorSetName_boxplot <- NULL
								descriptorSection_boxplot <- NULL
								descriptorSet_boxplotStacked <- NULL
								descriptorSetName_boxplotStacked <- NULL
								descriptorSection_boxplotStacked <- NULL
								descriptorSet_spiderplot <- NULL
								descriptorSetName_spiderplot <- NULL
								descriptorSection_spiderplot <- NULL
								descriptorSet_violinBox <- NULL
								descriptorSetName_violinBox <- NULL
								descriptorSection_violinBox <- NULL
								descriptorSet_linerangeplot <- NULL
								descriptorSetName_linerangeplot <- NULL
								descriptorSection_linerangeplot <- NULL
								
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
		buildReportTex(debug)
		
		if (debug) {
			#ownCat(warnings())
			warnings()
			
		}
	} 
	#### Bis hier einlesen beim manuellen testen ##############	
	return(NULL)
}

createDiagrams <- function(iniDataSet, saveFormat = "pdf", dpi = "90", isGray = "false", 
		nBoxDes = NULL, nBoxMultiDes = NULL, boxDes = NULL, boxStackDes = NULL, boxSpiderDes = NULL, violinBoxDes = NULL, linerangeDes = NULL, 
		nBoxDesName = NULL, nBoxMultiDesName = NULL, boxDesName = NULL, boxStackDesName = NULL, boxSpiderDesName = NULL, violinBoxDesName = NULL, linerangeDesName = NULL, 
		nBoxSection = NULL, nBoxMultiSection = NULL, boxSection = NULL, boxStackSection = NULL, boxSpiderSection = NULL, violinBoxSection = NULL, linerangeSection = NULL, 
		nBoxOptions = NULL, nBoxMultiOptions = NULL, boxOptions = NULL, stackedBarOptions = NULL, spiderOptions = NULL, violinOptions = NULL, linerangeOptions = NULL, 
		treatment = "Treatment", filterTreatment = NONE, 
		secondTreatment = NONE, filterSecondTreatment = NONE, filterXaxis = NONE, xAxis = xAxis, 
		xAxisName = NONE, debug = FALSE, appendix = FALSE, stoppTheCalculation = FALSE, isRatio = FALSE, 
		filterTreatmentRename = list(), secondFilterTreatmentRename = list(), 
		stressStart = -1, stressEnd = -1, stressTyp = -1, stressLabel = -1, 
		splitTreatmentFirst = TRUE, splitTreatmentSecond = FALSE, 
		deleteNboxplot = TRUE,
		catch = FALSE) {		
	
	overallList <- list(iniDataSet = iniDataSet, saveFormat = saveFormat, dpi = dpi, isGray = isGray, 
			nBoxDes = nBoxDes, nBoxMultiDes = nBoxMultiDes, boxDes = boxDes, boxStackDes = boxStackDes, boxSpiderDes = boxSpiderDes, violinBoxDes = violinBoxDes, linerangeDes = linerangeDes, 
			imageFileNames_nBoxplots = nBoxDes, imageFileNames_nBoxMultiPlots = nBoxMultiDes, imageFileNames_Boxplots = boxDes, imageFileNames_StackedPlots = boxStackDes, imageFileNames_SpiderPlots = boxSpiderDes, imageFileNames_violinPlots = violinBoxDes, imageFileNames_LinerangePlots = linerangeDes, 
			nBoxDesName = nBoxDesName, nBoxMultiDesName = nBoxMultiDesName, boxDesName = boxDesName, boxStackDesName = boxStackDesName, boxSpiderDesName = boxSpiderDesName, violinBoxDesName = violinBoxDesName, linerangeDesName = linerangeDesName, 
			nBoxSection = nBoxSection, nBoxMultiSection = nBoxMultiSection, boxSection = boxSection, boxStackSection = boxStackSection, boxSpiderSection = boxSpiderSection, violinBoxSection = violinBoxSection, linerangeSection = linerangeSection, 
			nBoxOptions = nBoxOptions, nBoxMultiOptions = nBoxMultiOptions, boxOptions = boxOptions, stackedBarOptions = stackedBarOptions, spiderOptions = spiderOptions, violinOptions = violinOptions, linerangeOptions = linerangeOptions, 
			treatment = treatment, filterTreatment = filterTreatment, 
			secondTreatment = secondTreatment, filterSecondTreatment = filterSecondTreatment, filterXaxis = filterXaxis, xAxis = xAxis, 
			xAxisName = xAxisName, debug = debug, 
			appendix = appendix, stoppTheCalculation = stoppTheCalculation, isRatio = isRatio, 
			overallResult_nBoxDes = data.frame(), overallResult_boxDes = data.frame(), overallResult_boxStackDes = data.frame(), overallResult_boxSpiderDes = data.frame(), overallResult_violinBoxDes = data.frame(), overallResult_linerangeDes = data.frame(), 
			color_nBox = list(), color_box = list(), color_boxStack = list(), color_spider = list(), color_violin = list(), color_linerange = list(), 
			user = NONE, typ = NONE, 
			filterTreatmentRename = filterTreatmentRename, secondFilterTreatmentRename = secondFilterTreatmentRename, 
			stressStart = stressStart, stressEnd = stressEnd, stressTyp = stressTyp, stressLabel = stressLabel, 
			splitTreatmentFirst = splitTreatmentFirst, splitTreatmentSecond = splitTreatmentSecond, 
			deleteNboxplot = deleteNboxplot,
			catch = catch
	)	
	
	overallList$debug %debug% "Start"
	
	overallList = checkTypOfExperiment(overallList)
	overallList = checkUserOfExperiment(overallList)
	overallList = setSomePrintingOptions(overallList)
	overallList = overallChangeName(overallList)
	overallList = changeXAxisName(overallList)
	overallList = overallPreprocessingOfDescriptor(overallList)
	
#	####
#	overallList = preprocessingOfxAxisValue(overallList)
#	overallList = preprocessingOfTreatment(overallList)
#	overallList = preprocessingOfSecondTreatment(overallList)
#	overallList = renameOfTheTreatments(overallList)
#	overallList = overallCheckIfDescriptorIsNaOrAllZero(overallList)
#	overallList <- getUnits(overallList)
#	overallList = reduceWorkingDataSize(overallList)
#	overallList = setDefaultAxisNames(overallList)
#	
#	#overallList = overallOutlierDetection(overallList)
#	overallList = overallGetResultDataFrame(overallList)
#	overallList = setColor(overallList) 
	##	makeDiagrams(overallList)
	#######
	
	if (!overallList$stoppTheCalculation) {
		overallList <- preprocessingOfxAxisValue(overallList)
		overallList <- preprocessingOfTreatment(overallList)
		overallList <- preprocessingOfSecondTreatment(overallList)
		overallList <- renameOfTheTreatments(overallList)
		overallList <- overallCheckIfDescriptorIsNaOrAllZero(overallList)
#		overallList <- getUnits(overallList)
		if (!overallList$stoppTheCalculation) {
			overallList <- reduceWorkingDataSize(overallList)
			overallList <- setDefaultAxisNames(overallList)
			#ownCat(overallList)
			#	overallList = overallOutlierDetection(overallList)
			overallList <- overallGetResultDataFrame(overallList)			
			if (!overallList$stoppTheCalculation) {
				overallList <- setColor(overallList) 
				overallList <- calculateStressValues(overallList)
				if (!plotNothing) {
					makeDiagrams(overallList)
				}
			}
		}
	}
	return(list(filterTreatmentRename = overallList$filterTreatmentRename, secondFilterTreatmentRename = overallList$secondFilterTreatmentRename))
}

initSnow <- function(debug = FALSE) {
	debug %debug% "initSnow()"
	
	library("snowfall")
	
	if (CPU.ATUO.DETECTED) {
		CPU.CNT <- parallel::detectCores()
	}
	
	sfInit(parallel = threaded, cpus = CPU.CNT)
	
	if (sfParallel()) {
		TRUE %print% paste("Running in parallel mode on", sfCpus(), "nodes.", sep="")
		threaded <- TRUE
	} else {
		TRUE %print% "Running in sequential mode."
		threaded <- FALSE
	}
	if (threaded)
		sfExportAll()
}

stopSnow <- function(debug = FALSE) {
	debug %debug% "stopSnow()"
	
	sfStop()
}


processing <- function() {
	
	args <- commandArgs(TRUE)
	catchErrorAndReport <- as.logical(CATCH.ERROR %exists% args[19])
	debug <- as.logical(DEBUG %exists% args[18])
	
	debug %debug% "processing()"
	
	if (DO.PARALLELISATION) {
		initSnow(debug = debug)
	}
	
	if (catchErrorAndReport) {
		error <- try(startOptions(args, START.TYP.REPORT, catch = catchErrorAndReport, debug = debug))
		checkOfTryError(error)
	} else {
		startOptions(args, START.TYP.REPORT, catch = catchErrorAndReport, debug = debug)
	}
		
	TRUE %print% "Completing diagram and creation with R ..."	
	if (DO.PARALLELISATION) {
		stopSnow(debug = debug)
	}
	
	if (debug) {
		TRUE %print% "Print all warnings:"
		warnings()
	}
}

processing()
rm(list = ls(all = TRUE))