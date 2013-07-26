# TODO: Add comment
# 
# Author: Entzian
###############################################################################

###später include ownCat


ckeckIfNoValuesImagesIsThere <- function(file = "noValues.pdf") {
	
	if (!file.exists(file)) {
		print("Check if the noValues-Image is there")
		library("Cairo")
		print(paste("Create defaultImage '", file, "'", sep=""))
		Cairo(width=900, height=70, file=file, type="pdf", bg="transparent", units="px", dpi=90)
		par(mar = c(0, 0, 0, 0))
		plot.new()
		legend("left", "no values", col= c("black"), pch=1, bty="n")
		dev.off()
	}
}

ckeckIfReportTexIsThere <- function(file = "report.tex") { # REPORT.FILE

	if (!file.exists(file)) {
		print("Check if the report.tex file is there")
		
		text <- "\\documentclass{article} \\newline
				\\begin{document} \\newline
				There was an error! \\newline
				\\end{document}"

		write(x=text, append=FALSE, file=file)
	}
}

checkIfAllNecessaryFilesAreThere <- function() {
	print("... check if all necessary files are there")
	ckeckIfNoValuesImagesIsThere()
	ckeckIfReportTexIsThere()
}


checkIfAllNecessaryFilesAreThere()