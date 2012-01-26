Aufgerufen wird folgendes Skript:
	createDiagramOneFile.r

Als Datenbasis wird folgende Datei verwendet:
	report.csv


Es können aktuell drei Parameter übergeben werden:
	Appendix (%Appendix%)
	Treatment (%Treat%)
	SecondTreatment (%Sec%)

Folgende Werte werden verwendet wenn gilt:

0 Parameter angegeben:

	Appendix = FALSE
	Treatment = Genotype
	SecondTreatment = none



Folgende Pakete müssen auf dem Server installiert sein, damit das R-Skript korrekt laufen kann:

	Cairo <- Bilder abspeichern
	RColorBrewer <- Farbwerte erstellen
	data.table <- effizienter Algorithmus um große Datenmengen verarbeiten zu können
	ggplot2 <- zum visualisieren der Daten

Zum installieren der Pakete folgenden Code ausführen:

	install.packages(c("Cairo"), repos="http://cran.r-project.org", dependencies = TRUE)
	install.packages(c("RColorBrewer"), repos="http://cran.r-project.org", dependencies = TRUE)
	install.packages(c("data.table"), repos="http://cran.r-project.org", dependencies = TRUE)
	install.packages(c("ggplot2"), repos="http://cran.r-project.org", dependencies = TRUE)
	
	
