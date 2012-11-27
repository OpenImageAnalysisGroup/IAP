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


DEL /s report.aux
DEL /s report.out
DEL /s report.toc
DEL /s report.tex
DEL /s report.pdf
RD /s /q plots
RD /s /q plotTex
RD /s /q section


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
