package org.edumips64.ui.swing;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;

import com.sun.javafx.application.PlatformImpl;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class HelpViewer extends JPanel {  
  private Stage stage;
  private WebView browser;
  private JFXPanel jfxPanel;
  private WebEngine webEngine;

  public HelpViewer(String helpPath) {
    jfxPanel = new JFXPanel();  

    PlatformImpl.startup(() -> {
        stage = new Stage();
        stage.setTitle("EduMIPS64 Help");
        stage.setResizable(true);

        Group root = new Group();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.load(new File(helpPath).toURI().toString());

        var children = root.getChildren();
        children.add(browser);

        jfxPanel.setScene(scene);
     });

    setLayout(new BorderLayout());
    add(jfxPanel, BorderLayout.CENTER);
  }
}