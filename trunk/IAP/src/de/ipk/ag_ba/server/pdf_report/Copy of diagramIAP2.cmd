#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"
PARAM=$#
INPUTFILE="report.csv"

if [ $PARAM = 1 ]; then
	TREAT=${1}
else
	TREAT="Treatment"	
fi

Rscript diagramForReportPDF.r ${INPUTFILE} png "boxplotStacked" ${TREAT}
Rscript diagramForReportPDF.r ${INPUTFILE} png "!boxplot" ${TREAT}
Rscript diagramForReportPDF.r ${INPUTFILE} png "appendix" ${TREAT}

echo "Create PDF..."
/usr/texbin/pdflatex report2.tex
/usr/bin/pdflatex report2.tex
echo "Finished"