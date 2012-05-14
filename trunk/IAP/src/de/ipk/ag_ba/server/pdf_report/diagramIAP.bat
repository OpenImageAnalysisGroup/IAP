@echo off
cd /d %~dp0
echo Current directory:
cd

echo Condition 1: %1
echo Condition 2: %2
echo Appendix?  : %3
echo Ratio?  : %4

Rscript --encoding=UTF-8 calculateClusters.r

Rscript --encoding=UTF-8 createDiagrams.R report.csv pdf %3 %4 %1 %2

rem The delete option is important at the development phase
rem del /s report.aux
rem del /s report.out
rem del /s report.toc

pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode

rem cmd
