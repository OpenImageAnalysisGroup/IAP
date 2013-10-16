/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ------------------
 * DemoResources.java
 * ------------------
 * (C) Copyright 2002-204, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * Polish translation: Krzysztof Paz (kpaz@samorzad.pw.edu.pl);
 * Fixed char encoding: Piotr Bzdyl (piotr@geek.krakow.pl);
 * $Id: DemoResources_pl.java,v 1.1 2011-01-31 09:02:41 klukas Exp $
 * Changes
 * -------
 * 15-Mar-2002 : Version 1 (DG);
 * 26-Mar-2002 : Changed name from JFreeChartDemoResources.java --> DemoResources.java (DG);
 */
package org.jfree.chart.demo.resources;

import java.util.ListResourceBundle;

/**
 * A resource bundle that stores all the user interface items that might need localisation.
 * 
 * @author KP
 */
public class DemoResources_pl extends ListResourceBundle {

	/**
	 * Returns the array of strings in the resource bundle.
	 * 
	 * @return the resources.
	 */
	public Object[][] getContents() {
		return CONTENTS;
	}

	/** The resources to be localised. */
	private static final Object[][] CONTENTS = {

	// about frame...
			{ "about.title", "Informacja o..." },
						{ "about.version.label", "Wersja" },

						// menu labels...
			{ "menu.file", "Plik" },
						{ "menu.file.mnemonic", new Character('P') },

						{ "menu.file.exit", "Zako\u0144cz" },
						{ "menu.file.exit.mnemonic", new Character('K') },

						{ "menu.help", "Pomoc" },
						{ "menu.help.mnemonic", new Character('C') },

						{ "menu.help.about", "O programie..." },
						{ "menu.help.about.mnemonic", new Character('A') },

						// dialog messages...
			{ "dialog.exit.title", "Potwierd\u017a zamkni\u0119cie..." },
						{ "dialog.exit.message",
											"Czy jeste\u015b pewien, \u017ce chcesz zako\u0144czy\u0107 program?" },

						// labels for the tabs in the main window...
			{ "tab.bar", "Wykresy Kolumnowe i S\u0142upkowe" },
						{ "tab.pie", "Wykresy Ko\u0142owe" },
						{ "tab.xy", "Wykresy XY" },
						{ "tab.time", "Wykresy Liniowe" },
						{ "tab.other", "Wykresy Inne" },
						{ "tab.test", "Wykresy Testowe" },
						{ "tab.combined", "Wykresy Niestandardowe" },

						// sample chart descriptions...
			{ "chart1.title", "S\u0142upkowy grupowany: " },
						{ "chart1.description",
											"Wy\u015bwietla poziome s\u0142upki, por\u00f3wnuje zgrupowane warto\u015bci  "
																+ "dla r\u00f3\u017cnych kategorii.  Uwaga: skala na osi poziomej jest odwr\u00f3cona."
						},

						{ "chart2.title", "S\u0142upkowy skumulowany: " },
						{ "chart2.description",
											"Wy\u015bwietla poziome s\u0142upki, por\u00f3wnuje wk\u0142ad poszczeg\u00f3lnych "
																+ "warto\u015bci do sumy dla r\u00f3\u017cnych kategorii." },

						{ "chart3.title", "Kolumnowy grupowany: " },
						{ "chart3.description",
											"Wy\u015bwietla pionowe kolumny, por\u00f3wnuje zgrupowane warto\u015bci dla "
																+ "r\u00f3\u017cnych kategorii." },

						{ "chart4.title", "Kolumnowy grupowany z efektem 3-W: " },
						{ "chart4.description", "Wy\u015bwietla pionowe kolumny z efektem 3-W,  "
											+ "por\u00f3wnuje zgrupowane warto\u015bci dla r\u00f3\u017cnych kategorii" },

						{ "chart5.title", "Kolumnowy skumulowany: " },
						{ "chart5.description", "Wy\u015bwietla pionowe kolumny, "
											+ "por\u00f3wnuje skumulowane warto\u015bci dla r\u00f3\u017cnych kategorii." },

						{ "chart6.title", "Kolumnowy skumulowany z efektem 3-W: " },
						{ "chart6.description", "Wy\u015bwietla pionowe kolumny z efektem 3-W,  "
											+ "por\u00f3wnuje skumulowane warto\u015bci dla r\u00f3\u017cnych kategorii." },

						{ "chart7.title", "Ko\u0142owy wysuni\u0119ty: " },
						{ "chart7.description",
											"Wy\u015bwietla wk\u0142ad poszczeg\u00f3lnych warto\u015bci do sumy "
																+ "ca\u0142kowitej, podkre\u015blaj\u0105c jedn\u0105 z "
																+ "warto\u015bci poprzez wysuni\u0119cie." },

						{ "chart8.title", "Ko\u0142owy tradycyjny: " },
						{ "chart8.description",
											"Wy\u015bwietla procentowy wk\u0142ad poszczeg\u00f3lnych warto\u015bci do sumy "
																+ "ca\u0142kowitej, ponadto wykres ma przyk\u0142adowy obrazek w tle." },

						{ "chart9.title", "XY Punktowy: " },
						{ "chart9.description", "Wykres punktowy, z punktami danych po\u0142\u0105czonymi "
											+ "wyg\u0142adzonymi liniami bez znacznik\u00f3w danych." },

						{ "chart10.title", "Liniowy 1: " },
						{ "chart10.description",
											"Wykres liniowy - wy\u015bwietla trend w czasie lub dla r\u00f3\u017cnych"
																+ " kategorii danych XY. "
																+ "Ponadto demonstruje u\u017cycie wielu etykiet/nazw na jednym wykresie." },

						{ "chart11.title", "Liniowy 2: " },
						{ "chart11.description",
											"Wykres liniowy - wy\u015bwietla trend w czasie lub dla r\u00f3\u017cnych"
																+ " kategorii danych XY. "
																+ "O\u015b pionowa jest wyskalowana logarytmicznie." },

						{ "chart12.title", "Liniowy 3: " },
						{ "chart12.description",
											"Wykres liniowy - wy\u015bwietla trend w czasie lub dla r\u00f3\u017cnych "
																+ "kategorii danych XY ze wskazaniem zmian warto\u015bci u\u015brednionej ." },

						{ "chart13.title", "Gie\u0142dowy - Liniowy: Max/Min/Otwarcie/Zamkni\u0119cie " },
						{ "chart13.description",
											"Wykres gie\u0142dowy typu Max/Min/Otwarcie/Zamkni\u0119cie oparty o dane "
																+ "HighLowDataset(serie warto\u015bci podawane w odpowiedniej kolejno\u015bci)." },

						{ "chart14.title", "Gie\u0142dowy - Candlestick: Max/Min/Otwarcie/Zamkni\u0119cie: " },
						{ "chart14.description",
											"Wykres gie\u0142dowy typu Candlestick (Max/Min/Otwarcie/Zamkni\u0119cie) "
																+ "oparty o dane HighLowDataset(serie warto\u015bci podawane w odpowiedniej "
																+ "kolejno\u015bci)." },

						{ "chart15.title", "Sygna\u0142owy: " },
						{ "chart15.description", "Wykres sygna\u0142owy oparty o dane z SignalDataset." },

						{ "chart16.title", "Wiatrowy: " },
						{ "chart16.description",
											"Ilustracja graficzna wiatru, przedstawiaj\u0105ca jego kierunek i si\u0142\u0119 "
																+ "(reprezentowan\u0105 w WindDataset)." },

						{ "chart17.title", "Rozproszony punktowy: " },
						{ "chart17.description",
											"Wykres punktowy, rozproszony przedstawiaj\u0105cy dane w uk\u0142adzie XY z XYDataset."
						},

						{ "chart18.title", "Liniowy: " },
						{ "chart18.description",
											"Wykres wy\u015bwielta linie i/lub kszta\u0142ty, przedstawiaj\u0105ce dane z "
																+ "CategoryDataset. "
																+ "Ponadto ilustruje u\u017cycie obrazka w tle wykresu oraz "
																+ "przezroczysto\u015bci alpha "
																+ "na rysunku." },

						{ "chart19.title", "Pionowy XY kolumnowy: " },
						{ "chart19.description", "Wykres prezentuje pionowe s\u0142upki oparte na "
											+ "IntervalXYDataset." },

						{ "chart20.title", "Puste dane: " },
						{ "chart20.description", "Wykres dla braku danych (null dataset)." },

						{ "chart21.title", "Dane zero: " },
						{ "chart21.description", "Wykres dla serii zer w danych." },

						{ "chart22.title", "Liniowy z JScrollPane: " },
						{ "chart22.description",
											"Wykres liniowy osadzony w komponencie JScrollPane pozwalaj\u0105cym na przewijanie "
																+ "obszaru wykresu wewn\u0105trz okna gdy jest ono za ma\u0142e." },

						{ "chart23.title", "Kolumnowy dla jednej serii: " },
						{ "chart23.description", "Wykres kolumnowy dla jednej serii danych. "
											+ "Demonstruje przy okazji \u017cycie ramki w ChartPanel." },

						{ "chart24.title", "Wykres dynamiczy: " },
						{ "chart24.description", "Dynamiczny (rysowany na bie\u017c\u0105co) wykres do testowania "
											+ "mechanizmu zdarze\u0144 (event notification mechanism)." },

						{ "chart25.title", "Nak\u0142adany gie\u0142dowy: Max/Min/Otwarcie/Zamkni\u0119cie: " },
						{ "chart25.description",
											"Wyswietla wykres nak\u0142adany gie\u0142dowy: Max/Min/Otwarcie/Zamkni\u0119cie z "
																+ "ilustracj\u0105 przebiegu \u015bredniej." },

						{ "chart26.title", "Poziomy - kombinowany: " },
						{ "chart26.description",
											"Wy\u015bwietla 3 r\u00f3\u017cne poziome wykresy liniowe /czasowe i XY kolumnowy." },

						{ "chart27.title", "Pionowy - kombinowany: " },
						{ "chart27.description",
											"Wy\u015bwietla 4 r\u00f3\u017cne wykresy umo\u017cliwiaj�ce por\u00f3wnanie danych "
																+ "w pionie na jednym rysunku "
																+ "dla XY, liniowe /czasowe oraz kolumn pionowych XY." },

						{ "chart28.title", "Kombinowany i nak\u0142adany: " },
						{ "chart28.description",
											"Kombinowany wykres XY, nak\u0142adany liniowy/TimeSeries i nak\u0142adany "
																+ "Max/Min & liniowy." },

						{ "chart29.title", "Kombinowany i nak\u0142adany dynamiczny: " },
						{ "chart29.description",
											"Wy\u015bwietla kombinowany i nak\u0142adany wykres dynamiczny w celu "
																+ "testowania / ilustracji mechnizmu obs\u0142ugi zdarze\u0144." },

						{ "charts.display", "Poka\u017c" },

						// chart titles and labels...
			{ "bar.horizontal.title", "Poziomy wykres s\u0142upkowy" },
						{ "bar.horizontal.domain", "Kategorie" },
						{ "bar.horizontal.range", "Warto\u015bci" },

						{ "bar.horizontal-stacked.title", "Poziomy, skumulowany wykres s\u0142upkowy" },
						{ "bar.horizontal-stacked.domain", "Kategorie" },
						{ "bar.horizontal-stacked.range", "Warto\u015bci" },

						{ "bar.vertical.title", "Pionowy wykres kolumnowy" },
						{ "bar.vertical.domain", "Kategorie" },
						{ "bar.vertical.range", "Warto\u015bci" },

						{ "bar.vertical3D.title", "Pionowy wykres kolumnowy z efektem 3-W" },
						{ "bar.vertical3D.domain", "Kategorie" },
						{ "bar.vertical3D.range", "Warto\u015bci" },

						{ "bar.vertical-stacked.title", "Pionowy, skumulowany wykres kolumnowy" },
						{ "bar.vertical-stacked.domain", "Kategorie" },
						{ "bar.vertical-stacked.range", "Warto\u015bci" },

						{ "bar.vertical-stacked3D.title", "Pionowy, skumulowany wykres kolumnowy z efektem 3-W" },
						{ "bar.vertical-stacked3D.domain", "Kategorie" },
						{ "bar.vertical-stacked3D.range", "Warto\u015bci" },

						{ "pie.pie1.title", "Wykres ko\u0142owy 1 - wysuni\u0119ty" },

						{ "pie.pie2.title", "Wykres ko\u0142owy 2 - tradycyjny" },

						{ "xyplot.sample1.title", "Wykres XY Punktowy" },
						{ "xyplot.sample1.domain", "X Warto\u015bci" },
						{ "xyplot.sample1.range", "Y Warto\u015bci" },

						{ "timeseries.sample1.title", "Wykres liniowy przebiegu kursu w czasie - 1" },
						{ "timeseries.sample1.subtitle", "Warto\u015bci PLN in JPY" },
						{ "timeseries.sample1.domain", "Data" },
						{ "timeseries.sample1.range", "CCY na z\u0142ot\u00f3wk\u0119" },
						{ "timeseries.sample1.copyright", "(C)opyright 2002, by Krzysztof Pa\u017a, PW" },

						{ "timeseries.sample2.title", "Liniowy 2" },
						{ "timeseries.sample2.domain", "Millisekundy" },
						{ "timeseries.sample2.range", "O\u015b logarytmiczna" },
						{ "timeseries.sample2.subtitle", "Millisekundy" },

						{ "timeseries.sample3.title", "Liniowy z ruchomym trendem u\u015brednionym" },
						{ "timeseries.sample3.domain", "Data" },
						{ "timeseries.sample3.range", "CCY na PLN" },
						{ "timeseries.sample3.subtitle", "30 dniowy \u015bredni przebieg kursu PLN" },

						{ "timeseries.highlow.title", "Gie\u0142dowy wykres Max/Min/Otwarcie/Zamkni\u0119cie " },
						{ "timeseries.highlow.domain", "Data" },
						{ "timeseries.highlow.range", "Cena (PLN za udzia\u0142)" },
						{ "timeseries.highlow.subtitle", "Warto\u015b\u0107 akcji TPSA" },

						{ "timeseries.candlestick.title", "Gie\u0142dowy CandleStick" },
						{ "timeseries.candlestick.domain", "Data" },
						{ "timeseries.candlestick.range", "Cena (PLN za udzia\u0142)" },
						{ "timeseries.candlestick.subtitle", "Warto\u015b\u0107 akcji JTT" },

						{ "timeseries.signal.title", "Wykres sygna\u0142owy" },
						{ "timeseries.signal.domain", "Data" },
						{ "timeseries.signal.range", "Cena (PLN za udzia\u0142)" },
						{ "timeseries.signal.subtitle", "Warto\u015b\u0107 akcji OPTIMUS S.A." },

						{ "other.wind.title", "Wykres wiatru" },
						{ "other.wind.domain", "O\u015b X" },
						{ "other.wind.range", "O\u015b Y" },

						{ "other.scatter.title", "Rozrzucony punktowy" },
						{ "other.scatter.domain", "O\u015b X" },
						{ "other.scatter.range", "O\u015b Y" },

						{ "other.line.title", "Liniowy" },
						{ "other.line.domain", "Kategoria" },
						{ "other.line.range", "Warto\u015b\u0107" },

						{ "other.xybar.title", "Liniowy kolumnowy" },
						{ "other.xybar.domain", "Data" },
						{ "other.xybar.range", "Warto\u015b\u0107" },

						{ "test.null.title", "Wykres XY (null data)" },
						{ "test.null.domain", "X" },
						{ "test.null.range", "Y" },

						{ "test.zero.title", "Wykres XY (zero data)" },
						{ "test.zero.domain", "O\u015b X" },
						{ "test.zero.range", "O\u015b Y" },

						{ "test.scroll.title", "Liniowy / Time Series" },
						{ "test.scroll.subtitle", "Warto\u015b\u0107 PLN" },
						{ "test.scroll.domain", "Data" },
						{ "test.scroll.range", "Warto\u015b\u0107" },

						{ "test.single.title", "Pojedyncza seria" },
						{ "test.single.subtitle1", "Podtytu\u0142 1" },
						{ "test.single.subtitle2", "Podtytu\u0142 2" },
						{ "test.single.domain", "Data" },
						{ "test.single.range", "Warto\u015b\u0107" },

						{ "test.dynamic.title", "Wykres Dynamiczny" },
						{ "test.dynamic.domain", "Domena" },
						{ "test.dynamic.range", "Zasi\u0119g" },

						{ "combined.overlaid.title", "Wykres Nak\u0142adany" },
						{ "combined.overlaid.subtitle",
											"Max/Min/Otwarcie/Zamkni\u0119cie z ilustracj\u0105 przebiegu \u015bredniej." },
						{ "combined.overlaid.domain", "Data" },
						{ "combined.overlaid.range", "OPTIMUS S.A." },

						{ "combined.horizontal.title", "Wykres poziomo kombinowany" },
						{ "combined.horizontal.subtitle", "Linowy / Time Series s\u0142upkowy XY " },
						{ "combined.horizontal.domains", new String[] { "Dane 1", "Dane 2", "Dane 3" } },
						{ "combined.horizontal.range", "CCY na PLN" },

						{ "combined.vertical.title", "Wykres pionowo kombinowany" },
						{ "combined.vertical.subtitle", "Cztery wykresy na jednym" },
						{ "combined.vertical.domain", "Data" },
						{ "combined.vertical.ranges",
											new String[] { "CCY na PLN", "Z\u0142ot\u00f3wki", "KGHM", "S\u0142upki" } },

						{ "combined.combined-overlaid.title", "Wykres kombinowany i nak\u0142adany" },
						{ "combined.combined-overlaid.subtitle",
											"XY, mnak\u0142adany (dwie TimeSeries) i nak\u0142adany "
																+ "(Max/Min i TimeSeries)" },
						{ "combined.combined-overlaid.domain", "Data" },
						{ "combined.combined-overlaid.ranges",
											new String[] { "CCY na PLN", "Z\u0142ot\u00f3wki", "TPSA" } },

						{ "combined.dynamic.title", "Wykres poziomo kombinowany - dynamiczny" },
						{ "combined.dynamic.subtitle", "XY (seria 0), XY (seria 1), nak\u0142adany (obie serie) "
													+ "oraz XY (obie serie)" },
						{ "combined.dynamic.domain", "X" },
						{ "combined.dynamic.ranges", new String[] { "Y1", "Y2", "Y3", "Y4" } },

	};

}
