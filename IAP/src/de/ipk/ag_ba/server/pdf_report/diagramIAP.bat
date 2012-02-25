@echo off
cd /d %~dp0
echo Current directory:
cd

echo Condition 1: %1
echo Condition 2: %2
echo Appendix?  : %3

Rscript createDiagrams.r report.csv pdf %3 %1 %2

pdflatex report.tex
pdflatex report.tex