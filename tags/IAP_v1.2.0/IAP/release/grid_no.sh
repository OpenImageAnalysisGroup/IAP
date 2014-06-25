#!/bin/bash
echo 
echo "**********************************************************"
echo "*       IAP grid execution with auto-updating client     *"
echo "**********************************************************"
cd $(dirname $0)
echo "Current directory: $(pwd)"
while true
do
	
	if java -Xmx120g -jar iap.jar close
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
