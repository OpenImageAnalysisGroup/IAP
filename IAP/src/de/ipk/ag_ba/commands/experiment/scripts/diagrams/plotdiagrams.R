############################################
# Author: S. Friedel, adapted by C. Klukas
# 2012/2013
############################################

cat(c("Plot diagrams...\n"))
fileName="report.csv"

col.time="precise.Time"
col.time="Day..Int."
col.plantID="Plant.ID"

stop.watering=-1
# as.numeric(commandArgs(TRUE)[1])
rewatering=-1
# as.numeric(commandArgs(TRUE)[2])

TraitsOfInterest<-c(col.time,
                    "Plant.ID", "Condition", "Species", "Genotype", "Variety", "GrowthCondition", 
                    "Treatment",
                    "Sequence", "Day", "vor", "Time", "Day..Int.", 
                    "Weight.A..g.", "Weight.B..g.", "Water..weight.diff.", "Water..sum.of.day.", 
                    "side.area..px.", "side.width.norm..mm.", "side.height.norm..mm.", "volume.fluo.iap")

cat("Read input file", fileName, "...\n")
#if (is.null(data)) {
  data<-read.csv(fileName, header=TRUE, sep=";", fileEncoding="UTF-8")
#  head(data)
#  data<-data[,TraitsOfInterest]
#}

plot.trait<-function(Treatment, Genotype, Trait) {
  # define Plant ID
  PlantIDsOfInterest<-unique(
                        data[
                          which(data$Treatment==Treatment & data$Genotype==Genotype),
                          col.plantID])
  # number of lines
  nPlantIDs <- length(PlantIDsOfInterest)
  # get the range for the x and y axis
  xrange <- range(data[which(data[which(data$Plant.ID %in% PlantIDsOfInterest),col.time]<2147483647),col.time])
  yrange <- range(data[,Trait], na.rm=T)
  # set up the plot
  plot(xrange, yrange, type="n", xlab="Days", ylab=Trait, pch = 50, cex = .5)
  colors <- rainbow(nPlantIDs)
  linetype <- c(1:nPlantIDs)
  plotchar <- seq(10,10+nPlantIDs,1)
  # add lines
  if (stop.watering>0) {
    if (rewatering>0) {
      abline(v=c(stop.watering,rewatering), col="red", lty=3)
  	}
  }
  # add a legend
  legend(xrange[1], yrange[2], PlantIDsOfInterest[1:nPlantIDs], cex=0.8, col=colors, # pch=plotchar,
         lty=linetype, title=Genotype)
  for (i in 1:nPlantIDs) {
    lines(data[which(data$Plant.ID == PlantIDsOfInterest[i]),col.time],
          data[which(data$Plant.ID == PlantIDsOfInterest[i]),Trait], type="b", 
          lwd=1.5, lty=linetype[i], col=colors[i], pch=plotchar[i])
  }
  # add a title and subtitle
  title(Treatment)
}

genotypes <- unique(data$Genotype)
treatments <- unique(data$Treatment)

pdf("plots.pdf")

#par(mfrow = c(2,length(genotypes)))

for (Trait in TraitsOfInterest) {
  if (Trait=="Plant.ID" || Trait==col.time || Trait=="Day" || Trait=="vor" 
      || Trait=="Time"
      || Trait=="Day..Int."
      || Trait=="Plant.ID"|| Trait=="Condition"
      || Trait=="Species"
      || Trait=="Genotype"
      || Trait=="Variety"
      || Trait=="GrowthCondition"
      || Trait=="Treatment"
      || Trait=="Sequence")
    next
  
  cat("Plot", Trait, "...\n")
  for (g in genotypes) {
    for (t in treatments) {
      plot.trait(t, g, Trait)
    }
  }
}

dev.off()
