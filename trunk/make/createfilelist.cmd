#!/bin/bash
cd $(dirname $0)
echo "Current directory: $(pwd)"

echo "Create XML file lists..."

cd ../Graffiti_Core/build/classes
find . | grep "\.xml$" > plugins1.txt
cd ../../..

cd Graffiti_Editor/build/classes/ 
find . | grep "\.xml$" > plugins2.txt
cd ../../..

cd Graffiti_Plugins/build/classes 
find . | grep "\.xml$" > plugins3.txt
cd ../../..

cd IPK-Plugins/build/classes
find . | grep "\.xml$" > plugins4.txt
cd ../../..

cd IAP/bin
find . | grep "\.xml$" > pluginsIAP.txt

echo create Cluster Plugin List
echo "./org/graffiti/plugins/views/defaults/plugin.xml" > plugins_cluster.txt
echo "./org/graffiti/plugins/modes/defaults/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/editcomponents/label_alignment/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/editcomponents/cluster_colors/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/expand_no_overlapp/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/modes/defaultEditMode/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/ios/importers/gml/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/ios/exporters/gml/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/inspectors/defaults/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/editcomponents/defaults/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/attributes/defaults/plugin.xml" >> plugins_cluster.txt
echo "./org/graffiti/plugins/attributecomponents/simplelabel/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/svg_exporter/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/invert_selection/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/graph_cleanup/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/tree_simple/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/rt_tree/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/random/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/radial_tree/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/pattern_springembedder/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/grid/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/graph_to_origin_mover/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/expand_reduce_space/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/circle/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/pajek/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/matrix/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/exporters/matrix/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/zoomfit/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/set_background_color/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/info_dialog_cluster_analysis/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/enhanced_attribute_editors/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/editing_tools/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/node_mover/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/ipk_graffitiview/plugin.xml" >> plugins_cluster.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/layout_control/pluginClusterTabs.xml" >> plugins_cluster.txt


echo create Exclude-List for DBE-Gravisto
echo "./org/graffiti/plugins/ios/gml/gmlWriter/plugin.xml" > plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/naive_pattern_finder/plugin.xml" >> plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/copy_pattern_layout/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/random/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/ios/exporters/gmlxml/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/views/defaults/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/ios/gml/gmlReader/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/ios/gml/gmlReader/parser/build.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/info_dialog/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/guis/switchselections/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/random_node_resizer/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/node_mover/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/node_highlighter/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/examples/edge_directer/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/trivialgridrestricted/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/trivialgrid/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/editor/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/springembedderrestricted/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/springembedder/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/pluginsForOnlineUse.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/webstart/jarprefs.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/tools/enhancedzoomtool/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/views/matrix/plugin.xml" >> plugins_exclude.txt
####echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/fast_view/plugin.xml" >> plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/print/printer/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/print/preview/plugin.xml" >> plugins_exclude.txt
echo "./org/jfree/chart/demo/piedata.xml" >> plugins_exclude.txt
echo "./org/jfree/chart/demo/categorydata.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/apsp/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/bfs/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/bfstopsort/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/bn_preparator/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/bonacich_eigenvector/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/pattern_from_canonical_label/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/closeness/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/connectspecial/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/connect/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/degree/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/edge_labeling/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/excentricity/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/fordfulkerson/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/frequent_pattern_finder/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/genophen/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/iterative_partitioning/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/maximum_independent_set/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/centralities/random_walk_betweenness/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/rectangle/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/springembedder_1/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/systematic_motif_generator/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/wclique3/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/tools/zoomtool/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/tools/enhancedzoomtool/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/HighDimEmbed/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/info_dialog_cluster_analysis/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/pattern_springembedder_no_cache/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/layout_control/pluginClusterTabs.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/misc/scripting/plugin.xml"  >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/layouters/fish_eye/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/gui/graph_colorer/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/generators/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/randomizedlabeling/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/algorithms/numbernodes/plugin.xml" >> plugins_exclude.txt
#echo "./org/graffiti/plugins/ios/importers/graphml/plugin.xml" >> plugins_exclude.txt
#echo "./org/graffiti/plugins/ios/exporters/graphviz/plugin.xml" >> plugins_exclude.txt
#echo "./org/graffiti/plugins/ios/exporters/graphml/plugin.xml" >> plugins_exclude.txt
echo "./org/graffiti/plugins/ios/exporters/gmlxml/plugin.xml" >> plugins_exclude.txt
#echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/xgmml/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/matrix/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/genophen/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/importers/flatfile/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/ios/exporters/matrix/plugin.xml" >> plugins_exclude.txt
echo "./de/ipk_gatersleben/ag_nw/graffiti/plugins/algorithms/collapsed_graph_producer/plugin.xml" >> plugins_exclude.txt

cd ../..
cd make
echo "READY"
