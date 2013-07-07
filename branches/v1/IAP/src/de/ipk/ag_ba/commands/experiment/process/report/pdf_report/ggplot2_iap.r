.packageName <- "ggplot2"
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aaa-.r"
require("proto")
require("grid")
require("reshape")

# INCLUDES <- "web/graphics"
# FILETYPE <- "html"

# Upper case first letter of string
# This comes from the examples of some R function.
# 
# @keyword internal
firstUpper <- function(s) {
  paste(toupper(substring(s, 1,1)), substring(s, 2), sep="")
}

TopLevel <- proto(expr = {
  find_all <- function(., only.documented = FALSE) {
    names <- ls(pattern=paste("^", firstUpper(.$class()), "[A-Z].+", sep=""), parent.env(TopLevel))
    objs <- structure(lapply(names, get), names=names)
    
    if (only.documented) objs <- objs[sapply(objs, function(x) get("doc", x))]
    objs
  }
  find <- function(., name) {
    fullname <- paste(firstUpper(.$class()), firstUpper(name), sep="")
    if (!exists(fullname)) {
      stop("No ", .$class(), " called ", name, call.=FALSE)
    }
    get(fullname)
  }
  
  my_name <- function(., prefix=TRUE) {
    if (!prefix) return(.$objname)
    paste(.$class(), .$objname, sep="_")
  }
  my_names <- function(.) .$my_name()
  
  myName <- function(.) {
    ps(firstUpper(.$class()), ps(firstUpper(strsplit(.$objname, "_")[[1]])))
  }

  
  doc <- TRUE
  
  # Function for html documentation ------------------------------------
  desc <- ""
  details <- ""
  advice <- ""
  objname <- ""
  desc_params <- list("..." = "ignored ")
  icon <- function(.) rectGrob(gp=gpar(fill="white", col=NA))
  
  # Name of physical file to create, doesn't include directory
  html_path <- function(.) {
    ps(.$my_name(), ".html")
  }
  
  html_link_self <- function(., prefix=TRUE) {
    ps("<a href='", .$html_path(), "' title='", .$desc, "'>", .$my_name(prefix=prefix), "</a>")
  }

  html_abbrev_link_self <- function(., prefix=TRUE) {
    ps("<a href='", .$html_path(), "' title='", .$desc, "'>", .$objname, "</a>")
  }

  html_parent_link <- function(.) {
    parent <- parent.env(.)
    if (identical(parent, TopLevel)) return("")
    ps(parent$html_parent_link(), " &gt; ", parent$html_link_self())
  }
  
  all_html_pages_create <- function(., path="web/") {
    invisible(lapply(.$find_all(), function(x) x$html_page_create(path)))
  }
    
  html_page_create <- function(., path="web/") {
    cat("Creating html documentation for", .$my_name(), "\n")
    target <- ps(path, .$html_path())
    
    .$html_img_draw(path)
    cat(.$html_page(), file=target)
  }  
    
  html_page <- function(.) {
    ps(
      .$html_header(),
      .$html_head(),
      .$html_details(),
      .$html_advice(),
      .$html_feedback(),
      .$html_aesthetics(),
      .$html_outputs(),
      .$html_parameters(),
      # .$html_defaults(),
      .$html_returns(),
      .$html_seealso(),
      .$html_examples(),
      .$html_feedback(),
      .$html_footer()
    )
  }
  
  # Header and footer templates -----------------------  
  html_header <- function(., title = .$my_name()) {
    template <- ps(readLines("templates/header.html"), collapse="\n")
    gsub("TITLE", title, template)
  }  

  html_footer <- function(.) {
    ps(readLines("templates/footer.html"), collapse="\n")
  }  
  
  # Page header -----------------------
  html_head <- function(.) {
    ps(
      # "<p class='hierarchy'>", .$html_parent_link(), "</p>\n",
      "<h1>", .$html_img(), .$my_name(), "</h1>\n",
      "<p class='call'>", ps(.$call(), collapse="<br />\n"), "</p>\n"
    )
  }
  
  html_details <- function(.) {
    ps(
      # "<h2>Details</h2>\n",
      "<div class='details'>\n",
      "<p>", .$desc, "</p>\n",
      html_auto_link(.$details, .$my_name()),
      "<p>This page describes ", .$my_name(), ", see <a href='layer.html'>layer</a> and <a href='qplot.html'>qplot</a> for how to create a complete plot from individual components.</p>\n",
      "</div>\n"
    )
  }

  html_advice <- function(.) {
    if (.$advice == "") return()
    ps(
      "<h2>Advice</h2>\n",
      "<div class='details'>\n",
      html_auto_link(.$advice, .$my_name()),
      "</div>\n"
    )
  }  

  html_scales <- function(., aesthetic) {
    scales <- Scale$find(aesthetic, only.documented = TRUE)
    if (length(scales) == 0) return()
    ps(lapply(scales, function(x) x$html_link_self(prefix=FALSE)), collapse=", ")
  }
  
  html_aesthetics <- function(.) {
    if (!exists("default_aes", .)) return("")
    
    req <- rep("<strong>required</strong>", length(.$required_aes))
    names(req) <- .$required_aes 
    
    aes <- c(req, .$default_aes())
    if (length(aes) == 0) return("")

    scale_links <- sapply(names(aes), .$html_scales)
    scale_links <- sapply(scale_links, ps)

    ps(
      "<h2>Aesthetics</h2>\n",
      html_auto_link(ps("<p>The following aesthetics can be used with  ", .$my_name(), ".  Aesthetics are mapped to variables in the data with the aes function: <code>", .$my_name(), "(aes(x = var))</code>. Note that you do not need quotes around the variable name.</p>\n",
      "<p>Scales control how the variable is mapped to the aesthetic and are listed after each aesthetic.</p>\n"), .$my_name()),
      "<table width='100%'>\n",
      "<tr><th>Aesthetic</th> <th>Default</th> <th>Related scales</th></tr>\n",
      ps(
        "<tr>\n", 
        "<td>", names(aes), "</td><td>", aes, "</td><td>", scale_links, "</td>\n", 
        "</tr>\n"
      ),
      "</table>\n",
      "<p>Layers are divided into groups by the <code>group</code> aesthetic.  By default this is set to the interaction of all categorical variables present in the plot.  </p>\n"
    )
  }
  
  html_feedback <- function(.) {
    ps("<p class='feedback'>What do you think of the documentation?  <a href='http://hadley.wufoo.com/forms/documentation-feedback/def/field0=", .$my_name(), "'>Please let me know by filling out this short online survey</a>.</p>")
  }
  
  html_outputs <- function(.) {
    if (!exists("desc_outputs", .)) return("")
    
    ps(
      "<h2>New variables produced by the statistic</h2>\n",
      "<p>To use these variables in an aesthetic mapping, you need to surrond them with .., like <code>aes(x = ..output..)</code>. This tells ggplot that the variable isn't the original dataset, but has been created by the statistic.</p>\n",
      "<ul>\n",
      ps("<li><code>", names(.$desc_outputs), "</code>, ", .$desc_outputs, "</li>\n"),
      "</ul>\n"
    )
  }

  html_defaults <- function(.) {
    ps(
      "<h2>Defaults</h2>\n",
      "<ul>\n",
      .$html_defaults_stat(),
      .$html_defaults_geom(),
      .$html_defaults_position(),
      "</ul>\n"
    )
  }

  
  html_defaults_stat <- function(.) {
    if (!exists("default_stat", .)) return("")
    
    ps(
      "<li>", .$default_stat()$html_link_self(), ".  Override with the <code>stat</code> argument: <code>", .$my_name(), "(stat=\"identity\")</code></li>\n"
    )
  }
  
  html_defaults_geom <- function(.) {
    if (!exists("default_geom", .)) return("")
    
    ps(
      "<li>", .$default_geom()$html_link_self(), ".  Override with the  <code>geom</code> argument: <code>", .$my_name(), "(geom=\"point\")</code>.</li>\n"
    )
  }
  
  html_defaults_position <- function(.) {
    if (!exists("default_pos", .)) return("")
    
    ps(
      "<li>", .$default_pos()$html_link_self(), ".  Override with the <code>position</code> argument: <code>", .$my_name(), "(position=\"jitter\")</code>.</li>\n"
    )
  }
  
  params <- function(.) {
    param <- .$parameters()
    if (length(param) == 0) return()
  
    if(!exists("required_aes", .)) return(param)
  
    aesthetics <- c(.$required_aes, names(.$default_aes()))
    param <- param[setdiff(names(param), aesthetics)]
  }
  
  
  html_parameters <- function(.) {
    if (!exists("parameters", .)) return("")
    param <- .$params()
    
    ps(
      "<h2>Parameters</h2>\n",
      "<p>Parameters control the appearance of the ", .$class(), ". In addition to the parameters listed below (if any), any aesthetic can be used as a parameter, in which case it will override any aesthetic mapping.</p>\n",
      if(length(param) > 0) ps(
        "<ul>\n",
        ps("<li><code>", names(param), "</code>: ", defaults(.$desc_params, .desc_param)[names(param)], "</li>\n"),
        "</ul>\n"
      )
    )
  }
  
  # See also ---------------------------
  
  seealso <- list()
  html_seealso <- function(.) {
    if (length(.$seealso) == 0) return()
    ps(
      "<h2>See also</h2>",
      "<ul>\n",
      ps("<li>", html_auto_link(names(.$seealso)), ": ", .$seealso, "</li>\n"),
      "</ul>\n"
    )
  }

  # Returns ---------------------------

  html_returns <- function(.) {
    ps(
      "<h2>Returns</h2>\n",
      "<p>This function returns a <a href='layer.html'>layer</a> object.</p>"
    )
  }
  
  # Object icon -----------------------
  html_img_path <- function(.) {
    ps(.$my_name(), ".png")
  }
  
  html_img_link <- function(., align=NULL) {
    ps("<a href='", .$html_path(), "'>", .$html_img(align), "</a>")
  }
  
  html_img <- function(., align=NULL) {
    ps(
      "<img src='", .$html_img_path(), "'", if (!is.null(align)) {ps(" align='", align, "'")}, " width='50' height='50' alt='' class='icon' />\n"
    )
  }
  
  html_img_draw <- function(., path="web/") {
    png(ps(path, .$html_img_path()), width=50, height=50)
    grid.newpage()
    grid.draw(.$icon())
    dev.off()
  }

  # Examples -----------------------
  html_examples <- function(.) {
    if (!.$doc) return(FALSE)
    require("decumar", quiet = TRUE)
    
    curdir <- getwd()
    on.exit(setwd(curdir))
    setwd("~/Documents/ggplot/ggplot/web")
    
    html_auto_link(ps(
      "<h2>Examples</h2>\n",
      interweave_html(.$examples_text(), outdir = "graphics", dpi = 72),
      "\n"
    ), .$my_name())
  }

})

print.proto <- function(x, ...) x$pprint(...)
pprint <- function(x, ...) print(as.list(x), ...)
# name.proto <- function (...) {
#        proto(print.proto = print.default, f = proto::name.proto)$f(...)
# }


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aaa-compare.r"
# Functions for comparing images produced by two different versions of ggplot.

# a <- "~/Desktop/test-1/"
# b <- "~/Desktop/test-2/"

# Directory diff
# Compute the set of differences in file make up between two directories.
# 
# @arguments path a
# @arguments path b
# @value list with components only\_a, only\_b and both
# @keyword internal
dir_diff <- function(a, b) {
  files_a <- dir(a)
  files_b <- dir(b)
  
  list(
    only_a = setdiff(files_a, files_b),
    only_b = setdiff(files_b, files_a),
    both = intersect(files_a, files_b)
  )
}

# Compare two images
# Saves image displaying differences
# 
# @arguments name of file
# @arguments location of image a
# @arguments location of image b
# @arguments location where output should be saved
# @keyword internal
compare_img <- function(file, path_a, path_b, path_out) {
  file_a <- file.path(path_a, file)
  file_b <- file.path(path_b, file)

  if (same_file(file_a, file_b)) return(FALSE)

  file_out <- file.path(path_out, file)

  cmd <- paste("compare", file_a, file_b, file_out)
  system(cmd, intern = TRUE)
  TRUE
}

# Test if all files are the same
# Uses md5 checksum to rapidly check if multiple files are equal. 
# 
# @arguments character vector of paths
# @value boolean
# @keyword internal
same_file <- function(...) {  
  files <- list(...)
  cmd <- paste("md5 -q", paste(files, collapse=" "))
  length(unique(system(cmd, intern=TRUE))) == 1
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aaa-constants.r"
.pt <- 1 / 0.352777778
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aaa-examples.r"
TopLevel$examples <- function(.) {
  # Coming soon
}

TopLevel$examples_text <- function(.) {
  source <- attr(get("examples", .), "source")
  source <- source[-c(1, length(source))]
  
  unlist(lapply(source, function(x) gsub("^    ", "", x)))
}

TopLevel$examples_run <- function(., path = NULL, verbose=TRUE) {
  if (!.$doc) return(NULL)
  # Set seed to ensure reproducibility of examples with random components,
  # e.g. jittering
  set.seed(141079)

  require(evaluate, quiet=TRUE, warn=FALSE)
  replay(evaluate(.$examples_text()))
  invisible()
  
  # display <- function(x) {
  #   hash <- digest.ggplot(x$value)
  #   if (verbose) cat(x$src)
  #   if (is.null(path)) {
  #     timing <- try_default(system.time(print(x$value)), c(NA, NA, NA))
  #   } else {      
  #     timing <- try_default(system.time(ggsave(x$value, path=path, width=8, height=8)), c(NA, NA, NA))
  #   }
  #   timing <- unname(timing)
  #   data.frame(
  #     class = .$class(),
  #     obj = .$objname,
  #     src = x$src,
  #     hash = hash,
  #     user = timing[1],
  #     sys = timing[2],
  #     elapsed = timing[3],
  #     stringsAsFactors = FALSE
  #   )
  # }
  # out <- lapply(plots, display)
  # cat("\n")
  # invisible(do.call("rbind", out))
}

TopLevel$all_examples_run <- function(., path=NULL, verbose=TRUE) {
  # Ensure warnings display immediately
  old_opt <- options(warn = 1)
  on.exit(options(old_opt))
  
  out <- tryapply(.$find_all(), function(x) {
    if (verbose) message("Running examples for", " ", x$my_name())
    suppressMessages(x$examples_run(path, verbose))
  })
  
  invisible(do.call("rbind", compact(out)))
}

# Run all examples
# Runs all ggplot2 examples
# 
# @arguments path to save file, if non-NULL
# @arguments if TRUE, report progress during run
# @keyword internal
all_examples_run <- function(path=NULL, verbose = TRUE) {
  invisible(rbind(
    Geom$all_examples_run(path, verbose),
    Stat$all_examples_run(path, verbose),
    Scale$all_examples_run(path, verbose),
    Coord$all_examples_run(path, verbose),
    Position$all_examples_run(path, verbose),
    Facet$all_examples_run(path, verbose)
  ))
}


# Save all examples in consistent format -------------------------------------

# Save examples
# Cache output from all examples in ggplot directory
# 
# Produces:
#  * png for each example
#  * csv with hash, code and timing info
# 
# @keyword internal
save_examples <- function(name = get_rev(), verbose = FALSE) {
  path <- paste("/User/hadley/documents/ggplot/examples/ex-", name, "/", sep="")
  dir.create(path, recursive = TRUE)
  
  info <- all_examples_run(path, verbose = verbose)
  write.table(info, file=file.path(path, "info.csv"), sep=",",col=TRUE, row=FALSE, qmethod="d")
  # system(paste("pdf2png ", path, "*.pdf", sep =""))
  # system(paste("rm ", path, "*.pdf", sep =""))
  
  invisible(info)
}

# Get current revision
# Developer use only
# 
# @keyword internal
get_rev <- function() {
  cmd <- paste("git log -1 --pretty=oneline")
  out <- system(cmd, intern=T)
  substr(out, 0, 6)
}


# Profiling code -------------------------------------------------------------

TopLevel$examples_profile <- function(.) {
  sw <- stopwatch(.$examples_run())
  plotting <- Filter(function(x) any(x == "\"print.ggplot\""), sw)
  attributes(plotting) <- attributes(sw)
  plotting
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aaa-html.r"
# Description of aesthetics
.desc_aes <- list(
  "alpha" = "transparency",
  "x"= "x position",
  "y"= "y position", 
  "group"= "how observations are divided into different groups", 
  "colour"= "border colour", 
  "fill"= "internal colour", 
  "hjust"= "horizontal justification, between 0 and 1", 
  "xintercept"= "x intercept", 
  "yintercept" = "y intercept",
  "label"= "text label", 
  "linetype"= "line type", 
  "ymax"= "top (vertical maximum)", 
  "ymin"= "bottom (vertical minimum)", 
  "xmax"= "right (hortizontal maximum)", 
  "xmin"= "left (hortizontal minimum)", 
  "height"= "height", 
  "width" = "width",
  "angle"= "angle", 
  "shape"= "shape of point", 
  "size"= "size", 
  "slope"= "slope of line", 
  "quantile" = "quantile of distribution",
  "vjust"= "vertical justification, between 0 and 1", 
  "weight"= "observation weight used in statistical transformation"
)

.desc_param <- list(
  "mapping" = "mapping between variables and aesthetics generated by aes",
  "data" = "dataset used in this layer, if not specified uses plot dataset",
  "stat" = "statistic used by this layer",
  "geom" = "geometric used by this layer", 
  "position" = "position adjustment used by this layer",
  "..." = "other arguments", 
  "trans" = "a transformer to use",
  "..." = "ignored pass to geom/stat",
  "to" = "numeric vector of length 2, giving minimum and maximum after transformation",
  "na.colour" = "colour to use for missing values",
  "xlim" = "x limits",
  "ylim" = "y limits",

  "name" = "name of scale to appear in legend or on axis.  Maybe be an expression: see ?plotmath",
  "limits" = "numeric vector of length 2, giving the extent of the scale",
  "breaks" = "numeric vector indicating where breaks should lie",
  "labels" = "character vector giving labels associated with breaks",
  "expand" = "numeric vector of length 2, giving multiplicative and additive expansion factors"
)


# Generate html for index page for documentation website.
# Static header template stored in templates/index.html
#
# @keyword internal
html_index <- function() {
  ps(
    TopLevel$html_header("ggplot"),
    html_auto_link(ps(readLines("templates/index.html"), collapse="\n"), skip="ggplot"),
    "<br clear='all' />\n", 
    "<h2>Geoms</h2>\n",
    "<p>Geoms, short for geometric objects, describe the type of plot you will produce.  <a href='geom_.html'>Read more</a></p>\n",
    html_linked_list(Geom$find_all()),
    "<br clear='all' />\n", 
    "<h2>Statistics</h2>\n",
    "<p>It's often useful to transform your data before plotting, and that's what statistical transformations do.  <a href='stat_.html'>Read more</a></p>\n",
    html_linked_list(Stat$find_all()),
    "<br clear='all' />\n", 
    "<h2>Scales</h2>\n",
    "<p>Scales control the mapping between data and aesthetics.  <a href='scale_.html'>Read more</a></p>\n",
    html_linked_list(Scale$find_all()),
    "<br clear='all' />\n", 
    "<h2>Coordinate systems</h2>\n",
    "<p>Coordinate systems adjust the mapping from coordinates to the 2d plane of the computer screen.  <a href='coord_.html'>Read more</a></p>\n",
    html_linked_list(Coord$find_all()),
    "<br clear='all' />\n", 
    "<h2>Faceting</h2>\n",
    "<p>Facets display subsets of the dataset in different panels.  <a href='facet_.html'>Read more</a></p>\n",
    html_linked_list(Facet$find_all()),
    "<br clear='all' />\n", 
    "<h2>Position adjustments</h2>\n",
    "<p>Position adjustments can be used to fine tune positioning of objects to achieve effects like dodging, jittering and stacking.  <a href='position_.html'>Read more</a></p>\n",
    html_linked_list(Position$find_all()),
    TopLevel$html_footer()
  )
}

# Create physical file for html documentation index
# See \code{\link{html_index}} for more details
#
# @arguments path to create file in
# @keyword internal
html_index_create <- function(path="web/") {
  target <- ps(path, "index.html")
  
  cat(html_index(), file=target)
}  

# Create all html documentation pages
# Create all html pages including indices and templates.  Also converts
# pdfs to pngs and optimises.
# 
# @arguments path to create files in
# @keyword internal
all_html_pages_create <- function(path="web/") {
  options(warn = 1)
  system("rm web/graphics/*")
  html_template_create_all()
  html_index_create(path)
  Geom$all_html_pages_create()
  Stat$all_html_pages_create()
  Scale$all_html_pages_create()
  Coord$all_html_pages_create()
  Position$all_html_pages_create()
  Facet$all_html_pages_create()
  # system("pdf2png web/graphics/*.pdf")
  # system("rm web/graphics/*.pdf")
  system("optipng web/graphics/*.png  > /dev/null")
}

# Generate html for templated files
# See templates directory for examples.  Each template is auto linked.
#
# @arguments name of template
# @keyword internal
html_template <- function(name) {
  path <- ps("templates/", name, ".html")
  ps(
    TopLevel$html_header(name),
    html_auto_link(ps(readLines(path), collapse="\n"), skip=gsub("_","", name)),
    TopLevel$html_footer()
  )
}

# Create html file for templated files
# See \code{\link{html_template}} for more details
#
# @arguments name of template
# @arguments path to create file in
# @keyword internal
html_template_create <- function(name, path="web/") {
  cat(html_template(name), file=ps(path, name, ".html"))
}

# Create all templates
# Render all templates in templates directory
#
# @arguments path to create file in
# @keyword internal
html_template_create_all <- function(path="web/") {
  templates <- setdiff(gsub("\\.html", "", dir("templates/")), c("header", "footer"))
  invisible(lapply(templates, html_template_create, path=path))
}

# Convenience function for generating lists of objects with their icons.
# Build a bulleted list of objects with description and icons
# 
# @arguments list of objects
# @keyword internal
html_linked_list <- function(objects) {
  objects <- objects[sapply(objects, function(x) get("doc", x))]
  
  links <- sapply(objects, function(x) {
    ps(
      x$html_img_link(align="left"), 
      x$html_link_self(), "<br />\n",
      "<span class='desc'>", get("desc", x), "</span>"
    )
  })
  
  left <- rep(c(TRUE, FALSE), length=length(links))
  
  ps(
    "<ul class='icons left'>\n",
    ps("<li>", links[left] , "</li>\n"),
    "</ul>\n",
    "<ul class='icons right'>\n",
    ps("<li>", links[!left] , "</li>\n"),
    "</ul><br clear='all' />\n"
  )
}

# Create index of objects for automatically linking names in html
# Build up index of links.
# 
# @keyword internal
html_autolink_index <- function() {
  all <- c(Geom$find_all(TRUE), Stat$find_all(TRUE), Coord$find_all(TRUE), Position$find_all(TRUE), Scale$find_all(TRUE), Facet$find_all(TRUE))

  links <- lapply(all, function(.) .$html_link_self())
  names(links) <- lapply(all, function(.) .$my_name())
  .links <<- c(links, 
    # aes = "<a href='aes.html'>aes</a>", 
    ggplot = "<a href='ggplot.html'>ggplot</a>", 
    layer = "<a href='layer.html'>layer</a>", 
    qplot = "<a href='qplot.html'>qplot</a>"
    # scale = "<a href='scale_.html'>scale</a>",
    # geom = "<a href='geom_.html'>geom</a>",
    # stat = "<a href='stat_.html'>stat</a>",
    # coord = "<a href='coord_.html'>coord</a>",
    # position = "<a href='position_.html'>position</a>",
    # facet = "<a href='facet_.html'>facet</a>"
  )
}

.links <- NULL
# Add html links to functions
# Add html links to functions
# 
# @keyword internal
html_auto_link <- function(input, skip="") {
  if (!exists(".links")) html_autolink_index()
  
  for (n in names(.links)[names(.links) != skip  ]) {
    input <- gsub(ps("\\b", n, "\\b"), .links[n], input)
  }
  input
  
}

plist <- function(l) {
  if (length(l) == 0)  return()
  l <- l[names(l) != "..."]
  if (length(l) == 0)  return()
  paste(paste(names(l), l, sep="&nbsp;=&nbsp;", collapse=", "), sep="")
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aaa-rdoc.r"
# Rebuild all rdoc documentation
# Builds rdoc pages for all ggplot2 objects
# 
# @arguments path to save rd files
# @keyword internal
all_rdoc_pages_create <- function(path="web/") {
  Geom$all_rdoc_pages_create()
  Stat$all_rdoc_pages_create()
  Scale$all_rdoc_pages_create()
  Coord$all_rdoc_pages_create()
  Position$all_rdoc_pages_create()
  Facet$all_rdoc_pages_create()
}

# Name of physical file to create, doesn't include directory
TopLevel$rdoc_path <- function (.) {
  ps(.$my_name(), ".rd")
}

TopLevel$all_rdoc_pages_create <- function(., path="man/") {
  invisible(lapply(.$find_all(TRUE), function(x) x$rdoc_page_create(path)))
}
  
TopLevel$rdoc_page_create <- function(., path="man/") {
  cat("Creating rdoc documentation for", .$my_name(), "\n")
  target <- ps(path, .$rdoc_path())
  cat(.$rdoc_page(), file=target)
}  
  
TopLevel$rdoc_page <- function(.) {
  ps(
    .$rdoc_name(),
    .$rdoc_aliases(),
    .$rdoc_title(), 
    .$rdoc_description(), 
    .$rdoc_details(), 
    .$rdoc_aesthetics(), 
    .$rdoc_advice(), 
    .$rdoc_usage(),
    .$rdoc_arguments(),
    .$rdoc_seealso(),
    .$rdoc_value(),
    .$rdoc_examples(),
    .$rdoc_author(),
    .$rdoc_keyword(),
    ""
  )
}

TopLevel$rdoc_name <- function(.) {
  ps(
    "\\name{", .$my_name(), "}\n"
  )
}

TopLevel$aliases <- c()

TopLevel$rdoc_aliases <- function(.) {
  aliases <- unique(c(
    .$my_name(),
    .$my_names(),
    .$myName(),
    .$aliases
  ))
  
  ps(
    "\\alias{", gsub("%", "\\%", aliases), "}\n"
  )
}

TopLevel$rdoc_title <- function(.) {
  ps(
    "\\title{", gsub("_", "\\\\_", .$my_name()), "}\n"
  )
}

TopLevel$rdoc_description <- function(.) {
  ps(
    "\\description{", .$desc, "}\n"
  )
}

TopLevel$rdoc_details <- function(.) {
  ps(
    "\\details{\n",
    rdoc_from_html(.$details, .$my_name()),
    rdoc_from_html(ps("This page describes ", .$my_name(), ", see \\code{\\link{layer}} and \\code{\\link{qplot}} for how to create a complete plot from individual components.\n")),
    "}\n"
  )
}

TopLevel$rdoc_aesthetics <- function(.) {
  if (!exists("default_aes", .)) return("")
  
  aes <- c(.$required_aes, names(.$default_aes()))
  if (length(aes) == 0) return("")

  req <- ifelse(aes %in% .$required_aes, " (\\strong{required})", "")
  desc <- paste(defaults(.$desc_params, .desc_aes)[aes], req, sep="")

  ps(
    "\\section{Aesthetics}{\n",
    rdoc_from_html(ps("The following aesthetics can be used with ", .$my_name(), ".  Aesthetics are mapped to variables in the data with the aes function: \\code{", .$my_name(), "(aes(x = var))}"), .$my_name()), "\n", 
    "\\itemize{\n",
    ps("  \\item \\code{", aes, "}: ", desc, " \n"), 
    "}\n",
    "}\n"
  )
}


TopLevel$rdoc_advice <- function(.) {
  if (.$advice == "") return()
  ps(
    "\\section{Advice}{\n",
    rdoc_from_html(.$advice, .$my_name()),
    "}\n"
  )
}  

TopLevel$rdoc_formals <- function(.)   {
  if (exists("common", .) && !is.null(.$common)) {
    formals(get(ps(.$class(), "_", .$common[1], "_", .$objname)))    
  } else {
    formals(get(.$my_name()))
  }
  
}



TopLevel$call <- function(.) {
  args <- .$rdoc_formals()
  is.missing.arg <- function(arg) sapply(arg, typeof) == "symbol" & sapply(arg, deparse) == ""

  equals <- ifelse(is.missing.arg(args), "", "=")
  ps(
    .$my_names(), ps("(", 
    ps(names(args), equals, sapply(args, deparse), collapse=", "),
    ")"), collapse=NULL
  )
}


# FIXME: need to generate usage statements for all common scales
TopLevel$rdoc_usage <- function (.) {
  # add line breaks
  call <- deparse(parse(text = .$call())[[1]])
  
  ps(
    "\\usage{", ps(call, collapse="\n"), "}\n"
  )
}

TopLevel$rdoc_arguments <- function(.) {
  p <- names(.$rdoc_formals())
  # p <- c("mapping", "data", "stat", "position", names(.$params()), "...")
  
  ps(
    "\\arguments{\n",
      ps(" \\item{", p, "}{", defaults(.$desc_params, .desc_param)[p], "}\n"),
    "}\n"
  )
}  


TopLevel$rdoc_seealso <- function(.) {
  ps(
    "\\seealso{\\itemize{\n",
    if(length(.$seealso) > 0) {
      ps("  \\item \\code{\\link{", names(.$seealso), "}}: ", .$seealso, "\n")
    },
    "  \\item \\url{http://had.co.nz/ggplot2/", .$html_path(), "}\n",
    "}}\n"
  )
}  

TopLevel$rdoc_value <- function(.) {
  "\\value{A \\code{\\link{layer}}}\n"
}  

TopLevel$rdoc_examples <- function(.) {
  if (!.$doc) return()
  
  ps(
    "\\examples{\\dontrun{\n",
    ps(.$examples_text(), collapse="\n"),
    "\n}}\n"
  )
}

TopLevel$rdoc_author <- function(.) {
  "\\author{Hadley Wickham, \\url{http://had.co.nz/}}\n"
}  
TopLevel$rdoc_keyword<- function(.) {
  "\\keyword{hplot}\n"
}  

# rdoc auto link
# Automatically link functions used in rdoc
# 
# @arguments input rdoc string
# @arguments functions to omit
# @keyword internal
rdoc_auto_link <- function(input, skip="") {
  if (!exists(".links")) html_autolink_index()
  
  for (n in names(.links)[names(.links) != skip]) {
    input <- gsub(ps("\\b", n, "\\b"), ps("\\\\code{\\\\link{", n, "}}"), input)
  }
  input
}

# Convert rdoc to html
# Crude regexp based conversion from html to rdoc.
# 
# Assumes well-formed xhtml.  Also autolinks any ggplot functions.
# 
# @arguments input rdoc string
# @arguments pass to \code{\link{rdoc_auto_link}}
# @keyword internal
rdoc_from_html <- function(html, skip="") {
  rd <- gsub("<p>", "", html)
  rd <- gsub("</p>\n?", "\n\n", rd)

  rd <- gsub("<em>(.*?)</em>", "\\\\emph{\\1}", rd)
  rd <- gsub("<code>(.*?)</code>", "\\\\code{\\1}", rd)

  rd <- gsub("_", "\\\\_", rd)
  
  rdoc_auto_link(rd, skip)
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/aes.r"
# all_aes <- function(y) c(names(y$default_aes()), y$required_aes)
# geom_aes <- unlist(lapply(Geom$find_all(), all_aes))
# stat_aes <- unlist(lapply(Stat$find_all(), all_aes))
# all <- sort(unique(c(names(.base_to_ggplot), geom_aes, stat_aes)))
# dput(all)

.all_aesthetics <- c("adj", "alpha", "angle", "bg", "cex", "col", "color", "colour", "fg", "fill", "group", "hjust", "label", "linetype", "lower", "lty", "lwd", "max", "middle", "min", "order", "pch", "radius", "sample", "shape", "size", "srt", "upper", "vjust", "weight", "width", "x", "xend", "xmax", "xmin", "y", "yend", "ymax", "ymin", "z")


.base_to_ggplot <- c(
  "col"   = "colour",
  "color" = "colour", 
  "pch"   = "shape",
  "cex"   = "size", 
  "lty"   = "linetype", 
  "lwd"   = "size",
  "srt"   = "angle",
  "adj"   = "hjust",
  "bg"    = "fill",
  "fg"    = "colour",
  "min"   = "ymin", 
  "max"   = "ymax"
)

# Generate aesthetic mappings
# Aesthetic mappings describe how variables in the data are mapped to visual properties (aesthetics) of geoms.
# 
# aes creates a list of unevaluated expressions.  This function also performs
# partial name matching, converts color to colour, and old style R names to
# new ggplot names (eg. pch to shape, cex to size)
# 
# @arguments x value
# @arguments y value
# @arguments List of name value pairs
# @keyword hplot
# @alias str.uneval
# @alias print.uneval
# @alias [.uneval
# @alias as.character.uneval
# @seealso \code{\link{aes_string}}
#X aes(x = mpg, y = wt)
#X aes(x = mpg ^ 2, y = wt / cyl)
aes <- function(x, y, ...) {
  aes <- structure(as.list(match.call()[-1]), class="uneval")
  rename_aes(aes)
}

# Rename aesthetics
# Rename aesthetics named in American spelling or with base R graphic parameter names to ggplot2 names
# 
# @keyword internal
rename_aes <- function(x) {
  # Convert prefixes to full names
  full <- charmatch(names(x), .all_aesthetics)
  names(x)[!is.na(full)] <- .all_aesthetics[full[!is.na(full)]]
  
  rename(x, .base_to_ggplot)
}

# Aesthetic to scale
# Look up the scale that should be used for a given aesthetic
# 
# @keyword internal
aes_to_scale <- function(var) {
  var[var %in% c("x", "xmin", "xmax", "xend", "xintercept")] <- "x"
  var[var %in% c("y", "ymin", "ymax", "yend", "yintercept")] <- "y"
  
  var
}

# Is aesthetic a position aesthetic?
# Figure out if an aesthetic is a position or not
# 
# @keyword internal
is_position_aes <- function(vars) {
  aes_to_scale(vars) %in% c("x", "y")
}


# Generate aesthetic mappings from a string
# Aesthetic mappings describe how variables in the data are mapped to visual properties (aesthetics) of geoms.  Compared to aes this function operates on strings rather than expressions.
# 
# \code{aes_string} is particularly useful when writing functions that create 
# plots because you can use strings to define the aesthetic mappings, rather
# than having to mess around with expressions.
#
# @arguments List of name value pairs
# @keyword internal
# @seealso \code{\link{aes}}
#X aes_string(x = "mpg", y = "wt")
#X aes(x = mpg, y = wt)
aes_string <- function(...) {
  mapping <- list(...)
  mapping[sapply(mapping, is.null)] <- "NULL"
  
  parsed <- lapply(mapping, function(x) parse(text = x)[[1]])
  structure(rename_aes(parsed), class = "uneval")
}

# Generate identity mappings
# Given a character vector, create a set of identity mappings
# 
# @arguments vector of variable names
# @keyword internal
#X aes_all(names(mtcars))
#X aes_all(c("x", "y", "col", "pch"))
aes_all <- function(vars) {
  names(vars) <- vars
  vars <- rename_aes(vars)
  
  structure(
    lapply(vars, function(x) parse(text=x)[[1]]),
    class = "uneval"
  )
  
}

print.uneval <- function(x, ...) str(unclass(x))
str.uneval <- function(object, ...) str(unclass(object), ...)
"[.uneval" <- function(x, i, ...) structure(unclass(x)[i], class = "uneval") 

as.character.uneval <- function(x, ...) {
  char <- as.character(unclass(x))
  names(char) <- names(x)
  char
}

# Aesthetic defaults
# Convenience method for setting aesthetic defaults
# 
# @arguments values from aesthetic mappings
# @arguments defaults
# @arguments user specified values
# @value a data.frame, with all factors converted to character strings
# @keyword internal 
aesdefaults <- function(data, y., params.) {
  updated <- updatelist(y., params.)
  
  cols <- tryapply(defaults(data, updated), function(x) eval(x, data, globalenv()))
  
  # Need to be careful here because stat_boxplot uses a list-column to store
  # a vector of outliers
  cols <- Filter(function(x) is.atomic(x) || is.list(x), cols)
  list_vars <- sapply(cols, is.list)
  cols[list_vars] <- lapply(cols[list_vars], I)
  
  df <- data.frame(cols, stringsAsFactors = FALSE)
  
  factors <- sapply(df, is.factor)
  df[factors] <- lapply(df[factors], as.character)
  df
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/annotation.r"
# Annotate a plot
# Add annotations to a plot in a convenient manner
# 
# @arguments name of geom to use for annotation
# @arguments x position
# @arguments y position
# @arguments xmin position
# @arguments ymin position
# @arguments xmax position
# @arguments ymax position
# @arguments ... other arguments passed to geom as parameters
# @keyword internal
#X annotate("text", x = 0, y = 0, label = "title")
annotate <- function(geom, x = NULL, y = NULL, xmin = NULL, xmax = NULL, ymin = NULL, ymax = NULL, ...) {
  
  layer_data <- compact(list(
    x = x, xmin = xmin, xmax = xmax, 
    y = y, ymin = ymin, ymax = ymax
  ))
  
  layer(
    geom = geom, geom_params = list(...), 
    stat = "identity", 
    inherit.aes = FALSE,
    data = data.frame(layer_data), mapping = aes_all(names(layer_data)),
    legend = FALSE
  )
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-.r"
Coord <- proto(TopLevel, expr={
  limits <- list()
  class <- function(.) "coord"
  
  muncher <- function(.) FALSE
  
  # Rescaling at coord level should not be clipped: this is what 
  # makes zooming work
  rescale_var <- function(., data, range, clip = FALSE) {
    rescale(data, 0:1, range, clip = clip)
  }
  
  munch <- function(., data, details, segment_length = 0.01) {
    if (!.$muncher()) return(.$transform(data, details))
    
    # Calculate distances using coord distance metric
    dist <- .$distance(data$x, data$y, details)
    dist[data$group[-1] != data$group[-nrow(data)]] <- NA
    
    # Munch and then transform result
    munched <- munch_data(data, dist, segment_length)
    .$transform(munched, details)
  }
  
  distance <- function(., x, y, details) {
    max_dist <- dist_euclidean(details$x.range, details$y.range)    
    dist_euclidean(x, y) / max_dist
  }
    
  compute_aspect <- function(., ranges) {
    NULL
  }
  
  labels <- function(., scales) {
    scales
  }
  
  pprint <- function(., newline=TRUE) {
    args <- formals(get("new", .))
    args <- args[!names(args) %in% c(".", "...")]
  
    cat("coord_", .$objname, ": ", clist(args), sep="")
    
    if (newline) cat("\n") 
  }
  
  guide_foreground <- function(., scales, theme) {
    theme_render(theme, "panel.border")
  }  
  # Html defaults
  
  html_returns <- function(.) {
    ps(
      "<h2>Returns</h2>\n",
      "<p>This function returns a coordinate system object.</p>"
    )
  }
  
  parameters <- function(.) {
    params <- formals(get("new", .))
    params[setdiff(names(params), c("."))]
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-cartesian-.r"
CoordCartesian <- proto(Coord, expr={  
  new <- function(., xlim = NULL, ylim = NULL, wise = FALSE) {
    .$proto(limits = list(x = xlim, y = ylim), wise = wise)
  }
  
  transform <- function(., data, details) {
    rescale_x <- function(data) .$rescale_var(data, details$x.range)
    rescale_y <- function(data) .$rescale_var(data, details$y.range)
    
    data <- transform_position(data, rescale_x, rescale_y)
    transform_position(data, trim_infinite_01, trim_infinite_01)
  }
  
  compute_ranges <- function(., scales) {
    if (is.null(.$limits$x)) {
      x.range <- scales$x$output_expand()
    } else {
      if (.$wise) {
        x.range <- expand_range(range(scales$x$.tr$transform(.$limits[["x"]])), scales$x$.expand[1], scales$x$.expand[2])
        scales$x$.domain<-.$limits[["x"]]
      } else {
        x.range <- range(scales$x$.tr$transform(.$limits[["x"]]))
      }
    }
    x.major <- .$rescale_var(scales$x$input_breaks_n(), x.range, TRUE)
    x.minor <- .$rescale_var(scales$x$output_breaks(), x.range, TRUE)
    x.labels <- scales$x$labels()

    if (is.null(.$limits$y)) {
      y.range <- scales$y$output_expand()
    } else {
      if (.$wise) {
        y.range <- expand_range(range(scales$y$.tr$transform(.$limits[["y"]])), scales$y$.expand[1], scales$y$.expand[2])
        scales$y$.domain<-.$limits[["y"]]
      } else {
        y.range <- range(scales$y$.tr$transform(.$limits[["y"]]))
      }
    }
    y.major <- .$rescale_var(scales$y$input_breaks_n(), y.range, TRUE)
    y.minor <- .$rescale_var(scales$y$output_breaks(), y.range, TRUE)
    y.labels <- scales$y$labels()
    
    list(
      x.range = x.range, y.range = y.range, 
      x.major = x.major, x.minor = x.minor, x.labels = x.labels,
      y.major = y.major, y.minor = y.minor, y.labels = y.labels
    )
  }
  
  guide_axis_h <- function(., details, theme) {
    guide_axis(details$x.major, details$x.labels, "bottom", theme)
  }

  guide_axis_v <- function(., details, theme) {
    guide_axis(details$y.major, details$y.labels, "left", theme)
  }

  
  guide_background <- function(., details, theme) {
    x.major <- unit(details$x.major, "native")
    x.minor <- unit(details$x.minor, "native")
    y.major <- unit(details$y.major, "native")
    y.minor <- unit(details$y.minor, "native")
    
    guide_grid(theme, x.minor, x.major, y.minor, y.major)
  }
  
  # Documentation -----------------------------------------------

  objname <- "cartesian"
  desc <- "Cartesian coordinates"
  
  details <- "<p>The Cartesian coordinate system is the most familiar, and common, type of coordinate system.  There are no options to modify, and it is used by default, so you shouldn't need to call it explicitly</p>\n"
  
  icon <- function(.) {
    gTree(children = gList(
      segmentsGrob(c(0, 0.25), c(0.25, 0), c(1, 0.25), c(0.25, 1), gp=gpar(col="grey50", lwd=0.5)),
      segmentsGrob(c(0, 0.75), c(0.75, 0), c(1, 0.75), c(0.75, 1), gp=gpar(col="grey50", lwd=0.5)),
      segmentsGrob(c(0, 0.5), c(0.5, 0), c(1, 0.5), c(0.5, 1))
    ))
  }
  
  examples <- function(.) {
    # There are two ways of zooming the plot display: with scales or 
    # with coordinate systems.  They work in two rather different ways.
    
    (p <- qplot(disp, wt, data=mtcars) + geom_smooth())
    
    # Setting the limits on a scale will throw away all data that's not
    # inside these limits.  This is equivalent to plotting a subset of
    # the original data
    p + scale_x_continuous(limits = c(325, 500))
    
    # Setting the limits on the coordinate system performs a visual zoom
    # the data is unchanged, and we just view a small portion of the original
    # plot.  See how the axis labels are the same as the original data, and 
    # the smooth continue past the points visible on this plot.
    p + coord_cartesian(xlim = c(325, 500))
    
    # You can see the same thing with this 2d histogram
    (d <- ggplot(diamonds, aes(carat, price)) + 
      stat_bin2d(bins = 25, colour="grey50"))
    
    # When zooming the scale, the we get 25 new bins that are the same
    # size on the plot, but represent smaller regions of the data space
    d + scale_x_continuous(limits = c(0, 2))
    
    # When zooming the coordinate system, we see a subset of original 50 bins, 
    # displayed bigger
    d + coord_cartesian(xlim = c(0, 2))
  
  }

})


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-cartesian-equal.r"
CoordFixed <- proto(CoordCartesian, {

  new <- function(., ratio = 1) {
    .$proto(ratio = ratio)
  }

  compute_aspect <- function(., ranges) {
    diff(ranges$y.range) / diff(ranges$x.range) * .$ratio
  }

  # Documentation -----------------------------------------------

  objname <- "fixed"
  aliases <- "coord_equal"
  desc <- "Cartesian coordinates with fixed relationship between x and y scales."
  icon <- function(.) textGrob("=", gp = gpar(cex=3))  
  
  details <- "<p>A fixed scale coordinate system forces a specified ratio between the physical representation of data units on the axes. The ratio represents the number of units on the y-axis equivalent to one unit on the x-axis. The default, ratio = 1, ensures that one unit on the x-axis is the same length as one unit on the y-axis. Ratios higher than one make units on the y axis longer than units on the x-axis, and vice versa. This is similar to ?eqscplot in MASS, but it works for all types of graphics</p>\n"
  
  examples <- function(.) {
    # ensures that the ranges of axes are equal to the specified ratio by
    # adjusting the plot aspect ratio
    
    qplot(mpg, wt, data = mtcars) + coord_equal(ratio = 1)
    qplot(mpg, wt, data = mtcars) + coord_equal(ratio = 5)
    qplot(mpg, wt, data = mtcars) + coord_equal(ratio = 1/5)
    
    # Resize the plot to see that the specified aspect ratio is mantained
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-cartesian-flipped.r"
CoordFlip <- proto(CoordCartesian, expr={
  
  transform <- function(., data, details) {
    rescale_x <- function(data) .$rescale_var(data, details$x.range)
    rescale_y <- function(data) .$rescale_var(data, details$y.range)
    
    data <- transform_position(data, rescale_y, rescale_x)
    data <- transform_position(data, trim_infinite_01, trim_infinite_01)
    
    rename(data, c(
      x = "y",       y = "x", 
      xend = "yend", yend = "xend", 
      xmin = "ymin", ymin = "xmin",
      xmax = "ymax", ymax = "xmax")
    )
  }

  compute_ranges <- function(., scales) {
    details <- .super$compute_ranges(., scales)
    with(details, list(
      x.range = y.range, y.range = x.range, 
      x.major = y.major, x.minor = y.minor, x.labels = y.labels,
      y.major = x.major, y.minor = x.minor, y.labels = x.labels
    ))
  }
  
  labels <- function(., scales) {
    list(
      x = scales$y,
      y = scales$x
    )
  }
  

  # Documentation -----------------------------------------------

  objname <- "flip"
  desc <- "Flipped cartesian coordinates"
  details <- "<p>Flipped cartesian coordinates so that horizontal becomes vertical, and vertical, horizontal.  This is primarily useful for converting geoms and statistics which display y conditional on x, to x conditional on y</p>"
  icon <- function(.) {
    angles <- seq(0, pi/2, length=20)[-c(1, 20)]
    gTree(children=gList(
      segmentsGrob(0, 0, 0, 1),
      segmentsGrob(0, 0, 1, 0),
      linesGrob(0.9 * sin(angles), 0.9 * cos(angles), arrow=arrow(length=unit(0.05, "npc"))),
      linesGrob(0.5 * sin(angles), 0.5 * cos(angles), arrow=arrow(end="first", length= unit(0.05, "npc")))
    ))
  }
  
  examples <- function(.) {
    # Very useful for creating boxplots, and other interval
    # geoms in the horizontal instead of vertical position.
    qplot(cut, price, data=diamonds, geom="boxplot")
    last_plot() + coord_flip()

    qplot(cut, data=diamonds, geom="bar")
    last_plot() + coord_flip()
    
    qplot(carat, data=diamonds, geom="histogram")
    last_plot() + coord_flip()

    # You can also use it to flip lines and area plots:
    qplot(1:5, (1:5)^2, geom="line")
    last_plot() + coord_flip()
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-map.r"
CoordMap <- proto(Coord, {  
  new <- function(., projection="mercator", ..., orientation = NULL, xlim = NULL, ylim = NULL, fast = TRUE) {
    if (!fast) {
      warning("Fast parameter now ignored.  Munching always occurs", 
        call. = FALSE)
    }
    
    try_require("mapproj")
    .$proto(
      projection = projection, 
      orientation = orientation,
      xlim = xlim,
      ylim = ylim,
      params = list(...)
    )
  }
  
  muncher <- function(.) TRUE
  
  transform <- function(., data, details) {
    trans <- .$mproject(data$x, data$y, details$orientation)
    out <- cunion(trans[c("x", "y")], data)
    
    out$x <- rescale(out$x, 0:1, details$x.range, clip = FALSE)
    out$y <- rescale(out$y, 0:1, details$y.range, clip = FALSE)
    out
  }
  
  distance <- function(., x, y, details) {
    max_dist <- dist_central_angle(details$x.raw, details$y.raw)
    dist_central_angle(x, y) / max_dist
  }
  
  compute_aspect <- function(., ranges) {
    diff(ranges$y.range) / diff(ranges$x.range)
  }
  
  
  mproject <- function(., x, y, orientation) {    
    suppressWarnings(do.call("mapproject",  list(
      data.frame(x = x, y = y), 
      projection = .$projection, 
      parameters  = .$params, 
      orientation = orientation
    )))
  }

  compute_ranges <- function(., scales) {
    x.raw <- .$xlim %||% scales$x$output_expand()
    y.raw <- .$ylim %||% scales$y$output_expand()
    orientation <- .$orientation %||% c(90, 0, mean(x.raw))
    
    # Increase chances of creating valid boundary region
    grid <- expand.grid(
      x = seq(x.raw[1], x.raw[2], length = 50),
      y = seq(y.raw[1], y.raw[2], length = 50)
    )
    range <- .$mproject(grid$x, grid$y, orientation)$range
    
    x.range <- range[1:2]
    x.major <- scales$x$input_breaks_n()
    x.minor <- scales$x$output_breaks()
    x.labels <- scales$x$labels()

    y.range <- range[3:4]
    y.major <- scales$y$input_breaks_n()
    y.minor <- scales$y$output_breaks()
    y.labels <- scales$y$labels()
    
    list(
      x.raw = x.raw, y.raw = y.raw, orientation = orientation,
      x.range = x.range, y.range = y.range, 
      x.major = x.major, x.minor = x.minor, x.labels = x.labels,
      y.major = y.major, y.minor = y.minor, y.labels = y.labels
    )
  }
  
  guide_background <- function(., details, theme) {    
    xrange <- expand_range(details$x.raw, 0.2)
    yrange <- expand_range(details$y.raw, 0.2)
    xgrid <- with(details, expand.grid(
      y = c(seq(yrange[1], yrange[2], len = 50), NA),
      x = x.major
    ))
    ygrid <- with(details, expand.grid(
      x = c(seq(xrange[1], xrange[2], len = 50), NA), 
      y = y.major
    ))
    
    xlines <- .$transform(xgrid, details)
    ylines <- .$transform(ygrid, details)

    ggname("grill", grobTree(
      theme_render(theme, "panel.background"),
      theme_render(
        theme, "panel.grid.major", name = "x", 
        xlines$x, xlines$y, default.units = "native"
      ),
      theme_render(
        theme, "panel.grid.major", name = "y", 
        ylines$x, ylines$y, default.units = "native"
      )
    ))
  }  

  guide_axis_h <- function(., details, theme) {
    x_intercept <- with(details, data.frame(
      x = x.major,
      y = y.raw[1]
    ))
    pos <- .$transform(x_intercept, details)
    
    guide_axis(pos$x, details$x.labels, "bottom", theme)
  }
  guide_axis_v <- function(., details, theme) {
    x_intercept <- with(details, data.frame(
      x = x.raw[1],
      y = y.major
    ))
    pos <- .$transform(x_intercept, details)
    
    guide_axis(pos$y, details$y.labels, "left", theme)
  }


  # Documentation -----------------------------------------------

  objname <- "map"
  desc <- "Map projections"
  icon <- function(.) {
    nz <- data.frame(map("nz", plot=FALSE)[c("x","y")])
    nz$x <- nz$x - min(nz$x, na.rm=TRUE)
    nz$y <- nz$y - min(nz$y, na.rm=TRUE)
    nz <- nz / max(nz, na.rm=TRUE)
    linesGrob(nz$x, nz$y, default.units="npc")
  }
  
  desc_params <- list(
    "projection" = "projection to use, see ?mapproject for complete list",
    "..." = "other arguments passed on to mapproject",
    "orientation" = "orientation, which defaults to c(90, 0, mean(range(x))).  This is not optimal for many projections, so you will have to supply your own."
  )
  
  details <- "<p>This coordinate system provides the full range of map projections available in the mapproj package.</p>\n\n<p>This is still experimental, and if you have any advice to offer regarding a better (or more correct) way to do this, please let me know</p>\n"
  
  examples <- function(.) {
    try_require("maps")
    # Create a lat-long dataframe from the maps package
    nz <- data.frame(map("nz", plot=FALSE)[c("x","y")])
    (nzmap <- qplot(x, y, data=nz, geom="path"))
    
    nzmap + coord_map()
    nzmap + coord_map(project="cylindrical")
    nzmap + coord_map(project='azequalarea',orientation=c(-36.92,174.6,0))
    
    states <- data.frame(map("state", plot=FALSE)[c("x","y")])
    (usamap <- qplot(x, y, data=states, geom="path"))
    usamap + coord_map()
    # See ?mapproject for coordinate systems and their parameters
    usamap + coord_map(project="gilbert")
    usamap + coord_map(project="lagrange")

    # For most projections, you'll need to set the orientation yourself
    # as the automatic selection done by mapproject is not available to
    # ggplot
    usamap + coord_map(project="orthographic")
    usamap + coord_map(project="stereographic")
    usamap + coord_map(project="conic", lat0 = 30)
    usamap + coord_map(project="bonne", lat0 = 50)
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-polar.r"
CoordPolar <- proto(Coord, {

  new <- function(., theta="x", start = 0, direction = 1, expand = FALSE) {
    theta <- match.arg(theta, c("x", "y"))
    r <- if (theta == "x") "y" else "x"

    c(
      .$proto(
        theta = theta, r = r, 
        start = start, direction = sign(direction),
        expand = expand
      ), 
      list(opts(aspect.ratio = 1))
    )
  }
  
  distance <- function(., x, y, details) {
    max_dist <- 2*pi*abs(diff(details$r.range))
    
    if (.$theta == "x") {
      r <- y
      theta <- .$theta_rescale_no_clip(x, details)
    } else {
      r <- x
      theta <- .$theta_rescale_no_clip(y, details)
    }
    px <- r*cos(theta)
    py <- r*sin(theta)
    pz <- theta*r

    sqrt(diff(px)^2+diff(py)^2+diff(pz)^2) / max_dist
  }

  compute_ranges <- function(., scales) {
    if (.$expand) {
      x.range <- scales$x$output_expand() 
      y.range <- scales$y$output_expand() 
    } else {
      x.range <- scales$x$output_set() 
      y.range <- scales$y$output_set() 
    }

    x.major <- scales$x$input_breaks_n()
    x.minor <- scales$x$output_breaks()
    x.labels <- scales$x$labels()

    y.major <- scales$y$input_breaks_n()
    y.minor <- scales$y$output_breaks()
    y.labels <- scales$y$labels()
    
    details <- list(
      x.range = x.range, y.range = y.range, 
      x.major = x.major, x.minor = x.minor, x.labels = x.labels,
      y.major = y.major, y.minor = y.minor, y.labels = y.labels
    )
    
    if (.$theta == "y") {
      names(details) <- gsub("x\\.", "r.", names(details))
      names(details) <- gsub("y\\.", "theta.", names(details))
    } else {
      names(details) <- gsub("x\\.", "theta.", names(details))      
      names(details) <- gsub("y\\.", "r.", names(details))
    }
    details
  }

  rename_data <- function(., data) {
    if (.$theta == "y") {
      rename(data, c("y" = "theta", "x" = "r"))
    } else {
      rename(data, c("y" = "r", "x" = "theta"))
    }
  }

  theta_rescale_no_clip <- function(., x, details) {
    rotate <- function(x) (x + .$start) * .$direction
    rotate(rescale(x, c(0, 2 * pi), details$theta.range, clip = FALSE))
  }

  theta_rescale <- function(., x, details) {
    rotate <- function(x) (x + .$start) %% (2 * pi) * .$direction
    rotate(rescale(x, c(0, 2 * pi), details$theta.range))
  }
    
  r_rescale <- function(., x, details) {
    rescale(x, c(0, 0.4), details$r.range)
  }

  muncher <- function(.) TRUE
  transform <- function(., data, details) {
    data <- .$rename_data(data)
    
    data <- within(data, {
      r <- .$r_rescale(r, details)
      theta <- .$theta_rescale(theta, details)

      x <- r * sin(theta) + 0.5
      y <- r * cos(theta) + 0.5
    })
  }
  
  guide_axis_v <- function(., details, theme) {
    guide_axis(.$r_rescale(details$r.major, details) + 0.5, details$r.labels, "left", theme)
  }
  guide_axis_h <- function(., details, theme) {
    guide_axis(NA, "", "bottom", theme)
  }
  
  guide_background <- function(., details, theme) {
    details <- .$rename_data(details)
    
    theta <- .$theta_rescale(details$theta.major, details)
    thetamin <- .$theta_rescale(details$theta.minor, details)
    thetafine <- seq(0, 2 * pi, length=100)    
    
    r <- 0.4
    rfine <- c(.$r_rescale(details$r.major, details), 0.45)

    ggname("grill", grobTree(
      theme_render(theme, "panel.background"),
      if (length(labels) > 0) theme_render(
        theme, "panel.grid.major", name = "angle", 
        x = c(rbind(0, 0.45 * sin(theta))) + 0.5, 
        y = c(rbind(0, 0.45 * cos(theta))) + 0.5,
        id.lengths = rep(2, length(theta)), 
        default.units="native"
      ),
      theme_render(
        theme, "panel.grid.minor", name = "angle", 
        x = c(rbind(0, 0.45 * sin(thetamin))) + 0.5, 
        y = c(rbind(0, 0.45 * cos(thetamin))) + 0.5,
        id.lengths = rep(2, length(thetamin)),  
        default.units="native"
      ),
      
      theme_render(
        theme, "panel.grid.major", name = "radius",
        x = rep(rfine, each=length(thetafine)) * sin(thetafine) + 0.5, 
        y = rep(rfine, each=length(thetafine)) * cos(thetafine) + 0.5,
        id.lengths = rep(length(thetafine), length(rfine)),
        default.units="native"
      )
    ))
  }

  guide_foreground <- function(., details, theme) {
    theta <- .$theta_rescale(details$theta.major, details)
    labels <- details$theta.labels
    
    # Combine the two ends of the scale if they are close
    theta <- theta[!is.na(theta)]
    ends_apart <- (theta[length(theta)] - theta[1]) %% (2*pi)
    if (ends_apart < 0.05) {
      n <- length(labels)
      if (is.expression(labels)) {
        combined <- substitute(paste(a, "/", b), 
          list(a = labels[[1]], b = labels[[n]]))
      } else {
        combined <- paste(labels[1], labels[n], sep="/")
      }
      labels[[n]] <- combined
      labels <- labels[-1]
      theta <- theta[-1]
    }
      
    grobTree(
      if (length(labels) > 0) theme_render(
        theme, "axis.text.x", 
        labels, 0.45 * sin(theta) + 0.5, 0.45 * cos(theta) + 0.5,
        hjust = 0.5, vjust = 0.5,
        default.units="native"
      ),      
      theme_render(theme, "panel.border")
    )
  }  

    

  # Documentation -----------------------------------------------

  objname <- "polar"
  desc <- "Polar coordinates"
  icon <- function(.) circleGrob(r = c(0.1, 0.25, 0.45), gp=gpar(fill=NA))
  
  details <- "<p>The polar coordinate system is most commonly used for pie charts, which are a stacked bar chart in polar coordinates.</p>\n\n<p>This coordinate system has one argument, <code>theta</code>, which determines which variable is mapped to angle and which to radius.  Valid values are \"x\" and \"y\".</p>\n"
  
  desc_params <- list(
    theta = "variable to map angle to ('x' or 'y')",
    start = "offset from 12 o'clock in radians",
    direction = "1, clockwise; -1, anticlockwise",
    expand = "should axes be expanded to slightly outside the range of the data? (default: FALSE)"
  )
  
  examples <- function(.) {
    # NOTE: Use these plots with caution - polar coordinates has
    # major perceptual problems.  The main point of these examples is 
    # to demonstrate how these common plots can be described in the
    # grammar.  Use with EXTREME caution.

    # A coxcomb plot = bar chart + polar coordinates
    cxc <- ggplot(mtcars, aes(x = factor(cyl))) + 
      geom_bar(width = 1, colour = "black")
    cxc + coord_polar()
    # A new type of plot?
    cxc + coord_polar(theta = "y")
    
    # A pie chart = stacked bar chart + polar coordinates
    pie <- ggplot(mtcars, aes(x = factor(1), fill = factor(cyl))) +
     geom_bar(width = 1)
    pie + coord_polar(theta = "y")

    # The bullseye chart
    pie + coord_polar()
    
    # Hadley's favourite pie chart
    df <- data.frame(
      variable = c("resembles", "does not resemble"),
      value = c(80, 20)
    )
    ggplot(df, aes(x = "", y = value, fill = variable)) + 
      geom_bar(width = 1) + 
      scale_fill_manual(values = c("red", "yellow")) + 
      coord_polar("y", start=pi / 3) + 
      opts(title = "Pac man")
    
    # Windrose + doughnut plot
    movies$rrating <- cut_interval(movies$rating, length = 1)
    movies$budgetq <- cut_number(movies$budget, 4)
    
    doh <- ggplot(movies, aes(x = rrating, fill = budgetq))
    
    # Wind rose
    doh + geom_bar(width = 1) + coord_polar()
    # Race track plot
    doh + geom_bar(width = 0.9, position = "fill") + coord_polar(theta = "y")
  }

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-transform.r"
CoordTrans <- proto(CoordCartesian, expr={
  
  new <- function(., xtrans="identity", ytrans="identity") {
    if (is.character(xtrans)) xtrans <- Trans$find(xtrans)
    if (is.character(ytrans)) ytrans <- Trans$find(ytrans)
    .$proto(xtr = xtrans, ytr = ytrans)
  }
  
  muncher <- function(.) TRUE
  
  distance <- function(., x, y, details) {
    max_dist <- dist_euclidean(details$x.range, details$y.range)
    dist_euclidean(.$xtr$transform(x), .$ytr$transform(y)) / max_dist
  }  

  transform <- function(., data, details) {
    trans_x <- function(data) .$transform_x(data, details$x.range)
    trans_y <- function(data) .$transform_y(data, details$y.range)
    
    data <- transform_position(data, trans_x, trans_y)
    transform_position(data, trim_infinite_01, trim_infinite_01)
  }
  transform_x <- function(., data, range) {
    rescale(.$xtr$transform(data), 0:1, range, clip = FALSE)
  }
  transform_y <- function(., data, range) {
    rescale(.$ytr$transform(data), 0:1, range, clip = FALSE)
  }

  compute_ranges <- function(., scales) {
    trans_range <- function(x, expand) {
      # range is necessary in case transform has flipped min and max
      expand_range(range(x, na.rm = TRUE), expand)
    }
    
    x.range <- trans_range(.$xtr$transform(scales$x$output_set()),
      scales$x$.expand)
    x.major <- .$transform_x(scales$x$input_breaks_n(), x.range)
    x.minor <- .$transform_x(scales$x$output_breaks(), x.range)
    x.labels <- scales$x$labels()

    y.range <- trans_range(.$ytr$transform(scales$y$output_set()),
      scales$y$.expand)
    y.major <- .$transform_y(scales$y$input_breaks_n(), y.range)
    y.minor <- .$transform_y(scales$y$output_breaks(), y.range)
    y.labels <- scales$y$labels()
    
    list(
      x.range = x.range, y.range = y.range, 
      x.major = x.major, x.minor = x.minor, x.labels = x.labels,
      y.major = y.major, y.minor = y.minor, y.labels = y.labels
    )
  }


  pprint <- function(., newline=TRUE) {
    cat("coord_", .$objname, ": ", 
      "x = ", .$xtr$objname, ", ", 
      "y = ", .$ytr$objname, sep = ""
    )
    
    if (newline) cat("\n") 
  }


  # Documentation -----------------------------------------------

  objname <- "trans"
  desc <- "Transformed cartesian coordinate system"
  details <- ""
  icon <- function(.) {
    breaks <- cumsum(1 / 2^(1:5))
    gTree(children=gList(
      segmentsGrob(breaks, 0, breaks, 1),
      segmentsGrob(0, breaks, 1, breaks)
    ))
  }
  
  examples <- function(.) {
    # See ?geom_boxplot for other examples
    
    # Three ways of doing transformating in ggplot:
    #  * by transforming the data
    qplot(log10(carat), log10(price), data=diamonds)
    #  * by transforming the scales
    qplot(carat, price, data=diamonds, log="xy")
    qplot(carat, price, data=diamonds) + scale_x_log10() + scale_y_log10()
    #  * by transforming the coordinate system:
    qplot(carat, price, data=diamonds) + coord_trans(x = "log10", y = "log10")

    # The difference between transforming the scales and
    # transforming the coordinate system is that scale
    # transformation occurs BEFORE statistics, and coordinate
    # transformation afterwards.  Coordinate transformation also 
    # changes the shape of geoms:
    
    d <- subset(diamonds, carat > 0.5)
    qplot(carat, price, data = d, log="xy") + 
      geom_smooth(method="lm")
    qplot(carat, price, data = d) + 
      geom_smooth(method="lm") +
      coord_trans(x = "log10", y = "log10")
      
    # Here I used a subset of diamonds so that the smoothed line didn't
    # drop below zero, which obviously causes problems on the log-transformed
    # scale
    
    # With a combination of scale and coordinate transformation, it's
    # possible to do back-transformations:
    qplot(carat, price, data=diamonds, log="xy") + 
      geom_smooth(method="lm") + 
      coord_trans(x="pow10", y="pow10")
    # cf.
    qplot(carat, price, data=diamonds) + geom_smooth(method = "lm")
    
  }

  
})


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/date-time-breaks.r"
# Time breaks
# Automatically compute sensible axis breaks for time data
# 
# @arguments range in seconds
# @keyword internal
time_breaks <- function(seconds) {
  days <- seconds / 86400
  if (days > 5) {
    return(date_breaks(days))
  }
  
  # seconds, minutes, hours, days
  length <- cut(seconds, c(0, 60, 3600, 24 * 3600, Inf) * 1.1, labels=FALSE)
  duration <- c(1, 60, 3600, 24 * 3600)
  units <- round(seconds / duration[length])
  
  major_mult <- ceiling(diff(pretty(c(0, units)))[1])
  minor_mult <- ceiling(diff(pretty(c(0, units), n = 15))[1])
  major <-  c("sec", "min",   "hour",  "day")[length]  
  format <-  c("%S", "%M.%S", "%H:%M", "%d-%b")[length]

  list(
    major = paste(major_mult, major), 
    minor = paste(minor_mult, major), 
    format = format
  )
  
}

# Date breaks
# Automatically compute sensible axis breaks for date data
# 
# @arguments range in days
# @keyword internal
date_breaks <- function(days) {
  length <- cut(days, c(0, 10, 56, 365, 730, 5000, Inf), labels=FALSE)

  major <- 
    c("days", "weeks", "months", "3 months", "years", "5 years")[length]
  minor <- 
    c("10 years", "days", "weeks", "months", "months", "years")[length]
  format <- 
    c("%d-%b", "%d-%b", "%b-%y", "%b-%y", "%Y", "%Y")[length]

  list(major = major, minor = minor, format = format)
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/date-time.r"
# Floor for dates and times
# Round date up to nearest multiple of time
# 
# @arguments date to round
# @arguments unit of time to round to (see \code{\link{cut.Date}}) for valid values
# @keyword internal
# @alias floor_time
floor_date <- function(date, time) {
  prec <- parse_unit_spec(time)
  if (prec$unit == "day") {
    structure(round_any(as.numeric(date), prec$mult), class="Date")
  } else {
    as.Date(cut(date, time, right = TRUE, include.lowest = TRUE))
  }
}
floor_time <- function(date, time) {
  prec <- parse_unit_spec(time)
  if (prec$unit == "sec") {
    to_time(round_any(as.numeric(date), prec$mult))
  } else if (prec$unit == "min") {
    to_time(round_any(as.numeric(date), prec$mult * 60))    
  } else {
    as.POSIXct(
      cut(date, time, right = TRUE, include.lowest = TRUE), 
      tz = attr(date, "tz") %||% ""
    )  
  }
}

# Parse date time unit specification
# Parse the time unit specification used by \code{\link{cut.Date}} into something useful
# 
# @keyword internal
parse_unit_spec <- function(unitspec) {
  parts <- strsplit(unitspec, " ")[[1]]
  if (length(parts) == 1) {
    mult <- 1
    unit <- unitspec
  } else {
    mult <- as.numeric(parts[[1]])
    unit <- parts[[2]]
  }
  unit <- gsub("s$", "", unit)
  
  list(unit = unit, mult = mult)
}

# Ceiling for dates and times
# Round date down to nearest multiple of time
# 
# @arguments date to round
# @arguments unit of time to round to (see \code{\link{cut.Date}}) for valid values
# @keyword internal
# @alias ceiling_time
ceiling_date <- function(date, time) { 
  prec <- parse_unit_spec(time)
  
  up <- c("day" = 1, "week" = 7, "month" = 31, "year" = 365)
  date <- date + prec$mult * up[prec$unit]
  
  floor_date(date, time)
}

ceiling_time <- function(date, time) { 
  prec <- parse_unit_spec(time)
  
  up <- c(
    "sec" = 1, "min" = 60, "hour" = 3600, 
    c("day" = 1, "week" = 7, "month" = 31, "year" = 365) * 3600 * 24
  )
  date <- date + prec$mult * up[prec$unit]
  
  floor_time(date, time)
}

# Fullseq for dates and times
# Analog of \code{link{fullseq}}, but for dates and times
# 
# Use in \code{\link{scale_date}}
# 
# @arguments range of dates
# @arguments unit of time to round to
# @keyword internal
# @alias fullseq_time
fullseq_date <- function(range, time) {
  seq.Date(
    floor_date(range[1], time), 
    ceiling_date(range[2], time), 
    by=time
  )
}
fullseq_time <- function(range, time) {
  seq.POSIXt(
    floor_time(range[1], time),
    ceiling_time(range[2], time),
    by=time
  )
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/facet-.r"
Facet <- proto(TopLevel, {
  clone <- function(.) {
    as.proto(.$as.list(all.names=TRUE), parent=.) 
  }
  objname <- "Facet"
  class <- function(.) "facet"
  
  html_returns <- function(.) {
    ps(
      "<h2>Returns</h2>\n",
      "<p>This function returns a facet object.</p>"
    )
  }
  
  parameters <- function(.) {
    params <- formals(get("new", .))
    params[setdiff(names(params), c(".","variable"))]
  }
  
  xlabel <- function(., theme) {
    nulldefault(.$scales$x[[1]]$name, theme$labels$x)
  }
    
  ylabel <- function(., theme) 
    nulldefault(.$scales$y[[1]]$name, theme$labels$y)
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/facet-grid-.r"
FacetGrid <- proto(Facet, {
  new <- function(., facets = . ~ ., margins = FALSE, scales = "fixed", space = "fixed", labeller = "label_value", as.table = TRUE, widths = NULL, heights = NULL) {
    scales <- match.arg(scales, c("fixed", "free_x", "free_y", "free"))
    free <- list(
      x = any(scales %in% c("free_x", "free")),
      y = any(scales %in% c("free_y", "free"))
    )
    space <- match.arg(space, c("fixed", "free"))
    
    if (is.formula(facets)) facets <- deparse(facets) 
    .$proto(
      facets = facets, margins = margins,
      free = free, space_is_free = (space == "free"),
      scales = NULL, labeller = list(labeller), as.table = as.table,
      space_widths = widths, space_heights = heights
    )
  }
  
  conditionals <- function(.) {
    vars <- all.vars(as.formula(.$facets))
    setdiff(vars, c(".", "..."))
  }
  
  
  # Initialisation  
  initialise <- function(., data) {
    .$facet_levels <- unique(
      ldply(data, failwith(NULL, "[", quiet = TRUE), .$conditionals()))
    
    .$shape <- stamp(.$facet_levels, .$facets, margins = .$margins,
      function(x) 0)
  }

  
  stamp_data <- function(., data) {
    data <- add_missing_levels(data, .$facet_levels)
    data <- lapply(data, function(df) {
      if (empty(df)) return(force_matrix(data.frame()))
      df <- stamp(add_group(df), .$facets, force, 
        margins=.$margins, fill = list(data.frame()), add.missing = TRUE)
      force_matrix(df)
    })
  }
  
  # Create grobs for each component of the panel guides
  add_guides <- function(., data, panels_grob, coord, theme) {

    aspect_ratio <- theme$aspect.ratio
    
    # If user hasn't set aspect ratio, and we have fixed scales, then
    # ask the coordinate system if it wants to specify one
    if (is.null(aspect_ratio) && !.$free$x && !.$free$y) {
      xscale <- .$scales$x[[1]]
      yscale <- .$scales$y[[1]]
      ranges <- coord$compute_ranges(list(x = xscale, y = yscale))
      aspect_ratio <- coord$compute_aspect(ranges)
    }
    
    if (is.null(aspect_ratio)) {
      aspect_ratio <- 1
      respect <- FALSE
    } else {
      respect <- TRUE
    }

    nr <- nrow(panels_grob)
    nc <- ncol(panels_grob)
    
    coord_details <- matrix(list(), nrow = nr, ncol = nc)
    for (i in seq_len(nr)) {
      for(j in seq_len(nc)) {
        scales <- list(
          x = .$scales$x[[j]]$clone(), 
          y = .$scales$y[[i]]$clone()
        )        
        coord_details[[i, j]] <- coord$compute_ranges(scales)
      }
    }
    
    # Horizontal axes
    axes_h <- list()
    for(i in seq_along(.$scales$x)) {
      axes_h[[i]] <- coord$guide_axis_h(coord_details[[1, i]], theme)
    }
    axes_h_height <- do.call("max2", llply(axes_h, grobHeight))
    axeshGrid <- grobGrid(
      "axis_h", axes_h, nrow = 1, ncol = nc,
      heights = axes_h_height, clip = "off"
    )
    
    
    # Vertical axes
    axes_v <- list()
    for(i in seq_along(.$scales$y)) {
      axes_v[[i]] <- coord$guide_axis_v(coord_details[[i, 1]], theme)
    }    
    axes_v_width <- do.call("max2", llply(axes_v, grobWidth))
    axesvGrid <- grobGrid(
      "axis_v", axes_v, nrow = nr, ncol = 1,
      widths = axes_v_width, as.table = .$as.table, clip = "off"
    )
    
    # Strips
    labels <- .$labels_default(.$shape, theme)
    
    strip_widths <- llply(labels$v, grobWidth)
    strip_widths <- do.call("unit.c", llply(1:ncol(strip_widths), 
      function(i) do.call("max2", strip_widths[, i])))
    stripvGrid <- grobGrid(
      "strip_v", t(labels$v), nrow = nrow(labels$v), ncol = ncol(labels$v),
      widths = strip_widths, as.table = .$as.table
    )

    strip_heights <- llply(labels$h, grobHeight)
    strip_heights <- do.call("unit.c", llply(1:nrow(strip_heights),
       function(i) do.call("max2", strip_heights[i, ])))
    striphGrid <- grobGrid(
      "strip_h", t(labels$h), nrow = nrow(labels$h), ncol = ncol(labels$h),
      heights = strip_heights
    )
      
    # Add background and foreground to panels
    panels <- matrix(list(), nrow=nr, ncol = nc)
    for(i in seq_len(nr)) {
      for(j in seq_len(nc)) {
        fg <- coord$guide_foreground(coord_details[[i, j]], theme)
        bg <- coord$guide_background(coord_details[[i, j]], theme)

        panels[[i,j]] <- grobTree(bg, panels_grob[[i, j]], fg)
      }
    }

    if(.$space_is_free) {
      size <- function(y) unit(diff(y$output_expand()), "null")
      panel_widths <- do.call("unit.c", llply(.$scales$x, size))
      panel_heights <- do.call("unit.c", llply(.$scales$y, size))
    } else {
      if (!is.null(.$space_widths)) {
        panel_widths <- do.call("unit.c", lapply(.$space_widths, function(x)unit(x, "null")))
      } else {
        panel_widths <- unit(1, "null")
      }
      if (!is.null(.$space_heights)) {
        panel_heights <- do.call("unit.c", lapply(.$space_heights, function(x)unit(x, "null")))
      } else {
        panel_heights <- unit(1 * aspect_ratio, "null")
      }
    }
    

    panelGrid <- grobGrid(
      "panel", t(panels), ncol = nc, nrow = nr,
      widths = panel_widths, heights = panel_heights, as.table = .$as.table,
      respect = respect
    )
       
    # Add gaps and compute widths and heights
    fill_tl <- spacer(nrow(labels$h), 1)
    fill_tr <- spacer(nrow(labels$h), ncol(labels$v))
    fill_bl <- spacer(1, 1)
    fill_br <- spacer(1, ncol(labels$v))
    
    all <- rbind(
      cbind(fill_tl,   striphGrid, fill_tr),
      cbind(axesvGrid, panelGrid,  stripvGrid),
      cbind(fill_bl,   axeshGrid,  fill_br) 
    )
    # theme$panel.margin, theme$panel.margin
    
    # from left to right
    hgap_widths <- do.call("unit.c", compact(list(
      unit(0, "cm"), # no gap after axis
      rep.unit2(theme$panel.margin, nc - 1), # gap after all panels except last
      unit(rep(0, ncol(stripvGrid) + 1), "cm") # no gap after strips 
    )))
    hgap <- grobGrid("hgap", 
      ncol = ncol(all), nrow = nrow(all),
      widths = hgap_widths, 
    )
    
    # from top to bottom
    vgap_heights <- do.call("unit.c", compact(list(
      rep(unit(0, "cm"), 2), # no gap before and after axis
      rep.unit2(theme$panel.margin, nr - 1), # gap after all panels except last
      unit(rep(0, nrow(striphGrid)), "cm") # no gap after strips
    )))
    
    vgap <- grobGrid("vgap",
      nrow = nrow(all), ncol = ncol(all) * 2,
      heights = vgap_heights
    )
    
    rweave(cweave(all, hgap), vgap)
  }


  labels_default <- function(., gm, theme) {
    labeller <- match.fun(.$labeller[[1]])
    add.names <- function(x) {
      for(i in 1:ncol(x)) x[[i]] <- labeller(colnames(x)[i], x[,i])
      x
    }

    row.labels <- add.names(rrownames(gm))
    col.labels <- add.names(rcolnames(gm))

    strip_h <- apply(col.labels, c(2,1), ggstrip, theme = theme)
    if (nrow(strip_h) == 1 && ncol(strip_h) == 1) strip_h <- matrix(list(zeroGrob()))
    strip_v <- apply(row.labels, c(1,2), ggstrip, horizontal=FALSE, theme=theme)
    if (nrow(strip_v) == 1 && ncol(strip_v) == 1) strip_v <- matrix(list(zeroGrob()))

    list(
      h = strip_h, 
      v = strip_v
    )
  }
  
  # Position scales ----------------------------------------------------------
  
  position_train <- function(., data, scales) {
    if (is.null(.$scales$x) && scales$has_scale("x")) {
      .$scales$x <- scales_list(
        scales$get_scales("x"), ncol(.$shape), .$free$x)
    }
    if (is.null(.$scales$y) && scales$has_scale("y")) {
      .$scales$y <- scales_list(
        scales$get_scales("y"), nrow(.$shape), .$free$y)
    }
    
    lapply(data, function(l) {
      for(i in seq_along(.$scales$x)) {
        lapply(l[, i], .$scales$x[[i]]$train_df, drop = .$free$x)
      }
      for(i in seq_along(.$scales$y)) {
        lapply(l[i, ], .$scales$y[[i]]$train_df, drop = .$free$y)
      }
    })
  }
  
  position_map <- function(., data, scales) {
    lapply(data, function(l) {
      for(i in seq_along(.$scales$x)) {
        l[, i] <- lapply(l[, i], function(old) {
          if (is.null(old)) return(data.frame())
          new <- .$scales$x[[i]]$map_df(old)
          cbind(new, old[setdiff(names(old), names(new))])
        }) 
      }
      for(i in seq_along(.$scales$y)) {
        l[i, ] <- lapply(l[i, ], function(old) {
          if (is.null(old)) return(data.frame())
          new <- .$scales$y[[i]]$map_df(old)
          cbind(new, old[setdiff(names(old), names(new))])
        }) 
      }
      l
    })
  }
  
  make_grobs <- function(., data, layers, coord) {
    lapply(seq_along(data), function(i) {
      layer <- layers[[i]]
      layerd <- data[[i]]
      grobs <- matrix(list(), nrow = nrow(layerd), ncol = ncol(layerd))

      for(i in seq_len(nrow(layerd))) {
        for(j in seq_len(ncol(layerd))) {
          scales <- list(
            x = .$scales$x[[j]]$clone(), 
            y = .$scales$y[[i]]$clone()
          )
          details <- coord$compute_ranges(scales)
          grobs[[i, j]] <- layer$make_grob(layerd[[i, j]], details, coord)
        }
      }
      grobs
    })
  }
  
  calc_statistics <- function(., data, layers) {
    lapply(seq_along(data), function(i) {
      layer <- layers[[i]]
      layerd <- data[[i]]
      grobs <- matrix(list(), nrow = nrow(layerd), ncol = ncol(layerd))

      for(i in seq_len(nrow(layerd))) {
        for(j in seq_len(ncol(layerd))) {
          scales <- list(
            x = .$scales$x[[j]], 
            y = .$scales$y[[i]]
          )
          grobs[[i, j]] <- layer$calc_statistic(layerd[[i, j]], scales)
        }
      }
      grobs
    })
  }

  # Documentation ------------------------------------------------------------

  objname <- "grid"
  desc <- "Lay out panels in a rectangular/tabular manner."
  
  desc_params <- list(
    facets = "a formula with the rows (of the tabular display) on the LHS and the columns (of the tabular display) on the RHS; the dot in the formula is used to indicate there should be no faceting on this dimension (either row or column); the formula can also be entered as a string instead of a classical formula object",
    margins = "logical value, should marginal rows and columns be displayed"
  )
    
  seealso <- list(
    # "cast" = "the formula and margin arguments are the same as those used in the reshape package"
  )  
  
  icon <- function(.) {
    gTree(children = gList(
      rectGrob(0, 1, width=0.95, height=0.05, hjust=0, vjust=1, gp=gpar(fill="grey60", col=NA)),
      rectGrob(0.95, 0.95, width=0.05, height=0.95, hjust=0, vjust=1, gp=gpar(fill="grey60", col=NA)),
      segmentsGrob(c(0, 0.475), c(0.475, 0), c(1, 0.475), c(0.475, 1))
    ))
  }  
  
  examples <- function(.) {
    # faceting displays subsets of the data in different panels
    p <- ggplot(diamonds, aes(carat, ..density..)) +
     geom_histogram(binwidth = 1)
    
    # With one variable
    p + facet_grid(. ~ cut)
    p + facet_grid(cut ~ .)

    # With two variables
    p + facet_grid(clarity ~ cut)
    p + facet_grid(cut ~ clarity)
    # p + facet_grid(cut ~ clarity, margins=TRUE)
    
    qplot(mpg, wt, data=mtcars, facets = . ~ vs + am)
    qplot(mpg, wt, data=mtcars, facets = vs + am ~ . )
    
    # You can also use strings, which makes it a little easier
    # when writing functions that generate faceting specifications
    # p + facet_grid("cut ~ .")
    
    # see also ?plotmatrix for the scatterplot matrix
    
    # If there isn't any data for a given combination, that panel 
    # will be empty
    qplot(mpg, wt, data=mtcars) + facet_grid(cyl ~ vs)
    
    # If you combine a facetted dataset with a dataset that lacks those
    # facetting variables, the data will be repeated across the missing
    # combinations:
    p <- qplot(mpg, wt, data=mtcars, facets = vs ~ cyl)

    df <- data.frame(mpg = 22, wt = 3)
    p + geom_point(data = df, colour="red", size = 2)
    
    df2 <- data.frame(mpg = c(19, 22), wt = c(2,4), vs = c(0, 1))
    p + geom_point(data = df2, colour="red", size = 2)

    df3 <- data.frame(mpg = c(19, 22), wt = c(2,4), vs = c(1, 1))
    p + geom_point(data = df3, colour="red", size = 2)

    
    # You can also choose whether the scales should be constant
    # across all panels (the default), or whether they should be allowed
    # to vary
    mt <- ggplot(mtcars, aes(mpg, wt, colour = factor(cyl))) + geom_point()
    
    mt + facet_grid(. ~ cyl, scales = "free")
    # If scales and space are free, then the mapping between position
    # and values in the data will be the same across all panels
    mt + facet_grid(. ~ cyl, scales = "free", space = "free")
    
    mt + facet_grid(vs ~ am, scales = "free")
    mt + facet_grid(vs ~ am, scales = "free_x")
    mt + facet_grid(vs ~ am, scales = "free_y")
    mt + facet_grid(vs ~ am, scales = "free", space="free")

    # You may need to set your own breaks for consitent display:
    mt + facet_grid(. ~ cyl, scales = "free_x", space="free") + 
      scale_x_continuous(breaks = seq(10, 36, by = 2))
    # Adding scale limits override free scales:
    last_plot() + xlim(10, 15)

    # Free scales are particularly useful for categorical variables
    qplot(cty, model, data=mpg) + 
      facet_grid(manufacturer ~ ., scales = "free", space = "free")
    # particularly when you reorder factor levels
    mpg <- within(mpg, {
      model <- reorder(model, cty)
      manufacturer <- reorder(manufacturer, cty)
    })
    last_plot() %+% mpg + opts(strip.text.y = theme_text())
  }
  
  pprint <- function(., newline=TRUE) {
    cat("facet_", .$objname, "(", .$facets, ", ", .$margins, ")", sep="")
    if (newline) cat("\n")
  }
  
})

# List of scales
# Make a list of scales, cloning if necessary
# 
# @arguments input scale
# @arguments number of scales to produce in output
# @arguments should the scales be free (TRUE) or fixed (FALSE)
# @keyword internal
scales_list <- function(scale, n, free) {
  if (free) {
    rlply(n, scale$clone())  
  } else {
    rep(list(scale), n)  
  }
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/facet-labels.r"
# Label facets with their value
# The default facet labelling just uses the value of the variable
# 
# @arguments variable name passed in by facetter
# @arguments variable value passed in by facetter
# @keyword hplot
#X p <- qplot(wt, mpg, data = mtcars)
#X p + facet_grid(~ cyl)
#X p + facet_grid(~ cyl, labeller = label_value)
label_value <- function(variable, value) value

# Label facets with value and variable
# Join together facet value and the name of the variable to create a label.
# 
# @arguments variable name passed in by facetter
# @arguments variable value passed in by facetter
# @keyword hplot
#X p <- qplot(wt, mpg, data = mtcars)
#X p + facet_grid(~ cyl)
#X p + facet_grid(~ cyl, labeller = label_both)
label_both <- function(variable, value) paste(variable, value, sep = ": ")

# Label facets with parsed label.
# Parses the facet label, as if 
# 
# 
# @seealso \code{\link{plotmath}}
# @arguments variable name passed in by facetter
# @arguments variable value passed in by facetter
# @keyword hplot
#X mtcars$cyl2 <- factor(mtcars$cyl, labels = c("alpha", "beta", "gamma"))
#X qplot(wt, mpg, data = mtcars) + facet_grid(. ~ cyl2)
#X qplot(wt, mpg, data = mtcars) + facet_grid(. ~ cyl2, 
#X   labeller = label_parsed)
label_parsed <- function(variable, value) {
  llply(as.character(value), function(x) parse(text = x))
}

# Label facet with 'bquoted' expressions
# Create facet labels which contain the facet label in a larger expression
# 
# See \code{\link{bquote}} for details on the syntax of the argument.  The
# label value is x. 
# 
# @arguments expression to use
# @seealso \code{\link{plotmath}}
# @keyword hplot
#X p <- qplot(wt, mpg, data = mtcars)
#X p + facet_grid(~ vs, labeller = label_bquote(alpha ^ .(x)))
#X p + facet_grid(~ vs, labeller = label_bquote(.(x) ^ .(x)))
label_bquote <- function(expr = beta ^ .(x)) {
  quoted <- substitute(expr)
  
  function(variable, value) {
    value <- as.character(value)
    lapply(value, function(x)
      eval(substitute(bquote(expr, list(x = x)), list(expr = quoted))))
  }
}

# Grob strip
# Grob for strip labels
# 
# @arguments text to display
# @arguments orientation, horizontal or vertical
# @keyword hplot 
# @keyword internal
ggstrip <- function(text, horizontal=TRUE, theme) {
  text_theme <- if (horizontal) "strip.text.x" else "strip.text.y"
  if (is.list(text)) text <- text[[1]]

  label <- theme_render(theme, text_theme, text)

  ggname("strip", absoluteGrob(
    gList(
      theme_render(theme, "strip.background"),
      label
    ),
    width = grobWidth(label) + unit(0.5, "lines"),
    height = grobHeight(label) + unit(0.5, "lines")
  ))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/facet-viewports.r"
# Assign viewports
# Assign viewports to a matrix of grobs
# 
# Uses the structure (and names) of the matrix of grobs, to automatically
# assign each grob to the appropriate viewport
# 
# @arguments named matrix of grobs
# @keyword internal
assign_viewports <- function(grobs) {
  make_grid <- function(type) {
    data.frame(
      type = type, 
      x = c(row(grobs[[type]])), 
      y = c(col(grobs[[type]]))
    )
  }
  
  assign_vp <- function(type, x, y) {
    ggname(type, editGrob(grobs[[type]][[x, y]], vp = vp_path(x, y, type)))
  }
  
  grid <- ldply(names(grobs), make_grid)
  mlply(grid, assign_vp)
}


# Setup viewports
# Setup matrix of viewports for a layout with given parameters 
# 
# @arguments viewport type
# @arguments number of rows
# @arguments number of columns
# @arguments optional data to compute rows and columns from
# @arguments offset from top and left
# @arguments list containing x and y ranges
# @keyword hplot 
# @keyword internal
setup_viewports <- function(type, data, offset = c(0,0), clip = "on") {
  rows <- nrow(data)
  cols <- ncol(data)
  
  vp <- function(x,y) {
    # cat(vp_name(x, y, type), ": ", x + offset[1], ", ", y + offset[2], "\n", sep="")
    viewport(
      name = vp_name(x, y, type), 
      layout.pos.row = x + offset[1], 
      layout.pos.col = y + offset[2], 
      clip=clip
    )
  }
  pos <- expand.grid(x = seq_len(rows), y= seq_len(cols))
  do.call("vpList", mlply(pos, vp))
}

# Viewport path
# Calculate viewport path.
# 
# Convience method for calculating the viewport path to a particular
# entry in a matrix viewport.  This helps ensure a common naming scheme throughout
# ggplot/
# 
# @arguments row index
# @arguments column index
# @arguments viewport type
# @keyword hplot 
# @keyword internal
vp_path <- function(row, col, type) {
  vpPath("panels", vp_name(row, col, type))
}

# Viewport name
# Compute viewport name
# 
# This helps ensure a common naming scheme throughout ggplot.
# 
# @arguments row index
# @arguments column index
# @arguments viewport type
# @keyword hplot 
# @keyword internal
vp_name <- function(row, col, type) {
  paste(type, row, col, sep="_")
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/facet-wrap.r"
FacetWrap <- proto(Facet, {
  new <- function(., facets, nrow = NULL, ncol = NULL, scales = "fixed", as.table = TRUE, drop = TRUE) {
    scales <- match.arg(scales, c("fixed", "free_x", "free_y", "free"))
    free <- list(
      x = any(scales %in% c("free_x", "free")),
      y = any(scales %in% c("free_y", "free"))
    )
    
    .$proto(
      facets = as.quoted(facets), free = free, 
      scales = NULL, as.table = as.table, drop = drop,
      ncol = ncol, nrow = nrow
    )
  }
  
  conditionals <- function(.) {
    names(.$facets)
  }
  
  # Data shape
  initialise <- function(., data) {
    # Compute facetting variables for all layers
    vars <- ldply(data, function(df) {
      as.data.frame(eval.quoted(.$facets, df))
    })
    
    .$facet_levels <- split_labels(vars, .$drop)
    .$facet_levels$PANEL <- factor(1:nrow(.$facet_levels))
  }
  
  stamp_data <- function(., data) {
    lapply(data, function(df) {
      df <- data.frame(df, eval.quoted(.$facets, df))

      df$.ORDER <- 1:nrow(df)
      df <- merge(add_group(df), .$facet_levels, by = .$conditionals())
      df <- df[order(df$PANEL, df$.ORDER), ]

      out <- as.list(dlply(df, .(PANEL), .drop = FALSE))
      dim(out) <- c(1, nrow(.$facet_levels))
      out
    })
  }
  
  # Create grobs for each component of the panel guides
  add_guides <- function(., data, panels_grob, coord, theme) {

    aspect_ratio <- theme$aspect.ratio
    
    # If user hasn't set aspect ratio, and we have fixed scales, then
    # ask the coordinate system if it wants to specify one
    if (is.null(aspect_ratio) && !.$free$x && !.$free$y) {
      xscale <- .$scales$x[[1]]
      yscale <- .$scales$y[[1]]
      ranges <- coord$compute_ranges(list(x = xscale, y = yscale))
      aspect_ratio <- coord$compute_aspect(ranges)
    }
    
    if (is.null(aspect_ratio)) {
      aspect_ratio <- 1
      respect <- FALSE
    } else {
      respect <- TRUE
    }
        
    n <- length(.$scales$x)

    axes_h <- matrix(list(), nrow = 1, ncol = n)
    axes_v <- matrix(list(), nrow = 1, ncol = n)
    panels <- matrix(list(), nrow = 1, ncol = n)

    for (i in seq_len(n)) {
      scales <- list(
        x = .$scales$x[[i]]$clone(), 
        y = .$scales$y[[i]]$clone()
      ) 
      details <- coord$compute_ranges(scales)
      axes_h[[1, i]] <- coord$guide_axis_h(details, theme)
      axes_v[[1, i]] <- coord$guide_axis_v(details, theme)

      fg <- coord$guide_foreground(details, theme)
      bg <- coord$guide_background(details, theme)
      name <- paste("panel", i, sep = "_")
      panels[[1,i]] <- ggname(name, grobTree(bg, panels_grob[[1, i]], fg))
    }
    
    # Arrange 1d structure into a grid -------
    if (is.null(.$ncol) && is.null(.$nrow)) {
      ncol <- ceiling(sqrt(n))
      nrow <- ceiling(n / ncol)
    } else if (is.null(.$ncol)) {
      nrow <- .$nrow
      ncol <- ceiling(n / nrow)
    } else if (is.null(.$nrow)) {
      ncol <- .$ncol
      nrow <- ceiling(n / ncol)
    } else {
      ncol <- .$ncol
      nrow <- .$nrow
    }
    stopifnot(nrow * ncol >= n)

    # Create a grid of interwoven strips and panels
    panelsGrid <- grobGrid(
      "panel", panels, nrow = nrow, ncol = ncol,
      heights = 1 * aspect_ratio, widths = 1,
      as.table = .$as.table, respect = respect
    )

    strips <- .$labels_default(.$facet_levels, theme)
    strips_height <- max(do.call("unit.c", llply(strips, grobHeight)))
    stripsGrid <- grobGrid(
      "strip", strips, nrow = nrow, ncol = ncol,
      heights = convertHeight(strips_height, "cm"),
      widths = 1,
      as.table = .$as.table
    )
    
    axis_widths <- max(do.call("unit.c", llply(axes_v, grobWidth)))
    axis_widths <- convertWidth(axis_widths, "cm")
    if (.$free$y) {
      axesvGrid <- grobGrid(
        "axis_v", axes_v, nrow = nrow, ncol = ncol, 
        widths = axis_widths, 
        as.table = .$as.table, clip = "off"
      )
    } else { 
      # When scales are not free, there is only really one scale, and this
      # should be shown only in the first column
      axesvGrid <- grobGrid(
        "axis_v", rep(axes_v[1], nrow), nrow = nrow, ncol = 1,
        widths = axis_widths[1], 
        as.table = .$as.table, clip = "off")
      if (ncol > 1) {
        axesvGrid <- cbind(axesvGrid, 
          spacer(nrow, ncol - 1, unit(0, "cm"), unit(1, "null")))
        
      }
    }
    
    axis_heights <- max(do.call("unit.c", llply(axes_h, grobHeight)))
    axis_heights <- convertHeight(axis_heights, "cm")
    if (.$free$x) {
      axeshGrid <- grobGrid(
        "axis_h", axes_h, nrow = nrow, ncol = ncol, 
        heights = axis_heights, 
        as.table = .$as.table, clip = "off"
      )
    } else {
      # When scales are not free, there is only really one scale, and this
      # should be shown only in the bottom row
      axeshGrid <- grobGrid(
        "axis_h", rep(axes_h[1], ncol), nrow = 1, ncol = ncol,
        heights = axis_heights[1], 
        as.table = .$as.table, clip = "off")
      if (nrow > 1) { 
        axeshGrid <- rbind(
          spacer(nrow - 1, ncol, unit(1, "null"), unit(0, "cm")),
          axeshGrid
        )
      }
    }

    gap <- spacer(nrow, ncol, theme$panel.margin, theme$panel.margin)
    fill <- spacer(nrow, ncol, 0, 0, "null")
    
    all <- rweave(
      cweave(fill,      stripsGrid, fill),
      cweave(axesvGrid, panelsGrid, fill),
      cweave(fill,      axeshGrid,  fill),
      cweave(fill,      fill,       gap)
    )
    
    all
  }
  
  labels_default <- function(., labels_df, theme) {
    # Remove column giving panel number
    labels_df <- labels_df[, -ncol(labels_df), drop = FALSE]
    labels_df[] <- llply(labels_df, format, justify = "none")
    
    labels <- apply(labels_df, 1, paste, collapse=", ")

    llply(labels, ggstrip, theme = theme)
  }
  
  # Position scales ----------------------------------------------------------
  
  position_train <- function(., data, scales) {
    fr <- .$free
    n <- nrow(.$facet_levels)
    if (is.null(.$scales$x) && scales$has_scale("x")) {
      .$scales$x <- scales_list(scales$get_scales("x"), n, fr$x)
    }
    if (is.null(.$scales$y) && scales$has_scale("y")) {
      .$scales$y <- scales_list(scales$get_scales("y"), n, fr$y)
    }

    lapply(data, function(l) {
      for(i in seq_along(.$scales$x)) {
        .$scales$x[[i]]$train_df(l[[i]], fr$x)
      }
      for(i in seq_along(.$scales$y)) {
        .$scales$y[[i]]$train_df(l[[i]], fr$y)
      }
    })
  }
  
  position_map <- function(., data, scales) {
    lapply(data, function(l) {
      for(i in seq_along(.$scales$x)) {
        l[1, i] <- lapply(l[1, i], function(old) {
          new <- .$scales$x[[i]]$map_df(old)
          if (!is.null(.$scales$y[[i]])) {
            new <- cbind(new, .$scales$y[[i]]$map_df(old))
          }
          
          
          cunion(new, old)
        }) 
      }
      l
    })
  }
  
  make_grobs <- function(., data, layers, coord) {
    lapply(seq_along(data), function(i) {
      layer <- layers[[i]]
      layerd <- data[[i]]
      grobs <- matrix(list(), nrow = nrow(layerd), ncol = ncol(layerd))

      for(i in seq_along(.$scales$x)) {
        scales <- list(
          x = .$scales$x[[i]]$clone(), 
          y = .$scales$y[[i]]$clone()
        )
        details <- coord$compute_ranges(scales)
        grobs[[1, i]] <- layer$make_grob(layerd[[1, i]], details, coord)
      }
      grobs
    })
  }
  
  calc_statistics <- function(., data, layers) {
    lapply(seq_along(data), function(i) {
      layer <- layers[[i]]
      layerd <- data[[i]]
      data_out <- matrix(list(), nrow = nrow(layerd), ncol = ncol(layerd))

      for(j in seq_len(nrow(.$facet_levels))) {
        scales <- list(
          x = .$scales$x[[j]], 
          y = .$scales$y[[j]]
        )
        data_out[[1, j]] <- layer$calc_statistic(layerd[[1, j]], scales)
      }
      data_out
    })
  }
  

  # Documentation ------------------------------------------------------------

  objname <- "wrap"
  desc <- "Wrap a 1d ribbon of panels into 2d."
  
  desc_params <- list(
    nrow = "number of rows",
    ncol = "number of columns", 
    facet = "formula specifying variables to facet by",
    scales = "should scales be fixed, free, or free in one dimension (\\code{free_x}, \\code{free_y}) "
  )

  
  
  examples <- function(.) {
    d <- ggplot(diamonds, aes(carat, price, fill = ..density..)) + 
      xlim(0, 2) + stat_binhex(na.rm = TRUE) + opts(aspect.ratio = 1)
    d + facet_wrap(~ color)
    d + facet_wrap(~ color, ncol = 1)
    d + facet_wrap(~ color, ncol = 4)
    d + facet_wrap(~ color, nrow = 1)
    d + facet_wrap(~ color, nrow = 3)
    
    # Using multiple variables continues to wrap the long ribbon of 
    # plots into 2d - the ribbon just gets longer
    # d + facet_wrap(~ color + cut)
    
    # You can choose to keep the scales constant across all panels
    # or vary the x scale, the y scale or both:
    p <- qplot(price, data = diamonds, geom = "histogram", binwidth = 1000)
    p + facet_wrap(~ color)
    p + facet_wrap(~ color, scales = "free_y")
    
    p <- qplot(displ, hwy, data = mpg)
    p + facet_wrap(~ cyl)
    p + facet_wrap(~ cyl, scales = "free") 
    
    # Add data that does not contain all levels of the faceting variables
    cyl6 <- subset(mpg, cyl == 6)
    p + geom_point(data = cyl6, colour = "red", size = 1) + 
      facet_wrap(~ cyl)
    p + geom_point(data = transform(cyl6, cyl = 7), colour = "red") + 
      facet_wrap(~ cyl)
    p + geom_point(data = transform(cyl6, cyl = NULL), colour = "red") + 
      facet_wrap(~ cyl)
    
    # By default, any empty factor levels will be dropped
    mpg$cyl2 <- factor(mpg$cyl, levels = c(2, 4, 5, 6, 8, 10))
    qplot(displ, hwy, data = mpg) + facet_wrap(~ cyl2)
    # Use drop = FALSE to force their inclusion
    qplot(displ, hwy, data = mpg) + facet_wrap(~ cyl2, drop = FALSE)
  }
  
  pprint <- function(., newline=TRUE) {
    cat("facet_", .$objname, "(", paste(names(.$facets), collapse = ", "), ")", sep="")
    if (newline) cat("\n")
  }
  
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/formats.r"
# Comma formatter
# Format number with commas separating thousands
# 
# @arguments numeric vector to format
# @arguments other arguments passed on to \code{\link{format}}
comma <- function(x, ...) {
  format(x, big.mark = ",", trim = TRUE, scientific = FALSE, ...)
}

# Currency formatter
# Round to nearest cent and display dollar sign
# 
# @arguments numeric vector to format
# @arguments other arguments passed on to \code{\link{format}}
dollar <- function(x, ...) {
  x <- round_any(x, 0.01)
  nsmall <- if (max(x) < 100) 2 else 0
  paste("$", comma(x, nsmall = nsmall), sep="")
}

# Percent formatter
# Multiply by one hundred and display percent sign
# 
# @arguments numeric vector to format
percent <- function(x) {
  x <- round_any(x, precision(x) / 10)
  paste(comma(x * 100), "%", sep="")
}

# Scientific formatter
# Default scientific formatting
# 
# @arguments numeric vector to format
scientific <- function(x) {
  format(x, trim = TRUE)
}

# Compute precision
# Compute precision (in power of 10) of a vector of numbers
# 
# @keyword internal
precision <- function(x) {
  10 ^ floor(log10(diff(range(x))))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/fortify-lm.r"
# Fortify a linear model with its data
# Supplement the data fitted to a linear model with model fit statistics.
# 
# The following statistics will be added to the data frame:
# 
# \itemize{
#   \item{.hat}{Diagonal of the hat matrix}
#   \item{.sigma}{Estimate of residual standard deviation when corresponding
#      observation is dropped from model}
#   \item{.cooksd}{Cooks distance, \code{\link{cooks.distance}}}
#   \item{.fitted}{Fitted values of model}
#   \item{.resid}{Residuals}
#  \item{.stdresid}{Standardised residuals}
# }
# 
# If you have missing values in your model data, you may need to refit 
# the model with \code{na.action = na.preserve}.
#
# @arguments linear model
# @arguments data set, defaults to data used to fit model
# @arguments not used
#X mod <- lm(mpg ~ wt, data = mtcars)
#X head(fortify(mod))
#X head(fortify(mod, mtcars))
#X 
#X plot(mod, which = 1)
#X qplot(.fitted, .resid, data = mod) + geom_hline() + geom_smooth(se = FALSE)
#X qplot(.fitted, .stdresid, data = mod) + geom_hline() + 
#X   geom_smooth(se = FALSE)
#X qplot(.fitted, .stdresid, data = fortify(mod, mtcars), 
#X   colour = factor(cyl))
#X qplot(mpg, .stdresid, data = fortify(mod, mtcars), colour = factor(cyl))
#X
#X plot(mod, which = 2)
#X # qplot(sample =.stdresid, data = mod, stat = "qq") + geom_abline()
#X
#X plot(mod, which = 3)
#X qplot(.fitted, sqrt(abs(.stdresid)), data = mod) + geom_smooth(se = FALSE)
#X
#X plot(mod, which = 4)
#X qplot(seq_along(.cooksd), .cooksd, data = mod, geom = "bar",
#X  stat="identity")
#X
#X plot(mod, which = 5)
#X qplot(.hat, .stdresid, data = mod) + geom_smooth(se = FALSE)
#X ggplot(mod, aes(.hat, .stdresid)) + 
#X   geom_vline(size = 2, colour = "white", xintercept = 0) +
#X   geom_hline(size = 2, colour = "white", yintercept = 0) +
#X   geom_point() + geom_smooth(se = FALSE)
#X 
#X qplot(.hat, .stdresid, data = mod, size = .cooksd) + 
#X   geom_smooth(se = FALSE, size = 0.5)
#X
#X plot(mod, which = 6)
#X ggplot(mod, aes(.hat, .cooksd, data = mod)) + 
#X   geom_vline(colour = NA) + 
#X   geom_abline(slope = seq(0, 3, by = 0.5), colour = "white") +
#X   geom_smooth(se = FALSE) + 
#X   geom_point()
#X qplot(.hat, .cooksd, size = .cooksd / .hat, data = mod) + scale_area()
fortify.lm <- function(model, data = model$model, ...) {
  infl <- influence(model, do.coef = FALSE)
  data$.hat <- infl$hat
  data$.sigma <- infl$sigma 
  data$.cooksd <- cooks.distance(model, infl)

  data$.fitted <- predict(model)
  data$.resid <- resid(model)
  data$.stdresid <- rstandard(model, infl)

  data
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/fortify-map.r"
# Fortify a map
# Fortify method for map objects
# 
# This function turns a map into a data frame than can more easily be
# plotted with ggplot2.
# 
# @arguments map object
# @arguments ignored
# @arguments ignored
# @keyword hplot
#X if (require(maps)) {
#X ca <- map_data("county", "ca")
#X qplot(long, lat, data = ca, geom="polygon", group = group)
#X tx <- map_data("county", "texas")
#X qplot(long, lat, data = tx, geom="polygon", group = group, 
#X  colour = I("white"))
#X }
fortify.map <- function(model, data, ...) {
  df <- as.data.frame(model[c("x", "y")])
  names(df) <- c("long", "lat")
  df$group <- cumsum(is.na(df$long) & is.na(df$lat)) + 1
  df$order <- 1:nrow(df)
  
  names <- do.call("rbind", lapply(strsplit(model$names, "[:,]"), "[", 1:2))
  df$region <- names[df$group, 1]
  df$subregion <- names[df$group, 2]
  df[complete.cases(df$lat, df$long), ]
}

# Map borders.
# Create a layer of map borders
# 
# @arguments map data, see \code{\link[maps]{map}} for details
# @arguments map region
# @arguments fill colour
# @arguments border colour
# @arguments other arguments passed on to \code{\link{geom_polygon}}
# @keyword hplot
#X if (require(maps)) {
#X ia <- map_data("county", "iowa")
#X mid_range <- function(x) mean(range(x))
#X seats <- ddply(ia, .(subregion), colwise(mid_range, .(lat, long)))
#X ggplot(ia, aes(long, lat)) + 
#X   geom_polygon(aes(group = group), fill = NA, colour = "grey60") +
#X   geom_text(aes(label = subregion), data = seats, size = 2, angle = 45)
#X
#X data(us.cities)
#X capitals <- subset(us.cities, capital == 2)
#X ggplot(capitals, aes(long, lat)) +
#X   borders("state") + 
#X   geom_point(aes(size = pop)) + 
#X   scale_area()
#X }
borders <- function(database = "world", regions = ".", fill = NA, colour = "grey50", ...) {
  df <- map_data(database, regions)
  geom_polygon(aes(long, lat, group = group), data = df, 
    fill = fill, colour = colour, ...)
}

# Map data
# Convert map to data frame
# 
# @arguments map name
# @arguments region name
# @keyword hplot
#X if (require(maps)) {
#X states <- map_data("state")
#X arrests <- USArrests
#X names(arrests) <- tolower(names(arrests))
#X arrests$region <- tolower(rownames(USArrests))
#X 
#X choro <- merge(states, arrests, sort = FALSE, by = "region")
#X choro <- choro[order(choro$order), ]
#X qplot(long, lat, data = choro, group = group, fill = assault,
#X   geom="polygon")
#X qplot(long, lat, data = choro, group = group, fill = assault / murder,
#X   geom="polygon")
#X }
map_data <- function(map, region = ".") {
  fortify(map(map, region, plot = FALSE, fill = TRUE))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/fortify-spatial.r"
# Fortify spatial polygons and lines
# Fortify method for a number of the class from the sp package.
# 
# To figure out the correct variable name for region, inspect 
# \code{as.data.frame(model)}.
# 
# @alias fortify.SpatialPolygons
# @alias fortify.Polygons
# @alias fortify.Polygon
# @alias fortify.SpatialLinesDataFrame
# @alias fortify.Lines
# @alias fortify.Line
# @arguments SpatialPolygonsDataFrame
# @arguments not used
# @arguments name of variable to split up regions by
# @arguments not used
fortify.SpatialPolygonsDataFrame <- function(model, data, region = NULL, ...) {
  attr <- as.data.frame(model)
  # If not specified, split into regions based on first variable in attributes
  if (is.null(region)) {
    region <- names(attr)[1]
    message("Using ", region, " to define regions.")
  }
  
  # Figure out how polygons should be split up into the region of interest
  
  polys <- split(as.numeric(row.names(attr)), addNA(attr[, region], TRUE))
  cp <- polygons(model)
  
  # Union together all polygons that make up a region
  try_require(c("gpclib", "maptools"))
  unioned <- unionSpatialPolygons(cp, invert(polys))
  
  coords <- fortify(unioned)
  coords$order <- 1:nrow(coords)
  coords
}

fortify.SpatialPolygons <- function(model, data, ...) {
  ldply(model@polygons, fortify)
}

fortify.Polygons <- function(model, data, ...) {
  subpolys <- model@Polygons
  pieces <- ldply(seq_along(subpolys), function(i) {
    df <- fortify(subpolys[[model@plotOrder[i]]])
    df$piece <- i
    df
  })
  
  within(pieces,{
    order <- 1:nrow(pieces)
    id <- model@ID
    piece <- factor(piece)
    group <- interaction(id, piece)
  })
}

fortify.Polygon <- function(model, data, ...) {
  df <- as.data.frame(model@coords)
  names(df) <- c("long", "lat")
  df$order <- 1:nrow(df)
  df$hole <- model@hole
  df
}

fortify.SpatialLinesDataFrame <- function(model, data, ...) {
  ldply(model@lines, fortify)
}

fortify.Lines <- function(model, data, ...) {
  lines <- model@Lines
  pieces <- ldply(seq_along(lines), function(i) {
    df <- fortify(lines[[i]])
    df$piece <- i
    df
  })
  
  within(pieces,{
    order <- 1:nrow(pieces)
    id <- model@ID
    piece <- factor(piece)
    group <- interaction(id, piece)
  })
}

fortify.Line <- function(model, data, ...) {
  df <- as.data.frame(model@coords)
  names(df) <- c("long", "lat")
  df$order <- 1:nrow(df)
  df  
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/fortify.r"
# Fortify a model with data
# Generic method to supplement the original data with model fit statistics
# 
# @seealso \code{\link{fortify.lm}}
# @alias fortify.data.frame
# @alias fortify.NULL
# @alias fortify.default
# @arguments model
# @arguments dataset
# @arguments other arguments passed to methods
fortify <- function(model, data, ...) UseMethod("fortify")

fortify.data.frame <- function(model, data, ...) model
fortify.NULL <- function(model, data, ...) data.frame()
fortify.default <- function(model, data, ...) {
  
  stop("ggplot2 doesn't know how to deal with data of class ", class(model), call. = FALSE)
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-.r"
Geom <- proto(TopLevel, expr={
  class <- function(.) "geom"

  parameters <- function(.) {
    params <- formals(get("draw", .))
    params <- params[setdiff(names(params), c(".","data","scales", "coordinates", "..."))]
    
    required <- rep(NA, length(.$required_aes))
    names(required) <- .$required_aes
    aesthetics <- c(.$default_aes(), required)
    
    c(params, aesthetics[setdiff(names(aesthetics), names(params))])
  }
  
  required_aes <- c()
  default_aes <- function(.) {}
  default_pos <- function(.) PositionIdentity

  guide_geom <- function(.) "point"

  draw <- function(...) {}
  draw_groups <- function(., data, scales, coordinates, ...) {
    if (empty(data)) return(zeroGrob())
    
    groups <- split(data, factor(data$group))
    grobs <- lapply(groups, function(group) .$draw(group, scales, coordinates, ...))
    
    ggname(paste(.$objname, "s", sep=""), gTree(
      children = do.call("gList", grobs)
    ))
  }
  
  new <- function(., mapping=NULL, data=NULL, stat=NULL, position=NULL, ...){
    do.call("layer", list(mapping=mapping, data=data, stat=stat, geom=., position=position, ...))
  }
  
  pprint <- function(., newline=TRUE) {
    cat("geom_", .$objname, ": ", sep="") #  , clist(.$parameters())
    if (newline) cat("\n")
  }
  
  reparameterise <- function(., data, params) data
  
  # Html documentation ----------------------------------

    
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-abline.r"
GeomAbline <- proto(Geom, {
  new <- function(., mapping = NULL, ...) {
    mapping <- compact(defaults(mapping, aes(group = 1)))
    class(mapping) <- "uneval"
    .super$new(., ..., mapping = mapping, inherit.aes = FALSE)
  }
  
  draw <- function(., data, scales, coordinates, ...) {
    xrange <- scales$x.range
    
    data <- transform(data,
      x = xrange[1],
      xend = xrange[2],
      y = xrange[1] * slope + intercept,
      yend = xrange[2] * slope + intercept
    )
    
    GeomSegment$draw(unique(data), scales, coordinates)
  }

  # Documentation -----------------------------------------------

  objname <- "abline"
  icon <- function(.) linesGrob(c(0, 1), c(0.2, 0.8))
  desc <- "Line, specified by slope and intercept"
  details <- "<p>The abline geom adds a line with specified slope and intercept to the plot.</p>\n<p>With its siblings geom_hline and geom_vline, it's useful for annotating plots.  You can supply the parameters for geom_abline, intercept and slope, in two ways: either explicitly as fixed values, or stored in the data set.  If you specify the fixed values (<code>geom_abline(intercept=0, slope=1)</code>) then the line will be the same in all panels, but if the intercept and slope are stored in the data, then can vary from panel to panel.  See the examples for more ideas.</p>\n"
  seealso <- list(
    stat_smooth = "To add lines derived from the data",
    geom_hline = "for horizontal lines",
    geom_vline = "for vertical lines",
    geom_segment = "for a more general approach"
  )
  guide_geom <- function(.) "abline"

  default_stat <- function(.) StatAbline
  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha=1)
  
  draw_legend <- function(., data, ...) {
    data <- aesdefaults(data, .$default_aes(), list(...))

    with(data, 
      ggname(.$my_name(), segmentsGrob(0, 0, 1, 1, default.units="npc",
      gp=gpar(col=alpha(colour, alpha), lwd=size * .pt, lty=linetype,
        lineend="butt")))
    )
  }
  
  
  examples <- function(.) {
    p <- qplot(wt, mpg, data = mtcars)

    # Fixed slopes and intercepts
    p + geom_abline() # Can't see it - outside the range of the data
    p + geom_abline(intercept = 20)

    # Calculate slope and intercept of line of best fit
    coef(lm(mpg ~ wt, data = mtcars))
    p + geom_abline(intercept = 37, slope = -5)
    p + geom_abline(intercept = 10, colour = "red", size = 2)
    
    # See ?stat_smooth for fitting smooth models to data
    p + stat_smooth(method="lm", se=FALSE)
    
    # Slopes and intercepts as data
    p <- ggplot(mtcars, aes(x = wt, y=mpg), . ~ cyl) + geom_point()
    df <- data.frame(a=rnorm(10, 25), b=rnorm(10, 0))
    p + geom_abline(aes(intercept=a, slope=b), data=df)

    # Slopes and intercepts from linear model
    coefs <- ddply(mtcars, .(cyl), function(df) { 
      m <- lm(mpg ~ wt, data=df)
      data.frame(a = coef(m)[1], b = coef(m)[2]) 
    })
    str(coefs)
    p + geom_abline(data=coefs, aes(intercept=a, slope=b))
    
    # It's actually a bit easier to do this with stat_smooth
    p + geom_smooth(aes(group=cyl), method="lm")
    p + geom_smooth(aes(group=cyl), method="lm", fullrange=TRUE)
    
  }  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-bar-.r"
GeomBar <- proto(Geom, {
  
  default_stat <- function(.) StatBin
  default_pos <- function(.) PositionStack
  default_aes <- function(.) aes(colour=NA, fill="grey20", size=0.5, linetype=1, weight = 1, alpha = 1)
  
  required_aes <- c("x")
 
  reparameterise <- function(., df, params) {
    df$width <- df$width %||% 
      params$width %||% (resolution(df$x, FALSE) * 0.9)
    transform(df,
      ymin = pmin(y, 0), ymax = pmax(y, 0),
      xmin = x - width / 2, xmax = x + width / 2, width = NULL
    )
  }
 
  draw_groups <- function(., data, scales, coordinates, ...) {
    GeomRect$draw_groups(data, scales, coordinates, ...)
  }
  
  # Documentation -----------------------------------------------
  objname <- "bar"
  desc <- "Bars, rectangles with bases on x-axis"
  guide_geom <- function(.) "polygon"
  
  icon <- function(.) {
    rectGrob(c(0.3, 0.7), c(0.4, 0.8), height=c(0.4, 0.8), width=0.3, vjust=1, gp=gpar(fill="grey20", col=NA))
  }
  details <- "<p>The bar geom is used to produce 1d area plots: bar charts for categorical x, and histograms for continuous y.  stat_bin explains the details of these summaries in more detail.  In particular, you can use the <code>weight</code> aesthetic to create weighted histograms and barcharts where the height of the bar no longer represent a count of observations, but a sum over some other variable.  See the examples for a practical example.</p>\n<p>By default, multiple x's occuring in the same place will be stacked a top one another by position_stack.  If you want them to be dodged from side-to-side, check out position_dodge.  Finally, position_fill shows relative propotions at each x by stacking the bars and then stretch or squashing them all to the same height</p>\n"
  
  advice <- "<p>If you have presummarised data, use <code>stat=\"identity\" to turn off the default summary</p>\n<p>Sometimes, bar charts are used not as a distributional summary, but instead of a dotplot.  Generally, it's preferable to use a dotplot (see geom_point) as it has a better data-ink ratio.  However, if you do want to create this type of plot, you can set y to the value you have calculated, and use stat='identity'.</p>\n<p>A bar chart maps the height of the bar to a variable, and so the base of the bar must always been shown to produce a valid visual comparison.  Naomi Robbins has a nice <a href='http://www.b-eye-network.com/view/index.php?cid=2468&amp;fc=0&amp;frss=1&amp;ua'>article on this topic</a>.  This is the reason it doesn't make sense to use a log-scaled y axis.</p>\n"
  
  seealso <- list(
    "stat_bin" = "for more details of the binning alogirithm", 
    "position_dodge" = "for creating side-by-side barcharts",
    "position_stack" = "for more info on stacking"
  )
  
  examples <- function(.) {
    # Generate data
    c <- ggplot(mtcars, aes(factor(cyl)))
    
    c + geom_bar()
    c + geom_bar() + coord_flip()
    c + geom_bar(fill="white", colour="darkgreen")
    
    # Use qplot
    qplot(factor(cyl), data=mtcars, geom="bar")
    qplot(factor(cyl), data=mtcars, geom="bar", fill=factor(cyl))

    # Stacked bar charts    
    qplot(factor(cyl), data=mtcars, geom="bar", fill=factor(vs))
    qplot(factor(cyl), data=mtcars, geom="bar", fill=factor(gear))

    # Stacked bar charts are easy in ggplot2, but not effective visually, 
    # particularly when there are many different things being stacked
    ggplot(diamonds, aes(clarity, fill=cut)) + geom_bar()
    ggplot(diamonds, aes(color, fill=cut)) + geom_bar() + coord_flip()
    
    # Faceting is a good alternative:
    ggplot(diamonds, aes(clarity)) + geom_bar() + 
      facet_wrap(~ cut)
    # If the x axis is ordered, using a line instead of bars is another
    # possibility:
    ggplot(diamonds, aes(clarity)) + 
      geom_freqpoly(aes(group = cut, colour = cut))

    # Dodged bar charts    
    ggplot(diamonds, aes(clarity, fill=cut)) + geom_bar(position="dodge")
    # compare with 
    ggplot(diamonds, aes(cut, fill=cut)) + geom_bar() + 
      facet_grid(. ~ clarity)
    
    # But again, probably better to use frequency polygons instead:
    ggplot(diamonds, aes(clarity, colour=cut)) + 
      geom_freqpoly(aes(group = cut))
    
    # Often we don't want the height of the bar to represent the
    # count of observations, but the sum of some other variable.
    # For example, the following plot shows the number of diamonds
    # of each colour
    qplot(color, data=diamonds, geom="bar")
    # If, however, we want to see the total number of carats in each colour
    # we need to weight by the carat variable
    qplot(color, data=diamonds, geom="bar", weight=carat, ylab="carat")
    
    # A bar chart used to display means
    meanprice <- tapply(diamonds$price, diamonds$cut, mean)
    cut <- factor(levels(diamonds$cut), levels = levels(diamonds$cut))
    qplot(cut, meanprice)
    qplot(cut, meanprice, geom="bar", stat="identity")
    qplot(cut, meanprice, geom="bar", stat="identity", fill = I("grey50"))
  }  

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-bar-histogram.r"
GeomHistogram <- proto(GeomBar, {
  objname <- "histogram"
  desc <- "Histogram"
  
  details <- "<p>geom_histogram is an alias for geom_bar + stat_bin so you will need to look at the documentation for those objects to get more information about the parameters.</p>"

  advice <- "<p>geom_histogram only allows you to set the width of the bins (with the binwidth parameter), not the number of bins, and it certainly does not suport the use of common heuristics to select the number of bins.  In practice, you will need to use multiple bin widths to discover all the signal in the data, and having bins with meaningful widths (rather than some arbitrary fraction of the range of the data) is more interpretable.</p> "
  
  icon <- function(.) {
    y <- c(0.2, 0.3, 0.5, 0.6,0.2, 0.8, 0.5, 0.3)
    rectGrob(seq(0.1, 0.9, by=0.1), y, height=y, width=0.1, vjust=1, gp=gpar(fill="grey20", col=NA))
  }
  
  examples <- function(.) {
    
    # Simple examles
    qplot(rating, data=movies, geom="histogram")
    qplot(rating, data=movies, weight=votes, geom="histogram")
    qplot(rating, data=movies, weight=votes, geom="histogram", binwidth=1)
    qplot(rating, data=movies, weight=votes, geom="histogram", binwidth=0.1)
    
    # More complex
    m <- ggplot(movies, aes(x=rating))
    m + geom_histogram()
    m + geom_histogram(aes(y = ..density..)) + geom_density()

    m + geom_histogram(binwidth = 1)
    m + geom_histogram(binwidth = 0.5)
    m + geom_histogram(binwidth = 0.1)
    
    # Add aesthetic mappings
    m + geom_histogram(aes(weight = votes))
    m + geom_histogram(aes(y = ..count..))
    m + geom_histogram(aes(fill = ..count..))

    # Change scales
    m + geom_histogram(aes(fill = ..count..)) + 
      scale_fill_gradient("Count", low = "green", high = "red")

    # Often we don't want the height of the bar to represent the
    # count of observations, but the sum of some other variable.
    # For example, the following plot shows the number of movies
    # in each rating.
    qplot(rating, data=movies, geom="bar", binwidth = 0.1)
    # If, however, we want to see the number of votes cast in each
    # category, we need to weight by the votes variable
    qplot(rating, data=movies, geom="bar", binwidth = 0.1,
      weight=votes, ylab = "votes")
    
    m <- ggplot(movies, aes(x = votes))
    # For transformed scales, binwidth applies to the transformed data.
    # The bins have constant width on the transformed scale.
    m + geom_histogram() + scale_x_log10()
    m + geom_histogram(binwidth = 1) + scale_x_log10()
    m + geom_histogram() + scale_x_sqrt()
    m + geom_histogram(binwidth = 10) + scale_x_sqrt()

    # For transformed coordinate systems, the binwidth applies to the 
    # raw data.  The bins have constant width on the original scale.

    # Using log scales does not work here, because the first
    # bar is anchored at zero, and so when transformed becomes negative
    # infinity.  This is not a problem when transforming the scales, because
    # no observations have 0 ratings.
    should_stop(m + geom_histogram() + coord_trans(x = "log10"))
    m + geom_histogram() + coord_trans(x = "sqrt")
    m + geom_histogram(binwidth=1000) + coord_trans(x = "sqrt")
      
    # You can also transform the y axis.  Remember that the base of the bars
    # has value 0, so log transformations are not appropriate 
    m <- ggplot(movies, aes(x = rating))
    m + geom_histogram(binwidth = 0.5) + scale_y_sqrt()
    m + geom_histogram(binwidth = 0.5) + scale_y_reverse()
    
    # Set aesthetics to fixed value
    m + geom_histogram(colour = "darkgreen", fill = "white", binwidth = 0.5)
    
    # Use facets
    m <- m + geom_histogram(binwidth = 0.5)
    m + facet_grid(Action ~ Comedy)
    
    # Often more useful to use density on the y axis when facetting
    m <- m + aes(y = ..density..)
    m + facet_grid(Action ~ Comedy)
    m + facet_wrap(~ mpaa)

    # Multiple histograms on the same graph
    # see ?position, ?position_fill, etc for more details.  
    ggplot(diamonds, aes(x=price)) + geom_bar()
    hist_cut <- ggplot(diamonds, aes(x=price, fill=cut))
    hist_cut + geom_bar() # defaults to stacking
    hist_cut + geom_bar(position="fill")
    hist_cut + geom_bar(position="dodge")
    
    # This is easy in ggplot2, but not visually effective.  It's better
    # to use a frequency polygon or density plot.  Like this:
    ggplot(diamonds, aes(price, ..density.., colour = cut)) +
      geom_freqpoly(binwidth = 1000)
    # Or this:
    ggplot(diamonds, aes(price, colour = cut)) +
      geom_density()
    # Or if you want to be fancy, maybe even this:
    ggplot(diamonds, aes(price, fill = cut)) +
      geom_density(alpha = 0.2)
    # Which looks better when the distributions are more distinct
    ggplot(diamonds, aes(depth, fill = cut)) +
      geom_density(alpha = 0.2) + xlim(55, 70)
    
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-bin2d.r"
GeomBin2d <- proto(Geom, {
  draw <- function(., data, scales, coordinates, ...) {
    GeomRect$draw(data, scales, coordinates, ...)
  }

  objname <- "bin2d"
  desc <- "Add heatmap of 2d bin counts"
  
  guide_geom <- function(.) "polygon"
  
  default_stat <- function(.) StatBin2d
  required_aes <- c("xmin", "xmax", "ymin", "ymax")
  default_aes <- function(.) {
    aes(colour = NA, fill = "grey60", size = 0.5, linetype = 1, weight = 1, , alpha = 1)
  }

  examples <- function(.) {
    # See ?stat_bin2d
  }

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-blank.r"
GeomBlank <- proto(Geom, {
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes()

  # Documentation -----------------------------------------------

  objname <- "blank"
  desc <- "Blank, draws nothing"
  detail <- "<p>The blank geom draws nothing, but can be a useful way of ensuring common scales between different plots</p>\n"
  
  examples <- function(.) {
    qplot(length, rating, data=movies, geom="blank")
    # Nothing to see here!
  }
  
  draw_legend <- function(., data, ...) {
    zeroGrob()
  }
  
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-boxplot.r"
GeomBoxplot <- proto(Geom, {
  
  reparameterise <- function(., df, params) {
    df$width <- df$width %||% 
      params$width %||% (resolution(df$x, FALSE) * 0.9)

    transform(df,
      xmin = x - width / 2, xmax = x + width / 2, width = NULL
    )
  }
  
  draw <- function(., data, ..., outlier.colour = "black", outlier.shape = 16, outlier.size = 2) { 
    defaults <- with(data, data.frame(
      x = x, xmin = xmin, xmax = xmax, 
      colour = colour, size = size, 
      linetype = 1, group = 1, alpha = 1, 
      fill = alpha(fill, alpha),  
      stringsAsFactors = FALSE
    ))
    defaults2 <- defaults[c(1,1), ]
    
    if (!is.null(data$outliers) && length(data$outliers[[1]] >= 1)) {
      outliers_grob <- with(data,
        GeomPoint$draw(data.frame(
          y = outliers[[1]], x = x[rep(1, length(outliers[[1]]))],
          colour=I(outlier.colour), shape = outlier.shape, alpha = 1, 
          size = outlier.size, fill = NA), ...
        )
      )
    } else {
      outliers_grob <- NULL
    }
    
    with(data, ggname(.$my_name(), grobTree(
      outliers_grob,
      GeomPath$draw(data.frame(y=c(upper, ymax), defaults2), ...),
      GeomPath$draw(data.frame(y=c(lower, ymin), defaults2), ...),
      GeomRect$draw(data.frame(ymax = upper, ymin = lower, defaults), ...),
      GeomRect$draw(data.frame(ymax = middle, ymin = middle, defaults), ...)
    )))
  }

  objname <- "boxplot"
  desc <- "Box and whiskers plot"
  guide_geom <- function(.) "boxplot"
  
  draw_legend <- function(., data, ...)  {
    data <- aesdefaults(data, .$default_aes(), list(...))
    gp <- with(data, gpar(col=colour, fill=fill, lwd=size * .pt))

    gTree(gp = gp, children = gList(
      linesGrob(0.5, c(0.1, 0.9)),
      rectGrob(height=0.5, width=0.75),
      linesGrob(c(0.125, 0.875), 0.5)
    ))
  }
  icon <- function(.) {
    gTree(children=gList(
      segmentsGrob(c(0.3, 0.7), c(0.1, 0.2), c(0.3, 0.7), c(0.7, 0.95)),
      rectGrob(c(0.3, 0.7), c(0.6, 0.8), width=0.3, height=c(0.4, 0.4), vjust=1),
      segmentsGrob(c(0.15, 0.55), c(0.5, 0.6), c(0.45, 0.85), c(0.5, 0.6))
    ))
  }
  
  default_stat <- function(.) StatBoxplot
  default_pos <- function(.) PositionDodge
  default_aes <- function(.) aes(weight=1, colour="grey20", fill="white", size=0.5, alpha = 1)
  required_aes <- c("x", "lower", "upper", "middle", "ymin", "ymax")
  seealso <- list(
    stat_quantile = "View quantiles conditioned on a continuous variable",
    geom_jitter = "Another way to look at conditional distributions"
  )
  desc_params <- list(
    outlier.colour = "colour for outlying points",
    outlier.shape = "shape of outlying points",
    outlier.size = "size of outlying points"
  )
  
  examples <- function(.) {
    p <- ggplot(mtcars, aes(factor(cyl), mpg))
    
    p + geom_boxplot()
    qplot(factor(cyl), mpg, data = mtcars, geom = "boxplot")
    
    p + geom_boxplot() + geom_jitter()
    p + geom_boxplot() + coord_flip()
    qplot(factor(cyl), mpg, data = mtcars, geom = "boxplot") +
      coord_flip()
    
    p + geom_boxplot(outlier.colour = "green", outlier.size = 3)
    
    # Add aesthetic mappings
    # Note that boxplots are automatically dodged when any aesthetic is 
    # a factor
    p + geom_boxplot(aes(fill = cyl))
    p + geom_boxplot(aes(fill = factor(cyl)))
    p + geom_boxplot(aes(fill = factor(vs)))
    p + geom_boxplot(aes(fill = factor(am)))
    
    # Set aesthetics to fixed value
    p + geom_boxplot(fill="grey80", colour="#3366FF")
    qplot(factor(cyl), mpg, data = mtcars, geom = "boxplot", 
      colour = I("#3366FF"))

    # Scales vs. coordinate transforms -------
    # Scale transformations occur before the boxplot statistics are computed.
    # Coordinate transformations occur afterwards.  Observe the effect on the
    # number of outliers.
    m <- ggplot(movies, aes(y = votes, x = rating,
       group = round_any(rating, 0.5)))
    m + geom_boxplot()
    m + geom_boxplot() + scale_y_log10()
    m + geom_boxplot() + coord_trans(y = "log10")
    m + geom_boxplot() + scale_y_log10() + coord_trans(y = "log10")
    
    # Boxplots with continuous x:
    # Use the group aesthetic to group observations in boxplots
    qplot(year, budget, data = movies, geom = "boxplot")
    qplot(year, budget, data = movies, geom = "boxplot", 
      group = round_any(year, 10, floor))
    
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-crossbar.r"
GeomCrossbar <- proto(Geom, {
  objname <- "crossbar"
  desc <- "Hollow bar with middle indicated by horizontal line"
  desc_params <- list(
    "fatten" = "a multiplicate factor to fatten middle bar by"
  )

  icon <- function(.) {
    gTree(children=gList(
      rectGrob(c(0.3, 0.7), c(0.6, 0.8), width=0.3, height=c(0.4, 0.4), vjust=1),
      segmentsGrob(c(0.15, 0.55), c(0.5, 0.6), c(0.45, 0.85), c(0.5, 0.6))
    ))
  }
  
  reparameterise <- function(., df, params) {
    GeomErrorbar$reparameterise(df, params)
  }
  

  seealso <- list(
    "geom_errorbar" = "error bars",
    "geom_pointrange" = "range indicated by straight line, with point in the middle",
    "geom_linerange" = "range indicated by straight line + examples",
    "stat_summary" = "examples of these guys in use",
    "geom_smooth" = "for continuous analog"
  )

  default_stat <- function(.) StatIdentity
  default_pos <- function(.) PositionIdentity
  default_aes = function(.) aes(colour="black", fill=NA, size=0.5, linetype=1, alpha = 1)
  required_aes <- c("x", "y", "ymin", "ymax")
  guide_geom <- function(.) "path"
  
  draw <- function(., data, scales, coordinates, fatten = 2, width = NULL, ...) {
    middle <- transform(data, x = xmin, xend = xmax, yend = y, size = size * fatten)
    
    ggname(.$my_name(), gTree(children=gList(
      GeomRect$draw(data, scales, coordinates, ...),
      GeomSegment$draw(middle, scales, coordinates, ...)
    )))
  }
  
  examples <- function(.) {
    # See geom_linerange for examples
  }
})


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-defaults.r"
# Update geom defaults
# Modify geom aesthetic defaults for future plots
# 
# @arguments name of geom to modify
# @arguments named list of aesthetics
# @keyword hplot
#X update_geom_defaults("point", list(colour = "darkblue"))
#X qplot(mpg, wt, data = mtcars)
#X update_geom_defaults("point", list(colour = "black"))
update_geom_defaults <- function(geom, new) {
  g <- Geom$find(geom)
  old <- g$default_aes()
  
  aes <- defaults(new, old)
  
  g$default_aes <- eval(substitute(function(.) aes, list(aes = aes)))
}

# change geom aesthetics
# change geom defaults for other params
# change scale defaults
# change default scale for given aesthetic

# Update geom defaults
# Modify geom aesthetic defaults for future plots
# 
# @arguments name of geom to modify
# @arguments named list of aesthetics
# @keyword hplot
update_stat_defaults <- function(geom, new) {
  g <- Stat$find(geom)
  old <- g$default_aes()
  
  aes <- defaults(new, old)
  g$default_aes <- eval(substitute(function(.) aes, list(aes = aes)))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-error.r"
GeomErrorbar <- proto(Geom, {
  objname <- "errorbar"
  desc <- "Error bars"
  icon <- function(.) {
    gTree(children=gList(
      segmentsGrob(c(0.3, 0.7), c(0.3, 0.5), c(0.3, 0.7), c(0.7, 0.9)),
      segmentsGrob(c(0.15, 0.55), c(0.3, 0.5), c(0.45, 0.85), c(0.3, 0.5)),
      segmentsGrob(c(0.15, 0.55), c(0.7, 0.9), c(0.45, 0.85), c(0.7, 0.9))
    ))
  }
  
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour = "black", size=0.5, linetype=1, width=0.5, alpha = 1)
  guide_geom <- function(.) "path"
  required_aes <- c("x", "ymin", "ymax")
  
  reparameterise <- function(., df, params) {
    df$width <- df$width %||% 
      params$width %||% (resolution(df$x, FALSE) * 0.9)
        
    transform(df,
      xmin = x - width / 2, xmax = x + width / 2, width = NULL
    )
  }

  seealso <- list(
    "geom_pointrange" = "range indicated by straight line, with point in the middle",
    "geom_linerange" = "range indicated by straight line",
    "geom_crossbar" = "hollow bar with middle indicated by horizontal line",
    "stat_summary" = "examples of these guys in use",
    "geom_smooth" = "for continuous analog"
  )

  draw <- function(., data, scales, coordinates, width = NULL, ...) {
    GeomPath$draw(with(data, data.frame( 
      x = as.vector(rbind(xmin, xmax, NA, x,    x,    NA, xmin, xmax)), 
      y = as.vector(rbind(ymax, ymax, NA, ymax, ymin, NA, ymin, ymin)),
      colour = rep(colour, each = 8),
      alpha = rep(alpha, each = 8),
      size = rep(size, each = 8),
      linetype = rep(linetype, each = 8),
      group = rep(1:(nrow(data)), each = 8),
      stringsAsFactors = FALSE, 
      row.names = 1:(nrow(data) * 8)
    )), scales, coordinates, ...)
  }
  
  examples <- function(.) {
    # Create a simple example dataset
    df <- data.frame(
      trt = factor(c(1, 1, 2, 2)), 
      resp = c(1, 5, 3, 4), 
      group = factor(c(1, 2, 1, 2)), 
      se = c(0.1, 0.3, 0.3, 0.2)
    )
    df2 <- df[c(1,3),]
    
    # Define the top and bottom of the errorbars
    limits <- aes(ymax = resp + se, ymin=resp - se)
    
    p <- ggplot(df, aes(fill=group, y=resp, x=trt))
    p + geom_bar(position="dodge", stat="identity")
    
    # Because the bars and errorbars have different widths
    # we need to specify how wide the objects we are dodging are
    dodge <- position_dodge(width=0.9)
    p + geom_bar(position=dodge) + geom_errorbar(limits, position=dodge, width=0.25)
    
    p <- ggplot(df2, aes(fill=group, y=resp, x=trt))
    p + geom_bar(position=dodge)
    p + geom_bar(position=dodge) + geom_errorbar(limits, position=dodge, width=0.25)

    p <- ggplot(df, aes(colour=group, y=resp, x=trt))
    p + geom_point() + geom_errorbar(limits, width=0.2)
    p + geom_pointrange(limits)
    p + geom_crossbar(limits, width=0.2)

    # If we want to draw lines, we need to manually set the
    # groups which define the lines - here the groups in the 
    # original dataframe
    p + geom_line(aes(group=group)) + geom_errorbar(limits, width=0.2)    
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-errorh.r"
GeomErrorbarh <- proto(Geom, {
  objname <- "errorbarh"
  desc <- "Horizontal error bars"
  icon <- function(.) {
    gTree(children=gList(
      segmentsGrob(c(0.5, 0.3), c(0.70, 0.30), c(0.9, 0.7), c(0.70, 0.30)),
      segmentsGrob(c(0.5, 0.3), c(0.55, 0.15), c(0.5, 0.3), c(0.85, 0.45)),
      segmentsGrob(c(0.9, 0.7), c(0.55, 0.15), c(0.9, 0.7), c(0.85, 0.45))
    ))
  }
  
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour = "black", size=0.5, linetype=1, height=0.5, alpha = 1)
  guide_geom <- function(.) "path"
  required_aes <- c("x", "xmin", "xmax")
  
  reparameterise <- function(., df, params) {
    df$height <- df$height %||% 
      params$height %||% (resolution(df$y, FALSE) * 0.9)
        
    transform(df,
      ymin = y - height / 2, ymax = y + height / 2, height = NULL
    )
  }

  seealso <- list(
    "geom_errorbar" = "vertical error bars"
  )
  desc_outputs <- list(
    "height" = "height of errorbars"
  )
  

  draw <- function(., data, scales, coordinates, height = NULL, ...) {
    GeomPath$draw(with(data, data.frame( 
      x = as.vector(rbind(xmax, xmax, NA, xmax, xmin, NA, xmin, xmin)),
      y = as.vector(rbind(ymin, ymax, NA, y,    y,    NA, ymin, ymax)), 
      colour = rep(colour, each = 8),
      alpha = rep(alpha, each = 8),
      size = rep(size, each = 8),
      linetype = rep(linetype, each = 8),
      group = rep(1:(nrow(data)), each = 8),
      stringsAsFactors = FALSE, 
      row.names = 1:(nrow(data) * 8)
    )), scales, coordinates, ...)
  }
  
  examples <- function(.) {
    df <- data.frame(
      trt = factor(c(1, 1, 2, 2)), 
      resp = c(1, 5, 3, 4), 
      group = factor(c(1, 2, 1, 2)), 
      se = c(0.1, 0.3, 0.3, 0.2)
    )
    
    # Define the top and bottom of the errorbars
    
    p <- ggplot(df, aes(resp, trt, colour = group))
    p + geom_point() +
      geom_errorbarh(aes(xmax = resp + se, xmin = resp - se))
      
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-freqpoly.r"
GeomFreqpoly <- proto(Geom, {
  objname <- "freqpoly"
  desc <- "Frequency polygon"
  icon <- function(.) {
    y <- c(0.2, 0.3, 0.5, 0.6,0.2, 0.8, 0.5, 0.3)
    linesGrob(seq(0.1, 0.9, by=0.1), y, gp=gpar(col="grey20"))
  }
  
  default_aes <- function(.) GeomPath$default_aes()
  default_stat <- function(.) StatBin
  draw <- function(., ...) GeomPath$draw(...)
  guide_geom <- function(.) "path"
  
  
  seealso <- list(
    geom_histogram = GeomHistogram$desc
  )
  
  examples <- function(.) {
    qplot(carat, data = diamonds, geom="freqpoly")
    qplot(carat, data = diamonds, geom="freqpoly", binwidth = 0.1)
    qplot(carat, data = diamonds, geom="freqpoly", binwidth = 0.01)

    qplot(price, data = diamonds, geom="freqpoly", binwidth = 1000)
    qplot(price, data = diamonds, geom="freqpoly", binwidth = 1000, 
      colour = color)
    qplot(price, ..density.., data = diamonds, geom="freqpoly", 
      binwidth = 1000, colour = color)

  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-hex.r"
GeomHex <- proto(Geom, {
  objname <- "hex"
  desc <- "Tile the plane with hexagons"

  draw <- function(., data, scales, coordinates, ...) { 
    with(coordinates$transform(data, scales), 
      ggname(.$my_name(), hexGrob(x, y, col=colour, 
        fill = alpha(fill, alpha)))
    )
  }
  
  required_aes <- c("x", "y")
  default_aes <- function(.) aes(colour=NA, fill = "grey50", size=0.5, alpha = 1)
  default_stat <- function(.) StatBinhex
  guide_geom <- function(.) "polygon"
  
  examples <- function() {
    # See ?stat_binhex for examples  
  }
  
})


# Draw hexagon grob
# Modified from code by Nicholas Lewin-Koh and Martin Maechler
# 
# @arguments x positions of hex centres
# @arguments y positions
# @arguments vector of hex sizes
# @arguments border colour
# @arguments fill colour
# @keyword internal
hexGrob <- function(x, y, size = rep(1, length(x)), colour = "grey50", fill = "grey90") {
  stopifnot(length(y) == length(x))
  
  dx <- resolution(x, FALSE)
  dy <- resolution(y, FALSE) / sqrt(3) / 2 * 1.15
  
  hexC <- hexcoords(dx, dy, n = 1)
  
  n <- length(x)

  polygonGrob(
    x = rep.int(hexC$x, n) * rep(size, each = 6) + rep(x, each = 6),
    y = rep.int(hexC$y, n) * rep(size, each = 6) + rep(y, each = 6),
    default.units = "native",
    id.lengths = rep(6, n), gp = gpar(col = colour, fill = fill)
  )
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-hline.r"
GeomHline <- proto(Geom, {
  new <- function(., data = NULL, mapping = NULL, yintercept = NULL, legend = NA, ...) {
    if (is.numeric(yintercept)) {
      data <- data.frame(yintercept = yintercept)
      yintercept <- NULL
      mapping <- aes_all(names(data))
      if(is.na(legend)) legend <- FALSE
    }
    .super$new(., data = data, mapping = mapping, inherit.aes = FALSE, 
      yintercept = yintercept, legend = legend, ...)
  }

  draw <- function(., data, scales, coordinates, ...) {
    data$x    <- -Inf
    data$xend <- Inf
    
    GeomSegment$draw(unique(data), scales, coordinates)
  }

  objname <- "hline"
  desc <- "Line, horizontal"
  icon <- function(.) linesGrob(c(0, 1), c(0.5, 0.5))
  details <- "<p>This geom allows you to annotate the plot with horizontal lines (see geom_vline and geom_abline for other types of lines)</p>\n\n<p>There are two ways to use it.  You can either specify the intercept of the line in the call to the geom, in which case the line will be in the same position in every panel.  Alternatively, you can supply a different intercept for each panel using a data.frame.  See the examples for the differences</p>"
    
  default_stat <- function(.) StatHline
  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha = 1)
  guide_geom <- function(.) "path"
  
  seealso <- list(
    geom_vline = "for vertical lines",
    geom_abline = "for lines defined by a slope and intercept",
    geom_segment = "for a more general approach"
  )
  
  examples <- function(.) {
    p <- ggplot(mtcars, aes(x = wt, y=mpg)) + geom_point()

    p + geom_hline(aes(yintercept=mpg))
    p + geom_hline(yintercept=20)
    p + geom_hline(yintercept=seq(10, 30, by=5))
    
    # To display different lines in different facets, you need to 
    # create a data frame.
    p <- qplot(mpg, wt, data=mtcars, facets = vs ~ am)

    hline.data <- data.frame(z = 1:4, vs = c(0,0,1,1), am = c(0,1,0,1))
    p + geom_hline(aes(yintercept = z), hline.data)
  } 
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-linerange.r"
GeomLinerange <- proto(Geom, {
  objname <- "linerange"
  desc <- "An interval represented by a vertical line"

  seealso <- list(
    "geom_errorbar" = "error bars",
    "geom_pointrange" = "range indicated by straight line, with point in the middle",
    "geom_crossbar" = "hollow bar with middle indicated by horizontal line",
    "stat_summary" = "examples of these guys in use",
    "geom_smooth" = "for continuous analog"
  )
  
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour = "black", size=0.5, linetype=1, alpha = 1)
  guide_geom <- function(.) "path"
  required_aes <- c("x", "ymin", "ymax")

  draw <- function(., data, scales, coordinates, ...) {
    munched <- coordinates$transform(data, scales)
    ggname(.$my_name(), GeomSegment$draw(transform(data, xend=x, y=ymin, yend=ymax), scales, coordinates, ...))
  }

  icon <- function(.) segmentsGrob(c(0.3, 0.7), c(0.1, 0.2), c(0.3, 0.7), c(0.7, 0.95))
  
  examples <- function(.) {
    # Generate data: means and standard errors of means for prices
    # for each type of cut
    dmod <- lm(price ~ cut, data=diamonds)
    cuts <- data.frame(cut=unique(diamonds$cut), predict(dmod, data.frame(cut = unique(diamonds$cut)), se=T)[c("fit","se.fit")])
    
    qplot(cut, fit, data=cuts)
    # With a bar chart, we are comparing lengths, so the y-axis is 
    # automatically extended to include 0
    qplot(cut, fit, data=cuts, geom="bar")
    
    # Display estimates and standard errors in various ways
    se <- ggplot(cuts, aes(cut, fit, 
      ymin = fit - se.fit, ymax=fit + se.fit, colour = cut))
    se + geom_linerange()
    se + geom_pointrange()
    se + geom_errorbar(width = 0.5)
    se + geom_crossbar(width = 0.5)

    # Use coord_flip to flip the x and y axes
    se + geom_linerange() + coord_flip()
  }  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-path-.r"
GeomPath <- proto(Geom, {
  draw_groups <- function(., ...) .$draw(...)

  draw <- function(., data, scales, coordinates, arrow = NULL, lineend = "butt", linejoin = "round", linemitre = 1, ..., na.rm = FALSE) {

    keep <- function(x) {
      # from first non-missing to last non-missing
      first <- match(FALSE, x, nomatch = 1) - 1
      last <- length(x) - match(FALSE, rev(x), nomatch = 1) + 1
      c(
        rep(FALSE, first), 
        rep(TRUE, last - first), 
        rep(FALSE, length(x) - last))
    }    
    # Drop missing values at the start or end of a line - can't drop in the 
    # middle since you expect those to be shown by a break in the line
    missing <- !complete.cases(data[c("x", "y", "size", "colour",
      "linetype")])
    kept <- ave(missing, data$group, FUN=keep)
    data <- data[kept, ]
    
    if (!all(kept) && !na.rm) {
      warning("Removed ", sum(!kept), " rows containing missing values", 
        " (geom_path).", call. = FALSE)
    }
    
    munched <- coordinates$munch(data, scales)

    # Silently drop lines with less than two points, preserving order
    rows <- ave(seq_len(nrow(munched)), munched$group, FUN = length)
    munched <- munched[rows >= 2, ]
    if (nrow(munched) < 2) return(zeroGrob())

    # Work out whether we should use lines or segments
    attr <- ddply(munched, .(group), function(df) {
      data.frame(
        solid = identical(unique(df$linetype), 1),
        constant = nrow(unique(df[, c("alpha", "colour","size", "linetype")])) == 1
      )
    })
    solid_lines <- all(attr$solid)
    constant <- all(attr$constant)
    if (!solid_lines && !constant) {
      stop("geom_path: If you are using dotted or dashed lines", 
        ", colour, size and linetype must be constant over the line",
        call.=FALSE)
    }
    
    # Work out grouping variables for grobs
    n <- nrow(munched)
    group_diff <- munched$group[-1] != munched$group[-n]
    start <- c(TRUE, group_diff)
    end <-   c(group_diff, TRUE)  
    
    if (!constant) {
      with(munched, 
        segmentsGrob(
          x[!end], y[!end], x[!start], y[!start],
          default.units="native", arrow = arrow, 
          gp = gpar(
            col = alpha(colour, alpha)[!end], 
            lwd = size[!end] * .pt, lty = linetype[!end], 
            lineend = lineend, linejoin = linejoin, linemitre = linemitre
          )
        )
      )
    } else {
      with(munched, 
        polylineGrob(
          x, y, id = as.integer(factor(group)), 
          default.units = "native", arrow = arrow, 
          gp = gpar(
            col = alpha(colour, alpha)[start], 
            lwd = size[start] * .pt, lty = linetype[start], 
            lineend = lineend, linejoin = linejoin, linemitre = linemitre)
        )
      )
    }
  }

  draw_legend <- function(., data, ...) {
    data$arrow <- NULL
    data <- aesdefaults(data, .$default_aes(), list(...))

    with(data, 
      ggname(.$my_name(), segmentsGrob(0.1, 0.5, 0.9, 0.5, default.units="npc",
      gp=gpar(col=alpha(colour, alpha), lwd=size * .pt, 
        lty=linetype, lineend="butt")))
    )
  }
  
  objname <- "path"
  desc <- "Connect observations, in original order"

  desc_params <- list(
    lineend = "Line end style (round, butt, square)",
    linejoin = "Line join style (round, mitre, bevel)",
    linemitre = "Line mitre limit (number greater than 1)",
    arrow = "Arrow specification, as created by ?arrow"
  )
  

  default_stat <- function(.) StatIdentity
  required_aes <- c("x", "y")
  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha = 1)
  icon <- function(.) linesGrob(c(0.2, 0.4, 0.8, 0.6, 0.5), c(0.2, 0.7, 0.4, 0.1, 0.5))
  guide_geom <- function(.) "path"
  
  seealso <- list(
    geom_line = "Functional (ordered) lines", 
    geom_polygon = "Filled paths (polygons)",
    geom_segment = "Line segments"
  )

  examples <- function(.) {
    # Generate data
    myear <- ddply(movies, .(year), colwise(mean, .(length, rating)))
    p <- ggplot(myear, aes(length, rating))
    p + geom_path()

    # Add aesthetic mappings
    p + geom_path(aes(size = year))
    p + geom_path(aes(colour = year))
    
    # Change scale
    p + geom_path(aes(size = year)) + scale_size(to = c(1, 3))

    # Set aesthetics to fixed value
    p + geom_path(colour = "green")
    
    # Control line join parameters
    df <- data.frame(x = 1:3, y = c(4, 1, 9))
    base <- ggplot(df, aes(x, y))
    base + geom_path(size = 10)
    base + geom_path(size = 10, lineend = "round")
    base + geom_path(size = 10, linejoin = "mitre", lineend = "butt")
    
    # Use qplot instead
    qplot(length, rating, data=myear, geom="path")
    
    # Using economic data:
    # How is unemployment and personal savings rate related?
    qplot(unemploy/pop, psavert, data=economics)
    qplot(unemploy/pop, psavert, data=economics, geom="path")
    qplot(unemploy/pop, psavert, data=economics, geom="path", size=as.numeric(date))

    # How is rate of unemployment and length of unemployment?
    qplot(unemploy/pop, uempmed, data=economics)
    qplot(unemploy/pop, uempmed, data=economics, geom="path")
    qplot(unemploy/pop, uempmed, data=economics, geom="path") +
      geom_point(data=head(economics, 1), colour="red") + 
      geom_point(data=tail(economics, 1), colour="blue")
    qplot(unemploy/pop, uempmed, data=economics, geom="path") +
      geom_text(data=head(economics, 1), label="1967", colour="blue") + 
      geom_text(data=tail(economics, 1), label="2007", colour="blue")
    
    # geom_path removes missing values on the ends of a line.
    # use na.rm = T to suppress the warning message
    df <- data.frame(
      x = 1:5,
      y1 = c(1, 2, 3, 4, NA),
      y2 = c(NA, 2, 3, 4, 5),
      y3 = c(1, 2, NA, 4, 5),
      y4 = c(1, 2, 3, 4, 5))
    qplot(x, y1, data = df, geom = c("point","line"))
    qplot(x, y2, data = df, geom = c("point","line"))
    qplot(x, y3, data = df, geom = c("point","line"))
    qplot(x, y4, data = df, geom = c("point","line"))
    
    # Setting line type vs colour/size
    # Line type needs to be applied to a line as a whole, so it can
    # not be used with colour or size that vary across a line
    
    x <- seq(0.01, .99, length=100)
    df <- data.frame(x = rep(x, 2), y = c(qlogis(x), 2 * qlogis(x)), group = rep(c("a","b"), each=100))
    p <- ggplot(df, aes(x=x, y=y, group=group))

    # Should work
    p + geom_line(linetype = 2)
    p + geom_line(aes(colour = group), linetype = 2)
    p + geom_line(aes(colour = x))
    
    # Should fail
    should_stop(p + geom_line(aes(colour = x), linetype=2))
    
  }  
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-path-contour.r"
GeomContour <- proto(GeomPath, {
  objname <- "contour"
  desc <- "Display contours of a 3d surface in 2d"
  icon <- function(.) {
    ggname(.$my_name(), gTree(children=gList(
      polygonGrob(c(0.45,0.5,0.6, 0.5), c(0.5, 0.4, 0.55, 0.6)),
      polygonGrob(c(0.25,0.6,0.8, 0.5), c(0.5, 0.2, 0.75, 0.9), gp=gpar(fill=NA))
    )))
  }
  default_aes <- function(.) aes(weight=1, colour="#3366FF", size = 0.5, linetype = 1, alpha = 1)

  default_stat <- function(.) StatContour
  seealso <- list(
    geom_density2d = "Draw 2d density contours"
  )
  examples <- function(.) {
    # See stat_contour for examples
  }
  
})


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-path-density2d.r"
GeomDensity2d <- proto(GeomPath, {
  objname <- "density2d"
  desc <- "Contours from a 2d density estimate"
  
  details <- "<p>Perform a 2D kernel density estimatation using kde2d and  display the results with contours.</p>"
  advice <- "<p>This can be useful for dealing with overplotting.</p>"
  
  default_stat <- function(.) StatDensity2d
  default_aes <- function(.) aes(weight=1, colour="#3366FF", size = 0.5, linetype = 1, alpha = 1)
  icon <- function(.) GeomContour$icon()
  

  seealso <- list(
    geom_contour = "contour drawing geom",
    stat_sum = "another way of dealing with overplotting"
  )
  
  examples <- function(.) {
    # See stat_density2d for examples
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-path-line.r"
GeomLine <- proto(GeomPath, {
  objname <- "line"
  desc <- "Connect observations, in ordered by x value"
  icon <- function(.) {
    pos <- seq(0, 1, length=5)
    linesGrob(pos, c(0.2, 0.7, 0.4, 0.8, 0.3))
  }
  
  draw <- function(., data, scales, coordinates, arrow = NULL, ...) {
    data <- data[order(data$group, data$x), ]
    GeomPath$draw(data, scales, coordinates, arrow, ...)
  }
  
  default_stat <- function(.) StatIdentity
  
  seealso <- list(
    geom_path = GeomPath$desc,
    geom_segment = "Line segments",
    geom_ribbon = "Fill between line and x-axis"
  )
  
  examples <- function(.) {
    # Summarise number of movie ratings by year of movie
    mry <- do.call(rbind, by(movies, round(movies$rating), function(df) { 
      nums <- tapply(df$length, df$year, length)
      data.frame(rating=round(df$rating[1]), year = as.numeric(names(nums)), number=as.vector(nums))
    }))

    p <- ggplot(mry, aes(x=year, y=number, group=rating))
    p + geom_line()

    # Add aesthetic mappings
    p + geom_line(aes(size = rating))
    p + geom_line(aes(colour = rating))

    # Change scale
    p + geom_line(aes(colour = rating)) + scale_colour_gradient(low="red")
    p + geom_line(aes(size = rating)) + scale_size(to = c(0.1, 3))
    
    # Set aesthetics to fixed value
    p + geom_line(colour = "red", size = 1)

    # Use qplot instead
    qplot(year, number, data=mry, group=rating, geom="line")
    
    # Using a time series
    qplot(date, pop, data=economics, geom="line")
    qplot(date, pop, data=economics, geom="line", log="y")
    qplot(date, pop, data=subset(economics, date > as.Date("2006-1-1")), geom="line")
    qplot(date, pop, data=economics, size=unemploy/pop, geom="line")
    
    # See scale_date for examples of plotting multiple times series on
    # a single graph
    
    # A simple pcp example

    y2005 <- runif(300, 20, 120)
    y2010 <- y2005 * runif(300, -1.05, 1.5)
    group <- rep(LETTERS[1:3], each = 100)

    df <- data.frame(id = seq_along(group), group, y2005, y2010)
    dfm <- reshape::melt(df, id.var = c("id", "group"))
    ggplot(dfm, aes(variable, value, group = id, colour = group)) + 
      geom_path(alpha = 0.5)
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-path-step.r"
GeomStep <- proto(Geom, {
  objname <- "step"
  desc <- "Connect observations by stairs"
  icon <- function(.) {
    n <- 15
    xs <- rep(0:n, each = 2)[-2*(n + 1)] / 15
    ys <- c(0, rep(1:n, each=2)) / 15
    
    linesGrob(xs, ys, gp=gpar(col="grey20"))
  }
  details <- "Equivalent to plot(type='s')."

  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha = 1)
  
  draw <- function(., data, scales, coordinates, direction = "hv", ...) {
    data <- stairstep(data, direction)
    GeomPath$draw(data, scales, coordinates, ...)
  }
  guide_geom <- function(.) "path"

  desc_params <- list(
    direction = "direction of stairs: 'vh' for vertical then horizontal, or 'hv' for horizontal then vertical"
  )
  default_stat <- function(.) StatIdentity
  
  examples <- function(.) {
    # Simple quantiles/ECDF from examples(plot)
    x <- sort(rnorm(47))
    qplot(seq_along(x), x, geom="step")
    
    # Steps go horizontally, then vertically (default)
    qplot(seq_along(x), x, geom="step", direction = "hv")
    plot(x, type = "s")
    # Steps go vertically, then horizontally
    qplot(seq_along(x), x, geom="step", direction = "vh")
    plot(x, type = "S")
    
    # Also works with other aesthetics
    df <- data.frame(
      x = sort(rnorm(50)),
      trt = sample(c("a", "b"), 50, rep = T)
    )
    qplot(seq_along(x), x, data = df, geom="step", colour = trt)
    
  }
})


# Calculate stairsteps
# Used by \code{\link{geom_step}}
# 
# @keyword internal
stairstep <- function(data, direction="hv") {
  direction <- match.arg(direction, c("hv", "vh"))
  data <- as.data.frame(data)[order(data$x), ]
  n <- nrow(data)
  
  if (direction == "vh") {
    xs <- rep(1:n, each = 2)[-2*n]
    ys <- c(1, rep(2:n, each=2))
  } else {
    ys <- rep(1:n, each = 2)[-2*n]
    xs <- c(1, rep(2:n, each=2))
  }
  
  data.frame(
    x = data$x[xs],
    y = data$y[ys],
    data[xs, setdiff(names(data), c("x", "y"))]
  ) 
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-point-.r"
GeomPoint <- proto(Geom, {
  draw_groups <- function(., ...) .$draw(...)
  draw <- function(., data, scales, coordinates, na.rm = FALSE, ...) {    
    data <- remove_missing(data, na.rm, 
      c("x", "y", "size", "shape"), name = "geom_point")
    if (empty(data)) return(zeroGrob())

    with(coordinates$transform(data, scales), 
      ggname(.$my_name(), pointsGrob(x, y, size=unit(size, "mm"), pch=shape, 
      gp=gpar(col=alpha(colour, alpha), fill = fill, fontsize = size * .pt)))
    )
  }

  draw_legend <- function(., data, ...) {
    # If fill is set, ensure that you can actually see it
    if (!is.null(data$fill) && !all(is.na(data$fill)) && data$shape == 16) {
      data$shape <- 21
    } 
    data <- aesdefaults(data, .$default_aes(), list(...))
    
    with(data,
      pointsGrob(0.5, 0.5, size=unit(size, "mm"), pch=shape, 
      gp=gpar(
        col=alpha(colour, alpha), 
        fill=alpha(fill, alpha), 
        fontsize = size * .pt)
      )
    )
  }

  objname <- "point"
  icon <- function(.) {
    pos <- seq(0.1, 0.9, length=6)
    pointsGrob(x=pos, y=pos, pch=19, gp=gpar(col="black", cex=0.5), default.units="npc")
  }
  
  desc <- "Points, as for a scatterplot"
  details <- "<p>The point geom is used to create scatterplots.</p>\n"
  
  advice <- "<p>The scatterplot is useful for displaying the relationship between two continuous variables, although it can also be used with one continuous and one categorical variable, or two categorical variables.  See geom_jitter for possibilities.</p>\n<p>The <em>bubblechart</em> is a scatterplot with a third variable mapped to the size of points.  There are no special names for scatterplots where another variable is mapped to point shape or colour, however.</p>\n<p>The biggest potential problem with a scatterplot is overplotting: whenever you have more than a few points, points may be plotted on top of one another.  This can severely distort the visual appearance of the plot.  There is no one solution to this problem, but there are some techniques that can help.  You can add additional information with stat_smooth, stat_quantile or stat_density2d.  If you have few unique x values, geom_boxplot may also be useful.  Alternatively, you can summarise the number of points at each location and display that in some way, using stat_sum.  Another technique is to use transparent points, <code>geom_point(colour=alpha('black', 0.05))</code></p>\n"
  
  default_stat <- function(.) StatIdentity
  required_aes <- c("x", "y")
  default_aes <- function(.) aes(shape=16, colour="black", size=2, fill = NA, alpha = 1)

  seealso <- list(
    scale_size = "To see how to scale area of points, instead of radius",
    geom_jitter = "Jittered points for categorical data"
  )
  
  examples <- function(.) {
    p <- ggplot(mtcars, aes(wt, mpg))
    p + geom_point()

    # Add aesthetic mappings
    p + geom_point(aes(colour = qsec))
    p + geom_point(aes(alpha = qsec))
    p + geom_point(aes(colour = factor(cyl)))
    p + geom_point(aes(shape = factor(cyl)))
    p + geom_point(aes(size = qsec))

    # Change scales
    p + geom_point(aes(colour = cyl)) + scale_colour_gradient(low = "blue")
    p + geom_point(aes(size = qsec)) + scale_area()
    p + geom_point(aes(shape = factor(cyl))) + scale_shape(solid = FALSE)
    
    # Set aesthetics to fixed value
    p + geom_point(colour = "red", size = 3)
    qplot(wt, mpg, data = mtcars, colour = I("red"), size = I(3))
    
    # Varying alpha is useful for large datasets
    d <- ggplot(diamonds, aes(carat, price))
    d + geom_point(alpha = 1/10)
    d + geom_point(alpha = 1/20)
    d + geom_point(alpha = 1/100)
    
    # You can create interesting shapes by layering multiple points of
    # different sizes
    p <- ggplot(mtcars, aes(mpg, wt))
    p + geom_point(colour="grey50", size = 4) + geom_point(aes(colour = cyl))  
    p + aes(shape = factor(cyl)) + 
      geom_point(aes(colour = factor(cyl)), size = 4) +
      geom_point(colour="grey90", size = 1.5)
    p + geom_point(colour="black", size = 4.5) + 
      geom_point(colour="pink", size = 4) + 
      geom_point(aes(shape = factor(cyl)))  
        
    # These extra layers don't usually appear in the legend, but we can
    # force their inclusion
    p + geom_point(colour="black", size = 4.5, legend = TRUE) + 
      geom_point(colour="pink", size = 4, legend = TRUE) + 
      geom_point(aes(shape = factor(cyl)))  
        
    # Transparent points:
    qplot(mpg, wt, data = mtcars, size = I(5), alpha = I(0.2))
    
    # geom_point warns when missing values have been dropped from the data set
    # and not plotted, you can turn this off by setting na.rm = TRUE
    mtcars2 <- transform(mtcars, mpg = ifelse(runif(32) < 0.2, NA, mpg))
    qplot(wt, mpg, data = mtcars2)
    qplot(wt, mpg, data = mtcars2, na.rm = TRUE)
    
    # Use qplot instead
    qplot(wt, mpg, data = mtcars)
    qplot(wt, mpg, data = mtcars, colour = factor(cyl))
    qplot(wt, mpg, data = mtcars, colour = I("red"))
  }
  
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-point-jitter.r"
GeomJitter <- proto(GeomPoint, {
  objname <- "jitter"
  details <- "<p>The jitter geom is a convenient default for geom_point with position = 'jitter'.  See position_jitter for more details on adjusting the amount of jittering.</p>"
  advice <- "<p>It is often useful for plotting categorical data.</p>"
  
  desc <- "Points, jittered to reduce overplotting"
  icon <- function(.) {
    pos <- seq(0.1, 0.9, length=6)
    pointsGrob(x=pos, y=jitter(pos, 3), pch=19, gp=gpar(col="black", cex=0.5), default.units="npc")
  }
  
  default_stat <- function(.) StatIdentity
  default_pos <- function(.) PositionJitter
  
  seealso <- list(
    geom_point = "Regular, unjittered points",
    geom_boxplot = "Another way of looking at the conditional distribution of a variable",
    position_jitter = "For examples, using jittering with other geoms"
  )
  
  examples <- function(.) {
    p <- ggplot(movies, aes(x=mpaa, y=rating)) 
    p + geom_point()
    p + geom_point(position = "jitter")

    # Add aesthetic mappings
    p + geom_jitter(aes(colour=rating))
    
    # Vary parameters
    p + geom_jitter(position=position_jitter(width=5))
    p + geom_jitter(position=position_jitter(height=5))
    
    # Use qplot instead
    qplot(mpaa, rating, data=movies, geom="jitter")
    qplot(mpaa, rating, data=movies, geom=c("boxplot","jitter"))
    qplot(mpaa, rating, data=movies, geom=c("jitter", "boxplot"))
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-pointrange.r"
GeomPointrange <- proto(Geom, {
  objname <- "pointrange"
  desc <- "An interval represented by a vertical line, with a point in the middle"
  icon <- function(.) {
    gTree(children=gList(
      segmentsGrob(c(0.3, 0.7), c(0.1, 0.2), c(0.3, 0.7), c(0.7, 0.95)),
      pointsGrob(c(0.3, 0.7), c(0.4, 0.6), pch=19, gp=gpar(col="black", cex=0.5), default.units="npc")
    ))
  }
  
  seealso <- list(
    "geom_errorbar" = "error bars",
    "geom_linerange" = "range indicated by straight line, + examples",
    "geom_crossbar" = "hollow bar with middle indicated by horizontal line",
    "stat_summary" = "examples of these guys in use",
    "geom_smooth" = "for continuous analog"
  )
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour = "black", size=0.5, linetype=1, shape=16, fill=NA, alpha = 1)
  guide_geom <- function(.) "pointrange"
  required_aes <- c("x", "y", "ymin", "ymax")

  draw <- function(., data, scales, coordinates, ...) {
    if (is.null(data$y)) return(GeomLinerange$draw(data, scales, coordinates, ...))
    ggname(.$my_name(),gTree(children=gList(
      GeomLinerange$draw(data, scales, coordinates, ...),
      GeomPoint$draw(transform(data, size = size * 4), scales, coordinates, ...)
    )))
  }

  draw_legend <- function(., data, ...) {
    data <- aesdefaults(data, .$default_aes(), list(...))
    
    grobTree(
      GeomPath$draw_legend(data, ...),
      GeomPoint$draw_legend(transform(data, size = size * 4), ...)
    )
  }
  
  
  examples <- function(.) {
    # See geom_linerange for examples
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-polygon.r"
GeomPolygon <- proto(Geom, {
  draw <- function(., data, scales, coordinates, ...) {
    n <- nrow(data)
    if (n == 1) return()
    
    ggname(.$my_name(), gTree(children=gList(
      with(coordinates$munch(data, scales), 
        polygonGrob(x, y, default.units="native",
        gp=gpar(col=colour, fill=alpha(fill, alpha), lwd=size * .pt,
         lty=linetype))
      )
      #GeomPath$draw(data, scales, coordinates)
    )))
  }

  objname <- "polygon"
  desc <- "Polygon, a filled path"
  icon <- function(.) polygonGrob(c(0.1, 0.4, 0.7, 0.9, 0.6, 0.3), c(0.5, 0.8, 0.9, 0.4, 0.2, 0.3), gp=gpar(fill="grey20", col=NA))
  
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour="NA", fill="grey20", size=0.5, linetype=1, alpha = 1)
  required_aes <- c("x", "y")
  guide_geom <- function(.) "polygon"

  draw_legend <- function(., data, ...)  {
    data <- aesdefaults(data, .$default_aes(), list(...))
  
    with(data, grobTree(
      rectGrob(gp = gpar(col = colour, fill = alpha(fill, alpha), lty = linetype)),
      linesGrob(gp = gpar(col = colour, lwd = size * .pt, lineend="butt", lty = linetype))
    ))
  }

  seealso <- list(
    geom_path = "an unfilled polygon",
    geom_ribbon = "a polygon anchored on the x-axis"
  )
  
  examples <- function(.) {
    # When using geom_polygon, you will typically need two data frames:
    # one contains the coordinates of each polygon (positions),  and the
    # other the values associated with each polygon (values).  An id
    # variable links the two together

    ids <- factor(c("1.1", "2.1", "1.2", "2.2", "1.3", "2.3"))

    values <- data.frame(
      id = ids, 
      value = c(3, 3.1, 3.1, 3.2, 3.15, 3.5)
    )

    positions <- data.frame(
      id = rep(ids, each = 4),
      x = c(2, 1, 1.1, 2.2, 1, 0, 0.3, 1.1, 2.2, 1.1, 1.2, 2.5, 1.1, 0.3, 
      0.5, 1.2, 2.5, 1.2, 1.3, 2.7, 1.2, 0.5, 0.6, 1.3),
      y = c(-0.5, 0, 1, 0.5, 0, 0.5, 1.5, 1, 0.5, 1, 2.1, 1.7, 1, 1.5, 
      2.2, 2.1, 1.7, 2.1, 3.2, 2.8, 2.1, 2.2, 3.3, 3.2)
    )
    
    # Currently we need to manually merge the two together
    datapoly <- merge(values, positions, by=c("id"))

    (p <- ggplot(datapoly, aes(x=x, y=y)) + geom_polygon(aes(fill=value, group=id)))

    # Which seems like a lot of work, but then it's easy to add on 
    # other features in this coordinate system, e.g.:

    stream <- data.frame(
      x = cumsum(runif(50, max = 0.1)), 
      y = cumsum(runif(50,max = 0.1))
    )

    p + geom_line(data = stream, colour="grey30", size = 5)
    
    # And if the positions are in longitude and latitude, you can use
    # coord_map to produce different map projections.
  }
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-quantile.r"
GeomQuantile <- proto(GeomPath, {
  objname <- "quantile"
  desc <- "Add quantile lines from a quantile regression"
  
  advice <- "<p>This can be used as a continuous analogue of a geom_boxplot.</p>\n"
  default_stat <- function(.) StatQuantile
  default_aes <- function(.) defaults(aes(weight=1, colour="#3366FF", size=0.5), GeomPath$default_aes())
  guide_geom <- function(.) "path"
  

  icon <- function(.) {
    ggname(.$my_name(), gTree(children=gList(
      linesGrob(c(0, 0.3, 0.5, 0.8, 1), c(0.8, 0.65, 0.6, 0.6, 0.8)),
      linesGrob(c(0, 0.3, 0.5, 0.8, 1), c(0.55, 0.45, 0.5, 0.45, 0.55)),
      linesGrob(c(0, 0.3, 0.5, 0.8, 1), c(0.3, 0.25, 0.4, 0.3, 0.2))
    )))
  }


  examples <- function(.) {
    # See stat_quantile for examples
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-rect.r"
GeomRect <- proto(Geom, {
  
  default_stat <- function(.) StatIdentity
  default_pos <- function(.) PositionIdentity
  default_aes <- function(.) aes(colour=NA, fill="grey20", size=0.5, linetype=1, alpha = 1)
  
  required_aes <- c("xmin", "xmax", "ymin", "ymax")

  draw <- draw_groups <- function(., data, scales, coordinates, ...) {
    if (coordinates$muncher()) {
      aesthetics <- setdiff(
        names(data), c("x", "y", "xmin","xmax", "ymin", "ymax")
      )
      
      polys <- alply(data, 1, function(row) {
        poly <- with(row, rect_to_poly(xmin, xmax, ymin, ymax))
        aes <- as.data.frame(row[aesthetics], 
          stringsAsFactors = FALSE)[rep(1,5), ]
      
        GeomPolygon$draw(cbind(poly, aes), scales, coordinates)
      })
      
      ggname("bar",do.call("grobTree", polys))
    } else {
      with(coordinates$transform(data, scales), 
        ggname(.$my_name(), rectGrob(
          xmin, ymax, 
          width = xmax - xmin, height = ymax - ymin, 
          default.units = "native", just = c("left", "top"), 
          gp=gpar(
            col=colour, fill=alpha(fill, alpha), 
            lwd=size * .pt, lty=linetype, lineend="butt"
          )
        ))
      )
    }
    
  }
  
  # Documentation -----------------------------------------------
  objname <- "rect"
  desc <- "2d rectangles"
  guide_geom <- function(.) "polygon"
  
  icon <- function(.) {
    rectGrob(c(0.3, 0.7), c(0.4, 0.8), height=c(0.4, 0.8), width=0.3, vjust=1, gp=gpar(fill="grey20", col=NA))
  }
  
  examples <- function(.) {
    df <- data.frame(
      x = sample(10, 20, replace = TRUE),
      y = sample(10, 20, replace = TRUE)
    )
    ggplot(df, aes(xmin = x, xmax = x + 1, ymin = y, ymax = y + 2)) +
    geom_rect()
  }  

})

# Convert rectangle to polygon
# Useful for non-Cartesian coordinate systems where it's easy to work purely in terms of locations, rather than locations and dimensions.
# 
# @keyword internal
rect_to_poly <- function(xmin, xmax, ymin, ymax) {
  data.frame(
    y = c(ymax, ymax, ymin, ymin, ymax),
    x = c(xmin, xmax, xmax, xmin, xmin)
  )
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-ribbon-.r"
GeomRibbon <- proto(Geom, {
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour=NA, fill="grey20", size=0.5, linetype=1, alpha = 1)
  required_aes <- c("x", "ymin", "ymax")
  guide_geom <- function(.) "polygon"
  
  
  draw <- function(., data, scales, coordinates, na.rm = FALSE, ...) {
    if (na.rm) data <- data[complete.cases(data[required_aes]), ]
    data <- data[order(data$group, data$x), ]

    # Check that aesthetics are constant
    aes <- unique(data[c("colour", "fill", "size", "linetype", "alpha")])
    if (nrow(aes) > 1) {
      stop("Aesthetics can not vary with a ribbon")
    }
    aes <- as.list(aes)

    # Instead of removing NA values from the data and plotting a single
    # polygon, we want to "stop" plotting the polygon whenever we're
    # missing values and "start" a new polygon as soon as we have new
    # values.  We do this by creating an id vector for polygonGrob that
    # has distinct polygon numbers for sequences of non-NA values and NA
    # for NA values in the original data.  Example: c(NA, 2, 2, 2, NA, NA,
    # 4, 4, 4, NA)
    missing_pos <- !complete.cases(data[required_aes])
    ids <- cumsum(missing_pos) + 1
    ids[missing_pos] <- NA

    positions <- summarise(data, 
      x = c(x, rev(x)), y = c(ymax, rev(ymin)), id = c(ids, rev(ids)))
    munched <- coordinates$munch(positions, scales)

    ggname(.$my_name(), polygonGrob(
      munched$x, munched$y, id = munched$id,
      default.units = "native",
      gp = gpar(
        fill = alpha(aes$fill, aes$alpha), 
        col = aes$colour, 
        lwd = aes$size * .pt, 
        lty = aes$linetype)
    ))
  }

  # Documentation -----------------------------------------------
  objname <- "ribbon"
  desc <- "Ribbons, y range with continuous x values"
  
  icon <- function(.) {
    polygonGrob(c(0, 0.3, 0.5, 0.8, 1, 1, 0.8, 0.5, 0.3, 0), c(0.5, 0.3, 0.4, 0.2, 0.3, 0.7, 0.5, 0.6, 0.5, 0.7), gp=gpar(fill="grey20", col=NA))
  }
  
  seealso <- list(
    geom_bar = "Discrete intervals (bars)",
    geom_linerange = "Discrete intervals (lines)",
    geom_polygon = "General polygons"
  )
  
  examples <- function(.) {
    # Generate data
    huron <- data.frame(year = 1875:1972, level = as.vector(LakeHuron))
    huron$decade <- round_any(huron$year, 10, floor)

    h <- ggplot(huron, aes(x=year))

    h + geom_ribbon(aes(ymin=0, ymax=level))
    h + geom_area(aes(y = level))

    # Add aesthetic mappings
    h + geom_ribbon(aes(ymin=level-1, ymax=level+1))
    h + geom_ribbon(aes(ymin=level-1, ymax=level+1)) + geom_line(aes(y=level))
    
    # Take out some values in the middle for an example of NA handling
    huron[huron$year > 1900 & huron$year < 1910, "level"] <- NA
    h <- ggplot(huron, aes(x=year))
    h + geom_ribbon(aes(ymin=level-1, ymax=level+1)) + geom_line(aes(y=level))

    # Another data set, with multiple y's for each x
    m <- ggplot(movies, aes(y=votes, x=year)) 
    (m <- m + geom_point())
    
    # The default summary isn't that useful
    m + stat_summary(geom="ribbon", fun.ymin="min", fun.ymax="max")
    m + stat_summary(geom="ribbon", fun.data="median_hilow")
    
    # Use qplot instead
    qplot(year, level, data=huron, geom=c("area", "line"))
  }  
})

GeomArea <- proto(GeomRibbon,{
  default_aes <- function(.) aes(colour=NA, fill="grey20", size=0.5, linetype=1, alpha = 1)
  default_pos <- function(.) PositionStack
  required_aes <- c("x", "y")

  reparameterise <- function(., df, params) {
    transform(df, ymin = 0, ymax = y)
  }

  # Documentation -----------------------------------------------
  objname <- "area"
  desc <- "Area plots"

  icon <- function(.) {
    polygonGrob(c(0, 0,0.3, 0.5, 0.8, 1, 1), c(0, 1,0.5, 0.6, 0.3, 0.8, 0), gp=gpar(fill="grey20", col=NA))
  }

  details <- "<p>An area plot is the continuous analog of a stacked bar chart (see geom_bar), and can be used to show how composition of the whole varies over the range of x.  Choosing the order in which different components is stacked is very important, as it becomes increasing hard to see the individual pattern as you move up the stack.</p>\n<p>An area plot is a special case of geom_ribbon, where the minimum of the range is fixed to 0, and the position adjustment defaults to position_stacked.</p>"


  examples <- function(.) {
    # Examples to come
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-ribbon-density.r"
GeomDensity <- proto(GeomArea, {
  objname <- "density"
  desc <- "Display a smooth density estimate"
  details <- "A smooth density estimate calculated by stat_density"
  icon <- function(.) {
    x <- seq(0, 1, length=80)
    y <- dnorm(x, mean=0.5, sd=0.15)
    linesGrob(x, 0.05 + y / max(y) * 0.9, default="npc")
  }
  default_stat <- function(.) StatDensity
  default_pos <- function(.) PositionIdentity
  
  seealso <- list(
    geom_histogram = "for the histogram"
  )  

  default_aes <- function(.) defaults(aes(fill=NA, weight=1, colour="black", alpha = 1), GeomArea$default_aes())

  examples <- function(.) {
    # See stat_density for examples
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-rug.r"
GeomRug <- proto(Geom, {
  draw <- function(., data, scales, coordinates, ...) {  
    rugs <- list()
    data <- coordinates$transform(data, scales)    
    if (!is.null(data$x)) {
      rugs$x <- with(data, segmentsGrob(
        x0 = unit(x, "native"), x1 = unit(x, "native"), 
        y0 = unit(0, "npc"), y1 = unit(0.03, "npc"),
        gp = gpar(col = alpha(colour, alpha), lty = linetype, lwd = size * .pt)
      ))
    }  

    if (!is.null(data$y)) {
      rugs$y <- with(data, segmentsGrob(
        y0 = unit(y, "native"), y1 = unit(y, "native"), 
        x0 = unit(0, "npc"), x1 = unit(0.03, "npc"),
        gp = gpar(col = alpha(colour, alpha), lty = linetype, lwd = size * .pt)
      ))
    }  
    
    gTree(children = do.call("gList", rugs))
  }

  objname <- "rug"
  
  desc <- "Marginal rug plots"
  
  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha = 1)
  guide_geom <- function(.) "path"

  examples <- function(.) {
    p <- ggplot(mtcars, aes(x=wt, y=mpg))
    p + geom_point()
    p + geom_point() + geom_rug()
    p + geom_point() + geom_rug(position='jitter')
  }
  
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-segment.r"
GeomSegment <- proto(Geom, {
  draw <- function(., data, scales, coordinates, arrow=NULL, ...) {
    if (!coordinates$muncher()) {
      return(with(coordinates$transform(data, scales), 
        segmentsGrob(x, y, xend, yend, default.units="native",
        gp = gpar(col=alpha(colour, alpha), lwd=size * .pt, 
          lty=linetype, lineend = "butt"), 
        arrow = arrow)
      ))
    }

    data$group <- 1:nrow(data)
    starts <- subset(data, select = c(-xend, -yend))
    ends <- rename(subset(data, select = c(-x, -y)), c("xend" = "x", "yend" = "y"))
    
    pieces <- rbind(starts, ends)
    pieces <- pieces[order(pieces$group),]
    
    GeomPath$draw_groups(pieces, scales, coordinates, arrow = arrow, ...)
  }

  
  objname <- "segment"
  desc <- "Single line segments"
  icon <- function(.) segmentsGrob(c(0.1, 0.3, 0.5, 0.7), c(0.3, 0.5, 0.1, 0.9), c(0.2, 0.5, 0.7, 0.9), c(0.8, 0.7, 0.4, 0.3))

  desc_params <- list(
    arrow = "specification for arrow heads, as created by arrow()"
  )

  seealso <- list(
    geom_path = GeomPath$desc,
    geom_line = GeomLine$desc
  )

  default_stat <- function(.) StatIdentity
  required_aes <- c("x", "y", "xend", "yend")
  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha = 1)
  guide_geom <- function(.) "path"
  
  examples <- function(.) {
    require("maps")
    
    xlim <- range(seals$long)
    ylim <- range(seals$lat)
    usamap <- data.frame(map("world", xlim = xlim, ylim = ylim, plot =
    FALSE)[c("x","y")])
    usamap <- rbind(usamap, NA, data.frame(map('state', xlim = xlim, ylim
    = ylim, plot = FALSE)[c("x","y")]))
    names(usamap) <- c("long", "lat")
    
    p <- ggplot(seals, aes(x = long, y = lat))
    (p <- p + geom_segment(aes(xend = long + delta_long, yend = lat + delta_lat), arrow=arrow(length=unit(0.1,"cm"))))
    p + geom_path(data = usamap) + scale_x_continuous(limits=xlim)
    
    # You can also use geom_segment to recreate plot(type = "h") : 
    counts <- as.data.frame(table(x = rpois(100,5)))
    counts$x <- as.numeric(as.character(counts$x))
    with(counts, plot(x, Freq, type = "h", lwd = 10))

    qplot(x, Freq, data = counts, geom="segment", 
      yend = 0, xend = x, size = I(10))
    
    
  }
  
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-smooth.r"
GeomSmooth <- proto(Geom, {
  draw <- function(., data, scales, coordinates, ...) {
    ribbon <- transform(data, colour = NA)
    path <- transform(data, alpha = 1)
    
    has_ribbon <- function(x) !is.null(data$ymax) && !is.null(data$ymin)
        
    gList(
      if (has_ribbon(data)) GeomRibbon$draw(ribbon, scales, coordinates),
      GeomLine$draw(path, scales, coordinates)
    )
  }

  objname <- "smooth"
  desc <- "Add a smoothed condition mean."
  icon <- function(.) {
    gTree(children=gList(
      polygonGrob(c(0, 0.3, 0.5, 0.8, 1, 1, 0.8, 0.5, 0.3, 0), c(0.5, 0.3, 0.4, 0.2, 0.3, 0.7, 0.5, 0.6, 0.5, 0.7), gp=gpar(fill="grey60", col=NA)),
      linesGrob(c(0, 0.3, 0.5, 0.8, 1), c(0.6, 0.4, 0.5, 0.4, 0.6))
    ))
  }
  
  guide_geom <- function(.) "smooth"
  
  default_stat <- function(.) StatSmooth
  required_aes <- c("x", "y")
  default_aes <- function(.) aes(colour="#3366FF", fill="grey60", size=0.5, linetype=1, weight=1, alpha=0.4)


  draw_legend <- function(., data, params, ...) {
    data <- aesdefaults(data, .$default_aes(), list(...))
    data$fill <- alpha(data$fill, data$alpha)
    data$alpha <- 1
    
    if (is.null(params$se) || params$se) {
      gTree(children = gList(
        rectGrob(gp = gpar(col = NA, fill = data$fill)),
        GeomPath$draw_legend(data, ...)
      ))      
    } else {
      GeomPath$draw_legend(data, ...)
    }
  }
  examples <- function(.) {
    # See stat_smooth for examples of using built in model fitting
    # if you need some more flexible, this example shows you how to
    # plot the fits from any model of your choosing
    qplot(wt, mpg, data=mtcars, colour=factor(cyl))

    model <- lm(mpg ~ wt + factor(cyl), data=mtcars)
    grid <- with(mtcars, expand.grid(
      wt = seq(min(wt), max(wt), length = 20),
      cyl = levels(factor(cyl))
    ))

    grid$mpg <- stats::predict(model, newdata=grid)

    qplot(wt, mpg, data=mtcars, colour=factor(cyl)) + geom_line(data=grid)

    # or with standard errors

    err <- stats::predict(model, newdata=grid, se = TRUE)
    grid$ucl <- err$fit + 1.96 * err$se.fit
    grid$lcl <- err$fit - 1.96 * err$se.fit

    qplot(wt, mpg, data=mtcars, colour=factor(cyl)) + 
      geom_smooth(aes(ymin = lcl, ymax = ucl), data=grid, stat="identity") 
  }

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-text.r"
GeomText <- proto(Geom, {
  draw <- function(., data, scales, coordinates, ..., parse = FALSE) {
    
    lab <- data$label
    if (parse) {
      lab <- parse(text = lab)
    }
    
    with(coordinates$transform(data, scales), 
      textGrob(lab, x, y, default.units="native", hjust=hjust, vjust=vjust, rot=angle, 
      gp=gpar(col=alpha(colour, alpha), fontsize=size * .pt)) 
    )
  }

  desc_params <- list(
    parse = "If TRUE, the labels will be parsed into expressions and displayed as described in ?plotmath"
  )


  draw_legend <- function(., data, ...) {
    data <- aesdefaults(data, .$default_aes(), list(...))
    with(data,
      textGrob("a", 0.5, 0.5, rot = angle, 
      gp=gpar(col=alpha(colour, alpha), fontsize = size * .pt))
    )
  }

  objname <- "text"
  icon <- function(.) textGrob("text", rot=45, gp=gpar(cex=1.2))
  desc <- "Textual annotations"
  
  default_stat <- function(.) StatIdentity
  required_aes <- c("x", "y", "label")
  default_aes <- function(.) aes(colour="black", size=5 , angle=0, hjust=0.5, vjust=0.5, alpha = 1)
  guide_geom <- function(x) "text"
  
  
  
  examples <- function(.) {
    p <- ggplot(mtcars, aes(x=wt, y=mpg, label=rownames(mtcars)))
    
    p + geom_text()
    p <- p + geom_point()

    # Set aesthetics to fixed value
    p + geom_text()
    p + geom_point() + geom_text(hjust=0, vjust=0)
    p + geom_point() + geom_text(angle = 45)

    # Add aesthetic mappings
    p + geom_text(aes(colour=factor(cyl)))
    p + geom_text(aes(colour=factor(cyl))) + scale_colour_discrete(l=40)
    
    p + geom_text(aes(size=wt))
    p + geom_text(aes(size=wt)) + scale_size(to=c(3,6))
    
    # You can display expressions by setting parse = TRUE.  The 
    # details of the display are described in ?plotmath, but note that
    # geom_text uses strings, not expressions.
    p + geom_text(aes(label = paste(wt, "^(", cyl, ")", sep = "")),
      parse = T)
    
    # Use qplot instead
    qplot(wt, mpg, data = mtcars, label = rownames(mtcars),
       geom=c("point", "text"))
    qplot(wt, mpg, data = mtcars, label = rownames(mtcars), size = wt) +
      geom_text(colour = "red")
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-tile.r"
GeomTile <- proto(Geom, {
  reparameterise <- function(., df, params) {
    df$width <- df$width %||% params$width %||% resolution(df$x, FALSE)
    df$height <- df$height %||% params$height %||% resolution(df$y, FALSE)

    transform(df, 
      xmin = x - width / 2,  xmax = x + width / 2,  width = NULL,
      ymin = y - height / 2, ymax = y + height / 2, height = NULL 
    )
  }

  draw_groups <- function(., data,  scales, coordinates, ...) {
    # data$colour[is.na(data$colour)] <- data$fill[is.na(data$colour)]
    GeomRect$draw_groups(data, scales, coordinates, ...)
  }

  objname <- "tile"
  desc <- "Tile plot as densely as possible, assuming that every tile is the same size. "
  
  details <- "<p>Similar to levelplot and image.</p>"

  icon <- function(.) {
    rectGrob(c(0.25, 0.25, 0.75, 0.75), c(0.25, 0.75, 0.75, 0.25), width=0.5, height=c(0.67, 0.5, 0.67, 0.5), gp=gpar(col="grey20", fill=c("#804070", "#668040")))
  }

  default_stat <- function(.) StatIdentity
  default_aes <- function(.) aes(fill="grey20", colour=NA, size=0.1, linetype=1, alpha = 1)
  required_aes <- c("x", "y")
  guide_geom <- function(.) "polygon"
  
  
  examples <- function(.) {
    # Generate data
    pp <- function (n,r=4) {
     x <- seq(-r*pi, r*pi, len=n)
     df <- expand.grid(x=x, y=x)
     df$r <- sqrt(df$x^2 + df$y^2)
     df$z <- cos(df$r^2)*exp(-df$r/6)
     df
    }
    p <- ggplot(pp(20), aes(x=x,y=y))
    
    p + geom_tile() #pretty useless!

    # Add aesthetic mappings
    p + geom_tile(aes(fill=z))
    
    # Change scale
    p + geom_tile(aes(fill=z)) + scale_fill_gradient(low="green", high="red")

    # Use qplot instead
    qplot(x, y, data=pp(20), geom="tile", fill=z)
    qplot(x, y, data=pp(100), geom="tile", fill=z)
    
    # Missing values
    p <- ggplot(pp(20)[sample(20*20, size=200),], aes(x=x,y=y,fill=z))
    p + geom_tile()
    
    # Input that works with image
    image(t(volcano)[ncol(volcano):1,])
    ggplot(melt(volcano), aes(x=X1, y=X2, fill=value)) + geom_tile()
    
    # inspired by the image-density plots of Ken Knoblauch
    cars <- ggplot(mtcars, aes(y=factor(cyl), x=mpg))
    cars + geom_point()
    cars + stat_bin(aes(fill=..count..), geom="tile", binwidth=3, position="identity")
    cars + stat_bin(aes(fill=..density..), geom="tile", binwidth=3, position="identity")

    cars + stat_density(aes(fill=..density..), geom="tile", position="identity")
    cars + stat_density(aes(fill=..count..), geom="tile", position="identity")
    
    # Another example with with unequal tile sizes
    x.cell.boundary <- c(0, 4, 6, 8, 10, 14)
    example <- data.frame(
      x = rep(c(2, 5, 7, 9, 12), 2),
      y = factor(rep(c(1,2), each=5)),
      z = rep(1:5, each=2),
      w = rep(diff(x.cell.boundary), 2)
    )
  
    qplot(x, y, fill=z, data=example, geom="tile")
    qplot(x, y, fill=z, data=example, geom="tile", width=w)
    qplot(x, y, fill=factor(z), data=example, geom="tile", width=w)

    # You can manually set the colour of the tiles using 
    # scale_manual
    col <- c("darkblue", "blue", "green", "orange", "red")
    qplot(x, y, fill=col[z], data=example, geom="tile", width=w, group=1) + scale_fill_identity(labels=letters[1:5], breaks=col)
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/geom-vline.r"
GeomVline <- proto(Geom, {
  new <- function(., data = NULL, mapping = NULL, xintercept = NULL, legend = NA, ...) {
    if (is.numeric(xintercept)) {
      data <- data.frame(xintercept = xintercept)
      xintercept <- NULL
      mapping <- aes_all(names(data))
      if(is.na(legend)) legend <- FALSE
    }
    .super$new(., data = data, mapping = mapping, inherit.aes = FALSE, 
      xintercept = xintercept, legend = legend, ...)
  }
  
  draw <- function(., data, scales, coordinates, ...) {
    data$y    <- -Inf
    data$yend <- Inf
    
    GeomSegment$draw(unique(data), scales, coordinates)
  }

  objname <- "vline"
  desc <- "Line, vertical"
  icon <- function(.) linesGrob(c(0.5, 0.5), c(0, 1))
  details <- "<p>This geom allows you to annotate the plot with vertical lines (see geom_hline and geom_abline for other types of lines)</p>\n\n<p>There are two ways to use it.  You can either specify the intercept of the line in the call to the geom, in which case the line will be in the same position in every panel.  Alternatively, you can supply a different intercept for each panel using a data.frame.  See the examples for the differences</p>"
  
  default_stat <- function(.) StatVline
  default_aes <- function(.) aes(colour="black", size=0.5, linetype=1, alpha = 1)
  guide_geom <- function(.) "vline"

  draw_legend <- function(., data, ...) {
    data <- aesdefaults(data, .$default_aes(), list(...))

    with(data, 
      ggname(.$my_name(), segmentsGrob(0.5, 0, 0.5, 1, default.units="npc",
      gp=gpar(col=alpha(colour, alpha), lwd=size * .pt, lty=linetype, lineend="butt")))
    )
  }

  seealso <- list(
    geom_hline = "for horizontal lines",
    geom_abline = "for lines defined by a slope and intercept",
    geom_segment = "for a more general approach"
  )
  
  examples <- function(.) {
    # Fixed lines
    p <- ggplot(mtcars, aes(x = wt, y = mpg)) + geom_point()
    p + geom_vline(xintercept = 5)
    p + geom_vline(xintercept = 1:5)
    p + geom_vline(xintercept = 1:5, colour="green")
    
    last_plot() + coord_equal()
    last_plot() + coord_flip()
    
    p2 <- p + aes(colour = factor(cyl))
    p2 + geom_vline(xintercept = 15)
  }  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/grob-absolute.r"
# Absolute grob
# This grob has fixed dimesions and position.
# 
# It's still experimental
# 
# @alias grobHeight.absoluteGrob
# @alias grobWidth.absoluteGrob
# @alias grobX.absoluteGrob
# @alias grobY.absoluteGrob
# @alias grid.draw.absoluteGrob
# @keyword internal
absoluteGrob <- function(grob, width = NULL, height = NULL, xmin = NULL, ymin = NULL) {
  gTree(
    children = grob, 
    width = width, height = height, 
    xmin = xmin, ymin = ymin,
    cl="absoluteGrob"
  )
}

grobHeight.absoluteGrob <- function(x) {
  nulldefault(x$height, grobHeight(x$children))
}
grobWidth.absoluteGrob <- function(x) {
  nulldefault(x$width, grobWidth(x$children))
}

grobX.absoluteGrob <- function(x, theta) {
  if (!is.null(x$xmin) && theta == "west") return(x$xmin)
  grobX(x$children, theta)
}
grobY.absoluteGrob <- function(x, theta) {
  if (!is.null(x$ymin) && theta == "south") return(x$ymin)
  grobY(x$children, theta)
}

grid.draw.absoluteGrob <- function(x, recording = TRUE) {
  grid:::drawGTree(x)
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/grob-background.r"
# background <- function(grob, fill = NA, colour = NA, padding = unit(0, "lines"), margin = unit(0, "lines"), size = 1, linetype = 1) {
#   padding <- rep(list(padding), length = 4)
#   margin <- rep(list(margin), length = 4)
#   names(padding) <- names(margin) <- c("top", "right", "bottom", "left")
#   
#   width.in   <- sum(padding$left, grobWidth(grob), padding$right)
#   width.out  <- sum(margin$left, width.in, margin$right)
#   height.in  <- sum(padding$top, grobHeight(grob), padding$bottom)
#   height.out <- sum(margin$top, height.in, margin$bottom)
#   
#   vp <- viewport(width = width.out, height = height.out)
#   bg <- rectGrob(
#     x = grobX(grob, "west") - padding$left, hjust = 0,
#     y = grobY(grob, "south") - padding$top, vjust = 0,
#     width = width.in, height = height.in, 
#     gp = gpar(col = NA, fill = fill)
#   )
#   border <- rectGrob(
#     x = grobX(grob, "west") - padding$left, hjust = 0,
#     y = grobY(grob, "south") - padding$top, vjust = 0,
#     width = width.in, height = height.in, 
#     gp = gpar(col = colour, fill = NA, lty = linetype, lwd = size)
#   )
#   margin <- rectGrob(
#     x = grobX(grob, "west") - padding$left - margin$left, hjust = 0,
#     y = grobY(grob, "south") - padding$top - margin$top, vjust = 0,
#     width = width.out, height = height.out, 
#     gp = gpar(fill = NA, col = "grey90", size = 0.5, lty = 3)
#   )
#   
#   absoluteGrob(
#     grobTree(margin, bg, grob, border),
#     width = width.out,
#     height = height.out,
#     xmin = grobX(margin, "west"),
#     ymin = grobY(margin, "south")
#   )
# }
# 
# bg.test <- function(grob) {
#   background(grob, fill=sample(colors(), 1), colour="grey50", padding=unit(1, "lines"), margin=unit(1, "lines"), size=2)
# }

# r <- rectGrob(height=unit(3, "cm"), width=unit(2, "cm"), x=0.8, gp=gpar(fill="red"))
# grid.newpage(); grid.draw(bg.test(r))
# grid.newpage(); grid.draw(bg.test(bg.test(r)))
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/grob-grid.r"
# Experimental tools for create grids of grobs
# Still a work in progress.
# 
# @keyword internal
# @alias cbind.grobGrid
# @alias colWidths
# @alias cweave.grobGrid
# @alias cweave.list
# @alias cweave.matrix
# @alias dim.grobGrid
# @alias gTree.grobGrid
# @alias grid.draw.grobGrid
# @alias gridLayout
# @alias grobCol
# @alias grobGrid
# @alias grobRow
# @alias grobs.grobGrid
# @alias print.grobGrid
# @alias rbind.grobGrid
# @alias rep.unit2
# @alias rowHeights
# @alias rweave.grobGrid
# @alias rweave.list
# @alias rweave.matrix
# @alias spacer
# @alias viewports.grobGrid
# @alias as.list.unit
# @alias interleave.default
# @alias interleave.list
# @alias interleave.unit
# @alias max2
grobGrid <- function(name, nrow, ncol, grobs = NULL, widths = 0, heights = 0, clip = "on", default.units = "null", as.table = FALSE, respect = FALSE) {
  
  if (!is.unit(widths)) widths <- unit(widths, default.units)
  if (!is.unit(heights)) heights <- unit(heights, default.units)

  if (!missing(nrow) && !missing(ncol)) {
    widths <- rep(widths, length = ncol)
    heights <- rep(heights, length = nrow)    
  } else {
    nrow <- length(heights)
    ncol <- length(widths)    
  }
  
  # stopifnot(is.list(grobs))
  if (is.null(grobs)) {
    grobs <- matrix(list(zeroGrob()), nrow = nrow, ncol = ncol)
  } else {  
    mat <- c(grobs, rep(list(zeroGrob()), nrow * ncol - length(grobs)))
    dim(mat) <- c(ncol, nrow)
    grobs <- t(mat)
  }
  
  # If not display as table, reverse order of rows
  if (!as.table) {
    grobs <- grobs[rev(seq_len(nrow)), , drop = FALSE]
    heights <- rev(heights)
  }
  
  names <- matrix(name, ncol = ncol, nrow = nrow)
  clip <- matrix(clip, ncol = ncol, nrow = nrow)
    
  structure(list(
    names = names,
    grobs = grobs, 
    clip = clip,
    widths = widths,
    heights = heights,
    respect = respect
  ), class = "grobGrid")
}

print.grobGrid <- function(x, ...) {
  grid.show.layout(gridLayout(x))
}

dim.grobGrid <- function(x) {
  dim(x$grobs)
}

grobCol <- function(name, grobs = NULL, heights, width = unit(1, "null"), clip = TRUE, default.units = "lines") {
  grobGrid(name, grobs, width, heights, clip, default.units)
}

grobRow <- function(name, grobs = NULL, widths, height = unit(1, "null"), clip = TRUE, default.units = "lines") {
  grobGrid(name, grobs, widths, height, clip, default.units)
}

gridLayout <- function(grid) {
  grid.layout(
    nrow = nrow(grid), ncol = ncol(grid),
    widths = grid$widths, heights = grid$heights, 
    respect = grid$respect
  )
}

rbind.grobGrid <- function(...) {
  all <- function(var) llply(grids, "[[", var)

  grids <- list(...)
  widths <- do.call("rbind", (llply(all("widths"), as.list)))
  
  structure(list(
    names =   do.call("rbind", all("names")),
    grobs =   do.call("rbind", all("grobs")),
    clip =    do.call("rbind", all("clip")),
    widths =  do.call("unit.c", alply(widths, 2, splat(max2))),
    heights = do.call("unit.c", all("heights")),
    respect = do.call("any", all("respect"))
  ), class = "grobGrid") 
}

max2 <- function(...) {
  units <- list(...)
  
  nulls <- laply(units, function(x) identical(attr(x, "unit"), "null"))
  
  if (all(nulls)) {
    null_length <- max(laply(units, as.numeric))    
    unit(null_length, "null")
  } else {
    to_cm <- function(x) as.numeric(convertX(x, unitTo = "cm"))
    absolute_length <- max(laply(units[!nulls], to_cm))
    unit(absolute_length, "cm")
  }  
}

as.list.unit <- function(x, ...) {
  l <- vector("list", length(x))
  for(i in seq_along(x)) l[[i]] <- x[i]
  l
}
interleave.unit <- function(...) {
  do.call("unit.c", do.call("interleave", llply(list(...), as.list)))
}
rweave.grobGrid <- function(...) {
  grids <- list(...)
  all <- function(var) llply(grids, "[[", var)
  widths <- do.call("rbind", (llply(all("widths"), as.list)))

  structure(list(
    names =   rweave(all("names")),
    grobs =   rweave(all("grobs")),
    clip =    rweave(all("clip")),
    heights =  interleave(all("heights")),
    widths =  do.call("unit.c", alply(widths, 2, splat(max2))),
    respect = do.call("any", all("respect"))
  ), class = "grobGrid") 
  
}

cbind.grobGrid <- function(...) {
  all <- function(var) llply(grids, "[[", var)

  grids <- list(...)
  heights <- do.call("rbind", (llply(all("heights"), as.list)))

  structure(list(
    names =   do.call("cbind", all("names")),
    grobs =   do.call("cbind", all("grobs")),
    clip =    do.call("cbind", all("clip")),
    widths =  do.call("unit.c", all("widths")),
    heights = do.call("unit.c", alply(heights, 2, splat(max2))),
    respect = do.call("any", all("respect"))
  ), class = "grobGrid") 
}

cweave.grobGrid <- function(...) {
  grids <- list(...)
  all <- function(var) llply(grids, "[[", var)
  
  heights <- do.call("rbind", (llply(all("heights"), as.list)))
  
  structure(list(
    names =   cweave(all("names")),
    grobs =   cweave(all("grobs")),
    clip =    cweave(all("clip")),
    widths =  interleave(all("widths")),
    heights = do.call("unit.c", alply(heights, 2, splat(max2))),
    respect = do.call("any", all("respect"))
  ), class = "grobGrid") 
}

spacer <- function(nrow = 1, ncol = 1, width = 0, height = 0, default.units = "lines") {
  grobGrid("spacer", nrow = nrow, ncol = ncol, width = width, height = height, default.units = default.units)
}

# axis_grobs <- list(textGrob("axis 1"), textGrob("axis 2"))
# axis_v <- grobCol("axis_v", axis_grobs, unit(c(1, 1), "null"), 2)
# axis_h <- grobRow("axis_h", axis_grobs, unit(c(1, 1), "null"), 2)
# 
# panel_grobs <- list(textGrob("panel 1"), textGrob("panel 2"),
#   textGrob("panel 3"), textGrob("panel 4"))
# panels <- grobGrid("panel", panel_grobs, width = c(1,1), heights = c(1, 1), 
#   default.units = "null")
# plot <- rbind(
#   cbind(axis_v, panels),
#   cbind(spacer(), axis_h)
# )                         

viewports.grobGrid <- function(grid, name = "layout") {
  layout <- gridLayout(grid)
  layout_vp <- viewport(layout = layout, name = name)
  
  vp <- function(x, y) {
    viewport(
      name = paste(grid$names[x, y], x, y, sep = "-"), 
      layout.pos.row = x, 
      layout.pos.col = y, 
      clip = grid$clip[x, y]
    )
  }
  pos <- expand.grid(x = seq_len(nrow(grid)), y = seq_len(ncol(grid)))
  children_vp <- do.call("vpList", mlply(pos, vp))
  
  vpTree(layout_vp, children_vp)
}
grobs.grobGrid <- function(grid) {
  names <- paste(grid$names, row(grid$names), col(grid$names), sep="-")
  
  llply(seq_along(names), function(i) {
    editGrob(grid$grobs[[i]], vp = vpPath("layout", names[i]), name = names[i])
  })
}

gTree.grobGrid <- function(grid, name = "layout") {
  vp <- viewports.grobGrid(grid, name)
  grobs <- grobs.grobGrid(grid)
  
  gTree(
    children = do.call("gList", grobs), 
    childrenvp = vp,
    name = name
  )
}

grid.draw.grobGrid <- function(x, recording) {
  grid.newpage()
  grid.draw(gTree.grobGrid(x))
}

rowHeights <- function(mat) {
  do.call("unit.c", alply(mat, 1, splat(max2)))  
}

colWidths <- function(mat) {
  col_widths <- alply(mat, 2, function(x) llply(x, grobWidth))
  do.call("unit.c", llply(col_widths, splat(max2)))  
}

rep.unit2 <- function (x, ...) {
  if (length(x) == 0) 
      return(x)
  values <- rep(unclass(x), ...)

  if(length(values) == 0) return(NULL)
  units <- attr(x, "unit")
  data <- grid:::recycle.data(attr(x, "data"), TRUE, length(values), units)

  unit <- unit(values, units, data = data)
  unit
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/grob-null.r"
# Zero grob
# The zero grob draws nothing and has zero size.
# 
# @alias widthDetails.zeroGrob
# @alias heightDetails.zeroGrob
# @alias grobWidth.zeroGrob
# @alias grobHeight.zeroGrob
# @alias drawDetails.zeroGrob
# @alias is.zero
# @keyword internal
zeroGrob <- function() .zeroGrob

.zeroGrob <- grob(cl = "zeroGrob", name = "NULL")
widthDetails.zeroGrob <-
heightDetails.zeroGrob <- 
grobWidth.zeroGrob <- 
grobHeight.zeroGrob <- function(x) unit(0, "cm")

drawDetails.zeroGrob <- function(x, recording) {}

is.zero <- function(x) inherits(x, "zeroGrob")
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/guides-axis.r"
# Grob axis
# Grob for axes
# 
# @arguments position of ticks
# @arguments labels at ticks
# @arguments position of axis (top, bottom, left or right)
# @arguments range of data values
# @keyword hplot 
# @keyword internal
guide_axis <- function(at, labels, position="right", theme) {
  position <- match.arg(position, c("top", "bottom", "right", "left"))
  
  at <- unit(at, "native")
  length <- theme$axis.ticks.length
  label_pos <- length + theme$axis.ticks.margin

  one <- unit(1, "npc")
  
  label_render <- switch(position,
    top = , bottom = "axis.text.x",
    left = , right = "axis.text.y"
  )

  label_x <- switch(position,
    top = , 
    bottom = at,
    right = label_pos,
    left = one - label_pos
  )
  label_y <- switch(position,
    top = label_pos, 
    bottom = one - label_pos,
    right = ,
    left = at,
  )
  
  if (is.list(labels)) {
    if (any(sapply(labels, is.language))) {
      labels <- do.call(expression, labels)
    } else {
      labels <- unlist(labels)    
    }
  }

  labels <- switch(position,
                   top = ,
                   bottom = theme_render(theme, label_render, labels, x = label_x),
                   right = ,
                   left =  theme_render(theme, label_render, labels, y = label_y))
  
  line <- switch(position,
    top =    theme_render(theme, "axis.line", 0, 0, 1, 0),
    bottom = theme_render(theme, "axis.line", 0, 1, 1, 1),
    right =  theme_render(theme, "axis.line", 0, 1, 0, 1),
    left =   theme_render(theme, "axis.line", 1, 0, 1, 1)
  )
  
  ticks <- switch(position,
    top =    theme_render(theme, "axis.ticks", at, 0, at, length),
    bottom = theme_render(theme, "axis.ticks", at, one - length, at, 1),
    right =  theme_render(theme, "axis.ticks", 0, at, length, at),
    left =   theme_render(theme, "axis.ticks", one - length, at, 1, at)
  )

  fg <- ggname("axis", switch(position,
                              top =, bottom = frameGrob(layout = grid.layout(nrow = 2, ncol = 1,
                                                          widths = one, heights = unit.c(label_pos, grobHeight(labels)))),
                              right =, left = frameGrob(layout = grid.layout(nrow = 1, ncol = 2,
                                                          widths = unit.c(grobWidth(labels), label_pos), heights = one))))


  if (!is.zero(labels)) {
    fg <- switch(position,
                 top = ,
                 bottom = placeGrob(fg, labels, row = 2, col = 1),
                 right = ,
                 left = placeGrob(fg, labels, row = 1, col = 1))
  }

  if (!is.zero(ticks)) {
    fg <- switch(position,
                 top = ,
                 bottom = placeGrob(fg, ticks, row = 1, col = 1),
                 right = ,
                 left = placeGrob(fg, ticks, row = 1, col = 2))
  }

  absoluteGrob(
    gList(line, fg),
    width = grobWidth(fg),
    height = grobHeight(fg)
  )
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/guides-grid.r"
# Draw a grid
# Produce a grob to be used as a background in panels
# 
# @arguments theme to use to draw elements
# @arguments minor breaks in x axis
# @arguments major breaks in x axis
# @arguments minor breaks in y axis
# @arguments major breaks in y axis
# @keyword internal
guide_grid <- function(theme, x.minor, x.major, y.minor, y.major) {
  ggname("grill", grobTree(
    theme_render(theme, "panel.background"),
    
    theme_render(
      theme, "panel.grid.minor", name = "y",
      x = rep(0:1, length(y.minor)), y = rep(y.minor, each=2), 
      id.lengths = rep(2, length(y.minor))
    ),
    theme_render(
      theme, "panel.grid.minor", name = "x", 
      x = rep(x.minor, each=2), y = rep(0:1, length(x.minor)),
      id.lengths = rep(2, length(x.minor))
    ),

    theme_render(
      theme, "panel.grid.major", name = "y",
      x = rep(0:1, length(y.major)), y = rep(y.major, each=2), 
      id.lengths = rep(2, length(y.major))
    ),
    theme_render(
      theme, "panel.grid.major", name = "x", 
      x = rep(x.major, each=2), y = rep(0:1, length(x.major)), 
      id.lengths = rep(2, length(x.major))
    )
  ))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/guides-legend.r"
# Legends
# Create and arrange legends for all scales.
# 
# This function gathers together all of the legends produced by 
# the scales that make up the plot and organises them into a 
# \code{\link[grid]{frameGrob}}.  
# 
# If there are no legends to create, this function will return \code{NULL}
# 
# @arguments scales object
# @arguments direction of scales, vertical by default
# @keyword hplot 
# @value frameGrob, or NULL if no legends
# @keyword internal
#X theme_update(legend.background = theme_rect(size = 0.2))
#X mtcars$long <- factor(sample(3, nrow(mtcars), TRUE),
#X   labels = c("this is very long label", "this is very long label2", "this is\nvery long\nlabel3"))
#X mtcars$short_elements_with_long_title <- factor(sample(2, nrow(mtcars), TRUE), labels = c("s1", "s2"))
#X
#X # with short title and long key/values
#X p <- qplot(mpg, wt, data = mtcars, colour = factor(cyl), shape = long)
#X p
#X p + opts(legend.direction = "horizontal", legend.position = "bottom")
#X p + opts(legend.direction = "horizontal", legend.position = "bottom", legend.box = "vertical")
#X 
#X # with long title and short key/values
#X p <- qplot(mpg, wt, data = mtcars, colour = factor(cyl), shape = short_elements_with_long_title)
#X p
#X p + opts(legend.direction = "horizontal", legend.position = "bottom") # to be fixed
#X p + opts(legend.direction = "horizontal", legend.position = "bottom", legend.box = "vertical")
#X theme_set(theme_grey())
guide_legends_box <- function(scales, layers, default_mapping, horizontal = FALSE, theme) {

  # override alignment of legends box if theme$legend.box is specified
  if (!is.na(theme$legend.box)) {
    horizontal <- 1 == charmatch(theme$legend.box, c("horizontal","vertical"))
  }
  
  legs <- guide_legends(scales, layers, default_mapping, theme=theme)
  
  n <- length(legs)
  if (n == 0) return(zeroGrob())
  
  if (!horizontal) {
    width <-   do.call("max", lapply(legs, widthDetails))
    heights <- do.call("unit.c", lapply(legs, function(x) heightDetails(x) * 1.1))
    fg <- frameGrob(grid.layout(nrow=n, 1, widths=width, heights=heights, just="centre"), name="legends")
    for(i in 1:n) {
      fg <- placeGrob(fg, legs[[i]], row=i)
    }
  } else {
    height <- do.call("sum", lapply(legs, heightDetails))
    widths <- do.call("unit.c", lapply(legs, function(x) widthDetails(x) * 1.1))
    fg <- frameGrob(grid.layout(ncol=n, 1, widths=widths, heights=height, just="centre"), name="legends")
    for(i in 1:n) {
      fg <- placeGrob(fg, legs[[i]], col=i)
    }
  }
  fg
}

# Build all legend grob
# Build legends, merging where possible
# 
# @arguments list of legend descriptions
# @arguments list description usage of aesthetics in geoms
# @keyword internal
# @value A list of grobs
# @alias build_legend
# @alias build_legend_data
#X theme_update(legend.background = theme_rect(size = 0.2))
#X qplot(mpg, wt, data = mtcars)
#X qplot(mpg, wt, data = mtcars, colour = cyl)
#X
#X # Legend with should expand to fit name
#X qplot(mpg, wt, data = mtcars, colour = factor(cyl))
#X 
#X qplot(mpg, wt, data = mtcars, colour = cyl) +
#X  opts(legend.position = c(0.5, 0.5), 
#X       legend.background = theme_rect(fill = "white", col = NA))
#X
#X mtcars$cyl2 <- factor(mtcars$cyl, 
#X   labels = c("a", "loooooooooooong", "two\nlines"))
#X qplot(mpg, wt, data = mtcars, colour = cyl2)
#X theme_set(theme_grey())
guide_legends <- function(scales, layers, default_mapping, theme) {
  legend <- scales$legend_desc(theme)
  if (length(legend$titles) == 0) return()
  
  hashes <- unique(legend$hash)
  lapply(hashes, function(hash) {
    keys <- legend$keys[legend$hash == hash]
    title <- legend$title[legend$hash == hash][[1]]
    
    if (length(keys) > 1) { 
      # Multiple scales for this legend      
      keys <- merge_recurse(keys, by = ".label")
    } else {
      keys <- keys[[1]]
    }
    build_legend(title, keys, layers, default_mapping, theme)
  })
}

build_legend <- function(name, mapping, layers, default_mapping, theme) {
  legend_data <- llply(layers, build_legend_data, mapping, default_mapping)

  # Determine key width and height
  if (is.na(theme$legend.key.width)) {
    theme$legend.key.width <- theme$legend.key.size
  }
  if (is.na(theme$legend.key.height)) {
    theme$legend.key.height <- theme$legend.key.size
  }

  # Determine the direction of the elements of legend.
  if (theme$legend.direction == "horizontal") {
    direction <- "horizontal"
  } else {
    direction <- "vertical"
  }

  # Calculate sizes for keys - mainly for v. large points and lines
  size_mat <- do.call("cbind", llply(legend_data, "[[", "size"))
  if (is.null(size_mat)) {
    key_sizes <- rep(0, nrow(mapping))
  } else {
    key_sizes <- apply(size_mat, 1, max)
  }

  # hjust for title of legend
  # if direction is vertical, then title is left-aligned
  # if direction is horizontal, then title is centre-aligned
  # if legend.title.align is specified, then title is alinged using the value
  if (is.na(theme$legend.title.align)) {
    if (direction == "vertical") {
      title <- theme_render(
        theme, "legend.title",
        name, x = 0, y = 0.5
      )
    } else if (direction == "horizontal") {
      title <- theme_render(
        theme, "legend.title",
        name, hjust = 0.5, x = 0.5, y = 0.5
      )
    }
  } else {
    title <- theme_render(
      theme, "legend.title",
      name, hjust = theme$legend.title.align, x = theme$legend.title.align, y = 0.5
    )
  }

  # Compute heights and widths of legend table
  nkeys <- nrow(mapping)
  hgap <- vgap <- unit(0.3, "lines")

  if (is.na(theme$legend.text.align)) {
    numeric_labels <- all(sapply(mapping$.label, is.language)) || suppressWarnings(all(!is.na(sapply(mapping$.label, "as.numeric"))))
    hpos <- numeric_labels * 1    
  } else {
    hpos <- theme$legend.text.align
  }

  labels <- lapply(mapping$.label, function(label) {
    theme_render(theme, "legend.text", label, hjust = hpos, x = hpos, y = 0.5)
  })

  if (direction == "vertical") {
    label_width <- do.call("max", lapply(labels, grobWidth))
    label_width <- convertWidth(label_width, "cm")
    label_heights <- do.call("unit.c", lapply(labels, grobHeight))
    label_heights <- convertHeight(label_heights, "cm")

    width <- max(unlist(llply(legend_data, "[[", "size")), 0)
    key_width <- max(theme$legend.key.width, unit(width, "mm"))

    widths <- unit.c(
                     hgap, key_width,
                     hgap, label_width,
                     max(
                         unit(1, "grobwidth", title) - key_width - label_width,
                         hgap
                         )
                     )
    widths <- convertWidth(widths, "cm")

    heights <- unit.c(
                      vgap, 
                      unit(1, "grobheight", title),
                      vgap, 
                      unit.pmax(
                                theme$legend.key.height, 
                                label_heights, 
                                unit(key_sizes, "mm")
                                ),
                      vgap
                      )  
    heights <- convertHeight(heights, "cm")

  } else if(direction == "horizontal") {
    label_width <- do.call("unit.c", lapply(labels, grobWidth))
    label_width <- convertWidth(label_width, "cm")
    label_heights <- do.call("max", lapply(labels, grobHeight))
    label_heights <- convertHeight(label_heights, "cm")

    height <- max(unlist(llply(legend_data, "[[", "size")), 0)
    key_heights <- max(theme$legend.key.height, unit(height, "mm"))

    key_width <- unit.pmax(theme$legend.key.width, unit(key_sizes, "mm"))
    # width of (key gap label gap) x nkeys
    kglg_width <- do.call("unit.c",lapply(1:length(key_width), function(i)unit.c(key_width[i], hgap, label_width[i], hgap)))
    widths <- unit.c(
                      max(
                          hgap,
                          (unit.c(unit(1, "grobwidth", title) - (sum(kglg_width) - hgap))) * 0.5
                          ),
                      kglg_width,
                      max(
                          hgap,
                          (unit.c(unit(1, "grobwidth", title) - (sum(kglg_width) - hgap))) * 0.5
                          )
                      )
    widths <- convertWidth(widths, "cm")

    heights <- unit.c(
                       vgap, 
                       unit(1, "grobheight", title),
                       vgap, 
                       max(
                           theme$legend.key.height,
                           label_heights, 
                           key_heights
                           ),
                       vgap
                       )  
    heights <- convertHeight(heights, "cm")

  }

  # horizontally center is pretty when direction is horizontal
  if (direction == "vertical") {
    hjust <- "left"
  } else if (direction == "horizontal") {
    hjust <- "centre"
  }
 
  # Layout the legend table
  legend.layout <- grid.layout(
    length(heights), length(widths), 
    widths = widths, heights = heights, 
    just = c(hjust, "centre")
  )

  fg <- ggname("legend", frameGrob(layout = legend.layout))
  fg <- placeGrob(fg, theme_render(theme, "legend.background"))

  fg <- placeGrob(fg, title, col = 2:(length(widths)-1), row = 2)
  for (i in 1:nkeys) {

    if (direction == "vertical") {
      fg <- placeGrob(fg, theme_render(theme, "legend.key"), col = 2, row = i+3)
    } else if (direction == "horizontal") {
      fg <- placeGrob(fg, theme_render(theme, "legend.key"), col = 1+(i*4)-3, row = 4)
    }

    for(j in seq_along(layers)) {
      if (!is.null(legend_data[[j]])) {
        legend_geom <- Geom$find(layers[[j]]$geom$guide_geom())
        key <- legend_geom$draw_legend(legend_data[[j]][i, ],
           c(layers[[j]]$geom_params, layers[[j]]$stat_params))
        if (direction == "vertical") {
          fg <- placeGrob(fg, ggname("key", key), col = 2, row = i+3)
        } else if (direction == "horizontal") {
          fg <- placeGrob(fg, ggname("key", key), col = 1+(i*4)-3, row = 4)
        }
      }
    }
    label <- theme_render(
      theme, "legend.text", 
      mapping$.label[[i]], hjust = hpos,
      x = hpos, y = 0.5
    )
    if (direction == "vertical") {
      fg <- placeGrob(fg, label, col = 4, row = i+3)
    } else if (direction == "horizontal") {
      fg <- placeGrob(fg, label, col = 1+(i*4)-1, row = 4)
    }
  }
  fg
}

build_legend_data <- function(layer, mapping, default_mapping) {
  all <- names(c(layer$mapping, default_mapping, layer$stat$default_aes()))
  geom <- c(layer$geom$required_aes, names(layer$geom$default_aes()))
 
  matched <- intersect(intersect(all, geom), names(mapping))
  matched <- setdiff(matched, names(layer$geom_params))

  if (length(matched) > 0) {
    # This layer contributes to the legend
    if (is.na(layer$legend) || layer$legend) {
      # Default is to include it 
      layer$use_defaults(mapping[matched])
    } else {
      NULL
    }
  } else {
    # This layer does not contribute to the legend
    if (is.na(layer$legend) || !layer$legend) {
      # Default is to exclude it
      NULL
    } else {
      layer$use_defaults(NULL)[rep(1, nrow(mapping)), ]
    }
  }
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/labels.r"
# Update axis/legend labels
# Change the scale names of an existing plot
# 
# @arguments plot
# @arguments named list of new labels
# @keyword internal
#X p <- qplot(mpg, wt, data = mtcars)
#X update_labels(p, list(x = "New x"))
#X update_labels(p, list(x = expression(x / y ^ 2)))
#X update_labels(p, list(x = "New x", y = "New Y"))
#X update_labels(p, list(colour = "Fail silently"))
update_labels <- function(p, labels) {
  p <- plot_clone(p)
  p + opts(labels = labels)
}

# Change axis labels and legend titles
# This is a convenience function that saves some typing when modifying the axis labels or legend titles
# 
# @arguments a list of new names in the form aesthetic = "new name"
# @alias xlab
# @alias ylab
#X p <- qplot(mpg, wt, data = mtcars)
#X p + labs(x = "New x label")
#X p + xlab("New x label")
#X p + ylab("New y label")
#X
#X # This should work indepdendently of other functions that modify the 
#X # the scale names
#X p + ylab("New y label") + ylim(2, 4)
#X p + ylim(2, 4) + ylab("New y label")
#X
#X # The labs function also modifies legend labels
#X p <- qplot(mpg, wt, data = mtcars, colour = cyl)
#X p + labs(colour = "Cylinders")
#X
#X # Can also pass in a list, if that is more convenient
#X p + labs(list(x = "X", y = "Y")) 
labs <- function(...) {
  args <- list(...)
  if (is.list(args[[1]])) args <- args[[1]]
  structure(args, class = "labels")
}

xlab <- function(label) {
  labs(x = label)
}
ylab <- function(label) {
  labs(y = label)
}

# Convert aesthetic mapping into text labels
# This is used by ggplot and + to ensure consistent label formatting
# 
# @keyword internal
make_labels <- function(mapping) {
  remove_dots <- function(x) {
    gsub("\\.\\.([a-zA-z._]+)\\.\\.", "\\1", x)
  }
  
  lapply(mapping, function(x) remove_dots(deparse(x)))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/layer.r"
  # Create a new layer
  # Layer objects store the layer of an object.
  # 
  # They have the following attributes:
  # 
  #  * data
  #  * geom + parameters
  #  * statistic + parameters
  #  * position + parameters
  #  * aesthetic mapping
  # 
  # Can think about grob creation as a series of data frame transformations.
Layer <- proto(expr = {  
  geom <- NULL
  geom_params <- NULL
  stat <- NULL
  stat_params <- NULL
  data <- NULL
  mapping <- NULL
  position <- NULL
  params <- NULL
  inherit.aes <- FALSE
  
  new <- function (., geom=NULL, geom_params=NULL, stat=NULL, stat_params=NULL, data=NULL, mapping=NULL, position=NULL, params=NULL, ..., inherit.aes = TRUE, legend = NA, subset = NULL) {
    
    if (is.null(geom) && is.null(stat)) stop("Need at least one of stat and geom")
    
    data <- fortify(data)
    if (!is.null(mapping) && !inherits(mapping, "uneval")) stop("Mapping should be a list of unevaluated mappings created by aes or aes_string")
    
    if (is.character(geom)) geom <- Geom$find(geom)
    if (is.character(stat)) stat <- Stat$find(stat)
    if (is.character(position)) position <- Position$find(position)$new()
    
    if (is.null(geom)) geom <- stat$default_geom()
    if (is.null(stat)) stat <- geom$default_stat()
    if (is.null(position)) position <- geom$default_pos()$new()

    match.params <- function(possible, params) {
      if ("..." %in% names(possible)) {
        params
      } else {
        params[match(names(possible), names(params), nomatch=0)]
      }
    }

    if (is.null(geom_params) && is.null(stat_params)) {
      params <- c(params, list(...))
      params <- rename_aes(params) # Rename American to British spellings etc
      
      geom_params <- match.params(geom$parameters(), params)
      stat_params <- match.params(stat$parameters(), params)
      stat_params <- stat_params[setdiff(names(stat_params),
        names(geom_params))]
    } else {      
      geom_params <- rename_aes(geom_params)
    }
    
    if (!is.null(geom_params)) {
      set_aesthetics <- geom_params[intersect(names(geom_params), .all_aesthetics)]
      # Check that all set aesthetics have length 1
      if (length(set_aesthetics) > 0) {
        lengths <- sapply(set_aesthetics, length)
        if (any(lengths > 1)) {
          stop("When _setting_ aesthetics, they may only take one value. ", 
            "Problems: ",
            paste(names(set_aesthetics)[lengths > 1], collapse = ","), 
            call. = FALSE)
        }
        
      }
    }
    
    proto(., 
      geom=geom, geom_params=geom_params, 
      stat=stat, stat_params=stat_params, 
      data=data, mapping=mapping, subset=subset,
      position=position,
      inherit.aes = inherit.aes,
      legend = legend
    )
  }
  
  clone <- function(.) as.proto(.$as.list(all.names=TRUE))
  
  use_defaults <- function(., data) {
    df <- aesdefaults(data, .$geom$default_aes(), NULL)
    
    # Override mappings with atomic parameters
    gp <- intersect(c(names(df), .$geom$required_aes), names(.$geom_params))
    gp <- gp[unlist(lapply(.$geom_params[gp], is.atomic))]

    df[gp] <- .$geom_params[gp]
    df
  }
  
  aesthetics_used <- function(., plot_aesthetics) {
    aes <- defaults(.$mapping, plot_aesthetics)
    aes <- defaults(.$stat$default_aes(), aes)
    aesthetics <- names(compact(aes))
    aesthetics <- intersect(aesthetics, names(.$geom$default_aes()))
    parameters <- names(.$geom_params)
    setdiff(aesthetics, parameters)
  }
  
  pprint <- function(.) {
    if (is.null(.$geom)) {
      cat("Empty layer\n")
      return(invisible());
    }
    if (!is.null(.$mapping)) {
      cat("mapping:", clist(.$mapping), "\n")      
    }
    .$geom$print(newline=FALSE)
    cat(clist(.$geom_params), "\n")
    .$stat$print(newline=FALSE)
    cat(clist(.$stat_params), "\n")
    .$position$print()
  }
  
  
  # Produce data.frame of evaluated aesthetics
  # Depending on the construction of the layer, we may need
  # to stitch together a data frame using the defaults from plot\$mapping 
  # and overrides for a given geom.
  #
  make_aesthetics <- function(., plot) {
    data <- if(empty(.$data)) plot$data else .$data

    # Apply subsetting, if used
    if (!is.null(.$subset)) {
      include <- data.frame(eval.quoted(.$subset, data))
      data <- data[rowSums(include) == ncol(include), ]
    }
    
    # For certain geoms, it is useful to be able to ignore the default
    # aesthetics and only use those set in the layer
    if (.$inherit.aes) {
      aesthetics <- compact(defaults(.$mapping, plot$mapping))
    } else {
      aesthetics <- .$mapping
    }
    
    # Override grouping if specified in layer
    if (!is.null(.$geom_params$group)) {
      aesthetics["group"] <- .$geom_params$group
    } 
    
    # Drop aesthetics that are set manually
    aesthetics <- aesthetics[setdiff(names(aesthetics), names(.$geom_params))]
    plot$scales$add_defaults(data, aesthetics, plot$plot_env)
    
    # Evaluate aesthetics in the context of their data frame
    eval.each <- function(dots) 
      compact(lapply(dots, function(x.) eval(x., data, plot$plot_env)))

    aesthetics <- aesthetics[!is_calculated_aes(aesthetics)]
    evaled <- eval.each(aesthetics)
    if (length(evaled) == 0) return(data.frame())

    evaled <- evaled[sapply(evaled, is.atomic)]
    df <- data.frame(evaled)

    # Add Conditioning variables needed for facets
    cond <- plot$facet$conditionals()
    facet_vars <- data[, intersect(names(data), cond), drop=FALSE]
    if (!empty(facet_vars)) {
      df <- cbind(df, facet_vars)  
    }

    if (empty(plot$data)) return(df)
    facet_vars <- unique(plot$data[, setdiff(cond, names(df)), drop=FALSE])
    
    if (empty(data)) return(facet_vars)
    expand.grid.df(df, facet_vars, unique = FALSE)
  }

  calc_statistics <- function(., data, scales) {
    gg_apply(data, function(x) .$calc_statistic(x, scales))  
  }
  
  calc_statistic <- function(., data, scales) {
    if (empty(data)) return(data.frame())
    
    check_required_aesthetics(.$stat$required_aes, 
      c(names(data), names(.$stat_params)), 
      paste("stat_", .$stat$objname, sep=""))

    res <- do.call(.$stat$calculate_groups, c(
      list(data=as.name("data"), scales=as.name("scales")), 
      .$stat_params)
    )
    if (is.null(res)) return(data.frame())
    
    res
    
  }

  # Map new aesthetic names
  # After the statistic transformation has been applied, a second round
  # of aesthetic mappings occur.  This allows the mapping of variables 
  # created by the statistic, for example, height in a histogram, levels
  # on a contour plot.
  # 
  # This also takes care of applying any scale transformations that might
  # be necessary
  map_statistics <- function(., data, plot) {
    gg_apply(data, function(x) .$map_statistic(x, plot=plot))
  }
  
  map_statistic <- function(., data, plot) {
    if (empty(data)) return(data.frame())

    # Assemble aesthetics from layer, plot and stat mappings
    aesthetics <- .$mapping
    if (.$inherit.aes) {
      aesthetics <- defaults(aesthetics, plot$mapping)
    }
    aesthetics <- defaults(aesthetics, .$stat$default_aes())
    aesthetics <- compact(aesthetics)
  
    new <- strip_dots(aesthetics[is_calculated_aes(aesthetics)])
    if (length(new) == 0) return(data)

    # Add map stat output to aesthetics
    stat_data <- as.data.frame(lapply(new, eval, data, baseenv()))
    names(stat_data) <- names(new)
    
    # Add any new scales, if needed
    plot$scales$add_defaults(data, new, plot$plot_env)
    # Transform the values, if the scale say it's ok 
    # (see stat_spoke for one exception)
    if (.$stat$retransform) {
      stat_data <- plot$scales$transform_df(stat_data)
    }
    
    cunion(stat_data, data)
  }

  reparameterise <- function(., data) {
    gg_apply(data, function(df) {
      if (empty(df)) return(data.frame())

      .$geom$reparameterise(df, .$geom_params) 
    })
  }

  adjust_position <- function(., data, scales) {
    gg_apply(data, function(df) {
      if (empty(df)) return(data.frame())
      if (is.null(df$group)) df$group <- 1

      # If ordering is set, modify group variable according to this order
      if (!is.null(df$order)) {
        df$group <- id(list(df$group, df$order))
        df$order <- NULL
      }

      df <- df[order(df$group), ]
      .$position$adjust(df, scales)
    })
  }
  
  make_grob <- function(., data, scales, cs) {
    if (empty(data)) return(zeroGrob())
    
    data <- .$use_defaults(data)
    
    check_required_aesthetics(.$geom$required_aes,
      c(names(data), names(.$geom_params)), 
      paste("geom_", .$geom$objname, sep=""))
    
    
    do.call(.$geom$draw_groups, c(
      data = list(as.name("data")), 
      scales = list(as.name("scales")), 
      coordinates = list(as.name("cs")), 
      .$geom_params
    ))
  }

  class <- function(.) "layer"

  # Methods that probably belong elsewhere ---------------------------------
  
  # Stamp data.frame into list of matrices
  
  scales_transform <- function(., data, scales) {
    gg_apply(data, scales$transform_df)
  }

  # Train scale for this layer
  scales_train <- function(., data, scales) {
    gg_apply(data, scales$train_df)
  }

  
  # Map data using scales.
  scales_map <- function(., data, scale) {
    gg_apply(data, function(x) scale$map_df(x))
  }  
})

# Apply function to plot data components
# Convenience apply function for facets data structure
# 
# @keyword internal
gg_apply <- function(gg, f, ...) {
  apply(gg, c(1,2), function(data) {
    f(data[[1]], ...)
  })
}
layer <- Layer$new



# Is calculated aesthetic?
# Determine if aesthetic is calculated from the statistic
# 
# @keyword internal
is_calculated_aes <- function(aesthetics) {
  match <- "\\.\\.([a-zA-z._]+)\\.\\."
  stats <- rep(F, length(aesthetics))
  stats[grep(match, sapply(aesthetics, deparse))] <- TRUE
  stats
}

# Strip dots
# Strip dots from expressions that represent mappings of aesthetics to output from statistics
# 
# @keyword internal
strip_dots <- function(aesthetics) {
  match <- "\\.\\.([a-zA-z._]+)\\.\\."
  strings <- lapply(aesthetics, deparse)
  strings <- lapply(strings, gsub, pattern = match, replacement = "\\1")
  lapply(strings, function(x) parse(text = x)[[1]]) 
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/matrix.r"
# Code to create a scatterplot matrix (experimental)
# Crude experimental scatterplot matrix
# 
# @arguments data frame
# @arguments any additional aesthetic mappings (do not use x and y)
# @arguments default point colour
# @keyword hplot
#X plotmatrix(mtcars[, 1:3])
#X plotmatrix(mtcars[, 1:3]) + geom_smooth(method="lm")
plotmatrix <- function(data, mapping=aes(), colour="black") {
  # data <- rescaler(data, "range")
  grid <- expand.grid(x=1:ncol(data), y=1:ncol(data))
  grid <- subset(grid, x != y)

  all <- do.call("rbind", lapply(1:nrow(grid), function(i) {
    xcol <- grid[i, "x"]
    ycol <- grid[i, "y"]

    data.frame(
      xvar = names(data)[ycol], 
      yvar = names(data)[xcol],
      x = data[, xcol], y = data[, ycol], data
    )
  }))
  all$xvar <- factor(all$xvar, levels=names(data))
  all$yvar <- factor(all$yvar, levels=names(data))

  densities <- do.call("rbind", lapply(1:ncol(data), function(i) {
    data.frame(
      xvar = names(data)[i], 
      yvar = names(data)[i],
      x = data[, i]
    )
  }))
  mapping <- defaults(mapping, aes_string(x="x", y="y"))
  class(mapping) <- "uneval"

  ggplot(all, mapping) + facet_grid(xvar ~ yvar, scales = "free") +
    geom_point(colour = colour, na.rm = TRUE) +
    stat_density(
      aes(x = x, y = ..scaled.. * diff(range(x)) + min(x)),
      data = densities, position ="identity", colour = "grey20", geom = "line"
    )
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/plot-build.r"
# Build ggplot for rendering
# This function is the powerhouse that converts the plot specification into something that's ready to be rendered on screen
# 
# @keyword internal
ggplot_build <- function(plot) {
  if (length(plot$layers) == 0) stop("No layers in plot", call.=FALSE)
  
  plot <- plot_clone(plot)
  layers <- plot$layers
  scales <- plot$scales
  facet <- plot$facet
  cs <- plot$coordinates
  # Apply function to layer and matching data
  dlapply <- function(f) mlply(cbind(d = data, p = layers), f)

  # Evaluate aesthetics
  data <- lapply(layers, function(x) x$make_aesthetics(plot))
  
  # Facet
  facet$initialise(data)
  data <- facet$stamp_data(data)
  
  # Transform all scales
  data <- dlapply(function(d, p) p$scales_transform(d, scales))
  
  # Map and train positions so that statistics have access to ranges
  # and all positions are numeric
  facet$position_train(data, scales)
  data <- facet$position_map(data, scales)
  
  # Apply and map statistics, then reparameterise geoms that need it
  data <- facet$calc_statistics(data, layers)
  data <- dlapply(function(d, p) p$map_statistics(d, plot)) 
  data <- dlapply(function(d, p) p$reparameterise(d))

  # Adjust position
  data <- dlapply(function(d, p) p$adjust_position(d, scales))
  
  npscales <- scales$non_position_scales()
  
  # Train and map, for final time
  if (npscales$n() > 0) {
    dlapply(function(d, p) p$scales_train(d, npscales))
    data <- dlapply(function(d, p) p$scales_map(d, npscales))
  }
  facet$position_train(data, scales)
  data <- facet$position_map(data, scales)    

  # Produce grobs
  grobs <- facet$make_grobs(data, layers, cs)
  
  grobs3d <- array(unlist(grobs, recursive=FALSE), c(dim(data[[1]]), length(data)))
  panels <- aaply(grobs3d, 1:2, splat(grobTree), .drop = FALSE)
  
  list(
    data = data,
    plot = plot,
    scales = npscales,
    cs = cs,
    panels = panels,
    facet = facet
  )
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/plot-construction.r"
# Plot construction
# The elements of a ggplot plot are combined together with addition.
# 
# \itemize{
#   \item \code{data.frame}: replace default data.frame (must use \code{\%+\%})
#   \item \code{uneval}: replace default aesthetics
#   \item \code{layer}: add new layer
#   \item \code{options}: update plot options
#   \item \code{scale}: replace default scale
#   \item \code{coord}: override default coordinate system
#   \item \code{facet}: override default coordinate faceting
# }
#
# @arguments plot object
# @arguments object to add
# @seealso \code{\link{set_last_plot}}, \code{\link{ggplot}}
# @keyword internal
# @alias \%+\%
"+.ggplot" <- function(p, object) {
  if (is.null(object)) return(p)

  p <- plot_clone(p)
  if (is.data.frame(object)) {
    p$data <- object
  } else if (inherits(object, "options")) {
    object$labels <- defaults(object$labels, p$options$labels)
    p$options <- defaults(object, p$options)
  } else if(inherits(object, "labels")) {
      p <- update_labels(p, object)
  } else if(inherits(object, "uneval")) {
      p$mapping <- defaults(object, p$mapping)
      
      labels <- lapply(object, deparse)
      names(labels) <- names(object)
      p <- update_labels(p, labels)
  } else if(is.list(object)) {
    for (o in object) {
      p <- p + o
    }
  } else if(is.proto(object)) {
    p <- switch(object$class(),
      layer  = {
        p$layers <- append(p$layers, object)
        
        # Add any new labels
        mapping <- make_labels(object$mapping)
        default <- make_labels(object$stat$default_aes())
        
        new_labels <- defaults(mapping, default)
        p$options$labels <- defaults(p$options$labels, new_labels)
        p
      },
      coord = {
        p$coordinates <- object
        p
      },
      facet = {
        p$facet <- object
        p
      },
      scale = {
        p$scales$add(object)
        p
      }
    )
  } else {
    stop("Don't know how to add ", deparse(substitute(object)), " to a plot",
      call. = FALSE)
  }
  set_last_plot(p)
  p
}
"%+%" <- `+.ggplot`
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/plot-last.r"
.plot_store <- function() {
  .last_plot <- NULL
  
  list(
    get = function() .last_plot, 
    set = function(value) .last_plot <<- value
  )
}
.store <- .plot_store()

# Set last plot
# Set last plot created or modified
# 
# @arguments plot to store
# @keyword internal
set_last_plot <- function(value) .store$set(value)


# Retrieve last plot modified/created.
# Whenever a plot is created or modified, it is recorded.
# 
# @seealso \code{\link{ggsave}}
# @keyword hplot
last_plot <- function() .store$get()
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/plot-render.r"
# ggplot plot
# Creates a complete ggplot grob.
#
# @arguments plot object
# @arguments should the plot be wrapped up inside the pretty accoutrements (labels, legends, etc)
# @keyword hplot
# @keyword internal
panelGrob <- function(plot, pieces = ggplot_build(plot)) {
  theme <- plot_theme(plot)

  grid <- pieces$facet$add_guides(plot$data, pieces$panels, pieces$cs, theme)
  gTree.grobGrid(grid)
}

# Pretty plot
# Build a plot with all the usual bits and pieces.
# 
# As well as the plotting area, a plot needs:
# 
# \itemize{
#  \item main title
#  \item x and y axis labels
#  \item space for legends (currently on the right hand side)
# }
# 
# These are stored as options in the plot object.
# 
# This function sets up the appropriate viewports and packs the
# various components in.  The viewport is set up so that each component
# will only take up the amount of space that it requires.  
# 
# @arguments plot
# @arguments plot grob
# @keyword internal
ggplotGrob <- function(plot, drop = plot$options$drop, keep = plot$options$keep, ...) {
  pieces <- ggplot_build(plot)
  
  panels <- panelGrob(plot, pieces)
  scales <- pieces$scales
  cs <- pieces$cs

  theme <- plot_theme(plot)
  margin <- list(
    top = theme$plot.margin[1], right = theme$plot.margin[2],
    bottom = theme$plot.margin[3], left = theme$plot.margin[4]
  )
  
  position <- theme$legend.position
  if (length(position) == 2) {
    coords <- position
    position <- "manual"
  }
  horiz <- any(c("top", "bottom") %in% position)
  vert <-  any(c("left", "right") %in% position)
  
  
  # Generate grobs -----------------------------------------------------------
  # each of these grobs has a vp set

  legend_box <- if (position != "none") {
    guide_legends_box(scales, plot$layers, plot$mapping, horiz, theme) 
  } else {
    zeroGrob()
  } 
  
  title <- theme_render(theme, "plot.title", plot$options$title)

  labels <- cs$labels(list(
    x = pieces$facet$xlabel(theme),
    y = pieces$facet$ylabel(theme))
  )
  xlabel <- theme_render(theme, "axis.title.x", labels$x)
  ylabel <- theme_render(theme, "axis.title.y", labels$y)

  grobs <- list(
    title = title, 
    xlabel = xlabel, ylabel = ylabel,
    panels = panels, legend_box = legend_box
  )
  if (!is.null(keep)) drop <- setdiff(names(grobs), keep)
  if (!is.null(drop)) grobs[drop] <- rep(list(zeroGrob()), length(drop))

  # Calculate sizes ----------------------------------------------------------
  if (is.null(legend_box)) position <- "none"
    
  ylab_width <- grobWidth(grobs$ylabel) + 
    if (is.zero(grobs$ylabel)) unit(0, "lines") else unit(0.5, "lines")
  legend_width <- grobWidth(grobs$legend_box) + unit(0.5, "lines")

  widths <- switch(position, 
    right =  unit.c(ylab_width, unit(1, "null"), legend_width),
    left =   unit.c(legend_width, ylab_width, unit(1, "null")), 
    top =    ,
    bottom = ,
    manual = ,
    none =   unit.c(ylab_width, unit(1, "null"))
  )
  widths <- unit.c(margin$left, widths, margin$right)

  legend_height <- grobHeight(grobs$legend_box)
  title_height <- grobHeight(grobs$title) + 
    if (is.null(plot$options$title)) unit(0, "lines") else unit(0.5, "lines")
  
  xlab_height <- grobHeight(grobs$xlabel) + 
    if (is.zero(grobs$xlabel)) unit(0, "lines") else unit(0.5, "lines")

  heights <- switch(position,
    top =    unit.c(
      title_height, legend_height, unit(1, "null"), xlab_height),
    bottom = unit.c(
      title_height, unit(1, "null"), xlab_height, legend_height),
    right =  ,
    left =   ,
    manual = ,
    none =   unit.c(title_height, unit(1, "null"), xlab_height)
  )
  heights <- unit.c(margin$top, heights, margin$bottom)
  
  if (position == "manual") {
    legend_vp <- viewport(
      name = "legend_box",
      x = coords[1], y = coords[2], just = theme$legend.justification,
      width = grobWidth(grobs$legend_box), 
      height = grobHeight(grobs$legend_box)
    )
  } else {
    legend_vp <- viewport(name = "legend_box")
  }
  vp <- surround_viewports(position, widths, heights, legend_vp)
  
  # Assign grobs to viewports ------------------------------------------------
  edit_vp <- function(x, name) {
    editGrob(x, vp=vpPath("background", name))
  }
  grobs <- c(
    list(theme_render(theme, "plot.background", vp = "background")),
    mlply(cbind(x = grobs, name = names(grobs)), edit_vp)
  )

  gTree(children = do.call("gList", grobs), childrenvp = vp)
}

# Generate viewports for plot surroundings
# This some pretty ugly code
# 
# @keyword internal
surround_viewports <- function(position, widths, heights, legend_vp) {
  layout <- grid.layout(
    length(heights), length(widths), 
    heights=heights, widths=widths
  )

  vp <- function(name, row, col) {
    viewport(
      name = name, 
      layout = layout, 
      layout.pos.row = row, 
      layout.pos.col = col
    )
  }

  if (position == "right") {
    viewports <- vpList(
      vp("panels", 3, 3),
      vp("legend_box", 3, 4),
      vp("ylabel", 3, 2),
      vp("xlabel", 4, 3),
      vp("title", 2, 3)
    )
  } else if (position == "left") {
    viewports <- vpList(
      vp("panels", 3, 4),
      vp("legend_box", 3, 2),
      vp("ylabel", 3, 3),
      vp("xlabel", 4, 4),
      vp("title", 2, 4)
    )
  } else if (position == "top") {
    viewports <- vpList(
      vp("panels", 4, 3),
      vp("legend_box", 3, 3),
      vp("ylabel", 4, 2),
      vp("xlabel", 5, 3),
      vp("title", 2, 3)
    )
  } else if (position == "bottom") {
    viewports <- vpList(
      vp("panels", 3, 3),
      vp("legend_box", 5, 3),
      vp("ylabel", 3, 2),
      vp("xlabel", 4, 3),
      vp("title", 2, 3)
    )
  } else {
    viewports <- vpList(
      vp("panels", 3, 3),
      vp("ylabel", 3, 2),
      vp("xlabel", 4, 3),
      vp("title", 2, 3),
      legend_vp
    )
  }
  vpTree(viewport(name = "background", layout = layout), viewports)
}

# Print ggplot
# Print generic for ggplot.  Plot on current graphics device.
#
# @arguments plot to display
# @arguments draw new (empty) page first?
# @arguments viewport to draw plot in
# @arguments other arguments passed on to \code{\link{ggplotGrob}}
# @keyword hplot
# @keyword internal 
print.ggplot <- function(x, newpage = is.null(vp), vp = NULL, ...) {
  set_last_plot(x)
  if (newpage) grid.newpage()
  if (is.null(vp)) {
    grid.draw(ggplotGrob(x, ...)) 
  } else {
    if (is.character(vp)) seekViewport(vp) else pushViewport(vp)
    grid.draw(ggplotGrob(x, ...)) 
    upViewport()
  }
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/plot-surrounds.r"



#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/plot.r"
# Create a new plot
# Create a new ggplot plot
# 
# @seealso \url{http://had.co.nz/ggplot2}
# @alias ggplot.default
# @keyword hplot
# @arguments default data set
# @arguments other arguments passed to specific methods
ggplot <- function(data = NULL, ...) UseMethod("ggplot")

ggplot.default <- function(data = NULL, mapping = aes(), ...) {
  ggplot.data.frame(fortify(data), mapping, ...)
}

# Create a new plot
# Create a new ggplot plot
# 
# @alias package-ggplot
# @arguments default data frame
# @arguments default list of aesthetic mappings (these can be colour, size, shape, line type -- see individual geom functions for more details)
# @arguments ignored
# @arguments environment in which evaluation of aesthetics should occur
# @seealso \url{http://had.co.nz/ggplot2}
# @alias package-ggplot
# @keyword hplot
ggplot.data.frame <- function(data, mapping=aes(), ..., environment = globalenv()) {
  if (!missing(mapping) && !inherits(mapping, "uneval")) stop("Mapping should be created with aes or aes_string")
  
  p <- structure(list(
    data = data, 
    layers = list(),
    scales = Scales$new(),
    mapping = mapping,
    options = list(),
    coordinates = CoordCartesian$new(),
    facet = FacetGrid$new(),
    plot_env = environment
  ), class="ggplot")
  
  p$options$labels <- make_labels(mapping)

  set_last_plot(p)
  p
}


plot_clone <- function(plot) {
  p <- plot
  p$scales <- plot$scales$clone()
  p$layers <- lapply(plot$layers, function(x) x$clone())
  p$facet <- plot$facet$clone()
  
  p
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-.r"
# Position adjustment occurs over all groups within a geom
# They work only with discrete x scales and may affect x and y position.
# Should occur after statistics and scales have been applied.

Position <- proto(TopLevel, expr = {
  adjust <- function(., data, scales, ...) data

  class <- function(.) "position"
  
  width <- NULL
  height <- NULL
  new <- function(., width = NULL, height = NULL) {
    .$proto(width = width, height = height)
  }

  parameters <- function(.) {
    pnames <- setdiff(names(formals(get("new", .))), ".")
    values <- lapply(pnames, get, envir = .)
    names(values) <- pnames
    
    values
  }
  
  pprint <- function(., newline=TRUE) {
    cat("position_", .$objname, ": (", clist(.$parameters()), ")", sep="")
    if (newline) cat("\n")
  }

  html_returns <- function(.) {
    ps(
      "<h2>Returns</h2>\n",
      "<p>This function returns a position object.</p>"
    )
  }
  
})


# Convenience function to ensure that all position variables 
# (x, xmin, xmax, xend) are transformed in the same way
# 
# @keyword internal
transform_position <- function(df, trans_x = NULL, trans_y = NULL, ...) {
  scales <- aes_to_scale(names(df))

  if (!is.null(trans_x)) {
    df[scales == "x"] <- lapply(df[scales == "x"], trans_x, ...)
  }
  if (!is.null(trans_y)) {
    df[scales == "y"] <- lapply(df[scales == "y"], trans_y, ...)
  }
  
  df
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-collide.r"
# Collide
# Detect and prevent collisions
# 
# Powers dodging, stacking and filling
# 
# @keyword internal
collide <- function(data, width = NULL, name, strategy, check.width = TRUE) {
  # Determine width
  if (!is.null(width)) {
    # Width set manually
    if (!(all(c("xmin", "xmax") %in% names(data)))) {
      data <- within(data, {
        xmin <- x - width / 2
        xmax <- x + width / 2
      })      
    }
  } else {
    if (!(all(c("xmin", "xmax") %in% names(data)))) {
      data$xmin <- data$x
      data$xmax <- data$x
    }
    
    # Width determined from data, must be floating point constant 
    widths <- unique(with(data, xmax - xmin))
    widths <- widths[!is.na(widths)]
    if (check.width && length(widths) > 1 && sd(widths) > 1e-6) {
      stop(name, " requires constant width", call. = FALSE)
    }
    width <- widths[1]
  }

  # Reorder by x position, preserving order of group
  data <- data[order(data$xmin, data$group), ]

  # Check for overlap
  intervals <- as.numeric(t(unique(data[c("xmin", "xmax")])))
  intervals <- scale(intervals[!is.na(intervals)])
  if (any(diff(intervals) < -1e-6)) {
    warning(name, " requires non-overlapping x intervals", call. = FALSE)
    # This is where the algorithm from [L. Wilkinson. Dot plots. 
    # The American Statistician, 1999.] should be used
  }

  if (!is.null(data$ymax)) {
    ddply(data, .(xmin), strategy, width = width)
  } else if (!is.null(data$y)) {
    message("ymax not defined: adjusting position using y instead")
    transform(
      ddply(transform(data, ymax = y), .(xmin), strategy, width = width),
      y = ymax
    )
  } else {
    stop("Neither y nor ymax defined")
  }
}

# Stack overlapping intervals
# Assumes that each set has the same horizontal position
# 
# @keyword internal
pos_stack <- function(df, width) {
  if (nrow(df) == 1) return(df)
  
  n <- nrow(df) + 1
  y <- with(df, ifelse(is.na(y), 0, y))
  if (all(is.na(df$x))) {
    heights <- rep(NA, n)
  } else {
    heights <- c(0, cumsum(y))
  }

  within(df, {
    ymin <- heights[-n]
    ymax <- heights[-1]
  })
}

# Stack overlapping intervals and set height to 1
# Assumes that each set has the same horizontal position
# 
# @keyword internal
pos_fill <- function(df, width) {
  within(pos_stack(df, width), {
    ymin <- ymin / max(ymax)
    ymax <- ymax / max(ymax)
  })
}

# Dodge overlapping interval
# Assumes that each set has the same horizontal position
# 
# @keyword internal
pos_dodge <- function(df, width) {
  n <- nrow(df)
  if (n == 1) return(df)
  
  if (!all(c("xmin", "xmax") %in% names(df))) {
    df$xmin <- df$x
    df$xmax <- df$x
  }

  d_width <- with(df, max(xmax - xmin))    
  diff <- width - d_width
  
  # df <- data.frame(n = c(2:5, 10, 26), div = c(4, 3, 2.666666,  2.5, 2.2, 2.1))
  # qplot(n, div, data = df)
  
  within(df, {
    xmin <- xmin + width / n * (seq_len(n) - 1) - diff * (n - 1) / (2 * n)
    xmax <- xmin + d_width / n
    x <- (xmin + xmax) / 2
  })
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-dodge.r"
PositionDodge <- proto(Position, {
  adjust <- function(., data, scales) {
    if (empty(data)) return(data.frame())
    check_required_aesthetics("x", names(data), "position_dodge")
    
    collide(data, .$width, .$my_name(), pos_dodge, check.width = FALSE)
  }  

  objname <- "dodge"
  desc <- "Adjust position by dodging overlaps to the side"
  icon <- function(.) {
    y <- c(0.5, 0.3)
    rectGrob(c(0.25, 0.75), y, width=0.4, height=y, gp=gpar(col="grey60", fill=c("#804070", "#668040")), vjust=1)
  }
  
  examples <- function(.) {
    ggplot(mtcars, aes(x=factor(cyl), fill=factor(vs))) +
      geom_bar(position="dodge")
    ggplot(diamonds, aes(x=price, fill=cut)) + geom_bar(position="dodge")
    # see ?geom_boxplot and ?geom_bar for more examples
    
    # Dodging things with different widths is tricky
    df <- data.frame(x=c("a","a","b","b"), y=1:4)
    (p <- qplot(x, y, data=df, position="dodge", geom="bar", stat="identity"))
    
    p + geom_linerange(aes(ymin = y-1, ymax = y+1), position="dodge")
    # You need to explicitly specify the width for dodging
    p + geom_linerange(aes(ymin = y-1, ymax = y+1), 
      position = position_dodge(width = 0.9))
      
    # Similarly with error bars:
    p + geom_errorbar(aes(ymin = y-1, ymax = y+1), width = 0.2,
      position="dodge")
    p + geom_errorbar(aes(ymin = y-1, ymax = y+1, width = 0.2),
      position = position_dodge(width = 0.90))
  }
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-fill.r"
PositionFill <- proto(Position, {
  adjust <- function(., data, scales) {
    if (empty(data)) return(data.frame())
    
    y <- scales$get_scales("y")
    y$limits <- c(0, 1)
    
    check_required_aesthetics(c("x", "ymax"), names(data), "position_fill")
    if (!all(data$ymin == 0)) warning("Filling not well defined when ymin != 0")
    collide(data, .$width, .$my_name(), pos_fill)
  }  

  objname <- "fill"
  desc <- "Stack overlapping objects on top of one another, and standardise have equal height"

  icon <- function(.) {
    y <- c(0.5, 0.8)
    rectGrob(0.5, c(0.625, 1), width=0.4, height=c(0.625, 0.375), gp=gpar(col="grey60", fill=c("#804070", "#668040")), vjust=1)
  }


  examples <- function(.) {
    # See ?geom_bar and ?geom_area for more examples
    ggplot(mtcars, aes(x=factor(cyl), fill=factor(vs))) + geom_bar(position="fill")
      
    cde <- geom_histogram(position="fill", binwidth = 500)
      
    ggplot(diamonds, aes(x=price)) + cde
    ggplot(diamonds, aes(x=price, fill=cut)) + cde
    ggplot(diamonds, aes(x=price, fill=clarity)) + cde
    ggplot(diamonds, aes(x=price, fill=color)) + cde
  }


})


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-identity.r"
PositionIdentity <- proto(Position, {
  objname <- "identity"
  desc <- "Don't adjust position"

  icon <- function(.) {
    rectGrob(0.5, c(0.5, 0.3), width=0.4, height=c(0.5, 0.3), gp=gpar(col="grey60", fill=c("#804070", "#668040")), vjust=1)
    
  }

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-jitter.r"
PositionJitter <- proto(Position, {
  
  adjust <- function(., data, scales) {
    if (empty(data)) return(data.frame())
    check_required_aesthetics(c("x", "y"), names(data), "position_jitter")
    
    if (is.null(.$width)) .$width <- resolution(data$x) * 0.4
    if (is.null(.$height)) .$height <- resolution(data$y) * 0.4
    
    trans_x <- NULL
    trans_y <- NULL
    if(.$width > 0) {
      trans_x <- function(x) jitter(x, amount = .$width)
    }
    if(.$height > 0) {
      trans_y <- function(x) jitter(x, amount = .$height)
    }
    
    transform_position(data, trans_x, trans_y)
  }
  
  objname <- "jitter" 
  desc <- "Jitter points to avoid overplotting"
  
  icon <- function(.) GeomJitter$icon()
  desc_params <- list(
    width = "degree of jitter in x direction. Defaults to 40\\% of the resolution of the data.", 
    height = "degree of jitter in y direction. Defaults to 40\\% of the resolution of the data."
    )

  examples <- function(.) {
    qplot(am, vs, data=mtcars)
    
    # Default amount of jittering will generally be too much for 
    # small datasets:
    qplot(am, vs, data=mtcars, position="jitter")
    # Control the amount as follows
    qplot(am, vs, data=mtcars, position=position_jitter(w=0.1, h=0.1))
    
    # The default works better for large datasets, where it will 
    # will up as much space as a boxplot or a bar
    qplot(cut, price, data=diamonds, geom=c("boxplot", "jitter"))
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/position-stack.r"
PositionStack <- proto(Position, {

  adjust <- function(., data, scales) {
    if (empty(data)) return(data.frame())
    
    if (is.null(data$ymax) && is.null(data$y)) {
      message("Missing y and ymax in position = 'stack'. ", 
        "Maybe you want position = 'identity'?")
      return(data)
    }

    if (!is.null(data$ymin) && !all(data$ymin == 0)) 
      warning("Stacking not well defined when ymin != 0", call. = FALSE)

    collide(data, .$width, .$my_name(), pos_stack)
  }  
  
  objname <- "stack"
  desc <- "Stack overlapping objects on top of one another"
  icon <- function(.) {
    y <- c(0.5, 0.8)
    rectGrob(0.5, c(0.5, 0.8), width=0.4, height=c(0.5, 0.3), gp=gpar(col="grey60", fill=c("#804070", "#668040")), vjust=1)
  }
  examples <- function(.) {
    # Stacking is the default behaviour for most area plots:
    ggplot(mtcars, aes(factor(cyl), fill = factor(vs))) + geom_bar()
      
    ggplot(diamonds, aes(price)) + geom_histogram(binwidth=500)
    ggplot(diamonds, aes(price, fill = cut)) + geom_histogram(binwidth=500)
    
    # Stacking is also useful for time series
    data.set <- data.frame(
      Time = c(rep(1, 4),rep(2, 4), rep(3, 4), rep(4, 4)),
      Type = rep(c('a', 'b', 'c', 'd'), 4),
      Value = rpois(16, 10)
    )
    
    qplot(Time, Value, data = data.set, fill = Type, geom = "area")
    # If you want to stack lines, you need to say so:
    qplot(Time, Value, data = data.set, colour = Type, geom = "line")
    qplot(Time, Value, data = data.set, colour = Type, geom = "line",
      position = "stack")
    # But realise that this makes it *much* harder to compare individual
    # trends
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/quick-plot.r"
# Quick plot.
# Quick plot is a convenient wrapper function for creating simple ggplot plot objects.
# 
# You can use it like you'd use the \code{\link{plot}} function.
# 
# @arguments x values
# @arguments y values
# @arguments z values
# @arguments other arguments passed on to the geom functions
# @arguments data frame to use (optional)
# @arguments faceting formula to use
# @arguments whether or not margins will be displayed
# @arguments geom to use (can be a vector of multiple names)
# @arguments statistic to use (can be a vector of multiple names)
# @arguments position adjustment to use (can be a vector of multiple names)
# @arguments limits for x axis (aesthetics to range of data)
# @arguments limits for y axis (aesthetics to range of data)
# @arguments which variables to log transform ("x", "y", or "xy")
# @arguments character vector or expression for plot title
# @arguments character vector or expression for x axis label
# @arguments character vector or expression for y axis label
# @arguments the y/x aspect ratio
# @keyword hplot 
# @alias quickplot 
#X # Use data from data.frame
#X qplot(mpg, wt, data=mtcars)
#X qplot(mpg, wt, data=mtcars, colour=cyl)
#X qplot(mpg, wt, data=mtcars, size=cyl)
#X qplot(mpg, wt, data=mtcars, facets=vs ~ am)
#X
#X # Use data from local environment
#X attach(mtcars)
#X qplot(hp, wt)
#X qplot(hp, wt, colour=cyl)
#X qplot(hp, wt, size=cyl)
#X qplot(hp, wt, facets=vs ~ am)
#X
#X qplot(1:10, rnorm(10), colour = runif(10))
#X qplot(1:10, letters[1:10])
#X mod <- lm(mpg ~ wt, data=mtcars)
#X qplot(resid(mod), fitted(mod))
#X qplot(resid(mod), fitted(mod), facets = . ~ vs)
#X
#X f <- function() {
#X    a <- 1:10
#X    b <- a ^ 2
#X    qplot(a, b)
#X } 
#X f()
#X 
#X # qplot will attempt to guess what geom you want depending on the input
#X # both x and y supplied = scatterplot
#X qplot(mpg, wt, data = mtcars)
#X # just x supplied = histogram
#X qplot(mpg, data = mtcars)
#X # just y supplied = scatterplot, with x = seq_along(y)
#X qplot(y = mpg, data = mtcars)
#X 
#X # Use different geoms
#X qplot(mpg, wt, geom="path")
#X qplot(factor(cyl), wt, geom=c("boxplot", "jitter"))
qplot <- function(x, y = NULL, z=NULL, ..., data, facets = . ~ ., margins=FALSE, geom = "auto", stat=list(NULL), position=list(NULL), xlim = c(NA, NA), ylim = c(NA, NA), log = "", main = NULL, xlab = deparse(substitute(x)), ylab = deparse(substitute(y)), asp = NA) {

  argnames <- names(as.list(match.call(expand.dots=FALSE)[-1]))
  arguments <- as.list(match.call()[-1])
  
  aesthetics <- compact(arguments[.all_aesthetics])
  aesthetics <- aesthetics[!is.constant(aesthetics)]
  aes_names <- names(aesthetics)
  aesthetics <- rename_aes(aesthetics)
  class(aesthetics) <- "uneval"
  
  if (missing(data)) {
    # If data not explicitly specified, will be pulled from workspace
    data <- data.frame()

    # Faceting variables must be in a data frame, so pull those out
    facetvars <- all.vars(facets)
    facetvars <- facetvars[facetvars != "."]
    names(facetvars) <- facetvars
    facetsdf <- as.data.frame(lapply(facetvars, get))
    if (nrow(facetsdf)) data <- facetsdf
  }

  # Work out plot data, and modify aesthetics, if necessary
  if ("auto" %in% geom) {
    if (stat == "qq" || "sample" %in% aes_names) {
      geom[geom == "auto"] <- "point"
      stat <- "qq"
    } else if (missing(y)) {
      geom[geom == "auto"] <- "histogram"
      if (is.null(ylab)) ylab <- "count"
    } else {
      if (missing(x)) {
        aesthetics$x <- bquote(seq_along(.(y)), aesthetics)
      }
      geom[geom == "auto"] <- "point"
    }
  }

  env <- parent.frame()
  p <- ggplot(data, aesthetics, environment = env)
  
  if (is.formula(facets) && length(facets) == 2) {
    p <- p + facet_wrap(facets)
  } else {
    p <- p + facet_grid(facets = deparse(facets), margins = margins)
  }
  
  if (!is.null(main)) p <- p + opts("title" = main)

  # Add geoms/statistics
  if (is.proto(position)) position <- list(position)
  
  mapply(function(g, s, ps) {
    if(is.character(g)) g <- Geom$find(g)
    if(is.character(s)) s <- Stat$find(s)
    if(is.character(ps)) ps <- Position$find(ps)

    params <- arguments[setdiff(names(arguments), c(aes_names, argnames))]
    params <- lapply(params, eval, parent.frame(n=1))
    
    p <<- p + layer(geom=g, stat=s, geom_params=params, stat_params=params, position=ps)
  }, geom, stat, position)
  
  logv <- function(var) var %in% strsplit(log, "")[[1]]

  if (logv("x")) p <- p + scale_x_log10()
  if (logv("y")) p <- p + scale_y_log10()
  
  if (!is.na(asp)) p <- p + opts(aspect.ratio = asp)
  
  if (!missing(xlab)) p <- p + xlab(xlab)
  if (!missing(ylab)) p <- p + ylab(ylab)
  
  if (!missing(xlim)) p <- p + xlim(xlim)
  if (!missing(ylim)) p <- p + ylim(ylim)
  
  p
}
quickplot <- qplot

# is.constant
# Determine if an expression represents a constant value
# 
# Used by qplot to determine whether a value should be mapped or set
#
# @keyword internal
is.constant <- function(x) {
  sapply(x, function(x) "I" %in% all.names(asOneSidedFormula(x)))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/save.r"
# ggsave
# Save a ggplot with sensible defaults
# 
# ggsave is a convenient function for saving a plot.  It defaults to
# saving the last plot that you displayed, and for a default size uses 
# the size of the current graphics device.  It also guesses the type of 
# graphics device from the extension.  This means the only argument you 
# need to supply is the filename.
# 
# \code{ggsave} currently recognises the extensions eps/ps, tex (pictex), pdf,
# jpeg, tiff, png, bmp, svg and wmf (windows only).
# 
# @arguments file name/filename of plot
# @arguments plot to save, defaults to last plot displayed
# @arguments device to use, automatically extract from file name extension
# @arguments path to save plot to (if you just want to set path and not filename)
# @arguments scaling factor
# @arguments width (in inches)
# @arguments height (in inches)
# @arguments dpi to use for raster graphics
# @arguments plot components to keep
# @arguments plot components to drop
# @arguments other arguments passed to graphics device
# @keyword file 
#X \dontrun{
#X ratings <- qplot(rating, data=movies, geom="histogram")
#X qplot(length, data=movies, geom="histogram")
#X ggsave(file="length-hist.pdf")
#X ggsave(file="length-hist.png")
#X ggsave(ratings, file="ratings.pdf")
#X ggsave(ratings, file="ratings.pdf", width=4, height=4)
#X # make twice as big as on screen
#X ggsave(ratings, file="ratings.pdf", scale=2)
#X }
ggsave <- function(filename=default_name(plot), plot = last_plot(), device=default_device(filename), path = NULL, scale=1, width=par("din")[1], height=par("din")[2], dpi=300, keep = plot$options$keep, drop = plot$options$drop, ...) {
  if (!inherits(plot, "ggplot")) stop("plot should be a ggplot2 plot")

  eps <- ps <- function(..., width, height)  
    grDevices::postscript(..., width=width, height=height, onefile=FALSE,
      horizontal = FALSE, paper = "special")
  tex <- function(..., width, height) 
    grDevices::pictex(..., width=width, height=height)
  pdf <- function(..., version="1.4") 
    grDevices::pdf(..., version=version)
  svg <- function(...) 
    grDevices::svg(...)
  wmf <- function(..., width, height) 
    grDevices::win.metafile(..., width=width, height=height)

  png <- function(..., width, height) 
    grDevices::png(...,  width=width, height=height, res = dpi, units = "in")
  jpg <- jpeg <- function(..., width, height) 
    grDevices::jpeg(..., width=width, height=height, res = dpi, units = "in")
  bmp <- function(..., width, height) 
    grDevices::bmp(...,  width=width, height=height, res = dpi, units = "in")
  tiff <- function(..., width, height) 
    grDevices::tiff(..., width=width, height=height, res = dpi, units = "in")
  
  default_name <- function(plot) { 
    paste(digest.ggplot(plot), ".pdf", sep="")
  }
  
  default_device <- function(filename) {
    pieces <- strsplit(filename, "\\.")[[1]]
    ext <- tolower(pieces[length(pieces)])
    match.fun(ext)
  }

  if (missing(width) || missing(height)) {
    message("Saving ", prettyNum(width * scale, digits=3), "\" x ", prettyNum(height * scale, digits=3), "\" image")
  }
  
  width <- width * scale
  height <- height * scale
  
  if (!is.null(path)) {
    filename <- file.path(path, filename)
  }
  device(file=filename, width=width, height=height, ...)
  on.exit(capture.output(dev.off()))
  print(plot, keep = keep, drop = drop)
  
  invisible()
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-.r"
# Domain: raw, transformed, user (limits)
# Range:  raw, transformed


Scale <- proto(TopLevel, expr={
  .input <- ""
  .output <- ""
  common <- NULL  
  legend <- TRUE
  limits <- NULL
  doc <- TRUE
  
  class <- function(.) "scale"
  
  new <- function(., name="Unknown") {
    .$proto(name=name)
  }
  
  clone <- function(.) {
    as.proto(.$as.list(all.names=TRUE), parent=.) 
  }
  
  trained <- function(.) {
    !is.null(.$input_set())
  }

  find <- function(., output, only.documented = FALSE) {
    scales <- Scales$find_all()
    select <- sapply(scales, function(x) any(output %in% c(x$output(), get("common", x))))
    if (only.documented) select <- select & sapply(scales, function(x) get("doc", x))
    
    unique(scales[select])
  }

  # Input --------------------------------------------------------------------
  
  breaks <- NULL

  input <- function(.) .$.input
  input_set <- function(.) {
    nulldefault(.$limits, .$.domain)
  }
  
  # Return names of all aesthetics in df that should be operated on
  # by this scale - this is currently used for x and y scales, which also
  # need to operate of {x,y}{min,max,end}.
  input_aesthetics <- function(., df) {
    input <- .$input()
    matches <- aes_to_scale(names(df)) == input
    names(df)[matches]
  }
  
  # Output -------------------------------------------------------------------
  
  output <- function(.) .$.output
  output_breaks <- function(.) .$map(.$input_breaks())
  output_expand <- function(.) {
    expand_range(.$output_set(), .$.expand[1], .$.expand[2])    
  }
  
  # Train scale from a data frame
  train_df <- function(., df, drop = FALSE) {
    if (empty(df)) return() 
    # Don't train if limits have already been set
    if (!is.null(.$limits)) return()
    
    input <- .$input_aesthetics(df)
    l_ply(input, function(var) .$train(df[[var]], drop))
  }

  # Map values from a data.frame.   Returns data.frame
  map_df <- function(., df) {
    output <- .$input_aesthetics(df)
    mapped <- llply(output, function(var) .$map(df[[var]]))
    
    if (length(mapped) == 0) {
      return(data.frame(matrix(nrow = nrow(df), ncol=0)))
    }
        
    output_df <- do.call("data.frame", mapped)
    names(output_df) <- output
    output_df
  }

  
  pprint <- function(., newline=TRUE) {
    clist <- function(x) paste(x, collapse=",")
    
    cat("scale_", .$objname, ": ", clist(.$input()),   " -> ", clist(.$output()), sep="")
    if (!is.null(.$input_set())) {
      cat(" (", clist(.$input_set()), " -> ", clist(.$output_set()), ")", sep="")
    }
    if (newline) cat("\n") 
  }
  
  html_returns <- function(.) {
    ps(
      "<h2>Returns</h2>\n",
      "<p>This function returns a scale object.</p>"
    )
  }
  
  my_names <- function(.) {
    ps(.$class(), .$common, .$objname, sep="_", collapse=NULL)
  }
  
  my_full_name <- function(.) {
    ps(.$class(), .$input(), .$objname, sep="_", collapse=NULL)
  }
  
  parameters <- function(.) {
    params <- formals(get("new", .))
    params[setdiff(names(params), c(".","variable"))]
  }
  
})




#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-continuous-.r"
ScaleContinuous <- proto(Scale, funEnvir = globalenv(), {  
  .domain <- c()
  .range <- c()
  .expand <- c(0.05, 0)
  .labels <- NULL
  discrete <- function(.) FALSE
  
  tr_default <- "identity"

  new <- function(., name=NULL, limits=NULL, breaks=NULL, labels=NULL, variable, trans = NULL, expand=c(0.05, 0), minor_breaks = NULL, formatter = "scientific", legend = TRUE, ...) {
    
    if (is.null(trans))      trans <- .$tr_default
    if (is.character(trans)) trans <- Trans$find(trans)
    
    # Transform limits and breaks
    limits <- trans$transform(limits)
    breaks <- trans$transform(breaks)
    minor_breaks <- trans$transform(minor_breaks)
    
    b_and_l <- check_breaks_and_labels(breaks, labels)
    
    .$proto(name=name, .input=variable, .output=variable, limits=limits, breaks = b_and_l$breaks, .labels = b_and_l$labels, .expand=expand, .tr = trans, minor_breaks = minor_breaks, formatter = formatter, legend = legend, ...)
  }
  
  set_limits <- function(., limits) {
    .$limits <- sort(.$.tr$transform(limits))
  }

  
  # Transform each 
  transform_df <- function(., df) {
    if (empty(df)) return(data.frame())
    input <- .$input()
    output <- .$output()
    
    if (length(input) == 1 && input %in% c("x", "y")) {
      matches <- aes_to_scale(names(df)) == input
      input <- output <- names(df)[matches]
    }
    input <- intersect(input, names(df))

    df <- colwise(.$.tr$transform)(df[input])
    if (ncol(df) == 0) return(NULL)
    names(df) <- output      
    df
  }
  
  train <- function(., x, drop = FALSE) {
    if (!is.null(.$limits)) return()
    if (is.null(x)) return()
    if (!is.numeric(x)) {
      stop(
        "Non-continuous variable supplied to ", .$my_full_name(), ".",
        call.=FALSE
      )
    }
    if (all(is.na(x)) || all(!is.finite(x))) return()
    .$.domain <- range(x, .$.domain, na.rm=TRUE, finite=TRUE)
  }
    
  # By default, a continuous scale does no transformation in the mapping stage
  # See scale_size for an exception
  map <- function(., values) {
    trunc <- !is.finite(values) | values %inside% .$output_set()
    as.numeric(ifelse(trunc, values, NA))
  }

  # By default, the range of a continuous scale is the same as its
  # (transformed) domain
  output_set <- function(.) .$input_set()
  
  # By default, breaks are regularly spaced along the (transformed) domain
  input_breaks <- function(.) {
    nulldefault(.$breaks, .$.tr$input_breaks(.$input_set()))
  }
  input_breaks_n <- function(.) .$input_breaks()


  minor_breaks <- NULL
  output_breaks <- function(., n = 2, b = .$input_breaks(), r = .$output_set()) {
    nulldefault(.$minor_breaks, .$.tr$output_breaks(n, b, r))
  }
  
  labels <- function(.) {
    if (!is.null(.$.labels)) return(.$.labels)
    b <- .$input_breaks()

    l <- .$.tr$label(b)
    numeric <- sapply(l, is.numeric)
    
    f <- match.fun(get("formatter", .))
    l[numeric] <- f(unlist(l[numeric]))
    l
  }
  
  test <- function(.) {
    m <- .$output_breaks(10)
    b <- .$input_breaks()
    
    plot(x=0,y=0,xlim=range(c(b,m)), ylim=c(1,5), type="n", axes=F,xlab="", ylab="")
    for(i in 1:(length(b))) axis(1, b[[i]], as.expression(.$labels()[[i]]))
    
    abline(v=m)
    abline(v=b, col="red")
  }
  
  objname <- "continuous"
  common <- c("x", "y")
  desc <- "Continuous position scale"
  seealso <- list(
    "scale_discrete" = "Discrete position scales"
  )
  examples <- function(.) {
    (m <- qplot(rating, votes, data=subset(movies, votes > 1000), na.rm = T))
    
    # Manipulating the default position scales lets you:

    #  * change the axis labels
    m + scale_y_continuous("number of votes")
    m + scale_y_continuous(expression(votes^alpha))
    
    #  * modify the axis limits
    m + scale_y_continuous(limits=c(0, 5000))
    m + scale_y_continuous(limits=c(1000, 10000))
    m + scale_x_continuous(limits=c(7, 8))
    
    # you can also use the short hand functions xlim and ylim
    m + ylim(0, 5000)
    m + ylim(1000, 10000)
    m + xlim(7, 8)

    #  * choose where the ticks appear
    m + scale_x_continuous(breaks=1:10)
    m + scale_x_continuous(breaks=c(1,3,7,9))

    #  * manually label the ticks
    m + scale_x_continuous(breaks=c(2,5,8), labels=c("two", "five", "eight"))
    m + scale_x_continuous(breaks=c(2,5,8), labels=c("horrible", "ok", "awesome"))
    m + scale_x_continuous(breaks=c(2,5,8), labels=expression(Alpha, Beta, Omega))
    
    # There are also a wide range of transformations you can use:
    m + scale_y_log10()
    m + scale_y_log()
    m + scale_y_log2()
    m + scale_y_sqrt()
    m + scale_y_reverse()
    # see ?transformer for a full list
    
    # You can control the formatting of the labels with the formatter
    # argument.  Some common formats are built in:
    x <- rnorm(10) * 100000
    y <- seq(0, 1, length = 10)
    p <- qplot(x, y)
    p + scale_y_continuous(formatter = "percent")
    p + scale_y_continuous(formatter = "dollar")
    p + scale_x_continuous(formatter = "comma")
    
    # qplot allows you to do some of this with a little less typing:
    #   * axis limits
    qplot(rating, votes, data=movies, ylim=c(1e4, 5e4))
    #   * axis labels
    qplot(rating, votes, data=movies, xlab="My x axis", ylab="My y axis")
    #   * log scaling
    qplot(rating, votes, data=movies, log="xy")
  }
})


# Check breaks and labels.
# Ensure that breaks and labels are the correct format.cd .. 
#
# @keyword internal
#X check_breaks_and_labels(NULL, NULL)
#X check_breaks_and_labels(1:5, NULL)
#X should_stop(check_breaks_and_labels(labels = 1:5))
#X check_breaks_and_labels(labels = c("a" = 1, "b" = 2))
#X check_breaks_and_labels(breaks = c("a" = 1, "b" = 2))
#X check_breaks_and_labels(1:2, c("a", "b"))
check_breaks_and_labels <- function(breaks = NULL, labels = NULL) {
  # Both missing, so it's ok
  if (is.null(breaks) && is.null(labels)) {
    return(list(breaks = NULL, labels = NULL))
  }
   
  # Otherwise check for names, and use them for the complement
  if (is.null(breaks) && !is.null(names(labels))) {
    breaks <- names(labels)
    labels <- unname(labels)
  } else if (is.null(labels) && !is.null(names(breaks))) {
    labels <- names(breaks)
    breaks <- unname(breaks)
  }
  
  # If specified, labels should:
  if (!is.null(labels)) {
    # * be accompanied by breaks
    if (is.null(breaks)) {
      stop("Labels can only be specified in conjunction with breaks", 
        call. = FALSE)
    }
    
    # * be the same length as breaks
    if (length(labels) != length(breaks)) {
      stop("Labels and breaks must be same length", call. = FALSE)
    }
  }
 
  list(breaks = breaks, labels = labels)  
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-continuous-alpha.r"
ScaleAlphaContinuous <- proto(ScaleContinuous, expr={
  doc <- TRUE
  common <- NULL
  aliases <- "scale_alpha"
  
  new <- function(., name=NULL, limits=NULL, breaks=NULL, labels=NULL, trans = NULL, to = c(0.1, 1), legend = TRUE) {
    .super$new(., name=name, limits=limits, breaks=breaks, labels=labels, trans=trans, variable = "alpha", to = to, legend = legend)
  }
  
  map <- function(., values) {
    rescale(values, .$to, .$input_set())
  }
  output_breaks <- function(.) .$map(.$input_breaks())
  
  objname <- "alpha_continuous"
  desc <- "Alpha scale for continuous variable"
  
  icon <- function(.) {
    x <- c(0.1, 0.3, 0.5, 0.7, 0.9)
    rectGrob(x, width=0.25, 
      gp=gpar(fill=alpha("black", x), col=NA)
    )
    
  }
  
  examples <- function(.) {
    (p <- qplot(mpg, cyl, data=mtcars, alpha=cyl))
    p + scale_alpha("cylinders")
    p + scale_alpha("number\nof\ncylinders")
    
    p + scale_alpha(to = c(0.4, 0.8))
  }  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-continuous-colour.r"
ScaleGradient <- proto(ScaleContinuous, expr={
  aliases <- c("scale_colour_continuous", "scale_fill_continuous", "scale_color_continuous", "scale_color_gradient")

  new <- function(., name = NULL, low = "#3B4FB8", high = "#B71B1A", space = "rgb", ...) {

    .super$new(., name = name, low = low, high = high, space = space, ...)
  }
  
  map <- function(., x) {
    ramp  <- colorRamp(c(.$low, .$high),  space=.$space, interpolate="linear")

    x <- rescale(x, from = .$input_set(), to = c(0, 1))
    nice_ramp(ramp, x)
  }
    
  output_breaks <- function(.) {
    .$map(.$input_breaks()) 
  }
  
  common <- c("colour", "fill")

  # Documentation -----------------------------------------------
  
  objname <- "gradient"
  desc <- "Smooth gradient between two colours"
  icon <- function(.) {
    g <- scale_fill_gradient()
    g$train(1:5)
    rectGrob(c(0.1, 0.3, 0.5, 0.7, 0.9), width=0.21, 
      gp=gpar(fill=g$map(1:5), col=NA)
    )    
  }


  desc_params <- list(
    low = "colour at low end of scale", 
    high = "colour at high end of scale",
    space = "colour space to interpolate through, rgb or Lab, see ?colorRamp for details",
    interpolate = "type of interpolation to use, linear or spline, see ?colorRamp for more details"
  )
  seealso <- list(
    "scale_gradient2" = "continuous colour scale with midpoint",
    "colorRamp" = "for details of interpolation algorithm"
  )
  
  examples <- function(.) {
    # It's hard to see, but look for the bright yellow dot 
    # in the bottom right hand corner
    dsub <- subset(diamonds, x > 5 & x < 6 & y > 5 & y < 6)
    (d <- qplot(x, y, data=dsub, colour=z))
    # That one point throws our entire scale off.  We could
    # remove it, or manually tweak the limits of the scale
    
    # Tweak scale limits.  Any points outside these
    # limits will not be plotted, but will continue to affect the 
    # calculate of statistics, etc
    d + scale_colour_gradient(limits=c(3, 10))
    d + scale_colour_gradient(limits=c(3, 4))
    # Setting the limits manually is also useful when producing
    # multiple plots that need to be comparable
    
    # Alternatively we could try transforming the scale:
    d + scale_colour_gradient(trans = "log")
    d + scale_colour_gradient(trans = "sqrt")
    
    # Other more trivial manipulations, including changing the name
    # of the scale and the colours.

    d + scale_colour_gradient("Depth")
    d + scale_colour_gradient(expression(Depth[mm]))
    
    d + scale_colour_gradient(limits=c(3, 4), low="red")
    d + scale_colour_gradient(limits=c(3, 4), low="red", high="white")
    # Much slower
    d + scale_colour_gradient(limits=c(3, 4), low="red", high="white", space="Lab")
    d + scale_colour_gradient(limits=c(3, 4), space="Lab")
    
    # scale_fill_continuous works similarly, but for fill colours
    (h <- qplot(x - y, data=dsub, geom="histogram", binwidth=0.01, fill=..count..))
    h + scale_fill_continuous(low="black", high="pink", limits=c(0,3100))
  }
  
  
})

ScaleGradient2 <- proto(ScaleContinuous, expr={  
  new <- function(., name = NULL, low = muted("red"), mid = "white", high = muted("blue"), midpoint = 0, space = "rgb", ...) {
    .super$new(., name = name, low = low, mid = mid, high = high,
      midpoint = midpoint, space = space, ...)
  }
  
  aliases <- c("scale_color_gradient2")
  map <- function(., x) {
    ramp  <- colorRamp(c(.$low, .$mid, .$high), space=.$space,
      interpolate="linear")
    
    rng <- .$output_set()  - .$midpoint
    extent <- max(abs(rng))
    
    domain <- .$input_set()
    x[x < domain[1] | x > domain[2]] <- NA

    x <- x - .$midpoint
    x <- x / extent / 2 + 0.5
    
    nice_ramp(ramp, x)
  }
  
  objname <-"gradient2"
  common <- c("colour", "fill")
  desc <- "Smooth gradient between three colours (high, low and midpoints)"

  output_breaks <- function(.) .$map(.$input_breaks())

  icon <- function(.) {
    g <- scale_fill_gradient2()
    g$train(1:5 - 3)
    rectGrob(c(0.1, 0.3, 0.5, 0.7, 0.9), width=0.21, 
      gp=gpar(fill=g$map(1:5 - 3), col=NA)
    )
  }

  desc_params <- list(
    low = "colour at low end of scale", 
    mid = "colour at mid point of scale",
    high = "colour at high end of scale",
    midpoint = "position of mid point of scale, defaults to 0",
    space = "colour space to interpolate through, rgb or Lab, see ?colorRamp for details",
    interpolate = "type of interpolation to use, linear or spline, see ?colorRamp for more details"
  )
  seealso <- list(
    "scale_gradient" = "continuous colour scale",
    "colorRamp" = "for details of interpolation algorithm"
  )
  
  examples <- function(.) {
    dsub <- subset(diamonds, x > 5 & x < 6 & y > 5 & y < 6)
    dsub$diff <- with(dsub, sqrt(abs(x-y))* sign(x-y))
    (d <- qplot(x, y, data=dsub, colour=diff))
    
    d + scale_colour_gradient2()
    # Change scale name
    d + scale_colour_gradient2(expression(sqrt(abs(x - y))))
    d + scale_colour_gradient2("Difference\nbetween\nwidth and\nheight")

    # Change limits and colours
    d + scale_colour_gradient2(limits=c(-0.2, 0.2))

    # Using "muted" colours makes for pleasant graphics 
    # (and they have better perceptual properties too)
    d + scale_colour_gradient2(low="red", high="blue")
    d + scale_colour_gradient2(low=muted("red"), high=muted("blue"))

    # Using the Lab colour space also improves perceptual properties
    # at the price of slightly slower operation
    d + scale_colour_gradient2(space="Lab")
    
    # About 5% of males are red-green colour blind, so it's a good
    # idea to avoid that combination
    d + scale_colour_gradient2(high=muted("green"))

    # We can also make the middle stand out
    d + scale_colour_gradient2(mid=muted("green"), high="white", low="white")
    
    # or use a non zero mid point
    (d <- qplot(carat, price, data=diamonds, colour=price/carat))
    d + scale_colour_gradient2(midpoint=mean(diamonds$price / diamonds$carat))
    
    # Fill gradients work much the same way
    p <- qplot(letters[1:5], 1:5, fill= c(-3, 3, 5, 2, -2), geom="bar")
    p + scale_fill_gradient2("fill")
    # Note how positive and negative values of the same magnitude
    # have similar intensity
  }
  
})


ScaleGradientn <- proto(ScaleContinuous, expr={  
  new <- function(., name=NULL, colours, values = NULL, rescale = TRUE, space="rgb", ...) {
    
    .super$new(., 
      name = name, 
      colours = colours, values = values, rescale = rescale, 
      space = space,  ..., 
    )
  }

  aliases <- c("scale_color_gradientn")
  
  map <- function(., x) {
    if (.$rescale) x <- rescale(x, c(0, 1), .$input_set())
    if (!is.null(.$values)) {
      xs <- seq(0, 1, length = length(.$values))      
      f <- approxfun(.$values, xs)
      x <- f(x)
    }
    ramp <- colorRamp(.$colours, space=.$space, interpolate="linear")
    nice_ramp(ramp, x)
  }
  
  objname <- "gradientn"
  common <- c("colour", "fill")
  desc <- "Smooth gradient between n colours"

  output_breaks <- function(.) .$map(.$input_breaks())

  icon <- function(.) {
    g <- scale_fill_gradientn(colours = rainbow(7))
    g$train(1:5)
    rectGrob(c(0.1, 0.3, 0.5, 0.7, 0.9), width=0.21, 
      gp=gpar(fill = g$map(1:5), col=NA)
    )
  }

  desc_params <- list(
    space = "colour space to interpolate through, rgb or Lab, see ?colorRamp for details",
    interpolate = "type of interpolation to use, linear or spline, see ?colorRamp for more details"
  )
  seealso <- list(
    "scale_gradient" = "continuous colour scale with midpoint",
    "colorRamp" = "for details of interpolation algorithm"
  )
  
  examples <- function(.) {    
    # scale_colour_gradient make it easy to use existing colour palettes

    dsub <- subset(diamonds, x > 5 & x < 6 & y > 5 & y < 6)
    dsub$diff <- with(dsub, sqrt(abs(x-y))* sign(x-y))
    (d <- qplot(x, y, data=dsub, colour=diff))

    d + scale_colour_gradientn(colour = rainbow(7))
    breaks <- c(-0.5, 0, 0.5)
    d + scale_colour_gradientn(colour = rainbow(7), 
      breaks = breaks, labels = format(breaks))
    
    d + scale_colour_gradientn(colour = topo.colors(10))
    d + scale_colour_gradientn(colour = terrain.colors(10))

    # You can force them to be symmetric by supplying a vector of 
    # values, and turning rescaling off
    max_val <- max(abs(dsub$diff))
    values <- seq(-max_val, max_val, length = 11)

    d + scale_colour_gradientn(colours = topo.colors(10), 
      values = values, rescale = FALSE)
    d + scale_colour_gradientn(colours = terrain.colors(10), 
      values = values, rescale = FALSE)
    

  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-convenience.r"
# Set x limits
# Convenience function to set the limits of the x axis.
# 
# @arguments if numeric, will create a continuos scale, if factor or character, will create a discrete scale
# @keyword hplot
# @arguments limits
#X xlim(15, 20)
#X xlim(20, 15)
#X xlim(c(10, 20))
#X xlim("a", "b", "c") 
#X qplot(mpg, wt, data=mtcars) + xlim(15, 20)
xlim <- function(...) {
  limits(c(...), "x")
}

# Set y limits
# Convenience function to set the limits of the y axis.
# 
# @arguments if numeric, will create a continuos scale, if factor or character, will create a discrete scale
# @keyword hplot
# @arguments limits
#X ylim(15, 20)
#X ylim(c(10, 20))
#X ylim("a", "b", "c") 
#X qplot(mpg, wt, data=mtcars) + ylim(15, 20)
ylim <- function(...) {
  limits(c(...), "y")
}

# Scale limits
# Generate correct scale type for specified limits
# 
# @arguments vector of limits
# @arguments variable
# @keyword internal
# @alias limits.numeric
# @alias limits.character
# @alias limits.factor 
# @alias limits.Date
# @alias limits.POSIXct
# @alias limits.POSIXlt
#X limits(c(1, 5), "x")
#X limits(c(5, 1), "x")
#X limits(c("A", "b", "c"), "x")
#X limits(as.Date(c("2008-01-01", "2009-01-01")), "x")
limits <- function(lims, var) UseMethod("limits")
limits.numeric <- function(lims, var) {
  stopifnot(length(lims) == 2)
  if (lims[1] > lims[2]) {
    trans <- "reverse"
  } else {
    trans <- "identity"
  }
  ScaleContinuous$new(var = var, limits = lims, trans = trans)  
}
limits.character <- function(lims, var) {
  ScaleDiscretePosition$new(var = var, limits = lims)
}
limits.factor <- function(lims, var) {
  ScaleDiscretePosition$new(var = var, limits = as.character(lims))
}
limits.Date <- function(lims, var) {
  stopifnot(length(lims) == 2)
  ScaleDate$new(var = var, limits = lims)
}
limits.POSIXct <- function(lims, var) {
  stopifnot(length(lims) == 2)
  ScaleDatetime$new(var = var, limits = lims)
}
limits.POSIXlt <- function(lims, var) {
  stopifnot(length(lims) == 2)
  ScaleDatetime$new(var = var, limits = as.POSIXct(lims))
}

# Expand the plot limits with data.
# Some times you may want to ensure limits include a single value, for all panels or all plots.  This function is a thin wrapper around \code{\link{geom_blank}} that makes it easy to add such values.
# 
# @arguments named list of aesthetics specifying the value (or values that should be included.
# @keyword hplot
#X p <- qplot(mpg, wt, data = mtcars)
#X p + expand_limits(x = 0)
#X p + expand_limits(y = c(1, 9))
#X p + expand_limits(x = 0, y = 0)
#X
#X qplot(mpg, wt, data = mtcars, colour = cyl) + 
#X  expand_limits(colour = seq(2, 10, by = 2))
#X qplot(mpg, wt, data = mtcars, colour = factor(cyl)) + 
#X  expand_limits(colour = factor(seq(2, 10, by = 2)))
expand_limits <- function(...) {
  data <- data.frame(...)
  
  geom_blank(aes_all(names(data)), data, inherit.aes = FALSE)
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-date.r"
#time <- ScaleTime$new(major="months", minor="weeks")

# For an time axis (ie. given two dates indicating the start and end of the time series), you want to be able to specify:
# 
#    * the interval between major and minor ticks. A string (second, minute, hour, day, week, month, quarter, year + all plurals) posibly including multiplier (usually integer, always positive) giving the interval between ticks.  
# 
#   * the position of the first tick, as a date/time.  This should default to a round number of intervals, before first the data point if necessary.
# 
#   * format string which controls how the date is printed (should default to displaying just enough to distinguish each interval).
#
#   * threshold for displaying first/last tick mark outside the data: if the last date point is > threshold * interval (default to 0.9)

ScaleDate <- proto(ScaleContinuous,{
  .major_seq <- NULL
  .minor_seq <- NULL
  
  common <- c("x", "y")
  
  new <- function(., name=NULL, limits=NULL, major=NULL, minor=NULL, format=NULL, expand=c(0.05, 0), variable="x") {
    
    trans <- Trans$find("date")
    limits <- trans$transform(limits)
    
    .$proto(name=name, .input=variable, .output=variable, 
      major_seq=major, minor_seq=minor, format=format, .expand = expand,
      .tr=trans, limits = limits)
  }
  
  train <- function(., values, drop = FALSE) {
    .$.domain <- range(c(values, .$.domain), na.rm=TRUE, finite = TRUE)
  }
  
  break_points <- function(.) {
    auto <- date_breaks(diff(range(.$input_set()))) 
    
    c(
      .$major_seq %||% auto$major,
      .$minor_seq %||% auto$minor,
      .$format %||% auto$format
    )
  }
  
  input_breaks <- function(.) {
    d <- to_date(.$input_set())
    as.numeric(fullseq_date(d, .$break_points()[1]))
  }
  input_breaks_n <- function(.) as.numeric(.$input_breaks())
  
  output_breaks <- function(., n) {
    d <- to_date(.$input_set())
    as.numeric(fullseq_date(d, .$break_points()[2]))
  }
  
  labels <- function(.) {
    format(.$.tr$inverse(.$input_breaks()), .$break_points()[3])
  }

  # Documentation -----------------------------------------------

  objname <- "date"
  desc <- "Position scale, date"
  
  icon <- function(.) {
    textGrob("14/10/1979", gp=gpar(cex=1))
  }

  examples <- function(.) {
    # We'll start by creating some nonsense data with dates
    df <- data.frame(
      date = seq(Sys.Date(), len=100, by="1 day")[sample(100, 50)],
      price = runif(50)
    )
    df <- df[order(df$date), ]
    dt <- qplot(date, price, data=df, geom="line") + opts(aspect.ratio = 1/4)
    
    # We can control the format of the labels, and the frequency of 
    # the major and minor tickmarks.  See ?format.Date and ?seq.Date 
    # for more details.
    dt + scale_x_date()
    dt + scale_x_date(format="%m/%d")
    dt + scale_x_date(format="%W")
    dt + scale_x_date(major="months", minor="weeks", format="%b")
    dt + scale_x_date(major="months", minor="3 days", format="%b")
    dt + scale_x_date(major="years", format="%b-%Y")
    
    # The date scale will attempt to pick sensible defaults for 
    # major and minor tick marks
    qplot(date, price, data=df[1:10,], geom="line")
    qplot(date, price, data=df[1:4,], geom="line")

    df <- data.frame(
      date = seq(Sys.Date(), len=1000, by="1 day"),
      price = runif(500)
    )
    qplot(date, price, data=df, geom="line")
    
    # A real example using economic time series data
    qplot(date, psavert, data=economics) 
    qplot(date, psavert, data=economics, geom="path") 
    
    end <- max(economics$date)
    last_plot() + scale_x_date(lim = c(as.Date("2000-1-1"), end))
    last_plot() + scale_x_date(lim = c(as.Date("2005-1-1"), end))
    last_plot() + scale_x_date(lim = c(as.Date("2006-1-1"), end))
    
    # If we want to display multiple series, one for each variable
    # it's easiest to first change the data from a "wide" to a "long"
    # format:
    em <- melt(economics, id = "date")
    
    # Then we can group and facet by the new "variable" variable
    qplot(date, value, data = em, geom = "line", group = variable)
    qplot(date, value, data = em, geom = "line", group = variable) + 
      facet_grid(variable ~ ., scale = "free_y")
    
  }
  
})




# To date
# Turn numeric vector into date vector
# 
# @keyword internal
to_date <- function(x) structure(x, class="Date")


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-datetime.r"
# To time
# Turn numeric vector into POSIXct vector
# 
# @keyword internal
to_time <- function(x) structure(x, class = c("POSIXt", "POSIXct"))

ScaleDatetime <- proto(ScaleDate, {
  .major_seq <- NULL
  .minor_seq <- NULL
  tz <- NULL
  
  common <- c("x", "y")
  
  new <- function(., name=NULL, limits=NULL, major=NULL, minor=NULL, format=NULL, expand=c(0.05, 0), variable="x", tz = "") {
    
    trans <- Trans$find("datetime")
    limits <- trans$transform(limits)
    .$proto(name=name, .input=variable, .output=variable, 
      major_seq=major, minor_seq=minor, format=format, .expand = expand, 
      .tr=trans, limits = limits, tz=tz)
  }
  
  break_points <- function(.) {
    auto <- time_breaks(diff(range(.$input_set()))) 
    c(
      .$major_seq %||% auto$major,
      .$minor_seq %||% auto$minor,
      .$format %||% auto$format
    )
  }

  input_breaks <- function(.) {
    d <- to_time(.$input_set())
    as.numeric(fullseq_time(d, .$break_points()[1]))
  }
  
  output_breaks <- function(., n) {
    d <- to_time(.$input_set())
    as.numeric(fullseq_time(d, .$break_points()[2]))
  }
  
  labels <- function(.) {
    breaks <- .$.tr$inverse(.$input_breaks())
    attr(breaks, "tzone") <- .$tz
    format(breaks, .$break_points()[3])
  }

  # Documentation -----------------------------------------------

  objname <- "datetime"
  desc <- "Position scale, date time"
  
  icon <- function(.) {
    textGrob("14/10/1979\n10:14am", gp=gpar(cex=0.9))
  }

  examples <- function(.) {
    start <- ISOdate(2001, 1, 1, tz = "")
    df <- data.frame(
      day30  = start + round(runif(100, max = 30 * 86400)),
      day7  = start + round(runif(100, max = 7 * 86400)),
      day   = start + round(runif(100, max = 86400)),
      hour10 = start + round(runif(100, max = 10 * 3600)),
      hour5 = start + round(runif(100, max = 5 * 3600)),
      hour  = start + round(runif(100, max = 3600)),
      min10 = start + round(runif(100, max = 10 * 60)),
      min5  = start + round(runif(100, max = 5 * 60)),
      min   = start + round(runif(100, max = 60)),
      sec10 = start + round(runif(100, max = 10)),
      y = runif(100)
    )

    # Automatic scale selection
    qplot(sec10, y, data = df)
    qplot(min, y, data = df)
    qplot(min5, y, data = df)
    qplot(min10, y, data = df)
    qplot(hour, y, data = df)
    qplot(hour5, y, data = df)
    qplot(hour10, y, data = df)
    qplot(day, y, data = df)
    qplot(day30, y, data = df)
    
    # Manual scale selection
    qplot(day30, y, data = df)
    last_plot() + scale_x_datetime(major = "2 weeks")
    last_plot() + scale_x_datetime(major = "2 weeks", minor = "1 week")
    last_plot() + scale_x_datetime(major = "10 days")
    # See ?strptime for formatting parameters
    last_plot() + scale_x_datetime(major = "10 days", format = "%d/%m")
    
  }
  
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-defaults.r"
# Set default scale
# Overrides the default scale with one of your choosing.
#
# @arguments 
# @arguments type of variable (discrete, continuous, date)
# @arguments name of new default scale
# @keyword internal
#X qplot(mpg, wt, data=mtcars, colour=factor(cyl)) 
#X set_default_scale("colour","discrete", "grey")
#X qplot(mpg, wt, data=mtcars, colour=factor(cyl)) 
#X set_default_scale("colour","discrete", "hue")
set_default_scale <- function(aesthetic, type, scale, ...) {
  default <- paste("scale", aesthetic, type, sep="_")
  settings <- list(...)
  
  new_scale <- get(paste("Scale", firstUpper(scale), sep=""))
  new_call <- function(...) {
    do.call(new_scale$new, c(settings, list(..., variable=aesthetic)))
  }
  
  # For development
  if (exists(default, 1, inherits=FALSE)) {
    assign(default, new_call, 1)
  }
  assignInNamespace(default, new_call, "ggplot2") 
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-discrete-.r"
ScaleDiscrete <- proto(Scale, expr={
  .domain <- c()
  max_levels <- function(.) Inf
  .expand <- c(0, 0.05)
  .labels <- NULL
  doc <- FALSE

  discrete <- function(.) TRUE

  new <- function(., name=NULL, variable=.$.input, expand = c(0.05, 0.55), limits = NULL, breaks = NULL, labels = NULL, formatter = identity, drop = FALSE, legend = TRUE) {
    
    b_and_l <- check_breaks_and_labels(breaks, labels)
    
    .$proto(name=name, .input=variable, .output=variable, .expand = expand, .labels = b_and_l$labels, limits = limits, breaks = b_and_l$breaks, formatter = formatter, drop = drop, legend = legend)
  }

  # Range -------------------
  map <- function(., values) {
    .$check_domain()
    .$output_set()[match(as.character(values), .$input_set())]
  }

  input_breaks <- function(.) nulldefault(.$breaks, .$input_set())
  input_breaks_n <- function(.) match(.$input_breaks(), .$input_set())
  
  labels <- function(.) {
    if (!is.null(.$.labels)) return(as.list(.$.labels))
    
    f <- match.fun(get("formatter", .))
    as.list(f(.$input_breaks()))
  }
  
  output_set <- function(.) seq_along(.$input_set())
  output_breaks <- function(.) .$map(.$input_breaks())


  # Domain ------------------------------------------------
  
  transform_df <- function(., df) {
    NULL
  }

  # Override default behaviour: we do need to train, even if limits
  # have been set
  train_df <- function(., df, drop = FALSE) {
    if (empty(df)) return() 
    if (!is.null(.$limits)) return()
    
    input <- .$input_aesthetics(df)
    l_ply(input, function(var) .$train(df[[var]], drop))
  }

  train <- function(., x, drop = .$drop) {
    if (is.null(x)) return()
    if (!is.discrete(x)) {
      stop("Continuous variable (", .$name , ") supplied to discrete ",
       .$my_name(), ".", call. = FALSE) 
    }
    
    .$.domain <- discrete_range(.$.domain, x, drop = drop)
  }

  check_domain <- function(.) {
    d <- .$input_set()
    if (length(d) > .$max_levels()) {
      stop(.$my_name(), " can deal with a maximum of ", .$max_levels(), " discrete values, but you have ", length(d), ".  See ?scale_manual for a possible alternative", call. = FALSE)
    }  
  }
  
  # Guides
  # -------------------

  minor_breaks <- function(.) NA
  
  # Documentation
  objname <- "discrete"
  

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-discrete-colour.r"
ScaleColour <- proto(ScaleDiscrete, expr={
  objname <- "colour"
  doc <- FALSE
  common <- c()
})

ScaleHue <- proto(ScaleColour, expr={
  aliases <- c("scale_colour_discrete", "scale_fill_discrete", "scale_color_hue", "scale_color_discrete")
  
  new <- function(., name=NULL, h=c(0,360) + 15, l=65, c=100, limits=NULL, breaks = NULL, labels=NULL, h.start = 0, direction = 1,  formatter = identity, legend = TRUE, variable) {
    b_and_l <- check_breaks_and_labels(breaks, labels)
    
    .$proto(name=name, h=h, l=l, c=c, .input=variable, .output=variable, .labels = b_and_l$labels, breaks = b_and_l$breaks, direction = direction, start  = h.start, limits = limits, formatter = formatter, legend = legend)
  }
  
  output_set <- function(.) {
    
    rotate <- function(x) (x + .$start) %% 360 * .$direction

    n <- length(.$input_set())
    if ((diff(.$h) %% 360) < 1) {
      .$h[2] <- .$h[2] - 360 / n
    }

    grDevices::hcl(
      h = rotate(seq(.$h[1], .$h[2], length = n)), 
      c =.$c, 
      l =.$l
    )
  }
  max_levels <- function(.) Inf

  doc <- TRUE
  common <- c("colour", "fill")

  # Documentation -----------------------------------------------
  objname <- "hue"
  desc <- "Qualitative colour scale with evenly spaced hues"
  icon <- function(.) {
    rectGrob(c(0.1, 0.3, 0.5, 0.7, 0.9), width=0.21, 
      gp=gpar(fill=hcl(seq(0, 360, length=6)[-6], c=100, l=65), col=NA)
    )
  }
  
  desc_params <- list(
    h = "range of hues to use, in [0, 360]", 
    l = "luminance (lightness), in [0, 100]",
    c = "chroma (intensity of colour)",
    h.start = "hue to start at",
    direction = "direction to travel around the colour wheel, 1 = clockwise, -1 = counter-clockwise"
  )
  
  examples <- function(.) {
    dsamp <- diamonds[sample(nrow(diamonds), 1000), ]
    (d <- qplot(carat, price, data=dsamp, colour=clarity))
  
    # Change scale label
    d + scale_colour_hue()
    d + scale_colour_hue("clarity")
    d + scale_colour_hue(expression(clarity[beta]))
    
    # Adjust luminosity and chroma
    d + scale_colour_hue(l=40, c=30)
    d + scale_colour_hue(l=70, c=30)
    d + scale_colour_hue(l=70, c=150)
    d + scale_colour_hue(l=80, c=150)
    
    # Change range of hues used
    d + scale_colour_hue(h=c(0, 90))
    d + scale_colour_hue(h=c(90, 180))
    d + scale_colour_hue(h=c(180, 270))
    d + scale_colour_hue(h=c(270, 360))
    
    # Vary opacity
    # (only works with pdf, quartz and cairo devices)
    d <- ggplot(dsamp, aes(carat, price, colour = clarity))
    d + geom_point(alpha = 0.9)
    d + geom_point(alpha = 0.5)
    d + geom_point(alpha = 0.2)
  }
})



ScaleBrewer <- proto(ScaleColour, expr={
  doc <- TRUE

  new <- function(., name=NULL, palette=1, type="qual", na.colour  = "grey80", limits=NULL, breaks = NULL, labels=NULL, formatter = identity, variable, legend = TRUE) {
    b_and_l <- check_breaks_and_labels(breaks, labels)
    .$proto(name=name, palette=palette, type=type, .input=variable, .output=variable, .labels = b_and_l$labels, breaks = b_and_l$breaks, limits= limits, formatter = formatter, legend = legend, na.colour = na.colour)
  }
  aliases <- c("scale_color_brewer")

  output_set <- function(.) {
    missing <- is.na(.$input_set())
    n <- sum(!missing)
    
    palette <- RColorBrewer::brewer.pal(n, .$pal_name())[1:n]
    missing_colour(palette, missing, .$na.colour)
  }

  pal_name <- function(.) {
    if (is.character(.$palette)) {
      if (!.$palette %in% RColorBrewer:::namelist) {
        warning("Unknown palette ", .$palette)
        .$palette <- "Greens"
      }
      return(.$palette)
    }
    
    switch(.$type, 
      div = RColorBrewer:::divlist, 
      qual = RColorBrewer:::quallist, 
      seq = RColorBrewer:::seqlist
    )[.$palette]
  }
  
  max_levels <- function(.) {
    RColorBrewer:::maxcolors[RColorBrewer:::namelist == .$pal_name()]
  }

  # Documentation -----------------------------------------------

  objname <- "brewer"
  desc <- "Sequential, diverging and qualitative colour scales from colorbrewer.org"
  
  desc_params <- list(
    palette = "Either numeric or character.  If numeric, selects the nth palette of type type.  If character, selects the named palette.  Get a complete list of all parameters by running \\code{RColorBrewer::display.brewer.all(n=8, exact.n=FALSE)}",
    type = "Type of scale.  One of 'div' (diverging), 'qual' (qualitative, the default), 'seq' (sequential), or 'all' (all).  Only used when palette is numeric." 
  )
  
  details <- "<p>See <a href='http://colorbrewer.org'>colorbrewer.org</a> for more info</p>"
  common <- c("colour", "fill")

  icon <- function(.) {
    rectGrob(c(0.1, 0.3, 0.5, 0.7, 0.9), width=0.21, 
      gp=gpar(fill=RColorBrewer::brewer.pal(5, "PuOr"), col=NA)
    )
  }
  
  examples <- function(.) {
    dsamp <- diamonds[sample(nrow(diamonds), 1000), ]
    (d <- qplot(carat, price, data=dsamp, colour=clarity))
    
    # Change scale label
    d + scale_colour_brewer()
    d + scale_colour_brewer("clarity")
    d + scale_colour_brewer(expression(clarity[beta]))

    # Select brewer palette to use, see ?brewer.pal for more details
    d + scale_colour_brewer(type="seq")
    d + scale_colour_brewer(type="seq", palette=3)

    RColorBrewer::display.brewer.all(n=8, exact.n=FALSE)

    d + scale_colour_brewer(palette="Blues")
    d + scale_colour_brewer(palette="Set1")
    
    # scale_fill_brewer works just the same as 
    # scale_colour_brewer but for fill colours
    ggplot(diamonds, aes(x=price, fill=cut)) + 
      geom_histogram(position="dodge", binwidth=1000) + 
      scale_fill_brewer()
    
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-discrete-grey.r"
ScaleGrey <- proto(ScaleColour, expr={
  doc <- TRUE
  common <- c("colour", "fill")
  aliases <- "scale_color_grey"

  new <- function(., name=NULL, variable, start = 0.2, end = 0.8, limits=NULL, breaks = NULL, labels=NULL, formatter = identity, legend = TRUE) {
    
    b_and_l <- check_breaks_and_labels(breaks, labels)
    
    .$proto(name=name, .input=variable, .output=variable, start=start, end=end, limits = limits, breaks = b_and_l$breaks, .labels = b_and_l$labels, formatter=formatter, legend = legend)
  }

  output_set <- function(.) {
    grey.colors(length(.$input_breaks()), start = .$start, end = .$end)
  }

  max_levels <- function(.) Inf

  # Documentation -----------------------------------------------

  objname <- "grey"
  desc <- "Sequential grey colour scale"
  details <- "<p>Based on ?gray.colors</p>"
  
  desc_params <- list(
    "start" = "starting grey colour (between 0 and 1)",
    "end" = "ending grey colour (between 0 and 1)"
  )

  icon <- function(.) {
    rectGrob(c(0.1, 0.3, 0.5, 0.7, 0.9), width=0.21, 
      gp=gpar(fill=gray(seq(0, 1, length=5)), col=NA)
    )
  }
  
  examples <- function(.) {
    p <- qplot(mpg, wt, data=mtcars, colour=factor(cyl)) 
    p + scale_colour_grey()
    p + scale_colour_grey(end = 0)
    
    # You may want to turn off the pale grey background with this scale
    p + scale_colour_grey() + theme_bw()
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-discrete-position.r"
# Basically pretends that discrete values are consecture integers starting at
# one.  

ScaleDiscretePosition <- proto(ScaleDiscrete, {
  doc <- TRUE
  
  objname <- "discrete"
  my_name <- function(., prefix = TRUE) {
    if (prefix) "scale_discrete" else "discrete"
  }
    
  myName <- function(.) "ScaleDiscretePosition"
  
  common <- c("x", "y", "z")
  desc <- "Discrete position scale"

  cont_domain <- c(NA, NA)
  
  # Always drops if .$drop is TRUE - the drop argument is used when
  # scales = "free" so you don't have to also specify drop = T here.
  train <- function(., x, drop = FALSE) {
    if (is.discrete(x)) {
      .$.domain <- discrete_range(.$.domain, x, drop = drop || .$drop)
    } else {
      .$cont_domain <- range(.$cont_domain, x, na.rm = TRUE)
    }
  }
  
  map <- function(., values) {
    if (is.discrete(values)) {
      .$check_domain()
      seq_along(.$input_set())[match(as.character(values), .$input_set())]
    } else {
      values
    }
  }
  
  
  output_set <- function(.) range(seq_along(.$input_set()), .$cont_domain, na.rm = TRUE)
  output_expand <- function(.) {
    disc_range <- c(1, length(.$input_set()))
    disc <- expand_range(disc_range, 0, .$.expand[2], .$.expand[2])
    cont <- expand_range(.$output_set(), .$.expand[1], 0, .$.expand[2])
    
    c(min(disc[1], cont[1]), max(disc[2], cont[2]))
  }
  
  
  examples <- function(.) {
    qplot(cut, data=diamonds, stat="bin")
    qplot(cut, data=diamonds, geom="bar")
    
    # The discrete position scale is added automatically whenever you
    # have a discrete position.
    
    (d <- qplot(cut, clarity, data=subset(diamonds, carat > 1), geom="jitter"))
    
    d + scale_x_discrete("Cut")
    d + scale_x_discrete("Cut", labels = c("Fair" = "F","Good" = "G",
      "Very Good" = "VG","Perfect" = "P","Ideal" = "I"))
    
    d + scale_y_discrete("Clarity")
    d + scale_x_discrete("Cut") + scale_y_discrete("Clarity")

    # Use limits to adjust the which levels (and in what order)
    # are displayed
    d + scale_x_discrete(limits=c("Fair","Ideal"))

    # you can also use the short hand functions xlim and ylim
    d + xlim("Fair","Ideal", "Good")
    d + ylim("I1", "IF")
    
    # See ?reorder to reorder based on the values of another variable
    qplot(manufacturer, cty, data=mpg)
    qplot(reorder(manufacturer, cty), cty, data=mpg)
    qplot(reorder(manufacturer, displ), cty, data=mpg)
    
    # Use abbreviate as a formatter to reduce long names
    qplot(reorder(manufacturer, cty), cty, data=mpg) +  
      scale_x_discrete(formatter = "abbreviate")
    
  }
  
  
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-identity.r"
ScaleIdentity <- proto(ScaleDiscrete, {  
  doc <- TRUE
  common <- c("alpha", "colour","fill","size","shape","linetype")
  aliases <- "scale_color_identity"
  new <- function(., name=NULL, breaks=NULL, labels=NULL, formatter = NULL, legend = TRUE, variable="x") {
    
    b_and_l <- check_breaks_and_labels(breaks, labels)
#    legend <- legend && !is.null(b_and_l$labels)
    
    .$proto(name=name, breaks=b_and_l$breaks, .labels=b_and_l$labels, .input=variable, .output=variable, formatter = formatter, legend = legend)
  }

  train <- function(., data, drop = FALSE) {
    .$breaks <- union(.$breaks, unique(data))
    if (is.numeric(data)) {
      if (all(is.na(data)) || all(!is.finite(data))) return()
      .$.domain <- range(data, .$.domain, na.rm=TRUE, finite=TRUE)
    } else {
      .$.domain <- discrete_range(.$.domain, data, drop = drop)
    }
  }

  map_df <- function(., data) {
    if (!all(.$input() %in% names(data))) return(data.frame())
    data[, .$input(), drop=FALSE]
  }
  output_breaks <- function(.) .$breaks
  labels <- function(.) {
    if (!is.null(.$.labels)) return(as.list(.$.labels))

    if (is.null(get("formatter", .))) {
      f <- match.fun(identity)
    } else {
      f <- match.fun(get("formatter", .))
    }
    as.list(f(.$input_breaks()))
  }

  # Documentation -----------------------------------------------

  objname <- "identity"
  desc <- "Use values without scaling"
  icon <- function(.) textGrob("f(x) = x", gp=gpar(cex=1.2))
  
  examples <- function(.) {
    colour <- c("red", "green", "blue", "yellow")
    qplot(1:4, 1:4, fill = colour, geom = "tile")
    qplot(1:4, 1:4, fill = colour, geom = "tile") + scale_fill_identity()
    
    # To get a legend, you also need to supply the labels to
    # be used on the legend
    qplot(1:4, 1:4, fill = colour, geom = "tile") +
      scale_fill_identity("trt", labels = letters[1:4], breaks = colour)
    
    # cyl scaled to appropriate size
    qplot(mpg, wt, data = mtcars, size = cyl)

    # cyl used as point size
    qplot(mpg, wt, data = mtcars, size = cyl) + scale_size_identity()
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-linetype.r"
ScaleLinetypeDiscrete <- proto(ScaleDiscrete, expr={
  doc <- TRUE
  common <- NULL
  .input <- .output <- "linetype"
  aliases <- "scale_linetype"

  output_set <- function(.) c("solid", "22", "42", "44", "13", "1343", "73", "2262", "12223242", "F282", "F4448444", "224282F2", "F1")[seq_along(.$input_set())]
  max_levels <- function(.) 12
  
  detail <- "<p>Default line types based on a set supplied by Richard Pearson, University of Manchester.</p>"
  
  # Documentation -----------------------------------------------

  objname <- "linetype_discrete"
  desc <- "Scale for line patterns"
  
  icon <- function(.) {
    gTree(children=gList(
      segmentsGrob(0, 0.25, 1, 0.25, gp=gpar(lty=1)),
      segmentsGrob(0, 0.50, 1, 0.50, gp=gpar(lty=2)),
      segmentsGrob(0, 0.75, 1, 0.75, gp=gpar(lty=3))
    ))
  }
  
  examples <- function() {
    ec_scaled <- data.frame(
      date = economics$date, 
      rescaler(economics[, -(1:2)], "range")
    )
    ecm <- melt(ec_scaled, id = "date")
    
    qplot(date, value, data=ecm, geom="line", group=variable)
    qplot(date, value, data=ecm, geom="line", linetype=variable)
    qplot(date, value, data=ecm, geom="line", colour=variable)
    
    # See scale_manual for more flexibility
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-manual.r"
ScaleManual <- proto(ScaleDiscrete, {  
  doc <- TRUE
  common <- c("colour","fill","size","shape","linetype")
  aliases <- "scale_color_manual"
  values <- c()
  
  new <- function(., name=NULL, values=NULL, variable="x", limits = NULL, breaks = NULL, labels = NULL, formatter = identity, legend = TRUE) {
    b_and_l <- check_breaks_and_labels(breaks, labels)
    
    .$proto(name=name, values=values, .input=variable, .output=variable, limits = limits, breaks = b_and_l$breaks, .labels = b_and_l$labels, formatter = formatter, legend = legend)
  }

  map <- function(., values) {
    .$check_domain()

    values <- as.character(values)
    values[is.na(values)] <- "NA"
    input <- .$input_set()
    input[is.na(input)] <- "NA"
    
    if (.$has_names()) {
      values[!values %in% input] <- NA
      .$output_set()[values]
    } else {
      
      .$output_set()[match(values, input)]
    }
  }

  has_names <- function(.) !is.null(names(.$output_set()))

  input_breaks <- function(.) nulldefault(.$breaks, .$input_set())
  output_breaks <- function(.) .$map(.$input_breaks())

  output_set <- function(.) .$values
  labels <- function(.) {
    as.list(.$.labels %||% .$input_breaks())
  }

  # Documentation -----------------------------------------------

  objname <- "manual"
  desc <- "Create your own discrete scale"
  icon <- function(.) textGrob("DIY", gp=gpar(cex=1.2))
  
  examples <- function(.) {
    p <- qplot(mpg, wt, data = mtcars, colour = factor(cyl))

    p + scale_colour_manual(values = c("red","blue", "green"))
    p + scale_colour_manual(
      values = c("8" = "red","4" = "blue","6" = "green"))
    
    # As with other scales you can use breaks to control the appearance
    # of the legend
    cols <- c("8" = "red","4" = "blue","6" = "darkgreen", "10" = "orange")
    p + scale_colour_manual(values = cols)
    p + scale_colour_manual(values = cols, breaks = c("4", "6", "8"))
    p + scale_colour_manual(values = cols, breaks = c("8", "6", "4"))
    p + scale_colour_manual(values = cols, breaks = c("4", "6", "8"),
      labels = c("four", "six", "eight"))
    
    # And limits to control the possible values of the scale
    p + scale_colour_manual(values = cols, limits = c("4", "8"))
    p + scale_colour_manual(values = cols, limits = c("4", "6", "8", "10"))
    
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-shape.r"
ScaleShapeDiscrete <- proto(ScaleDiscrete, expr={
  doc <- TRUE
  common <- NULL
  .input <- .output <- "shape"
  desc <- "Point glyph shapes"
  solid <- TRUE
  aliases <- c("scale_shape")
  

  new <- function(., name=NULL, solid=TRUE, limits = NULL, breaks = NULL, labels = NULL, formatter = identity, legend = TRUE) {
    
    b_and_l <- check_breaks_and_labels(breaks, labels)
    .$proto(name=name, solid=solid, limits = limits, breaks = b_and_l$breaks, .labels = b_and_l$labels, formatter = formatter, legend = legend)
  }
  
  output_set <- function(.) {
    (if (.$solid) {
      c(16, 17, 15, 3, 7, 8)
    } else {
      c(1, 2, 0, 3, 7, 8)
    })[1:length(.$input_set())]
  }

  max_levels <- function(.) 6
  
  # Documentation -----------------------------------------------
  objname <- "shape_discrete"
  desc <- "Scale for shapes, aka glyphs"
  icon <- function(.) {
    gTree(children=gList(
      circleGrob(0.7, 0.7, r=0.1),
      segmentsGrob(0.2, 0.3, 0.4, 0.3),
      segmentsGrob(0.3, 0.2, 0.3, 0.4),
      polygonGrob(c(0.2, 0.2, 0.4, 0.4), c(0.8, 0.6, 0.6, 0.8)),
      polygonGrob(c(0.6, 0.7, 0.8), c(0.2, 0.4, 0.2))
    ))
  }
  
  examples <- function(.) {
    dsmall <- diamonds[sample(nrow(diamonds), 100), ]
    
    (d <- qplot(carat, price, data=dsmall, shape=cut))
    d + scale_shape(solid = TRUE) # the default
    d + scale_shape(solid = FALSE)
    d + scale_shape(name="Cut of diamond")
    d + scale_shape(name="Cut of\ndiamond")
    
    # To change order of levels, change order of 
    # underlying factor
    levels(dsmall$cut) <- c("Fair", "Good", "Very Good", "Premium", "Ideal")

    # Need to recreate plot to pick up new data
    qplot(price, carat, data=dsmall, shape=cut)

    # Or for short:
    d %+% dsmall
    
  }
}) 
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scale-size.r"
ScaleSizeContinuous <- proto(ScaleContinuous, expr={
  doc <- TRUE
  common <- NULL
  .input <- .output  <- "size"
  aliases <- c("scale_area", "scale_size")
  
  new <- function(., name=NULL, limits=NULL, breaks=NULL, labels=NULL, trans = NULL, to = c(1, 6), legend = TRUE) {
    
    b_and_l <- check_breaks_and_labels(breaks, labels)
    
    .super$new(., name=name, limits=limits, breaks=b_and_l$breaks, labels=b_and_l$labels, trans=trans, variable = "size", to = to, legend = legend)
  }
  
  map <- function(., values) {
    rescale(values, .$to, .$input_set())
  }
  output_breaks <- function(.) .$map(.$input_breaks())
  
  objname <- "size_continuous"
  desc <- "Size scale for continuous variable"
  seealso <- list(
    "scale_manual" = "for sizing discrete variables"
  )
  desc_params <- list(
    "to" = "a numeric vector of length 2 that specifies the minimum and maximum size of the plotting symbol after transformation."
  )
  
  icon <- function(.) {
    pos <- c(0.15, 0.3, 0.5, 0.75)
    circleGrob(pos, pos, r=(c(0.1, 0.2, 0.3, 0.4)/2.5), gp=gpar(fill="grey50", col=NA))
  }
  
  examples <- function(.) {
    (p <- qplot(mpg, cyl, data=mtcars, size=cyl))
    p + scale_size("cylinders")
    p + scale_size("number\nof\ncylinders")
    
    p + scale_size(to = c(0, 10))
    p + scale_size(to = c(1, 2))

    # Map area, instead of width/radius
    # Perceptually, this is a little better
    p + scale_area()
    p + scale_area(to = c(1, 25))
    
    # Also works with factors, but not a terribly good
    # idea, unless your factor is ordered, as in this example
    qplot(mpg, cyl, data=mtcars, size=factor(cyl))
    
    # To control the size mapping for discrete variable, use 
    # scale_size_manual:
    last_plot() + scale_size_manual(values=c(2,4,6))
    
  }
  
})

ScaleSizeDiscrete <- proto(ScaleDiscrete, expr={
  common <- NULL
  objname <- "size_discrete"
  .input <- .output <- "size"
  desc <- "Size scale for discrete variables"
  doc <- FALSE

  max_levels <- function(.) Inf
  output_set <- function(.) seq_along(.$input_set())
  
}) 
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/scales-.r"
# Scales object encapsultes multiple scales.
# All input and output done with data.frames to facilitate 
# multiple input and output variables

Scales <- proto(Scale, expr={
  objname <- "scales"
  
  .scales <- list()
  # Should this scale produce a legend?
  legend <- TRUE
  
  n <- function(.) length(.$.scales)
  
  add <- function(., scale) {
    # Remove old scale if it exists
    old <- .$find(scale$output())
    .$.scales[old] <- NULL
    
    # Add new scale
    .$.scales <- append(.$.scales, scale)
  }

  clone <- function(.) {
    s <- Scales$new()
    s$add(lapply(.$.scales, function(x) x$clone()))
    s
  }
  
  find <- function(., output) {
    out <- sapply(.$.scales, function(x) any(output %in% x$output()))
    if (length(out) == 0) return(logical(0))
    out
  }


  
  get_scales <- function(., output, scales=FALSE) {
    scale <- .$.scales[.$find(output)]
    if (length(scale) == 0) return(Scales$new())
    if (scales || length(scale) > 1) {
      .$proto(.scales = scale)
    } else {
      scale[[1]]
    }
  }
  
  
  has_scale <- function(., output) {
    any(.$find(output))
  }
  
  get_trained_scales <- function(.) {
    Filter(function(x) x$trained(), .$.scales)
  }
  
  legend_desc <- function(., theme) {
    # Loop through all scales, creating a list of titles, and a list of keys
    keys <- titles <- vector("list", .$n())
    hash <- character(.$n())
    
    for(i in seq_len(.$n())) {
      scale <- .$.scales[[i]]
      if (!scale$legend) next
      if (is.null(scale$.domain) && is.null(scale$limits)) next
      
      # Figure out legend title
      output <- scale$output()
      titles[[i]] <- scale$name %||% theme$labels[[output]]
      
      key <- data.frame(
        scale$output_breaks(), I(scale$labels()))
      names(key) <- c(output, ".label")
      
      keys[[i]] <- key
      hash[i] <- digest::digest(list(titles[[i]], key$.label))
    }
    
    empty <- sapply(titles, is.null)
    
    list(titles = titles[!empty], keys = keys[!empty], hash = hash[!empty])

  }
  
  position_scales <- function(.) {
    .$get_scales(c("x","y","z"), TRUE)
  }
  
  non_position_scales <- function(.) {
    out <- setdiff(.$output(), c("x", "y", "z"))
    .$get_scales(out, TRUE)
  }
  
  output <- function(.) {
    sapply(.$.scales, function(scale) scale$output())
  }

  input <- function(.) {
    sapply(.$.scales, function(scale) scale$input())
  }
  
  # Train scale from a data frame
  train_df <- function(., df, drop = FALSE) {
    if (empty(df)) return() 

    lapply(.$.scales, function(scale) {
      scale$train_df(df, drop)
    })
  }
  
  # Map values from a data.frame. Returns data.frame
  map_df <- function(., df) {
    if (length(.$.scales) == 0) return(df)
    
    oldcols <- df[!(names(df) %in% .$input())]
    
    mapped <- lapply(.$.scales, function(scale) scale$map_df(df))
    mapped <- mapped[!sapply(mapped, empty)]
    
    if (length(mapped) > 0) {
      data.frame(mapped, oldcols)
    } else {
      oldcols
    }
  }
  
  # Transform values to cardinal representation
  transform_df <- function(., df) {
    if (length(.$.scales) == 0) return(df)
    if (empty(df)) return(data.frame())
    transformed <- compact(lapply(.$.scales, function(scale) {
      scale$transform_df(df)
    }))
    
    cunion(as.data.frame(transformed), df)
  }
  
  # Add default scales.
  # Add default scales to a plot.
  # 
  # Called during final construction to ensure that all aesthetics have 
  # a scale
  add_defaults <- function(., data, aesthetics, env) {
    if (is.null(aesthetics)) return()
    names(aesthetics) <- laply(names(aesthetics), aes_to_scale)
    
    new_aesthetics <- setdiff(names(aesthetics), .$input())
    # No new aesthetics, so no new scales to add
    if(is.null(new_aesthetics)) return()
    
    # Determine variable type for each column -------------------------------
    vartype <- function(x) {
      if (inherits(x, "Date")) return("date")
      if (inherits(x, "POSIXt")) return("datetime")
      if (is.numeric(x)) return("continuous")
      
      "discrete"
    }

    datacols <- tryapply(
      aesthetics[new_aesthetics], eval, 
      envir=data, enclos=env
    )
    new_aesthetics <- intersect(new_aesthetics, names(datacols))
    if (length(datacols) == 0) return()
    
    vartypes <- sapply(datacols, vartype)
    
    # Work out scale names
    scale_name_type <- paste("scale", new_aesthetics, vartypes, sep="_")

    for(i in 1:length(new_aesthetics)) {
      if (exists(scale_name_type[i])) {
        scale <- get(scale_name_type[i])()
        .$add(scale)
      }
    }
    
  }
  
  pprint <- function(., newline=TRUE) {
    clist <- function(x) paste(x, collapse=",")
    
    cat("Scales:   ", clist(.$input()), " -> ", clist(.$output()), sep="")
    if (newline) cat("\n") 
  }

})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-.r"
Stat <- proto(TopLevel, expr={
  objname <- "" 
  desc <- ""

  # Should the values produced by the statistic also be transformed
  # in the second pass when recently added statistics are trained to 
  # the scales
  retransform <- TRUE
  
  default_geom <- function(.) Geom
  default_aes <- function(.) aes()
  default_pos <- function(.) .$default_geom()$default_pos()
  required_aes <- c()
  
  aesthetics <- list()
  calculate <- function(., data, scales, ...) {}

  calculate_groups <- function(., data, scales, ...) {
    if (empty(data)) return(data.frame())
    
    force(data)
    force(scales)

    # # Alternative approach: cleaner, but much slower
    # # Compute statistic for each group
    # stats <- ddply(data, "group", function(group) {
    #   .$calculate(group, scales, ...) 
    # })
    # stats$ORDER <- seq_len(nrow(stats))
    # 
    # # Combine statistics with original columns
    # unique <- ddply(data, .(group), uniquecols)
    # stats <- merge(stats, unique, by = "group")
    # stats[stats$ORDER, ]
    
    groups <- split(data, data$group)
    stats <- lapply(groups, function(group) 
      .$calculate(data = group, scales = scales, ...))
    
    stats <- mapply(function(new, old) {
      if (empty(new)) return(data.frame())
      unique <- uniquecols(old)
      missing <- !(names(unique) %in% names(new))
      cbind(
        new, 
        unique[rep(1, nrow(new)), missing,drop=FALSE]
      )
    }, stats, groups, SIMPLIFY=FALSE)

    do.call(rbind.fill, stats)
  }


  pprint <- function(., newline=TRUE) {
    cat("stat_", .$objname ,": ", sep="") # , clist(.$parameters())
    if (newline) cat("\n")
  }
  
  parameters <- function(.) {
    params <- formals(get("calculate", .))
    params[setdiff(names(params), c(".","data","scales"))]
  }
  
  class <- function(.) "stat"
  
  new <- function(., mapping=aes(), data=NULL, geom=NULL, position=NULL, ...){
    do.call("layer", list(mapping=mapping, data=data, geom=geom, stat=., position=position, ...))
  }

})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-bin.r"
# Bin data
# This function powers \code{\link{stat_bin}}
#
# @keyword internal
bin <- function(x, weight=NULL, binwidth=NULL, origin=NULL, breaks=NULL, range=NULL, width=0.9, drop = FALSE, right = TRUE) {
  
  if (length(na.omit(x)) == 0) return(data.frame())
  if (is.null(weight))  weight <- rep(1, length(x))
  weight[is.na(weight)] <- 0

  if (is.null(range))    range <- range(x, na.rm = TRUE, finite=TRUE)
  if (is.null(binwidth)) binwidth <- diff(range) / 30

  if (is.integer(x)) {
    bins <- x
    x <- sort(unique(bins))
    width <- width    
  } else if (diff(range) == 0) {
    width <- width
    bins <- x
  } else { # if (is.numeric(x)) 
    if (is.null(breaks)) {
      if (is.null(origin)) {
        breaks <- fullseq(range, binwidth, pad = TRUE)        
      } else {
        breaks <- seq(origin, max(range) + binwidth, binwidth)
      }
    }
    
    # Adapt break fuzziness from base::hist - this protects from floating
    # point rounding errors
    diddle <- 1e-07 * stats::median(diff(breaks))
    if (right) {
      fuzz <- c(-diddle, rep.int(diddle, length(breaks) - 1))
    } else {
      fuzz <- c(rep.int(-diddle, length(breaks) - 1), diddle) 
    }
    fuzzybreaks <- sort(breaks) + fuzz
    
    bins <- cut(x, fuzzybreaks, include.lowest=TRUE, right = right)
    left <- breaks[-length(breaks)]
    right <- breaks[-1]
    x <- (left + right)/2
    width <- diff(breaks)
  }

  results <- data.frame(
    count = as.numeric(tapply(weight, bins, sum, na.rm=TRUE)),
    x = x,
    width = width
  )
  
  if (sum(results$count, na.rm = TRUE) == 0) {
    return(results)
  }
  
  res <- within(results, {
    count[is.na(count)] <- 0
    density <- count / width / sum(abs(count), na.rm=TRUE)
    ncount <- count / max(abs(count), na.rm=TRUE)
    ndensity <- density / max(abs(density), na.rm=TRUE)
  })
  if (drop) res <- subset(res, count > 0)
  res
}

# Generate sequence of fixed size intervals covering range
# All locations are multiples of size
# 
# @arguments range
# @arguments interval size
# @keyword internal
# @seealso \code{\link{reshape}{round_any}}
fullseq <- function(range, size, pad = FALSE) {
  if (diff(range) < 1e-6) return(c(range[1] - size / 2, range[1] + size / 2))
  
  x <- seq(
    round_any(range[1], size, floor), 
    round_any(range[2], size, ceiling), 
    by=size
  )
  
  if (pad) {
    # Add extra bin on bottom and on top, to guarantee that we cover complete
    # range of data, whether right = T or F
    c(min(x) - size, x, max(x) + size)
  } else {
    x
  }
  
}

StatBin <- proto(Stat, {
  informed <- FALSE
  
  calculate_groups <- function(., data, ...) {
    .$informed <- FALSE
    .super$calculate_groups(., data, ...)
  }
  
  calculate <- function(., data, scales, binwidth=NULL, origin=NULL, breaks=NULL, width=0.9, drop = FALSE, right = TRUE, ...) {
    range <- scales$x$output_set()

    if (is.null(breaks) && is.null(binwidth) && !is.integer(data$x) && !.$informed) {
      message("stat_bin: binwidth defaulted to range/30. Use 'binwidth = x' to adjust this.")
      .$informed <- TRUE
    }
    
    bin(data$x, data$weight, binwidth=binwidth, origin=origin, breaks=breaks, range=range, width=width, drop = drop, right = right)
  }

  objname <- "bin" 
  desc <- "Bin data"
  icon <- function(.) GeomHistogram$icon()
  desc_params <- list(
    binwidth = "Bin width to use. Defaults to 1/30 of the range of the data.",
    breaks = "Actual breaks to use.  Overrides bin width and origin",
    origin = "Origin of first bin",
    width = "Width of bars when used with categorical data",
    right = "Should intervals be closed on the right (a, b], or not [a, b)",
    drop = "If TRUE, remove all bins with zero counts"
  )
  desc_outputs <- list(
    count = "number of points in bin",
    density = "density of points in bin, scaled to integrate to 1",
    ncount = "count, scaled to maximum of 1",
    ndensity = "density, scaled to maximum of 1"
  )
  details <- "<p>Missing values are currently silently dropped.</p>"
  
  default_aes <- function(.) aes(y = ..count..)
  required_aes <- c("x")
  default_geom <- function(.) GeomBar
  
  examples <- function(.) {
    simple <- data.frame(x = rep(1:10, each = 2))
    base <- ggplot(simple, aes(x))
    # By default, right = TRUE, and intervals are of the form (a, b]
    base + stat_bin(binwidth = 1, drop = FALSE, right = TRUE, col = "black")
    # If right = FALSE intervals are of the form [a, b)
    base + stat_bin(binwidth = 1, drop = FALSE, right = FALSE, col = "black")
    
    m <- ggplot(movies, aes(x=rating))
    m + stat_bin()
    m + stat_bin(binwidth=0.1)
    m + stat_bin(breaks=seq(4,6, by=0.1))
    # See geom_histogram for more histogram examples
    
    # To create a unit area histogram, use aes(y = ..density..)
    (linehist <- m + stat_bin(aes(y = ..density..), binwidth=0.1,
      geom="line", position="identity"))
    linehist + stat_density(colour="blue", fill=NA)
    
    # Also works with categorical variables
    ggplot(movies, aes(x=mpaa)) + stat_bin()
    qplot(mpaa, data=movies, stat="bin")
    
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-bin2d.r"
StatBin2d <- proto(Stat, {
  objname <- "bin2d" 
  desc <- "Bin 2d plane into rectangles"
  default_aes <- function(.) aes(fill = ..count..)
  required_aes <- c("x", "y")
  default_geom <- function(.) GeomRect

  seealso <- list(
    "stat_binhex" = "For hexagonal binning"
  )
  
  calculate <- function(., data, scales, binwidth = NULL, bins = 30, breaks = NULL, origin = NULL, drop = TRUE, ...) {
    
    range <- list(
      x = scales$x$output_set(),
      y = scales$y$output_set()
    )
    
    # Determine binwidth, if omitted
    if (is.null(binwidth)) {
      binwidth <- c(NA, NA)
      if (is.integer(data$x)) {
        binwidth[1] <- 1
      } else {
        binwidth[1] <- diff(range$x) / bins
      }
      if (is.integer(data$y)) {
        binwidth[2] <- 1
      } else {
        binwidth[2] <- diff(range$y) / bins
      }      
    }
    stopifnot(is.numeric(binwidth))
    stopifnot(length(binwidth) == 2)
    
    # Determine breaks, if omitted
    if (is.null(breaks)) {
      if (is.null(origin)) {
        breaks <- list(
          fullseq(range$x, binwidth[1]),
          fullseq(range$y, binwidth[2])
        )
      } else {
        breaks <- list(
          seq(origin[1], max(range$x) + binwidth[1], binwidth[1]),
          seq(origin[2], max(range$y) + binwidth[2], binwidth[2])
        )
      }
    }
    stopifnot(is.list(breaks))
    stopifnot(length(breaks) == 2)
    stopifnot(all(sapply(breaks, is.numeric)))
    names(breaks) <- c("x", "y")
    
    xbin <- cut(data$x, sort(breaks$x), include.lowest=TRUE)
    ybin <- cut(data$y, sort(breaks$y), include.lowest=TRUE)
    
    if (is.null(data$weight)) data$weight <- 1
    
    counts <- as.data.frame(
      xtabs(weight ~ xbin + ybin, data), responseName="count")
    if (drop) counts <- subset(counts, count > 0)
    
    within(counts,{
      xint <- as.numeric(xbin)
      xmin <- breaks$x[xint]
      xmax <- breaks$x[xint + 1]

      yint <- as.numeric(ybin)
      ymin <- breaks$y[yint]
      ymax <- breaks$y[yint + 1]
  
      density <- count / sum(count, na.rm=TRUE)
    })
  }
  

  examples <- function(.) {
    d <- ggplot(diamonds, aes(carat, price))
    d + stat_bin2d()
    d + geom_bin2d()
    
    # You can control the size of the bins by specifying the number of
    # bins in each direction:
    d + stat_bin2d(bins = 10)
    d + stat_bin2d(bins = 30)
    
    # Or by specifying the width of the bins
    d + stat_bin2d(binwidth = c(1, 1000))
    d + stat_bin2d(binwidth = c(.1, 500))
    
    # Or with a list of breaks
    x <- seq(min(diamonds$carat), max(diamonds$carat), by = 0.1)
    y <- seq(min(diamonds$price), max(diamonds$price), length = 50)
    d + stat_bin2d(breaks = list(x = x, y = y))
    
    # With qplot
    qplot(x, y, data = diamonds, geom="bin2d", 
      xlim = c(4, 10), ylim = c(4, 10))
    qplot(x, y, data = diamonds, geom="bin2d", binwidth = c(0.1, 0.1),
      xlim = c(4, 10), ylim = c(4, 10))
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-binhex.r"
StatBinhex <- proto(Stat, {
  objname <- "binhex"
  desc <- "Bin 2d plane into hexagons"
  
  calculate <- function(., data, scales, binwidth = NULL, bins = 30, na.rm = FALSE, ...) {
    try_require("hexbin")
    data <- remove_missing(data, na.rm, c("x", "y"), name="stat_hexbin")

    if (is.null(binwidth)) {
      binwidth <- c( 
        diff(scales$x$input_set()) / bins,
        diff(scales$y$input_set() ) / bins
      )
    }
    
    hexBin(data$x, data$y, binwidth)
  }
  
  seealso <- list(
    "stat_bin2d" = "For rectangular binning"
  )
  
  default_aes <- function(.) aes(fill = ..count..)
  required_aes <- c("x", "y")
  default_geom <- function(.) GeomHex
  
  examples <- function() {
    d <- ggplot(diamonds, aes(carat, price))
    d + stat_binhex()
    d + geom_hex()
    
    # You can control the size of the bins by specifying the number of
    # bins in each direction:
    d + stat_binhex(bins = 10)
    d + stat_binhex(bins = 30)
    
    # Or by specifying the width of the bins
    d + stat_binhex(binwidth = c(1, 1000))
    d + stat_binhex(binwidth = c(.1, 500))
    
    # With qplot
    qplot(x, y, data = diamonds, geom="hex", xlim = c(4, 10), ylim = c(4, 10))
    qplot(x, y, data = diamonds, geom="hex", xlim = c(4, 10), ylim = c(4, 10),
      binwidth = c(0.1, 0.1))
  }
  
})

# Bin 2d plane into hexagons
# Wrapper around \code{\link[hexbin]{hcell2xy}} that returns a data frame
# 
# @arguments x positions
# @arguments y positions
# @arguments numeric vector of length 2 giving binwidth in x and y directions
# @keyword internal
hexBin <- function(x, y, binwidth) {
  try_require("hexbin")
  
  # Convert binwidths into bounds + nbins
  xbnds <- c(
    round_any(min(x), binwidth[1], floor) - 1e-6, 
    round_any(max(x), binwidth[1], ceiling) + 1e-6
  )
  xbins <- diff(xbnds) / binwidth[1]

  ybnds <- c(
    round_any(min(y), binwidth[1], floor) - 1e-6, 
    round_any(max(y), binwidth[2], ceiling) + 1e-6
  )
  ybins <- diff(ybnds) / binwidth[2]
  
  # Call hexbin
  hb <- hexbin(
    x, xbnds = xbnds, xbins = xbins,  
    y, ybnds = ybnds, shape = ybins / xbins, 
  )
  
  # Convert to data frame
  data.frame(
    hcell2xy(hb), 
    count = hb@count, 
    density = hb@count / sum(hb@count, na.rm=TRUE)
  )
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-boxplot.r"
StatBoxplot <- proto(Stat, {
  objname <- "boxplot" 
  desc <- "Calculate components of box and whisker plot"
  desc_outputs <- list(
    "width" = "width of boxplot",
    "ymin" = "lower whisker = lower hinge - 1.5 * IQR",
    "lower" = "lower hinge, 25% quantile", 
    "middle" = "median, 50% quantile",
    "upper" = "upper hinge, 75% quantile",
    "ymax" = "upper whisker = upper hinge + 1.5 * IQR"
  )
  required_aes <- c("x", "y")
  
  icon <- function(.) GeomBoxplot$icon()
  default_geom <- function(.) GeomBoxplot
  
  calculate_groups <- function(., data, na.rm = FALSE, width = NULL, ...) {
    data <- remove_missing(data, na.rm, c("y", "weight"), name="stat_boxplot")
    data$weight <- nulldefault(data$weight, 1)
    width <- nulldefault(width, resolution(data$x) * 0.75)
        
    .super$calculate_groups(., data, na.rm = na.rm, width = width, ...)
  }
  
  calculate <- function(., data, scales, width=NULL, na.rm = FALSE, coef = 1.5, ...) {
    with(data, {    
      qs <- c(0, 0.25, 0.5, 0.75, 1)
      if (length(unique(weight)) != 1) {
        try_require("quantreg")
        stats <- as.numeric(coef(rq(y ~ 1, weights = weight, tau=qs)))
      } else {
        stats <- as.numeric(quantile(y, qs))
      }
      names(stats) <- c("ymin", "lower", "middle", "upper", "ymax")
    
      iqr <- diff(stats[c(2, 4)])
      
      outliers <- y < (stats[2] - coef * iqr) | y > (stats[4] + coef * iqr)
      if (any(outliers)) stats[c(1, 5)] <- range(y[!outliers], na.rm=TRUE)
      
      if (length(unique(x)) > 1) width <- diff(range(x)) * 0.9
    
      df <- as.data.frame(as.list(stats))
      df$outliers <- I(list(y[outliers]))

      transform(df,
        x = if (is.factor(x)) x[1] else mean(range(x)),
        width = width
      )
    })
  }
  
  examples <- function(.) {
    # See geom_boxplot for examples
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-contour.r"
StatContour <- proto(Stat, {
  calculate <- function(., data, scales, bins=NULL, binwidth=NULL, breaks = NULL, na.rm = FALSE, ...) {
    data <- remove_missing(data, na.rm, name = "stat_contour")

    # If no parameters set, use pretty bins
    if (is.null(bins) && is.null(binwidth) && is.null(breaks)) {
      breaks <- pretty(range(data$z), 10)
    }
    # If provided, use bins to calculate binwidth
    if (!is.null(bins)) {
      binwidth <- diff(range(data$z)) / bins
    }
    # If necessary, compute breaks from binwidth
    if (is.null(breaks)) {
      breaks <- fullseq(range(data$z), binwidth)
    }
    
    z <- tapply(data$z, data[c("x", "y")], identity)
    cl <- contourLines(
      x = sort(unique(data$x)), y = sort(unique(data$y)), z = z, 
      levels = breaks)  
    cl <- lapply(cl, as.data.frame)
    
    contour_df <- rbind.fill(cl)
    contour_df$piece <- rep(seq_along(cl), sapply(cl, nrow))
    contour_df$group <- paste(data$group[1], contour_df$piece, sep = "-")
    contour_df
  }

  objname <- "contour" 
  desc <- "Contours of 3d data"
  
  icon <- function(.) GeomContour$icon()
  
  default_geom <- function(.) GeomPath
  default_aes <- function(.) aes(order = ..level..)
  required_aes <- c("x", "y", "z")
  desc_outputs <- list(
    level = "z value of contour"
  )
  
  examples <- function(.) {
    # Generate data
    volcano3d <- melt(volcano)
    names(volcano3d) <- c("x", "y", "z")

    # Basic plot
    v <- ggplot(volcano3d, aes(x, y, z = z))
    v + stat_contour()

    # Setting bins creates evenly spaced contours in the range of the data
    v + stat_contour(bins = 2)
    v + stat_contour(bins = 10)
    
    # Setting binwidth does the same thing, parameterised by the distance
    # between contours
    v + stat_contour(binwidth = 2)
    v + stat_contour(binwidth = 5)
    v + stat_contour(binwidth = 10)
    v + stat_contour(binwidth = 2, size = 0.5, colour = "grey50") +
      stat_contour(binwidth = 10, size = 1)

    # Add aesthetic mappings
    v + stat_contour(aes(size = ..level..))
    v + stat_contour(aes(colour = ..level..))

    # Change scale
    v + stat_contour(aes(colour = ..level..), size = 2) + 
      scale_colour_gradient(low = "brown", high = "white")

    # Set aesthetics to fixed value
    v + stat_contour(colour = "red")
    v + stat_contour(size = 2, linetype = 4)

    # Try different geoms
    v + stat_contour(geom="polygon", aes(fill=..level..))
    v + geom_tile(aes(fill = z)) + stat_contour()
    
    # Use qplot instead
    qplot(x, y, z, data = volcano3d, geom = "contour")
    qplot(x, y, z, data = volcano3d, stat = "contour", geom = "path")
  }
})

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-density-2d.r"
StatDensity2d <- proto(Stat, {
  objname <- "density2d" 
  desc <- "Density estimation, 2D"
  
  default_geom <- function(.) GeomDensity2d
  default_aes <- function(.) aes(colour = "#3366FF", size = 0.5)
  required_aes <- c("x", "y")

  desc_outputs <- list(
    level = "Computed density"
  )
  desc_params <- list(
    contour = "If TRUE, contour the results of the 2d density estimation.",
    n = "number of grid points in each direction",
    "..." = "other arguments passed on to ?kde2d"
  )
  
  icon <- function(.) GeomDensity2d$icon()

  calculate <- function(., data, scales, na.rm = FALSE, contour = TRUE, n = 100, ...) {
    df <- data.frame(data[, c("x", "y")])
    df <- remove_missing(df, na.rm, name = "stat_density2d")

    dens <- safe.call(MASS::kde2d, c(df, n = n, ...))
    df <- with(dens, data.frame(expand.grid(x = x, y = y), z = as.vector(z)))
    df$group <- data$group[1]
    
    if (contour) {
      StatContour$calculate(df, scales, ...)      
    } else {
      names(df) <- c("x", "y", "density", "group")
      df$level <- 1
      df$piece <- 1
      df
    }
  }
  
  examples <- function(.) {
    m <- ggplot(movies, aes(x=rating, y=length)) + 
      geom_point() + 
      scale_y_continuous(limits=c(1, 500))
    m + geom_density2d()

    dens <- MASS::kde2d(movies$rating, movies$length, n=100)
    densdf <- data.frame(expand.grid(rating = dens$x, length = dens$y),
     z = as.vector(dens$z))
    m + geom_contour(aes(z=z), data=densdf)

    m + geom_density2d() + scale_y_log10()
    m + geom_density2d() + coord_trans(y="log10")
    
    m + stat_density2d(aes(fill = ..level..), geom="polygon")

    qplot(rating, length, data=movies, geom=c("point","density2d")) +     
      ylim(1, 500)
    
    # If you map an aesthetic to a categorical variable, you will get a
    # set of contours for each value of that variable
    qplot(rating, length, data = movies, geom = "density2d", 
      colour = factor(Comedy), ylim = c(0, 150))
    qplot(rating, length, data = movies, geom = "density2d", 
      colour = factor(Action), ylim = c(0, 150))
    qplot(carat, price, data = diamonds, geom = "density2d", colour = cut)
    
    # Another example ------
    d <- ggplot(diamonds, aes(carat, price)) + xlim(1,3)
    d + geom_point() + geom_density2d()
    
    # If we turn contouring off, we can use use geoms like tiles:
    d + stat_density2d(geom="tile", aes(fill = ..density..), contour = FALSE)
    last_plot() + scale_fill_gradient(limits=c(1e-5,8e-4))
    
    # Or points:
    d + stat_density2d(geom="point", aes(size = ..density..), contour = FALSE)
  }  
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-density.r"
StatDensity <- proto(Stat, {
  calculate <- function(., data, scales, adjust=1, kernel="gaussian", trim=FALSE, na.rm = FALSE, ...) {
    data <- remove_missing(data, na.rm, "x", name = "stat_density")
    
    n <- nrow(data)
    if (n < 3) return(data.frame())
    if (is.null(data$weight)) data$weight <- rep(1, n) / n

    range <- scales$x$output_set()
    xgrid <- seq(range[1], range[2], length=200)
    
    dens <- density(data$x, adjust=adjust, kernel=kernel, weight=data$weight, from=range[1], to=range[2])
    densdf <- as.data.frame(dens[c("x","y")])

    densdf$scaled <- densdf$y / max(densdf$y, na.rm = TRUE)
    if (trim) densdf <- subset(densdf, x > min(data$x, na.rm = TRUE) & x < max(data$x, na.rm = TRUE))
  
    densdf$count <- densdf$y * n
    rename(densdf, c(y = "density"))
  }

  objname <- "density" 
  desc <- "Density estimation, 1D"
  icon <- function(.) GeomDensity$icon()

  desc_params <- list(
    adjust = "see ?density for details",
    kernel = "kernel used for density estimation, see \\code{\\link{density}} for details"
  )
  desc_outputs <- list(
    density = "density estimate",
    count = "density * number of points - useful for stacked density plots",
    scaled = "density estimate, scaled to maximum of 1"
  )

  seealso <- list(
    stat_bin = "for the histogram",
    density = "for details of the algorithm used"
  )
  
  default_geom <- function(.) GeomArea
  default_aes <- function(.) aes(y = ..density.., fill=NA)
  required_aes <- c("x")
  

  examples <- function(.) {
    m <- ggplot(movies, aes(x=rating))
    m + geom_density()
    
    # Adjust parameters
    m + geom_density(kernel = "rectangular")
    m + geom_density(kernel = "biweight") 
    m + geom_density(kernel = "epanechnikov")
    m + geom_density(adjust=1/5) # Very rough
    m + geom_density(adjust=5) # Very smooth
    
    # Adjust aesthetics
    m + geom_density(aes(fill=factor(Drama)), size=2)
    # Scale so peaks have same height:
    m + geom_density(aes(fill=factor(Drama), y = ..scaled..), size=2)

    m + geom_density(colour="darkgreen", size=2)
    m + geom_density(colour="darkgreen", size=2, fill=NA)
    m + geom_density(colour="darkgreen", size=2, fill="green")
    
    # Change scales
    (m <- ggplot(movies, aes(x=votes)) + geom_density(trim = TRUE))
    m + scale_x_log10()
    m + coord_trans(x="log10")
    m + scale_x_log10() + coord_trans(x="log10")
    
    # Also useful with
    m + stat_bin()
    
    # Make a volcano plot
    ggplot(diamonds, aes(x = price)) + 
      stat_density(aes(ymax = ..density..,  ymin = -..density..), 
        fill = "grey50", colour = "grey50", 
        geom = "ribbon", position = "identity") + 
      facet_grid(. ~ cut) + 
      coord_flip()

    # Stacked density plots
    # If you want to create a stacked density plot, you need to use
    # the 'count' (density * n) variable instead of the default density
    
    # Loses marginal densities
    qplot(rating, ..density.., data=movies, geom="density", fill=mpaa, position="stack")
    # Preserves marginal densities
    qplot(rating, ..count.., data=movies, geom="density", fill=mpaa, position="stack")
    
    # You can use position="fill" to produce a conditional density estimate
    qplot(rating, ..count.., data=movies, geom="density", fill=mpaa, position="fill")

    # Need to be careful with weighted data
    m <- ggplot(movies, aes(x=rating, weight=votes))
    m + geom_histogram(aes(y = ..count..)) + geom_density(fill=NA)

    m <- ggplot(movies, aes(x=rating, weight=votes/sum(votes)))
    m + geom_histogram(aes(y=..density..)) + geom_density(fill=NA, colour="black")

    movies$decade <- round_any(movies$year, 10)
    m <- ggplot(movies, aes(x=rating, colour=decade, group=decade)) 
    m + geom_density(fill=NA)
    m + geom_density(fill=NA) + aes(y = ..count..)
    
    # Use qplot instead
    qplot(length, data=movies, geom="density", weight=rating)
    qplot(length, data=movies, geom="density", weight=rating/sum(rating))
  }  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-function.r"
StatFunction <- proto(Stat, {
  
  calculate <- function(., data, scales, fun, n=101, args = list(), ...) {
    range <- scales$x$output_set()
    xseq <- seq(range[1], range[2], length=n)
    
    data.frame(
      x = xseq,
      y = do.call(fun, c(list(xseq), args))
    )
  }

  objname <- "function" 
  desc <- "Superimpose a function "

  desc_params <- list(
    fun = "function to use",
    n = "number of points to interpolate along",
    args = "list of additional arguments to pass to fun"
  )
  
  desc_outputs <- list(
    x = "x's along a grid",
    y = "value of function evaluated at corresponding x"
  )

  default_geom <- function(.) GeomPath
  default_aes <- function(.) aes(y = ..y..)
  
  examples <- function(.) {
    x <- rnorm(100)
    base <- qplot(x, geom="density")
    base + stat_function(fun = dnorm, colour = "red")
    base + stat_function(fun = dnorm, colour = "red", arg = list(mean = 3))
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-identity.r"
StatIdentity <- proto(Stat, {
  objname <- "identity" 
  desc <- "Don't transform data"
  
  default_geom <- function(.) GeomPoint
  calculate_groups <- function(., data, scales, ...) data
  icon <- function(.) textGrob("f(x) = x", gp=gpar(cex=1.2))
  
  desc_outputs <- list()
  
  examples <- function(.) {
    # Doesn't do anything, so hard to come up a useful example
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-qq.r"
StatQq <- proto(Stat, {
  objname <- "qq" 
  desc <- "Calculation for quantile-quantile plot"

  desc_params <- list(
    quantiles = "Quantiles to compute and display",
    dist = "Distribution function to use, if x not specified",
    dparams = "Parameters for distribution function", 
    "..." = "Other arguments passed to distribution function"
  )
  
  default_geom <- function(.) GeomPoint
  default_aes <- function(.) aes(y = ..sample.., x = ..theoretical..)
  required_aes <- c("sample")

  calculate <- function(., data, scales, quantiles = NULL, distribution = qnorm, dparams = list(), na.rm = FALSE) {
    data <- remove_missing(data, na.rm, "sample", name = "stat_qq")    

    sample <- sort(data$sample)
    n <- length(sample)
    
    # Compute theoretical quantiles
    if (is.null(quantiles)) {
      quantiles <- ppoints(n)
    } else {
      stopifnot(length(quantiles) == n)
    }

    theoretical <- safe.call(distribution, c(list(p = quantiles), dparams))
  
    data.frame(sample, theoretical)
  }
  
  desc_outputs <- list(
    sample = "sample quantiles", 
    theoretical = "theoretical quantiles"
  )
  
  examples <- function(.) {
    # From ?qqplot
    y <- rt(200, df = 5)
    qplot(sample = y, stat="qq")

    # qplot is smart enough to use stat_qq if you use sample
    qplot(sample = y)
    qplot(sample = precip)

    qplot(sample = y, dist = qt, dparams = list(df = 5))
    
    df <- data.frame(y)
    ggplot(df, aes(sample = y)) + stat_qq()
    ggplot(df, aes(sample = y)) + geom_point(stat = "qq")
    
    # Use fitdistr from MASS to estimate distribution params
    params <- as.list(MASS::fitdistr(y, "t")$estimate)
    ggplot(df, aes(sample = y)) + stat_qq(dist = qt, dparam = params)
    
    # Using to explore the distribution of a variable
    qplot(sample = mpg, data = mtcars)
    qplot(sample = mpg, data = mtcars, colour = factor(cyl))    
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-quantile.r"
StatQuantile <- proto(Stat, {
  objname <- "quantile" 
  desc <- "Continuous quantiles"
  icon <- function(.) GeomQuantile$icon()

  desc_params <- list(
    quantiles = "conditional quantiles of y to calculate and display",
    formula = "formula relating y variables to x variables",
    xseq = "exact points to evaluate smooth at, overrides n"
  )
  desc_outputs <- list(
    quantile = "quantile of distribution"
  )
  
  default_geom <- function(.) GeomQuantile
  default_aes <- function(.) aes()
  required_aes <- c("x", "y")

  calculate <- function(., data, scales, quantiles=c(0.25, 0.5, 0.75), formula=y ~ x, xseq = NULL, method="rq", na.rm = FALSE, ...) {
    try_require("quantreg")
    if (is.null(data$weight)) data$weight <- 1 

    if (is.null(xseq)) xseq <- seq(min(data$x, na.rm=TRUE), max(data$x, na.rm=TRUE), length=100)

    data <- as.data.frame(data)
    data <- remove_missing(data, na.rm, c("x", "y"), name = "stat_quantile")
    
    method <- match.fun(method)
    model <- method(formula, data=data, tau=quantiles, weight=weight, ...)

    yhats <- stats::predict(model, data.frame(x=xseq), type="matrix")
    
    quantile <- rep(quantiles, each=length(xseq))
    data.frame(
      y = as.vector(yhats), 
      x = xseq, 
      quantile = quantile,
      group = paste(data$group[1], quantile, sep = "-")
    )
  }
  
  examples <- function(.) {
    msamp <- movies[sample(nrow(movies), 1000), ]
    m <- ggplot(msamp, aes(y=rating, x=year)) + geom_point() 
    m + stat_quantile()
    m + stat_quantile(quantiles = 0.5)
    m + stat_quantile(quantiles = seq(0.1, 0.9, by=0.1))

    # Doesn't work.  Not sure why.
    # m + stat_quantile(method = rqss, formula = y ~ qss(x), quantiles = 0.5)

    # Add aesthetic mappings
    m + stat_quantile(aes(weight=votes))

    # Change scale
    m + stat_quantile(aes(colour = ..quantile..), quantiles = seq(0.05, 0.95, by=0.05))
    m + stat_quantile(aes(colour = ..quantile..), quantiles = seq(0.05, 0.95, by=0.05)) +
      scale_colour_gradient2(midpoint=0.5, low="green", mid="yellow", high="green")

    # Set aesthetics to fixed value
    m + stat_quantile(colour="red", size=2, linetype=2)
    
    # Use qplot instead
    qplot(year, rating, data=movies, geom="quantile")

  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-smooth-methods.r"
# Prediction data frame
# Get predictions with standard errors into data frame
# 
# @keyword internal
# @alias predictdf.default
# @alias predictdf.glm
# @alias predictdf.loess
# @alias predictdf.locfit
predictdf <- function(model, xseq, se, level) UseMethod("predictdf")

predictdf.default <- function(model, xseq, se, level) {
  pred <- stats::predict(model, newdata = data.frame(x = xseq), se = se,
    level = level, interval = if(se) "confidence" else "none")

  if (se) {
    fit <- as.data.frame(pred$fit)
    names(fit) <- c("y", "ymin", "ymax")
    data.frame(x = xseq, fit, se = pred$se)
  } else {
    data.frame(x = xseq, y = as.vector(pred))
  } 
}

predictdf.glm <- function(model, xseq, se, level) {
  pred <- stats::predict(model, newdata = data.frame(x = xseq), se = se, 
    type = "link")
  
  if (se) {
    std <- qnorm(level / 2 + 0.5)
    data.frame(
      x = xseq, 
      y = model$family$linkinv(as.vector(pred$fit)),
      ymin = model$family$linkinv(as.vector(pred$fit - std * pred$se)), 
      ymax = model$family$linkinv(as.vector(pred$fit + std * pred$se)), 
      se = as.vector(pred$se)
    )
  } else {
    data.frame(x = xseq, y = model$family$linkinv(as.vector(pred)))
  }
}

predictdf.loess <- function(model, xseq, se, level) {
  pred <- stats::predict(model, newdata = data.frame(x = xseq), se = se,
    level = level, interval = if(se) "confidence" else "none")

  if (se) {
    y = pred$fit
    ymin = y - pred$se.fit
    ymax = y + pred$se.fit
    data.frame(x = xseq, y, ymin, ymax, se = pred$se.fit)
  } else {
    data.frame(x = xseq, y = as.vector(pred))
  }
}

predictdf.locfit <- function(model, xseq, se, level) {
  pred <- predict(model, newdata = data.frame(x = xseq), se.fit = se)
                          
  if (se) {
    y = pred$fit
    ymin = y - pred$se.fit
    ymax = y + pred$se.fit
    data.frame(x = xseq, y, ymin, ymax, se = pred$se.fit)
  } else {
    data.frame(x = xseq, y = as.vector(pred))
  }
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-smooth.r"
StatSmooth <- proto(Stat, {
  calculate_groups <- function(., data, scales, ...) {
    rows <- daply(data, .(group), function(df) length(unique(df$x)))
    
    if (all(rows == 1) && length(rows) > 1) {
      message("geom_smooth: Only one unique x value each group.", 
        "Maybe you want aes(group = 1)?")
      return(data.frame())
    }
    
    .super$calculate_groups(., data, scales, ...)
  }
  
  calculate <- function(., data, scales, method="auto", formula=y~x, se = TRUE, n=80, fullrange=FALSE, xseq = NULL, level=0.95, na.rm = FALSE, ...) {
    data <- remove_missing(data, na.rm, c("x", "y"), name="stat_smooth")
    if (length(unique(data$x)) < 2) {
      # Not enough data to perform fit
      return(data.frame())
    }
    
    # Figure out what type of smoothing to do: loess for small datasets,
    # gam with a cubic regression basis for large data
    if (is.character(method) && method == "auto") {
      if (nrow(data) < 1000) {
        method <- "loess"
      } else {
        try_require("mgcv")
        method <- gam
        formula <- y ~ s(x, bs = "cs")
      }
    }
    
    if (is.null(data$weight)) data$weight <- 1
    
    if (is.null(xseq)) {
      if (is.integer(data$x)) {
        if (fullrange) {
          xseq <- scales$x$input_set() 
        } else {
          xseq <- sort(unique(data$x))
        } 
      } else {
        if (fullrange) {
          range <- scales$x$output_set()
        } else {
          range <- range(data$x, na.rm=TRUE)  
        } 
        xseq <- seq(range[1], range[2], length=n)
      } 
    }
    if (is.character(method)) method <- match.fun(method)
    
    method.special <- function(...) 
      method(formula, data=data, weights=weight, ...)
    model <- safe.call(method.special, list(...), names(formals(method)))
    
    predictdf(model, xseq, se, level)
  }
  
  objname <- "smooth" 
  desc <- "Add a smoother"
  details <- "Aids the eye in seeing patterns in the presence of overplotting."
  icon <- function(.) GeomSmooth$icon()
  
  required_aes <- c("x", "y")
  default_geom <- function(.) GeomSmooth
  desc_params <- list(
    method = "smoothing method (function) to use, eg. lm, glm, gam, loess, rlm",
    formula =  "formula to use in smoothing function, eg. y ~ x, y ~ poly(x, 2), y ~ log(x)",
    se = "display confidence interval around smooth? (true by default, see level to control)",
    fullrange = "should the fit span the full range of the plot, or just the data",
    level = "level of confidence interval to use (0.95 by default)",
    n = "number of points to evaluate smoother at",
    xseq = "exact points to evaluate smooth at, overrides n",
    "..." = "other arguments are passed to smoothing function"
  )
  desc_outputs <- list(
    "y" = "predicted value",
    "ymin" = "lower pointwise confidence interval around the mean",
    "ymax" = "upper pointwise confidence interval around the mean",
    "se" = "standard error"
  )
  
  seealso <- list(
    lm = "for linear smooths",
    glm = "for generalised linear smooths",
    loess = "for local smooths"
  )
  
  examples <- function(.) {
    c <- ggplot(mtcars, aes(qsec, wt))
    c + stat_smooth() 
    c + stat_smooth() + geom_point()

    # Adjust parameters
    c + stat_smooth(se = FALSE) + geom_point()

    c + stat_smooth(span = 0.9) + geom_point()  
    c + stat_smooth(method = "lm") + geom_point() 
    
    library(splines)
    c + stat_smooth(method = "lm", formula = y ~ ns(x,3)) +
      geom_point()  
    c + stat_smooth(method = MASS::rlm, formula= y ~ ns(x,3)) + geom_point()  
    
    # The default confidence band uses a transparent colour. 
    # This currently only works on a limited number of graphics devices 
    # (including Quartz, PDF, and Cairo) so you may need to set the
    # fill colour to a opaque colour, as shown below
    c + stat_smooth(fill = "grey50", size = 2, alpha = 1)
    c + stat_smooth(fill = "blue", size = 2, alpha = 1)
    
    # The colour of the line can be controlled with the colour aesthetic
    c + stat_smooth(fill="blue", colour="darkblue", size=2)
    c + stat_smooth(fill="blue", colour="darkblue", size=2, alpha = 0.2)
    c + geom_point() + 
      stat_smooth(fill="blue", colour="darkblue", size=2, alpha = 0.2)
    
    # Smoothers for subsets
    c <- ggplot(mtcars, aes(y=wt, x=mpg)) + facet_grid(. ~ cyl)
    c + stat_smooth(method=lm) + geom_point() 
    c + stat_smooth(method=lm, fullrange=T) + geom_point() 
    
    # Geoms and stats are automatically split by aesthetics that are factors
    c <- ggplot(mtcars, aes(y=wt, x=mpg, colour=factor(cyl)))
    c + stat_smooth(method=lm) + geom_point() 
    c + stat_smooth(method=lm, aes(fill = factor(cyl))) + geom_point() 
    c + stat_smooth(method=lm, fullrange=TRUE, alpha = 0.1) + geom_point() 

    # Use qplot instead
    qplot(qsec, wt, data=mtcars, geom=c("smooth", "point"))
    
    # Example with logistic regression
    data("kyphosis", package="rpart")
    qplot(Age, Kyphosis, data=kyphosis)
    qplot(Age, data=kyphosis, facets = . ~ Kyphosis, binwidth = 10)
    qplot(Age, Kyphosis, data=kyphosis, position="jitter")
    qplot(Age, Kyphosis, data=kyphosis, position=position_jitter(height=0.1))

    qplot(Age, as.numeric(Kyphosis) - 1, data = kyphosis) +
      stat_smooth(method="glm", family="binomial")
    qplot(Age, as.numeric(Kyphosis) - 1, data=kyphosis) +
      stat_smooth(method="glm", family="binomial", formula = y ~ ns(x, 2))
    
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-spoke.r"
StatSpoke <- proto(Stat, {
  retransform <- FALSE
  calculate <- function(., data, scales, radius = 1, ...) {
    transform(data,
      xend = x + cos(angle) * radius,
      yend = y + sin(angle) * radius
    )
  }

  objname <- "spoke" 
  desc <- "Convert angle and radius to xend and yend"
  
  desc_outputs <- list(
    xend = "x position of end of line segment",
    yend = "x position of end of line segment"
  )

  default_aes <- function(.) aes(xend = ..xend.., yend = ..yend..)
  required_aes <- c("x", "y", "angle", "radius")
  default_geom <- function(.) GeomSegment
  
  examples <- function(.) {
    df <- expand.grid(x = 1:10, y=1:10)
    df$angle <- runif(100, 0, 2*pi)
    df$speed <- runif(100, 0, 0.5)
    
    qplot(x, y, data=df) + stat_spoke(aes(angle=angle), radius = 0.5)
    last_plot() + scale_y_reverse()
    
    qplot(x, y, data=df) + stat_spoke(aes(angle=angle, radius=speed))
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-sum.r"
StatSum <- proto(Stat, {
  default_aes <- function(.) aes(size = ..prop..)
  required_aes <- c("x", "y")
  default_geom <- function(.) GeomPoint
  icon <- function(.) textGrob(expression(Sigma), gp=gpar(cex=4))
  
  calculate_groups <- function(., data, scales, ...) {
    if (is.null(data$weight)) data$weight <- 1
    
    counts <- ddply(data, .(x, y, group), function(df) {
      cols <- names(df)[sapply(df, function(x) length(unique(x)) == 1)]
      data.frame(n = sum(df$weight), df[1, cols, drop = FALSE])
    })
    counts <- ddply(counts, .(group), transform, prop = n / sum(n))
    counts$group <- 1

    counts
  }
  
  objname <- "sum" 
  desc <- "Sum unique values.  Useful for overplotting on scatterplots"
  seealso <- list(
    "ggfluctuation" = "Fluctuation diagram, which is very similar"
    # "round_any" = "for rounding continuous observations to desired level of accuracy"
  )
  desc_outputs <- list(
    "n" = "number of observations at position",
    "prop" = "percent of points in that panel at that position"
  )
  
  examples <- function(.) {
    d <- ggplot(diamonds, aes(x = cut, y = clarity))
    # Need to control which group proportion calculated over
    # Overall proportion
    d + stat_sum(aes(group = 1))
    d + stat_sum(aes(group = 1)) + scale_size(to = c(3, 10))
    d + stat_sum(aes(group = 1)) + scale_area(to = c(3, 10))
    # by cut
    d + stat_sum(aes(group = cut))
    d + stat_sum(aes(group = cut, colour = cut))
    # by clarity
    d + stat_sum(aes(group = clarity))
    d + stat_sum(aes(group = clarity, colour = cut))
    
    # Instead of proportions, can also use sums
    d + stat_sum(aes(size = ..n..))

    # Can also weight by another variable
    d + stat_sum(aes(group = 1, weight = price))
    d + stat_sum(aes(group = 1, weight = price, size = ..n..))
    
    # Or using qplot
    qplot(cut, clarity, data = diamonds)
    qplot(cut, clarity, data = diamonds, stat = "sum", group = 1)    
  }
  
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-summary.r"
StatSummary <- proto(Stat, {
  objname <- "summary" 
  desc <- "Summarise y values at every unique x"
  
  details <- "<p>stat_summary allows for tremendous flexibilty in the specification of summary functions.  The summary function can either operate on a data frame (with argument name data) or on a vector.  A simple vector function is easiest to work with as you can return a single number, but is somewhat less flexible.  If your summary function operates on a data.frame it should return a data frame with variables that the geom can use.</p>"
  
  default_geom <- function(.) GeomPointrange
  required_aes <- c("x", "y")
   
  calculate_groups <- function(., data, scales, fun.data = NULL, fun.y = NULL, fun.ymax = NULL, fun.ymin = NULL, na.rm = FALSE, ...) {
    data <- remove_missing(data, na.rm, c("x", "y"), name = "stat_summary")
    
    if (!missing(fun.data)) {
      # User supplied function that takes complete data frame as input
      fun.data <- match.fun(fun.data)
      fun <- function(df, ...) {
        fun.data(df$y, ...)
      }
    } else {
      # User supplied individual vector functions
      fs <- compact(list(ymin = fun.ymin, y = fun.y, ymax = fun.ymax))
      
      fun <- function(df, ...) {
        res <- llply(fs, function(f) do.call(f, list(df$y, ...)))
        names(res) <- names(fs)
        as.data.frame(res)
      }
    }
    summarise_by_x(data, fun, ...)
  }
  seealso <- list(
    "geom_errorbar" = "error bars",
    "geom_pointrange" = "range indicated by straight line, with point in the middle",
    "geom_linerange" = "range indicated by straight line",
    "geom_crossbar" = "hollow bar with middle indicated by horizontal line",
    # "smean.sdl" = "for description of summary functions provide by Hmisc.  Replace the . with a _ to get the ggplot name",
    "stat_smooth" = "for continuous analog"
  )
  
  desc_params <- list(
    fun.data = "Complete summary function.  Should take data frame as input and return data frame as output.",
    fun.ymin = "ymin summary function (should take numeric vector and return single number)",
    fun.y = "ym summary function (should take numeric vector and return single number)",
    fun.ymax = "ymax summary function (should take numeric vector and return single number)"
  )
  
  
  desc_outputs <- list()
  
  examples <- function(.) {
    # Basic operation on a small dataset
    c <- qplot(cyl, mpg, data=mtcars)
    c + stat_summary(fun.data = "mean_cl_boot", colour = "red")

    p <- qplot(cyl, mpg, data = mtcars, stat="summary", fun.y = "mean")
    p
    # Don't use ylim to zoom into a summary plot - this throws the
    # data away
    p + ylim(15, 30)
    # Instead use coord_cartesian
    p + coord_cartesian(ylim = c(15, 30))
    
    # You can supply individual functions to summarise the value at 
    # each x:
    
    stat_sum_single <- function(fun, geom="point", ...) {
      stat_summary(fun.y=fun, colour="red", geom=geom, size = 3, ...)      
    }
    
    c + stat_sum_single(mean)
    c + stat_sum_single(mean, geom="line")
    c + stat_sum_single(median)
    c + stat_sum_single(sd)
    
    c + stat_summary(fun.y = mean, fun.ymin = min, fun.ymax = max, 
      colour = "red")
    
    c + aes(colour = factor(vs)) + stat_summary(fun.y = mean, geom="line")
    
    # Alternatively, you can supply a function that operates on a data.frame.
    # A set of useful summary functions is provided from the Hmisc package:
    
    stat_sum_df <- function(fun, geom="crossbar", ...) {
      stat_summary(fun.data=fun, colour="red", geom=geom, width=0.2, ...)
    }
    
    c + stat_sum_df("mean_cl_boot")
    c + stat_sum_df("mean_sdl")
    c + stat_sum_df("mean_sdl", mult=1)
    c + stat_sum_df("median_hilow")

    # There are lots of different geoms you can use to display the summaries
        
    c + stat_sum_df("mean_cl_normal")
    c + stat_sum_df("mean_cl_normal", geom = "errorbar")
    c + stat_sum_df("mean_cl_normal", geom = "pointrange")
    c + stat_sum_df("mean_cl_normal", geom = "smooth")
        
    # Summaries are much more useful with a bigger data set:
    m <- ggplot(movies, aes(x=round(rating), y=votes)) + geom_point()
    m2 <- m + 
       stat_summary(fun.data = "mean_cl_boot", geom = "crossbar", 
         colour = "red", width = 0.3)
    m2
    # Notice how the overplotting skews off visual perception of the mean
    # supplementing the raw data with summary statisitcs is _very_ important
  
    # Next, we'll look at votes on a log scale.

    # Transforming the scale performs the transforming before the statistic.
    # This means we're calculating the summary on the logged data
    m2 + scale_y_log10()
    # Transforming the coordinate system performs the transforming after the
    # statistic. This means we're calculating the summary on the raw data, 
    # and stretching the geoms onto the log scale.  Compare the widths of the
    # standard errors.
    m2 + coord_trans(y="log10")
  }
})

# Summarise a data.frame by parts
# Summarise a data frame by unique value of x
# 
# This function is used by \code{\link{stat_summary}} to break a 
# data.frame into pieces, summarise each piece, and join the pieces
# back together, retaining original columns unaffected by the summary.
# 
# @arguments \code{\link{data.frame}} to summarise
# @arguments vector to summarise by
# @arguments summary function (must take and return a data.frame)
# @arguments other arguments passed on to summary function
# @keyword internal
summarise_by_x <- function(data, summary, ...) {
  summary <- ddply(data, .(group, x), summary, ...)
  unique <- ddply(data, .(group, x), uniquecols)
  unique$y <- NULL
  
  merge(summary, unique, by = c("x", "group"))
}

# Wrap Hmisc summary functions 
# Wrap up a selection of Hmisc to make it easy to use with \code{\link{stat_summary}}
# 
# See the Hmisc documentation for details of their options.
# 
# @seealso \code{\link[Hmisc]{smean.cl.boot}}, \code{\link[Hmisc]{smean.cl.normal}}, \code{\link[Hmisc]{smean.sdl}}, \code{\link[Hmisc]{smedian.hilow}}
# @alias mean_cl_boot
# @alias mean_cl_normal
# @alias mean_sdl
# @alias median_hilow
# @keyword internal
wrap_hmisc <- function(fun) {
  function(x, ...) {
    try_require("Hmisc")
  
    result <- safe.call(fun, list(x = x, ...))
    rename(
      data.frame(t(result)), 
      c(Median = "y", Mean = "y", Lower = "ymin", Upper = "ymax")
    )    
  }
}
mean_cl_boot <- wrap_hmisc("smean.cl.boot")
mean_cl_normal <- wrap_hmisc("smean.cl.normal")
mean_sdl <- wrap_hmisc("smean.sdl")
median_hilow <- wrap_hmisc("smedian.hilow")

# Mean + se's.
# Mean and standard errors on either side.
#
# @arguments numeric vector
# @arguments number of multiples of standard error
# @seealso for use with \code{\link{stat_summary}}
mean_se <- function(x, mult = 1) {  
  x <- na.omit(x)
  se <- mult * sqrt(var(x) / length(x))
  mean <- mean(x)
  data.frame(y = mean, ymin = mean - se, ymax = mean + se)
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-unique.r"
StatUnique <- proto(Stat, {
  objname <- "unique" 
  desc <- "Remove duplicates"
  
  default_geom <- function(.) GeomPoint
  
  calculate_groups <- function(., data, scales, ...) unique(data)
  
  desc_outputs <- list()
  
  examples <- function(.) {
    ggplot(mtcars, aes(x=vs, y=am)) + geom_point(colour="#00000010")
    ggplot(mtcars, aes(x=vs, y=am)) + geom_point(colour="#00000010", stat="unique")
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/stat-vline.r"
StatAbline <- proto(Stat, {
  calculate <- function(., data, scales, intercept = NULL, slope = NULL, ...) {
    data <- aesdefaults(data, .$default_aes(), list(...))
    if (is.null(intercept)) {
      if (is.null(data$intercept)) data$intercept <- 0
    } else {
      data <- data[rep(1, length(intercept)), , drop = FALSE]
      data$intercept <- intercept
    }
    if (is.null(slope)) {
      if (is.null(data$slope)) data$slope <- 1
    } else {
      data <- data[rep(1, length(slope)), , drop = FALSE]
      data$slope <- slope
    }
    unique(data)
  }
  
  objname <- "abline" 
  desc <- "Add a line with slope and intercept"
  icon <- function(.) GeomAbline$icon()
  
  required_aes <- c()
  default_geom <- function(.) GeomAbline
  
  examples <- function(.) {
    # See geom_abline for examples
  }
})

StatVline <- proto(Stat, {
  calculate <- function(., data, scales, xintercept = NULL, intercept, ...) {
    if (!missing(intercept)) {
      stop("stat_vline now uses xintercept instead of intercept")
    }
    data <- compute_intercept(data, xintercept, "x")
    
    unique(within(data, {
      x    <- xintercept
      xend <- xintercept
    }))
  }
  
  objname <- "vline" 
  desc <- "Add a vertical line"
  icon <- function(.) GeomVline$icon()
  
  required_aes <- c()
  default_geom <- function(.) GeomVline
  
  examples <- function(.) {
    # See geom_vline for examples
  }
})

StatHline <- proto(Stat, {
  calculate <- function(., data, scales, yintercept = NULL, intercept, ...) {
    if (!missing(intercept)) {
      stop("stat_hline now uses yintercept instead of intercept")
    }

    data <- compute_intercept(data, yintercept, "y")
    
    unique(within(data, {
      y    <- yintercept
      yend <- yintercept
    }))
  }
  
  objname <- "hline" 
  desc <- "Add a horizontal line"
  icon <- function(.) GeomHline$icon()
  
  required_aes <- c()
  default_geom <- function(.) GeomHline
  
  examples <- function(.) {
    # See geom_hline for examples
  }
})


# Compute intercept from data
# Compute intercept for vline and hline from data and parameters
# 
# @keyword internal
compute_intercept <- function(data, intercept, var = "x") {
  ivar <- paste(var, "intercept", sep = "")
  if (is.null(intercept)) {
    # Intercept comes from data, default to 0 if not set
    if (is.null(data[[ivar]])) data[[ivar]] <- 0
    
  } else if (is.numeric(intercept)) {
    # Intercept is a numeric vector of positions
    data <- data[rep(1, length(intercept)), ]
    data[[ivar]] <- intercept
    
  } else if (is.character(intercept) || is.function(intercept)) {
    # Intercept is a function
    f <- match.fun(intercept)
    trans <- function(data) {
      data[[ivar]] <- f(data[[var]])
      data
    }
    data <- ddply(data, .(group), trans)
  } else {
    stop("Invalid intercept type: should be a numeric vector, a function", 
         ", or a name of a function", call. = FALSE)
  }
  data
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/summary.r"
# Summarise ggplot object
# Displays a useful description of a ggplot object
# 
# @keyword internal
#X summary(qplot(mpg, wt, data=mtcars))
summary.ggplot <- function(object, ...) {
  wrap <- function(x) paste(
    paste(strwrap(x, exdent = 2), collapse = "\n"),
    "\n", sep =""
    )
  
  defaults <- function() {
    paste(mapply(function(x, n) {
      paste(n, deparse(x), sep="=")
    }, object$mapping, names(object$mapping)), collapse=", ")
  }
  
  # cat("Title:    ", object$title, "\n", sep="")
  # cat("-----------------------------------\n")
  if (!is.null(object$data)) {
    output <- paste(
      "data:     ", paste(names(object$data), collapse=", "), 
      " [", nrow(object$data), "x", ncol(object$data), "] ", 
      "\n", sep="")
    cat(wrap(output))
  }
  if (length(object$mapping) > 0) {
    cat("mapping:  ", clist(object$mapping), "\n", sep="")    
  }
  if (object$scales$n() > 0) {
    cat("scales:  ", paste(object$scales$output(), collapse = ", "), "\n")
  }
  
  cat("faceting: ")
  object$facet$pprint()

  if (length(object$layers) > 0)
    cat("-----------------------------------\n")
  invisible(lapply(object$layers, function(x) {print(x); cat("\n")}))

} 
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/templates.r"
# These functions provide template for creating common plots.
# They are also useful to illustrate some different capabilities of
# ggplot.

# Parallel coordinates plot.
# Generate a plot ``template'' for a parallel coordinates plot.
# 
# One way to think about a parallel coordinates plot, is as plotting 
# the data after it has transformation been transformed to gain a new
# variable.  This function does this using \code{\link[reshape]{melt}}.
# 
# This gives us enormous flexibility as we have separated out the 
# type of drawing (lines by tradition) and can now use any of the existing
# geom functions.  In particular this makes it very easy to create parallel
# boxplots, as shown in the example.
# 
# Three different scaling function are available:
# \itemize{
#   \item "range": scale coordinates to have common range $[0, 1]
#   \item "var": scale coordinates to have mean 0 and variance 1
#   \item "I": don't scale the coordinates at all 
# }
# @arguments data frame
# @arguments variables to include in parallel coordinates plot
# @arguments scaling function, one of "range", "var" or "I"
# @arguments other arguments passed on plot creation
# @keyword hplot 
#X ggpcp(mtcars) + geom_line()
#X ggpcp(mtcars, scale="var") + geom_line()
#X ggpcp(mtcars, vars=names(mtcars)[3:6], formula= . ~cyl, scale="I") + geom_line()
#X ggpcp(mtcars, scale="I") + geom_boxplot(aes(group=variable))
#X ggpcp(mtcars, vars=names(mtcars[2:6])) + geom_line()
#X p <- ggpcp(mtcars, vars=names(mtcars[2:6]))
#X p + geom_line()
#X p + geom_line(aes(colour=mpg)) 
ggpcp <- function(data, vars=names(data), scale="range", ...) {
  force(vars)  
  scaled <- rescaler(data[, vars], type=scale)
  data <- cunion(scaled, data)
  
  data$ROWID <- 1:nrow(data)
  molten <- melt(data, m=vars)

  ggplot(molten, aes_string(x = "variable", y = "value", group = "ROWID"), ...)
}

# Fluctuation plot
# Create a fluctuation plot.
# 
# A fluctutation diagram is a graphical representation of a contingency
# table.  This fuction currently only supports 2D contingency tabless
# but extension to more should be relatively straightforward.
# 
# With the default size fluctuation diagram, area is proportional to the 
# count (length of sides proportional to sqrt(count))
# 
# @arguments a table of values, or a data frame with three columns, the last column being frequency
# @arguments size, or colour to create traditional heatmap
# @arguments don't display cells smaller than this value
# @arguments round cells to at most this value
# @keyword hplot
#X ggfluctuation(table(movies$Action, movies$Comedy))
#X ggfluctuation(table(movies$Action, movies$mpaa))
#X ggfluctuation(table(movies$Action, movies$Comedy), type="colour")
#X ggfluctuation(table(warpbreaks$breaks, warpbreaks$tension))
ggfluctuation <- function(table, type="size", floor=0, ceiling=max(table$freq, na.rm=TRUE)) {
  if (is.table(table)) table <- as.data.frame(t(table))

  oldnames <- names(table)
  names(table) <- c("x","y", "result")
  
  table <- add.all.combinations(table, list("x","y"))  
  table <- transform(table,
    x = as.factor(x),
    y = as.factor(y),
    freq = result
 )

  if (type =="size") {
    table <- transform(table, 
      freq = sqrt(pmin(freq, ceiling) / ceiling),
      border = ifelse(is.na(freq), "grey90", ifelse(freq > ceiling, "grey30", "grey50"))
    )
    table[is.na(table$freq), "freq"] <- 1
    table <- subset(table, freq * ceiling >= floor)
  }

  if (type=="size") {
    nx <- length(levels(table$x))
    ny <- length(levels(table$y))
    
    p <- ggplot(table, 
      aes_string(x="x", y="y", height="freq", width="freq", fill="border")) +
      geom_tile(colour="white") + 
      scale_fill_identity() + 
      opts(aspect.ratio = ny / nx)

      # geom_rect(aes(xmin = as.numeric(x), ymin = as.numeric(y), xmax = as.numeric(x) + freq, ymax = as.numeric(y) + freq), colour="white") + 
    
  } else {
    p <- ggplot(table, aes_string(x="x", y="y", fill="freq")) + 
      geom_tile(colour="grey50") +
      scale_fill_gradient2(low="white", high="darkgreen")
  }

  p$xlabel <- oldnames[1]
  p$ylabel <- oldnames[2]
  p
}

# Missing values plot
# Create a plot to illustrate patterns of missing values
# 
# The missing values plot is a useful tool to get a rapid
# overview of the number of missings in a dataset.  It's strength
# is much more apparent when used with interactive graphics, as you can
# see in Mondrian (\url{http://rosuda.org/mondrian}) where this plot was
# copied from.
# 
# @arguments data.frame
# @arguments whether missings should be stacked or dodged, see \code{\link{geom_bar}} for more details
# @arguments whether variable should be ordered by number of missings
# @arguments whether only variables containing some missing values should be shown
# @keyword hplot
# @seealso \code{\link{ggstructure}}, \code{\link{ggorder}}
#X mmissing <- movies
#X mmissing[sample(nrow(movies), 1000), sample(ncol(movies), 5)] <- NA
#X ggmissing(mmissing)
#X ggmissing(mmissing, order=FALSE, missing.only = FALSE)
#X ggmissing(mmissing, avoid="dodge") + scale_y_sqrt()
ggmissing <- function(data, avoid="stack", order=TRUE, missing.only = TRUE) {
  missings <- mapply(function(var, name) cbind(as.data.frame(table(missing=factor(is.na(var), levels=c(TRUE, FALSE), labels=c("yes", "no")))), variable=name), 
    data, names(data), SIMPLIFY=FALSE
  )
  df <- do.call("rbind", missings)
  
  prop <- df[df$missing == "yes", "Freq"] / (df[df$missing == "no", "Freq"] + df[df$missing == "yes", "Freq"])
  df$prop <- rep(prop, each=2)
  
  if (order) {
    var <- df$variable
    var <- factor(var, levels = levels(var)[order(1 - prop)])
    df$variable <- var
  }

  if (missing.only) {
    df <- df[df$prop > 0 & df$prop < 1, , drop=FALSE]
    df$variable <- factor(df$variable)
  }
  
  ggplot(df, aes_string(y="Freq", x="variable", fill="missing")) + geom_bar(position=avoid)
}

# Structure plot
# A plot which aims to reveal gross structural anomalies in the data
# 
# @arguments data set to plot
# @arguments type of scaling to use.  See \code{\link[reshape]{rescaler}} for options
# @keyword hplot
#X ggstructure(mtcars)
ggstructure <- function(data, scale = "rank") {
  ggpcp(data, scale=scale) + 
    aes_string(y="ROWID", fill="value", x="variable") +
    geom_tile() +
    scale_y_continuous("row number", expand = c(0, 1)) +
    scale_fill_gradient2(low="blue", mid="white", high="red", midpoint=0)
}

# Order plot
# A plot to investigate the order in which observations were recorded.
# 
# @arguments data set to plot
# @arguments type of scaling to use.  See \code{\link[reshape]{rescaler}} for options
# @keyword hplot 
ggorder <- function(data, scale="rank") {
  ggpcp(data, scale="rank") +
    aes_string(x="ROWID", group="variable", y="value") +
    facet_grid(. ~ variable) +
    geom_line() +
    scale_x_continuous("row number")
}

# Distribution plot
# Experimental template
# 
# @keyword internal  
ggdist <- function(data, vars=names(data), facets = . ~ .) {
  cat <- sapply(data[vars], is.factor)
  facets <- deparse(substitute(facets))
  
  grid.newpage()
  pushViewport(viewport(layout=grid.layout(ncol = ncol(data))))
  
  mapply(function(name, cat, i) {
    p <- ggplot(data) + 
      facet_grid(facets) +
      aes_string(x=name, y=1) +
      geom_bar()

    pushViewport(viewport(layout.pos.col=i))
    grid.draw(ggplotGrob(p))
    popViewport()
  }, names(data[vars]), cat, 1:ncol(data[vars]))
  invisible()
  
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/theme-defaults.r"
# Grey theme
# Produce a theme with grey background and white gridlines
# 
# @arguments base font size
# @arguments base font family
# @keyword dplot
# @alias theme_gray
theme_grey <- function(base_size = 12, base_family = "") {
  structure(list(
    axis.line =          theme_blank(),
    axis.text.x =        theme_text(family = base_family, size = base_size * 0.8 , lineheight = 0.9, colour = "grey50", vjust = 1),
    axis.text.y =        theme_text(family = base_family, size = base_size * 0.8, lineheight = 0.9, colour = "grey50", hjust = 1),
    axis.ticks =         theme_segment(colour = "grey50"),
    axis.title.x =       theme_text(family = base_family, size = base_size, vjust = 0.5),
    axis.title.y =       theme_text(family = base_family, size = base_size, angle = 90, vjust = 0.5),
    axis.ticks.length =  unit(0.15, "cm"),
    axis.ticks.margin =  unit(0.1, "cm"),

    legend.background =  theme_rect(colour="white"), 
    legend.key =         theme_rect(fill = "grey95", colour = "white"),
    legend.key.size =    unit(1.2, "lines"),
    legend.key.height =  NA,
    legend.key.width =   NA,
    legend.text =        theme_text(family = base_family, size =  base_size * 0.8),
    legend.text.align =  NA,
    legend.title =       theme_text(family = base_family, size =  base_size * 0.8, face =  "bold", hjust =  0),
    legend.title.align = NA,
    legend.position =    "right",
    legend.direction =   "vertical",
    legend.box =         NA,
                 
    panel.background =   theme_rect(fill =  "grey90", colour =  NA), 
    panel.border =       theme_blank(), 
    panel.grid.major =   theme_line(colour =  "white"),
    panel.grid.minor =   theme_line(colour =  "grey95", size =  0.25),
    panel.margin =       unit(0.25, "lines"),

    strip.background =   theme_rect(fill =  "grey80", colour =  NA), 
    strip.text.x =       theme_text(family = base_family, size =  base_size * 0.8),
    strip.text.y =       theme_text(family = base_family, size =  base_size * 0.8, angle =  -90),

    plot.background =    theme_rect(colour =  NA, fill =  "white"),
    plot.title =         theme_text(family = base_family, size =  base_size * 1.2),
    plot.margin =        unit(c(1, 1, 0.5, 0.5), "lines")
  ), class =  "options")
}
theme_gray <- theme_grey

# Black and white theme
# Produce a theme with white background and black gridlines
# 
# @arguments base font size
# @arguments base font family
# @keyword dplot
theme_bw <- function(base_size =  12, base_family = "") {
  structure(list(
    axis.line =          theme_blank(),
    axis.text.x =        theme_text(family = base_family, size =  base_size * 0.8 , lineheight =  0.9, vjust =  1),
    axis.text.y =        theme_text(family = base_family, size =  base_size * 0.8, lineheight =  0.9, hjust =  1),
    axis.ticks =         theme_segment(colour =  "black", size =  0.2),
    axis.title.x =       theme_text(family = base_family, size =  base_size, vjust =  1),
    axis.title.y =       theme_text(family = base_family, size =  base_size, angle =  90, vjust =  0.5),
    axis.ticks.length =  unit(0.3, "lines"),
    axis.ticks.margin =  unit(0.5, "lines"),

    legend.background =  theme_rect(colour=NA), 
    legend.key =         theme_rect(colour =  "grey80"),
    legend.key.size =    unit(1.2, "lines"),
    legend.key.height =  NA,
    legend.key.width =   NA,
    legend.text =        theme_text(family = base_family, size =  base_size * 0.8),
    legend.text.align =  NA,
    legend.title =       theme_text(family = base_family, size =  base_size * 0.8, face =  "bold", hjust =  0),
    legend.title.align = NA,
    legend.position =    "right",
    legend.direction =   "vertical",
    legend.box =         NA,
                 
    panel.background =   theme_rect(fill =  "white", colour =  NA), 
    panel.border =       theme_rect(fill =  NA, colour="grey50"), 
    panel.grid.major =   theme_line(colour =  "grey90", size =  0.2),
    panel.grid.minor =   theme_line(colour =  "grey98", size =  0.5),
    panel.margin =       unit(0.25, "lines"),

    strip.background =   theme_rect(fill =  "grey80", colour =  "grey50"), 
    strip.text.x =       theme_text(family = base_family, size =  base_size * 0.8),
    strip.text.y =       theme_text(family = base_family, size =  base_size * 0.8, angle =  -90),

    plot.background =    theme_rect(colour =  NA),
    plot.title =         theme_text(family = base_family, size =  base_size * 1.2),
    plot.margin =        unit(c(1, 1, 0.5, 0.5), "lines")
  ), class =  "options")
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/theme-elements.r"
# Email Paul:  absolute vs relative grobs
# Exact grob heights
# Computing max and min at creation where possible

# Theme element: blank
# This theme element draws nothing, and assigns no space
# 
# @keyword dplot
theme_blank <- function() {
  structure(
    function(...) zeroGrob(),
    class = "theme",
    type = "any",
    call = match.call()
  )  
}

# Theme element: rectangle
# This element draws a rectangular box
# 
# This is most often used for backgrounds and borders
# 
# @seealso \code{\link{rectGrob}} for underlying grid function
# @arguments fill colour
# @arguments border color
# @arguments border size
# @arguments border linetype
# @keyword dplot
theme_rect <- function(fill = NA, colour = "black", size = 0.5, linetype = 1) {
  structure(
    function(x = 0.5, y = 0.5, width = 1, height = 1, ...) {
      rectGrob(
        x, y, width, height, ...,
        gp=gpar(lwd=size * .pt, col=colour, fill=fill, lty=linetype),
      )
    },
    class = "theme",
    type = "box",
    call = match.call()
  )
}

# Theme element: line
# This element draws a line between two (or more) points
# 
# @seealso \code{\link{polylineGrob}} for underlying grid function, \code{link{theme_segment}}
# @arguments line color
# @arguments line size
# @arguments line type
# @keyword dplot
theme_line <- function(colour = "black", size = 0.5, linetype = 1) {
  structure(
    function(x = 0:1, y = 0:1, ..., default.units = "npc") {
      polylineGrob(
        x, y, ..., default.units = default.units,
        gp=gpar(lwd=size * .pt, col=colour, lty=linetype),
      )
    },
    class = "theme",
    type = "line",
    call = match.call()
  )
}

# Theme element: segments
# This element draws segments between a set of points
# 
# @seealso \code{\link{segmentsGrob}} for underlying grid function, \code{link{theme_line}}
# @arguments line color
# @arguments line size
# @arguments line type
# @keyword dplot
theme_segment <- function(colour = "black", size = 0.5, linetype = 1) {
  structure(
    function(x0 = 0, y0 = 0, x1 = 1, y1 = 1, ...) {
      segmentsGrob(
        x0, y0, x1, y1, ..., default.units = "npc",
        gp=gpar(col=colour, lty=linetype, lwd = size * .pt),
      )
    },
    class = "theme",
    type = "segment",
    call = match.call()
  )
}


# Theme element: text
# This element adds text
# 
# @seealso \code{\link{textGrob}} for underlying grid function
# @arguments font family
# @arguments font face ("plain", "italic", "bold")
# @arguments text colour
# @arguments text size (in pts)
# @arguments horizontal justification (in [0, 1])
# @arguments vertical justification (in [0, 1])
# @arguments angle (in [0, 360])
# @arguments line height
# @keyword dplot
theme_text <- function(family = "", face = "plain", colour = "black", size = 10, hjust = 0.5, vjust = 0.5, angle = 0, lineheight = 1.1) {

  vj <- vjust
  hj <- hjust
  angle <- angle %% 360
  
  if (angle == 90) {
    xp <- vj
    yp <- hj
  } else if (angle == 180) {
    xp <- 1 - hj
    yp <- vj
  } else if (angle == 270) {
    xp <- vj
    yp <- 1 - hj
  }else {
    xp <- hj
    yp <- vj
  }

  structure(
    function(label, x = xp, y = yp, ..., vjust = vj, hjust = hj, default.units = "npc") {

      textGrob(
        label, x, y, hjust = hjust, vjust = vjust, ...,
        default.units = default.units,
        gp = gpar(
          fontsize = size, col = colour, 
          fontfamily = family, fontface = face, 
          lineheight = lineheight
        ),
        rot = angle
      )
    },
    class = "theme",
    type = "text",
    call = match.call()
  )
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/theme.r"
# Get, set and update themes.
# These three functions get, set and update themes.
# 
# Use \code{theme_update} to modify a small number of elements of the current
# theme or use \code{theme_set} to completely override it.
# 
# @alias theme settings to override
# @alias theme_set
# @alias theme_get
# @alias ggopt
# @arguments named list of theme settings
#X qplot(mpg, wt, data = mtcars)
#X old <- theme_set(theme_bw())
#X qplot(mpg, wt, data = mtcars)
#X theme_set(old)
#X qplot(mpg, wt, data = mtcars)
#X
#X old <- theme_update(panel.background = theme_rect(colour = "pink"))
#X qplot(mpg, wt, data = mtcars)
#X theme_set(old)
#X theme_get()
#X 
#X qplot(mpg, wt, data=mtcars, colour=mpg) + 
#X   opts(legend.position=c(0.95, 0.95), legend.justification = c(1, 1))
#X last_plot() + 
#X  opts(legend.background = theme_rect(fill = "white", col="white", size =3))
theme_update <- function(...) {
  elements <- list(...)
  if (length(args) == 1 && is.list(elements[[1]])) {
    elements <- elements[[1]]
  }
  theme <- defaults(elements, theme_get())
  class(theme) <- c("options")
  
  theme_set(theme)  
}

.theme <- (function() {
  theme <- theme_gray()

  list(
    get = function() theme,
    set = function(new) {
      missing <- setdiff(names(theme_gray()), names(new))
      if (length(missing) > 0) {
        warning("New theme missing the following elements: ", 
          paste(missing, collapse = ", "), call. = FALSE)
      }
      
      old <- theme
      theme <<- new
      invisible(old)
    }
  )
})()
theme_get <- .theme$get  
theme_set <- .theme$set

ggopt <- function(...) {
  .Deprecated("theme_update")
}

# Plot options
# Set options/theme elements for a single plot
# 
# Use this function if you want to modify a few theme settings for 
# a single plot.
# 
# @arguments named list of theme settings
#X p <- qplot(mpg, wt, data = mtcars)
#X p 
#X p + opts(panel_background = theme_rect(colour = "pink"))
#X p + theme_bw()
opts <- function(...) {
  structure(list(...), class="options")
}

# Render a theme element
# This function is used internally for all drawing of plot surrounds etc
# 
# It also names the created grobs consistently
# 
# @keyword internal
theme_render <- function(theme, element, ..., name = NULL) {
  el <- theme[[element]]
  if (is.null(el)) {
    message("Theme element ", element, " missing")
    return(zeroGrob())
  }
  
  ggname(ps(element, name, sep = "."), el(...))
}

# Print out a theme element
# Currently all theme elements save there call, which is printed here
# 
# @keyword internal
print.theme <- function(x, ...) {
  call <- attr(x, "call")
  print(call)
}

# Retrieve theme for a plot
# Combines plot defaults with current theme to get complete theme for a plot
# 
# @arguments plot
# @keyword internal
plot_theme <- function(x) {
  defaults(x$options, theme_get())
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/trans-.r"
Trans <- proto(TopLevel, {
  .transform <- force
  .transform_inverse <- force
  .transform_labels <- transform 

  objname <- "Transformer"
  class <- function(.) "trans"

  new <- function(., name, f="force", inverse="force", labels="force", ...) {
    .$proto(
      objname = name, 
      .transform = f,
      .transform_inverse = inverse,
      .transform_labels = labels,
      ...
    )
  }
  
  transform <- function(., values) {
    if (is.null(values)) return()
    match.fun(get(".transform", .))(values)
  }

  inverse <- function(., values) {
    if (is.null(values)) return()
    match.fun(get(".transform_inverse", .))(values)
  }

  label <- function(., values) {
    if (is.null(values)) return()
    lapply(values, match.fun(get(".transform_labels", .)))
  }

  # Create regular sequence in original scale, then transform back
  seq <- function(., from, to, length) {
    .$transform(get("seq", pos=1)(.$inverse(from), .$inverse(to), length=length))
  }
  
  input_breaks <- function(., range) {
    grid.pretty(range)
  }
  
  # Minor breaks are regular on the original scale
  # and need to cover entire range of plot
  output_breaks <- function(., n = 2, b, r) {
    if (length(b) == 1) return(b)

    bd <- diff(b)[1]
    if (min(r) < min(b)) b <- c(b[1] - bd, b)
    if (max(r) > max(b)) b <- c(b, b[length(b)] + bd)
    unique(unlist(mapply(.$seq, b[-length(b)], b[-1], length=n+1, SIMPLIFY=F)))
  }
  
  
  check <- function(., values) {
    .$inverse(.$transform(values))
  }

  pprint <- function(., newline=TRUE) {
    cat(deparse(get(".transform", .)), " <-> ", deparse(get(".transform_inverse", .)))
    if (newline) cat("\n") 
  }
  
})


PowerTrans <- proto(Trans, {
  new <- function(., exponent) {
    .$proto(objname = paste("pow", exponent, sep=""), p = exponent)
  }
  transform <- function(., values) {
    (values^.$p - 1) / .$p * sign(values - 1)
  }
  inverse <- function(., values) {
    (abs(values) * .$p + 1 * sign(values)) ^ (1 / .$p) 
  }
  label <- function(., values) .$inverse(values)
})

ProbabilityTrans <- proto(Trans, {
  new <- function(., family) {
    .$proto(objname=family, family = family)
  }
  transform <- function(., values) {
    if (is.null(values)) return()
    match.fun(paste("q", .$family, sep=""))(values)
  }
  inverse <- function(., values) {
    match.fun(paste("p", .$family, sep=""))(values)
  }
  label <- function(., values) .$inverse(values)
})

TransAsn <- Trans$new(
  "asn", 
  function(x) 2 * asin(sqrt(x)), 
  function(x) sin(x / 2)^2
)
TransAtanh <- Trans$new("atanh", "atanh", "tanh", "force")
TransExp <- Trans$new("exp", "exp", "log", function(x) bquote(log(.(x))))
TransIdentity <- Trans$new("identity", "force", "force", "force")
TransInverse <- Trans$new("inverse", function(x) 1/x, function(x) 1/x,  function(x) bquote(phantom()^1 / phantom()[.(x)]))
TransLog <- Trans$new("log", "log", "exp", function(x) bquote(e^.(x)))
TransLog10 <- Trans$new("log10", "log10", function(x) 10^x, function(x) bquote(10^.(x)))
TransLog2 <- Trans$new("log2", "log2", function(x) 2^x, function(x) bquote(2^.(x)))
TransLog1p <- Trans$new("log1p", "log1p", "expm1", function(x) bquote(e^.(x+1)))
TransPow10 <- Trans$new("pow10",function(x) 10^x, "log10", function(x) log10(x))
TransReverse <- Trans$new("reverse", function(x) -x, function(x) -x, function(x) bquote(.(-x)))
TransSqrt <- Trans$new("sqrt", "sqrt", function(x) x^2, function(x) x^2)

TransDate <- Trans$new("date", "as.numeric", "to_date")
TransDatetime <- Trans$new("datetime", "as.numeric", "to_time")

TransLogit <- ProbabilityTrans$new("logis")
TransProbit <- ProbabilityTrans$new("norm")
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/trans-scales.r"
ScaleArea <- proto(
  ScaleSizeContinuous, 
  desc = "Scale area",
  objname = "area", 
  icon = function(.) ScaleSize$icon(), 
  details = "", 
  doc = FALSE,
  new = function(., name=NULL, limits=NULL, breaks=NULL, labels=NULL, to = c(1, 6), legend = TRUE) {
    .super$new(., name = name, limits = limits, breaks = breaks, 
       labels = labels, trans="sqrt", to = to, legend = legend)
  }
  
)

ScaleSqrt <-     proto(ScaleContinuous, 
  desc = "Position scale, square root transformed",
  tr_default = Trans$find("sqrt"),     
  objname = "sqrt", 
  doc=FALSE, 
  examples=function(.) {}
)

ScaleLog10 <-    proto(ScaleContinuous,
  desc = "Position scale, log10 transformed",
  tr_default = Trans$find("log10"),
  objname = "log10",
  doc=FALSE,
  examples=function(.) {}
)

ScalePow10 <-    proto(ScaleContinuous,
  desc = "Position scale, pow10 transformed",
  tr_default = Trans$find("pow10"),
  objname = "pow10",
  doc=FALSE,
  examples=function(.) {}
)

ScaleLog2 <-     proto(ScaleContinuous,
  desc = "Position scale, log2 transformed",
  tr_default = Trans$find("log2"),
  objname = "log2",
  doc=FALSE,
  examples=function(.) {}
)

ScaleLog <-      proto(ScaleContinuous,
  desc = "Position scale, log transformed",
  tr_default = Trans$find("log"),
  objname = "log",
  doc=FALSE,
  examples=function(.) {}
)

ScaleLog1p <-      proto(ScaleContinuous,
  desc = "Position scale, log + 1 transformed",
  tr_default = Trans$find("log1p"),
  objname = "log1p",
  doc=FALSE,
  examples=function(.) {}
)

ScaleExp <-      proto(ScaleContinuous,
  desc = "Position scale, exponential transformed",
  tr_default = Trans$find("exp"),
  objname = "exp",
  doc=FALSE,
  examples=function(.) {}
)

ScaleLogit <-    proto(ScaleContinuous,
  desc = "Position scale, logit transformed",
  tr_default = Trans$find("logit"),
  objname = "logit",
  doc=FALSE,
  examples=function(.) {}
)

ScaleReverse <-    proto(ScaleContinuous,
  desc = "Position scale, axis direction reversed",
  tr_default = Trans$find("reverse"),
  objname = "reverse",
  doc=FALSE,
  examples=function(.) {}
)

ScaleAsn <-      proto(ScaleContinuous,
  desc = "Position scale, arc-sin transformed",
  tr_default = Trans$find("asn"),
  objname = "asn",
  doc=FALSE,
  examples=function(.) {}
)

ScaleProbit <-   proto(ScaleContinuous,
  desc = "Position scale, probit transformed",
  tr_default = Trans$find("probit"),
  objname = "probit",
  doc=FALSE,
  examples=function(.) {}
)

ScaleAtanh <-    proto(ScaleContinuous,
  desc = "Position scale, arc-hyperbolic tangent transformed",
  tr_default = Trans$find("atanh"),
  objname = "atanh",
  doc=FALSE,
  examples=function(.) {}
)

ScaleInverse <-  proto(ScaleContinuous,
  desc = "Position scale, inverse transformed",
  tr_default = Trans$find("inverse"),
  objname = "inverse",
  doc=FALSE,
  examples=function(.) {}
)
ScaleRecip <-  proto(ScaleContinuous,
  desc = "Position scale, reciprocal",
  tr_default = Trans$find("inverse"),
  objname = "recip",
  doc=FALSE,
  examples=function(.) {}
)

ScaleContinuous$tr_default <- Trans$find("identity")

ScaleProb <- proto(ScaleContinuous, {
  doc <- FALSE
  objname <- "prob"
  desc <- "Probability scale"
  icon <- function(.) {
    textGrob("P()", gp=gpar(cex=1.5))
  }
  new <- function(., name=NULL, limits=c(NA,NA), breaks=NULL, family="norm", variable="x") {
    .$proto(name=name, .input=variable, .output=variable, limits=limits, breaks = breaks, .tr = ProbabilityTrans$new(family), family=family)
  }
  examples <- function(.) {
    # Coming soon
  }
#  output_set <- function(.) c(0, 1)
})

ScalePow <- proto(ScaleContinuous, {
  doc <- FALSE
  objname <- "pow"
  desc <- "Power scale"
  icon <- function(.) {
    textGrob(expression(frac(x ^ (alpha - 1), alpha)), gp=gpar(cex=1.2))
  }
  new <- function(., name=NULL, limits=c(NA,NA), breaks=NULL, power=1, variable) {
    .$proto(name=name, .input=variable, .output=variable, limits=limits, breaks = breaks, .tr = PowerTrans$new(power), power=power)
  }
  examples <- function(.) {
    # Coming soon
  }
})
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-break.r"
# Discretise continuous variable, equal interval length.
# Cut numeric vector into intervals of equal length.
# 
# @arguments numeric vector
# @arguments number of intervals to create, OR
# @arguments length of each interval
# @arguments other arguments passed on to \code{\link{cut}}
# @keyword manip
# @seealso \code{\link{cut_number}}
# 
#X table(cut_interval(1:100, n = 10))
#X table(cut_interval(1:100, n = 11))
#X table(cut_interval(1:100, length = 10))
cut_interval <- function(x, n = NULL, length = NULL, ...) {
  cut(x, breaks(x, "width", n, length), include.lowest = TRUE, ...)
}

# Discretise continuous variable, equal number of points.
# Cut numeric vector into intervals containing equal number of points.
# 
# @arguments numeric vector
# @arguments number of intervals to create, OR
# @arguments length of each interval
# @arguments other arguments passed on to \code{\link{cut}}
# @keyword manip
# @seealso \code{\link{cut_interval}}
#X table(cut_number(runif(1000), n = 10))
cut_number <- function(x, n = NULL, ...) {
  cut(x, breaks(x, "n", n), include.lowest = TRUE, ...)
}

# Discretise continuous vector
# Method that powers \code{\link{cut_number}} and \code{\link{cut_interval}}
# @keyword internal
breaks <- function(x, equal, nbins = NULL, binwidth = NULL) {
  equal <- match.arg(equal, c("numbers", "width"))
  if ((!is.null(nbins) && !is.null(binwidth)) || (is.null(nbins) && is.null(binwidth))) {
    stop("Specify exactly one of n and width")
  }
  
  rng <- range(x, na.rm = TRUE, finite = TRUE)
  if (equal == "width") {
    if (!is.null(binwidth)) {
      fullseq(rng, binwidth)
    } else {
      seq(rng[1], rng[2], length = nbins + 1)
    }
  } else {
    if (!is.null(binwidth)) {
      probs <- seq(0, 1, by = binwidth)
    } else {
      probs <- seq(0, 1, length = nbins + 1)
    }
    quantile(x, probs, na.rm = TRUE)
  }
  
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-colour.r"
# Nice colour ramp
# Wrapper for colorRamp that deals with missing values and alpha
# 
# @keyword internal
nice_ramp <- function(ramp, x, alpha = 1) {
  cols <- ramp(x)
  missing <- !complete.cases(x)
  cols[missing, ] <- 0
  colour <- rgb(cols[, 1], cols[, 2], cols[, 3], maxColorValue = 255)
  colour <- alpha(colour, alpha)
  colour[missing] <- NA
  
  colour
}

# alpha
# Give a colour an alpha level
# 
# @arguments colour
# @arguments alpha level [0,1]
# @keyword internal 
alpha <- function(colour, alpha) {
  alpha[is.na(alpha)] <- 0
  col <- col2rgb(colour, TRUE) / 255
  
  if (length(colour) != length(alpha)) {
    if (length(colour) > 1 && length(alpha) > 1) {
      stop("Only one of colour and alpha can be vectorised")
    }
    
    if (length(colour) > 1) {
      alpha <- rep(alpha, length.out = length(colour))    
    } else if (length(alpha) > 1) {
      col <- col[, rep(1, length(alpha)), drop = FALSE]
    }
  }
  # Only set if colour is opaque
  col[4, ] <- ifelse(col[4, ] == 1, alpha, col[4, ])

  new_col <- rgb(col[1,], col[2,], col[3,], col[4,])
  new_col[is.na(colour)] <- NA  
  new_col
}

# Modify standard R colour in hcl colour space
# Transforms rgb to hcl, sets non-missing arguments and then backtransforms to rgb
#
# @keyword internal
# @examples col2hcl(colors())
col2hcl <- function(colour, h, c, l, alpha = 1) {
  try_require("colorspace")
  
  col <- colorspace::RGB(t(col2rgb(colour)) / 256)
  coords <- colorspace::coords(as(col, "polarLUV"))
  
  if (missing(h)) h <- coords[, "H"]
  if (missing(c)) c <- coords[, "C"]
  if (missing(l)) l <- coords[, "L"]
    
  hcl_colours <- hcl(h, c, l, alpha = alpha) 
  names(hcl_colours) <- names(colour) 
  hcl_colours
}

# Mute standard R colours.
# This produces colours with moderate luminance and saturation.
# 
# @keyword internal
muted <- function(colour, l=30, c=70) col2hcl(colour, l=l, c=c)

# Add a missing colour to a colour palette.
# Convenient method.
# 
# @keyword internal
missing_colour <- function(palette, missing, na.colour) {
  output <- character(length(missing))
  output[which(!missing)] <- palette
  output[which(missing)] <-  na.colour
  output
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-discrete.r"
# Calculate range for discrete position variables
# This is the equivalent of range for discrete variables 
# 
# @keyword internal
discrete_range <- function(..., drop = FALSE) {
  pieces <- list(...)
  
  clevels <- function(x) {
    if (is.null(x)) return(character())
    
    if (is.factor(x)) {
      if (drop) x <- factor(x)
      values <- levels(x)
    } else if (is.numeric(x)) {
      values <- unique(x)
    } else {
      values <- as.character(unique(x)) 
    }
    if (any(is.na(x))) values <- c(values, NA)
    values
  }
  all <- unique(unlist(lapply(pieces, clevels)))
  if (is.numeric(all)) {
    all <- all[order(all)]
    all <- as.character(all)
  }
  
  all
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-facet.r"
# Adding missing levels
# Ensure all data frames in list have same levels for selected variables
# 
# @keyword internal
add_missing_levels <- function(dfs, levels) {
  
  lapply(dfs, function(df) {
    for(var in intersect(names(df), names(levels))) {
      df[var] <- factor(df[, var], levels = ulevels(levels[[var]]))
    }
    df
  })
}

# Unique levels
# Get unique levels of vector
# 
# @keyword internal
ulevels <- function(x) {
  if (is.factor(x)) {
    levels(factor(x))
  } else {
    sort(unique(x))
  }
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-grid.r"
# Name ggplot grid object
# Convenience function to name grid objects
# 
# @keyword internal
ggname <- function(prefix, grob) {
  grob$name <- grobName(grob, prefix)
  grob
}

# Global grob editing
# To match grid.gedit
# 
# @keyword internal
geditGrob <- function(..., grep = TRUE, global = TRUE) {
  editGrob(..., grep = grep, global = global)
}

# Grob row heights
# Given a matrix of grobs, calculate the height needed for each row
# 
# @arguments matrix of grobs
# @keyword internal
grobRowHeight <- function(mat) {
  row_heights <- alply(mat, 1, function(x) llply(x, grobHeight))
  do.call("unit.c", llply(row_heights, splat(max)))  
}

# Grob column widths
# Given a matrix of grobs, calculate the width needed for each column
# 
# @arguments matrix of grobs
# @keyword internal
grobColWidth <- function(mat) {
  col_widths <- alply(mat, 2, function(x) llply(x, grobWidth))
  do.call("unit.c", llply(col_widths, splat(max)))  
}

# Build grob matrix
# Build a matrix of grobs given a vector of grobs and the desired dimensions of the matrix
# 
# Any missing cells at the end will be filled in with zeroGrobs.
# 
# @arguments vector of grobs
# @arguments number of rows
# @arguments number of columns
# @arguments should the matrix be arranged like a table or a plot
# @keyword internal
grobMatrix <- function(vec, nrow, ncol, as.table = FALSE) {
  if (nrow == 0 || ncol == 0) {
    return(matrix(ncol = ncol, nrow = nrow))
  }
  
  mat <- c(vec, rep(list(zeroGrob()), nrow * ncol - length(vec)))
  dim(mat) <- c(ncol, nrow)
  mat <- t(mat)
  if (!as.table) mat <- mat[rev(seq_len(nrow)), ]
  
  mat
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-layer.r"
# Are integers?
# Determine if a vector contains only integers
# 
# @arguments vector to test
# @keyword internal
#X is.integeric(runif(100))
#X is.integeric(rpois(100, 10))
#X is.integeric(1:10)
is.integeric <- function(x) all(floor(x) == x)

# Add group
# Ensure that the data frame contains a grouping variable.
#
# If the \code{group} variable is not present, then a new group
# variable is generated from the interaction of all discrete (factor or
# character) vectors excluding label.
# 
# @arguments data.frame
# @value data.frame with group variable
# @keyword internal
add_group <- function(data) {
  if (empty(data)) return(zeroGrob())
  
  if (is.null(data$group)) {
    cat <- sapply(data[setdiff(names(data), "label")], is.discrete)
    cat <- intersect(names(which(cat)), .all_aesthetics)
    
    if (length(cat) == 0) {
      data$group <- 1
    } else {
      data$group <- as.numeric(interaction(data[cat]))
    }
  }
  data$group <- as.numeric(factor(data$group, exclude = NULL))
  data
}

# Force matrix
# If not already a matrix, make a 1x1 matrix
# 
# @arguments object to make into a matrix
# @keyword internal
force_matrix <- function(x) {
  if (!is.matrix(x)) {
    mat <- list(x)
    dim(mat) <- c(1,1)
    mat
  } else {
    x
  }
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-matrix.r"

# Row weave
# Weave together two (or more) matrices by row
# 
# Matrices must have same dimensions
# 
# @arguments matrices to weave together
# @keyword internal
#X a <- matrix(1:10 * 2, ncol = 2)
#X b <- matrix(1:10 * 3, ncol = 2)
#X c <- matrix(1:10 * 5, ncol = 2)
rweave <- function(...) UseMethod("rweave")
rweave.list <- function(...) do.call("rweave", ...)
rweave.matrix <- function(...) {
  matrices <- list(...)
  stopifnot(equal_dims(matrices))
  
  n <- nrow(matrices[[1]])
  p <- length(matrices)
  
  interleave <- rep(1:n, each = p) + seq(0, p - 1) * n
  do.call("rbind", matrices)[interleave, , drop = FALSE]
}

# Col union
# Form the union of columns in a and b.  If there are columns of the same name in both a and b, take the column from a.
# 
# @arguments data frame a
# @arguments data frame b
# @keyword internal
cunion <- function(a, b) {
  if (length(a) == 0) return(b)
  if (length(b) == 0) return(a)
  
  cbind(a, b[setdiff(names(b), names(a))])
}

# Col weave
# Weave together two (or more) matrices by column
# 
# Matrices must have same dimensions
# 
# @arguments matrices to weave together
# @keyword internal
cweave <- function(...) UseMethod("cweave")
cweave.list <- function(...) do.call("cweave", ...)
cweave.matrix <- function(...) {
  matrices <- list(...)
  stopifnot(equal_dims(matrices))
  
  n <- ncol(matrices[[1]])
  p <- length(matrices)

  interleave <- rep(1:n, each = p) + seq(0, p - 1) * n
  do.call("cbind", matrices)[, interleave, drop = FALSE]
}

# Interleave vectors
# Interleave (or zip) multiple vectors into a single vector
# 
# @arguments vectors to interleave
# @keyword internal
interleave <- function(...) UseMethod("interleave")
interleave.list <- function(...) do.call("interleave", ...)
interleave.unit <- function(...) {
  do.call("unit.c", do.call("interleave.default", llply(list(...), as.list)))
}
interleave.default <- function(...) {
  vectors <- list(...)
  
  # Check lengths 
  lengths <- unique(setdiff(laply(vectors, length), 1))
  if (length(lengths) == 0) lengths <- 1
  stopifnot(length(lengths) <= 1)
  
  # Replicate elements of length one up to correct length
  singletons <- laply(vectors, length) == 1
  vectors[singletons] <- llply(vectors[singletons], rep, lengths)
  
  # Interleave vectors
  n <- lengths
  p <- length(vectors)
  interleave <- rep(1:n, each = p) + seq(0, p - 1) * n
  unlist(vectors, recursive=FALSE)[interleave]
}

# Equal dims?
# Check that a list of matrices have equal dimensions
# 
# @arguments list of matrices
# @keyword internal
equal_dims <- function(matrices) {
  are.matrices <- laply(matrices, is.matrix)
  stopifnot(all(are.matrices))
  
  cols <- laply(matrices, ncol)
  rows <- laply(matrices, ncol)

  length(unique(cols) == 1) && length(unique(rows) == 1)
} 
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-position.r"
# Expand range
# Convenience function for expanding a range with a multiplicative or additive constant.
# 
# @arguments range of data
# @arguments multiplicative constract
# @arguments additive constant
# @arguments distance to use if range has zero width
# @keyword manip 
expand_range <- function(range, mul = 0, add = 0, zero = 0.5) {
  if (length(range) == 1 || diff(range) == 0) {
    c(range[1] - zero, range[1] + zero)
  } else {    
    range + c(-1, 1) * (diff(range) * mul + add)
  }
}

# Trim infinite.
# Trim non-finite numbers to specified range
# 
# @keyword internal
# @alias trim_infinite_01
trim_infinite <- function(x, range) {
  x[x == -Inf] <- range[1]
  x[x == Inf] <- range[2]
  x
}

trim_infinite_01 <- function(x) {
  trim_infinite(x, c(0, 1))
}


#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities-resolution.r"
# Resolution
# Compute the "resolution" of a data vector, ie. what is the smallest non-zero
# distance between adjacent values.
#
# If there is only one unique value, then the resolution is defined to be one. 
# 
# @arguments numeric vector
# @arguments should a zero value be automatically included in the computation of resolution
# @keyword hplot
# @keyword internal 
#X resolution(1:10)
#X resolution((1:10) - 0.5)
#X resolution((1:10) - 0.5, FALSE)
#X resolution(c(1,2, 10, 20, 50))
resolution <- function(x, zero = TRUE) {
  x <- unique(as.numeric(x))
  if (length(x) == 1) return(1)

  if (zero) {
    x <- unique(c(0, x))
  }
  
  min(diff(sort(x)))
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/utilities.r"

# Null default
# Analog of || from ruby
# 
# @keyword internal
# @name nulldefault-infix
"%||%" <- function(a, b) {
  if (!is.null(a)) a else b
}

# Check required aesthetics are present
# This is used by geoms and stats to give a more helpful error message
# when required aesthetics are missing.
#
# @arguments character vector of required aesthetics
# @arguments character vector of present aesthetics
# @arguments name of object for error message
# @keyword internal
check_required_aesthetics <- function(required, present, name) {
  missing_aes <- setdiff(required, present)
  if (length(missing_aes) == 0) return()

  stop(name, " requires the following missing aesthetics: ", paste(missing_aes, collapse=", "), call. = FALSE)
}

# Concatenate a named list for output
# Print a \code{list(a=1, b=2)} as \code{(a=1, b=2)}
# 
# @arguments list to concatenate
# @keyword internal
#X clist(list(a=1, b=2))
#X clist(par()[1:5])
clist <- function(l) {
  paste(paste(names(l), l, sep=" = ", collapse=", "), sep="")
}

# Abbreviated paste
# Alias for paste with a shorter name and convenient defaults
# 
# @arguments character vectors to be concatenated
# @arguments default separator
# @arguments default collapser
# @keyword internal
ps <- function(..., sep="", collapse="") do.call(paste, compact(list(..., sep=sep, collapse=collapse)))

# Quietly try to require a package
# Queitly require a package, returning an error message if that package is not installed.
# 
# @arguments name of package
# @keyword internal
try_require <- function(package) {
  available <- suppressMessages(suppressWarnings(sapply(package, require, quietly = TRUE, character.only = TRUE, warn.conflicts=FALSE)))
  missing <- package[!available]

  if (length(missing) > 0) 
    stop(paste(package, collapse=", "), " package required for this functionality.  Please install and try again.", call. = FALSE)
}

# Return unique columns
# This is used for figuring out which columns are constant within a group
# 
# @keyword internal
uniquecols <- function(df) {
  df <- df[1, sapply(df, function(x) length(unique(x)) == 1), drop=FALSE]
  rownames(df) <- 1:nrow(df)
  df
}

# A "safe" version of do.call
# \code{safe.call} works like \code{\link{do.call}} but it will only supply arguments that exist in the function specification.
# 
# If ... is present in the param list, all parameters will be passed through
# unless \code{ignore.dots = TRUE}.  Positional arguments are not currently
# supported.
# 
# @arguments function to call
# @arugments named list of parameters to be supplied to function
# @arguments parameter names of function
# @arguments 
# @keyword internal
safe.call <- function(f, params, f.params = names(formals(f)), ignore.dots = TRUE) {
  if (!ignore.dots && "..." %in% f.params) {
    safe.params <- params
  } else {
    safe.params <- params[intersect(f.params, names(params))]    
  }
  do.call(f, safe.params)
}

# Convenience function to remove missing values from a data.frame
# Remove all non-complete rows, with a warning if \code{na.rm = FALSE}.
# 
# ggplot is somewhat more accomodating of missing values than R generally.
# For those stats which require complete data, missing values will be 
# automatically removed with a warning.  If \code{na.rm = TRUE} is supplied
# to the statistic, the warning will be suppressed.
# 
# @arguments data.frame
# @arguments suppress warning that rows are being removed?
# @argumnets variables to check for missings in
# @arguments optional function name to make warning message more informative
# @keyword internal
#X a <- remove_missing(movies)
#X a <- remove_missing(movies, na.rm = TRUE)
#X qplot(mpaa, budget, data=movies, geom="boxplot")
remove_missing <- function(df, na.rm=FALSE, vars = names(df), name="") {
  vars <- intersect(vars, names(df))
  if (name != "") name <- ps(" (", name, ")")
  missing <- !complete.cases(df[, vars])
  if (any(missing)) {
    df <- df[!missing, ]
    if (!na.rm) warning("Removed ", sum(missing), " rows containing missing values", name, ".", call. = FALSE)
  }


  df
}

# Traceback alias
# Alias of traceback with fewer keypresses, and severe restriction on number of lines for each function
# 
# @keyword manip 
# @keyword internal
tr <- function(x = NULL) traceback(x, max.lines=1)

# Rescale numeric vector
# Rescale numeric vector to have specified minimum and maximum.
# If vector has length one, it is not rescaled, but is restricted to the range.
#
# @arguments data to rescale
# @arguments range to scale to
# @arguments range to scale from, defaults to range of data
# @arguments should values be clipped to specified range?
# @keyword manip
rescale <- function(x, to=c(0,1), from=range(x, na.rm=TRUE), clip = TRUE) {
  if (length(to) == 1 || abs(to[1] - to[2]) < 1e-6) return(to[1])
  if (length(from) == 1 || abs(from[1] - from[2]) < 1e-6) return(mean(to))

  if (is.factor(x)) {
    warning("Categorical variable automatically converted to continuous", call.=FALSE)
    x <- as.numeric(x)
  }
  scaled <- (x - from[1]) / diff(from) * diff(to) + to[1]

  if (clip) {
    ifelse(!is.finite(scaled) | scaled %inside% to, scaled, NA) 
  } else {
    scaled
  }
}


# "Invert" a list
# Keys become values, values become keys
# 
# @arguments list to invert
# @keyword internal
invert <- function(L) {
  t1 <- unlist(L)
  names(t1) <- rep(names(L), lapply(L, length))
  tapply(names(t1), t1, c)
}

# Inside
# Return logical vector indicating if x is inside the interval
# 
# @keyword internal
"%inside%" <- function(x, interval) {
  x >= interval[1] & x <= interval[2]
}

# Expression should raise an error
# Used in examples to illustrate when errors should occur
#
# @keyword internal
should_stop <- function(expr) {
  res <- try(print(force(expr)), TRUE)
  if (!inherits(res, "try-error")) stop("No error!", call. = FALSE)
  invisible()
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/xxx-codegen.r"
# Print accessors
# Write out all convenience accessor functions to R file.
#
# @keyword internal
accessors_print <- function(file = "") {
  funs <- sort(c(
    Geom$accessors(), Stat$accessors(), Scale$accessors(),
    Coord$accessors(),  Position$accessors(), Facet$accessors()
  ))
  cat(funs, file=file, sep="")
}

TopLevel$accessors <- function(.) {
  accessors <- lapply(.$find_all(), function(y) y$create_accessor())
  unname(unlist(accessors))
}
 
TopLevel$create_accessor <- function(.) {
  paste(.$my_name(), " <- ", .$myName(), "$build_accessor()\n", sep="")
}
Scale$create_accessor <- function(.) {
  if (is.null(.$common)) {
    var <- NULL
    short <- paste(.$class(), .$objname, sep="_")
  } else {
    var <- paste("list(variable = \"\\\"", .$common, "\\\"\")", sep="")
    short <- paste(.$class(), .$common, .$objname, sep="_")
  }

  paste(short, " <- ", .$myName(), "$build_accessor(", var, ")\n", sep="")
}


TopLevel$build_accessor <- function(., extra_args = c()) {
  layer <- if (.$class() %in% c("geom","stat")) c(
    list(mapping=NULL,data=NULL),
    compact(list(
      geom = if (exists("default_geom", .)) .$default_geom()$objname, 
      stat = if (exists("default_stat", .)) .$default_stat()$objname, 
      position = if (exists("default_pos", .)) .$default_pos()$objname
    ))
  )
  params <- .$params()
  params <- params[names(params) != "..."]
  if (.$class() %in% c("geom","stat")) params <- params[!sapply(params, is.null)]
  args <- c(layer, params)
  
  body <- ps(
    .$myName(), "$", "new(",
    if (length(args) > 0) ps(names(args),"=", names(args), collase =", "), 
    if (length(extra_args) > 0) ps(names(extra_args),"=", extra_args, collase =", "), 
    "...",
    ")"
  )
  f <- function() {}
  formals(f) <- as.pairlist(c(args, alist(... =)))
  body(f) <- parse(text = body)
  environment(f) <- globalenv()
  f
}
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/xxx-digest.r"
bolus <- function(x) UseMethod("bolus")
bolus.proto <- function(x) x$bolus()

# Create a bolus object
# A bolus is a list suitable for digesting.
# 
# Most ggplot objects have components that should be hashed when creating
# a digest (especially since most ggplot objects are proto objects and 
# are also self-documenting).  The bolus methods ensure that only appropriate
# components are digested.
#
# @alias bolus
# @alias bolus.proto
# @alias digest.ggplot
# @alias digest.proto
# @keyword internal
#X hash_tests <- list(
#X   list(
#X     ggplot() + scale_x_continuous() + scale_y_continuous(),
#X     ggplot() + scale_y_continuous() + scale_x_continuous()
#X   ),
#X   list(
#X     qplot(mpg, wt, data=mtcars, na.rm = FALSE),
#X     ggplot(mtcars, aes(y=wt, x=mpg)) + geom_point()
#X   ),
#X   list(
#X     qplot(mpg, wt, data=mtcars, xlab = "blah"),
#X     qplot(mpg, wt, data=mtcars) + xlab("blah")
#X   )
#X )
#X 
#X lapply(hash_tests, function(equal) {
#X   hashes <- lapply(equal, digest.ggplot)
#X   
#X   if (length(unique(hashes)) != 1) {
#X     lapply(equal, function(x) print(str(bolus(x))))
#X     stop("Above plots not equal")
#X   }
#X })
bolus.ggplot <- function(x, ...) {
  sort.by.name <- function(x) {
    if (is.null(names(x))) return(x)
    x[order(names(x))]
  }
  
  with(x, list(
    data = digest::digest(data),
    mapping = sort.by.name(mapping),
    layers = sapply(layers, function(x) x$hash()),
    scales = scales$hash(),
    facet = facet$hash(),
    coord = coordinates$hash(),
    options = digest::digest(defaults(x$options, theme_get()))
  ))
}

digest.proto <- function(x, ...) x$hash(, ...)
digest.ggplot <- function(x, ...) {
  if (is.null(x)) return()
  digest::digest(bolus(x), ...)
}

TopLevel$settings <- function(.) {
  mget(setdiff(ls(., all.names=TRUE), c(".that", ".super")), .)
}

Layer$hash <- TopLevel$hash <- function(., ...) {
  digest::digest(.$bolus(), ...)
}
Scales$hash <- function(.) {
  scales <- sapply(.$.scales, function(x) x$hash())
  if (is.character(scales)) scales <- sort(scales)
  scales
}

Scales$bolus <- function(.) {
  sc <- lapply(.$.scales, function(x) x$bolus())
  names(sc) <- sapply(sc, "[[", "input")
  sc[order(names(sc))]
}
TopLevel$bolus <- function(.) {
  list(
    name = .$objname,
    settings = .$settings()
  )
}
Scale$bolus <- function(.) {
  settings <- .$settings()
  settings$.tr <- settings$.tr.$objname
  settings$.input <- NULL
  settings$.output <- NULL
  
  list(
    name = .$objname,
    input = .$.input,
    output = .$.output,
    settings = compact(settings)
  )
}
Layer$bolus <- function(.) {
  params <- c(.$geom_params, .$stat_params)
  params <- params[!duplicated(params)]
  if (!is.null(params) && length(params) > 1) params <- params[order(names(params))]
  
  mapping <- .$mapping
  if (!is.null(mapping)) mapping <- mapping[order(names(mapping))]
  
  list(
    geom = .$geom$objname,
    stat = .$stat$objname,
    pos  = .$position$objname,
    pos_parms  = .$position$settings(),
    data = .$data,
    mapping = mapping,
    params = params,
    legend = .$legend
  )
}

#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/xxx.r"
coord_cartesian <- CoordCartesian$build_accessor()
coord_fixed <- CoordFixed$build_accessor()
coord_flip <- CoordFlip$build_accessor()
coord_map <- CoordMap$build_accessor()
coord_polar <- CoordPolar$build_accessor()
coord_trans <- CoordTrans$build_accessor()
facet_grid <- FacetGrid$build_accessor()
facet_wrap <- FacetWrap$build_accessor()
geom_abline <- GeomAbline$build_accessor()
geom_area <- GeomArea$build_accessor()
geom_bar <- GeomBar$build_accessor()
geom_bin2d <- GeomBin2d$build_accessor()
geom_blank <- GeomBlank$build_accessor()
geom_boxplot <- GeomBoxplot$build_accessor()
geom_contour <- GeomContour$build_accessor()
geom_crossbar <- GeomCrossbar$build_accessor()
geom_density <- GeomDensity$build_accessor()
geom_density2d <- GeomDensity2d$build_accessor()
geom_errorbar <- GeomErrorbar$build_accessor()
geom_errorbarh <- GeomErrorbarh$build_accessor()
geom_freqpoly <- GeomFreqpoly$build_accessor()
geom_hex <- GeomHex$build_accessor()
geom_histogram <- GeomHistogram$build_accessor()
geom_hline <- GeomHline$build_accessor()
geom_jitter <- GeomJitter$build_accessor()
geom_line <- GeomLine$build_accessor()
geom_linerange <- GeomLinerange$build_accessor()
geom_path <- GeomPath$build_accessor()
geom_point <- GeomPoint$build_accessor()
geom_pointrange <- GeomPointrange$build_accessor()
geom_polygon <- GeomPolygon$build_accessor()
geom_quantile <- GeomQuantile$build_accessor()
geom_rect <- GeomRect$build_accessor()
geom_ribbon <- GeomRibbon$build_accessor()
geom_rug <- GeomRug$build_accessor()
geom_segment <- GeomSegment$build_accessor()
geom_smooth <- GeomSmooth$build_accessor()
geom_step <- GeomStep$build_accessor()
geom_text <- GeomText$build_accessor()
geom_tile <- GeomTile$build_accessor()
geom_vline <- GeomVline$build_accessor()
position_dodge <- PositionDodge$build_accessor()
position_fill <- PositionFill$build_accessor()
position_identity <- PositionIdentity$build_accessor()
position_jitter <- PositionJitter$build_accessor()
position_stack <- PositionStack$build_accessor()
scale_alpha_continuous <- ScaleAlphaContinuous$build_accessor()
scale_alpha_identity <- ScaleIdentity$build_accessor(list(variable = "\"alpha\""))
scale_area <- ScaleArea$build_accessor()
scale_colour <- ScaleColour$build_accessor()
scale_colour_brewer <- ScaleBrewer$build_accessor(list(variable = "\"colour\""))
scale_colour_gradient <- ScaleGradient$build_accessor(list(variable = "\"colour\""))
scale_colour_gradient2 <- ScaleGradient2$build_accessor(list(variable = "\"colour\""))
scale_colour_gradientn <- ScaleGradientn$build_accessor(list(variable = "\"colour\""))
scale_colour_grey <- ScaleGrey$build_accessor(list(variable = "\"colour\""))
scale_colour_hue <- ScaleHue$build_accessor(list(variable = "\"colour\""))
scale_colour_identity <- ScaleIdentity$build_accessor(list(variable = "\"colour\""))
scale_colour_manual <- ScaleManual$build_accessor(list(variable = "\"colour\""))
scale_discrete <- ScaleDiscrete$build_accessor()
scale_fill_brewer <- ScaleBrewer$build_accessor(list(variable = "\"fill\""))
scale_fill_gradient <- ScaleGradient$build_accessor(list(variable = "\"fill\""))
scale_fill_gradient2 <- ScaleGradient2$build_accessor(list(variable = "\"fill\""))
scale_fill_gradientn <- ScaleGradientn$build_accessor(list(variable = "\"fill\""))
scale_fill_grey <- ScaleGrey$build_accessor(list(variable = "\"fill\""))
scale_fill_hue <- ScaleHue$build_accessor(list(variable = "\"fill\""))
scale_fill_identity <- ScaleIdentity$build_accessor(list(variable = "\"fill\""))
scale_fill_manual <- ScaleManual$build_accessor(list(variable = "\"fill\""))
scale_linetype_discrete <- ScaleLinetypeDiscrete$build_accessor()
scale_linetype_identity <- ScaleIdentity$build_accessor(list(variable = "\"linetype\""))
scale_linetype_manual <- ScaleManual$build_accessor(list(variable = "\"linetype\""))
scale_shape_discrete <- ScaleShapeDiscrete$build_accessor()
scale_shape_identity <- ScaleIdentity$build_accessor(list(variable = "\"shape\""))
scale_shape_manual <- ScaleManual$build_accessor(list(variable = "\"shape\""))
scale_size_continuous <- ScaleSizeContinuous$build_accessor()
scale_size_discrete <- ScaleSizeDiscrete$build_accessor()
scale_size_identity <- ScaleIdentity$build_accessor(list(variable = "\"size\""))
scale_size_manual <- ScaleManual$build_accessor(list(variable = "\"size\""))
scale_x_asn <- ScaleAsn$build_accessor(list(variable = "\"x\""))
scale_x_atanh <- ScaleAtanh$build_accessor(list(variable = "\"x\""))
scale_x_continuous <- ScaleContinuous$build_accessor(list(variable = "\"x\""))
scale_x_date <- ScaleDate$build_accessor(list(variable = "\"x\""))
scale_x_datetime <- ScaleDatetime$build_accessor(list(variable = "\"x\""))
scale_x_discrete <- ScaleDiscretePosition$build_accessor(list(variable = "\"x\""))
scale_x_exp <- ScaleExp$build_accessor(list(variable = "\"x\""))
scale_x_inverse <- ScaleInverse$build_accessor(list(variable = "\"x\""))
scale_x_log <- ScaleLog$build_accessor(list(variable = "\"x\""))
scale_x_log10 <- ScaleLog10$build_accessor(list(variable = "\"x\""))
scale_x_log1p <- ScaleLog1p$build_accessor(list(variable = "\"x\""))
scale_x_log2 <- ScaleLog2$build_accessor(list(variable = "\"x\""))
scale_x_logit <- ScaleLogit$build_accessor(list(variable = "\"x\""))
scale_x_pow <- ScalePow$build_accessor(list(variable = "\"x\""))
scale_x_pow10 <- ScalePow10$build_accessor(list(variable = "\"x\""))
scale_x_prob <- ScaleProb$build_accessor(list(variable = "\"x\""))
scale_x_probit <- ScaleProbit$build_accessor(list(variable = "\"x\""))
scale_x_recip <- ScaleRecip$build_accessor(list(variable = "\"x\""))
scale_x_reverse <- ScaleReverse$build_accessor(list(variable = "\"x\""))
scale_x_sqrt <- ScaleSqrt$build_accessor(list(variable = "\"x\""))
scale_y_asn <- ScaleAsn$build_accessor(list(variable = "\"y\""))
scale_y_atanh <- ScaleAtanh$build_accessor(list(variable = "\"y\""))
scale_y_continuous <- ScaleContinuous$build_accessor(list(variable = "\"y\""))
scale_y_date <- ScaleDate$build_accessor(list(variable = "\"y\""))
scale_y_datetime <- ScaleDatetime$build_accessor(list(variable = "\"y\""))
scale_y_discrete <- ScaleDiscretePosition$build_accessor(list(variable = "\"y\""))
scale_y_exp <- ScaleExp$build_accessor(list(variable = "\"y\""))
scale_y_inverse <- ScaleInverse$build_accessor(list(variable = "\"y\""))
scale_y_log <- ScaleLog$build_accessor(list(variable = "\"y\""))
scale_y_log10 <- ScaleLog10$build_accessor(list(variable = "\"y\""))
scale_y_log1p <- ScaleLog1p$build_accessor(list(variable = "\"y\""))
scale_y_log2 <- ScaleLog2$build_accessor(list(variable = "\"y\""))
scale_y_logit <- ScaleLogit$build_accessor(list(variable = "\"y\""))
scale_y_pow <- ScalePow$build_accessor(list(variable = "\"y\""))
scale_y_pow10 <- ScalePow10$build_accessor(list(variable = "\"y\""))
scale_y_prob <- ScaleProb$build_accessor(list(variable = "\"y\""))
scale_y_probit <- ScaleProbit$build_accessor(list(variable = "\"y\""))
scale_y_recip <- ScaleRecip$build_accessor(list(variable = "\"y\""))
scale_y_reverse <- ScaleReverse$build_accessor(list(variable = "\"y\""))
scale_y_sqrt <- ScaleSqrt$build_accessor(list(variable = "\"y\""))
scale_z_discrete <- ScaleDiscretePosition$build_accessor(list(variable = "\"z\""))
stat_abline <- StatAbline$build_accessor()
stat_bin <- StatBin$build_accessor()
stat_bin2d <- StatBin2d$build_accessor()
stat_binhex <- StatBinhex$build_accessor()
stat_boxplot <- StatBoxplot$build_accessor()
stat_contour <- StatContour$build_accessor()
stat_density <- StatDensity$build_accessor()
stat_density2d <- StatDensity2d$build_accessor()
stat_function <- StatFunction$build_accessor()
stat_hline <- StatHline$build_accessor()
stat_identity <- StatIdentity$build_accessor()
stat_qq <- StatQq$build_accessor()
stat_quantile <- StatQuantile$build_accessor()
stat_smooth <- StatSmooth$build_accessor()
stat_spoke <- StatSpoke$build_accessor()
stat_sum <- StatSum$build_accessor()
stat_summary <- StatSummary$build_accessor()
stat_unique <- StatUnique$build_accessor()
stat_vline <- StatVline$build_accessor()
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/zxx.r"
scale_colour_discrete <- scale_colour_hue
scale_colour_continuous <- scale_colour_gradient
scale_fill_discrete <- scale_fill_hue
scale_fill_continuous <- scale_fill_gradient

# British to American spellings
scale_color_brewer <- scale_colour_brewer
scale_color_continuous <- scale_colour_gradient
scale_color_discrete <- scale_colour_hue
scale_color_gradient <- scale_colour_gradient
scale_color_gradient2 <- scale_colour_gradient2
scale_color_gradientn <- scale_colour_gradientn
scale_color_grey <- scale_colour_grey
scale_color_hue <- scale_colour_hue
scale_color_identity <- scale_colour_identity
scale_color_manual <- scale_colour_manual

# Single name scales
scale_size <- scale_size_continuous
scale_linetype <- scale_linetype_discrete
scale_alpha <- scale_alpha_continuous
scale_shape <- scale_shape_discrete

coord_equal <- coord_fixed
#line 1 "d:/RCompile/CRANpkg/local/2.13/ggplot2/R/coord-munch.r"
# For munching, only grobs are lines and polygons: everything else is 
# transfomed into those special cases by the geom.  
#
# @arguments distance, scaled from 0 to 1 (maximum distance on plot)
# @keyword internal
munch_data <- function(data, dist = NULL, segment_length = 0.01) {
  n <- nrow(data)
  
  if (is.null(dist)) {
    data <- add_group(data)
    dist <- dist_euclidean(data$x, data$y)
  }
  
  # How many pieces for each old segment
  extra <- floor(dist / segment_length) + 1
  extra[is.na(extra)] <- 1

  # Generate extra pieces for x and y values
  x <- unlist(mapply(interp, data$x[-n], data$x[-1], extra, SIMPLIFY = FALSE))
  y <- unlist(mapply(interp, data$y[-n], data$y[-1], extra, SIMPLIFY = FALSE))

  # Replicate other aesthetics: defined by start point
  id <- rep(seq_len(nrow(data) - 1), extra)
  aes_df <- data[id, setdiff(names(data), c("x", "y"))]
  
  unrowname(data.frame(x = x, y = y, aes_df))
}

# Interpolate.
# Interpolate n evenly spaced steps from start to end - (end - start) / n.
# 
# @keyword internal
interp <- function(start, end, n) {
  if (n == 1) return(start)
  start + seq(0, 1, length = n) * (end - start)
}

# Euclidean distance between points.
# NA indicates a break / terminal points
# 
# @keyword internal
dist_euclidean <- function(x, y) {
  n <- length(x)

  sqrt((x[-n] - x[-1]) ^ 2 + (y[-n] - y[-1]) ^ 2)
}

# Polar dist.
# Polar distance between points.
# 
# @keyword internal
dist_polar <- function(r, theta) {
  n <- length(r)
  r1 <- r[-n]
  r2 <- r[-1]

  sqrt(r1 ^ 2 + r2 ^ 2 - 2 * r1 * r2 * cos(diff(theta)))
}


# Compute central angle between two points.
# Multiple by radius of sphere to get great circle distance
# @arguments longitude
# @arguments latitude
dist_central_angle <- function(lon, lat) {
  # Convert to radians
  lat <- lat * pi / 180
  lon <- lon * pi / 180
  
  hav <- function(x) sin(x / 2) ^ 2
  ahav <- function(x) 2 * asin(x)
  
  n <- length(lon)
  ahav(sqrt(hav(diff(lat)) + cos(lat[-n]) * cos(lat[-1]) * hav(diff(lat))))
}
