package org.edumips64.ui.swing;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class HelpViewerSwingFrame extends JFrame {

  public HelpViewerSwingFrame(String helpPath, String title) {

    var editorPane = new JEditorPane();
    editorPane.setEditable(false);
    try {
      java.net.URL helpURL = new File(helpPath).toURL();
      editorPane.setPage(helpURL);
    } catch (Exception e) {
      System.out.println("yo.");
    }

    // Put the editor pane in a scroll pane.
    var editorScrollPane = new JScrollPane(editorPane);
    editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    editorScrollPane.setPreferredSize(new Dimension(250, 145));
    editorScrollPane.setMinimumSize(new Dimension(10, 10));

    // TODO: implement imagecache: http://java-sl.com/tip_local_images.html

    getContentPane().add(editorScrollPane);
    setMinimumSize(new Dimension(800, 600));
    setTitle(title);
  }
}