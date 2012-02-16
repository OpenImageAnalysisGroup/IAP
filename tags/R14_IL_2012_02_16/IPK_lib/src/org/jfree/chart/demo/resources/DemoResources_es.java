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
 * DemoResources_es.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors;
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Hans-Jurgen Greiner;
 * $Id: DemoResources_es.java,v 1.1 2011-01-31 09:02:41 klukas Exp $
 * Changes
 * -------
 * 26-Mar-2002 : Version 1, translation by Hans-Jurgen Greiner (DG);
 * 24-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart.demo.resources;

import java.util.ListResourceBundle;

/**
 * A resource bundle that stores all the user interface items that might need localisation.
 * 
 * @author HJG
 */
public class DemoResources_es extends ListResourceBundle {

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
			{ "about.title", "Acerca..." },
						{ "about.version.label", "Versi�n" },

						// menu labels...
			{ "menu.file", "Archivo" },
						{ "menu.file.mnemonic", new Character('F') },

						{ "menu.file.exit", "Salida" },
						{ "menu.file.exit.mnemonic", new Character('x') },

						{ "menu.help", "Ayuda" },
						{ "menu.help.mnemonic", new Character('H') },

						{ "menu.help.about", "Acerca..." },
						{ "menu.help.about.mnemonic", new Character('A') },

						// dialog messages...
			{ "dialog.exit.title", "Confirme salida..." },
						{ "dialog.exit.message", "Estas seguro que quieres salir?" },

						// labels for the tabs in the main window...
			{ "tab.bar", "Gr�fico de barras" },
						{ "tab.pie", "Gr�fico circular" },
						{ "tab.xy", "XY Gr�ficos" },
						{ "tab.time", "Gr�fico de la serie de  tiempo" },
						{ "tab.other", "Otros gr�ficos" },
						{ "tab.test", "Gr�ficos de examen" },
						{ "tab.combined", "Gr�ficos combinados" },

						// sample chart descriptions...
			{ "chart1.title", "Gr�fico de barras horizontales: " },
						{ "chart1.description", "Muestra barras horizontales, representando data desde a "
											+ "Categor�a dataset (grupo data).  Preste atenci�n que el eje "
											+ "num�rico esta invertido." },

						{ "chart2.title", "Gr�fico con pilas de barras horizontales: " },
						{ "chart2.description", "muestra gr�fico con pilas de barras horizontales,  "
											+ "representando data desde a "
											+ "Categor�a dataset (grupo data)." },

						{ "chart3.title", "Gr�fico con barras verticales: " },
						{ "chart3.description", "Muestra barras verticales, representando data "
											+ "de una categor�a dataset (grupo data)." },

						{ "chart4.title", "Gr�fico de barra vertical en 3D: " },
						{ "chart4.description", "muestra  barras verticales con un efecto de 3D, "
											+ "representando data desde a "
											+ "Categor�a dataset (grupo data)." },

						{ "chart5.title", "Gr�fico con pilas de barras verticales: " },
						{ "chart5.description", "muestra gr�fico con pilas de barras verticales, "
											+ "representando data desde a "
											+ "Categor�a dataset (grupo data)." },

						{ "chart6.title", "Gr�fico con pilas de barras en 3D: " },
						{ "chart6.description", "Muestra pila de  barras verticales con un efecto de 3D, "
											+ "representando data de una Categor�a dataset (grupo data)." },

						{ "chart7.title", "Gr�fico circular 1: " },
						{ "chart7.description", "Un gr�fico circular mostrando una secci�n explotada." },

						{ "chart8.title", "Gr�fico circular 2: " },
						{ "chart8.description", "Un gr�fico circular mostrando porcentajes sobre los "
											+ "niveles categ�ricos.  Tambi�n, "
											+ "este plan tiene una imagen de fondo." },

						{ "chart9.title", "Plan XY: " },
						{ "chart9.description", "un gr�fico de l�nea usando data desde un grupo de data XY.  "
											+ "Ambos ejes son num�rico." },

						{ "chart10.title", "Series de tiempo 1: " },
						{ "chart10.description", "un gr�fico de series de tiempo, representando data "
											+ "desde un grupo de data XY. Este gr�fico tambi�n "
											+ "demuestra el uso de m�ltiples t�tulos gr�ficos." },

						{ "chart11.title", "Series de tiempo 2: " },
						{ "chart11.description", "Un gr�fico de series de tiempo, representando un grupo "
											+ "de data XY. Este ejes verticales tienen una escala "
											+ "logar�tmica." },

						{ "chart12.title", "Series de tiempo 3: " },
						{ "chart12.description", "Un gr�fico de serie de tiempo con un movimiento promedio." },

						{ "chart13.title", "Gr�fico Alto/Bajo/Abierto/Cerrado: " },
						{ "chart13.description", "Un gr�fico alto/bajo/abierto/cerrado basado sobre "
											+ "data en un grupo de data alto bajo." },

						{ "chart14.title", "Gr�fico de cotizaciones: " },
						{ "chart14.description", "Un gr�fico de cotizaciones basado en un grupo e data altobajo." },

						{ "chart15.title", "Gr�fico de se�al: " },
						{ "chart15.description", "Un gr�fico de se�al basado en data en un grupo de data de se�al." },

						{ "chart16.title", "Plan de viento: " },
						{ "chart16.description", "un plan de viento, representa la direcci�n del "
											+ "viento e intensidad  ( suministro a trav�s de  "
											+ "un grupo data de viento)." },

						{ "chart17.title", "Esparcir plan: " },
						{ "chart17.description", "Un plan esparcido, representando data en un grupo data XY." },

						{ "chart18.title", "Gr�fico de l�nea: " },
						{ "chart18.description", "un gr�fico mostrando l�neas y/o figuras, representando "
											+ "data en a categor�a grupo data.  Este plan tambi�n "
											+ "ilustra el uso de a imagen de fondo en el gr�fico, y "
											+ "alpha-transparency en �l plan." },

						{ "chart19.title", "Gr�fico de barra vertical XY: " },
						{ "chart19.description", "Un gr�fico mostrando barras verticales, basadas en data en un "
											+ "grupo data interval XY." },

						{ "chart20.title", "Data Nula: " },
						{ "chart20.description", "Un gr�fico con un grupo data nulo." },

						{ "chart21.title", "Cero Data: " },
						{ "chart21.description", "Un gr�fico con un grupo de data que contiene una serie de ceros." },

						{ "chart22.title", "Un gr�fico en JScrollPane: " },
						{ "chart22.description", "Un gr�fico incrustado en un JScrollPane." },

						{ "chart23.title", "Un gr�fico de barra con serie �nica: " },
						{ "chart23.description", "un gr�fico de barra con serie �nica.  "
											+ "Este gr�fico tambi�n ilustra el uso "
											+ "de un borde alrededor de ChartPanel." },

						{ "chart24.title", "Gr�fico din�mico: " },
						{ "chart24.description", "Un gr�fico din�mico, para examinar la notificaci�n del "
											+ "evento mec�nico." },

						{ "chart25.title", "Gr�fico cubierto: " },
						{ "chart25.description", "muestra un gr�fico cubierto con alto/bajo/abierto/cerrado "
											+ "y movi�ndose planes en promedio." },

						{ "chart26.title", "Gr�fico combinado horizontalmente: " },
						{ "chart26.description", "Muestra un gr�fico combinado horizontalmente de la serie "
											+ "de tiempo y una barra XY planes." },

						{ "chart27.title", "Gr�fico combinado verticalmente: " },
						{ "chart27.description", "Muestra un gr�fico combinado verticalmente de XY, "
											+ "serie de tiempo y VerticalXYBar planes." },

						{ "chart28.title", "Gr�fico combinado y cubierto: " },
						{ "chart28.description", "Un gr�fico combinado de una XY, cubierto de series de "
											+ "tiempo y uno cubierto altobajo & planes de series de tiempo." },

						{ "chart29.title", "Gr�fico din�mico combinado y cubierto: " },
						{ "chart29.description", "muestra un gr�fico din�mico combinado y cubierto, "
											+ "para examinar el notificaci�n de evento mec�nico." },

						{ "charts.display", "Muestra" },

						// chart titles and labels...
			{ "bar.horizontal.title", "Gr�fico de barra horizontal" },
						{ "bar.horizontal.domain", "Categor�as" },
						{ "bar.horizontal.range", "Valor" },

						{ "bar.horizontal-stacked.title", "Gr�fico con pilas de barras horizontales" },
						{ "bar.horizontal-stacked.domain", "Categor�as" },
						{ "bar.horizontal-stacked.range", "Valor" },

						{ "bar.vertical.title", "Gr�fico de barras verticales" },
						{ "bar.vertical.domain", "Categor�as" },
						{ "bar.vertical.range", "Valor" },

						{ "bar.vertical3D.title", "Gr�fico de barra vertical en 3D" },
						{ "bar.vertical3D.domain", "Categor�as" },
						{ "bar.vertical3D.range", "Valor" },

						{ "bar.vertical-stacked.title", "Gr�fico con pilas de barras verticales" },
						{ "bar.vertical-stacked.domain", "Categor�as" },
						{ "bar.vertical-stacked.range", "Valor" },

						{ "bar.vertical-stacked3D.title", "Gr�fico de barras verticales en 3D" },
						{ "bar.vertical-stacked3D.domain", "Categor�as" },
						{ "bar.vertical-stacked3D.range", "Valor" },

						{ "pie.pie1.title", "Gr�fico circular 1" },

						{ "pie.pie2.title", "Gr�fico circular 2" },

						{ "xyplot.sample1.title", "Plan de XY" },
						{ "xyplot.sample1.domain", "Valores de X" },
						{ "xyplot.sample1.range", "Valores de Y" },

						{ "timeseries.sample1.title", "Gr�fico con series de tiempo 1" },
						{ "timeseries.sample1.subtitle", "Valor de GBP en JPY" },
						{ "timeseries.sample1.domain", "Fecha" },
						{ "timeseries.sample1.range", "CCY por GBP" },
						{ "timeseries.sample1.copyright", "(C)opyright 2002, por Object Refinery Limited" },

						{ "timeseries.sample2.title", "Gr�fico con series de tiempo 2" },
						{ "timeseries.sample2.domain", "Milisegundo" },
						{ "timeseries.sample2.range", "Eje tronco" },
						{ "timeseries.sample2.subtitle", "Milisegundos" },

						{ "timeseries.sample3.title", "gr�fico con series de tiempo moviendo al promedio" },
						{ "timeseries.sample3.domain", "Fecha" },
						{ "timeseries.sample3.range", "CCY por GBP" },
						{ "timeseries.sample3.subtitle", "30 dias moviendo de GBP" },
						// GEEK
			{ "timeseries.highlow.title", "Gr�fico Alto/Bajo/Abierto/Cerrado" },
						{ "timeseries.highlow.domain", "Fecha" },
						{ "timeseries.highlow.range", "Precio  ($ por porci�n)" },
						{ "timeseries.highlow.subtitle", "Precio de la acci�n IBM" },

						{ "timeseries.candlestick.title", "Gr�fico de cotizaci�n" },
						{ "timeseries.candlestick.domain", "Fecha" },
						{ "timeseries.candlestick.range", "Precio  ($ por porci�n)" },
						{ "timeseries.candlestick.subtitle", "Precio de la acci�n IBM" },

						{ "timeseries.signal.title", "Gr�fico de se�al" },
						{ "timeseries.signal.domain", "Fecha" },
						{ "timeseries.signal.range", "Precio  ($ por porci�n" },
						{ "timeseries.signal.subtitle", "Precio de la acci�n IBM" },

						{ "other.wind.title", "Plan de Viento" },
						{ "other.wind.domain", "eje-X" },
						{ "other.wind.range", "eje-Y" },

						{ "other.scatter.title", "Plan Esparcido" },
						{ "other.scatter.domain", "eje-X" },
						{ "other.scatter.range", "eje-Y" },

						{ "other.line.title", "Plan de l�nea" },
						{ "other.line.domain", "Categor�a" },
						{ "other.line.range", "Valor" },

						{ "other.xybar.title", "Gr�fico con barras y series de tiempo" },
						{ "other.xybar.domain", "Fecha" },
						{ "other.xybar.range", "Valor" },

						{ "test.null.title", "Plan XY (Nula data)" },
						{ "test.null.domain", "eje-X" },
						{ "test.null.range", "eje-Y" },

						{ "test.zero.title", "Plan XY (Cero data)" },
						{ "test.zero.domain", "eje-X" },
						{ "test.zero.range", "eje-Y" },

						{ "test.scroll.title", "Series de tiempo" },
						{ "test.scroll.subtitle", "Valor of GBP" },
						{ "test.scroll.domain", "Fecha" },
						{ "test.scroll.range", "Valor" },

						{ "test.single.title", "Gr�fico de barras de series �nicas" },
						{ "test.single.subtitle1", "Subt�tulo 1" },
						{ "test.single.subtitle2", "Subt�tulo 2" },
						{ "test.single.domain", "Fecha" },
						{ "test.single.range", "Valor" },

						{ "test.dynamic.title", "Gr�fico din�mico" },
						{ "test.dynamic.domain", "Dominios" },
						{ "test.dynamic.range", "Alcance" },

						{ "combined.overlaid.title", "Gr�fico cubierto" },
						{ "combined.overlaid.subtitle", "Alto/Bajo/Abierto/Cerrado mas moviendo a promedio" },
						{ "combined.overlaid.domain", "Fecha" },
						{ "combined.overlaid.range", "IBM" },

						{ "combined.horizontal.title", "Gr�fico horizontal combinado" },
						{ "combined.horizontal.subtitle", "Series de tiempo y gr�ficos de barras XY" },
						{ "combined.horizontal.domains", new String[] { "Fecha 1", "Facha 2", "Fecha 3" } },
						{ "combined.horizontal.range", "CCY por GBP" },

						{ "combined.vertical.title", "Gr�fico vertical combinado" },
						{ "combined.vertical.subtitle", "Cuatro gr�ficos en uno" },
						{ "combined.vertical.domain", "Fecha" },
						{ "combined.vertical.ranges", new String[] { "CCY por GBP", "Libras", "IBM", "Barras" } },

						{ "combined.combined-overlaid.title", "Gr�fico combinado y cubierto" },
						{ "combined.combined-overlaid.subtitle", "XY, cubierto(dos series de tiempo) y cubierto "
																+ "(Alto Bajo y series de tiempo)" },
						{ "combined.combined-overlaid.domain", "Fecha" },
						{ "combined.combined-overlaid.ranges", new String[] { "CCY por GBP", "Libras", "IBM" } },

						{ "combined.dynamic.title", "Gr�fico din�mico combinado" },
						{ "combined.dynamic.subtitle", "XY (series 0), XY (serie 1), cubierto (ambas series)) "
													+ "y XY (ambas series)" },
						{ "combined.dynamic.domain", "X" },
						{ "combined.dynamic.ranges", new String[] { "Y1", "Y2", "Y3", "Y4" } },

	};

}
