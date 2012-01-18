#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"
PARAM=$#
INPUTFILE="report.csv"

if [ $PARAM = 1 ]; then
	TREAT=${1}
	SEC="none"
	THI="FALSE"
elif [ $PARAM = 2 ]; then
	TREAT=${1}
	SEC=${2}
	THI="FALSE"	
elif [ $PARAM = 3 ]; then
	TREAT=${1}
	SEC=${2}
	THI=${3}
else
	TREAT="Condition"
	SEC="none"
	THI="FALSE"
fi

if [ ${THI} = "TRUE" ]; then
	Rscript createDiagramOneFile.r ${INPUTFILE} png TRUE ${TREAT} ${SEC}
else
	Rscript createDiagramOneFile.r ${INPUTFILE} png FALSE ${TREAT} ${SEC}
fi

echo "Create PDF..."
/usr/texbin/pdflatex report2.tex
/usr/bin/pdflatex report2.tex
echo "Finished"