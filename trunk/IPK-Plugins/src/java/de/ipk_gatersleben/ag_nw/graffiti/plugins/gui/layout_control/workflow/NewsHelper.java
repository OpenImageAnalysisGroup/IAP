package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.HelperClass;
import org.JMButton;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsTrue;
import org.StringManipulationTools;
import org.SystemInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import de.ipk_gatersleben.ag_nw.graffiti.JLabelHTMLlink;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.Addon;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.helper.DBEgravistoHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class NewsHelper implements HelperClass {
	
	final WorkflowHelper thisW;
	RSSFeedManager rfm;
	JButton newsButton;
	JButton closeButton;
	JButton editFeeds;
	JCheckBox autoNews;
	JPanel res;
	double border = 5;
	
	// private JMButton subscribeButton;
	// private static boolean already_downloaded = false;
	
	public NewsHelper(WorkflowHelper wh) {
		this.thisW = wh;
	}
	
	public JPanel getNews(final JTabbedPane tabbedPane) {
		res = new JPanel();
		res.setBackground(null);
		res.setOpaque(false);
		
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border,
												TableLayoutConstants.PREFERRED,
												2 * border,
												TableLayoutConstants.PREFERRED,
												border } }; // Rows
		res.setLayout(new TableLayout(size));
		
		try {
			if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_news_download_enabled").exists()) {
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_news_download_enabled").delete();
			}
		} catch (Exception e) {
			// empty
		}
		
		ClassLoader cl = NewsHelper.class.getClassLoader();
		String path = NewsHelper.class.getPackage().getName().replace('.', '/') + "/images";
		ImageIcon btIcon = new ImageIcon(cl.getResource(path + "/refresh.png"));
		
		newsButton = createNewsButton("Download", "Refresh all Feeds", getNewsDownloadAction(tabbedPane, newsButton));
		newsButton.setText(null);
		newsButton.setIcon(btIcon);
		
		// subscribeButton = createNewsButton("Subscribe","Subscribe to a RSS News-Feed", getSubscribeAction());
		
		closeButton = createNewsButton("Close", "Close this Tab", new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (tabbedPane == null) {
					JTabbedPane jt = findTabbedPane(res);
					if (jt != null) {
						jt.remove(thisW);
					} else
						closeButton.setEnabled(false);
				} else {
					tabbedPane.remove(res);
				}
			}
			
			private JTabbedPane findTabbedPane(Component res) {
				if (res == null)
					return null;
				if (res instanceof JTabbedPane)
					return (JTabbedPane) res;
				else
					return findTabbedPane(res.getParent());
			}
		});
		
		editFeeds = createNewsButton("Edit", "Edit the Feeds File", getEditFeedAction());
		autoNews = new JCheckBox("<html><small>Auto-download");
		autoNews.addActionListener(getAutoNewsSettingActionListener(autoNews));
		autoNews.setOpaque(false);
		
		if (SystemInfo.isMac()) {
			autoNews.putClientProperty("JComponent.sizeVariant", "mini");
		}
		boolean auto = new SettingsHelperDefaultIsTrue().isEnabled("download_release_infos");
		autoNews.setSelected(auto);
		
		refreshFolderPanels();
		
		ErrorMsg.addOnAddonLoadingFinishedAction(new Runnable() {
			public void run() {
				boolean auto = new SettingsHelperDefaultIsTrue().isEnabled("download_release_infos");
				if (auto) {
					newsButton.setEnabled(false);
					editFeeds.setEnabled(false);
					autoNews.setEnabled(false);
					refreshFolderPanels();
					refreshNews(rfm, new Runnable() {
						public void run() {
							newsButton.setEnabled(true);
							autoNews.setEnabled(true);
							editFeeds.setEnabled(true);
						}
					},
										newsButton);
				}
				StringBuilder warning = new StringBuilder();
				int incomp = 0;
				if (AddonManagerPlugin.getInstance() != null)
					for (Addon a : AddonManagerPlugin.getInstance().getAddons()) {
						if (a.isActive() && !a.isTestedWithRunningVersion()) {
							if (warning.length() > 0)
								warning.append(", ");
							warning.append(a.getDescription().getName());
							incomp++;
						}
					}
				if (warning.length() > 0) {
					MainFrame.showMessage(" Active Add-on" +
										(incomp > 1 ? "s are " : " is ") + "not tested with " + DBEgravistoHelper.DBE_GRAVISTO_VERSION + ": " +
										warning.toString(),
										MessageType.INFO, 20000);
				}
			}
		});
		
		return res;
	}
	
	private JMButton createNewsButton(String buttonText, String tooltip, ActionListener clickAction) {
		JMButton bt = new JMButton("<html><small>" + buttonText);
		if (SystemInfo.isMac()) {
			bt.setText(StringManipulationTools.removeHTMLtags(bt.getText()));
			bt.putClientProperty("JComponent.sizeVariant", "mini");
		}
		bt.setOpaque(false);
		bt.setToolTipText(tooltip);
		bt.addActionListener(clickAction);
		return bt;
	}
	
	private ActionListener getEditFeedAction() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainFrame.showMessageDialog("<html>RSS-Feeds are saved in a text-file, which will be opened<br>" +
									"after pressing \"OK\".<br>" +
									"Please use this format for editing:<br><br><code>" +
									"name of new feed<br>" +
									"URL1" + RSSFeedManager.urlSeparator + "URL2" + RSSFeedManager.urlSeparator + "URL3" + RSSFeedManager.urlSeparator
									+ "...</code><br><br>" +
									"The \"" + RSSFeedManager.urlSeparator + "\" is used to define more than one source feed<br>" +
									"which will be shown in one newsfeed group.<br>", "Editing RSS-Feeds");
				RSSFeedManager.getInstance().checkRSSfile();
				AttributeHelper.showInBrowser(RSSFeedManager.getInstance().getFeedtextfile());
			}
		};
	}
	
	@SuppressWarnings("unused")
	private ActionListener getSubscribeAction() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Object[] res = MyInputHelper.getInput("<html>Please specify the feeds name and its URLs.<br><br><small>Separate URLs by using the \"|\" symbol.",
									"Subscribe to a News Feed", new Object[] {
														"Feed Name", "News Feed",
														"URL(s)", ""
				});
				if (res != null &&
									res.length == 2 && res[0] != null &&
									!(((String) res[0]).trim().equalsIgnoreCase("")) &&
									!(((String) res[1]).trim().equalsIgnoreCase(""))) {
					String feeds = (String) res[1];
					if (urlError(feeds))
						return;
					rfm.registerNewFeed(((String) res[0]).trim(), new String[] { ((String) res[1]).trim() });
					refreshFolderPanels();
					refreshNews(rfm, null, newsButton);
				} else {
					MainFrame.showMessageDialog("No URL specified!", "Error");
				}
			}
		};
	}
	
	private void refreshFolderPanels() {
		res.removeAll();
		
		res.add(TableLayout.get3Split(newsButton, autoNews,
							TableLayout.get3Split(null/* subscribeButton */, null, editFeeds, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 0, 0),
							TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED), "1,1");
		// res.add(TableLayout.get3Split(newsButton, autoNews,
		// TableLayout.get3Split(editFeeds, null, closeButton, TableLayout.PREFERRED, 4, TableLayout.PREFERRED, 0, 0),
		// TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED), "1,1");
		
		if (rfm == null)
			rfm = new RSSFeedManager(border);
		
		rfm.loadRegisteredFeeds();
		res.add(TableLayout.getMultiSplitVertical(rfm.getNewsComponents()), "1,3");
		// for(FolderPanel fp : rfm.getNewsComponents()) {fp.validate();fp.repaint();}
		res.validate();
		res.repaint();
	}
	
	private static JLabel getCustomizedLabel(JLabel label) {
		label.setBackground(Color.WHITE);
		return label;
	}
	
	private ActionListener getAutoNewsSettingActionListener(final JCheckBox autoNews) {
		if (new File(ReleaseInfo.getAppFolderWithFinalSep() + "auto_news_download_setting_enabled").exists()) {
			new File(ReleaseInfo.getAppFolderWithFinalSep() + "auto_news_download_setting_enabled").delete();
			try {
				new File(ReleaseInfo.getAppFolderWithFinalSep() + "setting_news_download_enabled").createNewFile();
			} catch (IOException err) {
				ErrorMsg.addErrorMessage(err);
			}
		}
		ActionListener res = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SettingsHelperDefaultIsTrue().setEnabled("download_release_infos", autoNews.isSelected());
			}
		};
		return res;
	}
	
	private ActionListener getNewsDownloadAction(
						final JTabbedPane tabbedPane, final JButton newsButton) {
		
		ActionListener res = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshFolderPanels();
				refreshNews(rfm, new Runnable() {
					
					public void run() {
						try {
							// tabbedPane.setSelectedComponent(res);
						} catch (Exception e) {
						};
						
					}
				}, newsButton);
			}
		};
		return res;
	}
	
	private boolean urlError(String feeds) {
		feeds = feeds.trim();
		if (feeds.contains("|")) {
			for (String url : feeds.split("|")) {
				try {
					URL u = new URL(url);
					u.openConnection();
				} catch (Exception e) {
					MainFrame.showMessageDialog("Invalid URL: " + url, "Error");
					return true;
				}
			}
			return false;
		} else {
			String url = feeds;
			try {
				URL u = new URL(url);
				u.openConnection();
			} catch (Exception e) {
				MainFrame.showMessageDialog("Invalid URL: " + url, "Error");
				return true;
			}
			return false;
		}
	}
	
	public static void refreshNews(
						final RSSFeedManager rfm,
						final Runnable finishTask, final JButton optRefreshNewsButton) {
		
		final ArrayList<FolderPanel> fpList = rfm.getNewsComponents();
		final ArrayList<String> urls = rfm.getUrls();
		final ArrayList<Boolean> removeHTML = rfm.getRemoveHTML();
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("One moment...",
							"Access news feed...");
		final HashMap<Integer, ArrayList<GuiRow>> idx2newsItems = new HashMap<Integer, ArrayList<GuiRow>>();
		final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
		
		final ArrayList<Date> latestDates = new ArrayList<Date>();
		
		Runnable workTask = new Runnable() {
			public void run() {
				URL feedUrl;
				status.setCurrentStatusValue(-1);
				try {
					int news = 0;
					for (int i = 0; i < urls.size(); i++) {
						Date templatest = null;
						final HashMap<GuiRow, Date> row2date = new HashMap<GuiRow, Date>();
						ArrayList<GuiRow> newsItems = new ArrayList<GuiRow>();
						String urlList = urls.get(i);
						for (String url : urlList.split(RSSFeedManager.splitUrlSeparator)) {
							if (url.length() == 0)
								continue;
							status.setCurrentStatusText1("Access news feed " + (i + 1) + "/" + urls.size() + ":");
							status.setCurrentStatusText2(url);
							status.setCurrentStatusValueFine(i * 100 / urls.size());
							try {
								feedUrl = new URL(url);
							}
							catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
								continue;
							}
							SyndFeedInput input = new SyndFeedInput();
							try {
								SyndFeed feed = input.build(new XmlReader(feedUrl));
								for (Object o : feed.getEntries()) {
									if (o instanceof SyndEntry) {
										SyndEntry se = (SyndEntry) o;
										Date d = se.getPublishedDate();
										if (d == null)
											d = se.getUpdatedDate();
										
										JLabel c1;
										if (d != null) {
											String lbl = df.format(d);
											c1 = new JLabel("<html><small>" + lbl);
											if (templatest == null)
												templatest = d;
											else {
												if (d.after(templatest))
													templatest = d;
											}
										} else
											c1 = new JLabel("");
										
										String text = getContentText(se.getContents(), removeHTML.get(i));
										ArrayList<JComponent> buttons = new ArrayList<JComponent>();
										
										text = constructButtons(text, buttons, optRefreshNewsButton);
										
										JLabel c2 = new JLabelHTMLlink("<html><small><b>"
															+ checkTextWidth(rfm, se.getTitle()) + "</b><br>"
															+ checkTextWidth(rfm, text), se.getLink(), "Click to see full text in browser");
										
										JComponent buttonRow = TableLayout.getMultiSplit(buttons, TableLayoutConstants.PREFERRED, 4, 0, 8, 2);
										c1 = getCustomizedLabel(c1);
										c2 = getCustomizedLabel(c2);
										GuiRow gr;
										if (buttons.size() == 0)
											gr = new GuiRow(c1, c2);
										else
											gr = new GuiRow(c1, TableLayout
																.getSplitVertical(c2, buttonRow, TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED));
										row2date.put(gr, d);
										newsItems.add(gr);
										news++;
									}
								}
							} catch (Exception e) {
								JLabel c1 = new JLabel("<html><small>No connection:");
								JLabel c2 = new JLabel("<html><small>" + e.getMessage());
								c1 = getCustomizedLabel(c1);
								c2 = getCustomizedLabel(c2);
								newsItems.add(new GuiRow(c1, c2));
							}
						}
						Collections.sort(newsItems, new Comparator<GuiRow>() {
							public int compare(GuiRow o1, GuiRow o2) {
								Date a = row2date.get(o2);
								Date b = row2date.get(o1);
								if (a != null && b != null)
									return a.compareTo(b);
								else
									return 0;
							}
						});
						boolean even = false;
						new Color(245, 245, 255);
						for (GuiRow gr : newsItems) {
							even = !even;
							gr.right.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
							if (even) {
								// gr.right.setOpaque(true);
								// gr.right.setBackground(bgc);
							}
						}
						latestDates.add(templatest);
						idx2newsItems.put(i, newsItems);
					}
					// set the folderpanels condensed, if there are no new messages
					for (int a = 0; a < fpList.size(); a++) {
						if (latestDates.get(a) != null && rfm.getDate(a) != null)
							fpList.get(a).setCondensedState(
												latestDates.get(a).toString().
																	equalsIgnoreCase(rfm.getDate(a)));
						else {
							boolean auto = new SettingsHelperDefaultIsTrue().isEnabled("download_release_infos");
							fpList.get(a).setCondensedState(auto);
						}
						String title = fpList.get(a).getTitle();
						if (title != null && title.length() > 0) {
							title = StringManipulationTools.stringReplace(title, "<font color='gray'>", "");
						}
						
						boolean already_numbered;
						try {
							Integer.parseInt(title.substring(title.lastIndexOf(" (") + 2, title.lastIndexOf(")")));
							already_numbered = true;
						}
							catch (Exception e) {
								already_numbered = false;
							}
							if (already_numbered)
								title = title.substring(0, title.lastIndexOf(" ("));
							int cnt = idx2newsItems.get(a).size();
							fpList.get(a).setTitle(title + " (" + cnt + ")");
							if (latestDates.get(a) != null)
								rfm.setDate(a, latestDates.get(a).toString());
						}
						status.setCurrentStatusValueFine(100);
						status.setCurrentStatusText1("Access finished");
						status.setCurrentStatusText2(news + " news item(s) found");
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
					rfm.saveRegisteredFeeds();
				}
			
			private String checkTextWidth(RSSFeedManager rfm, String text) {
				if (rfm.getMaxCol() <= 0)
					return text;
				else
					return StringManipulationTools.getWordWrap(text, rfm.getMaxCol());
			}
			
			/**
			 * [[addon:vanted3d/v0.4:url1|url2|...]] -> download add-on(s) to addon-folder
			 * [[download:Download:url1|url2|...]] -> download file(s) with filechoosedialog
			 * [[url:License:url1|url2|...]] -> show url(s)
			 * [[feed:Vanted Add-ons:url1|url2|...]] -> subscribe to feed(s)
			 * [[preference:Download:url1|url2|...]] -> download file(s) to preference-folder
			 **/
			private String constructButtons(String text,
								ArrayList<JComponent> buttons, final JButton newsButton) {
				int a = 0;
				for (String s : StringManipulationTools.removeTagsGetTextAndRemovedTexts(text, "[[", "]]")) {
					if (a == 0)
						text = s;
					else {
						int pos = s.indexOf(":");
						if (pos > 0) {
							final String label = s.substring(0, pos);
							s = s.substring(pos + ":".length());
							pos = s.indexOf(":");
							final String title = s.substring(0, pos);
							s = s.substring(pos + ":".length());
							final String[] urls = s.split(RSSFeedManager.splitUrlSeparator);
							JComponent b = null;
							FeedDownloadType type = FeedDownloadType.getType(label);
							switch (type) {
								case ADDON: {
									if (AddonManagerPlugin.getInstance() == null) {
										b = new JButton("<html><small>Addon-Manager<br>not loaded");
										b.setEnabled(false);
									} else {
										b = GUIhelper.getWebsiteDownloadButton(title, null, System.getProperty("java.io.tmpdir") + ReleaseInfo.getFileSeparator(),
															"<html>" +
																				"Please manually download the following file(s):<br><br>" +
																				"<code>" + AttributeHelper.getStringList(urls, "<br>") + "</code><br><br>" +
																				"Then move the download file into the addon folder of the program!<br>" +
																				"(the addon folder will be automatically opened in a few seconds)<br><br>",
															urls,
															"Manual Download",
															null, new Runnable() {
																public void run() {
																	for (String u : urls) {
																		if (u.toLowerCase().endsWith(".jar")) {
																			if (u.indexOf("/") >= 0)
																				u = u.substring(u.lastIndexOf("/") + "/".length());
																			try {
																				AddonManagerPlugin.getInstance().installAddon(System.getProperty("java.io.tmpdir"), u);
																			} catch (Exception e) {
																				ErrorMsg.addErrorMessage(e);
																			} catch (Error err) {
																				ErrorMsg.addErrorMessage("Unexpected error: " + err.getMessage());
																			}
																		}
																	}
																}
															});
										((JButton) b).setText("Install Add-on");
										((JButton) b).putClientProperty("addon-version", title);
									}
									break;
								}
								case PREFERENCE: {
									b = GUIhelper.getWebsiteDownloadButton(title, null, ReleaseInfo.getAppFolderWithFinalSep(),
														"<html>" +
																			"Please manually download the following file(s):<br><br>" +
																			"<code>" + AttributeHelper.getStringList(urls, "<br>") + "</code><br><br>" +
																			"Then move the download file into the preferences folder of the program!<br>" +
																			"(the preferences folder will be automatically opened in a few seconds)<br><br>", urls,
														"Manual Download", null);
									((JButton) b).addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											MainFrame.showMessageDialog("<html>" +
																"After the download is finished, please restart the<br>" +
																"program to make updated/newly preferences or database files available!<br>" +
																"Click OK to start download!", "Information");
										}
									});
									((JButton) b).setText(title);
									break;
								}
								case URL: {
									b = new JMButton(title);
									((JButton) b).addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											for (String u : urls)
												AttributeHelper.showInBrowser(u);
										}
									});
									break;
								}
								case FEED: {
									b = new JMButton("Subscribe to \"" + title + "\"");
									
									((JButton) b).addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											if (rfm.registerNewFeed(title, urls)) {
												if (newsButton != null)
													newsButton.doClick();
											} else
												MainFrame.showMessage("Subscription to Feed(-URLs) exists already!", MessageType.ERROR, 3000);
										}
									});
									break;
								}
								case DOWNLOAD: {
									b = GUIhelper.getWebsiteDownloadButton(title, null, null,
														"<html>" +
																			"Please manually download the following file(s):<br><br>" +
																			"<code>" + AttributeHelper.getStringList(urls, "<br>") + "</code><br><br>" +
																			"Then move the downloaded file into the desired location at your computer!<br><br>",
														urls,
														"Manual Download", null);
									((JButton) b).addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
										}
									});
									((JButton) b).setText(title);
									break;
								}
							}
							if (b != null) {
								String tooltip = "<html>";
								for (String tt : urls)
									tooltip += tt + "<br>";
								((JButton) b).setToolTipText(tooltip);
								((JButton) b).setBackground(Color.white);
								buttons.add(b);
							}
						}
					}
					a++;
				}
				return text;
			}
		};
		
		Runnable finishSwingTask = new Runnable() {
			
			public void run() {
				for (int i = 0; i < urls.size(); i++) {
					FolderPanel items = fpList.get(i);
					ArrayList<GuiRow> newsItems = idx2newsItems.get(i);
					items.clearGuiComponentList();
					if (newsItems == null || newsItems.size() <= 0) {
						JLabel jl = new JLabelHTMLlink("<html><small><b>No news, click here to visit website</b>", urls.get(i), "Open feed URL in browser");
						items.addGuiComponentRow(jl, null, false, 5);
					} else {
						for (GuiRow gr : newsItems) {
							items.addGuiComponentRow(gr.left, gr.right, false, 2);
						}
					}
					items.layoutRows();
				}
				if (finishTask != null)
					finishTask.run();
			}
		};
		
		BackgroundTaskHelper.issueSimpleTask(null, "Init...", workTask, finishSwingTask, status);
	}
	
	protected static String getContentText(List<?> contents, boolean removeHTML) {
		StringBuilder res = new StringBuilder();
		for (Object o : contents) {
			if (o instanceof SyndContent) {
				SyndContent sc = (SyndContent) o;
				String s = sc.getValue();
				if (removeHTML)
					s = StringManipulationTools.removeHTMLtags(s);
				res.append(s + "...");
			}
		}
		return res.toString();
	}
}
