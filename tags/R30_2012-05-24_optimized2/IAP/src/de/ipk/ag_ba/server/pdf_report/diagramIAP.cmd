#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"

if -a report.clustering.csv; then Rscript calcClusters.r ${6}; fi

Rscript createDiagrams.r report.csv pdf ${3} ${4} ${1} ${2} ${5}

echo "Create PDF..."
/usr/bin/pdflatex report.tex
/usr/texbin/pdflatex report.tex
/usr/bin/pdflatex report.tex
/usr/texbin/pdflatex report.tex
echo ""
echo "Finished"
