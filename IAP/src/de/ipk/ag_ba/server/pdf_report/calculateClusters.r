##################################
# Author: C. Klukas
# May 2012
##################################

library(pvclust)
mydata <- read.csv('report.clustering.csv', sep=';', row.names="UniID")
commandArgs(TRUE)[1] -> n
cat(c("Calculate clustering for",(length(mydata)),"groups. Using bootstrap N =",n,"...\n"))
result <- pvclust(data.frame(mydata[2:length(mydata)]), nboot=n)
pdf("clusters.pdf")
plot(result)
pvrect(result, alpha=0.95)
dev.off()
