# TODO: Add comment
# 
# Author: Entzian
###############################################################################


testFunction <- function() {
	n <-0
	repeat{
		n <- n+1
		
		if(n==1000)
			break;
	}
}


callFunctionParalle <- function() {
	for(i in seq(1:5)) {
#		sfClusterCall(testFunction())
		sfClusterEval(testFunction())
		cat("Hallo\n")
	}
}

library("snowfall")
sfInit( parallel=TRUE, cpus=6 )

if( sfParallel() ) {
	cat( c("Running in parallel mode on", sfCpus(), "nodes.\n")  )
} else {
	cat( "Running in sequential mode.\n" )
}
sfExportAll()
callFunctionParalle()
#tryCatch(callFunctionParalle(),
#		error = function(e) {
#			sfStop()
#		})
sfStop()


#########################

## Not run:
# Init Snowfall with settings from sfCluster
##sfInit()
# Init Snowfall with explicit settings.
sfInit( parallel=TRUE, cpus=2 )
if( sfParallel() ) {
	cat( "Running in parallel mode on", sfCpus(), "nodes.\n" )
}	else {
	cat( "Running in sequential mode.\n" )
}
# Define some global objects.
globalVar1 <- c( "a", "b", "c" )
globalVar2 <- c( "d", "e" )
globalVar3 <- c( 1:10 )
globalNoExport <- "dummy"
# Define stupid little function.
calculate <- function( x ) {
	cat( x )
	return( 2 ^ x )
}
# Export all global objects except globalNoExport
# List of exported objects is listed.
# Work both parallel and sequential.
sfExportAll( except=c( "globalNoExport" ) )
# List objects on each node.
sfClusterEvalQ( ls() )
# Calc something with parallel sfLappy
cat( unlist( sfLapply( globalVar3, calculate ) ) )
# Remove all variables from object.
sfRemoveAll( except=c( "calculate" ) )
## End(Not run)