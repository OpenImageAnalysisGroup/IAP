#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"
PARAM=$#
INPUTFILE="report.csv"
Rscript diagramForReportPDF.r ${INPUTFILE} png "boxplotStacked" none
Rscript diagramForReportPDF.r ${INPUTFILE} png "!boxplot" ${2}
echo "Create PDF..."
/usr/texbin/pdflatex report2.tex
echo "Finished"