package org.edumips64.ui.swing;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

import com.sun.javafx.application.PlatformImpl;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class HelpViewerFrame extends JFrame {  
  private Stage stage;
  private WebView browser;
  private JFXPanel jfxPanel;
  private WebEngine webEngine;

  public HelpViewerFrame(String helpPath, String title) {
    jfxPanel = new JFXPanel();  

    PlatformImpl.startup(() -> {
        stage = new Stage();
        stage.setTitle("EduMIPS64 Help");
        stage.setResizable(true);

        var root = new StackPane();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.load(new File(helpPath).toURI().toString());

        root.getChildren().add(browser);

        jfxPanel.setScene(scene);
     });

    getContentPane().add(jfxPanel);
    setMinimumSize(new Dimension(800, 600));
    setTitle(title);
  }
}