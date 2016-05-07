FROM java:latest
MAINTAINER Dr. Christian Klukas <christian.klukas@gmail.com>
LABEL Description="This image is used to start IAP in command line mode (not yet working)." Version="2.0.3"
RUN apt-get update -y && apt-get install -y ant openjfx
RUN git clone --depth=1 https://github.com/OpenImageAnalysisGroup/IAP.git
RUN ant -f IAP/IAP/build.xml cleanall
RUN ant -f IAP/Graffiti_Core/build.xml
RUN ant -f IAP/Graffiti_lib/build.xml
RUN ant -f IAP/Graffiti_Editor/build.xml
RUN ant -f IAP/Graffiti_Plugins/build.xml
RUN ant -f IAP/MultimodalDataHandling/build.xml
RUN ant -f IAP/IPK_lib/build.xml
RUN ant -f IAP/IPK-Plugins/build.xml
RUN ant -f IAP/IAP\ Transfer/build.xml
RUN ant -f IAP/IAP\ CONSOLE/build.xml
RUN ant -f IAP/IAP/build.xml
RUN IAP/make/createfilelist.sh
RUN ant -f IAP/IAP/createReleaseQuick.xml
RUN mv IAP/IAP/release/iap_2_0.jar .
RUN echo '#!/bin/bash' > IAPconsole.sh
RUN echo 'java -cp /root/iap_2_0.jar iap.Console'  >> IAPconsole.sh
RUN chmod +x IAPconsole.sh
RUN echo '#!/bin/bash' > IAPgui.sh
RUN echo 'java -cp /root/iap_2_0.jar de.ipk.ag_ba.gui.webstart.IAPmain'  >> IAPgui.sh
RUN chmod +x IAPgui.sh
