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
 * ---------------------
 * DemoResources_fr.java
 * ---------------------
 * (C) Copyright 2002-2004, by Anthony Boulestreau.
 * Original Author: Anthony Boulestreau;
 * Contributor(s): -;
 * $Id: DemoResources_fr.java,v 1.1 2011-01-31 09:02:41 klukas Exp $
 * Changes
 * -------
 * 26-Mar-2002 : Version 1 (AB);
 * 24-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo.resources;

import java.util.ListResourceBundle;

/**
 * Localised resources for France.
 * 
 * @author AB
 */
public class DemoResources_fr extends ListResourceBundle {

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
			{ "about.title", "A propos de..." },
						{ "about.version.label", "Version" },

						// menu labels...
			{ "menu.file", "Fichier" },
						{ "menu.file.mnemonic", new Character('F') },

						{ "menu.file.exit", "Sortie" },
						{ "menu.file.exit.mnemonic", new Character('x') },

						{ "menu.help", "Aide" },
						{ "menu.help.mnemonic", new Character('H') },

						{ "menu.help.about", "A propos de..." },
						{ "menu.help.about.mnemonic", new Character('A') },

						// dialog messages...
			{ "dialog.exit.title", "Confirmation de fermeture..." },
						{ "dialog.exit.message", "Etes vous certain de vouloir sortir?" },

						// labels for the tabs in the main window...
			{ "tab.bar", "Diagrammes en Barre" },
						{ "tab.pie", "Diagrammes en Secteur" },
						{ "tab.xy", "Diagrammes XY" },
						{ "tab.time", "Diagrammes de S�ries Temporelles" },
						{ "tab.other", "Autres Diagrammes" },
						{ "tab.test", "Diagrammes de Test" },
						{ "tab.combined", "Diagrammes Combin�s" },

						// sample chart descriptions...
			{ "chart1.title", "Diagramme en Barre Horizontale: " },
						{ "chart1.description", "Affiche des barre horizontale � partir des donn�es "
											+ "d'un CategoryDataset. Remarquez que l'axe num�rique est invers�." },

						{ "chart2.title", "Diagramme en Barre Empil�e Horizontale: " },
						{ "chart2.description", "Affiche des barres empil�es horizontales � partir des donn�es "
											+ "d'un CategoryDataset." },

						{ "chart3.title", "Diagramme en Barre Verticale: " },
						{ "chart3.description",
											"Affiche des barres verticales � partir des donn�es d'un CategoryDataset." },

						{ "chart4.title", "Diagramme en Barre 3D Verticale: " },
						{ "chart4.description",
											"Affiche des barres verticales avec un effet 3D � partir des donn�es "
																+ "d'un CategoryDataset." },

						{ "chart5.title", "Diagramme en Barre Empil�e Verticale: " },
						{ "chart5.description", "Affiche des barres empil�es verticale � partir des donn�es "
											+ "d'un CategoryDataset." },

						{ "chart6.title", "Diagramme en Barre 3D Empil�e Verticale: " },
						{ "chart6.description",
											"Affiche des barres empil�es verticale avec un effet 3D � partir des donn�es "
																+ "d'un CategoryDataset." },

						{ "chart7.title", "Diagrammes en Secteur 1: " },
						{ "chart7.description", "Un diagramme en secteur avec une section �clat�e." },

						{ "chart8.title", "Diagrammes en Secteur 2: " },
						{ "chart8.description",
											"Un diagramme en secteur montrant des pourcentages sur les labels de cat�gories. De plus, "
																+ "ce graphique a une image de fond." },

						{ "chart9.title", "Trac� XY: " },
						{ "chart9.description",
											"Un diagramme en ligne � partir de donn�es d'un XYDataset. Les deux axes sont "
																+ "num�riques." },

						{ "chart10.title", "S�rie Temporelle 1: " },
						{ "chart10.description",
											"Un diagramme de s�ries temporelles � partir de donn�es d'un XYDataset. Ce "
																+ "diagramme montre de plus l'utilisation de plusieurs titres de diagramme." },

						{ "chart11.title", "S�rie Temporelle 2: " },
						{ "chart11.description",
											"Un diagramme de s�ries temporelles � partir de donn�es d'un XYDataset. "
																+ "L'axe vertical poss�de une �chelle logarithmique." },

						{ "chart12.title", "S�rie Temporelle 3: " },
						{ "chart12.description", "Un diagramme de s�ries temporelles avec une moyenne mobile." },

						{ "chart13.title", "Diagramme Max/Min/Ouverture/Fermeture: " },
						{ "chart13.description",
											"Un diagramme max/min/ouverture/fermeture bas� sur les donn�es d'un HighLowDataset." },

						{ "chart14.title", "Diagramme en Chandelier: " },
						{ "chart14.description",
											"Un diagramme en Chandelier bas� sur les donn�es d'un HighLowDataset." },

						{ "chart15.title", "Diagramme en Signal: " },
						{ "chart15.description", "Diagramme en signal bas� sur les donn�es d'un SignalDataset." },

						{ "chart16.title", "Trac� de Vents: " },
						{ "chart16.description",
											"Un trac� de vents, repr�sente la direction et l'intensit� du vent (fourni "
																+ "par l'interm�diaire d'un WindDataset)." },

						{ "chart17.title", "Nuage de points: " },
						{ "chart17.description", "Un nuage de points � partir des donn�es d'un XYDataset." },

						{ "chart18.title", "Diagramme en Ligne: " },
						{ "chart18.description",
											"Un diagramme affichant des lignes ou des formes � partir des donn�es "
																+ "d'un CategoryDataset. Ce trac� montre de plus l'utilisation "
																+ "d'une image de fond sur le diagramme, et de l'alpha-transparence sur le "
																+ "trac�." },

						{ "chart19.title", "Diagramme en Barre XY Verticale: " },
						{ "chart19.description", "Un diagramme avec des barres verticales, bas� sur des donn�es "
											+ "d'un IntervalXYDataset." },

						{ "chart20.title", "Donn�es Nulles: " },
						{ "chart20.description", "Diagramme � partir d'un ensemble de donn�es nulles." },

						{ "chart21.title", "Donn�es Z�ros: " },
						{ "chart21.description",
											"Diagramme � partir d'un ensemble de donn�es contenant des s�ries de z�ros." },

						{ "chart22.title", "Diagramme dans un JScrollPane: " },
						{ "chart22.description", "Un diagramme ins�r� dans un JScrollPane." },

						{ "chart23.title", "Diagramme en Barre avec S�rie Unique: " },
						{ "chart23.description",
											"Un diagramme en barre avec s�rie unique. Ce diagramme montre de plus l'utilisation "
																+ "d'une bordure autour d'un ChartPanel." },

						{ "chart24.title", "Diagramme dynamique: " },
						{ "chart24.description",
											"Un diagramme dynamique, pour tester le m�canisme de notification des �v�nements." },

						{ "chart25.title", "Diagramme Superpos�: " },
						{ "chart25.description",
											"Affiche un diagramme superpos� d'un trac� max/min/ouverture/fermeture et "
																+ "de moyenne mobile." },

						{ "chart26.title", "Diagramme Combin� Horizontalement: " },
						{ "chart26.description",
											"Affiche un diagramme combin� horizontalement d'un trac� de s�ries temporelles et "
																+ "de barres XY." },

						{ "chart27.title", "Diagramme Combin� Verticalement: " },
						{ "chart27.description",
											"Affiche un diagramme combin� verticalement d'un trac� XY, de s�ries temporelles et "
																+ "de barres XY vertical." },

						{ "chart28.title", "Diagramme Combin� et Superpos�: " },
						{ "chart28.description",
											"Un diagramme combin� d'un trac� XY, d'un trac� superpos� de deux s�ries temporelles et "
																+ "d'un trac� superpos� d'une s�rie temporelle et d'un max/min/ouverture/fermeture." },

						{ "chart29.title", "Diagramme Dynamique Combin� et Superpos�: " },
						{ "chart29.description",
											"Affiche un diagramme dynamique combin� et superpos�, pour tester le m�canisme "
																+ "de notification des �v�nements." },

						{ "charts.display", "Affiche" },

						// chart titles and labels...
			{ "bar.horizontal.title", "Diagramme en Barre Horizontale" },
						{ "bar.horizontal.domain", "Cat�gories" },
						{ "bar.horizontal.range", "Valeur" },

						{ "bar.horizontal-stacked.title", "Diagramme en Barre Empil�e Horizontale" },
						{ "bar.horizontal-stacked.domain", "Cat�gories" },
						{ "bar.horizontal-stacked.range", "Valeur" },

						{ "bar.vertical.title", "Diagramme en Barre Verticale" },
						{ "bar.vertical.domain", "Cat�gories" },
						{ "bar.vertical.range", "Valeur" },

						{ "bar.vertical3D.title", "Diagramme en Barre 3D Verticale" },
						{ "bar.vertical3D.domain", "Cat�gories" },
						{ "bar.vertical3D.range", "Valeur" },

						{ "bar.vertical-stacked.title", "Diagramme en Barre Empil�e Verticale" },
						{ "bar.vertical-stacked.domain", "Cat�gories" },
						{ "bar.vertical-stacked.range", "Valeur" },

						{ "bar.vertical-stacked3D.title", "Diagramme en Barre 3D Empil�e Verticale" },
						{ "bar.vertical-stacked3D.domain", "Cat�gories" },
						{ "bar.vertical-stacked3D.range", "Valeur" },

						{ "pie.pie1.title", "Diagramme en Secteur 1" },

						{ "pie.pie2.title", "Diagramme en Secteur 2" },

						{ "xyplot.sample1.title", "Trac� XY" },
						{ "xyplot.sample1.domain", "Valeurs X" },
						{ "xyplot.sample1.range", "Valeurs Y" },

						{ "timeseries.sample1.title", "Diagramme de S�ries Temporelles 1" },
						{ "timeseries.sample1.subtitle", "Valeur du GBP pour le JPY" },
						{ "timeseries.sample1.domain", "Date" },
						{ "timeseries.sample1.range", "CCY par GBP" },
						{ "timeseries.sample1.copyright", "(C)opyright 2002, by Object Refinery Limited" },

						{ "timeseries.sample2.title", "Diagramme de S�ries Temporelles 2" },
						{ "timeseries.sample2.domain", "Milliseconde" },
						{ "timeseries.sample2.range", "Axes Logarithmique" },
						{ "timeseries.sample2.subtitle", "Millisecondes" },

						{ "timeseries.sample3.title", "Diagramme de S�ries Temporelles avec Moyenne Mobile" },
						{ "timeseries.sample3.domain", "Date" },
						{ "timeseries.sample3.range", "CCY par GBP" },
						{ "timeseries.sample3.subtitle", "Moyenne mobile sur 30 jour du GBP" },

						{ "timeseries.highlow.title", "Diagramme Max/Min/Ouverture/Fermeture" },
						{ "timeseries.highlow.domain", "Date" },
						{ "timeseries.highlow.range", "Prix ($ par action)" },
						{ "timeseries.highlow.subtitle", "Prix des actions IBM" },

						{ "timeseries.candlestick.title", "Diagramme en Chandelier" },
						{ "timeseries.candlestick.domain", "Date" },
						{ "timeseries.candlestick.range", "Prix ($ par action)" },
						{ "timeseries.candlestick.subtitle", "Prix des actions IBM" },

						{ "timeseries.signal.title", "Diagramme en Signal" },
						{ "timeseries.signal.domain", "Date" },
						{ "timeseries.signal.range", "Prix ($ par action)" },
						{ "timeseries.signal.subtitle", "Prix des actions IBM" },

						{ "other.wind.title", "Trac� de Vents" },
						{ "other.wind.domain", "Axe X" },
						{ "other.wind.range", "Axe Y" },

						{ "other.scatter.title", "Nuage de Points" },
						{ "other.scatter.domain", "Axe X" },
						{ "other.scatter.range", "Axe Y" },

						{ "other.line.title", "Diagramme en Ligne" },
						{ "other.line.domain", "Cat�gorie" },
						{ "other.line.range", "Valeur" },

						{ "other.xybar.title", "Diagramme en Barre de S�ries Temporelles" },
						{ "other.xybar.domain", "Date" },
						{ "other.xybar.range", "Valeur" },

						{ "test.null.title", "Trac� XY (donn�es nulle)" },
						{ "test.null.domain", "X" },
						{ "test.null.range", "Y" },

						{ "test.zero.title", "Trac� XY (donn�es z�ros)" },
						{ "test.zero.domain", "Axe X" },
						{ "test.zero.range", "Axe Y" },

						{ "test.scroll.title", "S�rie Temporelle" },
						{ "test.scroll.subtitle", "Valeur du GBP" },
						{ "test.scroll.domain", "Date" },
						{ "test.scroll.range", "Valeur" },

						{ "test.single.title", "Diagramme en Barre avec S�rie Unique" },
						{ "test.single.subtitle1", "Sous-titre 1" },
						{ "test.single.subtitle2", "Sous-titre 2" },
						{ "test.single.domain", "Date" },
						{ "test.single.range", "Valeur" },

						{ "test.dynamic.title", "Diagramme dynamique" },
						{ "test.dynamic.domain", "Domaine" },
						{ "test.dynamic.range", "Interval" },

						{ "combined.overlaid.title", "Diagramme Superpos�" },
						{ "combined.overlaid.subtitle", "Max/Min/Ouverture/Fermeture plus Moyenne Mobile" },
						{ "combined.overlaid.domain", "Date" },
						{ "combined.overlaid.range", "IBM" },

						{ "combined.horizontal.title", "Diagramme Combin� Horizontalement" },
						{ "combined.horizontal.subtitle", "S�ries Temporelles et Diagrammes en Barres XY" },
						{ "combined.horizontal.domains", new String[] { "Date 1", "Date 2", "Date 3" } },
						{ "combined.horizontal.range", "CCY par GBP" },

						{ "combined.vertical.title", "Diagramme Combin� Verticalement" },
						{ "combined.vertical.subtitle", "Quatre diagramme en un" },
						{ "combined.vertical.domain", "Date" },
						{ "combined.vertical.ranges", new String[] { "CCY par GBP", "Pounds", "IBM", "Barres" } },

						{ "combined.combined-overlaid.title", "Diagramme Combin� et Superpos�" },
						{ "combined.combined-overlaid.subtitle", "XY, Superpos� (2 TimeSeriess) et Superpos� "
											+ "(HighLow et TimeSeries)" },
						{ "combined.combined-overlaid.domain", "Date" },
						{ "combined.combined-overlaid.ranges", new String[] { "CCY par GBP", "Pounds", "IBM" } },

						{ "combined.dynamic.title", "Diagramme Dynamique Combin�" },
						{ "combined.dynamic.subtitle", "XY (s�ries 0), XY (s�ries 1), Superpos� (les deux s�ries) "
											+ "et XY (les deux s�ries)" },
						{ "combined.dynamic.domain", "X" },
						{ "combined.dynamic.ranges", new String[] { "Y1", "Y2", "Y3", "Y4" } },

	};

}
