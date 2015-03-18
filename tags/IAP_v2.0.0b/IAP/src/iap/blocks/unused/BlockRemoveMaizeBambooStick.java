/**
 * 
 */
package iap.blocks.unused;

import iap.blocks.segmentation.BlRemoveMaizeBambooStick;


/**
 * Clear bamboo stick in visible image. Use lab filter to select the stick pixels (starting from top).
 * If there is more than one structure next to each other in the picture, the processing is stopped.
 * Only single sticks at the very top are cleared until a certain y position.
 * 
 * @author pape
 */
@Deprecated
public class BlockRemoveMaizeBambooStick extends BlRemoveMaizeBambooStick {}
