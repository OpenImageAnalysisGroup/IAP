#
# Author: Entzian
###############################################################################

#setwd("/home/entzian/R-Skripte")
rm(list=ls(all=TRUE))
source("createDiagramFromValuesLinux.r")
#source("valuesAsDiagramLinux.r")
#source(paste(getwd(),"/valuesAsDiagramLinux.r",sep=""))

while(!is.null(dev.list())) {
	dev.off()
}
