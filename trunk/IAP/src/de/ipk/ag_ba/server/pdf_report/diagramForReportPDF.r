# TODO: Add comment
# 
# Author: Entzian
###############################################################################

source("initLinux.r")

args <- commandArgs(TRUE)
print(args)

fileName <- args[1]
saveFormat <- args[2]
diagramTyp <- args[3]
treatment <- args[4]

#fileName <- "testDataset.csv"
#saveFormat <- "png"
#diagramTyp="boxplotStacked"

if(tolower(diagramTyp) != tolower("boxplotStacked")){
	descriptorSet <- c("Weight A (g)","Weight B (g)","Water (weight-diff)","side.height.norm (mm)","side.width.norm (mm)","side.area.norm (mm^2)",
			"side.fluo.intensity.chlorophyl.average (relative)","side.fluo.intensity.phenol.average (relative)",
			"side.nir.intensity.average (relative)",
			"side.leaf.count.median (leafs)","side.bloom.count (tassel)","side.leaf.length.sum.norm.max (mm)")
			   
	# "digital biomass IAP (pixel^3)","digital biomass KeyGene (pixel^3)", 
	descriptorSetName <- c("weight before watering (g)","weight after watering (g)", "water weight (g)", "height (mm)", "width (mm)", "side area (pixel^2)",
						   "chlorophyl intensity (relative intensity/pixel)", "fluorescence intensity (relative intensity/pixel)", "nir intensity (relative intensity/pixel)",
						   "number of leafs (leaf)", "number of tassels (tassel)", "length of leafs plus stem (mm)")
				   
} else if(tolower(diagramTyp) == tolower("boxplotStacked")) {
	descriptorSet <- c(
			"side.nir.normalized.histogram.bin.1.0_25$side.nir.normalized.histogram.bin.2.25_51$side.nir.normalized.histogram.bin.3.51_76$side.nir.normalized.histogram.bin.4.76_102$side.nir.normalized.histogram.bin.5.102_127$side.nir.normalized.histogram.bin.6.127_153$side.nir.normalized.histogram.bin.7.153_178$side.nir.normalized.histogram.bin.8.178_204$side.nir.normalized.histogram.bin.9.204_229$side.nir.normalized.histogram.bin.10.229_255",
			"side.fluo.normalized.histogram.bin.1.0_25$side.fluo.normalized.histogram.bin.2.25_51$side.fluo.normalized.histogram.bin.3.51_76$side.fluo.normalized.histogram.bin.4.76_102$side.fluo.normalized.histogram.bin.5.102_127$side.fluo.normalized.histogram.bin.6.127_153$side.fluo.normalized.histogram.bin.7.153_178$side.fluo.normalized.histogram.bin.8.178_204$side.fluo.normalized.histogram.bin.9.204_229$side.fluo.normalized.histogram.bin.10.229_255")
	
	descriptorSetName <- c("nir histogram (%)", "chlorophyl histogram (%)")

} else {
	descriptorSet <- ""
	descriptorSetName <- ""
}

filterTreatment <- "none"

if(length(args) < 4) {
	treatment = "none"
}

treatment = "Treatment"; #"Condition"

workingDataSet <- read.csv(fileName, header=TRUE, sep="\t", fileEncoding="ISO-8859-1", encoding="UTF-8") 

#print(colnames(workingDataSet));

#print(workingDataSet["side.leaf.length.sum.norm.max..mm."]);

for (y in 1:length(descriptorSet)) {
	
	print(paste("> Generate diagram of", descriptorSet[y]))
	valuesAsDiagram(workingDataSet,saveName=descriptorSet[y],saveFormat=saveFormat,diagramTyp=diagramTyp,
			treatment=treatment,filterTreatment=filterTreatment,
			descriptor=descriptorSet[y],xAxisName="Day",yAxisName=descriptorSetName[y])
	print("... finished")
	
}

