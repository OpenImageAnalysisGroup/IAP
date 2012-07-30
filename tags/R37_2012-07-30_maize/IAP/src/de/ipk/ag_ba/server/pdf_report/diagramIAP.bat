@echo off
cd /d %~dp0
echo Current directory:
cd

echo #####
echo Condition 1   : %1
echo Condition 2   : %2
echo Appendix?     : %3
echo Ratio?        : %4
echo Clustering?   : %5
echo Bootstrap-n?  : %6
echo Stress start? : %7
echo Stress end?   : %8
echo Stress typ?   : %9
shift /8
echo Stress label? : %9
shift /8
echo Split Cond1?  : %9
shift /8
echo Split Cond2?  : %9

IF EXIST report.clustering.csv Rscript --encoding=UTF-8 calcClusters.R %6

rem In the R Skript the first Value is report.csv so the %1 is in the R-Script args[3]
Rscript --encoding=UTF-8 createDiagrams.R report.csv pdf %*

rem The delete option is important at the development phase
rem del /s report.aux
rem del /s report.out
rem del /s report.toc

pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode

rem cmd
