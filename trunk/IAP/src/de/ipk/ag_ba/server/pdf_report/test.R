# TODO: Add comment
# 
# Author: Entzian
###############################################################################


f <- function(x) {
	
		r <- x-g(x)
		r
	}

g <- function(y) {
	r <- y*h(y)
	r
}

h <- function(z) {
	r <- log(z)
	if(r<10)
		r^2
	else 3
}


options(error = quote({
					#sink(file="error.txt", split = TRUE);
					dump.frames();
					print(attr(last.dump,"error.message"));
					#x <- attr(last.dump,"error.message")
					
					traceback();
					#sink(file=NULL);
					#print("############Test##############")
					
					#q()
				}))

f(-1)