@echo off
cd /d %~dp0
echo Current directory:
cd

IF "%~1" neq "" (
SET Treat=%1
) ELSE (
SET Treat=Treatment
)

if "%~2" neq "" (
SET Sec=%2
) ELSE (
SET Sec=""
)

:skript
Rscript createDiagramOneFile.r report.csv png FALSE %Treat% %Sec%
Rscript createDiagramOneFile.r report.csv png TRUE %Treat% %Sec%
pdflatex report2.tex