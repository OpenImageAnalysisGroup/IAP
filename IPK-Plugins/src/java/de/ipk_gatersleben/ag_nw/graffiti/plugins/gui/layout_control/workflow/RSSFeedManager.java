package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow;

import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.FolderPanel;
import org.HelperClass;
import org.Release;
import org.ReleaseInfo;
import org.UpdateInfoResult;
import org.graffiti.editor.MainFrame;
import org.graffiti.managers.pluginmgr.RSSfeedDefinition;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.Addon;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.addons.AddonManagerPlugin;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class RSSFeedManager implements HelperClass {
	
	public static String urlSeparator = "|";
	public static String splitUrlSeparator = "\\|";
	
	private ArrayList<String> urls;
	private ArrayList<String> desc;
	private ArrayList<Boolean> removeHTML;
	private ArrayList<FolderPanel> newsComponents;
	private ArrayList<String> date;
	
	private double border;
	private int maxCol = 0;
	
	static RSSFeedManager instance = null;
	
	public RSSFeedManager(double border) {
		instance = this;
		this.border = border;
		// check if the feed-textfile is already there, otherwise create standard-feeds
		// checkRSSfile();
	}
	
	public void checkRSSfile() {
		if (!(new File(getFeedtextfile()).exists())) {
			String text = "";
			text = getStandardFeedDefinition(text);
			writeFeedTextFile(text);
		} else {
			if (ReleaseInfo.isUpdated() == UpdateInfoResult.UPDATED) {
				try {
					String stored_feeds = TextFile.read(getFeedtextfile());
					String updated_feeds = getStandardFeedDefinition(stored_feeds);
					if (!stored_feeds.equals(updated_feeds))
						writeFeedTextFile(updated_feeds);
				} catch (Exception e) {
					// empty
				}
				
			}
		}
	}
	
	private void writeFeedTextFile(String text) {
		try {
			TextFile.writeE(getFeedtextfile(), text, System.getProperty("file.encoding"));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private String getStandardFeedDefinition(String text) {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			String url1 = "http://sourceforge.net/export/rss2_projnews.php?group_id=196037" + urlSeparator
								+ "http://kgml-ed.ipk-gatersleben.de/KGML-ED/Notes/rss.xml";
			if (text.indexOf(url1) <= 0)
				text = "KGML-ED Notes" + System.getProperty("line.separator") +
									url1 + System.getProperty("line.separator");
		} else {
			String url2 = "http://sourceforge.net/export/rss2_projnews.php?group_id=196037" + urlSeparator
								+ "http://kgml-ed.ipk-gatersleben.de/KGML-ED/VANTED%20news/rss.xml";
			if (text.indexOf(url2) <= 0)
				text = "Development Notes" + System.getProperty("line.separator") +
									url2 + System.getProperty("line.separator");
		}
		String url3 = "http://vanted.ipk-gatersleben.de/literature.xml";
		if (text.indexOf(url3) <= 0)
			text += "Publications" + System.getProperty("line.separator") +
								url3 + System.getProperty("line.separator");
		
		return text;
	}
	
	public static RSSFeedManager getInstance() {
		return instance;
	}
	
	public String getFeedtextfile() {
		return ReleaseInfo.getAppFolderWithFinalSep() + "rss_feeds.txt";
	}
	
	public void loadRegisteredFeeds() {
		urls = new ArrayList<String>();
		desc = new ArrayList<String>();
		removeHTML = new ArrayList<Boolean>();
		newsComponents = new ArrayList<FolderPanel>();
		date = new ArrayList<String>();
		
		String feeds = "";
		
		try {
			checkRSSfile();
			feeds = TextFile.read(getFeedtextfile());
			
			feeds += getPluginFeeds();
			
			boolean even = true;
			
			for (String s : feeds.split("\n")) {// TextFile.read appends an "\n" instead of system-line-seperator
				if (s.trim().length() == 0)
					continue;
				try {
					if (even) {
						desc.add(s.split(splitUrlSeparator)[0]);
						try {// if there is no date
							date.add(s.split(splitUrlSeparator)[1]);
						} catch (Exception e) {
							date.add(null);
						}
						removeHTML.add(true);
					} else
						urls.add(s);
					even = !even;
				} catch (Exception ee) {
					ErrorMsg.addErrorMessage(ee);
				}
			}
			for (int descI = 0; descI < desc.size(); descI++) {
				addFolderPanel(desc.get(descI), border);
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static final String pluginTag = "<!--(Plugin-Feed)-->";
	
	private String getPluginFeeds() {
		String result = "";
		Collection<RSSfeedDefinition> feeds = MainFrame.getInstance().getPluginManager().getPluginFeeds();
		if (AddonManagerPlugin.getInstance() != null)
			for (Addon addon : AddonManagerPlugin.getInstance().getAddons()) {
				if (!addon.isActive() && addon.getDescription().hasRSSfeedDefined()) {
					feeds.add(addon.getDescription().getFeed());
				}
			}
		for (RSSfeedDefinition def : feeds) {
			String url = def.getURL();
			String name = def.getName() + pluginTag;
			if (result.indexOf(url) <= 0)
				result += name + "\n" +
									url + "\n";
		}
		return result;
	}
	
	public boolean registerNewFeed(String description, String[] newUrls) {
		
		String newURLstring = "";
		for (String s : newUrls)
			newURLstring += s + urlSeparator;
		newURLstring = newURLstring.substring(0, newURLstring.length() - 1);
		
		boolean isnew = true;
		for (String s : urls) {
			if (s.equalsIgnoreCase(newURLstring)) {
				isnew = false;
				break;
			}
		}
		if (isnew) {
			desc.add(description);
			if (newURLstring.contains("feed://"))
				newURLstring.replaceFirst("feed://", "http://");
			urls.add(newURLstring);
			date.add(null);
			removeHTML.add(true);
			saveRegisteredFeeds();
			return true;
		} else
			return false;
	}
	
	public void saveRegisteredFeeds() {
		String text = "";
		for (int i = 0; i < desc.size(); i++) {
			
			if (desc.get(i) != null && desc.get(i).endsWith(RSSFeedManager.pluginTag))
				continue;
			
			if (i >= desc.size())
				continue;
			if (i >= urls.size())
				continue;
			
			text += desc.get(i) + ((date.get(i) == null) ? "" : urlSeparator + date.get(i)) + System.getProperty("line.separator");
			text += urls.get(i) + System.getProperty("line.separator");
		}
		try {
			TextFile.writeE(getFeedtextfile(), text, System.getProperty("file.encoding"));
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public ArrayList<String> getUrls() {
		return urls;
	}
	
	public ArrayList<String> getDesc() {
		return desc;
	}
	
	public ArrayList<Boolean> getRemoveHTML() {
		return removeHTML;
	}
	
	public String getDate(int a) {
		return date.get(a);
	}
	
	public void setDate(int a, String d) {
		date.set(a, d);
	}
	
	public ArrayList<FolderPanel> getNewsComponents() {
		return newsComponents;
	}
	
	private JLabel getCustomizedLabel(JLabel label) {
		label.setBackground(Color.WHITE);
		return label;
	}
	
	public void addFolderPanel(String title, double border) {
		FolderPanel items = new FolderPanel("<html><font color='gray'>" + title, true, true, false, null);
		items.setColumnStyle(TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
		items.addGuiComponentRow(
							getCustomizedLabel(new JLabel("<html><small>" +
												"News have not been downloaded.")), null, false, 5);
		items.enableSearch(true);
		items.addDefaultTextSearchFilter();
		items.setMaximumRowCount(1);
		items.layoutRows();
		items.setBorder(BorderFactory.createEmptyBorder(0, 0, (int) border, 0));
		newsComponents.add(items);
	}
	
	public void setWordWrap(int maxCol) {
		this.maxCol = maxCol;
	}
	
	public int getMaxCol() {
		return maxCol;
	}
}
