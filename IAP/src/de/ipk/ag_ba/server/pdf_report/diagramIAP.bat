@echo off
cd /d %~dp0
echo Current directory:
cd

if "%1"=="" GOTO point1
SET Treat=%1
GOTO skript

:point1
SET Treat=Treatment
GOTO skript 

:skript
Rscript diagramForReportPDF.r report.csv png "boxplotStacked" %Treat%
Rscript diagramForReportPDF.r report.csv png "!boxplot" %Treat%
Rscript diagramForReportPDF.r report.csv png "appendix" %Treat%
pdflatex report2.tex