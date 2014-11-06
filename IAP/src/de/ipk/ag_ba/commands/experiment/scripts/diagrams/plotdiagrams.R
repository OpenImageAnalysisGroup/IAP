############################################
# Authors: S. Friedel, adapted by C. Klukas,
# D. Chen
# 2012/2013/2014
############################################

cat(c("Plot diagrams:\n"))
fileName="report.csv"

col.time <- "Day (Int)"
col.variety <- "Variety"
col.condition <- "Condition"
col.plant <- "Plant ID"

stop.watering=-1
# as.numeric(commandArgs(TRUE)[1])
rewatering=-1
# as.numeric(commandArgs(TRUE)[2])
## the first *** columns are non-traits
non.traits <- 28

cat("Read input file", fileName, "...\n")
data <- read.csv(fileName, header=TRUE, sep=";", fileEncoding="UTF-8", check.names=FALSE)
data[data[, col.variety]=="", col.variety] <- "Other"

plot.trait <- function(conditions, trait) {
	## get the range for the x and y axis
	# sub.data <- data[data[, col.plant] %in% varieties, c(trait, col.time, col.plant)]
	## sub.data[!is.na(sub.data[,1]),]
	sub.data <- na.omit(data[, c(col.condition, col.variety, col.plant, col.time, trait)]) 
	if (nrow(sub.data) < 1){
		cat('******** no data for ', trait, "\n")
		return(NULL)
	}
	
	## cat('-----------------------\n')
	## plant varieties
	varieties <- unique(as.character(sub.data[, col.variety]))
	## number of varieties
	n.varieties <- length(varieties)
	## plant ID with variety
	plant2variety <- unique(sub.data[, c(col.plant, col.variety)])
	rownames(plant2variety) <- plant2variety[, col.plant]
	
	# set up the plot
	op <- par(mfrow=c(1, length(conditions)), pty="m")
	for(condition in conditions){
		plot.data <- sub.data[sub.data[, col.condition]==condition, ]
		xrange <- range(plot.data[, col.time])
		yrange <- range(plot.data[, trait], na.rm=T) 
		plot(xrange, yrange, type="n", xlab="Days", ylab=trait, main=condition)
		pchs <- seq(from=10, length.out=n.varieties)
		cols <- rainbow(n.varieties)
		ltys <- c(1:n.varieties)
		names(cols) <- names(ltys) <- names(pchs) <- varieties
		
		## highlight stress period
		if (stop.watering>0 & rewatering>0) {
			abline(v=c(stop.watering, rewatering), col="red", lty=3)
		}

		## add a legend
		legend('topleft', varieties, cex=0.8, col=cols, pch=pchs, lty=ltys, title="Varieties")

		## plot lines
		for (plant in rownames(plant2variety)) {
			plant.data <- plot.data[which(plot.data[, col.plant] == plant), c(col.time, trait)]
			if(nrow(plant.data) < 1) next
			variety <- as.character(plant2variety[plant, col.variety])
			lines(plant.data[, 1], plant.data[, 2], lwd=1.5, type="b", lty=ltys[variety], 
					col=cols[variety], pch=pchs[variety])
		}
	}
	par(op)
}

conditions <- unique(data[, col.condition])
##########################
trait.columns <- colnames(data)[-c(1:non.traits)]

pdf("plots.pdf", height=10, width=10*length(conditions))
#par(mfrow = c(1,length(conditions)))
for (trait in trait.columns) {
	cat(trait, "\n")
	## plot.trait(conditions, trait)
	tryCatch(plot.trait(conditions, trait), error=function(w) {
		cat(c("\n", "Can't plot condition ", condition, ", trait ", trait,"\n")); return(NA)}
	)
}
dev.off()

cat("Processing finished.\n")

wf <- file("warnings.txt", open = "wt")
sink(file = wf, append = FALSE, type = c("output", "message"), split = FALSE)
warnings()
