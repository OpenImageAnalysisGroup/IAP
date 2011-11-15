#
# Author: Entzian
###############################################################################

#setwd("/home/entzian/R-Skripte")
DEBUG = FALSE

if(DEBUG) {
	
	#library(debug)
	options(error = recover)
} else {
	
	options(error = NULL)
}
	
rm(list=ls(all=TRUE))
source("createDiagramFromValuesLinux.r")
#source("valuesAsDiagramLinux.r")
#source(paste(getwd(),"/valuesAsDiagramLinux.r",sep=""))

while(!is.null(dev.list())) {
	dev.off()
}
