package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;


import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.bouncycastle.jce.provider.symmetric.VMPC.Mappings;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_actions.SideGuiComponent;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import plyvis.PLYSettings;
import plyvis.PLYVISMenuBar;

/**
 * 
 * @author Jean-Michel Pape
 *
 * Visualization demo for point cloud data (.ply files).
 */
public class ActionPLYVIS extends AbstractNavigationAction {

	private NavigationButton src;
	private SubScene visGroup;
	Group g;
	Stage primaryStage;
	BorderPane bp;
	
	public ActionPLYVIS(String string) {
		super(string);
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}

	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<>(currentSet);
		currentSet.add(src);
		return res;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		
		JFXPanel jp = new JFXPanel();
		
		
		Platform.runLater(() -> {
			jp.setScene(createScene(jp));
		});
		
		MainPanelComponent mp = new MainPanelComponent(jp);
		return mp;
	}

	private Scene createScene(JFXPanel jp) {
		new PLYSettings();
		HBox hbox = new HBox(4.0);
		bp = new BorderPane();
		bp.setTop(hbox);
		bp.setMinWidth(1024);
		
		Stage ss = new Stage();
		
		ss.setWidth(jp.getWidth());
		ss.setWidth(jp.getHeight());
		
		Node m = new PLYVISMenuBar(ss, visGroup, bp).getNode();
		hbox.getChildren().addAll(m);
		HBox.setHgrow(m, Priority.ALWAYS);
		
		g = new Group();
		g.getChildren().addAll(bp);
		Scene s = new Scene(g, jp.getWidth(), jp.getHeight());
		
		return s;
	}

	@Override
	public String getDefaultTitle() {
		return "3D Visualization";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/plyvis_logo.png";
	}

	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		// TODO Auto-generated method stub
		return null;
	}

}
