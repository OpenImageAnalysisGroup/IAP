package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.tree_jung;

public class TreeLayoutJung {// extends AbstractAlgorithm {
	//
	// /*************************************************************/
	// /* Member variables */
	// /*************************************************************/
	//
	// /**
	// * Dynamical defined edge bend attribute.
	// */
	// private final String BENDS =
	// GraphicAttributeConstants.GRAPHICS
	// + Attribute.SEPARATOR
	// + GraphicAttributeConstants.BENDS;
	// /**
	// * Dynamical defined node coordinate attribute.
	// */
	// private final String COORDSTR =
	// GraphicAttributeConstants.GRAPHICS
	// + Attribute.SEPARATOR
	// + GraphicAttributeConstants.COORDINATE;
	//
	// /**
	// * Dynamical defined node dimension attribute.
	// */
	// private final String DIMENSIONSTR =
	// GraphicAttributeConstants.GRAPHICS
	// + Attribute.SEPARATOR
	// + GraphicAttributeConstants.DIMENSION;
	//
	// /**
	// * Distance of each node from the center
	// */
	// private double nodeDistance = 100;
	//
	// /**
	// * Horizontal distance between trees
	// */
	// private double xDistance = 30;
	//
	// /**
	// * Vertical distance between trees
	// */
	// private double yDistance = 30;
	//
	// /**
	// * The maximum y dimension for each node.
	// */
	// private HashMap maxNodeHeight = new HashMap();
	//
	// /**
	// * Put all trees in a row
	// */
	// private boolean horizontalLayout = true;
	//
	// /**
	// * Move all tree in the right direction either 0, 90, 180 or 270 degree
	// */
	// private int treeDirection = 0;
	//
	// /**
	// * Remove all bends
	// */
	// private boolean doRemoveBends = true;
	//
	// /**
	// * Activate source node red backround
	// */
	// private boolean doMarkSourceNode = true;
	//
	//
	// /**
	// * x coordinate of start point
	// */
	// private double xStart = 100;
	//
	// /**
	// * y coordinate of start point
	// */
	// private double yStart = 100;
	//
	//
	// /**
	// * All trees are initialized by this variable.
	// */
	// private HashMap forest;
	//
	// /**
	// * The roots in the forest.
	// */
	// private HashMap sourceNodes = new HashMap();
	//
	// /**
	// * The root node of the tree.
	// */
	// private Node sourceNode = null;
	//
	// /**
	// * The depth for each node .
	// */
	// private HashMap bfsNum = new HashMap();
	//
	//
	// /**
	// * If there are a circle edges, save them here
	// */
	// private LinkedList tempEdges = new LinkedList();
	//
	// /**
	// * Sum of all children
	// */
	// private HashMap magnitude = new HashMap();
	//
	// /**
	// * x coordinate of start point
	// */
	// private double xStartParam = 100;
	//
	// /**
	// * y coordinate of start point
	// */
	// private double yStartParam = 100;
	//
	//
	// /*************************************************************/
	// /* Declarations of methods */
	// /*************************************************************/
	//
	// /**
	// * Construct a new RadialTreeLayout algorithm instance.
	// */
	// public TreeLayoutJung() {
	// }
	// /**
	// * @see org.graffiti.plugin.algorithm.Algorithm#check()
	// */
	// @Override
	// public void check() throws PreconditionException {
	// if (graph==null)
	// throw new PreconditionException("No graph available!");
	// if (graph.getNumberOfNodes() <= 0) {
	// throw new PreconditionException("The graph is empty. Cannot run tree layouter.");
	// }
	//
	//
	//
	// }
	//
	// /**
	// * Check whether the tree is rooted.
	// */
	// public boolean rootedTree(Node rootNode) {
	//
	// int roots = 0;
	// for (Iterator iterator = bfsNum.keySet().iterator();
	// iterator.hasNext();
	// ) {
	// Node node = (Node) iterator.next();
	// /* maybe there is a second */
	// if ((node.getInDegree() == 0) && (node.getOutDegree() > 0)) {
	// if (roots == 0) {
	// roots++;
	// } else {
	// // return false;
	// }
	// }
	// int ancestors = 0;
	// for (Iterator neighbourEdges = node.getEdgesIterator();
	// neighbourEdges.hasNext();
	// ) {
	//
	// Edge neighbourEdge = (Edge) neighbourEdges.next();
	// Node neighbour = null;
	// if (neighbourEdge.getSource() == node) {
	// neighbour = neighbourEdge.getTarget();
	// } else {
	// neighbour = neighbourEdge.getSource();
	// }
	//
	// /* any links from upper level nodes more than once ? */
	// if (((Integer) bfsNum.get(node)).intValue()
	// > ((Integer) bfsNum.get(neighbour)).intValue()) {
	// ancestors++;
	// if (ancestors > 1) {
	// tempEdges.add(neighbourEdge);
	// graph.deleteEdge(neighbourEdge);
	// }
	// }
	// /* any links from same level nodes ? */
	// if (((Integer) bfsNum.get(node)).intValue()
	// == ((Integer) bfsNum.get(neighbour)).intValue()) {
	//
	// tempEdges.add(neighbourEdge);
	// graph.deleteEdge(neighbourEdge);
	// /* old rooted check routine
	// ancestors++;
	// if (ancestors > 1) {
	// return false;
	// }
	// */
	// }
	// }
	// }
	// return true;
	// }
	//
	// /**
	// * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	// */
	// @Override
	// public Parameter[] getParameters() {
	// Node initNode=null;
	// if (!selection.getNodes().isEmpty())
	// initNode=selection.getNodes().iterator().next();
	// else
	// initNode=graph.getNodes().iterator().next();
	//
	// NodeParameter nodeParam = new NodeParameter(graph, initNode, "Start-Node",
	// "Tree layouter will start only with a selected node.");
	//
	// DoubleParameter distanceParam =
	// new DoubleParameter(
	// "Node Radius",
	// "The distance from the center of each node");
	//
	// DoubleParameter xStartParam =
	// new DoubleParameter(
	// "X base",
	// "The x coordinate of the starting point of the grid horizontal direction.");
	//
	// DoubleParameter yStartParam =
	// new DoubleParameter(
	// "Y base",
	// "The y coordinate of the starting point of the grid horizontal direction.");
	//
	// BooleanParameter horizontalParam =
	// new BooleanParameter(
	// horizontalLayout,
	// "Place Trees in a Row",
	// "Place all trees in a row");
	//
	// BooleanParameter removeBendParam =
	// new BooleanParameter(
	// doRemoveBends,
	// "Remove Bends",
	// "Remove all bends in the forest");
	//
	// BooleanParameter markedSourceNodeParam =
	// new BooleanParameter(
	// doMarkSourceNode,
	// "Mark Start-Node",
	// "Mark each source Node");
	//
	//
	// IntegerParameter treeDirectionParam =
	// new IntegerParameter(
	// treeDirection,
	// "Tree Direction (0,90,180,270)",
	// "Move all trees in 0, 90, 180 or 270 degree");
	//
	// distanceParam.setDouble(nodeDistance);
	// xStartParam.setDouble(this.xStartParam);
	// yStartParam.setDouble(this.yStartParam);
	//
	// return new Parameter[] {
	// nodeParam,
	// distanceParam,
	// xStartParam,
	// yStartParam,
	// horizontalParam,
	// removeBendParam,
	// markedSourceNodeParam,
	// treeDirectionParam };
	// }
	//
	// /**
	// * @see org.graffiti.plugin.algorithm.Algorithm#
	// * setParameters(org.graffiti.plugin.algorithm.Parameter)
	// */
	// @Override
	// public void setParameters(Parameter[] params) {
	// this.parameters = params;
	//
	// Node n = ((NodeParameter) params[0]).getNode();
	// selection.clear();
	// selection.add(n);
	// System.out.println("Node: "+AttributeHelper.getLabel(n, "- unnamed -"));
	//
	// nodeDistance = ((DoubleParameter) params[1]).getDouble().doubleValue();
	// xStart = ((DoubleParameter) params[2]).getDouble().doubleValue();
	// yStart = ((DoubleParameter) params[3]).getDouble().doubleValue();
	// xStartParam = ((DoubleParameter) params[2]).getDouble().doubleValue();
	// yStartParam = ((DoubleParameter) params[3]).getDouble().doubleValue();
	// horizontalLayout =
	// ((BooleanParameter) params[4]).getBoolean().booleanValue();
	// doRemoveBends =
	// ((BooleanParameter) params[5]).getBoolean().booleanValue();
	// doMarkSourceNode =
	// ((BooleanParameter) params[6]).getBoolean().booleanValue();
	// treeDirection =
	// ((IntegerParameter) params[7]).getInteger().intValue();
	// if (!((treeDirection==0) ||(treeDirection==180) ||(treeDirection==270) ||(treeDirection==90))) {
	// treeDirection=0;
	// }
	// }
	//
	// /**
	// * Return a postordered tree
	// *
	// * @param root
	// * @return a postordered tree
	// */
	// private LinkedList postorder(Node root) {
	// LinkedList result = new LinkedList();
	// postorderTraverse(null, root, result);
	// return result;
	// }
	//
	// /**
	// * Return a postordered tree
	// *
	// * @param root
	// * @return a preordered tree
	// */
	// private LinkedList preorder(Node root) {
	// LinkedList result = new LinkedList();
	// preorderTraverse(null, root, result);
	// return result;
	// }
	//
	// /**
	// * Traverse the tree in postorder
	// *
	// * @param ancestor - from node
	// * @param node - start node
	// * @param lq - result is a LinkedList
	// */
	// private void postorderTraverse(Node ancestor, Node node, LinkedList lq) {
	// for (Iterator neighbors = node.getNeighborsIterator();
	// neighbors.hasNext();
	// ) {
	// Node neighbor = (Node) neighbors.next();
	// if (neighbor != ancestor) {
	// postorderTraverse(node, neighbor, lq);
	// }
	// }
	// lq.addLast(node);
	// }
	//
	// /**
	// * Traverse the tree in preorder
	// *
	// * @param ancestor - from node
	// * @param node - start node
	// * @param lq - result is a LinkedList
	// */
	// private void preorderTraverse(Node ancestor, Node node, LinkedList lq) {
	// lq.addLast(node);
	// for (Iterator neighbors = node.getNeighborsIterator();
	// neighbors.hasNext();
	// ) {
	// Node neighbor = (Node) neighbors.next();
	// if (neighbor != ancestor) {
	// preorderTraverse(node, neighbor, lq);
	// }
	// }
	//
	// }
	//
	// /**
	// * Get the successors of the given node
	// *
	// * @param node
	// * @return
	// */
	// private Iterator getSuccessors(Node node) {
	// LinkedList result = new LinkedList();
	//
	// for (Iterator neighbors = node.getNeighborsIterator();
	// neighbors.hasNext();
	// ) {
	// Node neighbor = (Node) neighbors.next();
	// if (((Integer) bfsNum.get(node)).intValue()
	// < ((Integer) bfsNum.get(neighbor)).intValue()) {
	// result.add(neighbor);
	// }
	// }
	// return result.iterator();
	// }
	//
	// /**
	// * Get the predecessors of the given node
	// *
	// * @param node
	// * @return
	// */
	// private Iterator getPredecessors(Node node) {
	// LinkedList result = new LinkedList();
	//
	// for (Iterator neighbors = node.getNeighborsIterator();
	// neighbors.hasNext();
	// ) {
	// Node neighbor = (Node) neighbors.next();
	// if (((Integer) bfsNum.get(node)).intValue()
	// > ((Integer) bfsNum.get(neighbor)).intValue()) {
	// result.add(neighbor);
	// }
	// }
	// return result.iterator();
	// }
	//
	//
	// /**
	// * Init for each node the sum of all children and sub children
	// */
	// protected void initMagnitude() {
	// magnitude = new HashMap();
	//
	// for (Iterator it = postorder(sourceNode).iterator(); it.hasNext();) {
	//
	// Node node = (Node) it.next();
	// int nodeValue = 1;
	// if (magnitude.get(node) != null) {
	// nodeValue = ((Integer) magnitude.get(node)).intValue();
	// } else {
	// magnitude.put(node, new Integer(1));
	// }
	//
	// for (Iterator it2 = getPredecessors(node)/*node.getInNeighborsIterator()*/;
	// it2.hasNext();
	// ) {
	// Node neighbour = (Node) it2.next();
	// int sum = nodeValue;
	// if (magnitude.get(neighbour) != null) {
	// sum += ((Integer) magnitude.get(neighbour)).intValue();
	// }
	//
	// magnitude.put(neighbour, new Integer(sum));
	// }
	//
	// }
	// }
	//
	// /**
	// * @see org.graffiti.plugin.algorithm.Algorithm#execute(Graph)
	// *
	// * The given graph must have at least one node.
	// */
	// public void execute() {
	// GravistoService.getInstance().algorithmAttachData(this);
	//
	// tempEdges = new LinkedList();
	//
	// sourceNodes = new HashMap();
	//
	//
	// forest = new HashMap();
	// for (Iterator iterator = graph.getNodesIterator(); iterator.hasNext();) {
	// forest.put(iterator.next(), null);
	// }
	//
	// /* check all trees with selected nodes, whether they have one root */
	//
	// for (Iterator iterator = selection.getNodes().iterator();
	// iterator.hasNext();
	// ) {
	//
	// sourceNode = (Node) iterator.next();
	//
	// /* ignore multiple selection */
	// if (forest.containsKey(sourceNode)) {
	// /* check circle connection by using the depth of each node */
	// computeDepth(sourceNode);
	// sourceNodes.put(
	// sourceNode,
	// new TreeContainer(
	// bfsNum,
	// maxNodeHeight));
	//
	// if (!rootedTree(sourceNode)) {
	// ErrorMsg.addErrorMessage("The given graph is not a tree.");
	// }
	// }
	// }
	//
	// /* check the trees whether they have one root */
	// while (forest.keySet().iterator().hasNext()) {
	//
	// sourceNode = (Node) forest.keySet().iterator().next();
	//
	// /* check circle connection by using the depth of each node */
	// computeDepth(sourceNode);
	// sourceNodes.put(
	// sourceNode,
	// new TreeContainer(
	// bfsNum,maxNodeHeight));
	//
	// if (!rootedTree(sourceNode)) {
	// for (Iterator it = tempEdges.iterator(); it.hasNext();) {
	// Edge edge = (Edge) it.next();
	// graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
	// }
	// ErrorMsg.addErrorMessage("The given graph has trees with multiple roots.");
	// }
	//
	// /* in case of arrows, try to find the root */
	// Node node = null;
	// for (Iterator iterator = preorder(sourceNode).iterator();
	// iterator.hasNext();
	// ) {
	// node = (Node) iterator.next();
	// if ((node.getInDegree() == 0) && (node.getOutDegree() > 0)) {
	//
	// sourceNodes.remove(sourceNode);
	//
	// sourceNode = node;
	//
	// computeDepth(sourceNode);
	//
	// sourceNodes.put(
	// sourceNode,
	// new TreeContainer(
	// bfsNum,maxNodeHeight));
	//
	// break;
	// }
	// }
	//
	// }
	//
	// graph.getListenerManager().transactionStarted(this);
	//
	// if (doRemoveBends) {
	// removeAllBends();
	// }
	//
	// for (Iterator iterator = sourceNodes.keySet().iterator();
	// iterator.hasNext();
	// ) {
	//
	// sourceNode = (Node) iterator.next();
	//
	// if (doMarkSourceNode)
	// AttributeHelper.setFillColor(sourceNode, Color.RED);
	//
	// bfsNum = ((TreeContainer) sourceNodes.get(sourceNode)).getBfsNum();
	// maxNodeHeight =
	// ((TreeContainer) sourceNodes.get(sourceNode))
	// .getMaxNodeHeight();
	//
	// /* compute segments of the tree */
	// initMagnitude();
	//
	// /* compute positions */
	// computePositions();
	//
	// /* place the tree on its position */
	// if (horizontalLayout) {
	// if ((treeDirection == 180) || (treeDirection == 0)) {
	// xStart += maxNodeHeight.size()*nodeDistance*2+xDistance;
	// } else {
	// yStart += maxNodeHeight.size()*nodeDistance*2+yDistance;
	// }
	// } else {
	// if ((treeDirection == 180) || (treeDirection == 0)) {
	// yStart += maxNodeHeight.size()*nodeDistance*2+yDistance;
	// } else {
	// xStart += maxNodeHeight.size()*nodeDistance*2+xDistance;
	// }
	// }
	//
	// }
	// for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
	// Edge edge = (Edge) iterator.next();
	// graph.addEdgeCopy(edge, edge.getSource(), edge.getTarget());
	// }
	//
	// graph.getListenerManager().transactionFinished(this);
	// }
	//
	// /**
	// * Remove all bends
	// */
	// private void removeAllBends() {
	// for (Iterator iterator = graph.getEdgesIterator(); iterator.hasNext();) {
	// try {
	// ((LinkedHashMapAttribute)((Edge) iterator.next()).getAttribute(BENDS)).setCollection(
	// new HashMap());
	// } catch(AttributeNotFoundException anfe) {};
	// }
	//
	// for (Iterator iterator = tempEdges.iterator(); iterator.hasNext();) {
	// try {
	// (
	// (LinkedHashMapAttribute)
	// ((Edge) iterator.next()).getAttribute(
	// BENDS)).setCollection(
	// new HashMap());
	// } catch(AttributeNotFoundException anfe) {};
	// }
	//
	// }
	//
	// /**
	// * Initialize the level of each node
	// */
	// private void computeDepth(Node startNode) {
	//
	// LinkedList queue = new LinkedList();
	//
	// maxNodeHeight = new HashMap();
	//
	// bfsNum = new HashMap();
	//
	// queue.addLast(startNode);
	// bfsNum.put(startNode, new Integer(0));
	// forest.remove(startNode);
	//
	// /* BreadthFirstSearch algorithm which calculates the depth of the tree */
	// while (!queue.isEmpty()) {
	//
	// Node v = (Node) queue.removeFirst();
	// /* Walk through all neighbours of the last node */
	// for (Iterator neighbours = v.getNeighborsIterator();
	// neighbours.hasNext();
	// ) {
	//
	// Node neighbour = (Node) neighbours.next();
	//
	// /* Not all neighbours, just the neighbours not visited yet*/
	// if (!bfsNum.containsKey(neighbour)) {
	// Integer depth =
	// new Integer(((Integer) bfsNum.get(v)).intValue() + 1);
	//
	// double nodeHeight = getNodeHeight(neighbour);
	//
	// /* Compute the maximum height of nodes in each level of the tree */
	// Double maxNodeHeightValue =
	// (Double) maxNodeHeight.get(depth);
	// if (maxNodeHeightValue != null) {
	// maxNodeHeight.put(
	// depth,
	// new Double(
	// Math.max(
	// maxNodeHeightValue.doubleValue(),
	// nodeHeight)));
	// } else {
	// maxNodeHeight.put(depth, new Double(nodeHeight));
	// }
	//
	// forest.remove(neighbour);
	// bfsNum.put(neighbour, depth);
	// queue.addFirst(neighbour);
	// }
	// }
	// }
	//
	// }
	//
	//
	// protected double polarToCartesianX(double rho, double alpha) {
	// double result =xStart
	// + rho * Math.cos(alpha) * nodeDistance+
	// maxNodeHeight.size()* nodeDistance;
	//
	// if (treeDirection==270) {
	// result =xStart-
	// rho * Math.cos(alpha) * nodeDistance+
	// maxNodeHeight.size()* nodeDistance;
	// }
	//
	// return result;
	// }
	//
	// protected double polarToCartesianY(double rho, double alpha) {
	// double result = yStart
	// + rho * Math.sin(alpha) * nodeDistance+
	// maxNodeHeight.size()* nodeDistance;
	//
	// if (treeDirection==180) {
	// result =yStart-
	// rho * Math.sin(alpha) * nodeDistance+
	// maxNodeHeight.size()* nodeDistance;
	// }
	//
	// return result;
	// }
	//
	// protected double magnitude(Node node) {
	// return ((Integer) magnitude.get(node)).intValue();
	// }
	//
	// private double getNodeHeight(Node n) {
	// DimensionAttribute dimAttr =
	// (DimensionAttribute) n.getAttribute(DIMENSIONSTR);
	//
	// double result = 0.0;
	//
	// if ((treeDirection == 90) || (treeDirection == 270)) {
	// result = dimAttr.getDimension().getWidth();
	// } else {
	// result = dimAttr.getDimension().getHeight();
	// }
	//
	// return result;
	// }
	//
	// private void setX(Node n, double x) {
	// CoordinateAttribute coordAttr =
	// (CoordinateAttribute) n.getAttribute(COORDSTR);
	//
	// if (coordAttr != null) {
	// if ((treeDirection == 90) || (treeDirection == 270)) {
	// coordAttr.setY(x);
	// } else {
	// coordAttr.setX(x);
	// }
	// }
	// }
	//
	// private void setY(Node n, double y) {
	// CoordinateAttribute coordAttr =
	// (CoordinateAttribute) n.getAttribute(COORDSTR);
	//
	// if (coordAttr != null) {
	// if ((treeDirection == 90) || (treeDirection == 270)) {
	// coordAttr.setX(y);
	// } else {
	// coordAttr.setY(y);
	// }
	// }
	// }
	//
	// public String getName() {
	// return "Tree Layout Jung";
	// }
	//
	// @Override
	// public String getCategory() {
	// return "Layout";
	// }
	//
	// @Override
	// public boolean isLayoutAlgorithm() {
	// return true;
	// }
	//
	// protected Dimension size = new Dimension(600,600);
	// protected Forest<V,E> graph;
	// protected Map<V,Integer> basePositions = new HashMap<V,Integer>();
	//
	// protected Map<V, Point2D> locations =
	// LazyMap.decorate(new HashMap<V, Point2D>(),
	// new Transformer<V,Point2D>() {
	// public Point2D transform(V arg0) {
	// return new Point2D.Double();
	// }});
	//
	// protected transient Set<V> alreadyDone = new HashSet<V>();
	//
	// /**
	// * The default horizontal vertex spacing. Initialized to 50.
	// */
	// public static int DEFAULT_DISTX = 50;
	//
	// /**
	// * The default vertical vertex spacing. Initialized to 50.
	// */
	// public static int DEFAULT_DISTY = 50;
	//
	// /**
	// * The horizontal vertex spacing. Defaults to {@code DEFAULT_XDIST}.
	// */
	// protected int distX = 50;
	//
	// /**
	// * The vertical vertex spacing. Defaults to {@code DEFAULT_YDIST}.
	// */
	// protected int distY = 50;
	//
	// protected transient Point m_currentPoint = new Point();
	//
	// /**
	// * Creates an instance for the specified graph with default X and Y distances.
	// */
	// public TreeLayout(Forest<V,E> g) {
	// this(g, DEFAULT_DISTX, DEFAULT_DISTY);
	// }
	//
	// /**
	// * Creates an instance for the specified graph and X distance with
	// * default Y distance.
	// */
	// public TreeLayout(Forest<V,E> g, int distx) {
	// this(g, distx, DEFAULT_DISTY);
	// }
	//
	// /**
	// * Creates an instance for the specified graph, X distance, and Y distance.
	// */
	// public TreeLayout(Forest<V,E> g, int distx, int disty) {
	// if (g == null)
	// throw new IllegalArgumentException("Graph must be non-null");
	// if (distx < 1 || disty < 1)
	// throw new IllegalArgumentException("X and Y distances must each be positive");
	// this.graph = g;
	// this.distX = distx;
	// this.distY = disty;
	// buildTree();
	// }
	//
	// protected void buildTree() {
	// this.m_currentPoint = new Point(0, 20);
	// Collection<V> roots = TreeUtils.getRoots(graph);
	// if (roots.size() > 0 && graph != null) {
	// calculateDimensionX(roots);
	// for(V v : roots) {
	// calculateDimensionX(v);
	// m_currentPoint.x += this.basePositions.get(v)/2 + this.distX;
	// buildTree(v, this.m_currentPoint.x);
	// }
	// }
	// int width = 0;
	// for(V v : roots) {
	// width += basePositions.get(v);
	// }
	// }
	//
	// protected void buildTree(V v, int x) {
	//
	// if (!alreadyDone.contains(v)) {
	// alreadyDone.add(v);
	//
	// //go one level further down
	// this.m_currentPoint.y += this.distY;
	// this.m_currentPoint.x = x;
	//
	// this.setCurrentPositionFor(v);
	//
	// int sizeXofCurrent = basePositions.get(v);
	//
	// int lastX = x - sizeXofCurrent / 2;
	//
	// int sizeXofChild;
	// int startXofChild;
	//
	// for (V element : graph.getSuccessors(v)) {
	// sizeXofChild = this.basePositions.get(element);
	// startXofChild = lastX + sizeXofChild / 2;
	// buildTree(element, startXofChild);
	// lastX = lastX + sizeXofChild + distX;
	// }
	// this.m_currentPoint.y -= this.distY;
	// }
	// }
	//
	// private int calculateDimensionX(V v) {
	//
	// int size = 0;
	// int childrenNum = graph.getSuccessors(v).size();
	//
	// if (childrenNum != 0) {
	// for (V element : graph.getSuccessors(v)) {
	// size += calculateDimensionX(element) + distX;
	// }
	// }
	// size = Math.max(0, size - distX);
	// basePositions.put(v, size);
	//
	// return size;
	// }
	//
	// private int calculateDimensionX(Collection<V> roots) {
	//
	// int size = 0;
	// for(V v : roots) {
	// int childrenNum = graph.getSuccessors(v).size();
	//
	// if (childrenNum != 0) {
	// for (V element : graph.getSuccessors(v)) {
	// size += calculateDimensionX(element) + distX;
	// }
	// }
	// size = Math.max(0, size - distX);
	// basePositions.put(v, size);
	// }
	//
	// return size;
	// }
	//
	// /**
	// * This method is not supported by this class. The size of the layout
	// * is determined by the topology of the tree, and by the horizontal
	// * and vertical spacing (optionally set by the constructor).
	// */
	// public void setSize(Dimension size) {
	// throw new UnsupportedOperationException("Size of TreeLayout is set" +
	// " by vertex spacing in constructor");
	// }
	//
	// protected void setCurrentPositionFor(V vertex) {
	// int x = m_currentPoint.x;
	// int y = m_currentPoint.y;
	// if(x < 0) size.width -= x;
	//
	// if(x > size.width-distX)
	// size.width = x + distX;
	//
	// if(y < 0) size.height -= y;
	// if(y > size.height-distY)
	// size.height = y + distY;
	// locations.get(vertex).setLocation(m_currentPoint);
	//
	// }
	//
	// public Graph<V,E> getGraph() {
	// return graph;
	// }
	//
	// public Dimension getSize() {
	// return size;
	// }
	//
	// public void initialize() {
	//
	// }
	//
	// public boolean isLocked(V v) {
	// return false;
	// }
	//
	// public void lock(V v, boolean state) {
	// }
	//
	// public void reset() {
	// }
	//
	// public void setGraph(Graph<V,E> graph) {
	// if(graph instanceof Forest) {
	// this.graph = (Forest<V,E>)graph;
	// buildTree();
	// } else {
	// throw new IllegalArgumentException("graph must be a Forest");
	// }
	// }
	//
	// public void setInitializer(Transformer<V, Point2D> initializer) {
	// }
	//
	// /**
	// * Returns the center of this layout's area.
	// */
	// public Point2D getCenter() {
	// return new Point2D.Double(size.getWidth()/2,size.getHeight()/2);
	// }
	//
	// public void setLocation(V v, Point2D location) {
	// locations.get(v).setLocation(location);
	// }
	//
	// public Point2D transform(V v) {
	// return locations.get(v);
	// }
	
}
