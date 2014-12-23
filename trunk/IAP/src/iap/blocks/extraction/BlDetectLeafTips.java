package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.blocks.image_analysis_tools.leafClustering.BorderAnalysis;
import iap.blocks.image_analysis_tools.leafClustering.Feature;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject;
import iap.blocks.image_analysis_tools.leafClustering.FeatureObject.FeatureObjectType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures;
import de.ipk.ag_ba.image.operation.GLCMTextureFeatures;
import de.ipk.ag_ba.image.operation.ImageConvolution;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.ImageTexture;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author pape, klukas
 */
public class BlDetectLeafTips extends AbstractBlock implements CalculatesProperties {
	
	boolean ignore = false;
	boolean debug_borderDetection;
	double borderSize;
	private boolean useAdaptiveRadiusEstimation;
	boolean calcWidthNearTip;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug_borderDetection = getBoolean("Debug Border Detection", false);
		useAdaptiveRadiusEstimation = getBoolean("Use Addaptive Radius", false);
		calcWidthNearTip = getBoolean("Calculate Width near Leaf-Tip", true);
	}
	
	@Override
	public boolean isChangingImages() {
		return true; // post-processor highlights leaf-tips
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null)
			return null;
		
		int searchRadius;
		if (useAdaptiveRadiusEstimation)
			searchRadius = (int) (Math.sqrt(mask.io().countFilledPixels()) * 0.25);
		else
			searchRadius = getInt("Search-radius (" + mask.getCameraType() + ")", 35);
		
		boolean isBestAngle = isBestAngle(mask.getCameraType());
		CameraType ct = mask.getCameraType();
		
		// search for best side image
		if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
			if (!isBestAngle)
				ignore = true;
		}
		
		if (getBoolean("Calculate on " + ct.getNiceName() + " Image", ct == CameraType.VIS) && !ignore) {
			Image workimg = mask.copy();
			
			double fillGradeInPercent = getDouble("Fillgrade (" + ct + ")", 0.3);
			int blurSize = getInt("Size for Bluring (" + ct + ")", 0);
			int erodeSize = getInt("Masksize Erode (" + ct + ")", 2);
			int dilateSize = getInt("Masksize Dilate (" + ct + ")", 5);
			int minHeightPercent = getInt("Minimum Leaf Height Percent", -1);
			
			borderSize = searchRadius / 2;
			workimg.setCameraType(mask.getCameraType());
			workimg = preprocessImage(workimg, searchRadius, blurSize, erodeSize, dilateSize);
			Roi bb = workimg.io().getBoundingBox();
			int maxValidY = (int) (bb.getBounds().y + bb.getBounds().height - minHeightPercent / 100d * bb.getBounds().height);
			workimg.setCameraType(mask.getCameraType());
			ImageData info = (ct == CameraType.VIS) ? input().images().getVisInfo() :
					(ct == CameraType.FLUO) ? input().images().getFluoInfo() :
							(ct == CameraType.NIR) ? input().images().getNirInfo() :
									(ct == CameraType.IR) ? null : null;
			savePeaksAndFeatures(getPeaksFromBorder(workimg, mask, searchRadius, fillGradeInPercent), ct,
					optionsAndResults.getCameraPosition(),
					searchRadius, maxValidY, info, input().masks().vis());
		}
		return mask;
	}
	
	private void savePeaksAndFeatures(LinkedList<Feature> peakList, CameraType cameraType, CameraPosition cameraPosition,
			int searchRadius, int maxValidY, ImageData imageRef, Image orig_image) {
		boolean saveListObject = true;
		boolean saveLeafCount = true;
		boolean saveAdditionalFeatures = true;
		boolean saveFeaturesInResultSet = getBoolean("Save individual leaf features", false);
		boolean saveColorFeaturesInResultSet = getBoolean("Save individual leaf color features", false);
		boolean saveTextureFeaturesInResultSet = getBoolean("Save leaf texture features (Mean)", true);
		boolean saveIndividualTextureFeaturesInResultSet = getBoolean("Save individual leaf texture features", false);
		boolean removeOutliers = getBoolean("Outlier removal based on leaf tip angle", true);
		
		double alpha = getDouble("Grubbs alpha level", 0.05);
		double circleArea = searchRadius * searchRadius * Math.PI;
		
		LinkedList<Feature> lowerOutlier = new LinkedList<Feature>();
		LinkedList<Feature> upperOutlier = new LinkedList<Feature>();
		
		if (removeOutliers) {
			int removed = Outlier.doGrubbsTest((LinkedList) peakList, alpha, (LinkedList) lowerOutlier, (LinkedList) upperOutlier);
		}
		
		if (saveListObject) {
			saveAndCorrectBorderLeafTipList(peakList, lowerOutlier, upperOutlier, cameraType, maxValidY);
		}
		
		if (saveLeafCount) {
			saveLeafCount(cameraType, cameraPosition, peakList.size(), lowerOutlier.size() + upperOutlier.size(), removeOutliers, imageRef);
		}
		
		if (saveAdditionalFeatures) {
			int n = 0, nup = 0, ndown = 0;
			DescriptiveStatistics statsArea = new DescriptiveStatistics();
			DescriptiveStatistics statsLeafDirection = new DescriptiveStatistics();
			HashMap<String, Double> firstOrderMeans = new HashMap<String, Double>();
			HashMap<String, Double> glcmMeans = new HashMap<String, Double>();
			
			DescriptiveStatistics statsLeafWidthNearTip = new DescriptiveStatistics();
			for (Feature bf : peakList) {
				
				final Double angle = (Double) bf.getFeature("angle");
				Vector2D direction = (Vector2D) bf.getFeature("direction");
				
				if (calcWidthNearTip) {
					Double widthNearLT = (Double) bf.getFeature("widthNearTip");
					if (widthNearLT > 0) {
						statsLeafWidthNearTip.addValue(widthNearLT);
						getResultSet().setNumericResult(
								getBlockPosition(),
								new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_GEOMETRY, "leaftip."
										+ StringManipulationTools.formatNumberAddZeroInFront(n, 2) + ".width_near_tip"), widthNearLT, "px", this,
								imageRef);
					}
				}
				
				if (statsLeafWidthNearTip.getN() > 0) {
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.width_near_tip.mean"), statsLeafWidthNearTip.getMean(), "px", this,
							imageRef);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.width_near_tip.stdev"),
							statsLeafWidthNearTip.getStandardDeviation(),
							null, this, imageRef);
					getResultSet()
							.setNumericResult(getBlockPosition(),
									new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.width_near_tip.skewness"),
									statsLeafWidthNearTip.getSkewness(),
									null, this,
									imageRef);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.width_near_tip.kurtosis"), statsLeafWidthNearTip.getKurtosis(),
							null,
							this,
							imageRef);
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.width_near_tip.n"), statsLeafWidthNearTip.getN(), "leaftips", this,
							imageRef);
				}
				
				if (angle != null) {
					ArrayList<PositionAndColor> temp_area = (ArrayList<PositionAndColor>) bf.getFeature("pixels");
					statsArea.addValue(temp_area.size());
					if (saveColorFeaturesInResultSet) {
						ArrayList<PositionAndColor> pixels = temp_area;
						
						if (pixels != null && pixels.size() > 0) {
							Image leafTipImage, leafTipImage2d = null;
							if (saveTextureFeaturesInResultSet) {
								int[][] regionArray2d = BorderAnalysis.copyRegiontoArray(BorderAnalysis.findDimensions(pixels), pixels);
								leafTipImage2d = new Image(regionArray2d).show("region array from orig image", false);
							}
							
							int[] regionArray = BorderAnalysis.copyRegiontoArray(pixels);
							leafTipImage = new Image(regionArray.length, 1, regionArray);
							
							for (Channel c : Channel.values()) {
								ImageOperation img = leafTipImage.io().channels().get(c);
								double stats = img.getImageAsImagePlus().getStatistics().mean;
								
								getResultSet().setNumericResult(
										getBlockPosition(),
										new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_INTENSITY, "leaftip."
												+ StringManipulationTools.formatNumberAddZeroInFront(n, 2) + "." + c + ".mean"), stats, null, this,
										imageRef);
								
								if (saveTextureFeaturesInResultSet) {
									ImageOperation img2d = leafTipImage2d.io().channels().get(c);
									boolean right = direction.getX() > 0;
									if (right)
										img2d = img2d.rotate(-angle);
									else
										img2d = img2d.rotate(angle);
									
									ImageTexture it = new ImageTexture(img2d.getImage());
									it.calcTextureFeatures();
									it.calcGLCMTextureFeatures();
									
									for (FirstOrderTextureFeatures tf : FirstOrderTextureFeatures.values()) {
										if (saveIndividualTextureFeaturesInResultSet)
											getResultSet().setNumericResult(
													getBlockPosition(),
													new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_TEXTURE, "leaftip."
															+ StringManipulationTools.formatNumberAddZeroInFront(n, 2) + "." + c + ".texture." + tf),
													it.firstOrderFeatures.get(tf), null, this, imageRef);
										
										if (firstOrderMeans.get("" + tf.toString() + ";" + c.toString()) != null)
											firstOrderMeans.put("" + tf.toString() + ";" + c.toString(),
													firstOrderMeans.get("" + tf.toString() + ";" + c.toString()) + it.firstOrderFeatures.get(tf));
										else
											firstOrderMeans.put("" + tf.toString() + ";" + c.toString(), it.firstOrderFeatures.get(tf));
									}
									
									for (GLCMTextureFeatures tf : GLCMTextureFeatures.values()) {
										if (saveIndividualTextureFeaturesInResultSet)
											getResultSet().setNumericResult(
													getBlockPosition(),
													new Trait(cameraPosition, cameraType, TraitCategory.ORGAN_TEXTURE, "leaftip."
															+ StringManipulationTools.formatNumberAddZeroInFront(n, 2) + "." + c + ".texture." + tf),
													it.glcmFeatures.get(tf), null, this, imageRef);
										
										if (glcmMeans.get("" + tf.toString() + ";" + c.toString()) != null)
											glcmMeans.put("" + tf.toString() + ";" + c.toString(), glcmMeans.get("" + tf.toString() + ";" + c.toString())
													+ it.glcmFeatures.get(tf));
										else
											glcmMeans.put("" + tf.toString() + ";" + c.toString(), it.glcmFeatures.get(tf));
									}
								}
							}
						}
					}
					
					if (angle.doubleValue() > 90.0)
						nup++;
					else
						ndown++;
					n++;
					statsLeafDirection.addValue(angle.doubleValue());
				}
			}
			
			if (n > 0) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.corner.angle.mean"), 360 * statsArea.getMean() / circleArea, "degree",
						this, imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.corner.angle.stdev"), statsArea.getStandardDeviation(), null, this,
						imageRef);
				getResultSet()
						.setNumericResult(getBlockPosition(),
								new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.corner.angle.skewness"), statsArea.getSkewness(), null, this,
								imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.corner.angle.mean.kurtosis"), statsArea.getKurtosis(), null, this,
						imageRef);
			}
			
			if (n > 0 && cameraPosition == CameraPosition.SIDE) {
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.up.count"), nup, "leaftips", this, imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.down.count"), ndown, "leaftips", this, imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.mean"), statsLeafDirection.getMean(), "degree", this, imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.stdev"), statsLeafDirection.getStandardDeviation(), null,
						this, imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.skewness"), statsLeafDirection.getSkewness(), null, this,
						imageRef);
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.angle.kurtosis"), statsLeafDirection.getKurtosis(), null, this,
						imageRef);
				
				// save texture avg
				for (String name : firstOrderMeans.keySet()) {
					double val = firstOrderMeans.get(name) / n;
					String tf = name.split(";")[0];
					String c = name.split(";")[1];
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip." + c + ".texture." + tf + ".mean"), val, null, this,
							imageRef);
				}
				
				for (String name : glcmMeans.keySet()) {
					double val = glcmMeans.get(name) / n;
					String tf = name.split(";")[0];
					String c = name.split(";")[1];
					getResultSet().setNumericResult(getBlockPosition(),
							new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip." + c + ".texture." + tf + ".mean"), val, null, this,
							imageRef);
				}
			}
		}
		
		boolean markResults = true;
		if (saveFeaturesInResultSet || markResults) {
			int index = 1;
			for (Feature bf : peakList) {
				Vector2D pos = bf.getPosition();
				final Double angle = (Double) bf.getFeature("angle");
				Vector2D direction = (Vector2D) bf.getFeature("direction");
				ArrayList<PositionAndColor> tip_area = (ArrayList<PositionAndColor>) bf.getFeature("pixels");
				int size = tip_area.size();
				Double diff = (Double) bf.getFeature("widthNearTip");
				ArrayList<PositionAndColor> near_tip_area = (ArrayList<PositionAndColor>) bf.getFeature("nearTipPixels");
				
				final CameraType cameraType_fin = cameraType;
				
				if (pos == null || cameraPosition == null || cameraType == null) {
					continue;
				}
				
				// correct positions
				Vector2D sub = new Vector2D(-borderSize, -borderSize);
				final Vector2D pos_fin = pos;
				final Vector2D direction_fin = direction.add(sub);
				
				if (saveFeaturesInResultSet) {
					getResultSet().setNumericResult(
							0,
							new Trait(cameraPosition, cameraType_fin, TraitCategory.GEOMETRY, "leaftip."
									+ StringManipulationTools.formatNumberAddZeroInFront(index, 2)
									+ ".x"), pos_fin.getX(), "px", this, imageRef);
					getResultSet().setNumericResult(
							0,
							new Trait(cameraPosition, cameraType_fin, TraitCategory.GEOMETRY, "leaftip."
									+ StringManipulationTools.formatNumberAddZeroInFront(index, 2)
									+ ".y"),
							pos_fin.getY(), "px", this, imageRef);
					
					if (angle != null && cameraPosition == CameraPosition.SIDE)
						getResultSet()
								.setNumericResult(
										0,
										new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip."
												+ StringManipulationTools.formatNumberAddZeroInFront(index, 2)
												+ ".angle"),
										angle, "degree", this, imageRef);
				}
				index++;
				
				if (searchRadius > 0) {
					final int searchRadius_fin = searchRadius;
					final double cornerAngle = 360 * size / circleArea;
					getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
						
						@Override
						public Image postProcessMask(Image mask) {
							int xPos = (int) pos_fin.getX();
							int yPos = (int) pos.getY();
							Vector2D vv = direction.subtract(new Vector2D(xPos, yPos));
							Vector2D d = vv.getNorm() > 0.01 ?
									vv.normalize()
											.scalarMultiply((1 + (Math.sqrt(2) - 1) * (1 - Math.abs(Math.cos(2 * angle / 180. * Math.PI)))) * searchRadius_fin)
									: vv;
							ImageCanvas t = mask
									.io()
									.canvas();
							if (near_tip_area != null && near_tip_area.size() > 0) {
								for (PositionAndColor pac : near_tip_area)
									t = t.drawLine(pac.x, pac.y, pac.x + 1, pac.y, Color.ORANGE.getRGB(), 0.9d, 1);
							}
							if (tip_area != null && tip_area.size() > 0) {
								for (PositionAndColor pac : tip_area)
									t = t.drawLine(pac.x, pac.y, pac.x + 1, pac.y, Color.CYAN.getRGB(), 0.9d, 1);
							}
							t = t.drawCircle((int) pos_fin.getX(), (int) pos_fin.getY(), searchRadius_fin, Color.RED.getRGB(), 0.5, 3)
									.drawLine(xPos, yPos, (int) d.getX() + xPos, (int) d.getY() + yPos, Color.BLUE.getRGB(), 0.8, 1)
									.text((int) direction_fin.getX() + 10, (int) direction_fin.getY(),
											"x: " + ((int) pos_fin.getX() + borderSize) + " y: " + ((int) pos_fin.getY() + borderSize),
											Color.BLACK);
							// if (isSide) {
							String s = "A: " + angle.intValue() + " | CA: " + StringManipulationTools.formatNumber(cornerAngle, 2);
							if (calcWidthNearTip)
								s += " | Diff: " + (diff != null ? StringManipulationTools.formatNumber(diff, 1) : "n/a");
							return t.text((int) direction_fin.getX() + 10, (int) direction_fin.getY() + 15, s, Color.BLACK)
									.getImage();
							// }
							// else
							// return t.getImage();
						}
						
						@Override
						public Image postProcessImage(Image image) {
							return image;
						}
						
						@Override
						public CameraType getConfig() {
							return cameraType_fin;
						}
					});
				}
			}
			
			// outlier marking
			{
				LinkedList<Feature> outlierList = new LinkedList<>();
				outlierList.addAll(lowerOutlier);
				outlierList.addAll(upperOutlier);
				
				for (Feature bf : outlierList) {
					Vector2D pos = bf.getPosition();
					final Double angle = (Double) bf.getFeature("angle");
					Vector2D direction = (Vector2D) bf.getFeature("direction");
					final CameraType cameraType_fin = cameraType;
					
					if (pos == null || cameraPosition == null || cameraType == null) {
						continue;
					}
					
					// correct positions
					final Vector2D pos_fin = pos;
					
					if (searchRadius > 0) {
						final int searchRadius_fin = searchRadius;
						
						getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
							
							@Override
							public Image postProcessMask(Image mask) {
								int xPos = (int) pos_fin.getX();
								int yPos = (int) pos.getY();
								Vector2D vv = direction.subtract(new Vector2D(xPos, yPos));
								Vector2D d = vv.getNorm() > 0.01 ?
										vv.normalize()
												.scalarMultiply((1 + (Math.sqrt(2) - 1) * (1 - Math.abs(Math.cos(2 * angle / 180. * Math.PI)))) * searchRadius_fin)
										: vv;
								ImageCanvas t = mask
										.io()
										.canvas()
										.drawCircle((int) pos_fin.getX(), (int) pos_fin.getY(), searchRadius_fin, Color.GRAY.getRGB(), 0.3, 1)
										.drawLine(xPos, yPos, (int) d.getX() + xPos, (int) d.getY() + yPos, Color.GRAY.getRGB(), 0.3, 1);
								return t.getImage();
							}
							
							@Override
							public Image postProcessImage(Image image) {
								return image;
							}
							
							@Override
							public CameraType getConfig() {
								return cameraType_fin;
							}
						});
					}
				}
			}
		}
	}
	
	private void saveLeafCount(CameraType cameraType, CameraPosition cameraPosition, int count, int outlierCount, boolean rmOutlier, ImageData imageRef) {
		// save leaf count
		getResultSet().setNumericResult(getBlockPosition(),
				new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.count"), count, "leaftips", this, imageRef);
		
		if (rmOutlier)
			getResultSet().setNumericResult(getBlockPosition(),
					new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.outlier.count"), outlierCount, "leaftips", this, imageRef);
		
		if (cameraPosition == CameraPosition.SIDE) {
			boolean isBestAngle = isBestAngle(cameraType);
			// search for best side image
			if (getBoolean("Only calculate for Best Angle (fits to Main Axis)", true)) {
				if (!isBestAngle)
					ignore = true;
			}
			// save leaf count for best angle
			if (isBestAngle)
				getResultSet().setNumericResult(getBlockPosition(),
						new Trait(cameraPosition, cameraType, TraitCategory.GEOMETRY, "leaftip.count.best_angle"), count, "leaftips", this, imageRef);
		}
	}
	
	private void saveAndCorrectBorderLeafTipList(LinkedList<Feature> pk, LinkedList<Feature> pl, LinkedList<Feature> pu, CameraType cameraType, int maxValidY) {
		// remove bordersize from all position-features
		for (LinkedList<Feature> peakList : new LinkedList[] { pk, pl, pu }) {
			ArrayList<Feature> toRemove = new ArrayList<Feature>();
			for (Feature bf : peakList) {
				HashMap<String, FeatureObject> fm = bf.getFeatureMap();
				if (bf.getPosition().getY() > maxValidY)
					toRemove.add(bf);
				for (FeatureObject fo : fm.values()) {
					if (fo.featureObjectType == FeatureObjectType.POSITION) {
						fo.feature = (int) ((Integer) (fo.feature) - borderSize);
					}
					if (fo.featureObjectType == FeatureObjectType.VECTOR) {
						fo.feature = ((Vector2D) fo.feature).add(new Vector2D(-borderSize, -borderSize));
					}
				}
			}
			
			peakList.removeAll(toRemove);
		}
		getResultSet().setObjectResult(getBlockPosition(), "leaftiplist_" + cameraType, pk);
	}
	
	private LinkedList<Feature> getPeaksFromBorder(Image img, Image orig, int searchRadius, double fillGradeInPercent) {
		BorderAnalysis ba = null;
		LinkedList<Feature> res = null;
		
		ba = new BorderAnalysis(img, orig);
		int geometricThresh = (int) (fillGradeInPercent * (Math.PI * searchRadius * searchRadius));
		ba.setCheckSplit(true);
		ba.calcSUSAN(searchRadius, geometricThresh);
		ba.getPeaksFromBorder(1, (searchRadius), "susan");
		ba.approxDirection(searchRadius * 2);
		
		if (debug_borderDetection)
			ba.plot(0, searchRadius);
		
		res = ba.getPeakList();
		
		if (calcWidthNearTip)
			calcWidthNearTip(res, img, searchRadius, getDouble("Factor for Width Estimation", 1.5));
		
		return res;
	}
	
	private void calcWidthNearTip(LinkedList<Feature> res, Image img, int searchRadius, double fac) {
		// ImageCanvas ic = new ImageCanvas(img);
		int[][] img2d = img.getAs2A();
		int newRadius = (int) (searchRadius * fac);
		for (Feature lt : res) {
			Vector2D pos = lt.getPosition();
			// ic.drawCircle((int) pos.getX(), (int) pos.getY(), searchRadius, Color.CYAN.getRGB(), 0.5, 2);
			
			ArrayList<PositionAndColor> areaBig = BorderAnalysis.regionGrowing((int) pos.getX(), (int) pos.getY(), img2d.clone(),
					ImageOperation.BACKGROUND_COLORint,
					newRadius, Integer.MAX_VALUE,
					false, false);
			ArrayList<PositionAndColor> areaSmall = (ArrayList<PositionAndColor>) lt.getFeature("pixels");
			
			int diff = areaBig.size() - areaSmall.size();
			// System.out.println("diff: " + diff);
			if (searchRadius - newRadius != 0) {
				double w = diff / Math.abs(searchRadius - newRadius);
				lt.addFeature("widthNearTip", w, FeatureObjectType.NUMERIC);
				lt.addFeature("nearTipPixels", areaBig, FeatureObjectType.OBJECT);
			} else
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Internal Error: SearchRadius - NewRadius == 0!");
		}
		
	}
	
	private Image preprocessImage(Image img, int searchRadius, int blurSize, int erode, int dilate) {
		int background = ImageOperation.BACKGROUND_COLORint;
		CameraType ct = img.getCameraType();
		
		// get skeleton-image and workimage to connect lose leaves and for optimization
		Image skel = getResultSet().getImage("skeleton_" + ct.toString(), false).getImage();
		Image skel_workimge = getResultSet().getImage("skeleton_workimage_" + ct.toString()).getImage();
		if (skel != null && skel_workimge != null) {
			img = img.io().or(skel.copy().io().bm().dilate(15).getImage()).or(skel_workimge).getImage()
					.show("skel images on mask" + ct.toString(), debugValues);
		} else {
			// if (ct != CameraType.NIR)
			// System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: No " + ct.toString()
			// + " skeleton image available, can't process it within leaf-tip detection!");
		}
		
		// morphological operations
		img = img.io().bm().dilate(dilate).erode(erode).getImage().show("Erode and Dilate on " + ct.toString(), debugValues);
		
		// blur
		img = img.io().blurImageJ(blurSize).getImage().show("Blured Image " + ct.toString(), debugValues);
		
		// enlarge 1 px lines
		ImageConvolution ic = new ImageConvolution(img);
		img = ic.enlargeLines().getImage().show("Enlarged Lines " + ct.toString(), debugValues);
		
		// add border around image
		img = img.io().addBorder((int) borderSize, 0, 0, background).getImage();
		
		return img;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Detect Leaf-Tips";
	}
	
	@Override
	public String getDescription() {
		return "Detect leaf-tips of a plant. (e.g. could be used for calculation of leaf number)";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		ArrayList<CalculatedPropertyDescription> desList = new ArrayList<CalculatedPropertyDescription>();
		
		desList.add(new CalculatedProperty("leaftip.*.x", "X-coordinate of a certain leaf-tip."));
		desList.add(new CalculatedProperty("leaftip.*.y", "Y-coordinate of a certain leaf-tip."));
		desList.add(new CalculatedProperty("leaftip.*.angle", "Leaf-tip angle of a certain leaf."));
		desList.add(new CalculatedProperty("leaftip.count", "Number of leaves."));
		desList.add(new CalculatedProperty("leaftip.count.best_angle", "Number of leaves for the 'best' side view angle "
				+ "(as determined from the main growth orientation observed from top-view."));
		desList.add(new CalculatedProperty("leaftip.rgb.red.mean",
				"Average intensity of the red channel of the leaves tips pixels in the visible light or fluorescence image."));
		desList.add(new CalculatedProperty("leaftip.rgb.green.mean",
				"Average intensity of the green channel of the leaves tips pixels in the visible light or fluorescence image."));
		desList.add(new CalculatedProperty("leaftip.rgb.blue.mean",
				"Average intensity of the blue channel of the leaves tips pixels in the visible light or fluorescence image."));
		desList
				.add(new CalculatedProperty(
						"leaftip.hsv.h.mean",
						"The average hue of the leaves tips pixels in the HSV/HSB colour space. For this property the value range is normalized to a minimum of 0 and a maximum of 255."));
		desList
				.add(new CalculatedProperty(
						"leaftip.hsv.s.mean",
						"The saturation of the leaves tips pixels in the HSV/HSB colour space. For this property the value range is normalized to a minimum of 0 and a maximum of 255."));
		desList
				.add(new CalculatedProperty(
						"leaftip.hsv.v.mean",
						"The leaves tips average brightness in the HSV/HSB colour space. For this property the value range is normalized to a minimum of 0 and a maximum of 255."));
		desList.add(new CalculatedProperty("leaftip.color.lab.l.mean",
				"The leaves tips average brightness value in the L*a*b* colour space. Small values "
						+ "indicate low and high values high brightness. This value ranges from 0 to 255."));
		desList.add(new CalculatedProperty("leaftip.lab.a.mean",
				"The leaves tips average a-value in the L*a*b* colour space. Small values indicate green "
						+ "while high values indicate magenta. This value ranges from 26 to 225, other software or references may "
						+ "utilize different ranges, e.g. higher negative together with higher positive values."));
		desList.add(new CalculatedProperty("leaftip.lab.b.mean",
				"The leaves tips average b-value in the L*a*b* colour space. Small values indicate blue and "
						+ "high values indicate yellow. This value ranges from 8 to 223, other software or references may utilize "
						+ "different ranges, e.g. higher negative values together with higher positive values."));
		desList.add(new CalculatedProperty("leaftip.up.count", "The number of leaf tips pointing upwards (greater 90 degree)."));
		desList.add(new CalculatedProperty("leaftip.down.count", "The number of leaf tips pointing downwards (less than 90 degree)."));
		desList.add(new CalculatedProperty("leaftip.corner.angle.mean", "The average leaf tip corner angle. (sharpness of leaf tip)"));
		desList.add(new CalculatedProperty("leaftip.corner.angle.stdev", "The standard deviation of the leaf corner angles. "
				+ "The lower this value, the more uniform are the leaf tip corner angles."));
		desList.add(new CalculatedProperty("leaftip.corner.angle.skewness",
				"The 'skewness' of the leaf tip corner angle values. 'Skewness' is a statistical term, "
						+ "indicating the tendency of the value distribution to lean to one side of the value range."));
		desList.add(new CalculatedProperty("leaftip.corner.angle.kurtosis",
				"The 'kurtosis' of the leaf tip corner angle values. 'Kurtosis' is a statistical term, indicating the 'peakedness' of the value distribution."));
		desList.add(new CalculatedProperty("leaftip.angle.mean", "The average leaf tip angle."));
		desList.add(new CalculatedProperty("leaftip.angle.stdev", "The standard deviation of the leaf angles. "
				+ "The lower this value, the more uniform are the leaf tip angles."));
		desList.add(new CalculatedProperty("leaftip.angle.skewness",
				"The 'skewness' of the leaf tip angle values. 'Skewness' is a statistical term, "
						+ "indicating the tendency of the value distribution to lean to one side of the value range."));
		desList.add(new CalculatedProperty("leaftip.angle.kurtosis",
				"The 'kurtosis' of the leaf tip angle values. 'Kurtosis' is a statistical term, indicating the 'peakedness' of the value distribution."));
		
		for (Channel c : Channel.values()) {
			for (FirstOrderTextureFeatures tf : FirstOrderTextureFeatures.values()) {
				desList.add(new CalculatedProperty(c + ".leaftip.*." + tf, tf.getNiceName()
						+ " - first order texture property (independent of pixel neighbors). Calculated on grayscale image derived from channel " + c
						+ " within leftip area." +
						(tf.getReferenceLink() != null ? " Further information: <a href='" + tf.getReferenceLink() + "'>Link</a>." : "")));
			}
			
			for (GLCMTextureFeatures tf : GLCMTextureFeatures.values()) {
				desList
						.add(new CalculatedProperty(
								c + ".leaftip.*." + tf,
								tf.getNiceName()
										+ " - Grey Level Co-occurrence Matrix (GLCM) texture property (independent of pixel neighbors). Calculated on grayscale image derived from channel "
										+ c + " within leftip area." +
										(tf.getReferenceLink() != null ? " Further information: <a href='" + tf.getReferenceLink() + "'>Link</a>." : "")));
			}
		}
		desList
				.add(new CalculatedProperty(
						"leaftip.*.width_near_tip",
						"Number of pixels of the area between two selected search radii for leaf tip detection, divided by that radii difference. Therefore, the average leaf width at that area."));
		return desList.toArray(new CalculatedPropertyDescription[desList.size()]);
	}
}