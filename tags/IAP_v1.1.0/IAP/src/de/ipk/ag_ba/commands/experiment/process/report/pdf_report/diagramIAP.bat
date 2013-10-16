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
shift /8
echo Check package version?  : %9
shift /8
echo Install new package?  : %9
shift /8
echo Update package?: %9
shift /8
echo Debug?: %9
shift /8
echo Catch error?: %9


rem The delete option is important at the development phase
if exist report.aux del /s report.aux
if exist report.out	del /s report.out
if exist report.tex	del /s report.tex
if exist report.pdf	del /s report.pdf
if exist plots rd /s /q plots
if exist plotTex rd /s /q plotTex
if exist section rd /s /q section

IF EXIST report.clustering.csv Rscript --encoding=UTF-8 calcClusters.R %6

rem In the R Skript the first Value is report.csv so the %1 is in the R-Script args[3]
Rscript --encoding=UTF-8 createDiagrams.R report.csv pdf %*
rem Rscript --encoding=UTF-8 createMissingFiles.R

rem IF NOT EXIST noValues.pdf Rscript --encoding=UTF-8 createMissingFiles.R

pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode
pdflatex report.tex -interaction batchmode

rem cmd
