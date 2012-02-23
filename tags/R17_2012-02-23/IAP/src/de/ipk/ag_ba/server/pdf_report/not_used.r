connectAllWithAll = function(value1, value2) {
	
	connectRow = character()
	for (k1 in value1) {
		if (k1 != "none") {
			for (k2 in value2) {
				if (k2 != "none") {
					connectRow = c(connectRow, paste(k1, k2, sep = "#"))
				}
			}
		}
	}
	return(connectRow)
}

