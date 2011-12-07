@echo off
cd /d %~dp0
echo Current directory:
cd

IF "%~1" neq "" (
SET Treat=%1
) ELSE (
SET Treat=Condition
)

if "%~2" neq "" (
SET Sec=%2
) ELSE (
SET Sec="none"
)

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

pdflatex report2.tex