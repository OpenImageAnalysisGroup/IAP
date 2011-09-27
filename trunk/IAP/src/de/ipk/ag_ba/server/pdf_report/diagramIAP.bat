@echo off
cd /d %~dp0
echo "Current directory: $(pwd)"
Rscript diagramForReportPDF.r report.csv png "boxplotStacked" none
Rscript diagramForReportPDF.r report.csv png "!boxplot" ${2}
#pdflatex report2.tex