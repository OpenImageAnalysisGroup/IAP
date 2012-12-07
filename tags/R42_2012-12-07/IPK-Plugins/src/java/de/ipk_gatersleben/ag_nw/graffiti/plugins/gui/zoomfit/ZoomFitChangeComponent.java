/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.zoomfit;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.Collection;
import java.util.ConcurrentModificationException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point2d;

import org.AttributeHelper;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.gui.GraffitiComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.plugin.view.ZoomListener;
import org.graffiti.plugin.view.Zoomable;
import org.graffiti.plugins.views.defaults.GraffitiView;
import org.graffiti.selection.SelectionEvent;
import org.graffiti.selection.SelectionListener;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.PaintStatusSupport;

/**
 * @author Christian Klukas
 */
public class ZoomFitChangeComponent extends JToolBar implements
					GraffitiComponent, ActionListener, ViewListener, SessionListener,
					SelectionListener, ZoomListener {
	
	// ~ Instance fields ========================================================
	
	private static final long serialVersionUID = 1L;
	
	/** The zoom buttons */
	private JButton jbZoomOut, jbZoomIn, jbZoom1to1, jbZoomRegion;
	
	private JSlider zoomSlider;
	
	/** active session */
	private static Session activeSession;
	
	private String prefComp;
	
	private static ZoomFitChangeComponent instance = null;
	
	private static boolean useSmooth = false;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for ZoomChangeComponent.
	 * 
	 * @param prefComp
	 *           DOCUMENT ME!
	 */
	public ZoomFitChangeComponent(String prefComp) {
		super("Zoom");
		this.prefComp = prefComp;
		ZoomFitChangeComponent.instance = this;
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		
		ImageIcon icon1 = new ImageIcon(cl.getResource(path
							+ "/images/lupeMinus.gif"));
		ImageIcon icon2 = new ImageIcon(cl.getResource(path
							+ "/images/lupePlus.gif"));
		ImageIcon icon3 = new ImageIcon(cl.getResource(path
							+ "/images/lupe1zu1.gif"));
		ImageIcon icon4 = new ImageIcon(cl.getResource(path
							+ "/images/lupeRegion.gif"));
		
		int s = 2;
		
		jbZoomOut = new JButton(icon1);
		jbZoomOut.setMargin(new Insets(s, s, s, s));
		jbZoomOut.addActionListener(this);
		jbZoomOut.setToolTipText("Zoom: Out");
		add(jbZoomOut);
		
		zoomSlider = new JSlider(0, 100, 50);
		zoomSlider.addChangeListener(getSliderChangeListener());
		zoomSlider.setToolTipText("Zoom");
		Dimension dim = zoomSlider.getPreferredSize();
		dim.width = 130;
		zoomSlider.setPreferredSize(dim);
		// add(zoomSlider);
		
		jbZoomIn = new JButton(icon2);
		jbZoomIn.setMargin(new Insets(s, s, s, s));
		jbZoomIn.addActionListener(this);
		jbZoomIn.setToolTipText("Zoom: In");
		add(jbZoomIn);
		
		jbZoom1to1 = new JButton(icon3);
		jbZoom1to1.setMargin(new Insets(s, s, s, s));
		jbZoom1to1.addActionListener(this);
		jbZoom1to1.setToolTipText("Zoom: 100%");
		add(jbZoom1to1);
		
		final ZoomFitChangeComponent zzz = this;
		
		jbZoomRegion = new JButton(icon4);
		jbZoomRegion.setMargin(new Insets(s, s, s, s));
		jbZoomRegion.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				ZoomFitChangeComponent.useSmooth = activeSession != null && activeSession.getGraph() != null && activeSession.getGraph().getNumberOfNodes() <= 500;
//				zzz.actionPerformed(e);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						zzz.actionPerformed(e);
					}
				});
			}
		});
		jbZoomRegion.setToolTipText("Zoom: Selected Region / Complete Graph (no selection)");
		add(jbZoomRegion);
		
		// setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		
		validate();
	}
	
	private ChangeListener getSliderChangeListener() {
		return new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int val = zoomSlider.getValue();
				MainFrame.showMessage("Active Zoom: " + val, MessageType.INFO);
			}
		};
	}
	
	// ~ Methods ================================================================
	public static void zoomIn() {
		ActionEvent e = new ActionEvent(instance.jbZoomIn, 0, "");
		instance.actionPerformed(e);
	}
	
	public static void zoomOut() {
		ActionEvent e = new ActionEvent(instance.jbZoomOut, 0, "");
		instance.actionPerformed(e);
	}
	
	public static void zoomRegion(boolean smooth) {
		useSmooth = smooth;
		ActionEvent e = new ActionEvent(instance.jbZoomRegion, 0, "");
		instance.actionPerformed(e);
		useSmooth = false;
	}
	
	public static void zoomRegion(boolean smooth, Zoomable view) {
		zoomRegion(smooth, view, null, 30);
	}
	
	public static void zoomRegion(boolean smooth, Zoomable view, Collection<GraphElement> elements) {
		zoomRegion(smooth, view, elements, 30);
	}
	
	public static void zoomRegion(boolean smooth, Zoomable view, Collection<GraphElement> elements, int zoomIntoValue) {
		useSmooth = smooth;
		ActionEvent e = new ActionEvent(instance.jbZoomRegion, 0, "");
		instance.zoomView(e, view, elements, zoomIntoValue);
		useSmooth = false;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		if (activeSession != null) {
			Zoomable myView = activeSession.getActiveView();
			
			zoomView(e, myView, null, 30);
		}
	}
	
	private void zoomView(final ActionEvent e, Zoomable myView, Collection<GraphElement> elements, int zoomIntoValue) {
		
		final double plusMinusZoom = e.getSource().equals(jbZoomOut) ? 0.05 : 0.05;
		
		if (myView instanceof PaintStatusSupport) {
			PaintStatusSupport pss = (PaintStatusSupport) myView;
			if (pss.statusDrawInProgress()) {
				MainFrame.showMessageDialog("Please do not change view characteristics until drawing has completed.", "Image Creation in Progress");
				return;
			}
		}
		
		AffineTransform currentZoom = myView.getZoom();
		final ZoomListener zoomView = (ZoomListener) myView;
		View view = (View) myView;
		if (!(view instanceof GraffitiView)) {
			MainFrame.showMessage("Operation not supported for this view type", MessageType.INFO);
			return;
		}
		final JScrollPane scrollPane;
		Dimension sps;
		try {
			scrollPane = (JScrollPane) ((JComponent) zoomView).getParent().getParent();
			sps = scrollPane.getViewport().getSize();
		} catch (ClassCastException cce) {
			sps = ((JComponent) myView).getPreferredSize();
			return;
		}
		final Dimension scrollPaneSize = sps;
		
		if (e.getSource().equals(jbZoom1to1)) {
			final AffineTransform at = new AffineTransform();
			at.setToScale(1, 1);
			zoomView.zoomChanged(at);
			MainFrame.showMessage("Active Zoom: " + (int) (100d * at.getScaleX()) + "%", MessageType.INFO);
		}
		if (e.getSource().equals(jbZoomRegion) || e.getSource().equals(jbZoomOut) || e.getSource().equals(jbZoomIn)) {
			
			Rectangle currentViewRect = scrollPane.getViewport().getViewRect();
			Point a = currentViewRect.getLocation();
			Point b = new Point(a.x + currentViewRect.width, a.y + currentViewRect.height);
			try {
				currentZoom.inverseTransform(a, a);
				currentZoom.inverseTransform(b, b);
				currentViewRect = new Rectangle(a.x, a.y, b.x - a.x, b.y - a.y);
			} catch (NoninvertibleTransformException e1) {
				System.err.println(e1);
			}
			
			Rectangle targetViewRect;
			
			if (e.getSource().equals(jbZoomRegion)) {
				Rectangle selectionViewRect = getViewRectFromSelection(view, elements);
				if (selectionViewRect == null)
					return;
				targetViewRect = selectionViewRect;
//				boolean changed = false;
				
//				if (targetViewRect.width < currentViewRect.width && targetViewRect.height < currentViewRect.height &&
//									Math.abs(targetViewRect.getCenterX() - currentViewRect.getCenterX()) > targetViewRect.width / 20d &&
//									Math.abs(targetViewRect.getCenterY() - currentViewRect.getCenterY()) > targetViewRect.height / 20d) {
//					Rectangle tryNewRect = new Rectangle(targetViewRect);
//					tryNewRect.grow((currentViewRect.width - targetViewRect.width) / zoomIntoValue,
//										(currentViewRect.height - targetViewRect.height) / zoomIntoValue);
//					if ((tryNewRect.getCenterX() >= tryNewRect.width / 2 && tryNewRect.getCenterX() <= view.getViewComponent().getWidth() - tryNewRect.width / 2)
//										|| (tryNewRect.getCenterY() >= tryNewRect.height / 2 && tryNewRect.getCenterY() <= view.getViewComponent().getHeight()
//															- tryNewRect.height / 2)) {
//						targetViewRect = tryNewRect;
//						changed = true;
//					}
//				}
				
//				if (!changed)
//					targetViewRect.grow((int) (targetViewRect.width / (zoomIntoValue / 3d)), (int) (targetViewRect.height / (zoomIntoValue / 3d)));
//				targetViewRect = selectionViewRect;
			} else {
				targetViewRect = scrollPane.getViewport().getViewRect();
				if (e.getSource().equals(jbZoomOut)) {
					targetViewRect.grow((int) (targetViewRect.width * plusMinusZoom / 2), (int) (targetViewRect.height * plusMinusZoom / 2));
				} else {
					targetViewRect.grow(-(int) (targetViewRect.width * plusMinusZoom / 2), -(int) (targetViewRect.height * plusMinusZoom / 2));
				}
				a = targetViewRect.getLocation();
				b = new Point(a.x + targetViewRect.width, a.y + targetViewRect.height);
				try {
					currentZoom.inverseTransform(a, a);
					currentZoom.inverseTransform(b, b);
					targetViewRect = new Rectangle(a.x, a.y, b.x - a.x, b.y - a.y);
				} catch (NoninvertibleTransformException e1) {
					ErrorMsg.addErrorMessage(e1);
				}
			}

			
			final double srcSmallestX = currentViewRect.getX();
			final double srcSmallestY = currentViewRect.getY();
			final double srcGreatestX = currentViewRect.getX() + currentViewRect.getWidth();
			final double srcGreatestY = currentViewRect.getY() + currentViewRect.getHeight();
			
			final double smallestX = targetViewRect.getX();
			final double smallestY = targetViewRect.getY();
			final double greatestX = targetViewRect.getX() + targetViewRect.getWidth();
			final double greatestY = targetViewRect.getY() + targetViewRect.getHeight();
			
			boolean smooth = !e.getSource().equals(jbZoomIn) && !e.getSource().equals(jbZoomOut); // (!e.getSource().equals(jbZoomRegion))
			if (!useSmooth)
				smooth = false;
			if (!smooth) {
				setZoom(zoomView, scrollPane, scrollPaneSize, smallestX, smallestY, greatestX, greatestY);
			} else {
				int duration = 300;
				if (e.getSource().equals(jbZoomRegion))
					duration = duration / 2;
				long startTime = System.currentTimeMillis();
				double f = 0;
				final ThreadSafeOptions tso = new ThreadSafeOptions();
				tso.setBval(0, false);
				while (f < 1) {
					long currTime = System.currentTimeMillis();
					f = (currTime - startTime) / (double) duration;
					if (f > 1)
						f = 1;
					if (f < 0)
						f = 0;
					double tx1 = getScale(f, srcSmallestX, smallestX);
					double ty1 = getScale(f, srcSmallestY, smallestY);
					double tx2 = getScale(f, srcGreatestX, greatestX);
					double ty2 = getScale(f, srcGreatestY, greatestY);
					setZoom(zoomView, scrollPane, scrollPaneSize, tx1, ty1, tx2, ty2);
					scrollPane.paintImmediately(scrollPane.getVisibleRect());
					try {
						Thread.sleep(10);
					} catch (InterruptedException err) {
						ErrorMsg.addErrorMessage(err);
					}
				}
				setZoom(zoomView, scrollPane, scrollPaneSize, smallestX, smallestY, greatestX, greatestY);
			}
		}
	}
	
	public static void zoomToPoint(Zoomable myView, Point coords, double zoomFactor) {
		double plusMinusZoom = zoomFactor;
		if (myView instanceof PaintStatusSupport) {
			PaintStatusSupport pss = (PaintStatusSupport) myView;
			if (pss.statusDrawInProgress()) {
				MainFrame.showMessageDialog("Please do not change view characteristics until drawing has completed.", "Image Creation in Progress");
				return;
			}
		}
		
		AffineTransform currentZoom = myView.getZoom();
		/*
		 * check for exceeding zoom level
		 * scaleX and scaleY are supposed to be equal
		 */
		if(zoomFactor > 0 && myView.getZoom().getScaleX() + myView.getZoom().getScaleX() * Math.abs(zoomFactor) > 40.0)
			return;
		if(zoomFactor < 0 && myView.getZoom().getScaleX() - myView.getZoom().getScaleX() * Math.abs(zoomFactor) < 0.02)
			return;
		
		final ZoomListener zoomView = (ZoomListener) myView;
		View view = (View) myView;
		if (!(view instanceof GraffitiView)) {
			MainFrame.showMessage("Operation not supported for this view type", MessageType.INFO);
			return;
		}
		Dimension sps;
		JScrollPane scrollPane;
		try {
			scrollPane = (JScrollPane) ((JComponent) zoomView).getParent().getParent();
			sps = scrollPane.getViewport().getSize();
		} catch (ClassCastException cce) {
			sps = ((JComponent) myView).getPreferredSize();
			return;
		}
		

		Point mouseCoord = new Point(coords.x, coords.y);
		try {
			currentZoom.inverseTransform(mouseCoord, mouseCoord);
		} catch (NoninvertibleTransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		
		
		Rectangle currentViewRect = scrollPane.getViewport().getViewRect();
		Point a = currentViewRect.getLocation();
		Point b = new Point(a.x + currentViewRect.width, a.y + currentViewRect.height);

		
//		System.out.println("sps: "+sps.toString());
//		System.out.println("currentViewRect: (before transform)"+currentViewRect.toString());
		
		/*
		 * transform current view to the actual visible area
		 */
		try {
			currentZoom.inverseTransform(a, a);
			currentZoom.inverseTransform(b, b);
			currentViewRect = new Rectangle(a.x, a.y, b.x - a.x, b.y - a.y);
		} catch (NoninvertibleTransformException e1) {
			System.err.println(e1);
		}

		/*
		 * get the relational point of the mouse in the view (between 0..1)
		 */
		Point2d mouseCoordRel = new Point2d(
				(double)(mouseCoord.x - currentViewRect.x) / currentViewRect.width, 
				(double)(mouseCoord.y - currentViewRect.y) / currentViewRect.height);
		
//		System.out.println("currentViewRect: ( after transform)"+currentViewRect.toString());
//		System.out.println("mousepos rel to viewport: "+mouseCoordRel.toString());

		Rectangle targetViewRect = currentViewRect;
		
//		System.out.println("point at "+mouseCoordRel.x+"%: "+ (targetViewRect.x + mouseCoordRel.x * (double)targetViewRect.width));
//		System.out.println("point at "+mouseCoordRel.y+"%: "+ (targetViewRect.y + mouseCoordRel.y * (double)targetViewRect.height));

		targetViewRect.grow(-(int) (targetViewRect.width * plusMinusZoom / 2), -(int) (targetViewRect.height * plusMinusZoom / 2));

//		System.out.println("targetViewRect: "+targetViewRect.toString());

		/*
		 * move the current view in a way, that the viewport coordinates under the mouse cursor stay the same
		 * after the zoom. in other words, the two rectangles (before and after zoom) are aligned to the
		 * mouse cursor coordinates
		 */
		targetViewRect.x = (int)Math.round((double)mouseCoord.x - mouseCoordRel.x * (double)targetViewRect.width); 
		targetViewRect.y = (int)Math.round((double)mouseCoord.y  - mouseCoordRel.y * (double)targetViewRect.height); 		

//		System.out.println("targetViewRect2: "+targetViewRect.toString());
		
//		System.out.println("point at_"+mouseCoordRel.x+"%: "+ (targetViewRect.x + mouseCoordRel.x * (double)targetViewRect.width));
//		System.out.println("point at_"+mouseCoordRel.y+"%: "+ (targetViewRect.y + mouseCoordRel.y * (double)targetViewRect.height));

		final double smallestX = targetViewRect.getX();
		final double smallestY = targetViewRect.getY();
		final double greatestX = targetViewRect.getX() + targetViewRect.getWidth();
		final double greatestY = targetViewRect.getY() + targetViewRect.getHeight();

		instance.setZoom(zoomView, scrollPane, sps, smallestX, smallestY, greatestX, greatestY);
	}
	
	private double getScale(double x, double a, double b) {
		double f2 = 1.2d / (1 + Math.exp(-(x - 0.5) * 5d)) - 0.1;
		if (f2 < 0)
			f2 = 0;
		if (f2 > 1)
			f2 = 1;
		return a + (b - a) * f2;
	}
	
	private Rectangle getViewRectFromSelection(View view, Collection<GraphElement> elements) {
		Rectangle viewRect = null;
		if (elements == null)
			elements = GraphHelper.getSelectedOrAllGraphElements(view.getGraph());
		for (GraphElement ge : elements) {
			if (view instanceof GraphView && ((GraphView) view).isHidden(ge))
				continue;
			
			GraphElementComponent gvc = view.getComponentForElement(ge);
			Rectangle r = null;
			boolean ra = view.redrawActive();
			if ((gvc == null || ra) && (ge instanceof Node)) {
				if (!AttributeHelper.isHiddenGraphElement(ge))
					r = AttributeHelper.getNodeRectangleAWT((Node) ge);
			} else
				if (!ra && gvc != null)
					r = gvc.getBounds();
			if (r == null)
				continue;
			if (viewRect == null)
				viewRect = r;
			else
				viewRect.add(r);
			if (gvc != null)
				try {
					for (Object o : gvc.getAttributeComponents()) {
						if (o instanceof JComponent && ((JComponent) o).isVisible()) {
							Rectangle bounds = ((JComponent) o).getBounds();
							if (viewRect == null)
								viewRect = bounds;
							else
								viewRect.add(bounds);
						}
					}
				} catch (ConcurrentModificationException cc) {
					
				}
		}
		return viewRect;
	}
	

	
	private void setZoom(final ZoomListener zoomView, JScrollPane scrollPane, Dimension scrollPaneSize,
						double smallestX, double smallestY, double greatestX, double greatestY) {
		double zomedSizeX, zomedSizeY;
		zomedSizeX = scrollPaneSize.getWidth() / (greatestX - smallestX);
		zomedSizeY = scrollPaneSize.getHeight() / (greatestY - smallestY);
		final boolean xIsLimit = zomedSizeX < zomedSizeY;
		
		double borderPercent = 0; // 0.1;
		
		double zoomFaktorWanted = min2(zomedSizeX, zomedSizeY)
							* (1 - borderPercent);
		final double zoomFaktor = zoomFaktorWanted; // min2(zoomFaktorWanted, 5); // maximum 500% zoom!
		
		final AffineTransform at = new AffineTransform();
		at.setToScale(zoomFaktor, zoomFaktor);
		
		MainFrame.showMessage("Active Zoom: " + (int) (100d * at.getScaleX()) + "%", MessageType.INFO);
		
		final double middleX = (greatestX + smallestX) / 2;
		final double middleY = (greatestY + smallestY) / 2;
		final double gtX = greatestX;
		final double gtY = greatestY;
		final double smX = smallestX;
		final double smY = smallestY;
		final double bdP = borderPercent;
		
		final JScrollPane spf = scrollPane;
		final Dimension spsf = scrollPaneSize;
		
		zoomView.zoomChanged(at);
		if (xIsLimit) {
			double offX = (gtX - smX) * bdP / 2;
			spf.getHorizontalScrollBar().setValue(
								(int) ((smX - offX) * at.getScaleX()));
			double targetY = middleY * zoomFaktor - spsf.getHeight()
								/ 2;
			spf.getVerticalScrollBar().setValue((int) targetY);
		} else {
			double offY = (gtY - smY) * bdP / 2;
			spf.getVerticalScrollBar().setValue(
								(int) ((smY - offY) * at.getScaleY()));
			double targetX = middleX * zoomFaktor - spsf.getWidth()
								/ 2;
			spf.getHorizontalScrollBar().setValue((int) targetX);
		}
		
		/*
		 * SwingUtilities.invokeLater(new Runnable() {
		 * public void run() {
		 * zoomView.zoomChanged(at);
		 * if (xIsLimit) {
		 * double offX = (gtX - smX) * bdP / 2;
		 * spf.getHorizontalScrollBar().setValue(
		 * (int) ((smX - offX) * at.getScaleX()));
		 * double targetY = middleY * zoomFaktor - spsf.getHeight()
		 * / 2;
		 * spf.getVerticalScrollBar().setValue((int) targetY);
		 * } else {
		 * double offY = (gtY - smY) * bdP / 2;
		 * spf.getVerticalScrollBar().setValue(
		 * (int) ((smY - offY) * at.getScaleY()));
		 * double targetX = middleX * zoomFaktor - spsf.getWidth()
		 * / 2;
		 * spf.getHorizontalScrollBar().setValue((int) targetX);
		 * }
		 * }
		 * });
		 */
	}
	
	/**
	 * @param smallestX
	 *           Value 1
	 * @param cx
	 *           Value 2
	 * @return The smaller one of the parameters
	 */
	private double min2(double smallestX, double cx) {
		return smallestX < cx ? smallestX : cx;
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionChanged(org.graffiti.session.Session)
	 */
	public void sessionChanged(Session s) {
		activeSession = s;
		
		jbZoomOut.setVisible(s != null);
		jbZoomIn.setVisible(s != null);
		jbZoom1to1.setVisible(s != null);
		jbZoomRegion.setVisible(s != null);
		
		if (s != null) {
			viewChanged(s.getActiveView());
		}
		
	}
	
	/**
	 * @see org.graffiti.session.SessionListener#sessionDataChanged(org.graffiti.session.Session)
	 */
	public void sessionDataChanged(Session s) {
		activeSession = s;
		viewChanged(s.getActiveView());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.ViewListener#viewChanged(org.graffiti.plugin.view.View)
	 */
	public void viewChanged(View newView) {
		View view = newView;
		if (view == null || !(view instanceof GraffitiView)) {
			jbZoomOut.setVisible(false);
			jbZoomIn.setVisible(false);
			jbZoom1to1.setVisible(false);
			jbZoomRegion.setVisible(false);
		} else {
			jbZoomOut.setVisible(true);
			jbZoomIn.setVisible(true);
			jbZoom1to1.setVisible(true);
			jbZoomRegion.setVisible(true);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.selection.SelectionListener#selectionChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionChanged(SelectionEvent e) {
		e.getSelection();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.selection.SelectionListener#selectionListChanged(org.graffiti.selection.SelectionEvent)
	 */
	public void selectionListChanged(SelectionEvent e) {
		e.getSelection();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.gui.GraffitiComponent#getPreferredComponent()
	 */
	public String getPreferredComponent() {
		return prefComp;
	}
	
	public void zoomChanged(AffineTransform newZoom) {
		zoomSlider.setValue((int) (newZoom.getScaleX() * 100d));
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
