#!/bin/bash
echo 
echo "**********************************************************"
echo "*       IAP grid execution with auto-updating client     *"
echo "**********************************************************"
cd $(dirname $0)
echo "Current directory: $(pwd)"
while true
do
	echo "Current client will be downloaded from http://ba-13.ipk-gatersleben.de/iap.jar"
	
	if java -cp iap.jar iap.Download http://ba-13.ipk-gatersleben.de/iap.jar iap2.jar 
	then
		echo "Download finished"
	else
		echo "Return value indicates that the current release could not be downloaded. Waiting 5 minutes for next try."
		sleep 300
		continue
	fi
	echo "Move downloaded jar into place ..."
	
	if mv iap2.jar iap.jar
	then
		echo Updated iap.jar
	else
		echo Return value indicates that the current release could not be moved over to target file. Waiting 5 minutes for next try.
		sleep 300
		continue
	fi 
	echo "JAR Release Info:"
	ls iap.jar
	echo "One moment, start grid job execution ..."
	
	if java -Xmx12g -jar iap.jar close
	then
		echo "Grid client closed"
		echo "Return value indicates no more work needs to be done. Waiting 1 minute for restart."
		sleep 60
	else
		echo "Grid client closed"
		echo Return value indicates more work needs to be done. Start processing next job in 10 seconds ...
		sleep 10
	fi
done