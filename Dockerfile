FROM java:latest
RUN apt-get update -y && apt-get install -y ant openjfx
RUN cd
RUN git clone --depth=1 https://github.com/OpenImageAnalysisGroup/IAP.git
RUN ant -f IAP/IAP/build.xml cleanall
RUN ant -f IAP/IAP/build.xml build
RUN IAP/make/createfilelist.sh
RUN ant -f IAP/IAP/createReleaseQuick.xml
RUN ant -f IAP/IAP/build.xml cleanall
RUN mv IAP/IAP/release/iap_2_0.jar .
RUN echo '#!/bin/bash' > IAPconsole.sh
RUN echo 'java -cp iap_2_0.jar iap.Console'  >> IAPconsole.sh
RUN chmod +x IAPconsole.sh
RUN echo '#!/bin/bash' > IAPgui.sh
RUN echo 'java -cp iap_2_0.jar de.ipk.ag_ba.gui.webstart.IAPmain'  >> IAPgui.sh
RUN chmod +x IAPgui.sh
