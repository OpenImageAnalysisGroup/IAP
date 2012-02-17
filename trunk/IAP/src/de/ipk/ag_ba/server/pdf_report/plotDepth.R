#===============================================================================
# Name           : plot.depth
# Original author: Andreas Plank 
# Changes        : 
# Date (dd/mm/yy): 05/12/2007 11:08
# Version        : v1.1
# Aim            : To plot depth profiles with many column data sets
#===============================================================================

# Arguments:
# data,               dataset as a data.frame or matrix: with first column as depth
# yaxis.first=TRUE    TRUE/FALSE does first column contain depth datas?
# yaxis.num="n",      switch on/off numbers at remaining y-axes on="s" off="n"
# xaxes.equal=TRUE,   equal scaling of xaxes; can be set individually by  c(...)
# xaxis.num="s",      switch on/off numbers+ticks at x-axis on="s" off="n"
# bty="L"             boxtype as in plot: L, c, o ...; can be set individually by  c(...)
# l.type="solid"      line type default; can be set individually by  c(...)
# l.width=1,          line width; can be set individually by  list(...) or nested with c()
# lp.color="black"    line color; can be set individually by  c(...)
# plot.type="o"       type of plot - as in plot(); can be set individually by  c(...)
#    possible: o, b, c, n, h, p, l, s, S
#    "p" for points,
#    "l" for lines,
#    "b" for both,
#    "c" for the lines part alone of "b",
#    "o" for both "overplotted",
#    "h" for "histogram" like horizontal lines,
#    "s" or "S" for stair steps,
#    "n" for no plotting.
# plot.before=NULL   evaluate/draw before plotting
#                    eg.: grid() as expression(); nested: 'expression(grid())'
#                    can be set individually by  list(...) or nested with expression()
# plot.after=NULL    evaluate/draw after plotting
#                    additional graphics eg.: points(), lines() as expression()
#                    expression(lines(...)) - can be set individually by  list(...)
#                    or nested with expression()
# yaxis.lab=FALSE     no additional labels on remaining y-axes
# yaxis.ticks=TRUE    add y-ticks to graph ?
# axis.top=list(c(FALSE, FALSE)) -- x-axis also on top?
#                    call for axis and labels as c(axis=TRUE, labels=TRUE)
#                    can be nested with list( c(T,F), c(T,T), ...)
# nx.minor.ticks=5   number of intervals at x-axis if package Hmisc loadable
#                    can be set individually by  c(...)
# ny.minor.ticks=5   number of intervals at y-axis if package Hmisc loadable
#                    can be set individually by  c(...)
# mar.outer=c(1,6,4,1) --  margin at outer side: c(bottom, left , top, right)
# mar.top=9          margin at the top
# mar.bottom=5       margin at the bottom
# txt.xadj=0.1       align text at plot-top in x-axis direction: 0...1 left...right
# txt.yadj=0.1       align text at plot-top in y-axis direction: in scalenumbers
#                    + -> to the top - -> to the bottom
# colnames=TRUE      can be set individually by  c(...)
# rotation=60        text rotation: can be set individually by  c(...)
# p.type=21          type of points like pch in points()
#                    can be set individually by list(...) also nested
# p.bgcolor="white"  point background color: can be set individually by c(...)
# p.size = 1         point size: can be set individually by list(...) also nested
# subtitle=""        subtitle: can be set individually by list(...)
# xlabel=""          x-labeling: can be set individually by list(...)
# main=""            titel of individual plots: can be set individually by list(...)
# polygon=FALSE      plot polygon on/off: can be set individually by  c(...)
# polygon.color="gray" -- color of polygon plot; can be set individually by  c(...)
# show.na=TRUE       show NA values as red cross
# min.scale.level=0.2
#                   0...1 if data are less than 0.2(=20%) from maximum of the data
#                   than draw raltive 'min.scale.rel'-width for the plot
# min.scale.rel=0.5,
#                   0...1 relative space for minimal data
#                   1 means maximal width
# min.scaling=FALSE --  add upscaling plots to rare data; can be set individually by  c(...)
# color.minscale="gray95" -- color for rare scaled data; can be set individually by list(...)
# wa.order="none", sort variables according to the weighted average with y
#                  "bottomleft", "topleft" from strat.plot(palaeo - pkg)
# ...  passed to function 'lines()'
#
##############################
plot.depth <- function(
		data,# data.frame
		yaxis.first=TRUE, # is 1st data-column 1st y-axis?
		yaxis.num="n", # supress labelling at y-axis
		xaxis.num="s", # show labelling at x-axis
		xaxes.equal=TRUE, # equal scaling
		xaxis.ticks.minmax=FALSE, # only min-max?
		cex.x.axis=par("cex.axis")*0.8,# size x-axis labels
		cex.y.axis=par("cex.axis")*0.8,# size y-axis labels
		yaxis.lab=FALSE, # axis labels on remaining y-axis?
		yaxis.ticks=TRUE,# add y-ticks to graph?
		axis.top=list(c(FALSE, FALSE)),# axis on top? c(axis=TRUE, labels=TRUE)
		nx.minor.ticks=5,# number intervals for minor ticks
		ny.minor.ticks=5,# number intervals for minor ticks
		bty="L", # boxtype
		plot.type="o", # point-plot type
		plot.before=NULL,# something to plot BEFORE the graph is drawn?
		plot.after=NULL,# something to plot AFTER the graph is drawn?
		l.type="solid", # line type
		l.width=1,# line width
		lp.color="black", # line/point color
		p.type=21,# point type
		p.bgcolor="white",# point background color
		p.size = 1, # point size
		mar.outer=c(1,6,4,1),# outer margin of whole plot
		mar.top=9,# margin on the top
		mar.bottom=5,# margin on the bottom
		mar.right=0,# margin on the right side
		txt.xadj=0.1,# x-adjusting text
		txt.yadj=0.1,# y-adjusting text
		colnames=TRUE,# add columnames
		rotation=60,# columnames rotation
		subtitle="",# below every plot
		xlabel="",# x-labels
		ylabel="",# first y-label
		main="",# title for each plot
		polygon=FALSE, # plot Polygon?
		polygon.color="gray", # color polygon
		show.na=TRUE,# show missing values?
		min.scale.level=0.2,#0...1 if data are less than 0.2(=20%)
		min.scale.rel=0.5,#0...1 relative space for minimal data
		min.scaling=FALSE,#switch min scaling
		color.minscale="gray95",# color for minimum scaled data
		wa.order="none", # sort variables according to the weighted average with y
		...
){
	# ------8<---- function minor.tick start
	# from Hmisc package added: axis=c(1,2) + '...' for axis( , ...)
	# axis=c(3,4) draws also ticks on top or right
	minor.tick <- function (nx = 2, ny = 2, tick.ratio = 0.5, axis=c(1,2), ...)
	{
		ax <- function(w, n, tick.ratio) {
			range <- par("usr")[if (w == "x")
								1:2
							else 3:4]
			tick.pos <- if (w == "x")
						par("xaxp")
					else par("yaxp")
			distance.between.minor <- (tick.pos[2] - tick.pos[1])/tick.pos[3]/n
			possible.minors <- tick.pos[1] - (0:100) * distance.between.minor
			low.minor <- min(possible.minors[possible.minors >= range[1]])
			if (is.na(low.minor))
				low.minor <- tick.pos[1]
			possible.minors <- tick.pos[2] + (0:100) * distance.between.minor
			hi.minor <- max(possible.minors[possible.minors <= range[2]])
			if (is.na(hi.minor))
				hi.minor <- tick.pos[2]
			if (.R.)
				axis(if (w == "x")
									axis[1]
								else axis[2], seq(low.minor, hi.minor, by = distance.between.minor),
						labels = FALSE, tcl = par("tcl") * tick.ratio, ...)
			else axis(if (w == "x")
									axis[1]
								else axis[2], seq(low.minor, hi.minor, by = distance.between.minor),
						labels = FALSE, tck = par("tck") * tick.ratio, ...)
		}
		if (nx > 1)
			ax("x", nx, tick.ratio = tick.ratio)
		if (ny > 1)
			ax("y", ny, tick.ratio = tick.ratio)
		invisible()
	}
	# ------8<---- function minor.tick end
	# check data
	if(!is.data.frame(data) & !is.matrix(data))
		stop(paste("\n#> function \'plot.depth(data, ...)\' expect a data.frame or matrix!
#> your data is: \'",mode(data),"\'.
#> Use \"as.data.frame(depthdata) -> depthdata\" or \"as.matrix(depthdata) -> depthdata\".", sep=""))
	if(ncol(data) < 2)
		stop("\n#> At least 2 columns in the data!")
	nc <- ncol(data) # number of columns
	nr <- nrow(data) # number of rows
	if(yaxis.first==TRUE){# if 1st column is first y-axis
		nc.data <- nc-1 # number of columns for drawing
		draw <- 2:nc # what should be drawn
		y.depth <- data[,1] # depth scale
		y.axfirst.type ="s"
	}
	else{# no first y-axis
		nc.data <- nc# number of columns for drawing
		draw <- 1:nc # what should be drawn
		y.depth <- (1:nr)*(-1) # depth scale
		warning("#> Your data will be drawn as category numbers (=number of rowname)\n")
		y.axfirst.type ="n"
	}
	# weighted averageing order
	# (from package paleo http://www.campus.ncl.ac.uk/staff/Stephen.Juggins/analysis.htm)
	if (wa.order == "topleft" || wa.order == "bottomleft") {
		colsum <- colSums(data[,draw])
		opt <- (t(data[,draw]) %*% y.depth)/colsum
		if (wa.order == "topleft")
			opt.order <- rev(order(opt))
		else opt.order <- order(opt)
		draw <- opt.order
		cat("#> Column Index (wa.order):",draw,"\n")
		# data <- data[, opt.order]
	}
	
	x.maximum <- max(apply(data[,draw],2,max, na.rm=TRUE))
	x.maxima <- apply(data[,draw],2,max, na.rm=TRUE)
	# cat(x.maximum) control
	x.max <- apply(data[,draw],2,max, na.rm=TRUE)
	stopifnot(0 <= min.scale.level && min.scale.level <=1)
	stopifnot(0 <= min.scale.rel && min.scale.rel <=1)
	par(no.readonly=TRUE) -> original # save graphical settings
	# ---8<--- get settings for layout
	# maxima from each column
	apply(data[,draw],2,max, na.rm=TRUE) -> x.widths
	xwidths <- NULL # temporary vector
	for(i in 1:length(x.widths)){# for each maximum
		# allow individual settings for plots via index
		ifelse(length(xaxes.equal)==nc.data, equal.i <- i, equal.i <- 1)
		ifelse(x.widths[i]/max(x.widths) <= min.scale.level,
				{# x.widths/max <= 0.5
					xwidths[i] <- min.scale.rel # 0...min.scale.rel
					# maximum for x-axis
					ifelse(xaxes.equal[equal.i]==FALSE,
							{# draw xaxes-equal FALSE:
								x.max[i] <- max(data[,draw[i]], na.rm=TRUE) # maximum of column
							}, {#  draw xaxes-equal TRUE
								x.max[i] <- x.maximum * min.scale.rel # maximum of all data
							}
					) # xaxes.equal
				},{# x.widths/max > 0.5
					xwidths[i] <- x.widths[i]/max(x.widths) # 0...1
					# maximum for x-axis
					ifelse(xaxes.equal[equal.i]==FALSE,
							{# FALSE:
								x.max[i] <- max(data[,draw[i]], na.rm=TRUE) # maximum of column
							},{
								x.max[i] <- x.maxima[i] # maximum of all data
							}
					) # xaxes.equal end
				}
		) # minscale.level end
	}
	# set layout
	x.widths <- xwidths
	layout(matrix(1:nc.data,1 , nc.data), widths=x.widths)
	# ---8<--- end get settings for layout
	par(mar=c(
					mar.bottom, # bottom
					0, # left
					mar.top, # top
					ifelse(yaxis.num=="s", 1.5 + mar.right, mar.right) # right
			)+0.1,
			xpd=NA # NA to get no overplotted text
	)
	for(i in 1:length(draw)){# draw each plot
		#cat(i,"\n")
		# check for lists in list() or c() in differrent options
		ifelse(length(plot.type)          == nc.data, n.i <- i,      n.i <- 1)
		ifelse(length(ny.minor.ticks)     == nc.data, ny.i <- i,     ny.i <- 1)
		ifelse(length(nx.minor.ticks)     == nc.data, nx.i <- i,     nx.i <- 1)
		ifelse(length(polygon)            == nc.data, p.i <- i,      p.i <- 1)
		ifelse(length(min.scaling)        == nc.data, min.i <- i,    min.i <- 1)
		ifelse(length(l.type)             == nc.data, lt.i <-i,      lt.i <- 1)
		ifelse(length(lp.color)           == nc.data, lc.i <-i,      lc.i <-1)
		ifelse(length(l.width)            == nc.data, lw.i <-i,      lw.i <- 1)
		ifelse(length(p.type)             == nc.data, pt.i <-i,      pt.i <- 1)
		ifelse(length(p.size)             == nc.data, pw.i <- i,     pw.i <- 1)
		ifelse(length(p.bgcolor)          == nc.data, pbg.i <- i,    pbg.i <- 1)
		ifelse(length(colnames)           == nc.data, col.i <- i,    col.i <- 1)
		ifelse(length(rotation)           == nc.data, r.i <- i,      r.i <- 1)
		ifelse(length(xlabel)             == nc.data, xlab.i <- i,   xlab.i <- 1)
		ifelse(length(subtitle)           == nc.data, sub.i <- i,    sub.i <- 1)
		ifelse(length(main)               == nc.data, main.i <- i,   main.i <- 1)
		ifelse(length(plot.before)        == nc.data, before.i <- i, before.i <- 1)
		ifelse(length(plot.after)         == nc.data, after.i <- i,  after.i <- 1)
		ifelse(length(axis.top)           == nc.data, axtop.i <- i,  axtop.i <- 1)
		ifelse(length(xaxis.num)          == nc.data, xnum.i <- i,   xnum.i <- 1)
		ifelse(length(xaxis.ticks.minmax) == nc.data, xminmax.i <- i,xminmax.i <- 1)
		# margins of x-axis
		if(i==1) par(oma=mar.outer, xaxt=xaxis.num[xnum.i])
		else par(xaxt=xaxis.num[xnum.i])
		# axis ticks and labelling
		par(
				mgp=c(3, ifelse(yaxis.num=="s" && i > 1, 0.3, 1), 0)
		)
		# minimum
		ifelse(
				min(data[,draw[i]], na.rm=TRUE) > 0,
				x.min <- 0,# 0... max
				x.min <- min(data[,draw[i]], na.rm=TRUE) # min...max
		)
		# draw plot()
		par(xpd=FALSE)# to draw also ylabel
		plot(data[,draw[i]], y.depth,
				ann=ifelse(i==1,TRUE, FALSE),# nichts an Achse
				type="n",# Punkttyp
				yaxt=ifelse(i==1,y.axfirst.type, yaxis.num),# y-Achse an/aus
				xlim=c(x.min,x.max[i]),
				bty=ifelse(length(bty)==nc.data, bty[i], bty),
				xlab=ifelse(length(xlabel)==nc.data, xlabel[i], xlabel),
				ylab=ylabel,#ifelse(i==1, ylabel, ""),
				panel.first = eval(plot.before[[before.i]]),
				xaxt = "n" # no x-axis
		)
		par(xpd=FALSE)
		if(i==1 && y.axfirst.type=="n"){ # draw extra first y-axis
			axis(side=2, labels=rownames(data),
					at=(1:nr)*(-1), cex.axis=cex.y.axis
			)
			box(bty=bty)
		}
		# draw x-axis
		axTicks(1,
				axp=if(xaxis.ticks.minmax[xminmax.i]==TRUE) {c(par()$xaxp[1:2], 1)} else NULL
		) -> x.axis
		axis(1, at=x.axis, cex.axis=cex.x.axis)
		
		# minor ticks if package Hmisc is installed
		if(yaxis.ticks==FALSE && i > 1) ny.minor.ticks[ny.i] <- 0
		
		if(require(Hmisc)) {minor.tick(
					ny=ifelse(i==1 && y.axfirst.type=="n", 0, ny.minor.ticks[ny.i]),
					nx=nx.minor.ticks[ny.i]
			)
		}
		else warning("#> Install package 'Hmisc' to add minor ticks on axes")
		
		# y-axis for remainig axes
		if(i > 1) { axis(side=2,
					labels=yaxis.lab,
					tick=yaxis.ticks,
					cex.axis=cex.y.axis
			)
		}
		# x-axis top
		if(length(axis.top[[axtop.i]])==2){
			if(axis.top[[axtop.i]][1]==TRUE){
				axis(side=3, labels=axis.top[[axtop.i]][2], tick=TRUE, tcl=0.5, cex.axis=cex.x.axis)
				minor.tick(ny=0, nx=nx.minor.ticks[ny.i], axis=c(3,4), tcl=0.25)
			}
		}
		else warning("#> Option 'axis.top' wants 2 arguments as list(...):",
					"\n#> 2nd argument is for numbers on axis, so eg.: axis.top=list(c(T, F))")
		# labelling of columns
		if(colnames[col.i]==TRUE){
			min(par()$usr[1:2]) -> x.text
			abs(max(par()$usr[1:2])-x.text)*txt.xadj -> x.adj # %-width of x-axis
			max(par()$usr[3:4]) -> y.text
			par(xpd=NA) # NA to get no overplotted text
			text(x.text+x.adj, y.text+txt.yadj, labels=colnames(data)[draw[i]], adj=0, srt=rotation[r.i] )
			par(xpd=FALSE)
		}
		# title subtitle, xlabels
		title(
				sub=subtitle[[sub.i]],
				xlab=xlabel[[xlab.i]],
				main=main[[main.i]]
		)
		
		# pseudo histograms; width can be set with option 'l.width'
		if( plot.type[n.i] =="h"){
			for(n in 1:nr){
				x <- c(0,data[n,draw[i]])
				y <- c(y.depth[n], y.depth[n])
				par(lend="butt") # line-End
				lines(x,y,
						lty=l.type[[lt.i]],
						lwd=l.width[[lw.i]],
						col=ifelse(length(lp.color[[lc.i]])==nr, lp.color[[lc.i]][n], lp.color[[lc.i]]),
				)
				par(lend="round")
			}
		}
		# Polygonplot
		if (polygon[p.i]==TRUE){
			# add zero values to margins where NA values occur
			# eg.: NA NA 23  7 34 84 NA NA
			#     -1 -2 -3 -4 -5 -6 -7 -8
			# to: NA NA| 0| 23  7 34 84 | 0| NA NA
			#   -1 -2 |-3| -3 -4 -5 -6 |-6| -7 -8
			data.null <- data.frame(
					rbind(
							if(!is.na(data[1, draw[i]])) cbind(y.depth[1], 0),
							cbind(y.depth[1], data[1,draw[i]])
					)
			)
			for(r in 2:nr){
				data.null <- rbind(
						as.matrix(data.null),
						# r-1==NA && r!=NA -> 0r
						if(is.na(data[r-1, draw[i]]) && !is.na(data[r, draw[i]])) cbind(y.depth[r], 0),
						# r-1!=NA && r==NA -> 0r-1
						if(!is.na(data[r-1, draw[i]]) && is.na(data[r, draw[i]])) cbind(y.depth[r-1], 0),
						as.matrix( cbind(y.depth[r], data[r, draw[i]]) ),
						# r==nr -> 0r
						if(r==nr && !is.na(data[r, draw[i]])) cbind(y.depth[r], 0)
				)
			}
			
			# min.scaling
			if (min.scaling[min.i]==TRUE || min.scaling[min.i] > 0){
				# default 5-scaled
				if(min.scaling[min.i]==TRUE) min.scaling[min.i] <- 5
				polygon(
						data.null[, 2]*min.scaling[min.i] ,
						data.null[, 1],
						col=ifelse(length(color.minscale)==nc.data,color.minscale[[i]],color.minscale[1]),
						xpd=FALSE
				)
				# scaling as message message
				message(paste("#> Column \'", colnames(data)[draw[i]],"\' is scaled ",
								min.scaling[min.i], "-times to original data.", sep="")
				)
			}# end min.scaling
			# default polygon
			polygon(
					data.null[, 2],
					data.null[, 1],
					col=ifelse(length(polygon.color)==nc.data, polygon.color[i], polygon.color),
					xpd=FALSE
			)
			# warning/recommendation, if NA in data
			if(any(is.na(data[,draw[i]]))) {warning("#> Column \'",
						colnames(data)[draw[i]], "\' contain NA-values.",
						"\n#> Other possibility to draw: switch off drawing polygon with option \'polygon=c(T, T, F, ...)\'",
						"\n#> and set the column to \'F\' (FALSE) than draw histogram-like lines with the following two options:",
						"\n#>   plot.type=c(...,\"h\",...),\n#>   l.width=c(..., 15, ...), ",call. = FALSE)
			}
		}# polygon end
		if(show.na==TRUE){# draw red cross, at NA-value position
			which(is.na(data[,draw[i]])) -> na.index
			# add red 'x'
			points(y=y.depth[na.index], x=rep(0, length(na.index)), pch=4, col="red")
			if(length(na.index) > 0) {
				message("#> With option 'show.na=FALSE' you can switch off red crosses.")
			}
		}
		# points lines ....
		lines(data[,draw[i]], y.depth,
				ann=FALSE,# nichts an Achse
				type=ifelse(plot.type[n.i]=="h", "n",plot.type[n.i]),# type of points
				lty=l.type[[lt.i]],
				lwd=l.width[[lw.i]],
				pch=p.type[[pt.i]],
				col=lp.color[[lc.i]],
				bg=p.bgcolor[[pbg.i]],
				panel.last = eval(plot.after[[after.i]]),
				cex = p.size[[pw.i]],
				...
		)
	}# end for i
	par(original)
}# end plot.depth