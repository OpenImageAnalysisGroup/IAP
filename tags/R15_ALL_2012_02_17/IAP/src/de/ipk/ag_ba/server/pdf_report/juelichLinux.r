#
# Author: Entzian
###############################################################################

source("initLinux.r")
#source("valuesAsDiagramLinux2.r")

args <- commandArgs(TRUE)
print(args)

fileName <- args[1]
saveFormat <- args[2]

fileName <- "numeric_data.MaizeAnalysisAction_ 1116BA_new3.csv"
#fileName <- "dryNormalRatio.csv"
#fileName <- "1107BA_Corn_new2.csv"
saveFormat <- "png"
                      


#descriptorSet <- c("orientation_fit", "orientation", "top.ndvi", "top.ndvi.nir.intensity.average",
#                   "top.ndvi.vis.intensity.average", "top.area", "top.border.length", "top.compactness",
#                   "top.compactness..normalized.", "top.hull.area", "top.hull.circularity", "top.hull.circumcircle.d",
#                   "top.hull.fillgrade", "mark1.y", "side.height", "side.width", "side.fluo.intensity.average",
#                   "side.ndvi", "side.ndvi.nir.intensity.average", "side.ndvi.vis.intensity.average",
#                   "side.nir.intensity.average", "side.area", "side.border.length", "side.compactness",
#                   "side.compactness...normalized.", "side.hull.area", "side.hull.circularity", 
#                   "side.hull.circumcircle.d", "side.hull.fillgrade", "side.hull.points", "rgb.side", "fluo.side",
#                   "nir.side", "mark2.y", "mark3.y", "Volume..Biomass.", "Volume.Change....day.")

workingDataSet <- read.csv2(fileName, header=TRUE, sep=";", fileEncoding="ISO-8859-1", encoding="UTF-8")                   
#descriptorSet <- c("side.nir.intensity.average.norm","side.nir.intensity.average","side.hull.area.norm","side.height.norm","Gewicht.B","water.consumption")                   
#descriptorSetName <- c("nir intensity (rel. intensity/px)","nir intensity (rel. intensity/px)","convex hull area (mm^2)","height (mm)","target weight (g)","water consumption")

#descriptorSet <- c("side.nir.intensity.average.norm","Gewicht.B")                   
#descriptorSetName <- c("nir intensity (rel. intensity/px)","target weight (g)")


#descriptorSet <- c("digital.biomass.unnormal","digital.biomass.normal","mark3.y")                   
#descriptorSetName <- c("digital biomass (mm^3)","digital biomass (mm^3)","mark (% from image height)")

#descriptorSet <- c("digital.biomass.keygene.norm","side.area","top.area")                   
#descriptorSetName <- c("digital biomass (mm^3)","test1", "test2")

#descriptorSet <- c("side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255",
#			"side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
#descriptorSetName <- c("NIR absorption class (%)", "chlorophyll fluorescence histogram (%)")

#descriptorSet <- c("side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255")
#descriptorSetName <- c("NIR absorption class (%)")

#descriptorSet <- c("Weight B (g)","side.height.norm (mm)","side.width.norm (mm)","side.area.norm (mm^2)",
#		"side.fluo.intensity.chlorophyl.average (relative)","side.fluo.intensity.phenol.average (relative)",
#		"side.nir.intensity.average (relative)",
#		"side.leaf.count.median (leafs)","side.bloom.count (tassel)","side.leaf.length.sum.norm.max (mm)")

# "digital biomass IAP (pixel^3)","digital biomass KeyGene (pixel^3)", 
#descriptorSetName <- c("weight (g)","height (mm)", "width (mm)", "side area (pixel^2)",
#		"chlorophyl intensity (relative intensity/pixel)", "fluorescence intensity (relative intensity/pixel)", "nir intensity (relative intensity/pixel)",
#		"number of leafs (leaf)", "number of tassels (tassel)", "length of leafs plus stem (mm)")


descriptorSet <- c("side.leaf.length.sum.norm.max (mm)")
descriptorSetName <- c("leaf length corrected (mm)")

#descriptorSet <- c("side.area.ratio (mm)")
#descriptorSetName <- c("side area ratio (dry/normal)")

#descriptorSet <- colnames(workingDataSet)
#descriptorSetName <- colnames(workingDataSet)


#descriptorSet <- colnames(workingDataSet)
#descriptorSetName <- colnames(workingDataSet)

#filterTreatment <- "normal"
#filterTreatment <- "dry$normal"
#filterTreatment <- "none"
##filterTreatment <- "Deutschland$Spanien$Italien$China"
filterTreatment <- "Fernandez$Athletico$Breslau II$Lester Phister"
#filterTreatment <- "Breslau II$Fernandez"
#Cukrova Cervena$lazuti$Gelber Badischer Landmais

#sort(unique(workingDataSet["Day..Int."])[[1]])

###1116BA#########6 8 10 12 13 14 15 16 17 19 20 21 22 23 24 25 26 27 28 29 30 31 33 34 35 36 37 38 39 40
filterXaxis <- c("6$8$10$12$13$14$17$20$21$22$23$24$25$26$27$28$29$30$31$33$34$35$38$39$40")
#ohne 24, 25 und 39, 40

###1107BA#########2$4$6$8$10$12$13$15$16$21$22$25$27$29$30$31$33$35$36$37$39$41$42$43$45$47$49$50$51$55$57$59$61$63$64
#filterXaxis <- c("2$4$6$8$10$12$13$15$16$21$22$25$27$29$30$31$33$35$36$37$39$41$43$45$47$49$50$51$55$57$59$61$63$64")
#filterXaxis <- c("6$8$10")
#filterXaxis <- "none"

#treatment <- "Treatment"
#treatment <- "none"
treatment <- "Species"
#treatment <- "Fernandez$Strenzfelder$lazuti$Col/Chung Nam Jusan/2620"

#secondTreatment <- "none"
secondTreatment <- "Treatment"
#secondTreatment <- "Species"
#filterSecondTreatment <- "none"
#filterSecondTreatment <- "Athletico$Strenzfelder$Cukrova Cervena"
filterSecondTreatment <- "normal"

#diagramTyp <- "boxplotStacked"
diagramTyp <- "!boxplot"
#diagramTyp <- "boxplot"

transparent <- "FALSE"
legendUnderImage <- "FALSE"
showResultInR <- "TRUE"

time <- system.time({
			
for (y in 1:length(descriptorSet)) {

		print(paste("... generate diagram of", descriptorSet[y]))
		valuesAsDiagram(iniDataSet=workingDataSet,saveName=descriptorSet[y],saveFormat=saveFormat,diagramTyp=diagramTyp,treatment=treatment,filterTreatment=filterTreatment,
						descriptor=descriptorSet[y],xAxisName="Day",yAxisName=descriptorSetName[y], filterXaxis=filterXaxis, transparent = transparent,
						showResultInR=showResultInR, legendUnderImage=legendUnderImage, secondTreatment=secondTreatment, filterSecondTreatment=filterSecondTreatment)
		print("... ready")

}
},TRUE)
print(time)
############ Time ####  alle Treatments, alle Tage, kein second ################
#
#[1] "... generate diagram of digital.biomass.keygene.norm"
#[1] "... ready"
#[1] "... generate diagram of side.area"
#[1] "... ready"
#[1] "... generate diagram of top.area"
#[1] "... ready"
#User      System verstrichen 
#6.85        0.06        6.94 