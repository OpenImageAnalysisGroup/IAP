@echo off
cd /d %~dp0
echo Current directory:
cd

echo Condition 1   : %1
echo Condition 2   : %2
echo Appendix?     : %3
echo Ratio?        : %4
echo Clustering?   : %5
echo Bootstrap-n?  : %6
echo Stress start? : %7
echo Stress end?   : %8
echo Stress typ?   : %9
echo Stress label? : %10

IF EXIST report.clustering.csv Rscript --encoding=UTF-8 calcClusters.R %6

Rscript --encoding=UTF-8 createDiagrams.R report.csv pdf %3 %4 %1 %2 %5 %7 %8 %9 %10

rem The delete option is important at the development phase
rem del /s report.aux
rem del /s report.out
rem del /s report.toc

pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode

rem cmd
