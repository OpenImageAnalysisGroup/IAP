package org.graffiti.editor;

import java.util.HashMap;

import javax.swing.JTabbedPane;

import org.graffiti.plugin.view.View;
import org.graffiti.plugin.view.ViewListener;
import org.graffiti.session.EditorSession;
import org.graffiti.session.Session;

public class FrameTabbedPane extends JTabbedPane implements ViewListener {
	
	HashMap<String, Integer> tab2index = new HashMap<String, Integer>();
	
	@Override
	public void viewChanged(View newView) {
		Session s = GravistoService.getInstance().getSessionFromView(newView);
		if (s instanceof EditorSession) {
			int idx = tab2index.get(((EditorSession) s).getFileNameFull());
			if (idx >= 0) {
				setSelectedIndex(idx);
				System.out.println(idx);
				
				repaint();
				validate();
			}
		}
	}
	
	public void sessionChanged() {
		removeAll();
		int cnt = 0;
		tab2index.clear();
		for (EditorSession es : MainFrame.getEditorSessions()) {
			addTab(es.getFileName(), null, null, es.getFileNameFull());
			tab2index.put(es.getFileNameFull(), cnt++);
		}
		repaint();
		validate();
	}
	
}
