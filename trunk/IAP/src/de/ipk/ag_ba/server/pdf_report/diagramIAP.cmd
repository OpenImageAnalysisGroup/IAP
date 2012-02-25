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
	TREAT="Genotype"
	SEC="none"
	THI="FALSE"
fi

if [ ${THI} = "TRUE" ]; then
	Rscript createDiagrams.r ${INPUTFILE} pdf TRUE ${TREAT} ${SEC}
else
	Rscript createDiagrams.r ${INPUTFILE} pdf FALSE ${TREAT} ${SEC}
fi

echo "Create PDF..."
/usr/bin/pdflatex report.tex
/usr/texbin/pdflatex report.tex
/usr/bin/pdflatex report.tex
/usr/texbin/pdflatex report.tex
echo ""
echo "Finished"
