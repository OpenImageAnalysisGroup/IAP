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
data<-read.csv(fileName, header=TRUE, sep=";", fileEncoding="UTF-8")

#data[is.na(data$Genotype)] <- "(not defined)"
#data[is.na(data$Treatment)] <- "(not defined)" 
#data[is.na(data$Plant.ID)]<- "(not defined)"

TraitsOfInterest <- colnames(data)

plot.trait<-function(Condition, Trait) {
  # define Plant ID
  PlantIDsOfInterest<-unique(
                        data[
                          which(data$Condition==Condition),
                          col.plantID])
  # number of lines
  nPlantIDs <- length(PlantIDsOfInterest)
  # get the range for the x and y axis
  xrange <- range(data[which(data[which(data$Plant.ID %in% PlantIDsOfInterest),col.time]<2147483647),col.time]) 
  yrange <- range(data[,Trait], na.rm=T) 
  # set up the plot
  colors <- rainbow(nPlantIDs)
  if (length(colors)==0)
    return;
  plot(xrange, yrange, type="n", xlab="Days", ylab=gsub("\\.", " ", Trait))
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
         lty=linetype, title=Condition)
  for (i in 1:nPlantIDs) {
    lines(data[which(data$Plant.ID == PlantIDsOfInterest[i]),col.time],
          data[which(data$Plant.ID == PlantIDsOfInterest[i]),Trait], type="b", 
          lwd=1.5, lty=linetype[i], col=colors[i], pch=plotchar[i])
  }
  # add a title and subtitle
  title(Condition)
}

conditions <- unique(data$Condition)

pdf("plots.pdf", height=10, width=10*length(conditions))

par(mfrow = c(1,length(conditions)))

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
  for (con in conditions) {
    tryCatch(plot.trait(con, Trait), error=function(w) {cat(c("Can't plot condition ", c, ", trait ", Trait,"\n"));return(NA)})
  }
}

dev.off()

cat("Processing finished.\n")

warnings()