# TODO: Add comment
# 
# Author: Klukas, Entzian
###############################################################################

rm(list=ls(all=TRUE))
library("ade4")
#remove <- c("F047", "Mo17", "UH007")
#removeMH <- c("P104", "B112", "A374")
#removeMB <- c("S052")

#### dCNV ######
dataCNV <- read.csv("CNV_Filtered list_cluster analysis_MMM_3col___FINAL.csv", header=TRUE, sep=";", row.names="Marker")
#data <- data[,!(colnames(data) %in% remove)]
dataCNV <- dataCNV[,sort(colnames(dataCNV))]
dCNV <- as.dist(1-cor(dataCNV))

#### dCNV_More #####
dataCNVM <- read.csv("CNV_more than 1genotype.csv", header=TRUE, sep=";", row.names="Marker")
#data <- data[,!(colnames(data) %in% remove)]
dataCNVM <- dataCNVM[,sort(colnames(dataCNVM))]
dCNVM <- as.dist(1-cor(dataCNVM))

#### dSNP ######
moses <- read.csv('CGH_SNP data_Moses_data.csv', sep=';', header=TRUE, row.names='marker')
#moses <- moses[,!(colnames(moses) %in% remove)]

x <- t(moses)
x[x=='AA'] <- 11
x[x=='AC'] <- 12
x[x=='AG'] <- 13
x[x=='AT'] <- 14
x[x=='CC'] <- 22
x[x=='CG'] <- 23
x[x=='CT'] <- 24
x[x=='GG'] <- 33
x[x=='GT'] <- 34
x[x=='TT'] <- 44
x[x=='']   <- NA
# (not in the first moses-dataset)
x[x=='CA'] <- 12
x[x=='GA'] <- 13
x[x=='TA'] <- 14
x[x=='GC'] <- 23
x[x=='TC'] <- 24
x[x=='TG'] <- 34

dx <- apply(x,1,as.numeric)
dx <- as.data.frame(dx)
dx <- dx[,sort(colnames(dx))]
dSNP <- as.dist(1-cor(dx, use="na.or.complete"))


############### mantel #################


mantel.rtest(m1=dCNV, m2=dSNP,nrepet=100000)
mantel.rtest(m1=dCNVM, m2=dSNP,nrepet=100000)




#######################################################
##################### ab hier tests!!! ################
#######################################################



#### dCNV_Bio ####


moses2 <- read.csv('CNV_Biomass_Dataxls.csv', sep=';', header=TRUE, row.names='Genotype')
#moses2 <- moses2[,!(colnames(moses2) %in% remove)]
#moses2_Maturity_Biomass <- moses2[c("Maturity_group", "Biomass_Rank"),!(colnames(moses2) %in% removeMB)]
#moses2_Maturity_Heterosis <- moses2[c("Maturity_group", "Mid_parent_Heterosis_Rank"),!(colnames(moses2) %in% removeMH)]
#moses2_Biomass_Heterosis <- moses2[c("Biomass_Rank", "Mid_parent_Heterosis_Rank"),]

#dCNV_Bio <- as.dist(1-cor(moses2))
#dCNV_Bio_MB <- as.dist(1-cor(moses2_Maturity_Biomass))
#dCNV_Bio_MH <- as.dist(1-cor(moses2_Maturity_Heterosis))
#dCNV_Bio_BH <- as.dist(1-cor(moses2_Biomass_Heterosis))

#moses2_BioRow1 <- moses2[1,] # Maturity_group
#moses2_BioRow2 <- moses2[2,] # Biomass_rank
#moses2_BioRow3 <- moses2[3,] # Mid Parent Heterosis_rank
#dCNV_BioRow1 <- as.dist(1-cor(moses2_BioRow1))
#dCNV_BioRow2 <- as.dist(1-cor(moses2_BioRow2))
#dCNV_BioRow3 <- as.dist(1-cor(moses2_BioRow3))

########### neue Variante matel(corelation(CNV, Bio), SNP) ##################

moses2_Biomass <- moses2[c("Biomass_Rank"),]
moses2BioSort <- moses2_Biomass[,sort(colnames(moses2_Biomass))]

meantest3 <- as.data.frame(apply(dataCNVSort,1,cor, moses2BioSort[1,]))

cor(meantest3[1,1], moses2BioSort[1,1])

dataCNVSort <- data[,sort(colnames(data))]
dataSNPSort <- dx[,sort(colnames(dx))]


test1 <- 1-cor(dataCNVSort)
test2 <- 1-cor(dataSNPSort)
test3 <- 1-cor(moses2BioSort)

cor(test1, test3)



#dataCNVSortMean <- as.data.frame(apply(dataCNVSort,2,mean))
#cor(dataCNVSortMean)
#cor(t(moses2BioSort))
#cor_CNV_Bio_Mean <- 1-cor((dataCNVSortMean), t(moses2BioSort), use="na.or.complete")

test_dataSNPSort <- na.omit(dataSNPSort)

cor(t(dataCNVSort[1:5,]), t(moses2BioSort))
cor(t(dataSNPSort[1:5,]), t(moses2BioSort), use="na.or.complete")


cor_CNV_Bio <- 1-cor(t(dataCNVSort), t(moses2BioSort), use="na.or.complete")
dcor_CNV_Bio <- as.dist(cor_CNV_Bio)

cor_SNP_Bio <- 1-cor(t(dataSNPSort), t(moses2BioSort), use="na.or.complete")
dcor_SNP_Bio <- as.dist(cor_SNP_Bio)

test_cor_SNP_Bio <- 1-cor(t(test_dataSNPSort), t(moses2BioSort), use="na.or.complete")
test_dcor_SNP_Bio <- as.dist(test_cor_SNP_Bio)

library("ade4")



dSNPm <- as.matrix(dSNP) 
dSNPms <- as.dist(dSNPm[order(attr(dSNP, "Labels")),order(attr(dSNP, "Labels"))])



mantel.rtest(m1=dcor_CNV_Bio, m2=test_dcor_SNP_Bio,nrepet=100)
mantel.rtest(m1=test, m2=dSNPms,nrepet=100)











#moses2_BiomassEx <- moses2_Biomass[,"F047"]
#moses2_Heterosis <- moses2[c("Mid_parent_Heterosis_Rank"),!(colnames(moses2) %in% remove)]

###### mantel.rtest ############
library("ade4")

dSNPm <- as.matrix(dSNP) 
dSNPms <- dSNPm[order(attr(dSNP, "Labels")),order(attr(dSNP, "Labels"))]

dCNVm <- as.matrix(dCNV)
dCNVms <- dCNVm[order(attr(dCNV, "Labels")),order(attr(dCNV, "Labels"))]

dCNV_Biom <- as.matrix(dCNV_Bio) 
dCNV_Bioms <- dCNV_Biom[order(attr(dCNV_Bio, "Labels")),order(attr(dCNV_Bio, "Labels"))]
###
dCNV_Bio_MB_m <- as.matrix(dCNV_Bio_MB) 
dCNV_Bio_MB_ms <- dCNV_Bio_MB_m[order(attr(dCNV_Bio_MB, "Labels")),order(attr(dCNV_Bio_MB, "Labels"))]

dCNV_Bio_MH_m <- as.matrix(dCNV_Bio_MH) 
dCNV_Bio_MH_ms <- dCNV_Bio_MH_m[order(attr(dCNV_Bio_MH, "Labels")),order(attr(dCNV_Bio_MH, "Labels"))]

dCNV_Bio_BH_m <- as.matrix(dCNV_Bio_BH) 
dCNV_Bio_BH_ms <- dCNV_Biom[order(attr(dCNV_Bio_BH, "Labels")),order(attr(dCNV_Bio_BH, "Labels"))]
#dCNV_BioRow1m <- as.matrix(dCNV_BioRow1) 
#dCNV_BioRow1ms <- dCNV_BioRow1m[order(attr(dCNV_BioRow1, "Labels")),order(attr(dCNV_BioRow1, "Labels"))]
#
#dCNV_BioRow2m <- as.matrix(dCNV_BioRow2) 
#dCNV_BioRow2ms <- dCNV_BioRow2m[order(attr(dCNV_BioRow2, "Labels")),order(attr(dCNV_BioRow2, "Labels"))]
#
#dCNV_BioRow3m <- as.matrix(dCNV_BioRow3) 
#dCNV_BioRow3ms <- dCNV_BioRow3m[order(attr(dCNV_BioRow3, "Labels")),order(attr(dCNV_BioRow3, "Labels"))]

mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dSNPms),nrepet=100)

mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_Bioms),nrepet=100000)
mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_Bioms),nrepet=100000)

mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_Bio_MB_ms),nrepet=100000)
mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_Bio_MH_ms),nrepet=100000)
mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_Bio_BH_ms),nrepet=100000)

mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_Bio_MB_ms),nrepet=100000)
mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_Bio_MH_ms),nrepet=100000)
mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_Bio_BH_ms),nrepet=100000)
#mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_BioRow1ms),nrepet=100)
#mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_BioRow1ms),nrepet=100)
#mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_BioRow2ms),nrepet=100)
#mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_BioRow2ms),nrepet=100)
#mantel.rtest(m1=as.dist(dCNVms), m2=as.dist(dCNV_BioRow3ms),nrepet=100)
#mantel.rtest(m1=as.dist(dSNPms), m2=as.dist(dCNV_BioRow3ms),nrepet=100)

####### partial.mantel.test ########
#library(ncf)
#
#partial.mantel.test(M1=as.dist(dCNVms), M2=as.dist(dSNPms), M3=as.dist(dCNV_Bioms), resamp=10)

####### Cluster ##############

library("pvclust")
library(snow)
cl <- makeCluster(4)
result_SNP <- parPvclust(cl=cl,data=dx, method.hclust="ward", nboot=10000)
plot(result_SNP)
