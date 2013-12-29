##################################
# Author: C. Klukas
# May 2012
##################################

mydata <- read.csv('report.clustering.csv', sep=';', row.names="UniID", header=TRUE)
commandArgs(TRUE)[1] -> n
n <- as.numeric(n)
cat(c("Calculate clustering for",(length(mydata)),"groups. Using bootstrap N =",n,"...\n"))

result <- NULL

if (n>0) {
	library(pvclust)
	result <- pvclust(data=data.frame(mydata), nboot=n, method.hclust="ward");
	pdf("clusters.pdf", width=15, height=15)
	plot(result)
	pvrect(result, alpha=0.95)
	dev.off()
} else {
	result <- hclust(d=dist(t(mydata)), method="ward")
	pdf("clusters.pdf", width=15, height=15)
	plot(result)
	dev.off()
}
