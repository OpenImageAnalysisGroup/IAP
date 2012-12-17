##################################
# Author: C. Klukas
# May 2012
##################################

library(pvclust)
mydata <- read.csv('report.clustering.csv', sep=';', row.names="UniID", header=TRUE)
commandArgs(TRUE)[1] -> n
n <- as.numeric(n)
cat(c("Calculate clustering for",(length(mydata)),"groups. Using bootstrap N =",n,"...\n"))

result <- NULL

if (n>0) {
#	tryCatch( 
#	library(snow)
#	cl <- makeCluster(6); 
#	result <- parPvclust(cl=cl, data=data.frame(mydata), nboot=n, method.hclust="ward");
#,	error = function(e) {
#		cat("Could not create compute cluster!\n")
	result <- pvclust(data=data.frame(mydata), nboot=n, method.hclust="ward");
#		} )
	

	pdf("clusters.pdf", width=15, height=15)
#[2:length(mydata)]
	plot(result)
	pvrect(result, alpha=0.95)
	dev.off()
} else {
	result <- hclust(d=dist(t(mydata)), method="ward")
	pdf("clusters.pdf", width=15, height=15)
	plot(result)
	dev.off()
}
