##################################
# Author: C. Klukas
# May 2012
##################################

library(pvclust)
mydata <- read.csv('report.clustering.csv', sep=';', row.names="UniID")
commandArgs(TRUE)[1] -> n
cat(c("Calculate clustering for",(length(mydata)),"groups. Using bootstrap N =",n,"...\n"))

if (n>0) {
	result <- pvclust(data.frame(mydata), nboot=n, method.hclust="ward")
	pdf("clusters.pdf")
#[2:length(mydata)]
	plot(result)
	dev.off()
} else {
	print(mydata)
	result <- hclust(d=dist(t(mydata)), method="ward")
	pdf("clusters.pdf")
	plot(result)
	pvrect(result, alpha=0.95)
	dev.off()
}
