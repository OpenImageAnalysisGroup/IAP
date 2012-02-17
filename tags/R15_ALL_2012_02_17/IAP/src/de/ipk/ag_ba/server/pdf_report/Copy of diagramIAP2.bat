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


altest Zeug

if "%~3" neq "" (

	if "%~3"=="FALSE" (
		Rscript createDiagramOneFile.r report.csv png FALSE %Treat% %Sec%
	) ELSE (
	  	Rscript createDiagramOneFile.r report.csv png FALSE %Treat% %Sec%
	  	Rscript createDiagramOneFile.r report.csv png TRUE %Treat% %Sec%
	)
) ELSE (
Rscript createDiagramOneFile.r report.csv png FALSE %Treat% %Sec%
)