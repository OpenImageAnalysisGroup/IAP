##################################
# Author: C. Klukas
# May 2012
##################################

loadLib <- function() {
	library(snow)
	library(parallel)
}
parP <- function(data, nboot, method.hclust) {
	tryCatch(loadLib(), error=function(w) {
		cat("Info: Library 'snow' or 'parallel' could not be loaded. Trying to install library...\n");
		cat("Info: In case of error start the R gui and choose install packages, select 'snow' and 'parallel'.\n");
		install.packages(c("snow", "parallel"), repos="http://cran.rstudio.com/"); loadLib();
	})
  cl <- makeCluster(12, type="MPI")
  cat("Info: Parallel computing is supported. Use multi-thread mode.\n");
  res <- parPvclust(cl, data=data, nboot=nboot, method.hclust=method.hclust)
  return(res)
}

mydata <- read.csv('report.clustering.csv', sep=';', row.names="UniID", header=TRUE)
commandArgs(TRUE)[1] -> n
n <- as.numeric(n)
cat(c("Calculate clustering for",(length(mydata)),"groups. Using bootstrap N =",n,"...\n"))

result <- NULL

if (n>0) {
	tryCatch(library(pvclust), error=function(w) {
		cat("Info: Library 'pvclust' could not be loaded. Trying to install library...\n");
		cat("Info: In case of error start the R gui and choose install packages, select 'pvclust'.\n");
		install.packages(c("pvclust"), repos="http://cran.rstudio.com/"); library(pvclust);;
	})
	
	
	tryCatch(result <- parP(data=data.frame(mydata), nboot=n, method.hclust="ward"), error=function(w) {
		cat("Info: Parallel computing not supported. Use single-thread mode.\n");
		result <- pvclust(data=data.frame(mydata), nboot=n, method.hclust="ward");;return(NA)
	})

	

	pdf("clusters.pdf", width=15, height=15)
	plot(result)
	
	ask.bak <- par()$ask
	par(ask=TRUE)
	
	pvrect(result, alpha=0.95)
	dev.off()
} else {
	result <- hclust(d=dist(t(mydata)), method="ward")
	pdf("clusters.pdf", width=15, height=15)
	plot(result)
	dev.off()
}
