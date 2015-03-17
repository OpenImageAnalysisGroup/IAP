// ===============================IX===============================================
//
// DefaultParameterDialog.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultParameterDialog.java,v 1.6 2013-06-13 15:11:30 klukas Exp $

package org.graffiti.editor.dialog;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.ObjectRef;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.core.ImageBundle;
import org.graffiti.core.StringBundle;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.managers.EditComponentManager;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.ColorParameter;
import org.graffiti.plugin.parameter.DoubleParameter;
import org.graffiti.plugin.parameter.EdgeParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.JComponentParameter;
import org.graffiti.plugin.parameter.NodeParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.plugin.parameter.StringParameter;
import org.graffiti.selection.Selection;
import org.graffiti.session.Session;

/**
 * The default implementation of a parameter dialog.
 * 
 * @version $Revision: 1.6 $
 */
public class DefaultParameterDialog extends AbstractParameterDialog implements
		ActionListener, WindowListener {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	// ~ Instance fields ========================================================
	
	/** The <code>ImageBundle</code> of the view type chooser. */
	protected ImageBundle iBundle = ImageBundle.getInstance();
	
	/** The panel used to display and change parameter values. */
	protected ParameterEditPanel paramEditPanel;
	
	/** The <code>StringBundle</code> of the view type chooser. */
	protected StringBundle sBundle = StringBundle.getInstance();
	
	/** The list of parameters, the user is editing. */
	protected Parameter[] params;
	
	/** The value edit component manager, the edit panel needs. */
	private final EditComponentManager editComponentManager;
	
	/** The dialog's buttons. */
	private final JButton cancel;
	
	/** The dialog's buttons. */
	private final JButton ok;
	
	/** The panel, which contains the parameters. */
	private final JPanel paramsPanel;
	
	/** <code>true</code>, if the user selected the ok button in this dialog. */
	private boolean selectedOk = false;
	
	private final String algorithmName;
	
	private final Collection<Session> validSessions = new ArrayList<Session>();
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructor for DefaultParameterDialog.
	 * 
	 * @param editComponentManager
	 *           DOCUMENT ME!
	 * @param parent
	 *           the parent of this dialog.
	 * @param parameters
	 *           the array of parameters to edit in this dialog.
	 * @param selection
	 *           DOCUMENT ME!
	 * @param algorithmName
	 *           the name of the algorithm, to edit the parameters
	 *           for.
	 */
	public DefaultParameterDialog(EditComponentManager editComponentManager,
			Component parent, Parameter[] parameters, Selection selection,
			String algorithmName, Object description) {
		this(editComponentManager, parent, parameters, selection, algorithmName, description, null, true);
	}
	
	public DefaultParameterDialog(EditComponentManager editComponentManager,
			Component parent, Parameter[] parameters, Selection selection,
			String algorithmName, Object description, boolean okOnly, boolean noButton, boolean allowMultipleGraphTargets) {
		this(editComponentManager, parent, parameters, selection, algorithmName, description, null, okOnly, noButton, allowMultipleGraphTargets, "OK");
	}
	
	public DefaultParameterDialog(EditComponentManager editComponentManager,
			Component parent, Parameter[] parameters, Selection selection,
			String algorithmName, Object descriptionOrComponent, JComponent descComponent, boolean allowMultipleGraphTargets) {
		this(editComponentManager, parent, parameters, selection, algorithmName, descriptionOrComponent, descComponent, false, false, allowMultipleGraphTargets,
				"OK");
	}
	
	public DefaultParameterDialog(EditComponentManager editComponentManager,
			Component parent, Parameter[] parameters, Selection selection,
			String algorithmName, Object descriptionOrComponent,
			JComponent descComponent, boolean okOnly, boolean noButton, boolean allowMultipleGraphTargets,
			String okOnlyButtonText) {
		this(editComponentManager, parent, parameters, selection, algorithmName, descriptionOrComponent, descComponent, false, false, allowMultipleGraphTargets,
				"OK", true);
	}
	
	public DefaultParameterDialog(EditComponentManager editComponentManager,
			Component parent, Parameter[] parameters, Selection selection,
			String algorithmNameUsedAsTitle, Object descriptionOrComponent,
			JComponent descComponent, boolean okOnly, boolean noButton, boolean allowMultipleGraphTargets,
			String okOnlyButtonText, boolean modal) {
		
		super(parent instanceof Frame ? (Frame) parent : null, modal);
		
		validSessions.clear();
		try {
			Session s = MainFrame.getInstance().getActiveSession();
			if (s != null)
				validSessions.add(s);
		} catch (Exception e) {
			// empty
		}
		
		String description = "";
		if (descriptionOrComponent instanceof String)
			description = (String) descriptionOrComponent;
		else
			descComponent = (JComponent) descriptionOrComponent;
		
		if (algorithmNameUsedAsTitle != null && algorithmNameUsedAsTitle.endsWith("..."))
			algorithmNameUsedAsTitle = algorithmNameUsedAsTitle.substring(0, algorithmNameUsedAsTitle.length() - "...".length());
		
		this.algorithmName = algorithmNameUsedAsTitle;
		
		this.editComponentManager = editComponentManager;
		
		this.params = parameters;
		
		getContentPane().setLayout(new BorderLayout());
		
		// setTitle("Set algorithm parameters");
		setTitle(algorithmNameUsedAsTitle);
		
		setSize(420, 320);
		setResizable(true);// false
		setLocationRelativeTo(parent);
		
		ok = new JButton(sBundle.getString("run.dialog.button.run"));
		cancel = new JButton(sBundle.getString("run.dialog.button.cancel"));
		
		if (okOnlyButtonText != null && okOnlyButtonText.indexOf(";") > 0) {
			cancel.setText(okOnlyButtonText.substring(okOnlyButtonText.lastIndexOf(";") + ";".length()));
			okOnlyButtonText = okOnlyButtonText.substring(0, okOnlyButtonText.lastIndexOf(";"));
			okOnly = false;
		}
		
		if (okOnlyButtonText != null && okOnlyButtonText.length() > 0)
			ok.setText(okOnlyButtonText);
		
		// JPanel buttonsPanel = new JPanel();
		//
		// buttonsPanel.add(ok);
		// buttonsPanel.add(cancel);
		
		paramsPanel = createValueEditContainer(params, selection,
				(description != null && description.length() > 0) ? description : "", algorithmNameUsedAsTitle, descComponent); // sBundle.getString("run.dialog.desc")
		
		// algorithmName + " parameters"
		
		ok.setEnabled(true);
		
		getRootPane().setDefaultButton(ok);
		
		getRootPane().getActionMap().put("escapeAction", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent event) {
				DefaultParameterDialog.this.dispose();
			}
		});
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "escapeAction");
		
		defineLayout(okOnly, noButton, allowMultipleGraphTargets);
		addListeners();
		
		pack();
		pack();
		setLocationRelativeTo(parent);
		
		// fixing dialogs, which are way too big for the screen -> scrollpanes may be useful
		Dimension size = getSize();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if (size.height > screen.height - 50)
			size.height = screen.height - 50;
		if (size.width > screen.width - 50)
			size.width = screen.width - 50;
		
		setSize(size);
		
		// end fixing
		
		setVisible(true);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.editor.dialog.ParameterDialog#getEditedParameters()
	 */
	@Override
	public Parameter[] getEditedParameters() {
		return this.paramEditPanel.getUpdatedParameters();
	}
	
	@Override
	public void pack() {
		if (!SwingUtilities.isEventDispatchThread())
			Thread.dumpStack();
		else
			super.pack();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	@Override
	public boolean isOkSelected() {
		return selectedOk;
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		
		if (src == cancel) {
			MainFrame.showMessage(algorithmName + " not started",
					MessageType.INFO, 3000);
			dispose();
		} else
			if (src == ok) {
				okSelected();
			}
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowActivated(WindowEvent)
	 */
	@Override
	public void windowActivated(WindowEvent arg0) {
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowClosed(WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent arg0) {
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowClosing(WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent arg0) {
		dispose();
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowDeactivated(WindowEvent)
	 */
	@Override
	public void windowDeactivated(WindowEvent arg0) {
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowDeiconified(WindowEvent)
	 */
	@Override
	public void windowDeiconified(WindowEvent arg0) {
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowIconified(WindowEvent)
	 */
	@Override
	public void windowIconified(WindowEvent arg0) {
	}
	
	/**
	 * @see java.awt.event.WindowListener#windowOpened(WindowEvent)
	 */
	@Override
	public void windowOpened(WindowEvent arg0) {
	}
	
	/**
	 * Adds the listeners to the dialog.
	 */
	private void addListeners() {
		cancel.addActionListener(this);
		ok.addActionListener(this);
		addWindowListener(this);
	}
	
	/**
	 * Creates and returns a value edit container for the given parameters.
	 * 
	 * @param parameters
	 *           the list of parameters, the user wants to edit.
	 * @param selection
	 *           DOCUMENT ME!
	 * @param descComponent
	 * @return DOCUMENT ME!
	 */
	private JPanel createValueEditContainer(Parameter[] parameters,
			Selection selection, String title, String heading, JComponent descComponent) {
		this.paramEditPanel = new ParameterEditPanel(parameters,
				editComponentManager != null ? editComponentManager.getEditComponents() : null, selection, title, true, heading, descComponent);
		
		return this.paramEditPanel;
	}
	
	/**
	 * Defines the layout of this dialog.
	 */
	private void defineLayout(boolean okOnly, boolean noButton, boolean allowMultipleGraphTargets) {
		double border = 8d;
		double[][] size = {
				new double[] { border, TableLayoutConstants.FILL, border },
				new double[] { border, TableLayoutConstants.FILL, border, TableLayoutConstants.PREFERRED, noButton ? 0 : border },
		};
		getContentPane().setLayout(new TableLayout(size));
		
		// paramsPanel.setBorder(BorderFactory.createEtchedBorder());
		//
		getContentPane().add(paramsPanel, "1,1");
		
		JComponent sessionselpan = null;
		
		if (allowMultipleGraphTargets) {
			final ObjectRef ml = new ObjectRef();
			sessionselpan = getSessionSelectionPanel(ml);
			ok.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					if (ml != null && ml.getObject() != null)
						((MouseListener) ml.getObject()).mouseExited(e);
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					if (ml != null && ml.getObject() != null)
						((MouseListener) ml.getObject()).mouseEntered(e);
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					
				}
			});
		}
		if (!noButton)
			getContentPane().add(
					TableLayout.get3Split(
							sessionselpan,
							TableLayout.getSplitVertical(null,
									TableLayout.get3Split(ok, null, okOnly ? null : cancel, TableLayoutConstants.PREFERRED, border,
											TableLayoutConstants.PREFERRED),
									TableLayout.FILL, TableLayoutConstants.PREFERRED), null,
							TableLayout.FILL, TableLayoutConstants.PREFERRED, TableLayout.FILL, border, 0),
					"1,3"
					);
		getContentPane().validate();
	}
	
	private JComponent getSessionSelectionPanel(ObjectRef ml) {
		MainFrame.getInstance();
		if (MainFrame.getSessions().size() <= 1)
			return new JLabel();
		else {
			final String pre = "<html><font color='#777777'><small>";
			final JLabel res = new JLabel(pre + getActiveWorkingSetDescription());
			
			Cursor c = new Cursor(Cursor.HAND_CURSOR);
			res.setCursor(c);
			final String hint = "Click &gt;here&lt; to modify working set";
			res.setOpaque(false);
			res.setToolTipText(getActiveWorkingSetDescriptionDetails());
			MouseListener m = new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
				}
				
				Color oldColor;
				boolean oldOpaque;
				
				@Override
				public void mouseExited(MouseEvent e) {
					res.setOpaque(oldOpaque);
					res.setBackground(oldColor);
					res.setToolTipText(getActiveWorkingSetDescriptionDetails());
					res.setText(pre + getActiveWorkingSetDescription());
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					oldOpaque = res.isOpaque();
					res.setOpaque(true);
					oldColor = res.getBackground();
					res.setBackground(new Color(240, 240, 255));
					res.setText(pre + hint);
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if (validSessions.size() <= 1) {
						validSessions.clear();
						validSessions.addAll(MainFrame.getSessions());
					} else {
						validSessions.clear();
						validSessions.add(MainFrame.getInstance().getActiveSession());
					}
					res.setToolTipText(getActiveWorkingSetDescriptionDetails());
					res.setText(pre + getActiveWorkingSetDescription());
				}
			};
			ml.setObject(m);
			res.addMouseListener(m);
			
			return res;
		}
	}
	
	private String getActiveWorkingSetDescription() {
		boolean isActiveGraph = false;
		try {
			if (validSessions.size() == 1 &&
					MainFrame.getInstance().getActiveSession() == validSessions.iterator().next())
				isActiveGraph = true;
		} catch (Exception e) {
			// empty
		}
		if (isActiveGraph)
			return "Process active graph";
		else {
			if (validSessions.size() > 1)
				return "Process " + validSessions.size() + " graphs";
			if (validSessions.size() == 1)
				return "Process " + validSessions.size() + " graph";
			else
				return "No graph available";
		}
	}
	
	private String getActiveWorkingSetDescriptionDetails() {
		try {
			StringBuilder res = new StringBuilder();
			int idx = 0;
			for (Session s : validSessions) {
				if (res.length() > 0)
					res.append("<br>");
				idx++;
				res.append(idx + ") graph " + s.getGraph().getName());
			}
			if (res.length() > 0)
				return "<html>Working set:<br>" + res.toString();
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * DOCUMENT ME!
	 */
	private void okSelected() {
		selectedOk = true;
		dispose();
	}
	
	/**
	 * @param description
	 *           In case the description is of type JComponent,
	 *           this GUI element will be shown at the top of the dialog. If this parameter
	 *           is of type String, a <code>JLabel</code> object will show the provided text.
	 *           If the description text starts with "[OK]", only the OK and not the Cancel button
	 *           will be shown. If the description text starts with "[]", no button
	 *           will be shown. If the description starts with "[Hello]", the single OK Button
	 *           will be titled "Hello". If the description starts with [Yes;No], two buttons,
	 *           titles 'Yes' and 'No' will be shown.
	 * @param title
	 *           The shown dialog window will use this value as its window title.
	 * @param parameters
	 * @return The return value depends on the selected button (OK/Cancel).
	 */
	@SuppressWarnings("unchecked")
	public static Object[] getInput(Object description, String title,
			Object... parameters) {
		
		title = StringManipulationTools.removeHTMLtags(title);
		
		if (title != null && title.endsWith("..."))
			title = title.substring(0, title.length() - "...".length());
		
		// Buttons: OK => close and return input values
		// Cancel => close and return null
		// Reset => set to initial values
		Parameter[] p = new Parameter[parameters.length / 2];
		for (int i = 0; i < p.length; i++) {
			Object desc = parameters[i * 2];
			String nameTitle, nameTooltip;
			String nameDesc;
			if (desc instanceof String)
				nameDesc = (String) desc;
			else
				if (desc instanceof JLabel)
					nameDesc = ((JLabel) desc).getText();
				else
					nameDesc = null;
			if (nameDesc != null && nameDesc.indexOf("@@") > 0) {
				nameTitle = nameDesc.substring(0, nameDesc.indexOf("@@"));
				nameTooltip = nameDesc.substring(nameDesc.indexOf("@@") + "@@".length());
			} else {
				nameTitle = nameDesc;
				nameTooltip = nameDesc;
			}
			Object param = parameters[i * 2 + 1];
			if (param instanceof JComponent) {
				JComponent val = (JComponent) param;
				JComponentParameter sp = new JComponentParameter(val, nameTitle, nameTooltip);
				p[i] = sp;
			} else
				if (param instanceof String[]) {
					String[] val = (String[]) param;
					ObjectListParameter sp = new ObjectListParameter(val[0], nameTitle, nameTooltip, val);
					p[i] = sp;
				} else
					if (param instanceof String) {
						String val = (String) param;
						StringParameter sp = new StringParameter(val, nameTitle, nameTooltip);
						p[i] = sp;
					} else
						if (param instanceof Double) {
							Double val = (Double) param;
							DoubleParameter dp = new DoubleParameter(val, nameTitle, nameTooltip);
							p[i] = dp;
						} else
							if (param instanceof Node) {
								Node val = (Node) param;
								NodeParameter ip = new NodeParameter(val.getGraph(), val, nameTitle, nameTooltip);
								p[i] = ip;
							} else
								if (param instanceof Edge) {
									Edge val = (Edge) param;
									EdgeParameter ip = new EdgeParameter(val, nameTitle, nameTooltip);
									p[i] = ip;
								} else
									if (param instanceof Integer) {
										Integer val = (Integer) param;
										IntegerParameter ip = new IntegerParameter(val, nameTitle, nameTooltip);
										p[i] = ip;
									} else
										if (param instanceof List) {
											List val = (List) param;
											ObjectListParameter ip = new ObjectListParameter(val.size() > 0 ? val.get(0) : null, nameTitle, nameTooltip, val);
											p[i] = ip;
										} else
											if (param instanceof Set) {
												Set val = (Set) param;
												ObjectListParameter ip = new ObjectListParameter(val.size() > 0 ? val.iterator().next() : null, nameTitle, nameTooltip, val);
												p[i] = ip;
											} else
												if (param instanceof Color) {
													Color c = (Color) param;
													ColorParameter sp = new ColorParameter(c, nameTitle, nameTooltip);
													p[i] = sp;
												} else
													if (param instanceof Boolean) {
														Boolean val = (Boolean) param;
														BooleanParameter ip = new BooleanParameter(val, nameTitle, nameTooltip);
														p[i] = ip;
													}
		}
		boolean modal = true;
		if (description != null && description != null && description instanceof String && ((String) description).startsWith("["))
			if (((String) description).indexOf("nonmodal,") > 0) {
				modal = false;
				description = StringManipulationTools.stringReplace((String) description, "nonmodal,", "");
			} else
				if (((String) description).indexOf("nonmodal") > 0) {
					modal = false;
					description = StringManipulationTools.stringReplace((String) description, "nonmodal", "");
				}
		
		boolean noButton = (description != null && description instanceof String && ((String) description).startsWith("[]"));
		if (noButton) {
			description = ((String) description).substring("[]".length());
			if (((String) description).length() <= 0)
				description = null;
		}
		boolean showOnlyOneButton = !noButton && oneButtonDescription(description);
		String buttonDesc = null;
		if (showOnlyOneButton) {
			buttonDesc = ((String) description).substring(((String) description).indexOf("[") + "[".length());
			buttonDesc = buttonDesc.substring(0, buttonDesc.indexOf("]"));
			description = ((String) description).substring(((String) description).indexOf("]") + "[".length());
			if (((String) description).length() <= 0)
				description = null;
		}
		if (description != null && description instanceof String) {
			if (((String) description).indexOf("<") >= 0
					&& ((String) description).indexOf(">") >= 0
					&& !((String) description).toUpperCase().startsWith("<HTML>"))
				description = "<html>" + (String) description;
		}
		
		boolean vis = MainFrame.getInstance() != null && MainFrame.getInstance().isVisible();
		Component ref;
		if (!vis)
			ref = ReleaseInfo.getApplet();
		else
			ref = MainFrame.getInstance();
		
		DefaultParameterDialog paramDialog = new DefaultParameterDialog(
				MainFrame.getInstance() != null ? MainFrame.getInstance().getEditComponentManager() : null,
				ref,
				p,
				(MainFrame.getInstance() != null && MainFrame.getInstance().getActiveEditorSession() != null ?
						MainFrame.getInstance().getActiveEditorSession().
								getSelectionModel().getActiveSelection() : null
				),
				title, description, null, showOnlyOneButton, noButton, false, buttonDesc, modal);
		if (paramDialog.isOkSelected()) {
			Parameter[] pe = paramDialog.getEditedParameters();
			Object[] result = new Object[pe.length];
			for (int i = 0; i < result.length; i++) {
				if (pe[i] != null)
					result[i] = pe[i].getValue();
			}
			return result;
		} else
			return null;
	}
	
	private static boolean oneButtonDescription(Object description) {
		if (description == null || !(description instanceof String))
			return false;
		String d = (String) description;
		if (d.indexOf("[") >= 0) {
			d = d.substring(d.indexOf("[") + "[".length());
			return d.indexOf("]") >= 0;
		} else
			return false;
	}
	
	@Override
	public Collection<Session> getTargetSessions() {
		return validSessions;
	}
	
	private static int scrollbarWidth = (int) (new JScrollBar(JScrollBar.VERTICAL).getPreferredSize().getWidth() + 1);
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension((int) d.getWidth() + scrollbarWidth, (int) d.getHeight());
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
