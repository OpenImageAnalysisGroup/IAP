#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"

Rscript createDiagrams.r report.csv pdf ${3} ${1} ${2}

echo "Create PDF..."
/usr/bin/pdflatex report.tex
/usr/texbin/pdflatex report.tex
/usr/bin/pdflatex report.tex
/usr/texbin/pdflatex report.tex
echo ""
echo "Finished"
