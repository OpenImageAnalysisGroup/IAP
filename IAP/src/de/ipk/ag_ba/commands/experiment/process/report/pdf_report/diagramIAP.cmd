#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"

echo "#####"
echo "Condition 1   : $1"
echo "Condition 2   : $2"
echo "Appendix?     : $3"
echo "Ratio?        : $4"
echo "Clustering?   : $5"
echo "Bootstrap-n?  : $6"
echo "Stress start? : $7"
echo "Stress end?   : $8"
echo "Stress typ?   : $9"
echo "Stress label? : ${10}"
echo "Split Cond1?  : ${11}"
echo "Split Cond2?  : ${12}"
echo "Check package version?  : ${13}"
echo "Install new package?  : ${14}"
echo "Update package? : ${15}"
echo "Debug? : ${16}"
echo "Catch error? : ${17}"

if [ -f report.aux ]; then
	rm /s report.aux
fi

if [ -f report.out ]; then
	rm /s report.out
fi

if [ -f report.tex ]; then
	rm /s report.tex
fi

if [ -f report.pdf ]; then
	rm /s report.pdf
fi

if [ -d plots ]; then
	rd /s /q plots
fi

if [ -d plotTex ]; then
	rd /s /q plotTex
fi

if [ -d section ]; then
	rd /s /q section
fi


if [ -f report.clustering.csv ]
then
Rscript calcClusters.R $6
fi

Rscript createDiagrams.R report.csv pdf "$@"

echo "Create PDF..."
/usr/bin/pdflatex report.tex -interaction batchmode
/usr/texbin/pdflatex report.tex -interaction batchmode

/usr/bin/pdflatex report.tex -interaction batchmode
/usr/texbin/pdflatex report.tex -interaction batchmode

/usr/bin/pdflatex report.tex -interaction batchmode
/usr/texbin/pdflatex report.tex -interaction batchmode
echo ""
echo "Finished"
