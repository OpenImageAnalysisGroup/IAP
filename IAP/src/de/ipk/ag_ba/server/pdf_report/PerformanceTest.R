# TODO: Add comment
# 
# Author: Entzian
###############################################################################

doTimeTestBenchmark <- function(kindOfTest= "timeTest", typ = "vector") {
	
	options(error = quote({
		#sink(file="error.txt", split = TRUE);
		dump.frames();
		print(attr(last.dump,"error.message"));
		#x <- attr(last.dump,"error.message")
		traceback();
		#sink(file=NULL);		
		#q()
	}))
	memory.limit(size=20000)
	#install.packages(c("rbenchmark"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(rbenchmark)
	#install.packages(c("data.table"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(data.table)
	#install.packages(c("plyr"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(plyr)
	#install.packages(c("resharpe"), repos="http://cran.r-project.org", dependencies = TRUE)
	library(reshape)
	
	if(tolower(kindOfTest) == "timetest") {
	
		#typ <- c("vector","leistungstest")
		for(j in typ){
			
			print(paste("Starte", kindOfTest," mit:",j))
			
			if(tolower(j) == "einfach") {
				rowNames <- c(1,1,1,5,5,3,8,8,9,10)
				exampleDataFrame <- data.frame(cbind(rowNames,1:10,5:14))
				colnames(exampleDataFrame) <- c("Days","Deskriptor1", "Deskriptor2")
				workWithDays <- c(1,8,10)
			} else if(tolower(j) == "vector") {
				
				rowNames <- sample(1:10,1000000,replace=TRUE)
				exampleDataFrame <- data.frame(cbind(rowNames,1:100000))
				colnames(exampleDataFrame) <- c("Days","Deskriptor1")
				workWithDays <- c(1,8,10)
				
			} else if(tolower(j) == "leistungstest") {
				rowNames <- sample(1:10,1000000,replace=TRUE)
				exampleDataFrame <- data.frame(cbind(rowNames,1:1000000,5:1000004, 7:1000006, 3:1000002))
				colnames(exampleDataFrame) <- c("Days", "Deskriptor1", "Deskriptor2", "Deskriptor3", "Deskriptor4")
				workWithDays <- c(1,8,10)	
			} else if(tolower(j) == "eachrow") {
				N <- 10000
				rowNames <- sample(1:N,N,replace=FALSE)
				exampleDataFrame <- data.frame(cbind(rowNames,1:N))
				colnames(exampleDataFrame) <- c("Days","Deskriptor1")
				workWithDays <- c(1:N)
				
			}

			ergebnis <-	benchmark("Ansatz1-apply" = timeTest("ansatz1", rowNames, exampleDataFrame, workWithDays),
						"Ansatz2-apply2" = timeTest("ansatz2", rowNames, exampleDataFrame, workWithDays),
						"Ansatz3-tapply" = timeTest("ansatz3", rowNames, exampleDataFrame, workWithDays),
						"Ansatz4-by" = timeTest("ansatz4", rowNames, exampleDataFrame, workWithDays),
						"Ansatz5-aggregate" = timeTest("ansatz5", rowNames, exampleDataFrame, workWithDays),
						"Ansatz6-data.table" = timeTest("ansatz6", rowNames, exampleDataFrame, workWithDays),
						"Ansatz7-plyr" = timeTest("ansatz7", rowNames, exampleDataFrame, workWithDays),
						"Ansatz8-resharpe" = timeTest("ansatz8", rowNames, exampleDataFrame, workWithDays),
						"Ansatz9-for" = timeTest("ansatz9", rowNames, exampleDataFrame, workWithDays),
						replications=5, order=c("replications","elapsed"))
			print(ergebnis)
		}
	} else if(tolower(kindOfTest) == "datatabletest") {
			
		for(j in typ){
		
			print(paste("Starte", kindOfTest," mit:",j))
				
				Anz <- 10000000
				df <- data.frame(Gruppe.1=sample(LETTERS,Anz,replace=TRUE),Gruppe.2=sample(letters,Anz,replace=TRUE),wert=runif(Anz))
								
				ergebnis <-	benchmark("Ansatz1-dataFrame" = testDataTable("ansatz1", df),
						"Ansatz2-dataTable1" = testDataTable("ansatz2",  data.table(df)),
						"Ansatz3-dataTable2" = testDataTable("ansatz3", data.table(df)),
						replications=10, order=c("replications","elapsed"))
				print(ergebnis)
		}
	}
}


timeTest <- function(whichApproach = "ansatz1", rowNames, exampleDataFrame, workWithDays){
	
	if(tolower(whichApproach) == "ansatz1") {
		#apply Variante1
		A <- apply(as.matrix(rowNames),1,"==", workWithDays)
		B <- apply(A,1, "*", exampleDataFrame[,2])
		B[B==0] <- NA
		C <- colMeans(B, na.rm=TRUE)			
		
	} else if(tolower(whichApproach) == "ansatz2") {
		#apply Variante2
		D <- apply(t(apply(as.matrix(rowNames),1,"==", workWithDays)),2,"%*%",as.matrix(exampleDataFrame))
		E <- t(D)/as.vector(table(rowNames)[as.character(workWithDays)])
		
	} else if(tolower(whichApproach) == "ansatz3") {	
		#tapply -> funktioniert nur für einen Vektor
		F <- tapply(exampleDataFrame[,2], rowNames, mean)	
		G <- F[as.character(workWithDays)]
		
	} else if(tolower(whichApproach) == "ansatz4") {
		#by
		H <- by(exampleDataFrame, rowNames, mean)
		I <- H[as.character(workWithDays)]
		
	} else if(tolower(whichApproach) == "ansatz5") {
		#aggregate
		J <- aggregate(exampleDataFrame[,-1], exampleDataFrame[1], mean, simplify=TRUE)
		K <- J[match(workWithDays, J[["Days"]]),colnames(exampleDataFrame)[-1]]
		
	} else if(tolower(whichApproach) == "ansatz6") {
		#data.table
		L <- data.table(exampleDataFrame)
		M <- L[,lapply(.SD, mean), by=list(Days)]
		setkey(M,Days)
		N <- M[J(workWithDays),colnames(exampleDataFrame)[-1],with=FALSE]
		
	}  else if(tolower(whichApproach) == "ansatz7") {
		#plyr
		O <- ddply(exampleDataFrame,"Days",mean,na.rm=TRUE)
		P <- O[match(workWithDays, O[["Days"]]),colnames(exampleDataFrame)[-1]]
		
	} else if(tolower(whichApproach) == "ansatz8") {
		#resharpe
		Q <- melt(exampleDataFrame,id=c("Days"))
		R <- cast(Q, Days~variable, mean)
		S <- R[match(workWithDays, R[["Days"]]),colnames(exampleDataFrame)[-1]]
		
	}  else if(tolower(whichApproach) == "ansatz9") {
		#for Schleife
		T <- data.frame(NULL)
		for(i in workWithDays){
			T <- rbind(T,mean(exampleDataFrame[exampleDataFrame["Days"]==i,])[-1])
		}	
	}
}


testDataTable <- function(whichApproach = "ansatz1", exampleDataFrame) {
		
	if(tolower(whichApproach) == "ansatz1") {
		A <- aggregate(exampleDataFrame$wert,list(exampleDataFrame$Gruppe.1,exampleDataFrame$Gruppe.2),FUN=sum)
		
	} else if(tolower(whichApproach) == "ansatz2") {
		B <- exampleDataFrame[,sum(wert),by="Gruppe.1,Gruppe.2"]
		
	} else if(tolower(whichApproach) == "ansatz3") {
		setkey(exampleDataFrame,Gruppe.1,Gruppe.2)
		C <- exampleDataFrame[,sum(wert),by="Gruppe.1,Gruppe.2"]
		
	} else if(tolower(whichApproach) == "ansatz4") {

	}
}


doTimeTestBenchmark("timeTest", c("vector","leistungstest"))
doTimeTestBenchmark("timeTest", c("eachRow"))
doTimeTestBenchmark("dataTableTest")
