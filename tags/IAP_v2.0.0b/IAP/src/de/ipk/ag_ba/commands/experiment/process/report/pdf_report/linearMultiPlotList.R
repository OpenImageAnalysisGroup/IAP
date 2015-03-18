###############################################################################
# Author: Entzian
###############################################################################
### section start with: 80

nBoxMultiOptions= NULL

nBoxMultiPlotList <- list(	
		
		"side.section#ndvi" = list(plotName = "side sections - NDVI", section = 80, subsection = 1, subsubsection = 1, paragraph = 1),
		"side.section#ndvi.vis.blue.intensity.average" = list(plotName = "side sections - NDVI blue", section = 80, subsection = 1, subsubsection = 1, paragraph = 2),
		"side.section#ndvi.vis.green.intensity.average" = list(plotName = "side sections - NDVI green", section = 80, subsection = 1, subsubsection = 1, paragraph = 3),
		"side.section#ndvi.vis.red.intensity.average" = list(plotName = "side sections - NDVI red", section = 80, subsection = 1, subsubsection = 1, paragraph = 4),
		
		"side.section#vis.hsv.dgci.average" = list(plotName = "side sections - DGCI", section = 80, subsection = 1, subsubsection = 2),
		
		"side.section#vis.hsv.h.average" = list(plotName = "side sections - color shade average", section = 80, subsection = 1, subsubsection = 3, paragraph = 1),
		"side.section#vis.hsv.h.kurtosis" = list(plotName = "side sections - color shade kurtosis", section = 80, subsection = 1, subsubsection = 3, paragraph = 2),
		"side.section#vis.hsv.h.skewness" = list(plotName = "side sections - color shade skewness", section = 80, subsection = 1, subsubsection = 3, paragraph = 3),
		"side.section#vis.hsv.h.stddev" = list(plotName = "side sections - color shade stddev", section = 80, subsection = 1, subsubsection = 3, paragraph = 4),
		
		"side.section#vis.hsv.s.average" = list(plotName = "side sections - saturation average", section = 80, subsection = 1, subsubsection = 4, paragraph = 1),
		"side.section#vis.hsv.s.kurtosis" = list(plotName = "side sections - saturation kurtosis", section = 80, subsection = 1, subsubsection = 4, paragraph = 2),
		"side.section#vis.hsv.s.skewness" = list(plotName = "side sections - saturation skewness", section = 80, subsection = 1, subsubsection = 4, paragraph = 3),
		"side.section#vis.hsv.s.stddev" = list(plotName = "side sections - saturation stddev", section = 80, subsection = 1, subsubsection = 4, paragraph = 4),
		
		"side.section#vis.hsv.v.average" = list(plotName = "side sections - brightness average", section = 80, subsection = 1, subsubsection = 5, paragraph = 1),
		"side.section#vis.hsv.v.kurtosis" = list(plotName = "side sections - brightness kurtosis", section = 80, subsection = 1, subsubsection = 5, paragraph = 2),
		"side.section#vis.hsv.v.skewness" = list(plotName = "side sections - brightness skewness", section = 80, subsection = 1, subsubsection = 5, paragraph = 3),
		"side.section#vis.hsv.v.stddev" = list(plotName = "side sections - brightness stddev", section = 80, subsection = 1, subsubsection = 5, paragraph = 4),
		
		"side.section#vis.lab.l.mean" = list(plotName = "side sections - (L)AB mean", section = 80, subsection = 1, subsubsection = 6, paragraph = 1),
		"side.section#vis.lab.l.kurtosis" = list(plotName = "side sections - (L)AB kurtosis", section = 80, subsection = 1, subsubsection = 6, paragraph = 2),
		"side.section#vis.lab.l.skewness" = list(plotName = "side sections - (L)AB skewness", section = 80, subsection = 1, subsubsection = 6, paragraph = 3),
		"side.section#vis.lab.l.stddev" = list(plotName = "side sections - (L)AB stdev", section = 80, subsection = 1, subsubsection = 6, paragraph = 4),
		
		"side.section#vis.lab.a.mean" = list(plotName = "side sections - L(A)B mean", section = 80, subsection = 1, subsubsection = 7, paragraph = 1),
		"side.section#vis.lab.a.kurtosis" = list(plotName = "side sections - L(A)B kurtosis", section = 80, subsection = 1, subsubsection = 7, paragraph = 2),
		"side.section#vis.lab.a.skewness" = list(plotName = "side sections - L(A)B skewness", section = 80, subsection = 1, subsubsection = 7, paragraph = 3),
		"side.section#vis.lab.a.stddev" = list(plotName = "side sections - L(A)B stddev", section = 80, subsection = 1, subsubsection = 7, paragraph = 4),
		
		"side.section#vis.lab.b.mean" = list(plotName = "side sections - LA(B) mean", section = 80, subsection = 1, subsubsection = 8, paragraph = 1),
		"side.section#vis.lab.b.kurtosis" = list(plotName = "side sections - LA(B) kurtosis", section = 80, subsection = 1, subsubsection = 8, paragraph = 2),
		"side.section#vis.lab.b.skewness" = list(plotName = "side sections - LA(B) skewness", section = 80, subsection = 1, subsubsection = 8, paragraph = 3),
		"side.section#vis.lab.b.stddev" = list(plotName = "side sections - LA(B) stddev", section = 80, subsection = 1, subsubsection = 8, paragraph = 4),
				
		"side.section#vis.stress.hue.brown2green" = list(plotName = "side sections - brown bin divided by green bins", section = 80, subsection = 1, subsubsection = 9),
		"side.section#vis.stress.hue.red2green" = list(plotName = "side sections - red bins divided by green bins", section = 80, subsection = 1, subsubsection = 10),	
		"side.section#vis.stress.hue.yellow2green" = list(plotName = "side sections - yellow bins divided by green bins", section = 80, subsection = 1, subsubsection = 11),
		
			
		"top.section#ndvi" = list(plotName = "top sections - NDVI", section = 80, subsection = 2, subsubsection = 1, paragraph = 1),
		"top.section#ndvi.vis.blue.intensity.average" = list(plotName = "top sections - NDVI blue", section = 80, subsection = 2, subsubsection = 1, paragraph = 2),
		"top.section#ndvi.vis.green.intensity.average" = list(plotName = "top sections - NDVI green", section = 80, subsection = 2, subsubsection = 1, paragraph = 3),
		"top.section#ndvi.vis.red.intensity.average" = list(plotName = "top sections - NDVI red", section = 80, subsection = 2, subsubsection = 1, paragraph = 4),

		"top.section#vis.hsv.dgci.average" = list(plotName = "top sections - DGCI", section = 80, subsection = 2, subsubsection = 2),
		
		"top.section#vis.hsv.h.average" = list(plotName = "top sections - color shade average", section = 80, subsection = 2, subsubsection = 3, paragraph = 1),
		"top.section#vis.hsv.h.kurtosis" = list(plotName = "top sections - color shade kurtosis", section = 80, subsection = 2, subsubsection = 3, paragraph = 2),
		"top.section#vis.hsv.h.skewness" = list(plotName = "top sections - color shade skewness", section = 80, subsection = 2, subsubsection = 3, paragraph = 3),
		"top.section#vis.hsv.h.stddev" = list(plotName = "top sections - color shade stddev", section = 80, subsection = 2, subsubsection = 3, paragraph = 4),
		
		"top.section#vis.hsv.s.average" = list(plotName = "top sections - saturation average", section = 80, subsection = 2, subsubsection = 4, paragraph = 1),
		"top.section#vis.hsv.s.kurtosis" = list(plotName = "top sections - saturation kurtosis", section = 80, subsection = 2, subsubsection = 4, paragraph = 2),
		"top.section#vis.hsv.s.skewness" = list(plotName = "top sections - saturation skewness", section = 80, subsection = 2, subsubsection = 4, paragraph = 3),
		"top.section#vis.hsv.s.stddev" = list(plotName = "top sections - saturation stddev", section = 80, subsection = 2, subsubsection = 4, paragraph = 4),
		
		"top.section#vis.hsv.v.average" = list(plotName = "top sections - brightness average", section = 80, subsection = 2, subsubsection = 5, paragraph = 1),
		"top.section#vis.hsv.v.kurtosis" = list(plotName = "top sections - brightness kurtosis", section = 80, subsection = 2, subsubsection = 5, paragraph = 2),
		"top.section#vis.hsv.v.skewness" = list(plotName = "top sections - brightness skewness", section = 80, subsection = 2, subsubsection = 5, paragraph = 3),
		"top.section#vis.hsv.v.stddev" = list(plotName = "top sections - brightness stddev", section = 80, subsection = 2, subsubsection = 5, paragraph = 4),
		
		"top.section#vis.lab.l.mean" = list(plotName = "top sections - (L)AB mean", section = 80, subsection = 2, subsubsection = 6, paragraph = 1),
		"top.section#vis.lab.l.kurtosis" = list(plotName = "top sections - (L)AB kurtosis", section = 80, subsection = 2, subsubsection = 6, paragraph = 2),
		"top.section#vis.lab.l.skewness" = list(plotName = "top sections - (L)AB skewness", section = 80, subsection = 2, subsubsection = 6, paragraph = 3),
		"top.section#vis.lab.l.stddev" = list(plotName = "top sections - (L)AB stdev", section = 80, subsection = 2, subsubsection = 6, paragraph = 4),
		
		"top.section#vis.lab.a.mean" = list(plotName = "top sections - L(A)B mean", section = 80, subsection = 2, subsubsection = 7, paragraph = 1),
		"top.section#vis.lab.a.kurtosis" = list(plotName = "top sections - L(A)B kurtosis", section = 80, subsection = 2, subsubsection = 7, paragraph = 2),
		"top.section#vis.lab.a.skewness" = list(plotName = "top sections - L(A)B skewness", section = 80, subsection = 2, subsubsection = 7, paragraph = 3),
		"top.section#vis.lab.a.stddev" = list(plotName = "top sections - L(A)B stddev", section = 80, subsection = 2, subsubsection = 7, paragraph = 4),
		
		"top.section#vis.lab.b.mean" = list(plotName = "top sections - LA(B) mean", section = 80, subsection = 2, subsubsection = 8, paragraph = 1),
		"top.section#vis.lab.b.kurtosis" = list(plotName = "top sections - LA(B) kurtosis", section = 80, subsection = 2, subsubsection = 8, paragraph = 2),
		"top.section#vis.lab.b.skewness" = list(plotName = "top sections - LA(B) skewness", section = 80, subsection = 2, subsubsection = 8, paragraph = 3),
		"top.section#vis.lab.b.stddev" = list(plotName = "top sections - LA(B) stddev", section = 80, subsection = 2, subsubsection = 8, paragraph = 4),
		
		"top.section#vis.stress.hue.brown2green" = list(plotName = "top sections - brown bin divided by green bins", section = 80, subsection = 2, subsubsection = 9),
		
		"top.section#vis.stress.hue.red2green" = list(plotName = "top sections - red bins divided by green bins", section = 80, subsection = 2, subsubsection = 10),
		
		"top.section#vis.stress.hue.yellow2green" = list(plotName = "top sections - yellow bins divided by green bins", section = 80, subsection = 2, subsubsection = 11)
)