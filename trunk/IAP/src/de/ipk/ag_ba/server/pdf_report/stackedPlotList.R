# TODO: Add comment
# 
# Author: Entzian
###############################################################################

stackedBarOptions = list(typOfGeomBar=c("fill", "stack")) #, "dodge"

stacked.plot.list <- list(
		"side.nir.normalized.histogram.bin." = "side near-infrared intensities (zoom corrected) (%)", 
		"side.nir.histogram.bin." = "side near-infrared intensities (%)",
		"top.nir.normalized.histogram.bin." = "top near-infrared intensities (zoom corrected) (%)",
		"top.nir.histogram.bin." = "top near-infrared intensities (%)", 
		
		"side.fluo.normalized.histogram.bin." = "side fluorescence color spectra (zoom corrected) (%)",
		"side.fluo.histogram.bin." = "side fluorescence color spectra (%)", 
		"top.fluo.normalized.histogram.bin." = "top fluorescence color spectra (zoom corrected) (%)", 
		"top.fluo.histogram.bin." = "top fluorescence color spectra (%)", 
		 
		"side.vis.hsv.h.normalized.histogram.bin." = "side visible light colors (zoom corrected) (%)",
		"side.vis.hsv.h.histogram.bin." = "side visible light colors (%)",  
		"top.vis.hsv.h.normalized.histogram.bin." = "top visible light colors (zoom corrected) (%)",
		"top.vis.hsv.h.histogram.bin." = "top visible light colors (%)",

		"side.vis.hsv.s.normalized.histogram.bin." = "side visible light color saturations (zoom corrected) (%)",
		"side.vis.hsv.s.histogram.bin." = "side visible light color saturations (%)",  
		"top.vis.hsv.s.normalized.histogram.bin." = "top visible light color saturations (zoom corrected) (%)",
		"top.vis.hsv.s.histogram.bin." = "top visible light color saturations (%)",
		
		"side.vis.hsv.v.normalized.histogram.bin." = "side visible light brightnesses (zoom corrected) (%)",
		"side.vis.hsv.v.histogram.bin." = "side visible light brightnesses (%)",  
		"top.vis.hsv.v.normalized.histogram.bin." = "top visible light brightnesses (zoom corrected) (%)",
		"top.vis.hsv.v.histogram.bin." = "top visible light brightnesses (%)",
		
		"side.ir.normalized.histogram.bin." = "side infrared intensities (zoom corrected) (%)",
		"side.ir.histogram.bin." = "side infrared light intensities (%)",
		"top.ir.normalized.histogram.bin." = "top infrared light intensities (zoom corrected) (%)",
		"top.ir.histogram.bin." = "top infrared light intensities (%)"
		
)

##boxplotStacked
#descriptorSet_boxplotStacked = c("side.nir.normalized.histogram.bin.", 
#		"side.fluo.histogram.bin.", 
#		"top.nir.histogram.bin.", 
#		"side.fluo.histogram.ratio.bin.", 
#		"side.nir.normalized.histogram.bin.", 
#		"side.fluo.normalized.histogram.bin.", 
#		"side.fluo.normalized.histogram.ratio.bin.", 
#		"side.vis.hue.histogram.bin.", 
#		"side.vis.normalized.histogram.bin.", 
#		"top.fluo.histogram.bin.", 
#		"top.fluo.histogram.ratio.bin.", 
#		"top.nir.histogram.bin.", 
#		"top.vis.hue.histogram.bin.",
#		"top.vis.hue.normalized.histogram.bin.",
#		"top.ir.histogram.bin.",
#		"side.ir.histogram.bin."
#)
#
#
#descriptorSetName_boxplotStacked = c("side near-infrared intensities (zoom corrected) (%)", 
#		"side fluorescence colour spectra (%)", 
#		"top near-infrared intensities (%)", 
#		"side fluorescence ratio histogram (%)", 
#		"side near-infrared (zoom corrected) (%)", 
#		"side fluorescence colour spectra (zoom corrected) (%)", 
#		"side fluorescence  colour spectra (%)", 
#		"side visible light colour histogram (%)", 
#		"side visible light ratio histogram (zoom corrected) (%)", 
#		"top fluorescence colour spectra (%)", 
#		"top fluo ratio histogram (%)", 
#		"NIR top histogram (%)", 
#		"top visible light color histogram (%)",
#		"top visible light color histogram (zoom corrected) (%)",
#		"top infrared light heat histogram (%)",
#		"side infrared light heat histogram (%)"
#)
#
#stackedBarOptions = list(typOfGeomBar=c("fill", "stack")) #, "dodge"