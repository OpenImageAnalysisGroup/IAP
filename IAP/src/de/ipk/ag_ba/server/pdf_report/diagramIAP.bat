@echo off
cd /d %~dp0
echo Current directory:
cd

IF "%~1" neq "" (
SET Treat=%1
) ELSE (
SET Treat=Genotype
)

if "%~2" neq "" (
SET Sec=%2
) ELSE (
SET Sec="none"
)

if "%~3" neq "" (
SET Appendix=%3
) ELSE (
SET Appendix=FALSE
)

Rscript createDiagramOneFile.r report.csv pdf %Appendix% %Treat% %Sec%

pdflatex report2.tex
pdflatex report2.tex