#############################################
# Author: Dijun Chen, Christian Klukas
# 03 March 2014, 6th March 2014
# Outlier removal based on Grubbs tests
#############################################

cat(">>> Outlier detection and removal based on Grubbs tests\n")
require(outliers)
infile <- "report.csv"
outfile <- "report_without_outliers.csv"
col.day <- "Day (Int)"
col.condition <- "Condition"
threshold <- as.numeric(commandArgs(TRUE)[1])

removed <<- 0

## Function for outlier removal best on Grubbs tests
grubbs.test.rm.outliers <- function(x, set.na=TRUE, threshold=0.01){
	if (is.null(x)) return(x)
	if (is.matrix(x)){
		apply(x, 2, grubbs.test.rm.outliers)
	} else if (is.data.frame(x)){
		as.data.frame(sapply(x, grubbs.test.rm.outliers))
	} else{
		if (all(is.na(x)) || length(!is.na(x)) < 3 
#				|| length(unique(x)) < 2 
#				|| sum(is.na(x)) > length(x)/2 
#				|| sum(!is.na(x)) < 6
			) 
			return(x)

		repeat {
			gt <- grubbs.test(x)
			if (!is.na(gt$p.value) && gt$p.value < threshold) {
				cat(".")
				if (length(unique(x[!is.na(x)])) < 2) break
				if (set.na) {
					removed <<- removed + 1
					x[which(x == outlier(x))[1]] <- NA
				} else {
					olden <- length(x)
					x <- rm.outlier(x)
					removed <<- removed + olden - length(x)
				}
			} else {
				break
			}
		} 
		return(x)
	}
}

cat("Reading input from \"", infile, "\"...\n", sep="")
input <- read.csv(infile, header=TRUE, sep=";", check.names=FALSE, fileEncoding="UTF-8")
traits <- colnames(input)[-(1:21)]
conditions <- sort(unique(as.character(input[, col.condition])))
days <- unique(input[, col.day])

result <- input
for (condition in conditions) {
	for (day in days) {
		plant.rows <- which(input[, col.condition] %in% condition & input[, col.day] == day)
		plant.data <- input[plant.rows, traits]
		result[plant.rows, traits] <- grubbs.test.rm.outliers(plant.data, TRUE, threshold)
		cat("\n")
	}
}

cat("Writinging output to \"", outfile, "\"...\n")
write.table(result, file=outfile, sep=";", na="", row.names=FALSE)
cat(">>>Processing finished. Removed ", removed, " outliers!\n")

wf <- file("warnings.txt", open="wt")
sink(file=wf, append=FALSE, type=c("output", "message"), split=FALSE)
warnings()
sink()
