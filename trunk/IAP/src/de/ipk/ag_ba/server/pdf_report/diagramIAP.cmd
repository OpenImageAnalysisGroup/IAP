#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"
PARAM=$#
INPUTFILE="report.csv"

if [ $PARAM = 1 ]; then
	TREAT=${1}
	SEC=""
elif [ $PARAM = 2 ]; then
	TREAT=${1}
	SEC=${2}	
else
	TREAT="Treatment"
	SEC=""
fi

Rscript createDiagramOneFile.r ${INPUTFILE} png FALSE ${TREAT} ${SEC}
Rscript createDiagramOneFile.r ${INPUTFILE} png TRUE ${TREAT} ${SEC}

echo "Create PDF..."
/usr/texbin/pdflatex report2.tex
/usr/bin/pdflatex report2.tex
echo "Finished"